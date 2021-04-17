package com.itis.infosearch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.itis.infosearch.Constants.FILE_LIMIT;

public class Indexer {

    private double idf;
    private float[] tfs;
    private boolean[] index;
    private Map<Integer, Double> tf_idfs;
    private final List<Integer> documentIds;

    public Indexer() {
        idf = 0.0d;
        index = new boolean[FILE_LIMIT];
        documentIds = new ArrayList<>();
    }

    public Indexer(int id) {
        this();
        index[id - 1] = true;
    }

    private Indexer(boolean[] index) {
        this();
        this.index = index;
    }

    public List<Integer> getDocumentIds() {
        if (documentIds.isEmpty())
            for (int i = 0; i < index.length; i++)
                if (index[i]) documentIds.add(i + 1);
        return documentIds;
    }

    public float[] getTfs() {
        return tfs;
    }

    public double getIdf() {
        return idf;
    }

    public Map<Integer, Double> getTf_Idfs() {
        return tf_idfs;
    }

    public void setTfs(float[] tfs) {
        this.tfs = tfs;
    }

    public void setIdf(double idf) {
        this.idf = idf;
    }

    public void setTf_Idfs(Map<Integer, Double> tf_idfs) {
        this.tf_idfs = tf_idfs;
    }

    public Indexer conjunction(Indexer indexer) {
        Indexer clone = null;
        try {
            clone = this.clone();
            for (int i = 0; i < index.length; i++)
                clone.index[i] &= indexer.index[i];
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return clone;
    }

    public Indexer disjunction(Indexer indexer) {
        Indexer clone = null;
        try {
            clone = this.clone();
            for (int i = 0; i < index.length; i++)
                clone.index[i] |= indexer.index[i];
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return clone;
    }

    public Indexer negation() {
        Indexer clone = null;
        try {
            clone = this.clone();
            for (int i = 0; i < index.length; i++)
                clone.index[i] = !clone.index[i];
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return clone;
    }

    @Override
    protected Indexer clone() throws CloneNotSupportedException {
        return new Indexer(index.clone());
    }
}
