/*
 * org.openmicroscopy.shoola.util.ui.login.ScreenLogin 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.util.ui.login;


//Java imports
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JTextPane;


//Third-party libraries
import layout.TableLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.IconManager;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * The login frame.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class ScreenLogin 
	extends JFrame
	implements ActionListener, PropertyChangeListener
{

	/** Bounds property indicating this window is moved to the front. */
	public static final String		TO_FRONT_PROPERTY = "toFront";
	
	/** Bounds property indicating to log in. */
	public static final String 		LOGIN_PROPERTY = "login";
	
	/** Bounds property indicating to log in. */
	public static final String 		QUIT_PROPERTY = "quit";
	
	/** Identifies the user name field. */
	public static final int			USERNAME_FIELD = 0;
	
	/** Identifies the passwrod field. */
	public static final int			PASSWORD_FIELD = 1;
	
	 /** Default text if no server. */
    public static final String		DEFAULT_SERVER = "Add a new server ->";
    
    /** The property name for the user who connects to <i>OMERO</i>. */
    private static final String  	OMERO_USER= "omeroUser";
     
	/** The font color for text. */
    static final Color      		TEXT_COLOR   = Color.WHITE;
		
    /** The size of the font for the version. */
    private static final float		VERSION_FONT_SIZE   = 10;
    
    /** The size of the font for the text. */
    private static final int      	TEXT_FONT_SIZE   = 18;
    
    /** The login text. */
    private static final String		TEXT_LOGIN = "Please Log In";
    
    /** The username text. */
    private static final String		USER_TEXT = "Username: ";
    
    /** The password text. */
    private static final String		PASSWORD_TEXT = "Password: ";

    /** Text field to enter the login user name. */
	private JTextField          user;
	
	/** Password field to enter login password. */
	private JPasswordField      pass;
	
	/** Button to bring up the <code>ServerDialog</code>. */
	private JButton				configButton;
	
	/** Button to login to server. */
	private JButton             login;
	
    /** Button to quit the application. */
	private JButton             cancel;
	
    /** The name of the server or default value if none already defined. */
    private String				serverName;
    
    /** Field hosting the server text. */
    private JTextPane 			serverText;
    
    /** UI component hosting the version of the sotfware. */
    private JTextPane 			versionInfo;
    
    /** Reference to the editor hosting the table. */
	private ServerEditor		editor;
    
    /** Quits the application. */
    private void quit()
    {
    	firePropertyChange(QUIT_PROPERTY, Boolean.FALSE, Boolean.TRUE);
    }
    
    /** Atempts to log in. */
    private void login()
    {
    	firePropertyChange(TO_FRONT_PROPERTY, Boolean.FALSE, 
								Boolean.TRUE);
		requestFocusOnField();
		
    	StringBuffer buf = new StringBuffer();
        buf.append(pass.getPassword());
        String usr = user.getText().trim(), psw = buf.toString();
        String s = serverText.getText().trim();
        setControlsEnabled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        LoginCredentials lc = new LoginCredentials(usr, psw, s);
        setUserName(usr);
        firePropertyChange(LOGIN_PROPERTY, null, lc);
    }
    
    /** 
     * Bings up the server dialog to select an existing server or enter
     * a new server address.
     */
    private void config()
    {
    	ServerDialog d = new ServerDialog(editor);
    	d.addPropertyChangeListener(this);
    	UIUtilities.centerAndShow(d);
    }
    
    /** Adds listeners to the UI components. */
    private void initListeners()
    {
		login.addActionListener(this);
		user.addActionListener(this);
		pass.addActionListener(this);
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { quit(); }
		});
		configButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { config(); }
		});
		addWindowListener(new WindowAdapter()
        {
        	public void windowOpened(WindowEvent e) {
        		requestFocusOnField();
        	} 
        });
		user.addMouseListener(new MouseAdapter() {
		
			/**
			 * Selects the user's name if it exists.
			 * @see MouseListener#mousePressed(MouseEvent)
			 */
			public void mousePressed(MouseEvent e) {
				if (user.getText() != null) {
					user.selectAll();
				}
			}
			
			/**
			 * Fires a property to move the window to the front.
			 * @see MouseListener#mouseClicked(MouseEvent)
			 */
			public void mouseClicked(MouseEvent e) {
				firePropertyChange(TO_FRONT_PROPERTY, Boolean.FALSE, 
									Boolean.TRUE);
				user.requestFocus();
			}
			
		});
		pass.addMouseListener(new MouseAdapter() {

			/**
			 * Fires a property to move the window to the front.
			 * @see MouseListener#mouseClicked(MouseEvent)
			 */
			public void mouseClicked(MouseEvent e) {
				firePropertyChange(TO_FRONT_PROPERTY, Boolean.FALSE, 
									Boolean.TRUE);
				requestFocusOnField();
			}
		});
		
    }
    
    /**
     * Removes border and margin for the specified button and sets the default
     * cursor to {@link Cursor#HAND_CURSOR}.
     * 
     * @param button	The button to set the default for.
     */
    private void setButtonDefault(JButton button)
    {
        //Next two statements get rid of surrounding border.
        button.setOpaque(false);
        button.setRolloverEnabled(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
    
    /** Creates and initializes the login button and the cancel button. */
    private void initButtons()
    {
    	login = new JButton("Login");
    	login.setMnemonic('L');
    	login.setToolTipText("Login");
    	setButtonDefault(login);
    	UIUtilities.enterPressesWhenFocused(login);
    	cancel = new JButton("Quit");
    	cancel.setMnemonic('Q');
    	cancel.setToolTipText("Quit the Application");
    	setButtonDefault(cancel);
    	configButton = new JButton();
    	configButton.setMnemonic('X');
    	configButton.setToolTipText("Config Server");
    	configButton.setBorderPainted(false);
    	configButton.setBorder(null);
    	configButton.setMargin(new Insets(0, 0, 0, 0));
    	configButton.setFocusPainted(false);
    	configButton.setContentAreaFilled(false);
    	IconManager icons = IconManager.getInstance();
    	configButton.setIcon(icons.getIcon(IconManager.CONFIG));
    	configButton.setPressedIcon(icons.getIcon(IconManager.CONFIG_PRESSED));
    	getRootPane().setDefaultButton(login);
    }
    
    /** Creates and initializes the login fields. 
     * 
     * @param userName The name of the user.
     */
    private void initFields(String userName)
    {
    	user = new JTextField(20);
    	user.setText(userName);
    	user.setToolTipText("Enter your username.");
    	pass = new JPasswordField();
    	pass.setToolTipText("Enter your password.");
    	List<String> servers = editor.getServers();
    	if (servers == null || servers.size() == 0)
    		serverName = DEFAULT_SERVER;
    	else serverName = servers.get(servers.size()-1);
        serverText = UIUtilities.buildTextPane(serverName, TEXT_COLOR);
    }
    
    /**
     * Builds and lays out the specified text field.
     * 
     * @param field		The field to lay out.
     * @param mnemonic	The mnemonic value.
     * @param s			The value to display in front of the field.
     * @return See above.
     */
    private JPanel buildTextPanel(JTextField field, int mnemonic, String s)
    {
    	double[][] size = new double[][]{{TableLayout.PREFERRED, 
    									TableLayout.FILL}, {30}};
        
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        TableLayout layout = new TableLayout(size);
        panel.setLayout(layout);       

        JLabel label = UIUtilities.setTextFont(s);
        label.setForeground(TEXT_COLOR);
        label.setDisplayedMnemonic(mnemonic);
        
        label.setLabelFor(field);
        label.setOpaque(false);
        panel.add(label, "0, 0, r, c");        
        panel.add(field, "1, 0, f, c");
        return panel;
    }
    
    /**
     * Builds and lays out the panel hosting the login information.
     * 
     * @return See above.
     */
    private JPanel buildTopPanel()
    {
    	double topTable[][] =  {{245, 18, 220, 28}, // columns
    							{32, TableLayout.FILL}}; // rows
    	JPanel topPanel = new JPanel();
    	topPanel.setOpaque(false);
        topPanel.setLayout(new TableLayout(topTable));   
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        JTextPane pleaseLogIn = UIUtilities.buildTextPane(TEXT_LOGIN, 
        												TEXT_COLOR);
        Font f = pleaseLogIn.getFont();
        Font newFont = f.deriveFont(Font.BOLD, TEXT_FONT_SIZE);
        pleaseLogIn.setFont(newFont);
        topPanel.add(pleaseLogIn, "0, 0, l, c"); //Add to panel.
        
        topPanel.add(serverText, "2, 0, r, c"); //Add to panel.
        topPanel.add(configButton, "3, 0, c, c");
        topPanel.add(buildTextPanel(user, 'U', USER_TEXT), "0, 1, 0, 1");
        topPanel.add(buildTextPanel(pass, 'P', PASSWORD_TEXT), "2, 1, 3, 1");
    	return topPanel;
    }
    
    /**
     * Builds the UI component hosting the buttons.
     * 
     * @param version The version of the software.
     * @return See above.
     */
    private JPanel buildMainPanel(String version)
    {
        double mainTable[][] =
                {{TableLayout.FILL, 100, 5, 100}, // columns
                {TableLayout.FILL, 30}}; // rows
    	JPanel mainPanel = new JPanel();
    	mainPanel.setLayout(new TableLayout(mainTable));       
    	mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
    	mainPanel.setOpaque(false);
    	mainPanel.add(login, "1, 1, f, c");
    	mainPanel.add(cancel, "3, 1, f, c");

    	versionInfo = UIUtilities.buildTextPane(version, TEXT_COLOR);
    	Font f = versionInfo.getFont();
    	Font newFont = f.deriveFont(VERSION_FONT_SIZE);
    	versionInfo.setFont(newFont);
    	mainPanel.add(versionInfo, "0, 1, l, b");
    	mainPanel.add(buildTopPanel(), "0, 0, 3, 0");
    	return mainPanel;
    }
    
    /** 
     * Lays out the widgets and positions the window at the centre of
     * the screen.
     * 
     * @param logo 		The Frame's background logo. 
     * @param version	The version of the software.
     */
    private void buildGUI(Icon logo, String version)
    {
        JLabel background = new JLabel(logo);
        background.setBorder(BorderFactory.createEmptyBorder());
        JLayeredPane layers = new JLayeredPane();  //Default is absolute layout.
        int width = logo.getIconWidth();
        int height = logo.getIconHeight();
        layers.setBounds(0, 0, width, height);
        JPanel p = buildMainPanel(version);
        background.setBounds(0, 0, width, height);
        p.setBounds(0, 0, width, height);
        
        layers.add(background, new Integer(0));
        layers.add(p, new Integer(1));
        getContentPane().add(layers); 
    }
    
    /** 
     * Returns the server's name.
     * 
     * @return See above.
     */
    private String getServerName()
    {
    	String s = serverText.getText();
    	if (s == null) return null;
    	return s.trim();
    }
    
    /** 
     * Sets the value of the new server
     * 
     * @param s The value to set.
     */
    private void setNewServer(String s)
    {
    	if (s == null || s.length() == 0) s = DEFAULT_SERVER;
    	serverText.setText(s);
    	serverText.validate();
    	serverText.repaint();
    }

	/**
	 * Sets the name of the user in the preferences.
	 * 
	 * @param name The name to set.
	 */
	private void setUserName(String  name)
	{
		if (name == null) return;
		Preferences prefs = Preferences.userNodeForPackage(ScreenLogin.class);
        prefs.put(OMERO_USER, name);
	}
	
	/**
	 * Returns the name of the user if saved.
	 * 
	 * @return See above.
	 */
	private String getUserName()
	{
		Preferences prefs = Preferences.userNodeForPackage(ScreenLogin.class);
        return prefs.get(OMERO_USER, null);
	}
	
	/** 
	 * Sets the focus on the username field if no user name entered
	 * otherwise, sets the focus on the password field.
	 */
	private void requestFocusOnField()
	{
		String txt = user.getText();
		if (txt == null || txt.trim().length() == 0)
			user.requestFocus();
		else pass.requestFocus();
	}

    /**
     * Creates a new instance.
     * 
     * @param title		The frame's title.
     * @param logo		The frame's background logo. 
     * 					Mustn't be <code>null</code>.
     * @param version	The version of the software.
     */
    public ScreenLogin(String title, Icon logo, String version)
    {
    	super();
    	setTitle(title);
    	if (logo == null)
			throw new NullPointerException("No Frame icon.");
    	Dimension d = new Dimension(logo.getIconWidth(), logo.getIconHeight());
		setSize(d);
		setPreferredSize(d);
		editor = new ServerEditor();
		editor.addPropertyChangeListener(ServerEditor.REMOVE_PROPERTY, this);
		initFields(getUserName());
		initButtons();
		initListeners();
		buildGUI(logo, version);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(false);
		setUndecorated(true);
		toFront();
		addMouseListener(new MouseAdapter() {
			
			/**
			 * Fires a property to move the window to the front.
			 * @see MouseListener#mouseClicked(MouseEvent)
			 */
			public void mouseClicked(MouseEvent e) {
				firePropertyChange(TO_FRONT_PROPERTY, Boolean.FALSE, 
									Boolean.TRUE);
				requestFocusOnField();
			}
		});
    }
    
    /**
     * Creates a new instance.
     * 
     * @param title		The frame's title.
     * @param logo		The frame's background logo. 
     * 					Mustn't be <code>null</code>.
     * @param version	The version of the software.
     */
    public ScreenLogin(String title, Icon logo)
    {
    	this(title, logo, null);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param logo		The frame's background logo. 
     * 					Mustn't be <code>null</code>.
     * @param version	The version of the software.
     */
    public ScreenLogin(Icon logo, String version)
    {
    	this(null, logo, version);
    }

    /**
     * Creates a new instance.
     * 
     * @param logo	The frame's background logo. Mustn't be <code>null</code>.
     */
    public ScreenLogin(Icon logo)
    {
    	this(null, logo, null);
    }
    
    /**
     * Sets whether or not the buttons composing the display areenabled.
     * 
     * @param b Pass <code>true</code> if this component should be enabled, 
     * 			<code>false</code> otherwise.
     */
    public void setControlsEnabled(boolean b)
    {
    	 user.setEnabled(b);
         pass.setEnabled(b);
         login.setEnabled(b);
         login.requestFocus();
         configButton.setEnabled(b);
    }
    
    /** Sets the text of all textFields to <code>null</code>. */
    public void cleanFields()
    {
    	setCursor(Cursor.getDefaultCursor());
        user.setText("");
        pass.setText("");
    }
    
    /**
     * Sets the text of the textField corresponding to the specified id
     * to <code>null</code>.
     * 
     * @param fieldID 	The textField's id. One of the following constants:
     * 					{@link #USERNAME_FIELD} or {@link #PASSWORD_FIELD}.
     */
    public void cleanField(int fieldID)
    {
    	setCursor(Cursor.getDefaultCursor());
    	switch (fieldID) {
			case USERNAME_FIELD:
				user.setText("");
				break;
			case PASSWORD_FIELD:
				pass.setText("");
				break;
			default:
				cleanFields();	
		}
    }
    
    /** Closes and disposes. */
    public void close()
    {
    	setVisible(false);
    	dispose();
    }
    
    /**
     * Reacts to property changes fired by the <code>ScreenDialog</code>
     * window.
     * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
     */
	public void propertyChange(PropertyChangeEvent evt) 
	{
		String name = evt.getPropertyName();
		if (ServerDialog.SERVER_PROPERTY.equals(name)) {
			String v = getServerName();
			String s = (String) evt.getNewValue();
			if (s == null) {
				setNewServer(null);
				return;
			}
			String trim = s.trim();
			if (v.equals(trim)) return;
			setNewServer(trim);
		} else if (ServerEditor.REMOVE_PROPERTY.equals(name)) {
			requestFocusOnField();
			String v = getServerName();
			String oldValue = (String) evt.getOldValue();
			if (v.equals(oldValue)) 
				setNewServer((String) evt.getNewValue());
		} 
	}

	/** 
	 * Handles action events fired by the login fields and button.
	 * Once user name and password have been entered, the login fields and
	 * button will be disabled.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) { login(); }
    
}
