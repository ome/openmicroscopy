/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.formats.importer;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import ome.formats.importer.gui.ErrorHandler;
import ome.formats.importer.util.ErrorContainer;
import omero.model.Pixels;

/**
 * Utility class which configures the Import.
 * 
 * @since Beta4.1
 */
public interface ImportReport {

    public interface Callback {
        String email();
        boolean sendErrors();
        boolean uploadErrors();
        boolean sendSuccesses();
        boolean uploadSuccesses();
    }

    /**
     * Default callback logic which anonymously uploads only the stack traces
     * of exceptions, but not files.
     */
    public final static Callback DEFAULT = new Callback(){
        public String email() {
            return null;
        }
        public boolean sendErrors() {
            return true;
        }
        public boolean sendSuccesses() {
            return true;
        }
        public boolean uploadErrors() {
            return false;
        }
        public boolean uploadSuccesses() {
            return false;
        }};
    
    public void success(String path, List<Pixels> pix);
    
    public void exception(String path, Exception ex);

    public void close();

    public void close(Callback callback);

    abstract class Impl implements ImportReport {
        AtomicInteger errorsCollected = new AtomicInteger(0);
        private void addError(Throwable error, File file, String[] files, String readerType)
        {
            ErrorContainer errorContainer = new ErrorContainer();
            errorContainer.setFiles(files);
            errorContainer.setSelectedFile(file);
            errorContainer.setReaderType(readerType);
            errorContainer.setCommentType("2");
            
            errorContainer.setJavaVersion(System.getProperty("java.version"));
            errorContainer.setJavaClasspath(System.getProperty("java.class.path"));
            errorContainer.setOSName(System.getProperty("os.name"));
            errorContainer.setOSArch(System.getProperty("os.arch"));
            errorContainer.setOSVersion(System.getProperty("os.version"));
            errorContainer.setError(error);
            
            ErrorHandler errorHandler = ErrorHandler.getErrorHandler();
            errorHandler.addError(errorContainer);
            
            errorsCollected.incrementAndGet();
            
        }
    }
    
}
