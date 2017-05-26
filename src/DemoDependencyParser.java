import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.corpus.dependency.CoNll.CoNLLSentence;
import com.hankcs.hanlp.corpus.dependency.CoNll.CoNLLWord;

public class DemoDependencyParser
{
    public static void main(String[] args)
    {
        CoNLLSentence sentence = HanLP.parseDependency("����Ժ�������ǿ9��14�������ٿ�����Ժ������飬����ӿ��ƽ���������+������񡱣������������ĸ����̶�������񣻾���������չװ��ʽ�������ƶ���ҵ�ṹ����������");
        System.out.println(sentence);
        // ���Է���ر�����
        for (CoNLLWord word : sentence)
        {
            System.out.printf("%s --(%s)--> %s\n", word.LEMMA, word.DEPREL, word.HEAD.LEMMA);
        }
        // Ҳ����ֱ���õ����飬����˳����������
        CoNLLWord[] wordArray = sentence.getWordArray();
        for (int i = wordArray.length - 1; i >= 0; i--)
        {
            CoNLLWord word = wordArray[i];
            System.out.printf("%s --(%s)--> %s\n", word.LEMMA, word.DEPREL, word.HEAD.LEMMA);
        }
        // ������ֱ�ӱ�����������ĳ��������ĳ���ڵ�һ·���������
        CoNLLWord head = wordArray[12];
        while ((head = head.HEAD) != null)
        {
            if (head == CoNLLWord.ROOT) System.out.println(head.LEMMA);
            else System.out.printf("%s --(%s)--> ", head.LEMMA, head.DEPREL);
        }
    }
}