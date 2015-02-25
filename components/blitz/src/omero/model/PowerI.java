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

    private static final Map<UnitsPower, Map<UnitsPower, Conversion>> conversions;
    static {

        EnumMap<UnitsPower, EnumMap<UnitsPower, Conversion>> c
            = new EnumMap<UnitsPower, EnumMap<UnitsPower, Conversion>>(UnitsPower.class);

        for (UnitsPower e : UnitsPower.values()) {
            c.put(e, new EnumMap<UnitsPower, Conversion>(UnitsPower.class));
        }

        c.get(UnitsPower.ATTOWATT).put(UnitsPower.CENTIWATT, Mul(Pow(10, 16), Sym("attow")));
        c.get(UnitsPower.ATTOWATT).put(UnitsPower.DECAWATT, Mul(Pow(10, 19), Sym("attow")));
        c.get(UnitsPower.ATTOWATT).put(UnitsPower.DECIWATT, Mul(Pow(10, 17), Sym("attow")));
        c.get(UnitsPower.ATTOWATT).put(UnitsPower.EXAWATT, Mul(Pow(10, 36), Sym("attow")));
        c.get(UnitsPower.ATTOWATT).put(UnitsPower.FEMTOWATT, Mul(Int(1000), Sym("attow")));
        c.get(UnitsPower.ATTOWATT).put(UnitsPower.GIGAWATT, Mul(Pow(10, 27), Sym("attow")));
        c.get(UnitsPower.ATTOWATT).put(UnitsPower.HECTOWATT, Mul(Pow(10, 20), Sym("attow")));
        c.get(UnitsPower.ATTOWATT).put(UnitsPower.KILOWATT, Mul(Pow(10, 21), Sym("attow")));
        c.get(UnitsPower.ATTOWATT).put(UnitsPower.MEGAWATT, Mul(Pow(10, 24), Sym("attow")));
        c.get(UnitsPower.ATTOWATT).put(UnitsPower.MICROWATT, Mul(Pow(10, 12), Sym("attow")));
        c.get(UnitsPower.ATTOWATT).put(UnitsPower.MILLIWATT, Mul(Pow(10, 15), Sym("attow")));
        c.get(UnitsPower.ATTOWATT).put(UnitsPower.NANOWATT, Mul(Pow(10, 9), Sym("attow")));
        c.get(UnitsPower.ATTOWATT).put(UnitsPower.PETAWATT, Mul(Pow(10, 33), Sym("attow")));
        c.get(UnitsPower.ATTOWATT).put(UnitsPower.PICOWATT, Mul(Pow(10, 6), Sym("attow")));
        c.get(UnitsPower.ATTOWATT).put(UnitsPower.TERAWATT, Mul(Pow(10, 30), Sym("attow")));
        c.get(UnitsPower.ATTOWATT).put(UnitsPower.WATT, Mul(Pow(10, 18), Sym("attow")));
        c.get(UnitsPower.ATTOWATT).put(UnitsPower.YOCTOWATT, Mul(Rat(Int(1), Pow(10, 6)), Sym("attow")));
        c.get(UnitsPower.ATTOWATT).put(UnitsPower.YOTTAWATT, Mul(Pow(10, 42), Sym("attow")));
        c.get(UnitsPower.ATTOWATT).put(UnitsPower.ZEPTOWATT, Mul(Rat(Int(1), Int(1000)), Sym("attow")));
        c.get(UnitsPower.ATTOWATT).put(UnitsPower.ZETTAWATT, Mul(Pow(10, 39), Sym("attow")));
        c.get(UnitsPower.CENTIWATT).put(UnitsPower.ATTOWATT, Mul(Rat(Int(1), Pow(10, 16)), Sym("centiw")));
        c.get(UnitsPower.CENTIWATT).put(UnitsPower.DECAWATT, Mul(Int(1000), Sym("centiw")));
        c.get(UnitsPower.CENTIWATT).put(UnitsPower.DECIWATT, Mul(Int(10), Sym("centiw")));
        c.get(UnitsPower.CENTIWATT).put(UnitsPower.EXAWATT, Mul(Pow(10, 20), Sym("centiw")));
        c.get(UnitsPower.CENTIWATT).put(UnitsPower.FEMTOWATT, Mul(Rat(Int(1), Pow(10, 13)), Sym("centiw")));
        c.get(UnitsPower.CENTIWATT).put(UnitsPower.GIGAWATT, Mul(Pow(10, 11), Sym("centiw")));
        c.get(UnitsPower.CENTIWATT).put(UnitsPower.HECTOWATT, Mul(Pow(10, 4), Sym("centiw")));
        c.get(UnitsPower.CENTIWATT).put(UnitsPower.KILOWATT, Mul(Pow(10, 5), Sym("centiw")));
        c.get(UnitsPower.CENTIWATT).put(UnitsPower.MEGAWATT, Mul(Pow(10, 8), Sym("centiw")));
        c.get(UnitsPower.CENTIWATT).put(UnitsPower.MICROWATT, Mul(Rat(Int(1), Pow(10, 4)), Sym("centiw")));
        c.get(UnitsPower.CENTIWATT).put(UnitsPower.MILLIWATT, Mul(Rat(Int(1), Int(10)), Sym("centiw")));
        c.get(UnitsPower.CENTIWATT).put(UnitsPower.NANOWATT, Mul(Rat(Int(1), Pow(10, 7)), Sym("centiw")));
        c.get(UnitsPower.CENTIWATT).put(UnitsPower.PETAWATT, Mul(Pow(10, 17), Sym("centiw")));
        c.get(UnitsPower.CENTIWATT).put(UnitsPower.PICOWATT, Mul(Rat(Int(1), Pow(10, 10)), Sym("centiw")));
        c.get(UnitsPower.CENTIWATT).put(UnitsPower.TERAWATT, Mul(Pow(10, 14), Sym("centiw")));
        c.get(UnitsPower.CENTIWATT).put(UnitsPower.WATT, Mul(Int(100), Sym("centiw")));
        c.get(UnitsPower.CENTIWATT).put(UnitsPower.YOCTOWATT, Mul(Rat(Int(1), Pow(10, 22)), Sym("centiw")));
        c.get(UnitsPower.CENTIWATT).put(UnitsPower.YOTTAWATT, Mul(Pow(10, 26), Sym("centiw")));
        c.get(UnitsPower.CENTIWATT).put(UnitsPower.ZEPTOWATT, Mul(Rat(Int(1), Pow(10, 19)), Sym("centiw")));
        c.get(UnitsPower.CENTIWATT).put(UnitsPower.ZETTAWATT, Mul(Pow(10, 23), Sym("centiw")));
        c.get(UnitsPower.DECAWATT).put(UnitsPower.ATTOWATT, Mul(Rat(Int(1), Pow(10, 19)), Sym("decaw")));
        c.get(UnitsPower.DECAWATT).put(UnitsPower.CENTIWATT, Mul(Rat(Int(1), Int(1000)), Sym("decaw")));
        c.get(UnitsPower.DECAWATT).put(UnitsPower.DECIWATT, Mul(Rat(Int(1), Int(100)), Sym("decaw")));
        c.get(UnitsPower.DECAWATT).put(UnitsPower.EXAWATT, Mul(Pow(10, 17), Sym("decaw")));
        c.get(UnitsPower.DECAWATT).put(UnitsPower.FEMTOWATT, Mul(Rat(Int(1), Pow(10, 16)), Sym("decaw")));
        c.get(UnitsPower.DECAWATT).put(UnitsPower.GIGAWATT, Mul(Pow(10, 8), Sym("decaw")));
        c.get(UnitsPower.DECAWATT).put(UnitsPower.HECTOWATT, Mul(Int(10), Sym("decaw")));
        c.get(UnitsPower.DECAWATT).put(UnitsPower.KILOWATT, Mul(Int(100), Sym("decaw")));
        c.get(UnitsPower.DECAWATT).put(UnitsPower.MEGAWATT, Mul(Pow(10, 5), Sym("decaw")));
        c.get(UnitsPower.DECAWATT).put(UnitsPower.MICROWATT, Mul(Rat(Int(1), Pow(10, 7)), Sym("decaw")));
        c.get(UnitsPower.DECAWATT).put(UnitsPower.MILLIWATT, Mul(Rat(Int(1), Pow(10, 4)), Sym("decaw")));
        c.get(UnitsPower.DECAWATT).put(UnitsPower.NANOWATT, Mul(Rat(Int(1), Pow(10, 10)), Sym("decaw")));
        c.get(UnitsPower.DECAWATT).put(UnitsPower.PETAWATT, Mul(Pow(10, 14), Sym("decaw")));
        c.get(UnitsPower.DECAWATT).put(UnitsPower.PICOWATT, Mul(Rat(Int(1), Pow(10, 13)), Sym("decaw")));
        c.get(UnitsPower.DECAWATT).put(UnitsPower.TERAWATT, Mul(Pow(10, 11), Sym("decaw")));
        c.get(UnitsPower.DECAWATT).put(UnitsPower.WATT, Mul(Rat(Int(1), Int(10)), Sym("decaw")));
        c.get(UnitsPower.DECAWATT).put(UnitsPower.YOCTOWATT, Mul(Rat(Int(1), Pow(10, 25)), Sym("decaw")));
        c.get(UnitsPower.DECAWATT).put(UnitsPower.YOTTAWATT, Mul(Pow(10, 23), Sym("decaw")));
        c.get(UnitsPower.DECAWATT).put(UnitsPower.ZEPTOWATT, Mul(Rat(Int(1), Pow(10, 22)), Sym("decaw")));
        c.get(UnitsPower.DECAWATT).put(UnitsPower.ZETTAWATT, Mul(Pow(10, 20), Sym("decaw")));
        c.get(UnitsPower.DECIWATT).put(UnitsPower.ATTOWATT, Mul(Rat(Int(1), Pow(10, 17)), Sym("deciw")));
        c.get(UnitsPower.DECIWATT).put(UnitsPower.CENTIWATT, Mul(Rat(Int(1), Int(10)), Sym("deciw")));
        c.get(UnitsPower.DECIWATT).put(UnitsPower.DECAWATT, Mul(Int(100), Sym("deciw")));
        c.get(UnitsPower.DECIWATT).put(UnitsPower.EXAWATT, Mul(Pow(10, 19), Sym("deciw")));
        c.get(UnitsPower.DECIWATT).put(UnitsPower.FEMTOWATT, Mul(Rat(Int(1), Pow(10, 14)), Sym("deciw")));
        c.get(UnitsPower.DECIWATT).put(UnitsPower.GIGAWATT, Mul(Pow(10, 10), Sym("deciw")));
        c.get(UnitsPower.DECIWATT).put(UnitsPower.HECTOWATT, Mul(Int(1000), Sym("deciw")));
        c.get(UnitsPower.DECIWATT).put(UnitsPower.KILOWATT, Mul(Pow(10, 4), Sym("deciw")));
        c.get(UnitsPower.DECIWATT).put(UnitsPower.MEGAWATT, Mul(Pow(10, 7), Sym("deciw")));
        c.get(UnitsPower.DECIWATT).put(UnitsPower.MICROWATT, Mul(Rat(Int(1), Pow(10, 5)), Sym("deciw")));
        c.get(UnitsPower.DECIWATT).put(UnitsPower.MILLIWATT, Mul(Rat(Int(1), Int(100)), Sym("deciw")));
        c.get(UnitsPower.DECIWATT).put(UnitsPower.NANOWATT, Mul(Rat(Int(1), Pow(10, 8)), Sym("deciw")));
        c.get(UnitsPower.DECIWATT).put(UnitsPower.PETAWATT, Mul(Pow(10, 16), Sym("deciw")));
        c.get(UnitsPower.DECIWATT).put(UnitsPower.PICOWATT, Mul(Rat(Int(1), Pow(10, 11)), Sym("deciw")));
        c.get(UnitsPower.DECIWATT).put(UnitsPower.TERAWATT, Mul(Pow(10, 13), Sym("deciw")));
        c.get(UnitsPower.DECIWATT).put(UnitsPower.WATT, Mul(Int(10), Sym("deciw")));
        c.get(UnitsPower.DECIWATT).put(UnitsPower.YOCTOWATT, Mul(Rat(Int(1), Pow(10, 23)), Sym("deciw")));
        c.get(UnitsPower.DECIWATT).put(UnitsPower.YOTTAWATT, Mul(Pow(10, 25), Sym("deciw")));
        c.get(UnitsPower.DECIWATT).put(UnitsPower.ZEPTOWATT, Mul(Rat(Int(1), Pow(10, 20)), Sym("deciw")));
        c.get(UnitsPower.DECIWATT).put(UnitsPower.ZETTAWATT, Mul(Pow(10, 22), Sym("deciw")));
        c.get(UnitsPower.EXAWATT).put(UnitsPower.ATTOWATT, Mul(Rat(Int(1), Pow(10, 36)), Sym("exaw")));
        c.get(UnitsPower.EXAWATT).put(UnitsPower.CENTIWATT, Mul(Rat(Int(1), Pow(10, 20)), Sym("exaw")));
        c.get(UnitsPower.EXAWATT).put(UnitsPower.DECAWATT, Mul(Rat(Int(1), Pow(10, 17)), Sym("exaw")));
        c.get(UnitsPower.EXAWATT).put(UnitsPower.DECIWATT, Mul(Rat(Int(1), Pow(10, 19)), Sym("exaw")));
        c.get(UnitsPower.EXAWATT).put(UnitsPower.FEMTOWATT, Mul(Rat(Int(1), Pow(10, 33)), Sym("exaw")));
        c.get(UnitsPower.EXAWATT).put(UnitsPower.GIGAWATT, Mul(Rat(Int(1), Pow(10, 9)), Sym("exaw")));
        c.get(UnitsPower.EXAWATT).put(UnitsPower.HECTOWATT, Mul(Rat(Int(1), Pow(10, 16)), Sym("exaw")));
        c.get(UnitsPower.EXAWATT).put(UnitsPower.KILOWATT, Mul(Rat(Int(1), Pow(10, 15)), Sym("exaw")));
        c.get(UnitsPower.EXAWATT).put(UnitsPower.MEGAWATT, Mul(Rat(Int(1), Pow(10, 12)), Sym("exaw")));
        c.get(UnitsPower.EXAWATT).put(UnitsPower.MICROWATT, Mul(Rat(Int(1), Pow(10, 24)), Sym("exaw")));
        c.get(UnitsPower.EXAWATT).put(UnitsPower.MILLIWATT, Mul(Rat(Int(1), Pow(10, 21)), Sym("exaw")));
        c.get(UnitsPower.EXAWATT).put(UnitsPower.NANOWATT, Mul(Rat(Int(1), Pow(10, 27)), Sym("exaw")));
        c.get(UnitsPower.EXAWATT).put(UnitsPower.PETAWATT, Mul(Rat(Int(1), Int(1000)), Sym("exaw")));
        c.get(UnitsPower.EXAWATT).put(UnitsPower.PICOWATT, Mul(Rat(Int(1), Pow(10, 30)), Sym("exaw")));
        c.get(UnitsPower.EXAWATT).put(UnitsPower.TERAWATT, Mul(Rat(Int(1), Pow(10, 6)), Sym("exaw")));
        c.get(UnitsPower.EXAWATT).put(UnitsPower.WATT, Mul(Rat(Int(1), Pow(10, 18)), Sym("exaw")));
        c.get(UnitsPower.EXAWATT).put(UnitsPower.YOCTOWATT, Mul(Rat(Int(1), Pow(10, 42)), Sym("exaw")));
        c.get(UnitsPower.EXAWATT).put(UnitsPower.YOTTAWATT, Mul(Pow(10, 6), Sym("exaw")));
        c.get(UnitsPower.EXAWATT).put(UnitsPower.ZEPTOWATT, Mul(Rat(Int(1), Pow(10, 39)), Sym("exaw")));
        c.get(UnitsPower.EXAWATT).put(UnitsPower.ZETTAWATT, Mul(Int(1000), Sym("exaw")));
        c.get(UnitsPower.FEMTOWATT).put(UnitsPower.ATTOWATT, Mul(Rat(Int(1), Int(1000)), Sym("femtow")));
        c.get(UnitsPower.FEMTOWATT).put(UnitsPower.CENTIWATT, Mul(Pow(10, 13), Sym("femtow")));
        c.get(UnitsPower.FEMTOWATT).put(UnitsPower.DECAWATT, Mul(Pow(10, 16), Sym("femtow")));
        c.get(UnitsPower.FEMTOWATT).put(UnitsPower.DECIWATT, Mul(Pow(10, 14), Sym("femtow")));
        c.get(UnitsPower.FEMTOWATT).put(UnitsPower.EXAWATT, Mul(Pow(10, 33), Sym("femtow")));
        c.get(UnitsPower.FEMTOWATT).put(UnitsPower.GIGAWATT, Mul(Pow(10, 24), Sym("femtow")));
        c.get(UnitsPower.FEMTOWATT).put(UnitsPower.HECTOWATT, Mul(Pow(10, 17), Sym("femtow")));
        c.get(UnitsPower.FEMTOWATT).put(UnitsPower.KILOWATT, Mul(Pow(10, 18), Sym("femtow")));
        c.get(UnitsPower.FEMTOWATT).put(UnitsPower.MEGAWATT, Mul(Pow(10, 21), Sym("femtow")));
        c.get(UnitsPower.FEMTOWATT).put(UnitsPower.MICROWATT, Mul(Pow(10, 9), Sym("femtow")));
        c.get(UnitsPower.FEMTOWATT).put(UnitsPower.MILLIWATT, Mul(Pow(10, 12), Sym("femtow")));
        c.get(UnitsPower.FEMTOWATT).put(UnitsPower.NANOWATT, Mul(Pow(10, 6), Sym("femtow")));
        c.get(UnitsPower.FEMTOWATT).put(UnitsPower.PETAWATT, Mul(Pow(10, 30), Sym("femtow")));
        c.get(UnitsPower.FEMTOWATT).put(UnitsPower.PICOWATT, Mul(Int(1000), Sym("femtow")));
        c.get(UnitsPower.FEMTOWATT).put(UnitsPower.TERAWATT, Mul(Pow(10, 27), Sym("femtow")));
        c.get(UnitsPower.FEMTOWATT).put(UnitsPower.WATT, Mul(Pow(10, 15), Sym("femtow")));
        c.get(UnitsPower.FEMTOWATT).put(UnitsPower.YOCTOWATT, Mul(Rat(Int(1), Pow(10, 9)), Sym("femtow")));
        c.get(UnitsPower.FEMTOWATT).put(UnitsPower.YOTTAWATT, Mul(Pow(10, 39), Sym("femtow")));
        c.get(UnitsPower.FEMTOWATT).put(UnitsPower.ZEPTOWATT, Mul(Rat(Int(1), Pow(10, 6)), Sym("femtow")));
        c.get(UnitsPower.FEMTOWATT).put(UnitsPower.ZETTAWATT, Mul(Pow(10, 36), Sym("femtow")));
        c.get(UnitsPower.GIGAWATT).put(UnitsPower.ATTOWATT, Mul(Rat(Int(1), Pow(10, 27)), Sym("gigaw")));
        c.get(UnitsPower.GIGAWATT).put(UnitsPower.CENTIWATT, Mul(Rat(Int(1), Pow(10, 11)), Sym("gigaw")));
        c.get(UnitsPower.GIGAWATT).put(UnitsPower.DECAWATT, Mul(Rat(Int(1), Pow(10, 8)), Sym("gigaw")));
        c.get(UnitsPower.GIGAWATT).put(UnitsPower.DECIWATT, Mul(Rat(Int(1), Pow(10, 10)), Sym("gigaw")));
        c.get(UnitsPower.GIGAWATT).put(UnitsPower.EXAWATT, Mul(Pow(10, 9), Sym("gigaw")));
        c.get(UnitsPower.GIGAWATT).put(UnitsPower.FEMTOWATT, Mul(Rat(Int(1), Pow(10, 24)), Sym("gigaw")));
        c.get(UnitsPower.GIGAWATT).put(UnitsPower.HECTOWATT, Mul(Rat(Int(1), Pow(10, 7)), Sym("gigaw")));
        c.get(UnitsPower.GIGAWATT).put(UnitsPower.KILOWATT, Mul(Rat(Int(1), Pow(10, 6)), Sym("gigaw")));
        c.get(UnitsPower.GIGAWATT).put(UnitsPower.MEGAWATT, Mul(Rat(Int(1), Int(1000)), Sym("gigaw")));
        c.get(UnitsPower.GIGAWATT).put(UnitsPower.MICROWATT, Mul(Rat(Int(1), Pow(10, 15)), Sym("gigaw")));
        c.get(UnitsPower.GIGAWATT).put(UnitsPower.MILLIWATT, Mul(Rat(Int(1), Pow(10, 12)), Sym("gigaw")));
        c.get(UnitsPower.GIGAWATT).put(UnitsPower.NANOWATT, Mul(Rat(Int(1), Pow(10, 18)), Sym("gigaw")));
        c.get(UnitsPower.GIGAWATT).put(UnitsPower.PETAWATT, Mul(Pow(10, 6), Sym("gigaw")));
        c.get(UnitsPower.GIGAWATT).put(UnitsPower.PICOWATT, Mul(Rat(Int(1), Pow(10, 21)), Sym("gigaw")));
        c.get(UnitsPower.GIGAWATT).put(UnitsPower.TERAWATT, Mul(Int(1000), Sym("gigaw")));
        c.get(UnitsPower.GIGAWATT).put(UnitsPower.WATT, Mul(Rat(Int(1), Pow(10, 9)), Sym("gigaw")));
        c.get(UnitsPower.GIGAWATT).put(UnitsPower.YOCTOWATT, Mul(Rat(Int(1), Pow(10, 33)), Sym("gigaw")));
        c.get(UnitsPower.GIGAWATT).put(UnitsPower.YOTTAWATT, Mul(Pow(10, 15), Sym("gigaw")));
        c.get(UnitsPower.GIGAWATT).put(UnitsPower.ZEPTOWATT, Mul(Rat(Int(1), Pow(10, 30)), Sym("gigaw")));
        c.get(UnitsPower.GIGAWATT).put(UnitsPower.ZETTAWATT, Mul(Pow(10, 12), Sym("gigaw")));
        c.get(UnitsPower.HECTOWATT).put(UnitsPower.ATTOWATT, Mul(Rat(Int(1), Pow(10, 20)), Sym("hectow")));
        c.get(UnitsPower.HECTOWATT).put(UnitsPower.CENTIWATT, Mul(Rat(Int(1), Pow(10, 4)), Sym("hectow")));
        c.get(UnitsPower.HECTOWATT).put(UnitsPower.DECAWATT, Mul(Rat(Int(1), Int(10)), Sym("hectow")));
        c.get(UnitsPower.HECTOWATT).put(UnitsPower.DECIWATT, Mul(Rat(Int(1), Int(1000)), Sym("hectow")));
        c.get(UnitsPower.HECTOWATT).put(UnitsPower.EXAWATT, Mul(Pow(10, 16), Sym("hectow")));
        c.get(UnitsPower.HECTOWATT).put(UnitsPower.FEMTOWATT, Mul(Rat(Int(1), Pow(10, 17)), Sym("hectow")));
        c.get(UnitsPower.HECTOWATT).put(UnitsPower.GIGAWATT, Mul(Pow(10, 7), Sym("hectow")));
        c.get(UnitsPower.HECTOWATT).put(UnitsPower.KILOWATT, Mul(Int(10), Sym("hectow")));
        c.get(UnitsPower.HECTOWATT).put(UnitsPower.MEGAWATT, Mul(Pow(10, 4), Sym("hectow")));
        c.get(UnitsPower.HECTOWATT).put(UnitsPower.MICROWATT, Mul(Rat(Int(1), Pow(10, 8)), Sym("hectow")));
        c.get(UnitsPower.HECTOWATT).put(UnitsPower.MILLIWATT, Mul(Rat(Int(1), Pow(10, 5)), Sym("hectow")));
        c.get(UnitsPower.HECTOWATT).put(UnitsPower.NANOWATT, Mul(Rat(Int(1), Pow(10, 11)), Sym("hectow")));
        c.get(UnitsPower.HECTOWATT).put(UnitsPower.PETAWATT, Mul(Pow(10, 13), Sym("hectow")));
        c.get(UnitsPower.HECTOWATT).put(UnitsPower.PICOWATT, Mul(Rat(Int(1), Pow(10, 14)), Sym("hectow")));
        c.get(UnitsPower.HECTOWATT).put(UnitsPower.TERAWATT, Mul(Pow(10, 10), Sym("hectow")));
        c.get(UnitsPower.HECTOWATT).put(UnitsPower.WATT, Mul(Rat(Int(1), Int(100)), Sym("hectow")));
        c.get(UnitsPower.HECTOWATT).put(UnitsPower.YOCTOWATT, Mul(Rat(Int(1), Pow(10, 26)), Sym("hectow")));
        c.get(UnitsPower.HECTOWATT).put(UnitsPower.YOTTAWATT, Mul(Pow(10, 22), Sym("hectow")));
        c.get(UnitsPower.HECTOWATT).put(UnitsPower.ZEPTOWATT, Mul(Rat(Int(1), Pow(10, 23)), Sym("hectow")));
        c.get(UnitsPower.HECTOWATT).put(UnitsPower.ZETTAWATT, Mul(Pow(10, 19), Sym("hectow")));
        c.get(UnitsPower.KILOWATT).put(UnitsPower.ATTOWATT, Mul(Rat(Int(1), Pow(10, 21)), Sym("kilow")));
        c.get(UnitsPower.KILOWATT).put(UnitsPower.CENTIWATT, Mul(Rat(Int(1), Pow(10, 5)), Sym("kilow")));
        c.get(UnitsPower.KILOWATT).put(UnitsPower.DECAWATT, Mul(Rat(Int(1), Int(100)), Sym("kilow")));
        c.get(UnitsPower.KILOWATT).put(UnitsPower.DECIWATT, Mul(Rat(Int(1), Pow(10, 4)), Sym("kilow")));
        c.get(UnitsPower.KILOWATT).put(UnitsPower.EXAWATT, Mul(Pow(10, 15), Sym("kilow")));
        c.get(UnitsPower.KILOWATT).put(UnitsPower.FEMTOWATT, Mul(Rat(Int(1), Pow(10, 18)), Sym("kilow")));
        c.get(UnitsPower.KILOWATT).put(UnitsPower.GIGAWATT, Mul(Pow(10, 6), Sym("kilow")));
        c.get(UnitsPower.KILOWATT).put(UnitsPower.HECTOWATT, Mul(Rat(Int(1), Int(10)), Sym("kilow")));
        c.get(UnitsPower.KILOWATT).put(UnitsPower.MEGAWATT, Mul(Int(1000), Sym("kilow")));
        c.get(UnitsPower.KILOWATT).put(UnitsPower.MICROWATT, Mul(Rat(Int(1), Pow(10, 9)), Sym("kilow")));
        c.get(UnitsPower.KILOWATT).put(UnitsPower.MILLIWATT, Mul(Rat(Int(1), Pow(10, 6)), Sym("kilow")));
        c.get(UnitsPower.KILOWATT).put(UnitsPower.NANOWATT, Mul(Rat(Int(1), Pow(10, 12)), Sym("kilow")));
        c.get(UnitsPower.KILOWATT).put(UnitsPower.PETAWATT, Mul(Pow(10, 12), Sym("kilow")));
        c.get(UnitsPower.KILOWATT).put(UnitsPower.PICOWATT, Mul(Rat(Int(1), Pow(10, 15)), Sym("kilow")));
        c.get(UnitsPower.KILOWATT).put(UnitsPower.TERAWATT, Mul(Pow(10, 9), Sym("kilow")));
        c.get(UnitsPower.KILOWATT).put(UnitsPower.WATT, Mul(Rat(Int(1), Int(1000)), Sym("kilow")));
        c.get(UnitsPower.KILOWATT).put(UnitsPower.YOCTOWATT, Mul(Rat(Int(1), Pow(10, 27)), Sym("kilow")));
        c.get(UnitsPower.KILOWATT).put(UnitsPower.YOTTAWATT, Mul(Pow(10, 21), Sym("kilow")));
        c.get(UnitsPower.KILOWATT).put(UnitsPower.ZEPTOWATT, Mul(Rat(Int(1), Pow(10, 24)), Sym("kilow")));
        c.get(UnitsPower.KILOWATT).put(UnitsPower.ZETTAWATT, Mul(Pow(10, 18), Sym("kilow")));
        c.get(UnitsPower.MEGAWATT).put(UnitsPower.ATTOWATT, Mul(Rat(Int(1), Pow(10, 24)), Sym("megaw")));
        c.get(UnitsPower.MEGAWATT).put(UnitsPower.CENTIWATT, Mul(Rat(Int(1), Pow(10, 8)), Sym("megaw")));
        c.get(UnitsPower.MEGAWATT).put(UnitsPower.DECAWATT, Mul(Rat(Int(1), Pow(10, 5)), Sym("megaw")));
        c.get(UnitsPower.MEGAWATT).put(UnitsPower.DECIWATT, Mul(Rat(Int(1), Pow(10, 7)), Sym("megaw")));
        c.get(UnitsPower.MEGAWATT).put(UnitsPower.EXAWATT, Mul(Pow(10, 12), Sym("megaw")));
        c.get(UnitsPower.MEGAWATT).put(UnitsPower.FEMTOWATT, Mul(Rat(Int(1), Pow(10, 21)), Sym("megaw")));
        c.get(UnitsPower.MEGAWATT).put(UnitsPower.GIGAWATT, Mul(Int(1000), Sym("megaw")));
        c.get(UnitsPower.MEGAWATT).put(UnitsPower.HECTOWATT, Mul(Rat(Int(1), Pow(10, 4)), Sym("megaw")));
        c.get(UnitsPower.MEGAWATT).put(UnitsPower.KILOWATT, Mul(Rat(Int(1), Int(1000)), Sym("megaw")));
        c.get(UnitsPower.MEGAWATT).put(UnitsPower.MICROWATT, Mul(Rat(Int(1), Pow(10, 12)), Sym("megaw")));
        c.get(UnitsPower.MEGAWATT).put(UnitsPower.MILLIWATT, Mul(Rat(Int(1), Pow(10, 9)), Sym("megaw")));
        c.get(UnitsPower.MEGAWATT).put(UnitsPower.NANOWATT, Mul(Rat(Int(1), Pow(10, 15)), Sym("megaw")));
        c.get(UnitsPower.MEGAWATT).put(UnitsPower.PETAWATT, Mul(Pow(10, 9), Sym("megaw")));
        c.get(UnitsPower.MEGAWATT).put(UnitsPower.PICOWATT, Mul(Rat(Int(1), Pow(10, 18)), Sym("megaw")));
        c.get(UnitsPower.MEGAWATT).put(UnitsPower.TERAWATT, Mul(Pow(10, 6), Sym("megaw")));
        c.get(UnitsPower.MEGAWATT).put(UnitsPower.WATT, Mul(Rat(Int(1), Pow(10, 6)), Sym("megaw")));
        c.get(UnitsPower.MEGAWATT).put(UnitsPower.YOCTOWATT, Mul(Rat(Int(1), Pow(10, 30)), Sym("megaw")));
        c.get(UnitsPower.MEGAWATT).put(UnitsPower.YOTTAWATT, Mul(Pow(10, 18), Sym("megaw")));
        c.get(UnitsPower.MEGAWATT).put(UnitsPower.ZEPTOWATT, Mul(Rat(Int(1), Pow(10, 27)), Sym("megaw")));
        c.get(UnitsPower.MEGAWATT).put(UnitsPower.ZETTAWATT, Mul(Pow(10, 15), Sym("megaw")));
        c.get(UnitsPower.MICROWATT).put(UnitsPower.ATTOWATT, Mul(Rat(Int(1), Pow(10, 12)), Sym("microw")));
        c.get(UnitsPower.MICROWATT).put(UnitsPower.CENTIWATT, Mul(Pow(10, 4), Sym("microw")));
        c.get(UnitsPower.MICROWATT).put(UnitsPower.DECAWATT, Mul(Pow(10, 7), Sym("microw")));
        c.get(UnitsPower.MICROWATT).put(UnitsPower.DECIWATT, Mul(Pow(10, 5), Sym("microw")));
        c.get(UnitsPower.MICROWATT).put(UnitsPower.EXAWATT, Mul(Pow(10, 24), Sym("microw")));
        c.get(UnitsPower.MICROWATT).put(UnitsPower.FEMTOWATT, Mul(Rat(Int(1), Pow(10, 9)), Sym("microw")));
        c.get(UnitsPower.MICROWATT).put(UnitsPower.GIGAWATT, Mul(Pow(10, 15), Sym("microw")));
        c.get(UnitsPower.MICROWATT).put(UnitsPower.HECTOWATT, Mul(Pow(10, 8), Sym("microw")));
        c.get(UnitsPower.MICROWATT).put(UnitsPower.KILOWATT, Mul(Pow(10, 9), Sym("microw")));
        c.get(UnitsPower.MICROWATT).put(UnitsPower.MEGAWATT, Mul(Pow(10, 12), Sym("microw")));
        c.get(UnitsPower.MICROWATT).put(UnitsPower.MILLIWATT, Mul(Int(1000), Sym("microw")));
        c.get(UnitsPower.MICROWATT).put(UnitsPower.NANOWATT, Mul(Rat(Int(1), Int(1000)), Sym("microw")));
        c.get(UnitsPower.MICROWATT).put(UnitsPower.PETAWATT, Mul(Pow(10, 21), Sym("microw")));
        c.get(UnitsPower.MICROWATT).put(UnitsPower.PICOWATT, Mul(Rat(Int(1), Pow(10, 6)), Sym("microw")));
        c.get(UnitsPower.MICROWATT).put(UnitsPower.TERAWATT, Mul(Pow(10, 18), Sym("microw")));
        c.get(UnitsPower.MICROWATT).put(UnitsPower.WATT, Mul(Pow(10, 6), Sym("microw")));
        c.get(UnitsPower.MICROWATT).put(UnitsPower.YOCTOWATT, Mul(Rat(Int(1), Pow(10, 18)), Sym("microw")));
        c.get(UnitsPower.MICROWATT).put(UnitsPower.YOTTAWATT, Mul(Pow(10, 30), Sym("microw")));
        c.get(UnitsPower.MICROWATT).put(UnitsPower.ZEPTOWATT, Mul(Rat(Int(1), Pow(10, 15)), Sym("microw")));
        c.get(UnitsPower.MICROWATT).put(UnitsPower.ZETTAWATT, Mul(Pow(10, 27), Sym("microw")));
        c.get(UnitsPower.MILLIWATT).put(UnitsPower.ATTOWATT, Mul(Rat(Int(1), Pow(10, 15)), Sym("milliw")));
        c.get(UnitsPower.MILLIWATT).put(UnitsPower.CENTIWATT, Mul(Int(10), Sym("milliw")));
        c.get(UnitsPower.MILLIWATT).put(UnitsPower.DECAWATT, Mul(Pow(10, 4), Sym("milliw")));
        c.get(UnitsPower.MILLIWATT).put(UnitsPower.DECIWATT, Mul(Int(100), Sym("milliw")));
        c.get(UnitsPower.MILLIWATT).put(UnitsPower.EXAWATT, Mul(Pow(10, 21), Sym("milliw")));
        c.get(UnitsPower.MILLIWATT).put(UnitsPower.FEMTOWATT, Mul(Rat(Int(1), Pow(10, 12)), Sym("milliw")));
        c.get(UnitsPower.MILLIWATT).put(UnitsPower.GIGAWATT, Mul(Pow(10, 12), Sym("milliw")));
        c.get(UnitsPower.MILLIWATT).put(UnitsPower.HECTOWATT, Mul(Pow(10, 5), Sym("milliw")));
        c.get(UnitsPower.MILLIWATT).put(UnitsPower.KILOWATT, Mul(Pow(10, 6), Sym("milliw")));
        c.get(UnitsPower.MILLIWATT).put(UnitsPower.MEGAWATT, Mul(Pow(10, 9), Sym("milliw")));
        c.get(UnitsPower.MILLIWATT).put(UnitsPower.MICROWATT, Mul(Rat(Int(1), Int(1000)), Sym("milliw")));
        c.get(UnitsPower.MILLIWATT).put(UnitsPower.NANOWATT, Mul(Rat(Int(1), Pow(10, 6)), Sym("milliw")));
        c.get(UnitsPower.MILLIWATT).put(UnitsPower.PETAWATT, Mul(Pow(10, 18), Sym("milliw")));
        c.get(UnitsPower.MILLIWATT).put(UnitsPower.PICOWATT, Mul(Rat(Int(1), Pow(10, 9)), Sym("milliw")));
        c.get(UnitsPower.MILLIWATT).put(UnitsPower.TERAWATT, Mul(Pow(10, 15), Sym("milliw")));
        c.get(UnitsPower.MILLIWATT).put(UnitsPower.WATT, Mul(Int(1000), Sym("milliw")));
        c.get(UnitsPower.MILLIWATT).put(UnitsPower.YOCTOWATT, Mul(Rat(Int(1), Pow(10, 21)), Sym("milliw")));
        c.get(UnitsPower.MILLIWATT).put(UnitsPower.YOTTAWATT, Mul(Pow(10, 27), Sym("milliw")));
        c.get(UnitsPower.MILLIWATT).put(UnitsPower.ZEPTOWATT, Mul(Rat(Int(1), Pow(10, 18)), Sym("milliw")));
        c.get(UnitsPower.MILLIWATT).put(UnitsPower.ZETTAWATT, Mul(Pow(10, 24), Sym("milliw")));
        c.get(UnitsPower.NANOWATT).put(UnitsPower.ATTOWATT, Mul(Rat(Int(1), Pow(10, 9)), Sym("nanow")));
        c.get(UnitsPower.NANOWATT).put(UnitsPower.CENTIWATT, Mul(Pow(10, 7), Sym("nanow")));
        c.get(UnitsPower.NANOWATT).put(UnitsPower.DECAWATT, Mul(Pow(10, 10), Sym("nanow")));
        c.get(UnitsPower.NANOWATT).put(UnitsPower.DECIWATT, Mul(Pow(10, 8), Sym("nanow")));
        c.get(UnitsPower.NANOWATT).put(UnitsPower.EXAWATT, Mul(Pow(10, 27), Sym("nanow")));
        c.get(UnitsPower.NANOWATT).put(UnitsPower.FEMTOWATT, Mul(Rat(Int(1), Pow(10, 6)), Sym("nanow")));
        c.get(UnitsPower.NANOWATT).put(UnitsPower.GIGAWATT, Mul(Pow(10, 18), Sym("nanow")));
        c.get(UnitsPower.NANOWATT).put(UnitsPower.HECTOWATT, Mul(Pow(10, 11), Sym("nanow")));
        c.get(UnitsPower.NANOWATT).put(UnitsPower.KILOWATT, Mul(Pow(10, 12), Sym("nanow")));
        c.get(UnitsPower.NANOWATT).put(UnitsPower.MEGAWATT, Mul(Pow(10, 15), Sym("nanow")));
        c.get(UnitsPower.NANOWATT).put(UnitsPower.MICROWATT, Mul(Int(1000), Sym("nanow")));
        c.get(UnitsPower.NANOWATT).put(UnitsPower.MILLIWATT, Mul(Pow(10, 6), Sym("nanow")));
        c.get(UnitsPower.NANOWATT).put(UnitsPower.PETAWATT, Mul(Pow(10, 24), Sym("nanow")));
        c.get(UnitsPower.NANOWATT).put(UnitsPower.PICOWATT, Mul(Rat(Int(1), Int(1000)), Sym("nanow")));
        c.get(UnitsPower.NANOWATT).put(UnitsPower.TERAWATT, Mul(Pow(10, 21), Sym("nanow")));
        c.get(UnitsPower.NANOWATT).put(UnitsPower.WATT, Mul(Pow(10, 9), Sym("nanow")));
        c.get(UnitsPower.NANOWATT).put(UnitsPower.YOCTOWATT, Mul(Rat(Int(1), Pow(10, 15)), Sym("nanow")));
        c.get(UnitsPower.NANOWATT).put(UnitsPower.YOTTAWATT, Mul(Pow(10, 33), Sym("nanow")));
        c.get(UnitsPower.NANOWATT).put(UnitsPower.ZEPTOWATT, Mul(Rat(Int(1), Pow(10, 12)), Sym("nanow")));
        c.get(UnitsPower.NANOWATT).put(UnitsPower.ZETTAWATT, Mul(Pow(10, 30), Sym("nanow")));
        c.get(UnitsPower.PETAWATT).put(UnitsPower.ATTOWATT, Mul(Rat(Int(1), Pow(10, 33)), Sym("petaw")));
        c.get(UnitsPower.PETAWATT).put(UnitsPower.CENTIWATT, Mul(Rat(Int(1), Pow(10, 17)), Sym("petaw")));
        c.get(UnitsPower.PETAWATT).put(UnitsPower.DECAWATT, Mul(Rat(Int(1), Pow(10, 14)), Sym("petaw")));
        c.get(UnitsPower.PETAWATT).put(UnitsPower.DECIWATT, Mul(Rat(Int(1), Pow(10, 16)), Sym("petaw")));
        c.get(UnitsPower.PETAWATT).put(UnitsPower.EXAWATT, Mul(Int(1000), Sym("petaw")));
        c.get(UnitsPower.PETAWATT).put(UnitsPower.FEMTOWATT, Mul(Rat(Int(1), Pow(10, 30)), Sym("petaw")));
        c.get(UnitsPower.PETAWATT).put(UnitsPower.GIGAWATT, Mul(Rat(Int(1), Pow(10, 6)), Sym("petaw")));
        c.get(UnitsPower.PETAWATT).put(UnitsPower.HECTOWATT, Mul(Rat(Int(1), Pow(10, 13)), Sym("petaw")));
        c.get(UnitsPower.PETAWATT).put(UnitsPower.KILOWATT, Mul(Rat(Int(1), Pow(10, 12)), Sym("petaw")));
        c.get(UnitsPower.PETAWATT).put(UnitsPower.MEGAWATT, Mul(Rat(Int(1), Pow(10, 9)), Sym("petaw")));
        c.get(UnitsPower.PETAWATT).put(UnitsPower.MICROWATT, Mul(Rat(Int(1), Pow(10, 21)), Sym("petaw")));
        c.get(UnitsPower.PETAWATT).put(UnitsPower.MILLIWATT, Mul(Rat(Int(1), Pow(10, 18)), Sym("petaw")));
        c.get(UnitsPower.PETAWATT).put(UnitsPower.NANOWATT, Mul(Rat(Int(1), Pow(10, 24)), Sym("petaw")));
        c.get(UnitsPower.PETAWATT).put(UnitsPower.PICOWATT, Mul(Rat(Int(1), Pow(10, 27)), Sym("petaw")));
        c.get(UnitsPower.PETAWATT).put(UnitsPower.TERAWATT, Mul(Rat(Int(1), Int(1000)), Sym("petaw")));
        c.get(UnitsPower.PETAWATT).put(UnitsPower.WATT, Mul(Rat(Int(1), Pow(10, 15)), Sym("petaw")));
        c.get(UnitsPower.PETAWATT).put(UnitsPower.YOCTOWATT, Mul(Rat(Int(1), Pow(10, 39)), Sym("petaw")));
        c.get(UnitsPower.PETAWATT).put(UnitsPower.YOTTAWATT, Mul(Pow(10, 9), Sym("petaw")));
        c.get(UnitsPower.PETAWATT).put(UnitsPower.ZEPTOWATT, Mul(Rat(Int(1), Pow(10, 36)), Sym("petaw")));
        c.get(UnitsPower.PETAWATT).put(UnitsPower.ZETTAWATT, Mul(Pow(10, 6), Sym("petaw")));
        c.get(UnitsPower.PICOWATT).put(UnitsPower.ATTOWATT, Mul(Rat(Int(1), Pow(10, 6)), Sym("picow")));
        c.get(UnitsPower.PICOWATT).put(UnitsPower.CENTIWATT, Mul(Pow(10, 10), Sym("picow")));
        c.get(UnitsPower.PICOWATT).put(UnitsPower.DECAWATT, Mul(Pow(10, 13), Sym("picow")));
        c.get(UnitsPower.PICOWATT).put(UnitsPower.DECIWATT, Mul(Pow(10, 11), Sym("picow")));
        c.get(UnitsPower.PICOWATT).put(UnitsPower.EXAWATT, Mul(Pow(10, 30), Sym("picow")));
        c.get(UnitsPower.PICOWATT).put(UnitsPower.FEMTOWATT, Mul(Rat(Int(1), Int(1000)), Sym("picow")));
        c.get(UnitsPower.PICOWATT).put(UnitsPower.GIGAWATT, Mul(Pow(10, 21), Sym("picow")));
        c.get(UnitsPower.PICOWATT).put(UnitsPower.HECTOWATT, Mul(Pow(10, 14), Sym("picow")));
        c.get(UnitsPower.PICOWATT).put(UnitsPower.KILOWATT, Mul(Pow(10, 15), Sym("picow")));
        c.get(UnitsPower.PICOWATT).put(UnitsPower.MEGAWATT, Mul(Pow(10, 18), Sym("picow")));
        c.get(UnitsPower.PICOWATT).put(UnitsPower.MICROWATT, Mul(Pow(10, 6), Sym("picow")));
        c.get(UnitsPower.PICOWATT).put(UnitsPower.MILLIWATT, Mul(Pow(10, 9), Sym("picow")));
        c.get(UnitsPower.PICOWATT).put(UnitsPower.NANOWATT, Mul(Int(1000), Sym("picow")));
        c.get(UnitsPower.PICOWATT).put(UnitsPower.PETAWATT, Mul(Pow(10, 27), Sym("picow")));
        c.get(UnitsPower.PICOWATT).put(UnitsPower.TERAWATT, Mul(Pow(10, 24), Sym("picow")));
        c.get(UnitsPower.PICOWATT).put(UnitsPower.WATT, Mul(Pow(10, 12), Sym("picow")));
        c.get(UnitsPower.PICOWATT).put(UnitsPower.YOCTOWATT, Mul(Rat(Int(1), Pow(10, 12)), Sym("picow")));
        c.get(UnitsPower.PICOWATT).put(UnitsPower.YOTTAWATT, Mul(Pow(10, 36), Sym("picow")));
        c.get(UnitsPower.PICOWATT).put(UnitsPower.ZEPTOWATT, Mul(Rat(Int(1), Pow(10, 9)), Sym("picow")));
        c.get(UnitsPower.PICOWATT).put(UnitsPower.ZETTAWATT, Mul(Pow(10, 33), Sym("picow")));
        c.get(UnitsPower.TERAWATT).put(UnitsPower.ATTOWATT, Mul(Rat(Int(1), Pow(10, 30)), Sym("teraw")));
        c.get(UnitsPower.TERAWATT).put(UnitsPower.CENTIWATT, Mul(Rat(Int(1), Pow(10, 14)), Sym("teraw")));
        c.get(UnitsPower.TERAWATT).put(UnitsPower.DECAWATT, Mul(Rat(Int(1), Pow(10, 11)), Sym("teraw")));
        c.get(UnitsPower.TERAWATT).put(UnitsPower.DECIWATT, Mul(Rat(Int(1), Pow(10, 13)), Sym("teraw")));
        c.get(UnitsPower.TERAWATT).put(UnitsPower.EXAWATT, Mul(Pow(10, 6), Sym("teraw")));
        c.get(UnitsPower.TERAWATT).put(UnitsPower.FEMTOWATT, Mul(Rat(Int(1), Pow(10, 27)), Sym("teraw")));
        c.get(UnitsPower.TERAWATT).put(UnitsPower.GIGAWATT, Mul(Rat(Int(1), Int(1000)), Sym("teraw")));
        c.get(UnitsPower.TERAWATT).put(UnitsPower.HECTOWATT, Mul(Rat(Int(1), Pow(10, 10)), Sym("teraw")));
        c.get(UnitsPower.TERAWATT).put(UnitsPower.KILOWATT, Mul(Rat(Int(1), Pow(10, 9)), Sym("teraw")));
        c.get(UnitsPower.TERAWATT).put(UnitsPower.MEGAWATT, Mul(Rat(Int(1), Pow(10, 6)), Sym("teraw")));
        c.get(UnitsPower.TERAWATT).put(UnitsPower.MICROWATT, Mul(Rat(Int(1), Pow(10, 18)), Sym("teraw")));
        c.get(UnitsPower.TERAWATT).put(UnitsPower.MILLIWATT, Mul(Rat(Int(1), Pow(10, 15)), Sym("teraw")));
        c.get(UnitsPower.TERAWATT).put(UnitsPower.NANOWATT, Mul(Rat(Int(1), Pow(10, 21)), Sym("teraw")));
        c.get(UnitsPower.TERAWATT).put(UnitsPower.PETAWATT, Mul(Int(1000), Sym("teraw")));
        c.get(UnitsPower.TERAWATT).put(UnitsPower.PICOWATT, Mul(Rat(Int(1), Pow(10, 24)), Sym("teraw")));
        c.get(UnitsPower.TERAWATT).put(UnitsPower.WATT, Mul(Rat(Int(1), Pow(10, 12)), Sym("teraw")));
        c.get(UnitsPower.TERAWATT).put(UnitsPower.YOCTOWATT, Mul(Rat(Int(1), Pow(10, 36)), Sym("teraw")));
        c.get(UnitsPower.TERAWATT).put(UnitsPower.YOTTAWATT, Mul(Pow(10, 12), Sym("teraw")));
        c.get(UnitsPower.TERAWATT).put(UnitsPower.ZEPTOWATT, Mul(Rat(Int(1), Pow(10, 33)), Sym("teraw")));
        c.get(UnitsPower.TERAWATT).put(UnitsPower.ZETTAWATT, Mul(Pow(10, 9), Sym("teraw")));
        c.get(UnitsPower.WATT).put(UnitsPower.ATTOWATT, Mul(Rat(Int(1), Pow(10, 18)), Sym("w")));
        c.get(UnitsPower.WATT).put(UnitsPower.CENTIWATT, Mul(Rat(Int(1), Int(100)), Sym("w")));
        c.get(UnitsPower.WATT).put(UnitsPower.DECAWATT, Mul(Int(10), Sym("w")));
        c.get(UnitsPower.WATT).put(UnitsPower.DECIWATT, Mul(Rat(Int(1), Int(10)), Sym("w")));
        c.get(UnitsPower.WATT).put(UnitsPower.EXAWATT, Mul(Pow(10, 18), Sym("w")));
        c.get(UnitsPower.WATT).put(UnitsPower.FEMTOWATT, Mul(Rat(Int(1), Pow(10, 15)), Sym("w")));
        c.get(UnitsPower.WATT).put(UnitsPower.GIGAWATT, Mul(Pow(10, 9), Sym("w")));
        c.get(UnitsPower.WATT).put(UnitsPower.HECTOWATT, Mul(Int(100), Sym("w")));
        c.get(UnitsPower.WATT).put(UnitsPower.KILOWATT, Mul(Int(1000), Sym("w")));
        c.get(UnitsPower.WATT).put(UnitsPower.MEGAWATT, Mul(Pow(10, 6), Sym("w")));
        c.get(UnitsPower.WATT).put(UnitsPower.MICROWATT, Mul(Rat(Int(1), Pow(10, 6)), Sym("w")));
        c.get(UnitsPower.WATT).put(UnitsPower.MILLIWATT, Mul(Rat(Int(1), Int(1000)), Sym("w")));
        c.get(UnitsPower.WATT).put(UnitsPower.NANOWATT, Mul(Rat(Int(1), Pow(10, 9)), Sym("w")));
        c.get(UnitsPower.WATT).put(UnitsPower.PETAWATT, Mul(Pow(10, 15), Sym("w")));
        c.get(UnitsPower.WATT).put(UnitsPower.PICOWATT, Mul(Rat(Int(1), Pow(10, 12)), Sym("w")));
        c.get(UnitsPower.WATT).put(UnitsPower.TERAWATT, Mul(Pow(10, 12), Sym("w")));
        c.get(UnitsPower.WATT).put(UnitsPower.YOCTOWATT, Mul(Rat(Int(1), Pow(10, 24)), Sym("w")));
        c.get(UnitsPower.WATT).put(UnitsPower.YOTTAWATT, Mul(Pow(10, 24), Sym("w")));
        c.get(UnitsPower.WATT).put(UnitsPower.ZEPTOWATT, Mul(Rat(Int(1), Pow(10, 21)), Sym("w")));
        c.get(UnitsPower.WATT).put(UnitsPower.ZETTAWATT, Mul(Pow(10, 21), Sym("w")));
        c.get(UnitsPower.YOCTOWATT).put(UnitsPower.ATTOWATT, Mul(Pow(10, 6), Sym("yoctow")));
        c.get(UnitsPower.YOCTOWATT).put(UnitsPower.CENTIWATT, Mul(Pow(10, 22), Sym("yoctow")));
        c.get(UnitsPower.YOCTOWATT).put(UnitsPower.DECAWATT, Mul(Pow(10, 25), Sym("yoctow")));
        c.get(UnitsPower.YOCTOWATT).put(UnitsPower.DECIWATT, Mul(Pow(10, 23), Sym("yoctow")));
        c.get(UnitsPower.YOCTOWATT).put(UnitsPower.EXAWATT, Mul(Pow(10, 42), Sym("yoctow")));
        c.get(UnitsPower.YOCTOWATT).put(UnitsPower.FEMTOWATT, Mul(Pow(10, 9), Sym("yoctow")));
        c.get(UnitsPower.YOCTOWATT).put(UnitsPower.GIGAWATT, Mul(Pow(10, 33), Sym("yoctow")));
        c.get(UnitsPower.YOCTOWATT).put(UnitsPower.HECTOWATT, Mul(Pow(10, 26), Sym("yoctow")));
        c.get(UnitsPower.YOCTOWATT).put(UnitsPower.KILOWATT, Mul(Pow(10, 27), Sym("yoctow")));
        c.get(UnitsPower.YOCTOWATT).put(UnitsPower.MEGAWATT, Mul(Pow(10, 30), Sym("yoctow")));
        c.get(UnitsPower.YOCTOWATT).put(UnitsPower.MICROWATT, Mul(Pow(10, 18), Sym("yoctow")));
        c.get(UnitsPower.YOCTOWATT).put(UnitsPower.MILLIWATT, Mul(Pow(10, 21), Sym("yoctow")));
        c.get(UnitsPower.YOCTOWATT).put(UnitsPower.NANOWATT, Mul(Pow(10, 15), Sym("yoctow")));
        c.get(UnitsPower.YOCTOWATT).put(UnitsPower.PETAWATT, Mul(Pow(10, 39), Sym("yoctow")));
        c.get(UnitsPower.YOCTOWATT).put(UnitsPower.PICOWATT, Mul(Pow(10, 12), Sym("yoctow")));
        c.get(UnitsPower.YOCTOWATT).put(UnitsPower.TERAWATT, Mul(Pow(10, 36), Sym("yoctow")));
        c.get(UnitsPower.YOCTOWATT).put(UnitsPower.WATT, Mul(Pow(10, 24), Sym("yoctow")));
        c.get(UnitsPower.YOCTOWATT).put(UnitsPower.YOTTAWATT, Mul(Pow(10, 48), Sym("yoctow")));
        c.get(UnitsPower.YOCTOWATT).put(UnitsPower.ZEPTOWATT, Mul(Int(1000), Sym("yoctow")));
        c.get(UnitsPower.YOCTOWATT).put(UnitsPower.ZETTAWATT, Mul(Pow(10, 45), Sym("yoctow")));
        c.get(UnitsPower.YOTTAWATT).put(UnitsPower.ATTOWATT, Mul(Rat(Int(1), Pow(10, 42)), Sym("yottaw")));
        c.get(UnitsPower.YOTTAWATT).put(UnitsPower.CENTIWATT, Mul(Rat(Int(1), Pow(10, 26)), Sym("yottaw")));
        c.get(UnitsPower.YOTTAWATT).put(UnitsPower.DECAWATT, Mul(Rat(Int(1), Pow(10, 23)), Sym("yottaw")));
        c.get(UnitsPower.YOTTAWATT).put(UnitsPower.DECIWATT, Mul(Rat(Int(1), Pow(10, 25)), Sym("yottaw")));
        c.get(UnitsPower.YOTTAWATT).put(UnitsPower.EXAWATT, Mul(Rat(Int(1), Pow(10, 6)), Sym("yottaw")));
        c.get(UnitsPower.YOTTAWATT).put(UnitsPower.FEMTOWATT, Mul(Rat(Int(1), Pow(10, 39)), Sym("yottaw")));
        c.get(UnitsPower.YOTTAWATT).put(UnitsPower.GIGAWATT, Mul(Rat(Int(1), Pow(10, 15)), Sym("yottaw")));
        c.get(UnitsPower.YOTTAWATT).put(UnitsPower.HECTOWATT, Mul(Rat(Int(1), Pow(10, 22)), Sym("yottaw")));
        c.get(UnitsPower.YOTTAWATT).put(UnitsPower.KILOWATT, Mul(Rat(Int(1), Pow(10, 21)), Sym("yottaw")));
        c.get(UnitsPower.YOTTAWATT).put(UnitsPower.MEGAWATT, Mul(Rat(Int(1), Pow(10, 18)), Sym("yottaw")));
        c.get(UnitsPower.YOTTAWATT).put(UnitsPower.MICROWATT, Mul(Rat(Int(1), Pow(10, 30)), Sym("yottaw")));
        c.get(UnitsPower.YOTTAWATT).put(UnitsPower.MILLIWATT, Mul(Rat(Int(1), Pow(10, 27)), Sym("yottaw")));
        c.get(UnitsPower.YOTTAWATT).put(UnitsPower.NANOWATT, Mul(Rat(Int(1), Pow(10, 33)), Sym("yottaw")));
        c.get(UnitsPower.YOTTAWATT).put(UnitsPower.PETAWATT, Mul(Rat(Int(1), Pow(10, 9)), Sym("yottaw")));
        c.get(UnitsPower.YOTTAWATT).put(UnitsPower.PICOWATT, Mul(Rat(Int(1), Pow(10, 36)), Sym("yottaw")));
        c.get(UnitsPower.YOTTAWATT).put(UnitsPower.TERAWATT, Mul(Rat(Int(1), Pow(10, 12)), Sym("yottaw")));
        c.get(UnitsPower.YOTTAWATT).put(UnitsPower.WATT, Mul(Rat(Int(1), Pow(10, 24)), Sym("yottaw")));
        c.get(UnitsPower.YOTTAWATT).put(UnitsPower.YOCTOWATT, Mul(Rat(Int(1), Pow(10, 48)), Sym("yottaw")));
        c.get(UnitsPower.YOTTAWATT).put(UnitsPower.ZEPTOWATT, Mul(Rat(Int(1), Pow(10, 45)), Sym("yottaw")));
        c.get(UnitsPower.YOTTAWATT).put(UnitsPower.ZETTAWATT, Mul(Rat(Int(1), Int(1000)), Sym("yottaw")));
        c.get(UnitsPower.ZEPTOWATT).put(UnitsPower.ATTOWATT, Mul(Int(1000), Sym("zeptow")));
        c.get(UnitsPower.ZEPTOWATT).put(UnitsPower.CENTIWATT, Mul(Pow(10, 19), Sym("zeptow")));
        c.get(UnitsPower.ZEPTOWATT).put(UnitsPower.DECAWATT, Mul(Pow(10, 22), Sym("zeptow")));
        c.get(UnitsPower.ZEPTOWATT).put(UnitsPower.DECIWATT, Mul(Pow(10, 20), Sym("zeptow")));
        c.get(UnitsPower.ZEPTOWATT).put(UnitsPower.EXAWATT, Mul(Pow(10, 39), Sym("zeptow")));
        c.get(UnitsPower.ZEPTOWATT).put(UnitsPower.FEMTOWATT, Mul(Pow(10, 6), Sym("zeptow")));
        c.get(UnitsPower.ZEPTOWATT).put(UnitsPower.GIGAWATT, Mul(Pow(10, 30), Sym("zeptow")));
        c.get(UnitsPower.ZEPTOWATT).put(UnitsPower.HECTOWATT, Mul(Pow(10, 23), Sym("zeptow")));
        c.get(UnitsPower.ZEPTOWATT).put(UnitsPower.KILOWATT, Mul(Pow(10, 24), Sym("zeptow")));
        c.get(UnitsPower.ZEPTOWATT).put(UnitsPower.MEGAWATT, Mul(Pow(10, 27), Sym("zeptow")));
        c.get(UnitsPower.ZEPTOWATT).put(UnitsPower.MICROWATT, Mul(Pow(10, 15), Sym("zeptow")));
        c.get(UnitsPower.ZEPTOWATT).put(UnitsPower.MILLIWATT, Mul(Pow(10, 18), Sym("zeptow")));
        c.get(UnitsPower.ZEPTOWATT).put(UnitsPower.NANOWATT, Mul(Pow(10, 12), Sym("zeptow")));
        c.get(UnitsPower.ZEPTOWATT).put(UnitsPower.PETAWATT, Mul(Pow(10, 36), Sym("zeptow")));
        c.get(UnitsPower.ZEPTOWATT).put(UnitsPower.PICOWATT, Mul(Pow(10, 9), Sym("zeptow")));
        c.get(UnitsPower.ZEPTOWATT).put(UnitsPower.TERAWATT, Mul(Pow(10, 33), Sym("zeptow")));
        c.get(UnitsPower.ZEPTOWATT).put(UnitsPower.WATT, Mul(Pow(10, 21), Sym("zeptow")));
        c.get(UnitsPower.ZEPTOWATT).put(UnitsPower.YOCTOWATT, Mul(Rat(Int(1), Int(1000)), Sym("zeptow")));
        c.get(UnitsPower.ZEPTOWATT).put(UnitsPower.YOTTAWATT, Mul(Pow(10, 45), Sym("zeptow")));
        c.get(UnitsPower.ZEPTOWATT).put(UnitsPower.ZETTAWATT, Mul(Pow(10, 42), Sym("zeptow")));
        c.get(UnitsPower.ZETTAWATT).put(UnitsPower.ATTOWATT, Mul(Rat(Int(1), Pow(10, 39)), Sym("zettaw")));
        c.get(UnitsPower.ZETTAWATT).put(UnitsPower.CENTIWATT, Mul(Rat(Int(1), Pow(10, 23)), Sym("zettaw")));
        c.get(UnitsPower.ZETTAWATT).put(UnitsPower.DECAWATT, Mul(Rat(Int(1), Pow(10, 20)), Sym("zettaw")));
        c.get(UnitsPower.ZETTAWATT).put(UnitsPower.DECIWATT, Mul(Rat(Int(1), Pow(10, 22)), Sym("zettaw")));
        c.get(UnitsPower.ZETTAWATT).put(UnitsPower.EXAWATT, Mul(Rat(Int(1), Int(1000)), Sym("zettaw")));
        c.get(UnitsPower.ZETTAWATT).put(UnitsPower.FEMTOWATT, Mul(Rat(Int(1), Pow(10, 36)), Sym("zettaw")));
        c.get(UnitsPower.ZETTAWATT).put(UnitsPower.GIGAWATT, Mul(Rat(Int(1), Pow(10, 12)), Sym("zettaw")));
        c.get(UnitsPower.ZETTAWATT).put(UnitsPower.HECTOWATT, Mul(Rat(Int(1), Pow(10, 19)), Sym("zettaw")));
        c.get(UnitsPower.ZETTAWATT).put(UnitsPower.KILOWATT, Mul(Rat(Int(1), Pow(10, 18)), Sym("zettaw")));
        c.get(UnitsPower.ZETTAWATT).put(UnitsPower.MEGAWATT, Mul(Rat(Int(1), Pow(10, 15)), Sym("zettaw")));
        c.get(UnitsPower.ZETTAWATT).put(UnitsPower.MICROWATT, Mul(Rat(Int(1), Pow(10, 27)), Sym("zettaw")));
        c.get(UnitsPower.ZETTAWATT).put(UnitsPower.MILLIWATT, Mul(Rat(Int(1), Pow(10, 24)), Sym("zettaw")));
        c.get(UnitsPower.ZETTAWATT).put(UnitsPower.NANOWATT, Mul(Rat(Int(1), Pow(10, 30)), Sym("zettaw")));
        c.get(UnitsPower.ZETTAWATT).put(UnitsPower.PETAWATT, Mul(Rat(Int(1), Pow(10, 6)), Sym("zettaw")));
        c.get(UnitsPower.ZETTAWATT).put(UnitsPower.PICOWATT, Mul(Rat(Int(1), Pow(10, 33)), Sym("zettaw")));
        c.get(UnitsPower.ZETTAWATT).put(UnitsPower.TERAWATT, Mul(Rat(Int(1), Pow(10, 9)), Sym("zettaw")));
        c.get(UnitsPower.ZETTAWATT).put(UnitsPower.WATT, Mul(Rat(Int(1), Pow(10, 21)), Sym("zettaw")));
        c.get(UnitsPower.ZETTAWATT).put(UnitsPower.YOCTOWATT, Mul(Rat(Int(1), Pow(10, 45)), Sym("zettaw")));
        c.get(UnitsPower.ZETTAWATT).put(UnitsPower.YOTTAWATT, Mul(Int(1000), Sym("zettaw")));
        c.get(UnitsPower.ZETTAWATT).put(UnitsPower.ZEPTOWATT, Mul(Rat(Int(1), Pow(10, 42)), Sym("zettaw")));
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

