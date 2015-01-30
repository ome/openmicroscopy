/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2015 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.env.data.model;

//Java imports
import java.util.List;

/** 
 * Dialog used to save results from ImageJ.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 5.1.0
 */
public class ResultsObject {

    /** The objects holding the results information.*/
    public List<Object> refObjects;

    /** The objects to link the results to.*/
    public List<DataObject> targets;

    /** Indicates to save the roi if any.*/
    public boolean roi;
 
    /**
     * Creates a new instance.
     *
     * @param refObjects The objects holding the results information.
     * @param targets The objects to link the results to.
     */
    public ResultsObject(List<Object> refObjects, List<DataObject> targets)
    {
        this.refObjects = refObjects;
        this.targets = targets;
    }

    /**
     * Returns the objects holding the results information.
     * @return See above.
     */
    public List<Object> getRefObjects() { return refObjects; }

    /**
     * Returns the objects to link the results to.
     * @return See above.
     */
    public List<DataObject> getTargets() { return targets; }

    public void setROI(boolean roi) { this.roi = roi; }

    /**
     * Returns <code>true</code> to save the roi.
     *
     * @return See above.
     */
    public boolean isROI() { return roi; }
}
