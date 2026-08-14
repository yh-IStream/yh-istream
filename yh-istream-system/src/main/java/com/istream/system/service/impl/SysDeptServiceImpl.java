package com.istream.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.istream.system.entity.SysDept;
import com.istream.system.mapper.SysDeptMapper;
import com.istream.system.service.SysDeptService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements SysDeptService {

    @Override
    public List<SysDept> listDeptTree() {
        List<SysDept> allDepts = list(new LambdaQueryWrapper<SysDept>()
                .eq(SysDept::getStatus, 0)
                .orderByAsc(SysDept::getOrderNum));

        Map<Long, List<SysDept>> parentMap = allDepts.stream()
                .collect(Collectors.groupingBy(SysDept::getParentId));

        List<SysDept> roots = new ArrayList<>();
        for (SysDept dept : allDepts) {
            if (dept.getParentId() == null || dept.getParentId() == 0L) {
                roots.add(dept);
            }
            dept.setChildren(parentMap.getOrDefault(dept.getId(), new ArrayList<>()));
        }
        return roots;
    }

    @Override
    public boolean hasChildren(Long deptId) {
        return count(new LambdaQueryWrapper<SysDept>()
                .eq(SysDept::getParentId, deptId)) > 0;
    }
}