package ome.formats.importer;

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

import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ome.formats.importer.util.Actions;
import ome.formats.importer.util.ErrorContainer;
import ome.formats.importer.util.FileUploader;
import ome.formats.importer.util.GuiCommonElements;
import ome.formats.importer.util.HtmlMessenger;
import ome.formats.importer.util.IniFileLoader;

@SuppressWarnings("serial")
public class ErrorHandler 
    extends JPanel implements IObserver, IObservable
{
	/** Logger for this class */
	private static Log log = LogFactory.getLog(ErrorHandler.class);
	
    ArrayList<IObserver> observers = new ArrayList<IObserver>();
    
    private ErrorTable    errorTable;
    private ArrayList<ErrorContainer> errors = new ArrayList<ErrorContainer>();
    

    IniFileLoader ini = IniFileLoader.getIniFileLoader();
    
    String url = ini.getBugTrackerURL();   
    String tokenUrl = ini.getUploaderTokenURL(); 
    String uploaderUrl = ini.getUploaderURL();
    
    GuiCommonElements gui = new GuiCommonElements();
    JTextPane               debugTextPane;
    StyledDocument          debugDocument;
    Style                   debugStyle;
    
    DebugMessenger debugMessenger;
    
    private Thread runThread;

	private int totalErrors;

	private FileUploader fileUploader;

	private boolean cancelUploads = false;

	private boolean sendFiles = true;
    
    ErrorHandler()
    {
        this.setOpaque(false);
        setLayout(new BorderLayout());
        
        errorTable = ErrorTable.getErrorTable();
        
        if (errorTable != null)
            add(errorTable, BorderLayout.CENTER);
        
        errorTable.addObserver(this);
    }
    
    
    /**
     * Creates a singularity of the error Handler
     * @param viewer
     * @return
     */
    public static synchronized ErrorHandler getErrorHandler()
    {
        if (ref == null) 
            ref = new ErrorHandler();
        return ref;
    }
    
    private static ErrorHandler ref;
    
    
    /**
     * @param args
     */
    public static void main(String[] args)
    {   
        ErrorHandler eh = new ErrorHandler(); 
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


	public void update(IObservable importLibrary, Object message, Object[] args) 
	{
		if (message == Actions.ERRORS_SEND)
		{
			cancelUploads = false;
			errorTable.enableCancelBtn(true);
			debugMessenger = new DebugMessenger(null, "OMERO.importer Error Dialog", false, errors);
			debugMessenger.addObserver(this);	
			debugMessenger.setAlwaysOnTop(true);
		}
		
		if (message == Actions.DEBUG_SEND)
		{

			sendFiles = ((Boolean)args[0]);
			
            runThread = new Thread()
            {
            	public void run()
            	{
            		try
            		{
            			sendErrors();
            		}
            		catch (Throwable error)
            		{ 
            		    error.printStackTrace();
            		}
            	}
            };
            runThread.start();
		}
		
		if (message == Actions.FILE_UPLOAD_STARTED)
		{
			errorTable.setFilesInSet((Integer)args[2]);
		}
		
		if (message == Actions.FILE_UPLOAD_BYTES)
		{
			errorTable.setBytesFileSize(((Long) args[4]).intValue());
			errorTable.setBytesProgress(((Long) args[3]).intValue());	
			errorTable.setFilesProgress((Integer) args[1] - 1);
		}
		
		if (message == Actions.FILE_UPLOAD_COMPLETE)
		{
			errorTable.setFilesProgress((Integer) args[1]);
		}
		
		if (message == Actions.ERRORS_UPLOAD_CANCELLED)
		{
			cancelUploads = true;
		}
	}
	
	private void sendErrors()
	{		
		errorTable.enableSendBtn(false);

		for (int i = 0; i < errors.size(); i++)
		{
			if (cancelUploads ) 
			{
				errorTable.enableSendBtn(true);
				break;
			}
			ErrorContainer errorContainer = errors.get(i);
			if (errorContainer.getStatus() != -1) // if file not pending, skip it
				continue;

			List<Part> postList = new ArrayList<Part>();

			postList.add(new StringPart("java_version", errorContainer.getJavaVersion()));
			postList.add(new StringPart("java_classpath", errorContainer.getJavaClasspath()));
			postList.add(new StringPart("app_version", errorContainer.getAppVersion()));
			postList.add(new StringPart("comment_type", errorContainer.getCommentType()));
			postList.add(new StringPart("os_name", errorContainer.getOSName()));
			postList.add(new StringPart("os_arch", errorContainer.getOSArch()));
			postList.add(new StringPart("os_version", errorContainer.getOSVersion()));
			postList.add(new StringPart("extra", errorContainer.getExtra()));
			postList.add(new StringPart("error", errorContainer.getError().toString()));
			postList.add(new StringPart("comment", errorContainer.getComment()));
			postList.add(new StringPart("email", errorContainer.getEmail()));
			postList.add(new StringPart("app_name", "2"));
			postList.add(new StringPart("import_session", "test"));
			postList.add(new StringPart("absolute_path", "blarg"));

			String sendUrl = tokenUrl;
			
			boolean send = (Boolean)errorTable.table.getValueAt(i, 0);
			//System.err.println(send);
			
			if (errorContainer.getSelectedFile() != null && sendFiles && send)
			{
				postList.add(new StringPart("selected_file", errorContainer.getSelectedFile().getName()));
				postList.add(new StringPart("absolute_path", errorContainer.getAbsolutePath()));

				if (errorContainer.getFiles().length > 1)
				{
    				for (String f : errorContainer.getFiles())
    				{
    					File file = new File(f);          
    					postList.add(new StringPart("additional_files", file.getName()));
    					postList.add(new StringPart("additional_files_size", ((Long)file.length()).toString()));
    					if (file.getParent() != null)
    					    postList.add(new StringPart("additional_files_path", file.getParent()));
    				}
				}
			}

			try 
			{
				HtmlMessenger messenger = new HtmlMessenger(sendUrl, postList);
				String serverReply = messenger.executePost();

				if (errorContainer.getSelectedFile() != null  && sendFiles  && send)
				{
					errorTable.setProgressSending(errorContainer.getIndex());
					errorContainer.setToken(serverReply);
					System.err.println(serverReply);	

					fileUploader = new FileUploader(messenger.getHttpClient());
					fileUploader.addObserver(this);

					fileUploader.uploadFiles(uploaderUrl, 2000, errorContainer);
					errorTable.setProgressDone(errorContainer.getIndex());
				} else 
				{
					JEditorPane reply = new JEditorPane("text/html", serverReply);
					reply.setEditable(false);
					reply.setOpaque(false);
					//JOptionPane.showMessageDialog(this, reply);
					errorTable.setProgressDone(errorContainer.getIndex());
				}
			}
			catch( Exception e ) 
			{
				log.error("Error while sending error information.", e);
				//Get the full debug text
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

				gui.appendTextToDocument(debugDocument, debugStyle, "----\n"+debugText);
				String sendErrorMsg = "Sorry, but due to an error we were not able " +
				"to automatically \n send your debug information. \n\n" +
				"Pleae let us know about this problem by contacting \n\n" +
				"us at <a href='mailto:comments@openmicroscopy.org.uk'>.";
				try
				{
					JEditorPane popup = new JEditorPane(sendErrorMsg);
					JOptionPane.showMessageDialog(this, popup);
					errorTable.setSendBtnEnable(true);
				} catch (IOException e1){}
			}

		}
		if (cancelUploads)
		{
			errorTable.setCancelBtnCancelled();
			JOptionPane.showMessageDialog(
					this,
					"\nThank you for your support!" +
					"\n\nYou have cancelled uploading your data" +
					"\nfiles to us, however some files may have" +
					"\nbeen sent (as shown on the error list)." +
					"\n\nIf you wish to continue uploading your" +
					"\nfiles, simply click the 'Send Feedback'" +
					"\nbutton again.", 
					"Cancelled Upload!", 
					JOptionPane.INFORMATION_MESSAGE);	    
		}
		else 
		{
		    errorTable.setCancelBtnVisible(false);
			JOptionPane.showMessageDialog(
					this,
					"\nThank you for your support, your errors " +
					"\nhave successfully been collected." +
					"\n\nIf you have provided us with an email" +
					"\naddress, you should recieve a message" +
					"\nshortly detailing how you can track the" +
					"\nstatus of your errors.", 
					"Success!", 
					JOptionPane.INFORMATION_MESSAGE);
			notifyObservers(Actions.ERRORS_COMPLETE, null);
		}
	}

	public void addError(ErrorContainer errorContainer) {

		String errorMessage = errorContainer.getError().toString();
	    String[] splitMessage = errorMessage.split("\n");
	    
		errorMessage = errorMessage.replaceAll("\n", "<br>&nbsp;&nbsp;");
		String htmlMessage = "<html><head></head><body><table width='100'><tr><td>" 
		    + errorMessage + "</td></tr></table></body></html>";
		
		errorContainer.setIndex(totalErrors);
		totalErrors = totalErrors + 1;
		errorContainer.setStatus(-1); //pending status
		
		errors.add(errorContainer);
		Vector<Object> row = new Vector<Object>();
        row.add(new Boolean(true));
        row.add(errorContainer.getSelectedFile().getName());
        row.add(splitMessage[0]);
        row.add(-1);
        row.add(null);
        row.add(null);
        row.add(null); //full error for tooltip
        errorTable.addRow(row);
        errorTable.fireTableDataChanged();
		notifyObservers(Actions.ERRORS_PENDING, null);
	}

    // Observable methods    
    public boolean addObserver(IObserver object)
    {
        return observers.add(object);
    }
    
    public boolean deleteObserver(IObserver object)
    {
        return observers.remove(object);
        
    }

    public void notifyObservers(Object message, Object[] args)
    {
        for (IObserver observer:observers)
        {
            observer.update(this, message, args);
        }
    }
}
