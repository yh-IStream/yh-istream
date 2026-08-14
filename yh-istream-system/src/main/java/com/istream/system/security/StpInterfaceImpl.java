package com.istream.system.security;

import cn.dev33.satoken.stp.StpInterface;
import com.istream.system.mapper.SysMenuMapper;
import com.istream.system.mapper.SysRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Sa-Token 权限加载实现
 * <p>
 * 从数据库加载当前用户的角色和权限码集合
 */
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final SysRoleMapper sysRoleMapper;
    private final SysMenuMapper sysMenuMapper;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return sysMenuMapper.selectPermissionsByUserId(Long.valueOf(loginId.toString()));
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return sysRoleMapper.selectRoleKeysByUserId(Long.valueOf(loginId.toString()));
    }
}