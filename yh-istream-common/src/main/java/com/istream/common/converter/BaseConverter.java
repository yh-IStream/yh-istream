package com.istream.common.converter;

import java.util.List;

/**
 * Entity ↔ DTO 基础转换器接口
 *
 * <p>所有 MapStruct Converter 继承此接口，统一转换方法命名。
 * 子接口使用 {@code @Mapper(componentModel = "spring")} 标注，
 * 编译期自动生成实现类并注入 Spring 容器。</p>
 *
 * @param <E> Entity 类型
 * @param <D> DTO 类型
 * @author istream
 * @since 2026-08-17
 */
public interface BaseConverter<E, D> {

    /**
     * Entity → DTO
     */
    D toDto(E entity);

    /**
     * Entity 列表 → DTO 列表
     */
    List<D> toDtoList(List<E> entities);

    /**
     * DTO → Entity
     */
    E toEntity(D dto);

    /**
     * DTO 列表 → Entity 列表
     */
    List<E> toEntityList(List<D> dtos);
}