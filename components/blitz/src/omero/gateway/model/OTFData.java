/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package omero.gateway.model;

import omero.RInt;
import omero.model.OTF;

/** 
 * Hosts an OTF.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class OTFData
    extends DataObject
{

    /** The objective associated. */
    private ObjectiveData objective;

    /** The filterSet associated. */
    private FilterSetData filterSet;

    /**
     * Creates a new instance.
     *
     * @param otf The OTF to host. Mustn't be <code>null</code>.
     */
    public OTFData(OTF otf)
    {
        if (otf == null)
            throw new IllegalArgumentException("OTF cannot null.");
        setValue(otf);
    }

    /**
     * Returns the associated objective.
     *
     * @return See above.
     */
    public ObjectiveData getObjective()
    {
        if (objective != null) return objective;
        objective = new ObjectiveData(((OTF) asIObject()).getObjective());
        return objective;
    }

    /**
     * Returns the associated filter set.
     *
     * @return See above.
     */
    public FilterSetData getFilterSet()
    {
        if (filterSet != null) return filterSet;
        filterSet = new FilterSetData(((OTF) asIObject()).getFilterSet());
        return filterSet;
    }

    /**
     * Returns a boolean flag if the value has been set
     *
     * @return See above.
     */
    public boolean hasOpticalAxisAveraged()
    {
        OTF otf = (OTF) asIObject();
        return otf.getOpticalAxisAveraged().getValue();
    }

    /**
     * Returns the path.
     *
     * @return See above.
     */
    public String getPath()
    {
        OTF otf = (OTF) asIObject();
        return otf.getPath().getValue();
    }

    /**
     * Returns the size along the X-axis.
     *
     * @return See above.
     */
    public int getSizeX()
    {
        OTF otf = (OTF) asIObject();
        RInt v = otf.getSizeX();
        return v.getValue();
    }

    /**
     * Returns the size along the Y-axis.
     *
     * @return See above.
     */
    public int getSizeY()
    {
        OTF otf = (OTF) asIObject();
        RInt v = otf.getSizeY();
        return v.getValue();
    }

    /**
     * Returns the pixels type.
     *
     * @return See above.
     */
    public String getPixelsType()
    {
        OTF otf = (OTF) asIObject();
        return otf.getPixelsType().getValue().getValue();
    }

}
