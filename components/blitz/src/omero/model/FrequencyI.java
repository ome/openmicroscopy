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

        c.put("AHZ:CHZ", new double[][]{new double[]{0, 1}, new double[]{10, -16}});
        c.put("AHZ:DAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -19}});
        c.put("AHZ:DHZ", new double[][]{new double[]{0, 1}, new double[]{10, -17}});
        c.put("AHZ:EXAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -36}});
        c.put("AHZ:FHZ", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("AHZ:GIGAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("AHZ:HHZ", new double[][]{new double[]{0, 1}, new double[]{10, -20}});
        c.put("AHZ:HZ", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("AHZ:KHZ", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("AHZ:MEGAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("AHZ:MHZ", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("AHZ:MICROHZ", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("AHZ:NHZ", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("AHZ:PETAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("AHZ:PHZ", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("AHZ:TERAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("AHZ:YHZ", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("AHZ:YOTTAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -42}});
        c.put("AHZ:ZETTAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -39}});
        c.put("AHZ:ZHZ", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("CHZ:AHZ", new double[][]{new double[]{0, 1}, new double[]{10, 16}});
        c.put("CHZ:DAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("CHZ:DHZ", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("CHZ:EXAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -20}});
        c.put("CHZ:FHZ", new double[][]{new double[]{0, 1}, new double[]{10, 13}});
        c.put("CHZ:GIGAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -11}});
        c.put("CHZ:HHZ", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("CHZ:HZ", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("CHZ:KHZ", new double[][]{new double[]{0, 1}, new double[]{10, -5}});
        c.put("CHZ:MEGAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -8}});
        c.put("CHZ:MHZ", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("CHZ:MICROHZ", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("CHZ:NHZ", new double[][]{new double[]{0, 1}, new double[]{10, 7}});
        c.put("CHZ:PETAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -17}});
        c.put("CHZ:PHZ", new double[][]{new double[]{0, 1}, new double[]{10, 10}});
        c.put("CHZ:TERAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -14}});
        c.put("CHZ:YHZ", new double[][]{new double[]{0, 1}, new double[]{10, 22}});
        c.put("CHZ:YOTTAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -26}});
        c.put("CHZ:ZETTAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -23}});
        c.put("CHZ:ZHZ", new double[][]{new double[]{0, 1}, new double[]{10, 19}});
        c.put("DAHZ:AHZ", new double[][]{new double[]{0, 1}, new double[]{10, 19}});
        c.put("DAHZ:CHZ", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("DAHZ:DHZ", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("DAHZ:EXAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -17}});
        c.put("DAHZ:FHZ", new double[][]{new double[]{0, 1}, new double[]{10, 16}});
        c.put("DAHZ:GIGAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -8}});
        c.put("DAHZ:HHZ", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("DAHZ:HZ", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("DAHZ:KHZ", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("DAHZ:MEGAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -5}});
        c.put("DAHZ:MHZ", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("DAHZ:MICROHZ", new double[][]{new double[]{0, 1}, new double[]{10, 7}});
        c.put("DAHZ:NHZ", new double[][]{new double[]{0, 1}, new double[]{10, 10}});
        c.put("DAHZ:PETAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -14}});
        c.put("DAHZ:PHZ", new double[][]{new double[]{0, 1}, new double[]{10, 13}});
        c.put("DAHZ:TERAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -11}});
        c.put("DAHZ:YHZ", new double[][]{new double[]{0, 1}, new double[]{10, 25}});
        c.put("DAHZ:YOTTAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -23}});
        c.put("DAHZ:ZETTAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -20}});
        c.put("DAHZ:ZHZ", new double[][]{new double[]{0, 1}, new double[]{10, 22}});
        c.put("DHZ:AHZ", new double[][]{new double[]{0, 1}, new double[]{10, 17}});
        c.put("DHZ:CHZ", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("DHZ:DAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("DHZ:EXAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -19}});
        c.put("DHZ:FHZ", new double[][]{new double[]{0, 1}, new double[]{10, 14}});
        c.put("DHZ:GIGAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -10}});
        c.put("DHZ:HHZ", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("DHZ:HZ", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("DHZ:KHZ", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("DHZ:MEGAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -7}});
        c.put("DHZ:MHZ", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("DHZ:MICROHZ", new double[][]{new double[]{0, 1}, new double[]{10, 5}});
        c.put("DHZ:NHZ", new double[][]{new double[]{0, 1}, new double[]{10, 8}});
        c.put("DHZ:PETAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -16}});
        c.put("DHZ:PHZ", new double[][]{new double[]{0, 1}, new double[]{10, 11}});
        c.put("DHZ:TERAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -13}});
        c.put("DHZ:YHZ", new double[][]{new double[]{0, 1}, new double[]{10, 23}});
        c.put("DHZ:YOTTAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -25}});
        c.put("DHZ:ZETTAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -22}});
        c.put("DHZ:ZHZ", new double[][]{new double[]{0, 1}, new double[]{10, 20}});
        c.put("EXAHZ:AHZ", new double[][]{new double[]{0, 1}, new double[]{10, 36}});
        c.put("EXAHZ:CHZ", new double[][]{new double[]{0, 1}, new double[]{10, 20}});
        c.put("EXAHZ:DAHZ", new double[][]{new double[]{0, 1}, new double[]{10, 17}});
        c.put("EXAHZ:DHZ", new double[][]{new double[]{0, 1}, new double[]{10, 19}});
        c.put("EXAHZ:FHZ", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("EXAHZ:GIGAHZ", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("EXAHZ:HHZ", new double[][]{new double[]{0, 1}, new double[]{10, 16}});
        c.put("EXAHZ:HZ", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("EXAHZ:KHZ", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("EXAHZ:MEGAHZ", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("EXAHZ:MHZ", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("EXAHZ:MICROHZ", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("EXAHZ:NHZ", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("EXAHZ:PETAHZ", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("EXAHZ:PHZ", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("EXAHZ:TERAHZ", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("EXAHZ:YHZ", new double[][]{new double[]{0, 1}, new double[]{10, 42}});
        c.put("EXAHZ:YOTTAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("EXAHZ:ZETTAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("EXAHZ:ZHZ", new double[][]{new double[]{0, 1}, new double[]{10, 39}});
        c.put("FHZ:AHZ", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("FHZ:CHZ", new double[][]{new double[]{0, 1}, new double[]{10, -13}});
        c.put("FHZ:DAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -16}});
        c.put("FHZ:DHZ", new double[][]{new double[]{0, 1}, new double[]{10, -14}});
        c.put("FHZ:EXAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("FHZ:GIGAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("FHZ:HHZ", new double[][]{new double[]{0, 1}, new double[]{10, -17}});
        c.put("FHZ:HZ", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("FHZ:KHZ", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("FHZ:MEGAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("FHZ:MHZ", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("FHZ:MICROHZ", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("FHZ:NHZ", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("FHZ:PETAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("FHZ:PHZ", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("FHZ:TERAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("FHZ:YHZ", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("FHZ:YOTTAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -39}});
        c.put("FHZ:ZETTAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -36}});
        c.put("FHZ:ZHZ", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("GIGAHZ:AHZ", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("GIGAHZ:CHZ", new double[][]{new double[]{0, 1}, new double[]{10, 11}});
        c.put("GIGAHZ:DAHZ", new double[][]{new double[]{0, 1}, new double[]{10, 8}});
        c.put("GIGAHZ:DHZ", new double[][]{new double[]{0, 1}, new double[]{10, 10}});
        c.put("GIGAHZ:EXAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("GIGAHZ:FHZ", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("GIGAHZ:HHZ", new double[][]{new double[]{0, 1}, new double[]{10, 7}});
        c.put("GIGAHZ:HZ", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("GIGAHZ:KHZ", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("GIGAHZ:MEGAHZ", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("GIGAHZ:MHZ", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("GIGAHZ:MICROHZ", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("GIGAHZ:NHZ", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("GIGAHZ:PETAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("GIGAHZ:PHZ", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("GIGAHZ:TERAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("GIGAHZ:YHZ", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("GIGAHZ:YOTTAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("GIGAHZ:ZETTAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("GIGAHZ:ZHZ", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("HHZ:AHZ", new double[][]{new double[]{0, 1}, new double[]{10, 20}});
        c.put("HHZ:CHZ", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("HHZ:DAHZ", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("HHZ:DHZ", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("HHZ:EXAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -16}});
        c.put("HHZ:FHZ", new double[][]{new double[]{0, 1}, new double[]{10, 17}});
        c.put("HHZ:GIGAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -7}});
        c.put("HHZ:HZ", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("HHZ:KHZ", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("HHZ:MEGAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("HHZ:MHZ", new double[][]{new double[]{0, 1}, new double[]{10, 5}});
        c.put("HHZ:MICROHZ", new double[][]{new double[]{0, 1}, new double[]{10, 8}});
        c.put("HHZ:NHZ", new double[][]{new double[]{0, 1}, new double[]{10, 11}});
        c.put("HHZ:PETAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -13}});
        c.put("HHZ:PHZ", new double[][]{new double[]{0, 1}, new double[]{10, 14}});
        c.put("HHZ:TERAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -10}});
        c.put("HHZ:YHZ", new double[][]{new double[]{0, 1}, new double[]{10, 26}});
        c.put("HHZ:YOTTAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -22}});
        c.put("HHZ:ZETTAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -19}});
        c.put("HHZ:ZHZ", new double[][]{new double[]{0, 1}, new double[]{10, 23}});
        c.put("HZ:AHZ", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("HZ:CHZ", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("HZ:DAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("HZ:DHZ", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("HZ:EXAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("HZ:FHZ", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("HZ:GIGAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("HZ:HHZ", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("HZ:KHZ", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("HZ:MEGAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("HZ:MHZ", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("HZ:MICROHZ", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("HZ:NHZ", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("HZ:PETAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("HZ:PHZ", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("HZ:TERAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("HZ:YHZ", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("HZ:YOTTAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("HZ:ZETTAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("HZ:ZHZ", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("KHZ:AHZ", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("KHZ:CHZ", new double[][]{new double[]{0, 1}, new double[]{10, 5}});
        c.put("KHZ:DAHZ", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("KHZ:DHZ", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("KHZ:EXAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("KHZ:FHZ", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("KHZ:GIGAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("KHZ:HHZ", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("KHZ:HZ", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("KHZ:MEGAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("KHZ:MHZ", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("KHZ:MICROHZ", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("KHZ:NHZ", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("KHZ:PETAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("KHZ:PHZ", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("KHZ:TERAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("KHZ:YHZ", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("KHZ:YOTTAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("KHZ:ZETTAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("KHZ:ZHZ", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("MEGAHZ:AHZ", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("MEGAHZ:CHZ", new double[][]{new double[]{0, 1}, new double[]{10, 8}});
        c.put("MEGAHZ:DAHZ", new double[][]{new double[]{0, 1}, new double[]{10, 5}});
        c.put("MEGAHZ:DHZ", new double[][]{new double[]{0, 1}, new double[]{10, 7}});
        c.put("MEGAHZ:EXAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("MEGAHZ:FHZ", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("MEGAHZ:GIGAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("MEGAHZ:HHZ", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("MEGAHZ:HZ", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("MEGAHZ:KHZ", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("MEGAHZ:MHZ", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("MEGAHZ:MICROHZ", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("MEGAHZ:NHZ", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("MEGAHZ:PETAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("MEGAHZ:PHZ", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("MEGAHZ:TERAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("MEGAHZ:YHZ", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("MEGAHZ:YOTTAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("MEGAHZ:ZETTAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("MEGAHZ:ZHZ", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("MHZ:AHZ", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("MHZ:CHZ", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("MHZ:DAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("MHZ:DHZ", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("MHZ:EXAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("MHZ:FHZ", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("MHZ:GIGAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("MHZ:HHZ", new double[][]{new double[]{0, 1}, new double[]{10, -5}});
        c.put("MHZ:HZ", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("MHZ:KHZ", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("MHZ:MEGAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("MHZ:MICROHZ", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("MHZ:NHZ", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("MHZ:PETAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("MHZ:PHZ", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("MHZ:TERAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("MHZ:YHZ", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("MHZ:YOTTAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("MHZ:ZETTAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("MHZ:ZHZ", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("MICROHZ:AHZ", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("MICROHZ:CHZ", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("MICROHZ:DAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -7}});
        c.put("MICROHZ:DHZ", new double[][]{new double[]{0, 1}, new double[]{10, -5}});
        c.put("MICROHZ:EXAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("MICROHZ:FHZ", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("MICROHZ:GIGAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("MICROHZ:HHZ", new double[][]{new double[]{0, 1}, new double[]{10, -8}});
        c.put("MICROHZ:HZ", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("MICROHZ:KHZ", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("MICROHZ:MEGAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("MICROHZ:MHZ", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("MICROHZ:NHZ", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("MICROHZ:PETAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("MICROHZ:PHZ", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("MICROHZ:TERAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("MICROHZ:YHZ", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("MICROHZ:YOTTAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("MICROHZ:ZETTAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("MICROHZ:ZHZ", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("NHZ:AHZ", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("NHZ:CHZ", new double[][]{new double[]{0, 1}, new double[]{10, -7}});
        c.put("NHZ:DAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -10}});
        c.put("NHZ:DHZ", new double[][]{new double[]{0, 1}, new double[]{10, -8}});
        c.put("NHZ:EXAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("NHZ:FHZ", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("NHZ:GIGAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("NHZ:HHZ", new double[][]{new double[]{0, 1}, new double[]{10, -11}});
        c.put("NHZ:HZ", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("NHZ:KHZ", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("NHZ:MEGAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("NHZ:MHZ", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("NHZ:MICROHZ", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("NHZ:PETAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("NHZ:PHZ", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("NHZ:TERAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("NHZ:YHZ", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("NHZ:YOTTAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("NHZ:ZETTAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("NHZ:ZHZ", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("PETAHZ:AHZ", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("PETAHZ:CHZ", new double[][]{new double[]{0, 1}, new double[]{10, 17}});
        c.put("PETAHZ:DAHZ", new double[][]{new double[]{0, 1}, new double[]{10, 14}});
        c.put("PETAHZ:DHZ", new double[][]{new double[]{0, 1}, new double[]{10, 16}});
        c.put("PETAHZ:EXAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("PETAHZ:FHZ", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("PETAHZ:GIGAHZ", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("PETAHZ:HHZ", new double[][]{new double[]{0, 1}, new double[]{10, 13}});
        c.put("PETAHZ:HZ", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("PETAHZ:KHZ", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("PETAHZ:MEGAHZ", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("PETAHZ:MHZ", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("PETAHZ:MICROHZ", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("PETAHZ:NHZ", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("PETAHZ:PHZ", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("PETAHZ:TERAHZ", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("PETAHZ:YHZ", new double[][]{new double[]{0, 1}, new double[]{10, 39}});
        c.put("PETAHZ:YOTTAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("PETAHZ:ZETTAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("PETAHZ:ZHZ", new double[][]{new double[]{0, 1}, new double[]{10, 36}});
        c.put("PHZ:AHZ", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("PHZ:CHZ", new double[][]{new double[]{0, 1}, new double[]{10, -10}});
        c.put("PHZ:DAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -13}});
        c.put("PHZ:DHZ", new double[][]{new double[]{0, 1}, new double[]{10, -11}});
        c.put("PHZ:EXAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("PHZ:FHZ", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("PHZ:GIGAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("PHZ:HHZ", new double[][]{new double[]{0, 1}, new double[]{10, -14}});
        c.put("PHZ:HZ", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("PHZ:KHZ", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("PHZ:MEGAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("PHZ:MHZ", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("PHZ:MICROHZ", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("PHZ:NHZ", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("PHZ:PETAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("PHZ:TERAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("PHZ:YHZ", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("PHZ:YOTTAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -36}});
        c.put("PHZ:ZETTAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("PHZ:ZHZ", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("TERAHZ:AHZ", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("TERAHZ:CHZ", new double[][]{new double[]{0, 1}, new double[]{10, 14}});
        c.put("TERAHZ:DAHZ", new double[][]{new double[]{0, 1}, new double[]{10, 11}});
        c.put("TERAHZ:DHZ", new double[][]{new double[]{0, 1}, new double[]{10, 13}});
        c.put("TERAHZ:EXAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("TERAHZ:FHZ", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("TERAHZ:GIGAHZ", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("TERAHZ:HHZ", new double[][]{new double[]{0, 1}, new double[]{10, 10}});
        c.put("TERAHZ:HZ", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("TERAHZ:KHZ", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("TERAHZ:MEGAHZ", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("TERAHZ:MHZ", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("TERAHZ:MICROHZ", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("TERAHZ:NHZ", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("TERAHZ:PETAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("TERAHZ:PHZ", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("TERAHZ:YHZ", new double[][]{new double[]{0, 1}, new double[]{10, 36}});
        c.put("TERAHZ:YOTTAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("TERAHZ:ZETTAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("TERAHZ:ZHZ", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("YHZ:AHZ", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("YHZ:CHZ", new double[][]{new double[]{0, 1}, new double[]{10, -22}});
        c.put("YHZ:DAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -25}});
        c.put("YHZ:DHZ", new double[][]{new double[]{0, 1}, new double[]{10, -23}});
        c.put("YHZ:EXAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -42}});
        c.put("YHZ:FHZ", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("YHZ:GIGAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("YHZ:HHZ", new double[][]{new double[]{0, 1}, new double[]{10, -26}});
        c.put("YHZ:HZ", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("YHZ:KHZ", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("YHZ:MEGAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("YHZ:MHZ", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("YHZ:MICROHZ", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("YHZ:NHZ", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("YHZ:PETAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -39}});
        c.put("YHZ:PHZ", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("YHZ:TERAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -36}});
        c.put("YHZ:YOTTAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -48}});
        c.put("YHZ:ZETTAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -45}});
        c.put("YHZ:ZHZ", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("YOTTAHZ:AHZ", new double[][]{new double[]{0, 1}, new double[]{10, 42}});
        c.put("YOTTAHZ:CHZ", new double[][]{new double[]{0, 1}, new double[]{10, 26}});
        c.put("YOTTAHZ:DAHZ", new double[][]{new double[]{0, 1}, new double[]{10, 23}});
        c.put("YOTTAHZ:DHZ", new double[][]{new double[]{0, 1}, new double[]{10, 25}});
        c.put("YOTTAHZ:EXAHZ", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("YOTTAHZ:FHZ", new double[][]{new double[]{0, 1}, new double[]{10, 39}});
        c.put("YOTTAHZ:GIGAHZ", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("YOTTAHZ:HHZ", new double[][]{new double[]{0, 1}, new double[]{10, 22}});
        c.put("YOTTAHZ:HZ", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("YOTTAHZ:KHZ", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("YOTTAHZ:MEGAHZ", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("YOTTAHZ:MHZ", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("YOTTAHZ:MICROHZ", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("YOTTAHZ:NHZ", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("YOTTAHZ:PETAHZ", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("YOTTAHZ:PHZ", new double[][]{new double[]{0, 1}, new double[]{10, 36}});
        c.put("YOTTAHZ:TERAHZ", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("YOTTAHZ:YHZ", new double[][]{new double[]{0, 1}, new double[]{10, 48}});
        c.put("YOTTAHZ:ZETTAHZ", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("YOTTAHZ:ZHZ", new double[][]{new double[]{0, 1}, new double[]{10, 45}});
        c.put("ZETTAHZ:AHZ", new double[][]{new double[]{0, 1}, new double[]{10, 39}});
        c.put("ZETTAHZ:CHZ", new double[][]{new double[]{0, 1}, new double[]{10, 23}});
        c.put("ZETTAHZ:DAHZ", new double[][]{new double[]{0, 1}, new double[]{10, 20}});
        c.put("ZETTAHZ:DHZ", new double[][]{new double[]{0, 1}, new double[]{10, 22}});
        c.put("ZETTAHZ:EXAHZ", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("ZETTAHZ:FHZ", new double[][]{new double[]{0, 1}, new double[]{10, 36}});
        c.put("ZETTAHZ:GIGAHZ", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("ZETTAHZ:HHZ", new double[][]{new double[]{0, 1}, new double[]{10, 19}});
        c.put("ZETTAHZ:HZ", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("ZETTAHZ:KHZ", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("ZETTAHZ:MEGAHZ", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("ZETTAHZ:MHZ", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("ZETTAHZ:MICROHZ", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("ZETTAHZ:NHZ", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("ZETTAHZ:PETAHZ", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("ZETTAHZ:PHZ", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("ZETTAHZ:TERAHZ", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("ZETTAHZ:YHZ", new double[][]{new double[]{0, 1}, new double[]{10, 45}});
        c.put("ZETTAHZ:YOTTAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("ZETTAHZ:ZHZ", new double[][]{new double[]{0, 1}, new double[]{10, 42}});
        c.put("ZHZ:AHZ", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("ZHZ:CHZ", new double[][]{new double[]{0, 1}, new double[]{10, -19}});
        c.put("ZHZ:DAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -22}});
        c.put("ZHZ:DHZ", new double[][]{new double[]{0, 1}, new double[]{10, -20}});
        c.put("ZHZ:EXAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -39}});
        c.put("ZHZ:FHZ", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("ZHZ:GIGAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("ZHZ:HHZ", new double[][]{new double[]{0, 1}, new double[]{10, -23}});
        c.put("ZHZ:HZ", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("ZHZ:KHZ", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("ZHZ:MEGAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("ZHZ:MHZ", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("ZHZ:MICROHZ", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("ZHZ:NHZ", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("ZHZ:PETAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -36}});
        c.put("ZHZ:PHZ", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("ZHZ:TERAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("ZHZ:YHZ", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("ZHZ:YOTTAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -45}});
        c.put("ZHZ:ZETTAHZ", new double[][]{new double[]{0, 1}, new double[]{10, -42}});
        conversions = Collections.unmodifiableMap(c);
    }

    private static final Map<UnitsFrequency, String> SYMBOLS;
    static {
        Map<UnitsFrequency, String> s = new HashMap<UnitsFrequency, String>();
        s.put(UnitsFrequency.AHZ, "aHz");
        s.put(UnitsFrequency.CHZ, "cHz");
        s.put(UnitsFrequency.DAHZ, "daHz");
        s.put(UnitsFrequency.DHZ, "dHz");
        s.put(UnitsFrequency.EXAHZ, "EHz");
        s.put(UnitsFrequency.FHZ, "fHz");
        s.put(UnitsFrequency.GIGAHZ, "GHz");
        s.put(UnitsFrequency.HHZ, "hHz");
        s.put(UnitsFrequency.HZ, "Hz");
        s.put(UnitsFrequency.KHZ, "kHz");
        s.put(UnitsFrequency.MEGAHZ, "MHz");
        s.put(UnitsFrequency.MHZ, "mHz");
        s.put(UnitsFrequency.MICROHZ, "µHz");
        s.put(UnitsFrequency.NHZ, "nHz");
        s.put(UnitsFrequency.PETAHZ, "PHz");
        s.put(UnitsFrequency.PHZ, "pHz");
        s.put(UnitsFrequency.TERAHZ, "THz");
        s.put(UnitsFrequency.YHZ, "yHz");
        s.put(UnitsFrequency.YOTTAHZ, "YHz");
        s.put(UnitsFrequency.ZETTAHZ, "ZHz");
        s.put(UnitsFrequency.ZHZ, "zHz");
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

