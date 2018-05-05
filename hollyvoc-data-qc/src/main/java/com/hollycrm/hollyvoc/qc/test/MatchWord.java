package com.hollycrm.hollyvoc.qc.test;


import shade.storm.org.apache.commons.lang.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by qianxm on 2017/9/12.
 */
public class MatchWord {
    private static String matchTxt(String txtContent, String reg) {

        // 1. 将括号里的表达式提取出来,还要知道(前面的关系值,并将原有的 截取的（）替换成空格
        List<String> list = matchByStack(reg);
        for (int i = 0; i < list.size(); i++) {
            System.out.println(i+"-->"+list.get(i));
        }
        System.out.println(reg);
        reg = reg.replaceAll("( AND\\([^\\)]*\\))|( OR \\([^\\)]*\\))|( NOT \\([^\\)]*\\))|(\\()|(\\)\\))|(\\))"," ");
        System.out.println("reg1"+reg);

        // 2. 根据AND
        String[] andRelease = reg.split("AND");
//        System.out.println("   " + StringUtils.join(andRelease, "-"));
        System.out.println(" andRelease: " + Arrays.toString(andRelease));

        // 3. 然后对两次截取的结果进行匹配， 先对not进行匹配，再对and进行匹配，OR可以替换成|用正则匹配
        // 如果not中的词匹配结果为true，则这条记录不符合，否则符合继续匹配
        Set<String> matchWords = new HashSet<>();
//        boolean macthResult = false;
        // 对() 中的表达式进行匹配
        for(String str:list){
            System.out.println(" (word): " + str);
            str = str.replaceAll(" ","");
            // str 是以NOT (、AND (、OR ( 开头的
            // 保证not 后面的表达式匹配不到即可
            if(str.contains("NOT(")){

                // 如果有匹配的值，return，否则继续
                str = str.replaceAll("NOT\\(","").replaceAll("\\(","").replaceAll("\\)","");

                // 如果not(),中有成立的则不匹配
                boolean isFind = matchword(reg, txtContent, matchWords);
                if(isFind){
                    return "";
                }
            }

            // 如果是AND ，必须有匹配的，如果是or，可以没有匹配值
            if(str.contains("AND(")){
                str = str.replaceAll("AND\\(","").replaceAll("\\(","").replaceAll("\\)","");
                boolean isFind = matchword(reg, txtContent, matchWords);
                if(!isFind){
                    return "";
                }
            }

             // OR() 关系的，不管最后结果如何都可以
            if(str.contains("OR(")){
                str = str.replaceAll("OR\\(","").replaceAll("\\(","").replaceAll("\\)","");
                matchword(reg, txtContent, matchWords);
            }


        }

        // 4. 对and 表达式进行匹配,如果匹配不到说明不符合要求，not后面的需要不符合，not前面的不需要
        for(String s: andRelease) {
            String[] notRelease = s.split("NOT");
            boolean isFind = false;
            for(int i = 0; i < notRelease.length; i++) {
                String nw = notRelease[i];
                nw = "(" + nw.replaceAll("OR", "|").replaceAll(" ","").replaceAll("(\\(|\\))","") + ")";
                Set<String> set = match(txtContent, nw);
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
    private static boolean matchword(String reg, String content, Set<String> words){
        boolean result = false;
        // 优先匹配 NOT后面的内容，即NOT截取到数组之后除了第一位的值，后面所有的词只要能匹配到就不成立
        String[] notWords = reg.split("NOT");
        for(int i = 0; i < notWords.length; i++) {
            String s = notWords[i];
            s = s.replaceAll("OR","|");
            // and 关系的都需要成立
            String[] andWords = s.split("AND");
            for(String as: andWords) {
                as = "(" + as.replaceAll(" ","") + ")";
                Set<String> set = match(content, as);
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

    /**
     * 使用正则表达式提取中括号中的内容，如果()中有括号，结果会 第1个中括号(第一个括号中括号
     * @param msg
     * @return
     */
    private static List<String> extractMessageByRegular(String msg){

        List<String> list=new ArrayList<String>();
        Pattern p = Pattern.compile("( AND\\([^\\)]*\\))|( OR \\([^\\)]*\\))|( NOT \\([^\\)]*\\))");
        Matcher m = p.matcher(msg);
        while(m.find()){
            list.add(m.group().substring(1, m.group().length()-1));
        }
        return list;
    }

    private static Set<String> match(String content, String reg){
        Pattern p1 = Pattern.compile(reg);
        Matcher match = p1.matcher(content);
        Set<String> result = new HashSet<>();
        // 没有匹配
        while(match.find()){
            result.add(match.group(1));
        }
        return result;
    }

    /**
     * 提取中括号中内容，忽略中括号中的中括号
     * @param msg 提取内容
     * @return
     */
    public static List<String> extractMessage(String msg) {

        List<String> list = new ArrayList<String>();
        int start = 0;
        int startFlag = 0;
        int endFlag = 0;
        for (int i = 0; i < msg.length(); i++) {
            if (msg.charAt(i) == '(') {
                startFlag++;
                if (startFlag == endFlag + 1) {
                    start = i;
                }
            } else if (msg.charAt(i) == ')') {
                endFlag++;
                if (endFlag == startFlag) {
                    list.add(msg.substring(start + 1, i));
                }
            }
        }
        return list;
    }

    // 利用栈原理，匹配（） 关键词
    private static List<String> matchByStack(String str){
        List<String> result = new ArrayList<>();
        String[] strs = str.split(" ");
        System.out.println(StringUtils.join(strs,"-"));
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
     * 队列截取(), 队列是先进先出
     * @param str
     * @return
     */
    private static List<String> sortByQueue(String str){
        String[] strs = str.split(" ");
        System.out.println(StringUtils.join(strs,"--"));
        // 栈是后进先出
        Queue<String> queue = new LinkedList<>();
        for(int i=0; i < strs.length; i++){
            String s = strs[i];
            if(StringUtils.isEmpty(s)){
                continue;
            }

            if(!queue.isEmpty()){
                queue.add(s);
            }

            if(s.contains("(")) {
                int index = i-1;
                if(index < 0) {
                    queue.add(s);
                } else if(queue.isEmpty()) {
                    queue.add(strs[index] + " " + s);
                }
            }


            if(s.contains(")")) {
                StringBuffer sb = new StringBuffer();
//                System.out.println(" stack " + stack);
                while(!queue.isEmpty()){
//                    System.out.println(stack.pop());// 将数据弹出堆栈
                    String ss = queue.poll();
                    sb.append(ss);
//                    if(ss.contains("(")) {
//                        break;
//                    }
                }
                System.out.println(sb.toString());

            }

        }
        System.out.println(" end " + queue.toString());

        // 思路，匹配（， 并查找（前面的关键词
        return null;
    }

    public static void main(String[] args) {
        String txt = "帮您把漫游费取消了,对，您欠费的原因是长途漫游，收漫游费。如果升级套餐为全国就不收漫游费";
        // 测试通过  macthWords: 不收漫游费 收漫游费 漫游费取消
        String reg = "漫游费取消 AND 收漫游费 NOT (国际漫游 OR 国际长途) OR 不收漫游费 OR 长途漫游费";
        // 测试通过  macthWords: 不收漫游费 收漫游费 漫游费取消
//        reg = "漫游费取消 OR (收漫游费 NOT (国际漫游 OR 国际长途) ) OR 不收漫游费 OR 长途漫游费";
        // 多括号验证通过
        reg = "(漫游费取消 NOT 您好 ) NOT (收漫游费 AND (长途漫游 OR 国际长途) OR 不收漫游费)";

//        txt = "请24小时回复，，你怎么这样，态度这么差，如果不解决我就投  诉你";
//        reg = "( 24小时回复 OR 48小时回复 OR 专人回复 OR 专员联系 OR 专人联系 ) NOT ( 新装 OR 宽带 OR 光纤 OR 态度不好 OR 投诉你 OR 态度差 OR 人员问题 OR 未激活 OR 收货信息 OR 订单 OR 定单 OR 滴滴 OR 弟弟 OR DD OR 腾讯 OR 蚂蚁 OR 上传 )";
//        txt = "工信部工商局消费委员会, ";
//        reg = "( 工信部 OR 通管局 OR 工商局 OR 报社 OR 曝光 OR 12315 OR 消委会 OR 消费委员会 ) NOT ( 人脸识别 OR 实名 OR 绿通 OR 加急 OR 优先处理 )";
//        reg = "工信部 OR (通管局 NOT 工商局 AND 你好)";
        // ( 工信部 OR 通管局 OR 工商局 OR 报社 OR 曝光 OR 12315 OR 消委会 OR 消费委员会 ) NOT ( 人脸识别 OR 实名 OR 绿通 OR 加急 OR 优先处理 )
//        (网速慢 AND 刷新) NOT (投诉 OR 登记 OR 小时 OR 记录 OR 测速)
//         漫游费取消 OR 收漫游费 NOT (国际漫游 OR 国际长途) OR 不收漫游费 OR 长途漫游费
//        System.out.println(extractMessage(reg).toString());

//        System.out.println("matchByStack" + matchByStack(reg));
//        sortByQueue(reg);

        txt = "喂，您好，那个我问一下现在我手机。发不出短信你就是我，然后我查询我的话费可以查询，他查询不了，我这没有。。是您这个号码吗先生。。对。。稍等帮您看一下提示年什么时候发送失败吗？。信息发送的时候，显示什么网？请检查网络服务。。您是把这个中心点，那么这个短信中心的服务号码给改了。。没有改呀！。从什么时间开始出现这种情况的。。嗯，就是我，刚才把这卡安上以后。他开机依然后发短信不行，然后。我我以为是欠费了，欠费查询，他说什么暂时无法查询，然后我交了交了。。行那我帮您记录反馈一下，可以所有号码发都发不出去是吧！都什么时间开始出现了先生信号正常吗？。信号正常。。今天晚上出现了这个问题是吧！。对对。。行那联系电话留您来电号码联系您可以吗？。就我的联系电话啊！。对。嗯？。嗯157。。157您说。。0339。4536。。对那您再说一下联系号码，现在没有登记上去，您再说一下。。157你是说是留我的联系，号码还是说我本机号。。号码。联系号码，您不是。噢，159明说。。0339。。1339。。4536。。4536。。嗯？。嗯，行那您。这个问题我这边帮您记录下来，我们将在48小时之内和查清楚，您这问题已经回复您。。嗯，那好吧！。看一下啊，请您稍后自行吗？。嗯没有了，谢谢啊！。不客气感谢来电，稍后对我服务评价再见。您好，很高兴为您服务。";
        reg ="( 24小时回复 OR 48小时回复 OR 专人回复 OR 专员联系 OR 专人联系 ) NOT ( 新装 OR 宽带 OR 光纤 OR 态度不好 OR 投诉你 OR 态度差 OR 人员问题 OR 未激活 OR 收货信息 OR 订单 OR 定单 OR 滴滴 OR 弟弟 OR DD OR 腾讯 OR 蚂蚁 OR 上传 )";

        String macthWords = matchTxt(txt, reg);

        if(StringUtils.isEmpty(macthWords)) {
            System.out.println(" has no macth words");
        } else {
            System.out.println(" macthWords: " + macthWords);
        }


//        String[] or = reg.split("AND");
//        System.out.println("and "+ Arrays.toString(or));

//        String msg = "PerformanceManager(第1个中括号(第一个(括号)中括号)4)Product(第2个中括号)<(第3个中括号)79~";
//        List<String> list = extractMessageByRegular(reg);
//        for (int i = 0; i < list.size(); i++) {
//            System.out.println(i+"-->"+list.get(i));
//        }
//        System.out.println(reg);
//        reg = reg.replaceAll("( AND\\([^\\)]*\\))|( OR \\([^\\)]*\\))|( NOT \\([^\\)]*\\))"," ");
//        System.out.println(reg);

        //        String msg1 = "PerformanceManager(第1个中括号(第一个括号中括号)4)Product(第2个中括号)<(第3个中括号)79~";
//        System.out.println("list1");
//        List<String> list1  = extractMessage(reg);
//        for (int i = 0; i < list1.size(); i++) {
//            System.out.println(i+"-->"+list1.get(i));
//        }
    }

}
