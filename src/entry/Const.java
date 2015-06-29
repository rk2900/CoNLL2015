package entry;

import java.util.Scanner;

public class Const {
	// train
	public static final String dataFolder_train = "./data/conll15-st-03-04-15-train/";
	public static final String docFolder_train = dataFolder_train+"raw/";
	public static final String data_train = dataFolder_train+"pdtb-data.json";
	public static final String parses_train = dataFolder_train+"pdtb-parses.json";
	
	// test
	public static final String dataFolder_test = "./data/conll15-st-03-04-15-dev/";
	public static final String docFolder_test = dataFolder_test+"raw/";
	public static final String parses_test = dataFolder_test+"pdtb-parses.json";

	// output
	public static final String outputFile = "./output.json";
	
	// configurations
	public static final boolean modelEval = true;
	public static final boolean explicitFlag = true;
	public static final boolean splitFlag = false;
	public static final String connCategoryFilePath = "./data/connective-category-mod.txt";
	public static final double ratio = 0.8;
	
	
	public static void pause() {
		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(System.in);
		scanner.next();
	}
	
}
