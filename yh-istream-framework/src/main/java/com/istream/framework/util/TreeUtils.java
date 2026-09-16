package com.istream.framework.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * 树形结构构建工具类
 *
 * <p>将扁平列表转换为树形结构，适用于菜单、部门等具有父子关系的数据。</p>
 *
 * <h3>设计</h3>
 * <ul>
 *   <li><b>职责分离</b>：根节点识别与子节点查找使用独立的数据结构，避免 {@code id=0} 与根节点标记
 *       {@code parentId=0} 发生语义碰撞</li>
 *   <li><b>泛型设计</b>：通过 {@code Function}/{@code BiConsumer} 解耦实体类型，不依赖具体实体类</li>
 *   <li><b>边界防护</b>：空列表、null 值安全处理</li>
 * </ul>
 *
 * <h3>约定</h3>
 * <ul>
 *   <li>{@code parentId == null} 或 {@code parentId == 0} 视为根节点</li>
 *   <li>节点按原始列表顺序排列，调用方应在传入前排序</li>
 * </ul>
 *
 * @author yh-istream
 * @since 1.0.0
 */
public final class TreeUtils {

    private TreeUtils() {
    }

    /**
     * 构建树形结构
     *
     * <p>两阶段算法：</p>
     * <ol>
     *   <li><b>分类阶段</b>：将节点分为根节点（parentId=0/null）和非根节点，非根节点按 parentId 分组</li>
     *   <li><b>赋值阶段</b>：遍历所有节点，将对应 parentId 组的节点列表赋值给 children 字段</li>
     * </ol>
     *
     * <p>此设计确保根节点的 children 不会被错误地填充为其他根节点，消除了 {@code id=0} 与
     * 根节点标记 {@code parentId=0} 的语义冲突。</p>
     *
     * @param allNodes       所有节点（扁平列表）
     * @param idGetter       节点 ID 获取器
     * @param parentIdGetter 父节点 ID 获取器
     * @param childrenSetter 子节点列表设置器
     * @param <T>            节点类型
     * @return 根节点列表
     */
    public static <T> List<T> build(List<T> allNodes,
                                     Function<T, Long> idGetter,
                                     Function<T, Long> parentIdGetter,
                                     BiConsumer<T, List<T>> childrenSetter) {
        if (allNodes == null || allNodes.isEmpty()) {
            return new ArrayList<>();
        }

        // ========== 阶段1：分离根节点与非根节点 ==========
        List<T> roots = new ArrayList<>();
        // 仅存储非根节点（parentId != 0 && parentId != null），按 parentId 分组
        Map<Long, List<T>> childrenByParentId = new LinkedHashMap<>();

        for (T node : allNodes) {
            Long parentId = parentIdGetter.apply(node);
            if (parentId == null || parentId == 0L) {
                roots.add(node);
            } else {
                childrenByParentId.computeIfAbsent(parentId, k -> new ArrayList<>()).add(node);
            }
        }

        // ========== 阶段2：为有子节点的节点分配 children ==========
        // 仅当节点拥有子节点时才调用 setter；叶子节点保留实体初始的空 ArrayList，
        // 避免用 Collections.emptyList() 覆盖导致不可变列表异常
        for (T node : allNodes) {
            List<T> children = childrenByParentId.get(idGetter.apply(node));
            if (children != null) {
                childrenSetter.accept(node, children);
            }
        }

        return roots;
    }
}