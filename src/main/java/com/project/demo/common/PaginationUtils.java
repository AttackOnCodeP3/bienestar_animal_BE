package com.project.demo.common;

import com.project.demo.logic.entity.http.Meta;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * Utility class for handling pagination-related operations.
 * Provides methods to build a Pageable object and to create a Meta object
 * for pagination metadata.
 * @author dgutierrez
 */
public class PaginationUtils {

    public static Pageable buildPageable(int page, int size) {
        int safePage = Math.max(0, page - 1);
        return PageRequest.of(safePage, size);
    }

    public static Meta buildMeta(HttpServletRequest request, Page<?> pageData) {
        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(pageData.getTotalPages());
        meta.setTotalElements(pageData.getTotalElements());
        meta.setPageNumber(pageData.getNumber() + 1);
        meta.setPageSize(pageData.getSize());
        return meta;
    }
}

