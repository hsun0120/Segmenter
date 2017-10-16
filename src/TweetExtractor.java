import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Scanner;

import org.json.JSONObject;

public class TweetExtractor {
	private HashSet<String> stopwords;
	
	public TweetExtractor() {
		this.stopwords = new HashSet<>();
	}
	
	public void extract(String filename, String outDir, boolean removeHashtag) {
		try (BufferedReader in = new BufferedReader(new 
				InputStreamReader(new FileInputStream(filename)))) {
			String line = null;
			while((line = in.readLine()) != null) {
				if(!line.startsWith(",") && !line.startsWith("[")) continue;
				line = line.substring(1, line.length());
				JSONObject doc = new JSONObject(line);
				String id = doc.get("id").toString();
				String record = doc.get("text").toString().toLowerCase();
				record = record.replace("\\n", " ");
				record = record.replaceAll("[^a-zA-Z #]", "");
				Scanner sc = new Scanner(record);
				try(BufferedWriter writer = new BufferedWriter(new 
						FileWriter(outDir + "/" + id))) {
					while(sc.hasNext()) {
						String term = sc.next();
						if(term.length() == 0) continue;
						if(this.stopwords.contains(term)) continue;
						if(term.startsWith("http")) continue;
						if(removeHashtag && term.startsWith("#")) continue;
						if(this.stopwords.contains(term.substring(term.length() - 1,
								term.length())))
								writer.write(term.substring(0, term.length() - 1) +
										" ");
						else
							writer.write(term + " ");
					}
					sc.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void loadStopwords(String filename) {
		try {
			Scanner sc = new Scanner(new FileInputStream(filename));
			while(sc.hasNextLine()) 
				this.stopwords.add(sc.nextLine());
			sc.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		TweetExtractor te = new TweetExtractor();
		te.loadStopwords(args[0]);
		te.extract(args[1], args[2], false);
	}
}