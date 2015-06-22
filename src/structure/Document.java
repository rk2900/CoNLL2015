package structure;

import java.util.LinkedList;

import org.json.simple.JSONObject;

public class Document {
	public String text;
	public LinkedList<JSONObject> sentences;
	
	public Document() {
		text = new String();
		sentences = new LinkedList<>();
	}
	
	public Document(String t) {
		text = new String(t);
		sentences = new LinkedList<>();
	}
	
	public void addSentence(JSONObject obj) {
		sentences.add(obj);
	}
	
}
