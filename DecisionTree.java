import java.io.Serializable;
import java.util.ArrayList;
import java.text.*;
import java.lang.Math;

public class DecisionTree implements Serializable {

    DTNode rootDTNode;
    int minSizeDatalist; //minimum number of datapoints that should be present in the dataset so as to initiate a split

    public static final long serialVersionUID = 343L;

    public DecisionTree(ArrayList<Datum> datalist, int min) {
        minSizeDatalist = min;
        rootDTNode = (new DTNode()).fillDTNode(datalist);
    }

    class DTNode implements Serializable {
        public static final long serialVersionUID = 438L;
        boolean leaf;
        int label = -1;      // only defined if node is a leaf
        int attribute; // only defined if node is not a leaf
        double threshold;  // only defined if node is not a leaf

        DTNode left, right; //the left and right child of a particular node. (null if leaf)

        DTNode() {
            leaf = true;
            threshold = Double.MAX_VALUE;
        }


        // this method takes in a datalist (ArrayList of type datum). It returns the calling DTNode object
        // as the root of a decision tree trained using the datapoints present in the datalist variable and minSizeDatalist.
        // the left and right child of the node correspond to "less than" and "greater than or equal to" threshold
        DTNode fillDTNode(ArrayList<Datum> datalist) {
            if (datalist == null) {
                return null;
            }

            // if the labelled data set has at least k data items
            if (datalist.size() >= minSizeDatalist) {
                //check if all the data items have the same label
                int curLabel = datalist.get(0).y;
                boolean sameLabel = true;
                for (int i = 1; i < datalist.size(); i++) {
                    if (datalist.get(i).y != curLabel) {
                        sameLabel = false;
                        break;
                    }
                }

                // if all the data items have the same label
                if (sameLabel) {
                    // create a leaf node with that class label and return it
					DTNode newLeaf = new DTNode();
					newLeaf.leaf = true;
					newLeaf.label = curLabel;
					newLeaf.left = null;
					newLeaf.right = null;
					return newLeaf;

                } else {
                    // create a "best" attribute test question
					double bestAvgEntropy = 2;
					int bestAttr = -1;
					double bestThreshold = -1;

					// for each attribute in x
					for (int i = 0 ; i < datalist.get(0).x.length; i++) {
						// for each data point in the list
						for (Datum dataPoint : datalist) {
							// compute split and current avg entropy based on that split
                            ArrayList<Datum> data1 = new ArrayList<Datum>(); // left
                            ArrayList<Datum> data2 = new ArrayList<Datum>(); // right

                            for (Datum data : datalist) {
                                if (data.x[i] < dataPoint.x[i]) {
                                    data1.add(data);
                                } else {
                                    data2.add(data);
                                }
                            }

                            double entropy1 = calcEntropy(data1);
                            double entropy2 = calcEntropy(data2);

                            double totalSize = data1.size() + data2.size();
							double currAvgEntropy = (data1.size() / totalSize) * entropy1 + (data2.size() / totalSize) * entropy2;

							if (bestAvgEntropy > currAvgEntropy) {
								bestAvgEntropy = currAvgEntropy;
								bestAttr = i;
								bestThreshold = dataPoint.x[i];
							}
						}
					}

					// in case a split is not necessary
                    double entropyWithoutSplit = calcEntropy(datalist);
					if (bestAvgEntropy >= entropyWithoutSplit) {
                        // create leaf node with label equal to the majority of labels and return it
                        DTNode newLeaf = new DTNode();
                        newLeaf.leaf = true;
                        newLeaf.label = newLeaf.findMajority(datalist);
                        newLeaf.left = null;
                        newLeaf.right = null;
                        return newLeaf;
                    }

                    // create a new node and store the attribute test in that node
					DTNode newNode = new DTNode();
					newNode.leaf = false;
					newNode.attribute = bestAttr;
					newNode.threshold = bestThreshold;


                    // split the set of data items into two subsets
					ArrayList<Datum> data1 = new ArrayList<Datum>();
					ArrayList<Datum> data2 = new ArrayList<Datum>();

                    for (Datum dataPoint : datalist) {
                        if (dataPoint.x[bestAttr] < bestThreshold) {
                            data1.add(dataPoint);
                        } else {
                            data2.add(dataPoint);
                        }
                    }
                    newNode.left = fillDTNode(data1);
                    newNode.right = fillDTNode(data2);

                    return newNode;
                }

            } else {
                // create leaf node with label equal to the majority of labels and return it
                DTNode newLeaf = new DTNode();
                newLeaf.leaf = true;
                newLeaf.label = newLeaf.findMajority(datalist);
                newLeaf.left = null;
                newLeaf.right = null;
                return newLeaf;
            }
        }


        // Given a datalist, this method returns the label that has the most occurrences.
        // In case of a tie it returns the label with the smallest value (numerically) involved in the tie.
        int findMajority(ArrayList<Datum> datalist) {

            int[] votes = new int[2];

            //loop through the data and count the occurrences of datapoints of each label
            for (Datum data : datalist) {
                votes[data.y] += 1;
            }

            if (votes[0] >= votes[1])
                return 0;
            else
                return 1;
        }


        // This method takes in a datapoint (excluding the label) in the form of an array of type double (Datum.x) and
        // returns its corresponding label, as determined by the decision tree
        int classifyAtNode(double[] xQuery) {

            //ADD CODE HERE
            if (this.leaf) {
                return this.label;
            } else {
                if (this.threshold <= xQuery[attribute]) {
                    return (this.right).classifyAtNode(xQuery);
                } else {
                    return (this.left).classifyAtNode(xQuery);
                }
            }
        }


        //given another DTNode object, this method checks if the tree rooted at the calling DTNode is equal to the tree rooted
        //at DTNode object passed as the parameter
        public boolean equals(Object dt2) {

            //ADD CODE HERE
            // check if the object passed as a parameter is of type DTNode
            if (!(dt2 instanceof DTNode)) {
                return false;

            } else {
                DTNode second = (DTNode) dt2;

                // leaf node -> base case
                if (this.leaf && second.leaf) {
                    return (this.label == second.label);

                } else {
                    // internal node
                    if (this.threshold != second.threshold || this.attribute != second.attribute) {
                        return false;
                    } else {
                        // traverse the rest of the tree and check if children are equal
                        return (this.left.equals(second.left) && (this.right.equals(second.right)));
                    }

                }
            }
        }
    }


    //Given a dataset, this returns the entropy of the dataset
    double calcEntropy(ArrayList<Datum> datalist) {
        double entropy = 0;
        double px = 0;
        float[] counter = new float[2];
        if (datalist.size() == 0)
            return 0;
        double num0 = 0.00000001, num1 = 0.000000001;

        //calculates the number of points belonging to each of the labels
        for (Datum d : datalist) {
            counter[d.y] += 1;
        }
        //calculates the entropy using the formula specified in the document
        for (int i = 0; i < counter.length; i++) {
            if (counter[i] > 0) {
                px = counter[i] / datalist.size();
                entropy -= (px * Math.log(px) / Math.log(2));
            }
        }

        return entropy;
    }


    // given a datapoint (without the label) calls the DTNode.classifyAtNode() on the rootnode of the calling DecisionTree object
    int classify(double[] xQuery) {
        return this.rootDTNode.classifyAtNode(xQuery);
    }

    // Checks the performance of a DecisionTree on a dataset
    String checkPerformance(ArrayList<Datum> datalist) {
        DecimalFormat df = new DecimalFormat("0.000");
        float total = datalist.size();
        float count = 0;

        for (int s = 0; s < datalist.size(); s++) {
            double[] x = datalist.get(s).x;
            int result = datalist.get(s).y;
            if (classify(x) != result) {
                count = count + 1;
            }
        }

        return df.format((count / total));
    }


    //Given two DecisionTree objects, this method checks if both the trees are equal by
    //calling onto the DTNode.equals() method
    public static boolean equals(DecisionTree dt1, DecisionTree dt2) {
        boolean flag = true;
        flag = dt1.rootDTNode.equals(dt2.rootDTNode);
        return flag;
    }

}
