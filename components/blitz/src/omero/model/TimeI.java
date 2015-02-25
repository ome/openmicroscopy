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

    private static final Map<UnitsTime, Map<UnitsTime, Conversion>> conversions;
    static {

        EnumMap<UnitsTime, EnumMap<UnitsTime, Conversion>> c
            = new EnumMap<UnitsTime, EnumMap<UnitsTime, Conversion>>(UnitsTime.class);

        for (UnitsTime e : UnitsTime.values()) {
            c.put(e, new EnumMap<UnitsTime, Conversion>(UnitsTime.class));
        }

        c.get(UnitsTime.ATTOSECOND).put(UnitsTime.CENTISECOND, Mul(Pow(10, 16), Sym("attos")));
        c.get(UnitsTime.ATTOSECOND).put(UnitsTime.DAY, Mul(Mul(Int(864), Pow(10, 20)), Sym("attos")));
        c.get(UnitsTime.ATTOSECOND).put(UnitsTime.DECASECOND, Mul(Pow(10, 19), Sym("attos")));
        c.get(UnitsTime.ATTOSECOND).put(UnitsTime.DECISECOND, Mul(Pow(10, 17), Sym("attos")));
        c.get(UnitsTime.ATTOSECOND).put(UnitsTime.EXASECOND, Mul(Pow(10, 36), Sym("attos")));
        c.get(UnitsTime.ATTOSECOND).put(UnitsTime.FEMTOSECOND, Mul(Int(1000), Sym("attos")));
        c.get(UnitsTime.ATTOSECOND).put(UnitsTime.GIGASECOND, Mul(Pow(10, 27), Sym("attos")));
        c.get(UnitsTime.ATTOSECOND).put(UnitsTime.HECTOSECOND, Mul(Pow(10, 20), Sym("attos")));
        c.get(UnitsTime.ATTOSECOND).put(UnitsTime.HOUR, Mul(Mul(Int(36), Pow(10, 20)), Sym("attos")));
        c.get(UnitsTime.ATTOSECOND).put(UnitsTime.KILOSECOND, Mul(Pow(10, 21), Sym("attos")));
        c.get(UnitsTime.ATTOSECOND).put(UnitsTime.MEGASECOND, Mul(Pow(10, 24), Sym("attos")));
        c.get(UnitsTime.ATTOSECOND).put(UnitsTime.MICROSECOND, Mul(Pow(10, 12), Sym("attos")));
        c.get(UnitsTime.ATTOSECOND).put(UnitsTime.MILLISECOND, Mul(Pow(10, 15), Sym("attos")));
        c.get(UnitsTime.ATTOSECOND).put(UnitsTime.MINUTE, Mul(Mul(Int(6), Pow(10, 19)), Sym("attos")));
        c.get(UnitsTime.ATTOSECOND).put(UnitsTime.NANOSECOND, Mul(Pow(10, 9), Sym("attos")));
        c.get(UnitsTime.ATTOSECOND).put(UnitsTime.PETASECOND, Mul(Pow(10, 33), Sym("attos")));
        c.get(UnitsTime.ATTOSECOND).put(UnitsTime.PICOSECOND, Mul(Pow(10, 6), Sym("attos")));
        c.get(UnitsTime.ATTOSECOND).put(UnitsTime.SECOND, Mul(Pow(10, 18), Sym("attos")));
        c.get(UnitsTime.ATTOSECOND).put(UnitsTime.TERASECOND, Mul(Pow(10, 30), Sym("attos")));
        c.get(UnitsTime.ATTOSECOND).put(UnitsTime.YOCTOSECOND, Mul(Rat(Int(1), Pow(10, 6)), Sym("attos")));
        c.get(UnitsTime.ATTOSECOND).put(UnitsTime.YOTTASECOND, Mul(Pow(10, 42), Sym("attos")));
        c.get(UnitsTime.ATTOSECOND).put(UnitsTime.ZEPTOSECOND, Mul(Rat(Int(1), Int(1000)), Sym("attos")));
        c.get(UnitsTime.ATTOSECOND).put(UnitsTime.ZETTASECOND, Mul(Pow(10, 39), Sym("attos")));
        c.get(UnitsTime.CENTISECOND).put(UnitsTime.ATTOSECOND, Mul(Rat(Int(1), Pow(10, 16)), Sym("centis")));
        c.get(UnitsTime.CENTISECOND).put(UnitsTime.DAY, Mul(Mul(Int(864), Pow(10, 4)), Sym("centis")));
        c.get(UnitsTime.CENTISECOND).put(UnitsTime.DECASECOND, Mul(Int(1000), Sym("centis")));
        c.get(UnitsTime.CENTISECOND).put(UnitsTime.DECISECOND, Mul(Int(10), Sym("centis")));
        c.get(UnitsTime.CENTISECOND).put(UnitsTime.EXASECOND, Mul(Pow(10, 20), Sym("centis")));
        c.get(UnitsTime.CENTISECOND).put(UnitsTime.FEMTOSECOND, Mul(Rat(Int(1), Pow(10, 13)), Sym("centis")));
        c.get(UnitsTime.CENTISECOND).put(UnitsTime.GIGASECOND, Mul(Pow(10, 11), Sym("centis")));
        c.get(UnitsTime.CENTISECOND).put(UnitsTime.HECTOSECOND, Mul(Pow(10, 4), Sym("centis")));
        c.get(UnitsTime.CENTISECOND).put(UnitsTime.HOUR, Mul(Mul(Int(36), Pow(10, 4)), Sym("centis")));
        c.get(UnitsTime.CENTISECOND).put(UnitsTime.KILOSECOND, Mul(Pow(10, 5), Sym("centis")));
        c.get(UnitsTime.CENTISECOND).put(UnitsTime.MEGASECOND, Mul(Pow(10, 8), Sym("centis")));
        c.get(UnitsTime.CENTISECOND).put(UnitsTime.MICROSECOND, Mul(Rat(Int(1), Pow(10, 4)), Sym("centis")));
        c.get(UnitsTime.CENTISECOND).put(UnitsTime.MILLISECOND, Mul(Rat(Int(1), Int(10)), Sym("centis")));
        c.get(UnitsTime.CENTISECOND).put(UnitsTime.MINUTE, Mul(Int(6000), Sym("centis")));
        c.get(UnitsTime.CENTISECOND).put(UnitsTime.NANOSECOND, Mul(Rat(Int(1), Pow(10, 7)), Sym("centis")));
        c.get(UnitsTime.CENTISECOND).put(UnitsTime.PETASECOND, Mul(Pow(10, 17), Sym("centis")));
        c.get(UnitsTime.CENTISECOND).put(UnitsTime.PICOSECOND, Mul(Rat(Int(1), Pow(10, 10)), Sym("centis")));
        c.get(UnitsTime.CENTISECOND).put(UnitsTime.SECOND, Mul(Int(100), Sym("centis")));
        c.get(UnitsTime.CENTISECOND).put(UnitsTime.TERASECOND, Mul(Pow(10, 14), Sym("centis")));
        c.get(UnitsTime.CENTISECOND).put(UnitsTime.YOCTOSECOND, Mul(Rat(Int(1), Pow(10, 22)), Sym("centis")));
        c.get(UnitsTime.CENTISECOND).put(UnitsTime.YOTTASECOND, Mul(Pow(10, 26), Sym("centis")));
        c.get(UnitsTime.CENTISECOND).put(UnitsTime.ZEPTOSECOND, Mul(Rat(Int(1), Pow(10, 19)), Sym("centis")));
        c.get(UnitsTime.CENTISECOND).put(UnitsTime.ZETTASECOND, Mul(Pow(10, 23), Sym("centis")));
        c.get(UnitsTime.DAY).put(UnitsTime.ATTOSECOND, Mul(Rat(Int(1), Mul(Int(864), Pow(10, 20))), Sym("d")));
        c.get(UnitsTime.DAY).put(UnitsTime.CENTISECOND, Mul(Rat(Int(1), Mul(Int(864), Pow(10, 4))), Sym("d")));
        c.get(UnitsTime.DAY).put(UnitsTime.DECASECOND, Mul(Rat(Int(1), Int(8640)), Sym("d")));
        c.get(UnitsTime.DAY).put(UnitsTime.DECISECOND, Mul(Rat(Int(1), Int(864000)), Sym("d")));
        c.get(UnitsTime.DAY).put(UnitsTime.EXASECOND, Mul(Rat(Mul(Int(3125), Pow(10, 11)), Int(27)), Sym("d")));
        c.get(UnitsTime.DAY).put(UnitsTime.FEMTOSECOND, Mul(Rat(Int(1), Mul(Int(864), Pow(10, 17))), Sym("d")));
        c.get(UnitsTime.DAY).put(UnitsTime.GIGASECOND, Mul(Rat(Int(312500), Int(27)), Sym("d")));
        c.get(UnitsTime.DAY).put(UnitsTime.HECTOSECOND, Mul(Rat(Int(1), Int(864)), Sym("d")));
        c.get(UnitsTime.DAY).put(UnitsTime.HOUR, Mul(Rat(Int(1), Int(24)), Sym("d")));
        c.get(UnitsTime.DAY).put(UnitsTime.KILOSECOND, Mul(Rat(Int(5), Int(432)), Sym("d")));
        c.get(UnitsTime.DAY).put(UnitsTime.MEGASECOND, Mul(Rat(Int(625), Int(54)), Sym("d")));
        c.get(UnitsTime.DAY).put(UnitsTime.MICROSECOND, Mul(Rat(Int(1), Mul(Int(864), Pow(10, 8))), Sym("d")));
        c.get(UnitsTime.DAY).put(UnitsTime.MILLISECOND, Mul(Rat(Int(1), Mul(Int(864), Pow(10, 5))), Sym("d")));
        c.get(UnitsTime.DAY).put(UnitsTime.MINUTE, Mul(Rat(Int(1), Int(1440)), Sym("d")));
        c.get(UnitsTime.DAY).put(UnitsTime.NANOSECOND, Mul(Rat(Int(1), Mul(Int(864), Pow(10, 11))), Sym("d")));
        c.get(UnitsTime.DAY).put(UnitsTime.PETASECOND, Mul(Rat(Mul(Int(3125), Pow(10, 8)), Int(27)), Sym("d")));
        c.get(UnitsTime.DAY).put(UnitsTime.PICOSECOND, Mul(Rat(Int(1), Mul(Int(864), Pow(10, 14))), Sym("d")));
        c.get(UnitsTime.DAY).put(UnitsTime.SECOND, Mul(Rat(Int(1), Int(86400)), Sym("d")));
        c.get(UnitsTime.DAY).put(UnitsTime.TERASECOND, Mul(Rat(Mul(Int(3125), Pow(10, 5)), Int(27)), Sym("d")));
        c.get(UnitsTime.DAY).put(UnitsTime.YOCTOSECOND, Mul(Rat(Int(1), Mul(Int(864), Pow(10, 26))), Sym("d")));
        c.get(UnitsTime.DAY).put(UnitsTime.YOTTASECOND, Mul(Rat(Mul(Int(3125), Pow(10, 17)), Int(27)), Sym("d")));
        c.get(UnitsTime.DAY).put(UnitsTime.ZEPTOSECOND, Mul(Rat(Int(1), Mul(Int(864), Pow(10, 23))), Sym("d")));
        c.get(UnitsTime.DAY).put(UnitsTime.ZETTASECOND, Mul(Rat(Mul(Int(3125), Pow(10, 14)), Int(27)), Sym("d")));
        c.get(UnitsTime.DECASECOND).put(UnitsTime.ATTOSECOND, Mul(Rat(Int(1), Pow(10, 19)), Sym("decas")));
        c.get(UnitsTime.DECASECOND).put(UnitsTime.CENTISECOND, Mul(Rat(Int(1), Int(1000)), Sym("decas")));
        c.get(UnitsTime.DECASECOND).put(UnitsTime.DAY, Mul(Int(8640), Sym("decas")));
        c.get(UnitsTime.DECASECOND).put(UnitsTime.DECISECOND, Mul(Rat(Int(1), Int(100)), Sym("decas")));
        c.get(UnitsTime.DECASECOND).put(UnitsTime.EXASECOND, Mul(Pow(10, 17), Sym("decas")));
        c.get(UnitsTime.DECASECOND).put(UnitsTime.FEMTOSECOND, Mul(Rat(Int(1), Pow(10, 16)), Sym("decas")));
        c.get(UnitsTime.DECASECOND).put(UnitsTime.GIGASECOND, Mul(Pow(10, 8), Sym("decas")));
        c.get(UnitsTime.DECASECOND).put(UnitsTime.HECTOSECOND, Mul(Int(10), Sym("decas")));
        c.get(UnitsTime.DECASECOND).put(UnitsTime.HOUR, Mul(Int(360), Sym("decas")));
        c.get(UnitsTime.DECASECOND).put(UnitsTime.KILOSECOND, Mul(Int(100), Sym("decas")));
        c.get(UnitsTime.DECASECOND).put(UnitsTime.MEGASECOND, Mul(Pow(10, 5), Sym("decas")));
        c.get(UnitsTime.DECASECOND).put(UnitsTime.MICROSECOND, Mul(Rat(Int(1), Pow(10, 7)), Sym("decas")));
        c.get(UnitsTime.DECASECOND).put(UnitsTime.MILLISECOND, Mul(Rat(Int(1), Pow(10, 4)), Sym("decas")));
        c.get(UnitsTime.DECASECOND).put(UnitsTime.MINUTE, Mul(Int(6), Sym("decas")));
        c.get(UnitsTime.DECASECOND).put(UnitsTime.NANOSECOND, Mul(Rat(Int(1), Pow(10, 10)), Sym("decas")));
        c.get(UnitsTime.DECASECOND).put(UnitsTime.PETASECOND, Mul(Pow(10, 14), Sym("decas")));
        c.get(UnitsTime.DECASECOND).put(UnitsTime.PICOSECOND, Mul(Rat(Int(1), Pow(10, 13)), Sym("decas")));
        c.get(UnitsTime.DECASECOND).put(UnitsTime.SECOND, Mul(Rat(Int(1), Int(10)), Sym("decas")));
        c.get(UnitsTime.DECASECOND).put(UnitsTime.TERASECOND, Mul(Pow(10, 11), Sym("decas")));
        c.get(UnitsTime.DECASECOND).put(UnitsTime.YOCTOSECOND, Mul(Rat(Int(1), Pow(10, 25)), Sym("decas")));
        c.get(UnitsTime.DECASECOND).put(UnitsTime.YOTTASECOND, Mul(Pow(10, 23), Sym("decas")));
        c.get(UnitsTime.DECASECOND).put(UnitsTime.ZEPTOSECOND, Mul(Rat(Int(1), Pow(10, 22)), Sym("decas")));
        c.get(UnitsTime.DECASECOND).put(UnitsTime.ZETTASECOND, Mul(Pow(10, 20), Sym("decas")));
        c.get(UnitsTime.DECISECOND).put(UnitsTime.ATTOSECOND, Mul(Rat(Int(1), Pow(10, 17)), Sym("decis")));
        c.get(UnitsTime.DECISECOND).put(UnitsTime.CENTISECOND, Mul(Rat(Int(1), Int(10)), Sym("decis")));
        c.get(UnitsTime.DECISECOND).put(UnitsTime.DAY, Mul(Int(864000), Sym("decis")));
        c.get(UnitsTime.DECISECOND).put(UnitsTime.DECASECOND, Mul(Int(100), Sym("decis")));
        c.get(UnitsTime.DECISECOND).put(UnitsTime.EXASECOND, Mul(Pow(10, 19), Sym("decis")));
        c.get(UnitsTime.DECISECOND).put(UnitsTime.FEMTOSECOND, Mul(Rat(Int(1), Pow(10, 14)), Sym("decis")));
        c.get(UnitsTime.DECISECOND).put(UnitsTime.GIGASECOND, Mul(Pow(10, 10), Sym("decis")));
        c.get(UnitsTime.DECISECOND).put(UnitsTime.HECTOSECOND, Mul(Int(1000), Sym("decis")));
        c.get(UnitsTime.DECISECOND).put(UnitsTime.HOUR, Mul(Int(36000), Sym("decis")));
        c.get(UnitsTime.DECISECOND).put(UnitsTime.KILOSECOND, Mul(Pow(10, 4), Sym("decis")));
        c.get(UnitsTime.DECISECOND).put(UnitsTime.MEGASECOND, Mul(Pow(10, 7), Sym("decis")));
        c.get(UnitsTime.DECISECOND).put(UnitsTime.MICROSECOND, Mul(Rat(Int(1), Pow(10, 5)), Sym("decis")));
        c.get(UnitsTime.DECISECOND).put(UnitsTime.MILLISECOND, Mul(Rat(Int(1), Int(100)), Sym("decis")));
        c.get(UnitsTime.DECISECOND).put(UnitsTime.MINUTE, Mul(Int(600), Sym("decis")));
        c.get(UnitsTime.DECISECOND).put(UnitsTime.NANOSECOND, Mul(Rat(Int(1), Pow(10, 8)), Sym("decis")));
        c.get(UnitsTime.DECISECOND).put(UnitsTime.PETASECOND, Mul(Pow(10, 16), Sym("decis")));
        c.get(UnitsTime.DECISECOND).put(UnitsTime.PICOSECOND, Mul(Rat(Int(1), Pow(10, 11)), Sym("decis")));
        c.get(UnitsTime.DECISECOND).put(UnitsTime.SECOND, Mul(Int(10), Sym("decis")));
        c.get(UnitsTime.DECISECOND).put(UnitsTime.TERASECOND, Mul(Pow(10, 13), Sym("decis")));
        c.get(UnitsTime.DECISECOND).put(UnitsTime.YOCTOSECOND, Mul(Rat(Int(1), Pow(10, 23)), Sym("decis")));
        c.get(UnitsTime.DECISECOND).put(UnitsTime.YOTTASECOND, Mul(Pow(10, 25), Sym("decis")));
        c.get(UnitsTime.DECISECOND).put(UnitsTime.ZEPTOSECOND, Mul(Rat(Int(1), Pow(10, 20)), Sym("decis")));
        c.get(UnitsTime.DECISECOND).put(UnitsTime.ZETTASECOND, Mul(Pow(10, 22), Sym("decis")));
        c.get(UnitsTime.EXASECOND).put(UnitsTime.ATTOSECOND, Mul(Rat(Int(1), Pow(10, 36)), Sym("exas")));
        c.get(UnitsTime.EXASECOND).put(UnitsTime.CENTISECOND, Mul(Rat(Int(1), Pow(10, 20)), Sym("exas")));
        c.get(UnitsTime.EXASECOND).put(UnitsTime.DAY, Mul(Rat(Int(27), Mul(Int(3125), Pow(10, 11))), Sym("exas")));
        c.get(UnitsTime.EXASECOND).put(UnitsTime.DECASECOND, Mul(Rat(Int(1), Pow(10, 17)), Sym("exas")));
        c.get(UnitsTime.EXASECOND).put(UnitsTime.DECISECOND, Mul(Rat(Int(1), Pow(10, 19)), Sym("exas")));
        c.get(UnitsTime.EXASECOND).put(UnitsTime.FEMTOSECOND, Mul(Rat(Int(1), Pow(10, 33)), Sym("exas")));
        c.get(UnitsTime.EXASECOND).put(UnitsTime.GIGASECOND, Mul(Rat(Int(1), Pow(10, 9)), Sym("exas")));
        c.get(UnitsTime.EXASECOND).put(UnitsTime.HECTOSECOND, Mul(Rat(Int(1), Pow(10, 16)), Sym("exas")));
        c.get(UnitsTime.EXASECOND).put(UnitsTime.HOUR, Mul(Rat(Int(9), Mul(Int(25), Pow(10, 14))), Sym("exas")));
        c.get(UnitsTime.EXASECOND).put(UnitsTime.KILOSECOND, Mul(Rat(Int(1), Pow(10, 15)), Sym("exas")));
        c.get(UnitsTime.EXASECOND).put(UnitsTime.MEGASECOND, Mul(Rat(Int(1), Pow(10, 12)), Sym("exas")));
        c.get(UnitsTime.EXASECOND).put(UnitsTime.MICROSECOND, Mul(Rat(Int(1), Pow(10, 24)), Sym("exas")));
        c.get(UnitsTime.EXASECOND).put(UnitsTime.MILLISECOND, Mul(Rat(Int(1), Pow(10, 21)), Sym("exas")));
        c.get(UnitsTime.EXASECOND).put(UnitsTime.MINUTE, Mul(Rat(Int(3), Mul(Int(5), Pow(10, 16))), Sym("exas")));
        c.get(UnitsTime.EXASECOND).put(UnitsTime.NANOSECOND, Mul(Rat(Int(1), Pow(10, 27)), Sym("exas")));
        c.get(UnitsTime.EXASECOND).put(UnitsTime.PETASECOND, Mul(Rat(Int(1), Int(1000)), Sym("exas")));
        c.get(UnitsTime.EXASECOND).put(UnitsTime.PICOSECOND, Mul(Rat(Int(1), Pow(10, 30)), Sym("exas")));
        c.get(UnitsTime.EXASECOND).put(UnitsTime.SECOND, Mul(Rat(Int(1), Pow(10, 18)), Sym("exas")));
        c.get(UnitsTime.EXASECOND).put(UnitsTime.TERASECOND, Mul(Rat(Int(1), Pow(10, 6)), Sym("exas")));
        c.get(UnitsTime.EXASECOND).put(UnitsTime.YOCTOSECOND, Mul(Rat(Int(1), Pow(10, 42)), Sym("exas")));
        c.get(UnitsTime.EXASECOND).put(UnitsTime.YOTTASECOND, Mul(Pow(10, 6), Sym("exas")));
        c.get(UnitsTime.EXASECOND).put(UnitsTime.ZEPTOSECOND, Mul(Rat(Int(1), Pow(10, 39)), Sym("exas")));
        c.get(UnitsTime.EXASECOND).put(UnitsTime.ZETTASECOND, Mul(Int(1000), Sym("exas")));
        c.get(UnitsTime.FEMTOSECOND).put(UnitsTime.ATTOSECOND, Mul(Rat(Int(1), Int(1000)), Sym("femtos")));
        c.get(UnitsTime.FEMTOSECOND).put(UnitsTime.CENTISECOND, Mul(Pow(10, 13), Sym("femtos")));
        c.get(UnitsTime.FEMTOSECOND).put(UnitsTime.DAY, Mul(Mul(Int(864), Pow(10, 17)), Sym("femtos")));
        c.get(UnitsTime.FEMTOSECOND).put(UnitsTime.DECASECOND, Mul(Pow(10, 16), Sym("femtos")));
        c.get(UnitsTime.FEMTOSECOND).put(UnitsTime.DECISECOND, Mul(Pow(10, 14), Sym("femtos")));
        c.get(UnitsTime.FEMTOSECOND).put(UnitsTime.EXASECOND, Mul(Pow(10, 33), Sym("femtos")));
        c.get(UnitsTime.FEMTOSECOND).put(UnitsTime.GIGASECOND, Mul(Pow(10, 24), Sym("femtos")));
        c.get(UnitsTime.FEMTOSECOND).put(UnitsTime.HECTOSECOND, Mul(Pow(10, 17), Sym("femtos")));
        c.get(UnitsTime.FEMTOSECOND).put(UnitsTime.HOUR, Mul(Mul(Int(36), Pow(10, 17)), Sym("femtos")));
        c.get(UnitsTime.FEMTOSECOND).put(UnitsTime.KILOSECOND, Mul(Pow(10, 18), Sym("femtos")));
        c.get(UnitsTime.FEMTOSECOND).put(UnitsTime.MEGASECOND, Mul(Pow(10, 21), Sym("femtos")));
        c.get(UnitsTime.FEMTOSECOND).put(UnitsTime.MICROSECOND, Mul(Pow(10, 9), Sym("femtos")));
        c.get(UnitsTime.FEMTOSECOND).put(UnitsTime.MILLISECOND, Mul(Pow(10, 12), Sym("femtos")));
        c.get(UnitsTime.FEMTOSECOND).put(UnitsTime.MINUTE, Mul(Mul(Int(6), Pow(10, 16)), Sym("femtos")));
        c.get(UnitsTime.FEMTOSECOND).put(UnitsTime.NANOSECOND, Mul(Pow(10, 6), Sym("femtos")));
        c.get(UnitsTime.FEMTOSECOND).put(UnitsTime.PETASECOND, Mul(Pow(10, 30), Sym("femtos")));
        c.get(UnitsTime.FEMTOSECOND).put(UnitsTime.PICOSECOND, Mul(Int(1000), Sym("femtos")));
        c.get(UnitsTime.FEMTOSECOND).put(UnitsTime.SECOND, Mul(Pow(10, 15), Sym("femtos")));
        c.get(UnitsTime.FEMTOSECOND).put(UnitsTime.TERASECOND, Mul(Pow(10, 27), Sym("femtos")));
        c.get(UnitsTime.FEMTOSECOND).put(UnitsTime.YOCTOSECOND, Mul(Rat(Int(1), Pow(10, 9)), Sym("femtos")));
        c.get(UnitsTime.FEMTOSECOND).put(UnitsTime.YOTTASECOND, Mul(Pow(10, 39), Sym("femtos")));
        c.get(UnitsTime.FEMTOSECOND).put(UnitsTime.ZEPTOSECOND, Mul(Rat(Int(1), Pow(10, 6)), Sym("femtos")));
        c.get(UnitsTime.FEMTOSECOND).put(UnitsTime.ZETTASECOND, Mul(Pow(10, 36), Sym("femtos")));
        c.get(UnitsTime.GIGASECOND).put(UnitsTime.ATTOSECOND, Mul(Rat(Int(1), Pow(10, 27)), Sym("gigas")));
        c.get(UnitsTime.GIGASECOND).put(UnitsTime.CENTISECOND, Mul(Rat(Int(1), Pow(10, 11)), Sym("gigas")));
        c.get(UnitsTime.GIGASECOND).put(UnitsTime.DAY, Mul(Rat(Int(27), Int(312500)), Sym("gigas")));
        c.get(UnitsTime.GIGASECOND).put(UnitsTime.DECASECOND, Mul(Rat(Int(1), Pow(10, 8)), Sym("gigas")));
        c.get(UnitsTime.GIGASECOND).put(UnitsTime.DECISECOND, Mul(Rat(Int(1), Pow(10, 10)), Sym("gigas")));
        c.get(UnitsTime.GIGASECOND).put(UnitsTime.EXASECOND, Mul(Pow(10, 9), Sym("gigas")));
        c.get(UnitsTime.GIGASECOND).put(UnitsTime.FEMTOSECOND, Mul(Rat(Int(1), Pow(10, 24)), Sym("gigas")));
        c.get(UnitsTime.GIGASECOND).put(UnitsTime.HECTOSECOND, Mul(Rat(Int(1), Pow(10, 7)), Sym("gigas")));
        c.get(UnitsTime.GIGASECOND).put(UnitsTime.HOUR, Mul(Rat(Int(9), Mul(Int(25), Pow(10, 5))), Sym("gigas")));
        c.get(UnitsTime.GIGASECOND).put(UnitsTime.KILOSECOND, Mul(Rat(Int(1), Pow(10, 6)), Sym("gigas")));
        c.get(UnitsTime.GIGASECOND).put(UnitsTime.MEGASECOND, Mul(Rat(Int(1), Int(1000)), Sym("gigas")));
        c.get(UnitsTime.GIGASECOND).put(UnitsTime.MICROSECOND, Mul(Rat(Int(1), Pow(10, 15)), Sym("gigas")));
        c.get(UnitsTime.GIGASECOND).put(UnitsTime.MILLISECOND, Mul(Rat(Int(1), Pow(10, 12)), Sym("gigas")));
        c.get(UnitsTime.GIGASECOND).put(UnitsTime.MINUTE, Mul(Rat(Int(3), Mul(Int(5), Pow(10, 7))), Sym("gigas")));
        c.get(UnitsTime.GIGASECOND).put(UnitsTime.NANOSECOND, Mul(Rat(Int(1), Pow(10, 18)), Sym("gigas")));
        c.get(UnitsTime.GIGASECOND).put(UnitsTime.PETASECOND, Mul(Pow(10, 6), Sym("gigas")));
        c.get(UnitsTime.GIGASECOND).put(UnitsTime.PICOSECOND, Mul(Rat(Int(1), Pow(10, 21)), Sym("gigas")));
        c.get(UnitsTime.GIGASECOND).put(UnitsTime.SECOND, Mul(Rat(Int(1), Pow(10, 9)), Sym("gigas")));
        c.get(UnitsTime.GIGASECOND).put(UnitsTime.TERASECOND, Mul(Int(1000), Sym("gigas")));
        c.get(UnitsTime.GIGASECOND).put(UnitsTime.YOCTOSECOND, Mul(Rat(Int(1), Pow(10, 33)), Sym("gigas")));
        c.get(UnitsTime.GIGASECOND).put(UnitsTime.YOTTASECOND, Mul(Pow(10, 15), Sym("gigas")));
        c.get(UnitsTime.GIGASECOND).put(UnitsTime.ZEPTOSECOND, Mul(Rat(Int(1), Pow(10, 30)), Sym("gigas")));
        c.get(UnitsTime.GIGASECOND).put(UnitsTime.ZETTASECOND, Mul(Pow(10, 12), Sym("gigas")));
        c.get(UnitsTime.HECTOSECOND).put(UnitsTime.ATTOSECOND, Mul(Rat(Int(1), Pow(10, 20)), Sym("hectos")));
        c.get(UnitsTime.HECTOSECOND).put(UnitsTime.CENTISECOND, Mul(Rat(Int(1), Pow(10, 4)), Sym("hectos")));
        c.get(UnitsTime.HECTOSECOND).put(UnitsTime.DAY, Mul(Int(864), Sym("hectos")));
        c.get(UnitsTime.HECTOSECOND).put(UnitsTime.DECASECOND, Mul(Rat(Int(1), Int(10)), Sym("hectos")));
        c.get(UnitsTime.HECTOSECOND).put(UnitsTime.DECISECOND, Mul(Rat(Int(1), Int(1000)), Sym("hectos")));
        c.get(UnitsTime.HECTOSECOND).put(UnitsTime.EXASECOND, Mul(Pow(10, 16), Sym("hectos")));
        c.get(UnitsTime.HECTOSECOND).put(UnitsTime.FEMTOSECOND, Mul(Rat(Int(1), Pow(10, 17)), Sym("hectos")));
        c.get(UnitsTime.HECTOSECOND).put(UnitsTime.GIGASECOND, Mul(Pow(10, 7), Sym("hectos")));
        c.get(UnitsTime.HECTOSECOND).put(UnitsTime.HOUR, Mul(Int(36), Sym("hectos")));
        c.get(UnitsTime.HECTOSECOND).put(UnitsTime.KILOSECOND, Mul(Int(10), Sym("hectos")));
        c.get(UnitsTime.HECTOSECOND).put(UnitsTime.MEGASECOND, Mul(Pow(10, 4), Sym("hectos")));
        c.get(UnitsTime.HECTOSECOND).put(UnitsTime.MICROSECOND, Mul(Rat(Int(1), Pow(10, 8)), Sym("hectos")));
        c.get(UnitsTime.HECTOSECOND).put(UnitsTime.MILLISECOND, Mul(Rat(Int(1), Pow(10, 5)), Sym("hectos")));
        c.get(UnitsTime.HECTOSECOND).put(UnitsTime.MINUTE, Mul(Rat(Int(3), Int(5)), Sym("hectos")));
        c.get(UnitsTime.HECTOSECOND).put(UnitsTime.NANOSECOND, Mul(Rat(Int(1), Pow(10, 11)), Sym("hectos")));
        c.get(UnitsTime.HECTOSECOND).put(UnitsTime.PETASECOND, Mul(Pow(10, 13), Sym("hectos")));
        c.get(UnitsTime.HECTOSECOND).put(UnitsTime.PICOSECOND, Mul(Rat(Int(1), Pow(10, 14)), Sym("hectos")));
        c.get(UnitsTime.HECTOSECOND).put(UnitsTime.SECOND, Mul(Rat(Int(1), Int(100)), Sym("hectos")));
        c.get(UnitsTime.HECTOSECOND).put(UnitsTime.TERASECOND, Mul(Pow(10, 10), Sym("hectos")));
        c.get(UnitsTime.HECTOSECOND).put(UnitsTime.YOCTOSECOND, Mul(Rat(Int(1), Pow(10, 26)), Sym("hectos")));
        c.get(UnitsTime.HECTOSECOND).put(UnitsTime.YOTTASECOND, Mul(Pow(10, 22), Sym("hectos")));
        c.get(UnitsTime.HECTOSECOND).put(UnitsTime.ZEPTOSECOND, Mul(Rat(Int(1), Pow(10, 23)), Sym("hectos")));
        c.get(UnitsTime.HECTOSECOND).put(UnitsTime.ZETTASECOND, Mul(Pow(10, 19), Sym("hectos")));
        c.get(UnitsTime.HOUR).put(UnitsTime.ATTOSECOND, Mul(Rat(Int(1), Mul(Int(36), Pow(10, 20))), Sym("h")));
        c.get(UnitsTime.HOUR).put(UnitsTime.CENTISECOND, Mul(Rat(Int(1), Mul(Int(36), Pow(10, 4))), Sym("h")));
        c.get(UnitsTime.HOUR).put(UnitsTime.DAY, Mul(Int(24), Sym("h")));
        c.get(UnitsTime.HOUR).put(UnitsTime.DECASECOND, Mul(Rat(Int(1), Int(360)), Sym("h")));
        c.get(UnitsTime.HOUR).put(UnitsTime.DECISECOND, Mul(Rat(Int(1), Int(36000)), Sym("h")));
        c.get(UnitsTime.HOUR).put(UnitsTime.EXASECOND, Mul(Rat(Mul(Int(25), Pow(10, 14)), Int(9)), Sym("h")));
        c.get(UnitsTime.HOUR).put(UnitsTime.FEMTOSECOND, Mul(Rat(Int(1), Mul(Int(36), Pow(10, 17))), Sym("h")));
        c.get(UnitsTime.HOUR).put(UnitsTime.GIGASECOND, Mul(Rat(Mul(Int(25), Pow(10, 5)), Int(9)), Sym("h")));
        c.get(UnitsTime.HOUR).put(UnitsTime.HECTOSECOND, Mul(Rat(Int(1), Int(36)), Sym("h")));
        c.get(UnitsTime.HOUR).put(UnitsTime.KILOSECOND, Mul(Rat(Int(5), Int(18)), Sym("h")));
        c.get(UnitsTime.HOUR).put(UnitsTime.MEGASECOND, Mul(Rat(Int(2500), Int(9)), Sym("h")));
        c.get(UnitsTime.HOUR).put(UnitsTime.MICROSECOND, Mul(Rat(Int(1), Mul(Int(36), Pow(10, 8))), Sym("h")));
        c.get(UnitsTime.HOUR).put(UnitsTime.MILLISECOND, Mul(Rat(Int(1), Mul(Int(36), Pow(10, 5))), Sym("h")));
        c.get(UnitsTime.HOUR).put(UnitsTime.MINUTE, Mul(Rat(Int(1), Int(60)), Sym("h")));
        c.get(UnitsTime.HOUR).put(UnitsTime.NANOSECOND, Mul(Rat(Int(1), Mul(Int(36), Pow(10, 11))), Sym("h")));
        c.get(UnitsTime.HOUR).put(UnitsTime.PETASECOND, Mul(Rat(Mul(Int(25), Pow(10, 11)), Int(9)), Sym("h")));
        c.get(UnitsTime.HOUR).put(UnitsTime.PICOSECOND, Mul(Rat(Int(1), Mul(Int(36), Pow(10, 14))), Sym("h")));
        c.get(UnitsTime.HOUR).put(UnitsTime.SECOND, Mul(Rat(Int(1), Int(3600)), Sym("h")));
        c.get(UnitsTime.HOUR).put(UnitsTime.TERASECOND, Mul(Rat(Mul(Int(25), Pow(10, 8)), Int(9)), Sym("h")));
        c.get(UnitsTime.HOUR).put(UnitsTime.YOCTOSECOND, Mul(Rat(Int(1), Mul(Int(36), Pow(10, 26))), Sym("h")));
        c.get(UnitsTime.HOUR).put(UnitsTime.YOTTASECOND, Mul(Rat(Mul(Int(25), Pow(10, 20)), Int(9)), Sym("h")));
        c.get(UnitsTime.HOUR).put(UnitsTime.ZEPTOSECOND, Mul(Rat(Int(1), Mul(Int(36), Pow(10, 23))), Sym("h")));
        c.get(UnitsTime.HOUR).put(UnitsTime.ZETTASECOND, Mul(Rat(Mul(Int(25), Pow(10, 17)), Int(9)), Sym("h")));
        c.get(UnitsTime.KILOSECOND).put(UnitsTime.ATTOSECOND, Mul(Rat(Int(1), Pow(10, 21)), Sym("kilos")));
        c.get(UnitsTime.KILOSECOND).put(UnitsTime.CENTISECOND, Mul(Rat(Int(1), Pow(10, 5)), Sym("kilos")));
        c.get(UnitsTime.KILOSECOND).put(UnitsTime.DAY, Mul(Rat(Int(432), Int(5)), Sym("kilos")));
        c.get(UnitsTime.KILOSECOND).put(UnitsTime.DECASECOND, Mul(Rat(Int(1), Int(100)), Sym("kilos")));
        c.get(UnitsTime.KILOSECOND).put(UnitsTime.DECISECOND, Mul(Rat(Int(1), Pow(10, 4)), Sym("kilos")));
        c.get(UnitsTime.KILOSECOND).put(UnitsTime.EXASECOND, Mul(Pow(10, 15), Sym("kilos")));
        c.get(UnitsTime.KILOSECOND).put(UnitsTime.FEMTOSECOND, Mul(Rat(Int(1), Pow(10, 18)), Sym("kilos")));
        c.get(UnitsTime.KILOSECOND).put(UnitsTime.GIGASECOND, Mul(Pow(10, 6), Sym("kilos")));
        c.get(UnitsTime.KILOSECOND).put(UnitsTime.HECTOSECOND, Mul(Rat(Int(1), Int(10)), Sym("kilos")));
        c.get(UnitsTime.KILOSECOND).put(UnitsTime.HOUR, Mul(Rat(Int(18), Int(5)), Sym("kilos")));
        c.get(UnitsTime.KILOSECOND).put(UnitsTime.MEGASECOND, Mul(Int(1000), Sym("kilos")));
        c.get(UnitsTime.KILOSECOND).put(UnitsTime.MICROSECOND, Mul(Rat(Int(1), Pow(10, 9)), Sym("kilos")));
        c.get(UnitsTime.KILOSECOND).put(UnitsTime.MILLISECOND, Mul(Rat(Int(1), Pow(10, 6)), Sym("kilos")));
        c.get(UnitsTime.KILOSECOND).put(UnitsTime.MINUTE, Mul(Rat(Int(3), Int(50)), Sym("kilos")));
        c.get(UnitsTime.KILOSECOND).put(UnitsTime.NANOSECOND, Mul(Rat(Int(1), Pow(10, 12)), Sym("kilos")));
        c.get(UnitsTime.KILOSECOND).put(UnitsTime.PETASECOND, Mul(Pow(10, 12), Sym("kilos")));
        c.get(UnitsTime.KILOSECOND).put(UnitsTime.PICOSECOND, Mul(Rat(Int(1), Pow(10, 15)), Sym("kilos")));
        c.get(UnitsTime.KILOSECOND).put(UnitsTime.SECOND, Mul(Rat(Int(1), Int(1000)), Sym("kilos")));
        c.get(UnitsTime.KILOSECOND).put(UnitsTime.TERASECOND, Mul(Pow(10, 9), Sym("kilos")));
        c.get(UnitsTime.KILOSECOND).put(UnitsTime.YOCTOSECOND, Mul(Rat(Int(1), Pow(10, 27)), Sym("kilos")));
        c.get(UnitsTime.KILOSECOND).put(UnitsTime.YOTTASECOND, Mul(Pow(10, 21), Sym("kilos")));
        c.get(UnitsTime.KILOSECOND).put(UnitsTime.ZEPTOSECOND, Mul(Rat(Int(1), Pow(10, 24)), Sym("kilos")));
        c.get(UnitsTime.KILOSECOND).put(UnitsTime.ZETTASECOND, Mul(Pow(10, 18), Sym("kilos")));
        c.get(UnitsTime.MEGASECOND).put(UnitsTime.ATTOSECOND, Mul(Rat(Int(1), Pow(10, 24)), Sym("megas")));
        c.get(UnitsTime.MEGASECOND).put(UnitsTime.CENTISECOND, Mul(Rat(Int(1), Pow(10, 8)), Sym("megas")));
        c.get(UnitsTime.MEGASECOND).put(UnitsTime.DAY, Mul(Rat(Int(54), Int(625)), Sym("megas")));
        c.get(UnitsTime.MEGASECOND).put(UnitsTime.DECASECOND, Mul(Rat(Int(1), Pow(10, 5)), Sym("megas")));
        c.get(UnitsTime.MEGASECOND).put(UnitsTime.DECISECOND, Mul(Rat(Int(1), Pow(10, 7)), Sym("megas")));
        c.get(UnitsTime.MEGASECOND).put(UnitsTime.EXASECOND, Mul(Pow(10, 12), Sym("megas")));
        c.get(UnitsTime.MEGASECOND).put(UnitsTime.FEMTOSECOND, Mul(Rat(Int(1), Pow(10, 21)), Sym("megas")));
        c.get(UnitsTime.MEGASECOND).put(UnitsTime.GIGASECOND, Mul(Int(1000), Sym("megas")));
        c.get(UnitsTime.MEGASECOND).put(UnitsTime.HECTOSECOND, Mul(Rat(Int(1), Pow(10, 4)), Sym("megas")));
        c.get(UnitsTime.MEGASECOND).put(UnitsTime.HOUR, Mul(Rat(Int(9), Int(2500)), Sym("megas")));
        c.get(UnitsTime.MEGASECOND).put(UnitsTime.KILOSECOND, Mul(Rat(Int(1), Int(1000)), Sym("megas")));
        c.get(UnitsTime.MEGASECOND).put(UnitsTime.MICROSECOND, Mul(Rat(Int(1), Pow(10, 12)), Sym("megas")));
        c.get(UnitsTime.MEGASECOND).put(UnitsTime.MILLISECOND, Mul(Rat(Int(1), Pow(10, 9)), Sym("megas")));
        c.get(UnitsTime.MEGASECOND).put(UnitsTime.MINUTE, Mul(Rat(Int(3), Mul(Int(5), Pow(10, 4))), Sym("megas")));
        c.get(UnitsTime.MEGASECOND).put(UnitsTime.NANOSECOND, Mul(Rat(Int(1), Pow(10, 15)), Sym("megas")));
        c.get(UnitsTime.MEGASECOND).put(UnitsTime.PETASECOND, Mul(Pow(10, 9), Sym("megas")));
        c.get(UnitsTime.MEGASECOND).put(UnitsTime.PICOSECOND, Mul(Rat(Int(1), Pow(10, 18)), Sym("megas")));
        c.get(UnitsTime.MEGASECOND).put(UnitsTime.SECOND, Mul(Rat(Int(1), Pow(10, 6)), Sym("megas")));
        c.get(UnitsTime.MEGASECOND).put(UnitsTime.TERASECOND, Mul(Pow(10, 6), Sym("megas")));
        c.get(UnitsTime.MEGASECOND).put(UnitsTime.YOCTOSECOND, Mul(Rat(Int(1), Pow(10, 30)), Sym("megas")));
        c.get(UnitsTime.MEGASECOND).put(UnitsTime.YOTTASECOND, Mul(Pow(10, 18), Sym("megas")));
        c.get(UnitsTime.MEGASECOND).put(UnitsTime.ZEPTOSECOND, Mul(Rat(Int(1), Pow(10, 27)), Sym("megas")));
        c.get(UnitsTime.MEGASECOND).put(UnitsTime.ZETTASECOND, Mul(Pow(10, 15), Sym("megas")));
        c.get(UnitsTime.MICROSECOND).put(UnitsTime.ATTOSECOND, Mul(Rat(Int(1), Pow(10, 12)), Sym("micros")));
        c.get(UnitsTime.MICROSECOND).put(UnitsTime.CENTISECOND, Mul(Pow(10, 4), Sym("micros")));
        c.get(UnitsTime.MICROSECOND).put(UnitsTime.DAY, Mul(Mul(Int(864), Pow(10, 8)), Sym("micros")));
        c.get(UnitsTime.MICROSECOND).put(UnitsTime.DECASECOND, Mul(Pow(10, 7), Sym("micros")));
        c.get(UnitsTime.MICROSECOND).put(UnitsTime.DECISECOND, Mul(Pow(10, 5), Sym("micros")));
        c.get(UnitsTime.MICROSECOND).put(UnitsTime.EXASECOND, Mul(Pow(10, 24), Sym("micros")));
        c.get(UnitsTime.MICROSECOND).put(UnitsTime.FEMTOSECOND, Mul(Rat(Int(1), Pow(10, 9)), Sym("micros")));
        c.get(UnitsTime.MICROSECOND).put(UnitsTime.GIGASECOND, Mul(Pow(10, 15), Sym("micros")));
        c.get(UnitsTime.MICROSECOND).put(UnitsTime.HECTOSECOND, Mul(Pow(10, 8), Sym("micros")));
        c.get(UnitsTime.MICROSECOND).put(UnitsTime.HOUR, Mul(Mul(Int(36), Pow(10, 8)), Sym("micros")));
        c.get(UnitsTime.MICROSECOND).put(UnitsTime.KILOSECOND, Mul(Pow(10, 9), Sym("micros")));
        c.get(UnitsTime.MICROSECOND).put(UnitsTime.MEGASECOND, Mul(Pow(10, 12), Sym("micros")));
        c.get(UnitsTime.MICROSECOND).put(UnitsTime.MILLISECOND, Mul(Int(1000), Sym("micros")));
        c.get(UnitsTime.MICROSECOND).put(UnitsTime.MINUTE, Mul(Mul(Int(6), Pow(10, 7)), Sym("micros")));
        c.get(UnitsTime.MICROSECOND).put(UnitsTime.NANOSECOND, Mul(Rat(Int(1), Int(1000)), Sym("micros")));
        c.get(UnitsTime.MICROSECOND).put(UnitsTime.PETASECOND, Mul(Pow(10, 21), Sym("micros")));
        c.get(UnitsTime.MICROSECOND).put(UnitsTime.PICOSECOND, Mul(Rat(Int(1), Pow(10, 6)), Sym("micros")));
        c.get(UnitsTime.MICROSECOND).put(UnitsTime.SECOND, Mul(Pow(10, 6), Sym("micros")));
        c.get(UnitsTime.MICROSECOND).put(UnitsTime.TERASECOND, Mul(Pow(10, 18), Sym("micros")));
        c.get(UnitsTime.MICROSECOND).put(UnitsTime.YOCTOSECOND, Mul(Rat(Int(1), Pow(10, 18)), Sym("micros")));
        c.get(UnitsTime.MICROSECOND).put(UnitsTime.YOTTASECOND, Mul(Pow(10, 30), Sym("micros")));
        c.get(UnitsTime.MICROSECOND).put(UnitsTime.ZEPTOSECOND, Mul(Rat(Int(1), Pow(10, 15)), Sym("micros")));
        c.get(UnitsTime.MICROSECOND).put(UnitsTime.ZETTASECOND, Mul(Pow(10, 27), Sym("micros")));
        c.get(UnitsTime.MILLISECOND).put(UnitsTime.ATTOSECOND, Mul(Rat(Int(1), Pow(10, 15)), Sym("millis")));
        c.get(UnitsTime.MILLISECOND).put(UnitsTime.CENTISECOND, Mul(Int(10), Sym("millis")));
        c.get(UnitsTime.MILLISECOND).put(UnitsTime.DAY, Mul(Mul(Int(864), Pow(10, 5)), Sym("millis")));
        c.get(UnitsTime.MILLISECOND).put(UnitsTime.DECASECOND, Mul(Pow(10, 4), Sym("millis")));
        c.get(UnitsTime.MILLISECOND).put(UnitsTime.DECISECOND, Mul(Int(100), Sym("millis")));
        c.get(UnitsTime.MILLISECOND).put(UnitsTime.EXASECOND, Mul(Pow(10, 21), Sym("millis")));
        c.get(UnitsTime.MILLISECOND).put(UnitsTime.FEMTOSECOND, Mul(Rat(Int(1), Pow(10, 12)), Sym("millis")));
        c.get(UnitsTime.MILLISECOND).put(UnitsTime.GIGASECOND, Mul(Pow(10, 12), Sym("millis")));
        c.get(UnitsTime.MILLISECOND).put(UnitsTime.HECTOSECOND, Mul(Pow(10, 5), Sym("millis")));
        c.get(UnitsTime.MILLISECOND).put(UnitsTime.HOUR, Mul(Mul(Int(36), Pow(10, 5)), Sym("millis")));
        c.get(UnitsTime.MILLISECOND).put(UnitsTime.KILOSECOND, Mul(Pow(10, 6), Sym("millis")));
        c.get(UnitsTime.MILLISECOND).put(UnitsTime.MEGASECOND, Mul(Pow(10, 9), Sym("millis")));
        c.get(UnitsTime.MILLISECOND).put(UnitsTime.MICROSECOND, Mul(Rat(Int(1), Int(1000)), Sym("millis")));
        c.get(UnitsTime.MILLISECOND).put(UnitsTime.MINUTE, Mul(Mul(Int(6), Pow(10, 4)), Sym("millis")));
        c.get(UnitsTime.MILLISECOND).put(UnitsTime.NANOSECOND, Mul(Rat(Int(1), Pow(10, 6)), Sym("millis")));
        c.get(UnitsTime.MILLISECOND).put(UnitsTime.PETASECOND, Mul(Pow(10, 18), Sym("millis")));
        c.get(UnitsTime.MILLISECOND).put(UnitsTime.PICOSECOND, Mul(Rat(Int(1), Pow(10, 9)), Sym("millis")));
        c.get(UnitsTime.MILLISECOND).put(UnitsTime.SECOND, Mul(Int(1000), Sym("millis")));
        c.get(UnitsTime.MILLISECOND).put(UnitsTime.TERASECOND, Mul(Pow(10, 15), Sym("millis")));
        c.get(UnitsTime.MILLISECOND).put(UnitsTime.YOCTOSECOND, Mul(Rat(Int(1), Pow(10, 21)), Sym("millis")));
        c.get(UnitsTime.MILLISECOND).put(UnitsTime.YOTTASECOND, Mul(Pow(10, 27), Sym("millis")));
        c.get(UnitsTime.MILLISECOND).put(UnitsTime.ZEPTOSECOND, Mul(Rat(Int(1), Pow(10, 18)), Sym("millis")));
        c.get(UnitsTime.MILLISECOND).put(UnitsTime.ZETTASECOND, Mul(Pow(10, 24), Sym("millis")));
        c.get(UnitsTime.MINUTE).put(UnitsTime.ATTOSECOND, Mul(Rat(Int(1), Mul(Int(6), Pow(10, 19))), Sym("m")));
        c.get(UnitsTime.MINUTE).put(UnitsTime.CENTISECOND, Mul(Rat(Int(1), Int(6000)), Sym("m")));
        c.get(UnitsTime.MINUTE).put(UnitsTime.DAY, Mul(Int(1440), Sym("m")));
        c.get(UnitsTime.MINUTE).put(UnitsTime.DECASECOND, Mul(Rat(Int(1), Int(6)), Sym("m")));
        c.get(UnitsTime.MINUTE).put(UnitsTime.DECISECOND, Mul(Rat(Int(1), Int(600)), Sym("m")));
        c.get(UnitsTime.MINUTE).put(UnitsTime.EXASECOND, Mul(Rat(Mul(Int(5), Pow(10, 16)), Int(3)), Sym("m")));
        c.get(UnitsTime.MINUTE).put(UnitsTime.FEMTOSECOND, Mul(Rat(Int(1), Mul(Int(6), Pow(10, 16))), Sym("m")));
        c.get(UnitsTime.MINUTE).put(UnitsTime.GIGASECOND, Mul(Rat(Mul(Int(5), Pow(10, 7)), Int(3)), Sym("m")));
        c.get(UnitsTime.MINUTE).put(UnitsTime.HECTOSECOND, Mul(Rat(Int(5), Int(3)), Sym("m")));
        c.get(UnitsTime.MINUTE).put(UnitsTime.HOUR, Mul(Int(60), Sym("m")));
        c.get(UnitsTime.MINUTE).put(UnitsTime.KILOSECOND, Mul(Rat(Int(50), Int(3)), Sym("m")));
        c.get(UnitsTime.MINUTE).put(UnitsTime.MEGASECOND, Mul(Rat(Mul(Int(5), Pow(10, 4)), Int(3)), Sym("m")));
        c.get(UnitsTime.MINUTE).put(UnitsTime.MICROSECOND, Mul(Rat(Int(1), Mul(Int(6), Pow(10, 7))), Sym("m")));
        c.get(UnitsTime.MINUTE).put(UnitsTime.MILLISECOND, Mul(Rat(Int(1), Mul(Int(6), Pow(10, 4))), Sym("m")));
        c.get(UnitsTime.MINUTE).put(UnitsTime.NANOSECOND, Mul(Rat(Int(1), Mul(Int(6), Pow(10, 10))), Sym("m")));
        c.get(UnitsTime.MINUTE).put(UnitsTime.PETASECOND, Mul(Rat(Mul(Int(5), Pow(10, 13)), Int(3)), Sym("m")));
        c.get(UnitsTime.MINUTE).put(UnitsTime.PICOSECOND, Mul(Rat(Int(1), Mul(Int(6), Pow(10, 13))), Sym("m")));
        c.get(UnitsTime.MINUTE).put(UnitsTime.SECOND, Mul(Rat(Int(1), Int(60)), Sym("m")));
        c.get(UnitsTime.MINUTE).put(UnitsTime.TERASECOND, Mul(Rat(Mul(Int(5), Pow(10, 10)), Int(3)), Sym("m")));
        c.get(UnitsTime.MINUTE).put(UnitsTime.YOCTOSECOND, Mul(Rat(Int(1), Mul(Int(6), Pow(10, 25))), Sym("m")));
        c.get(UnitsTime.MINUTE).put(UnitsTime.YOTTASECOND, Mul(Rat(Mul(Int(5), Pow(10, 22)), Int(3)), Sym("m")));
        c.get(UnitsTime.MINUTE).put(UnitsTime.ZEPTOSECOND, Mul(Rat(Int(1), Mul(Int(6), Pow(10, 22))), Sym("m")));
        c.get(UnitsTime.MINUTE).put(UnitsTime.ZETTASECOND, Mul(Rat(Mul(Int(5), Pow(10, 19)), Int(3)), Sym("m")));
        c.get(UnitsTime.NANOSECOND).put(UnitsTime.ATTOSECOND, Mul(Rat(Int(1), Pow(10, 9)), Sym("nanos")));
        c.get(UnitsTime.NANOSECOND).put(UnitsTime.CENTISECOND, Mul(Pow(10, 7), Sym("nanos")));
        c.get(UnitsTime.NANOSECOND).put(UnitsTime.DAY, Mul(Mul(Int(864), Pow(10, 11)), Sym("nanos")));
        c.get(UnitsTime.NANOSECOND).put(UnitsTime.DECASECOND, Mul(Pow(10, 10), Sym("nanos")));
        c.get(UnitsTime.NANOSECOND).put(UnitsTime.DECISECOND, Mul(Pow(10, 8), Sym("nanos")));
        c.get(UnitsTime.NANOSECOND).put(UnitsTime.EXASECOND, Mul(Pow(10, 27), Sym("nanos")));
        c.get(UnitsTime.NANOSECOND).put(UnitsTime.FEMTOSECOND, Mul(Rat(Int(1), Pow(10, 6)), Sym("nanos")));
        c.get(UnitsTime.NANOSECOND).put(UnitsTime.GIGASECOND, Mul(Pow(10, 18), Sym("nanos")));
        c.get(UnitsTime.NANOSECOND).put(UnitsTime.HECTOSECOND, Mul(Pow(10, 11), Sym("nanos")));
        c.get(UnitsTime.NANOSECOND).put(UnitsTime.HOUR, Mul(Mul(Int(36), Pow(10, 11)), Sym("nanos")));
        c.get(UnitsTime.NANOSECOND).put(UnitsTime.KILOSECOND, Mul(Pow(10, 12), Sym("nanos")));
        c.get(UnitsTime.NANOSECOND).put(UnitsTime.MEGASECOND, Mul(Pow(10, 15), Sym("nanos")));
        c.get(UnitsTime.NANOSECOND).put(UnitsTime.MICROSECOND, Mul(Int(1000), Sym("nanos")));
        c.get(UnitsTime.NANOSECOND).put(UnitsTime.MILLISECOND, Mul(Pow(10, 6), Sym("nanos")));
        c.get(UnitsTime.NANOSECOND).put(UnitsTime.MINUTE, Mul(Mul(Int(6), Pow(10, 10)), Sym("nanos")));
        c.get(UnitsTime.NANOSECOND).put(UnitsTime.PETASECOND, Mul(Pow(10, 24), Sym("nanos")));
        c.get(UnitsTime.NANOSECOND).put(UnitsTime.PICOSECOND, Mul(Rat(Int(1), Int(1000)), Sym("nanos")));
        c.get(UnitsTime.NANOSECOND).put(UnitsTime.SECOND, Mul(Pow(10, 9), Sym("nanos")));
        c.get(UnitsTime.NANOSECOND).put(UnitsTime.TERASECOND, Mul(Pow(10, 21), Sym("nanos")));
        c.get(UnitsTime.NANOSECOND).put(UnitsTime.YOCTOSECOND, Mul(Rat(Int(1), Pow(10, 15)), Sym("nanos")));
        c.get(UnitsTime.NANOSECOND).put(UnitsTime.YOTTASECOND, Mul(Pow(10, 33), Sym("nanos")));
        c.get(UnitsTime.NANOSECOND).put(UnitsTime.ZEPTOSECOND, Mul(Rat(Int(1), Pow(10, 12)), Sym("nanos")));
        c.get(UnitsTime.NANOSECOND).put(UnitsTime.ZETTASECOND, Mul(Pow(10, 30), Sym("nanos")));
        c.get(UnitsTime.PETASECOND).put(UnitsTime.ATTOSECOND, Mul(Rat(Int(1), Pow(10, 33)), Sym("petas")));
        c.get(UnitsTime.PETASECOND).put(UnitsTime.CENTISECOND, Mul(Rat(Int(1), Pow(10, 17)), Sym("petas")));
        c.get(UnitsTime.PETASECOND).put(UnitsTime.DAY, Mul(Rat(Int(27), Mul(Int(3125), Pow(10, 8))), Sym("petas")));
        c.get(UnitsTime.PETASECOND).put(UnitsTime.DECASECOND, Mul(Rat(Int(1), Pow(10, 14)), Sym("petas")));
        c.get(UnitsTime.PETASECOND).put(UnitsTime.DECISECOND, Mul(Rat(Int(1), Pow(10, 16)), Sym("petas")));
        c.get(UnitsTime.PETASECOND).put(UnitsTime.EXASECOND, Mul(Int(1000), Sym("petas")));
        c.get(UnitsTime.PETASECOND).put(UnitsTime.FEMTOSECOND, Mul(Rat(Int(1), Pow(10, 30)), Sym("petas")));
        c.get(UnitsTime.PETASECOND).put(UnitsTime.GIGASECOND, Mul(Rat(Int(1), Pow(10, 6)), Sym("petas")));
        c.get(UnitsTime.PETASECOND).put(UnitsTime.HECTOSECOND, Mul(Rat(Int(1), Pow(10, 13)), Sym("petas")));
        c.get(UnitsTime.PETASECOND).put(UnitsTime.HOUR, Mul(Rat(Int(9), Mul(Int(25), Pow(10, 11))), Sym("petas")));
        c.get(UnitsTime.PETASECOND).put(UnitsTime.KILOSECOND, Mul(Rat(Int(1), Pow(10, 12)), Sym("petas")));
        c.get(UnitsTime.PETASECOND).put(UnitsTime.MEGASECOND, Mul(Rat(Int(1), Pow(10, 9)), Sym("petas")));
        c.get(UnitsTime.PETASECOND).put(UnitsTime.MICROSECOND, Mul(Rat(Int(1), Pow(10, 21)), Sym("petas")));
        c.get(UnitsTime.PETASECOND).put(UnitsTime.MILLISECOND, Mul(Rat(Int(1), Pow(10, 18)), Sym("petas")));
        c.get(UnitsTime.PETASECOND).put(UnitsTime.MINUTE, Mul(Rat(Int(3), Mul(Int(5), Pow(10, 13))), Sym("petas")));
        c.get(UnitsTime.PETASECOND).put(UnitsTime.NANOSECOND, Mul(Rat(Int(1), Pow(10, 24)), Sym("petas")));
        c.get(UnitsTime.PETASECOND).put(UnitsTime.PICOSECOND, Mul(Rat(Int(1), Pow(10, 27)), Sym("petas")));
        c.get(UnitsTime.PETASECOND).put(UnitsTime.SECOND, Mul(Rat(Int(1), Pow(10, 15)), Sym("petas")));
        c.get(UnitsTime.PETASECOND).put(UnitsTime.TERASECOND, Mul(Rat(Int(1), Int(1000)), Sym("petas")));
        c.get(UnitsTime.PETASECOND).put(UnitsTime.YOCTOSECOND, Mul(Rat(Int(1), Pow(10, 39)), Sym("petas")));
        c.get(UnitsTime.PETASECOND).put(UnitsTime.YOTTASECOND, Mul(Pow(10, 9), Sym("petas")));
        c.get(UnitsTime.PETASECOND).put(UnitsTime.ZEPTOSECOND, Mul(Rat(Int(1), Pow(10, 36)), Sym("petas")));
        c.get(UnitsTime.PETASECOND).put(UnitsTime.ZETTASECOND, Mul(Pow(10, 6), Sym("petas")));
        c.get(UnitsTime.PICOSECOND).put(UnitsTime.ATTOSECOND, Mul(Rat(Int(1), Pow(10, 6)), Sym("picos")));
        c.get(UnitsTime.PICOSECOND).put(UnitsTime.CENTISECOND, Mul(Pow(10, 10), Sym("picos")));
        c.get(UnitsTime.PICOSECOND).put(UnitsTime.DAY, Mul(Mul(Int(864), Pow(10, 14)), Sym("picos")));
        c.get(UnitsTime.PICOSECOND).put(UnitsTime.DECASECOND, Mul(Pow(10, 13), Sym("picos")));
        c.get(UnitsTime.PICOSECOND).put(UnitsTime.DECISECOND, Mul(Pow(10, 11), Sym("picos")));
        c.get(UnitsTime.PICOSECOND).put(UnitsTime.EXASECOND, Mul(Pow(10, 30), Sym("picos")));
        c.get(UnitsTime.PICOSECOND).put(UnitsTime.FEMTOSECOND, Mul(Rat(Int(1), Int(1000)), Sym("picos")));
        c.get(UnitsTime.PICOSECOND).put(UnitsTime.GIGASECOND, Mul(Pow(10, 21), Sym("picos")));
        c.get(UnitsTime.PICOSECOND).put(UnitsTime.HECTOSECOND, Mul(Pow(10, 14), Sym("picos")));
        c.get(UnitsTime.PICOSECOND).put(UnitsTime.HOUR, Mul(Mul(Int(36), Pow(10, 14)), Sym("picos")));
        c.get(UnitsTime.PICOSECOND).put(UnitsTime.KILOSECOND, Mul(Pow(10, 15), Sym("picos")));
        c.get(UnitsTime.PICOSECOND).put(UnitsTime.MEGASECOND, Mul(Pow(10, 18), Sym("picos")));
        c.get(UnitsTime.PICOSECOND).put(UnitsTime.MICROSECOND, Mul(Pow(10, 6), Sym("picos")));
        c.get(UnitsTime.PICOSECOND).put(UnitsTime.MILLISECOND, Mul(Pow(10, 9), Sym("picos")));
        c.get(UnitsTime.PICOSECOND).put(UnitsTime.MINUTE, Mul(Mul(Int(6), Pow(10, 13)), Sym("picos")));
        c.get(UnitsTime.PICOSECOND).put(UnitsTime.NANOSECOND, Mul(Int(1000), Sym("picos")));
        c.get(UnitsTime.PICOSECOND).put(UnitsTime.PETASECOND, Mul(Pow(10, 27), Sym("picos")));
        c.get(UnitsTime.PICOSECOND).put(UnitsTime.SECOND, Mul(Pow(10, 12), Sym("picos")));
        c.get(UnitsTime.PICOSECOND).put(UnitsTime.TERASECOND, Mul(Pow(10, 24), Sym("picos")));
        c.get(UnitsTime.PICOSECOND).put(UnitsTime.YOCTOSECOND, Mul(Rat(Int(1), Pow(10, 12)), Sym("picos")));
        c.get(UnitsTime.PICOSECOND).put(UnitsTime.YOTTASECOND, Mul(Pow(10, 36), Sym("picos")));
        c.get(UnitsTime.PICOSECOND).put(UnitsTime.ZEPTOSECOND, Mul(Rat(Int(1), Pow(10, 9)), Sym("picos")));
        c.get(UnitsTime.PICOSECOND).put(UnitsTime.ZETTASECOND, Mul(Pow(10, 33), Sym("picos")));
        c.get(UnitsTime.SECOND).put(UnitsTime.ATTOSECOND, Mul(Rat(Int(1), Pow(10, 18)), Sym("s")));
        c.get(UnitsTime.SECOND).put(UnitsTime.CENTISECOND, Mul(Rat(Int(1), Int(100)), Sym("s")));
        c.get(UnitsTime.SECOND).put(UnitsTime.DAY, Mul(Int(86400), Sym("s")));
        c.get(UnitsTime.SECOND).put(UnitsTime.DECASECOND, Mul(Int(10), Sym("s")));
        c.get(UnitsTime.SECOND).put(UnitsTime.DECISECOND, Mul(Rat(Int(1), Int(10)), Sym("s")));
        c.get(UnitsTime.SECOND).put(UnitsTime.EXASECOND, Mul(Pow(10, 18), Sym("s")));
        c.get(UnitsTime.SECOND).put(UnitsTime.FEMTOSECOND, Mul(Rat(Int(1), Pow(10, 15)), Sym("s")));
        c.get(UnitsTime.SECOND).put(UnitsTime.GIGASECOND, Mul(Pow(10, 9), Sym("s")));
        c.get(UnitsTime.SECOND).put(UnitsTime.HECTOSECOND, Mul(Int(100), Sym("s")));
        c.get(UnitsTime.SECOND).put(UnitsTime.HOUR, Mul(Int(3600), Sym("s")));
        c.get(UnitsTime.SECOND).put(UnitsTime.KILOSECOND, Mul(Int(1000), Sym("s")));
        c.get(UnitsTime.SECOND).put(UnitsTime.MEGASECOND, Mul(Pow(10, 6), Sym("s")));
        c.get(UnitsTime.SECOND).put(UnitsTime.MICROSECOND, Mul(Rat(Int(1), Pow(10, 6)), Sym("s")));
        c.get(UnitsTime.SECOND).put(UnitsTime.MILLISECOND, Mul(Rat(Int(1), Int(1000)), Sym("s")));
        c.get(UnitsTime.SECOND).put(UnitsTime.MINUTE, Mul(Int(60), Sym("s")));
        c.get(UnitsTime.SECOND).put(UnitsTime.NANOSECOND, Mul(Rat(Int(1), Pow(10, 9)), Sym("s")));
        c.get(UnitsTime.SECOND).put(UnitsTime.PETASECOND, Mul(Pow(10, 15), Sym("s")));
        c.get(UnitsTime.SECOND).put(UnitsTime.PICOSECOND, Mul(Rat(Int(1), Pow(10, 12)), Sym("s")));
        c.get(UnitsTime.SECOND).put(UnitsTime.TERASECOND, Mul(Pow(10, 12), Sym("s")));
        c.get(UnitsTime.SECOND).put(UnitsTime.YOCTOSECOND, Mul(Rat(Int(1), Pow(10, 24)), Sym("s")));
        c.get(UnitsTime.SECOND).put(UnitsTime.YOTTASECOND, Mul(Pow(10, 24), Sym("s")));
        c.get(UnitsTime.SECOND).put(UnitsTime.ZEPTOSECOND, Mul(Rat(Int(1), Pow(10, 21)), Sym("s")));
        c.get(UnitsTime.SECOND).put(UnitsTime.ZETTASECOND, Mul(Pow(10, 21), Sym("s")));
        c.get(UnitsTime.TERASECOND).put(UnitsTime.ATTOSECOND, Mul(Rat(Int(1), Pow(10, 30)), Sym("teras")));
        c.get(UnitsTime.TERASECOND).put(UnitsTime.CENTISECOND, Mul(Rat(Int(1), Pow(10, 14)), Sym("teras")));
        c.get(UnitsTime.TERASECOND).put(UnitsTime.DAY, Mul(Rat(Int(27), Mul(Int(3125), Pow(10, 5))), Sym("teras")));
        c.get(UnitsTime.TERASECOND).put(UnitsTime.DECASECOND, Mul(Rat(Int(1), Pow(10, 11)), Sym("teras")));
        c.get(UnitsTime.TERASECOND).put(UnitsTime.DECISECOND, Mul(Rat(Int(1), Pow(10, 13)), Sym("teras")));
        c.get(UnitsTime.TERASECOND).put(UnitsTime.EXASECOND, Mul(Pow(10, 6), Sym("teras")));
        c.get(UnitsTime.TERASECOND).put(UnitsTime.FEMTOSECOND, Mul(Rat(Int(1), Pow(10, 27)), Sym("teras")));
        c.get(UnitsTime.TERASECOND).put(UnitsTime.GIGASECOND, Mul(Rat(Int(1), Int(1000)), Sym("teras")));
        c.get(UnitsTime.TERASECOND).put(UnitsTime.HECTOSECOND, Mul(Rat(Int(1), Pow(10, 10)), Sym("teras")));
        c.get(UnitsTime.TERASECOND).put(UnitsTime.HOUR, Mul(Rat(Int(9), Mul(Int(25), Pow(10, 8))), Sym("teras")));
        c.get(UnitsTime.TERASECOND).put(UnitsTime.KILOSECOND, Mul(Rat(Int(1), Pow(10, 9)), Sym("teras")));
        c.get(UnitsTime.TERASECOND).put(UnitsTime.MEGASECOND, Mul(Rat(Int(1), Pow(10, 6)), Sym("teras")));
        c.get(UnitsTime.TERASECOND).put(UnitsTime.MICROSECOND, Mul(Rat(Int(1), Pow(10, 18)), Sym("teras")));
        c.get(UnitsTime.TERASECOND).put(UnitsTime.MILLISECOND, Mul(Rat(Int(1), Pow(10, 15)), Sym("teras")));
        c.get(UnitsTime.TERASECOND).put(UnitsTime.MINUTE, Mul(Rat(Int(3), Mul(Int(5), Pow(10, 10))), Sym("teras")));
        c.get(UnitsTime.TERASECOND).put(UnitsTime.NANOSECOND, Mul(Rat(Int(1), Pow(10, 21)), Sym("teras")));
        c.get(UnitsTime.TERASECOND).put(UnitsTime.PETASECOND, Mul(Int(1000), Sym("teras")));
        c.get(UnitsTime.TERASECOND).put(UnitsTime.PICOSECOND, Mul(Rat(Int(1), Pow(10, 24)), Sym("teras")));
        c.get(UnitsTime.TERASECOND).put(UnitsTime.SECOND, Mul(Rat(Int(1), Pow(10, 12)), Sym("teras")));
        c.get(UnitsTime.TERASECOND).put(UnitsTime.YOCTOSECOND, Mul(Rat(Int(1), Pow(10, 36)), Sym("teras")));
        c.get(UnitsTime.TERASECOND).put(UnitsTime.YOTTASECOND, Mul(Pow(10, 12), Sym("teras")));
        c.get(UnitsTime.TERASECOND).put(UnitsTime.ZEPTOSECOND, Mul(Rat(Int(1), Pow(10, 33)), Sym("teras")));
        c.get(UnitsTime.TERASECOND).put(UnitsTime.ZETTASECOND, Mul(Pow(10, 9), Sym("teras")));
        c.get(UnitsTime.YOCTOSECOND).put(UnitsTime.ATTOSECOND, Mul(Pow(10, 6), Sym("yoctos")));
        c.get(UnitsTime.YOCTOSECOND).put(UnitsTime.CENTISECOND, Mul(Pow(10, 22), Sym("yoctos")));
        c.get(UnitsTime.YOCTOSECOND).put(UnitsTime.DAY, Mul(Mul(Int(864), Pow(10, 26)), Sym("yoctos")));
        c.get(UnitsTime.YOCTOSECOND).put(UnitsTime.DECASECOND, Mul(Pow(10, 25), Sym("yoctos")));
        c.get(UnitsTime.YOCTOSECOND).put(UnitsTime.DECISECOND, Mul(Pow(10, 23), Sym("yoctos")));
        c.get(UnitsTime.YOCTOSECOND).put(UnitsTime.EXASECOND, Mul(Pow(10, 42), Sym("yoctos")));
        c.get(UnitsTime.YOCTOSECOND).put(UnitsTime.FEMTOSECOND, Mul(Pow(10, 9), Sym("yoctos")));
        c.get(UnitsTime.YOCTOSECOND).put(UnitsTime.GIGASECOND, Mul(Pow(10, 33), Sym("yoctos")));
        c.get(UnitsTime.YOCTOSECOND).put(UnitsTime.HECTOSECOND, Mul(Pow(10, 26), Sym("yoctos")));
        c.get(UnitsTime.YOCTOSECOND).put(UnitsTime.HOUR, Mul(Mul(Int(36), Pow(10, 26)), Sym("yoctos")));
        c.get(UnitsTime.YOCTOSECOND).put(UnitsTime.KILOSECOND, Mul(Pow(10, 27), Sym("yoctos")));
        c.get(UnitsTime.YOCTOSECOND).put(UnitsTime.MEGASECOND, Mul(Pow(10, 30), Sym("yoctos")));
        c.get(UnitsTime.YOCTOSECOND).put(UnitsTime.MICROSECOND, Mul(Pow(10, 18), Sym("yoctos")));
        c.get(UnitsTime.YOCTOSECOND).put(UnitsTime.MILLISECOND, Mul(Pow(10, 21), Sym("yoctos")));
        c.get(UnitsTime.YOCTOSECOND).put(UnitsTime.MINUTE, Mul(Mul(Int(6), Pow(10, 25)), Sym("yoctos")));
        c.get(UnitsTime.YOCTOSECOND).put(UnitsTime.NANOSECOND, Mul(Pow(10, 15), Sym("yoctos")));
        c.get(UnitsTime.YOCTOSECOND).put(UnitsTime.PETASECOND, Mul(Pow(10, 39), Sym("yoctos")));
        c.get(UnitsTime.YOCTOSECOND).put(UnitsTime.PICOSECOND, Mul(Pow(10, 12), Sym("yoctos")));
        c.get(UnitsTime.YOCTOSECOND).put(UnitsTime.SECOND, Mul(Pow(10, 24), Sym("yoctos")));
        c.get(UnitsTime.YOCTOSECOND).put(UnitsTime.TERASECOND, Mul(Pow(10, 36), Sym("yoctos")));
        c.get(UnitsTime.YOCTOSECOND).put(UnitsTime.YOTTASECOND, Mul(Pow(10, 48), Sym("yoctos")));
        c.get(UnitsTime.YOCTOSECOND).put(UnitsTime.ZEPTOSECOND, Mul(Int(1000), Sym("yoctos")));
        c.get(UnitsTime.YOCTOSECOND).put(UnitsTime.ZETTASECOND, Mul(Pow(10, 45), Sym("yoctos")));
        c.get(UnitsTime.YOTTASECOND).put(UnitsTime.ATTOSECOND, Mul(Rat(Int(1), Pow(10, 42)), Sym("yottas")));
        c.get(UnitsTime.YOTTASECOND).put(UnitsTime.CENTISECOND, Mul(Rat(Int(1), Pow(10, 26)), Sym("yottas")));
        c.get(UnitsTime.YOTTASECOND).put(UnitsTime.DAY, Mul(Rat(Int(27), Mul(Int(3125), Pow(10, 17))), Sym("yottas")));
        c.get(UnitsTime.YOTTASECOND).put(UnitsTime.DECASECOND, Mul(Rat(Int(1), Pow(10, 23)), Sym("yottas")));
        c.get(UnitsTime.YOTTASECOND).put(UnitsTime.DECISECOND, Mul(Rat(Int(1), Pow(10, 25)), Sym("yottas")));
        c.get(UnitsTime.YOTTASECOND).put(UnitsTime.EXASECOND, Mul(Rat(Int(1), Pow(10, 6)), Sym("yottas")));
        c.get(UnitsTime.YOTTASECOND).put(UnitsTime.FEMTOSECOND, Mul(Rat(Int(1), Pow(10, 39)), Sym("yottas")));
        c.get(UnitsTime.YOTTASECOND).put(UnitsTime.GIGASECOND, Mul(Rat(Int(1), Pow(10, 15)), Sym("yottas")));
        c.get(UnitsTime.YOTTASECOND).put(UnitsTime.HECTOSECOND, Mul(Rat(Int(1), Pow(10, 22)), Sym("yottas")));
        c.get(UnitsTime.YOTTASECOND).put(UnitsTime.HOUR, Mul(Rat(Int(9), Mul(Int(25), Pow(10, 20))), Sym("yottas")));
        c.get(UnitsTime.YOTTASECOND).put(UnitsTime.KILOSECOND, Mul(Rat(Int(1), Pow(10, 21)), Sym("yottas")));
        c.get(UnitsTime.YOTTASECOND).put(UnitsTime.MEGASECOND, Mul(Rat(Int(1), Pow(10, 18)), Sym("yottas")));
        c.get(UnitsTime.YOTTASECOND).put(UnitsTime.MICROSECOND, Mul(Rat(Int(1), Pow(10, 30)), Sym("yottas")));
        c.get(UnitsTime.YOTTASECOND).put(UnitsTime.MILLISECOND, Mul(Rat(Int(1), Pow(10, 27)), Sym("yottas")));
        c.get(UnitsTime.YOTTASECOND).put(UnitsTime.MINUTE, Mul(Rat(Int(3), Mul(Int(5), Pow(10, 22))), Sym("yottas")));
        c.get(UnitsTime.YOTTASECOND).put(UnitsTime.NANOSECOND, Mul(Rat(Int(1), Pow(10, 33)), Sym("yottas")));
        c.get(UnitsTime.YOTTASECOND).put(UnitsTime.PETASECOND, Mul(Rat(Int(1), Pow(10, 9)), Sym("yottas")));
        c.get(UnitsTime.YOTTASECOND).put(UnitsTime.PICOSECOND, Mul(Rat(Int(1), Pow(10, 36)), Sym("yottas")));
        c.get(UnitsTime.YOTTASECOND).put(UnitsTime.SECOND, Mul(Rat(Int(1), Pow(10, 24)), Sym("yottas")));
        c.get(UnitsTime.YOTTASECOND).put(UnitsTime.TERASECOND, Mul(Rat(Int(1), Pow(10, 12)), Sym("yottas")));
        c.get(UnitsTime.YOTTASECOND).put(UnitsTime.YOCTOSECOND, Mul(Rat(Int(1), Pow(10, 48)), Sym("yottas")));
        c.get(UnitsTime.YOTTASECOND).put(UnitsTime.ZEPTOSECOND, Mul(Rat(Int(1), Pow(10, 45)), Sym("yottas")));
        c.get(UnitsTime.YOTTASECOND).put(UnitsTime.ZETTASECOND, Mul(Rat(Int(1), Int(1000)), Sym("yottas")));
        c.get(UnitsTime.ZEPTOSECOND).put(UnitsTime.ATTOSECOND, Mul(Int(1000), Sym("zeptos")));
        c.get(UnitsTime.ZEPTOSECOND).put(UnitsTime.CENTISECOND, Mul(Pow(10, 19), Sym("zeptos")));
        c.get(UnitsTime.ZEPTOSECOND).put(UnitsTime.DAY, Mul(Mul(Int(864), Pow(10, 23)), Sym("zeptos")));
        c.get(UnitsTime.ZEPTOSECOND).put(UnitsTime.DECASECOND, Mul(Pow(10, 22), Sym("zeptos")));
        c.get(UnitsTime.ZEPTOSECOND).put(UnitsTime.DECISECOND, Mul(Pow(10, 20), Sym("zeptos")));
        c.get(UnitsTime.ZEPTOSECOND).put(UnitsTime.EXASECOND, Mul(Pow(10, 39), Sym("zeptos")));
        c.get(UnitsTime.ZEPTOSECOND).put(UnitsTime.FEMTOSECOND, Mul(Pow(10, 6), Sym("zeptos")));
        c.get(UnitsTime.ZEPTOSECOND).put(UnitsTime.GIGASECOND, Mul(Pow(10, 30), Sym("zeptos")));
        c.get(UnitsTime.ZEPTOSECOND).put(UnitsTime.HECTOSECOND, Mul(Pow(10, 23), Sym("zeptos")));
        c.get(UnitsTime.ZEPTOSECOND).put(UnitsTime.HOUR, Mul(Mul(Int(36), Pow(10, 23)), Sym("zeptos")));
        c.get(UnitsTime.ZEPTOSECOND).put(UnitsTime.KILOSECOND, Mul(Pow(10, 24), Sym("zeptos")));
        c.get(UnitsTime.ZEPTOSECOND).put(UnitsTime.MEGASECOND, Mul(Pow(10, 27), Sym("zeptos")));
        c.get(UnitsTime.ZEPTOSECOND).put(UnitsTime.MICROSECOND, Mul(Pow(10, 15), Sym("zeptos")));
        c.get(UnitsTime.ZEPTOSECOND).put(UnitsTime.MILLISECOND, Mul(Pow(10, 18), Sym("zeptos")));
        c.get(UnitsTime.ZEPTOSECOND).put(UnitsTime.MINUTE, Mul(Mul(Int(6), Pow(10, 22)), Sym("zeptos")));
        c.get(UnitsTime.ZEPTOSECOND).put(UnitsTime.NANOSECOND, Mul(Pow(10, 12), Sym("zeptos")));
        c.get(UnitsTime.ZEPTOSECOND).put(UnitsTime.PETASECOND, Mul(Pow(10, 36), Sym("zeptos")));
        c.get(UnitsTime.ZEPTOSECOND).put(UnitsTime.PICOSECOND, Mul(Pow(10, 9), Sym("zeptos")));
        c.get(UnitsTime.ZEPTOSECOND).put(UnitsTime.SECOND, Mul(Pow(10, 21), Sym("zeptos")));
        c.get(UnitsTime.ZEPTOSECOND).put(UnitsTime.TERASECOND, Mul(Pow(10, 33), Sym("zeptos")));
        c.get(UnitsTime.ZEPTOSECOND).put(UnitsTime.YOCTOSECOND, Mul(Rat(Int(1), Int(1000)), Sym("zeptos")));
        c.get(UnitsTime.ZEPTOSECOND).put(UnitsTime.YOTTASECOND, Mul(Pow(10, 45), Sym("zeptos")));
        c.get(UnitsTime.ZEPTOSECOND).put(UnitsTime.ZETTASECOND, Mul(Pow(10, 42), Sym("zeptos")));
        c.get(UnitsTime.ZETTASECOND).put(UnitsTime.ATTOSECOND, Mul(Rat(Int(1), Pow(10, 39)), Sym("zettas")));
        c.get(UnitsTime.ZETTASECOND).put(UnitsTime.CENTISECOND, Mul(Rat(Int(1), Pow(10, 23)), Sym("zettas")));
        c.get(UnitsTime.ZETTASECOND).put(UnitsTime.DAY, Mul(Rat(Int(27), Mul(Int(3125), Pow(10, 14))), Sym("zettas")));
        c.get(UnitsTime.ZETTASECOND).put(UnitsTime.DECASECOND, Mul(Rat(Int(1), Pow(10, 20)), Sym("zettas")));
        c.get(UnitsTime.ZETTASECOND).put(UnitsTime.DECISECOND, Mul(Rat(Int(1), Pow(10, 22)), Sym("zettas")));
        c.get(UnitsTime.ZETTASECOND).put(UnitsTime.EXASECOND, Mul(Rat(Int(1), Int(1000)), Sym("zettas")));
        c.get(UnitsTime.ZETTASECOND).put(UnitsTime.FEMTOSECOND, Mul(Rat(Int(1), Pow(10, 36)), Sym("zettas")));
        c.get(UnitsTime.ZETTASECOND).put(UnitsTime.GIGASECOND, Mul(Rat(Int(1), Pow(10, 12)), Sym("zettas")));
        c.get(UnitsTime.ZETTASECOND).put(UnitsTime.HECTOSECOND, Mul(Rat(Int(1), Pow(10, 19)), Sym("zettas")));
        c.get(UnitsTime.ZETTASECOND).put(UnitsTime.HOUR, Mul(Rat(Int(9), Mul(Int(25), Pow(10, 17))), Sym("zettas")));
        c.get(UnitsTime.ZETTASECOND).put(UnitsTime.KILOSECOND, Mul(Rat(Int(1), Pow(10, 18)), Sym("zettas")));
        c.get(UnitsTime.ZETTASECOND).put(UnitsTime.MEGASECOND, Mul(Rat(Int(1), Pow(10, 15)), Sym("zettas")));
        c.get(UnitsTime.ZETTASECOND).put(UnitsTime.MICROSECOND, Mul(Rat(Int(1), Pow(10, 27)), Sym("zettas")));
        c.get(UnitsTime.ZETTASECOND).put(UnitsTime.MILLISECOND, Mul(Rat(Int(1), Pow(10, 24)), Sym("zettas")));
        c.get(UnitsTime.ZETTASECOND).put(UnitsTime.MINUTE, Mul(Rat(Int(3), Mul(Int(5), Pow(10, 19))), Sym("zettas")));
        c.get(UnitsTime.ZETTASECOND).put(UnitsTime.NANOSECOND, Mul(Rat(Int(1), Pow(10, 30)), Sym("zettas")));
        c.get(UnitsTime.ZETTASECOND).put(UnitsTime.PETASECOND, Mul(Rat(Int(1), Pow(10, 6)), Sym("zettas")));
        c.get(UnitsTime.ZETTASECOND).put(UnitsTime.PICOSECOND, Mul(Rat(Int(1), Pow(10, 33)), Sym("zettas")));
        c.get(UnitsTime.ZETTASECOND).put(UnitsTime.SECOND, Mul(Rat(Int(1), Pow(10, 21)), Sym("zettas")));
        c.get(UnitsTime.ZETTASECOND).put(UnitsTime.TERASECOND, Mul(Rat(Int(1), Pow(10, 9)), Sym("zettas")));
        c.get(UnitsTime.ZETTASECOND).put(UnitsTime.YOCTOSECOND, Mul(Rat(Int(1), Pow(10, 45)), Sym("zettas")));
        c.get(UnitsTime.ZETTASECOND).put(UnitsTime.YOTTASECOND, Mul(Int(1000), Sym("zettas")));
        c.get(UnitsTime.ZETTASECOND).put(UnitsTime.ZEPTOSECOND, Mul(Rat(Int(1), Pow(10, 42)), Sym("zettas")));
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
            UnitsTime targetUnit = UnitsTime.valueOf(target);
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

