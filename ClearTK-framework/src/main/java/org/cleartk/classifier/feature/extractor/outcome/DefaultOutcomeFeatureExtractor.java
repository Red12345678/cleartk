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

package org.cleartk.classifier.feature.extractor.outcome;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.WindowFeature;
import org.cleartk.classifier.feature.WindowNGramFeature;
import org.cleartk.test.util.ConfigurationParameterNameFactory;
import org.uutuc.descriptor.ConfigurationParameter;
import org.uutuc.util.InitializeUtil;

/**
 * <br>
 * Copyright (c) 2009, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * @author Philip Ogren
 */

public class DefaultOutcomeFeatureExtractor implements OutcomeFeatureExtractor {

	private static final long serialVersionUID = 7476684786572310025L;

	public static final String PARAM_MOST_RECENT_OUTCOME = ConfigurationParameterNameFactory
			.createConfigurationParameterName(DefaultOutcomeFeatureExtractor.class, "mostRecentOutcome");

	@ConfigurationParameter(description = "indicates the position of the first (most recent) outcome to include. For example, the default value of 1 means that if the outcomes produced so far by the classifier were [A, B, C, D], then the first outcome to be used as a feature would be D since it is the most recent.", defaultValue = "1")
	private int mostRecentOutcome = 1;

	public static final String PARAM_LEAST_RECENT_OUTCOME = ConfigurationParameterNameFactory
			.createConfigurationParameterName(DefaultOutcomeFeatureExtractor.class, "leastRecentOutcome");

	@ConfigurationParameter(description = "indicates the position of the last (least recent) outcome to include. For example, the default value of 3 means that if the outcomes produced so far by the classifier were [A, B, C, D], then the last outcome to be used as a feature would be B since and is considered the least recent.", defaultValue = "3")
	private int leastRecentOutcome = 3;

	public static final String PARAM_USE_BIGRAM = ConfigurationParameterNameFactory.createConfigurationParameterName(
			DefaultOutcomeFeatureExtractor.class, "useBigram");

	@ConfigurationParameter(description = "when true indicates that bigrams of outcomes should be included as features", defaultValue = "true")
	private boolean useBigram = true;

	public static final String PARAM_USE_TRIGRAM = ConfigurationParameterNameFactory.createConfigurationParameterName(
			DefaultOutcomeFeatureExtractor.class, "useTrigram");

	@ConfigurationParameter(defaultValue = "true", description = "indicates that trigrams of outcomes should be included as features")
	private boolean useTrigram = true;

	public static final String PARAM_USE4GRAM =  ConfigurationParameterNameFactory.createConfigurationParameterName(
			DefaultOutcomeFeatureExtractor.class, "use4gram");

	@ConfigurationParameter(defaultValue = "false", description = "indicates that 4-grams of outcomes should be included as features")
	private boolean use4gram = false;

	public void initialize(UimaContext context) throws ResourceInitializationException {
		InitializeUtil.initialize(this, context);

		if (mostRecentOutcome < 1) {
			throw new ResourceInitializationException(new IllegalArgumentException(String.format(
					"the parameter '%1$s' must be greater than 1.", PARAM_MOST_RECENT_OUTCOME)));
		}

		if (leastRecentOutcome < mostRecentOutcome) {
			throw new ResourceInitializationException(new IllegalArgumentException(String.format(
					"the parameter '%1$s' must be greater than the value for parameter '%2$s'.",
					PARAM_LEAST_RECENT_OUTCOME, PARAM_MOST_RECENT_OUTCOME)));
		}

	}

	public List<Feature> extractFeatures(List<Object> previousOutcomes) {
		if (previousOutcomes == null || previousOutcomes.size() == 0) {
			return Collections.emptyList();
		}

		List<Feature> features = new ArrayList<Feature>();

		for (int i = mostRecentOutcome; i <= leastRecentOutcome; i++) {
			int index = previousOutcomes.size() - i;
			if (index >= 0) {
				Feature feature = new WindowFeature("PreviousOutcome", previousOutcomes.get(index),
						WindowFeature.ORIENTATION_LEFT, i, (Feature) null);
				features.add(feature);
			}
		}

		if (useBigram && previousOutcomes.size() >= 2) {
			int size = previousOutcomes.size();
			String featureValue = previousOutcomes.get(size - 1).toString() + "_" + previousOutcomes.get(size - 2);
			Feature feature = new WindowNGramFeature("PreviousOutcomes", featureValue,
					WindowNGramFeature.ORIENTATION_LEFT, WindowNGramFeature.DIRECTION_LEFT_TO_RIGHT, "_", 2, 1,
					(List<Feature>) null);
			features.add(feature);

		}

		if (useTrigram && previousOutcomes.size() >= 3) {
			int size = previousOutcomes.size();
			String featureValue = previousOutcomes.get(size - 1).toString() + "_" + previousOutcomes.get(size - 2)
					+ "_" + previousOutcomes.get(size - 3);
			Feature feature = new WindowNGramFeature("PreviousOutcomes", featureValue,
					WindowNGramFeature.ORIENTATION_LEFT, WindowNGramFeature.DIRECTION_LEFT_TO_RIGHT, "_", 3, 1,
					(List<Feature>) null);
			features.add(feature);

		}

		if (use4gram && previousOutcomes.size() >= 4) {
			int size = previousOutcomes.size();
			String featureValue = previousOutcomes.get(size - 1).toString() + "_" + previousOutcomes.get(size - 2)
					+ "_" + previousOutcomes.get(size - 3) + "_" + previousOutcomes.get(size - 4);
			Feature feature = new WindowNGramFeature("PreviousOutcomes", featureValue,
					WindowNGramFeature.ORIENTATION_LEFT, WindowNGramFeature.DIRECTION_LEFT_TO_RIGHT, "_", 4, 1,
					(List<Feature>) null);
			features.add(feature);
		}

		return features;
	}

}
