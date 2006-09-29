/*
 * org.openmicroscopy.shoola.util.ui.IconManager
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

package org.openmicroscopy.shoola.util.ui;


//Java imports
import javax.swing.Icon;


//Third-party libraries

//Application-internal dependencies

/** 
 * Provides the icons used by the util.ui package.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
public class IconManager
{

    /** ID of the <code>Colour slider</code> icon. */
    public static final int COLOUR_SLIDER = 0;
    
    /** ID of the <code>Colour watch</code> icon. */
    public static final int COLOUR_SWATCH = 1;
    
    /** ID of the <code>Colour wheel</code> icon. */
    public static final int COLOUR_WHEEL = 2;
    
    /** ID of the <code>Cancel</code> icon. */
    public static final int CANCEL = 3;
    
    /** ID of the <code>OK</code> icon. */
    public static final int OK = 4;
    
    /** ID of the <code>Undo</code> icon. */
    public static final int UNDO = 5;
    
    /** 
     * The maximum ID used for the icon IDs.
     * Allows to correctly build arrays for direct indexing. 
     */
    private static int      MAX_ID = 5;
    
    /** Paths of the icon files. */
    private static String[]     relPaths = new String[MAX_ID+1];
    static {  
        relPaths[COLOUR_SLIDER] = "coloursliders24.png";
        relPaths[COLOUR_SWATCH] = "colourswatch24.png";
        relPaths[COLOUR_WHEEL] = "colourwheel24.png";
        relPaths[CANCEL] = "nuvola_cancel22.png";
        relPaths[OK] = "nuvola_button_accept22.png";
        relPaths[UNDO] = "nuvola_undo22.png";
    }
    
    /** 
     * Retrieves the icon specified by <code>id</code>.
     * If the icon can't be retrieved, then this method will log the error and
     * return <code>null</code>.
     *
     * @param id    The index of the file name in the array of file names 
     *              specified to this class' constructor.
     * @return  An {@link Icon} object created from the image file.  The return
     *          value will be <code>null</code> if the file couldn't be found
     *          or an image icon couldn't be created from that file.
     */ 
    public Icon getIcon(int id)
    {
        if (id < 0 || relPaths.length <= id) return null;
        return getIcon(relPaths[id]);
    }
    
    /** 
     * Retrieves the icon specified by <code>name</code>.
     * If the icon can't be retrieved, then this method will log the error and
     * return <code>null</code>.
     *
     * @param name    Must be one a valid icon file name within the directory
     *                  used by the {@link IconFactory} instance specified via
     *                  this class' constructor.
     * @return  An {@link Icon} object created from the image file.  The return
     *          value will be <code>null</code> if the file couldn't be found
     *          or an image icon couldn't be created from that file.
     */ 
    public Icon getIcon(String name)
    {
        Icon icon = factory.getIcon(name);
        if (icon == null) {
            StringBuffer buf = new StringBuffer("Failed to retrieve icon: ");
            buf.append("<classpath>");
            buf.append(factory.getResourcePathname(name));
            buf.append(".");
        }
        return icon;
    }
    
    /** The sole instance. */
    private static IconManager  singleton;
    
    /** The factory. */
    private IconFactory         factory;
 
    /** 
     * Returns the <code>IconManager</code> object. 
     * 
     * @return See above.
     */
    public static IconManager getInstance()
    {
        if (singleton == null) singleton = new IconManager();
        return singleton;
    }
    
    /** Creates a new instance and configures the parameters. */
    private IconManager()
    {
        factory = new IconFactory();
    }
    
}
