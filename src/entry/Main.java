package entry;

import java.util.HashMap;

import structure.Relation;

public class Main {

	public static void main(String[] args) {
		// loading train data
		Loader loader = new Loader();
		loader.initialize();
		loader.loadDocuments(Const.docFolder_train);
		loader.loadData(Const.data_train);
		loader.loadParses(Const.parses_train);
		
//		System.out.println(loader.senses);
//		System.out.println(loader.types);
		
//		HashMap<Integer, Relation> relations = loader.trainData;
//		for (Integer id : relations.keySet()) {
//			Relation relation = relations.get(id);
//			if(relation.type.equals("Explicit")) {
//				System.out.println(relation.id);
//				System.out.println("\t"+relation.docId);
//				System.out.println("\t"+relation.connective.rawText);
//				System.out.println("\t"+relation.arg1.rawText);
//				System.out.println("\t"+relation.arg1.tokenList);
//				System.out.println("\t"+relation.arg2.rawText);
//				System.out.println("\t"+relation.arg2.tokenList);
//			}
//		}
		
	}

}
