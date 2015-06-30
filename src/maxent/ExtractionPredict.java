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
public class ExtractionPredict {
	MaxentModel _model;
	ContextGenerator _cg = new BasicContextGenerator();
	 public static String modelFilePath = "./train/arg_featuresModel.txt",
			 		testFilePath = "./train/arg_features.test";

	public ExtractionPredict(MaxentModel m) {
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
		dataFileName = ExtractionPredict.testFilePath;
		modelFileName = ExtractionPredict.modelFilePath;
		ExtractionPredict predictor = null;
		try {
			MaxentModel m = new GenericModelReader(new File(modelFileName))
					.getModel();
			predictor = new ExtractionPredict(m);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}

		try {
			DataStream ds = new PlainTextByLineDataStream(new FileReader(
					new File(dataFileName)));

			int totalCount = 0;
			int trueCount = 0;
			while (ds.hasNext()) {
				totalCount++;
				String s = (String) ds.nextToken();
				String label = s.substring(s.lastIndexOf(' ') + 1,
						s.length());
				String predict = predictor.eval(
						s.substring(0, s.lastIndexOf(' ')), real);
				if(label.equals(predict)) {
					trueCount++;
				}
				
			}

			double accuracy = 1.0 * trueCount/totalCount;
			System.out.println("Accuracy = "+accuracy);

			return;
		} catch (Exception e) {
			System.out.println("Unable to read from specified file: "
					+ modelFileName);
			System.out.println();
			e.printStackTrace();
		}
	}

}
