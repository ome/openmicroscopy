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
        c.put("AM:GIGAM", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("AM:HM", new double[][]{new double[]{0, 1}, new double[]{10, -20}});
        c.put("AM:KM", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("AM:M", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("AM:MEGAM", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("AM:MICROM", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("AM:MM", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("AM:NM", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("AM:PETAM", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("AM:PM", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("AM:TERAM", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
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
        c.put("ANGSTROM:GIGAM", new double[][]{new double[]{0, 1}, new double[]{10, -19}});
        c.put("ANGSTROM:HM", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("ANGSTROM:KM", new double[][]{new double[]{0, 1}, new double[]{10, -13}});
        c.put("ANGSTROM:M", new double[][]{new double[]{0, 1}, new double[]{10, -10}});
        c.put("ANGSTROM:MEGAM", new double[][]{new double[]{0, 1}, new double[]{10, -16}});
        c.put("ANGSTROM:MICROM", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("ANGSTROM:MM", new double[][]{new double[]{0, 1}, new double[]{10, -7}});
        c.put("ANGSTROM:NM", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("ANGSTROM:PETAM", new double[][]{new double[]{0, 1}, new double[]{10, -25}});
        c.put("ANGSTROM:PM", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("ANGSTROM:TERAM", new double[][]{new double[]{0, 1}, new double[]{10, -22}});
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
        c.put("CM:GIGAM", new double[][]{new double[]{0, 1}, new double[]{10, -11}});
        c.put("CM:HM", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("CM:KM", new double[][]{new double[]{0, 1}, new double[]{10, -5}});
        c.put("CM:M", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("CM:MEGAM", new double[][]{new double[]{0, 1}, new double[]{10, -8}});
        c.put("CM:MICROM", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("CM:MM", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("CM:NM", new double[][]{new double[]{0, 1}, new double[]{10, 7}});
        c.put("CM:PETAM", new double[][]{new double[]{0, 1}, new double[]{10, -17}});
        c.put("CM:PM", new double[][]{new double[]{0, 1}, new double[]{10, 10}});
        c.put("CM:TERAM", new double[][]{new double[]{0, 1}, new double[]{10, -14}});
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
        c.put("DAM:GIGAM", new double[][]{new double[]{0, 1}, new double[]{10, -8}});
        c.put("DAM:HM", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("DAM:KM", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("DAM:M", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("DAM:MEGAM", new double[][]{new double[]{0, 1}, new double[]{10, -5}});
        c.put("DAM:MICROM", new double[][]{new double[]{0, 1}, new double[]{10, 7}});
        c.put("DAM:MM", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("DAM:NM", new double[][]{new double[]{0, 1}, new double[]{10, 10}});
        c.put("DAM:PETAM", new double[][]{new double[]{0, 1}, new double[]{10, -14}});
        c.put("DAM:PM", new double[][]{new double[]{0, 1}, new double[]{10, 13}});
        c.put("DAM:TERAM", new double[][]{new double[]{0, 1}, new double[]{10, -11}});
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
        c.put("DM:GIGAM", new double[][]{new double[]{0, 1}, new double[]{10, -10}});
        c.put("DM:HM", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("DM:KM", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("DM:M", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("DM:MEGAM", new double[][]{new double[]{0, 1}, new double[]{10, -7}});
        c.put("DM:MICROM", new double[][]{new double[]{0, 1}, new double[]{10, 5}});
        c.put("DM:MM", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("DM:NM", new double[][]{new double[]{0, 1}, new double[]{10, 8}});
        c.put("DM:PETAM", new double[][]{new double[]{0, 1}, new double[]{10, -16}});
        c.put("DM:PM", new double[][]{new double[]{0, 1}, new double[]{10, 11}});
        c.put("DM:TERAM", new double[][]{new double[]{0, 1}, new double[]{10, -13}});
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
        c.put("EXAM:GIGAM", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("EXAM:HM", new double[][]{new double[]{0, 1}, new double[]{10, 16}});
        c.put("EXAM:KM", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("EXAM:M", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("EXAM:MEGAM", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("EXAM:MICROM", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("EXAM:MM", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("EXAM:NM", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("EXAM:PETAM", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("EXAM:PM", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("EXAM:TERAM", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
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
        c.put("FM:GIGAM", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("FM:HM", new double[][]{new double[]{0, 1}, new double[]{10, -17}});
        c.put("FM:KM", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("FM:M", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("FM:MEGAM", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("FM:MICROM", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("FM:MM", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("FM:NM", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("FM:PETAM", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("FM:PM", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("FM:TERAM", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("FM:YM", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("FM:YOTTAM", new double[][]{new double[]{0, 1}, new double[]{10, -39}});
        c.put("FM:ZETTAM", new double[][]{new double[]{0, 1}, new double[]{10, -36}});
        c.put("FM:ZM", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("GIGAM:AM", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("GIGAM:ANGSTROM", new double[][]{new double[]{0, 1}, new double[]{10, 19}});
        c.put("GIGAM:CM", new double[][]{new double[]{0, 1}, new double[]{10, 11}});
        c.put("GIGAM:DAM", new double[][]{new double[]{0, 1}, new double[]{10, 8}});
        c.put("GIGAM:DM", new double[][]{new double[]{0, 1}, new double[]{10, 10}});
        c.put("GIGAM:EXAM", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("GIGAM:FM", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("GIGAM:HM", new double[][]{new double[]{0, 1}, new double[]{10, 7}});
        c.put("GIGAM:KM", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("GIGAM:M", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("GIGAM:MEGAM", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("GIGAM:MICROM", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("GIGAM:MM", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("GIGAM:NM", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("GIGAM:PETAM", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("GIGAM:PM", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("GIGAM:TERAM", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
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
        c.put("HM:GIGAM", new double[][]{new double[]{0, 1}, new double[]{10, -7}});
        c.put("HM:KM", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("HM:M", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("HM:MEGAM", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("HM:MICROM", new double[][]{new double[]{0, 1}, new double[]{10, 8}});
        c.put("HM:MM", new double[][]{new double[]{0, 1}, new double[]{10, 5}});
        c.put("HM:NM", new double[][]{new double[]{0, 1}, new double[]{10, 11}});
        c.put("HM:PETAM", new double[][]{new double[]{0, 1}, new double[]{10, -13}});
        c.put("HM:PM", new double[][]{new double[]{0, 1}, new double[]{10, 14}});
        c.put("HM:TERAM", new double[][]{new double[]{0, 1}, new double[]{10, -10}});
        c.put("HM:YM", new double[][]{new double[]{0, 1}, new double[]{10, 26}});
        c.put("HM:YOTTAM", new double[][]{new double[]{0, 1}, new double[]{10, -22}});
        c.put("HM:ZETTAM", new double[][]{new double[]{0, 1}, new double[]{10, -19}});
        c.put("HM:ZM", new double[][]{new double[]{0, 1}, new double[]{10, 23}});
        c.put("KM:AM", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("KM:ANGSTROM", new double[][]{new double[]{0, 1}, new double[]{10, 13}});
        c.put("KM:CM", new double[][]{new double[]{0, 1}, new double[]{10, 5}});
        c.put("KM:DAM", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("KM:DM", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("KM:EXAM", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("KM:FM", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("KM:GIGAM", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("KM:HM", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("KM:M", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("KM:MEGAM", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("KM:MICROM", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("KM:MM", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("KM:NM", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("KM:PETAM", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("KM:PM", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("KM:TERAM", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("KM:YM", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("KM:YOTTAM", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("KM:ZETTAM", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("KM:ZM", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("M:AM", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("M:ANGSTROM", new double[][]{new double[]{0, 1}, new double[]{10, 10}});
        c.put("M:CM", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("M:DAM", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("M:DM", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("M:EXAM", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("M:FM", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("M:GIGAM", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("M:HM", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("M:KM", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("M:MEGAM", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("M:MICROM", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("M:MM", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("M:NM", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("M:PETAM", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("M:PM", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("M:TERAM", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
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
        c.put("MEGAM:GIGAM", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("MEGAM:HM", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("MEGAM:KM", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("MEGAM:M", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("MEGAM:MICROM", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("MEGAM:MM", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("MEGAM:NM", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("MEGAM:PETAM", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("MEGAM:PM", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("MEGAM:TERAM", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("MEGAM:YM", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("MEGAM:YOTTAM", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("MEGAM:ZETTAM", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("MEGAM:ZM", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("MICROM:AM", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("MICROM:ANGSTROM", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("MICROM:CM", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("MICROM:DAM", new double[][]{new double[]{0, 1}, new double[]{10, -7}});
        c.put("MICROM:DM", new double[][]{new double[]{0, 1}, new double[]{10, -5}});
        c.put("MICROM:EXAM", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("MICROM:FM", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("MICROM:GIGAM", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("MICROM:HM", new double[][]{new double[]{0, 1}, new double[]{10, -8}});
        c.put("MICROM:KM", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("MICROM:M", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("MICROM:MEGAM", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("MICROM:MM", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("MICROM:NM", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("MICROM:PETAM", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("MICROM:PM", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("MICROM:TERAM", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
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
        c.put("MM:GIGAM", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("MM:HM", new double[][]{new double[]{0, 1}, new double[]{10, -5}});
        c.put("MM:KM", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("MM:M", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("MM:MEGAM", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("MM:MICROM", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("MM:NM", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("MM:PETAM", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("MM:PM", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("MM:TERAM", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
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
        c.put("NM:GIGAM", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("NM:HM", new double[][]{new double[]{0, 1}, new double[]{10, -11}});
        c.put("NM:KM", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("NM:M", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("NM:MEGAM", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("NM:MICROM", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("NM:MM", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("NM:PETAM", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("NM:PM", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("NM:TERAM", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("NM:YM", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("NM:YOTTAM", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("NM:ZETTAM", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("NM:ZM", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("PETAM:AM", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("PETAM:ANGSTROM", new double[][]{new double[]{0, 1}, new double[]{10, 25}});
        c.put("PETAM:CM", new double[][]{new double[]{0, 1}, new double[]{10, 17}});
        c.put("PETAM:DAM", new double[][]{new double[]{0, 1}, new double[]{10, 14}});
        c.put("PETAM:DM", new double[][]{new double[]{0, 1}, new double[]{10, 16}});
        c.put("PETAM:EXAM", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("PETAM:FM", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("PETAM:GIGAM", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("PETAM:HM", new double[][]{new double[]{0, 1}, new double[]{10, 13}});
        c.put("PETAM:KM", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("PETAM:M", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("PETAM:MEGAM", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("PETAM:MICROM", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("PETAM:MM", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("PETAM:NM", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("PETAM:PM", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("PETAM:TERAM", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("PETAM:YM", new double[][]{new double[]{0, 1}, new double[]{10, 39}});
        c.put("PETAM:YOTTAM", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("PETAM:ZETTAM", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("PETAM:ZM", new double[][]{new double[]{0, 1}, new double[]{10, 36}});
        c.put("PM:AM", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("PM:ANGSTROM", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("PM:CM", new double[][]{new double[]{0, 1}, new double[]{10, -10}});
        c.put("PM:DAM", new double[][]{new double[]{0, 1}, new double[]{10, -13}});
        c.put("PM:DM", new double[][]{new double[]{0, 1}, new double[]{10, -11}});
        c.put("PM:EXAM", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("PM:FM", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("PM:GIGAM", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("PM:HM", new double[][]{new double[]{0, 1}, new double[]{10, -14}});
        c.put("PM:KM", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("PM:M", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("PM:MEGAM", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("PM:MICROM", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("PM:MM", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("PM:NM", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("PM:PETAM", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("PM:TERAM", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("PM:YM", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("PM:YOTTAM", new double[][]{new double[]{0, 1}, new double[]{10, -36}});
        c.put("PM:ZETTAM", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("PM:ZM", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("TERAM:AM", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("TERAM:ANGSTROM", new double[][]{new double[]{0, 1}, new double[]{10, 22}});
        c.put("TERAM:CM", new double[][]{new double[]{0, 1}, new double[]{10, 14}});
        c.put("TERAM:DAM", new double[][]{new double[]{0, 1}, new double[]{10, 11}});
        c.put("TERAM:DM", new double[][]{new double[]{0, 1}, new double[]{10, 13}});
        c.put("TERAM:EXAM", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("TERAM:FM", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("TERAM:GIGAM", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("TERAM:HM", new double[][]{new double[]{0, 1}, new double[]{10, 10}});
        c.put("TERAM:KM", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("TERAM:M", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("TERAM:MEGAM", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("TERAM:MICROM", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("TERAM:MM", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("TERAM:NM", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("TERAM:PETAM", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("TERAM:PM", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("TERAM:YM", new double[][]{new double[]{0, 1}, new double[]{10, 36}});
        c.put("TERAM:YOTTAM", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("TERAM:ZETTAM", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("TERAM:ZM", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("YM:AM", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("YM:ANGSTROM", new double[][]{new double[]{0, 1}, new double[]{10, -14}});
        c.put("YM:CM", new double[][]{new double[]{0, 1}, new double[]{10, -22}});
        c.put("YM:DAM", new double[][]{new double[]{0, 1}, new double[]{10, -25}});
        c.put("YM:DM", new double[][]{new double[]{0, 1}, new double[]{10, -23}});
        c.put("YM:EXAM", new double[][]{new double[]{0, 1}, new double[]{10, -42}});
        c.put("YM:FM", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("YM:GIGAM", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("YM:HM", new double[][]{new double[]{0, 1}, new double[]{10, -26}});
        c.put("YM:KM", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("YM:M", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("YM:MEGAM", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("YM:MICROM", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("YM:MM", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("YM:NM", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("YM:PETAM", new double[][]{new double[]{0, 1}, new double[]{10, -39}});
        c.put("YM:PM", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("YM:TERAM", new double[][]{new double[]{0, 1}, new double[]{10, -36}});
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
        c.put("YOTTAM:GIGAM", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("YOTTAM:HM", new double[][]{new double[]{0, 1}, new double[]{10, 22}});
        c.put("YOTTAM:KM", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("YOTTAM:M", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("YOTTAM:MEGAM", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("YOTTAM:MICROM", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("YOTTAM:MM", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("YOTTAM:NM", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("YOTTAM:PETAM", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("YOTTAM:PM", new double[][]{new double[]{0, 1}, new double[]{10, 36}});
        c.put("YOTTAM:TERAM", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
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
        c.put("ZETTAM:GIGAM", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("ZETTAM:HM", new double[][]{new double[]{0, 1}, new double[]{10, 19}});
        c.put("ZETTAM:KM", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("ZETTAM:M", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("ZETTAM:MEGAM", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("ZETTAM:MICROM", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("ZETTAM:MM", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("ZETTAM:NM", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("ZETTAM:PETAM", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("ZETTAM:PM", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("ZETTAM:TERAM", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
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
        c.put("ZM:GIGAM", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("ZM:HM", new double[][]{new double[]{0, 1}, new double[]{10, -23}});
        c.put("ZM:KM", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("ZM:M", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("ZM:MEGAM", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("ZM:MICROM", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("ZM:MM", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("ZM:NM", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("ZM:PETAM", new double[][]{new double[]{0, 1}, new double[]{10, -36}});
        c.put("ZM:PM", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("ZM:TERAM", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
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

