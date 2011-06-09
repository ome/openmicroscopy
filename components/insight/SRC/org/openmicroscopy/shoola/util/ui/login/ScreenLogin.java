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
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.prefs.Preferences;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

//Third-party libraries
import info.clearthought.layout.TableLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.StringComparator;
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
	implements ActionListener, DocumentListener, PropertyChangeListener
{
	
	/** Bounds property indicating this window is moved to the front. */
	public static final String		TO_FRONT_PROPERTY = "toFront";
	
	/** Bounds property indicating to log in. */
	public static final String 		LOGIN_PROPERTY = "login";
	
	/** Bounds property indicating to log in. */
	public static final String 		QUIT_PROPERTY = "quit";
	
	/** Identifies the user name field. */
	public static final int			USERNAME_FIELD = 0;
	
	/** Identifies the password field. */
	public static final int			PASSWORD_FIELD = 1;
	
	/** Default text if no server. */
	public static final String		DEFAULT_SERVER = "Add a new server ->";

	/** The font color for text. */
	static final Color      		TEXT_COLOR = Color.WHITE;

	/** The default size of the window. */
	static final Dimension			DEFAULT_SIZE = new Dimension(551, 113);
	
	/** The default color for the foreground. */
	private final static Color		FOREGROUND_COLOR = Color.DARK_GRAY;
	
	/** The property name for the user who connects to <i>OMERO</i>. */
	private static final String  	OMERO_USER = "omeroUser";

	/** The property name for the connection speed used to connect to server. */
	private static final String  	OMERO_CONNECTION_SPEED = 
													"omeroConnectionSpeed";
	
	/** The property name for the user who connects to <i>OMERO</i>. */
	private static final String  	OMERO_USER_GROUP = "omeroUserGroup";
	
	/** 
	 * The property name to indicated that the data transfer is
	 * encrypted or not. 
	 */
	private static final String  	OMERO_TRANSFER_ENCRYPTED = 
		"omeroTransferEncrypted";
	
	/** The size of the font for the version. */
	private static final float		VERSION_FONT_SIZE = 14;

	/** The style of the font for the version. */
	private static final int		VERSION_FONT_STYLE = Font.BOLD;

	/** The size of the font for the text. */
	private static final int      	TEXT_FONT_SIZE = 16;

	/** The login text. */
	private static final String		TEXT_LOGIN = "Log In";

	/** The group name text. */
	private static final String		GROUP_TEXT = "Group: ";
	
	/** The user name text. */
	private static final String		USER_TEXT = "Username: ";

	/** The password text. */
	private static final String		PASSWORD_TEXT = "Password: ";

	/** The number of column of the text field. */
	private static final int		TEXT_COLUMN = 12;
	
	/** The maximum number of characters.*/
	private static final int 		MAX_CHAR = 50;
	
	/** Text field to enter the login user name. */
	private static JTextField          user;

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
	private static JTextPane 	serverText;
	
	/** The UI component hosting the server text. */
	private JPanel 				serverTextPane;

	/** Field hosting the server text. */
	private JLabel 				connectionSpeedText;
	
	/** UI component hosting the version of the software. */
	private JTextPane 			versionInfo;

	/** Reference to the editor hosting the table. */
	private ServerEditor		editor;

	/** The selected connection speed. */
	private int					speedIndex;
	
	/** The port value. */
	private int					selectedPort;
	
	/** Indicates to show or hide the connection speed option. */
	private boolean				connectionSpeed;
	
	/** The default foreground color. */
	private Color				defaultForeground;
	
	/** The groups the user is a member of. */
	private JComboBox			groupsBox;
	
	//private JButton				groupsBox;
	
	/** The component displaying the login text. */
	private JTextPane 			pleaseLogIn;
	
	/** The groups the user is member of. */
	private Map<Long, String>	groups;
	
	/** The name read from the system property if previously set. */
	private String				originalName;
	
	/** The name read from the system property if previously set. */
	private String				originalServerName;
	
	/** The component displaying the login option. */
	private List<JComponent> 	ref;
	
	/** The component displaying the controls. */
	private JPanel 				mainPanel;

	/** The possible groups. */
	private String[]			groupValues;

	/** 
	 * Button indicating that the transfer of data is secured or not
	 * depending on the selected status. 
	 */
	private JButton				encryptedButton;
	
	/** Flag indicating that the transfer of data is secured or not. */
	private boolean				encrypted;
	
	/** The map hosting the real group names.*/
	private Map<Integer, String> groupNames;
	
	/** Quits the application. */
	private void quit()
	{
		String usr = user.getText().trim();
		String server = serverText.getText();
		if (usr == null) usr = "";
		if (server == null) server = "";
		//if (!server.equals(originalServerName) || !usr.equals(originalName))
			//registerGroup(null);
	
		firePropertyChange(QUIT_PROPERTY, Boolean.valueOf(false), 
				Boolean.valueOf(true));
	}

	/** Attempts to log in. */
	private void login()
	{
		firePropertyChange(TO_FRONT_PROPERTY, Boolean.valueOf(false),
				Boolean.valueOf(true));
		requestFocusOnField();
		StringBuffer buf = new StringBuffer();
		buf.append(pass.getPassword());
		String usr = user.getText().trim(), psw = buf.toString();
		String s = serverText.getText();
		if (usr == null || usr.length() == 0 ||
				psw == null || psw.length() == 0 ||
				s == null || s.trim().length() == 0 ||
				s.trim().equals(DEFAULT_SERVER)) {
			requestFocusOnField();
			return;
		}
		if (usr != null) usr = usr.trim();
		if (s != null) s = s.trim();
		setControlsEnabled(false);
		LoginCredentials lc;
		if (groupsBox == null) {
			lc = new LoginCredentials(usr, psw, s, speedIndex, 
					selectedPort, encrypted);
		} else {
			long id = -1L;
			if (hasGroupOption() && groupsBox.isVisible()) 
				//id = getGroupId(groupsBox.getText());
				id = getGroupId(groupNames.get(groupsBox.getSelectedIndex()));
			
			lc = new LoginCredentials(usr, psw, s, speedIndex, 
					selectedPort, id, encrypted);
		}
		setUserName(usr);
		setEncrypted();
		setControlsEnabled(false);
		firePropertyChange(LOGIN_PROPERTY, null, lc);
	}

	/**
	 * Returns the identifier of the group corresponding to the passed value.
	 * 
	 * @param value The name of the
	 * @return
	 */
	private Long getGroupId(String value)
	{
		Entry entry;
		Iterator i = groups.entrySet().iterator();
		while (i.hasNext()) {
			entry = (Entry) i.next();
			if (entry.getValue().equals(value))
				return (Long) entry.getKey();
		}
		return -1L;
	}
	
	/** 
	 * Returns <code>true</code> if the group option can be displayed if
	 * available, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	private boolean hasGroupOption()
	{
		String usr = user.getText().trim();
		String s = serverText.getText().trim();
		return (usr.equals(originalName) && s.equals(originalServerName));
	}
	
	/** 
	 * Brings up the server dialog to select an existing server or enter
	 * a new server address.
	 */
	private void config()
	{
		ServerDialog d;
		String s = serverText.getText().trim();
		if (connectionSpeed) 
			d = new ServerDialog(this, editor, s, speedIndex);
		else d = new ServerDialog(this, editor, s);
		d.addPropertyChangeListener(this);
		UIUtilities.centerAndShow(d);
	}

	/**
	 * Sets the {@link #encrypted} flag and modifies the icon of the 
	 * {@link #encryptedButton} accordingly.
	 */
	private void encrypt()
	{
		encrypted = !encrypted;
		IconManager icons = IconManager.getInstance();
		if (encrypted) 
			encryptedButton.setIcon(icons.getIcon(IconManager.ENCRYPTED_24));
		else encryptedButton.setIcon(icons.getIcon(IconManager.DECRYPTED_24));
	}
	
	/** Adds listeners to the UI components. */
	private void initListeners()
	{
		user.getDocument().addDocumentListener(this);
		pass.getDocument().addDocumentListener(this);
		login.addActionListener(this);
		user.addActionListener(this);
		pass.addActionListener(this);
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { quit(); }
		});
		configButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { config(); }
		});
		encryptedButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { encrypt(); }
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
				if (user.getText() != null) 
					user.selectAll();
			}

			/**
			 * Fires a property to move the window to the front.
			 * @see MouseListener#mouseClicked(MouseEvent)
			 */
			public void mouseClicked(MouseEvent e) {
				firePropertyChange(TO_FRONT_PROPERTY, Boolean.FALSE, 
									Boolean.TRUE);
				user.requestFocus();
				if (user.getText() != null) 
					user.selectAll();
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
				//requestFocusOnField();
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
		//button.setOpaque(true);
		button.setRolloverEnabled(false);
		button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	}
	
	/** Initializes the groups.*/
	private void initializeGroups()
	{
		groups = getGroups();
		if (groups != null && groups.size() > 1) {
			groupNames = new HashMap<Integer, String>();
			groupValues = new String[groups.size()];
			Entry entry;
			Iterator i = groups.entrySet().iterator();
			int index = 0;
			while (i.hasNext()) {
				entry = (Entry) i.next();
				groupValues[index] = (String) entry.getValue();
				index++;
			}
			String selectedGroup = groupValues[groupValues.length-1];
			
			//Review the groups
			Arrays.sort(groupValues, new StringComparator());
			String[] sorted = new String[groupValues.length];
			String v;
			String value;
			for (int j = 0; j < groupValues.length; j++) {
				v = groupValues[j];
				value = v;
				if (v.length() > MAX_CHAR) {
					value = v.substring(0, MAX_CHAR)+"...";
				}
				groupNames.put(j, v);
				sorted[j] = value;
				if (selectedGroup.equals(v)) {
					selectedGroup = value;
				}
			}
			
			
			
			groupsBox = new JComboBox(sorted);
			groupsBox.setSelectedItem(selectedGroup);
		} else {
			if (groupNames != null) groupNames.clear();
			groupsBox = null;
		}
	}
	
	/** 
	 * Creates and initializes the components
	 * 
	 * @param userName The name of the user.
	 * @param hostName The default hostName.
	 */
	private void initialize(String userName, String hostName)
	{
		originalName = userName;
		user = new JTextField();
		user.setText(userName);
		user.setToolTipText("Enter your username.");
		user.setColumns(TEXT_COLUMN);
		pass = new JPasswordField();
		pass.setToolTipText("Enter your password.");
		pass.setColumns(TEXT_COLUMN);
		Map<String, String> servers = editor.getServers();
		if (hostName != null && hostName.trim().length() > 0) {
			serverName = hostName;
			//if user did point to another server
			if (servers != null && servers.size() > 0) {
				int n = servers.size()-1;
				Iterator<String> i = servers.keySet().iterator();
				int k = 0;
				String value;
				while (i.hasNext()) {
					serverName = i.next();
					if (k == n) {
						value = servers.get(serverName);
						if (value != null) {
							try {
								selectedPort = Integer.parseInt(value);
							} catch (Exception e) {}
						}
					}
					k++;
				}
			} else {
				editor.removeLastRow();
				editor.addRow(hostName);
			}
		} else {
			if (servers == null || servers.size() == 0) 
				serverName = DEFAULT_SERVER;
			else {
				int n = servers.size()-1;
				Iterator<String> i = servers.keySet().iterator();
				int k = 0;
				String value;
				while (i.hasNext()) {
					serverName = i.next();
					if (k == n) {
						value = servers.get(serverName);
						if (value != null) {
							try {
								selectedPort = Integer.parseInt(value);
							} catch (Exception e) {}
						}
					}
					k++;
				}
			}
		}
		
		if (!DEFAULT_SERVER.equals(serverName))
			originalServerName = serverName;
		connectionSpeedText = new JLabel(getConnectionSpeed());
		connectionSpeedText.setForeground(TEXT_COLOR);
		connectionSpeedText.setBorder(
				BorderFactory.createEmptyBorder(5, 0, 0, 0));
		serverText = UIUtilities.buildTextPane(serverName, TEXT_COLOR);
		serverTextPane = UIUtilities.buildComponentPanelRight(serverText, 
				false);
		serverTextPane.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
		
		initializeGroups();
		ref = new ArrayList<JComponent>();
		login = new JButton("Login");
		defaultForeground = login.getForeground();
		login.setMnemonic('L');
		login.setToolTipText("Login");
		setButtonDefault(login);
		UIUtilities.enterPressesWhenFocused(login);
		UIUtilities.opacityCheck(login);
		cancel = new JButton("Quit");
		cancel.setMnemonic('Q');
		cancel.setToolTipText("Cancel Login.");
		setButtonDefault(cancel);
		UIUtilities.opacityCheck(cancel);
		configButton = new JButton();
		configButton.setMnemonic('X');
		configButton.setToolTipText("Enter the server's address.");
		configButton.setBorderPainted(false);
		configButton.setBorder(null);
		//configButton.setMargin(new Insets(1, 1, 1, 1));
		configButton.setFocusPainted(false);
		configButton.setContentAreaFilled(false);
		IconManager icons = IconManager.getInstance();
		configButton.setIcon(icons.getIcon(IconManager.CONFIG_24));
		//configButton.setPressedIcon(icons.getIcon(
		//		IconManager.CONFIG_PRESSED_24));
		
		encrypted = !isEncrypted();
		encryptedButton = new JButton();
		List<String> tips = new ArrayList<String>();
		tips.add("The connexion to the server is always encrypted.");
		tips.add("If selected, the data transfer (e.g. annotations, images) " +
				"will also be encrypted.");
		tips.add("But the transfer will be much slower.");
		
		encryptedButton.setToolTipText(UIUtilities.formatToolTipText(tips));
		encryptedButton.setBorderPainted(false);
		encryptedButton.setBorder(null);
		encryptedButton.setFocusPainted(false);
		encryptedButton.setContentAreaFilled(false);
		if (encrypted) 
			encryptedButton.setIcon(icons.getIcon(IconManager.ENCRYPTED_24));
		else encryptedButton.setIcon(icons.getIcon(IconManager.DECRYPTED_24));
		getRootPane().setDefaultButton(login);
		enableControls();
		
	}

	/** 
	 * Layouts the groups or login options.
	 * 
	 * @param group Pass <code>true</code> to display the groups options if 
	 * 				available, <code>false</code> otherwise.
	 */
	private void layout(boolean group)
	{
		if (mainPanel == null) return;
		if (ref == null) return;
		Iterator<JComponent> i = ref.iterator();
		boolean visible = false;
		if (group) {
			if (groups != null && groupsBox != null) visible = true;
		}
		while (i.hasNext()) {
			i.next().setVisible(visible);
		}
		mainPanel.validate();
		mainPanel.repaint();
	}
	
	/**
	 * Builds the UI component hosting the buttons.
	 * 
	 * @param version The version of the software.
	 * @return See above.
	 */
	private JPanel buildMainPanel(String version)
	{
		mainPanel = new JPanel();
		int g = 10;
		int t = 10;
		mainPanel.setBorder(BorderFactory.createEmptyBorder(t, g, t, g));
		mainPanel.setOpaque(false);
		double[][] size = {{TableLayout.PREFERRED, TableLayout.FILL, 
			TableLayout.PREFERRED, 
			TableLayout.FILL, TableLayout.FILL, TableLayout.PREFERRED}, 
				{TableLayout.PREFERRED, TableLayout.PREFERRED,
				TableLayout.PREFERRED, TableLayout.PREFERRED}};
		TableLayout layout = new TableLayout(size);
		mainPanel.setLayout(layout);
		Font f;
		JTextPane l;
		pleaseLogIn = UIUtilities.buildTextPane(TEXT_LOGIN, 
				TEXT_COLOR);
		f = pleaseLogIn.getFont();
		pleaseLogIn.setFont(f.deriveFont(Font.BOLD, TEXT_FONT_SIZE));
		mainPanel.add(pleaseLogIn, "0, 0, LEFT, CENTER");
		
		versionInfo = UIUtilities.buildTextPane(version, TEXT_COLOR);
		f = versionInfo.getFont();
		versionInfo.setFont(f.deriveFont(VERSION_FONT_STYLE, 
				VERSION_FONT_SIZE));
		
		JPanel p = new JPanel();
		p.setOpaque(false);
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		p.add(serverTextPane);
		p.add(connectionSpeedText);
		mainPanel.add(UIUtilities.buildComponentPanelRight(p, 0, 0, false), 
				"0, 0, 4, 0");
		JToolBar bar = new JToolBar();
		bar.setOpaque(false);
		bar.setBorder(null);
		bar.setFloatable(false);
		bar.add(encryptedButton);
		bar.add(configButton);
		mainPanel.add(bar, "5, 0, LEFT, TOP");
		
		//second row
		l = UIUtilities.buildTextPane(GROUP_TEXT, TEXT_COLOR);
		mainPanel.add(l, "0, 1, LEFT, CENTER");
		ref.add(l);
		if (groupsBox != null) {
			JPanel gp = UIUtilities.buildComponentPanel(groupsBox, 0, 0);
			gp.setBorder(null);
			gp.setOpaque(false);
			mainPanel.add(gp, "1, 1, 5, 1");
			ref.add(groupsBox);
		}
		//third row
		l = UIUtilities.buildTextPane(USER_TEXT, TEXT_COLOR);
		
		mainPanel.add(l, "0, 2, LEFT, CENTER");
		mainPanel.add(user, "1, 2, 2, 2");
		l = UIUtilities.buildTextPane(" "+PASSWORD_TEXT, TEXT_COLOR);
		mainPanel.add(l, "3, 2, RIGHT, CENTER");
		mainPanel.add(pass, "4, 2, 5, 2");
		//4th row
		
		JPanel cPanel = new JPanel();
		cPanel.setOpaque(false);
		cPanel.add(Box.createHorizontalGlue());
		cPanel.add(login);
		cPanel.add(cancel);
		
		double[][] s = {{TableLayout.FILL, TableLayout.PREFERRED},
				{TableLayout.PREFERRED}};
		p = new JPanel();
		p.setOpaque(false);
		p.setLayout(new TableLayout(s));
		p.add(versionInfo, "0, 0, LEFT, CENTER");
		p.add(UIUtilities.buildComponentPanelRight(cPanel, 0, 0, false), 
				"1, 0, RIGHT, CENTER");
		mainPanel.add(p, "0, 3, 5, 3");
		
		layout(groups != null);
		return mainPanel;
	}

	/** 
	 * Lays out the widgets and positions the window in the middle of
	 * the screen.
	 * 
	 * @param logo 		The Frame's background logo. 
	 * @param version	The version of the software.
	 */
	private void buildGUI(Icon logo, String version)
	{
		JLabel background;
		JLayeredPane layers = new JLayeredPane();  //Default is absolute layout.
		int width;
		int height;
		if (logo != null) {
			background = new JLabel(logo);
			width = logo.getIconWidth();
			height = logo.getIconHeight();
		} else {
			background = new JLabel();
			width = DEFAULT_SIZE.width;
			height = DEFAULT_SIZE.height;
		}
		background.setBorder(BorderFactory.createEmptyBorder());
		layers.setBounds(0, 0, width, height);
		JPanel p = buildMainPanel(version);
		background.setBounds(0, 0, width, height);
		p.setBounds(0, 0, width, height);

		layers.add(background, Integer.valueOf(0));
		layers.add(p, Integer.valueOf(1));
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
	 * Adds the connection speed to the passed string.
	 * 
	 * @return The value of the connection speed.
	 */
	private String getConnectionSpeed()
	{
		switch (speedIndex) {
			case LoginCredentials.HIGH: return " [LAN]";
			case LoginCredentials.MEDIUM: return " [High]";
			case LoginCredentials.LOW: return " [Low]";
		}
		return null;
	}
	
	/** 
	 * Sets the value of the new server.
	 * 
	 * @param s The value to set.
	 */
	private void setNewServer(String s)
	{
		if (s == null || s.length() == 0) s = DEFAULT_SERVER;
		String[] values = s.split(ServerEditor.SERVER_PORT_SEPARATOR, 0);
		s = values[0];
		if (values.length == 2) {
			try {
				selectedPort = Integer.parseInt(values[1]);
			} catch (Exception e) {}
		}
		serverText.setText(s);
		//serverText.validate();
		//serverText.repaint();
		serverTextPane.validate();
		serverTextPane.repaint();
		initializeGroups();
		layout(groupsBox != null);
		enableControls();
	}

	/** Sets the enabled flag of the {@link #login} button.*/
	private void enableControls()
	{
		boolean enabled = true;
		String s = serverText.getText();
		char[] name = pass.getPassword();
		String usr = user.getText().trim();
		usr = usr.trim();
		if (s == null || usr == null || name == null) {
			//login.setEnabled(false);
			//return;
			enabled = false;
		} else {
			s = s.trim();
			if (login != null) {
				if (DEFAULT_SERVER.equals(s)) {
					//login.setEnabled(false);
					//return;
					enabled = false;
				} else {
					if (usr.length() == 0 || name.length == 0) {
						//login.setEnabled(false);
						//return;
						enabled = false;
					}
				}
			}
		}
		if (enabled) {
			ActionListener[] listeners = login.getActionListeners();
			if (listeners != null) {
				boolean set = false;
				for (int i = 0; i < listeners.length; i++) {
					if (listeners[i] == this) {
						set = true;
						break;
					}
				}
				if (!set) login.addActionListener(this);
			}
			login.setForeground(defaultForeground);
		} else {
			//login.removeActionListener(this);
			login.setForeground(FOREGROUND_COLOR);
		}
		layout(hasGroupOption());
	}
	
	/**
	 * Sets the connection speed used to connect to the server.
	 * 
	 * @param speed The connection speed.
	 */
	private void setConnectionSpeed(int speed)
	{
		speedIndex = speed;
		Preferences prefs = Preferences.userNodeForPackage(ScreenLogin.class);
		prefs.put(OMERO_CONNECTION_SPEED, ""+speedIndex);
	}
	
	/**
	 * Retrieves the connection speed used to connect to the server.
	 * 
	 * @return See above.
	 */
	private int retrieveConnectionSpeed()
	{
		Preferences prefs = Preferences.userNodeForPackage(ScreenLogin.class);
		String s = prefs.get(OMERO_CONNECTION_SPEED, null);
		if (s == null || s.trim().length() == 0)
			return LoginCredentials.HIGH;
		return Integer.parseInt(s);
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
	 * Returns <code>true</code> if the data transfer is encrypted,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	private boolean isEncrypted()
	{
		Preferences prefs = Preferences.userNodeForPackage(ScreenLogin.class);
		String value = prefs.get(OMERO_TRANSFER_ENCRYPTED, null);
		if (value == null || value.trim().length() == 0) return false;
		if (value.equals("true")) return Boolean.valueOf(true);
		return Boolean.valueOf(false); 
	}
	
	/** 
	 * Sets to <code>true</code> if the data transfer is encrypted, 
	 * <code>false</code> otherwise. 
	 */
	private void setEncrypted()
	{
		String value = "false";
		if (encrypted) value = "true";
		Preferences prefs = Preferences.userNodeForPackage(ScreenLogin.class);
		prefs.put(OMERO_TRANSFER_ENCRYPTED, value);
	}
	
	/**
	 * Returns the possible groups.
	 * 
	 * @return See above.
	 */
	private Map<Long, String> getGroups()
	{
		Map<Long, String> groups = new LinkedHashMap<Long, String>();
		Preferences prefs = Preferences.userNodeForPackage(ScreenLogin.class);
		String list = prefs.get(OMERO_USER_GROUP, null);
		if (list == null || list.length() == 0)  return groups;
		String[] l = list.split(ServerEditor.SERVER_NAME_SEPARATOR, 0);
        
        if (l == null) return groups;
        int index;
        String group;
        String name;
        long id;
        String[] values;
        String userName;
        String serverName;
        String selectedName = user.getText();
        String selectedServer = serverText.getText();
        for (index = 0; index < l.length; index++) {
        	group = l[index].trim();
        	if (group.length() > 0) {
        		values = group.split(ServerEditor.SERVER_PORT_SEPARATOR, 0);
        		if (values.length == 2) {
        			name = values[1];
        			try {
						id = Long.parseLong(values[0]);
						groups.put(id, name);
					} catch (Exception e) {
						//ignore: not possible to read the group
					}
        		} else if (values.length == 4) {
        			userName = values[0];
        			serverName = values[1];
        			name = values[3];
        			if (userName.equals(selectedName) &&
        					serverName.equals(selectedServer)) {
        				try {
    						id = Long.parseLong(values[2]);
    						groups.put(id, name);
    					} catch (Exception e) {
    						//ignore: not possible to read the group
    					}
        			}
        		}
        	}
        }	
		return groups;
	}
	
	//public void registerGroup(Map<Str>)
	/** 
	 * Sets the default for the window. 
	 * 
	 * @param frameIcon The icon associated to the frame.
	 */
	private void setProperties(Image frameIcon)
	{
		setIconImage(frameIcon);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(false);
		setUndecorated(true);
		toFront();
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param title		 The frame's title.
	 * @param logo		 The frame's background logo. 
	 * 					 Mustn't be <code>null</code>.
	 * @param frameIcon  The image icon for the window.
	 * @param version	 The version of the software.
	 * @param defaultPort The default port.
	 * @param hostName   The default host name.
	 */
	public ScreenLogin(String title, Icon logo, Image frameIcon, String version,
			String defaultPort, String hostName)
	{
		super();
		selectedPort = -1;
		setTitle(title);
		Dimension d;
		if (logo != null)
			d = new Dimension(logo.getIconWidth(), logo.getIconHeight());
		else d = DEFAULT_SIZE;
		setSize(d);
		setPreferredSize(d);
		editor = new ServerEditor(defaultPort);
		editor.addPropertyChangeListener(ServerEditor.REMOVE_PROPERTY, this);
		speedIndex = retrieveConnectionSpeed();
		initialize(getUserName(), hostName);
		initListeners();
		buildGUI(logo, version);
		encrypt();
		setProperties(frameIcon);
		showConnectionSpeed(false);
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
	 * @param title		 The frame's title.
	 * @param logo		 The frame's background logo. 
	 * 					 Mustn't be <code>null</code>.
	 * @param frameIcon  The image icon for the window.
	 * @param version	 The version of the software.
	 * @param defaultPort The default port.
	 */
	public ScreenLogin(String title, Icon logo, Image frameIcon, String version,
			String defaultPort)
	{
		this(title, logo, frameIcon, version, defaultPort, null);
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param title		The frame's title.
	 * @param logo		The frame's background logo. 
	 * 					Mustn't be <code>null</code>.
	 * @param frameIcon The image icon for the window.
	 */
	public ScreenLogin(String title, Icon logo, Image frameIcon)
	{
		this(title, logo, frameIcon, null, null);
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param logo		The frame's background logo. 
	 * 					Mustn't be <code>null</code>.
	 * @param frameIcon The image icon for the window.
	 * @param version	The version of the software.
	 */
	public ScreenLogin(Icon logo, Image frameIcon, String version)
	{
		this(null, logo, frameIcon, version, null);
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param logo		The frame's background logo. 
	 * 					Mustn't be <code>null</code>.
	 * @param frameIcon The image icon for the window.
	 */
	public ScreenLogin(Icon logo, Image frameIcon)
	{
		this(null, logo, frameIcon, null, null);
	}

	/** 
	 * Indicates to show or hide the connection speed selection. 
	 * By default the speed is hidden.
	 * 
	 * @param connectionSpeed Pass <code>true</code> to show the 
	 * 						  connection speed option, <code>false</code>
	 * 						  otherwise. 						
	 */
	public void showConnectionSpeed(boolean connectionSpeed)
	{
		this.connectionSpeed = connectionSpeed;
		connectionSpeedText.setVisible(connectionSpeed);
	}
	
	/**
	 * Sets whether or not the buttons composing the display are enabled.
	 * 
	 * @param b Pass <code>true</code> if this component should be enabled, 
	 * 			<code>false</code> otherwise.
	 */
	public void setControlsEnabled(boolean b)
	{
		user.setEnabled(b);
		pass.setEnabled(b);
		enableControls();
		login.requestFocus();
		configButton.setEnabled(b);
		encryptedButton.setEnabled(b);
		if (groupsBox != null) groupsBox.setEnabled(b);
		if (b) {
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			setButtonDefault(login);
			setButtonDefault(cancel);
		} else {
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			login.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		}
	}

	/** Sets the text of all textFields to <code>null</code>. */
	public void cleanFields()
	{
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
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
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
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
	 * Sets the focus on the user name field if no user name entered
	 * otherwise, sets the focus on the password field.
	 */
	public void requestFocusOnField()
	{
		setControlsEnabled(true);
		String txt = user.getText();
		if (txt == null || txt.trim().length() == 0) user.requestFocus();
		else pass.requestFocus();
	}

	/**
	 * Sets the text of the {@link #cancel}.
	 * 
	 * @param text The text to set.
	 */
	public void setQuitButtonText(String text) 
	{
		if (text == null) return;
		text = text.trim();
		if (text.length() == 0) return;
		if (text.equals(cancel.getText())) return;
		cancel.setText(text);
		char c = text.toUpperCase().charAt(0);
		setQuitButtonMnemonic(c);
	}
	
	/**
	 * Sets the text displays when the cursor lingers over the component.
	 * 
	 * @param toolTipText The text to set.
	 */
	public void setQuitButtonToolTipText(String toolTipText)
	{
		cancel.setToolTipText(toolTipText);
	}
	
	/**
	 * Sets the keyboard mnemonic.
	 * 
	 * @param mnemonic The value to set.
	 */
	public void setQuitButtonMnemonic(int mnemonic)
	{
		cancel.setMnemonic(mnemonic);
	}

	/**
	 * Registers the passed groups.
	 * 
	 * @param groups The groups to register.
	 */
	public static void registerGroup(Map<Long, String> groups)
	{
		Preferences prefs = 
			Preferences.userNodeForPackage(ScreenLogin.class);
		if (groups == null) {
			prefs.put(OMERO_USER_GROUP, "");
			return;
		}
		Entry entry;
		String name;

		Iterator i = groups.entrySet().iterator();
		int n = groups.size()-1;
		int index = 0;
		String list = "";
		Long id;
		StringBuffer buffer = new StringBuffer();
		//need to get the 
		String value = user.getText()+ServerEditor.SERVER_PORT_SEPARATOR+
		serverText.getText()+ServerEditor.SERVER_PORT_SEPARATOR;
		while (i.hasNext()) {
			entry = (Entry) i.next();
			id = (Long) entry.getKey();
			buffer.append(value);
			buffer.append(""+id);
			buffer.append(ServerEditor.SERVER_PORT_SEPARATOR);
			if (entry.getValue() != null)
				buffer.append((String) entry.getValue());
			if (index != n) buffer.append(ServerEditor.SERVER_NAME_SEPARATOR);
			index++;
		}
		list = buffer.toString();
		if (list.length() != 0) {
			prefs.put(OMERO_USER_GROUP, "");
			prefs.put(OMERO_USER_GROUP, list);
		}	
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
		} else if (ServerDialog.CONNECTION_SPEED_PROPERTY.endsWith(name)) {
			setConnectionSpeed(((Integer) evt.getNewValue()).intValue());
			connectionSpeedText.setText(getConnectionSpeed());
		}
	}

	/**
	 * Enables the controls.
	 * @see DocumentListener#insertUpdate(DocumentEvent)
	 */
	public void insertUpdate(DocumentEvent e) { enableControls(); }

	/**
	 * Enables the controls.
	 * @see DocumentListener#removeUpdate(DocumentEvent)
	 */
	public void removeUpdate(DocumentEvent e) { enableControls(); }
	
	/** 
	 * Handles action events fired by the login fields and button.
	 * Once user name and password have been entered, the login fields and
	 * button will be disabled.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) { login(); }

	/**
	 * Required by the {@link DocumentListener} I/F but no-op implementation 
	 * in our case.
	 * @see DocumentListener#changedUpdate(DocumentEvent)
	 */
	public void changedUpdate(DocumentEvent e) {}

}
