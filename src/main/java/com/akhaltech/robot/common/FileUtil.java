package com.akhaltech.robot.common;

import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by vince on 2015-09-09.
 */
public class FileUtil {
    private final static Logger log = Logger.getLogger(FileUtil.class);

    public static void appendContent(String fileName, String data) {
        BufferedWriter bufferWritter = null;
        try {
            File file =new File(fileName);
            if(!file.exists()) {
                file.createNewFile();
            }

            FileWriter fileWritter = new FileWriter(file, true);
            bufferWritter = new BufferedWriter(fileWritter);
            bufferWritter.write(data);
            bufferWritter.write(",\n");
        }catch(IOException e) {
            log.error(e.getMessage());
        }finally {
            if(bufferWritter != null) {
                try {
                    bufferWritter.close();
                } catch (IOException e) {
                    log.error(e.getMessage());
                }
            }
        }
    }
}
