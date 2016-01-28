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
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package omero.gateway.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import omero.model.Dichroic;
import omero.model.LightPath;
import omero.model.LightPathEmissionFilterLink;
import omero.model.LightPathExcitationFilterLink;

/**
 * Object hosting a light path.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class LightPathData
    extends DataObject
{

    /** The collection of emission filters. */
    private List<FilterData> emissionFilters;

    /** The collection of excitation filters. */
    private List<FilterData> excitationFilters;

    /**
     * Creates a new instance.
     *
     * @param lightPath The lightPath to host. Mustn't be <code>null</code>.
     */
    public LightPathData(LightPath lightPath)
    {
        if (lightPath == null)
            throw new IllegalArgumentException("lightPath cannot null.");
        setValue(lightPath);
    }

    /**
     * Returns the collections of emission filters if any.
     *
     * @return See above.
     */
    public List<FilterData> getEmissionFilters()
    {
        LightPath path = (LightPath) asIObject();
        if (emissionFilters == null && path.sizeOfEmissionFilterLink() > 0) {
            emissionFilters = new ArrayList<FilterData>();
            List<LightPathEmissionFilterLink> l = path.copyEmissionFilterLink();
            Iterator<LightPathEmissionFilterLink> i = l.iterator();
            while (i.hasNext()) {
                emissionFilters.add(new FilterData(i.next().getChild()));
            }
        }
        return emissionFilters;
    }

    /**
     * Returns the collections of excitation filters if any.
     *
     * @return See above.
     */
    public List<FilterData> getExcitationFilters()
    {
        LightPath path = (LightPath) asIObject();
        if (excitationFilters == null && path.sizeOfExcitationFilterLink() > 0) {
            excitationFilters = new ArrayList<FilterData>();
            List<LightPathExcitationFilterLink> l = 
                    path.copyExcitationFilterLink();
            Iterator<LightPathExcitationFilterLink> i = l.iterator();
            while (i.hasNext()) {
                excitationFilters.add(new FilterData(i.next().getChild()));
            }
        }
        return excitationFilters;
    }

    /**
     * Returns the dichroic if any.
     *
     * @return See above.
     */
    public DichroicData getDichroic()
    {
        LightPath path = (LightPath) asIObject();
        Dichroic d = path.getDichroic();
        if (d == null) return null;
        return new DichroicData(d);
    }

}
