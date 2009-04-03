/*
 * org.openmicroscopy.shoola.agents.hiviewer.saver.ContainerSaver
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
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Set;
import javax.swing.JDialog;
import javax.swing.JFrame;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.HiViewerAgent;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.image.io.WriterImage;

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
public class ContainerSaver
    extends JDialog
{   
    
    private ContainerSaverManager   manager;
    
    /** The set of Buffered images to save. */
    private Set                     thumbnails;
    
    /** The format of the image either PNG or JPEG.*/
    private String                  format;
    
    /** The name of the file to create.*/
    private String                  name;
    
    /** The save message. */
    private String                  message;
    
    /** Notify the user that an error occured. */
    private void notifyError(String msg)
    {
        UserNotifier un = HiViewerAgent.getRegistry().getUserNotifier();
        un.notifyError("Save the thumbnails", msg);
    }
    
    void previewImage(String format, String name, String msg, boolean exist)
    {
        if (format == null) {
            notifyError("file format cannot be null");
            return;
        }
        if (name == null) {
            notifyError("name cannot be null");
            return;
        }
        this.format = format;
        this.name = name;
        message = msg;
        if (exist) manager.showSelectionDialog();    
        else manager.showPreview();
    }
    
    /** Save the image. */
    void saveImage(BufferedImage img)
    {
        UserNotifier un = HiViewerAgent.getRegistry().getUserNotifier();
        name += "."+format; //Add extension
        File f = new File(name);
        try {
            WriterImage.saveImage(f, img, format);
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
    
    Set getThumbnails() {return thumbnails; }
   
    void showPreview() { manager.showPreview(); }
    
    public ContainerSaver(JFrame owner, Set thumbnails) 
    {
        super(owner);
        if (owner == null) throw new NullPointerException("No owner.");
        if (thumbnails == null)
            throw new NullPointerException("No images.");
        this.thumbnails = thumbnails;
        //create the view and the control
        new ContainerSaverUI(this);
        manager = new ContainerSaverManager(this);
    }
    
}
