/*
 * org.openmicroscopy.shoola.env.ui.SoftwareUpdateDialog
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

package org.openmicroscopy.shoola.env.ui;


//Java imports
import javax.swing.JDialog;
import javax.swing.JFrame;

//Third-party libraries

//Application-internal dependencies

/** 
 * Basic modal dialog displaying the last version of the software.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
class SoftwareUpdateDialog
    extends JDialog
{

    /** The client's version. */
    private static final String     VERSION = "3.0_M3 ";
    
    /** The server's version. For developers' purpose only. */
    private static final String     OMERO_VERSION = " server: OMERO M3";
    
    public static String            revisionDate = "$LastChangedDate$";
    
    public static String            releaseDate = "$Date$";
    
    private String getPrintableKeyword(String keyword)
    {
        int begin = keyword.indexOf(" ") + 1;
        int end = keyword.lastIndexOf(" ");
        return keyword.substring(begin, end);
    }
    
    /**
     * Creates a new intance.
     * 
     * @param owner The owner of the frame.
     */
    SoftwareUpdateDialog(JFrame owner)
    {
        super(owner);
        System.out.println(getPrintableKeyword(revisionDate));
        setSize(300, 300);
    }
    
}
