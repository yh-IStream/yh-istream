package com.istream.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.istream.common.model.R;
import com.istream.system.entity.SysDictData;
import com.istream.system.service.SysDictDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "字典数据管理")
@RestController
@RequestMapping("/system/dict-data")
@RequiredArgsConstructor
public class SysDictDataController {

    private final SysDictDataService sysDictDataService;

    @Operation(summary = "分页查询字典数据")
    @SaCheckPermission("system:dict:list")
    @GetMapping("/list")
    public R<IPage<SysDictData>> list(@RequestParam(defaultValue = "1") Long pageNum,
                                      @RequestParam(defaultValue = "10") Long pageSize,
                                      @RequestParam(required = false) String dictType) {
        Page<SysDictData> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysDictData> wrapper = new LambdaQueryWrapper<SysDictData>()
                .eq(dictType != null && !dictType.isEmpty(), SysDictData::getDictType, dictType)
                .orderByAsc(SysDictData::getOrderNum);
        sysDictDataService.page(page, wrapper);
        return R.ok(page);
    }

    @Operation(summary = "根据字典类型查询字典数据")
    @GetMapping("/by-type/{dictType}")
    public R<List<SysDictData>> getByType(@PathVariable String dictType) {
        List<SysDictData> list = sysDictDataService.list(new LambdaQueryWrapper<SysDictData>()
                .eq(SysDictData::getDictType, dictType)
                .eq(SysDictData::getStatus, 0)
                .orderByAsc(SysDictData::getOrderNum));
        return R.ok(list);
    }

    @Operation(summary = "获取所有字典数据（按类型分组）")
    @SaCheckPermission("system:dict:list")
    @GetMapping("/map")
    public R<Map<String, List<SysDictData>>> dictMap() {
        return R.ok(sysDictDataService.getDictMap());
    }

    @Operation(summary = "根据ID查询字典数据")
    @SaCheckPermission("system:dict:query")
    @GetMapping("/{id}")
    public R<SysDictData> getById(@PathVariable Long id) {
        return R.ok(sysDictDataService.getById(id));
    }

    @Operation(summary = "新增字典数据")
    @SaCheckPermission("system:dict:add")
    @PostMapping
    public R<Void> add(@RequestBody SysDictData dictData) {
        dictData.setId(null);
        sysDictDataService.save(dictData);
        return R.ok();
    }

    @Operation(summary = "修改字典数据")
    @SaCheckPermission("system:dict:edit")
    @PutMapping
    public R<Void> update(@RequestBody SysDictData dictData) {
        sysDictDataService.updateById(dictData);
        return R.ok();
    }

    @Operation(summary = "删除字典数据")
    @SaCheckPermission("system:dict:delete")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        sysDictDataService.removeById(id);
        return R.ok();
    }

    @Operation(summary = "批量删除字典数据")
    @SaCheckPermission("system:dict:delete")
    @DeleteMapping("/batch")
    public R<Void> deleteBatch(@RequestBody List<Long> ids) {
        sysDictDataService.removeByIds(ids);
        return R.ok();
    }
}