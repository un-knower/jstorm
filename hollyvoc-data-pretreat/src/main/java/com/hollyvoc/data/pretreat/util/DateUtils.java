package com.hollyvoc.data.pretreat.util;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static java.util.Calendar.DATE;
import static java.util.Calendar.MONTH;

/**
 * 时间工具类
 */
public class DateUtils {
    static Logger logger = Logger.getLogger(DateUtils.class);
    public final static String DF_YYYY_MM_DD="yyyy-MM-dd";
    public final static String DF_YYYYMMDD="yyyyMMdd";
    public final static String DF_YYYYMMDDHH="yyyyMMddHH";
    public final static String DF_24Hours="yyyy-MM-dd HH:mm:ss";
    public final static String DF_LONG_CURRENT="yyyyMMddHHmmssSSS";

    /**
     * 获取当前时间
     * @return yyyy-MM-dd HH:mm:ss
     */
    public static String getCurrentTime(){
        return DateFormatUtils.format(new Date(),DF_24Hours);
    }
    /**
     * 获取当前时间
     * @return yyyy-MM-dd HH:mm:ss
     */
    public static String getCurrentTime(String fmt){
        return DateFormatUtils.format(new Date(),fmt);
    }

    /**
     * 获取当前时间
     * @return yyyyMMdd
     */
    public static String getCurrentDate(){
        return DateFormatUtils.format(new Date(),DF_YYYYMMDD);
    }

    /**
     * 获取昨天的日期
     * @return 昨天的日期
     */
    public static String getYesterday(){
        return getOffsetDay(-1);
    }

    /**
     *  偏移日期 默认格式 YYYYMMDD
     * @param i 偏移量
     * @return 字符串
     */
    public static String getOffsetDay(int i){
        return DateFormatUtils.format(getAppointDate(Calendar.DATE, i),DF_YYYYMMDD);
    }

    /**
     * 偏移时间 小时
     * @param offset 偏移量
     * @return 字符串 yyyyMMddHH
     */
    public static String getOffsetHour(int offset){
        return DateFormatUtils.format(getAppointDate(Calendar.HOUR, offset),DF_YYYYMMDDHH);
    }
    /**
     * 偏移时间 小时
     * @param offset 偏移量
     * @return 字符串 yyyyMMddHH
     */
    public static String getOffsetHour(Date date, int offset){
        return DateFormatUtils.format(offsetDate(date, Calendar.HOUR, offset),DF_YYYYMMDDHH);
    }
    /**
     *  偏移日期 默认格式 YYYYMMDD
     * @param i 偏移量
     * @return 字符串
     */
    public static String getOffsetDay(Date date, int i){
        return DateFormatUtils.format(offsetDate(date, Calendar.DATE, i), DF_YYYYMMDD);
    }
    /**
     * 偏移月份
     * @param i 偏移量
     * @return 字符串 yyyyMM
     */
    public static String getOffsetMonth(Date date, int i) {
        return DateFormatUtils.format(offsetDate(date, Calendar.MONTH, i), DF_YYYYMMDD);
    }
    /**
     * 偏移月份
     * @param i 偏移量
     * @return 字符串 yyyyMM
     */
    public static String getOffsetMonth(int i) {
        return DateFormatUtils.format(getAppointDate(Calendar.MONTH, i), "yyyyMM");
    }
    /**
     * 获取日期，
     * @param i i为负数 则日期提前，i为正数则日期延后
     * @return 日期
     */
    public static Date getAppointDate(int c, int i){
        Calendar cal = Calendar.getInstance();
        cal.add(c, i);
        return cal.getTime();
    }

    /**
     * 便宜日期
     * @param date 日期
     * @param c 字段
     * @param i 量
     * @return 时间
     */
    public static Date offsetDate(Date date, int c, int i){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(c, i);
        return cal.getTime();
    }
    /**
     * 对指定的日期进行运算
     * @param date 指定日期
     * @param field Calendar 中定义的field
     * @param value 期望的值
     * @return
     */
    private static Date getAppointDate(Date date,int field,int value){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(field,value);
        return cal.getTime();
    }
    /**
     * 格式化日期
     * @param date 日期
     * @param formatStr 格式化字符串
     * @return
     */
    public static String formatDate(Date date,String formatStr){
        return DateFormatUtils.format(date,formatStr);
    }

    /**
     * 将指定格式的日期串转为日期
     * @param dateStr
     * @param format
     * @return
     * @throws ParseException
     */
    public static Date parseDate(String dateStr,String format)throws ParseException{
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.parse(dateStr);
    }

    /**
     * 获取指定时间所在月有多少天
     * @param date 日期
     * @return 天数
     */
    public static int getDaysOfMonth(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    /**
     * 根据指定格式的日期字符串获取其月份有多少天
     * @param dateSrc 日期字符串
     * @param srcFormat 字符串格式
     * @return 天数
     */
    public static int getDaysOfMonth(String dateSrc,String srcFormat)throws ParseException{
        Date date = parseDate(dateSrc,srcFormat);
        return getDaysOfMonth(date);
    }

    @Deprecated
    public static List<String> getDaysOfMonthList(String dateSrc,String srcFormat,String destForamt)throws ParseException{
        Date srcDate = parseDate(dateSrc,srcFormat);
        return null;
    }

    /**
     * 获取两个日期字符串之间的每一天的字符串
     * @param beginDateStr 开始日期字符串
     * @param endDateStr 结束日期
     * @param srcFormat 输入日期字符串格式
     * @param destFormat 输出日期字符串指定格式
     * @return
     * @throws ParseException
     */
    public static List<String> getDaysOf2Date(String beginDateStr,String endDateStr,String srcFormat,String destFormat)throws ParseException{
        Date bDate = parseDate(beginDateStr,srcFormat);
        Date eDate = parseDate(endDateStr,srcFormat);
        return getDaysOf2Date(bDate,eDate,destFormat);
    }

    /**
     * 获取两个日期之间的所有天数，并以指定的格式返回
     * @param beginDate 开始日期
     * @param endDate 结束日期
     * @param dateFormat 日期格式串
     * @return
     */
    public static List<String> getDaysOf2Date(Date beginDate,Date endDate,String dateFormat){
        Calendar cal = Calendar.getInstance();
        cal.setTime(beginDate);
        List<String> days = new ArrayList<>();
        while(cal.getTime().before(endDate) || cal.getTime().compareTo(endDate) == 0){
            days.add(formatDate(cal.getTime(),dateFormat));
            cal.add(Calendar.DAY_OF_YEAR,1);
        }
        return days;
    }



    /**
     * 获取指定月份的第一天
     * @param date 指定月份
     * @return 月份第一天
     */
    public static Date getFirstDayOfMonth(Date date){
        return getAppointDate(date,Calendar.DAY_OF_MONTH,1);
    }

    /**
     * 获取指定月份的最后天
     * @param date 指定月份
     * @return 月份最后一天
     */
    public static Date getLastDayOfMonth(Date date){
        int days = getDaysOfMonth(date);
        return getAppointDate(date,Calendar.DAY_OF_MONTH,days);
    }

    /**
     * 当前是日期是否月的最后一天
     * @param date 日期
     * @return 是否
     */
    public static boolean isLastDayOfMonth(Date date) {
        int days = getDaysOfMonth(date);
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date);
        int realDay = cal1.get(Calendar.DATE);
        return days == realDay;
    }

    /**
     * 查询当前日期是周几，0-6分别表示周日~周六
     * @param date
     * @return
     */
    public static int getWhichDayOfWeek(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.DAY_OF_WEEK)-1;
    }

    /**
     * 获取指定日期所在月份的每一天 ，并按照期望格式输出
     * @param dateStr 源日期字符串
     * @param srcFormat 源字符串格式
     * @param destFormat 期望输出字符串格式
     * @return {@see java.util.List}
     * @throws ParseException
     */
    public static List<String> getDaysOfMonth(String dateStr,String srcFormat,String destFormat)throws ParseException{
        Date date = parseDate(dateStr,srcFormat);
        Date firstDay = getFirstDayOfMonth(date);
        Date lastDay = getLastDayOfMonth(date);
        return getDaysOf2Date(firstDay,lastDay,destFormat);
    }

    /**
     * 计算两个日期相差的天数
     * @param date1 开始日期
     * @param date2 结束日期
     * @return 天数
     */
    public static int differDays(Date date1, Date date2) {
        long dif = date2.getTime() - date1.getTime();
        return (int) (dif / (1000 * 3600 * 24));
    }

    /**
     * 获取两个日期之间的月份
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return list
     */
    public static List<String> getMonthOfYear(Date startDate, Date endDate) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(startDate);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(endDate);
        int l = cal2.get(MONTH) - cal1.get(MONTH) ;
        List<String> list = new ArrayList<>();
        for(int i = 0; i <= l; i ++) {
            list.add(DateUtils.formatDate(cal1.getTime(), "yyyyMM"));
            cal1.set(MONTH, cal1.get(MONTH) + 1);
        }
        return list;
    }
    public static void main(String[] rags){
        String date1="20160229", date2 = "20160527", date3 = "20160501";
        try {
            Date startDate = DateUtils.parseDate(date1, DF_YYYYMMDD);
            Date endDate = DateUtils.parseDate(date2, DF_YYYYMMDD);
            Date currentDate = DateUtils.parseDate(date3, DF_YYYYMMDD);
            System.out.println(differDays(startDate, endDate));
            Test test = new Test();
////            test.createDayReports(startDate, endDate,currentDate, true);
//            test.createWeekReports(startDate, endDate, currentDate, false);
            test.createMonthReports(startDate, endDate, currentDate, false);
//            System.out.println(isLastDayOfMonth(date));

        }catch (Exception e){
            e.printStackTrace();
        }
    }

   static class Test {
        // 创建日报
        public void createDayReports(Date startDate, Date endDate, Date currentDate, boolean hasReport) {
            Date realDate = DateUtils.offsetDate(currentDate, DATE, -1); // 真实的数据时间
            if(realDate.getTime() > endDate.getTime()) { // 数据时间大于等于结束时间，最终计算的结束时间以结束时间的为主
                createDayReport(startDate, endDate);
            }else{ // 数据时间小于等于结束时间 最终计算时间以数据为主
                if(hasReport) { // 如果已存在日报，则生成当前数据时间的日报
                    String startDay = formatDate(realDate, DF_YYYYMMDD),endDay = startDay;;
                    System.out.println(startDay + "   " + endDay);
                }else{ // 从来没有生成过日报， 则从开始时间到真实数据时间之间，生成每个日报
                    createDayReport(startDate, realDate);
                }
            }
        }

        private void createDayReport(Date startDate, Date endDate) {
            List<String> days = DateUtils.getDaysOf2Date(startDate, endDate, DF_YYYYMMDD);
            System.out.println(days.size());
            days.forEach(ds -> {
                String endDay = ds, startDay = ds;
                System.out.println(startDay + "   " + endDay);
            });
        }

        //创建周报
        public void createWeekReports(Date startDate, Date endDate, Date currentDate, boolean hasReport) {
            Date realDate = DateUtils.offsetDate(currentDate, DATE, -1); // 真实的数据时间
            if(realDate.getTime() > endDate.getTime()) { // 数据时间大于等于结束时间，最终计算的结束时间以结束时间的为主
                createWeekReport(startDate, endDate, endDate);
            }else{ // 数据时间小于等于结束时间 最终计算时间以数据为主
                if(hasReport) { // 如果已存在周报，则生成当前数据时间的周报
                    int difDays = DateUtils.differDays(startDate, realDate) + 1;
                    int op = difDays % 7;
                    if(op == 0) { // 真实数据时间是周的最后一天 则生成
                        String startDay = DateUtils.getOffsetDay(realDate, - 6);
                        String endDay = formatDate(realDate, DF_YYYYMMDD);
                        System.out.println(startDay + "   " + endDay);
                    }else{ // 如果剩余的天数 不足一周,且当前的数据时间等于结束时间 则生成周报
                        int overPlusDays = DateUtils.differDays(realDate, endDate) + 1;
                        if (overPlusDays < 7 || realDate.getTime() == endDate.getTime()) {
                            String startDay = DateUtils.getOffsetDay(realDate, - overPlusDays);
                            String endDay = formatDate(realDate, DF_YYYYMMDD);
                            System.out.println(startDay + "   " + endDay);
                        }
                    }
                }else{ // 从来没有生成过日报， 则从开始时间到真实数据时间之间，生成每个周报
                    createWeekReport(startDate, realDate, endDate);
                }
            }
        }

        private void createWeekReport(Date startDate, Date endDate, Date finishDate) {
            Date next = startDate;
            while(next.getTime() <= endDate.getTime()) {
                String startDay = formatDate(next, DF_YYYYMMDD);
                next = DateUtils.offsetDate(next, DATE, 6);
                // 如果不是完结的话 如果下次的时间大于结束时间 且当前时间不是完结时间的 则直接退出
                if(next.getTime() > endDate.getTime() && endDate.getTime() != finishDate.getTime()) {
                    return;
                }
                Date end = next.getTime() <= endDate.getTime() ? next : endDate;;

                String endDay = formatDate(end, DF_YYYYMMDD);
                System.out.println(startDay + "   " + endDay);
                next = DateUtils.offsetDate(next, DATE, 1);
            }
        }

        public void createMonthReports(Date startDate, Date endDate, Date currentDate, boolean hasReport) throws Exception{
            Date realDate = DateUtils.offsetDate(currentDate, DATE, -1); // 真实的数据时间
            if(realDate.getTime() > endDate.getTime()) { // 数据时间大于等于结束时间，最终计算的结束时间以结束时间的为主
                createMonthReport(startDate, endDate, endDate);
            }else{ // 数据时间小于等于结束时间 最终计算时间以数据为主
                if(hasReport) {
                    if(DateUtils.isLastDayOfMonth(realDate) || realDate.getTime() == endDate.getTime()) {
                        String startDay = formatDate(realDate, "yyyyMM") + "01";
                        String endDay = formatDate(realDate, DF_YYYYMMDD);
                        System.out.println(startDay + "   " + endDay);
                    }
                }else{
                    createMonthReport(startDate, realDate, endDate);
                }
            }
        }

        private void createMonthReport(Date startDate, Date endDate, Date finishDate) throws Exception{
            List<String> months = DateUtils.getMonthOfYear(startDate, endDate);
            for(int i = 0; i< months.size(); i++) {
                String month = months.get(i);
                int countDays = getDaysOfMonth(month, "yyyyMM");
                String last = month + String.format("%02d", countDays);;
                Date lastD = DateUtils.parseDate(last, DF_YYYYMMDD);
                if(lastD.getTime() > endDate.getTime() && endDate.getTime() != finishDate.getTime()) {
                    return;
                }
                String startDay, endDay;
                if(i == 0) {
                    startDay = formatDate(startDate, DF_YYYYMMDD);
                }else{
                    startDay = month + "01";
                }
                if(i == months.size() -1) {
                    endDay = formatDate(endDate, DF_YYYYMMDD);
                }else{
                    endDay = month + String.format("%02d", countDays);
                }
                System.out.println(startDay + "   " + endDay);
            }
        }
    }
}
