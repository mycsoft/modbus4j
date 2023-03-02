package com.serotonin.modbus4j.util;

import com.serotonin.modbus4j.code.DataType;
import com.serotonin.modbus4j.code.RegisterRange;

import java.math.BigInteger;
import java.nio.charset.Charset;

/**
 * 数据解析类
 *
 * @author LiuHuan
 * @since 3.1.1
 */
public class DataParser {

    public static Boolean bytesToBoolean(byte[] data, int offset, int range, int bit) {
        // If this is a coil or input, convert to boolean.
        if (range == RegisterRange.COIL_STATUS || range == RegisterRange.INPUT_STATUS) {
            return (((data[offset / 8] & 0xff) >> (offset % 8)) & 0x1) == 1;
        }
        // For the rest of the types, we double the normalized offset to account for short to byte.
        offset *= 2;

        // We could still be asking for a binary if it's a bit in a register.
        return (((data[offset + 1 - bit / 8] & 0xff) >> (bit % 8)) & 0x1) == 1;
    }

    public static String bytesToString(byte[] data, int offset, int registerCount, int dataType, Charset charset) {
        offset *= 2;
        int length = registerCount * 2;

        if (dataType == DataType.CHAR) {
            return new String(data, offset, length, charset);
        }

        if (dataType == DataType.VARCHAR) {
            int nullPos = -1;
            for (int i = offset; i < offset + length; i++) {
                if (data[i] == 0) {
                    nullPos = i;
                    break;
                }
            }

            if (nullPos == -1) {
                return new String(data, offset, length, charset);
            }
            return new String(data, offset, nullPos, charset);
        }

        throw new IllegalArgumentException("Unsupported data type: " + dataType);
    }

    public static Number bytesToNumber(byte[] data, int offset, int dataType) {
        offset *= 2;

        switch (dataType) {
            // 2 bytes
            case DataType.TWO_BYTE_INT_UNSIGNED:
                return ((data[offset] & 0xff) << 8) | (data[offset + 1] & 0xff);
            case DataType.TWO_BYTE_INT_SIGNED:
                return (short) (((data[offset] & 0xff) << 8) | (data[offset + 1] & 0xff));
            case DataType.TWO_BYTE_INT_UNSIGNED_SWAPPED:
                return ((data[offset + 1] & 0xff) << 8) | (data[offset] & 0xff);
            case DataType.TWO_BYTE_INT_SIGNED_SWAPPED:
                return (short) (((data[offset + 1] & 0xff) << 8) | (data[offset] & 0xff));
            case DataType.TWO_BYTE_BCD:
                StringBuilder sb = new StringBuilder();
                appendBcd(sb, data[offset]);
                appendBcd(sb, data[offset + 1]);
                return Short.parseShort(sb.toString());
            // 1 byte
            case DataType.ONE_BYTE_INT_UNSIGNED_LOWER:
                return data[offset + 1] & 0xff;
            case DataType.ONE_BYTE_INT_UNSIGNED_UPPER:
                return data[offset] & 0xff;
            // 4 bytes
            case DataType.FOUR_BYTE_INT_UNSIGNED:
                return ((long) (data[offset] & 0xff) << 24) | ((long) (data[offset + 1] & 0xff) << 16)
                        | ((long) (data[offset + 2] & 0xff) << 8) | (data[offset + 3] & 0xff);
            case DataType.FOUR_BYTE_INT_SIGNED:
                return ((data[offset] & 0xff) << 24) | ((data[offset + 1] & 0xff) << 16)
                        | ((data[offset + 2] & 0xff) << 8) | (data[offset + 3] & 0xff);
            case DataType.FOUR_BYTE_INT_UNSIGNED_SWAPPED:
                return ((long) (data[offset + 2] & 0xff) << 24) | ((long) (data[offset + 3] & 0xff) << 16)
                        | ((long) (data[offset] & 0xff) << 8) | (data[offset + 1] & 0xff);
            case DataType.FOUR_BYTE_INT_SIGNED_SWAPPED:
                return ((data[offset + 2] & 0xff) << 24) | ((data[offset + 3] & 0xff) << 16)
                        | ((data[offset] & 0xff) << 8) | (data[offset + 1] & 0xff);
            case DataType.FOUR_BYTE_INT_UNSIGNED_SWAPPED_SWAPPED:
                return ((long) (data[offset + 3] & 0xff) << 24) | ((data[offset + 2] & 0xff) << 16)
                        | ((long) (data[offset + 1] & 0xff) << 8) | (data[offset] & 0xff);
            case DataType.FOUR_BYTE_INT_SIGNED_SWAPPED_SWAPPED:
                return ((data[offset + 3] & 0xff) << 24) | ((data[offset + 2] & 0xff) << 16)
                        | ((data[offset + 1] & 0xff) << 8) | (data[offset] & 0xff);
            case DataType.FOUR_BYTE_FLOAT:
                return Float.intBitsToFloat(((data[offset] & 0xff) << 24) | ((data[offset + 1] & 0xff) << 16)
                        | ((data[offset + 2] & 0xff) << 8) | (data[offset + 3] & 0xff));
            case DataType.FOUR_BYTE_FLOAT_SWAPPED:
                return Float.intBitsToFloat(((data[offset + 2] & 0xff) << 24) | ((data[offset + 3] & 0xff) << 16)
                        | ((data[offset] & 0xff) << 8) | (data[offset + 1] & 0xff));
            case DataType.FOUR_BYTE_BCD:
                StringBuilder sb1 = new StringBuilder();
                appendBcd(sb1, data[offset]);
                appendBcd(sb1, data[offset + 1]);
                appendBcd(sb1, data[offset + 2]);
                appendBcd(sb1, data[offset + 3]);
                return Integer.parseInt(sb1.toString());
            case DataType.FOUR_BYTE_BCD_SWAPPED:
                StringBuilder sb2 = new StringBuilder();
                appendBcd(sb2, data[offset + 2]);
                appendBcd(sb2, data[offset + 3]);
                appendBcd(sb2, data[offset]);
                appendBcd(sb2, data[offset + 1]);
                return Integer.parseInt(sb2.toString());
            //MOD10K types
            case DataType.FOUR_BYTE_MOD_10K_SWAPPED:
                return BigInteger.valueOf((((data[offset + 2] & 0xff) << 8) + (data[offset + 3] & 0xff))).multiply(BigInteger.valueOf(10000L))
                        .add(BigInteger.valueOf((((data[offset] & 0xff) << 8) + (data[offset + 1] & 0xff))));
            case DataType.FOUR_BYTE_MOD_10K:
                return BigInteger.valueOf((((data[offset] & 0xff) << 8) + (data[offset + 1] & 0xff))).multiply(BigInteger.valueOf(10000L))
                        .add(BigInteger.valueOf((((data[offset + 2] & 0xff) << 8) + (data[offset + 3] & 0xff))));
            case DataType.SIX_BYTE_MOD_10K_SWAPPED:
                return BigInteger.valueOf((((data[offset + 4] & 0xff) << 8) + (data[offset + 5] & 0xff))).multiply(BigInteger.valueOf(100000000L))
                        .add(BigInteger.valueOf((((data[offset + 2] & 0xff) << 8) + (data[offset + 3] & 0xff))).multiply(BigInteger.valueOf(10000L)))
                        .add(BigInteger.valueOf((((data[offset] & 0xff) << 8) + (data[offset + 1] & 0xff))));
            case DataType.SIX_BYTE_MOD_10K:
                return BigInteger.valueOf((((data[offset] & 0xff) << 8) + (data[offset + 1] & 0xff))).multiply(BigInteger.valueOf(100000000L))
                        .add(BigInteger.valueOf((((data[offset + 2] & 0xff) << 8) + (data[offset + 3] & 0xff))).multiply(BigInteger.valueOf(10000L)))
                        .add(BigInteger.valueOf((((data[offset + 4] & 0xff) << 8) + (data[offset + 5] & 0xff))));
            case DataType.EIGHT_BYTE_MOD_10K_SWAPPED:
                return BigInteger.valueOf((((data[offset + 6] & 0xff) << 8) + (data[offset + 7] & 0xff))).multiply(BigInteger.valueOf(1000000000000L))
                        .add(BigInteger.valueOf((((data[offset + 4] & 0xff) << 8) + (data[offset + 5] & 0xff))).multiply(BigInteger.valueOf(100000000L)))
                        .add(BigInteger.valueOf((((data[offset + 2] & 0xff) << 8) + (data[offset + 3] & 0xff))).multiply(BigInteger.valueOf(10000L)))
                        .add(BigInteger.valueOf((((data[offset] & 0xff) << 8) + (data[offset + 1] & 0xff))));
            case DataType.EIGHT_BYTE_MOD_10K:
                return BigInteger.valueOf((((data[offset] & 0xff) << 8) + (data[offset + 1] & 0xff))).multiply(BigInteger.valueOf(1000000000000L))
                        .add(BigInteger.valueOf((((data[offset + 2] & 0xff) << 8) + (data[offset + 3] & 0xff))).multiply(BigInteger.valueOf(100000000L)))
                        .add(BigInteger.valueOf((((data[offset + 4] & 0xff) << 8) + (data[offset + 5] & 0xff))).multiply(BigInteger.valueOf(10000L)))
                        .add(BigInteger.valueOf((((data[offset + 6] & 0xff) << 8) + (data[offset + 7] & 0xff))));
            // 8 bytes
            case DataType.EIGHT_BYTE_INT_UNSIGNED:
                byte[] b9 = new byte[9];
                System.arraycopy(data, offset, b9, 1, 8);
                return new BigInteger(b9);
            case DataType.EIGHT_BYTE_INT_SIGNED:
                return ((long) (data[offset] & 0xff) << 56) | ((long) (data[offset + 1] & 0xff) << 48)
                        | ((long) (data[offset + 2] & 0xff) << 40) | ((long) (data[offset + 3] & 0xff) << 32)
                        | ((long) (data[offset + 4] & 0xff) << 24) | ((long) (data[offset + 5] & 0xff) << 16)
                        | ((long) (data[offset + 6] & 0xff) << 8) | (data[offset + 7] & 0xff);
            case DataType.EIGHT_BYTE_INT_UNSIGNED_SWAPPED:
                byte[] b9a = new byte[9];
                b9a[1] = data[offset + 6];
                b9a[2] = data[offset + 7];
                b9a[3] = data[offset + 4];
                b9a[4] = data[offset + 5];
                b9a[5] = data[offset + 2];
                b9a[6] = data[offset + 3];
                b9a[7] = data[offset];
                b9a[8] = data[offset + 1];
                return new BigInteger(b9a);


            case DataType.EIGHT_BYTE_INT_SIGNED_SWAPPED:
                return ((long) (data[offset + 6] & 0xff) << 56) | ((long) (data[offset + 7] & 0xff) << 48)
                        | ((long) (data[offset + 4] & 0xff) << 40) | ((long) (data[offset + 5] & 0xff) << 32)
                        | ((long) (data[offset + 2] & 0xff) << 24) | ((long) (data[offset + 3] & 0xff) << 16)
                        | ((long) (data[offset] & 0xff) << 8) | (data[offset + 1] & 0xff);


            case DataType.EIGHT_BYTE_FLOAT:
                return Double.longBitsToDouble(((long) (data[offset] & 0xff) << 56)
                        | ((long) (data[offset + 1] & 0xff) << 48) | ((long) (data[offset + 2] & 0xff) << 40)
                        | ((long) (data[offset + 3] & 0xff) << 32) | ((long) (data[offset + 4] & 0xff) << 24)
                        | ((long) (data[offset + 5] & 0xff) << 16) | ((long) (data[offset + 6] & 0xff) << 8)
                        | (data[offset + 7] & 0xff));


            case DataType.EIGHT_BYTE_FLOAT_SWAPPED:
                return Double.longBitsToDouble(((long) (data[offset + 6] & 0xff) << 56)
                        | ((long) (data[offset + 7] & 0xff) << 48) | ((long) (data[offset + 4] & 0xff) << 40)
                        | ((long) (data[offset + 5] & 0xff) << 32) | ((long) (data[offset + 2] & 0xff) << 24)
                        | ((long) (data[offset + 3] & 0xff) << 16) | ((long) (data[offset] & 0xff) << 8)
                        | (data[offset + 1] & 0xff));
            default:
                throw new IllegalArgumentException("Unsupported data type: " + dataType);
        }
    }

    private static void appendBcd(StringBuilder sb, byte b) {
        sb.append(bcdNibbleToInt(b, true));
        sb.append(bcdNibbleToInt(b, false));
    }

    private static int bcdNibbleToInt(byte b, boolean high) {
        int n;
        if (high) {
            n = (b >> 4) & 0xf;
        } else {
            n = b & 0xf;
        }
        if (n > 9) {
            n = 0;
        }
        return n;
    }

}
