/*
 * Copyright (c) 2007-2011, Regents of the University of Colorado 
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
package org.cleartk.ml.svmlight;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

import org.cleartk.ml.jar.ClassifierBuilder_ImplBase;
import org.cleartk.ml.jar.JarStreams;
import org.cleartk.ml.sigmoid.Sigmoid;
import org.cleartk.ml.svmlight.model.SvmLightModel;
import org.cleartk.ml.util.featurevector.FeatureVector;

/**
 * <br>
 * Copyright (c) 2007-2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public class SvmLightStringOutcomeClassifierBuilder extends
    ClassifierBuilder_ImplBase<SvmLightStringOutcomeClassifier, FeatureVector, String, Integer> {

  public File getTrainingDataFile(File dir) {
    return new File(dir, "training-data-allfalse.svmlight");
  }

  public File getTrainingDataFile(File dir, int label) {
    return new File(dir, String.format("training-data-%d.svmlight", label));
  }

  public void trainClassifier(File dir, String... args) throws Exception {
    SvmLightBooleanOutcomeClassifierBuilder builder = new SvmLightBooleanOutcomeClassifierBuilder();
    for (File file : dir.listFiles()) {
      if (file.getName().matches("training-data-\\d+.svmlight")) {
        builder.trainClassifier(dir, file, args);

        Sigmoid s = FitSigmoid.fit(new File(file.toString() + ".model"), file);

        ObjectOutput o = new ObjectOutputStream(new FileOutputStream(new File(file.toString()
            + ".sigmoid")));
        o.writeObject(s);
        o.close();
      }
    }
  }

  @Override
  protected void packageClassifier(File dir, JarOutputStream modelStream) throws IOException {
    super.packageClassifier(dir, modelStream);

    int label = 1;
    while (true) {
      File modelFile = new File(dir, String.format("training-data-%d.svmlight.model", label));
      File sigmoidFile = new File(dir, String.format("training-data-%d.svmlight.sigmoid", label));
      if (!modelFile.exists()) {
        break;
      }

      String modelName = String.format("model-%d.svmlight", label);
      String sigmoidName = String.format("model-%d.sigmoid", label);
      JarStreams.putNextJarEntry(modelStream, modelName, modelFile);
      JarStreams.putNextJarEntry(modelStream, sigmoidName, sigmoidFile);

      label += 1;
    }
  }

  private TreeMap<Integer, SvmLightModel> models;

  private TreeMap<Integer, Sigmoid> sigmoids;

  @Override
  protected void unpackageClassifier(JarInputStream modelStream) throws IOException {
    super.unpackageClassifier(modelStream);
    this.models = new TreeMap<Integer, SvmLightModel>();
    this.sigmoids = new TreeMap<Integer, Sigmoid>();

    int label = 1;
    SvmLightModel model;
    while ((model = getNextModel(modelStream, label)) != null) {
      this.models.put(label, model);

      JarStreams.getNextJarEntry(modelStream, String.format("model-%d.sigmoid", label));
      try {
        this.sigmoids.put(label, (Sigmoid) new ObjectInputStream(modelStream).readObject());
      } catch (ClassNotFoundException e) {
        throw new IOException(e);
      }

      label += 1;
    }

    if (this.models.isEmpty()) {
      throw new IOException(String.format("no models found in %s", modelStream));
    }
  }

  @Override
  protected SvmLightStringOutcomeClassifier newClassifier() {
    return new SvmLightStringOutcomeClassifier(
        this.featuresEncoder,
        this.outcomeEncoder,
        this.models,
        this.sigmoids);
  }

  private static SvmLightModel getNextModel(JarInputStream modelStream, int label)
      throws IOException {
    // look for a next entry or return null if there isn't one
    JarEntry entry = modelStream.getNextJarEntry();
    if (entry == null) {
      return null;
    }

    // make sure the name was the model we expected
    String expectedName = String.format("model-%d.svmlight", label);
    if (!entry.getName().equals(expectedName)) {
      throw new IOException(String.format(
          "expected next jar entry to be %s, found %s",
          expectedName,
          entry.getName()));
    }

    // read the model from the jar stream
    return SvmLightModel.fromInputStream(modelStream);
  }
}
