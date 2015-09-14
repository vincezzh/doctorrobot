package com.akhaltech.robot.parse;

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
import java.util.Map;

/**
 * Created by vince on 2015-09-08.
 */
public class SummaryParse {

    private final static Logger log = Logger.getLogger(SummaryParse.class);
    private HttpClient httpClient = null;

    private Map<String, Boolean> doctorIDMap;
    private ObjectMapper mapper = new ObjectMapper();
    private String locationLink;
    private String registrationDetailLink;
    private String additionalDetailLink;

    public SummaryParse(Map<String, Boolean> doctorIDMap) {
        httpClient = HttpClientBuilder.create().build();
        this.doctorIDMap = doctorIDMap;
    }

    public void accessSummary(String url) throws Exception {
        httpClient = HttpClientBuilder.create().build();
        HttpGet httpGet = new HttpGet(url);
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

        // Doctor ID
        Element idDiv = document.getElementById("singlecolumn");
        String idText = idDiv.select("h1").get(0).text();
        String id = idText.substring(idText.indexOf(":") + 1).trim();

        if(doctorIDMap.get(id) == null) {
            Element linkDiv = document.getElementById("profile");
            Elements lis = linkDiv.select("li");
            if (lis.size() > 1) {
                locationLink = Constant.BASE_URL + lis.get(1).select("a").get(0).attr("href");
            }
            if (lis.size() > 2) {
                registrationDetailLink = Constant.BASE_URL + lis.get(2).select("a").get(0).attr("href");
            }
            if (lis.size() > 3) {
                additionalDetailLink = Constant.BASE_URL + lis.get(3).select("a").get(0).attr("href");
            }

            Element summaryDiv = document.getElementById("profile-content");
            Elements detailsDivs = summaryDiv.select("div[class=detail]");
            Doctor doctor = new Doctor();
            doctor.set_id(id);
            if (detailsDivs.size() > 0) {
                doctor.setProfile(parseProfile(id, detailsDivs.get(0)));
            }
            if (detailsDivs.size() > 2) {
                doctor.setRegistration(parseRegistration(detailsDivs.get(2)));
            }
            if (detailsDivs.size() > 3) {
                doctor.setSpecialtyList(parseSpecialties(detailsDivs.get(3)));
            }
            if (detailsDivs.size() > 4) {
                doctor.setPrivilegeList(parsePrivileges(detailsDivs.get(4)));
            }

            parseLinks(doctor);
            doctorIDMap.put(id, true);
        }
    }

    private Profile parseProfile(String id, Element profileElement) {
        String profileText = profileElement.select("p").text();
        String givenNameSign = "Given Name:";
        String surnameSign = "Surname:";
        String formerNameSign = "Former Name:";
        String formerNamesSign = "Former Names:";
        String genderSign = "Gender:";
        String languageSign = "Language Fluency:";

        profileText = profileText.substring(profileText.indexOf(givenNameSign) + givenNameSign.length());
        String givenName = StringUtil.trimValue(profileText.substring(0, profileText.indexOf(surnameSign)));
        profileText = profileText.substring(profileText.indexOf(surnameSign) + surnameSign.length());
        int formerNameIndex = profileText.indexOf(formerNameSign);
        if(formerNameIndex == -1)
            formerNameIndex = profileText.indexOf(formerNamesSign);
        String surname = StringUtil.trimValue(profileText.substring(0, formerNameIndex));
        profileText = profileText.substring(profileText.indexOf(formerNameSign) + formerNameSign.length());
        String formerName = StringUtil.trimValue(profileText.substring(0, profileText.indexOf(genderSign)));
        profileText = profileText.substring(profileText.indexOf(genderSign) + genderSign.length());
        String gender = StringUtil.trimValue(profileText.substring(0, profileText.indexOf(languageSign)));
        profileText = profileText.substring(profileText.indexOf(languageSign) + languageSign.length());
        String[] languageArray = StringUtil.trimValue(profileText).split(",");
        List<String> languageList = new ArrayList<String>();
        for(String language : languageArray) {
            languageList.add(language.trim());
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

    private Registration parseRegistration(Element element) {
        String registrationText = element.select("p").text();
        String classSign = "Registration Class:";
        String certificateIssuedOnSign = "Certificate Issued On:";
        String statusSign = "Registration Status:";
        String effectiveFromSign = "Effective From:";
        String expiryDateSign = "Expiry Date:";
        String graduatedFromSign = "Graduated From:";
        String yearOfGraduationSign = "Year of Graduation:";

        registrationText = registrationText.substring(registrationText.indexOf(classSign) + classSign.length());
        String registrationClass = StringUtil.trimValue(registrationText.substring(0, registrationText.indexOf(certificateIssuedOnSign)));
        registrationText = registrationText.substring(registrationText.indexOf(certificateIssuedOnSign) + certificateIssuedOnSign.length());
        String certificateIssued = StringUtil.trimValue(registrationText.substring(0, registrationText.indexOf(statusSign)));
        registrationText = registrationText.substring(registrationText.indexOf(statusSign) + statusSign.length());
        String registrationStatus = StringUtil.trimValue(registrationText.substring(0, registrationText.indexOf(effectiveFromSign)));
        registrationText = registrationText.substring(registrationText.indexOf(effectiveFromSign) + effectiveFromSign.length());
        String effectiveFrom = null;
        String expiryDate = null;
        if (registrationText.indexOf(expiryDateSign) != -1) {
            effectiveFrom = StringUtil.trimValue(registrationText.substring(0, registrationText.indexOf(expiryDateSign)));
            registrationText = registrationText.substring(registrationText.indexOf(expiryDateSign) + expiryDateSign.length());
            expiryDate = StringUtil.trimValue(registrationText.substring(0, registrationText.indexOf(graduatedFromSign)));
            registrationText = registrationText.substring(registrationText.indexOf(graduatedFromSign) + graduatedFromSign.length());
        }else {
            effectiveFrom = StringUtil.trimValue(registrationText.substring(0, registrationText.indexOf(graduatedFromSign)));
            registrationText = registrationText.substring(registrationText.indexOf(graduatedFromSign) + graduatedFromSign.length());
        }
        String graduatedFrom = StringUtil.trimValue(registrationText.substring(0, registrationText.indexOf(yearOfGraduationSign)));
        registrationText = registrationText.substring(registrationText.indexOf(yearOfGraduationSign) + yearOfGraduationSign.length());
        String yearOfGraduation = StringUtil.trimValue(registrationText);

        Registration registration = new Registration();
        registration.setRegistrationClass(registrationClass);
        registration.setCertificateIssued(certificateIssued);
        registration.setRegistrationStatus(registrationStatus);
        registration.setEffectiveFrom(effectiveFrom);
        registration.setExpiryDate(expiryDate);
        registration.setGraduatedFrom(graduatedFrom);
        registration.setYearOfGraduation(yearOfGraduation);

        return registration;
    }

    private List<Specialty> parseSpecialties(Element element) {
        Elements trs = element.select("table").select("tr");
        List<Specialty> specialtyList = new ArrayList<Specialty>();
        for(int i=1; i<trs.size(); i++) {
            Element tr = trs.get(i);
            Elements tds = tr.select("td");
            String name = null;
            if(tds.size() > 0) {
                name = StringUtil.trimValue(tds.get(0).text());
            }
            String issueOn = null;
            if(tds.size() > 1) {
                issueOn = StringUtil.trimValue(tds.get(1).text());
            }
            String type = null;
            if(tds.size() > 2) {
                type = StringUtil.trimValue(tds.get(2).text());
            }

            Specialty specialty = new Specialty();
            specialty.setName(name);
            specialty.setIssueOn(issueOn);
            specialty.setType(type);
            specialtyList.add(specialty);
        }

        return specialtyList;
    }

    private List<Privilege> parsePrivileges(Element element) {
        String html = element.select("p").html();
        List<Privilege> privilegeList = new ArrayList<Privilege>();
        String[] privilegeArray = html.split("<br>");
        for(String privilegeString : privilegeArray) {
            String hospital = StringUtil.trimValue(privilegeString);
            if(hospital.indexOf("No Privileges reported") == -1) {
                Privilege privilege = new Privilege();
                privilege.setHospitalDetail(hospital);
                privilegeList.add(privilege);
            }
        }

        return privilegeList;
    }

    private void parseLinks(Doctor doctor) throws Exception {
        if(locationLink != null) {
            LocationParse locationParse = new LocationParse(doctor);
            locationParse.accessLocation(locationLink);
        }
        if(registrationDetailLink != null) {
            DetailParse detailParse = new DetailParse(doctor.getRegistration());
            detailParse.accessDetail(registrationDetailLink);
        }
        if(additionalDetailLink != null) {

        }

        recordDoctorJSon(doctor);
    }

    private void recordDoctorJSon(Doctor doctor) throws Exception {
        String doctorJson = mapper.writeValueAsString(doctor);
        log.info(doctorJson);
        FileUtil.appendContent(Constant.JSON_OUTPUT, doctorJson);
    }

}
