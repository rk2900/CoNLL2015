package model;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.LinkedList;

import opennlp.maxent.BasicEventStream;
import opennlp.maxent.GIS;
import opennlp.maxent.PlainTextByLineDataStream;
import opennlp.maxent.RealBasicEventStream;
import opennlp.maxent.io.SuffixSensitiveGISModelWriter;
import opennlp.model.AbstractModel;
import opennlp.model.AbstractModelWriter;
import opennlp.model.EventStream;
import opennlp.model.MaxentModel;
import opennlp.model.OnePassDataIndexer;
import opennlp.model.OnePassRealValueDataIndexer;
import opennlp.model.RealValueFileEventStream;
import opennlp.perceptron.PerceptronTrainer;
import structure.Document;
import structure.Relation;
import entry.Loader;

public abstract class Model {
	
	public static boolean USE_SMOOTHING = false;
	public static double SMOOTHING_OBSERVATION = 0.1;
	public MaxentModel _model;
	
	protected abstract void init();
	
	protected abstract void train(Loader loader);
	
	protected abstract HashMap<String, LinkedList<Relation>> predict(HashMap<String, Document> docs);
	
	public HashMap<String, LinkedList<Relation>> run(Loader loader, HashMap<String, Document> docs) {
		this.init();
		this.train(loader);
		return this.predict(docs);
	}
	
	public void trainModel(String dataFilePath, String modelFilePath) {
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
	
	protected String eval (String predicates) {
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
	
	public void initializeModel(MaxentModel m) {
		_model = m;
	}
	
}
