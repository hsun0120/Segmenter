import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Scanner;

import org.json.JSONObject;

import com.hankcs.hanlp.dictionary.stopword.CoreStopWordDictionary;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.StandardTokenizer;

public class courtCaseSegmenter {
	public void segment(String[] fileList, String outDir, List<String> fields,
			String fileId, boolean useStopwords) throws IOException {
		Scanner sc;
		for(int i = 0; i < fileList.length; i++) {
			try {
				sc = new Scanner(new FileInputStream(fileList[i]), 
						StandardCharsets.UTF_8.toString());
				sc.useDelimiter("\\Z");
				while(sc.hasNext()) {
					JSONObject json = new JSONObject(sc.next());
					int date = json.getInt("year_id");
					if(date < 2014) continue;
					
					String filename = json.getString(fileId);
					int end = filename.indexOf(".");
					filename = filename.substring(0, end);
					boolean containsTerm = false;
					ListIterator<String> it = fields.listIterator();
					LinkedList<List<Term>> contents = new LinkedList<>();
					while(it.hasNext()) {
						String field = it.next();

						String content = json.get(field).toString();
						if(content == null || content.length() == 0) continue;
						/* String clean-up */
						int start = content.indexOf('\\');
						if(start < 0) continue; //Empty field
						content = content.substring(start);
						content = content.replaceAll("\",\"", "");
						content = content.replaceAll("\"", "");
						content = content.replaceAll("\\\\", "");
						content = content.replaceAll("\\[", "");
						content = content.replaceAll("\\]", "");
						content = content.replaceAll("\\{", "");
						content = content.replaceAll("\\}", "");
						
						List<Term> termList = StandardTokenizer.segment(content);
						contents.add(termList);
						if(!containsTerm)
							for(Term term: termList)
								if(term.word.equals("ЩЫВа"))
									containsTerm = true;
					}
					
					if(!containsTerm) continue;

					try (BufferedWriter writer = new BufferedWriter(new
							OutputStreamWriter(new FileOutputStream(outDir + "/" +
									filename), StandardCharsets.UTF_8))){
						ListIterator<List<Term>> iter = contents.listIterator();
						while(iter.hasNext()) {
							List<Term> text = iter.next();
							if(useStopwords) {
								for(Term term: text) {
									if(useStopwords &&
											CoreStopWordDictionary.shouldInclude(term)) {
										writer.write(term.word);
										writer.write(" ");
									}
								}
							}else {
								for(Term term: text) {
									writer.write(term.word);
									writer.write(" ");
								}
							}
							writer.write("\n");
						}
					}
				}
				sc.close();
			} catch (FileNotFoundException | UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
	    LinkedList<String> fields = new LinkedList<>();
	    fields.add("facts");
	    fields.add("holding");
	    
	    courtCaseSegmenter seg = new courtCaseSegmenter();
	    File folder = new File(args[0]);
	    String[] fileList = folder.list();
	    for(int i = 0; i < fileList.length; i++)
	    	fileList[i] = args[0] + "/" + fileList[i];
	    
	    long start = System.nanoTime();
	    try {
	    	seg.segment(fileList, args[1], fields, "file_id", true);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	    long end = System.nanoTime();
	    System.out.println("Total time: " + (end - start) + "ns.");
	}
}