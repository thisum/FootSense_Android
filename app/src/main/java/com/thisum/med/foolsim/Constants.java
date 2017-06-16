package com.thisum.med.foolsim;

/**
 * Created by thisum on 4/19/2017.
 */

public class Constants
{
    public static final int REQUEST_ENABLE_BT = 1;
    public static final long SCAN_PERIOD = 10000;
//    public static final String DEVICE = "F6:93:F4:74:EE:19";
    public static final String DEVICE = "D4:D1:0D:09:77:97";
    public static final String HEART_RATE_SERVICE = "00002a37-0000-1000-8000-00805f9b34fb";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    public static final String QUERY_PARAM_LEFT_LEG = "left_leg";
    public static final String QUERY_PARAM_RIGHT_LEG = "right_leg";
    public static final String QUERY_PARAM_PATIENT_EMAIL = "patient_email";
    public static final String QUERY_PARAM_PATIENT_NAME = "patient_name";

    public enum Application
    {
        SETUP,
        FOOT_SCAN,
        SERVER_RESPONSE
    }
}
