package com.istream.system.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.istream.common.annotation.DataScope;
import com.istream.common.enums.ResultCode;
import com.istream.common.exception.BusinessException;
import com.istream.common.model.dto.SysUserQuery;
import com.istream.system.entity.SysUser;
import com.istream.system.entity.SysUserRole;
import com.istream.system.mapper.SysUserMapper;
import com.istream.system.mapper.SysUserRoleMapper;
import com.istream.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final SysUserRoleMapper sysUserRoleMapper;

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
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(SysUser user) {
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
        return updateById(user);
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
    public void updateLoginInfo(Long userId, String ip) {
        update(new LambdaUpdateWrapper<SysUser>()
                .set(SysUser::getLoginIp, ip)
                .set(SysUser::getLoginDate, LocalDateTime.now())
                .setSql("login_count = login_count + 1")
                .eq(SysUser::getId, userId));
    }
}