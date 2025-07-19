package com.urlshortener.core;

/**
 * Service for encoding numbers to Base62 strings and decoding Base62 strings back to numbers.
 * Base62 encoding uses digits (0-9), lowercase letters (a-z), and uppercase letters (A-Z).
 */
public final class Base62Service {

    private static final String BASE62_CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int BASE = BASE62_CHARS.length();
    private static final char[] BASE62_ARRAY = BASE62_CHARS.toCharArray();

    // Private constructor to prevent instantiation
	public Base62Service() {}

    /**
     * Encodes a positive long number to Base62 string.
     *
     * @param number The positive number to encode
     * @return Base62 encoded string
     * @throws IllegalArgumentException if the input number is negative
     */
    public String encode(long number) {
        if (number < 0) {
            throw new IllegalArgumentException("Number must be positive");
        }

        if (number == 0) {
            return String.valueOf(BASE62_ARRAY[0]);
        }

        final StringBuilder encoded = new StringBuilder();

        while (number > 0) {
            encoded.append(BASE62_ARRAY[(int) (number % BASE)]);
            number /= BASE;
        }

        return encoded.reverse().toString();
    }

    /**
     * Decodes a Base62 string back to the original number.
     *
     * @param encoded The Base62 string to decode
     * @return The decoded number
     * @throws IllegalArgumentException if the input string is null, empty, or contains invalid characters
     */
    public long decode(String encoded) {
        if (encoded == null || encoded.isEmpty()) {
            throw new IllegalArgumentException("Encoded string cannot be null or empty");
        }

        long number = 0;

        for (int i = 0; i < encoded.length(); i++) {
            char c = encoded.charAt(i);
            int digit = BASE62_CHARS.indexOf(c);

            if (digit < 0) {
                throw new IllegalArgumentException("Invalid character in encoded string: " + c);
            }

            number = number * BASE + digit;
        }

        return number;
    }
}