/*
 * org.openmicroscopy.shoola.env.rnd.RndProxyDef
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

package org.openmicroscopy.shoola.env.rnd;

import java.util.HashMap;
import java.util.Map;


//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * 
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
class RndProxyDef
{

    /** The default z-section. Cached value to speed up the process. */
    private int     defaultZ;
    
    /** The default timepoint. Cached value to speed up the process. */
    private int     defaultT;
    
    /** The bit resolution. Cached value to speed up the process. */
    private int     bitResolution;
    
    /** 
     * The lower bound of the codomain interval.
     * Cached value to speed up the process. 
     */
    private int     cdStart;
    
    /** 
     * The upper bound of the codomain interval.
     * Cached value to speed up the process. 
     */
    private int     cdEnd;
    
    /** The color model. Cached value to speed up the process. */
    private String  colorModel;
    
    /** The codomain the channel bindings. */
    private Map     channels;
    
    /** Creates a new instance. */
    RndProxyDef()
    {
        channels = new HashMap();
    }
    
    /**
     * Sets the bindings corresponding to the specified channel.
     * 
     * @param index The channel index.
     * @param c     The value to set.
     */
    void setChannel(int index, ChannelBindingsProxy c) 
    {
        channels.put(new Integer(index), c);
    }
    
    /**
     * Returns the bindings corresponding to the specified channel.
     * 
     * @param index The channel index.
     * @return See above.
     */
    ChannelBindingsProxy getChannel(int index)
    {
         return (ChannelBindingsProxy) channels.get(new Integer(index));
    }
    
    /**
     * Sets the selected z-section.
     * 
     * @param z The value to set.
     */
    void setDefaultZ(int z) { defaultZ = z; }
    
    /**
     * Sets the selected timepoint.
     * 
     * @param t The value to set.
     */
    void setDefaultT(int t) { defaultT = t; }

    /**
     * Returns the bit resolution.
     * 
     * @return See above.
     */
    int getBitResolution() { return bitResolution; }

    /**
     * Sets the bit resolution.
     * 
     * @param bitResolution The value to set.
     */
    void setBitResolution(int bitResolution)
    { 
        this.bitResolution = bitResolution;
    }

    /**
     * Returns the upper bound of the codomain interval.
     * 
     * @return See above. 
     */
    int getCdEnd() { return cdEnd; }

    /**
     * Sets the bounds of the codomain interval.
     * 
     * @param cdStart   The lower bound of the interval.
     * @param cdEnd     The upper bound of the interval.
     */
    void setCodomain(int cdStart, int cdEnd)
    { 
        this.cdStart = cdStart; 
        this.cdEnd = cdEnd; 
    }

    /**
     * Returns the lower bound of the codomain interval.
     * 
     * @return See above. 
     */
    int getCdStart() { return cdStart; }
 
    /**
     * Returns the selected color model.
     * 
     * @return See above. 
     */
    String getColorModel() { return colorModel; }

    /**
     * Sets the color model.  
     * 
     * @param colorModel The value to set.
     */
    void setColorModel(String colorModel) { this.colorModel = colorModel; }
    
    /**
     * Returns the currently selected timepoint.
     * 
     * @return See above. 
     */
    int getDefaultT() { return defaultT; }

    /**
     * Returns the currently selected z-section.
     * 
     * @return See above. 
     */
    int getDefaultZ() { return defaultZ; }
    
}
