package com.istream.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.istream.common.annotation.OperLog;
import com.istream.common.enums.BusinessType;
import com.istream.system.model.query.dict.SysDictDataQuery;
import com.istream.common.model.R;
import com.istream.common.validation.Groups;
import com.istream.framework.util.PageUtils;
import com.istream.system.model.dto.dict.SysDictDataSaveDTO;
import com.istream.system.model.dto.dict.SysDictDataDTO;
import com.istream.system.converter.SysDictDataConverter;
import com.istream.system.entity.SysDictData;
import com.istream.system.service.SysDictDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 字典数据管理控制器
 *
 * @author istream
 * @since 2026-08-17
 */
@Tag(name = "字典数据管理")
@RestController
@RequestMapping("/system/dict-data")
@RequiredArgsConstructor
public class SysDictDataController {

    private final SysDictDataService sysDictDataService;
    private final SysDictDataConverter sysDictDataConverter;

    @Operation(summary = "分页查询字典数据")
    @SaCheckPermission("system:dict:list")
    @GetMapping("/list")
    public R<IPage<SysDictDataDTO>> list(SysDictDataQuery query) {
        IPage<SysDictData> page = sysDictDataService.page(query);
        return R.ok(PageUtils.toDtoPage(page, sysDictDataConverter::toDto));
    }

    /**
     * 根据字典类型查询字典数据（公开接口，无需登录）
     * <p>用于前端下拉框、单选框等组件动态获取字典选项</p>
     */
    @Operation(summary = "根据字典类型查询字典数据")
    @GetMapping("/by-type/{dictType}")
    public R<List<SysDictDataDTO>> getByType(@PathVariable String dictType) {
        return R.ok(sysDictDataService.listByType(dictType).stream()
                .map(sysDictDataConverter::toDto)
                .toList());
    }

    @Operation(summary = "获取所有字典数据（按类型分组）")
    @SaCheckPermission("system:dict:list")
    @GetMapping("/map")
    public R<Map<String, List<SysDictDataDTO>>> dictMap() {
        Map<String, List<SysDictData>> entityMap = sysDictDataService.getDictMap();
        Map<String, List<SysDictDataDTO>> dtoMap = entityMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().stream().map(sysDictDataConverter::toDto).toList()
                ));
        return R.ok(dtoMap);
    }

    @Operation(summary = "根据ID查询字典数据")
    @SaCheckPermission("system:dict:query")
    @GetMapping("/{id}")
    public R<SysDictDataDTO> getById(@PathVariable Long id) {
        return R.ok(sysDictDataConverter.toDto(sysDictDataService.getById(id)));
    }

    @OperLog(title = "字典数据管理", businessType = BusinessType.INSERT)
    @Operation(summary = "新增字典数据")
    @SaCheckPermission("system:dict:add")
    @PostMapping
    public R<Void> add(@Validated(Groups.Create.class) @RequestBody SysDictDataSaveDTO dto) {
        SysDictData dictData = sysDictDataConverter.toEntity(dto);
        sysDictDataService.save(dictData);
        return R.ok();
    }

    @OperLog(title = "字典数据管理", businessType = BusinessType.UPDATE)
    @Operation(summary = "修改字典数据")
    @SaCheckPermission("system:dict:edit")
    @PutMapping
    public R<Void> update(@Validated(Groups.Update.class) @RequestBody SysDictDataSaveDTO dto) {
        SysDictData dictData = new SysDictData();
        dictData.setId(dto.getId());
        sysDictDataConverter.updateEntity(dictData, dto);
        sysDictDataService.updateById(dictData);
        return R.ok();
    }

    @OperLog(title = "字典数据管理", businessType = BusinessType.DELETE)
    @Operation(summary = "删除字典数据")
    @SaCheckPermission("system:dict:delete")
    @DeleteMapping
    public R<Void> delete(@RequestBody List<Long> ids) {
        sysDictDataService.removeByIds(ids);
        return R.ok();
    }
}