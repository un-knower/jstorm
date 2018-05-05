package com.hollycrm.hollyvoc.qc.rule;

//import com.hollycrm.contant.Contants;
//import com.ql.util.express.Rule;
//import com.ql.util.express.RuleContext;
//import com.ql.util.express.RuleRunner;
import com.hollycrm.hollyvoc.qc.bean.CustcontentInfo;
import lombok.extern.log4j.Log4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.hollycrm.hollyvoc.constant.Constant.*;

/**
 * Created by qianxm on 2017/12/1.
 *
 */
@Log4j
public class RuleCustInfoDemo {
    private static String outpath = "D:\\workspace\\text-rule\\hollynlp\\hollynlp\\doc\\rule";

    private static Map<Integer,String> filedHBaseMap;  // 字段-顺序

    public RuleCustInfoDemo() {
        Map<String,Integer> map = getHBaseMapping();
        filedHBaseMap = new HashMap<>(map.size());
        map.forEach((k, v) -> {
            filedHBaseMap.put(v, k);
        });
    }


    /**
     * 读取文件内容.
     * @param src 文件
     * @return 返回文件内容
     */
    public static String readFileContect(File src) {
        try {
            String res = "";
            String code = "utf-8";
            InputStreamReader isr = new InputStreamReader(new FileInputStream(src), code);
            BufferedReader bf = new BufferedReader(isr);
            String str = null;
            while ((str = bf.readLine()) != null) {
                if (str.trim().length() > 0) {
                    res += str.trim();
                }
            }
            bf.close();
            isr.close();
            System.out.println("file: " + src.getName() + "\n content: " + res);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 读取原始文件.
     * @param src
     * @return
     */
    public static List<CustcontentInfo> readConstInfoData(File src){
        try {
            String res = "";
            String code = "utf-8";
            List<CustcontentInfo> list = new ArrayList<>();
            InputStreamReader isr = new InputStreamReader(new FileInputStream(src), code);
            BufferedReader bf = new BufferedReader(isr);
            String str = null;
            while ((str = bf.readLine()) != null) {
                CustcontentInfo ci = new CustcontentInfo();
                if (str.trim().length() > 0) {
//                    res += str.trim();
                    String[] infos = str.split("\\" + DELIMITER_PIPE);
                    for(int i=0;i<infos.length;i++){
                        // 找到文本的值添
                        String val = infos[i];
                        String file = filedHBaseMap.get(i);
                        if(userContent.equals(file)){
                            ci.setUserContent(val);
                                continue;
                        }
                        if(agentContent.equals(file)){
                            ci.setAgentContent(val);
                            continue;
                        }
                        if(mobileNo.equals(file)){
                            ci.setMobileNo(val);
                            continue;
                        }
                        if(custinfoId.equals(file)){
                            ci.setCustinfoId(val);
                            continue;
                        }
                    }

                    list.add(ci);
                }
            }
            bf.close();
            isr.close();
//            System.out.println("file: " + src.getName() + "\n content: " + res);
            return list;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static void main(String[] args) throws Exception {

        RuleCustInfoDemo rd = new RuleCustInfoDemo();
        String expressFile = outpath + "/moble";
        String express = readFileContect(new File(expressFile));
        //System.out.println("express:" + express);

        List<Rule> ruleList=new ArrayList<Rule>();

        Rule aRule=new Rule();
        aRule.setId(1);
        aRule.setRuleName("电话号码");
        aRule.setGroupType(1);
        aRule.setGroupName("包含电话号码");
        aRule.setMatchCondition("true");
        aRule.setExecuteContent(express);
        ruleList.add(aRule);
        // 录音数据
        String dataPath = outpath + File.separator +"Infodata";
        List<CustcontentInfo> CustcontentInfo=readConstInfoData(new File(dataPath));
        // todo 数据加载
//        Random rand = new Random();
//        for (int i=0;i<10;i++){
//            int j = rand.nextInt(100); // 生成0-10以内的随机数
//            CustcontentInfo.add(new CustomersInfo(i+ "",j + ""));
//            System.out.println("欠费额度（元）:" + CustcontentInfo.get(i).getARR_FEE());
//        }
        List<String> mobels = new ArrayList<>(); // 电话号码
        // todo 参数匹配
        mobels.add("10010");
        mobels.add("17633169418");

        List<CustcontentInfo> result = new ArrayList<>();
        RuleRunner ruleRunner = new RuleRunner();
        ruleRunner.setRuleList(ruleList);
        RuleContext<String,Object> ruleContext = new RuleContext<String,Object>();
        ruleContext.put("CustcontentInfo", CustcontentInfo);
        ruleContext.put("mobel", mobels);
        ruleContext.put("result", result);
        //这里设置规则执行上下文信息
        ruleContext.setGroupType(1);
        //规则分组，1:库存占位
        ruleRunner.dispatch(ruleContext);//开始解析并执行规则

//        for (int i=0;i<result.size();i++){
//            System.out.println("欠费额度（元）:" + result.get(i).getCustinfoId() + "" + result.get(i).getCustinfoId());
//        }
        result.forEach(k->{
            log.info("匹配到：id： " + k.getCustinfoId() + " mobel: " + k.getMobileNo());
        });
    }
}
