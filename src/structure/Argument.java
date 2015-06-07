package structure;

import java.util.LinkedList;

import basic.format.Pair;

public class Argument {
	public Pair<Integer, Integer> characterSpanList;
	public String rawText;
	public LinkedList<Integer[]> tokenList;
	
	public Argument() {
		initialize();
	}
	
	public void initialize() {
		tokenList = new LinkedList<>();
	}
	
	
}
