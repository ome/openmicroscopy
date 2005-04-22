/*
 * org.openmicroscopy.shoola.agents.hiviewer.actions.BrowserAction
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

package org.openmicroscopy.shoola.agents.hiviewer.actions;




//Java imports
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;



//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.Colors;
import org.openmicroscopy.shoola.agents.hiviewer.HiViewerCtrl;
import org.openmicroscopy.shoola.agents.hiviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;

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
public abstract class BrowserAction
    extends AbstractAction
    implements PropertyChangeListener
{

    protected HiViewerCtrl  agentCtrl;
    
    protected Browser       browser;
    
    public BrowserAction(HiViewerCtrl agentCtrl)
    {
        super();
        this.agentCtrl = agentCtrl;
    }
    
    protected abstract void onDisplayChange(ImageDisplay selectedDisplay);
    
    public void setBrowser(Browser browser)
    {
        this.browser = browser;
        browser.addPropertyChangeListener(Browser.SELECTED_DISPLAY_PROPERTY, 
                                            this);
    }
    
    public Browser getBrowser() { return browser; }
    
    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
        //Highlight the selected node.
        ImageDisplay oldNode, newNode;
        if (evt.getNewValue() != null) {
            newNode = (ImageDisplay) evt.getNewValue();
            newNode.setHighlight(Colors.getSelectedHighLight(newNode));
            if (evt.getOldValue() != null) {
                oldNode = (ImageDisplay) evt.getOldValue();
                oldNode.setHighlight(Colors.getDeselectedHighLight(oldNode));
            }
        }
        onDisplayChange(browser.getSelectedDisplay());
    }

}
