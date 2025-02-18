# 简介

**flowjob**主要用于搭建统一的任务调度平台，方便各个业务方进行接入使用。
项目在设计的时候，考虑了扩展性、稳定性、伸缩性等相关问题，可以作为公司的任务调度中间件被使用。

## 功能介绍

**flowjob**主要分为以下几个部分：

* Broker：中心节点，负责任务的调度。
* Worker：工作节点，主要负责任务的具体执行。
* Console：通过Broker提供的Api，进行任务创建/更新等一些管控操作。
* Registry：注册中心，目前使用DB做为注册中心。提供了对应接口，可以基于其它组件如zk、nacos等进行灵活的封装。
* Datasource：数据库用于持久化运行数据

<div align="center">
<img src="docs/flowjob-framework.png" alt="framework" title="framework"/>
</div>

### 调度类型

* 固定速度：作业创建后，每次调度下发后，间隔固定时间长度后，再次触发作业调度。
* 固定延迟：作业创建后，每次作业下发执行完成（成功或失败）后，间隔固定时间长度后，再次触发作业调度。
* CRON：通过CRON表达式指定作业触发调度的时间点。

### 负载策略

* 随机：将作业随机下发给某一个worker执行。
* 轮询：将任务逐个分配给worker。
* 最不经常使用：将作业下发给一个时间窗口内，接收作业最少的worker。
* 最近最少使用：将作业下发给一个时间窗口内，最长时间没有接受worker的worker。
* 一致性hash：同样参数的作业将始终下发给同一台机器。
* 指定节点：让作业指定下发到某个worker执行。

### 节点过滤方式

1. 执行器：任务只会下发给包含任务对应执行器的worker。
2. 标签：任务只会下发给包含指定标签的worker。
3. 容量：基于worker的任务队列/CPU/内存使用情况过滤（TODO）

### 任务触发方式

* API：通过指定api触发任务执行。
* 调度：Broker自动组装数据，调度对应的任务。

### 任务类型

任务可以为单任务/工作流。单任务可以理解为只有一个节点的工作流任务。

* 普通：对应某个执行器，执行结束任务结束。
* 广播：在下发的时间点，对每个可下发的节点下发任务，所以子任务执行完成当前任务才执行完成。
* Map：分为split和map两个步骤。split的时候进行任务拆分，map则对每个拆分任务进行执行。
* MapReduce：相比于Map多了Reduce过程，可以对所有Map任务的执行结果进行一个汇总。

## 参与贡献

如果你对本项目有任何建议或想加入我们的，可以通过下面方式：，欢迎提交 issues 进行指正。
- 报告 issue: [github issues](https://github.com/limbo-world/flowjob/issues)
- 提交PR：[github PR](https://github.com/limbo-world/flowjob/pulls)
- 加入我们：ysodevilo@gmail.com
