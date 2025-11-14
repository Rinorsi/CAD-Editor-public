
[English](./README.md) | [简体中文](./README_CN.md)

## CAD Editor 下载途径
[![CurseForge](https://cf.way2muchnoise.eu/1352735.svg?badge_style=for_the_badge)](https://legacy.curseforge.com/minecraft/mc-mods/cad-editor)  [![Modrinth](https://img.shields.io/modrinth/dt/cad-editor?label=Modrinth&style=for-the-badge&logo=modrinth)](https://modrinth.com/project/cad-editor)  [![License](https://img.shields.io/badge/License-MIT-blue.svg?style=for-the-badge)](https://mit-license.org/)

---

## CAD Editor 是什么

一款简洁直观的游戏内可视化编辑器，让你在当前世界里直接编辑物品、方块或实体。
支持 Minecraft 组件系统（1.20.5+），涵盖常见要素：自定义模型数据、属性、附魔、名字/描述、容器与战利品表、效果、告示牌文本等。

CAD Editor（Component And Data Editor）是在 IBE Editor（原作者 Skye，MIT 许可）基础上重构并持续维护的版本。 我与 Skye 无隶属关系，亦非官方版本，但完整保留了原版权声明与署名。

原项目地址：
https://github.com/skyecodes/IBE-Editor

## 主要特性
* 局内 GUI 编辑：所见即所得地修改并拿到结果
* 组件优先：面向 1.21+ 的 `components` 工作流，同时兼容常见 NBT 用例
* 覆盖面广：物品/方块/实体的常用字段与高级玩法元素
* 快速复制/导出：一键复制为命令，方便分享

## 安装与依赖
* 运行环境：目前仅适用于 Minecraft 1.21.4（后续逐步适配更多版本）
* 加载器：NeoForge 或 Fabric
* Fabric 版本需要安装 [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api)
* 建议使用 Java 21

## 构建
项目基于 [Architectury](https://github.com/architectury) 工具链。

```bash
git clone https://github.com/Rinorsi/CAD-Editor.git
cd CAD-Editor
chmod +x gradlew
./gradlew build
