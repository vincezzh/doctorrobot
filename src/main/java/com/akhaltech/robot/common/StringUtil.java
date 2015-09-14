package com.akhaltech.robot.common;

/**
 * Created by vince on 2015-09-08.
 */
public class StringUtil {

    public static String trimValue(String value) {
        return trim(value.toCharArray()).replace("&nbsp;", "");
    }

    public static String trim(char[] value) {
        int len = value.length;
        int st = 0;
        char[] val = value;
        while ((st < len) && (val[st] == 32 || val[st] == 160)) {
            st++;
        }
        while ((st < len) && (val[len - 1] == 32 || val[len - 1] == 160)) {
            len--;
        }
        return ((st > 0) || (len < value.length)) ? String.valueOf(value).substring(st, len) : String.valueOf(value);
    }

    public static String removeLastComman(String value) {
        if(value.endsWith(","))
            value = value.substring(0, value.length() - 2);
        return value;
    }

}
