/*
 * org.openmicroscopy.shoola.agents.hiviewer.saver.ContainerSaver
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

package org.openmicroscopy.shoola.agents.hiviewer.saver;

//Java imports
import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.JFrame;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.HiViewerAgent;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.filter.file.TIFFFilter;
import org.openmicroscopy.shoola.util.image.geom.Factory;
import org.openmicroscopy.shoola.util.image.io.Encoder;
import org.openmicroscopy.shoola.util.image.io.TIFFEncoder;
import org.openmicroscopy.shoola.util.image.io.WriterImage;

/** 
 * Dialog window to save a set of thumbnails.
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
public class ContainerSaver
    extends JDialog
{   
    
    /** Reference to the class manager. */
    private ContainerSaverManager   manager;
    
    /** The set of images to save. */
    private List                    thumbnails;
    
    /** The format of the image either PNG or JPEG.*/
    private String                  extension;
    
    /** The name of the file to create.*/
    private String                  name;
    
    /** The message displayed when the image has been saved. */
    private String                  message;
    
    /**
     * Notifies the user that an error occured. 
     * 
     * @param msg The notification message.
     */
    private void notifyError(String msg)
    {
        UserNotifier un = HiViewerAgent.getRegistry().getUserNotifier();
        un.notifyError("Save the thumbnails", msg);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param owner			The owner of this widget.
     * @param thumbnails 	The thumbnails to save.
     * 						Mustn't be <code>null</code>.
     */
    public ContainerSaver(JFrame owner, List thumbnails) 
    {
        super(owner);
        if (thumbnails == null)
            throw new IllegalArgumentException("No images.");
        this.thumbnails = thumbnails;
        //create the view and the control
        new ContainerSaverUI(this);
        manager = new ContainerSaverManager(this);
    }
    
    /**
     * Previews the image to save. 
     * 
     * @param extension The image's extension i.e. <i>PNG</i> or <i>JPEG</i>.
     * @param name The name of the image.
     * @param msg The message displayed when the image has been saved.
     * @param exist Passed <code>true</code> if a file with the same name and
     * extension already exists.
     */
    void previewImage(String extension, String name, String msg, boolean exist)
    {
        if (extension == null) {
            notifyError("The file extension cannot be null.");
            return;
        }
        if (name == null) {
            notifyError("The name cannot be null.");
            return;
        }
        this.extension = extension;
        this.name = name;
        message = msg;
        if (exist) manager.showSelectionDialog();    
        else manager.showPreview();
    }
    
    /**
     * Saves the specified {@link BufferedImage}.
     * @param img The image to save.
     */
    void saveImage(BufferedImage img)
    {
        UserNotifier un = HiViewerAgent.getRegistry().getUserNotifier();
        name += "."+extension; //Add extension
        File f = new File(name);
        try {
        	if (extension.equals(TIFFFilter.TIF)) {
                Encoder encoder = new TIFFEncoder(Factory.createImage(img), 
                        new DataOutputStream(new FileOutputStream(f)));
                WriterImage.saveImage(encoder);
            } else WriterImage.saveImage(f, img, extension);
            un.notifyInfo("Image saved", message);
        } catch (Exception e) {
            f.delete();
            un.notifyError("Save image failure", "Unable to save the image", e);
        }
        closeWindow();
    }
    
    /** Closes and disposes. */
    void closeWindow()
    { 
        setVisible(false);
        dispose();
    }
    
    /**
     * Returns the passed thumbnails.
     * 
     * @return See above.
     */
    List getThumbnails() { return thumbnails; }
   
    /** Brings up the preview widget. */
    void showPreview() { manager.showPreview(); }
    
}
