/** 
 * Copyright (c) 2007-2008, Regents of the University of Colorado 
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
package org.cleartk.ml.feature.extractor;

import static org.junit.Assert.assertEquals;

import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.ml.Feature;
import org.cleartk.ml.feature.extractor.DirectedDistanceExtractor;
import org.cleartk.test.util.DefaultTestBase;
import org.cleartk.test.util.type.Token;
import org.junit.Test;
import org.apache.uima.fit.util.JCasUtil;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * @author Philip Ogren
 */

public class DirectedDistanceExtractorTest extends DefaultTestBase {

  @Test
  public void test1() throws Exception {
    tokenBuilder.buildTokens(
        jCas,
        "A simple sentence to test the distance of tokens from each other.",
        "A simple sentence to test the distance of tokens from each other .");

    DirectedDistanceExtractor<Token, Token> extractor = new DirectedDistanceExtractor<Token, Token>(
        null,
        Token.class);

    Token token1 = JCasUtil.selectByIndex(jCas, Token.class, 0);
    Token token2 = JCasUtil.selectByIndex(jCas, Token.class, 1);
    Token token3 = JCasUtil.selectByIndex(jCas, Token.class, 2);
    Token token4 = JCasUtil.selectByIndex(jCas, Token.class, 3);
    Token token5 = JCasUtil.selectByIndex(jCas, Token.class, 4);

    Feature feature = extractor.extract(jCas, token1, token2).get(0);
    assertEquals(1, feature.getValue());
    feature = extractor.extract(jCas, token2, token1).get(0);
    assertEquals(-1, feature.getValue());

    feature = extractor.extract(jCas, token1, token3).get(0);
    assertEquals(2, feature.getValue());
    feature = extractor.extract(jCas, token3, token1).get(0);
    assertEquals(-2, feature.getValue());

    feature = extractor.extract(jCas, token1, token5).get(0);
    assertEquals(4, feature.getValue());
    feature = extractor.extract(jCas, token5, token1).get(0);
    assertEquals(-4, feature.getValue());

    feature = extractor.extract(jCas, token4, token5).get(0);
    assertEquals(1, feature.getValue());
    feature = extractor.extract(jCas, token5, token4).get(0);
    assertEquals(-1, feature.getValue());

    feature = extractor.extract(jCas, token5, token5).get(0);
    assertEquals(0, feature.getValue());

    feature = extractor.extract(jCas, new Annotation(jCas, 0, 3), token1).get(0);
    assertEquals(0, feature.getValue());
    feature = extractor.extract(jCas, new Annotation(jCas, 0, 3), token2).get(0);
    assertEquals(0, feature.getValue());
    feature = extractor.extract(jCas, new Annotation(jCas, 0, 3), token3).get(0);
    assertEquals(1, feature.getValue());
    feature = extractor.extract(jCas, new Annotation(jCas, 0, 3), token4).get(0);
    assertEquals(2, feature.getValue());
    feature = extractor.extract(jCas, new Annotation(jCas, 0, 3), token5).get(0);
    assertEquals(3, feature.getValue());

    Annotation annotation = new Annotation(jCas, 64, 65);
    assertEquals(".", annotation.getCoveredText());
    feature = extractor.extract(jCas, annotation, token1).get(0);
    assertEquals(-12, feature.getValue());

  }
}
