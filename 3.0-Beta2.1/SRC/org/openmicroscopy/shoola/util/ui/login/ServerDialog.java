/*
 * org.openmicroscopy.shoola.util.ui.login.ServerDialog 
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
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.IconManager;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Modal dialog used to manage servers.
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
class ServerDialog 
	extends JDialog
	implements ComponentListener, PropertyChangeListener
{

	/** Bound property indicating that a new server is selected. */
	static final String 				SERVER_PROPERTY = "server";

	/** Bound property indicating that the window is closed. */
	static final String 				CLOSE_PROPERTY = "close";

	/** Bound property indicating that the window is closed. */
	static final String 				REMOVE_PROPERTY = "remove";
    
	/** The default size of the window. */
	private static final Dimension		WINDOW_DIM = new Dimension(400, 450);
	
	/** The window's title. */
	private static final String			TITLE = "Servers";
	
	/** The textual decription of the window. */
	private static final String 		TEXT = "Enter a new server or \n" +
										"select an existing one.";
	
    /** 
     * The size of the invisible components used to separate buttons
     * horizontally.
     */
    private static final Dimension  	H_SPACER_SIZE = new Dimension(5, 10);
    
	/** 
	 * The size of the invisible components used to separate widgets
	 * vertically.
	 */
	protected static final Dimension	V_SPACER_SIZE = new Dimension(1, 20);

	/** Button to close and dispose of the window. */
	private JButton			cancelButton;
	
	/** Button to select a new server. */
	private JButton			finishButton;
	
	/** Reference to the editor hosting the table. */
	private ServerEditor	editor;
    
    /** The component hosting the title and the warning messages if required. */
    private JLayeredPane    titleLayer;
    
    /** The UI component hosting the title. */
    private TitlePanel      titlePanel;
    
	/** Closes and disposes. */
	private void close()
	{
		editor.stopEdition();
		setVisible(false);
		dispose();
	}
	
	/** Fires a property indicating that a new server is selected. */
	private void apply()
	{
		editor.stopEdition();
		String server = editor.getSelectedServer();
		if (server != null) editor.handleServers(server);
		firePropertyChange(SERVER_PROPERTY, null, server);
		close();
	}
	
	/** Sets the window's properties. */
	private void setProperties()
	{
		setTitle(TITLE);
		setModal(true);
		setAlwaysOnTop(true);
	}
	
	/** Attaches the various listeners. */
	private void initListeners()
	{
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { close(); }
		
		});
		finishButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { apply(); }

		});
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter()
        {
        	public void windowClosing(WindowEvent e) { close(); }
        	public void windowOpened(WindowEvent e) { editor.initFocus(); } 
        });
		addComponentListener(this);
	}
	
	/** Initializes the UI components. */
	private void initComponents()
	{
		editor.addPropertyChangeListener(this);
		cancelButton = new JButton("Cancel");
		cancelButton.setToolTipText("Close the window.");
		
		finishButton =  new JButton("Apply");
		finishButton.setEnabled(false);
		getRootPane().setDefaultButton(finishButton);
		//layer hosting title and empty message
		IconManager icons = IconManager.getInstance();
		titleLayer = new JLayeredPane();
		titlePanel = new TitlePanel(TITLE, TEXT, 
									icons.getIcon(IconManager.CONFIG_48));
		titleLayer.add(titlePanel, new Integer(0));
	}
	
	/**
	 * Builds and lays out the tool bar.
	 * 
	 * @return See above.
	 */
	private JPanel buildToolBar()
	{
		JPanel bar = new JPanel();
        bar.setBorder(null);
        bar.add(finishButton);
        bar.add(Box.createRigidArea(H_SPACER_SIZE));
        bar.add(cancelButton);
        JPanel p = UIUtilities.buildComponentPanelRight(bar);
        p.setOpaque(true);
        return p;
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
        Container c = getContentPane();
        setLayout(new BorderLayout(0, 0));
        c.add(titleLayer, BorderLayout.NORTH);
        c.add(editor, BorderLayout.CENTER);
        c.add(buildToolBar(), BorderLayout.SOUTH);
	}
	
	/**
	 * Shows the warning message if the passed value is <code>true</code>,
	 * hides it otherwise.
	 * 
	 * @param warning 	Pass <code>true</code> to show the message, 
	 * 					<code>false</code> otherwise.	
	 * @param p			The component to add or remove.		
	 */
	private void showMessagePanel(boolean warning, JComponent p)
	{
		if (warning) {
            titleLayer.add(p, new Integer(1));
            titleLayer.validate();
            titleLayer.repaint();
        } else {
        	if (p == null) return;
        	titleLayer.remove(p);
            titleLayer.repaint();
        }
	}

	/** 
	 * Creates a new instance. 
	 * 
	 * @param frame		The parent frame. 
	 * @param editor 	The server editor. Mustn't be <code>null</code>.
	 */
	ServerDialog(JFrame frame, ServerEditor editor)
	{ 
		super(frame);
		this.editor = editor;
		setProperties();
		initComponents();
		initListeners();
		buildGUI();
		setSize(WINDOW_DIM);
	}

	/** 
     * Resizes the layered pane hosting the title when the window is resized.
     * @see ComponentListener#componentResized(ComponentEvent)
     */
	public void componentResized(ComponentEvent e) 
	{
		Rectangle r = getBounds();
		if (titleLayer == null) return;
		Dimension d  = new Dimension(r.width, ServerEditor.TITLE_HEIGHT);
	    titlePanel.setSize(d);
	    titlePanel.setPreferredSize(d);
	    titleLayer.setSize(d);
	    titleLayer.setPreferredSize(d);
	    titleLayer.validate();
	    titleLayer.repaint();
	}

	/**
	 * Reacts to property changes fired by the{@link ServerEditor}.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) 
	{
		String name = evt.getPropertyName();
		if (ServerEditor.EDIT_PROPERTY.equals(name)) {
			Boolean value = (Boolean) evt.getNewValue();
			finishButton.setEnabled(value.booleanValue());
		} else if (ServerEditor.ADD_MESSAGE_PROPERTY.equals(name)) {
			showMessagePanel(true, (JComponent) evt.getNewValue());
		}  else if (ServerEditor.REMOVE_MESSAGE_PROPERTY.equals(name)) {
			showMessagePanel(false, (JComponent) evt.getNewValue());
		} 
	}
	
	/** 
     * Required by {@link ComponentListener} interface but no-op implementation 
     * in our case. 
     * @see ComponentListener#componentShown(ComponentEvent)
     */
	public void componentShown(ComponentEvent e) {}
	
	/** 
     * Required by {@link ComponentListener} interface but no-op implementation 
     * in our case. 
     * @see ComponentListener#componentHidden(ComponentEvent)
     */
	public void componentHidden(ComponentEvent e) {}

	/** 
     * Required by {@link ComponentListener} interface but no-op implementation 
     * in our case. 
     * @see ComponentListener#componentMoved(ComponentEvent)
     */
	public void componentMoved(ComponentEvent e) {}
    
}
