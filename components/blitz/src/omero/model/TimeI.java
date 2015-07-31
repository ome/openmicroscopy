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

    private static Map<UnitsTime, Conversion> createMapATTOSECOND() {
        EnumMap<UnitsTime, Conversion> c =
            new EnumMap<UnitsTime, Conversion>(UnitsTime.class);
        c.put(UnitsTime.CENTISECOND, Mul(Rat(Int(1), Pow(10, 16)), Sym("attos")));
        c.put(UnitsTime.DAY, Mul(Rat(Int(1), Mul(Int(864), Pow(10, 20))), Sym("attos")));
        c.put(UnitsTime.DECASECOND, Mul(Rat(Int(1), Pow(10, 19)), Sym("attos")));
        c.put(UnitsTime.DECISECOND, Mul(Rat(Int(1), Pow(10, 17)), Sym("attos")));
        c.put(UnitsTime.EXASECOND, Mul(Rat(Int(1), Pow(10, 36)), Sym("attos")));
        c.put(UnitsTime.FEMTOSECOND, Mul(Rat(Int(1), Int(1000)), Sym("attos")));
        c.put(UnitsTime.GIGASECOND, Mul(Rat(Int(1), Pow(10, 27)), Sym("attos")));
        c.put(UnitsTime.HECTOSECOND, Mul(Rat(Int(1), Pow(10, 20)), Sym("attos")));
        c.put(UnitsTime.HOUR, Mul(Rat(Int(1), Mul(Int(36), Pow(10, 20))), Sym("attos")));
        c.put(UnitsTime.KILOSECOND, Mul(Rat(Int(1), Pow(10, 21)), Sym("attos")));
        c.put(UnitsTime.MEGASECOND, Mul(Rat(Int(1), Pow(10, 24)), Sym("attos")));
        c.put(UnitsTime.MICROSECOND, Mul(Rat(Int(1), Pow(10, 12)), Sym("attos")));
        c.put(UnitsTime.MILLISECOND, Mul(Rat(Int(1), Pow(10, 15)), Sym("attos")));
        c.put(UnitsTime.MINUTE, Mul(Rat(Int(1), Mul(Int(6), Pow(10, 19))), Sym("attos")));
        c.put(UnitsTime.NANOSECOND, Mul(Rat(Int(1), Pow(10, 9)), Sym("attos")));
        c.put(UnitsTime.PETASECOND, Mul(Rat(Int(1), Pow(10, 33)), Sym("attos")));
        c.put(UnitsTime.PICOSECOND, Mul(Rat(Int(1), Pow(10, 6)), Sym("attos")));
        c.put(UnitsTime.SECOND, Mul(Rat(Int(1), Pow(10, 18)), Sym("attos")));
        c.put(UnitsTime.TERASECOND, Mul(Rat(Int(1), Pow(10, 30)), Sym("attos")));
        c.put(UnitsTime.YOCTOSECOND, Mul(Pow(10, 6), Sym("attos")));
        c.put(UnitsTime.YOTTASECOND, Mul(Rat(Int(1), Pow(10, 42)), Sym("attos")));
        c.put(UnitsTime.ZEPTOSECOND, Mul(Int(1000), Sym("attos")));
        c.put(UnitsTime.ZETTASECOND, Mul(Rat(Int(1), Pow(10, 39)), Sym("attos")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsTime, Conversion> createMapCENTISECOND() {
        EnumMap<UnitsTime, Conversion> c =
            new EnumMap<UnitsTime, Conversion>(UnitsTime.class);
        c.put(UnitsTime.ATTOSECOND, Mul(Pow(10, 16), Sym("centis")));
        c.put(UnitsTime.DAY, Mul(Rat(Int(1), Mul(Int(864), Pow(10, 4))), Sym("centis")));
        c.put(UnitsTime.DECASECOND, Mul(Rat(Int(1), Int(1000)), Sym("centis")));
        c.put(UnitsTime.DECISECOND, Mul(Rat(Int(1), Int(10)), Sym("centis")));
        c.put(UnitsTime.EXASECOND, Mul(Rat(Int(1), Pow(10, 20)), Sym("centis")));
        c.put(UnitsTime.FEMTOSECOND, Mul(Pow(10, 13), Sym("centis")));
        c.put(UnitsTime.GIGASECOND, Mul(Rat(Int(1), Pow(10, 11)), Sym("centis")));
        c.put(UnitsTime.HECTOSECOND, Mul(Rat(Int(1), Pow(10, 4)), Sym("centis")));
        c.put(UnitsTime.HOUR, Mul(Rat(Int(1), Mul(Int(36), Pow(10, 4))), Sym("centis")));
        c.put(UnitsTime.KILOSECOND, Mul(Rat(Int(1), Pow(10, 5)), Sym("centis")));
        c.put(UnitsTime.MEGASECOND, Mul(Rat(Int(1), Pow(10, 8)), Sym("centis")));
        c.put(UnitsTime.MICROSECOND, Mul(Pow(10, 4), Sym("centis")));
        c.put(UnitsTime.MILLISECOND, Mul(Int(10), Sym("centis")));
        c.put(UnitsTime.MINUTE, Mul(Rat(Int(1), Int(6000)), Sym("centis")));
        c.put(UnitsTime.NANOSECOND, Mul(Pow(10, 7), Sym("centis")));
        c.put(UnitsTime.PETASECOND, Mul(Rat(Int(1), Pow(10, 17)), Sym("centis")));
        c.put(UnitsTime.PICOSECOND, Mul(Pow(10, 10), Sym("centis")));
        c.put(UnitsTime.SECOND, Mul(Rat(Int(1), Int(100)), Sym("centis")));
        c.put(UnitsTime.TERASECOND, Mul(Rat(Int(1), Pow(10, 14)), Sym("centis")));
        c.put(UnitsTime.YOCTOSECOND, Mul(Pow(10, 22), Sym("centis")));
        c.put(UnitsTime.YOTTASECOND, Mul(Rat(Int(1), Pow(10, 26)), Sym("centis")));
        c.put(UnitsTime.ZEPTOSECOND, Mul(Pow(10, 19), Sym("centis")));
        c.put(UnitsTime.ZETTASECOND, Mul(Rat(Int(1), Pow(10, 23)), Sym("centis")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsTime, Conversion> createMapDAY() {
        EnumMap<UnitsTime, Conversion> c =
            new EnumMap<UnitsTime, Conversion>(UnitsTime.class);
        c.put(UnitsTime.ATTOSECOND, Mul(Mul(Int(864), Pow(10, 20)), Sym("d")));
        c.put(UnitsTime.CENTISECOND, Mul(Mul(Int(864), Pow(10, 4)), Sym("d")));
        c.put(UnitsTime.DECASECOND, Mul(Int(8640), Sym("d")));
        c.put(UnitsTime.DECISECOND, Mul(Int(864000), Sym("d")));
        c.put(UnitsTime.EXASECOND, Mul(Rat(Int(27), Mul(Int(3125), Pow(10, 11))), Sym("d")));
        c.put(UnitsTime.FEMTOSECOND, Mul(Mul(Int(864), Pow(10, 17)), Sym("d")));
        c.put(UnitsTime.GIGASECOND, Mul(Rat(Int(27), Int(312500)), Sym("d")));
        c.put(UnitsTime.HECTOSECOND, Mul(Int(864), Sym("d")));
        c.put(UnitsTime.HOUR, Mul(Int(24), Sym("d")));
        c.put(UnitsTime.KILOSECOND, Mul(Rat(Int(432), Int(5)), Sym("d")));
        c.put(UnitsTime.MEGASECOND, Mul(Rat(Int(54), Int(625)), Sym("d")));
        c.put(UnitsTime.MICROSECOND, Mul(Mul(Int(864), Pow(10, 8)), Sym("d")));
        c.put(UnitsTime.MILLISECOND, Mul(Mul(Int(864), Pow(10, 5)), Sym("d")));
        c.put(UnitsTime.MINUTE, Mul(Int(1440), Sym("d")));
        c.put(UnitsTime.NANOSECOND, Mul(Mul(Int(864), Pow(10, 11)), Sym("d")));
        c.put(UnitsTime.PETASECOND, Mul(Rat(Int(27), Mul(Int(3125), Pow(10, 8))), Sym("d")));
        c.put(UnitsTime.PICOSECOND, Mul(Mul(Int(864), Pow(10, 14)), Sym("d")));
        c.put(UnitsTime.SECOND, Mul(Int(86400), Sym("d")));
        c.put(UnitsTime.TERASECOND, Mul(Rat(Int(27), Mul(Int(3125), Pow(10, 5))), Sym("d")));
        c.put(UnitsTime.YOCTOSECOND, Mul(Mul(Int(864), Pow(10, 26)), Sym("d")));
        c.put(UnitsTime.YOTTASECOND, Mul(Rat(Int(27), Mul(Int(3125), Pow(10, 17))), Sym("d")));
        c.put(UnitsTime.ZEPTOSECOND, Mul(Mul(Int(864), Pow(10, 23)), Sym("d")));
        c.put(UnitsTime.ZETTASECOND, Mul(Rat(Int(27), Mul(Int(3125), Pow(10, 14))), Sym("d")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsTime, Conversion> createMapDECASECOND() {
        EnumMap<UnitsTime, Conversion> c =
            new EnumMap<UnitsTime, Conversion>(UnitsTime.class);
        c.put(UnitsTime.ATTOSECOND, Mul(Pow(10, 19), Sym("decas")));
        c.put(UnitsTime.CENTISECOND, Mul(Int(1000), Sym("decas")));
        c.put(UnitsTime.DAY, Mul(Rat(Int(1), Int(8640)), Sym("decas")));
        c.put(UnitsTime.DECISECOND, Mul(Int(100), Sym("decas")));
        c.put(UnitsTime.EXASECOND, Mul(Rat(Int(1), Pow(10, 17)), Sym("decas")));
        c.put(UnitsTime.FEMTOSECOND, Mul(Pow(10, 16), Sym("decas")));
        c.put(UnitsTime.GIGASECOND, Mul(Rat(Int(1), Pow(10, 8)), Sym("decas")));
        c.put(UnitsTime.HECTOSECOND, Mul(Rat(Int(1), Int(10)), Sym("decas")));
        c.put(UnitsTime.HOUR, Mul(Rat(Int(1), Int(360)), Sym("decas")));
        c.put(UnitsTime.KILOSECOND, Mul(Rat(Int(1), Int(100)), Sym("decas")));
        c.put(UnitsTime.MEGASECOND, Mul(Rat(Int(1), Pow(10, 5)), Sym("decas")));
        c.put(UnitsTime.MICROSECOND, Mul(Pow(10, 7), Sym("decas")));
        c.put(UnitsTime.MILLISECOND, Mul(Pow(10, 4), Sym("decas")));
        c.put(UnitsTime.MINUTE, Mul(Rat(Int(1), Int(6)), Sym("decas")));
        c.put(UnitsTime.NANOSECOND, Mul(Pow(10, 10), Sym("decas")));
        c.put(UnitsTime.PETASECOND, Mul(Rat(Int(1), Pow(10, 14)), Sym("decas")));
        c.put(UnitsTime.PICOSECOND, Mul(Pow(10, 13), Sym("decas")));
        c.put(UnitsTime.SECOND, Mul(Int(10), Sym("decas")));
        c.put(UnitsTime.TERASECOND, Mul(Rat(Int(1), Pow(10, 11)), Sym("decas")));
        c.put(UnitsTime.YOCTOSECOND, Mul(Pow(10, 25), Sym("decas")));
        c.put(UnitsTime.YOTTASECOND, Mul(Rat(Int(1), Pow(10, 23)), Sym("decas")));
        c.put(UnitsTime.ZEPTOSECOND, Mul(Pow(10, 22), Sym("decas")));
        c.put(UnitsTime.ZETTASECOND, Mul(Rat(Int(1), Pow(10, 20)), Sym("decas")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsTime, Conversion> createMapDECISECOND() {
        EnumMap<UnitsTime, Conversion> c =
            new EnumMap<UnitsTime, Conversion>(UnitsTime.class);
        c.put(UnitsTime.ATTOSECOND, Mul(Pow(10, 17), Sym("decis")));
        c.put(UnitsTime.CENTISECOND, Mul(Int(10), Sym("decis")));
        c.put(UnitsTime.DAY, Mul(Rat(Int(1), Int(864000)), Sym("decis")));
        c.put(UnitsTime.DECASECOND, Mul(Rat(Int(1), Int(100)), Sym("decis")));
        c.put(UnitsTime.EXASECOND, Mul(Rat(Int(1), Pow(10, 19)), Sym("decis")));
        c.put(UnitsTime.FEMTOSECOND, Mul(Pow(10, 14), Sym("decis")));
        c.put(UnitsTime.GIGASECOND, Mul(Rat(Int(1), Pow(10, 10)), Sym("decis")));
        c.put(UnitsTime.HECTOSECOND, Mul(Rat(Int(1), Int(1000)), Sym("decis")));
        c.put(UnitsTime.HOUR, Mul(Rat(Int(1), Int(36000)), Sym("decis")));
        c.put(UnitsTime.KILOSECOND, Mul(Rat(Int(1), Pow(10, 4)), Sym("decis")));
        c.put(UnitsTime.MEGASECOND, Mul(Rat(Int(1), Pow(10, 7)), Sym("decis")));
        c.put(UnitsTime.MICROSECOND, Mul(Pow(10, 5), Sym("decis")));
        c.put(UnitsTime.MILLISECOND, Mul(Int(100), Sym("decis")));
        c.put(UnitsTime.MINUTE, Mul(Rat(Int(1), Int(600)), Sym("decis")));
        c.put(UnitsTime.NANOSECOND, Mul(Pow(10, 8), Sym("decis")));
        c.put(UnitsTime.PETASECOND, Mul(Rat(Int(1), Pow(10, 16)), Sym("decis")));
        c.put(UnitsTime.PICOSECOND, Mul(Pow(10, 11), Sym("decis")));
        c.put(UnitsTime.SECOND, Mul(Rat(Int(1), Int(10)), Sym("decis")));
        c.put(UnitsTime.TERASECOND, Mul(Rat(Int(1), Pow(10, 13)), Sym("decis")));
        c.put(UnitsTime.YOCTOSECOND, Mul(Pow(10, 23), Sym("decis")));
        c.put(UnitsTime.YOTTASECOND, Mul(Rat(Int(1), Pow(10, 25)), Sym("decis")));
        c.put(UnitsTime.ZEPTOSECOND, Mul(Pow(10, 20), Sym("decis")));
        c.put(UnitsTime.ZETTASECOND, Mul(Rat(Int(1), Pow(10, 22)), Sym("decis")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsTime, Conversion> createMapEXASECOND() {
        EnumMap<UnitsTime, Conversion> c =
            new EnumMap<UnitsTime, Conversion>(UnitsTime.class);
        c.put(UnitsTime.ATTOSECOND, Mul(Pow(10, 36), Sym("exas")));
        c.put(UnitsTime.CENTISECOND, Mul(Pow(10, 20), Sym("exas")));
        c.put(UnitsTime.DAY, Mul(Rat(Mul(Int(3125), Pow(10, 11)), Int(27)), Sym("exas")));
        c.put(UnitsTime.DECASECOND, Mul(Pow(10, 17), Sym("exas")));
        c.put(UnitsTime.DECISECOND, Mul(Pow(10, 19), Sym("exas")));
        c.put(UnitsTime.FEMTOSECOND, Mul(Pow(10, 33), Sym("exas")));
        c.put(UnitsTime.GIGASECOND, Mul(Pow(10, 9), Sym("exas")));
        c.put(UnitsTime.HECTOSECOND, Mul(Pow(10, 16), Sym("exas")));
        c.put(UnitsTime.HOUR, Mul(Rat(Mul(Int(25), Pow(10, 14)), Int(9)), Sym("exas")));
        c.put(UnitsTime.KILOSECOND, Mul(Pow(10, 15), Sym("exas")));
        c.put(UnitsTime.MEGASECOND, Mul(Pow(10, 12), Sym("exas")));
        c.put(UnitsTime.MICROSECOND, Mul(Pow(10, 24), Sym("exas")));
        c.put(UnitsTime.MILLISECOND, Mul(Pow(10, 21), Sym("exas")));
        c.put(UnitsTime.MINUTE, Mul(Rat(Mul(Int(5), Pow(10, 16)), Int(3)), Sym("exas")));
        c.put(UnitsTime.NANOSECOND, Mul(Pow(10, 27), Sym("exas")));
        c.put(UnitsTime.PETASECOND, Mul(Int(1000), Sym("exas")));
        c.put(UnitsTime.PICOSECOND, Mul(Pow(10, 30), Sym("exas")));
        c.put(UnitsTime.SECOND, Mul(Pow(10, 18), Sym("exas")));
        c.put(UnitsTime.TERASECOND, Mul(Pow(10, 6), Sym("exas")));
        c.put(UnitsTime.YOCTOSECOND, Mul(Pow(10, 42), Sym("exas")));
        c.put(UnitsTime.YOTTASECOND, Mul(Rat(Int(1), Pow(10, 6)), Sym("exas")));
        c.put(UnitsTime.ZEPTOSECOND, Mul(Pow(10, 39), Sym("exas")));
        c.put(UnitsTime.ZETTASECOND, Mul(Rat(Int(1), Int(1000)), Sym("exas")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsTime, Conversion> createMapFEMTOSECOND() {
        EnumMap<UnitsTime, Conversion> c =
            new EnumMap<UnitsTime, Conversion>(UnitsTime.class);
        c.put(UnitsTime.ATTOSECOND, Mul(Int(1000), Sym("femtos")));
        c.put(UnitsTime.CENTISECOND, Mul(Rat(Int(1), Pow(10, 13)), Sym("femtos")));
        c.put(UnitsTime.DAY, Mul(Rat(Int(1), Mul(Int(864), Pow(10, 17))), Sym("femtos")));
        c.put(UnitsTime.DECASECOND, Mul(Rat(Int(1), Pow(10, 16)), Sym("femtos")));
        c.put(UnitsTime.DECISECOND, Mul(Rat(Int(1), Pow(10, 14)), Sym("femtos")));
        c.put(UnitsTime.EXASECOND, Mul(Rat(Int(1), Pow(10, 33)), Sym("femtos")));
        c.put(UnitsTime.GIGASECOND, Mul(Rat(Int(1), Pow(10, 24)), Sym("femtos")));
        c.put(UnitsTime.HECTOSECOND, Mul(Rat(Int(1), Pow(10, 17)), Sym("femtos")));
        c.put(UnitsTime.HOUR, Mul(Rat(Int(1), Mul(Int(36), Pow(10, 17))), Sym("femtos")));
        c.put(UnitsTime.KILOSECOND, Mul(Rat(Int(1), Pow(10, 18)), Sym("femtos")));
        c.put(UnitsTime.MEGASECOND, Mul(Rat(Int(1), Pow(10, 21)), Sym("femtos")));
        c.put(UnitsTime.MICROSECOND, Mul(Rat(Int(1), Pow(10, 9)), Sym("femtos")));
        c.put(UnitsTime.MILLISECOND, Mul(Rat(Int(1), Pow(10, 12)), Sym("femtos")));
        c.put(UnitsTime.MINUTE, Mul(Rat(Int(1), Mul(Int(6), Pow(10, 16))), Sym("femtos")));
        c.put(UnitsTime.NANOSECOND, Mul(Rat(Int(1), Pow(10, 6)), Sym("femtos")));
        c.put(UnitsTime.PETASECOND, Mul(Rat(Int(1), Pow(10, 30)), Sym("femtos")));
        c.put(UnitsTime.PICOSECOND, Mul(Rat(Int(1), Int(1000)), Sym("femtos")));
        c.put(UnitsTime.SECOND, Mul(Rat(Int(1), Pow(10, 15)), Sym("femtos")));
        c.put(UnitsTime.TERASECOND, Mul(Rat(Int(1), Pow(10, 27)), Sym("femtos")));
        c.put(UnitsTime.YOCTOSECOND, Mul(Pow(10, 9), Sym("femtos")));
        c.put(UnitsTime.YOTTASECOND, Mul(Rat(Int(1), Pow(10, 39)), Sym("femtos")));
        c.put(UnitsTime.ZEPTOSECOND, Mul(Pow(10, 6), Sym("femtos")));
        c.put(UnitsTime.ZETTASECOND, Mul(Rat(Int(1), Pow(10, 36)), Sym("femtos")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsTime, Conversion> createMapGIGASECOND() {
        EnumMap<UnitsTime, Conversion> c =
            new EnumMap<UnitsTime, Conversion>(UnitsTime.class);
        c.put(UnitsTime.ATTOSECOND, Mul(Pow(10, 27), Sym("gigas")));
        c.put(UnitsTime.CENTISECOND, Mul(Pow(10, 11), Sym("gigas")));
        c.put(UnitsTime.DAY, Mul(Rat(Int(312500), Int(27)), Sym("gigas")));
        c.put(UnitsTime.DECASECOND, Mul(Pow(10, 8), Sym("gigas")));
        c.put(UnitsTime.DECISECOND, Mul(Pow(10, 10), Sym("gigas")));
        c.put(UnitsTime.EXASECOND, Mul(Rat(Int(1), Pow(10, 9)), Sym("gigas")));
        c.put(UnitsTime.FEMTOSECOND, Mul(Pow(10, 24), Sym("gigas")));
        c.put(UnitsTime.HECTOSECOND, Mul(Pow(10, 7), Sym("gigas")));
        c.put(UnitsTime.HOUR, Mul(Rat(Mul(Int(25), Pow(10, 5)), Int(9)), Sym("gigas")));
        c.put(UnitsTime.KILOSECOND, Mul(Pow(10, 6), Sym("gigas")));
        c.put(UnitsTime.MEGASECOND, Mul(Int(1000), Sym("gigas")));
        c.put(UnitsTime.MICROSECOND, Mul(Pow(10, 15), Sym("gigas")));
        c.put(UnitsTime.MILLISECOND, Mul(Pow(10, 12), Sym("gigas")));
        c.put(UnitsTime.MINUTE, Mul(Rat(Mul(Int(5), Pow(10, 7)), Int(3)), Sym("gigas")));
        c.put(UnitsTime.NANOSECOND, Mul(Pow(10, 18), Sym("gigas")));
        c.put(UnitsTime.PETASECOND, Mul(Rat(Int(1), Pow(10, 6)), Sym("gigas")));
        c.put(UnitsTime.PICOSECOND, Mul(Pow(10, 21), Sym("gigas")));
        c.put(UnitsTime.SECOND, Mul(Pow(10, 9), Sym("gigas")));
        c.put(UnitsTime.TERASECOND, Mul(Rat(Int(1), Int(1000)), Sym("gigas")));
        c.put(UnitsTime.YOCTOSECOND, Mul(Pow(10, 33), Sym("gigas")));
        c.put(UnitsTime.YOTTASECOND, Mul(Rat(Int(1), Pow(10, 15)), Sym("gigas")));
        c.put(UnitsTime.ZEPTOSECOND, Mul(Pow(10, 30), Sym("gigas")));
        c.put(UnitsTime.ZETTASECOND, Mul(Rat(Int(1), Pow(10, 12)), Sym("gigas")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsTime, Conversion> createMapHECTOSECOND() {
        EnumMap<UnitsTime, Conversion> c =
            new EnumMap<UnitsTime, Conversion>(UnitsTime.class);
        c.put(UnitsTime.ATTOSECOND, Mul(Pow(10, 20), Sym("hectos")));
        c.put(UnitsTime.CENTISECOND, Mul(Pow(10, 4), Sym("hectos")));
        c.put(UnitsTime.DAY, Mul(Rat(Int(1), Int(864)), Sym("hectos")));
        c.put(UnitsTime.DECASECOND, Mul(Int(10), Sym("hectos")));
        c.put(UnitsTime.DECISECOND, Mul(Int(1000), Sym("hectos")));
        c.put(UnitsTime.EXASECOND, Mul(Rat(Int(1), Pow(10, 16)), Sym("hectos")));
        c.put(UnitsTime.FEMTOSECOND, Mul(Pow(10, 17), Sym("hectos")));
        c.put(UnitsTime.GIGASECOND, Mul(Rat(Int(1), Pow(10, 7)), Sym("hectos")));
        c.put(UnitsTime.HOUR, Mul(Rat(Int(1), Int(36)), Sym("hectos")));
        c.put(UnitsTime.KILOSECOND, Mul(Rat(Int(1), Int(10)), Sym("hectos")));
        c.put(UnitsTime.MEGASECOND, Mul(Rat(Int(1), Pow(10, 4)), Sym("hectos")));
        c.put(UnitsTime.MICROSECOND, Mul(Pow(10, 8), Sym("hectos")));
        c.put(UnitsTime.MILLISECOND, Mul(Pow(10, 5), Sym("hectos")));
        c.put(UnitsTime.MINUTE, Mul(Rat(Int(5), Int(3)), Sym("hectos")));
        c.put(UnitsTime.NANOSECOND, Mul(Pow(10, 11), Sym("hectos")));
        c.put(UnitsTime.PETASECOND, Mul(Rat(Int(1), Pow(10, 13)), Sym("hectos")));
        c.put(UnitsTime.PICOSECOND, Mul(Pow(10, 14), Sym("hectos")));
        c.put(UnitsTime.SECOND, Mul(Int(100), Sym("hectos")));
        c.put(UnitsTime.TERASECOND, Mul(Rat(Int(1), Pow(10, 10)), Sym("hectos")));
        c.put(UnitsTime.YOCTOSECOND, Mul(Pow(10, 26), Sym("hectos")));
        c.put(UnitsTime.YOTTASECOND, Mul(Rat(Int(1), Pow(10, 22)), Sym("hectos")));
        c.put(UnitsTime.ZEPTOSECOND, Mul(Pow(10, 23), Sym("hectos")));
        c.put(UnitsTime.ZETTASECOND, Mul(Rat(Int(1), Pow(10, 19)), Sym("hectos")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsTime, Conversion> createMapHOUR() {
        EnumMap<UnitsTime, Conversion> c =
            new EnumMap<UnitsTime, Conversion>(UnitsTime.class);
        c.put(UnitsTime.ATTOSECOND, Mul(Mul(Int(36), Pow(10, 20)), Sym("h")));
        c.put(UnitsTime.CENTISECOND, Mul(Mul(Int(36), Pow(10, 4)), Sym("h")));
        c.put(UnitsTime.DAY, Mul(Rat(Int(1), Int(24)), Sym("h")));
        c.put(UnitsTime.DECASECOND, Mul(Int(360), Sym("h")));
        c.put(UnitsTime.DECISECOND, Mul(Int(36000), Sym("h")));
        c.put(UnitsTime.EXASECOND, Mul(Rat(Int(9), Mul(Int(25), Pow(10, 14))), Sym("h")));
        c.put(UnitsTime.FEMTOSECOND, Mul(Mul(Int(36), Pow(10, 17)), Sym("h")));
        c.put(UnitsTime.GIGASECOND, Mul(Rat(Int(9), Mul(Int(25), Pow(10, 5))), Sym("h")));
        c.put(UnitsTime.HECTOSECOND, Mul(Int(36), Sym("h")));
        c.put(UnitsTime.KILOSECOND, Mul(Rat(Int(18), Int(5)), Sym("h")));
        c.put(UnitsTime.MEGASECOND, Mul(Rat(Int(9), Int(2500)), Sym("h")));
        c.put(UnitsTime.MICROSECOND, Mul(Mul(Int(36), Pow(10, 8)), Sym("h")));
        c.put(UnitsTime.MILLISECOND, Mul(Mul(Int(36), Pow(10, 5)), Sym("h")));
        c.put(UnitsTime.MINUTE, Mul(Int(60), Sym("h")));
        c.put(UnitsTime.NANOSECOND, Mul(Mul(Int(36), Pow(10, 11)), Sym("h")));
        c.put(UnitsTime.PETASECOND, Mul(Rat(Int(9), Mul(Int(25), Pow(10, 11))), Sym("h")));
        c.put(UnitsTime.PICOSECOND, Mul(Mul(Int(36), Pow(10, 14)), Sym("h")));
        c.put(UnitsTime.SECOND, Mul(Int(3600), Sym("h")));
        c.put(UnitsTime.TERASECOND, Mul(Rat(Int(9), Mul(Int(25), Pow(10, 8))), Sym("h")));
        c.put(UnitsTime.YOCTOSECOND, Mul(Mul(Int(36), Pow(10, 26)), Sym("h")));
        c.put(UnitsTime.YOTTASECOND, Mul(Rat(Int(9), Mul(Int(25), Pow(10, 20))), Sym("h")));
        c.put(UnitsTime.ZEPTOSECOND, Mul(Mul(Int(36), Pow(10, 23)), Sym("h")));
        c.put(UnitsTime.ZETTASECOND, Mul(Rat(Int(9), Mul(Int(25), Pow(10, 17))), Sym("h")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsTime, Conversion> createMapKILOSECOND() {
        EnumMap<UnitsTime, Conversion> c =
            new EnumMap<UnitsTime, Conversion>(UnitsTime.class);
        c.put(UnitsTime.ATTOSECOND, Mul(Pow(10, 21), Sym("kilos")));
        c.put(UnitsTime.CENTISECOND, Mul(Pow(10, 5), Sym("kilos")));
        c.put(UnitsTime.DAY, Mul(Rat(Int(5), Int(432)), Sym("kilos")));
        c.put(UnitsTime.DECASECOND, Mul(Int(100), Sym("kilos")));
        c.put(UnitsTime.DECISECOND, Mul(Pow(10, 4), Sym("kilos")));
        c.put(UnitsTime.EXASECOND, Mul(Rat(Int(1), Pow(10, 15)), Sym("kilos")));
        c.put(UnitsTime.FEMTOSECOND, Mul(Pow(10, 18), Sym("kilos")));
        c.put(UnitsTime.GIGASECOND, Mul(Rat(Int(1), Pow(10, 6)), Sym("kilos")));
        c.put(UnitsTime.HECTOSECOND, Mul(Int(10), Sym("kilos")));
        c.put(UnitsTime.HOUR, Mul(Rat(Int(5), Int(18)), Sym("kilos")));
        c.put(UnitsTime.MEGASECOND, Mul(Rat(Int(1), Int(1000)), Sym("kilos")));
        c.put(UnitsTime.MICROSECOND, Mul(Pow(10, 9), Sym("kilos")));
        c.put(UnitsTime.MILLISECOND, Mul(Pow(10, 6), Sym("kilos")));
        c.put(UnitsTime.MINUTE, Mul(Rat(Int(50), Int(3)), Sym("kilos")));
        c.put(UnitsTime.NANOSECOND, Mul(Pow(10, 12), Sym("kilos")));
        c.put(UnitsTime.PETASECOND, Mul(Rat(Int(1), Pow(10, 12)), Sym("kilos")));
        c.put(UnitsTime.PICOSECOND, Mul(Pow(10, 15), Sym("kilos")));
        c.put(UnitsTime.SECOND, Mul(Int(1000), Sym("kilos")));
        c.put(UnitsTime.TERASECOND, Mul(Rat(Int(1), Pow(10, 9)), Sym("kilos")));
        c.put(UnitsTime.YOCTOSECOND, Mul(Pow(10, 27), Sym("kilos")));
        c.put(UnitsTime.YOTTASECOND, Mul(Rat(Int(1), Pow(10, 21)), Sym("kilos")));
        c.put(UnitsTime.ZEPTOSECOND, Mul(Pow(10, 24), Sym("kilos")));
        c.put(UnitsTime.ZETTASECOND, Mul(Rat(Int(1), Pow(10, 18)), Sym("kilos")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsTime, Conversion> createMapMEGASECOND() {
        EnumMap<UnitsTime, Conversion> c =
            new EnumMap<UnitsTime, Conversion>(UnitsTime.class);
        c.put(UnitsTime.ATTOSECOND, Mul(Pow(10, 24), Sym("megas")));
        c.put(UnitsTime.CENTISECOND, Mul(Pow(10, 8), Sym("megas")));
        c.put(UnitsTime.DAY, Mul(Rat(Int(625), Int(54)), Sym("megas")));
        c.put(UnitsTime.DECASECOND, Mul(Pow(10, 5), Sym("megas")));
        c.put(UnitsTime.DECISECOND, Mul(Pow(10, 7), Sym("megas")));
        c.put(UnitsTime.EXASECOND, Mul(Rat(Int(1), Pow(10, 12)), Sym("megas")));
        c.put(UnitsTime.FEMTOSECOND, Mul(Pow(10, 21), Sym("megas")));
        c.put(UnitsTime.GIGASECOND, Mul(Rat(Int(1), Int(1000)), Sym("megas")));
        c.put(UnitsTime.HECTOSECOND, Mul(Pow(10, 4), Sym("megas")));
        c.put(UnitsTime.HOUR, Mul(Rat(Int(2500), Int(9)), Sym("megas")));
        c.put(UnitsTime.KILOSECOND, Mul(Int(1000), Sym("megas")));
        c.put(UnitsTime.MICROSECOND, Mul(Pow(10, 12), Sym("megas")));
        c.put(UnitsTime.MILLISECOND, Mul(Pow(10, 9), Sym("megas")));
        c.put(UnitsTime.MINUTE, Mul(Rat(Mul(Int(5), Pow(10, 4)), Int(3)), Sym("megas")));
        c.put(UnitsTime.NANOSECOND, Mul(Pow(10, 15), Sym("megas")));
        c.put(UnitsTime.PETASECOND, Mul(Rat(Int(1), Pow(10, 9)), Sym("megas")));
        c.put(UnitsTime.PICOSECOND, Mul(Pow(10, 18), Sym("megas")));
        c.put(UnitsTime.SECOND, Mul(Pow(10, 6), Sym("megas")));
        c.put(UnitsTime.TERASECOND, Mul(Rat(Int(1), Pow(10, 6)), Sym("megas")));
        c.put(UnitsTime.YOCTOSECOND, Mul(Pow(10, 30), Sym("megas")));
        c.put(UnitsTime.YOTTASECOND, Mul(Rat(Int(1), Pow(10, 18)), Sym("megas")));
        c.put(UnitsTime.ZEPTOSECOND, Mul(Pow(10, 27), Sym("megas")));
        c.put(UnitsTime.ZETTASECOND, Mul(Rat(Int(1), Pow(10, 15)), Sym("megas")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsTime, Conversion> createMapMICROSECOND() {
        EnumMap<UnitsTime, Conversion> c =
            new EnumMap<UnitsTime, Conversion>(UnitsTime.class);
        c.put(UnitsTime.ATTOSECOND, Mul(Pow(10, 12), Sym("micros")));
        c.put(UnitsTime.CENTISECOND, Mul(Rat(Int(1), Pow(10, 4)), Sym("micros")));
        c.put(UnitsTime.DAY, Mul(Rat(Int(1), Mul(Int(864), Pow(10, 8))), Sym("micros")));
        c.put(UnitsTime.DECASECOND, Mul(Rat(Int(1), Pow(10, 7)), Sym("micros")));
        c.put(UnitsTime.DECISECOND, Mul(Rat(Int(1), Pow(10, 5)), Sym("micros")));
        c.put(UnitsTime.EXASECOND, Mul(Rat(Int(1), Pow(10, 24)), Sym("micros")));
        c.put(UnitsTime.FEMTOSECOND, Mul(Pow(10, 9), Sym("micros")));
        c.put(UnitsTime.GIGASECOND, Mul(Rat(Int(1), Pow(10, 15)), Sym("micros")));
        c.put(UnitsTime.HECTOSECOND, Mul(Rat(Int(1), Pow(10, 8)), Sym("micros")));
        c.put(UnitsTime.HOUR, Mul(Rat(Int(1), Mul(Int(36), Pow(10, 8))), Sym("micros")));
        c.put(UnitsTime.KILOSECOND, Mul(Rat(Int(1), Pow(10, 9)), Sym("micros")));
        c.put(UnitsTime.MEGASECOND, Mul(Rat(Int(1), Pow(10, 12)), Sym("micros")));
        c.put(UnitsTime.MILLISECOND, Mul(Rat(Int(1), Int(1000)), Sym("micros")));
        c.put(UnitsTime.MINUTE, Mul(Rat(Int(1), Mul(Int(6), Pow(10, 7))), Sym("micros")));
        c.put(UnitsTime.NANOSECOND, Mul(Int(1000), Sym("micros")));
        c.put(UnitsTime.PETASECOND, Mul(Rat(Int(1), Pow(10, 21)), Sym("micros")));
        c.put(UnitsTime.PICOSECOND, Mul(Pow(10, 6), Sym("micros")));
        c.put(UnitsTime.SECOND, Mul(Rat(Int(1), Pow(10, 6)), Sym("micros")));
        c.put(UnitsTime.TERASECOND, Mul(Rat(Int(1), Pow(10, 18)), Sym("micros")));
        c.put(UnitsTime.YOCTOSECOND, Mul(Pow(10, 18), Sym("micros")));
        c.put(UnitsTime.YOTTASECOND, Mul(Rat(Int(1), Pow(10, 30)), Sym("micros")));
        c.put(UnitsTime.ZEPTOSECOND, Mul(Pow(10, 15), Sym("micros")));
        c.put(UnitsTime.ZETTASECOND, Mul(Rat(Int(1), Pow(10, 27)), Sym("micros")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsTime, Conversion> createMapMILLISECOND() {
        EnumMap<UnitsTime, Conversion> c =
            new EnumMap<UnitsTime, Conversion>(UnitsTime.class);
        c.put(UnitsTime.ATTOSECOND, Mul(Pow(10, 15), Sym("millis")));
        c.put(UnitsTime.CENTISECOND, Mul(Rat(Int(1), Int(10)), Sym("millis")));
        c.put(UnitsTime.DAY, Mul(Rat(Int(1), Mul(Int(864), Pow(10, 5))), Sym("millis")));
        c.put(UnitsTime.DECASECOND, Mul(Rat(Int(1), Pow(10, 4)), Sym("millis")));
        c.put(UnitsTime.DECISECOND, Mul(Rat(Int(1), Int(100)), Sym("millis")));
        c.put(UnitsTime.EXASECOND, Mul(Rat(Int(1), Pow(10, 21)), Sym("millis")));
        c.put(UnitsTime.FEMTOSECOND, Mul(Pow(10, 12), Sym("millis")));
        c.put(UnitsTime.GIGASECOND, Mul(Rat(Int(1), Pow(10, 12)), Sym("millis")));
        c.put(UnitsTime.HECTOSECOND, Mul(Rat(Int(1), Pow(10, 5)), Sym("millis")));
        c.put(UnitsTime.HOUR, Mul(Rat(Int(1), Mul(Int(36), Pow(10, 5))), Sym("millis")));
        c.put(UnitsTime.KILOSECOND, Mul(Rat(Int(1), Pow(10, 6)), Sym("millis")));
        c.put(UnitsTime.MEGASECOND, Mul(Rat(Int(1), Pow(10, 9)), Sym("millis")));
        c.put(UnitsTime.MICROSECOND, Mul(Int(1000), Sym("millis")));
        c.put(UnitsTime.MINUTE, Mul(Rat(Int(1), Mul(Int(6), Pow(10, 4))), Sym("millis")));
        c.put(UnitsTime.NANOSECOND, Mul(Pow(10, 6), Sym("millis")));
        c.put(UnitsTime.PETASECOND, Mul(Rat(Int(1), Pow(10, 18)), Sym("millis")));
        c.put(UnitsTime.PICOSECOND, Mul(Pow(10, 9), Sym("millis")));
        c.put(UnitsTime.SECOND, Mul(Rat(Int(1), Int(1000)), Sym("millis")));
        c.put(UnitsTime.TERASECOND, Mul(Rat(Int(1), Pow(10, 15)), Sym("millis")));
        c.put(UnitsTime.YOCTOSECOND, Mul(Pow(10, 21), Sym("millis")));
        c.put(UnitsTime.YOTTASECOND, Mul(Rat(Int(1), Pow(10, 27)), Sym("millis")));
        c.put(UnitsTime.ZEPTOSECOND, Mul(Pow(10, 18), Sym("millis")));
        c.put(UnitsTime.ZETTASECOND, Mul(Rat(Int(1), Pow(10, 24)), Sym("millis")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsTime, Conversion> createMapMINUTE() {
        EnumMap<UnitsTime, Conversion> c =
            new EnumMap<UnitsTime, Conversion>(UnitsTime.class);
        c.put(UnitsTime.ATTOSECOND, Mul(Mul(Int(6), Pow(10, 19)), Sym("m")));
        c.put(UnitsTime.CENTISECOND, Mul(Int(6000), Sym("m")));
        c.put(UnitsTime.DAY, Mul(Rat(Int(1), Int(1440)), Sym("m")));
        c.put(UnitsTime.DECASECOND, Mul(Int(6), Sym("m")));
        c.put(UnitsTime.DECISECOND, Mul(Int(600), Sym("m")));
        c.put(UnitsTime.EXASECOND, Mul(Rat(Int(3), Mul(Int(5), Pow(10, 16))), Sym("m")));
        c.put(UnitsTime.FEMTOSECOND, Mul(Mul(Int(6), Pow(10, 16)), Sym("m")));
        c.put(UnitsTime.GIGASECOND, Mul(Rat(Int(3), Mul(Int(5), Pow(10, 7))), Sym("m")));
        c.put(UnitsTime.HECTOSECOND, Mul(Rat(Int(3), Int(5)), Sym("m")));
        c.put(UnitsTime.HOUR, Mul(Rat(Int(1), Int(60)), Sym("m")));
        c.put(UnitsTime.KILOSECOND, Mul(Rat(Int(3), Int(50)), Sym("m")));
        c.put(UnitsTime.MEGASECOND, Mul(Rat(Int(3), Mul(Int(5), Pow(10, 4))), Sym("m")));
        c.put(UnitsTime.MICROSECOND, Mul(Mul(Int(6), Pow(10, 7)), Sym("m")));
        c.put(UnitsTime.MILLISECOND, Mul(Mul(Int(6), Pow(10, 4)), Sym("m")));
        c.put(UnitsTime.NANOSECOND, Mul(Mul(Int(6), Pow(10, 10)), Sym("m")));
        c.put(UnitsTime.PETASECOND, Mul(Rat(Int(3), Mul(Int(5), Pow(10, 13))), Sym("m")));
        c.put(UnitsTime.PICOSECOND, Mul(Mul(Int(6), Pow(10, 13)), Sym("m")));
        c.put(UnitsTime.SECOND, Mul(Int(60), Sym("m")));
        c.put(UnitsTime.TERASECOND, Mul(Rat(Int(3), Mul(Int(5), Pow(10, 10))), Sym("m")));
        c.put(UnitsTime.YOCTOSECOND, Mul(Mul(Int(6), Pow(10, 25)), Sym("m")));
        c.put(UnitsTime.YOTTASECOND, Mul(Rat(Int(3), Mul(Int(5), Pow(10, 22))), Sym("m")));
        c.put(UnitsTime.ZEPTOSECOND, Mul(Mul(Int(6), Pow(10, 22)), Sym("m")));
        c.put(UnitsTime.ZETTASECOND, Mul(Rat(Int(3), Mul(Int(5), Pow(10, 19))), Sym("m")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsTime, Conversion> createMapNANOSECOND() {
        EnumMap<UnitsTime, Conversion> c =
            new EnumMap<UnitsTime, Conversion>(UnitsTime.class);
        c.put(UnitsTime.ATTOSECOND, Mul(Pow(10, 9), Sym("nanos")));
        c.put(UnitsTime.CENTISECOND, Mul(Rat(Int(1), Pow(10, 7)), Sym("nanos")));
        c.put(UnitsTime.DAY, Mul(Rat(Int(1), Mul(Int(864), Pow(10, 11))), Sym("nanos")));
        c.put(UnitsTime.DECASECOND, Mul(Rat(Int(1), Pow(10, 10)), Sym("nanos")));
        c.put(UnitsTime.DECISECOND, Mul(Rat(Int(1), Pow(10, 8)), Sym("nanos")));
        c.put(UnitsTime.EXASECOND, Mul(Rat(Int(1), Pow(10, 27)), Sym("nanos")));
        c.put(UnitsTime.FEMTOSECOND, Mul(Pow(10, 6), Sym("nanos")));
        c.put(UnitsTime.GIGASECOND, Mul(Rat(Int(1), Pow(10, 18)), Sym("nanos")));
        c.put(UnitsTime.HECTOSECOND, Mul(Rat(Int(1), Pow(10, 11)), Sym("nanos")));
        c.put(UnitsTime.HOUR, Mul(Rat(Int(1), Mul(Int(36), Pow(10, 11))), Sym("nanos")));
        c.put(UnitsTime.KILOSECOND, Mul(Rat(Int(1), Pow(10, 12)), Sym("nanos")));
        c.put(UnitsTime.MEGASECOND, Mul(Rat(Int(1), Pow(10, 15)), Sym("nanos")));
        c.put(UnitsTime.MICROSECOND, Mul(Rat(Int(1), Int(1000)), Sym("nanos")));
        c.put(UnitsTime.MILLISECOND, Mul(Rat(Int(1), Pow(10, 6)), Sym("nanos")));
        c.put(UnitsTime.MINUTE, Mul(Rat(Int(1), Mul(Int(6), Pow(10, 10))), Sym("nanos")));
        c.put(UnitsTime.PETASECOND, Mul(Rat(Int(1), Pow(10, 24)), Sym("nanos")));
        c.put(UnitsTime.PICOSECOND, Mul(Int(1000), Sym("nanos")));
        c.put(UnitsTime.SECOND, Mul(Rat(Int(1), Pow(10, 9)), Sym("nanos")));
        c.put(UnitsTime.TERASECOND, Mul(Rat(Int(1), Pow(10, 21)), Sym("nanos")));
        c.put(UnitsTime.YOCTOSECOND, Mul(Pow(10, 15), Sym("nanos")));
        c.put(UnitsTime.YOTTASECOND, Mul(Rat(Int(1), Pow(10, 33)), Sym("nanos")));
        c.put(UnitsTime.ZEPTOSECOND, Mul(Pow(10, 12), Sym("nanos")));
        c.put(UnitsTime.ZETTASECOND, Mul(Rat(Int(1), Pow(10, 30)), Sym("nanos")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsTime, Conversion> createMapPETASECOND() {
        EnumMap<UnitsTime, Conversion> c =
            new EnumMap<UnitsTime, Conversion>(UnitsTime.class);
        c.put(UnitsTime.ATTOSECOND, Mul(Pow(10, 33), Sym("petas")));
        c.put(UnitsTime.CENTISECOND, Mul(Pow(10, 17), Sym("petas")));
        c.put(UnitsTime.DAY, Mul(Rat(Mul(Int(3125), Pow(10, 8)), Int(27)), Sym("petas")));
        c.put(UnitsTime.DECASECOND, Mul(Pow(10, 14), Sym("petas")));
        c.put(UnitsTime.DECISECOND, Mul(Pow(10, 16), Sym("petas")));
        c.put(UnitsTime.EXASECOND, Mul(Rat(Int(1), Int(1000)), Sym("petas")));
        c.put(UnitsTime.FEMTOSECOND, Mul(Pow(10, 30), Sym("petas")));
        c.put(UnitsTime.GIGASECOND, Mul(Pow(10, 6), Sym("petas")));
        c.put(UnitsTime.HECTOSECOND, Mul(Pow(10, 13), Sym("petas")));
        c.put(UnitsTime.HOUR, Mul(Rat(Mul(Int(25), Pow(10, 11)), Int(9)), Sym("petas")));
        c.put(UnitsTime.KILOSECOND, Mul(Pow(10, 12), Sym("petas")));
        c.put(UnitsTime.MEGASECOND, Mul(Pow(10, 9), Sym("petas")));
        c.put(UnitsTime.MICROSECOND, Mul(Pow(10, 21), Sym("petas")));
        c.put(UnitsTime.MILLISECOND, Mul(Pow(10, 18), Sym("petas")));
        c.put(UnitsTime.MINUTE, Mul(Rat(Mul(Int(5), Pow(10, 13)), Int(3)), Sym("petas")));
        c.put(UnitsTime.NANOSECOND, Mul(Pow(10, 24), Sym("petas")));
        c.put(UnitsTime.PICOSECOND, Mul(Pow(10, 27), Sym("petas")));
        c.put(UnitsTime.SECOND, Mul(Pow(10, 15), Sym("petas")));
        c.put(UnitsTime.TERASECOND, Mul(Int(1000), Sym("petas")));
        c.put(UnitsTime.YOCTOSECOND, Mul(Pow(10, 39), Sym("petas")));
        c.put(UnitsTime.YOTTASECOND, Mul(Rat(Int(1), Pow(10, 9)), Sym("petas")));
        c.put(UnitsTime.ZEPTOSECOND, Mul(Pow(10, 36), Sym("petas")));
        c.put(UnitsTime.ZETTASECOND, Mul(Rat(Int(1), Pow(10, 6)), Sym("petas")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsTime, Conversion> createMapPICOSECOND() {
        EnumMap<UnitsTime, Conversion> c =
            new EnumMap<UnitsTime, Conversion>(UnitsTime.class);
        c.put(UnitsTime.ATTOSECOND, Mul(Pow(10, 6), Sym("picos")));
        c.put(UnitsTime.CENTISECOND, Mul(Rat(Int(1), Pow(10, 10)), Sym("picos")));
        c.put(UnitsTime.DAY, Mul(Rat(Int(1), Mul(Int(864), Pow(10, 14))), Sym("picos")));
        c.put(UnitsTime.DECASECOND, Mul(Rat(Int(1), Pow(10, 13)), Sym("picos")));
        c.put(UnitsTime.DECISECOND, Mul(Rat(Int(1), Pow(10, 11)), Sym("picos")));
        c.put(UnitsTime.EXASECOND, Mul(Rat(Int(1), Pow(10, 30)), Sym("picos")));
        c.put(UnitsTime.FEMTOSECOND, Mul(Int(1000), Sym("picos")));
        c.put(UnitsTime.GIGASECOND, Mul(Rat(Int(1), Pow(10, 21)), Sym("picos")));
        c.put(UnitsTime.HECTOSECOND, Mul(Rat(Int(1), Pow(10, 14)), Sym("picos")));
        c.put(UnitsTime.HOUR, Mul(Rat(Int(1), Mul(Int(36), Pow(10, 14))), Sym("picos")));
        c.put(UnitsTime.KILOSECOND, Mul(Rat(Int(1), Pow(10, 15)), Sym("picos")));
        c.put(UnitsTime.MEGASECOND, Mul(Rat(Int(1), Pow(10, 18)), Sym("picos")));
        c.put(UnitsTime.MICROSECOND, Mul(Rat(Int(1), Pow(10, 6)), Sym("picos")));
        c.put(UnitsTime.MILLISECOND, Mul(Rat(Int(1), Pow(10, 9)), Sym("picos")));
        c.put(UnitsTime.MINUTE, Mul(Rat(Int(1), Mul(Int(6), Pow(10, 13))), Sym("picos")));
        c.put(UnitsTime.NANOSECOND, Mul(Rat(Int(1), Int(1000)), Sym("picos")));
        c.put(UnitsTime.PETASECOND, Mul(Rat(Int(1), Pow(10, 27)), Sym("picos")));
        c.put(UnitsTime.SECOND, Mul(Rat(Int(1), Pow(10, 12)), Sym("picos")));
        c.put(UnitsTime.TERASECOND, Mul(Rat(Int(1), Pow(10, 24)), Sym("picos")));
        c.put(UnitsTime.YOCTOSECOND, Mul(Pow(10, 12), Sym("picos")));
        c.put(UnitsTime.YOTTASECOND, Mul(Rat(Int(1), Pow(10, 36)), Sym("picos")));
        c.put(UnitsTime.ZEPTOSECOND, Mul(Pow(10, 9), Sym("picos")));
        c.put(UnitsTime.ZETTASECOND, Mul(Rat(Int(1), Pow(10, 33)), Sym("picos")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsTime, Conversion> createMapSECOND() {
        EnumMap<UnitsTime, Conversion> c =
            new EnumMap<UnitsTime, Conversion>(UnitsTime.class);
        c.put(UnitsTime.ATTOSECOND, Mul(Pow(10, 18), Sym("s")));
        c.put(UnitsTime.CENTISECOND, Mul(Int(100), Sym("s")));
        c.put(UnitsTime.DAY, Mul(Rat(Int(1), Int(86400)), Sym("s")));
        c.put(UnitsTime.DECASECOND, Mul(Rat(Int(1), Int(10)), Sym("s")));
        c.put(UnitsTime.DECISECOND, Mul(Int(10), Sym("s")));
        c.put(UnitsTime.EXASECOND, Mul(Rat(Int(1), Pow(10, 18)), Sym("s")));
        c.put(UnitsTime.FEMTOSECOND, Mul(Pow(10, 15), Sym("s")));
        c.put(UnitsTime.GIGASECOND, Mul(Rat(Int(1), Pow(10, 9)), Sym("s")));
        c.put(UnitsTime.HECTOSECOND, Mul(Rat(Int(1), Int(100)), Sym("s")));
        c.put(UnitsTime.HOUR, Mul(Rat(Int(1), Int(3600)), Sym("s")));
        c.put(UnitsTime.KILOSECOND, Mul(Rat(Int(1), Int(1000)), Sym("s")));
        c.put(UnitsTime.MEGASECOND, Mul(Rat(Int(1), Pow(10, 6)), Sym("s")));
        c.put(UnitsTime.MICROSECOND, Mul(Pow(10, 6), Sym("s")));
        c.put(UnitsTime.MILLISECOND, Mul(Int(1000), Sym("s")));
        c.put(UnitsTime.MINUTE, Mul(Rat(Int(1), Int(60)), Sym("s")));
        c.put(UnitsTime.NANOSECOND, Mul(Pow(10, 9), Sym("s")));
        c.put(UnitsTime.PETASECOND, Mul(Rat(Int(1), Pow(10, 15)), Sym("s")));
        c.put(UnitsTime.PICOSECOND, Mul(Pow(10, 12), Sym("s")));
        c.put(UnitsTime.TERASECOND, Mul(Rat(Int(1), Pow(10, 12)), Sym("s")));
        c.put(UnitsTime.YOCTOSECOND, Mul(Pow(10, 24), Sym("s")));
        c.put(UnitsTime.YOTTASECOND, Mul(Rat(Int(1), Pow(10, 24)), Sym("s")));
        c.put(UnitsTime.ZEPTOSECOND, Mul(Pow(10, 21), Sym("s")));
        c.put(UnitsTime.ZETTASECOND, Mul(Rat(Int(1), Pow(10, 21)), Sym("s")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsTime, Conversion> createMapTERASECOND() {
        EnumMap<UnitsTime, Conversion> c =
            new EnumMap<UnitsTime, Conversion>(UnitsTime.class);
        c.put(UnitsTime.ATTOSECOND, Mul(Pow(10, 30), Sym("teras")));
        c.put(UnitsTime.CENTISECOND, Mul(Pow(10, 14), Sym("teras")));
        c.put(UnitsTime.DAY, Mul(Rat(Mul(Int(3125), Pow(10, 5)), Int(27)), Sym("teras")));
        c.put(UnitsTime.DECASECOND, Mul(Pow(10, 11), Sym("teras")));
        c.put(UnitsTime.DECISECOND, Mul(Pow(10, 13), Sym("teras")));
        c.put(UnitsTime.EXASECOND, Mul(Rat(Int(1), Pow(10, 6)), Sym("teras")));
        c.put(UnitsTime.FEMTOSECOND, Mul(Pow(10, 27), Sym("teras")));
        c.put(UnitsTime.GIGASECOND, Mul(Int(1000), Sym("teras")));
        c.put(UnitsTime.HECTOSECOND, Mul(Pow(10, 10), Sym("teras")));
        c.put(UnitsTime.HOUR, Mul(Rat(Mul(Int(25), Pow(10, 8)), Int(9)), Sym("teras")));
        c.put(UnitsTime.KILOSECOND, Mul(Pow(10, 9), Sym("teras")));
        c.put(UnitsTime.MEGASECOND, Mul(Pow(10, 6), Sym("teras")));
        c.put(UnitsTime.MICROSECOND, Mul(Pow(10, 18), Sym("teras")));
        c.put(UnitsTime.MILLISECOND, Mul(Pow(10, 15), Sym("teras")));
        c.put(UnitsTime.MINUTE, Mul(Rat(Mul(Int(5), Pow(10, 10)), Int(3)), Sym("teras")));
        c.put(UnitsTime.NANOSECOND, Mul(Pow(10, 21), Sym("teras")));
        c.put(UnitsTime.PETASECOND, Mul(Rat(Int(1), Int(1000)), Sym("teras")));
        c.put(UnitsTime.PICOSECOND, Mul(Pow(10, 24), Sym("teras")));
        c.put(UnitsTime.SECOND, Mul(Pow(10, 12), Sym("teras")));
        c.put(UnitsTime.YOCTOSECOND, Mul(Pow(10, 36), Sym("teras")));
        c.put(UnitsTime.YOTTASECOND, Mul(Rat(Int(1), Pow(10, 12)), Sym("teras")));
        c.put(UnitsTime.ZEPTOSECOND, Mul(Pow(10, 33), Sym("teras")));
        c.put(UnitsTime.ZETTASECOND, Mul(Rat(Int(1), Pow(10, 9)), Sym("teras")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsTime, Conversion> createMapYOCTOSECOND() {
        EnumMap<UnitsTime, Conversion> c =
            new EnumMap<UnitsTime, Conversion>(UnitsTime.class);
        c.put(UnitsTime.ATTOSECOND, Mul(Rat(Int(1), Pow(10, 6)), Sym("yoctos")));
        c.put(UnitsTime.CENTISECOND, Mul(Rat(Int(1), Pow(10, 22)), Sym("yoctos")));
        c.put(UnitsTime.DAY, Mul(Rat(Int(1), Mul(Int(864), Pow(10, 26))), Sym("yoctos")));
        c.put(UnitsTime.DECASECOND, Mul(Rat(Int(1), Pow(10, 25)), Sym("yoctos")));
        c.put(UnitsTime.DECISECOND, Mul(Rat(Int(1), Pow(10, 23)), Sym("yoctos")));
        c.put(UnitsTime.EXASECOND, Mul(Rat(Int(1), Pow(10, 42)), Sym("yoctos")));
        c.put(UnitsTime.FEMTOSECOND, Mul(Rat(Int(1), Pow(10, 9)), Sym("yoctos")));
        c.put(UnitsTime.GIGASECOND, Mul(Rat(Int(1), Pow(10, 33)), Sym("yoctos")));
        c.put(UnitsTime.HECTOSECOND, Mul(Rat(Int(1), Pow(10, 26)), Sym("yoctos")));
        c.put(UnitsTime.HOUR, Mul(Rat(Int(1), Mul(Int(36), Pow(10, 26))), Sym("yoctos")));
        c.put(UnitsTime.KILOSECOND, Mul(Rat(Int(1), Pow(10, 27)), Sym("yoctos")));
        c.put(UnitsTime.MEGASECOND, Mul(Rat(Int(1), Pow(10, 30)), Sym("yoctos")));
        c.put(UnitsTime.MICROSECOND, Mul(Rat(Int(1), Pow(10, 18)), Sym("yoctos")));
        c.put(UnitsTime.MILLISECOND, Mul(Rat(Int(1), Pow(10, 21)), Sym("yoctos")));
        c.put(UnitsTime.MINUTE, Mul(Rat(Int(1), Mul(Int(6), Pow(10, 25))), Sym("yoctos")));
        c.put(UnitsTime.NANOSECOND, Mul(Rat(Int(1), Pow(10, 15)), Sym("yoctos")));
        c.put(UnitsTime.PETASECOND, Mul(Rat(Int(1), Pow(10, 39)), Sym("yoctos")));
        c.put(UnitsTime.PICOSECOND, Mul(Rat(Int(1), Pow(10, 12)), Sym("yoctos")));
        c.put(UnitsTime.SECOND, Mul(Rat(Int(1), Pow(10, 24)), Sym("yoctos")));
        c.put(UnitsTime.TERASECOND, Mul(Rat(Int(1), Pow(10, 36)), Sym("yoctos")));
        c.put(UnitsTime.YOTTASECOND, Mul(Rat(Int(1), Pow(10, 48)), Sym("yoctos")));
        c.put(UnitsTime.ZEPTOSECOND, Mul(Rat(Int(1), Int(1000)), Sym("yoctos")));
        c.put(UnitsTime.ZETTASECOND, Mul(Rat(Int(1), Pow(10, 45)), Sym("yoctos")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsTime, Conversion> createMapYOTTASECOND() {
        EnumMap<UnitsTime, Conversion> c =
            new EnumMap<UnitsTime, Conversion>(UnitsTime.class);
        c.put(UnitsTime.ATTOSECOND, Mul(Pow(10, 42), Sym("yottas")));
        c.put(UnitsTime.CENTISECOND, Mul(Pow(10, 26), Sym("yottas")));
        c.put(UnitsTime.DAY, Mul(Rat(Mul(Int(3125), Pow(10, 17)), Int(27)), Sym("yottas")));
        c.put(UnitsTime.DECASECOND, Mul(Pow(10, 23), Sym("yottas")));
        c.put(UnitsTime.DECISECOND, Mul(Pow(10, 25), Sym("yottas")));
        c.put(UnitsTime.EXASECOND, Mul(Pow(10, 6), Sym("yottas")));
        c.put(UnitsTime.FEMTOSECOND, Mul(Pow(10, 39), Sym("yottas")));
        c.put(UnitsTime.GIGASECOND, Mul(Pow(10, 15), Sym("yottas")));
        c.put(UnitsTime.HECTOSECOND, Mul(Pow(10, 22), Sym("yottas")));
        c.put(UnitsTime.HOUR, Mul(Rat(Mul(Int(25), Pow(10, 20)), Int(9)), Sym("yottas")));
        c.put(UnitsTime.KILOSECOND, Mul(Pow(10, 21), Sym("yottas")));
        c.put(UnitsTime.MEGASECOND, Mul(Pow(10, 18), Sym("yottas")));
        c.put(UnitsTime.MICROSECOND, Mul(Pow(10, 30), Sym("yottas")));
        c.put(UnitsTime.MILLISECOND, Mul(Pow(10, 27), Sym("yottas")));
        c.put(UnitsTime.MINUTE, Mul(Rat(Mul(Int(5), Pow(10, 22)), Int(3)), Sym("yottas")));
        c.put(UnitsTime.NANOSECOND, Mul(Pow(10, 33), Sym("yottas")));
        c.put(UnitsTime.PETASECOND, Mul(Pow(10, 9), Sym("yottas")));
        c.put(UnitsTime.PICOSECOND, Mul(Pow(10, 36), Sym("yottas")));
        c.put(UnitsTime.SECOND, Mul(Pow(10, 24), Sym("yottas")));
        c.put(UnitsTime.TERASECOND, Mul(Pow(10, 12), Sym("yottas")));
        c.put(UnitsTime.YOCTOSECOND, Mul(Pow(10, 48), Sym("yottas")));
        c.put(UnitsTime.ZEPTOSECOND, Mul(Pow(10, 45), Sym("yottas")));
        c.put(UnitsTime.ZETTASECOND, Mul(Int(1000), Sym("yottas")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsTime, Conversion> createMapZEPTOSECOND() {
        EnumMap<UnitsTime, Conversion> c =
            new EnumMap<UnitsTime, Conversion>(UnitsTime.class);
        c.put(UnitsTime.ATTOSECOND, Mul(Rat(Int(1), Int(1000)), Sym("zeptos")));
        c.put(UnitsTime.CENTISECOND, Mul(Rat(Int(1), Pow(10, 19)), Sym("zeptos")));
        c.put(UnitsTime.DAY, Mul(Rat(Int(1), Mul(Int(864), Pow(10, 23))), Sym("zeptos")));
        c.put(UnitsTime.DECASECOND, Mul(Rat(Int(1), Pow(10, 22)), Sym("zeptos")));
        c.put(UnitsTime.DECISECOND, Mul(Rat(Int(1), Pow(10, 20)), Sym("zeptos")));
        c.put(UnitsTime.EXASECOND, Mul(Rat(Int(1), Pow(10, 39)), Sym("zeptos")));
        c.put(UnitsTime.FEMTOSECOND, Mul(Rat(Int(1), Pow(10, 6)), Sym("zeptos")));
        c.put(UnitsTime.GIGASECOND, Mul(Rat(Int(1), Pow(10, 30)), Sym("zeptos")));
        c.put(UnitsTime.HECTOSECOND, Mul(Rat(Int(1), Pow(10, 23)), Sym("zeptos")));
        c.put(UnitsTime.HOUR, Mul(Rat(Int(1), Mul(Int(36), Pow(10, 23))), Sym("zeptos")));
        c.put(UnitsTime.KILOSECOND, Mul(Rat(Int(1), Pow(10, 24)), Sym("zeptos")));
        c.put(UnitsTime.MEGASECOND, Mul(Rat(Int(1), Pow(10, 27)), Sym("zeptos")));
        c.put(UnitsTime.MICROSECOND, Mul(Rat(Int(1), Pow(10, 15)), Sym("zeptos")));
        c.put(UnitsTime.MILLISECOND, Mul(Rat(Int(1), Pow(10, 18)), Sym("zeptos")));
        c.put(UnitsTime.MINUTE, Mul(Rat(Int(1), Mul(Int(6), Pow(10, 22))), Sym("zeptos")));
        c.put(UnitsTime.NANOSECOND, Mul(Rat(Int(1), Pow(10, 12)), Sym("zeptos")));
        c.put(UnitsTime.PETASECOND, Mul(Rat(Int(1), Pow(10, 36)), Sym("zeptos")));
        c.put(UnitsTime.PICOSECOND, Mul(Rat(Int(1), Pow(10, 9)), Sym("zeptos")));
        c.put(UnitsTime.SECOND, Mul(Rat(Int(1), Pow(10, 21)), Sym("zeptos")));
        c.put(UnitsTime.TERASECOND, Mul(Rat(Int(1), Pow(10, 33)), Sym("zeptos")));
        c.put(UnitsTime.YOCTOSECOND, Mul(Int(1000), Sym("zeptos")));
        c.put(UnitsTime.YOTTASECOND, Mul(Rat(Int(1), Pow(10, 45)), Sym("zeptos")));
        c.put(UnitsTime.ZETTASECOND, Mul(Rat(Int(1), Pow(10, 42)), Sym("zeptos")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsTime, Conversion> createMapZETTASECOND() {
        EnumMap<UnitsTime, Conversion> c =
            new EnumMap<UnitsTime, Conversion>(UnitsTime.class);
        c.put(UnitsTime.ATTOSECOND, Mul(Pow(10, 39), Sym("zettas")));
        c.put(UnitsTime.CENTISECOND, Mul(Pow(10, 23), Sym("zettas")));
        c.put(UnitsTime.DAY, Mul(Rat(Mul(Int(3125), Pow(10, 14)), Int(27)), Sym("zettas")));
        c.put(UnitsTime.DECASECOND, Mul(Pow(10, 20), Sym("zettas")));
        c.put(UnitsTime.DECISECOND, Mul(Pow(10, 22), Sym("zettas")));
        c.put(UnitsTime.EXASECOND, Mul(Int(1000), Sym("zettas")));
        c.put(UnitsTime.FEMTOSECOND, Mul(Pow(10, 36), Sym("zettas")));
        c.put(UnitsTime.GIGASECOND, Mul(Pow(10, 12), Sym("zettas")));
        c.put(UnitsTime.HECTOSECOND, Mul(Pow(10, 19), Sym("zettas")));
        c.put(UnitsTime.HOUR, Mul(Rat(Mul(Int(25), Pow(10, 17)), Int(9)), Sym("zettas")));
        c.put(UnitsTime.KILOSECOND, Mul(Pow(10, 18), Sym("zettas")));
        c.put(UnitsTime.MEGASECOND, Mul(Pow(10, 15), Sym("zettas")));
        c.put(UnitsTime.MICROSECOND, Mul(Pow(10, 27), Sym("zettas")));
        c.put(UnitsTime.MILLISECOND, Mul(Pow(10, 24), Sym("zettas")));
        c.put(UnitsTime.MINUTE, Mul(Rat(Mul(Int(5), Pow(10, 19)), Int(3)), Sym("zettas")));
        c.put(UnitsTime.NANOSECOND, Mul(Pow(10, 30), Sym("zettas")));
        c.put(UnitsTime.PETASECOND, Mul(Pow(10, 6), Sym("zettas")));
        c.put(UnitsTime.PICOSECOND, Mul(Pow(10, 33), Sym("zettas")));
        c.put(UnitsTime.SECOND, Mul(Pow(10, 21), Sym("zettas")));
        c.put(UnitsTime.TERASECOND, Mul(Pow(10, 9), Sym("zettas")));
        c.put(UnitsTime.YOCTOSECOND, Mul(Pow(10, 45), Sym("zettas")));
        c.put(UnitsTime.YOTTASECOND, Mul(Rat(Int(1), Int(1000)), Sym("zettas")));
        c.put(UnitsTime.ZEPTOSECOND, Mul(Pow(10, 42), Sym("zettas")));
        return Collections.unmodifiableMap(c);
    }

    private static final Map<UnitsTime, Map<UnitsTime, Conversion>> conversions;
    static {

        Map<UnitsTime, Map<UnitsTime, Conversion>> c
            = new EnumMap<UnitsTime, Map<UnitsTime, Conversion>>(UnitsTime.class);

        c.put(UnitsTime.ATTOSECOND, createMapATTOSECOND());
        c.put(UnitsTime.CENTISECOND, createMapCENTISECOND());
        c.put(UnitsTime.DAY, createMapDAY());
        c.put(UnitsTime.DECASECOND, createMapDECASECOND());
        c.put(UnitsTime.DECISECOND, createMapDECISECOND());
        c.put(UnitsTime.EXASECOND, createMapEXASECOND());
        c.put(UnitsTime.FEMTOSECOND, createMapFEMTOSECOND());
        c.put(UnitsTime.GIGASECOND, createMapGIGASECOND());
        c.put(UnitsTime.HECTOSECOND, createMapHECTOSECOND());
        c.put(UnitsTime.HOUR, createMapHOUR());
        c.put(UnitsTime.KILOSECOND, createMapKILOSECOND());
        c.put(UnitsTime.MEGASECOND, createMapMEGASECOND());
        c.put(UnitsTime.MICROSECOND, createMapMICROSECOND());
        c.put(UnitsTime.MILLISECOND, createMapMILLISECOND());
        c.put(UnitsTime.MINUTE, createMapMINUTE());
        c.put(UnitsTime.NANOSECOND, createMapNANOSECOND());
        c.put(UnitsTime.PETASECOND, createMapPETASECOND());
        c.put(UnitsTime.PICOSECOND, createMapPICOSECOND());
        c.put(UnitsTime.SECOND, createMapSECOND());
        c.put(UnitsTime.TERASECOND, createMapTERASECOND());
        c.put(UnitsTime.YOCTOSECOND, createMapYOCTOSECOND());
        c.put(UnitsTime.YOTTASECOND, createMapYOTTASECOND());
        c.put(UnitsTime.ZEPTOSECOND, createMapZEPTOSECOND());
        c.put(UnitsTime.ZETTASECOND, createMapZETTASECOND());
        conversions = Collections.unmodifiableMap(c);
    }

    private static final Map<UnitsTime, String> SYMBOLS;
    static {
        Map<UnitsTime, String> s = new HashMap<UnitsTime, String>();
        s.put(UnitsTime.ATTOSECOND, "as");
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

