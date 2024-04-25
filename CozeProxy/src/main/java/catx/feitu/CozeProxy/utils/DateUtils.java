package catx.feitu.CozeProxy.utils;

import java.util.Calendar;
import java.util.Date;

public class DateUtils {
    private DateUtils () { }
    /**
     * 计算时间是否过了相对与计算时间的第二天某点
     *
     * @param inputTime 被比较时间
     * @param hour 小时
     * @return true/false
     * @author ChatGPT
     */
    public static boolean isBeforeNineAMNextDay(Date inputTime, int hour) {
        // 设置日历对象为传入时间
        Calendar inputCalendar = Calendar.getInstance();
        inputCalendar.setTime(inputTime);

        // 在传入时间上加上一天，并设置时间为9点整
        inputCalendar.add(Calendar.DAY_OF_MONTH, 1);
        inputCalendar.set(Calendar.HOUR_OF_DAY, hour);
        inputCalendar.set(Calendar.MINUTE, 0);
        inputCalendar.set(Calendar.SECOND, 0);
        inputCalendar.set(Calendar.MILLISECOND, 0);

        // 获取设定时间（第二天的9点）
        Date nextDayNineAM = inputCalendar.getTime();

        // 返回当前时间是否在第二天的9点之前的结果
        return  new Date().before(nextDayNineAM);
    }

}
