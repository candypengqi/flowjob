package org.limbo.flowjob.broker.core.repository;

import org.limbo.flowjob.broker.core.plan.PlanInstance;

/**
 * @author Devil
 * @since 2021/7/24
 */
public interface PlanInstanceRepository {

    /**
     * 持久化
     */
    String add(PlanInstance instance);


    /**
     * 获取
     */
    PlanInstance get(String planInstanceId);

    /**
     * 获取
     */
    PlanInstance get(String planId, long expectTriggerTime);

    /**
     * 计划执行成功
     */
    void executeSucceed(PlanInstance instance);

    /**
     * 计划执行失败
     */
    void executeFailed(PlanInstance instance);

}
