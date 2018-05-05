package com.alibaba.jstorm.kafka;


import backtype.storm.Config;
import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.utils.Utils;
import com.google.common.collect.ImmutableMap;
import com.hollyvoc.helper.redis.RedisHelper;
import kafka.javaapi.message.ByteBufferMessageSet;
import kafka.message.Message;
import kafka.message.MessageAndOffset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.*;

/**
 * 
 * @author feilaoda
 *
 */
public class PartitionConsumer {
    private static Logger LOG = LoggerFactory.getLogger(PartitionConsumer.class);

    static enum EmitState {
        EMIT_MORE, EMIT_END, EMIT_NONE
    }

    private int partition;
    private KafkaConsumer consumer;
   

    private PartitionCoordinator coordinator;

    private KafkaSpoutConfig config;
    private LinkedList<MessageAndOffset> emittingMessages = new LinkedList<MessageAndOffset>();
    private SortedSet<Long> pendingOffsets = new TreeSet<Long>();
    private SortedSet<Long> failedOffsets = new TreeSet<Long>();
    private long emittingOffset;
    private long lastCommittedOffset;
    private ZkState zkState;
    private Map stormConf;
    private  RedisHelper redis;

    public PartitionConsumer(Map conf, KafkaSpoutConfig config, int partition, ZkState offsetState) {
        this.stormConf = conf;
        this.config = config;
        this.partition = partition;
        this.consumer = new KafkaConsumer(config);
        this.zkState = offsetState;
        redis = RedisHelper.getInstance();
        Long jsonOffset = null;
        try {
            // 获取zk上topic的参数
            Map<Object, Object> json = offsetState.readJSON(zkPath());
            if (json != null) {
                // jsonTopologyId = (String)((Map<Object,Object>)json.get("topology"));
                jsonOffset = (Long) json.get("offset");
            }
        } catch (Throwable e) {
            LOG.warn("Error reading and/or parsing at ZkNode: " + zkPath(), e);
        }

        try {
            if (config.fromBeginning) {
                emittingOffset = consumer.getOffset(config.topic, partition, kafka.api.OffsetRequest.EarliestTime());
            } else {
                if (jsonOffset == null) {
                    lastCommittedOffset = consumer.getOffset(config.topic, partition, kafka.api.OffsetRequest.LatestTime());
                } else {
                    lastCommittedOffset = jsonOffset;
                }
                emittingOffset = lastCommittedOffset;
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public EmitState emit(SpoutOutputCollector collector) {
        if (emittingMessages.isEmpty()) {
            fillMessages();
        }

        int count = 0;
        while (true) {
            MessageAndOffset toEmitMsg = emittingMessages.pollFirst();
            if (toEmitMsg == null) {
                return EmitState.EMIT_END;
            }

            count ++;
            Iterable<List<Object>> tups = generateTuples(toEmitMsg.message());

            if (tups != null) {
                for (List<Object> tuple : tups) {
                    LOG.debug("emit message {}", new String(Utils.toByteArray(toEmitMsg.message().payload())));
                    collector.emit(tuple, new KafkaMessageId(partition, toEmitMsg.offset()));
                }
                if(count>=config.batchSendCount) {
                    break;
                }
            } else {
                ack(toEmitMsg.offset());
            }
        }

        if (emittingMessages.isEmpty()) {
            return EmitState.EMIT_END;
        } else {
            return EmitState.EMIT_MORE;
        }
    }

    /**
     * 扩展的方法， 指定streamId.
     * @param streamId streamId
     * @param collector collector
     * @return
     */
    public EmitState emit(String streamId, SpoutOutputCollector collector) {
        if (emittingMessages.isEmpty()) {
            fillMessages();
        }

        int count = 0;
        while (true) {
            MessageAndOffset toEmitMsg = emittingMessages.pollFirst();
            if (toEmitMsg == null) {
                return EmitState.EMIT_END;
            }

            count ++;
            Iterable<List<Object>> tups = generateTuples(toEmitMsg.message());

            if (tups != null) {
                for (List<Object> tuple : tups) {
                    LOG.debug("emit message {}", new String(Utils.toByteArray(toEmitMsg.message().payload())));
                    collector.emit(streamId,tuple, new KafkaMessageId(partition, toEmitMsg.offset()));
                }
                if(count>=config.batchSendCount) {
                    break;
                }
            } else {
                ack(toEmitMsg.offset());
            }
        }

        if (emittingMessages.isEmpty()) {
            return EmitState.EMIT_END;
        } else {
            return EmitState.EMIT_MORE;
        }
    }

    private void fillMessages() {

        ByteBufferMessageSet msgs;
        try {
            long start = System.currentTimeMillis();
            msgs = consumer.fetchMessages(partition, emittingOffset + 1);
            
            if (msgs == null) {
                LOG.error("fetch null message from offset {}", emittingOffset +" topic: " + config.topic);
                return;
            }
            
            int count = 0;
            for (MessageAndOffset msg : msgs) {
                count += 1;
                emittingMessages.add(msg);
                emittingOffset = msg.offset();
                pendingOffsets.add(emittingOffset);
                LOG.debug("fillmessage fetched a message:{}, offset:{}", msg.message().toString(), msg.offset());
            }
            long end = System.currentTimeMillis();
            System.out.println("fetch message from partition:"+partition+", offset:" + emittingOffset+", size:"+msgs.sizeInBytes()+", count:"+count +", time:"+(end-start));
            LOG.info("fetch message from partition:"+partition+", offset:" + emittingOffset+", size:"+msgs.sizeInBytes()+", count:"+count +", time:"+(end-start));
            try {
                Thread.sleep(500);
            }catch (Exception e) {
                LOG.error(" sleep fail!" , e);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error(e.getMessage(),e);
        }
    }

    public void commitState() {
        try {
            long lastOffset = 0;

            if (pendingOffsets.isEmpty() || pendingOffsets.size() <= 0) {
                lastOffset = emittingOffset;
            } else {
                lastOffset = pendingOffsets.first();
            }
            if (lastOffset != lastCommittedOffset) {
                Map<Object, Object> data = new HashMap<Object, Object>();
                data.put("topology", stormConf.get(Config.TOPOLOGY_NAME));
                data.put("offset", lastOffset);
                data.put("partition", partition);
                data.put("broker", ImmutableMap.of("host", consumer.getLeaderBroker().host(), "port", consumer.getLeaderBroker().port()));
                data.put("topic", config.topic);
                data.put("group.id",config.groupId);


                zkState.writeJSON(zkPath(), data);
                // todo 将offset存放到redis中.
                redis.add2Hash(config.redisName, config.topic+"#"+config.groupId+"#"+stormConf.get(Config.TOPOLOGY_NAME)+"#"+partition+ "#"+ config.clientId,Long.toString(lastOffset));
                redis.add2Hash(config.redisName, config.topic+"#"+config.clientId,data.toString());

                lastCommittedOffset = lastOffset;
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }

    }

    public void ack(long offset) {
        try {
            pendingOffsets.remove(offset);
        } catch (Exception e) {
            LOG.error("offset ack error " + offset);
        }
    }

    public void fail(long offset) {
        failedOffsets.remove(offset);
    }

    public void close() {
        coordinator.removeConsumer(partition);
        consumer.close();
    }

    @SuppressWarnings("unchecked")
    public Iterable<List<Object>> generateTuples(Message msg) {
        Iterable<List<Object>> tups = null;
        ByteBuffer payload = msg.payload();
        ByteBuffer key = msg.key();
        if (payload == null) {
            return null;
        }
        tups = Arrays.asList(Utils.tuple(Utils.toByteArray(key),Utils.toByteArray(payload)));
        return tups;
    }

    private String zkPath() {
        return config.zkRoot + "/kafka/offset/topic/" + config.topic + "/" + config.clientId + "/" + partition;
    }

    public PartitionCoordinator getCoordinator() {
        return coordinator;
    }

    public void setCoordinator(PartitionCoordinator coordinator) {
        this.coordinator = coordinator;
    }

    public int getPartition() {
        return partition;
    }

    public void setPartition(int partition) {
        this.partition = partition;
    }

    public KafkaConsumer getConsumer() {
        return consumer;
    }

    public void setConsumer(KafkaConsumer consumer) {
        this.consumer = consumer;
    }
}
