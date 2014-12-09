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

import omero.model.enums.UnitsElectricPotential;

/**
 * Blitz wrapper around the {@link ome.model.units.ElectricPotential} class.
 * Like {@link Details} and {@link Permissions}, this object
 * is embedded into other objects and does not have a full life
 * cycle of its own.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 */
public class ElectricPotentialI extends ElectricPotential implements ModelBased {

    private static final long serialVersionUID = 1L;

    private static final Map<String, double[][]> conversions;
    static {
        Map<String, double[][]> c = new HashMap<String, double[][]>();

        c.put("ATTOVOLT:CENTIVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -16}});
        c.put("ATTOVOLT:DECIVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -17}});
        c.put("ATTOVOLT:DEKAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -19}});
        c.put("ATTOVOLT:EXAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -36}});
        c.put("ATTOVOLT:FEMTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("ATTOVOLT:GIGAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("ATTOVOLT:HECTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -20}});
        c.put("ATTOVOLT:KILOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("ATTOVOLT:MEGAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("ATTOVOLT:MICROVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("ATTOVOLT:MILLIVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("ATTOVOLT:NANOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("ATTOVOLT:PETAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("ATTOVOLT:PICOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("ATTOVOLT:TERAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("ATTOVOLT:VOLT", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("ATTOVOLT:YOCTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("ATTOVOLT:YOTTAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -42}});
        c.put("ATTOVOLT:ZEPTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("ATTOVOLT:ZETTAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -39}});
        c.put("CENTIVOLT:ATTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 16}});
        c.put("CENTIVOLT:DECIVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("CENTIVOLT:DEKAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("CENTIVOLT:EXAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -20}});
        c.put("CENTIVOLT:FEMTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 13}});
        c.put("CENTIVOLT:GIGAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -11}});
        c.put("CENTIVOLT:HECTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("CENTIVOLT:KILOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -5}});
        c.put("CENTIVOLT:MEGAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -8}});
        c.put("CENTIVOLT:MICROVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("CENTIVOLT:MILLIVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("CENTIVOLT:NANOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 7}});
        c.put("CENTIVOLT:PETAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -17}});
        c.put("CENTIVOLT:PICOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 10}});
        c.put("CENTIVOLT:TERAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -14}});
        c.put("CENTIVOLT:VOLT", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("CENTIVOLT:YOCTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 22}});
        c.put("CENTIVOLT:YOTTAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -26}});
        c.put("CENTIVOLT:ZEPTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 19}});
        c.put("CENTIVOLT:ZETTAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -23}});
        c.put("DECIVOLT:ATTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 17}});
        c.put("DECIVOLT:CENTIVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("DECIVOLT:DEKAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("DECIVOLT:EXAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -19}});
        c.put("DECIVOLT:FEMTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 14}});
        c.put("DECIVOLT:GIGAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -10}});
        c.put("DECIVOLT:HECTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("DECIVOLT:KILOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("DECIVOLT:MEGAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -7}});
        c.put("DECIVOLT:MICROVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 5}});
        c.put("DECIVOLT:MILLIVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("DECIVOLT:NANOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 8}});
        c.put("DECIVOLT:PETAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -16}});
        c.put("DECIVOLT:PICOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 11}});
        c.put("DECIVOLT:TERAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -13}});
        c.put("DECIVOLT:VOLT", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("DECIVOLT:YOCTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 23}});
        c.put("DECIVOLT:YOTTAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -25}});
        c.put("DECIVOLT:ZEPTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 20}});
        c.put("DECIVOLT:ZETTAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -22}});
        c.put("DEKAVOLT:ATTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 19}});
        c.put("DEKAVOLT:CENTIVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("DEKAVOLT:DECIVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("DEKAVOLT:EXAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -17}});
        c.put("DEKAVOLT:FEMTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 16}});
        c.put("DEKAVOLT:GIGAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -8}});
        c.put("DEKAVOLT:HECTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("DEKAVOLT:KILOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("DEKAVOLT:MEGAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -5}});
        c.put("DEKAVOLT:MICROVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 7}});
        c.put("DEKAVOLT:MILLIVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("DEKAVOLT:NANOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 10}});
        c.put("DEKAVOLT:PETAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -14}});
        c.put("DEKAVOLT:PICOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 13}});
        c.put("DEKAVOLT:TERAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -11}});
        c.put("DEKAVOLT:VOLT", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("DEKAVOLT:YOCTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 25}});
        c.put("DEKAVOLT:YOTTAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -23}});
        c.put("DEKAVOLT:ZEPTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 22}});
        c.put("DEKAVOLT:ZETTAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -20}});
        c.put("EXAVOLT:ATTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 36}});
        c.put("EXAVOLT:CENTIVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 20}});
        c.put("EXAVOLT:DECIVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 19}});
        c.put("EXAVOLT:DEKAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 17}});
        c.put("EXAVOLT:FEMTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("EXAVOLT:GIGAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("EXAVOLT:HECTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 16}});
        c.put("EXAVOLT:KILOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("EXAVOLT:MEGAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("EXAVOLT:MICROVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("EXAVOLT:MILLIVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("EXAVOLT:NANOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("EXAVOLT:PETAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("EXAVOLT:PICOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("EXAVOLT:TERAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("EXAVOLT:VOLT", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("EXAVOLT:YOCTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 42}});
        c.put("EXAVOLT:YOTTAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("EXAVOLT:ZEPTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 39}});
        c.put("EXAVOLT:ZETTAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("FEMTOVOLT:ATTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("FEMTOVOLT:CENTIVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -13}});
        c.put("FEMTOVOLT:DECIVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -14}});
        c.put("FEMTOVOLT:DEKAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -16}});
        c.put("FEMTOVOLT:EXAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("FEMTOVOLT:GIGAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("FEMTOVOLT:HECTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -17}});
        c.put("FEMTOVOLT:KILOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("FEMTOVOLT:MEGAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("FEMTOVOLT:MICROVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("FEMTOVOLT:MILLIVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("FEMTOVOLT:NANOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("FEMTOVOLT:PETAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("FEMTOVOLT:PICOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("FEMTOVOLT:TERAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("FEMTOVOLT:VOLT", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("FEMTOVOLT:YOCTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("FEMTOVOLT:YOTTAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -39}});
        c.put("FEMTOVOLT:ZEPTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("FEMTOVOLT:ZETTAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -36}});
        c.put("GIGAVOLT:ATTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("GIGAVOLT:CENTIVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 11}});
        c.put("GIGAVOLT:DECIVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 10}});
        c.put("GIGAVOLT:DEKAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 8}});
        c.put("GIGAVOLT:EXAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("GIGAVOLT:FEMTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("GIGAVOLT:HECTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 7}});
        c.put("GIGAVOLT:KILOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("GIGAVOLT:MEGAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("GIGAVOLT:MICROVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("GIGAVOLT:MILLIVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("GIGAVOLT:NANOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("GIGAVOLT:PETAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("GIGAVOLT:PICOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("GIGAVOLT:TERAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("GIGAVOLT:VOLT", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("GIGAVOLT:YOCTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("GIGAVOLT:YOTTAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("GIGAVOLT:ZEPTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("GIGAVOLT:ZETTAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("HECTOVOLT:ATTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 20}});
        c.put("HECTOVOLT:CENTIVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("HECTOVOLT:DECIVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("HECTOVOLT:DEKAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("HECTOVOLT:EXAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -16}});
        c.put("HECTOVOLT:FEMTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 17}});
        c.put("HECTOVOLT:GIGAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -7}});
        c.put("HECTOVOLT:KILOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("HECTOVOLT:MEGAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("HECTOVOLT:MICROVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 8}});
        c.put("HECTOVOLT:MILLIVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 5}});
        c.put("HECTOVOLT:NANOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 11}});
        c.put("HECTOVOLT:PETAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -13}});
        c.put("HECTOVOLT:PICOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 14}});
        c.put("HECTOVOLT:TERAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -10}});
        c.put("HECTOVOLT:VOLT", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("HECTOVOLT:YOCTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 26}});
        c.put("HECTOVOLT:YOTTAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -22}});
        c.put("HECTOVOLT:ZEPTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 23}});
        c.put("HECTOVOLT:ZETTAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -19}});
        c.put("KILOVOLT:ATTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("KILOVOLT:CENTIVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 5}});
        c.put("KILOVOLT:DECIVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("KILOVOLT:DEKAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("KILOVOLT:EXAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("KILOVOLT:FEMTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("KILOVOLT:GIGAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("KILOVOLT:HECTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("KILOVOLT:MEGAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("KILOVOLT:MICROVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("KILOVOLT:MILLIVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("KILOVOLT:NANOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("KILOVOLT:PETAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("KILOVOLT:PICOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("KILOVOLT:TERAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("KILOVOLT:VOLT", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("KILOVOLT:YOCTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("KILOVOLT:YOTTAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("KILOVOLT:ZEPTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("KILOVOLT:ZETTAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("MEGAVOLT:ATTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("MEGAVOLT:CENTIVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 8}});
        c.put("MEGAVOLT:DECIVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 7}});
        c.put("MEGAVOLT:DEKAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 5}});
        c.put("MEGAVOLT:EXAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("MEGAVOLT:FEMTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("MEGAVOLT:GIGAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("MEGAVOLT:HECTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("MEGAVOLT:KILOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("MEGAVOLT:MICROVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("MEGAVOLT:MILLIVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("MEGAVOLT:NANOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("MEGAVOLT:PETAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("MEGAVOLT:PICOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("MEGAVOLT:TERAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("MEGAVOLT:VOLT", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("MEGAVOLT:YOCTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("MEGAVOLT:YOTTAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("MEGAVOLT:ZEPTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("MEGAVOLT:ZETTAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("MICROVOLT:ATTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("MICROVOLT:CENTIVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("MICROVOLT:DECIVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -5}});
        c.put("MICROVOLT:DEKAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -7}});
        c.put("MICROVOLT:EXAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("MICROVOLT:FEMTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("MICROVOLT:GIGAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("MICROVOLT:HECTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -8}});
        c.put("MICROVOLT:KILOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("MICROVOLT:MEGAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("MICROVOLT:MILLIVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("MICROVOLT:NANOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("MICROVOLT:PETAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("MICROVOLT:PICOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("MICROVOLT:TERAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("MICROVOLT:VOLT", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("MICROVOLT:YOCTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("MICROVOLT:YOTTAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("MICROVOLT:ZEPTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("MICROVOLT:ZETTAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("MILLIVOLT:ATTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("MILLIVOLT:CENTIVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("MILLIVOLT:DECIVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("MILLIVOLT:DEKAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("MILLIVOLT:EXAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("MILLIVOLT:FEMTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("MILLIVOLT:GIGAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("MILLIVOLT:HECTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -5}});
        c.put("MILLIVOLT:KILOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("MILLIVOLT:MEGAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("MILLIVOLT:MICROVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("MILLIVOLT:NANOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("MILLIVOLT:PETAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("MILLIVOLT:PICOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("MILLIVOLT:TERAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("MILLIVOLT:VOLT", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("MILLIVOLT:YOCTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("MILLIVOLT:YOTTAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("MILLIVOLT:ZEPTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("MILLIVOLT:ZETTAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("NANOVOLT:ATTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("NANOVOLT:CENTIVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -7}});
        c.put("NANOVOLT:DECIVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -8}});
        c.put("NANOVOLT:DEKAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -10}});
        c.put("NANOVOLT:EXAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("NANOVOLT:FEMTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("NANOVOLT:GIGAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("NANOVOLT:HECTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -11}});
        c.put("NANOVOLT:KILOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("NANOVOLT:MEGAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("NANOVOLT:MICROVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("NANOVOLT:MILLIVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("NANOVOLT:PETAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("NANOVOLT:PICOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("NANOVOLT:TERAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("NANOVOLT:VOLT", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("NANOVOLT:YOCTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("NANOVOLT:YOTTAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("NANOVOLT:ZEPTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("NANOVOLT:ZETTAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("PETAVOLT:ATTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("PETAVOLT:CENTIVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 17}});
        c.put("PETAVOLT:DECIVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 16}});
        c.put("PETAVOLT:DEKAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 14}});
        c.put("PETAVOLT:EXAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("PETAVOLT:FEMTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("PETAVOLT:GIGAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("PETAVOLT:HECTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 13}});
        c.put("PETAVOLT:KILOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("PETAVOLT:MEGAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("PETAVOLT:MICROVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("PETAVOLT:MILLIVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("PETAVOLT:NANOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("PETAVOLT:PICOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("PETAVOLT:TERAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("PETAVOLT:VOLT", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("PETAVOLT:YOCTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 39}});
        c.put("PETAVOLT:YOTTAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("PETAVOLT:ZEPTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 36}});
        c.put("PETAVOLT:ZETTAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("PICOVOLT:ATTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("PICOVOLT:CENTIVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -10}});
        c.put("PICOVOLT:DECIVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -11}});
        c.put("PICOVOLT:DEKAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -13}});
        c.put("PICOVOLT:EXAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("PICOVOLT:FEMTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("PICOVOLT:GIGAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("PICOVOLT:HECTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -14}});
        c.put("PICOVOLT:KILOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("PICOVOLT:MEGAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("PICOVOLT:MICROVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("PICOVOLT:MILLIVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("PICOVOLT:NANOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("PICOVOLT:PETAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("PICOVOLT:TERAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("PICOVOLT:VOLT", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("PICOVOLT:YOCTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("PICOVOLT:YOTTAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -36}});
        c.put("PICOVOLT:ZEPTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("PICOVOLT:ZETTAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("TERAVOLT:ATTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("TERAVOLT:CENTIVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 14}});
        c.put("TERAVOLT:DECIVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 13}});
        c.put("TERAVOLT:DEKAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 11}});
        c.put("TERAVOLT:EXAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("TERAVOLT:FEMTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("TERAVOLT:GIGAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("TERAVOLT:HECTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 10}});
        c.put("TERAVOLT:KILOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("TERAVOLT:MEGAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("TERAVOLT:MICROVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("TERAVOLT:MILLIVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("TERAVOLT:NANOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("TERAVOLT:PETAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("TERAVOLT:PICOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("TERAVOLT:VOLT", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("TERAVOLT:YOCTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 36}});
        c.put("TERAVOLT:YOTTAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("TERAVOLT:ZEPTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("TERAVOLT:ZETTAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("VOLT:ATTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("VOLT:CENTIVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("VOLT:DECIVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("VOLT:DEKAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("VOLT:EXAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("VOLT:FEMTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("VOLT:GIGAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("VOLT:HECTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("VOLT:KILOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("VOLT:MEGAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("VOLT:MICROVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("VOLT:MILLIVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("VOLT:NANOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("VOLT:PETAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("VOLT:PICOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("VOLT:TERAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("VOLT:YOCTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("VOLT:YOTTAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("VOLT:ZEPTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("VOLT:ZETTAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("YOCTOVOLT:ATTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("YOCTOVOLT:CENTIVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -22}});
        c.put("YOCTOVOLT:DECIVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -23}});
        c.put("YOCTOVOLT:DEKAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -25}});
        c.put("YOCTOVOLT:EXAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -42}});
        c.put("YOCTOVOLT:FEMTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("YOCTOVOLT:GIGAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("YOCTOVOLT:HECTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -26}});
        c.put("YOCTOVOLT:KILOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("YOCTOVOLT:MEGAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("YOCTOVOLT:MICROVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("YOCTOVOLT:MILLIVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("YOCTOVOLT:NANOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("YOCTOVOLT:PETAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -39}});
        c.put("YOCTOVOLT:PICOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("YOCTOVOLT:TERAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -36}});
        c.put("YOCTOVOLT:VOLT", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("YOCTOVOLT:YOTTAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -48}});
        c.put("YOCTOVOLT:ZEPTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("YOCTOVOLT:ZETTAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -45}});
        c.put("YOTTAVOLT:ATTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 42}});
        c.put("YOTTAVOLT:CENTIVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 26}});
        c.put("YOTTAVOLT:DECIVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 25}});
        c.put("YOTTAVOLT:DEKAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 23}});
        c.put("YOTTAVOLT:EXAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("YOTTAVOLT:FEMTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 39}});
        c.put("YOTTAVOLT:GIGAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("YOTTAVOLT:HECTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 22}});
        c.put("YOTTAVOLT:KILOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("YOTTAVOLT:MEGAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("YOTTAVOLT:MICROVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("YOTTAVOLT:MILLIVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("YOTTAVOLT:NANOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("YOTTAVOLT:PETAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("YOTTAVOLT:PICOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 36}});
        c.put("YOTTAVOLT:TERAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("YOTTAVOLT:VOLT", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("YOTTAVOLT:YOCTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 48}});
        c.put("YOTTAVOLT:ZEPTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 45}});
        c.put("YOTTAVOLT:ZETTAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("ZEPTOVOLT:ATTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("ZEPTOVOLT:CENTIVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -19}});
        c.put("ZEPTOVOLT:DECIVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -20}});
        c.put("ZEPTOVOLT:DEKAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -22}});
        c.put("ZEPTOVOLT:EXAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -39}});
        c.put("ZEPTOVOLT:FEMTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("ZEPTOVOLT:GIGAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("ZEPTOVOLT:HECTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -23}});
        c.put("ZEPTOVOLT:KILOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("ZEPTOVOLT:MEGAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("ZEPTOVOLT:MICROVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("ZEPTOVOLT:MILLIVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("ZEPTOVOLT:NANOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("ZEPTOVOLT:PETAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -36}});
        c.put("ZEPTOVOLT:PICOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("ZEPTOVOLT:TERAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("ZEPTOVOLT:VOLT", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("ZEPTOVOLT:YOCTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("ZEPTOVOLT:YOTTAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -45}});
        c.put("ZEPTOVOLT:ZETTAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -42}});
        c.put("ZETTAVOLT:ATTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 39}});
        c.put("ZETTAVOLT:CENTIVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 23}});
        c.put("ZETTAVOLT:DECIVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 22}});
        c.put("ZETTAVOLT:DEKAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 20}});
        c.put("ZETTAVOLT:EXAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("ZETTAVOLT:FEMTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 36}});
        c.put("ZETTAVOLT:GIGAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("ZETTAVOLT:HECTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 19}});
        c.put("ZETTAVOLT:KILOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("ZETTAVOLT:MEGAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("ZETTAVOLT:MICROVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("ZETTAVOLT:MILLIVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("ZETTAVOLT:NANOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("ZETTAVOLT:PETAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("ZETTAVOLT:PICOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("ZETTAVOLT:TERAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("ZETTAVOLT:VOLT", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("ZETTAVOLT:YOCTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 45}});
        c.put("ZETTAVOLT:YOTTAVOLT", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("ZETTAVOLT:ZEPTOVOLT", new double[][]{new double[]{0, 1}, new double[]{10, 42}});
        conversions = Collections.unmodifiableMap(c);
    }

    private static final Map<UnitsElectricPotential, String> SYMBOLS;
    static {
        Map<UnitsElectricPotential, String> s = new HashMap<UnitsElectricPotential, String>();
        s.put(UnitsElectricPotential.ATTOVOLT, "aV");
        s.put(UnitsElectricPotential.CENTIVOLT, "cV");
        s.put(UnitsElectricPotential.DECIVOLT, "dV");
        s.put(UnitsElectricPotential.DEKAVOLT, "daV");
        s.put(UnitsElectricPotential.EXAVOLT, "EV");
        s.put(UnitsElectricPotential.FEMTOVOLT, "fV");
        s.put(UnitsElectricPotential.GIGAVOLT, "GV");
        s.put(UnitsElectricPotential.HECTOVOLT, "hV");
        s.put(UnitsElectricPotential.KILOVOLT, "kV");
        s.put(UnitsElectricPotential.MEGAVOLT, "MV");
        s.put(UnitsElectricPotential.MICROVOLT, "µV");
        s.put(UnitsElectricPotential.MILLIVOLT, "mV");
        s.put(UnitsElectricPotential.NANOVOLT, "nV");
        s.put(UnitsElectricPotential.PETAVOLT, "PV");
        s.put(UnitsElectricPotential.PICOVOLT, "pV");
        s.put(UnitsElectricPotential.TERAVOLT, "TV");
        s.put(UnitsElectricPotential.VOLT, "V");
        s.put(UnitsElectricPotential.YOCTOVOLT, "yV");
        s.put(UnitsElectricPotential.YOTTAVOLT, "YV");
        s.put(UnitsElectricPotential.ZEPTOVOLT, "zV");
        s.put(UnitsElectricPotential.ZETTAVOLT, "ZV");
        SYMBOLS = s;
    }

    public static String lookupSymbol(UnitsElectricPotential unit) {
        return SYMBOLS.get(unit);
    }

    public static final Ice.ObjectFactory makeFactory(final omero.client client) {

        return new Ice.ObjectFactory() {

            public Ice.Object create(String arg0) {
                return new ElectricPotentialI();
            }

            public void destroy() {
                // no-op
            }

        };
    };

    //
    // CONVERSIONS
    //

    public static ome.xml.model.enums.UnitsElectricPotential makeXMLUnit(String unit) {
        try {
            return ome.xml.model.enums.UnitsElectricPotential
                    .fromString((String) unit);
        } catch (EnumerationException e) {
            throw new RuntimeException("Bad ElectricPotential unit: " + unit, e);
        }
    }

    public static ome.units.quantity.ElectricPotential makeXMLQuantity(double d, String unit) {
        ome.units.unit.Unit<ome.units.quantity.ElectricPotential> units =
                ome.xml.model.enums.handlers.UnitsElectricPotentialEnumHandler
                        .getBaseUnit(makeXMLUnit(unit));
        return new ome.units.quantity.ElectricPotential(d, units);
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
   public static ome.units.quantity.ElectricPotential convert(ElectricPotential t) {
       if (t == null) {
           return null;
       }

       Double v = t.getValue();
       // Use the code/symbol-mapping in the ome.model.enums files
       // to convert to the specification value.
       String u = ome.model.enums.UnitsElectricPotential.valueOf(
               t.getUnit().toString()).getSymbol();
       ome.xml.model.enums.UnitsElectricPotential units = makeXMLUnit(u);
       ome.units.unit.Unit<ome.units.quantity.ElectricPotential> units2 =
               ome.xml.model.enums.handlers.UnitsElectricPotentialEnumHandler
                       .getBaseUnit(units);

       return new ome.units.quantity.ElectricPotential(v, units2);
   }


    //
    // REGULAR ICE CLASS
    //

    public final static Ice.ObjectFactory Factory = makeFactory(null);

    public ElectricPotentialI() {
        super();
    }

    public ElectricPotentialI(double d, UnitsElectricPotential unit) {
        super();
        this.setUnit(unit);
        this.setValue(d);
    }

    public ElectricPotentialI(double d,
            Unit<ome.units.quantity.ElectricPotential> unit) {
        this(d, ome.model.enums.UnitsElectricPotential.bySymbol(unit.getSymbol()));
    }

   /**
    * Copy constructor that converts the given {@link omero.model.ElectricPotential}
    * based on the given ome-xml enum
    */
   public ElectricPotentialI(ElectricPotential value, Unit<ome.units.quantity.ElectricPotential> ul) {
       this(value,
            ome.model.enums.UnitsElectricPotential.bySymbol(ul.getSymbol()).toString());
   }

   /**
    * Copy constructor that converts the given {@link omero.model.ElectricPotential}
    * based on the given ome.model enum
    */
   public ElectricPotentialI(double d, ome.model.enums.UnitsElectricPotential ul) {
        this(d, UnitsElectricPotential.valueOf(ul.toString()));
    }

   /**
    * Copy constructor that converts the given {@link omero.model.ElectricPotential}
    * based on the given enum string.
    *
    * @param target String representation of the CODE enum
    */
    public ElectricPotentialI(ElectricPotential value, String target) {
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
            setUnit(UnitsElectricPotential.valueOf(target));
       }
    }

   /**
    * Copy constructor that converts between units if possible.
    *
    * @param target unit that is desired. non-null.
    */
    public ElectricPotentialI(ElectricPotential value, UnitsElectricPotential target) {
        this(value, target.toString());
    }

    /**
     * Convert a Bio-Formats {@link Length} to an OMERO Length.
     */
    public ElectricPotentialI(ome.units.quantity.ElectricPotential value) {
        ome.model.enums.UnitsElectricPotential internal =
            ome.model.enums.UnitsElectricPotential.bySymbol(value.unit().getSymbol());
        UnitsElectricPotential ul = UnitsElectricPotential.valueOf(internal.toString());
        setValue(value.value().doubleValue());
        setUnit(ul);
    }

    public double getValue(Ice.Current current) {
        return this.value;
    }

    public void setValue(double value , Ice.Current current) {
        this.value = value;
    }

    public UnitsElectricPotential getUnit(Ice.Current current) {
        return this.unit;
    }

    public void setUnit(UnitsElectricPotential unit, Ice.Current current) {
        this.unit = unit;
    }

    public String getSymbol(Ice.Current current) {
        return SYMBOLS.get(this.unit);
    }

    public ElectricPotential copy(Ice.Current ignore) {
        ElectricPotentialI copy = new ElectricPotentialI();
        copy.setValue(getValue());
        copy.setUnit(getUnit());
        return copy;
    }

    @Override
    public void copyObject(Filterable model, ModelMapper mapper) {
        if (model instanceof ome.model.units.ElectricPotential) {
            ome.model.units.ElectricPotential t = (ome.model.units.ElectricPotential) model;
            this.value = t.getValue();
            this.unit = UnitsElectricPotential.valueOf(t.getUnit().toString());
        } else {
            throw new IllegalArgumentException(
              "ElectricPotential cannot copy from " +
              (model==null ? "null" : model.getClass().getName()));
        }
    }

    @Override
    public Filterable fillObject(ReverseModelMapper mapper) {
        ome.model.enums.UnitsElectricPotential ut = ome.model.enums.UnitsElectricPotential.valueOf(getUnit().toString());
        ome.model.units.ElectricPotential t = new ome.model.units.ElectricPotential(getValue(), ut);
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
        return "ElectricPotential(" + value + " " + unit + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ElectricPotential other = (ElectricPotential) obj;
        if (unit != other.unit)
            return false;
        if (Double.doubleToLongBits(value) != Double
                .doubleToLongBits(other.value))
            return false;
        return true;
    }

}

