package org.limbo.flowjob.broker.core.plan.job.handler;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Devil
 * @since 2021/8/24
 */
@Slf4j
public class LogJobFailHandler implements JobFailHandler {

    @Override
    public void handle() {

    }

    @Override
    public boolean terminate() {
        return false;
    }

}
