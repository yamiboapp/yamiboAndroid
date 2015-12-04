package com.yamibo.main.yamibolib.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by WINFIELD on 2015/11/22.
 */
public class DateUtils {
    private static final SimpleDateFormat datetimeFormat = new SimpleDateFormat("yy-MM-dd");

    public static String timeStamp2Date(String timeStampString) {
        Long timeStamp = Long.parseLong(timeStampString) * 1000;
        String date = datetimeFormat.format(new Date(timeStamp));
        return date;
    }

}
