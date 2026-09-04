package com.istream.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.istream.common.enums.ResultCode;
import com.istream.common.exception.BusinessException;
import com.istream.framework.util.TreeUtils;
import com.istream.system.entity.SysDept;
import com.istream.system.entity.SysUser;
import com.istream.system.mapper.SysDeptMapper;
import com.istream.system.mapper.SysUserMapper;
import com.istream.system.service.SysDeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements SysDeptService {

    private static final long ROOT_PARENT_ID = 0L;
    private static final String ROOT_ANCESTORS = "0";

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

    /**
     * 新增部门：根据父部门计算
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(SysDept entity) {
        entity.setAncestors(buildAncestors(entity.getParentId()));
        return super.save(entity);
    }

    /**
     * 修改部门：若 parentId 变更，级联更新本部门及所有子部门的 ancestors
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(SysDept entity) {
        SysDept old = getById(entity.getId());
        if (old == null) {
            return super.updateById(entity);
        }
        Long oldParentId = old.getParentId();
        Long newParentId = entity.getParentId();
        boolean parentChanged = newParentId != null && !newParentId.equals(oldParentId);
        if (parentChanged) {
            checkCycleReference(entity.getId(), newParentId);
            String newAncestors = buildAncestors(newParentId);
            entity.setAncestors(newAncestors);
            updateChildrenAncestors(entity.getId(), newAncestors);
        }
        return super.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(Serializable id) {
        Long deptId = (Long) id;
        if (hasChildren(deptId)) {
            throw new BusinessException(ResultCode.PARAM_VALID_ERROR.getCode(), "存在子部门，不允许删除");
        }
        if (hasUsers(deptId)) {
            throw new BusinessException(ResultCode.PARAM_VALID_ERROR.getCode(), "部门下存在用户，不允许删除");
        }
        return super.removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeByIds(Collection<?> list) {
        for (Object id : list) {
            Long deptId = (Long) id;
            if (hasChildren(deptId)) {
                throw new BusinessException(ResultCode.PARAM_VALID_ERROR.getCode(), "存在子部门，不允许删除");
            }
            if (hasUsers(deptId)) {
                throw new BusinessException(ResultCode.PARAM_VALID_ERROR.getCode(), "部门下存在用户，不允许删除");
            }
        }
        return super.removeByIds(list);
    }

    /**
     * 校验循环引用：新父部门不能是当前部门自身或其子部门
     *
     * @param deptId       当前部门ID
     * @param newParentId  新的父部门ID
     */
    private void checkCycleReference(Long deptId, Long newParentId) {
        if (deptId.equals(newParentId)) {
            throw new BusinessException(ResultCode.DEPT_CYCLE_REFERENCE.getCode(),
                    ResultCode.DEPT_CYCLE_REFERENCE.getMsg());
        }
        List<SysDept> allDepts = list(new LambdaQueryWrapper<SysDept>()
                .select(SysDept::getId, SysDept::getParentId));
        Map<Long, List<Long>> parentChildMap = allDepts.stream()
                .collect(Collectors.groupingBy(SysDept::getParentId,
                        Collectors.mapping(SysDept::getId, Collectors.toList())));
        checkCycleInMemory(parentChildMap, deptId, newParentId);
    }

    private void checkCycleInMemory(Map<Long, List<Long>> parentChildMap, Long deptId, Long newParentId) {
        List<Long> children = parentChildMap.get(deptId);
        if (children != null) {
            for (Long childId : children) {
                if (childId.equals(newParentId)) {
                    throw new BusinessException(ResultCode.DEPT_CYCLE_REFERENCE.getCode(),
                            ResultCode.DEPT_CYCLE_REFERENCE.getMsg());
                }
                checkCycleInMemory(parentChildMap, childId, newParentId);
            }
        }
    }

    /**
     * 根据父部门ID构建祖级列表
     *
     * @param parentId 父部门ID，0 表示根部门
     * @return 祖级列表字符串，如 "0,1,100"
     */
    private String buildAncestors(Long parentId) {
        if (parentId == null || parentId == ROOT_PARENT_ID) {
            return ROOT_ANCESTORS;
        }
        SysDept parent = getById(parentId);
        if (parent == null) {
            return ROOT_ANCESTORS;
        }
        String parentAncestors = parent.getAncestors();
        if (parentAncestors == null || parentAncestors.isEmpty()) {
            return String.valueOf(parentId);
        }
        return parentAncestors + "," + parentId;
    }

    /**
     * 级联更新子部门的 ancestors
     *
     * @param deptId       当前部门ID
     * @param newAncestors 当前部门新的 ancestors
     */
    private void updateChildrenAncestors(Long deptId, String newAncestors) {
        List<SysDept> children = list(new LambdaQueryWrapper<SysDept>()
                .eq(SysDept::getParentId, deptId));
        for (SysDept child : children) {
            String childNewAncestors = newAncestors + "," + deptId;
            update(new LambdaUpdateWrapper<SysDept>()
                    .eq(SysDept::getId, child.getId())
                    .set(SysDept::getAncestors, childNewAncestors));
            updateChildrenAncestors(child.getId(), childNewAncestors);
        }
    }
}