import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;


public class LDACMatrixTweets {
	public void populateVocab(String[]fileList, String vocabName,
			int lowerbound) {
		Hashtable<String, Integer> vocab = new Hashtable<>();
		  for(int i = 0; i < fileList.length; i++) {
			  try {
				  Scanner sc = new Scanner(new FileInputStream(fileList[i]));
				  while(sc.hasNextLine()) {
					  String line = sc.nextLine();
					  line = line.substring(line.indexOf(' ') + 1);
					  try(Scanner lineSc = new Scanner(line)) {
						  while(lineSc.hasNext()) {
							  String term = lineSc.next();
							  if(vocab.containsKey(term)) {
								  Integer newVal = new Integer(vocab.
										  get(term).intValue() + 1);
								  vocab.put(term, newVal);
							  } else
								  vocab.put(term, 1);
						  }
					  }
				  }
				  sc.close();
			  } catch (FileNotFoundException e) {
				  e.printStackTrace();
			  }
		  }
		  
		  /* Output vocabularies */
		  try {
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(new
					  FileOutputStream(vocabName)));
			Set<String> set = vocab.keySet();
			Iterator<String> it = set.iterator();
			while(it.hasNext()) {
				String term = it.next();
				if(vocab.get(term).intValue() <= lowerbound) continue;
				writer.write(term);
				writer.println();
			}
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void getMatrix(String[]fileList, String vocabName, String matName) {
		HashMap<String, Integer> map = new HashMap<>();
		try { /* Index vocabularies */
			Scanner vocSc = new Scanner(new FileInputStream(vocabName), 
					  StandardCharsets.UTF_8.toString());
			int index = 0;
			while(vocSc.hasNext())
				map.put(vocSc.next(), index++);
			vocSc.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
		try(PrintWriter writer = new PrintWriter(new 
				OutputStreamWriter(new 
						FileOutputStream(matName)))) {
			for(int i = 0; i < fileList.length; i++) {
				try {
					Scanner sc = new Scanner(new FileInputStream(fileList[i]));
					while(sc.hasNextLine()) {
						HashMap<String, Integer> locMap = new HashMap<>();
						String line = sc.nextLine();
						line = line.substring(line.indexOf(' ') + 1);
						try(Scanner lineSc = new Scanner(line)) {
							while(lineSc.hasNext()) {
								String term = lineSc.next();
								if(!map.containsKey(term)) continue;
								if(locMap.containsKey(term))
									locMap.put(term, locMap.get(term).intValue() + 1);
								else
									locMap.put(term, 1);
							}
						}
						
						if(locMap.size() == 0) continue;

						writer.write(locMap.size() + " "); //# of unique words
						Set<String> set = locMap.keySet();
						Iterator<String> it = set.iterator();
						while(it.hasNext()) {
							String key = it.next();
							writer.write(map.get(key).intValue() +
									":" + locMap.get(key));
							if(it.hasNext())
								writer.print(" ");
						}
						writer.println();
					}
					sc.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
	}
	
	public static void main (String[] args) {
		LocalDate start = LocalDate.parse("2017-04-10"),
		          end   = LocalDate.parse("2017-04-24");
		ArrayList<String> list = new ArrayList<>();
		for(LocalDate date = start; date.isBefore(end); date = date.plusDays(1)) {
			File folder = new File(args[0] + "/" + date.toString().replace("-", ""));
		    String[] fileList = folder.list(); //Get all the files of the source folder
		    for(int i = 0; i < fileList.length; i++)
		    	list.add(args[0] + "/" + date.toString().replace("-", "") + "/"
		    	+ fileList[i]);
		}
	    String[] fileList = new String[list.size()];
	    fileList = list.toArray(fileList);
	    LDACMatrixTweets ldac = new LDACMatrixTweets();
	    ldac.populateVocab(fileList, args[1], 600);
	    ldac.getMatrix(fileList, args[1], args[2]);
	  }
}