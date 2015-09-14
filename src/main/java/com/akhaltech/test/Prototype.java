package com.akhaltech.test;


import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
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
public class Prototype {
    private static String url = "http://qyxy.baic.gov.cn/dito/ditoAction!ycmlFrame.dhtml";
    private static String detailUrl = "http://qyxy.baic.gov.cn/gjjbj/gjjQueryCreditAction!openEntInfo.dhtml?";

    private static String entId;
    private static String credit_ticket;
    private static String entNo;
    private static String type="jyycDiv";

    private static BasicCookieStore cookies = new BasicCookieStore();
    private static HttpClient client = HttpClientBuilder.create().build();

    public static void main(String[] args) {
        getContent();
    }

    /**
     * url即为a链接
     */
    public static String getNextPage(String url, String pageNumber) {

        HttpPost httpPost = null;
        BufferedReader reader = null;
        StringBuilder stringBuilder = null;
        try {
            httpPost = new HttpPost(url);
            List<NameValuePair> list = new ArrayList<NameValuePair>();
            list.add(new BasicNameValuePair("pageNos", pageNumber));
            httpPost.setEntity(new UrlEncodedFormEntity(list, "UTF-8"));
            HttpResponse response = client.execute(httpPost);
            HttpEntity entity = response.getEntity();
            reader = new BufferedReader(new InputStreamReader(entity.getContent(), "UTF-8"));

            String line = null;
            stringBuilder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if ( httpPost != null ) {
                httpPost.abort();
            }
            if ( reader != null ) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return stringBuilder.toString();
    }

    /**
     * 解析b链接所需要的参数
     */
    public static List getParams(String string) {
        List list = new ArrayList();
        Document document = Jsoup.parse(string);
        Elements tds = document.select("table").select("tr").select("td");
        for (int i = 0; i < tds.size(); i++) {
            Elements as = tds.get(i).select("a");
            for (Element a : as) {
                String text = a.attr("onclick");
                String[] strs = text.replace("openEntInfo(", "").replace(");", "").replace("'", "").split(",");
                list.add(strs);
            }
            System.out.println();
        }
        return list;
    }

    /**
     * 请求a链接，并保存Cookies；
     * 再次请求b链接时，将Cookies带上
     */
    public static String getContent() {
        HttpContext localContext = new BasicHttpContext();
        localContext.setAttribute(HttpClientContext.COOKIE_STORE, cookies);
        HttpPost httpPost = new HttpPost(url);

        HttpResponse httpResponse = null;
        Header headers[] = null;
        try {
            httpResponse = client.execute(httpPost);
            headers = httpResponse.getAllHeaders();
            for (Header header : headers) {
                String name = header.getName();
                String value = header.getValue();
                if("Set-Cookie".equalsIgnoreCase(name)) {
                    String[] strs = value.split(";");
                    for (String str : strs) {
                        String[] cookiess = str.split("=");
                        if(cookiess.length == 2) {
                            cookies.addCookie(new BasicClientCookie(cookiess[0], cookiess[1]));
                        }
                        else {
                            cookies.addCookie(new BasicClientCookie(cookiess[0], ""));
                        }
                    }
                }
            }

            httpPost.releaseConnection();

            String cookieStr = "";
            List<Cookie> list = cookies.getCookies();
            for (Cookie cookie : list) {
                cookieStr += cookie.getName() + "=" + cookie.getValue() + ";";
            }
            HttpGet httpGet = new HttpGet(getPath1());

            httpGet.setHeader("Host", "qyxy.baic.gov.cn");
            httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:38.0) Gecko/20100101 Firefox/38.0");
            httpGet.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            httpGet.setHeader("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
            httpGet.setHeader("Accept-Encoding", "gzip, deflate");
            httpGet.setHeader("Referer", "http://qyxy.baic.gov.cn/dito/ditoAction!ycmlFrame.dhtml");
            httpGet.setHeader("Cookie", cookieStr);
            httpGet.setHeader("Connection", "keep-alive");

            HttpResponse httpResponse2 = client.execute(httpGet);
            HttpEntity entity = httpResponse2.getEntity();
            String resultStr = "";
            BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(), "gbk"));
            String line = null;
            while((line = reader.readLine()) != null) {
                resultStr += line + "\n";
            }
            System.out.println(resultStr);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * b链接
     *
     */
    public static String getPath1() {
        List list = getParams(getNextPage(url, "1"));
        String path = null;
        String[] strs = (String[]) list.get(0);
        path = detailUrl + "entId=" + strs[1].trim()
                + "&credit_ticket=" + strs[3].trim() + "&entNo="
                + strs[2].trim() + "&type=" + type + "&timeStamp="
                + getDate();
        return path;
    }

    public static String getDate() {
        return new Date().getTime() + "";
    }
}
