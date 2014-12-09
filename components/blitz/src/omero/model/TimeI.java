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

        c.put("ATOOSECOND:CENTISECOND", new double[][]{new double[]{0, 1}, new double[]{10, -16}});
        c.put("ATOOSECOND:DECISECOND", new double[][]{new double[]{0, 1}, new double[]{10, -17}});
        c.put("ATOOSECOND:DEKASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -19}});
        c.put("ATOOSECOND:EXASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -36}});
        c.put("ATOOSECOND:FEMTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("ATOOSECOND:GIGASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("ATOOSECOND:HECTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, -20}});
        c.put("ATOOSECOND:KILOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("ATOOSECOND:MEGASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("ATOOSECOND:MICROSECOND", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("ATOOSECOND:MILLISECOND", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("ATOOSECOND:NANOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("ATOOSECOND:PETASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("ATOOSECOND:PICOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("ATOOSECOND:SECOND", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("ATOOSECOND:TERASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("ATOOSECOND:YOCTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("ATOOSECOND:YOTTASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -42}});
        c.put("ATOOSECOND:ZEPTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("ATOOSECOND:ZETTASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -39}});
        c.put("CENTISECOND:ATOOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 16}});
        c.put("CENTISECOND:DECISECOND", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("CENTISECOND:DEKASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("CENTISECOND:EXASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -20}});
        c.put("CENTISECOND:FEMTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 13}});
        c.put("CENTISECOND:GIGASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -11}});
        c.put("CENTISECOND:HECTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("CENTISECOND:KILOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, -5}});
        c.put("CENTISECOND:MEGASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -8}});
        c.put("CENTISECOND:MICROSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("CENTISECOND:MILLISECOND", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("CENTISECOND:NANOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 7}});
        c.put("CENTISECOND:PETASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -17}});
        c.put("CENTISECOND:PICOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 10}});
        c.put("CENTISECOND:SECOND", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("CENTISECOND:TERASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -14}});
        c.put("CENTISECOND:YOCTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 22}});
        c.put("CENTISECOND:YOTTASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -26}});
        c.put("CENTISECOND:ZEPTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 19}});
        c.put("CENTISECOND:ZETTASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -23}});
        c.put("DECISECOND:ATOOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 17}});
        c.put("DECISECOND:CENTISECOND", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("DECISECOND:DEKASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("DECISECOND:EXASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -19}});
        c.put("DECISECOND:FEMTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 14}});
        c.put("DECISECOND:GIGASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -10}});
        c.put("DECISECOND:HECTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("DECISECOND:KILOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("DECISECOND:MEGASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -7}});
        c.put("DECISECOND:MICROSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 5}});
        c.put("DECISECOND:MILLISECOND", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("DECISECOND:NANOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 8}});
        c.put("DECISECOND:PETASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -16}});
        c.put("DECISECOND:PICOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 11}});
        c.put("DECISECOND:SECOND", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("DECISECOND:TERASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -13}});
        c.put("DECISECOND:YOCTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 23}});
        c.put("DECISECOND:YOTTASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -25}});
        c.put("DECISECOND:ZEPTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 20}});
        c.put("DECISECOND:ZETTASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -22}});
        c.put("DEKASECOND:ATOOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 19}});
        c.put("DEKASECOND:CENTISECOND", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("DEKASECOND:DECISECOND", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("DEKASECOND:EXASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -17}});
        c.put("DEKASECOND:FEMTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 16}});
        c.put("DEKASECOND:GIGASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -8}});
        c.put("DEKASECOND:HECTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("DEKASECOND:KILOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("DEKASECOND:MEGASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -5}});
        c.put("DEKASECOND:MICROSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 7}});
        c.put("DEKASECOND:MILLISECOND", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("DEKASECOND:NANOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 10}});
        c.put("DEKASECOND:PETASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -14}});
        c.put("DEKASECOND:PICOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 13}});
        c.put("DEKASECOND:SECOND", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("DEKASECOND:TERASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -11}});
        c.put("DEKASECOND:YOCTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 25}});
        c.put("DEKASECOND:YOTTASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -23}});
        c.put("DEKASECOND:ZEPTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 22}});
        c.put("DEKASECOND:ZETTASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -20}});
        c.put("EXASECOND:ATOOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 36}});
        c.put("EXASECOND:CENTISECOND", new double[][]{new double[]{0, 1}, new double[]{10, 20}});
        c.put("EXASECOND:DECISECOND", new double[][]{new double[]{0, 1}, new double[]{10, 19}});
        c.put("EXASECOND:DEKASECOND", new double[][]{new double[]{0, 1}, new double[]{10, 17}});
        c.put("EXASECOND:FEMTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("EXASECOND:GIGASECOND", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("EXASECOND:HECTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 16}});
        c.put("EXASECOND:KILOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("EXASECOND:MEGASECOND", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("EXASECOND:MICROSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("EXASECOND:MILLISECOND", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("EXASECOND:NANOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("EXASECOND:PETASECOND", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("EXASECOND:PICOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("EXASECOND:SECOND", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("EXASECOND:TERASECOND", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("EXASECOND:YOCTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 42}});
        c.put("EXASECOND:YOTTASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("EXASECOND:ZEPTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 39}});
        c.put("EXASECOND:ZETTASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("FEMTOSECOND:ATOOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("FEMTOSECOND:CENTISECOND", new double[][]{new double[]{0, 1}, new double[]{10, -13}});
        c.put("FEMTOSECOND:DECISECOND", new double[][]{new double[]{0, 1}, new double[]{10, -14}});
        c.put("FEMTOSECOND:DEKASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -16}});
        c.put("FEMTOSECOND:EXASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("FEMTOSECOND:GIGASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("FEMTOSECOND:HECTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, -17}});
        c.put("FEMTOSECOND:KILOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("FEMTOSECOND:MEGASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("FEMTOSECOND:MICROSECOND", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("FEMTOSECOND:MILLISECOND", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("FEMTOSECOND:NANOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("FEMTOSECOND:PETASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("FEMTOSECOND:PICOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("FEMTOSECOND:SECOND", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("FEMTOSECOND:TERASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("FEMTOSECOND:YOCTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("FEMTOSECOND:YOTTASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -39}});
        c.put("FEMTOSECOND:ZEPTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("FEMTOSECOND:ZETTASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -36}});
        c.put("GIGASECOND:ATOOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("GIGASECOND:CENTISECOND", new double[][]{new double[]{0, 1}, new double[]{10, 11}});
        c.put("GIGASECOND:DECISECOND", new double[][]{new double[]{0, 1}, new double[]{10, 10}});
        c.put("GIGASECOND:DEKASECOND", new double[][]{new double[]{0, 1}, new double[]{10, 8}});
        c.put("GIGASECOND:EXASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("GIGASECOND:FEMTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("GIGASECOND:HECTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 7}});
        c.put("GIGASECOND:KILOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("GIGASECOND:MEGASECOND", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("GIGASECOND:MICROSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("GIGASECOND:MILLISECOND", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("GIGASECOND:NANOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("GIGASECOND:PETASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("GIGASECOND:PICOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("GIGASECOND:SECOND", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("GIGASECOND:TERASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("GIGASECOND:YOCTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("GIGASECOND:YOTTASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("GIGASECOND:ZEPTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("GIGASECOND:ZETTASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("HECTOSECOND:ATOOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 20}});
        c.put("HECTOSECOND:CENTISECOND", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("HECTOSECOND:DECISECOND", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("HECTOSECOND:DEKASECOND", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("HECTOSECOND:EXASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -16}});
        c.put("HECTOSECOND:FEMTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 17}});
        c.put("HECTOSECOND:GIGASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -7}});
        c.put("HECTOSECOND:KILOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("HECTOSECOND:MEGASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("HECTOSECOND:MICROSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 8}});
        c.put("HECTOSECOND:MILLISECOND", new double[][]{new double[]{0, 1}, new double[]{10, 5}});
        c.put("HECTOSECOND:NANOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 11}});
        c.put("HECTOSECOND:PETASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -13}});
        c.put("HECTOSECOND:PICOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 14}});
        c.put("HECTOSECOND:SECOND", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("HECTOSECOND:TERASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -10}});
        c.put("HECTOSECOND:YOCTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 26}});
        c.put("HECTOSECOND:YOTTASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -22}});
        c.put("HECTOSECOND:ZEPTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 23}});
        c.put("HECTOSECOND:ZETTASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -19}});
        c.put("KILOSECOND:ATOOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("KILOSECOND:CENTISECOND", new double[][]{new double[]{0, 1}, new double[]{10, 5}});
        c.put("KILOSECOND:DECISECOND", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("KILOSECOND:DEKASECOND", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("KILOSECOND:EXASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("KILOSECOND:FEMTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("KILOSECOND:GIGASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("KILOSECOND:HECTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("KILOSECOND:MEGASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("KILOSECOND:MICROSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("KILOSECOND:MILLISECOND", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("KILOSECOND:NANOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("KILOSECOND:PETASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("KILOSECOND:PICOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("KILOSECOND:SECOND", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("KILOSECOND:TERASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("KILOSECOND:YOCTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("KILOSECOND:YOTTASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("KILOSECOND:ZEPTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("KILOSECOND:ZETTASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("MEGASECOND:ATOOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("MEGASECOND:CENTISECOND", new double[][]{new double[]{0, 1}, new double[]{10, 8}});
        c.put("MEGASECOND:DECISECOND", new double[][]{new double[]{0, 1}, new double[]{10, 7}});
        c.put("MEGASECOND:DEKASECOND", new double[][]{new double[]{0, 1}, new double[]{10, 5}});
        c.put("MEGASECOND:EXASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("MEGASECOND:FEMTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("MEGASECOND:GIGASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("MEGASECOND:HECTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("MEGASECOND:KILOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("MEGASECOND:MICROSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("MEGASECOND:MILLISECOND", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("MEGASECOND:NANOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("MEGASECOND:PETASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("MEGASECOND:PICOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("MEGASECOND:SECOND", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("MEGASECOND:TERASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("MEGASECOND:YOCTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("MEGASECOND:YOTTASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("MEGASECOND:ZEPTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("MEGASECOND:ZETTASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("MICROSECOND:ATOOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("MICROSECOND:CENTISECOND", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("MICROSECOND:DECISECOND", new double[][]{new double[]{0, 1}, new double[]{10, -5}});
        c.put("MICROSECOND:DEKASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -7}});
        c.put("MICROSECOND:EXASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("MICROSECOND:FEMTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("MICROSECOND:GIGASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("MICROSECOND:HECTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, -8}});
        c.put("MICROSECOND:KILOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("MICROSECOND:MEGASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("MICROSECOND:MILLISECOND", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("MICROSECOND:NANOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("MICROSECOND:PETASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("MICROSECOND:PICOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("MICROSECOND:SECOND", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("MICROSECOND:TERASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("MICROSECOND:YOCTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("MICROSECOND:YOTTASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("MICROSECOND:ZEPTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("MICROSECOND:ZETTASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("MILLISECOND:ATOOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("MILLISECOND:CENTISECOND", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("MILLISECOND:DECISECOND", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("MILLISECOND:DEKASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("MILLISECOND:EXASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("MILLISECOND:FEMTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("MILLISECOND:GIGASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("MILLISECOND:HECTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, -5}});
        c.put("MILLISECOND:KILOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("MILLISECOND:MEGASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("MILLISECOND:MICROSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("MILLISECOND:NANOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("MILLISECOND:PETASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("MILLISECOND:PICOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("MILLISECOND:SECOND", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("MILLISECOND:TERASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("MILLISECOND:YOCTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("MILLISECOND:YOTTASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("MILLISECOND:ZEPTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("MILLISECOND:ZETTASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("NANOSECOND:ATOOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("NANOSECOND:CENTISECOND", new double[][]{new double[]{0, 1}, new double[]{10, -7}});
        c.put("NANOSECOND:DECISECOND", new double[][]{new double[]{0, 1}, new double[]{10, -8}});
        c.put("NANOSECOND:DEKASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -10}});
        c.put("NANOSECOND:EXASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("NANOSECOND:FEMTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("NANOSECOND:GIGASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("NANOSECOND:HECTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, -11}});
        c.put("NANOSECOND:KILOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("NANOSECOND:MEGASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("NANOSECOND:MICROSECOND", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("NANOSECOND:MILLISECOND", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("NANOSECOND:PETASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("NANOSECOND:PICOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("NANOSECOND:SECOND", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("NANOSECOND:TERASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("NANOSECOND:YOCTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("NANOSECOND:YOTTASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("NANOSECOND:ZEPTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("NANOSECOND:ZETTASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("PETASECOND:ATOOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("PETASECOND:CENTISECOND", new double[][]{new double[]{0, 1}, new double[]{10, 17}});
        c.put("PETASECOND:DECISECOND", new double[][]{new double[]{0, 1}, new double[]{10, 16}});
        c.put("PETASECOND:DEKASECOND", new double[][]{new double[]{0, 1}, new double[]{10, 14}});
        c.put("PETASECOND:EXASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("PETASECOND:FEMTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("PETASECOND:GIGASECOND", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("PETASECOND:HECTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 13}});
        c.put("PETASECOND:KILOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("PETASECOND:MEGASECOND", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("PETASECOND:MICROSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("PETASECOND:MILLISECOND", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("PETASECOND:NANOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("PETASECOND:PICOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("PETASECOND:SECOND", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("PETASECOND:TERASECOND", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("PETASECOND:YOCTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 39}});
        c.put("PETASECOND:YOTTASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("PETASECOND:ZEPTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 36}});
        c.put("PETASECOND:ZETTASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("PICOSECOND:ATOOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("PICOSECOND:CENTISECOND", new double[][]{new double[]{0, 1}, new double[]{10, -10}});
        c.put("PICOSECOND:DECISECOND", new double[][]{new double[]{0, 1}, new double[]{10, -11}});
        c.put("PICOSECOND:DEKASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -13}});
        c.put("PICOSECOND:EXASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("PICOSECOND:FEMTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("PICOSECOND:GIGASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("PICOSECOND:HECTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, -14}});
        c.put("PICOSECOND:KILOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("PICOSECOND:MEGASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("PICOSECOND:MICROSECOND", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("PICOSECOND:MILLISECOND", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("PICOSECOND:NANOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("PICOSECOND:PETASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("PICOSECOND:SECOND", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("PICOSECOND:TERASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("PICOSECOND:YOCTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("PICOSECOND:YOTTASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -36}});
        c.put("PICOSECOND:ZEPTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("PICOSECOND:ZETTASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("SECOND:ATOOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("SECOND:CENTISECOND", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("SECOND:DECISECOND", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("SECOND:DEKASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("SECOND:EXASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("SECOND:FEMTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("SECOND:GIGASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("SECOND:HECTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("SECOND:KILOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("SECOND:MEGASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("SECOND:MICROSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("SECOND:MILLISECOND", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("SECOND:NANOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("SECOND:PETASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("SECOND:PICOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("SECOND:TERASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("SECOND:YOCTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("SECOND:YOTTASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("SECOND:ZEPTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("SECOND:ZETTASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("TERASECOND:ATOOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("TERASECOND:CENTISECOND", new double[][]{new double[]{0, 1}, new double[]{10, 14}});
        c.put("TERASECOND:DECISECOND", new double[][]{new double[]{0, 1}, new double[]{10, 13}});
        c.put("TERASECOND:DEKASECOND", new double[][]{new double[]{0, 1}, new double[]{10, 11}});
        c.put("TERASECOND:EXASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("TERASECOND:FEMTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("TERASECOND:GIGASECOND", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("TERASECOND:HECTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 10}});
        c.put("TERASECOND:KILOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("TERASECOND:MEGASECOND", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("TERASECOND:MICROSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("TERASECOND:MILLISECOND", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("TERASECOND:NANOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("TERASECOND:PETASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("TERASECOND:PICOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("TERASECOND:SECOND", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("TERASECOND:YOCTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 36}});
        c.put("TERASECOND:YOTTASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("TERASECOND:ZEPTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("TERASECOND:ZETTASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("YOCTOSECOND:ATOOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("YOCTOSECOND:CENTISECOND", new double[][]{new double[]{0, 1}, new double[]{10, -22}});
        c.put("YOCTOSECOND:DECISECOND", new double[][]{new double[]{0, 1}, new double[]{10, -23}});
        c.put("YOCTOSECOND:DEKASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -25}});
        c.put("YOCTOSECOND:EXASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -42}});
        c.put("YOCTOSECOND:FEMTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("YOCTOSECOND:GIGASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("YOCTOSECOND:HECTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, -26}});
        c.put("YOCTOSECOND:KILOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("YOCTOSECOND:MEGASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("YOCTOSECOND:MICROSECOND", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("YOCTOSECOND:MILLISECOND", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("YOCTOSECOND:NANOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("YOCTOSECOND:PETASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -39}});
        c.put("YOCTOSECOND:PICOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("YOCTOSECOND:SECOND", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("YOCTOSECOND:TERASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -36}});
        c.put("YOCTOSECOND:YOTTASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -48}});
        c.put("YOCTOSECOND:ZEPTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("YOCTOSECOND:ZETTASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -45}});
        c.put("YOTTASECOND:ATOOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 42}});
        c.put("YOTTASECOND:CENTISECOND", new double[][]{new double[]{0, 1}, new double[]{10, 26}});
        c.put("YOTTASECOND:DECISECOND", new double[][]{new double[]{0, 1}, new double[]{10, 25}});
        c.put("YOTTASECOND:DEKASECOND", new double[][]{new double[]{0, 1}, new double[]{10, 23}});
        c.put("YOTTASECOND:EXASECOND", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("YOTTASECOND:FEMTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 39}});
        c.put("YOTTASECOND:GIGASECOND", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("YOTTASECOND:HECTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 22}});
        c.put("YOTTASECOND:KILOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("YOTTASECOND:MEGASECOND", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("YOTTASECOND:MICROSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("YOTTASECOND:MILLISECOND", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("YOTTASECOND:NANOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("YOTTASECOND:PETASECOND", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("YOTTASECOND:PICOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 36}});
        c.put("YOTTASECOND:SECOND", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("YOTTASECOND:TERASECOND", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("YOTTASECOND:YOCTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 48}});
        c.put("YOTTASECOND:ZEPTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 45}});
        c.put("YOTTASECOND:ZETTASECOND", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("ZEPTOSECOND:ATOOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("ZEPTOSECOND:CENTISECOND", new double[][]{new double[]{0, 1}, new double[]{10, -19}});
        c.put("ZEPTOSECOND:DECISECOND", new double[][]{new double[]{0, 1}, new double[]{10, -20}});
        c.put("ZEPTOSECOND:DEKASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -22}});
        c.put("ZEPTOSECOND:EXASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -39}});
        c.put("ZEPTOSECOND:FEMTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("ZEPTOSECOND:GIGASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("ZEPTOSECOND:HECTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, -23}});
        c.put("ZEPTOSECOND:KILOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("ZEPTOSECOND:MEGASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("ZEPTOSECOND:MICROSECOND", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("ZEPTOSECOND:MILLISECOND", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("ZEPTOSECOND:NANOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("ZEPTOSECOND:PETASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -36}});
        c.put("ZEPTOSECOND:PICOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("ZEPTOSECOND:SECOND", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("ZEPTOSECOND:TERASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("ZEPTOSECOND:YOCTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("ZEPTOSECOND:YOTTASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -45}});
        c.put("ZEPTOSECOND:ZETTASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -42}});
        c.put("ZETTASECOND:ATOOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 39}});
        c.put("ZETTASECOND:CENTISECOND", new double[][]{new double[]{0, 1}, new double[]{10, 23}});
        c.put("ZETTASECOND:DECISECOND", new double[][]{new double[]{0, 1}, new double[]{10, 22}});
        c.put("ZETTASECOND:DEKASECOND", new double[][]{new double[]{0, 1}, new double[]{10, 20}});
        c.put("ZETTASECOND:EXASECOND", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("ZETTASECOND:FEMTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 36}});
        c.put("ZETTASECOND:GIGASECOND", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("ZETTASECOND:HECTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 19}});
        c.put("ZETTASECOND:KILOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("ZETTASECOND:MEGASECOND", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("ZETTASECOND:MICROSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("ZETTASECOND:MILLISECOND", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("ZETTASECOND:NANOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("ZETTASECOND:PETASECOND", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("ZETTASECOND:PICOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("ZETTASECOND:SECOND", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("ZETTASECOND:TERASECOND", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("ZETTASECOND:YOCTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 45}});
        c.put("ZETTASECOND:YOTTASECOND", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("ZETTASECOND:ZEPTOSECOND", new double[][]{new double[]{0, 1}, new double[]{10, 42}});
        conversions = Collections.unmodifiableMap(c);
    }

    private static final Map<UnitsTime, String> SYMBOLS;
    static {
        Map<UnitsTime, String> s = new HashMap<UnitsTime, String>();
        s.put(UnitsTime.ATOOSECOND, "as");
        s.put(UnitsTime.CENTISECOND, "cs");
        s.put(UnitsTime.DAY, "d");
        s.put(UnitsTime.DECISECOND, "ds");
        s.put(UnitsTime.DEKASECOND, "das");
        s.put(UnitsTime.EXASECOND, "Es");
        s.put(UnitsTime.FEMTOSECOND, "fs");
        s.put(UnitsTime.GIGASECOND, "Gs");
        s.put(UnitsTime.HECTOSECOND, "hs");
        s.put(UnitsTime.HOUR, "h");
        s.put(UnitsTime.KILOSECOND, "ks");
        s.put(UnitsTime.MEGASECOND, "Ms");
        s.put(UnitsTime.MICROSECOND, "µs");
        s.put(UnitsTime.MILLISECOND, "ms");
        s.put(UnitsTime.MINUTE, "min");
        s.put(UnitsTime.NANOSECOND, "ns");
        s.put(UnitsTime.PETASECOND, "Ps");
        s.put(UnitsTime.PICOSECOND, "ps");
        s.put(UnitsTime.SECOND, "s");
        s.put(UnitsTime.TERASECOND, "Ts");
        s.put(UnitsTime.YOCTOSECOND, "ys");
        s.put(UnitsTime.YOTTASECOND, "Ys");
        s.put(UnitsTime.ZEPTOSECOND, "zs");
        s.put(UnitsTime.ZETTASECOND, "Zs");
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

