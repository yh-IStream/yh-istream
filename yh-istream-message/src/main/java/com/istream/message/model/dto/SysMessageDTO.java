package com.istream.message.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息 DTO
 *
 * @author istream
 * @since 2026-09-21
 */
@Data
public class SysMessageDTO {

    private Long id;

    /** 消息类型（0=定向 1=租户广播 2=全量广播） */
    private Integer messageType;

    /** 消息标题 */
    private String title;

    /** 消息内容 */
    private String content;

    /** 事件类型（DATA_CHANGE / NOTIFICATION / SYSTEM） */
    private String eventType;

    /** 关联实体类型 */
    private String entityType;

    /** 关联实体ID */
    private Long entityId;

    /** 发送者ID */
    private Long senderId;

    /** 发送者名称 */
    private String senderName;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 是否已读（0=未读 1=已读） */
    private Integer readFlag;
}