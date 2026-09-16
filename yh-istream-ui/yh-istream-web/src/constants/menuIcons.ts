import type { Component } from 'vue'
import {
  HomeOutline,
  SettingsOutline,
  CodeSlashOutline,
  PersonOutline,
  MenuOutline,
  DesktopOutline,
  CloudUploadOutline,
  ListOutline,
  DocumentTextOutline,
  LogInOutline,
  ClipboardOutline,
  FolderOpenOutline,
  GitBranchOutline,
  BookOutline,
  CogOutline,
  ShieldCheckmarkOutline,
} from '@vicons/ionicons5'

/**
 * 菜单图标映射
 *
 * 数据库 sys_menu.icon 存储字符串标识，前端通过此映射解析为 Vue 组件。
 * 新增菜单图标时只需在此处追加映射，无需修改布局组件。
 */
export const MENU_ICON_MAP: Record<string, Component> = {
  home: HomeOutline,
  system: SettingsOutline,
  monitor: DesktopOutline,
  generator: CodeSlashOutline,
  user: PersonOutline,
  role: ShieldCheckmarkOutline,
  menu: MenuOutline,
  dept: GitBranchOutline,
  config: CogOutline,
  dict: BookOutline,
  file: FolderOpenOutline,
  upload: CloudUploadOutline,
  list: ListOutline,
  log: DocumentTextOutline,
  operLog: ClipboardOutline,
  loginInfo: LogInOutline,
}