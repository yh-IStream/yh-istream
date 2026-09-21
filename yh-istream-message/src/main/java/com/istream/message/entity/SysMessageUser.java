package com.istream.message.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户消息关联表实体
 *
 * <p>记录用户与消息的关联关系，支持已读/未读状态追踪。</p>
 *
 * @author istream
 * @since 2026-09-21
 */
@Data
@TableName("sys_message_user")
public class SysMessageUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 消息ID */
    private Long messageId;

    /** 接收用户ID */
    private Long userId;

    /** 是否已读（0=未读 1=已读） */
    private Integer isRead;

    /** 阅读时间 */
    private LocalDateTime readTime;
}