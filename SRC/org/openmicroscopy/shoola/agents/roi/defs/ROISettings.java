/*
 * org.openmicroscopy.shoola.agents.roi.defs.ROISettings
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

package org.openmicroscopy.shoola.agents.roi.defs;


//Java imports

//Third-party libraries

//Application-internal dependencies

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
public class ROISettings
{
    
    private int startZ;
    
    private int endZ;
    
    private int startT;
    
    private int endT;

    private boolean zSelected, tSelected, ztSelected;
    
    public ROISettings(int startZ, int endZ, int startT, int endT) 
    {
        this.startZ = startZ;
        this.endZ = endZ;
        this.startT = startT;
        this.endT = endT;
        ztSelected = true;
    }

    public boolean isZSelected() { return zSelected; }
    
    public boolean isTSelected() { return tSelected; }
    
    public boolean isZTSelected() { return ztSelected; }
    
    public void setZSelected(boolean b) { zSelected = b; }
    
    public void setTSelected(boolean b) { tSelected = b; }
    
    public void setZTSelected(boolean b) { ztSelected = b; }
    
    public int getEndT() { return endT; }

    public int getEndZ() { return endZ; }

    public int getStartT() { return startT; }

    public int getStartZ() { return startZ; }

    public void setStartZ(int v) { startZ = v; }
    
    public void setStartT(int v) { startT = v; }
    
    public void setEndZ(int v) { endZ = v; }
    
    public void setEndT(int v) { endT = v; }
    
}
