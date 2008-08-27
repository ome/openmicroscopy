/*
 *  $Id$
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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

package ome.formats.importer;

import ome.model.acquisition.LightSource;


/**
 * Since the LightSource class is abstract, MetaLightSource is used 
 * as a place holder for any sets  from bio-formats before we know 
 * specifically what kind of light source we are dealing with 
 * (laser, arc, or filament). Once any of the concrete methods are 
 * called (for example, setLaserWavelength()), any values stored in
 * this temporary object will be copied into the new concrete object.
 * 
 * @author Brian W. Loranger
 *
 */
public class MetaLightSource extends ome.model.acquisition.LightSource
{
   
    public void copyData(LightSource lightSource)
    {
        lightSource.setManufacturer(manufacturer);
        lightSource.setModel(model);
        lightSource.setPower(power);
        lightSource.setSerialNumber(serialNumber);
    }
    
}
