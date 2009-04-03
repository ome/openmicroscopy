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
    
    /** Default extension format. */
    private static final String DEFAULT_FORMAT = PNGFilter.PNG;
    
    private static final String MSG_DIR = "The image has been saved in \n";
    
    private ContainerSaver  window;
    
    private boolean         display;
    
    public FileChooser(ContainerSaver window)
    {
        if (window == null) throw new NullPointerException("No window."); 
        this.window = window;
        display = false;
        buildGUI();
    }
    
    /** Build and lay out the GUI. */
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
     * Retrieve the File format selected.
     * @param filter    filter specified.
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
     * @param format        format selected <code>jpeg<code>, 
     *                      <code>png<code> or <code>tif<code>.
     * @param fileName      image's name.
     * @param message       message displayed after the image has been created.
     * @param list          lis of files in the current directory.
     */
    private void setSelection(String format, String fileName, String message,
                                File[] list)
    {
        boolean exist = false;
        String name = fileName + "." + format;
        for (int i = 0; i < list.length; i++) {
            if ((list[i].getAbsolutePath()).equals(name)) {
                exist = true;
                break;
            }
        }
        window.previewImage(format, fileName, message, exist);   
    }
    
    /** Override the {@link #cancelSelection} method. */
    public void cancelSelection() { window.closeWindow(); }
    
    /** Override the {@link #approveSelection} method. */
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
