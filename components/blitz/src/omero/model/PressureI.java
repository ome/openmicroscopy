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

import omero.model.enums.UnitsPressure;

/**
 * Blitz wrapper around the {@link ome.model.units.Pressure} class.
 * Like {@link Details} and {@link Permissions}, this object
 * is embedded into other objects and does not have a full life
 * cycle of its own.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 */
public class PressureI extends Pressure implements ModelBased {

    private static final long serialVersionUID = 1L;

    private static final Map<String, double[][]> conversions;
    static {
        Map<String, double[][]> c = new HashMap<String, double[][]>();

        c.put("ATTOPASCAL:CENTIPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -16}});
        c.put("ATTOPASCAL:DECAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -19}});
        c.put("ATTOPASCAL:DECIPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -17}});
        c.put("ATTOPASCAL:EXAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -36}});
        c.put("ATTOPASCAL:FEMTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("ATTOPASCAL:GIGAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("ATTOPASCAL:HECTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -20}});
        c.put("ATTOPASCAL:KILOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("ATTOPASCAL:MEGAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("ATTOPASCAL:MICROPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("ATTOPASCAL:MILLIPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("ATTOPASCAL:NANOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("ATTOPASCAL:PETAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("ATTOPASCAL:PICOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("ATTOPASCAL:Pascal", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("ATTOPASCAL:TERAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("ATTOPASCAL:YOCTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("ATTOPASCAL:YOTTAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -42}});
        c.put("ATTOPASCAL:ZEPTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("ATTOPASCAL:ZETTAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -39}});
        c.put("CENTIPASCAL:ATTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 16}});
        c.put("CENTIPASCAL:DECAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("CENTIPASCAL:DECIPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("CENTIPASCAL:EXAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -20}});
        c.put("CENTIPASCAL:FEMTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 13}});
        c.put("CENTIPASCAL:GIGAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -11}});
        c.put("CENTIPASCAL:HECTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("CENTIPASCAL:KILOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -5}});
        c.put("CENTIPASCAL:MEGAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -8}});
        c.put("CENTIPASCAL:MICROPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("CENTIPASCAL:MILLIPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("CENTIPASCAL:NANOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 7}});
        c.put("CENTIPASCAL:PETAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -17}});
        c.put("CENTIPASCAL:PICOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 10}});
        c.put("CENTIPASCAL:Pascal", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("CENTIPASCAL:TERAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -14}});
        c.put("CENTIPASCAL:YOCTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 22}});
        c.put("CENTIPASCAL:YOTTAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -26}});
        c.put("CENTIPASCAL:ZEPTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 19}});
        c.put("CENTIPASCAL:ZETTAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -23}});
        c.put("DECAPASCAL:ATTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 19}});
        c.put("DECAPASCAL:CENTIPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("DECAPASCAL:DECIPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("DECAPASCAL:EXAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -17}});
        c.put("DECAPASCAL:FEMTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 16}});
        c.put("DECAPASCAL:GIGAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -8}});
        c.put("DECAPASCAL:HECTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("DECAPASCAL:KILOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("DECAPASCAL:MEGAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -5}});
        c.put("DECAPASCAL:MICROPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 7}});
        c.put("DECAPASCAL:MILLIPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("DECAPASCAL:NANOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 10}});
        c.put("DECAPASCAL:PETAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -14}});
        c.put("DECAPASCAL:PICOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 13}});
        c.put("DECAPASCAL:Pascal", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("DECAPASCAL:TERAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -11}});
        c.put("DECAPASCAL:YOCTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 25}});
        c.put("DECAPASCAL:YOTTAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -23}});
        c.put("DECAPASCAL:ZEPTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 22}});
        c.put("DECAPASCAL:ZETTAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -20}});
        c.put("DECIPASCAL:ATTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 17}});
        c.put("DECIPASCAL:CENTIPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("DECIPASCAL:DECAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("DECIPASCAL:EXAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -19}});
        c.put("DECIPASCAL:FEMTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 14}});
        c.put("DECIPASCAL:GIGAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -10}});
        c.put("DECIPASCAL:HECTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("DECIPASCAL:KILOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("DECIPASCAL:MEGAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -7}});
        c.put("DECIPASCAL:MICROPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 5}});
        c.put("DECIPASCAL:MILLIPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("DECIPASCAL:NANOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 8}});
        c.put("DECIPASCAL:PETAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -16}});
        c.put("DECIPASCAL:PICOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 11}});
        c.put("DECIPASCAL:Pascal", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("DECIPASCAL:TERAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -13}});
        c.put("DECIPASCAL:YOCTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 23}});
        c.put("DECIPASCAL:YOTTAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -25}});
        c.put("DECIPASCAL:ZEPTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 20}});
        c.put("DECIPASCAL:ZETTAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -22}});
        c.put("EXAPASCAL:ATTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 36}});
        c.put("EXAPASCAL:CENTIPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 20}});
        c.put("EXAPASCAL:DECAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 17}});
        c.put("EXAPASCAL:DECIPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 19}});
        c.put("EXAPASCAL:FEMTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("EXAPASCAL:GIGAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("EXAPASCAL:HECTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 16}});
        c.put("EXAPASCAL:KILOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("EXAPASCAL:MEGAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("EXAPASCAL:MICROPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("EXAPASCAL:MILLIPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("EXAPASCAL:NANOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("EXAPASCAL:PETAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("EXAPASCAL:PICOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("EXAPASCAL:Pascal", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("EXAPASCAL:TERAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("EXAPASCAL:YOCTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 42}});
        c.put("EXAPASCAL:YOTTAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("EXAPASCAL:ZEPTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 39}});
        c.put("EXAPASCAL:ZETTAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("FEMTOPASCAL:ATTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("FEMTOPASCAL:CENTIPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -13}});
        c.put("FEMTOPASCAL:DECAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -16}});
        c.put("FEMTOPASCAL:DECIPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -14}});
        c.put("FEMTOPASCAL:EXAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("FEMTOPASCAL:GIGAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("FEMTOPASCAL:HECTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -17}});
        c.put("FEMTOPASCAL:KILOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("FEMTOPASCAL:MEGAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("FEMTOPASCAL:MICROPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("FEMTOPASCAL:MILLIPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("FEMTOPASCAL:NANOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("FEMTOPASCAL:PETAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("FEMTOPASCAL:PICOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("FEMTOPASCAL:Pascal", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("FEMTOPASCAL:TERAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("FEMTOPASCAL:YOCTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("FEMTOPASCAL:YOTTAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -39}});
        c.put("FEMTOPASCAL:ZEPTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("FEMTOPASCAL:ZETTAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -36}});
        c.put("GIGAPASCAL:ATTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("GIGAPASCAL:CENTIPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 11}});
        c.put("GIGAPASCAL:DECAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 8}});
        c.put("GIGAPASCAL:DECIPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 10}});
        c.put("GIGAPASCAL:EXAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("GIGAPASCAL:FEMTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("GIGAPASCAL:HECTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 7}});
        c.put("GIGAPASCAL:KILOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("GIGAPASCAL:MEGAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("GIGAPASCAL:MICROPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("GIGAPASCAL:MILLIPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("GIGAPASCAL:NANOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("GIGAPASCAL:PETAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("GIGAPASCAL:PICOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("GIGAPASCAL:Pascal", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("GIGAPASCAL:TERAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("GIGAPASCAL:YOCTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("GIGAPASCAL:YOTTAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("GIGAPASCAL:ZEPTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("GIGAPASCAL:ZETTAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("HECTOPASCAL:ATTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 20}});
        c.put("HECTOPASCAL:CENTIPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("HECTOPASCAL:DECAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("HECTOPASCAL:DECIPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("HECTOPASCAL:EXAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -16}});
        c.put("HECTOPASCAL:FEMTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 17}});
        c.put("HECTOPASCAL:GIGAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -7}});
        c.put("HECTOPASCAL:KILOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("HECTOPASCAL:MEGAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("HECTOPASCAL:MICROPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 8}});
        c.put("HECTOPASCAL:MILLIPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 5}});
        c.put("HECTOPASCAL:NANOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 11}});
        c.put("HECTOPASCAL:PETAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -13}});
        c.put("HECTOPASCAL:PICOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 14}});
        c.put("HECTOPASCAL:Pascal", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("HECTOPASCAL:TERAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -10}});
        c.put("HECTOPASCAL:YOCTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 26}});
        c.put("HECTOPASCAL:YOTTAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -22}});
        c.put("HECTOPASCAL:ZEPTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 23}});
        c.put("HECTOPASCAL:ZETTAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -19}});
        c.put("KILOPASCAL:ATTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("KILOPASCAL:CENTIPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 5}});
        c.put("KILOPASCAL:DECAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("KILOPASCAL:DECIPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("KILOPASCAL:EXAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("KILOPASCAL:FEMTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("KILOPASCAL:GIGAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("KILOPASCAL:HECTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("KILOPASCAL:MEGAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("KILOPASCAL:MICROPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("KILOPASCAL:MILLIPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("KILOPASCAL:NANOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("KILOPASCAL:PETAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("KILOPASCAL:PICOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("KILOPASCAL:Pascal", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("KILOPASCAL:TERAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("KILOPASCAL:YOCTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("KILOPASCAL:YOTTAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("KILOPASCAL:ZEPTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("KILOPASCAL:ZETTAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("MEGAPASCAL:ATTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("MEGAPASCAL:CENTIPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 8}});
        c.put("MEGAPASCAL:DECAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 5}});
        c.put("MEGAPASCAL:DECIPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 7}});
        c.put("MEGAPASCAL:EXAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("MEGAPASCAL:FEMTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("MEGAPASCAL:GIGAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("MEGAPASCAL:HECTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("MEGAPASCAL:KILOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("MEGAPASCAL:MICROPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("MEGAPASCAL:MILLIPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("MEGAPASCAL:NANOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("MEGAPASCAL:PETAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("MEGAPASCAL:PICOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("MEGAPASCAL:Pascal", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("MEGAPASCAL:TERAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("MEGAPASCAL:YOCTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("MEGAPASCAL:YOTTAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("MEGAPASCAL:ZEPTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("MEGAPASCAL:ZETTAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("MICROPASCAL:ATTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("MICROPASCAL:CENTIPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("MICROPASCAL:DECAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -7}});
        c.put("MICROPASCAL:DECIPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -5}});
        c.put("MICROPASCAL:EXAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("MICROPASCAL:FEMTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("MICROPASCAL:GIGAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("MICROPASCAL:HECTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -8}});
        c.put("MICROPASCAL:KILOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("MICROPASCAL:MEGAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("MICROPASCAL:MILLIPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("MICROPASCAL:NANOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("MICROPASCAL:PETAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("MICROPASCAL:PICOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("MICROPASCAL:Pascal", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("MICROPASCAL:TERAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("MICROPASCAL:YOCTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("MICROPASCAL:YOTTAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("MICROPASCAL:ZEPTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("MICROPASCAL:ZETTAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("MILLIPASCAL:ATTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("MILLIPASCAL:CENTIPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("MILLIPASCAL:DECAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("MILLIPASCAL:DECIPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("MILLIPASCAL:EXAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("MILLIPASCAL:FEMTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("MILLIPASCAL:GIGAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("MILLIPASCAL:HECTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -5}});
        c.put("MILLIPASCAL:KILOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("MILLIPASCAL:MEGAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("MILLIPASCAL:MICROPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("MILLIPASCAL:NANOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("MILLIPASCAL:PETAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("MILLIPASCAL:PICOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("MILLIPASCAL:Pascal", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("MILLIPASCAL:TERAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("MILLIPASCAL:YOCTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("MILLIPASCAL:YOTTAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("MILLIPASCAL:ZEPTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("MILLIPASCAL:ZETTAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("NANOPASCAL:ATTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("NANOPASCAL:CENTIPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -7}});
        c.put("NANOPASCAL:DECAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -10}});
        c.put("NANOPASCAL:DECIPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -8}});
        c.put("NANOPASCAL:EXAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("NANOPASCAL:FEMTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("NANOPASCAL:GIGAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("NANOPASCAL:HECTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -11}});
        c.put("NANOPASCAL:KILOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("NANOPASCAL:MEGAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("NANOPASCAL:MICROPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("NANOPASCAL:MILLIPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("NANOPASCAL:PETAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("NANOPASCAL:PICOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("NANOPASCAL:Pascal", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("NANOPASCAL:TERAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("NANOPASCAL:YOCTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("NANOPASCAL:YOTTAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("NANOPASCAL:ZEPTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("NANOPASCAL:ZETTAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("PETAPASCAL:ATTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("PETAPASCAL:CENTIPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 17}});
        c.put("PETAPASCAL:DECAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 14}});
        c.put("PETAPASCAL:DECIPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 16}});
        c.put("PETAPASCAL:EXAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("PETAPASCAL:FEMTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("PETAPASCAL:GIGAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("PETAPASCAL:HECTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 13}});
        c.put("PETAPASCAL:KILOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("PETAPASCAL:MEGAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("PETAPASCAL:MICROPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("PETAPASCAL:MILLIPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("PETAPASCAL:NANOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("PETAPASCAL:PICOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("PETAPASCAL:Pascal", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("PETAPASCAL:TERAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("PETAPASCAL:YOCTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 39}});
        c.put("PETAPASCAL:YOTTAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("PETAPASCAL:ZEPTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 36}});
        c.put("PETAPASCAL:ZETTAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("PICOPASCAL:ATTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("PICOPASCAL:CENTIPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -10}});
        c.put("PICOPASCAL:DECAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -13}});
        c.put("PICOPASCAL:DECIPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -11}});
        c.put("PICOPASCAL:EXAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("PICOPASCAL:FEMTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("PICOPASCAL:GIGAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("PICOPASCAL:HECTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -14}});
        c.put("PICOPASCAL:KILOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("PICOPASCAL:MEGAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("PICOPASCAL:MICROPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("PICOPASCAL:MILLIPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("PICOPASCAL:NANOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("PICOPASCAL:PETAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("PICOPASCAL:Pascal", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("PICOPASCAL:TERAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("PICOPASCAL:YOCTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("PICOPASCAL:YOTTAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -36}});
        c.put("PICOPASCAL:ZEPTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("PICOPASCAL:ZETTAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("Pascal:ATTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("Pascal:CENTIPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("Pascal:DECAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("Pascal:DECIPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("Pascal:EXAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("Pascal:FEMTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("Pascal:GIGAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("Pascal:HECTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("Pascal:KILOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("Pascal:MEGAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("Pascal:MICROPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("Pascal:MILLIPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("Pascal:NANOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("Pascal:PETAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("Pascal:PICOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("Pascal:TERAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("Pascal:YOCTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("Pascal:YOTTAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("Pascal:ZEPTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("Pascal:ZETTAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("TERAPASCAL:ATTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("TERAPASCAL:CENTIPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 14}});
        c.put("TERAPASCAL:DECAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 11}});
        c.put("TERAPASCAL:DECIPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 13}});
        c.put("TERAPASCAL:EXAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("TERAPASCAL:FEMTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("TERAPASCAL:GIGAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("TERAPASCAL:HECTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 10}});
        c.put("TERAPASCAL:KILOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("TERAPASCAL:MEGAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("TERAPASCAL:MICROPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("TERAPASCAL:MILLIPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("TERAPASCAL:NANOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("TERAPASCAL:PETAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("TERAPASCAL:PICOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("TERAPASCAL:Pascal", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("TERAPASCAL:YOCTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 36}});
        c.put("TERAPASCAL:YOTTAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("TERAPASCAL:ZEPTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("TERAPASCAL:ZETTAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("YOCTOPASCAL:ATTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("YOCTOPASCAL:CENTIPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -22}});
        c.put("YOCTOPASCAL:DECAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -25}});
        c.put("YOCTOPASCAL:DECIPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -23}});
        c.put("YOCTOPASCAL:EXAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -42}});
        c.put("YOCTOPASCAL:FEMTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("YOCTOPASCAL:GIGAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("YOCTOPASCAL:HECTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -26}});
        c.put("YOCTOPASCAL:KILOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("YOCTOPASCAL:MEGAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("YOCTOPASCAL:MICROPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("YOCTOPASCAL:MILLIPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("YOCTOPASCAL:NANOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("YOCTOPASCAL:PETAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -39}});
        c.put("YOCTOPASCAL:PICOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("YOCTOPASCAL:Pascal", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("YOCTOPASCAL:TERAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -36}});
        c.put("YOCTOPASCAL:YOTTAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -48}});
        c.put("YOCTOPASCAL:ZEPTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("YOCTOPASCAL:ZETTAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -45}});
        c.put("YOTTAPASCAL:ATTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 42}});
        c.put("YOTTAPASCAL:CENTIPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 26}});
        c.put("YOTTAPASCAL:DECAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 23}});
        c.put("YOTTAPASCAL:DECIPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 25}});
        c.put("YOTTAPASCAL:EXAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("YOTTAPASCAL:FEMTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 39}});
        c.put("YOTTAPASCAL:GIGAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("YOTTAPASCAL:HECTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 22}});
        c.put("YOTTAPASCAL:KILOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("YOTTAPASCAL:MEGAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("YOTTAPASCAL:MICROPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("YOTTAPASCAL:MILLIPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("YOTTAPASCAL:NANOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("YOTTAPASCAL:PETAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("YOTTAPASCAL:PICOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 36}});
        c.put("YOTTAPASCAL:Pascal", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("YOTTAPASCAL:TERAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("YOTTAPASCAL:YOCTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 48}});
        c.put("YOTTAPASCAL:ZEPTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 45}});
        c.put("YOTTAPASCAL:ZETTAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("ZEPTOPASCAL:ATTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("ZEPTOPASCAL:CENTIPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -19}});
        c.put("ZEPTOPASCAL:DECAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -22}});
        c.put("ZEPTOPASCAL:DECIPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -20}});
        c.put("ZEPTOPASCAL:EXAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -39}});
        c.put("ZEPTOPASCAL:FEMTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("ZEPTOPASCAL:GIGAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("ZEPTOPASCAL:HECTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -23}});
        c.put("ZEPTOPASCAL:KILOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("ZEPTOPASCAL:MEGAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("ZEPTOPASCAL:MICROPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("ZEPTOPASCAL:MILLIPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("ZEPTOPASCAL:NANOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("ZEPTOPASCAL:PETAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -36}});
        c.put("ZEPTOPASCAL:PICOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("ZEPTOPASCAL:Pascal", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("ZEPTOPASCAL:TERAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("ZEPTOPASCAL:YOCTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("ZEPTOPASCAL:YOTTAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -45}});
        c.put("ZEPTOPASCAL:ZETTAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -42}});
        c.put("ZETTAPASCAL:ATTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 39}});
        c.put("ZETTAPASCAL:CENTIPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 23}});
        c.put("ZETTAPASCAL:DECAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 20}});
        c.put("ZETTAPASCAL:DECIPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 22}});
        c.put("ZETTAPASCAL:EXAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("ZETTAPASCAL:FEMTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 36}});
        c.put("ZETTAPASCAL:GIGAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("ZETTAPASCAL:HECTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 19}});
        c.put("ZETTAPASCAL:KILOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("ZETTAPASCAL:MEGAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("ZETTAPASCAL:MICROPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("ZETTAPASCAL:MILLIPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("ZETTAPASCAL:NANOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("ZETTAPASCAL:PETAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("ZETTAPASCAL:PICOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("ZETTAPASCAL:Pascal", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("ZETTAPASCAL:TERAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("ZETTAPASCAL:YOCTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 45}});
        c.put("ZETTAPASCAL:YOTTAPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("ZETTAPASCAL:ZEPTOPASCAL", new double[][]{new double[]{0, 1}, new double[]{10, 42}});
        conversions = Collections.unmodifiableMap(c);
    }

    private static final Map<UnitsPressure, String> SYMBOLS;
    static {
        Map<UnitsPressure, String> s = new HashMap<UnitsPressure, String>();
        s.put(UnitsPressure.ATMOSPHERE, "atm");
        s.put(UnitsPressure.ATTOPASCAL, "aPa");
        s.put(UnitsPressure.BAR, "bar");
        s.put(UnitsPressure.CENTIBAR, "cbar");
        s.put(UnitsPressure.CENTIPASCAL, "cPa");
        s.put(UnitsPressure.DECAPASCAL, "daPa");
        s.put(UnitsPressure.DECIBAR, "dbar");
        s.put(UnitsPressure.DECIPASCAL, "dPa");
        s.put(UnitsPressure.EXAPASCAL, "EPa");
        s.put(UnitsPressure.FEMTOPASCAL, "fPa");
        s.put(UnitsPressure.GIGAPASCAL, "GPa");
        s.put(UnitsPressure.HECTOPASCAL, "hPa");
        s.put(UnitsPressure.KILOBAR, "kbar");
        s.put(UnitsPressure.KILOPASCAL, "kPa");
        s.put(UnitsPressure.MEGABAR, "Mbar");
        s.put(UnitsPressure.MEGAPASCAL, "MPa");
        s.put(UnitsPressure.MICROPASCAL, "µPa");
        s.put(UnitsPressure.MILLIBAR, "mbar");
        s.put(UnitsPressure.MILLIPASCAL, "mPa");
        s.put(UnitsPressure.MILLITORR, "mTorr");
        s.put(UnitsPressure.MMHG, "mm Hg");
        s.put(UnitsPressure.NANOPASCAL, "nPa");
        s.put(UnitsPressure.PETAPASCAL, "PPa");
        s.put(UnitsPressure.PICOPASCAL, "pPa");
        s.put(UnitsPressure.PSI, "psi");
        s.put(UnitsPressure.Pascal, "Pa");
        s.put(UnitsPressure.TERAPASCAL, "TPa");
        s.put(UnitsPressure.TORR, "Torr");
        s.put(UnitsPressure.YOCTOPASCAL, "yPa");
        s.put(UnitsPressure.YOTTAPASCAL, "YPa");
        s.put(UnitsPressure.ZEPTOPASCAL, "zPa");
        s.put(UnitsPressure.ZETTAPASCAL, "ZPa");
        SYMBOLS = s;
    }

    public static String lookupSymbol(UnitsPressure unit) {
        return SYMBOLS.get(unit);
    }

    public static final Ice.ObjectFactory makeFactory(final omero.client client) {

        return new Ice.ObjectFactory() {

            public Ice.Object create(String arg0) {
                return new PressureI();
            }

            public void destroy() {
                // no-op
            }

        };
    };

    //
    // CONVERSIONS
    //

    public static ome.xml.model.enums.UnitsPressure makeXMLUnit(String unit) {
        try {
            return ome.xml.model.enums.UnitsPressure
                    .fromString((String) unit);
        } catch (EnumerationException e) {
            throw new RuntimeException("Bad Pressure unit: " + unit, e);
        }
    }

    public static ome.units.quantity.Pressure makeXMLQuantity(double d, String unit) {
        ome.units.unit.Unit<ome.units.quantity.Pressure> units =
                ome.xml.model.enums.handlers.UnitsPressureEnumHandler
                        .getBaseUnit(makeXMLUnit(unit));
        return new ome.units.quantity.Pressure(d, units);
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
   public static ome.units.quantity.Pressure convert(Pressure t) {
       if (t == null) {
           return null;
       }

       Double v = t.getValue();
       // Use the code/symbol-mapping in the ome.model.enums files
       // to convert to the specification value.
       String u = ome.model.enums.UnitsPressure.valueOf(
               t.getUnit().toString()).getSymbol();
       ome.xml.model.enums.UnitsPressure units = makeXMLUnit(u);
       ome.units.unit.Unit<ome.units.quantity.Pressure> units2 =
               ome.xml.model.enums.handlers.UnitsPressureEnumHandler
                       .getBaseUnit(units);

       return new ome.units.quantity.Pressure(v, units2);
   }


    //
    // REGULAR ICE CLASS
    //

    public final static Ice.ObjectFactory Factory = makeFactory(null);

    public PressureI() {
        super();
    }

    public PressureI(double d, UnitsPressure unit) {
        super();
        this.setUnit(unit);
        this.setValue(d);
    }

    public PressureI(double d,
            Unit<ome.units.quantity.Pressure> unit) {
        this(d, ome.model.enums.UnitsPressure.bySymbol(unit.getSymbol()));
    }

   /**
    * Copy constructor that converts the given {@link omero.model.Pressure}
    * based on the given ome-xml enum
    */
   public PressureI(Pressure value, Unit<ome.units.quantity.Pressure> ul) {
       this(value,
            ome.model.enums.UnitsPressure.bySymbol(ul.getSymbol()).toString());
   }

   /**
    * Copy constructor that converts the given {@link omero.model.Pressure}
    * based on the given ome.model enum
    */
   public PressureI(double d, ome.model.enums.UnitsPressure ul) {
        this(d, UnitsPressure.valueOf(ul.toString()));
    }

   /**
    * Copy constructor that converts the given {@link omero.model.Pressure}
    * based on the given enum string.
    *
    * @param target String representation of the CODE enum
    */
    public PressureI(Pressure value, String target) {
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
            setUnit(UnitsPressure.valueOf(target));
       }
    }

   /**
    * Copy constructor that converts between units if possible.
    *
    * @param target unit that is desired. non-null.
    */
    public PressureI(Pressure value, UnitsPressure target) {
        this(value, target.toString());
    }

    /**
     * Convert a Bio-Formats {@link Length} to an OMERO Length.
     */
    public PressureI(ome.units.quantity.Pressure value) {
        ome.model.enums.UnitsPressure internal =
            ome.model.enums.UnitsPressure.bySymbol(value.unit().getSymbol());
        UnitsPressure ul = UnitsPressure.valueOf(internal.toString());
        setValue(value.value().doubleValue());
        setUnit(ul);
    }

    public double getValue(Ice.Current current) {
        return this.value;
    }

    public void setValue(double value , Ice.Current current) {
        this.value = value;
    }

    public UnitsPressure getUnit(Ice.Current current) {
        return this.unit;
    }

    public void setUnit(UnitsPressure unit, Ice.Current current) {
        this.unit = unit;
    }

    public String getSymbol(Ice.Current current) {
        return SYMBOLS.get(this.unit);
    }

    public Pressure copy(Ice.Current ignore) {
        PressureI copy = new PressureI();
        copy.setValue(getValue());
        copy.setUnit(getUnit());
        return copy;
    }

    @Override
    public void copyObject(Filterable model, ModelMapper mapper) {
        if (model instanceof ome.model.units.Pressure) {
            ome.model.units.Pressure t = (ome.model.units.Pressure) model;
            this.value = t.getValue();
            this.unit = UnitsPressure.valueOf(t.getUnit().toString());
        } else {
            throw new IllegalArgumentException(
              "Pressure cannot copy from " +
              (model==null ? "null" : model.getClass().getName()));
        }
    }

    @Override
    public Filterable fillObject(ReverseModelMapper mapper) {
        ome.model.enums.UnitsPressure ut = ome.model.enums.UnitsPressure.valueOf(getUnit().toString());
        ome.model.units.Pressure t = new ome.model.units.Pressure(getValue(), ut);
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
        return "Pressure(" + value + " " + unit + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Pressure other = (Pressure) obj;
        if (unit != other.unit)
            return false;
        if (Double.doubleToLongBits(value) != Double
                .doubleToLongBits(other.value))
            return false;
        return true;
    }

}

