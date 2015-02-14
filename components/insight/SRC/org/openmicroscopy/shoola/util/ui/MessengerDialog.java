/*
 * org.openmicroscopy.shoola.util.ui.MessengerDialog 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.util.ui;


import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import org.jdesktop.swingx.JXBusyLabel;

import info.clearthought.layout.TableLayout;

import org.apache.commons.collections.CollectionUtils;
import org.openmicroscopy.shoola.util.CommonsLangUtils;

import org.openmicroscopy.shoola.util.file.ImportErrorObject;

/** 
 * A dialog used to collect and send comments or error messages.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * after code from 
 * @author Brian Loranger &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:brian.loranger@lifesci.dundee.ac.uk">
 * brian.loranger@lifesci.dundee.ac.uk</a>
 * 
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class MessengerDialog 
	extends JDialog
	implements ActionListener, DocumentListener
{

	/** Identifies the error dialog type. */
	public static final int			ERROR_TYPE = 0;
	
	/** Identifies the error dialog type. */
	public static final int			COMMENT_TYPE = 1;
	
	/** Identifies the error dialog type. */
	public static final int			SUBMIT_ERROR_TYPE = 2;
	
	/** Bound property indicating to send the message. */
	public static final String		SEND_PROPERTY = "send";

	/** Bound property indicating to close the window. */
	public static final String		CLOSE_MESSENGER_PROPERTY = "closeMessenger";
	
	/** Action ID to close the dialog. */
	private static final int		CANCEL = 0;
	
	/** Action ID to send comment and close the dialog. */
	private static final int		SEND = 1;

	/** Action ID to copy on the clipboard. */
	private static final int		COPY = 3;
	
	/** Action ID to indicate the consequence of not submitting files. */
	private static final int		SUBMIT = 4;
	
	/** The default size of the window. */
	private static final Dimension 	DEFAULT_SIZE = new Dimension(700, 400);
	
	/** The tooltip of the {@link #cancelButton}. */
	private static final String		CANCEL_TOOLTIP = "Cancel your message";
	
	/** The tooltip of the {@link #sendButton}. */
	private static final String		SEND_TOOLTIP = "Send the information to " +
										"the development team";
	
	/** The tooltip of the {@link #copyButton}. */
	private static final String		COPY_TOOLTIP = "Copy the Exception " +
									"Message to the clipboard";
	
	/** The default message displayed. */
	private static final String		MESSAGE = "Thank you for taking the time " +
			"to send us your comments. \n\n" +
			"Your feedback will be used to further the development of " +
			"OMERO and improve our software. Any personal details you " +
			"provide are purely optional, and will only be used for " +
			"development purposes.\n";
	
	/** The default message displayed. */
	private static final String		DEBUG_MESSAGE = "An error message has " +
			"been generated by the application.\n\n" +
			"To help us improve our software, please fill " +
			"out the following form. Your personal details are purely " +
			"optional, and will only be used for development purposes.\n\n" +
			"Please note that your application may need to be restarted " +
			"to work properly.\n";
	
	/** The default message displayed. */
	private static final String		SUBMIT_MESSAGE = "Submit to the " +
			"development team the files that failed to import.\n\n" +
			"To help us improve our software, please fill " +
			"out the following form. Your personal details are purely " +
			"optional, and will only be used for development purposes.\n\n" +
			"Please note that your application may need to be restarted " +
			"to work properly.\n";
	
	/** The default message displayed when a non valid e-mail is entered. */
	private static final String		EMAIL_MESSAGE = "The e-mail address " +
			"entered \n does not seem to be valid. \n Please enter a new " +
			"e-mail address.";
	
	/** 
	 * The default message displayed if user decides not to submit the files. 
	 */
	private static final String		SUBMIT_FILES_MESSAGE = "Choosing not " +
			"to submit to the files will make it more difficult to " +
			"fix the problem you are experimenting.\nAre you sure " +
			"you do not want to submit the files?";
	
	/** Value of the  comment field. */
	private static final String		COMMENT_FIELD = "Comment: ";
	
	/** Value of the comment field when an exception is specified. */
	private static final String		DEBUG_COMMENT_FIELD ="What you were doing" +
														" when you crashed?";
	
	/** Value of the field. */
	private static final String		EMAIL_FIELD = "Email: ";
	
	/** The default tool-tip of the e-mail area. */
	private static final String 	EMAIL_TOOLTIP = "Enter your email " +
												"address here.";
	
	/** The e-mail field's suffix. */
	private static final String		EMAIL_SUFFIX = " (Optional)";

	/** Brief description of the error. */
	private static final String		ERROR_BRIEF = "Brief Description:";
	
	/** 
	 * One of the following constants: {@link #ERROR_TYPE} or 
	 * {@link #COMMENT_TYPE}.
	 */
	private int				dialogType;
	
	/** Button to close and dispose of the window. */
	private JButton 		cancelButton;
	
	/** Button to post the message. */
	private JButton			sendButton;

	/** The area displaying the <code>e-mail address</code>. */
	private JTextField		emailArea;
	
	/** The comment Area. */
	private MultilineLabel	commentArea;

	/** The e-mail address of the user submitting the message. */
	private String			emailAddress;
	
	/** The exception to handle, <code>null</code> if no exception. */
	private Exception		exception;
	
	/** The text pane displaying the error message. */
	private JTextPane		debugArea;
	
	/** Button to copy the message on the clipBoard. */
	private JButton			copyButton;
	
	/** The version of the server. */
	private String			serverVersion;
	
	/** A brief description of the error. */
	private String			errorDescription;
	
	/** The component displaying the files to send. */
	private FileTable		table;
	
	/** Indicates the status of the files submission. */
	private JXBusyLabel		submitStatus;
	
	/** Component indicating to submit the files or not. */
	private JCheckBox		submitFile;
	
	/** Indicates the progress of the files submission.*/
	private JXBusyLabel		progress;
	
	/** Indicates the progress of the files submission.*/
	private JLabel			progressLabel;

	/** 
	 * Displays the dialog indicating the consequence of not submitting
	 * the files.
	 */
	private void submitFilesControl()
	{
		if (!submitFile.isSelected()) {
			MessageBox dialog = new MessageBox(this, "Submit Files", 
					SUBMIT_FILES_MESSAGE);
			dialog.setResizable(false);
			if (dialog.centerMsgBox() == MessageBox.NO_OPTION)
				submitFile.setSelected(true);
		}
	}
	
	/**
	 * Formats the specified button.
	 * 
	 * @param b			The button to format.
	 * @param mnemonic	The key-code that indicates a mnemonic key.
	 * @param tooltip	The button's tooltip.
	 * @param actionID	The action id associated to the passed button.
	 */
	private void formatButton(JButton b, int mnemonic, String tooltip, int
			actionID)
	{
		b.setMnemonic(mnemonic);
        b.setOpaque(false);
        b.setToolTipText(tooltip);
        b.addActionListener(this);
        b.setActionCommand(""+actionID);
	}
    
    /** Hides the window and disposes. */
	private void close()
	{
		setVisible(false);
		dispose();
		firePropertyChange(CLOSE_MESSENGER_PROPERTY, Boolean.valueOf(false), 
				 Boolean.valueOf(true));
	}
	
	/** Copies the error message on the clipboard. */
	private void copy()
	{
		if (debugArea != null) {
			debugArea.selectAll();
			debugArea.copy();
		}
	}
	
	/**
	 * Sends the error to the server.
	 * 
	 * @param propertyName The name of the property.
	 */
	private void sendError(String propertyName)
	{
		String email = emailArea.getText().trim();
		String comment = commentArea.getText().trim();
		String error = null;
		if (debugArea != null)  error = debugArea.getText().trim();
		MessengerDetails details = new MessengerDetails(email, comment);
		details.setExtra(serverVersion);
		details.setError(error);
		firePropertyChange(propertyName, null, details);
		close();
	}
	
	/** 
	 * Sends the message. 
	 * 
	 * @param propertyName The name of the property to fire.
	 */
	private void send(String propertyName)
	{
		if (dialogType == SUBMIT_ERROR_TYPE) {
			List<FileTableNode> files = null;
			if (table != null) files = table.getSelectedFiles();
			if (CollectionUtils.isEmpty(files)) {
				sendError(propertyName);
			} else {
				String email = emailArea.getText().trim();
				String comment = commentArea.getText().trim();
				MessengerDetails details = new MessengerDetails(email, comment);
				details.setExtra(serverVersion);
				details.setObjectToSubmit(files);
				submitStatus.setVisible(true);
				submitStatus.setBusy(true);
				details.setExceptionOnly(!submitFile.isSelected());
				firePropertyChange(propertyName, null, details);
			}
		} else {
			sendError(propertyName);
		}
		sendButton.setEnabled(false);
	}
	
	/** Initializes the various components. */
	private void initComponents()
	{
		progress = new JXBusyLabel(new Dimension(16, 16));
		progress.setVisible(false);
		progressLabel = new JLabel();
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) { close(); }
		});
		cancelButton = new JButton("Cancel");
		formatButton(cancelButton, 'C', CANCEL_TOOLTIP, CANCEL);
		
		sendButton = new JButton("Send");
		formatButton(sendButton, 'S', SEND_TOOLTIP, SEND);
		
        emailArea = new JTextField(20);
        emailArea.setToolTipText(EMAIL_TOOLTIP);
        emailArea.setText(emailAddress);
        commentArea = new MultilineLabel();
        commentArea.setEditable(true);
        commentArea.setBackground(UIUtilities.BACKGROUND_COLOR);
        commentArea.setOpaque(true);
        if (exception != null) {
        	debugArea = buildExceptionArea();
        	copyButton = new JButton("Copy to Clipboard");
        	formatButton(copyButton, 'C', COPY_TOOLTIP, COPY);
        }
        setAlwaysOnTop(true);
        if (dialogType == COMMENT_TYPE) {
			sendButton.setEnabled(false);
			commentArea.getDocument().addDocumentListener(this);
		}
        submitStatus = new JXBusyLabel(new Dimension(16, 16));
        submitStatus.setText("Uploading files");
        submitStatus.setVisible(false);
        submitFile = new JCheckBox("Files");
        submitFile.setSelected(true);
        submitFile.addActionListener(this);
        submitFile.setActionCommand(""+SUBMIT);
	}

	/**
	 * Builds the UI component displaying the exception.
	 * 
	 * @return See above.
	 */
	private JTextPane buildExceptionArea()
	{
		JTextPane pane = UIUtilities.buildExceptionArea();
		StyledDocument document = pane.getStyledDocument();
		Style style = pane.getLogicalStyle();
		//Get the full debug text
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        try {
        	document.insertString(document.getLength(), sw.toString(), style);
        } catch (BadLocationException e) {}
        
        return pane;
	}
	
	/**
	 * Builds and lays out the panel hosting the <code>comment</code> details.
	 * 
	 * @param comment		The comment's text.
	 * @param mnemonic 		The key-code that indicates a mnemonic key.
	 * @return See above.
	 */
	private JPanel buildCommentAreaPanel(String comment, int mnemonic)
	{
		JPanel panel = new JPanel();
        panel.setOpaque(false);
        
        double size[][] = {{TableLayout.FILL}, {20, TableLayout.FILL}};
        TableLayout layout = new TableLayout(size);
        panel.setLayout(layout);
        JScrollPane areaScrollPane = new JScrollPane(commentArea);
        areaScrollPane.setVerticalScrollBarPolicy(
        		JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        commentArea.setPreferredSize(new Dimension(300,100));
        JLabel label = new JLabel(comment);
        label.setOpaque(false);
        label.setDisplayedMnemonic(mnemonic);
        panel.add(label, "0, 0, LEFT, CENTER");
        panel.add(areaScrollPane, "0, 1");
        return panel;
	}
	
	/**
	 * Builds and lays out the panel hosting the <code>email</code> details.
	 * 
	 * @param mnemonic The key-code that indicates a mnemonic key.
	 * @return See above.
	 */
	private JPanel buildEmailAreaPanel(int mnemonic)
	{
		double[][] size = null;
        
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        
        if (EMAIL_SUFFIX.length() == 0)
            size = new double[][]{{TableLayout.PREFERRED, TableLayout.FILL}, 
        							{30}};
        else
            size = new double[][] 
                   {{TableLayout.PREFERRED,TableLayout.FILL, 
                	   TableLayout.PREFERRED}, {30}};
     
        TableLayout layout = new TableLayout(size);
        panel.setLayout(layout);       

        JLabel label = new JLabel(EMAIL_FIELD);
        label.setDisplayedMnemonic(mnemonic);
        label.setLabelFor(emailArea);
        label.setOpaque(false);

        panel.add(label, "0, 0, RIGHT, CENTER");
        panel.add(emailArea, "1, 0, FULL, CENTER");

        if (EMAIL_SUFFIX.length() != 0)
            panel.add(new JLabel(EMAIL_SUFFIX), "2, 0, LEFT, CENTER");

		return panel;
	}
	
	/**
	 * Builds and lays out the panel hosting the debug information.
	 * 
	 * @return See above.
	 */
	private JPanel buildDebugPane()
	{
		JPanel panel = new JPanel();
        panel.setOpaque(false);
        double tableSize[][] = {{TableLayout.FILL}, // columns
        						{TableLayout.FILL, 32}}; // rows
        TableLayout layout = new TableLayout(tableSize);
        panel.setLayout(layout);       
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JScrollPane pane = new JScrollPane(debugArea);
        panel.add(pane, "0, 0");
        panel.add(copyButton, "0, 1, CENTER, BOTTOM");
        return panel;
	}
	
	/**
	 * Builds and lays out the panel hosting the collection of files to submit.
	 * 
	 * @return See above.
	 */
	private JPanel buildFilesToSubmitPane(List<ImportErrorObject> toSubmit)
	{
		JPanel panel = new JPanel();
        panel.setOpaque(false);
        double tableSize[][] = {{TableLayout.FILL}, // columns
        						{TableLayout.FILL}}; // rows
        TableLayout layout = new TableLayout(tableSize);
        panel.setLayout(layout);       
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        table = new FileTable(toSubmit);
        JScrollPane pane = new JScrollPane(table);
        panel.add(pane, "0, 0");
        return panel;
	}
	
	/**
	 * Builds and lays out the panel hosting the comments.
	 * 
	 * @param comment		The comment's text.
	 * @return See above.
	 */
	private JPanel buildCommentPane(String comment)
	{
		JPanel commentPanel = new JPanel();
        int iconSpace = 0;
        double tableSize[][] =  {{iconSpace, (160 - iconSpace), 
        						TableLayout.FILL}, // columns
        						{0, 0, 30, TableLayout.FILL}}; // rows
	    TableLayout layout = new TableLayout(tableSize);
	    commentPanel.setLayout(layout);  
	    commentPanel.setBorder(
	    				BorderFactory.createEmptyBorder(10, 10, 10, 10));
	    commentPanel.add(buildEmailAreaPanel('E'), "0, 2, 2, 2");
	    commentPanel.add(buildCommentAreaPanel(comment, 'W'), "0, 3, 2, 3");
	    if (CommonsLangUtils.isNotBlank(errorDescription)) {
	    	layout.setRow(1, 30);
	    	JPanel p = new JPanel();
	    	p.add(UIUtilities.setTextFont(ERROR_BRIEF));
	    	p.add(new JLabel(errorDescription));
	    	commentPanel.add(UIUtilities.buildComponentPanel(p), "0, 1, 2, 1");
	    }
		return commentPanel;
	}
	
	/** 
	 * Builds the UI component hosting the debug information.
	 * 
	 * @param toSubmit The collection of files to send.
	 * @return See above
	 */
	private JTabbedPane buildExceptionPane(List<ImportErrorObject> toSubmit)
	{
        JTabbedPane tPane = new JTabbedPane();
        tPane.setOpaque(false);
        if (dialogType == SUBMIT_ERROR_TYPE) {
        	tPane.addTab("Comments", null, buildCommentPane(COMMENT_FIELD), 
        		"Your comments go here.");
        	tPane.addTab("Files to Send", null, 
        			buildFilesToSubmitPane(toSubmit),
        	"The files to send to the development team.");
        } else {
        	tPane.addTab("Comments", null, 
        			buildCommentPane(DEBUG_COMMENT_FIELD), 
        		"Your comments go here.");
        	tPane.addTab("Error Message", null, buildDebugPane(),
        	"The Exception Message.");
        }
		return tPane;
	}
	
    /**
     * Builds and lays out the buttons.
     * 
     * @param submit Collection of files to submit.
     * @return See above.
     */
    private JPanel buildToolBar(List<ImportErrorObject> toSubmit)
    {
    	JPanel bars = new JPanel();
    	bars.setLayout(new BoxLayout(bars, BoxLayout.X_AXIS));
    	if (CollectionUtils.isNotEmpty(toSubmit)) {
    	    boolean count = false;
    	    Iterator<ImportErrorObject> j = toSubmit.iterator();
    	    while (j.hasNext()) {
                if (j.next().getFile() != null) {
                    count = true;
                    break;
                }
            }
    		JPanel row = new JPanel();
    		row.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
    		if (count) {
    		    row.add(new JLabel("Submit Exceptions and: "));
                row.add(UIUtilities.buildComponentPanel(submitFile));
    		}
    		
    		JPanel p = new JPanel();
    		p.setBorder(null);
    		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
    		p.add(row);
    		JPanel progressPane = new JPanel();
    		progressPane.setLayout(new BoxLayout(progressPane,
    				BoxLayout.X_AXIS));
    		progressPane.add(progress);
    		progressPane.add(Box.createHorizontalStrut(5));
    		progressPane.add(progressLabel);
    		p.add(UIUtilities.buildComponentPanel(progressPane));
    		bars.add(UIUtilities.buildComponentPanel(p));
    	}
    	JPanel bar = new JPanel();
    	bar.setLayout(new BoxLayout(bar, BoxLayout.X_AXIS));
    	bar.add(cancelButton);
    	bar.add(Box.createHorizontalStrut(5));
    	bar.add(sendButton);
    	bar.add(Box.createHorizontalStrut(10));
    	bars.add(UIUtilities.buildComponentPanelRight(bar));
    	return bars;
    }
    
	/** 
	 * Builds and lays out the GUI. 
	 * 
	 * @param toSubmit The collection of files to send.
	 */
	private void buildGUI(List<ImportErrorObject> toSubmit)
	{
        JComponent component;
        Icon icon;
        IconManager icons = IconManager.getInstance();
        String message;
        if (dialogType == SUBMIT_ERROR_TYPE) {
        	message = SUBMIT_MESSAGE;
        	component = buildExceptionPane(toSubmit);
        	icon = icons.getIcon(IconManager.SUBMIT_ICON_64);
            if (icon == null) icon = UIManager.getIcon("OptionPane.errorIcon");
        } else if (exception == null) {
        	message = MESSAGE;
            icon = icons.getIcon(IconManager.COMMENT_ICON_64);
            if (icon == null)
            	icon = UIManager.getIcon("OptionPane.questionIcon");
        	component = buildCommentPane(COMMENT_FIELD);
        } else {
        	message = DEBUG_MESSAGE;
        	component = buildExceptionPane(null);
        	icon = icons.getIcon(IconManager.ERROR_ICON_64);
            if (icon == null) icon = UIManager.getIcon("OptionPane.errorIcon");
        }
        Container c = getContentPane();
        TitlePanel tp = new TitlePanel(getTitle(), message, icon);
        c.setLayout(new BorderLayout(0, 0));
        c.add(tp, BorderLayout.NORTH);
		c.add(component, BorderLayout.CENTER);
		c.add(buildToolBar(toSubmit),BorderLayout.SOUTH);
	}
	
	/** 
	 * Initializes the dialog.
	 * 
	 *  @param title 	The title of the dialog.
	 *  @param toSubmit The collection of files to send.
	 */
	private void initialize(String title, List<ImportErrorObject> toSubmit)
	{
		setTitle(title);
		initComponents();
		buildGUI(toSubmit);
		setSize(DEFAULT_SIZE);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param parent		The parent of this dialog.
	 * @param title			The dialog's title.
	 * @param emailAddress	The e-mail address of the current user.
	 */
	public MessengerDialog(JFrame parent, String title, String emailAddress)
	{
		super(parent);
		this.emailAddress = emailAddress;
		dialogType = COMMENT_TYPE;
		initialize(title, null);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param parent		The parent of this dialog.
	 * @param title			The dialog's title.
	 * @param emailAddress	The e-mail address of the current user.
	 * @param exception		The exception to handle.
	 */
	public MessengerDialog(JFrame parent, String title, String emailAddress, 
						Exception exception)
	{
		super(parent);
		dialogType = ERROR_TYPE;
		this.emailAddress = emailAddress;
		this.exception = exception;
		initialize(title, null);
	}	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param parent		The parent of this dialog.
	 * @param title			The dialog's title.
	 * @param emailAddress	The e-mail address of the current user.
	 * @param toSubmit		The object to submit.
	 */
	public MessengerDialog(JFrame parent, String title, String emailAddress, 
						List<ImportErrorObject> toSubmit)
	{
		super(parent);
		this.dialogType = SUBMIT_ERROR_TYPE;
		this.emailAddress = emailAddress;
		initialize(title, toSubmit);
	}	
	
	/**
	 * Sets a brief description of the error.
	 * 
	 * @param description The value to set.
	 */
	public void setErrorDescription(String description)
	{
		errorDescription = description;
	}
	
	/**
	 * Sets the comment to send.
	 * 
	 * @param comment The text to display.
	 */
	public void setComment(String comment) 
	{
		if (comment != null && comment.trim().length() > 0)
			commentArea.setText(comment);
	}
	
	/** 
	 * Sets the version of the server.
	 * 
	 * @param serverVersion The value to set.
	 */
	public void setServerVersion(String serverVersion)
	{ 
		this.serverVersion = serverVersion; 
	}
	
	/**
	 * Returns the type associated to this widget. 
	 * 
	 * @return See above.
	 */
	public int getDialogType() { return dialogType; }

	/**
	 * Sets the status of the file submission.
	 * 
	 * @param text The text to display.
	 * @param hide Pass <code>true</code> to hide the progress,
	 * 				<code>false</code> otherwise.
	 */
	public void setSubmitStatus(String text, boolean hide)
	{
		progressLabel.setText(text);
		progress.setVisible(!hide);
		progress.setBusy(!hide);
	}
	
	/**
	 * Reacts to click on controls.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		switch (index) {
			case CANCEL:
				close();
				break;
			case SEND:
				send(SEND_PROPERTY);
				break;
			case COPY:
				copy();
				break;
			case SUBMIT:
				submitFilesControl();
		}
	}
	
	/**
	 * Sets the enabled flag of the {@link #sendButton} depending on the 
	 * type of dialog we handle.
	 * @see DocumentListener#insertUpdate(DocumentEvent)
	 */
	public void insertUpdate(DocumentEvent e)
	{
		if (dialogType == COMMENT_TYPE) {
			String text = commentArea.getText();
			sendButton.setEnabled(text != null && text.trim().length() > 0);
		}
	}

	/**
	 * Sets the enabled flag of the {@link #sendButton} depending on the 
	 * type of dialog we handle.
	 * @see DocumentListener#removeUpdate(DocumentEvent)
	 */
	public void removeUpdate(DocumentEvent e)
	{
		if (dialogType == COMMENT_TYPE) {
			String text = commentArea.getText();
			sendButton.setEnabled(text != null && text.trim().length() > 0);
		}
	}
	
	/**
	 * Required by the {@link DocumentListener} I/F but no-op implementation
	 * in our case.
	 * @see DocumentListener#changedUpdate(DocumentEvent)
	 */
	public void changedUpdate(DocumentEvent e) {}
	
	@Override
	public void setVisible(boolean b) {
		super.setVisible(b);
		pack();
	}
}
