package model;

import java.util.HashMap;
import java.util.LinkedList;

import structure.Document;
import structure.Relation;
import entry.Loader;

public abstract class Model {
	
	
	protected abstract void init();
	
	protected abstract void train(Loader loader);
	
	protected abstract HashMap<String, LinkedList<Relation>> predict(HashMap<String, Document> docs);
	
	public HashMap<String, LinkedList<Relation>> run(Loader loader, HashMap<String, Document> docs) {
		this.init();
		this.train(loader);
		return this.predict(docs);
	}
}
