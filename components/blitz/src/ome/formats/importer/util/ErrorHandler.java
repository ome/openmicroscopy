/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.formats.importer.util;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import loci.formats.MissingLibraryException;
import ome.formats.importer.IObservable;
import ome.formats.importer.IObserver;
import ome.formats.importer.ImportCandidates;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.ImportEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Top of the error handling hierarchy. Will add errors to a queue
 * which can be sent with {@link #sendErrors()}. Subclasses will get
 * a change to handle all {@link ImportEvent} instances, but should
 * try not to duplicate handling.
 *
 * @author Brian W. Loranger
 * @author Josh Moore
 *
 * @since Beta4.1
 */
public abstract class ErrorHandler implements IObserver, IObservable {

    /**
     * @author Brian W. Loranger
     * @author Josh Moore
     */
    public abstract static class EXCEPTION_EVENT extends ImportEvent {
        public final Exception exception;

        /**
         * @param exception - set exception
         */
        public EXCEPTION_EVENT(Exception exception) {
            this.exception = exception;
        }
    }

    /**
     * @author Brian W. Loranger
     * @author Josh Moore
     */
    public static class INTERNAL_EXCEPTION extends EXCEPTION_EVENT {
        public final String filename;
        public final String[] usedFiles;
        public final String reader;
        public INTERNAL_EXCEPTION(String filename, Exception exception, String[] usedFiles, String reader) {
            super(exception);
            this.filename = filename;
            this.usedFiles = usedFiles;
            this.reader = reader;
        }
        /* (non-Javadoc)
         * @see ome.formats.importer.ImportEvent#toLog()
         */
        @Override
        public String toLog() {
		return String.format("%s: %s\n%s", super.toLog(), filename,
				             getStackTrace(exception));
        }
    }

    /**
     * Unlike {@link FILE_EXCEPTION}, UNKNOWN_FORMAT does not have a reader
     * since bio-formats is telling us that it does not know how to handle
     * the given file. This should be generally be considered less fatal
     * than a {@link FILE_EXCEPTION}, but if the user is specifically saying
     * that a file should be imported, and an {@link UNKNOWN_FORMAT} is raised,
     * then perhaps there is a configuration issue.
     *
     * @author Brian W. Loranger
     * @author Josh Moore
     */
    public static class UNKNOWN_FORMAT extends EXCEPTION_EVENT {
        public final String filename;
        public final Object source;

        /**
         * @param filename
         * @param exception
         * @param source
         */
        public UNKNOWN_FORMAT(String filename, Exception exception, Object source) {
            super(exception);
            this.filename = filename;
            this.source = source;
        }
        /* (non-Javadoc)
         * @see ome.formats.importer.ImportEvent#toLog()
         */
        @Override
        public String toLog() {
            return super.toLog() + ": "+filename;
        }
    }

    /**
     * Similar to {@link UNKNOWN_FORMAT} UNREADABLE_FILE specifies that the
     * file which is being accessed is unreadable (does not exist or canRead
     * is false), so if the user is specifically saying that the file should
     * be imported, there may be some underlying issue.
     *
     * @author Brian W. Loranger
     * @author Josh Moore
     */
    public static class UNREADABLE_FILE extends EXCEPTION_EVENT {
        public final String filename;
        public final Object source;

        /**
         * @param filename
         * @param exception
         * @param source
         */
        public UNREADABLE_FILE(String filename, Exception exception, Object source) {
            super(exception);
            this.filename = filename;
            this.source = source;
        }
        /* (non-Javadoc)
         * @see ome.formats.importer.ImportEvent#toLog()
         */
        @Override
        public String toLog() {
            return super.toLog() + ": "+filename;
        }
    }

    /**
     * {@link FILE_EXCEPTION}s are thrown any time in the context of a particular
     * file and otherwise unspecified exception takes place. An example of an
     * exception which receives separate handling is {@link UNKNOWN_FORMAT} which
     * can be considered less serious than {@link FILE_EXCEPTION}. Subclasses of
     * this class may should receive special handling. For example,
     * {@link ImportCandidates#SCANNING_FILE_EXCEPTION} may be considered less
     * significant if the user was trying to import a large directory.
     * {@link MISSING_LIBRARY} below is probably more of a warn situation rather
     * than an error.
     */
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
            //this.exception.printStackTrace();
            return super.toLog() + ": "+filename;
        }
    }


    /**
     * A {@link FILE_EXCEPTION} caused specifically by some library (native
     * or otherwise) not being installed locally.
     */
    public static class MISSING_LIBRARY extends FILE_EXCEPTION {
        public MISSING_LIBRARY(String filename, MissingLibraryException exception, String[] usedFiles, String reader) {
            super(filename, exception, usedFiles, reader);
        }
    }

    final protected Logger log = LoggerFactory.getLogger(getClass());

    final protected ArrayList<IObserver> observers = new ArrayList<IObserver>();

    final protected ArrayList<ErrorContainer> errors = new ArrayList<ErrorContainer>();

    final protected ImportConfig config;

    protected boolean cancelUploads = false;

    protected boolean sendFiles = true;

    protected boolean sendLogs = true;

	public boolean fileUploadErrors = false;

    protected int totalErrors = 0;

    // These values are used within the sendErrors loop. They are *very* not
    // thread-safe.

    private HtmlMessenger messenger;

    private FileUploader fileUploader;

    private String serverReply;

    /**
     * Initialize
     *
     * @param config
     */
    public ErrorHandler(ImportConfig config)
    {
        this.config = config;
    }

    /* (non-Javadoc)
     * @see ome.formats.importer.IObserver#update(ome.formats.importer.IObservable, ome.formats.importer.ImportEvent)
     */
    public final void update(IObservable observable, ImportEvent event) {


        if (event instanceof MISSING_LIBRARY) {
            MISSING_LIBRARY ev = (MISSING_LIBRARY) event;
            log.warn(ev.toLog(), ev.exception);
        }

        else if (event instanceof FILE_EXCEPTION) {
            FILE_EXCEPTION ev = (FILE_EXCEPTION) event;
            log.error(ev.toLog(), ev.exception);
            addError(ev.exception, new File(ev.filename), ev.usedFiles, ev.reader);
        }

        else if (event instanceof INTERNAL_EXCEPTION) {
            INTERNAL_EXCEPTION ev = (INTERNAL_EXCEPTION) event;
            log.error(event.toLog(), ev.exception);
            addError(ev.exception, new File(ev.filename), ev.usedFiles, ev.reader);
        }

        else if (event instanceof UNKNOWN_FORMAT) {
            UNKNOWN_FORMAT ev = (UNKNOWN_FORMAT) event;
            String[] usedFiles = {ev.filename};
            // Here it is important to not report errors which
            // are coming from ImportCandidates, since that doesn't
            // count as an error situation. Previously, this checked
            // for (ev.source instanceof ImportLibrary), but that is
            // no longer on the compile-time classpath.
            if (!(ev.source instanceof ImportCandidates))
                addError(ev.exception, new File(ev.filename), usedFiles, "");
            log.debug(event.toLog());
        }

        else if (event instanceof EXCEPTION_EVENT) {
            EXCEPTION_EVENT ev = (EXCEPTION_EVENT) event;
            log.error(ev.toLog(), ev.exception);
        }

        onUpdate(observable, event);

    }

    /**
     * @return number of errors in ErrorContainer array
     */
    public int errorCount()
    {
        return errors.size();
    }

    /**
     * abstract on update method
     *
     * @param importLibrary
     * @param event - importEvent
     */
    protected abstract void onUpdate(IObservable importLibrary, ImportEvent event);

    /**
     * Send existing errors in ErrorContainer array to server
     */
    protected void sendErrors() {

        for (int i = 0; i < errors.size(); i++) {

            if (!isSend(i))
            {
                onSent(i);
                continue; // Don't send file if not selected
            }

            if (cancelUploads) {
                onCancel();
                break;
            }

            ErrorContainer errorContainer = errors.get(i);
            if (errorContainer.getStatus() != -1) // if file not pending, skip
                // it
                continue;

            Map<String, String> postList = new HashMap<String, String>();

            postList.put("java_version", errorContainer.getJavaVersion());
            postList.put("java_classpath", errorContainer.getJavaClasspath());
            postList.put("app_version", errorContainer.getAppVersion());
            postList.put("comment_type", errorContainer.getCommentType());
            postList.put("os_name", errorContainer.getOSName());
            postList.put("os_arch", errorContainer.getOSArch());
            postList.put("os_version", errorContainer.getOSVersion());
            postList.put("extra", errorContainer.getExtra());
            postList.put("error", getStackTrace(errorContainer.getError()));
            postList.put("comment", errorContainer.getComment());
            postList.put("email", errorContainer.getEmail());
            postList.put("app_name", "2");
            postList.put("import_session", "test");
            postList.put("absolute_path", errorContainer.getAbsolutePath() + "/");

            String sendUrl = config.getTokenUrl();

            if (isSend(i)) {
                if (!sendFiles)
                {
                    errorContainer.clearFiles();
                }

                if (sendLogs)
                {
                    errorContainer.addFile(config.getLogFile());
                }

                if (sendFiles)
                {
                    postList.put("selected_file",
                            errorContainer.getSelectedFile().getName());
                    postList.put("absolute_path",
                            errorContainer.getAbsolutePath());
                    String[] files = errorContainer.getFiles();

                    if (files != null && files.length > 0) {
                        for (String f : errorContainer.getFiles()) {
                            File file = new File(f);
                            postList.put("additional_files", file.getName());
                            if (file.getParent() != null)
                                postList.put("additional_files_path",
                                        file.getParent() + "/");
                            postList.put("additional_files_size",
                                    ((Long) file.length()).toString());
                        }
                    }
                }
            }

            try {

                messenger = new HtmlMessenger(sendUrl, postList);
                serverReply = messenger.executePost();
                if (sendFiles || sendLogs) {
                    onSending(i);
                    errorContainer.setToken(serverReply);
                    fileUploader = new FileUploader(messenger.getCommunicationLink(
                            config.getUploaderUrl()));
                    fileUploader.addObserver(this);
                    fileUploader.uploadFiles(config.getUploaderUrl(), 2000,
                            errorContainer);
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
        }

        if (fileUploadErrors) {
            finishWithErroredFiles();
            notifyObservers(new ImportEvent.ERRORS_COMPLETE());
        } else {
            finishComplete();
            notifyObservers(new ImportEvent.ERRORS_COMPLETE());
        }
    }

    /**
     * Add detailed error to error container array
     * @param error - error thrown
     * @param file - head file for error
     * @param files - all files in import collection
     * @param readerType - reader type supplied from bio-formats
     */
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
        errorContainer.setAppVersion(config.getVersionNumber());
        errorContainer.setError(error);
        addError(errorContainer);
    }

    /**
     * add simple error to error container array
     *
     * @param errorContainer
     */
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

    protected void clearErrors(int index) {
    	errors.remove(index);
    }
    
    //
    // OBSERVER PATTERN
    //

    /* (non-Javadoc)
     * @see ome.formats.importer.IObservable#addObserver(ome.formats.importer.IObserver)
     */
    public final boolean addObserver(IObserver object)
    {
        return observers.add(object);
    }

    /* (non-Javadoc)
     * @see ome.formats.importer.IObservable#deleteObserver(ome.formats.importer.IObserver)
     */
    public final boolean deleteObserver(IObserver object)
    {
        return observers.remove(object);

    }

    /* (non-Javadoc)
     * @see ome.formats.importer.IObservable#notifyObservers(ome.formats.importer.ImportEvent)
     */
    public final void notifyObservers(ImportEvent event)
    {
        for (IObserver observer : observers)
        {
            observer.update(this, event);
        }
    }


    //
    // OVERRIDEABLE METHODS
    //

    /**
     * action to take on cancel
     */
    protected void onCancel()
    {
        
    }


    /**
     * Action to take on adding an error to container
     *
     * @param errorContainer - error container
     * @param message - message string for action (if needed)
     */
    protected void onAddError(ErrorContainer errorContainer, String message)
    {
    }

    /**
     * Check if files need sending at error container index
     * @param index - index in error container
     * @return - true if file is to be sent
     */
    protected boolean isSend(int index)
    {
        if (errors.get(index).getSelectedFile() == null) {
            return false;
        }
        return true;
    }

    /**
     * @param index
     */
    protected void onSending(int index)
    {
    }

    /**
     * @param index
     */
    protected void onSent(int index)
    {
    }

    /**
     * @param index
     * @param serverReply
     */
    protected void onNotSending(int index, String serverReply)
    {
    }

    /**
     * Action to take on exception
     * @param exception
     */
    protected void onException(Exception exception)
    {
        notifyObservers(new ImportEvent.ERRORS_FAILED());
    }

    /**
     * Action to take when finish cancelled
     */
    protected void finishCancelled()
    {
        
    }

    /**
     * Action to take when finish completed
     */
    protected void finishComplete()
    {
    }

    /**
     * Action to take when finish completed but with some errors
     * (For example, missing files)
     */
    protected void finishWithErroredFiles()
    {
    }


    /**
     * Return stack trace from throwable
     * @param throwable
     * @return stack trace
     */
    public static String getStackTrace(Throwable throwable)
    {
        final Writer writer = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(writer);
        throwable.printStackTrace(printWriter);
        return writer.toString();
      }


}
