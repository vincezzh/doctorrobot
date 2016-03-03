package com.akhaltech.robot.common;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Created by vzhang on 03/03/2016.
 */
public class QueryUtil {
    public static String getQuery(String name, String id) {
        String sql = null;
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            InputStream input = loader.getResourceAsStream("query/" + name + ".xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(input);
            doc.getDocumentElement().normalize();

            XPath xpath = XPathFactory.newInstance().newXPath();
            Node node = (Node) xpath.evaluate("//*[@id='" + id + "']", doc, XPathConstants.NODE);
            sql = node.getTextContent().trim();
        }catch (Exception e) {
            e.printStackTrace();
        }

        return sql;
    }
}
