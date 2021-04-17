package com.itis.infosearch;

import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

import java.util.Map;

import static com.itis.infosearch.Constants.*;

public class TextCrawler extends WebCrawler {

    private final CrawlController controller;
    private final Map<Integer, Document> documents;

    private int pagesCountCurrent;

    public TextCrawler(CrawlController controller, Map<Integer, Document> documents) {
        this.controller = controller;
        this.documents = documents;
        pagesCountCurrent = 0;
    }

    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String href = url.getURL().toLowerCase();
        return !FILTER_FORMAT.matcher(href).matches();
    }

    @Override
    public void visit(Page page) {
        if (pagesCountCurrent == FILE_LIMIT) {
            controller.shutdown();
        } else {
            if (page.getParseData() instanceof HtmlParseData) {
                String rawContent = ((HtmlParseData) page.getParseData()).getText();
                Tokenizer tokenizer = new Tokenizer(rawContent);
                tokenizer.getWordsTokenized();
                if (tokenizer.getTokenizedLength() >= WORD_LIMIT) {
                    ++pagesCountCurrent;
                    documents.put(pagesCountCurrent, new Document(pagesCountCurrent,
                            rawContent, page.getWebURL().getURL(), tokenizer));
                }
            }
        }
    }
}