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

import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;

import ome.formats.importer.ImportConfig;
import ome.formats.importer.util.HtmlMessenger;

import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;

/**
 * @author Brian Loranger brain at lifesci.dundee.ac.uk
 *
 */
public class CommentMessenger extends JDialog implements ActionListener
{
    private static final long serialVersionUID = -894653530593047377L;
    
    private static final String ICON = "gfx/nuvola_mail_send64.png";

    ImportConfig            config;
    
    JPanel                  mainPanel;
    JPanel                  commentPanel;
    JPanel                  debugPanel;

    //JButton                 quitBtn;
    JButton                 sendBtn;
    JButton                 cancelBtn;
    JButton                 copyBtn;
    
    JTextField              emailTextField;
    String                  emailText           = "";          
    
    JTextArea               commentTextArea;
    String                  commentText         = "";
    
    /**
     * @param owner - owner JFrame
     * @param title - comment dialog title
     * @param config - import config
     * @param modal - modal yes/no
     * @param debug - display debug borders yes/no
     */
    CommentMessenger(JFrame owner, String title, ImportConfig config, Boolean modal, boolean debug)
    {
        super(owner);
        this.config = config; 
        
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        setTitle(title);
        setModal(modal);
        setResizable(true);
        setSize(new Dimension(680, 400));
        setLocationRelativeTo(owner);
        
        // Set up the main panel for tPane, quit, and send buttons
        double mainTable[][] =
                {{10, 150, TableLayout.FILL, 100, 5, 100, 10}, // columns
                {TableLayout.FILL, 40}}; // rows

        mainPanel = GuiCommonElements.addMainPanel(this, mainTable, 10,10,10,10, debug);

        // Add the quit, cancel and send buttons to the main panel
        //quitBtn = GuiCommonElements.addButton(mainPanel, "Quit Application", 'Q',
        //        "Quit the application", "1, 1, f, c", debug);
        //quitBtn.addActionListener(this);

        cancelBtn = GuiCommonElements.addButton(mainPanel, "Cancel", 'C',
                "Cancel your message", "3, 1, f, c", debug);
        cancelBtn.addActionListener(this);

        sendBtn = GuiCommonElements.addButton(mainPanel, "Send", 'S',
                "Send your comment to the development team", "5, 1, f, c", debug);
        sendBtn.addActionListener(this);

        this.getRootPane().setDefaultButton(sendBtn);
        GuiCommonElements.enterPressesWhenFocused(sendBtn);
        
        // fill out the comments panel (changes according to icon existance)        
        Icon icon = GuiCommonElements.getImageIcon(ICON);
        
        int iconSpace = 0;
        if (icon != null) iconSpace = icon.getIconWidth() + 20;
        
        double commentTable[][] = 
        {{iconSpace, (160 - iconSpace), TableLayout.FILL}, // columns
                {90, 30, TableLayout.FILL}}; // rows
        
        commentPanel = GuiCommonElements.addMainPanel(this, commentTable, 10,10,10,10, debug);

        String message = "Thank you for taking the time to send us your comments. \n\n" +
                "Your feedback will be used to further the developmment of the " +
                "importer and improve our software. Any personal details you provide are" +
                " purely optional, and will only be used for development purposes.";

        JLabel iconLabel = new JLabel(icon);
        commentPanel.add(iconLabel, "0,0, l, c");
        
        @SuppressWarnings("unused")
        JTextPane instructions = 
        	GuiCommonElements.addTextPane(commentPanel, message, "1,0,2,0", debug);

        emailTextField = GuiCommonElements.addTextField(commentPanel, "Email: ", emailText, 'E',
        "Input tyour email address here.", "(Optional)", TableLayout.PREFERRED, "0, 1, 2, 1", debug);
        
        emailTextField.setText(config.email.get());
        
        commentTextArea = GuiCommonElements.addScrollingTextArea(commentPanel, "Comment:", 
                "", 'W', "0, 2, 2, 2", debug);
        
        // Add the tab panel to the main panel
        mainPanel.add(commentPanel, "0, 0, 6, 0");
        
        add(mainPanel, BorderLayout.CENTER);
        
        setVisible(true);      
        
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent event)
    {
        Object source = event.getSource();
        
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
                sendRequest(emailText, commentText, "");  
            }
            else
            {                
                JOptionPane.showMessageDialog(this, 
                    "Your email address must be valid\n" +
                    "(or blank) to send a comment.");    
            }
            
        }
    }

    /**
     * Send the requested comment to the feedback system
     * 
     * @param email - email comment
     * @param comment - text comment
     * @param extra - any additional information to send
     */
    private void sendRequest(String email, String comment, String extra)
    {
        List<Part> postList = new ArrayList<Part>();
        
        postList.add(new StringPart("java_version", System.getProperty("java.version")));
        postList.add(new StringPart("java_classpath", System.getProperty("java.class.path")));
        postList.add(new StringPart("os_name", System.getProperty("os.name")));
        postList.add(new StringPart("os_arch", System.getProperty("os.arch")));
        postList.add(new StringPart("os_version", System.getProperty("os.version")));
        postList.add(new StringPart("extra", extra));
        postList.add(new StringPart("comment", comment));
        postList.add(new StringPart("email", email));
        postList.add(new StringPart("app_name", "1"));
        postList.add(new StringPart("import_session", "test"));

        try {
            HtmlMessenger messenger = new HtmlMessenger(config.getTokenUrl(), postList);
            String serverReply = messenger.executePost();
            System.err.println(serverReply);
            if (serverReply != null)
                JOptionPane.showMessageDialog(this, "Thank you for your feedback.\n\n" +
                		"If you included your email address, you\n" +
                		"should receive a confirmation shortly.\n" +
                		"\n");
            this.dispose();
        }
        catch( Exception e ) {
            JOptionPane.showMessageDialog(this, 
                    "Sorry, but due to an error we were not able to automatically \n" +
                    "send your comment information. \n\n" +
                    "You can still send us your comments by emailing us at \n" +
                    "comments@openmicroscopy.org.uk.");
            sendBtn.setEnabled(true);
        }
    }
    
    /**
     * Test main (for debugging use only)
     * 
     * @param args
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception
    {
        new CommentMessenger(null, "Comment Dialog Test", new ImportConfig(), true, false);
    }
}
