/*
 * ome.formats.importer.GuiCommonElements.AddDatasetDialog
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

import info.clearthought.layout.TableLayout;

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

import ome.formats.importer.IObservable;
import ome.formats.importer.IObserver;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.ImportEvent;
import ome.formats.importer.util.ErrorContainer;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.FileUtils;

/**
 * @author Brian Loranger brain at lifesci.dundee.ac.uk
 *
 */
public class DebugMessenger extends JDialog implements ActionListener, IObservable, IObserver
{
    private static final long serialVersionUID = -1026712513033611084L;

    private final ImportConfig config;
    
    private static final boolean debug = false;
    
    private static final String ICON = "gfx/nuvola_mail_send64.png";
    
    private ArrayList<IObserver> observers = new ArrayList<IObserver>();

    private JFrame                  owner;
    
    private JPanel                  mainPanel;
    private JPanel                  commentPanel;

    private JButton                 quitBtn;
    private JButton                 cancelBtn;
    private JButton                 sendBtn;
    private JButton                 ignoreBtn;
    private JButton                 copyBtn;
    
    private JTextField              emailTextField;
    private String                  emailText           = "";          
    
    private JTextArea               commentTextArea;
    private String                  commentText         = "";
    
    private JTextPane               debugTextPane;

	private ArrayList<ErrorContainer> errorsArrayList;

	private JCheckBox uploadCheckmark, logUploadCheckmark;
	
    private long total_files, total_file_length;
	
	private FileSizeChecker checker;

    private String file_info;
    
    /**
     * @param owner - parent JFrame
     * @param title - dialog title
     * @param config - importerconfig
     * @param modal - modal yes/no
     * @param errorsArrayList - array of ErrorContainers to be sent.
     */
    DebugMessenger(JFrame owner, String title, ImportConfig config, Boolean modal, ArrayList<ErrorContainer> errorsArrayList)
    {
        super(owner);
        this.config = config;
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
        
        mainPanel = GuiCommonElements.addMainPanel(this, mainTable, 10, 10, 10, 10, debug);

        cancelBtn = GuiCommonElements.addButton(mainPanel, "Cancel", 'C',
                "Cancel your message", "5, 2, f, c", debug);
        cancelBtn.addActionListener(this);

        sendBtn = GuiCommonElements.addButton(mainPanel, "Send Comment", 'S',
                "Send your comment to the development team", "7, 2, f, c", debug);
        sendBtn.addActionListener(this);

        this.getRootPane().setDefaultButton(sendBtn);
        GuiCommonElements.enterPressesWhenFocused(sendBtn);
        
        uploadCheckmark = GuiCommonElements.addCheckBox(mainPanel, "Send the image files for these errors. " + file_info, "1,1,7,1", debug);
        //uploadCheckmark.setSelected(config.sendFiles.get());
        uploadCheckmark.setSelected(true);
       
        logUploadCheckmark = GuiCommonElements.addCheckBox(mainPanel, "Send importer log file.", "1,2,7,2", debug);
        config.sendLogFile.load();
        uploadCheckmark.setSelected(config.sendLogFile.get());
        //logUploadCheckmark.setSelected(true);
        
        // fill out the comments panel (changes according to icon existance)        
        Icon icon = GuiCommonElements.getImageIcon(ICON);
        
        int iconSpace = 0;
        if (icon != null) iconSpace = icon.getIconWidth() + 10;
        
        double commentTable[][] = 
        {{iconSpace, (160 - iconSpace), TableLayout.FILL}, // columns
                {100, 30, TableLayout.FILL, 110}}; // rows
        
        commentPanel = GuiCommonElements.addMainPanel(this, commentTable, 10, 10, 10, 10, debug);

        String message = "To help us improve our software, please fill " +
        "out the following form. \n\nPlease note that providing your email " +
        "address is optional, however doing so will make it easier for us " +
        "to contact you for addition information about your errors, and for " +
        "you to track their status.";

        JLabel iconLabel = new JLabel(icon);
        commentPanel.add(iconLabel, "0,0, l, c");
        
        @SuppressWarnings("unused")
        JTextPane instructions = 
                GuiCommonElements.addTextPane(commentPanel, message, "1,0,2,0", debug);

        emailTextField = GuiCommonElements.addTextField(commentPanel, "Email: ", emailText, 'E',
        "Input your email address here.", "(Optional)", TableLayout.PREFERRED, "0, 1, 2, 1", debug);
        
        emailTextField.setText(config.email.get());
        
        commentTextArea = GuiCommonElements.addScrollingTextArea(commentPanel, "Please provide any additional information of importance.", 
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


    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent event)
    {
        Object source = event.getSource();
        
        
        if (source == quitBtn)
        {
            if (GuiCommonElements.quitConfirmed(this, "Abandon your import and quit the application?") == true)
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
            
            if (GuiCommonElements.validEmail(emailText) || emailText.trim().length() == 0)
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
    
    /**
     * Send debug information to feedback system
     * 
     * @param emailText - email address of sender
     * @param commentText - their comment
     * @param extraText - any extra 'hidden' text to send
     */
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
    /* (non-Javadoc)
     * @see ome.formats.importer.IObservable#addObserver(ome.formats.importer.IObserver)
     */
    public boolean addObserver(IObserver object)
    {
        return observers.add(object);
    }
    
    /* (non-Javadoc)
     * @see ome.formats.importer.IObservable#deleteObserver(ome.formats.importer.IObserver)
     */
    public boolean deleteObserver(IObserver object)
    {
        return observers.remove(object);
        
    }

    /* (non-Javadoc)
     * @see ome.formats.importer.IObservable#notifyObservers(ome.formats.importer.ImportEvent)
     */
    public void notifyObservers(ImportEvent event)
    {
        for (IObserver observer:observers)
        {
            observer.update(this, event);
        }
    }
    
    
    /* (non-Javadoc)
     * @see ome.formats.importer.IObserver#update(ome.formats.importer.IObservable, ome.formats.importer.ImportEvent)
     */
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
           
            //System.out.println(file_info);
            uploadCheckmark.setText(file_info);
            uploadCheckmark.repaint();
        }
    }
    
    /**
     * Main for testing (debugging only)
     * 
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

/**
 * @author Brian Loranger brain at lifesci.dundee.ac.uk
 *
 */
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
    /* (non-Javadoc)
     * @see ome.formats.importer.IObservable#addObserver(ome.formats.importer.IObserver)
     */
    public boolean addObserver(IObserver object)
    {
        return observers.add(object);
    }
    
    /* (non-Javadoc)
     * @see ome.formats.importer.IObservable#deleteObserver(ome.formats.importer.IObserver)
     */
    public boolean deleteObserver(IObserver object)
    {
        return observers.remove(object);
        
    }

    /* (non-Javadoc)
     * @see ome.formats.importer.IObservable#notifyObservers(ome.formats.importer.ImportEvent)
     */
    public synchronized void notifyObservers(ImportEvent event)
    {
        for (IObserver observer:observers)
        {
            observer.update(this, event);
        }
    }
}