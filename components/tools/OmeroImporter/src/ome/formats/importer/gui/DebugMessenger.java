/*
 * ome.formats.importer.gui.DebugMessenger
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *------------------------------------------------------------------------------
 */
package ome.formats.importer.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import layout.TableLayout;
import ome.formats.importer.IObservable;
import ome.formats.importer.IObserver;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.ImportEvent;
import ome.formats.importer.util.ErrorContainer;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.FileUtils;

/**
 * @author TheBrain
 *
 */
public class DebugMessenger extends JDialog implements ActionListener, IObservable, IObserver
{
    private static final long serialVersionUID = -1026712513033611084L;

    private final ImportConfig config;
    
    boolean debug = false;
    
    private static final String ICON = "gfx/nuvola_mail_send64.png";
    
    ArrayList<IObserver> observers = new ArrayList<IObserver>();
    
    GuiCommonElements       gui;
    
    JFrame                  owner;
    
    JPanel                  mainPanel;
    JPanel                  commentPanel;
    JPanel                  debugPanel;

    JButton                 quitBtn;
    JButton                 cancelBtn;
    JButton                 sendBtn;
    JButton                 sendWithFilesBtn;
    JButton                 ignoreBtn;
    JButton                 copyBtn;
    
    JTextField              emailTextField;
    String                  emailText           = "";          
    
    JTextArea               commentTextArea;
    String                  commentText         = "";
    
    JTextPane               debugTextPane;
    StyledDocument          debugDocument;
    Style                   debugStyle;

	private ArrayList<ErrorContainer> errorsArrayList;

	private JCheckBox uploadCheckmark, logUploadCheckmark;
	
    private long total_files, total_file_length;
	
	private FileSizeChecker checker;

    private String file_info; 
    
    DebugMessenger(JFrame owner, String title, ImportConfig config, Boolean modal, ArrayList<ErrorContainer> errorsArrayList)
    {
        super(owner);
        this.config = config;
        this.gui = new GuiCommonElements(config);
        this.owner = owner;
        this.errorsArrayList = errorsArrayList;
        
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        setTitle(title);
        setModal(modal);
        setResizable(true);
        setSize(new Dimension(680, 400));
        setLocationRelativeTo(owner);
        
        file_info = "(Calculating file info)";
        
        // Set up the main panel for tPane, quit, and send buttons
        double mainTable[][] =
                {{10, 150, TableLayout.FILL, 100, 5, 150, 5, 150, 10}, // columns
                {TableLayout.FILL, 20, 40}}; // rows
        
        mainPanel = gui.addMainPanel(this, mainTable, 10, 10, 10, 10, debug);

        cancelBtn = gui.addButton(mainPanel, "Cancel", 'C',
                "Cancel your message", "5, 2, f, c", debug);
        cancelBtn.addActionListener(this);

        sendBtn = gui.addButton(mainPanel, "Send Comment", 'S',
                "Send your comment to the development team", "7, 2, f, c", debug);
        sendBtn.addActionListener(this);

        this.getRootPane().setDefaultButton(sendBtn);
        gui.enterPressesWhenFocused(sendBtn);
        
        uploadCheckmark = gui.addCheckBox(mainPanel, "Send the image files for these errors. " + file_info, "1,1,7,c", debug);
        //uploadCheckmark.setSelected(config.sendFiles.get());
        uploadCheckmark.setSelected(true);
       
        logUploadCheckmark = gui.addCheckBox(mainPanel, "Send importer log file.", "1,2,7,c", debug);
        //uploadCheckmark.setSelected(config.sendFiles.get());
        logUploadCheckmark.setSelected(true);
        
        // fill out the comments panel (changes according to icon existance)        
        Icon icon = gui.getImageIcon(ICON);
        
        int iconSpace = 0;
        if (icon != null) iconSpace = icon.getIconWidth() + 10;
        
        double commentTable[][] = 
        {{iconSpace, (160 - iconSpace), TableLayout.FILL}, // columns
                {100, 30, TableLayout.FILL, 110}}; // rows
        
        commentPanel = gui.addMainPanel(this, commentTable, 10, 10, 10, 10, debug);

        String message = "To help us improve our software, please fill " +
        "out the following form. \n\nPlease note that providing your email " +
        "address is optional, however doing so will make it easier for us " +
        "to contact you for addition information about your errors, and for " +
        "you to track their status.";

        JLabel iconLabel = new JLabel(icon);
        commentPanel.add(iconLabel, "0,0, l, c");
        
        @SuppressWarnings("unused")
        JTextPane instructions = 
                gui.addTextPane(commentPanel, message, "1,0,2,0", debug);

        emailTextField = gui.addTextField(commentPanel, "Email: ", emailText, 'E',
        "Input your email address here.", "(Optional)", TableLayout.PREFERRED, "0, 1, 2, 1", debug);
        
        emailTextField.setText(config.email.get());
        
        commentTextArea = gui.addTextArea(commentPanel, "Please provide any additional information of importance.", 
                "", 'W', "0, 2, 2, 3", debug);
        
        // Add the tab panel to the main panel
        mainPanel.add(commentPanel, "0, 0, 8, 0");
        
        add(mainPanel, BorderLayout.CENTER);
        
        setVisible(true); 
        
        checker = new FileSizeChecker(errorsArrayList);
        checker.addObserver(this);
        checker.run();
        
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                checker.doStop();
            }
        });
       
    }


    public void actionPerformed(ActionEvent e)
    {
        Object source = e.getSource();
        
        
        if (source == quitBtn)
        {
            if (gui.quitConfirmed(this, "Abandon your import and quit the application?") == true)
            {
                System.exit(0);
            }
        }
        
        
        if (source == cancelBtn)
        {
            dispose();
        }
        
        if (source == sendBtn)
        {           
            emailText = emailTextField.getText();
            commentText = commentTextArea.getText();
            
            if (validEmail(emailText) || emailText.trim().length() == 0)
            {
                sendBtn.setEnabled(false);
                config.email.set(emailText);
                config.sendFiles.set(uploadCheckmark.isSelected());
                config.sendLogFile.set(logUploadCheckmark.isSelected());
                sendRequest(emailText, commentText, "");
                dispose();
            }
            else
            {
                JOptionPane.showMessageDialog(this, 
                        "Your email address must be valid\n" +
                        "(or blank) to send feedback.");   
            }
        }
        
        if (source == ignoreBtn)
        {
            dispose();
        }
        
        if (source == copyBtn)
        {
            debugTextPane.selectAll();
            debugTextPane.copy();
        }
    }
    
    private void sendRequest(String emailText, String commentText, String extraText)
    {
        
    	for (ErrorContainer errorContainer : errorsArrayList)
    	{
            errorContainer.setEmail(emailText);
            errorContainer.setComment(commentText);
            errorContainer.setExtra(extraText);
    	}

    	notifyObservers(new ImportEvent.DEBUG_SEND(uploadCheckmark.isSelected(), logUploadCheckmark.isSelected()));
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

    public void notifyObservers(ImportEvent event)
    {
        for (IObserver observer:observers)
        {
            observer.update(this, event);
        }
    }
    
    
    public void update(IObservable importLibrary, ImportEvent event)
    {
        if (event instanceof ImportEvent.FILE_SIZE_STEP)
        {
            ImportEvent.FILE_SIZE_STEP ev = (ImportEvent.FILE_SIZE_STEP) event;
            
            total_files = ev.total_files;
            total_file_length = ev.total_files_length;
                    
            file_info = "Total files: " + total_files + " ("+ FileUtils.byteCountToDisplaySize(total_file_length) + ")";
            if (ev.total_files > 100 || ev.total_files_length >= 104857600) 
                file_info = "<html>Send the image files for these errors. <font color='AA0000'>" + file_info + "</font></html>";
            else
                file_info = "Send the image files for these errors. " + file_info;
           
            System.out.println(file_info);
            uploadCheckmark.setText(file_info);
            uploadCheckmark.repaint();
        }
    }
    
    // Validate the basic construct for the user's email
    public boolean validEmail(String emailAddress)
    {
        String[] parts = emailAddress.split("@");
        if (parts.length == 2 && parts[0].length() != 0 && parts[1].length() != 0)
            return true;
        else
            return false;
    }
    
    /**
     * @param args
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception
    {       
        try {
                HttpClient client = new HttpClient();
                PostMethod method = new PostMethod( "blarg" );
                client.executeMethod( method );
        }
        catch (Exception e)
        {
            new DebugMessenger(null, "Error Dialog Test", new ImportConfig(), true, null);
        }
    }
}

class FileSizeChecker implements Runnable, IObservable {

    private boolean stop = false;
    private long total_files = 0;
    private long total_file_length = 0;
    
    ArrayList<ErrorContainer> errorsArrayList;
    
    ArrayList<IObserver> observers = new ArrayList<IObserver>();
    
    FileSizeChecker(ArrayList<ErrorContainer> errorsArrayList)
    {
        this.errorsArrayList = errorsArrayList;
    }
    
    public synchronized void doStop(){
        stop = true;
    }
    
    public void run()
    {

        File f;
        int count = 0;

        for (ErrorContainer e: errorsArrayList)
        { 
            count++; 
            total_files += e.getFiles().length;

            if (stop) return;

            for (String path : e.getFiles())
            {
                if (stop) return;

                f = new File(path);
                if(f.exists())
                {
                    total_file_length += f.length();
                }
            }

            if (count%100 == 0) // update count eveyy 100 files
                notifyObservers(new ImportEvent.FILE_SIZE_STEP(total_files, total_file_length)); 
        }

        if (count%100 != 0) // last update if there is a remainder
            notifyObservers(new ImportEvent.FILE_SIZE_STEP(total_files, total_file_length));

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

    public synchronized void notifyObservers(ImportEvent event)
    {
        for (IObserver observer:observers)
        {
            observer.update(this, event);
        }
    }
}