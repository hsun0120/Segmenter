import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.corpus.dependency.CoNll.CoNLLSentence;
import com.hankcs.hanlp.corpus.dependency.CoNll.CoNLLWord;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.StandardTokenizer;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

public class NewsSegmenter {
static final String DELI = "(?<=\\)\"})"; //File delimiter
static final String PUNCT= "[。?;]";
static final String SV = "主谓关系";
static final String VO = "动宾关系";
static final String CORE = "核心关系";
static final String DATE = "1473922800000";
static final String END = "1474415979927";
  
  public static void main (String[] args) {
    File folder = new File(args[0]);
    String[] fileList = folder.list(); //Get all the files of the source folder
    NewsSegmenter nsg = new NewsSegmenter();
    long start = System.nanoTime();
    if(args[2].equals("content"))
    	nsg.readContent(fileList, args[0], args[1]);
    else if(args[2].equals("NLP"))
    	nsg.readHanLPContent(fileList, args[0], args[1]);
    else if (args[2].equals("NP"))
    	nsg.readNP(fileList, args[0], args[1]);
    else if (args[2].equals("SVO"))
    	nsg.readDP(fileList, args[0], args[1]);
    long end = System.nanoTime();
    System.out.println(end - start);
  }
  
  public void readContent(String[] fileList, String inDir, String outDir) {
	  Scanner sc;
	  for(int i = 0; i < fileList.length; i++) {
		  try {
			  sc = new Scanner(new FileInputStream(inDir + "/" + fileList[i]), 
					  StandardCharsets.UTF_8.toString());
			  sc.useDelimiter(DELI);
			  while(sc.hasNext()) {
				  String content = sc.next();
				  DocumentContext document = JsonPath.parse(content);
				  String currDate = document.read("$.date").toString();
				  if(currDate.compareTo(DATE) < 0 || currDate.compareTo(END) > 0)
					  continue;
				  String filename = document.read("$.articleId");
				  PrintWriter writer = new PrintWriter(new OutputStreamWriter(new
						  FileOutputStream(outDir + "/" + filename), 
						  StandardCharsets.UTF_8.toString()));
				  String str = document.read("$.content").toString();
				 
				  String[] arr = str.split(" ");
				  for(int j = 0; j < arr.length; j++) {
					  if(!this.isPunc(arr[j]))
						  writer.write(arr[j].replaceAll(" ", "") + " ");
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
  
  public void readHanLPContent(String[] fileList, String inDir, String outDir) {
	  Scanner sc;
	  for(int i = 0; i < fileList.length; i++) {
		  try {
			  sc = new Scanner(new FileInputStream(inDir + "/" + fileList[i]), 
					  StandardCharsets.UTF_8.toString());
			  sc.useDelimiter(DELI);
			  while(sc.hasNext()) {
				  String content = sc.next();
				  DocumentContext document = JsonPath.parse(content);
				  String currDate = document.read("$.date").toString();
				  if(currDate.compareTo(DATE) < 0 || currDate.compareTo(END) > 0)
					  continue;
				  String filename = document.read("$.articleId");
				  PrintWriter writer = new PrintWriter(new OutputStreamWriter(new
						  FileOutputStream(outDir + "/" + filename), 
						  StandardCharsets.UTF_8.toString()));
				  String str = document.read("$.Content-HNLP").toString();
				  String[] arr = str.split("\\)");
				  for(int j = 0; j < arr.length; j++) {
					  String token = arr[j].trim();
					  if(!this.isPunc(token) && token.contains(": n")) {
						  writer.write(token.substring(0, token.indexOf("(")) );
						  if(j + 1 < arr.length)
							  writer.write(" ");
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
  
  public void readNP(String[] fileList, String inDir, String outDir) {
	  Scanner sc;
	  for(int i = 0; i < fileList.length; i++) {
		  try {
			  sc = new Scanner(new FileInputStream(inDir + "/" + fileList[i]), 
					  StandardCharsets.UTF_8.toString());
			  sc.useDelimiter(DELI);
			  while(sc.hasNext()) {
				  String content = sc.next();
				  DocumentContext document = JsonPath.parse(content);
				  String currDate = document.read("$.date").toString();
				  if(currDate.compareTo(DATE) < 0 || currDate.compareTo(END) > 0)
					  continue;
				  String filename = document.read("$.articleId");
				  PrintWriter writer = new PrintWriter(new OutputStreamWriter(new
						  FileOutputStream(outDir + "/" + filename), 
						  StandardCharsets.UTF_8.toString()));
				  String str = 
						  document.read("$.content").toString().replaceAll(" ", "");
				  List<Term> termList = StandardTokenizer.segment(str);
				  StringBuilder sb = new StringBuilder();
				  for(Term term : termList) {
					  if(term.nature.startsWith("n") || term.nature.startsWith("a")) {
						  sb.append(term.word);
					  }
					  else {
						  if(sb.length() > 0) {
							  writer.write(sb.toString() + " ");
							  sb = new StringBuilder();
						  }
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
  
  public void readDP(String[] fileList, String inDir, String outDir) {
	  Scanner sc;
	  for(int i = 0; i < fileList.length; i++) {
		  try {
			  sc = new Scanner(new FileInputStream(inDir + "/" + fileList[i]), 
					  StandardCharsets.UTF_8.toString());
			  sc.useDelimiter(DELI);
			  while(sc.hasNext()) {
				  String content = sc.next();
				  DocumentContext document = JsonPath.parse(content);
				  String currDate = document.read("$.date").toString();
				  if(currDate.compareTo(DATE) < 0 || currDate.compareTo(END) > 0)
					  continue;
				  String filename = document.read("$.articleId");
				  PrintWriter writer = new PrintWriter(new OutputStreamWriter(new
						  FileOutputStream(outDir + "/" + filename), 
						  StandardCharsets.UTF_8.toString()));
				  String str = 
						  document.read("$.content").toString().replaceAll(" ", "");
				  String[] sentences = str.split(PUNCT);
				  for(int j = 0; j < sentences.length; j++) {
					  CoNLLSentence sentence = HanLP.parseDependency(sentences[j]);
					  CoNLLWord[] wordArray = sentence.getWordArray();
					  
					  for(CoNLLWord subject : wordArray) {
						  if(!subject.DEPREL.equals(SV)) continue;
						  CoNLLWord verb = subject.HEAD;
						  
						  for (int k = 0; k < wordArray.length; k++) {
					         CoNLLWord word = wordArray[k];
					         if(word.DEPREL.equals(VO) && isPath(word, verb)){
					        	writer.write(subject.LEMMA);
						        writer.write("-");
						        writer.write(word.LEMMA);
						        writer.write(" ");
					         }
					         /*
					         else if(word.HEAD.equals(head) && word.DEPREL.equals(VO)) {
					        	 writer.write(word.LEMMA);
					        	 prev = word;
					         }*/
					      }
					  }
					  
				      //writer.write(" ");
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
  
  public boolean isPunc(String str) {
	  if(str.equals("，") || str.equals("、") || str.equals("。") ||
			  str.equals("“") || str.equals("”") || str.equals("‘") ||
			  str.equals("’") || str.equals(" ") || str.equals("（")
			  || str.endsWith("）"))
		  return true;
	  return false;
  }
  
  private boolean isPath(CoNLLWord word, CoNLLWord dest) {
	  CoNLLWord curr = word;
	  while(curr.HEAD != null) {
		  if(curr.HEAD.equals(dest))
			  return true;
		  else
			  curr = curr.HEAD;
	  }
	return false;
  }
}