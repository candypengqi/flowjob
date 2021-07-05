package org.limbo.flowjob.tracker.dao.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * @author Brozen
 * @since 2021-06-02
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("worker_statistics")
public class WorkerStatisticsPO extends PO {

    private static final long serialVersionUID = 4463926711851672545L;

    /**
     * worker节点ID
     */
    @TableId(type = IdType.INPUT)
    private String workerId;

    /**
     * 作业下发到此worker的次数
     */
    private Long jobDispatchCount;

    /**
     * 最后一次向此worker下发作业成功的时间
     */
    private LocalDateTime latestDispatchTime;

}
