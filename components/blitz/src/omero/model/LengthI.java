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

import omero.model.enums.UnitsLength;

/**
 * Blitz wrapper around the {@link ome.model.units.Length} class.
 * Like {@link Details} and {@link Permissions}, this object
 * is embedded into other objects and does not have a full life
 * cycle of its own.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 */
public class LengthI extends Length implements ModelBased {

    private static final long serialVersionUID = 1L;

    private static final Map<String, double[][]> conversions;
    static {
        Map<String, double[][]> c = new HashMap<String, double[][]>();

        c.put("ANGSTROM:ATTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 8}});
        c.put("ANGSTROM:CENTIMETER", new double[][]{new double[]{0, 1}, new double[]{10, -8}});
        c.put("ANGSTROM:DECIMETER", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("ANGSTROM:DEKAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -11}});
        c.put("ANGSTROM:EXAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -28}});
        c.put("ANGSTROM:FEMTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 5}});
        c.put("ANGSTROM:GIGAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -19}});
        c.put("ANGSTROM:HECTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("ANGSTROM:KILOMETER", new double[][]{new double[]{0, 1}, new double[]{10, -13}});
        c.put("ANGSTROM:MEGAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -16}});
        c.put("ANGSTROM:METER", new double[][]{new double[]{0, 1}, new double[]{10, -10}});
        c.put("ANGSTROM:MICROMETER", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("ANGSTROM:MILLIMETER", new double[][]{new double[]{0, 1}, new double[]{10, -7}});
        c.put("ANGSTROM:NANOMETER", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("ANGSTROM:PETAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -25}});
        c.put("ANGSTROM:PICOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("ANGSTROM:TERAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -22}});
        c.put("ANGSTROM:YOCTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 14}});
        c.put("ANGSTROM:YOTTAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -34}});
        c.put("ANGSTROM:ZEPTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 11}});
        c.put("ANGSTROM:ZETTAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -31}});
        c.put("ATTOMETER:ANGSTROM", new double[][]{new double[]{0, 1}, new double[]{10, -8}});
        c.put("ATTOMETER:CENTIMETER", new double[][]{new double[]{0, 1}, new double[]{10, -16}});
        c.put("ATTOMETER:DECIMETER", new double[][]{new double[]{0, 1}, new double[]{10, -17}});
        c.put("ATTOMETER:DEKAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -19}});
        c.put("ATTOMETER:EXAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -36}});
        c.put("ATTOMETER:FEMTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("ATTOMETER:GIGAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("ATTOMETER:HECTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, -20}});
        c.put("ATTOMETER:KILOMETER", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("ATTOMETER:MEGAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("ATTOMETER:METER", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("ATTOMETER:MICROMETER", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("ATTOMETER:MILLIMETER", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("ATTOMETER:NANOMETER", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("ATTOMETER:PETAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("ATTOMETER:PICOMETER", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("ATTOMETER:TERAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("ATTOMETER:YOCTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("ATTOMETER:YOTTAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -42}});
        c.put("ATTOMETER:ZEPTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("ATTOMETER:ZETTAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -39}});
        c.put("CENTIMETER:ANGSTROM", new double[][]{new double[]{0, 1}, new double[]{10, 8}});
        c.put("CENTIMETER:ATTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 16}});
        c.put("CENTIMETER:DECIMETER", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("CENTIMETER:DEKAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("CENTIMETER:EXAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -20}});
        c.put("CENTIMETER:FEMTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 13}});
        c.put("CENTIMETER:GIGAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -11}});
        c.put("CENTIMETER:HECTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("CENTIMETER:KILOMETER", new double[][]{new double[]{0, 1}, new double[]{10, -5}});
        c.put("CENTIMETER:MEGAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -8}});
        c.put("CENTIMETER:METER", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("CENTIMETER:MICROMETER", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("CENTIMETER:MILLIMETER", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("CENTIMETER:NANOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 7}});
        c.put("CENTIMETER:PETAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -17}});
        c.put("CENTIMETER:PICOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 10}});
        c.put("CENTIMETER:TERAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -14}});
        c.put("CENTIMETER:YOCTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 22}});
        c.put("CENTIMETER:YOTTAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -26}});
        c.put("CENTIMETER:ZEPTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 19}});
        c.put("CENTIMETER:ZETTAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -23}});
        c.put("DECIMETER:ANGSTROM", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("DECIMETER:ATTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 17}});
        c.put("DECIMETER:CENTIMETER", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("DECIMETER:DEKAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("DECIMETER:EXAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -19}});
        c.put("DECIMETER:FEMTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 14}});
        c.put("DECIMETER:GIGAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -10}});
        c.put("DECIMETER:HECTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("DECIMETER:KILOMETER", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("DECIMETER:MEGAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -7}});
        c.put("DECIMETER:METER", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("DECIMETER:MICROMETER", new double[][]{new double[]{0, 1}, new double[]{10, 5}});
        c.put("DECIMETER:MILLIMETER", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("DECIMETER:NANOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 8}});
        c.put("DECIMETER:PETAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -16}});
        c.put("DECIMETER:PICOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 11}});
        c.put("DECIMETER:TERAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -13}});
        c.put("DECIMETER:YOCTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 23}});
        c.put("DECIMETER:YOTTAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -25}});
        c.put("DECIMETER:ZEPTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 20}});
        c.put("DECIMETER:ZETTAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -22}});
        c.put("DEKAMETER:ANGSTROM", new double[][]{new double[]{0, 1}, new double[]{10, 11}});
        c.put("DEKAMETER:ATTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 19}});
        c.put("DEKAMETER:CENTIMETER", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("DEKAMETER:DECIMETER", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("DEKAMETER:EXAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -17}});
        c.put("DEKAMETER:FEMTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 16}});
        c.put("DEKAMETER:GIGAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -8}});
        c.put("DEKAMETER:HECTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("DEKAMETER:KILOMETER", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("DEKAMETER:MEGAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -5}});
        c.put("DEKAMETER:METER", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("DEKAMETER:MICROMETER", new double[][]{new double[]{0, 1}, new double[]{10, 7}});
        c.put("DEKAMETER:MILLIMETER", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("DEKAMETER:NANOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 10}});
        c.put("DEKAMETER:PETAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -14}});
        c.put("DEKAMETER:PICOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 13}});
        c.put("DEKAMETER:TERAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -11}});
        c.put("DEKAMETER:YOCTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 25}});
        c.put("DEKAMETER:YOTTAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -23}});
        c.put("DEKAMETER:ZEPTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 22}});
        c.put("DEKAMETER:ZETTAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -20}});
        c.put("EXAMETER:ANGSTROM", new double[][]{new double[]{0, 1}, new double[]{10, 28}});
        c.put("EXAMETER:ATTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 36}});
        c.put("EXAMETER:CENTIMETER", new double[][]{new double[]{0, 1}, new double[]{10, 20}});
        c.put("EXAMETER:DECIMETER", new double[][]{new double[]{0, 1}, new double[]{10, 19}});
        c.put("EXAMETER:DEKAMETER", new double[][]{new double[]{0, 1}, new double[]{10, 17}});
        c.put("EXAMETER:FEMTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("EXAMETER:GIGAMETER", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("EXAMETER:HECTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 16}});
        c.put("EXAMETER:KILOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("EXAMETER:MEGAMETER", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("EXAMETER:METER", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("EXAMETER:MICROMETER", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("EXAMETER:MILLIMETER", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("EXAMETER:NANOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("EXAMETER:PETAMETER", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("EXAMETER:PICOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("EXAMETER:TERAMETER", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("EXAMETER:YOCTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 42}});
        c.put("EXAMETER:YOTTAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("EXAMETER:ZEPTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 39}});
        c.put("EXAMETER:ZETTAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("FEMTOMETER:ANGSTROM", new double[][]{new double[]{0, 1}, new double[]{10, -5}});
        c.put("FEMTOMETER:ATTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("FEMTOMETER:CENTIMETER", new double[][]{new double[]{0, 1}, new double[]{10, -13}});
        c.put("FEMTOMETER:DECIMETER", new double[][]{new double[]{0, 1}, new double[]{10, -14}});
        c.put("FEMTOMETER:DEKAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -16}});
        c.put("FEMTOMETER:EXAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("FEMTOMETER:GIGAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("FEMTOMETER:HECTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, -17}});
        c.put("FEMTOMETER:KILOMETER", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("FEMTOMETER:MEGAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("FEMTOMETER:METER", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("FEMTOMETER:MICROMETER", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("FEMTOMETER:MILLIMETER", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("FEMTOMETER:NANOMETER", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("FEMTOMETER:PETAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("FEMTOMETER:PICOMETER", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("FEMTOMETER:TERAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("FEMTOMETER:YOCTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("FEMTOMETER:YOTTAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -39}});
        c.put("FEMTOMETER:ZEPTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("FEMTOMETER:ZETTAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -36}});
        c.put("GIGAMETER:ANGSTROM", new double[][]{new double[]{0, 1}, new double[]{10, 19}});
        c.put("GIGAMETER:ATTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("GIGAMETER:CENTIMETER", new double[][]{new double[]{0, 1}, new double[]{10, 11}});
        c.put("GIGAMETER:DECIMETER", new double[][]{new double[]{0, 1}, new double[]{10, 10}});
        c.put("GIGAMETER:DEKAMETER", new double[][]{new double[]{0, 1}, new double[]{10, 8}});
        c.put("GIGAMETER:EXAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("GIGAMETER:FEMTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("GIGAMETER:HECTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 7}});
        c.put("GIGAMETER:KILOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("GIGAMETER:MEGAMETER", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("GIGAMETER:METER", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("GIGAMETER:MICROMETER", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("GIGAMETER:MILLIMETER", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("GIGAMETER:NANOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("GIGAMETER:PETAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("GIGAMETER:PICOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("GIGAMETER:TERAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("GIGAMETER:YOCTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("GIGAMETER:YOTTAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("GIGAMETER:ZEPTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("GIGAMETER:ZETTAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("HECTOMETER:ANGSTROM", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("HECTOMETER:ATTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 20}});
        c.put("HECTOMETER:CENTIMETER", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("HECTOMETER:DECIMETER", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("HECTOMETER:DEKAMETER", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("HECTOMETER:EXAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -16}});
        c.put("HECTOMETER:FEMTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 17}});
        c.put("HECTOMETER:GIGAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -7}});
        c.put("HECTOMETER:KILOMETER", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("HECTOMETER:MEGAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("HECTOMETER:METER", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("HECTOMETER:MICROMETER", new double[][]{new double[]{0, 1}, new double[]{10, 8}});
        c.put("HECTOMETER:MILLIMETER", new double[][]{new double[]{0, 1}, new double[]{10, 5}});
        c.put("HECTOMETER:NANOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 11}});
        c.put("HECTOMETER:PETAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -13}});
        c.put("HECTOMETER:PICOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 14}});
        c.put("HECTOMETER:TERAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -10}});
        c.put("HECTOMETER:YOCTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 26}});
        c.put("HECTOMETER:YOTTAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -22}});
        c.put("HECTOMETER:ZEPTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 23}});
        c.put("HECTOMETER:ZETTAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -19}});
        c.put("KILOMETER:ANGSTROM", new double[][]{new double[]{0, 1}, new double[]{10, 13}});
        c.put("KILOMETER:ATTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("KILOMETER:CENTIMETER", new double[][]{new double[]{0, 1}, new double[]{10, 5}});
        c.put("KILOMETER:DECIMETER", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("KILOMETER:DEKAMETER", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("KILOMETER:EXAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("KILOMETER:FEMTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("KILOMETER:GIGAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("KILOMETER:HECTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("KILOMETER:MEGAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("KILOMETER:METER", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("KILOMETER:MICROMETER", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("KILOMETER:MILLIMETER", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("KILOMETER:NANOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("KILOMETER:PETAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("KILOMETER:PICOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("KILOMETER:TERAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("KILOMETER:YOCTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("KILOMETER:YOTTAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("KILOMETER:ZEPTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("KILOMETER:ZETTAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("MEGAMETER:ANGSTROM", new double[][]{new double[]{0, 1}, new double[]{10, 16}});
        c.put("MEGAMETER:ATTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("MEGAMETER:CENTIMETER", new double[][]{new double[]{0, 1}, new double[]{10, 8}});
        c.put("MEGAMETER:DECIMETER", new double[][]{new double[]{0, 1}, new double[]{10, 7}});
        c.put("MEGAMETER:DEKAMETER", new double[][]{new double[]{0, 1}, new double[]{10, 5}});
        c.put("MEGAMETER:EXAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("MEGAMETER:FEMTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("MEGAMETER:GIGAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("MEGAMETER:HECTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("MEGAMETER:KILOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("MEGAMETER:METER", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("MEGAMETER:MICROMETER", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("MEGAMETER:MILLIMETER", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("MEGAMETER:NANOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("MEGAMETER:PETAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("MEGAMETER:PICOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("MEGAMETER:TERAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("MEGAMETER:YOCTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("MEGAMETER:YOTTAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("MEGAMETER:ZEPTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("MEGAMETER:ZETTAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("METER:ANGSTROM", new double[][]{new double[]{0, 1}, new double[]{10, 10}});
        c.put("METER:ATTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("METER:CENTIMETER", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("METER:DECIMETER", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("METER:DEKAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("METER:EXAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("METER:FEMTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("METER:GIGAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("METER:HECTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("METER:KILOMETER", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("METER:MEGAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("METER:MICROMETER", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("METER:MILLIMETER", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("METER:NANOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("METER:PETAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("METER:PICOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("METER:TERAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("METER:YOCTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("METER:YOTTAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("METER:ZEPTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("METER:ZETTAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("MICROMETER:ANGSTROM", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("MICROMETER:ATTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("MICROMETER:CENTIMETER", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("MICROMETER:DECIMETER", new double[][]{new double[]{0, 1}, new double[]{10, -5}});
        c.put("MICROMETER:DEKAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -7}});
        c.put("MICROMETER:EXAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("MICROMETER:FEMTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("MICROMETER:GIGAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("MICROMETER:HECTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, -8}});
        c.put("MICROMETER:KILOMETER", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("MICROMETER:MEGAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("MICROMETER:METER", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("MICROMETER:MILLIMETER", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("MICROMETER:NANOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("MICROMETER:PETAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("MICROMETER:PICOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("MICROMETER:TERAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("MICROMETER:YOCTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("MICROMETER:YOTTAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("MICROMETER:ZEPTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("MICROMETER:ZETTAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("MILLIMETER:ANGSTROM", new double[][]{new double[]{0, 1}, new double[]{10, 7}});
        c.put("MILLIMETER:ATTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("MILLIMETER:CENTIMETER", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("MILLIMETER:DECIMETER", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("MILLIMETER:DEKAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("MILLIMETER:EXAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("MILLIMETER:FEMTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("MILLIMETER:GIGAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("MILLIMETER:HECTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, -5}});
        c.put("MILLIMETER:KILOMETER", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("MILLIMETER:MEGAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("MILLIMETER:METER", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("MILLIMETER:MICROMETER", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("MILLIMETER:NANOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("MILLIMETER:PETAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("MILLIMETER:PICOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("MILLIMETER:TERAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("MILLIMETER:YOCTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("MILLIMETER:YOTTAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("MILLIMETER:ZEPTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("MILLIMETER:ZETTAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("NANOMETER:ANGSTROM", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("NANOMETER:ATTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("NANOMETER:CENTIMETER", new double[][]{new double[]{0, 1}, new double[]{10, -7}});
        c.put("NANOMETER:DECIMETER", new double[][]{new double[]{0, 1}, new double[]{10, -8}});
        c.put("NANOMETER:DEKAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -10}});
        c.put("NANOMETER:EXAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("NANOMETER:FEMTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("NANOMETER:GIGAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("NANOMETER:HECTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, -11}});
        c.put("NANOMETER:KILOMETER", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("NANOMETER:MEGAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("NANOMETER:METER", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("NANOMETER:MICROMETER", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("NANOMETER:MILLIMETER", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("NANOMETER:PETAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("NANOMETER:PICOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("NANOMETER:TERAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("NANOMETER:YOCTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("NANOMETER:YOTTAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("NANOMETER:ZEPTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("NANOMETER:ZETTAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("PETAMETER:ANGSTROM", new double[][]{new double[]{0, 1}, new double[]{10, 25}});
        c.put("PETAMETER:ATTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("PETAMETER:CENTIMETER", new double[][]{new double[]{0, 1}, new double[]{10, 17}});
        c.put("PETAMETER:DECIMETER", new double[][]{new double[]{0, 1}, new double[]{10, 16}});
        c.put("PETAMETER:DEKAMETER", new double[][]{new double[]{0, 1}, new double[]{10, 14}});
        c.put("PETAMETER:EXAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("PETAMETER:FEMTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("PETAMETER:GIGAMETER", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("PETAMETER:HECTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 13}});
        c.put("PETAMETER:KILOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("PETAMETER:MEGAMETER", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("PETAMETER:METER", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("PETAMETER:MICROMETER", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("PETAMETER:MILLIMETER", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("PETAMETER:NANOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("PETAMETER:PICOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("PETAMETER:TERAMETER", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("PETAMETER:YOCTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 39}});
        c.put("PETAMETER:YOTTAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("PETAMETER:ZEPTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 36}});
        c.put("PETAMETER:ZETTAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("PICOMETER:ANGSTROM", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("PICOMETER:ATTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("PICOMETER:CENTIMETER", new double[][]{new double[]{0, 1}, new double[]{10, -10}});
        c.put("PICOMETER:DECIMETER", new double[][]{new double[]{0, 1}, new double[]{10, -11}});
        c.put("PICOMETER:DEKAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -13}});
        c.put("PICOMETER:EXAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("PICOMETER:FEMTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("PICOMETER:GIGAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("PICOMETER:HECTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, -14}});
        c.put("PICOMETER:KILOMETER", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("PICOMETER:MEGAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("PICOMETER:METER", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("PICOMETER:MICROMETER", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("PICOMETER:MILLIMETER", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("PICOMETER:NANOMETER", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("PICOMETER:PETAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("PICOMETER:TERAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("PICOMETER:YOCTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("PICOMETER:YOTTAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -36}});
        c.put("PICOMETER:ZEPTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("PICOMETER:ZETTAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("TERAMETER:ANGSTROM", new double[][]{new double[]{0, 1}, new double[]{10, 22}});
        c.put("TERAMETER:ATTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("TERAMETER:CENTIMETER", new double[][]{new double[]{0, 1}, new double[]{10, 14}});
        c.put("TERAMETER:DECIMETER", new double[][]{new double[]{0, 1}, new double[]{10, 13}});
        c.put("TERAMETER:DEKAMETER", new double[][]{new double[]{0, 1}, new double[]{10, 11}});
        c.put("TERAMETER:EXAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("TERAMETER:FEMTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("TERAMETER:GIGAMETER", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("TERAMETER:HECTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 10}});
        c.put("TERAMETER:KILOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("TERAMETER:MEGAMETER", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("TERAMETER:METER", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("TERAMETER:MICROMETER", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("TERAMETER:MILLIMETER", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("TERAMETER:NANOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("TERAMETER:PETAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("TERAMETER:PICOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("TERAMETER:YOCTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 36}});
        c.put("TERAMETER:YOTTAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("TERAMETER:ZEPTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("TERAMETER:ZETTAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("YOCTOMETER:ANGSTROM", new double[][]{new double[]{0, 1}, new double[]{10, -14}});
        c.put("YOCTOMETER:ATTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("YOCTOMETER:CENTIMETER", new double[][]{new double[]{0, 1}, new double[]{10, -22}});
        c.put("YOCTOMETER:DECIMETER", new double[][]{new double[]{0, 1}, new double[]{10, -23}});
        c.put("YOCTOMETER:DEKAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -25}});
        c.put("YOCTOMETER:EXAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -42}});
        c.put("YOCTOMETER:FEMTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("YOCTOMETER:GIGAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("YOCTOMETER:HECTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, -26}});
        c.put("YOCTOMETER:KILOMETER", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("YOCTOMETER:MEGAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("YOCTOMETER:METER", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("YOCTOMETER:MICROMETER", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("YOCTOMETER:MILLIMETER", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("YOCTOMETER:NANOMETER", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("YOCTOMETER:PETAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -39}});
        c.put("YOCTOMETER:PICOMETER", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("YOCTOMETER:TERAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -36}});
        c.put("YOCTOMETER:YOTTAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -48}});
        c.put("YOCTOMETER:ZEPTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("YOCTOMETER:ZETTAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -45}});
        c.put("YOTTAMETER:ANGSTROM", new double[][]{new double[]{0, 1}, new double[]{10, 34}});
        c.put("YOTTAMETER:ATTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 42}});
        c.put("YOTTAMETER:CENTIMETER", new double[][]{new double[]{0, 1}, new double[]{10, 26}});
        c.put("YOTTAMETER:DECIMETER", new double[][]{new double[]{0, 1}, new double[]{10, 25}});
        c.put("YOTTAMETER:DEKAMETER", new double[][]{new double[]{0, 1}, new double[]{10, 23}});
        c.put("YOTTAMETER:EXAMETER", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("YOTTAMETER:FEMTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 39}});
        c.put("YOTTAMETER:GIGAMETER", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("YOTTAMETER:HECTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 22}});
        c.put("YOTTAMETER:KILOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("YOTTAMETER:MEGAMETER", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("YOTTAMETER:METER", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("YOTTAMETER:MICROMETER", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("YOTTAMETER:MILLIMETER", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("YOTTAMETER:NANOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("YOTTAMETER:PETAMETER", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("YOTTAMETER:PICOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 36}});
        c.put("YOTTAMETER:TERAMETER", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("YOTTAMETER:YOCTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 48}});
        c.put("YOTTAMETER:ZEPTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 45}});
        c.put("YOTTAMETER:ZETTAMETER", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("ZEPTOMETER:ANGSTROM", new double[][]{new double[]{0, 1}, new double[]{10, -11}});
        c.put("ZEPTOMETER:ATTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("ZEPTOMETER:CENTIMETER", new double[][]{new double[]{0, 1}, new double[]{10, -19}});
        c.put("ZEPTOMETER:DECIMETER", new double[][]{new double[]{0, 1}, new double[]{10, -20}});
        c.put("ZEPTOMETER:DEKAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -22}});
        c.put("ZEPTOMETER:EXAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -39}});
        c.put("ZEPTOMETER:FEMTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("ZEPTOMETER:GIGAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("ZEPTOMETER:HECTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, -23}});
        c.put("ZEPTOMETER:KILOMETER", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("ZEPTOMETER:MEGAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("ZEPTOMETER:METER", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("ZEPTOMETER:MICROMETER", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("ZEPTOMETER:MILLIMETER", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("ZEPTOMETER:NANOMETER", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("ZEPTOMETER:PETAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -36}});
        c.put("ZEPTOMETER:PICOMETER", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("ZEPTOMETER:TERAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("ZEPTOMETER:YOCTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("ZEPTOMETER:YOTTAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -45}});
        c.put("ZEPTOMETER:ZETTAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -42}});
        c.put("ZETTAMETER:ANGSTROM", new double[][]{new double[]{0, 1}, new double[]{10, 31}});
        c.put("ZETTAMETER:ATTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 39}});
        c.put("ZETTAMETER:CENTIMETER", new double[][]{new double[]{0, 1}, new double[]{10, 23}});
        c.put("ZETTAMETER:DECIMETER", new double[][]{new double[]{0, 1}, new double[]{10, 22}});
        c.put("ZETTAMETER:DEKAMETER", new double[][]{new double[]{0, 1}, new double[]{10, 20}});
        c.put("ZETTAMETER:EXAMETER", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("ZETTAMETER:FEMTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 36}});
        c.put("ZETTAMETER:GIGAMETER", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("ZETTAMETER:HECTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 19}});
        c.put("ZETTAMETER:KILOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("ZETTAMETER:MEGAMETER", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("ZETTAMETER:METER", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("ZETTAMETER:MICROMETER", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("ZETTAMETER:MILLIMETER", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("ZETTAMETER:NANOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("ZETTAMETER:PETAMETER", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("ZETTAMETER:PICOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("ZETTAMETER:TERAMETER", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("ZETTAMETER:YOCTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 45}});
        c.put("ZETTAMETER:YOTTAMETER", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("ZETTAMETER:ZEPTOMETER", new double[][]{new double[]{0, 1}, new double[]{10, 42}});
        conversions = Collections.unmodifiableMap(c);
    }

    private static final Map<UnitsLength, String> SYMBOLS;
    static {
        Map<UnitsLength, String> s = new HashMap<UnitsLength, String>();
        s.put(UnitsLength.ANGSTROM, "Å");
        s.put(UnitsLength.ASTRONOMICALUNIT, "ua");
        s.put(UnitsLength.ATTOMETER, "am");
        s.put(UnitsLength.CENTIMETER, "cm");
        s.put(UnitsLength.DECIMETER, "dm");
        s.put(UnitsLength.DEKAMETER, "dam");
        s.put(UnitsLength.EXAMETER, "Em");
        s.put(UnitsLength.FEMTOMETER, "fm");
        s.put(UnitsLength.FOOT, "ft");
        s.put(UnitsLength.GIGAMETER, "Gm");
        s.put(UnitsLength.HECTOMETER, "hm");
        s.put(UnitsLength.INCH, "in");
        s.put(UnitsLength.KILOMETER, "km");
        s.put(UnitsLength.LIGHTYEAR, "ly");
        s.put(UnitsLength.LINE, "li");
        s.put(UnitsLength.MEGAMETER, "Mm");
        s.put(UnitsLength.METER, "m");
        s.put(UnitsLength.MICROMETER, "µm");
        s.put(UnitsLength.MILE, "mi");
        s.put(UnitsLength.MILLIMETER, "mm");
        s.put(UnitsLength.NANOMETER, "nm");
        s.put(UnitsLength.PARSEC, "pc");
        s.put(UnitsLength.PETAMETER, "Pm");
        s.put(UnitsLength.PICOMETER, "pm");
        s.put(UnitsLength.PIXEL, "pixel");
        s.put(UnitsLength.POINT, "pt");
        s.put(UnitsLength.REFERENCEFRAME, "reference frame");
        s.put(UnitsLength.TERAMETER, "Tm");
        s.put(UnitsLength.THOU, "thou");
        s.put(UnitsLength.YARD, "yd");
        s.put(UnitsLength.YOCTOMETER, "ym");
        s.put(UnitsLength.YOTTAMETER, "Ym");
        s.put(UnitsLength.ZEPTOMETER, "zm");
        s.put(UnitsLength.ZETTAMETER, "Zm");
        SYMBOLS = s;
    }

    public static String lookupSymbol(UnitsLength unit) {
        return SYMBOLS.get(unit);
    }

    public static final Ice.ObjectFactory makeFactory(final omero.client client) {

        return new Ice.ObjectFactory() {

            public Ice.Object create(String arg0) {
                return new LengthI();
            }

            public void destroy() {
                // no-op
            }

        };
    };

    //
    // CONVERSIONS
    //

    public static ome.xml.model.enums.UnitsLength makeXMLUnit(String unit) {
        try {
            return ome.xml.model.enums.UnitsLength
                    .fromString((String) unit);
        } catch (EnumerationException e) {
            throw new RuntimeException("Bad Length unit: " + unit, e);
        }
    }

    public static ome.units.quantity.Length makeXMLQuantity(double d, String unit) {
        ome.units.unit.Unit<ome.units.quantity.Length> units =
                ome.xml.model.enums.handlers.UnitsLengthEnumHandler
                        .getBaseUnit(makeXMLUnit(unit));
        return new ome.units.quantity.Length(d, units);
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
   public static ome.units.quantity.Length convert(Length t) {
       if (t == null) {
           return null;
       }

       Double v = t.getValue();
       // Use the code/symbol-mapping in the ome.model.enums files
       // to convert to the specification value.
       String u = ome.model.enums.UnitsLength.valueOf(
               t.getUnit().toString()).getSymbol();
       ome.xml.model.enums.UnitsLength units = makeXMLUnit(u);
       ome.units.unit.Unit<ome.units.quantity.Length> units2 =
               ome.xml.model.enums.handlers.UnitsLengthEnumHandler
                       .getBaseUnit(units);

       return new ome.units.quantity.Length(v, units2);
   }


    //
    // REGULAR ICE CLASS
    //

    public final static Ice.ObjectFactory Factory = makeFactory(null);

    public LengthI() {
        super();
    }

    public LengthI(double d, UnitsLength unit) {
        super();
        this.setUnit(unit);
        this.setValue(d);
    }

    public LengthI(double d,
            Unit<ome.units.quantity.Length> unit) {
        this(d, ome.model.enums.UnitsLength.bySymbol(unit.getSymbol()));
    }

   /**
    * Copy constructor that converts the given {@link omero.model.Length}
    * based on the given ome-xml enum
    */
   public LengthI(Length value, Unit<ome.units.quantity.Length> ul) {
       this(value,
            ome.model.enums.UnitsLength.bySymbol(ul.getSymbol()).toString());
   }

   /**
    * Copy constructor that converts the given {@link omero.model.Length}
    * based on the given ome.model enum
    */
   public LengthI(double d, ome.model.enums.UnitsLength ul) {
        this(d, UnitsLength.valueOf(ul.toString()));
    }

   /**
    * Copy constructor that converts the given {@link omero.model.Length}
    * based on the given enum string.
    *
    * @param target String representation of the CODE enum
    */
    public LengthI(Length value, String target) {
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
            setUnit(UnitsLength.valueOf(target));
       }
    }

   /**
    * Copy constructor that converts between units if possible.
    *
    * @param target unit that is desired. non-null.
    */
    public LengthI(Length value, UnitsLength target) {
        this(value, target.toString());
    }

    /**
     * Convert a Bio-Formats {@link Length} to an OMERO Length.
     */
    public LengthI(ome.units.quantity.Length value) {
        ome.model.enums.UnitsLength internal =
            ome.model.enums.UnitsLength.bySymbol(value.unit().getSymbol());
        UnitsLength ul = UnitsLength.valueOf(internal.toString());
        setValue(value.value().doubleValue());
        setUnit(ul);
    }

    public double getValue(Ice.Current current) {
        return this.value;
    }

    public void setValue(double value , Ice.Current current) {
        this.value = value;
    }

    public UnitsLength getUnit(Ice.Current current) {
        return this.unit;
    }

    public void setUnit(UnitsLength unit, Ice.Current current) {
        this.unit = unit;
    }

    public String getSymbol(Ice.Current current) {
        return SYMBOLS.get(this.unit);
    }

    public Length copy(Ice.Current ignore) {
        LengthI copy = new LengthI();
        copy.setValue(getValue());
        copy.setUnit(getUnit());
        return copy;
    }

    @Override
    public void copyObject(Filterable model, ModelMapper mapper) {
        if (model instanceof ome.model.units.Length) {
            ome.model.units.Length t = (ome.model.units.Length) model;
            this.value = t.getValue();
            this.unit = UnitsLength.valueOf(t.getUnit().toString());
        } else {
            throw new IllegalArgumentException(
              "Length cannot copy from " +
              (model==null ? "null" : model.getClass().getName()));
        }
    }

    @Override
    public Filterable fillObject(ReverseModelMapper mapper) {
        ome.model.enums.UnitsLength ut = ome.model.enums.UnitsLength.valueOf(getUnit().toString());
        ome.model.units.Length t = new ome.model.units.Length(getValue(), ut);
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
        return "Length(" + value + " " + unit + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Length other = (Length) obj;
        if (unit != other.unit)
            return false;
        if (Double.doubleToLongBits(value) != Double
                .doubleToLongBits(other.value))
            return false;
        return true;
    }

}

