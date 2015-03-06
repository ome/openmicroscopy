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
        c.put(UnitsLength.ASTRONOMICALUNIT, Mul(Mul(Int(1495978707), Pow(10, 12)), Sym("ang")));
        c.put(UnitsLength.ATTOMETER, Mul(Rat(Int(1), Pow(10, 8)), Sym("ang")));
        c.put(UnitsLength.CENTIMETER, Mul(Pow(10, 8), Sym("ang")));
        c.put(UnitsLength.DECAMETER, Mul(Pow(10, 11), Sym("ang")));
        c.put(UnitsLength.DECIMETER, Mul(Pow(10, 9), Sym("ang")));
        c.put(UnitsLength.EXAMETER, Mul(Pow(10, 28), Sym("ang")));
        c.put(UnitsLength.FEMTOMETER, Mul(Rat(Int(1), Pow(10, 5)), Sym("ang")));
        c.put(UnitsLength.FOOT, Mul(Mul(Int(3048), Pow(10, 6)), Sym("ang")));
        c.put(UnitsLength.GIGAMETER, Mul(Pow(10, 19), Sym("ang")));
        c.put(UnitsLength.HECTOMETER, Mul(Pow(10, 12), Sym("ang")));
        c.put(UnitsLength.INCH, Mul(Mul(Int(254), Pow(10, 6)), Sym("ang")));
        c.put(UnitsLength.KILOMETER, Mul(Pow(10, 13), Sym("ang")));
        c.put(UnitsLength.LIGHTYEAR, Mul(Mul(Int("94607304725808"), Pow(10, 12)), Sym("ang")));
        c.put(UnitsLength.LINE, Mul(Rat(Mul(Int(635), Pow(10, 5)), Int(3)), Sym("ang")));
        c.put(UnitsLength.MEGAMETER, Mul(Pow(10, 16), Sym("ang")));
        c.put(UnitsLength.METER, Mul(Pow(10, 10), Sym("ang")));
        c.put(UnitsLength.MICROMETER, Mul(Pow(10, 4), Sym("ang")));
        c.put(UnitsLength.MILE, Mul(Mul(Int(1609344), Pow(10, 7)), Sym("ang")));
        c.put(UnitsLength.MILLIMETER, Mul(Pow(10, 7), Sym("ang")));
        c.put(UnitsLength.NANOMETER, Mul(Int(10), Sym("ang")));
        c.put(UnitsLength.PARSEC, Mul(Mul(Int(30856776), Pow(10, 19)), Sym("ang")));
        c.put(UnitsLength.PETAMETER, Mul(Pow(10, 25), Sym("ang")));
        c.put(UnitsLength.PICOMETER, Mul(Rat(Int(1), Int(100)), Sym("ang")));
        c.put(UnitsLength.POINT, Mul(Rat(Mul(Int(3175), Pow(10, 4)), Int(9)), Sym("ang")));
        c.put(UnitsLength.TERAMETER, Mul(Pow(10, 22), Sym("ang")));
        c.put(UnitsLength.THOU, Mul(Int(254000), Sym("ang")));
        c.put(UnitsLength.YARD, Mul(Mul(Int(9144), Pow(10, 6)), Sym("ang")));
        c.put(UnitsLength.YOCTOMETER, Mul(Rat(Int(1), Pow(10, 14)), Sym("ang")));
        c.put(UnitsLength.YOTTAMETER, Mul(Pow(10, 34), Sym("ang")));
        c.put(UnitsLength.ZEPTOMETER, Mul(Rat(Int(1), Pow(10, 11)), Sym("ang")));
        c.put(UnitsLength.ZETTAMETER, Mul(Pow(10, 31), Sym("ang")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsLength, Conversion> createMapASTRONOMICALUNIT() {
        EnumMap<UnitsLength, Conversion> c =
            new EnumMap<UnitsLength, Conversion>(UnitsLength.class);
        c.put(UnitsLength.ANGSTROM, Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 12))), Sym("ua")));
        c.put(UnitsLength.ATTOMETER, Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 20))), Sym("ua")));
        c.put(UnitsLength.CENTIMETER, Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 4))), Sym("ua")));
        c.put(UnitsLength.DECAMETER, Mul(Rat(Int(1), Int("14959787070")), Sym("ua")));
        c.put(UnitsLength.DECIMETER, Mul(Rat(Int(1), Int("1495978707000")), Sym("ua")));
        c.put(UnitsLength.EXAMETER, Mul(Rat(Pow(10, 16), Int(1495978707)), Sym("ua")));
        c.put(UnitsLength.FEMTOMETER, Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 17))), Sym("ua")));
        c.put(UnitsLength.FOOT, Mul(Rat(Int(127), Int("62332446125000")), Sym("ua")));
        c.put(UnitsLength.GIGAMETER, Mul(Rat(Pow(10, 7), Int(1495978707)), Sym("ua")));
        c.put(UnitsLength.HECTOMETER, Mul(Rat(Int(1), Int(1495978707)), Sym("ua")));
        c.put(UnitsLength.INCH, Mul(Rat(Int(127), Mul(Int("7479893535"), Pow(10, 5))), Sym("ua")));
        c.put(UnitsLength.KILOMETER, Mul(Rat(Int(10), Int(1495978707)), Sym("ua")));
        c.put(UnitsLength.LIGHTYEAR, Mul(Rat(Int("431996825232"), Int(6830953)), Sym("ua")));
        c.put(UnitsLength.LINE, Mul(Rat(Int(127), Mul(Int("8975872242"), Pow(10, 6))), Sym("ua")));
        c.put(UnitsLength.MEGAMETER, Mul(Rat(Pow(10, 4), Int(1495978707)), Sym("ua")));
        c.put(UnitsLength.METER, Mul(Rat(Int(1), Int("149597870700")), Sym("ua")));
        c.put(UnitsLength.MICROMETER, Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 8))), Sym("ua")));
        c.put(UnitsLength.MILE, Mul(Rat(Int(16764), Int("1558311153125")), Sym("ua")));
        c.put(UnitsLength.MILLIMETER, Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 5))), Sym("ua")));
        c.put(UnitsLength.NANOMETER, Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 11))), Sym("ua")));
        c.put(UnitsLength.PARSEC, Mul(Rat(Mul(Int(10285592), Pow(10, 7)), Int(498659569)), Sym("ua")));
        c.put(UnitsLength.PETAMETER, Mul(Rat(Pow(10, 13), Int(1495978707)), Sym("ua")));
        c.put(UnitsLength.PICOMETER, Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 14))), Sym("ua")));
        c.put(UnitsLength.POINT, Mul(Rat(Int(127), Mul(Int("53855233452"), Pow(10, 6))), Sym("ua")));
        c.put(UnitsLength.TERAMETER, Mul(Rat(Pow(10, 10), Int(1495978707)), Sym("ua")));
        c.put(UnitsLength.THOU, Mul(Rat(Int(127), Mul(Int("7479893535"), Pow(10, 8))), Sym("ua")));
        c.put(UnitsLength.YARD, Mul(Rat(Int(381), Int("62332446125000")), Sym("ua")));
        c.put(UnitsLength.YOCTOMETER, Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 26))), Sym("ua")));
        c.put(UnitsLength.YOTTAMETER, Mul(Rat(Pow(10, 22), Int(1495978707)), Sym("ua")));
        c.put(UnitsLength.ZEPTOMETER, Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 23))), Sym("ua")));
        c.put(UnitsLength.ZETTAMETER, Mul(Rat(Pow(10, 19), Int(1495978707)), Sym("ua")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsLength, Conversion> createMapATTOMETER() {
        EnumMap<UnitsLength, Conversion> c =
            new EnumMap<UnitsLength, Conversion>(UnitsLength.class);
        c.put(UnitsLength.ANGSTROM, Mul(Pow(10, 8), Sym("attom")));
        c.put(UnitsLength.ASTRONOMICALUNIT, Mul(Mul(Int(1495978707), Pow(10, 20)), Sym("attom")));
        c.put(UnitsLength.CENTIMETER, Mul(Pow(10, 16), Sym("attom")));
        c.put(UnitsLength.DECAMETER, Mul(Pow(10, 19), Sym("attom")));
        c.put(UnitsLength.DECIMETER, Mul(Pow(10, 17), Sym("attom")));
        c.put(UnitsLength.EXAMETER, Mul(Pow(10, 36), Sym("attom")));
        c.put(UnitsLength.FEMTOMETER, Mul(Int(1000), Sym("attom")));
        c.put(UnitsLength.FOOT, Mul(Mul(Int(3048), Pow(10, 14)), Sym("attom")));
        c.put(UnitsLength.GIGAMETER, Mul(Pow(10, 27), Sym("attom")));
        c.put(UnitsLength.HECTOMETER, Mul(Pow(10, 20), Sym("attom")));
        c.put(UnitsLength.INCH, Mul(Mul(Int(254), Pow(10, 14)), Sym("attom")));
        c.put(UnitsLength.KILOMETER, Mul(Pow(10, 21), Sym("attom")));
        c.put(UnitsLength.LIGHTYEAR, Mul(Mul(Int("94607304725808"), Pow(10, 20)), Sym("attom")));
        c.put(UnitsLength.LINE, Mul(Rat(Mul(Int(635), Pow(10, 13)), Int(3)), Sym("attom")));
        c.put(UnitsLength.MEGAMETER, Mul(Pow(10, 24), Sym("attom")));
        c.put(UnitsLength.METER, Mul(Pow(10, 18), Sym("attom")));
        c.put(UnitsLength.MICROMETER, Mul(Pow(10, 12), Sym("attom")));
        c.put(UnitsLength.MILE, Mul(Mul(Int(1609344), Pow(10, 15)), Sym("attom")));
        c.put(UnitsLength.MILLIMETER, Mul(Pow(10, 15), Sym("attom")));
        c.put(UnitsLength.NANOMETER, Mul(Pow(10, 9), Sym("attom")));
        c.put(UnitsLength.PARSEC, Mul(Mul(Int(30856776), Pow(10, 27)), Sym("attom")));
        c.put(UnitsLength.PETAMETER, Mul(Pow(10, 33), Sym("attom")));
        c.put(UnitsLength.PICOMETER, Mul(Pow(10, 6), Sym("attom")));
        c.put(UnitsLength.POINT, Mul(Rat(Mul(Int(3175), Pow(10, 12)), Int(9)), Sym("attom")));
        c.put(UnitsLength.TERAMETER, Mul(Pow(10, 30), Sym("attom")));
        c.put(UnitsLength.THOU, Mul(Mul(Int(254), Pow(10, 11)), Sym("attom")));
        c.put(UnitsLength.YARD, Mul(Mul(Int(9144), Pow(10, 14)), Sym("attom")));
        c.put(UnitsLength.YOCTOMETER, Mul(Rat(Int(1), Pow(10, 6)), Sym("attom")));
        c.put(UnitsLength.YOTTAMETER, Mul(Pow(10, 42), Sym("attom")));
        c.put(UnitsLength.ZEPTOMETER, Mul(Rat(Int(1), Int(1000)), Sym("attom")));
        c.put(UnitsLength.ZETTAMETER, Mul(Pow(10, 39), Sym("attom")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsLength, Conversion> createMapCENTIMETER() {
        EnumMap<UnitsLength, Conversion> c =
            new EnumMap<UnitsLength, Conversion>(UnitsLength.class);
        c.put(UnitsLength.ANGSTROM, Mul(Rat(Int(1), Pow(10, 8)), Sym("centim")));
        c.put(UnitsLength.ASTRONOMICALUNIT, Mul(Mul(Int(1495978707), Pow(10, 4)), Sym("centim")));
        c.put(UnitsLength.ATTOMETER, Mul(Rat(Int(1), Pow(10, 16)), Sym("centim")));
        c.put(UnitsLength.DECAMETER, Mul(Int(1000), Sym("centim")));
        c.put(UnitsLength.DECIMETER, Mul(Int(10), Sym("centim")));
        c.put(UnitsLength.EXAMETER, Mul(Pow(10, 20), Sym("centim")));
        c.put(UnitsLength.FEMTOMETER, Mul(Rat(Int(1), Pow(10, 13)), Sym("centim")));
        c.put(UnitsLength.FOOT, Mul(Rat(Int(762), Int(25)), Sym("centim")));
        c.put(UnitsLength.GIGAMETER, Mul(Pow(10, 11), Sym("centim")));
        c.put(UnitsLength.HECTOMETER, Mul(Pow(10, 4), Sym("centim")));
        c.put(UnitsLength.INCH, Mul(Rat(Int(127), Int(50)), Sym("centim")));
        c.put(UnitsLength.KILOMETER, Mul(Pow(10, 5), Sym("centim")));
        c.put(UnitsLength.LIGHTYEAR, Mul(Mul(Int("94607304725808"), Pow(10, 4)), Sym("centim")));
        c.put(UnitsLength.LINE, Mul(Rat(Int(127), Int(600)), Sym("centim")));
        c.put(UnitsLength.MEGAMETER, Mul(Pow(10, 8), Sym("centim")));
        c.put(UnitsLength.METER, Mul(Int(100), Sym("centim")));
        c.put(UnitsLength.MICROMETER, Mul(Rat(Int(1), Pow(10, 4)), Sym("centim")));
        c.put(UnitsLength.MILE, Mul(Rat(Int(804672), Int(5)), Sym("centim")));
        c.put(UnitsLength.MILLIMETER, Mul(Rat(Int(1), Int(10)), Sym("centim")));
        c.put(UnitsLength.NANOMETER, Mul(Rat(Int(1), Pow(10, 7)), Sym("centim")));
        c.put(UnitsLength.PARSEC, Mul(Mul(Int(30856776), Pow(10, 11)), Sym("centim")));
        c.put(UnitsLength.PETAMETER, Mul(Pow(10, 17), Sym("centim")));
        c.put(UnitsLength.PICOMETER, Mul(Rat(Int(1), Pow(10, 10)), Sym("centim")));
        c.put(UnitsLength.POINT, Mul(Rat(Int(127), Int(3600)), Sym("centim")));
        c.put(UnitsLength.TERAMETER, Mul(Pow(10, 14), Sym("centim")));
        c.put(UnitsLength.THOU, Mul(Rat(Int(127), Mul(Int(5), Pow(10, 4))), Sym("centim")));
        c.put(UnitsLength.YARD, Mul(Rat(Int(2286), Int(25)), Sym("centim")));
        c.put(UnitsLength.YOCTOMETER, Mul(Rat(Int(1), Pow(10, 22)), Sym("centim")));
        c.put(UnitsLength.YOTTAMETER, Mul(Pow(10, 26), Sym("centim")));
        c.put(UnitsLength.ZEPTOMETER, Mul(Rat(Int(1), Pow(10, 19)), Sym("centim")));
        c.put(UnitsLength.ZETTAMETER, Mul(Pow(10, 23), Sym("centim")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsLength, Conversion> createMapDECAMETER() {
        EnumMap<UnitsLength, Conversion> c =
            new EnumMap<UnitsLength, Conversion>(UnitsLength.class);
        c.put(UnitsLength.ANGSTROM, Mul(Rat(Int(1), Pow(10, 11)), Sym("decam")));
        c.put(UnitsLength.ASTRONOMICALUNIT, Mul(Int("14959787070"), Sym("decam")));
        c.put(UnitsLength.ATTOMETER, Mul(Rat(Int(1), Pow(10, 19)), Sym("decam")));
        c.put(UnitsLength.CENTIMETER, Mul(Rat(Int(1), Int(1000)), Sym("decam")));
        c.put(UnitsLength.DECIMETER, Mul(Rat(Int(1), Int(100)), Sym("decam")));
        c.put(UnitsLength.EXAMETER, Mul(Pow(10, 17), Sym("decam")));
        c.put(UnitsLength.FEMTOMETER, Mul(Rat(Int(1), Pow(10, 16)), Sym("decam")));
        c.put(UnitsLength.FOOT, Mul(Rat(Int(381), Int(12500)), Sym("decam")));
        c.put(UnitsLength.GIGAMETER, Mul(Pow(10, 8), Sym("decam")));
        c.put(UnitsLength.HECTOMETER, Mul(Int(10), Sym("decam")));
        c.put(UnitsLength.INCH, Mul(Rat(Int(127), Mul(Int(5), Pow(10, 4))), Sym("decam")));
        c.put(UnitsLength.KILOMETER, Mul(Int(100), Sym("decam")));
        c.put(UnitsLength.LIGHTYEAR, Mul(Int("946073047258080"), Sym("decam")));
        c.put(UnitsLength.LINE, Mul(Rat(Int(127), Mul(Int(6), Pow(10, 5))), Sym("decam")));
        c.put(UnitsLength.MEGAMETER, Mul(Pow(10, 5), Sym("decam")));
        c.put(UnitsLength.METER, Mul(Rat(Int(1), Int(10)), Sym("decam")));
        c.put(UnitsLength.MICROMETER, Mul(Rat(Int(1), Pow(10, 7)), Sym("decam")));
        c.put(UnitsLength.MILE, Mul(Rat(Int(100584), Int(625)), Sym("decam")));
        c.put(UnitsLength.MILLIMETER, Mul(Rat(Int(1), Pow(10, 4)), Sym("decam")));
        c.put(UnitsLength.NANOMETER, Mul(Rat(Int(1), Pow(10, 10)), Sym("decam")));
        c.put(UnitsLength.PARSEC, Mul(Mul(Int(30856776), Pow(10, 8)), Sym("decam")));
        c.put(UnitsLength.PETAMETER, Mul(Pow(10, 14), Sym("decam")));
        c.put(UnitsLength.PICOMETER, Mul(Rat(Int(1), Pow(10, 13)), Sym("decam")));
        c.put(UnitsLength.POINT, Mul(Rat(Int(127), Mul(Int(36), Pow(10, 5))), Sym("decam")));
        c.put(UnitsLength.TERAMETER, Mul(Pow(10, 11), Sym("decam")));
        c.put(UnitsLength.THOU, Mul(Rat(Int(127), Mul(Int(5), Pow(10, 7))), Sym("decam")));
        c.put(UnitsLength.YARD, Mul(Rat(Int(1143), Int(12500)), Sym("decam")));
        c.put(UnitsLength.YOCTOMETER, Mul(Rat(Int(1), Pow(10, 25)), Sym("decam")));
        c.put(UnitsLength.YOTTAMETER, Mul(Pow(10, 23), Sym("decam")));
        c.put(UnitsLength.ZEPTOMETER, Mul(Rat(Int(1), Pow(10, 22)), Sym("decam")));
        c.put(UnitsLength.ZETTAMETER, Mul(Pow(10, 20), Sym("decam")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsLength, Conversion> createMapDECIMETER() {
        EnumMap<UnitsLength, Conversion> c =
            new EnumMap<UnitsLength, Conversion>(UnitsLength.class);
        c.put(UnitsLength.ANGSTROM, Mul(Rat(Int(1), Pow(10, 9)), Sym("decim")));
        c.put(UnitsLength.ASTRONOMICALUNIT, Mul(Int("1495978707000"), Sym("decim")));
        c.put(UnitsLength.ATTOMETER, Mul(Rat(Int(1), Pow(10, 17)), Sym("decim")));
        c.put(UnitsLength.CENTIMETER, Mul(Rat(Int(1), Int(10)), Sym("decim")));
        c.put(UnitsLength.DECAMETER, Mul(Int(100), Sym("decim")));
        c.put(UnitsLength.EXAMETER, Mul(Pow(10, 19), Sym("decim")));
        c.put(UnitsLength.FEMTOMETER, Mul(Rat(Int(1), Pow(10, 14)), Sym("decim")));
        c.put(UnitsLength.FOOT, Mul(Rat(Int(381), Int(125)), Sym("decim")));
        c.put(UnitsLength.GIGAMETER, Mul(Pow(10, 10), Sym("decim")));
        c.put(UnitsLength.HECTOMETER, Mul(Int(1000), Sym("decim")));
        c.put(UnitsLength.INCH, Mul(Rat(Int(127), Int(500)), Sym("decim")));
        c.put(UnitsLength.KILOMETER, Mul(Pow(10, 4), Sym("decim")));
        c.put(UnitsLength.LIGHTYEAR, Mul(Int("94607304725808000"), Sym("decim")));
        c.put(UnitsLength.LINE, Mul(Rat(Int(127), Int(6000)), Sym("decim")));
        c.put(UnitsLength.MEGAMETER, Mul(Pow(10, 7), Sym("decim")));
        c.put(UnitsLength.METER, Mul(Int(10), Sym("decim")));
        c.put(UnitsLength.MICROMETER, Mul(Rat(Int(1), Pow(10, 5)), Sym("decim")));
        c.put(UnitsLength.MILE, Mul(Rat(Int(402336), Int(25)), Sym("decim")));
        c.put(UnitsLength.MILLIMETER, Mul(Rat(Int(1), Int(100)), Sym("decim")));
        c.put(UnitsLength.NANOMETER, Mul(Rat(Int(1), Pow(10, 8)), Sym("decim")));
        c.put(UnitsLength.PARSEC, Mul(Mul(Int(30856776), Pow(10, 10)), Sym("decim")));
        c.put(UnitsLength.PETAMETER, Mul(Pow(10, 16), Sym("decim")));
        c.put(UnitsLength.PICOMETER, Mul(Rat(Int(1), Pow(10, 11)), Sym("decim")));
        c.put(UnitsLength.POINT, Mul(Rat(Int(127), Int(36000)), Sym("decim")));
        c.put(UnitsLength.TERAMETER, Mul(Pow(10, 13), Sym("decim")));
        c.put(UnitsLength.THOU, Mul(Rat(Int(127), Mul(Int(5), Pow(10, 5))), Sym("decim")));
        c.put(UnitsLength.YARD, Mul(Rat(Int(1143), Int(125)), Sym("decim")));
        c.put(UnitsLength.YOCTOMETER, Mul(Rat(Int(1), Pow(10, 23)), Sym("decim")));
        c.put(UnitsLength.YOTTAMETER, Mul(Pow(10, 25), Sym("decim")));
        c.put(UnitsLength.ZEPTOMETER, Mul(Rat(Int(1), Pow(10, 20)), Sym("decim")));
        c.put(UnitsLength.ZETTAMETER, Mul(Pow(10, 22), Sym("decim")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsLength, Conversion> createMapEXAMETER() {
        EnumMap<UnitsLength, Conversion> c =
            new EnumMap<UnitsLength, Conversion>(UnitsLength.class);
        c.put(UnitsLength.ANGSTROM, Mul(Rat(Int(1), Pow(10, 28)), Sym("exam")));
        c.put(UnitsLength.ASTRONOMICALUNIT, Mul(Rat(Int(1495978707), Pow(10, 16)), Sym("exam")));
        c.put(UnitsLength.ATTOMETER, Mul(Rat(Int(1), Pow(10, 36)), Sym("exam")));
        c.put(UnitsLength.CENTIMETER, Mul(Rat(Int(1), Pow(10, 20)), Sym("exam")));
        c.put(UnitsLength.DECAMETER, Mul(Rat(Int(1), Pow(10, 17)), Sym("exam")));
        c.put(UnitsLength.DECIMETER, Mul(Rat(Int(1), Pow(10, 19)), Sym("exam")));
        c.put(UnitsLength.FEMTOMETER, Mul(Rat(Int(1), Pow(10, 33)), Sym("exam")));
        c.put(UnitsLength.FOOT, Mul(Rat(Int(381), Mul(Int(125), Pow(10, 19))), Sym("exam")));
        c.put(UnitsLength.GIGAMETER, Mul(Rat(Int(1), Pow(10, 9)), Sym("exam")));
        c.put(UnitsLength.HECTOMETER, Mul(Rat(Int(1), Pow(10, 16)), Sym("exam")));
        c.put(UnitsLength.INCH, Mul(Rat(Int(127), Mul(Int(5), Pow(10, 21))), Sym("exam")));
        c.put(UnitsLength.KILOMETER, Mul(Rat(Int(1), Pow(10, 15)), Sym("exam")));
        c.put(UnitsLength.LIGHTYEAR, Mul(Rat(Int("5912956545363"), Mul(Int(625), Pow(10, 12))), Sym("exam")));
        c.put(UnitsLength.LINE, Mul(Rat(Int(127), Mul(Int(6), Pow(10, 22))), Sym("exam")));
        c.put(UnitsLength.MEGAMETER, Mul(Rat(Int(1), Pow(10, 12)), Sym("exam")));
        c.put(UnitsLength.METER, Mul(Rat(Int(1), Pow(10, 18)), Sym("exam")));
        c.put(UnitsLength.MICROMETER, Mul(Rat(Int(1), Pow(10, 24)), Sym("exam")));
        c.put(UnitsLength.MILE, Mul(Rat(Int(12573), Mul(Int(78125), Pow(10, 14))), Sym("exam")));
        c.put(UnitsLength.MILLIMETER, Mul(Rat(Int(1), Pow(10, 21)), Sym("exam")));
        c.put(UnitsLength.NANOMETER, Mul(Rat(Int(1), Pow(10, 27)), Sym("exam")));
        c.put(UnitsLength.PARSEC, Mul(Rat(Int(3857097), Mul(Int(125), Pow(10, 6))), Sym("exam")));
        c.put(UnitsLength.PETAMETER, Mul(Rat(Int(1), Int(1000)), Sym("exam")));
        c.put(UnitsLength.PICOMETER, Mul(Rat(Int(1), Pow(10, 30)), Sym("exam")));
        c.put(UnitsLength.POINT, Mul(Rat(Int(127), Mul(Int(36), Pow(10, 22))), Sym("exam")));
        c.put(UnitsLength.TERAMETER, Mul(Rat(Int(1), Pow(10, 6)), Sym("exam")));
        c.put(UnitsLength.THOU, Mul(Rat(Int(127), Mul(Int(5), Pow(10, 24))), Sym("exam")));
        c.put(UnitsLength.YARD, Mul(Rat(Int(1143), Mul(Int(125), Pow(10, 19))), Sym("exam")));
        c.put(UnitsLength.YOCTOMETER, Mul(Rat(Int(1), Pow(10, 42)), Sym("exam")));
        c.put(UnitsLength.YOTTAMETER, Mul(Pow(10, 6), Sym("exam")));
        c.put(UnitsLength.ZEPTOMETER, Mul(Rat(Int(1), Pow(10, 39)), Sym("exam")));
        c.put(UnitsLength.ZETTAMETER, Mul(Int(1000), Sym("exam")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsLength, Conversion> createMapFEMTOMETER() {
        EnumMap<UnitsLength, Conversion> c =
            new EnumMap<UnitsLength, Conversion>(UnitsLength.class);
        c.put(UnitsLength.ANGSTROM, Mul(Pow(10, 5), Sym("femtom")));
        c.put(UnitsLength.ASTRONOMICALUNIT, Mul(Mul(Int(1495978707), Pow(10, 17)), Sym("femtom")));
        c.put(UnitsLength.ATTOMETER, Mul(Rat(Int(1), Int(1000)), Sym("femtom")));
        c.put(UnitsLength.CENTIMETER, Mul(Pow(10, 13), Sym("femtom")));
        c.put(UnitsLength.DECAMETER, Mul(Pow(10, 16), Sym("femtom")));
        c.put(UnitsLength.DECIMETER, Mul(Pow(10, 14), Sym("femtom")));
        c.put(UnitsLength.EXAMETER, Mul(Pow(10, 33), Sym("femtom")));
        c.put(UnitsLength.FOOT, Mul(Mul(Int(3048), Pow(10, 11)), Sym("femtom")));
        c.put(UnitsLength.GIGAMETER, Mul(Pow(10, 24), Sym("femtom")));
        c.put(UnitsLength.HECTOMETER, Mul(Pow(10, 17), Sym("femtom")));
        c.put(UnitsLength.INCH, Mul(Mul(Int(254), Pow(10, 11)), Sym("femtom")));
        c.put(UnitsLength.KILOMETER, Mul(Pow(10, 18), Sym("femtom")));
        c.put(UnitsLength.LIGHTYEAR, Mul(Mul(Int("94607304725808"), Pow(10, 17)), Sym("femtom")));
        c.put(UnitsLength.LINE, Mul(Rat(Mul(Int(635), Pow(10, 10)), Int(3)), Sym("femtom")));
        c.put(UnitsLength.MEGAMETER, Mul(Pow(10, 21), Sym("femtom")));
        c.put(UnitsLength.METER, Mul(Pow(10, 15), Sym("femtom")));
        c.put(UnitsLength.MICROMETER, Mul(Pow(10, 9), Sym("femtom")));
        c.put(UnitsLength.MILE, Mul(Mul(Int(1609344), Pow(10, 12)), Sym("femtom")));
        c.put(UnitsLength.MILLIMETER, Mul(Pow(10, 12), Sym("femtom")));
        c.put(UnitsLength.NANOMETER, Mul(Pow(10, 6), Sym("femtom")));
        c.put(UnitsLength.PARSEC, Mul(Mul(Int(30856776), Pow(10, 24)), Sym("femtom")));
        c.put(UnitsLength.PETAMETER, Mul(Pow(10, 30), Sym("femtom")));
        c.put(UnitsLength.PICOMETER, Mul(Int(1000), Sym("femtom")));
        c.put(UnitsLength.POINT, Mul(Rat(Mul(Int(3175), Pow(10, 9)), Int(9)), Sym("femtom")));
        c.put(UnitsLength.TERAMETER, Mul(Pow(10, 27), Sym("femtom")));
        c.put(UnitsLength.THOU, Mul(Mul(Int(254), Pow(10, 8)), Sym("femtom")));
        c.put(UnitsLength.YARD, Mul(Mul(Int(9144), Pow(10, 11)), Sym("femtom")));
        c.put(UnitsLength.YOCTOMETER, Mul(Rat(Int(1), Pow(10, 9)), Sym("femtom")));
        c.put(UnitsLength.YOTTAMETER, Mul(Pow(10, 39), Sym("femtom")));
        c.put(UnitsLength.ZEPTOMETER, Mul(Rat(Int(1), Pow(10, 6)), Sym("femtom")));
        c.put(UnitsLength.ZETTAMETER, Mul(Pow(10, 36), Sym("femtom")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsLength, Conversion> createMapFOOT() {
        EnumMap<UnitsLength, Conversion> c =
            new EnumMap<UnitsLength, Conversion>(UnitsLength.class);
        c.put(UnitsLength.ANGSTROM, Mul(Rat(Int(1), Mul(Int(3048), Pow(10, 6))), Sym("ft")));
        c.put(UnitsLength.ASTRONOMICALUNIT, Mul(Rat(Int("62332446125000"), Int(127)), Sym("ft")));
        c.put(UnitsLength.ATTOMETER, Mul(Rat(Int(1), Mul(Int(3048), Pow(10, 14))), Sym("ft")));
        c.put(UnitsLength.CENTIMETER, Mul(Rat(Int(25), Int(762)), Sym("ft")));
        c.put(UnitsLength.DECAMETER, Mul(Rat(Int(12500), Int(381)), Sym("ft")));
        c.put(UnitsLength.DECIMETER, Mul(Rat(Int(125), Int(381)), Sym("ft")));
        c.put(UnitsLength.EXAMETER, Mul(Rat(Mul(Int(125), Pow(10, 19)), Int(381)), Sym("ft")));
        c.put(UnitsLength.FEMTOMETER, Mul(Rat(Int(1), Mul(Int(3048), Pow(10, 11))), Sym("ft")));
        c.put(UnitsLength.GIGAMETER, Mul(Rat(Mul(Int(125), Pow(10, 10)), Int(381)), Sym("ft")));
        c.put(UnitsLength.HECTOMETER, Mul(Rat(Int(125000), Int(381)), Sym("ft")));
        c.put(UnitsLength.INCH, Mul(Rat(Int(1), Int(12)), Sym("ft")));
        c.put(UnitsLength.KILOMETER, Mul(Rat(Mul(Int(125), Pow(10, 4)), Int(381)), Sym("ft")));
        c.put(UnitsLength.LIGHTYEAR, Mul(Rat(Mul(Int("3941971030242"), Pow(10, 6)), Int(127)), Sym("ft")));
        c.put(UnitsLength.LINE, Mul(Rat(Int(1), Int(144)), Sym("ft")));
        c.put(UnitsLength.MEGAMETER, Mul(Rat(Mul(Int(125), Pow(10, 7)), Int(381)), Sym("ft")));
        c.put(UnitsLength.METER, Mul(Rat(Int(1250), Int(381)), Sym("ft")));
        c.put(UnitsLength.MICROMETER, Mul(Rat(Int(1), Int(304800)), Sym("ft")));
        c.put(UnitsLength.MILE, Mul(Int(5280), Sym("ft")));
        c.put(UnitsLength.MILLIMETER, Mul(Rat(Int(5), Int(1524)), Sym("ft")));
        c.put(UnitsLength.NANOMETER, Mul(Rat(Int(1), Mul(Int(3048), Pow(10, 5))), Sym("ft")));
        c.put(UnitsLength.PARSEC, Mul(Rat(Mul(Int(1285699), Pow(10, 13)), Int(127)), Sym("ft")));
        c.put(UnitsLength.PETAMETER, Mul(Rat(Mul(Int(125), Pow(10, 16)), Int(381)), Sym("ft")));
        c.put(UnitsLength.PICOMETER, Mul(Rat(Int(1), Mul(Int(3048), Pow(10, 8))), Sym("ft")));
        c.put(UnitsLength.POINT, Mul(Rat(Int(1), Int(864)), Sym("ft")));
        c.put(UnitsLength.TERAMETER, Mul(Rat(Mul(Int(125), Pow(10, 13)), Int(381)), Sym("ft")));
        c.put(UnitsLength.THOU, Mul(Rat(Int(1), Int(12000)), Sym("ft")));
        c.put(UnitsLength.YARD, Mul(Int(3), Sym("ft")));
        c.put(UnitsLength.YOCTOMETER, Mul(Rat(Int(1), Mul(Int(3048), Pow(10, 20))), Sym("ft")));
        c.put(UnitsLength.YOTTAMETER, Mul(Rat(Mul(Int(125), Pow(10, 25)), Int(381)), Sym("ft")));
        c.put(UnitsLength.ZEPTOMETER, Mul(Rat(Int(1), Mul(Int(3048), Pow(10, 17))), Sym("ft")));
        c.put(UnitsLength.ZETTAMETER, Mul(Rat(Mul(Int(125), Pow(10, 22)), Int(381)), Sym("ft")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsLength, Conversion> createMapGIGAMETER() {
        EnumMap<UnitsLength, Conversion> c =
            new EnumMap<UnitsLength, Conversion>(UnitsLength.class);
        c.put(UnitsLength.ANGSTROM, Mul(Rat(Int(1), Pow(10, 19)), Sym("gigam")));
        c.put(UnitsLength.ASTRONOMICALUNIT, Mul(Rat(Int(1495978707), Pow(10, 7)), Sym("gigam")));
        c.put(UnitsLength.ATTOMETER, Mul(Rat(Int(1), Pow(10, 27)), Sym("gigam")));
        c.put(UnitsLength.CENTIMETER, Mul(Rat(Int(1), Pow(10, 11)), Sym("gigam")));
        c.put(UnitsLength.DECAMETER, Mul(Rat(Int(1), Pow(10, 8)), Sym("gigam")));
        c.put(UnitsLength.DECIMETER, Mul(Rat(Int(1), Pow(10, 10)), Sym("gigam")));
        c.put(UnitsLength.EXAMETER, Mul(Pow(10, 9), Sym("gigam")));
        c.put(UnitsLength.FEMTOMETER, Mul(Rat(Int(1), Pow(10, 24)), Sym("gigam")));
        c.put(UnitsLength.FOOT, Mul(Rat(Int(381), Mul(Int(125), Pow(10, 10))), Sym("gigam")));
        c.put(UnitsLength.HECTOMETER, Mul(Rat(Int(1), Pow(10, 7)), Sym("gigam")));
        c.put(UnitsLength.INCH, Mul(Rat(Int(127), Mul(Int(5), Pow(10, 12))), Sym("gigam")));
        c.put(UnitsLength.KILOMETER, Mul(Rat(Int(1), Pow(10, 6)), Sym("gigam")));
        c.put(UnitsLength.LIGHTYEAR, Mul(Rat(Int("5912956545363"), Int(625000)), Sym("gigam")));
        c.put(UnitsLength.LINE, Mul(Rat(Int(127), Mul(Int(6), Pow(10, 13))), Sym("gigam")));
        c.put(UnitsLength.MEGAMETER, Mul(Rat(Int(1), Int(1000)), Sym("gigam")));
        c.put(UnitsLength.METER, Mul(Rat(Int(1), Pow(10, 9)), Sym("gigam")));
        c.put(UnitsLength.MICROMETER, Mul(Rat(Int(1), Pow(10, 15)), Sym("gigam")));
        c.put(UnitsLength.MILE, Mul(Rat(Int(12573), Mul(Int(78125), Pow(10, 5))), Sym("gigam")));
        c.put(UnitsLength.MILLIMETER, Mul(Rat(Int(1), Pow(10, 12)), Sym("gigam")));
        c.put(UnitsLength.NANOMETER, Mul(Rat(Int(1), Pow(10, 18)), Sym("gigam")));
        c.put(UnitsLength.PARSEC, Mul(Int(30856776), Sym("gigam")));
        c.put(UnitsLength.PETAMETER, Mul(Pow(10, 6), Sym("gigam")));
        c.put(UnitsLength.PICOMETER, Mul(Rat(Int(1), Pow(10, 21)), Sym("gigam")));
        c.put(UnitsLength.POINT, Mul(Rat(Int(127), Mul(Int(36), Pow(10, 13))), Sym("gigam")));
        c.put(UnitsLength.TERAMETER, Mul(Int(1000), Sym("gigam")));
        c.put(UnitsLength.THOU, Mul(Rat(Int(127), Mul(Int(5), Pow(10, 15))), Sym("gigam")));
        c.put(UnitsLength.YARD, Mul(Rat(Int(1143), Mul(Int(125), Pow(10, 10))), Sym("gigam")));
        c.put(UnitsLength.YOCTOMETER, Mul(Rat(Int(1), Pow(10, 33)), Sym("gigam")));
        c.put(UnitsLength.YOTTAMETER, Mul(Pow(10, 15), Sym("gigam")));
        c.put(UnitsLength.ZEPTOMETER, Mul(Rat(Int(1), Pow(10, 30)), Sym("gigam")));
        c.put(UnitsLength.ZETTAMETER, Mul(Pow(10, 12), Sym("gigam")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsLength, Conversion> createMapHECTOMETER() {
        EnumMap<UnitsLength, Conversion> c =
            new EnumMap<UnitsLength, Conversion>(UnitsLength.class);
        c.put(UnitsLength.ANGSTROM, Mul(Rat(Int(1), Pow(10, 12)), Sym("hectom")));
        c.put(UnitsLength.ASTRONOMICALUNIT, Mul(Int(1495978707), Sym("hectom")));
        c.put(UnitsLength.ATTOMETER, Mul(Rat(Int(1), Pow(10, 20)), Sym("hectom")));
        c.put(UnitsLength.CENTIMETER, Mul(Rat(Int(1), Pow(10, 4)), Sym("hectom")));
        c.put(UnitsLength.DECAMETER, Mul(Rat(Int(1), Int(10)), Sym("hectom")));
        c.put(UnitsLength.DECIMETER, Mul(Rat(Int(1), Int(1000)), Sym("hectom")));
        c.put(UnitsLength.EXAMETER, Mul(Pow(10, 16), Sym("hectom")));
        c.put(UnitsLength.FEMTOMETER, Mul(Rat(Int(1), Pow(10, 17)), Sym("hectom")));
        c.put(UnitsLength.FOOT, Mul(Rat(Int(381), Int(125000)), Sym("hectom")));
        c.put(UnitsLength.GIGAMETER, Mul(Pow(10, 7), Sym("hectom")));
        c.put(UnitsLength.INCH, Mul(Rat(Int(127), Mul(Int(5), Pow(10, 5))), Sym("hectom")));
        c.put(UnitsLength.KILOMETER, Mul(Int(10), Sym("hectom")));
        c.put(UnitsLength.LIGHTYEAR, Mul(Int("94607304725808"), Sym("hectom")));
        c.put(UnitsLength.LINE, Mul(Rat(Int(127), Mul(Int(6), Pow(10, 6))), Sym("hectom")));
        c.put(UnitsLength.MEGAMETER, Mul(Pow(10, 4), Sym("hectom")));
        c.put(UnitsLength.METER, Mul(Rat(Int(1), Int(100)), Sym("hectom")));
        c.put(UnitsLength.MICROMETER, Mul(Rat(Int(1), Pow(10, 8)), Sym("hectom")));
        c.put(UnitsLength.MILE, Mul(Rat(Int(50292), Int(3125)), Sym("hectom")));
        c.put(UnitsLength.MILLIMETER, Mul(Rat(Int(1), Pow(10, 5)), Sym("hectom")));
        c.put(UnitsLength.NANOMETER, Mul(Rat(Int(1), Pow(10, 11)), Sym("hectom")));
        c.put(UnitsLength.PARSEC, Mul(Mul(Int(30856776), Pow(10, 7)), Sym("hectom")));
        c.put(UnitsLength.PETAMETER, Mul(Pow(10, 13), Sym("hectom")));
        c.put(UnitsLength.PICOMETER, Mul(Rat(Int(1), Pow(10, 14)), Sym("hectom")));
        c.put(UnitsLength.POINT, Mul(Rat(Int(127), Mul(Int(36), Pow(10, 6))), Sym("hectom")));
        c.put(UnitsLength.TERAMETER, Mul(Pow(10, 10), Sym("hectom")));
        c.put(UnitsLength.THOU, Mul(Rat(Int(127), Mul(Int(5), Pow(10, 8))), Sym("hectom")));
        c.put(UnitsLength.YARD, Mul(Rat(Int(1143), Int(125000)), Sym("hectom")));
        c.put(UnitsLength.YOCTOMETER, Mul(Rat(Int(1), Pow(10, 26)), Sym("hectom")));
        c.put(UnitsLength.YOTTAMETER, Mul(Pow(10, 22), Sym("hectom")));
        c.put(UnitsLength.ZEPTOMETER, Mul(Rat(Int(1), Pow(10, 23)), Sym("hectom")));
        c.put(UnitsLength.ZETTAMETER, Mul(Pow(10, 19), Sym("hectom")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsLength, Conversion> createMapINCH() {
        EnumMap<UnitsLength, Conversion> c =
            new EnumMap<UnitsLength, Conversion>(UnitsLength.class);
        c.put(UnitsLength.ANGSTROM, Mul(Rat(Int(1), Mul(Int(254), Pow(10, 6))), Sym("in")));
        c.put(UnitsLength.ASTRONOMICALUNIT, Mul(Rat(Mul(Int("7479893535"), Pow(10, 5)), Int(127)), Sym("in")));
        c.put(UnitsLength.ATTOMETER, Mul(Rat(Int(1), Mul(Int(254), Pow(10, 14))), Sym("in")));
        c.put(UnitsLength.CENTIMETER, Mul(Rat(Int(50), Int(127)), Sym("in")));
        c.put(UnitsLength.DECAMETER, Mul(Rat(Mul(Int(5), Pow(10, 4)), Int(127)), Sym("in")));
        c.put(UnitsLength.DECIMETER, Mul(Rat(Int(500), Int(127)), Sym("in")));
        c.put(UnitsLength.EXAMETER, Mul(Rat(Mul(Int(5), Pow(10, 21)), Int(127)), Sym("in")));
        c.put(UnitsLength.FEMTOMETER, Mul(Rat(Int(1), Mul(Int(254), Pow(10, 11))), Sym("in")));
        c.put(UnitsLength.FOOT, Mul(Int(12), Sym("in")));
        c.put(UnitsLength.GIGAMETER, Mul(Rat(Mul(Int(5), Pow(10, 12)), Int(127)), Sym("in")));
        c.put(UnitsLength.HECTOMETER, Mul(Rat(Mul(Int(5), Pow(10, 5)), Int(127)), Sym("in")));
        c.put(UnitsLength.KILOMETER, Mul(Rat(Mul(Int(5), Pow(10, 6)), Int(127)), Sym("in")));
        c.put(UnitsLength.LIGHTYEAR, Mul(Rat(Mul(Int("47303652362904"), Pow(10, 6)), Int(127)), Sym("in")));
        c.put(UnitsLength.LINE, Mul(Rat(Int(1), Int(12)), Sym("in")));
        c.put(UnitsLength.MEGAMETER, Mul(Rat(Mul(Int(5), Pow(10, 9)), Int(127)), Sym("in")));
        c.put(UnitsLength.METER, Mul(Rat(Int(5000), Int(127)), Sym("in")));
        c.put(UnitsLength.MICROMETER, Mul(Rat(Int(1), Int(25400)), Sym("in")));
        c.put(UnitsLength.MILE, Mul(Int(63360), Sym("in")));
        c.put(UnitsLength.MILLIMETER, Mul(Rat(Int(5), Int(127)), Sym("in")));
        c.put(UnitsLength.NANOMETER, Mul(Rat(Int(1), Mul(Int(254), Pow(10, 5))), Sym("in")));
        c.put(UnitsLength.PARSEC, Mul(Rat(Mul(Int(15428388), Pow(10, 13)), Int(127)), Sym("in")));
        c.put(UnitsLength.PETAMETER, Mul(Rat(Mul(Int(5), Pow(10, 18)), Int(127)), Sym("in")));
        c.put(UnitsLength.PICOMETER, Mul(Rat(Int(1), Mul(Int(254), Pow(10, 8))), Sym("in")));
        c.put(UnitsLength.POINT, Mul(Rat(Int(1), Int(72)), Sym("in")));
        c.put(UnitsLength.TERAMETER, Mul(Rat(Mul(Int(5), Pow(10, 15)), Int(127)), Sym("in")));
        c.put(UnitsLength.THOU, Mul(Rat(Int(1), Int(1000)), Sym("in")));
        c.put(UnitsLength.YARD, Mul(Int(36), Sym("in")));
        c.put(UnitsLength.YOCTOMETER, Mul(Rat(Int(1), Mul(Int(254), Pow(10, 20))), Sym("in")));
        c.put(UnitsLength.YOTTAMETER, Mul(Rat(Mul(Int(5), Pow(10, 27)), Int(127)), Sym("in")));
        c.put(UnitsLength.ZEPTOMETER, Mul(Rat(Int(1), Mul(Int(254), Pow(10, 17))), Sym("in")));
        c.put(UnitsLength.ZETTAMETER, Mul(Rat(Mul(Int(5), Pow(10, 24)), Int(127)), Sym("in")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsLength, Conversion> createMapKILOMETER() {
        EnumMap<UnitsLength, Conversion> c =
            new EnumMap<UnitsLength, Conversion>(UnitsLength.class);
        c.put(UnitsLength.ANGSTROM, Mul(Rat(Int(1), Pow(10, 13)), Sym("kilom")));
        c.put(UnitsLength.ASTRONOMICALUNIT, Mul(Rat(Int(1495978707), Int(10)), Sym("kilom")));
        c.put(UnitsLength.ATTOMETER, Mul(Rat(Int(1), Pow(10, 21)), Sym("kilom")));
        c.put(UnitsLength.CENTIMETER, Mul(Rat(Int(1), Pow(10, 5)), Sym("kilom")));
        c.put(UnitsLength.DECAMETER, Mul(Rat(Int(1), Int(100)), Sym("kilom")));
        c.put(UnitsLength.DECIMETER, Mul(Rat(Int(1), Pow(10, 4)), Sym("kilom")));
        c.put(UnitsLength.EXAMETER, Mul(Pow(10, 15), Sym("kilom")));
        c.put(UnitsLength.FEMTOMETER, Mul(Rat(Int(1), Pow(10, 18)), Sym("kilom")));
        c.put(UnitsLength.FOOT, Mul(Rat(Int(381), Mul(Int(125), Pow(10, 4))), Sym("kilom")));
        c.put(UnitsLength.GIGAMETER, Mul(Pow(10, 6), Sym("kilom")));
        c.put(UnitsLength.HECTOMETER, Mul(Rat(Int(1), Int(10)), Sym("kilom")));
        c.put(UnitsLength.INCH, Mul(Rat(Int(127), Mul(Int(5), Pow(10, 6))), Sym("kilom")));
        c.put(UnitsLength.LIGHTYEAR, Mul(Rat(Int("47303652362904"), Int(5)), Sym("kilom")));
        c.put(UnitsLength.LINE, Mul(Rat(Int(127), Mul(Int(6), Pow(10, 7))), Sym("kilom")));
        c.put(UnitsLength.MEGAMETER, Mul(Int(1000), Sym("kilom")));
        c.put(UnitsLength.METER, Mul(Rat(Int(1), Int(1000)), Sym("kilom")));
        c.put(UnitsLength.MICROMETER, Mul(Rat(Int(1), Pow(10, 9)), Sym("kilom")));
        c.put(UnitsLength.MILE, Mul(Rat(Int(25146), Int(15625)), Sym("kilom")));
        c.put(UnitsLength.MILLIMETER, Mul(Rat(Int(1), Pow(10, 6)), Sym("kilom")));
        c.put(UnitsLength.NANOMETER, Mul(Rat(Int(1), Pow(10, 12)), Sym("kilom")));
        c.put(UnitsLength.PARSEC, Mul(Mul(Int(30856776), Pow(10, 6)), Sym("kilom")));
        c.put(UnitsLength.PETAMETER, Mul(Pow(10, 12), Sym("kilom")));
        c.put(UnitsLength.PICOMETER, Mul(Rat(Int(1), Pow(10, 15)), Sym("kilom")));
        c.put(UnitsLength.POINT, Mul(Rat(Int(127), Mul(Int(36), Pow(10, 7))), Sym("kilom")));
        c.put(UnitsLength.TERAMETER, Mul(Pow(10, 9), Sym("kilom")));
        c.put(UnitsLength.THOU, Mul(Rat(Int(127), Mul(Int(5), Pow(10, 9))), Sym("kilom")));
        c.put(UnitsLength.YARD, Mul(Rat(Int(1143), Mul(Int(125), Pow(10, 4))), Sym("kilom")));
        c.put(UnitsLength.YOCTOMETER, Mul(Rat(Int(1), Pow(10, 27)), Sym("kilom")));
        c.put(UnitsLength.YOTTAMETER, Mul(Pow(10, 21), Sym("kilom")));
        c.put(UnitsLength.ZEPTOMETER, Mul(Rat(Int(1), Pow(10, 24)), Sym("kilom")));
        c.put(UnitsLength.ZETTAMETER, Mul(Pow(10, 18), Sym("kilom")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsLength, Conversion> createMapLIGHTYEAR() {
        EnumMap<UnitsLength, Conversion> c =
            new EnumMap<UnitsLength, Conversion>(UnitsLength.class);
        c.put(UnitsLength.ANGSTROM, Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 12))), Sym("ly")));
        c.put(UnitsLength.ASTRONOMICALUNIT, Mul(Rat(Int(6830953), Int("431996825232")), Sym("ly")));
        c.put(UnitsLength.ATTOMETER, Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 20))), Sym("ly")));
        c.put(UnitsLength.CENTIMETER, Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 4))), Sym("ly")));
        c.put(UnitsLength.DECAMETER, Mul(Rat(Int(1), Int("946073047258080")), Sym("ly")));
        c.put(UnitsLength.DECIMETER, Mul(Rat(Int(1), Int("94607304725808000")), Sym("ly")));
        c.put(UnitsLength.EXAMETER, Mul(Rat(Mul(Int(625), Pow(10, 12)), Int("5912956545363")), Sym("ly")));
        c.put(UnitsLength.FEMTOMETER, Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 17))), Sym("ly")));
        c.put(UnitsLength.FOOT, Mul(Rat(Int(127), Mul(Int("3941971030242"), Pow(10, 6))), Sym("ly")));
        c.put(UnitsLength.GIGAMETER, Mul(Rat(Int(625000), Int("5912956545363")), Sym("ly")));
        c.put(UnitsLength.HECTOMETER, Mul(Rat(Int(1), Int("94607304725808")), Sym("ly")));
        c.put(UnitsLength.INCH, Mul(Rat(Int(127), Mul(Int("47303652362904"), Pow(10, 6))), Sym("ly")));
        c.put(UnitsLength.KILOMETER, Mul(Rat(Int(5), Int("47303652362904")), Sym("ly")));
        c.put(UnitsLength.LINE, Mul(Rat(Int(127), Mul(Int("567643828354848"), Pow(10, 6))), Sym("ly")));
        c.put(UnitsLength.MEGAMETER, Mul(Rat(Int(625), Int("5912956545363")), Sym("ly")));
        c.put(UnitsLength.METER, Mul(Rat(Int(1), Int("9460730472580800")), Sym("ly")));
        c.put(UnitsLength.MICROMETER, Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 8))), Sym("ly")));
        c.put(UnitsLength.MILE, Mul(Rat(Int(1397), Int("8212439646337500")), Sym("ly")));
        c.put(UnitsLength.MILLIMETER, Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 5))), Sym("ly")));
        c.put(UnitsLength.NANOMETER, Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 11))), Sym("ly")));
        c.put(UnitsLength.PARSEC, Mul(Rat(Mul(Int(6428495), Pow(10, 6)), Int("1970985515121")), Sym("ly")));
        c.put(UnitsLength.PETAMETER, Mul(Rat(Mul(Int(625), Pow(10, 9)), Int("5912956545363")), Sym("ly")));
        c.put(UnitsLength.PICOMETER, Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 14))), Sym("ly")));
        c.put(UnitsLength.POINT, Mul(Rat(Int(127), Mul(Int("3405862970129088"), Pow(10, 6))), Sym("ly")));
        c.put(UnitsLength.TERAMETER, Mul(Rat(Mul(Int(625), Pow(10, 6)), Int("5912956545363")), Sym("ly")));
        c.put(UnitsLength.THOU, Mul(Rat(Int(127), Mul(Int("47303652362904"), Pow(10, 9))), Sym("ly")));
        c.put(UnitsLength.YARD, Mul(Rat(Int(127), Mul(Int("1313990343414"), Pow(10, 6))), Sym("ly")));
        c.put(UnitsLength.YOCTOMETER, Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 26))), Sym("ly")));
        c.put(UnitsLength.YOTTAMETER, Mul(Rat(Mul(Int(625), Pow(10, 18)), Int("5912956545363")), Sym("ly")));
        c.put(UnitsLength.ZEPTOMETER, Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 23))), Sym("ly")));
        c.put(UnitsLength.ZETTAMETER, Mul(Rat(Mul(Int(625), Pow(10, 15)), Int("5912956545363")), Sym("ly")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsLength, Conversion> createMapLINE() {
        EnumMap<UnitsLength, Conversion> c =
            new EnumMap<UnitsLength, Conversion>(UnitsLength.class);
        c.put(UnitsLength.ANGSTROM, Mul(Rat(Int(3), Mul(Int(635), Pow(10, 5))), Sym("li")));
        c.put(UnitsLength.ASTRONOMICALUNIT, Mul(Rat(Mul(Int("8975872242"), Pow(10, 6)), Int(127)), Sym("li")));
        c.put(UnitsLength.ATTOMETER, Mul(Rat(Int(3), Mul(Int(635), Pow(10, 13))), Sym("li")));
        c.put(UnitsLength.CENTIMETER, Mul(Rat(Int(600), Int(127)), Sym("li")));
        c.put(UnitsLength.DECAMETER, Mul(Rat(Mul(Int(6), Pow(10, 5)), Int(127)), Sym("li")));
        c.put(UnitsLength.DECIMETER, Mul(Rat(Int(6000), Int(127)), Sym("li")));
        c.put(UnitsLength.EXAMETER, Mul(Rat(Mul(Int(6), Pow(10, 22)), Int(127)), Sym("li")));
        c.put(UnitsLength.FEMTOMETER, Mul(Rat(Int(3), Mul(Int(635), Pow(10, 10))), Sym("li")));
        c.put(UnitsLength.FOOT, Mul(Int(144), Sym("li")));
        c.put(UnitsLength.GIGAMETER, Mul(Rat(Mul(Int(6), Pow(10, 13)), Int(127)), Sym("li")));
        c.put(UnitsLength.HECTOMETER, Mul(Rat(Mul(Int(6), Pow(10, 6)), Int(127)), Sym("li")));
        c.put(UnitsLength.INCH, Mul(Int(12), Sym("li")));
        c.put(UnitsLength.KILOMETER, Mul(Rat(Mul(Int(6), Pow(10, 7)), Int(127)), Sym("li")));
        c.put(UnitsLength.LIGHTYEAR, Mul(Rat(Mul(Int("567643828354848"), Pow(10, 6)), Int(127)), Sym("li")));
        c.put(UnitsLength.MEGAMETER, Mul(Rat(Mul(Int(6), Pow(10, 10)), Int(127)), Sym("li")));
        c.put(UnitsLength.METER, Mul(Rat(Mul(Int(6), Pow(10, 4)), Int(127)), Sym("li")));
        c.put(UnitsLength.MICROMETER, Mul(Rat(Int(3), Int(6350)), Sym("li")));
        c.put(UnitsLength.MILE, Mul(Int(760320), Sym("li")));
        c.put(UnitsLength.MILLIMETER, Mul(Rat(Int(60), Int(127)), Sym("li")));
        c.put(UnitsLength.NANOMETER, Mul(Rat(Int(3), Mul(Int(635), Pow(10, 4))), Sym("li")));
        c.put(UnitsLength.PARSEC, Mul(Rat(Mul(Int(185140656), Pow(10, 13)), Int(127)), Sym("li")));
        c.put(UnitsLength.PETAMETER, Mul(Rat(Mul(Int(6), Pow(10, 19)), Int(127)), Sym("li")));
        c.put(UnitsLength.PICOMETER, Mul(Rat(Int(3), Mul(Int(635), Pow(10, 7))), Sym("li")));
        c.put(UnitsLength.POINT, Mul(Rat(Int(1), Int(6)), Sym("li")));
        c.put(UnitsLength.TERAMETER, Mul(Rat(Mul(Int(6), Pow(10, 16)), Int(127)), Sym("li")));
        c.put(UnitsLength.THOU, Mul(Rat(Int(3), Int(250)), Sym("li")));
        c.put(UnitsLength.YARD, Mul(Int(432), Sym("li")));
        c.put(UnitsLength.YOCTOMETER, Mul(Rat(Int(3), Mul(Int(635), Pow(10, 19))), Sym("li")));
        c.put(UnitsLength.YOTTAMETER, Mul(Rat(Mul(Int(6), Pow(10, 28)), Int(127)), Sym("li")));
        c.put(UnitsLength.ZEPTOMETER, Mul(Rat(Int(3), Mul(Int(635), Pow(10, 16))), Sym("li")));
        c.put(UnitsLength.ZETTAMETER, Mul(Rat(Mul(Int(6), Pow(10, 25)), Int(127)), Sym("li")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsLength, Conversion> createMapMEGAMETER() {
        EnumMap<UnitsLength, Conversion> c =
            new EnumMap<UnitsLength, Conversion>(UnitsLength.class);
        c.put(UnitsLength.ANGSTROM, Mul(Rat(Int(1), Pow(10, 16)), Sym("megam")));
        c.put(UnitsLength.ASTRONOMICALUNIT, Mul(Rat(Int(1495978707), Pow(10, 4)), Sym("megam")));
        c.put(UnitsLength.ATTOMETER, Mul(Rat(Int(1), Pow(10, 24)), Sym("megam")));
        c.put(UnitsLength.CENTIMETER, Mul(Rat(Int(1), Pow(10, 8)), Sym("megam")));
        c.put(UnitsLength.DECAMETER, Mul(Rat(Int(1), Pow(10, 5)), Sym("megam")));
        c.put(UnitsLength.DECIMETER, Mul(Rat(Int(1), Pow(10, 7)), Sym("megam")));
        c.put(UnitsLength.EXAMETER, Mul(Pow(10, 12), Sym("megam")));
        c.put(UnitsLength.FEMTOMETER, Mul(Rat(Int(1), Pow(10, 21)), Sym("megam")));
        c.put(UnitsLength.FOOT, Mul(Rat(Int(381), Mul(Int(125), Pow(10, 7))), Sym("megam")));
        c.put(UnitsLength.GIGAMETER, Mul(Int(1000), Sym("megam")));
        c.put(UnitsLength.HECTOMETER, Mul(Rat(Int(1), Pow(10, 4)), Sym("megam")));
        c.put(UnitsLength.INCH, Mul(Rat(Int(127), Mul(Int(5), Pow(10, 9))), Sym("megam")));
        c.put(UnitsLength.KILOMETER, Mul(Rat(Int(1), Int(1000)), Sym("megam")));
        c.put(UnitsLength.LIGHTYEAR, Mul(Rat(Int("5912956545363"), Int(625)), Sym("megam")));
        c.put(UnitsLength.LINE, Mul(Rat(Int(127), Mul(Int(6), Pow(10, 10))), Sym("megam")));
        c.put(UnitsLength.METER, Mul(Rat(Int(1), Pow(10, 6)), Sym("megam")));
        c.put(UnitsLength.MICROMETER, Mul(Rat(Int(1), Pow(10, 12)), Sym("megam")));
        c.put(UnitsLength.MILE, Mul(Rat(Int(12573), Int(7812500)), Sym("megam")));
        c.put(UnitsLength.MILLIMETER, Mul(Rat(Int(1), Pow(10, 9)), Sym("megam")));
        c.put(UnitsLength.NANOMETER, Mul(Rat(Int(1), Pow(10, 15)), Sym("megam")));
        c.put(UnitsLength.PARSEC, Mul(Int("30856776000"), Sym("megam")));
        c.put(UnitsLength.PETAMETER, Mul(Pow(10, 9), Sym("megam")));
        c.put(UnitsLength.PICOMETER, Mul(Rat(Int(1), Pow(10, 18)), Sym("megam")));
        c.put(UnitsLength.POINT, Mul(Rat(Int(127), Mul(Int(36), Pow(10, 10))), Sym("megam")));
        c.put(UnitsLength.TERAMETER, Mul(Pow(10, 6), Sym("megam")));
        c.put(UnitsLength.THOU, Mul(Rat(Int(127), Mul(Int(5), Pow(10, 12))), Sym("megam")));
        c.put(UnitsLength.YARD, Mul(Rat(Int(1143), Mul(Int(125), Pow(10, 7))), Sym("megam")));
        c.put(UnitsLength.YOCTOMETER, Mul(Rat(Int(1), Pow(10, 30)), Sym("megam")));
        c.put(UnitsLength.YOTTAMETER, Mul(Pow(10, 18), Sym("megam")));
        c.put(UnitsLength.ZEPTOMETER, Mul(Rat(Int(1), Pow(10, 27)), Sym("megam")));
        c.put(UnitsLength.ZETTAMETER, Mul(Pow(10, 15), Sym("megam")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsLength, Conversion> createMapMETER() {
        EnumMap<UnitsLength, Conversion> c =
            new EnumMap<UnitsLength, Conversion>(UnitsLength.class);
        c.put(UnitsLength.ANGSTROM, Mul(Rat(Int(1), Pow(10, 10)), Sym("m")));
        c.put(UnitsLength.ASTRONOMICALUNIT, Mul(Int("149597870700"), Sym("m")));
        c.put(UnitsLength.ATTOMETER, Mul(Rat(Int(1), Pow(10, 18)), Sym("m")));
        c.put(UnitsLength.CENTIMETER, Mul(Rat(Int(1), Int(100)), Sym("m")));
        c.put(UnitsLength.DECAMETER, Mul(Int(10), Sym("m")));
        c.put(UnitsLength.DECIMETER, Mul(Rat(Int(1), Int(10)), Sym("m")));
        c.put(UnitsLength.EXAMETER, Mul(Pow(10, 18), Sym("m")));
        c.put(UnitsLength.FEMTOMETER, Mul(Rat(Int(1), Pow(10, 15)), Sym("m")));
        c.put(UnitsLength.FOOT, Mul(Rat(Int(381), Int(1250)), Sym("m")));
        c.put(UnitsLength.GIGAMETER, Mul(Pow(10, 9), Sym("m")));
        c.put(UnitsLength.HECTOMETER, Mul(Int(100), Sym("m")));
        c.put(UnitsLength.INCH, Mul(Rat(Int(127), Int(5000)), Sym("m")));
        c.put(UnitsLength.KILOMETER, Mul(Int(1000), Sym("m")));
        c.put(UnitsLength.LIGHTYEAR, Mul(Int("9460730472580800"), Sym("m")));
        c.put(UnitsLength.LINE, Mul(Rat(Int(127), Mul(Int(6), Pow(10, 4))), Sym("m")));
        c.put(UnitsLength.MEGAMETER, Mul(Pow(10, 6), Sym("m")));
        c.put(UnitsLength.MICROMETER, Mul(Rat(Int(1), Pow(10, 6)), Sym("m")));
        c.put(UnitsLength.MILE, Mul(Rat(Int(201168), Int(125)), Sym("m")));
        c.put(UnitsLength.MILLIMETER, Mul(Rat(Int(1), Int(1000)), Sym("m")));
        c.put(UnitsLength.NANOMETER, Mul(Rat(Int(1), Pow(10, 9)), Sym("m")));
        c.put(UnitsLength.PARSEC, Mul(Mul(Int(30856776), Pow(10, 9)), Sym("m")));
        c.put(UnitsLength.PETAMETER, Mul(Pow(10, 15), Sym("m")));
        c.put(UnitsLength.PICOMETER, Mul(Rat(Int(1), Pow(10, 12)), Sym("m")));
        c.put(UnitsLength.POINT, Mul(Rat(Int(127), Mul(Int(36), Pow(10, 4))), Sym("m")));
        c.put(UnitsLength.TERAMETER, Mul(Pow(10, 12), Sym("m")));
        c.put(UnitsLength.THOU, Mul(Rat(Int(127), Mul(Int(5), Pow(10, 6))), Sym("m")));
        c.put(UnitsLength.YARD, Mul(Rat(Int(1143), Int(1250)), Sym("m")));
        c.put(UnitsLength.YOCTOMETER, Mul(Rat(Int(1), Pow(10, 24)), Sym("m")));
        c.put(UnitsLength.YOTTAMETER, Mul(Pow(10, 24), Sym("m")));
        c.put(UnitsLength.ZEPTOMETER, Mul(Rat(Int(1), Pow(10, 21)), Sym("m")));
        c.put(UnitsLength.ZETTAMETER, Mul(Pow(10, 21), Sym("m")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsLength, Conversion> createMapMICROMETER() {
        EnumMap<UnitsLength, Conversion> c =
            new EnumMap<UnitsLength, Conversion>(UnitsLength.class);
        c.put(UnitsLength.ANGSTROM, Mul(Rat(Int(1), Pow(10, 4)), Sym("microm")));
        c.put(UnitsLength.ASTRONOMICALUNIT, Mul(Mul(Int(1495978707), Pow(10, 8)), Sym("microm")));
        c.put(UnitsLength.ATTOMETER, Mul(Rat(Int(1), Pow(10, 12)), Sym("microm")));
        c.put(UnitsLength.CENTIMETER, Mul(Pow(10, 4), Sym("microm")));
        c.put(UnitsLength.DECAMETER, Mul(Pow(10, 7), Sym("microm")));
        c.put(UnitsLength.DECIMETER, Mul(Pow(10, 5), Sym("microm")));
        c.put(UnitsLength.EXAMETER, Mul(Pow(10, 24), Sym("microm")));
        c.put(UnitsLength.FEMTOMETER, Mul(Rat(Int(1), Pow(10, 9)), Sym("microm")));
        c.put(UnitsLength.FOOT, Mul(Int(304800), Sym("microm")));
        c.put(UnitsLength.GIGAMETER, Mul(Pow(10, 15), Sym("microm")));
        c.put(UnitsLength.HECTOMETER, Mul(Pow(10, 8), Sym("microm")));
        c.put(UnitsLength.INCH, Mul(Int(25400), Sym("microm")));
        c.put(UnitsLength.KILOMETER, Mul(Pow(10, 9), Sym("microm")));
        c.put(UnitsLength.LIGHTYEAR, Mul(Mul(Int("94607304725808"), Pow(10, 8)), Sym("microm")));
        c.put(UnitsLength.LINE, Mul(Rat(Int(6350), Int(3)), Sym("microm")));
        c.put(UnitsLength.MEGAMETER, Mul(Pow(10, 12), Sym("microm")));
        c.put(UnitsLength.METER, Mul(Pow(10, 6), Sym("microm")));
        c.put(UnitsLength.MILE, Mul(Int(1609344000), Sym("microm")));
        c.put(UnitsLength.MILLIMETER, Mul(Int(1000), Sym("microm")));
        c.put(UnitsLength.NANOMETER, Mul(Rat(Int(1), Int(1000)), Sym("microm")));
        c.put(UnitsLength.PARSEC, Mul(Mul(Int(30856776), Pow(10, 15)), Sym("microm")));
        c.put(UnitsLength.PETAMETER, Mul(Pow(10, 21), Sym("microm")));
        c.put(UnitsLength.PICOMETER, Mul(Rat(Int(1), Pow(10, 6)), Sym("microm")));
        c.put(UnitsLength.POINT, Mul(Rat(Int(3175), Int(9)), Sym("microm")));
        c.put(UnitsLength.TERAMETER, Mul(Pow(10, 18), Sym("microm")));
        c.put(UnitsLength.THOU, Mul(Rat(Int(127), Int(5)), Sym("microm")));
        c.put(UnitsLength.YARD, Mul(Int(914400), Sym("microm")));
        c.put(UnitsLength.YOCTOMETER, Mul(Rat(Int(1), Pow(10, 18)), Sym("microm")));
        c.put(UnitsLength.YOTTAMETER, Mul(Pow(10, 30), Sym("microm")));
        c.put(UnitsLength.ZEPTOMETER, Mul(Rat(Int(1), Pow(10, 15)), Sym("microm")));
        c.put(UnitsLength.ZETTAMETER, Mul(Pow(10, 27), Sym("microm")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsLength, Conversion> createMapMILE() {
        EnumMap<UnitsLength, Conversion> c =
            new EnumMap<UnitsLength, Conversion>(UnitsLength.class);
        c.put(UnitsLength.ANGSTROM, Mul(Rat(Int(1), Mul(Int(1609344), Pow(10, 7))), Sym("mi")));
        c.put(UnitsLength.ASTRONOMICALUNIT, Mul(Rat(Int("1558311153125"), Int(16764)), Sym("mi")));
        c.put(UnitsLength.ATTOMETER, Mul(Rat(Int(1), Mul(Int(1609344), Pow(10, 15))), Sym("mi")));
        c.put(UnitsLength.CENTIMETER, Mul(Rat(Int(5), Int(804672)), Sym("mi")));
        c.put(UnitsLength.DECAMETER, Mul(Rat(Int(625), Int(100584)), Sym("mi")));
        c.put(UnitsLength.DECIMETER, Mul(Rat(Int(25), Int(402336)), Sym("mi")));
        c.put(UnitsLength.EXAMETER, Mul(Rat(Mul(Int(78125), Pow(10, 14)), Int(12573)), Sym("mi")));
        c.put(UnitsLength.FEMTOMETER, Mul(Rat(Int(1), Mul(Int(1609344), Pow(10, 12))), Sym("mi")));
        c.put(UnitsLength.FOOT, Mul(Rat(Int(1), Int(5280)), Sym("mi")));
        c.put(UnitsLength.GIGAMETER, Mul(Rat(Mul(Int(78125), Pow(10, 5)), Int(12573)), Sym("mi")));
        c.put(UnitsLength.HECTOMETER, Mul(Rat(Int(3125), Int(50292)), Sym("mi")));
        c.put(UnitsLength.INCH, Mul(Rat(Int(1), Int(63360)), Sym("mi")));
        c.put(UnitsLength.KILOMETER, Mul(Rat(Int(15625), Int(25146)), Sym("mi")));
        c.put(UnitsLength.LIGHTYEAR, Mul(Rat(Int("8212439646337500"), Int(1397)), Sym("mi")));
        c.put(UnitsLength.LINE, Mul(Rat(Int(1), Int(760320)), Sym("mi")));
        c.put(UnitsLength.MEGAMETER, Mul(Rat(Int(7812500), Int(12573)), Sym("mi")));
        c.put(UnitsLength.METER, Mul(Rat(Int(125), Int(201168)), Sym("mi")));
        c.put(UnitsLength.MICROMETER, Mul(Rat(Int(1), Int(1609344000)), Sym("mi")));
        c.put(UnitsLength.MILLIMETER, Mul(Rat(Int(1), Int(1609344)), Sym("mi")));
        c.put(UnitsLength.NANOMETER, Mul(Rat(Int(1), Mul(Int(1609344), Pow(10, 6))), Sym("mi")));
        c.put(UnitsLength.PARSEC, Mul(Rat(Mul(Int(803561875), Pow(10, 8)), Int(4191)), Sym("mi")));
        c.put(UnitsLength.PETAMETER, Mul(Rat(Mul(Int(78125), Pow(10, 11)), Int(12573)), Sym("mi")));
        c.put(UnitsLength.PICOMETER, Mul(Rat(Int(1), Mul(Int(1609344), Pow(10, 9))), Sym("mi")));
        c.put(UnitsLength.POINT, Mul(Rat(Int(1), Int(4561920)), Sym("mi")));
        c.put(UnitsLength.TERAMETER, Mul(Rat(Mul(Int(78125), Pow(10, 8)), Int(12573)), Sym("mi")));
        c.put(UnitsLength.THOU, Mul(Rat(Int(1), Mul(Int(6336), Pow(10, 4))), Sym("mi")));
        c.put(UnitsLength.YARD, Mul(Rat(Int(1), Int(1760)), Sym("mi")));
        c.put(UnitsLength.YOCTOMETER, Mul(Rat(Int(1), Mul(Int(1609344), Pow(10, 21))), Sym("mi")));
        c.put(UnitsLength.YOTTAMETER, Mul(Rat(Mul(Int(78125), Pow(10, 20)), Int(12573)), Sym("mi")));
        c.put(UnitsLength.ZEPTOMETER, Mul(Rat(Int(1), Mul(Int(1609344), Pow(10, 18))), Sym("mi")));
        c.put(UnitsLength.ZETTAMETER, Mul(Rat(Mul(Int(78125), Pow(10, 17)), Int(12573)), Sym("mi")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsLength, Conversion> createMapMILLIMETER() {
        EnumMap<UnitsLength, Conversion> c =
            new EnumMap<UnitsLength, Conversion>(UnitsLength.class);
        c.put(UnitsLength.ANGSTROM, Mul(Rat(Int(1), Pow(10, 7)), Sym("millim")));
        c.put(UnitsLength.ASTRONOMICALUNIT, Mul(Mul(Int(1495978707), Pow(10, 5)), Sym("millim")));
        c.put(UnitsLength.ATTOMETER, Mul(Rat(Int(1), Pow(10, 15)), Sym("millim")));
        c.put(UnitsLength.CENTIMETER, Mul(Int(10), Sym("millim")));
        c.put(UnitsLength.DECAMETER, Mul(Pow(10, 4), Sym("millim")));
        c.put(UnitsLength.DECIMETER, Mul(Int(100), Sym("millim")));
        c.put(UnitsLength.EXAMETER, Mul(Pow(10, 21), Sym("millim")));
        c.put(UnitsLength.FEMTOMETER, Mul(Rat(Int(1), Pow(10, 12)), Sym("millim")));
        c.put(UnitsLength.FOOT, Mul(Rat(Int(1524), Int(5)), Sym("millim")));
        c.put(UnitsLength.GIGAMETER, Mul(Pow(10, 12), Sym("millim")));
        c.put(UnitsLength.HECTOMETER, Mul(Pow(10, 5), Sym("millim")));
        c.put(UnitsLength.INCH, Mul(Rat(Int(127), Int(5)), Sym("millim")));
        c.put(UnitsLength.KILOMETER, Mul(Pow(10, 6), Sym("millim")));
        c.put(UnitsLength.LIGHTYEAR, Mul(Mul(Int("94607304725808"), Pow(10, 5)), Sym("millim")));
        c.put(UnitsLength.LINE, Mul(Rat(Int(127), Int(60)), Sym("millim")));
        c.put(UnitsLength.MEGAMETER, Mul(Pow(10, 9), Sym("millim")));
        c.put(UnitsLength.METER, Mul(Int(1000), Sym("millim")));
        c.put(UnitsLength.MICROMETER, Mul(Rat(Int(1), Int(1000)), Sym("millim")));
        c.put(UnitsLength.MILE, Mul(Int(1609344), Sym("millim")));
        c.put(UnitsLength.NANOMETER, Mul(Rat(Int(1), Pow(10, 6)), Sym("millim")));
        c.put(UnitsLength.PARSEC, Mul(Mul(Int(30856776), Pow(10, 12)), Sym("millim")));
        c.put(UnitsLength.PETAMETER, Mul(Pow(10, 18), Sym("millim")));
        c.put(UnitsLength.PICOMETER, Mul(Rat(Int(1), Pow(10, 9)), Sym("millim")));
        c.put(UnitsLength.POINT, Mul(Rat(Int(127), Int(360)), Sym("millim")));
        c.put(UnitsLength.TERAMETER, Mul(Pow(10, 15), Sym("millim")));
        c.put(UnitsLength.THOU, Mul(Rat(Int(127), Int(5000)), Sym("millim")));
        c.put(UnitsLength.YARD, Mul(Rat(Int(4572), Int(5)), Sym("millim")));
        c.put(UnitsLength.YOCTOMETER, Mul(Rat(Int(1), Pow(10, 21)), Sym("millim")));
        c.put(UnitsLength.YOTTAMETER, Mul(Pow(10, 27), Sym("millim")));
        c.put(UnitsLength.ZEPTOMETER, Mul(Rat(Int(1), Pow(10, 18)), Sym("millim")));
        c.put(UnitsLength.ZETTAMETER, Mul(Pow(10, 24), Sym("millim")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsLength, Conversion> createMapNANOMETER() {
        EnumMap<UnitsLength, Conversion> c =
            new EnumMap<UnitsLength, Conversion>(UnitsLength.class);
        c.put(UnitsLength.ANGSTROM, Mul(Rat(Int(1), Int(10)), Sym("nanom")));
        c.put(UnitsLength.ASTRONOMICALUNIT, Mul(Mul(Int(1495978707), Pow(10, 11)), Sym("nanom")));
        c.put(UnitsLength.ATTOMETER, Mul(Rat(Int(1), Pow(10, 9)), Sym("nanom")));
        c.put(UnitsLength.CENTIMETER, Mul(Pow(10, 7), Sym("nanom")));
        c.put(UnitsLength.DECAMETER, Mul(Pow(10, 10), Sym("nanom")));
        c.put(UnitsLength.DECIMETER, Mul(Pow(10, 8), Sym("nanom")));
        c.put(UnitsLength.EXAMETER, Mul(Pow(10, 27), Sym("nanom")));
        c.put(UnitsLength.FEMTOMETER, Mul(Rat(Int(1), Pow(10, 6)), Sym("nanom")));
        c.put(UnitsLength.FOOT, Mul(Mul(Int(3048), Pow(10, 5)), Sym("nanom")));
        c.put(UnitsLength.GIGAMETER, Mul(Pow(10, 18), Sym("nanom")));
        c.put(UnitsLength.HECTOMETER, Mul(Pow(10, 11), Sym("nanom")));
        c.put(UnitsLength.INCH, Mul(Mul(Int(254), Pow(10, 5)), Sym("nanom")));
        c.put(UnitsLength.KILOMETER, Mul(Pow(10, 12), Sym("nanom")));
        c.put(UnitsLength.LIGHTYEAR, Mul(Mul(Int("94607304725808"), Pow(10, 11)), Sym("nanom")));
        c.put(UnitsLength.LINE, Mul(Rat(Mul(Int(635), Pow(10, 4)), Int(3)), Sym("nanom")));
        c.put(UnitsLength.MEGAMETER, Mul(Pow(10, 15), Sym("nanom")));
        c.put(UnitsLength.METER, Mul(Pow(10, 9), Sym("nanom")));
        c.put(UnitsLength.MICROMETER, Mul(Int(1000), Sym("nanom")));
        c.put(UnitsLength.MILE, Mul(Mul(Int(1609344), Pow(10, 6)), Sym("nanom")));
        c.put(UnitsLength.MILLIMETER, Mul(Pow(10, 6), Sym("nanom")));
        c.put(UnitsLength.PARSEC, Mul(Mul(Int(30856776), Pow(10, 18)), Sym("nanom")));
        c.put(UnitsLength.PETAMETER, Mul(Pow(10, 24), Sym("nanom")));
        c.put(UnitsLength.PICOMETER, Mul(Rat(Int(1), Int(1000)), Sym("nanom")));
        c.put(UnitsLength.POINT, Mul(Rat(Int(3175000), Int(9)), Sym("nanom")));
        c.put(UnitsLength.TERAMETER, Mul(Pow(10, 21), Sym("nanom")));
        c.put(UnitsLength.THOU, Mul(Int(25400), Sym("nanom")));
        c.put(UnitsLength.YARD, Mul(Mul(Int(9144), Pow(10, 5)), Sym("nanom")));
        c.put(UnitsLength.YOCTOMETER, Mul(Rat(Int(1), Pow(10, 15)), Sym("nanom")));
        c.put(UnitsLength.YOTTAMETER, Mul(Pow(10, 33), Sym("nanom")));
        c.put(UnitsLength.ZEPTOMETER, Mul(Rat(Int(1), Pow(10, 12)), Sym("nanom")));
        c.put(UnitsLength.ZETTAMETER, Mul(Pow(10, 30), Sym("nanom")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsLength, Conversion> createMapPARSEC() {
        EnumMap<UnitsLength, Conversion> c =
            new EnumMap<UnitsLength, Conversion>(UnitsLength.class);
        c.put(UnitsLength.ANGSTROM, Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 19))), Sym("pc")));
        c.put(UnitsLength.ASTRONOMICALUNIT, Mul(Rat(Int(498659569), Mul(Int(10285592), Pow(10, 7))), Sym("pc")));
        c.put(UnitsLength.ATTOMETER, Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 27))), Sym("pc")));
        c.put(UnitsLength.CENTIMETER, Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 11))), Sym("pc")));
        c.put(UnitsLength.DECAMETER, Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 8))), Sym("pc")));
        c.put(UnitsLength.DECIMETER, Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 10))), Sym("pc")));
        c.put(UnitsLength.EXAMETER, Mul(Rat(Mul(Int(125), Pow(10, 6)), Int(3857097)), Sym("pc")));
        c.put(UnitsLength.FEMTOMETER, Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 24))), Sym("pc")));
        c.put(UnitsLength.FOOT, Mul(Rat(Int(127), Mul(Int(1285699), Pow(10, 13))), Sym("pc")));
        c.put(UnitsLength.GIGAMETER, Mul(Rat(Int(1), Int(30856776)), Sym("pc")));
        c.put(UnitsLength.HECTOMETER, Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 7))), Sym("pc")));
        c.put(UnitsLength.INCH, Mul(Rat(Int(127), Mul(Int(15428388), Pow(10, 13))), Sym("pc")));
        c.put(UnitsLength.KILOMETER, Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 6))), Sym("pc")));
        c.put(UnitsLength.LIGHTYEAR, Mul(Rat(Int("1970985515121"), Mul(Int(6428495), Pow(10, 6))), Sym("pc")));
        c.put(UnitsLength.LINE, Mul(Rat(Int(127), Mul(Int(185140656), Pow(10, 13))), Sym("pc")));
        c.put(UnitsLength.MEGAMETER, Mul(Rat(Int(1), Int("30856776000")), Sym("pc")));
        c.put(UnitsLength.METER, Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 9))), Sym("pc")));
        c.put(UnitsLength.MICROMETER, Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 15))), Sym("pc")));
        c.put(UnitsLength.MILE, Mul(Rat(Int(4191), Mul(Int(803561875), Pow(10, 8))), Sym("pc")));
        c.put(UnitsLength.MILLIMETER, Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 12))), Sym("pc")));
        c.put(UnitsLength.NANOMETER, Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 18))), Sym("pc")));
        c.put(UnitsLength.PETAMETER, Mul(Rat(Int(125000), Int(3857097)), Sym("pc")));
        c.put(UnitsLength.PICOMETER, Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 21))), Sym("pc")));
        c.put(UnitsLength.POINT, Mul(Rat(Int(127), Mul(Int(1110843936), Pow(10, 13))), Sym("pc")));
        c.put(UnitsLength.TERAMETER, Mul(Rat(Int(125), Int(3857097)), Sym("pc")));
        c.put(UnitsLength.THOU, Mul(Rat(Int(127), Mul(Int(15428388), Pow(10, 16))), Sym("pc")));
        c.put(UnitsLength.YARD, Mul(Rat(Int(381), Mul(Int(1285699), Pow(10, 13))), Sym("pc")));
        c.put(UnitsLength.YOCTOMETER, Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 33))), Sym("pc")));
        c.put(UnitsLength.YOTTAMETER, Mul(Rat(Mul(Int(125), Pow(10, 12)), Int(3857097)), Sym("pc")));
        c.put(UnitsLength.ZEPTOMETER, Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 30))), Sym("pc")));
        c.put(UnitsLength.ZETTAMETER, Mul(Rat(Mul(Int(125), Pow(10, 9)), Int(3857097)), Sym("pc")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsLength, Conversion> createMapPETAMETER() {
        EnumMap<UnitsLength, Conversion> c =
            new EnumMap<UnitsLength, Conversion>(UnitsLength.class);
        c.put(UnitsLength.ANGSTROM, Mul(Rat(Int(1), Pow(10, 25)), Sym("petam")));
        c.put(UnitsLength.ASTRONOMICALUNIT, Mul(Rat(Int(1495978707), Pow(10, 13)), Sym("petam")));
        c.put(UnitsLength.ATTOMETER, Mul(Rat(Int(1), Pow(10, 33)), Sym("petam")));
        c.put(UnitsLength.CENTIMETER, Mul(Rat(Int(1), Pow(10, 17)), Sym("petam")));
        c.put(UnitsLength.DECAMETER, Mul(Rat(Int(1), Pow(10, 14)), Sym("petam")));
        c.put(UnitsLength.DECIMETER, Mul(Rat(Int(1), Pow(10, 16)), Sym("petam")));
        c.put(UnitsLength.EXAMETER, Mul(Int(1000), Sym("petam")));
        c.put(UnitsLength.FEMTOMETER, Mul(Rat(Int(1), Pow(10, 30)), Sym("petam")));
        c.put(UnitsLength.FOOT, Mul(Rat(Int(381), Mul(Int(125), Pow(10, 16))), Sym("petam")));
        c.put(UnitsLength.GIGAMETER, Mul(Rat(Int(1), Pow(10, 6)), Sym("petam")));
        c.put(UnitsLength.HECTOMETER, Mul(Rat(Int(1), Pow(10, 13)), Sym("petam")));
        c.put(UnitsLength.INCH, Mul(Rat(Int(127), Mul(Int(5), Pow(10, 18))), Sym("petam")));
        c.put(UnitsLength.KILOMETER, Mul(Rat(Int(1), Pow(10, 12)), Sym("petam")));
        c.put(UnitsLength.LIGHTYEAR, Mul(Rat(Int("5912956545363"), Mul(Int(625), Pow(10, 9))), Sym("petam")));
        c.put(UnitsLength.LINE, Mul(Rat(Int(127), Mul(Int(6), Pow(10, 19))), Sym("petam")));
        c.put(UnitsLength.MEGAMETER, Mul(Rat(Int(1), Pow(10, 9)), Sym("petam")));
        c.put(UnitsLength.METER, Mul(Rat(Int(1), Pow(10, 15)), Sym("petam")));
        c.put(UnitsLength.MICROMETER, Mul(Rat(Int(1), Pow(10, 21)), Sym("petam")));
        c.put(UnitsLength.MILE, Mul(Rat(Int(12573), Mul(Int(78125), Pow(10, 11))), Sym("petam")));
        c.put(UnitsLength.MILLIMETER, Mul(Rat(Int(1), Pow(10, 18)), Sym("petam")));
        c.put(UnitsLength.NANOMETER, Mul(Rat(Int(1), Pow(10, 24)), Sym("petam")));
        c.put(UnitsLength.PARSEC, Mul(Rat(Int(3857097), Int(125000)), Sym("petam")));
        c.put(UnitsLength.PICOMETER, Mul(Rat(Int(1), Pow(10, 27)), Sym("petam")));
        c.put(UnitsLength.POINT, Mul(Rat(Int(127), Mul(Int(36), Pow(10, 19))), Sym("petam")));
        c.put(UnitsLength.TERAMETER, Mul(Rat(Int(1), Int(1000)), Sym("petam")));
        c.put(UnitsLength.THOU, Mul(Rat(Int(127), Mul(Int(5), Pow(10, 21))), Sym("petam")));
        c.put(UnitsLength.YARD, Mul(Rat(Int(1143), Mul(Int(125), Pow(10, 16))), Sym("petam")));
        c.put(UnitsLength.YOCTOMETER, Mul(Rat(Int(1), Pow(10, 39)), Sym("petam")));
        c.put(UnitsLength.YOTTAMETER, Mul(Pow(10, 9), Sym("petam")));
        c.put(UnitsLength.ZEPTOMETER, Mul(Rat(Int(1), Pow(10, 36)), Sym("petam")));
        c.put(UnitsLength.ZETTAMETER, Mul(Pow(10, 6), Sym("petam")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsLength, Conversion> createMapPICOMETER() {
        EnumMap<UnitsLength, Conversion> c =
            new EnumMap<UnitsLength, Conversion>(UnitsLength.class);
        c.put(UnitsLength.ANGSTROM, Mul(Int(100), Sym("picom")));
        c.put(UnitsLength.ASTRONOMICALUNIT, Mul(Mul(Int(1495978707), Pow(10, 14)), Sym("picom")));
        c.put(UnitsLength.ATTOMETER, Mul(Rat(Int(1), Pow(10, 6)), Sym("picom")));
        c.put(UnitsLength.CENTIMETER, Mul(Pow(10, 10), Sym("picom")));
        c.put(UnitsLength.DECAMETER, Mul(Pow(10, 13), Sym("picom")));
        c.put(UnitsLength.DECIMETER, Mul(Pow(10, 11), Sym("picom")));
        c.put(UnitsLength.EXAMETER, Mul(Pow(10, 30), Sym("picom")));
        c.put(UnitsLength.FEMTOMETER, Mul(Rat(Int(1), Int(1000)), Sym("picom")));
        c.put(UnitsLength.FOOT, Mul(Mul(Int(3048), Pow(10, 8)), Sym("picom")));
        c.put(UnitsLength.GIGAMETER, Mul(Pow(10, 21), Sym("picom")));
        c.put(UnitsLength.HECTOMETER, Mul(Pow(10, 14), Sym("picom")));
        c.put(UnitsLength.INCH, Mul(Mul(Int(254), Pow(10, 8)), Sym("picom")));
        c.put(UnitsLength.KILOMETER, Mul(Pow(10, 15), Sym("picom")));
        c.put(UnitsLength.LIGHTYEAR, Mul(Mul(Int("94607304725808"), Pow(10, 14)), Sym("picom")));
        c.put(UnitsLength.LINE, Mul(Rat(Mul(Int(635), Pow(10, 7)), Int(3)), Sym("picom")));
        c.put(UnitsLength.MEGAMETER, Mul(Pow(10, 18), Sym("picom")));
        c.put(UnitsLength.METER, Mul(Pow(10, 12), Sym("picom")));
        c.put(UnitsLength.MICROMETER, Mul(Pow(10, 6), Sym("picom")));
        c.put(UnitsLength.MILE, Mul(Mul(Int(1609344), Pow(10, 9)), Sym("picom")));
        c.put(UnitsLength.MILLIMETER, Mul(Pow(10, 9), Sym("picom")));
        c.put(UnitsLength.NANOMETER, Mul(Int(1000), Sym("picom")));
        c.put(UnitsLength.PARSEC, Mul(Mul(Int(30856776), Pow(10, 21)), Sym("picom")));
        c.put(UnitsLength.PETAMETER, Mul(Pow(10, 27), Sym("picom")));
        c.put(UnitsLength.POINT, Mul(Rat(Mul(Int(3175), Pow(10, 6)), Int(9)), Sym("picom")));
        c.put(UnitsLength.TERAMETER, Mul(Pow(10, 24), Sym("picom")));
        c.put(UnitsLength.THOU, Mul(Mul(Int(254), Pow(10, 5)), Sym("picom")));
        c.put(UnitsLength.YARD, Mul(Mul(Int(9144), Pow(10, 8)), Sym("picom")));
        c.put(UnitsLength.YOCTOMETER, Mul(Rat(Int(1), Pow(10, 12)), Sym("picom")));
        c.put(UnitsLength.YOTTAMETER, Mul(Pow(10, 36), Sym("picom")));
        c.put(UnitsLength.ZEPTOMETER, Mul(Rat(Int(1), Pow(10, 9)), Sym("picom")));
        c.put(UnitsLength.ZETTAMETER, Mul(Pow(10, 33), Sym("picom")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsLength, Conversion> createMapPOINT() {
        EnumMap<UnitsLength, Conversion> c =
            new EnumMap<UnitsLength, Conversion>(UnitsLength.class);
        c.put(UnitsLength.ANGSTROM, Mul(Rat(Int(9), Mul(Int(3175), Pow(10, 4))), Sym("pt")));
        c.put(UnitsLength.ASTRONOMICALUNIT, Mul(Rat(Mul(Int("53855233452"), Pow(10, 6)), Int(127)), Sym("pt")));
        c.put(UnitsLength.ATTOMETER, Mul(Rat(Int(9), Mul(Int(3175), Pow(10, 12))), Sym("pt")));
        c.put(UnitsLength.CENTIMETER, Mul(Rat(Int(3600), Int(127)), Sym("pt")));
        c.put(UnitsLength.DECAMETER, Mul(Rat(Mul(Int(36), Pow(10, 5)), Int(127)), Sym("pt")));
        c.put(UnitsLength.DECIMETER, Mul(Rat(Int(36000), Int(127)), Sym("pt")));
        c.put(UnitsLength.EXAMETER, Mul(Rat(Mul(Int(36), Pow(10, 22)), Int(127)), Sym("pt")));
        c.put(UnitsLength.FEMTOMETER, Mul(Rat(Int(9), Mul(Int(3175), Pow(10, 9))), Sym("pt")));
        c.put(UnitsLength.FOOT, Mul(Int(864), Sym("pt")));
        c.put(UnitsLength.GIGAMETER, Mul(Rat(Mul(Int(36), Pow(10, 13)), Int(127)), Sym("pt")));
        c.put(UnitsLength.HECTOMETER, Mul(Rat(Mul(Int(36), Pow(10, 6)), Int(127)), Sym("pt")));
        c.put(UnitsLength.INCH, Mul(Int(72), Sym("pt")));
        c.put(UnitsLength.KILOMETER, Mul(Rat(Mul(Int(36), Pow(10, 7)), Int(127)), Sym("pt")));
        c.put(UnitsLength.LIGHTYEAR, Mul(Rat(Mul(Int("3405862970129088"), Pow(10, 6)), Int(127)), Sym("pt")));
        c.put(UnitsLength.LINE, Mul(Int(6), Sym("pt")));
        c.put(UnitsLength.MEGAMETER, Mul(Rat(Mul(Int(36), Pow(10, 10)), Int(127)), Sym("pt")));
        c.put(UnitsLength.METER, Mul(Rat(Mul(Int(36), Pow(10, 4)), Int(127)), Sym("pt")));
        c.put(UnitsLength.MICROMETER, Mul(Rat(Int(9), Int(3175)), Sym("pt")));
        c.put(UnitsLength.MILE, Mul(Int(4561920), Sym("pt")));
        c.put(UnitsLength.MILLIMETER, Mul(Rat(Int(360), Int(127)), Sym("pt")));
        c.put(UnitsLength.NANOMETER, Mul(Rat(Int(9), Int(3175000)), Sym("pt")));
        c.put(UnitsLength.PARSEC, Mul(Rat(Mul(Int(1110843936), Pow(10, 13)), Int(127)), Sym("pt")));
        c.put(UnitsLength.PETAMETER, Mul(Rat(Mul(Int(36), Pow(10, 19)), Int(127)), Sym("pt")));
        c.put(UnitsLength.PICOMETER, Mul(Rat(Int(9), Mul(Int(3175), Pow(10, 6))), Sym("pt")));
        c.put(UnitsLength.TERAMETER, Mul(Rat(Mul(Int(36), Pow(10, 16)), Int(127)), Sym("pt")));
        c.put(UnitsLength.THOU, Mul(Rat(Int(9), Int(125)), Sym("pt")));
        c.put(UnitsLength.YARD, Mul(Int(2592), Sym("pt")));
        c.put(UnitsLength.YOCTOMETER, Mul(Rat(Int(9), Mul(Int(3175), Pow(10, 18))), Sym("pt")));
        c.put(UnitsLength.YOTTAMETER, Mul(Rat(Mul(Int(36), Pow(10, 28)), Int(127)), Sym("pt")));
        c.put(UnitsLength.ZEPTOMETER, Mul(Rat(Int(9), Mul(Int(3175), Pow(10, 15))), Sym("pt")));
        c.put(UnitsLength.ZETTAMETER, Mul(Rat(Mul(Int(36), Pow(10, 25)), Int(127)), Sym("pt")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsLength, Conversion> createMapTERAMETER() {
        EnumMap<UnitsLength, Conversion> c =
            new EnumMap<UnitsLength, Conversion>(UnitsLength.class);
        c.put(UnitsLength.ANGSTROM, Mul(Rat(Int(1), Pow(10, 22)), Sym("teram")));
        c.put(UnitsLength.ASTRONOMICALUNIT, Mul(Rat(Int(1495978707), Pow(10, 10)), Sym("teram")));
        c.put(UnitsLength.ATTOMETER, Mul(Rat(Int(1), Pow(10, 30)), Sym("teram")));
        c.put(UnitsLength.CENTIMETER, Mul(Rat(Int(1), Pow(10, 14)), Sym("teram")));
        c.put(UnitsLength.DECAMETER, Mul(Rat(Int(1), Pow(10, 11)), Sym("teram")));
        c.put(UnitsLength.DECIMETER, Mul(Rat(Int(1), Pow(10, 13)), Sym("teram")));
        c.put(UnitsLength.EXAMETER, Mul(Pow(10, 6), Sym("teram")));
        c.put(UnitsLength.FEMTOMETER, Mul(Rat(Int(1), Pow(10, 27)), Sym("teram")));
        c.put(UnitsLength.FOOT, Mul(Rat(Int(381), Mul(Int(125), Pow(10, 13))), Sym("teram")));
        c.put(UnitsLength.GIGAMETER, Mul(Rat(Int(1), Int(1000)), Sym("teram")));
        c.put(UnitsLength.HECTOMETER, Mul(Rat(Int(1), Pow(10, 10)), Sym("teram")));
        c.put(UnitsLength.INCH, Mul(Rat(Int(127), Mul(Int(5), Pow(10, 15))), Sym("teram")));
        c.put(UnitsLength.KILOMETER, Mul(Rat(Int(1), Pow(10, 9)), Sym("teram")));
        c.put(UnitsLength.LIGHTYEAR, Mul(Rat(Int("5912956545363"), Mul(Int(625), Pow(10, 6))), Sym("teram")));
        c.put(UnitsLength.LINE, Mul(Rat(Int(127), Mul(Int(6), Pow(10, 16))), Sym("teram")));
        c.put(UnitsLength.MEGAMETER, Mul(Rat(Int(1), Pow(10, 6)), Sym("teram")));
        c.put(UnitsLength.METER, Mul(Rat(Int(1), Pow(10, 12)), Sym("teram")));
        c.put(UnitsLength.MICROMETER, Mul(Rat(Int(1), Pow(10, 18)), Sym("teram")));
        c.put(UnitsLength.MILE, Mul(Rat(Int(12573), Mul(Int(78125), Pow(10, 8))), Sym("teram")));
        c.put(UnitsLength.MILLIMETER, Mul(Rat(Int(1), Pow(10, 15)), Sym("teram")));
        c.put(UnitsLength.NANOMETER, Mul(Rat(Int(1), Pow(10, 21)), Sym("teram")));
        c.put(UnitsLength.PARSEC, Mul(Rat(Int(3857097), Int(125)), Sym("teram")));
        c.put(UnitsLength.PETAMETER, Mul(Int(1000), Sym("teram")));
        c.put(UnitsLength.PICOMETER, Mul(Rat(Int(1), Pow(10, 24)), Sym("teram")));
        c.put(UnitsLength.POINT, Mul(Rat(Int(127), Mul(Int(36), Pow(10, 16))), Sym("teram")));
        c.put(UnitsLength.THOU, Mul(Rat(Int(127), Mul(Int(5), Pow(10, 18))), Sym("teram")));
        c.put(UnitsLength.YARD, Mul(Rat(Int(1143), Mul(Int(125), Pow(10, 13))), Sym("teram")));
        c.put(UnitsLength.YOCTOMETER, Mul(Rat(Int(1), Pow(10, 36)), Sym("teram")));
        c.put(UnitsLength.YOTTAMETER, Mul(Pow(10, 12), Sym("teram")));
        c.put(UnitsLength.ZEPTOMETER, Mul(Rat(Int(1), Pow(10, 33)), Sym("teram")));
        c.put(UnitsLength.ZETTAMETER, Mul(Pow(10, 9), Sym("teram")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsLength, Conversion> createMapTHOU() {
        EnumMap<UnitsLength, Conversion> c =
            new EnumMap<UnitsLength, Conversion>(UnitsLength.class);
        c.put(UnitsLength.ANGSTROM, Mul(Rat(Int(1), Int(254000)), Sym("thou")));
        c.put(UnitsLength.ASTRONOMICALUNIT, Mul(Rat(Mul(Int("7479893535"), Pow(10, 8)), Int(127)), Sym("thou")));
        c.put(UnitsLength.ATTOMETER, Mul(Rat(Int(1), Mul(Int(254), Pow(10, 11))), Sym("thou")));
        c.put(UnitsLength.CENTIMETER, Mul(Rat(Mul(Int(5), Pow(10, 4)), Int(127)), Sym("thou")));
        c.put(UnitsLength.DECAMETER, Mul(Rat(Mul(Int(5), Pow(10, 7)), Int(127)), Sym("thou")));
        c.put(UnitsLength.DECIMETER, Mul(Rat(Mul(Int(5), Pow(10, 5)), Int(127)), Sym("thou")));
        c.put(UnitsLength.EXAMETER, Mul(Rat(Mul(Int(5), Pow(10, 24)), Int(127)), Sym("thou")));
        c.put(UnitsLength.FEMTOMETER, Mul(Rat(Int(1), Mul(Int(254), Pow(10, 8))), Sym("thou")));
        c.put(UnitsLength.FOOT, Mul(Int(12000), Sym("thou")));
        c.put(UnitsLength.GIGAMETER, Mul(Rat(Mul(Int(5), Pow(10, 15)), Int(127)), Sym("thou")));
        c.put(UnitsLength.HECTOMETER, Mul(Rat(Mul(Int(5), Pow(10, 8)), Int(127)), Sym("thou")));
        c.put(UnitsLength.INCH, Mul(Int(1000), Sym("thou")));
        c.put(UnitsLength.KILOMETER, Mul(Rat(Mul(Int(5), Pow(10, 9)), Int(127)), Sym("thou")));
        c.put(UnitsLength.LIGHTYEAR, Mul(Rat(Mul(Int("47303652362904"), Pow(10, 9)), Int(127)), Sym("thou")));
        c.put(UnitsLength.LINE, Mul(Rat(Int(250), Int(3)), Sym("thou")));
        c.put(UnitsLength.MEGAMETER, Mul(Rat(Mul(Int(5), Pow(10, 12)), Int(127)), Sym("thou")));
        c.put(UnitsLength.METER, Mul(Rat(Mul(Int(5), Pow(10, 6)), Int(127)), Sym("thou")));
        c.put(UnitsLength.MICROMETER, Mul(Rat(Int(5), Int(127)), Sym("thou")));
        c.put(UnitsLength.MILE, Mul(Mul(Int(6336), Pow(10, 4)), Sym("thou")));
        c.put(UnitsLength.MILLIMETER, Mul(Rat(Int(5000), Int(127)), Sym("thou")));
        c.put(UnitsLength.NANOMETER, Mul(Rat(Int(1), Int(25400)), Sym("thou")));
        c.put(UnitsLength.PARSEC, Mul(Rat(Mul(Int(15428388), Pow(10, 16)), Int(127)), Sym("thou")));
        c.put(UnitsLength.PETAMETER, Mul(Rat(Mul(Int(5), Pow(10, 21)), Int(127)), Sym("thou")));
        c.put(UnitsLength.PICOMETER, Mul(Rat(Int(1), Mul(Int(254), Pow(10, 5))), Sym("thou")));
        c.put(UnitsLength.POINT, Mul(Rat(Int(125), Int(9)), Sym("thou")));
        c.put(UnitsLength.TERAMETER, Mul(Rat(Mul(Int(5), Pow(10, 18)), Int(127)), Sym("thou")));
        c.put(UnitsLength.YARD, Mul(Int(36000), Sym("thou")));
        c.put(UnitsLength.YOCTOMETER, Mul(Rat(Int(1), Mul(Int(254), Pow(10, 17))), Sym("thou")));
        c.put(UnitsLength.YOTTAMETER, Mul(Rat(Mul(Int(5), Pow(10, 30)), Int(127)), Sym("thou")));
        c.put(UnitsLength.ZEPTOMETER, Mul(Rat(Int(1), Mul(Int(254), Pow(10, 14))), Sym("thou")));
        c.put(UnitsLength.ZETTAMETER, Mul(Rat(Mul(Int(5), Pow(10, 27)), Int(127)), Sym("thou")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsLength, Conversion> createMapYARD() {
        EnumMap<UnitsLength, Conversion> c =
            new EnumMap<UnitsLength, Conversion>(UnitsLength.class);
        c.put(UnitsLength.ANGSTROM, Mul(Rat(Int(1), Mul(Int(9144), Pow(10, 6))), Sym("yd")));
        c.put(UnitsLength.ASTRONOMICALUNIT, Mul(Rat(Int("62332446125000"), Int(381)), Sym("yd")));
        c.put(UnitsLength.ATTOMETER, Mul(Rat(Int(1), Mul(Int(9144), Pow(10, 14))), Sym("yd")));
        c.put(UnitsLength.CENTIMETER, Mul(Rat(Int(25), Int(2286)), Sym("yd")));
        c.put(UnitsLength.DECAMETER, Mul(Rat(Int(12500), Int(1143)), Sym("yd")));
        c.put(UnitsLength.DECIMETER, Mul(Rat(Int(125), Int(1143)), Sym("yd")));
        c.put(UnitsLength.EXAMETER, Mul(Rat(Mul(Int(125), Pow(10, 19)), Int(1143)), Sym("yd")));
        c.put(UnitsLength.FEMTOMETER, Mul(Rat(Int(1), Mul(Int(9144), Pow(10, 11))), Sym("yd")));
        c.put(UnitsLength.FOOT, Mul(Rat(Int(1), Int(3)), Sym("yd")));
        c.put(UnitsLength.GIGAMETER, Mul(Rat(Mul(Int(125), Pow(10, 10)), Int(1143)), Sym("yd")));
        c.put(UnitsLength.HECTOMETER, Mul(Rat(Int(125000), Int(1143)), Sym("yd")));
        c.put(UnitsLength.INCH, Mul(Rat(Int(1), Int(36)), Sym("yd")));
        c.put(UnitsLength.KILOMETER, Mul(Rat(Mul(Int(125), Pow(10, 4)), Int(1143)), Sym("yd")));
        c.put(UnitsLength.LIGHTYEAR, Mul(Rat(Mul(Int("1313990343414"), Pow(10, 6)), Int(127)), Sym("yd")));
        c.put(UnitsLength.LINE, Mul(Rat(Int(1), Int(432)), Sym("yd")));
        c.put(UnitsLength.MEGAMETER, Mul(Rat(Mul(Int(125), Pow(10, 7)), Int(1143)), Sym("yd")));
        c.put(UnitsLength.METER, Mul(Rat(Int(1250), Int(1143)), Sym("yd")));
        c.put(UnitsLength.MICROMETER, Mul(Rat(Int(1), Int(914400)), Sym("yd")));
        c.put(UnitsLength.MILE, Mul(Int(1760), Sym("yd")));
        c.put(UnitsLength.MILLIMETER, Mul(Rat(Int(5), Int(4572)), Sym("yd")));
        c.put(UnitsLength.NANOMETER, Mul(Rat(Int(1), Mul(Int(9144), Pow(10, 5))), Sym("yd")));
        c.put(UnitsLength.PARSEC, Mul(Rat(Mul(Int(1285699), Pow(10, 13)), Int(381)), Sym("yd")));
        c.put(UnitsLength.PETAMETER, Mul(Rat(Mul(Int(125), Pow(10, 16)), Int(1143)), Sym("yd")));
        c.put(UnitsLength.PICOMETER, Mul(Rat(Int(1), Mul(Int(9144), Pow(10, 8))), Sym("yd")));
        c.put(UnitsLength.POINT, Mul(Rat(Int(1), Int(2592)), Sym("yd")));
        c.put(UnitsLength.TERAMETER, Mul(Rat(Mul(Int(125), Pow(10, 13)), Int(1143)), Sym("yd")));
        c.put(UnitsLength.THOU, Mul(Rat(Int(1), Int(36000)), Sym("yd")));
        c.put(UnitsLength.YOCTOMETER, Mul(Rat(Int(1), Mul(Int(9144), Pow(10, 20))), Sym("yd")));
        c.put(UnitsLength.YOTTAMETER, Mul(Rat(Mul(Int(125), Pow(10, 25)), Int(1143)), Sym("yd")));
        c.put(UnitsLength.ZEPTOMETER, Mul(Rat(Int(1), Mul(Int(9144), Pow(10, 17))), Sym("yd")));
        c.put(UnitsLength.ZETTAMETER, Mul(Rat(Mul(Int(125), Pow(10, 22)), Int(1143)), Sym("yd")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsLength, Conversion> createMapYOCTOMETER() {
        EnumMap<UnitsLength, Conversion> c =
            new EnumMap<UnitsLength, Conversion>(UnitsLength.class);
        c.put(UnitsLength.ANGSTROM, Mul(Pow(10, 14), Sym("yoctom")));
        c.put(UnitsLength.ASTRONOMICALUNIT, Mul(Mul(Int(1495978707), Pow(10, 26)), Sym("yoctom")));
        c.put(UnitsLength.ATTOMETER, Mul(Pow(10, 6), Sym("yoctom")));
        c.put(UnitsLength.CENTIMETER, Mul(Pow(10, 22), Sym("yoctom")));
        c.put(UnitsLength.DECAMETER, Mul(Pow(10, 25), Sym("yoctom")));
        c.put(UnitsLength.DECIMETER, Mul(Pow(10, 23), Sym("yoctom")));
        c.put(UnitsLength.EXAMETER, Mul(Pow(10, 42), Sym("yoctom")));
        c.put(UnitsLength.FEMTOMETER, Mul(Pow(10, 9), Sym("yoctom")));
        c.put(UnitsLength.FOOT, Mul(Mul(Int(3048), Pow(10, 20)), Sym("yoctom")));
        c.put(UnitsLength.GIGAMETER, Mul(Pow(10, 33), Sym("yoctom")));
        c.put(UnitsLength.HECTOMETER, Mul(Pow(10, 26), Sym("yoctom")));
        c.put(UnitsLength.INCH, Mul(Mul(Int(254), Pow(10, 20)), Sym("yoctom")));
        c.put(UnitsLength.KILOMETER, Mul(Pow(10, 27), Sym("yoctom")));
        c.put(UnitsLength.LIGHTYEAR, Mul(Mul(Int("94607304725808"), Pow(10, 26)), Sym("yoctom")));
        c.put(UnitsLength.LINE, Mul(Rat(Mul(Int(635), Pow(10, 19)), Int(3)), Sym("yoctom")));
        c.put(UnitsLength.MEGAMETER, Mul(Pow(10, 30), Sym("yoctom")));
        c.put(UnitsLength.METER, Mul(Pow(10, 24), Sym("yoctom")));
        c.put(UnitsLength.MICROMETER, Mul(Pow(10, 18), Sym("yoctom")));
        c.put(UnitsLength.MILE, Mul(Mul(Int(1609344), Pow(10, 21)), Sym("yoctom")));
        c.put(UnitsLength.MILLIMETER, Mul(Pow(10, 21), Sym("yoctom")));
        c.put(UnitsLength.NANOMETER, Mul(Pow(10, 15), Sym("yoctom")));
        c.put(UnitsLength.PARSEC, Mul(Mul(Int(30856776), Pow(10, 33)), Sym("yoctom")));
        c.put(UnitsLength.PETAMETER, Mul(Pow(10, 39), Sym("yoctom")));
        c.put(UnitsLength.PICOMETER, Mul(Pow(10, 12), Sym("yoctom")));
        c.put(UnitsLength.POINT, Mul(Rat(Mul(Int(3175), Pow(10, 18)), Int(9)), Sym("yoctom")));
        c.put(UnitsLength.TERAMETER, Mul(Pow(10, 36), Sym("yoctom")));
        c.put(UnitsLength.THOU, Mul(Mul(Int(254), Pow(10, 17)), Sym("yoctom")));
        c.put(UnitsLength.YARD, Mul(Mul(Int(9144), Pow(10, 20)), Sym("yoctom")));
        c.put(UnitsLength.YOTTAMETER, Mul(Pow(10, 48), Sym("yoctom")));
        c.put(UnitsLength.ZEPTOMETER, Mul(Int(1000), Sym("yoctom")));
        c.put(UnitsLength.ZETTAMETER, Mul(Pow(10, 45), Sym("yoctom")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsLength, Conversion> createMapYOTTAMETER() {
        EnumMap<UnitsLength, Conversion> c =
            new EnumMap<UnitsLength, Conversion>(UnitsLength.class);
        c.put(UnitsLength.ANGSTROM, Mul(Rat(Int(1), Pow(10, 34)), Sym("yottam")));
        c.put(UnitsLength.ASTRONOMICALUNIT, Mul(Rat(Int(1495978707), Pow(10, 22)), Sym("yottam")));
        c.put(UnitsLength.ATTOMETER, Mul(Rat(Int(1), Pow(10, 42)), Sym("yottam")));
        c.put(UnitsLength.CENTIMETER, Mul(Rat(Int(1), Pow(10, 26)), Sym("yottam")));
        c.put(UnitsLength.DECAMETER, Mul(Rat(Int(1), Pow(10, 23)), Sym("yottam")));
        c.put(UnitsLength.DECIMETER, Mul(Rat(Int(1), Pow(10, 25)), Sym("yottam")));
        c.put(UnitsLength.EXAMETER, Mul(Rat(Int(1), Pow(10, 6)), Sym("yottam")));
        c.put(UnitsLength.FEMTOMETER, Mul(Rat(Int(1), Pow(10, 39)), Sym("yottam")));
        c.put(UnitsLength.FOOT, Mul(Rat(Int(381), Mul(Int(125), Pow(10, 25))), Sym("yottam")));
        c.put(UnitsLength.GIGAMETER, Mul(Rat(Int(1), Pow(10, 15)), Sym("yottam")));
        c.put(UnitsLength.HECTOMETER, Mul(Rat(Int(1), Pow(10, 22)), Sym("yottam")));
        c.put(UnitsLength.INCH, Mul(Rat(Int(127), Mul(Int(5), Pow(10, 27))), Sym("yottam")));
        c.put(UnitsLength.KILOMETER, Mul(Rat(Int(1), Pow(10, 21)), Sym("yottam")));
        c.put(UnitsLength.LIGHTYEAR, Mul(Rat(Int("5912956545363"), Mul(Int(625), Pow(10, 18))), Sym("yottam")));
        c.put(UnitsLength.LINE, Mul(Rat(Int(127), Mul(Int(6), Pow(10, 28))), Sym("yottam")));
        c.put(UnitsLength.MEGAMETER, Mul(Rat(Int(1), Pow(10, 18)), Sym("yottam")));
        c.put(UnitsLength.METER, Mul(Rat(Int(1), Pow(10, 24)), Sym("yottam")));
        c.put(UnitsLength.MICROMETER, Mul(Rat(Int(1), Pow(10, 30)), Sym("yottam")));
        c.put(UnitsLength.MILE, Mul(Rat(Int(12573), Mul(Int(78125), Pow(10, 20))), Sym("yottam")));
        c.put(UnitsLength.MILLIMETER, Mul(Rat(Int(1), Pow(10, 27)), Sym("yottam")));
        c.put(UnitsLength.NANOMETER, Mul(Rat(Int(1), Pow(10, 33)), Sym("yottam")));
        c.put(UnitsLength.PARSEC, Mul(Rat(Int(3857097), Mul(Int(125), Pow(10, 12))), Sym("yottam")));
        c.put(UnitsLength.PETAMETER, Mul(Rat(Int(1), Pow(10, 9)), Sym("yottam")));
        c.put(UnitsLength.PICOMETER, Mul(Rat(Int(1), Pow(10, 36)), Sym("yottam")));
        c.put(UnitsLength.POINT, Mul(Rat(Int(127), Mul(Int(36), Pow(10, 28))), Sym("yottam")));
        c.put(UnitsLength.TERAMETER, Mul(Rat(Int(1), Pow(10, 12)), Sym("yottam")));
        c.put(UnitsLength.THOU, Mul(Rat(Int(127), Mul(Int(5), Pow(10, 30))), Sym("yottam")));
        c.put(UnitsLength.YARD, Mul(Rat(Int(1143), Mul(Int(125), Pow(10, 25))), Sym("yottam")));
        c.put(UnitsLength.YOCTOMETER, Mul(Rat(Int(1), Pow(10, 48)), Sym("yottam")));
        c.put(UnitsLength.ZEPTOMETER, Mul(Rat(Int(1), Pow(10, 45)), Sym("yottam")));
        c.put(UnitsLength.ZETTAMETER, Mul(Rat(Int(1), Int(1000)), Sym("yottam")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsLength, Conversion> createMapZEPTOMETER() {
        EnumMap<UnitsLength, Conversion> c =
            new EnumMap<UnitsLength, Conversion>(UnitsLength.class);
        c.put(UnitsLength.ANGSTROM, Mul(Pow(10, 11), Sym("zeptom")));
        c.put(UnitsLength.ASTRONOMICALUNIT, Mul(Mul(Int(1495978707), Pow(10, 23)), Sym("zeptom")));
        c.put(UnitsLength.ATTOMETER, Mul(Int(1000), Sym("zeptom")));
        c.put(UnitsLength.CENTIMETER, Mul(Pow(10, 19), Sym("zeptom")));
        c.put(UnitsLength.DECAMETER, Mul(Pow(10, 22), Sym("zeptom")));
        c.put(UnitsLength.DECIMETER, Mul(Pow(10, 20), Sym("zeptom")));
        c.put(UnitsLength.EXAMETER, Mul(Pow(10, 39), Sym("zeptom")));
        c.put(UnitsLength.FEMTOMETER, Mul(Pow(10, 6), Sym("zeptom")));
        c.put(UnitsLength.FOOT, Mul(Mul(Int(3048), Pow(10, 17)), Sym("zeptom")));
        c.put(UnitsLength.GIGAMETER, Mul(Pow(10, 30), Sym("zeptom")));
        c.put(UnitsLength.HECTOMETER, Mul(Pow(10, 23), Sym("zeptom")));
        c.put(UnitsLength.INCH, Mul(Mul(Int(254), Pow(10, 17)), Sym("zeptom")));
        c.put(UnitsLength.KILOMETER, Mul(Pow(10, 24), Sym("zeptom")));
        c.put(UnitsLength.LIGHTYEAR, Mul(Mul(Int("94607304725808"), Pow(10, 23)), Sym("zeptom")));
        c.put(UnitsLength.LINE, Mul(Rat(Mul(Int(635), Pow(10, 16)), Int(3)), Sym("zeptom")));
        c.put(UnitsLength.MEGAMETER, Mul(Pow(10, 27), Sym("zeptom")));
        c.put(UnitsLength.METER, Mul(Pow(10, 21), Sym("zeptom")));
        c.put(UnitsLength.MICROMETER, Mul(Pow(10, 15), Sym("zeptom")));
        c.put(UnitsLength.MILE, Mul(Mul(Int(1609344), Pow(10, 18)), Sym("zeptom")));
        c.put(UnitsLength.MILLIMETER, Mul(Pow(10, 18), Sym("zeptom")));
        c.put(UnitsLength.NANOMETER, Mul(Pow(10, 12), Sym("zeptom")));
        c.put(UnitsLength.PARSEC, Mul(Mul(Int(30856776), Pow(10, 30)), Sym("zeptom")));
        c.put(UnitsLength.PETAMETER, Mul(Pow(10, 36), Sym("zeptom")));
        c.put(UnitsLength.PICOMETER, Mul(Pow(10, 9), Sym("zeptom")));
        c.put(UnitsLength.POINT, Mul(Rat(Mul(Int(3175), Pow(10, 15)), Int(9)), Sym("zeptom")));
        c.put(UnitsLength.TERAMETER, Mul(Pow(10, 33), Sym("zeptom")));
        c.put(UnitsLength.THOU, Mul(Mul(Int(254), Pow(10, 14)), Sym("zeptom")));
        c.put(UnitsLength.YARD, Mul(Mul(Int(9144), Pow(10, 17)), Sym("zeptom")));
        c.put(UnitsLength.YOCTOMETER, Mul(Rat(Int(1), Int(1000)), Sym("zeptom")));
        c.put(UnitsLength.YOTTAMETER, Mul(Pow(10, 45), Sym("zeptom")));
        c.put(UnitsLength.ZETTAMETER, Mul(Pow(10, 42), Sym("zeptom")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsLength, Conversion> createMapZETTAMETER() {
        EnumMap<UnitsLength, Conversion> c =
            new EnumMap<UnitsLength, Conversion>(UnitsLength.class);
        c.put(UnitsLength.ANGSTROM, Mul(Rat(Int(1), Pow(10, 31)), Sym("zettam")));
        c.put(UnitsLength.ASTRONOMICALUNIT, Mul(Rat(Int(1495978707), Pow(10, 19)), Sym("zettam")));
        c.put(UnitsLength.ATTOMETER, Mul(Rat(Int(1), Pow(10, 39)), Sym("zettam")));
        c.put(UnitsLength.CENTIMETER, Mul(Rat(Int(1), Pow(10, 23)), Sym("zettam")));
        c.put(UnitsLength.DECAMETER, Mul(Rat(Int(1), Pow(10, 20)), Sym("zettam")));
        c.put(UnitsLength.DECIMETER, Mul(Rat(Int(1), Pow(10, 22)), Sym("zettam")));
        c.put(UnitsLength.EXAMETER, Mul(Rat(Int(1), Int(1000)), Sym("zettam")));
        c.put(UnitsLength.FEMTOMETER, Mul(Rat(Int(1), Pow(10, 36)), Sym("zettam")));
        c.put(UnitsLength.FOOT, Mul(Rat(Int(381), Mul(Int(125), Pow(10, 22))), Sym("zettam")));
        c.put(UnitsLength.GIGAMETER, Mul(Rat(Int(1), Pow(10, 12)), Sym("zettam")));
        c.put(UnitsLength.HECTOMETER, Mul(Rat(Int(1), Pow(10, 19)), Sym("zettam")));
        c.put(UnitsLength.INCH, Mul(Rat(Int(127), Mul(Int(5), Pow(10, 24))), Sym("zettam")));
        c.put(UnitsLength.KILOMETER, Mul(Rat(Int(1), Pow(10, 18)), Sym("zettam")));
        c.put(UnitsLength.LIGHTYEAR, Mul(Rat(Int("5912956545363"), Mul(Int(625), Pow(10, 15))), Sym("zettam")));
        c.put(UnitsLength.LINE, Mul(Rat(Int(127), Mul(Int(6), Pow(10, 25))), Sym("zettam")));
        c.put(UnitsLength.MEGAMETER, Mul(Rat(Int(1), Pow(10, 15)), Sym("zettam")));
        c.put(UnitsLength.METER, Mul(Rat(Int(1), Pow(10, 21)), Sym("zettam")));
        c.put(UnitsLength.MICROMETER, Mul(Rat(Int(1), Pow(10, 27)), Sym("zettam")));
        c.put(UnitsLength.MILE, Mul(Rat(Int(12573), Mul(Int(78125), Pow(10, 17))), Sym("zettam")));
        c.put(UnitsLength.MILLIMETER, Mul(Rat(Int(1), Pow(10, 24)), Sym("zettam")));
        c.put(UnitsLength.NANOMETER, Mul(Rat(Int(1), Pow(10, 30)), Sym("zettam")));
        c.put(UnitsLength.PARSEC, Mul(Rat(Int(3857097), Mul(Int(125), Pow(10, 9))), Sym("zettam")));
        c.put(UnitsLength.PETAMETER, Mul(Rat(Int(1), Pow(10, 6)), Sym("zettam")));
        c.put(UnitsLength.PICOMETER, Mul(Rat(Int(1), Pow(10, 33)), Sym("zettam")));
        c.put(UnitsLength.POINT, Mul(Rat(Int(127), Mul(Int(36), Pow(10, 25))), Sym("zettam")));
        c.put(UnitsLength.TERAMETER, Mul(Rat(Int(1), Pow(10, 9)), Sym("zettam")));
        c.put(UnitsLength.THOU, Mul(Rat(Int(127), Mul(Int(5), Pow(10, 27))), Sym("zettam")));
        c.put(UnitsLength.YARD, Mul(Rat(Int(1143), Mul(Int(125), Pow(10, 22))), Sym("zettam")));
        c.put(UnitsLength.YOCTOMETER, Mul(Rat(Int(1), Pow(10, 45)), Sym("zettam")));
        c.put(UnitsLength.YOTTAMETER, Mul(Int(1000), Sym("zettam")));
        c.put(UnitsLength.ZEPTOMETER, Mul(Rat(Int(1), Pow(10, 42)), Sym("zettam")));
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
            Conversion conversion = conversions.get(targetUnit).get(value.getUnit());
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

