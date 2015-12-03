/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.util.ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.UIManager;

import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.ScriptObject;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.filter.file.CppFilter;
import org.openmicroscopy.shoola.util.filter.file.CustomizedFileFilter;
import org.openmicroscopy.shoola.util.filter.file.JavaFilter;
import org.openmicroscopy.shoola.util.filter.file.MatlabFilter;
import org.openmicroscopy.shoola.util.filter.file.PythonFilter;
import org.openmicroscopy.shoola.util.ui.IconManager;
import org.openmicroscopy.shoola.util.ui.MessageBox;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.filechooser.GenericFileChooser;

/** 
 * Dialog used to select the scripts to upload to the server.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class ScriptUploaderDialog 
	extends JDialog
	implements ActionListener
{

	/** Bound property indicating to upload the script. */
	public static final String	UPLOAD_SCRIPT_PROPERTY = "uploadScript";
	
	/** Property name  for file selection change */
	public static final String FILESELECTION_PROPERTY = "SelectedFileChangedProperty";
	
    /** 
     * The size of the invisible components used to separate buttons
     * horizontally.
     */
    private static final Dimension	H_SPACER_SIZE = new Dimension(3, 10);
    
	/** The title of the dialog. */
	private static final String TITLE = "Upload Script";
	
	/** The text of the dialog. */
	private static final String TEXT = "Select the script to upload";
	
	/** Action ID indicating to upload the script to the server. */
	private static final int	SAVE = 0;
	
	/** Action ID indicating to close and disposes of the dialog. */
	private static final int	CANCEL = 1;

	/** Collection of supported filters. */
	private static final List<CustomizedFileFilter> FILTERS;
	
	static {
		FILTERS = new ArrayList<CustomizedFileFilter>();
		FILTERS.add(new CppFilter());
		FILTERS.add(new JavaFilter());
		FILTERS.add(new MatlabFilter());
		FILTERS.add(new PythonFilter());
	}
	
	/** Chooser used to select the file. */
	private GenericFileChooser chooser;
	
    /** 
     * Replaces the <code>ApproveButton</code> provided by the 
     * {@link JFileChooser} class. 
     */
    private JButton		saveButton;
    
    /** 
     * Replaces the <code>CancelButton</code> provided by the 
     * {@link JFileChooser} class. 
     */
    private JButton		cancelButton;
   
    /** The location of the script. */
    private JTextField location;
    
    /** Display the available location. */
    private JButton		locationFinder;
    
    /** The available scripts. */
    private Map<Long, String> scripts;
    
    /** The available folders. */
    private List<String> folders;
    
    /** The menu displaying the list of available folders. */
    private JPopupMenu	menu;
    
    /** Helper reference.*/
    private Registry context;
    
	/** Initializes the components. */
	private void initComponents()
	{
		folders = new ArrayList<String>();
		if (scripts != null) {
			Entry entry;
			Iterator i = scripts.entrySet().iterator();
			String[] values;
			String value;
			while (i.hasNext()) {
				entry = (Entry) i.next();
				values = UIUtilities.splitString((String) entry.getValue());
				if (values != null && values.length > 1) {
					value = values[values.length-2];
					if (!folders.contains(value))
						folders.add(value);
				}
			}
		}
		
        chooser = new GenericFileChooser();
        chooser.setAcceptAllFileFilterUsed(true);
        Iterator<CustomizedFileFilter> i = FILTERS.iterator();
        while (i.hasNext()) {
            chooser.addChoosableFileFilter(i.next());
        }
        chooser.setControlButtonsAreShown(false);
        chooser.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (FILESELECTION_PROPERTY.equals(evt.getPropertyName())) {
                    File f = chooser.getSelectedFile();
                    saveButton.setEnabled(f != null && f.isFile());
                }
            }
        });
		
		saveButton = new JButton("Upload");
		saveButton.setToolTipText(
				UIUtilities.formatToolTipText("Upload the selected script " +
						"to the server."));
		saveButton.addActionListener(this);
		saveButton.setActionCommand(""+SAVE);
		cancelButton = new JButton("Cancel");
		cancelButton.setToolTipText(
				UIUtilities.formatToolTipText("Closes the dialog."));
        cancelButton.addActionListener(this);
        cancelButton.setActionCommand(""+CANCEL);

        location = new JTextField();
        locationFinder = new JButton("Find Folder");
        locationFinder.setToolTipText("List the existing folders.");
        locationFinder.setEnabled(folders.size() > 0);
        locationFinder.addMouseListener(new MouseAdapter() {
			
			public void mouseReleased(MouseEvent e)
			{
				showFolderList(e.getPoint());
			}

		});
        
		saveButton.setEnabled(false);
	}
	
	/**
	 * Builds and lays out the controls.
	 * 
	 * @return See above.
	 */
	private JPanel buildControls()
	{
		JPanel controls = new JPanel();
    	controls.setLayout(new BorderLayout(0, 0));
    	controls.add(buildToolbar(), BorderLayout.CENTER);
    	
    	JPanel p = new JPanel();
    	p.setLayout(new BorderLayout(0, 0));
    	p.add(chooser, BorderLayout.CENTER);
    	p.add(controls, BorderLayout.SOUTH);
		return p;
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		IconManager icons = IconManager.getInstance();
		TitlePanel tp = new TitlePanel(TITLE, TEXT, 
				icons.getIcon(IconManager.UPLOAD_SCRIPT_48));
		Container c = getContentPane();
		c.setLayout(new BorderLayout(0, 0));
		c.add(tp, BorderLayout.NORTH);
		c.add(buildControls(), BorderLayout.CENTER);
		if (JDialog.isDefaultLookAndFeelDecorated()) {
            boolean supportsWindowDecorations = 
            UIManager.getLookAndFeel().getSupportsWindowDecorations();
            if (supportsWindowDecorations)
                getRootPane().setWindowDecorationStyle(
                            JRootPane.FILE_CHOOSER_DIALOG);
        }
	}
	
    /**
     * Builds the tool bar.
     * 
     * @return See above
     */
    private JPanel buildToolbar()
    {
    	JPanel bar = new JPanel();
    	bar.setBorder(null);
    	bar.add(cancelButton);
    	bar.add(Box.createRigidArea(H_SPACER_SIZE));
    	bar.add(saveButton);
    	JPanel p = UIUtilities.buildComponentPanelRight(bar);
        p.setOpaque(true);
        return p;
    }
    
	/** Closes and disposes. */
	private void close()
	{
		setVisible(false);
		dispose();
	}
	
	/** Uploads the script to the server. */
	private void upload()
	{
		File f = chooser.getSelectedFile();

		Iterator<CustomizedFileFilter> i = FILTERS.iterator();
		boolean supported = false;
		CustomizedFileFilter filter;
		String mimeType = null;
		while (i.hasNext()) {
			filter = i.next();
			if (filter.accept(f)) {
				supported = true;
				mimeType = filter.getMIMEType();
				break;
			}
		}
		if (!supported) {
			UserNotifier un = context.getUserNotifier();
			un.notifyInfo(TITLE, "The selected script does not seem to " +
					"be supported.");
			return;
		}
		
		if (scripts != null) {
			//File should not be null.
			String name = f.getName();
			Entry entry;
			Iterator j = scripts.entrySet().iterator();
			String value;
			supported = false;
			while (j.hasNext()) {
				entry = (Entry) j.next();
				value = (String) entry.getValue();
				if (value.equals(name)) {
					supported = true;
					break;
				}
			}
			if (supported) {
				MessageBox box = new MessageBox((JFrame) getOwner(), TITLE, 
						"A script with the same name already exists in " +
						"the system.\n" +
						"Do you still want to upload the script?");
				if (box.centerMsgBox() == MessageBox.NO_OPTION) 
					return;
			}
		}
		ScriptObject script = new ScriptObject(-1, f.getAbsolutePath(), 
				f.getName());
		script.setMIMEType(mimeType);

		firePropertyChange(UPLOAD_SCRIPT_PROPERTY, null, script);
		close();
	}
	
	/** Sets the properties of the dialog. */
    private void setProperties()
    {
    	setTitle(TITLE);
        setModal(true);
    }
    
    /** 
     * Displays the existing folders. 
     * 
     * @param p The location of the mouse click.
     */
    private void showFolderList(Point p)
    {
    	if (menu == null) {
    		menu = new JPopupMenu();
    		int index = CANCEL+1;
    		Iterator<String> i = folders.iterator();
    		JMenuItem item;
    		while (i.hasNext()) {
				item = new JMenuItem(i.next());
				item.setActionCommand(""+index);
				item.addActionListener(this);
				index++;
			}
    	}
    	menu.show(locationFinder, p.x, p.y);
    }
    
	/**
	 * Creates a new instance.
	 * 
	 * @param owner The owner of the dialog.
	 * @param scripts The scripts already uploaded.
	 * @param context Reference to the context.
	 */
	public ScriptUploaderDialog(JFrame owner, Map<Long, String> scripts, 
			Registry context)
	{
		super(owner);
		this.scripts = scripts;
		this.context = context;
		setProperties();
		initComponents();
		buildGUI();
		pack();
	}

	/**
	 * Uploads the script or closes the dialog.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		switch (index) {
			case CANCEL:
				close();
				break;
			case SAVE:
				upload();
				break;
				default:
					if (e.getSource() instanceof JMenuItem) {
						JMenuItem item = (JMenuItem) e.getSource();
						location.setText(item.getText());
					}
		}
	}
	
}
