package com.hollyvoc.data.pretreat.pares.voice.parsing.impl;


import com.hollyvoc.data.pretreat.pares.voice.bean.ParseResult;
import com.hollyvoc.data.pretreat.pares.voice.bean.WordBean;
import com.hollyvoc.data.pretreat.pares.voice.bean.iflytekvoice.Channel;
import com.hollyvoc.data.pretreat.pares.voice.bean.iflytekvoice.Function;
import com.hollyvoc.data.pretreat.pares.voice.bean.iflytekvoice.Result;
import com.hollyvoc.data.pretreat.pares.voice.bean.iflytekvoice.Subject;
import com.hollyvoc.data.pretreat.pares.voice.parsing.IVoiceParse;

import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.*;

import static java.lang.Integer.parseInt;

/**
 * Created by zhaihw on 2016/3/9.
 * 科大讯飞的xml解析
 */
public class IflytekParser implements IVoiceParse {
    //场景分隔
    final static String SUB_SPEAKER_SEP="speaker-separation";
    final static String SUB_SEARCH="search";
    final static String CHANNEL_N0="n0";
    final static String CHANNEL_N1="n1";
    final static String CHANNEL_MIX="mix";
    final static String FUNCTION_1_BEST="1-best";
    private InputStream is;
    private Unmarshaller unmarshaller;
    private boolean isParseWord;
    public IflytekParser(InputStream is, Unmarshaller unmarshaller) {
        this.is = is;
        this.unmarshaller = unmarshaller;
    }
    public IflytekParser(InputStream is, Unmarshaller unmarshaller,boolean isParseWord ) {
        this(is,unmarshaller);
        this.isParseWord = isParseWord;
    }
    public ParseResult parse(){
        ParseResult pr = new ParseResult();
        try {
            Result result=(Result)unmarshaller.unmarshal(is);
            String waveUri=result.getInstance().getWaveuri();
            pr.setWaveUri(waveUri);
            Map<Long,String> n0Map=null;
            Map<Long,String> n1Map=null;
            // todo 坐席平均速度
            Number n0Spead=0.0;
            BigInteger sayTime=null;
            if(result.getInstance()!=null) {
                long duration=result.getInstance().getDuration().longValue();
                pr.setDuration(duration);
                List<Subject> subs=result.getInstance().getSubject();
                for(Subject sub:subs){
                    if(SUB_SPEAKER_SEP.equals(sub.getValue())){
                        List<Channel> channels=sub.getChannel();
                        if(channels!=null && channels.size()>0){
                            for(Channel channel:channels){
                                if(CHANNEL_MIX.equals(channel.getNo())){
                                    sayTime=channel.getItems().getDuration();
                                    break;
                                }
                            }
                        }
                        continue;
                    }
                    if(SUB_SEARCH.equals(sub.getValue())){
                        List<Channel> channels=sub.getChannel();
                        if(channels!=null && !channels.isEmpty()){
                            for(Channel channel:channels){
                                if(CHANNEL_N0.equals(channel.getNo())){
                                    for(Function function:channel.getFunction()){
                                        if(FUNCTION_1_BEST.equals(function.getValue())){
                                            // todo 获取坐席平均速度
                                            n0Spead = function.getSpeed();
                                            n0Map=parseSen(function,CHANNEL_N0);
                                            break;
                                        }
                                    }
                                    continue;
                                }else if(CHANNEL_N1.equals(channel.getNo())){
                                    for(Function function:channel.getFunction()){
                                        if(FUNCTION_1_BEST.equals(function.getValue())){
                                            n1Map=parseSen(function,CHANNEL_N1);
                                            break;
                                        }
                                    }
                                    continue;
                                }
                            }
                        }

                    }
                    if(isParseWord) {//是否解析词信息  生成波形图片需要
                        if (SUB_SEARCH.equals(sub.getValue())) {// 解析xml中的词信息 role ,start,end, text
                            List<Channel> channels = sub.getChannel();
                            if (channels != null && !channels.isEmpty()) {
                                for (Channel channel : channels) {
                                    if (CHANNEL_N0.equals(channel.getNo())) {
                                        for (Function function : channel.getFunction()) {
                                            if (FUNCTION_1_BEST.equals(function.getValue())) {
                                                if (pr.getWordTimes() == null) {
                                                    pr.setWordTimes(new ArrayList<WordBean>());
                                                }
                                                pr.getWordTimes().addAll(parse(function, CHANNEL_N0));
                                                break;
                                            }
                                        }
                                        continue;
                                    } else if (CHANNEL_N1.equals(channel.getNo())) {
                                        for (Function function : channel.getFunction()) {
                                            if (FUNCTION_1_BEST.equals(function.getValue())) {
                                                pr.getWordTimes().addAll(parse(function, CHANNEL_N1));
                                                break;
                                            }
                                        }
                                        continue;
                                    }
                                }
                            }

                        }
                    }
                }
            }
            long silence = pr.getDuration() - sayTime.longValue();
            pr.setSilence(silence);
            pr.setAgentSpeed(n0Spead);
            getRecordText(n0Map, n1Map, pr);
        }catch (Exception e){
            return null;
        }finally {
            try {
                if (is != null)
                    is.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return pr;
    }
    private void getRecordText(Map<Long,String> first,Map<Long,String> last,ParseResult pr){
        if(first==null || last==null)return;
        List<Long> ks=new ArrayList<>(first.size()+last.size());
        mergeKeys(first, ks);
        mergeKeys(last, ks);
        StringBuilder n0=new StringBuilder();
        StringBuilder n1=new StringBuilder();
        Collections.sort(ks, new Comparator<Long>() {
            @Override
            public int compare(Long o1, Long o2) {
                return o1 > o2 ? 1 : (o1 < o2 ? -1 : 0);
            }
        });
        StringBuilder alls=new StringBuilder();
        int b=0;//一个人多次说话进行合并
        for(Long l:ks){
            String text=first.get(l);
            if(text!=null){
                if(b==1){
                    alls.append(text);
                }else{
                    alls.append(agentMark).append(text);
                }
                n0.append(senMark).append(text);
                b=1;
                continue;
            }
            text=last.get(l);
            if(text!=null){
                if(b==2){
                    alls.append(text);
                }else{
                    alls.append(userMark).append(text);
                }
                n1.append(senMark).append(text);
                b=2;
            }
        }
        pr.setUserText(n1.toString());
        pr.setAgentText(n0.toString());
        pr.setAllText(alls.toString());
    }

    protected void mergeKeys(Map<Long,String> map,List<Long> ks){
        Iterator<Long> fks=map.keySet().iterator();
        while(fks.hasNext())
            ks.add(fks.next());
    }
    protected Map<Long,String> parseSen(Function function,String type)throws Exception{
        if(function==null)return null;
        Map<Long,String> map=new HashMap<Long,String>();
        String spaceText=function.getText();
        String spaceTime=function.getTime();
        if(spaceText==null || spaceText.length()==0)return map;
        String[] texts=spaceText.split(" ");
        String[] times=spaceTime.split(" ");
        if(texts.length<times.length)
            throw new Exception("时间与文字间隔不匹配，spaceText:\n"+spaceText+"\nspaceTime:\n"+spaceText);
        long es=0;
        StringBuffer sb=new StringBuffer();
        for(int i=0;i<times.length;i++){
            String temp=times[i];
            String[] se=temp.split(",");
            long start=Long.parseLong(se[0]);
            long end=Long.parseLong(se[1]);
            if(es==0 || es==start){
                sb.append(texts[i]);
                es=end;
                continue;
            }
            map.put(es, sb.toString());
            es=0;
            sb=new StringBuffer();
            sb.append(texts[i]);
        }
        map.put(Long.parseLong(times[times.length-1].split(",")[1]),sb.toString());
        return map;
    }
    protected List<WordBean> parse(Function function,String type)throws Exception{
        int role = 1;//1 表示 客户，0 表示 坐席
        List<WordBean> wbs = new ArrayList<>();
        if(function==null)return null;
        if(CHANNEL_N0.equals(type)){
            role = 0;
        }
        String spaceText=function.getText();
        String spaceTime=function.getTime();
        if(spaceText==null || spaceText.length()==0)return wbs;
        String[] texts=spaceText.split(" ");
        String[] times=spaceTime.split(" ");
        if(texts.length<times.length)
            throw new Exception("时间与文字间隔不匹配，spaceText:\n"+spaceText+"\nspaceTime:\n"+spaceText);
        for(int i=0;i<times.length;i++){
            String rt = texts[i];
            if(rt.length() <= 1) continue;
            String temp=times[i];
            String[] se=temp.split(",");
            WordBean wb = new WordBean();
            wb.setRole(role);
            wb.setStartTime(parseInt(se[0]));
            wb.setEndTime(parseInt(se[1]));
            wb.setText(rt);
            wbs.add(wb);
        }
        return  wbs;
    }
}
