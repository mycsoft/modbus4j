package com.serotonin.modbus4j.locator;

import com.serotonin.modbus4j.base.ModbusUtils;
import com.serotonin.modbus4j.code.DataType;
import com.serotonin.modbus4j.code.RegisterRange;
import com.serotonin.modbus4j.exception.ModbusIdException;
import com.serotonin.modbus4j.sero.NotImplementedException;
import com.serotonin.modbus4j.util.DataParser;

/**
 * <p>BinaryLocator class.</p>
 *
 * @author Matthew Lohbihler
 * @version 5.0.0
 */
public class BinaryLocator extends BaseLocator<Boolean> {
    private int bit = -1;

    /**
     * <p>Constructor for BinaryLocator.</p>
     *
     * @param slaveId a int.
     * @param range a int.
     * @param offset a int.
     */
    public BinaryLocator(int slaveId, int range, int offset) {
        super(slaveId, range, offset);
        if (!isBinaryRange(range))
            throw new ModbusIdException("Non-bit requests can only be made from coil status and input status ranges");
        validate();
    }

    /**
     * <p>Constructor for BinaryLocator.</p>
     *
     * @param slaveId a int.
     * @param range a int.
     * @param offset a int.
     * @param bit a int.
     */
    public BinaryLocator(int slaveId, int range, int offset, int bit) {
        super(slaveId, range, offset);
        if (isBinaryRange(range))
            throw new ModbusIdException("Bit requests can only be made from holding registers and input registers");
        this.bit = bit;
        validate();
    }

    /**
     * <p>isBinaryRange.</p>
     *
     * @param range a int.
     * @return a boolean.
     */
    public static boolean isBinaryRange(int range) {
        return range == RegisterRange.COIL_STATUS || range == RegisterRange.INPUT_STATUS;
    }

    /**
     * <p>validate.</p>
     */
    protected void validate() {
        super.validate(1);

        if (!isBinaryRange(range))
            ModbusUtils.validateBit(bit);
    }

    /**
     * <p>Getter for the field <code>bit</code>.</p>
     *
     * @return a int.
     */
    public int getBit() {
        return bit;
    }

    /** {@inheritDoc} */
    @Override
    public int getDataType() {
        return DataType.BINARY;
    }

    /** {@inheritDoc} */
    @Override
    public int getRegisterCount() {
        return 1;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "BinaryLocator(slaveId=" + getSlaveId() + ", range=" + range + ", offset=" + offset + ", bit=" + bit
                + ")";
    }

    /** {@inheritDoc} */
    @Override
    public Boolean bytesToValueRealOffset(byte[] data, int offset) {
        return DataParser.bytesToBoolean(data, offset, range, bit);
    }

    /** {@inheritDoc} */
    @Override
    public short[] valueToShorts(Boolean value) {
        throw new NotImplementedException();
    }
}
