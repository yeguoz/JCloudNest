package icu.yeguo.cloudnest.util;

import java.nio.ByteBuffer;

public class Base62EncoderUtils {
    private static final String BASE62 = "ErdvcIfm9wGD0OlgB2Vhp8iA6CsabotMRzXTqQuWJ54nSFPKUey137HNkZYLxj";
    private static final int FIXED_LENGTH = 10;

    private static long murmurHash64(long input) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(input);
        byte[] bytes = buffer.array();

        int seed = 0x1234ABCD;
        long h1 = seed;
        long h2 = seed;

        for (int i = 0; i < bytes.length; i++) {
            h1 ^= (bytes[i] & 0xffL);
            h1 *= 0x5bd1e9955bd1e995L;
            h1 ^= h1 >>> 47;
        }

        h2 ^= h1;
        h2 *= 0x5bd1e9955bd1e995L;
        h2 ^= h2 >>> 47;

        return h2;
    }

    public static String encodeBase62(long num) {
        StringBuilder sb = new StringBuilder();
        while (num > 0) {
            sb.append(BASE62.charAt((int) (num % 62)));
            num /= 62;
        }
        return sb.reverse().toString();
    }

    public static String generateShortId(long id) {
        long hashed = murmurHash64(id);
        String base62 = encodeBase62(Math.abs(hashed));
        return base62.substring(0, Math.min(FIXED_LENGTH, base62.length()));
    }
}
