/*
 * org.openmicroscopy.shoola.util.ui.filechooser.FolderChooserDialog 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.util.ui.filechooser;


//Java imports
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.UIManager;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.IconManager;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * A dialog used to select a folder.
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
public class FolderChooserDialog 	
	extends JDialog
{

	/** The window's title. */
	static final String 			TITLE = "Download files";
	
	/** 
	 * Bound property indicating the directory where to save the original files.
	 */
	public static final String		LOCATION_PROPERTY = "location";

	/** The window's text. */
	private static final String 	TEXT = "Select a directory where to " +
											"download the files.";
    
	/** Box to save the current directory as default. */
    private JCheckBox		settings;
    
    /** UI component to select the folder. */
    private FolderChooser	chooser;
    
    
    /** The title of the dialog. */
    private String			title;
    
    /** The header of the dialog. */
    private TitlePanel 		header;
    
    /** Initializes the components composing the display. */
    private void initComponents()
    {
    	settings = new JCheckBox();
        settings.setText("Set the current directory as default.");
        settings.setSelected(true);
        chooser = new FolderChooser(this);
    }
    
    /** Builds and lays out the UI. */
    private void buildGUI()
    {
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout(0, 0));
        p.add(chooser, BorderLayout.CENTER);
        p.add(UIUtilities.buildComponentPanel(settings), BorderLayout.SOUTH);
        IconManager im = IconManager.getInstance();
        Container c = getContentPane();
        c.setLayout(new BorderLayout(0, 0));
        header = new TitlePanel(title, TEXT, 
        						im.getIcon(IconManager.DOWNLOAD_48));
                    
        c.add(header, BorderLayout.NORTH);
        c.add(p, BorderLayout.CENTER);
        if (JDialog.isDefaultLookAndFeelDecorated()) {
            boolean supportsWindowDecorations = 
            UIManager.getLookAndFeel().getSupportsWindowDecorations();
            if (supportsWindowDecorations)
                getRootPane().setWindowDecorationStyle(
                            JRootPane.FILE_CHOOSER_DIALOG);
        }
    }
    
    /** Sets the properties of the dialog. */
    private void setProperties()
    {
    	setTitle(title);
        setModal(true);
        //setAlwaysOnTop(true);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
		
			/** 
			 * Cancels the selection.
			 * @see WindowAdapter#windowClosing(WindowEvent)
			 */
			public void windowClosing(WindowEvent e) {
				chooser.cancelSelection();
			}
			
			/**
			 * Requests focus on name to enable the <code>Approve button</code>.
			 * @see WindowAdapter#windowOpened(WindowEvent)
			 */
			public void windowOpened(WindowEvent e) {
				//chooser.requestFocusOnName();
			}
		});
    }
	

	/**
	 * Creates a new instance.
     * 
     * @param owner The owner of this dialog.
     * @param title	The title associated to the dialog.
	 */
	public FolderChooserDialog(JFrame owner, String title)
	{
		super(owner);
		if (title == null || title.length() == 0)
			title = TITLE;
		this.title = title;
		setProperties();
		initComponents();
		buildGUI();
		pack();
	}
	
	/**
	 * Creates a new instance.
     * 
     * @param owner The owner of this dialog.
	 */
	public FolderChooserDialog(JFrame owner)
	{
		this(owner, TITLE);
	}

	/**
	 * Sets the title of the dialog.
	 * 
	 * @param title The value to set.
	 */
	public void setTitle(String title)
	{
		if (title == null || title.length() == 0)
			title = TITLE;
		this.title = title;
		super.setTitle(title);
		header.setTitle(title);
	}
    
    /** Closes and disposes. */
	void close()
	{
		setVisible(false);
		dispose();
	}
	
	/**
	 * Fires a property indicating where to save the archived files.
	 * 
	 * @param path	The path to the directory.
	 */
	void setFolderPath(String path)
	{
		if (path == null) return;
		char separator = File.separatorChar;
		boolean exist = false;
		File[] l = chooser.getCurrentDirectory().listFiles();
        for (int i = 0; i < l.length; i++) {
            if ((l[i].getAbsolutePath()).equals(path)) {
                exist = true;
                break;
            }
        }
        if (!exist) new File(path).mkdir();
		firePropertyChange(LOCATION_PROPERTY, null, path+separator);
		if (settings.isSelected()) 
			UIUtilities.setDefaultFolder(path);
        close();	
	}
	
}
