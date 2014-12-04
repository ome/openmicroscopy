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

import omero.model.enums.UnitsTime;

/**
 * Blitz wrapper around the {@link ome.model.units.Time} class.
 * Like {@link Details} and {@link Permissions}, this object
 * is embedded into other objects and does not have a full life
 * cycle of its own.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 */
public class TimeI extends Time implements ModelBased {

    private static final long serialVersionUID = 1L;

    private static final Map<String, double[][]> conversions;
    static {
        Map<String, double[][]> c = new HashMap<String, double[][]>();

        c.put("AS:CS", new double[][]{new double[]{0, 1}, new double[]{10, -16}});
        c.put("AS:D", new double[][]{null});
        c.put("AS:DAS", new double[][]{new double[]{0, 1}, new double[]{10, -19}});
        c.put("AS:DS", new double[][]{new double[]{0, 1}, new double[]{10, -17}});
        c.put("AS:EXAS", new double[][]{new double[]{0, 1}, new double[]{10, -36}});
        c.put("AS:FS", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("AS:GIGAS", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("AS:H", new double[][]{null});
        c.put("AS:HS", new double[][]{new double[]{0, 1}, new double[]{10, -20}});
        c.put("AS:KS", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("AS:MEGAS", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("AS:MICROS", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("AS:MIN", new double[][]{null});
        c.put("AS:MS", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("AS:NS", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("AS:PETAS", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("AS:PS", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("AS:S", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("AS:TERAS", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("AS:YOTTAS", new double[][]{new double[]{0, 1}, new double[]{10, -42}});
        c.put("AS:YS", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("AS:ZETTAS", new double[][]{new double[]{0, 1}, new double[]{10, -39}});
        c.put("AS:ZS", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("CS:AS", new double[][]{new double[]{0, 1}, new double[]{10, 16}});
        c.put("CS:D", new double[][]{null});
        c.put("CS:DAS", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("CS:DS", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("CS:EXAS", new double[][]{new double[]{0, 1}, new double[]{10, -20}});
        c.put("CS:FS", new double[][]{new double[]{0, 1}, new double[]{10, 13}});
        c.put("CS:GIGAS", new double[][]{new double[]{0, 1}, new double[]{10, -11}});
        c.put("CS:H", new double[][]{null});
        c.put("CS:HS", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("CS:KS", new double[][]{new double[]{0, 1}, new double[]{10, -5}});
        c.put("CS:MEGAS", new double[][]{new double[]{0, 1}, new double[]{10, -8}});
        c.put("CS:MICROS", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("CS:MIN", new double[][]{null});
        c.put("CS:MS", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("CS:NS", new double[][]{new double[]{0, 1}, new double[]{10, 7}});
        c.put("CS:PETAS", new double[][]{new double[]{0, 1}, new double[]{10, -17}});
        c.put("CS:PS", new double[][]{new double[]{0, 1}, new double[]{10, 10}});
        c.put("CS:S", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("CS:TERAS", new double[][]{new double[]{0, 1}, new double[]{10, -14}});
        c.put("CS:YOTTAS", new double[][]{new double[]{0, 1}, new double[]{10, -26}});
        c.put("CS:YS", new double[][]{new double[]{0, 1}, new double[]{10, 22}});
        c.put("CS:ZETTAS", new double[][]{new double[]{0, 1}, new double[]{10, -23}});
        c.put("CS:ZS", new double[][]{new double[]{0, 1}, new double[]{10, 19}});
        c.put("D:AS", new double[][]{null});
        c.put("D:CS", new double[][]{null});
        c.put("D:DAS", new double[][]{null});
        c.put("D:DS", new double[][]{null});
        c.put("D:EXAS", new double[][]{null});
        c.put("D:FS", new double[][]{null});
        c.put("D:GIGAS", new double[][]{null});
        c.put("D:H", new double[][]{null});
        c.put("D:HS", new double[][]{null});
        c.put("D:KS", new double[][]{null});
        c.put("D:MEGAS", new double[][]{null});
        c.put("D:MICROS", new double[][]{null});
        c.put("D:MIN", new double[][]{null});
        c.put("D:MS", new double[][]{null});
        c.put("D:NS", new double[][]{null});
        c.put("D:PETAS", new double[][]{null});
        c.put("D:PS", new double[][]{null});
        c.put("D:S", new double[][]{null});
        c.put("D:TERAS", new double[][]{null});
        c.put("D:YOTTAS", new double[][]{null});
        c.put("D:YS", new double[][]{null});
        c.put("D:ZETTAS", new double[][]{null});
        c.put("D:ZS", new double[][]{null});
        c.put("DAS:AS", new double[][]{new double[]{0, 1}, new double[]{10, 19}});
        c.put("DAS:CS", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("DAS:D", new double[][]{null});
        c.put("DAS:DS", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("DAS:EXAS", new double[][]{new double[]{0, 1}, new double[]{10, -17}});
        c.put("DAS:FS", new double[][]{new double[]{0, 1}, new double[]{10, 16}});
        c.put("DAS:GIGAS", new double[][]{new double[]{0, 1}, new double[]{10, -8}});
        c.put("DAS:H", new double[][]{null});
        c.put("DAS:HS", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("DAS:KS", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("DAS:MEGAS", new double[][]{new double[]{0, 1}, new double[]{10, -5}});
        c.put("DAS:MICROS", new double[][]{new double[]{0, 1}, new double[]{10, 7}});
        c.put("DAS:MIN", new double[][]{null});
        c.put("DAS:MS", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("DAS:NS", new double[][]{new double[]{0, 1}, new double[]{10, 10}});
        c.put("DAS:PETAS", new double[][]{new double[]{0, 1}, new double[]{10, -14}});
        c.put("DAS:PS", new double[][]{new double[]{0, 1}, new double[]{10, 13}});
        c.put("DAS:S", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("DAS:TERAS", new double[][]{new double[]{0, 1}, new double[]{10, -11}});
        c.put("DAS:YOTTAS", new double[][]{new double[]{0, 1}, new double[]{10, -23}});
        c.put("DAS:YS", new double[][]{new double[]{0, 1}, new double[]{10, 25}});
        c.put("DAS:ZETTAS", new double[][]{new double[]{0, 1}, new double[]{10, -20}});
        c.put("DAS:ZS", new double[][]{new double[]{0, 1}, new double[]{10, 22}});
        c.put("DS:AS", new double[][]{new double[]{0, 1}, new double[]{10, 17}});
        c.put("DS:CS", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("DS:D", new double[][]{null});
        c.put("DS:DAS", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("DS:EXAS", new double[][]{new double[]{0, 1}, new double[]{10, -19}});
        c.put("DS:FS", new double[][]{new double[]{0, 1}, new double[]{10, 14}});
        c.put("DS:GIGAS", new double[][]{new double[]{0, 1}, new double[]{10, -10}});
        c.put("DS:H", new double[][]{null});
        c.put("DS:HS", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("DS:KS", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("DS:MEGAS", new double[][]{new double[]{0, 1}, new double[]{10, -7}});
        c.put("DS:MICROS", new double[][]{new double[]{0, 1}, new double[]{10, 5}});
        c.put("DS:MIN", new double[][]{null});
        c.put("DS:MS", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("DS:NS", new double[][]{new double[]{0, 1}, new double[]{10, 8}});
        c.put("DS:PETAS", new double[][]{new double[]{0, 1}, new double[]{10, -16}});
        c.put("DS:PS", new double[][]{new double[]{0, 1}, new double[]{10, 11}});
        c.put("DS:S", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("DS:TERAS", new double[][]{new double[]{0, 1}, new double[]{10, -13}});
        c.put("DS:YOTTAS", new double[][]{new double[]{0, 1}, new double[]{10, -25}});
        c.put("DS:YS", new double[][]{new double[]{0, 1}, new double[]{10, 23}});
        c.put("DS:ZETTAS", new double[][]{new double[]{0, 1}, new double[]{10, -22}});
        c.put("DS:ZS", new double[][]{new double[]{0, 1}, new double[]{10, 20}});
        c.put("EXAS:AS", new double[][]{new double[]{0, 1}, new double[]{10, 36}});
        c.put("EXAS:CS", new double[][]{new double[]{0, 1}, new double[]{10, 20}});
        c.put("EXAS:D", new double[][]{null});
        c.put("EXAS:DAS", new double[][]{new double[]{0, 1}, new double[]{10, 17}});
        c.put("EXAS:DS", new double[][]{new double[]{0, 1}, new double[]{10, 19}});
        c.put("EXAS:FS", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("EXAS:GIGAS", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("EXAS:H", new double[][]{null});
        c.put("EXAS:HS", new double[][]{new double[]{0, 1}, new double[]{10, 16}});
        c.put("EXAS:KS", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("EXAS:MEGAS", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("EXAS:MICROS", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("EXAS:MIN", new double[][]{null});
        c.put("EXAS:MS", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("EXAS:NS", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("EXAS:PETAS", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("EXAS:PS", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("EXAS:S", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("EXAS:TERAS", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("EXAS:YOTTAS", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("EXAS:YS", new double[][]{new double[]{0, 1}, new double[]{10, 42}});
        c.put("EXAS:ZETTAS", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("EXAS:ZS", new double[][]{new double[]{0, 1}, new double[]{10, 39}});
        c.put("FS:AS", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("FS:CS", new double[][]{new double[]{0, 1}, new double[]{10, -13}});
        c.put("FS:D", new double[][]{null});
        c.put("FS:DAS", new double[][]{new double[]{0, 1}, new double[]{10, -16}});
        c.put("FS:DS", new double[][]{new double[]{0, 1}, new double[]{10, -14}});
        c.put("FS:EXAS", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("FS:GIGAS", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("FS:H", new double[][]{null});
        c.put("FS:HS", new double[][]{new double[]{0, 1}, new double[]{10, -17}});
        c.put("FS:KS", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("FS:MEGAS", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("FS:MICROS", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("FS:MIN", new double[][]{null});
        c.put("FS:MS", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("FS:NS", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("FS:PETAS", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("FS:PS", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("FS:S", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("FS:TERAS", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("FS:YOTTAS", new double[][]{new double[]{0, 1}, new double[]{10, -39}});
        c.put("FS:YS", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("FS:ZETTAS", new double[][]{new double[]{0, 1}, new double[]{10, -36}});
        c.put("FS:ZS", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("GIGAS:AS", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("GIGAS:CS", new double[][]{new double[]{0, 1}, new double[]{10, 11}});
        c.put("GIGAS:D", new double[][]{null});
        c.put("GIGAS:DAS", new double[][]{new double[]{0, 1}, new double[]{10, 8}});
        c.put("GIGAS:DS", new double[][]{new double[]{0, 1}, new double[]{10, 10}});
        c.put("GIGAS:EXAS", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("GIGAS:FS", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("GIGAS:H", new double[][]{null});
        c.put("GIGAS:HS", new double[][]{new double[]{0, 1}, new double[]{10, 7}});
        c.put("GIGAS:KS", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("GIGAS:MEGAS", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("GIGAS:MICROS", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("GIGAS:MIN", new double[][]{null});
        c.put("GIGAS:MS", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("GIGAS:NS", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("GIGAS:PETAS", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("GIGAS:PS", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("GIGAS:S", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("GIGAS:TERAS", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("GIGAS:YOTTAS", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("GIGAS:YS", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("GIGAS:ZETTAS", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("GIGAS:ZS", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("H:AS", new double[][]{null});
        c.put("H:CS", new double[][]{null});
        c.put("H:D", new double[][]{null});
        c.put("H:DAS", new double[][]{null});
        c.put("H:DS", new double[][]{null});
        c.put("H:EXAS", new double[][]{null});
        c.put("H:FS", new double[][]{null});
        c.put("H:GIGAS", new double[][]{null});
        c.put("H:HS", new double[][]{null});
        c.put("H:KS", new double[][]{null});
        c.put("H:MEGAS", new double[][]{null});
        c.put("H:MICROS", new double[][]{null});
        c.put("H:MIN", new double[][]{null});
        c.put("H:MS", new double[][]{null});
        c.put("H:NS", new double[][]{null});
        c.put("H:PETAS", new double[][]{null});
        c.put("H:PS", new double[][]{null});
        c.put("H:S", new double[][]{null});
        c.put("H:TERAS", new double[][]{null});
        c.put("H:YOTTAS", new double[][]{null});
        c.put("H:YS", new double[][]{null});
        c.put("H:ZETTAS", new double[][]{null});
        c.put("H:ZS", new double[][]{null});
        c.put("HS:AS", new double[][]{new double[]{0, 1}, new double[]{10, 20}});
        c.put("HS:CS", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("HS:D", new double[][]{null});
        c.put("HS:DAS", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("HS:DS", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("HS:EXAS", new double[][]{new double[]{0, 1}, new double[]{10, -16}});
        c.put("HS:FS", new double[][]{new double[]{0, 1}, new double[]{10, 17}});
        c.put("HS:GIGAS", new double[][]{new double[]{0, 1}, new double[]{10, -7}});
        c.put("HS:H", new double[][]{null});
        c.put("HS:KS", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("HS:MEGAS", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("HS:MICROS", new double[][]{new double[]{0, 1}, new double[]{10, 8}});
        c.put("HS:MIN", new double[][]{null});
        c.put("HS:MS", new double[][]{new double[]{0, 1}, new double[]{10, 5}});
        c.put("HS:NS", new double[][]{new double[]{0, 1}, new double[]{10, 11}});
        c.put("HS:PETAS", new double[][]{new double[]{0, 1}, new double[]{10, -13}});
        c.put("HS:PS", new double[][]{new double[]{0, 1}, new double[]{10, 14}});
        c.put("HS:S", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("HS:TERAS", new double[][]{new double[]{0, 1}, new double[]{10, -10}});
        c.put("HS:YOTTAS", new double[][]{new double[]{0, 1}, new double[]{10, -22}});
        c.put("HS:YS", new double[][]{new double[]{0, 1}, new double[]{10, 26}});
        c.put("HS:ZETTAS", new double[][]{new double[]{0, 1}, new double[]{10, -19}});
        c.put("HS:ZS", new double[][]{new double[]{0, 1}, new double[]{10, 23}});
        c.put("KS:AS", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("KS:CS", new double[][]{new double[]{0, 1}, new double[]{10, 5}});
        c.put("KS:D", new double[][]{null});
        c.put("KS:DAS", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("KS:DS", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("KS:EXAS", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("KS:FS", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("KS:GIGAS", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("KS:H", new double[][]{null});
        c.put("KS:HS", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("KS:MEGAS", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("KS:MICROS", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("KS:MIN", new double[][]{null});
        c.put("KS:MS", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("KS:NS", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("KS:PETAS", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("KS:PS", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("KS:S", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("KS:TERAS", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("KS:YOTTAS", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("KS:YS", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("KS:ZETTAS", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("KS:ZS", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("MEGAS:AS", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("MEGAS:CS", new double[][]{new double[]{0, 1}, new double[]{10, 8}});
        c.put("MEGAS:D", new double[][]{null});
        c.put("MEGAS:DAS", new double[][]{new double[]{0, 1}, new double[]{10, 5}});
        c.put("MEGAS:DS", new double[][]{new double[]{0, 1}, new double[]{10, 7}});
        c.put("MEGAS:EXAS", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("MEGAS:FS", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("MEGAS:GIGAS", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("MEGAS:H", new double[][]{null});
        c.put("MEGAS:HS", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("MEGAS:KS", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("MEGAS:MICROS", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("MEGAS:MIN", new double[][]{null});
        c.put("MEGAS:MS", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("MEGAS:NS", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("MEGAS:PETAS", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("MEGAS:PS", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("MEGAS:S", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("MEGAS:TERAS", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("MEGAS:YOTTAS", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("MEGAS:YS", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("MEGAS:ZETTAS", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("MEGAS:ZS", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("MICROS:AS", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("MICROS:CS", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("MICROS:D", new double[][]{null});
        c.put("MICROS:DAS", new double[][]{new double[]{0, 1}, new double[]{10, -7}});
        c.put("MICROS:DS", new double[][]{new double[]{0, 1}, new double[]{10, -5}});
        c.put("MICROS:EXAS", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("MICROS:FS", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("MICROS:GIGAS", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("MICROS:H", new double[][]{null});
        c.put("MICROS:HS", new double[][]{new double[]{0, 1}, new double[]{10, -8}});
        c.put("MICROS:KS", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("MICROS:MEGAS", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("MICROS:MIN", new double[][]{null});
        c.put("MICROS:MS", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("MICROS:NS", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("MICROS:PETAS", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("MICROS:PS", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("MICROS:S", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("MICROS:TERAS", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("MICROS:YOTTAS", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("MICROS:YS", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("MICROS:ZETTAS", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("MICROS:ZS", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("MIN:AS", new double[][]{null});
        c.put("MIN:CS", new double[][]{null});
        c.put("MIN:D", new double[][]{null});
        c.put("MIN:DAS", new double[][]{null});
        c.put("MIN:DS", new double[][]{null});
        c.put("MIN:EXAS", new double[][]{null});
        c.put("MIN:FS", new double[][]{null});
        c.put("MIN:GIGAS", new double[][]{null});
        c.put("MIN:H", new double[][]{null});
        c.put("MIN:HS", new double[][]{null});
        c.put("MIN:KS", new double[][]{null});
        c.put("MIN:MEGAS", new double[][]{null});
        c.put("MIN:MICROS", new double[][]{null});
        c.put("MIN:MS", new double[][]{null});
        c.put("MIN:NS", new double[][]{null});
        c.put("MIN:PETAS", new double[][]{null});
        c.put("MIN:PS", new double[][]{null});
        c.put("MIN:S", new double[][]{null});
        c.put("MIN:TERAS", new double[][]{null});
        c.put("MIN:YOTTAS", new double[][]{null});
        c.put("MIN:YS", new double[][]{null});
        c.put("MIN:ZETTAS", new double[][]{null});
        c.put("MIN:ZS", new double[][]{null});
        c.put("MS:AS", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("MS:CS", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("MS:D", new double[][]{null});
        c.put("MS:DAS", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("MS:DS", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("MS:EXAS", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("MS:FS", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("MS:GIGAS", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("MS:H", new double[][]{null});
        c.put("MS:HS", new double[][]{new double[]{0, 1}, new double[]{10, -5}});
        c.put("MS:KS", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("MS:MEGAS", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("MS:MICROS", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("MS:MIN", new double[][]{null});
        c.put("MS:NS", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("MS:PETAS", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("MS:PS", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("MS:S", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("MS:TERAS", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("MS:YOTTAS", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("MS:YS", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("MS:ZETTAS", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("MS:ZS", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("NS:AS", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("NS:CS", new double[][]{new double[]{0, 1}, new double[]{10, -7}});
        c.put("NS:D", new double[][]{null});
        c.put("NS:DAS", new double[][]{new double[]{0, 1}, new double[]{10, -10}});
        c.put("NS:DS", new double[][]{new double[]{0, 1}, new double[]{10, -8}});
        c.put("NS:EXAS", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("NS:FS", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("NS:GIGAS", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("NS:H", new double[][]{null});
        c.put("NS:HS", new double[][]{new double[]{0, 1}, new double[]{10, -11}});
        c.put("NS:KS", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("NS:MEGAS", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("NS:MICROS", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("NS:MIN", new double[][]{null});
        c.put("NS:MS", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("NS:PETAS", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("NS:PS", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("NS:S", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("NS:TERAS", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("NS:YOTTAS", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("NS:YS", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("NS:ZETTAS", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("NS:ZS", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("PETAS:AS", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("PETAS:CS", new double[][]{new double[]{0, 1}, new double[]{10, 17}});
        c.put("PETAS:D", new double[][]{null});
        c.put("PETAS:DAS", new double[][]{new double[]{0, 1}, new double[]{10, 14}});
        c.put("PETAS:DS", new double[][]{new double[]{0, 1}, new double[]{10, 16}});
        c.put("PETAS:EXAS", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("PETAS:FS", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("PETAS:GIGAS", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("PETAS:H", new double[][]{null});
        c.put("PETAS:HS", new double[][]{new double[]{0, 1}, new double[]{10, 13}});
        c.put("PETAS:KS", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("PETAS:MEGAS", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("PETAS:MICROS", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("PETAS:MIN", new double[][]{null});
        c.put("PETAS:MS", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("PETAS:NS", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("PETAS:PS", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("PETAS:S", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("PETAS:TERAS", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("PETAS:YOTTAS", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("PETAS:YS", new double[][]{new double[]{0, 1}, new double[]{10, 39}});
        c.put("PETAS:ZETTAS", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("PETAS:ZS", new double[][]{new double[]{0, 1}, new double[]{10, 36}});
        c.put("PS:AS", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("PS:CS", new double[][]{new double[]{0, 1}, new double[]{10, -10}});
        c.put("PS:D", new double[][]{null});
        c.put("PS:DAS", new double[][]{new double[]{0, 1}, new double[]{10, -13}});
        c.put("PS:DS", new double[][]{new double[]{0, 1}, new double[]{10, -11}});
        c.put("PS:EXAS", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("PS:FS", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("PS:GIGAS", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("PS:H", new double[][]{null});
        c.put("PS:HS", new double[][]{new double[]{0, 1}, new double[]{10, -14}});
        c.put("PS:KS", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("PS:MEGAS", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("PS:MICROS", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("PS:MIN", new double[][]{null});
        c.put("PS:MS", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("PS:NS", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("PS:PETAS", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("PS:S", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("PS:TERAS", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("PS:YOTTAS", new double[][]{new double[]{0, 1}, new double[]{10, -36}});
        c.put("PS:YS", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("PS:ZETTAS", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("PS:ZS", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("S:AS", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("S:CS", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("S:D", new double[][]{null});
        c.put("S:DAS", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("S:DS", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("S:EXAS", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("S:FS", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("S:GIGAS", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("S:H", new double[][]{null});
        c.put("S:HS", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("S:KS", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("S:MEGAS", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("S:MICROS", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("S:MIN", new double[][]{null});
        c.put("S:MS", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("S:NS", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("S:PETAS", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("S:PS", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("S:TERAS", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("S:YOTTAS", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("S:YS", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("S:ZETTAS", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("S:ZS", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("TERAS:AS", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("TERAS:CS", new double[][]{new double[]{0, 1}, new double[]{10, 14}});
        c.put("TERAS:D", new double[][]{null});
        c.put("TERAS:DAS", new double[][]{new double[]{0, 1}, new double[]{10, 11}});
        c.put("TERAS:DS", new double[][]{new double[]{0, 1}, new double[]{10, 13}});
        c.put("TERAS:EXAS", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("TERAS:FS", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("TERAS:GIGAS", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("TERAS:H", new double[][]{null});
        c.put("TERAS:HS", new double[][]{new double[]{0, 1}, new double[]{10, 10}});
        c.put("TERAS:KS", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("TERAS:MEGAS", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("TERAS:MICROS", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("TERAS:MIN", new double[][]{null});
        c.put("TERAS:MS", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("TERAS:NS", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("TERAS:PETAS", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("TERAS:PS", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("TERAS:S", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("TERAS:YOTTAS", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("TERAS:YS", new double[][]{new double[]{0, 1}, new double[]{10, 36}});
        c.put("TERAS:ZETTAS", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("TERAS:ZS", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("YOTTAS:AS", new double[][]{new double[]{0, 1}, new double[]{10, 42}});
        c.put("YOTTAS:CS", new double[][]{new double[]{0, 1}, new double[]{10, 26}});
        c.put("YOTTAS:D", new double[][]{null});
        c.put("YOTTAS:DAS", new double[][]{new double[]{0, 1}, new double[]{10, 23}});
        c.put("YOTTAS:DS", new double[][]{new double[]{0, 1}, new double[]{10, 25}});
        c.put("YOTTAS:EXAS", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("YOTTAS:FS", new double[][]{new double[]{0, 1}, new double[]{10, 39}});
        c.put("YOTTAS:GIGAS", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("YOTTAS:H", new double[][]{null});
        c.put("YOTTAS:HS", new double[][]{new double[]{0, 1}, new double[]{10, 22}});
        c.put("YOTTAS:KS", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("YOTTAS:MEGAS", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("YOTTAS:MICROS", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("YOTTAS:MIN", new double[][]{null});
        c.put("YOTTAS:MS", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("YOTTAS:NS", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("YOTTAS:PETAS", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("YOTTAS:PS", new double[][]{new double[]{0, 1}, new double[]{10, 36}});
        c.put("YOTTAS:S", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("YOTTAS:TERAS", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("YOTTAS:YS", new double[][]{new double[]{0, 1}, new double[]{10, 48}});
        c.put("YOTTAS:ZETTAS", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("YOTTAS:ZS", new double[][]{new double[]{0, 1}, new double[]{10, 45}});
        c.put("YS:AS", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("YS:CS", new double[][]{new double[]{0, 1}, new double[]{10, -22}});
        c.put("YS:D", new double[][]{null});
        c.put("YS:DAS", new double[][]{new double[]{0, 1}, new double[]{10, -25}});
        c.put("YS:DS", new double[][]{new double[]{0, 1}, new double[]{10, -23}});
        c.put("YS:EXAS", new double[][]{new double[]{0, 1}, new double[]{10, -42}});
        c.put("YS:FS", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("YS:GIGAS", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("YS:H", new double[][]{null});
        c.put("YS:HS", new double[][]{new double[]{0, 1}, new double[]{10, -26}});
        c.put("YS:KS", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("YS:MEGAS", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("YS:MICROS", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("YS:MIN", new double[][]{null});
        c.put("YS:MS", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("YS:NS", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("YS:PETAS", new double[][]{new double[]{0, 1}, new double[]{10, -39}});
        c.put("YS:PS", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("YS:S", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("YS:TERAS", new double[][]{new double[]{0, 1}, new double[]{10, -36}});
        c.put("YS:YOTTAS", new double[][]{new double[]{0, 1}, new double[]{10, -48}});
        c.put("YS:ZETTAS", new double[][]{new double[]{0, 1}, new double[]{10, -45}});
        c.put("YS:ZS", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("ZETTAS:AS", new double[][]{new double[]{0, 1}, new double[]{10, 39}});
        c.put("ZETTAS:CS", new double[][]{new double[]{0, 1}, new double[]{10, 23}});
        c.put("ZETTAS:D", new double[][]{null});
        c.put("ZETTAS:DAS", new double[][]{new double[]{0, 1}, new double[]{10, 20}});
        c.put("ZETTAS:DS", new double[][]{new double[]{0, 1}, new double[]{10, 22}});
        c.put("ZETTAS:EXAS", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("ZETTAS:FS", new double[][]{new double[]{0, 1}, new double[]{10, 36}});
        c.put("ZETTAS:GIGAS", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("ZETTAS:H", new double[][]{null});
        c.put("ZETTAS:HS", new double[][]{new double[]{0, 1}, new double[]{10, 19}});
        c.put("ZETTAS:KS", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("ZETTAS:MEGAS", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("ZETTAS:MICROS", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("ZETTAS:MIN", new double[][]{null});
        c.put("ZETTAS:MS", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("ZETTAS:NS", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("ZETTAS:PETAS", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("ZETTAS:PS", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("ZETTAS:S", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("ZETTAS:TERAS", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("ZETTAS:YOTTAS", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("ZETTAS:YS", new double[][]{new double[]{0, 1}, new double[]{10, 45}});
        c.put("ZETTAS:ZS", new double[][]{new double[]{0, 1}, new double[]{10, 42}});
        c.put("ZS:AS", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("ZS:CS", new double[][]{new double[]{0, 1}, new double[]{10, -19}});
        c.put("ZS:D", new double[][]{null});
        c.put("ZS:DAS", new double[][]{new double[]{0, 1}, new double[]{10, -22}});
        c.put("ZS:DS", new double[][]{new double[]{0, 1}, new double[]{10, -20}});
        c.put("ZS:EXAS", new double[][]{new double[]{0, 1}, new double[]{10, -39}});
        c.put("ZS:FS", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("ZS:GIGAS", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("ZS:H", new double[][]{null});
        c.put("ZS:HS", new double[][]{new double[]{0, 1}, new double[]{10, -23}});
        c.put("ZS:KS", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("ZS:MEGAS", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("ZS:MICROS", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("ZS:MIN", new double[][]{null});
        c.put("ZS:MS", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("ZS:NS", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("ZS:PETAS", new double[][]{new double[]{0, 1}, new double[]{10, -36}});
        c.put("ZS:PS", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("ZS:S", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("ZS:TERAS", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("ZS:YOTTAS", new double[][]{new double[]{0, 1}, new double[]{10, -45}});
        c.put("ZS:YS", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("ZS:ZETTAS", new double[][]{new double[]{0, 1}, new double[]{10, -42}});
        conversions = Collections.unmodifiableMap(c);
    }

    private static final Map<UnitsTime, String> SYMBOLS;
    static {
        Map<UnitsTime, String> s = new HashMap<UnitsTime, String>();
        s.put(UnitsTime.AS, "as");
        s.put(UnitsTime.CS, "cs");
        s.put(UnitsTime.D, "d");
        s.put(UnitsTime.DAS, "das");
        s.put(UnitsTime.DS, "ds");
        s.put(UnitsTime.EXAS, "Es");
        s.put(UnitsTime.FS, "fs");
        s.put(UnitsTime.GIGAS, "Gs");
        s.put(UnitsTime.H, "h");
        s.put(UnitsTime.HS, "hs");
        s.put(UnitsTime.KS, "ks");
        s.put(UnitsTime.MEGAS, "Ms");
        s.put(UnitsTime.MICROS, "µs");
        s.put(UnitsTime.MIN, "min");
        s.put(UnitsTime.MS, "ms");
        s.put(UnitsTime.NS, "ns");
        s.put(UnitsTime.PETAS, "Ps");
        s.put(UnitsTime.PS, "ps");
        s.put(UnitsTime.S, "s");
        s.put(UnitsTime.TERAS, "Ts");
        s.put(UnitsTime.YOTTAS, "Ys");
        s.put(UnitsTime.YS, "ys");
        s.put(UnitsTime.ZETTAS, "Zs");
        s.put(UnitsTime.ZS, "zs");
        SYMBOLS = s;
    }

    public static String lookupSymbol(UnitsTime unit) {
        return SYMBOLS.get(unit);
    }

    public static final Ice.ObjectFactory makeFactory(final omero.client client) {

        return new Ice.ObjectFactory() {

            public Ice.Object create(String arg0) {
                return new TimeI();
            }

            public void destroy() {
                // no-op
            }

        };
    };

    //
    // CONVERSIONS
    //

    public static ome.xml.model.enums.UnitsTime makeXMLUnit(String unit) {
        try {
            return ome.xml.model.enums.UnitsTime
                    .fromString((String) unit);
        } catch (EnumerationException e) {
            throw new RuntimeException("Bad Time unit: " + unit, e);
        }
    }

    public static ome.units.quantity.Time makeXMLQuantity(double d, String unit) {
        ome.units.unit.Unit<ome.units.quantity.Time> units =
                ome.xml.model.enums.handlers.UnitsTimeEnumHandler
                        .getBaseUnit(makeXMLUnit(unit));
        return new ome.units.quantity.Time(d, units);
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
   public static ome.units.quantity.Time convert(Time t) {
       if (t == null) {
           return null;
       }

       Double v = t.getValue();
       // Use the code/symbol-mapping in the ome.model.enums files
       // to convert to the specification value.
       String u = ome.model.enums.UnitsTime.valueOf(
               t.getUnit().toString()).getSymbol();
       ome.xml.model.enums.UnitsTime units = makeXMLUnit(u);
       ome.units.unit.Unit<ome.units.quantity.Time> units2 =
               ome.xml.model.enums.handlers.UnitsTimeEnumHandler
                       .getBaseUnit(units);

       return new ome.units.quantity.Time(v, units2);
   }


    //
    // REGULAR ICE CLASS
    //

    public final static Ice.ObjectFactory Factory = makeFactory(null);

    public TimeI() {
        super();
    }

    public TimeI(double d, UnitsTime unit) {
        super();
        this.setUnit(unit);
        this.setValue(d);
    }

    public TimeI(double d,
            Unit<ome.units.quantity.Time> unit) {
        this(d, ome.model.enums.UnitsTime.bySymbol(unit.getSymbol()));
    }

   /**
    * Copy constructor that converts the given {@link omero.model.Time}
    * based on the given ome-xml enum
    */
   public TimeI(Time value, Unit<ome.units.quantity.Time> ul) {
       this(value,
            ome.model.enums.UnitsTime.bySymbol(ul.getSymbol()).toString());
   }

   /**
    * Copy constructor that converts the given {@link omero.model.Time}
    * based on the given ome.model enum
    */
   public TimeI(double d, ome.model.enums.UnitsTime ul) {
        this(d, UnitsTime.valueOf(ul.toString()));
    }

   /**
    * Copy constructor that converts the given {@link omero.model.Time}
    * based on the given enum string.
    *
    * @param target String representation of the CODE enum
    */
    public TimeI(Time value, String target) {
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
            setUnit(UnitsTime.valueOf(target));
       }
    }

   /**
    * Copy constructor that converts between units if possible.
    *
    * @param target unit that is desired. non-null.
    */
    public TimeI(Time value, UnitsTime target) {
        this(value, target.toString());
    }

    /**
     * Convert a Bio-Formats {@link Length} to an OMERO Length.
     */
    public TimeI(ome.units.quantity.Time value) {
        ome.model.enums.UnitsTime internal =
            ome.model.enums.UnitsTime.bySymbol(value.unit().getSymbol());
        UnitsTime ul = UnitsTime.valueOf(internal.toString());
        setValue(value.value().doubleValue());
        setUnit(ul);
    }

    public double getValue(Ice.Current current) {
        return this.value;
    }

    public void setValue(double value , Ice.Current current) {
        this.value = value;
    }

    public UnitsTime getUnit(Ice.Current current) {
        return this.unit;
    }

    public void setUnit(UnitsTime unit, Ice.Current current) {
        this.unit = unit;
    }

    public String getSymbol(Ice.Current current) {
        return SYMBOLS.get(this.unit);
    }

    public Time copy(Ice.Current ignore) {
        TimeI copy = new TimeI();
        copy.setValue(getValue());
        copy.setUnit(getUnit());
        return copy;
    }

    @Override
    public void copyObject(Filterable model, ModelMapper mapper) {
        if (model instanceof ome.model.units.Time) {
            ome.model.units.Time t = (ome.model.units.Time) model;
            this.value = t.getValue();
            this.unit = UnitsTime.valueOf(t.getUnit().toString());
        } else {
            throw new IllegalArgumentException(
              "Time cannot copy from " +
              (model==null ? "null" : model.getClass().getName()));
        }
    }

    @Override
    public Filterable fillObject(ReverseModelMapper mapper) {
        ome.model.enums.UnitsTime ut = ome.model.enums.UnitsTime.valueOf(getUnit().toString());
        ome.model.units.Time t = new ome.model.units.Time(getValue(), ut);
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
        return "Time(" + value + " " + unit + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Time other = (Time) obj;
        if (unit != other.unit)
            return false;
        if (Double.doubleToLongBits(value) != Double
                .doubleToLongBits(other.value))
            return false;
        return true;
    }

}

