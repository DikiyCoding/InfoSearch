package com.itis.infosearch;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.Map;

import static com.itis.infosearch.Constants.*;

public class MyCrawler {

    private final int STATUS_CODE_OK;
    public final ArrayDeque<String> urls;
    private final Map<Integer, Document> documents;

    private int pagesCountCurrent;

    public MyCrawler(Map<Integer, Document> documents) {
        STATUS_CODE_OK = 200;
        pagesCountCurrent = 0;
        this.documents = documents;
        urls = new ArrayDeque<>(FILE_LIMIT);
    }

    public Map<Integer, Document> crawl(String strUrl) {
        try {
            processUrl(strUrl);
            if (pagesCountCurrent < FILE_LIMIT)
                crawl(urls.pop());
        } catch (IOException exception) {
            if (exception instanceof MalformedURLException) crawl(urls.pop());
            else exception.printStackTrace();
        }
        return documents;
    }

    private void processUrl(String strUrl) throws IOException {
        URL url = new URL(strUrl);
        if (!checkUrl(url)) return;
        if (FILTER_FORMAT.matcher(strUrl).matches()) return;
        String parseUrl = parseUrl(strUrl);
        Tokenizer contentUrl = getUrlContent(parseUrl);
        if (!checkUrlContent(contentUrl)) return;
        documents.put(++pagesCountCurrent, new Document(pagesCountCurrent,
                parseUrl, strUrl, contentUrl));
    }

    private boolean checkUrl(URL url) throws IOException {
        HttpURLConnection httpURLConnection =
                (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod("HEAD");
        httpURLConnection.connect();
        return httpURLConnection.getResponseCode() == STATUS_CODE_OK;
    }

    private String parseUrl(String strUrl) throws IOException {
        return parseJsoup(strUrl);
    }

    private String parseJsoup(String strUrl) throws IOException {
        org.jsoup.nodes.Document document = Jsoup.connect(strUrl).get();
        Elements nodeHrefs = document.select("a");
        for (Element nodeHref : nodeHrefs) {
            String href = nodeHref.attr("href");
            if (!href.isEmpty()) urls.add(href);
            nodeHref.remove();
        }
        Elements nodeScripts = document.select("script");
        for (Element nodeScript : nodeScripts)
            nodeScript.remove();
        Elements nodeCodes = document.select("code");
        for (Element nodeCode : nodeCodes)
            nodeCode.remove();
        return Jsoup.parse(document.html()).text();
    }

    private String parseXPath(URL url) throws Exception {
        org.w3c.dom.Document document = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(url.openConnection()
                        .getInputStream());
        NodeList nodeHrefs = document.getElementsByTagName("a");
        for (int i = 0; i < nodeHrefs.getLength(); i++) {
            String href = getAttribute(nodeHrefs.item(i), "href");
            if (!href.isEmpty()) urls.add(href);
            document.removeChild(nodeHrefs.item(i));
        }
        NodeList nodeScripts = document.getElementsByTagName("script");
        for (int i = 0; i < nodeScripts.getLength(); i++)
            document.removeChild(nodeScripts.item(i));
        return document.toString();
    }

    private String getAttribute(Node node, String attribute) {
        Node nodeAttribute = node.getAttributes()
                .getNamedItem(attribute);
        return nodeAttribute != null ? nodeAttribute.getTextContent() : "";
    }

    private Tokenizer getUrlContent(String parseUrl) {
        return new Tokenizer(parseUrl);
    }

    private boolean checkUrlContent(Tokenizer tokenizer) {
        tokenizer.getWordsTokenized();
        return tokenizer.getTokenizedLength() >= WORD_LIMIT;
    }
}
