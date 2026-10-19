package com.istream.message.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.istream.common.enums.MessageType;
import com.istream.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 消息主表实体
 *
 * <p>每条消息对应一条记录，通过 sys_message_user 关联接收用户。</p>
 * <p>entityType + entityId 用于消息聚合：同一实体 10 秒内多次变更聚合为 1 条通知。</p>
 * <p>messageType 区分投递范围：USER(定向) / TENANT(租户广播) / GLOBAL(全量广播)。</p>
 *
 * @author istream
 * @since 2026-09-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_message")
public class SysMessage extends BaseEntity {

    /** 消息类型（0=定向 1=租户广播 2=全量广播） */
    private Integer messageType;

    /** 消息标题 */
    private String title;

    /** 消息内容 */
    private String content;

    /** 事件类型（DATA_CHANGE / NOTIFICATION / SYSTEM） */
    private String eventType;

    /** 关联实体类型（如 SysOrder，用于聚合） */
    private String entityType;

    /** 关联实体ID（用于聚合） */
    private Long entityId;

    /** 发送者ID */
    private Long senderId;

    /** 发送者名称 */
    private String senderName;

    /**
     * 判断是否为广播类消息（租户广播或全量广播）
     *
     * @return true=广播消息
     */
    public boolean isBroadcast() {
        return messageType != null && messageType != MessageType.USER.getCode();
    }
}