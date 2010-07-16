/*
 * ome.formats.importer.gui.History
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package ome.formats.importer.gui;

import info.clearthought.layout.TableLayout;

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

import ome.formats.importer.ImportConfig;

/**
 * @author Brian W. Loranger
 *
 */
public class LoginDialog extends JDialog implements ActionListener, PropertyChangeListener
{
    private static final long serialVersionUID = 1L;

    boolean debug = false;

    JFrame                  main;

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
    
    Frame                  	f;
    
    ImportConfig			config;
    
    public boolean          cancelled = true;

    /**
     * Set up and display login dialog
     * 
     * @param config - ImportConfig for default valutes
     * @param owner - parent window
     * @param main - set relative to this window
     * @param title - dialog title
     * @param modal - modal dialog yes/no
     * @param center - center relative to main yes/no
     */
    LoginDialog (ImportConfig config, JFrame owner, JFrame main, String title, boolean modal, boolean center)
    {   
        super(owner);
        this.config = config;
        this.main = main;

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
                    + GuiCommonElements.getImageIcon(GuiImporter.splash).getIconHeight() + 20;
            setLocation(this.getX(), offset);           
        }
        setUndecorated(true);
        
        // Set up the main panel for tPane, quit, and send buttons
        double mainTable[][] =
                {{TableLayout.FILL, 100, 5, 100}, // columns
                {TableLayout.FILL, 30}}; // rows
        
        mainPanel = GuiCommonElements.addMainPanel(this, mainTable, 10,20,10,20, debug);

        // Add the login and quit buttons to the main panel

        loginBtn = GuiCommonElements.addButton(mainPanel, "Login", 'L',
                "Login", "1, 1, f, c", debug);
        loginBtn.addActionListener(this);
        
        quitBtn = GuiCommonElements.addButton(mainPanel, "Cancel", 'Q',
                "Quit the Application", "3, 1, f, c", debug);
        quitBtn.addActionListener(this);

        this.getRootPane().setDefaultButton(loginBtn);
        GuiCommonElements.enterPressesWhenFocused(loginBtn);
        
        // top table containing comment and server information
        double topTable[][] = 
                {{245, 18, 220, 28}, // columns
                {32, TableLayout.FILL}}; // rows
        
        topPanel = GuiCommonElements.addMainPanel(this, topTable, 0,0,0,0, debug);
        
        String message = "Please Log In";
        
        StyleContext context = new StyleContext();

        Style style = context.getStyle(StyleContext.DEFAULT_STYLE);
        StyleConstants.setAlignment(style, StyleConstants.ALIGN_LEFT);;
        StyleConstants.setForeground(style, textColor);
        StyleConstants.setBold(style, true);
        StyleConstants.setFontSize(style, 18);
        
        pleaseLogIn = GuiCommonElements.addTextPane(topPanel, message, "0, 0, l, c", 
                context, style, debug);

        style = context.getStyle(StyleContext.DEFAULT_STYLE);
        StyleConstants.setAlignment(style, StyleConstants.ALIGN_RIGHT);
        StyleConstants.setForeground(style, textColor);
        
        String currentServer = config.hostname.get();
        List<String> serverList = config.getServerList();
        if (serverList == null || !serverList.contains(currentServer)) {
            currentServer = DEFAULT_SERVER_TEXT;
            config.hostname.set(currentServer);
        }
        serverText = GuiCommonElements.addTextPane(topPanel, currentServer, "2, 0, r, c", 
                context, style, debug);
        
        configBtn = GuiCommonElements.addButton(topPanel, "", 'X', "Config Server", "3, 0, c, c", debug);
        configBtn.setText(null);
        
        configBtn.setBorderPainted(false);
        configBtn.setMargin(new Insets(0,0,0,0));
        configBtn.setBorder(BorderFactory.createEmptyBorder());
        configBtn.setFocusPainted(false);
        configBtn.setOpaque(false);
        configBtn.setContentAreaFilled(false);
        
        configBtn.setIcon(GuiCommonElements.getImageIcon("gfx/config.png"));
        configBtn.setPressedIcon(GuiCommonElements.getImageIcon("gfx/config_pressed.png"));
        
        uname = GuiCommonElements.addTextField(topPanel, 
                unameLabel, 
                config.username.get(), 'U',"Input your useername here.", "", 
                TableLayout.PREFERRED, "0, 1, 0, 1", debug);
                
        pswd = GuiCommonElements.addPasswordField(topPanel, 
                pswdLabel, 
                config.password.get(), 'U',"Input your useername here.", "", 
                TableLayout.PREFERRED, "2, 1, 3, 1", debug);

        style = context.getStyle(StyleContext.DEFAULT_STYLE);
        StyleConstants.setAlignment(style, StyleConstants.ALIGN_LEFT);;
        StyleConstants.setForeground(style, textColor);
        StyleConstants.setFontSize(style, 12);
        StyleConstants.setBold(style, true);
        
        versionInfo = GuiCommonElements.addTextPane(mainPanel, config.getIniVersionNumber(), "0, 1, l, b", 
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
        
        JLabel background = new JLabel(GuiCommonElements.getImageIcon("gfx/login_background.png"));
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

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent event)
    {
        if (event.getSource() == loginBtn)
        {
            @SuppressWarnings("unused")
            String username = uname.getText();
            
            @SuppressWarnings("unused")
            String password = new String(pswd.getPassword());
            //server = srvr.getText();
            //port = prt.getText();
            cancelled = false;
            firePropertyChange(LoginHandler.LOGIN, false, true);
            this.dispose();
            if (f != null) f.dispose();
        }
        if(event.getSource() == quitBtn)
        {
            cancelled = true;
            firePropertyChange(LoginHandler.LOGIN_CANCELLED, false, true);
            this.dispose();
            if (f != null) f.dispose();
        }

        if(event.getSource() == configBtn)
        {
            ServerDialog serverDialog = new ServerDialog(config.getServerList());
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
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
        String e = evt.getPropertyName();
        if (ServerDialog.REMOVE_PROPERTY.equals(e)) {
            String oldValue = (String) evt.getOldValue();
            config.removeServer(oldValue);
            if (config.hostname.get().equals(oldValue)) 
                config.hostname.set(DEFAULT_SERVER_TEXT);
        }
    }
}
