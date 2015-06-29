package structure;

import java.util.LinkedList;

public class Tree {
	public Tree parent;
	public LinkedList<Tree> children;
	public String constituent;
	public String leafText;
	
	public Tree() {
		children = new LinkedList<>();
	}
	
	public static Tree parseTree(String parseTree) {
		Tree root = null;
		Tree parent = null;
		int i=2;
		String s = "";
		while(i<parseTree.length()-3) {
			char c = parseTree.charAt(i);
			if(c == '(') {
				s = "";
				for(int j=i+1; j<parseTree.length()-3; j++) {
					char next = parseTree.charAt(j);
					if(next != ' ') {
						s = s+next;
					} else { // space
						i=j;
						break;
					}
				}
				Tree t = new Tree();
				t.setParent(parent);
				t.setConstituent(s);
				if(parent != null)
					parent.addChild(t);
				parent = t;
				if(root == null) {
					root = t;
				}
			}
			else if(c == ')') {
				parent = parent.parent;
			} else if(c == ' ') {
				
			}
			else { // space or character
				s = "";
				for(int j=i; j<parseTree.length()-3; j++) {
					char next = parseTree.charAt(j);
					if(next != ')') {
						s = s+next;
					} else {
						if(parent != null) {
							parent.setLeaf(s);
							parent = parent.parent;
							i=j;
						}
						break;
					}
				}
			}
			
			i++;
		}
		System.out.println("Parse done.");
		return root;
	}
	
	public void setParent(Tree t) {
		this.parent = t;
	}
	
	public void setConstituent(String text) {
		this.constituent = new String(text);
	}
	
	public void setLeaf(String text) {
		this.leafText = new String(text);
	}
	
	public void addChild(Tree t) {
		this.children.add(t);
	}
	
	public boolean isLeaf() {
		return (leafText!=null);
	}
	
	public boolean isRoot() {
		return (parent==null);
	}
	
	public LinkedList<Tree> getWordNodeList() {
		LinkedList<Tree> nodeList = new LinkedList<>();
		if(isLeaf()) {
			nodeList.add(this);
		} else {
			for (Tree child : children) {
				nodeList.addAll(child.getWordNodeList());
			}
		}
		return nodeList;
	}
	
	public Tree getParent() {
		return parent;
	}
	
	public LinkedList<Tree> getSiblings() {
		LinkedList<Tree> siblings = new LinkedList<>();
		if(isRoot());
		else {
			siblings.addAll(parent.children);
		}
		return siblings;
	}
	
	public LinkedList<Tree> getPathToRoot() {
		LinkedList<Tree> path = new LinkedList<>();
		path.add(this);
		if(isRoot());
		else {
			path.addAll(parent.getPathToRoot());
		}
		return path;
	}
	
	public void printTree(int level) {
		for(int i=0; i<level; i++) {
			System.out.print("  ");
		}
		System.out.print(constituent+" ");
		if(isLeaf()) {
			System.out.print(leafText);
		}
		System.out.println();
		level++;
		for (Tree tree : children) {
			tree.printTree(level);
		}
	}
	
	public void printWords() {
		if(isLeaf()) {
			System.out.print(leafText+" ");
		} else {
			for (Tree tree : children) {
				tree.printWords();
			}
		}
	}
	
	@Override
	public String toString() {
		if(isLeaf()) {
			return leafText;
		} else {
			return "";
		}
	}
	
	public static void main(String[] args) {
		String parseTree = "( (S (NP (PRP We)) (VP (VBP 've) (VP (VP (VBN talked) (PP (TO to) (NP (NP (NNS proponents)) (PP (IN of) (NP (NN index) (NN arbitrage)))))) (CC and) (VP (VBD told) (NP (PRP them)) (S (VP (TO to) (VP (VB cool) (NP (PRP it)) (SBAR (IN because) (S (NP (PRP they)) (VP (VBP 're) (VP (VBG ruining) (NP (DT the) (NN market)))))))))))) (. .)) )\n";
		Tree tree = Tree.parseTree(parseTree);
		tree.printTree(0);
		tree.printWords();
		System.out.println(tree.getWordNodeList());
	}
	
}
