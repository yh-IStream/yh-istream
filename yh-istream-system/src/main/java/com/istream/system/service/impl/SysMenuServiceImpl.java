package com.istream.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.istream.common.constant.Constants;
import com.istream.common.enums.MenuTypeEnum;
import com.istream.common.enums.ResultCode;
import com.istream.common.enums.StatusEnum;
import com.istream.common.exception.BusinessException;
import com.istream.common.model.BaseEntity;
import com.istream.framework.security.SecurityUtils;
import com.istream.framework.util.TreeUtils;
import com.istream.system.entity.SysMenu;
import com.istream.system.entity.SysRoleMenu;
import com.istream.system.mapper.SysMenuMapper;
import com.istream.system.mapper.SysRoleMenuMapper;
import com.istream.system.service.SysMenuService;
import com.istream.framework.cache.CacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.istream.common.constant.Constants.PERM_CACHE_PREFIX;

/**
 * 菜单管理服务实现
 *
 * @author istream
 * @since 2026-08-17
 */
@Service
@RequiredArgsConstructor
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    private final SysRoleMenuMapper sysRoleMenuMapper;
    private final CacheService cacheService;

    private static final Duration PERM_CACHE_TTL = Duration.ofMinutes(30);

    @Override
    public List<String> getPermissionsByUserId(Long userId) {
        String cacheKey = PERM_CACHE_PREFIX + userId;
        List<String> cached = cacheService.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        List<String> permissions = baseMapper.selectPermissionsByUserId(userId);
        cacheService.set(cacheKey, permissions, PERM_CACHE_TTL);
        return permissions;
    }

    @Override
    public List<SysMenu> listMenuTree() {
        List<SysMenu> allMenus = list(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getStatus, StatusEnum.ENABLED.getCode())
                .ne(SysMenu::getMenuType, MenuTypeEnum.BUTTON.getCode())
                .orderByAsc(SysMenu::getOrderNum));
        return TreeUtils.build(allMenus, SysMenu::getId, SysMenu::getParentId,
                SysMenu::setChildren);
    }

    @Override
    public List<SysMenu> listAllMenuTree() {
        List<SysMenu> allMenus = list(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getStatus, StatusEnum.ENABLED.getCode())
                .orderByAsc(SysMenu::getOrderNum));
        return TreeUtils.build(allMenus, SysMenu::getId, SysMenu::getParentId,
                SysMenu::setChildren);
    }

    @Override
    public List<SysMenu> getCurrentUserMenuTree() {
        Long userId = SecurityUtils.requireLoginUserId();

        List<Long> menuIds = baseMapper.selectMenuIdsByUserId(userId);
        if (menuIds == null || menuIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<SysMenu> allMenus = loadAllVisibleMenus();
        Map<Long, SysMenu> menuMap = buildMenuMap(allMenus);
        Set<Long> needIds = collectAncestorIds(new HashSet<>(menuIds), menuMap);
        List<SysMenu> userMenus = filterVisibleMenus(allMenus, needIds);

        return TreeUtils.build(userMenus, SysMenu::getId, SysMenu::getParentId,
                SysMenu::setChildren);
    }

    private List<SysMenu> loadAllVisibleMenus() {
        return list(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getStatus, StatusEnum.ENABLED.getCode())
                .ne(SysMenu::getMenuType, MenuTypeEnum.BUTTON.getCode())
                .orderByAsc(SysMenu::getOrderNum));
    }

    private Map<Long, SysMenu> buildMenuMap(List<SysMenu> allMenus) {
        Map<Long, SysMenu> menuMap = new HashMap<>();
        for (SysMenu menu : allMenus) {
            menuMap.put(menu.getId(), menu);
        }
        return menuMap;
    }

    private Set<Long> collectAncestorIds(Set<Long> menuIds, Map<Long, SysMenu> menuMap) {
        Set<Long> needIds = new HashSet<>(menuIds);
        Set<Long> queue = new HashSet<>(menuIds);
        while (!queue.isEmpty()) {
            Set<Long> next = new HashSet<>();
            for (Long id : queue) {
                SysMenu menu = menuMap.get(id);
                if (menu != null) {
                    Long pid = menu.getParentId();
                    if (pid != null && !pid.equals(Constants.ROOT_PARENT_ID) && needIds.add(pid)) {
                        next.add(pid);
                    }
                }
            }
            queue = next;
        }
        return needIds;
    }

    private List<SysMenu> filterVisibleMenus(List<SysMenu> allMenus, Set<Long> needIds) {
        List<SysMenu> userMenus = new ArrayList<>();
        for (SysMenu menu : allMenus) {
            if (needIds.contains(menu.getId())
                    && (menu.getVisible() == null || Objects.equals(menu.getVisible(), Constants.MENU_VISIBLE_SHOW))) {
                userMenus.add(menu);
            }
        }
        userMenus.sort(Comparator.comparingInt((SysMenu a) -> a.getOrderNum() != null ? a.getOrderNum() : 0)
                .thenComparingLong(BaseEntity::getId));
        return userMenus;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(SysMenu entity) {
        boolean result = super.save(entity);
        if (result) {
            clearAllPermissionCache();
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(SysMenu entity) {
        SysMenu old = getById(entity.getId());
        if (old != null && entity.getParentId() != null
                && !entity.getParentId().equals(old.getParentId())
                && !entity.getParentId().equals(Constants.ROOT_PARENT_ID)) {
            checkMenuCycleReference(entity.getId(), entity.getParentId());
        }
        boolean result = super.updateById(entity);
        if (result && (old == null || !Objects.equals(old.getPermission(), entity.getPermission())
                || !Objects.equals(old.getStatus(), entity.getStatus())
                || !Objects.equals(old.getVisible(), entity.getVisible()))) {
            clearAllPermissionCache();
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(Serializable id) {
        List<Long> allIds = collectDescendantIds((Long) id);
        allIds.add((Long) id);
        sysRoleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>()
                .in(SysRoleMenu::getMenuId, allIds));
        boolean result = super.removeByIds(allIds);
        if (result) {
            clearAllPermissionCache();
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeByIds(Collection<?> list) {
        List<Long> allIds = new ArrayList<>();
        for (Object id : list) {
            allIds.add((Long) id);
            allIds.addAll(collectDescendantIds((Long) id));
        }
        sysRoleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>()
                .in(SysRoleMenu::getMenuId, allIds));
        boolean result = super.removeByIds(allIds);
        if (result) {
            clearAllPermissionCache();
        }
        return result;
    }

    private List<Long> collectDescendantIds(Long parentId) {
        List<SysMenu> allMenus = list(new LambdaQueryWrapper<SysMenu>()
                .select(SysMenu::getId, SysMenu::getParentId));
        Map<Long, List<Long>> parentChildMap = allMenus.stream()
                .collect(Collectors.groupingBy(SysMenu::getParentId,
                        Collectors.mapping(SysMenu::getId, Collectors.toList())));
        List<Long> result = new ArrayList<>();
        collectChildren(parentChildMap, parentId, result);
        return result;
    }

    private void collectChildren(Map<Long, List<Long>> parentChildMap, Long parentId, List<Long> result) {
        List<Long> children = parentChildMap.get(parentId);
        if (children != null) {
            for (Long childId : children) {
                result.add(childId);
                collectChildren(parentChildMap, childId, result);
            }
        }
    }

    private void clearAllPermissionCache() {
        cacheService.deleteByPattern(PERM_CACHE_PREFIX + "*");
    }

    /**
     * 校验菜单循环引用：新父菜单不能是当前菜单自身或其子菜单
     *
     * @param menuId   当前菜单ID
     * @param parentId 新的父菜单ID
     */
    private void checkMenuCycleReference(Long menuId, Long parentId) {
        if (menuId.equals(parentId)) {
            throw new BusinessException(ResultCode.MENU_CYCLE_REFERENCE.getCode(),
                    ResultCode.MENU_CYCLE_REFERENCE.getMsg());
        }
        List<SysMenu> allMenus = list(new LambdaQueryWrapper<SysMenu>()
                .select(SysMenu::getId, SysMenu::getParentId));
        Map<Long, List<Long>> parentChildMap = allMenus.stream()
                .collect(Collectors.groupingBy(SysMenu::getParentId,
                        Collectors.mapping(SysMenu::getId, Collectors.toList())));
        checkMenuCycleInMemory(parentChildMap, menuId, parentId);
    }

    private void checkMenuCycleInMemory(Map<Long, List<Long>> parentChildMap, Long menuId, Long parentId) {
        List<Long> children = parentChildMap.get(menuId);
        if (children != null) {
            for (Long childId : children) {
                if (childId.equals(parentId)) {
                    throw new BusinessException(ResultCode.MENU_CYCLE_REFERENCE.getCode(),
                            ResultCode.MENU_CYCLE_REFERENCE.getMsg());
                }
                checkMenuCycleInMemory(parentChildMap, childId, parentId);
            }
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