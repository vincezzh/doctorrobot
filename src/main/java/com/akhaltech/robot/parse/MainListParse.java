package com.akhaltech.robot.parse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

/**
 * Created by vince on 2015-09-08.
 */
public class MainListParse {
    public static void parseHttpContent(String html) throws IOException {
        Document document = Jsoup.parse(html);
        Element searchResult = document.getElementById("results");

        Elements trs = searchResult.select("table").select("tr");
        for (int i = 1; i < trs.size(); i++) {
            Elements tds = trs.get(i).select("td");
            if(tds.size() > 0) {
                Elements a = tds.get(0).select("a");
                String hrefLink = a.attr("href");
                String doctorName = a.text();
//                accessSummary("http://www.cpso.on.ca" + hrefLink);
            }
            if(tds.size() > 1) {
                String address = tds.get(1).text();
            }
            if(tds.size() > 2) {
                Elements a = tds.get(2).select("a");
                String hrefLink = a.attr("href");
            }
        }
    }
}
