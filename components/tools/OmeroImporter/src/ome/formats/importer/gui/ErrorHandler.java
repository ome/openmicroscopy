package ome.formats.importer.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import ome.formats.importer.IObservable;
import ome.formats.importer.IObserver;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.ImportEvent;
import ome.formats.importer.util.ErrorContainer;
import ome.formats.importer.util.FileUploader;
import ome.formats.importer.util.GuiCommonElements;
import ome.formats.importer.util.HtmlMessenger;

import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@SuppressWarnings("serial")
public class ErrorHandler extends JPanel implements IObserver, IObservable {
    /** Logger for this class */
    private static Log log = LogFactory.getLog(ErrorHandler.class);

    public final MyErrorHandler delegate; // THIS SHOULD NOT BE PUBLIC
    private final ErrorTable errorTable;
    private final GuiCommonElements gui;
    private JTextPane debugTextPane;
    private StyledDocument debugDocument;
    private Style debugStyle;

    private Thread runThread;

    public ErrorHandler(ImportConfig config) {
        this.setOpaque(false);
        setLayout(new BorderLayout());

        gui = new GuiCommonElements(config);
        errorTable = new ErrorTable(gui);
        delegate = new MyErrorHandler(config, this);

        if (errorTable != null)
            add(errorTable, BorderLayout.CENTER);

        errorTable.addObserver(this);
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        ImportConfig config = new ImportConfig(args);
        ErrorHandler eh = new ErrorHandler(config);
        JFrame f = new JFrame();
        f.getContentPane().add(eh);
        f.setVisible(true);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.pack();

        Vector<Object> row = new Vector<Object>();
        row.add(new Boolean(true));
        row.add("testfile.test");
        row.add("test/test");
        row.add(-1);
        row.add(null);
        row.add(null);
        row.add(null);
        eh.errorTable.addRow(row);
        eh.errorTable.fireTableDataChanged();
        eh.errorTable.setProgressSending(0);
    }

    class MyErrorHandler extends ome.formats.importer.util.ErrorHandler {

        private final JPanel panel;
        private DebugMessenger debugMessenger;

        MyErrorHandler(ImportConfig config, JPanel panel) {
            super(config);
            this.panel = panel;
        }

        public void update(IObservable importLibrary, ImportEvent event) {
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

                runThread = new Thread() {
                    public void run() {
                        try {
                            sendErrors();
                        } catch (Throwable error) {
                            error.printStackTrace();
                        }
                    }
                };
                runThread.start();
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
            } else if (event instanceof ImportEvent.FILE_UPLOAD_CANCELLED) {
                cancelUploads = true;
            }
        }

        @Override
        protected void onCancel() {
            super.onCancel();
            errorTable.enableSendBtn(true);
        }

        protected void sendErrors() {
            errorTable.enableSendBtn(false);
            super.sendErrors();
        }

        @Override
        protected boolean isSend(int index) {
            return super.isSend(index)
                    && ((Boolean) errorTable.table.getValueAt(index, 0))
                            .booleanValue();
        }

        @Override
        protected void onSending(int index) {
            super.onSending(index);
            errorTable.setProgressSending(index);
        }

        @Override
        protected void onSent(int index) {
            super.onSent(index);
            errorTable.setProgressDone(index);
        }

        @Override
        protected void onNotSending(int index, String serverReply) {
            super.onNotSending(index, serverReply);
            JEditorPane reply = new JEditorPane("text/html", serverReply);
            reply.setEditable(false);
            reply.setOpaque(false);
            // JOptionPane.showMessageDialog(this, reply);
            errorTable.setProgressDone(index);
        }

        @Override
        protected void onException(Exception e) {
            super.onException(e);
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

            gui.appendTextToDocument(debugDocument, debugStyle, "");

            gui.appendTextToDocument(debugDocument, debugStyle, "----\n"
                    + debugText);
            String sendErrorMsg = "Sorry, but due to an error we were not able "
                    + "to automatically \n send your debug information. \n\n"
                    + "Pleae let us know about this problem by contacting \n\n"
                    + "us at <a href='mailto:comments@openmicroscopy.org.uk'>.";
            try {
                JEditorPane popup = new JEditorPane(sendErrorMsg);
                JOptionPane.showMessageDialog(panel, popup);
                errorTable.setSendBtnEnable(true);
            } catch (IOException e1) {
            }
        }

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
        }

        @Override
        protected void finishComplete() {
            super.finishComplete();
            errorTable.setCancelBtnVisible(false);
            JOptionPane.showMessageDialog(panel,
                    "\nThank you for your support, your errors "
                            + "\nhave successfully been collected."
                            + "\n\nIf you have provided us with an email"
                            + "\naddress, you should recieve a message"
                            + "\nshortly detailing how you can track the"
                            + "\nstatus of your errors.", "Success!",
                    JOptionPane.INFORMATION_MESSAGE);
        }

        @Override
        protected void onAddError(ErrorContainer errorContainer, String message) {
            super.onAddError(errorContainer, message);
            Vector<Object> row = new Vector<Object>();
            row.add(new Boolean(true));
            row.add(errorContainer.getSelectedFile().getName());
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

    public void update(IObservable importLibrary, ImportEvent event) {
        this.delegate.update(importLibrary, event);
    }

    public boolean addObserver(IObserver object) {
        return this.delegate.addObserver(object);
    }

    public boolean deleteObserver(IObserver object) {
        return this.delegate.deleteObserver(object);
    }

    public void notifyObservers(ImportEvent event) {
        this.delegate.notifyObservers(event);
    }
}
