package com.istream.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.istream.common.annotation.OperLog;
import com.istream.common.constant.Constants;
import com.istream.common.enums.BusinessType;
import com.istream.common.enums.ResultCode;
import com.istream.common.model.R;
import com.istream.system.model.dto.logininfo.SysLoginInfoDTO;
import com.istream.system.model.query.logininfo.SysLoginInfoQuery;
import com.istream.framework.util.ExcelExportUtil;
import com.istream.framework.util.PageUtils;
import com.istream.system.converter.SysLoginInfoConverter;
import com.istream.system.entity.SysLoginInfo;
import com.istream.system.service.SysLoginInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/**
 * 登录日志管理控制器
 *
 * @author istream
 * @since 2026-08-17
 */
@Tag(name = "登录日志管理")
@RestController
@RequestMapping("/monitor/login-info")
@RequiredArgsConstructor
public class SysLoginInfoController {

    private final SysLoginInfoService sysLoginInfoService;
    private final SysLoginInfoConverter sysLoginInfoConverter;

    @Operation(summary = "分页查询登录日志")
    @SaCheckPermission("monitor:login-info:list")
    @GetMapping("/list")
    public R<IPage<SysLoginInfoDTO>> list(SysLoginInfoQuery query) {
        IPage<SysLoginInfo> page = sysLoginInfoService.page(query);
        return R.ok(PageUtils.toDtoPage(page, sysLoginInfoConverter::toDto));
    }

    @Operation(summary = "根据ID查询登录日志")
    @SaCheckPermission("monitor:login-info:query")
    @GetMapping("/{id}")
    public R<SysLoginInfoDTO> getById(@PathVariable Long id) {
        SysLoginInfo loginInfo = sysLoginInfoService.getById(id);
        if (loginInfo == null) {
            return R.fail(ResultCode.DATA_NOT_EXIST);
        }
        return R.ok(sysLoginInfoConverter.toDto(loginInfo));
    }

    @OperLog(title = "登录日志管理", businessType = BusinessType.DELETE)
    @Operation(summary = "删除登录日志")
    @SaCheckPermission("monitor:login-info:delete")
    @DeleteMapping
    public R<Void> delete(@RequestBody List<Long> ids) {
        sysLoginInfoService.removeByIds(ids);
        return R.ok();
    }

    @OperLog(title = "登录日志管理", businessType = BusinessType.DELETE)
    @Operation(summary = "清空登录日志")
    @SaCheckPermission("monitor:login-info:clean")
    @DeleteMapping("/clear")
    public R<Void> clear() {
        sysLoginInfoService.truncate();
        return R.ok();
    }

    @Operation(summary = "导出登录日志")
    @SaCheckPermission("monitor:login-info:export")
    @GetMapping("/export")
    public void export(HttpServletResponse response) throws IOException {
        ExcelExportUtil.exportByPage(response, "登录日志", "登录日志", SysLoginInfo.class,
                (pageNum) -> sysLoginInfoService.pageExport(pageNum, Constants.EXPORT_PAGE_SIZE));
    }
}