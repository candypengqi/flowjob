/*
 *
 *  * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * 	http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.limbo.flowjob.broker.application.component.schedule;

import lombok.Setter;
import org.limbo.flowjob.broker.core.domain.IDGenerator;
import org.limbo.flowjob.broker.core.domain.IDType;
import org.limbo.flowjob.broker.core.domain.job.JobInfo;
import org.limbo.flowjob.broker.core.domain.job.JobInstance;
import org.limbo.flowjob.broker.core.domain.job.SingleJobInstance;
import org.limbo.flowjob.broker.core.domain.job.WorkflowJobInfo;
import org.limbo.flowjob.broker.core.domain.job.WorkflowJobInstance;
import org.limbo.flowjob.broker.dao.converter.DomainConverter;
import org.limbo.flowjob.broker.dao.entity.JobInstanceEntity;
import org.limbo.flowjob.broker.dao.entity.PlanInfoEntity;
import org.limbo.flowjob.broker.dao.repositories.JobInstanceEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.PlanInfoEntityRepo;
import org.limbo.flowjob.common.constants.JobStatus;
import org.limbo.flowjob.common.constants.MsgConstants;
import org.limbo.flowjob.common.constants.PlanType;
import org.limbo.flowjob.common.exception.VerifyException;
import org.limbo.flowjob.common.utils.attribute.Attributes;
import org.limbo.flowjob.common.utils.dag.DAG;
import org.limbo.flowjob.common.utils.json.JacksonUtils;
import org.limbo.flowjob.common.utils.time.TimeUtils;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.LocalDateTime;

/**
 * @author Devil
 * @since 2023/2/9
 */
@Component
public class JobInstanceHelper {

    @Setter(onMethod_ = @Inject)
    private JobInstanceEntityRepo jobInstanceEntityRepo;

    @Setter(onMethod_ = @Inject)
    private PlanInfoEntityRepo planInfoEntityRepo;

    @Setter(onMethod_ = @Inject)
    private IDGenerator idGenerator;

    public JobInstance getJobInstance(String id) {
        JobInstanceEntity entity = jobInstanceEntityRepo.findById(id)
                .orElseThrow(VerifyException.supplier(MsgConstants.CANT_FIND_JOB_INSTANCE + id));
        PlanInfoEntity planInfoEntity = planInfoEntityRepo.findById(entity.getPlanInfoId())
                .orElseThrow(VerifyException.supplier(MsgConstants.CANT_FIND_PLAN_INSTANCE + entity.getPlanId()));

        JobInstance jobInstance;
        PlanType planType = PlanType.parse(planInfoEntity.getPlanType());
        if (PlanType.SINGLE == planType) {
            JobInfo jobInfo = JacksonUtils.parseObject(planInfoEntity.getJobInfo(), JobInfo.class);
            jobInstance = newSingleJobInstance(entity.getPlanId(), entity.getPlanInfoId(), entity.getPlanInstanceId(),
                    new Attributes(entity.getContext()), jobInfo, entity.getTriggerAt());
        } else if (PlanType.WORKFLOW == planType) {
            DAG<WorkflowJobInfo> dag = DomainConverter.toJobDag(planInfoEntity.getJobInfo());
            WorkflowJobInfo workflowJobInfo = dag.getNode(entity.getJobId());
            jobInstance = newWorkflowJobInstance(entity.getPlanId(), entity.getPlanInfoId(), entity.getPlanInstanceId(),
                    new Attributes(entity.getContext()), workflowJobInfo, entity.getTriggerAt());

        } else {
            throw new IllegalArgumentException("Illegal PlanType in plan:" + planInfoEntity.getPlanId() + " version:" + planInfoEntity.getPlanInfoId());
        }
        jobInstance.setJobInstanceId(entity.getJobInstanceId());
        jobInstance.setStatus(JobStatus.parse(entity.getStatus()));
        jobInstance.setStartAt(entity.getStartAt());
        jobInstance.setEndAt(entity.getEndAt());
        return jobInstance;
    }

    /**
     * 设置为 retry 状态
     */
    public void retryReset(JobInstance jobInstance, Integer retryInterval) {
        jobInstance.setTriggerAt(TimeUtils.currentLocalDateTime().plusSeconds(retryInterval));
        jobInstance.setJobInstanceId(idGenerator.generateId(IDType.JOB_INSTANCE));
        jobInstance.setStatus(JobStatus.SCHEDULING);
    }

    public JobInstance newSingleJobInstance(String planId, String planVersion, String planInstanceId,
                                              Attributes context, JobInfo jobInfo, LocalDateTime triggerAt) {
        String jobInstanceId = idGenerator.generateId(IDType.JOB_INSTANCE);
        SingleJobInstance instance = new SingleJobInstance();
        instance.setJobInstanceId(jobInstanceId);
        instance.setJobInfo(jobInfo);
        instance.setPlanType(PlanType.SINGLE);
        wrapJobInstance(instance, planId, planVersion, planInstanceId, context, jobInfo.getAttributes(), triggerAt);
        return instance;
    }

    public JobInstance newWorkflowJobInstance(String planId, String planVersion, String planInstanceId,
                                              Attributes context, WorkflowJobInfo workflowJobInfo, LocalDateTime triggerAt) {
        JobInfo job = workflowJobInfo.getJob();
        String jobInstanceId = idGenerator.generateId(IDType.JOB_INSTANCE);
        WorkflowJobInstance instance = new WorkflowJobInstance();
        instance.setJobInstanceId(jobInstanceId);
        instance.setWorkflowJobInfo(workflowJobInfo);
        instance.setTerminateWithFail(workflowJobInfo.isTerminateWithFail());
        instance.setPlanType(PlanType.WORKFLOW);
        wrapJobInstance(instance, planId, planVersion, planInstanceId, context, job.getAttributes(), triggerAt);
        return instance;
    }

    private void wrapJobInstance(JobInstance instance, String planId, String planVersion, String planInstanceId,
                                 Attributes context, Attributes jobAttributes, LocalDateTime triggerAt) {
        instance.setPlanId(planId);
        instance.setPlanInstanceId(planInstanceId);
        instance.setPlanVersion(planVersion);
        instance.setStatus(JobStatus.SCHEDULING);
        instance.setTriggerAt(triggerAt);
        instance.setContext(context == null ? new Attributes() : context);
        instance.setJobAttributes(jobAttributes == null ? new Attributes() : jobAttributes);
    }
}
