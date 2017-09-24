import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Scanner;

import com.hankcs.hanlp.corpus.dependency.CoNll.CoNLLSentence;
import com.hankcs.hanlp.corpus.dependency.CoNll.CoNLLWord;
import com.hankcs.hanlp.dependency.IDependencyParser;
import com.hankcs.hanlp.dependency.nnparser.NeuralNetworkDependencyParser;
import com.hankcs.hanlp.dictionary.stopword.CoreStopWordDictionary;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.StandardTokenizer;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

public class NewsSegmenter {
  static final String DELI = "(?<=\\)\"})"; //File delimiter
  static final String PUNCT= "(?<=[¡££¿£»])";
  static final String SV = "SBV";
  static final String VO = "VOB";
  static final String CORE = "HED";
  static final String DATE = "1482393600000";
  static final String END = "1482998399000";

  private LinkedList<List<CoNLLWord[]>> docs;
  private LinkedList<String> ids;
  
  public NewsSegmenter(String[] fileList) {
	  this.docs = new LinkedList<>();
	  this.ids = new LinkedList<>();
  }
  
  public static void main (String[] args) {
    File folder = new File(args[0]);
    String[] fileList = folder.list(); //Get all the files of the source folder
    NewsSegmenter nsg = new NewsSegmenter(fileList);
    long start = System.nanoTime();
    //nsg.buildDep(fileList, args[0]);
    long end = System.nanoTime();
    System.out.println("Finished in " + (end - start) + " ns.");
    nsg.HanLP(fileList, args[0], "HanLPOutput", false);
    nsg.HanLP(fileList, args[0], "Stopwords", true);
    //nsg.collapseNoun(fileList, args[0], args[1] + "/NP");
    /*
    nsg.readSingleComp(fileList, args[1] + "/S", "S");
    nsg.readSingleComp(fileList, args[1] + "/V", "V");
    nsg.readSingleComp(fileList, args[1] + "/O", "O");
    nsg.readSV(fileList, args[1] + "/SV");
    nsg.readVO(fileList, args[1] + "/VO");
    nsg.readSO(fileList, args[1] + "/SO");*/
  }
  
  private void buildDep(String[] fileList, String inDir) {
	  Scanner sc;
	  for(int i = 0; i < fileList.length; i++) {
		  try {
			  sc = new Scanner(new FileInputStream(inDir + "/" + fileList[i]), 
					  StandardCharsets.UTF_8.toString());
			  sc.useDelimiter(DELI);
			  IDependencyParser parser = 
					  new NeuralNetworkDependencyParser().enableDeprelTranslator(false);
			  while(sc.hasNext()) {
				  String content = sc.next();
				  DocumentContext document = JsonPath.parse(content);
				  String currDate = document.read("$.date").toString();
				  if(currDate.compareTo(DATE) < 0 || currDate.compareTo(END) > 0)
					  continue;
				  String filename = document.read("$.articleId");
				  this.ids.add(filename);
				  
				  String str = 
						  document.read("$.content").toString().replaceAll(" ", "");
				  String[] sentences = str.split(PUNCT);
				  LinkedList<CoNLLWord[]> doc = new LinkedList<>();
				  this.docs.add(doc);
				  /* Add dependency graph of each sentence*/
				  for(int j = 0; j < sentences.length; j++) {
					  CoNLLSentence sentence = parser.parse(sentences[j]);
					  CoNLLWord[] wordArray = sentence.getWordArray();
					  doc.add(wordArray);
				  }
			  }
			  sc.close();
		  } catch (FileNotFoundException e) {
			  e.printStackTrace();
		  }
	  }
  }
  
  public void HanLP(String[] fileList, String inDir, String outDir,
		  boolean useStopwords) {
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
				  if(useStopwords) {
					  for(Term term: termList) {
						  if(useStopwords && CoreStopWordDictionary.shouldInclude(term)) {
							  writer.write(term.word);
							  writer.write(" ");
						  }
					  }
				  }else {
					  for(Term term: termList) {
						  writer.write(term.word);
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
  
  public void readHanLPContent(String[] fileList, String inDir, String outDir,
		  boolean useStopwords) {
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
					  if(token.contains(": n")) {
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
  
  public void collapseNoun(String[] fileList, String inDir, String outDir) {
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
				  boolean prevNoun = false;
				  for(Term term : termList) {
					  if(sb.length() == 0 || prevNoun)
						  sb.append(term.word);
					  else {
						  sb.append(" ");
						  sb.append(term.word);
					  }
					  
					  if(term.nature.startsWith("n")) {
						  prevNoun = true;
					  }
					  else
						  prevNoun = false;
				  }
				  writer.write(sb.toString());
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
  
  public void readSingleComp(String[] fileList, String outDir, String component) {
	  ListIterator<List<CoNLLWord[]>> docIt = this.docs.listIterator();
	  ListIterator<String> idIt = this.ids.listIterator();
	  while(docIt.hasNext()) {
		  ListIterator<CoNLLWord[]> it = docIt.next().listIterator();
		  String filename = idIt.next();
		  try {
			  PrintWriter writer = new PrintWriter(new OutputStreamWriter(new
					  FileOutputStream(outDir + "/" + filename), 
					  StandardCharsets.UTF_8.toString()));
			  while(it.hasNext()) {
				  CoNLLWord[] wordArray = it.next();
				  for(CoNLLWord term: wordArray) {
					  switch(component) {
					  case "S":
						  if(term.DEPREL.equals(SV)) {
							  writer.write(this.formPhrase(wordArray, term.ID - 1));
							  writer.write(" ");
						  }
						  break;
					  case "V":
						  if(term.DEPREL.equals(SV)) {
							  writer.write(term.HEAD.LEMMA);
							  writer.write(" ");
						  } else if(term.DEPREL.equals(VO)) {
							  writer.write(term.HEAD.LEMMA);
							  writer.write(" ");
						  }
						  break;
					  case "O":
						  if(term.DEPREL.equals(VO)) {
							  writer.write(this.formPhrase(wordArray, term.ID - 1));
							  writer.write(" ");
						  }
						  break;
					  }
				  }
			  }
			  writer.write("\n");
			  writer.close();
		  } catch (UnsupportedEncodingException | FileNotFoundException e) {
			  e.printStackTrace();
		  }
	  }
  }
  
  public void readSO(String[] fileList, String outDir) {
	  ListIterator<List<CoNLLWord[]>> docIt = this.docs.listIterator();
	  ListIterator<String> idIt = this.ids.listIterator();
	  while(docIt.hasNext()) {
		  ListIterator<CoNLLWord[]> it = docIt.next().listIterator();
		  String filename = idIt.next();
		  try {
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(new
					  FileOutputStream(outDir + "/" + filename), 
					  StandardCharsets.UTF_8.toString()));
			while(it.hasNext()) {
				CoNLLWord[] wordArray = it.next();
				HashMap<CoNLLWord, CoNLLWord> map = new HashMap<>();
				  for(CoNLLWord term: wordArray) {
					  if(term.DEPREL.equals(SV))
						  map.put(term.HEAD, term);
				  }
				  
				  for(int k = wordArray.length - 1; k >= 0; k--) {
					  if(!wordArray[k].DEPREL.equals(VO)) continue;
					  if(wordArray[k].HEAD == null) continue;

					  CoNLLWord subject = this.findSbj(wordArray[k].HEAD,
							  map);
					  if(subject == null) continue;
					  writer.write(this.formPhrase(wordArray, subject.ID - 1));
					  writer.write("-");
					  writer.write(this.formPhrase(wordArray, k));
					  writer.write(" ");
				  }
			}
			writer.write("\n");
			writer.close();
		} catch (UnsupportedEncodingException | FileNotFoundException e) {
			e.printStackTrace();
		}
	  }
  }
  
  public void readVO(String[] fileList, String outDir) {
	  ListIterator<List<CoNLLWord[]>> docIt = this.docs.listIterator();
	  ListIterator<String> idIt = this.ids.listIterator();
	  while(docIt.hasNext()) {
		  ListIterator<CoNLLWord[]> it = docIt.next().listIterator();
		  String filename = idIt.next();
		  try {
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(new
					  FileOutputStream(outDir + "/" + filename), 
					  StandardCharsets.UTF_8.toString()));
			while(it.hasNext()) {
				CoNLLWord[] wordArray = it.next();
				HashMap<CoNLLWord, CoNLLWord> map = new HashMap<>();
				for(CoNLLWord term: wordArray) {
					  if(term.DEPREL.equals(SV))
						  map.put(term.HEAD, term);
				  }
				  
				for(int k = wordArray.length - 1; k >= 0; k--) {
					if(!wordArray[k].DEPREL.equals(VO)) continue;
					if(wordArray[k].HEAD == null || 
							(!wordArray[k].HEAD.CPOSTAG.startsWith("v")))
						continue;

					CoNLLWord subject = this.findSbj(wordArray[k].HEAD,
							map);
					if(subject == null) continue;
					writer.write(wordArray[k].HEAD.LEMMA);
					writer.write("-");
					writer.write(this.formPhrase(wordArray, k));
					writer.write(" ");
				}
			}
			writer.write("\n");
			writer.close();
		} catch (UnsupportedEncodingException | FileNotFoundException e) {
			e.printStackTrace();
		}
	  }
  }
  
  public void readSV(String[] fileList, String outDir) {
	  ListIterator<List<CoNLLWord[]>> docIt = this.docs.listIterator();
	  ListIterator<String> idIt = this.ids.listIterator();
	  while(docIt.hasNext()) {
		  ListIterator<CoNLLWord[]> it = docIt.next().listIterator();
		  String filename = idIt.next();
		  try {
			  PrintWriter writer = new PrintWriter(new OutputStreamWriter(new
					  FileOutputStream(outDir + "/" + filename), 
					  StandardCharsets.UTF_8.toString()));
			  while(it.hasNext()) {
				  CoNLLWord[] wordArray = it.next();
				  HashMap<CoNLLWord, CoNLLWord> map = new HashMap<>();
				  for(CoNLLWord term: wordArray) {
					  if(term.DEPREL.equals(SV))
						  map.put(term.HEAD, term);
				  }
				  
				  for(int k = wordArray.length - 1; k >= 0; k--) {
					  if(!wordArray[k].CPOSTAG.startsWith("v")) continue;
					  if(wordArray[k].HEAD == null) continue;

					  CoNLLWord subject = this.findSbj(wordArray[k].HEAD,
							  map);
					  if(subject == null) continue;
					  writer.write(this.formPhrase(wordArray, subject.ID - 1));
					  writer.write("-");
					  writer.write(wordArray[k].LEMMA);
					  writer.write(" ");
				  }
			  }
			  writer.write("\n");
			  writer.close();
		  } catch (UnsupportedEncodingException | FileNotFoundException e) {
			  e.printStackTrace();
		  }
	  }
  }
  
  public boolean isPunc(String str) {
	  if(str.equals("£¬") || str.equals("¡¢") || str.equals("¡£") ||
			  str.equals("¡°") || str.equals("¡±") || str.equals("¡®") ||
			  str.equals("¡¯") || str.equals(" ") || str.equals("£¨")
			  || str.endsWith("£©"))
		  return true;
	  return false;
  }
  
  private CoNLLWord findSbj(CoNLLWord object, HashMap<CoNLLWord, CoNLLWord> map) {
	  while(object.HEAD != null) {
		  if(map.containsKey(object))
			  return map.get(object);
		  if(object.DEPREL.equals("COO") || object.DEPREL.equals(VO))
			  object = object.HEAD;
		  else
			  return null;
	  }
	return null;
  }
  
  private String formPhrase(CoNLLWord[] wordArray, int k) {
	  StringBuilder sb = new StringBuilder();
	  int i = k;
	  while(i > 0) {
		  if(wordArray[i - 1].POSTAG.startsWith("n"))
			  i--;
		  else break;
	  }
	  while(i <= k) {
		  sb.append(wordArray[i].LEMMA);
		  i++;
	  }
	  return sb.toString();
  }
}