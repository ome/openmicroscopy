/*
 * org.openmicroscopy.shoola.agents.roi.defs.ScreenROI
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
import java.awt.Color;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.image.roi.ROI3D;
import org.openmicroscopy.shoola.util.image.roi.ROI4D;
import org.openmicroscopy.shoola.util.image.roi.ROI5D;
import org.openmicroscopy.shoola.util.math.geom2D.PlaneArea;
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
public class ScreenROI
{

    private int     index;
    
    private Color   areaColor;
    
    private String  name;
    
    private String  annotation;
    
    private ROI4D   logicalROI;
    
    private ROI5D   actualROI;
    
    public ScreenROI(int index,  String  name, String  annotation, 
                    Color areaColor, ROI4D logicalROI)
    {
        if (index <= -1) 
            throw new IllegalArgumentException("not valid index.");
        if (logicalROI == null)
            throw new IllegalArgumentException("logicalROI not valid.");
        this.index = index;
        this.areaColor = areaColor;
        this.name = name;
        this.annotation = annotation;
        this.logicalROI = logicalROI;
    }
    
    //tempo
    public ROI5D getActualROI() { return actualROI; }
    
    public void setActualROI(ROI5D roi5D) { actualROI = roi5D; }
    
    public int getIndex() { return index; }
    
    public void setIndex(int i) { index = i;}
    
    public String getName() { return name; }
    
    public void setName(String name) { this.name = name; }
    
    public String getAnnotation() { return annotation; }
    
    public void setAnnotation(String s) { annotation = s; }
    
    public Color getAreaColor() { return areaColor; }
    
    public void setAreaColor(Color c) { areaColor = c; }
    
    public ROI4D getLogicalROI() { return logicalROI; }
    
    public void copyAcrossZ(PlaneArea pa, int from, int to, int t)
    {
        if (pa == null) return;
        for (int z = from; z <= to; z++)
            logicalROI.setPlaneArea((PlaneArea) (pa.copy()), z, t);
    }
    
    public void copyAcrossT(PlaneArea pa, int from, int to, int z)
    {
        if (pa == null) return;
        for (int t = from; t <= to; t++)
            logicalROI.setPlaneArea((PlaneArea) (pa.copy()), z, t);
    }
    
    public void copyAcrossZAndT(PlaneArea pa, int fromZ, int toZ, int fromT,
                                int toT)
    {
        if (pa == null) return;
        for (int t = fromT; t <= toT; t++) {
            for (int z = fromZ; z <= toZ; z++) {
                logicalROI.setPlaneArea((PlaneArea) (pa.copy()), z, t);
            }
        }
    }
    
    public void copyStackAcrossT(int from, int to, int sizeZ)
    {
        PlaneArea pa;
        ROI3D roi3D = logicalROI.getStack(from);
        for (int z = 0; z < sizeZ; z++) {
            pa = roi3D.getPlaneArea(z);
            for (int t = from+1; t <= to; t++) {
              if (pa != null)
                  logicalROI.setPlaneArea(
                          (PlaneArea) (pa.copy()), z, t);
            }
        }
    }
    
    public void copyStack(int from, int to, int sizeZ)
    {
        PlaneArea pa;
        for (int z = 0; z < sizeZ; z++) {
            pa = logicalROI.getPlaneArea(z, from);
            if (pa != null) {
                logicalROI.setPlaneArea((PlaneArea) (pa.copy()), z, to);
            }
        }
    }
}
