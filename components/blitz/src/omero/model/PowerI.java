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

import omero.model.enums.UnitsPower;

/**
 * Blitz wrapper around the {@link ome.model.units.Power} class.
 * Like {@link Details} and {@link Permissions}, this object
 * is embedded into other objects and does not have a full life
 * cycle of its own.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 */
public class PowerI extends Power implements ModelBased {

    private static final long serialVersionUID = 1L;

    private static final Map<String, double[][]> conversions;
    static {
        Map<String, double[][]> c = new HashMap<String, double[][]>();

        c.put("ATTOWATT:CENTIWATT", new double[][]{new double[]{0, 1}, new double[]{10, -16}});
        c.put("ATTOWATT:DECIWATT", new double[][]{new double[]{0, 1}, new double[]{10, -17}});
        c.put("ATTOWATT:DEKAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -19}});
        c.put("ATTOWATT:EXAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -36}});
        c.put("ATTOWATT:FEMTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("ATTOWATT:GIGAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("ATTOWATT:HECTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, -20}});
        c.put("ATTOWATT:KILOWATT", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("ATTOWATT:MEGAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("ATTOWATT:MICROWATT", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("ATTOWATT:MILLIWATT", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("ATTOWATT:NANOWATT", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("ATTOWATT:PETAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("ATTOWATT:PICOWATT", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("ATTOWATT:TERAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("ATTOWATT:WATT", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("ATTOWATT:YOCTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("ATTOWATT:YOTTAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -42}});
        c.put("ATTOWATT:ZEPTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("ATTOWATT:ZETTAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -39}});
        c.put("CENTIWATT:ATTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 16}});
        c.put("CENTIWATT:DECIWATT", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("CENTIWATT:DEKAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("CENTIWATT:EXAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -20}});
        c.put("CENTIWATT:FEMTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 13}});
        c.put("CENTIWATT:GIGAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -11}});
        c.put("CENTIWATT:HECTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("CENTIWATT:KILOWATT", new double[][]{new double[]{0, 1}, new double[]{10, -5}});
        c.put("CENTIWATT:MEGAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -8}});
        c.put("CENTIWATT:MICROWATT", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("CENTIWATT:MILLIWATT", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("CENTIWATT:NANOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 7}});
        c.put("CENTIWATT:PETAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -17}});
        c.put("CENTIWATT:PICOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 10}});
        c.put("CENTIWATT:TERAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -14}});
        c.put("CENTIWATT:WATT", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("CENTIWATT:YOCTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 22}});
        c.put("CENTIWATT:YOTTAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -26}});
        c.put("CENTIWATT:ZEPTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 19}});
        c.put("CENTIWATT:ZETTAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -23}});
        c.put("DECIWATT:ATTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 17}});
        c.put("DECIWATT:CENTIWATT", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("DECIWATT:DEKAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("DECIWATT:EXAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -19}});
        c.put("DECIWATT:FEMTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 14}});
        c.put("DECIWATT:GIGAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -10}});
        c.put("DECIWATT:HECTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("DECIWATT:KILOWATT", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("DECIWATT:MEGAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -7}});
        c.put("DECIWATT:MICROWATT", new double[][]{new double[]{0, 1}, new double[]{10, 5}});
        c.put("DECIWATT:MILLIWATT", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("DECIWATT:NANOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 8}});
        c.put("DECIWATT:PETAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -16}});
        c.put("DECIWATT:PICOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 11}});
        c.put("DECIWATT:TERAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -13}});
        c.put("DECIWATT:WATT", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("DECIWATT:YOCTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 23}});
        c.put("DECIWATT:YOTTAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -25}});
        c.put("DECIWATT:ZEPTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 20}});
        c.put("DECIWATT:ZETTAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -22}});
        c.put("DEKAWATT:ATTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 19}});
        c.put("DEKAWATT:CENTIWATT", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("DEKAWATT:DECIWATT", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("DEKAWATT:EXAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -17}});
        c.put("DEKAWATT:FEMTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 16}});
        c.put("DEKAWATT:GIGAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -8}});
        c.put("DEKAWATT:HECTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("DEKAWATT:KILOWATT", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("DEKAWATT:MEGAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -5}});
        c.put("DEKAWATT:MICROWATT", new double[][]{new double[]{0, 1}, new double[]{10, 7}});
        c.put("DEKAWATT:MILLIWATT", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("DEKAWATT:NANOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 10}});
        c.put("DEKAWATT:PETAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -14}});
        c.put("DEKAWATT:PICOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 13}});
        c.put("DEKAWATT:TERAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -11}});
        c.put("DEKAWATT:WATT", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("DEKAWATT:YOCTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 25}});
        c.put("DEKAWATT:YOTTAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -23}});
        c.put("DEKAWATT:ZEPTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 22}});
        c.put("DEKAWATT:ZETTAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -20}});
        c.put("EXAWATT:ATTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 36}});
        c.put("EXAWATT:CENTIWATT", new double[][]{new double[]{0, 1}, new double[]{10, 20}});
        c.put("EXAWATT:DECIWATT", new double[][]{new double[]{0, 1}, new double[]{10, 19}});
        c.put("EXAWATT:DEKAWATT", new double[][]{new double[]{0, 1}, new double[]{10, 17}});
        c.put("EXAWATT:FEMTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("EXAWATT:GIGAWATT", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("EXAWATT:HECTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 16}});
        c.put("EXAWATT:KILOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("EXAWATT:MEGAWATT", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("EXAWATT:MICROWATT", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("EXAWATT:MILLIWATT", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("EXAWATT:NANOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("EXAWATT:PETAWATT", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("EXAWATT:PICOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("EXAWATT:TERAWATT", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("EXAWATT:WATT", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("EXAWATT:YOCTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 42}});
        c.put("EXAWATT:YOTTAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("EXAWATT:ZEPTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 39}});
        c.put("EXAWATT:ZETTAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("FEMTOWATT:ATTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("FEMTOWATT:CENTIWATT", new double[][]{new double[]{0, 1}, new double[]{10, -13}});
        c.put("FEMTOWATT:DECIWATT", new double[][]{new double[]{0, 1}, new double[]{10, -14}});
        c.put("FEMTOWATT:DEKAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -16}});
        c.put("FEMTOWATT:EXAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("FEMTOWATT:GIGAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("FEMTOWATT:HECTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, -17}});
        c.put("FEMTOWATT:KILOWATT", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("FEMTOWATT:MEGAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("FEMTOWATT:MICROWATT", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("FEMTOWATT:MILLIWATT", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("FEMTOWATT:NANOWATT", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("FEMTOWATT:PETAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("FEMTOWATT:PICOWATT", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("FEMTOWATT:TERAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("FEMTOWATT:WATT", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("FEMTOWATT:YOCTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("FEMTOWATT:YOTTAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -39}});
        c.put("FEMTOWATT:ZEPTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("FEMTOWATT:ZETTAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -36}});
        c.put("GIGAWATT:ATTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("GIGAWATT:CENTIWATT", new double[][]{new double[]{0, 1}, new double[]{10, 11}});
        c.put("GIGAWATT:DECIWATT", new double[][]{new double[]{0, 1}, new double[]{10, 10}});
        c.put("GIGAWATT:DEKAWATT", new double[][]{new double[]{0, 1}, new double[]{10, 8}});
        c.put("GIGAWATT:EXAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("GIGAWATT:FEMTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("GIGAWATT:HECTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 7}});
        c.put("GIGAWATT:KILOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("GIGAWATT:MEGAWATT", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("GIGAWATT:MICROWATT", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("GIGAWATT:MILLIWATT", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("GIGAWATT:NANOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("GIGAWATT:PETAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("GIGAWATT:PICOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("GIGAWATT:TERAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("GIGAWATT:WATT", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("GIGAWATT:YOCTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("GIGAWATT:YOTTAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("GIGAWATT:ZEPTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("GIGAWATT:ZETTAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("HECTOWATT:ATTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 20}});
        c.put("HECTOWATT:CENTIWATT", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("HECTOWATT:DECIWATT", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("HECTOWATT:DEKAWATT", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("HECTOWATT:EXAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -16}});
        c.put("HECTOWATT:FEMTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 17}});
        c.put("HECTOWATT:GIGAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -7}});
        c.put("HECTOWATT:KILOWATT", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("HECTOWATT:MEGAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("HECTOWATT:MICROWATT", new double[][]{new double[]{0, 1}, new double[]{10, 8}});
        c.put("HECTOWATT:MILLIWATT", new double[][]{new double[]{0, 1}, new double[]{10, 5}});
        c.put("HECTOWATT:NANOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 11}});
        c.put("HECTOWATT:PETAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -13}});
        c.put("HECTOWATT:PICOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 14}});
        c.put("HECTOWATT:TERAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -10}});
        c.put("HECTOWATT:WATT", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("HECTOWATT:YOCTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 26}});
        c.put("HECTOWATT:YOTTAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -22}});
        c.put("HECTOWATT:ZEPTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 23}});
        c.put("HECTOWATT:ZETTAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -19}});
        c.put("KILOWATT:ATTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("KILOWATT:CENTIWATT", new double[][]{new double[]{0, 1}, new double[]{10, 5}});
        c.put("KILOWATT:DECIWATT", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("KILOWATT:DEKAWATT", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("KILOWATT:EXAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("KILOWATT:FEMTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("KILOWATT:GIGAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("KILOWATT:HECTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("KILOWATT:MEGAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("KILOWATT:MICROWATT", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("KILOWATT:MILLIWATT", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("KILOWATT:NANOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("KILOWATT:PETAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("KILOWATT:PICOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("KILOWATT:TERAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("KILOWATT:WATT", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("KILOWATT:YOCTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("KILOWATT:YOTTAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("KILOWATT:ZEPTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("KILOWATT:ZETTAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("MEGAWATT:ATTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("MEGAWATT:CENTIWATT", new double[][]{new double[]{0, 1}, new double[]{10, 8}});
        c.put("MEGAWATT:DECIWATT", new double[][]{new double[]{0, 1}, new double[]{10, 7}});
        c.put("MEGAWATT:DEKAWATT", new double[][]{new double[]{0, 1}, new double[]{10, 5}});
        c.put("MEGAWATT:EXAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("MEGAWATT:FEMTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("MEGAWATT:GIGAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("MEGAWATT:HECTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("MEGAWATT:KILOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("MEGAWATT:MICROWATT", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("MEGAWATT:MILLIWATT", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("MEGAWATT:NANOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("MEGAWATT:PETAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("MEGAWATT:PICOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("MEGAWATT:TERAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("MEGAWATT:WATT", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("MEGAWATT:YOCTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("MEGAWATT:YOTTAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("MEGAWATT:ZEPTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("MEGAWATT:ZETTAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("MICROWATT:ATTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("MICROWATT:CENTIWATT", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("MICROWATT:DECIWATT", new double[][]{new double[]{0, 1}, new double[]{10, -5}});
        c.put("MICROWATT:DEKAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -7}});
        c.put("MICROWATT:EXAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("MICROWATT:FEMTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("MICROWATT:GIGAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("MICROWATT:HECTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, -8}});
        c.put("MICROWATT:KILOWATT", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("MICROWATT:MEGAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("MICROWATT:MILLIWATT", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("MICROWATT:NANOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("MICROWATT:PETAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("MICROWATT:PICOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("MICROWATT:TERAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("MICROWATT:WATT", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("MICROWATT:YOCTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("MICROWATT:YOTTAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("MICROWATT:ZEPTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("MICROWATT:ZETTAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("MILLIWATT:ATTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("MILLIWATT:CENTIWATT", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("MILLIWATT:DECIWATT", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("MILLIWATT:DEKAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("MILLIWATT:EXAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("MILLIWATT:FEMTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("MILLIWATT:GIGAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("MILLIWATT:HECTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, -5}});
        c.put("MILLIWATT:KILOWATT", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("MILLIWATT:MEGAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("MILLIWATT:MICROWATT", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("MILLIWATT:NANOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("MILLIWATT:PETAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("MILLIWATT:PICOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("MILLIWATT:TERAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("MILLIWATT:WATT", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("MILLIWATT:YOCTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("MILLIWATT:YOTTAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("MILLIWATT:ZEPTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("MILLIWATT:ZETTAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("NANOWATT:ATTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("NANOWATT:CENTIWATT", new double[][]{new double[]{0, 1}, new double[]{10, -7}});
        c.put("NANOWATT:DECIWATT", new double[][]{new double[]{0, 1}, new double[]{10, -8}});
        c.put("NANOWATT:DEKAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -10}});
        c.put("NANOWATT:EXAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("NANOWATT:FEMTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("NANOWATT:GIGAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("NANOWATT:HECTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, -11}});
        c.put("NANOWATT:KILOWATT", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("NANOWATT:MEGAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("NANOWATT:MICROWATT", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("NANOWATT:MILLIWATT", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("NANOWATT:PETAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("NANOWATT:PICOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("NANOWATT:TERAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("NANOWATT:WATT", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("NANOWATT:YOCTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("NANOWATT:YOTTAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("NANOWATT:ZEPTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("NANOWATT:ZETTAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("PETAWATT:ATTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("PETAWATT:CENTIWATT", new double[][]{new double[]{0, 1}, new double[]{10, 17}});
        c.put("PETAWATT:DECIWATT", new double[][]{new double[]{0, 1}, new double[]{10, 16}});
        c.put("PETAWATT:DEKAWATT", new double[][]{new double[]{0, 1}, new double[]{10, 14}});
        c.put("PETAWATT:EXAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("PETAWATT:FEMTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("PETAWATT:GIGAWATT", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("PETAWATT:HECTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 13}});
        c.put("PETAWATT:KILOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("PETAWATT:MEGAWATT", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("PETAWATT:MICROWATT", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("PETAWATT:MILLIWATT", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("PETAWATT:NANOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("PETAWATT:PICOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("PETAWATT:TERAWATT", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("PETAWATT:WATT", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("PETAWATT:YOCTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 39}});
        c.put("PETAWATT:YOTTAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("PETAWATT:ZEPTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 36}});
        c.put("PETAWATT:ZETTAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("PICOWATT:ATTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("PICOWATT:CENTIWATT", new double[][]{new double[]{0, 1}, new double[]{10, -10}});
        c.put("PICOWATT:DECIWATT", new double[][]{new double[]{0, 1}, new double[]{10, -11}});
        c.put("PICOWATT:DEKAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -13}});
        c.put("PICOWATT:EXAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("PICOWATT:FEMTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("PICOWATT:GIGAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("PICOWATT:HECTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, -14}});
        c.put("PICOWATT:KILOWATT", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("PICOWATT:MEGAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("PICOWATT:MICROWATT", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("PICOWATT:MILLIWATT", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("PICOWATT:NANOWATT", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("PICOWATT:PETAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("PICOWATT:TERAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("PICOWATT:WATT", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("PICOWATT:YOCTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("PICOWATT:YOTTAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -36}});
        c.put("PICOWATT:ZEPTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("PICOWATT:ZETTAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("TERAWATT:ATTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("TERAWATT:CENTIWATT", new double[][]{new double[]{0, 1}, new double[]{10, 14}});
        c.put("TERAWATT:DECIWATT", new double[][]{new double[]{0, 1}, new double[]{10, 13}});
        c.put("TERAWATT:DEKAWATT", new double[][]{new double[]{0, 1}, new double[]{10, 11}});
        c.put("TERAWATT:EXAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("TERAWATT:FEMTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("TERAWATT:GIGAWATT", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("TERAWATT:HECTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 10}});
        c.put("TERAWATT:KILOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("TERAWATT:MEGAWATT", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("TERAWATT:MICROWATT", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("TERAWATT:MILLIWATT", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("TERAWATT:NANOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("TERAWATT:PETAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("TERAWATT:PICOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("TERAWATT:WATT", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("TERAWATT:YOCTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 36}});
        c.put("TERAWATT:YOTTAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("TERAWATT:ZEPTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("TERAWATT:ZETTAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("WATT:ATTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("WATT:CENTIWATT", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("WATT:DECIWATT", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("WATT:DEKAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("WATT:EXAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("WATT:FEMTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("WATT:GIGAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("WATT:HECTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("WATT:KILOWATT", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("WATT:MEGAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("WATT:MICROWATT", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("WATT:MILLIWATT", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("WATT:NANOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("WATT:PETAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("WATT:PICOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("WATT:TERAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("WATT:YOCTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("WATT:YOTTAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("WATT:ZEPTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("WATT:ZETTAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("YOCTOWATT:ATTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("YOCTOWATT:CENTIWATT", new double[][]{new double[]{0, 1}, new double[]{10, -22}});
        c.put("YOCTOWATT:DECIWATT", new double[][]{new double[]{0, 1}, new double[]{10, -23}});
        c.put("YOCTOWATT:DEKAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -25}});
        c.put("YOCTOWATT:EXAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -42}});
        c.put("YOCTOWATT:FEMTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("YOCTOWATT:GIGAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("YOCTOWATT:HECTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, -26}});
        c.put("YOCTOWATT:KILOWATT", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("YOCTOWATT:MEGAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("YOCTOWATT:MICROWATT", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("YOCTOWATT:MILLIWATT", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("YOCTOWATT:NANOWATT", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("YOCTOWATT:PETAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -39}});
        c.put("YOCTOWATT:PICOWATT", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("YOCTOWATT:TERAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -36}});
        c.put("YOCTOWATT:WATT", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("YOCTOWATT:YOTTAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -48}});
        c.put("YOCTOWATT:ZEPTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("YOCTOWATT:ZETTAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -45}});
        c.put("YOTTAWATT:ATTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 42}});
        c.put("YOTTAWATT:CENTIWATT", new double[][]{new double[]{0, 1}, new double[]{10, 26}});
        c.put("YOTTAWATT:DECIWATT", new double[][]{new double[]{0, 1}, new double[]{10, 25}});
        c.put("YOTTAWATT:DEKAWATT", new double[][]{new double[]{0, 1}, new double[]{10, 23}});
        c.put("YOTTAWATT:EXAWATT", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("YOTTAWATT:FEMTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 39}});
        c.put("YOTTAWATT:GIGAWATT", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("YOTTAWATT:HECTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 22}});
        c.put("YOTTAWATT:KILOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("YOTTAWATT:MEGAWATT", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("YOTTAWATT:MICROWATT", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("YOTTAWATT:MILLIWATT", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("YOTTAWATT:NANOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("YOTTAWATT:PETAWATT", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("YOTTAWATT:PICOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 36}});
        c.put("YOTTAWATT:TERAWATT", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("YOTTAWATT:WATT", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("YOTTAWATT:YOCTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 48}});
        c.put("YOTTAWATT:ZEPTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 45}});
        c.put("YOTTAWATT:ZETTAWATT", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("ZEPTOWATT:ATTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("ZEPTOWATT:CENTIWATT", new double[][]{new double[]{0, 1}, new double[]{10, -19}});
        c.put("ZEPTOWATT:DECIWATT", new double[][]{new double[]{0, 1}, new double[]{10, -20}});
        c.put("ZEPTOWATT:DEKAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -22}});
        c.put("ZEPTOWATT:EXAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -39}});
        c.put("ZEPTOWATT:FEMTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("ZEPTOWATT:GIGAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("ZEPTOWATT:HECTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, -23}});
        c.put("ZEPTOWATT:KILOWATT", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("ZEPTOWATT:MEGAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("ZEPTOWATT:MICROWATT", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("ZEPTOWATT:MILLIWATT", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("ZEPTOWATT:NANOWATT", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("ZEPTOWATT:PETAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -36}});
        c.put("ZEPTOWATT:PICOWATT", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("ZEPTOWATT:TERAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("ZEPTOWATT:WATT", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("ZEPTOWATT:YOCTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("ZEPTOWATT:YOTTAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -45}});
        c.put("ZEPTOWATT:ZETTAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -42}});
        c.put("ZETTAWATT:ATTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 39}});
        c.put("ZETTAWATT:CENTIWATT", new double[][]{new double[]{0, 1}, new double[]{10, 23}});
        c.put("ZETTAWATT:DECIWATT", new double[][]{new double[]{0, 1}, new double[]{10, 22}});
        c.put("ZETTAWATT:DEKAWATT", new double[][]{new double[]{0, 1}, new double[]{10, 20}});
        c.put("ZETTAWATT:EXAWATT", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("ZETTAWATT:FEMTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 36}});
        c.put("ZETTAWATT:GIGAWATT", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("ZETTAWATT:HECTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 19}});
        c.put("ZETTAWATT:KILOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("ZETTAWATT:MEGAWATT", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("ZETTAWATT:MICROWATT", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("ZETTAWATT:MILLIWATT", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("ZETTAWATT:NANOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("ZETTAWATT:PETAWATT", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("ZETTAWATT:PICOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("ZETTAWATT:TERAWATT", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("ZETTAWATT:WATT", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("ZETTAWATT:YOCTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 45}});
        c.put("ZETTAWATT:YOTTAWATT", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("ZETTAWATT:ZEPTOWATT", new double[][]{new double[]{0, 1}, new double[]{10, 42}});
        conversions = Collections.unmodifiableMap(c);
    }

    private static final Map<UnitsPower, String> SYMBOLS;
    static {
        Map<UnitsPower, String> s = new HashMap<UnitsPower, String>();
        s.put(UnitsPower.ATTOWATT, "aW");
        s.put(UnitsPower.CENTIWATT, "cW");
        s.put(UnitsPower.DECIWATT, "dW");
        s.put(UnitsPower.DEKAWATT, "daW");
        s.put(UnitsPower.EXAWATT, "EW");
        s.put(UnitsPower.FEMTOWATT, "fW");
        s.put(UnitsPower.GIGAWATT, "GW");
        s.put(UnitsPower.HECTOWATT, "hW");
        s.put(UnitsPower.KILOWATT, "kW");
        s.put(UnitsPower.MEGAWATT, "MW");
        s.put(UnitsPower.MICROWATT, "µW");
        s.put(UnitsPower.MILLIWATT, "mW");
        s.put(UnitsPower.NANOWATT, "nW");
        s.put(UnitsPower.PETAWATT, "PW");
        s.put(UnitsPower.PICOWATT, "pW");
        s.put(UnitsPower.TERAWATT, "TW");
        s.put(UnitsPower.WATT, "W");
        s.put(UnitsPower.YOCTOWATT, "yW");
        s.put(UnitsPower.YOTTAWATT, "YW");
        s.put(UnitsPower.ZEPTOWATT, "zW");
        s.put(UnitsPower.ZETTAWATT, "ZW");
        SYMBOLS = s;
    }

    public static String lookupSymbol(UnitsPower unit) {
        return SYMBOLS.get(unit);
    }

    public static final Ice.ObjectFactory makeFactory(final omero.client client) {

        return new Ice.ObjectFactory() {

            public Ice.Object create(String arg0) {
                return new PowerI();
            }

            public void destroy() {
                // no-op
            }

        };
    };

    //
    // CONVERSIONS
    //

    public static ome.xml.model.enums.UnitsPower makeXMLUnit(String unit) {
        try {
            return ome.xml.model.enums.UnitsPower
                    .fromString((String) unit);
        } catch (EnumerationException e) {
            throw new RuntimeException("Bad Power unit: " + unit, e);
        }
    }

    public static ome.units.quantity.Power makeXMLQuantity(double d, String unit) {
        ome.units.unit.Unit<ome.units.quantity.Power> units =
                ome.xml.model.enums.handlers.UnitsPowerEnumHandler
                        .getBaseUnit(makeXMLUnit(unit));
        return new ome.units.quantity.Power(d, units);
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
   public static ome.units.quantity.Power convert(Power t) {
       if (t == null) {
           return null;
       }

       Double v = t.getValue();
       // Use the code/symbol-mapping in the ome.model.enums files
       // to convert to the specification value.
       String u = ome.model.enums.UnitsPower.valueOf(
               t.getUnit().toString()).getSymbol();
       ome.xml.model.enums.UnitsPower units = makeXMLUnit(u);
       ome.units.unit.Unit<ome.units.quantity.Power> units2 =
               ome.xml.model.enums.handlers.UnitsPowerEnumHandler
                       .getBaseUnit(units);

       return new ome.units.quantity.Power(v, units2);
   }


    //
    // REGULAR ICE CLASS
    //

    public final static Ice.ObjectFactory Factory = makeFactory(null);

    public PowerI() {
        super();
    }

    public PowerI(double d, UnitsPower unit) {
        super();
        this.setUnit(unit);
        this.setValue(d);
    }

    public PowerI(double d,
            Unit<ome.units.quantity.Power> unit) {
        this(d, ome.model.enums.UnitsPower.bySymbol(unit.getSymbol()));
    }

   /**
    * Copy constructor that converts the given {@link omero.model.Power}
    * based on the given ome-xml enum
    */
   public PowerI(Power value, Unit<ome.units.quantity.Power> ul) {
       this(value,
            ome.model.enums.UnitsPower.bySymbol(ul.getSymbol()).toString());
   }

   /**
    * Copy constructor that converts the given {@link omero.model.Power}
    * based on the given ome.model enum
    */
   public PowerI(double d, ome.model.enums.UnitsPower ul) {
        this(d, UnitsPower.valueOf(ul.toString()));
    }

   /**
    * Copy constructor that converts the given {@link omero.model.Power}
    * based on the given enum string.
    *
    * @param target String representation of the CODE enum
    */
    public PowerI(Power value, String target) {
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
            setUnit(UnitsPower.valueOf(target));
       }
    }

   /**
    * Copy constructor that converts between units if possible.
    *
    * @param target unit that is desired. non-null.
    */
    public PowerI(Power value, UnitsPower target) {
        this(value, target.toString());
    }

    /**
     * Convert a Bio-Formats {@link Length} to an OMERO Length.
     */
    public PowerI(ome.units.quantity.Power value) {
        ome.model.enums.UnitsPower internal =
            ome.model.enums.UnitsPower.bySymbol(value.unit().getSymbol());
        UnitsPower ul = UnitsPower.valueOf(internal.toString());
        setValue(value.value().doubleValue());
        setUnit(ul);
    }

    public double getValue(Ice.Current current) {
        return this.value;
    }

    public void setValue(double value , Ice.Current current) {
        this.value = value;
    }

    public UnitsPower getUnit(Ice.Current current) {
        return this.unit;
    }

    public void setUnit(UnitsPower unit, Ice.Current current) {
        this.unit = unit;
    }

    public String getSymbol(Ice.Current current) {
        return SYMBOLS.get(this.unit);
    }

    public Power copy(Ice.Current ignore) {
        PowerI copy = new PowerI();
        copy.setValue(getValue());
        copy.setUnit(getUnit());
        return copy;
    }

    @Override
    public void copyObject(Filterable model, ModelMapper mapper) {
        if (model instanceof ome.model.units.Power) {
            ome.model.units.Power t = (ome.model.units.Power) model;
            this.value = t.getValue();
            this.unit = UnitsPower.valueOf(t.getUnit().toString());
        } else {
            throw new IllegalArgumentException(
              "Power cannot copy from " +
              (model==null ? "null" : model.getClass().getName()));
        }
    }

    @Override
    public Filterable fillObject(ReverseModelMapper mapper) {
        ome.model.enums.UnitsPower ut = ome.model.enums.UnitsPower.valueOf(getUnit().toString());
        ome.model.units.Power t = new ome.model.units.Power(getValue(), ut);
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
        return "Power(" + value + " " + unit + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Power other = (Power) obj;
        if (unit != other.unit)
            return false;
        if (Double.doubleToLongBits(value) != Double
                .doubleToLongBits(other.value))
            return false;
        return true;
    }

}

