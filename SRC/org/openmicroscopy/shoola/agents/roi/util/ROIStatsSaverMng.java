/*
 * org.openmicroscopy.shoola.agents.roi.util.ROIStatsSaverMng
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

package org.openmicroscopy.shoola.agents.roi.util;


//Java imports
import java.io.File;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.roi.IconManager;
import org.openmicroscopy.shoola.agents.roi.ROIAgtCtrl;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.filter.file.TEXTFilter;
import org.openmicroscopy.shoola.util.filter.file.XMLFilter;
import org.openmicroscopy.shoola.util.file.WriterText;
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
class ROIStatsSaverMng
{
    
    private ROIStatsSaver       view;
    private ROIAgtCtrl          control;
    private ROIStatsMng         mng;
    
    ROIStatsSaverMng(ROIStatsSaver view, ROIStatsMng mng, ROIAgtCtrl control) 
    {
        this.view = view;
        this.mng = mng;
        this.control = control; 
    }
    
    ROIStatsSaver getView() { return view; }
    
    /** Bring up the selection dialog. */
    void showSelectionDialog(String format, String fileName, String message)
    {
        IconManager im = IconManager.getInstance(control.getRegistry());
        SelectionDialog dialog = new SelectionDialog(this, format,
                                        fileName, message, 
                                        im.getIcon(IconManager.QUESTION));
        dialog.pack();  
        UIUtilities.centerAndShow(dialog);
    }
    
    void setDisplay(boolean b) { view.setDisplay(b); }
    
    /** Dispose and close. */
    void disposeView()
    {
        view.setVisible(false);
        view.dispose();
    }
    
    void saveROIResult(String format, String name, String msg)
    {
        UserNotifier un = control.getRegistry().getUserNotifier();
        if (mng.getROIStats() == null) 
            un.notifyError("Save ROI results", msg);
        else {
            name +="."+format; //Add extension
            File f = new File(name);
            try {
                if (format.equals(TEXTFilter.TEXT))
                    WriterText.writeTableAsText(f, mng.getModel());
                else if (format.equals(XMLFilter.XML))
                    WriterText.writeTableAsXML(f, mng.getModel());
                un.notifyInfo("ROI results saved", msg);
            } catch (Exception e) {
                f.delete();
                un.notifyError("Save ROI result failure", "Unable to save the " +
                        "result of the ROI analysis.",
                                   e);
            }
        }
    }
    
}
