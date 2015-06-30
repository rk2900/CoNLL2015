package structure;

import java.util.LinkedList;

import org.json.simple.JSONArray;

import basic.format.Pair;

public class Argument {
	public LinkedList<Pair<Integer, Integer>> characterSpanList;
	public String rawText;
	public LinkedList<Integer[]> tokenList; 
//	0 character offset begin, 
//	1 character offset end, 
//	2 token offset within the document, 
//	3 sentence offset, 
//	4 token offset within the sentence
	
	public Argument() {
		initialize();
	}
	
	public void initialize() {
		characterSpanList = new LinkedList<>();
		tokenList = new LinkedList<>();
	}
	
	public boolean setCharacterSpanList(JSONArray array) {
		for (Object object : array) {
			JSONArray spanList = (JSONArray) object;
			Pair<Integer, Integer> p = new Pair<Integer, Integer>(Integer.parseInt(spanList.get(0).toString()), Integer.parseInt(spanList.get(1).toString()));
			characterSpanList.add(p);
		}
		if(characterSpanList.size() > 0) 
			return true;
		else
			return false;
	}
	
	public void setRawText(String text) {
		rawText = new String(text);
	}
	
	public void setToken(JSONArray tokenArray) {
		Integer[] token = {Integer.parseInt(tokenArray.get(0).toString()),
							Integer.parseInt(tokenArray.get(1).toString()),
							Integer.parseInt(tokenArray.get(2).toString()),
							Integer.parseInt(tokenArray.get(3).toString()),
							Integer.parseInt(tokenArray.get(4).toString()),};
		tokenList.add(token);
	}
	
	public void setToken(Integer[] token) {
		tokenList.add(token);
	}
	
	@SuppressWarnings("unchecked")
	public JSONArray getTokenList() {
		JSONArray jTokenList = new JSONArray();
		for (Integer[] token : tokenList) {
			jTokenList.add(token[2]);
		}
		return jTokenList;
	}
	
	/**
	 * to judge whether the arguments/connective contains multi words in the sentence.
	 * @return
	 */
	public boolean ifMultiWords() {
		return (tokenList.getFirst())[4] != (tokenList.getLast())[4];
	}
	
}
