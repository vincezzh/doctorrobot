package com.akhaltech.robot.common;

/**
 * Created by vince on 2015-09-08.
 */
public class Constant {
    public static final String[] ALOOP = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};

    public static final String FILE_OUTPUT_BASE_PATH = "/Users/vince/Temp/DoctorRobot";

    // Ontario
    public static final String JSON_OUTPUT = FILE_OUTPUT_BASE_PATH + "/doctors_on.json";
    public static final String ATTENSION_OUTPUT = FILE_OUTPUT_BASE_PATH + "/attension_on.txt";
    public static final String JSON_OUTPUT_FINAL = FILE_OUTPUT_BASE_PATH + "/doctors_final.json";
    public static final String BASE_URL = "http://www.cpso.on.ca";
    public static final String INITIAL_DOCTOR_SEARCH_URL = BASE_URL + "/Public-Register/All-Doctors-Search";
    public static final String BRIDGE_URL = BASE_URL + "/Public-Register/All-Doctors-Search.aspx";
    public static final String DOCTOR_SEARCH_URL = BASE_URL + "/Public-Register-Info-(1)/Doctor-Search-Results";
    public static final String DOCTOR_SEARCH_NEXT_PAGE_URL = BASE_URL + "/Public-Register-Info-(1)/Doctor-Search-Results.aspx";

    // British Columbia
    public static final String BASE_URL_BC = "https://www.cpsbc.ca";
    public static final String INITIAL_DOCTOR_SEARCH_URL_BC = BASE_URL_BC + "/physician_search";
    public static final String JSON_OUTPUT_BC = FILE_OUTPUT_BASE_PATH + "/doctors_bc.json";
    public static final String ATTENSION_OUTPUT_BC = FILE_OUTPUT_BASE_PATH + "/attension_bc.txt";

    // Quebec
    public static final String BASE_URL_QC = "http://www.cmq.org";
    public static final String INITIAL_DOCTOR_SEARCH_URL_QC = BASE_URL_QC + "/bottin/list.aspx?lang=en";
    public static final String JSON_OUTPUT_QC = FILE_OUTPUT_BASE_PATH + "/doctors_qc.json";
    public static final String ATTENSION_OUTPUT_QC = FILE_OUTPUT_BASE_PATH + "/attension_qc.txt";

}
