package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.TreeMap;
import java.util.TreeSet;

import model.ModelUtilities;
import model.nGram;

public class Loader {
	private File posFolder, negFolder;
	private File testPosFolder, testNegFolder, testUnknownFolder;
	private File wordFile, stopWordsFile, notWordsFile;
	private File posWordsFile, negWordsFile;
	public TreeMap<String, Integer> wordWeight;
	public TreeSet<String> stopWords, notWords;
	public ArrayList<String> posViews, negViews;
	public ArrayList<String> testPos, testNeg, testUnknown;
	public ArrayList<String> testPosName, testNegName, testUnknownName;

	/**
	 * read path + '/pos' & path + '/neg', single file as one views
	 * 
	 * @param path
	 */
	public Loader(String path) {
		posViews = new ArrayList<String>();
		negViews = new ArrayList<String>();
		testPos = new ArrayList<String>();
		testNeg = new ArrayList<String>();
		testUnknown = new ArrayList<String>();
		testPosName = new ArrayList<String>();
		testNegName = new ArrayList<String>();
		testUnknownName = new ArrayList<String>();

		posFolder = new File(path + "/pos");
		negFolder = new File(path + "/neg");
		testPosFolder = new File(path + "/user_test/pos");
		testNegFolder = new File(path + "/user_test/neg");
		testUnknownFolder = new File(path + "/user_test/unknown");

		listFilesForFolder(posFolder, 1);
		listFilesForFolder(negFolder, -1);
		listFilesForFolder(testPosFolder, 2);
		listFilesForFolder(testNegFolder, -2);
		listFilesForFolder(testUnknownFolder, 0);

		// = { "the", "are", "is", "i", "it", "he", "she", "-", "a", "an" }
		wordFile = new File(path + "/extra/AFINN-111.txt");
		wordWeight = new TreeMap<String, Integer>();
		storeWordWeight(wordFile);
		// System.out.printf("AFINN-111.txt = %d\n", wordWeight.size());
		ModelUtilities.addWordWeight(wordWeight);

//		stopWordsFile = new File(path + "/extra/en.stopnegation-removed.txt");
		stopWordsFile = new File(path + "/extra/stopwords.txt");
		stopWords = new TreeSet<String>();
		storeWords(stopWordsFile, stopWords);
		ModelUtilities.ignoreToken = stopWords;

		notWordsFile = new File(path + "/extra/negation_not.txt");
		notWords = new TreeSet<String>();
		storeWords(notWordsFile, notWords);
		ModelUtilities.notToken = notWords;

		posWordsFile = new File(path + "/extra/pos_word.txt");
		// combineWordsToDoc(posWordsFile, 1);
		negWordsFile = new File(path + "/extra/neg_word.txt");
		// combineWordsToDoc(negWordsFile, -1);
	}

	public void combineWordsToDoc(File f, int kind) {
		if (f != null && f.getName().charAt(0) == '.')
			return;
		TreeMap<String, Integer> wordWeight = new TreeMap<String, Integer>();
		try {
			BufferedReader fin = new BufferedReader(new FileReader(f));
			String line;
			while ((line = fin.readLine()) != null) {
				String[] s = line.split("\\s+");
				if (s.length != 1)
					continue;
				String word = s[0];
				wordWeight.put(word, kind);
			}
			fin.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		ModelUtilities.addWordWeight(wordWeight);
	}

	public void storeWordWeight(File f) {
		if (f != null && f.getName().charAt(0) == '.')
			return;
		try {
			BufferedReader fin = new BufferedReader(new FileReader(f));
			String line;
			while ((line = fin.readLine()) != null) {
				String[] s = line.split("\\s+");
				if (s.length != 2)
					continue;
				String word = s[0];
				int weight = Integer.parseInt(s[1]);
				if (wordWeight.containsKey(word))
					weight += wordWeight.get(word);
				wordWeight.put(word, weight);
			}
			fin.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void storeWords(File f, TreeSet<String> words) {
		if (f != null && f.getName().charAt(0) == '.')
			return;
		try {
			BufferedReader fin = new BufferedReader(new FileReader(f));
			String line;
			while ((line = fin.readLine()) != null) {
				line = line.trim();
				words.add(line);
			}
			fin.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void storeFile(String path, int kind) {
		File f = new File(path);
		if (f != null && f.getName().charAt(0) == '.')
			return;
		try {
			BufferedReader fin = new BufferedReader(new FileReader(f));
			StringBuilder sb = new StringBuilder();
			String line, content = "";
			while ((line = fin.readLine()) != null) {
				sb.append(line);
				sb.append(System.lineSeparator());
			}
			content = sb.toString();
			switch (kind) {
			case 1:
				posViews.add(content);
				break;
			case -1:
				negViews.add(content);
				break;
			case 2:
				testPos.add(content);
				testPosName.add(f.getName());
				break;
			case -2:
				testNeg.add(content);
				testNegName.add(f.getName());
				break;
			case 0:
				testUnknown.add(content);
				testUnknownName.add(f.getName());
				break;
			}
			fin.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void listFilesForFolder(final File folder, int kind) {
		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				listFilesForFolder(fileEntry, kind);
			} else {
				storeFile(fileEntry.getPath(), kind);
			}
		}
	}

	public void writeGram(ArrayList<nGram> p) {
		ArrayList<nGram> mixPick = new ArrayList<nGram>(p);
		try {
			File A;
			PrintWriter printWriter;
			A = new File(Main.outputPath + "/feature/ngram.txt");
			A.getParentFile().mkdirs();

			printWriter = new PrintWriter(A);
			for (int i = 0; i < mixPick.size(); i++) {
				nGram e = mixPick.get(i);
				printWriter.printf("Score ( ");
				for (int j = 0; j < e.iWord.length; j++) {
					printWriter.printf("%s ",
							ModelUtilities.getWordName(e.iWord[j]));
				}
				printWriter.println(")");
			}
			printWriter.close();
		} catch (Exception e) {

		}
	}
}
