/*
 * org.openmicroscopy.shoola.agents.hiviewer.tframe.TinyFrameStaticUI
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

package org.openmicroscopy.shoola.agents.hiviewer.tframe;


//Java imports
import javax.swing.JInternalFrame;
import javax.swing.event.MouseInputAdapter;

//Third-party libraries

//Application-internal dependencies

/** 
 * A thin extension to the default {@link TinyFrame}'s UI that makes the UI
 * completely static.
 * That is, it will behave like a <code>JPanel</code> and be suitable to be
 * added to components other than a <code>JDesktopPane</code>.
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
public class TinyFrameStaticUI
    extends TinyFrameUI
{

    /** Overridden to return a better border for this UI delegate. */
    protected FrameBorder makeBorder()
    {
        return new FrameBorder(BORDER_COLOR, DESKTOP_COLOR, 0);
    }
    
    /** Overridden so we don't respond to resize, move, etc. events. */
    protected MouseInputAdapter createBorderListener(JInternalFrame frame)
    {
        return new MouseInputAdapter() {};
    }  
    //TODO: Why is this making us lose mouse clicks on inner nodes?
    //TODO: We also lose the resize cursor on inner nodes.  The user has to
    //click on the border to resize and the area that is sensible to this
    //appears to be just one pixel wide.  (Note that the border is 3px.)
    
    /**
     * Creates a new UI delegate for the specified <code>frame</code>.
     * 
     * @param frame The frame that will own this UI delegate.  
     *              Mustn't be <code>null</code>.
     */
    public TinyFrameStaticUI(TinyFrame frame)
    {
        super(frame);
    }
    
}
