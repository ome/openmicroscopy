/*
 * ome.formats.importer.gui.LoginDialog
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

package ome.formats.importer.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import layout.TableLayout;

public class LoginDialog extends JDialog 
    implements ActionListener, PropertyChangeListener
{
    private static final long serialVersionUID = 1L;

    boolean debug = false;

    JFrame                  main;
    
    GuiCommonElements       gui;

    Integer                 loginHeight = 113;
    Integer                 loginWidth = 551;
    
    String                  DEFAULT_SERVER_TEXT = "Add a New Server ->";
    String                  SERVER_NAME_SEPARATOR = ",";
    List<String>            serverList = new ArrayList<String>();
    JTextPane               serverText;
    
    Color                   panelColor = Color.black ;
    Color                   textColor   = Color.white;
    String                  unameLabel = "<html><font color=white><b>Username: </b></font></html>";
    String                  pswdLabel = "<html><font color=white><b>Password: </b></font></html>";
      
    JPanel                  mainPanel;
    JPanel                  topPanel;

    JButton                 loginBtn;
    JButton                 quitBtn;
    JButton                 configBtn;
    
    JTextPane               pleaseLogIn;
    
    // Change to use version information
    JTextPane               versionInfo;
    
    JTextField              uname;
    JPasswordField          pswd;

    JTextField              srvr;
    JTextField              prt;
    
    Frame                  f;
    
    public boolean          cancelled = true;

    LoginDialog (GuiCommonElements gui, JFrame owner, JFrame main, String title, boolean modal, boolean center)
    {   
        super(owner);
        this.main = main;
        this.gui = gui;
        
        setLocation(200, 200);
        setModal(modal);
        setResizable(false);
        toFront();
        setSize(new Dimension(loginWidth, loginHeight));
        setLocationRelativeTo(main);
        //get the amount to offset the login by
        if (!center && GuiImporter.getSplashLocation() != null)
        {
            int offset = GuiImporter.getSplashLocation().y 
                    + gui.getImageIcon(GuiImporter.splash).getIconHeight() + 20;
            setLocation(this.getX(), offset);           
        }
        setUndecorated(true);
        
        // Set up the main panel for tPane, quit, and send buttons
        double mainTable[][] =
                {{TableLayout.FILL, 100, 5, 100}, // columns
                {TableLayout.FILL, 30}}; // rows
        
        mainPanel = gui.addMainPanel(this, mainTable, 10,20,10,20, debug);

        // Add the login and quit buttons to the main panel

        loginBtn = gui.addButton(mainPanel, "Login", 'L',
                "Login", "1, 1, f, c", debug);
        loginBtn.addActionListener(this);
        
        quitBtn = gui.addButton(mainPanel, "Cancel", 'Q',
                "Quit the Application", "3, 1, f, c", debug);
        quitBtn.addActionListener(this);

        this.getRootPane().setDefaultButton(loginBtn);
        gui.enterPressesWhenFocused(loginBtn);
        
        // top table containing comment and server information
        double topTable[][] = 
                {{245, 18, 220, 28}, // columns
                {32, TableLayout.FILL}}; // rows
        
        topPanel = gui.addMainPanel(this, topTable, 0,0,0,0, debug);
        
        String message = "Please Log In";
        
        StyleContext context = new StyleContext();

        Style style = context.getStyle(StyleContext.DEFAULT_STYLE);
        StyleConstants.setAlignment(style, StyleConstants.ALIGN_LEFT);;
        StyleConstants.setForeground(style, textColor);
        StyleConstants.setBold(style, true);
        StyleConstants.setFontSize(style, 18);
        
        pleaseLogIn = gui.addTextPane(topPanel, message, "0, 0, l, c", 
                context, style, debug);

        style = context.getStyle(StyleContext.DEFAULT_STYLE);
        StyleConstants.setAlignment(style, StyleConstants.ALIGN_RIGHT);
        StyleConstants.setForeground(style, textColor);
        
        String currentServer = gui.config.hostname.get();
        List<String> serverList = gui.config.getServerList();
        if (serverList == null || !serverList.contains(currentServer)) {
            currentServer = DEFAULT_SERVER_TEXT;
            gui.config.hostname.set(currentServer);
        }
        serverText = gui.addTextPane(topPanel, currentServer, "2, 0, r, c", 
                context, style, debug);
        
        configBtn = gui.addButton(topPanel, "", 'X', "Config Server", "3, 0, c, c", debug);
        configBtn.setText(null);
        
        configBtn.setBorderPainted(false);
        configBtn.setMargin(new Insets(0,0,0,0));
        configBtn.setBorder(BorderFactory.createEmptyBorder());
        configBtn.setFocusPainted(false);
        configBtn.setOpaque(false);
        configBtn.setContentAreaFilled(false);
        
        configBtn.setIcon(gui.getImageIcon("gfx/config.png"));
        configBtn.setPressedIcon(gui.getImageIcon("gfx/config_pressed.png"));
        
        uname = gui.addTextField(topPanel, 
                unameLabel, 
                gui.config.username.get(), 'U',"Input your useername here.", "", 
                TableLayout.PREFERRED, "0, 1, 0, 1", debug);
                
        pswd = gui.addPasswordField(topPanel, 
                pswdLabel, 
                gui.config.password.get(), 'U',"Input your useername here.", "", 
                TableLayout.PREFERRED, "2, 1, 3, 1", debug);

        style = context.getStyle(StyleContext.DEFAULT_STYLE);
        StyleConstants.setAlignment(style, StyleConstants.ALIGN_LEFT);;
        StyleConstants.setForeground(style, textColor);
        StyleConstants.setFontSize(style, 12);
        StyleConstants.setBold(style, true);
        
        versionInfo = gui.addTextPane(mainPanel, gui.config.getIniVersionNumber(), "0, 1, l, b", 
                context, style, debug);
        
        // Add an action listener to the uname to move to pswd
        configBtn.addActionListener(this);

        addWindowListener(new WindowAdapter()
        {

            public void windowOpened(WindowEvent e)
            {
                if (uname != null) uname.requestFocus();
                else
                    pswd.requestFocus();
            }
        });
        
        // Add the tab panel to the main panel
        mainPanel.add(topPanel, "0, 0, 3, 0");
        
        JLabel background = new JLabel(gui.getImageIcon("gfx/login_background.png"));
        background.setBorder(BorderFactory.createEmptyBorder());
        this.getRootPane().setBorder(null);
        JLayeredPane layers = new JLayeredPane();  //Default is absolute layout.
        layers.setBounds(0,0,loginWidth, loginHeight);
        layers.setBackground(Color.black);
        background.setBounds(0,0,loginWidth, loginHeight);
        mainPanel.setSize(loginWidth, loginHeight);
        
        layers.add(background, new Integer(0));
        layers.add(mainPanel, new Integer(1));
        getContentPane().add(layers);
               
        setVisible(true);
        
       //serverList = getServerList();
        //serverList.clear();
        //removeAllServers();
    }





    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == loginBtn)
        {
            String username = uname.getText();
            String password = new String(pswd.getPassword());
            //server = srvr.getText();
            //port = prt.getText();
            cancelled = false;
            firePropertyChange(LoginHandler.LOGIN, false, true);
            this.dispose();
            if (f != null) f.dispose();
        }
        if(e.getSource() == quitBtn)
        {
            cancelled = true;
            firePropertyChange(LoginHandler.LOGIN_CANCELLED, false, true);
            this.dispose();
            if (f != null) f.dispose();
        }

        if(e.getSource() == configBtn)
        {
            ServerDialog serverDialog = new ServerDialog(gui.config.getServerList());
            serverDialog.setLocationRelativeTo(main);
            serverDialog.addPropertyChangeListener(this);
            serverDialog.setVisible(true);
            String currentServer = serverDialog.getCurrentServer();
            if (currentServer.trim().length() == 0)
            {
                currentServer = DEFAULT_SERVER_TEXT;
            }
            serverText.setText(currentServer);
        }
    }
    
    /**
     * Reacts to property changes fired by the {@link ServerDialog}.
     * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
        String e = evt.getPropertyName();
        if (ServerDialog.REMOVE_PROPERTY.equals(e)) {
            String oldValue = (String) evt.getOldValue();
            gui.config.removeServer(oldValue);
            if (gui.config.hostname.get().equals(oldValue)) 
                gui.config.hostname.set(DEFAULT_SERVER_TEXT);
        }
    }
    
    ///////////////////////////////////////////////////////////////////////
    /**
     * Main used for testing standalone dialog
     */
    public static void main(String[] args)
    {
        String laf = UIManager.getSystemLookAndFeelClassName() ;
        //laf = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
        //laf = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
        laf = "javax.swing.plaf.metal.MetalLookAndFeel";
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
        new LoginDialog(null, f, f, "", false, true); 
        f.setVisible(false);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.pack();
    }
}
