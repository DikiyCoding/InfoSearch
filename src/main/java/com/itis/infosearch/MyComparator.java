package com.itis.infosearch;

import java.util.Comparator;

import static com.itis.infosearch.Constants.BILLION;

public class MyComparator implements Comparator<MyComparator.DocumentWeight> {

    @Override
    public int compare(MyComparator.DocumentWeight first, MyComparator.DocumentWeight second) {
        return (int) (second.weight * BILLION - first.weight * BILLION);
    }

    public static class DocumentWeight {
        private final int id;
        private final double weight;
        private final String url;

        public DocumentWeight(int id, double weight, String url) {
            this.id = id;
            this.weight = weight;
            this.url = url;
        }

        public int getId() {
            return id;
        }

        public double getWeight() {
            return weight;
        }

        public String getUrl() {
            return url;
        }
    }
}
