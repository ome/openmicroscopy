/*
 * ome.formats.testclient.LoginDialog
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *------------------------------------------------------------------------------
 */

package ome.formats.importer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import ome.formats.importer.util.GuiCommonElements;

import layout.TableLayout;

public class LoginDialog extends JDialog implements ActionListener
{

    boolean debug = false;
    
    GuiCommonElements       gui;
    
    JPanel                  mainPanel;
    JPanel                  topPanel;

    JButton                 loginBtn;
    JButton                 quitBtn;
    JButton                 configBtn;
    
    JTextPane               pleaseLogIn;
    
    private JTextField     uname;
    private JPasswordField pswd;

    private JTextField     srvr;
    private JTextField     prt;

    public String          username;
    public String          password;

    public String          server;
    public String          port;

    public boolean         cancelled = true;

    private Preferences    userPrefs = Preferences
                                             .userNodeForPackage(LoginDialog.class);

    LoginDialog(JFrame owner, String title, boolean modal)
    {
        setLocation(200, 200);
        setTitle(title);
        setModal(modal);
        setResizable(false);
        setSize(new Dimension(550, 115));
        setLocationRelativeTo(owner);
        setUndecorated(true);
        setBackground(Color.black);
        
        
        // Get the preference file options
        username = userPrefs.get("username", username);
        // password = userPrefs.get("password", password);
        server = userPrefs.get("server", server);
        port = userPrefs.get("port", port);
        if (port == null) port = "1099";
        
        gui = new GuiCommonElements();
        
        // Set up the main panel for tPane, quit, and send buttons
        double mainTable[][] =
                {{TableLayout.FILL, 100, 5, 100, 10}, // columns
                {TableLayout.FILL, 30}}; // rows
        
        mainPanel = gui.addMainPanel(this, mainTable, 10,15,10,15, debug);

        // Add the login and quit buttons to the main panel

        loginBtn = gui.addButton(mainPanel, "Login", 'L',
                "Login", "1, 1, f, c", debug);
        loginBtn.addActionListener(this);
        
        quitBtn = gui.addButton(mainPanel, "Quit", 'Q',
                "Quit the Application", "3, 1, f, c", debug);
        quitBtn.addActionListener(this);

        this.getRootPane().setDefaultButton(loginBtn);
        gui.enterPressesWhenFocused(loginBtn);
        
        // top table containing comment and server information
        double topTable[][] = 
                {{250, 20, 225, 30}, // columns
                {24, TableLayout.FILL}}; // rows
        
        topPanel = gui.addMainPanel(this, topTable, 0,0,0,0, debug);
        
        String message = "Please Log In";
        
        StyleContext context = new StyleContext();

        Style style = context.getStyle(StyleContext.DEFAULT_STYLE);
        StyleConstants.setAlignment(style, StyleConstants.ALIGN_LEFT);;
        StyleConstants.setForeground(style, Color.white);
        StyleConstants.setBold(style, true);
        StyleConstants.setFontSize(style, 18);
        
        JTextPane instructions = gui.addTextPane(topPanel, message, "0, 0, l, c", 
                context, style, debug);

        style = context.getStyle(StyleContext.DEFAULT_STYLE);
        StyleConstants.setAlignment(style, StyleConstants.ALIGN_RIGHT);
        StyleConstants.setForeground(style, Color.white);
        
        JTextPane serverText = gui.addTextPane(topPanel, server, "2, 0, r, c", 
                context, style, debug);
        
        configBtn = gui.addButton(topPanel, "", 'X', "Config Server", "3, 0, f, c", debug);
        configBtn.setText(null);
        
        configBtn.setBorderPainted(false);
        
        String path = "gfx/config.png";
        
        java.net.URL imgURL = Main.class.getResource(path);
        if (imgURL != null)
        {
            configBtn.setIcon(new ImageIcon(imgURL));
        } else
        {
            System.err.println("Couldn't find icon: " + path);
        }

        path = "gfx/config_pressed.png";
        
        imgURL = Main.class.getResource(path);
        if (imgURL != null)
        {
            configBtn.setPressedIcon(new ImageIcon(imgURL));
        } else
        {
            System.err.println("Couldn't find icon: " + path);
        }
        
        uname = gui.addTextField(topPanel, "<HTML><font color=white>Label</font></HTML>", username, 'U',
                "Input tyour email address here.", "(Optional)", 
                TableLayout.PREFERRED, "0, 1", debug);
                
        //pswd = gui.addTextField(topPanel, "Email: ", password, 'E',
        //        "Input tyour email address here.", "(Optional)", TableLayout.PREFERRED, "0, 1, 2, 1", debug);
        //uname = addEntryField(this, "Username: ", username, 'U', c, 0, 1, 2,
        //        "Input the database username here.");

        //pswd = addPasswordField(this, "Password: ", password, 'P', c, 0, 1, 2,
        //        "Input the database password here.");

        //srvr = addEntryField(this, "Server: ", server, 'S', c, 0, 1, 2,
        //        "Input the server hostname here.");

        //prt = addEntryField(this, "Port: ", port, 'R', c, 0, 1, 1,
        //        "Input the server port here.");

        //loginBtn = addButton(this, "Login", c, 2, 1, 1.0f, "Click to login.");

        //this.getRootPane().setDefaultButton(loginBtn);

        //loginBtn.addActionListener(this);

//        addWindowListener(new WindowAdapter()
//        {
//
//            public void windowOpened(WindowEvent e)
//            {
//                if (uname == null) uname.requestFocus();
//                else
//                    pswd.requestFocus();
//            }
//        });

        // Add the tab panel to the main panel
        mainPanel.add(topPanel, "0, 0, 4, 0");

        add(mainPanel, BorderLayout.CENTER);
        
        setVisible(true);
    }

    static JTextField addEntryField(Container container, String name,
            String initialValue, int mnemonic, GridBagConstraints c,
            int labelCol, int labelWidth, int fieldWidth, String tooltip)
    {

        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(2, 20, 2, 2);
        c.weightx = 0.0;

        c.gridx = labelCol;
        c.gridwidth = labelWidth;

        JLabel label = new JLabel(name);
        label.setDisplayedMnemonic(mnemonic);
        container.add(label, c);

        c.insets = new Insets(2, 2, 2, 20);
        c.weightx = 1.0;

        c.gridx = labelCol + 1;
        c.gridwidth = fieldWidth;

        JTextField result = new JTextField(100);
        label.setLabelFor(result);
        result.setToolTipText(tooltip);
        if (initialValue != null) result.setText(initialValue);
        container.add(result, c);
        return result;
    }

    static JPasswordField addPasswordField(Container container, String name,
            String initialValue, int mnemonic, GridBagConstraints c,
            int labelCol, int labelWidth, int fieldWidth, String tooltip)
    {

        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(2, 20, 2, 2);
        c.weightx = 0.0;

        c.gridx = labelCol;
        c.gridwidth = labelWidth;

        JLabel label = new JLabel(name);
        label.setDisplayedMnemonic(mnemonic);
        container.add(label, c);

        c.insets = new Insets(2, 2, 2, 20);
        c.weightx = 1.0;

        c.gridx = labelCol + 1;
        c.gridwidth = fieldWidth;

        JPasswordField result = new JPasswordField(100);
        label.setLabelFor(result);
        result.setToolTipText(tooltip);
        if (initialValue != null) result.setText(initialValue);
        container.add(result, c);
        return result;
    }

    static JButton addButton(Container container, String name,
            GridBagConstraints c, int column, int width, float weight,
            String tooltip)
    {

        c.anchor = GridBagConstraints.EAST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(2, 2, 2, 20);
        c.weightx = weight;

        c.gridx = column;
        c.gridwidth = width;

        JButton button = new JButton(name);
        container.add(button, c);

        return button;
    }

    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == loginBtn)
        {
            username = uname.getText();
            password = pswd.getText();
            server = srvr.getText();
            port = prt.getText();
            cancelled = false;
            this.dispose();
        }

    }
    
    /**
     * @param args
     */
    public static void main(String[] args)
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
        

        JFrame f = new JFrame();   
        LoginDialog loginDialog = new LoginDialog(f, "", false); 
        f.setVisible(false);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.pack();
    }
}
