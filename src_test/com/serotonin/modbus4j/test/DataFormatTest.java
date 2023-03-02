package com.serotonin.modbus4j.test;

import com.serotonin.modbus4j.code.DataType;
import com.serotonin.modbus4j.locator.BinaryLocator;
import com.serotonin.modbus4j.locator.NumericLocator;
import com.serotonin.modbus4j.locator.StringLocator;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

public class DataFormatTest {

    public static void main(String[] args) {
        byte[] data = {1, 0};
        byte[] data1 = {0, 0, 1, 0};
        byte[] data2 = {0, 0, 0, 0, 1, 0};
        byte[] data3 = {0, 0, 0, 0, 0, 0, 1, 0};
        check("aBoolean", Boolean.TRUE, new BinaryLocator(1, 1, 0).bytesToValueRealOffset(data, 0));
        check("aBoolean1", Boolean.FALSE, new BinaryLocator(1, 3, 0, 0).bytesToValueRealOffset(data, 0));
        check("aBoolean2", Boolean.FALSE, new BinaryLocator(1, 3, 0, 1).bytesToValueRealOffset(data, 0));
        check("aBoolean3", Boolean.FALSE, new BinaryLocator(1, 3, 0, 2).bytesToValueRealOffset(data, 0));
        check("aBoolean4", Boolean.FALSE, new BinaryLocator(1, 3, 0, 4).bytesToValueRealOffset(data, 0));
        check("aBoolean5", Boolean.FALSE, new BinaryLocator(1, 3, 0, 5).bytesToValueRealOffset(data, 0));

        check("s", "ttee", new StringLocator(1, 3, 0, DataType.CHAR, 2).bytesToValueRealOffset("tteesstt".getBytes(StandardCharsets.UTF_8), 0));
        check("s1", "tteesstt", new StringLocator(1, 3, 0, DataType.VARCHAR, 4).bytesToValueRealOffset("tteesstt1".getBytes(StandardCharsets.UTF_8), 0));

        check("number", 256, new NumericLocator(1, 3, 0, DataType.TWO_BYTE_INT_UNSIGNED).bytesToValueRealOffset(data, 0));
        check("number1", Short.valueOf("256"), new NumericLocator(1, 3, 0, DataType.TWO_BYTE_INT_SIGNED).bytesToValueRealOffset(data, 0));
        check("number2", 1, new NumericLocator(1, 3, 0, DataType.TWO_BYTE_INT_UNSIGNED_SWAPPED).bytesToValueRealOffset(data, 0));
        check("number3", Short.valueOf("1"), new NumericLocator(1, 3, 0, DataType.TWO_BYTE_INT_SIGNED_SWAPPED).bytesToValueRealOffset(data, 0));
        check("number4", 256L, new NumericLocator(1, 3, 0, DataType.FOUR_BYTE_INT_UNSIGNED).bytesToValueRealOffset(data1, 0));
        check("number5", 256, new NumericLocator(1, 3, 0, DataType.FOUR_BYTE_INT_SIGNED).bytesToValueRealOffset(data1, 0));
        check("number6", 16777216L, new NumericLocator(1, 3, 0, DataType.FOUR_BYTE_INT_UNSIGNED_SWAPPED).bytesToValueRealOffset(data1, 0));
        check("number7", 16777216, new NumericLocator(1, 3, 0, DataType.FOUR_BYTE_INT_SIGNED_SWAPPED).bytesToValueRealOffset(data1, 0));
        check("number8", 65536L, new NumericLocator(1, 3, 0, DataType.FOUR_BYTE_INT_UNSIGNED_SWAPPED_SWAPPED).bytesToValueRealOffset(data1, 0));
        check("number9", 65536, new NumericLocator(1, 3, 0, DataType.FOUR_BYTE_INT_SIGNED_SWAPPED_SWAPPED).bytesToValueRealOffset(data1, 0));
        check("number10", 11.11F, new NumericLocator(1, 3, 0, DataType.FOUR_BYTE_FLOAT).bytesToValueRealOffset(new byte[]{65, 49, -62, -113}, 0));
        check("number11", 11.11F, new NumericLocator(1, 3, 0, DataType.FOUR_BYTE_FLOAT_SWAPPED).bytesToValueRealOffset(new byte[]{-62, -113, 65, 49}, 0));
        check("number13", BigInteger.valueOf(256), new NumericLocator(1, 3, 0, DataType.EIGHT_BYTE_INT_UNSIGNED).bytesToValueRealOffset(data3, 0));
        check("number14", 256L, new NumericLocator(1, 3, 0, DataType.EIGHT_BYTE_INT_SIGNED).bytesToValueRealOffset(data3, 0));
        check("number15", BigInteger.valueOf(72057594037927936L), new NumericLocator(1, 3, 0, DataType.EIGHT_BYTE_INT_UNSIGNED_SWAPPED).bytesToValueRealOffset(data3, 0));
        check("number16", 72057594037927936L, new NumericLocator(1, 3, 0, DataType.EIGHT_BYTE_INT_SIGNED_SWAPPED).bytesToValueRealOffset(data3, 0));
        check("number17", 11.11D, new NumericLocator(1, 3, 0, DataType.EIGHT_BYTE_FLOAT).bytesToValueRealOffset(new byte[]{64, 38, 56, 81, -21, -123, 30, -72}, 0));
        check("number18", 11.11D, new NumericLocator(1, 3, 0, DataType.EIGHT_BYTE_FLOAT_SWAPPED).bytesToValueRealOffset(new byte[]{30, -72, -21, -123, 56, 81, 64, 38}, 0));
        check("number19", Short.valueOf("100"), new NumericLocator(1, 3, 0, DataType.TWO_BYTE_BCD).bytesToValueRealOffset(data, 0));
        check("number20", 100, new NumericLocator(1, 3, 0, DataType.FOUR_BYTE_BCD).bytesToValueRealOffset(data1, 0));
        check("number21", 1000000, new NumericLocator(1, 3, 0, DataType.FOUR_BYTE_BCD_SWAPPED).bytesToValueRealOffset(data1, 0));
        check("number22", BigInteger.valueOf(256), new NumericLocator(1, 3, 0, DataType.FOUR_BYTE_MOD_10K).bytesToValueRealOffset(data1, 0));
        check("number23", BigInteger.valueOf(256), new NumericLocator(1, 3, 0, DataType.SIX_BYTE_MOD_10K).bytesToValueRealOffset(data2, 0));
        check("number24", BigInteger.valueOf(256), new NumericLocator(1, 3, 0, DataType.EIGHT_BYTE_MOD_10K).bytesToValueRealOffset(data3, 0));
        check("number25", BigInteger.valueOf(2560000), new NumericLocator(1, 3, 0, DataType.FOUR_BYTE_MOD_10K_SWAPPED).bytesToValueRealOffset(data1, 0));
        check("number26", BigInteger.valueOf(25600000000L), new NumericLocator(1, 3, 0, DataType.SIX_BYTE_MOD_10K_SWAPPED).bytesToValueRealOffset(data2, 0));
        check("number27", BigInteger.valueOf(256000000000000L), new NumericLocator(1, 3, 0, DataType.EIGHT_BYTE_MOD_10K_SWAPPED).bytesToValueRealOffset(data3, 0));
        check("number28", 0, new NumericLocator(1, 3, 0, DataType.ONE_BYTE_INT_UNSIGNED_LOWER).bytesToValueRealOffset(data, 0));
        check("number29", 1, new NumericLocator(1, 3, 0, DataType.ONE_BYTE_INT_UNSIGNED_UPPER).bytesToValueRealOffset(data, 0));
        System.out.println("--------end--------");
    }

    private static void check(String msg, Object expected, Object actual) {
        System.out.println(String.format("%s [%s] expected: %s, actual: %s", actual.equals(expected) ? "SUCCESS" : "FAILED", msg, expected, actual));
    }
}
