package com.ipqualityscore.JavaIPQSDBReader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Utility {
    /*
    * Takes an array of unsigned little endian int bytes and converts it to a long.
    * @return long
    */
    public static final long uVarInt(int[] bytes)
    {
        long x = 0;
        long s = 0;
        for(int i = 0; i < bytes.length; i++){
            int b = bytes[i];
            if(b < 0x80) {
                return x | b<<s;
            }

            x |= b&0x7f << s;
            s += 7;
        }

        return 0;
    }

    public static final long uVarInt(byte[] bytes)
    {
        long x = 0;
        long s = 0;
        for(int i = 0; i < bytes.length; i++){
            int b = bytes[i];
            if(b < 0x80) {
                return x | b<<s;
            }

            x |= b&0x7f << s;
            s += 7;
        }

        return 0;
    }

    public static long toUnsignedInt(byte[] bytes)
    {
        ByteBuffer buffer = ByteBuffer.allocate(4).put(bytes);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.position(0);
        return buffer.getInt() & 0xFFFFFFFFL;
    }
}
