package com.akhaltech.robot.tools.quebec;

import com.akhaltech.robot.common.Constant;
import com.akhaltech.robot.common.FileUtil;
import com.akhaltech.robot.tools.quebec.parse.SummaryParse;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by vince on 2015-09-08.
 */
public class DoctorRobot {

    private final static Logger log = Logger.getLogger(DoctorRobot.class);

    private CloseableHttpClient httpClient = null;
    private BasicCookieStore cookies = null;
    private HttpContext localContext = null;
    private int startID = 0;
    private int endID = 99999;

    public DoctorRobot() throws Exception {
        cookies = new BasicCookieStore();
        localContext = new BasicHttpContext();
        localContext.setAttribute(HttpClientContext.COOKIE_STORE, cookies);
    }

    public void grabDoctorInformation() {
        try {
            boolean loop = true;
            while(loop) {
                httpClient = HttpClientBuilder.create().build();

                String searchKeyword = generateSearchKeyword();
                if(searchKeyword != null) {
                    log.info("========== Grabing keyword: " + searchKeyword);
                    accessSearchResultPage(searchKeyword);
                }else {
                    loop = false;
                }
            }

        }catch(Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    private List<NameValuePair> getParams(String searchKeyword) {
        List<NameValuePair> paramList = new ArrayList<NameValuePair>();
        paramList.add(new BasicNameValuePair("__EVENTTARGET", ""));
        paramList.add(new BasicNameValuePair("__EVENTARGUMENT", ""));
        paramList.add(new BasicNameValuePair("__VIEWSTATE", ""));
        paramList.add(new BasicNameValuePair("__PREVIOUSPAGE", "5SIzzxqnz3gY_qYJ-BuLR_84gbfjKkwCzJ47eb2u0VgR2mrUlJ2N2HH6iUzmeKhIC157RVuxZ8Ys_asAB4HL-sQWso81"));
        paramList.add(new BasicNameValuePair("txbNoPermis", searchKeyword));
        paramList.add(new BasicNameValuePair("txbNom", ""));
        paramList.add(new BasicNameValuePair("txbPrenom", ""));
        paramList.add(new BasicNameValuePair("DDListSpecialite", "0"));
        paramList.add(new BasicNameValuePair("HFieldSpecialite", ""));
        paramList.add(new BasicNameValuePair("txbVille", ""));
        paramList.add(new BasicNameValuePair("btSubmit", "Search"));
        paramList.add(new BasicNameValuePair("ft$ddlKeywordsSearch", ""));

        return paramList;
    }

    private void accessSearchResultPage(String searchKeyword) throws Exception {
        String url = Constant.INITIAL_DOCTOR_SEARCH_URL_QC;
        HttpPost httpPost = new HttpPost(url);
        BufferedReader reader = null;
        try {
            reader = null;
            httpPost.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            httpPost.setHeader("Accept-Encoding", "gzip, deflate");
            httpPost.setHeader("Accept-Language", "en-US,en;q=0.8,zh-CN;q=0.6,zh;q=0.4,de;q=0.2,fr-FR;q=0.2,fr;q=0.2,ja;q=0.2,zh-TW;q=0.2");
            httpPost.setHeader("Cache-Control", "max-age=0");
            httpPost.setHeader("Connection", "keep-alive");
            httpPost.setHeader("Cookie", "_ga=GA1.2.314019381.1445970943; _gat=1");
            httpPost.setHeader("Host", "www.cmq.org");
            httpPost.setHeader("Origin", "http://www.cmq.org");
            httpPost.setHeader("Referer", "http://www.cmq.org/bottin/index.aspx?lang=en&a=1");
            httpPost.setHeader("Upgrade-Insecure-Requests", "1");
            httpPost.setHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.85 Safari/537.36");

            List<NameValuePair> paramList = getParams(searchKeyword);
            httpPost.setEntity(new UrlEncodedFormEntity(paramList, "UTF-8"));

            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity entity = httpResponse.getEntity();
            String line = null;
            reader = new BufferedReader(new InputStreamReader(entity.getContent(), "UTF-8"));
            StringBuilder htmlBuilder = new StringBuilder();
            while((line = reader.readLine()) != null) {
                htmlBuilder.append(line + "\n");
            }
            httpPost.releaseConnection();

            Document document = Jsoup.parse(htmlBuilder.toString());
            Element resultTag = document.getElementById("GViewList");
            if(resultTag != null) {
                Elements as = resultTag.select("a");
                if(as.size() > 0) {
                    for(Element a : as) {
                        String href = a.attr("href");
                        String doctor = a.text();
                        startToProcessResultSet(href, doctor);
                    }
                }
            }else {
                log.info("==================== NO RECORDS for: " + searchKeyword);
            }
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

    private void startToProcessResultSet(String url, String doctor) {
        try {
            SummaryParse summaryParse = new SummaryParse(Constant.BASE_URL_QC + "/bottin/" + url);
            summaryParse.accessSummary();
        } catch (Exception e) {
            log.error("========== Got ERROR then Skip Doctor: " + doctor);
            recordAttensionInfomation("Got ERROR then Skip Doctor: " + doctor);
            e.printStackTrace();
        }
    }

    private String generateSearchKeyword() {
        startID++;
        if(startID <= endID) {
            String startIDString = String.valueOf(startID);
            for(int i=startIDString.length(); i<5; i++) {
                startIDString = "0" + startIDString;
            }
            return startIDString;
        }else {
            return null;
        }
    }

    private void recordAttensionInfomation(String content) {
        FileUtil.appendContent(Constant.ATTENSION_OUTPUT_QC, content);
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("jsse.enableSNIExtension", "false");

        log.info("============================== DoctorRobot starts at " + new Date() + " ==============================");
        DoctorRobot dr = new DoctorRobot();
        dr.grabDoctorInformation();
        log.info("============================== DoctorRobot ends at " + new Date() + " ==============================");

    }
}
