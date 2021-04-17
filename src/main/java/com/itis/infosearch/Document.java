package com.itis.infosearch;

import java.util.Map;

public class Document {

    private final int id;
    private final String url;
    private final String rawContent;
    private final Tokenizer tokenizer;
    private final Lemmatizer lemmatizer;

    public Document(int id, String rawContent, String url, Tokenizer tokenizer) {
        this.id = id;
        this.url = url;
        this.tokenizer = tokenizer;
        this.rawContent = rawContent;
        this.lemmatizer = new Lemmatizer(tokenizer.getWordsTokenized());
    }

    public Map<String, Integer> getWordsTokenized() {
        return tokenizer.getWordsTokenized();
    }

    public Map<String, Integer> getWordsLemmatized() {
        return lemmatizer.getWordsLemmatized();
    }

    public int getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public String getRawContent() {
        return rawContent;
    }

    public int getWordCount() {
        return tokenizer.getTokenizedLength();
    }
}