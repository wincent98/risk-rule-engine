# risk-rule-engine

交易风控规则判定器。给一笔交易打分，输出 APPROVE / REVIEW / REJECT，并记录命中的规则。

## 结构

| 类 | 说明 |
|----|------|
| `RiskContext` | 一笔待评估交易。除了 16 个输入字段，还带一块 scratch，有几条规则往里写中间值供后面的规则读 |
| `RiskEvaluator` | `evaluate` 是唯一入口，32 条规则按固定顺序求值，命中 REJECT 级规则立即返回 |
| `RiskResult` | decision、hitRuleIds（按命中顺序）、score |
| `SampleFactory` | 确定性样本生成，黄金样本集和基准共用 |
| `RiskBenchmark` | 吞吐基准 |

## 构建与运行

```bash
mvn -o package
java -jar target/risk-rule-engine-3.2.0.jar   # 吞吐基准
mvn -o test
```

## 黄金样本

`src/test/resources/golden/samples.tsv` 有 200 条样本，每行前 16 列是输入，后 3 列是期望的
decision、hitRuleIds、score。`GoldenSampleTest` 逐条比对，任何改动都必须保持这 200 条输出不变。

## 已知痛点

- `evaluate` 是一个长方法，规则之间的优先级、短路和 scratch 依赖全靠书写顺序维持，加规则风险高。
- 规则无法单独测试，也没有运行时启停的入口。
