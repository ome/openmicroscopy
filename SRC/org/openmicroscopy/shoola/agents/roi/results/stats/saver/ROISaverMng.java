/*
 * org.openmicroscopy.shoola.agents.roi.results.stats.saver.ROISaverMng
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

package org.openmicroscopy.shoola.agents.roi.results.stats.saver;


//Java imports
import java.awt.image.BufferedImage;
import java.io.File;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.roi.IconManager;
import org.openmicroscopy.shoola.agents.roi.results.stats.StatsResultsPaneMng;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.filter.file.JPEGFilter;
import org.openmicroscopy.shoola.util.filter.file.PNGFilter;
import org.openmicroscopy.shoola.util.filter.file.TEXTFilter;
import org.openmicroscopy.shoola.util.file.WriterText;
import org.openmicroscopy.shoola.util.image.io.Encoder;
import org.openmicroscopy.shoola.util.image.io.WriterImage;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

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
class ROISaverMng
{
    
    private ROISaver            view;
    
    private StatsResultsPaneMng mng;
    
    private String              format, fileName, message;
    
    ROISaverMng(ROISaver view, StatsResultsPaneMng mng) 
    {
        this.view = view;
        this.mng = mng; 
    }
    
    ROISaver getView() { return view; }
    
    /** Bring up the selection dialog. */
    void showSelectionDialog()
    {
        IconManager im = IconManager.getInstance(mng.getRegistry());
        SelectionDialog dialog = new SelectionDialog(this, 
                                        im.getIcon(IconManager.QUESTION));
        dialog.pack();  
        UIUtilities.centerAndShow(dialog);
    }

    void setDisplay(boolean b) { view.setDisplay(b); }
    
    void setInfo(String format, String fileName, String message)
    {
        if (format == null || fileName == null) {
            UserNotifier un = mng.getRegistry().getUserNotifier();
            un.notifyError("Save ROI results", "The name cannot be null");
            return;
        }
        this.format = format;
        this.fileName = fileName;
        this.message = message;
    }
    
    /** Dispose and close. */
    void disposeView()
    {
        view.setVisible(false);
        view.dispose();
    }
    
    /** Save the result according to the format selected. */
    void saveROIResult()
    {
        UserNotifier un = mng.getRegistry().getUserNotifier();
        fileName +="."+format; //Add extension
        File f = new File(fileName);
        try {
            if (format.equals(TEXTFilter.TEXT))
                WriterText.writeTableAsText(f, mng.getTableModel());
            else if (format.equals(JPEGFilter.JPG) || 
                        format.equals(PNGFilter.PNG))
                saveGraphicImage(f, null); 
            un.notifyInfo("ROI results saved", message);
        } catch (Exception e) {
            f.delete();
            un.notifyError("Save ROI result failure", "Unable to save " +
                    "the result of the ROI analysis.", e);
        }
    }
    
    private void saveGraphicImage(File f, Encoder encoder)
        throws Exception
    {
        BufferedImage img = mng.getGraphicImage();
        if (encoder == null) 
            WriterImage.saveImage(f, img, format);
        else  
            WriterImage.saveImage(f, encoder, img);
    }
    
}
