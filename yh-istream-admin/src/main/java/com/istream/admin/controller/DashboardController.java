package com.istream.admin.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.istream.common.model.R;
import com.istream.system.service.SysUserService;
import com.istream.system.service.SysRoleService;
import com.istream.system.service.SysOperLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "仪表盘")
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final SysUserService sysUserService;
    private final SysRoleService sysRoleService;
    private final SysOperLogService sysOperLogService;

    @Operation(summary = "获取仪表盘统计数据")
    @GetMapping("/stats")
    public R<Map<String, Object>> stats() {
        Map<String, Object> data = new HashMap<>();
        data.put("userCount", sysUserService.count());
        data.put("roleCount", sysRoleService.count());
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        data.put("todayOperCount", sysOperLogService.countTodayOps(todayStart));
        data.put("onlineCount", StpUtil.searchTokenSessionId("", 0, -1, false).size());
        return R.ok(data);
    }
}