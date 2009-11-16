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
package org.cleartk.util.linewriter;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.regex.Pattern;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.jcas.tcas.DocumentAnnotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.FileUtils;
import org.cleartk.CleartkComponents;
import org.cleartk.classifier.SequentialClassifierAnnotator;
import org.cleartk.type.Sentence;
import org.cleartk.type.Token;
import org.cleartk.util.AnnotationRetrieval;
import org.cleartk.util.ViewURIUtil;
import org.cleartk.util.linewriter.annotation.CoveredTextAnnotationWriter;
import org.cleartk.util.linewriter.block.BlankLineBlockWriter;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.uutuc.factory.AnalysisEngineFactory;
import org.uutuc.factory.JCasFactory;
import org.uutuc.factory.TokenFactory;
import org.uutuc.factory.TypeSystemDescriptionFactory;
import org.uutuc.util.TearDownUtil;


/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

*/

public class LineWriterTest {

	private final File outputDir = new File("test/data/linewriter");

	static String newline = System.getProperty("line.separator");
	
	@After
	public void tearDown() throws Exception {
		TearDownUtil.removeDirectory(outputDir);
	}

	
	@Test
	public void test1() throws Exception {
		AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(
					LineWriter.class,
					TypeSystemDescriptionFactory.createTypeSystemDescription("org.cleartk.TypeSystem"),
					LineWriter.PARAM_OUTPUT_DIRECTORY_NAME, this.outputDir.getPath(), 
					LineWriter.PARAM_OUTPUT_ANNOTATION_CLASS_NAME, "org.cleartk.type.Token",
					LineWriter.PARAM_BLOCK_ANNOTATION_CLASS_NAME, "org.cleartk.type.Sentence",
					LineWriter.PARAM_ANNOTATION_WRITER_CLASS_NAME, "org.cleartk.util.linewriter.annotation.CoveredTextAnnotationWriter",
					LineWriter.PARAM_FILE_SUFFIX, ".txt");
		
		JCas jCas = engine.newJCas();
		String text = "What if we built a rocket ship made of cheese?"+newline +
					  "We could fly it to the moon for repairs.";
		TokenFactory.createTokens(jCas, text, Token.class, Sentence.class,
				"What if we built a rocket ship made of cheese ?\nWe could fly it to the moon for repairs .",
				"A B C D E F G H I J K L M N O P Q R S T U", null, "org.cleartk.type.Token:pos", null);
		ViewURIUtil.setURI(jCas, "1234");
		engine.process(jCas);
		engine.collectionProcessComplete();
		
		String expectedText = newline+"What"+newline+
		"if"+newline+ 
		"we"+newline+
		"built"+newline+
		"a"+newline+
		"rocket"+newline+
		"ship"+newline+
		"made"+newline+
		"of"+newline+
		"cheese"+newline+
		"?"+newline+
		""+newline+
		"We"+newline+
		"could"+newline+
		"fly"+newline+
		"it"+newline+
		"to"+newline+
		"the"+newline+
		"moon"+newline+
		"for"+newline+
		"repairs"+newline+
		"."+newline;
		
		File outputFile = new File(this.outputDir, "1234.txt");
		assertTrue(outputFile.exists());
		String actualText = FileUtils.file2String(outputFile);
		Assert.assertEquals(expectedText, actualText);
		
		jCas = engine.newJCas();
		TokenFactory.createTokens(jCas, text, Token.class, Sentence.class,
				"What if we \n built a rocket \n ship made of cheese ?\nWe could fly it \nto the moon for repairs .",
				"A B C D E F G H I J K L M N O P Q R S T U", null, "org.cleartk.type.Token:pos", null);
		ViewURIUtil.setURI(jCas, "1234");
		engine.process(jCas);
		engine.collectionProcessComplete();

		expectedText = newline+"What"+newline+
		"if"+newline+
		"we"+newline+
		""+newline+
		"built"+newline+
		"a"+newline+
		"rocket"+newline+
		""+newline+
		"ship"+newline+
		"made"+newline+
		"of"+newline+
		"cheese"+newline+
		"?"+newline+
		""+newline+
		"We"+newline+
		"could"+newline+
		"fly"+newline+
		"it"+newline+
		""+newline+
		"to"+newline+
		"the"+newline+
		"moon"+newline+
		"for"+newline+
		"repairs"+newline+
		"."+newline;
		
		outputFile = new File(this.outputDir, "1234.txt");
		assertTrue(outputFile.exists());
		actualText = FileUtils.file2String(outputFile);
		Assert.assertEquals(expectedText, actualText);
	}

	
	@Test
	public void test2() throws Exception {
		AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(
					LineWriter.class,
					TypeSystemDescriptionFactory.createTypeSystemDescription("org.cleartk.TypeSystem"),
					LineWriter.PARAM_OUTPUT_FILE_NAME, new File(outputDir, "output.txt").getPath(), 
					LineWriter.PARAM_OUTPUT_ANNOTATION_CLASS_NAME, "org.cleartk.type.Token",
					LineWriter.PARAM_ANNOTATION_WRITER_CLASS_NAME, ExampleTokenWriter.class.getName());
		
		JCas jCas = engine.newJCas();
		String text = "Me and all my friends are non-conformists.  I will subjugate my freedom oppressor.";
		TokenFactory.createTokens(jCas, text, Token.class, Sentence.class,
				"Me and all my friends are non-conformists . \n I will subjugate my freedom oppressor . ",
				"1 2 3 4 5 6 7 8 9 10 11 12 13 14 15", null, "org.cleartk.type.Token:pos", null);
		engine.process(jCas);
		engine.collectionProcessComplete();
		
		String expectedText =  
			"Me\t1"+newline +
			"and\t2"+newline +
			"all\t3"+newline +
			"my\t4"+newline +
			"friends\t5"+newline +
			"are\t6"+newline +
			"non-conformists\t7"+newline +
			".\t8"+newline +
			"I\t9"+newline +
			"will\t10"+newline +
			"subjugate\t11"+newline +
			"my\t12"+newline +
			"freedom\t13"+newline +
			"oppressor\t14"+newline +
			".\t15"+newline;
		
		File outputFile = new File(this.outputDir, "output.txt");
		assertTrue(outputFile.exists());
		String actualText = FileUtils.file2String(outputFile);
		Assert.assertEquals(expectedText, actualText);
	}

	@Test
	public void test3() throws Exception {
		AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(
					LineWriter.class,
					TypeSystemDescriptionFactory.createTypeSystemDescription("org.cleartk.TypeSystem"),
					LineWriter.PARAM_OUTPUT_FILE_NAME, new File(outputDir, "output.txt").getPath(), 
					LineWriter.PARAM_OUTPUT_ANNOTATION_CLASS_NAME, "org.cleartk.type.Sentence",
					LineWriter.PARAM_BLOCK_ANNOTATION_CLASS_NAME, "org.apache.uima.jcas.tcas.DocumentAnnotation",
					LineWriter.PARAM_BLOCK_WRITER_CLASS_NAME, "org.cleartk.util.linewriter.block.DoNothingBlockWriter",
					LineWriter.PARAM_ANNOTATION_WRITER_CLASS_NAME, "org.cleartk.util.linewriter.annotation.CoveredTextAnnotationWriter");

		
		JCas jCas = engine.newJCas();
		String text = "If you like line writer, then you should really check out line rider.";
		TokenFactory.createTokens(jCas, text, Token.class, Sentence.class);
		engine.process(jCas);
		Token token = AnnotationRetrieval.get(jCas, Token.class, 0);
		Assert.assertEquals("If", token.getCoveredText());

		jCas = engine.newJCas();
		text = "I highly recommend reading 'Three Cups of Tea' by Greg Mortenson.\n Swashbuckling action and inspirational story.";
		TokenFactory.createTokens(jCas, text, Token.class, Sentence.class);
		engine.process(jCas);

		DocumentAnnotation da = AnnotationRetrieval.get(jCas, DocumentAnnotation.class, 0);
		assertNotNull(da);
		

		engine.collectionProcessComplete();
		
		String expectedText = 
			"If you like line writer, then you should really check out line rider."+newline +
			"I highly recommend reading 'Three Cups of Tea' by Greg Mortenson."+newline+
			"Swashbuckling action and inspirational story."+newline;
		
		File outputFile = new File(this.outputDir, "output.txt");
		assertTrue(outputFile.exists());
		String actualText = FileUtils.file2String(outputFile); 
		Assert.assertEquals(expectedText, actualText);
	}

	
	@Test
	public void testDescriptor() throws Exception {
		ResourceInitializationException rie = getResourceInitializationException();
		assertNotNull(rie);

		rie = getResourceInitializationException(LineWriter.PARAM_OUTPUT_ANNOTATION_CLASS_NAME, "org.cleartk.type.Token",
												 LineWriter.PARAM_OUTPUT_DIRECTORY_NAME, "test/data/linewriter", 
					 							 LineWriter.PARAM_OUTPUT_FILE_NAME, "test/data/linewriter/linewriter.txt");
		assertNotNull(rie);

		rie = getResourceInitializationException(LineWriter.PARAM_OUTPUT_DIRECTORY_NAME, "test/data/linewriter", 
				LineWriter.PARAM_OUTPUT_ANNOTATION_CLASS_NAME, "edu.cleartk.");
		assertNotNull(rie);

		rie = getResourceInitializationException(LineWriter.PARAM_OUTPUT_DIRECTORY_NAME, "test/data/linewriter", 
				LineWriter.PARAM_OUTPUT_ANNOTATION_CLASS_NAME, "org.cleartk.type.Token");
		assertNull(rie);

		rie = getResourceInitializationException(LineWriter.PARAM_OUTPUT_DIRECTORY_NAME, "test/data/linewriter", 
				LineWriter.PARAM_OUTPUT_ANNOTATION_CLASS_NAME, "org.cleartk.type.Token",
				LineWriter.PARAM_ANNOTATION_WRITER_CLASS_NAME, "org.cleartk.type.Token");
		assertNotNull(rie);

		rie = getResourceInitializationException(LineWriter.PARAM_OUTPUT_DIRECTORY_NAME, "test/data/linewriter", 
				LineWriter.PARAM_OUTPUT_ANNOTATION_CLASS_NAME, "org.cleartk.type.Token",
				LineWriter.PARAM_ANNOTATION_WRITER_CLASS_NAME, "org.cleartk.util.linewriter.annotation.CoveredTextAnnotationWriter");
		assertNull(rie);

		rie = getResourceInitializationException(LineWriter.PARAM_OUTPUT_FILE_NAME, "test/data/linewriter/linewriter.txt", 
				LineWriter.PARAM_OUTPUT_ANNOTATION_CLASS_NAME, "org.cleartk.type.Token",
				LineWriter.PARAM_ANNOTATION_WRITER_CLASS_NAME, "org.cleartk.util.linewriter.annotation.CoveredTextAnnotationWriter");
		assertNull(rie);

		rie = getResourceInitializationException(LineWriter.PARAM_OUTPUT_FILE_NAME, "test/data/linewriter/linewriter.txt", 
				LineWriter.PARAM_OUTPUT_ANNOTATION_CLASS_NAME, "org.cleartk.type.Token",
				LineWriter.PARAM_BLOCK_ANNOTATION_CLASS_NAME, "org.cleartk.type.Sentence",
				LineWriter.PARAM_ANNOTATION_WRITER_CLASS_NAME, "org.cleartk.util.linewriter.annotation.CoveredTextAnnotationWriter",
				LineWriter.PARAM_FILE_SUFFIX, ".txt");
		assertNull(rie);

		rie = getResourceInitializationException(LineWriter.PARAM_OUTPUT_FILE_NAME, "test/data/linewriter/linewriter.txt", 
				LineWriter.PARAM_OUTPUT_ANNOTATION_CLASS_NAME, "org.cleartk.type.Token",
				LineWriter.PARAM_BLOCK_ANNOTATION_CLASS_NAME, "org.cleartk.type.Sentence",
				LineWriter.PARAM_ANNOTATION_WRITER_CLASS_NAME, "org.cleartk.util.linewriter.annotation.CoveredTextAnnotationWriter",
				LineWriter.PARAM_FILE_SUFFIX, ".txt");
		assertNull(rie);

		rie = getResourceInitializationException( 
				LineWriter.PARAM_OUTPUT_DIRECTORY_NAME, "test/data/linewriter",
				LineWriter.PARAM_OUTPUT_ANNOTATION_CLASS_NAME, "org.cleartk.type.Token",
				LineWriter.PARAM_BLOCK_ANNOTATION_CLASS_NAME, "org.cleartk.type.Sentence",
				LineWriter.PARAM_ANNOTATION_WRITER_CLASS_NAME, "org.cleartk.util.linewriter.annotation.CoveredTextAnnotationWriter",
				LineWriter.PARAM_FILE_SUFFIX, ".txt");
		assertNull(rie);

		rie = getResourceInitializationException( 
				LineWriter.PARAM_OUTPUT_DIRECTORY_NAME, "test/data/linewriter",
				LineWriter.PARAM_OUTPUT_ANNOTATION_CLASS_NAME, "org.apache.uima.jcas.tcas.Annotation",
				LineWriter.PARAM_ANNOTATION_WRITER_CLASS_NAME, "org.cleartk.util.linewriter.annotation.CoveredTextAnnotationWriter",
				LineWriter.PARAM_FILE_SUFFIX, ".txt");
		assertNull(rie);

		
		rie = getResourceInitializationException(LineWriter.PARAM_OUTPUT_FILE_NAME, "test/data/linewriter/linewriter.txt", 
				LineWriter.PARAM_OUTPUT_ANNOTATION_CLASS_NAME, "org.cleartk.type.Token",
				LineWriter.PARAM_BLOCK_ANNOTATION_CLASS_NAME, "java.lang.String",
				LineWriter.PARAM_ANNOTATION_WRITER_CLASS_NAME, "org.cleartk.util.linewriter.annotation.CoveredTextAnnotationWriter",
				LineWriter.PARAM_FILE_SUFFIX, ".txt");
		assertNotNull(rie);

		rie = getResourceInitializationException(LineWriter.PARAM_OUTPUT_DIRECTORY_NAME, "test/data/linewriter", 
				LineWriter.PARAM_OUTPUT_ANNOTATION_CLASS_NAME, "org.cleartk.type.Token",
				LineWriter.PARAM_BLOCK_ANNOTATION_CLASS_NAME, "org.cleartk.type.Sentence",
				LineWriter.PARAM_ANNOTATION_WRITER_CLASS_NAME, "org.cleartk.chunk.DefaultChunkLabeler",
				LineWriter.PARAM_FILE_SUFFIX, ".txt");
		assertNotNull(rie);

		rie = getResourceInitializationException(LineWriter.PARAM_OUTPUT_DIRECTORY_NAME, "test/data/linewriter", 
				LineWriter.PARAM_OUTPUT_ANNOTATION_CLASS_NAME, "org.cleartk.type.Token",
				LineWriter.PARAM_BLOCK_ANNOTATION_CLASS_NAME, "org.cleartk.type.Sentence",
				LineWriter.PARAM_BLOCK_WRITER_CLASS_NAME, "org.cleartk.util.linewriter.SillyBlockWriter",
				LineWriter.PARAM_FILE_SUFFIX, ".txt");
		assertNotNull(rie);

		rie = getResourceInitializationException(LineWriter.PARAM_OUTPUT_DIRECTORY_NAME, "test/data/linewriter", 
				LineWriter.PARAM_OUTPUT_ANNOTATION_CLASS_NAME, "org.cleartk.type.Token",
				LineWriter.PARAM_BLOCK_ANNOTATION_CLASS_NAME, "org.cleartk.type.Sentence",
				LineWriter.PARAM_BLOCK_WRITER_CLASS_NAME, "org.cleartk.util.linewriter.block.BlankLineBlockWriter",
				LineWriter.PARAM_FILE_SUFFIX, ".txt");
		assertNull(rie);

		rie = getResourceInitializationException(LineWriter.PARAM_OUTPUT_DIRECTORY_NAME, "test/data/linewriter", 
				LineWriter.PARAM_OUTPUT_ANNOTATION_CLASS_NAME, "org.cleartk.type.Sentence",
				LineWriter.PARAM_ANNOTATION_WRITER_CLASS_NAME, ExampleTokenWriter.class.getName());
		assertNotNull(rie);
	}

	private ResourceInitializationException getResourceInitializationException(Object... params) throws Exception{
		ResourceInitializationException rie = null;
		try {
			AnalysisEngineFactory.createAnalysisEngine("org.cleartk.util.linewriter.LineWriter", params);
		}
		catch (ResourceInitializationException e) {
			rie = e;
		}
		return rie;

	}
	
	@Test
	public void miscGenericTests() {
		assertTrue(Annotation.class.isAssignableFrom(org.cleartk.type.Token.class));
		assertTrue(Annotation.class.isAssignableFrom(DocumentAnnotation.class));
	}
	
	@Test
	public void test4() throws Exception {
		AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(
					LineWriter.class,
					TypeSystemDescriptionFactory.createTypeSystemDescription("org.cleartk.TypeSystem"),
					LineWriter.PARAM_OUTPUT_FILE_NAME, new File(outputDir, "output.txt").getPath(), 
					LineWriter.PARAM_OUTPUT_ANNOTATION_CLASS_NAME, "org.cleartk.type.Token",
					LineWriter.PARAM_ANNOTATION_WRITER_CLASS_NAME, "org.cleartk.util.linewriter.annotation.TokenPOSWriter",
					LineWriter.PARAM_BLOCK_ANNOTATION_CLASS_NAME, "org.cleartk.type.Sentence",
					LineWriter.PARAM_BLOCK_WRITER_CLASS_NAME, "org.cleartk.util.linewriter.block.BlankLineBlockWriter");
		
		JCas jCas = engine.newJCas();
		String text = "Me and all my friends are non-conformists.  I will subjugate my freedom oppressor.";
		TokenFactory.createTokens(jCas, text, Token.class, Sentence.class,
				"Me and all my friends are non-conformists . \n I will subjugate my freedom oppressor . ",
				"1 2 3 4 5 6 7 8 9 10 11 12 13 14 15", null, "org.cleartk.type.Token:pos", null);
		engine.process(jCas);
		engine.collectionProcessComplete();
		
		String expectedText = newline +  
			"Me\t1"+newline +
			"and\t2"+newline +
			"all\t3"+newline +
			"my\t4"+newline +
			"friends\t5"+newline +
			"are\t6"+newline +
			"non-conformists\t7"+newline +
			".\t8"+newline +
			newline+
			"I\t9"+newline +
			"will\t10"+newline +
			"subjugate\t11"+newline +
			"my\t12"+newline +
			"freedom\t13"+newline +
			"oppressor\t14"+newline +
			".\t15"+newline;
			
		
		File outputFile = new File(this.outputDir, "output.txt");
		assertTrue(outputFile.exists());
		String actualText = FileUtils.file2String(outputFile);
		Assert.assertEquals(expectedText, actualText);
	}

	@Test
	public void test5() throws Exception {
		AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(
					LineWriter.class,
					TypeSystemDescriptionFactory.createTypeSystemDescription("org.cleartk.TypeSystem"),
					LineWriter.PARAM_OUTPUT_DIRECTORY_NAME, outputDir.getPath(), 
					LineWriter.PARAM_OUTPUT_ANNOTATION_CLASS_NAME, "org.cleartk.type.Token",
					LineWriter.PARAM_FILE_SUFFIX, "txt",
					LineWriter.PARAM_ANNOTATION_WRITER_CLASS_NAME, "org.cleartk.util.linewriter.annotation.TokenPOSWriter",
					LineWriter.PARAM_BLOCK_ANNOTATION_CLASS_NAME, "org.apache.uima.jcas.tcas.DocumentAnnotation",
					LineWriter.PARAM_BLOCK_WRITER_CLASS_NAME, "org.cleartk.util.linewriter.block.DocumentIdBlockWriter");
		
		JCas jCas = engine.newJCas();
		String text = "Me and all my friends are non-conformists.  I will subjugate my freedom oppressor.";
		TokenFactory.createTokens(jCas, text, Token.class, Sentence.class,
				"Me and all my friends are non-conformists . \n I will subjugate my freedom oppressor . ",
				"1 2 3 4 5 6 7 8 9 10 11 12 13 14 15", null, "org.cleartk.type.Token:pos", null);
		ViewURIUtil.setURI(jCas, "1234");
		engine.process(jCas);
		engine.collectionProcessComplete();
		
		String expectedText =
			"1234"+newline + 
			"Me\t1"+newline +
			"and\t2"+newline +
			"all\t3"+newline +
			"my\t4"+newline +
			"friends\t5"+newline +
			"are\t6"+newline +
			"non-conformists\t7"+newline +
			".\t8"+newline +
			"I\t9"+newline +
			"will\t10"+newline +
			"subjugate\t11"+newline +
			"my\t12"+newline +
			"freedom\t13"+newline +
			"oppressor\t14"+newline +
			".\t15"+newline;
			
		
		File outputFile = new File(this.outputDir, "1234.txt");
		assertTrue(outputFile.exists());
		String actualText = FileUtils.file2String(outputFile);
		Assert.assertEquals(expectedText, actualText);
	}

	@Test
	public void testLineWriterAfterChunkTokenizer() throws Exception {
		AnalysisEngine[] engines = new AnalysisEngine[]{
				AnalysisEngineFactory.createAnalysisEngine("org.cleartk.sentence.opennlp.OpenNLPSentenceSegmenter"),
				AnalysisEngineFactory.createAnalysisEngine("org.cleartk.token.Subtokenizer"),
				AnalysisEngineFactory.createAnalysisEngine("org.cleartk.token.chunk.ChunkTokenizer",
						SequentialClassifierAnnotator.PARAM_CLASSIFIER_JAR_PATH, "test/data/token/chunk/model.jar"),
				AnalysisEngineFactory.createAnalysisEngine("org.cleartk.util.linewriter.LineWriter",
						LineWriter.PARAM_OUTPUT_DIRECTORY_NAME, this.outputDir.getPath(), 
						LineWriter.PARAM_OUTPUT_ANNOTATION_CLASS_NAME, "org.cleartk.type.Token")};
		JCas jCas = JCasFactory.createJCas("org.cleartk.TypeSystem");
		jCas.setDocumentText(
				"Philip Ogren didn't write this sentence.\n" +
				"ROIs are required for CD28-mediated activation of the NF-kappa B/CD28-responsive complex.");
		ViewURIUtil.setURI(jCas, "id");
		for (AnalysisEngine engine: engines) {
			engine.process(jCas);
		}
		for (AnalysisEngine engine: engines) {
			engine.collectionProcessComplete();
		}
		
		// make sure there were two sentences
		int sentCount = AnnotationRetrieval.getAnnotations(jCas, Sentence.class).size();
		Assert.assertEquals(2, sentCount);
		
		// make sure there no extra blank lines
		String text = FileUtils.file2String(new File(this.outputDir, "id"));
		boolean hasDoubleNewlines = Pattern.compile("\n\\s*\n").matcher(text).find();
		Assert.assertFalse(hasDoubleNewlines);
	}
	
	@Test
	public void testTokenWriter() throws Exception {
		AnalysisEngine engine = CleartkComponents.createPrimitive(LineWriter.class, 
				LineWriter.PARAM_OUTPUT_DIRECTORY_NAME, this.outputDir.getPath(),
				LineWriter.PARAM_BLOCK_ANNOTATION_CLASS_NAME, Sentence.class.getName(),
				LineWriter.PARAM_OUTPUT_ANNOTATION_CLASS_NAME, Token.class.getName(),
				LineWriter.PARAM_FILE_SUFFIX, "txt",
				LineWriter.PARAM_ANNOTATION_WRITER_CLASS_NAME, CoveredTextAnnotationWriter.class.getName(),
				LineWriter.PARAM_BLOCK_WRITER_CLASS_NAME, BlankLineBlockWriter.class.getName());
		
		JCas jCas = engine.newJCas();
		String spacedTokens = "What if we built a large , wooden badger ?\nHmm? ";
		TokenFactory.createTokens(jCas,
				"What if we built\na large, wooden badger? Hmm?",
				Token.class, Sentence.class, 
				spacedTokens);
		ViewURIUtil.setURI(jCas, "identifier");
		engine.process(jCas);
		engine.collectionProcessComplete();
		
		String expected = "\n"+spacedTokens.replace("\n", "\n\n").replace(' ', '\n');
		File outputFile = new File(this.outputDir, "identifier.txt");
		String actual = FileUtils.file2String(outputFile).replace("\r", "");
		Assert.assertEquals(expected, actual);
	}

}
