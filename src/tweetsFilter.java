import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CursorMarkParams;

public class tweetsFilter {
	private String url;
	private HashSet<String> set;
	private int count;
	
	public tweetsFilter(String url) {
		this.url = url;
		this.set = new HashSet<>();
		this.count = 0;
	}
	
	public void download(String period, String outDir, boolean removeHashtag)
			throws SolrServerException, IOException {
		if(this.set.isEmpty()) {
			System.err.println("Please load stopwords first.");
			return;
		}
		boolean done = false;
		SolrClient solr = new HttpSolrClient.Builder(this.url).build();
		SolrQuery query = new SolrQuery();
		query.setRequestHandler("/select");
		query.set("q", "TweetDate: [2017-05-01T00:00:00Z TO 2017-05-01T23:59:59Z]");
		query.set("fl", "tf(Text, 'trump'), TweetId, Text");
		query.set("facet", true);
		query.set("facet.field", "Text");
		query.set("facet.limit", 10);
		query.set("rows", 10);
		query.addSort("id", ORDER.asc);  // Pay attention to this line
		//String cursorMark = CursorMarkParams.CURSOR_MARK_START;
		QueryRequest req = new QueryRequest(query);
		req.setBasicAuthCredentials("ucla", "abc123");
		//while(!done) {
			//query.set(CursorMarkParams.CURSOR_MARK_PARAM, cursorMark);
			QueryResponse rsp = req.process(solr);
			//String nextCursorMark = rsp.getNextCursorMark();
			SolrDocumentList list = rsp.getResults();
			System.out.println(list);
			rsp.getFacetField("Text");
			List<FacetField> fields = rsp.getFacetFields();
			System.out.println(fields);
			//this.segment(outDir, list, removeHashtag);
			//if (cursorMark.equals(nextCursorMark)) {
		        //done = true;
		    //}
		    //cursorMark = nextCursorMark;
		//}
	}
	
	public void loadList(String path) {
		try(Scanner sc = new Scanner(new FileReader(path))) {
			while(sc.hasNextLine())
				set.add(sc.nextLine());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private void segment(String outDir, SolrDocumentList list, boolean removeHashtag) {
		Iterator<SolrDocument> it = list.iterator();
		int numLine = 0;
		StringBuilder sb = new StringBuilder();
		while(it.hasNext()) {
			SolrDocument doc = it.next();
			String fileId = doc.get("TweetId").toString().replaceAll("\"",
					"");
			String record = doc.get("Text").toString().toLowerCase();
			record = record.replace("\\n", " ");
			record = record.replaceAll("[^a-zA-Z #]", "");
			Scanner sc = new Scanner(record);
			sb.append(fileId + " ");
			while(sc.hasNext()) {
				String term = sc.next();
				if(term.length() == 0) continue;
				if(this.set.contains(term)) continue;
				if(term.startsWith("http")) continue;
				if(removeHashtag && term.startsWith("#")) continue;
				if(this.set.contains(term.substring(term.length() - 1,
						term.length())))
						sb.append(term.substring(0, term.length() - 1) +
								" ");
				else
					sb.append(term + " ");
			}
			sc.close();
			sb.append("\n");
			numLine++;
			if(numLine == 5000) {
				try(BufferedWriter writer = new BufferedWriter(new 
						FileWriter(outDir + "/" + count))) {
					writer.write(sb.toString());
				} catch (IOException e) {
					e.printStackTrace();
				}
				numLine = 0;
				count++;
				sb = new StringBuilder();
			}
		}
		
		if(sb.length() > 0) {
			try(BufferedWriter writer = new BufferedWriter(new 
					FileWriter(outDir + "/" + count))) {
				writer.write(sb.toString());
				count++;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
		tweetsFilter filter = new tweetsFilter("https://awesome-ucla.sdsc.edu"
				+ ":8983/solr/uclaaca");
		filter.loadList("Stopwords.txt");
		LocalDate start = LocalDate.parse("2017-03-15"),
		          end   = LocalDate.parse("2017-03-17");
		for(LocalDate date = start; date.isBefore(end); date = date.plusDays(1)) {
			//String dir = date.toString().replace("-", "");
			String dir = "test";
			File outDir = new File(args[0] + "/" + dir);
			outDir.mkdirs();
			try {
				filter.download("TweetDate:{" + date.minusDays(1).toString()
						+ " TO " + date.plusDays(1).toString() + "}", args[0] +
						"/" + dir, false);
			} catch(SolrServerException | IOException e) {
				e.printStackTrace();
			}
		}
		
	}
}