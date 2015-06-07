package entry;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import structure.Relation;

public class Loader {
	
	public HashSet<String> senses;
	public HashMap<String, String> docs;
	public LinkedList<Relation> trainData;
	
	public Loader() {
		initialize();
	}
	
	public void initialize() {
		senses = new HashSet<>();
		docs = new HashMap<>();
		trainData = new LinkedList<>();
	}
	
	public void loadDocuments() {
		
	}
	
	public void loadData() {
		
	}
	
	public void loadParses() {
		
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
