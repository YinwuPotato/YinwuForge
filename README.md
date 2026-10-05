# YinwuForge — Yinwu锻造
# YinwuForge — Forge System

**最新版本：v1.2.3** | [下载 Release](https://github.com/YinwuPotato/YinwuForge/releases/tag/v1.2.3)

A Minecraft forge plugin for enhancing equipment through altars, materials, and potions.

一个为 **Folia / Paper 1.21+** 设计的 Minecraft 锻造插件，提供药水锻造与属性强化双系统。

> ⚡ 完全兼容 Folia 区域线程调度，无 NMS、无 unsafe 反射。

---

## Features | 功能概览

- **Attribute Enhancement** — 6 attributes (damage, attack speed, armor, toughness, mining speed, durability) — 5 as ADD_NUMBER modifiers, durability as max-damage bonus
- **Effective Value Display** — 锻造 lore 直接显示攻速/伤害实际值（原版 tooltip 显示修饰符原始值属客户端行为，已用 lore 补齐有效值）
- **Potion Forging** — Attach potion effects to equipment
- **Forge Altar** — Custom multi-layer 5x5 structure with second-layer bonus blocks
- **Concentrated Materials** — 30 materials in 6 categories (mineral/undead/farming strength cores, nether/end/challenge adjusters) + 1 potion material
- **Material Drops** — 击杀怪物 / 挖掘对应方块低概率额外掉落浓缩材料（无需 MythicMobs）
- **Diminishing Returns** — Forge count affects yield (1-5×1.5 / 6-10×1.0 / 11-20×0.7 / 21-40×0.4 / 41+×0.2)
- **YinwuEnchant Integration** — 锻造成功/极品时 60%/90% 概率附加自定义附魔（需安装 YinwuEnchant）

| 模块 | 说明 |
|------|------|
| 🧪 **药水锻造** | 为装备附加药水效果（速度、力量、急迫、抗火等） |
| ⚔️ **属性强化** | 强化装备属性（耐久、伤害、护甲、攻速等） |
| 🗿 **锻造祭坛** | 自定义多层结构祭坛，搭建后右键打开GUI锻造 |
| 🎛️ **GUI锻造** | 3槽位GUI：装备 + 强化材料 + 概率调整材料 |
| 📦 **材料体系** | 30种浓缩材料 + 6大分类 + 药水锻造材料，击杀/挖掘低概率掉落 |
| 📖 **材料一览** | `/yinwuforge gui` 查看全部材料的分类、功能与ID |
| 🎯 **概率调整** | 炼狱/末地/挑战核心，动态调整成功/损坏率 |
| 🌟 **光柱效果** | 祭坛激活后持续显示粒子光柱 |

---

## Quick Start | 快速开始

1. 将 `YinwuForge-1.2.3.jar` 放入 `plugins/` 目录
2. 重启服务器
3. 搭建**锻造祭坛**结构（见 §锻造祭坛结构）
4. 右键祭坛中心（锻造台）→ 打开**锻造GUI**
5. 放入装备 + 对应核心材料（可选放入概率调整材料）→ 点击锻造！

---

## 锻造祭坛结构

**第一层（底层，必须完整 5×5 才能激活）：**

```
B B B B B      B = 下界合金块（NETHERITE_BLOCK）
B A A A B      C = 锻造台（SMITHING_TABLE，正中心）
B A C A B      A = 占位符，不检测，可放任意方块
B A A A B
B B B B B
```

**第二层（可选，放在锻造台正上方一格 Y+1，形状相同，只有 B 位置生效）：**

每个 B 方块为锻造提供 **成功率 +2%、失败率 −1%**，最多计算 **16 个**（满配 = 成功率 +32%、失败率 −16%）。

> 两层结构与加成数值都在 `config.yml` 的 `altar:` 段可调：
> `center-block`（中心方块）、`base-blocks`（底座方块列表）、`structure-layers`（5×5 布局）、`layer-bonus`（加成）。
> 游戏内也可以让玩家执行 `/yinwuforge help` 看到祭坛搭建提示。

---

## Commands | 命令

| 命令 | 说明 | 权限 |
|------|------|------|
| `/yinwuforge`（别名 `/yf`） | 查看帮助 | `yinwu.forge.use` |
| `/yinwuforge reload` | 重载所有配置 | `yinwu.forge.admin` |
| `/yinwuforge gui` | 打开浓缩材料一览GUI | `yinwu.forge.use` |
| `/yinwuforge give <玩家> potion` | 获取药水锻造材料（锻造奇点） | `yinwu.forge.admin` |
| `/yinwuforge give <玩家> concentrated <id>` | 获取浓缩材料 | `yinwu.forge.admin` |

## Permissions | 权限

| 权限节点 | 说明 | 默认 |
|----------|------|------|
| `yinwu.forge.use` | 允许使用锻造功能 | 所有人 |
| `yinwu.forge.admin` | 管理员权限（重载/获取材料） | OP |

---

## Architecture | 架构

```
YinwuForge
├── api/
│   ├── ForgeAPIImpl           # ForgeAPI 服务实现（供其他 Yinwu 插件调用）
│   └── EnchantLink            # 与 YinwuEnchant 的反射桥接（可选联动）
├── manager/
│   ├── ForgeManager           # 锻造核心逻辑（概率/收益递减/属性应用）
│   ├── AltarManager           # 祭坛结构检测、第二层加成与光柱效果
│   ├── ForgeGUI               # 锻造GUI（3输入槽位 + 概率显示 + 预览循环）
│   ├── MaterialGUI            # 浓缩材料一览GUI（/yinwuforge gui）
│   ├── MaterialConfig         # 材料定义加载(material.yml)
│   ├── MaterialDropManager    # 浓缩材料掉落（击杀/挖掘低概率）
│   ├── ConfigManager          # 配置管理器
│   ├── PotionEffectManager    # 药水效果管理（穿戴应用）
│   ├── PotionForgeConfig      # 药水锻造配置
│   ├── AlloyForgeConfig       # 强化锻造配置
│   ├── BaseWeaponStats        # 基础属性数据（伤害/攻速/护甲基础值）
│   ├── AttributeUtil          # 属性修饰符工具
│   ├── EventListener          # 事件监听
│   └── CommandHandler         # 命令处理
├── model/
│   ├── EquipmentData          # 装备锻造数据（PDC 持久化）
│   ├── EquipmentAttributes    # 装备属性数据
│   ├── ForgeAttributes        # 属性常量与默认系数
│   ├── PotionEffectData       # 药水效果数据
│   └── ForgeResult            # 锻造结果枚举
└── YinwuForgePlugin           # 主类
```

---

## Build | 构建

依赖：**Java 21+**、**Maven 3.8+**

```bash
git clone https://github.com/YinwuPotato/YinwuForge.git
cd YinwuForge
mvn clean package
```

产出：`target/YinwuForge-1.2.3.jar`

> **前置步骤（首次构建必需）**：本插件依赖共享库 `YinwuPluginLib`，而它不在 Maven 中央仓库。
> 首次构建前先克隆并安装一次：
>
> ```bash
> git clone https://github.com/YinwuPotato/YinwuPluginLib.git
> cd YinwuPluginLib && mvn clean install
> ```
>
> 之后回到本仓库 `mvn clean package` 即可。父 POM（`net.yinwu:YinwuPlugins:1.0.1`）
> 已随仓库提供在 `parent/pom.xml`，无需额外操作。

---

## Dependencies | 依赖

- **[YinwuPluginLib](https://github.com/YinwuPotato/YinwuPluginLib)**（**构建期**前置 —— 构建产物已把该库 shade 进 jar，服务器上**不需要**单独安装它）
- **[Paper API 1.21+](https://papermc.io/)**（provided）
- **[YinwuEnchant](https://github.com/YinwuPotato/YinwuEnchant)**（可选，锻造成功/极品时联动附加自定义附魔）

---

## Configuration | 配置

```
plugins/YinwuForge/
├── config.yml          # 主配置（锻造白名单/概率/收益递减/祭坛结构/GUI槽位/属性系数/掉落获取）
└── material.yml        # 材料定义（物品ID、名称、分类、功能）
```

使用 `/yinwuforge reload` 热重载所有配置。

---

## Design Principles | 设计原则

- **Folia First** — 所有调度使用 `RegionScheduler` / `GlobalRegionScheduler` / `EntityScheduler`
- **零 NMS** — 仅依赖 Paper/Folia 公共 API
- **线程安全** — 共享状态使用 `ConcurrentHashMap`
- **Java 21** — 使用 records、switch expressions、pattern matching

---


## License | 许可证

LGPL-3.0 —— 见 [LICENSE](LICENSE)。

---

## Links | 链接

- 仓库：[github.com/YinwuPotato/YinwuForge](https://github.com/YinwuPotato/YinwuForge)
- 前置：[YinwuPluginLib](https://github.com/YinwuPotato/YinwuPluginLib)
- 关联：[YinwuRaid](https://github.com/YinwuPotato/YinwuRaid) | [YinwuEnchant](https://github.com/YinwuPotato/YinwuEnchant)
- 作者：Qumingjam（插件内 `plugin.yml` 声明为 `yinwu`）

---

## Changelog | 更新日志

### 未发版（v1.2.3 之后）
- 新增 `MaterialDropManager`：材料掉落管理（击杀怪物 / 挖掘对应方块低概率额外掉落浓缩材料，无需 MythicMobs）
- 同步更新命令处理与配置

### v1.2.3
- 命令统一、材料一览 GUI、ForgeGUI 美化
- 锻造 lore 显示攻速/伤害**有效值**（原版 tooltip 只显示修饰符原始值）
- 修复下界合金剑基础伤害

### v1.2.2
- lore 显示有效攻速/伤害
- 修复下界合金剑基础伤害

### v1.2.1
- 版本号更新
- README 完善

### v1.2.0
- Folia 调度全面适配
- onDisable() 新增任务取消和监听器解注册
- Java 21, Paper API 1.21+, Folia 兼容
