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

import omero.model.enums.UnitsLength;

/**
 * Blitz wrapper around the {@link ome.model.units.Length} class.
 * Like {@link Details} and {@link Permissions}, this object
 * is embedded into other objects and does not have a full life
 * cycle of its own.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 */
public class LengthI extends Length implements ModelBased {

    private static final long serialVersionUID = 1L;

    private static Map<UnitsLength, Conversion> createMapANGSTROM() {
        EnumMap<UnitsLength, Conversion> c =
            new EnumMap<UnitsLength, Conversion>(UnitsLength.class);
        c.put(UnitsLength.ASTRONOMICALUNIT, Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 12))), Sym("ang")));
        c.put(UnitsLength.ATTOMETER, Mul(Pow(10, 8), Sym("ang")));
        c.put(UnitsLength.CENTIMETER, Mul(Rat(Int(1), Pow(10, 8)), Sym("ang")));
        c.put(UnitsLength.DECAMETER, Mul(Rat(Int(1), Pow(10, 11)), Sym("ang")));
        c.put(UnitsLength.DECIMETER, Mul(Rat(Int(1), Pow(10, 9)), Sym("ang")));
        c.put(UnitsLength.EXAMETER, Mul(Rat(Int(1), Pow(10, 28)), Sym("ang")));
        c.put(UnitsLength.FEMTOMETER, Mul(Pow(10, 5), Sym("ang")));
        c.put(UnitsLength.FOOT, Mul(Rat(Int(393701), Mul(Int(12), Pow(10, 14))), Sym("ang")));
        c.put(UnitsLength.GIGAMETER, Mul(Rat(Int(1), Pow(10, 19)), Sym("ang")));
        c.put(UnitsLength.HECTOMETER, Mul(Rat(Int(1), Pow(10, 12)), Sym("ang")));
        c.put(UnitsLength.INCH, Mul(Rat(Int(393701), Pow(10, 14)), Sym("ang")));
        c.put(UnitsLength.KILOMETER, Mul(Rat(Int(1), Pow(10, 13)), Sym("ang")));
        c.put(UnitsLength.LIGHTYEAR, Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 12))), Sym("ang")));
        c.put(UnitsLength.LINE, Mul(Rat(Int(1181103), Mul(Int(25), Pow(10, 12))), Sym("ang")));
        c.put(UnitsLength.MEGAMETER, Mul(Rat(Int(1), Pow(10, 16)), Sym("ang")));
        c.put(UnitsLength.METER, Mul(Rat(Int(1), Pow(10, 10)), Sym("ang")));
        c.put(UnitsLength.MICROMETER, Mul(Rat(Int(1), Pow(10, 4)), Sym("ang")));
        c.put(UnitsLength.MILE, Mul(Rat(Int(35791), Mul(Int(576), Pow(10, 15))), Sym("ang")));
        c.put(UnitsLength.MILLIMETER, Mul(Rat(Int(1), Pow(10, 7)), Sym("ang")));
        c.put(UnitsLength.NANOMETER, Mul(Rat(Int(1), Int(10)), Sym("ang")));
        c.put(UnitsLength.PARSEC, Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 19))), Sym("ang")));
        c.put(UnitsLength.PETAMETER, Mul(Rat(Int(1), Pow(10, 25)), Sym("ang")));
        c.put(UnitsLength.PICOMETER, Mul(Int(100), Sym("ang")));
        c.put(UnitsLength.POINT, Mul(Rat(Int(3543309), Mul(Int(125), Pow(10, 11))), Sym("ang")));
        c.put(UnitsLength.TERAMETER, Mul(Rat(Int(1), Pow(10, 22)), Sym("ang")));
        c.put(UnitsLength.THOU, Mul(Rat(Int(393701), Pow(10, 17)), Sym("ang")));
        c.put(UnitsLength.YARD, Mul(Rat(Int(393701), Mul(Int(36), Pow(10, 14))), Sym("ang")));
        c.put(UnitsLength.YOCTOMETER, Mul(Pow(10, 14), Sym("ang")));
        c.put(UnitsLength.YOTTAMETER, Mul(Rat(Int(1), Pow(10, 34)), Sym("ang")));
        c.put(UnitsLength.ZEPTOMETER, Mul(Pow(10, 11), Sym("ang")));
        c.put(UnitsLength.ZETTAMETER, Mul(Rat(Int(1), Pow(10, 31)), Sym("ang")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsLength, Conversion> createMapASTRONOMICALUNIT() {
        EnumMap<UnitsLength, Conversion> c =
            new EnumMap<UnitsLength, Conversion>(UnitsLength.class);
        c.put(UnitsLength.ANGSTROM, Mul(Mul(Int(1495978707), Pow(10, 12)), Sym("ua")));
        c.put(UnitsLength.ATTOMETER, Mul(Mul(Int(1495978707), Pow(10, 20)), Sym("ua")));
        c.put(UnitsLength.CENTIMETER, Mul(Mul(Int(1495978707), Pow(10, 4)), Sym("ua")));
        c.put(UnitsLength.DECAMETER, Mul(Int("14959787070"), Sym("ua")));
        c.put(UnitsLength.DECIMETER, Mul(Int("1495978707000"), Sym("ua")));
        c.put(UnitsLength.EXAMETER, Mul(Rat(Int(1495978707), Pow(10, 16)), Sym("ua")));
        c.put(UnitsLength.FEMTOMETER, Mul(Mul(Int(1495978707), Pow(10, 17)), Sym("ua")));
        c.put(UnitsLength.FOOT, Mul(Rat(Int("196322770974869"), Int(400)), Sym("ua")));
        c.put(UnitsLength.GIGAMETER, Mul(Rat(Int(1495978707), Pow(10, 7)), Sym("ua")));
        c.put(UnitsLength.HECTOMETER, Mul(Int(1495978707), Sym("ua")));
        c.put(UnitsLength.INCH, Mul(Rat(Int("588968312924607"), Int(100)), Sym("ua")));
        c.put(UnitsLength.KILOMETER, Mul(Rat(Int(1495978707), Int(10)), Sym("ua")));
        c.put(UnitsLength.LIGHTYEAR, Mul(Rat(Int(6830953), Int("431996825232")), Sym("ua")));
        c.put(UnitsLength.LINE, Mul(Rat(Int("1766904938773821"), Int(25)), Sym("ua")));
        c.put(UnitsLength.MEGAMETER, Mul(Rat(Int(1495978707), Pow(10, 4)), Sym("ua")));
        c.put(UnitsLength.METER, Mul(Int("149597870700"), Sym("ua")));
        c.put(UnitsLength.MICROMETER, Mul(Mul(Int(1495978707), Pow(10, 8)), Sym("ua")));
        c.put(UnitsLength.MILE, Mul(Rat(Int("17847524634079"), Int(192000)), Sym("ua")));
        c.put(UnitsLength.MILLIMETER, Mul(Mul(Int(1495978707), Pow(10, 5)), Sym("ua")));
        c.put(UnitsLength.NANOMETER, Mul(Mul(Int(1495978707), Pow(10, 11)), Sym("ua")));
        c.put(UnitsLength.PARSEC, Mul(Rat(Int(498659569), Mul(Int(10285592), Pow(10, 7))), Sym("ua")));
        c.put(UnitsLength.PETAMETER, Mul(Rat(Int(1495978707), Pow(10, 13)), Sym("ua")));
        c.put(UnitsLength.PICOMETER, Mul(Mul(Int(1495978707), Pow(10, 14)), Sym("ua")));
        c.put(UnitsLength.POINT, Mul(Rat(Int("10601429632642926"), Int(25)), Sym("ua")));
        c.put(UnitsLength.TERAMETER, Mul(Rat(Int(1495978707), Pow(10, 10)), Sym("ua")));
        c.put(UnitsLength.THOU, Mul(Rat(Int("588968312924607"), Pow(10, 5)), Sym("ua")));
        c.put(UnitsLength.YARD, Mul(Rat(Int("196322770974869"), Int(1200)), Sym("ua")));
        c.put(UnitsLength.YOCTOMETER, Mul(Mul(Int(1495978707), Pow(10, 26)), Sym("ua")));
        c.put(UnitsLength.YOTTAMETER, Mul(Rat(Int(1495978707), Pow(10, 22)), Sym("ua")));
        c.put(UnitsLength.ZEPTOMETER, Mul(Mul(Int(1495978707), Pow(10, 23)), Sym("ua")));
        c.put(UnitsLength.ZETTAMETER, Mul(Rat(Int(1495978707), Pow(10, 19)), Sym("ua")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsLength, Conversion> createMapATTOMETER() {
        EnumMap<UnitsLength, Conversion> c =
            new EnumMap<UnitsLength, Conversion>(UnitsLength.class);
        c.put(UnitsLength.ANGSTROM, Mul(Rat(Int(1), Pow(10, 8)), Sym("attom")));
        c.put(UnitsLength.ASTRONOMICALUNIT, Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 20))), Sym("attom")));
        c.put(UnitsLength.CENTIMETER, Mul(Rat(Int(1), Pow(10, 16)), Sym("attom")));
        c.put(UnitsLength.DECAMETER, Mul(Rat(Int(1), Pow(10, 19)), Sym("attom")));
        c.put(UnitsLength.DECIMETER, Mul(Rat(Int(1), Pow(10, 17)), Sym("attom")));
        c.put(UnitsLength.EXAMETER, Mul(Rat(Int(1), Pow(10, 36)), Sym("attom")));
        c.put(UnitsLength.FEMTOMETER, Mul(Rat(Int(1), Int(1000)), Sym("attom")));
        c.put(UnitsLength.FOOT, Mul(Rat(Int(393701), Mul(Int(12), Pow(10, 22))), Sym("attom")));
        c.put(UnitsLength.GIGAMETER, Mul(Rat(Int(1), Pow(10, 27)), Sym("attom")));
        c.put(UnitsLength.HECTOMETER, Mul(Rat(Int(1), Pow(10, 20)), Sym("attom")));
        c.put(UnitsLength.INCH, Mul(Rat(Int(393701), Pow(10, 22)), Sym("attom")));
        c.put(UnitsLength.KILOMETER, Mul(Rat(Int(1), Pow(10, 21)), Sym("attom")));
        c.put(UnitsLength.LIGHTYEAR, Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 20))), Sym("attom")));
        c.put(UnitsLength.LINE, Mul(Rat(Int(1181103), Mul(Int(25), Pow(10, 20))), Sym("attom")));
        c.put(UnitsLength.MEGAMETER, Mul(Rat(Int(1), Pow(10, 24)), Sym("attom")));
        c.put(UnitsLength.METER, Mul(Rat(Int(1), Pow(10, 18)), Sym("attom")));
        c.put(UnitsLength.MICROMETER, Mul(Rat(Int(1), Pow(10, 12)), Sym("attom")));
        c.put(UnitsLength.MILE, Mul(Rat(Int(35791), Mul(Int(576), Pow(10, 23))), Sym("attom")));
        c.put(UnitsLength.MILLIMETER, Mul(Rat(Int(1), Pow(10, 15)), Sym("attom")));
        c.put(UnitsLength.NANOMETER, Mul(Rat(Int(1), Pow(10, 9)), Sym("attom")));
        c.put(UnitsLength.PARSEC, Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 27))), Sym("attom")));
        c.put(UnitsLength.PETAMETER, Mul(Rat(Int(1), Pow(10, 33)), Sym("attom")));
        c.put(UnitsLength.PICOMETER, Mul(Rat(Int(1), Pow(10, 6)), Sym("attom")));
        c.put(UnitsLength.POINT, Mul(Rat(Int(3543309), Mul(Int(125), Pow(10, 19))), Sym("attom")));
        c.put(UnitsLength.TERAMETER, Mul(Rat(Int(1), Pow(10, 30)), Sym("attom")));
        c.put(UnitsLength.THOU, Mul(Rat(Int(393701), Pow(10, 25)), Sym("attom")));
        c.put(UnitsLength.YARD, Mul(Rat(Int(393701), Mul(Int(36), Pow(10, 22))), Sym("attom")));
        c.put(UnitsLength.YOCTOMETER, Mul(Pow(10, 6), Sym("attom")));
        c.put(UnitsLength.YOTTAMETER, Mul(Rat(Int(1), Pow(10, 42)), Sym("attom")));
        c.put(UnitsLength.ZEPTOMETER, Mul(Int(1000), Sym("attom")));
        c.put(UnitsLength.ZETTAMETER, Mul(Rat(Int(1), Pow(10, 39)), Sym("attom")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsLength, Conversion> createMapCENTIMETER() {
        EnumMap<UnitsLength, Conversion> c =
            new EnumMap<UnitsLength, Conversion>(UnitsLength.class);
        c.put(UnitsLength.ANGSTROM, Mul(Pow(10, 8), Sym("centim")));
        c.put(UnitsLength.ASTRONOMICALUNIT, Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 4))), Sym("centim")));
        c.put(UnitsLength.ATTOMETER, Mul(Pow(10, 16), Sym("centim")));
        c.put(UnitsLength.DECAMETER, Mul(Rat(Int(1), Int(1000)), Sym("centim")));
        c.put(UnitsLength.DECIMETER, Mul(Rat(Int(1), Int(10)), Sym("centim")));
        c.put(UnitsLength.EXAMETER, Mul(Rat(Int(1), Pow(10, 20)), Sym("centim")));
        c.put(UnitsLength.FEMTOMETER, Mul(Pow(10, 13), Sym("centim")));
        c.put(UnitsLength.FOOT, Mul(Rat(Int(393701), Mul(Int(12), Pow(10, 6))), Sym("centim")));
        c.put(UnitsLength.GIGAMETER, Mul(Rat(Int(1), Pow(10, 11)), Sym("centim")));
        c.put(UnitsLength.HECTOMETER, Mul(Rat(Int(1), Pow(10, 4)), Sym("centim")));
        c.put(UnitsLength.INCH, Mul(Rat(Int(393701), Pow(10, 6)), Sym("centim")));
        c.put(UnitsLength.KILOMETER, Mul(Rat(Int(1), Pow(10, 5)), Sym("centim")));
        c.put(UnitsLength.LIGHTYEAR, Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 4))), Sym("centim")));
        c.put(UnitsLength.LINE, Mul(Rat(Int(1181103), Mul(Int(25), Pow(10, 4))), Sym("centim")));
        c.put(UnitsLength.MEGAMETER, Mul(Rat(Int(1), Pow(10, 8)), Sym("centim")));
        c.put(UnitsLength.METER, Mul(Rat(Int(1), Int(100)), Sym("centim")));
        c.put(UnitsLength.MICROMETER, Mul(Pow(10, 4), Sym("centim")));
        c.put(UnitsLength.MILE, Mul(Rat(Int(35791), Mul(Int(576), Pow(10, 7))), Sym("centim")));
        c.put(UnitsLength.MILLIMETER, Mul(Int(10), Sym("centim")));
        c.put(UnitsLength.NANOMETER, Mul(Pow(10, 7), Sym("centim")));
        c.put(UnitsLength.PARSEC, Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 11))), Sym("centim")));
        c.put(UnitsLength.PETAMETER, Mul(Rat(Int(1), Pow(10, 17)), Sym("centim")));
        c.put(UnitsLength.PICOMETER, Mul(Pow(10, 10), Sym("centim")));
        c.put(UnitsLength.POINT, Mul(Rat(Int(3543309), Int(125000)), Sym("centim")));
        c.put(UnitsLength.TERAMETER, Mul(Rat(Int(1), Pow(10, 14)), Sym("centim")));
        c.put(UnitsLength.THOU, Mul(Rat(Int(393701), Pow(10, 9)), Sym("centim")));
        c.put(UnitsLength.YARD, Mul(Rat(Int(393701), Mul(Int(36), Pow(10, 6))), Sym("centim")));
        c.put(UnitsLength.YOCTOMETER, Mul(Pow(10, 22), Sym("centim")));
        c.put(UnitsLength.YOTTAMETER, Mul(Rat(Int(1), Pow(10, 26)), Sym("centim")));
        c.put(UnitsLength.ZEPTOMETER, Mul(Pow(10, 19), Sym("centim")));
        c.put(UnitsLength.ZETTAMETER, Mul(Rat(Int(1), Pow(10, 23)), Sym("centim")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsLength, Conversion> createMapDECAMETER() {
        EnumMap<UnitsLength, Conversion> c =
            new EnumMap<UnitsLength, Conversion>(UnitsLength.class);
        c.put(UnitsLength.ANGSTROM, Mul(Pow(10, 11), Sym("decam")));
        c.put(UnitsLength.ASTRONOMICALUNIT, Mul(Rat(Int(1), Int("14959787070")), Sym("decam")));
        c.put(UnitsLength.ATTOMETER, Mul(Pow(10, 19), Sym("decam")));
        c.put(UnitsLength.CENTIMETER, Mul(Int(1000), Sym("decam")));
        c.put(UnitsLength.DECIMETER, Mul(Int(100), Sym("decam")));
        c.put(UnitsLength.EXAMETER, Mul(Rat(Int(1), Pow(10, 17)), Sym("decam")));
        c.put(UnitsLength.FEMTOMETER, Mul(Pow(10, 16), Sym("decam")));
        c.put(UnitsLength.FOOT, Mul(Rat(Int(393701), Int(12000)), Sym("decam")));
        c.put(UnitsLength.GIGAMETER, Mul(Rat(Int(1), Pow(10, 8)), Sym("decam")));
        c.put(UnitsLength.HECTOMETER, Mul(Rat(Int(1), Int(10)), Sym("decam")));
        c.put(UnitsLength.INCH, Mul(Rat(Int(393701), Int(1000)), Sym("decam")));
        c.put(UnitsLength.KILOMETER, Mul(Rat(Int(1), Int(100)), Sym("decam")));
        c.put(UnitsLength.LIGHTYEAR, Mul(Rat(Int(1), Int("946073047258080")), Sym("decam")));
        c.put(UnitsLength.LINE, Mul(Rat(Int(1181103), Int(250)), Sym("decam")));
        c.put(UnitsLength.MEGAMETER, Mul(Rat(Int(1), Pow(10, 5)), Sym("decam")));
        c.put(UnitsLength.METER, Mul(Int(10), Sym("decam")));
        c.put(UnitsLength.MICROMETER, Mul(Pow(10, 7), Sym("decam")));
        c.put(UnitsLength.MILE, Mul(Rat(Int(35791), Mul(Int(576), Pow(10, 4))), Sym("decam")));
        c.put(UnitsLength.MILLIMETER, Mul(Pow(10, 4), Sym("decam")));
        c.put(UnitsLength.NANOMETER, Mul(Pow(10, 10), Sym("decam")));
        c.put(UnitsLength.PARSEC, Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 8))), Sym("decam")));
        c.put(UnitsLength.PETAMETER, Mul(Rat(Int(1), Pow(10, 14)), Sym("decam")));
        c.put(UnitsLength.PICOMETER, Mul(Pow(10, 13), Sym("decam")));
        c.put(UnitsLength.POINT, Mul(Rat(Int(3543309), Int(125)), Sym("decam")));
        c.put(UnitsLength.TERAMETER, Mul(Rat(Int(1), Pow(10, 11)), Sym("decam")));
        c.put(UnitsLength.THOU, Mul(Rat(Int(393701), Pow(10, 6)), Sym("decam")));
        c.put(UnitsLength.YARD, Mul(Rat(Int(393701), Int(36000)), Sym("decam")));
        c.put(UnitsLength.YOCTOMETER, Mul(Pow(10, 25), Sym("decam")));
        c.put(UnitsLength.YOTTAMETER, Mul(Rat(Int(1), Pow(10, 23)), Sym("decam")));
        c.put(UnitsLength.ZEPTOMETER, Mul(Pow(10, 22), Sym("decam")));
        c.put(UnitsLength.ZETTAMETER, Mul(Rat(Int(1), Pow(10, 20)), Sym("decam")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsLength, Conversion> createMapDECIMETER() {
        EnumMap<UnitsLength, Conversion> c =
            new EnumMap<UnitsLength, Conversion>(UnitsLength.class);
        c.put(UnitsLength.ANGSTROM, Mul(Pow(10, 9), Sym("decim")));
        c.put(UnitsLength.ASTRONOMICALUNIT, Mul(Rat(Int(1), Int("1495978707000")), Sym("decim")));
        c.put(UnitsLength.ATTOMETER, Mul(Pow(10, 17), Sym("decim")));
        c.put(UnitsLength.CENTIMETER, Mul(Int(10), Sym("decim")));
        c.put(UnitsLength.DECAMETER, Mul(Rat(Int(1), Int(100)), Sym("decim")));
        c.put(UnitsLength.EXAMETER, Mul(Rat(Int(1), Pow(10, 19)), Sym("decim")));
        c.put(UnitsLength.FEMTOMETER, Mul(Pow(10, 14), Sym("decim")));
        c.put(UnitsLength.FOOT, Mul(Rat(Int(393701), Mul(Int(12), Pow(10, 5))), Sym("decim")));
        c.put(UnitsLength.GIGAMETER, Mul(Rat(Int(1), Pow(10, 10)), Sym("decim")));
        c.put(UnitsLength.HECTOMETER, Mul(Rat(Int(1), Int(1000)), Sym("decim")));
        c.put(UnitsLength.INCH, Mul(Rat(Int(393701), Pow(10, 5)), Sym("decim")));
        c.put(UnitsLength.KILOMETER, Mul(Rat(Int(1), Pow(10, 4)), Sym("decim")));
        c.put(UnitsLength.LIGHTYEAR, Mul(Rat(Int(1), Int("94607304725808000")), Sym("decim")));
        c.put(UnitsLength.LINE, Mul(Rat(Int(1181103), Int(25000)), Sym("decim")));
        c.put(UnitsLength.MEGAMETER, Mul(Rat(Int(1), Pow(10, 7)), Sym("decim")));
        c.put(UnitsLength.METER, Mul(Rat(Int(1), Int(10)), Sym("decim")));
        c.put(UnitsLength.MICROMETER, Mul(Pow(10, 5), Sym("decim")));
        c.put(UnitsLength.MILE, Mul(Rat(Int(35791), Mul(Int(576), Pow(10, 6))), Sym("decim")));
        c.put(UnitsLength.MILLIMETER, Mul(Int(100), Sym("decim")));
        c.put(UnitsLength.NANOMETER, Mul(Pow(10, 8), Sym("decim")));
        c.put(UnitsLength.PARSEC, Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 10))), Sym("decim")));
        c.put(UnitsLength.PETAMETER, Mul(Rat(Int(1), Pow(10, 16)), Sym("decim")));
        c.put(UnitsLength.PICOMETER, Mul(Pow(10, 11), Sym("decim")));
        c.put(UnitsLength.POINT, Mul(Rat(Int(3543309), Int(12500)), Sym("decim")));
        c.put(UnitsLength.TERAMETER, Mul(Rat(Int(1), Pow(10, 13)), Sym("decim")));
        c.put(UnitsLength.THOU, Mul(Rat(Int(393701), Pow(10, 8)), Sym("decim")));
        c.put(UnitsLength.YARD, Mul(Rat(Int(393701), Mul(Int(36), Pow(10, 5))), Sym("decim")));
        c.put(UnitsLength.YOCTOMETER, Mul(Pow(10, 23), Sym("decim")));
        c.put(UnitsLength.YOTTAMETER, Mul(Rat(Int(1), Pow(10, 25)), Sym("decim")));
        c.put(UnitsLength.ZEPTOMETER, Mul(Pow(10, 20), Sym("decim")));
        c.put(UnitsLength.ZETTAMETER, Mul(Rat(Int(1), Pow(10, 22)), Sym("decim")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsLength, Conversion> createMapEXAMETER() {
        EnumMap<UnitsLength, Conversion> c =
            new EnumMap<UnitsLength, Conversion>(UnitsLength.class);
        c.put(UnitsLength.ANGSTROM, Mul(Pow(10, 28), Sym("exam")));
        c.put(UnitsLength.ASTRONOMICALUNIT, Mul(Rat(Pow(10, 16), Int(1495978707)), Sym("exam")));
        c.put(UnitsLength.ATTOMETER, Mul(Pow(10, 36), Sym("exam")));
        c.put(UnitsLength.CENTIMETER, Mul(Pow(10, 20), Sym("exam")));
        c.put(UnitsLength.DECAMETER, Mul(Pow(10, 17), Sym("exam")));
        c.put(UnitsLength.DECIMETER, Mul(Pow(10, 19), Sym("exam")));
        c.put(UnitsLength.FEMTOMETER, Mul(Pow(10, 33), Sym("exam")));
        c.put(UnitsLength.FOOT, Mul(Rat(Mul(Int(9842525), Pow(10, 12)), Int(3)), Sym("exam")));
        c.put(UnitsLength.GIGAMETER, Mul(Pow(10, 9), Sym("exam")));
        c.put(UnitsLength.HECTOMETER, Mul(Pow(10, 16), Sym("exam")));
        c.put(UnitsLength.INCH, Mul(Mul(Int(393701), Pow(10, 14)), Sym("exam")));
        c.put(UnitsLength.KILOMETER, Mul(Pow(10, 15), Sym("exam")));
        c.put(UnitsLength.LIGHTYEAR, Mul(Rat(Mul(Int(625), Pow(10, 12)), Int("5912956545363")), Sym("exam")));
        c.put(UnitsLength.LINE, Mul(Mul(Int(4724412), Pow(10, 14)), Sym("exam")));
        c.put(UnitsLength.MEGAMETER, Mul(Pow(10, 12), Sym("exam")));
        c.put(UnitsLength.METER, Mul(Pow(10, 18), Sym("exam")));
        c.put(UnitsLength.MICROMETER, Mul(Pow(10, 24), Sym("exam")));
        c.put(UnitsLength.MILE, Mul(Rat(Mul(Int(559234375), Pow(10, 7)), Int(9)), Sym("exam")));
        c.put(UnitsLength.MILLIMETER, Mul(Pow(10, 21), Sym("exam")));
        c.put(UnitsLength.NANOMETER, Mul(Pow(10, 27), Sym("exam")));
        c.put(UnitsLength.PARSEC, Mul(Rat(Mul(Int(125), Pow(10, 6)), Int(3857097)), Sym("exam")));
        c.put(UnitsLength.PETAMETER, Mul(Int(1000), Sym("exam")));
        c.put(UnitsLength.PICOMETER, Mul(Pow(10, 30), Sym("exam")));
        c.put(UnitsLength.POINT, Mul(Mul(Int(28346472), Pow(10, 14)), Sym("exam")));
        c.put(UnitsLength.TERAMETER, Mul(Pow(10, 6), Sym("exam")));
        c.put(UnitsLength.THOU, Mul(Mul(Int(393701), Pow(10, 11)), Sym("exam")));
        c.put(UnitsLength.YARD, Mul(Rat(Mul(Int(9842525), Pow(10, 12)), Int(9)), Sym("exam")));
        c.put(UnitsLength.YOCTOMETER, Mul(Pow(10, 42), Sym("exam")));
        c.put(UnitsLength.YOTTAMETER, Mul(Rat(Int(1), Pow(10, 6)), Sym("exam")));
        c.put(UnitsLength.ZEPTOMETER, Mul(Pow(10, 39), Sym("exam")));
        c.put(UnitsLength.ZETTAMETER, Mul(Rat(Int(1), Int(1000)), Sym("exam")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsLength, Conversion> createMapFEMTOMETER() {
        EnumMap<UnitsLength, Conversion> c =
            new EnumMap<UnitsLength, Conversion>(UnitsLength.class);
        c.put(UnitsLength.ANGSTROM, Mul(Rat(Int(1), Pow(10, 5)), Sym("femtom")));
        c.put(UnitsLength.ASTRONOMICALUNIT, Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 17))), Sym("femtom")));
        c.put(UnitsLength.ATTOMETER, Mul(Int(1000), Sym("femtom")));
        c.put(UnitsLength.CENTIMETER, Mul(Rat(Int(1), Pow(10, 13)), Sym("femtom")));
        c.put(UnitsLength.DECAMETER, Mul(Rat(Int(1), Pow(10, 16)), Sym("femtom")));
        c.put(UnitsLength.DECIMETER, Mul(Rat(Int(1), Pow(10, 14)), Sym("femtom")));
        c.put(UnitsLength.EXAMETER, Mul(Rat(Int(1), Pow(10, 33)), Sym("femtom")));
        c.put(UnitsLength.FOOT, Mul(Rat(Int(393701), Mul(Int(12), Pow(10, 19))), Sym("femtom")));
        c.put(UnitsLength.GIGAMETER, Mul(Rat(Int(1), Pow(10, 24)), Sym("femtom")));
        c.put(UnitsLength.HECTOMETER, Mul(Rat(Int(1), Pow(10, 17)), Sym("femtom")));
        c.put(UnitsLength.INCH, Mul(Rat(Int(393701), Pow(10, 19)), Sym("femtom")));
        c.put(UnitsLength.KILOMETER, Mul(Rat(Int(1), Pow(10, 18)), Sym("femtom")));
        c.put(UnitsLength.LIGHTYEAR, Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 17))), Sym("femtom")));
        c.put(UnitsLength.LINE, Mul(Rat(Int(1181103), Mul(Int(25), Pow(10, 17))), Sym("femtom")));
        c.put(UnitsLength.MEGAMETER, Mul(Rat(Int(1), Pow(10, 21)), Sym("femtom")));
        c.put(UnitsLength.METER, Mul(Rat(Int(1), Pow(10, 15)), Sym("femtom")));
        c.put(UnitsLength.MICROMETER, Mul(Rat(Int(1), Pow(10, 9)), Sym("femtom")));
        c.put(UnitsLength.MILE, Mul(Rat(Int(35791), Mul(Int(576), Pow(10, 20))), Sym("femtom")));
        c.put(UnitsLength.MILLIMETER, Mul(Rat(Int(1), Pow(10, 12)), Sym("femtom")));
        c.put(UnitsLength.NANOMETER, Mul(Rat(Int(1), Pow(10, 6)), Sym("femtom")));
        c.put(UnitsLength.PARSEC, Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 24))), Sym("femtom")));
        c.put(UnitsLength.PETAMETER, Mul(Rat(Int(1), Pow(10, 30)), Sym("femtom")));
        c.put(UnitsLength.PICOMETER, Mul(Rat(Int(1), Int(1000)), Sym("femtom")));
        c.put(UnitsLength.POINT, Mul(Rat(Int(3543309), Mul(Int(125), Pow(10, 16))), Sym("femtom")));
        c.put(UnitsLength.TERAMETER, Mul(Rat(Int(1), Pow(10, 27)), Sym("femtom")));
        c.put(UnitsLength.THOU, Mul(Rat(Int(393701), Pow(10, 22)), Sym("femtom")));
        c.put(UnitsLength.YARD, Mul(Rat(Int(393701), Mul(Int(36), Pow(10, 19))), Sym("femtom")));
        c.put(UnitsLength.YOCTOMETER, Mul(Pow(10, 9), Sym("femtom")));
        c.put(UnitsLength.YOTTAMETER, Mul(Rat(Int(1), Pow(10, 39)), Sym("femtom")));
        c.put(UnitsLength.ZEPTOMETER, Mul(Pow(10, 6), Sym("femtom")));
        c.put(UnitsLength.ZETTAMETER, Mul(Rat(Int(1), Pow(10, 36)), Sym("femtom")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsLength, Conversion> createMapFOOT() {
        EnumMap<UnitsLength, Conversion> c =
            new EnumMap<UnitsLength, Conversion>(UnitsLength.class);
        c.put(UnitsLength.ANGSTROM, Mul(Rat(Mul(Int(12), Pow(10, 14)), Int(393701)), Sym("ft")));
        c.put(UnitsLength.ASTRONOMICALUNIT, Mul(Rat(Int(400), Int("196322770974869")), Sym("ft")));
        c.put(UnitsLength.ATTOMETER, Mul(Rat(Mul(Int(12), Pow(10, 22)), Int(393701)), Sym("ft")));
        c.put(UnitsLength.CENTIMETER, Mul(Rat(Mul(Int(12), Pow(10, 6)), Int(393701)), Sym("ft")));
        c.put(UnitsLength.DECAMETER, Mul(Rat(Int(12000), Int(393701)), Sym("ft")));
        c.put(UnitsLength.DECIMETER, Mul(Rat(Mul(Int(12), Pow(10, 5)), Int(393701)), Sym("ft")));
        c.put(UnitsLength.EXAMETER, Mul(Rat(Int(3), Mul(Int(9842525), Pow(10, 12))), Sym("ft")));
        c.put(UnitsLength.FEMTOMETER, Mul(Rat(Mul(Int(12), Pow(10, 19)), Int(393701)), Sym("ft")));
        c.put(UnitsLength.GIGAMETER, Mul(Rat(Int(3), Int("9842525000")), Sym("ft")));
        c.put(UnitsLength.HECTOMETER, Mul(Rat(Int(1200), Int(393701)), Sym("ft")));
        c.put(UnitsLength.INCH, Mul(Int(12), Sym("ft")));
        c.put(UnitsLength.KILOMETER, Mul(Rat(Int(120), Int(393701)), Sym("ft")));
        c.put(UnitsLength.LIGHTYEAR, Mul(Rat(Int(25), Int("775978968288652821")), Sym("ft")));
        c.put(UnitsLength.LINE, Mul(Int(144), Sym("ft")));
        c.put(UnitsLength.MEGAMETER, Mul(Rat(Int(3), Int(9842525)), Sym("ft")));
        c.put(UnitsLength.METER, Mul(Rat(Mul(Int(12), Pow(10, 4)), Int(393701)), Sym("ft")));
        c.put(UnitsLength.MICROMETER, Mul(Rat(Mul(Int(12), Pow(10, 10)), Int(393701)), Sym("ft")));
        c.put(UnitsLength.MILE, Mul(Rat(Int(1), Int(5280)), Sym("ft")));
        c.put(UnitsLength.MILLIMETER, Mul(Rat(Mul(Int(12), Pow(10, 7)), Int(393701)), Sym("ft")));
        c.put(UnitsLength.NANOMETER, Mul(Rat(Mul(Int(12), Pow(10, 13)), Int(393701)), Sym("ft")));
        c.put(UnitsLength.PARSEC, Mul(Rat(Int(1), Mul(Int("1012361963998"), Pow(10, 5))), Sym("ft")));
        c.put(UnitsLength.PETAMETER, Mul(Rat(Int(3), Mul(Int(9842525), Pow(10, 9))), Sym("ft")));
        c.put(UnitsLength.PICOMETER, Mul(Rat(Mul(Int(12), Pow(10, 16)), Int(393701)), Sym("ft")));
        c.put(UnitsLength.POINT, Mul(Int(864), Sym("ft")));
        c.put(UnitsLength.TERAMETER, Mul(Rat(Int(3), Mul(Int(9842525), Pow(10, 6))), Sym("ft")));
        c.put(UnitsLength.THOU, Mul(Rat(Int(3), Int(250)), Sym("ft")));
        c.put(UnitsLength.YARD, Mul(Rat(Int(1), Int(3)), Sym("ft")));
        c.put(UnitsLength.YOCTOMETER, Mul(Rat(Mul(Int(12), Pow(10, 28)), Int(393701)), Sym("ft")));
        c.put(UnitsLength.YOTTAMETER, Mul(Rat(Int(3), Mul(Int(9842525), Pow(10, 18))), Sym("ft")));
        c.put(UnitsLength.ZEPTOMETER, Mul(Rat(Mul(Int(12), Pow(10, 25)), Int(393701)), Sym("ft")));
        c.put(UnitsLength.ZETTAMETER, Mul(Rat(Int(3), Mul(Int(9842525), Pow(10, 15))), Sym("ft")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsLength, Conversion> createMapGIGAMETER() {
        EnumMap<UnitsLength, Conversion> c =
            new EnumMap<UnitsLength, Conversion>(UnitsLength.class);
        c.put(UnitsLength.ANGSTROM, Mul(Pow(10, 19), Sym("gigam")));
        c.put(UnitsLength.ASTRONOMICALUNIT, Mul(Rat(Pow(10, 7), Int(1495978707)), Sym("gigam")));
        c.put(UnitsLength.ATTOMETER, Mul(Pow(10, 27), Sym("gigam")));
        c.put(UnitsLength.CENTIMETER, Mul(Pow(10, 11), Sym("gigam")));
        c.put(UnitsLength.DECAMETER, Mul(Pow(10, 8), Sym("gigam")));
        c.put(UnitsLength.DECIMETER, Mul(Pow(10, 10), Sym("gigam")));
        c.put(UnitsLength.EXAMETER, Mul(Rat(Int(1), Pow(10, 9)), Sym("gigam")));
        c.put(UnitsLength.FEMTOMETER, Mul(Pow(10, 24), Sym("gigam")));
        c.put(UnitsLength.FOOT, Mul(Rat(Int("9842525000"), Int(3)), Sym("gigam")));
        c.put(UnitsLength.HECTOMETER, Mul(Pow(10, 7), Sym("gigam")));
        c.put(UnitsLength.INCH, Mul(Mul(Int(393701), Pow(10, 5)), Sym("gigam")));
        c.put(UnitsLength.KILOMETER, Mul(Pow(10, 6), Sym("gigam")));
        c.put(UnitsLength.LIGHTYEAR, Mul(Rat(Int(625000), Int("5912956545363")), Sym("gigam")));
        c.put(UnitsLength.LINE, Mul(Mul(Int(4724412), Pow(10, 5)), Sym("gigam")));
        c.put(UnitsLength.MEGAMETER, Mul(Int(1000), Sym("gigam")));
        c.put(UnitsLength.METER, Mul(Pow(10, 9), Sym("gigam")));
        c.put(UnitsLength.MICROMETER, Mul(Pow(10, 15), Sym("gigam")));
        c.put(UnitsLength.MILE, Mul(Rat(Int(22369375), Int(36)), Sym("gigam")));
        c.put(UnitsLength.MILLIMETER, Mul(Pow(10, 12), Sym("gigam")));
        c.put(UnitsLength.NANOMETER, Mul(Pow(10, 18), Sym("gigam")));
        c.put(UnitsLength.PARSEC, Mul(Rat(Int(1), Int(30856776)), Sym("gigam")));
        c.put(UnitsLength.PETAMETER, Mul(Rat(Int(1), Pow(10, 6)), Sym("gigam")));
        c.put(UnitsLength.PICOMETER, Mul(Pow(10, 21), Sym("gigam")));
        c.put(UnitsLength.POINT, Mul(Mul(Int(28346472), Pow(10, 5)), Sym("gigam")));
        c.put(UnitsLength.TERAMETER, Mul(Rat(Int(1), Int(1000)), Sym("gigam")));
        c.put(UnitsLength.THOU, Mul(Int(39370100), Sym("gigam")));
        c.put(UnitsLength.YARD, Mul(Rat(Int("9842525000"), Int(9)), Sym("gigam")));
        c.put(UnitsLength.YOCTOMETER, Mul(Pow(10, 33), Sym("gigam")));
        c.put(UnitsLength.YOTTAMETER, Mul(Rat(Int(1), Pow(10, 15)), Sym("gigam")));
        c.put(UnitsLength.ZEPTOMETER, Mul(Pow(10, 30), Sym("gigam")));
        c.put(UnitsLength.ZETTAMETER, Mul(Rat(Int(1), Pow(10, 12)), Sym("gigam")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsLength, Conversion> createMapHECTOMETER() {
        EnumMap<UnitsLength, Conversion> c =
            new EnumMap<UnitsLength, Conversion>(UnitsLength.class);
        c.put(UnitsLength.ANGSTROM, Mul(Pow(10, 12), Sym("hectom")));
        c.put(UnitsLength.ASTRONOMICALUNIT, Mul(Rat(Int(1), Int(1495978707)), Sym("hectom")));
        c.put(UnitsLength.ATTOMETER, Mul(Pow(10, 20), Sym("hectom")));
        c.put(UnitsLength.CENTIMETER, Mul(Pow(10, 4), Sym("hectom")));
        c.put(UnitsLength.DECAMETER, Mul(Int(10), Sym("hectom")));
        c.put(UnitsLength.DECIMETER, Mul(Int(1000), Sym("hectom")));
        c.put(UnitsLength.EXAMETER, Mul(Rat(Int(1), Pow(10, 16)), Sym("hectom")));
        c.put(UnitsLength.FEMTOMETER, Mul(Pow(10, 17), Sym("hectom")));
        c.put(UnitsLength.FOOT, Mul(Rat(Int(393701), Int(1200)), Sym("hectom")));
        c.put(UnitsLength.GIGAMETER, Mul(Rat(Int(1), Pow(10, 7)), Sym("hectom")));
        c.put(UnitsLength.INCH, Mul(Rat(Int(393701), Int(100)), Sym("hectom")));
        c.put(UnitsLength.KILOMETER, Mul(Rat(Int(1), Int(10)), Sym("hectom")));
        c.put(UnitsLength.LIGHTYEAR, Mul(Rat(Int(1), Int("94607304725808")), Sym("hectom")));
        c.put(UnitsLength.LINE, Mul(Rat(Int(1181103), Int(25)), Sym("hectom")));
        c.put(UnitsLength.MEGAMETER, Mul(Rat(Int(1), Pow(10, 4)), Sym("hectom")));
        c.put(UnitsLength.METER, Mul(Int(100), Sym("hectom")));
        c.put(UnitsLength.MICROMETER, Mul(Pow(10, 8), Sym("hectom")));
        c.put(UnitsLength.MILE, Mul(Rat(Int(35791), Int(576000)), Sym("hectom")));
        c.put(UnitsLength.MILLIMETER, Mul(Pow(10, 5), Sym("hectom")));
        c.put(UnitsLength.NANOMETER, Mul(Pow(10, 11), Sym("hectom")));
        c.put(UnitsLength.PARSEC, Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 7))), Sym("hectom")));
        c.put(UnitsLength.PETAMETER, Mul(Rat(Int(1), Pow(10, 13)), Sym("hectom")));
        c.put(UnitsLength.PICOMETER, Mul(Pow(10, 14), Sym("hectom")));
        c.put(UnitsLength.POINT, Mul(Rat(Int(7086618), Int(25)), Sym("hectom")));
        c.put(UnitsLength.TERAMETER, Mul(Rat(Int(1), Pow(10, 10)), Sym("hectom")));
        c.put(UnitsLength.THOU, Mul(Rat(Int(393701), Pow(10, 5)), Sym("hectom")));
        c.put(UnitsLength.YARD, Mul(Rat(Int(393701), Int(3600)), Sym("hectom")));
        c.put(UnitsLength.YOCTOMETER, Mul(Pow(10, 26), Sym("hectom")));
        c.put(UnitsLength.YOTTAMETER, Mul(Rat(Int(1), Pow(10, 22)), Sym("hectom")));
        c.put(UnitsLength.ZEPTOMETER, Mul(Pow(10, 23), Sym("hectom")));
        c.put(UnitsLength.ZETTAMETER, Mul(Rat(Int(1), Pow(10, 19)), Sym("hectom")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsLength, Conversion> createMapINCH() {
        EnumMap<UnitsLength, Conversion> c =
            new EnumMap<UnitsLength, Conversion>(UnitsLength.class);
        c.put(UnitsLength.ANGSTROM, Mul(Rat(Pow(10, 14), Int(393701)), Sym("in")));
        c.put(UnitsLength.ASTRONOMICALUNIT, Mul(Rat(Int(100), Int("588968312924607")), Sym("in")));
        c.put(UnitsLength.ATTOMETER, Mul(Rat(Pow(10, 22), Int(393701)), Sym("in")));
        c.put(UnitsLength.CENTIMETER, Mul(Rat(Pow(10, 6), Int(393701)), Sym("in")));
        c.put(UnitsLength.DECAMETER, Mul(Rat(Int(1000), Int(393701)), Sym("in")));
        c.put(UnitsLength.DECIMETER, Mul(Rat(Pow(10, 5), Int(393701)), Sym("in")));
        c.put(UnitsLength.EXAMETER, Mul(Rat(Int(1), Mul(Int(393701), Pow(10, 14))), Sym("in")));
        c.put(UnitsLength.FEMTOMETER, Mul(Rat(Pow(10, 19), Int(393701)), Sym("in")));
        c.put(UnitsLength.FOOT, Mul(Rat(Int(1), Int(12)), Sym("in")));
        c.put(UnitsLength.GIGAMETER, Mul(Rat(Int(1), Mul(Int(393701), Pow(10, 5))), Sym("in")));
        c.put(UnitsLength.HECTOMETER, Mul(Rat(Int(100), Int(393701)), Sym("in")));
        c.put(UnitsLength.KILOMETER, Mul(Rat(Int(10), Int(393701)), Sym("in")));
        c.put(UnitsLength.LIGHTYEAR, Mul(Rat(Int(25), Int("9311747619463833852")), Sym("in")));
        c.put(UnitsLength.LINE, Mul(Int(12), Sym("in")));
        c.put(UnitsLength.MEGAMETER, Mul(Rat(Int(1), Int(39370100)), Sym("in")));
        c.put(UnitsLength.METER, Mul(Rat(Pow(10, 4), Int(393701)), Sym("in")));
        c.put(UnitsLength.MICROMETER, Mul(Rat(Pow(10, 10), Int(393701)), Sym("in")));
        c.put(UnitsLength.MILE, Mul(Rat(Int(1), Int(63360)), Sym("in")));
        c.put(UnitsLength.MILLIMETER, Mul(Rat(Pow(10, 7), Int(393701)), Sym("in")));
        c.put(UnitsLength.NANOMETER, Mul(Rat(Pow(10, 13), Int(393701)), Sym("in")));
        c.put(UnitsLength.PARSEC, Mul(Rat(Int(1), Mul(Int("12148343567976"), Pow(10, 5))), Sym("in")));
        c.put(UnitsLength.PETAMETER, Mul(Rat(Int(1), Mul(Int(393701), Pow(10, 11))), Sym("in")));
        c.put(UnitsLength.PICOMETER, Mul(Rat(Pow(10, 16), Int(393701)), Sym("in")));
        c.put(UnitsLength.POINT, Mul(Int(72), Sym("in")));
        c.put(UnitsLength.TERAMETER, Mul(Rat(Int(1), Mul(Int(393701), Pow(10, 8))), Sym("in")));
        c.put(UnitsLength.THOU, Mul(Rat(Int(1), Int(1000)), Sym("in")));
        c.put(UnitsLength.YARD, Mul(Rat(Int(1), Int(36)), Sym("in")));
        c.put(UnitsLength.YOCTOMETER, Mul(Rat(Pow(10, 28), Int(393701)), Sym("in")));
        c.put(UnitsLength.YOTTAMETER, Mul(Rat(Int(1), Mul(Int(393701), Pow(10, 20))), Sym("in")));
        c.put(UnitsLength.ZEPTOMETER, Mul(Rat(Pow(10, 25), Int(393701)), Sym("in")));
        c.put(UnitsLength.ZETTAMETER, Mul(Rat(Int(1), Mul(Int(393701), Pow(10, 17))), Sym("in")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsLength, Conversion> createMapKILOMETER() {
        EnumMap<UnitsLength, Conversion> c =
            new EnumMap<UnitsLength, Conversion>(UnitsLength.class);
        c.put(UnitsLength.ANGSTROM, Mul(Pow(10, 13), Sym("kilom")));
        c.put(UnitsLength.ASTRONOMICALUNIT, Mul(Rat(Int(10), Int(1495978707)), Sym("kilom")));
        c.put(UnitsLength.ATTOMETER, Mul(Pow(10, 21), Sym("kilom")));
        c.put(UnitsLength.CENTIMETER, Mul(Pow(10, 5), Sym("kilom")));
        c.put(UnitsLength.DECAMETER, Mul(Int(100), Sym("kilom")));
        c.put(UnitsLength.DECIMETER, Mul(Pow(10, 4), Sym("kilom")));
        c.put(UnitsLength.EXAMETER, Mul(Rat(Int(1), Pow(10, 15)), Sym("kilom")));
        c.put(UnitsLength.FEMTOMETER, Mul(Pow(10, 18), Sym("kilom")));
        c.put(UnitsLength.FOOT, Mul(Rat(Int(393701), Int(120)), Sym("kilom")));
        c.put(UnitsLength.GIGAMETER, Mul(Rat(Int(1), Pow(10, 6)), Sym("kilom")));
        c.put(UnitsLength.HECTOMETER, Mul(Int(10), Sym("kilom")));
        c.put(UnitsLength.INCH, Mul(Rat(Int(393701), Int(10)), Sym("kilom")));
        c.put(UnitsLength.LIGHTYEAR, Mul(Rat(Int(5), Int("47303652362904")), Sym("kilom")));
        c.put(UnitsLength.LINE, Mul(Rat(Int(2362206), Int(5)), Sym("kilom")));
        c.put(UnitsLength.MEGAMETER, Mul(Rat(Int(1), Int(1000)), Sym("kilom")));
        c.put(UnitsLength.METER, Mul(Int(1000), Sym("kilom")));
        c.put(UnitsLength.MICROMETER, Mul(Pow(10, 9), Sym("kilom")));
        c.put(UnitsLength.MILE, Mul(Rat(Int(35791), Int(57600)), Sym("kilom")));
        c.put(UnitsLength.MILLIMETER, Mul(Pow(10, 6), Sym("kilom")));
        c.put(UnitsLength.NANOMETER, Mul(Pow(10, 12), Sym("kilom")));
        c.put(UnitsLength.PARSEC, Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 6))), Sym("kilom")));
        c.put(UnitsLength.PETAMETER, Mul(Rat(Int(1), Pow(10, 12)), Sym("kilom")));
        c.put(UnitsLength.PICOMETER, Mul(Pow(10, 15), Sym("kilom")));
        c.put(UnitsLength.POINT, Mul(Rat(Int(14173236), Int(5)), Sym("kilom")));
        c.put(UnitsLength.TERAMETER, Mul(Rat(Int(1), Pow(10, 9)), Sym("kilom")));
        c.put(UnitsLength.THOU, Mul(Rat(Int(393701), Pow(10, 4)), Sym("kilom")));
        c.put(UnitsLength.YARD, Mul(Rat(Int(393701), Int(360)), Sym("kilom")));
        c.put(UnitsLength.YOCTOMETER, Mul(Pow(10, 27), Sym("kilom")));
        c.put(UnitsLength.YOTTAMETER, Mul(Rat(Int(1), Pow(10, 21)), Sym("kilom")));
        c.put(UnitsLength.ZEPTOMETER, Mul(Pow(10, 24), Sym("kilom")));
        c.put(UnitsLength.ZETTAMETER, Mul(Rat(Int(1), Pow(10, 18)), Sym("kilom")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsLength, Conversion> createMapLIGHTYEAR() {
        EnumMap<UnitsLength, Conversion> c =
            new EnumMap<UnitsLength, Conversion>(UnitsLength.class);
        c.put(UnitsLength.ANGSTROM, Mul(Mul(Int("94607304725808"), Pow(10, 12)), Sym("ly")));
        c.put(UnitsLength.ASTRONOMICALUNIT, Mul(Rat(Int("431996825232"), Int(6830953)), Sym("ly")));
        c.put(UnitsLength.ATTOMETER, Mul(Mul(Int("94607304725808"), Pow(10, 20)), Sym("ly")));
        c.put(UnitsLength.CENTIMETER, Mul(Mul(Int("94607304725808"), Pow(10, 4)), Sym("ly")));
        c.put(UnitsLength.DECAMETER, Mul(Int("946073047258080"), Sym("ly")));
        c.put(UnitsLength.DECIMETER, Mul(Int("94607304725808000"), Sym("ly")));
        c.put(UnitsLength.EXAMETER, Mul(Rat(Int("5912956545363"), Mul(Int(625), Pow(10, 12))), Sym("ly")));
        c.put(UnitsLength.FEMTOMETER, Mul(Mul(Int("94607304725808"), Pow(10, 17)), Sym("ly")));
        c.put(UnitsLength.FOOT, Mul(Rat(Int("775978968288652821"), Int(25)), Sym("ly")));
        c.put(UnitsLength.GIGAMETER, Mul(Rat(Int("5912956545363"), Int(625000)), Sym("ly")));
        c.put(UnitsLength.HECTOMETER, Mul(Int("94607304725808"), Sym("ly")));
        c.put(UnitsLength.INCH, Mul(Rat(Int("9311747619463833852"), Int(25)), Sym("ly")));
        c.put(UnitsLength.KILOMETER, Mul(Rat(Int("47303652362904"), Int(5)), Sym("ly")));
        c.put(UnitsLength.LINE, Mul(Rat(Int("111740971433566006224"), Int(25)), Sym("ly")));
        c.put(UnitsLength.MEGAMETER, Mul(Rat(Int("5912956545363"), Int(625)), Sym("ly")));
        c.put(UnitsLength.METER, Mul(Int("9460730472580800"), Sym("ly")));
        c.put(UnitsLength.MICROMETER, Mul(Mul(Int("94607304725808"), Pow(10, 8)), Sym("ly")));
        c.put(UnitsLength.MILE, Mul(Rat(Int("23514514190565237"), Int(4000)), Sym("ly")));
        c.put(UnitsLength.MILLIMETER, Mul(Mul(Int("94607304725808"), Pow(10, 5)), Sym("ly")));
        c.put(UnitsLength.NANOMETER, Mul(Mul(Int("94607304725808"), Pow(10, 11)), Sym("ly")));
        c.put(UnitsLength.PARSEC, Mul(Rat(Int("1970985515121"), Mul(Int(6428495), Pow(10, 6))), Sym("ly")));
        c.put(UnitsLength.PETAMETER, Mul(Rat(Int("5912956545363"), Mul(Int(625), Pow(10, 9))), Sym("ly")));
        c.put(UnitsLength.PICOMETER, Mul(Mul(Int("94607304725808"), Pow(10, 14)), Sym("ly")));
        c.put(UnitsLength.POINT, Mul(Rat(Int("670445828601396037344"), Int(25)), Sym("ly")));
        c.put(UnitsLength.TERAMETER, Mul(Rat(Int("5912956545363"), Mul(Int(625), Pow(10, 6))), Sym("ly")));
        c.put(UnitsLength.THOU, Mul(Rat(Int("2327936904865958463"), Int(6250)), Sym("ly")));
        c.put(UnitsLength.YARD, Mul(Rat(Int("258659656096217607"), Int(25)), Sym("ly")));
        c.put(UnitsLength.YOCTOMETER, Mul(Mul(Int("94607304725808"), Pow(10, 26)), Sym("ly")));
        c.put(UnitsLength.YOTTAMETER, Mul(Rat(Int("5912956545363"), Mul(Int(625), Pow(10, 18))), Sym("ly")));
        c.put(UnitsLength.ZEPTOMETER, Mul(Mul(Int("94607304725808"), Pow(10, 23)), Sym("ly")));
        c.put(UnitsLength.ZETTAMETER, Mul(Rat(Int("5912956545363"), Mul(Int(625), Pow(10, 15))), Sym("ly")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsLength, Conversion> createMapLINE() {
        EnumMap<UnitsLength, Conversion> c =
            new EnumMap<UnitsLength, Conversion>(UnitsLength.class);
        c.put(UnitsLength.ANGSTROM, Mul(Rat(Mul(Int(25), Pow(10, 12)), Int(1181103)), Sym("li")));
        c.put(UnitsLength.ASTRONOMICALUNIT, Mul(Rat(Int(25), Int("1766904938773821")), Sym("li")));
        c.put(UnitsLength.ATTOMETER, Mul(Rat(Mul(Int(25), Pow(10, 20)), Int(1181103)), Sym("li")));
        c.put(UnitsLength.CENTIMETER, Mul(Rat(Mul(Int(25), Pow(10, 4)), Int(1181103)), Sym("li")));
        c.put(UnitsLength.DECAMETER, Mul(Rat(Int(250), Int(1181103)), Sym("li")));
        c.put(UnitsLength.DECIMETER, Mul(Rat(Int(25000), Int(1181103)), Sym("li")));
        c.put(UnitsLength.EXAMETER, Mul(Rat(Int(1), Mul(Int(4724412), Pow(10, 14))), Sym("li")));
        c.put(UnitsLength.FEMTOMETER, Mul(Rat(Mul(Int(25), Pow(10, 17)), Int(1181103)), Sym("li")));
        c.put(UnitsLength.FOOT, Mul(Rat(Int(1), Int(144)), Sym("li")));
        c.put(UnitsLength.GIGAMETER, Mul(Rat(Int(1), Mul(Int(4724412), Pow(10, 5))), Sym("li")));
        c.put(UnitsLength.HECTOMETER, Mul(Rat(Int(25), Int(1181103)), Sym("li")));
        c.put(UnitsLength.INCH, Mul(Rat(Int(1), Int(12)), Sym("li")));
        c.put(UnitsLength.KILOMETER, Mul(Rat(Int(5), Int(2362206)), Sym("li")));
        c.put(UnitsLength.LIGHTYEAR, Mul(Rat(Int(25), Int("111740971433566006224")), Sym("li")));
        c.put(UnitsLength.MEGAMETER, Mul(Rat(Int(1), Int(472441200)), Sym("li")));
        c.put(UnitsLength.METER, Mul(Rat(Int(2500), Int(1181103)), Sym("li")));
        c.put(UnitsLength.MICROMETER, Mul(Rat(Mul(Int(25), Pow(10, 8)), Int(1181103)), Sym("li")));
        c.put(UnitsLength.MILE, Mul(Rat(Int(1), Int(760320)), Sym("li")));
        c.put(UnitsLength.MILLIMETER, Mul(Rat(Mul(Int(25), Pow(10, 5)), Int(1181103)), Sym("li")));
        c.put(UnitsLength.NANOMETER, Mul(Rat(Mul(Int(25), Pow(10, 11)), Int(1181103)), Sym("li")));
        c.put(UnitsLength.PARSEC, Mul(Rat(Int(1), Mul(Int("145780122815712"), Pow(10, 5))), Sym("li")));
        c.put(UnitsLength.PETAMETER, Mul(Rat(Int(1), Mul(Int(4724412), Pow(10, 11))), Sym("li")));
        c.put(UnitsLength.PICOMETER, Mul(Rat(Mul(Int(25), Pow(10, 14)), Int(1181103)), Sym("li")));
        c.put(UnitsLength.POINT, Mul(Int(6), Sym("li")));
        c.put(UnitsLength.TERAMETER, Mul(Rat(Int(1), Mul(Int(4724412), Pow(10, 8))), Sym("li")));
        c.put(UnitsLength.THOU, Mul(Rat(Int(1), Int(12000)), Sym("li")));
        c.put(UnitsLength.YARD, Mul(Rat(Int(1), Int(432)), Sym("li")));
        c.put(UnitsLength.YOCTOMETER, Mul(Rat(Mul(Int(25), Pow(10, 26)), Int(1181103)), Sym("li")));
        c.put(UnitsLength.YOTTAMETER, Mul(Rat(Int(1), Mul(Int(4724412), Pow(10, 20))), Sym("li")));
        c.put(UnitsLength.ZEPTOMETER, Mul(Rat(Mul(Int(25), Pow(10, 23)), Int(1181103)), Sym("li")));
        c.put(UnitsLength.ZETTAMETER, Mul(Rat(Int(1), Mul(Int(4724412), Pow(10, 17))), Sym("li")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsLength, Conversion> createMapMEGAMETER() {
        EnumMap<UnitsLength, Conversion> c =
            new EnumMap<UnitsLength, Conversion>(UnitsLength.class);
        c.put(UnitsLength.ANGSTROM, Mul(Pow(10, 16), Sym("megam")));
        c.put(UnitsLength.ASTRONOMICALUNIT, Mul(Rat(Pow(10, 4), Int(1495978707)), Sym("megam")));
        c.put(UnitsLength.ATTOMETER, Mul(Pow(10, 24), Sym("megam")));
        c.put(UnitsLength.CENTIMETER, Mul(Pow(10, 8), Sym("megam")));
        c.put(UnitsLength.DECAMETER, Mul(Pow(10, 5), Sym("megam")));
        c.put(UnitsLength.DECIMETER, Mul(Pow(10, 7), Sym("megam")));
        c.put(UnitsLength.EXAMETER, Mul(Rat(Int(1), Pow(10, 12)), Sym("megam")));
        c.put(UnitsLength.FEMTOMETER, Mul(Pow(10, 21), Sym("megam")));
        c.put(UnitsLength.FOOT, Mul(Rat(Int(9842525), Int(3)), Sym("megam")));
        c.put(UnitsLength.GIGAMETER, Mul(Rat(Int(1), Int(1000)), Sym("megam")));
        c.put(UnitsLength.HECTOMETER, Mul(Pow(10, 4), Sym("megam")));
        c.put(UnitsLength.INCH, Mul(Int(39370100), Sym("megam")));
        c.put(UnitsLength.KILOMETER, Mul(Int(1000), Sym("megam")));
        c.put(UnitsLength.LIGHTYEAR, Mul(Rat(Int(625), Int("5912956545363")), Sym("megam")));
        c.put(UnitsLength.LINE, Mul(Int(472441200), Sym("megam")));
        c.put(UnitsLength.METER, Mul(Pow(10, 6), Sym("megam")));
        c.put(UnitsLength.MICROMETER, Mul(Pow(10, 12), Sym("megam")));
        c.put(UnitsLength.MILE, Mul(Rat(Int(178955), Int(288)), Sym("megam")));
        c.put(UnitsLength.MILLIMETER, Mul(Pow(10, 9), Sym("megam")));
        c.put(UnitsLength.NANOMETER, Mul(Pow(10, 15), Sym("megam")));
        c.put(UnitsLength.PARSEC, Mul(Rat(Int(1), Int("30856776000")), Sym("megam")));
        c.put(UnitsLength.PETAMETER, Mul(Rat(Int(1), Pow(10, 9)), Sym("megam")));
        c.put(UnitsLength.PICOMETER, Mul(Pow(10, 18), Sym("megam")));
        c.put(UnitsLength.POINT, Mul(Int("2834647200"), Sym("megam")));
        c.put(UnitsLength.TERAMETER, Mul(Rat(Int(1), Pow(10, 6)), Sym("megam")));
        c.put(UnitsLength.THOU, Mul(Rat(Int(393701), Int(10)), Sym("megam")));
        c.put(UnitsLength.YARD, Mul(Rat(Int(9842525), Int(9)), Sym("megam")));
        c.put(UnitsLength.YOCTOMETER, Mul(Pow(10, 30), Sym("megam")));
        c.put(UnitsLength.YOTTAMETER, Mul(Rat(Int(1), Pow(10, 18)), Sym("megam")));
        c.put(UnitsLength.ZEPTOMETER, Mul(Pow(10, 27), Sym("megam")));
        c.put(UnitsLength.ZETTAMETER, Mul(Rat(Int(1), Pow(10, 15)), Sym("megam")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsLength, Conversion> createMapMETER() {
        EnumMap<UnitsLength, Conversion> c =
            new EnumMap<UnitsLength, Conversion>(UnitsLength.class);
        c.put(UnitsLength.ANGSTROM, Mul(Pow(10, 10), Sym("m")));
        c.put(UnitsLength.ASTRONOMICALUNIT, Mul(Rat(Int(1), Int("149597870700")), Sym("m")));
        c.put(UnitsLength.ATTOMETER, Mul(Pow(10, 18), Sym("m")));
        c.put(UnitsLength.CENTIMETER, Mul(Int(100), Sym("m")));
        c.put(UnitsLength.DECAMETER, Mul(Rat(Int(1), Int(10)), Sym("m")));
        c.put(UnitsLength.DECIMETER, Mul(Int(10), Sym("m")));
        c.put(UnitsLength.EXAMETER, Mul(Rat(Int(1), Pow(10, 18)), Sym("m")));
        c.put(UnitsLength.FEMTOMETER, Mul(Pow(10, 15), Sym("m")));
        c.put(UnitsLength.FOOT, Mul(Rat(Int(393701), Mul(Int(12), Pow(10, 4))), Sym("m")));
        c.put(UnitsLength.GIGAMETER, Mul(Rat(Int(1), Pow(10, 9)), Sym("m")));
        c.put(UnitsLength.HECTOMETER, Mul(Rat(Int(1), Int(100)), Sym("m")));
        c.put(UnitsLength.INCH, Mul(Rat(Int(393701), Pow(10, 4)), Sym("m")));
        c.put(UnitsLength.KILOMETER, Mul(Rat(Int(1), Int(1000)), Sym("m")));
        c.put(UnitsLength.LIGHTYEAR, Mul(Rat(Int(1), Int("9460730472580800")), Sym("m")));
        c.put(UnitsLength.LINE, Mul(Rat(Int(1181103), Int(2500)), Sym("m")));
        c.put(UnitsLength.MEGAMETER, Mul(Rat(Int(1), Pow(10, 6)), Sym("m")));
        c.put(UnitsLength.MICROMETER, Mul(Pow(10, 6), Sym("m")));
        c.put(UnitsLength.MILE, Mul(Rat(Int(35791), Mul(Int(576), Pow(10, 5))), Sym("m")));
        c.put(UnitsLength.MILLIMETER, Mul(Int(1000), Sym("m")));
        c.put(UnitsLength.NANOMETER, Mul(Pow(10, 9), Sym("m")));
        c.put(UnitsLength.PARSEC, Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 9))), Sym("m")));
        c.put(UnitsLength.PETAMETER, Mul(Rat(Int(1), Pow(10, 15)), Sym("m")));
        c.put(UnitsLength.PICOMETER, Mul(Pow(10, 12), Sym("m")));
        c.put(UnitsLength.POINT, Mul(Rat(Int(3543309), Int(1250)), Sym("m")));
        c.put(UnitsLength.TERAMETER, Mul(Rat(Int(1), Pow(10, 12)), Sym("m")));
        c.put(UnitsLength.THOU, Mul(Rat(Int(393701), Pow(10, 7)), Sym("m")));
        c.put(UnitsLength.YARD, Mul(Rat(Int(393701), Mul(Int(36), Pow(10, 4))), Sym("m")));
        c.put(UnitsLength.YOCTOMETER, Mul(Pow(10, 24), Sym("m")));
        c.put(UnitsLength.YOTTAMETER, Mul(Rat(Int(1), Pow(10, 24)), Sym("m")));
        c.put(UnitsLength.ZEPTOMETER, Mul(Pow(10, 21), Sym("m")));
        c.put(UnitsLength.ZETTAMETER, Mul(Rat(Int(1), Pow(10, 21)), Sym("m")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsLength, Conversion> createMapMICROMETER() {
        EnumMap<UnitsLength, Conversion> c =
            new EnumMap<UnitsLength, Conversion>(UnitsLength.class);
        c.put(UnitsLength.ANGSTROM, Mul(Pow(10, 4), Sym("microm")));
        c.put(UnitsLength.ASTRONOMICALUNIT, Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 8))), Sym("microm")));
        c.put(UnitsLength.ATTOMETER, Mul(Pow(10, 12), Sym("microm")));
        c.put(UnitsLength.CENTIMETER, Mul(Rat(Int(1), Pow(10, 4)), Sym("microm")));
        c.put(UnitsLength.DECAMETER, Mul(Rat(Int(1), Pow(10, 7)), Sym("microm")));
        c.put(UnitsLength.DECIMETER, Mul(Rat(Int(1), Pow(10, 5)), Sym("microm")));
        c.put(UnitsLength.EXAMETER, Mul(Rat(Int(1), Pow(10, 24)), Sym("microm")));
        c.put(UnitsLength.FEMTOMETER, Mul(Pow(10, 9), Sym("microm")));
        c.put(UnitsLength.FOOT, Mul(Rat(Int(393701), Mul(Int(12), Pow(10, 10))), Sym("microm")));
        c.put(UnitsLength.GIGAMETER, Mul(Rat(Int(1), Pow(10, 15)), Sym("microm")));
        c.put(UnitsLength.HECTOMETER, Mul(Rat(Int(1), Pow(10, 8)), Sym("microm")));
        c.put(UnitsLength.INCH, Mul(Rat(Int(393701), Pow(10, 10)), Sym("microm")));
        c.put(UnitsLength.KILOMETER, Mul(Rat(Int(1), Pow(10, 9)), Sym("microm")));
        c.put(UnitsLength.LIGHTYEAR, Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 8))), Sym("microm")));
        c.put(UnitsLength.LINE, Mul(Rat(Int(1181103), Mul(Int(25), Pow(10, 8))), Sym("microm")));
        c.put(UnitsLength.MEGAMETER, Mul(Rat(Int(1), Pow(10, 12)), Sym("microm")));
        c.put(UnitsLength.METER, Mul(Rat(Int(1), Pow(10, 6)), Sym("microm")));
        c.put(UnitsLength.MILE, Mul(Rat(Int(35791), Mul(Int(576), Pow(10, 11))), Sym("microm")));
        c.put(UnitsLength.MILLIMETER, Mul(Rat(Int(1), Int(1000)), Sym("microm")));
        c.put(UnitsLength.NANOMETER, Mul(Int(1000), Sym("microm")));
        c.put(UnitsLength.PARSEC, Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 15))), Sym("microm")));
        c.put(UnitsLength.PETAMETER, Mul(Rat(Int(1), Pow(10, 21)), Sym("microm")));
        c.put(UnitsLength.PICOMETER, Mul(Pow(10, 6), Sym("microm")));
        c.put(UnitsLength.POINT, Mul(Rat(Int(3543309), Mul(Int(125), Pow(10, 7))), Sym("microm")));
        c.put(UnitsLength.TERAMETER, Mul(Rat(Int(1), Pow(10, 18)), Sym("microm")));
        c.put(UnitsLength.THOU, Mul(Rat(Int(393701), Pow(10, 13)), Sym("microm")));
        c.put(UnitsLength.YARD, Mul(Rat(Int(393701), Mul(Int(36), Pow(10, 10))), Sym("microm")));
        c.put(UnitsLength.YOCTOMETER, Mul(Pow(10, 18), Sym("microm")));
        c.put(UnitsLength.YOTTAMETER, Mul(Rat(Int(1), Pow(10, 30)), Sym("microm")));
        c.put(UnitsLength.ZEPTOMETER, Mul(Pow(10, 15), Sym("microm")));
        c.put(UnitsLength.ZETTAMETER, Mul(Rat(Int(1), Pow(10, 27)), Sym("microm")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsLength, Conversion> createMapMILE() {
        EnumMap<UnitsLength, Conversion> c =
            new EnumMap<UnitsLength, Conversion>(UnitsLength.class);
        c.put(UnitsLength.ANGSTROM, Mul(Rat(Mul(Int(576), Pow(10, 15)), Int(35791)), Sym("mi")));
        c.put(UnitsLength.ASTRONOMICALUNIT, Mul(Rat(Int(192000), Int("17847524634079")), Sym("mi")));
        c.put(UnitsLength.ATTOMETER, Mul(Rat(Mul(Int(576), Pow(10, 23)), Int(35791)), Sym("mi")));
        c.put(UnitsLength.CENTIMETER, Mul(Rat(Mul(Int(576), Pow(10, 7)), Int(35791)), Sym("mi")));
        c.put(UnitsLength.DECAMETER, Mul(Rat(Mul(Int(576), Pow(10, 4)), Int(35791)), Sym("mi")));
        c.put(UnitsLength.DECIMETER, Mul(Rat(Mul(Int(576), Pow(10, 6)), Int(35791)), Sym("mi")));
        c.put(UnitsLength.EXAMETER, Mul(Rat(Int(9), Mul(Int(559234375), Pow(10, 7))), Sym("mi")));
        c.put(UnitsLength.FEMTOMETER, Mul(Rat(Mul(Int(576), Pow(10, 20)), Int(35791)), Sym("mi")));
        c.put(UnitsLength.FOOT, Mul(Int(5280), Sym("mi")));
        c.put(UnitsLength.GIGAMETER, Mul(Rat(Int(36), Int(22369375)), Sym("mi")));
        c.put(UnitsLength.HECTOMETER, Mul(Rat(Int(576000), Int(35791)), Sym("mi")));
        c.put(UnitsLength.INCH, Mul(Int(63360), Sym("mi")));
        c.put(UnitsLength.KILOMETER, Mul(Rat(Int(57600), Int(35791)), Sym("mi")));
        c.put(UnitsLength.LIGHTYEAR, Mul(Rat(Int(4000), Int("23514514190565237")), Sym("mi")));
        c.put(UnitsLength.LINE, Mul(Int(760320), Sym("mi")));
        c.put(UnitsLength.MEGAMETER, Mul(Rat(Int(288), Int(178955)), Sym("mi")));
        c.put(UnitsLength.METER, Mul(Rat(Mul(Int(576), Pow(10, 5)), Int(35791)), Sym("mi")));
        c.put(UnitsLength.MICROMETER, Mul(Rat(Mul(Int(576), Pow(10, 11)), Int(35791)), Sym("mi")));
        c.put(UnitsLength.MILLIMETER, Mul(Rat(Mul(Int(576), Pow(10, 8)), Int(35791)), Sym("mi")));
        c.put(UnitsLength.NANOMETER, Mul(Rat(Mul(Int(576), Pow(10, 14)), Int(35791)), Sym("mi")));
        c.put(UnitsLength.PARSEC, Mul(Rat(Int(3), Int("57520566136250")), Sym("mi")));
        c.put(UnitsLength.PETAMETER, Mul(Rat(Int(9), Mul(Int(559234375), Pow(10, 4))), Sym("mi")));
        c.put(UnitsLength.PICOMETER, Mul(Rat(Mul(Int(576), Pow(10, 17)), Int(35791)), Sym("mi")));
        c.put(UnitsLength.POINT, Mul(Int(4561920), Sym("mi")));
        c.put(UnitsLength.TERAMETER, Mul(Rat(Int(9), Int("5592343750")), Sym("mi")));
        c.put(UnitsLength.THOU, Mul(Rat(Int(1584), Int(25)), Sym("mi")));
        c.put(UnitsLength.YARD, Mul(Int(1760), Sym("mi")));
        c.put(UnitsLength.YOCTOMETER, Mul(Rat(Mul(Int(576), Pow(10, 29)), Int(35791)), Sym("mi")));
        c.put(UnitsLength.YOTTAMETER, Mul(Rat(Int(9), Mul(Int(559234375), Pow(10, 13))), Sym("mi")));
        c.put(UnitsLength.ZEPTOMETER, Mul(Rat(Mul(Int(576), Pow(10, 26)), Int(35791)), Sym("mi")));
        c.put(UnitsLength.ZETTAMETER, Mul(Rat(Int(9), Mul(Int(559234375), Pow(10, 10))), Sym("mi")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsLength, Conversion> createMapMILLIMETER() {
        EnumMap<UnitsLength, Conversion> c =
            new EnumMap<UnitsLength, Conversion>(UnitsLength.class);
        c.put(UnitsLength.ANGSTROM, Mul(Pow(10, 7), Sym("millim")));
        c.put(UnitsLength.ASTRONOMICALUNIT, Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 5))), Sym("millim")));
        c.put(UnitsLength.ATTOMETER, Mul(Pow(10, 15), Sym("millim")));
        c.put(UnitsLength.CENTIMETER, Mul(Rat(Int(1), Int(10)), Sym("millim")));
        c.put(UnitsLength.DECAMETER, Mul(Rat(Int(1), Pow(10, 4)), Sym("millim")));
        c.put(UnitsLength.DECIMETER, Mul(Rat(Int(1), Int(100)), Sym("millim")));
        c.put(UnitsLength.EXAMETER, Mul(Rat(Int(1), Pow(10, 21)), Sym("millim")));
        c.put(UnitsLength.FEMTOMETER, Mul(Pow(10, 12), Sym("millim")));
        c.put(UnitsLength.FOOT, Mul(Rat(Int(393701), Mul(Int(12), Pow(10, 7))), Sym("millim")));
        c.put(UnitsLength.GIGAMETER, Mul(Rat(Int(1), Pow(10, 12)), Sym("millim")));
        c.put(UnitsLength.HECTOMETER, Mul(Rat(Int(1), Pow(10, 5)), Sym("millim")));
        c.put(UnitsLength.INCH, Mul(Rat(Int(393701), Pow(10, 7)), Sym("millim")));
        c.put(UnitsLength.KILOMETER, Mul(Rat(Int(1), Pow(10, 6)), Sym("millim")));
        c.put(UnitsLength.LIGHTYEAR, Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 5))), Sym("millim")));
        c.put(UnitsLength.LINE, Mul(Rat(Int(1181103), Mul(Int(25), Pow(10, 5))), Sym("millim")));
        c.put(UnitsLength.MEGAMETER, Mul(Rat(Int(1), Pow(10, 9)), Sym("millim")));
        c.put(UnitsLength.METER, Mul(Rat(Int(1), Int(1000)), Sym("millim")));
        c.put(UnitsLength.MICROMETER, Mul(Int(1000), Sym("millim")));
        c.put(UnitsLength.MILE, Mul(Rat(Int(35791), Mul(Int(576), Pow(10, 8))), Sym("millim")));
        c.put(UnitsLength.NANOMETER, Mul(Pow(10, 6), Sym("millim")));
        c.put(UnitsLength.PARSEC, Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 12))), Sym("millim")));
        c.put(UnitsLength.PETAMETER, Mul(Rat(Int(1), Pow(10, 18)), Sym("millim")));
        c.put(UnitsLength.PICOMETER, Mul(Pow(10, 9), Sym("millim")));
        c.put(UnitsLength.POINT, Mul(Rat(Int(3543309), Mul(Int(125), Pow(10, 4))), Sym("millim")));
        c.put(UnitsLength.TERAMETER, Mul(Rat(Int(1), Pow(10, 15)), Sym("millim")));
        c.put(UnitsLength.THOU, Mul(Rat(Int(393701), Pow(10, 10)), Sym("millim")));
        c.put(UnitsLength.YARD, Mul(Rat(Int(393701), Mul(Int(36), Pow(10, 7))), Sym("millim")));
        c.put(UnitsLength.YOCTOMETER, Mul(Pow(10, 21), Sym("millim")));
        c.put(UnitsLength.YOTTAMETER, Mul(Rat(Int(1), Pow(10, 27)), Sym("millim")));
        c.put(UnitsLength.ZEPTOMETER, Mul(Pow(10, 18), Sym("millim")));
        c.put(UnitsLength.ZETTAMETER, Mul(Rat(Int(1), Pow(10, 24)), Sym("millim")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsLength, Conversion> createMapNANOMETER() {
        EnumMap<UnitsLength, Conversion> c =
            new EnumMap<UnitsLength, Conversion>(UnitsLength.class);
        c.put(UnitsLength.ANGSTROM, Mul(Int(10), Sym("nanom")));
        c.put(UnitsLength.ASTRONOMICALUNIT, Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 11))), Sym("nanom")));
        c.put(UnitsLength.ATTOMETER, Mul(Pow(10, 9), Sym("nanom")));
        c.put(UnitsLength.CENTIMETER, Mul(Rat(Int(1), Pow(10, 7)), Sym("nanom")));
        c.put(UnitsLength.DECAMETER, Mul(Rat(Int(1), Pow(10, 10)), Sym("nanom")));
        c.put(UnitsLength.DECIMETER, Mul(Rat(Int(1), Pow(10, 8)), Sym("nanom")));
        c.put(UnitsLength.EXAMETER, Mul(Rat(Int(1), Pow(10, 27)), Sym("nanom")));
        c.put(UnitsLength.FEMTOMETER, Mul(Pow(10, 6), Sym("nanom")));
        c.put(UnitsLength.FOOT, Mul(Rat(Int(393701), Mul(Int(12), Pow(10, 13))), Sym("nanom")));
        c.put(UnitsLength.GIGAMETER, Mul(Rat(Int(1), Pow(10, 18)), Sym("nanom")));
        c.put(UnitsLength.HECTOMETER, Mul(Rat(Int(1), Pow(10, 11)), Sym("nanom")));
        c.put(UnitsLength.INCH, Mul(Rat(Int(393701), Pow(10, 13)), Sym("nanom")));
        c.put(UnitsLength.KILOMETER, Mul(Rat(Int(1), Pow(10, 12)), Sym("nanom")));
        c.put(UnitsLength.LIGHTYEAR, Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 11))), Sym("nanom")));
        c.put(UnitsLength.LINE, Mul(Rat(Int(1181103), Mul(Int(25), Pow(10, 11))), Sym("nanom")));
        c.put(UnitsLength.MEGAMETER, Mul(Rat(Int(1), Pow(10, 15)), Sym("nanom")));
        c.put(UnitsLength.METER, Mul(Rat(Int(1), Pow(10, 9)), Sym("nanom")));
        c.put(UnitsLength.MICROMETER, Mul(Rat(Int(1), Int(1000)), Sym("nanom")));
        c.put(UnitsLength.MILE, Mul(Rat(Int(35791), Mul(Int(576), Pow(10, 14))), Sym("nanom")));
        c.put(UnitsLength.MILLIMETER, Mul(Rat(Int(1), Pow(10, 6)), Sym("nanom")));
        c.put(UnitsLength.PARSEC, Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 18))), Sym("nanom")));
        c.put(UnitsLength.PETAMETER, Mul(Rat(Int(1), Pow(10, 24)), Sym("nanom")));
        c.put(UnitsLength.PICOMETER, Mul(Int(1000), Sym("nanom")));
        c.put(UnitsLength.POINT, Mul(Rat(Int(3543309), Mul(Int(125), Pow(10, 10))), Sym("nanom")));
        c.put(UnitsLength.TERAMETER, Mul(Rat(Int(1), Pow(10, 21)), Sym("nanom")));
        c.put(UnitsLength.THOU, Mul(Rat(Int(393701), Pow(10, 16)), Sym("nanom")));
        c.put(UnitsLength.YARD, Mul(Rat(Int(393701), Mul(Int(36), Pow(10, 13))), Sym("nanom")));
        c.put(UnitsLength.YOCTOMETER, Mul(Pow(10, 15), Sym("nanom")));
        c.put(UnitsLength.YOTTAMETER, Mul(Rat(Int(1), Pow(10, 33)), Sym("nanom")));
        c.put(UnitsLength.ZEPTOMETER, Mul(Pow(10, 12), Sym("nanom")));
        c.put(UnitsLength.ZETTAMETER, Mul(Rat(Int(1), Pow(10, 30)), Sym("nanom")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsLength, Conversion> createMapPARSEC() {
        EnumMap<UnitsLength, Conversion> c =
            new EnumMap<UnitsLength, Conversion>(UnitsLength.class);
        c.put(UnitsLength.ANGSTROM, Mul(Mul(Int(30856776), Pow(10, 19)), Sym("pc")));
        c.put(UnitsLength.ASTRONOMICALUNIT, Mul(Rat(Mul(Int(10285592), Pow(10, 7)), Int(498659569)), Sym("pc")));
        c.put(UnitsLength.ATTOMETER, Mul(Mul(Int(30856776), Pow(10, 27)), Sym("pc")));
        c.put(UnitsLength.CENTIMETER, Mul(Mul(Int(30856776), Pow(10, 11)), Sym("pc")));
        c.put(UnitsLength.DECAMETER, Mul(Mul(Int(30856776), Pow(10, 8)), Sym("pc")));
        c.put(UnitsLength.DECIMETER, Mul(Mul(Int(30856776), Pow(10, 10)), Sym("pc")));
        c.put(UnitsLength.EXAMETER, Mul(Rat(Int(3857097), Mul(Int(125), Pow(10, 6))), Sym("pc")));
        c.put(UnitsLength.FEMTOMETER, Mul(Mul(Int(30856776), Pow(10, 24)), Sym("pc")));
        c.put(UnitsLength.FOOT, Mul(Mul(Int("1012361963998"), Pow(10, 5)), Sym("pc")));
        c.put(UnitsLength.GIGAMETER, Mul(Int(30856776), Sym("pc")));
        c.put(UnitsLength.HECTOMETER, Mul(Mul(Int(30856776), Pow(10, 7)), Sym("pc")));
        c.put(UnitsLength.INCH, Mul(Mul(Int("12148343567976"), Pow(10, 5)), Sym("pc")));
        c.put(UnitsLength.KILOMETER, Mul(Mul(Int(30856776), Pow(10, 6)), Sym("pc")));
        c.put(UnitsLength.LIGHTYEAR, Mul(Rat(Mul(Int(6428495), Pow(10, 6)), Int("1970985515121")), Sym("pc")));
        c.put(UnitsLength.LINE, Mul(Mul(Int("145780122815712"), Pow(10, 5)), Sym("pc")));
        c.put(UnitsLength.MEGAMETER, Mul(Int("30856776000"), Sym("pc")));
        c.put(UnitsLength.METER, Mul(Mul(Int(30856776), Pow(10, 9)), Sym("pc")));
        c.put(UnitsLength.MICROMETER, Mul(Mul(Int(30856776), Pow(10, 15)), Sym("pc")));
        c.put(UnitsLength.MILE, Mul(Rat(Int("57520566136250"), Int(3)), Sym("pc")));
        c.put(UnitsLength.MILLIMETER, Mul(Mul(Int(30856776), Pow(10, 12)), Sym("pc")));
        c.put(UnitsLength.NANOMETER, Mul(Mul(Int(30856776), Pow(10, 18)), Sym("pc")));
        c.put(UnitsLength.PETAMETER, Mul(Rat(Int(3857097), Int(125000)), Sym("pc")));
        c.put(UnitsLength.PICOMETER, Mul(Mul(Int(30856776), Pow(10, 21)), Sym("pc")));
        c.put(UnitsLength.POINT, Mul(Mul(Int("874680736894272"), Pow(10, 5)), Sym("pc")));
        c.put(UnitsLength.TERAMETER, Mul(Rat(Int(3857097), Int(125)), Sym("pc")));
        c.put(UnitsLength.THOU, Mul(Int("1214834356797600"), Sym("pc")));
        c.put(UnitsLength.YARD, Mul(Rat(Mul(Int("1012361963998"), Pow(10, 5)), Int(3)), Sym("pc")));
        c.put(UnitsLength.YOCTOMETER, Mul(Mul(Int(30856776), Pow(10, 33)), Sym("pc")));
        c.put(UnitsLength.YOTTAMETER, Mul(Rat(Int(3857097), Mul(Int(125), Pow(10, 12))), Sym("pc")));
        c.put(UnitsLength.ZEPTOMETER, Mul(Mul(Int(30856776), Pow(10, 30)), Sym("pc")));
        c.put(UnitsLength.ZETTAMETER, Mul(Rat(Int(3857097), Mul(Int(125), Pow(10, 9))), Sym("pc")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsLength, Conversion> createMapPETAMETER() {
        EnumMap<UnitsLength, Conversion> c =
            new EnumMap<UnitsLength, Conversion>(UnitsLength.class);
        c.put(UnitsLength.ANGSTROM, Mul(Pow(10, 25), Sym("petam")));
        c.put(UnitsLength.ASTRONOMICALUNIT, Mul(Rat(Pow(10, 13), Int(1495978707)), Sym("petam")));
        c.put(UnitsLength.ATTOMETER, Mul(Pow(10, 33), Sym("petam")));
        c.put(UnitsLength.CENTIMETER, Mul(Pow(10, 17), Sym("petam")));
        c.put(UnitsLength.DECAMETER, Mul(Pow(10, 14), Sym("petam")));
        c.put(UnitsLength.DECIMETER, Mul(Pow(10, 16), Sym("petam")));
        c.put(UnitsLength.EXAMETER, Mul(Rat(Int(1), Int(1000)), Sym("petam")));
        c.put(UnitsLength.FEMTOMETER, Mul(Pow(10, 30), Sym("petam")));
        c.put(UnitsLength.FOOT, Mul(Rat(Mul(Int(9842525), Pow(10, 9)), Int(3)), Sym("petam")));
        c.put(UnitsLength.GIGAMETER, Mul(Pow(10, 6), Sym("petam")));
        c.put(UnitsLength.HECTOMETER, Mul(Pow(10, 13), Sym("petam")));
        c.put(UnitsLength.INCH, Mul(Mul(Int(393701), Pow(10, 11)), Sym("petam")));
        c.put(UnitsLength.KILOMETER, Mul(Pow(10, 12), Sym("petam")));
        c.put(UnitsLength.LIGHTYEAR, Mul(Rat(Mul(Int(625), Pow(10, 9)), Int("5912956545363")), Sym("petam")));
        c.put(UnitsLength.LINE, Mul(Mul(Int(4724412), Pow(10, 11)), Sym("petam")));
        c.put(UnitsLength.MEGAMETER, Mul(Pow(10, 9), Sym("petam")));
        c.put(UnitsLength.METER, Mul(Pow(10, 15), Sym("petam")));
        c.put(UnitsLength.MICROMETER, Mul(Pow(10, 21), Sym("petam")));
        c.put(UnitsLength.MILE, Mul(Rat(Mul(Int(559234375), Pow(10, 4)), Int(9)), Sym("petam")));
        c.put(UnitsLength.MILLIMETER, Mul(Pow(10, 18), Sym("petam")));
        c.put(UnitsLength.NANOMETER, Mul(Pow(10, 24), Sym("petam")));
        c.put(UnitsLength.PARSEC, Mul(Rat(Int(125000), Int(3857097)), Sym("petam")));
        c.put(UnitsLength.PICOMETER, Mul(Pow(10, 27), Sym("petam")));
        c.put(UnitsLength.POINT, Mul(Mul(Int(28346472), Pow(10, 11)), Sym("petam")));
        c.put(UnitsLength.TERAMETER, Mul(Int(1000), Sym("petam")));
        c.put(UnitsLength.THOU, Mul(Mul(Int(393701), Pow(10, 8)), Sym("petam")));
        c.put(UnitsLength.YARD, Mul(Rat(Mul(Int(9842525), Pow(10, 9)), Int(9)), Sym("petam")));
        c.put(UnitsLength.YOCTOMETER, Mul(Pow(10, 39), Sym("petam")));
        c.put(UnitsLength.YOTTAMETER, Mul(Rat(Int(1), Pow(10, 9)), Sym("petam")));
        c.put(UnitsLength.ZEPTOMETER, Mul(Pow(10, 36), Sym("petam")));
        c.put(UnitsLength.ZETTAMETER, Mul(Rat(Int(1), Pow(10, 6)), Sym("petam")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsLength, Conversion> createMapPICOMETER() {
        EnumMap<UnitsLength, Conversion> c =
            new EnumMap<UnitsLength, Conversion>(UnitsLength.class);
        c.put(UnitsLength.ANGSTROM, Mul(Rat(Int(1), Int(100)), Sym("picom")));
        c.put(UnitsLength.ASTRONOMICALUNIT, Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 14))), Sym("picom")));
        c.put(UnitsLength.ATTOMETER, Mul(Pow(10, 6), Sym("picom")));
        c.put(UnitsLength.CENTIMETER, Mul(Rat(Int(1), Pow(10, 10)), Sym("picom")));
        c.put(UnitsLength.DECAMETER, Mul(Rat(Int(1), Pow(10, 13)), Sym("picom")));
        c.put(UnitsLength.DECIMETER, Mul(Rat(Int(1), Pow(10, 11)), Sym("picom")));
        c.put(UnitsLength.EXAMETER, Mul(Rat(Int(1), Pow(10, 30)), Sym("picom")));
        c.put(UnitsLength.FEMTOMETER, Mul(Int(1000), Sym("picom")));
        c.put(UnitsLength.FOOT, Mul(Rat(Int(393701), Mul(Int(12), Pow(10, 16))), Sym("picom")));
        c.put(UnitsLength.GIGAMETER, Mul(Rat(Int(1), Pow(10, 21)), Sym("picom")));
        c.put(UnitsLength.HECTOMETER, Mul(Rat(Int(1), Pow(10, 14)), Sym("picom")));
        c.put(UnitsLength.INCH, Mul(Rat(Int(393701), Pow(10, 16)), Sym("picom")));
        c.put(UnitsLength.KILOMETER, Mul(Rat(Int(1), Pow(10, 15)), Sym("picom")));
        c.put(UnitsLength.LIGHTYEAR, Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 14))), Sym("picom")));
        c.put(UnitsLength.LINE, Mul(Rat(Int(1181103), Mul(Int(25), Pow(10, 14))), Sym("picom")));
        c.put(UnitsLength.MEGAMETER, Mul(Rat(Int(1), Pow(10, 18)), Sym("picom")));
        c.put(UnitsLength.METER, Mul(Rat(Int(1), Pow(10, 12)), Sym("picom")));
        c.put(UnitsLength.MICROMETER, Mul(Rat(Int(1), Pow(10, 6)), Sym("picom")));
        c.put(UnitsLength.MILE, Mul(Rat(Int(35791), Mul(Int(576), Pow(10, 17))), Sym("picom")));
        c.put(UnitsLength.MILLIMETER, Mul(Rat(Int(1), Pow(10, 9)), Sym("picom")));
        c.put(UnitsLength.NANOMETER, Mul(Rat(Int(1), Int(1000)), Sym("picom")));
        c.put(UnitsLength.PARSEC, Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 21))), Sym("picom")));
        c.put(UnitsLength.PETAMETER, Mul(Rat(Int(1), Pow(10, 27)), Sym("picom")));
        c.put(UnitsLength.POINT, Mul(Rat(Int(3543309), Mul(Int(125), Pow(10, 13))), Sym("picom")));
        c.put(UnitsLength.TERAMETER, Mul(Rat(Int(1), Pow(10, 24)), Sym("picom")));
        c.put(UnitsLength.THOU, Mul(Rat(Int(393701), Pow(10, 19)), Sym("picom")));
        c.put(UnitsLength.YARD, Mul(Rat(Int(393701), Mul(Int(36), Pow(10, 16))), Sym("picom")));
        c.put(UnitsLength.YOCTOMETER, Mul(Pow(10, 12), Sym("picom")));
        c.put(UnitsLength.YOTTAMETER, Mul(Rat(Int(1), Pow(10, 36)), Sym("picom")));
        c.put(UnitsLength.ZEPTOMETER, Mul(Pow(10, 9), Sym("picom")));
        c.put(UnitsLength.ZETTAMETER, Mul(Rat(Int(1), Pow(10, 33)), Sym("picom")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsLength, Conversion> createMapPOINT() {
        EnumMap<UnitsLength, Conversion> c =
            new EnumMap<UnitsLength, Conversion>(UnitsLength.class);
        c.put(UnitsLength.ANGSTROM, Mul(Rat(Mul(Int(125), Pow(10, 11)), Int(3543309)), Sym("pt")));
        c.put(UnitsLength.ASTRONOMICALUNIT, Mul(Rat(Int(25), Int("10601429632642926")), Sym("pt")));
        c.put(UnitsLength.ATTOMETER, Mul(Rat(Mul(Int(125), Pow(10, 19)), Int(3543309)), Sym("pt")));
        c.put(UnitsLength.CENTIMETER, Mul(Rat(Int(125000), Int(3543309)), Sym("pt")));
        c.put(UnitsLength.DECAMETER, Mul(Rat(Int(125), Int(3543309)), Sym("pt")));
        c.put(UnitsLength.DECIMETER, Mul(Rat(Int(12500), Int(3543309)), Sym("pt")));
        c.put(UnitsLength.EXAMETER, Mul(Rat(Int(1), Mul(Int(28346472), Pow(10, 14))), Sym("pt")));
        c.put(UnitsLength.FEMTOMETER, Mul(Rat(Mul(Int(125), Pow(10, 16)), Int(3543309)), Sym("pt")));
        c.put(UnitsLength.FOOT, Mul(Rat(Int(1), Int(864)), Sym("pt")));
        c.put(UnitsLength.GIGAMETER, Mul(Rat(Int(1), Mul(Int(28346472), Pow(10, 5))), Sym("pt")));
        c.put(UnitsLength.HECTOMETER, Mul(Rat(Int(25), Int(7086618)), Sym("pt")));
        c.put(UnitsLength.INCH, Mul(Rat(Int(1), Int(72)), Sym("pt")));
        c.put(UnitsLength.KILOMETER, Mul(Rat(Int(5), Int(14173236)), Sym("pt")));
        c.put(UnitsLength.LIGHTYEAR, Mul(Rat(Int(25), Int("670445828601396037344")), Sym("pt")));
        c.put(UnitsLength.LINE, Mul(Rat(Int(1), Int(6)), Sym("pt")));
        c.put(UnitsLength.MEGAMETER, Mul(Rat(Int(1), Int("2834647200")), Sym("pt")));
        c.put(UnitsLength.METER, Mul(Rat(Int(1250), Int(3543309)), Sym("pt")));
        c.put(UnitsLength.MICROMETER, Mul(Rat(Mul(Int(125), Pow(10, 7)), Int(3543309)), Sym("pt")));
        c.put(UnitsLength.MILE, Mul(Rat(Int(1), Int(4561920)), Sym("pt")));
        c.put(UnitsLength.MILLIMETER, Mul(Rat(Mul(Int(125), Pow(10, 4)), Int(3543309)), Sym("pt")));
        c.put(UnitsLength.NANOMETER, Mul(Rat(Mul(Int(125), Pow(10, 10)), Int(3543309)), Sym("pt")));
        c.put(UnitsLength.PARSEC, Mul(Rat(Int(1), Mul(Int("874680736894272"), Pow(10, 5))), Sym("pt")));
        c.put(UnitsLength.PETAMETER, Mul(Rat(Int(1), Mul(Int(28346472), Pow(10, 11))), Sym("pt")));
        c.put(UnitsLength.PICOMETER, Mul(Rat(Mul(Int(125), Pow(10, 13)), Int(3543309)), Sym("pt")));
        c.put(UnitsLength.TERAMETER, Mul(Rat(Int(1), Mul(Int(28346472), Pow(10, 8))), Sym("pt")));
        c.put(UnitsLength.THOU, Mul(Rat(Int(1), Int(72000)), Sym("pt")));
        c.put(UnitsLength.YARD, Mul(Rat(Int(1), Int(2592)), Sym("pt")));
        c.put(UnitsLength.YOCTOMETER, Mul(Rat(Mul(Int(125), Pow(10, 25)), Int(3543309)), Sym("pt")));
        c.put(UnitsLength.YOTTAMETER, Mul(Rat(Int(1), Mul(Int(28346472), Pow(10, 20))), Sym("pt")));
        c.put(UnitsLength.ZEPTOMETER, Mul(Rat(Mul(Int(125), Pow(10, 22)), Int(3543309)), Sym("pt")));
        c.put(UnitsLength.ZETTAMETER, Mul(Rat(Int(1), Mul(Int(28346472), Pow(10, 17))), Sym("pt")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsLength, Conversion> createMapTERAMETER() {
        EnumMap<UnitsLength, Conversion> c =
            new EnumMap<UnitsLength, Conversion>(UnitsLength.class);
        c.put(UnitsLength.ANGSTROM, Mul(Pow(10, 22), Sym("teram")));
        c.put(UnitsLength.ASTRONOMICALUNIT, Mul(Rat(Pow(10, 10), Int(1495978707)), Sym("teram")));
        c.put(UnitsLength.ATTOMETER, Mul(Pow(10, 30), Sym("teram")));
        c.put(UnitsLength.CENTIMETER, Mul(Pow(10, 14), Sym("teram")));
        c.put(UnitsLength.DECAMETER, Mul(Pow(10, 11), Sym("teram")));
        c.put(UnitsLength.DECIMETER, Mul(Pow(10, 13), Sym("teram")));
        c.put(UnitsLength.EXAMETER, Mul(Rat(Int(1), Pow(10, 6)), Sym("teram")));
        c.put(UnitsLength.FEMTOMETER, Mul(Pow(10, 27), Sym("teram")));
        c.put(UnitsLength.FOOT, Mul(Rat(Mul(Int(9842525), Pow(10, 6)), Int(3)), Sym("teram")));
        c.put(UnitsLength.GIGAMETER, Mul(Int(1000), Sym("teram")));
        c.put(UnitsLength.HECTOMETER, Mul(Pow(10, 10), Sym("teram")));
        c.put(UnitsLength.INCH, Mul(Mul(Int(393701), Pow(10, 8)), Sym("teram")));
        c.put(UnitsLength.KILOMETER, Mul(Pow(10, 9), Sym("teram")));
        c.put(UnitsLength.LIGHTYEAR, Mul(Rat(Mul(Int(625), Pow(10, 6)), Int("5912956545363")), Sym("teram")));
        c.put(UnitsLength.LINE, Mul(Mul(Int(4724412), Pow(10, 8)), Sym("teram")));
        c.put(UnitsLength.MEGAMETER, Mul(Pow(10, 6), Sym("teram")));
        c.put(UnitsLength.METER, Mul(Pow(10, 12), Sym("teram")));
        c.put(UnitsLength.MICROMETER, Mul(Pow(10, 18), Sym("teram")));
        c.put(UnitsLength.MILE, Mul(Rat(Int("5592343750"), Int(9)), Sym("teram")));
        c.put(UnitsLength.MILLIMETER, Mul(Pow(10, 15), Sym("teram")));
        c.put(UnitsLength.NANOMETER, Mul(Pow(10, 21), Sym("teram")));
        c.put(UnitsLength.PARSEC, Mul(Rat(Int(125), Int(3857097)), Sym("teram")));
        c.put(UnitsLength.PETAMETER, Mul(Rat(Int(1), Int(1000)), Sym("teram")));
        c.put(UnitsLength.PICOMETER, Mul(Pow(10, 24), Sym("teram")));
        c.put(UnitsLength.POINT, Mul(Mul(Int(28346472), Pow(10, 8)), Sym("teram")));
        c.put(UnitsLength.THOU, Mul(Mul(Int(393701), Pow(10, 5)), Sym("teram")));
        c.put(UnitsLength.YARD, Mul(Rat(Mul(Int(9842525), Pow(10, 6)), Int(9)), Sym("teram")));
        c.put(UnitsLength.YOCTOMETER, Mul(Pow(10, 36), Sym("teram")));
        c.put(UnitsLength.YOTTAMETER, Mul(Rat(Int(1), Pow(10, 12)), Sym("teram")));
        c.put(UnitsLength.ZEPTOMETER, Mul(Pow(10, 33), Sym("teram")));
        c.put(UnitsLength.ZETTAMETER, Mul(Rat(Int(1), Pow(10, 9)), Sym("teram")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsLength, Conversion> createMapTHOU() {
        EnumMap<UnitsLength, Conversion> c =
            new EnumMap<UnitsLength, Conversion>(UnitsLength.class);
        c.put(UnitsLength.ANGSTROM, Mul(Rat(Pow(10, 17), Int(393701)), Sym("thou")));
        c.put(UnitsLength.ASTRONOMICALUNIT, Mul(Rat(Pow(10, 5), Int("588968312924607")), Sym("thou")));
        c.put(UnitsLength.ATTOMETER, Mul(Rat(Pow(10, 25), Int(393701)), Sym("thou")));
        c.put(UnitsLength.CENTIMETER, Mul(Rat(Pow(10, 9), Int(393701)), Sym("thou")));
        c.put(UnitsLength.DECAMETER, Mul(Rat(Pow(10, 6), Int(393701)), Sym("thou")));
        c.put(UnitsLength.DECIMETER, Mul(Rat(Pow(10, 8), Int(393701)), Sym("thou")));
        c.put(UnitsLength.EXAMETER, Mul(Rat(Int(1), Mul(Int(393701), Pow(10, 11))), Sym("thou")));
        c.put(UnitsLength.FEMTOMETER, Mul(Rat(Pow(10, 22), Int(393701)), Sym("thou")));
        c.put(UnitsLength.FOOT, Mul(Rat(Int(250), Int(3)), Sym("thou")));
        c.put(UnitsLength.GIGAMETER, Mul(Rat(Int(1), Int(39370100)), Sym("thou")));
        c.put(UnitsLength.HECTOMETER, Mul(Rat(Pow(10, 5), Int(393701)), Sym("thou")));
        c.put(UnitsLength.INCH, Mul(Int(1000), Sym("thou")));
        c.put(UnitsLength.KILOMETER, Mul(Rat(Pow(10, 4), Int(393701)), Sym("thou")));
        c.put(UnitsLength.LIGHTYEAR, Mul(Rat(Int(6250), Int("2327936904865958463")), Sym("thou")));
        c.put(UnitsLength.LINE, Mul(Int(12000), Sym("thou")));
        c.put(UnitsLength.MEGAMETER, Mul(Rat(Int(10), Int(393701)), Sym("thou")));
        c.put(UnitsLength.METER, Mul(Rat(Pow(10, 7), Int(393701)), Sym("thou")));
        c.put(UnitsLength.MICROMETER, Mul(Rat(Pow(10, 13), Int(393701)), Sym("thou")));
        c.put(UnitsLength.MILE, Mul(Rat(Int(25), Int(1584)), Sym("thou")));
        c.put(UnitsLength.MILLIMETER, Mul(Rat(Pow(10, 10), Int(393701)), Sym("thou")));
        c.put(UnitsLength.NANOMETER, Mul(Rat(Pow(10, 16), Int(393701)), Sym("thou")));
        c.put(UnitsLength.PARSEC, Mul(Rat(Int(1), Int("1214834356797600")), Sym("thou")));
        c.put(UnitsLength.PETAMETER, Mul(Rat(Int(1), Mul(Int(393701), Pow(10, 8))), Sym("thou")));
        c.put(UnitsLength.PICOMETER, Mul(Rat(Pow(10, 19), Int(393701)), Sym("thou")));
        c.put(UnitsLength.POINT, Mul(Int(72000), Sym("thou")));
        c.put(UnitsLength.TERAMETER, Mul(Rat(Int(1), Mul(Int(393701), Pow(10, 5))), Sym("thou")));
        c.put(UnitsLength.YARD, Mul(Rat(Int(250), Int(9)), Sym("thou")));
        c.put(UnitsLength.YOCTOMETER, Mul(Rat(Pow(10, 31), Int(393701)), Sym("thou")));
        c.put(UnitsLength.YOTTAMETER, Mul(Rat(Int(1), Mul(Int(393701), Pow(10, 17))), Sym("thou")));
        c.put(UnitsLength.ZEPTOMETER, Mul(Rat(Pow(10, 28), Int(393701)), Sym("thou")));
        c.put(UnitsLength.ZETTAMETER, Mul(Rat(Int(1), Mul(Int(393701), Pow(10, 14))), Sym("thou")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsLength, Conversion> createMapYARD() {
        EnumMap<UnitsLength, Conversion> c =
            new EnumMap<UnitsLength, Conversion>(UnitsLength.class);
        c.put(UnitsLength.ANGSTROM, Mul(Rat(Mul(Int(36), Pow(10, 14)), Int(393701)), Sym("yd")));
        c.put(UnitsLength.ASTRONOMICALUNIT, Mul(Rat(Int(1200), Int("196322770974869")), Sym("yd")));
        c.put(UnitsLength.ATTOMETER, Mul(Rat(Mul(Int(36), Pow(10, 22)), Int(393701)), Sym("yd")));
        c.put(UnitsLength.CENTIMETER, Mul(Rat(Mul(Int(36), Pow(10, 6)), Int(393701)), Sym("yd")));
        c.put(UnitsLength.DECAMETER, Mul(Rat(Int(36000), Int(393701)), Sym("yd")));
        c.put(UnitsLength.DECIMETER, Mul(Rat(Mul(Int(36), Pow(10, 5)), Int(393701)), Sym("yd")));
        c.put(UnitsLength.EXAMETER, Mul(Rat(Int(9), Mul(Int(9842525), Pow(10, 12))), Sym("yd")));
        c.put(UnitsLength.FEMTOMETER, Mul(Rat(Mul(Int(36), Pow(10, 19)), Int(393701)), Sym("yd")));
        c.put(UnitsLength.FOOT, Mul(Int(3), Sym("yd")));
        c.put(UnitsLength.GIGAMETER, Mul(Rat(Int(9), Int("9842525000")), Sym("yd")));
        c.put(UnitsLength.HECTOMETER, Mul(Rat(Int(3600), Int(393701)), Sym("yd")));
        c.put(UnitsLength.INCH, Mul(Int(36), Sym("yd")));
        c.put(UnitsLength.KILOMETER, Mul(Rat(Int(360), Int(393701)), Sym("yd")));
        c.put(UnitsLength.LIGHTYEAR, Mul(Rat(Int(25), Int("258659656096217607")), Sym("yd")));
        c.put(UnitsLength.LINE, Mul(Int(432), Sym("yd")));
        c.put(UnitsLength.MEGAMETER, Mul(Rat(Int(9), Int(9842525)), Sym("yd")));
        c.put(UnitsLength.METER, Mul(Rat(Mul(Int(36), Pow(10, 4)), Int(393701)), Sym("yd")));
        c.put(UnitsLength.MICROMETER, Mul(Rat(Mul(Int(36), Pow(10, 10)), Int(393701)), Sym("yd")));
        c.put(UnitsLength.MILE, Mul(Rat(Int(1), Int(1760)), Sym("yd")));
        c.put(UnitsLength.MILLIMETER, Mul(Rat(Mul(Int(36), Pow(10, 7)), Int(393701)), Sym("yd")));
        c.put(UnitsLength.NANOMETER, Mul(Rat(Mul(Int(36), Pow(10, 13)), Int(393701)), Sym("yd")));
        c.put(UnitsLength.PARSEC, Mul(Rat(Int(3), Mul(Int("1012361963998"), Pow(10, 5))), Sym("yd")));
        c.put(UnitsLength.PETAMETER, Mul(Rat(Int(9), Mul(Int(9842525), Pow(10, 9))), Sym("yd")));
        c.put(UnitsLength.PICOMETER, Mul(Rat(Mul(Int(36), Pow(10, 16)), Int(393701)), Sym("yd")));
        c.put(UnitsLength.POINT, Mul(Int(2592), Sym("yd")));
        c.put(UnitsLength.TERAMETER, Mul(Rat(Int(9), Mul(Int(9842525), Pow(10, 6))), Sym("yd")));
        c.put(UnitsLength.THOU, Mul(Rat(Int(9), Int(250)), Sym("yd")));
        c.put(UnitsLength.YOCTOMETER, Mul(Rat(Mul(Int(36), Pow(10, 28)), Int(393701)), Sym("yd")));
        c.put(UnitsLength.YOTTAMETER, Mul(Rat(Int(9), Mul(Int(9842525), Pow(10, 18))), Sym("yd")));
        c.put(UnitsLength.ZEPTOMETER, Mul(Rat(Mul(Int(36), Pow(10, 25)), Int(393701)), Sym("yd")));
        c.put(UnitsLength.ZETTAMETER, Mul(Rat(Int(9), Mul(Int(9842525), Pow(10, 15))), Sym("yd")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsLength, Conversion> createMapYOCTOMETER() {
        EnumMap<UnitsLength, Conversion> c =
            new EnumMap<UnitsLength, Conversion>(UnitsLength.class);
        c.put(UnitsLength.ANGSTROM, Mul(Rat(Int(1), Pow(10, 14)), Sym("yoctom")));
        c.put(UnitsLength.ASTRONOMICALUNIT, Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 26))), Sym("yoctom")));
        c.put(UnitsLength.ATTOMETER, Mul(Rat(Int(1), Pow(10, 6)), Sym("yoctom")));
        c.put(UnitsLength.CENTIMETER, Mul(Rat(Int(1), Pow(10, 22)), Sym("yoctom")));
        c.put(UnitsLength.DECAMETER, Mul(Rat(Int(1), Pow(10, 25)), Sym("yoctom")));
        c.put(UnitsLength.DECIMETER, Mul(Rat(Int(1), Pow(10, 23)), Sym("yoctom")));
        c.put(UnitsLength.EXAMETER, Mul(Rat(Int(1), Pow(10, 42)), Sym("yoctom")));
        c.put(UnitsLength.FEMTOMETER, Mul(Rat(Int(1), Pow(10, 9)), Sym("yoctom")));
        c.put(UnitsLength.FOOT, Mul(Rat(Int(393701), Mul(Int(12), Pow(10, 28))), Sym("yoctom")));
        c.put(UnitsLength.GIGAMETER, Mul(Rat(Int(1), Pow(10, 33)), Sym("yoctom")));
        c.put(UnitsLength.HECTOMETER, Mul(Rat(Int(1), Pow(10, 26)), Sym("yoctom")));
        c.put(UnitsLength.INCH, Mul(Rat(Int(393701), Pow(10, 28)), Sym("yoctom")));
        c.put(UnitsLength.KILOMETER, Mul(Rat(Int(1), Pow(10, 27)), Sym("yoctom")));
        c.put(UnitsLength.LIGHTYEAR, Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 26))), Sym("yoctom")));
        c.put(UnitsLength.LINE, Mul(Rat(Int(1181103), Mul(Int(25), Pow(10, 26))), Sym("yoctom")));
        c.put(UnitsLength.MEGAMETER, Mul(Rat(Int(1), Pow(10, 30)), Sym("yoctom")));
        c.put(UnitsLength.METER, Mul(Rat(Int(1), Pow(10, 24)), Sym("yoctom")));
        c.put(UnitsLength.MICROMETER, Mul(Rat(Int(1), Pow(10, 18)), Sym("yoctom")));
        c.put(UnitsLength.MILE, Mul(Rat(Int(35791), Mul(Int(576), Pow(10, 29))), Sym("yoctom")));
        c.put(UnitsLength.MILLIMETER, Mul(Rat(Int(1), Pow(10, 21)), Sym("yoctom")));
        c.put(UnitsLength.NANOMETER, Mul(Rat(Int(1), Pow(10, 15)), Sym("yoctom")));
        c.put(UnitsLength.PARSEC, Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 33))), Sym("yoctom")));
        c.put(UnitsLength.PETAMETER, Mul(Rat(Int(1), Pow(10, 39)), Sym("yoctom")));
        c.put(UnitsLength.PICOMETER, Mul(Rat(Int(1), Pow(10, 12)), Sym("yoctom")));
        c.put(UnitsLength.POINT, Mul(Rat(Int(3543309), Mul(Int(125), Pow(10, 25))), Sym("yoctom")));
        c.put(UnitsLength.TERAMETER, Mul(Rat(Int(1), Pow(10, 36)), Sym("yoctom")));
        c.put(UnitsLength.THOU, Mul(Rat(Int(393701), Pow(10, 31)), Sym("yoctom")));
        c.put(UnitsLength.YARD, Mul(Rat(Int(393701), Mul(Int(36), Pow(10, 28))), Sym("yoctom")));
        c.put(UnitsLength.YOTTAMETER, Mul(Rat(Int(1), Pow(10, 48)), Sym("yoctom")));
        c.put(UnitsLength.ZEPTOMETER, Mul(Rat(Int(1), Int(1000)), Sym("yoctom")));
        c.put(UnitsLength.ZETTAMETER, Mul(Rat(Int(1), Pow(10, 45)), Sym("yoctom")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsLength, Conversion> createMapYOTTAMETER() {
        EnumMap<UnitsLength, Conversion> c =
            new EnumMap<UnitsLength, Conversion>(UnitsLength.class);
        c.put(UnitsLength.ANGSTROM, Mul(Pow(10, 34), Sym("yottam")));
        c.put(UnitsLength.ASTRONOMICALUNIT, Mul(Rat(Pow(10, 22), Int(1495978707)), Sym("yottam")));
        c.put(UnitsLength.ATTOMETER, Mul(Pow(10, 42), Sym("yottam")));
        c.put(UnitsLength.CENTIMETER, Mul(Pow(10, 26), Sym("yottam")));
        c.put(UnitsLength.DECAMETER, Mul(Pow(10, 23), Sym("yottam")));
        c.put(UnitsLength.DECIMETER, Mul(Pow(10, 25), Sym("yottam")));
        c.put(UnitsLength.EXAMETER, Mul(Pow(10, 6), Sym("yottam")));
        c.put(UnitsLength.FEMTOMETER, Mul(Pow(10, 39), Sym("yottam")));
        c.put(UnitsLength.FOOT, Mul(Rat(Mul(Int(9842525), Pow(10, 18)), Int(3)), Sym("yottam")));
        c.put(UnitsLength.GIGAMETER, Mul(Pow(10, 15), Sym("yottam")));
        c.put(UnitsLength.HECTOMETER, Mul(Pow(10, 22), Sym("yottam")));
        c.put(UnitsLength.INCH, Mul(Mul(Int(393701), Pow(10, 20)), Sym("yottam")));
        c.put(UnitsLength.KILOMETER, Mul(Pow(10, 21), Sym("yottam")));
        c.put(UnitsLength.LIGHTYEAR, Mul(Rat(Mul(Int(625), Pow(10, 18)), Int("5912956545363")), Sym("yottam")));
        c.put(UnitsLength.LINE, Mul(Mul(Int(4724412), Pow(10, 20)), Sym("yottam")));
        c.put(UnitsLength.MEGAMETER, Mul(Pow(10, 18), Sym("yottam")));
        c.put(UnitsLength.METER, Mul(Pow(10, 24), Sym("yottam")));
        c.put(UnitsLength.MICROMETER, Mul(Pow(10, 30), Sym("yottam")));
        c.put(UnitsLength.MILE, Mul(Rat(Mul(Int(559234375), Pow(10, 13)), Int(9)), Sym("yottam")));
        c.put(UnitsLength.MILLIMETER, Mul(Pow(10, 27), Sym("yottam")));
        c.put(UnitsLength.NANOMETER, Mul(Pow(10, 33), Sym("yottam")));
        c.put(UnitsLength.PARSEC, Mul(Rat(Mul(Int(125), Pow(10, 12)), Int(3857097)), Sym("yottam")));
        c.put(UnitsLength.PETAMETER, Mul(Pow(10, 9), Sym("yottam")));
        c.put(UnitsLength.PICOMETER, Mul(Pow(10, 36), Sym("yottam")));
        c.put(UnitsLength.POINT, Mul(Mul(Int(28346472), Pow(10, 20)), Sym("yottam")));
        c.put(UnitsLength.TERAMETER, Mul(Pow(10, 12), Sym("yottam")));
        c.put(UnitsLength.THOU, Mul(Mul(Int(393701), Pow(10, 17)), Sym("yottam")));
        c.put(UnitsLength.YARD, Mul(Rat(Mul(Int(9842525), Pow(10, 18)), Int(9)), Sym("yottam")));
        c.put(UnitsLength.YOCTOMETER, Mul(Pow(10, 48), Sym("yottam")));
        c.put(UnitsLength.ZEPTOMETER, Mul(Pow(10, 45), Sym("yottam")));
        c.put(UnitsLength.ZETTAMETER, Mul(Int(1000), Sym("yottam")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsLength, Conversion> createMapZEPTOMETER() {
        EnumMap<UnitsLength, Conversion> c =
            new EnumMap<UnitsLength, Conversion>(UnitsLength.class);
        c.put(UnitsLength.ANGSTROM, Mul(Rat(Int(1), Pow(10, 11)), Sym("zeptom")));
        c.put(UnitsLength.ASTRONOMICALUNIT, Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 23))), Sym("zeptom")));
        c.put(UnitsLength.ATTOMETER, Mul(Rat(Int(1), Int(1000)), Sym("zeptom")));
        c.put(UnitsLength.CENTIMETER, Mul(Rat(Int(1), Pow(10, 19)), Sym("zeptom")));
        c.put(UnitsLength.DECAMETER, Mul(Rat(Int(1), Pow(10, 22)), Sym("zeptom")));
        c.put(UnitsLength.DECIMETER, Mul(Rat(Int(1), Pow(10, 20)), Sym("zeptom")));
        c.put(UnitsLength.EXAMETER, Mul(Rat(Int(1), Pow(10, 39)), Sym("zeptom")));
        c.put(UnitsLength.FEMTOMETER, Mul(Rat(Int(1), Pow(10, 6)), Sym("zeptom")));
        c.put(UnitsLength.FOOT, Mul(Rat(Int(393701), Mul(Int(12), Pow(10, 25))), Sym("zeptom")));
        c.put(UnitsLength.GIGAMETER, Mul(Rat(Int(1), Pow(10, 30)), Sym("zeptom")));
        c.put(UnitsLength.HECTOMETER, Mul(Rat(Int(1), Pow(10, 23)), Sym("zeptom")));
        c.put(UnitsLength.INCH, Mul(Rat(Int(393701), Pow(10, 25)), Sym("zeptom")));
        c.put(UnitsLength.KILOMETER, Mul(Rat(Int(1), Pow(10, 24)), Sym("zeptom")));
        c.put(UnitsLength.LIGHTYEAR, Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 23))), Sym("zeptom")));
        c.put(UnitsLength.LINE, Mul(Rat(Int(1181103), Mul(Int(25), Pow(10, 23))), Sym("zeptom")));
        c.put(UnitsLength.MEGAMETER, Mul(Rat(Int(1), Pow(10, 27)), Sym("zeptom")));
        c.put(UnitsLength.METER, Mul(Rat(Int(1), Pow(10, 21)), Sym("zeptom")));
        c.put(UnitsLength.MICROMETER, Mul(Rat(Int(1), Pow(10, 15)), Sym("zeptom")));
        c.put(UnitsLength.MILE, Mul(Rat(Int(35791), Mul(Int(576), Pow(10, 26))), Sym("zeptom")));
        c.put(UnitsLength.MILLIMETER, Mul(Rat(Int(1), Pow(10, 18)), Sym("zeptom")));
        c.put(UnitsLength.NANOMETER, Mul(Rat(Int(1), Pow(10, 12)), Sym("zeptom")));
        c.put(UnitsLength.PARSEC, Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 30))), Sym("zeptom")));
        c.put(UnitsLength.PETAMETER, Mul(Rat(Int(1), Pow(10, 36)), Sym("zeptom")));
        c.put(UnitsLength.PICOMETER, Mul(Rat(Int(1), Pow(10, 9)), Sym("zeptom")));
        c.put(UnitsLength.POINT, Mul(Rat(Int(3543309), Mul(Int(125), Pow(10, 22))), Sym("zeptom")));
        c.put(UnitsLength.TERAMETER, Mul(Rat(Int(1), Pow(10, 33)), Sym("zeptom")));
        c.put(UnitsLength.THOU, Mul(Rat(Int(393701), Pow(10, 28)), Sym("zeptom")));
        c.put(UnitsLength.YARD, Mul(Rat(Int(393701), Mul(Int(36), Pow(10, 25))), Sym("zeptom")));
        c.put(UnitsLength.YOCTOMETER, Mul(Int(1000), Sym("zeptom")));
        c.put(UnitsLength.YOTTAMETER, Mul(Rat(Int(1), Pow(10, 45)), Sym("zeptom")));
        c.put(UnitsLength.ZETTAMETER, Mul(Rat(Int(1), Pow(10, 42)), Sym("zeptom")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsLength, Conversion> createMapZETTAMETER() {
        EnumMap<UnitsLength, Conversion> c =
            new EnumMap<UnitsLength, Conversion>(UnitsLength.class);
        c.put(UnitsLength.ANGSTROM, Mul(Pow(10, 31), Sym("zettam")));
        c.put(UnitsLength.ASTRONOMICALUNIT, Mul(Rat(Pow(10, 19), Int(1495978707)), Sym("zettam")));
        c.put(UnitsLength.ATTOMETER, Mul(Pow(10, 39), Sym("zettam")));
        c.put(UnitsLength.CENTIMETER, Mul(Pow(10, 23), Sym("zettam")));
        c.put(UnitsLength.DECAMETER, Mul(Pow(10, 20), Sym("zettam")));
        c.put(UnitsLength.DECIMETER, Mul(Pow(10, 22), Sym("zettam")));
        c.put(UnitsLength.EXAMETER, Mul(Int(1000), Sym("zettam")));
        c.put(UnitsLength.FEMTOMETER, Mul(Pow(10, 36), Sym("zettam")));
        c.put(UnitsLength.FOOT, Mul(Rat(Mul(Int(9842525), Pow(10, 15)), Int(3)), Sym("zettam")));
        c.put(UnitsLength.GIGAMETER, Mul(Pow(10, 12), Sym("zettam")));
        c.put(UnitsLength.HECTOMETER, Mul(Pow(10, 19), Sym("zettam")));
        c.put(UnitsLength.INCH, Mul(Mul(Int(393701), Pow(10, 17)), Sym("zettam")));
        c.put(UnitsLength.KILOMETER, Mul(Pow(10, 18), Sym("zettam")));
        c.put(UnitsLength.LIGHTYEAR, Mul(Rat(Mul(Int(625), Pow(10, 15)), Int("5912956545363")), Sym("zettam")));
        c.put(UnitsLength.LINE, Mul(Mul(Int(4724412), Pow(10, 17)), Sym("zettam")));
        c.put(UnitsLength.MEGAMETER, Mul(Pow(10, 15), Sym("zettam")));
        c.put(UnitsLength.METER, Mul(Pow(10, 21), Sym("zettam")));
        c.put(UnitsLength.MICROMETER, Mul(Pow(10, 27), Sym("zettam")));
        c.put(UnitsLength.MILE, Mul(Rat(Mul(Int(559234375), Pow(10, 10)), Int(9)), Sym("zettam")));
        c.put(UnitsLength.MILLIMETER, Mul(Pow(10, 24), Sym("zettam")));
        c.put(UnitsLength.NANOMETER, Mul(Pow(10, 30), Sym("zettam")));
        c.put(UnitsLength.PARSEC, Mul(Rat(Mul(Int(125), Pow(10, 9)), Int(3857097)), Sym("zettam")));
        c.put(UnitsLength.PETAMETER, Mul(Pow(10, 6), Sym("zettam")));
        c.put(UnitsLength.PICOMETER, Mul(Pow(10, 33), Sym("zettam")));
        c.put(UnitsLength.POINT, Mul(Mul(Int(28346472), Pow(10, 17)), Sym("zettam")));
        c.put(UnitsLength.TERAMETER, Mul(Pow(10, 9), Sym("zettam")));
        c.put(UnitsLength.THOU, Mul(Mul(Int(393701), Pow(10, 14)), Sym("zettam")));
        c.put(UnitsLength.YARD, Mul(Rat(Mul(Int(9842525), Pow(10, 15)), Int(9)), Sym("zettam")));
        c.put(UnitsLength.YOCTOMETER, Mul(Pow(10, 45), Sym("zettam")));
        c.put(UnitsLength.YOTTAMETER, Mul(Rat(Int(1), Int(1000)), Sym("zettam")));
        c.put(UnitsLength.ZEPTOMETER, Mul(Pow(10, 42), Sym("zettam")));
        return Collections.unmodifiableMap(c);
    }

    private static final Map<UnitsLength, Map<UnitsLength, Conversion>> conversions;
    static {

        Map<UnitsLength, Map<UnitsLength, Conversion>> c
            = new EnumMap<UnitsLength, Map<UnitsLength, Conversion>>(UnitsLength.class);

        c.put(UnitsLength.ANGSTROM, createMapANGSTROM());
        c.put(UnitsLength.ASTRONOMICALUNIT, createMapASTRONOMICALUNIT());
        c.put(UnitsLength.ATTOMETER, createMapATTOMETER());
        c.put(UnitsLength.CENTIMETER, createMapCENTIMETER());
        c.put(UnitsLength.DECAMETER, createMapDECAMETER());
        c.put(UnitsLength.DECIMETER, createMapDECIMETER());
        c.put(UnitsLength.EXAMETER, createMapEXAMETER());
        c.put(UnitsLength.FEMTOMETER, createMapFEMTOMETER());
        c.put(UnitsLength.FOOT, createMapFOOT());
        c.put(UnitsLength.GIGAMETER, createMapGIGAMETER());
        c.put(UnitsLength.HECTOMETER, createMapHECTOMETER());
        c.put(UnitsLength.INCH, createMapINCH());
        c.put(UnitsLength.KILOMETER, createMapKILOMETER());
        c.put(UnitsLength.LIGHTYEAR, createMapLIGHTYEAR());
        c.put(UnitsLength.LINE, createMapLINE());
        c.put(UnitsLength.MEGAMETER, createMapMEGAMETER());
        c.put(UnitsLength.METER, createMapMETER());
        c.put(UnitsLength.MICROMETER, createMapMICROMETER());
        c.put(UnitsLength.MILE, createMapMILE());
        c.put(UnitsLength.MILLIMETER, createMapMILLIMETER());
        c.put(UnitsLength.NANOMETER, createMapNANOMETER());
        c.put(UnitsLength.PARSEC, createMapPARSEC());
        c.put(UnitsLength.PETAMETER, createMapPETAMETER());
        c.put(UnitsLength.PICOMETER, createMapPICOMETER());
        c.put(UnitsLength.POINT, createMapPOINT());
        c.put(UnitsLength.TERAMETER, createMapTERAMETER());
        c.put(UnitsLength.THOU, createMapTHOU());
        c.put(UnitsLength.YARD, createMapYARD());
        c.put(UnitsLength.YOCTOMETER, createMapYOCTOMETER());
        c.put(UnitsLength.YOTTAMETER, createMapYOTTAMETER());
        c.put(UnitsLength.ZEPTOMETER, createMapZEPTOMETER());
        c.put(UnitsLength.ZETTAMETER, createMapZETTAMETER());
        conversions = Collections.unmodifiableMap(c);
    }

    private static final Map<UnitsLength, String> SYMBOLS;
    static {
        Map<UnitsLength, String> s = new HashMap<UnitsLength, String>();
        s.put(UnitsLength.ANGSTROM, "Å");
        s.put(UnitsLength.ASTRONOMICALUNIT, "ua");
        s.put(UnitsLength.ATTOMETER, "am");
        s.put(UnitsLength.CENTIMETER, "cm");
        s.put(UnitsLength.DECAMETER, "dam");
        s.put(UnitsLength.DECIMETER, "dm");
        s.put(UnitsLength.EXAMETER, "Em");
        s.put(UnitsLength.FEMTOMETER, "fm");
        s.put(UnitsLength.FOOT, "ft");
        s.put(UnitsLength.GIGAMETER, "Gm");
        s.put(UnitsLength.HECTOMETER, "hm");
        s.put(UnitsLength.INCH, "in");
        s.put(UnitsLength.KILOMETER, "km");
        s.put(UnitsLength.LIGHTYEAR, "ly");
        s.put(UnitsLength.LINE, "li");
        s.put(UnitsLength.MEGAMETER, "Mm");
        s.put(UnitsLength.METER, "m");
        s.put(UnitsLength.MICROMETER, "µm");
        s.put(UnitsLength.MILE, "mi");
        s.put(UnitsLength.MILLIMETER, "mm");
        s.put(UnitsLength.NANOMETER, "nm");
        s.put(UnitsLength.PARSEC, "pc");
        s.put(UnitsLength.PETAMETER, "Pm");
        s.put(UnitsLength.PICOMETER, "pm");
        s.put(UnitsLength.PIXEL, "pixel");
        s.put(UnitsLength.POINT, "pt");
        s.put(UnitsLength.REFERENCEFRAME, "reference frame");
        s.put(UnitsLength.TERAMETER, "Tm");
        s.put(UnitsLength.THOU, "thou");
        s.put(UnitsLength.YARD, "yd");
        s.put(UnitsLength.YOCTOMETER, "ym");
        s.put(UnitsLength.YOTTAMETER, "Ym");
        s.put(UnitsLength.ZEPTOMETER, "zm");
        s.put(UnitsLength.ZETTAMETER, "Zm");
        SYMBOLS = s;
    }

    public static String lookupSymbol(UnitsLength unit) {
        return SYMBOLS.get(unit);
    }

    public static final Ice.ObjectFactory makeFactory(final omero.client client) {

        return new Ice.ObjectFactory() {

            public Ice.Object create(String arg0) {
                return new LengthI();
            }

            public void destroy() {
                // no-op
            }

        };
    };

    //
    // CONVERSIONS
    //

    public static ome.xml.model.enums.UnitsLength makeXMLUnit(String unit) {
        try {
            return ome.xml.model.enums.UnitsLength
                    .fromString((String) unit);
        } catch (EnumerationException e) {
            throw new RuntimeException("Bad Length unit: " + unit, e);
        }
    }

    public static ome.units.quantity.Length makeXMLQuantity(double d, String unit) {
        ome.units.unit.Unit<ome.units.quantity.Length> units =
                ome.xml.model.enums.handlers.UnitsLengthEnumHandler
                        .getBaseUnit(makeXMLUnit(unit));
        return new ome.units.quantity.Length(d, units);
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
   public static ome.units.quantity.Length convert(Length t) {
       if (t == null) {
           return null;
       }

       Double v = t.getValue();
       // Use the code/symbol-mapping in the ome.model.enums files
       // to convert to the specification value.
       String u = ome.model.enums.UnitsLength.valueOf(
               t.getUnit().toString()).getSymbol();
       ome.xml.model.enums.UnitsLength units = makeXMLUnit(u);
       ome.units.unit.Unit<ome.units.quantity.Length> units2 =
               ome.xml.model.enums.handlers.UnitsLengthEnumHandler
                       .getBaseUnit(units);

       return new ome.units.quantity.Length(v, units2);
   }


    //
    // REGULAR ICE CLASS
    //

    public final static Ice.ObjectFactory Factory = makeFactory(null);

    public LengthI() {
        super();
    }

    public LengthI(double d, UnitsLength unit) {
        super();
        this.setUnit(unit);
        this.setValue(d);
    }

    public LengthI(double d,
            Unit<ome.units.quantity.Length> unit) {
        this(d, ome.model.enums.UnitsLength.bySymbol(unit.getSymbol()));
    }

   /**
    * Copy constructor that converts the given {@link omero.model.Length}
    * based on the given ome-xml enum
    */
   public LengthI(Length value, Unit<ome.units.quantity.Length> ul) throws BigResult {
       this(value,
            ome.model.enums.UnitsLength.bySymbol(ul.getSymbol()).toString());
   }

   /**
    * Copy constructor that converts the given {@link omero.model.Length}
    * based on the given ome.model enum
    */
   public LengthI(double d, ome.model.enums.UnitsLength ul) {
        this(d, UnitsLength.valueOf(ul.toString()));
    }

   /**
    * Copy constructor that converts the given {@link omero.model.Length}
    * based on the given enum string.
    *
    * @param target String representation of the CODE enum
    */
    public LengthI(Length value, String target) throws BigResult {
       String source = value.getUnit().toString();
       if (target.equals(source)) {
           setValue(value.getValue());
           setUnit(value.getUnit());
        } else {
            UnitsLength targetUnit = UnitsLength.valueOf(target);
            Conversion conversion = conversions.get(value.getUnit()).get(target);
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
    public LengthI(Length value, UnitsLength target) throws BigResult {
        this(value, target.toString());
    }

    /**
     * Convert a Bio-Formats {@link Length} to an OMERO Length.
     */
    public LengthI(ome.units.quantity.Length value) {
        ome.model.enums.UnitsLength internal =
            ome.model.enums.UnitsLength.bySymbol(value.unit().getSymbol());
        UnitsLength ul = UnitsLength.valueOf(internal.toString());
        setValue(value.value().doubleValue());
        setUnit(ul);
    }

    public double getValue(Ice.Current current) {
        return this.value;
    }

    public void setValue(double value , Ice.Current current) {
        this.value = value;
    }

    public UnitsLength getUnit(Ice.Current current) {
        return this.unit;
    }

    public void setUnit(UnitsLength unit, Ice.Current current) {
        this.unit = unit;
    }

    public String getSymbol(Ice.Current current) {
        return SYMBOLS.get(this.unit);
    }

    public Length copy(Ice.Current ignore) {
        LengthI copy = new LengthI();
        copy.setValue(getValue());
        copy.setUnit(getUnit());
        return copy;
    }

    @Override
    public void copyObject(Filterable model, ModelMapper mapper) {
        if (model instanceof ome.model.units.Length) {
            ome.model.units.Length t = (ome.model.units.Length) model;
            this.value = t.getValue();
            this.unit = UnitsLength.valueOf(t.getUnit().toString());
        } else {
            throw new IllegalArgumentException(
              "Length cannot copy from " +
              (model==null ? "null" : model.getClass().getName()));
        }
    }

    @Override
    public Filterable fillObject(ReverseModelMapper mapper) {
        ome.model.enums.UnitsLength ut = ome.model.enums.UnitsLength.valueOf(getUnit().toString());
        ome.model.units.Length t = new ome.model.units.Length(getValue(), ut);
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
        return "Length(" + value + " " + unit + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Length other = (Length) obj;
        if (unit != other.unit)
            return false;
        if (Double.doubleToLongBits(value) != Double
                .doubleToLongBits(other.value))
            return false;
        return true;
    }

}

