/*
 * org.openmicroscopy.shoola.agents.viewer.controls.BottomBar
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

package org.openmicroscopy.shoola.agents.viewer.controls;


//Java imports
import java.awt.FlowLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies


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
public class BottomBar
    extends JPanel
{
    
    public static final int ANNOTATE = 0, EDIT = 1,  LENS = 2;
    
    private static final String ROI_ANNOTATE = "Double click to annotate. ";
    
    private static final String ROI_EDIT = "Double click to edit. " ;
    
    private static final String LENS_MSG = "Double click to " +
                        "bring up the image inspector (zoom, lens, etc.).";
    
    private static final int LIMIT = 30;
    
    private JLabel  msgLabel;
    
    public BottomBar()
    {
        buildGUI();
    }
    
    public void resetMessage(int index)
    {
        String msg = "";
        if (index == ANNOTATE) msg = ROI_ANNOTATE;
        else if (index == EDIT) msg = ROI_EDIT;
        else if (index == LENS) msg = LENS_MSG;
        msgLabel.setText(setLabelText(msg, null));
    }
    
    public void setMessage(String txt)
    {
        msgLabel.setText(setLabelText(ROI_EDIT, txt));
    }
    
    private String setLabelText(String msg, String txt)
    {
        StringBuffer buf = new StringBuffer(LIMIT+msg.length());
        buf.append("<html><body>");
        buf.append(msg);
        if (txt != null) {
            String s = "";
            String tail = "";
            int length = txt.length();
            if (length > LIMIT) {
                tail = "...";
                length = LIMIT;
            }
            for (int i = 0; i < length; i++) {
                s += txt.charAt(i);
            }
            s += tail;
            //buf.append("<br>"); 
            buf.append("<b>"+s+"</b>"); 
        }
        buf.append("</body></html>");
        return buf.toString();
    }
    
    /** Build and lay out the GUI. */
    private void buildGUI()
    {
        msgLabel = new JLabel(); //default
        resetMessage(LENS);
        setLayout(new FlowLayout(FlowLayout.LEFT));
        add(msgLabel);
    }
    
}
