package entry;

public class Main {

	public static void main(String[] args) {
		// train
		Loader loader = new Loader();
		loader.initialize();
		loader.loadDocuments(Const.docFolder_train);
		loader.loadData(Const.dataFolder_train);
		loader.loadParses(Const.parses_train);
	}

}
