package com.istream.admin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.istream.common.enums.ResultCode;
import com.istream.system.entity.SysMenu;
import com.istream.system.service.SysMenuService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("SysMenuController 接口测试")
class SysMenuControllerTest {

    @Mock
    private SysMenuService sysMenuService;

    @InjectMocks
    private SysMenuController sysMenuController;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(sysMenuController).build();
    }

    private SysMenu createMenu(Long id, String menuName, String menuType, Long parentId) {
        SysMenu menu = new SysMenu();
        menu.setId(id);
        menu.setMenuName(menuName);
        menu.setMenuType(menuType);
        menu.setParentId(parentId);
        menu.setStatus(0);
        return menu;
    }

    @Nested
    @DisplayName("菜单树查询")
    class TreeTests {

        @Test
        @DisplayName("正常查询")
        void tree_Success() throws Exception {
            when(sysMenuService.listAllMenuTree()).thenReturn(List.of(
                    createMenu(1L, "系统管理", "M", 0L),
                    createMenu(2L, "用户管理", "C", 1L)
            ));

            mockMvc.perform(get("/system/menu/tree"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data[0].menuName").value("系统管理"));
        }
    }

    @Nested
    @DisplayName("根据ID查询")
    class GetByIdTests {

        @Test
        @DisplayName("菜单存在")
        void getById_Success() throws Exception {
            when(sysMenuService.getById(1L)).thenReturn(createMenu(1L, "系统管理", "M", 0L));

            mockMvc.perform(get("/system/menu/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.menuName").value("系统管理"));
        }
    }

    @Nested
    @DisplayName("新增菜单")
    class AddTests {

        @Test
        @DisplayName("正常新增")
        void add_Success() throws Exception {
            SysMenu menu = createMenu(null, "用户管理", "C", 1L);
            when(sysMenuService.save(any(SysMenu.class))).thenReturn(true);

            mockMvc.perform(post("/system/menu")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(menu)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("修改菜单")
    class UpdateTests {

        @Test
        @DisplayName("正常修改")
        void update_Success() throws Exception {
            SysMenu menu = createMenu(2L, "用户管理", "C", 1L);
            when(sysMenuService.updateById(any(SysMenu.class))).thenReturn(true);

            mockMvc.perform(put("/system/menu")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(menu)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("上级菜单不能是自己")
        void update_ParentIsSelf() throws Exception {
            SysMenu menu = createMenu(1L, "系统管理", "M", 1L);

            mockMvc.perform(put("/system/menu")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(menu)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(ResultCode.PARAM_VALID_ERROR.getCode()));
        }
    }

    @Nested
    @DisplayName("删除菜单")
    class DeleteTests {

        @Test
        @DisplayName("有子菜单，无法删除")
        void delete_HasChildren() throws Exception {
            when(sysMenuService.hasChildren(1L)).thenReturn(true);

            mockMvc.perform(delete("/system/menu/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(ResultCode.HAS_CHILDREN.getCode()));
        }

        @Test
        @DisplayName("有关联角色，无法删除")
        void delete_HasRoles() throws Exception {
            when(sysMenuService.hasChildren(1L)).thenReturn(false);
            when(sysMenuService.hasRoles(1L)).thenReturn(true);

            mockMvc.perform(delete("/system/menu/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(ResultCode.HAS_ROLES.getCode()));
        }

        @Test
        @DisplayName("正常删除")
        void delete_Success() throws Exception {
            when(sysMenuService.hasChildren(1L)).thenReturn(false);
            when(sysMenuService.hasRoles(1L)).thenReturn(false);
            when(sysMenuService.removeById(1L)).thenReturn(true);

            mockMvc.perform(delete("/system/menu/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }
}