package com.istream.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.istream.message.entity.SysMessageUser;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户消息关联表 Mapper
 *
 * @author istream
 * @since 2026-09-21
 */
@Mapper
public interface SysMessageUserMapper extends BaseMapper<SysMessageUser> {

    /**
     * 幂等插入已读记录
     *
     * <p>用于单条广播消息标记已读（markBroadcastAsRead）：
     * 并发场景下多个请求同时调用，只有第一个 INSERT 成功，
     * 后续请求因 uk_message_user 唯一约束被跳过。</p>
     */
    @Insert("INSERT IGNORE INTO sys_message_user (message_id, user_id, read_flag, read_time) " +
            "VALUES (#{messageId}, #{userId}, #{readFlag}, #{readTime})")
    int insertIgnore(SysMessageUser entity);

    /**
     * 为用户所有未追踪的广播消息批量创建已读记录
     *
     * <p>使用 INSERT IGNORE ... SELECT 模式，在数据库端完成"查找未追踪广播 → 插入已读记录"，
     * 以彻底消除 OOM 风险和 N 次 DB 往返。</p>
     *
     * <p>租户隔离：租户广播（message_type=1）仅匹配同租户，全量广播（message_type=2）匹配所有。</p>
     *
     * @param userId   当前用户ID
     * @param tenantId 当前租户ID
     * @return 插入行数
     */
    @Insert("INSERT IGNORE INTO sys_message_user (message_id, user_id, read_flag, read_time) " +
            "SELECT m.id, #{userId}, 1, NOW() " +
            "FROM sys_message m " +
            "WHERE m.del_flag = 0 " +
            "AND (m.message_type = 2 OR (m.message_type = 1 AND m.tenant_id = #{tenantId})) " +
            "AND NOT EXISTS (" +
            "  SELECT 1 FROM sys_message_user mu " +
            "  WHERE mu.message_id = m.id AND mu.user_id = #{userId}" +
            ")")
    int insertIgnoreBroadcastRead(@Param("userId") Long userId,
                                  @Param("tenantId") Long tenantId);
}