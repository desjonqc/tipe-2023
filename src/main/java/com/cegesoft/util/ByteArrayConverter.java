package com.cegesoft.util;

import java.nio.ByteBuffer;

/**
 * Classe utilitaire pour convertir des tableaux de nombres en tableau de bytes et inversement.
 */
public class ByteArrayConverter {

    public static byte[] floatsToBytes(float[] floats) {
        ByteBuffer buffer = ByteBuffer.allocate(floats.length * 4);
        for (int i = 0; i < floats.length; i++) {
            buffer.position(4 * i); // 4 bytes per float, so we multiply by 4 to get the correct position in the buffer
            buffer.putFloat(floats[i]);
        }
        return buffer.array();
    }

    public static void bytesToFloats(byte[] bytes, float[] floats) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        for (int i = 0; i < floats.length; i++) {
            buffer.position(4 * i); // 4 bytes per float, so we multiply by 4 to get the correct position in the buffer
            floats[i] = buffer.getFloat();
        }
    }

    public static byte[] intsToBytes(int[] ints) {
        ByteBuffer buffer = ByteBuffer.allocate(ints.length * 4);
        for (int i = 0; i < ints.length; i++) {
            buffer.position(4 * i); // 4 bytes per float, so we multiply by 4 to get the correct position in the buffer
            buffer.putInt(ints[i]);
        }
        return buffer.array();
    }

    public static void bytesToInts(byte[] bytes, int[] ints) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        for (int i = 0; i < ints.length; i++) {
            buffer.position(4 * i); // 4 bytes per float, so we multiply by 4 to get the correct position in the buffer
            ints[i] = buffer.getInt();
        }
    }

    public static byte[] shortsToBytes(short[] shorts) {
        ByteBuffer buffer = ByteBuffer.allocate(shorts.length * 2);
        for (int i = 0; i < shorts.length; i++) {
            buffer.position(2 * i); // 4 bytes per float, so we multiply by 4 to get the correct position in the buffer
            buffer.putShort(shorts[i]);
        }
        return buffer.array();
    }

    public static void bytesToShorts(byte[] bytes, short[] shorts) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        for (int i = 0; i < shorts.length; i++) {
            buffer.position(2 * i); // 4 bytes per float, so we multiply by 4 to get the correct position in the buffer
            shorts[i] = buffer.getShort();
        }
    }

    public static double log2(double x) {
        return Math.log(x) / Math.log(2);
    }

    public static int ceilLog2(double x) {
        return (int) Math.ceil(log2(x));
    }

    public static int maskGenerator(int bits, int offset) {
        int mask = 0;
        for (int i = 0; i < bits; i++) {
            mask |= 1 << (offset + i);
        }
        return mask;
    }

}
