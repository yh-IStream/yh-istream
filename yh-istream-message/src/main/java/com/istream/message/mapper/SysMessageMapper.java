package com.istream.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.istream.message.entity.SysMessage;
import com.istream.message.model.dto.SysMessageDTO;
import com.istream.message.model.query.SysMessageQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 消息主表 Mapper
 *
 * @author istream
 * @since 2026-09-21
 */
@Mapper
public interface SysMessageMapper extends BaseMapper<SysMessage> {

    /**
     * 分页查询用户消息
     *
     * <p>查询逻辑：</p>
     * <ul>
     *   <li>定向消息（message_type=0）：仅当 sys_message_user 存在且 user_id 匹配时可见</li>
     *   <li>租户广播（message_type=1）：同租户所有用户可见</li>
     *   <li>全量广播（message_type=2）：所有用户可见</li>
     * </ul>
     *
     * @param page     分页参数
     * @param userId   当前用户ID
     * @param tenantId 当前租户ID
     * @param query    查询条件
     * @return 分页消息 DTO
     */
    IPage<SysMessageDTO> selectUserMessagePage(IPage<SysMessageDTO> page,
                                               @Param("userId") Long userId,
                                               @Param("tenantId") Long tenantId,
                                               @Param("query") SysMessageQuery query);

    /**
     * 统计用户未读消息数
     *
     * <p>可见性逻辑与 {@link #selectUserMessagePage} 一致。</p>
     *
     * @param userId   当前用户ID
     * @param tenantId 当前租户ID
     * @return 未读消息数
     */
    @Select("SELECT COUNT(*) FROM sys_message m " +
            "LEFT JOIN sys_message_user mu ON m.id = mu.message_id AND mu.user_id = #{userId} " +
            "WHERE m.del_flag = 0 " +
            "AND (m.message_type = 2 OR (m.message_type = 1 AND m.tenant_id = #{tenantId}) OR mu.id IS NOT NULL) " +
            "AND COALESCE(mu.read_flag, 0) = 0")
    long countUserUnread(@Param("userId") Long userId, @Param("tenantId") Long tenantId);
}