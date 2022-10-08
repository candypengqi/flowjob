/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.limbo.flowjob.broker.application.plan.config;

import lombok.Setter;
import org.flywaydb.core.Flyway;
import org.limbo.flowjob.broker.application.plan.component.JobStatusCheckTask;
import org.limbo.flowjob.broker.application.plan.component.PlanScheduleTask;
import org.limbo.flowjob.broker.application.plan.component.TaskStatusCheckTask;
import org.limbo.flowjob.broker.application.plan.support.NodeMangerImpl;
import org.limbo.flowjob.broker.core.cluster.Broker;
import org.limbo.flowjob.broker.core.cluster.BrokerRegistry;
import org.limbo.flowjob.broker.core.cluster.NodeManger;
import org.limbo.flowjob.broker.core.cluster.WorkerManager;
import org.limbo.flowjob.broker.core.cluster.WorkerManagerImpl;
import org.limbo.flowjob.broker.core.dispatcher.strategies.RoundRobinWorkerSelector;
import org.limbo.flowjob.broker.core.domain.task.TaskDispatcher;
import org.limbo.flowjob.broker.core.domain.task.TaskFactory;
import org.limbo.flowjob.broker.core.schedule.calculator.SimpleScheduleCalculatorFactory;
import org.limbo.flowjob.broker.core.worker.WorkerRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import javax.inject.Inject;
import java.util.Timer;

/**
 * @author Brozen
 * @since 2021-06-01
 */
@EnableConfigurationProperties({BrokerProperties.class})
public class BrokerConfiguration {

    @Setter(onMethod_ = @Inject)
    private BrokerProperties brokerProperties;

    @Setter(onMethod_ = @Inject)
    private PlanScheduleTask planScheduleTask;

    @Setter(onMethod_ = @Inject)
    private TaskStatusCheckTask taskStatusCheckTask;

    @Setter(onMethod_ = @Inject)
    private JobStatusCheckTask jobStatusCheckTask;

    @Setter(onMethod_ = @Inject)
    private BrokerRegistry brokerRegistry;

    @Bean
    public NodeManger brokerManger() {
        return new NodeMangerImpl();
    }

    /**
     * worker 管理，持久化等
     */
    @Bean
    public Broker brokerNode(NodeManger nodeManger) {
        return new Broker(brokerProperties, brokerRegistry, nodeManger) {
            @Override
            public void stop() {

            }

            @Override
            public boolean isRunning() {
                return false;
            }

            @Override
            public boolean isStopped() {
                return false;
            }

            @Override
            public void start() {
                super.start();

                // 下发任务task
                new Timer().schedule(planScheduleTask, 3000, brokerProperties.getRebalanceInterval());

                // 状态检查task
                new Timer().schedule(taskStatusCheckTask, 3000, brokerProperties.getStatusCheckInterval());

                // 状态检查task
                new Timer().schedule(jobStatusCheckTask, 3000, brokerProperties.getStatusCheckInterval());
            }
        };
    }

    /**
     * worker 管理，持久化等
     */
    @Bean
    public WorkerManager workerManager(WorkerRepository workerRepository) {
        return new WorkerManagerImpl(workerRepository);
    }


    /**
     * 调度时间计算器
     */
    @Bean
    public SimpleScheduleCalculatorFactory scheduleCalculatorFactory() {
        return new SimpleScheduleCalculatorFactory();
    }


    /**
     * Worker负载均衡：轮询
     */
    @Bean
    public RoundRobinWorkerSelector roundRobinWorkerSelector() {
        return new RoundRobinWorkerSelector();
    }

    @Bean
    public TaskFactory taskFactory(WorkerManager workerManager) {
        return new TaskFactory(workerManager);
    }

    @Bean
    public TaskDispatcher taskDispatcher(WorkerManager workerManager) {
        return new TaskDispatcher(workerManager);
    }

}
