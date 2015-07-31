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

    private static Map<UnitsElectricPotential, Conversion> createMapATTOVOLT() {
        EnumMap<UnitsElectricPotential, Conversion> c =
            new EnumMap<UnitsElectricPotential, Conversion>(UnitsElectricPotential.class);
        c.put(UnitsElectricPotential.CENTIVOLT, Mul(Rat(Int(1), Pow(10, 16)), Sym("attov")));
        c.put(UnitsElectricPotential.DECAVOLT, Mul(Rat(Int(1), Pow(10, 19)), Sym("attov")));
        c.put(UnitsElectricPotential.DECIVOLT, Mul(Rat(Int(1), Pow(10, 17)), Sym("attov")));
        c.put(UnitsElectricPotential.EXAVOLT, Mul(Rat(Int(1), Pow(10, 36)), Sym("attov")));
        c.put(UnitsElectricPotential.FEMTOVOLT, Mul(Rat(Int(1), Int(1000)), Sym("attov")));
        c.put(UnitsElectricPotential.GIGAVOLT, Mul(Rat(Int(1), Pow(10, 27)), Sym("attov")));
        c.put(UnitsElectricPotential.HECTOVOLT, Mul(Rat(Int(1), Pow(10, 20)), Sym("attov")));
        c.put(UnitsElectricPotential.KILOVOLT, Mul(Rat(Int(1), Pow(10, 21)), Sym("attov")));
        c.put(UnitsElectricPotential.MEGAVOLT, Mul(Rat(Int(1), Pow(10, 24)), Sym("attov")));
        c.put(UnitsElectricPotential.MICROVOLT, Mul(Rat(Int(1), Pow(10, 12)), Sym("attov")));
        c.put(UnitsElectricPotential.MILLIVOLT, Mul(Rat(Int(1), Pow(10, 15)), Sym("attov")));
        c.put(UnitsElectricPotential.NANOVOLT, Mul(Rat(Int(1), Pow(10, 9)), Sym("attov")));
        c.put(UnitsElectricPotential.PETAVOLT, Mul(Rat(Int(1), Pow(10, 33)), Sym("attov")));
        c.put(UnitsElectricPotential.PICOVOLT, Mul(Rat(Int(1), Pow(10, 6)), Sym("attov")));
        c.put(UnitsElectricPotential.TERAVOLT, Mul(Rat(Int(1), Pow(10, 30)), Sym("attov")));
        c.put(UnitsElectricPotential.VOLT, Mul(Rat(Int(1), Pow(10, 18)), Sym("attov")));
        c.put(UnitsElectricPotential.YOCTOVOLT, Mul(Pow(10, 6), Sym("attov")));
        c.put(UnitsElectricPotential.YOTTAVOLT, Mul(Rat(Int(1), Pow(10, 42)), Sym("attov")));
        c.put(UnitsElectricPotential.ZEPTOVOLT, Mul(Int(1000), Sym("attov")));
        c.put(UnitsElectricPotential.ZETTAVOLT, Mul(Rat(Int(1), Pow(10, 39)), Sym("attov")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsElectricPotential, Conversion> createMapCENTIVOLT() {
        EnumMap<UnitsElectricPotential, Conversion> c =
            new EnumMap<UnitsElectricPotential, Conversion>(UnitsElectricPotential.class);
        c.put(UnitsElectricPotential.ATTOVOLT, Mul(Pow(10, 16), Sym("centiv")));
        c.put(UnitsElectricPotential.DECAVOLT, Mul(Rat(Int(1), Int(1000)), Sym("centiv")));
        c.put(UnitsElectricPotential.DECIVOLT, Mul(Rat(Int(1), Int(10)), Sym("centiv")));
        c.put(UnitsElectricPotential.EXAVOLT, Mul(Rat(Int(1), Pow(10, 20)), Sym("centiv")));
        c.put(UnitsElectricPotential.FEMTOVOLT, Mul(Pow(10, 13), Sym("centiv")));
        c.put(UnitsElectricPotential.GIGAVOLT, Mul(Rat(Int(1), Pow(10, 11)), Sym("centiv")));
        c.put(UnitsElectricPotential.HECTOVOLT, Mul(Rat(Int(1), Pow(10, 4)), Sym("centiv")));
        c.put(UnitsElectricPotential.KILOVOLT, Mul(Rat(Int(1), Pow(10, 5)), Sym("centiv")));
        c.put(UnitsElectricPotential.MEGAVOLT, Mul(Rat(Int(1), Pow(10, 8)), Sym("centiv")));
        c.put(UnitsElectricPotential.MICROVOLT, Mul(Pow(10, 4), Sym("centiv")));
        c.put(UnitsElectricPotential.MILLIVOLT, Mul(Int(10), Sym("centiv")));
        c.put(UnitsElectricPotential.NANOVOLT, Mul(Pow(10, 7), Sym("centiv")));
        c.put(UnitsElectricPotential.PETAVOLT, Mul(Rat(Int(1), Pow(10, 17)), Sym("centiv")));
        c.put(UnitsElectricPotential.PICOVOLT, Mul(Pow(10, 10), Sym("centiv")));
        c.put(UnitsElectricPotential.TERAVOLT, Mul(Rat(Int(1), Pow(10, 14)), Sym("centiv")));
        c.put(UnitsElectricPotential.VOLT, Mul(Rat(Int(1), Int(100)), Sym("centiv")));
        c.put(UnitsElectricPotential.YOCTOVOLT, Mul(Pow(10, 22), Sym("centiv")));
        c.put(UnitsElectricPotential.YOTTAVOLT, Mul(Rat(Int(1), Pow(10, 26)), Sym("centiv")));
        c.put(UnitsElectricPotential.ZEPTOVOLT, Mul(Pow(10, 19), Sym("centiv")));
        c.put(UnitsElectricPotential.ZETTAVOLT, Mul(Rat(Int(1), Pow(10, 23)), Sym("centiv")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsElectricPotential, Conversion> createMapDECAVOLT() {
        EnumMap<UnitsElectricPotential, Conversion> c =
            new EnumMap<UnitsElectricPotential, Conversion>(UnitsElectricPotential.class);
        c.put(UnitsElectricPotential.ATTOVOLT, Mul(Pow(10, 19), Sym("decav")));
        c.put(UnitsElectricPotential.CENTIVOLT, Mul(Int(1000), Sym("decav")));
        c.put(UnitsElectricPotential.DECIVOLT, Mul(Int(100), Sym("decav")));
        c.put(UnitsElectricPotential.EXAVOLT, Mul(Rat(Int(1), Pow(10, 17)), Sym("decav")));
        c.put(UnitsElectricPotential.FEMTOVOLT, Mul(Pow(10, 16), Sym("decav")));
        c.put(UnitsElectricPotential.GIGAVOLT, Mul(Rat(Int(1), Pow(10, 8)), Sym("decav")));
        c.put(UnitsElectricPotential.HECTOVOLT, Mul(Rat(Int(1), Int(10)), Sym("decav")));
        c.put(UnitsElectricPotential.KILOVOLT, Mul(Rat(Int(1), Int(100)), Sym("decav")));
        c.put(UnitsElectricPotential.MEGAVOLT, Mul(Rat(Int(1), Pow(10, 5)), Sym("decav")));
        c.put(UnitsElectricPotential.MICROVOLT, Mul(Pow(10, 7), Sym("decav")));
        c.put(UnitsElectricPotential.MILLIVOLT, Mul(Pow(10, 4), Sym("decav")));
        c.put(UnitsElectricPotential.NANOVOLT, Mul(Pow(10, 10), Sym("decav")));
        c.put(UnitsElectricPotential.PETAVOLT, Mul(Rat(Int(1), Pow(10, 14)), Sym("decav")));
        c.put(UnitsElectricPotential.PICOVOLT, Mul(Pow(10, 13), Sym("decav")));
        c.put(UnitsElectricPotential.TERAVOLT, Mul(Rat(Int(1), Pow(10, 11)), Sym("decav")));
        c.put(UnitsElectricPotential.VOLT, Mul(Int(10), Sym("decav")));
        c.put(UnitsElectricPotential.YOCTOVOLT, Mul(Pow(10, 25), Sym("decav")));
        c.put(UnitsElectricPotential.YOTTAVOLT, Mul(Rat(Int(1), Pow(10, 23)), Sym("decav")));
        c.put(UnitsElectricPotential.ZEPTOVOLT, Mul(Pow(10, 22), Sym("decav")));
        c.put(UnitsElectricPotential.ZETTAVOLT, Mul(Rat(Int(1), Pow(10, 20)), Sym("decav")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsElectricPotential, Conversion> createMapDECIVOLT() {
        EnumMap<UnitsElectricPotential, Conversion> c =
            new EnumMap<UnitsElectricPotential, Conversion>(UnitsElectricPotential.class);
        c.put(UnitsElectricPotential.ATTOVOLT, Mul(Pow(10, 17), Sym("deciv")));
        c.put(UnitsElectricPotential.CENTIVOLT, Mul(Int(10), Sym("deciv")));
        c.put(UnitsElectricPotential.DECAVOLT, Mul(Rat(Int(1), Int(100)), Sym("deciv")));
        c.put(UnitsElectricPotential.EXAVOLT, Mul(Rat(Int(1), Pow(10, 19)), Sym("deciv")));
        c.put(UnitsElectricPotential.FEMTOVOLT, Mul(Pow(10, 14), Sym("deciv")));
        c.put(UnitsElectricPotential.GIGAVOLT, Mul(Rat(Int(1), Pow(10, 10)), Sym("deciv")));
        c.put(UnitsElectricPotential.HECTOVOLT, Mul(Rat(Int(1), Int(1000)), Sym("deciv")));
        c.put(UnitsElectricPotential.KILOVOLT, Mul(Rat(Int(1), Pow(10, 4)), Sym("deciv")));
        c.put(UnitsElectricPotential.MEGAVOLT, Mul(Rat(Int(1), Pow(10, 7)), Sym("deciv")));
        c.put(UnitsElectricPotential.MICROVOLT, Mul(Pow(10, 5), Sym("deciv")));
        c.put(UnitsElectricPotential.MILLIVOLT, Mul(Int(100), Sym("deciv")));
        c.put(UnitsElectricPotential.NANOVOLT, Mul(Pow(10, 8), Sym("deciv")));
        c.put(UnitsElectricPotential.PETAVOLT, Mul(Rat(Int(1), Pow(10, 16)), Sym("deciv")));
        c.put(UnitsElectricPotential.PICOVOLT, Mul(Pow(10, 11), Sym("deciv")));
        c.put(UnitsElectricPotential.TERAVOLT, Mul(Rat(Int(1), Pow(10, 13)), Sym("deciv")));
        c.put(UnitsElectricPotential.VOLT, Mul(Rat(Int(1), Int(10)), Sym("deciv")));
        c.put(UnitsElectricPotential.YOCTOVOLT, Mul(Pow(10, 23), Sym("deciv")));
        c.put(UnitsElectricPotential.YOTTAVOLT, Mul(Rat(Int(1), Pow(10, 25)), Sym("deciv")));
        c.put(UnitsElectricPotential.ZEPTOVOLT, Mul(Pow(10, 20), Sym("deciv")));
        c.put(UnitsElectricPotential.ZETTAVOLT, Mul(Rat(Int(1), Pow(10, 22)), Sym("deciv")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsElectricPotential, Conversion> createMapEXAVOLT() {
        EnumMap<UnitsElectricPotential, Conversion> c =
            new EnumMap<UnitsElectricPotential, Conversion>(UnitsElectricPotential.class);
        c.put(UnitsElectricPotential.ATTOVOLT, Mul(Pow(10, 36), Sym("exav")));
        c.put(UnitsElectricPotential.CENTIVOLT, Mul(Pow(10, 20), Sym("exav")));
        c.put(UnitsElectricPotential.DECAVOLT, Mul(Pow(10, 17), Sym("exav")));
        c.put(UnitsElectricPotential.DECIVOLT, Mul(Pow(10, 19), Sym("exav")));
        c.put(UnitsElectricPotential.FEMTOVOLT, Mul(Pow(10, 33), Sym("exav")));
        c.put(UnitsElectricPotential.GIGAVOLT, Mul(Pow(10, 9), Sym("exav")));
        c.put(UnitsElectricPotential.HECTOVOLT, Mul(Pow(10, 16), Sym("exav")));
        c.put(UnitsElectricPotential.KILOVOLT, Mul(Pow(10, 15), Sym("exav")));
        c.put(UnitsElectricPotential.MEGAVOLT, Mul(Pow(10, 12), Sym("exav")));
        c.put(UnitsElectricPotential.MICROVOLT, Mul(Pow(10, 24), Sym("exav")));
        c.put(UnitsElectricPotential.MILLIVOLT, Mul(Pow(10, 21), Sym("exav")));
        c.put(UnitsElectricPotential.NANOVOLT, Mul(Pow(10, 27), Sym("exav")));
        c.put(UnitsElectricPotential.PETAVOLT, Mul(Int(1000), Sym("exav")));
        c.put(UnitsElectricPotential.PICOVOLT, Mul(Pow(10, 30), Sym("exav")));
        c.put(UnitsElectricPotential.TERAVOLT, Mul(Pow(10, 6), Sym("exav")));
        c.put(UnitsElectricPotential.VOLT, Mul(Pow(10, 18), Sym("exav")));
        c.put(UnitsElectricPotential.YOCTOVOLT, Mul(Pow(10, 42), Sym("exav")));
        c.put(UnitsElectricPotential.YOTTAVOLT, Mul(Rat(Int(1), Pow(10, 6)), Sym("exav")));
        c.put(UnitsElectricPotential.ZEPTOVOLT, Mul(Pow(10, 39), Sym("exav")));
        c.put(UnitsElectricPotential.ZETTAVOLT, Mul(Rat(Int(1), Int(1000)), Sym("exav")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsElectricPotential, Conversion> createMapFEMTOVOLT() {
        EnumMap<UnitsElectricPotential, Conversion> c =
            new EnumMap<UnitsElectricPotential, Conversion>(UnitsElectricPotential.class);
        c.put(UnitsElectricPotential.ATTOVOLT, Mul(Int(1000), Sym("femtov")));
        c.put(UnitsElectricPotential.CENTIVOLT, Mul(Rat(Int(1), Pow(10, 13)), Sym("femtov")));
        c.put(UnitsElectricPotential.DECAVOLT, Mul(Rat(Int(1), Pow(10, 16)), Sym("femtov")));
        c.put(UnitsElectricPotential.DECIVOLT, Mul(Rat(Int(1), Pow(10, 14)), Sym("femtov")));
        c.put(UnitsElectricPotential.EXAVOLT, Mul(Rat(Int(1), Pow(10, 33)), Sym("femtov")));
        c.put(UnitsElectricPotential.GIGAVOLT, Mul(Rat(Int(1), Pow(10, 24)), Sym("femtov")));
        c.put(UnitsElectricPotential.HECTOVOLT, Mul(Rat(Int(1), Pow(10, 17)), Sym("femtov")));
        c.put(UnitsElectricPotential.KILOVOLT, Mul(Rat(Int(1), Pow(10, 18)), Sym("femtov")));
        c.put(UnitsElectricPotential.MEGAVOLT, Mul(Rat(Int(1), Pow(10, 21)), Sym("femtov")));
        c.put(UnitsElectricPotential.MICROVOLT, Mul(Rat(Int(1), Pow(10, 9)), Sym("femtov")));
        c.put(UnitsElectricPotential.MILLIVOLT, Mul(Rat(Int(1), Pow(10, 12)), Sym("femtov")));
        c.put(UnitsElectricPotential.NANOVOLT, Mul(Rat(Int(1), Pow(10, 6)), Sym("femtov")));
        c.put(UnitsElectricPotential.PETAVOLT, Mul(Rat(Int(1), Pow(10, 30)), Sym("femtov")));
        c.put(UnitsElectricPotential.PICOVOLT, Mul(Rat(Int(1), Int(1000)), Sym("femtov")));
        c.put(UnitsElectricPotential.TERAVOLT, Mul(Rat(Int(1), Pow(10, 27)), Sym("femtov")));
        c.put(UnitsElectricPotential.VOLT, Mul(Rat(Int(1), Pow(10, 15)), Sym("femtov")));
        c.put(UnitsElectricPotential.YOCTOVOLT, Mul(Pow(10, 9), Sym("femtov")));
        c.put(UnitsElectricPotential.YOTTAVOLT, Mul(Rat(Int(1), Pow(10, 39)), Sym("femtov")));
        c.put(UnitsElectricPotential.ZEPTOVOLT, Mul(Pow(10, 6), Sym("femtov")));
        c.put(UnitsElectricPotential.ZETTAVOLT, Mul(Rat(Int(1), Pow(10, 36)), Sym("femtov")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsElectricPotential, Conversion> createMapGIGAVOLT() {
        EnumMap<UnitsElectricPotential, Conversion> c =
            new EnumMap<UnitsElectricPotential, Conversion>(UnitsElectricPotential.class);
        c.put(UnitsElectricPotential.ATTOVOLT, Mul(Pow(10, 27), Sym("gigav")));
        c.put(UnitsElectricPotential.CENTIVOLT, Mul(Pow(10, 11), Sym("gigav")));
        c.put(UnitsElectricPotential.DECAVOLT, Mul(Pow(10, 8), Sym("gigav")));
        c.put(UnitsElectricPotential.DECIVOLT, Mul(Pow(10, 10), Sym("gigav")));
        c.put(UnitsElectricPotential.EXAVOLT, Mul(Rat(Int(1), Pow(10, 9)), Sym("gigav")));
        c.put(UnitsElectricPotential.FEMTOVOLT, Mul(Pow(10, 24), Sym("gigav")));
        c.put(UnitsElectricPotential.HECTOVOLT, Mul(Pow(10, 7), Sym("gigav")));
        c.put(UnitsElectricPotential.KILOVOLT, Mul(Pow(10, 6), Sym("gigav")));
        c.put(UnitsElectricPotential.MEGAVOLT, Mul(Int(1000), Sym("gigav")));
        c.put(UnitsElectricPotential.MICROVOLT, Mul(Pow(10, 15), Sym("gigav")));
        c.put(UnitsElectricPotential.MILLIVOLT, Mul(Pow(10, 12), Sym("gigav")));
        c.put(UnitsElectricPotential.NANOVOLT, Mul(Pow(10, 18), Sym("gigav")));
        c.put(UnitsElectricPotential.PETAVOLT, Mul(Rat(Int(1), Pow(10, 6)), Sym("gigav")));
        c.put(UnitsElectricPotential.PICOVOLT, Mul(Pow(10, 21), Sym("gigav")));
        c.put(UnitsElectricPotential.TERAVOLT, Mul(Rat(Int(1), Int(1000)), Sym("gigav")));
        c.put(UnitsElectricPotential.VOLT, Mul(Pow(10, 9), Sym("gigav")));
        c.put(UnitsElectricPotential.YOCTOVOLT, Mul(Pow(10, 33), Sym("gigav")));
        c.put(UnitsElectricPotential.YOTTAVOLT, Mul(Rat(Int(1), Pow(10, 15)), Sym("gigav")));
        c.put(UnitsElectricPotential.ZEPTOVOLT, Mul(Pow(10, 30), Sym("gigav")));
        c.put(UnitsElectricPotential.ZETTAVOLT, Mul(Rat(Int(1), Pow(10, 12)), Sym("gigav")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsElectricPotential, Conversion> createMapHECTOVOLT() {
        EnumMap<UnitsElectricPotential, Conversion> c =
            new EnumMap<UnitsElectricPotential, Conversion>(UnitsElectricPotential.class);
        c.put(UnitsElectricPotential.ATTOVOLT, Mul(Pow(10, 20), Sym("hectov")));
        c.put(UnitsElectricPotential.CENTIVOLT, Mul(Pow(10, 4), Sym("hectov")));
        c.put(UnitsElectricPotential.DECAVOLT, Mul(Int(10), Sym("hectov")));
        c.put(UnitsElectricPotential.DECIVOLT, Mul(Int(1000), Sym("hectov")));
        c.put(UnitsElectricPotential.EXAVOLT, Mul(Rat(Int(1), Pow(10, 16)), Sym("hectov")));
        c.put(UnitsElectricPotential.FEMTOVOLT, Mul(Pow(10, 17), Sym("hectov")));
        c.put(UnitsElectricPotential.GIGAVOLT, Mul(Rat(Int(1), Pow(10, 7)), Sym("hectov")));
        c.put(UnitsElectricPotential.KILOVOLT, Mul(Rat(Int(1), Int(10)), Sym("hectov")));
        c.put(UnitsElectricPotential.MEGAVOLT, Mul(Rat(Int(1), Pow(10, 4)), Sym("hectov")));
        c.put(UnitsElectricPotential.MICROVOLT, Mul(Pow(10, 8), Sym("hectov")));
        c.put(UnitsElectricPotential.MILLIVOLT, Mul(Pow(10, 5), Sym("hectov")));
        c.put(UnitsElectricPotential.NANOVOLT, Mul(Pow(10, 11), Sym("hectov")));
        c.put(UnitsElectricPotential.PETAVOLT, Mul(Rat(Int(1), Pow(10, 13)), Sym("hectov")));
        c.put(UnitsElectricPotential.PICOVOLT, Mul(Pow(10, 14), Sym("hectov")));
        c.put(UnitsElectricPotential.TERAVOLT, Mul(Rat(Int(1), Pow(10, 10)), Sym("hectov")));
        c.put(UnitsElectricPotential.VOLT, Mul(Int(100), Sym("hectov")));
        c.put(UnitsElectricPotential.YOCTOVOLT, Mul(Pow(10, 26), Sym("hectov")));
        c.put(UnitsElectricPotential.YOTTAVOLT, Mul(Rat(Int(1), Pow(10, 22)), Sym("hectov")));
        c.put(UnitsElectricPotential.ZEPTOVOLT, Mul(Pow(10, 23), Sym("hectov")));
        c.put(UnitsElectricPotential.ZETTAVOLT, Mul(Rat(Int(1), Pow(10, 19)), Sym("hectov")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsElectricPotential, Conversion> createMapKILOVOLT() {
        EnumMap<UnitsElectricPotential, Conversion> c =
            new EnumMap<UnitsElectricPotential, Conversion>(UnitsElectricPotential.class);
        c.put(UnitsElectricPotential.ATTOVOLT, Mul(Pow(10, 21), Sym("kilov")));
        c.put(UnitsElectricPotential.CENTIVOLT, Mul(Pow(10, 5), Sym("kilov")));
        c.put(UnitsElectricPotential.DECAVOLT, Mul(Int(100), Sym("kilov")));
        c.put(UnitsElectricPotential.DECIVOLT, Mul(Pow(10, 4), Sym("kilov")));
        c.put(UnitsElectricPotential.EXAVOLT, Mul(Rat(Int(1), Pow(10, 15)), Sym("kilov")));
        c.put(UnitsElectricPotential.FEMTOVOLT, Mul(Pow(10, 18), Sym("kilov")));
        c.put(UnitsElectricPotential.GIGAVOLT, Mul(Rat(Int(1), Pow(10, 6)), Sym("kilov")));
        c.put(UnitsElectricPotential.HECTOVOLT, Mul(Int(10), Sym("kilov")));
        c.put(UnitsElectricPotential.MEGAVOLT, Mul(Rat(Int(1), Int(1000)), Sym("kilov")));
        c.put(UnitsElectricPotential.MICROVOLT, Mul(Pow(10, 9), Sym("kilov")));
        c.put(UnitsElectricPotential.MILLIVOLT, Mul(Pow(10, 6), Sym("kilov")));
        c.put(UnitsElectricPotential.NANOVOLT, Mul(Pow(10, 12), Sym("kilov")));
        c.put(UnitsElectricPotential.PETAVOLT, Mul(Rat(Int(1), Pow(10, 12)), Sym("kilov")));
        c.put(UnitsElectricPotential.PICOVOLT, Mul(Pow(10, 15), Sym("kilov")));
        c.put(UnitsElectricPotential.TERAVOLT, Mul(Rat(Int(1), Pow(10, 9)), Sym("kilov")));
        c.put(UnitsElectricPotential.VOLT, Mul(Int(1000), Sym("kilov")));
        c.put(UnitsElectricPotential.YOCTOVOLT, Mul(Pow(10, 27), Sym("kilov")));
        c.put(UnitsElectricPotential.YOTTAVOLT, Mul(Rat(Int(1), Pow(10, 21)), Sym("kilov")));
        c.put(UnitsElectricPotential.ZEPTOVOLT, Mul(Pow(10, 24), Sym("kilov")));
        c.put(UnitsElectricPotential.ZETTAVOLT, Mul(Rat(Int(1), Pow(10, 18)), Sym("kilov")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsElectricPotential, Conversion> createMapMEGAVOLT() {
        EnumMap<UnitsElectricPotential, Conversion> c =
            new EnumMap<UnitsElectricPotential, Conversion>(UnitsElectricPotential.class);
        c.put(UnitsElectricPotential.ATTOVOLT, Mul(Pow(10, 24), Sym("megav")));
        c.put(UnitsElectricPotential.CENTIVOLT, Mul(Pow(10, 8), Sym("megav")));
        c.put(UnitsElectricPotential.DECAVOLT, Mul(Pow(10, 5), Sym("megav")));
        c.put(UnitsElectricPotential.DECIVOLT, Mul(Pow(10, 7), Sym("megav")));
        c.put(UnitsElectricPotential.EXAVOLT, Mul(Rat(Int(1), Pow(10, 12)), Sym("megav")));
        c.put(UnitsElectricPotential.FEMTOVOLT, Mul(Pow(10, 21), Sym("megav")));
        c.put(UnitsElectricPotential.GIGAVOLT, Mul(Rat(Int(1), Int(1000)), Sym("megav")));
        c.put(UnitsElectricPotential.HECTOVOLT, Mul(Pow(10, 4), Sym("megav")));
        c.put(UnitsElectricPotential.KILOVOLT, Mul(Int(1000), Sym("megav")));
        c.put(UnitsElectricPotential.MICROVOLT, Mul(Pow(10, 12), Sym("megav")));
        c.put(UnitsElectricPotential.MILLIVOLT, Mul(Pow(10, 9), Sym("megav")));
        c.put(UnitsElectricPotential.NANOVOLT, Mul(Pow(10, 15), Sym("megav")));
        c.put(UnitsElectricPotential.PETAVOLT, Mul(Rat(Int(1), Pow(10, 9)), Sym("megav")));
        c.put(UnitsElectricPotential.PICOVOLT, Mul(Pow(10, 18), Sym("megav")));
        c.put(UnitsElectricPotential.TERAVOLT, Mul(Rat(Int(1), Pow(10, 6)), Sym("megav")));
        c.put(UnitsElectricPotential.VOLT, Mul(Pow(10, 6), Sym("megav")));
        c.put(UnitsElectricPotential.YOCTOVOLT, Mul(Pow(10, 30), Sym("megav")));
        c.put(UnitsElectricPotential.YOTTAVOLT, Mul(Rat(Int(1), Pow(10, 18)), Sym("megav")));
        c.put(UnitsElectricPotential.ZEPTOVOLT, Mul(Pow(10, 27), Sym("megav")));
        c.put(UnitsElectricPotential.ZETTAVOLT, Mul(Rat(Int(1), Pow(10, 15)), Sym("megav")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsElectricPotential, Conversion> createMapMICROVOLT() {
        EnumMap<UnitsElectricPotential, Conversion> c =
            new EnumMap<UnitsElectricPotential, Conversion>(UnitsElectricPotential.class);
        c.put(UnitsElectricPotential.ATTOVOLT, Mul(Pow(10, 12), Sym("microv")));
        c.put(UnitsElectricPotential.CENTIVOLT, Mul(Rat(Int(1), Pow(10, 4)), Sym("microv")));
        c.put(UnitsElectricPotential.DECAVOLT, Mul(Rat(Int(1), Pow(10, 7)), Sym("microv")));
        c.put(UnitsElectricPotential.DECIVOLT, Mul(Rat(Int(1), Pow(10, 5)), Sym("microv")));
        c.put(UnitsElectricPotential.EXAVOLT, Mul(Rat(Int(1), Pow(10, 24)), Sym("microv")));
        c.put(UnitsElectricPotential.FEMTOVOLT, Mul(Pow(10, 9), Sym("microv")));
        c.put(UnitsElectricPotential.GIGAVOLT, Mul(Rat(Int(1), Pow(10, 15)), Sym("microv")));
        c.put(UnitsElectricPotential.HECTOVOLT, Mul(Rat(Int(1), Pow(10, 8)), Sym("microv")));
        c.put(UnitsElectricPotential.KILOVOLT, Mul(Rat(Int(1), Pow(10, 9)), Sym("microv")));
        c.put(UnitsElectricPotential.MEGAVOLT, Mul(Rat(Int(1), Pow(10, 12)), Sym("microv")));
        c.put(UnitsElectricPotential.MILLIVOLT, Mul(Rat(Int(1), Int(1000)), Sym("microv")));
        c.put(UnitsElectricPotential.NANOVOLT, Mul(Int(1000), Sym("microv")));
        c.put(UnitsElectricPotential.PETAVOLT, Mul(Rat(Int(1), Pow(10, 21)), Sym("microv")));
        c.put(UnitsElectricPotential.PICOVOLT, Mul(Pow(10, 6), Sym("microv")));
        c.put(UnitsElectricPotential.TERAVOLT, Mul(Rat(Int(1), Pow(10, 18)), Sym("microv")));
        c.put(UnitsElectricPotential.VOLT, Mul(Rat(Int(1), Pow(10, 6)), Sym("microv")));
        c.put(UnitsElectricPotential.YOCTOVOLT, Mul(Pow(10, 18), Sym("microv")));
        c.put(UnitsElectricPotential.YOTTAVOLT, Mul(Rat(Int(1), Pow(10, 30)), Sym("microv")));
        c.put(UnitsElectricPotential.ZEPTOVOLT, Mul(Pow(10, 15), Sym("microv")));
        c.put(UnitsElectricPotential.ZETTAVOLT, Mul(Rat(Int(1), Pow(10, 27)), Sym("microv")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsElectricPotential, Conversion> createMapMILLIVOLT() {
        EnumMap<UnitsElectricPotential, Conversion> c =
            new EnumMap<UnitsElectricPotential, Conversion>(UnitsElectricPotential.class);
        c.put(UnitsElectricPotential.ATTOVOLT, Mul(Pow(10, 15), Sym("milliv")));
        c.put(UnitsElectricPotential.CENTIVOLT, Mul(Rat(Int(1), Int(10)), Sym("milliv")));
        c.put(UnitsElectricPotential.DECAVOLT, Mul(Rat(Int(1), Pow(10, 4)), Sym("milliv")));
        c.put(UnitsElectricPotential.DECIVOLT, Mul(Rat(Int(1), Int(100)), Sym("milliv")));
        c.put(UnitsElectricPotential.EXAVOLT, Mul(Rat(Int(1), Pow(10, 21)), Sym("milliv")));
        c.put(UnitsElectricPotential.FEMTOVOLT, Mul(Pow(10, 12), Sym("milliv")));
        c.put(UnitsElectricPotential.GIGAVOLT, Mul(Rat(Int(1), Pow(10, 12)), Sym("milliv")));
        c.put(UnitsElectricPotential.HECTOVOLT, Mul(Rat(Int(1), Pow(10, 5)), Sym("milliv")));
        c.put(UnitsElectricPotential.KILOVOLT, Mul(Rat(Int(1), Pow(10, 6)), Sym("milliv")));
        c.put(UnitsElectricPotential.MEGAVOLT, Mul(Rat(Int(1), Pow(10, 9)), Sym("milliv")));
        c.put(UnitsElectricPotential.MICROVOLT, Mul(Int(1000), Sym("milliv")));
        c.put(UnitsElectricPotential.NANOVOLT, Mul(Pow(10, 6), Sym("milliv")));
        c.put(UnitsElectricPotential.PETAVOLT, Mul(Rat(Int(1), Pow(10, 18)), Sym("milliv")));
        c.put(UnitsElectricPotential.PICOVOLT, Mul(Pow(10, 9), Sym("milliv")));
        c.put(UnitsElectricPotential.TERAVOLT, Mul(Rat(Int(1), Pow(10, 15)), Sym("milliv")));
        c.put(UnitsElectricPotential.VOLT, Mul(Rat(Int(1), Int(1000)), Sym("milliv")));
        c.put(UnitsElectricPotential.YOCTOVOLT, Mul(Pow(10, 21), Sym("milliv")));
        c.put(UnitsElectricPotential.YOTTAVOLT, Mul(Rat(Int(1), Pow(10, 27)), Sym("milliv")));
        c.put(UnitsElectricPotential.ZEPTOVOLT, Mul(Pow(10, 18), Sym("milliv")));
        c.put(UnitsElectricPotential.ZETTAVOLT, Mul(Rat(Int(1), Pow(10, 24)), Sym("milliv")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsElectricPotential, Conversion> createMapNANOVOLT() {
        EnumMap<UnitsElectricPotential, Conversion> c =
            new EnumMap<UnitsElectricPotential, Conversion>(UnitsElectricPotential.class);
        c.put(UnitsElectricPotential.ATTOVOLT, Mul(Pow(10, 9), Sym("nanov")));
        c.put(UnitsElectricPotential.CENTIVOLT, Mul(Rat(Int(1), Pow(10, 7)), Sym("nanov")));
        c.put(UnitsElectricPotential.DECAVOLT, Mul(Rat(Int(1), Pow(10, 10)), Sym("nanov")));
        c.put(UnitsElectricPotential.DECIVOLT, Mul(Rat(Int(1), Pow(10, 8)), Sym("nanov")));
        c.put(UnitsElectricPotential.EXAVOLT, Mul(Rat(Int(1), Pow(10, 27)), Sym("nanov")));
        c.put(UnitsElectricPotential.FEMTOVOLT, Mul(Pow(10, 6), Sym("nanov")));
        c.put(UnitsElectricPotential.GIGAVOLT, Mul(Rat(Int(1), Pow(10, 18)), Sym("nanov")));
        c.put(UnitsElectricPotential.HECTOVOLT, Mul(Rat(Int(1), Pow(10, 11)), Sym("nanov")));
        c.put(UnitsElectricPotential.KILOVOLT, Mul(Rat(Int(1), Pow(10, 12)), Sym("nanov")));
        c.put(UnitsElectricPotential.MEGAVOLT, Mul(Rat(Int(1), Pow(10, 15)), Sym("nanov")));
        c.put(UnitsElectricPotential.MICROVOLT, Mul(Rat(Int(1), Int(1000)), Sym("nanov")));
        c.put(UnitsElectricPotential.MILLIVOLT, Mul(Rat(Int(1), Pow(10, 6)), Sym("nanov")));
        c.put(UnitsElectricPotential.PETAVOLT, Mul(Rat(Int(1), Pow(10, 24)), Sym("nanov")));
        c.put(UnitsElectricPotential.PICOVOLT, Mul(Int(1000), Sym("nanov")));
        c.put(UnitsElectricPotential.TERAVOLT, Mul(Rat(Int(1), Pow(10, 21)), Sym("nanov")));
        c.put(UnitsElectricPotential.VOLT, Mul(Rat(Int(1), Pow(10, 9)), Sym("nanov")));
        c.put(UnitsElectricPotential.YOCTOVOLT, Mul(Pow(10, 15), Sym("nanov")));
        c.put(UnitsElectricPotential.YOTTAVOLT, Mul(Rat(Int(1), Pow(10, 33)), Sym("nanov")));
        c.put(UnitsElectricPotential.ZEPTOVOLT, Mul(Pow(10, 12), Sym("nanov")));
        c.put(UnitsElectricPotential.ZETTAVOLT, Mul(Rat(Int(1), Pow(10, 30)), Sym("nanov")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsElectricPotential, Conversion> createMapPETAVOLT() {
        EnumMap<UnitsElectricPotential, Conversion> c =
            new EnumMap<UnitsElectricPotential, Conversion>(UnitsElectricPotential.class);
        c.put(UnitsElectricPotential.ATTOVOLT, Mul(Pow(10, 33), Sym("petav")));
        c.put(UnitsElectricPotential.CENTIVOLT, Mul(Pow(10, 17), Sym("petav")));
        c.put(UnitsElectricPotential.DECAVOLT, Mul(Pow(10, 14), Sym("petav")));
        c.put(UnitsElectricPotential.DECIVOLT, Mul(Pow(10, 16), Sym("petav")));
        c.put(UnitsElectricPotential.EXAVOLT, Mul(Rat(Int(1), Int(1000)), Sym("petav")));
        c.put(UnitsElectricPotential.FEMTOVOLT, Mul(Pow(10, 30), Sym("petav")));
        c.put(UnitsElectricPotential.GIGAVOLT, Mul(Pow(10, 6), Sym("petav")));
        c.put(UnitsElectricPotential.HECTOVOLT, Mul(Pow(10, 13), Sym("petav")));
        c.put(UnitsElectricPotential.KILOVOLT, Mul(Pow(10, 12), Sym("petav")));
        c.put(UnitsElectricPotential.MEGAVOLT, Mul(Pow(10, 9), Sym("petav")));
        c.put(UnitsElectricPotential.MICROVOLT, Mul(Pow(10, 21), Sym("petav")));
        c.put(UnitsElectricPotential.MILLIVOLT, Mul(Pow(10, 18), Sym("petav")));
        c.put(UnitsElectricPotential.NANOVOLT, Mul(Pow(10, 24), Sym("petav")));
        c.put(UnitsElectricPotential.PICOVOLT, Mul(Pow(10, 27), Sym("petav")));
        c.put(UnitsElectricPotential.TERAVOLT, Mul(Int(1000), Sym("petav")));
        c.put(UnitsElectricPotential.VOLT, Mul(Pow(10, 15), Sym("petav")));
        c.put(UnitsElectricPotential.YOCTOVOLT, Mul(Pow(10, 39), Sym("petav")));
        c.put(UnitsElectricPotential.YOTTAVOLT, Mul(Rat(Int(1), Pow(10, 9)), Sym("petav")));
        c.put(UnitsElectricPotential.ZEPTOVOLT, Mul(Pow(10, 36), Sym("petav")));
        c.put(UnitsElectricPotential.ZETTAVOLT, Mul(Rat(Int(1), Pow(10, 6)), Sym("petav")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsElectricPotential, Conversion> createMapPICOVOLT() {
        EnumMap<UnitsElectricPotential, Conversion> c =
            new EnumMap<UnitsElectricPotential, Conversion>(UnitsElectricPotential.class);
        c.put(UnitsElectricPotential.ATTOVOLT, Mul(Pow(10, 6), Sym("picov")));
        c.put(UnitsElectricPotential.CENTIVOLT, Mul(Rat(Int(1), Pow(10, 10)), Sym("picov")));
        c.put(UnitsElectricPotential.DECAVOLT, Mul(Rat(Int(1), Pow(10, 13)), Sym("picov")));
        c.put(UnitsElectricPotential.DECIVOLT, Mul(Rat(Int(1), Pow(10, 11)), Sym("picov")));
        c.put(UnitsElectricPotential.EXAVOLT, Mul(Rat(Int(1), Pow(10, 30)), Sym("picov")));
        c.put(UnitsElectricPotential.FEMTOVOLT, Mul(Int(1000), Sym("picov")));
        c.put(UnitsElectricPotential.GIGAVOLT, Mul(Rat(Int(1), Pow(10, 21)), Sym("picov")));
        c.put(UnitsElectricPotential.HECTOVOLT, Mul(Rat(Int(1), Pow(10, 14)), Sym("picov")));
        c.put(UnitsElectricPotential.KILOVOLT, Mul(Rat(Int(1), Pow(10, 15)), Sym("picov")));
        c.put(UnitsElectricPotential.MEGAVOLT, Mul(Rat(Int(1), Pow(10, 18)), Sym("picov")));
        c.put(UnitsElectricPotential.MICROVOLT, Mul(Rat(Int(1), Pow(10, 6)), Sym("picov")));
        c.put(UnitsElectricPotential.MILLIVOLT, Mul(Rat(Int(1), Pow(10, 9)), Sym("picov")));
        c.put(UnitsElectricPotential.NANOVOLT, Mul(Rat(Int(1), Int(1000)), Sym("picov")));
        c.put(UnitsElectricPotential.PETAVOLT, Mul(Rat(Int(1), Pow(10, 27)), Sym("picov")));
        c.put(UnitsElectricPotential.TERAVOLT, Mul(Rat(Int(1), Pow(10, 24)), Sym("picov")));
        c.put(UnitsElectricPotential.VOLT, Mul(Rat(Int(1), Pow(10, 12)), Sym("picov")));
        c.put(UnitsElectricPotential.YOCTOVOLT, Mul(Pow(10, 12), Sym("picov")));
        c.put(UnitsElectricPotential.YOTTAVOLT, Mul(Rat(Int(1), Pow(10, 36)), Sym("picov")));
        c.put(UnitsElectricPotential.ZEPTOVOLT, Mul(Pow(10, 9), Sym("picov")));
        c.put(UnitsElectricPotential.ZETTAVOLT, Mul(Rat(Int(1), Pow(10, 33)), Sym("picov")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsElectricPotential, Conversion> createMapTERAVOLT() {
        EnumMap<UnitsElectricPotential, Conversion> c =
            new EnumMap<UnitsElectricPotential, Conversion>(UnitsElectricPotential.class);
        c.put(UnitsElectricPotential.ATTOVOLT, Mul(Pow(10, 30), Sym("terav")));
        c.put(UnitsElectricPotential.CENTIVOLT, Mul(Pow(10, 14), Sym("terav")));
        c.put(UnitsElectricPotential.DECAVOLT, Mul(Pow(10, 11), Sym("terav")));
        c.put(UnitsElectricPotential.DECIVOLT, Mul(Pow(10, 13), Sym("terav")));
        c.put(UnitsElectricPotential.EXAVOLT, Mul(Rat(Int(1), Pow(10, 6)), Sym("terav")));
        c.put(UnitsElectricPotential.FEMTOVOLT, Mul(Pow(10, 27), Sym("terav")));
        c.put(UnitsElectricPotential.GIGAVOLT, Mul(Int(1000), Sym("terav")));
        c.put(UnitsElectricPotential.HECTOVOLT, Mul(Pow(10, 10), Sym("terav")));
        c.put(UnitsElectricPotential.KILOVOLT, Mul(Pow(10, 9), Sym("terav")));
        c.put(UnitsElectricPotential.MEGAVOLT, Mul(Pow(10, 6), Sym("terav")));
        c.put(UnitsElectricPotential.MICROVOLT, Mul(Pow(10, 18), Sym("terav")));
        c.put(UnitsElectricPotential.MILLIVOLT, Mul(Pow(10, 15), Sym("terav")));
        c.put(UnitsElectricPotential.NANOVOLT, Mul(Pow(10, 21), Sym("terav")));
        c.put(UnitsElectricPotential.PETAVOLT, Mul(Rat(Int(1), Int(1000)), Sym("terav")));
        c.put(UnitsElectricPotential.PICOVOLT, Mul(Pow(10, 24), Sym("terav")));
        c.put(UnitsElectricPotential.VOLT, Mul(Pow(10, 12), Sym("terav")));
        c.put(UnitsElectricPotential.YOCTOVOLT, Mul(Pow(10, 36), Sym("terav")));
        c.put(UnitsElectricPotential.YOTTAVOLT, Mul(Rat(Int(1), Pow(10, 12)), Sym("terav")));
        c.put(UnitsElectricPotential.ZEPTOVOLT, Mul(Pow(10, 33), Sym("terav")));
        c.put(UnitsElectricPotential.ZETTAVOLT, Mul(Rat(Int(1), Pow(10, 9)), Sym("terav")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsElectricPotential, Conversion> createMapVOLT() {
        EnumMap<UnitsElectricPotential, Conversion> c =
            new EnumMap<UnitsElectricPotential, Conversion>(UnitsElectricPotential.class);
        c.put(UnitsElectricPotential.ATTOVOLT, Mul(Pow(10, 18), Sym("v")));
        c.put(UnitsElectricPotential.CENTIVOLT, Mul(Int(100), Sym("v")));
        c.put(UnitsElectricPotential.DECAVOLT, Mul(Rat(Int(1), Int(10)), Sym("v")));
        c.put(UnitsElectricPotential.DECIVOLT, Mul(Int(10), Sym("v")));
        c.put(UnitsElectricPotential.EXAVOLT, Mul(Rat(Int(1), Pow(10, 18)), Sym("v")));
        c.put(UnitsElectricPotential.FEMTOVOLT, Mul(Pow(10, 15), Sym("v")));
        c.put(UnitsElectricPotential.GIGAVOLT, Mul(Rat(Int(1), Pow(10, 9)), Sym("v")));
        c.put(UnitsElectricPotential.HECTOVOLT, Mul(Rat(Int(1), Int(100)), Sym("v")));
        c.put(UnitsElectricPotential.KILOVOLT, Mul(Rat(Int(1), Int(1000)), Sym("v")));
        c.put(UnitsElectricPotential.MEGAVOLT, Mul(Rat(Int(1), Pow(10, 6)), Sym("v")));
        c.put(UnitsElectricPotential.MICROVOLT, Mul(Pow(10, 6), Sym("v")));
        c.put(UnitsElectricPotential.MILLIVOLT, Mul(Int(1000), Sym("v")));
        c.put(UnitsElectricPotential.NANOVOLT, Mul(Pow(10, 9), Sym("v")));
        c.put(UnitsElectricPotential.PETAVOLT, Mul(Rat(Int(1), Pow(10, 15)), Sym("v")));
        c.put(UnitsElectricPotential.PICOVOLT, Mul(Pow(10, 12), Sym("v")));
        c.put(UnitsElectricPotential.TERAVOLT, Mul(Rat(Int(1), Pow(10, 12)), Sym("v")));
        c.put(UnitsElectricPotential.YOCTOVOLT, Mul(Pow(10, 24), Sym("v")));
        c.put(UnitsElectricPotential.YOTTAVOLT, Mul(Rat(Int(1), Pow(10, 24)), Sym("v")));
        c.put(UnitsElectricPotential.ZEPTOVOLT, Mul(Pow(10, 21), Sym("v")));
        c.put(UnitsElectricPotential.ZETTAVOLT, Mul(Rat(Int(1), Pow(10, 21)), Sym("v")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsElectricPotential, Conversion> createMapYOCTOVOLT() {
        EnumMap<UnitsElectricPotential, Conversion> c =
            new EnumMap<UnitsElectricPotential, Conversion>(UnitsElectricPotential.class);
        c.put(UnitsElectricPotential.ATTOVOLT, Mul(Rat(Int(1), Pow(10, 6)), Sym("yoctov")));
        c.put(UnitsElectricPotential.CENTIVOLT, Mul(Rat(Int(1), Pow(10, 22)), Sym("yoctov")));
        c.put(UnitsElectricPotential.DECAVOLT, Mul(Rat(Int(1), Pow(10, 25)), Sym("yoctov")));
        c.put(UnitsElectricPotential.DECIVOLT, Mul(Rat(Int(1), Pow(10, 23)), Sym("yoctov")));
        c.put(UnitsElectricPotential.EXAVOLT, Mul(Rat(Int(1), Pow(10, 42)), Sym("yoctov")));
        c.put(UnitsElectricPotential.FEMTOVOLT, Mul(Rat(Int(1), Pow(10, 9)), Sym("yoctov")));
        c.put(UnitsElectricPotential.GIGAVOLT, Mul(Rat(Int(1), Pow(10, 33)), Sym("yoctov")));
        c.put(UnitsElectricPotential.HECTOVOLT, Mul(Rat(Int(1), Pow(10, 26)), Sym("yoctov")));
        c.put(UnitsElectricPotential.KILOVOLT, Mul(Rat(Int(1), Pow(10, 27)), Sym("yoctov")));
        c.put(UnitsElectricPotential.MEGAVOLT, Mul(Rat(Int(1), Pow(10, 30)), Sym("yoctov")));
        c.put(UnitsElectricPotential.MICROVOLT, Mul(Rat(Int(1), Pow(10, 18)), Sym("yoctov")));
        c.put(UnitsElectricPotential.MILLIVOLT, Mul(Rat(Int(1), Pow(10, 21)), Sym("yoctov")));
        c.put(UnitsElectricPotential.NANOVOLT, Mul(Rat(Int(1), Pow(10, 15)), Sym("yoctov")));
        c.put(UnitsElectricPotential.PETAVOLT, Mul(Rat(Int(1), Pow(10, 39)), Sym("yoctov")));
        c.put(UnitsElectricPotential.PICOVOLT, Mul(Rat(Int(1), Pow(10, 12)), Sym("yoctov")));
        c.put(UnitsElectricPotential.TERAVOLT, Mul(Rat(Int(1), Pow(10, 36)), Sym("yoctov")));
        c.put(UnitsElectricPotential.VOLT, Mul(Rat(Int(1), Pow(10, 24)), Sym("yoctov")));
        c.put(UnitsElectricPotential.YOTTAVOLT, Mul(Rat(Int(1), Pow(10, 48)), Sym("yoctov")));
        c.put(UnitsElectricPotential.ZEPTOVOLT, Mul(Rat(Int(1), Int(1000)), Sym("yoctov")));
        c.put(UnitsElectricPotential.ZETTAVOLT, Mul(Rat(Int(1), Pow(10, 45)), Sym("yoctov")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsElectricPotential, Conversion> createMapYOTTAVOLT() {
        EnumMap<UnitsElectricPotential, Conversion> c =
            new EnumMap<UnitsElectricPotential, Conversion>(UnitsElectricPotential.class);
        c.put(UnitsElectricPotential.ATTOVOLT, Mul(Pow(10, 42), Sym("yottav")));
        c.put(UnitsElectricPotential.CENTIVOLT, Mul(Pow(10, 26), Sym("yottav")));
        c.put(UnitsElectricPotential.DECAVOLT, Mul(Pow(10, 23), Sym("yottav")));
        c.put(UnitsElectricPotential.DECIVOLT, Mul(Pow(10, 25), Sym("yottav")));
        c.put(UnitsElectricPotential.EXAVOLT, Mul(Pow(10, 6), Sym("yottav")));
        c.put(UnitsElectricPotential.FEMTOVOLT, Mul(Pow(10, 39), Sym("yottav")));
        c.put(UnitsElectricPotential.GIGAVOLT, Mul(Pow(10, 15), Sym("yottav")));
        c.put(UnitsElectricPotential.HECTOVOLT, Mul(Pow(10, 22), Sym("yottav")));
        c.put(UnitsElectricPotential.KILOVOLT, Mul(Pow(10, 21), Sym("yottav")));
        c.put(UnitsElectricPotential.MEGAVOLT, Mul(Pow(10, 18), Sym("yottav")));
        c.put(UnitsElectricPotential.MICROVOLT, Mul(Pow(10, 30), Sym("yottav")));
        c.put(UnitsElectricPotential.MILLIVOLT, Mul(Pow(10, 27), Sym("yottav")));
        c.put(UnitsElectricPotential.NANOVOLT, Mul(Pow(10, 33), Sym("yottav")));
        c.put(UnitsElectricPotential.PETAVOLT, Mul(Pow(10, 9), Sym("yottav")));
        c.put(UnitsElectricPotential.PICOVOLT, Mul(Pow(10, 36), Sym("yottav")));
        c.put(UnitsElectricPotential.TERAVOLT, Mul(Pow(10, 12), Sym("yottav")));
        c.put(UnitsElectricPotential.VOLT, Mul(Pow(10, 24), Sym("yottav")));
        c.put(UnitsElectricPotential.YOCTOVOLT, Mul(Pow(10, 48), Sym("yottav")));
        c.put(UnitsElectricPotential.ZEPTOVOLT, Mul(Pow(10, 45), Sym("yottav")));
        c.put(UnitsElectricPotential.ZETTAVOLT, Mul(Int(1000), Sym("yottav")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsElectricPotential, Conversion> createMapZEPTOVOLT() {
        EnumMap<UnitsElectricPotential, Conversion> c =
            new EnumMap<UnitsElectricPotential, Conversion>(UnitsElectricPotential.class);
        c.put(UnitsElectricPotential.ATTOVOLT, Mul(Rat(Int(1), Int(1000)), Sym("zeptov")));
        c.put(UnitsElectricPotential.CENTIVOLT, Mul(Rat(Int(1), Pow(10, 19)), Sym("zeptov")));
        c.put(UnitsElectricPotential.DECAVOLT, Mul(Rat(Int(1), Pow(10, 22)), Sym("zeptov")));
        c.put(UnitsElectricPotential.DECIVOLT, Mul(Rat(Int(1), Pow(10, 20)), Sym("zeptov")));
        c.put(UnitsElectricPotential.EXAVOLT, Mul(Rat(Int(1), Pow(10, 39)), Sym("zeptov")));
        c.put(UnitsElectricPotential.FEMTOVOLT, Mul(Rat(Int(1), Pow(10, 6)), Sym("zeptov")));
        c.put(UnitsElectricPotential.GIGAVOLT, Mul(Rat(Int(1), Pow(10, 30)), Sym("zeptov")));
        c.put(UnitsElectricPotential.HECTOVOLT, Mul(Rat(Int(1), Pow(10, 23)), Sym("zeptov")));
        c.put(UnitsElectricPotential.KILOVOLT, Mul(Rat(Int(1), Pow(10, 24)), Sym("zeptov")));
        c.put(UnitsElectricPotential.MEGAVOLT, Mul(Rat(Int(1), Pow(10, 27)), Sym("zeptov")));
        c.put(UnitsElectricPotential.MICROVOLT, Mul(Rat(Int(1), Pow(10, 15)), Sym("zeptov")));
        c.put(UnitsElectricPotential.MILLIVOLT, Mul(Rat(Int(1), Pow(10, 18)), Sym("zeptov")));
        c.put(UnitsElectricPotential.NANOVOLT, Mul(Rat(Int(1), Pow(10, 12)), Sym("zeptov")));
        c.put(UnitsElectricPotential.PETAVOLT, Mul(Rat(Int(1), Pow(10, 36)), Sym("zeptov")));
        c.put(UnitsElectricPotential.PICOVOLT, Mul(Rat(Int(1), Pow(10, 9)), Sym("zeptov")));
        c.put(UnitsElectricPotential.TERAVOLT, Mul(Rat(Int(1), Pow(10, 33)), Sym("zeptov")));
        c.put(UnitsElectricPotential.VOLT, Mul(Rat(Int(1), Pow(10, 21)), Sym("zeptov")));
        c.put(UnitsElectricPotential.YOCTOVOLT, Mul(Int(1000), Sym("zeptov")));
        c.put(UnitsElectricPotential.YOTTAVOLT, Mul(Rat(Int(1), Pow(10, 45)), Sym("zeptov")));
        c.put(UnitsElectricPotential.ZETTAVOLT, Mul(Rat(Int(1), Pow(10, 42)), Sym("zeptov")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsElectricPotential, Conversion> createMapZETTAVOLT() {
        EnumMap<UnitsElectricPotential, Conversion> c =
            new EnumMap<UnitsElectricPotential, Conversion>(UnitsElectricPotential.class);
        c.put(UnitsElectricPotential.ATTOVOLT, Mul(Pow(10, 39), Sym("zettav")));
        c.put(UnitsElectricPotential.CENTIVOLT, Mul(Pow(10, 23), Sym("zettav")));
        c.put(UnitsElectricPotential.DECAVOLT, Mul(Pow(10, 20), Sym("zettav")));
        c.put(UnitsElectricPotential.DECIVOLT, Mul(Pow(10, 22), Sym("zettav")));
        c.put(UnitsElectricPotential.EXAVOLT, Mul(Int(1000), Sym("zettav")));
        c.put(UnitsElectricPotential.FEMTOVOLT, Mul(Pow(10, 36), Sym("zettav")));
        c.put(UnitsElectricPotential.GIGAVOLT, Mul(Pow(10, 12), Sym("zettav")));
        c.put(UnitsElectricPotential.HECTOVOLT, Mul(Pow(10, 19), Sym("zettav")));
        c.put(UnitsElectricPotential.KILOVOLT, Mul(Pow(10, 18), Sym("zettav")));
        c.put(UnitsElectricPotential.MEGAVOLT, Mul(Pow(10, 15), Sym("zettav")));
        c.put(UnitsElectricPotential.MICROVOLT, Mul(Pow(10, 27), Sym("zettav")));
        c.put(UnitsElectricPotential.MILLIVOLT, Mul(Pow(10, 24), Sym("zettav")));
        c.put(UnitsElectricPotential.NANOVOLT, Mul(Pow(10, 30), Sym("zettav")));
        c.put(UnitsElectricPotential.PETAVOLT, Mul(Pow(10, 6), Sym("zettav")));
        c.put(UnitsElectricPotential.PICOVOLT, Mul(Pow(10, 33), Sym("zettav")));
        c.put(UnitsElectricPotential.TERAVOLT, Mul(Pow(10, 9), Sym("zettav")));
        c.put(UnitsElectricPotential.VOLT, Mul(Pow(10, 21), Sym("zettav")));
        c.put(UnitsElectricPotential.YOCTOVOLT, Mul(Pow(10, 45), Sym("zettav")));
        c.put(UnitsElectricPotential.YOTTAVOLT, Mul(Rat(Int(1), Int(1000)), Sym("zettav")));
        c.put(UnitsElectricPotential.ZEPTOVOLT, Mul(Pow(10, 42), Sym("zettav")));
        return Collections.unmodifiableMap(c);
    }

    private static final Map<UnitsElectricPotential, Map<UnitsElectricPotential, Conversion>> conversions;
    static {

        Map<UnitsElectricPotential, Map<UnitsElectricPotential, Conversion>> c
            = new EnumMap<UnitsElectricPotential, Map<UnitsElectricPotential, Conversion>>(UnitsElectricPotential.class);

        c.put(UnitsElectricPotential.ATTOVOLT, createMapATTOVOLT());
        c.put(UnitsElectricPotential.CENTIVOLT, createMapCENTIVOLT());
        c.put(UnitsElectricPotential.DECAVOLT, createMapDECAVOLT());
        c.put(UnitsElectricPotential.DECIVOLT, createMapDECIVOLT());
        c.put(UnitsElectricPotential.EXAVOLT, createMapEXAVOLT());
        c.put(UnitsElectricPotential.FEMTOVOLT, createMapFEMTOVOLT());
        c.put(UnitsElectricPotential.GIGAVOLT, createMapGIGAVOLT());
        c.put(UnitsElectricPotential.HECTOVOLT, createMapHECTOVOLT());
        c.put(UnitsElectricPotential.KILOVOLT, createMapKILOVOLT());
        c.put(UnitsElectricPotential.MEGAVOLT, createMapMEGAVOLT());
        c.put(UnitsElectricPotential.MICROVOLT, createMapMICROVOLT());
        c.put(UnitsElectricPotential.MILLIVOLT, createMapMILLIVOLT());
        c.put(UnitsElectricPotential.NANOVOLT, createMapNANOVOLT());
        c.put(UnitsElectricPotential.PETAVOLT, createMapPETAVOLT());
        c.put(UnitsElectricPotential.PICOVOLT, createMapPICOVOLT());
        c.put(UnitsElectricPotential.TERAVOLT, createMapTERAVOLT());
        c.put(UnitsElectricPotential.VOLT, createMapVOLT());
        c.put(UnitsElectricPotential.YOCTOVOLT, createMapYOCTOVOLT());
        c.put(UnitsElectricPotential.YOTTAVOLT, createMapYOTTAVOLT());
        c.put(UnitsElectricPotential.ZEPTOVOLT, createMapZEPTOVOLT());
        c.put(UnitsElectricPotential.ZETTAVOLT, createMapZETTAVOLT());
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
            if (Double.isInfinite(converted)) {
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

