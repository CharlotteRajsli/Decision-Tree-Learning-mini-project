//package ID3_CW;

// ECS629/759 Assignment 2 - ID3 Skeleton Code
// Author: Simon Dixon

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;



class ID3 {

	/** Each node of the tree contains either the attribute number (for non-leaf
	 *  nodes) or class number (for leaf nodes) in <b>value</b>, and an array of
	 *  tree nodes in <b>children</b> containing each of the children of the
	 *  node (for non-leaf nodes).
	 *  The attribute number corresponds to the column number in the training
	 *  and test files. The children are ordered in the same order as the
	 *  Strings in strings[][]. E.g., if value == 3, then the array of
	 *  children correspond to the branches for attribute 3 (named data[0][3]):
	 *      children[0] is the branch for attribute 3 == strings[3][0]
	 *      children[1] is the branch for attribute 3 == strings[3][1]
	 *      children[2] is the branch for attribute 3 == strings[3][2]
	 *      etc.
	 *  The class number (leaf nodes) also corresponds to the order of classes
	 *  in strings[][]. For example, a leaf with value == 3 corresponds
	 *  to the class label strings[attributes-1][3].
	 **/
	class TreeNode {

		TreeNode[] children;
		int value;

		public TreeNode(TreeNode[] ch, int val) {
			value = val;
			children = ch;
		} // constructor

		public String toString() {
			return toString("");
		} // toString()
		
		String toString(String indent) {
			if (children != null) {
				String s = "";
				for (int i = 0; i < children.length; i++)
					s += indent + data[0][value] + "=" +
							strings[value][i] + "\n" +
							children[i].toString(indent + '\t');
				return s;
			} else
				return indent + "Class: " + strings[attributes-1][value] + "\n";
		} // toString(String)

	} // inner class TreeNode

	private int attributes; 	// Number of attributes (including the class)
	private int examples;		// Number of training examples
	private TreeNode decisionTree;	// Tree learnt in training, used for classifying
	private String[][] data;	// Training data indexed by example, attribute
	private String[][] strings; // Unique strings for each attribute
	private int[] stringCount;  // Number of unique strings for each attribute

	public ID3() {
		attributes = 0;
		examples = 0;
		decisionTree = null;
		data = null;
		strings = null;
		stringCount = null;
	} // constructor
	
	public void printTree() {
		if (decisionTree == null)
			error("Attempted to print null Tree");
		else
			System.out.println(decisionTree);
	} // printTree()

	/** Print error message and exit. **/
	static void error(String msg) {
		System.err.println("Error: " + msg);
		System.exit(1);
	} // error()

	static final double LOG2 = Math.log(2.0);
	
	static double xlogx(double x) {
		return x == 0? 0: x * Math.log(x) / LOG2;
	} // xlogx()

	/** Execute the decision tree on the given examples in testData, and print
	 *  the resulting class names, one to a line, for each example in testData.
	 **/
	public void classify(String[][] testData) {
		if (decisionTree == null)
			error("Please run training phase before classification");
		// PUT  YOUR CODE HERE FOR CLASSIFICATION
		
		//iterate through the rows(i) of testData
		for (int i=1; i<testData.length; i++) {
			int output = classify(testData[i], decisionTree);
			System.out.println(strings[attributes-1][output]);
		}
			
	} // classify()
	/** 
	 *  classify override method for children leaf nodes,
	 *  (only row is passed to here)
	 *  if the tree leaf node is reached
	 **/
	private int classify (String[] testData, TreeNode tree) {
		if (tree.children == null) {
			return tree.value;
		}
		//given the string of chosen attribute for the row
		//find the best branch to follow (based on string number?)
		String s = testData [tree.value];
		//iterate trough the branch (j)
		for (int j =0; j<stringCount[tree.value]; j++) {
			if (s.equals(strings[tree.value][j])) {
				//System.out.println(tree.children[j])
				return classify(testData, tree.children[j]);				
			}
		}
		//if there is no string match in the training and test data return 0
		return 0;
	}
	
	public void train(String[][] trainingData) {
		indexStrings(trainingData);
		// PUT  YOUR CODE HERE FOR TRAINING
		decisionTree = train (new ArrayList <Integer>(), new ArrayList <Integer>());
	} // train()
	
	private TreeNode train (ArrayList<Integer> otherColumns, ArrayList<Integer> otherRows) {
		//check if there is any more examples left in the training set
		if (otherRows.size() == examples-1) {
			return null;
		}
		
		//same for the attributes
		if (otherColumns.size() >= attributes-1) {
			return new TreeNode(null, getMostCommon(otherRows));
		}
		
		//check if they are the same class
		int cls = isSameClass(otherRows);
		if (cls != -1) {
			return new TreeNode (null, cls);
		}
		
		int bestAttribute = getBestAttribute (otherColumns, otherRows);
		//System.out.println("best attribute is: " + bestAttribute);
		
		//need to add the chosen attribute to the other not used columns, so it won't be used again
		ArrayList <Integer> bestUsedColumn = new ArrayList<Integer> (otherColumns);
		bestUsedColumn.add(bestAttribute);
		
		//create a branch for each string of this attribute
		TreeNode[] subsets = new TreeNode[stringCount[bestAttribute]];
		for(int i=0; i<subsets.length; i++) {
			
			//split data for each string of an attribute
			//create a shallow copy of the array, to divide the data 
			ArrayList<Integer> bestUsedRow = addOtherRows(bestAttribute, i, otherRows);
			//pass new ignored columns and rows to the method
			subsets[i] = train(bestUsedColumn, bestUsedRow);
			if(subsets[i] == null){
				subsets[i] = new TreeNode(null, getMostCommon(otherRows));
			}
		}
		return new TreeNode (subsets, bestAttribute);
	}

	
	private int getMostCommon(ArrayList<Integer> otherRows){
		int cColumn = attributes-1;
		//count how many of each class is being used (not under the "other" data)
		int[] clsCount = new int[stringCount[cColumn]];
		//iterate trough rows (i)
		for(int i=1; i<examples; i++){
			if(isOther(i, otherRows)) continue;
			String cellClass = data[i][cColumn];
			for(int cls=0; cls<clsCount.length; cls++){
				//find class index
				if(cellClass.equals(strings[cColumn][cls])){
					clsCount[cls]++;
				}
			}
		}
		//find highest number of all array
		int mostCommonId = 0;
		for(int cls=1; cls<clsCount.length; cls++){
			if(clsCount[cls] > clsCount[mostCommonId]){
				mostCommonId = cls;
			}
		}
		return mostCommonId;
	}
	
	private boolean isOther(int number, ArrayList<Integer> otherValues){
		for(Integer other : otherValues){
			if(number == other) return true;
		}
		return false;
	}
	
	
	private int isSameClass(ArrayList<Integer> otherRows){
		int cls = -1;
		boolean found = false;
		//iterate trough rows(i) and find the first row being used
		for(int i=1; i<examples || !found; i++){
			if(isOther(i, otherRows)) continue;
			//set class string as class of this row
			String clsString = data[i][attributes-1];
			//find index of this class
			for(int clsId=0; clsId<stringCount[attributes-1]; clsId++){
				if(clsString.equals(strings[attributes-1][clsId])){
					cls = clsId;
					found = true;
					break;
				}
			}
		}
		//save class string to be reused for comparing
		String clsString = strings[attributes-1][cls];
		//check each row (if being used), if all are the same
		for(int row=1; row<examples; row++){
			if(isOther(row, otherRows)) continue;
			if(!clsString.equals(data[row][attributes-1])){
				//if class string of this row is different
				return -1;
			}
		}
		//return class that is same for all rows;
		return cls;
	}
	
	
	private ArrayList<Integer> addOtherRows(int attribute, int sId, ArrayList<Integer> otherRows){
		String string = strings[attribute][sId];
		if(string == null) return otherRows;
		ArrayList<Integer> newOther = new ArrayList<Integer>(otherRows);
		for(int row=1; row<examples; row++){
			//if string is used but not equal cell value it is added to the other not used data (rows)
			if(!string.equals(data[row][attribute]) && !isOther(row, otherRows)){
				newOther.add(row);
			}
		}
		return newOther;
	}
	
	
	
	
	/**
	 * method to calculate gain and entropy of each attribute and choose the best attribute for the classification
	 * @param otherColumns
	 * @param otherRows
	 * @return bestAttribute
	 */
	private int getBestAttribute(ArrayList<Integer> otherColumns, ArrayList<Integer> otherRows){
		//-1 - bc 1 is title row
		double s = getS(otherRows);
		int[][][] cCount = getClassCount(otherColumns, otherRows);
		int totalRows = examples - 1 - otherRows.size();
		
		double bestGain = -1;
		int bestAttribute = -1;
		
		for(int attribute=0; attribute<cCount.length; attribute++){
			//calculate for every used attribute, but not for the other ones
			if(isOther(attribute, otherColumns)) continue;
			double gain = s;
			for(int[] string : cCount[attribute]){
				//for each string in that attribute
				int stringTotal = 0; //total number of those strings in data
				for(int classSum : string){
					stringTotal += classSum;
				}
				//calculate entropy H(s)
				double entropy = 0;
				for(int classSum : string){
					//class fraction of a string e.g.:
					//2/3 = 2 rows are some class out of 3 rows of this string
					double cfos = (double)classSum/(double)stringTotal;
					entropy -= xlogx(cfos);
					//System.out.println("calculated entropy is: " + entropy);
				}
							
				//gain -= ratio * entropy;
				gain -= ((double)stringTotal/(double)totalRows) * entropy;
				//System.out.println("calculated gain is: " + gain);
			}
			if(gain > bestGain){
				//if gain for this attribute is higher than previous - set as new best attribute
				bestGain = gain;
				bestAttribute = attribute;
			}
		}
		return bestAttribute;
	}

	
	
	/**
	 * method getClassCount used above to calculate the gain 
	 * @param otherColumns
	 * @param otherRows
	 * @return cCount as class count
	 */
	private int[][][] getClassCount(ArrayList<Integer> otherColumns, ArrayList<Integer> otherRows){
		int cColumn = attributes-1; //class column
		int classes = stringCount[cColumn];
		//[attribute][string][class]
		int[][][] cCount = new int[cColumn][][];
		for(int attr=0; attr<cColumn; attr++){
			/*
			find out if the column is to be ignored
			alternatively I had option to remove column from data, but that
			created multiple copies of smaller and smaller data, plus it required
			twice as many iterations
			*/
			if(isOther(attr, otherColumns)) continue;
			//System.out.println("[ATTR]");
			int attrStrings = stringCount[attr];
			cCount[attr] = new int[attrStrings][classes];
			//check each row, without title row (1st row)
			for(int row=1; row<examples; row++){
				//find out if the row should be ignored
				if(isOther(row, otherRows)) continue;
				//data[row][attr];
				String cell = data[row][attr];
				String rowClass = data[row][cColumn];
				int stringId = 0;
				for(String val : strings[attr]){
					if(val == null) continue;
					/*
					1) Try matching each string for this attribute with cell value
						 to find string index
					*/
					if(cell.equals(val)){
						//matched cell value with one of the attribute string
						int classId = 0;
						for(String cls : strings[cColumn]){
							if(cls == null) continue;
							/*
							2) Try matching each string for class with class of this column
								 to find index of this class
							*/
							if(rowClass.equals(cls)){
								cCount[attr][stringId][classId]++;
							}
							classId++;
						}
					}
					stringId++;
				}
			}
		}
		return cCount;
	}
	
	
	
	
	
	
	
	
	private double getS(ArrayList<Integer> otherRows){
		int cColumn = attributes-1;
		int[] cCnt = new int[stringCount[cColumn]];
		for(int row=1; row<examples; row++){
			//check if row is used or not
			if(isOther(row, otherRows)) continue;
			String rowCls = data[row][cColumn];
			//find which class matches with the row
			for(int c=0; c<cCnt.length; c++){
				if(rowCls.equals(strings[cColumn][c])){
					cCnt[c]++;
				}
			}
		}
		double s = 0.0;
		int totalRows = examples - 1 - otherRows.size();
		for(Integer sum : cCnt){
			double fraction = ((double)sum/totalRows);
			s -= xlogx(fraction);
		}
		return s;
	}
	
	
	
	
	/** Given a 2-dimensional array containing the training data, numbers each
	 *  unique value that each attribute has, and stores these Strings in
	 *  instance variables; for example, for attribute 2, its first value
	 *  would be stored in strings[2][0], its second value in strings[2][1],
	 *  and so on; and the number of different values in stringCount[2].
	 **/
	void indexStrings(String[][] inputData) {
		data = inputData;
		examples = data.length;
		attributes = data[0].length;
		stringCount = new int[attributes];
		strings = new String[attributes][examples];// might not need all columns
		int index = 0;
		for (int attr = 0; attr < attributes; attr++) {
			stringCount[attr] = 0;
			for (int ex = 1; ex < examples; ex++) {
				for (index = 0; index < stringCount[attr]; index++)
					if (data[ex][attr].equals(strings[attr][index]))
						break;	// we've seen this String before
				if (index == stringCount[attr])		// if new String found
					strings[attr][stringCount[attr]++] = data[ex][attr];
			} // for each example
		} // for each attribute
	} // indexStrings()

	/** For debugging: prints the list of attribute values for each attribute
	 *  and their index values.
	 **/
	void printStrings() {
		for (int attr = 0; attr < attributes; attr++)
			for (int index = 0; index < stringCount[attr]; index++)
				System.out.println(data[0][attr] + " value " + index +
									" = " + strings[attr][index]);
	} // printStrings()
		
	/** Reads a text file containing a fixed number of comma-separated values
	 *  on each line, and returns a two dimensional array of these values,
	 *  indexed by line number and position in line.
	 **/
	static String[][] parseCSV(String fileName)
								throws FileNotFoundException, IOException {
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		String s = br.readLine();
		int fields = 1;
		int index = 0;
		while ((index = s.indexOf(',', index) + 1) > 0)
			fields++;
		int lines = 1;
		while (br.readLine() != null)
			lines++;
		br.close();
		String[][] data = new String[lines][fields];
		Scanner sc = new Scanner(new File(fileName));
		sc.useDelimiter("[,\n]");
		for (int l = 0; l < lines; l++)
			for (int f = 0; f < fields; f++)
				if (sc.hasNext())
					data[l][f] = sc.next();
				else
					error("Scan error in " + fileName + " at " + l + ":" + f);
		sc.close();
		return data;
	} // parseCSV()

	public static void main(String[] args) throws FileNotFoundException,
												  IOException {
		if (args.length != 2)
			error("Expected 2 arguments: file names of training and test data");
		String[][] trainingData = parseCSV(args[0]);
		String[][] testData = parseCSV(args[1]);
		ID3 classifier = new ID3();
		classifier.train(trainingData);
		classifier.printTree();
		classifier.classify(testData);
	} // main()

} // class ID3
