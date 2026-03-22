# Tasks

## 第一阶段：基础物品

- [x] Task 1: 创建基础物品注册
  - [x] SubTask 1.1: 在 Fuzhouplan.java 中注册新物品
  - [x] SubTask 1.2: 创建物品类：铁罐(IronCan)、石灰粉(LimePowder)、蒸馏水(DistilledWater)、无核酸酶水瓶(NucleaseFreeWater)、氨水瓶(AmmoniaBottle)、醋瓶(VinegarBottle)、乙二胺(Ethylenediamine)、Tris糊糊(TrisPaste)、Tris粉末(TrisPowder)、EDTA糊糊(EDTAPaste)、EDTA粉末(EDTAPowder)、TE缓冲液(TEBuffer)、TE缓冲液储存罐(TEBufferCan)、未知混合物(UnknownMixture)
  - [x] SubTask 1.3: 创建物品模型JSON文件
  - [ ] SubTask 1.4: 创建物品贴图PNG文件（需手动创建16x16贴图）
  - [x] SubTask 1.5: 添加中英文语言文件条目

- [x] Task 2: 创建生物基因提取器物品
  - [x] SubTask 2.1: 创建 BioGeneExtractorItem 类
  - [x] SubTask 2.2: 实现对麻醉生物的检测逻辑
  - [x] SubTask 2.3: 实现消耗TE缓冲液储存罐逻辑
  - [x] SubTask 2.4: 实现生成"[生物名称]TE缓冲液储存罐"逻辑
  - [x] SubTask 2.5: 添加物品模型、语言文件

## 第二阶段：基础合成配方

- [x] Task 3: 创建简单合成配方
  - [x] SubTask 3.1: 铁罐合成配方（U字形铁粒+中间铁锭）
  - [x] SubTask 3.2: 乙二胺合成配方（氨水瓶+粘液球）
  - [x] SubTask 3.3: Tris糊糊合成配方（3糖+氨水瓶+石灰粉）
  - [x] SubTask 3.4: EDTA糊糊合成配方（2铁粒+醋瓶+乙二胺+火药）
  - [x] SubTask 3.5: TE缓冲液储存罐合成配方（铁罐+TE缓冲液）
  - [x] SubTask 3.6: 石灰粉合成配方（圆石烧炼）

- [x] Task 4: 创建熔炉烧炼配方
  - [x] SubTask 4.1: 水瓶→蒸馏水
  - [x] SubTask 4.2: Tris糊糊→Tris粉末
  - [x] SubTask 4.3: EDTA糊糊→EDTA粉末

## 第三阶段：发酵桶方块

- [x] Task 5: 创建发酵桶方块
  - [x] SubTask 5.1: 创建 FermentationBarrelBlock 类
  - [x] SubTask 5.2: 创建 FermentationBarrelBlockEntity 类
  - [x] SubTask 5.3: 实现发酵逻辑（腐肉→氨水，2游戏日；小麦→醋，1游戏日）
  - [x] SubTask 5.4: 创建方块模型JSON文件
  - [ ] SubTask 5.5: 创建方块贴图PNG文件（需手动创建16x16贴图）
  - [x] SubTask 5.6: 注册方块和方块实体

## 第四阶段：分子蒸馏塔方块

- [x] Task 6: 创建分子蒸馏塔方块
  - [x] SubTask 6.1: 创建 MolecularDistillationTowerBlock 类
  - [x] SubTask 6.2: 创建 MolecularDistillationTowerBlockEntity 类
  - [x] SubTask 6.3: 实现FE能量存储接口(IEnergyStorage)
  - [x] SubTask 6.4: 实现蒸馏逻辑（蒸馏水+能量→无核酸酶水瓶）
  - [x] SubTask 6.5: 创建方块模型JSON文件
  - [ ] SubTask 6.6: 创建方块贴图PNG文件（需手动创建16x16贴图）
  - [x] SubTask 6.7: 注册方块和方块实体

## 第五阶段：精密搅拌器方块

- [x] Task 7: 创建精密搅拌器方块
  - [x] SubTask 7.1: 创建 PrecisionStirrerBlock 类
  - [x] SubTask 7.2: 创建 PrecisionStirrerBlockEntity 类
  - [x] SubTask 7.3: 创建 PrecisionStirrerMenu 类（GUI容器）
  - [x] SubTask 7.4: 创建 PrecisionStirrerScreen 类（GUI界面）
  - [x] SubTask 7.5: 实现PH值小游戏逻辑
    - [x] 两个进度条系统
    - [x] 4个控制按钮逻辑
    - [x] 第二进度条随机移动逻辑
    - [x] 15%误差判定逻辑
    - [x] 5次同步成功判定逻辑
  - [x] SubTask 7.6: 实现合成逻辑（成功→TE缓冲液，失败→未知混合物）
  - [x] SubTask 7.7: 创建方块模型JSON文件
  - [ ] SubTask 7.8: 创建方块贴图PNG文件（需手动创建16x16贴图）
  - [x] SubTask 7.9: 注册方块、方块实体、容器类型

## 第六阶段：整合与测试

- [x] Task 8: 整合所有功能
  - [x] SubTask 8.1: 确保所有物品在创造模式标签页中显示
  - [x] SubTask 8.2: 添加所有语言文件条目
  - [x] SubTask 8.3: 代码无诊断错误

# Task Dependencies
- [Task 2] depends on [Task 1]
- [Task 3] depends on [Task 1]
- [Task 4] depends on [Task 1]
- [Task 5] depends on [Task 1]
- [Task 6] depends on [Task 1]
- [Task 7] depends on [Task 1]
- [Task 8] depends on [Task 1, Task 2, Task 3, Task 4, Task 5, Task 6, Task 7]

# Parallelizable Work
- Task 1 完成后，Task 2-7 可以并行开发
- Task 3 和 Task 4 可以并行
- Task 5、Task 6、Task 7 可以并行

# 待手动完成的任务
以下贴图文件需要手动创建（16x16 PNG格式）：

**物品贴图** (src/main/resources/assets/fuzhouplan/textures/item/):
- iron_can.png
- lime_powder.png
- distilled_water.png
- nuclease_free_water.png
- ammonia_bottle.png
- vinegar_bottle.png
- ethylenediamine.png
- tris_paste.png
- tris_powder.png
- edta_paste.png
- edta_powder.png
- te_buffer.png
- te_buffer_can.png
- unknown_mixture.png
- bio_gene_extractor.png

**方块贴图** (src/main/resources/assets/fuzhouplan/textures/block/):
- fermentation_barrel_bottom.png
- fermentation_barrel_side.png
- fermentation_barrel_top.png
- fermentation_barrel_side_stage1.png, stage2.png, stage3.png
- fermentation_barrel_top_stage1.png, stage2.png, stage3.png
- molecular_distillation_tower_top.png
- molecular_distillation_tower_side.png
- precision_stirrer_bottom.png
- precision_stirrer_side.png
- precision_stirrer_top.png
