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

    private static final Map<String, Conversion> conversions;
    static {
        Map<String, Conversion> c = new HashMap<String, Conversion>();

        c.put("ATTOSECOND:CENTISECOND", Mul(Pow(10, 16), Sym("attos")));
        c.put("ATTOSECOND:DAY", Mul(Mul(Int(864), Pow(10, 20)), Sym("attos")));
        c.put("ATTOSECOND:DECASECOND", Mul(Pow(10, 19), Sym("attos")));
        c.put("ATTOSECOND:DECISECOND", Mul(Pow(10, 17), Sym("attos")));
        c.put("ATTOSECOND:EXASECOND", Mul(Pow(10, 36), Sym("attos")));
        c.put("ATTOSECOND:FEMTOSECOND", Mul(Int(1000), Sym("attos")));
        c.put("ATTOSECOND:GIGASECOND", Mul(Pow(10, 27), Sym("attos")));
        c.put("ATTOSECOND:HECTOSECOND", Mul(Pow(10, 20), Sym("attos")));
        c.put("ATTOSECOND:HOUR", Mul(Mul(Int(36), Pow(10, 20)), Sym("attos")));
        c.put("ATTOSECOND:KILOSECOND", Mul(Pow(10, 21), Sym("attos")));
        c.put("ATTOSECOND:MEGASECOND", Mul(Pow(10, 24), Sym("attos")));
        c.put("ATTOSECOND:MICROSECOND", Mul(Pow(10, 12), Sym("attos")));
        c.put("ATTOSECOND:MILLISECOND", Mul(Pow(10, 15), Sym("attos")));
        c.put("ATTOSECOND:MINUTE", Mul(Mul(Int(6), Pow(10, 19)), Sym("attos")));
        c.put("ATTOSECOND:NANOSECOND", Mul(Pow(10, 9), Sym("attos")));
        c.put("ATTOSECOND:PETASECOND", Mul(Pow(10, 33), Sym("attos")));
        c.put("ATTOSECOND:PICOSECOND", Mul(Pow(10, 6), Sym("attos")));
        c.put("ATTOSECOND:SECOND", Mul(Pow(10, 18), Sym("attos")));
        c.put("ATTOSECOND:TERASECOND", Mul(Pow(10, 30), Sym("attos")));
        c.put("ATTOSECOND:YOCTOSECOND", Mul(Rat(Int(1), Pow(10, 6)), Sym("attos")));
        c.put("ATTOSECOND:YOTTASECOND", Mul(Pow(10, 42), Sym("attos")));
        c.put("ATTOSECOND:ZEPTOSECOND", Mul(Rat(Int(1), Int(1000)), Sym("attos")));
        c.put("ATTOSECOND:ZETTASECOND", Mul(Pow(10, 39), Sym("attos")));
        c.put("CENTISECOND:ATTOSECOND", Mul(Rat(Int(1), Pow(10, 16)), Sym("centis")));
        c.put("CENTISECOND:DAY", Mul(Mul(Int(864), Pow(10, 4)), Sym("centis")));
        c.put("CENTISECOND:DECASECOND", Mul(Int(1000), Sym("centis")));
        c.put("CENTISECOND:DECISECOND", Mul(Int(10), Sym("centis")));
        c.put("CENTISECOND:EXASECOND", Mul(Pow(10, 20), Sym("centis")));
        c.put("CENTISECOND:FEMTOSECOND", Mul(Rat(Int(1), Pow(10, 13)), Sym("centis")));
        c.put("CENTISECOND:GIGASECOND", Mul(Pow(10, 11), Sym("centis")));
        c.put("CENTISECOND:HECTOSECOND", Mul(Pow(10, 4), Sym("centis")));
        c.put("CENTISECOND:HOUR", Mul(Mul(Int(36), Pow(10, 4)), Sym("centis")));
        c.put("CENTISECOND:KILOSECOND", Mul(Pow(10, 5), Sym("centis")));
        c.put("CENTISECOND:MEGASECOND", Mul(Pow(10, 8), Sym("centis")));
        c.put("CENTISECOND:MICROSECOND", Mul(Rat(Int(1), Pow(10, 4)), Sym("centis")));
        c.put("CENTISECOND:MILLISECOND", Mul(Rat(Int(1), Int(10)), Sym("centis")));
        c.put("CENTISECOND:MINUTE", Mul(Int(6000), Sym("centis")));
        c.put("CENTISECOND:NANOSECOND", Mul(Rat(Int(1), Pow(10, 7)), Sym("centis")));
        c.put("CENTISECOND:PETASECOND", Mul(Pow(10, 17), Sym("centis")));
        c.put("CENTISECOND:PICOSECOND", Mul(Rat(Int(1), Pow(10, 10)), Sym("centis")));
        c.put("CENTISECOND:SECOND", Mul(Int(100), Sym("centis")));
        c.put("CENTISECOND:TERASECOND", Mul(Pow(10, 14), Sym("centis")));
        c.put("CENTISECOND:YOCTOSECOND", Mul(Rat(Int(1), Pow(10, 22)), Sym("centis")));
        c.put("CENTISECOND:YOTTASECOND", Mul(Pow(10, 26), Sym("centis")));
        c.put("CENTISECOND:ZEPTOSECOND", Mul(Rat(Int(1), Pow(10, 19)), Sym("centis")));
        c.put("CENTISECOND:ZETTASECOND", Mul(Pow(10, 23), Sym("centis")));
        c.put("DAY:ATTOSECOND", Mul(Rat(Int(1), Mul(Int(864), Pow(10, 20))), Sym("d")));
        c.put("DAY:CENTISECOND", Mul(Rat(Int(1), Mul(Int(864), Pow(10, 4))), Sym("d")));
        c.put("DAY:DECASECOND", Mul(Rat(Int(1), Int(8640)), Sym("d")));
        c.put("DAY:DECISECOND", Mul(Rat(Int(1), Int(864000)), Sym("d")));
        c.put("DAY:EXASECOND", Mul(Rat(Mul(Int(3125), Pow(10, 11)), Int(27)), Sym("d")));
        c.put("DAY:FEMTOSECOND", Mul(Rat(Int(1), Mul(Int(864), Pow(10, 17))), Sym("d")));
        c.put("DAY:GIGASECOND", Mul(Rat(Int(312500), Int(27)), Sym("d")));
        c.put("DAY:HECTOSECOND", Mul(Rat(Int(1), Int(864)), Sym("d")));
        c.put("DAY:HOUR", Mul(Rat(Int(1), Int(24)), Sym("d")));
        c.put("DAY:KILOSECOND", Mul(Rat(Int(5), Int(432)), Sym("d")));
        c.put("DAY:MEGASECOND", Mul(Rat(Int(625), Int(54)), Sym("d")));
        c.put("DAY:MICROSECOND", Mul(Rat(Int(1), Mul(Int(864), Pow(10, 8))), Sym("d")));
        c.put("DAY:MILLISECOND", Mul(Rat(Int(1), Mul(Int(864), Pow(10, 5))), Sym("d")));
        c.put("DAY:MINUTE", Mul(Rat(Int(1), Int(1440)), Sym("d")));
        c.put("DAY:NANOSECOND", Mul(Rat(Int(1), Mul(Int(864), Pow(10, 11))), Sym("d")));
        c.put("DAY:PETASECOND", Mul(Rat(Mul(Int(3125), Pow(10, 8)), Int(27)), Sym("d")));
        c.put("DAY:PICOSECOND", Mul(Rat(Int(1), Mul(Int(864), Pow(10, 14))), Sym("d")));
        c.put("DAY:SECOND", Mul(Rat(Int(1), Int(86400)), Sym("d")));
        c.put("DAY:TERASECOND", Mul(Rat(Mul(Int(3125), Pow(10, 5)), Int(27)), Sym("d")));
        c.put("DAY:YOCTOSECOND", Mul(Rat(Int(1), Mul(Int(864), Pow(10, 26))), Sym("d")));
        c.put("DAY:YOTTASECOND", Mul(Rat(Mul(Int(3125), Pow(10, 17)), Int(27)), Sym("d")));
        c.put("DAY:ZEPTOSECOND", Mul(Rat(Int(1), Mul(Int(864), Pow(10, 23))), Sym("d")));
        c.put("DAY:ZETTASECOND", Mul(Rat(Mul(Int(3125), Pow(10, 14)), Int(27)), Sym("d")));
        c.put("DECASECOND:ATTOSECOND", Mul(Rat(Int(1), Pow(10, 19)), Sym("decas")));
        c.put("DECASECOND:CENTISECOND", Mul(Rat(Int(1), Int(1000)), Sym("decas")));
        c.put("DECASECOND:DAY", Mul(Int(8640), Sym("decas")));
        c.put("DECASECOND:DECISECOND", Mul(Rat(Int(1), Int(100)), Sym("decas")));
        c.put("DECASECOND:EXASECOND", Mul(Pow(10, 17), Sym("decas")));
        c.put("DECASECOND:FEMTOSECOND", Mul(Rat(Int(1), Pow(10, 16)), Sym("decas")));
        c.put("DECASECOND:GIGASECOND", Mul(Pow(10, 8), Sym("decas")));
        c.put("DECASECOND:HECTOSECOND", Mul(Int(10), Sym("decas")));
        c.put("DECASECOND:HOUR", Mul(Int(360), Sym("decas")));
        c.put("DECASECOND:KILOSECOND", Mul(Int(100), Sym("decas")));
        c.put("DECASECOND:MEGASECOND", Mul(Pow(10, 5), Sym("decas")));
        c.put("DECASECOND:MICROSECOND", Mul(Rat(Int(1), Pow(10, 7)), Sym("decas")));
        c.put("DECASECOND:MILLISECOND", Mul(Rat(Int(1), Pow(10, 4)), Sym("decas")));
        c.put("DECASECOND:MINUTE", Mul(Int(6), Sym("decas")));
        c.put("DECASECOND:NANOSECOND", Mul(Rat(Int(1), Pow(10, 10)), Sym("decas")));
        c.put("DECASECOND:PETASECOND", Mul(Pow(10, 14), Sym("decas")));
        c.put("DECASECOND:PICOSECOND", Mul(Rat(Int(1), Pow(10, 13)), Sym("decas")));
        c.put("DECASECOND:SECOND", Mul(Rat(Int(1), Int(10)), Sym("decas")));
        c.put("DECASECOND:TERASECOND", Mul(Pow(10, 11), Sym("decas")));
        c.put("DECASECOND:YOCTOSECOND", Mul(Rat(Int(1), Pow(10, 25)), Sym("decas")));
        c.put("DECASECOND:YOTTASECOND", Mul(Pow(10, 23), Sym("decas")));
        c.put("DECASECOND:ZEPTOSECOND", Mul(Rat(Int(1), Pow(10, 22)), Sym("decas")));
        c.put("DECASECOND:ZETTASECOND", Mul(Pow(10, 20), Sym("decas")));
        c.put("DECISECOND:ATTOSECOND", Mul(Rat(Int(1), Pow(10, 17)), Sym("decis")));
        c.put("DECISECOND:CENTISECOND", Mul(Rat(Int(1), Int(10)), Sym("decis")));
        c.put("DECISECOND:DAY", Mul(Int(864000), Sym("decis")));
        c.put("DECISECOND:DECASECOND", Mul(Int(100), Sym("decis")));
        c.put("DECISECOND:EXASECOND", Mul(Pow(10, 19), Sym("decis")));
        c.put("DECISECOND:FEMTOSECOND", Mul(Rat(Int(1), Pow(10, 14)), Sym("decis")));
        c.put("DECISECOND:GIGASECOND", Mul(Pow(10, 10), Sym("decis")));
        c.put("DECISECOND:HECTOSECOND", Mul(Int(1000), Sym("decis")));
        c.put("DECISECOND:HOUR", Mul(Int(36000), Sym("decis")));
        c.put("DECISECOND:KILOSECOND", Mul(Pow(10, 4), Sym("decis")));
        c.put("DECISECOND:MEGASECOND", Mul(Pow(10, 7), Sym("decis")));
        c.put("DECISECOND:MICROSECOND", Mul(Rat(Int(1), Pow(10, 5)), Sym("decis")));
        c.put("DECISECOND:MILLISECOND", Mul(Rat(Int(1), Int(100)), Sym("decis")));
        c.put("DECISECOND:MINUTE", Mul(Int(600), Sym("decis")));
        c.put("DECISECOND:NANOSECOND", Mul(Rat(Int(1), Pow(10, 8)), Sym("decis")));
        c.put("DECISECOND:PETASECOND", Mul(Pow(10, 16), Sym("decis")));
        c.put("DECISECOND:PICOSECOND", Mul(Rat(Int(1), Pow(10, 11)), Sym("decis")));
        c.put("DECISECOND:SECOND", Mul(Int(10), Sym("decis")));
        c.put("DECISECOND:TERASECOND", Mul(Pow(10, 13), Sym("decis")));
        c.put("DECISECOND:YOCTOSECOND", Mul(Rat(Int(1), Pow(10, 23)), Sym("decis")));
        c.put("DECISECOND:YOTTASECOND", Mul(Pow(10, 25), Sym("decis")));
        c.put("DECISECOND:ZEPTOSECOND", Mul(Rat(Int(1), Pow(10, 20)), Sym("decis")));
        c.put("DECISECOND:ZETTASECOND", Mul(Pow(10, 22), Sym("decis")));
        c.put("EXASECOND:ATTOSECOND", Mul(Rat(Int(1), Pow(10, 36)), Sym("exas")));
        c.put("EXASECOND:CENTISECOND", Mul(Rat(Int(1), Pow(10, 20)), Sym("exas")));
        c.put("EXASECOND:DAY", Mul(Rat(Int(27), Mul(Int(3125), Pow(10, 11))), Sym("exas")));
        c.put("EXASECOND:DECASECOND", Mul(Rat(Int(1), Pow(10, 17)), Sym("exas")));
        c.put("EXASECOND:DECISECOND", Mul(Rat(Int(1), Pow(10, 19)), Sym("exas")));
        c.put("EXASECOND:FEMTOSECOND", Mul(Rat(Int(1), Pow(10, 33)), Sym("exas")));
        c.put("EXASECOND:GIGASECOND", Mul(Rat(Int(1), Pow(10, 9)), Sym("exas")));
        c.put("EXASECOND:HECTOSECOND", Mul(Rat(Int(1), Pow(10, 16)), Sym("exas")));
        c.put("EXASECOND:HOUR", Mul(Rat(Int(9), Mul(Int(25), Pow(10, 14))), Sym("exas")));
        c.put("EXASECOND:KILOSECOND", Mul(Rat(Int(1), Pow(10, 15)), Sym("exas")));
        c.put("EXASECOND:MEGASECOND", Mul(Rat(Int(1), Pow(10, 12)), Sym("exas")));
        c.put("EXASECOND:MICROSECOND", Mul(Rat(Int(1), Pow(10, 24)), Sym("exas")));
        c.put("EXASECOND:MILLISECOND", Mul(Rat(Int(1), Pow(10, 21)), Sym("exas")));
        c.put("EXASECOND:MINUTE", Mul(Rat(Int(3), Mul(Int(5), Pow(10, 16))), Sym("exas")));
        c.put("EXASECOND:NANOSECOND", Mul(Rat(Int(1), Pow(10, 27)), Sym("exas")));
        c.put("EXASECOND:PETASECOND", Mul(Rat(Int(1), Int(1000)), Sym("exas")));
        c.put("EXASECOND:PICOSECOND", Mul(Rat(Int(1), Pow(10, 30)), Sym("exas")));
        c.put("EXASECOND:SECOND", Mul(Rat(Int(1), Pow(10, 18)), Sym("exas")));
        c.put("EXASECOND:TERASECOND", Mul(Rat(Int(1), Pow(10, 6)), Sym("exas")));
        c.put("EXASECOND:YOCTOSECOND", Mul(Rat(Int(1), Pow(10, 42)), Sym("exas")));
        c.put("EXASECOND:YOTTASECOND", Mul(Pow(10, 6), Sym("exas")));
        c.put("EXASECOND:ZEPTOSECOND", Mul(Rat(Int(1), Pow(10, 39)), Sym("exas")));
        c.put("EXASECOND:ZETTASECOND", Mul(Int(1000), Sym("exas")));
        c.put("FEMTOSECOND:ATTOSECOND", Mul(Rat(Int(1), Int(1000)), Sym("femtos")));
        c.put("FEMTOSECOND:CENTISECOND", Mul(Pow(10, 13), Sym("femtos")));
        c.put("FEMTOSECOND:DAY", Mul(Mul(Int(864), Pow(10, 17)), Sym("femtos")));
        c.put("FEMTOSECOND:DECASECOND", Mul(Pow(10, 16), Sym("femtos")));
        c.put("FEMTOSECOND:DECISECOND", Mul(Pow(10, 14), Sym("femtos")));
        c.put("FEMTOSECOND:EXASECOND", Mul(Pow(10, 33), Sym("femtos")));
        c.put("FEMTOSECOND:GIGASECOND", Mul(Pow(10, 24), Sym("femtos")));
        c.put("FEMTOSECOND:HECTOSECOND", Mul(Pow(10, 17), Sym("femtos")));
        c.put("FEMTOSECOND:HOUR", Mul(Mul(Int(36), Pow(10, 17)), Sym("femtos")));
        c.put("FEMTOSECOND:KILOSECOND", Mul(Pow(10, 18), Sym("femtos")));
        c.put("FEMTOSECOND:MEGASECOND", Mul(Pow(10, 21), Sym("femtos")));
        c.put("FEMTOSECOND:MICROSECOND", Mul(Pow(10, 9), Sym("femtos")));
        c.put("FEMTOSECOND:MILLISECOND", Mul(Pow(10, 12), Sym("femtos")));
        c.put("FEMTOSECOND:MINUTE", Mul(Mul(Int(6), Pow(10, 16)), Sym("femtos")));
        c.put("FEMTOSECOND:NANOSECOND", Mul(Pow(10, 6), Sym("femtos")));
        c.put("FEMTOSECOND:PETASECOND", Mul(Pow(10, 30), Sym("femtos")));
        c.put("FEMTOSECOND:PICOSECOND", Mul(Int(1000), Sym("femtos")));
        c.put("FEMTOSECOND:SECOND", Mul(Pow(10, 15), Sym("femtos")));
        c.put("FEMTOSECOND:TERASECOND", Mul(Pow(10, 27), Sym("femtos")));
        c.put("FEMTOSECOND:YOCTOSECOND", Mul(Rat(Int(1), Pow(10, 9)), Sym("femtos")));
        c.put("FEMTOSECOND:YOTTASECOND", Mul(Pow(10, 39), Sym("femtos")));
        c.put("FEMTOSECOND:ZEPTOSECOND", Mul(Rat(Int(1), Pow(10, 6)), Sym("femtos")));
        c.put("FEMTOSECOND:ZETTASECOND", Mul(Pow(10, 36), Sym("femtos")));
        c.put("GIGASECOND:ATTOSECOND", Mul(Rat(Int(1), Pow(10, 27)), Sym("gigas")));
        c.put("GIGASECOND:CENTISECOND", Mul(Rat(Int(1), Pow(10, 11)), Sym("gigas")));
        c.put("GIGASECOND:DAY", Mul(Rat(Int(27), Int(312500)), Sym("gigas")));
        c.put("GIGASECOND:DECASECOND", Mul(Rat(Int(1), Pow(10, 8)), Sym("gigas")));
        c.put("GIGASECOND:DECISECOND", Mul(Rat(Int(1), Pow(10, 10)), Sym("gigas")));
        c.put("GIGASECOND:EXASECOND", Mul(Pow(10, 9), Sym("gigas")));
        c.put("GIGASECOND:FEMTOSECOND", Mul(Rat(Int(1), Pow(10, 24)), Sym("gigas")));
        c.put("GIGASECOND:HECTOSECOND", Mul(Rat(Int(1), Pow(10, 7)), Sym("gigas")));
        c.put("GIGASECOND:HOUR", Mul(Rat(Int(9), Mul(Int(25), Pow(10, 5))), Sym("gigas")));
        c.put("GIGASECOND:KILOSECOND", Mul(Rat(Int(1), Pow(10, 6)), Sym("gigas")));
        c.put("GIGASECOND:MEGASECOND", Mul(Rat(Int(1), Int(1000)), Sym("gigas")));
        c.put("GIGASECOND:MICROSECOND", Mul(Rat(Int(1), Pow(10, 15)), Sym("gigas")));
        c.put("GIGASECOND:MILLISECOND", Mul(Rat(Int(1), Pow(10, 12)), Sym("gigas")));
        c.put("GIGASECOND:MINUTE", Mul(Rat(Int(3), Mul(Int(5), Pow(10, 7))), Sym("gigas")));
        c.put("GIGASECOND:NANOSECOND", Mul(Rat(Int(1), Pow(10, 18)), Sym("gigas")));
        c.put("GIGASECOND:PETASECOND", Mul(Pow(10, 6), Sym("gigas")));
        c.put("GIGASECOND:PICOSECOND", Mul(Rat(Int(1), Pow(10, 21)), Sym("gigas")));
        c.put("GIGASECOND:SECOND", Mul(Rat(Int(1), Pow(10, 9)), Sym("gigas")));
        c.put("GIGASECOND:TERASECOND", Mul(Int(1000), Sym("gigas")));
        c.put("GIGASECOND:YOCTOSECOND", Mul(Rat(Int(1), Pow(10, 33)), Sym("gigas")));
        c.put("GIGASECOND:YOTTASECOND", Mul(Pow(10, 15), Sym("gigas")));
        c.put("GIGASECOND:ZEPTOSECOND", Mul(Rat(Int(1), Pow(10, 30)), Sym("gigas")));
        c.put("GIGASECOND:ZETTASECOND", Mul(Pow(10, 12), Sym("gigas")));
        c.put("HECTOSECOND:ATTOSECOND", Mul(Rat(Int(1), Pow(10, 20)), Sym("hectos")));
        c.put("HECTOSECOND:CENTISECOND", Mul(Rat(Int(1), Pow(10, 4)), Sym("hectos")));
        c.put("HECTOSECOND:DAY", Mul(Int(864), Sym("hectos")));
        c.put("HECTOSECOND:DECASECOND", Mul(Rat(Int(1), Int(10)), Sym("hectos")));
        c.put("HECTOSECOND:DECISECOND", Mul(Rat(Int(1), Int(1000)), Sym("hectos")));
        c.put("HECTOSECOND:EXASECOND", Mul(Pow(10, 16), Sym("hectos")));
        c.put("HECTOSECOND:FEMTOSECOND", Mul(Rat(Int(1), Pow(10, 17)), Sym("hectos")));
        c.put("HECTOSECOND:GIGASECOND", Mul(Pow(10, 7), Sym("hectos")));
        c.put("HECTOSECOND:HOUR", Mul(Int(36), Sym("hectos")));
        c.put("HECTOSECOND:KILOSECOND", Mul(Int(10), Sym("hectos")));
        c.put("HECTOSECOND:MEGASECOND", Mul(Pow(10, 4), Sym("hectos")));
        c.put("HECTOSECOND:MICROSECOND", Mul(Rat(Int(1), Pow(10, 8)), Sym("hectos")));
        c.put("HECTOSECOND:MILLISECOND", Mul(Rat(Int(1), Pow(10, 5)), Sym("hectos")));
        c.put("HECTOSECOND:MINUTE", Mul(Rat(Int(3), Int(5)), Sym("hectos")));
        c.put("HECTOSECOND:NANOSECOND", Mul(Rat(Int(1), Pow(10, 11)), Sym("hectos")));
        c.put("HECTOSECOND:PETASECOND", Mul(Pow(10, 13), Sym("hectos")));
        c.put("HECTOSECOND:PICOSECOND", Mul(Rat(Int(1), Pow(10, 14)), Sym("hectos")));
        c.put("HECTOSECOND:SECOND", Mul(Rat(Int(1), Int(100)), Sym("hectos")));
        c.put("HECTOSECOND:TERASECOND", Mul(Pow(10, 10), Sym("hectos")));
        c.put("HECTOSECOND:YOCTOSECOND", Mul(Rat(Int(1), Pow(10, 26)), Sym("hectos")));
        c.put("HECTOSECOND:YOTTASECOND", Mul(Pow(10, 22), Sym("hectos")));
        c.put("HECTOSECOND:ZEPTOSECOND", Mul(Rat(Int(1), Pow(10, 23)), Sym("hectos")));
        c.put("HECTOSECOND:ZETTASECOND", Mul(Pow(10, 19), Sym("hectos")));
        c.put("HOUR:ATTOSECOND", Mul(Rat(Int(1), Mul(Int(36), Pow(10, 20))), Sym("h")));
        c.put("HOUR:CENTISECOND", Mul(Rat(Int(1), Mul(Int(36), Pow(10, 4))), Sym("h")));
        c.put("HOUR:DAY", Mul(Int(24), Sym("h")));
        c.put("HOUR:DECASECOND", Mul(Rat(Int(1), Int(360)), Sym("h")));
        c.put("HOUR:DECISECOND", Mul(Rat(Int(1), Int(36000)), Sym("h")));
        c.put("HOUR:EXASECOND", Mul(Rat(Mul(Int(25), Pow(10, 14)), Int(9)), Sym("h")));
        c.put("HOUR:FEMTOSECOND", Mul(Rat(Int(1), Mul(Int(36), Pow(10, 17))), Sym("h")));
        c.put("HOUR:GIGASECOND", Mul(Rat(Mul(Int(25), Pow(10, 5)), Int(9)), Sym("h")));
        c.put("HOUR:HECTOSECOND", Mul(Rat(Int(1), Int(36)), Sym("h")));
        c.put("HOUR:KILOSECOND", Mul(Rat(Int(5), Int(18)), Sym("h")));
        c.put("HOUR:MEGASECOND", Mul(Rat(Int(2500), Int(9)), Sym("h")));
        c.put("HOUR:MICROSECOND", Mul(Rat(Int(1), Mul(Int(36), Pow(10, 8))), Sym("h")));
        c.put("HOUR:MILLISECOND", Mul(Rat(Int(1), Mul(Int(36), Pow(10, 5))), Sym("h")));
        c.put("HOUR:MINUTE", Mul(Rat(Int(1), Int(60)), Sym("h")));
        c.put("HOUR:NANOSECOND", Mul(Rat(Int(1), Mul(Int(36), Pow(10, 11))), Sym("h")));
        c.put("HOUR:PETASECOND", Mul(Rat(Mul(Int(25), Pow(10, 11)), Int(9)), Sym("h")));
        c.put("HOUR:PICOSECOND", Mul(Rat(Int(1), Mul(Int(36), Pow(10, 14))), Sym("h")));
        c.put("HOUR:SECOND", Mul(Rat(Int(1), Int(3600)), Sym("h")));
        c.put("HOUR:TERASECOND", Mul(Rat(Mul(Int(25), Pow(10, 8)), Int(9)), Sym("h")));
        c.put("HOUR:YOCTOSECOND", Mul(Rat(Int(1), Mul(Int(36), Pow(10, 26))), Sym("h")));
        c.put("HOUR:YOTTASECOND", Mul(Rat(Mul(Int(25), Pow(10, 20)), Int(9)), Sym("h")));
        c.put("HOUR:ZEPTOSECOND", Mul(Rat(Int(1), Mul(Int(36), Pow(10, 23))), Sym("h")));
        c.put("HOUR:ZETTASECOND", Mul(Rat(Mul(Int(25), Pow(10, 17)), Int(9)), Sym("h")));
        c.put("KILOSECOND:ATTOSECOND", Mul(Rat(Int(1), Pow(10, 21)), Sym("kilos")));
        c.put("KILOSECOND:CENTISECOND", Mul(Rat(Int(1), Pow(10, 5)), Sym("kilos")));
        c.put("KILOSECOND:DAY", Mul(Rat(Int(432), Int(5)), Sym("kilos")));
        c.put("KILOSECOND:DECASECOND", Mul(Rat(Int(1), Int(100)), Sym("kilos")));
        c.put("KILOSECOND:DECISECOND", Mul(Rat(Int(1), Pow(10, 4)), Sym("kilos")));
        c.put("KILOSECOND:EXASECOND", Mul(Pow(10, 15), Sym("kilos")));
        c.put("KILOSECOND:FEMTOSECOND", Mul(Rat(Int(1), Pow(10, 18)), Sym("kilos")));
        c.put("KILOSECOND:GIGASECOND", Mul(Pow(10, 6), Sym("kilos")));
        c.put("KILOSECOND:HECTOSECOND", Mul(Rat(Int(1), Int(10)), Sym("kilos")));
        c.put("KILOSECOND:HOUR", Mul(Rat(Int(18), Int(5)), Sym("kilos")));
        c.put("KILOSECOND:MEGASECOND", Mul(Int(1000), Sym("kilos")));
        c.put("KILOSECOND:MICROSECOND", Mul(Rat(Int(1), Pow(10, 9)), Sym("kilos")));
        c.put("KILOSECOND:MILLISECOND", Mul(Rat(Int(1), Pow(10, 6)), Sym("kilos")));
        c.put("KILOSECOND:MINUTE", Mul(Rat(Int(3), Int(50)), Sym("kilos")));
        c.put("KILOSECOND:NANOSECOND", Mul(Rat(Int(1), Pow(10, 12)), Sym("kilos")));
        c.put("KILOSECOND:PETASECOND", Mul(Pow(10, 12), Sym("kilos")));
        c.put("KILOSECOND:PICOSECOND", Mul(Rat(Int(1), Pow(10, 15)), Sym("kilos")));
        c.put("KILOSECOND:SECOND", Mul(Rat(Int(1), Int(1000)), Sym("kilos")));
        c.put("KILOSECOND:TERASECOND", Mul(Pow(10, 9), Sym("kilos")));
        c.put("KILOSECOND:YOCTOSECOND", Mul(Rat(Int(1), Pow(10, 27)), Sym("kilos")));
        c.put("KILOSECOND:YOTTASECOND", Mul(Pow(10, 21), Sym("kilos")));
        c.put("KILOSECOND:ZEPTOSECOND", Mul(Rat(Int(1), Pow(10, 24)), Sym("kilos")));
        c.put("KILOSECOND:ZETTASECOND", Mul(Pow(10, 18), Sym("kilos")));
        c.put("MEGASECOND:ATTOSECOND", Mul(Rat(Int(1), Pow(10, 24)), Sym("megas")));
        c.put("MEGASECOND:CENTISECOND", Mul(Rat(Int(1), Pow(10, 8)), Sym("megas")));
        c.put("MEGASECOND:DAY", Mul(Rat(Int(54), Int(625)), Sym("megas")));
        c.put("MEGASECOND:DECASECOND", Mul(Rat(Int(1), Pow(10, 5)), Sym("megas")));
        c.put("MEGASECOND:DECISECOND", Mul(Rat(Int(1), Pow(10, 7)), Sym("megas")));
        c.put("MEGASECOND:EXASECOND", Mul(Pow(10, 12), Sym("megas")));
        c.put("MEGASECOND:FEMTOSECOND", Mul(Rat(Int(1), Pow(10, 21)), Sym("megas")));
        c.put("MEGASECOND:GIGASECOND", Mul(Int(1000), Sym("megas")));
        c.put("MEGASECOND:HECTOSECOND", Mul(Rat(Int(1), Pow(10, 4)), Sym("megas")));
        c.put("MEGASECOND:HOUR", Mul(Rat(Int(9), Int(2500)), Sym("megas")));
        c.put("MEGASECOND:KILOSECOND", Mul(Rat(Int(1), Int(1000)), Sym("megas")));
        c.put("MEGASECOND:MICROSECOND", Mul(Rat(Int(1), Pow(10, 12)), Sym("megas")));
        c.put("MEGASECOND:MILLISECOND", Mul(Rat(Int(1), Pow(10, 9)), Sym("megas")));
        c.put("MEGASECOND:MINUTE", Mul(Rat(Int(3), Mul(Int(5), Pow(10, 4))), Sym("megas")));
        c.put("MEGASECOND:NANOSECOND", Mul(Rat(Int(1), Pow(10, 15)), Sym("megas")));
        c.put("MEGASECOND:PETASECOND", Mul(Pow(10, 9), Sym("megas")));
        c.put("MEGASECOND:PICOSECOND", Mul(Rat(Int(1), Pow(10, 18)), Sym("megas")));
        c.put("MEGASECOND:SECOND", Mul(Rat(Int(1), Pow(10, 6)), Sym("megas")));
        c.put("MEGASECOND:TERASECOND", Mul(Pow(10, 6), Sym("megas")));
        c.put("MEGASECOND:YOCTOSECOND", Mul(Rat(Int(1), Pow(10, 30)), Sym("megas")));
        c.put("MEGASECOND:YOTTASECOND", Mul(Pow(10, 18), Sym("megas")));
        c.put("MEGASECOND:ZEPTOSECOND", Mul(Rat(Int(1), Pow(10, 27)), Sym("megas")));
        c.put("MEGASECOND:ZETTASECOND", Mul(Pow(10, 15), Sym("megas")));
        c.put("MICROSECOND:ATTOSECOND", Mul(Rat(Int(1), Pow(10, 12)), Sym("micros")));
        c.put("MICROSECOND:CENTISECOND", Mul(Pow(10, 4), Sym("micros")));
        c.put("MICROSECOND:DAY", Mul(Mul(Int(864), Pow(10, 8)), Sym("micros")));
        c.put("MICROSECOND:DECASECOND", Mul(Pow(10, 7), Sym("micros")));
        c.put("MICROSECOND:DECISECOND", Mul(Pow(10, 5), Sym("micros")));
        c.put("MICROSECOND:EXASECOND", Mul(Pow(10, 24), Sym("micros")));
        c.put("MICROSECOND:FEMTOSECOND", Mul(Rat(Int(1), Pow(10, 9)), Sym("micros")));
        c.put("MICROSECOND:GIGASECOND", Mul(Pow(10, 15), Sym("micros")));
        c.put("MICROSECOND:HECTOSECOND", Mul(Pow(10, 8), Sym("micros")));
        c.put("MICROSECOND:HOUR", Mul(Mul(Int(36), Pow(10, 8)), Sym("micros")));
        c.put("MICROSECOND:KILOSECOND", Mul(Pow(10, 9), Sym("micros")));
        c.put("MICROSECOND:MEGASECOND", Mul(Pow(10, 12), Sym("micros")));
        c.put("MICROSECOND:MILLISECOND", Mul(Int(1000), Sym("micros")));
        c.put("MICROSECOND:MINUTE", Mul(Mul(Int(6), Pow(10, 7)), Sym("micros")));
        c.put("MICROSECOND:NANOSECOND", Mul(Rat(Int(1), Int(1000)), Sym("micros")));
        c.put("MICROSECOND:PETASECOND", Mul(Pow(10, 21), Sym("micros")));
        c.put("MICROSECOND:PICOSECOND", Mul(Rat(Int(1), Pow(10, 6)), Sym("micros")));
        c.put("MICROSECOND:SECOND", Mul(Pow(10, 6), Sym("micros")));
        c.put("MICROSECOND:TERASECOND", Mul(Pow(10, 18), Sym("micros")));
        c.put("MICROSECOND:YOCTOSECOND", Mul(Rat(Int(1), Pow(10, 18)), Sym("micros")));
        c.put("MICROSECOND:YOTTASECOND", Mul(Pow(10, 30), Sym("micros")));
        c.put("MICROSECOND:ZEPTOSECOND", Mul(Rat(Int(1), Pow(10, 15)), Sym("micros")));
        c.put("MICROSECOND:ZETTASECOND", Mul(Pow(10, 27), Sym("micros")));
        c.put("MILLISECOND:ATTOSECOND", Mul(Rat(Int(1), Pow(10, 15)), Sym("millis")));
        c.put("MILLISECOND:CENTISECOND", Mul(Int(10), Sym("millis")));
        c.put("MILLISECOND:DAY", Mul(Mul(Int(864), Pow(10, 5)), Sym("millis")));
        c.put("MILLISECOND:DECASECOND", Mul(Pow(10, 4), Sym("millis")));
        c.put("MILLISECOND:DECISECOND", Mul(Int(100), Sym("millis")));
        c.put("MILLISECOND:EXASECOND", Mul(Pow(10, 21), Sym("millis")));
        c.put("MILLISECOND:FEMTOSECOND", Mul(Rat(Int(1), Pow(10, 12)), Sym("millis")));
        c.put("MILLISECOND:GIGASECOND", Mul(Pow(10, 12), Sym("millis")));
        c.put("MILLISECOND:HECTOSECOND", Mul(Pow(10, 5), Sym("millis")));
        c.put("MILLISECOND:HOUR", Mul(Mul(Int(36), Pow(10, 5)), Sym("millis")));
        c.put("MILLISECOND:KILOSECOND", Mul(Pow(10, 6), Sym("millis")));
        c.put("MILLISECOND:MEGASECOND", Mul(Pow(10, 9), Sym("millis")));
        c.put("MILLISECOND:MICROSECOND", Mul(Rat(Int(1), Int(1000)), Sym("millis")));
        c.put("MILLISECOND:MINUTE", Mul(Mul(Int(6), Pow(10, 4)), Sym("millis")));
        c.put("MILLISECOND:NANOSECOND", Mul(Rat(Int(1), Pow(10, 6)), Sym("millis")));
        c.put("MILLISECOND:PETASECOND", Mul(Pow(10, 18), Sym("millis")));
        c.put("MILLISECOND:PICOSECOND", Mul(Rat(Int(1), Pow(10, 9)), Sym("millis")));
        c.put("MILLISECOND:SECOND", Mul(Int(1000), Sym("millis")));
        c.put("MILLISECOND:TERASECOND", Mul(Pow(10, 15), Sym("millis")));
        c.put("MILLISECOND:YOCTOSECOND", Mul(Rat(Int(1), Pow(10, 21)), Sym("millis")));
        c.put("MILLISECOND:YOTTASECOND", Mul(Pow(10, 27), Sym("millis")));
        c.put("MILLISECOND:ZEPTOSECOND", Mul(Rat(Int(1), Pow(10, 18)), Sym("millis")));
        c.put("MILLISECOND:ZETTASECOND", Mul(Pow(10, 24), Sym("millis")));
        c.put("MINUTE:ATTOSECOND", Mul(Rat(Int(1), Mul(Int(6), Pow(10, 19))), Sym("m")));
        c.put("MINUTE:CENTISECOND", Mul(Rat(Int(1), Int(6000)), Sym("m")));
        c.put("MINUTE:DAY", Mul(Int(1440), Sym("m")));
        c.put("MINUTE:DECASECOND", Mul(Rat(Int(1), Int(6)), Sym("m")));
        c.put("MINUTE:DECISECOND", Mul(Rat(Int(1), Int(600)), Sym("m")));
        c.put("MINUTE:EXASECOND", Mul(Rat(Mul(Int(5), Pow(10, 16)), Int(3)), Sym("m")));
        c.put("MINUTE:FEMTOSECOND", Mul(Rat(Int(1), Mul(Int(6), Pow(10, 16))), Sym("m")));
        c.put("MINUTE:GIGASECOND", Mul(Rat(Mul(Int(5), Pow(10, 7)), Int(3)), Sym("m")));
        c.put("MINUTE:HECTOSECOND", Mul(Rat(Int(5), Int(3)), Sym("m")));
        c.put("MINUTE:HOUR", Mul(Int(60), Sym("m")));
        c.put("MINUTE:KILOSECOND", Mul(Rat(Int(50), Int(3)), Sym("m")));
        c.put("MINUTE:MEGASECOND", Mul(Rat(Mul(Int(5), Pow(10, 4)), Int(3)), Sym("m")));
        c.put("MINUTE:MICROSECOND", Mul(Rat(Int(1), Mul(Int(6), Pow(10, 7))), Sym("m")));
        c.put("MINUTE:MILLISECOND", Mul(Rat(Int(1), Mul(Int(6), Pow(10, 4))), Sym("m")));
        c.put("MINUTE:NANOSECOND", Mul(Rat(Int(1), Mul(Int(6), Pow(10, 10))), Sym("m")));
        c.put("MINUTE:PETASECOND", Mul(Rat(Mul(Int(5), Pow(10, 13)), Int(3)), Sym("m")));
        c.put("MINUTE:PICOSECOND", Mul(Rat(Int(1), Mul(Int(6), Pow(10, 13))), Sym("m")));
        c.put("MINUTE:SECOND", Mul(Rat(Int(1), Int(60)), Sym("m")));
        c.put("MINUTE:TERASECOND", Mul(Rat(Mul(Int(5), Pow(10, 10)), Int(3)), Sym("m")));
        c.put("MINUTE:YOCTOSECOND", Mul(Rat(Int(1), Mul(Int(6), Pow(10, 25))), Sym("m")));
        c.put("MINUTE:YOTTASECOND", Mul(Rat(Mul(Int(5), Pow(10, 22)), Int(3)), Sym("m")));
        c.put("MINUTE:ZEPTOSECOND", Mul(Rat(Int(1), Mul(Int(6), Pow(10, 22))), Sym("m")));
        c.put("MINUTE:ZETTASECOND", Mul(Rat(Mul(Int(5), Pow(10, 19)), Int(3)), Sym("m")));
        c.put("NANOSECOND:ATTOSECOND", Mul(Rat(Int(1), Pow(10, 9)), Sym("nanos")));
        c.put("NANOSECOND:CENTISECOND", Mul(Pow(10, 7), Sym("nanos")));
        c.put("NANOSECOND:DAY", Mul(Mul(Int(864), Pow(10, 11)), Sym("nanos")));
        c.put("NANOSECOND:DECASECOND", Mul(Pow(10, 10), Sym("nanos")));
        c.put("NANOSECOND:DECISECOND", Mul(Pow(10, 8), Sym("nanos")));
        c.put("NANOSECOND:EXASECOND", Mul(Pow(10, 27), Sym("nanos")));
        c.put("NANOSECOND:FEMTOSECOND", Mul(Rat(Int(1), Pow(10, 6)), Sym("nanos")));
        c.put("NANOSECOND:GIGASECOND", Mul(Pow(10, 18), Sym("nanos")));
        c.put("NANOSECOND:HECTOSECOND", Mul(Pow(10, 11), Sym("nanos")));
        c.put("NANOSECOND:HOUR", Mul(Mul(Int(36), Pow(10, 11)), Sym("nanos")));
        c.put("NANOSECOND:KILOSECOND", Mul(Pow(10, 12), Sym("nanos")));
        c.put("NANOSECOND:MEGASECOND", Mul(Pow(10, 15), Sym("nanos")));
        c.put("NANOSECOND:MICROSECOND", Mul(Int(1000), Sym("nanos")));
        c.put("NANOSECOND:MILLISECOND", Mul(Pow(10, 6), Sym("nanos")));
        c.put("NANOSECOND:MINUTE", Mul(Mul(Int(6), Pow(10, 10)), Sym("nanos")));
        c.put("NANOSECOND:PETASECOND", Mul(Pow(10, 24), Sym("nanos")));
        c.put("NANOSECOND:PICOSECOND", Mul(Rat(Int(1), Int(1000)), Sym("nanos")));
        c.put("NANOSECOND:SECOND", Mul(Pow(10, 9), Sym("nanos")));
        c.put("NANOSECOND:TERASECOND", Mul(Pow(10, 21), Sym("nanos")));
        c.put("NANOSECOND:YOCTOSECOND", Mul(Rat(Int(1), Pow(10, 15)), Sym("nanos")));
        c.put("NANOSECOND:YOTTASECOND", Mul(Pow(10, 33), Sym("nanos")));
        c.put("NANOSECOND:ZEPTOSECOND", Mul(Rat(Int(1), Pow(10, 12)), Sym("nanos")));
        c.put("NANOSECOND:ZETTASECOND", Mul(Pow(10, 30), Sym("nanos")));
        c.put("PETASECOND:ATTOSECOND", Mul(Rat(Int(1), Pow(10, 33)), Sym("petas")));
        c.put("PETASECOND:CENTISECOND", Mul(Rat(Int(1), Pow(10, 17)), Sym("petas")));
        c.put("PETASECOND:DAY", Mul(Rat(Int(27), Mul(Int(3125), Pow(10, 8))), Sym("petas")));
        c.put("PETASECOND:DECASECOND", Mul(Rat(Int(1), Pow(10, 14)), Sym("petas")));
        c.put("PETASECOND:DECISECOND", Mul(Rat(Int(1), Pow(10, 16)), Sym("petas")));
        c.put("PETASECOND:EXASECOND", Mul(Int(1000), Sym("petas")));
        c.put("PETASECOND:FEMTOSECOND", Mul(Rat(Int(1), Pow(10, 30)), Sym("petas")));
        c.put("PETASECOND:GIGASECOND", Mul(Rat(Int(1), Pow(10, 6)), Sym("petas")));
        c.put("PETASECOND:HECTOSECOND", Mul(Rat(Int(1), Pow(10, 13)), Sym("petas")));
        c.put("PETASECOND:HOUR", Mul(Rat(Int(9), Mul(Int(25), Pow(10, 11))), Sym("petas")));
        c.put("PETASECOND:KILOSECOND", Mul(Rat(Int(1), Pow(10, 12)), Sym("petas")));
        c.put("PETASECOND:MEGASECOND", Mul(Rat(Int(1), Pow(10, 9)), Sym("petas")));
        c.put("PETASECOND:MICROSECOND", Mul(Rat(Int(1), Pow(10, 21)), Sym("petas")));
        c.put("PETASECOND:MILLISECOND", Mul(Rat(Int(1), Pow(10, 18)), Sym("petas")));
        c.put("PETASECOND:MINUTE", Mul(Rat(Int(3), Mul(Int(5), Pow(10, 13))), Sym("petas")));
        c.put("PETASECOND:NANOSECOND", Mul(Rat(Int(1), Pow(10, 24)), Sym("petas")));
        c.put("PETASECOND:PICOSECOND", Mul(Rat(Int(1), Pow(10, 27)), Sym("petas")));
        c.put("PETASECOND:SECOND", Mul(Rat(Int(1), Pow(10, 15)), Sym("petas")));
        c.put("PETASECOND:TERASECOND", Mul(Rat(Int(1), Int(1000)), Sym("petas")));
        c.put("PETASECOND:YOCTOSECOND", Mul(Rat(Int(1), Pow(10, 39)), Sym("petas")));
        c.put("PETASECOND:YOTTASECOND", Mul(Pow(10, 9), Sym("petas")));
        c.put("PETASECOND:ZEPTOSECOND", Mul(Rat(Int(1), Pow(10, 36)), Sym("petas")));
        c.put("PETASECOND:ZETTASECOND", Mul(Pow(10, 6), Sym("petas")));
        c.put("PICOSECOND:ATTOSECOND", Mul(Rat(Int(1), Pow(10, 6)), Sym("picos")));
        c.put("PICOSECOND:CENTISECOND", Mul(Pow(10, 10), Sym("picos")));
        c.put("PICOSECOND:DAY", Mul(Mul(Int(864), Pow(10, 14)), Sym("picos")));
        c.put("PICOSECOND:DECASECOND", Mul(Pow(10, 13), Sym("picos")));
        c.put("PICOSECOND:DECISECOND", Mul(Pow(10, 11), Sym("picos")));
        c.put("PICOSECOND:EXASECOND", Mul(Pow(10, 30), Sym("picos")));
        c.put("PICOSECOND:FEMTOSECOND", Mul(Rat(Int(1), Int(1000)), Sym("picos")));
        c.put("PICOSECOND:GIGASECOND", Mul(Pow(10, 21), Sym("picos")));
        c.put("PICOSECOND:HECTOSECOND", Mul(Pow(10, 14), Sym("picos")));
        c.put("PICOSECOND:HOUR", Mul(Mul(Int(36), Pow(10, 14)), Sym("picos")));
        c.put("PICOSECOND:KILOSECOND", Mul(Pow(10, 15), Sym("picos")));
        c.put("PICOSECOND:MEGASECOND", Mul(Pow(10, 18), Sym("picos")));
        c.put("PICOSECOND:MICROSECOND", Mul(Pow(10, 6), Sym("picos")));
        c.put("PICOSECOND:MILLISECOND", Mul(Pow(10, 9), Sym("picos")));
        c.put("PICOSECOND:MINUTE", Mul(Mul(Int(6), Pow(10, 13)), Sym("picos")));
        c.put("PICOSECOND:NANOSECOND", Mul(Int(1000), Sym("picos")));
        c.put("PICOSECOND:PETASECOND", Mul(Pow(10, 27), Sym("picos")));
        c.put("PICOSECOND:SECOND", Mul(Pow(10, 12), Sym("picos")));
        c.put("PICOSECOND:TERASECOND", Mul(Pow(10, 24), Sym("picos")));
        c.put("PICOSECOND:YOCTOSECOND", Mul(Rat(Int(1), Pow(10, 12)), Sym("picos")));
        c.put("PICOSECOND:YOTTASECOND", Mul(Pow(10, 36), Sym("picos")));
        c.put("PICOSECOND:ZEPTOSECOND", Mul(Rat(Int(1), Pow(10, 9)), Sym("picos")));
        c.put("PICOSECOND:ZETTASECOND", Mul(Pow(10, 33), Sym("picos")));
        c.put("SECOND:ATTOSECOND", Mul(Rat(Int(1), Pow(10, 18)), Sym("s")));
        c.put("SECOND:CENTISECOND", Mul(Rat(Int(1), Int(100)), Sym("s")));
        c.put("SECOND:DAY", Mul(Int(86400), Sym("s")));
        c.put("SECOND:DECASECOND", Mul(Int(10), Sym("s")));
        c.put("SECOND:DECISECOND", Mul(Rat(Int(1), Int(10)), Sym("s")));
        c.put("SECOND:EXASECOND", Mul(Pow(10, 18), Sym("s")));
        c.put("SECOND:FEMTOSECOND", Mul(Rat(Int(1), Pow(10, 15)), Sym("s")));
        c.put("SECOND:GIGASECOND", Mul(Pow(10, 9), Sym("s")));
        c.put("SECOND:HECTOSECOND", Mul(Int(100), Sym("s")));
        c.put("SECOND:HOUR", Mul(Int(3600), Sym("s")));
        c.put("SECOND:KILOSECOND", Mul(Int(1000), Sym("s")));
        c.put("SECOND:MEGASECOND", Mul(Pow(10, 6), Sym("s")));
        c.put("SECOND:MICROSECOND", Mul(Rat(Int(1), Pow(10, 6)), Sym("s")));
        c.put("SECOND:MILLISECOND", Mul(Rat(Int(1), Int(1000)), Sym("s")));
        c.put("SECOND:MINUTE", Mul(Int(60), Sym("s")));
        c.put("SECOND:NANOSECOND", Mul(Rat(Int(1), Pow(10, 9)), Sym("s")));
        c.put("SECOND:PETASECOND", Mul(Pow(10, 15), Sym("s")));
        c.put("SECOND:PICOSECOND", Mul(Rat(Int(1), Pow(10, 12)), Sym("s")));
        c.put("SECOND:TERASECOND", Mul(Pow(10, 12), Sym("s")));
        c.put("SECOND:YOCTOSECOND", Mul(Rat(Int(1), Pow(10, 24)), Sym("s")));
        c.put("SECOND:YOTTASECOND", Mul(Pow(10, 24), Sym("s")));
        c.put("SECOND:ZEPTOSECOND", Mul(Rat(Int(1), Pow(10, 21)), Sym("s")));
        c.put("SECOND:ZETTASECOND", Mul(Pow(10, 21), Sym("s")));
        c.put("TERASECOND:ATTOSECOND", Mul(Rat(Int(1), Pow(10, 30)), Sym("teras")));
        c.put("TERASECOND:CENTISECOND", Mul(Rat(Int(1), Pow(10, 14)), Sym("teras")));
        c.put("TERASECOND:DAY", Mul(Rat(Int(27), Mul(Int(3125), Pow(10, 5))), Sym("teras")));
        c.put("TERASECOND:DECASECOND", Mul(Rat(Int(1), Pow(10, 11)), Sym("teras")));
        c.put("TERASECOND:DECISECOND", Mul(Rat(Int(1), Pow(10, 13)), Sym("teras")));
        c.put("TERASECOND:EXASECOND", Mul(Pow(10, 6), Sym("teras")));
        c.put("TERASECOND:FEMTOSECOND", Mul(Rat(Int(1), Pow(10, 27)), Sym("teras")));
        c.put("TERASECOND:GIGASECOND", Mul(Rat(Int(1), Int(1000)), Sym("teras")));
        c.put("TERASECOND:HECTOSECOND", Mul(Rat(Int(1), Pow(10, 10)), Sym("teras")));
        c.put("TERASECOND:HOUR", Mul(Rat(Int(9), Mul(Int(25), Pow(10, 8))), Sym("teras")));
        c.put("TERASECOND:KILOSECOND", Mul(Rat(Int(1), Pow(10, 9)), Sym("teras")));
        c.put("TERASECOND:MEGASECOND", Mul(Rat(Int(1), Pow(10, 6)), Sym("teras")));
        c.put("TERASECOND:MICROSECOND", Mul(Rat(Int(1), Pow(10, 18)), Sym("teras")));
        c.put("TERASECOND:MILLISECOND", Mul(Rat(Int(1), Pow(10, 15)), Sym("teras")));
        c.put("TERASECOND:MINUTE", Mul(Rat(Int(3), Mul(Int(5), Pow(10, 10))), Sym("teras")));
        c.put("TERASECOND:NANOSECOND", Mul(Rat(Int(1), Pow(10, 21)), Sym("teras")));
        c.put("TERASECOND:PETASECOND", Mul(Int(1000), Sym("teras")));
        c.put("TERASECOND:PICOSECOND", Mul(Rat(Int(1), Pow(10, 24)), Sym("teras")));
        c.put("TERASECOND:SECOND", Mul(Rat(Int(1), Pow(10, 12)), Sym("teras")));
        c.put("TERASECOND:YOCTOSECOND", Mul(Rat(Int(1), Pow(10, 36)), Sym("teras")));
        c.put("TERASECOND:YOTTASECOND", Mul(Pow(10, 12), Sym("teras")));
        c.put("TERASECOND:ZEPTOSECOND", Mul(Rat(Int(1), Pow(10, 33)), Sym("teras")));
        c.put("TERASECOND:ZETTASECOND", Mul(Pow(10, 9), Sym("teras")));
        c.put("YOCTOSECOND:ATTOSECOND", Mul(Pow(10, 6), Sym("yoctos")));
        c.put("YOCTOSECOND:CENTISECOND", Mul(Pow(10, 22), Sym("yoctos")));
        c.put("YOCTOSECOND:DAY", Mul(Mul(Int(864), Pow(10, 26)), Sym("yoctos")));
        c.put("YOCTOSECOND:DECASECOND", Mul(Pow(10, 25), Sym("yoctos")));
        c.put("YOCTOSECOND:DECISECOND", Mul(Pow(10, 23), Sym("yoctos")));
        c.put("YOCTOSECOND:EXASECOND", Mul(Pow(10, 42), Sym("yoctos")));
        c.put("YOCTOSECOND:FEMTOSECOND", Mul(Pow(10, 9), Sym("yoctos")));
        c.put("YOCTOSECOND:GIGASECOND", Mul(Pow(10, 33), Sym("yoctos")));
        c.put("YOCTOSECOND:HECTOSECOND", Mul(Pow(10, 26), Sym("yoctos")));
        c.put("YOCTOSECOND:HOUR", Mul(Mul(Int(36), Pow(10, 26)), Sym("yoctos")));
        c.put("YOCTOSECOND:KILOSECOND", Mul(Pow(10, 27), Sym("yoctos")));
        c.put("YOCTOSECOND:MEGASECOND", Mul(Pow(10, 30), Sym("yoctos")));
        c.put("YOCTOSECOND:MICROSECOND", Mul(Pow(10, 18), Sym("yoctos")));
        c.put("YOCTOSECOND:MILLISECOND", Mul(Pow(10, 21), Sym("yoctos")));
        c.put("YOCTOSECOND:MINUTE", Mul(Mul(Int(6), Pow(10, 25)), Sym("yoctos")));
        c.put("YOCTOSECOND:NANOSECOND", Mul(Pow(10, 15), Sym("yoctos")));
        c.put("YOCTOSECOND:PETASECOND", Mul(Pow(10, 39), Sym("yoctos")));
        c.put("YOCTOSECOND:PICOSECOND", Mul(Pow(10, 12), Sym("yoctos")));
        c.put("YOCTOSECOND:SECOND", Mul(Pow(10, 24), Sym("yoctos")));
        c.put("YOCTOSECOND:TERASECOND", Mul(Pow(10, 36), Sym("yoctos")));
        c.put("YOCTOSECOND:YOTTASECOND", Mul(Pow(10, 48), Sym("yoctos")));
        c.put("YOCTOSECOND:ZEPTOSECOND", Mul(Int(1000), Sym("yoctos")));
        c.put("YOCTOSECOND:ZETTASECOND", Mul(Pow(10, 45), Sym("yoctos")));
        c.put("YOTTASECOND:ATTOSECOND", Mul(Rat(Int(1), Pow(10, 42)), Sym("yottas")));
        c.put("YOTTASECOND:CENTISECOND", Mul(Rat(Int(1), Pow(10, 26)), Sym("yottas")));
        c.put("YOTTASECOND:DAY", Mul(Rat(Int(27), Mul(Int(3125), Pow(10, 17))), Sym("yottas")));
        c.put("YOTTASECOND:DECASECOND", Mul(Rat(Int(1), Pow(10, 23)), Sym("yottas")));
        c.put("YOTTASECOND:DECISECOND", Mul(Rat(Int(1), Pow(10, 25)), Sym("yottas")));
        c.put("YOTTASECOND:EXASECOND", Mul(Rat(Int(1), Pow(10, 6)), Sym("yottas")));
        c.put("YOTTASECOND:FEMTOSECOND", Mul(Rat(Int(1), Pow(10, 39)), Sym("yottas")));
        c.put("YOTTASECOND:GIGASECOND", Mul(Rat(Int(1), Pow(10, 15)), Sym("yottas")));
        c.put("YOTTASECOND:HECTOSECOND", Mul(Rat(Int(1), Pow(10, 22)), Sym("yottas")));
        c.put("YOTTASECOND:HOUR", Mul(Rat(Int(9), Mul(Int(25), Pow(10, 20))), Sym("yottas")));
        c.put("YOTTASECOND:KILOSECOND", Mul(Rat(Int(1), Pow(10, 21)), Sym("yottas")));
        c.put("YOTTASECOND:MEGASECOND", Mul(Rat(Int(1), Pow(10, 18)), Sym("yottas")));
        c.put("YOTTASECOND:MICROSECOND", Mul(Rat(Int(1), Pow(10, 30)), Sym("yottas")));
        c.put("YOTTASECOND:MILLISECOND", Mul(Rat(Int(1), Pow(10, 27)), Sym("yottas")));
        c.put("YOTTASECOND:MINUTE", Mul(Rat(Int(3), Mul(Int(5), Pow(10, 22))), Sym("yottas")));
        c.put("YOTTASECOND:NANOSECOND", Mul(Rat(Int(1), Pow(10, 33)), Sym("yottas")));
        c.put("YOTTASECOND:PETASECOND", Mul(Rat(Int(1), Pow(10, 9)), Sym("yottas")));
        c.put("YOTTASECOND:PICOSECOND", Mul(Rat(Int(1), Pow(10, 36)), Sym("yottas")));
        c.put("YOTTASECOND:SECOND", Mul(Rat(Int(1), Pow(10, 24)), Sym("yottas")));
        c.put("YOTTASECOND:TERASECOND", Mul(Rat(Int(1), Pow(10, 12)), Sym("yottas")));
        c.put("YOTTASECOND:YOCTOSECOND", Mul(Rat(Int(1), Pow(10, 48)), Sym("yottas")));
        c.put("YOTTASECOND:ZEPTOSECOND", Mul(Rat(Int(1), Pow(10, 45)), Sym("yottas")));
        c.put("YOTTASECOND:ZETTASECOND", Mul(Rat(Int(1), Int(1000)), Sym("yottas")));
        c.put("ZEPTOSECOND:ATTOSECOND", Mul(Int(1000), Sym("zeptos")));
        c.put("ZEPTOSECOND:CENTISECOND", Mul(Pow(10, 19), Sym("zeptos")));
        c.put("ZEPTOSECOND:DAY", Mul(Mul(Int(864), Pow(10, 23)), Sym("zeptos")));
        c.put("ZEPTOSECOND:DECASECOND", Mul(Pow(10, 22), Sym("zeptos")));
        c.put("ZEPTOSECOND:DECISECOND", Mul(Pow(10, 20), Sym("zeptos")));
        c.put("ZEPTOSECOND:EXASECOND", Mul(Pow(10, 39), Sym("zeptos")));
        c.put("ZEPTOSECOND:FEMTOSECOND", Mul(Pow(10, 6), Sym("zeptos")));
        c.put("ZEPTOSECOND:GIGASECOND", Mul(Pow(10, 30), Sym("zeptos")));
        c.put("ZEPTOSECOND:HECTOSECOND", Mul(Pow(10, 23), Sym("zeptos")));
        c.put("ZEPTOSECOND:HOUR", Mul(Mul(Int(36), Pow(10, 23)), Sym("zeptos")));
        c.put("ZEPTOSECOND:KILOSECOND", Mul(Pow(10, 24), Sym("zeptos")));
        c.put("ZEPTOSECOND:MEGASECOND", Mul(Pow(10, 27), Sym("zeptos")));
        c.put("ZEPTOSECOND:MICROSECOND", Mul(Pow(10, 15), Sym("zeptos")));
        c.put("ZEPTOSECOND:MILLISECOND", Mul(Pow(10, 18), Sym("zeptos")));
        c.put("ZEPTOSECOND:MINUTE", Mul(Mul(Int(6), Pow(10, 22)), Sym("zeptos")));
        c.put("ZEPTOSECOND:NANOSECOND", Mul(Pow(10, 12), Sym("zeptos")));
        c.put("ZEPTOSECOND:PETASECOND", Mul(Pow(10, 36), Sym("zeptos")));
        c.put("ZEPTOSECOND:PICOSECOND", Mul(Pow(10, 9), Sym("zeptos")));
        c.put("ZEPTOSECOND:SECOND", Mul(Pow(10, 21), Sym("zeptos")));
        c.put("ZEPTOSECOND:TERASECOND", Mul(Pow(10, 33), Sym("zeptos")));
        c.put("ZEPTOSECOND:YOCTOSECOND", Mul(Rat(Int(1), Int(1000)), Sym("zeptos")));
        c.put("ZEPTOSECOND:YOTTASECOND", Mul(Pow(10, 45), Sym("zeptos")));
        c.put("ZEPTOSECOND:ZETTASECOND", Mul(Pow(10, 42), Sym("zeptos")));
        c.put("ZETTASECOND:ATTOSECOND", Mul(Rat(Int(1), Pow(10, 39)), Sym("zettas")));
        c.put("ZETTASECOND:CENTISECOND", Mul(Rat(Int(1), Pow(10, 23)), Sym("zettas")));
        c.put("ZETTASECOND:DAY", Mul(Rat(Int(27), Mul(Int(3125), Pow(10, 14))), Sym("zettas")));
        c.put("ZETTASECOND:DECASECOND", Mul(Rat(Int(1), Pow(10, 20)), Sym("zettas")));
        c.put("ZETTASECOND:DECISECOND", Mul(Rat(Int(1), Pow(10, 22)), Sym("zettas")));
        c.put("ZETTASECOND:EXASECOND", Mul(Rat(Int(1), Int(1000)), Sym("zettas")));
        c.put("ZETTASECOND:FEMTOSECOND", Mul(Rat(Int(1), Pow(10, 36)), Sym("zettas")));
        c.put("ZETTASECOND:GIGASECOND", Mul(Rat(Int(1), Pow(10, 12)), Sym("zettas")));
        c.put("ZETTASECOND:HECTOSECOND", Mul(Rat(Int(1), Pow(10, 19)), Sym("zettas")));
        c.put("ZETTASECOND:HOUR", Mul(Rat(Int(9), Mul(Int(25), Pow(10, 17))), Sym("zettas")));
        c.put("ZETTASECOND:KILOSECOND", Mul(Rat(Int(1), Pow(10, 18)), Sym("zettas")));
        c.put("ZETTASECOND:MEGASECOND", Mul(Rat(Int(1), Pow(10, 15)), Sym("zettas")));
        c.put("ZETTASECOND:MICROSECOND", Mul(Rat(Int(1), Pow(10, 27)), Sym("zettas")));
        c.put("ZETTASECOND:MILLISECOND", Mul(Rat(Int(1), Pow(10, 24)), Sym("zettas")));
        c.put("ZETTASECOND:MINUTE", Mul(Rat(Int(3), Mul(Int(5), Pow(10, 19))), Sym("zettas")));
        c.put("ZETTASECOND:NANOSECOND", Mul(Rat(Int(1), Pow(10, 30)), Sym("zettas")));
        c.put("ZETTASECOND:PETASECOND", Mul(Rat(Int(1), Pow(10, 6)), Sym("zettas")));
        c.put("ZETTASECOND:PICOSECOND", Mul(Rat(Int(1), Pow(10, 33)), Sym("zettas")));
        c.put("ZETTASECOND:SECOND", Mul(Rat(Int(1), Pow(10, 21)), Sym("zettas")));
        c.put("ZETTASECOND:TERASECOND", Mul(Rat(Int(1), Pow(10, 9)), Sym("zettas")));
        c.put("ZETTASECOND:YOCTOSECOND", Mul(Rat(Int(1), Pow(10, 45)), Sym("zettas")));
        c.put("ZETTASECOND:YOTTASECOND", Mul(Int(1000), Sym("zettas")));
        c.put("ZETTASECOND:ZEPTOSECOND", Mul(Rat(Int(1), Pow(10, 42)), Sym("zettas")));
        conversions = Collections.unmodifiableMap(c);
    }

    private static final Map<UnitsTime, String> SYMBOLS;
    static {
        Map<UnitsTime, String> s = new HashMap<UnitsTime, String>();
        s.put(UnitsTime.ATOOSECOND, "as");
        s.put(UnitsTime.CENTISECOND, "cs");
        s.put(UnitsTime.DAY, "d");
        s.put(UnitsTime.DECASECOND, "das");
        s.put(UnitsTime.DECISECOND, "ds");
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
   public TimeI(Time value, Unit<ome.units.quantity.Time> ul) throws BigResult {
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
    public TimeI(Time value, String target) throws BigResult {
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
            setUnit(UnitsTime.valueOf(target));
       }
    }

   /**
    * Copy constructor that converts between units if possible.
    *
    * @param target unit that is desired. non-null.
    */
    public TimeI(Time value, UnitsTime target) throws BigResult {
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

