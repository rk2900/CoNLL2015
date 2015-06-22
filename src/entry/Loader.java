package entry;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import basic.FileOps;
import structure.Argument;
import structure.Document;
import structure.Relation;

public class Loader {
	
	public HashSet<String> senses;
	public HashSet<String> types;
	public HashMap<String, Document> docs;
	public HashMap<Integer, Relation> trainData;
	
	public Loader() {
		initialize();
	}
	
	public void initialize() {
		senses = new HashSet<>();
		types = new HashSet<>();
		docs = new HashMap<>();
		trainData = new HashMap<>();
	}

	public void loadDocuments(String docFolder) {
		System.out.println("Loading documents.\t"+docFolder);
		File folder = new File(docFolder);
		File[] files = folder.listFiles();
		for (File file : files) {
			String fileName = file.getName();
			if(!fileName.startsWith("wsj"))
				continue;
			String text = FileOps.LoadFile(file.getPath());
			Document doc = new Document(text);
			docs.put(fileName, doc);
		}
	}
	
	public void loadData(String dataPath) {
		System.out.println("Loading data.\t"+dataPath);
		try {
			BufferedReader bf = new BufferedReader(new FileReader(new File(Const.data_train)));
			JSONParser jParser = new JSONParser();
			String line;
			while((line = bf.readLine()) != null) {
				// read
				JSONObject jObject = (JSONObject) jParser.parse(line);
				JSONObject arg1 = (JSONObject) jObject.get("Arg1");
				JSONObject arg2 = (JSONObject) jObject.get("Arg2");
				JSONObject conn = (JSONObject) jObject.get("Connective");
				String docId = jObject.get("DocID").toString();
				String id = jObject.get("ID").toString();
				String sense = jObject.get("Sense").toString();
				String type = jObject.get("Type").toString();
				
				// parse
				int idNum = Integer.parseInt(id);
				senses.add(sense);
				types.add(type);
				Argument argument1 = parseArgument(arg1);
				Argument argument2 = parseArgument(arg2);
				Argument connective = parseArgument(conn);
				Relation relation = new Relation(argument1, argument2, connective, docId, idNum, sense, type);
				trainData.put(idNum, relation);
			}
			bf.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	public void loadParses(String parseFilePath) {
		System.out.println("Loading parses.\t"+parseFilePath);
		JSONParser jParser = new JSONParser();
		try {
			BufferedReader bf = new BufferedReader(new FileReader(new File(parseFilePath)));
			String line;
			while((line = bf.readLine()) != null) {
				JSONObject jObject = (JSONObject) jParser.parse(line);
				Set<String> docSet = jObject.keySet();
				for (String docKey : docSet) {
					JSONArray doc = (JSONArray) ((JSONObject) jObject.get(docKey)).get("sentences");
					Document document = docs.get(docKey);
					
					for (Object sentence : doc) {
						document.addSentence((JSONObject) sentence);
					}
				}
			}
			bf.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	private Argument parseArgument(JSONObject arg) {
		Argument argument = new Argument();
		
		// CharacterSpanList
		JSONArray characterSpanList = (JSONArray) arg.get("CharacterSpanList");
		argument.setCharacterSpanList(characterSpanList);
		
		// RawText
		String rawText = arg.get("RawText").toString();
		argument.setRawText(rawText);
		
		// TokenList
		JSONArray tokenList = (JSONArray) arg.get("TokenList");
		if(tokenList != null) {
			for (Object object : tokenList) {
				argument.setToken((JSONArray) object);
			}
		}
		
		return argument;
	}

}
