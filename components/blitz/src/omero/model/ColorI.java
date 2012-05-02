/*
 * omero.model.ColorI 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2012 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
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
package omero.model;

import Ice.Current;
import Ice.Object;

import ome.model.ModelBased;
import ome.util.Filterable;
import ome.util.ModelMapper;
import ome.util.ReverseModelMapper;
import ome.util.Utils;
import omero.RInt;
import omero.RLong;
import static omero.rtypes.rint;
/** 
 * 
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.4
 */
public class ColorI extends Color implements ModelBased {

    public final static Ice.ObjectFactory Factory = new Ice.ObjectFactory() {

        public Object create(String arg0) {
            return new ColorI();
        }

        public void destroy() {
            // no-op
        }
        
    };
    
	public ColorI() {
		setValue(0);
	}

	public ColorI(int color) {
		setValue(color);
	}

	public int getValue(Ice.Current current) {
		return value;
	}

	public void setValue(int color, Ice.Current current) {
		this.value = color;
	}
	
    /**
     * Returns the <code>red</code> component of the color.
     * 
     * @return See above.
     */
    public int getRed(Ice.Current current)
    {
    	return 0; //To be implemented.
    }
    
    /**
     * Returns the <code>green</code> component of the color.
     * 
     * @return See above.
     */
    public int getGreen(Ice.Current current)
    {
    	return 0; //To be implemented.
    }
    
    /**
     * Returns the <code>blue</code> component of the color.
     * 
     * @return See above.
     */
    public int getBlue(Ice.Current current)
    {
    	return 0; //To be implemented.
    }
    
    /**
     * Returns the <code>alpha</code> component of the color.
     * 
     * @return See above.
     */
    public int getAlpha(Ice.Current current)
    {
    	return 0; //To be implemented.
    }

    public void setRed(int red, Current __current) {
        // FIXME
    }

    public void setGreen(int green, Current __current) {
        // FIXME
    }

    public void setBlue(int blue, Current __current) {
        // FIXME
    }

    public void setAlpha(int alpha, Current __current) {
        // FIXME
    }

    public Color proxy(Current __current) {
        return new ColorI(value);
    }

    public void copyObject(Filterable model, ModelMapper _mapper) {
        if (model instanceof ome.model.core.Color) {
            ome.model.core.Color source = (ome.model.core.Color) model;
            this.setValue(source.getColor());
        } else {
            throw new IllegalArgumentException("Color cannot copy from "
                    + (model == null ? "null" : model.getClass().getName()));
        }
    }

    public ome.util.Filterable fillObject(ome.util.ReverseModelMapper _mapper) {
        omero.util.IceMapper mapper = (omero.util.IceMapper) _mapper;
        ome.model.core.Color target = new ome.model.core.Color(
                this.getValue());
        mapper.store(this, target);
        return target;
    }


}
