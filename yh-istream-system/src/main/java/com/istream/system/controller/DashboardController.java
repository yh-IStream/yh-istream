package com.istream.system.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.istream.common.model.R;
import com.istream.system.model.vo.dashboard.DashboardVO;
import com.istream.system.service.SysUserService;
import com.istream.system.service.SysRoleService;
import com.istream.system.service.SysOperLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * 仪表盘控制器
 *
 * @author istream
 * @since 2026-08-17
 */
@Tag(name = "仪表盘")
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final SysUserService sysUserService;
    private final SysRoleService sysRoleService;
    private final SysOperLogService sysOperLogService;

    @SaCheckLogin
    @Operation(summary = "获取仪表盘统计数据")
    @GetMapping("/stats")
    public R<DashboardVO> stats() {
        LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
        return R.ok(DashboardVO.builder()
                .userCount(sysUserService.count())
                .roleCount(sysRoleService.count())
                .todayOperCount(sysOperLogService.countTodayOps(todayStart))
                .onlineCount(StpUtil.searchTokenSessionId("", 0, -1, false).size())
                .build());
    }
}