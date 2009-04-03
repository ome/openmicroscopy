/*
 * org.openmicroscopy.shoola.agents.hiviewer.saver.FileChooser
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

package org.openmicroscopy.shoola.agents.hiviewer.saver;



//Java imports
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.filter.file.JPEGFilter;
import org.openmicroscopy.shoola.util.filter.file.PNGFilter;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class FileChooser
    extends JFileChooser
{
    
    /** The default extension. */
    private static final String DEFAULT_FORMAT = PNGFilter.PNG;
    
    /** The default save message. */
    private static final String MSG_DIR = "The image has been saved in \n";
    
    /** The window hosting the display. */
    private ContainerSaver  window;
    
    /** Flag to indicate if the file chooser is already on screen. */
    private boolean         display;
    
    /** Builds and lays out the GUI. */
    private void buildGUI()
    {
        setDialogType(SAVE_DIALOG);
        setFileSelectionMode(FILES_ONLY);
        JPEGFilter jpegFilter = new JPEGFilter();
        setFileFilter(jpegFilter);
        addChoosableFileFilter(jpegFilter); 
        PNGFilter pngFilter = new PNGFilter();
        addChoosableFileFilter(pngFilter); 
        setFileFilter(pngFilter);
        setAcceptAllFileFilterUsed(false);
        setApproveButtonText("Save as");
    }

    /**
     * Retrieves the selected extension.
     * 
     * @param filter The filter specified.
     * @return See above.
     */
    private String getFormat(FileFilter filter)
    {
        String format = DEFAULT_FORMAT;
        if (filter instanceof JPEGFilter) format = JPEGFilter.JPG;
        else if (filter instanceof PNGFilter) format = PNGFilter.PNG;
        return format;
    }
    
    /** 
     * Check if the fileName specified already exists if not the image is saved
     * in the specified format.
     * 
     * @param extension  The extension selected <code>jpeg<code>, 
     *                      <code>png<code> or <code>tif<code>.
     * @param fileName The image's name.
     * @param message The message displayed after the image has been created.
     * @param list The list of files in the current directory.
     */
    private void setSelection(String extension, String fileName, String message,
                                File[] list)
    {
        boolean exist = false;
        String name = fileName + "." + extension;
        for (int i = 0; i < list.length; i++) {
            if ((list[i].getAbsolutePath()).equals(name)) {
                exist = true;
                break;
            }
        }
        window.previewImage(extension, fileName, message, exist);   
    }
    
    /**
     * Creates a new instance.
     * 
     * @param window Reference to the model. Mustn't be <code>null</code>.
     */
    FileChooser(ContainerSaver window)
    {
        if (window == null) throw new IllegalArgumentException("No window."); 
        this.window = window;
        display = false;
        buildGUI();
    }

    /** Overrides the {@link #cancelSelection} method. */
    public void cancelSelection() { window.closeWindow(); }
    
    /** Overrides the {@link #approveSelection} method. */
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
