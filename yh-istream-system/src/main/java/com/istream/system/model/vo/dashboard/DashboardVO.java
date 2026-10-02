package com.istream.system.model.vo.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 仪表盘统计数据响应 VO
 *
 * @author istream
 * @since 2026-08-17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardVO {

    private long userCount;

    private long roleCount;

    private long todayOperCount;

    private int onlineCount;
}