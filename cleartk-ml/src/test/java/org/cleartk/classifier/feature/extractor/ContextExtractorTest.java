/*
 * Copyright (c) 2011, Regents of the University of Colorado 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer. 
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution. 
 * Neither the name of the University of Colorado at Boulder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission. 
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE. 
 */
package org.cleartk.classifier.feature.extractor;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.extractor.ContextExtractor.Bag;
import org.cleartk.classifier.feature.extractor.ContextExtractor.Covered;
import org.cleartk.classifier.feature.extractor.ContextExtractor.FirstCovered;
import org.cleartk.classifier.feature.extractor.ContextExtractor.Focus;
import org.cleartk.classifier.feature.extractor.ContextExtractor.Following;
import org.cleartk.classifier.feature.extractor.ContextExtractor.LastCovered;
import org.cleartk.classifier.feature.extractor.ContextExtractor.Ngram;
import org.cleartk.classifier.feature.extractor.ContextExtractor.Preceding;
import org.cleartk.classifier.feature.extractor.simple.SpannedTextExtractor;
import org.cleartk.classifier.feature.extractor.simple.TypePathExtractor;
import org.cleartk.test.CleartkTestBase;
import org.cleartk.type.test.Chunk;
import org.cleartk.type.test.Sentence;
import org.cleartk.type.test.Token;
import org.junit.Before;
import org.junit.Test;
import org.uimafit.testing.factory.TokenBuilder;
import org.uimafit.util.JCasUtil;

/**
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public class ContextExtractorTest extends CleartkTestBase {

  protected TokenBuilder<Token, Sentence> tokenBuilder;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    this.tokenBuilder = new TokenBuilder<Token, Sentence>(
        Token.class,
        Sentence.class,
        "pos",
        "stem");
  }

  @Override
  public String[] getTypeSystemDescriptorNames() {
    return new String[] { "org.cleartk.type.test.TestTypeSystem" };
  }

  @Test
  public void testBasic() throws Exception {
    ContextExtractor<Token> extractor = new ContextExtractor<Token>(
        Token.class,
        new SpannedTextExtractor(),
        new Preceding(2),
        new Preceding(3, 6),
        new Covered(),
        new FirstCovered(1),
        new FirstCovered(1, 3),
        new LastCovered(1),
        new LastCovered(1, 3),
        new Following(1, 3),
        new Following(3, 5));

    this.tokenBuilder.buildTokens(
        this.jCas,
        "The quick brown fox jumped over the lazy dog.",
        "The quick brown fox jumped over the lazy dog .",
        "DT JJ JJ NN VBD IN DT JJ NN .");
    Chunk chunk = new Chunk(this.jCas, 20, 31);
    chunk.addToIndexes();
    Assert.assertEquals("jumped over", chunk.getCoveredText());

    List<Feature> features = extractor.extract(this.jCas, chunk);
    Assert.assertEquals(17, features.size());
    Iterator<Feature> iter = features.iterator();
    this.assertFeature("Preceding_0_2_1", "brown", iter.next());
    this.assertFeature("Preceding_0_2_0", "fox", iter.next());
    this.assertFeature("Preceding_3_6_5", "OOB2", iter.next());
    this.assertFeature("Preceding_3_6_4", "OOB1", iter.next());
    this.assertFeature("Preceding_3_6_3", "The", iter.next());
    this.assertFeature("Covered_0", "jumped", iter.next());
    this.assertFeature("Covered_1", "over", iter.next());
    this.assertFeature("FirstCovered_0_1_0", "jumped", iter.next());
    this.assertFeature("FirstCovered_1_3_1", "over", iter.next());
    this.assertFeature("FirstCovered_1_3_2", "OOB1", iter.next());
    this.assertFeature("LastCovered_0_1_0", "over", iter.next());
    this.assertFeature("LastCovered_1_3_2", "OOB1", iter.next());
    this.assertFeature("LastCovered_1_3_1", "jumped", iter.next());
    this.assertFeature("Following_1_3_1", "lazy", iter.next());
    this.assertFeature("Following_1_3_2", "dog", iter.next());
    this.assertFeature("Following_3_5_3", ".", iter.next());
    this.assertFeature("Following_3_5_4", "OOB1", iter.next());
  }

  @Test
  public void testBag() throws Exception {
    ContextExtractor<Token> extractor = new ContextExtractor<Token>(
        Token.class,
        new TypePathExtractor(Token.class, "pos"),
        new Bag(new Preceding(2)),
        new Bag(new Preceding(3, 6)),
        new Bag(new FirstCovered(1), new LastCovered(1)),
        new Bag(new Following(1, 3)),
        new Bag(new Following(3, 5)),
        new Bag(new Preceding(1), new Following(1)));

    this.tokenBuilder.buildTokens(
        this.jCas,
        "The quick brown fox jumped over the lazy dog.",
        "The quick brown fox jumped over the lazy dog .",
        "DT JJ JJ NN VBD IN DT JJ NN .");
    Chunk chunk = new Chunk(this.jCas, 20, 31);
    chunk.addToIndexes();
    Assert.assertEquals("jumped over", chunk.getCoveredText());

    List<Feature> features = extractor.extract(this.jCas, chunk);
    Assert.assertEquals(13, features.size());
    Iterator<Feature> iter = features.iterator();
    this.assertFeature("Bag_Preceding_0_2_TypePath(Pos)", "JJ", iter.next());
    this.assertFeature("Bag_Preceding_0_2_TypePath(Pos)", "NN", iter.next());
    this.assertFeature("Bag_Preceding_3_6", "OOB2", iter.next());
    this.assertFeature("Bag_Preceding_3_6", "OOB1", iter.next());
    this.assertFeature("Bag_Preceding_3_6_TypePath(Pos)", "DT", iter.next());
    this.assertFeature("Bag_FirstCovered_0_1_LastCovered_0_1_TypePath(Pos)", "VBD", iter.next());
    this.assertFeature("Bag_FirstCovered_0_1_LastCovered_0_1_TypePath(Pos)", "IN", iter.next());
    this.assertFeature("Bag_Following_1_3_TypePath(Pos)", "JJ", iter.next());
    this.assertFeature("Bag_Following_1_3_TypePath(Pos)", "NN", iter.next());
    this.assertFeature("Bag_Following_3_5_TypePath(Pos)", ".", iter.next());
    this.assertFeature("Bag_Following_3_5", "OOB1", iter.next());
    this.assertFeature("Bag_Preceding_0_1_Following_0_1_TypePath(Pos)", "NN", iter.next());
    this.assertFeature("Bag_Preceding_0_1_Following_0_1_TypePath(Pos)", "DT", iter.next());
  }

  @Test
  public void testNgram() throws Exception {
    ContextExtractor<Token> extractor = new ContextExtractor<Token>(
        Token.class,
        new SpannedTextExtractor(),
        new Ngram(new Preceding(2)),
        new Ngram(new Preceding(3, 6)),
        new Ngram(new Preceding(1), new FirstCovered(1), new LastCovered(1)),
        new Ngram(new Following(1, 3)),
        new Ngram(new Following(3, 5)),
        new Ngram(new Preceding(2), new Following(1, 2)));

    this.tokenBuilder.buildTokens(
        this.jCas,
        "The quick brown fox jumped over the lazy dog.",
        "The quick brown fox jumped over the lazy dog .",
        "DT JJ JJ NN VBD IN DT JJ NN .");
    Chunk chunk = new Chunk(this.jCas, 20, 31);
    chunk.addToIndexes();
    Assert.assertEquals("jumped over", chunk.getCoveredText());

    List<Feature> features = extractor.extract(this.jCas, chunk);
    Assert.assertEquals(6, features.size());
    Iterator<Feature> iter = features.iterator();
    this.assertFeature("Ngram_Preceding_0_2", "brown_fox", iter.next());
    this.assertFeature("Ngram_Preceding_3_6", "OOB2_OOB1_The", iter.next());
    this.assertFeature(
        "Ngram_Preceding_0_1_FirstCovered_0_1_LastCovered_0_1",
        "fox_jumped_over",
        iter.next());
    this.assertFeature("Ngram_Following_1_3", "lazy_dog", iter.next());
    this.assertFeature("Ngram_Following_3_5", "._OOB1", iter.next());
    this.assertFeature("Ngram_Preceding_0_2_Following_1_2", "brown_fox_lazy", iter.next());
  }

  @Test
  public void testFocus() throws Exception {
    ContextExtractor<Token> extractor = new ContextExtractor<Token>(
        Token.class,
        new SpannedTextExtractor(),
        new Focus(),
        new Bag(new Preceding(1), new Focus()),
        new Ngram(new Following(2), new Focus()));

    this.tokenBuilder.buildTokens(
        this.jCas,
        "The quick brown fox jumped over the lazy dog.",
        "The quick brown fox jumped over the lazy dog .",
        "DT JJ JJ NN VBD IN DT JJ NN .");
    Token jumped = JCasUtil.selectByIndex(this.jCas, Token.class, 4);
    Assert.assertEquals("jumped", jumped.getCoveredText());

    List<Feature> features = extractor.extract(this.jCas, jumped);
    Assert.assertEquals(4, features.size());
    Iterator<Feature> iter = features.iterator();
    this.assertFeature("Focus", "jumped", iter.next());
    this.assertFeature("Bag_Preceding_0_1_Focus", "fox", iter.next());
    this.assertFeature("Bag_Preceding_0_1_Focus", "jumped", iter.next());
    this.assertFeature("Ngram_Following_0_2_Focus", "over_the_jumped", iter.next());

    ContextExtractor<Chunk> chunkExtractor = new ContextExtractor<Chunk>(
        Chunk.class,
        new SpannedTextExtractor(),
        new Focus());
    try {
      chunkExtractor.extract(this.jCas, jumped);
      Assert.fail("Expected exception from Focus of wrong type");
    } catch (IllegalArgumentException e) {
    }
  }

  @Test
  public void testBounds() throws Exception {
    ContextExtractor<Token> extractor = new ContextExtractor<Token>(
        Token.class,
        new SpannedTextExtractor(),
        new Preceding(2),
        new LastCovered(1),
        new Following(3));

    this.tokenBuilder.buildTokens(
        this.jCas,
        "She bought milk.\nHe sold oranges.",
        "She bought milk .\nHe sold oranges .");
    Chunk boughMilk = new Chunk(this.jCas, 4, 15);
    boughMilk.addToIndexes();
    Assert.assertEquals("bought milk", boughMilk.getCoveredText());
    Chunk soldOranges = new Chunk(this.jCas, 20, 32);
    soldOranges.addToIndexes();
    Assert.assertEquals("sold oranges", soldOranges.getCoveredText());
    Collection<Sentence> sentences = JCasUtil.select(jCas, Sentence.class);
    Assert.assertEquals(2, sentences.size());
    Iterator<Sentence> sentIter = sentences.iterator();
    Sentence sent1 = sentIter.next();
    Sentence sent2 = sentIter.next();

    List<Feature> features = extractor.extractWithin(this.jCas, boughMilk, sent1);
    Assert.assertEquals(6, features.size());
    Iterator<Feature> iter = features.iterator();
    this.assertFeature("Preceding_0_2_1", "OOB1", iter.next());
    this.assertFeature("Preceding_0_2_0", "She", iter.next());
    this.assertFeature("LastCovered_0_1_0", "milk", iter.next());
    this.assertFeature("Following_0_3_0", ".", iter.next());
    this.assertFeature("Following_0_3_1", "OOB1", iter.next());
    this.assertFeature("Following_0_3_2", "OOB2", iter.next());

    features = extractor.extractWithin(this.jCas, boughMilk, sent2);
    Assert.assertEquals(6, features.size());
    iter = features.iterator();
    this.assertFeature("Preceding_0_2_1", "OOB2", iter.next());
    this.assertFeature("Preceding_0_2_0", "OOB1", iter.next());
    this.assertFeature("LastCovered_0_1_0", "OOB1", iter.next());
    this.assertFeature("Following_0_3_0", "OOB1", iter.next());
    this.assertFeature("Following_0_3_1", "He", iter.next());
    this.assertFeature("Following_0_3_2", "sold", iter.next());

    features = extractor.extractWithin(this.jCas, soldOranges, sent2);
    Assert.assertEquals(6, features.size());
    iter = features.iterator();
    this.assertFeature("Preceding_0_2_1", "OOB1", iter.next());
    this.assertFeature("Preceding_0_2_0", "He", iter.next());
    this.assertFeature("LastCovered_0_1_0", "oranges", iter.next());
    this.assertFeature("Following_0_3_0", ".", iter.next());
    this.assertFeature("Following_0_3_1", "OOB1", iter.next());
    this.assertFeature("Following_0_3_2", "OOB2", iter.next());
  }

  private void assertFeature(String expectedName, Object expectedValue, Feature actualFeature) {
    Assert.assertNotNull(actualFeature);
    Assert.assertEquals(expectedName, actualFeature.getName());
    Assert.assertEquals(expectedValue, actualFeature.getValue());
  }
}