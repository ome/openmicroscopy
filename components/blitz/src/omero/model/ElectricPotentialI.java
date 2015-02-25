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

import static ome.model.units.Conversion.Mul;
import static ome.model.units.Conversion.Add;
import static ome.model.units.Conversion.Int;
import static ome.model.units.Conversion.Pow;
import static ome.model.units.Conversion.Rat;
import static ome.model.units.Conversion.Sym;

import java.math.BigDecimal;

import java.util.Collections;
import java.util.Map;
import java.util.EnumMap;
import java.util.HashMap;

import ome.model.ModelBased;
import ome.model.units.BigResult;
import ome.model.units.Conversion;
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

    private static final Map<UnitsElectricPotential, Map<UnitsElectricPotential, Conversion>> conversions;
    static {

        EnumMap<UnitsElectricPotential, EnumMap<UnitsElectricPotential, Conversion>> c
            = new EnumMap<UnitsElectricPotential, EnumMap<UnitsElectricPotential, Conversion>>(UnitsElectricPotential.class);

        for (UnitsElectricPotential e : UnitsElectricPotential.values()) {
            c.put(e, new EnumMap<UnitsElectricPotential, Conversion>(UnitsElectricPotential.class));
        }

        c.get(UnitsElectricPotential.ATTOVOLT).put(UnitsElectricPotential.CENTIVOLT, Mul(Pow(10, 16), Sym("attov")));
        c.get(UnitsElectricPotential.ATTOVOLT).put(UnitsElectricPotential.DECAVOLT, Mul(Pow(10, 19), Sym("attov")));
        c.get(UnitsElectricPotential.ATTOVOLT).put(UnitsElectricPotential.DECIVOLT, Mul(Pow(10, 17), Sym("attov")));
        c.get(UnitsElectricPotential.ATTOVOLT).put(UnitsElectricPotential.EXAVOLT, Mul(Pow(10, 36), Sym("attov")));
        c.get(UnitsElectricPotential.ATTOVOLT).put(UnitsElectricPotential.FEMTOVOLT, Mul(Int(1000), Sym("attov")));
        c.get(UnitsElectricPotential.ATTOVOLT).put(UnitsElectricPotential.GIGAVOLT, Mul(Pow(10, 27), Sym("attov")));
        c.get(UnitsElectricPotential.ATTOVOLT).put(UnitsElectricPotential.HECTOVOLT, Mul(Pow(10, 20), Sym("attov")));
        c.get(UnitsElectricPotential.ATTOVOLT).put(UnitsElectricPotential.KILOVOLT, Mul(Pow(10, 21), Sym("attov")));
        c.get(UnitsElectricPotential.ATTOVOLT).put(UnitsElectricPotential.MEGAVOLT, Mul(Pow(10, 24), Sym("attov")));
        c.get(UnitsElectricPotential.ATTOVOLT).put(UnitsElectricPotential.MICROVOLT, Mul(Pow(10, 12), Sym("attov")));
        c.get(UnitsElectricPotential.ATTOVOLT).put(UnitsElectricPotential.MILLIVOLT, Mul(Pow(10, 15), Sym("attov")));
        c.get(UnitsElectricPotential.ATTOVOLT).put(UnitsElectricPotential.NANOVOLT, Mul(Pow(10, 9), Sym("attov")));
        c.get(UnitsElectricPotential.ATTOVOLT).put(UnitsElectricPotential.PETAVOLT, Mul(Pow(10, 33), Sym("attov")));
        c.get(UnitsElectricPotential.ATTOVOLT).put(UnitsElectricPotential.PICOVOLT, Mul(Pow(10, 6), Sym("attov")));
        c.get(UnitsElectricPotential.ATTOVOLT).put(UnitsElectricPotential.TERAVOLT, Mul(Pow(10, 30), Sym("attov")));
        c.get(UnitsElectricPotential.ATTOVOLT).put(UnitsElectricPotential.VOLT, Mul(Pow(10, 18), Sym("attov")));
        c.get(UnitsElectricPotential.ATTOVOLT).put(UnitsElectricPotential.YOCTOVOLT, Mul(Rat(Int(1), Pow(10, 6)), Sym("attov")));
        c.get(UnitsElectricPotential.ATTOVOLT).put(UnitsElectricPotential.YOTTAVOLT, Mul(Pow(10, 42), Sym("attov")));
        c.get(UnitsElectricPotential.ATTOVOLT).put(UnitsElectricPotential.ZEPTOVOLT, Mul(Rat(Int(1), Int(1000)), Sym("attov")));
        c.get(UnitsElectricPotential.ATTOVOLT).put(UnitsElectricPotential.ZETTAVOLT, Mul(Pow(10, 39), Sym("attov")));
        c.get(UnitsElectricPotential.CENTIVOLT).put(UnitsElectricPotential.ATTOVOLT, Mul(Rat(Int(1), Pow(10, 16)), Sym("centiv")));
        c.get(UnitsElectricPotential.CENTIVOLT).put(UnitsElectricPotential.DECAVOLT, Mul(Int(1000), Sym("centiv")));
        c.get(UnitsElectricPotential.CENTIVOLT).put(UnitsElectricPotential.DECIVOLT, Mul(Int(10), Sym("centiv")));
        c.get(UnitsElectricPotential.CENTIVOLT).put(UnitsElectricPotential.EXAVOLT, Mul(Pow(10, 20), Sym("centiv")));
        c.get(UnitsElectricPotential.CENTIVOLT).put(UnitsElectricPotential.FEMTOVOLT, Mul(Rat(Int(1), Pow(10, 13)), Sym("centiv")));
        c.get(UnitsElectricPotential.CENTIVOLT).put(UnitsElectricPotential.GIGAVOLT, Mul(Pow(10, 11), Sym("centiv")));
        c.get(UnitsElectricPotential.CENTIVOLT).put(UnitsElectricPotential.HECTOVOLT, Mul(Pow(10, 4), Sym("centiv")));
        c.get(UnitsElectricPotential.CENTIVOLT).put(UnitsElectricPotential.KILOVOLT, Mul(Pow(10, 5), Sym("centiv")));
        c.get(UnitsElectricPotential.CENTIVOLT).put(UnitsElectricPotential.MEGAVOLT, Mul(Pow(10, 8), Sym("centiv")));
        c.get(UnitsElectricPotential.CENTIVOLT).put(UnitsElectricPotential.MICROVOLT, Mul(Rat(Int(1), Pow(10, 4)), Sym("centiv")));
        c.get(UnitsElectricPotential.CENTIVOLT).put(UnitsElectricPotential.MILLIVOLT, Mul(Rat(Int(1), Int(10)), Sym("centiv")));
        c.get(UnitsElectricPotential.CENTIVOLT).put(UnitsElectricPotential.NANOVOLT, Mul(Rat(Int(1), Pow(10, 7)), Sym("centiv")));
        c.get(UnitsElectricPotential.CENTIVOLT).put(UnitsElectricPotential.PETAVOLT, Mul(Pow(10, 17), Sym("centiv")));
        c.get(UnitsElectricPotential.CENTIVOLT).put(UnitsElectricPotential.PICOVOLT, Mul(Rat(Int(1), Pow(10, 10)), Sym("centiv")));
        c.get(UnitsElectricPotential.CENTIVOLT).put(UnitsElectricPotential.TERAVOLT, Mul(Pow(10, 14), Sym("centiv")));
        c.get(UnitsElectricPotential.CENTIVOLT).put(UnitsElectricPotential.VOLT, Mul(Int(100), Sym("centiv")));
        c.get(UnitsElectricPotential.CENTIVOLT).put(UnitsElectricPotential.YOCTOVOLT, Mul(Rat(Int(1), Pow(10, 22)), Sym("centiv")));
        c.get(UnitsElectricPotential.CENTIVOLT).put(UnitsElectricPotential.YOTTAVOLT, Mul(Pow(10, 26), Sym("centiv")));
        c.get(UnitsElectricPotential.CENTIVOLT).put(UnitsElectricPotential.ZEPTOVOLT, Mul(Rat(Int(1), Pow(10, 19)), Sym("centiv")));
        c.get(UnitsElectricPotential.CENTIVOLT).put(UnitsElectricPotential.ZETTAVOLT, Mul(Pow(10, 23), Sym("centiv")));
        c.get(UnitsElectricPotential.DECAVOLT).put(UnitsElectricPotential.ATTOVOLT, Mul(Rat(Int(1), Pow(10, 19)), Sym("decav")));
        c.get(UnitsElectricPotential.DECAVOLT).put(UnitsElectricPotential.CENTIVOLT, Mul(Rat(Int(1), Int(1000)), Sym("decav")));
        c.get(UnitsElectricPotential.DECAVOLT).put(UnitsElectricPotential.DECIVOLT, Mul(Rat(Int(1), Int(100)), Sym("decav")));
        c.get(UnitsElectricPotential.DECAVOLT).put(UnitsElectricPotential.EXAVOLT, Mul(Pow(10, 17), Sym("decav")));
        c.get(UnitsElectricPotential.DECAVOLT).put(UnitsElectricPotential.FEMTOVOLT, Mul(Rat(Int(1), Pow(10, 16)), Sym("decav")));
        c.get(UnitsElectricPotential.DECAVOLT).put(UnitsElectricPotential.GIGAVOLT, Mul(Pow(10, 8), Sym("decav")));
        c.get(UnitsElectricPotential.DECAVOLT).put(UnitsElectricPotential.HECTOVOLT, Mul(Int(10), Sym("decav")));
        c.get(UnitsElectricPotential.DECAVOLT).put(UnitsElectricPotential.KILOVOLT, Mul(Int(100), Sym("decav")));
        c.get(UnitsElectricPotential.DECAVOLT).put(UnitsElectricPotential.MEGAVOLT, Mul(Pow(10, 5), Sym("decav")));
        c.get(UnitsElectricPotential.DECAVOLT).put(UnitsElectricPotential.MICROVOLT, Mul(Rat(Int(1), Pow(10, 7)), Sym("decav")));
        c.get(UnitsElectricPotential.DECAVOLT).put(UnitsElectricPotential.MILLIVOLT, Mul(Rat(Int(1), Pow(10, 4)), Sym("decav")));
        c.get(UnitsElectricPotential.DECAVOLT).put(UnitsElectricPotential.NANOVOLT, Mul(Rat(Int(1), Pow(10, 10)), Sym("decav")));
        c.get(UnitsElectricPotential.DECAVOLT).put(UnitsElectricPotential.PETAVOLT, Mul(Pow(10, 14), Sym("decav")));
        c.get(UnitsElectricPotential.DECAVOLT).put(UnitsElectricPotential.PICOVOLT, Mul(Rat(Int(1), Pow(10, 13)), Sym("decav")));
        c.get(UnitsElectricPotential.DECAVOLT).put(UnitsElectricPotential.TERAVOLT, Mul(Pow(10, 11), Sym("decav")));
        c.get(UnitsElectricPotential.DECAVOLT).put(UnitsElectricPotential.VOLT, Mul(Rat(Int(1), Int(10)), Sym("decav")));
        c.get(UnitsElectricPotential.DECAVOLT).put(UnitsElectricPotential.YOCTOVOLT, Mul(Rat(Int(1), Pow(10, 25)), Sym("decav")));
        c.get(UnitsElectricPotential.DECAVOLT).put(UnitsElectricPotential.YOTTAVOLT, Mul(Pow(10, 23), Sym("decav")));
        c.get(UnitsElectricPotential.DECAVOLT).put(UnitsElectricPotential.ZEPTOVOLT, Mul(Rat(Int(1), Pow(10, 22)), Sym("decav")));
        c.get(UnitsElectricPotential.DECAVOLT).put(UnitsElectricPotential.ZETTAVOLT, Mul(Pow(10, 20), Sym("decav")));
        c.get(UnitsElectricPotential.DECIVOLT).put(UnitsElectricPotential.ATTOVOLT, Mul(Rat(Int(1), Pow(10, 17)), Sym("deciv")));
        c.get(UnitsElectricPotential.DECIVOLT).put(UnitsElectricPotential.CENTIVOLT, Mul(Rat(Int(1), Int(10)), Sym("deciv")));
        c.get(UnitsElectricPotential.DECIVOLT).put(UnitsElectricPotential.DECAVOLT, Mul(Int(100), Sym("deciv")));
        c.get(UnitsElectricPotential.DECIVOLT).put(UnitsElectricPotential.EXAVOLT, Mul(Pow(10, 19), Sym("deciv")));
        c.get(UnitsElectricPotential.DECIVOLT).put(UnitsElectricPotential.FEMTOVOLT, Mul(Rat(Int(1), Pow(10, 14)), Sym("deciv")));
        c.get(UnitsElectricPotential.DECIVOLT).put(UnitsElectricPotential.GIGAVOLT, Mul(Pow(10, 10), Sym("deciv")));
        c.get(UnitsElectricPotential.DECIVOLT).put(UnitsElectricPotential.HECTOVOLT, Mul(Int(1000), Sym("deciv")));
        c.get(UnitsElectricPotential.DECIVOLT).put(UnitsElectricPotential.KILOVOLT, Mul(Pow(10, 4), Sym("deciv")));
        c.get(UnitsElectricPotential.DECIVOLT).put(UnitsElectricPotential.MEGAVOLT, Mul(Pow(10, 7), Sym("deciv")));
        c.get(UnitsElectricPotential.DECIVOLT).put(UnitsElectricPotential.MICROVOLT, Mul(Rat(Int(1), Pow(10, 5)), Sym("deciv")));
        c.get(UnitsElectricPotential.DECIVOLT).put(UnitsElectricPotential.MILLIVOLT, Mul(Rat(Int(1), Int(100)), Sym("deciv")));
        c.get(UnitsElectricPotential.DECIVOLT).put(UnitsElectricPotential.NANOVOLT, Mul(Rat(Int(1), Pow(10, 8)), Sym("deciv")));
        c.get(UnitsElectricPotential.DECIVOLT).put(UnitsElectricPotential.PETAVOLT, Mul(Pow(10, 16), Sym("deciv")));
        c.get(UnitsElectricPotential.DECIVOLT).put(UnitsElectricPotential.PICOVOLT, Mul(Rat(Int(1), Pow(10, 11)), Sym("deciv")));
        c.get(UnitsElectricPotential.DECIVOLT).put(UnitsElectricPotential.TERAVOLT, Mul(Pow(10, 13), Sym("deciv")));
        c.get(UnitsElectricPotential.DECIVOLT).put(UnitsElectricPotential.VOLT, Mul(Int(10), Sym("deciv")));
        c.get(UnitsElectricPotential.DECIVOLT).put(UnitsElectricPotential.YOCTOVOLT, Mul(Rat(Int(1), Pow(10, 23)), Sym("deciv")));
        c.get(UnitsElectricPotential.DECIVOLT).put(UnitsElectricPotential.YOTTAVOLT, Mul(Pow(10, 25), Sym("deciv")));
        c.get(UnitsElectricPotential.DECIVOLT).put(UnitsElectricPotential.ZEPTOVOLT, Mul(Rat(Int(1), Pow(10, 20)), Sym("deciv")));
        c.get(UnitsElectricPotential.DECIVOLT).put(UnitsElectricPotential.ZETTAVOLT, Mul(Pow(10, 22), Sym("deciv")));
        c.get(UnitsElectricPotential.EXAVOLT).put(UnitsElectricPotential.ATTOVOLT, Mul(Rat(Int(1), Pow(10, 36)), Sym("exav")));
        c.get(UnitsElectricPotential.EXAVOLT).put(UnitsElectricPotential.CENTIVOLT, Mul(Rat(Int(1), Pow(10, 20)), Sym("exav")));
        c.get(UnitsElectricPotential.EXAVOLT).put(UnitsElectricPotential.DECAVOLT, Mul(Rat(Int(1), Pow(10, 17)), Sym("exav")));
        c.get(UnitsElectricPotential.EXAVOLT).put(UnitsElectricPotential.DECIVOLT, Mul(Rat(Int(1), Pow(10, 19)), Sym("exav")));
        c.get(UnitsElectricPotential.EXAVOLT).put(UnitsElectricPotential.FEMTOVOLT, Mul(Rat(Int(1), Pow(10, 33)), Sym("exav")));
        c.get(UnitsElectricPotential.EXAVOLT).put(UnitsElectricPotential.GIGAVOLT, Mul(Rat(Int(1), Pow(10, 9)), Sym("exav")));
        c.get(UnitsElectricPotential.EXAVOLT).put(UnitsElectricPotential.HECTOVOLT, Mul(Rat(Int(1), Pow(10, 16)), Sym("exav")));
        c.get(UnitsElectricPotential.EXAVOLT).put(UnitsElectricPotential.KILOVOLT, Mul(Rat(Int(1), Pow(10, 15)), Sym("exav")));
        c.get(UnitsElectricPotential.EXAVOLT).put(UnitsElectricPotential.MEGAVOLT, Mul(Rat(Int(1), Pow(10, 12)), Sym("exav")));
        c.get(UnitsElectricPotential.EXAVOLT).put(UnitsElectricPotential.MICROVOLT, Mul(Rat(Int(1), Pow(10, 24)), Sym("exav")));
        c.get(UnitsElectricPotential.EXAVOLT).put(UnitsElectricPotential.MILLIVOLT, Mul(Rat(Int(1), Pow(10, 21)), Sym("exav")));
        c.get(UnitsElectricPotential.EXAVOLT).put(UnitsElectricPotential.NANOVOLT, Mul(Rat(Int(1), Pow(10, 27)), Sym("exav")));
        c.get(UnitsElectricPotential.EXAVOLT).put(UnitsElectricPotential.PETAVOLT, Mul(Rat(Int(1), Int(1000)), Sym("exav")));
        c.get(UnitsElectricPotential.EXAVOLT).put(UnitsElectricPotential.PICOVOLT, Mul(Rat(Int(1), Pow(10, 30)), Sym("exav")));
        c.get(UnitsElectricPotential.EXAVOLT).put(UnitsElectricPotential.TERAVOLT, Mul(Rat(Int(1), Pow(10, 6)), Sym("exav")));
        c.get(UnitsElectricPotential.EXAVOLT).put(UnitsElectricPotential.VOLT, Mul(Rat(Int(1), Pow(10, 18)), Sym("exav")));
        c.get(UnitsElectricPotential.EXAVOLT).put(UnitsElectricPotential.YOCTOVOLT, Mul(Rat(Int(1), Pow(10, 42)), Sym("exav")));
        c.get(UnitsElectricPotential.EXAVOLT).put(UnitsElectricPotential.YOTTAVOLT, Mul(Pow(10, 6), Sym("exav")));
        c.get(UnitsElectricPotential.EXAVOLT).put(UnitsElectricPotential.ZEPTOVOLT, Mul(Rat(Int(1), Pow(10, 39)), Sym("exav")));
        c.get(UnitsElectricPotential.EXAVOLT).put(UnitsElectricPotential.ZETTAVOLT, Mul(Int(1000), Sym("exav")));
        c.get(UnitsElectricPotential.FEMTOVOLT).put(UnitsElectricPotential.ATTOVOLT, Mul(Rat(Int(1), Int(1000)), Sym("femtov")));
        c.get(UnitsElectricPotential.FEMTOVOLT).put(UnitsElectricPotential.CENTIVOLT, Mul(Pow(10, 13), Sym("femtov")));
        c.get(UnitsElectricPotential.FEMTOVOLT).put(UnitsElectricPotential.DECAVOLT, Mul(Pow(10, 16), Sym("femtov")));
        c.get(UnitsElectricPotential.FEMTOVOLT).put(UnitsElectricPotential.DECIVOLT, Mul(Pow(10, 14), Sym("femtov")));
        c.get(UnitsElectricPotential.FEMTOVOLT).put(UnitsElectricPotential.EXAVOLT, Mul(Pow(10, 33), Sym("femtov")));
        c.get(UnitsElectricPotential.FEMTOVOLT).put(UnitsElectricPotential.GIGAVOLT, Mul(Pow(10, 24), Sym("femtov")));
        c.get(UnitsElectricPotential.FEMTOVOLT).put(UnitsElectricPotential.HECTOVOLT, Mul(Pow(10, 17), Sym("femtov")));
        c.get(UnitsElectricPotential.FEMTOVOLT).put(UnitsElectricPotential.KILOVOLT, Mul(Pow(10, 18), Sym("femtov")));
        c.get(UnitsElectricPotential.FEMTOVOLT).put(UnitsElectricPotential.MEGAVOLT, Mul(Pow(10, 21), Sym("femtov")));
        c.get(UnitsElectricPotential.FEMTOVOLT).put(UnitsElectricPotential.MICROVOLT, Mul(Pow(10, 9), Sym("femtov")));
        c.get(UnitsElectricPotential.FEMTOVOLT).put(UnitsElectricPotential.MILLIVOLT, Mul(Pow(10, 12), Sym("femtov")));
        c.get(UnitsElectricPotential.FEMTOVOLT).put(UnitsElectricPotential.NANOVOLT, Mul(Pow(10, 6), Sym("femtov")));
        c.get(UnitsElectricPotential.FEMTOVOLT).put(UnitsElectricPotential.PETAVOLT, Mul(Pow(10, 30), Sym("femtov")));
        c.get(UnitsElectricPotential.FEMTOVOLT).put(UnitsElectricPotential.PICOVOLT, Mul(Int(1000), Sym("femtov")));
        c.get(UnitsElectricPotential.FEMTOVOLT).put(UnitsElectricPotential.TERAVOLT, Mul(Pow(10, 27), Sym("femtov")));
        c.get(UnitsElectricPotential.FEMTOVOLT).put(UnitsElectricPotential.VOLT, Mul(Pow(10, 15), Sym("femtov")));
        c.get(UnitsElectricPotential.FEMTOVOLT).put(UnitsElectricPotential.YOCTOVOLT, Mul(Rat(Int(1), Pow(10, 9)), Sym("femtov")));
        c.get(UnitsElectricPotential.FEMTOVOLT).put(UnitsElectricPotential.YOTTAVOLT, Mul(Pow(10, 39), Sym("femtov")));
        c.get(UnitsElectricPotential.FEMTOVOLT).put(UnitsElectricPotential.ZEPTOVOLT, Mul(Rat(Int(1), Pow(10, 6)), Sym("femtov")));
        c.get(UnitsElectricPotential.FEMTOVOLT).put(UnitsElectricPotential.ZETTAVOLT, Mul(Pow(10, 36), Sym("femtov")));
        c.get(UnitsElectricPotential.GIGAVOLT).put(UnitsElectricPotential.ATTOVOLT, Mul(Rat(Int(1), Pow(10, 27)), Sym("gigav")));
        c.get(UnitsElectricPotential.GIGAVOLT).put(UnitsElectricPotential.CENTIVOLT, Mul(Rat(Int(1), Pow(10, 11)), Sym("gigav")));
        c.get(UnitsElectricPotential.GIGAVOLT).put(UnitsElectricPotential.DECAVOLT, Mul(Rat(Int(1), Pow(10, 8)), Sym("gigav")));
        c.get(UnitsElectricPotential.GIGAVOLT).put(UnitsElectricPotential.DECIVOLT, Mul(Rat(Int(1), Pow(10, 10)), Sym("gigav")));
        c.get(UnitsElectricPotential.GIGAVOLT).put(UnitsElectricPotential.EXAVOLT, Mul(Pow(10, 9), Sym("gigav")));
        c.get(UnitsElectricPotential.GIGAVOLT).put(UnitsElectricPotential.FEMTOVOLT, Mul(Rat(Int(1), Pow(10, 24)), Sym("gigav")));
        c.get(UnitsElectricPotential.GIGAVOLT).put(UnitsElectricPotential.HECTOVOLT, Mul(Rat(Int(1), Pow(10, 7)), Sym("gigav")));
        c.get(UnitsElectricPotential.GIGAVOLT).put(UnitsElectricPotential.KILOVOLT, Mul(Rat(Int(1), Pow(10, 6)), Sym("gigav")));
        c.get(UnitsElectricPotential.GIGAVOLT).put(UnitsElectricPotential.MEGAVOLT, Mul(Rat(Int(1), Int(1000)), Sym("gigav")));
        c.get(UnitsElectricPotential.GIGAVOLT).put(UnitsElectricPotential.MICROVOLT, Mul(Rat(Int(1), Pow(10, 15)), Sym("gigav")));
        c.get(UnitsElectricPotential.GIGAVOLT).put(UnitsElectricPotential.MILLIVOLT, Mul(Rat(Int(1), Pow(10, 12)), Sym("gigav")));
        c.get(UnitsElectricPotential.GIGAVOLT).put(UnitsElectricPotential.NANOVOLT, Mul(Rat(Int(1), Pow(10, 18)), Sym("gigav")));
        c.get(UnitsElectricPotential.GIGAVOLT).put(UnitsElectricPotential.PETAVOLT, Mul(Pow(10, 6), Sym("gigav")));
        c.get(UnitsElectricPotential.GIGAVOLT).put(UnitsElectricPotential.PICOVOLT, Mul(Rat(Int(1), Pow(10, 21)), Sym("gigav")));
        c.get(UnitsElectricPotential.GIGAVOLT).put(UnitsElectricPotential.TERAVOLT, Mul(Int(1000), Sym("gigav")));
        c.get(UnitsElectricPotential.GIGAVOLT).put(UnitsElectricPotential.VOLT, Mul(Rat(Int(1), Pow(10, 9)), Sym("gigav")));
        c.get(UnitsElectricPotential.GIGAVOLT).put(UnitsElectricPotential.YOCTOVOLT, Mul(Rat(Int(1), Pow(10, 33)), Sym("gigav")));
        c.get(UnitsElectricPotential.GIGAVOLT).put(UnitsElectricPotential.YOTTAVOLT, Mul(Pow(10, 15), Sym("gigav")));
        c.get(UnitsElectricPotential.GIGAVOLT).put(UnitsElectricPotential.ZEPTOVOLT, Mul(Rat(Int(1), Pow(10, 30)), Sym("gigav")));
        c.get(UnitsElectricPotential.GIGAVOLT).put(UnitsElectricPotential.ZETTAVOLT, Mul(Pow(10, 12), Sym("gigav")));
        c.get(UnitsElectricPotential.HECTOVOLT).put(UnitsElectricPotential.ATTOVOLT, Mul(Rat(Int(1), Pow(10, 20)), Sym("hectov")));
        c.get(UnitsElectricPotential.HECTOVOLT).put(UnitsElectricPotential.CENTIVOLT, Mul(Rat(Int(1), Pow(10, 4)), Sym("hectov")));
        c.get(UnitsElectricPotential.HECTOVOLT).put(UnitsElectricPotential.DECAVOLT, Mul(Rat(Int(1), Int(10)), Sym("hectov")));
        c.get(UnitsElectricPotential.HECTOVOLT).put(UnitsElectricPotential.DECIVOLT, Mul(Rat(Int(1), Int(1000)), Sym("hectov")));
        c.get(UnitsElectricPotential.HECTOVOLT).put(UnitsElectricPotential.EXAVOLT, Mul(Pow(10, 16), Sym("hectov")));
        c.get(UnitsElectricPotential.HECTOVOLT).put(UnitsElectricPotential.FEMTOVOLT, Mul(Rat(Int(1), Pow(10, 17)), Sym("hectov")));
        c.get(UnitsElectricPotential.HECTOVOLT).put(UnitsElectricPotential.GIGAVOLT, Mul(Pow(10, 7), Sym("hectov")));
        c.get(UnitsElectricPotential.HECTOVOLT).put(UnitsElectricPotential.KILOVOLT, Mul(Int(10), Sym("hectov")));
        c.get(UnitsElectricPotential.HECTOVOLT).put(UnitsElectricPotential.MEGAVOLT, Mul(Pow(10, 4), Sym("hectov")));
        c.get(UnitsElectricPotential.HECTOVOLT).put(UnitsElectricPotential.MICROVOLT, Mul(Rat(Int(1), Pow(10, 8)), Sym("hectov")));
        c.get(UnitsElectricPotential.HECTOVOLT).put(UnitsElectricPotential.MILLIVOLT, Mul(Rat(Int(1), Pow(10, 5)), Sym("hectov")));
        c.get(UnitsElectricPotential.HECTOVOLT).put(UnitsElectricPotential.NANOVOLT, Mul(Rat(Int(1), Pow(10, 11)), Sym("hectov")));
        c.get(UnitsElectricPotential.HECTOVOLT).put(UnitsElectricPotential.PETAVOLT, Mul(Pow(10, 13), Sym("hectov")));
        c.get(UnitsElectricPotential.HECTOVOLT).put(UnitsElectricPotential.PICOVOLT, Mul(Rat(Int(1), Pow(10, 14)), Sym("hectov")));
        c.get(UnitsElectricPotential.HECTOVOLT).put(UnitsElectricPotential.TERAVOLT, Mul(Pow(10, 10), Sym("hectov")));
        c.get(UnitsElectricPotential.HECTOVOLT).put(UnitsElectricPotential.VOLT, Mul(Rat(Int(1), Int(100)), Sym("hectov")));
        c.get(UnitsElectricPotential.HECTOVOLT).put(UnitsElectricPotential.YOCTOVOLT, Mul(Rat(Int(1), Pow(10, 26)), Sym("hectov")));
        c.get(UnitsElectricPotential.HECTOVOLT).put(UnitsElectricPotential.YOTTAVOLT, Mul(Pow(10, 22), Sym("hectov")));
        c.get(UnitsElectricPotential.HECTOVOLT).put(UnitsElectricPotential.ZEPTOVOLT, Mul(Rat(Int(1), Pow(10, 23)), Sym("hectov")));
        c.get(UnitsElectricPotential.HECTOVOLT).put(UnitsElectricPotential.ZETTAVOLT, Mul(Pow(10, 19), Sym("hectov")));
        c.get(UnitsElectricPotential.KILOVOLT).put(UnitsElectricPotential.ATTOVOLT, Mul(Rat(Int(1), Pow(10, 21)), Sym("kilov")));
        c.get(UnitsElectricPotential.KILOVOLT).put(UnitsElectricPotential.CENTIVOLT, Mul(Rat(Int(1), Pow(10, 5)), Sym("kilov")));
        c.get(UnitsElectricPotential.KILOVOLT).put(UnitsElectricPotential.DECAVOLT, Mul(Rat(Int(1), Int(100)), Sym("kilov")));
        c.get(UnitsElectricPotential.KILOVOLT).put(UnitsElectricPotential.DECIVOLT, Mul(Rat(Int(1), Pow(10, 4)), Sym("kilov")));
        c.get(UnitsElectricPotential.KILOVOLT).put(UnitsElectricPotential.EXAVOLT, Mul(Pow(10, 15), Sym("kilov")));
        c.get(UnitsElectricPotential.KILOVOLT).put(UnitsElectricPotential.FEMTOVOLT, Mul(Rat(Int(1), Pow(10, 18)), Sym("kilov")));
        c.get(UnitsElectricPotential.KILOVOLT).put(UnitsElectricPotential.GIGAVOLT, Mul(Pow(10, 6), Sym("kilov")));
        c.get(UnitsElectricPotential.KILOVOLT).put(UnitsElectricPotential.HECTOVOLT, Mul(Rat(Int(1), Int(10)), Sym("kilov")));
        c.get(UnitsElectricPotential.KILOVOLT).put(UnitsElectricPotential.MEGAVOLT, Mul(Int(1000), Sym("kilov")));
        c.get(UnitsElectricPotential.KILOVOLT).put(UnitsElectricPotential.MICROVOLT, Mul(Rat(Int(1), Pow(10, 9)), Sym("kilov")));
        c.get(UnitsElectricPotential.KILOVOLT).put(UnitsElectricPotential.MILLIVOLT, Mul(Rat(Int(1), Pow(10, 6)), Sym("kilov")));
        c.get(UnitsElectricPotential.KILOVOLT).put(UnitsElectricPotential.NANOVOLT, Mul(Rat(Int(1), Pow(10, 12)), Sym("kilov")));
        c.get(UnitsElectricPotential.KILOVOLT).put(UnitsElectricPotential.PETAVOLT, Mul(Pow(10, 12), Sym("kilov")));
        c.get(UnitsElectricPotential.KILOVOLT).put(UnitsElectricPotential.PICOVOLT, Mul(Rat(Int(1), Pow(10, 15)), Sym("kilov")));
        c.get(UnitsElectricPotential.KILOVOLT).put(UnitsElectricPotential.TERAVOLT, Mul(Pow(10, 9), Sym("kilov")));
        c.get(UnitsElectricPotential.KILOVOLT).put(UnitsElectricPotential.VOLT, Mul(Rat(Int(1), Int(1000)), Sym("kilov")));
        c.get(UnitsElectricPotential.KILOVOLT).put(UnitsElectricPotential.YOCTOVOLT, Mul(Rat(Int(1), Pow(10, 27)), Sym("kilov")));
        c.get(UnitsElectricPotential.KILOVOLT).put(UnitsElectricPotential.YOTTAVOLT, Mul(Pow(10, 21), Sym("kilov")));
        c.get(UnitsElectricPotential.KILOVOLT).put(UnitsElectricPotential.ZEPTOVOLT, Mul(Rat(Int(1), Pow(10, 24)), Sym("kilov")));
        c.get(UnitsElectricPotential.KILOVOLT).put(UnitsElectricPotential.ZETTAVOLT, Mul(Pow(10, 18), Sym("kilov")));
        c.get(UnitsElectricPotential.MEGAVOLT).put(UnitsElectricPotential.ATTOVOLT, Mul(Rat(Int(1), Pow(10, 24)), Sym("megav")));
        c.get(UnitsElectricPotential.MEGAVOLT).put(UnitsElectricPotential.CENTIVOLT, Mul(Rat(Int(1), Pow(10, 8)), Sym("megav")));
        c.get(UnitsElectricPotential.MEGAVOLT).put(UnitsElectricPotential.DECAVOLT, Mul(Rat(Int(1), Pow(10, 5)), Sym("megav")));
        c.get(UnitsElectricPotential.MEGAVOLT).put(UnitsElectricPotential.DECIVOLT, Mul(Rat(Int(1), Pow(10, 7)), Sym("megav")));
        c.get(UnitsElectricPotential.MEGAVOLT).put(UnitsElectricPotential.EXAVOLT, Mul(Pow(10, 12), Sym("megav")));
        c.get(UnitsElectricPotential.MEGAVOLT).put(UnitsElectricPotential.FEMTOVOLT, Mul(Rat(Int(1), Pow(10, 21)), Sym("megav")));
        c.get(UnitsElectricPotential.MEGAVOLT).put(UnitsElectricPotential.GIGAVOLT, Mul(Int(1000), Sym("megav")));
        c.get(UnitsElectricPotential.MEGAVOLT).put(UnitsElectricPotential.HECTOVOLT, Mul(Rat(Int(1), Pow(10, 4)), Sym("megav")));
        c.get(UnitsElectricPotential.MEGAVOLT).put(UnitsElectricPotential.KILOVOLT, Mul(Rat(Int(1), Int(1000)), Sym("megav")));
        c.get(UnitsElectricPotential.MEGAVOLT).put(UnitsElectricPotential.MICROVOLT, Mul(Rat(Int(1), Pow(10, 12)), Sym("megav")));
        c.get(UnitsElectricPotential.MEGAVOLT).put(UnitsElectricPotential.MILLIVOLT, Mul(Rat(Int(1), Pow(10, 9)), Sym("megav")));
        c.get(UnitsElectricPotential.MEGAVOLT).put(UnitsElectricPotential.NANOVOLT, Mul(Rat(Int(1), Pow(10, 15)), Sym("megav")));
        c.get(UnitsElectricPotential.MEGAVOLT).put(UnitsElectricPotential.PETAVOLT, Mul(Pow(10, 9), Sym("megav")));
        c.get(UnitsElectricPotential.MEGAVOLT).put(UnitsElectricPotential.PICOVOLT, Mul(Rat(Int(1), Pow(10, 18)), Sym("megav")));
        c.get(UnitsElectricPotential.MEGAVOLT).put(UnitsElectricPotential.TERAVOLT, Mul(Pow(10, 6), Sym("megav")));
        c.get(UnitsElectricPotential.MEGAVOLT).put(UnitsElectricPotential.VOLT, Mul(Rat(Int(1), Pow(10, 6)), Sym("megav")));
        c.get(UnitsElectricPotential.MEGAVOLT).put(UnitsElectricPotential.YOCTOVOLT, Mul(Rat(Int(1), Pow(10, 30)), Sym("megav")));
        c.get(UnitsElectricPotential.MEGAVOLT).put(UnitsElectricPotential.YOTTAVOLT, Mul(Pow(10, 18), Sym("megav")));
        c.get(UnitsElectricPotential.MEGAVOLT).put(UnitsElectricPotential.ZEPTOVOLT, Mul(Rat(Int(1), Pow(10, 27)), Sym("megav")));
        c.get(UnitsElectricPotential.MEGAVOLT).put(UnitsElectricPotential.ZETTAVOLT, Mul(Pow(10, 15), Sym("megav")));
        c.get(UnitsElectricPotential.MICROVOLT).put(UnitsElectricPotential.ATTOVOLT, Mul(Rat(Int(1), Pow(10, 12)), Sym("microv")));
        c.get(UnitsElectricPotential.MICROVOLT).put(UnitsElectricPotential.CENTIVOLT, Mul(Pow(10, 4), Sym("microv")));
        c.get(UnitsElectricPotential.MICROVOLT).put(UnitsElectricPotential.DECAVOLT, Mul(Pow(10, 7), Sym("microv")));
        c.get(UnitsElectricPotential.MICROVOLT).put(UnitsElectricPotential.DECIVOLT, Mul(Pow(10, 5), Sym("microv")));
        c.get(UnitsElectricPotential.MICROVOLT).put(UnitsElectricPotential.EXAVOLT, Mul(Pow(10, 24), Sym("microv")));
        c.get(UnitsElectricPotential.MICROVOLT).put(UnitsElectricPotential.FEMTOVOLT, Mul(Rat(Int(1), Pow(10, 9)), Sym("microv")));
        c.get(UnitsElectricPotential.MICROVOLT).put(UnitsElectricPotential.GIGAVOLT, Mul(Pow(10, 15), Sym("microv")));
        c.get(UnitsElectricPotential.MICROVOLT).put(UnitsElectricPotential.HECTOVOLT, Mul(Pow(10, 8), Sym("microv")));
        c.get(UnitsElectricPotential.MICROVOLT).put(UnitsElectricPotential.KILOVOLT, Mul(Pow(10, 9), Sym("microv")));
        c.get(UnitsElectricPotential.MICROVOLT).put(UnitsElectricPotential.MEGAVOLT, Mul(Pow(10, 12), Sym("microv")));
        c.get(UnitsElectricPotential.MICROVOLT).put(UnitsElectricPotential.MILLIVOLT, Mul(Int(1000), Sym("microv")));
        c.get(UnitsElectricPotential.MICROVOLT).put(UnitsElectricPotential.NANOVOLT, Mul(Rat(Int(1), Int(1000)), Sym("microv")));
        c.get(UnitsElectricPotential.MICROVOLT).put(UnitsElectricPotential.PETAVOLT, Mul(Pow(10, 21), Sym("microv")));
        c.get(UnitsElectricPotential.MICROVOLT).put(UnitsElectricPotential.PICOVOLT, Mul(Rat(Int(1), Pow(10, 6)), Sym("microv")));
        c.get(UnitsElectricPotential.MICROVOLT).put(UnitsElectricPotential.TERAVOLT, Mul(Pow(10, 18), Sym("microv")));
        c.get(UnitsElectricPotential.MICROVOLT).put(UnitsElectricPotential.VOLT, Mul(Pow(10, 6), Sym("microv")));
        c.get(UnitsElectricPotential.MICROVOLT).put(UnitsElectricPotential.YOCTOVOLT, Mul(Rat(Int(1), Pow(10, 18)), Sym("microv")));
        c.get(UnitsElectricPotential.MICROVOLT).put(UnitsElectricPotential.YOTTAVOLT, Mul(Pow(10, 30), Sym("microv")));
        c.get(UnitsElectricPotential.MICROVOLT).put(UnitsElectricPotential.ZEPTOVOLT, Mul(Rat(Int(1), Pow(10, 15)), Sym("microv")));
        c.get(UnitsElectricPotential.MICROVOLT).put(UnitsElectricPotential.ZETTAVOLT, Mul(Pow(10, 27), Sym("microv")));
        c.get(UnitsElectricPotential.MILLIVOLT).put(UnitsElectricPotential.ATTOVOLT, Mul(Rat(Int(1), Pow(10, 15)), Sym("milliv")));
        c.get(UnitsElectricPotential.MILLIVOLT).put(UnitsElectricPotential.CENTIVOLT, Mul(Int(10), Sym("milliv")));
        c.get(UnitsElectricPotential.MILLIVOLT).put(UnitsElectricPotential.DECAVOLT, Mul(Pow(10, 4), Sym("milliv")));
        c.get(UnitsElectricPotential.MILLIVOLT).put(UnitsElectricPotential.DECIVOLT, Mul(Int(100), Sym("milliv")));
        c.get(UnitsElectricPotential.MILLIVOLT).put(UnitsElectricPotential.EXAVOLT, Mul(Pow(10, 21), Sym("milliv")));
        c.get(UnitsElectricPotential.MILLIVOLT).put(UnitsElectricPotential.FEMTOVOLT, Mul(Rat(Int(1), Pow(10, 12)), Sym("milliv")));
        c.get(UnitsElectricPotential.MILLIVOLT).put(UnitsElectricPotential.GIGAVOLT, Mul(Pow(10, 12), Sym("milliv")));
        c.get(UnitsElectricPotential.MILLIVOLT).put(UnitsElectricPotential.HECTOVOLT, Mul(Pow(10, 5), Sym("milliv")));
        c.get(UnitsElectricPotential.MILLIVOLT).put(UnitsElectricPotential.KILOVOLT, Mul(Pow(10, 6), Sym("milliv")));
        c.get(UnitsElectricPotential.MILLIVOLT).put(UnitsElectricPotential.MEGAVOLT, Mul(Pow(10, 9), Sym("milliv")));
        c.get(UnitsElectricPotential.MILLIVOLT).put(UnitsElectricPotential.MICROVOLT, Mul(Rat(Int(1), Int(1000)), Sym("milliv")));
        c.get(UnitsElectricPotential.MILLIVOLT).put(UnitsElectricPotential.NANOVOLT, Mul(Rat(Int(1), Pow(10, 6)), Sym("milliv")));
        c.get(UnitsElectricPotential.MILLIVOLT).put(UnitsElectricPotential.PETAVOLT, Mul(Pow(10, 18), Sym("milliv")));
        c.get(UnitsElectricPotential.MILLIVOLT).put(UnitsElectricPotential.PICOVOLT, Mul(Rat(Int(1), Pow(10, 9)), Sym("milliv")));
        c.get(UnitsElectricPotential.MILLIVOLT).put(UnitsElectricPotential.TERAVOLT, Mul(Pow(10, 15), Sym("milliv")));
        c.get(UnitsElectricPotential.MILLIVOLT).put(UnitsElectricPotential.VOLT, Mul(Int(1000), Sym("milliv")));
        c.get(UnitsElectricPotential.MILLIVOLT).put(UnitsElectricPotential.YOCTOVOLT, Mul(Rat(Int(1), Pow(10, 21)), Sym("milliv")));
        c.get(UnitsElectricPotential.MILLIVOLT).put(UnitsElectricPotential.YOTTAVOLT, Mul(Pow(10, 27), Sym("milliv")));
        c.get(UnitsElectricPotential.MILLIVOLT).put(UnitsElectricPotential.ZEPTOVOLT, Mul(Rat(Int(1), Pow(10, 18)), Sym("milliv")));
        c.get(UnitsElectricPotential.MILLIVOLT).put(UnitsElectricPotential.ZETTAVOLT, Mul(Pow(10, 24), Sym("milliv")));
        c.get(UnitsElectricPotential.NANOVOLT).put(UnitsElectricPotential.ATTOVOLT, Mul(Rat(Int(1), Pow(10, 9)), Sym("nanov")));
        c.get(UnitsElectricPotential.NANOVOLT).put(UnitsElectricPotential.CENTIVOLT, Mul(Pow(10, 7), Sym("nanov")));
        c.get(UnitsElectricPotential.NANOVOLT).put(UnitsElectricPotential.DECAVOLT, Mul(Pow(10, 10), Sym("nanov")));
        c.get(UnitsElectricPotential.NANOVOLT).put(UnitsElectricPotential.DECIVOLT, Mul(Pow(10, 8), Sym("nanov")));
        c.get(UnitsElectricPotential.NANOVOLT).put(UnitsElectricPotential.EXAVOLT, Mul(Pow(10, 27), Sym("nanov")));
        c.get(UnitsElectricPotential.NANOVOLT).put(UnitsElectricPotential.FEMTOVOLT, Mul(Rat(Int(1), Pow(10, 6)), Sym("nanov")));
        c.get(UnitsElectricPotential.NANOVOLT).put(UnitsElectricPotential.GIGAVOLT, Mul(Pow(10, 18), Sym("nanov")));
        c.get(UnitsElectricPotential.NANOVOLT).put(UnitsElectricPotential.HECTOVOLT, Mul(Pow(10, 11), Sym("nanov")));
        c.get(UnitsElectricPotential.NANOVOLT).put(UnitsElectricPotential.KILOVOLT, Mul(Pow(10, 12), Sym("nanov")));
        c.get(UnitsElectricPotential.NANOVOLT).put(UnitsElectricPotential.MEGAVOLT, Mul(Pow(10, 15), Sym("nanov")));
        c.get(UnitsElectricPotential.NANOVOLT).put(UnitsElectricPotential.MICROVOLT, Mul(Int(1000), Sym("nanov")));
        c.get(UnitsElectricPotential.NANOVOLT).put(UnitsElectricPotential.MILLIVOLT, Mul(Pow(10, 6), Sym("nanov")));
        c.get(UnitsElectricPotential.NANOVOLT).put(UnitsElectricPotential.PETAVOLT, Mul(Pow(10, 24), Sym("nanov")));
        c.get(UnitsElectricPotential.NANOVOLT).put(UnitsElectricPotential.PICOVOLT, Mul(Rat(Int(1), Int(1000)), Sym("nanov")));
        c.get(UnitsElectricPotential.NANOVOLT).put(UnitsElectricPotential.TERAVOLT, Mul(Pow(10, 21), Sym("nanov")));
        c.get(UnitsElectricPotential.NANOVOLT).put(UnitsElectricPotential.VOLT, Mul(Pow(10, 9), Sym("nanov")));
        c.get(UnitsElectricPotential.NANOVOLT).put(UnitsElectricPotential.YOCTOVOLT, Mul(Rat(Int(1), Pow(10, 15)), Sym("nanov")));
        c.get(UnitsElectricPotential.NANOVOLT).put(UnitsElectricPotential.YOTTAVOLT, Mul(Pow(10, 33), Sym("nanov")));
        c.get(UnitsElectricPotential.NANOVOLT).put(UnitsElectricPotential.ZEPTOVOLT, Mul(Rat(Int(1), Pow(10, 12)), Sym("nanov")));
        c.get(UnitsElectricPotential.NANOVOLT).put(UnitsElectricPotential.ZETTAVOLT, Mul(Pow(10, 30), Sym("nanov")));
        c.get(UnitsElectricPotential.PETAVOLT).put(UnitsElectricPotential.ATTOVOLT, Mul(Rat(Int(1), Pow(10, 33)), Sym("petav")));
        c.get(UnitsElectricPotential.PETAVOLT).put(UnitsElectricPotential.CENTIVOLT, Mul(Rat(Int(1), Pow(10, 17)), Sym("petav")));
        c.get(UnitsElectricPotential.PETAVOLT).put(UnitsElectricPotential.DECAVOLT, Mul(Rat(Int(1), Pow(10, 14)), Sym("petav")));
        c.get(UnitsElectricPotential.PETAVOLT).put(UnitsElectricPotential.DECIVOLT, Mul(Rat(Int(1), Pow(10, 16)), Sym("petav")));
        c.get(UnitsElectricPotential.PETAVOLT).put(UnitsElectricPotential.EXAVOLT, Mul(Int(1000), Sym("petav")));
        c.get(UnitsElectricPotential.PETAVOLT).put(UnitsElectricPotential.FEMTOVOLT, Mul(Rat(Int(1), Pow(10, 30)), Sym("petav")));
        c.get(UnitsElectricPotential.PETAVOLT).put(UnitsElectricPotential.GIGAVOLT, Mul(Rat(Int(1), Pow(10, 6)), Sym("petav")));
        c.get(UnitsElectricPotential.PETAVOLT).put(UnitsElectricPotential.HECTOVOLT, Mul(Rat(Int(1), Pow(10, 13)), Sym("petav")));
        c.get(UnitsElectricPotential.PETAVOLT).put(UnitsElectricPotential.KILOVOLT, Mul(Rat(Int(1), Pow(10, 12)), Sym("petav")));
        c.get(UnitsElectricPotential.PETAVOLT).put(UnitsElectricPotential.MEGAVOLT, Mul(Rat(Int(1), Pow(10, 9)), Sym("petav")));
        c.get(UnitsElectricPotential.PETAVOLT).put(UnitsElectricPotential.MICROVOLT, Mul(Rat(Int(1), Pow(10, 21)), Sym("petav")));
        c.get(UnitsElectricPotential.PETAVOLT).put(UnitsElectricPotential.MILLIVOLT, Mul(Rat(Int(1), Pow(10, 18)), Sym("petav")));
        c.get(UnitsElectricPotential.PETAVOLT).put(UnitsElectricPotential.NANOVOLT, Mul(Rat(Int(1), Pow(10, 24)), Sym("petav")));
        c.get(UnitsElectricPotential.PETAVOLT).put(UnitsElectricPotential.PICOVOLT, Mul(Rat(Int(1), Pow(10, 27)), Sym("petav")));
        c.get(UnitsElectricPotential.PETAVOLT).put(UnitsElectricPotential.TERAVOLT, Mul(Rat(Int(1), Int(1000)), Sym("petav")));
        c.get(UnitsElectricPotential.PETAVOLT).put(UnitsElectricPotential.VOLT, Mul(Rat(Int(1), Pow(10, 15)), Sym("petav")));
        c.get(UnitsElectricPotential.PETAVOLT).put(UnitsElectricPotential.YOCTOVOLT, Mul(Rat(Int(1), Pow(10, 39)), Sym("petav")));
        c.get(UnitsElectricPotential.PETAVOLT).put(UnitsElectricPotential.YOTTAVOLT, Mul(Pow(10, 9), Sym("petav")));
        c.get(UnitsElectricPotential.PETAVOLT).put(UnitsElectricPotential.ZEPTOVOLT, Mul(Rat(Int(1), Pow(10, 36)), Sym("petav")));
        c.get(UnitsElectricPotential.PETAVOLT).put(UnitsElectricPotential.ZETTAVOLT, Mul(Pow(10, 6), Sym("petav")));
        c.get(UnitsElectricPotential.PICOVOLT).put(UnitsElectricPotential.ATTOVOLT, Mul(Rat(Int(1), Pow(10, 6)), Sym("picov")));
        c.get(UnitsElectricPotential.PICOVOLT).put(UnitsElectricPotential.CENTIVOLT, Mul(Pow(10, 10), Sym("picov")));
        c.get(UnitsElectricPotential.PICOVOLT).put(UnitsElectricPotential.DECAVOLT, Mul(Pow(10, 13), Sym("picov")));
        c.get(UnitsElectricPotential.PICOVOLT).put(UnitsElectricPotential.DECIVOLT, Mul(Pow(10, 11), Sym("picov")));
        c.get(UnitsElectricPotential.PICOVOLT).put(UnitsElectricPotential.EXAVOLT, Mul(Pow(10, 30), Sym("picov")));
        c.get(UnitsElectricPotential.PICOVOLT).put(UnitsElectricPotential.FEMTOVOLT, Mul(Rat(Int(1), Int(1000)), Sym("picov")));
        c.get(UnitsElectricPotential.PICOVOLT).put(UnitsElectricPotential.GIGAVOLT, Mul(Pow(10, 21), Sym("picov")));
        c.get(UnitsElectricPotential.PICOVOLT).put(UnitsElectricPotential.HECTOVOLT, Mul(Pow(10, 14), Sym("picov")));
        c.get(UnitsElectricPotential.PICOVOLT).put(UnitsElectricPotential.KILOVOLT, Mul(Pow(10, 15), Sym("picov")));
        c.get(UnitsElectricPotential.PICOVOLT).put(UnitsElectricPotential.MEGAVOLT, Mul(Pow(10, 18), Sym("picov")));
        c.get(UnitsElectricPotential.PICOVOLT).put(UnitsElectricPotential.MICROVOLT, Mul(Pow(10, 6), Sym("picov")));
        c.get(UnitsElectricPotential.PICOVOLT).put(UnitsElectricPotential.MILLIVOLT, Mul(Pow(10, 9), Sym("picov")));
        c.get(UnitsElectricPotential.PICOVOLT).put(UnitsElectricPotential.NANOVOLT, Mul(Int(1000), Sym("picov")));
        c.get(UnitsElectricPotential.PICOVOLT).put(UnitsElectricPotential.PETAVOLT, Mul(Pow(10, 27), Sym("picov")));
        c.get(UnitsElectricPotential.PICOVOLT).put(UnitsElectricPotential.TERAVOLT, Mul(Pow(10, 24), Sym("picov")));
        c.get(UnitsElectricPotential.PICOVOLT).put(UnitsElectricPotential.VOLT, Mul(Pow(10, 12), Sym("picov")));
        c.get(UnitsElectricPotential.PICOVOLT).put(UnitsElectricPotential.YOCTOVOLT, Mul(Rat(Int(1), Pow(10, 12)), Sym("picov")));
        c.get(UnitsElectricPotential.PICOVOLT).put(UnitsElectricPotential.YOTTAVOLT, Mul(Pow(10, 36), Sym("picov")));
        c.get(UnitsElectricPotential.PICOVOLT).put(UnitsElectricPotential.ZEPTOVOLT, Mul(Rat(Int(1), Pow(10, 9)), Sym("picov")));
        c.get(UnitsElectricPotential.PICOVOLT).put(UnitsElectricPotential.ZETTAVOLT, Mul(Pow(10, 33), Sym("picov")));
        c.get(UnitsElectricPotential.TERAVOLT).put(UnitsElectricPotential.ATTOVOLT, Mul(Rat(Int(1), Pow(10, 30)), Sym("terav")));
        c.get(UnitsElectricPotential.TERAVOLT).put(UnitsElectricPotential.CENTIVOLT, Mul(Rat(Int(1), Pow(10, 14)), Sym("terav")));
        c.get(UnitsElectricPotential.TERAVOLT).put(UnitsElectricPotential.DECAVOLT, Mul(Rat(Int(1), Pow(10, 11)), Sym("terav")));
        c.get(UnitsElectricPotential.TERAVOLT).put(UnitsElectricPotential.DECIVOLT, Mul(Rat(Int(1), Pow(10, 13)), Sym("terav")));
        c.get(UnitsElectricPotential.TERAVOLT).put(UnitsElectricPotential.EXAVOLT, Mul(Pow(10, 6), Sym("terav")));
        c.get(UnitsElectricPotential.TERAVOLT).put(UnitsElectricPotential.FEMTOVOLT, Mul(Rat(Int(1), Pow(10, 27)), Sym("terav")));
        c.get(UnitsElectricPotential.TERAVOLT).put(UnitsElectricPotential.GIGAVOLT, Mul(Rat(Int(1), Int(1000)), Sym("terav")));
        c.get(UnitsElectricPotential.TERAVOLT).put(UnitsElectricPotential.HECTOVOLT, Mul(Rat(Int(1), Pow(10, 10)), Sym("terav")));
        c.get(UnitsElectricPotential.TERAVOLT).put(UnitsElectricPotential.KILOVOLT, Mul(Rat(Int(1), Pow(10, 9)), Sym("terav")));
        c.get(UnitsElectricPotential.TERAVOLT).put(UnitsElectricPotential.MEGAVOLT, Mul(Rat(Int(1), Pow(10, 6)), Sym("terav")));
        c.get(UnitsElectricPotential.TERAVOLT).put(UnitsElectricPotential.MICROVOLT, Mul(Rat(Int(1), Pow(10, 18)), Sym("terav")));
        c.get(UnitsElectricPotential.TERAVOLT).put(UnitsElectricPotential.MILLIVOLT, Mul(Rat(Int(1), Pow(10, 15)), Sym("terav")));
        c.get(UnitsElectricPotential.TERAVOLT).put(UnitsElectricPotential.NANOVOLT, Mul(Rat(Int(1), Pow(10, 21)), Sym("terav")));
        c.get(UnitsElectricPotential.TERAVOLT).put(UnitsElectricPotential.PETAVOLT, Mul(Int(1000), Sym("terav")));
        c.get(UnitsElectricPotential.TERAVOLT).put(UnitsElectricPotential.PICOVOLT, Mul(Rat(Int(1), Pow(10, 24)), Sym("terav")));
        c.get(UnitsElectricPotential.TERAVOLT).put(UnitsElectricPotential.VOLT, Mul(Rat(Int(1), Pow(10, 12)), Sym("terav")));
        c.get(UnitsElectricPotential.TERAVOLT).put(UnitsElectricPotential.YOCTOVOLT, Mul(Rat(Int(1), Pow(10, 36)), Sym("terav")));
        c.get(UnitsElectricPotential.TERAVOLT).put(UnitsElectricPotential.YOTTAVOLT, Mul(Pow(10, 12), Sym("terav")));
        c.get(UnitsElectricPotential.TERAVOLT).put(UnitsElectricPotential.ZEPTOVOLT, Mul(Rat(Int(1), Pow(10, 33)), Sym("terav")));
        c.get(UnitsElectricPotential.TERAVOLT).put(UnitsElectricPotential.ZETTAVOLT, Mul(Pow(10, 9), Sym("terav")));
        c.get(UnitsElectricPotential.VOLT).put(UnitsElectricPotential.ATTOVOLT, Mul(Rat(Int(1), Pow(10, 18)), Sym("v")));
        c.get(UnitsElectricPotential.VOLT).put(UnitsElectricPotential.CENTIVOLT, Mul(Rat(Int(1), Int(100)), Sym("v")));
        c.get(UnitsElectricPotential.VOLT).put(UnitsElectricPotential.DECAVOLT, Mul(Int(10), Sym("v")));
        c.get(UnitsElectricPotential.VOLT).put(UnitsElectricPotential.DECIVOLT, Mul(Rat(Int(1), Int(10)), Sym("v")));
        c.get(UnitsElectricPotential.VOLT).put(UnitsElectricPotential.EXAVOLT, Mul(Pow(10, 18), Sym("v")));
        c.get(UnitsElectricPotential.VOLT).put(UnitsElectricPotential.FEMTOVOLT, Mul(Rat(Int(1), Pow(10, 15)), Sym("v")));
        c.get(UnitsElectricPotential.VOLT).put(UnitsElectricPotential.GIGAVOLT, Mul(Pow(10, 9), Sym("v")));
        c.get(UnitsElectricPotential.VOLT).put(UnitsElectricPotential.HECTOVOLT, Mul(Int(100), Sym("v")));
        c.get(UnitsElectricPotential.VOLT).put(UnitsElectricPotential.KILOVOLT, Mul(Int(1000), Sym("v")));
        c.get(UnitsElectricPotential.VOLT).put(UnitsElectricPotential.MEGAVOLT, Mul(Pow(10, 6), Sym("v")));
        c.get(UnitsElectricPotential.VOLT).put(UnitsElectricPotential.MICROVOLT, Mul(Rat(Int(1), Pow(10, 6)), Sym("v")));
        c.get(UnitsElectricPotential.VOLT).put(UnitsElectricPotential.MILLIVOLT, Mul(Rat(Int(1), Int(1000)), Sym("v")));
        c.get(UnitsElectricPotential.VOLT).put(UnitsElectricPotential.NANOVOLT, Mul(Rat(Int(1), Pow(10, 9)), Sym("v")));
        c.get(UnitsElectricPotential.VOLT).put(UnitsElectricPotential.PETAVOLT, Mul(Pow(10, 15), Sym("v")));
        c.get(UnitsElectricPotential.VOLT).put(UnitsElectricPotential.PICOVOLT, Mul(Rat(Int(1), Pow(10, 12)), Sym("v")));
        c.get(UnitsElectricPotential.VOLT).put(UnitsElectricPotential.TERAVOLT, Mul(Pow(10, 12), Sym("v")));
        c.get(UnitsElectricPotential.VOLT).put(UnitsElectricPotential.YOCTOVOLT, Mul(Rat(Int(1), Pow(10, 24)), Sym("v")));
        c.get(UnitsElectricPotential.VOLT).put(UnitsElectricPotential.YOTTAVOLT, Mul(Pow(10, 24), Sym("v")));
        c.get(UnitsElectricPotential.VOLT).put(UnitsElectricPotential.ZEPTOVOLT, Mul(Rat(Int(1), Pow(10, 21)), Sym("v")));
        c.get(UnitsElectricPotential.VOLT).put(UnitsElectricPotential.ZETTAVOLT, Mul(Pow(10, 21), Sym("v")));
        c.get(UnitsElectricPotential.YOCTOVOLT).put(UnitsElectricPotential.ATTOVOLT, Mul(Pow(10, 6), Sym("yoctov")));
        c.get(UnitsElectricPotential.YOCTOVOLT).put(UnitsElectricPotential.CENTIVOLT, Mul(Pow(10, 22), Sym("yoctov")));
        c.get(UnitsElectricPotential.YOCTOVOLT).put(UnitsElectricPotential.DECAVOLT, Mul(Pow(10, 25), Sym("yoctov")));
        c.get(UnitsElectricPotential.YOCTOVOLT).put(UnitsElectricPotential.DECIVOLT, Mul(Pow(10, 23), Sym("yoctov")));
        c.get(UnitsElectricPotential.YOCTOVOLT).put(UnitsElectricPotential.EXAVOLT, Mul(Pow(10, 42), Sym("yoctov")));
        c.get(UnitsElectricPotential.YOCTOVOLT).put(UnitsElectricPotential.FEMTOVOLT, Mul(Pow(10, 9), Sym("yoctov")));
        c.get(UnitsElectricPotential.YOCTOVOLT).put(UnitsElectricPotential.GIGAVOLT, Mul(Pow(10, 33), Sym("yoctov")));
        c.get(UnitsElectricPotential.YOCTOVOLT).put(UnitsElectricPotential.HECTOVOLT, Mul(Pow(10, 26), Sym("yoctov")));
        c.get(UnitsElectricPotential.YOCTOVOLT).put(UnitsElectricPotential.KILOVOLT, Mul(Pow(10, 27), Sym("yoctov")));
        c.get(UnitsElectricPotential.YOCTOVOLT).put(UnitsElectricPotential.MEGAVOLT, Mul(Pow(10, 30), Sym("yoctov")));
        c.get(UnitsElectricPotential.YOCTOVOLT).put(UnitsElectricPotential.MICROVOLT, Mul(Pow(10, 18), Sym("yoctov")));
        c.get(UnitsElectricPotential.YOCTOVOLT).put(UnitsElectricPotential.MILLIVOLT, Mul(Pow(10, 21), Sym("yoctov")));
        c.get(UnitsElectricPotential.YOCTOVOLT).put(UnitsElectricPotential.NANOVOLT, Mul(Pow(10, 15), Sym("yoctov")));
        c.get(UnitsElectricPotential.YOCTOVOLT).put(UnitsElectricPotential.PETAVOLT, Mul(Pow(10, 39), Sym("yoctov")));
        c.get(UnitsElectricPotential.YOCTOVOLT).put(UnitsElectricPotential.PICOVOLT, Mul(Pow(10, 12), Sym("yoctov")));
        c.get(UnitsElectricPotential.YOCTOVOLT).put(UnitsElectricPotential.TERAVOLT, Mul(Pow(10, 36), Sym("yoctov")));
        c.get(UnitsElectricPotential.YOCTOVOLT).put(UnitsElectricPotential.VOLT, Mul(Pow(10, 24), Sym("yoctov")));
        c.get(UnitsElectricPotential.YOCTOVOLT).put(UnitsElectricPotential.YOTTAVOLT, Mul(Pow(10, 48), Sym("yoctov")));
        c.get(UnitsElectricPotential.YOCTOVOLT).put(UnitsElectricPotential.ZEPTOVOLT, Mul(Int(1000), Sym("yoctov")));
        c.get(UnitsElectricPotential.YOCTOVOLT).put(UnitsElectricPotential.ZETTAVOLT, Mul(Pow(10, 45), Sym("yoctov")));
        c.get(UnitsElectricPotential.YOTTAVOLT).put(UnitsElectricPotential.ATTOVOLT, Mul(Rat(Int(1), Pow(10, 42)), Sym("yottav")));
        c.get(UnitsElectricPotential.YOTTAVOLT).put(UnitsElectricPotential.CENTIVOLT, Mul(Rat(Int(1), Pow(10, 26)), Sym("yottav")));
        c.get(UnitsElectricPotential.YOTTAVOLT).put(UnitsElectricPotential.DECAVOLT, Mul(Rat(Int(1), Pow(10, 23)), Sym("yottav")));
        c.get(UnitsElectricPotential.YOTTAVOLT).put(UnitsElectricPotential.DECIVOLT, Mul(Rat(Int(1), Pow(10, 25)), Sym("yottav")));
        c.get(UnitsElectricPotential.YOTTAVOLT).put(UnitsElectricPotential.EXAVOLT, Mul(Rat(Int(1), Pow(10, 6)), Sym("yottav")));
        c.get(UnitsElectricPotential.YOTTAVOLT).put(UnitsElectricPotential.FEMTOVOLT, Mul(Rat(Int(1), Pow(10, 39)), Sym("yottav")));
        c.get(UnitsElectricPotential.YOTTAVOLT).put(UnitsElectricPotential.GIGAVOLT, Mul(Rat(Int(1), Pow(10, 15)), Sym("yottav")));
        c.get(UnitsElectricPotential.YOTTAVOLT).put(UnitsElectricPotential.HECTOVOLT, Mul(Rat(Int(1), Pow(10, 22)), Sym("yottav")));
        c.get(UnitsElectricPotential.YOTTAVOLT).put(UnitsElectricPotential.KILOVOLT, Mul(Rat(Int(1), Pow(10, 21)), Sym("yottav")));
        c.get(UnitsElectricPotential.YOTTAVOLT).put(UnitsElectricPotential.MEGAVOLT, Mul(Rat(Int(1), Pow(10, 18)), Sym("yottav")));
        c.get(UnitsElectricPotential.YOTTAVOLT).put(UnitsElectricPotential.MICROVOLT, Mul(Rat(Int(1), Pow(10, 30)), Sym("yottav")));
        c.get(UnitsElectricPotential.YOTTAVOLT).put(UnitsElectricPotential.MILLIVOLT, Mul(Rat(Int(1), Pow(10, 27)), Sym("yottav")));
        c.get(UnitsElectricPotential.YOTTAVOLT).put(UnitsElectricPotential.NANOVOLT, Mul(Rat(Int(1), Pow(10, 33)), Sym("yottav")));
        c.get(UnitsElectricPotential.YOTTAVOLT).put(UnitsElectricPotential.PETAVOLT, Mul(Rat(Int(1), Pow(10, 9)), Sym("yottav")));
        c.get(UnitsElectricPotential.YOTTAVOLT).put(UnitsElectricPotential.PICOVOLT, Mul(Rat(Int(1), Pow(10, 36)), Sym("yottav")));
        c.get(UnitsElectricPotential.YOTTAVOLT).put(UnitsElectricPotential.TERAVOLT, Mul(Rat(Int(1), Pow(10, 12)), Sym("yottav")));
        c.get(UnitsElectricPotential.YOTTAVOLT).put(UnitsElectricPotential.VOLT, Mul(Rat(Int(1), Pow(10, 24)), Sym("yottav")));
        c.get(UnitsElectricPotential.YOTTAVOLT).put(UnitsElectricPotential.YOCTOVOLT, Mul(Rat(Int(1), Pow(10, 48)), Sym("yottav")));
        c.get(UnitsElectricPotential.YOTTAVOLT).put(UnitsElectricPotential.ZEPTOVOLT, Mul(Rat(Int(1), Pow(10, 45)), Sym("yottav")));
        c.get(UnitsElectricPotential.YOTTAVOLT).put(UnitsElectricPotential.ZETTAVOLT, Mul(Rat(Int(1), Int(1000)), Sym("yottav")));
        c.get(UnitsElectricPotential.ZEPTOVOLT).put(UnitsElectricPotential.ATTOVOLT, Mul(Int(1000), Sym("zeptov")));
        c.get(UnitsElectricPotential.ZEPTOVOLT).put(UnitsElectricPotential.CENTIVOLT, Mul(Pow(10, 19), Sym("zeptov")));
        c.get(UnitsElectricPotential.ZEPTOVOLT).put(UnitsElectricPotential.DECAVOLT, Mul(Pow(10, 22), Sym("zeptov")));
        c.get(UnitsElectricPotential.ZEPTOVOLT).put(UnitsElectricPotential.DECIVOLT, Mul(Pow(10, 20), Sym("zeptov")));
        c.get(UnitsElectricPotential.ZEPTOVOLT).put(UnitsElectricPotential.EXAVOLT, Mul(Pow(10, 39), Sym("zeptov")));
        c.get(UnitsElectricPotential.ZEPTOVOLT).put(UnitsElectricPotential.FEMTOVOLT, Mul(Pow(10, 6), Sym("zeptov")));
        c.get(UnitsElectricPotential.ZEPTOVOLT).put(UnitsElectricPotential.GIGAVOLT, Mul(Pow(10, 30), Sym("zeptov")));
        c.get(UnitsElectricPotential.ZEPTOVOLT).put(UnitsElectricPotential.HECTOVOLT, Mul(Pow(10, 23), Sym("zeptov")));
        c.get(UnitsElectricPotential.ZEPTOVOLT).put(UnitsElectricPotential.KILOVOLT, Mul(Pow(10, 24), Sym("zeptov")));
        c.get(UnitsElectricPotential.ZEPTOVOLT).put(UnitsElectricPotential.MEGAVOLT, Mul(Pow(10, 27), Sym("zeptov")));
        c.get(UnitsElectricPotential.ZEPTOVOLT).put(UnitsElectricPotential.MICROVOLT, Mul(Pow(10, 15), Sym("zeptov")));
        c.get(UnitsElectricPotential.ZEPTOVOLT).put(UnitsElectricPotential.MILLIVOLT, Mul(Pow(10, 18), Sym("zeptov")));
        c.get(UnitsElectricPotential.ZEPTOVOLT).put(UnitsElectricPotential.NANOVOLT, Mul(Pow(10, 12), Sym("zeptov")));
        c.get(UnitsElectricPotential.ZEPTOVOLT).put(UnitsElectricPotential.PETAVOLT, Mul(Pow(10, 36), Sym("zeptov")));
        c.get(UnitsElectricPotential.ZEPTOVOLT).put(UnitsElectricPotential.PICOVOLT, Mul(Pow(10, 9), Sym("zeptov")));
        c.get(UnitsElectricPotential.ZEPTOVOLT).put(UnitsElectricPotential.TERAVOLT, Mul(Pow(10, 33), Sym("zeptov")));
        c.get(UnitsElectricPotential.ZEPTOVOLT).put(UnitsElectricPotential.VOLT, Mul(Pow(10, 21), Sym("zeptov")));
        c.get(UnitsElectricPotential.ZEPTOVOLT).put(UnitsElectricPotential.YOCTOVOLT, Mul(Rat(Int(1), Int(1000)), Sym("zeptov")));
        c.get(UnitsElectricPotential.ZEPTOVOLT).put(UnitsElectricPotential.YOTTAVOLT, Mul(Pow(10, 45), Sym("zeptov")));
        c.get(UnitsElectricPotential.ZEPTOVOLT).put(UnitsElectricPotential.ZETTAVOLT, Mul(Pow(10, 42), Sym("zeptov")));
        c.get(UnitsElectricPotential.ZETTAVOLT).put(UnitsElectricPotential.ATTOVOLT, Mul(Rat(Int(1), Pow(10, 39)), Sym("zettav")));
        c.get(UnitsElectricPotential.ZETTAVOLT).put(UnitsElectricPotential.CENTIVOLT, Mul(Rat(Int(1), Pow(10, 23)), Sym("zettav")));
        c.get(UnitsElectricPotential.ZETTAVOLT).put(UnitsElectricPotential.DECAVOLT, Mul(Rat(Int(1), Pow(10, 20)), Sym("zettav")));
        c.get(UnitsElectricPotential.ZETTAVOLT).put(UnitsElectricPotential.DECIVOLT, Mul(Rat(Int(1), Pow(10, 22)), Sym("zettav")));
        c.get(UnitsElectricPotential.ZETTAVOLT).put(UnitsElectricPotential.EXAVOLT, Mul(Rat(Int(1), Int(1000)), Sym("zettav")));
        c.get(UnitsElectricPotential.ZETTAVOLT).put(UnitsElectricPotential.FEMTOVOLT, Mul(Rat(Int(1), Pow(10, 36)), Sym("zettav")));
        c.get(UnitsElectricPotential.ZETTAVOLT).put(UnitsElectricPotential.GIGAVOLT, Mul(Rat(Int(1), Pow(10, 12)), Sym("zettav")));
        c.get(UnitsElectricPotential.ZETTAVOLT).put(UnitsElectricPotential.HECTOVOLT, Mul(Rat(Int(1), Pow(10, 19)), Sym("zettav")));
        c.get(UnitsElectricPotential.ZETTAVOLT).put(UnitsElectricPotential.KILOVOLT, Mul(Rat(Int(1), Pow(10, 18)), Sym("zettav")));
        c.get(UnitsElectricPotential.ZETTAVOLT).put(UnitsElectricPotential.MEGAVOLT, Mul(Rat(Int(1), Pow(10, 15)), Sym("zettav")));
        c.get(UnitsElectricPotential.ZETTAVOLT).put(UnitsElectricPotential.MICROVOLT, Mul(Rat(Int(1), Pow(10, 27)), Sym("zettav")));
        c.get(UnitsElectricPotential.ZETTAVOLT).put(UnitsElectricPotential.MILLIVOLT, Mul(Rat(Int(1), Pow(10, 24)), Sym("zettav")));
        c.get(UnitsElectricPotential.ZETTAVOLT).put(UnitsElectricPotential.NANOVOLT, Mul(Rat(Int(1), Pow(10, 30)), Sym("zettav")));
        c.get(UnitsElectricPotential.ZETTAVOLT).put(UnitsElectricPotential.PETAVOLT, Mul(Rat(Int(1), Pow(10, 6)), Sym("zettav")));
        c.get(UnitsElectricPotential.ZETTAVOLT).put(UnitsElectricPotential.PICOVOLT, Mul(Rat(Int(1), Pow(10, 33)), Sym("zettav")));
        c.get(UnitsElectricPotential.ZETTAVOLT).put(UnitsElectricPotential.TERAVOLT, Mul(Rat(Int(1), Pow(10, 9)), Sym("zettav")));
        c.get(UnitsElectricPotential.ZETTAVOLT).put(UnitsElectricPotential.VOLT, Mul(Rat(Int(1), Pow(10, 21)), Sym("zettav")));
        c.get(UnitsElectricPotential.ZETTAVOLT).put(UnitsElectricPotential.YOCTOVOLT, Mul(Rat(Int(1), Pow(10, 45)), Sym("zettav")));
        c.get(UnitsElectricPotential.ZETTAVOLT).put(UnitsElectricPotential.YOTTAVOLT, Mul(Int(1000), Sym("zettav")));
        c.get(UnitsElectricPotential.ZETTAVOLT).put(UnitsElectricPotential.ZEPTOVOLT, Mul(Rat(Int(1), Pow(10, 42)), Sym("zettav")));
        conversions = Collections.unmodifiableMap(c);
    }

    private static final Map<UnitsElectricPotential, String> SYMBOLS;
    static {
        Map<UnitsElectricPotential, String> s = new HashMap<UnitsElectricPotential, String>();
        s.put(UnitsElectricPotential.ATTOVOLT, "aV");
        s.put(UnitsElectricPotential.CENTIVOLT, "cV");
        s.put(UnitsElectricPotential.DECAVOLT, "daV");
        s.put(UnitsElectricPotential.DECIVOLT, "dV");
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
   public ElectricPotentialI(ElectricPotential value, Unit<ome.units.quantity.ElectricPotential> ul) throws BigResult {
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
    public ElectricPotentialI(ElectricPotential value, String target) throws BigResult {
       String source = value.getUnit().toString();
       if (target.equals(source)) {
           setValue(value.getValue());
           setUnit(value.getUnit());
        } else {
            UnitsElectricPotential targetUnit = UnitsElectricPotential.valueOf(target);
            Conversion conversion = conversions.get(value.getUnit()).get(targetUnit);
            if (conversion == null) {
                throw new RuntimeException(String.format(
                    "%f %s cannot be converted to %s",
                        value.getValue(), value.getUnit(), target));
            }
            double orig = value.getValue();
            BigDecimal big = conversion.convert(orig);
            double converted = big.doubleValue();
            if (converted == Double.NEGATIVE_INFINITY ||
                    converted == Double.POSITIVE_INFINITY) {
                throw new BigResult(big,
                        "Failed to convert " + source + ":" + target);
            }

            setValue(converted);
            setUnit(targetUnit);
       }
    }

   /**
    * Copy constructor that converts between units if possible.
    *
    * @param target unit that is desired. non-null.
    */
    public ElectricPotentialI(ElectricPotential value, UnitsElectricPotential target) throws BigResult {
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

