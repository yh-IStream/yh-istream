package com.istream.message.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户消息关联表实体
 *
 * <p>记录用户与消息的关联关系，支持已读/未读状态追踪。</p>
 * <p>定向消息（message_type=0）在投递时创建关联记录；广播消息在用户首次标记已读时按需创建。</p>
 * <p>主键采用 AUTO_INCREMENT 非雪花 ID：关联表主键无业务含义，自增主键天然递增无冲突，
 * 而 baseId + rownum 的雪花 ID 有溢出冲突风险。</p>
 *
 * @author istream
 * @since 2026-09-21
 */
@Data
@EqualsAndHashCode(of = "id")
@TableName("sys_message_user")
public class SysMessageUser {

    /** 主键ID（自增） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 消息ID */
    private Long messageId;

    /** 接收用户ID */
    private Long userId;

    /** 是否已读（0=未读 1=已读） */
    private Integer readFlag;

    /** 阅读时间 */
    private LocalDateTime readTime;
}