package com.istream.message.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息 DTO
 *
 * @author istream
 * @since 2026-09-21
 */
@Data
public class SysMessageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String title;

    private String content;

    private String eventType;

    private String entityType;

    private Long entityId;

    private Long senderId;

    private String senderName;

    private LocalDateTime createTime;

    private Integer isRead;
}