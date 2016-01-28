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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import omero.gateway.SecurityContext;
import omero.log.LogMessage;

import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.OmeroImageService;
import org.openmicroscopy.shoola.env.data.model.ScriptObject;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.CommonsLangUtils;
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
    
    /** Existing scripts */
    private List<ScriptObject> scripts;
    
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
    	controls.setLayout(new BorderLayout());
    	controls.add(buildToolbar(), BorderLayout.CENTER);
    	
    	JPanel folder = new JPanel();
    	folder.setLayout(new BorderLayout());
    	JLabel l = new JLabel("Upload into Folder:");
    	folder.add(l, BorderLayout.WEST);
    	folder.add(location, BorderLayout.CENTER);
    	folder.add(locationFinder, BorderLayout.EAST);
    	
    	JPanel p = new JPanel();
    	p.setLayout(new GridBagLayout());
    	p.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    	GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        p.add(chooser, c);
        c.gridy++;
        
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
    	p.add(folder, c);
    	c.gridy++;
    	
    	p.add(controls, c);
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
		    boolean exists = false;
		    ScriptObject tmp = new ScriptObject(-1, "", f.getName());
			String name = tmp.getDisplayedName();
			for(ScriptObject s : scripts) {
				String value = s.getDisplayedName();
				if (value.equals(name)) {
				    exists = true;
					break;
				}
			}
			if (exists) {
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
		
		String value = location.getText();
		if (CommonsLangUtils.isNotEmpty(value)) 
		    script.setFolder(value.trim());
		else 
		    script.setFolder(f.getParentFile().getName());
		
		IconManager icons = IconManager.getInstance();
		script.setIcon(icons.getIcon(IconManager.UPLOAD_SCRIPT));
		

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
				menu.add(item);
			}
    	}
    	menu.show(locationFinder, p.x, p.y);
    }
    
	/**
	 * Creates a new instance.
	 * 
	 * @param owner The owner of the dialog.
	 * @param context Reference to the context.
	 * @param ctx The {@link SecurityContext}
	 */
	public ScriptUploaderDialog(JFrame owner,
			Registry context, SecurityContext ctx)
	{
		super(owner);
		this.context = context;
		setProperties();
		initComponents();
		buildGUI();
		loadScripts(ctx);
		pack();
	}

	/**
	 * Set the scripts which already exist on the server
	 * @param scripts The List of {@link ScriptObject}s
	 */
    void setScripts(List<ScriptObject> scripts) {
        this.scripts = scripts;
        
        for (ScriptObject s : scripts) {
            String folder = s.getFolder();
            if(folder.startsWith("/"))
                 folder = folder.replaceFirst("/", "");
            if(folder.endsWith("/"))
                folder = folder.substring(0, folder.length()-1);
            if (!folders.contains(folder))
                folders.add(folder);
        }
        locationFinder.setEnabled(!folders.isEmpty());
    }
	
    /**
     * Starts an async call to load the existing scripts
     * from the server
     * @param The {@link SecurityContext}
     */
    private void loadScripts(final SecurityContext ctx) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    OmeroImageService svc = context.getImageService();
                    final List<ScriptObject> scripts = svc
                            .loadAvailableScripts(ctx, -1);
                    
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            ScriptUploaderDialog.this.setScripts(scripts);
                        }
                    });

                } catch (Exception e) {
                    LogMessage m = new LogMessage("Couldn't load scripts", e);
                    context.getLogger().error(ScriptUploaderDialog.this, m);
                }
            }
        };
        new Thread(r).start();
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
