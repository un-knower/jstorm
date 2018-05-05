package com.hollycrm.hollyvoc.batch;

import backtype.storm.topology.BoltDeclarer;
import backtype.storm.topology.TopologyBuilder;
import com.alibaba.jstorm.batch.BatchTopologyBuilder;

import static com.hollycrm.hollyvoc.constant.TopoConstant.TOPOLOGY_STREAM_TXT_BACTH_ID;

/**
 * Created by qianxm on 2017/7/17.
 * 批量处理数据拓扑结构
 */
public class BatchTopology {
    public final static String TOPOLOGY_NAME = "record-batch-topology";
    /**
     * 定义拓扑
     * @param builder 拓扑对象
     */
    public static TopologyBuilder defineTopology(BatchTopologyBuilder builder){
        System.out.println(" defineTopology .....");

        BoltDeclarer boltDeclarer = builder.setSpout(TxtBatchSpout.NAME, new TxtBatchSpout(), 1);
        // componentId, streamId 两个id都要有，否则会保存
        builder.setBolt(HbaseBatchBolt.NAME,new HbaseBatchBolt(),2).shuffleGrouping(TxtBatchSpout.NAME,TOPOLOGY_STREAM_TXT_BACTH_ID);
        return builder.getTopologyBuilder();
    }

}
