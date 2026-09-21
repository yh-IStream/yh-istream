package com.istream.message.model.query;

import com.istream.common.model.query.BaseQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 消息查询参数
 *
 * @author istream
 * @since 2026-09-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysMessageQuery extends BaseQuery {

    /** 事件类型过滤 */
    private String eventType;

    /** 是否只查未读 */
    private Boolean unreadOnly;
}