package model;

import java.util.LinkedList;

import structure.Document;
import structure.Relation;
import entry.Loader;

public abstract class Model {
	
	
	protected abstract void init();
	
	protected abstract void train(Loader loader);
	
	protected abstract LinkedList<Relation> predict(LinkedList<Document> docs);
	
	public LinkedList<Relation> run(Loader loader, LinkedList<Document> docs) {
		this.init();
		this.train(loader);
		return this.predict(docs);
	}
}
