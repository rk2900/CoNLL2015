package structure;

import org.json.simple.JSONArray;

public class Relation {

	public Argument arg1;
	public Argument arg2;
	public Argument connective;
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
	
	public JSONArray getSenseList() {
		JSONArray jSenseList = new JSONArray();
		jSenseList.add(sense);
		return jSenseList;
	}
	
}
