package com.hollycrm.hollyvoc.test;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ConsumerConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.hollycrm.hollyvoc.api.newword.INewWord;
import com.hollycrm.hollyvoc.api.newword.bean.Result;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

import static com.hollycrm.hollyvoc.constant.Constant.DELIMITER_FIELDS;
import static com.hollycrm.hollyvoc.constant.Constant.WORD_DELIMITER;


/**
 * Created by zhaihw on 2017/8/15.
 */
public class NewWord {

    public static void main(String[] args) {
        ApplicationConfig config = new ApplicationConfig();
        config.setName("sonsumer-newword");

        RegistryConfig registryConfig = new RegistryConfig("zookeeper://hd-23:2181");
        registryConfig.setProtocol("zookeeper");

        ConsumerConfig consumerConfig = new ConsumerConfig();
        consumerConfig.setRegistry(registryConfig);
        consumerConfig.setApplication(config);

        ReferenceConfig<INewWord> referenceConfig = new ReferenceConfig<>();
        referenceConfig.setCheck(false);
        referenceConfig.setVersion("1.0");
        referenceConfig.setInterface(INewWord.class);
        referenceConfig.setConsumer(consumerConfig);

        INewWord iNewWord = referenceConfig.get();

        System.out.println("------------- start call ");
        String ens = "会议认为，今年以来，面对复杂多变的国内外形势，各地区各部门按照中央经济工作会议部署，坚持稳中求进工作总基调，贯彻落实新发展理念，以推进供给侧结构性改革为主线，有效推进各项工作，保持了经济发展稳中向好态势。上半年经济运行在合理区间，主要指标好于预期，城镇就业平稳增加，财政收入、企业利润和居民收入较快增长，质量效益回升。物价总体稳定。经济结构调整不断深化，消费需求对经济增长的拉动作用保持强劲，产业结构调整加快，过剩产能继续化解，适应消费升级的行业和战略性新兴产业快速发展，各产业内部组织结构改善。区域协同联动效应初步显现，“一带一路”建设、京津冀协同发展、长江经济带发展三大战略深入实施，脱贫攻坚战成效明显，生态保护、环境治理取得新进展。新发展理念和供给侧结构性改革决策部署日益深入人心，政府和企业行为正在发生积极变化，促进供求关系发生变化，推动了市场信心逐步好转。\n" +
                "\n" +
                "　　会议指出，在充分肯定成绩的同时要清醒看到，经济运行中还存在不少矛盾和问题，要站在经济长周期和结构优化升级的角度，把握经济发展阶段性特征，保持头脑清醒和战略定力，坚定不移抓好供给侧结构性改革，妥善化解重大风险隐患，促进经济社会持续健康发展。\n" +
                "\n" +
                "　　会议强调，做好下半年经济工作，要坚持稳中求进工作总基调，更好把握稳和进的关系，把握好平衡，把握好时机，把握好度。要保持政策连续性和稳定性，实施好积极的财政政策和稳健的货币政策，坚持以供给侧结构性改革为主线，适度扩大总需求，加强预期引导，深化创新驱动，确保经济平稳健康发展，提高经济运行质量和效益；确保供给侧结构性改革得到深化，推动经济结构调整取得实质性进展；确保守住不发生系统性金融风险的底线。各方面要努力工作，保持社会大局稳定，尽职尽责为党的十九大召开创造良好环境。\n" +
                "\n" +
                "　　会议要求，各地区各部门要增强政治意识、大局意识、核心意识、看齐意识，把思想认识统一到党中央对经济形势的判断上来，不折不扣贯彻执行党中央制定的大政方针。要做好统筹协调，把工作做精做细，形成政策合力。要有高度的责任心和担当精神，发扬钉钉子精神，不断提高执行力。\n" +
                "\n" +
                "　　会议强调，要坚定不移深化供给侧结构性改革，深入推进“三去一降一补”，紧紧抓住处置“僵尸企业”这个牛鼻子，更多运用市场机制实现优胜劣汰。加大补短板力度，改善供给质量。要积极稳妥化解累积的地方政府债务风险，有效规范地方政府举债融资，坚决遏制隐性债务增量。要深入扎实整治金融乱象，加强金融监管协调，提高金融服务实体经济的效率和水平。要稳定房地产市场，坚持政策连续性稳定性，加快建立长效机制。要稳定外资和民间投资，稳定信心，加强产权保护，扩大外资市场准入，增强营商环境对投资者的吸引力。要高度重视民生工作，积极促进就业，切实帮助困难群众解决生产生活中遇到的困难和问题。";

        Result result= iNewWord.findNewWord(ens,50, true);
        if(!result.isSuccess()){
            System.out.println("服务端出错了，请发送短信给维护人员,短信内容:" + result.getContent());
            return;
        }
        System.out.println(result.getContent());
        //  经济结构调整/n_new/5.01/2
        // 经济结构调整 n_new/5.01/2
        // 多个词以#分割
        //经济结构调整/n_new/5.01/2#地方政府/n_new/3.34/2#政策连续性/n_new/3.33/2#
        Map<String,String> map = new HashMap<>();
        map.put("经济结构调整", "1");
        String nws = result.getContent();
        if(!StringUtils.isEmpty(nws)){
            String [] words =  nws.split(DELIMITER_FIELDS);
            System.out.println();
            for (String ws : words) {
                int first = ws.indexOf(WORD_DELIMITER);
                String word = ws.substring(0, first);
                String freq = ws.substring(first + 1);
                System.out.println("word: " +word + "freq: " +freq);
                map.put(word,freq);
                // 发送到下游，保存到redis中
//                collector.emit(TOPOLOGY_STREAM_NW_ID, new Values(word, freq));
            }
        }

        System.out.println("=====================================");
        // k 相同会覆盖value的值
        map.forEach((k,v)->{
            System.out.println("k : " + k + "v " + v);
        });
        System.out.println("-------------- end call");


    }
}
