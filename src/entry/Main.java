package entry;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

import model.ConnClassifier;
import model.Model;

import org.json.simple.JSONObject;

import structure.Relation;

public class Main {

	static Collection<Relation> relations = new LinkedList<>();
	
	@SuppressWarnings("unchecked")
	public static void outputResults(String outputFile) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outputFile)));
			for (Relation relation : relations) {
				JSONObject jObject = new JSONObject();
				JSONObject arg1 = new JSONObject();
				arg1.put("TokenList", relation.arg1.getTokenList());
				jObject.put("Arg1", arg1);
				JSONObject arg2 = new JSONObject();
				arg2.put("TokenList", relation.arg2.getTokenList());
				jObject.put("Arg2", arg2);
				JSONObject conn = new JSONObject();
				conn.put("TokenList", relation.connective.getTokenList());
				jObject.put("Connective", conn);
				jObject.put("DocID", relation.docId);
				jObject.put("Sense", relation.getSenseList());
				jObject.put("Type", relation.type);
				bw.write(jObject.toJSONString());
				bw.newLine();
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
		loader.loadDocuments(Const.docFolder_train);
		loader.loadData(Const.data_train);
		loader.loadParses(Const.parses_train);
		loader.loadConnCategory(Const.connCategoryFilePath);
//		System.out.println(loader.docs.get("wsj_0292").getSentence(3));
//		Const.pause();
		System.out.println("Train data loaded.\n");
		
		// loading test data
		/**
		Loader testLoader = new Loader();
		testLoader.initialize();
		testLoader.loadDocuments(Const.docFolder_test);
		testLoader.loadParses(Const.parses_test);
		System.out.println("Test data loaded.\n");
		/**/
		
		// train
		/**/
		Model connClassifier = new ConnClassifier();
		connClassifier.run(loader, new LinkedList<>(loader.docs.values()));
		/**/
		
		Main.relations = (Collection<Relation>) loader.trainData.values();
		// output results
		/**
		Main.outputResults(Const.outputFile);
		/**/
		
		// validate and score
		/**
		for (Relation rel : relations) {
//			System.out.println(rel.type+"\t"+rel.connective.rawText);
			System.out.println(rel.connective.rawText);
		}
		/**/
	}

}
