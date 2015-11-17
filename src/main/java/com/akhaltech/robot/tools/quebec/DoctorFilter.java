package com.akhaltech.robot.tools.quebec;

import com.akhaltech.robot.common.Constant;
import com.akhaltech.robot.common.FileUtil;
import com.akhaltech.robot.model.Doctor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by vince on 2015-09-14.
 */
public class DoctorFilter {

    private final static Logger log = Logger.getLogger(DoctorFilter.class);
    private ObjectMapper mapper = new ObjectMapper();
    private Map<String, Boolean> savedDoctorMap = new HashMap<String, Boolean>();
    private Set<String> citySet = new TreeSet<String>();

    public void manageDoctors() {
        try {
            ObjectMapper mapper = new ObjectMapper();
//            Doctor[] doctors = mapper.readValue(new File(Constant.JSON_OUTPUT), Doctor[].class);
            Doctor[] doctors = mapper.readValue(new File("/Users/vince/Downloads/doctors_qc.json"), Doctor[].class);
            filterDuplication(doctors);
            System.out.println(citySet.toString());
        }catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private void filterDuplication(Doctor[] doctors) {
        if (doctors != null && doctors.length > 0) {
            for(Doctor doctor : doctors) {
                try {
                    if (savedDoctorMap.get(doctor.getProfile().getId()) == null) {
                        if(doctor.get_id() == null)
                            doctor.set_id(doctor.getProfile().getId());
                        recordDoctorJSon(doctor);
                        savedDoctorMap.put(doctor.getProfile().getId(), true);

                        recordCity(doctor.getLocation().getAddressSummary());
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                    log.error(e.getMessage());
                    recordAttensionInfomation("Doctor<" + doctor.getProfile().getId() + "> " + doctor.getProfile().getSurname() + ", " + doctor.getProfile().getGivenName() + ": got ERROR while filtering");
                }
            }
        }
    }

    private void recordDoctorJSon(Doctor doctor) throws Exception {
        String doctorJson = mapper.writeValueAsString(doctor);
        log.info(doctorJson);
        FileUtil.appendContent(Constant.JSON_OUTPUT_FINAL_QC, doctorJson);
    }

    private void recordCity(String address) {
        if(address != null) {
            String[] fields = address.split(" ");
            for (int i = 0; i < fields.length; i++) {
                if (i > 0 && "qc".equalsIgnoreCase(fields[i].trim())) {
                    citySet.add("\"" + fields[i - 1].trim() + "\"");
                }
            }
        }
    }

    private void recordAttensionInfomation(String content) {
        FileUtil.appendContent(Constant.ATTENSION_OUTPUT_QC, content);
    }

    public static void main(String[] args) throws Exception {
        DoctorFilter doctorFilter = new DoctorFilter();
        doctorFilter.manageDoctors();
    }
}
