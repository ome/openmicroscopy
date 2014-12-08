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

        c.put("AW:CW", new double[][]{new double[]{0, 1}, new double[]{10, -16}});
        c.put("AW:DAW", new double[][]{new double[]{0, 1}, new double[]{10, -19}});
        c.put("AW:DW", new double[][]{new double[]{0, 1}, new double[]{10, -17}});
        c.put("AW:EXAW", new double[][]{new double[]{0, 1}, new double[]{10, -36}});
        c.put("AW:FW", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("AW:GIGAW", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("AW:HW", new double[][]{new double[]{0, 1}, new double[]{10, -20}});
        c.put("AW:KW", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("AW:MEGAW", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("AW:MICROW", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("AW:MW", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("AW:NW", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("AW:PETAW", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("AW:PW", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("AW:TERAW", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("AW:W", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("AW:YOTTAW", new double[][]{new double[]{0, 1}, new double[]{10, -42}});
        c.put("AW:YW", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("AW:ZETTAW", new double[][]{new double[]{0, 1}, new double[]{10, -39}});
        c.put("AW:ZW", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("CW:AW", new double[][]{new double[]{0, 1}, new double[]{10, 16}});
        c.put("CW:DAW", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("CW:DW", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("CW:EXAW", new double[][]{new double[]{0, 1}, new double[]{10, -20}});
        c.put("CW:FW", new double[][]{new double[]{0, 1}, new double[]{10, 13}});
        c.put("CW:GIGAW", new double[][]{new double[]{0, 1}, new double[]{10, -11}});
        c.put("CW:HW", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("CW:KW", new double[][]{new double[]{0, 1}, new double[]{10, -5}});
        c.put("CW:MEGAW", new double[][]{new double[]{0, 1}, new double[]{10, -8}});
        c.put("CW:MICROW", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("CW:MW", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("CW:NW", new double[][]{new double[]{0, 1}, new double[]{10, 7}});
        c.put("CW:PETAW", new double[][]{new double[]{0, 1}, new double[]{10, -17}});
        c.put("CW:PW", new double[][]{new double[]{0, 1}, new double[]{10, 10}});
        c.put("CW:TERAW", new double[][]{new double[]{0, 1}, new double[]{10, -14}});
        c.put("CW:W", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("CW:YOTTAW", new double[][]{new double[]{0, 1}, new double[]{10, -26}});
        c.put("CW:YW", new double[][]{new double[]{0, 1}, new double[]{10, 22}});
        c.put("CW:ZETTAW", new double[][]{new double[]{0, 1}, new double[]{10, -23}});
        c.put("CW:ZW", new double[][]{new double[]{0, 1}, new double[]{10, 19}});
        c.put("DAW:AW", new double[][]{new double[]{0, 1}, new double[]{10, 19}});
        c.put("DAW:CW", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("DAW:DW", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("DAW:EXAW", new double[][]{new double[]{0, 1}, new double[]{10, -17}});
        c.put("DAW:FW", new double[][]{new double[]{0, 1}, new double[]{10, 16}});
        c.put("DAW:GIGAW", new double[][]{new double[]{0, 1}, new double[]{10, -8}});
        c.put("DAW:HW", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("DAW:KW", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("DAW:MEGAW", new double[][]{new double[]{0, 1}, new double[]{10, -5}});
        c.put("DAW:MICROW", new double[][]{new double[]{0, 1}, new double[]{10, 7}});
        c.put("DAW:MW", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("DAW:NW", new double[][]{new double[]{0, 1}, new double[]{10, 10}});
        c.put("DAW:PETAW", new double[][]{new double[]{0, 1}, new double[]{10, -14}});
        c.put("DAW:PW", new double[][]{new double[]{0, 1}, new double[]{10, 13}});
        c.put("DAW:TERAW", new double[][]{new double[]{0, 1}, new double[]{10, -11}});
        c.put("DAW:W", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("DAW:YOTTAW", new double[][]{new double[]{0, 1}, new double[]{10, -23}});
        c.put("DAW:YW", new double[][]{new double[]{0, 1}, new double[]{10, 25}});
        c.put("DAW:ZETTAW", new double[][]{new double[]{0, 1}, new double[]{10, -20}});
        c.put("DAW:ZW", new double[][]{new double[]{0, 1}, new double[]{10, 22}});
        c.put("DW:AW", new double[][]{new double[]{0, 1}, new double[]{10, 17}});
        c.put("DW:CW", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("DW:DAW", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("DW:EXAW", new double[][]{new double[]{0, 1}, new double[]{10, -19}});
        c.put("DW:FW", new double[][]{new double[]{0, 1}, new double[]{10, 14}});
        c.put("DW:GIGAW", new double[][]{new double[]{0, 1}, new double[]{10, -10}});
        c.put("DW:HW", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("DW:KW", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("DW:MEGAW", new double[][]{new double[]{0, 1}, new double[]{10, -7}});
        c.put("DW:MICROW", new double[][]{new double[]{0, 1}, new double[]{10, 5}});
        c.put("DW:MW", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("DW:NW", new double[][]{new double[]{0, 1}, new double[]{10, 8}});
        c.put("DW:PETAW", new double[][]{new double[]{0, 1}, new double[]{10, -16}});
        c.put("DW:PW", new double[][]{new double[]{0, 1}, new double[]{10, 11}});
        c.put("DW:TERAW", new double[][]{new double[]{0, 1}, new double[]{10, -13}});
        c.put("DW:W", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("DW:YOTTAW", new double[][]{new double[]{0, 1}, new double[]{10, -25}});
        c.put("DW:YW", new double[][]{new double[]{0, 1}, new double[]{10, 23}});
        c.put("DW:ZETTAW", new double[][]{new double[]{0, 1}, new double[]{10, -22}});
        c.put("DW:ZW", new double[][]{new double[]{0, 1}, new double[]{10, 20}});
        c.put("EXAW:AW", new double[][]{new double[]{0, 1}, new double[]{10, 36}});
        c.put("EXAW:CW", new double[][]{new double[]{0, 1}, new double[]{10, 20}});
        c.put("EXAW:DAW", new double[][]{new double[]{0, 1}, new double[]{10, 17}});
        c.put("EXAW:DW", new double[][]{new double[]{0, 1}, new double[]{10, 19}});
        c.put("EXAW:FW", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("EXAW:GIGAW", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("EXAW:HW", new double[][]{new double[]{0, 1}, new double[]{10, 16}});
        c.put("EXAW:KW", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("EXAW:MEGAW", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("EXAW:MICROW", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("EXAW:MW", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("EXAW:NW", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("EXAW:PETAW", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("EXAW:PW", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("EXAW:TERAW", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("EXAW:W", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("EXAW:YOTTAW", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("EXAW:YW", new double[][]{new double[]{0, 1}, new double[]{10, 42}});
        c.put("EXAW:ZETTAW", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("EXAW:ZW", new double[][]{new double[]{0, 1}, new double[]{10, 39}});
        c.put("FW:AW", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("FW:CW", new double[][]{new double[]{0, 1}, new double[]{10, -13}});
        c.put("FW:DAW", new double[][]{new double[]{0, 1}, new double[]{10, -16}});
        c.put("FW:DW", new double[][]{new double[]{0, 1}, new double[]{10, -14}});
        c.put("FW:EXAW", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("FW:GIGAW", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("FW:HW", new double[][]{new double[]{0, 1}, new double[]{10, -17}});
        c.put("FW:KW", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("FW:MEGAW", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("FW:MICROW", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("FW:MW", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("FW:NW", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("FW:PETAW", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("FW:PW", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("FW:TERAW", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("FW:W", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("FW:YOTTAW", new double[][]{new double[]{0, 1}, new double[]{10, -39}});
        c.put("FW:YW", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("FW:ZETTAW", new double[][]{new double[]{0, 1}, new double[]{10, -36}});
        c.put("FW:ZW", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("GIGAW:AW", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("GIGAW:CW", new double[][]{new double[]{0, 1}, new double[]{10, 11}});
        c.put("GIGAW:DAW", new double[][]{new double[]{0, 1}, new double[]{10, 8}});
        c.put("GIGAW:DW", new double[][]{new double[]{0, 1}, new double[]{10, 10}});
        c.put("GIGAW:EXAW", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("GIGAW:FW", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("GIGAW:HW", new double[][]{new double[]{0, 1}, new double[]{10, 7}});
        c.put("GIGAW:KW", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("GIGAW:MEGAW", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("GIGAW:MICROW", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("GIGAW:MW", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("GIGAW:NW", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("GIGAW:PETAW", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("GIGAW:PW", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("GIGAW:TERAW", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("GIGAW:W", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("GIGAW:YOTTAW", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("GIGAW:YW", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("GIGAW:ZETTAW", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("GIGAW:ZW", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("HW:AW", new double[][]{new double[]{0, 1}, new double[]{10, 20}});
        c.put("HW:CW", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("HW:DAW", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("HW:DW", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("HW:EXAW", new double[][]{new double[]{0, 1}, new double[]{10, -16}});
        c.put("HW:FW", new double[][]{new double[]{0, 1}, new double[]{10, 17}});
        c.put("HW:GIGAW", new double[][]{new double[]{0, 1}, new double[]{10, -7}});
        c.put("HW:KW", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("HW:MEGAW", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("HW:MICROW", new double[][]{new double[]{0, 1}, new double[]{10, 8}});
        c.put("HW:MW", new double[][]{new double[]{0, 1}, new double[]{10, 5}});
        c.put("HW:NW", new double[][]{new double[]{0, 1}, new double[]{10, 11}});
        c.put("HW:PETAW", new double[][]{new double[]{0, 1}, new double[]{10, -13}});
        c.put("HW:PW", new double[][]{new double[]{0, 1}, new double[]{10, 14}});
        c.put("HW:TERAW", new double[][]{new double[]{0, 1}, new double[]{10, -10}});
        c.put("HW:W", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("HW:YOTTAW", new double[][]{new double[]{0, 1}, new double[]{10, -22}});
        c.put("HW:YW", new double[][]{new double[]{0, 1}, new double[]{10, 26}});
        c.put("HW:ZETTAW", new double[][]{new double[]{0, 1}, new double[]{10, -19}});
        c.put("HW:ZW", new double[][]{new double[]{0, 1}, new double[]{10, 23}});
        c.put("KW:AW", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("KW:CW", new double[][]{new double[]{0, 1}, new double[]{10, 5}});
        c.put("KW:DAW", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("KW:DW", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("KW:EXAW", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("KW:FW", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("KW:GIGAW", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("KW:HW", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("KW:MEGAW", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("KW:MICROW", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("KW:MW", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("KW:NW", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("KW:PETAW", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("KW:PW", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("KW:TERAW", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("KW:W", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("KW:YOTTAW", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("KW:YW", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("KW:ZETTAW", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("KW:ZW", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("MEGAW:AW", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("MEGAW:CW", new double[][]{new double[]{0, 1}, new double[]{10, 8}});
        c.put("MEGAW:DAW", new double[][]{new double[]{0, 1}, new double[]{10, 5}});
        c.put("MEGAW:DW", new double[][]{new double[]{0, 1}, new double[]{10, 7}});
        c.put("MEGAW:EXAW", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("MEGAW:FW", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("MEGAW:GIGAW", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("MEGAW:HW", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("MEGAW:KW", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("MEGAW:MICROW", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("MEGAW:MW", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("MEGAW:NW", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("MEGAW:PETAW", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("MEGAW:PW", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("MEGAW:TERAW", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("MEGAW:W", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("MEGAW:YOTTAW", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("MEGAW:YW", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("MEGAW:ZETTAW", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("MEGAW:ZW", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("MICROW:AW", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("MICROW:CW", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("MICROW:DAW", new double[][]{new double[]{0, 1}, new double[]{10, -7}});
        c.put("MICROW:DW", new double[][]{new double[]{0, 1}, new double[]{10, -5}});
        c.put("MICROW:EXAW", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("MICROW:FW", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("MICROW:GIGAW", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("MICROW:HW", new double[][]{new double[]{0, 1}, new double[]{10, -8}});
        c.put("MICROW:KW", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("MICROW:MEGAW", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("MICROW:MW", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("MICROW:NW", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("MICROW:PETAW", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("MICROW:PW", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("MICROW:TERAW", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("MICROW:W", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("MICROW:YOTTAW", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("MICROW:YW", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("MICROW:ZETTAW", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("MICROW:ZW", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("MW:AW", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("MW:CW", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("MW:DAW", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("MW:DW", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("MW:EXAW", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("MW:FW", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("MW:GIGAW", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("MW:HW", new double[][]{new double[]{0, 1}, new double[]{10, -5}});
        c.put("MW:KW", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("MW:MEGAW", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("MW:MICROW", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("MW:NW", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("MW:PETAW", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("MW:PW", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("MW:TERAW", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("MW:W", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("MW:YOTTAW", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("MW:YW", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("MW:ZETTAW", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("MW:ZW", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("NW:AW", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("NW:CW", new double[][]{new double[]{0, 1}, new double[]{10, -7}});
        c.put("NW:DAW", new double[][]{new double[]{0, 1}, new double[]{10, -10}});
        c.put("NW:DW", new double[][]{new double[]{0, 1}, new double[]{10, -8}});
        c.put("NW:EXAW", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("NW:FW", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("NW:GIGAW", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("NW:HW", new double[][]{new double[]{0, 1}, new double[]{10, -11}});
        c.put("NW:KW", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("NW:MEGAW", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("NW:MICROW", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("NW:MW", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("NW:PETAW", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("NW:PW", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("NW:TERAW", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("NW:W", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("NW:YOTTAW", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("NW:YW", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("NW:ZETTAW", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("NW:ZW", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("PETAW:AW", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("PETAW:CW", new double[][]{new double[]{0, 1}, new double[]{10, 17}});
        c.put("PETAW:DAW", new double[][]{new double[]{0, 1}, new double[]{10, 14}});
        c.put("PETAW:DW", new double[][]{new double[]{0, 1}, new double[]{10, 16}});
        c.put("PETAW:EXAW", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("PETAW:FW", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("PETAW:GIGAW", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("PETAW:HW", new double[][]{new double[]{0, 1}, new double[]{10, 13}});
        c.put("PETAW:KW", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("PETAW:MEGAW", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("PETAW:MICROW", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("PETAW:MW", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("PETAW:NW", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("PETAW:PW", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("PETAW:TERAW", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("PETAW:W", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("PETAW:YOTTAW", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("PETAW:YW", new double[][]{new double[]{0, 1}, new double[]{10, 39}});
        c.put("PETAW:ZETTAW", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("PETAW:ZW", new double[][]{new double[]{0, 1}, new double[]{10, 36}});
        c.put("PW:AW", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("PW:CW", new double[][]{new double[]{0, 1}, new double[]{10, -10}});
        c.put("PW:DAW", new double[][]{new double[]{0, 1}, new double[]{10, -13}});
        c.put("PW:DW", new double[][]{new double[]{0, 1}, new double[]{10, -11}});
        c.put("PW:EXAW", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("PW:FW", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("PW:GIGAW", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("PW:HW", new double[][]{new double[]{0, 1}, new double[]{10, -14}});
        c.put("PW:KW", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("PW:MEGAW", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("PW:MICROW", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("PW:MW", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("PW:NW", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("PW:PETAW", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("PW:TERAW", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("PW:W", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("PW:YOTTAW", new double[][]{new double[]{0, 1}, new double[]{10, -36}});
        c.put("PW:YW", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("PW:ZETTAW", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("PW:ZW", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("TERAW:AW", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("TERAW:CW", new double[][]{new double[]{0, 1}, new double[]{10, 14}});
        c.put("TERAW:DAW", new double[][]{new double[]{0, 1}, new double[]{10, 11}});
        c.put("TERAW:DW", new double[][]{new double[]{0, 1}, new double[]{10, 13}});
        c.put("TERAW:EXAW", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("TERAW:FW", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("TERAW:GIGAW", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("TERAW:HW", new double[][]{new double[]{0, 1}, new double[]{10, 10}});
        c.put("TERAW:KW", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("TERAW:MEGAW", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("TERAW:MICROW", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("TERAW:MW", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("TERAW:NW", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("TERAW:PETAW", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("TERAW:PW", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("TERAW:W", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("TERAW:YOTTAW", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("TERAW:YW", new double[][]{new double[]{0, 1}, new double[]{10, 36}});
        c.put("TERAW:ZETTAW", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("TERAW:ZW", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("W:AW", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("W:CW", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("W:DAW", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("W:DW", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("W:EXAW", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("W:FW", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("W:GIGAW", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("W:HW", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("W:KW", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("W:MEGAW", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("W:MICROW", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("W:MW", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("W:NW", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("W:PETAW", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("W:PW", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("W:TERAW", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("W:YOTTAW", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("W:YW", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("W:ZETTAW", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("W:ZW", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("YOTTAW:AW", new double[][]{new double[]{0, 1}, new double[]{10, 42}});
        c.put("YOTTAW:CW", new double[][]{new double[]{0, 1}, new double[]{10, 26}});
        c.put("YOTTAW:DAW", new double[][]{new double[]{0, 1}, new double[]{10, 23}});
        c.put("YOTTAW:DW", new double[][]{new double[]{0, 1}, new double[]{10, 25}});
        c.put("YOTTAW:EXAW", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("YOTTAW:FW", new double[][]{new double[]{0, 1}, new double[]{10, 39}});
        c.put("YOTTAW:GIGAW", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("YOTTAW:HW", new double[][]{new double[]{0, 1}, new double[]{10, 22}});
        c.put("YOTTAW:KW", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("YOTTAW:MEGAW", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("YOTTAW:MICROW", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("YOTTAW:MW", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("YOTTAW:NW", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("YOTTAW:PETAW", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("YOTTAW:PW", new double[][]{new double[]{0, 1}, new double[]{10, 36}});
        c.put("YOTTAW:TERAW", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("YOTTAW:W", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("YOTTAW:YW", new double[][]{new double[]{0, 1}, new double[]{10, 48}});
        c.put("YOTTAW:ZETTAW", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("YOTTAW:ZW", new double[][]{new double[]{0, 1}, new double[]{10, 45}});
        c.put("YW:AW", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("YW:CW", new double[][]{new double[]{0, 1}, new double[]{10, -22}});
        c.put("YW:DAW", new double[][]{new double[]{0, 1}, new double[]{10, -25}});
        c.put("YW:DW", new double[][]{new double[]{0, 1}, new double[]{10, -23}});
        c.put("YW:EXAW", new double[][]{new double[]{0, 1}, new double[]{10, -42}});
        c.put("YW:FW", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("YW:GIGAW", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("YW:HW", new double[][]{new double[]{0, 1}, new double[]{10, -26}});
        c.put("YW:KW", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("YW:MEGAW", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("YW:MICROW", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("YW:MW", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("YW:NW", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("YW:PETAW", new double[][]{new double[]{0, 1}, new double[]{10, -39}});
        c.put("YW:PW", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("YW:TERAW", new double[][]{new double[]{0, 1}, new double[]{10, -36}});
        c.put("YW:W", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("YW:YOTTAW", new double[][]{new double[]{0, 1}, new double[]{10, -48}});
        c.put("YW:ZETTAW", new double[][]{new double[]{0, 1}, new double[]{10, -45}});
        c.put("YW:ZW", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("ZETTAW:AW", new double[][]{new double[]{0, 1}, new double[]{10, 39}});
        c.put("ZETTAW:CW", new double[][]{new double[]{0, 1}, new double[]{10, 23}});
        c.put("ZETTAW:DAW", new double[][]{new double[]{0, 1}, new double[]{10, 20}});
        c.put("ZETTAW:DW", new double[][]{new double[]{0, 1}, new double[]{10, 22}});
        c.put("ZETTAW:EXAW", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("ZETTAW:FW", new double[][]{new double[]{0, 1}, new double[]{10, 36}});
        c.put("ZETTAW:GIGAW", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("ZETTAW:HW", new double[][]{new double[]{0, 1}, new double[]{10, 19}});
        c.put("ZETTAW:KW", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("ZETTAW:MEGAW", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("ZETTAW:MICROW", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("ZETTAW:MW", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("ZETTAW:NW", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("ZETTAW:PETAW", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("ZETTAW:PW", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("ZETTAW:TERAW", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("ZETTAW:W", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("ZETTAW:YOTTAW", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("ZETTAW:YW", new double[][]{new double[]{0, 1}, new double[]{10, 45}});
        c.put("ZETTAW:ZW", new double[][]{new double[]{0, 1}, new double[]{10, 42}});
        c.put("ZW:AW", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("ZW:CW", new double[][]{new double[]{0, 1}, new double[]{10, -19}});
        c.put("ZW:DAW", new double[][]{new double[]{0, 1}, new double[]{10, -22}});
        c.put("ZW:DW", new double[][]{new double[]{0, 1}, new double[]{10, -20}});
        c.put("ZW:EXAW", new double[][]{new double[]{0, 1}, new double[]{10, -39}});
        c.put("ZW:FW", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("ZW:GIGAW", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("ZW:HW", new double[][]{new double[]{0, 1}, new double[]{10, -23}});
        c.put("ZW:KW", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("ZW:MEGAW", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("ZW:MICROW", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("ZW:MW", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("ZW:NW", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("ZW:PETAW", new double[][]{new double[]{0, 1}, new double[]{10, -36}});
        c.put("ZW:PW", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("ZW:TERAW", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("ZW:W", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("ZW:YOTTAW", new double[][]{new double[]{0, 1}, new double[]{10, -45}});
        c.put("ZW:YW", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("ZW:ZETTAW", new double[][]{new double[]{0, 1}, new double[]{10, -42}});
        conversions = Collections.unmodifiableMap(c);
    }

    private static final Map<UnitsPower, String> SYMBOLS;
    static {
        Map<UnitsPower, String> s = new HashMap<UnitsPower, String>();
        s.put(UnitsPower.AW, "aW");
        s.put(UnitsPower.CW, "cW");
        s.put(UnitsPower.DAW, "daW");
        s.put(UnitsPower.DW, "dW");
        s.put(UnitsPower.EXAW, "EW");
        s.put(UnitsPower.FW, "fW");
        s.put(UnitsPower.GIGAW, "GW");
        s.put(UnitsPower.HW, "hW");
        s.put(UnitsPower.KW, "kW");
        s.put(UnitsPower.MEGAW, "MW");
        s.put(UnitsPower.MICROW, "µW");
        s.put(UnitsPower.MW, "mW");
        s.put(UnitsPower.NW, "nW");
        s.put(UnitsPower.PETAW, "PW");
        s.put(UnitsPower.PW, "pW");
        s.put(UnitsPower.TERAW, "TW");
        s.put(UnitsPower.W, "W");
        s.put(UnitsPower.YOTTAW, "YW");
        s.put(UnitsPower.YW, "yW");
        s.put(UnitsPower.ZETTAW, "ZW");
        s.put(UnitsPower.ZW, "zW");
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

