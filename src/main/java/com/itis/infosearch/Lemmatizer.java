package com.itis.infosearch;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.morphology.english.EnglishAnalyzer;
import org.apache.lucene.morphology.russian.RussianAnalyzer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Lemmatizer {

    private final Map<String, Integer> wordsTokenized;
    private final Map<String, Integer> wordsLemmatized;

    private Analyzer russian;
    private Analyzer english;

    public Lemmatizer(Map<String, Integer> wordsTokenized) {
        this.wordsTokenized = wordsTokenized;
        wordsLemmatized = new HashMap<>();
        try {
            russian = new RussianAnalyzer();
            english = new EnglishAnalyzer();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public Map<String, Integer> getWordsLemmatized() {
        if (!wordsLemmatized.isEmpty()) return wordsLemmatized;
        String wordLemmatized;
        TokenStream tokenStream;
        try {
            for (String word : wordsTokenized.keySet()) {
                if (word.charAt(0) <= 255) tokenStream = english.tokenStream(null, word);
                else tokenStream = russian.tokenStream(null, word);
                tokenStream.reset();
                tokenStream.incrementToken();
                wordLemmatized = tokenStream.addAttribute(CharTermAttribute.class).toString();
                wordsLemmatized.put(wordLemmatized, wordsLemmatized.get(wordLemmatized) == null
                        ? wordsTokenized.get(word) : wordsLemmatized.get(wordLemmatized) + wordsTokenized.get(word));
                tokenStream.end();
                tokenStream.close();
            }
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }
        return wordsLemmatized;
    }
}
