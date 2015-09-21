package com.akhaltech.robot;

import com.akhaltech.robot.common.Constant;
import com.akhaltech.robot.common.FileUtil;
import com.akhaltech.robot.parse.SummaryParse;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created by vince on 2015-09-08.
 */
public class DoctorRobot {

    private final static Logger log = Logger.getLogger(DoctorRobot.class);

    private Map<String, Boolean> doctorIDMap = new HashMap<String, Boolean>();

    private CloseableHttpClient httpClient = null;
    private BasicCookieStore cookies = null;
    private HttpContext localContext = null;
    private String cookieString = null;
    private char[] keyword = null;
    private char[] startKeywordTwo = {'a', 'a'};
    private char[] endKeywordTwo = {'z', 'z'};
    private char[] startKeywordFour = {'a', 'a', 'a', 'a'};
    private char[] endKeywordFour = {'z', 'z', 'z', 'z'};
    private int searchRound = 0;

    public DoctorRobot() {
        httpClient = HttpClientBuilder.create().build();
        cookies = new BasicCookieStore();
        localContext = new BasicHttpContext();
        localContext.setAttribute(HttpClientContext.COOKIE_STORE, cookies);
    }

    public void grabDoctorInformation() {
        try {
            boolean loop = true;
            while(loop) {
                // Keyword with 4 letters
                String searchKeyword = generateSearchKeywordFour();
                // Keyword with 2 letters
//                String searchKeyword = generateSearchKeywordTwo();
                if(searchKeyword != null) {
                    log.info("========== Grabing keyword: " + searchKeyword);
                    accessInitialPage();
                    accessBridgePage(searchKeyword);
                    accessSearchResultPage(searchKeyword);
                }else {
                    loop = false;
                }
            }

            // Search with one word
//            String searchKeyword = "Meunier";
//            accessInitialPage();
//            accessBridgePage(searchKeyword);
//            accessSearchResultPage(searchKeyword);
        }catch(Exception e) {
            log.error(e.getMessage());
        }

    }

    private void accessInitialPage() throws Exception {
        HttpGet httpGet = new HttpGet(Constant.INITIAL_DOCTOR_SEARCH_URL);
        try {
            HttpResponse httpResponse = httpClient.execute(httpGet);
            Header[] headers = httpResponse.getAllHeaders();
            for (Header header : headers) {
                String name = header.getName();
                String value = header.getValue();
                if ("Set-Cookie".equalsIgnoreCase(name)) {
                    String[] strs = value.split(";");
                    for (String str : strs) {
                        String[] cookiess = str.split("=");
                        if (cookiess.length == 2) {
                            cookies.addCookie(new BasicClientCookie(cookiess[0], cookiess[1]));
                        } else {
                            cookies.addCookie(new BasicClientCookie(cookiess[0], ""));
                        }
                    }
                }
            }
            httpGet.releaseConnection();
        }finally {
            if (httpGet != null) {
                httpGet.abort();
            }
        }
    }

    private void accessBridgePage(String searchKeyword) throws Exception {
        HttpPost httpPost = new HttpPost(Constant.BRIDGE_URL);
        try {
            httpPost.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            httpPost.setHeader("Accept-Encoding", "gzip, deflate");
            httpPost.setHeader("Accept-Language", "en-US,en;q=0.8,zh-CN;q=0.6,zh;q=0.4,de;q=0.2,fr-FR;q=0.2,fr;q=0.2,ja;q=0.2,zh-TW;q=0.2");
            httpPost.setHeader("Cache-Control", "max-age=0");
            httpPost.setHeader("Connection", "keep-alive");
            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
            httpPost.setHeader("Cookie", getCookieString());
            httpPost.setHeader("Host", "www.cpso.on.ca");
            httpPost.setHeader("Origin", "http://www.cpso.on.ca");
            httpPost.setHeader("Referer", "http://www.cpso.on.ca/Public-Register/All-Doctors-Search");
            httpPost.setHeader("Upgrade-Insecure-Requests", "1");
            httpPost.setHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.85 Safari/537.36");

            List<NameValuePair> paramList = getParams(searchKeyword);
            httpPost.setEntity(new UrlEncodedFormEntity(paramList, "UTF-8"));

            httpClient.execute(httpPost);
            httpPost.releaseConnection();
        }finally {
            if (httpPost != null) {
                httpPost.abort();
            }
        }
    }

    private List<NameValuePair> getParams(String searchKeyword) {
        List<NameValuePair> paramList = new ArrayList<NameValuePair>();
        paramList.add(new BasicNameValuePair("manScript_HiddenField", ""));
        paramList.add(new BasicNameValuePair("__EVENTTARGET", "p$lt$ctl03$pageplaceholder$p$lt$ctl03$AllDoctorsSearch$btnSubmit"));
        paramList.add(new BasicNameValuePair("__EVENTARGUMENT", ""));
        paramList.add(new BasicNameValuePair("__LASTFOCUS", ""));
        paramList.add(new BasicNameValuePair("lng", "en-CA"));
        paramList.add(new BasicNameValuePair("__VIEWSTATEGENERATOR", "A5343185"));
        paramList.add(new BasicNameValuePair("p$lt$ctl00$SearchBox$txtWord", "Site Search"));
        paramList.add(new BasicNameValuePair("p$lt$ctl03$pageplaceholder$p$lt$ctl03$AllDoctorsSearch$txtLastName", searchKeyword));
        paramList.add(new BasicNameValuePair("p$lt$ctl03$pageplaceholder$p$lt$ctl03$AllDoctorsSearch$grpGender", "08"));
        paramList.add(new BasicNameValuePair("p$lt$ctl03$pageplaceholder$p$lt$ctl03$AllDoctorsSearch$grpDocType", "rdoDocTypeAll"));
        paramList.add(new BasicNameValuePair("p$lt$ctl03$pageplaceholder$p$lt$ctl03$AllDoctorsSearch$grpStatus", "rdoStatusActive"));
        paramList.add(new BasicNameValuePair("p$lt$ctl03$pageplaceholder$p$lt$ctl03$AllDoctorsSearch$ddCity", "Select -->"));
        paramList.add(new BasicNameValuePair("p$lt$ctl03$pageplaceholder$p$lt$ctl03$AllDoctorsSearch$txtPostalCode", ""));
        paramList.add(new BasicNameValuePair("p$lt$ctl03$pageplaceholder$p$lt$ctl03$AllDoctorsSearch$ddHospitalCity", "Select -->"));
        paramList.add(new BasicNameValuePair("p$lt$ctl03$pageplaceholder$p$lt$ctl03$AllDoctorsSearch$ddHospitalCity", "-1"));

        return paramList;
    }

    private void accessSearchResultPage(String searchKeyword) throws Exception {
        HttpPost httpPost = new HttpPost(Constant.DOCTOR_SEARCH_URL);
        BufferedReader reader = null;
        try {
            httpPost.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            httpPost.setHeader("Accept-Encoding", "gzip, deflate, sdch");
            httpPost.setHeader("Accept-Language", "en-US,en;q=0.8,zh-CN;q=0.6,zh;q=0.4,de;q=0.2,fr-FR;q=0.2,fr;q=0.2,ja;q=0.2,zh-TW;q=0.2");
            httpPost.setHeader("Cache-Control", "max-age=0");
            httpPost.setHeader("Connection", "keep-alive");
            httpPost.setHeader("Cookie", getCookieString());
            httpPost.setHeader("Host", "www.cpso.on.ca");
            httpPost.setHeader("Referer", "http://www.cpso.on.ca/Public-Register/All-Doctors-Search");
            httpPost.setHeader("Upgrade-Insecure-Requests", "1");
            httpPost.setHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.85 Safari/537.36");

            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity entity = httpResponse.getEntity();
            String line = null;
            reader = new BufferedReader(new InputStreamReader(entity.getContent(), "UTF-8"));
            StringBuilder htmlBuilder = new StringBuilder();
            while((line = reader.readLine()) != null) {
                htmlBuilder.append(line + "\n");
            }

            startToProcessResultSet(htmlBuilder.toString(), searchKeyword);
            searchRound = 0;
        }finally {
            if (httpPost != null) {
                httpPost.abort();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String getCookieString() {
        if(cookieString == null) {
            List<Cookie> list = cookies.getCookies();
            for (Cookie cookie : list) {
                cookieString += cookie.getName() + "=" + cookie.getValue() + ";";
            }
        }
        return cookieString;
    }

    private void startToProcessResultSet(String html, String searchKeyword) {
        Document document = Jsoup.parse(html);
        Element results = document.getElementById("results");
        if(results != null) {
            Elements trs = results.select("table").select("tr");
            if (trs.size() > 1) {
                for (int i = 1; i < trs.size(); i++) {
                    String doctor = trs.get(i).select("td").get(0).text();
                    try {
                        String href = trs.get(i).select("td").get(0).select("a").get(0).attr("href");
                        SummaryParse summaryParse = new SummaryParse(doctorIDMap);
                        summaryParse.accessSummary(Constant.BASE_URL + href);
                    } catch (Exception e) {
                        log.error("========== Got ERROR then Skip Doctor: " + doctor);
                        recordAttensionInfomation("Got ERROR then Skip Doctor: " + doctor);
                        e.printStackTrace();
                    }
                }

                Element next = document.getElementById("next");
                if(next != null) {
                    searchRound++;
                    log.info("========== Keyword: " + searchKeyword + " has more than " + searchRound*25 + " records");
                    Elements as = next.select("a");
                    if(as != null && as.size() > 0) {
                        Element viewState = document.getElementById("__VIEWSTATE");
                        accessSearchResultNextPage(searchKeyword, viewState.attr("value"));
                    }
                }
            }
        }else {
            log.info("==================== NO RECORDS for: " + searchKeyword);
        }
    }

    private void accessSearchResultNextPage(String searchKeyword, String viewStateValue) {
        HttpPost httpPost = new HttpPost(Constant.DOCTOR_SEARCH_NEXT_PAGE_URL);
        BufferedReader reader = null;
        try {
            httpPost.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            httpPost.setHeader("Accept-Encoding", "gzip, deflate");
            httpPost.setHeader("Accept-Language", "en-US,en;q=0.8,zh-CN;q=0.6,zh;q=0.4,de;q=0.2,fr-FR;q=0.2,fr;q=0.2,ja;q=0.2,zh-TW;q=0.2");
            httpPost.setHeader("Cache-Control", "max-age=0");
            httpPost.setHeader("Connection", "keep-alive");
            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
            httpPost.setHeader("Cookie", getCookieString());
            httpPost.setHeader("Host", "www.cpso.on.ca");
            httpPost.setHeader("Origin", "http://www.cpso.on.ca");
            if(searchRound > 1)
                httpPost.setHeader("Referer", "http://www.cpso.on.ca/Public-Register-Info-(1)/Doctor-Search-Results.aspx");
            else
                httpPost.setHeader("Referer", "http://www.cpso.on.ca/Public-Register-Info-(1)/Doctor-Search-Results");
            httpPost.setHeader("Upgrade-Insecure-Requests", "1");
            httpPost.setHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.85 Safari/537.36");

            List<NameValuePair> paramList = getNextPageParams(viewStateValue);
            httpPost.setEntity(new UrlEncodedFormEntity(paramList, "UTF-8"));

            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity entity = httpResponse.getEntity();
            String line = null;
            reader = new BufferedReader(new InputStreamReader(entity.getContent(), "UTF-8"));
            StringBuilder htmlBuilder = new StringBuilder();
            while((line = reader.readLine()) != null) {
                htmlBuilder.append(line + "\n");
            }
            httpPost.abort();

            startToProcessResultSet(htmlBuilder.toString(), searchKeyword);
        }catch(Exception e) {
            log.error(e.getMessage());
            recordAttensionInfomation("Access next page ERROR, Keyword : " + searchKeyword);
        }finally {
            if (httpPost != null) {
                httpPost.abort();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private List<NameValuePair> getNextPageParams(String viewStateValue) {
        List<NameValuePair> paramList = new ArrayList<NameValuePair>();
        paramList.add(new BasicNameValuePair("manScript_HiddenField", ""));
        paramList.add(new BasicNameValuePair("__EVENTTARGET", "p$lt$ctl03$pageplaceholder$p$lt$ctl03$CPSO_DoctorSearchResults$lnkNext"));
        paramList.add(new BasicNameValuePair("__EVENTARGUMENT", ""));
        paramList.add(new BasicNameValuePair("lng", "en-CA"));
        paramList.add(new BasicNameValuePair("__VIEWSTATEGENERATOR", "A5343185"));
        paramList.add(new BasicNameValuePair("p$lt$ctl00$SearchBox$txtWord", "Site Search"));
        paramList.add(new BasicNameValuePair("__VIEWSTATE", viewStateValue));

        return paramList;
    }

    private String generateSearchKeywordTwo() {
        if(keyword == null) {
            keyword = startKeywordTwo;
        }else {
            keyword[1]++;
            if (keyword[1] > endKeywordTwo[1]) {
                keyword[1] = 'a';
                keyword[0]++;
                if (keyword[0] > endKeywordTwo[0]) {
                    return null;
                }
            }
        }

        return String.valueOf(keyword[0]) + String.valueOf(keyword[1]);
    }

    private String generateSearchKeywordFour() {
        if(keyword == null) {
            keyword = startKeywordFour;
        }else {
            keyword[3]++;
            if (keyword[3] > endKeywordFour[3]) {
                keyword[3] = 'a';
                keyword[2]++;
                if (keyword[2] > endKeywordFour[2]) {
                    keyword[2] = 'a';
                    keyword[1]++;
                    if (keyword[1] > endKeywordFour[1]) {
                        keyword[1] = 'a';
                        keyword[0]++;
                        if (keyword[0] > endKeywordFour[0]) {
                            return null;
                        }
                    }
                }
            }
        }

        return String.valueOf(keyword[0]) + String.valueOf(keyword[1]) + String.valueOf(keyword[2]) + String.valueOf(keyword[3]);
    }

    private void recordAttensionInfomation(String content) {
        FileUtil.appendContent(Constant.ATTENSION_OUTPUT, content);
    }

    public static void main(String[] args) {
        log.info("============================== DoctorRobot starts at " + new Date() + " ==============================");
        DoctorRobot dr = new DoctorRobot();
        dr.grabDoctorInformation();
        log.info("============================== DoctorRobot ends at " + new Date() + " ==============================");

//        boolean loop = true;
//        while(loop) {
//            String keyword = dr.generateSearchKeywordTwo();
//            System.out.println(keyword);
//            if(keyword == null)
//                loop = false;
//        }
    }
}
