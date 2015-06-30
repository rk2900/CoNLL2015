package model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

import opennlp.model.GenericModelReader;
import opennlp.model.MaxentModel;

import org.json.simple.JSONObject;

import structure.Argument;
import structure.Document;
import structure.Relation;
import structure.Tree;
import entry.Loader;

public class ArgumentExtraction extends Model {

	public Loader loader;
	public String dataFilePath = "./train/arg_features.dat";
	public String modelFilePath = "./train/arg_featuresModel.txt";
	public String testFilePath = "./train/arg_features.test";
	public MaxentModel _model;
	
	@Override
	protected void init() {
	}

	@Override
	protected void train(Loader loader) {
		this.loader = loader;
		
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(new File(dataFilePath)));
			
			for(Relation relation: loader.trainData.values()) {
				String docId = relation.docId;
				Document document = loader.docs.get(docId);
				LinkedList<Tree> candidates = getCandidates(docId, document, relation);
				for (Tree candidate : candidates) {
					String feature = genFeature(docId, document, relation, candidate);
					String label = getGoldLabel(docId, document, relation, candidate);
					bw.write(feature+" "+label);
					bw.newLine();
				}
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.err.println("\nExtraction features generated.");
		trainModel(dataFilePath, modelFilePath);
		System.err.println("Extraction model trained.");
	}

	
	
	private String getGoldLabel(String docId, Document document,
			Relation relation, Tree candidate) {
		String label = "null";
		Argument arg1 = relation.arg1;
		Argument arg2 = relation.arg2;
		
		if(arg1.tokenList.getFirst()[3] == candidate.sentenceIndex) {
			if(arg1.tokenList.getFirst()[4] <= candidate.leftPos && arg1.tokenList.getLast()[4] >= candidate.rightPos) {
				label = "arg1";
			} else if(arg2.tokenList.getFirst()[4] <= candidate.leftPos && arg2.tokenList.getLast()[4] >= candidate.rightPos) {
				label = "arg2";
			}
		}
		return label;
	}

	private String getLabel(String feature) {
		return eval(feature);
	}

	private String genFeature(String docId, Document document,
			Relation relation, Tree candidate) {
		StringBuilder feature = new StringBuilder();
		Argument connective = relation.connective;
		Tree connTree = relation.connTree;
		
		// CON-Str
		String connStr = new String(connective.rawText.replace(" ", "_"));
		feature.append("con_str=").append(connStr).append(" ");
		
		// CON-LStr
		feature.append("con_lstr=").append(connStr.toLowerCase()).append(" ");
		
		// CON-Cat
		String category = Loader.connCategoryMap.get(connective.rawText.toLowerCase());
		feature.append("con_cat=").append(category);
		feature.append(" ");
		
		LinkedList<Tree> siblings = connTree.getSiblings();
		int leftSibCount = 0;
		int rightSibCount = 0;
		for (Tree sib : siblings) {
			if(sib.leftPos <= connTree.rightPos)
				leftSibCount++;
			else if(sib.rightPos >= connTree.leftPos)
				rightSibCount++;
		}
		// CON-iLSib
		feature.append("con_ilsib=").append(leftSibCount).append(" ");
		
		// CON-iRSib
		feature.append("con_irsib=").append(rightSibCount).append(" ");
		
		// NT-Ctx
		feature.append("nt_ctx=");
		feature.append(candidate.constituent).append("_");
		Tree parent = candidate.parent;
		feature.append(parent==null?"null":parent.constituent).append("_");
		Tree leftSibling = candidate.getLeftSibling();
		Tree rightSibling = candidate.getRightSibling();
		feature.append(leftSibling==null?"null":candidate.getLeftSibling()).append("_");
		feature.append(rightSibling==null?"null":candidate.getRightSibling()).append(" ");
		
		// CON-NT-Path
		StringBuilder conntpath = new StringBuilder();
		LinkedList<Tree> path = getPathNode(connTree, candidate);
		feature.append("con_nt_path=");
		if(path.size()==0) {
			conntpath.append("null");
		} else {
			for (Tree t : path) {
				conntpath.append(t.constituent);
				if(!path.getLast().equals(t)) {
					conntpath.append("_>_");
				}
			}
		}
		feature.append(conntpath.toString());
		feature.append(" ");
		
		// CON-NT-Position
		feature.append("con_nt_position=");
		if(!candidate.getRoot().equals(connTree.getRoot())) {
			feature.append("previous");
		} else {
			if(candidate.leftPos <= connTree.rightPos) {
				feature.append("left");
			} else {
				feature.append("right");
			}
		}
		feature.append(" ");
		
		// CON-NT-Path-iLsib
		feature.append("con_nt_path_ilsib=");
		feature.append(conntpath.toString()).append(":");
		feature.append(leftSibCount>1?">1":"<=1");
		
		return feature.toString();
	}
	
	public LinkedList<Tree> getPathNode(Tree from, Tree to) {
		LinkedList<Tree> path = new LinkedList<>();
		
		LinkedList<Tree> fromToRoot = from.getPathToRoot();
		LinkedList<Tree> toToRoot = to.getPathToRoot();
		toToRoot.removeLast();
		
		path.addAll(fromToRoot);
		
		if(!from.getRoot().equals(to.getRoot()) || toToRoot.size() == 0) {
			path.add(to);
		} else {
			for(int i=toToRoot.size()-1; i>=0; i--) {
				path.add(toToRoot.get(i));
			}
		}
		return path;
	}

	public LinkedList<Tree> getCandidates(String docId, Document document, Relation relation) {
		LinkedList<Tree> candidateList = new LinkedList<>();
		Argument connective = relation.connective;
		Integer[] firstToken = connective.tokenList.getFirst();
		Integer[] lastToken = connective.tokenList.getLast();
		int leftPosOfConn = firstToken[4];
		int rightPosOfConn = lastToken[4];
//		System.out.println(docId+"\t"+leftPosOfConn+" "+rightPosOfConn+"\t"+relation.connective.rawText);
		JSONObject prevSentence = firstToken[3]==0?null:document.getSentence(firstToken[3]-1);
		JSONObject currSentence = document.getSentence(firstToken[3]);
		Tree prevTree = null;
		Tree currTree = Tree.parseTree(currSentence.get("parsetree").toString());
		
		if(currTree == null) { // case of (()) in current sentence
			System.err.println("case of (()) in current sentence");
			return candidateList;
		}
		currTree.setSentenceIndex(firstToken[3]);
		if(prevSentence != null) { // case of no previous sentence
			String parseStr = prevSentence.get("parsetree").toString();
			prevTree = Tree.parseTree(parseStr);
		}
		Tree connTree = currTree.getTreeNode(leftPosOfConn, rightPosOfConn);
		if(connTree == null) { // special case
			System.err.println("Special case of connective.");
			//TODO 
			return candidateList;
		} else ;
		
		relation.connTree = connTree;
		
		if(prevTree != null) { // other case of (()) in previous sentence & no previous sentence
			prevTree.setSentenceIndex(firstToken[3]-1);
			candidateList.add(prevTree);
		}
		LinkedList<Tree> pathToRoot = connTree.getPathToRoot();
		for (Tree node : pathToRoot) {
			candidateList.addAll(node.getSiblings());
		}
		
		return candidateList;
	}
	
	@Override
	protected HashMap<String, LinkedList<Relation>> predict(
			HashMap<String, Document> docs) {
		MaxentModel m;
		try {
			m = new GenericModelReader(new File(modelFilePath)).getModel();
			initializeModel(m);
		} catch (IOException e) {
			e.printStackTrace();
		}
		HashMap<String, LinkedList<Relation>> predResults = new HashMap<>();
		int relationCount = 0;
		for (String docId : docs.keySet()) {
			Document document = docs.get(docId);
			for (Relation relation : document.connResult) {
				// candidate constituents generation
				LinkedList<Tree> candidateList = getCandidates(docId, document, relation);
				boolean hasTrueRelation = false;
				// candidate classification
				// 1. generate features
				// 2. classify
				// 3. merge
				for (Tree candidate : candidateList) {
					String feature = genFeature(docId, document, relation, candidate);
					String label = getLabel(feature);
//					System.out.println(label+"\t"+candidate.getWordNodeList());
					if(label.equals("arg1")) {
						for(int cur=candidate.leftPos; cur<=candidate.rightPos; cur++) {
							Integer[] token = {-1,-1,-1,candidate.sentenceIndex,cur};
							relation.arg1.setToken(token);
						}
						hasTrueRelation = true;
					} else if(label.equals("arg2")) {
						for(int cur=candidate.leftPos; cur<=candidate.rightPos; cur++) {
							Integer[] token = {-1,-1,-1,candidate.sentenceIndex,cur};
							relation.arg2.setToken(token);
						}
						hasTrueRelation = true;
					} else ;
				}
				if(hasTrueRelation) {
					relationCount++;
//					relation.print();
					if(predResults.containsKey(docId))
						predResults.get(docId).add(relation);
					else {
						LinkedList<Relation> list = new LinkedList<>();
						list.add(relation);
						predResults.put(docId, list);
					}
				}
			}
		}
		System.err.println("\nExtraction finished.");
		System.err.println("\nPredict results size: "+relationCount);
		return predResults;
	}
	
	public Tree getTreeNode(Tree root, int leftPos, int rightPos) {
		//TODO
		return root.getTreeNode(leftPos, rightPos);
	}

}
