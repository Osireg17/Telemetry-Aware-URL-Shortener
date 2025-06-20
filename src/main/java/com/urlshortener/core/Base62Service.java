package com.urlshortener.core;

public class Base62Service {
    
    private static final String BASE62_CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int BASE = BASE62_CHARS.length();
    
    public String encode(long number) {
        if (number == 0) {
            return "0";
        }
        
        StringBuilder encoded = new StringBuilder();
        
        while (number > 0) {
            encoded.append(BASE62_CHARS.charAt((int) (number % BASE)));
            number /= BASE;
        }
        
        return encoded.reverse().toString();
    }
}
