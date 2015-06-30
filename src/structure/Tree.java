package structure;

import java.util.LinkedList;

public class Tree {
	public String parsetree;
	public Tree parent;
	public LinkedList<Tree> children;
	public String constituent;
	public String leafText;
	public int leftPos, rightPos;
	public int sentenceIndex;
	
	public Tree() {
		children = new LinkedList<>();
		leftPos = rightPos = 0;
		sentenceIndex = -1;
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
		if(root != null) {
			root.setWordIndex(-1);
			root.parsetree = new String(parseTree);
		}
		return root;
	}
	
	public void setSentenceIndex(int i) {
		this.sentenceIndex = i;
		for(Tree child: children) 
			child.setSentenceIndex(i);
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
	
	public int setWordIndex(int pos) {
		if(isLeaf()) {
			leftPos = rightPos = pos+1;
		} else {
			int childPos = pos;
			leftPos = pos+1;
			for (Tree child : children) {
				childPos = child.setWordIndex(childPos);
			}
			rightPos = children.getLast().rightPos;
		}
		return rightPos;
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
	
	/**
	 * to get the leaf node of the tree
	 * @return
	 */
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
	
	/**
	 * to get the (pos)th word node tree
	 * @param pos
	 * @return
	 */
	public Tree getWordNode(int pos) {
		LinkedList<Tree> words = getWordNodeList();
		if(pos>-1 && pos < words.size()) {
			return words.get(pos);
		} else {
			return null;
		}
	}
	
	/**
	 * to get the tree node within the left position and the right position 
	 * @param left
	 * @param right
	 * @return
	 */
	public Tree getTreeNode(int left, int right) {
		//XXX
		if(leftPos == left && rightPos == right) {
			return this;
		} else {
			for (Tree child : children) {
				Tree t = child.getTreeNode(left, right);
				if(t != null) {
					return t;
				} else {
					continue;
				}
			}
		}
		return null;
	}
	
	public Tree getParent() {
		return parent;
	}
	
	public Tree getRoot() {
		if(isRoot())
			return this;
		else 
			return parent.getRoot();
	}
	
	public LinkedList<Tree> getSiblings() {
		LinkedList<Tree> siblings = new LinkedList<>();
		if(isRoot());
		else {
			for (Tree t : parent.children) {
				if(!t.equals(this))
					siblings.add(t);
			}
		}
		return siblings;
	}
	
	public Tree getLeftSibling() {
		if(isRoot())
			return null;
		else {
			int idx = parent.children.indexOf(this);
			if(idx == 0)
				return null;
			else 
				return parent.children.get(idx-1);
		}
	}
	
	public Tree getRightSibling() {
		if(isRoot())
			return null;
		else {
			int idx = parent.children.indexOf(this);
			if(idx == parent.children.size()-1)
				return null;
			else 
				return parent.children.get(idx+1);
		}
	}
	
	/**
	 * all the nodes on the path of the tree to the node
	 * @return
	 */
	public LinkedList<Tree> getPathToRoot() {
		LinkedList<Tree> path = new LinkedList<>();
		path.add(this);
		if(isRoot());
		else {
			if(parent != null) {
				path.addAll(parent.getPathToRoot());
			} else ; // parent is null (this tree is the root)
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
		System.out.print("("+leftPos+","+rightPos+")\n");
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
			return constituent+"("+leafText+")";
		} else {
			return constituent;
		}
	}
	
	public static void main(String[] args) {
//		String parseTree = "( (S (NP (PRP We)) (VP (VBP 've) (VP (VP (VBN talked) (PP (TO to) (NP (NP (NNS proponents)) (PP (IN of) (NP (NN index) (NN arbitrage)))))) (CC and) (VP (VBD told) (NP (PRP them)) (S (VP (TO to) (VP (VB cool) (NP (PRP it)) (SBAR (IN because) (S (NP (PRP they)) (VP (VBP 're) (VP (VBG ruining) (NP (DT the) (NN market)))))))))))) (. .)) )\n";
		String parseTree = "( (S (NP (NNP Treasury) (NNP Secretary) (NNP Nicholas) (NNP Brady)) (VP (VBD called) (S (NP (DT the) (NN agreement)) (`` ``) (NP (NP (DT an) (JJ important) (NN step)) (PP (ADVP (RB forward)) (IN in) (NP (DT the) (VBN strengthened) (NN debt) (NN strategy))))) (, ,) ('' '') (S (VP (VBG noting) (SBAR (IN that) (S (NP (PRP it)) (VP (MD will) (`` ``) (VP (SBAR (WHADVP (WRB when)) (S (VP (VBN implemented)))) (, ,) (VP (VB provide) (NP (JJ significant) (NN reduction)) (PP (IN in) (NP (NP (DT the) (NN level)) (PP (IN of) (NP (NP (NP (NN debt)) (CC and) (NP (NN debt) (NN service))) (VP (VBN owed) (PP (IN by) (NP (NNP Costa) (NNP Rica)))))))))))))))) (. .) ('' '')) )";
		
		Tree tree = Tree.parseTree(parseTree);
		tree.printTree(0);
		tree.printWords();
		Tree t = tree.getTreeNode(24, 24);
		t.printWords();
	}
	
}
