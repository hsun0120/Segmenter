import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;

public class solrQuery {
	static final String server = "https://awesome-ucla.sdsc.edu:8983/solr/"
			+ "uclaaca";
	
	private HashSet<String> set;
	
	public solrQuery() {
		this.set = new HashSet<>();
	}
	
	public void executeQuery(String filename) throws SolrServerException, IOException {
		this.loadList("sw.txt");
		SolrClient solr = new HttpSolrClient.Builder(server).build();
		SolrQuery query = new SolrQuery();
		int limit = 200;
		query.setRequestHandler("/select");
		query.set("q", "Text:healthcare OR health care");
		query.set("row", 0);
		query.set("facet", true);
		query.set("facet.field", "Text");
		query.set("facet.limit", limit);
		
		int page = 0;
		int count = 0;
		try(BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
			while(true) {
				query.set("facet.offset", page * limit);
				QueryRequest req = new QueryRequest(query);
				req.setBasicAuthCredentials("ucla", "abc123");
				QueryResponse rsp = req.process(solr);
				FacetField histogram= rsp.getFacetField("Text");
				if(histogram == null) break;

				List<Count> vals = histogram.getValues();
				System.out.println(page);
				for(Count val: vals) {
					if(!set.contains(val.getName())) {
						writer.write(val + "\n");
						count++;
					}
					if(count == 200) return;
				}
				page++;
			}
		}
		
	}
	
	private void loadList(String listName) {
		try(Scanner sc = new Scanner(new FileReader(listName))) {
			while(sc.hasNextLine())
				set.add(sc.nextLine());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		solrQuery query = new solrQuery();
		try {
			query.executeQuery("healthcare.txt");
		} catch (SolrServerException | IOException e) {
			e.printStackTrace();
		}
	}
}