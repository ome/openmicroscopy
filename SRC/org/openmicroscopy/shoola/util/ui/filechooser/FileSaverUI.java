/*
 * org.openmicroscopy.shoola.util.ui.filechooser.FileSaverUI 
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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.UIManager;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.IconManager;
import org.openmicroscopy.shoola.agents.imviewer.util.saver.ImgSaver;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class FileSaverUI
{

	/** The tool tip of the <code>Approve</code> button. */
	static final String 			SAVE_AS = "Save the current file in" +
  												"the specified format.";  
	 /** The tool tip of the <code>Approve</code> button. */
	static final String 			LOAD = "Load the current file in" +
  												"the specified format.";  
   /** 
    * The size of the invisible components used to separate buttons
    * horizontally.
    */
   private static final Dimension	H_SPACER_SIZE = new Dimension(3, 10);
   
   
   /** Reference to the {@link ImgSaver}. */
   private FileChooser                    model;
   
   /** Reference to the file chooser. */
   private OMEFileChooser					chooser;
   
   /** Box to save the current directory as default. */
   private JCheckBox					settings;
   
   /** 
    * Replaces the <code>CancelButton</code> provided by the 
    * {@link JFileChooser} class. 
    */
   private JButton						cancelButton;
   
   /** 
    * Replaces the <code>ApproveButton</code> provided by the 
    * {@link JFileChooser} class. 
    */
   private JButton						loadsaveButton;
   
   /** is this a save dialog. */
   private boolean						save;
   
   
   /** Initializes the component composing the display. */
   private void initComponents()
   {
   	
       chooser = new OMEFileChooser(save, model, this);
       settings = new JCheckBox();
       settings.setText("Set the current directory as default.");
       settings.setSelected(true);
       cancelButton = new JButton("Cancel");
   		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { 
				chooser.cancelSelection(); }
		});
   	if(save)
   	{
   		loadsaveButton = new JButton("Save as");
   		loadsaveButton.setToolTipText(UIUtilities.formatToolTipText(SAVE_AS));
   	}
   	else
   	{
  		loadsaveButton = new JButton("Load");
  		loadsaveButton.setToolTipText(UIUtilities.formatToolTipText(LOAD));
   	}
   	
   	loadsaveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { 
				chooser.approveSelection(); 
			}
		});
   	loadsaveButton.setEnabled(false);
   	model.getRootPane().setDefaultButton(loadsaveButton);
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
   	bar.add(loadsaveButton);
   	JPanel p = UIUtilities.buildComponentPanelRight(bar);
       p.setOpaque(true);
       return p;
   }
   
   /**
    * Builds the UI component displaying the saving options.
    * 
    * @return See above.
    */
   private JPanel buildSelectionPane()
   {
       JPanel p = new JPanel();
       JPanel result = new JPanel();
       result.setLayout(new BoxLayout(result, BoxLayout.X_AXIS));
       p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
       p.add(result);
       p.add(UIUtilities.buildComponentPanelRight(settings));
       return p;
   }
   
   /** Builds and lays out the UI. */
   private void buildGUI()
   {
   	JPanel controls = new JPanel();
   	controls.setLayout(new BorderLayout(0, 0));
   	controls.add(buildToolbar(), BorderLayout.CENTER);
   	controls.add(buildSelectionPane(), BorderLayout.SOUTH);
       JPanel p = new JPanel();
       p.setLayout(new BorderLayout(0, 0));
       p.add(chooser, BorderLayout.CENTER);
       p.add(controls, BorderLayout.SOUTH);
       IconManager im = IconManager.getInstance();
       Container c = model.getContentPane();
       c.setLayout(new BorderLayout(0, 0));
       TitlePanel tp = new TitlePanel(model.getTitle(), model.getNote(), 
                               im.getIcon(IconManager.SAVE_BIG));
                   
       c.add(tp, BorderLayout.NORTH);
       c.add(p, BorderLayout.CENTER);
       if (JDialog.isDefaultLookAndFeelDecorated()) {
           boolean supportsWindowDecorations = 
           UIManager.getLookAndFeel().getSupportsWindowDecorations();
           if (supportsWindowDecorations)
               model.getRootPane().setWindowDecorationStyle(
                           JRootPane.FILE_CHOOSER_DIALOG);
       }
   }
   
   /**
    * Creates a new instance.
    * @param save is this a save dialog.
    * @param model Reference to the Model. Mustn't be <code>null</code>.
    */
   FileSaverUI(boolean save, FileChooser model)
   { 
       if (model == null) throw new IllegalArgumentException("No model.");
       this.model = model;
       this.save = save;
       initComponents();
       buildGUI();
   }
   
   /**
    * Set the current directory of the file chooser. 
    * @param directory see above.
    */
   void setCurrentDirectory(File directory)
   {
	   chooser.setCurrentDirectory(directory);
   }
   
   /**
    * Set the current file of the file chooser. 
    * @param file see above.
    */
   void setSelectedFile(File file)
   {
	   chooser.setSelectedFile(file);
   }
   
   /**
   * Returns the pathname of the current directory.
   *
   * @return  The directory path.
   */
   File getCurrentDirectory()
   { 
   	return chooser.getCurrentDirectory(); 
   }
   
   /**
    * Returns the pathname of the current file.
    *
    * @return  The file path.
    */
    File getSelectedFile()
    { 
    	return chooser.getSelectedFile(); 
    }
    
   /**
    * Returns <code>true</code> if the default folder is set when
    * saving the file, <code>false</code> toherwise.
    * 
    * @return See above.
    */
   boolean isSetDefaultFolder() { return settings.isSelected(); }
   

   /**
    * Sets the <code>enabled</code> flag of not the <code>Save</code> and
    * <code>Preview</code> options.
    * 
    * @param b The value to set.
    */
	void setControlsEnabled(boolean b)
	{
		loadsaveButton.setEnabled(b);
	}

}



