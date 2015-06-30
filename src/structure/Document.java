package structure;

import java.util.LinkedList;

import org.json.simple.JSONObject;

public class Document {
	public String text;
	public LinkedList<JSONObject> sentences;
	public LinkedList<Relation> connResult;
	/*  */
	
	public Document() {
		text = new String();
		sentences = new LinkedList<>();
		connResult = new LinkedList<>();
	}
	
	public Document(String t) {
		text = new String(t);
		sentences = new LinkedList<>();
		connResult = new LinkedList<>();
	}
	
	public void addSentences(JSONObject obj) {
		sentences.add(obj);
	}
	
	public JSONObject getSentence(int index) {
		return sentences.get(index);
	}
	
	public void addDetectionResult(Relation rel) {
		this.connResult.add(rel);
	}
	
}
