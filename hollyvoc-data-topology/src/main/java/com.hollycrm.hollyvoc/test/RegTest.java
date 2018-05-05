package com.hollycrm.hollyvoc.test;

import org.apache.commons.lang.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by qianxm on 2017/8/4.
 */
public class RegTest {
    /**
     * 获取所有匹配内容[正则表达式]
     *
     * @param regex
     *            正则表达式
     * @param content
     *            要匹配的文本内容
     * @return
     */
    public static List<String> find(String regex, String content) {
        List<String> list = new ArrayList<String>();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            list.add(matcher.group());
        }
        return list;
    }

    public static void main(String[] args) {

//        String txt = "您好，十一话务员服务。嗯，我问一下这部手机可以开通长途漫游吗？国际漫游吗？我去漫游，可以开通。可以开通是吧！但是您这个出国之后，用不了网，因为现在境外的话，把这个2g的网络都掐了。嗯？最低就是3g的了。呃，它不是有3g4g都可以用吗这个。不行。但是您这个是2g的卡。换机卡，但是在北京的。3g流量还没有都可以使啊！对这个是可以您这网络是可以用的。噢那你说什么不可以用啊！就是就是，境外的话，是那个2g的2g的就是拔插了吗，因为我这边的话。查了您这个2g的卡，但是还是可以用3g3g的网络的。那到境外怎么使不了还是么样。什么意思我没太明白。稍等一下啊！你先让我操作，不要做啊！那行那就不挂。不开了不开了好吧！喂，恩恩，您好。嗯？您不开什么？对不开不开不开好吧嗯，好，那感谢您再见。";
        String txt = "您好，很高兴为您服务。唉，您好我想开通一下那个国际漫游。您稍等帮您看一下。嗯？正在给您看一下，您本机号码国际漫游已经开通了女士。啊！嗯，已经开通了对。九九那个其他的副卡好呢！您稍等帮您看一下。嗯？正在帮您看一下副卡没有开通女士。噢，那你帮我开通一下行吗？嗯，您稍等一下。您好女士您有主卡的号码的，修改后的服务密码吗？没有。那您能提供一下机主姓名身份证号和身份证地址吗？女士。嗯，那个嗯。金额一千1110102197808292727。嗯，还有什么长安还有您身份证地址。嗯，华676好多16从2号。嗯？您稍等现在给您办理。您稍等一下。嗯？您好女士您副卡需要开通国际的上网功能吗？嗯，不需要了唉。心愿啊！嗯，您是要去哪个国家呢！嗯，泰国。嗯，泰国您稍等帮您看一下。现在给您看一下泰国的话，咱们这边有一个一天26元不限流量的这个功能，您需要开通吗？嗯不需要。您稍等现在给您办理。您好女士这边已经给您办理成功了。嗯？那个东哪个哪个卡啊！嗯，您稍等正在给您看一下。这边帮您看一下号码的话，是有130012385。嗯？三。然后还有118519258909。然后还有一个18519259909。然后还有一个18519259919。然后有一个18519353753。嗯，这些都是开了开通了国际漫游，别的好对然后那个那个上网上就是按计时来算好吗。对是的。哦那个打电话就是最多少钱，一分钟要泰国还。您稍等帮您看一下。嗯嗯。现在给您看一下泰国的资费的话，您在当地接听拨打都是每分钟1块8毛6。噢，那那短信什么的是90。短信是接短信免费的发送短信的话是每条八毛六。哦，行嗯，上网多少钱啊！您稍等。嗯？现在给您看在泰国的话，上网是呃，5块钱五兆流量相当于一兆一块钱。噢好的行那谢谢啊！您还有其他这个开通的。嗯网名还省了哈唉，好，谢谢！唉，祝您生。";
        String reg = "(?=.*(流量))^(?=.*(漫游|停机))^(?!.*(短信)).*$";
        reg = "(漫游|停机)"; // 流量 AND 漫游 OR 停机 AND 短信
        txt = "喂，我请问想办理一下流量包腾讯网凯，都有什么流量套餐4";
        reg = "(办理|流量)*"; // 你好 OR 流量 NOT 请问
        reg ="(?!.*(您好|请问))^((?=.*(腾讯王卡|腾讯网凯|腾讯网卡))^(?!.*(业务)))^.*$";
        // 非 ((?!您好).)*
//        reg = "((?!办理).)*";
        Pattern p = Pattern.compile(reg);
        Matcher match = p.matcher(txt);
        Set<String> strs = new HashSet<>();
        int i =0;
        // 没有匹配
        System.out.println("find" + match.find());
        while(match.find()){
//            System.out.println(i++);
            strs.add(match.group(1));
//            System.out.println(match.group(1));
        }
        System.out.println("匹配个数：" + strs.size());
        for (String s : strs){
            System.out.println("s: "+s);
        }
        System.out.println("============================");
        System.out.println(StringUtils.join(strs," "));

        if (txt.matches(reg)) {

//            tmpCategoryName = RuleList.get(i).split("=")[0];
            List<String> Words = find("[\\u4E00-\\u9FA5\\w]+", reg);
            for (String word : Words) {
                if (word.trim().length() > 1){
//                    txt = txt.replaceAll(word, " 【" + word + "】");
//                    System.out.println("w: "+word);
                    if(txt.contains(word)){
                        System.out.println(word);
                    }
                }
            }

//            for (String w : Words){
//                System.out.println("w: "+w);
//            }

        }
        System.out.println("txt" + txt);


//        String s = "流量争议#1#1# #漫游 OR 停机 OR 流量#(漫游|停机|流量)#beijing1# # # # # # # # # ";
//        String str = "&&&";
//        String[] ss= s.split("#");
//        for(String s1:ss) {
//            System.out.println(s1 + " " + StringUtils.isEmpty(s1) +" " + s1.equals(" "));
//        }
//        System.out.println(ss.length);
//        System.out.println(StringUtils.join(ss, "___"));

//        String[] str1= str.split("&");
//        for(String s1:str1) {
//            System.out.println(s1 + " " + StringUtils.isEmpty(s1));
//        }
//        System.out.println(str1.length);
//        System.out.println(StringUtils.join(str1, "___"));
//        Map<String,Long> map = new HashMap<>();
//        map.put("1",2L);
//        map.put("2",3L);
//        System.out.println("size " + map.size());
//        map.forEach((k,v)->{
//            System.out.println("k"+k + " " + v);
////            map.remove(k);
//        });
//        System.out.println("size " + map.size());

//        Map<String,List<String[]>> resource = new HashMap<>();
//        resource.computeIfAbsent("1", list-> new Vector<>(18));
//
//        System.out.println(resource.get("1").size());

        // ============== 表达式匹配 先匹配（），再用AND分割，匹配各个分段如果都能匹配则成立
//        String txtContent = "流量 AND 漫游 OR (停机 AND 短信) AND ((短信 OR 话费) AND (123))";
//        // 思路：后缀表达式
//        String[] words = txtContent.split("\\(*\\)");
//        for(String w : words){
//            System.out.println("================");
//            System.out.println(w);
//        }

//        String regs = "(你好)*|(流量)*!(请问)*";


    }

}
