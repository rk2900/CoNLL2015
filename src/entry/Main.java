package entry;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;

import model.ArgumentExtraction;
import model.ConnDetection;
import model.Model;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import structure.Argument;
import structure.Document;
import structure.Relation;

public class Main {

	@SuppressWarnings("unchecked")
	public static void outputResults(String outputFile, HashMap<String, LinkedList<Relation>> results) {
		Comparator<Integer> comp = new Comparator<Integer>() {

			@Override
			public int compare(Integer a, Integer b) {
				if(a>b) 
					return 1;
				else if(a<b)
					return -1;
				else {
					return 0;
				}
			}
			
		};
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outputFile)));
			for (String docId : results.keySet()) {
				for (Relation relation : results.get(docId)) {
					JSONObject jObject = new JSONObject();
					
					JSONObject arg1 = new JSONObject();
					JSONArray arg1Tokens = new JSONArray();
					for (Integer[] token : relation.arg1.tokenList) {
						arg1Tokens.add(token[4]);
					}
					arg1Tokens.sort(comp);
					arg1.put("TokenList", arg1Tokens);
					jObject.put("Arg1", arg1);
					
					JSONObject arg2 = new JSONObject();
					JSONArray arg2Tokens = new JSONArray();
					for (Integer[] token : relation.arg2.tokenList) {
						arg2Tokens.add(token[4]);
					}
					arg2Tokens.sort(comp);
					arg2.put("TokenList", arg2Tokens);
					jObject.put("Arg2", arg2);
					
					JSONObject conn = new JSONObject();
					JSONArray connTokens = new JSONArray();
					for (Integer[] token : relation.connective.tokenList) {
						connTokens.add(token[4]);
					}
					connTokens.sort(comp);
					conn.put("TokenList", connTokens);
					jObject.put("Connective", conn);
					
					if(arg1Tokens.size()==0 || arg2Tokens.size()==0 || connTokens.size()==0) {
						continue;
					}
					jObject.put("DocID", relation.docId);
					jObject.put("Sense", relation.getSenseList());
					jObject.put("Type", relation.type);
					bw.write(jObject.toJSONString());
					bw.newLine();
				}
			}
			
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		// loading train data
		Loader loader = new Loader();
		loader.initialize();
		loader.loadConnCategory(Const.connCategoryFilePath);
		loader.loadDocuments(Const.docFolder_train);
		loader.loadData(Const.data_train);
		loader.loadParses(Const.parses_train);
		System.out.println("Train data loaded.\n");
		
		// loading test data
		Loader testLoader = new Loader();
		testLoader.initialize();
		testLoader.loadDocuments(Const.docFolder_test);
		testLoader.loadParses(Const.parses_test);
		System.out.println("Test data loaded.\n");
		
		// connective detection
		Model connDetector = new ConnDetection();
		connDetector.run(loader, testLoader.docs);
		Model argExtractor = new ArgumentExtraction();
		HashMap<String, LinkedList<Relation>> results = argExtractor.run(loader, testLoader.docs);
		
		// output results
		Main.setGlobalIndex(testLoader, results);
		Main.outputResults(Const.outputFile, results);
		
	}

	private static void setGlobalIndex(Loader loader,
			HashMap<String, LinkedList<Relation>> results) {
		for (String docId : results.keySet()) {
			Document document = loader.docs.get(docId);
			for (Relation relation : results.get(docId)) {
				relation.connective = setIndex(document, relation.connective);
				relation.arg1 = setIndex(document, relation.arg1);
				relation.arg2 = setIndex(document, relation.arg2);
			}
		}
	}
	
	private static Argument setIndex(Document document, Argument argument) {
		int[] sentencesLength = new int[document.sentences.size()];
		for(int i=0; i<document.sentences.size(); i++) {
			int sentenceLength = ((JSONArray)(document.getSentence(i).get("words"))).size();
			sentencesLength[i] = sentenceLength;
		}
		for (Integer[] token : argument.tokenList) {
			int position = 0;
			int senIdx = token[3];
			int wordIdx = token[4];
			for(int k=0; k<senIdx; k++) {
				position+=sentencesLength[k];
			}
			position+=wordIdx;
			token[4] = position;
		}
		
		return argument;
	}

}
