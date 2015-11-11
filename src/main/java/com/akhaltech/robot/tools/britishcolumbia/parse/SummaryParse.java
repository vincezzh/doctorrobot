package com.akhaltech.robot.tools.britishcolumbia.parse;

import com.akhaltech.robot.common.Constant;
import com.akhaltech.robot.common.FileUtil;
import com.akhaltech.robot.common.StringUtil;
import com.akhaltech.robot.model.*;
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
    private String referalURL;
    private String cookieValue;
    private String doctorFullName;

    public SummaryParse(String referalURL, String doctorFullName, String cookieValue) {
        this.referalURL = referalURL;
        this.cookieValue = cookieValue;
        this.doctorFullName = doctorFullName;
    }

    public void accessSummary(String url) throws Exception {
        httpClient = HttpClientBuilder.create().build();
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        httpGet.setHeader("Accept-Encoding", "gzip, deflate, sdch");
        httpGet.setHeader("Accept-Language", "en-US,en;q=0.8,zh-CN;q=0.6,zh;q=0.4,de;q=0.2,fr-FR;q=0.2,fr;q=0.2,ja;q=0.2,zh-TW;q=0.2");
        httpGet.setHeader("Cache-Control", "max-age=0");
        httpGet.setHeader("Connection", "keep-alive");
        httpGet.setHeader("Cookie", cookieValue);
        httpGet.setHeader("Host", "www.cpsbc.ca");
//        httpGet.setHeader("If-Modified-Since", "Tue, 20 Oct 2015 03:04:58 GMT");
//        httpGet.setHeader("If-None-Match", "");
        httpGet.setHeader("Referer", referalURL);
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

        Element tab1Div = document.getElementById("tabs-1");
        Element tab2Div = document.getElementById("tabs-2");
        Profile profile = parseProfile(tab1Div);
        if(profile.getId() != null) {
            Location location = parseLocation(tab1Div);
            Registration registration = parseRegistration(tab2Div);
            List<Specialty> specialtyList = parseSpecialties(tab2Div);

            Doctor doctor = new Doctor();
            doctor.set_id(profile.getId());
            doctor.setCountry("Canada");
            doctor.setProvince("British Columbia");
            doctor.setStatus("Active");
            doctor.setProfile(profile);
            doctor.setLocation(location);
            doctor.setRegistration(registration);
            doctor.setSpecialtyList(specialtyList);

            recordDoctorJSon(doctor);
        }else {
            log.info("Dr. " + doctorFullName + " has no ID.");
            recordAttensionInfomation("Dr. " + doctorFullName + " has no ID.");
        }
    }

    private Profile parseProfile(Element profileElement) {
        String genderText = "Gender";
        String languageText = "Additional Language(s)";
        String idText = "MSP Number";

        Elements divs = profileElement.select("div.label-value-row");
        String id = null;
        String givenName = null;
        String surname = null;
        String formerName = null;
        String gender = null;
        List<String> languageList = new ArrayList<String>();
        languageList.add("ENGLISH");

        for(Element div : divs) {
            String textInB = div.select("b").first().text();
            if(genderText.equals(textInB)) {
                gender = div.select("div.label-value-row-value").first().text();
                if("m".equalsIgnoreCase(gender)) {
                    gender = "Male";
                }else {
                    gender = "Female";
                }
            }else if(idText.equals(textInB)) {
                id = div.select("div.label-value-row-value").first().text();
            }else if(languageText.equals(textInB)) {
                Elements languageElements = div.select("ul").select("li");
                for(Element languageElement : languageElements) {
                    languageList.add(languageElement.text().toUpperCase());
                }
            }
        }

        String[] names = doctorFullName.split(",");
        surname = names[0].trim();
        givenName = names[1].substring(0, names[1].lastIndexOf(" ")).trim();

        Profile profile = new Profile();
        profile.setId(id);
        profile.setGivenName(givenName);
        profile.setSurname(surname);
        profile.setFormerName(formerName);
        profile.setGender(gender);
        profile.setLanguageList(languageList);

        return profile;
    }

    private Location parseLocation(Element profileElement) {
        String addressText = "Business Address";

        Elements divs = profileElement.select("div.label-value-row");
        String addressSummary = null;
        String contactSummary = null;
        String corporationName = null;

        for(Element div : divs) {
            String textInB = div.select("b").first().text();
            if(addressText.equals(textInB)) {
                String addressHtml = div.select("div.label-value-row-value").select("div.physio-address-data").first().html();
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

                String contactHtmlAll = div.select("div.label-value-row-value").first().html();
                String[] contactContents = contactHtmlAll.substring(contactHtmlAll.indexOf("</div>") + "</div>".length()).split("<br>");
                for(String contactContent : contactContents) {
                    if(contactSummary == null) {
                        contactSummary = contactContent;
                    }else {
                        contactSummary = contactSummary + ", " + contactContent;
                    }
                }
                if(contactSummary != null) {
                    contactSummary = contactSummary.replace("T:", "Phone:").replace("F:", "Fax:");
                }
            }
        }

        Location location = new Location();
        location.setAddressSummary(addressSummary);
        location.setContactSummary(contactSummary);
        location.setCorporationName(corporationName);

        return location;
    }

    private Registration parseRegistration(Element element) {
        String registrationText = "Class of Registration";
        String certificationText = "Certification";
        String degreeText = "Degree";

        Elements divs = element.select("div.label-value-row");
        String registrationStatus = "Active Member";
        String registrationClass = null;
        String graduatedFrom = null;
        List<RegistrationHistory> historyList = new ArrayList<RegistrationHistory>();
        List<PostgraduateTraining> trainingList = new ArrayList<PostgraduateTraining>();

        for(Element div : divs) {
            String textInB = div.select("b").first().text();
            if (registrationText.equals(textInB)) {
                String titleHtml = div.select("div.label-value-row-value").first().html();
                int index = titleHtml.indexOf("<ul");
                if(index != -1) {
                    registrationClass = titleHtml.substring(0, titleHtml.indexOf("<ul")).trim();
                }else {
                    registrationClass = titleHtml.trim();
                }

                Elements trainingElements = div.select("ul").select("li");
                for(Element oneTraining : trainingElements) {
                    PostgraduateTraining training = new PostgraduateTraining();
                    training.setType(oneTraining.text().trim());
                    trainingList.add(training);
                }
            }else if(certificationText.equals(textInB)) {
                Elements historyElements = div.select("ul").select("li");
                for(Element oneHistory : historyElements) {
                    RegistrationHistory history = new RegistrationHistory();
                    history.setDescription(oneHistory.text());
                    historyList.add(history);
                }
            }else if(degreeText.equals(textInB)) {
                graduatedFrom = div.select("div.label-value-row-value").first().text();
            }
        }

        Registration registration = new Registration();
        registration.setRegistrationClass(registrationClass);
        registration.setRegistrationStatus(registrationStatus);
        registration.setGraduatedFrom(graduatedFrom);
        registration.setTrainingList(trainingList);
        registration.setHistoryList(historyList);

        return registration;
    }

    private List<Specialty> parseSpecialties(Element element) {
        List<Specialty> specialtyList = new ArrayList<Specialty>();
        String specialtyText = "Class of Registration";

        Elements divs = element.select("div.label-value-row");
        for(Element div : divs) {
            String textInB = div.select("b").first().text();
            if (specialtyText.equals(textInB)) {
                String titleHtml = div.select("div.label-value-row-value").first().html();
                int index = titleHtml.indexOf("<ul");
                String title = titleHtml.trim();
                if(index != -1) {
                    title = titleHtml.substring(0, titleHtml.indexOf("<ul")).trim();
                }
                if(title.contains("Family")) {
                    Specialty specialty = new Specialty();
                    specialty.setType("FULL General Family Practice");
                    specialty.setName("Family Medicine");
                    specialtyList.add(specialty);
                }else {
                    Elements specialtyElements = div.select("ul").select("li");
                    for(Element oneSpecialty : specialtyElements) {
                        Specialty specialty = new Specialty();
                        specialty.setType("FULL Specialty Practice");
                        specialty.setName(oneSpecialty.text().replace("RCPSC - ", "").trim());
                        specialtyList.add(specialty);
                    }
                }
            }
        }

        return specialtyList;
    }

    private void recordAttensionInfomation(String content) {
        FileUtil.appendContent(Constant.ATTENSION_OUTPUT_BC, content);
    }

    private void recordDoctorJSon(Doctor doctor) throws Exception {
        String doctorJson = mapper.writeValueAsString(doctor);
        log.info(doctorJson);
        FileUtil.appendContent(Constant.JSON_OUTPUT_BC, doctorJson);
    }

}
