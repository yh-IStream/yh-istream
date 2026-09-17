package com.istream.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.istream.common.constant.Constants;
import com.istream.common.enums.ResultCode;
import com.istream.common.enums.StatusEnum;
import com.istream.common.exception.BusinessException;
import com.istream.system.helper.UserCacheHelper;
import com.istream.system.model.query.SysRoleQuery;
import com.istream.system.entity.SysRole;
import com.istream.system.entity.SysRoleMenu;
import com.istream.system.entity.SysRoleDept;
import com.istream.system.entity.SysMenu;
import com.istream.system.entity.SysUserRole;
import com.istream.system.mapper.SysRoleMapper;
import com.istream.system.mapper.SysRoleMenuMapper;
import com.istream.system.mapper.SysRoleDeptMapper;
import com.istream.system.mapper.SysMenuMapper;
import com.istream.system.mapper.SysUserRoleMapper;
import com.istream.system.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 角色管理服务实现
 *
 * @author istream
 * @since 2026-08-17
 */
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    private final SysRoleMenuMapper sysRoleMenuMapper;
    private final SysRoleDeptMapper sysRoleDeptMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysMenuMapper sysMenuMapper;
    private final UserCacheHelper userCacheHelper;

    @Override
    @Transactional(readOnly = true)
    public IPage<SysRole> page(SysRoleQuery query) {
        return baseMapper.selectRolePage(new Page<>(query.getPageNum(), query.getPageSize()), query);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SysRole> listAllEnabled() {
        return list(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getStatus, StatusEnum.ENABLED.getCode())
                .eq(SysRole::getDelFlag, Constants.DEL_FLAG_NORMAL)
                .orderByAsc(SysRole::getRoleSort));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByRoleKey(String roleKey, Long excludeId) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleKey, roleKey);
        if (excludeId != null) {
            wrapper.ne(SysRole::getId, excludeId);
        }
        return count(wrapper) > 0;
    }

    @Override
    public boolean changeStatus(Long roleId, Integer status) {
        checkSuperAdminRole(roleId);
        SysRole role = new SysRole();
        role.setId(roleId);
        role.setStatus(status);
        boolean result = updateById(role);
        if (result) {
            List<Long> userIds = getAffectedUserIds(roleId);
            userCacheHelper.evictAllBatch(userIds);
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getMenuIdsByRoleId(Long roleId) {
        List<SysRoleMenu> list = sysRoleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>()
                        .eq(SysRoleMenu::getRoleId, roleId));
        return list.stream().map(SysRoleMenu::getMenuId).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveRoleMenu(Long roleId, List<Long> menuIds) {
        sysRoleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>()
                .eq(SysRoleMenu::getRoleId, roleId));

        if (menuIds != null && !menuIds.isEmpty()) {
            Set<Long> expandedIds = expandMenuIds(menuIds);
            List<SysRoleMenu> roleMenus = expandedIds.stream()
                    .map(menuId -> {
                        SysRoleMenu rm = new SysRoleMenu();
                        rm.setRoleId(roleId);
                        rm.setMenuId(menuId);
                        return rm;
                    })
                    .toList();
            if (!roleMenus.isEmpty()) {
                sysRoleMenuMapper.insertBatch(roleMenus);
            }
        }
        List<Long> userIds = getAffectedUserIds(roleId);
        userCacheHelper.evictAllBatch(userIds);
    }

    private Set<Long> expandMenuIds(List<Long> menuIds) {
        Set<Long> result = new HashSet<>(menuIds);

        List<SysMenu> allMenus = sysMenuMapper.selectList(
                new LambdaQueryWrapper<SysMenu>()
                        .eq(SysMenu::getStatus, StatusEnum.ENABLED.getCode())
                        .select(SysMenu::getId, SysMenu::getParentId));

        Map<Long, List<Long>> parentChildMap = allMenus.stream()
                .collect(Collectors.groupingBy(SysMenu::getParentId,
                        Collectors.mapping(SysMenu::getId, Collectors.toList())));

        Set<Long> currentIds = new HashSet<>(menuIds);
        while (!currentIds.isEmpty()) {
            Set<Long> next = new HashSet<>();
            for (Long id : currentIds) {
                List<Long> children = parentChildMap.get(id);
                if (children != null) {
                    for (Long childId : children) {
                        if (result.add(childId)) {
                            next.add(childId);
                        }
                    }
                }
            }
            currentIds = next;
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(Serializable id) {
        checkSuperAdminRole((Long) id);
        List<Long> userIds = getAffectedUserIds((Long) id);
        sysRoleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>()
                .eq(SysRoleMenu::getRoleId, (Long) id));
        sysRoleDeptMapper.delete(new LambdaQueryWrapper<SysRoleDept>()
                .eq(SysRoleDept::getRoleId, (Long) id));
        sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getRoleId, (Long) id));
        boolean result = super.removeById(id);
        if (result) {
            userCacheHelper.evictAllBatch(userIds);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeByIds(Collection<?> list) {
        @SuppressWarnings("unchecked")
        List<SysRole> roles = listByIds((Collection<? extends Serializable>) list);
        for (SysRole role : roles) {
            if (Constants.SUPER_ADMIN_ROLE.equals(role.getRoleKey())) {
                throw new BusinessException(ResultCode.SUPER_ADMIN_PROTECT);
            }
        }
        List<Long> allUserIds = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>()
                        .in(SysUserRole::getRoleId, list))
                .stream().map(SysUserRole::getUserId).toList();
        sysRoleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>()
                .in(SysRoleMenu::getRoleId, list));
        sysRoleDeptMapper.delete(new LambdaQueryWrapper<SysRoleDept>()
                .in(SysRoleDept::getRoleId, list));
        sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                .in(SysUserRole::getRoleId, list));
        boolean result = super.removeByIds(list);
        if (result) {
            userCacheHelper.evictAllBatch(allUserIds);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(SysRole entity) {
        boolean result = super.updateById(entity);
        if (result) {
            List<Long> userIds = getAffectedUserIds(entity.getId());
            userCacheHelper.evictAllBatch(userIds);
        }
        return result;
    }

    private List<Long> getAffectedUserIds(Long roleId) {
        return sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getRoleId, roleId))
                .stream().map(SysUserRole::getUserId).toList();
    }

    /**
     * 校验是否为超级管理员角色，防止误删除
     */
    private void checkSuperAdminRole(Long roleId) {
        SysRole role = getById(roleId);
        if (role != null && Constants.SUPER_ADMIN_ROLE.equals(role.getRoleKey())) {
            throw new BusinessException(ResultCode.SUPER_ADMIN_PROTECT);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasUsers(Long roleId) {
        return sysUserRoleMapper.selectCount(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getRoleId, roleId)) > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasUsersAny(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return false;
        }
        return sysUserRoleMapper.selectCount(new LambdaQueryWrapper<SysUserRole>()
                .in(SysUserRole::getRoleId, roleIds)) > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public IPage<SysRole> pageExport(long pageNum, long pageSize) {
        Page<SysRole> page = new Page<>(pageNum, pageSize);
        return baseMapper.selectPage(page, new LambdaQueryWrapper<SysRole>()
                .orderByDesc(SysRole::getCreateTime));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignUsersToRole(Long roleId, List<Long> userIds) {
        List<SysUserRole> existing = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getRoleId, roleId));
        Set<Long> existingUserIds = existing.stream()
                .map(SysUserRole::getUserId).collect(Collectors.toSet());

        Set<Long> toAdd = new HashSet<>(userIds != null ? userIds : List.of());
        Set<Long> toRemove = new HashSet<>(existingUserIds);
        toRemove.removeAll(toAdd);
        toAdd.removeAll(existingUserIds);

        if (!toRemove.isEmpty()) {
            sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                    .eq(SysUserRole::getRoleId, roleId)
                    .in(SysUserRole::getUserId, toRemove));
        }
        if (!toAdd.isEmpty()) {
            List<SysUserRole> addList = toAdd.stream().map(userId -> {
                SysUserRole ur = new SysUserRole();
                ur.setRoleId(roleId);
                ur.setUserId(userId);
                return ur;
            }).toList();
            sysUserRoleMapper.insertBatch(addList);
        }

        Set<Long> affected = new HashSet<>();
        affected.addAll(toAdd);
        affected.addAll(toRemove);
        userCacheHelper.evictAllBatch(affected);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getDeptIdsByRoleId(Long roleId) {
        return sysRoleDeptMapper.selectDeptIdsByRoleId(roleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveRoleDept(Long roleId, List<Long> deptIds) {
        sysRoleDeptMapper.delete(new LambdaQueryWrapper<SysRoleDept>()
                .eq(SysRoleDept::getRoleId, roleId));
        if (deptIds != null && !deptIds.isEmpty()) {
            List<SysRoleDept> roleDepts = deptIds.stream().map(deptId -> {
                SysRoleDept rd = new SysRoleDept();
                rd.setRoleId(roleId);
                rd.setDeptId(deptId);
                return rd;
            }).toList();
            if (!roleDepts.isEmpty()) {
                sysRoleDeptMapper.insertBatch(roleDepts);
            }
        }
        List<Long> userIds = getAffectedUserIds(roleId);
        userCacheHelper.evictAllBatch(userIds);
    }
}