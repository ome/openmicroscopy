/*
 * Copyright (C) 2014 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package omero.model;

import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

import ome.model.ModelBased;
import ome.units.unit.Unit;
import ome.util.Filterable;
import ome.util.ModelMapper;
import ome.util.ReverseModelMapper;
import ome.xml.model.enums.EnumerationException;

import omero.model.enums.UnitsFrequency;

/**
 * Blitz wrapper around the {@link ome.model.units.Frequency} class.
 * Like {@link Details} and {@link Permissions}, this object
 * is embedded into other objects and does not have a full life
 * cycle of its own.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 */
public class FrequencyI extends Frequency implements ModelBased {

    private static final long serialVersionUID = 1L;

    private static final Map<String, double[][]> conversions;
    static {
        Map<String, double[][]> c = new HashMap<String, double[][]>();

        c.put("ATTOHERTZ:CENTIHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -16}});
        c.put("ATTOHERTZ:DECIHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -17}});
        c.put("ATTOHERTZ:DEKAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -19}});
        c.put("ATTOHERTZ:EXAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -36}});
        c.put("ATTOHERTZ:FEMTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("ATTOHERTZ:GIGAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("ATTOHERTZ:HECTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -20}});
        c.put("ATTOHERTZ:HERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("ATTOHERTZ:KILOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("ATTOHERTZ:MEGAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("ATTOHERTZ:MICROHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("ATTOHERTZ:MILLIHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("ATTOHERTZ:NANOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("ATTOHERTZ:PETAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("ATTOHERTZ:PICOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("ATTOHERTZ:TERAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("ATTOHERTZ:YOCTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("ATTOHERTZ:YOTTAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -42}});
        c.put("ATTOHERTZ:ZEPTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("ATTOHERTZ:ZETTAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -39}});
        c.put("CENTIHERTZ:ATTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 16}});
        c.put("CENTIHERTZ:DECIHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("CENTIHERTZ:DEKAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("CENTIHERTZ:EXAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -20}});
        c.put("CENTIHERTZ:FEMTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 13}});
        c.put("CENTIHERTZ:GIGAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -11}});
        c.put("CENTIHERTZ:HECTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("CENTIHERTZ:HERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("CENTIHERTZ:KILOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -5}});
        c.put("CENTIHERTZ:MEGAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -8}});
        c.put("CENTIHERTZ:MICROHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("CENTIHERTZ:MILLIHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("CENTIHERTZ:NANOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 7}});
        c.put("CENTIHERTZ:PETAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -17}});
        c.put("CENTIHERTZ:PICOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 10}});
        c.put("CENTIHERTZ:TERAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -14}});
        c.put("CENTIHERTZ:YOCTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 22}});
        c.put("CENTIHERTZ:YOTTAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -26}});
        c.put("CENTIHERTZ:ZEPTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 19}});
        c.put("CENTIHERTZ:ZETTAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -23}});
        c.put("DECIHERTZ:ATTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 17}});
        c.put("DECIHERTZ:CENTIHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("DECIHERTZ:DEKAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("DECIHERTZ:EXAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -19}});
        c.put("DECIHERTZ:FEMTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 14}});
        c.put("DECIHERTZ:GIGAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -10}});
        c.put("DECIHERTZ:HECTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("DECIHERTZ:HERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("DECIHERTZ:KILOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("DECIHERTZ:MEGAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -7}});
        c.put("DECIHERTZ:MICROHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 5}});
        c.put("DECIHERTZ:MILLIHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("DECIHERTZ:NANOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 8}});
        c.put("DECIHERTZ:PETAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -16}});
        c.put("DECIHERTZ:PICOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 11}});
        c.put("DECIHERTZ:TERAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -13}});
        c.put("DECIHERTZ:YOCTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 23}});
        c.put("DECIHERTZ:YOTTAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -25}});
        c.put("DECIHERTZ:ZEPTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 20}});
        c.put("DECIHERTZ:ZETTAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -22}});
        c.put("DEKAHERTZ:ATTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 19}});
        c.put("DEKAHERTZ:CENTIHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("DEKAHERTZ:DECIHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("DEKAHERTZ:EXAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -17}});
        c.put("DEKAHERTZ:FEMTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 16}});
        c.put("DEKAHERTZ:GIGAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -8}});
        c.put("DEKAHERTZ:HECTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("DEKAHERTZ:HERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("DEKAHERTZ:KILOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("DEKAHERTZ:MEGAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -5}});
        c.put("DEKAHERTZ:MICROHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 7}});
        c.put("DEKAHERTZ:MILLIHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("DEKAHERTZ:NANOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 10}});
        c.put("DEKAHERTZ:PETAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -14}});
        c.put("DEKAHERTZ:PICOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 13}});
        c.put("DEKAHERTZ:TERAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -11}});
        c.put("DEKAHERTZ:YOCTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 25}});
        c.put("DEKAHERTZ:YOTTAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -23}});
        c.put("DEKAHERTZ:ZEPTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 22}});
        c.put("DEKAHERTZ:ZETTAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -20}});
        c.put("EXAHERTZ:ATTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 36}});
        c.put("EXAHERTZ:CENTIHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 20}});
        c.put("EXAHERTZ:DECIHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 19}});
        c.put("EXAHERTZ:DEKAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 17}});
        c.put("EXAHERTZ:FEMTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("EXAHERTZ:GIGAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("EXAHERTZ:HECTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 16}});
        c.put("EXAHERTZ:HERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("EXAHERTZ:KILOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("EXAHERTZ:MEGAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("EXAHERTZ:MICROHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("EXAHERTZ:MILLIHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("EXAHERTZ:NANOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("EXAHERTZ:PETAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("EXAHERTZ:PICOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("EXAHERTZ:TERAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("EXAHERTZ:YOCTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 42}});
        c.put("EXAHERTZ:YOTTAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("EXAHERTZ:ZEPTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 39}});
        c.put("EXAHERTZ:ZETTAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("FEMTOHERTZ:ATTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("FEMTOHERTZ:CENTIHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -13}});
        c.put("FEMTOHERTZ:DECIHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -14}});
        c.put("FEMTOHERTZ:DEKAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -16}});
        c.put("FEMTOHERTZ:EXAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("FEMTOHERTZ:GIGAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("FEMTOHERTZ:HECTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -17}});
        c.put("FEMTOHERTZ:HERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("FEMTOHERTZ:KILOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("FEMTOHERTZ:MEGAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("FEMTOHERTZ:MICROHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("FEMTOHERTZ:MILLIHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("FEMTOHERTZ:NANOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("FEMTOHERTZ:PETAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("FEMTOHERTZ:PICOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("FEMTOHERTZ:TERAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("FEMTOHERTZ:YOCTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("FEMTOHERTZ:YOTTAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -39}});
        c.put("FEMTOHERTZ:ZEPTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("FEMTOHERTZ:ZETTAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -36}});
        c.put("GIGAHERTZ:ATTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("GIGAHERTZ:CENTIHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 11}});
        c.put("GIGAHERTZ:DECIHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 10}});
        c.put("GIGAHERTZ:DEKAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 8}});
        c.put("GIGAHERTZ:EXAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("GIGAHERTZ:FEMTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("GIGAHERTZ:HECTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 7}});
        c.put("GIGAHERTZ:HERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("GIGAHERTZ:KILOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("GIGAHERTZ:MEGAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("GIGAHERTZ:MICROHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("GIGAHERTZ:MILLIHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("GIGAHERTZ:NANOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("GIGAHERTZ:PETAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("GIGAHERTZ:PICOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("GIGAHERTZ:TERAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("GIGAHERTZ:YOCTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("GIGAHERTZ:YOTTAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("GIGAHERTZ:ZEPTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("GIGAHERTZ:ZETTAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("HECTOHERTZ:ATTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 20}});
        c.put("HECTOHERTZ:CENTIHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("HECTOHERTZ:DECIHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("HECTOHERTZ:DEKAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("HECTOHERTZ:EXAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -16}});
        c.put("HECTOHERTZ:FEMTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 17}});
        c.put("HECTOHERTZ:GIGAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -7}});
        c.put("HECTOHERTZ:HERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("HECTOHERTZ:KILOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("HECTOHERTZ:MEGAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("HECTOHERTZ:MICROHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 8}});
        c.put("HECTOHERTZ:MILLIHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 5}});
        c.put("HECTOHERTZ:NANOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 11}});
        c.put("HECTOHERTZ:PETAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -13}});
        c.put("HECTOHERTZ:PICOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 14}});
        c.put("HECTOHERTZ:TERAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -10}});
        c.put("HECTOHERTZ:YOCTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 26}});
        c.put("HECTOHERTZ:YOTTAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -22}});
        c.put("HECTOHERTZ:ZEPTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 23}});
        c.put("HECTOHERTZ:ZETTAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -19}});
        c.put("HERTZ:ATTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("HERTZ:CENTIHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("HERTZ:DECIHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("HERTZ:DEKAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("HERTZ:EXAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("HERTZ:FEMTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("HERTZ:GIGAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("HERTZ:HECTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("HERTZ:KILOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("HERTZ:MEGAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("HERTZ:MICROHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("HERTZ:MILLIHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("HERTZ:NANOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("HERTZ:PETAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("HERTZ:PICOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("HERTZ:TERAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("HERTZ:YOCTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("HERTZ:YOTTAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("HERTZ:ZEPTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("HERTZ:ZETTAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("KILOHERTZ:ATTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("KILOHERTZ:CENTIHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 5}});
        c.put("KILOHERTZ:DECIHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("KILOHERTZ:DEKAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("KILOHERTZ:EXAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("KILOHERTZ:FEMTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("KILOHERTZ:GIGAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("KILOHERTZ:HECTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("KILOHERTZ:HERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("KILOHERTZ:MEGAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("KILOHERTZ:MICROHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("KILOHERTZ:MILLIHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("KILOHERTZ:NANOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("KILOHERTZ:PETAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("KILOHERTZ:PICOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("KILOHERTZ:TERAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("KILOHERTZ:YOCTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("KILOHERTZ:YOTTAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("KILOHERTZ:ZEPTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("KILOHERTZ:ZETTAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("MEGAHERTZ:ATTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("MEGAHERTZ:CENTIHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 8}});
        c.put("MEGAHERTZ:DECIHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 7}});
        c.put("MEGAHERTZ:DEKAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 5}});
        c.put("MEGAHERTZ:EXAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("MEGAHERTZ:FEMTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("MEGAHERTZ:GIGAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("MEGAHERTZ:HECTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("MEGAHERTZ:HERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("MEGAHERTZ:KILOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("MEGAHERTZ:MICROHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("MEGAHERTZ:MILLIHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("MEGAHERTZ:NANOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("MEGAHERTZ:PETAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("MEGAHERTZ:PICOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("MEGAHERTZ:TERAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("MEGAHERTZ:YOCTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("MEGAHERTZ:YOTTAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("MEGAHERTZ:ZEPTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("MEGAHERTZ:ZETTAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("MICROHERTZ:ATTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("MICROHERTZ:CENTIHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("MICROHERTZ:DECIHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -5}});
        c.put("MICROHERTZ:DEKAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -7}});
        c.put("MICROHERTZ:EXAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("MICROHERTZ:FEMTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("MICROHERTZ:GIGAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("MICROHERTZ:HECTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -8}});
        c.put("MICROHERTZ:HERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("MICROHERTZ:KILOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("MICROHERTZ:MEGAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("MICROHERTZ:MILLIHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("MICROHERTZ:NANOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("MICROHERTZ:PETAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("MICROHERTZ:PICOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("MICROHERTZ:TERAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("MICROHERTZ:YOCTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("MICROHERTZ:YOTTAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("MICROHERTZ:ZEPTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("MICROHERTZ:ZETTAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("MILLIHERTZ:ATTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("MILLIHERTZ:CENTIHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("MILLIHERTZ:DECIHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("MILLIHERTZ:DEKAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("MILLIHERTZ:EXAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("MILLIHERTZ:FEMTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("MILLIHERTZ:GIGAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("MILLIHERTZ:HECTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -5}});
        c.put("MILLIHERTZ:HERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("MILLIHERTZ:KILOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("MILLIHERTZ:MEGAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("MILLIHERTZ:MICROHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("MILLIHERTZ:NANOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("MILLIHERTZ:PETAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("MILLIHERTZ:PICOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("MILLIHERTZ:TERAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("MILLIHERTZ:YOCTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("MILLIHERTZ:YOTTAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("MILLIHERTZ:ZEPTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("MILLIHERTZ:ZETTAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("NANOHERTZ:ATTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("NANOHERTZ:CENTIHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -7}});
        c.put("NANOHERTZ:DECIHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -8}});
        c.put("NANOHERTZ:DEKAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -10}});
        c.put("NANOHERTZ:EXAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("NANOHERTZ:FEMTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("NANOHERTZ:GIGAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("NANOHERTZ:HECTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -11}});
        c.put("NANOHERTZ:HERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("NANOHERTZ:KILOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("NANOHERTZ:MEGAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("NANOHERTZ:MICROHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("NANOHERTZ:MILLIHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("NANOHERTZ:PETAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("NANOHERTZ:PICOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("NANOHERTZ:TERAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("NANOHERTZ:YOCTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("NANOHERTZ:YOTTAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("NANOHERTZ:ZEPTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("NANOHERTZ:ZETTAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("PETAHERTZ:ATTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("PETAHERTZ:CENTIHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 17}});
        c.put("PETAHERTZ:DECIHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 16}});
        c.put("PETAHERTZ:DEKAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 14}});
        c.put("PETAHERTZ:EXAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("PETAHERTZ:FEMTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("PETAHERTZ:GIGAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("PETAHERTZ:HECTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 13}});
        c.put("PETAHERTZ:HERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("PETAHERTZ:KILOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("PETAHERTZ:MEGAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("PETAHERTZ:MICROHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("PETAHERTZ:MILLIHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("PETAHERTZ:NANOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("PETAHERTZ:PICOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("PETAHERTZ:TERAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("PETAHERTZ:YOCTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 39}});
        c.put("PETAHERTZ:YOTTAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("PETAHERTZ:ZEPTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 36}});
        c.put("PETAHERTZ:ZETTAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("PICOHERTZ:ATTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("PICOHERTZ:CENTIHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -10}});
        c.put("PICOHERTZ:DECIHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -11}});
        c.put("PICOHERTZ:DEKAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -13}});
        c.put("PICOHERTZ:EXAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("PICOHERTZ:FEMTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("PICOHERTZ:GIGAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("PICOHERTZ:HECTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -14}});
        c.put("PICOHERTZ:HERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("PICOHERTZ:KILOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("PICOHERTZ:MEGAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("PICOHERTZ:MICROHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("PICOHERTZ:MILLIHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("PICOHERTZ:NANOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("PICOHERTZ:PETAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("PICOHERTZ:TERAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("PICOHERTZ:YOCTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("PICOHERTZ:YOTTAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -36}});
        c.put("PICOHERTZ:ZEPTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("PICOHERTZ:ZETTAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("TERAHERTZ:ATTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("TERAHERTZ:CENTIHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 14}});
        c.put("TERAHERTZ:DECIHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 13}});
        c.put("TERAHERTZ:DEKAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 11}});
        c.put("TERAHERTZ:EXAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("TERAHERTZ:FEMTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("TERAHERTZ:GIGAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("TERAHERTZ:HECTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 10}});
        c.put("TERAHERTZ:HERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("TERAHERTZ:KILOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("TERAHERTZ:MEGAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("TERAHERTZ:MICROHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("TERAHERTZ:MILLIHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("TERAHERTZ:NANOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("TERAHERTZ:PETAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("TERAHERTZ:PICOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("TERAHERTZ:YOCTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 36}});
        c.put("TERAHERTZ:YOTTAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("TERAHERTZ:ZEPTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("TERAHERTZ:ZETTAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("YOCTOHERTZ:ATTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("YOCTOHERTZ:CENTIHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -22}});
        c.put("YOCTOHERTZ:DECIHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -23}});
        c.put("YOCTOHERTZ:DEKAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -25}});
        c.put("YOCTOHERTZ:EXAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -42}});
        c.put("YOCTOHERTZ:FEMTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("YOCTOHERTZ:GIGAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("YOCTOHERTZ:HECTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -26}});
        c.put("YOCTOHERTZ:HERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("YOCTOHERTZ:KILOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("YOCTOHERTZ:MEGAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("YOCTOHERTZ:MICROHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("YOCTOHERTZ:MILLIHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("YOCTOHERTZ:NANOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("YOCTOHERTZ:PETAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -39}});
        c.put("YOCTOHERTZ:PICOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("YOCTOHERTZ:TERAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -36}});
        c.put("YOCTOHERTZ:YOTTAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -48}});
        c.put("YOCTOHERTZ:ZEPTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("YOCTOHERTZ:ZETTAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -45}});
        c.put("YOTTAHERTZ:ATTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 42}});
        c.put("YOTTAHERTZ:CENTIHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 26}});
        c.put("YOTTAHERTZ:DECIHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 25}});
        c.put("YOTTAHERTZ:DEKAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 23}});
        c.put("YOTTAHERTZ:EXAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("YOTTAHERTZ:FEMTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 39}});
        c.put("YOTTAHERTZ:GIGAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("YOTTAHERTZ:HECTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 22}});
        c.put("YOTTAHERTZ:HERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("YOTTAHERTZ:KILOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("YOTTAHERTZ:MEGAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("YOTTAHERTZ:MICROHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("YOTTAHERTZ:MILLIHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("YOTTAHERTZ:NANOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("YOTTAHERTZ:PETAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("YOTTAHERTZ:PICOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 36}});
        c.put("YOTTAHERTZ:TERAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("YOTTAHERTZ:YOCTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 48}});
        c.put("YOTTAHERTZ:ZEPTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 45}});
        c.put("YOTTAHERTZ:ZETTAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("ZEPTOHERTZ:ATTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("ZEPTOHERTZ:CENTIHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -19}});
        c.put("ZEPTOHERTZ:DECIHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -20}});
        c.put("ZEPTOHERTZ:DEKAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -22}});
        c.put("ZEPTOHERTZ:EXAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -39}});
        c.put("ZEPTOHERTZ:FEMTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("ZEPTOHERTZ:GIGAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("ZEPTOHERTZ:HECTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -23}});
        c.put("ZEPTOHERTZ:HERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("ZEPTOHERTZ:KILOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("ZEPTOHERTZ:MEGAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("ZEPTOHERTZ:MICROHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("ZEPTOHERTZ:MILLIHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("ZEPTOHERTZ:NANOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("ZEPTOHERTZ:PETAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -36}});
        c.put("ZEPTOHERTZ:PICOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("ZEPTOHERTZ:TERAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("ZEPTOHERTZ:YOCTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("ZEPTOHERTZ:YOTTAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -45}});
        c.put("ZEPTOHERTZ:ZETTAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -42}});
        c.put("ZETTAHERTZ:ATTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 39}});
        c.put("ZETTAHERTZ:CENTIHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 23}});
        c.put("ZETTAHERTZ:DECIHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 22}});
        c.put("ZETTAHERTZ:DEKAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 20}});
        c.put("ZETTAHERTZ:EXAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("ZETTAHERTZ:FEMTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 36}});
        c.put("ZETTAHERTZ:GIGAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("ZETTAHERTZ:HECTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 19}});
        c.put("ZETTAHERTZ:HERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("ZETTAHERTZ:KILOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("ZETTAHERTZ:MEGAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("ZETTAHERTZ:MICROHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("ZETTAHERTZ:MILLIHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("ZETTAHERTZ:NANOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("ZETTAHERTZ:PETAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("ZETTAHERTZ:PICOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("ZETTAHERTZ:TERAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("ZETTAHERTZ:YOCTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 45}});
        c.put("ZETTAHERTZ:YOTTAHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("ZETTAHERTZ:ZEPTOHERTZ", new double[][]{new double[]{0, 1}, new double[]{10, 42}});
        conversions = Collections.unmodifiableMap(c);
    }

    private static final Map<UnitsFrequency, String> SYMBOLS;
    static {
        Map<UnitsFrequency, String> s = new HashMap<UnitsFrequency, String>();
        s.put(UnitsFrequency.ATTOHERTZ, "aHz");
        s.put(UnitsFrequency.CENTIHERTZ, "cHz");
        s.put(UnitsFrequency.DECIHERTZ, "dHz");
        s.put(UnitsFrequency.DEKAHERTZ, "daHz");
        s.put(UnitsFrequency.EXAHERTZ, "EHz");
        s.put(UnitsFrequency.FEMTOHERTZ, "fHz");
        s.put(UnitsFrequency.GIGAHERTZ, "GHz");
        s.put(UnitsFrequency.HECTOHERTZ, "hHz");
        s.put(UnitsFrequency.HERTZ, "Hz");
        s.put(UnitsFrequency.KILOHERTZ, "kHz");
        s.put(UnitsFrequency.MEGAHERTZ, "MHz");
        s.put(UnitsFrequency.MICROHERTZ, "µHz");
        s.put(UnitsFrequency.MILLIHERTZ, "mHz");
        s.put(UnitsFrequency.NANOHERTZ, "nHz");
        s.put(UnitsFrequency.PETAHERTZ, "PHz");
        s.put(UnitsFrequency.PICOHERTZ, "pHz");
        s.put(UnitsFrequency.TERAHERTZ, "THz");
        s.put(UnitsFrequency.YOCTOHERTZ, "yHz");
        s.put(UnitsFrequency.YOTTAHERTZ, "YHz");
        s.put(UnitsFrequency.ZEPTOHERTZ, "zHz");
        s.put(UnitsFrequency.ZETTAHERTZ, "ZHz");
        SYMBOLS = s;
    }

    public static String lookupSymbol(UnitsFrequency unit) {
        return SYMBOLS.get(unit);
    }

    public static final Ice.ObjectFactory makeFactory(final omero.client client) {

        return new Ice.ObjectFactory() {

            public Ice.Object create(String arg0) {
                return new FrequencyI();
            }

            public void destroy() {
                // no-op
            }

        };
    };

    //
    // CONVERSIONS
    //

    public static ome.xml.model.enums.UnitsFrequency makeXMLUnit(String unit) {
        try {
            return ome.xml.model.enums.UnitsFrequency
                    .fromString((String) unit);
        } catch (EnumerationException e) {
            throw new RuntimeException("Bad Frequency unit: " + unit, e);
        }
    }

    public static ome.units.quantity.Frequency makeXMLQuantity(double d, String unit) {
        ome.units.unit.Unit<ome.units.quantity.Frequency> units =
                ome.xml.model.enums.handlers.UnitsFrequencyEnumHandler
                        .getBaseUnit(makeXMLUnit(unit));
        return new ome.units.quantity.Frequency(d, units);
    }

   /**
    * FIXME: this should likely take a default so that locations which don't
    * want an exception can have
    *
    * log.warn("Using new PositiveFloat(1.0)!", e); return new
    * PositiveFloat(1.0);
    *
    * or similar.
    */
   public static ome.units.quantity.Frequency convert(Frequency t) {
       if (t == null) {
           return null;
       }

       Double v = t.getValue();
       // Use the code/symbol-mapping in the ome.model.enums files
       // to convert to the specification value.
       String u = ome.model.enums.UnitsFrequency.valueOf(
               t.getUnit().toString()).getSymbol();
       ome.xml.model.enums.UnitsFrequency units = makeXMLUnit(u);
       ome.units.unit.Unit<ome.units.quantity.Frequency> units2 =
               ome.xml.model.enums.handlers.UnitsFrequencyEnumHandler
                       .getBaseUnit(units);

       return new ome.units.quantity.Frequency(v, units2);
   }


    //
    // REGULAR ICE CLASS
    //

    public final static Ice.ObjectFactory Factory = makeFactory(null);

    public FrequencyI() {
        super();
    }

    public FrequencyI(double d, UnitsFrequency unit) {
        super();
        this.setUnit(unit);
        this.setValue(d);
    }

    public FrequencyI(double d,
            Unit<ome.units.quantity.Frequency> unit) {
        this(d, ome.model.enums.UnitsFrequency.bySymbol(unit.getSymbol()));
    }

   /**
    * Copy constructor that converts the given {@link omero.model.Frequency}
    * based on the given ome-xml enum
    */
   public FrequencyI(Frequency value, Unit<ome.units.quantity.Frequency> ul) {
       this(value,
            ome.model.enums.UnitsFrequency.bySymbol(ul.getSymbol()).toString());
   }

   /**
    * Copy constructor that converts the given {@link omero.model.Frequency}
    * based on the given ome.model enum
    */
   public FrequencyI(double d, ome.model.enums.UnitsFrequency ul) {
        this(d, UnitsFrequency.valueOf(ul.toString()));
    }

   /**
    * Copy constructor that converts the given {@link omero.model.Frequency}
    * based on the given enum string.
    *
    * @param target String representation of the CODE enum
    */
    public FrequencyI(Frequency value, String target) {
       String source = value.getUnit().toString();
       if (target.equals(source)) {
           setValue(value.getValue());
           setUnit(value.getUnit());
        } else {
            double[][] coeffs = conversions.get(source + ":" + target);
            if (coeffs == null) {
                throw new RuntimeException(String.format(
                    "%f %s cannot be converted to %s",
                        value.getValue(), value.getUnit(), target));
            }
            double orig = value.getValue();
            double k, p, v;
            if (coeffs.length == 0) {
                v = orig;
            } else if (coeffs.length == 2){
                k = coeffs[0][0];
                p = coeffs[0][1];
                v = Math.pow(k, p);

                k = coeffs[1][0];
                p = coeffs[1][1];
                v += Math.pow(k, p) * orig;
            } else {
                throw new RuntimeException("coefficients of unknown length: " +  coeffs.length);
            }

            setValue(v);
            setUnit(UnitsFrequency.valueOf(target));
       }
    }

   /**
    * Copy constructor that converts between units if possible.
    *
    * @param target unit that is desired. non-null.
    */
    public FrequencyI(Frequency value, UnitsFrequency target) {
        this(value, target.toString());
    }

    /**
     * Convert a Bio-Formats {@link Length} to an OMERO Length.
     */
    public FrequencyI(ome.units.quantity.Frequency value) {
        ome.model.enums.UnitsFrequency internal =
            ome.model.enums.UnitsFrequency.bySymbol(value.unit().getSymbol());
        UnitsFrequency ul = UnitsFrequency.valueOf(internal.toString());
        setValue(value.value().doubleValue());
        setUnit(ul);
    }

    public double getValue(Ice.Current current) {
        return this.value;
    }

    public void setValue(double value , Ice.Current current) {
        this.value = value;
    }

    public UnitsFrequency getUnit(Ice.Current current) {
        return this.unit;
    }

    public void setUnit(UnitsFrequency unit, Ice.Current current) {
        this.unit = unit;
    }

    public String getSymbol(Ice.Current current) {
        return SYMBOLS.get(this.unit);
    }

    public Frequency copy(Ice.Current ignore) {
        FrequencyI copy = new FrequencyI();
        copy.setValue(getValue());
        copy.setUnit(getUnit());
        return copy;
    }

    @Override
    public void copyObject(Filterable model, ModelMapper mapper) {
        if (model instanceof ome.model.units.Frequency) {
            ome.model.units.Frequency t = (ome.model.units.Frequency) model;
            this.value = t.getValue();
            this.unit = UnitsFrequency.valueOf(t.getUnit().toString());
        } else {
            throw new IllegalArgumentException(
              "Frequency cannot copy from " +
              (model==null ? "null" : model.getClass().getName()));
        }
    }

    @Override
    public Filterable fillObject(ReverseModelMapper mapper) {
        ome.model.enums.UnitsFrequency ut = ome.model.enums.UnitsFrequency.valueOf(getUnit().toString());
        ome.model.units.Frequency t = new ome.model.units.Frequency(getValue(), ut);
        return t;
    }

    // ~ Java overrides
    // =========================================================================

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((unit == null) ? 0 : unit.hashCode());
        long temp;
        temp = Double.doubleToLongBits(value);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "Frequency(" + value + " " + unit + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Frequency other = (Frequency) obj;
        if (unit != other.unit)
            return false;
        if (Double.doubleToLongBits(value) != Double
                .doubleToLongBits(other.value))
            return false;
        return true;
    }

}

