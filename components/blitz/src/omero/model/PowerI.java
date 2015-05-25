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

    private static Map<UnitsPower, Conversion> createMapATTOWATT() {
        EnumMap<UnitsPower, Conversion> c =
            new EnumMap<UnitsPower, Conversion>(UnitsPower.class);
        c.put(UnitsPower.CENTIWATT, Mul(Rat(Int(1), Pow(10, 16)), Sym("attow")));
        c.put(UnitsPower.DECAWATT, Mul(Rat(Int(1), Pow(10, 19)), Sym("attow")));
        c.put(UnitsPower.DECIWATT, Mul(Rat(Int(1), Pow(10, 17)), Sym("attow")));
        c.put(UnitsPower.EXAWATT, Mul(Rat(Int(1), Pow(10, 36)), Sym("attow")));
        c.put(UnitsPower.FEMTOWATT, Mul(Rat(Int(1), Int(1000)), Sym("attow")));
        c.put(UnitsPower.GIGAWATT, Mul(Rat(Int(1), Pow(10, 27)), Sym("attow")));
        c.put(UnitsPower.HECTOWATT, Mul(Rat(Int(1), Pow(10, 20)), Sym("attow")));
        c.put(UnitsPower.KILOWATT, Mul(Rat(Int(1), Pow(10, 21)), Sym("attow")));
        c.put(UnitsPower.MEGAWATT, Mul(Rat(Int(1), Pow(10, 24)), Sym("attow")));
        c.put(UnitsPower.MICROWATT, Mul(Rat(Int(1), Pow(10, 12)), Sym("attow")));
        c.put(UnitsPower.MILLIWATT, Mul(Rat(Int(1), Pow(10, 15)), Sym("attow")));
        c.put(UnitsPower.NANOWATT, Mul(Rat(Int(1), Pow(10, 9)), Sym("attow")));
        c.put(UnitsPower.PETAWATT, Mul(Rat(Int(1), Pow(10, 33)), Sym("attow")));
        c.put(UnitsPower.PICOWATT, Mul(Rat(Int(1), Pow(10, 6)), Sym("attow")));
        c.put(UnitsPower.TERAWATT, Mul(Rat(Int(1), Pow(10, 30)), Sym("attow")));
        c.put(UnitsPower.WATT, Mul(Rat(Int(1), Pow(10, 18)), Sym("attow")));
        c.put(UnitsPower.YOCTOWATT, Mul(Pow(10, 6), Sym("attow")));
        c.put(UnitsPower.YOTTAWATT, Mul(Rat(Int(1), Pow(10, 42)), Sym("attow")));
        c.put(UnitsPower.ZEPTOWATT, Mul(Int(1000), Sym("attow")));
        c.put(UnitsPower.ZETTAWATT, Mul(Rat(Int(1), Pow(10, 39)), Sym("attow")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsPower, Conversion> createMapCENTIWATT() {
        EnumMap<UnitsPower, Conversion> c =
            new EnumMap<UnitsPower, Conversion>(UnitsPower.class);
        c.put(UnitsPower.ATTOWATT, Mul(Pow(10, 16), Sym("centiw")));
        c.put(UnitsPower.DECAWATT, Mul(Rat(Int(1), Int(1000)), Sym("centiw")));
        c.put(UnitsPower.DECIWATT, Mul(Rat(Int(1), Int(10)), Sym("centiw")));
        c.put(UnitsPower.EXAWATT, Mul(Rat(Int(1), Pow(10, 20)), Sym("centiw")));
        c.put(UnitsPower.FEMTOWATT, Mul(Pow(10, 13), Sym("centiw")));
        c.put(UnitsPower.GIGAWATT, Mul(Rat(Int(1), Pow(10, 11)), Sym("centiw")));
        c.put(UnitsPower.HECTOWATT, Mul(Rat(Int(1), Pow(10, 4)), Sym("centiw")));
        c.put(UnitsPower.KILOWATT, Mul(Rat(Int(1), Pow(10, 5)), Sym("centiw")));
        c.put(UnitsPower.MEGAWATT, Mul(Rat(Int(1), Pow(10, 8)), Sym("centiw")));
        c.put(UnitsPower.MICROWATT, Mul(Pow(10, 4), Sym("centiw")));
        c.put(UnitsPower.MILLIWATT, Mul(Int(10), Sym("centiw")));
        c.put(UnitsPower.NANOWATT, Mul(Pow(10, 7), Sym("centiw")));
        c.put(UnitsPower.PETAWATT, Mul(Rat(Int(1), Pow(10, 17)), Sym("centiw")));
        c.put(UnitsPower.PICOWATT, Mul(Pow(10, 10), Sym("centiw")));
        c.put(UnitsPower.TERAWATT, Mul(Rat(Int(1), Pow(10, 14)), Sym("centiw")));
        c.put(UnitsPower.WATT, Mul(Rat(Int(1), Int(100)), Sym("centiw")));
        c.put(UnitsPower.YOCTOWATT, Mul(Pow(10, 22), Sym("centiw")));
        c.put(UnitsPower.YOTTAWATT, Mul(Rat(Int(1), Pow(10, 26)), Sym("centiw")));
        c.put(UnitsPower.ZEPTOWATT, Mul(Pow(10, 19), Sym("centiw")));
        c.put(UnitsPower.ZETTAWATT, Mul(Rat(Int(1), Pow(10, 23)), Sym("centiw")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsPower, Conversion> createMapDECAWATT() {
        EnumMap<UnitsPower, Conversion> c =
            new EnumMap<UnitsPower, Conversion>(UnitsPower.class);
        c.put(UnitsPower.ATTOWATT, Mul(Pow(10, 19), Sym("decaw")));
        c.put(UnitsPower.CENTIWATT, Mul(Int(1000), Sym("decaw")));
        c.put(UnitsPower.DECIWATT, Mul(Int(100), Sym("decaw")));
        c.put(UnitsPower.EXAWATT, Mul(Rat(Int(1), Pow(10, 17)), Sym("decaw")));
        c.put(UnitsPower.FEMTOWATT, Mul(Pow(10, 16), Sym("decaw")));
        c.put(UnitsPower.GIGAWATT, Mul(Rat(Int(1), Pow(10, 8)), Sym("decaw")));
        c.put(UnitsPower.HECTOWATT, Mul(Rat(Int(1), Int(10)), Sym("decaw")));
        c.put(UnitsPower.KILOWATT, Mul(Rat(Int(1), Int(100)), Sym("decaw")));
        c.put(UnitsPower.MEGAWATT, Mul(Rat(Int(1), Pow(10, 5)), Sym("decaw")));
        c.put(UnitsPower.MICROWATT, Mul(Pow(10, 7), Sym("decaw")));
        c.put(UnitsPower.MILLIWATT, Mul(Pow(10, 4), Sym("decaw")));
        c.put(UnitsPower.NANOWATT, Mul(Pow(10, 10), Sym("decaw")));
        c.put(UnitsPower.PETAWATT, Mul(Rat(Int(1), Pow(10, 14)), Sym("decaw")));
        c.put(UnitsPower.PICOWATT, Mul(Pow(10, 13), Sym("decaw")));
        c.put(UnitsPower.TERAWATT, Mul(Rat(Int(1), Pow(10, 11)), Sym("decaw")));
        c.put(UnitsPower.WATT, Mul(Int(10), Sym("decaw")));
        c.put(UnitsPower.YOCTOWATT, Mul(Pow(10, 25), Sym("decaw")));
        c.put(UnitsPower.YOTTAWATT, Mul(Rat(Int(1), Pow(10, 23)), Sym("decaw")));
        c.put(UnitsPower.ZEPTOWATT, Mul(Pow(10, 22), Sym("decaw")));
        c.put(UnitsPower.ZETTAWATT, Mul(Rat(Int(1), Pow(10, 20)), Sym("decaw")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsPower, Conversion> createMapDECIWATT() {
        EnumMap<UnitsPower, Conversion> c =
            new EnumMap<UnitsPower, Conversion>(UnitsPower.class);
        c.put(UnitsPower.ATTOWATT, Mul(Pow(10, 17), Sym("deciw")));
        c.put(UnitsPower.CENTIWATT, Mul(Int(10), Sym("deciw")));
        c.put(UnitsPower.DECAWATT, Mul(Rat(Int(1), Int(100)), Sym("deciw")));
        c.put(UnitsPower.EXAWATT, Mul(Rat(Int(1), Pow(10, 19)), Sym("deciw")));
        c.put(UnitsPower.FEMTOWATT, Mul(Pow(10, 14), Sym("deciw")));
        c.put(UnitsPower.GIGAWATT, Mul(Rat(Int(1), Pow(10, 10)), Sym("deciw")));
        c.put(UnitsPower.HECTOWATT, Mul(Rat(Int(1), Int(1000)), Sym("deciw")));
        c.put(UnitsPower.KILOWATT, Mul(Rat(Int(1), Pow(10, 4)), Sym("deciw")));
        c.put(UnitsPower.MEGAWATT, Mul(Rat(Int(1), Pow(10, 7)), Sym("deciw")));
        c.put(UnitsPower.MICROWATT, Mul(Pow(10, 5), Sym("deciw")));
        c.put(UnitsPower.MILLIWATT, Mul(Int(100), Sym("deciw")));
        c.put(UnitsPower.NANOWATT, Mul(Pow(10, 8), Sym("deciw")));
        c.put(UnitsPower.PETAWATT, Mul(Rat(Int(1), Pow(10, 16)), Sym("deciw")));
        c.put(UnitsPower.PICOWATT, Mul(Pow(10, 11), Sym("deciw")));
        c.put(UnitsPower.TERAWATT, Mul(Rat(Int(1), Pow(10, 13)), Sym("deciw")));
        c.put(UnitsPower.WATT, Mul(Rat(Int(1), Int(10)), Sym("deciw")));
        c.put(UnitsPower.YOCTOWATT, Mul(Pow(10, 23), Sym("deciw")));
        c.put(UnitsPower.YOTTAWATT, Mul(Rat(Int(1), Pow(10, 25)), Sym("deciw")));
        c.put(UnitsPower.ZEPTOWATT, Mul(Pow(10, 20), Sym("deciw")));
        c.put(UnitsPower.ZETTAWATT, Mul(Rat(Int(1), Pow(10, 22)), Sym("deciw")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsPower, Conversion> createMapEXAWATT() {
        EnumMap<UnitsPower, Conversion> c =
            new EnumMap<UnitsPower, Conversion>(UnitsPower.class);
        c.put(UnitsPower.ATTOWATT, Mul(Pow(10, 36), Sym("exaw")));
        c.put(UnitsPower.CENTIWATT, Mul(Pow(10, 20), Sym("exaw")));
        c.put(UnitsPower.DECAWATT, Mul(Pow(10, 17), Sym("exaw")));
        c.put(UnitsPower.DECIWATT, Mul(Pow(10, 19), Sym("exaw")));
        c.put(UnitsPower.FEMTOWATT, Mul(Pow(10, 33), Sym("exaw")));
        c.put(UnitsPower.GIGAWATT, Mul(Pow(10, 9), Sym("exaw")));
        c.put(UnitsPower.HECTOWATT, Mul(Pow(10, 16), Sym("exaw")));
        c.put(UnitsPower.KILOWATT, Mul(Pow(10, 15), Sym("exaw")));
        c.put(UnitsPower.MEGAWATT, Mul(Pow(10, 12), Sym("exaw")));
        c.put(UnitsPower.MICROWATT, Mul(Pow(10, 24), Sym("exaw")));
        c.put(UnitsPower.MILLIWATT, Mul(Pow(10, 21), Sym("exaw")));
        c.put(UnitsPower.NANOWATT, Mul(Pow(10, 27), Sym("exaw")));
        c.put(UnitsPower.PETAWATT, Mul(Int(1000), Sym("exaw")));
        c.put(UnitsPower.PICOWATT, Mul(Pow(10, 30), Sym("exaw")));
        c.put(UnitsPower.TERAWATT, Mul(Pow(10, 6), Sym("exaw")));
        c.put(UnitsPower.WATT, Mul(Pow(10, 18), Sym("exaw")));
        c.put(UnitsPower.YOCTOWATT, Mul(Pow(10, 42), Sym("exaw")));
        c.put(UnitsPower.YOTTAWATT, Mul(Rat(Int(1), Pow(10, 6)), Sym("exaw")));
        c.put(UnitsPower.ZEPTOWATT, Mul(Pow(10, 39), Sym("exaw")));
        c.put(UnitsPower.ZETTAWATT, Mul(Rat(Int(1), Int(1000)), Sym("exaw")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsPower, Conversion> createMapFEMTOWATT() {
        EnumMap<UnitsPower, Conversion> c =
            new EnumMap<UnitsPower, Conversion>(UnitsPower.class);
        c.put(UnitsPower.ATTOWATT, Mul(Int(1000), Sym("femtow")));
        c.put(UnitsPower.CENTIWATT, Mul(Rat(Int(1), Pow(10, 13)), Sym("femtow")));
        c.put(UnitsPower.DECAWATT, Mul(Rat(Int(1), Pow(10, 16)), Sym("femtow")));
        c.put(UnitsPower.DECIWATT, Mul(Rat(Int(1), Pow(10, 14)), Sym("femtow")));
        c.put(UnitsPower.EXAWATT, Mul(Rat(Int(1), Pow(10, 33)), Sym("femtow")));
        c.put(UnitsPower.GIGAWATT, Mul(Rat(Int(1), Pow(10, 24)), Sym("femtow")));
        c.put(UnitsPower.HECTOWATT, Mul(Rat(Int(1), Pow(10, 17)), Sym("femtow")));
        c.put(UnitsPower.KILOWATT, Mul(Rat(Int(1), Pow(10, 18)), Sym("femtow")));
        c.put(UnitsPower.MEGAWATT, Mul(Rat(Int(1), Pow(10, 21)), Sym("femtow")));
        c.put(UnitsPower.MICROWATT, Mul(Rat(Int(1), Pow(10, 9)), Sym("femtow")));
        c.put(UnitsPower.MILLIWATT, Mul(Rat(Int(1), Pow(10, 12)), Sym("femtow")));
        c.put(UnitsPower.NANOWATT, Mul(Rat(Int(1), Pow(10, 6)), Sym("femtow")));
        c.put(UnitsPower.PETAWATT, Mul(Rat(Int(1), Pow(10, 30)), Sym("femtow")));
        c.put(UnitsPower.PICOWATT, Mul(Rat(Int(1), Int(1000)), Sym("femtow")));
        c.put(UnitsPower.TERAWATT, Mul(Rat(Int(1), Pow(10, 27)), Sym("femtow")));
        c.put(UnitsPower.WATT, Mul(Rat(Int(1), Pow(10, 15)), Sym("femtow")));
        c.put(UnitsPower.YOCTOWATT, Mul(Pow(10, 9), Sym("femtow")));
        c.put(UnitsPower.YOTTAWATT, Mul(Rat(Int(1), Pow(10, 39)), Sym("femtow")));
        c.put(UnitsPower.ZEPTOWATT, Mul(Pow(10, 6), Sym("femtow")));
        c.put(UnitsPower.ZETTAWATT, Mul(Rat(Int(1), Pow(10, 36)), Sym("femtow")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsPower, Conversion> createMapGIGAWATT() {
        EnumMap<UnitsPower, Conversion> c =
            new EnumMap<UnitsPower, Conversion>(UnitsPower.class);
        c.put(UnitsPower.ATTOWATT, Mul(Pow(10, 27), Sym("gigaw")));
        c.put(UnitsPower.CENTIWATT, Mul(Pow(10, 11), Sym("gigaw")));
        c.put(UnitsPower.DECAWATT, Mul(Pow(10, 8), Sym("gigaw")));
        c.put(UnitsPower.DECIWATT, Mul(Pow(10, 10), Sym("gigaw")));
        c.put(UnitsPower.EXAWATT, Mul(Rat(Int(1), Pow(10, 9)), Sym("gigaw")));
        c.put(UnitsPower.FEMTOWATT, Mul(Pow(10, 24), Sym("gigaw")));
        c.put(UnitsPower.HECTOWATT, Mul(Pow(10, 7), Sym("gigaw")));
        c.put(UnitsPower.KILOWATT, Mul(Pow(10, 6), Sym("gigaw")));
        c.put(UnitsPower.MEGAWATT, Mul(Int(1000), Sym("gigaw")));
        c.put(UnitsPower.MICROWATT, Mul(Pow(10, 15), Sym("gigaw")));
        c.put(UnitsPower.MILLIWATT, Mul(Pow(10, 12), Sym("gigaw")));
        c.put(UnitsPower.NANOWATT, Mul(Pow(10, 18), Sym("gigaw")));
        c.put(UnitsPower.PETAWATT, Mul(Rat(Int(1), Pow(10, 6)), Sym("gigaw")));
        c.put(UnitsPower.PICOWATT, Mul(Pow(10, 21), Sym("gigaw")));
        c.put(UnitsPower.TERAWATT, Mul(Rat(Int(1), Int(1000)), Sym("gigaw")));
        c.put(UnitsPower.WATT, Mul(Pow(10, 9), Sym("gigaw")));
        c.put(UnitsPower.YOCTOWATT, Mul(Pow(10, 33), Sym("gigaw")));
        c.put(UnitsPower.YOTTAWATT, Mul(Rat(Int(1), Pow(10, 15)), Sym("gigaw")));
        c.put(UnitsPower.ZEPTOWATT, Mul(Pow(10, 30), Sym("gigaw")));
        c.put(UnitsPower.ZETTAWATT, Mul(Rat(Int(1), Pow(10, 12)), Sym("gigaw")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsPower, Conversion> createMapHECTOWATT() {
        EnumMap<UnitsPower, Conversion> c =
            new EnumMap<UnitsPower, Conversion>(UnitsPower.class);
        c.put(UnitsPower.ATTOWATT, Mul(Pow(10, 20), Sym("hectow")));
        c.put(UnitsPower.CENTIWATT, Mul(Pow(10, 4), Sym("hectow")));
        c.put(UnitsPower.DECAWATT, Mul(Int(10), Sym("hectow")));
        c.put(UnitsPower.DECIWATT, Mul(Int(1000), Sym("hectow")));
        c.put(UnitsPower.EXAWATT, Mul(Rat(Int(1), Pow(10, 16)), Sym("hectow")));
        c.put(UnitsPower.FEMTOWATT, Mul(Pow(10, 17), Sym("hectow")));
        c.put(UnitsPower.GIGAWATT, Mul(Rat(Int(1), Pow(10, 7)), Sym("hectow")));
        c.put(UnitsPower.KILOWATT, Mul(Rat(Int(1), Int(10)), Sym("hectow")));
        c.put(UnitsPower.MEGAWATT, Mul(Rat(Int(1), Pow(10, 4)), Sym("hectow")));
        c.put(UnitsPower.MICROWATT, Mul(Pow(10, 8), Sym("hectow")));
        c.put(UnitsPower.MILLIWATT, Mul(Pow(10, 5), Sym("hectow")));
        c.put(UnitsPower.NANOWATT, Mul(Pow(10, 11), Sym("hectow")));
        c.put(UnitsPower.PETAWATT, Mul(Rat(Int(1), Pow(10, 13)), Sym("hectow")));
        c.put(UnitsPower.PICOWATT, Mul(Pow(10, 14), Sym("hectow")));
        c.put(UnitsPower.TERAWATT, Mul(Rat(Int(1), Pow(10, 10)), Sym("hectow")));
        c.put(UnitsPower.WATT, Mul(Int(100), Sym("hectow")));
        c.put(UnitsPower.YOCTOWATT, Mul(Pow(10, 26), Sym("hectow")));
        c.put(UnitsPower.YOTTAWATT, Mul(Rat(Int(1), Pow(10, 22)), Sym("hectow")));
        c.put(UnitsPower.ZEPTOWATT, Mul(Pow(10, 23), Sym("hectow")));
        c.put(UnitsPower.ZETTAWATT, Mul(Rat(Int(1), Pow(10, 19)), Sym("hectow")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsPower, Conversion> createMapKILOWATT() {
        EnumMap<UnitsPower, Conversion> c =
            new EnumMap<UnitsPower, Conversion>(UnitsPower.class);
        c.put(UnitsPower.ATTOWATT, Mul(Pow(10, 21), Sym("kilow")));
        c.put(UnitsPower.CENTIWATT, Mul(Pow(10, 5), Sym("kilow")));
        c.put(UnitsPower.DECAWATT, Mul(Int(100), Sym("kilow")));
        c.put(UnitsPower.DECIWATT, Mul(Pow(10, 4), Sym("kilow")));
        c.put(UnitsPower.EXAWATT, Mul(Rat(Int(1), Pow(10, 15)), Sym("kilow")));
        c.put(UnitsPower.FEMTOWATT, Mul(Pow(10, 18), Sym("kilow")));
        c.put(UnitsPower.GIGAWATT, Mul(Rat(Int(1), Pow(10, 6)), Sym("kilow")));
        c.put(UnitsPower.HECTOWATT, Mul(Int(10), Sym("kilow")));
        c.put(UnitsPower.MEGAWATT, Mul(Rat(Int(1), Int(1000)), Sym("kilow")));
        c.put(UnitsPower.MICROWATT, Mul(Pow(10, 9), Sym("kilow")));
        c.put(UnitsPower.MILLIWATT, Mul(Pow(10, 6), Sym("kilow")));
        c.put(UnitsPower.NANOWATT, Mul(Pow(10, 12), Sym("kilow")));
        c.put(UnitsPower.PETAWATT, Mul(Rat(Int(1), Pow(10, 12)), Sym("kilow")));
        c.put(UnitsPower.PICOWATT, Mul(Pow(10, 15), Sym("kilow")));
        c.put(UnitsPower.TERAWATT, Mul(Rat(Int(1), Pow(10, 9)), Sym("kilow")));
        c.put(UnitsPower.WATT, Mul(Int(1000), Sym("kilow")));
        c.put(UnitsPower.YOCTOWATT, Mul(Pow(10, 27), Sym("kilow")));
        c.put(UnitsPower.YOTTAWATT, Mul(Rat(Int(1), Pow(10, 21)), Sym("kilow")));
        c.put(UnitsPower.ZEPTOWATT, Mul(Pow(10, 24), Sym("kilow")));
        c.put(UnitsPower.ZETTAWATT, Mul(Rat(Int(1), Pow(10, 18)), Sym("kilow")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsPower, Conversion> createMapMEGAWATT() {
        EnumMap<UnitsPower, Conversion> c =
            new EnumMap<UnitsPower, Conversion>(UnitsPower.class);
        c.put(UnitsPower.ATTOWATT, Mul(Pow(10, 24), Sym("megaw")));
        c.put(UnitsPower.CENTIWATT, Mul(Pow(10, 8), Sym("megaw")));
        c.put(UnitsPower.DECAWATT, Mul(Pow(10, 5), Sym("megaw")));
        c.put(UnitsPower.DECIWATT, Mul(Pow(10, 7), Sym("megaw")));
        c.put(UnitsPower.EXAWATT, Mul(Rat(Int(1), Pow(10, 12)), Sym("megaw")));
        c.put(UnitsPower.FEMTOWATT, Mul(Pow(10, 21), Sym("megaw")));
        c.put(UnitsPower.GIGAWATT, Mul(Rat(Int(1), Int(1000)), Sym("megaw")));
        c.put(UnitsPower.HECTOWATT, Mul(Pow(10, 4), Sym("megaw")));
        c.put(UnitsPower.KILOWATT, Mul(Int(1000), Sym("megaw")));
        c.put(UnitsPower.MICROWATT, Mul(Pow(10, 12), Sym("megaw")));
        c.put(UnitsPower.MILLIWATT, Mul(Pow(10, 9), Sym("megaw")));
        c.put(UnitsPower.NANOWATT, Mul(Pow(10, 15), Sym("megaw")));
        c.put(UnitsPower.PETAWATT, Mul(Rat(Int(1), Pow(10, 9)), Sym("megaw")));
        c.put(UnitsPower.PICOWATT, Mul(Pow(10, 18), Sym("megaw")));
        c.put(UnitsPower.TERAWATT, Mul(Rat(Int(1), Pow(10, 6)), Sym("megaw")));
        c.put(UnitsPower.WATT, Mul(Pow(10, 6), Sym("megaw")));
        c.put(UnitsPower.YOCTOWATT, Mul(Pow(10, 30), Sym("megaw")));
        c.put(UnitsPower.YOTTAWATT, Mul(Rat(Int(1), Pow(10, 18)), Sym("megaw")));
        c.put(UnitsPower.ZEPTOWATT, Mul(Pow(10, 27), Sym("megaw")));
        c.put(UnitsPower.ZETTAWATT, Mul(Rat(Int(1), Pow(10, 15)), Sym("megaw")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsPower, Conversion> createMapMICROWATT() {
        EnumMap<UnitsPower, Conversion> c =
            new EnumMap<UnitsPower, Conversion>(UnitsPower.class);
        c.put(UnitsPower.ATTOWATT, Mul(Pow(10, 12), Sym("microw")));
        c.put(UnitsPower.CENTIWATT, Mul(Rat(Int(1), Pow(10, 4)), Sym("microw")));
        c.put(UnitsPower.DECAWATT, Mul(Rat(Int(1), Pow(10, 7)), Sym("microw")));
        c.put(UnitsPower.DECIWATT, Mul(Rat(Int(1), Pow(10, 5)), Sym("microw")));
        c.put(UnitsPower.EXAWATT, Mul(Rat(Int(1), Pow(10, 24)), Sym("microw")));
        c.put(UnitsPower.FEMTOWATT, Mul(Pow(10, 9), Sym("microw")));
        c.put(UnitsPower.GIGAWATT, Mul(Rat(Int(1), Pow(10, 15)), Sym("microw")));
        c.put(UnitsPower.HECTOWATT, Mul(Rat(Int(1), Pow(10, 8)), Sym("microw")));
        c.put(UnitsPower.KILOWATT, Mul(Rat(Int(1), Pow(10, 9)), Sym("microw")));
        c.put(UnitsPower.MEGAWATT, Mul(Rat(Int(1), Pow(10, 12)), Sym("microw")));
        c.put(UnitsPower.MILLIWATT, Mul(Rat(Int(1), Int(1000)), Sym("microw")));
        c.put(UnitsPower.NANOWATT, Mul(Int(1000), Sym("microw")));
        c.put(UnitsPower.PETAWATT, Mul(Rat(Int(1), Pow(10, 21)), Sym("microw")));
        c.put(UnitsPower.PICOWATT, Mul(Pow(10, 6), Sym("microw")));
        c.put(UnitsPower.TERAWATT, Mul(Rat(Int(1), Pow(10, 18)), Sym("microw")));
        c.put(UnitsPower.WATT, Mul(Rat(Int(1), Pow(10, 6)), Sym("microw")));
        c.put(UnitsPower.YOCTOWATT, Mul(Pow(10, 18), Sym("microw")));
        c.put(UnitsPower.YOTTAWATT, Mul(Rat(Int(1), Pow(10, 30)), Sym("microw")));
        c.put(UnitsPower.ZEPTOWATT, Mul(Pow(10, 15), Sym("microw")));
        c.put(UnitsPower.ZETTAWATT, Mul(Rat(Int(1), Pow(10, 27)), Sym("microw")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsPower, Conversion> createMapMILLIWATT() {
        EnumMap<UnitsPower, Conversion> c =
            new EnumMap<UnitsPower, Conversion>(UnitsPower.class);
        c.put(UnitsPower.ATTOWATT, Mul(Pow(10, 15), Sym("milliw")));
        c.put(UnitsPower.CENTIWATT, Mul(Rat(Int(1), Int(10)), Sym("milliw")));
        c.put(UnitsPower.DECAWATT, Mul(Rat(Int(1), Pow(10, 4)), Sym("milliw")));
        c.put(UnitsPower.DECIWATT, Mul(Rat(Int(1), Int(100)), Sym("milliw")));
        c.put(UnitsPower.EXAWATT, Mul(Rat(Int(1), Pow(10, 21)), Sym("milliw")));
        c.put(UnitsPower.FEMTOWATT, Mul(Pow(10, 12), Sym("milliw")));
        c.put(UnitsPower.GIGAWATT, Mul(Rat(Int(1), Pow(10, 12)), Sym("milliw")));
        c.put(UnitsPower.HECTOWATT, Mul(Rat(Int(1), Pow(10, 5)), Sym("milliw")));
        c.put(UnitsPower.KILOWATT, Mul(Rat(Int(1), Pow(10, 6)), Sym("milliw")));
        c.put(UnitsPower.MEGAWATT, Mul(Rat(Int(1), Pow(10, 9)), Sym("milliw")));
        c.put(UnitsPower.MICROWATT, Mul(Int(1000), Sym("milliw")));
        c.put(UnitsPower.NANOWATT, Mul(Pow(10, 6), Sym("milliw")));
        c.put(UnitsPower.PETAWATT, Mul(Rat(Int(1), Pow(10, 18)), Sym("milliw")));
        c.put(UnitsPower.PICOWATT, Mul(Pow(10, 9), Sym("milliw")));
        c.put(UnitsPower.TERAWATT, Mul(Rat(Int(1), Pow(10, 15)), Sym("milliw")));
        c.put(UnitsPower.WATT, Mul(Rat(Int(1), Int(1000)), Sym("milliw")));
        c.put(UnitsPower.YOCTOWATT, Mul(Pow(10, 21), Sym("milliw")));
        c.put(UnitsPower.YOTTAWATT, Mul(Rat(Int(1), Pow(10, 27)), Sym("milliw")));
        c.put(UnitsPower.ZEPTOWATT, Mul(Pow(10, 18), Sym("milliw")));
        c.put(UnitsPower.ZETTAWATT, Mul(Rat(Int(1), Pow(10, 24)), Sym("milliw")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsPower, Conversion> createMapNANOWATT() {
        EnumMap<UnitsPower, Conversion> c =
            new EnumMap<UnitsPower, Conversion>(UnitsPower.class);
        c.put(UnitsPower.ATTOWATT, Mul(Pow(10, 9), Sym("nanow")));
        c.put(UnitsPower.CENTIWATT, Mul(Rat(Int(1), Pow(10, 7)), Sym("nanow")));
        c.put(UnitsPower.DECAWATT, Mul(Rat(Int(1), Pow(10, 10)), Sym("nanow")));
        c.put(UnitsPower.DECIWATT, Mul(Rat(Int(1), Pow(10, 8)), Sym("nanow")));
        c.put(UnitsPower.EXAWATT, Mul(Rat(Int(1), Pow(10, 27)), Sym("nanow")));
        c.put(UnitsPower.FEMTOWATT, Mul(Pow(10, 6), Sym("nanow")));
        c.put(UnitsPower.GIGAWATT, Mul(Rat(Int(1), Pow(10, 18)), Sym("nanow")));
        c.put(UnitsPower.HECTOWATT, Mul(Rat(Int(1), Pow(10, 11)), Sym("nanow")));
        c.put(UnitsPower.KILOWATT, Mul(Rat(Int(1), Pow(10, 12)), Sym("nanow")));
        c.put(UnitsPower.MEGAWATT, Mul(Rat(Int(1), Pow(10, 15)), Sym("nanow")));
        c.put(UnitsPower.MICROWATT, Mul(Rat(Int(1), Int(1000)), Sym("nanow")));
        c.put(UnitsPower.MILLIWATT, Mul(Rat(Int(1), Pow(10, 6)), Sym("nanow")));
        c.put(UnitsPower.PETAWATT, Mul(Rat(Int(1), Pow(10, 24)), Sym("nanow")));
        c.put(UnitsPower.PICOWATT, Mul(Int(1000), Sym("nanow")));
        c.put(UnitsPower.TERAWATT, Mul(Rat(Int(1), Pow(10, 21)), Sym("nanow")));
        c.put(UnitsPower.WATT, Mul(Rat(Int(1), Pow(10, 9)), Sym("nanow")));
        c.put(UnitsPower.YOCTOWATT, Mul(Pow(10, 15), Sym("nanow")));
        c.put(UnitsPower.YOTTAWATT, Mul(Rat(Int(1), Pow(10, 33)), Sym("nanow")));
        c.put(UnitsPower.ZEPTOWATT, Mul(Pow(10, 12), Sym("nanow")));
        c.put(UnitsPower.ZETTAWATT, Mul(Rat(Int(1), Pow(10, 30)), Sym("nanow")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsPower, Conversion> createMapPETAWATT() {
        EnumMap<UnitsPower, Conversion> c =
            new EnumMap<UnitsPower, Conversion>(UnitsPower.class);
        c.put(UnitsPower.ATTOWATT, Mul(Pow(10, 33), Sym("petaw")));
        c.put(UnitsPower.CENTIWATT, Mul(Pow(10, 17), Sym("petaw")));
        c.put(UnitsPower.DECAWATT, Mul(Pow(10, 14), Sym("petaw")));
        c.put(UnitsPower.DECIWATT, Mul(Pow(10, 16), Sym("petaw")));
        c.put(UnitsPower.EXAWATT, Mul(Rat(Int(1), Int(1000)), Sym("petaw")));
        c.put(UnitsPower.FEMTOWATT, Mul(Pow(10, 30), Sym("petaw")));
        c.put(UnitsPower.GIGAWATT, Mul(Pow(10, 6), Sym("petaw")));
        c.put(UnitsPower.HECTOWATT, Mul(Pow(10, 13), Sym("petaw")));
        c.put(UnitsPower.KILOWATT, Mul(Pow(10, 12), Sym("petaw")));
        c.put(UnitsPower.MEGAWATT, Mul(Pow(10, 9), Sym("petaw")));
        c.put(UnitsPower.MICROWATT, Mul(Pow(10, 21), Sym("petaw")));
        c.put(UnitsPower.MILLIWATT, Mul(Pow(10, 18), Sym("petaw")));
        c.put(UnitsPower.NANOWATT, Mul(Pow(10, 24), Sym("petaw")));
        c.put(UnitsPower.PICOWATT, Mul(Pow(10, 27), Sym("petaw")));
        c.put(UnitsPower.TERAWATT, Mul(Int(1000), Sym("petaw")));
        c.put(UnitsPower.WATT, Mul(Pow(10, 15), Sym("petaw")));
        c.put(UnitsPower.YOCTOWATT, Mul(Pow(10, 39), Sym("petaw")));
        c.put(UnitsPower.YOTTAWATT, Mul(Rat(Int(1), Pow(10, 9)), Sym("petaw")));
        c.put(UnitsPower.ZEPTOWATT, Mul(Pow(10, 36), Sym("petaw")));
        c.put(UnitsPower.ZETTAWATT, Mul(Rat(Int(1), Pow(10, 6)), Sym("petaw")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsPower, Conversion> createMapPICOWATT() {
        EnumMap<UnitsPower, Conversion> c =
            new EnumMap<UnitsPower, Conversion>(UnitsPower.class);
        c.put(UnitsPower.ATTOWATT, Mul(Pow(10, 6), Sym("picow")));
        c.put(UnitsPower.CENTIWATT, Mul(Rat(Int(1), Pow(10, 10)), Sym("picow")));
        c.put(UnitsPower.DECAWATT, Mul(Rat(Int(1), Pow(10, 13)), Sym("picow")));
        c.put(UnitsPower.DECIWATT, Mul(Rat(Int(1), Pow(10, 11)), Sym("picow")));
        c.put(UnitsPower.EXAWATT, Mul(Rat(Int(1), Pow(10, 30)), Sym("picow")));
        c.put(UnitsPower.FEMTOWATT, Mul(Int(1000), Sym("picow")));
        c.put(UnitsPower.GIGAWATT, Mul(Rat(Int(1), Pow(10, 21)), Sym("picow")));
        c.put(UnitsPower.HECTOWATT, Mul(Rat(Int(1), Pow(10, 14)), Sym("picow")));
        c.put(UnitsPower.KILOWATT, Mul(Rat(Int(1), Pow(10, 15)), Sym("picow")));
        c.put(UnitsPower.MEGAWATT, Mul(Rat(Int(1), Pow(10, 18)), Sym("picow")));
        c.put(UnitsPower.MICROWATT, Mul(Rat(Int(1), Pow(10, 6)), Sym("picow")));
        c.put(UnitsPower.MILLIWATT, Mul(Rat(Int(1), Pow(10, 9)), Sym("picow")));
        c.put(UnitsPower.NANOWATT, Mul(Rat(Int(1), Int(1000)), Sym("picow")));
        c.put(UnitsPower.PETAWATT, Mul(Rat(Int(1), Pow(10, 27)), Sym("picow")));
        c.put(UnitsPower.TERAWATT, Mul(Rat(Int(1), Pow(10, 24)), Sym("picow")));
        c.put(UnitsPower.WATT, Mul(Rat(Int(1), Pow(10, 12)), Sym("picow")));
        c.put(UnitsPower.YOCTOWATT, Mul(Pow(10, 12), Sym("picow")));
        c.put(UnitsPower.YOTTAWATT, Mul(Rat(Int(1), Pow(10, 36)), Sym("picow")));
        c.put(UnitsPower.ZEPTOWATT, Mul(Pow(10, 9), Sym("picow")));
        c.put(UnitsPower.ZETTAWATT, Mul(Rat(Int(1), Pow(10, 33)), Sym("picow")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsPower, Conversion> createMapTERAWATT() {
        EnumMap<UnitsPower, Conversion> c =
            new EnumMap<UnitsPower, Conversion>(UnitsPower.class);
        c.put(UnitsPower.ATTOWATT, Mul(Pow(10, 30), Sym("teraw")));
        c.put(UnitsPower.CENTIWATT, Mul(Pow(10, 14), Sym("teraw")));
        c.put(UnitsPower.DECAWATT, Mul(Pow(10, 11), Sym("teraw")));
        c.put(UnitsPower.DECIWATT, Mul(Pow(10, 13), Sym("teraw")));
        c.put(UnitsPower.EXAWATT, Mul(Rat(Int(1), Pow(10, 6)), Sym("teraw")));
        c.put(UnitsPower.FEMTOWATT, Mul(Pow(10, 27), Sym("teraw")));
        c.put(UnitsPower.GIGAWATT, Mul(Int(1000), Sym("teraw")));
        c.put(UnitsPower.HECTOWATT, Mul(Pow(10, 10), Sym("teraw")));
        c.put(UnitsPower.KILOWATT, Mul(Pow(10, 9), Sym("teraw")));
        c.put(UnitsPower.MEGAWATT, Mul(Pow(10, 6), Sym("teraw")));
        c.put(UnitsPower.MICROWATT, Mul(Pow(10, 18), Sym("teraw")));
        c.put(UnitsPower.MILLIWATT, Mul(Pow(10, 15), Sym("teraw")));
        c.put(UnitsPower.NANOWATT, Mul(Pow(10, 21), Sym("teraw")));
        c.put(UnitsPower.PETAWATT, Mul(Rat(Int(1), Int(1000)), Sym("teraw")));
        c.put(UnitsPower.PICOWATT, Mul(Pow(10, 24), Sym("teraw")));
        c.put(UnitsPower.WATT, Mul(Pow(10, 12), Sym("teraw")));
        c.put(UnitsPower.YOCTOWATT, Mul(Pow(10, 36), Sym("teraw")));
        c.put(UnitsPower.YOTTAWATT, Mul(Rat(Int(1), Pow(10, 12)), Sym("teraw")));
        c.put(UnitsPower.ZEPTOWATT, Mul(Pow(10, 33), Sym("teraw")));
        c.put(UnitsPower.ZETTAWATT, Mul(Rat(Int(1), Pow(10, 9)), Sym("teraw")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsPower, Conversion> createMapWATT() {
        EnumMap<UnitsPower, Conversion> c =
            new EnumMap<UnitsPower, Conversion>(UnitsPower.class);
        c.put(UnitsPower.ATTOWATT, Mul(Pow(10, 18), Sym("w")));
        c.put(UnitsPower.CENTIWATT, Mul(Int(100), Sym("w")));
        c.put(UnitsPower.DECAWATT, Mul(Rat(Int(1), Int(10)), Sym("w")));
        c.put(UnitsPower.DECIWATT, Mul(Int(10), Sym("w")));
        c.put(UnitsPower.EXAWATT, Mul(Rat(Int(1), Pow(10, 18)), Sym("w")));
        c.put(UnitsPower.FEMTOWATT, Mul(Pow(10, 15), Sym("w")));
        c.put(UnitsPower.GIGAWATT, Mul(Rat(Int(1), Pow(10, 9)), Sym("w")));
        c.put(UnitsPower.HECTOWATT, Mul(Rat(Int(1), Int(100)), Sym("w")));
        c.put(UnitsPower.KILOWATT, Mul(Rat(Int(1), Int(1000)), Sym("w")));
        c.put(UnitsPower.MEGAWATT, Mul(Rat(Int(1), Pow(10, 6)), Sym("w")));
        c.put(UnitsPower.MICROWATT, Mul(Pow(10, 6), Sym("w")));
        c.put(UnitsPower.MILLIWATT, Mul(Int(1000), Sym("w")));
        c.put(UnitsPower.NANOWATT, Mul(Pow(10, 9), Sym("w")));
        c.put(UnitsPower.PETAWATT, Mul(Rat(Int(1), Pow(10, 15)), Sym("w")));
        c.put(UnitsPower.PICOWATT, Mul(Pow(10, 12), Sym("w")));
        c.put(UnitsPower.TERAWATT, Mul(Rat(Int(1), Pow(10, 12)), Sym("w")));
        c.put(UnitsPower.YOCTOWATT, Mul(Pow(10, 24), Sym("w")));
        c.put(UnitsPower.YOTTAWATT, Mul(Rat(Int(1), Pow(10, 24)), Sym("w")));
        c.put(UnitsPower.ZEPTOWATT, Mul(Pow(10, 21), Sym("w")));
        c.put(UnitsPower.ZETTAWATT, Mul(Rat(Int(1), Pow(10, 21)), Sym("w")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsPower, Conversion> createMapYOCTOWATT() {
        EnumMap<UnitsPower, Conversion> c =
            new EnumMap<UnitsPower, Conversion>(UnitsPower.class);
        c.put(UnitsPower.ATTOWATT, Mul(Rat(Int(1), Pow(10, 6)), Sym("yoctow")));
        c.put(UnitsPower.CENTIWATT, Mul(Rat(Int(1), Pow(10, 22)), Sym("yoctow")));
        c.put(UnitsPower.DECAWATT, Mul(Rat(Int(1), Pow(10, 25)), Sym("yoctow")));
        c.put(UnitsPower.DECIWATT, Mul(Rat(Int(1), Pow(10, 23)), Sym("yoctow")));
        c.put(UnitsPower.EXAWATT, Mul(Rat(Int(1), Pow(10, 42)), Sym("yoctow")));
        c.put(UnitsPower.FEMTOWATT, Mul(Rat(Int(1), Pow(10, 9)), Sym("yoctow")));
        c.put(UnitsPower.GIGAWATT, Mul(Rat(Int(1), Pow(10, 33)), Sym("yoctow")));
        c.put(UnitsPower.HECTOWATT, Mul(Rat(Int(1), Pow(10, 26)), Sym("yoctow")));
        c.put(UnitsPower.KILOWATT, Mul(Rat(Int(1), Pow(10, 27)), Sym("yoctow")));
        c.put(UnitsPower.MEGAWATT, Mul(Rat(Int(1), Pow(10, 30)), Sym("yoctow")));
        c.put(UnitsPower.MICROWATT, Mul(Rat(Int(1), Pow(10, 18)), Sym("yoctow")));
        c.put(UnitsPower.MILLIWATT, Mul(Rat(Int(1), Pow(10, 21)), Sym("yoctow")));
        c.put(UnitsPower.NANOWATT, Mul(Rat(Int(1), Pow(10, 15)), Sym("yoctow")));
        c.put(UnitsPower.PETAWATT, Mul(Rat(Int(1), Pow(10, 39)), Sym("yoctow")));
        c.put(UnitsPower.PICOWATT, Mul(Rat(Int(1), Pow(10, 12)), Sym("yoctow")));
        c.put(UnitsPower.TERAWATT, Mul(Rat(Int(1), Pow(10, 36)), Sym("yoctow")));
        c.put(UnitsPower.WATT, Mul(Rat(Int(1), Pow(10, 24)), Sym("yoctow")));
        c.put(UnitsPower.YOTTAWATT, Mul(Rat(Int(1), Pow(10, 48)), Sym("yoctow")));
        c.put(UnitsPower.ZEPTOWATT, Mul(Rat(Int(1), Int(1000)), Sym("yoctow")));
        c.put(UnitsPower.ZETTAWATT, Mul(Rat(Int(1), Pow(10, 45)), Sym("yoctow")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsPower, Conversion> createMapYOTTAWATT() {
        EnumMap<UnitsPower, Conversion> c =
            new EnumMap<UnitsPower, Conversion>(UnitsPower.class);
        c.put(UnitsPower.ATTOWATT, Mul(Pow(10, 42), Sym("yottaw")));
        c.put(UnitsPower.CENTIWATT, Mul(Pow(10, 26), Sym("yottaw")));
        c.put(UnitsPower.DECAWATT, Mul(Pow(10, 23), Sym("yottaw")));
        c.put(UnitsPower.DECIWATT, Mul(Pow(10, 25), Sym("yottaw")));
        c.put(UnitsPower.EXAWATT, Mul(Pow(10, 6), Sym("yottaw")));
        c.put(UnitsPower.FEMTOWATT, Mul(Pow(10, 39), Sym("yottaw")));
        c.put(UnitsPower.GIGAWATT, Mul(Pow(10, 15), Sym("yottaw")));
        c.put(UnitsPower.HECTOWATT, Mul(Pow(10, 22), Sym("yottaw")));
        c.put(UnitsPower.KILOWATT, Mul(Pow(10, 21), Sym("yottaw")));
        c.put(UnitsPower.MEGAWATT, Mul(Pow(10, 18), Sym("yottaw")));
        c.put(UnitsPower.MICROWATT, Mul(Pow(10, 30), Sym("yottaw")));
        c.put(UnitsPower.MILLIWATT, Mul(Pow(10, 27), Sym("yottaw")));
        c.put(UnitsPower.NANOWATT, Mul(Pow(10, 33), Sym("yottaw")));
        c.put(UnitsPower.PETAWATT, Mul(Pow(10, 9), Sym("yottaw")));
        c.put(UnitsPower.PICOWATT, Mul(Pow(10, 36), Sym("yottaw")));
        c.put(UnitsPower.TERAWATT, Mul(Pow(10, 12), Sym("yottaw")));
        c.put(UnitsPower.WATT, Mul(Pow(10, 24), Sym("yottaw")));
        c.put(UnitsPower.YOCTOWATT, Mul(Pow(10, 48), Sym("yottaw")));
        c.put(UnitsPower.ZEPTOWATT, Mul(Pow(10, 45), Sym("yottaw")));
        c.put(UnitsPower.ZETTAWATT, Mul(Int(1000), Sym("yottaw")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsPower, Conversion> createMapZEPTOWATT() {
        EnumMap<UnitsPower, Conversion> c =
            new EnumMap<UnitsPower, Conversion>(UnitsPower.class);
        c.put(UnitsPower.ATTOWATT, Mul(Rat(Int(1), Int(1000)), Sym("zeptow")));
        c.put(UnitsPower.CENTIWATT, Mul(Rat(Int(1), Pow(10, 19)), Sym("zeptow")));
        c.put(UnitsPower.DECAWATT, Mul(Rat(Int(1), Pow(10, 22)), Sym("zeptow")));
        c.put(UnitsPower.DECIWATT, Mul(Rat(Int(1), Pow(10, 20)), Sym("zeptow")));
        c.put(UnitsPower.EXAWATT, Mul(Rat(Int(1), Pow(10, 39)), Sym("zeptow")));
        c.put(UnitsPower.FEMTOWATT, Mul(Rat(Int(1), Pow(10, 6)), Sym("zeptow")));
        c.put(UnitsPower.GIGAWATT, Mul(Rat(Int(1), Pow(10, 30)), Sym("zeptow")));
        c.put(UnitsPower.HECTOWATT, Mul(Rat(Int(1), Pow(10, 23)), Sym("zeptow")));
        c.put(UnitsPower.KILOWATT, Mul(Rat(Int(1), Pow(10, 24)), Sym("zeptow")));
        c.put(UnitsPower.MEGAWATT, Mul(Rat(Int(1), Pow(10, 27)), Sym("zeptow")));
        c.put(UnitsPower.MICROWATT, Mul(Rat(Int(1), Pow(10, 15)), Sym("zeptow")));
        c.put(UnitsPower.MILLIWATT, Mul(Rat(Int(1), Pow(10, 18)), Sym("zeptow")));
        c.put(UnitsPower.NANOWATT, Mul(Rat(Int(1), Pow(10, 12)), Sym("zeptow")));
        c.put(UnitsPower.PETAWATT, Mul(Rat(Int(1), Pow(10, 36)), Sym("zeptow")));
        c.put(UnitsPower.PICOWATT, Mul(Rat(Int(1), Pow(10, 9)), Sym("zeptow")));
        c.put(UnitsPower.TERAWATT, Mul(Rat(Int(1), Pow(10, 33)), Sym("zeptow")));
        c.put(UnitsPower.WATT, Mul(Rat(Int(1), Pow(10, 21)), Sym("zeptow")));
        c.put(UnitsPower.YOCTOWATT, Mul(Int(1000), Sym("zeptow")));
        c.put(UnitsPower.YOTTAWATT, Mul(Rat(Int(1), Pow(10, 45)), Sym("zeptow")));
        c.put(UnitsPower.ZETTAWATT, Mul(Rat(Int(1), Pow(10, 42)), Sym("zeptow")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsPower, Conversion> createMapZETTAWATT() {
        EnumMap<UnitsPower, Conversion> c =
            new EnumMap<UnitsPower, Conversion>(UnitsPower.class);
        c.put(UnitsPower.ATTOWATT, Mul(Pow(10, 39), Sym("zettaw")));
        c.put(UnitsPower.CENTIWATT, Mul(Pow(10, 23), Sym("zettaw")));
        c.put(UnitsPower.DECAWATT, Mul(Pow(10, 20), Sym("zettaw")));
        c.put(UnitsPower.DECIWATT, Mul(Pow(10, 22), Sym("zettaw")));
        c.put(UnitsPower.EXAWATT, Mul(Int(1000), Sym("zettaw")));
        c.put(UnitsPower.FEMTOWATT, Mul(Pow(10, 36), Sym("zettaw")));
        c.put(UnitsPower.GIGAWATT, Mul(Pow(10, 12), Sym("zettaw")));
        c.put(UnitsPower.HECTOWATT, Mul(Pow(10, 19), Sym("zettaw")));
        c.put(UnitsPower.KILOWATT, Mul(Pow(10, 18), Sym("zettaw")));
        c.put(UnitsPower.MEGAWATT, Mul(Pow(10, 15), Sym("zettaw")));
        c.put(UnitsPower.MICROWATT, Mul(Pow(10, 27), Sym("zettaw")));
        c.put(UnitsPower.MILLIWATT, Mul(Pow(10, 24), Sym("zettaw")));
        c.put(UnitsPower.NANOWATT, Mul(Pow(10, 30), Sym("zettaw")));
        c.put(UnitsPower.PETAWATT, Mul(Pow(10, 6), Sym("zettaw")));
        c.put(UnitsPower.PICOWATT, Mul(Pow(10, 33), Sym("zettaw")));
        c.put(UnitsPower.TERAWATT, Mul(Pow(10, 9), Sym("zettaw")));
        c.put(UnitsPower.WATT, Mul(Pow(10, 21), Sym("zettaw")));
        c.put(UnitsPower.YOCTOWATT, Mul(Pow(10, 45), Sym("zettaw")));
        c.put(UnitsPower.YOTTAWATT, Mul(Rat(Int(1), Int(1000)), Sym("zettaw")));
        c.put(UnitsPower.ZEPTOWATT, Mul(Pow(10, 42), Sym("zettaw")));
        return Collections.unmodifiableMap(c);
    }

    private static final Map<UnitsPower, Map<UnitsPower, Conversion>> conversions;
    static {

        Map<UnitsPower, Map<UnitsPower, Conversion>> c
            = new EnumMap<UnitsPower, Map<UnitsPower, Conversion>>(UnitsPower.class);

        c.put(UnitsPower.ATTOWATT, createMapATTOWATT());
        c.put(UnitsPower.CENTIWATT, createMapCENTIWATT());
        c.put(UnitsPower.DECAWATT, createMapDECAWATT());
        c.put(UnitsPower.DECIWATT, createMapDECIWATT());
        c.put(UnitsPower.EXAWATT, createMapEXAWATT());
        c.put(UnitsPower.FEMTOWATT, createMapFEMTOWATT());
        c.put(UnitsPower.GIGAWATT, createMapGIGAWATT());
        c.put(UnitsPower.HECTOWATT, createMapHECTOWATT());
        c.put(UnitsPower.KILOWATT, createMapKILOWATT());
        c.put(UnitsPower.MEGAWATT, createMapMEGAWATT());
        c.put(UnitsPower.MICROWATT, createMapMICROWATT());
        c.put(UnitsPower.MILLIWATT, createMapMILLIWATT());
        c.put(UnitsPower.NANOWATT, createMapNANOWATT());
        c.put(UnitsPower.PETAWATT, createMapPETAWATT());
        c.put(UnitsPower.PICOWATT, createMapPICOWATT());
        c.put(UnitsPower.TERAWATT, createMapTERAWATT());
        c.put(UnitsPower.WATT, createMapWATT());
        c.put(UnitsPower.YOCTOWATT, createMapYOCTOWATT());
        c.put(UnitsPower.YOTTAWATT, createMapYOTTAWATT());
        c.put(UnitsPower.ZEPTOWATT, createMapZEPTOWATT());
        c.put(UnitsPower.ZETTAWATT, createMapZETTAWATT());
        conversions = Collections.unmodifiableMap(c);
    }

    private static final Map<UnitsPower, String> SYMBOLS;
    static {
        Map<UnitsPower, String> s = new HashMap<UnitsPower, String>();
        s.put(UnitsPower.ATTOWATT, "aW");
        s.put(UnitsPower.CENTIWATT, "cW");
        s.put(UnitsPower.DECAWATT, "daW");
        s.put(UnitsPower.DECIWATT, "dW");
        s.put(UnitsPower.EXAWATT, "EW");
        s.put(UnitsPower.FEMTOWATT, "fW");
        s.put(UnitsPower.GIGAWATT, "GW");
        s.put(UnitsPower.HECTOWATT, "hW");
        s.put(UnitsPower.KILOWATT, "kW");
        s.put(UnitsPower.MEGAWATT, "MW");
        s.put(UnitsPower.MICROWATT, "µW");
        s.put(UnitsPower.MILLIWATT, "mW");
        s.put(UnitsPower.NANOWATT, "nW");
        s.put(UnitsPower.PETAWATT, "PW");
        s.put(UnitsPower.PICOWATT, "pW");
        s.put(UnitsPower.TERAWATT, "TW");
        s.put(UnitsPower.WATT, "W");
        s.put(UnitsPower.YOCTOWATT, "yW");
        s.put(UnitsPower.YOTTAWATT, "YW");
        s.put(UnitsPower.ZEPTOWATT, "zW");
        s.put(UnitsPower.ZETTAWATT, "ZW");
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
   public PowerI(Power value, Unit<ome.units.quantity.Power> ul) throws BigResult {
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
    public PowerI(Power value, String target) throws BigResult {
       String source = value.getUnit().toString();
       if (target.equals(source)) {
           setValue(value.getValue());
           setUnit(value.getUnit());
        } else {
            UnitsPower targetUnit = UnitsPower.valueOf(target);
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
    public PowerI(Power value, UnitsPower target) throws BigResult {
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

