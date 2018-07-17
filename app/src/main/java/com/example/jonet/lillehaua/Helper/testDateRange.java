package com.example.jonet.lillehaua.Helper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class testDateRange {

    static final int START_HOUR = 9;
    static final int END_HOUR = 2;

    public static void main(String[] args) {
        String now_time = new SimpleDateFormat("HH:mm").format(new Date());
        System.err.println(isInRange(Integer.parseInt(now_time.replace(":","")),START_HOUR*100,END_HOUR*100));

    }

    public static boolean isInRange(int now_time, int start_time, int end_time) {

        if ((now_time>start_time)&&
                (now_time<end_time)       )
        {
            return true;
        }
        return false;
    }

    public boolean isTimeBetweenTwoHours(int fromHour, int toHour, Calendar now) {
        //Start Time
        Calendar from = Calendar.getInstance();
        from.set(Calendar.HOUR_OF_DAY, fromHour);
        from.set(Calendar.MINUTE, 0);
        //Stop Time
        Calendar to = Calendar.getInstance();
        to.set(Calendar.HOUR_OF_DAY, toHour);
        to.set(Calendar.MINUTE, 0);

        if(to.before(from)) {
            if (now.after(to)) to.add(Calendar.DATE, 1);
            else from.add(Calendar.DATE, -1);
        }
        return now.after(from) && now.before(to);
    }

}

