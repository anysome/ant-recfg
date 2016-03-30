package net.asfun.ant.reconfig;

import java.util.Date;

/**
 * Created by asfun on 16/3/30.
 */
public class ValueUtil {

    /**
     * magical value support
     * @param value
     * @return
     */
    public static String computeValue(String value) {
        if (value != null && value.startsWith("${") ) {
            if ( "${now}".equalsIgnoreCase(value) ) {
                return String.valueOf(new Date().getTime());
            }
            return value;
        } else {
            return value;
        }
    }
}
