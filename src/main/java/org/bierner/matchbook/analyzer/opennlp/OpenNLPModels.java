/*
 * Copyright (c) 2014, Gann Bierner
 */

package org.bierner.matchbook.analyzer.opennlp;

import java.io.InputStream;
import java.util.Locale;
import lombok.AllArgsConstructor;
import lombok.Setter;

/**
 * A common utility class for OpenNLP wrappers to access their models.  By default, it is assumed
 * that the models are all located in the opennlp.models package, but any strategy may be used for
 * loading them.  The strategy is set statically by the 
 * {@link #setLoader(org.bierner.matchbook.analyzer.opennlp.OpenNLPModels.LoadStrategy)}
 * method.
 * 
 * @author gann
 */
public class OpenNLPModels {
    /**
     * A strategy for loading an OpenNLP model given its name.  The wrappers in this package assume that
     * the name is the same as on the OpenNLP model download page.
     * @see <a href="http://opennlp.sourceforge.net/models-1.5">OpenNLP Models</a>
     */
    public interface LoadStrategy {
        InputStream load(String modelName);
    }
    
    /**
     * A model loading strategy that loads as a resource from a given base path.
     */
    @AllArgsConstructor
    public static class ResourceLoadStrategy implements LoadStrategy {
        private String basePath;
        @Override
        public InputStream load(String modelName) {
            return ClassLoader.getSystemResourceAsStream(basePath + "/" + modelName);
        }
    }
    
    /**
     * A strategy for identifying and loading OpenNLP models.  The default loader attempts to locate
     * models as a resource from opennlp/models.
     * 
     * @param loader 
     */
    @Setter private static LoadStrategy loader = new ResourceLoadStrategy("opennlp/models");
    
    /**
     * Loads the requested model for the requested locale.  This method assumes the current 
     * OpenNLP model naming convention where all models start with a language code.
     * 
     * @param clazz The class of the model requester.  This is only used to provide a clearer error 
     * message if the model does not exist
     * @param locale The Locale for the requested model.
     * @param modelSuffix The non-Locale part of the model name.
     * @return the model as an InputStream 
     * @throws IllegalArgumentException If the model is not available for the requested locale
     */
    public static InputStream getModel(Class<?> clazz, Locale locale, String modelSuffix) {
        String modelFile = locale.getLanguage() + modelSuffix;
        InputStream is = loader.load(modelFile);
        if (is == null)
            throw new IllegalArgumentException(clazz.getSimpleName() + " model not available for " + locale);
        return is;
    }
}
