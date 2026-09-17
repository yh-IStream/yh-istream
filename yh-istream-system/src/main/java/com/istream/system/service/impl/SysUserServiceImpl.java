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
import com.istream.common.enums.StatusEnum;
import com.istream.common.exception.BusinessException;
import com.istream.system.helper.UserCacheHelper;
import com.istream.system.model.dto.SysUserCreateDTO;
import com.istream.system.model.dto.SysUserUpdateDTO;
import com.istream.system.model.query.SysUserQuery;
import com.istream.system.entity.SysRole;
import com.istream.system.entity.SysUser;
import com.istream.system.entity.SysUserRole;
import com.istream.system.mapper.SysRoleMapper;
import com.istream.system.mapper.SysUserMapper;
import com.istream.system.mapper.SysUserRoleMapper;
import com.istream.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户管理服务实现
 *
 * @author istream
 * @since 2026-08-17
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleMapper sysRoleMapper;
    private final UserCacheHelper userCacheHelper;

    @Override
    @Transactional(readOnly = true)
    public SysUser getByUsername(String username) {
        return getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
    }

    @Override
    @Transactional(readOnly = true)
    @DataScope(deptAlias = "d", userAlias = "u")
    public IPage<SysUser> page(SysUserQuery query) {
        return baseMapper.selectUserPage(new Page<>(query.getPageNum(), query.getPageSize()), query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createUser(SysUserCreateDTO dto) {
        if (getByUsername(dto.getUsername()) != null) {
            throw new BusinessException(ResultCode.USER_USERNAME_DUPLICATE);
        }
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(BCrypt.hashpw(dto.getPassword()));
        user.setNickname(dto.getNickname());
        user.setDeptId(dto.getDeptId());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setGender(dto.getGender());
        user.setStatus(dto.getStatus() != null ? dto.getStatus() : StatusEnum.ENABLED.getCode());
        user.setRemark(dto.getRemark());
        save(user);
        if (dto.getRoleIds() != null && !dto.getRoleIds().isEmpty()) {
            assignRoles(user.getId(), dto.getRoleIds());
        } else {
            assignDefaultRole(user.getId());
        }
    }

    private void assignDefaultRole(Long userId) {
        SysRole defaultRole = sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleKey, Constants.DEFAULT_ROLE_KEY)
                .eq(SysRole::getStatus, StatusEnum.ENABLED.getCode())
                .eq(SysRole::getDelFlag, Constants.DEL_FLAG_NORMAL));
        if (defaultRole != null) {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(userId);
            ur.setRoleId(defaultRole.getId());
            sysUserRoleMapper.insert(ur);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(SysUserUpdateDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException(ResultCode.USER_ID_REQUIRED);
        }
        SysUser exist = getByUsername(dto.getUsername());
        if (exist != null && !exist.getId().equals(dto.getId())) {
            throw new BusinessException(ResultCode.USER_USERNAME_DUPLICATE);
        }
        update(new LambdaUpdateWrapper<SysUser>()
                .set(SysUser::getDeptId, dto.getDeptId())
                .set(SysUser::getUsername, dto.getUsername())
                .set(SysUser::getNickname, dto.getNickname())
                .set(SysUser::getEmail, dto.getEmail())
                .set(SysUser::getPhone, dto.getPhone())
                .set(SysUser::getGender, dto.getGender())
                .set(SysUser::getStatus, dto.getStatus())
                .set(SysUser::getRemark, dto.getRemark())
                .eq(SysUser::getId, dto.getId()));
        userCacheHelper.evictAll(dto.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignUserRoles(Long userId, List<Long> roleIds) {
        sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId));
        if (roleIds != null && !roleIds.isEmpty()) {
            Set<Long> distinctRoleIds = new HashSet<>(roleIds);
            List<SysRole> validRoles = sysRoleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                    .in(SysRole::getId, distinctRoleIds)
                    .eq(SysRole::getStatus, StatusEnum.ENABLED.getCode())
                    .eq(SysRole::getDelFlag, Constants.DEL_FLAG_NORMAL));
            if (validRoles.size() != distinctRoleIds.size()) {
                throw new BusinessException(ResultCode.USER_ROLE_INVALID);
            }
            assignRoles(userId, distinctRoleIds.stream().toList());
        }
        userCacheHelper.evictAll(userId);
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
        if (!userRoles.isEmpty()) {
            sysUserRoleMapper.insertBatch(userRoles);
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
            userCacheHelper.evictAll(userId);
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SysUser> getUsersByRoleId(Long roleId) {
        List<SysUser> users = baseMapper.selectUsersByRoleId(roleId);
        users.forEach(u -> u.setPassword(null));
        return users;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SysRole> getRolesByUserId(Long userId) {
        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, userId));
        if (userRoles.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).toList();
        return sysRoleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                .in(SysRole::getId, roleIds)
                .eq(SysRole::getStatus, StatusEnum.ENABLED.getCode())
                .eq(SysRole::getDelFlag, Constants.DEL_FLAG_NORMAL));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cleanOrphanedUserRoles(Long userId) {
        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, userId));
        if (userRoles.isEmpty()) {
            return;
        }
        List<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).toList();
        List<SysRole> validRoles = sysRoleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                .in(SysRole::getId, roleIds)
                .eq(SysRole::getStatus, StatusEnum.ENABLED.getCode())
                .eq(SysRole::getDelFlag, Constants.DEL_FLAG_NORMAL));

        Set<Long> validRoleIds = validRoles.stream().map(SysRole::getId).collect(Collectors.toSet());
        List<Long> orphanedIds = roleIds.stream()
                .filter(id -> !validRoleIds.contains(id))
                .toList();
        if (!orphanedIds.isEmpty()) {
            sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                    .eq(SysUserRole::getUserId, userId)
                    .in(SysUserRole::getRoleId, orphanedIds));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(Long id) {
        checkSuperAdmin(id);
        sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, id));
        userCacheHelper.evictAll(id);
        return super.removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeByIds(Collection<?> list) {
        @SuppressWarnings("unchecked")
        List<SysUser> users = listByIds((Collection<? extends Serializable>) list);
        for (SysUser user : users) {
            if (hasSuperAdminRole(user.getId())) {
                throw new BusinessException(ResultCode.SUPER_ADMIN_PROTECT);
            }
        }
        sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                .in(SysUserRole::getUserId, list));
        for (Object id : list) {
            userCacheHelper.evictAll((Long) id);
        }
        return super.removeByIds(list);
    }

    private void checkSuperAdmin(Long userId) {
        if (hasSuperAdminRole(userId)) {
            throw new BusinessException(ResultCode.SUPER_ADMIN_PROTECT);
        }
    }

    private boolean hasSuperAdminRole(Long userId) {
        return getRolesByUserId(userId).stream()
                .anyMatch(role -> Constants.SUPER_ADMIN_ROLE.equals(role.getRoleKey()));
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

    @Override
    public IPage<SysUser> pageExport(long pageNum, long pageSize) {
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        IPage<SysUser> result = baseMapper.selectPage(page, new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getDelFlag, Constants.DEL_FLAG_NORMAL)
                .orderByDesc(SysUser::getCreateTime));
        result.getRecords().forEach(user -> user.setPassword(null));
        return result;
    }
}