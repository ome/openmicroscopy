/*
 * org.openmicroscopy.shoola.agents.fsimporter.view.ImporterChooserDialog 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.fsimporter.chooser;


//Java imports
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.fsimporter.IconManager;
import org.openmicroscopy.shoola.agents.fsimporter.ImporterAgent;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class ImporterChooserDialog 
	extends JDialog
	implements ActionListener
{

	/** Bound property indicating to import the selected files. */
	public static final String		IMPORT_PROPERTY = "import";
	
	/** Bound property indicating to monitor the selected directory. */
	public static final String		MONITOR_PROPERTY = "monitorFS";
	
	/** Identifies the <code>Cancel</code> action. */
	private static final int 		CANCEL = 0;
	
	/** Identifies the <code>Save</code> action. */
	private static final int 		SAVE = 1;
	
	/** Identifies the <code>Monitor</code> action. */
	private static final int		MONITOR = 2;
	
	/** The description of the <code>import</code> action. */
	private static final String 	SAVE_DESCRIPTION = "Import the " +
										"selected file.";
	
	/** The description of the <code>Monitor</code> action. */
	private static final String 	MONITOR_DESCRIPTION = 
						"Monitor the selected directory.";
	
    /** Brief description of the action performed by this widget. */
    private static final String     NOTE = "Import the selected file " +
    		"or monitor a directory.";
    
    /** Brief description of the action performed by this widget. */
    private static final String     TITLE = "Import.";
    
    /** 
     * The size of the invisible components used to separate buttons
     * horizontally.
     */
    private static final Dimension	H_SPACER_SIZE = new Dimension(3, 10);
    
	/** The chooser used. */
	private ImporterChooser	chooser;
	
	 /** 
     * Replaces the <code>CancelButton</code> provided by the 
     * {@link JFileChooser} class. 
     */
    private JButton		cancelButton;
    
    /** 
     * Replaces the <code>ApproveButton</code> provided by the 
     * {@link JFileChooser} class. 
     */
    private JButton		saveButton;
    
    /** Adds button to monitor a directory. */
    private JButton		monitorButton;
    
	/** Initiliazes the components composing the display. */
	private void initComponents()
	{
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);
		cancelButton.setActionCommand(""+CANCEL);
		saveButton = new JButton("Import");
		saveButton.setToolTipText(
				UIUtilities.formatToolTipText(SAVE_DESCRIPTION));
		saveButton.addActionListener(this);
		saveButton.setActionCommand(""+SAVE);
		//saveButton.setEnabled(false);
		monitorButton = new JButton("Monitor");
		monitorButton.addActionListener(this);
		monitorButton.setActionCommand(""+MONITOR);
		monitorButton.setToolTipText(
				UIUtilities.formatToolTipText(MONITOR_DESCRIPTION));
	    //getRootPane().setDefaultButton(saveButton);
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
    	bar.add(monitorButton);
    	bar.add(Box.createRigidArea(H_SPACER_SIZE));
    	bar.add(saveButton);
        return UIUtilities.buildComponentPanelRight(bar);
    }
    
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout(0, 0));
        p.add(chooser, BorderLayout.CENTER);
        p.add(buildToolbar(), BorderLayout.SOUTH);
        IconManager im = IconManager.getInstance();
        Container c = getContentPane();
        c.setLayout(new BorderLayout(0, 0));
        TitlePanel tp = new TitlePanel(TITLE, NOTE, 
                                im.getIcon(IconManager.IMPORT_48));
                    
        c.add(tp, BorderLayout.NORTH);
        c.add(p, BorderLayout.CENTER);
        if (JDialog.isDefaultLookAndFeelDecorated()) {
            boolean supportsWindowDecorations = 
            UIManager.getLookAndFeel().getSupportsWindowDecorations();
            if (supportsWindowDecorations)
                getRootPane().setWindowDecorationStyle(
                            JRootPane.FILE_CHOOSER_DIALOG);
        }
	}
	
	/** Closes the dialog. */
	private void close()
	{
		setVisible(false);
		dispose();
	}

	/** Import the file. */
	private void importFile()
	{
		File[] files = chooser.getSelectedFiles();
		if (files == null || files.length == 0) {
			UserNotifier un = ImporterAgent.getRegistry().getUserNotifier();
			un.notifyInfo("Import", "Please select a file to import.");
			return;
		}
		chooser.approveSelection();
		firePropertyChange(IMPORT_PROPERTY, null, files);
	}
	
	/** Monitors the selected directory. */
	private void monitorDirectory()
	{
		File file = chooser.getFSDefaultDirectory();//chooser.getSelectedDirectory();//chooser.getFSDefaultDirectory();
		if (file == null) {
			UserNotifier un = ImporterAgent.getRegistry().getUserNotifier();
			un.notifyInfo("Import", "Please select a directory to monitor.");
			return;
		}
		//chooser.approveSelection();
		firePropertyChange(MONITOR_PROPERTY, null, file);
		close();
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param parent The parent of the dialog.
	 * @param fsv	 The file system view.
	 */
	public ImporterChooserDialog(JFrame parent, FileSystemView fsv)
	{
		super(parent);
		Registry reg = ImporterAgent.getRegistry();
		List<FileFilter> filters = 
			reg.getImageService().getSupportedFileFilters();
		if (fsv == null)
			chooser = new ImporterChooser(filters);
		else chooser = new ImporterChooser(fsv, filters);
		initComponents();
		buildGUI();
		//pack();
	}

	/** 
	 * Reacts to buttons selection.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		int index = Integer.parseInt(e.getActionCommand());
		switch (index) {
			case CANCEL:
				close();
				chooser.cancelSelection();
				break;
			case SAVE:
				importFile();
				break;
			case MONITOR:
				monitorDirectory();
				break;
		}
	}
	
}
