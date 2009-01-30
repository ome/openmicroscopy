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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.JButton;
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

import ome.formats.importer.util.Actions;
import ome.formats.importer.util.GuiCommonElements;
import ome.formats.importer.util.IniFileLoader;

import layout.TableLayout;

public class LoginFrame extends JFrame 
    implements ActionListener, PropertyChangeListener
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    IniFileLoader ini;
    
    /**
     * 
     */
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
    
    public String           username;
    public String           password;

    public String           currentServer;
    public int              port = 4063;

    public boolean          cancelled = true;

    private Preferences    userPrefs = Preferences
                                             .userNodeForPackage(LoginFrame.class);
    
    LoginFrame (JFrame owner, JFrame main, String title, boolean modal, boolean center)
    {   
        this.main = main;
        
        gui = new GuiCommonElements();
        
        setTitle("Login");
        setIconImage(gui.getImageIcon(Main.ICON).getImage());

        setLocation(200, 200);
        setResizable(false);
        toFront();
        setSize(new Dimension(loginWidth, loginHeight));
        setLocationRelativeTo(main);
        //get the amount to offset the login by
        //get the amount to offset the login by
        if (!center && Main.getSplashLocation() != null)
        {
            int offset = Main.getSplashLocation().y 
                    + gui.getImageIcon(Main.splash).getIconHeight() + 5;
            setLocation(this.getX(), offset);           
        }
        setUndecorated(true);
                
        // Get the preference file options
        username = userPrefs.get("username", username);
        if (username != null) username = username.trim();
        
        
        // password = userPrefs.get("password", password);
        currentServer = userPrefs.get("server", currentServer);
        if (currentServer != null) currentServer = currentServer.trim();
        
        serverList = getServerList();
        
        port = userPrefs.getInt("port", port);
        
        // Set up the main panel for tPane, quit, and send buttons
        double mainTable[][] =
                {{TableLayout.FILL, 100, 5, 100}, // columns
                {TableLayout.FILL, 30}}; // rows
        
        mainPanel = gui.addMainPanel(this, mainTable, 10,20,10,20, debug);

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
        
        serverList = getServerList();
        if (serverList == null || !serverList.contains(currentServer))
            currentServer = DEFAULT_SERVER_TEXT;
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
                username, 'U',"Input your useername here.", "", 
                TableLayout.PREFERRED, "0, 1, 0, 1", debug);
                
        pswd = gui.addPasswordField(topPanel, 
                pswdLabel, 
                password, 'U',"Input your useername here.", "", 
                TableLayout.PREFERRED, "2, 1, 3, 1", debug);

        style = context.getStyle(StyleContext.DEFAULT_STYLE);
        StyleConstants.setAlignment(style, StyleConstants.ALIGN_LEFT);;
        StyleConstants.setForeground(style, textColor);
        StyleConstants.setFontSize(style, 12);
        StyleConstants.setBold(style, true);
        
        versionInfo = gui.addTextPane(mainPanel, ini.getVersionNumber(), "0, 1, l, b", 
                context, style, debug);
        
        // Add an action listener to the uname to move to pswd
        configBtn.addActionListener(this);

        addWindowListener(new WindowAdapter()
        {

            public void windowOpened(WindowEvent e)
            {
                if (uname == null) uname.requestFocus();
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

    private List<String> getServerList()
    {
        String serverListString = null;
        serverListString = userPrefs.get("servers", serverListString);
        if (serverListString == null || serverListString.trim().length() == 0) {
            return null;
        } else {
            String[] l = 
                    serverListString.split(SERVER_NAME_SEPARATOR, 0);
            if (l == null || l.length == 0) {
                return null;
            } else {
                if (serverList != null) serverList.clear();
                for (int index = 0; index < l.length; index++) {
                    if (serverList != null)
                        serverList.add(l[index].trim());
                }
            }
        }
        return serverList;
    }

    // Save the current serverList if the currentServer is not on the list
    void updateServerList(String currentServer)
    {
        // get the server list 
        if (currentServer.trim().length() == 0 || currentServer.contains(DEFAULT_SERVER_TEXT))
        {
            return;            
        }

        
        List<String> l = getServerList();
        if (l != null && l.contains(currentServer))
        {
            return;
        }
              
        String serverListString = null;
        serverListString = userPrefs.get("servers", serverListString);
        if (serverListString == null || serverListString.length() == 0) {
            userPrefs.put("servers", currentServer.trim());
        } else {
            userPrefs.put("servers", serverListString+SERVER_NAME_SEPARATOR+currentServer);
        }
    }
    
    private void removeServer(String server)
    {
        List l = getServerList();
        if (l == null) return;
        l.remove(server);
        Iterator i = l.iterator();
        String list = "";
        int n = l.size()-1;
        int index = 0;
        while (i.hasNext()) {
            list += (String) i.next();
            if (index != n)
                list += SERVER_NAME_SEPARATOR;
            index++;
        }
        userPrefs.put("servers", list);
        serverList = getServerList();
    }

    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == loginBtn)
        {
            username = uname.getText();
            password = new String(pswd.getPassword());
            //server = srvr.getText();
            //port = prt.getText();
            cancelled = false;
            firePropertyChange(Actions.LOGIN, false, true);
            this.dispose();
            if (f != null) f.dispose();
        }
        if(e.getSource() == quitBtn)
        {
            System.exit(0);
        }

        if(e.getSource() == configBtn)
        {
            serverList = getServerList();
            ServerDialog serverDialog = new ServerDialog(serverList);
            serverDialog.setLocationRelativeTo(main);
            serverDialog.addPropertyChangeListener(this);
            serverDialog.setVisible(true);
            currentServer = serverDialog.getCurrentServer();
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
            removeServer(oldValue);
            if (currentServer.equals(oldValue)) 
                currentServer = DEFAULT_SERVER_TEXT;
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
        new LoginFrame(f, f, "", false, true); 
        f.setVisible(false);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.pack();
    }
}
