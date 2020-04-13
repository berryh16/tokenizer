package tokenizer;

/* Henry Berry
*  cosin similarity program
*  This program will use tokenizer class to parse files in a corpus and generate the cosin similarity between givem queries and the documents
*/

import java.util.*;
import javafx.util.Pair;
import java.io.*;

public class cosinSimilarity {
	
	public static void main(String args[]) {
		
		Scanner kb = new Scanner(System.in);
		System.out.println("Please enter path to documents");
		String path = kb.next();		
		//HashMap<String, Pair<Integer, Integer>>
		HashMap<String, Pair<int[], Integer>> docMap = tokenizer.readFiles(path);
		try {
			docMap = tokenizer.removeStop(docMap);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		docMap = tokenizer.stemWords(docMap);
		
		genWeightMatrix(docMap);
		
		kb.close();
	}
	
	public static void genWeightMatrix(HashMap<String, Pair<int[], Integer>> map) {

		int docCount = map.get("docCount").getValue();
		map.remove("docCount");
		int uniWordCount = map.size();
		double[][] weightMatrix = new double[docCount][uniWordCount];
		
		// Create a list from elements of HashMap 
        List<Map.Entry<String, Pair<int[], Integer>>> list = 
               new LinkedList<Map.Entry<String, Pair<int[], Integer>>>(map.entrySet());
		System.out.println("Genrerate Weight Matrix");
		for(int i = 0; i < docCount; i++) {
			for (int j = 0; j < uniWordCount; j++){
				weightMatrix[i][j] = calcWeight(list.get(j).getValue().getValue(), list.get(j).getValue().getKey()[i],docCount);
			}
		}
		
		System.out.println("Process Queries");
		processQueries(list, weightMatrix, uniWordCount, docCount);
		
	}
	
	public static double calcWeight(int docFreq, int termFreq, int docCount) {
		
		double iDocFreq;
		if(docFreq == 0.0) {
			iDocFreq = 0.0;
		}else {
			iDocFreq = (double)log2((double)(docCount/docFreq));
		}
		double result =(double)( termFreq * iDocFreq);
		
		return result;
		
	}
	
	public static double[] genQueryMatrix(HashMap<String, Pair<int[], Integer>> map, int size){
		
		double[] res = new double[size];
		
		// Create a list from elements of HashMap 
        List<Map.Entry<String, Pair<int[], Integer>>> list = 
               new LinkedList<Map.Entry<String, Pair<int[], Integer>>>(map.entrySet());
		
		for(int i = 0; i < size; i++) {
			res[i] = list.get(i).getValue().getValue();
		}
		
		return res;
		
	}

	// Function to calculate the 
    // log base 2 of an integer 
    public static double log2(double N) 
    { 
        // calculate log2 N indirectly 
        // using log() method 
        double result = (double)(Math.log(N) / Math.log(2)); 
  
        return result; 
    } 
    
    public static void processQueries(List<Map.Entry<String, Pair<int[], Integer>>> list, double[][] weightMatrix, int uniWordCount, int docCount) {
    	
    	File query = new File(tokenizer.class.getResource("queries.txt").getFile());
    	Scanner myReader;
    	HashMap<String, Pair<int[], Integer>> map = new HashMap<String, Pair<int[], Integer>>();
    	
		try {
			myReader = new Scanner(query);
			String line;
	    	String[][] word = new String[100][100];
	    	int qCount = -1;
	    	int numWords = 0;
	    	double[][] qMatrix = new double[100][100];
	    	Double[][] simMatrix = new Double[100][docCount];
	    	while (myReader.hasNext()){
	    		qCount++;
	    		System.out.println(qCount);
	    		line = myReader.nextLine();
	    		String lWords[] = line.split(" ");
	    		numWords = lWords.length;
	    		for(int i = 0; i < numWords; i ++) {
	    			word[qCount][i] = lWords[i];
	    		}
	    		for(int i = 0; i < numWords - 1; i++) {
	    			map = tokenizer.genMap(map, word[qCount][i], 0, 1);
	    		}
	    		map.put("docCount",map.get(word[qCount][0]));
	    		map = tokenizer.removeStop(map);
	    		map = tokenizer.stemWords(map);
	    		map.remove("docCount");
	    		numWords = map.size();
	    		System.out.println("genQueryMatrix ");
	    		qMatrix[qCount] = genQueryMatrix(map, numWords);
	    		simMatrix[qCount] = findSim(list, map, numWords, weightMatrix, qMatrix, docCount, qCount, uniWordCount);
	    		map.clear();
	    	}
	    	calcTopTen(simMatrix, qCount, docCount);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }
    
    public static void calcTopTen(Double[][] simMatrix, int qCount, int docCount) {
    	
    	int[][] index = new int[qCount][docCount];
    	
    	for(int i =0; i < qCount; i++) {
    		for(int j = 0; j < docCount; j++) {
    			index[i][j] = j + 1;
    		}
    	}
    	
    	//could use a better sort but eh
    	
    	for(int i = 0; i < qCount; i++) {
    		for(int j = 0; j < docCount; j++) {
    			for(int k = j + 1; k < docCount; k++) {
    				if(simMatrix[i][j] < simMatrix[i][k]) {
    					int inxTemp = index[i][j];
    					double temp = simMatrix[i][j];
    					simMatrix[i][j] = simMatrix[i][k];
    					index[i][j] = index[i][k];
    					simMatrix[i][k] = temp;
    					index[i][k] = inxTemp;
    				}
    			}
    		}
    	}
    	for(int i = 0; i < qCount; i++) {
    		for(int j = 0; j < 10; j++) {
    			System.out.println("Query #" + (i+1) + " Document #" + (index[i][j]) + " Similarity score: " + simMatrix[i][j]);
    		}
    	}
    }
    
    public static Double[] findSim(List<Map.Entry<String, Pair<int[], Integer>>> list, HashMap <String, Pair<int[], Integer>> word, int wordLeng, double[][] weightMatrix, double[][] qMatrix, int docCount, int qCount, int uniWord) {
    	
    	Double[] simMatrix = new Double [docCount];
    	
    	int i = qCount;
    	for(int j = 0; j < docCount; j++) {
    		simMatrix[j] = calcSim(word, wordLeng, weightMatrix[j], list, uniWord, qMatrix[i]);
    		if(simMatrix[j].isNaN()) {
    			simMatrix[j] = 0.0;
    		}
    		System.out.println("Query " + (i+1) + " Document " + (j+1) + " Simmilarity " + simMatrix[j]);
    	}
    	
    	return simMatrix;
    }
    
    public static double calcSim(HashMap <String, Pair<int[], Integer>> words, int wordLeng, double[] weightMatrix, List<Map.Entry<String, Pair<int[], Integer>>> list, int docCount, double[] qMatrix) {
    	
    	// Create a list from elements of HashMap 
        List<Map.Entry<String, Pair<int[], Integer>>> wordsList = 
               new LinkedList<Map.Entry<String, Pair<int[], Integer>>>(words.entrySet());
    	
    	double res = 0;
    	double docWTot = 0.0;
    	double qWTot = 0.0;
    	for(int i = 0; i < wordLeng; i++) {
    		for(int j = 0; j < docCount; j++) {
    			if((wordsList.get(i).getKey()).equals(list.get(j).getKey())) {
    				res = res + (qMatrix[i] * (weightMatrix[j]));
    				docWTot = docWTot + weightMatrix[j];
    				qWTot = qWTot + qMatrix[i];
    			}else {
    				res = res + 0;
    			}
    		}
    	}
    	res = (res/((docWTot*docWTot)*(qWTot*qWTot)));
    	return res;
    	
    }
}
