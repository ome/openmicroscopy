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

        c.put("AM:ANGSTROM", new double[][]{new double[]{0, 1}, new double[]{10, -8}});
        c.put("AM:CM", new double[][]{new double[]{0, 1}, new double[]{10, -16}});
        c.put("AM:DAM", new double[][]{new double[]{0, 1}, new double[]{10, -19}});
        c.put("AM:DM", new double[][]{new double[]{0, 1}, new double[]{10, -17}});
        c.put("AM:EXAM", new double[][]{new double[]{0, 1}, new double[]{10, -36}});
        c.put("AM:FM", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("AM:FT", new double[][]{null});
        c.put("AM:GIGAM", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("AM:HM", new double[][]{new double[]{0, 1}, new double[]{10, -20}});
        c.put("AM:IN", new double[][]{null});
        c.put("AM:KM", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("AM:LI", new double[][]{null});
        c.put("AM:LY", new double[][]{null});
        c.put("AM:M", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("AM:MEGAM", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("AM:MI", new double[][]{null});
        c.put("AM:MICROM", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("AM:MM", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("AM:NM", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("AM:PC", new double[][]{null});
        c.put("AM:PETAM", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("AM:PIXEL", new double[][]{null});
        c.put("AM:PM", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("AM:PT", new double[][]{null});
        c.put("AM:REFERENCEFRAME", new double[][]{null});
        c.put("AM:TERAM", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("AM:THOU", new double[][]{null});
        c.put("AM:UA", new double[][]{null});
        c.put("AM:YD", new double[][]{null});
        c.put("AM:YM", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("AM:YOTTAM", new double[][]{new double[]{0, 1}, new double[]{10, -42}});
        c.put("AM:ZETTAM", new double[][]{new double[]{0, 1}, new double[]{10, -39}});
        c.put("AM:ZM", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("ANGSTROM:AM", new double[][]{new double[]{0, 1}, new double[]{10, 8}});
        c.put("ANGSTROM:CM", new double[][]{new double[]{0, 1}, new double[]{10, -8}});
        c.put("ANGSTROM:DAM", new double[][]{new double[]{0, 1}, new double[]{10, -11}});
        c.put("ANGSTROM:DM", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("ANGSTROM:EXAM", new double[][]{new double[]{0, 1}, new double[]{10, -28}});
        c.put("ANGSTROM:FM", new double[][]{new double[]{0, 1}, new double[]{10, 5}});
        c.put("ANGSTROM:FT", new double[][]{null});
        c.put("ANGSTROM:GIGAM", new double[][]{new double[]{0, 1}, new double[]{10, -19}});
        c.put("ANGSTROM:HM", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("ANGSTROM:IN", new double[][]{null});
        c.put("ANGSTROM:KM", new double[][]{new double[]{0, 1}, new double[]{10, -13}});
        c.put("ANGSTROM:LI", new double[][]{null});
        c.put("ANGSTROM:LY", new double[][]{null});
        c.put("ANGSTROM:M", new double[][]{new double[]{0, 1}, new double[]{10, -10}});
        c.put("ANGSTROM:MEGAM", new double[][]{new double[]{0, 1}, new double[]{10, -16}});
        c.put("ANGSTROM:MI", new double[][]{null});
        c.put("ANGSTROM:MICROM", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("ANGSTROM:MM", new double[][]{new double[]{0, 1}, new double[]{10, -7}});
        c.put("ANGSTROM:NM", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("ANGSTROM:PC", new double[][]{null});
        c.put("ANGSTROM:PETAM", new double[][]{new double[]{0, 1}, new double[]{10, -25}});
        c.put("ANGSTROM:PIXEL", new double[][]{null});
        c.put("ANGSTROM:PM", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("ANGSTROM:PT", new double[][]{null});
        c.put("ANGSTROM:REFERENCEFRAME", new double[][]{null});
        c.put("ANGSTROM:TERAM", new double[][]{new double[]{0, 1}, new double[]{10, -22}});
        c.put("ANGSTROM:THOU", new double[][]{null});
        c.put("ANGSTROM:UA", new double[][]{null});
        c.put("ANGSTROM:YD", new double[][]{null});
        c.put("ANGSTROM:YM", new double[][]{new double[]{0, 1}, new double[]{10, 14}});
        c.put("ANGSTROM:YOTTAM", new double[][]{new double[]{0, 1}, new double[]{10, -34}});
        c.put("ANGSTROM:ZETTAM", new double[][]{new double[]{0, 1}, new double[]{10, -31}});
        c.put("ANGSTROM:ZM", new double[][]{new double[]{0, 1}, new double[]{10, 11}});
        c.put("CM:AM", new double[][]{new double[]{0, 1}, new double[]{10, 16}});
        c.put("CM:ANGSTROM", new double[][]{new double[]{0, 1}, new double[]{10, 8}});
        c.put("CM:DAM", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("CM:DM", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("CM:EXAM", new double[][]{new double[]{0, 1}, new double[]{10, -20}});
        c.put("CM:FM", new double[][]{new double[]{0, 1}, new double[]{10, 13}});
        c.put("CM:FT", new double[][]{null});
        c.put("CM:GIGAM", new double[][]{new double[]{0, 1}, new double[]{10, -11}});
        c.put("CM:HM", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("CM:IN", new double[][]{null});
        c.put("CM:KM", new double[][]{new double[]{0, 1}, new double[]{10, -5}});
        c.put("CM:LI", new double[][]{null});
        c.put("CM:LY", new double[][]{null});
        c.put("CM:M", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("CM:MEGAM", new double[][]{new double[]{0, 1}, new double[]{10, -8}});
        c.put("CM:MI", new double[][]{null});
        c.put("CM:MICROM", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("CM:MM", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("CM:NM", new double[][]{new double[]{0, 1}, new double[]{10, 7}});
        c.put("CM:PC", new double[][]{null});
        c.put("CM:PETAM", new double[][]{new double[]{0, 1}, new double[]{10, -17}});
        c.put("CM:PIXEL", new double[][]{null});
        c.put("CM:PM", new double[][]{new double[]{0, 1}, new double[]{10, 10}});
        c.put("CM:PT", new double[][]{null});
        c.put("CM:REFERENCEFRAME", new double[][]{null});
        c.put("CM:TERAM", new double[][]{new double[]{0, 1}, new double[]{10, -14}});
        c.put("CM:THOU", new double[][]{null});
        c.put("CM:UA", new double[][]{null});
        c.put("CM:YD", new double[][]{null});
        c.put("CM:YM", new double[][]{new double[]{0, 1}, new double[]{10, 22}});
        c.put("CM:YOTTAM", new double[][]{new double[]{0, 1}, new double[]{10, -26}});
        c.put("CM:ZETTAM", new double[][]{new double[]{0, 1}, new double[]{10, -23}});
        c.put("CM:ZM", new double[][]{new double[]{0, 1}, new double[]{10, 19}});
        c.put("DAM:AM", new double[][]{new double[]{0, 1}, new double[]{10, 19}});
        c.put("DAM:ANGSTROM", new double[][]{new double[]{0, 1}, new double[]{10, 11}});
        c.put("DAM:CM", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("DAM:DM", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("DAM:EXAM", new double[][]{new double[]{0, 1}, new double[]{10, -17}});
        c.put("DAM:FM", new double[][]{new double[]{0, 1}, new double[]{10, 16}});
        c.put("DAM:FT", new double[][]{null});
        c.put("DAM:GIGAM", new double[][]{new double[]{0, 1}, new double[]{10, -8}});
        c.put("DAM:HM", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("DAM:IN", new double[][]{null});
        c.put("DAM:KM", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("DAM:LI", new double[][]{null});
        c.put("DAM:LY", new double[][]{null});
        c.put("DAM:M", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("DAM:MEGAM", new double[][]{new double[]{0, 1}, new double[]{10, -5}});
        c.put("DAM:MI", new double[][]{null});
        c.put("DAM:MICROM", new double[][]{new double[]{0, 1}, new double[]{10, 7}});
        c.put("DAM:MM", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("DAM:NM", new double[][]{new double[]{0, 1}, new double[]{10, 10}});
        c.put("DAM:PC", new double[][]{null});
        c.put("DAM:PETAM", new double[][]{new double[]{0, 1}, new double[]{10, -14}});
        c.put("DAM:PIXEL", new double[][]{null});
        c.put("DAM:PM", new double[][]{new double[]{0, 1}, new double[]{10, 13}});
        c.put("DAM:PT", new double[][]{null});
        c.put("DAM:REFERENCEFRAME", new double[][]{null});
        c.put("DAM:TERAM", new double[][]{new double[]{0, 1}, new double[]{10, -11}});
        c.put("DAM:THOU", new double[][]{null});
        c.put("DAM:UA", new double[][]{null});
        c.put("DAM:YD", new double[][]{null});
        c.put("DAM:YM", new double[][]{new double[]{0, 1}, new double[]{10, 25}});
        c.put("DAM:YOTTAM", new double[][]{new double[]{0, 1}, new double[]{10, -23}});
        c.put("DAM:ZETTAM", new double[][]{new double[]{0, 1}, new double[]{10, -20}});
        c.put("DAM:ZM", new double[][]{new double[]{0, 1}, new double[]{10, 22}});
        c.put("DM:AM", new double[][]{new double[]{0, 1}, new double[]{10, 17}});
        c.put("DM:ANGSTROM", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("DM:CM", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("DM:DAM", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("DM:EXAM", new double[][]{new double[]{0, 1}, new double[]{10, -19}});
        c.put("DM:FM", new double[][]{new double[]{0, 1}, new double[]{10, 14}});
        c.put("DM:FT", new double[][]{null});
        c.put("DM:GIGAM", new double[][]{new double[]{0, 1}, new double[]{10, -10}});
        c.put("DM:HM", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("DM:IN", new double[][]{null});
        c.put("DM:KM", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("DM:LI", new double[][]{null});
        c.put("DM:LY", new double[][]{null});
        c.put("DM:M", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("DM:MEGAM", new double[][]{new double[]{0, 1}, new double[]{10, -7}});
        c.put("DM:MI", new double[][]{null});
        c.put("DM:MICROM", new double[][]{new double[]{0, 1}, new double[]{10, 5}});
        c.put("DM:MM", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("DM:NM", new double[][]{new double[]{0, 1}, new double[]{10, 8}});
        c.put("DM:PC", new double[][]{null});
        c.put("DM:PETAM", new double[][]{new double[]{0, 1}, new double[]{10, -16}});
        c.put("DM:PIXEL", new double[][]{null});
        c.put("DM:PM", new double[][]{new double[]{0, 1}, new double[]{10, 11}});
        c.put("DM:PT", new double[][]{null});
        c.put("DM:REFERENCEFRAME", new double[][]{null});
        c.put("DM:TERAM", new double[][]{new double[]{0, 1}, new double[]{10, -13}});
        c.put("DM:THOU", new double[][]{null});
        c.put("DM:UA", new double[][]{null});
        c.put("DM:YD", new double[][]{null});
        c.put("DM:YM", new double[][]{new double[]{0, 1}, new double[]{10, 23}});
        c.put("DM:YOTTAM", new double[][]{new double[]{0, 1}, new double[]{10, -25}});
        c.put("DM:ZETTAM", new double[][]{new double[]{0, 1}, new double[]{10, -22}});
        c.put("DM:ZM", new double[][]{new double[]{0, 1}, new double[]{10, 20}});
        c.put("EXAM:AM", new double[][]{new double[]{0, 1}, new double[]{10, 36}});
        c.put("EXAM:ANGSTROM", new double[][]{new double[]{0, 1}, new double[]{10, 28}});
        c.put("EXAM:CM", new double[][]{new double[]{0, 1}, new double[]{10, 20}});
        c.put("EXAM:DAM", new double[][]{new double[]{0, 1}, new double[]{10, 17}});
        c.put("EXAM:DM", new double[][]{new double[]{0, 1}, new double[]{10, 19}});
        c.put("EXAM:FM", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("EXAM:FT", new double[][]{null});
        c.put("EXAM:GIGAM", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("EXAM:HM", new double[][]{new double[]{0, 1}, new double[]{10, 16}});
        c.put("EXAM:IN", new double[][]{null});
        c.put("EXAM:KM", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("EXAM:LI", new double[][]{null});
        c.put("EXAM:LY", new double[][]{null});
        c.put("EXAM:M", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("EXAM:MEGAM", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("EXAM:MI", new double[][]{null});
        c.put("EXAM:MICROM", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("EXAM:MM", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("EXAM:NM", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("EXAM:PC", new double[][]{null});
        c.put("EXAM:PETAM", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("EXAM:PIXEL", new double[][]{null});
        c.put("EXAM:PM", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("EXAM:PT", new double[][]{null});
        c.put("EXAM:REFERENCEFRAME", new double[][]{null});
        c.put("EXAM:TERAM", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("EXAM:THOU", new double[][]{null});
        c.put("EXAM:UA", new double[][]{null});
        c.put("EXAM:YD", new double[][]{null});
        c.put("EXAM:YM", new double[][]{new double[]{0, 1}, new double[]{10, 42}});
        c.put("EXAM:YOTTAM", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("EXAM:ZETTAM", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("EXAM:ZM", new double[][]{new double[]{0, 1}, new double[]{10, 39}});
        c.put("FM:AM", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("FM:ANGSTROM", new double[][]{new double[]{0, 1}, new double[]{10, -5}});
        c.put("FM:CM", new double[][]{new double[]{0, 1}, new double[]{10, -13}});
        c.put("FM:DAM", new double[][]{new double[]{0, 1}, new double[]{10, -16}});
        c.put("FM:DM", new double[][]{new double[]{0, 1}, new double[]{10, -14}});
        c.put("FM:EXAM", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("FM:FT", new double[][]{null});
        c.put("FM:GIGAM", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("FM:HM", new double[][]{new double[]{0, 1}, new double[]{10, -17}});
        c.put("FM:IN", new double[][]{null});
        c.put("FM:KM", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("FM:LI", new double[][]{null});
        c.put("FM:LY", new double[][]{null});
        c.put("FM:M", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("FM:MEGAM", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("FM:MI", new double[][]{null});
        c.put("FM:MICROM", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("FM:MM", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("FM:NM", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("FM:PC", new double[][]{null});
        c.put("FM:PETAM", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("FM:PIXEL", new double[][]{null});
        c.put("FM:PM", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("FM:PT", new double[][]{null});
        c.put("FM:REFERENCEFRAME", new double[][]{null});
        c.put("FM:TERAM", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("FM:THOU", new double[][]{null});
        c.put("FM:UA", new double[][]{null});
        c.put("FM:YD", new double[][]{null});
        c.put("FM:YM", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("FM:YOTTAM", new double[][]{new double[]{0, 1}, new double[]{10, -39}});
        c.put("FM:ZETTAM", new double[][]{new double[]{0, 1}, new double[]{10, -36}});
        c.put("FM:ZM", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("FT:AM", new double[][]{null});
        c.put("FT:ANGSTROM", new double[][]{null});
        c.put("FT:CM", new double[][]{null});
        c.put("FT:DAM", new double[][]{null});
        c.put("FT:DM", new double[][]{null});
        c.put("FT:EXAM", new double[][]{null});
        c.put("FT:FM", new double[][]{null});
        c.put("FT:GIGAM", new double[][]{null});
        c.put("FT:HM", new double[][]{null});
        c.put("FT:IN", new double[][]{null});
        c.put("FT:KM", new double[][]{null});
        c.put("FT:LI", new double[][]{null});
        c.put("FT:LY", new double[][]{null});
        c.put("FT:M", new double[][]{null});
        c.put("FT:MEGAM", new double[][]{null});
        c.put("FT:MI", new double[][]{null});
        c.put("FT:MICROM", new double[][]{null});
        c.put("FT:MM", new double[][]{null});
        c.put("FT:NM", new double[][]{null});
        c.put("FT:PC", new double[][]{null});
        c.put("FT:PETAM", new double[][]{null});
        c.put("FT:PIXEL", new double[][]{null});
        c.put("FT:PM", new double[][]{null});
        c.put("FT:PT", new double[][]{null});
        c.put("FT:REFERENCEFRAME", new double[][]{null});
        c.put("FT:TERAM", new double[][]{null});
        c.put("FT:THOU", new double[][]{null});
        c.put("FT:UA", new double[][]{null});
        c.put("FT:YD", new double[][]{null});
        c.put("FT:YM", new double[][]{null});
        c.put("FT:YOTTAM", new double[][]{null});
        c.put("FT:ZETTAM", new double[][]{null});
        c.put("FT:ZM", new double[][]{null});
        c.put("GIGAM:AM", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("GIGAM:ANGSTROM", new double[][]{new double[]{0, 1}, new double[]{10, 19}});
        c.put("GIGAM:CM", new double[][]{new double[]{0, 1}, new double[]{10, 11}});
        c.put("GIGAM:DAM", new double[][]{new double[]{0, 1}, new double[]{10, 8}});
        c.put("GIGAM:DM", new double[][]{new double[]{0, 1}, new double[]{10, 10}});
        c.put("GIGAM:EXAM", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("GIGAM:FM", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("GIGAM:FT", new double[][]{null});
        c.put("GIGAM:HM", new double[][]{new double[]{0, 1}, new double[]{10, 7}});
        c.put("GIGAM:IN", new double[][]{null});
        c.put("GIGAM:KM", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("GIGAM:LI", new double[][]{null});
        c.put("GIGAM:LY", new double[][]{null});
        c.put("GIGAM:M", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("GIGAM:MEGAM", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("GIGAM:MI", new double[][]{null});
        c.put("GIGAM:MICROM", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("GIGAM:MM", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("GIGAM:NM", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("GIGAM:PC", new double[][]{null});
        c.put("GIGAM:PETAM", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("GIGAM:PIXEL", new double[][]{null});
        c.put("GIGAM:PM", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("GIGAM:PT", new double[][]{null});
        c.put("GIGAM:REFERENCEFRAME", new double[][]{null});
        c.put("GIGAM:TERAM", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("GIGAM:THOU", new double[][]{null});
        c.put("GIGAM:UA", new double[][]{null});
        c.put("GIGAM:YD", new double[][]{null});
        c.put("GIGAM:YM", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("GIGAM:YOTTAM", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("GIGAM:ZETTAM", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("GIGAM:ZM", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("HM:AM", new double[][]{new double[]{0, 1}, new double[]{10, 20}});
        c.put("HM:ANGSTROM", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("HM:CM", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("HM:DAM", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("HM:DM", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("HM:EXAM", new double[][]{new double[]{0, 1}, new double[]{10, -16}});
        c.put("HM:FM", new double[][]{new double[]{0, 1}, new double[]{10, 17}});
        c.put("HM:FT", new double[][]{null});
        c.put("HM:GIGAM", new double[][]{new double[]{0, 1}, new double[]{10, -7}});
        c.put("HM:IN", new double[][]{null});
        c.put("HM:KM", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("HM:LI", new double[][]{null});
        c.put("HM:LY", new double[][]{null});
        c.put("HM:M", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("HM:MEGAM", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("HM:MI", new double[][]{null});
        c.put("HM:MICROM", new double[][]{new double[]{0, 1}, new double[]{10, 8}});
        c.put("HM:MM", new double[][]{new double[]{0, 1}, new double[]{10, 5}});
        c.put("HM:NM", new double[][]{new double[]{0, 1}, new double[]{10, 11}});
        c.put("HM:PC", new double[][]{null});
        c.put("HM:PETAM", new double[][]{new double[]{0, 1}, new double[]{10, -13}});
        c.put("HM:PIXEL", new double[][]{null});
        c.put("HM:PM", new double[][]{new double[]{0, 1}, new double[]{10, 14}});
        c.put("HM:PT", new double[][]{null});
        c.put("HM:REFERENCEFRAME", new double[][]{null});
        c.put("HM:TERAM", new double[][]{new double[]{0, 1}, new double[]{10, -10}});
        c.put("HM:THOU", new double[][]{null});
        c.put("HM:UA", new double[][]{null});
        c.put("HM:YD", new double[][]{null});
        c.put("HM:YM", new double[][]{new double[]{0, 1}, new double[]{10, 26}});
        c.put("HM:YOTTAM", new double[][]{new double[]{0, 1}, new double[]{10, -22}});
        c.put("HM:ZETTAM", new double[][]{new double[]{0, 1}, new double[]{10, -19}});
        c.put("HM:ZM", new double[][]{new double[]{0, 1}, new double[]{10, 23}});
        c.put("IN:AM", new double[][]{null});
        c.put("IN:ANGSTROM", new double[][]{null});
        c.put("IN:CM", new double[][]{null});
        c.put("IN:DAM", new double[][]{null});
        c.put("IN:DM", new double[][]{null});
        c.put("IN:EXAM", new double[][]{null});
        c.put("IN:FM", new double[][]{null});
        c.put("IN:FT", new double[][]{null});
        c.put("IN:GIGAM", new double[][]{null});
        c.put("IN:HM", new double[][]{null});
        c.put("IN:KM", new double[][]{null});
        c.put("IN:LI", new double[][]{null});
        c.put("IN:LY", new double[][]{null});
        c.put("IN:M", new double[][]{null});
        c.put("IN:MEGAM", new double[][]{null});
        c.put("IN:MI", new double[][]{null});
        c.put("IN:MICROM", new double[][]{null});
        c.put("IN:MM", new double[][]{null});
        c.put("IN:NM", new double[][]{null});
        c.put("IN:PC", new double[][]{null});
        c.put("IN:PETAM", new double[][]{null});
        c.put("IN:PIXEL", new double[][]{null});
        c.put("IN:PM", new double[][]{null});
        c.put("IN:PT", new double[][]{null});
        c.put("IN:REFERENCEFRAME", new double[][]{null});
        c.put("IN:TERAM", new double[][]{null});
        c.put("IN:THOU", new double[][]{null});
        c.put("IN:UA", new double[][]{null});
        c.put("IN:YD", new double[][]{null});
        c.put("IN:YM", new double[][]{null});
        c.put("IN:YOTTAM", new double[][]{null});
        c.put("IN:ZETTAM", new double[][]{null});
        c.put("IN:ZM", new double[][]{null});
        c.put("KM:AM", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("KM:ANGSTROM", new double[][]{new double[]{0, 1}, new double[]{10, 13}});
        c.put("KM:CM", new double[][]{new double[]{0, 1}, new double[]{10, 5}});
        c.put("KM:DAM", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("KM:DM", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("KM:EXAM", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("KM:FM", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("KM:FT", new double[][]{null});
        c.put("KM:GIGAM", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("KM:HM", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("KM:IN", new double[][]{null});
        c.put("KM:LI", new double[][]{null});
        c.put("KM:LY", new double[][]{null});
        c.put("KM:M", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("KM:MEGAM", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("KM:MI", new double[][]{null});
        c.put("KM:MICROM", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("KM:MM", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("KM:NM", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("KM:PC", new double[][]{null});
        c.put("KM:PETAM", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("KM:PIXEL", new double[][]{null});
        c.put("KM:PM", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("KM:PT", new double[][]{null});
        c.put("KM:REFERENCEFRAME", new double[][]{null});
        c.put("KM:TERAM", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("KM:THOU", new double[][]{null});
        c.put("KM:UA", new double[][]{null});
        c.put("KM:YD", new double[][]{null});
        c.put("KM:YM", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("KM:YOTTAM", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("KM:ZETTAM", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("KM:ZM", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("LI:AM", new double[][]{null});
        c.put("LI:ANGSTROM", new double[][]{null});
        c.put("LI:CM", new double[][]{null});
        c.put("LI:DAM", new double[][]{null});
        c.put("LI:DM", new double[][]{null});
        c.put("LI:EXAM", new double[][]{null});
        c.put("LI:FM", new double[][]{null});
        c.put("LI:FT", new double[][]{null});
        c.put("LI:GIGAM", new double[][]{null});
        c.put("LI:HM", new double[][]{null});
        c.put("LI:IN", new double[][]{null});
        c.put("LI:KM", new double[][]{null});
        c.put("LI:LY", new double[][]{null});
        c.put("LI:M", new double[][]{null});
        c.put("LI:MEGAM", new double[][]{null});
        c.put("LI:MI", new double[][]{null});
        c.put("LI:MICROM", new double[][]{null});
        c.put("LI:MM", new double[][]{null});
        c.put("LI:NM", new double[][]{null});
        c.put("LI:PC", new double[][]{null});
        c.put("LI:PETAM", new double[][]{null});
        c.put("LI:PIXEL", new double[][]{null});
        c.put("LI:PM", new double[][]{null});
        c.put("LI:PT", new double[][]{null});
        c.put("LI:REFERENCEFRAME", new double[][]{null});
        c.put("LI:TERAM", new double[][]{null});
        c.put("LI:THOU", new double[][]{null});
        c.put("LI:UA", new double[][]{null});
        c.put("LI:YD", new double[][]{null});
        c.put("LI:YM", new double[][]{null});
        c.put("LI:YOTTAM", new double[][]{null});
        c.put("LI:ZETTAM", new double[][]{null});
        c.put("LI:ZM", new double[][]{null});
        c.put("LY:AM", new double[][]{null});
        c.put("LY:ANGSTROM", new double[][]{null});
        c.put("LY:CM", new double[][]{null});
        c.put("LY:DAM", new double[][]{null});
        c.put("LY:DM", new double[][]{null});
        c.put("LY:EXAM", new double[][]{null});
        c.put("LY:FM", new double[][]{null});
        c.put("LY:FT", new double[][]{null});
        c.put("LY:GIGAM", new double[][]{null});
        c.put("LY:HM", new double[][]{null});
        c.put("LY:IN", new double[][]{null});
        c.put("LY:KM", new double[][]{null});
        c.put("LY:LI", new double[][]{null});
        c.put("LY:M", new double[][]{null});
        c.put("LY:MEGAM", new double[][]{null});
        c.put("LY:MI", new double[][]{null});
        c.put("LY:MICROM", new double[][]{null});
        c.put("LY:MM", new double[][]{null});
        c.put("LY:NM", new double[][]{null});
        c.put("LY:PC", new double[][]{null});
        c.put("LY:PETAM", new double[][]{null});
        c.put("LY:PIXEL", new double[][]{null});
        c.put("LY:PM", new double[][]{null});
        c.put("LY:PT", new double[][]{null});
        c.put("LY:REFERENCEFRAME", new double[][]{null});
        c.put("LY:TERAM", new double[][]{null});
        c.put("LY:THOU", new double[][]{null});
        c.put("LY:UA", new double[][]{null});
        c.put("LY:YD", new double[][]{null});
        c.put("LY:YM", new double[][]{null});
        c.put("LY:YOTTAM", new double[][]{null});
        c.put("LY:ZETTAM", new double[][]{null});
        c.put("LY:ZM", new double[][]{null});
        c.put("M:AM", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("M:ANGSTROM", new double[][]{new double[]{0, 1}, new double[]{10, 10}});
        c.put("M:CM", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("M:DAM", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("M:DM", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("M:EXAM", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("M:FM", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("M:FT", new double[][]{null});
        c.put("M:GIGAM", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("M:HM", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("M:IN", new double[][]{null});
        c.put("M:KM", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("M:LI", new double[][]{null});
        c.put("M:LY", new double[][]{null});
        c.put("M:MEGAM", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("M:MI", new double[][]{null});
        c.put("M:MICROM", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("M:MM", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("M:NM", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("M:PC", new double[][]{null});
        c.put("M:PETAM", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("M:PIXEL", new double[][]{null});
        c.put("M:PM", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("M:PT", new double[][]{null});
        c.put("M:REFERENCEFRAME", new double[][]{null});
        c.put("M:TERAM", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("M:THOU", new double[][]{null});
        c.put("M:UA", new double[][]{null});
        c.put("M:YD", new double[][]{null});
        c.put("M:YM", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("M:YOTTAM", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("M:ZETTAM", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("M:ZM", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("MEGAM:AM", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("MEGAM:ANGSTROM", new double[][]{new double[]{0, 1}, new double[]{10, 16}});
        c.put("MEGAM:CM", new double[][]{new double[]{0, 1}, new double[]{10, 8}});
        c.put("MEGAM:DAM", new double[][]{new double[]{0, 1}, new double[]{10, 5}});
        c.put("MEGAM:DM", new double[][]{new double[]{0, 1}, new double[]{10, 7}});
        c.put("MEGAM:EXAM", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("MEGAM:FM", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("MEGAM:FT", new double[][]{null});
        c.put("MEGAM:GIGAM", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("MEGAM:HM", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("MEGAM:IN", new double[][]{null});
        c.put("MEGAM:KM", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("MEGAM:LI", new double[][]{null});
        c.put("MEGAM:LY", new double[][]{null});
        c.put("MEGAM:M", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("MEGAM:MI", new double[][]{null});
        c.put("MEGAM:MICROM", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("MEGAM:MM", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("MEGAM:NM", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("MEGAM:PC", new double[][]{null});
        c.put("MEGAM:PETAM", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("MEGAM:PIXEL", new double[][]{null});
        c.put("MEGAM:PM", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("MEGAM:PT", new double[][]{null});
        c.put("MEGAM:REFERENCEFRAME", new double[][]{null});
        c.put("MEGAM:TERAM", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("MEGAM:THOU", new double[][]{null});
        c.put("MEGAM:UA", new double[][]{null});
        c.put("MEGAM:YD", new double[][]{null});
        c.put("MEGAM:YM", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("MEGAM:YOTTAM", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("MEGAM:ZETTAM", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("MEGAM:ZM", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("MI:AM", new double[][]{null});
        c.put("MI:ANGSTROM", new double[][]{null});
        c.put("MI:CM", new double[][]{null});
        c.put("MI:DAM", new double[][]{null});
        c.put("MI:DM", new double[][]{null});
        c.put("MI:EXAM", new double[][]{null});
        c.put("MI:FM", new double[][]{null});
        c.put("MI:FT", new double[][]{null});
        c.put("MI:GIGAM", new double[][]{null});
        c.put("MI:HM", new double[][]{null});
        c.put("MI:IN", new double[][]{null});
        c.put("MI:KM", new double[][]{null});
        c.put("MI:LI", new double[][]{null});
        c.put("MI:LY", new double[][]{null});
        c.put("MI:M", new double[][]{null});
        c.put("MI:MEGAM", new double[][]{null});
        c.put("MI:MICROM", new double[][]{null});
        c.put("MI:MM", new double[][]{null});
        c.put("MI:NM", new double[][]{null});
        c.put("MI:PC", new double[][]{null});
        c.put("MI:PETAM", new double[][]{null});
        c.put("MI:PIXEL", new double[][]{null});
        c.put("MI:PM", new double[][]{null});
        c.put("MI:PT", new double[][]{null});
        c.put("MI:REFERENCEFRAME", new double[][]{null});
        c.put("MI:TERAM", new double[][]{null});
        c.put("MI:THOU", new double[][]{null});
        c.put("MI:UA", new double[][]{null});
        c.put("MI:YD", new double[][]{null});
        c.put("MI:YM", new double[][]{null});
        c.put("MI:YOTTAM", new double[][]{null});
        c.put("MI:ZETTAM", new double[][]{null});
        c.put("MI:ZM", new double[][]{null});
        c.put("MICROM:AM", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("MICROM:ANGSTROM", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("MICROM:CM", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("MICROM:DAM", new double[][]{new double[]{0, 1}, new double[]{10, -7}});
        c.put("MICROM:DM", new double[][]{new double[]{0, 1}, new double[]{10, -5}});
        c.put("MICROM:EXAM", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("MICROM:FM", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("MICROM:FT", new double[][]{null});
        c.put("MICROM:GIGAM", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("MICROM:HM", new double[][]{new double[]{0, 1}, new double[]{10, -8}});
        c.put("MICROM:IN", new double[][]{null});
        c.put("MICROM:KM", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("MICROM:LI", new double[][]{null});
        c.put("MICROM:LY", new double[][]{null});
        c.put("MICROM:M", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("MICROM:MEGAM", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("MICROM:MI", new double[][]{null});
        c.put("MICROM:MM", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("MICROM:NM", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("MICROM:PC", new double[][]{null});
        c.put("MICROM:PETAM", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("MICROM:PIXEL", new double[][]{null});
        c.put("MICROM:PM", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("MICROM:PT", new double[][]{null});
        c.put("MICROM:REFERENCEFRAME", new double[][]{null});
        c.put("MICROM:TERAM", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("MICROM:THOU", new double[][]{null});
        c.put("MICROM:UA", new double[][]{null});
        c.put("MICROM:YD", new double[][]{null});
        c.put("MICROM:YM", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("MICROM:YOTTAM", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("MICROM:ZETTAM", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("MICROM:ZM", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("MM:AM", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("MM:ANGSTROM", new double[][]{new double[]{0, 1}, new double[]{10, 7}});
        c.put("MM:CM", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("MM:DAM", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("MM:DM", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("MM:EXAM", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("MM:FM", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("MM:FT", new double[][]{null});
        c.put("MM:GIGAM", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("MM:HM", new double[][]{new double[]{0, 1}, new double[]{10, -5}});
        c.put("MM:IN", new double[][]{null});
        c.put("MM:KM", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("MM:LI", new double[][]{null});
        c.put("MM:LY", new double[][]{null});
        c.put("MM:M", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("MM:MEGAM", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("MM:MI", new double[][]{null});
        c.put("MM:MICROM", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("MM:NM", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("MM:PC", new double[][]{null});
        c.put("MM:PETAM", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("MM:PIXEL", new double[][]{null});
        c.put("MM:PM", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("MM:PT", new double[][]{null});
        c.put("MM:REFERENCEFRAME", new double[][]{null});
        c.put("MM:TERAM", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("MM:THOU", new double[][]{null});
        c.put("MM:UA", new double[][]{null});
        c.put("MM:YD", new double[][]{null});
        c.put("MM:YM", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("MM:YOTTAM", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("MM:ZETTAM", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("MM:ZM", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("NM:AM", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("NM:ANGSTROM", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("NM:CM", new double[][]{new double[]{0, 1}, new double[]{10, -7}});
        c.put("NM:DAM", new double[][]{new double[]{0, 1}, new double[]{10, -10}});
        c.put("NM:DM", new double[][]{new double[]{0, 1}, new double[]{10, -8}});
        c.put("NM:EXAM", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("NM:FM", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("NM:FT", new double[][]{null});
        c.put("NM:GIGAM", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("NM:HM", new double[][]{new double[]{0, 1}, new double[]{10, -11}});
        c.put("NM:IN", new double[][]{null});
        c.put("NM:KM", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("NM:LI", new double[][]{null});
        c.put("NM:LY", new double[][]{null});
        c.put("NM:M", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("NM:MEGAM", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("NM:MI", new double[][]{null});
        c.put("NM:MICROM", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("NM:MM", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("NM:PC", new double[][]{null});
        c.put("NM:PETAM", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("NM:PIXEL", new double[][]{null});
        c.put("NM:PM", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("NM:PT", new double[][]{null});
        c.put("NM:REFERENCEFRAME", new double[][]{null});
        c.put("NM:TERAM", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("NM:THOU", new double[][]{null});
        c.put("NM:UA", new double[][]{null});
        c.put("NM:YD", new double[][]{null});
        c.put("NM:YM", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("NM:YOTTAM", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("NM:ZETTAM", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("NM:ZM", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("PC:AM", new double[][]{null});
        c.put("PC:ANGSTROM", new double[][]{null});
        c.put("PC:CM", new double[][]{null});
        c.put("PC:DAM", new double[][]{null});
        c.put("PC:DM", new double[][]{null});
        c.put("PC:EXAM", new double[][]{null});
        c.put("PC:FM", new double[][]{null});
        c.put("PC:FT", new double[][]{null});
        c.put("PC:GIGAM", new double[][]{null});
        c.put("PC:HM", new double[][]{null});
        c.put("PC:IN", new double[][]{null});
        c.put("PC:KM", new double[][]{null});
        c.put("PC:LI", new double[][]{null});
        c.put("PC:LY", new double[][]{null});
        c.put("PC:M", new double[][]{null});
        c.put("PC:MEGAM", new double[][]{null});
        c.put("PC:MI", new double[][]{null});
        c.put("PC:MICROM", new double[][]{null});
        c.put("PC:MM", new double[][]{null});
        c.put("PC:NM", new double[][]{null});
        c.put("PC:PETAM", new double[][]{null});
        c.put("PC:PIXEL", new double[][]{null});
        c.put("PC:PM", new double[][]{null});
        c.put("PC:PT", new double[][]{null});
        c.put("PC:REFERENCEFRAME", new double[][]{null});
        c.put("PC:TERAM", new double[][]{null});
        c.put("PC:THOU", new double[][]{null});
        c.put("PC:UA", new double[][]{null});
        c.put("PC:YD", new double[][]{null});
        c.put("PC:YM", new double[][]{null});
        c.put("PC:YOTTAM", new double[][]{null});
        c.put("PC:ZETTAM", new double[][]{null});
        c.put("PC:ZM", new double[][]{null});
        c.put("PETAM:AM", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("PETAM:ANGSTROM", new double[][]{new double[]{0, 1}, new double[]{10, 25}});
        c.put("PETAM:CM", new double[][]{new double[]{0, 1}, new double[]{10, 17}});
        c.put("PETAM:DAM", new double[][]{new double[]{0, 1}, new double[]{10, 14}});
        c.put("PETAM:DM", new double[][]{new double[]{0, 1}, new double[]{10, 16}});
        c.put("PETAM:EXAM", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("PETAM:FM", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("PETAM:FT", new double[][]{null});
        c.put("PETAM:GIGAM", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("PETAM:HM", new double[][]{new double[]{0, 1}, new double[]{10, 13}});
        c.put("PETAM:IN", new double[][]{null});
        c.put("PETAM:KM", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("PETAM:LI", new double[][]{null});
        c.put("PETAM:LY", new double[][]{null});
        c.put("PETAM:M", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("PETAM:MEGAM", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("PETAM:MI", new double[][]{null});
        c.put("PETAM:MICROM", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("PETAM:MM", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("PETAM:NM", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("PETAM:PC", new double[][]{null});
        c.put("PETAM:PIXEL", new double[][]{null});
        c.put("PETAM:PM", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("PETAM:PT", new double[][]{null});
        c.put("PETAM:REFERENCEFRAME", new double[][]{null});
        c.put("PETAM:TERAM", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("PETAM:THOU", new double[][]{null});
        c.put("PETAM:UA", new double[][]{null});
        c.put("PETAM:YD", new double[][]{null});
        c.put("PETAM:YM", new double[][]{new double[]{0, 1}, new double[]{10, 39}});
        c.put("PETAM:YOTTAM", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("PETAM:ZETTAM", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("PETAM:ZM", new double[][]{new double[]{0, 1}, new double[]{10, 36}});
        c.put("PIXEL:AM", new double[][]{null});
        c.put("PIXEL:ANGSTROM", new double[][]{null});
        c.put("PIXEL:CM", new double[][]{null});
        c.put("PIXEL:DAM", new double[][]{null});
        c.put("PIXEL:DM", new double[][]{null});
        c.put("PIXEL:EXAM", new double[][]{null});
        c.put("PIXEL:FM", new double[][]{null});
        c.put("PIXEL:FT", new double[][]{null});
        c.put("PIXEL:GIGAM", new double[][]{null});
        c.put("PIXEL:HM", new double[][]{null});
        c.put("PIXEL:IN", new double[][]{null});
        c.put("PIXEL:KM", new double[][]{null});
        c.put("PIXEL:LI", new double[][]{null});
        c.put("PIXEL:LY", new double[][]{null});
        c.put("PIXEL:M", new double[][]{null});
        c.put("PIXEL:MEGAM", new double[][]{null});
        c.put("PIXEL:MI", new double[][]{null});
        c.put("PIXEL:MICROM", new double[][]{null});
        c.put("PIXEL:MM", new double[][]{null});
        c.put("PIXEL:NM", new double[][]{null});
        c.put("PIXEL:PC", new double[][]{null});
        c.put("PIXEL:PETAM", new double[][]{null});
        c.put("PIXEL:PM", new double[][]{null});
        c.put("PIXEL:PT", new double[][]{null});
        c.put("PIXEL:REFERENCEFRAME", new double[][]{null});
        c.put("PIXEL:TERAM", new double[][]{null});
        c.put("PIXEL:THOU", new double[][]{null});
        c.put("PIXEL:UA", new double[][]{null});
        c.put("PIXEL:YD", new double[][]{null});
        c.put("PIXEL:YM", new double[][]{null});
        c.put("PIXEL:YOTTAM", new double[][]{null});
        c.put("PIXEL:ZETTAM", new double[][]{null});
        c.put("PIXEL:ZM", new double[][]{null});
        c.put("PM:AM", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("PM:ANGSTROM", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("PM:CM", new double[][]{new double[]{0, 1}, new double[]{10, -10}});
        c.put("PM:DAM", new double[][]{new double[]{0, 1}, new double[]{10, -13}});
        c.put("PM:DM", new double[][]{new double[]{0, 1}, new double[]{10, -11}});
        c.put("PM:EXAM", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("PM:FM", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("PM:FT", new double[][]{null});
        c.put("PM:GIGAM", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("PM:HM", new double[][]{new double[]{0, 1}, new double[]{10, -14}});
        c.put("PM:IN", new double[][]{null});
        c.put("PM:KM", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("PM:LI", new double[][]{null});
        c.put("PM:LY", new double[][]{null});
        c.put("PM:M", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("PM:MEGAM", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("PM:MI", new double[][]{null});
        c.put("PM:MICROM", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("PM:MM", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("PM:NM", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("PM:PC", new double[][]{null});
        c.put("PM:PETAM", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("PM:PIXEL", new double[][]{null});
        c.put("PM:PT", new double[][]{null});
        c.put("PM:REFERENCEFRAME", new double[][]{null});
        c.put("PM:TERAM", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("PM:THOU", new double[][]{null});
        c.put("PM:UA", new double[][]{null});
        c.put("PM:YD", new double[][]{null});
        c.put("PM:YM", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("PM:YOTTAM", new double[][]{new double[]{0, 1}, new double[]{10, -36}});
        c.put("PM:ZETTAM", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("PM:ZM", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("PT:AM", new double[][]{null});
        c.put("PT:ANGSTROM", new double[][]{null});
        c.put("PT:CM", new double[][]{null});
        c.put("PT:DAM", new double[][]{null});
        c.put("PT:DM", new double[][]{null});
        c.put("PT:EXAM", new double[][]{null});
        c.put("PT:FM", new double[][]{null});
        c.put("PT:FT", new double[][]{null});
        c.put("PT:GIGAM", new double[][]{null});
        c.put("PT:HM", new double[][]{null});
        c.put("PT:IN", new double[][]{null});
        c.put("PT:KM", new double[][]{null});
        c.put("PT:LI", new double[][]{null});
        c.put("PT:LY", new double[][]{null});
        c.put("PT:M", new double[][]{null});
        c.put("PT:MEGAM", new double[][]{null});
        c.put("PT:MI", new double[][]{null});
        c.put("PT:MICROM", new double[][]{null});
        c.put("PT:MM", new double[][]{null});
        c.put("PT:NM", new double[][]{null});
        c.put("PT:PC", new double[][]{null});
        c.put("PT:PETAM", new double[][]{null});
        c.put("PT:PIXEL", new double[][]{null});
        c.put("PT:PM", new double[][]{null});
        c.put("PT:REFERENCEFRAME", new double[][]{null});
        c.put("PT:TERAM", new double[][]{null});
        c.put("PT:THOU", new double[][]{null});
        c.put("PT:UA", new double[][]{null});
        c.put("PT:YD", new double[][]{null});
        c.put("PT:YM", new double[][]{null});
        c.put("PT:YOTTAM", new double[][]{null});
        c.put("PT:ZETTAM", new double[][]{null});
        c.put("PT:ZM", new double[][]{null});
        c.put("REFERENCEFRAME:AM", new double[][]{null});
        c.put("REFERENCEFRAME:ANGSTROM", new double[][]{null});
        c.put("REFERENCEFRAME:CM", new double[][]{null});
        c.put("REFERENCEFRAME:DAM", new double[][]{null});
        c.put("REFERENCEFRAME:DM", new double[][]{null});
        c.put("REFERENCEFRAME:EXAM", new double[][]{null});
        c.put("REFERENCEFRAME:FM", new double[][]{null});
        c.put("REFERENCEFRAME:FT", new double[][]{null});
        c.put("REFERENCEFRAME:GIGAM", new double[][]{null});
        c.put("REFERENCEFRAME:HM", new double[][]{null});
        c.put("REFERENCEFRAME:IN", new double[][]{null});
        c.put("REFERENCEFRAME:KM", new double[][]{null});
        c.put("REFERENCEFRAME:LI", new double[][]{null});
        c.put("REFERENCEFRAME:LY", new double[][]{null});
        c.put("REFERENCEFRAME:M", new double[][]{null});
        c.put("REFERENCEFRAME:MEGAM", new double[][]{null});
        c.put("REFERENCEFRAME:MI", new double[][]{null});
        c.put("REFERENCEFRAME:MICROM", new double[][]{null});
        c.put("REFERENCEFRAME:MM", new double[][]{null});
        c.put("REFERENCEFRAME:NM", new double[][]{null});
        c.put("REFERENCEFRAME:PC", new double[][]{null});
        c.put("REFERENCEFRAME:PETAM", new double[][]{null});
        c.put("REFERENCEFRAME:PIXEL", new double[][]{null});
        c.put("REFERENCEFRAME:PM", new double[][]{null});
        c.put("REFERENCEFRAME:PT", new double[][]{null});
        c.put("REFERENCEFRAME:TERAM", new double[][]{null});
        c.put("REFERENCEFRAME:THOU", new double[][]{null});
        c.put("REFERENCEFRAME:UA", new double[][]{null});
        c.put("REFERENCEFRAME:YD", new double[][]{null});
        c.put("REFERENCEFRAME:YM", new double[][]{null});
        c.put("REFERENCEFRAME:YOTTAM", new double[][]{null});
        c.put("REFERENCEFRAME:ZETTAM", new double[][]{null});
        c.put("REFERENCEFRAME:ZM", new double[][]{null});
        c.put("TERAM:AM", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("TERAM:ANGSTROM", new double[][]{new double[]{0, 1}, new double[]{10, 22}});
        c.put("TERAM:CM", new double[][]{new double[]{0, 1}, new double[]{10, 14}});
        c.put("TERAM:DAM", new double[][]{new double[]{0, 1}, new double[]{10, 11}});
        c.put("TERAM:DM", new double[][]{new double[]{0, 1}, new double[]{10, 13}});
        c.put("TERAM:EXAM", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("TERAM:FM", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("TERAM:FT", new double[][]{null});
        c.put("TERAM:GIGAM", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("TERAM:HM", new double[][]{new double[]{0, 1}, new double[]{10, 10}});
        c.put("TERAM:IN", new double[][]{null});
        c.put("TERAM:KM", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("TERAM:LI", new double[][]{null});
        c.put("TERAM:LY", new double[][]{null});
        c.put("TERAM:M", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("TERAM:MEGAM", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("TERAM:MI", new double[][]{null});
        c.put("TERAM:MICROM", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("TERAM:MM", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("TERAM:NM", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("TERAM:PC", new double[][]{null});
        c.put("TERAM:PETAM", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("TERAM:PIXEL", new double[][]{null});
        c.put("TERAM:PM", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("TERAM:PT", new double[][]{null});
        c.put("TERAM:REFERENCEFRAME", new double[][]{null});
        c.put("TERAM:THOU", new double[][]{null});
        c.put("TERAM:UA", new double[][]{null});
        c.put("TERAM:YD", new double[][]{null});
        c.put("TERAM:YM", new double[][]{new double[]{0, 1}, new double[]{10, 36}});
        c.put("TERAM:YOTTAM", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("TERAM:ZETTAM", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("TERAM:ZM", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("THOU:AM", new double[][]{null});
        c.put("THOU:ANGSTROM", new double[][]{null});
        c.put("THOU:CM", new double[][]{null});
        c.put("THOU:DAM", new double[][]{null});
        c.put("THOU:DM", new double[][]{null});
        c.put("THOU:EXAM", new double[][]{null});
        c.put("THOU:FM", new double[][]{null});
        c.put("THOU:FT", new double[][]{null});
        c.put("THOU:GIGAM", new double[][]{null});
        c.put("THOU:HM", new double[][]{null});
        c.put("THOU:IN", new double[][]{null});
        c.put("THOU:KM", new double[][]{null});
        c.put("THOU:LI", new double[][]{null});
        c.put("THOU:LY", new double[][]{null});
        c.put("THOU:M", new double[][]{null});
        c.put("THOU:MEGAM", new double[][]{null});
        c.put("THOU:MI", new double[][]{null});
        c.put("THOU:MICROM", new double[][]{null});
        c.put("THOU:MM", new double[][]{null});
        c.put("THOU:NM", new double[][]{null});
        c.put("THOU:PC", new double[][]{null});
        c.put("THOU:PETAM", new double[][]{null});
        c.put("THOU:PIXEL", new double[][]{null});
        c.put("THOU:PM", new double[][]{null});
        c.put("THOU:PT", new double[][]{null});
        c.put("THOU:REFERENCEFRAME", new double[][]{null});
        c.put("THOU:TERAM", new double[][]{null});
        c.put("THOU:UA", new double[][]{null});
        c.put("THOU:YD", new double[][]{null});
        c.put("THOU:YM", new double[][]{null});
        c.put("THOU:YOTTAM", new double[][]{null});
        c.put("THOU:ZETTAM", new double[][]{null});
        c.put("THOU:ZM", new double[][]{null});
        c.put("UA:AM", new double[][]{null});
        c.put("UA:ANGSTROM", new double[][]{null});
        c.put("UA:CM", new double[][]{null});
        c.put("UA:DAM", new double[][]{null});
        c.put("UA:DM", new double[][]{null});
        c.put("UA:EXAM", new double[][]{null});
        c.put("UA:FM", new double[][]{null});
        c.put("UA:FT", new double[][]{null});
        c.put("UA:GIGAM", new double[][]{null});
        c.put("UA:HM", new double[][]{null});
        c.put("UA:IN", new double[][]{null});
        c.put("UA:KM", new double[][]{null});
        c.put("UA:LI", new double[][]{null});
        c.put("UA:LY", new double[][]{null});
        c.put("UA:M", new double[][]{null});
        c.put("UA:MEGAM", new double[][]{null});
        c.put("UA:MI", new double[][]{null});
        c.put("UA:MICROM", new double[][]{null});
        c.put("UA:MM", new double[][]{null});
        c.put("UA:NM", new double[][]{null});
        c.put("UA:PC", new double[][]{null});
        c.put("UA:PETAM", new double[][]{null});
        c.put("UA:PIXEL", new double[][]{null});
        c.put("UA:PM", new double[][]{null});
        c.put("UA:PT", new double[][]{null});
        c.put("UA:REFERENCEFRAME", new double[][]{null});
        c.put("UA:TERAM", new double[][]{null});
        c.put("UA:THOU", new double[][]{null});
        c.put("UA:YD", new double[][]{null});
        c.put("UA:YM", new double[][]{null});
        c.put("UA:YOTTAM", new double[][]{null});
        c.put("UA:ZETTAM", new double[][]{null});
        c.put("UA:ZM", new double[][]{null});
        c.put("YD:AM", new double[][]{null});
        c.put("YD:ANGSTROM", new double[][]{null});
        c.put("YD:CM", new double[][]{null});
        c.put("YD:DAM", new double[][]{null});
        c.put("YD:DM", new double[][]{null});
        c.put("YD:EXAM", new double[][]{null});
        c.put("YD:FM", new double[][]{null});
        c.put("YD:FT", new double[][]{null});
        c.put("YD:GIGAM", new double[][]{null});
        c.put("YD:HM", new double[][]{null});
        c.put("YD:IN", new double[][]{null});
        c.put("YD:KM", new double[][]{null});
        c.put("YD:LI", new double[][]{null});
        c.put("YD:LY", new double[][]{null});
        c.put("YD:M", new double[][]{null});
        c.put("YD:MEGAM", new double[][]{null});
        c.put("YD:MI", new double[][]{null});
        c.put("YD:MICROM", new double[][]{null});
        c.put("YD:MM", new double[][]{null});
        c.put("YD:NM", new double[][]{null});
        c.put("YD:PC", new double[][]{null});
        c.put("YD:PETAM", new double[][]{null});
        c.put("YD:PIXEL", new double[][]{null});
        c.put("YD:PM", new double[][]{null});
        c.put("YD:PT", new double[][]{null});
        c.put("YD:REFERENCEFRAME", new double[][]{null});
        c.put("YD:TERAM", new double[][]{null});
        c.put("YD:THOU", new double[][]{null});
        c.put("YD:UA", new double[][]{null});
        c.put("YD:YM", new double[][]{null});
        c.put("YD:YOTTAM", new double[][]{null});
        c.put("YD:ZETTAM", new double[][]{null});
        c.put("YD:ZM", new double[][]{null});
        c.put("YM:AM", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("YM:ANGSTROM", new double[][]{new double[]{0, 1}, new double[]{10, -14}});
        c.put("YM:CM", new double[][]{new double[]{0, 1}, new double[]{10, -22}});
        c.put("YM:DAM", new double[][]{new double[]{0, 1}, new double[]{10, -25}});
        c.put("YM:DM", new double[][]{new double[]{0, 1}, new double[]{10, -23}});
        c.put("YM:EXAM", new double[][]{new double[]{0, 1}, new double[]{10, -42}});
        c.put("YM:FM", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("YM:FT", new double[][]{null});
        c.put("YM:GIGAM", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("YM:HM", new double[][]{new double[]{0, 1}, new double[]{10, -26}});
        c.put("YM:IN", new double[][]{null});
        c.put("YM:KM", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("YM:LI", new double[][]{null});
        c.put("YM:LY", new double[][]{null});
        c.put("YM:M", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("YM:MEGAM", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("YM:MI", new double[][]{null});
        c.put("YM:MICROM", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("YM:MM", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("YM:NM", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("YM:PC", new double[][]{null});
        c.put("YM:PETAM", new double[][]{new double[]{0, 1}, new double[]{10, -39}});
        c.put("YM:PIXEL", new double[][]{null});
        c.put("YM:PM", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("YM:PT", new double[][]{null});
        c.put("YM:REFERENCEFRAME", new double[][]{null});
        c.put("YM:TERAM", new double[][]{new double[]{0, 1}, new double[]{10, -36}});
        c.put("YM:THOU", new double[][]{null});
        c.put("YM:UA", new double[][]{null});
        c.put("YM:YD", new double[][]{null});
        c.put("YM:YOTTAM", new double[][]{new double[]{0, 1}, new double[]{10, -48}});
        c.put("YM:ZETTAM", new double[][]{new double[]{0, 1}, new double[]{10, -45}});
        c.put("YM:ZM", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("YOTTAM:AM", new double[][]{new double[]{0, 1}, new double[]{10, 42}});
        c.put("YOTTAM:ANGSTROM", new double[][]{new double[]{0, 1}, new double[]{10, 34}});
        c.put("YOTTAM:CM", new double[][]{new double[]{0, 1}, new double[]{10, 26}});
        c.put("YOTTAM:DAM", new double[][]{new double[]{0, 1}, new double[]{10, 23}});
        c.put("YOTTAM:DM", new double[][]{new double[]{0, 1}, new double[]{10, 25}});
        c.put("YOTTAM:EXAM", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("YOTTAM:FM", new double[][]{new double[]{0, 1}, new double[]{10, 39}});
        c.put("YOTTAM:FT", new double[][]{null});
        c.put("YOTTAM:GIGAM", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("YOTTAM:HM", new double[][]{new double[]{0, 1}, new double[]{10, 22}});
        c.put("YOTTAM:IN", new double[][]{null});
        c.put("YOTTAM:KM", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("YOTTAM:LI", new double[][]{null});
        c.put("YOTTAM:LY", new double[][]{null});
        c.put("YOTTAM:M", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("YOTTAM:MEGAM", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("YOTTAM:MI", new double[][]{null});
        c.put("YOTTAM:MICROM", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("YOTTAM:MM", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("YOTTAM:NM", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("YOTTAM:PC", new double[][]{null});
        c.put("YOTTAM:PETAM", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("YOTTAM:PIXEL", new double[][]{null});
        c.put("YOTTAM:PM", new double[][]{new double[]{0, 1}, new double[]{10, 36}});
        c.put("YOTTAM:PT", new double[][]{null});
        c.put("YOTTAM:REFERENCEFRAME", new double[][]{null});
        c.put("YOTTAM:TERAM", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("YOTTAM:THOU", new double[][]{null});
        c.put("YOTTAM:UA", new double[][]{null});
        c.put("YOTTAM:YD", new double[][]{null});
        c.put("YOTTAM:YM", new double[][]{new double[]{0, 1}, new double[]{10, 48}});
        c.put("YOTTAM:ZETTAM", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("YOTTAM:ZM", new double[][]{new double[]{0, 1}, new double[]{10, 45}});
        c.put("ZETTAM:AM", new double[][]{new double[]{0, 1}, new double[]{10, 39}});
        c.put("ZETTAM:ANGSTROM", new double[][]{new double[]{0, 1}, new double[]{10, 31}});
        c.put("ZETTAM:CM", new double[][]{new double[]{0, 1}, new double[]{10, 23}});
        c.put("ZETTAM:DAM", new double[][]{new double[]{0, 1}, new double[]{10, 20}});
        c.put("ZETTAM:DM", new double[][]{new double[]{0, 1}, new double[]{10, 22}});
        c.put("ZETTAM:EXAM", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("ZETTAM:FM", new double[][]{new double[]{0, 1}, new double[]{10, 36}});
        c.put("ZETTAM:FT", new double[][]{null});
        c.put("ZETTAM:GIGAM", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("ZETTAM:HM", new double[][]{new double[]{0, 1}, new double[]{10, 19}});
        c.put("ZETTAM:IN", new double[][]{null});
        c.put("ZETTAM:KM", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("ZETTAM:LI", new double[][]{null});
        c.put("ZETTAM:LY", new double[][]{null});
        c.put("ZETTAM:M", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("ZETTAM:MEGAM", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("ZETTAM:MI", new double[][]{null});
        c.put("ZETTAM:MICROM", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("ZETTAM:MM", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("ZETTAM:NM", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("ZETTAM:PC", new double[][]{null});
        c.put("ZETTAM:PETAM", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("ZETTAM:PIXEL", new double[][]{null});
        c.put("ZETTAM:PM", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("ZETTAM:PT", new double[][]{null});
        c.put("ZETTAM:REFERENCEFRAME", new double[][]{null});
        c.put("ZETTAM:TERAM", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("ZETTAM:THOU", new double[][]{null});
        c.put("ZETTAM:UA", new double[][]{null});
        c.put("ZETTAM:YD", new double[][]{null});
        c.put("ZETTAM:YM", new double[][]{new double[]{0, 1}, new double[]{10, 45}});
        c.put("ZETTAM:YOTTAM", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("ZETTAM:ZM", new double[][]{new double[]{0, 1}, new double[]{10, 42}});
        c.put("ZM:AM", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("ZM:ANGSTROM", new double[][]{new double[]{0, 1}, new double[]{10, -11}});
        c.put("ZM:CM", new double[][]{new double[]{0, 1}, new double[]{10, -19}});
        c.put("ZM:DAM", new double[][]{new double[]{0, 1}, new double[]{10, -22}});
        c.put("ZM:DM", new double[][]{new double[]{0, 1}, new double[]{10, -20}});
        c.put("ZM:EXAM", new double[][]{new double[]{0, 1}, new double[]{10, -39}});
        c.put("ZM:FM", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("ZM:FT", new double[][]{null});
        c.put("ZM:GIGAM", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("ZM:HM", new double[][]{new double[]{0, 1}, new double[]{10, -23}});
        c.put("ZM:IN", new double[][]{null});
        c.put("ZM:KM", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("ZM:LI", new double[][]{null});
        c.put("ZM:LY", new double[][]{null});
        c.put("ZM:M", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("ZM:MEGAM", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("ZM:MI", new double[][]{null});
        c.put("ZM:MICROM", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("ZM:MM", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("ZM:NM", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("ZM:PC", new double[][]{null});
        c.put("ZM:PETAM", new double[][]{new double[]{0, 1}, new double[]{10, -36}});
        c.put("ZM:PIXEL", new double[][]{null});
        c.put("ZM:PM", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("ZM:PT", new double[][]{null});
        c.put("ZM:REFERENCEFRAME", new double[][]{null});
        c.put("ZM:TERAM", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("ZM:THOU", new double[][]{null});
        c.put("ZM:UA", new double[][]{null});
        c.put("ZM:YD", new double[][]{null});
        c.put("ZM:YM", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("ZM:YOTTAM", new double[][]{new double[]{0, 1}, new double[]{10, -45}});
        c.put("ZM:ZETTAM", new double[][]{new double[]{0, 1}, new double[]{10, -42}});
        conversions = Collections.unmodifiableMap(c);
    }

    private static final Map<UnitsLength, String> SYMBOLS;
    static {
        Map<UnitsLength, String> s = new HashMap<UnitsLength, String>();
        s.put(UnitsLength.AM, "am");
        s.put(UnitsLength.ANGSTROM, "Å");
        s.put(UnitsLength.CM, "cm");
        s.put(UnitsLength.DAM, "dam");
        s.put(UnitsLength.DM, "dm");
        s.put(UnitsLength.EXAM, "Em");
        s.put(UnitsLength.FM, "fm");
        s.put(UnitsLength.FOOT, "ft");
        s.put(UnitsLength.GIGAM, "Gm");
        s.put(UnitsLength.HM, "hm");
        s.put(UnitsLength.INCH, "in");
        s.put(UnitsLength.KM, "km");
        s.put(UnitsLength.LINE, "li");
        s.put(UnitsLength.LY, "ly");
        s.put(UnitsLength.M, "m");
        s.put(UnitsLength.MEGAM, "Mm");
        s.put(UnitsLength.MICROM, "µm");
        s.put(UnitsLength.MILE, "mi");
        s.put(UnitsLength.MM, "mm");
        s.put(UnitsLength.NM, "nm");
        s.put(UnitsLength.PC, "pc");
        s.put(UnitsLength.PETAM, "Pm");
        s.put(UnitsLength.PIXEL, "pixel");
        s.put(UnitsLength.PM, "pm");
        s.put(UnitsLength.POINT, "pt");
        s.put(UnitsLength.REFERENCEFRAME, "reference frame");
        s.put(UnitsLength.TERAM, "Tm");
        s.put(UnitsLength.THOU, "thou");
        s.put(UnitsLength.UA, "ua");
        s.put(UnitsLength.YARD, "yd");
        s.put(UnitsLength.YM, "ym");
        s.put(UnitsLength.YOTTAM, "Ym");
        s.put(UnitsLength.ZETTAM, "Zm");
        s.put(UnitsLength.ZM, "zm");
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

