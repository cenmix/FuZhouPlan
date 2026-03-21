# 浮舟计划 / FuZhou Plan

**版本**: 0.1.0  
**Minecraft**: 1.20.1  
**Forge**: 47.x  
**作者**: FuZhou

---

## 简介

浮舟计划是一个 Minecraft Forge 模组，为游戏添加了实用的物品和植物。

在这个模组中，你将拥有控制和收集生物的能力，不想让僵尸逃跑？哈，使用麻醉剂麻醉生物，使其5秒内无法动弹。

**注意！该mod属于早期开发阶段，没有具体主线、内容，兼容性可能发生问题**

---

## 物品

### 针管
基础材料，用于制作麻醉剂。

**合成**: 玻璃 + 铁粒 × 2

### 生物麻醉剂
一种强效麻醉剂，对生物使用后可使其定身 5 秒。

- 仅对最大生命值 ≤ 30 的生物有效（太强的生物无法被麻醉）
- 仅对 Mob 类生物有效（玩家、盔甲架等无效）
- 已被麻醉的生物不会重复生效
- 使用时会播放玻璃破碎音效
- 可堆叠至 16 个

### 绿浆果
一种清新的小果实，可以直接食用或种植。

- 食用恢复 2 点饥饿值
- 会引起 5 秒反胃效果
- 右键草方块或泥土可种植浆果丛

---

## 方块

### 绿浆果丛
生长在平原的小型灌木，成熟后可收获绿浆果。

- 自然生成于平原生物群系
- 共有 4 个生长阶段（AGE_3 属性，0-3）
- 生长需要上方光照等级 ≥ 9
- 随机生长概率为 20%（random.nextInt(5) == 0）
- 成熟后（阶段 2-3）右键收获 1-2 个绿浆果
- 收获后重置为阶段 1，可持续采摘
- 可使用骨粉加速生长
- 幼苗期碰撞箱较小，成熟后变大

---

## 安装

- 需要双端安装（客户端和服务端）
- 需要 Minecraft 1.20.1 和 Forge 47.x

---

## 许可证

本项目采用 MIT 许证证开源，详见 [LICENSE](LICENSE) 文件

---

## 支持作者

如果你喜欢这个模组，觉得它给你的 Minecraft 之旅增添了乐趣，欢迎请作者喝杯咖啡！你的支持是我继续开发的动力 ❤️

## 关于mod

本模组代码由 AI 辅助生成，虽然作者会尽力确保代码质量，但难免存在疏漏。如遇到 bug 或兼容性问题，欢迎在 GitHub 提 Issue 反馈，作者会尽快处理！

---

# English Version

## Introduction

FuZhou Plan is a Minecraft Forge mod that adds practical items and plants to the game.

In this mod, you'll have the ability to control and collect creatures. Don't want that zombie to escape? Ha! Use the anesthetic to immobilize creatures for 5 seconds.

**Note: This mod is in early development stage, with no specific storyline or content. Compatibility issues may occur.**

---

## Items

### Syringe
A basic material used to craft anesthetics.

**Recipe**: Glass + Iron Nugget × 2

### Bio Anesthetic
A powerful anesthetic that immobilizes creatures for 5 seconds when used.

- Only effective on creatures with max health ≤ 30 (stronger creatures cannot be anesthetized)
- Only works on Mob-type entities (players, armor stands, etc. are unaffected)
- Already anesthetized creatures won't be affected again
- Plays glass breaking sound when used
- Stackable up to 16

### Green Berry
A refreshing small fruit that can be eaten or planted.

- Restores 2 hunger points when eaten
- Causes 5 seconds of nausea effect
- Right-click on grass block or dirt to plant a berry bush

---

## Blocks

### Green Berry Bush
A small shrub that grows in plains, yielding green berries when mature.

- Naturally generates in plains biomes
- Has 4 growth stages (AGE_3 property, 0-3)
- Requires light level ≥ 9 above to grow
- 20% random growth chance
- Right-click to harvest 1-2 green berries when mature (stage 2-3)
- Resets to stage 1 after harvest, can be picked continuously
- Can be accelerated with bone meal
- Smaller hitbox when young, larger when mature

---

## Installation

- Requires installation on both client and server
- Requires Minecraft 1.20.1 and Forge 47.x

---

## License

This project is open-sourced under the MIT License. See [LICENSE](LICENSE) file for details.

---

## Support the Author

If you like this mod and it adds fun to your Minecraft journey, consider buying the author a coffee! Your support is my motivation to keep developing ❤️

## About the Mod

This mod's code is AI-assisted. While the author strives to ensure code quality, oversights may occur. If you encounter bugs or compatibility issues, feel free to submit an Issue on GitHub, and the author will address it as soon as possible!
