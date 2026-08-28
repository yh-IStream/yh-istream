package com.istream.system.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.istream.common.annotation.DataScope;
import com.istream.common.constant.Constants;
import com.istream.common.enums.ResultCode;
import com.istream.common.exception.BusinessException;
import com.istream.common.model.dto.SysUserQuery;
import com.istream.system.entity.SysRole;
import com.istream.system.entity.SysUser;
import com.istream.system.entity.SysUserRole;
import com.istream.system.mapper.SysRoleMapper;
import com.istream.system.mapper.SysUserMapper;
import com.istream.system.mapper.SysUserRoleMapper;
import com.istream.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleMapper sysRoleMapper;
    private final RedissonClient redissonClient;

    private static final String PERM_CACHE_PREFIX = "perm:cache:";
    private static final String ROLE_CACHE_PREFIX = "role:cache:";

    @Override
    public SysUser getByUsername(String username) {
        return getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
    }

    @Override
    @DataScope(deptAlias = "d", userAlias = "u")
    public IPage<SysUser> page(SysUserQuery query) {
        return baseMapper.selectUserPage(new Page<>(query.getPageNum(), query.getPageSize()), query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createUser(SysUser user) {
        if (getByUsername(user.getUsername()) != null) {
            throw new BusinessException(ResultCode.DATA_DUPLICATE.getCode(), "用户名已存在");
        }
        user.setId(null);
        user.setPassword(BCrypt.hashpw(user.getPassword()));
        save(user);
        if (user.getRoleIds() != null && !user.getRoleIds().isEmpty()) {
            assignRoles(user.getId(), user.getRoleIds());
        } else {
            assignDefaultRole(user.getId());
        }
    }

    private void assignDefaultRole(Long userId) {
        SysRole defaultRole = sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleKey, Constants.DEFAULT_ROLE_KEY)
                .eq(SysRole::getStatus, 0));
        if (defaultRole != null) {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(userId);
            ur.setRoleId(defaultRole.getId());
            sysUserRoleMapper.insert(ur);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(SysUser user) {
        if (user.getId() == null) {
            throw new BusinessException(ResultCode.PARAM_VALID_ERROR.getCode(), "用户ID不能为空");
        }
        SysUser exist = getByUsername(user.getUsername());
        if (exist != null && !exist.getId().equals(user.getId())) {
            throw new BusinessException(ResultCode.DATA_DUPLICATE.getCode(), "用户名已存在");
        }
        update(new LambdaUpdateWrapper<SysUser>()
                .set(SysUser::getDeptId, user.getDeptId())
                .set(SysUser::getUsername, user.getUsername())
                .set(SysUser::getNickname, user.getNickname())
                .set(SysUser::getEmail, user.getEmail())
                .set(SysUser::getPhone, user.getPhone())
                .set(SysUser::getGender, user.getGender())
                .set(SysUser::getAvatar, user.getAvatar())
                .set(SysUser::getStatus, user.getStatus())
                .set(SysUser::getRemark, user.getRemark())
                .eq(SysUser::getId, user.getId()));
        clearUserCache(user.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignUserRoles(Long userId, List<Long> roleIds) {
        sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId));
        if (roleIds != null && !roleIds.isEmpty()) {
            List<SysRole> validRoles = sysRoleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                    .in(SysRole::getId, roleIds)
                    .eq(SysRole::getStatus, 0)
                    .eq(SysRole::getDelFlag, 0));
            if (validRoles.size() != new java.util.HashSet<>(roleIds).size()) {
                throw new BusinessException(ResultCode.PARAM_VALID_ERROR.getCode(), "存在无效的角色ID");
            }
            assignRoles(userId, roleIds);
        } else {
            assignDefaultRole(userId);
        }
        clearUserCache(userId);
    }

    private void clearUserCache(Long userId) {
        redissonClient.getBucket(PERM_CACHE_PREFIX + userId).delete();
        redissonClient.getBucket(ROLE_CACHE_PREFIX + userId).delete();
    }

    private void assignRoles(Long userId, List<Long> roleIds) {
        List<SysUserRole> userRoles = roleIds.stream()
                .map(roleId -> {
                    SysUserRole ur = new SysUserRole();
                    ur.setUserId(userId);
                    ur.setRoleId(roleId);
                    return ur;
                })
                .toList();
        for (SysUserRole ur : userRoles) {
            sysUserRoleMapper.insert(ur);
        }
    }

    @Override
    public boolean resetPassword(Long userId, String newPassword) {
        SysUser user = new SysUser();
        user.setId(userId);
        user.setPassword(BCrypt.hashpw(newPassword));
        user.setPwdResetTime(LocalDateTime.now());
        return updateById(user);
    }

    @Override
    public boolean changeStatus(Long userId, Integer status) {
        SysUser user = new SysUser();
        user.setId(userId);
        user.setStatus(status);
        boolean result = updateById(user);
        if (result) {
            clearUserCache(userId);
        }
        return result;
    }

    @Override
    public List<SysUser> getUsersByRoleId(Long roleId) {
        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getRoleId, roleId));
        if (userRoles.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> userIds = userRoles.stream().map(SysUserRole::getUserId).collect(Collectors.toList());
        List<SysUser> users = listByIds(userIds);
        users.forEach(u -> u.setPassword(null));
        return users;
    }

    @Override
    public List<SysRole> getRolesByUserId(Long userId) {
        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, userId));
        if (userRoles.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
        List<SysRole> roles = sysRoleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                .in(SysRole::getId, roleIds)
                .eq(SysRole::getStatus, 0)
                .eq(SysRole::getDelFlag, 0));

        Set<Long> validRoleIds = roles.stream().map(SysRole::getId).collect(Collectors.toSet());
        List<Long> orphanedIds = roleIds.stream()
                .filter(id -> !validRoleIds.contains(id))
                .toList();
        if (!orphanedIds.isEmpty()) {
            for (Long orphanedId : orphanedIds) {
                sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, userId)
                        .eq(SysUserRole::getRoleId, orphanedId));
            }
        }

        return roles;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(Long id) {
        sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, id));
        clearUserCache(id);
        return super.removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeByIds(Collection<?> list) {
        for (Object id : list) {
            Long userId = (Long) id;
            sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                    .eq(SysUserRole::getUserId, userId));
            clearUserCache(userId);
        }
        return super.removeByIds(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLoginInfo(Long userId, String ip) {
        update(new LambdaUpdateWrapper<SysUser>()
                .set(SysUser::getLoginIp, ip)
                .set(SysUser::getLoginDate, LocalDateTime.now())
                .set(SysUser::getLoginFailCount, 0)
                .setSql("login_count = login_count + 1")
                .eq(SysUser::getId, userId));
    }

    @Override
    public void updateLoginFailCount(Long userId, int failCount) {
        update(new LambdaUpdateWrapper<SysUser>()
                .set(SysUser::getLoginFailCount, failCount)
                .eq(SysUser::getId, userId));
    }
}