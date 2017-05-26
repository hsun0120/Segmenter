import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.CRF.CRFSegment;
import com.hankcs.hanlp.seg.common.Term;

import com.jayway.jsonpath.*;

public class segmenter {
  static final String DELI = "\\Z"; //File delimiter
  
  public static void main (String[] args) {
    Segment segment = new CRFSegment();
    segment.enablePartOfSpeechTagging(false);
    File folder = new File(args[0]);
    String[] fileList = folder.list(); //Get all the files of the source folder
    
    ArrayList<String> fields = new ArrayList<>();
    fields.add("$.content");

    Scanner sc;
    long start = System.nanoTime();
    for(int i = 0; i < fileList.length; i++) {
      try {
        sc = new Scanner(new FileInputStream(args[0] + "/" + fileList[i]), 
            StandardCharsets.UTF_8.toString());
        sc.useDelimiter(DELI);
        String content = sc.next();
        DocumentContext document = JsonPath.parse(content);
        String parsed = "";
        for(int j = 0; j < fields.size(); j++) {
          String path = fields.get(j);
          parsed += document.read(path).toString();
        }
        List<Term> termList = segment.seg(parsed);
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(new
            FileOutputStream(args[1] + "/" + fileList[i]), 
            StandardCharsets.UTF_8.toString()));
        for (Term term : termList)
        {
          if(term.nature != null && term.nature.startsWith('n') && 
              !StringUtils.isNumeric(term.word) && !Pattern.matches("\\p{Punct}", term.word)) 
            writer.write(term.word + " ");
        }
        writer.write("\n");
        writer.close();
        sc.close();
      } catch (FileNotFoundException | UnsupportedEncodingException e) {
        e.printStackTrace();
      }
    }
    long end = System.nanoTime();
    System.out.println(end - start);
  }
}