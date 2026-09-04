package com.istream.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.istream.common.constant.Constants;
import com.istream.common.enums.ResultCode;
import com.istream.common.exception.BusinessException;
import com.istream.common.model.query.SysRoleQuery;
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
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.istream.common.constant.Constants.PERM_CACHE_PREFIX;
import static com.istream.common.constant.Constants.ROLE_CACHE_PREFIX;

@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    private final SysRoleMenuMapper sysRoleMenuMapper;
    private final SysRoleDeptMapper sysRoleDeptMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysMenuMapper sysMenuMapper;
    private final RedissonClient redissonClient;

    @Override
    public IPage<SysRole> page(SysRoleQuery query) {
        return baseMapper.selectRolePage(new Page<>(query.getPageNum(), query.getPageSize()), query);
    }

    @Override
    public boolean changeStatus(Long roleId, Integer status) {
        SysRole role = new SysRole();
        role.setId(roleId);
        role.setStatus(status);
        return updateById(role);
    }

    @Override
    public List<Long> getMenuIdsByRoleId(Long roleId) {
        List<SysRoleMenu> list = sysRoleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>()
                        .eq(SysRoleMenu::getRoleId, roleId));
        return list.stream().map(SysRoleMenu::getMenuId).collect(Collectors.toList());
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
        clearCacheBatch(userIds);
    }

    private Set<Long> expandMenuIds(List<Long> menuIds) {
        Set<Long> result = new HashSet<>(menuIds);
        Set<Long> currentIds = new HashSet<>(menuIds);
        Set<Long> allCollected = new HashSet<>(menuIds);

        while (!currentIds.isEmpty()) {
            List<SysMenu> children = sysMenuMapper.selectList(
                    new LambdaQueryWrapper<SysMenu>()
                            .eq(SysMenu::getStatus, 0)
                            .in(SysMenu::getParentId, currentIds));
            currentIds.clear();
            for (SysMenu child : children) {
                if (allCollected.add(child.getId())) {
                    currentIds.add(child.getId());
                    result.add(child.getId());
                }
            }
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
            clearCacheBatch(userIds);
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
                throw new BusinessException(ResultCode.SUPER_ADMIN_PROTECT.getCode(),
                        ResultCode.SUPER_ADMIN_PROTECT.getMsg());
            }
        }
        List<Long> allUserIds = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>()
                        .in(SysUserRole::getRoleId, list))
                .stream().map(SysUserRole::getUserId).collect(Collectors.toList());
        sysRoleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>()
                .in(SysRoleMenu::getRoleId, list));
        sysRoleDeptMapper.delete(new LambdaQueryWrapper<SysRoleDept>()
                .in(SysRoleDept::getRoleId, list));
        sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                .in(SysUserRole::getRoleId, list));
        boolean result = super.removeByIds(list);
        if (result) {
            clearCacheBatch(allUserIds);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(SysRole entity) {
        boolean result = super.updateById(entity);
        if (result) {
            List<Long> userIds = getAffectedUserIds(entity.getId());
            clearCacheBatch(userIds);
        }
        return result;
    }

    private List<Long> getAffectedUserIds(Long roleId) {
        return sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getRoleId, roleId))
                .stream().map(SysUserRole::getUserId).collect(Collectors.toList());
    }

    private void clearCacheBatch(Collection<Long> userIds) {
        for (Long userId : userIds) {
            redissonClient.getBucket(PERM_CACHE_PREFIX + userId).delete();
            redissonClient.getBucket(ROLE_CACHE_PREFIX + userId).delete();
        }
    }

    /**
     * 校验是否为超级管理员角色，防止误删除
     */
    private void checkSuperAdminRole(Long roleId) {
        SysRole role = getById(roleId);
        if (role != null && Constants.SUPER_ADMIN_ROLE.equals(role.getRoleKey())) {
            throw new BusinessException(ResultCode.SUPER_ADMIN_PROTECT.getCode(),
                    ResultCode.SUPER_ADMIN_PROTECT.getMsg());
        }
    }

    @Override
    public boolean hasUsers(Long roleId) {
        return sysUserRoleMapper.selectCount(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getRoleId, roleId)) > 0;
    }

    @Override
    public boolean hasUsersAny(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return false;
        }
        return sysUserRoleMapper.selectCount(new LambdaQueryWrapper<SysUserRole>()
                .in(SysUserRole::getRoleId, roleIds)) > 0;
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
        for (Long userId : affected) {
            redissonClient.getBucket(PERM_CACHE_PREFIX + userId).delete();
            redissonClient.getBucket(ROLE_CACHE_PREFIX + userId).delete();
        }
    }

    @Override
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
        clearCacheBatch(userIds);
    }
}