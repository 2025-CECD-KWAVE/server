package com.example.kwave.global.util;

import java.nio.ByteBuffer;

public class FloatArrayConverter {

    // float[] -> byte[]
    public static byte[] toBytes(float[] vector) {
        ByteBuffer buffer = ByteBuffer.allocate(vector.length * Float.BYTES);

        for (float v : vector) {
            buffer.putFloat(v);
        }
        return buffer.array();
    }

    // byte[] -> float[]
    public static float[] toFloatArray(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        float[] vector = new float[bytes.length / Float.BYTES];

        for (int i = 0; i < vector.length; i++) {
            vector[i] = buffer.getFloat();
        }
        return vector;
    }
}
