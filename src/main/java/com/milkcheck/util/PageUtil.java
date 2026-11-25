package com.milkcheck.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;


public final class PageUtil {

    private static final int MAX_SIZE = 50;

    private PageUtil() {}

    /** Clamps page/size to safe values so clients cannot request huge pages. */
    public static Pageable of(int page, int size, @NonNull Sort sort) {
        return PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_SIZE), sort);
    }
}