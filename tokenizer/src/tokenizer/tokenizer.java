package tokenizer;

/* Henry Berry
*  Tokenizer program
*  This program will read all the files in a corpus and tokenize the documents and count the frequency of the words
*/

import java.util.*;

import javafx.util.Pair;

import java.io.*;
import java.nio.file.Paths;

public class tokenizer{
  
	public static void main(String args[]) throws FileNotFoundException{  
		Scanner kb = new Scanner(System.in);
		System.out.println("Please enter path to corpus");

		//get path to corpus to be processed
		String path = kb.nextLine();

		//process corpus
		processFiles(path);
		
		kb.close();
		
	}  

	public static void processFiles(String path) throws FileNotFoundException{
		HashMap<String, Pair<int[], Integer>> map = new HashMap<>();
		//read words and store in a HashMap 
		map = readFiles(path);
		//display the ten most frequent words in corpus
		displayTop20(map, 0);
		//remove stop words
		map = removeStop(map);
		displayTop20(map, 1);
		//stem words
		map = stemWords(map);
		displayTop20(map, 1);
	}

	//public static HashMap<String, Pair<Integer, Integer>>
	public static HashMap<String, Pair<int[], Integer>> readFiles(String path){
		HashMap<String, Pair<int [], Integer>> map = new HashMap<>();
		//change path from string to file so that it can be processed
		File folder = (Paths.get(path)).toFile();
		//get the files to be processed
		File[] fileNames = folder.listFiles();
		int wordCount = 0;
		int docCount = -1;
		int numFiles = fileNames.length;
			//process files one by one
        	for(File file : fileNames){
        		//add document counter
        		//can do something with an array of strings in hash map or string list
             	// if directory then print error
             	if(file.isDirectory()){
                	 System.out.println("ERROR: Can not process directory!");
                	 System.out.println( file + " was not processed.");
             	}else{
             		//if file is not director get frequency count of words in file
                	try {
                		Scanner myReader = new Scanner(file);
                		docCount++;
                	      while (myReader.hasNext()) {
                	    	//get next token
                	        String data = myReader.next(); 
                	        wordCount++;
                	        map = genMap(map, data, docCount, numFiles);
                	      }
                	      myReader.close();
                 } catch (IOException e) {
                 	// TODO Auto-generated catch block
                 	e.printStackTrace();
                 }
        
             }
         }
        map = calcDocFreq(map, docCount);
        int[] totWordCount = new int[numFiles];
        totWordCount[0] = wordCount;
        map.put("totalWordCount", new Pair<int[], Integer>(totWordCount,0));
        map.put("docCount", new Pair<int[], Integer>(totWordCount,docCount));
        
		return map;
	}
	
	public static HashMap<String, Pair<int[], Integer>> genMap(HashMap<String, Pair<int[], Integer>> map, String data, int docCount, int numFiles){
		//get next token 
        data = data.toLowerCase();
        data = data.replaceAll("\\p{Punct}","");
        if(map.containsKey(data)) {
        //if token is in map increase frequency count
        	Pair<int[], Integer> count = map.get(data);
        	int[] freq = count.getKey();
        	int docFreq = count.getValue();
        	freq[docCount] = freq[docCount] + 1;
        	map.remove(data);
        	map.put(data, new Pair<int[], Integer>(freq, docFreq));
        }else {
        //if the token is not in map add it with frequency of one
        	int[] nArr = new int[numFiles];
        	nArr[docCount] = 1;
        	Pair<int[], Integer> nPair = new Pair<int[], Integer>(nArr, 1);
        	
        	map.put(data, nPair);
        }
        map.remove("");
        return map;
	}
	
	public static HashMap<String, Pair<int[], Integer>> calcDocFreq(HashMap<String,Pair<int[], Integer>> map, int size){
		
		HashMap<String, Pair<int[], Integer>> res = new HashMap<String, Pair<int[], Integer>>();
		
		// Create a list from elements of HashMap 
        List<Map.Entry<String, Pair<int [], Integer>>> list = 
               new LinkedList<Map.Entry<String, Pair<int[], Integer>>>(map.entrySet());
        for(int i = 0; i < list.size(); i++) {
        	int count = 0;
        	int[] a = list.get(i).getValue().getKey();
        	for(int j = 0; j < size; j++) {
        		if(a[j] != 0) {
        			count++;
        		}
        	}
        	res.put(list.get(i).getKey(), new Pair<int[], Integer>(a,count));
        }
		
		return res;
	}
	
	public static void displayTop20(HashMap<String, Pair<int[], Integer>> map, int count) {
		int totWordCount = 0;
		Pair<int[], Integer> docCount = map.get("docCount");
		map.remove("docCount");
		if(count == 0) {
			totWordCount = map.get("totalWordCount").getKey()[0];
			map.remove("totalWordCount");
		}
		int uniWordCount = map.size();
		
		// Create a list from elements of HashMap 
        List<Map.Entry<String, Pair<int [], Integer>>> list = 
               new LinkedList<Map.Entry<String, Pair<int[], Integer>>>(map.entrySet()); 
  
        // Sort the list 
        Collections.sort(list, new Comparator<Map.Entry<String, Pair<int[], Integer>>>() { 
            public int compare(Map.Entry<String, Pair<int[], Integer>> o1,  
                               Map.Entry<String, Pair<int[], Integer>> o2) 
            { 
                return (o1.getValue().getValue()).compareTo(o2.getValue().getValue()); 
            } 
        }); 
        
        if(count == 0) {
        	System.out.println("Total word count for corpus " + totWordCount);
        }
		System.out.println("Unique word count for corpus " + uniWordCount);
		System.out.println("Top 20 most frequent words:");
		for(int i = (list.size() - 1); i > (list.size() - 21); i--) {
			System.out.print(list.get(i).getKey() + "  ");
			System.out.print(list.get(i).getValue().getValue() + "  ");
			for(int j = 0; j < docCount.getValue(); j++) {
				System.out.print(list.get(i).getValue().getKey()[j] + "  ");
			}
			System.out.println();
		}
		map.put("docCount", docCount);
	}
	
	public static HashMap<String, Pair<int[], Integer>> removeStop(HashMap<String, Pair<int[], Integer>> map) throws FileNotFoundException{
		HashMap<String, Pair<int[], Integer>> map2 = new HashMap<String, Pair<int[], Integer>>();
		Pair<int[],Integer> docCount = map.get("docCount");
		map.remove("docCount");
		// Create a list from elements of HashMap 
        List<Map.Entry<String, Pair<int[], Integer>>> list = 
               new LinkedList<Map.Entry<String, Pair<int[], Integer>>>(map.entrySet());
		String data;
		for(int i = 0; i < map.size(); i++) {
			data = list.get(i).getKey();
			
			if(isStopWord(data)) {
				map.remove(data);
			}else {
				map2.put(data, map.get(data));
			}
		}
		
		map2.put("docCount", docCount);
		
		return map2;
	}
	
	public static boolean isStopWord(String word) throws FileNotFoundException {
		File stop = new File(tokenizer.class.getResource("stopwords.txt").getFile());
		Scanner myReader = new Scanner(stop);
		String stopWord;
	      while (myReader.hasNext()) {
	    	  stopWord = myReader.next();
	    	  if(word.equals(stopWord)) {
	    		  myReader.close();
	    		  return true;
	    	  }
	      }
	      myReader.close();
	      return false;
	}
	
	public static HashMap<String, Pair<int[], Integer>> stemWords(HashMap<String, Pair<int[], Integer>> map){
		Porter stemmer = new Porter();
		Pair<int[],Integer> docCount = map.get("docCount");
		HashMap<String, Pair<int[], Integer>> map2 = new HashMap<String, Pair<int[], Integer>>();
		map.remove("docCount");
	 // Create a list from elements of HashMap 
        List<Map.Entry<String, Pair<int[], Integer>>> list = 
               new LinkedList<Map.Entry<String, Pair<int[], Integer>>>(map.entrySet());
		String data;
		Pair<int[], Integer> value;
		Pair<int[], Integer> value2;
		for(int i = 0; i < map.size(); i++) {
			data = list.get(i).getKey();
			
			String stem = stemmer.stripAffixes(data);
			value = map.get(data);
			map.remove(data);
			if(map2.get(stem) == null) {
				map2.put(stem, value);
			}else {
				int docFreq;
				value2 = map2.get(stem);
				if(value.getValue() >= value2.getValue()) {
					docFreq = value.getValue();
				}else {
					docFreq = value2.getValue();
				}
				int[] totalWordCount = addWordCount(value.getKey(), value2.getKey(), docCount.getValue()); 
				map2.put(stem, new Pair<int[],Integer>(totalWordCount,docFreq));
			}
			
		}
		
		map2.put("docCount", docCount);
		
		return map2;
	}
	
	public static int[] addWordCount(int[] a, int[] b, int size) {
		int[] res = new int[size];
		for(int i = 0; i < size; i++) {
			res[i] = (a[i] + b[i]);
		}
		return res;
	}
}  