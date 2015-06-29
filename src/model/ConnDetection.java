package model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

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
import entry.Loader;

public class ConnDetection extends Model {

	public static boolean USE_SMOOTHING = false;
	public static double SMOOTHING_OBSERVATION = 0.1;
	public static String dataFilePath = "./train/conn_features.dat";
	public static String modelFilePath = "./train/conn_classify_model.txt";
	public HashMap<String, String> connCategory;
	public MaxentModel _model;
	public ContextGenerator _cg = new BasicContextGenerator();

	@Override
	protected void init() {
		connCategory = new HashMap<>();
	}

	@Override
	protected void train(Loader loader) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
					dataFilePath)));

			for (Relation relation : loader.trainData.values()) {
				Document document = loader.docs.get(relation.docId);
				Argument conn = relation.connective;
				Integer[] token = conn.tokenList.get(0);
				int sentenceOffset = token[3]; // get the sentence offset
				JSONObject sentence = document.getSentence(sentenceOffset);
				if (token[4] == 0 || token[4] == sentence.size() - 1)
					continue;
				JSONArray words = (JSONArray) sentence.get("words");

				bw.write(genFeature(words, token, "connective"));
				bw.newLine();
			}

			for (String docId : loader.docs.keySet()) {
				Document document = loader.docs.get(docId);
				for (int i = 0; i < document.sentences.size(); i++) {
					JSONObject sentence = document.getSentence(i);
					JSONArray words = (JSONArray) sentence.get("words");
					for (int j = 0; j < words.size(); j++) {
						JSONArray word = (JSONArray) words.get(j);
						String wordStr = word.get(0).toString();
						if (loader.connCategory.containsKey(wordStr
								.toLowerCase())) { // the word is a connective
													// candidate
							boolean nonConnFlag = true;
							if (!loader.trainDocData.containsKey(docId))
								;
							else {
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
							}
							if (nonConnFlag) {
								Integer[] index = { -1, -1, -1, i, j };
								bw.write(genFeature(words, index,
										"non_connective"));
								bw.newLine();
							}
						}
					}
				}
			}

			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		trainModel();

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

	public String genFeature(JSONArray words, Integer[] token, String label) {
		StringBuilder sb = new StringBuilder();
		JSONArray connWord = (JSONArray) words.get(token[4]);

		String connStr = connWord.get(0).toString();

		// POS of C
		String connPOS = ((JSONObject) connWord.get(1)).get("PartOfSpeech")
				.toString();
		sb.append("pos_of_c=").append(connPOS).append(" ");

		// prev + C
		// prev POS
		// prev POS + C POS
		if (token[4] > 0) {
			JSONArray prevWord = (JSONArray) words.get(token[4] - 1);
			String prevStr = prevWord.get(0).toString();
			sb.append("prev_c=").append(prevStr).append("_").append(connStr)
					.append(" ");

			String prevPOS = ((JSONObject) (prevWord.get(1))).get(
					"PartOfSpeech").toString();
			sb.append("pos_of_prev=").append(prevPOS).append(" ");
			sb.append("pos_prev_pos_c=").append(prevPOS).append("_")
					.append(connPOS).append(" ");
		} else {
			sb.append("prev_c=").append("null").append("_").append(connStr)
					.append(" ");
			sb.append("pos_of_prev=").append("null").append(" ");
			sb.append("pos_prev_pos_c=").append("null").append("_")
					.append(connPOS).append(" ");
		}

		// C + next
		// next POS
		// C POS + next POS
		if (token[4] < words.size() - 1) {
			JSONArray nextWord = (JSONArray) words.get(token[4] + 1);
			String nextStr = nextWord.get(0).toString();
			sb.append("c_next=").append(connStr).append("_").append(nextStr)
					.append(" ");

			String nextPOS = ((JSONObject) (nextWord.get(1))).get(
					"PartOfSpeech").toString();
			sb.append("pos_of_next=").append(nextPOS).append(" ");
			sb.append("pos_c_pos_next=").append(connPOS).append("_")
					.append(nextPOS).append(" ");
		} else {
			sb.append("c_next=").append(connStr).append("_").append("null")
					.append(" ");
			sb.append("pos_of_next=").append("null").append(" ");
			sb.append("pos_c_pos_next=").append(connPOS).append("_")
					.append("null").append(" ");
		}
		sb.append(label);
		return sb.toString();
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
		System.out.println("For context: " + predicates + "\n"
				+ _model.getAllOutcomes(ocs) + "\n");
		return _model.getBestOutcome(ocs);

	}

	@Override
	protected HashMap<String, LinkedList<Relation>> predict(
			HashMap<String, Document> docs) {
		HashMap<String, LinkedList<Relation>> results = new HashMap<>();
		
		// load maxent model
		MaxentModel m;
		try {
			m = new GenericModelReader(new File(modelFilePath)).getModel();
			initializeModel(m);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for (String docId : docs.keySet()) {
			Document document = docs.get(docId);
			for (int i = 0; i < document.sentences.size(); i++) {
				JSONObject sentence = document.getSentence(i);
				JSONArray words = (JSONArray) sentence.get("words");
				for (int j = 0; j < words.size(); j++) {
					JSONArray word = (JSONArray) words.get(j);
					String wordStr = word.get(0).toString();
					if (!Loader.connCategory.containsKey(wordStr.toLowerCase()))
						continue;
					else {
						Integer[] token = { -1, -1, -1, i, j };
						String sample = genFeature(words, token, "?");
						String feature = sample.substring(0, sample.lastIndexOf(" "));
						if(eval(feature).equals("connective")) { // result is a connective
							Argument conn = new Argument();
							conn.setRawText(wordStr);
							conn.setToken(token);
							Relation relation = new Relation();
							relation.setConnective(conn);
							if(results.containsKey(docId)) {
								results.get(docId).add(relation);
							} else {
								LinkedList<Relation> list = new LinkedList<>();
								results.put(docId, list);
							}
						} else;
					}
				}
			}
		}

		// TODO Auto-generated method stub
		return results;
	}
}
