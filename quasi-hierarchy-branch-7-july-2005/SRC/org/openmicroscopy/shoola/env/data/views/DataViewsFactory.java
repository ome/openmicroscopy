/*
 * org.openmicroscopy.shoola.env.data.views.DataViewsFactory
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

package org.openmicroscopy.shoola.env.data.views;


//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.Container;
import org.openmicroscopy.shoola.env.config.Registry;

/** 
 * Supplies suitable implementations for each of the <code>DataServicesView
 * </code> interfaces defined in this package.
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
public class DataViewsFactory
{

    /** 
     * A reference to the container's registry.
     * We store it here at initialization time so that it can later be 
     * retrieved by {@link BatchCallTree} every time a new object is
     * instantiated.
     */
    private static Registry   context;
    
    
    /**
     * Returns a reference to the container's registry.
     * 
     * @return See above.
     */
    static Registry getContext() { return context; }
    
    
    /**
     * Intializes this singleton.
     * This method has to be called during the container's intialization
     * procedure, after a reference to the container's registry is made
     * available.
     * 
     * @param c Reference to the container.  Mustn't be <code>null</code>.
     */
    public static void initialize(Container c)
    {
        if (c == null)
            throw new NullPointerException();  //An agent called this method?
        context = c.getRegistry();
    }
    
    /**
     * Returns an implementation of the specified <code>view</code>.
     * The <code>view</code> argument specifies the interface class that
     * defines the desired view.  It has to be one of the sub-interfaces 
     * of <code>DataServicesView</code> defined in this package.  The
     * returned object implements the <code>view</code> interface and can
     * be safely cast to it.
     * 
     * @param view The view's interface.
     * @return An implementation of the specified <code>view</code>.
     * @throws NullPointerException If <code>view</code> is <code>null</code>.
     * @throws IllegalArgumentException If <code>view</code> is not one of the
     *          supported <code>DataServicesView</code> interfaces defined in 
     *          this package.
     */
    public static DataServicesView makeView(Class view)
    {
        if (view == null) throw new NullPointerException("No view specified.");
        DataServicesView dsv = null;
        if (view.equals(HierarchyBrowsingView.class))
            dsv = new HierarchyBrowsingViewImpl();
        //else if (view.equals(..)) ...
        if (dsv == null) 
            throw new IllegalArgumentException("Unknown view type: "+view+".");
        return dsv;
    }

}
