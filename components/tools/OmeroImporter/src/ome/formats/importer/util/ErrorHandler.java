/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.formats.importer.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ome.formats.importer.IObservable;
import ome.formats.importer.IObserver;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.ImportEvent;

import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @since Beta4.1
 */
public abstract class ErrorHandler implements IObserver, IObservable {

    public static class EXCEPTION_EVENT extends ImportEvent {
        public final Exception exception;

        public EXCEPTION_EVENT(Exception exception) {
            this.exception = exception;
        }
    }
    
    public static class INTERNAL_EXCEPTION extends EXCEPTION_EVENT {
        public INTERNAL_EXCEPTION(Exception exception) {
            super(exception);
        }
    }

    public static class FILE_EXCEPTION extends EXCEPTION_EVENT {
        public final String filename;
        public final String[] usedFiles;
        public final String reader;
        public FILE_EXCEPTION(String filename, Exception exception, String[] usedFiles, String reader) {
            super(exception);
            this.filename = filename;
            this.usedFiles = usedFiles;
            this.reader = reader;
        }
        @Override
        public String toLog() {
            return super.toLog() + ": "+filename;
        }
        
    }

    final protected Log log = LogFactory.getLog(getClass());

    final protected ArrayList<IObserver> observers = new ArrayList<IObserver>();

    final protected ArrayList<ErrorContainer> errors = new ArrayList<ErrorContainer>();

    final protected ImportConfig config;
    
    protected boolean cancelUploads = false;

    protected boolean sendFiles = true;

    protected int totalErrors = 0;
    
    public ErrorHandler(ImportConfig config) {
        this.config = config;
    }

    public final void update(IObservable observable, ImportEvent event) {
        System.out.println(event.toLog());
        if (event instanceof FILE_EXCEPTION) {
            FILE_EXCEPTION ev = (FILE_EXCEPTION) event;
            addError(ev.exception, new File(ev.filename), ev.usedFiles, ev.reader);
        }
        onUpdate(observable, event);
    }
    
    public int errorCount() {
        return errors.size();
    }
    
    protected abstract void onUpdate(IObservable importLibrary, ImportEvent event);

    protected void sendErrors() {

        for (int i = 0; i < errors.size(); i++) {

            if (cancelUploads) {
                onCancel();
                break;
            }

            ErrorContainer errorContainer = errors.get(i);
            if (errorContainer.getStatus() != -1) // if file not pending, skip
                // it
                continue;

            List<Part> postList = new ArrayList<Part>();

            postList.add(new StringPart("java_version", errorContainer
                    .getJavaVersion()));
            postList.add(new StringPart("java_classpath", errorContainer
                    .getJavaClasspath()));
            postList.add(new StringPart("app_version", errorContainer
                    .getAppVersion()));
            postList.add(new StringPart("comment_type", errorContainer
                    .getCommentType()));
            postList.add(new StringPart("os_name", errorContainer.getOSName()));
            postList.add(new StringPart("os_arch", errorContainer.getOSArch()));
            postList.add(new StringPart("os_version", errorContainer
                    .getOSVersion()));
            postList.add(new StringPart("extra", errorContainer.getExtra()));
            postList.add(new StringPart("error", errorContainer.getError()
                    .toString()));
            postList
                    .add(new StringPart("comment", errorContainer.getComment()));
            postList.add(new StringPart("email", errorContainer.getEmail()));
            postList.add(new StringPart("app_name", "2"));
            postList.add(new StringPart("import_session", "test"));
            postList.add(new StringPart("absolute_path", "blarg"));

            String sendUrl = config.getTokenUrl();

            if (isSend(i)) {
                postList.add(new StringPart("selected_file", errorContainer
                        .getSelectedFile().getName()));
                postList.add(new StringPart("absolute_path", errorContainer
                        .getAbsolutePath()));

                String[] files = errorContainer.getFiles();
                if (files != null && files.length > 0) {
                    for (String f : errorContainer.getFiles()) {
                        File file = new File(f);
                        postList.add(new StringPart("additional_files", file
                                .getName()));
                        postList.add(new StringPart("additional_files_size",
                                ((Long) file.length()).toString()));
                        if (file.getParent() != null)
                            postList.add(new StringPart(
                                    "additional_files_path", file.getParent()));
                    }
                }
            }

            try {
                HtmlMessenger messenger = new HtmlMessenger(sendUrl, postList);
                String serverReply = messenger.executePost();

                if (isSend(i)) {
                    onSending(i);
                    errorContainer.setToken(serverReply);

                    FileUploader fileUploader = new FileUploader(messenger.getHttpClient());
                    fileUploader.addObserver(this);

                    fileUploader.uploadFiles(config.getUploaderUrl(), 2000, errorContainer);
                    onSent(i);
                } else {
                    onNotSending(i, serverReply);
                }
            } catch (Exception e) {
                log.error("Error while sending error information.", e);
                onException(e);
            }

        }
        if (cancelUploads) {
            finishCancelled();
        } else {
            finishComplete();
            notifyObservers(new ImportEvent.ERRORS_COMPLETE());
        }
    }


    protected void addError(Throwable error, File file, String[] files,
            String readerType) {
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
        addError(errorContainer);
    }
    
    private void addError(ErrorContainer errorContainer) {
        String errorMessage = errorContainer.getError().toString();
        String[] splitMessage = errorMessage.split("\n");

        errorMessage = errorMessage.replaceAll("\n", "<br>&nbsp;&nbsp;");

        errorContainer.setIndex(totalErrors);
        totalErrors = totalErrors + 1;
        errorContainer.setStatus(-1); // pending status

        errors.add(errorContainer);
        onAddError(errorContainer, splitMessage[0]);
        notifyObservers(new ImportEvent.ERRORS_PENDING());
    }

    //
    // OBSERVER PATTERN
    //
    
    public final boolean addObserver(IObserver object) {
        return observers.add(object);
    }

    public final boolean deleteObserver(IObserver object) {
        return observers.remove(object);

    }

    public final void notifyObservers(ImportEvent event) {
        for (IObserver observer : observers) {
            observer.update(this, event);
        }
    }
    
    
    //
    // OVERRIDEABLE METHODS
    //
    
    protected void onCancel() {

    }
 
    
    protected void onAddError(ErrorContainer errorContainer, String message) {
    }
 
    protected boolean isSend(int index) {
        if (errors.get(index).getSelectedFile() == null) {
            return false;
        }
        return sendFiles;
    }
 
    protected void onSending(int index) {
        
    }
    
    protected void onSent(int index) {
        
    }
    
    protected void onNotSending(int index, String serverReply) {
        
    }
    
    protected void onException(Exception e) {
        
    }
    
    protected void finishCancelled() {
    }

    protected void finishComplete() {
    }

}
