package model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

import opennlp.maxent.BasicContextGenerator;
import opennlp.maxent.ContextGenerator;
import opennlp.model.GenericModelReader;
import opennlp.model.MaxentModel;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import structure.Argument;
import structure.Document;
import structure.Relation;
import entry.Loader;

public class ConnDetection extends Model {

	public static String dataFilePath = "./train/conn_features.dat";
	public static String modelFilePath = "./train/conn_featuresModel.txt";
	public static String testFilePath = "./train/conn_classify.test";
	public HashMap<String, String> connCategory;
	public MaxentModel _model;
	public ContextGenerator _cg = new BasicContextGenerator();
	public String seg = "_";
	public Loader loader;

	@Override
	protected void init() {
		connCategory = new HashMap<>();
	}

	@Override
	protected void train(Loader loader) {
		this.loader = loader;
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
					dataFilePath)));

			for (String docId : loader.docs.keySet()) {
				Document document = loader.docs.get(docId);
				for (int i = 0; i < document.sentences.size(); i++) {
					JSONObject sentence = document.getSentence(i);
					JSONArray words = (JSONArray) sentence.get("words");
					for (int j = 0; j < words.size(); j++) {
						int k = getTailPosition(words, j);
						if (k >= 0) { // the word phrase is a connective candidate
							Integer[] firstToken = {-1,-1,-1,i,j};
							Integer[] lastToken = {-1,-1,-1,i,k};
							boolean nonConnFlag = true;
							if (!loader.trainDocData.containsKey(docId));
							else {
								LinkedList<Relation> relList = loader.trainDocData.get(docId);
								for (Relation relation : relList) {
									Integer[] fIdx = relation.connective.tokenList.getFirst();
									Integer[] lIdx = relation.connective.tokenList.getLast();
									if (fIdx[3] == i && fIdx[4] ==j && lIdx[4] == k) {
										nonConnFlag = false;
										break;
									}
								}
							}
							String feature = genFeature(words, firstToken, lastToken, nonConnFlag?"non_connective":"connective");
							bw.write(feature);
							bw.newLine();
							j=k;
						}
					}
				}
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.err.println("\nDetection features generated.");
		trainModel(dataFilePath, modelFilePath);
		System.err.println("\nDetection model trained.");
	}
	
	public int getTailPosition(JSONArray words, int start) {
		int position = -1;
		LinkedList<String> dict = Loader.connDictionary;
		for (String term : dict) {
			String[] termWords = term.split(" ");
			boolean matched = true;
			if(start+termWords.length > words.size()) {
				matched = false;
				break;
			}
			for(int i=0; i<termWords.length; i++) {
				String word = ( (JSONArray) (words.get(start+i))).get(0).toString().toLowerCase();
				if(!termWords[i].equals(word)) {
					matched = false;
					break;
				}
			}
			if(!matched) {
				continue;
			} else {
				position = start+termWords.length-1;
				break;
			}
		}
		return position;
	}

	public String genFeature(JSONArray words, Integer[] firstToken, Integer[] lastToken, String label) {
		StringBuilder sb = new StringBuilder();
		String connStr = "";
		String connPOS = "";
		for(int i=firstToken[4]; i<=lastToken[4]; i++) {
			JSONArray w = (JSONArray)words.get(i);
			if(connStr.length()>0) {
				connStr = connStr+"_";
				connPOS = connPOS+"_";
			}
			connStr = connStr+w.get(0).toString();
			connPOS = connPOS+((JSONObject)w.get(1)).get("PartOfSpeech").toString();
		}
		
		// POS of C
		sb.append("pos_of_c=").append(connPOS).append(" ");

		// prev + C
		// prev POS
		// prev POS + C POS
		if (firstToken[4] > 0) {
			JSONArray prevWord = (JSONArray) words.get(firstToken[4] - 1);
			String prevStr = prevWord.get(0).toString();
			sb.append("prev_c=").append(prevStr).append(seg).append(connStr)
					.append(" ");

			String prevPOS = ((JSONObject) (prevWord.get(1))).get(
					"PartOfSpeech").toString();
			sb.append("pos_of_prev=").append(prevPOS).append(" ");
			sb.append("pos_prev_pos_c=").append(prevPOS).append(seg)
					.append(connPOS).append(" ");
		} else {
			sb.append("prev_c=").append("null").append(seg).append(connStr)
					.append(" ");
			sb.append("pos_of_prev=").append("null").append(" ");
			sb.append("pos_prev_pos_c=").append("null").append(seg)
					.append(connPOS).append(" ");
		}

		// C + next
		// next POS
		// C POS + next POS
		if (lastToken[4] < words.size() - 1) {
			JSONArray nextWord = (JSONArray) words.get(lastToken[4] + 1);
			String nextStr = nextWord.get(0).toString();
			sb.append("c_next=").append(connStr).append(seg).append(nextStr)
					.append(" ");

			String nextPOS = ((JSONObject) (nextWord.get(1))).get(
					"PartOfSpeech").toString();
			sb.append("pos_of_next=").append(nextPOS).append(" ");
			sb.append("pos_c_pos_next=").append(connPOS).append(seg)
					.append(nextPOS).append(" ");
		} else {
			sb.append("c_next=").append(connStr).append(seg).append("null")
					.append(" ");
			sb.append("pos_of_next=").append("null").append(" ");
			sb.append("pos_c_pos_next=").append(connPOS).append(seg)
					.append("null").append(" ");
		}
		sb.append(label);
		return sb.toString();
	}
	
	/**
	 * to judge whether the conn-candidate is a real connective in train data
	 * @return
	 */
	public boolean isConneTrain(int i, int j, String docId) {
		boolean nonConnFlag = true;
		LinkedList<Relation> relList = loader.trainDocData
				.get(docId);
		for (Relation relation : relList) {
			Integer[] idx = relation.connective.tokenList
					.getFirst();
			if (idx[3] == i && idx[4] == j) {
				nonConnFlag = false;
				break;
			}
		}
		return !nonConnFlag;
	}

	@Override
	protected HashMap<String, LinkedList<Relation>> predict(HashMap<String, Document> docs) {
		HashMap<String, LinkedList<Relation>> results = new HashMap<>();
		// load maximum entropy model
		MaxentModel m;
		try {
			m = new GenericModelReader(new File(modelFilePath)).getModel();
			initializeModel(m);
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (String docId : docs.keySet()) {
			Document document = docs.get(docId);
			for (int i = 0; i < document.sentences.size(); i++) {
				JSONObject sentence = document.getSentence(i);
				JSONArray words = (JSONArray) sentence.get("words");
				for (int j = 0; j < words.size(); j++) {
					int k = getTailPosition(words, j);
					
					if(k>0) { // connective dictionary contains the phrase, maybe it is a connective
						Integer[] firstToken = {-1,-1,-1,i,j};
						Integer[] lastToken = {-1,-1,-1,i,k};
						
						String sample = genFeature(words, firstToken, lastToken, "?");
						String feature = sample.substring(0, sample.lastIndexOf(" "));
						if(eval(feature).equals("connective")) { // predict as positive
							Argument conn = new Argument();
							String rawText = "";
							for(int t=j; t<=k; t++) {
								if(rawText.length()>0)
									rawText = rawText+" ";
								rawText = rawText+((JSONArray)words.get(t)).get(0).toString();
								Integer[] tokenTemp = {-1,-1,-1,i,t};
								conn.setToken(tokenTemp);
							}
							conn.setRawText(rawText);
							Relation relation = new Relation();
							relation.setConnective(conn);
							relation.setDocId(docId);
							relation.setSense("Comparison.Concession");
							relation.setType("Explicit");
							if(results.containsKey(docId)) {
								results.get(docId).add(relation);
							} else {
								LinkedList<Relation> list = new LinkedList<>();
								results.put(docId, list);
							}
							document.addDetectionResult(relation);
						} else ; // predict as negative
						j=k;
					} else ; // connective dictionary does not contains the phrase
					
				}
			}
		}
		System.err.println("\nDetection finished.");
		return results;
	}
}
