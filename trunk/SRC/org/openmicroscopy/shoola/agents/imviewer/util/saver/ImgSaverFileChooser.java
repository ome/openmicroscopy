/*
 * org.openmicroscopy.shoola.agents.imviewer.util.saver.ImgSaverFileChooser
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.agents.imviewer.util.saver;


//Java imports
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.filter.file.BMPFilter;
import org.openmicroscopy.shoola.util.filter.file.JPEGFilter;
import org.openmicroscopy.shoola.util.filter.file.PNGFilter;
import org.openmicroscopy.shoola.util.filter.file.TIFFFilter;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

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
    extends JFileChooser
{

    /** Default extension format. */
    private static final String DEFAULT_FORMAT = PNGFilter.PNG;
    
    /** Message used to indicate the directory in which the image is saved. */
    private static final String MSG_DIR = "The image has been saved in \n";
    
    /** The tool tip of the <code>Approve</code> button. */
    private static final String SAVE_AS = "Save the current image in the " +
                                            "specified format.";
    
    /** Reference to the model. */
    private ImgSaver    model;
    
    /** Flag to indicate if the file choose is visible. */
    private boolean     display;
    
    /** Builds and lays out the GUI. */
    private void buildGUI()
    {
        setDialogType(SAVE_DIALOG);
        setFileSelectionMode(FILES_ONLY);
        BMPFilter bmpFilter = new BMPFilter();
        setFileFilter(bmpFilter);
        addChoosableFileFilter(bmpFilter); 
        JPEGFilter jpegFilter = new JPEGFilter();
        setFileFilter(jpegFilter);
        addChoosableFileFilter(jpegFilter); 
        PNGFilter pngFilter = new PNGFilter();
        addChoosableFileFilter(pngFilter); 
        setFileFilter(pngFilter);
        TIFFFilter tiffFilter = new TIFFFilter();
        setFileFilter(tiffFilter);
        addChoosableFileFilter(tiffFilter); 
        setAcceptAllFileFilterUsed(false);
        setApproveButtonToolTipText(UIUtilities.formatToolTipText(SAVE_AS));
        setApproveButtonText("Save as");
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
     * Checks if the selected name already exists. If not, a preview of
     * the image to save is brought up on screen.
     * 
     * @param format    The selected format.
     * @param name      The name of the image.
     * @param msg       The message displayed after creation.
     * @param l         The list of files in the current directory.
     */
    private void setSelection(String format, String name, String msg, File[] l)
    {
        String n = name + "." + format;
        boolean exist = false;
        for (int i = 0; i < l.length; i++) {
            if ((l[i].getAbsolutePath()).equals(n)) {
                exist = true;
                break;
            }
        }
        model.setFileName(name);
        model.setFileFormat(format);
        model.setFileMessage(msg);
        //If the file already exits so do we override it?
        model.previewImage(exist);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the model. Mustn't be <code>null</code>.
     */
    ImgSaverFileChooser(ImgSaver model)
    {
        if (model == null) throw new IllegalArgumentException("No model.");
        this.model = model;
        display = false;
        buildGUI();
    }
    
    /**
     * Overriden to close the {@link ImgSaver} when the selection is cancelled.
     * @see JFileChooser#cancelSelection()
     */
    public void cancelSelection() { model.setClosed(true); }
    
    /**
     * Overriden to set the format, name and type of images to save.
     * @see JFileChooser#approveSelection()
     */
    public void approveSelection()
    {
        File file = getSelectedFile();
        if (file != null) {
            String format = getFormat(getFileFilter());
            String  fileName = file.getAbsolutePath();
            String message = MSG_DIR+""+getCurrentDirectory();
            setSelection(format, fileName, message, 
                                getCurrentDirectory().listFiles());
            setSelectedFile(null);
            if (display) return;    // to check
        }      
        // No file selected, or file can be written - let OK action continue
        super.approveSelection();
    }
    
}
