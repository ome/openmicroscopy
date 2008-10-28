/*
 * ome.formats.importer.CommentMessenger
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
package ome.formats.importer;

import java.awt.BorderLayout;
import java.awt.Dimension;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

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
import javax.swing.UIManager;

import ome.formats.importer.util.GuiCommonElements;
import ome.formats.importer.util.HtmlMessenger;
import ome.formats.importer.util.IniFileLoader;

import layout.TableLayout;

/**
 * @author TheBrain
 *
 */
public class CommentMessenger extends JDialog implements ActionListener
{
    private static final long serialVersionUID = -894653530593047377L;
    
    IniFileLoader ini = IniFileLoader.getIniFileLoader();
    
    private Preferences    userPrefs = 
        Preferences.userNodeForPackage(Main.class);

    private String userEmail = userPrefs.get("userEmail", "");
    
    boolean debug = false;
    
    String url = "http://users.openmicroscopy.org.uk/~brain/omero/commentcollector.php";
    
    private static final String ICON = "gfx/nuvola_mail_send64.png";

    GuiCommonElements       gui;
    
    JPanel                  mainPanel;
    JPanel                  commentPanel;
    JPanel                  debugPanel;

    JButton                 quitBtn;
    JButton                 sendBtn;
    JButton                 cancelBtn;
    JButton                 copyBtn;
    
    JTextField              emailTextField;
    String                  emailText           = "";          
    
    JTextArea               commentTextArea;
    String                  commentText         = "";
    
    CommentMessenger(JFrame owner, String title, Boolean modal)
    {
        super(owner);
        gui = new GuiCommonElements();
        
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
        
        emailTextField.setText(userEmail);
        
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
        
        if (source == quitBtn)
        {
            if (gui.quitConfirmed(this) == true)
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
            
            userPrefs.put("userEmail", emailText);
            
            sendRequest(emailText, commentText, "Extra data goes here.");
        }
    }

    private void sendRequest(String email, String comment, String extra)
    {
        Map <String, String>map = new HashMap<String, String>();
        extra = "(" + ini.getVersionNumber() + ") " + extra;
        
        map.put("email",email);
        map.put("comment", comment);
        map.put("extra", extra);
        
        map.put("type", "importer_comments");
        map.put("java_version", System.getProperty("java.version"));
        map.put("java_class_path", System.getProperty("java.class.path"));
        map.put("os_name", System.getProperty("os.name"));
        map.put("os_arch", System.getProperty("os.arch"));
        map.put("os_version", System.getProperty("os.version"));

        try {
            HtmlMessenger messenger = new HtmlMessenger(url, map);
            String serverReply = messenger.executePost();
            JOptionPane.showMessageDialog(this, serverReply);
            this.dispose();
        }
        catch( Exception e ) {
            JOptionPane.showMessageDialog(this, 
                    "Sorry, but due to an error we were not able to automatically \n" +
                    "send your comment information. \n\n" +
                    "You can still send us your comments by emailing us at \n" +
                    "comments@openmicroscopy.org.uk.");
        }
    }
        
    /**
     * @param args
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception
    {
        String laf = UIManager.getSystemLookAndFeelClassName() ;
        //laf = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
        //laf = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
        //laf = "javax.swing.plaf.metal.MetalLookAndFeel";
        //laf = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
        
        if (laf.equals("apple.laf.AquaLookAndFeel"))
        {
            System.setProperty("Quaqua.design", "panther");
            
            try {
                UIManager.setLookAndFeel(
                    "ch.randelshofer.quaqua.QuaquaLookAndFeel"
                );
           } catch (Exception e) { System.err.println(laf + " not supported.");}
        } else {
            try {
                UIManager.setLookAndFeel(laf);
            } catch (Exception e) 
            { System.err.println(laf + " not supported."); }
        }
        new CommentMessenger(null, "Comment Dialog Test", true);
    }
}
