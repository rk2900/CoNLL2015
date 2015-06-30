package structure;

import org.json.simple.JSONArray;

import entry.Const;

public class Relation {

	public Argument arg1;
	public Argument arg2;
	public Argument connective;
	public Tree connTree;
	public String docId;
	public int id;
	public String sense;
	public String type;
	
	public Relation(Argument arg1, Argument arg2, Argument conn, String docId, int id, String sense, String type) {
		this.arg1 = arg1;
		this.arg2 = arg2;
		this.connective = conn;
		this.docId = docId;
		this.id = id;
		this.sense = sense;
		this.type = type;
	}
	
	public Relation() {
		arg1 = new Argument();
		arg2 = new Argument();
		connective = new Argument();
	}
	
	@SuppressWarnings("unchecked")
	public JSONArray getSenseList() {
		JSONArray jSenseList = new JSONArray();
		jSenseList.add(sense);
		return jSenseList;
	}
	
	public void setConnective(Argument conn) {
		this.connective = conn;
	}
	
	public void setDocId(String did) {
		this.docId = new String(did);
	}
	
	public void setSense(String sense) {
		this.sense = new String(Const.defaultSense);
	}

	public void setType(String string) {
		this.type = new String(Const.defaultType);
		
	}

	public void print() {
		System.out.println(docId);
		System.out.println("\t"+connective.rawText);
		System.out.println("\tArg1: ");
		for (Integer[] token : arg1.tokenList) {
			System.out.println("\t\t"+token[3]+", "+token[4]);
		}
		System.out.println("\tArg1: ");
		for (Integer[] token : arg1.tokenList) {
			System.out.println("\t\t"+token[3]+", "+token[4]);
		}
	}
	
}
