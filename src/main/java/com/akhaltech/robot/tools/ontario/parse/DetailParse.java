package com.akhaltech.robot.tools.ontario.parse;

import com.akhaltech.robot.model.PostgraduateTraining;
import com.akhaltech.robot.model.Registration;
import com.akhaltech.robot.model.RegistrationHistory;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vince on 2015-09-08.
 */
public class DetailParse {

    private final static Logger log = Logger.getLogger(DetailParse.class);
    private HttpClient httpClient = null;
    private Registration registration;

    public DetailParse(Registration registration) {
        this.registration = registration;
    }

    public void accessDetail(String url) throws Exception {
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

        parseDetail(htmlBuilder.toString());
    }

    private void parseDetail(String html) {
        Document document = Jsoup.parse(html);
        Element detailDiv = document.getElementById("profile-content");
        Elements detailsH2 = detailDiv.select("h2");
        Elements detailsDivs = detailDiv.select("div[class=detail]");
        Elements detailsTables = detailDiv.select("table");

        String rhSign = "Registration History";
        String ptSign = "Postgraduate Training";

        for(int i=0; i<detailsH2.size(); i++) {
            String title = detailsH2.get(i).text();
            if(title.indexOf(rhSign) != -1 && detailsTables.size() > 0) {
                registration.setHistoryList(parseRegistrationHistory(detailsTables.get(0)));
            }else if(title.indexOf(ptSign) != -1) {
                registration.setTrainingList(parsePostgraduateTraining(detailsDivs.get(detailsDivs.size() - 1)));
            }
        }
    }

    private List<RegistrationHistory> parseRegistrationHistory(Element element) {
        Elements trs = element.select("tr");
        String effectiveSign = "Effective:";
        List<RegistrationHistory> historyList = new ArrayList<RegistrationHistory>();

        for(int i=0; i<trs.size(); i++) {
            Elements tds = trs.get(i).select("td");
            RegistrationHistory registrationHistory = new RegistrationHistory();
            if(tds.size() > 0) {
                registrationHistory.setDescription(StringUtil.trimValue(tds.get(0).text()));
            }
            if(tds.size() > 1) {
                registrationHistory.setEffectiveDate(StringUtil.trimValue(tds.get(1).text().substring(tds.get(1).text().indexOf(effectiveSign) + effectiveSign.length())));
            }
            historyList.add(registrationHistory);
        }

        return historyList;
    }

    private List<PostgraduateTraining> parsePostgraduateTraining(Element element) {
        Elements ps = element.select("p");
        List<PostgraduateTraining> trainingList = new ArrayList<PostgraduateTraining>();

        if(ps.size() > 1) {
            String typeSign = "Type:";
            String disciplineSign = "Discipline:";
            String medicalSchoolSign = "Medical School:";
            String fromSign = "From:";
            String toSign = "To:";

            for(int i=1; i<ps.size(); i++) {
                String trainingText = ps.get(i).text();
                trainingText = trainingText.substring(trainingText.indexOf(typeSign) + typeSign.length());
                String type = StringUtil.trimValue(trainingText.substring(0, trainingText.indexOf(disciplineSign)));
                trainingText = trainingText.substring(trainingText.indexOf(disciplineSign) + disciplineSign.length());
                String discipline = StringUtil.trimValue(trainingText.substring(0, trainingText.indexOf(medicalSchoolSign)));
                trainingText = trainingText.substring(trainingText.indexOf(medicalSchoolSign) + medicalSchoolSign.length());
                String medicalSchool = StringUtil.trimValue(trainingText.substring(0, trainingText.indexOf(fromSign)));
                trainingText = trainingText.substring(trainingText.indexOf(fromSign) + fromSign.length());
                String from = StringUtil.trimValue(trainingText.substring(0, trainingText.indexOf(toSign)));
                trainingText = trainingText.substring(trainingText.indexOf(toSign) + toSign.length());
                String to = StringUtil.trimValue(trainingText);

                PostgraduateTraining postgraduateTraining = new PostgraduateTraining();
                postgraduateTraining.setType(type);
                postgraduateTraining.setDiscipline(discipline);
                postgraduateTraining.setMedicalSchool(medicalSchool);
                postgraduateTraining.setFrom(from);
                postgraduateTraining.setTo(to);
                trainingList.add(postgraduateTraining);
            }
        }

        return trainingList;
    }
}
