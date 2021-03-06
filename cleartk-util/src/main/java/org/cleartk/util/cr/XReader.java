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
package org.cleartk.util.cr;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import org.apache.uima.UimaContext;
import org.apache.uima.cas.impl.XCASDeserializer;
import org.apache.uima.cas.impl.XCASSerializer;
import org.apache.uima.cas.impl.XmiCasDeserializer;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.util.CleartkInitializationException;
import org.xml.sax.SAXException;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @see XmiCasSerializer
 * @see XCASSerializer
 */

public class XReader extends FilesCollectionReader {

  public static final String XMI = "XMI";

  public static final String XCAS = "XCAS";

  public static final String PARAM_XML_SCHEME = "xmlScheme";

  @ConfigurationParameter(
      name = PARAM_XML_SCHEME,
      mandatory = false,
      defaultValue = "XMI",
      description = "specifies the UIMA XML serialization scheme that should be used. Valid values for this parameter are 'XMI' and 'XCAS'")
  private String xmlScheme;

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);

    if (!xmlScheme.equals(XMI) && !xmlScheme.equals(XCAS))
      throw CleartkInitializationException.invalidParameterValueSelectFrom(
          PARAM_XML_SCHEME,
          Arrays.asList(XMI, XCAS),
          xmlScheme);
  }

  public void getNext(JCas jCas) throws IOException, CollectionException {
    if (!hasNext()) {
      throw new RuntimeException("getNext(jCas) was called but hasNext() returns false");
    }

    FileInputStream inputStream = new FileInputStream(currentFile);

    try {
      if (xmlScheme.equals(XMI))
        XmiCasDeserializer.deserialize(inputStream, jCas.getCas());
      else
        XCASDeserializer.deserialize(inputStream, jCas.getCas());
    } catch (SAXException e) {
      throw new CollectionException(e);
    } finally {
      inputStream.close();
    }

    completed++;
    currentFile = null;
  }

}
