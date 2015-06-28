package model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import structure.Argument;
import structure.Document;
import structure.Relation;
import entry.Loader;

public class ConnClassifier extends Model {

	public HashMap<String, String> connCategory;
	
	@Override
	protected void init() {
		connCategory = new HashMap<>();
	}

	@Override
	protected void train(Loader loader) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File("features.dat")));
			
			for (Relation relation : loader.trainData.values()) {
				Document document = loader.docs.get(relation.docId);
				Argument conn = relation.connective;
				Integer[] token = conn.tokenList.get(0);
				int sentenceOffset = token[3]; // get the sentence offset
				JSONObject sentence = document.getSentence(sentenceOffset);
				if(token[4] == 0 || token[4] == sentence.size()-1) 
					continue;
				JSONArray words = (JSONArray) sentence.get("words");
				
				bw.write(genFeature(words, token, "connective"));
				bw.newLine();
			}
			
			for (String docId : loader.docs.keySet()) {
				Document document = loader.docs.get(docId);
				for (int i=0; i<document.sentences.size(); i++) {
					JSONObject sentence = document.getSentence(i);
					JSONArray words = (JSONArray) sentence.get("words");
					for (int j = 0; j < words.size(); j++) {
						JSONArray word = (JSONArray) words.get(j);
						String wordStr = word.get(0).toString();
						if(loader.connCategory.containsKey(wordStr.toLowerCase())) { // the word is a connective candidate
							boolean nonConnFlag = true;
							if(!loader.trainDocData.containsKey(docId));
							else {
								LinkedList<Relation> relList = loader.trainDocData.get(docId);
								for (Relation relation : relList) {
									Integer[] idx = relation.connective.tokenList.getFirst();
									if(idx[3] == i && idx[4] == j) {
										nonConnFlag = false;
										break;
									}
								}
							}
							if(nonConnFlag) {
								Integer[] index = {-1,-1,-1,i,j};
								bw.write(genFeature(words, index, "non_connective"));
								bw.newLine();
							}
						}
					}
				}
			}
			
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String genFeature(JSONArray words, Integer[] token, String label) {
		StringBuilder sb = new StringBuilder();
		JSONArray connWord = (JSONArray) words.get(token[4]);
		
		String connStr = connWord.get(0).toString();
		
		// POS of C
		String connPOS = ((JSONObject) connWord.get(1)).get("PartOfSpeech").toString();
		sb.append("pos_of_c=").append(connPOS).append(" ");
		
		// prev + C
		// prev POS
		// prev POS + C POS
		if(token[4] > 0) {
			JSONArray prevWord = (JSONArray) words.get(token[4]-1);
			String prevStr = prevWord.get(0).toString();
			sb.append("prev_c=").append(prevStr).append("_").append(connStr).append(" ");
			
			String prevPOS = ((JSONObject) (prevWord.get(1))).get("PartOfSpeech").toString();
			sb.append("pos_of_prev=").append(prevPOS).append(" ");
			sb.append("pos_prev_pos_c=").append(prevPOS).append("_").append(connPOS).append(" ");
		} else {
			sb.append("prev_c=").append("null").append("_").append(connStr).append(" ");
			sb.append("pos_of_prev=").append("null").append(" ");
			sb.append("pos_prev_pos_c=").append("null").append("_").append(connPOS).append(" ");
		}
		
		// C + next
		// next POS
		// C POS + next POS
		if(token[4] < words.size()-1) {
			JSONArray nextWord = (JSONArray) words.get(token[4]+1);
			String nextStr = nextWord.get(0).toString();
			sb.append("c_next=").append(connStr).append("_").append(nextStr).append(" ");

			String nextPOS = ( (JSONObject) (nextWord.get(1)) ).get("PartOfSpeech").toString();
			sb.append("pos_of_next=").append(nextPOS).append(" ");
			sb.append("pos_c_pos_next=").append(connPOS).append("_").append(nextPOS).append(" ");
		} else {
			sb.append("c_next=").append(connStr).append("_").append("null").append(" ");
			sb.append("pos_of_next=").append("null").append(" ");
			sb.append("pos_c_pos_next=").append(connPOS).append("_").append("null").append(" ");
		}
		sb.append(label);
		return sb.toString();
	}

	@Override
	protected LinkedList<Relation> predict(LinkedList<Document> docs) {
		// TODO Auto-generated method stub
		return null;
	}
}
