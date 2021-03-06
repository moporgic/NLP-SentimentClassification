package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import main.Article;

public class DataSieve {
	// public final static double[] ngramBonus = { 0, 1, 1.01, 1.02, 1.01, 1.02,
	// 1.01, 1.02 };
	public final static double[] ngramBonus = { 0, 0.1, 0.2, 0.3, 0.3, 0.1,
			0.1, 0.2 };
	private int n;
	private ArrayList<Article> views, otherViews;
	private TreeMap<nGram, Integer> viewsMap, otherMap; // total appear count
	private TreeMap<nGram, Integer> viewsCount, otherCount; // #appear in views
	private ArrayList<nGram> xsquare;
	public int ngramCount = 0;

	/**
	 * 
	 * @param n
	 *            n-grams
	 * @param negTrainArticles
	 *            main class
	 * @param posTrainArticles
	 *            other class
	 */
	public DataSieve(int n, ArrayList<Article> negTrainArticles,
			ArrayList<Article> posTrainArticles) {
		this.n = n;
		this.views = negTrainArticles;
		this.otherViews = posTrainArticles;
		sieve();
	}

	public void storeNgram(ArrayList<nGram> t, int kind) {
		nGram e;
		TreeSet<nGram> set = new TreeSet<nGram>();
		int count;
		for (int i = 0; i < t.size(); i++) {
			e = t.get(i);
			count = 1;
			set.add(e);
			if (kind == 1) {
				if (viewsMap.containsKey(e)) {
					count = viewsMap.get(e) + 1;
				}
				viewsMap.put(e, count);
			} else {
				if (otherMap.containsKey(e)) {
					count = otherMap.get(e) + 1;
				}
				otherMap.put(e, count);
			}
		}
		for (nGram ee : set) {
			count = 1;
			if (kind == 1) {
				if (viewsCount.containsKey(ee)) {
					count = viewsCount.get(ee) + 1;
				}
				viewsCount.put(ee, count);
			} else {
				if (otherCount.containsKey(ee)) {
					count = otherCount.get(ee) + 1;
				}
				otherCount.put(ee, count);
			}
		}
	}

	/**
	 * debug example: Score 5.402974 (one of the)
	 * 
	 * @param k
	 *            top-k n-gram
	 */
	public void printBestNgram(int k) {
		for (int i = 0; i < k && i < xsquare.size(); i++) {
			nGram e = xsquare.get(i);
			System.out.printf("Score %f (", e.score);
			for (int j = 0; j < e.iWord.length; j++) {
				System.out
						.printf("%s ", ModelUtilities.getWordName(e.iWord[j]));
			}
			System.out.println(")");
		}
	}

	public ArrayList<nGram> getBestNgram(int k) {
		int[] pickWeight = new int[this.n];
		SubGramSet subNgram = new SubGramSet();
		pickWeight[0] = k;
		for (int i = 0; i < this.n - 1; i++) {
			int part = pickWeight[i] * 7 / 10;
			pickWeight[i + 1] = pickWeight[i] - part;
			pickWeight[i] = part;
			if (pickWeight[i + 1] < 10)
				pickWeight[i + 1] = 10;
		}
		ArrayList<nGram> ret = new ArrayList<nGram>();
		for (int i = 0; ret.size() < k && i < xsquare.size(); i++) {
			nGram e = xsquare.get(i);
			if (!subNgram.contains(e)) {
				int tmp = e.getNonTerminal();
				if (pickWeight[tmp - 1] > 0) {
					pickWeight[tmp - 1]--;
					ret.add(e);
					subNgram.add(e);
				}
			}
		}
		return ret;
	}

	/**
	 * call this sieve(), result will store in xsquare
	 */
	public void sieve() {
		viewsMap = new TreeMap<nGram, Integer>();
		otherMap = new TreeMap<nGram, Integer>();
		viewsCount = new TreeMap<nGram, Integer>();
		otherCount = new TreeMap<nGram, Integer>();

		for (int i = 0; i < views.size(); i++) {
			ArrayList<nGram> t = ModelUtilities.transformNgram(
					views.get(i).content, n);
			storeNgram(t, 1);
		}
		for (int i = 0; i < otherViews.size(); i++) {
			ArrayList<nGram> t = ModelUtilities.transformNgram(
					otherViews.get(i).content, n);
			storeNgram(t, -1);
		}
		ngramCount = viewsMap.size();
		xsquare = new ArrayList<nGram>();
		for (Map.Entry<nGram, Integer> entry : viewsMap.entrySet()) {
			double A, B, C, D;
			A = viewsCount.get(entry.getKey());
			if (otherCount.containsKey(entry.getKey()))
				B = otherCount.get(entry.getKey());
			else
				B = 0;
			C = views.size() - A;
			D = otherViews.size() - B;
			assert (A + C != 0 && B + D != 0 && A + B != 0 && C + D != 0);
			nGram e = entry.getKey();
			double score = entry.getValue() * Math.pow(A * D - C * B, 2)
					/ (A + C) / (B + D) / (A + B) / (C + D);
			score *= ngramBonus[e.getNonTerminal()];
			score *= ModelUtilities.scoreNgram(e);
			e.score = score;
			xsquare.add(e);
		}
		Collections.sort(xsquare, new Comparator<nGram>() {
			@Override
			public int compare(nGram a, nGram b) {
				int c = Double.compare(a.score, b.score);
				if (c == 0) {
					int an = a.getNonTerminal();
					int bn = b.getNonTerminal();
					return Integer.compare(an, bn);
				}
				return -c;
			}
		});
	}
}
