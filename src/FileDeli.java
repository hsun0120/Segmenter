import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

public class FileDeli {
	static final String DELI = "(?<=\\)\"})"; //File delimiter
	static final String DATE = "1473922800000";
	static final String END = "1474415979927";
	public static void main (String[] args) {
		File folder = new File(args[0]);
	    String[] fileList = folder.list(); //Get all the files of the source folder
	    Scanner sc;
		  for(int i = 0; i < fileList.length; i++) {
			  try {
				  sc = new Scanner(new FileInputStream(args[0] + "/" + fileList[i]), 
						  StandardCharsets.UTF_8.toString());
				  sc.useDelimiter(DELI);
				  while(sc.hasNext()) {
					  String content = sc.next().replaceAll(" ", "");
					  DocumentContext document = JsonPath.parse(content);
					  String currDate = document.read("$.date").toString();
					  if(currDate.compareTo(DATE) < 0 || currDate.compareTo(END) > 0)
						  continue;
					  String filename = document.read("$.articleId");
					  PrintWriter writer = new PrintWriter(new OutputStreamWriter(new
							  FileOutputStream(args[1] + "/" + filename), 
							  StandardCharsets.UTF_8.toString()));
					  writer.write(content);
					  writer.close();
				  }
				  sc.close();
			  } catch (FileNotFoundException | UnsupportedEncodingException e) {
				  e.printStackTrace();
			  }
		  }
	}
}