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

    private static final Map<String, Conversion> conversions;
    static {
        Map<String, Conversion> c = new HashMap<String, Conversion>();

        c.put("ATTOVOLT:CENTIVOLT", Mul(Pow(10, 16), Sym("attov")));
        c.put("ATTOVOLT:DECAVOLT", Mul(Pow(10, 19), Sym("attov")));
        c.put("ATTOVOLT:DECIVOLT", Mul(Pow(10, 17), Sym("attov")));
        c.put("ATTOVOLT:EXAVOLT", Mul(Pow(10, 36), Sym("attov")));
        c.put("ATTOVOLT:FEMTOVOLT", Mul(Int(1000), Sym("attov")));
        c.put("ATTOVOLT:GIGAVOLT", Mul(Pow(10, 27), Sym("attov")));
        c.put("ATTOVOLT:HECTOVOLT", Mul(Pow(10, 20), Sym("attov")));
        c.put("ATTOVOLT:KILOVOLT", Mul(Pow(10, 21), Sym("attov")));
        c.put("ATTOVOLT:MEGAVOLT", Mul(Pow(10, 24), Sym("attov")));
        c.put("ATTOVOLT:MICROVOLT", Mul(Pow(10, 12), Sym("attov")));
        c.put("ATTOVOLT:MILLIVOLT", Mul(Pow(10, 15), Sym("attov")));
        c.put("ATTOVOLT:NANOVOLT", Mul(Pow(10, 9), Sym("attov")));
        c.put("ATTOVOLT:PETAVOLT", Mul(Pow(10, 33), Sym("attov")));
        c.put("ATTOVOLT:PICOVOLT", Mul(Pow(10, 6), Sym("attov")));
        c.put("ATTOVOLT:TERAVOLT", Mul(Pow(10, 30), Sym("attov")));
        c.put("ATTOVOLT:VOLT", Mul(Pow(10, 18), Sym("attov")));
        c.put("ATTOVOLT:YOCTOVOLT", Mul(Rat(Int(1), Pow(10, 6)), Sym("attov")));
        c.put("ATTOVOLT:YOTTAVOLT", Mul(Pow(10, 42), Sym("attov")));
        c.put("ATTOVOLT:ZEPTOVOLT", Mul(Rat(Int(1), Int(1000)), Sym("attov")));
        c.put("ATTOVOLT:ZETTAVOLT", Mul(Pow(10, 39), Sym("attov")));
        c.put("CENTIVOLT:ATTOVOLT", Mul(Rat(Int(1), Pow(10, 16)), Sym("centiv")));
        c.put("CENTIVOLT:DECAVOLT", Mul(Int(1000), Sym("centiv")));
        c.put("CENTIVOLT:DECIVOLT", Mul(Int(10), Sym("centiv")));
        c.put("CENTIVOLT:EXAVOLT", Mul(Pow(10, 20), Sym("centiv")));
        c.put("CENTIVOLT:FEMTOVOLT", Mul(Rat(Int(1), Pow(10, 13)), Sym("centiv")));
        c.put("CENTIVOLT:GIGAVOLT", Mul(Pow(10, 11), Sym("centiv")));
        c.put("CENTIVOLT:HECTOVOLT", Mul(Pow(10, 4), Sym("centiv")));
        c.put("CENTIVOLT:KILOVOLT", Mul(Pow(10, 5), Sym("centiv")));
        c.put("CENTIVOLT:MEGAVOLT", Mul(Pow(10, 8), Sym("centiv")));
        c.put("CENTIVOLT:MICROVOLT", Mul(Rat(Int(1), Pow(10, 4)), Sym("centiv")));
        c.put("CENTIVOLT:MILLIVOLT", Mul(Rat(Int(1), Int(10)), Sym("centiv")));
        c.put("CENTIVOLT:NANOVOLT", Mul(Rat(Int(1), Pow(10, 7)), Sym("centiv")));
        c.put("CENTIVOLT:PETAVOLT", Mul(Pow(10, 17), Sym("centiv")));
        c.put("CENTIVOLT:PICOVOLT", Mul(Rat(Int(1), Pow(10, 10)), Sym("centiv")));
        c.put("CENTIVOLT:TERAVOLT", Mul(Pow(10, 14), Sym("centiv")));
        c.put("CENTIVOLT:VOLT", Mul(Int(100), Sym("centiv")));
        c.put("CENTIVOLT:YOCTOVOLT", Mul(Rat(Int(1), Pow(10, 22)), Sym("centiv")));
        c.put("CENTIVOLT:YOTTAVOLT", Mul(Pow(10, 26), Sym("centiv")));
        c.put("CENTIVOLT:ZEPTOVOLT", Mul(Rat(Int(1), Pow(10, 19)), Sym("centiv")));
        c.put("CENTIVOLT:ZETTAVOLT", Mul(Pow(10, 23), Sym("centiv")));
        c.put("DECAVOLT:ATTOVOLT", Mul(Rat(Int(1), Pow(10, 19)), Sym("decav")));
        c.put("DECAVOLT:CENTIVOLT", Mul(Rat(Int(1), Int(1000)), Sym("decav")));
        c.put("DECAVOLT:DECIVOLT", Mul(Rat(Int(1), Int(100)), Sym("decav")));
        c.put("DECAVOLT:EXAVOLT", Mul(Pow(10, 17), Sym("decav")));
        c.put("DECAVOLT:FEMTOVOLT", Mul(Rat(Int(1), Pow(10, 16)), Sym("decav")));
        c.put("DECAVOLT:GIGAVOLT", Mul(Pow(10, 8), Sym("decav")));
        c.put("DECAVOLT:HECTOVOLT", Mul(Int(10), Sym("decav")));
        c.put("DECAVOLT:KILOVOLT", Mul(Int(100), Sym("decav")));
        c.put("DECAVOLT:MEGAVOLT", Mul(Pow(10, 5), Sym("decav")));
        c.put("DECAVOLT:MICROVOLT", Mul(Rat(Int(1), Pow(10, 7)), Sym("decav")));
        c.put("DECAVOLT:MILLIVOLT", Mul(Rat(Int(1), Pow(10, 4)), Sym("decav")));
        c.put("DECAVOLT:NANOVOLT", Mul(Rat(Int(1), Pow(10, 10)), Sym("decav")));
        c.put("DECAVOLT:PETAVOLT", Mul(Pow(10, 14), Sym("decav")));
        c.put("DECAVOLT:PICOVOLT", Mul(Rat(Int(1), Pow(10, 13)), Sym("decav")));
        c.put("DECAVOLT:TERAVOLT", Mul(Pow(10, 11), Sym("decav")));
        c.put("DECAVOLT:VOLT", Mul(Rat(Int(1), Int(10)), Sym("decav")));
        c.put("DECAVOLT:YOCTOVOLT", Mul(Rat(Int(1), Pow(10, 25)), Sym("decav")));
        c.put("DECAVOLT:YOTTAVOLT", Mul(Pow(10, 23), Sym("decav")));
        c.put("DECAVOLT:ZEPTOVOLT", Mul(Rat(Int(1), Pow(10, 22)), Sym("decav")));
        c.put("DECAVOLT:ZETTAVOLT", Mul(Pow(10, 20), Sym("decav")));
        c.put("DECIVOLT:ATTOVOLT", Mul(Rat(Int(1), Pow(10, 17)), Sym("deciv")));
        c.put("DECIVOLT:CENTIVOLT", Mul(Rat(Int(1), Int(10)), Sym("deciv")));
        c.put("DECIVOLT:DECAVOLT", Mul(Int(100), Sym("deciv")));
        c.put("DECIVOLT:EXAVOLT", Mul(Pow(10, 19), Sym("deciv")));
        c.put("DECIVOLT:FEMTOVOLT", Mul(Rat(Int(1), Pow(10, 14)), Sym("deciv")));
        c.put("DECIVOLT:GIGAVOLT", Mul(Pow(10, 10), Sym("deciv")));
        c.put("DECIVOLT:HECTOVOLT", Mul(Int(1000), Sym("deciv")));
        c.put("DECIVOLT:KILOVOLT", Mul(Pow(10, 4), Sym("deciv")));
        c.put("DECIVOLT:MEGAVOLT", Mul(Pow(10, 7), Sym("deciv")));
        c.put("DECIVOLT:MICROVOLT", Mul(Rat(Int(1), Pow(10, 5)), Sym("deciv")));
        c.put("DECIVOLT:MILLIVOLT", Mul(Rat(Int(1), Int(100)), Sym("deciv")));
        c.put("DECIVOLT:NANOVOLT", Mul(Rat(Int(1), Pow(10, 8)), Sym("deciv")));
        c.put("DECIVOLT:PETAVOLT", Mul(Pow(10, 16), Sym("deciv")));
        c.put("DECIVOLT:PICOVOLT", Mul(Rat(Int(1), Pow(10, 11)), Sym("deciv")));
        c.put("DECIVOLT:TERAVOLT", Mul(Pow(10, 13), Sym("deciv")));
        c.put("DECIVOLT:VOLT", Mul(Int(10), Sym("deciv")));
        c.put("DECIVOLT:YOCTOVOLT", Mul(Rat(Int(1), Pow(10, 23)), Sym("deciv")));
        c.put("DECIVOLT:YOTTAVOLT", Mul(Pow(10, 25), Sym("deciv")));
        c.put("DECIVOLT:ZEPTOVOLT", Mul(Rat(Int(1), Pow(10, 20)), Sym("deciv")));
        c.put("DECIVOLT:ZETTAVOLT", Mul(Pow(10, 22), Sym("deciv")));
        c.put("EXAVOLT:ATTOVOLT", Mul(Rat(Int(1), Pow(10, 36)), Sym("exav")));
        c.put("EXAVOLT:CENTIVOLT", Mul(Rat(Int(1), Pow(10, 20)), Sym("exav")));
        c.put("EXAVOLT:DECAVOLT", Mul(Rat(Int(1), Pow(10, 17)), Sym("exav")));
        c.put("EXAVOLT:DECIVOLT", Mul(Rat(Int(1), Pow(10, 19)), Sym("exav")));
        c.put("EXAVOLT:FEMTOVOLT", Mul(Rat(Int(1), Pow(10, 33)), Sym("exav")));
        c.put("EXAVOLT:GIGAVOLT", Mul(Rat(Int(1), Pow(10, 9)), Sym("exav")));
        c.put("EXAVOLT:HECTOVOLT", Mul(Rat(Int(1), Pow(10, 16)), Sym("exav")));
        c.put("EXAVOLT:KILOVOLT", Mul(Rat(Int(1), Pow(10, 15)), Sym("exav")));
        c.put("EXAVOLT:MEGAVOLT", Mul(Rat(Int(1), Pow(10, 12)), Sym("exav")));
        c.put("EXAVOLT:MICROVOLT", Mul(Rat(Int(1), Pow(10, 24)), Sym("exav")));
        c.put("EXAVOLT:MILLIVOLT", Mul(Rat(Int(1), Pow(10, 21)), Sym("exav")));
        c.put("EXAVOLT:NANOVOLT", Mul(Rat(Int(1), Pow(10, 27)), Sym("exav")));
        c.put("EXAVOLT:PETAVOLT", Mul(Rat(Int(1), Int(1000)), Sym("exav")));
        c.put("EXAVOLT:PICOVOLT", Mul(Rat(Int(1), Pow(10, 30)), Sym("exav")));
        c.put("EXAVOLT:TERAVOLT", Mul(Rat(Int(1), Pow(10, 6)), Sym("exav")));
        c.put("EXAVOLT:VOLT", Mul(Rat(Int(1), Pow(10, 18)), Sym("exav")));
        c.put("EXAVOLT:YOCTOVOLT", Mul(Rat(Int(1), Pow(10, 42)), Sym("exav")));
        c.put("EXAVOLT:YOTTAVOLT", Mul(Pow(10, 6), Sym("exav")));
        c.put("EXAVOLT:ZEPTOVOLT", Mul(Rat(Int(1), Pow(10, 39)), Sym("exav")));
        c.put("EXAVOLT:ZETTAVOLT", Mul(Int(1000), Sym("exav")));
        c.put("FEMTOVOLT:ATTOVOLT", Mul(Rat(Int(1), Int(1000)), Sym("femtov")));
        c.put("FEMTOVOLT:CENTIVOLT", Mul(Pow(10, 13), Sym("femtov")));
        c.put("FEMTOVOLT:DECAVOLT", Mul(Pow(10, 16), Sym("femtov")));
        c.put("FEMTOVOLT:DECIVOLT", Mul(Pow(10, 14), Sym("femtov")));
        c.put("FEMTOVOLT:EXAVOLT", Mul(Pow(10, 33), Sym("femtov")));
        c.put("FEMTOVOLT:GIGAVOLT", Mul(Pow(10, 24), Sym("femtov")));
        c.put("FEMTOVOLT:HECTOVOLT", Mul(Pow(10, 17), Sym("femtov")));
        c.put("FEMTOVOLT:KILOVOLT", Mul(Pow(10, 18), Sym("femtov")));
        c.put("FEMTOVOLT:MEGAVOLT", Mul(Pow(10, 21), Sym("femtov")));
        c.put("FEMTOVOLT:MICROVOLT", Mul(Pow(10, 9), Sym("femtov")));
        c.put("FEMTOVOLT:MILLIVOLT", Mul(Pow(10, 12), Sym("femtov")));
        c.put("FEMTOVOLT:NANOVOLT", Mul(Pow(10, 6), Sym("femtov")));
        c.put("FEMTOVOLT:PETAVOLT", Mul(Pow(10, 30), Sym("femtov")));
        c.put("FEMTOVOLT:PICOVOLT", Mul(Int(1000), Sym("femtov")));
        c.put("FEMTOVOLT:TERAVOLT", Mul(Pow(10, 27), Sym("femtov")));
        c.put("FEMTOVOLT:VOLT", Mul(Pow(10, 15), Sym("femtov")));
        c.put("FEMTOVOLT:YOCTOVOLT", Mul(Rat(Int(1), Pow(10, 9)), Sym("femtov")));
        c.put("FEMTOVOLT:YOTTAVOLT", Mul(Pow(10, 39), Sym("femtov")));
        c.put("FEMTOVOLT:ZEPTOVOLT", Mul(Rat(Int(1), Pow(10, 6)), Sym("femtov")));
        c.put("FEMTOVOLT:ZETTAVOLT", Mul(Pow(10, 36), Sym("femtov")));
        c.put("GIGAVOLT:ATTOVOLT", Mul(Rat(Int(1), Pow(10, 27)), Sym("gigav")));
        c.put("GIGAVOLT:CENTIVOLT", Mul(Rat(Int(1), Pow(10, 11)), Sym("gigav")));
        c.put("GIGAVOLT:DECAVOLT", Mul(Rat(Int(1), Pow(10, 8)), Sym("gigav")));
        c.put("GIGAVOLT:DECIVOLT", Mul(Rat(Int(1), Pow(10, 10)), Sym("gigav")));
        c.put("GIGAVOLT:EXAVOLT", Mul(Pow(10, 9), Sym("gigav")));
        c.put("GIGAVOLT:FEMTOVOLT", Mul(Rat(Int(1), Pow(10, 24)), Sym("gigav")));
        c.put("GIGAVOLT:HECTOVOLT", Mul(Rat(Int(1), Pow(10, 7)), Sym("gigav")));
        c.put("GIGAVOLT:KILOVOLT", Mul(Rat(Int(1), Pow(10, 6)), Sym("gigav")));
        c.put("GIGAVOLT:MEGAVOLT", Mul(Rat(Int(1), Int(1000)), Sym("gigav")));
        c.put("GIGAVOLT:MICROVOLT", Mul(Rat(Int(1), Pow(10, 15)), Sym("gigav")));
        c.put("GIGAVOLT:MILLIVOLT", Mul(Rat(Int(1), Pow(10, 12)), Sym("gigav")));
        c.put("GIGAVOLT:NANOVOLT", Mul(Rat(Int(1), Pow(10, 18)), Sym("gigav")));
        c.put("GIGAVOLT:PETAVOLT", Mul(Pow(10, 6), Sym("gigav")));
        c.put("GIGAVOLT:PICOVOLT", Mul(Rat(Int(1), Pow(10, 21)), Sym("gigav")));
        c.put("GIGAVOLT:TERAVOLT", Mul(Int(1000), Sym("gigav")));
        c.put("GIGAVOLT:VOLT", Mul(Rat(Int(1), Pow(10, 9)), Sym("gigav")));
        c.put("GIGAVOLT:YOCTOVOLT", Mul(Rat(Int(1), Pow(10, 33)), Sym("gigav")));
        c.put("GIGAVOLT:YOTTAVOLT", Mul(Pow(10, 15), Sym("gigav")));
        c.put("GIGAVOLT:ZEPTOVOLT", Mul(Rat(Int(1), Pow(10, 30)), Sym("gigav")));
        c.put("GIGAVOLT:ZETTAVOLT", Mul(Pow(10, 12), Sym("gigav")));
        c.put("HECTOVOLT:ATTOVOLT", Mul(Rat(Int(1), Pow(10, 20)), Sym("hectov")));
        c.put("HECTOVOLT:CENTIVOLT", Mul(Rat(Int(1), Pow(10, 4)), Sym("hectov")));
        c.put("HECTOVOLT:DECAVOLT", Mul(Rat(Int(1), Int(10)), Sym("hectov")));
        c.put("HECTOVOLT:DECIVOLT", Mul(Rat(Int(1), Int(1000)), Sym("hectov")));
        c.put("HECTOVOLT:EXAVOLT", Mul(Pow(10, 16), Sym("hectov")));
        c.put("HECTOVOLT:FEMTOVOLT", Mul(Rat(Int(1), Pow(10, 17)), Sym("hectov")));
        c.put("HECTOVOLT:GIGAVOLT", Mul(Pow(10, 7), Sym("hectov")));
        c.put("HECTOVOLT:KILOVOLT", Mul(Int(10), Sym("hectov")));
        c.put("HECTOVOLT:MEGAVOLT", Mul(Pow(10, 4), Sym("hectov")));
        c.put("HECTOVOLT:MICROVOLT", Mul(Rat(Int(1), Pow(10, 8)), Sym("hectov")));
        c.put("HECTOVOLT:MILLIVOLT", Mul(Rat(Int(1), Pow(10, 5)), Sym("hectov")));
        c.put("HECTOVOLT:NANOVOLT", Mul(Rat(Int(1), Pow(10, 11)), Sym("hectov")));
        c.put("HECTOVOLT:PETAVOLT", Mul(Pow(10, 13), Sym("hectov")));
        c.put("HECTOVOLT:PICOVOLT", Mul(Rat(Int(1), Pow(10, 14)), Sym("hectov")));
        c.put("HECTOVOLT:TERAVOLT", Mul(Pow(10, 10), Sym("hectov")));
        c.put("HECTOVOLT:VOLT", Mul(Rat(Int(1), Int(100)), Sym("hectov")));
        c.put("HECTOVOLT:YOCTOVOLT", Mul(Rat(Int(1), Pow(10, 26)), Sym("hectov")));
        c.put("HECTOVOLT:YOTTAVOLT", Mul(Pow(10, 22), Sym("hectov")));
        c.put("HECTOVOLT:ZEPTOVOLT", Mul(Rat(Int(1), Pow(10, 23)), Sym("hectov")));
        c.put("HECTOVOLT:ZETTAVOLT", Mul(Pow(10, 19), Sym("hectov")));
        c.put("KILOVOLT:ATTOVOLT", Mul(Rat(Int(1), Pow(10, 21)), Sym("kilov")));
        c.put("KILOVOLT:CENTIVOLT", Mul(Rat(Int(1), Pow(10, 5)), Sym("kilov")));
        c.put("KILOVOLT:DECAVOLT", Mul(Rat(Int(1), Int(100)), Sym("kilov")));
        c.put("KILOVOLT:DECIVOLT", Mul(Rat(Int(1), Pow(10, 4)), Sym("kilov")));
        c.put("KILOVOLT:EXAVOLT", Mul(Pow(10, 15), Sym("kilov")));
        c.put("KILOVOLT:FEMTOVOLT", Mul(Rat(Int(1), Pow(10, 18)), Sym("kilov")));
        c.put("KILOVOLT:GIGAVOLT", Mul(Pow(10, 6), Sym("kilov")));
        c.put("KILOVOLT:HECTOVOLT", Mul(Rat(Int(1), Int(10)), Sym("kilov")));
        c.put("KILOVOLT:MEGAVOLT", Mul(Int(1000), Sym("kilov")));
        c.put("KILOVOLT:MICROVOLT", Mul(Rat(Int(1), Pow(10, 9)), Sym("kilov")));
        c.put("KILOVOLT:MILLIVOLT", Mul(Rat(Int(1), Pow(10, 6)), Sym("kilov")));
        c.put("KILOVOLT:NANOVOLT", Mul(Rat(Int(1), Pow(10, 12)), Sym("kilov")));
        c.put("KILOVOLT:PETAVOLT", Mul(Pow(10, 12), Sym("kilov")));
        c.put("KILOVOLT:PICOVOLT", Mul(Rat(Int(1), Pow(10, 15)), Sym("kilov")));
        c.put("KILOVOLT:TERAVOLT", Mul(Pow(10, 9), Sym("kilov")));
        c.put("KILOVOLT:VOLT", Mul(Rat(Int(1), Int(1000)), Sym("kilov")));
        c.put("KILOVOLT:YOCTOVOLT", Mul(Rat(Int(1), Pow(10, 27)), Sym("kilov")));
        c.put("KILOVOLT:YOTTAVOLT", Mul(Pow(10, 21), Sym("kilov")));
        c.put("KILOVOLT:ZEPTOVOLT", Mul(Rat(Int(1), Pow(10, 24)), Sym("kilov")));
        c.put("KILOVOLT:ZETTAVOLT", Mul(Pow(10, 18), Sym("kilov")));
        c.put("MEGAVOLT:ATTOVOLT", Mul(Rat(Int(1), Pow(10, 24)), Sym("megav")));
        c.put("MEGAVOLT:CENTIVOLT", Mul(Rat(Int(1), Pow(10, 8)), Sym("megav")));
        c.put("MEGAVOLT:DECAVOLT", Mul(Rat(Int(1), Pow(10, 5)), Sym("megav")));
        c.put("MEGAVOLT:DECIVOLT", Mul(Rat(Int(1), Pow(10, 7)), Sym("megav")));
        c.put("MEGAVOLT:EXAVOLT", Mul(Pow(10, 12), Sym("megav")));
        c.put("MEGAVOLT:FEMTOVOLT", Mul(Rat(Int(1), Pow(10, 21)), Sym("megav")));
        c.put("MEGAVOLT:GIGAVOLT", Mul(Int(1000), Sym("megav")));
        c.put("MEGAVOLT:HECTOVOLT", Mul(Rat(Int(1), Pow(10, 4)), Sym("megav")));
        c.put("MEGAVOLT:KILOVOLT", Mul(Rat(Int(1), Int(1000)), Sym("megav")));
        c.put("MEGAVOLT:MICROVOLT", Mul(Rat(Int(1), Pow(10, 12)), Sym("megav")));
        c.put("MEGAVOLT:MILLIVOLT", Mul(Rat(Int(1), Pow(10, 9)), Sym("megav")));
        c.put("MEGAVOLT:NANOVOLT", Mul(Rat(Int(1), Pow(10, 15)), Sym("megav")));
        c.put("MEGAVOLT:PETAVOLT", Mul(Pow(10, 9), Sym("megav")));
        c.put("MEGAVOLT:PICOVOLT", Mul(Rat(Int(1), Pow(10, 18)), Sym("megav")));
        c.put("MEGAVOLT:TERAVOLT", Mul(Pow(10, 6), Sym("megav")));
        c.put("MEGAVOLT:VOLT", Mul(Rat(Int(1), Pow(10, 6)), Sym("megav")));
        c.put("MEGAVOLT:YOCTOVOLT", Mul(Rat(Int(1), Pow(10, 30)), Sym("megav")));
        c.put("MEGAVOLT:YOTTAVOLT", Mul(Pow(10, 18), Sym("megav")));
        c.put("MEGAVOLT:ZEPTOVOLT", Mul(Rat(Int(1), Pow(10, 27)), Sym("megav")));
        c.put("MEGAVOLT:ZETTAVOLT", Mul(Pow(10, 15), Sym("megav")));
        c.put("MICROVOLT:ATTOVOLT", Mul(Rat(Int(1), Pow(10, 12)), Sym("microv")));
        c.put("MICROVOLT:CENTIVOLT", Mul(Pow(10, 4), Sym("microv")));
        c.put("MICROVOLT:DECAVOLT", Mul(Pow(10, 7), Sym("microv")));
        c.put("MICROVOLT:DECIVOLT", Mul(Pow(10, 5), Sym("microv")));
        c.put("MICROVOLT:EXAVOLT", Mul(Pow(10, 24), Sym("microv")));
        c.put("MICROVOLT:FEMTOVOLT", Mul(Rat(Int(1), Pow(10, 9)), Sym("microv")));
        c.put("MICROVOLT:GIGAVOLT", Mul(Pow(10, 15), Sym("microv")));
        c.put("MICROVOLT:HECTOVOLT", Mul(Pow(10, 8), Sym("microv")));
        c.put("MICROVOLT:KILOVOLT", Mul(Pow(10, 9), Sym("microv")));
        c.put("MICROVOLT:MEGAVOLT", Mul(Pow(10, 12), Sym("microv")));
        c.put("MICROVOLT:MILLIVOLT", Mul(Int(1000), Sym("microv")));
        c.put("MICROVOLT:NANOVOLT", Mul(Rat(Int(1), Int(1000)), Sym("microv")));
        c.put("MICROVOLT:PETAVOLT", Mul(Pow(10, 21), Sym("microv")));
        c.put("MICROVOLT:PICOVOLT", Mul(Rat(Int(1), Pow(10, 6)), Sym("microv")));
        c.put("MICROVOLT:TERAVOLT", Mul(Pow(10, 18), Sym("microv")));
        c.put("MICROVOLT:VOLT", Mul(Pow(10, 6), Sym("microv")));
        c.put("MICROVOLT:YOCTOVOLT", Mul(Rat(Int(1), Pow(10, 18)), Sym("microv")));
        c.put("MICROVOLT:YOTTAVOLT", Mul(Pow(10, 30), Sym("microv")));
        c.put("MICROVOLT:ZEPTOVOLT", Mul(Rat(Int(1), Pow(10, 15)), Sym("microv")));
        c.put("MICROVOLT:ZETTAVOLT", Mul(Pow(10, 27), Sym("microv")));
        c.put("MILLIVOLT:ATTOVOLT", Mul(Rat(Int(1), Pow(10, 15)), Sym("milliv")));
        c.put("MILLIVOLT:CENTIVOLT", Mul(Int(10), Sym("milliv")));
        c.put("MILLIVOLT:DECAVOLT", Mul(Pow(10, 4), Sym("milliv")));
        c.put("MILLIVOLT:DECIVOLT", Mul(Int(100), Sym("milliv")));
        c.put("MILLIVOLT:EXAVOLT", Mul(Pow(10, 21), Sym("milliv")));
        c.put("MILLIVOLT:FEMTOVOLT", Mul(Rat(Int(1), Pow(10, 12)), Sym("milliv")));
        c.put("MILLIVOLT:GIGAVOLT", Mul(Pow(10, 12), Sym("milliv")));
        c.put("MILLIVOLT:HECTOVOLT", Mul(Pow(10, 5), Sym("milliv")));
        c.put("MILLIVOLT:KILOVOLT", Mul(Pow(10, 6), Sym("milliv")));
        c.put("MILLIVOLT:MEGAVOLT", Mul(Pow(10, 9), Sym("milliv")));
        c.put("MILLIVOLT:MICROVOLT", Mul(Rat(Int(1), Int(1000)), Sym("milliv")));
        c.put("MILLIVOLT:NANOVOLT", Mul(Rat(Int(1), Pow(10, 6)), Sym("milliv")));
        c.put("MILLIVOLT:PETAVOLT", Mul(Pow(10, 18), Sym("milliv")));
        c.put("MILLIVOLT:PICOVOLT", Mul(Rat(Int(1), Pow(10, 9)), Sym("milliv")));
        c.put("MILLIVOLT:TERAVOLT", Mul(Pow(10, 15), Sym("milliv")));
        c.put("MILLIVOLT:VOLT", Mul(Int(1000), Sym("milliv")));
        c.put("MILLIVOLT:YOCTOVOLT", Mul(Rat(Int(1), Pow(10, 21)), Sym("milliv")));
        c.put("MILLIVOLT:YOTTAVOLT", Mul(Pow(10, 27), Sym("milliv")));
        c.put("MILLIVOLT:ZEPTOVOLT", Mul(Rat(Int(1), Pow(10, 18)), Sym("milliv")));
        c.put("MILLIVOLT:ZETTAVOLT", Mul(Pow(10, 24), Sym("milliv")));
        c.put("NANOVOLT:ATTOVOLT", Mul(Rat(Int(1), Pow(10, 9)), Sym("nanov")));
        c.put("NANOVOLT:CENTIVOLT", Mul(Pow(10, 7), Sym("nanov")));
        c.put("NANOVOLT:DECAVOLT", Mul(Pow(10, 10), Sym("nanov")));
        c.put("NANOVOLT:DECIVOLT", Mul(Pow(10, 8), Sym("nanov")));
        c.put("NANOVOLT:EXAVOLT", Mul(Pow(10, 27), Sym("nanov")));
        c.put("NANOVOLT:FEMTOVOLT", Mul(Rat(Int(1), Pow(10, 6)), Sym("nanov")));
        c.put("NANOVOLT:GIGAVOLT", Mul(Pow(10, 18), Sym("nanov")));
        c.put("NANOVOLT:HECTOVOLT", Mul(Pow(10, 11), Sym("nanov")));
        c.put("NANOVOLT:KILOVOLT", Mul(Pow(10, 12), Sym("nanov")));
        c.put("NANOVOLT:MEGAVOLT", Mul(Pow(10, 15), Sym("nanov")));
        c.put("NANOVOLT:MICROVOLT", Mul(Int(1000), Sym("nanov")));
        c.put("NANOVOLT:MILLIVOLT", Mul(Pow(10, 6), Sym("nanov")));
        c.put("NANOVOLT:PETAVOLT", Mul(Pow(10, 24), Sym("nanov")));
        c.put("NANOVOLT:PICOVOLT", Mul(Rat(Int(1), Int(1000)), Sym("nanov")));
        c.put("NANOVOLT:TERAVOLT", Mul(Pow(10, 21), Sym("nanov")));
        c.put("NANOVOLT:VOLT", Mul(Pow(10, 9), Sym("nanov")));
        c.put("NANOVOLT:YOCTOVOLT", Mul(Rat(Int(1), Pow(10, 15)), Sym("nanov")));
        c.put("NANOVOLT:YOTTAVOLT", Mul(Pow(10, 33), Sym("nanov")));
        c.put("NANOVOLT:ZEPTOVOLT", Mul(Rat(Int(1), Pow(10, 12)), Sym("nanov")));
        c.put("NANOVOLT:ZETTAVOLT", Mul(Pow(10, 30), Sym("nanov")));
        c.put("PETAVOLT:ATTOVOLT", Mul(Rat(Int(1), Pow(10, 33)), Sym("petav")));
        c.put("PETAVOLT:CENTIVOLT", Mul(Rat(Int(1), Pow(10, 17)), Sym("petav")));
        c.put("PETAVOLT:DECAVOLT", Mul(Rat(Int(1), Pow(10, 14)), Sym("petav")));
        c.put("PETAVOLT:DECIVOLT", Mul(Rat(Int(1), Pow(10, 16)), Sym("petav")));
        c.put("PETAVOLT:EXAVOLT", Mul(Int(1000), Sym("petav")));
        c.put("PETAVOLT:FEMTOVOLT", Mul(Rat(Int(1), Pow(10, 30)), Sym("petav")));
        c.put("PETAVOLT:GIGAVOLT", Mul(Rat(Int(1), Pow(10, 6)), Sym("petav")));
        c.put("PETAVOLT:HECTOVOLT", Mul(Rat(Int(1), Pow(10, 13)), Sym("petav")));
        c.put("PETAVOLT:KILOVOLT", Mul(Rat(Int(1), Pow(10, 12)), Sym("petav")));
        c.put("PETAVOLT:MEGAVOLT", Mul(Rat(Int(1), Pow(10, 9)), Sym("petav")));
        c.put("PETAVOLT:MICROVOLT", Mul(Rat(Int(1), Pow(10, 21)), Sym("petav")));
        c.put("PETAVOLT:MILLIVOLT", Mul(Rat(Int(1), Pow(10, 18)), Sym("petav")));
        c.put("PETAVOLT:NANOVOLT", Mul(Rat(Int(1), Pow(10, 24)), Sym("petav")));
        c.put("PETAVOLT:PICOVOLT", Mul(Rat(Int(1), Pow(10, 27)), Sym("petav")));
        c.put("PETAVOLT:TERAVOLT", Mul(Rat(Int(1), Int(1000)), Sym("petav")));
        c.put("PETAVOLT:VOLT", Mul(Rat(Int(1), Pow(10, 15)), Sym("petav")));
        c.put("PETAVOLT:YOCTOVOLT", Mul(Rat(Int(1), Pow(10, 39)), Sym("petav")));
        c.put("PETAVOLT:YOTTAVOLT", Mul(Pow(10, 9), Sym("petav")));
        c.put("PETAVOLT:ZEPTOVOLT", Mul(Rat(Int(1), Pow(10, 36)), Sym("petav")));
        c.put("PETAVOLT:ZETTAVOLT", Mul(Pow(10, 6), Sym("petav")));
        c.put("PICOVOLT:ATTOVOLT", Mul(Rat(Int(1), Pow(10, 6)), Sym("picov")));
        c.put("PICOVOLT:CENTIVOLT", Mul(Pow(10, 10), Sym("picov")));
        c.put("PICOVOLT:DECAVOLT", Mul(Pow(10, 13), Sym("picov")));
        c.put("PICOVOLT:DECIVOLT", Mul(Pow(10, 11), Sym("picov")));
        c.put("PICOVOLT:EXAVOLT", Mul(Pow(10, 30), Sym("picov")));
        c.put("PICOVOLT:FEMTOVOLT", Mul(Rat(Int(1), Int(1000)), Sym("picov")));
        c.put("PICOVOLT:GIGAVOLT", Mul(Pow(10, 21), Sym("picov")));
        c.put("PICOVOLT:HECTOVOLT", Mul(Pow(10, 14), Sym("picov")));
        c.put("PICOVOLT:KILOVOLT", Mul(Pow(10, 15), Sym("picov")));
        c.put("PICOVOLT:MEGAVOLT", Mul(Pow(10, 18), Sym("picov")));
        c.put("PICOVOLT:MICROVOLT", Mul(Pow(10, 6), Sym("picov")));
        c.put("PICOVOLT:MILLIVOLT", Mul(Pow(10, 9), Sym("picov")));
        c.put("PICOVOLT:NANOVOLT", Mul(Int(1000), Sym("picov")));
        c.put("PICOVOLT:PETAVOLT", Mul(Pow(10, 27), Sym("picov")));
        c.put("PICOVOLT:TERAVOLT", Mul(Pow(10, 24), Sym("picov")));
        c.put("PICOVOLT:VOLT", Mul(Pow(10, 12), Sym("picov")));
        c.put("PICOVOLT:YOCTOVOLT", Mul(Rat(Int(1), Pow(10, 12)), Sym("picov")));
        c.put("PICOVOLT:YOTTAVOLT", Mul(Pow(10, 36), Sym("picov")));
        c.put("PICOVOLT:ZEPTOVOLT", Mul(Rat(Int(1), Pow(10, 9)), Sym("picov")));
        c.put("PICOVOLT:ZETTAVOLT", Mul(Pow(10, 33), Sym("picov")));
        c.put("TERAVOLT:ATTOVOLT", Mul(Rat(Int(1), Pow(10, 30)), Sym("terav")));
        c.put("TERAVOLT:CENTIVOLT", Mul(Rat(Int(1), Pow(10, 14)), Sym("terav")));
        c.put("TERAVOLT:DECAVOLT", Mul(Rat(Int(1), Pow(10, 11)), Sym("terav")));
        c.put("TERAVOLT:DECIVOLT", Mul(Rat(Int(1), Pow(10, 13)), Sym("terav")));
        c.put("TERAVOLT:EXAVOLT", Mul(Pow(10, 6), Sym("terav")));
        c.put("TERAVOLT:FEMTOVOLT", Mul(Rat(Int(1), Pow(10, 27)), Sym("terav")));
        c.put("TERAVOLT:GIGAVOLT", Mul(Rat(Int(1), Int(1000)), Sym("terav")));
        c.put("TERAVOLT:HECTOVOLT", Mul(Rat(Int(1), Pow(10, 10)), Sym("terav")));
        c.put("TERAVOLT:KILOVOLT", Mul(Rat(Int(1), Pow(10, 9)), Sym("terav")));
        c.put("TERAVOLT:MEGAVOLT", Mul(Rat(Int(1), Pow(10, 6)), Sym("terav")));
        c.put("TERAVOLT:MICROVOLT", Mul(Rat(Int(1), Pow(10, 18)), Sym("terav")));
        c.put("TERAVOLT:MILLIVOLT", Mul(Rat(Int(1), Pow(10, 15)), Sym("terav")));
        c.put("TERAVOLT:NANOVOLT", Mul(Rat(Int(1), Pow(10, 21)), Sym("terav")));
        c.put("TERAVOLT:PETAVOLT", Mul(Int(1000), Sym("terav")));
        c.put("TERAVOLT:PICOVOLT", Mul(Rat(Int(1), Pow(10, 24)), Sym("terav")));
        c.put("TERAVOLT:VOLT", Mul(Rat(Int(1), Pow(10, 12)), Sym("terav")));
        c.put("TERAVOLT:YOCTOVOLT", Mul(Rat(Int(1), Pow(10, 36)), Sym("terav")));
        c.put("TERAVOLT:YOTTAVOLT", Mul(Pow(10, 12), Sym("terav")));
        c.put("TERAVOLT:ZEPTOVOLT", Mul(Rat(Int(1), Pow(10, 33)), Sym("terav")));
        c.put("TERAVOLT:ZETTAVOLT", Mul(Pow(10, 9), Sym("terav")));
        c.put("VOLT:ATTOVOLT", Mul(Rat(Int(1), Pow(10, 18)), Sym("v")));
        c.put("VOLT:CENTIVOLT", Mul(Rat(Int(1), Int(100)), Sym("v")));
        c.put("VOLT:DECAVOLT", Mul(Int(10), Sym("v")));
        c.put("VOLT:DECIVOLT", Mul(Rat(Int(1), Int(10)), Sym("v")));
        c.put("VOLT:EXAVOLT", Mul(Pow(10, 18), Sym("v")));
        c.put("VOLT:FEMTOVOLT", Mul(Rat(Int(1), Pow(10, 15)), Sym("v")));
        c.put("VOLT:GIGAVOLT", Mul(Pow(10, 9), Sym("v")));
        c.put("VOLT:HECTOVOLT", Mul(Int(100), Sym("v")));
        c.put("VOLT:KILOVOLT", Mul(Int(1000), Sym("v")));
        c.put("VOLT:MEGAVOLT", Mul(Pow(10, 6), Sym("v")));
        c.put("VOLT:MICROVOLT", Mul(Rat(Int(1), Pow(10, 6)), Sym("v")));
        c.put("VOLT:MILLIVOLT", Mul(Rat(Int(1), Int(1000)), Sym("v")));
        c.put("VOLT:NANOVOLT", Mul(Rat(Int(1), Pow(10, 9)), Sym("v")));
        c.put("VOLT:PETAVOLT", Mul(Pow(10, 15), Sym("v")));
        c.put("VOLT:PICOVOLT", Mul(Rat(Int(1), Pow(10, 12)), Sym("v")));
        c.put("VOLT:TERAVOLT", Mul(Pow(10, 12), Sym("v")));
        c.put("VOLT:YOCTOVOLT", Mul(Rat(Int(1), Pow(10, 24)), Sym("v")));
        c.put("VOLT:YOTTAVOLT", Mul(Pow(10, 24), Sym("v")));
        c.put("VOLT:ZEPTOVOLT", Mul(Rat(Int(1), Pow(10, 21)), Sym("v")));
        c.put("VOLT:ZETTAVOLT", Mul(Pow(10, 21), Sym("v")));
        c.put("YOCTOVOLT:ATTOVOLT", Mul(Pow(10, 6), Sym("yoctov")));
        c.put("YOCTOVOLT:CENTIVOLT", Mul(Pow(10, 22), Sym("yoctov")));
        c.put("YOCTOVOLT:DECAVOLT", Mul(Pow(10, 25), Sym("yoctov")));
        c.put("YOCTOVOLT:DECIVOLT", Mul(Pow(10, 23), Sym("yoctov")));
        c.put("YOCTOVOLT:EXAVOLT", Mul(Pow(10, 42), Sym("yoctov")));
        c.put("YOCTOVOLT:FEMTOVOLT", Mul(Pow(10, 9), Sym("yoctov")));
        c.put("YOCTOVOLT:GIGAVOLT", Mul(Pow(10, 33), Sym("yoctov")));
        c.put("YOCTOVOLT:HECTOVOLT", Mul(Pow(10, 26), Sym("yoctov")));
        c.put("YOCTOVOLT:KILOVOLT", Mul(Pow(10, 27), Sym("yoctov")));
        c.put("YOCTOVOLT:MEGAVOLT", Mul(Pow(10, 30), Sym("yoctov")));
        c.put("YOCTOVOLT:MICROVOLT", Mul(Pow(10, 18), Sym("yoctov")));
        c.put("YOCTOVOLT:MILLIVOLT", Mul(Pow(10, 21), Sym("yoctov")));
        c.put("YOCTOVOLT:NANOVOLT", Mul(Pow(10, 15), Sym("yoctov")));
        c.put("YOCTOVOLT:PETAVOLT", Mul(Pow(10, 39), Sym("yoctov")));
        c.put("YOCTOVOLT:PICOVOLT", Mul(Pow(10, 12), Sym("yoctov")));
        c.put("YOCTOVOLT:TERAVOLT", Mul(Pow(10, 36), Sym("yoctov")));
        c.put("YOCTOVOLT:VOLT", Mul(Pow(10, 24), Sym("yoctov")));
        c.put("YOCTOVOLT:YOTTAVOLT", Mul(Pow(10, 48), Sym("yoctov")));
        c.put("YOCTOVOLT:ZEPTOVOLT", Mul(Int(1000), Sym("yoctov")));
        c.put("YOCTOVOLT:ZETTAVOLT", Mul(Pow(10, 45), Sym("yoctov")));
        c.put("YOTTAVOLT:ATTOVOLT", Mul(Rat(Int(1), Pow(10, 42)), Sym("yottav")));
        c.put("YOTTAVOLT:CENTIVOLT", Mul(Rat(Int(1), Pow(10, 26)), Sym("yottav")));
        c.put("YOTTAVOLT:DECAVOLT", Mul(Rat(Int(1), Pow(10, 23)), Sym("yottav")));
        c.put("YOTTAVOLT:DECIVOLT", Mul(Rat(Int(1), Pow(10, 25)), Sym("yottav")));
        c.put("YOTTAVOLT:EXAVOLT", Mul(Rat(Int(1), Pow(10, 6)), Sym("yottav")));
        c.put("YOTTAVOLT:FEMTOVOLT", Mul(Rat(Int(1), Pow(10, 39)), Sym("yottav")));
        c.put("YOTTAVOLT:GIGAVOLT", Mul(Rat(Int(1), Pow(10, 15)), Sym("yottav")));
        c.put("YOTTAVOLT:HECTOVOLT", Mul(Rat(Int(1), Pow(10, 22)), Sym("yottav")));
        c.put("YOTTAVOLT:KILOVOLT", Mul(Rat(Int(1), Pow(10, 21)), Sym("yottav")));
        c.put("YOTTAVOLT:MEGAVOLT", Mul(Rat(Int(1), Pow(10, 18)), Sym("yottav")));
        c.put("YOTTAVOLT:MICROVOLT", Mul(Rat(Int(1), Pow(10, 30)), Sym("yottav")));
        c.put("YOTTAVOLT:MILLIVOLT", Mul(Rat(Int(1), Pow(10, 27)), Sym("yottav")));
        c.put("YOTTAVOLT:NANOVOLT", Mul(Rat(Int(1), Pow(10, 33)), Sym("yottav")));
        c.put("YOTTAVOLT:PETAVOLT", Mul(Rat(Int(1), Pow(10, 9)), Sym("yottav")));
        c.put("YOTTAVOLT:PICOVOLT", Mul(Rat(Int(1), Pow(10, 36)), Sym("yottav")));
        c.put("YOTTAVOLT:TERAVOLT", Mul(Rat(Int(1), Pow(10, 12)), Sym("yottav")));
        c.put("YOTTAVOLT:VOLT", Mul(Rat(Int(1), Pow(10, 24)), Sym("yottav")));
        c.put("YOTTAVOLT:YOCTOVOLT", Mul(Rat(Int(1), Pow(10, 48)), Sym("yottav")));
        c.put("YOTTAVOLT:ZEPTOVOLT", Mul(Rat(Int(1), Pow(10, 45)), Sym("yottav")));
        c.put("YOTTAVOLT:ZETTAVOLT", Mul(Rat(Int(1), Int(1000)), Sym("yottav")));
        c.put("ZEPTOVOLT:ATTOVOLT", Mul(Int(1000), Sym("zeptov")));
        c.put("ZEPTOVOLT:CENTIVOLT", Mul(Pow(10, 19), Sym("zeptov")));
        c.put("ZEPTOVOLT:DECAVOLT", Mul(Pow(10, 22), Sym("zeptov")));
        c.put("ZEPTOVOLT:DECIVOLT", Mul(Pow(10, 20), Sym("zeptov")));
        c.put("ZEPTOVOLT:EXAVOLT", Mul(Pow(10, 39), Sym("zeptov")));
        c.put("ZEPTOVOLT:FEMTOVOLT", Mul(Pow(10, 6), Sym("zeptov")));
        c.put("ZEPTOVOLT:GIGAVOLT", Mul(Pow(10, 30), Sym("zeptov")));
        c.put("ZEPTOVOLT:HECTOVOLT", Mul(Pow(10, 23), Sym("zeptov")));
        c.put("ZEPTOVOLT:KILOVOLT", Mul(Pow(10, 24), Sym("zeptov")));
        c.put("ZEPTOVOLT:MEGAVOLT", Mul(Pow(10, 27), Sym("zeptov")));
        c.put("ZEPTOVOLT:MICROVOLT", Mul(Pow(10, 15), Sym("zeptov")));
        c.put("ZEPTOVOLT:MILLIVOLT", Mul(Pow(10, 18), Sym("zeptov")));
        c.put("ZEPTOVOLT:NANOVOLT", Mul(Pow(10, 12), Sym("zeptov")));
        c.put("ZEPTOVOLT:PETAVOLT", Mul(Pow(10, 36), Sym("zeptov")));
        c.put("ZEPTOVOLT:PICOVOLT", Mul(Pow(10, 9), Sym("zeptov")));
        c.put("ZEPTOVOLT:TERAVOLT", Mul(Pow(10, 33), Sym("zeptov")));
        c.put("ZEPTOVOLT:VOLT", Mul(Pow(10, 21), Sym("zeptov")));
        c.put("ZEPTOVOLT:YOCTOVOLT", Mul(Rat(Int(1), Int(1000)), Sym("zeptov")));
        c.put("ZEPTOVOLT:YOTTAVOLT", Mul(Pow(10, 45), Sym("zeptov")));
        c.put("ZEPTOVOLT:ZETTAVOLT", Mul(Pow(10, 42), Sym("zeptov")));
        c.put("ZETTAVOLT:ATTOVOLT", Mul(Rat(Int(1), Pow(10, 39)), Sym("zettav")));
        c.put("ZETTAVOLT:CENTIVOLT", Mul(Rat(Int(1), Pow(10, 23)), Sym("zettav")));
        c.put("ZETTAVOLT:DECAVOLT", Mul(Rat(Int(1), Pow(10, 20)), Sym("zettav")));
        c.put("ZETTAVOLT:DECIVOLT", Mul(Rat(Int(1), Pow(10, 22)), Sym("zettav")));
        c.put("ZETTAVOLT:EXAVOLT", Mul(Rat(Int(1), Int(1000)), Sym("zettav")));
        c.put("ZETTAVOLT:FEMTOVOLT", Mul(Rat(Int(1), Pow(10, 36)), Sym("zettav")));
        c.put("ZETTAVOLT:GIGAVOLT", Mul(Rat(Int(1), Pow(10, 12)), Sym("zettav")));
        c.put("ZETTAVOLT:HECTOVOLT", Mul(Rat(Int(1), Pow(10, 19)), Sym("zettav")));
        c.put("ZETTAVOLT:KILOVOLT", Mul(Rat(Int(1), Pow(10, 18)), Sym("zettav")));
        c.put("ZETTAVOLT:MEGAVOLT", Mul(Rat(Int(1), Pow(10, 15)), Sym("zettav")));
        c.put("ZETTAVOLT:MICROVOLT", Mul(Rat(Int(1), Pow(10, 27)), Sym("zettav")));
        c.put("ZETTAVOLT:MILLIVOLT", Mul(Rat(Int(1), Pow(10, 24)), Sym("zettav")));
        c.put("ZETTAVOLT:NANOVOLT", Mul(Rat(Int(1), Pow(10, 30)), Sym("zettav")));
        c.put("ZETTAVOLT:PETAVOLT", Mul(Rat(Int(1), Pow(10, 6)), Sym("zettav")));
        c.put("ZETTAVOLT:PICOVOLT", Mul(Rat(Int(1), Pow(10, 33)), Sym("zettav")));
        c.put("ZETTAVOLT:TERAVOLT", Mul(Rat(Int(1), Pow(10, 9)), Sym("zettav")));
        c.put("ZETTAVOLT:VOLT", Mul(Rat(Int(1), Pow(10, 21)), Sym("zettav")));
        c.put("ZETTAVOLT:YOCTOVOLT", Mul(Rat(Int(1), Pow(10, 45)), Sym("zettav")));
        c.put("ZETTAVOLT:YOTTAVOLT", Mul(Int(1000), Sym("zettav")));
        c.put("ZETTAVOLT:ZEPTOVOLT", Mul(Rat(Int(1), Pow(10, 42)), Sym("zettav")));
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
            Conversion conversion = conversions.get(source + ":" + target);
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
            setUnit(UnitsElectricPotential.valueOf(target));
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

