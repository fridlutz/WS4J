package edu.uniba.di.lacam.kdde.ws4j.similarity;

import static org.junit.Assert.assertEquals;

import edu.uniba.di.lacam.kdde.lexical_db.ILexicalDatabase;
import edu.uniba.di.lacam.kdde.lexical_db.MITWordNet;
import edu.uniba.di.lacam.kdde.lexical_db.data.Concept;
import edu.uniba.di.lacam.kdde.ws4j.RelatednessCalculator;
import edu.uniba.di.lacam.kdde.ws4j.RelatednessCalculatorTest;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test class for {@link WuPalmer}.
 *
 * @author Hideki Shima
 */
public class WuPalmerTest extends RelatednessCalculatorTest {

	private static RelatednessCalculator rc;
	
	@BeforeClass
	public static void oneTimeSetUp() {
		ILexicalDatabase db = new MITWordNet();
		rc = new WuPalmer(db);
	}
	
	/**
	 * Test method for {@link WuPalmer#calcRelatednessOfSynsets(Concept, Concept)}.
	 */
	@Test
	public void testHappyPathOnSynsets() {
		assertEquals(0.9565, rc.calcRelatednessOfSynsets(n1Synsets.get(1), n2Synsets.get(0)).getScore(), 0.0001D);
		assertEquals(0.6957, rc.calcRelatednessOfSynsets(n1Synsets.get(0), n2Synsets.get(0)).getScore(), 0.0001D);
		assertEquals(0.8571, rc.calcRelatednessOfSynsets(v1Synsets.get(0), v2Synsets.get(0)).getScore(), 0.0001D);
		assertEquals(0.5714, rc.calcRelatednessOfSynsets(v1Synsets.get(1), v2Synsets.get(0)).getScore(), 0.0001D);
	}

	/**
	 * Test method for {@link RelatednessCalculator#calcRelatednessOfWords(String, String)}.
	 */
	@Test
	public void testHappyPathOnWords() {
		assertEquals(0.9565, rc.calcRelatednessOfWords(n1, n2), 0.0001D);
		assertEquals(0.8571, rc.calcRelatednessOfWords(v1, v2), 0.0001D);
	}
	
	/**
	 * Test method for {@link RelatednessCalculator#calcRelatednessOfWords(String, String)}.
	 */
	@Test
	public void testHappyPathOnWordsWithPOS() {
		assertEquals(0.8750, rc.calcRelatednessOfWords(nv1 + "#n", nv2 + "#n"), 0.0001D);
		assertEquals(0.0000, rc.calcRelatednessOfWords(nv1 + "#n", nv2 + "#v"), 0.0001D);
		assertEquals(0.0000, rc.calcRelatednessOfWords(nv1 + "#v", nv2 + "#n"), 0.0001D);
		assertEquals(0.8333, rc.calcRelatednessOfWords(nv1 + "#v", nv2 + "#v"), 0.0001D);
		assertEquals(0.0000, rc.calcRelatednessOfWords(nv1 + "#other", nv2 + "#other"), 0.0001D);
	}

	@Test
	public void testOnSameSynsets() {
		assertEquals(rc.getMax(), rc.calcRelatednessOfSynsets(n1Synsets.get(0), n1Synsets.get(0)).getScore(), 0.0001D);
	}
	
	@Test
	public void testOnUnknownSynsets() {
		assertEquals(rc.getMin(), rc.calcRelatednessOfSynsets(null, n1Synsets.get(0)).getScore(), 0.0001D);
		assertEquals(rc.getMin(), rc.calcRelatednessOfWords(null, n1), 0.0001D);
		assertEquals(rc.getMin(), rc.calcRelatednessOfWords("", n1), 0.0001D);
	}
}