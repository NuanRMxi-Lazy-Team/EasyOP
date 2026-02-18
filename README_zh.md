# EasyOP

EasyOP 是一个基于 Fabric 的 Minecraft 模组，为服务器管理员和单人游戏玩家提供直观的游戏内控制面板。它通过图形用户界面 (HUD) 简化了常见的管理任务。

## 功能

- **游戏内 HUD**: 通过专用 HUD 访问所有管理功能（默认按键：`Left Alt`，可在“控制”中更改）。
- **快速设置 (Fast Settings)**:
  - **时间控制**: 一键将时间设置为日出、白天、中午、日落、夜晚或午夜。
  - **定位**: 轻松查找附近的结构（村庄、堡垒、林地府邸等）、生物群系（沙漠）和兴趣点（图书管理员）。
  - **生物生成**: 快速切换自然生物生成规则。
- **玩家列表 (Player List)**:
  - 查看所有在线玩家。
  - **踢出 (Kick)**: 一键断开玩家连接。
  - **传送 (TP)**: 直接传送到任意在线玩家的位置。
- **游戏规则管理 (Game Rules Management)**:
  - 包含所有 Minecraft 游戏规则的可滚动列表。
  - 提供详细的中英文描述。
  - 支持布尔值规则（TRUE/FALSE 按钮）和整数规则（直接在 HUD 上输入文本）。
- **完全汉化**: 完美支持简体中文和英文。

## 安装

1. 确保已安装 [Fabric Loader](https://fabricmc.net/use/)。
2. 下载模组并将其放入 `.minecraft/mods` 文件夹中。
3. 确保同时安装了 [Fabric API](https://modrinth.com/mod/fabric-api) 和 [Fabric Language Kotlin](https://modrinth.com/mod/fabric-language-kotlin)。

## 使用说明

- 按 `Left Alt`（默认）打开/关闭 EasyOP 面板。
- 使用侧边栏在 **快速设置**、**玩家列表** 和 **游戏规则** 之间切换。
- 在“游戏规则”选项卡中，使用鼠标滚轮滚动列表。
- 要编辑整数型游戏规则，点击“EDIT”，输入数字，然后按“Enter”。

## 许可证

本项目采用 MPL-2.0 许可证。
