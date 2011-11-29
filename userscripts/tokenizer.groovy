import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.util.Version;
import org.apache.lucene.analysis.shingle.ShingleAnalyzerWrapper;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.search.suggest.tst.TSTLookup;
import org.apache.lucene.search.suggest.tst.TSTAutocomplete;
import org.apache.lucene.search.suggest.tst.TernaryTreeNode;

def lookUp = new TSTLookup();

def analyzer = new ShingleAnalyzerWrapper(Version.LUCENE_CURRENT, 2, 3)
analyzer.setOutputUnigrams(true);

def tokenStream = analyzer.tokenStream("name", new StringReader('I am Sravanthi'));
OffsetAttribute offsetAttribute = tokenStream.getAttribute(OffsetAttribute.class);
CharTermAttribute charTermAttribute = tokenStream.getAttribute(CharTermAttribute.class);

while (tokenStream.incrementToken()) {
    int startOffset = offsetAttribute.startOffset();
    int endOffset = offsetAttribute.endOffset();
    String term = charTermAttribute.toString();
    println term;
    //lookUp.insert(root, term, term, 0)
    lookUp.add(term, term);
}

//def resultNode = lookUp.prefixCompletion(root, 'sravan', 0);
println lookUp.lookup('sravanthi', false, 10);

