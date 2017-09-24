import java.util.Collection;

import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.trees.international.pennchinese.ChineseGrammaticalStructure;

public class DemoStanfordParser {
	public static void main(String[] args) {
		String modelPath = "edu/stanford/nlp/models/lexparser/xinhuaFactoredSegmenting.ser.gz";
		String text = "������(һ��ԭ��)֣�ݻ������귿�ز��������޹�˾(���¼��Ϊ���ȹ�˾)";
		LexicalizedParser lp = LexicalizedParser.loadModel(modelPath);
		Tree t = lp.parse(text);
		t.pennPrint();
		ChineseGrammaticalStructure gs =  new ChineseGrammaticalStructure(t);
		Collection<TypedDependency> tdl = gs.typedDependenciesCollapsed();
		System.out.println(tdl.toString());
		String s = "";
		for(int i = 0; i < tdl.size(); i++) {
			TypedDependency td = (TypedDependency)tdl.toArray()[i];
			String age = td.dep().toString();
			s+= age + "/";
			s+= " ";
		}
		System.out.println(s);
	}
}