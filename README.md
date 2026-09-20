# risk-rule-engine

交易风控规则判定器。给一笔交易打分，输出 APPROVE / REVIEW / REJECT，并记录命中的规则。

## 结构

| 类 | 说明 |
|----|------|
| `RiskContext` | 一笔待评估交易。除了 16 个输入字段，还带一块 scratch，有几条规则往里写中间值供后面的规则读 |
| `RiskRule` | 单条规则的接口：`id()`、显式 `priority()`（小者先执行）、`provides()`/`requires()` 声明 scratch 依赖、`apply()` 返回 PASS / HIT / REJECT |
| `rules.R01..R32` | 32 条内置规则，每条一个类，可独立定义、独立测试 |
| `rules.DefaultRules` | 内置规则集的装配点，纯代码组装，无反射无注解扫描 |
| `RuleRegistry` | 注册表。按 priority 排序（与注册顺序无关），校验 scratch 依赖，运行时 `setEnabled` 启停单条规则；每次变更发布一个不可变快照 + 一个 64 位启用掩码（volatile 单次读，进行中的评估不受影响） |
| `RiskEvaluator` | `evaluate` 是唯一入口。命中 REJECT 级规则立即短路。默认规则集走 `DefaultPipeline` 快路径，自定义规则集走通用规则循环，两者行为由 `EngineEquivalenceTest` 锁死 |
| `rules.DefaultPipeline` | 默认规则集的手工内联编译形态，每条规则一个位掩码保护；存在只是为了吞吐，逻辑必须与规则对象保持一致 |
| `RiskResult` | decision、hitRuleIds（按命中顺序）、score |
| `SampleFactory` | 确定性样本生成，黄金样本集和基准共用 |
| `RiskBenchmark` | 吞吐基准 |

## 加一条规则

1. 在 `com.fta.risk.rules` 下新建一个 `RiskRule` 实现，给定唯一 id 和 priority（现有规则按
   编号 × 10 占位，中间留了空档）。
2. 在 `DefaultRules.all()` 里注册。
3. 在 `DefaultPipeline` 里补一个对应的掩码保护块（漏掉不会出错：`createIfMatches` 会发现
   规则集对不上，自动回退到通用循环，只是吞吐下降）。
4. 如果规则读写 scratch 中间值，用 `provides()`/`requires()` 声明；注册表会在装配和启停时
   校验依赖，缺提供方直接报错而不是悄悄算错。

## 构建与运行

```bash
mvn -o package
java -jar target/risk-rule-engine-3.2.0.jar   # 吞吐基准
mvn -o test
```

## 黄金样本

`src/test/resources/golden/samples.tsv` 有 200 条样本，每行前 16 列是输入，后 3 列是期望的
decision、hitRuleIds、score。`GoldenSampleTest` 逐条比对，任何改动都必须保持这 200 条输出不变。

## 运行时启停

```java
RiskEvaluator evaluator = new RiskEvaluator();
evaluator.setRuleEnabled("R07", false);   // 之后开始的评估不再执行 R07
evaluator.isRuleEnabled("R07");           // false
evaluator.setRuleEnabled("R07", true);    // 恢复
```

启停只影响调用返回之后开始的评估；进行中的评估用它开始时那一刻的规则集合跑完。
禁用某条规则的提供方（如 R03 提供 `adjustedAmount`）而其消费方（R14、R22）仍启用时，
`setRuleEnabled` 会抛出 `IllegalStateException` 并回滚，不会发布一个依赖不成立的规则集。
