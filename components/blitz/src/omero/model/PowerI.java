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

    private static final Map<String, Conversion> conversions;
    static {
        Map<String, Conversion> c = new HashMap<String, Conversion>();

        c.put("ATTOWATT:CENTIWATT", Mul(Pow(10, 16), Sym("attow")));
        c.put("ATTOWATT:DECAWATT", Mul(Pow(10, 19), Sym("attow")));
        c.put("ATTOWATT:DECIWATT", Mul(Pow(10, 17), Sym("attow")));
        c.put("ATTOWATT:EXAWATT", Mul(Pow(10, 36), Sym("attow")));
        c.put("ATTOWATT:FEMTOWATT", Mul(Int(1000), Sym("attow")));
        c.put("ATTOWATT:GIGAWATT", Mul(Pow(10, 27), Sym("attow")));
        c.put("ATTOWATT:HECTOWATT", Mul(Pow(10, 20), Sym("attow")));
        c.put("ATTOWATT:KILOWATT", Mul(Pow(10, 21), Sym("attow")));
        c.put("ATTOWATT:MEGAWATT", Mul(Pow(10, 24), Sym("attow")));
        c.put("ATTOWATT:MICROWATT", Mul(Pow(10, 12), Sym("attow")));
        c.put("ATTOWATT:MILLIWATT", Mul(Pow(10, 15), Sym("attow")));
        c.put("ATTOWATT:NANOWATT", Mul(Pow(10, 9), Sym("attow")));
        c.put("ATTOWATT:PETAWATT", Mul(Pow(10, 33), Sym("attow")));
        c.put("ATTOWATT:PICOWATT", Mul(Pow(10, 6), Sym("attow")));
        c.put("ATTOWATT:TERAWATT", Mul(Pow(10, 30), Sym("attow")));
        c.put("ATTOWATT:WATT", Mul(Pow(10, 18), Sym("attow")));
        c.put("ATTOWATT:YOCTOWATT", Mul(Rat(Int(1), Pow(10, 6)), Sym("attow")));
        c.put("ATTOWATT:YOTTAWATT", Mul(Pow(10, 42), Sym("attow")));
        c.put("ATTOWATT:ZEPTOWATT", Mul(Rat(Int(1), Int(1000)), Sym("attow")));
        c.put("ATTOWATT:ZETTAWATT", Mul(Pow(10, 39), Sym("attow")));
        c.put("CENTIWATT:ATTOWATT", Mul(Rat(Int(1), Pow(10, 16)), Sym("centiw")));
        c.put("CENTIWATT:DECAWATT", Mul(Int(1000), Sym("centiw")));
        c.put("CENTIWATT:DECIWATT", Mul(Int(10), Sym("centiw")));
        c.put("CENTIWATT:EXAWATT", Mul(Pow(10, 20), Sym("centiw")));
        c.put("CENTIWATT:FEMTOWATT", Mul(Rat(Int(1), Pow(10, 13)), Sym("centiw")));
        c.put("CENTIWATT:GIGAWATT", Mul(Pow(10, 11), Sym("centiw")));
        c.put("CENTIWATT:HECTOWATT", Mul(Pow(10, 4), Sym("centiw")));
        c.put("CENTIWATT:KILOWATT", Mul(Pow(10, 5), Sym("centiw")));
        c.put("CENTIWATT:MEGAWATT", Mul(Pow(10, 8), Sym("centiw")));
        c.put("CENTIWATT:MICROWATT", Mul(Rat(Int(1), Pow(10, 4)), Sym("centiw")));
        c.put("CENTIWATT:MILLIWATT", Mul(Rat(Int(1), Int(10)), Sym("centiw")));
        c.put("CENTIWATT:NANOWATT", Mul(Rat(Int(1), Pow(10, 7)), Sym("centiw")));
        c.put("CENTIWATT:PETAWATT", Mul(Pow(10, 17), Sym("centiw")));
        c.put("CENTIWATT:PICOWATT", Mul(Rat(Int(1), Pow(10, 10)), Sym("centiw")));
        c.put("CENTIWATT:TERAWATT", Mul(Pow(10, 14), Sym("centiw")));
        c.put("CENTIWATT:WATT", Mul(Int(100), Sym("centiw")));
        c.put("CENTIWATT:YOCTOWATT", Mul(Rat(Int(1), Pow(10, 22)), Sym("centiw")));
        c.put("CENTIWATT:YOTTAWATT", Mul(Pow(10, 26), Sym("centiw")));
        c.put("CENTIWATT:ZEPTOWATT", Mul(Rat(Int(1), Pow(10, 19)), Sym("centiw")));
        c.put("CENTIWATT:ZETTAWATT", Mul(Pow(10, 23), Sym("centiw")));
        c.put("DECAWATT:ATTOWATT", Mul(Rat(Int(1), Pow(10, 19)), Sym("decaw")));
        c.put("DECAWATT:CENTIWATT", Mul(Rat(Int(1), Int(1000)), Sym("decaw")));
        c.put("DECAWATT:DECIWATT", Mul(Rat(Int(1), Int(100)), Sym("decaw")));
        c.put("DECAWATT:EXAWATT", Mul(Pow(10, 17), Sym("decaw")));
        c.put("DECAWATT:FEMTOWATT", Mul(Rat(Int(1), Pow(10, 16)), Sym("decaw")));
        c.put("DECAWATT:GIGAWATT", Mul(Pow(10, 8), Sym("decaw")));
        c.put("DECAWATT:HECTOWATT", Mul(Int(10), Sym("decaw")));
        c.put("DECAWATT:KILOWATT", Mul(Int(100), Sym("decaw")));
        c.put("DECAWATT:MEGAWATT", Mul(Pow(10, 5), Sym("decaw")));
        c.put("DECAWATT:MICROWATT", Mul(Rat(Int(1), Pow(10, 7)), Sym("decaw")));
        c.put("DECAWATT:MILLIWATT", Mul(Rat(Int(1), Pow(10, 4)), Sym("decaw")));
        c.put("DECAWATT:NANOWATT", Mul(Rat(Int(1), Pow(10, 10)), Sym("decaw")));
        c.put("DECAWATT:PETAWATT", Mul(Pow(10, 14), Sym("decaw")));
        c.put("DECAWATT:PICOWATT", Mul(Rat(Int(1), Pow(10, 13)), Sym("decaw")));
        c.put("DECAWATT:TERAWATT", Mul(Pow(10, 11), Sym("decaw")));
        c.put("DECAWATT:WATT", Mul(Rat(Int(1), Int(10)), Sym("decaw")));
        c.put("DECAWATT:YOCTOWATT", Mul(Rat(Int(1), Pow(10, 25)), Sym("decaw")));
        c.put("DECAWATT:YOTTAWATT", Mul(Pow(10, 23), Sym("decaw")));
        c.put("DECAWATT:ZEPTOWATT", Mul(Rat(Int(1), Pow(10, 22)), Sym("decaw")));
        c.put("DECAWATT:ZETTAWATT", Mul(Pow(10, 20), Sym("decaw")));
        c.put("DECIWATT:ATTOWATT", Mul(Rat(Int(1), Pow(10, 17)), Sym("deciw")));
        c.put("DECIWATT:CENTIWATT", Mul(Rat(Int(1), Int(10)), Sym("deciw")));
        c.put("DECIWATT:DECAWATT", Mul(Int(100), Sym("deciw")));
        c.put("DECIWATT:EXAWATT", Mul(Pow(10, 19), Sym("deciw")));
        c.put("DECIWATT:FEMTOWATT", Mul(Rat(Int(1), Pow(10, 14)), Sym("deciw")));
        c.put("DECIWATT:GIGAWATT", Mul(Pow(10, 10), Sym("deciw")));
        c.put("DECIWATT:HECTOWATT", Mul(Int(1000), Sym("deciw")));
        c.put("DECIWATT:KILOWATT", Mul(Pow(10, 4), Sym("deciw")));
        c.put("DECIWATT:MEGAWATT", Mul(Pow(10, 7), Sym("deciw")));
        c.put("DECIWATT:MICROWATT", Mul(Rat(Int(1), Pow(10, 5)), Sym("deciw")));
        c.put("DECIWATT:MILLIWATT", Mul(Rat(Int(1), Int(100)), Sym("deciw")));
        c.put("DECIWATT:NANOWATT", Mul(Rat(Int(1), Pow(10, 8)), Sym("deciw")));
        c.put("DECIWATT:PETAWATT", Mul(Pow(10, 16), Sym("deciw")));
        c.put("DECIWATT:PICOWATT", Mul(Rat(Int(1), Pow(10, 11)), Sym("deciw")));
        c.put("DECIWATT:TERAWATT", Mul(Pow(10, 13), Sym("deciw")));
        c.put("DECIWATT:WATT", Mul(Int(10), Sym("deciw")));
        c.put("DECIWATT:YOCTOWATT", Mul(Rat(Int(1), Pow(10, 23)), Sym("deciw")));
        c.put("DECIWATT:YOTTAWATT", Mul(Pow(10, 25), Sym("deciw")));
        c.put("DECIWATT:ZEPTOWATT", Mul(Rat(Int(1), Pow(10, 20)), Sym("deciw")));
        c.put("DECIWATT:ZETTAWATT", Mul(Pow(10, 22), Sym("deciw")));
        c.put("EXAWATT:ATTOWATT", Mul(Rat(Int(1), Pow(10, 36)), Sym("exaw")));
        c.put("EXAWATT:CENTIWATT", Mul(Rat(Int(1), Pow(10, 20)), Sym("exaw")));
        c.put("EXAWATT:DECAWATT", Mul(Rat(Int(1), Pow(10, 17)), Sym("exaw")));
        c.put("EXAWATT:DECIWATT", Mul(Rat(Int(1), Pow(10, 19)), Sym("exaw")));
        c.put("EXAWATT:FEMTOWATT", Mul(Rat(Int(1), Pow(10, 33)), Sym("exaw")));
        c.put("EXAWATT:GIGAWATT", Mul(Rat(Int(1), Pow(10, 9)), Sym("exaw")));
        c.put("EXAWATT:HECTOWATT", Mul(Rat(Int(1), Pow(10, 16)), Sym("exaw")));
        c.put("EXAWATT:KILOWATT", Mul(Rat(Int(1), Pow(10, 15)), Sym("exaw")));
        c.put("EXAWATT:MEGAWATT", Mul(Rat(Int(1), Pow(10, 12)), Sym("exaw")));
        c.put("EXAWATT:MICROWATT", Mul(Rat(Int(1), Pow(10, 24)), Sym("exaw")));
        c.put("EXAWATT:MILLIWATT", Mul(Rat(Int(1), Pow(10, 21)), Sym("exaw")));
        c.put("EXAWATT:NANOWATT", Mul(Rat(Int(1), Pow(10, 27)), Sym("exaw")));
        c.put("EXAWATT:PETAWATT", Mul(Rat(Int(1), Int(1000)), Sym("exaw")));
        c.put("EXAWATT:PICOWATT", Mul(Rat(Int(1), Pow(10, 30)), Sym("exaw")));
        c.put("EXAWATT:TERAWATT", Mul(Rat(Int(1), Pow(10, 6)), Sym("exaw")));
        c.put("EXAWATT:WATT", Mul(Rat(Int(1), Pow(10, 18)), Sym("exaw")));
        c.put("EXAWATT:YOCTOWATT", Mul(Rat(Int(1), Pow(10, 42)), Sym("exaw")));
        c.put("EXAWATT:YOTTAWATT", Mul(Pow(10, 6), Sym("exaw")));
        c.put("EXAWATT:ZEPTOWATT", Mul(Rat(Int(1), Pow(10, 39)), Sym("exaw")));
        c.put("EXAWATT:ZETTAWATT", Mul(Int(1000), Sym("exaw")));
        c.put("FEMTOWATT:ATTOWATT", Mul(Rat(Int(1), Int(1000)), Sym("femtow")));
        c.put("FEMTOWATT:CENTIWATT", Mul(Pow(10, 13), Sym("femtow")));
        c.put("FEMTOWATT:DECAWATT", Mul(Pow(10, 16), Sym("femtow")));
        c.put("FEMTOWATT:DECIWATT", Mul(Pow(10, 14), Sym("femtow")));
        c.put("FEMTOWATT:EXAWATT", Mul(Pow(10, 33), Sym("femtow")));
        c.put("FEMTOWATT:GIGAWATT", Mul(Pow(10, 24), Sym("femtow")));
        c.put("FEMTOWATT:HECTOWATT", Mul(Pow(10, 17), Sym("femtow")));
        c.put("FEMTOWATT:KILOWATT", Mul(Pow(10, 18), Sym("femtow")));
        c.put("FEMTOWATT:MEGAWATT", Mul(Pow(10, 21), Sym("femtow")));
        c.put("FEMTOWATT:MICROWATT", Mul(Pow(10, 9), Sym("femtow")));
        c.put("FEMTOWATT:MILLIWATT", Mul(Pow(10, 12), Sym("femtow")));
        c.put("FEMTOWATT:NANOWATT", Mul(Pow(10, 6), Sym("femtow")));
        c.put("FEMTOWATT:PETAWATT", Mul(Pow(10, 30), Sym("femtow")));
        c.put("FEMTOWATT:PICOWATT", Mul(Int(1000), Sym("femtow")));
        c.put("FEMTOWATT:TERAWATT", Mul(Pow(10, 27), Sym("femtow")));
        c.put("FEMTOWATT:WATT", Mul(Pow(10, 15), Sym("femtow")));
        c.put("FEMTOWATT:YOCTOWATT", Mul(Rat(Int(1), Pow(10, 9)), Sym("femtow")));
        c.put("FEMTOWATT:YOTTAWATT", Mul(Pow(10, 39), Sym("femtow")));
        c.put("FEMTOWATT:ZEPTOWATT", Mul(Rat(Int(1), Pow(10, 6)), Sym("femtow")));
        c.put("FEMTOWATT:ZETTAWATT", Mul(Pow(10, 36), Sym("femtow")));
        c.put("GIGAWATT:ATTOWATT", Mul(Rat(Int(1), Pow(10, 27)), Sym("gigaw")));
        c.put("GIGAWATT:CENTIWATT", Mul(Rat(Int(1), Pow(10, 11)), Sym("gigaw")));
        c.put("GIGAWATT:DECAWATT", Mul(Rat(Int(1), Pow(10, 8)), Sym("gigaw")));
        c.put("GIGAWATT:DECIWATT", Mul(Rat(Int(1), Pow(10, 10)), Sym("gigaw")));
        c.put("GIGAWATT:EXAWATT", Mul(Pow(10, 9), Sym("gigaw")));
        c.put("GIGAWATT:FEMTOWATT", Mul(Rat(Int(1), Pow(10, 24)), Sym("gigaw")));
        c.put("GIGAWATT:HECTOWATT", Mul(Rat(Int(1), Pow(10, 7)), Sym("gigaw")));
        c.put("GIGAWATT:KILOWATT", Mul(Rat(Int(1), Pow(10, 6)), Sym("gigaw")));
        c.put("GIGAWATT:MEGAWATT", Mul(Rat(Int(1), Int(1000)), Sym("gigaw")));
        c.put("GIGAWATT:MICROWATT", Mul(Rat(Int(1), Pow(10, 15)), Sym("gigaw")));
        c.put("GIGAWATT:MILLIWATT", Mul(Rat(Int(1), Pow(10, 12)), Sym("gigaw")));
        c.put("GIGAWATT:NANOWATT", Mul(Rat(Int(1), Pow(10, 18)), Sym("gigaw")));
        c.put("GIGAWATT:PETAWATT", Mul(Pow(10, 6), Sym("gigaw")));
        c.put("GIGAWATT:PICOWATT", Mul(Rat(Int(1), Pow(10, 21)), Sym("gigaw")));
        c.put("GIGAWATT:TERAWATT", Mul(Int(1000), Sym("gigaw")));
        c.put("GIGAWATT:WATT", Mul(Rat(Int(1), Pow(10, 9)), Sym("gigaw")));
        c.put("GIGAWATT:YOCTOWATT", Mul(Rat(Int(1), Pow(10, 33)), Sym("gigaw")));
        c.put("GIGAWATT:YOTTAWATT", Mul(Pow(10, 15), Sym("gigaw")));
        c.put("GIGAWATT:ZEPTOWATT", Mul(Rat(Int(1), Pow(10, 30)), Sym("gigaw")));
        c.put("GIGAWATT:ZETTAWATT", Mul(Pow(10, 12), Sym("gigaw")));
        c.put("HECTOWATT:ATTOWATT", Mul(Rat(Int(1), Pow(10, 20)), Sym("hectow")));
        c.put("HECTOWATT:CENTIWATT", Mul(Rat(Int(1), Pow(10, 4)), Sym("hectow")));
        c.put("HECTOWATT:DECAWATT", Mul(Rat(Int(1), Int(10)), Sym("hectow")));
        c.put("HECTOWATT:DECIWATT", Mul(Rat(Int(1), Int(1000)), Sym("hectow")));
        c.put("HECTOWATT:EXAWATT", Mul(Pow(10, 16), Sym("hectow")));
        c.put("HECTOWATT:FEMTOWATT", Mul(Rat(Int(1), Pow(10, 17)), Sym("hectow")));
        c.put("HECTOWATT:GIGAWATT", Mul(Pow(10, 7), Sym("hectow")));
        c.put("HECTOWATT:KILOWATT", Mul(Int(10), Sym("hectow")));
        c.put("HECTOWATT:MEGAWATT", Mul(Pow(10, 4), Sym("hectow")));
        c.put("HECTOWATT:MICROWATT", Mul(Rat(Int(1), Pow(10, 8)), Sym("hectow")));
        c.put("HECTOWATT:MILLIWATT", Mul(Rat(Int(1), Pow(10, 5)), Sym("hectow")));
        c.put("HECTOWATT:NANOWATT", Mul(Rat(Int(1), Pow(10, 11)), Sym("hectow")));
        c.put("HECTOWATT:PETAWATT", Mul(Pow(10, 13), Sym("hectow")));
        c.put("HECTOWATT:PICOWATT", Mul(Rat(Int(1), Pow(10, 14)), Sym("hectow")));
        c.put("HECTOWATT:TERAWATT", Mul(Pow(10, 10), Sym("hectow")));
        c.put("HECTOWATT:WATT", Mul(Rat(Int(1), Int(100)), Sym("hectow")));
        c.put("HECTOWATT:YOCTOWATT", Mul(Rat(Int(1), Pow(10, 26)), Sym("hectow")));
        c.put("HECTOWATT:YOTTAWATT", Mul(Pow(10, 22), Sym("hectow")));
        c.put("HECTOWATT:ZEPTOWATT", Mul(Rat(Int(1), Pow(10, 23)), Sym("hectow")));
        c.put("HECTOWATT:ZETTAWATT", Mul(Pow(10, 19), Sym("hectow")));
        c.put("KILOWATT:ATTOWATT", Mul(Rat(Int(1), Pow(10, 21)), Sym("kilow")));
        c.put("KILOWATT:CENTIWATT", Mul(Rat(Int(1), Pow(10, 5)), Sym("kilow")));
        c.put("KILOWATT:DECAWATT", Mul(Rat(Int(1), Int(100)), Sym("kilow")));
        c.put("KILOWATT:DECIWATT", Mul(Rat(Int(1), Pow(10, 4)), Sym("kilow")));
        c.put("KILOWATT:EXAWATT", Mul(Pow(10, 15), Sym("kilow")));
        c.put("KILOWATT:FEMTOWATT", Mul(Rat(Int(1), Pow(10, 18)), Sym("kilow")));
        c.put("KILOWATT:GIGAWATT", Mul(Pow(10, 6), Sym("kilow")));
        c.put("KILOWATT:HECTOWATT", Mul(Rat(Int(1), Int(10)), Sym("kilow")));
        c.put("KILOWATT:MEGAWATT", Mul(Int(1000), Sym("kilow")));
        c.put("KILOWATT:MICROWATT", Mul(Rat(Int(1), Pow(10, 9)), Sym("kilow")));
        c.put("KILOWATT:MILLIWATT", Mul(Rat(Int(1), Pow(10, 6)), Sym("kilow")));
        c.put("KILOWATT:NANOWATT", Mul(Rat(Int(1), Pow(10, 12)), Sym("kilow")));
        c.put("KILOWATT:PETAWATT", Mul(Pow(10, 12), Sym("kilow")));
        c.put("KILOWATT:PICOWATT", Mul(Rat(Int(1), Pow(10, 15)), Sym("kilow")));
        c.put("KILOWATT:TERAWATT", Mul(Pow(10, 9), Sym("kilow")));
        c.put("KILOWATT:WATT", Mul(Rat(Int(1), Int(1000)), Sym("kilow")));
        c.put("KILOWATT:YOCTOWATT", Mul(Rat(Int(1), Pow(10, 27)), Sym("kilow")));
        c.put("KILOWATT:YOTTAWATT", Mul(Pow(10, 21), Sym("kilow")));
        c.put("KILOWATT:ZEPTOWATT", Mul(Rat(Int(1), Pow(10, 24)), Sym("kilow")));
        c.put("KILOWATT:ZETTAWATT", Mul(Pow(10, 18), Sym("kilow")));
        c.put("MEGAWATT:ATTOWATT", Mul(Rat(Int(1), Pow(10, 24)), Sym("megaw")));
        c.put("MEGAWATT:CENTIWATT", Mul(Rat(Int(1), Pow(10, 8)), Sym("megaw")));
        c.put("MEGAWATT:DECAWATT", Mul(Rat(Int(1), Pow(10, 5)), Sym("megaw")));
        c.put("MEGAWATT:DECIWATT", Mul(Rat(Int(1), Pow(10, 7)), Sym("megaw")));
        c.put("MEGAWATT:EXAWATT", Mul(Pow(10, 12), Sym("megaw")));
        c.put("MEGAWATT:FEMTOWATT", Mul(Rat(Int(1), Pow(10, 21)), Sym("megaw")));
        c.put("MEGAWATT:GIGAWATT", Mul(Int(1000), Sym("megaw")));
        c.put("MEGAWATT:HECTOWATT", Mul(Rat(Int(1), Pow(10, 4)), Sym("megaw")));
        c.put("MEGAWATT:KILOWATT", Mul(Rat(Int(1), Int(1000)), Sym("megaw")));
        c.put("MEGAWATT:MICROWATT", Mul(Rat(Int(1), Pow(10, 12)), Sym("megaw")));
        c.put("MEGAWATT:MILLIWATT", Mul(Rat(Int(1), Pow(10, 9)), Sym("megaw")));
        c.put("MEGAWATT:NANOWATT", Mul(Rat(Int(1), Pow(10, 15)), Sym("megaw")));
        c.put("MEGAWATT:PETAWATT", Mul(Pow(10, 9), Sym("megaw")));
        c.put("MEGAWATT:PICOWATT", Mul(Rat(Int(1), Pow(10, 18)), Sym("megaw")));
        c.put("MEGAWATT:TERAWATT", Mul(Pow(10, 6), Sym("megaw")));
        c.put("MEGAWATT:WATT", Mul(Rat(Int(1), Pow(10, 6)), Sym("megaw")));
        c.put("MEGAWATT:YOCTOWATT", Mul(Rat(Int(1), Pow(10, 30)), Sym("megaw")));
        c.put("MEGAWATT:YOTTAWATT", Mul(Pow(10, 18), Sym("megaw")));
        c.put("MEGAWATT:ZEPTOWATT", Mul(Rat(Int(1), Pow(10, 27)), Sym("megaw")));
        c.put("MEGAWATT:ZETTAWATT", Mul(Pow(10, 15), Sym("megaw")));
        c.put("MICROWATT:ATTOWATT", Mul(Rat(Int(1), Pow(10, 12)), Sym("microw")));
        c.put("MICROWATT:CENTIWATT", Mul(Pow(10, 4), Sym("microw")));
        c.put("MICROWATT:DECAWATT", Mul(Pow(10, 7), Sym("microw")));
        c.put("MICROWATT:DECIWATT", Mul(Pow(10, 5), Sym("microw")));
        c.put("MICROWATT:EXAWATT", Mul(Pow(10, 24), Sym("microw")));
        c.put("MICROWATT:FEMTOWATT", Mul(Rat(Int(1), Pow(10, 9)), Sym("microw")));
        c.put("MICROWATT:GIGAWATT", Mul(Pow(10, 15), Sym("microw")));
        c.put("MICROWATT:HECTOWATT", Mul(Pow(10, 8), Sym("microw")));
        c.put("MICROWATT:KILOWATT", Mul(Pow(10, 9), Sym("microw")));
        c.put("MICROWATT:MEGAWATT", Mul(Pow(10, 12), Sym("microw")));
        c.put("MICROWATT:MILLIWATT", Mul(Int(1000), Sym("microw")));
        c.put("MICROWATT:NANOWATT", Mul(Rat(Int(1), Int(1000)), Sym("microw")));
        c.put("MICROWATT:PETAWATT", Mul(Pow(10, 21), Sym("microw")));
        c.put("MICROWATT:PICOWATT", Mul(Rat(Int(1), Pow(10, 6)), Sym("microw")));
        c.put("MICROWATT:TERAWATT", Mul(Pow(10, 18), Sym("microw")));
        c.put("MICROWATT:WATT", Mul(Pow(10, 6), Sym("microw")));
        c.put("MICROWATT:YOCTOWATT", Mul(Rat(Int(1), Pow(10, 18)), Sym("microw")));
        c.put("MICROWATT:YOTTAWATT", Mul(Pow(10, 30), Sym("microw")));
        c.put("MICROWATT:ZEPTOWATT", Mul(Rat(Int(1), Pow(10, 15)), Sym("microw")));
        c.put("MICROWATT:ZETTAWATT", Mul(Pow(10, 27), Sym("microw")));
        c.put("MILLIWATT:ATTOWATT", Mul(Rat(Int(1), Pow(10, 15)), Sym("milliw")));
        c.put("MILLIWATT:CENTIWATT", Mul(Int(10), Sym("milliw")));
        c.put("MILLIWATT:DECAWATT", Mul(Pow(10, 4), Sym("milliw")));
        c.put("MILLIWATT:DECIWATT", Mul(Int(100), Sym("milliw")));
        c.put("MILLIWATT:EXAWATT", Mul(Pow(10, 21), Sym("milliw")));
        c.put("MILLIWATT:FEMTOWATT", Mul(Rat(Int(1), Pow(10, 12)), Sym("milliw")));
        c.put("MILLIWATT:GIGAWATT", Mul(Pow(10, 12), Sym("milliw")));
        c.put("MILLIWATT:HECTOWATT", Mul(Pow(10, 5), Sym("milliw")));
        c.put("MILLIWATT:KILOWATT", Mul(Pow(10, 6), Sym("milliw")));
        c.put("MILLIWATT:MEGAWATT", Mul(Pow(10, 9), Sym("milliw")));
        c.put("MILLIWATT:MICROWATT", Mul(Rat(Int(1), Int(1000)), Sym("milliw")));
        c.put("MILLIWATT:NANOWATT", Mul(Rat(Int(1), Pow(10, 6)), Sym("milliw")));
        c.put("MILLIWATT:PETAWATT", Mul(Pow(10, 18), Sym("milliw")));
        c.put("MILLIWATT:PICOWATT", Mul(Rat(Int(1), Pow(10, 9)), Sym("milliw")));
        c.put("MILLIWATT:TERAWATT", Mul(Pow(10, 15), Sym("milliw")));
        c.put("MILLIWATT:WATT", Mul(Int(1000), Sym("milliw")));
        c.put("MILLIWATT:YOCTOWATT", Mul(Rat(Int(1), Pow(10, 21)), Sym("milliw")));
        c.put("MILLIWATT:YOTTAWATT", Mul(Pow(10, 27), Sym("milliw")));
        c.put("MILLIWATT:ZEPTOWATT", Mul(Rat(Int(1), Pow(10, 18)), Sym("milliw")));
        c.put("MILLIWATT:ZETTAWATT", Mul(Pow(10, 24), Sym("milliw")));
        c.put("NANOWATT:ATTOWATT", Mul(Rat(Int(1), Pow(10, 9)), Sym("nanow")));
        c.put("NANOWATT:CENTIWATT", Mul(Pow(10, 7), Sym("nanow")));
        c.put("NANOWATT:DECAWATT", Mul(Pow(10, 10), Sym("nanow")));
        c.put("NANOWATT:DECIWATT", Mul(Pow(10, 8), Sym("nanow")));
        c.put("NANOWATT:EXAWATT", Mul(Pow(10, 27), Sym("nanow")));
        c.put("NANOWATT:FEMTOWATT", Mul(Rat(Int(1), Pow(10, 6)), Sym("nanow")));
        c.put("NANOWATT:GIGAWATT", Mul(Pow(10, 18), Sym("nanow")));
        c.put("NANOWATT:HECTOWATT", Mul(Pow(10, 11), Sym("nanow")));
        c.put("NANOWATT:KILOWATT", Mul(Pow(10, 12), Sym("nanow")));
        c.put("NANOWATT:MEGAWATT", Mul(Pow(10, 15), Sym("nanow")));
        c.put("NANOWATT:MICROWATT", Mul(Int(1000), Sym("nanow")));
        c.put("NANOWATT:MILLIWATT", Mul(Pow(10, 6), Sym("nanow")));
        c.put("NANOWATT:PETAWATT", Mul(Pow(10, 24), Sym("nanow")));
        c.put("NANOWATT:PICOWATT", Mul(Rat(Int(1), Int(1000)), Sym("nanow")));
        c.put("NANOWATT:TERAWATT", Mul(Pow(10, 21), Sym("nanow")));
        c.put("NANOWATT:WATT", Mul(Pow(10, 9), Sym("nanow")));
        c.put("NANOWATT:YOCTOWATT", Mul(Rat(Int(1), Pow(10, 15)), Sym("nanow")));
        c.put("NANOWATT:YOTTAWATT", Mul(Pow(10, 33), Sym("nanow")));
        c.put("NANOWATT:ZEPTOWATT", Mul(Rat(Int(1), Pow(10, 12)), Sym("nanow")));
        c.put("NANOWATT:ZETTAWATT", Mul(Pow(10, 30), Sym("nanow")));
        c.put("PETAWATT:ATTOWATT", Mul(Rat(Int(1), Pow(10, 33)), Sym("petaw")));
        c.put("PETAWATT:CENTIWATT", Mul(Rat(Int(1), Pow(10, 17)), Sym("petaw")));
        c.put("PETAWATT:DECAWATT", Mul(Rat(Int(1), Pow(10, 14)), Sym("petaw")));
        c.put("PETAWATT:DECIWATT", Mul(Rat(Int(1), Pow(10, 16)), Sym("petaw")));
        c.put("PETAWATT:EXAWATT", Mul(Int(1000), Sym("petaw")));
        c.put("PETAWATT:FEMTOWATT", Mul(Rat(Int(1), Pow(10, 30)), Sym("petaw")));
        c.put("PETAWATT:GIGAWATT", Mul(Rat(Int(1), Pow(10, 6)), Sym("petaw")));
        c.put("PETAWATT:HECTOWATT", Mul(Rat(Int(1), Pow(10, 13)), Sym("petaw")));
        c.put("PETAWATT:KILOWATT", Mul(Rat(Int(1), Pow(10, 12)), Sym("petaw")));
        c.put("PETAWATT:MEGAWATT", Mul(Rat(Int(1), Pow(10, 9)), Sym("petaw")));
        c.put("PETAWATT:MICROWATT", Mul(Rat(Int(1), Pow(10, 21)), Sym("petaw")));
        c.put("PETAWATT:MILLIWATT", Mul(Rat(Int(1), Pow(10, 18)), Sym("petaw")));
        c.put("PETAWATT:NANOWATT", Mul(Rat(Int(1), Pow(10, 24)), Sym("petaw")));
        c.put("PETAWATT:PICOWATT", Mul(Rat(Int(1), Pow(10, 27)), Sym("petaw")));
        c.put("PETAWATT:TERAWATT", Mul(Rat(Int(1), Int(1000)), Sym("petaw")));
        c.put("PETAWATT:WATT", Mul(Rat(Int(1), Pow(10, 15)), Sym("petaw")));
        c.put("PETAWATT:YOCTOWATT", Mul(Rat(Int(1), Pow(10, 39)), Sym("petaw")));
        c.put("PETAWATT:YOTTAWATT", Mul(Pow(10, 9), Sym("petaw")));
        c.put("PETAWATT:ZEPTOWATT", Mul(Rat(Int(1), Pow(10, 36)), Sym("petaw")));
        c.put("PETAWATT:ZETTAWATT", Mul(Pow(10, 6), Sym("petaw")));
        c.put("PICOWATT:ATTOWATT", Mul(Rat(Int(1), Pow(10, 6)), Sym("picow")));
        c.put("PICOWATT:CENTIWATT", Mul(Pow(10, 10), Sym("picow")));
        c.put("PICOWATT:DECAWATT", Mul(Pow(10, 13), Sym("picow")));
        c.put("PICOWATT:DECIWATT", Mul(Pow(10, 11), Sym("picow")));
        c.put("PICOWATT:EXAWATT", Mul(Pow(10, 30), Sym("picow")));
        c.put("PICOWATT:FEMTOWATT", Mul(Rat(Int(1), Int(1000)), Sym("picow")));
        c.put("PICOWATT:GIGAWATT", Mul(Pow(10, 21), Sym("picow")));
        c.put("PICOWATT:HECTOWATT", Mul(Pow(10, 14), Sym("picow")));
        c.put("PICOWATT:KILOWATT", Mul(Pow(10, 15), Sym("picow")));
        c.put("PICOWATT:MEGAWATT", Mul(Pow(10, 18), Sym("picow")));
        c.put("PICOWATT:MICROWATT", Mul(Pow(10, 6), Sym("picow")));
        c.put("PICOWATT:MILLIWATT", Mul(Pow(10, 9), Sym("picow")));
        c.put("PICOWATT:NANOWATT", Mul(Int(1000), Sym("picow")));
        c.put("PICOWATT:PETAWATT", Mul(Pow(10, 27), Sym("picow")));
        c.put("PICOWATT:TERAWATT", Mul(Pow(10, 24), Sym("picow")));
        c.put("PICOWATT:WATT", Mul(Pow(10, 12), Sym("picow")));
        c.put("PICOWATT:YOCTOWATT", Mul(Rat(Int(1), Pow(10, 12)), Sym("picow")));
        c.put("PICOWATT:YOTTAWATT", Mul(Pow(10, 36), Sym("picow")));
        c.put("PICOWATT:ZEPTOWATT", Mul(Rat(Int(1), Pow(10, 9)), Sym("picow")));
        c.put("PICOWATT:ZETTAWATT", Mul(Pow(10, 33), Sym("picow")));
        c.put("TERAWATT:ATTOWATT", Mul(Rat(Int(1), Pow(10, 30)), Sym("teraw")));
        c.put("TERAWATT:CENTIWATT", Mul(Rat(Int(1), Pow(10, 14)), Sym("teraw")));
        c.put("TERAWATT:DECAWATT", Mul(Rat(Int(1), Pow(10, 11)), Sym("teraw")));
        c.put("TERAWATT:DECIWATT", Mul(Rat(Int(1), Pow(10, 13)), Sym("teraw")));
        c.put("TERAWATT:EXAWATT", Mul(Pow(10, 6), Sym("teraw")));
        c.put("TERAWATT:FEMTOWATT", Mul(Rat(Int(1), Pow(10, 27)), Sym("teraw")));
        c.put("TERAWATT:GIGAWATT", Mul(Rat(Int(1), Int(1000)), Sym("teraw")));
        c.put("TERAWATT:HECTOWATT", Mul(Rat(Int(1), Pow(10, 10)), Sym("teraw")));
        c.put("TERAWATT:KILOWATT", Mul(Rat(Int(1), Pow(10, 9)), Sym("teraw")));
        c.put("TERAWATT:MEGAWATT", Mul(Rat(Int(1), Pow(10, 6)), Sym("teraw")));
        c.put("TERAWATT:MICROWATT", Mul(Rat(Int(1), Pow(10, 18)), Sym("teraw")));
        c.put("TERAWATT:MILLIWATT", Mul(Rat(Int(1), Pow(10, 15)), Sym("teraw")));
        c.put("TERAWATT:NANOWATT", Mul(Rat(Int(1), Pow(10, 21)), Sym("teraw")));
        c.put("TERAWATT:PETAWATT", Mul(Int(1000), Sym("teraw")));
        c.put("TERAWATT:PICOWATT", Mul(Rat(Int(1), Pow(10, 24)), Sym("teraw")));
        c.put("TERAWATT:WATT", Mul(Rat(Int(1), Pow(10, 12)), Sym("teraw")));
        c.put("TERAWATT:YOCTOWATT", Mul(Rat(Int(1), Pow(10, 36)), Sym("teraw")));
        c.put("TERAWATT:YOTTAWATT", Mul(Pow(10, 12), Sym("teraw")));
        c.put("TERAWATT:ZEPTOWATT", Mul(Rat(Int(1), Pow(10, 33)), Sym("teraw")));
        c.put("TERAWATT:ZETTAWATT", Mul(Pow(10, 9), Sym("teraw")));
        c.put("WATT:ATTOWATT", Mul(Rat(Int(1), Pow(10, 18)), Sym("w")));
        c.put("WATT:CENTIWATT", Mul(Rat(Int(1), Int(100)), Sym("w")));
        c.put("WATT:DECAWATT", Mul(Int(10), Sym("w")));
        c.put("WATT:DECIWATT", Mul(Rat(Int(1), Int(10)), Sym("w")));
        c.put("WATT:EXAWATT", Mul(Pow(10, 18), Sym("w")));
        c.put("WATT:FEMTOWATT", Mul(Rat(Int(1), Pow(10, 15)), Sym("w")));
        c.put("WATT:GIGAWATT", Mul(Pow(10, 9), Sym("w")));
        c.put("WATT:HECTOWATT", Mul(Int(100), Sym("w")));
        c.put("WATT:KILOWATT", Mul(Int(1000), Sym("w")));
        c.put("WATT:MEGAWATT", Mul(Pow(10, 6), Sym("w")));
        c.put("WATT:MICROWATT", Mul(Rat(Int(1), Pow(10, 6)), Sym("w")));
        c.put("WATT:MILLIWATT", Mul(Rat(Int(1), Int(1000)), Sym("w")));
        c.put("WATT:NANOWATT", Mul(Rat(Int(1), Pow(10, 9)), Sym("w")));
        c.put("WATT:PETAWATT", Mul(Pow(10, 15), Sym("w")));
        c.put("WATT:PICOWATT", Mul(Rat(Int(1), Pow(10, 12)), Sym("w")));
        c.put("WATT:TERAWATT", Mul(Pow(10, 12), Sym("w")));
        c.put("WATT:YOCTOWATT", Mul(Rat(Int(1), Pow(10, 24)), Sym("w")));
        c.put("WATT:YOTTAWATT", Mul(Pow(10, 24), Sym("w")));
        c.put("WATT:ZEPTOWATT", Mul(Rat(Int(1), Pow(10, 21)), Sym("w")));
        c.put("WATT:ZETTAWATT", Mul(Pow(10, 21), Sym("w")));
        c.put("YOCTOWATT:ATTOWATT", Mul(Pow(10, 6), Sym("yoctow")));
        c.put("YOCTOWATT:CENTIWATT", Mul(Pow(10, 22), Sym("yoctow")));
        c.put("YOCTOWATT:DECAWATT", Mul(Pow(10, 25), Sym("yoctow")));
        c.put("YOCTOWATT:DECIWATT", Mul(Pow(10, 23), Sym("yoctow")));
        c.put("YOCTOWATT:EXAWATT", Mul(Pow(10, 42), Sym("yoctow")));
        c.put("YOCTOWATT:FEMTOWATT", Mul(Pow(10, 9), Sym("yoctow")));
        c.put("YOCTOWATT:GIGAWATT", Mul(Pow(10, 33), Sym("yoctow")));
        c.put("YOCTOWATT:HECTOWATT", Mul(Pow(10, 26), Sym("yoctow")));
        c.put("YOCTOWATT:KILOWATT", Mul(Pow(10, 27), Sym("yoctow")));
        c.put("YOCTOWATT:MEGAWATT", Mul(Pow(10, 30), Sym("yoctow")));
        c.put("YOCTOWATT:MICROWATT", Mul(Pow(10, 18), Sym("yoctow")));
        c.put("YOCTOWATT:MILLIWATT", Mul(Pow(10, 21), Sym("yoctow")));
        c.put("YOCTOWATT:NANOWATT", Mul(Pow(10, 15), Sym("yoctow")));
        c.put("YOCTOWATT:PETAWATT", Mul(Pow(10, 39), Sym("yoctow")));
        c.put("YOCTOWATT:PICOWATT", Mul(Pow(10, 12), Sym("yoctow")));
        c.put("YOCTOWATT:TERAWATT", Mul(Pow(10, 36), Sym("yoctow")));
        c.put("YOCTOWATT:WATT", Mul(Pow(10, 24), Sym("yoctow")));
        c.put("YOCTOWATT:YOTTAWATT", Mul(Pow(10, 48), Sym("yoctow")));
        c.put("YOCTOWATT:ZEPTOWATT", Mul(Int(1000), Sym("yoctow")));
        c.put("YOCTOWATT:ZETTAWATT", Mul(Pow(10, 45), Sym("yoctow")));
        c.put("YOTTAWATT:ATTOWATT", Mul(Rat(Int(1), Pow(10, 42)), Sym("yottaw")));
        c.put("YOTTAWATT:CENTIWATT", Mul(Rat(Int(1), Pow(10, 26)), Sym("yottaw")));
        c.put("YOTTAWATT:DECAWATT", Mul(Rat(Int(1), Pow(10, 23)), Sym("yottaw")));
        c.put("YOTTAWATT:DECIWATT", Mul(Rat(Int(1), Pow(10, 25)), Sym("yottaw")));
        c.put("YOTTAWATT:EXAWATT", Mul(Rat(Int(1), Pow(10, 6)), Sym("yottaw")));
        c.put("YOTTAWATT:FEMTOWATT", Mul(Rat(Int(1), Pow(10, 39)), Sym("yottaw")));
        c.put("YOTTAWATT:GIGAWATT", Mul(Rat(Int(1), Pow(10, 15)), Sym("yottaw")));
        c.put("YOTTAWATT:HECTOWATT", Mul(Rat(Int(1), Pow(10, 22)), Sym("yottaw")));
        c.put("YOTTAWATT:KILOWATT", Mul(Rat(Int(1), Pow(10, 21)), Sym("yottaw")));
        c.put("YOTTAWATT:MEGAWATT", Mul(Rat(Int(1), Pow(10, 18)), Sym("yottaw")));
        c.put("YOTTAWATT:MICROWATT", Mul(Rat(Int(1), Pow(10, 30)), Sym("yottaw")));
        c.put("YOTTAWATT:MILLIWATT", Mul(Rat(Int(1), Pow(10, 27)), Sym("yottaw")));
        c.put("YOTTAWATT:NANOWATT", Mul(Rat(Int(1), Pow(10, 33)), Sym("yottaw")));
        c.put("YOTTAWATT:PETAWATT", Mul(Rat(Int(1), Pow(10, 9)), Sym("yottaw")));
        c.put("YOTTAWATT:PICOWATT", Mul(Rat(Int(1), Pow(10, 36)), Sym("yottaw")));
        c.put("YOTTAWATT:TERAWATT", Mul(Rat(Int(1), Pow(10, 12)), Sym("yottaw")));
        c.put("YOTTAWATT:WATT", Mul(Rat(Int(1), Pow(10, 24)), Sym("yottaw")));
        c.put("YOTTAWATT:YOCTOWATT", Mul(Rat(Int(1), Pow(10, 48)), Sym("yottaw")));
        c.put("YOTTAWATT:ZEPTOWATT", Mul(Rat(Int(1), Pow(10, 45)), Sym("yottaw")));
        c.put("YOTTAWATT:ZETTAWATT", Mul(Rat(Int(1), Int(1000)), Sym("yottaw")));
        c.put("ZEPTOWATT:ATTOWATT", Mul(Int(1000), Sym("zeptow")));
        c.put("ZEPTOWATT:CENTIWATT", Mul(Pow(10, 19), Sym("zeptow")));
        c.put("ZEPTOWATT:DECAWATT", Mul(Pow(10, 22), Sym("zeptow")));
        c.put("ZEPTOWATT:DECIWATT", Mul(Pow(10, 20), Sym("zeptow")));
        c.put("ZEPTOWATT:EXAWATT", Mul(Pow(10, 39), Sym("zeptow")));
        c.put("ZEPTOWATT:FEMTOWATT", Mul(Pow(10, 6), Sym("zeptow")));
        c.put("ZEPTOWATT:GIGAWATT", Mul(Pow(10, 30), Sym("zeptow")));
        c.put("ZEPTOWATT:HECTOWATT", Mul(Pow(10, 23), Sym("zeptow")));
        c.put("ZEPTOWATT:KILOWATT", Mul(Pow(10, 24), Sym("zeptow")));
        c.put("ZEPTOWATT:MEGAWATT", Mul(Pow(10, 27), Sym("zeptow")));
        c.put("ZEPTOWATT:MICROWATT", Mul(Pow(10, 15), Sym("zeptow")));
        c.put("ZEPTOWATT:MILLIWATT", Mul(Pow(10, 18), Sym("zeptow")));
        c.put("ZEPTOWATT:NANOWATT", Mul(Pow(10, 12), Sym("zeptow")));
        c.put("ZEPTOWATT:PETAWATT", Mul(Pow(10, 36), Sym("zeptow")));
        c.put("ZEPTOWATT:PICOWATT", Mul(Pow(10, 9), Sym("zeptow")));
        c.put("ZEPTOWATT:TERAWATT", Mul(Pow(10, 33), Sym("zeptow")));
        c.put("ZEPTOWATT:WATT", Mul(Pow(10, 21), Sym("zeptow")));
        c.put("ZEPTOWATT:YOCTOWATT", Mul(Rat(Int(1), Int(1000)), Sym("zeptow")));
        c.put("ZEPTOWATT:YOTTAWATT", Mul(Pow(10, 45), Sym("zeptow")));
        c.put("ZEPTOWATT:ZETTAWATT", Mul(Pow(10, 42), Sym("zeptow")));
        c.put("ZETTAWATT:ATTOWATT", Mul(Rat(Int(1), Pow(10, 39)), Sym("zettaw")));
        c.put("ZETTAWATT:CENTIWATT", Mul(Rat(Int(1), Pow(10, 23)), Sym("zettaw")));
        c.put("ZETTAWATT:DECAWATT", Mul(Rat(Int(1), Pow(10, 20)), Sym("zettaw")));
        c.put("ZETTAWATT:DECIWATT", Mul(Rat(Int(1), Pow(10, 22)), Sym("zettaw")));
        c.put("ZETTAWATT:EXAWATT", Mul(Rat(Int(1), Int(1000)), Sym("zettaw")));
        c.put("ZETTAWATT:FEMTOWATT", Mul(Rat(Int(1), Pow(10, 36)), Sym("zettaw")));
        c.put("ZETTAWATT:GIGAWATT", Mul(Rat(Int(1), Pow(10, 12)), Sym("zettaw")));
        c.put("ZETTAWATT:HECTOWATT", Mul(Rat(Int(1), Pow(10, 19)), Sym("zettaw")));
        c.put("ZETTAWATT:KILOWATT", Mul(Rat(Int(1), Pow(10, 18)), Sym("zettaw")));
        c.put("ZETTAWATT:MEGAWATT", Mul(Rat(Int(1), Pow(10, 15)), Sym("zettaw")));
        c.put("ZETTAWATT:MICROWATT", Mul(Rat(Int(1), Pow(10, 27)), Sym("zettaw")));
        c.put("ZETTAWATT:MILLIWATT", Mul(Rat(Int(1), Pow(10, 24)), Sym("zettaw")));
        c.put("ZETTAWATT:NANOWATT", Mul(Rat(Int(1), Pow(10, 30)), Sym("zettaw")));
        c.put("ZETTAWATT:PETAWATT", Mul(Rat(Int(1), Pow(10, 6)), Sym("zettaw")));
        c.put("ZETTAWATT:PICOWATT", Mul(Rat(Int(1), Pow(10, 33)), Sym("zettaw")));
        c.put("ZETTAWATT:TERAWATT", Mul(Rat(Int(1), Pow(10, 9)), Sym("zettaw")));
        c.put("ZETTAWATT:WATT", Mul(Rat(Int(1), Pow(10, 21)), Sym("zettaw")));
        c.put("ZETTAWATT:YOCTOWATT", Mul(Rat(Int(1), Pow(10, 45)), Sym("zettaw")));
        c.put("ZETTAWATT:YOTTAWATT", Mul(Int(1000), Sym("zettaw")));
        c.put("ZETTAWATT:ZEPTOWATT", Mul(Rat(Int(1), Pow(10, 42)), Sym("zettaw")));
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
            setUnit(UnitsPower.valueOf(target));
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

