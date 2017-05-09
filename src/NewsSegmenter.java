import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import org.json.JSONArray;

import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.CRF.CRFSegment;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

public class NewsSegmenter {
static final String DELI = "(?<=]})"; //File delimiter
  
  public static void main (String[] args) {
    Segment segment = new CRFSegment();
    segment.enablePartOfSpeechTagging(false);
    File folder = new File(args[0]);
    String[] fileList = folder.list(); //Get all the files of the source folder

    Scanner sc;
    for(int i = 0; i < fileList.length; i++) {
      try {
        sc = new Scanner(new FileInputStream(args[0] + "/" + fileList[i]), 
            StandardCharsets.UTF_8.toString());
        sc.useDelimiter(DELI);
        while(sc.hasNext()) {
          String content = sc.next();
          DocumentContext document = JsonPath.parse(content);
          String filename = document.read("$.articleId");
          PrintWriter writer = new PrintWriter(new OutputStreamWriter(new
              FileOutputStream(args[1] + "/" + filename), 
              StandardCharsets.UTF_8.toString()));
          JSONArray jarray = new JSONArray(document.read("$.Content-halLP").toString());
          for(int j = 0; j < jarray.length(); j++) {
            String token = jarray.getString(j);
            if(token.contains("/n")) {
              writer.write(token.substring(0, token.indexOf('/'))+ " ");
            }
          }
          writer.write("\n");
          writer.close();
        }
        sc.close();
      } catch (FileNotFoundException | UnsupportedEncodingException e) {
        e.printStackTrace();
      }
    }
  }
}