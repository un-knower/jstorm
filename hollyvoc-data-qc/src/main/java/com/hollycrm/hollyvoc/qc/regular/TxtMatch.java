package com.hollycrm.hollyvoc.qc.regular;

import shade.storm.org.apache.commons.lang.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by qianxm on 2017/9/18.
 * 文本内容匹配
 */
public class TxtMatch {

    private static TxtMatch instance = new TxtMatch();
    public static TxtMatch getInstance(){
        return instance;
    }


    /**
     * 提取() 中的内容
     * @param str 逻辑表达式
     * @return 返回() 中的内容
     */
    private static List<String>  splitBrackets(String str){
        List<String> result = new ArrayList<>();

        String[] strs = str.split(" ");
        // 栈是后进先出
        Stack<String> stack = new Stack<>();
        for(int i=0; i < strs.length; i++){
            String s = strs[i];
            if(StringUtils.isEmpty(s)){
                continue;
            }
            if(s.contains("(")) {
                int index = i-1;
                if(index < 0) {
                    stack.push(s);
                } else if(stack.empty()) {
                    stack.push(strs[index] + " " + s);
                } else if(!stack.empty()) {
                    stack.pop();
                    stack.push(strs[index] + " " + s);
                }
                continue;
            }

            if(!stack.empty()){
                stack.push(s);
            }

            if(s.contains(")")) {
                List<String> queue = new ArrayList<>();
                while(!stack.isEmpty()){
                    String ss = stack.pop();
                    queue.add(ss);
                    if(ss.contains("(")) {
                        break;
                    }
                }
                Collections.reverse(queue);
                result.add(StringUtils.join(queue, " "));
            }

        }
        // 思路，匹配（， 并查找（前面的关键词
        return result;
    }

    /**
     * 正则匹配.
     * @param content 文本
     * @param reg 正则表达式
     * @return 匹配结果
     */
    private static Set<String> regMatch(String content, String reg){
        Pattern p1 = Pattern.compile(reg);
        Matcher match = p1.matcher(content);
        Set<String> result = new HashSet<>();
        while(match.find()){
            result.add(match.group(1));
        }
        return result;
    }


    /**
     * 获取匹配内容.
     * @param txtContent 文本内容
     * @param reg 逻辑表达式
     * @return 返回匹配结果
     */
    public  String getMatchWords(String txtContent, String reg) {

        // 1. 将括号里的表达式提取出来,还要知道(前面的关系值,并将原有的 截取的（）替换成空格
        List<String> list = splitBrackets(reg);
        reg = reg.replaceAll("(\\([^\\)]*\\))|( AND\\([^\\)]*\\))|( OR \\([^\\)]*\\))|( NOT \\([^\\)]*\\))|(\\()|(\\)\\))|(\\))"," ");

        // 2. 根据AND 关键词截取
        String[] andRelease = reg.split("AND");

        // 3. 然后对两次截取的结果进行匹配， 先对not进行匹配，再对and进行匹配，OR可以替换成|用正则匹配
        // 如果not中的词匹配结果为true，则这条记录不符合，否则符合继续匹配
        Set<String> matchWords = new HashSet<>();
        // 对() 中的表达式进行匹配
        for(String str:list){
            str = str.replaceAll(" ","");
            if(StringUtils.isEmpty(str)){
                continue;
            }

            // str 是以NOT (、AND (、OR ( 开头的
            // 保证not 后面的表达式匹配不到即可
            if(str.contains("NOT(")){
                // 如果有匹配的值，return，否则继续
                str = str.replaceAll("NOT\\(","").replaceAll("\\(","").replaceAll("\\)","");

                // 如果not(),中有成立的则不匹配
//                boolean isFind = matchWord(str, txtContent, matchWords);
                if(matchWord(str, txtContent, matchWords)){
                    return "";
                }
            }

            // 如果是AND ，必须有匹配的，如果是or，可以没有匹配值
            if(str.contains("AND(")){
                str = str.replaceAll("AND\\(","").replaceAll("\\(","").replaceAll("\\)","");
//                boolean isFind = matchWord(str, txtContent, matchWords);
                if(!matchWord(str, txtContent, matchWords)){
                    return "";
                }
            }

            // OR() 关系的，不管最后结果如何都可以
            if(str.contains("OR(")){
                str = str.replaceAll("OR\\(","").replaceAll("\\(","").replaceAll("\\)","");
                matchWord(str, txtContent, matchWords);
            }
            str = str.replaceAll("(\\()|(\\))","");
            // 开头有（）的表达式
            matchWord(str, txtContent, matchWords);

        }

        // 4. 对and 表达式进行匹配,如果匹配不到说明不符合要求，not后面的需要不符合，not前面的不需要
        for(String s: andRelease) {
            String[] notRelease = s.split("NOT");
            boolean isFind = false;
            for(int i = 0; i < notRelease.length; i++) {
                String nw = notRelease[i];
                nw = "(" + nw.replaceAll("OR", "|").replaceAll(" ","").replaceAll("(\\(|\\))","") + ")";
                Set<String> set = regMatch(txtContent, nw);
                if(set.size()>0){
                    matchWords.addAll(set);
                    isFind = true;
                } else {
                    return "";
                }
                // not 关系的词有匹配的值
                if(isFind && i > 0){
                    return "";
                }
            }
        }
        return StringUtils.join(matchWords," ");
    }


    /**
     * 对没有括号的逻辑词进行匹配.
     * @param reg 逻辑表达式
     * @param words 匹配到的词
     * @param content 文本内容
     * @return 返回匹配结果
     */
    private static boolean matchWord(String reg, String content, Set<String> words){
        boolean result = false;
        // 优先匹配 NOT后面的内容，即NOT截取到数组之后除了第一位的值，后面所有的词只要能匹配到就不成立
        String[] notWords = reg.split("NOT");
        for(int i = 0; i < notWords.length; i++) {
            String s = notWords[i];
            s = s.replaceAll("OR","|");
            // and 关系的都需要成立
            String[] andWords = s.split("AND");
            for(String as: andWords) {
                if(StringUtils.isEmpty(as)){
                    continue;
                }
                as = "(" + as.replaceAll(" ","") + ")";
                Set<String> set = regMatch(content, as);
                if(set.size()>0){
                    words.addAll(set);
                    result = true;
                } else {
                    return false;
                }
            }
            // not 关系的词有匹配的值
            if(result && i > 0){
                return false;
            }
        }
        return result;
    }


    public static void main(String[] args) {
        try{
            TxtMatch txtMatch = new TxtMatch();
            String content = "喂，您好，那个我问一下现在我手机。发不出短信你就是我，然后我查询我的话费可以查询，他查询不了，我这没有。。是您这个号码吗先生。。对。。稍等帮您看一下提示年什么时候发送失败吗？。信息发送的时候，显示什么网？请检查网络服务。。您是把这个中心点，那么这个短信中心的服务号码给改了。。没有改呀！。从什么时间开始出现这种情况的。。嗯，就是我，刚才把这卡安上以后。他开机依然后发短信不行，然后。我我以为是欠费了，欠费查询，他说什么暂时无法查询，然后我交了交了。。行那我帮您记录反馈一下，可以所有号码发都发不出去是吧！都什么时间开始出现了先生信号正常吗？。信号正常。。今天晚上出现了这个问题是吧！。对对。。行那联系电话留您来电号码联系您可以吗？。就我的联系电话啊！。对。嗯？。嗯157。。157您说。。0339。4536。。对那您再说一下联系号码，现在没有登记上去，您再说一下。。157你是说是留我的联系，号码还是说我本机号。。号码。联系号码，您不是。噢，159明说。。0339。。1339。。4536。。4536。。嗯？。嗯，行那您。这个问题我这边帮您记录下来，我们将在48小时之内和查清楚，您这问题已经回复您。。嗯，那好吧！。看一下啊，请您稍后自行吗？。嗯没有了，谢谢啊！。不客气感谢来电，稍后对我服务评价再见。您好，很高兴为您服务。";
            String reg ="( 24小时回复 OR 48小时 OR 专人回复 OR 专员联系 OR 专人联系 ) NOT ( 新装 OR 宽带 OR 光纤 OR 态度不好 OR 投诉你 OR 态度差 OR 人员问题 OR 未激活 OR 收货信息 OR 订单 OR 定单 OR 滴滴 OR 弟弟 OR DD OR 腾讯 OR 蚂蚁 OR 上传 )";
            content = "嗯，什么的啊您好。银行的联通公司打扰您一下，请问您之前是反映手机产生流量费用的问题吗女士。对。嗯，这边给您看了一下流量详单您在2月份的流量得话是套餐里边有两个g的流量给您看了一下流量使用是正常的，您是反馈说没有流量提醒，短信是嘛。对呢！就流量超出的时候，没有收到还是就所有10010发送短信都收不到呀！后来就流量超出以后没给我发短信。嗯，这边给您看了一下您这是按天提醒的，然后这个二十八号的时候流量超出然后还有一条法值的提醒，在11:59的时候给您发送流量提醒是剩了呃，是剩了202.5兆，然后这个月白日扣了三问一下。然后嗯，然后看了一下在13:39的时候，这边给您发送的短信是告诉您，这个领导已经超出了，建议您在使用过程中，随时查询一下，当前流量发这提醒您不足10%的时候，您叫酌情使用了，然后考虑您满意度和首次反映这个问题，您产生49块1毛8，可以按照产生费用的10%收取您五块钱剩下的44块1毛8可以给您申请一个首次的返费，但这个费用的话也只能分申请。这一次。后期如果您再产生的还就不能再退了女士嗯？嗯，还没有呢，女士就是说咱们这个地话，稍后会有一个10010的回访教您凭满意，但您满意的情况下，才能去给您申请这个费用。关闭嗯，对啊！嗯，但是如果您测平时一般或者不满意的活动，没有办法去给您申请的好吧！嗯行。嗯，那您还有其他问题吗？嗯对10010会给您回访的，然后您根据语音提示自行满意就行，然后44块1毛8就给您申请一周左右退费，到账请您，随时查收。嗯行。嗯，那打扰您了女士再见。嗯，再见。";
            reg = "投诉 AND (不满意 OR 坏人) ";
            String words = txtMatch.getMatchWords(content,reg);
            if(StringUtils.isEmpty(words)) {
                System.out.println(" has no macth words");
            } else {
                System.out.println(" macthWords: " + words);
            }
//
//            reg = "(24小时回复|48小时|专人回复|专员联系|专人联系)";
//            System.out.println(regMatch(content,reg).toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
