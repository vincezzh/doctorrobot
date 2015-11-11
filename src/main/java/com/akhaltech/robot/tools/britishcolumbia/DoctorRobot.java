package com.akhaltech.robot.tools.britishcolumbia;

import com.akhaltech.robot.common.Constant;
import com.akhaltech.robot.common.FileUtil;
import com.akhaltech.robot.tools.britishcolumbia.parse.SummaryParse;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
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
import java.util.Date;

/**
 * Created by vince on 2015-09-08.
 */
public class DoctorRobot {

    private final static Logger log = Logger.getLogger(DoctorRobot.class);

    private CloseableHttpClient httpClient = null;
    private BasicCookieStore cookies = null;
    private HttpContext localContext = null;
    private String[] params;
    private char[] keyword = null;
    private char[] startKeywordTwo = {'a', 'a'};
    private char[] endKeywordTwo = {'z', 'z'};
    private int currentPage = 0;

    public DoctorRobot() throws Exception {
        cookies = new BasicCookieStore();
        localContext = new BasicHttpContext();
        localContext.setAttribute(HttpClientContext.COOKIE_STORE, cookies);
        params = accessInitialPage();
        if(params.length != 2 || params[0] == null || params[1] == null) {
            throw new Exception();
        }
    }

    public void grabDoctorInformation() {
        try {
            boolean loop = true;
            while(loop) {
                httpClient = HttpClientBuilder.create().build();

                // Keyword with 2 letters
                String searchKeyword = generateSearchKeywordTwo();
                if(searchKeyword != null) {
                    log.info("========== Grabing keyword: " + searchKeyword);
                    accessSearchResultPage(searchKeyword, params[0], params[1]);
                }else {
                    loop = false;
                }
            }

        }catch(Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    private String[] accessInitialPage() throws Exception {
        HttpGet httpGet = new HttpGet(Constant.INITIAL_DOCTOR_SEARCH_URL_BC);
        BufferedReader reader = null;
        String filterNonce = null;
        String cookieKeyValue = null;
        try {
            httpClient = HttpClientBuilder.create().build();
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

                        if("SSESS46e5ac66c3cb256f0a441094408a6223".equals(cookiess[0])) {
                            cookieKeyValue = cookiess[1];
                        }
                    }
                }
            }

            HttpEntity entity = httpResponse.getEntity();
            String line = null;
            reader = new BufferedReader(new InputStreamReader(entity.getContent(), "UTF-8"));

            String keyValueHead = "<input type=\"hidden\" name=\"filter[nonce]\" value=\"";
            String keyValueTail = "\"";
            while((line = reader.readLine()) != null) {
                int index = line.indexOf(keyValueHead);
                if(index != -1) {
                    String keyValueFirstPart = line.substring(index + keyValueHead.length());
                    index = keyValueFirstPart.indexOf(keyValueTail);
                    if(index != -1) {
                        filterNonce = keyValueFirstPart.substring(0, index);
                        break;
                    }
                }
            }

            httpGet.releaseConnection();
        }finally {
            if (httpGet != null) {
                httpGet.abort();
            }
        }

        String[] params = {filterNonce, cookieKeyValue};
        return params;
    }

    private void accessSearchResultPage(String searchKeyword, String filterNonce, String cookieKeyValue) throws Exception {
        currentPage = 0;
        String url = Constant.INITIAL_DOCTOR_SEARCH_URL_BC + "?filter_first_name=&filter_last_name=" + searchKeyword + "&filter_city=&filter_gp_or_spec=A&filter_specialty=&filter_accept_new_pat=0&filter_gender=&filter_active=A&filter_radius=&filter_postal_code=&filter_language=&filter_nonce=" + filterNonce;
        String cookieValue = "SSESS46e5ac66c3cb256f0a441094408a6223=" + cookieKeyValue + "; " + "device=3; device_type=0; has_js=1; __utmt=1; __utma=196404862.1241480227.1445284334.1445284334.1445284334.1; __utmb=196404862.1.10.1445284334; __utmc=196404862; __utmz=196404862.1445284334.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none)";
        HttpGet httpGet = new HttpGet(url);
        BufferedReader reader = null;
        try {
            reader = null;
            httpGet.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            httpGet.setHeader("Accept-Encoding", "gzip, deflate, sdch");
            httpGet.setHeader("Accept-Language", "en-US,en;q=0.8,zh-CN;q=0.6,zh;q=0.4,de;q=0.2,fr-FR;q=0.2,fr;q=0.2,ja;q=0.2,zh-TW;q=0.2");
            httpGet.setHeader("Cache-Control", "max-age=0");
            httpGet.setHeader("Connection", "keep-alive");
            httpGet.setHeader("Cookie", cookieValue);
            httpGet.setHeader("Host", "www.cpsbc.ca");
            httpGet.setHeader("Pragma", "no-cache");
            httpGet.setHeader("Upgrade-Insecure-Requests", "1");
            httpGet.setHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.85 Safari/537.36");

            HttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity entity = httpResponse.getEntity();
            String line = null;
            reader = new BufferedReader(new InputStreamReader(entity.getContent(), "UTF-8"));
            StringBuilder htmlBuilder = new StringBuilder();
            while((line = reader.readLine()) != null) {
                htmlBuilder.append(line + "\n");
            }
            httpGet.releaseConnection();

            Document document = Jsoup.parse(htmlBuilder.toString());
            Element resultTag = document.getElementById("college-physio-search");
            if(resultTag != null) {
                Elements result = resultTag.select("div.error");
                if(result.size() > 0) {
                    String message = result.get(0).text();
                    if(message.indexOf("no results") != -1) {
                        log.info("==================== NO RECORDS for: " + searchKeyword);
                        return;
                    }else if(message.indexOf("too many") != -1) {
                        for(String aLetter : Constant.ALOOP) {
                            accessSearchResultPage(searchKeyword + aLetter, filterNonce, cookieKeyValue);
                        }
                    }
                }else {
                    startToProcessResultSet(document, searchKeyword, url + "&page=" + currentPage, cookieValue);
                }
            }else {
                log.info("==================== NO RECORDS for: " + searchKeyword);
            }
        }finally {
            if (httpGet != null) {
                httpGet.abort();
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

    private void accessSearchResultNextPage(String url, String cookieValue, String searchKeyword) {
        currentPage++;
        HttpGet httpGet = new HttpGet(url);
        BufferedReader reader = null;
        try {
            httpGet.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            httpGet.setHeader("Accept-Encoding", "gzip, deflate");
            httpGet.setHeader("Accept-Language", "en-US,en;q=0.8,zh-CN;q=0.6,zh;q=0.4,de;q=0.2,fr-FR;q=0.2,fr;q=0.2,ja;q=0.2,zh-TW;q=0.2");
            httpGet.setHeader("Connection", "keep-alive");
            httpGet.setHeader("Cookie", cookieValue);
            httpGet.setHeader("Host", "www.cpsbc.ca");
            httpGet.setHeader("Referer", url);
            httpGet.setHeader("Upgrade-Insecure-Requests", "1");
            httpGet.setHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.85 Safari/537.36");

            HttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity entity = httpResponse.getEntity();
            String line = null;
            reader = new BufferedReader(new InputStreamReader(entity.getContent(), "UTF-8"));
            StringBuilder htmlBuilder = new StringBuilder();
            while((line = reader.readLine()) != null) {
                htmlBuilder.append(line + "\n");
            }

            Document document = Jsoup.parse(htmlBuilder.toString());
            startToProcessResultSet(document, searchKeyword, url, cookieValue);
        }catch(Exception e) {
            log.error(e.getMessage());
            recordAttensionInfomation("Access next page ERROR, Keyword : " + searchKeyword);
        }finally {
            if (httpGet != null) {
                httpGet.abort();
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

    private void startToProcessResultSet(Document document, String searchKeyword, String referalURL, String cookieValue) {
        Element resultDIV = document.getElementById("college-physio-search");
        if(resultDIV != null) {
            Elements trs = resultDIV.select("table.sticky-enabled").select("tbody").select("tr");
            if(trs.size() > 0) {
                for (int i = 0; i < trs.size(); i++) {
                    Element a = trs.get(i).select("td").get(0).select("a").first();
                    String doctor = a.text();
                    String url = a.attr("href");

                    try {
                        SummaryParse summaryParse = new SummaryParse(referalURL, doctor, cookieValue);
                        summaryParse.accessSummary(Constant.BASE_URL_BC + url);
                    } catch (Exception e) {
                        log.error("========== Got ERROR then Skip Doctor: " + doctor);
                        recordAttensionInfomation("Got ERROR then Skip Doctor: " + doctor);
                        e.printStackTrace();
                    }
                }

                Element pages = document.getElementById("college-physio-search");
                if(pages != null) {
                    Elements as = pages.select("div.pager-wrapper").select("a");
                    if(as != null && as.size() > 0) {
                        for(Element a : as) {
                            if(a.text().contains("Next")) {
                                log.info("========== Keyword: " + searchKeyword + " has more than " + (currentPage+1)*10 + " records");
                                String href = a.attr("href");
                                accessSearchResultNextPage(href, cookieValue, searchKeyword);
                            }
                        }
                    }
                }
            }
        }else {
            log.info("==================== NO RECORDS for: " + searchKeyword);
        }
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

    private void recordAttensionInfomation(String content) {
        FileUtil.appendContent(Constant.ATTENSION_OUTPUT_BC, content);
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("jsse.enableSNIExtension", "false");

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
