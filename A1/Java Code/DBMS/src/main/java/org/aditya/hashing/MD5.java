package org.aditya.hashing;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * This class implements HashingAlgorithm interface for generating MD5 hash for a given string.
 */
public class MD5 implements HashingAlgorithm {
    /**
     * Generates MD5 hash.
     *
     * @param str input string to be hashed.
     * @return Hashed string.
     */
    @Override
    public String getHash(String str) {
        StringBuilder hash = new StringBuilder();
        try {
            byte[] hashInBytes = MessageDigest.getInstance("MD5").digest(str.getBytes(StandardCharsets.UTF_8));
            for (byte b : hashInBytes) {
                String h = Integer.toHexString(0xff & b);
                if (h.length() == 1) {
                    hash.append('0');
                }
                hash.append(h);
            }
            return hash.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
