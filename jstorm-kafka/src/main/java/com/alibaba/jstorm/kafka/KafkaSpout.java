package com.alibaba.jstorm.kafka;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichSpout;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import com.alibaba.jstorm.kafka.PartitionConsumer.EmitState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;

import static com.alibaba.jstorm.kafka.KafkaConstant.KEY;
import static com.alibaba.jstorm.kafka.KafkaConstant.VALUE;

public class KafkaSpout implements IRichSpout {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static Logger LOG = LoggerFactory.getLogger(KafkaSpout.class);

	protected SpoutOutputCollector collector;
	
	private long lastUpdateMs;
	PartitionCoordinator coordinator;
	
	private KafkaSpoutConfig config;
	
	private ZkState zkState;
	private String streamId;
	public KafkaSpout() {
	    
	}
	
	public KafkaSpout(KafkaSpoutConfig config, String streamId) {
		this.config = config;
		this.streamId = streamId;
	}
	public KafkaSpout(KafkaSpoutConfig config) {
		this.config = config;
	}
	@Override
	public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
		this.collector = collector;
		if (this.config == null) {
			config = new KafkaSpoutConfig();
			config.configure(conf);
		}
		zkState = new ZkState(conf, config);
		coordinator = new PartitionCoordinator(conf, config, context, zkState);
		lastUpdateMs = System.currentTimeMillis();
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
	    zkState.close();
	}

	@Override
	public void activate() {
		// TODO Auto-generated method stub

	}

	@Override
	public void deactivate() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void nextTuple() {
		Collection<PartitionConsumer> partitionConsumers = coordinator.getPartitionConsumers();
		for(PartitionConsumer consumer: partitionConsumers) {
			EmitState state = consumer.emit(streamId, collector);
			LOG.debug("====== partition "+ consumer.getPartition() + " emit message state is "+state);
//			if(state != EmitState.EMIT_MORE) {
//				currentPartitionIndex  = (currentPartitionIndex+1) % consumerSize;
//			}
//			if(state != EmitState.EMIT_NONE) {
//				break;
//			}
		}

		long now = System.currentTimeMillis();
        if((now - lastUpdateMs) > config.offsetUpdateIntervalMs) {
            commitState();
        }
        // 每2秒发送一条数据
//        try {
//			Thread.sleep(2000);
//		}catch (Exception e) {
//        	LOG.error(" sleep fail!" , e);
//		}
		
	}
	
	public void commitState() {
	    lastUpdateMs = System.currentTimeMillis();
		for(PartitionConsumer consumer: coordinator.getPartitionConsumers()) {
			consumer.commitState();
        }
		
	}

	@Override
	public void ack(Object msgId) {
		KafkaMessageId messageId = (KafkaMessageId)msgId;
		PartitionConsumer consumer = coordinator.getConsumer(messageId.getPartition());
		consumer.ack(messageId.getOffset());
	}

	@Override
	public void fail(Object msgId) {
		KafkaMessageId messageId = (KafkaMessageId)msgId;
		PartitionConsumer consumer = coordinator.getConsumer(messageId.getPartition());
		consumer.fail(messageId.getOffset());
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declareStream(streamId, new Fields(KEY,VALUE));
//		declarer.declare(new Fields(KEY,VALUE));
	}

	@Override
	public Map<String, Object> getComponentConfiguration() {
		return null;
	}
	
	

}
