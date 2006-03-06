/*
 * ome.util.Validator
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
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

package ome.util;

// Java imports

// Third-party libraries
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// Application-internal dependencies
import ome.model.IObject;
import ome.model.acquisition.AcquisitionContext;
import ome.model.acquisition.ImagingEnvironment;
import ome.model.core.Channel;
import ome.model.core.Pixels;
import ome.model.display.Color;

/**
 * tests of model objects for validity.
 * 
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since 1.0
 */
public abstract class Validator
{

    protected static Log log = LogFactory.getLog(Validator.class);

    public static Validation validate(IObject obj)
    {
        return Validation.VALID();
    }

    public static Validation validate(Channel channel)
    {
        Validation v = Validation.VALID();

        /**
         * this needs to span multiple types. we need to be an attached graph so
         * we can check those relationships. Can be assume that? Non-Anemic
         * model? Do null tests count?
         */
        // Pixels pixels = channel.getPixels (need inverse)
        AcquisitionContext acqCtx = new Pixels().getAcquisitionContext(); // FIXME
        Color color = channel.getColorComponent();
        String piType = acqCtx.getPhotometricInterpretation().getValue(); // TODO
        // null
        // Safe?
        if (piType.equals("RGB") || piType.equals("ARGB")
                || piType.equals("CMYK") || piType.equals("HSV"))
        {
            if (color == null)
                v
                        .invalidate("Channel.color cannot be null if PiType == {RGB|ARGB|CMYK|HSV}");
        }

        return v;
    }

    public static Validation validate(ImagingEnvironment imageEnvironment)
    {
        Validation v = Validation.VALID();

        Float co2 = imageEnvironment.getCo2percent();
        if (null != co2 && (co2.floatValue() < 0 || co2.floatValue() > 1))
        {
            v.invalidate("ImageEnvironment.co2percent must be between 0 and 1");
        }

        Float humidity = imageEnvironment.getHumidity();
        if (null != humidity
                && (humidity.floatValue() < 0 || humidity.floatValue() > 1))
        {
            v.invalidate("ImageEnvironment.humidity must be between 0 and 1");
        }

        return v;
    }

    public static Validation valid(Pixels pixels)
    {
        Validation v = Validation.VALID();

        /** careful; collections! */
        int planeInfoSize = pixels.sizeOfPlaneInfo();
        int sizeC = pixels.getSizeC().intValue(); // TODO and sizeX null
        // (excep? or invalidate?)
        int sizeT = pixels.getSizeT().intValue();
        int sizeZ = pixels.getSizeZ().intValue();
        if (!(planeInfoSize == 0 || planeInfoSize == sizeC * sizeT * sizeZ))
            v
                    .invalidate("Size of planeInfo in Pixels should be 0 or sizeC*sizeT*sizeZ");

        return v;
    }

}
