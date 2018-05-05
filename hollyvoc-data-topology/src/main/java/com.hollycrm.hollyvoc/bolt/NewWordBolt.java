package com.hollycrm.hollyvoc.bolt;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ConsumerConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.hollycrm.hollyvoc.api.newword.INewWord;
import com.hollycrm.hollyvoc.api.newword.bean.Result;
import com.hollycrm.hollyvoc.constant.TopoConstant;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.hollycrm.hollyvoc.constant.TopoConstant.*;
import static com.hollycrm.util.config.ConfigUtils.getStrVal;
import static com.hollycrm.hollyvoc.constant.Constant.DELIMITER_FIELDS;
import static com.hollycrm.hollyvoc.constant.Constant.WORD_DELIMITER;

/**
 * Created by qianxm on 2017/8/17.
 * 新词发现
 *
 */
public class NewWordBolt implements IRichBolt{

    public final static String NAME = "newWord-bolt";

    private static Logger logger = LoggerFactory.getLogger(NewWordBolt.class);

    private  ReferenceConfig<INewWord> referenceConfig;

//    private ConcurrentLinkedQueue<String> contents;
    private ConcurrentHashMap<String,List<String>> resource;

    private OutputCollector collector;
    private ConcurrentHashMap<String,Long> timeMap;
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private AtomicInteger hbcounter = new AtomicInteger(0);

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {

//        contents = new ConcurrentLinkedQueue<>();
        resource = new ConcurrentHashMap<>();
        this.collector = collector;
        timeMap = new ConcurrentHashMap<>();
        // dubbo 配置
        ApplicationConfig config = new ApplicationConfig();
        config.setName(getStrVal("dubbo.application"));

        RegistryConfig registryConfig = new RegistryConfig(getStrVal("dubbo.zk.host"));
        registryConfig.setProtocol(DUBBO_PROTOCOL);

        ConsumerConfig consumerConfig = new ConsumerConfig();
        consumerConfig.setRegistry(registryConfig);
        consumerConfig.setApplication(config);
        consumerConfig.setTimeout(3000); // 设置超时时间

        referenceConfig = new ReferenceConfig<>();
        referenceConfig.setCheck(false);
        referenceConfig.setVersion(getStrVal("dubbo.version")); // 版本
        referenceConfig.setInterface(INewWord.class);
        referenceConfig.setConsumer(consumerConfig);

//        INewWord iNewWord = referenceConfig.get();

    }

    @Override
    public void execute(Tuple input) {
        try{
            switch (input.getSourceStreamId()) {
                case TOPOLOGY_STREAM_HBASE_ID:
                    hbcounter.incrementAndGet();
                    // 正常流程处理
                    findNewWord(input,collector,resource,referenceConfig);
                    timeMap.put(Thread.currentThread().getName(), System.currentTimeMillis());
                    break;
                case  TOPOLOGY_STREAM_NW_ERR_ID:
//                    emitcounter.incrementAndGet();

                    // 异常处理，将获取到的数据文本直接进行新词发现
                    errFindNewWord(input, collector, referenceConfig);
                    break;
                default:
                    // 批量超时的情况,线程情况
                   timeMap.forEach((k,v)->{
                        // 超时的数据进行新词发现
                       if (((System.currentTimeMillis() - v) / 1000) > TIME_OUT && resource.size()>0) { // 超过1秒没有接受到数据，需要提交。
                            logger.debug(" acppet : " + hbcounter);
                           ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
                           List<String> data = resource.get(k);
                           System.out.println(k + data.size());
                           logger.debug("超时 "+k + " data: " + data.size());
                           boolean isLock = writeLock.tryLock();
                           // 获取锁并且有数据在可以提交
                           while(isLock) {
                               System.out.println(k + " 添加写锁 ");
                               // 有数据才提交
                               if (data.size() > 0) {
                                   List<String> temp = new ArrayList<>();
                                   temp.addAll(data);
                                   data.clear();
                                   resource.remove(k);
                                   writeLock.unlock();
                                   logger.debug(k + "new word lock-- data szie:" + temp.size());
                                   String ens = "";
                                   try {
                                       INewWord iNewWord = referenceConfig.get();
                                       // 获取新词
                                       ens = StringUtils.join(temp, "\n");
                                       // 最多返回50个词
                                       Result result = iNewWord.findNewWord(ens, 50, true);
                                       if (!result.isSuccess()) {
//                                           System.out.println("服务端出错了，请发送短信给维护人员,短信内容:" + result.getContent());
                                           // 将异常消息发送给kafka
                                           collector.emit(TOPOLOGY_STREAM_NW_ERR_ID, new Values(ens));
                                           return;
                                       }
                                       String nws = result.getContent();

                                       // 多个词发送给下游 词和词性 多个词用#分割 ，/后面的是词性，保存到redis单条发送
                                       // 如果学习到了新词，需要保存
                                       if (!StringUtils.isEmpty(nws)) {
                                           emitWord(nws, collector);
                                       }
                                       logger.info(k + " emit:  " + temp.size());
                                       temp.clear();
                                   }catch (Exception e) {
                                       logger.error(" 请求异常");
                                       // 请求异常时需要将异常数据发送给kafka
                                       if(!StringUtils.isEmpty(ens)){
                                           collector.emit(TOPOLOGY_STREAM_NW_ERR_ID, new Values(ens));
                                       }
                                   }
                               } else {
                                   writeLock.unlock();
                               }
                               isLock = false;
                           }
                           // 更新时间
                           timeMap.put(k, System.currentTimeMillis());
                       }
                   });
                    break;
            }
            collector.ack(input);
        } catch (Exception e) {
            logger.error(" newWord error! ", e);
            collector.fail(input);
        }

    }

    @Override
    public void cleanup() {

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declareStream(TOPOLOGY_STREAM_NW_ID, new Fields(DEC_WORD, DEC_FREQ));
        // 发现新词事时异常
        declarer.declareStream(TOPOLOGY_STREAM_NW_ERR_ID,new Fields(DEC_ALL_TXT));

    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }

    /**
     * 正常数据新词发信.
     * @param input 正常数据
     * @param collector collector
     * @param resource 缓存数据
     * @param referenceConfig dubbo链接配置
     */
    private static void findNewWord(Tuple input, OutputCollector collector,
                                    ConcurrentHashMap<String,List<String>> resource, ReferenceConfig<INewWord> referenceConfig) {
        // 对allTxt字段进行新词学习 TODO 是否考虑省份，每个省份最多可以学习多少个词？
        logger.debug("==========orgin find nws=============");
        String allTxt = input.getStringByField(TopoConstant.DEC_ALL_TXT);
        String threadName = Thread.currentThread().getName();
        resource.computeIfAbsent(threadName, list-> new ArrayList<>(ORACLE_BATCHSIZE));
        List<String> contents = resource.get(threadName);
        // 每50条学习一次。
        contents.add(allTxt);

        if(contents.size()==CONTENT_BATCH_NUM){
            // 调用新词接口
            String ens="";
            try {
                INewWord iNewWord = referenceConfig.get();
                logger.debug(" new word txtsize " + contents.size());
                List<String> temp = new ArrayList<>(contents.size());
                temp.addAll(contents);
                contents.clear();
                ens = StringUtils.join(temp, "\n");
                // 最多返回50个词
                Result result = iNewWord.findNewWord(ens, 50, true);
                if (!result.isSuccess()) {
//                    System.out.println("服务端出错了，请发送短信给维护人员,短信内容:" + result.getContent());
                    logger.error("服务端出错了，请发送短信给维护人员,短信内容:", result.getContent());
                    // 将异常消息发送给kafka
                    collector.emit(TOPOLOGY_STREAM_NW_ERR_ID, new Values(ens));
                    return;
                }
                String nws = result.getContent();

                // 多个词发送给下游 词和词性 多个词用#分割 ，/后面的是词性，保存到redis单条发送
                // 如果学习到了新词，需要保存
                if(!StringUtils.isEmpty(nws)){
                    emitWord(nws,collector);

                }
            }catch (Exception e) {
                logger.error(" 请求异常");
                // 请求异常时需要将异常数据发送给kafka
                if(!StringUtils.isEmpty(ens)){
                    collector.emit(TOPOLOGY_STREAM_NW_ERR_ID, new Values(ens));
                }

            }

        }
    }

    private static void errFindNewWord(Tuple input, OutputCollector collector, ReferenceConfig<INewWord> referenceConfig){
        // todo 异常数据处理
        // 需要学习新词的文本,个文本按照\n 分割
        logger.debug("==========error find nws=============");

        String allTxt = input.getStringByField(TopoConstant.DEC_ALL_TXT);
        try{
            INewWord iNewWord = referenceConfig.get();
            logger.debug(" ---------------errFindNewWord ----- find newWord");
            // 最多返回50个词
            Result result= iNewWord.findNewWord(allTxt,50, true);
            if(!result.isSuccess()){
//                System.out.println("服务端出错了，请发送短信给维护人员,短信内容:" + result.getContent());
                // todo 将异常消息发送给kafka, 把拼接好的数据发送给kafka
                collector.emit(TOPOLOGY_STREAM_NW_ERR_ID, new Values(allTxt));
                return;
            }
            String nws = result.getContent();

            //  多个词发送给下游 词和词性 多个词用#分割 ，/后面的是词性，保存到redis单条发送
            // 如果学习到了新词，需要保存
            if(!StringUtils.isEmpty(nws)){
                emitWord(nws,collector);
            }
        } catch (Exception e) {
            logger.error(" errFindNewWord error!", e);
            if(!StringUtils.isEmpty(allTxt)){
                collector.emit(TOPOLOGY_STREAM_NW_ERR_ID, new Values(allTxt));
            }
        }
    }

    private static void emitWord(String nws, OutputCollector collector){
        String [] words =  nws.split(DELIMITER_FIELDS);
        logger.info(" find newWords : " + words.length);
        for (String ws : words) {
            int first = ws.indexOf(WORD_DELIMITER);
            String word = ws.substring(0, first);
            String freq = ws.substring(first + 1);
            // 发送到下游，保存到redis中
            collector.emit(TOPOLOGY_STREAM_NW_ID, new Values(word, freq));
        }
        logger.debug(" emit newWords : " + words.length);

    }
}
