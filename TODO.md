# TODO - 待绘制贴图清单

@Redersha 需要绘制以下贴图：

## 方块贴图

### 1. 烘干机 (DryerBlock)
- `dryer_top.png` - 顶面贴图
- `dryer_side.png` - 侧面贴图
- `dryer_bottom.png` - 底面贴图
- `dryer_front.png` - 正面贴图

### 2. 解析器 (ResolverBlock)
- `resolver_top.png` - 顶面贴图
- `resolver_side.png` - 侧面贴图
- `resolver_bottom.png` - 底面贴图
- `resolver_front.png` - 正面贴图

### 3. 蓝莓灌木 (BlueBerryBushBlock)
- `blue_berry_bush_stage0.png` - 生长阶段0
- `blue_berry_bush_stage1.png` - 生长阶段1
- `blue_berry_bush_stage2.png` - 生长阶段2
- `blue_berry_bush_stage3.png` - 生长阶段3

### 4. 无限发电机 (InfiniteGeneratorBlock)
- `infinite_generator.png` - 全面贴图（cube_all风格）

### 5. 分子蒸馏塔底面 (MolecularDistillationTowerBlock) — 缺少底面
- `molecular_distillation_tower_bottom.png` - 底面贴图

## GUI贴图

> ⚠️ 以下三台机器的GUI目前使用的是**原版熔炉贴图**作为临时占位，需要绘制专属GUI贴图！

### 1. 烘干机GUI（临时：熔炉贴图）
- `dryer.png` - 烘干机GUI背景贴图
  - 路径: `assets/fuzhouplan/textures/gui/container/dryer.png`
  - 布局意图: 左侧输入槽(56,35) → 中央进度箭头(79,35) → 右侧主输出槽(110,35) + 副输出槽(130,35)，左侧能量条(13,20)

### 2. 解析器GUI（临时：熔炉贴图）
- `resolver.png` - 解析器GUI背景贴图
  - 路径: `assets/fuzhouplan/textures/gui/container/resolver.png`
  - 布局意图: 左侧染料输入槽 + DNA罐输入槽 → 中央进度箭头 → 右侧输出槽，左侧能量条

### 3. 分子蒸馏塔GUI（临时：熔炉贴图）
- `molecular_distillation_tower.png` - 分子蒸馏塔GUI背景贴图
  - 路径: `assets/fuzhouplan/textures/gui/container/molecular_distillation_tower.png`
  - 布局意图: 左侧输入槽(56,35) → 中央进度箭头(79,35) → 右侧输出槽(116,35)，左侧能量条(13,20)

### 4. 无限发电机GUI — 无GUI
- 无需GUI贴图（纯能量输出方块，无交互界面）

## 已完成的贴图（参考风格）

### 方块贴图
- 精密搅拌器 (precision_stirrer_*.png)
- 分子蒸馏塔 (molecular_distillation_tower_top/side.png)
- 发酵桶 (fermentation_barrel_*.png)
- 绿莓灌木 (green_berry_bush_stage*.png)

### GUI贴图
- 精密搅拌器GUI (precision_stirrer.png)
- 发酵桶GUI (fermentation_barrel.png)

---

请按照已有贴图的风格和分辨率进行绘制。GUI贴图尺寸建议 176×166 像素（标准容器尺寸）。
