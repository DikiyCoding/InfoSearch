package com.itis.infosearch;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

import java.io.File;
import java.util.*;

import static com.itis.infosearch.Constants.FILE_LIMIT;

public class Controller {

    private static boolean isTextCrawler;
    private static boolean isDocumentsCrawled;

    private static String urls;
    private static String startUrl;
    private static MyCrawler myCrawler;
    private static CrawlController controller;
    private static Map<Integer, Document> documents;
    private static Map<String, Indexer> wordsIndexed;

    public static void main(String[] args) throws Exception {
        initValues(args);
        if (isDocumentsCrawled)
            getDocuments();
        else {
            initCrawler();
            crawlDocuments();
            saveDocuments();
        }
        indexDocuments();
        searchDocuments();
        calculateMeasures();
        browseDocuments();
    }

    private static void initValues(String[] args) {
        urls = "";
        startUrl = args[0];
//        isTextCrawler = true;
        isTextCrawler = false;
        isDocumentsCrawled = true;
//        isDocumentsCrawled = false;
        wordsIndexed = new HashMap<>();
        documents = new HashMap<>(FILE_LIMIT);
    }

    private static void initCrawler() throws Exception {
        if (isTextCrawler) {
            CrawlConfig config = new CrawlConfig();
            config.setPolitenessDelay(200);
            config.setCrawlStorageFolder("src/meta/");
            config.setMaxDepthOfCrawling(-1);
            config.setMaxPagesToFetch(-1);
            config.setResumableCrawling(false);
            config.setIncludeBinaryContentInCrawling(false);
            config.setIncludeHttpsPages(true);

            PageFetcher pageFetcher = new PageFetcher(config);
            RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
            RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);

            controller = new CrawlController(config, pageFetcher, robotstxtServer);
            controller.addSeed(startUrl);
        } else {
            myCrawler = new MyCrawler(documents);
        }
    }

    private static void crawlDocuments() {
        if (isTextCrawler) {
            CrawlController.WebCrawlerFactory<TextCrawler> factory = () ->
                    new TextCrawler(controller, documents);
            controller.start(factory, 1);
        } else {
            myCrawler.crawl(startUrl);
        }
    }

    private static void saveDocuments() {
        for (Document document : documents.values()) {
            collectUrls(document);
            saveDocument(document);
            saveData(document);
        }
        saveDocumentList();
    }

    private static void getDocuments() {
        int id;
        int lineNumber;
        String url;
        String rawContent;
        urls = StreamUtils.read(new File("src/index/index.txt"));
        File directory = new File("src/pages");
        File[] files = directory.listFiles();
        if (files != null)
            for (File file : files) {
                id = Integer.parseInt(file.getName().substring(0, file.getName().indexOf(".")));
                lineNumber = urls.indexOf(id + ".");
                lineNumber = urls.indexOf(". http", lineNumber);
                url = urls.substring(lineNumber, urls.indexOf("\n", lineNumber)).substring(urls.indexOf(".") + 1);
                rawContent = StreamUtils.read(file);
                documents.put(id, new Document(id, rawContent, url, new Tokenizer(rawContent)));
            }
    }

    private static void indexDocuments() {
        Set<String> words;
        Indexer wordIndexer;
        Indexer documentIndexer;
        for (Document document : documents.values()) {
            wordIndexer = new Indexer(document.getId());
            words = document.getWordsLemmatized().keySet();
            for (String word : words) {
                documentIndexer = wordsIndexed.get(word);
                wordsIndexed.put(word, documentIndexer == null ?
                        wordIndexer : documentIndexer.disjunction(wordIndexer));
            }
        }
    }

    private static void searchDocuments() {
        boolean isInverted;
        Scanner sc;
        String word;
        String expression;
        String[] operators;
        Indexer wordIndexer;
        Indexer resultIndexer;
        Indexer wordNonExisted;
        Indexer[] wordIndexers;
        List<Integer> conjunctions;
        List<Integer> disjunctions;

        isInverted = false;
        sc = new Scanner(System.in);
        resultIndexer = new Indexer();
        wordNonExisted = new Indexer();
        disjunctions = new ArrayList<>();
        conjunctions = new ArrayList<>();

        System.out.println("\nДля инвертированного поиска доступны три логические операции:" +
                "\n1) \"&\" - конъюнкция (битовое \"и\")," +
                "\n2) \"|\" - дизъюнкция (битовое \"или\")," +
                "\n3) \"!\" - отрицание (битовое \"не\")." +
                "\n\nПримеры использования:" +
                "\nstr1 & str2 | str3\t\tstr1 & !str2 | !str3" +
                "\nstr1 | str2 | str3\t\tstr1 | !str2 | !str3" +
                "\n\n\"exit\" - команда выхода.\n");

        System.out.print("Введите выражение: ");
        expression = sc.nextLine();

        while (!expression.equals("exit")) {
            operators = expression.split(" ");
            wordIndexers = new Indexer[operators.length];
            for (int i = 0; i <= operators.length; i += 2) {
                word = operators[i];
                if (word.startsWith("!")) {
                    isInverted = true;
                    word = word.substring(1);
                }
                wordIndexer = wordsIndexed.get(word);
                wordIndexers[i] = wordIndexer == null ?
                        wordNonExisted : isInverted ?
                        wordIndexer.negation() : wordIndexer;
                if (i == 0) resultIndexer = wordIndexers[i];
                isInverted = false;
            }
            for (int i = 1; i < operators.length; i += 2)
                if (operators[i].equals("&")) conjunctions.add(i);
                else if (operators[i].equals("|")) disjunctions.add(i);
            for (int i : conjunctions) {
                resultIndexer = wordIndexers[i - 1].conjunction(wordIndexers[i + 1]);
                wordIndexers[i - 1] = resultIndexer;
                wordIndexers[i + 1] = resultIndexer;
            }
            for (int i : disjunctions) {
                resultIndexer = wordIndexers[i - 1].disjunction(wordIndexers[i + 1]);
                wordIndexers[i - 1] = resultIndexer;
                wordIndexers[i + 1] = resultIndexer;
            }
            conjunctions.clear();
            disjunctions.clear();
            System.out.println("Ответ: " + resultIndexer.getDocumentIds().toString() + "\n");
            System.out.print("Введите выражение: ");
            expression = sc.nextLine();
        }
        System.out.println();
    }

    private static void calculateMeasures() {
        int countDocuments;
        double idf;
        float[] tfs;
        double[] tf_idfs;
        String strTfs;
        String strIdf;
        String strTf_Idfs;
        String result;
        Indexer indexer;
        Document document;
        Map<Integer, Double> weights;

        result = "";
        strTfs = "tf: [";
        strIdf = "idf: [";
        strTf_Idfs = "tf-idf: [";
        weights = new HashMap<>();
        for (String word : wordsIndexed.keySet()) {
            indexer = wordsIndexed.get(word);
            countDocuments = indexer.getDocumentIds().size();
            tfs = new float[countDocuments];
            tf_idfs = new double[countDocuments];
            idf = Math.log((double) FILE_LIMIT / countDocuments);
            strIdf = strIdf.concat(String.format("%.5f]", idf));
            for (int i = 0; i < countDocuments; i++) {
                document = documents.get(indexer.getDocumentIds().get(i));
                tfs[i] = (float) document.getWordsLemmatized().get(word) / document.getWordCount();
                strTfs = strTfs.concat(String.format("%.5f", tfs[i]))
                        .concat(i == countDocuments - 1 ? "]" : ", ");
                tf_idfs[i] = tfs[i] * idf;
                weights.put(indexer.getDocumentIds().get(i), tf_idfs[i]);
                strTf_Idfs = strTf_Idfs.concat(String.format("%.5f", tf_idfs[i]))
                        .concat(i == countDocuments - 1 ? "]" : ", ");
            }
            indexer.setTfs(tfs);
            indexer.setIdf(idf);
            indexer.setTf_Idfs(weights);
            result = result.concat(String.format("%-100s%-100s%-100s%-100s%-100s%n",
                    "Word: " + word, "Document ids: " + indexer.getDocumentIds().toString(),
                    strTfs, strIdf, strTf_Idfs));
            strTfs = "tf: [";
            strIdf = "idf: [";
            strTf_Idfs = "tf-idf: [";
            weights = new HashMap<>();
        }
        StreamUtils.write("src/measures/measures.txt", result);
    }

    private static void browseDocuments() {
        float tf;
        double idf;
        double numerator;
        double similarity;
        double denominator;
        double queryLength;
        double documentLength;
        double[] queryVector;
        Scanner sc;
        String word;
        Double tf_idf;
        String result;
        String expression;
        Integer wordCounted;
        Indexer wordIndexer;
        Double[] documentVector;
        String[] words;
        MyComparator comparator;
        Map<String, Integer> wordsCounted;
        Map<Integer, Double[]> documentVectors;
        MyComparator.DocumentWeight[] similarities;

        sc = new Scanner(System.in);
        comparator = new MyComparator();
        wordsCounted = new HashMap<>();
        documentVectors = new HashMap<>(FILE_LIMIT);
        similarities = new MyComparator.DocumentWeight[FILE_LIMIT];

        result = "";
        System.out.print("Введите поисковой запрос: ");
        expression = sc.nextLine();

        while (!expression.equals("exit")) {

            result = "";
            queryLength = 0.0d;

            words = expression.split(" ");
            queryVector = new double[words.length];
            for (int i = 1; i <= FILE_LIMIT; i++) {
                documentVector = new Double[words.length];
                for (int j = 0; j < words.length; j++)
                    documentVector[j] = 0.0d;
                documentVectors.put(i, documentVector);
            }
            for (int i = 0; i < words.length; i++) {
                word = words[i];
                wordCounted = wordsCounted.get(word);
                if (wordCounted == null) wordCounted = 0;
                wordsCounted.put(word, ++wordCounted);
            }
            for (int i = 0; i < words.length; i++) {
                word = words[i];
                wordIndexer = wordsIndexed.get(word);
                if (wordIndexer == null) wordIndexer = new Indexer();
                tf = (float) wordsCounted.get(word) / words.length;
                idf = wordIndexer.getIdf();
                queryVector[i] = tf * idf;
                queryLength += Math.pow(queryVector[i], 2);
                for (int id : wordIndexer.getDocumentIds()) {
                    documentVector = documentVectors.get(id);
                    tf_idf = wordIndexer.getTf_Idfs().get(id);
                    if (tf_idf == null) tf_idf = 0.0d;
                    documentVector[i] = tf_idf;
                }
            }
            queryLength = Math.sqrt(queryLength);
            for (int i = 1; i <= FILE_LIMIT; i++) {
                numerator = 0.0d;
                documentLength = 0.0d;
                documentVector = documentVectors.get(i);
                for (int j = 0; j < words.length; j++) {
                    numerator += queryVector[j] * documentVector[j];
                    documentLength += Math.pow(documentVector[j], 2);
                }
                documentLength = Math.sqrt(documentLength);
                denominator = queryLength * documentLength;
                if(denominator == 0.0d) similarity = 0.0d;
                else similarity = numerator / denominator;
                similarities[i - 1] = new MyComparator.DocumentWeight(i,
                        similarity, documents.get(i).getUrl());
            }
            Arrays.sort(similarities, comparator);
            for (int i = 0; i < FILE_LIMIT; i++)
                result = result.concat(String.format("%-10s%-30s%-30s%n",
                        "id " + similarities[i].getId(),
                        "weight: " + similarities[i].getWeight(),
                        "url: " + similarities[i].getUrl()));
            System.out.println(result);

            wordsCounted.clear();

            System.out.print("Введите поисковой запрос: ");
            expression = sc.nextLine();
        }
        StreamUtils.write("src/result/result.txt", result);
    }

    private static void collectUrls(Document document) {
        urls = urls.concat(document.getId() +
                ". " + document.getUrl() + "\n");
    }

    private static void saveDocument(Document document) {
        StreamUtils.write("src/pages/" + document.getId() + ".txt", document.getRawContent());
    }

    private static void saveData(Document document) {
        StreamUtils.write("src/data/" + document.getId() + ".txt",
                document.getWordsLemmatized().toString() + "\n" + document.getWordCount());
    }

    private static void saveDocumentList() {
        StreamUtils.write("src/index/index.txt", urls);
    }
}