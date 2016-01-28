/*
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.formats.importer.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
import ome.formats.importer.ImportContainer;
import ome.formats.importer.ImportEvent;
import omero.client;
import omero.api.IQueryPrx;
import omero.api.RawFileStorePrx;
import omero.api.ServiceFactoryPrx;
import omero.model.OriginalFile;
import omero.sys.ParametersI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Top of the error handling hierarchy. Will add errors to a queue
 * which can be sent with {@link #sendErrors()}. Subclasses will get
 * a chance to handle all {@link ImportEvent} instances, but should
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
         * @param filename the filename
         * @param exception the exception
         * @param source the source (e.g., {@link ImportCandidates})
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
         * @param filename the filename
         * @param exception the exception
         * @param source the source
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
     * this class may should receive special handling.
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

    final protected List<IObserver> observers = new ArrayList<IObserver>();

    final protected List<ErrorContainer> errors = new ArrayList<ErrorContainer>();

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

    /** Host information about the file and its corresponding log file.*/
    private Map<String, Long> logFiles;

    /** Host information about the file and its corresponding import candidate.*/
    protected Map<String, ImportContainer> icMap;

    /**
     * Initialize
     *
     * @param config the import configuration
     */
    public ErrorHandler(ImportConfig config)
    {
        this.config = config;
        logFiles = new HashMap<String, Long>();
        icMap = new HashMap<String, ImportContainer>();
    }

    /* (non-Javadoc)
     * @see ome.formats.importer.IObserver#update(ome.formats.importer.IObservable, ome.formats.importer.ImportEvent)
     */
    public final void update(IObservable observable, ImportEvent event) {

        if (event instanceof MISSING_LIBRARY) {
            MISSING_LIBRARY ev = (MISSING_LIBRARY) event;
            log.warn(ev.toLog(), ev.exception);
        } else if (event instanceof FILE_EXCEPTION) {
            FILE_EXCEPTION ev = (FILE_EXCEPTION) event;
            log.error(ev.toLog(), ev.exception);
            addError(ev.exception, new File(ev.filename), ev.usedFiles, ev.reader);
        } else if (event instanceof INTERNAL_EXCEPTION) {
            INTERNAL_EXCEPTION ev = (INTERNAL_EXCEPTION) event;
            log.error(event.toLog(), ev.exception);
            addError(ev.exception, new File(ev.filename), ev.usedFiles, ev.reader);
        } else if (event instanceof UNKNOWN_FORMAT) {
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
        } else if (event instanceof EXCEPTION_EVENT) {
            EXCEPTION_EVENT ev = (EXCEPTION_EVENT) event;
            log.error(ev.toLog(), ev.exception);
        } else if (event instanceof ImportEvent.METADATA_IMPORTED) {
            ImportEvent.METADATA_IMPORTED e =
                    (ImportEvent.METADATA_IMPORTED) event;
            logFiles.put(e.container.getFile().getAbsolutePath(), e.logFileId);
        } else if (event instanceof ImportEvent.POST_UPLOAD_EVENT) {
            ImportEvent.POST_UPLOAD_EVENT e =
                    (ImportEvent.POST_UPLOAD_EVENT) event;
            icMap.put(e.container.getFile().getAbsolutePath(), e.container);
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
     * @param importLibrary the import library
     * @param event - importEvent
     */
    protected abstract void onUpdate(IObservable importLibrary, ImportEvent event);

    /**
     * Retrieve the log file.
     *
     * @param id The id of the file to load.
     * @param session The OMERO session.
     * @return See above.
     * @throws Throwable Thrown if an error occurred while loading file.
     */
    private File retrieveLogFile(Long id, ServiceFactoryPrx session)
            throws Throwable
    {
        if (id == null) return null;
        //dowload the file
        StringBuffer buf = new StringBuffer();
        buf.append("importLog_");
        buf.append(id);
        File logfile = File.createTempFile(buf.toString(), ".log");
        logfile.deleteOnExit();
        IQueryPrx svc = session.getQueryService();
        ParametersI param = new ParametersI();
        param.map.put("id", omero.rtypes.rlong(id));
        OriginalFile of = (OriginalFile) svc.findByQuery(
                "select p from OriginalFile as p where p.id = :id", param);
        if (of == null) return null;

        final String path = logfile.getAbsolutePath();

        RawFileStorePrx store = null;
        try {
            store = session.createRawFileStore();
            store.setFileId(id);
        } catch (Throwable e) {
           store.close();
          return null; // Never reached.
        }
        try {
            long size = -1;
            long offset = 0;
            int INC = 262144;
            FileOutputStream stream = new FileOutputStream(logfile);
            try {
                try {
                    size = store.size();
                    for (offset = 0; (offset+INC) < size;) {
                        stream.write(store.read(offset, INC));
                        offset += INC;
                    }
                } finally {
                    stream.write(store.read(offset, (int) (size-offset)));
                    stream.close();
                }
            } catch (Exception e) {
                log.error("Cannot write log file", e);
                if (stream != null) stream.close();
            }
        } catch (IOException e) {
            log.error("Cannot write log file", e);
        } finally {
            store.close();
        }
        return logfile;
    }
    /**
     * Send existing errors in ErrorContainer array to server
     */
    protected void sendErrors() {

        //create an omero client.
        client sc = null;
        client client = null;
        ServiceFactoryPrx session = null;
        try {
            if (sendLogs || sendFiles) {
                sc = new client(config.hostname.get(), config.port.get());
                ServiceFactoryPrx entryEncrypted;
                if (!config.sessionKey.empty()) {
                    entryEncrypted = sc.joinSession(config.sessionKey.get());
                } else {
                    entryEncrypted = sc.createSession(config.username.get(),
                            config.password.get());
                }
                client = sc.createClient(false);
                session = client.getSession();
            }
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

                    if (sendLogs || sendFiles) {
                        File f = errorContainer.getSelectedFile();
                        if (f != null) {
                            Long id = logFiles.get(f.getAbsolutePath());
                            //load the log
                            File logFile = null;
                            try {
                                logFile = retrieveLogFile(id, session);
                            } catch (Throwable e) {
                                log.error("Cannot load log file", e);
                            }
                            
                            if (logFile != null) {
                                sendLogs = true;
                                errorContainer.addFile(logFile.getAbsolutePath());
                            } else sendLogs = false;
                        } else sendLogs = false;
                    }
                }
                messenger = new HtmlMessenger(sendUrl, postList);
                serverReply = messenger.executePost();
                if (sendFiles || sendLogs) {
                    onSending(i);
                    log.info("Sending File(s)...");
                    errorContainer.setToken(serverReply);
                    fileUploader = new FileUploader(
                            messenger.getCommunicationLink(
                            config.getUploaderUrl()));
                    fileUploader.addObserver(this);
                    fileUploader.uploadFiles(config.getUploaderUrl(), 2000,
                            errorContainer);
                    onSent(i);
                } else {
                    onNotSending(i, serverReply);
                }
            }
        } catch (Exception e) {
            log.error("Error during upload", e);
        } finally {
            if (client != null) client.__del__();
            if (sc != null) sc.__del__();
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
     * @param index the index in the error container
     */
    protected void onSending(int index)
    {
    }

    /**
     * @param index the index in the error container
     */
    protected void onSent(int index)
    {
    }

    /**
     * @param index the index in the error container
     * @param serverReply the reply from the server
     */
    protected void onNotSending(int index, String serverReply)
    {
    }

    /**
     * Action to take on exception
     * @param exception the exception
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
     * Execute a post with the given post list. This can be overwritten in order
     * to test error handling without touching QA. The server reply should be
     * non-null, but is otherwise unimportant.
     *
     * @param sendUrl the HTTP POST URL
     * @param postList the form values
     * @throws HtmlMessengerException if POST fails
     */
    public void executePost(String sendUrl, Map<String, String> postList)
            throws HtmlMessengerException {
        messenger = new HtmlMessenger(sendUrl, postList);
        serverReply = messenger.executePost();
    }

    /**
     * Upload a single {@link ErrorContainer}. This can be overwritten in order
     * to test error handling without touching QA.
     *
     * @param errorContainer the error container
     */
    public void uploadFile(ErrorContainer errorContainer) {
        errorContainer.setToken(serverReply);
        try {
            fileUploader = new FileUploader(messenger.getCommunicationLink(
                    config.getUploaderUrl()));
            fileUploader.addObserver(this);
            fileUploader.uploadFiles(config.getUploaderUrl(), 2000, errorContainer);
        } catch (Exception e) {
            log.error("Error during upload", e);
        }
    }
    
    /**
     * Return the stack trace from a {@link Throwable}.
     * @param throwable the {@link Throwable} to inspect
     * @return the stack trace
     */
    public static String getStackTrace(Throwable throwable)
    {
        final Writer writer = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(writer);
        throwable.printStackTrace(printWriter);
        return writer.toString();
      }


}
