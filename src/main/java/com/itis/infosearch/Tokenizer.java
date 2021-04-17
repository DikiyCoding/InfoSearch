package com.itis.infosearch;

import java.util.*;
import java.util.regex.Pattern;

public class Tokenizer {

    private final Pattern FILTER_SPELLING_RU =
            Pattern.compile("^[а-яА-Я]+$");
    private final Pattern FILTER_SPELLING_EN =
            Pattern.compile("^[a-zA-Z]+$");
    private final String rawContent;

    private String regex;
    private int regexLength;
    private int tokenizedLength;
    private Map<String, Integer> wordsRegex;
    private Map<String, Integer> wordsTokenized;

    public Tokenizer(String rawContent) {
        this.rawContent = rawContent;
        wordsTokenized = new HashMap<>();
    }

    public Map<String, Integer> getWordsRegex(String regex) {
        if (!wordsRegex.isEmpty() && this.regex.equals(regex)) return wordsRegex;
        this.regex = regex;
        String[] wordsRaw = rawContent.split(regex);
        wordsRegex = new HashMap<>(wordsRaw.length);
        for (String word : wordsRaw)
            if (FILTER_SPELLING_EN.matcher(word).matches() ||
                    FILTER_SPELLING_RU.matcher(word).matches()) {
                wordsRegex.put(word, wordsRegex.get(word) == null
                        ? 1 : wordsRegex.get(word) + 1);
                ++regexLength;
            }
        return wordsRegex;
    }

    public Map<String, Integer> getWordsTokenized() {
        if (!wordsTokenized.isEmpty()) return wordsTokenized;
        StringTokenizer tokenizer = new StringTokenizer(rawContent);
        wordsTokenized = new HashMap<>(tokenizer.countTokens());
        for (int i = tokenizer.countTokens(); i > 0; i--) {
            String token = tokenizer.nextToken();
            if (FILTER_SPELLING_EN.matcher(token).matches() ||
                    FILTER_SPELLING_RU.matcher(token).matches()) {
                wordsTokenized.put(token, wordsTokenized.get(token) == null
                        ? 1 : wordsTokenized.get(token) + 1);
                ++tokenizedLength;
            }
        }
        return wordsTokenized;
    }

    public int getRegexLength() {
        return regexLength;
    }

    public int getTokenizedLength() {
        return tokenizedLength;
    }
}
