package com.istream.common.validation;

/**
 * JSR 303 校验组标记接口
 *
 * <p>用于同一个 DTO 在不同操作场景下应用不同的校验规则。</p>
 * <p>Controller 中使用 {@code @Validated(Groups.Create.class)} 或 {@code @Validated(Groups.Update.class)} 激活对应分组。</p>
 *
 * @author istream
 * @since 2026-09-17
 */
public final class Groups {

    private Groups() {
    }

    /** 新增操作校验组 */
    public interface Create {
    }

    /** 修改操作校验组 */
    public interface Update {
    }
}