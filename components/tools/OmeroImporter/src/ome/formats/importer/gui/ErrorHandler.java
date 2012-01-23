/*
 * ome.formats.importer.gui.AddDatasetDialog
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */

package ome.formats.importer.gui;

import java.awt.BorderLayout;
import java.io.IOException;
import java.util.Vector;
import java.util.concurrent.ScheduledExecutorService;

import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import ome.formats.importer.IObservable;
import ome.formats.importer.IObserver;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.ImportEvent;
import ome.formats.importer.util.ErrorContainer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Brian Loranger brain at lifesci.dundee.ac.uk
 *
 */
@SuppressWarnings("serial")
public class ErrorHandler extends JPanel implements IObserver, IObservable {
    /** Logger for this class */
    @SuppressWarnings("unused")
    private static Log log = LogFactory.getLog(ErrorHandler.class);

    public final MyErrorHandler delegate;
    private final ScheduledExecutorService ex;
    private final ErrorTable errorTable;

    /**
     * Creates and manages error messages and the errors table tab
     * @param ex
     * @param config
     */
    public ErrorHandler(ScheduledExecutorService ex, ImportConfig config) {
        this.ex = ex;
        this.setOpaque(false);
        setLayout(new BorderLayout());

        errorTable = new ErrorTable();
        delegate = new MyErrorHandler(config, this);

        if (errorTable != null)
            add(errorTable, BorderLayout.CENTER);

        errorTable.addObserver(this);
    }

    /**
     * @author Brian Loranger brain at lifesci.dundee.ac.uk
     *
     */
    class MyErrorHandler extends ome.formats.importer.util.ErrorHandler {

        private final JPanel panel;
        private DebugMessenger debugMessenger;

        /**
         * @param config - importerConfig
         * @param panel - parent panel
         */
        MyErrorHandler(ImportConfig config, JPanel panel) {
            super(config);
            this.panel = panel;
        }

        /* (non-Javadoc)
         * @see ome.formats.importer.util.ErrorHandler#onUpdate(ome.formats.importer.IObservable, ome.formats.importer.ImportEvent)
         */
        public void onUpdate(IObservable importLibrary, ImportEvent event) {
            if (event instanceof ImportEvent.ERRORS_SEND) {
                cancelUploads = false;
                errorTable.enableCancelBtn(true);
                debugMessenger = new DebugMessenger(null,
                        "OMERO.importer Error Dialog", config, false, errors);
                debugMessenger.addObserver(this);
                debugMessenger.setAlwaysOnTop(true);
            }

            if (event instanceof ImportEvent.DEBUG_SEND) {
                ImportEvent.DEBUG_SEND ev = (ImportEvent.DEBUG_SEND) event;
                sendFiles = ev.sendFiles;
                sendLogs = ev.sendLogs;

                Runnable run = new Runnable() {
                    public void run() {
                        try {
                            sendErrors();
                        } catch (Throwable error) {
                            error.printStackTrace();
                        }
                    }
                };
                ex.execute(run);
            }

            else if (event instanceof ImportEvent.FILE_UPLOAD_STARTED) {
                ImportEvent.FILE_UPLOAD_STARTED ev = (ImportEvent.FILE_UPLOAD_STARTED) event;
                errorTable.setFilesInSet(ev.fileTotal);
            } else if (event instanceof ImportEvent.FILE_UPLOAD_BYTES) {
                ImportEvent.FILE_UPLOAD_BYTES ev = (ImportEvent.FILE_UPLOAD_BYTES) event;
                errorTable.setBytesFileSize(Long.valueOf(ev.contentLength).intValue());
                errorTable.setBytesProgress(Long.valueOf(ev.uploadedBytes).intValue());
                errorTable.setFilesProgress(ev.fileIndex);
            } else if (event instanceof ImportEvent.FILE_UPLOAD_COMPLETE) {
                ImportEvent.FILE_UPLOAD_COMPLETE ev = (ImportEvent.FILE_UPLOAD_COMPLETE) event;
                errorTable.setFilesProgress(ev.fileIndex);
            } else if (event instanceof ImportEvent.FILE_UPLOAD_ERROR) {
                ImportEvent.FILE_UPLOAD_ERROR ev = (ImportEvent.FILE_UPLOAD_ERROR) event;
                errorTable.setFilesProgress(ev.fileIndex);
                log.info("Error while sending QA Feedback file:" + ev.filename);
                fileUploadErrors  = true;
            } else if (event instanceof ImportEvent.FILE_UPLOAD_CANCELLED || event instanceof ImportEvent.ERRORS_UPLOAD_CANCELLED) {
            	errorTable.resetProgress();
            	log.debug("Uploads Cancelled");
                cancelUploads = true;
                super.cancelUploads = true;
            } else if (event instanceof ImportEvent.ERRORS_CLEARED) {
            	ImportEvent.ERRORS_CLEARED ev = (ImportEvent.ERRORS_CLEARED) event;
            	super.clearErrors(ev.index);
            }
        }

        /* (non-Javadoc)
         * @see ome.formats.importer.util.ErrorHandler#onCancel()
         */
        @Override
        protected void onCancel() {
            super.onCancel();
            errorTable.enableSendBtn(true);
        }

        /* (non-Javadoc)
         * @see ome.formats.importer.util.ErrorHandler#sendErrors()
         */
        protected void sendErrors() {
            errorTable.enableSendBtn(false);
            super.sendErrors();
        }

        /* (non-Javadoc)
         * @see ome.formats.importer.util.ErrorHandler#isSend(int)
         */
        @Override
        protected boolean isSend(int index) {
            return super.isSend(index)
                    && ((Boolean) errorTable.table.getValueAt(index, 0))
                            .booleanValue();
        }

        /* (non-Javadoc)
         * @see ome.formats.importer.util.ErrorHandler#onSending(int)
         */
        @Override
        protected void onSending(int index) {
            super.onSending(index);
            errorTable.setProgressSending(index);
        }

        /* (non-Javadoc)
         * @see ome.formats.importer.util.ErrorHandler#onSent(int)
         */
        @Override
        protected void onSent(int index) {
            super.onSent(index);
            errorTable.setProgressDone(index);
        }

        /* (non-Javadoc)
         * @see ome.formats.importer.util.ErrorHandler#onNotSending(int, java.lang.String)
         */
        @Override
        protected void onNotSending(int index, String serverReply) {
            super.onNotSending(index, serverReply);
            JEditorPane reply = new JEditorPane("text/html", serverReply);
            reply.setEditable(false);
            reply.setOpaque(false);
            // JOptionPane.showMessageDialog(this, reply);
            errorTable.setProgressDone(index);
        }

        /* (non-Javadoc)
         * @see ome.formats.importer.util.ErrorHandler#onException(java.lang.Exception)
         */
        @Override
        protected void onException(Exception e) {
            super.onException(e);
            /*
            // Get the full debug text
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);

            String debugText = sw.toString();
            debugDocument = (StyledDocument) debugTextPane.getDocument();
            debugStyle = debugDocument.addStyle("StyleName", null);
            StyleConstants.setForeground(debugStyle, Color.black);
            StyleConstants.setFontFamily(debugStyle, "SansSerif");
            StyleConstants.setFontSize(debugStyle, 12);
            StyleConstants.setBold(debugStyle, false);

            GuiCommonElements.appendTextToDocument(debugDocument, debugStyle, "");

            GuiCommonElements.appendTextToDocument(debugDocument, debugStyle, "----\n"
                    + debugText);
            */
            String sendErrorMsg = "Sorry, but due to an error we were not able "
                    + "to automatically \n send your debug information. \n\n"
                    + "Pleae let us know about this problem by contacting \n\n"
                    + "us at <a href='mailto:comments@openmicroscopy.org.uk'>.";
            try {
                JEditorPane popup = new JEditorPane(sendErrorMsg);
                JOptionPane.showMessageDialog(panel, popup);
                errorTable.enableSendBtn(true);
            } catch (IOException e1) {
            }
        }

        /* (non-Javadoc)
         * @see ome.formats.importer.util.ErrorHandler#finishCancelled()
         */
        @Override
        protected void finishCancelled() {
            super.finishCancelled();
            errorTable.setCancelBtnCancelled();
            JOptionPane.showMessageDialog(panel,
                    "\nThank you for your support!"
                            + "\n\nYou have cancelled uploading your data"
                            + "\nfiles to us, however some files may have"
                            + "\nbeen sent (as shown on the error list)."
                            + "\n\nIf you wish to continue uploading your"
                            + "\nfiles, simply click the 'Send Feedback'"
                            + "\nbutton again.", "Cancelled Upload!",
                    JOptionPane.INFORMATION_MESSAGE);
            errorTable.resetProgress();
        }

        /* (non-Javadoc)
         * @see ome.formats.importer.util.ErrorHandler#finishComplete()
         */
        @Override
        protected void finishComplete() {
            super.finishComplete();
            errorTable.setCancelBtnVisible(false);
            JOptionPane.showMessageDialog(panel,
                    "\nThank you for your support, your errors "
                            + "\nhave successfully been collected."
                            + "\n\nIf you have provided us with an email"
                            + "\naddress, you should receive a message"
                            + "\nshortly detailing how you can track the"
                            + "\nstatus of your errors.", "Success!",
                    JOptionPane.INFORMATION_MESSAGE);
            errorTable.resetProgress();
        }

        /* (non-Javadoc)
         * @see ome.formats.importer.util.ErrorHandler#finishWithErrors()
         */
        @Override
        protected void finishWithErroredFiles() {
            super.finishWithErroredFiles();
            errorTable.setCancelBtnCancelled();
            
            JOptionPane.showMessageDialog(panel,
                    "\nThank you for your support, your errors "
                            + "\nhave successfully been collected."
                            + "\n\nIf you have provided us with an email"
                            + "\naddress, you should receive a message"
                            + "\nshortly detailing how you can track the"
                            + "\nstatus of your errors."
                            + "\n\n Please note that during the import"
                            + "\nthere were problems sending some of your"
                            + "\nincluded files, however the error itself"
                            + "\nhas been reported."
                            , "Success!",
                    JOptionPane.INFORMATION_MESSAGE);
            errorTable.resetProgress();
        }
        
        /* (non-Javadoc)
         * @see ome.formats.importer.util.ErrorHandler#onAddError(ome.formats.importer.util.ErrorContainer, java.lang.String)
         */
        @Override
        protected void onAddError(ErrorContainer errorContainer, String message) {
            super.onAddError(errorContainer, message);
            Vector<Object> row = new Vector<Object>();
            row.add(Boolean.valueOf(true));
            if (errorContainer.getSelectedFile() == null)
            {
                row.add("None");
            }
            else
            {
                row.add(errorContainer.getSelectedFile().getName());   
            }
            row.add(message);
            row.add(-1);
            row.add(null);
            row.add(null);
            row.add(null); // full error for tooltip
            errorTable.addRow(row);
            errorTable.fireTableDataChanged();
            notifyObservers(new ImportEvent.ERRORS_PENDING());
        }

    }

    /* (non-Javadoc)
     * @see ome.formats.importer.IObserver#update(ome.formats.importer.IObservable, ome.formats.importer.ImportEvent)
     */
    public void update(IObservable importLibrary, ImportEvent event) {
        this.delegate.update(importLibrary, event);
    }

    /* (non-Javadoc)
     * @see ome.formats.importer.IObservable#addObserver(ome.formats.importer.IObserver)
     */
    public boolean addObserver(IObserver object) {
        return this.delegate.addObserver(object);
    }

    /* (non-Javadoc)
     * @see ome.formats.importer.IObservable#deleteObserver(ome.formats.importer.IObserver)
     */
    public boolean deleteObserver(IObserver object) {
        return this.delegate.deleteObserver(object);
    }

    /* (non-Javadoc)
     * @see ome.formats.importer.IObservable#notifyObservers(ome.formats.importer.ImportEvent)
     */
    public void notifyObservers(ImportEvent event) {
        this.delegate.notifyObservers(event);
    }
}
