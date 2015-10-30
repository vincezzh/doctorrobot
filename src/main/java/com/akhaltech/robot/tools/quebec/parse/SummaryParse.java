package com.akhaltech.robot.tools.quebec.parse;

import com.akhaltech.robot.common.Constant;
import com.akhaltech.robot.common.FileUtil;
import com.akhaltech.robot.common.StringUtil;
import com.akhaltech.robot.model.Doctor;
import com.akhaltech.robot.model.Location;
import com.akhaltech.robot.model.Profile;
import com.akhaltech.robot.model.Specialty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vince on 2015-09-08.
 */
public class SummaryParse {

    private final static Logger log = Logger.getLogger(SummaryParse.class);
    private HttpClient httpClient = null;

    private ObjectMapper mapper = new ObjectMapper();
    private String url;

    public SummaryParse(String url) {
        this.url = url;
    }

    public void accessSummary() throws Exception {
        httpClient = HttpClientBuilder.create().build();
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        httpGet.setHeader("Accept-Encoding", "gzip, deflate, sdch");
        httpGet.setHeader("Accept-Language", "en-US,en;q=0.8,zh-CN;q=0.6,zh;q=0.4,de;q=0.2,fr-FR;q=0.2,fr;q=0.2,ja;q=0.2,zh-TW;q=0.2");
        httpGet.setHeader("Connection", "keep-alive");
        httpGet.setHeader("Cookie", "_gat=1; _ga=GA1.2.314019381.1445970943");
        httpGet.setHeader("Host", "www.cmq.org");
        httpGet.setHeader("Referer", "http://www.cmq.org/bottin/list.aspx?lang=en");
        httpGet.setHeader("Upgrade-Insecure-Requests", "1");
        httpGet.setHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.85 Safari/537.36");

        HttpResponse httpResponse = httpClient.execute(httpGet);
        HttpEntity entity = httpResponse.getEntity();
        String line = null;
        BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(), "UTF-8"));
        StringBuilder htmlBuilder = new StringBuilder();
        while((line = reader.readLine()) != null) {
            htmlBuilder.append(line + "\n");
        }

        parseSummary(htmlBuilder.toString());
    }

    private void parseSummary(String html) throws Exception {
        Document document = Jsoup.parse(html);

        Element table = document.getElementById("content-html");
        Profile profile = parseProfile(table);
        if(profile.getId() != null) {
            Location location = parseLocation(table);
            List<Specialty> specialtyList = parseSpecialties(table);

            Doctor doctor = new Doctor();
            doctor.set_id(profile.getId());
            doctor.setCountry("Canada");
            doctor.setProvince("Quebec");
            doctor.setStatus("Active");
            doctor.setProfile(profile);
            doctor.setLocation(location);
            doctor.setSpecialtyList(specialtyList);

            recordDoctorJSon(doctor);
        }else {
            log.info("Dr. " + profile.getGivenName() + " " + profile.getSurname() + " has no ID.");
            recordAttensionInfomation("Dr. " + profile.getGivenName() + " " + profile.getSurname() + " has no ID.");
        }
    }

    private Profile parseProfile(Element table) {
        String genderText = "Gender";
        String idText = "Permit number";

        Elements trs = table.select("tr");
        String id = null;
        String givenName = null;
        String surname = null;
        String formerName = null;
        String gender = null;
        List<String> languageList = new ArrayList<String>();
        languageList.add("English");
        languageList.add("French");

        for(int i=0; i<trs.size(); i++) {
            Element tr = trs.get(i);

            if(i == 0) {
                String fullName = tr.text();
                String[] names = fullName.split(",");
                surname = names[0].trim();
                givenName = names[1].trim();
                givenName = givenName.substring(0, givenName.indexOf("(")).trim();
            }else {
                String header = tr.select("td").first().text();
                if(genderText.equals(header)) {
                    gender = tr.select("td").get(1).text();
                    if("m".equalsIgnoreCase(gender)) {
                        gender = "Male";
                    }else {
                        gender = "Female";
                    }
                }else if(idText.equals(header)) {
                    id = tr.select("td").get(1).text();
                }
            }
        }

        Profile profile = new Profile();
        profile.setId(id);
        profile.setGivenName(givenName);
        profile.setSurname(surname);
        profile.setFormerName(formerName);
        profile.setGender(gender);
        profile.setLanguageList(languageList);

        return profile;
    }

    private Location parseLocation(Element table) {
        String addressText = "Address";
        String contactText = "Phone";

        Elements trs = table.select("tr");
        String addressSummary = null;
        String contactSummary = null;
        String corporationName = null;

        for(Element tr : trs) {
            Elements tds = tr.select("td");
            if(tds != null && tds.size() == 2) {
                String header = tds.get(0).text();

                if(addressText.equals(header)) {
                    String addressHtml = tds.get(1).html();
                    String[] addressContents = addressHtml.split("<br>");
                    boolean isCompanyName = true;
                    for(String addressContent : addressContents) {
                        if(isCompanyName && !StringUtil.containsDitital(addressContent)) {
                            if(corporationName == null) {
                                corporationName = addressContent.trim();
                            }else {
                                corporationName = corporationName + ", " + addressContent.trim();
                            }
                        }else {
                            isCompanyName = false;
                            if(addressSummary == null) {
                                addressSummary = addressContent.trim();
                            }else {
                                addressSummary = addressSummary + ", " + addressContent.trim();
                            }
                        }
                    }

                    if(corporationName != null)
                        corporationName = corporationName.replace("&nbsp;", " ");
                    if(addressSummary != null)
                        addressSummary = addressSummary.replace("&nbsp;", " ");
                }else if(contactText.equals(header)) {
                    contactSummary = "Phone:" + tds.get(1).text().trim().replace("#", "Ext.");
                }
            }
        }

        Location location = new Location();
        location.setAddressSummary(addressSummary);
        location.setContactSummary(contactSummary);
        location.setCorporationName(corporationName);

        return location;
    }

    private List<Specialty> parseSpecialties(Element table) {
        List<Specialty> specialtyList = new ArrayList<Specialty>();
        String specialtyText = "Specialty";

        Elements trs = table.select("tr");
        for(Element tr : trs) {
            Elements tds = tr.select("td");
            if (tds != null && tds.size() == 2) {
                String header = tds.get(0).text();

                if(specialtyText.equals(header)) {
                    String value = tds.get(1).text();
                    if(value.contains("Family")) {
                        Specialty specialty = new Specialty();
                        specialty.setType("FULL General Family Practice");
                        specialty.setName("Family Medicine");
                        specialtyList.add(specialty);
                    }else {
                        Specialty specialty = new Specialty();
                        specialty.setType("FULL Specialty Practice");
                        specialty.setName(value);
                        specialtyList.add(specialty);
                    }
                }
            }
        }

        return specialtyList;
    }

    private void recordAttensionInfomation(String content) {
        FileUtil.appendContent(Constant.ATTENSION_OUTPUT_QC, content);
    }

    private void recordDoctorJSon(Doctor doctor) throws Exception {
        String doctorJson = mapper.writeValueAsString(doctor);
        log.info(doctorJson);
        FileUtil.appendContent(Constant.JSON_OUTPUT_QC, doctorJson);
    }

}
