package com.istream.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.istream.common.enums.MenuTypeEnum;
import com.istream.system.entity.SysMenu;
import com.istream.system.mapper.SysMenuMapper;
import com.istream.system.service.SysMenuService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    @Override
    public List<String> getPermissionsByUserId(Long userId) {
        return baseMapper.selectPermissionsByUserId(userId);
    }

    @Override
    public List<SysMenu> listMenuTree() {
        List<SysMenu> allMenus = list(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getStatus, 0)
                .ne(SysMenu::getMenuType, MenuTypeEnum.BUTTON.getCode())
                .orderByAsc(SysMenu::getOrderNum));
        return buildTree(allMenus);
    }

    @Override
    public List<SysMenu> listAllMenuTree() {
        List<SysMenu> allMenus = list(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getStatus, 0)
                .orderByAsc(SysMenu::getOrderNum));
        return buildTree(allMenus);
    }

    @Override
    public boolean hasChildren(Long menuId) {
        return count(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getParentId, menuId)) > 0;
    }

    private List<SysMenu> buildTree(List<SysMenu> allMenus) {
        Map<Long, List<SysMenu>> parentMap = allMenus.stream()
                .collect(Collectors.groupingBy(SysMenu::getParentId));

        List<SysMenu> roots = new ArrayList<>();
        for (SysMenu menu : allMenus) {
            if (menu.getParentId() == null || menu.getParentId() == 0L) {
                roots.add(menu);
            }
            menu.setChildren(parentMap.getOrDefault(menu.getId(), new ArrayList<>()));
        }
        return roots;
    }
}