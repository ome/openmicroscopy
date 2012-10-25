/*
 * org.openmicroscopy.shoola.agents.imviewer.util.saver.ImgSaverFileChooser
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

package org.openmicroscopy.shoola.agents.imviewer.util.saver;


//Java imports
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.ImViewerAgent;
import org.openmicroscopy.shoola.util.filter.file.BMPFilter;
import org.openmicroscopy.shoola.util.filter.file.JPEGFilter;
import org.openmicroscopy.shoola.util.filter.file.PNGFilter;
import org.openmicroscopy.shoola.util.filter.file.TIFFFilter;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.filechooser.GenericFileChooser;

/** 
 * Chooser to select the name, format and type of images to save.
 * The supported formats are: JPEG, PNG, BMP, TIFF.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
class ImgSaverFileChooser
    extends GenericFileChooser
    implements DocumentListener
{

    /** Default extension format. */
    private static final String DEFAULT_FORMAT = PNGFilter.PNG;
    
    /** Message used to indicate the directory in which the image is saved. */
    private static final String MSG_DIR = "The image has been successfully " +
    		"saved in";
    
    /** Reference to the model. */
    private ImgSaver    model;
    
    /** Reference to the View. */
    private ImgSaverUI	view;
    
    /** The text area where to enter the name of the file to save. */
    private JTextField	nameArea;
    
    /** Initializes the components composing the display. */
    private void initComponents()
    {
    	nameArea = (JTextField) UIUtilities.findComponent(this, 
    											JTextField.class);
    	if (nameArea != null) {
    		nameArea.setText(model.getPartialImageName());
    		nameArea.getDocument().addDocumentListener(this);
    	}
    }

    /** Builds and lays out the GUI. */
    private void buildGUI()
    {
    	setAcceptAllFileFilterUsed(false);
        setDialogType(SAVE_DIALOG);
        setFileSelectionMode(FILES_ONLY);
        addChoosableFileFilter(new BMPFilter()); 
        addChoosableFileFilter(new JPEGFilter()); 
        PNGFilter filter = new PNGFilter();
        addChoosableFileFilter(filter); 
        addChoosableFileFilter(new TIFFFilter());
        setFileFilter(filter);
        
        try {
			File f = UIUtilities.getDefaultFolder();
			if (f != null) setCurrentDirectory(f);
		} catch (Exception ex) {}
        if (nameArea != null) setControlButtonsAreShown(false);
        else {
        	setApproveButtonToolTipText(
        				UIUtilities.formatToolTipText(ImgSaverUI.SAVE_AS));
            setApproveButtonText("Save as");
        }
    }
    
    /**
     * Returns the format corresponding to the specified filter.
     * 
     * @param filter The filter specified.
     * @return See above.
     */
    private String getFormat(FileFilter filter)
    {
        String format = DEFAULT_FORMAT;
        if (filter instanceof JPEGFilter) format = JPEGFilter.JPG;
        else if (filter instanceof PNGFilter) format = PNGFilter.PNG;
        else if (filter instanceof TIFFFilter) format = TIFFFilter.TIF;
        else if (filter instanceof BMPFilter) format = BMPFilter.BMP;
        return format;
    }

    /**
     * Sets the <code>enabled</code> flag of not the <code>Save</code> and
     * <code>Preview</code> options depending on the length of the text entered
     * in the {@link #nameArea}.
     */
    private void handleTextUpdate()
    {
    	if (nameArea == null) return; //should happen
    	String text = nameArea.getText();
    	boolean b = (text == null || text.trim().length() == 0);
    	view.setControlsEnabled(!b);
    }
   
    /**
     * Sets the format, the file name and the message to display.
     * Returns <code>null</code> if the selected file is <code>null</code>
     * or a <code>Boolean</code> whose value is <code>true</code>
     * if the file already exists, <code>false</code> otherwise.
     * 
     * @return See above.
     */
    private Boolean setSelection()
    {
    	// Build the file .
    	File f = getSelectedFile();
    	if (f == null) return Boolean.valueOf(false);
    	 String format = getFormat(getFileFilter());
         String fileName = f.getAbsolutePath();
         model.setFileFormat(format);
         File[] l = getCurrentDirectory().listFiles();
         String n = model.getExtendedName(fileName, format);
         boolean exist = false;
         for (int i = 0; i < l.length; i++) {
             if ((l[i].getAbsolutePath()).equals(n)) {
                 exist = true;
                 break;
             }
         }
         setSelectedFile(null);
         return Boolean.valueOf(exist);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the model. Mustn't be <code>null</code>.
     * @param view 	Reference to the view. Mustn't be <code>null</code>.
     */
    ImgSaverFileChooser(ImgSaver model, ImgSaverUI view)
    {
        if (model == null) throw new IllegalArgumentException("No model.");
        if (view == null) throw new IllegalArgumentException("No view.");
        this.model = model;
        this.view = view;
        initComponents();
        buildGUI();
    }
 
    /** Previews the image to save. */
    void previewSelection()
    {
    	Boolean b = setSelection();
    	if (b == null) return;
    	if (b.booleanValue()) model.setSelection(ImgSaver.PREVIEW);
    	else model.previewImage();
    }
    
    /**
     * Creates a new folder.
     * 
     * @param name The name of the folder.
     */
    void createFolder(String name)
    {
    	File dir = getCurrentDirectory();
    	String n = dir.getAbsolutePath()+File.separator+name;
    	String message = "Cannot create a folder.";
    	boolean b = new File(n).mkdir();
		if (b) message = "Folder created.";
		ImViewerAgent.getRegistry().getLogger().info(this, message);
    }
    
    /**
	 * Enables or not the <code>Save</code> and <code>Preview</code> options
	 * depending on the text entered in the {@link #nameArea}.
	 * @see DocumentListener#insertUpdate(DocumentEvent)
	 */
	public void insertUpdate(DocumentEvent e) { handleTextUpdate(); }

	/**
	 * Enables or not the <code>Save</code> and <code>Preview</code> options
	 * depending on the text entered in the {@link #nameArea}.
	 * @see DocumentListener#removeUpdate(DocumentEvent)
	 */
	public void removeUpdate(DocumentEvent e) { handleTextUpdate(); }
    
	/**
	 * Required by the {@link DocumentListener} I/F but no-op implementation
	 * in our case.
	 * @see DocumentListener#changedUpdate(DocumentEvent)
	 */
	public void changedUpdate(DocumentEvent e) {}
	
    /**
     * Overridden to close the {@link ImgSaver} when the selection is cancelled.
     * @see JFileChooser#cancelSelection()
     */
    public void cancelSelection()
    { 
    	model.close(); 
    	super.cancelSelection();
    }
    
    /**
     * Overridden to set the format, name and type of images to save.
     * @see JFileChooser#approveSelection()
     */
    public void approveSelection()
    {
        Boolean exist = setSelection();
        if (exist == null)
        	// No file selected, or file can be written - let OK action continue
        	super.approveSelection();
        else {
        	if (exist.booleanValue()) 
        		model.setSelection(ImgSaver.DIRECT);
        	else model.saveImage(true);
        	super.approveSelection();
        }
    	//previewSelection();
    	super.approveSelection();
    }

    /**
     * Overridden to create the selected file when 
     * <code>Save</code> and <code>Preview</code> options are visible,
     * otherwise the selected file is <code>null</code>.
     * @see JFileChooser#getSelectedFile()
     */
    public File getSelectedFile()
    {
    	if (nameArea == null) return super.getSelectedFile();
    	return new File(getCurrentDirectory().toString(), nameArea.getText());
    }
	
}
