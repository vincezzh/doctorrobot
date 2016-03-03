package com.akhaltech.robot.tools.migrate;

import com.akhaltech.robot.common.Constant;
import com.akhaltech.robot.common.FileUtil;
import com.akhaltech.robot.model.Doctor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * Created by vzhang on 03/03/2016.
 */
@Component
public class DoctorJSonToSql {

    private final static Logger log = Logger.getLogger(DoctorJSonToSql.class);

    @Autowired
    private DoctorSaver doctorSaver;

    public void migrateDoctors() {
        try {
            File folder = new File("C:\\temp\\json");
            File[] jsons = folder.listFiles();
            for(File json : jsons) {
                ObjectMapper mapper = new ObjectMapper();
                Doctor[] doctors = mapper.readValue(json, Doctor[].class);
                saveDoctors(doctors);
            }
        }catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private void saveDoctors(Doctor[] doctors) {
        if (doctors != null && doctors.length > 0) {
            for(Doctor doctor : doctors) {
                try {
                    doctorSaver.save(doctor);
                }catch (Exception e) {
                    log.error(e.getMessage());
                    recordAttensionInfomation("Doctor<" + doctor.getProfile().getId() + "> " + doctor.getProfile().getSurname() + ", " + doctor.getProfile().getGivenName() + ": got ERROR while filtering");
                }
            }
        }
    }

    private void recordAttensionInfomation(String content) {
        FileUtil.appendContent(Constant.ATTENSION_OUTPUT, content);
    }

    public static void main(String[] args) throws Exception {
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        DoctorJSonToSql doctorJSonToSql = (DoctorJSonToSql) context.getBean("doctorJSonToSql");
        doctorJSonToSql.migrateDoctors();
    }

}
