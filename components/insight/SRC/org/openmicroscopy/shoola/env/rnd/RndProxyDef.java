/*
 * org.openmicroscopy.shoola.env.rnd.RndProxyDef
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.env.rnd;




//Java imports
import java.awt.Color;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import omero.model.RenderingDef;

//Third-party libraries

//Application-internal dependencies

/** 
 * Stores the rendering settings to speed-up the process.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *          <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 *          <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME2.2
 */
public class RndProxyDef
{

    /** The default z-section. Cached value to speed up the process. */
    private int defaultZ;

    /** The default timepoint. Cached value to speed up the process. */
    private int defaultT;

    /** The bit resolution. Cached value to speed up the process. */
    private int bitResolution;

    /** 
     * The lower bound of the codomain interval.
     * Cached value to speed up the process.
     */
    private int cdStart;

    /** 
     * The upper bound of the codomain interval.
     * Cached value to speed up the process.
     */
    private int cdEnd;

    /** The color model. Cached value to speed up the process. */
    private String colorModel;

    /** The codomain the channel bindings. */
    private Map<Integer, ChannelBindingsProxy>	channels;

    /** Flag indicating is the pixels type is signed or not. */
    private boolean typeSigned;

    /** The compression value. */
    private double compression;

    /** Indicates when the settings was last modified. */
    private Timestamp lastModified;

    /** The name associated to the rendering settings. */
    private String name;

    /** The original object.*/
    private RenderingDef data;

    /**
     * Creates a new instance.
     *
     * @param data The original object
     * */
    RndProxyDef(RenderingDef data)
    {
        this.data = data;
        compression = 1.0;
        channels = new HashMap<Integer, ChannelBindingsProxy>();
        name = "";
    }

    /**
     * Sets the name associated to the rendering def. 
     * 
     * @param name The name to set.
     */
    void setName(String name) { this.name = name; }

    /**
     * Returns the name.
     * 
     * @return See above.
     */
    String getName() { return name; }

    /**
     * Sets when the settings were last modified.
     * 
     * @param lastModified The value to set.
     */
    void setLastModified(Timestamp lastModified)
    { 
        this.lastModified = lastModified;
    }

    /**
     * Returns the number of channels.
     * 
     * @return See above.
     */
    int getNumberOfChannels() { return channels.size(); }

    /**
     * Sets the bindings corresponding to the specified channel.
     * 
     * @param index The channel index.
     * @param c The value to set.
     */
    void setChannel(int index, ChannelBindingsProxy c) 
    {
        channels.put(Integer.valueOf(index), c);
    }

    /**
     * Returns the bindings corresponding to the specified channel.
     * 
     * @param index The channel index.
     * @return See above.
     */
    ChannelBindingsProxy getChannel(int index)
    {
        return channels.get(Integer.valueOf(index));
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
     * @param cdStart The lower bound of the interval.
     * @param cdEnd The upper bound of the interval.
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
     * Sets to <code>true</code> if the pixels type is signed,
     * <code>false</code> otherwise.
     * 
     * @param b The value to set.
     */
    void setTypeSigned(boolean b) { typeSigned = b; }

    /**
     * Returns <code>true</code> if the pixels type is signed,
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    boolean isTypeSigned() { return typeSigned; }

    /**
     * Returns the compression, a value in the interval ]0, 1].
     * 
     * @return See above.
     */
    double getCompression() { return compression; }

    /**
     * Sets the compression, a value in the interval ]0, 1].
     * 
     * @param compression See above.
     */
    void setCompression(double compression)
    {
        if (compression <= 0) compression = 0.1;
        else if (compression > 1) compression = 1.0;
        this.compression = compression;
    }

    /**
     * Creates and returns a copy of the element.
     * 
     * @return See above.
     */
    RndProxyDef copy()
    {
        RndProxyDef copy = new RndProxyDef(this.data);
        copy.setLastModified(this.getLastModified());
        copy.setCompression(this.getCompression());
        copy.setTypeSigned(this.isTypeSigned());
        copy.setDefaultZ(this.getDefaultZ());
        copy.setDefaultT(this.getDefaultT());
        copy.setBitResolution(this.getBitResolution());
        copy.setColorModel(this.getColorModel());
        copy.setCodomain(this.getCdStart(), this.getCdEnd());
        Iterator<Integer> i = channels.keySet().iterator();
        int index;
        while (i.hasNext()) {
            index = i.next();
            copy.setChannel(index, this.getChannel(index).copy());
        }
        return copy;
    }

    /**
     * Returns the currently selected time-point.
     * 
     * @return See above.
     */
    public int getDefaultT() { return defaultT; }

    /**
     * Returns the currently selected z-section.
     * 
     * @return See above.
     */
    public int getDefaultZ() { return defaultZ; }

    /**
     * Returns the color associated to channel.
     * 
     * @param index The index of the channel.
     * @return See above.
     */
    public Color getChannelColor(int index)
    {
        ChannelBindingsProxy channel = getChannel(index);
        if (channel == null) return null;
        int[] rgba = channel.getRGBA();
        return new Color(rgba[0], rgba[1], rgba[2], rgba[3]);
    }

    /**
     * Returns when the settings were last modified.
     * 
     * @return See above.
     */
    public Timestamp getLastModified() { return lastModified; }

    /** 
     * Returns the owner's identifier of the rendering settings.
     *
     * @return See above.
     */
    public long getOwnerID()
    {
        return data.getDetails().getOwner().getId().getValue();
    }

    /**
     * Returns the identifier of the settings.
     *
     * @return See above.
     */
    public long getDataID()
    {
        return data.getId().getValue();
    }

    /**
     * Returns the data hosted by this class.
     *
     * @return See above.
     */
    public RenderingDef getData() { return data; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName() + "\n");
        Iterator<Integer> i = channels.keySet().iterator();
        int index;
        while (i.hasNext()) {
            index = i.next();
            ChannelBindingsProxy channel = this.getChannel(index);
            int[] rgba = channel.getRGBA();
            if (channel.isActive())
                sb.append("* ");
            else
                sb.append(" ");
            sb.append("[" + rgba[0] + "," + rgba[1] + "," + rgba[2] + ","
                    + rgba[3] + "] ");
            sb.append(" start=" + channel.getInputStart() + ", end="
                    + channel.getInputEnd() + " \n");
        }
        return sb.toString();
    }
    
}
