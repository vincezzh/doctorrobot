package com.akhaltech.robot.tools.britishcolumbia;

import com.akhaltech.robot.common.Constant;
import com.akhaltech.robot.common.FileUtil;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private char[] startKeywordTwo = {'a', 'b'};
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
//                String searchKeyword = generateSearchKeywordFour();
                // Keyword with 2 letters
                String searchKeyword = generateSearchKeywordTwo();
                if(searchKeyword != null) {
                    log.info("========== Grabing keyword: " + searchKeyword);
                    String key = accessInitialPage();
//                    accessBridgePage(searchKeyword, key);
//                    accessSearchResultPage(searchKeyword, key);
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
            e.printStackTrace();
        }

    }

    private String accessInitialPage() throws Exception {
        HttpGet httpGet = new HttpGet("https://www.cpsbc.ca/physician_search");
        BufferedReader reader = null;
        String keyValue = null;
        try {
            HttpResponse httpResponse = httpClient.execute(httpGet);

            String strangeValue = "";

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
                            strangeValue = cookiess[1];
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
                        keyValue = keyValueFirstPart.substring(0, index);
                        break;
                    }
                }
            }








            httpGet.setURI(URI.create("https://www.cpsbc.ca/physician_search?filter_first_name=&filter_last_name=ab&filter_city=&filter_gp_or_spec=A&filter_specialty=&filter_accept_new_pat=0&filter_gender=&filter_active=A&filter_radius=&filter_postal_code=&filter_language=&filter_nonce=" + keyValue));
            reader = null;
                httpGet.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
                httpGet.setHeader("Accept-Encoding", "gzip, deflate, sdch");
                httpGet.setHeader("Accept-Language", "en-US,en;q=0.8,zh-CN;q=0.6,zh;q=0.4,de;q=0.2,fr-FR;q=0.2,fr;q=0.2,ja;q=0.2,zh-TW;q=0.2");
                httpGet.setHeader("Cache-Control", "max-age=0");
                httpGet.setHeader("Connection", "keep-alive");
                httpGet.setHeader("Host", "www.cpsbc.ca");
                httpGet.setHeader("Cookie", "SSESS46e5ac66c3cb256f0a441094408a6223=" + strangeValue + "; " + "device=3; device_type=0; has_js=1; __utmt=1; __utma=196404862.1241480227.1445284334.1445284334.1445284334.1; __utmb=196404862.1.10.1445284334; __utmc=196404862; __utmz=196404862.1445284334.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none)");
//            httpPost.setHeader("If-Modified-Since", "Mon, 19 Oct 2015 16:03:16 GMT");
//                httpGet.setHeader("If-None-Match", keyValue);
//            httpPost.setHeader("Referer", "https://www.cpsbc.ca/physician_search");
                  httpGet.setHeader("Pragma", "no-cache");
                httpGet.setHeader("Upgrade-Insecure-Requests", "1");
                httpGet.setHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.85 Safari/537.36");

//            List<NameValuePair> paramList = getParams(searchKeyword);
//            httpPost.setEntity(new UrlEncodedFormEntity(paramList, "UTF-8"));

                httpResponse = httpClient.execute(httpGet);
                entity = httpResponse.getEntity();
                line = null;
                reader = new BufferedReader(new InputStreamReader(entity.getContent(), "UTF-8"));
                StringBuilder htmlBuilder = new StringBuilder();
                while((line = reader.readLine()) != null) {
                    htmlBuilder.append(line + "\n");
                }

                startToProcessResultSet(htmlBuilder.toString(), "ab");










            httpGet.releaseConnection();
        }finally {
            if (httpGet != null) {
                httpGet.abort();
            }
        }

        return keyValue;
    }
//
//    private void accessBridgePage(String searchKeyword) throws Exception {
//        HttpPost httpPost = new HttpPost("https://www.cpsbc.ca/physician_search");
//        try {
//            httpPost.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
//            httpPost.setHeader("Accept-Encoding", "gzip, deflate");
//            httpPost.setHeader("Accept-Language", "en-US,en;q=0.8,zh-CN;q=0.6,zh;q=0.4,de;q=0.2,fr-FR;q=0.2,fr;q=0.2,ja;q=0.2,zh-TW;q=0.2");
//            httpPost.setHeader("Cache-Control", "max-age=0");
//            httpPost.setHeader("Connection", "keep-alive");
////            httpPost.setHeader("Content-Length", "431");
//            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
//            httpPost.setHeader("Cookie", "device=3; device_type=0; SSESS46e5ac66c3cb256f0a441094408a6223=hc9X_qn3EDKJORZjCLWGuwQ3ONKAOc8Uwnwi5WSxWSY; __utmt=1; has_js=1; __utma=196404862.468545606.1445276573.1445276573.1445276573.1; __utmb=196404862.2.10.1445276573; __utmc=196404862; __utmz=196404862.1445276573.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none)");
//            httpPost.setHeader("Host", "www.cpsbc.ca");
//            httpPost.setHeader("Origin", "https://www.cpsbc.ca");
//            httpPost.setHeader("Referer", "https://www.cpsbc.ca/physician_search");
//            httpPost.setHeader("Upgrade-Insecure-Requests", "1");
//            httpPost.setHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36");
//
//            List<NameValuePair> paramList = getParams(searchKeyword);
//            httpPost.setEntity(new UrlEncodedFormEntity(paramList, "UTF-8"));
//
//            httpClient.execute(httpPost);
//            httpPost.releaseConnection();
//        }finally {
//            if (httpPost != null) {
//                httpPost.abort();
//            }
//        }
//    }

//    private List<NameValuePair> getParams(String searchKeyword) {
//        List<NameValuePair> paramList = new ArrayList<NameValuePair>();
//        paramList.add(new BasicNameValuePair("filter[first_name]", ""));
//        paramList.add(new BasicNameValuePair("filter[last_name]", searchKeyword));
//        paramList.add(new BasicNameValuePair("filter[accept_new_pat]", "0"));
//        paramList.add(new BasicNameValuePair("filter[specialty]", ""));
//        paramList.add(new BasicNameValuePair("filter[gp_or_spec]", "A"));
//        paramList.add(new BasicNameValuePair("filter[gender]", ""));
//        paramList.add(new BasicNameValuePair("filter[language]", ""));
//        paramList.add(new BasicNameValuePair("filter[city]", ""));
//        paramList.add(new BasicNameValuePair("filter[radius]", ""));
//        paramList.add(new BasicNameValuePair("filter[postal_code]", ""));
//        paramList.add(new BasicNameValuePair("filter[active]", "A"));
//        paramList.add(new BasicNameValuePair("op", "Search"));
//        paramList.add(new BasicNameValuePair("filter[nonce]", "727286826"));
//        paramList.add(new BasicNameValuePair("form_build_id", "form-C7FsezpymqtDmStJ_oj-fY5zm5p0EX4T1Bo-wHsyFc0"));
//        paramList.add(new BasicNameValuePair("form_id", "college_physio_search_filter_form"));
//        paramList.add(new BasicNameValuePair("c71902e748efe3888c2716588d087126", "237"));
//
//        return paramList;
//    }

    private void accessSearchResultPage(String searchKeyword, String key) throws Exception {
        HttpGet httpPost = new HttpGet("https://www.cpsbc.ca/physician_search?filter_first_name=&filter_last_name=" + searchKeyword + "&filter_city=&filter_gp_or_spec=A&filter_specialty=&filter_accept_new_pat=0&filter_gender=&filter_active=A&filter_radius=&filter_postal_code=&filter_language=&filter_nonce=" + key);
        BufferedReader reader = null;
        try {
            httpPost.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            httpPost.setHeader("Accept-Encoding", "gzip, deflate, sdch");
            httpPost.setHeader("Accept-Language", "en-US,en;q=0.8,zh-CN;q=0.6,zh;q=0.4,de;q=0.2,fr-FR;q=0.2,fr;q=0.2,ja;q=0.2,zh-TW;q=0.2");
            httpPost.setHeader("Cache-Control", "max-age=0");
            httpPost.setHeader("Connection", "keep-alive");
            httpPost.setHeader("Host", "www.cpsbc.ca");
            httpPost.setHeader("Cookie", getCookieString());
//            httpPost.setHeader("If-Modified-Since", "Mon, 19 Oct 2015 16:03:16 GMT");
            httpPost.setHeader("If-None-Match", key);
//            httpPost.setHeader("Referer", "https://www.cpsbc.ca/physician_search");
            httpPost.setHeader("Upgrade-Insecure-Requests", "1");
            httpPost.setHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.85 Safari/537.36");

//            List<NameValuePair> paramList = getParams(searchKeyword);
//            httpPost.setEntity(new UrlEncodedFormEntity(paramList, "UTF-8"));

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
        System.out.println(html);
//        Document document = Jsoup.parse(html);
//        Element results = document.getElementById("results");
//        if(results != null) {
//            Elements trs = results.select("table").select("tr");
//            if (trs.size() > 1) {
//                for (int i = 1; i < trs.size(); i++) {
//                    String doctor = trs.get(i).select("td").get(0).text();
//                    try {
//                        String href = trs.get(i).select("td").get(0).select("a").get(0).attr("href");
//                        SummaryParse summaryParse = new SummaryParse(doctorIDMap);
//                        summaryParse.accessSummary(Constant.BASE_URL + href);
//                    } catch (Exception e) {
//                        log.error("========== Got ERROR then Skip Doctor: " + doctor);
//                        recordAttensionInfomation("Got ERROR then Skip Doctor: " + doctor);
//                        e.printStackTrace();
//                    }
//                }
//
//                Element next = document.getElementById("next");
//                if(next != null) {
//                    searchRound++;
//                    log.info("========== Keyword: " + searchKeyword + " has more than " + searchRound*25 + " records");
//                    Elements as = next.select("a");
//                    if(as != null && as.size() > 0) {
//                        Element viewState = document.getElementById("__VIEWSTATE");
//                        accessSearchResultNextPage(searchKeyword, viewState.attr("value"));
//                    }
//                }
//            }
//        }else {
//            log.info("==================== NO RECORDS for: " + searchKeyword);
//        }
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
