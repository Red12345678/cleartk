/*
 * Copyright (c) 2007-2013, Regents of the University of Colorado 
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
package org.cleartk.ml.libsvm.tk;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.cleartk.ml.CleartkProcessingException;
import org.cleartk.ml.tksvmlight.TreeFeatureVector;
import org.cleartk.ml.tksvmlight.TreeKernelSvmBooleanOutcomeClassifier;
import org.cleartk.ml.tksvmlight.TreeKernelSvmStringOutcomeDataWriter;
import org.cleartk.ml.tksvmlight.kernel.ArrayTreeKernel;
import org.cleartk.ml.tksvmlight.kernel.ComposableTreeKernel;

/**
 * A class that provided interfaces to train, package and unpackage a
 * {@link TreeKernelSvmBooleanOutcomeClassifier} into a jar file.
 * 
 * <br>
 * Copyright (c) 2013, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Tim Miller
 */

public class TkLibSvmStringOutcomeDataWriter extends
TreeKernelSvmStringOutcomeDataWriter<TkLibSvmStringOutcomeClassifierBuilder>{

  protected Map<String, ComposableTreeKernel> treeKernels;

  public TkLibSvmStringOutcomeDataWriter(File outputDirectory)
      throws FileNotFoundException {
    super(outputDirectory);
    treeKernels = new HashMap<>();
  }

  @Override
  protected TkLibSvmStringOutcomeClassifierBuilder newClassifierBuilder() {
    return new TkLibSvmStringOutcomeClassifierBuilder();
  }
  
  @Override
  protected void writeEncoded(TreeFeatureVector features, Integer outcome)
      throws CleartkProcessingException {
    super.writeEncoded(features, outcome);

    if(outcome != null){
      for(String tkKey : features.getTrees().keySet()){
        if(!treeKernels.containsKey(tkKey)){
          ComposableTreeKernel tk = features.getTrees().get(tkKey).getKernel();
          if(tk != null){
            treeKernels.put(tkKey, tk);
          }
        }
      }
    }
  }
  
  @Override
  public void finish() throws CleartkProcessingException {
    super.finish();
    if(treeKernels.size() > 0){
      ObjectOutputStream oos = null;
      ArrayTreeKernel atk = new ArrayTreeKernel(treeKernels);
      try{
        oos = new ObjectOutputStream(new FileOutputStream(new File(this.outputDirectory, "tree-kernel.obj")));
        oos.writeObject(atk);
        oos.close();
      } catch (IOException e) {
        e.printStackTrace();
        throw new CleartkProcessingException(e);
      }
    }
  }
}
