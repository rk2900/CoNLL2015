package model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import javax.print.attribute.DocAttributeSet;
import javax.swing.text.Segment;

import opennlp.maxent.BasicContextGenerator;
import opennlp.maxent.BasicEventStream;
import opennlp.maxent.ContextGenerator;
import opennlp.maxent.GIS;
import opennlp.maxent.PlainTextByLineDataStream;
import opennlp.maxent.RealBasicEventStream;
import opennlp.maxent.io.SuffixSensitiveGISModelWriter;
import opennlp.model.AbstractModel;
import opennlp.model.AbstractModelWriter;
import opennlp.model.EventStream;
import opennlp.model.GenericModelReader;
import opennlp.model.MaxentModel;
import opennlp.model.OnePassDataIndexer;
import opennlp.model.OnePassRealValueDataIndexer;
import opennlp.model.RealValueFileEventStream;
import opennlp.perceptron.PerceptronTrainer;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import structure.Argument;
import structure.Document;
import structure.Relation;
import entry.Const;
import entry.Loader;

public class ConnDetection extends Model {

	public static boolean USE_SMOOTHING = false;
	public static double SMOOTHING_OBSERVATION = 0.1;
	public static String dataFilePath = "./train/conn_features.dat";
	public static String modelFilePath = "./train/conn_featuresModel.txt";
	public static String testFilePath = "./train/conn_classify.test";
	public HashMap<String, String> connCategory;
	public MaxentModel _model;
	public ContextGenerator _cg = new BasicContextGenerator();
	public String seg = "_";
	public Loader loader;

	@Override
	protected void init() {
		connCategory = new HashMap<>();
	}

	@Override
	protected void train(Loader loader) {
		this.loader = loader;
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
					dataFilePath)));

			int trainNum = (int) (loader.docs.keySet().size()*Const.ratio);
			int trainCount = 0;
			for (String docId : loader.docs.keySet()) {
				trainCount++;
				if(Const.splitFlag && (trainCount > trainNum) ) {
					System.err.println("Passed doc. trainNum = "+trainNum+", trainCount = "+trainCount);
					break;
				}
				Document document = loader.docs.get(docId);
				for (int i = 0; i < document.sentences.size(); i++) {
					JSONObject sentence = document.getSentence(i);
					JSONArray words = (JSONArray) sentence.get("words");
					for (int j = 0; j < words.size(); j++) {
						int k = getTailPosition(words, j);
						if (k >= 0) { // the word phrase is a connective candidate
							Integer[] firstToken = {-1,-1,-1,i,j};
							Integer[] lastToken = {-1,-1,-1,i,k};
							boolean nonConnFlag = true;
							if (!loader.trainDocData.containsKey(docId));
							else {
								LinkedList<Relation> relList = loader.trainDocData.get(docId);
								for (Relation relation : relList) {
									Integer[] fIdx = relation.connective.tokenList.getFirst();
									Integer[] lIdx = relation.connective.tokenList.getLast();
									if (fIdx[3] == i && fIdx[4] ==j && lIdx[4] == k) {
										nonConnFlag = false;
										break;
									}
								}
							}
							bw.write(genFeature(words, firstToken, lastToken, nonConnFlag?"non_connective":"connective"));
							bw.newLine();
							j=k;
						}
					}
				}
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.err.println("\nTrain feature generated.");
		trainModel();
		System.err.println("\nModel trained.");
	}
	
	public int getTailPosition(JSONArray words, int start) {
		int position = -1;
		LinkedList<String> dict = Loader.connDictionary;
		for (String term : dict) {
			String[] termWords = term.split(" ");
			boolean matched = true;
			if(start+termWords.length > words.size()) {
				matched = false;
				break;
			}
			for(int i=0; i<termWords.length; i++) {
				String word = ( (JSONArray) (words.get(start+i))).get(0).toString().toLowerCase();
				if(!termWords[i].equals(word)) {
					matched = false;
					break;
				}
			}
			if(!matched) {
				continue;
			} else {
				position = start+termWords.length-1;
				break;
			}
		}
		return position;
	}

	public void trainModel() {
		boolean real = false;
		String type = "maxent";
		AbstractModelWriter writer = null;
		File outputFile = new File(modelFilePath);
		try {
			FileReader datafr = new FileReader(new File(dataFilePath));
			EventStream es;
			if (!real) {
				es = new BasicEventStream(new PlainTextByLineDataStream(datafr));
			} else {
				es = new RealBasicEventStream(new PlainTextByLineDataStream(
						datafr));
			}
			GIS.SMOOTHING_OBSERVATION = SMOOTHING_OBSERVATION;
			AbstractModel model;
			if (type.equals("maxent")) {
				if (!real) {
					model = GIS.trainModel(es, USE_SMOOTHING);
				} else {
					model = GIS.trainModel(100,
							new OnePassRealValueDataIndexer(es, 0),
							USE_SMOOTHING);
				}
				writer = new SuffixSensitiveGISModelWriter(model, outputFile);
			} else if (type.equals("perceptron")) {
				System.err.println("Perceptron training");
				model = new PerceptronTrainer().trainModel(10,
						new OnePassDataIndexer(es, 0), 0);
				writer = new SuffixSensitiveGISModelWriter(model, outputFile);
			} else {
				System.err.println("Unknown model type: " + type);
				model = null;
			}
			writer.persist();
		} catch (Exception e) {
			System.out.println("Unable to create model due to exception: ");
			System.out.println(e);
			e.printStackTrace();
		}
	}

	public String genFeature(JSONArray words, Integer[] firstToken, Integer[] lastToken, String label) {
		StringBuilder sb = new StringBuilder();
		String connStr = "";
		String connPOS = "";
		for(int i=firstToken[4]; i<=lastToken[4]; i++) {
			JSONArray w = (JSONArray)words.get(i);
			if(connStr.length()>0) {
				connStr = connStr+"_";
				connPOS = connPOS+"_";
			}
			connStr = connStr+w.get(0).toString();
			connPOS = connPOS+((JSONObject)w.get(1)).get("PartOfSpeech").toString();
		}
		
		// POS of C
		sb.append("pos_of_c=").append(connPOS).append(" ");

		// prev + C
		// prev POS
		// prev POS + C POS
		if (firstToken[4] > 0) {
			JSONArray prevWord = (JSONArray) words.get(firstToken[4] - 1);
			String prevStr = prevWord.get(0).toString();
			sb.append("prev_c=").append(prevStr).append(seg).append(connStr)
					.append(" ");

			String prevPOS = ((JSONObject) (prevWord.get(1))).get(
					"PartOfSpeech").toString();
			sb.append("pos_of_prev=").append(prevPOS).append(" ");
			sb.append("pos_prev_pos_c=").append(prevPOS).append(seg)
					.append(connPOS).append(" ");
		} else {
			sb.append("prev_c=").append("null").append(seg).append(connStr)
					.append(" ");
			sb.append("pos_of_prev=").append("null").append(" ");
			sb.append("pos_prev_pos_c=").append("null").append(seg)
					.append(connPOS).append(" ");
		}

		// C + next
		// next POS
		// C POS + next POS
		if (lastToken[4] < words.size() - 1) {
			JSONArray nextWord = (JSONArray) words.get(lastToken[4] + 1);
			String nextStr = nextWord.get(0).toString();
			sb.append("c_next=").append(connStr).append(seg).append(nextStr)
					.append(" ");

			String nextPOS = ((JSONObject) (nextWord.get(1))).get(
					"PartOfSpeech").toString();
			sb.append("pos_of_next=").append(nextPOS).append(" ");
			sb.append("pos_c_pos_next=").append(connPOS).append(seg)
					.append(nextPOS).append(" ");
		} else {
			sb.append("c_next=").append(connStr).append(seg).append("null")
					.append(" ");
			sb.append("pos_of_next=").append("null").append(" ");
			sb.append("pos_c_pos_next=").append(connPOS).append(seg)
					.append("null").append(" ");
		}
		sb.append(label);
		return sb.toString();
	}
	
	/**
	 * to judge whether the conn-candidate is a real connective in train data
	 * @return
	 */
	public boolean isConneTrain(int i, int j, String docId) {
		boolean nonConnFlag = true;
		LinkedList<Relation> relList = loader.trainDocData
				.get(docId);
		for (Relation relation : relList) {
			Integer[] idx = relation.connective.tokenList
					.getFirst();
			if (idx[3] == i && idx[4] == j) {
				nonConnFlag = false;
				break;
			}
		}
		return !nonConnFlag;
	}

	public void initializeModel(MaxentModel m) {
		_model = m;
	}
	
	private String eval (String predicates) {
		return eval(predicates,false);
	}

	private String eval(String predicates, boolean real) {
		String[] contexts = predicates.split(" ");
		double[] ocs;
		if (!real) {
			ocs = _model.eval(contexts);
		} else {
			float[] values = RealValueFileEventStream.parseContexts(contexts);
			ocs = _model.eval(contexts, values);
		}
//		System.out.println("For context: " + predicates + "\n" + _model.getAllOutcomes(ocs) + "\n");
		return _model.getBestOutcome(ocs);

	}

	@Override
	protected HashMap<String, LinkedList<Relation>> predict(HashMap<String, Document> docs) {
		HashMap<String, LinkedList<Relation>> results = new HashMap<>();
		// load maximum entropy model
		MaxentModel m;
		try {
			m = new GenericModelReader(new File(modelFilePath)).getModel();
			initializeModel(m);
		} catch (IOException e) {
			e.printStackTrace();
		}
		int trainNum = (int) (docs.keySet().size()*Const.ratio);
		int count = 0;
		for (String docId : docs.keySet()) {
			count++;
			if(Const.splitFlag && (count <= trainNum)) {
				continue;
			}
			Document document = docs.get(docId);
			for (int i = 0; i < document.sentences.size(); i++) {
				JSONObject sentence = document.getSentence(i);
				JSONArray words = (JSONArray) sentence.get("words");
				for (int j = 0; j < words.size(); j++) {
					int k = getTailPosition(words, j);
					
					if(k>0) { // connective dictionary contains the phrase, maybe it is a connective
						Integer[] firstToken = {-1,-1,-1,i,j};
						Integer[] lastToken = {-1,-1,-1,i,k};
						
						String sample = genFeature(words, firstToken, lastToken, "?");
						String feature = sample.substring(0, sample.lastIndexOf(" "));
						if(eval(feature).equals("connective")) { // predict as positive
							Argument conn = new Argument();
							String rawText = "";
							for(int t=j; t<=k; t++) {
								if(rawText.length()>0)
									rawText = rawText+" ";
								rawText = rawText+((JSONArray)words.get(t)).get(0).toString();
								Integer[] tokenTemp = {-1,-1,-1,i,t};
								conn.setToken(tokenTemp);
							}
							conn.setRawText(rawText);
							Relation relation = new Relation();
							relation.setConnective(conn);
							relation.setDocId(docId);
							if(results.containsKey(docId)) {
								results.get(docId).add(relation);
							} else {
								LinkedList<Relation> list = new LinkedList<>();
								results.put(docId, list);
							}
						} else ; // predict as negative
						j=k;
					} else ; // connective dictionary does not contains the phrase
					
				}
			}
		}
		System.err.println("\nPrediction finished.");
		return results;
	}
}
