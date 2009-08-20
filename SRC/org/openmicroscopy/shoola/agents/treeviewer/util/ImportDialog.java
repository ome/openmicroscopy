/*
 * org.openmicroscopy.shoola.agents.treeviewer.util.ImportDialog
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
 *
 *
 *     This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.treeviewer.util;

//Java imports
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JSplitPane;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.IconManager;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/**
 * Basic import dialog.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *     <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author    Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 *     <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class ImportDialog
	extends JDialog
	implements ActionListener, PropertyChangeListener
{

	/** Bound property indicating that the cancel button is pressed. */
	public static final String	CANCEL_SELECTION_PROPERTY = "cancelSelection";
	
	/** Bound property indicating to import the selected files. */
	public static final String	IMPORT_PROPERTY = "import";

	/** Action id indicating to import the selected files. */
	public static final int		IMPORT = 0;
	
	/** Action id indicating to close the dialog. */
	public static final int		CANCEL = 1;
	
	/** Action id indicating to refresh the file view. */
	private static final int	REFRESH = 2;
	
	/** The title of the dialog. */
	private static final String TITLE = "Import";
	
	/** The message to display in the header. */
	private static final String MESSAGE = "Selects the files to import";
	
	/** The approval option the user chose. */
	private int					option;

	/** The table hosting the file to import. */
	private FileSelectionTable  table;
	
	/** The file chooser. */
	private JFileChooser	    chooser;
	
	/** Button to close the dialog. */
	private JButton				cancelButton;
	
	/** Button to import the files. */
	private JButton				importButton;
	
	/** Button to import the files. */
	private JButton				refreshButton;
	
	/** The collection of supported filters. */
	private List<FileFilter> 	filters;
	
	/** Sets the properties of the dialog. */
	private void setProperties()
	{
		setTitle(TITLE);
        setModal(true);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
		
			/** 
			 * Cancels the selection.
			 * @see WindowAdapter#windowClosing(WindowEvent)
			 */
			public void windowClosing(WindowEvent e) {
				cancelSelection();
			}
		
		});
	}

	/** Initializes the components composing the display. */
	private void initComponents()
	{
		chooser = new JFileChooser();
		File f = UIUtilities.getDefaultFolder();
		if (f != null) chooser.setCurrentDirectory(f);
		chooser.addPropertyChangeListener(this);
		chooser.setMultiSelectionEnabled(true);
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		//chooser.setAcceptAllFileFilterUsed(false);
		chooser.setControlButtonsAreShown(false);
		chooser.setApproveButtonText("Import");
		chooser.setApproveButtonToolTipText("Import files");
		/*
		if (filters != null) {
			Iterator<FileFilter> i = filters.iterator();
			while (i.hasNext()) {
				chooser.setFileFilter(i.next());
			}
		}
		*/
		table = new FileSelectionTable();
		table.addPropertyChangeListener(this);
		cancelButton = new JButton("Close");
		cancelButton.setToolTipText("Close the dialog and do not import.");
		cancelButton.setActionCommand(""+CANCEL);
		cancelButton.addActionListener(this);
		importButton = new JButton("Import");
		importButton.setToolTipText("Import the selected files.");
		importButton.setActionCommand(""+IMPORT);
		importButton.addActionListener(this);
		importButton.setEnabled(false);
		refreshButton = new JButton("Refresh");
		refreshButton.setToolTipText("Reloads the files view.");
		refreshButton.setActionCommand(""+REFRESH);
		refreshButton.addActionListener(this);
		getRootPane().setDefaultButton(cancelButton);
	}
	
	/** 
	 * Builds and lays out the tool bar. 
	 * 
	 * @return See above.
	 */
	private JPanel buildToolBarRight()
	{
		JPanel bar = new JPanel();
		bar.setLayout(new FlowLayout(FlowLayout.RIGHT));
		bar.add(cancelButton);
		bar.add(Box.createHorizontalStrut(5));
		bar.add(importButton);
		bar.add(Box.createHorizontalStrut(10));
		return bar;
	}
	
	/** 
	 * Builds and lays out the tool bar. 
	 * 
	 * @return See above.
	 */
	private JPanel buildToolBarLeft()
	{
		JPanel bar = new JPanel();
		bar.setLayout(new FlowLayout(FlowLayout.LEFT));
		bar.add(refreshButton);
		return bar;
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		Container c = getContentPane();
		c.setLayout(new BorderLayout(0, 0));
		IconManager icons = IconManager.getInstance();
		TitlePanel tp = new TitlePanel(TITLE, MESSAGE, 
				icons.getIcon(IconManager.IMPORT_48));
		c.add(tp, BorderLayout.NORTH);
		JSplitPane pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, chooser, 
				table);
		c.add(pane, BorderLayout.CENTER);
		
		JPanel bar = new JPanel();
		bar.setLayout(new BoxLayout(bar, BoxLayout.X_AXIS));
		bar.add(buildToolBarLeft());
		bar.add(buildToolBarRight());
		c.add(bar, BorderLayout.SOUTH);
		if (JDialog.isDefaultLookAndFeelDecorated()) {
			boolean supportsWindowDecorations = 
				UIManager.getLookAndFeel().getSupportsWindowDecorations();
			if (supportsWindowDecorations)
				getRootPane().setWindowDecorationStyle(
						JRootPane.FILE_CHOOSER_DIALOG);
		}
	}
	
    /** Closes the window and disposes. */
    private void cancelSelection()
    {
    	firePropertyChange(CANCEL_SELECTION_PROPERTY, Boolean.valueOf(false), 
    			Boolean.valueOf(true));
    	option = CANCEL;
    	setVisible(false);
    	dispose();
    }
    
    /** Imports the selected files. */
    private void importFiles()
    {
    	option = IMPORT;
    	//Set the current directory as the defaults
    	UIUtilities.setDefaultFolder(
    			chooser.getCurrentDirectory().toString());
    	firePropertyChange(IMPORT_PROPERTY, null, table.getFilesToImport());
    	setVisible(false);
    	dispose();
    }
    
	/**
	 * Returns <code>true</code> if the file can be imported, 
	 * <code>false</code> otherwise.
	 * 
	 * @param f The file to check.
	 * @return See above.
	 */
	private boolean isFileImportable(File f)
	{
		/*
		Iterator<FileFilter> i = filters.iterator();
		FileFilter filter;
		while (i.hasNext()) {
			filter = i.next();
			if (filter.accept(f)) return true;
		}
		return false;
		*/
		return true;
	}
	
	/**
	 * Checks if the file can be added to the passed list.
	 * 
	 * @param f The file to handle.
	 * @param l The list to populate.
	 */
	private void checkFile(File f, List<File> l)
	{
		if (f == null || f.isHidden()) return;
		if (f.isFile()) {
			if (isFileImportable(f)) l.add(f);
		} else if (f.isDirectory()) {
			File[] list = f.listFiles();
			if (list != null && list.length > 0) {
				for (int i = 0; i < list.length; i++) {
					checkFile(list[i], l);
				}
			}
		}
	}
    
    /** 
     * Creates a new instance.
     * 
     * @param owner 	The owner of the dialog.
     * @param filters 	The list of filters.
     */
    public ImportDialog(JFrame owner, List<FileFilter> filters)
    {
    	super(owner);
    	this.filters = filters;
    	setProperties();
    	initComponents();
    	buildGUI();
    	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    	setSize(7*(screenSize.width/10), 7*(screenSize.height/10));
    }
    
    /**
     * Returns the collection of files to import.
     * 
     * @return See above.
     */
    public List<File> getFilesToImport() { return table.getFilesToImport(); }
    
    /**
     * Shows the chooser dialog. 
     * 
     * @return The option selected.
     */
    public int showDialog()
    {
	    UIUtilities.setLocationRelativeToAndShow(getParent(), this);
	    return option;
    }

    /**
     * Shows the chooser dialog. 
     * 
     * @return The option selected.
     */
    public int centerDialog()
    {
	    UIUtilities.centerAndShow(this);
	    return option;
    }
    
	/**
	 * Reacts to property fired by the table.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (FileSelectionTable.ADD_PROPERTY.equals(name)) {
			File[] files = chooser.getSelectedFiles();
			if (files == null || files.length == 0) return;
			List<File> l = new ArrayList<File>();
			for (int i = 0; i < files.length; i++) 
				checkFile(files[i], l);
			table.addFiles(l);
			importButton.setEnabled(table.hasFilesToImport());
		} else if (FileSelectionTable.REMOVE_PROPERTY.equals(name)) {
			importButton.setEnabled(table.hasFilesToImport());
		} else if (JFileChooser.SELECTED_FILES_CHANGED_PROPERTY.equals(name)) {
			File[] files = chooser.getSelectedFiles();
			table.allowAddition(files != null && files.length > 0);
		} 
	}

	/**
	 * Cancels or imports the files.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt)
	{
		int index = Integer.parseInt(evt.getActionCommand());
		switch (index) {
			case IMPORT:
				importFiles();
				break;
			case CANCEL:
				cancelSelection();
				break;
			case REFRESH:
				chooser.rescanCurrentDirectory();
				chooser.repaint();
		}
	}
	
}
