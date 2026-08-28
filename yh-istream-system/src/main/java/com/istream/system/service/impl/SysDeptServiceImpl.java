package com.istream.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.istream.framework.util.TreeUtils;
import com.istream.system.entity.SysDept;
import com.istream.system.entity.SysUser;
import com.istream.system.mapper.SysDeptMapper;
import com.istream.system.mapper.SysUserMapper;
import com.istream.system.service.SysDeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements SysDeptService {

    private final SysUserMapper sysUserMapper;

    @Override
    public List<SysDept> listDeptTree() {
        List<SysDept> allDepts = list(new LambdaQueryWrapper<SysDept>()
                .eq(SysDept::getStatus, 0)
                .orderByAsc(SysDept::getOrderNum));
        return TreeUtils.build(allDepts, SysDept::getId, SysDept::getParentId,
                SysDept::setChildren);
    }

    @Override
    public boolean hasChildren(Long deptId) {
        return count(new LambdaQueryWrapper<SysDept>()
                .eq(SysDept::getParentId, deptId)) > 0;
    }

    @Override
    public boolean hasUsers(Long deptId) {
        return sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getDeptId, deptId)) > 0;
    }
}