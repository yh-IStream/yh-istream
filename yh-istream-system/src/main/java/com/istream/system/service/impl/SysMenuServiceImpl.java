package com.istream.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.istream.common.enums.MenuTypeEnum;
import com.istream.common.model.BaseEntity;
import com.istream.framework.security.SecurityUtils;
import com.istream.framework.util.TreeUtils;
import com.istream.system.entity.SysMenu;
import com.istream.system.entity.SysRoleMenu;
import com.istream.system.mapper.SysMenuMapper;
import com.istream.system.mapper.SysRoleMenuMapper;
import com.istream.system.service.SysMenuService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.time.Duration;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    private final SysRoleMenuMapper sysRoleMenuMapper;
    private final RedissonClient redissonClient;

    private static final String PERM_CACHE_PREFIX = "perm:cache:";
    private static final Duration PERM_CACHE_TTL = Duration.ofMinutes(30);

    @Override
    public List<String> getPermissionsByUserId(Long userId) {
        String cacheKey = PERM_CACHE_PREFIX + userId;
        RBucket<List<String>> bucket = redissonClient.getBucket(cacheKey);
        List<String> cached = bucket.get();
        if (cached != null) {
            return cached;
        }
        List<String> permissions = baseMapper.selectPermissionsByUserId(userId);
        bucket.set(permissions, PERM_CACHE_TTL);
        return permissions;
    }

    @Override
    public List<SysMenu> listMenuTree() {
        List<SysMenu> allMenus = list(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getStatus, 0)
                .ne(SysMenu::getMenuType, MenuTypeEnum.BUTTON.getCode())
                .orderByAsc(SysMenu::getOrderNum));
        return TreeUtils.build(allMenus, SysMenu::getId, SysMenu::getParentId,
                SysMenu::setChildren);
    }

    @Override
    public List<SysMenu> listAllMenuTree() {
        List<SysMenu> allMenus = list(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getStatus, 0)
                .orderByAsc(SysMenu::getOrderNum));
        return TreeUtils.build(allMenus, SysMenu::getId, SysMenu::getParentId,
                SysMenu::setChildren);
    }

    @Override
    public List<SysMenu> getCurrentUserMenuTree() {
        Long userId = SecurityUtils.getLoginUserId();
        if (userId == null) {
            return Collections.emptyList();
        }
        List<Long> menuIds = baseMapper.selectMenuIdsByUserId(userId);
        if (menuIds == null || menuIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<SysMenu> allMenus = list(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getStatus, 0)
                .ne(SysMenu::getMenuType, MenuTypeEnum.BUTTON.getCode())
                .orderByAsc(SysMenu::getOrderNum));

        Map<Long, SysMenu> menuMap = new HashMap<>();
        for (SysMenu menu : allMenus) {
            menuMap.put(menu.getId(), menu);
        }

        Set<Long> needIds = new HashSet<>(menuIds);
        Set<Long> queue = new HashSet<>(menuIds);
        while (!queue.isEmpty()) {
            Set<Long> next = new HashSet<>();
            for (Long id : queue) {
                SysMenu menu = menuMap.get(id);
                if (menu != null) {
                    Long pid = menu.getParentId();
                    if (pid != null && pid != 0 && needIds.add(pid)) {
                        next.add(pid);
                    }
                }
            }
            queue = next;
        }

        List<SysMenu> userMenus = new ArrayList<>();
        for (SysMenu menu : allMenus) {
            if (needIds.contains(menu.getId())
                    && (menu.getVisible() == null || menu.getVisible() == 1)) {
                userMenus.add(menu);
            }
        }

        userMenus.sort(Comparator.comparingInt((SysMenu a) -> a.getOrderNum() != null ? a.getOrderNum() : 0).thenComparingLong(BaseEntity::getId));

//        userMenus.sort((a, b) -> {
//            int cmp = Integer.compare(
//                    a.getOrderNum() != null ? a.getOrderNum() : 0,
//                    b.getOrderNum() != null ? b.getOrderNum() : 0);
//            if (cmp != 0) return cmp;
//            return Long.compare(a.getId(), b.getId());
//        });

        return TreeUtils.build(userMenus, SysMenu::getId, SysMenu::getParentId,
                SysMenu::setChildren);
    }

    @Override
    public boolean save(SysMenu entity) {
        boolean result = super.save(entity);
        if (result) {
            clearAllPermissionCache();
        }
        return result;
    }

    @Override
    public boolean updateById(SysMenu entity) {
        SysMenu old = getById(entity.getId());
        boolean result = super.updateById(entity);
        if (result && (old == null || !Objects.equals(old.getPermission(), entity.getPermission())
                || !Objects.equals(old.getStatus(), entity.getStatus())
                || !Objects.equals(old.getVisible(), entity.getVisible()))) {
            clearAllPermissionCache();
        }
        return result;
    }

    @Override
    public boolean removeById(Serializable id) {
        SysMenu menu = getById(id);
        boolean result = super.removeById(id);
        if (result && menu != null && menu.getPermission() != null && !menu.getPermission().isEmpty()) {
            clearAllPermissionCache();
        }
        return result;
    }

    private void clearAllPermissionCache() {
        Iterable<String> keys = redissonClient.getKeys().getKeysByPattern(PERM_CACHE_PREFIX + "*");
        for (String key : keys) {
            redissonClient.getBucket(key).delete();
        }
    }

    @Override
    public boolean hasChildren(Long menuId) {
        return count(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getParentId, menuId)) > 0;
    }

    @Override
    public boolean hasRoles(Long menuId) {
        return sysRoleMenuMapper.selectCount(new LambdaQueryWrapper<SysRoleMenu>()
                .eq(SysRoleMenu::getMenuId, menuId)) > 0;
    }
}