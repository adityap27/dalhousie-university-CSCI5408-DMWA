package org.aditya.hashing;

public interface HashingAlgorithm {

    /**
     * Generate hash string from the input string.
     *
     * @param str input string to be hashed.
     * @return Hashed string.
     */
    String getHash(String str);
}
