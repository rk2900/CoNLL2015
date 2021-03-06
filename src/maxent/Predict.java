package maxent;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.io.FileReader;

import opennlp.maxent.BasicContextGenerator;
import opennlp.maxent.ContextGenerator;
import opennlp.maxent.DataStream;
import opennlp.maxent.PlainTextByLineDataStream;
import opennlp.model.GenericModelReader;
import opennlp.model.MaxentModel;
import opennlp.model.RealValueFileEventStream;

/**
 * Test the model on some input.
 */
public class Predict {
	MaxentModel _model;
	ContextGenerator _cg = new BasicContextGenerator();
	 public static String modelFilePath = "./train/conn_featuresModel.txt",
			 		testFilePath = "./train/conn_features.test";

	public Predict(MaxentModel m) {
		_model = m;
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

	/**
	 * Main method. Call as follows:
	 * <p>
	 * java Predict dataFile (modelFile)
	 */
	public static void main(String[] args) {
		String dataFileName, modelFileName;
		boolean real = false;
		dataFileName = Predict.testFilePath;
		modelFileName = Predict.modelFilePath;
		Predict predictor = null;
		try {
			MaxentModel m = new GenericModelReader(new File(modelFileName))
					.getModel();
			predictor = new Predict(m);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}

		try {
			DataStream ds = new PlainTextByLineDataStream(new FileReader(
					new File(dataFileName)));

			int true_positive = 0;
			int true_negative = 0;
			int false_positive = 0;
			int false_negative = 0;
			while (ds.hasNext()) {
				String s = (String) ds.nextToken();
				String label = s.substring(s.lastIndexOf(' ') + 1,
						s.length());
				String predict = predictor.eval(
						s.substring(0, s.lastIndexOf(' ')), real);
				
				if (label.equals("connective")) {
					if (predict.equals("connective"))
						true_positive++;
					else {
						false_negative++;
					}
				} else {
					if (predict.equals("non_connective"))
						true_negative++;
					else {
						false_positive++;
					}
				}
			}
			System.out.println("TP: " + true_positive);
			System.out.println("FP: " + false_positive);
			System.out.println("TN: " + true_negative);
			System.out.println("FN: " + false_negative);

			double precision = 1.0 * true_positive
					/ (true_positive + false_positive);
			double recall = 1.0 * true_positive
					/ (true_positive + false_negative);
			System.out.println("Precision = " + precision);
			System.out.println("Recall = " + recall);
			System.out.println("F1 Score = "
					+ (2 * precision * recall / (precision + recall)));

			return;
		} catch (Exception e) {
			System.out.println("Unable to read from specified file: "
					+ modelFileName);
			System.out.println();
			e.printStackTrace();
		}
	}

}
