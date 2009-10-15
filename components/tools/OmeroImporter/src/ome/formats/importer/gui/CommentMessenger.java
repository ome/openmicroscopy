/*
 * ome.formats.importer.gui.CommentMessenger
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

import layout.TableLayout;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.util.HtmlMessenger;

import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;

/**
 * @author TheBrain
 *
 */
public class CommentMessenger extends JDialog implements ActionListener
{
    private static final long serialVersionUID = -894653530593047377L;
    
    private static final String ICON = "gfx/nuvola_mail_send64.png";

    ImportConfig            config;
    GuiCommonElements       gui;
    
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
    
    CommentMessenger(JFrame owner, String title, ImportConfig config, Boolean modal, boolean debug)
    {
        super(owner);
        this.config = config; 
        this.gui = new GuiCommonElements(config);
        
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

        mainPanel = gui.addMainPanel(this, mainTable, 10,10,10,10, debug);

        // Add the quit, cancel and send buttons to the main panel
        //quitBtn = gui.addButton(mainPanel, "Quit Application", 'Q',
        //        "Quit the application", "1, 1, f, c", debug);
        //quitBtn.addActionListener(this);

        cancelBtn = gui.addButton(mainPanel, "Cancel", 'C',
                "Cancel your message", "3, 1, f, c", debug);
        cancelBtn.addActionListener(this);

        sendBtn = gui.addButton(mainPanel, "Send", 'S',
                "Send your comment to the development team", "5, 1, f, c", debug);
        sendBtn.addActionListener(this);

        this.getRootPane().setDefaultButton(sendBtn);
        gui.enterPressesWhenFocused(sendBtn);
        
        // fill out the comments panel (changes according to icon existance)        
        Icon icon = gui.getImageIcon(ICON);
        
        int iconSpace = 0;
        if (icon != null) iconSpace = icon.getIconWidth() + 20;
        
        double commentTable[][] = 
        {{iconSpace, (160 - iconSpace), TableLayout.FILL}, // columns
                {90, 30, TableLayout.FILL}}; // rows
        
        commentPanel = gui.addMainPanel(this, commentTable, 10,10,10,10, debug);

        String message = "Thank you for taking the time to send us your comments. \n\n" +
                "Your feedback will be used to futher the developmment of the " +
                "importer and improve our software. Any personal details you provide are" +
                " purely optional, and will only be used for development purposes.";

        JLabel iconLabel = new JLabel(icon);
        commentPanel.add(iconLabel, "0,0, l, c");
        
        @SuppressWarnings("unused")
        JTextPane instructions = 
                gui.addTextPane(commentPanel, message, "1,0,2,0", debug);

        emailTextField = gui.addTextField(commentPanel, "Email: ", emailText, 'E',
        "Input tyour email address here.", "(Optional)", TableLayout.PREFERRED, "0, 1, 2, 1", debug);
        
        emailTextField.setText(config.email.get());
        
        commentTextArea = gui.addTextArea(commentPanel, "Comment:", 
                "", 'W', "0, 2, 2, 2", debug);
        
        // Add the tab panel to the main panel
        mainPanel.add(commentPanel, "0, 0, 6, 0");
        
        add(mainPanel, BorderLayout.CENTER);
        
        setVisible(true);      
        
    }
    
    public void actionPerformed(ActionEvent e)
    {
        Object source = e.getSource();
        
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
            @SuppressWarnings("unused")
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
        new CommentMessenger(null, "Comment Dialog Test", new ImportConfig(), true, false);
    }
}
