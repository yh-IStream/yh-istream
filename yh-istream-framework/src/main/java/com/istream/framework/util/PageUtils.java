package com.istream.framework.util;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;
import java.util.function.Function;

/**
 * 分页工具类
 *
 * <p>提供 Entity 分页 → DTO 分页的通用转换方法。</p>
 *
 * @author istream
 * @since 2026-09-18
 */
public final class PageUtils {

    private PageUtils() {
    }

    /**
     * Entity 分页 → DTO 分页
     *
     * <p>保留分页元数据（当前页、每页大小、总记录数），仅转换记录列表。</p>
     *
     * @param entityPage Entity 分页结果
     * @param converter  单条转换函数（如 {@code converter::toDto}）
     * @param <E>        Entity 类型
     * @param <D>        DTO 类型
     * @return DTO 分页结果
     */
    public static <E, D> IPage<D> toDtoPage(IPage<E> entityPage, Function<E, D> converter) {
        IPage<D> dtoPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        dtoPage.setRecords(entityPage.getRecords().stream().map(converter).toList());
        return dtoPage;
    }
}