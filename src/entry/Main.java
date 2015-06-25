package entry;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

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
		System.out.println("Train data loaded.\n");
		
		// loading test data
		Loader testLoader = new Loader();
		testLoader.initialize();
		testLoader.loadDocuments(Const.docFolder_test);
		testLoader.loadParses(Const.parses_test);
		System.out.println("Test data loaded.\n");
		
		// output results
		Main.relations = (Collection<Relation>) loader.trainData.values();
		Main.outputResults(Const.outputFile);
		
		// validate and score
		for (String string : loader.senses) {
			System.out.println(string);
		}
		
	}

}
