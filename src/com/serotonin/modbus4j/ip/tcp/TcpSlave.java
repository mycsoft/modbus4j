/*
/*
 * ============================================================================
 * GNU General Public License
 * ============================================================================
 *
 * Copyright (C) 2006-2011 Serotonin Software Technologies Inc. http://serotoninsoftware.com
 * @author Matthew Lohbihler
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.serotonin.modbus4j.ip.tcp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.serotonin.modbus4j.ModbusSlaveSet;
import com.serotonin.modbus4j.base.BaseMessageParser;
import com.serotonin.modbus4j.base.BaseRequestHandler;
import com.serotonin.modbus4j.base.ModbusUtils;
import com.serotonin.modbus4j.exception.ModbusInitException;
import com.serotonin.modbus4j.ip.encap.EncapMessageParser;
import com.serotonin.modbus4j.ip.encap.EncapRequestHandler;
import com.serotonin.modbus4j.ip.xa.XaMessageParser;
import com.serotonin.modbus4j.ip.xa.XaRequestHandler;
import com.serotonin.modbus4j.sero.messaging.MessageControl;
import com.serotonin.modbus4j.sero.messaging.TestableTransport;
import static java.util.Optional.ofNullable;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * TcpSlave class.</p>
 *
 * @author Matthew Lohbihler , MaYichao
 * @version 5.0.1
 */
@Slf4j
public class TcpSlave extends ModbusSlaveSet {

    // Configuration fields
    private final int port;
    final boolean encapsulated;

    // Runtime fields.
    private ServerSocket serverSocket;
    final ExecutorService executorService;
    final List<TcpConnectionHandler> listConnections = new ArrayList<>();

    /**
     * <p>
     * Constructor for TcpSlave.</p>
     *
     * @param encapsulated a boolean.
     */
    public TcpSlave(boolean encapsulated) {
        this(ModbusUtils.TCP_PORT, encapsulated);
    }

    /**
     * <p>
     * Constructor for TcpSlave.</p>
     *
     * @param port a int.
     * @param encapsulated a boolean.
     */
    public TcpSlave(int port, boolean encapsulated) {
        this.port = port;
        this.encapsulated = encapsulated;
        executorService = Executors.newCachedThreadPool();
    }

    /**
     * {@inheritDoc}
     *
     * @since 3.1.2
     */
    @Override
    public void start() throws ModbusInitException {
        try {
            serverSocket = new ServerSocket(port);

            Socket socket;
            while (true) {
                socket = serverSocket.accept();
                onAcceptSocket(socket);
            }
        } catch (IOException e) {
            throw new ModbusInitException(e);
        }
    }

    /**
     * 当接收到新的socket请求时。
     *
     * @param socket
     * @throws ModbusInitException
     * @since 3.1.2
     */
    protected void onAcceptSocket(Socket socket) throws ModbusInitException {
        //如果hander不为空，则执行这个通道。
        ofNullable(createConnectionHandler(socket)).ifPresent(this::executeConnection);
    }

    /**
     * 根据socket创建新Hanler.
     *
     * @param socket
     * @return 如果当前连接不被允许，则返回null.
     * @throws ModbusInitException
     * @since 3.1.2
     */
    protected TcpConnectionHandler createConnectionHandler(Socket socket) throws ModbusInitException {
        return new TcpConnectionHandler(socket);
    }

    /**
     * 执行一个连接。
     *
     * @param handler
     * @since 3.1.2
     */
    protected void executeConnection(TcpConnectionHandler handler) {
        executorService.execute(handler);
        synchronized (listConnections) {
            listConnections.add(handler);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() {
        // Close the socket first to prevent new messages.
        try {
            serverSocket.close();
        } catch (IOException e) {
            getExceptionHandler().receivedException(e);
        }

        // Close all open connections.
        synchronized (listConnections) {
            for (TcpConnectionHandler tch : listConnections) {
                tch.kill();
            }
            listConnections.clear();
        }

        // Now close the executor service.
        executorService.shutdown();
        try {
            executorService.awaitTermination(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            getExceptionHandler().receivedException(e);
        }
    }

    public class TcpConnectionHandler implements Runnable {

        private final Socket socket;
        @Getter
        private TestableTransport transport;
        private MessageControl conn;
        /**
         * 消息解析器创建器。
         *
         * @since 3.1.2
         */
        @Setter
        private Function<Boolean, BaseMessageParser> messageParserCreater;
        /**
         * 消息处理器创建器。
         *
         * @since 3.1.2
         */
        @Setter
        private Function<Boolean, BaseRequestHandler> messageHandlerCreater;

        /**
         * 连接关闭前处理。
         *
         * @since 3.1.2
         */
        @Setter
        private Consumer<MessageControl> beforeClose;
        /**
         * 连接关闭后。
         *
         * @since 3.1.2
         */
        @Setter
        private Consumer<TcpConnectionHandler> afterClose;

        TcpConnectionHandler(Socket socket) throws ModbusInitException {
            this.socket = socket;
            try {
                transport = new TestableTransport(socket.getInputStream(), socket.getOutputStream());
            } catch (IOException e) {
                throw new ModbusInitException(e);
            }
        }

        /**
         * 执行。
         *
         * @since 3.1.2
         */
        @Override
        public void run() {
            BaseMessageParser messageParser;
            BaseRequestHandler requestHandler;

            if (encapsulated) {
                messageParser = ofNullable(messageParserCreater)
                        .map(f -> f.apply(encapsulated))
                        .orElseGet(() -> new EncapMessageParser(false));
                requestHandler = ofNullable(messageHandlerCreater)
                        .map(f -> f.apply(encapsulated))
                        .orElseGet(() -> new EncapRequestHandler(TcpSlave.this));
            } else {
                messageParser = ofNullable(messageParserCreater)
                        .map(f -> f.apply(encapsulated))
                        .orElseGet(() -> new XaMessageParser(false));
                requestHandler = ofNullable(messageHandlerCreater)
                        .map(f -> f.apply(encapsulated))
                        .orElseGet(() -> new XaRequestHandler(TcpSlave.this));
            }

            conn = new MessageControl();
            conn.setExceptionHandler(getExceptionHandler());

            try {
                conn.start(transport, messageParser, requestHandler, null);
                executorService.execute(transport);
            } catch (IOException e) {
                getExceptionHandler().receivedException(new ModbusInitException(e));
            }

            // Monitor the socket to detect when it gets closed.
            try {
                do {
                    try {
                        transport.testInputStream();
                    } catch (IOException e) {
                        //测试连接失败。
                        log.warn("连接{}测试失败，本连接将要中断。", socket.getInetAddress().getHostAddress());
                        break;
                    }
                    try {
                        TimeUnit.MILLISECONDS.sleep(500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        // no op
                    }
                } while (true);
            } finally {
                ofNullable(beforeClose).ifPresent(c -> c.accept(conn));
                conn.close();
                kill();
                synchronized (listConnections) {
                    listConnections.remove(this);
                }
            }
        }

        public void kill() {
            try {
                socket.close();
            } catch (IOException e) {
                getExceptionHandler().receivedException(new ModbusInitException(e));
            }
            ofNullable(afterClose).ifPresent(c -> c.accept(this));
        }
    }
}
