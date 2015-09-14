package com.akhaltech.robot.parse;

import com.akhaltech.robot.model.Doctor;
import com.akhaltech.robot.model.Location;
import com.akhaltech.robot.common.StringUtil;
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

/**
 * Created by vince on 2015-09-08.
 */
public class LocationParse {

    private final static Logger log = Logger.getLogger(LocationParse.class);
    private HttpClient httpClient = null;
    private Doctor doctor = null;

    public LocationParse(Doctor doctor) {
        httpClient = HttpClientBuilder.create().build();
        this.doctor = doctor;
    }

    public void accessLocation(String url) throws Exception {
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

        parseLocation(htmlBuilder.toString());
    }

    private void parseLocation(String html) {
        Document document = Jsoup.parse(html);
        Element locationDiv = document.getElementById("profile-content");
        Elements detailsH2 = locationDiv.select("h2");
        Elements detailsDivs = locationDiv.select("div[class=detail]");

        String pplSign = "Primary Practice Location";
        String pciSign = "Professional Corporation Information";
        String mliojSign = "Medical Licenses in Other Jurisdictions";

        Location location = new Location();
        for(int i=0; i<detailsH2.size(); i++) {
            String title = detailsH2.get(i).text();
            if(title.indexOf(pplSign) != -1) {
                parsePrimaryPracticeLocation(location, detailsDivs.get(i));
            }else if(title.indexOf(pciSign) != -1) {
                parseProfessionalCorporationInformation(location, detailsDivs.get(i));
            }else if(title.indexOf(mliojSign) != -1) {
                parseMedicalLicensesInOtherJurisdictions(location, detailsDivs.get(i));
            }
        }

        doctor.setLocation(location);
    }

    private void parsePrimaryPracticeLocation(Location location, Element element) {
        String html = element.select("p").html();
        html = html.replace("<strong>", "").replace("</strong>", "");
        String phoneSign = "Phone:";
        String electoralDistrictSign = "Electoral District:";

        String addressSummary = null;
        String contactSummary = null;
        String electoralDistrict = null;
        if(html.indexOf(phoneSign) != -1) {
            addressSummary = shrankValue(html.substring(0, html.indexOf(phoneSign)).trim());
            contactSummary = shrankValue(html.substring(html.indexOf(phoneSign)).trim());
            if(contactSummary.indexOf(electoralDistrictSign) != -1) {
                electoralDistrict = StringUtil.trimValue(contactSummary.substring(contactSummary.indexOf(electoralDistrictSign) + electoralDistrictSign.length()));
                contactSummary = StringUtil.removeLastComman(StringUtil.trimValue(contactSummary.substring(0, contactSummary.indexOf(electoralDistrictSign))));
            }
        }else {
            addressSummary = shrankValue(html.trim());
            if(addressSummary != null && addressSummary.indexOf(electoralDistrictSign) != -1) {
                electoralDistrict = StringUtil.trimValue(addressSummary.substring(addressSummary.indexOf(electoralDistrictSign) + electoralDistrictSign.length()));
                addressSummary = StringUtil.removeLastComman(StringUtil.trimValue(addressSummary.substring(0, addressSummary.indexOf(electoralDistrictSign))));
            }
        }

        location.setAddressSummary(addressSummary);
        location.setContactSummary(contactSummary);
        location.setElectoralDistrict(electoralDistrict);
    }

    private String shrankValue(String contentArray) {
        String returnString = null;
        for(String content : contentArray.split("<br>")) {
            content = content.replace("&nbsp;", " ").trim();
            if(!content.isEmpty()) {
                if(returnString != null)
                    returnString = returnString + ", " + content.trim();
                else
                    returnString = content.trim();
            }
        }

        return returnString;
    }

    private void parseProfessionalCorporationInformation(Location location, Element element) {
        Elements titleH3 = element.select("h3");
        String issueDateSign = "Issued Date:";

        if(titleH3.size() > 0) {
            String corporationName = titleH3.get(0).select("span").get(0).text();
            String corporationText = element.select("p").get(0).text();
            String corporationIssuedDate = StringUtil.trimValue(corporationText.substring(corporationText.indexOf(issueDateSign) + issueDateSign.length()));

            location.setCorporationName(corporationName);
            location.setCorporationIssuedDate(corporationIssuedDate);
        }
    }

    private void parseMedicalLicensesInOtherJurisdictions(Location location, Element element) {
        String otherJurisdictionsText = element.text();
        String titleSign = "if this is known to the College.";

        String title = StringUtil.trimValue(otherJurisdictionsText.substring(otherJurisdictionsText.indexOf(titleSign) + titleSign.length()));
        location.setMedicalLicensesInOtherJurisdictions(title);
    }
}
