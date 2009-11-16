/** 
 * Copyright (c) 2009, Regents of the University of Colorado 
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
package org.cleartk.classifier;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Logger;
import org.cleartk.CleartkException;
import org.cleartk.test.util.ConfigurationParameterNameFactory;
import org.cleartk.util.ReflectionUtil;
import org.cleartk.util.UIMAUtil;
import org.uutuc.descriptor.ConfigurationParameter;
import org.uutuc.util.InitializeUtil;

/**
 * <br>
 * Copyright (c) 2009, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * <p>
 * 
 * @author Steven Bethard
 * @author Philip Ogren
 */
public abstract class InstanceConsumer_ImplBase<OUTCOME_TYPE> extends JCasAnnotator_ImplBase implements InstanceConsumer<OUTCOME_TYPE>{

	/**
	 * "org.cleartk.classifier.InstanceConsumer.PARAM_ANNOTATION_HANDLER"
	 * is a single, required, string parameter that provides the full name of the AnnotationHandler class that will be used with this InstanceConsumer.
	 */
	public static final String PARAM_ANNOTATION_HANDLER_NAME = ConfigurationParameterNameFactory.createConfigurationParameterName(
			InstanceConsumer_ImplBase.class, "annotationHandlerName");
	@ConfigurationParameter(
			mandatory = true,
			description = "provides the full name of the AnnotationHandler class that will be used with this InstanceConsumer")
	private String annotationHandlerName;
	
	protected Logger logger;
	protected AnnotationHandler<OUTCOME_TYPE> annotationHandler;
	
	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		InitializeUtil.initialize(this, context);
		this.annotationHandler = ReflectionUtil.uncheckedCast(UIMAUtil.create(annotationHandlerName, AnnotationHandler.class, context));
		this.logger = context.getLogger();
	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		try {
			this.annotationHandler.process(jCas, this);
		} catch (CleartkException ctke) {
			throw new AnalysisEngineProcessException(ctke);
		}
	}
	
	protected <T> void checkOutcomeType(Class<T> cls, String parameterName, T object)
	throws ResourceInitializationException {
		UIMAUtil.checkTypeParameterIsAssignable(
				AnnotationHandler.class, "OUTCOME_TYPE", this.annotationHandler,
				cls, parameterName, object);
	}

}
