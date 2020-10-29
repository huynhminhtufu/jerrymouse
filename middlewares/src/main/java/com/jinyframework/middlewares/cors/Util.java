package com.jinyframework.middlewares.cors;

import java.util.ArrayList;

import lombok.NonNull;
import lombok.val;

/**
 * The type Util.
 */
public final class Util {
    /**
     * Normalize header string.
     *
     * @param h the h
     * @return the string
     */
    public static String normalizeHeader(@NonNull String h) {
        val split = h.split("-");
        val res = new ArrayList<String>();
        for (val word : split) {
            res.add(word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase());
        }
        return String.join("-", res);
    }
}
