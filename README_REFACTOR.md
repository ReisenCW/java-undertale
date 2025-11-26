# java-undertale — Composite 模式

## 已完成
- GameObject 与 UI 的组合模式 POC 已实现并通过测试
- 粒子/特效（例如 `RippleEffect`、`TitanSpawnParticle`）迁移为继承 `GameObject`
- 子弹（bullets）迁移完成：活动子弹从 `bullets` 列表转为以 `bulletsLayer`（组合层）为权威管理
- `ObjectManager` 更新：创建了 `root/bulletsLayer/collectablesLayer/effectsLayer`，允许以层为单位控制更新与渲染
- 渲染路径更新：`renderFightScene` 使用层的 `render()`，对象自行渲染
- 保留并标注旧的 `BulletRenderer` / `CollectableRenderer` 为弃用以备参考或未来作为可插拔后端
