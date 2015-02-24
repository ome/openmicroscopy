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

    private static final Map<String, Conversion> conversions;
    static {
        Map<String, Conversion> c = new HashMap<String, Conversion>();

        c.put("ANGSTROM:ASTRONMICALUNIT", Mul(Mul(Int(1495978707), Pow(10, 12)), Sym("ang")));
        c.put("ANGSTROM:ATTOMETER", Mul(Rat(Int(1), Pow(10, 8)), Sym("ang")));
        c.put("ANGSTROM:CENTIMETER", Mul(Pow(10, 8), Sym("ang")));
        c.put("ANGSTROM:DECAMETER", Mul(Pow(10, 11), Sym("ang")));
        c.put("ANGSTROM:DECIMETER", Mul(Pow(10, 9), Sym("ang")));
        c.put("ANGSTROM:EXAMETER", Mul(Pow(10, 28), Sym("ang")));
        c.put("ANGSTROM:FEMTOMETER", Mul(Rat(Int(1), Pow(10, 5)), Sym("ang")));
        c.put("ANGSTROM:FOOT", Mul(Mul(Int(3048), Pow(10, 6)), Sym("ang")));
        c.put("ANGSTROM:GIGAMETER", Mul(Pow(10, 19), Sym("ang")));
        c.put("ANGSTROM:HECTOMETER", Mul(Pow(10, 12), Sym("ang")));
        c.put("ANGSTROM:INCH", Mul(Mul(Int(254), Pow(10, 6)), Sym("ang")));
        c.put("ANGSTROM:KILOMETER", Mul(Pow(10, 13), Sym("ang")));
        c.put("ANGSTROM:LIGHTYEAR", Mul(Mul(Int("94607304725808"), Pow(10, 12)), Sym("ang")));
        c.put("ANGSTROM:LINE", Mul(Rat(Mul(Int(635), Pow(10, 5)), Int(3)), Sym("ang")));
        c.put("ANGSTROM:MEGAMETER", Mul(Pow(10, 16), Sym("ang")));
        c.put("ANGSTROM:METER", Mul(Pow(10, 10), Sym("ang")));
        c.put("ANGSTROM:MICROMETER", Mul(Pow(10, 4), Sym("ang")));
        c.put("ANGSTROM:MILE", Mul(Mul(Int(1609344), Pow(10, 7)), Sym("ang")));
        c.put("ANGSTROM:MILLIMETER", Mul(Pow(10, 7), Sym("ang")));
        c.put("ANGSTROM:NANOMETER", Mul(Int(10), Sym("ang")));
        c.put("ANGSTROM:PARSEC", Mul(Mul(Int(30856776), Pow(10, 19)), Sym("ang")));
        c.put("ANGSTROM:PETAMETER", Mul(Pow(10, 25), Sym("ang")));
        c.put("ANGSTROM:PICOMETER", Mul(Rat(Int(1), Int(100)), Sym("ang")));
        c.put("ANGSTROM:POINT", Mul(Rat(Mul(Int(3175), Pow(10, 4)), Int(9)), Sym("ang")));
        c.put("ANGSTROM:TERAMETER", Mul(Pow(10, 22), Sym("ang")));
        c.put("ANGSTROM:THOU", Mul(Int(254000), Sym("ang")));
        c.put("ANGSTROM:YARD", Mul(Mul(Int(9144), Pow(10, 6)), Sym("ang")));
        c.put("ANGSTROM:YOCTOMETER", Mul(Rat(Int(1), Pow(10, 14)), Sym("ang")));
        c.put("ANGSTROM:YOTTAMETER", Mul(Pow(10, 34), Sym("ang")));
        c.put("ANGSTROM:ZEPTOMETER", Mul(Rat(Int(1), Pow(10, 11)), Sym("ang")));
        c.put("ANGSTROM:ZETTAMETER", Mul(Pow(10, 31), Sym("ang")));
        c.put("ASTRONMICALUNIT:ANGSTROM", Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 12))), Sym("ua")));
        c.put("ASTRONMICALUNIT:ATTOMETER", Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 20))), Sym("ua")));
        c.put("ASTRONMICALUNIT:CENTIMETER", Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 4))), Sym("ua")));
        c.put("ASTRONMICALUNIT:DECAMETER", Mul(Rat(Int(1), Int("14959787070")), Sym("ua")));
        c.put("ASTRONMICALUNIT:DECIMETER", Mul(Rat(Int(1), Int("1495978707000")), Sym("ua")));
        c.put("ASTRONMICALUNIT:EXAMETER", Mul(Rat(Pow(10, 16), Int(1495978707)), Sym("ua")));
        c.put("ASTRONMICALUNIT:FEMTOMETER", Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 17))), Sym("ua")));
        c.put("ASTRONMICALUNIT:FOOT", Mul(Rat(Int(127), Int("62332446125000")), Sym("ua")));
        c.put("ASTRONMICALUNIT:GIGAMETER", Mul(Rat(Pow(10, 7), Int(1495978707)), Sym("ua")));
        c.put("ASTRONMICALUNIT:HECTOMETER", Mul(Rat(Int(1), Int(1495978707)), Sym("ua")));
        c.put("ASTRONMICALUNIT:INCH", Mul(Rat(Int(127), Mul(Int("7479893535"), Pow(10, 5))), Sym("ua")));
        c.put("ASTRONMICALUNIT:KILOMETER", Mul(Rat(Int(10), Int(1495978707)), Sym("ua")));
        c.put("ASTRONMICALUNIT:LIGHTYEAR", Mul(Rat(Int("431996825232"), Int(6830953)), Sym("ua")));
        c.put("ASTRONMICALUNIT:LINE", Mul(Rat(Int(127), Mul(Int("8975872242"), Pow(10, 6))), Sym("ua")));
        c.put("ASTRONMICALUNIT:MEGAMETER", Mul(Rat(Pow(10, 4), Int(1495978707)), Sym("ua")));
        c.put("ASTRONMICALUNIT:METER", Mul(Rat(Int(1), Int("149597870700")), Sym("ua")));
        c.put("ASTRONMICALUNIT:MICROMETER", Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 8))), Sym("ua")));
        c.put("ASTRONMICALUNIT:MILE", Mul(Rat(Int(16764), Int("1558311153125")), Sym("ua")));
        c.put("ASTRONMICALUNIT:MILLIMETER", Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 5))), Sym("ua")));
        c.put("ASTRONMICALUNIT:NANOMETER", Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 11))), Sym("ua")));
        c.put("ASTRONMICALUNIT:PARSEC", Mul(Rat(Mul(Int(10285592), Pow(10, 7)), Int(498659569)), Sym("ua")));
        c.put("ASTRONMICALUNIT:PETAMETER", Mul(Rat(Pow(10, 13), Int(1495978707)), Sym("ua")));
        c.put("ASTRONMICALUNIT:PICOMETER", Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 14))), Sym("ua")));
        c.put("ASTRONMICALUNIT:POINT", Mul(Rat(Int(127), Mul(Int("53855233452"), Pow(10, 6))), Sym("ua")));
        c.put("ASTRONMICALUNIT:TERAMETER", Mul(Rat(Pow(10, 10), Int(1495978707)), Sym("ua")));
        c.put("ASTRONMICALUNIT:THOU", Mul(Rat(Int(127), Mul(Int("7479893535"), Pow(10, 8))), Sym("ua")));
        c.put("ASTRONMICALUNIT:YARD", Mul(Rat(Int(381), Int("62332446125000")), Sym("ua")));
        c.put("ASTRONMICALUNIT:YOCTOMETER", Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 26))), Sym("ua")));
        c.put("ASTRONMICALUNIT:YOTTAMETER", Mul(Rat(Pow(10, 22), Int(1495978707)), Sym("ua")));
        c.put("ASTRONMICALUNIT:ZEPTOMETER", Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 23))), Sym("ua")));
        c.put("ASTRONMICALUNIT:ZETTAMETER", Mul(Rat(Pow(10, 19), Int(1495978707)), Sym("ua")));
        c.put("ATTOMETER:ANGSTROM", Mul(Pow(10, 8), Sym("attom")));
        c.put("ATTOMETER:ASTRONMICALUNIT", Mul(Mul(Int(1495978707), Pow(10, 20)), Sym("attom")));
        c.put("ATTOMETER:CENTIMETER", Mul(Pow(10, 16), Sym("attom")));
        c.put("ATTOMETER:DECAMETER", Mul(Pow(10, 19), Sym("attom")));
        c.put("ATTOMETER:DECIMETER", Mul(Pow(10, 17), Sym("attom")));
        c.put("ATTOMETER:EXAMETER", Mul(Pow(10, 36), Sym("attom")));
        c.put("ATTOMETER:FEMTOMETER", Mul(Int(1000), Sym("attom")));
        c.put("ATTOMETER:FOOT", Mul(Mul(Int(3048), Pow(10, 14)), Sym("attom")));
        c.put("ATTOMETER:GIGAMETER", Mul(Pow(10, 27), Sym("attom")));
        c.put("ATTOMETER:HECTOMETER", Mul(Pow(10, 20), Sym("attom")));
        c.put("ATTOMETER:INCH", Mul(Mul(Int(254), Pow(10, 14)), Sym("attom")));
        c.put("ATTOMETER:KILOMETER", Mul(Pow(10, 21), Sym("attom")));
        c.put("ATTOMETER:LIGHTYEAR", Mul(Mul(Int("94607304725808"), Pow(10, 20)), Sym("attom")));
        c.put("ATTOMETER:LINE", Mul(Rat(Mul(Int(635), Pow(10, 13)), Int(3)), Sym("attom")));
        c.put("ATTOMETER:MEGAMETER", Mul(Pow(10, 24), Sym("attom")));
        c.put("ATTOMETER:METER", Mul(Pow(10, 18), Sym("attom")));
        c.put("ATTOMETER:MICROMETER", Mul(Pow(10, 12), Sym("attom")));
        c.put("ATTOMETER:MILE", Mul(Mul(Int(1609344), Pow(10, 15)), Sym("attom")));
        c.put("ATTOMETER:MILLIMETER", Mul(Pow(10, 15), Sym("attom")));
        c.put("ATTOMETER:NANOMETER", Mul(Pow(10, 9), Sym("attom")));
        c.put("ATTOMETER:PARSEC", Mul(Mul(Int(30856776), Pow(10, 27)), Sym("attom")));
        c.put("ATTOMETER:PETAMETER", Mul(Pow(10, 33), Sym("attom")));
        c.put("ATTOMETER:PICOMETER", Mul(Pow(10, 6), Sym("attom")));
        c.put("ATTOMETER:POINT", Mul(Rat(Mul(Int(3175), Pow(10, 12)), Int(9)), Sym("attom")));
        c.put("ATTOMETER:TERAMETER", Mul(Pow(10, 30), Sym("attom")));
        c.put("ATTOMETER:THOU", Mul(Mul(Int(254), Pow(10, 11)), Sym("attom")));
        c.put("ATTOMETER:YARD", Mul(Mul(Int(9144), Pow(10, 14)), Sym("attom")));
        c.put("ATTOMETER:YOCTOMETER", Mul(Rat(Int(1), Pow(10, 6)), Sym("attom")));
        c.put("ATTOMETER:YOTTAMETER", Mul(Pow(10, 42), Sym("attom")));
        c.put("ATTOMETER:ZEPTOMETER", Mul(Rat(Int(1), Int(1000)), Sym("attom")));
        c.put("ATTOMETER:ZETTAMETER", Mul(Pow(10, 39), Sym("attom")));
        c.put("CENTIMETER:ANGSTROM", Mul(Rat(Int(1), Pow(10, 8)), Sym("centim")));
        c.put("CENTIMETER:ASTRONMICALUNIT", Mul(Mul(Int(1495978707), Pow(10, 4)), Sym("centim")));
        c.put("CENTIMETER:ATTOMETER", Mul(Rat(Int(1), Pow(10, 16)), Sym("centim")));
        c.put("CENTIMETER:DECAMETER", Mul(Int(1000), Sym("centim")));
        c.put("CENTIMETER:DECIMETER", Mul(Int(10), Sym("centim")));
        c.put("CENTIMETER:EXAMETER", Mul(Pow(10, 20), Sym("centim")));
        c.put("CENTIMETER:FEMTOMETER", Mul(Rat(Int(1), Pow(10, 13)), Sym("centim")));
        c.put("CENTIMETER:FOOT", Mul(Rat(Int(762), Int(25)), Sym("centim")));
        c.put("CENTIMETER:GIGAMETER", Mul(Pow(10, 11), Sym("centim")));
        c.put("CENTIMETER:HECTOMETER", Mul(Pow(10, 4), Sym("centim")));
        c.put("CENTIMETER:INCH", Mul(Rat(Int(127), Int(50)), Sym("centim")));
        c.put("CENTIMETER:KILOMETER", Mul(Pow(10, 5), Sym("centim")));
        c.put("CENTIMETER:LIGHTYEAR", Mul(Mul(Int("94607304725808"), Pow(10, 4)), Sym("centim")));
        c.put("CENTIMETER:LINE", Mul(Rat(Int(127), Int(600)), Sym("centim")));
        c.put("CENTIMETER:MEGAMETER", Mul(Pow(10, 8), Sym("centim")));
        c.put("CENTIMETER:METER", Mul(Int(100), Sym("centim")));
        c.put("CENTIMETER:MICROMETER", Mul(Rat(Int(1), Pow(10, 4)), Sym("centim")));
        c.put("CENTIMETER:MILE", Mul(Rat(Int(804672), Int(5)), Sym("centim")));
        c.put("CENTIMETER:MILLIMETER", Mul(Rat(Int(1), Int(10)), Sym("centim")));
        c.put("CENTIMETER:NANOMETER", Mul(Rat(Int(1), Pow(10, 7)), Sym("centim")));
        c.put("CENTIMETER:PARSEC", Mul(Mul(Int(30856776), Pow(10, 11)), Sym("centim")));
        c.put("CENTIMETER:PETAMETER", Mul(Pow(10, 17), Sym("centim")));
        c.put("CENTIMETER:PICOMETER", Mul(Rat(Int(1), Pow(10, 10)), Sym("centim")));
        c.put("CENTIMETER:POINT", Mul(Rat(Int(127), Int(3600)), Sym("centim")));
        c.put("CENTIMETER:TERAMETER", Mul(Pow(10, 14), Sym("centim")));
        c.put("CENTIMETER:THOU", Mul(Rat(Int(127), Mul(Int(5), Pow(10, 4))), Sym("centim")));
        c.put("CENTIMETER:YARD", Mul(Rat(Int(2286), Int(25)), Sym("centim")));
        c.put("CENTIMETER:YOCTOMETER", Mul(Rat(Int(1), Pow(10, 22)), Sym("centim")));
        c.put("CENTIMETER:YOTTAMETER", Mul(Pow(10, 26), Sym("centim")));
        c.put("CENTIMETER:ZEPTOMETER", Mul(Rat(Int(1), Pow(10, 19)), Sym("centim")));
        c.put("CENTIMETER:ZETTAMETER", Mul(Pow(10, 23), Sym("centim")));
        c.put("DECAMETER:ANGSTROM", Mul(Rat(Int(1), Pow(10, 11)), Sym("decam")));
        c.put("DECAMETER:ASTRONMICALUNIT", Mul(Int("14959787070"), Sym("decam")));
        c.put("DECAMETER:ATTOMETER", Mul(Rat(Int(1), Pow(10, 19)), Sym("decam")));
        c.put("DECAMETER:CENTIMETER", Mul(Rat(Int(1), Int(1000)), Sym("decam")));
        c.put("DECAMETER:DECIMETER", Mul(Rat(Int(1), Int(100)), Sym("decam")));
        c.put("DECAMETER:EXAMETER", Mul(Pow(10, 17), Sym("decam")));
        c.put("DECAMETER:FEMTOMETER", Mul(Rat(Int(1), Pow(10, 16)), Sym("decam")));
        c.put("DECAMETER:FOOT", Mul(Rat(Int(381), Int(12500)), Sym("decam")));
        c.put("DECAMETER:GIGAMETER", Mul(Pow(10, 8), Sym("decam")));
        c.put("DECAMETER:HECTOMETER", Mul(Int(10), Sym("decam")));
        c.put("DECAMETER:INCH", Mul(Rat(Int(127), Mul(Int(5), Pow(10, 4))), Sym("decam")));
        c.put("DECAMETER:KILOMETER", Mul(Int(100), Sym("decam")));
        c.put("DECAMETER:LIGHTYEAR", Mul(Int("946073047258080"), Sym("decam")));
        c.put("DECAMETER:LINE", Mul(Rat(Int(127), Mul(Int(6), Pow(10, 5))), Sym("decam")));
        c.put("DECAMETER:MEGAMETER", Mul(Pow(10, 5), Sym("decam")));
        c.put("DECAMETER:METER", Mul(Rat(Int(1), Int(10)), Sym("decam")));
        c.put("DECAMETER:MICROMETER", Mul(Rat(Int(1), Pow(10, 7)), Sym("decam")));
        c.put("DECAMETER:MILE", Mul(Rat(Int(100584), Int(625)), Sym("decam")));
        c.put("DECAMETER:MILLIMETER", Mul(Rat(Int(1), Pow(10, 4)), Sym("decam")));
        c.put("DECAMETER:NANOMETER", Mul(Rat(Int(1), Pow(10, 10)), Sym("decam")));
        c.put("DECAMETER:PARSEC", Mul(Mul(Int(30856776), Pow(10, 8)), Sym("decam")));
        c.put("DECAMETER:PETAMETER", Mul(Pow(10, 14), Sym("decam")));
        c.put("DECAMETER:PICOMETER", Mul(Rat(Int(1), Pow(10, 13)), Sym("decam")));
        c.put("DECAMETER:POINT", Mul(Rat(Int(127), Mul(Int(36), Pow(10, 5))), Sym("decam")));
        c.put("DECAMETER:TERAMETER", Mul(Pow(10, 11), Sym("decam")));
        c.put("DECAMETER:THOU", Mul(Rat(Int(127), Mul(Int(5), Pow(10, 7))), Sym("decam")));
        c.put("DECAMETER:YARD", Mul(Rat(Int(1143), Int(12500)), Sym("decam")));
        c.put("DECAMETER:YOCTOMETER", Mul(Rat(Int(1), Pow(10, 25)), Sym("decam")));
        c.put("DECAMETER:YOTTAMETER", Mul(Pow(10, 23), Sym("decam")));
        c.put("DECAMETER:ZEPTOMETER", Mul(Rat(Int(1), Pow(10, 22)), Sym("decam")));
        c.put("DECAMETER:ZETTAMETER", Mul(Pow(10, 20), Sym("decam")));
        c.put("DECIMETER:ANGSTROM", Mul(Rat(Int(1), Pow(10, 9)), Sym("decim")));
        c.put("DECIMETER:ASTRONMICALUNIT", Mul(Int("1495978707000"), Sym("decim")));
        c.put("DECIMETER:ATTOMETER", Mul(Rat(Int(1), Pow(10, 17)), Sym("decim")));
        c.put("DECIMETER:CENTIMETER", Mul(Rat(Int(1), Int(10)), Sym("decim")));
        c.put("DECIMETER:DECAMETER", Mul(Int(100), Sym("decim")));
        c.put("DECIMETER:EXAMETER", Mul(Pow(10, 19), Sym("decim")));
        c.put("DECIMETER:FEMTOMETER", Mul(Rat(Int(1), Pow(10, 14)), Sym("decim")));
        c.put("DECIMETER:FOOT", Mul(Rat(Int(381), Int(125)), Sym("decim")));
        c.put("DECIMETER:GIGAMETER", Mul(Pow(10, 10), Sym("decim")));
        c.put("DECIMETER:HECTOMETER", Mul(Int(1000), Sym("decim")));
        c.put("DECIMETER:INCH", Mul(Rat(Int(127), Int(500)), Sym("decim")));
        c.put("DECIMETER:KILOMETER", Mul(Pow(10, 4), Sym("decim")));
        c.put("DECIMETER:LIGHTYEAR", Mul(Int("94607304725808000"), Sym("decim")));
        c.put("DECIMETER:LINE", Mul(Rat(Int(127), Int(6000)), Sym("decim")));
        c.put("DECIMETER:MEGAMETER", Mul(Pow(10, 7), Sym("decim")));
        c.put("DECIMETER:METER", Mul(Int(10), Sym("decim")));
        c.put("DECIMETER:MICROMETER", Mul(Rat(Int(1), Pow(10, 5)), Sym("decim")));
        c.put("DECIMETER:MILE", Mul(Rat(Int(402336), Int(25)), Sym("decim")));
        c.put("DECIMETER:MILLIMETER", Mul(Rat(Int(1), Int(100)), Sym("decim")));
        c.put("DECIMETER:NANOMETER", Mul(Rat(Int(1), Pow(10, 8)), Sym("decim")));
        c.put("DECIMETER:PARSEC", Mul(Mul(Int(30856776), Pow(10, 10)), Sym("decim")));
        c.put("DECIMETER:PETAMETER", Mul(Pow(10, 16), Sym("decim")));
        c.put("DECIMETER:PICOMETER", Mul(Rat(Int(1), Pow(10, 11)), Sym("decim")));
        c.put("DECIMETER:POINT", Mul(Rat(Int(127), Int(36000)), Sym("decim")));
        c.put("DECIMETER:TERAMETER", Mul(Pow(10, 13), Sym("decim")));
        c.put("DECIMETER:THOU", Mul(Rat(Int(127), Mul(Int(5), Pow(10, 5))), Sym("decim")));
        c.put("DECIMETER:YARD", Mul(Rat(Int(1143), Int(125)), Sym("decim")));
        c.put("DECIMETER:YOCTOMETER", Mul(Rat(Int(1), Pow(10, 23)), Sym("decim")));
        c.put("DECIMETER:YOTTAMETER", Mul(Pow(10, 25), Sym("decim")));
        c.put("DECIMETER:ZEPTOMETER", Mul(Rat(Int(1), Pow(10, 20)), Sym("decim")));
        c.put("DECIMETER:ZETTAMETER", Mul(Pow(10, 22), Sym("decim")));
        c.put("EXAMETER:ANGSTROM", Mul(Rat(Int(1), Pow(10, 28)), Sym("exam")));
        c.put("EXAMETER:ASTRONMICALUNIT", Mul(Rat(Int(1495978707), Pow(10, 16)), Sym("exam")));
        c.put("EXAMETER:ATTOMETER", Mul(Rat(Int(1), Pow(10, 36)), Sym("exam")));
        c.put("EXAMETER:CENTIMETER", Mul(Rat(Int(1), Pow(10, 20)), Sym("exam")));
        c.put("EXAMETER:DECAMETER", Mul(Rat(Int(1), Pow(10, 17)), Sym("exam")));
        c.put("EXAMETER:DECIMETER", Mul(Rat(Int(1), Pow(10, 19)), Sym("exam")));
        c.put("EXAMETER:FEMTOMETER", Mul(Rat(Int(1), Pow(10, 33)), Sym("exam")));
        c.put("EXAMETER:FOOT", Mul(Rat(Int(381), Mul(Int(125), Pow(10, 19))), Sym("exam")));
        c.put("EXAMETER:GIGAMETER", Mul(Rat(Int(1), Pow(10, 9)), Sym("exam")));
        c.put("EXAMETER:HECTOMETER", Mul(Rat(Int(1), Pow(10, 16)), Sym("exam")));
        c.put("EXAMETER:INCH", Mul(Rat(Int(127), Mul(Int(5), Pow(10, 21))), Sym("exam")));
        c.put("EXAMETER:KILOMETER", Mul(Rat(Int(1), Pow(10, 15)), Sym("exam")));
        c.put("EXAMETER:LIGHTYEAR", Mul(Rat(Int("5912956545363"), Mul(Int(625), Pow(10, 12))), Sym("exam")));
        c.put("EXAMETER:LINE", Mul(Rat(Int(127), Mul(Int(6), Pow(10, 22))), Sym("exam")));
        c.put("EXAMETER:MEGAMETER", Mul(Rat(Int(1), Pow(10, 12)), Sym("exam")));
        c.put("EXAMETER:METER", Mul(Rat(Int(1), Pow(10, 18)), Sym("exam")));
        c.put("EXAMETER:MICROMETER", Mul(Rat(Int(1), Pow(10, 24)), Sym("exam")));
        c.put("EXAMETER:MILE", Mul(Rat(Int(12573), Mul(Int(78125), Pow(10, 14))), Sym("exam")));
        c.put("EXAMETER:MILLIMETER", Mul(Rat(Int(1), Pow(10, 21)), Sym("exam")));
        c.put("EXAMETER:NANOMETER", Mul(Rat(Int(1), Pow(10, 27)), Sym("exam")));
        c.put("EXAMETER:PARSEC", Mul(Rat(Int(3857097), Mul(Int(125), Pow(10, 6))), Sym("exam")));
        c.put("EXAMETER:PETAMETER", Mul(Rat(Int(1), Int(1000)), Sym("exam")));
        c.put("EXAMETER:PICOMETER", Mul(Rat(Int(1), Pow(10, 30)), Sym("exam")));
        c.put("EXAMETER:POINT", Mul(Rat(Int(127), Mul(Int(36), Pow(10, 22))), Sym("exam")));
        c.put("EXAMETER:TERAMETER", Mul(Rat(Int(1), Pow(10, 6)), Sym("exam")));
        c.put("EXAMETER:THOU", Mul(Rat(Int(127), Mul(Int(5), Pow(10, 24))), Sym("exam")));
        c.put("EXAMETER:YARD", Mul(Rat(Int(1143), Mul(Int(125), Pow(10, 19))), Sym("exam")));
        c.put("EXAMETER:YOCTOMETER", Mul(Rat(Int(1), Pow(10, 42)), Sym("exam")));
        c.put("EXAMETER:YOTTAMETER", Mul(Pow(10, 6), Sym("exam")));
        c.put("EXAMETER:ZEPTOMETER", Mul(Rat(Int(1), Pow(10, 39)), Sym("exam")));
        c.put("EXAMETER:ZETTAMETER", Mul(Int(1000), Sym("exam")));
        c.put("FEMTOMETER:ANGSTROM", Mul(Pow(10, 5), Sym("femtom")));
        c.put("FEMTOMETER:ASTRONMICALUNIT", Mul(Mul(Int(1495978707), Pow(10, 17)), Sym("femtom")));
        c.put("FEMTOMETER:ATTOMETER", Mul(Rat(Int(1), Int(1000)), Sym("femtom")));
        c.put("FEMTOMETER:CENTIMETER", Mul(Pow(10, 13), Sym("femtom")));
        c.put("FEMTOMETER:DECAMETER", Mul(Pow(10, 16), Sym("femtom")));
        c.put("FEMTOMETER:DECIMETER", Mul(Pow(10, 14), Sym("femtom")));
        c.put("FEMTOMETER:EXAMETER", Mul(Pow(10, 33), Sym("femtom")));
        c.put("FEMTOMETER:FOOT", Mul(Mul(Int(3048), Pow(10, 11)), Sym("femtom")));
        c.put("FEMTOMETER:GIGAMETER", Mul(Pow(10, 24), Sym("femtom")));
        c.put("FEMTOMETER:HECTOMETER", Mul(Pow(10, 17), Sym("femtom")));
        c.put("FEMTOMETER:INCH", Mul(Mul(Int(254), Pow(10, 11)), Sym("femtom")));
        c.put("FEMTOMETER:KILOMETER", Mul(Pow(10, 18), Sym("femtom")));
        c.put("FEMTOMETER:LIGHTYEAR", Mul(Mul(Int("94607304725808"), Pow(10, 17)), Sym("femtom")));
        c.put("FEMTOMETER:LINE", Mul(Rat(Mul(Int(635), Pow(10, 10)), Int(3)), Sym("femtom")));
        c.put("FEMTOMETER:MEGAMETER", Mul(Pow(10, 21), Sym("femtom")));
        c.put("FEMTOMETER:METER", Mul(Pow(10, 15), Sym("femtom")));
        c.put("FEMTOMETER:MICROMETER", Mul(Pow(10, 9), Sym("femtom")));
        c.put("FEMTOMETER:MILE", Mul(Mul(Int(1609344), Pow(10, 12)), Sym("femtom")));
        c.put("FEMTOMETER:MILLIMETER", Mul(Pow(10, 12), Sym("femtom")));
        c.put("FEMTOMETER:NANOMETER", Mul(Pow(10, 6), Sym("femtom")));
        c.put("FEMTOMETER:PARSEC", Mul(Mul(Int(30856776), Pow(10, 24)), Sym("femtom")));
        c.put("FEMTOMETER:PETAMETER", Mul(Pow(10, 30), Sym("femtom")));
        c.put("FEMTOMETER:PICOMETER", Mul(Int(1000), Sym("femtom")));
        c.put("FEMTOMETER:POINT", Mul(Rat(Mul(Int(3175), Pow(10, 9)), Int(9)), Sym("femtom")));
        c.put("FEMTOMETER:TERAMETER", Mul(Pow(10, 27), Sym("femtom")));
        c.put("FEMTOMETER:THOU", Mul(Mul(Int(254), Pow(10, 8)), Sym("femtom")));
        c.put("FEMTOMETER:YARD", Mul(Mul(Int(9144), Pow(10, 11)), Sym("femtom")));
        c.put("FEMTOMETER:YOCTOMETER", Mul(Rat(Int(1), Pow(10, 9)), Sym("femtom")));
        c.put("FEMTOMETER:YOTTAMETER", Mul(Pow(10, 39), Sym("femtom")));
        c.put("FEMTOMETER:ZEPTOMETER", Mul(Rat(Int(1), Pow(10, 6)), Sym("femtom")));
        c.put("FEMTOMETER:ZETTAMETER", Mul(Pow(10, 36), Sym("femtom")));
        c.put("FOOT:ANGSTROM", Mul(Rat(Int(1), Mul(Int(3048), Pow(10, 6))), Sym("ft")));
        c.put("FOOT:ASTRONMICALUNIT", Mul(Rat(Int("62332446125000"), Int(127)), Sym("ft")));
        c.put("FOOT:ATTOMETER", Mul(Rat(Int(1), Mul(Int(3048), Pow(10, 14))), Sym("ft")));
        c.put("FOOT:CENTIMETER", Mul(Rat(Int(25), Int(762)), Sym("ft")));
        c.put("FOOT:DECAMETER", Mul(Rat(Int(12500), Int(381)), Sym("ft")));
        c.put("FOOT:DECIMETER", Mul(Rat(Int(125), Int(381)), Sym("ft")));
        c.put("FOOT:EXAMETER", Mul(Rat(Mul(Int(125), Pow(10, 19)), Int(381)), Sym("ft")));
        c.put("FOOT:FEMTOMETER", Mul(Rat(Int(1), Mul(Int(3048), Pow(10, 11))), Sym("ft")));
        c.put("FOOT:GIGAMETER", Mul(Rat(Mul(Int(125), Pow(10, 10)), Int(381)), Sym("ft")));
        c.put("FOOT:HECTOMETER", Mul(Rat(Int(125000), Int(381)), Sym("ft")));
        c.put("FOOT:INCH", Mul(Rat(Int(1), Int(12)), Sym("ft")));
        c.put("FOOT:KILOMETER", Mul(Rat(Mul(Int(125), Pow(10, 4)), Int(381)), Sym("ft")));
        c.put("FOOT:LIGHTYEAR", Mul(Rat(Mul(Int("3941971030242"), Pow(10, 6)), Int(127)), Sym("ft")));
        c.put("FOOT:LINE", Mul(Rat(Int(1), Int(144)), Sym("ft")));
        c.put("FOOT:MEGAMETER", Mul(Rat(Mul(Int(125), Pow(10, 7)), Int(381)), Sym("ft")));
        c.put("FOOT:METER", Mul(Rat(Int(1250), Int(381)), Sym("ft")));
        c.put("FOOT:MICROMETER", Mul(Rat(Int(1), Int(304800)), Sym("ft")));
        c.put("FOOT:MILE", Mul(Int(5280), Sym("ft")));
        c.put("FOOT:MILLIMETER", Mul(Rat(Int(5), Int(1524)), Sym("ft")));
        c.put("FOOT:NANOMETER", Mul(Rat(Int(1), Mul(Int(3048), Pow(10, 5))), Sym("ft")));
        c.put("FOOT:PARSEC", Mul(Rat(Mul(Int(1285699), Pow(10, 13)), Int(127)), Sym("ft")));
        c.put("FOOT:PETAMETER", Mul(Rat(Mul(Int(125), Pow(10, 16)), Int(381)), Sym("ft")));
        c.put("FOOT:PICOMETER", Mul(Rat(Int(1), Mul(Int(3048), Pow(10, 8))), Sym("ft")));
        c.put("FOOT:POINT", Mul(Rat(Int(1), Int(864)), Sym("ft")));
        c.put("FOOT:TERAMETER", Mul(Rat(Mul(Int(125), Pow(10, 13)), Int(381)), Sym("ft")));
        c.put("FOOT:THOU", Mul(Rat(Int(1), Int(12000)), Sym("ft")));
        c.put("FOOT:YARD", Mul(Int(3), Sym("ft")));
        c.put("FOOT:YOCTOMETER", Mul(Rat(Int(1), Mul(Int(3048), Pow(10, 20))), Sym("ft")));
        c.put("FOOT:YOTTAMETER", Mul(Rat(Mul(Int(125), Pow(10, 25)), Int(381)), Sym("ft")));
        c.put("FOOT:ZEPTOMETER", Mul(Rat(Int(1), Mul(Int(3048), Pow(10, 17))), Sym("ft")));
        c.put("FOOT:ZETTAMETER", Mul(Rat(Mul(Int(125), Pow(10, 22)), Int(381)), Sym("ft")));
        c.put("GIGAMETER:ANGSTROM", Mul(Rat(Int(1), Pow(10, 19)), Sym("gigam")));
        c.put("GIGAMETER:ASTRONMICALUNIT", Mul(Rat(Int(1495978707), Pow(10, 7)), Sym("gigam")));
        c.put("GIGAMETER:ATTOMETER", Mul(Rat(Int(1), Pow(10, 27)), Sym("gigam")));
        c.put("GIGAMETER:CENTIMETER", Mul(Rat(Int(1), Pow(10, 11)), Sym("gigam")));
        c.put("GIGAMETER:DECAMETER", Mul(Rat(Int(1), Pow(10, 8)), Sym("gigam")));
        c.put("GIGAMETER:DECIMETER", Mul(Rat(Int(1), Pow(10, 10)), Sym("gigam")));
        c.put("GIGAMETER:EXAMETER", Mul(Pow(10, 9), Sym("gigam")));
        c.put("GIGAMETER:FEMTOMETER", Mul(Rat(Int(1), Pow(10, 24)), Sym("gigam")));
        c.put("GIGAMETER:FOOT", Mul(Rat(Int(381), Mul(Int(125), Pow(10, 10))), Sym("gigam")));
        c.put("GIGAMETER:HECTOMETER", Mul(Rat(Int(1), Pow(10, 7)), Sym("gigam")));
        c.put("GIGAMETER:INCH", Mul(Rat(Int(127), Mul(Int(5), Pow(10, 12))), Sym("gigam")));
        c.put("GIGAMETER:KILOMETER", Mul(Rat(Int(1), Pow(10, 6)), Sym("gigam")));
        c.put("GIGAMETER:LIGHTYEAR", Mul(Rat(Int("5912956545363"), Int(625000)), Sym("gigam")));
        c.put("GIGAMETER:LINE", Mul(Rat(Int(127), Mul(Int(6), Pow(10, 13))), Sym("gigam")));
        c.put("GIGAMETER:MEGAMETER", Mul(Rat(Int(1), Int(1000)), Sym("gigam")));
        c.put("GIGAMETER:METER", Mul(Rat(Int(1), Pow(10, 9)), Sym("gigam")));
        c.put("GIGAMETER:MICROMETER", Mul(Rat(Int(1), Pow(10, 15)), Sym("gigam")));
        c.put("GIGAMETER:MILE", Mul(Rat(Int(12573), Mul(Int(78125), Pow(10, 5))), Sym("gigam")));
        c.put("GIGAMETER:MILLIMETER", Mul(Rat(Int(1), Pow(10, 12)), Sym("gigam")));
        c.put("GIGAMETER:NANOMETER", Mul(Rat(Int(1), Pow(10, 18)), Sym("gigam")));
        c.put("GIGAMETER:PARSEC", Mul(Int(30856776), Sym("gigam")));
        c.put("GIGAMETER:PETAMETER", Mul(Pow(10, 6), Sym("gigam")));
        c.put("GIGAMETER:PICOMETER", Mul(Rat(Int(1), Pow(10, 21)), Sym("gigam")));
        c.put("GIGAMETER:POINT", Mul(Rat(Int(127), Mul(Int(36), Pow(10, 13))), Sym("gigam")));
        c.put("GIGAMETER:TERAMETER", Mul(Int(1000), Sym("gigam")));
        c.put("GIGAMETER:THOU", Mul(Rat(Int(127), Mul(Int(5), Pow(10, 15))), Sym("gigam")));
        c.put("GIGAMETER:YARD", Mul(Rat(Int(1143), Mul(Int(125), Pow(10, 10))), Sym("gigam")));
        c.put("GIGAMETER:YOCTOMETER", Mul(Rat(Int(1), Pow(10, 33)), Sym("gigam")));
        c.put("GIGAMETER:YOTTAMETER", Mul(Pow(10, 15), Sym("gigam")));
        c.put("GIGAMETER:ZEPTOMETER", Mul(Rat(Int(1), Pow(10, 30)), Sym("gigam")));
        c.put("GIGAMETER:ZETTAMETER", Mul(Pow(10, 12), Sym("gigam")));
        c.put("HECTOMETER:ANGSTROM", Mul(Rat(Int(1), Pow(10, 12)), Sym("hectom")));
        c.put("HECTOMETER:ASTRONMICALUNIT", Mul(Int(1495978707), Sym("hectom")));
        c.put("HECTOMETER:ATTOMETER", Mul(Rat(Int(1), Pow(10, 20)), Sym("hectom")));
        c.put("HECTOMETER:CENTIMETER", Mul(Rat(Int(1), Pow(10, 4)), Sym("hectom")));
        c.put("HECTOMETER:DECAMETER", Mul(Rat(Int(1), Int(10)), Sym("hectom")));
        c.put("HECTOMETER:DECIMETER", Mul(Rat(Int(1), Int(1000)), Sym("hectom")));
        c.put("HECTOMETER:EXAMETER", Mul(Pow(10, 16), Sym("hectom")));
        c.put("HECTOMETER:FEMTOMETER", Mul(Rat(Int(1), Pow(10, 17)), Sym("hectom")));
        c.put("HECTOMETER:FOOT", Mul(Rat(Int(381), Int(125000)), Sym("hectom")));
        c.put("HECTOMETER:GIGAMETER", Mul(Pow(10, 7), Sym("hectom")));
        c.put("HECTOMETER:INCH", Mul(Rat(Int(127), Mul(Int(5), Pow(10, 5))), Sym("hectom")));
        c.put("HECTOMETER:KILOMETER", Mul(Int(10), Sym("hectom")));
        c.put("HECTOMETER:LIGHTYEAR", Mul(Int("94607304725808"), Sym("hectom")));
        c.put("HECTOMETER:LINE", Mul(Rat(Int(127), Mul(Int(6), Pow(10, 6))), Sym("hectom")));
        c.put("HECTOMETER:MEGAMETER", Mul(Pow(10, 4), Sym("hectom")));
        c.put("HECTOMETER:METER", Mul(Rat(Int(1), Int(100)), Sym("hectom")));
        c.put("HECTOMETER:MICROMETER", Mul(Rat(Int(1), Pow(10, 8)), Sym("hectom")));
        c.put("HECTOMETER:MILE", Mul(Rat(Int(50292), Int(3125)), Sym("hectom")));
        c.put("HECTOMETER:MILLIMETER", Mul(Rat(Int(1), Pow(10, 5)), Sym("hectom")));
        c.put("HECTOMETER:NANOMETER", Mul(Rat(Int(1), Pow(10, 11)), Sym("hectom")));
        c.put("HECTOMETER:PARSEC", Mul(Mul(Int(30856776), Pow(10, 7)), Sym("hectom")));
        c.put("HECTOMETER:PETAMETER", Mul(Pow(10, 13), Sym("hectom")));
        c.put("HECTOMETER:PICOMETER", Mul(Rat(Int(1), Pow(10, 14)), Sym("hectom")));
        c.put("HECTOMETER:POINT", Mul(Rat(Int(127), Mul(Int(36), Pow(10, 6))), Sym("hectom")));
        c.put("HECTOMETER:TERAMETER", Mul(Pow(10, 10), Sym("hectom")));
        c.put("HECTOMETER:THOU", Mul(Rat(Int(127), Mul(Int(5), Pow(10, 8))), Sym("hectom")));
        c.put("HECTOMETER:YARD", Mul(Rat(Int(1143), Int(125000)), Sym("hectom")));
        c.put("HECTOMETER:YOCTOMETER", Mul(Rat(Int(1), Pow(10, 26)), Sym("hectom")));
        c.put("HECTOMETER:YOTTAMETER", Mul(Pow(10, 22), Sym("hectom")));
        c.put("HECTOMETER:ZEPTOMETER", Mul(Rat(Int(1), Pow(10, 23)), Sym("hectom")));
        c.put("HECTOMETER:ZETTAMETER", Mul(Pow(10, 19), Sym("hectom")));
        c.put("INCH:ANGSTROM", Mul(Rat(Int(1), Mul(Int(254), Pow(10, 6))), Sym("in")));
        c.put("INCH:ASTRONMICALUNIT", Mul(Rat(Mul(Int("7479893535"), Pow(10, 5)), Int(127)), Sym("in")));
        c.put("INCH:ATTOMETER", Mul(Rat(Int(1), Mul(Int(254), Pow(10, 14))), Sym("in")));
        c.put("INCH:CENTIMETER", Mul(Rat(Int(50), Int(127)), Sym("in")));
        c.put("INCH:DECAMETER", Mul(Rat(Mul(Int(5), Pow(10, 4)), Int(127)), Sym("in")));
        c.put("INCH:DECIMETER", Mul(Rat(Int(500), Int(127)), Sym("in")));
        c.put("INCH:EXAMETER", Mul(Rat(Mul(Int(5), Pow(10, 21)), Int(127)), Sym("in")));
        c.put("INCH:FEMTOMETER", Mul(Rat(Int(1), Mul(Int(254), Pow(10, 11))), Sym("in")));
        c.put("INCH:FOOT", Mul(Int(12), Sym("in")));
        c.put("INCH:GIGAMETER", Mul(Rat(Mul(Int(5), Pow(10, 12)), Int(127)), Sym("in")));
        c.put("INCH:HECTOMETER", Mul(Rat(Mul(Int(5), Pow(10, 5)), Int(127)), Sym("in")));
        c.put("INCH:KILOMETER", Mul(Rat(Mul(Int(5), Pow(10, 6)), Int(127)), Sym("in")));
        c.put("INCH:LIGHTYEAR", Mul(Rat(Mul(Int("47303652362904"), Pow(10, 6)), Int(127)), Sym("in")));
        c.put("INCH:LINE", Mul(Rat(Int(1), Int(12)), Sym("in")));
        c.put("INCH:MEGAMETER", Mul(Rat(Mul(Int(5), Pow(10, 9)), Int(127)), Sym("in")));
        c.put("INCH:METER", Mul(Rat(Int(5000), Int(127)), Sym("in")));
        c.put("INCH:MICROMETER", Mul(Rat(Int(1), Int(25400)), Sym("in")));
        c.put("INCH:MILE", Mul(Int(63360), Sym("in")));
        c.put("INCH:MILLIMETER", Mul(Rat(Int(5), Int(127)), Sym("in")));
        c.put("INCH:NANOMETER", Mul(Rat(Int(1), Mul(Int(254), Pow(10, 5))), Sym("in")));
        c.put("INCH:PARSEC", Mul(Rat(Mul(Int(15428388), Pow(10, 13)), Int(127)), Sym("in")));
        c.put("INCH:PETAMETER", Mul(Rat(Mul(Int(5), Pow(10, 18)), Int(127)), Sym("in")));
        c.put("INCH:PICOMETER", Mul(Rat(Int(1), Mul(Int(254), Pow(10, 8))), Sym("in")));
        c.put("INCH:POINT", Mul(Rat(Int(1), Int(72)), Sym("in")));
        c.put("INCH:TERAMETER", Mul(Rat(Mul(Int(5), Pow(10, 15)), Int(127)), Sym("in")));
        c.put("INCH:THOU", Mul(Rat(Int(1), Int(1000)), Sym("in")));
        c.put("INCH:YARD", Mul(Int(36), Sym("in")));
        c.put("INCH:YOCTOMETER", Mul(Rat(Int(1), Mul(Int(254), Pow(10, 20))), Sym("in")));
        c.put("INCH:YOTTAMETER", Mul(Rat(Mul(Int(5), Pow(10, 27)), Int(127)), Sym("in")));
        c.put("INCH:ZEPTOMETER", Mul(Rat(Int(1), Mul(Int(254), Pow(10, 17))), Sym("in")));
        c.put("INCH:ZETTAMETER", Mul(Rat(Mul(Int(5), Pow(10, 24)), Int(127)), Sym("in")));
        c.put("KILOMETER:ANGSTROM", Mul(Rat(Int(1), Pow(10, 13)), Sym("kilom")));
        c.put("KILOMETER:ASTRONMICALUNIT", Mul(Rat(Int(1495978707), Int(10)), Sym("kilom")));
        c.put("KILOMETER:ATTOMETER", Mul(Rat(Int(1), Pow(10, 21)), Sym("kilom")));
        c.put("KILOMETER:CENTIMETER", Mul(Rat(Int(1), Pow(10, 5)), Sym("kilom")));
        c.put("KILOMETER:DECAMETER", Mul(Rat(Int(1), Int(100)), Sym("kilom")));
        c.put("KILOMETER:DECIMETER", Mul(Rat(Int(1), Pow(10, 4)), Sym("kilom")));
        c.put("KILOMETER:EXAMETER", Mul(Pow(10, 15), Sym("kilom")));
        c.put("KILOMETER:FEMTOMETER", Mul(Rat(Int(1), Pow(10, 18)), Sym("kilom")));
        c.put("KILOMETER:FOOT", Mul(Rat(Int(381), Mul(Int(125), Pow(10, 4))), Sym("kilom")));
        c.put("KILOMETER:GIGAMETER", Mul(Pow(10, 6), Sym("kilom")));
        c.put("KILOMETER:HECTOMETER", Mul(Rat(Int(1), Int(10)), Sym("kilom")));
        c.put("KILOMETER:INCH", Mul(Rat(Int(127), Mul(Int(5), Pow(10, 6))), Sym("kilom")));
        c.put("KILOMETER:LIGHTYEAR", Mul(Rat(Int("47303652362904"), Int(5)), Sym("kilom")));
        c.put("KILOMETER:LINE", Mul(Rat(Int(127), Mul(Int(6), Pow(10, 7))), Sym("kilom")));
        c.put("KILOMETER:MEGAMETER", Mul(Int(1000), Sym("kilom")));
        c.put("KILOMETER:METER", Mul(Rat(Int(1), Int(1000)), Sym("kilom")));
        c.put("KILOMETER:MICROMETER", Mul(Rat(Int(1), Pow(10, 9)), Sym("kilom")));
        c.put("KILOMETER:MILE", Mul(Rat(Int(25146), Int(15625)), Sym("kilom")));
        c.put("KILOMETER:MILLIMETER", Mul(Rat(Int(1), Pow(10, 6)), Sym("kilom")));
        c.put("KILOMETER:NANOMETER", Mul(Rat(Int(1), Pow(10, 12)), Sym("kilom")));
        c.put("KILOMETER:PARSEC", Mul(Mul(Int(30856776), Pow(10, 6)), Sym("kilom")));
        c.put("KILOMETER:PETAMETER", Mul(Pow(10, 12), Sym("kilom")));
        c.put("KILOMETER:PICOMETER", Mul(Rat(Int(1), Pow(10, 15)), Sym("kilom")));
        c.put("KILOMETER:POINT", Mul(Rat(Int(127), Mul(Int(36), Pow(10, 7))), Sym("kilom")));
        c.put("KILOMETER:TERAMETER", Mul(Pow(10, 9), Sym("kilom")));
        c.put("KILOMETER:THOU", Mul(Rat(Int(127), Mul(Int(5), Pow(10, 9))), Sym("kilom")));
        c.put("KILOMETER:YARD", Mul(Rat(Int(1143), Mul(Int(125), Pow(10, 4))), Sym("kilom")));
        c.put("KILOMETER:YOCTOMETER", Mul(Rat(Int(1), Pow(10, 27)), Sym("kilom")));
        c.put("KILOMETER:YOTTAMETER", Mul(Pow(10, 21), Sym("kilom")));
        c.put("KILOMETER:ZEPTOMETER", Mul(Rat(Int(1), Pow(10, 24)), Sym("kilom")));
        c.put("KILOMETER:ZETTAMETER", Mul(Pow(10, 18), Sym("kilom")));
        c.put("LIGHTYEAR:ANGSTROM", Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 12))), Sym("ly")));
        c.put("LIGHTYEAR:ASTRONMICALUNIT", Mul(Rat(Int(6830953), Int("431996825232")), Sym("ly")));
        c.put("LIGHTYEAR:ATTOMETER", Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 20))), Sym("ly")));
        c.put("LIGHTYEAR:CENTIMETER", Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 4))), Sym("ly")));
        c.put("LIGHTYEAR:DECAMETER", Mul(Rat(Int(1), Int("946073047258080")), Sym("ly")));
        c.put("LIGHTYEAR:DECIMETER", Mul(Rat(Int(1), Int("94607304725808000")), Sym("ly")));
        c.put("LIGHTYEAR:EXAMETER", Mul(Rat(Mul(Int(625), Pow(10, 12)), Int("5912956545363")), Sym("ly")));
        c.put("LIGHTYEAR:FEMTOMETER", Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 17))), Sym("ly")));
        c.put("LIGHTYEAR:FOOT", Mul(Rat(Int(127), Mul(Int("3941971030242"), Pow(10, 6))), Sym("ly")));
        c.put("LIGHTYEAR:GIGAMETER", Mul(Rat(Int(625000), Int("5912956545363")), Sym("ly")));
        c.put("LIGHTYEAR:HECTOMETER", Mul(Rat(Int(1), Int("94607304725808")), Sym("ly")));
        c.put("LIGHTYEAR:INCH", Mul(Rat(Int(127), Mul(Int("47303652362904"), Pow(10, 6))), Sym("ly")));
        c.put("LIGHTYEAR:KILOMETER", Mul(Rat(Int(5), Int("47303652362904")), Sym("ly")));
        c.put("LIGHTYEAR:LINE", Mul(Rat(Int(127), Mul(Int("567643828354848"), Pow(10, 6))), Sym("ly")));
        c.put("LIGHTYEAR:MEGAMETER", Mul(Rat(Int(625), Int("5912956545363")), Sym("ly")));
        c.put("LIGHTYEAR:METER", Mul(Rat(Int(1), Int("9460730472580800")), Sym("ly")));
        c.put("LIGHTYEAR:MICROMETER", Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 8))), Sym("ly")));
        c.put("LIGHTYEAR:MILE", Mul(Rat(Int(1397), Int("8212439646337500")), Sym("ly")));
        c.put("LIGHTYEAR:MILLIMETER", Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 5))), Sym("ly")));
        c.put("LIGHTYEAR:NANOMETER", Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 11))), Sym("ly")));
        c.put("LIGHTYEAR:PARSEC", Mul(Rat(Mul(Int(6428495), Pow(10, 6)), Int("1970985515121")), Sym("ly")));
        c.put("LIGHTYEAR:PETAMETER", Mul(Rat(Mul(Int(625), Pow(10, 9)), Int("5912956545363")), Sym("ly")));
        c.put("LIGHTYEAR:PICOMETER", Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 14))), Sym("ly")));
        c.put("LIGHTYEAR:POINT", Mul(Rat(Int(127), Mul(Int("3405862970129088"), Pow(10, 6))), Sym("ly")));
        c.put("LIGHTYEAR:TERAMETER", Mul(Rat(Mul(Int(625), Pow(10, 6)), Int("5912956545363")), Sym("ly")));
        c.put("LIGHTYEAR:THOU", Mul(Rat(Int(127), Mul(Int("47303652362904"), Pow(10, 9))), Sym("ly")));
        c.put("LIGHTYEAR:YARD", Mul(Rat(Int(127), Mul(Int("1313990343414"), Pow(10, 6))), Sym("ly")));
        c.put("LIGHTYEAR:YOCTOMETER", Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 26))), Sym("ly")));
        c.put("LIGHTYEAR:YOTTAMETER", Mul(Rat(Mul(Int(625), Pow(10, 18)), Int("5912956545363")), Sym("ly")));
        c.put("LIGHTYEAR:ZEPTOMETER", Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 23))), Sym("ly")));
        c.put("LIGHTYEAR:ZETTAMETER", Mul(Rat(Mul(Int(625), Pow(10, 15)), Int("5912956545363")), Sym("ly")));
        c.put("LINE:ANGSTROM", Mul(Rat(Int(3), Mul(Int(635), Pow(10, 5))), Sym("li")));
        c.put("LINE:ASTRONMICALUNIT", Mul(Rat(Mul(Int("8975872242"), Pow(10, 6)), Int(127)), Sym("li")));
        c.put("LINE:ATTOMETER", Mul(Rat(Int(3), Mul(Int(635), Pow(10, 13))), Sym("li")));
        c.put("LINE:CENTIMETER", Mul(Rat(Int(600), Int(127)), Sym("li")));
        c.put("LINE:DECAMETER", Mul(Rat(Mul(Int(6), Pow(10, 5)), Int(127)), Sym("li")));
        c.put("LINE:DECIMETER", Mul(Rat(Int(6000), Int(127)), Sym("li")));
        c.put("LINE:EXAMETER", Mul(Rat(Mul(Int(6), Pow(10, 22)), Int(127)), Sym("li")));
        c.put("LINE:FEMTOMETER", Mul(Rat(Int(3), Mul(Int(635), Pow(10, 10))), Sym("li")));
        c.put("LINE:FOOT", Mul(Int(144), Sym("li")));
        c.put("LINE:GIGAMETER", Mul(Rat(Mul(Int(6), Pow(10, 13)), Int(127)), Sym("li")));
        c.put("LINE:HECTOMETER", Mul(Rat(Mul(Int(6), Pow(10, 6)), Int(127)), Sym("li")));
        c.put("LINE:INCH", Mul(Int(12), Sym("li")));
        c.put("LINE:KILOMETER", Mul(Rat(Mul(Int(6), Pow(10, 7)), Int(127)), Sym("li")));
        c.put("LINE:LIGHTYEAR", Mul(Rat(Mul(Int("567643828354848"), Pow(10, 6)), Int(127)), Sym("li")));
        c.put("LINE:MEGAMETER", Mul(Rat(Mul(Int(6), Pow(10, 10)), Int(127)), Sym("li")));
        c.put("LINE:METER", Mul(Rat(Mul(Int(6), Pow(10, 4)), Int(127)), Sym("li")));
        c.put("LINE:MICROMETER", Mul(Rat(Int(3), Int(6350)), Sym("li")));
        c.put("LINE:MILE", Mul(Int(760320), Sym("li")));
        c.put("LINE:MILLIMETER", Mul(Rat(Int(60), Int(127)), Sym("li")));
        c.put("LINE:NANOMETER", Mul(Rat(Int(3), Mul(Int(635), Pow(10, 4))), Sym("li")));
        c.put("LINE:PARSEC", Mul(Rat(Mul(Int(185140656), Pow(10, 13)), Int(127)), Sym("li")));
        c.put("LINE:PETAMETER", Mul(Rat(Mul(Int(6), Pow(10, 19)), Int(127)), Sym("li")));
        c.put("LINE:PICOMETER", Mul(Rat(Int(3), Mul(Int(635), Pow(10, 7))), Sym("li")));
        c.put("LINE:POINT", Mul(Rat(Int(1), Int(6)), Sym("li")));
        c.put("LINE:TERAMETER", Mul(Rat(Mul(Int(6), Pow(10, 16)), Int(127)), Sym("li")));
        c.put("LINE:THOU", Mul(Rat(Int(3), Int(250)), Sym("li")));
        c.put("LINE:YARD", Mul(Int(432), Sym("li")));
        c.put("LINE:YOCTOMETER", Mul(Rat(Int(3), Mul(Int(635), Pow(10, 19))), Sym("li")));
        c.put("LINE:YOTTAMETER", Mul(Rat(Mul(Int(6), Pow(10, 28)), Int(127)), Sym("li")));
        c.put("LINE:ZEPTOMETER", Mul(Rat(Int(3), Mul(Int(635), Pow(10, 16))), Sym("li")));
        c.put("LINE:ZETTAMETER", Mul(Rat(Mul(Int(6), Pow(10, 25)), Int(127)), Sym("li")));
        c.put("MEGAMETER:ANGSTROM", Mul(Rat(Int(1), Pow(10, 16)), Sym("megam")));
        c.put("MEGAMETER:ASTRONMICALUNIT", Mul(Rat(Int(1495978707), Pow(10, 4)), Sym("megam")));
        c.put("MEGAMETER:ATTOMETER", Mul(Rat(Int(1), Pow(10, 24)), Sym("megam")));
        c.put("MEGAMETER:CENTIMETER", Mul(Rat(Int(1), Pow(10, 8)), Sym("megam")));
        c.put("MEGAMETER:DECAMETER", Mul(Rat(Int(1), Pow(10, 5)), Sym("megam")));
        c.put("MEGAMETER:DECIMETER", Mul(Rat(Int(1), Pow(10, 7)), Sym("megam")));
        c.put("MEGAMETER:EXAMETER", Mul(Pow(10, 12), Sym("megam")));
        c.put("MEGAMETER:FEMTOMETER", Mul(Rat(Int(1), Pow(10, 21)), Sym("megam")));
        c.put("MEGAMETER:FOOT", Mul(Rat(Int(381), Mul(Int(125), Pow(10, 7))), Sym("megam")));
        c.put("MEGAMETER:GIGAMETER", Mul(Int(1000), Sym("megam")));
        c.put("MEGAMETER:HECTOMETER", Mul(Rat(Int(1), Pow(10, 4)), Sym("megam")));
        c.put("MEGAMETER:INCH", Mul(Rat(Int(127), Mul(Int(5), Pow(10, 9))), Sym("megam")));
        c.put("MEGAMETER:KILOMETER", Mul(Rat(Int(1), Int(1000)), Sym("megam")));
        c.put("MEGAMETER:LIGHTYEAR", Mul(Rat(Int("5912956545363"), Int(625)), Sym("megam")));
        c.put("MEGAMETER:LINE", Mul(Rat(Int(127), Mul(Int(6), Pow(10, 10))), Sym("megam")));
        c.put("MEGAMETER:METER", Mul(Rat(Int(1), Pow(10, 6)), Sym("megam")));
        c.put("MEGAMETER:MICROMETER", Mul(Rat(Int(1), Pow(10, 12)), Sym("megam")));
        c.put("MEGAMETER:MILE", Mul(Rat(Int(12573), Int(7812500)), Sym("megam")));
        c.put("MEGAMETER:MILLIMETER", Mul(Rat(Int(1), Pow(10, 9)), Sym("megam")));
        c.put("MEGAMETER:NANOMETER", Mul(Rat(Int(1), Pow(10, 15)), Sym("megam")));
        c.put("MEGAMETER:PARSEC", Mul(Int("30856776000"), Sym("megam")));
        c.put("MEGAMETER:PETAMETER", Mul(Pow(10, 9), Sym("megam")));
        c.put("MEGAMETER:PICOMETER", Mul(Rat(Int(1), Pow(10, 18)), Sym("megam")));
        c.put("MEGAMETER:POINT", Mul(Rat(Int(127), Mul(Int(36), Pow(10, 10))), Sym("megam")));
        c.put("MEGAMETER:TERAMETER", Mul(Pow(10, 6), Sym("megam")));
        c.put("MEGAMETER:THOU", Mul(Rat(Int(127), Mul(Int(5), Pow(10, 12))), Sym("megam")));
        c.put("MEGAMETER:YARD", Mul(Rat(Int(1143), Mul(Int(125), Pow(10, 7))), Sym("megam")));
        c.put("MEGAMETER:YOCTOMETER", Mul(Rat(Int(1), Pow(10, 30)), Sym("megam")));
        c.put("MEGAMETER:YOTTAMETER", Mul(Pow(10, 18), Sym("megam")));
        c.put("MEGAMETER:ZEPTOMETER", Mul(Rat(Int(1), Pow(10, 27)), Sym("megam")));
        c.put("MEGAMETER:ZETTAMETER", Mul(Pow(10, 15), Sym("megam")));
        c.put("METER:ANGSTROM", Mul(Rat(Int(1), Pow(10, 10)), Sym("m")));
        c.put("METER:ASTRONMICALUNIT", Mul(Int("149597870700"), Sym("m")));
        c.put("METER:ATTOMETER", Mul(Rat(Int(1), Pow(10, 18)), Sym("m")));
        c.put("METER:CENTIMETER", Mul(Rat(Int(1), Int(100)), Sym("m")));
        c.put("METER:DECAMETER", Mul(Int(10), Sym("m")));
        c.put("METER:DECIMETER", Mul(Rat(Int(1), Int(10)), Sym("m")));
        c.put("METER:EXAMETER", Mul(Pow(10, 18), Sym("m")));
        c.put("METER:FEMTOMETER", Mul(Rat(Int(1), Pow(10, 15)), Sym("m")));
        c.put("METER:FOOT", Mul(Rat(Int(381), Int(1250)), Sym("m")));
        c.put("METER:GIGAMETER", Mul(Pow(10, 9), Sym("m")));
        c.put("METER:HECTOMETER", Mul(Int(100), Sym("m")));
        c.put("METER:INCH", Mul(Rat(Int(127), Int(5000)), Sym("m")));
        c.put("METER:KILOMETER", Mul(Int(1000), Sym("m")));
        c.put("METER:LIGHTYEAR", Mul(Int("9460730472580800"), Sym("m")));
        c.put("METER:LINE", Mul(Rat(Int(127), Mul(Int(6), Pow(10, 4))), Sym("m")));
        c.put("METER:MEGAMETER", Mul(Pow(10, 6), Sym("m")));
        c.put("METER:MICROMETER", Mul(Rat(Int(1), Pow(10, 6)), Sym("m")));
        c.put("METER:MILE", Mul(Rat(Int(201168), Int(125)), Sym("m")));
        c.put("METER:MILLIMETER", Mul(Rat(Int(1), Int(1000)), Sym("m")));
        c.put("METER:NANOMETER", Mul(Rat(Int(1), Pow(10, 9)), Sym("m")));
        c.put("METER:PARSEC", Mul(Mul(Int(30856776), Pow(10, 9)), Sym("m")));
        c.put("METER:PETAMETER", Mul(Pow(10, 15), Sym("m")));
        c.put("METER:PICOMETER", Mul(Rat(Int(1), Pow(10, 12)), Sym("m")));
        c.put("METER:POINT", Mul(Rat(Int(127), Mul(Int(36), Pow(10, 4))), Sym("m")));
        c.put("METER:TERAMETER", Mul(Pow(10, 12), Sym("m")));
        c.put("METER:THOU", Mul(Rat(Int(127), Mul(Int(5), Pow(10, 6))), Sym("m")));
        c.put("METER:YARD", Mul(Rat(Int(1143), Int(1250)), Sym("m")));
        c.put("METER:YOCTOMETER", Mul(Rat(Int(1), Pow(10, 24)), Sym("m")));
        c.put("METER:YOTTAMETER", Mul(Pow(10, 24), Sym("m")));
        c.put("METER:ZEPTOMETER", Mul(Rat(Int(1), Pow(10, 21)), Sym("m")));
        c.put("METER:ZETTAMETER", Mul(Pow(10, 21), Sym("m")));
        c.put("MICROMETER:ANGSTROM", Mul(Rat(Int(1), Pow(10, 4)), Sym("microm")));
        c.put("MICROMETER:ASTRONMICALUNIT", Mul(Mul(Int(1495978707), Pow(10, 8)), Sym("microm")));
        c.put("MICROMETER:ATTOMETER", Mul(Rat(Int(1), Pow(10, 12)), Sym("microm")));
        c.put("MICROMETER:CENTIMETER", Mul(Pow(10, 4), Sym("microm")));
        c.put("MICROMETER:DECAMETER", Mul(Pow(10, 7), Sym("microm")));
        c.put("MICROMETER:DECIMETER", Mul(Pow(10, 5), Sym("microm")));
        c.put("MICROMETER:EXAMETER", Mul(Pow(10, 24), Sym("microm")));
        c.put("MICROMETER:FEMTOMETER", Mul(Rat(Int(1), Pow(10, 9)), Sym("microm")));
        c.put("MICROMETER:FOOT", Mul(Int(304800), Sym("microm")));
        c.put("MICROMETER:GIGAMETER", Mul(Pow(10, 15), Sym("microm")));
        c.put("MICROMETER:HECTOMETER", Mul(Pow(10, 8), Sym("microm")));
        c.put("MICROMETER:INCH", Mul(Int(25400), Sym("microm")));
        c.put("MICROMETER:KILOMETER", Mul(Pow(10, 9), Sym("microm")));
        c.put("MICROMETER:LIGHTYEAR", Mul(Mul(Int("94607304725808"), Pow(10, 8)), Sym("microm")));
        c.put("MICROMETER:LINE", Mul(Rat(Int(6350), Int(3)), Sym("microm")));
        c.put("MICROMETER:MEGAMETER", Mul(Pow(10, 12), Sym("microm")));
        c.put("MICROMETER:METER", Mul(Pow(10, 6), Sym("microm")));
        c.put("MICROMETER:MILE", Mul(Int(1609344000), Sym("microm")));
        c.put("MICROMETER:MILLIMETER", Mul(Int(1000), Sym("microm")));
        c.put("MICROMETER:NANOMETER", Mul(Rat(Int(1), Int(1000)), Sym("microm")));
        c.put("MICROMETER:PARSEC", Mul(Mul(Int(30856776), Pow(10, 15)), Sym("microm")));
        c.put("MICROMETER:PETAMETER", Mul(Pow(10, 21), Sym("microm")));
        c.put("MICROMETER:PICOMETER", Mul(Rat(Int(1), Pow(10, 6)), Sym("microm")));
        c.put("MICROMETER:POINT", Mul(Rat(Int(3175), Int(9)), Sym("microm")));
        c.put("MICROMETER:TERAMETER", Mul(Pow(10, 18), Sym("microm")));
        c.put("MICROMETER:THOU", Mul(Rat(Int(127), Int(5)), Sym("microm")));
        c.put("MICROMETER:YARD", Mul(Int(914400), Sym("microm")));
        c.put("MICROMETER:YOCTOMETER", Mul(Rat(Int(1), Pow(10, 18)), Sym("microm")));
        c.put("MICROMETER:YOTTAMETER", Mul(Pow(10, 30), Sym("microm")));
        c.put("MICROMETER:ZEPTOMETER", Mul(Rat(Int(1), Pow(10, 15)), Sym("microm")));
        c.put("MICROMETER:ZETTAMETER", Mul(Pow(10, 27), Sym("microm")));
        c.put("MILE:ANGSTROM", Mul(Rat(Int(1), Mul(Int(1609344), Pow(10, 7))), Sym("mi")));
        c.put("MILE:ASTRONMICALUNIT", Mul(Rat(Int("1558311153125"), Int(16764)), Sym("mi")));
        c.put("MILE:ATTOMETER", Mul(Rat(Int(1), Mul(Int(1609344), Pow(10, 15))), Sym("mi")));
        c.put("MILE:CENTIMETER", Mul(Rat(Int(5), Int(804672)), Sym("mi")));
        c.put("MILE:DECAMETER", Mul(Rat(Int(625), Int(100584)), Sym("mi")));
        c.put("MILE:DECIMETER", Mul(Rat(Int(25), Int(402336)), Sym("mi")));
        c.put("MILE:EXAMETER", Mul(Rat(Mul(Int(78125), Pow(10, 14)), Int(12573)), Sym("mi")));
        c.put("MILE:FEMTOMETER", Mul(Rat(Int(1), Mul(Int(1609344), Pow(10, 12))), Sym("mi")));
        c.put("MILE:FOOT", Mul(Rat(Int(1), Int(5280)), Sym("mi")));
        c.put("MILE:GIGAMETER", Mul(Rat(Mul(Int(78125), Pow(10, 5)), Int(12573)), Sym("mi")));
        c.put("MILE:HECTOMETER", Mul(Rat(Int(3125), Int(50292)), Sym("mi")));
        c.put("MILE:INCH", Mul(Rat(Int(1), Int(63360)), Sym("mi")));
        c.put("MILE:KILOMETER", Mul(Rat(Int(15625), Int(25146)), Sym("mi")));
        c.put("MILE:LIGHTYEAR", Mul(Rat(Int("8212439646337500"), Int(1397)), Sym("mi")));
        c.put("MILE:LINE", Mul(Rat(Int(1), Int(760320)), Sym("mi")));
        c.put("MILE:MEGAMETER", Mul(Rat(Int(7812500), Int(12573)), Sym("mi")));
        c.put("MILE:METER", Mul(Rat(Int(125), Int(201168)), Sym("mi")));
        c.put("MILE:MICROMETER", Mul(Rat(Int(1), Int(1609344000)), Sym("mi")));
        c.put("MILE:MILLIMETER", Mul(Rat(Int(1), Int(1609344)), Sym("mi")));
        c.put("MILE:NANOMETER", Mul(Rat(Int(1), Mul(Int(1609344), Pow(10, 6))), Sym("mi")));
        c.put("MILE:PARSEC", Mul(Rat(Mul(Int(803561875), Pow(10, 8)), Int(4191)), Sym("mi")));
        c.put("MILE:PETAMETER", Mul(Rat(Mul(Int(78125), Pow(10, 11)), Int(12573)), Sym("mi")));
        c.put("MILE:PICOMETER", Mul(Rat(Int(1), Mul(Int(1609344), Pow(10, 9))), Sym("mi")));
        c.put("MILE:POINT", Mul(Rat(Int(1), Int(4561920)), Sym("mi")));
        c.put("MILE:TERAMETER", Mul(Rat(Mul(Int(78125), Pow(10, 8)), Int(12573)), Sym("mi")));
        c.put("MILE:THOU", Mul(Rat(Int(1), Mul(Int(6336), Pow(10, 4))), Sym("mi")));
        c.put("MILE:YARD", Mul(Rat(Int(1), Int(1760)), Sym("mi")));
        c.put("MILE:YOCTOMETER", Mul(Rat(Int(1), Mul(Int(1609344), Pow(10, 21))), Sym("mi")));
        c.put("MILE:YOTTAMETER", Mul(Rat(Mul(Int(78125), Pow(10, 20)), Int(12573)), Sym("mi")));
        c.put("MILE:ZEPTOMETER", Mul(Rat(Int(1), Mul(Int(1609344), Pow(10, 18))), Sym("mi")));
        c.put("MILE:ZETTAMETER", Mul(Rat(Mul(Int(78125), Pow(10, 17)), Int(12573)), Sym("mi")));
        c.put("MILLIMETER:ANGSTROM", Mul(Rat(Int(1), Pow(10, 7)), Sym("millim")));
        c.put("MILLIMETER:ASTRONMICALUNIT", Mul(Mul(Int(1495978707), Pow(10, 5)), Sym("millim")));
        c.put("MILLIMETER:ATTOMETER", Mul(Rat(Int(1), Pow(10, 15)), Sym("millim")));
        c.put("MILLIMETER:CENTIMETER", Mul(Int(10), Sym("millim")));
        c.put("MILLIMETER:DECAMETER", Mul(Pow(10, 4), Sym("millim")));
        c.put("MILLIMETER:DECIMETER", Mul(Int(100), Sym("millim")));
        c.put("MILLIMETER:EXAMETER", Mul(Pow(10, 21), Sym("millim")));
        c.put("MILLIMETER:FEMTOMETER", Mul(Rat(Int(1), Pow(10, 12)), Sym("millim")));
        c.put("MILLIMETER:FOOT", Mul(Rat(Int(1524), Int(5)), Sym("millim")));
        c.put("MILLIMETER:GIGAMETER", Mul(Pow(10, 12), Sym("millim")));
        c.put("MILLIMETER:HECTOMETER", Mul(Pow(10, 5), Sym("millim")));
        c.put("MILLIMETER:INCH", Mul(Rat(Int(127), Int(5)), Sym("millim")));
        c.put("MILLIMETER:KILOMETER", Mul(Pow(10, 6), Sym("millim")));
        c.put("MILLIMETER:LIGHTYEAR", Mul(Mul(Int("94607304725808"), Pow(10, 5)), Sym("millim")));
        c.put("MILLIMETER:LINE", Mul(Rat(Int(127), Int(60)), Sym("millim")));
        c.put("MILLIMETER:MEGAMETER", Mul(Pow(10, 9), Sym("millim")));
        c.put("MILLIMETER:METER", Mul(Int(1000), Sym("millim")));
        c.put("MILLIMETER:MICROMETER", Mul(Rat(Int(1), Int(1000)), Sym("millim")));
        c.put("MILLIMETER:MILE", Mul(Int(1609344), Sym("millim")));
        c.put("MILLIMETER:NANOMETER", Mul(Rat(Int(1), Pow(10, 6)), Sym("millim")));
        c.put("MILLIMETER:PARSEC", Mul(Mul(Int(30856776), Pow(10, 12)), Sym("millim")));
        c.put("MILLIMETER:PETAMETER", Mul(Pow(10, 18), Sym("millim")));
        c.put("MILLIMETER:PICOMETER", Mul(Rat(Int(1), Pow(10, 9)), Sym("millim")));
        c.put("MILLIMETER:POINT", Mul(Rat(Int(127), Int(360)), Sym("millim")));
        c.put("MILLIMETER:TERAMETER", Mul(Pow(10, 15), Sym("millim")));
        c.put("MILLIMETER:THOU", Mul(Rat(Int(127), Int(5000)), Sym("millim")));
        c.put("MILLIMETER:YARD", Mul(Rat(Int(4572), Int(5)), Sym("millim")));
        c.put("MILLIMETER:YOCTOMETER", Mul(Rat(Int(1), Pow(10, 21)), Sym("millim")));
        c.put("MILLIMETER:YOTTAMETER", Mul(Pow(10, 27), Sym("millim")));
        c.put("MILLIMETER:ZEPTOMETER", Mul(Rat(Int(1), Pow(10, 18)), Sym("millim")));
        c.put("MILLIMETER:ZETTAMETER", Mul(Pow(10, 24), Sym("millim")));
        c.put("NANOMETER:ANGSTROM", Mul(Rat(Int(1), Int(10)), Sym("nanom")));
        c.put("NANOMETER:ASTRONMICALUNIT", Mul(Mul(Int(1495978707), Pow(10, 11)), Sym("nanom")));
        c.put("NANOMETER:ATTOMETER", Mul(Rat(Int(1), Pow(10, 9)), Sym("nanom")));
        c.put("NANOMETER:CENTIMETER", Mul(Pow(10, 7), Sym("nanom")));
        c.put("NANOMETER:DECAMETER", Mul(Pow(10, 10), Sym("nanom")));
        c.put("NANOMETER:DECIMETER", Mul(Pow(10, 8), Sym("nanom")));
        c.put("NANOMETER:EXAMETER", Mul(Pow(10, 27), Sym("nanom")));
        c.put("NANOMETER:FEMTOMETER", Mul(Rat(Int(1), Pow(10, 6)), Sym("nanom")));
        c.put("NANOMETER:FOOT", Mul(Mul(Int(3048), Pow(10, 5)), Sym("nanom")));
        c.put("NANOMETER:GIGAMETER", Mul(Pow(10, 18), Sym("nanom")));
        c.put("NANOMETER:HECTOMETER", Mul(Pow(10, 11), Sym("nanom")));
        c.put("NANOMETER:INCH", Mul(Mul(Int(254), Pow(10, 5)), Sym("nanom")));
        c.put("NANOMETER:KILOMETER", Mul(Pow(10, 12), Sym("nanom")));
        c.put("NANOMETER:LIGHTYEAR", Mul(Mul(Int("94607304725808"), Pow(10, 11)), Sym("nanom")));
        c.put("NANOMETER:LINE", Mul(Rat(Mul(Int(635), Pow(10, 4)), Int(3)), Sym("nanom")));
        c.put("NANOMETER:MEGAMETER", Mul(Pow(10, 15), Sym("nanom")));
        c.put("NANOMETER:METER", Mul(Pow(10, 9), Sym("nanom")));
        c.put("NANOMETER:MICROMETER", Mul(Int(1000), Sym("nanom")));
        c.put("NANOMETER:MILE", Mul(Mul(Int(1609344), Pow(10, 6)), Sym("nanom")));
        c.put("NANOMETER:MILLIMETER", Mul(Pow(10, 6), Sym("nanom")));
        c.put("NANOMETER:PARSEC", Mul(Mul(Int(30856776), Pow(10, 18)), Sym("nanom")));
        c.put("NANOMETER:PETAMETER", Mul(Pow(10, 24), Sym("nanom")));
        c.put("NANOMETER:PICOMETER", Mul(Rat(Int(1), Int(1000)), Sym("nanom")));
        c.put("NANOMETER:POINT", Mul(Rat(Int(3175000), Int(9)), Sym("nanom")));
        c.put("NANOMETER:TERAMETER", Mul(Pow(10, 21), Sym("nanom")));
        c.put("NANOMETER:THOU", Mul(Int(25400), Sym("nanom")));
        c.put("NANOMETER:YARD", Mul(Mul(Int(9144), Pow(10, 5)), Sym("nanom")));
        c.put("NANOMETER:YOCTOMETER", Mul(Rat(Int(1), Pow(10, 15)), Sym("nanom")));
        c.put("NANOMETER:YOTTAMETER", Mul(Pow(10, 33), Sym("nanom")));
        c.put("NANOMETER:ZEPTOMETER", Mul(Rat(Int(1), Pow(10, 12)), Sym("nanom")));
        c.put("NANOMETER:ZETTAMETER", Mul(Pow(10, 30), Sym("nanom")));
        c.put("PARSEC:ANGSTROM", Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 19))), Sym("pc")));
        c.put("PARSEC:ASTRONMICALUNIT", Mul(Rat(Int(498659569), Mul(Int(10285592), Pow(10, 7))), Sym("pc")));
        c.put("PARSEC:ATTOMETER", Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 27))), Sym("pc")));
        c.put("PARSEC:CENTIMETER", Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 11))), Sym("pc")));
        c.put("PARSEC:DECAMETER", Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 8))), Sym("pc")));
        c.put("PARSEC:DECIMETER", Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 10))), Sym("pc")));
        c.put("PARSEC:EXAMETER", Mul(Rat(Mul(Int(125), Pow(10, 6)), Int(3857097)), Sym("pc")));
        c.put("PARSEC:FEMTOMETER", Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 24))), Sym("pc")));
        c.put("PARSEC:FOOT", Mul(Rat(Int(127), Mul(Int(1285699), Pow(10, 13))), Sym("pc")));
        c.put("PARSEC:GIGAMETER", Mul(Rat(Int(1), Int(30856776)), Sym("pc")));
        c.put("PARSEC:HECTOMETER", Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 7))), Sym("pc")));
        c.put("PARSEC:INCH", Mul(Rat(Int(127), Mul(Int(15428388), Pow(10, 13))), Sym("pc")));
        c.put("PARSEC:KILOMETER", Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 6))), Sym("pc")));
        c.put("PARSEC:LIGHTYEAR", Mul(Rat(Int("1970985515121"), Mul(Int(6428495), Pow(10, 6))), Sym("pc")));
        c.put("PARSEC:LINE", Mul(Rat(Int(127), Mul(Int(185140656), Pow(10, 13))), Sym("pc")));
        c.put("PARSEC:MEGAMETER", Mul(Rat(Int(1), Int("30856776000")), Sym("pc")));
        c.put("PARSEC:METER", Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 9))), Sym("pc")));
        c.put("PARSEC:MICROMETER", Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 15))), Sym("pc")));
        c.put("PARSEC:MILE", Mul(Rat(Int(4191), Mul(Int(803561875), Pow(10, 8))), Sym("pc")));
        c.put("PARSEC:MILLIMETER", Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 12))), Sym("pc")));
        c.put("PARSEC:NANOMETER", Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 18))), Sym("pc")));
        c.put("PARSEC:PETAMETER", Mul(Rat(Int(125000), Int(3857097)), Sym("pc")));
        c.put("PARSEC:PICOMETER", Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 21))), Sym("pc")));
        c.put("PARSEC:POINT", Mul(Rat(Int(127), Mul(Int(1110843936), Pow(10, 13))), Sym("pc")));
        c.put("PARSEC:TERAMETER", Mul(Rat(Int(125), Int(3857097)), Sym("pc")));
        c.put("PARSEC:THOU", Mul(Rat(Int(127), Mul(Int(15428388), Pow(10, 16))), Sym("pc")));
        c.put("PARSEC:YARD", Mul(Rat(Int(381), Mul(Int(1285699), Pow(10, 13))), Sym("pc")));
        c.put("PARSEC:YOCTOMETER", Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 33))), Sym("pc")));
        c.put("PARSEC:YOTTAMETER", Mul(Rat(Mul(Int(125), Pow(10, 12)), Int(3857097)), Sym("pc")));
        c.put("PARSEC:ZEPTOMETER", Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 30))), Sym("pc")));
        c.put("PARSEC:ZETTAMETER", Mul(Rat(Mul(Int(125), Pow(10, 9)), Int(3857097)), Sym("pc")));
        c.put("PETAMETER:ANGSTROM", Mul(Rat(Int(1), Pow(10, 25)), Sym("petam")));
        c.put("PETAMETER:ASTRONMICALUNIT", Mul(Rat(Int(1495978707), Pow(10, 13)), Sym("petam")));
        c.put("PETAMETER:ATTOMETER", Mul(Rat(Int(1), Pow(10, 33)), Sym("petam")));
        c.put("PETAMETER:CENTIMETER", Mul(Rat(Int(1), Pow(10, 17)), Sym("petam")));
        c.put("PETAMETER:DECAMETER", Mul(Rat(Int(1), Pow(10, 14)), Sym("petam")));
        c.put("PETAMETER:DECIMETER", Mul(Rat(Int(1), Pow(10, 16)), Sym("petam")));
        c.put("PETAMETER:EXAMETER", Mul(Int(1000), Sym("petam")));
        c.put("PETAMETER:FEMTOMETER", Mul(Rat(Int(1), Pow(10, 30)), Sym("petam")));
        c.put("PETAMETER:FOOT", Mul(Rat(Int(381), Mul(Int(125), Pow(10, 16))), Sym("petam")));
        c.put("PETAMETER:GIGAMETER", Mul(Rat(Int(1), Pow(10, 6)), Sym("petam")));
        c.put("PETAMETER:HECTOMETER", Mul(Rat(Int(1), Pow(10, 13)), Sym("petam")));
        c.put("PETAMETER:INCH", Mul(Rat(Int(127), Mul(Int(5), Pow(10, 18))), Sym("petam")));
        c.put("PETAMETER:KILOMETER", Mul(Rat(Int(1), Pow(10, 12)), Sym("petam")));
        c.put("PETAMETER:LIGHTYEAR", Mul(Rat(Int("5912956545363"), Mul(Int(625), Pow(10, 9))), Sym("petam")));
        c.put("PETAMETER:LINE", Mul(Rat(Int(127), Mul(Int(6), Pow(10, 19))), Sym("petam")));
        c.put("PETAMETER:MEGAMETER", Mul(Rat(Int(1), Pow(10, 9)), Sym("petam")));
        c.put("PETAMETER:METER", Mul(Rat(Int(1), Pow(10, 15)), Sym("petam")));
        c.put("PETAMETER:MICROMETER", Mul(Rat(Int(1), Pow(10, 21)), Sym("petam")));
        c.put("PETAMETER:MILE", Mul(Rat(Int(12573), Mul(Int(78125), Pow(10, 11))), Sym("petam")));
        c.put("PETAMETER:MILLIMETER", Mul(Rat(Int(1), Pow(10, 18)), Sym("petam")));
        c.put("PETAMETER:NANOMETER", Mul(Rat(Int(1), Pow(10, 24)), Sym("petam")));
        c.put("PETAMETER:PARSEC", Mul(Rat(Int(3857097), Int(125000)), Sym("petam")));
        c.put("PETAMETER:PICOMETER", Mul(Rat(Int(1), Pow(10, 27)), Sym("petam")));
        c.put("PETAMETER:POINT", Mul(Rat(Int(127), Mul(Int(36), Pow(10, 19))), Sym("petam")));
        c.put("PETAMETER:TERAMETER", Mul(Rat(Int(1), Int(1000)), Sym("petam")));
        c.put("PETAMETER:THOU", Mul(Rat(Int(127), Mul(Int(5), Pow(10, 21))), Sym("petam")));
        c.put("PETAMETER:YARD", Mul(Rat(Int(1143), Mul(Int(125), Pow(10, 16))), Sym("petam")));
        c.put("PETAMETER:YOCTOMETER", Mul(Rat(Int(1), Pow(10, 39)), Sym("petam")));
        c.put("PETAMETER:YOTTAMETER", Mul(Pow(10, 9), Sym("petam")));
        c.put("PETAMETER:ZEPTOMETER", Mul(Rat(Int(1), Pow(10, 36)), Sym("petam")));
        c.put("PETAMETER:ZETTAMETER", Mul(Pow(10, 6), Sym("petam")));
        c.put("PICOMETER:ANGSTROM", Mul(Int(100), Sym("picom")));
        c.put("PICOMETER:ASTRONMICALUNIT", Mul(Mul(Int(1495978707), Pow(10, 14)), Sym("picom")));
        c.put("PICOMETER:ATTOMETER", Mul(Rat(Int(1), Pow(10, 6)), Sym("picom")));
        c.put("PICOMETER:CENTIMETER", Mul(Pow(10, 10), Sym("picom")));
        c.put("PICOMETER:DECAMETER", Mul(Pow(10, 13), Sym("picom")));
        c.put("PICOMETER:DECIMETER", Mul(Pow(10, 11), Sym("picom")));
        c.put("PICOMETER:EXAMETER", Mul(Pow(10, 30), Sym("picom")));
        c.put("PICOMETER:FEMTOMETER", Mul(Rat(Int(1), Int(1000)), Sym("picom")));
        c.put("PICOMETER:FOOT", Mul(Mul(Int(3048), Pow(10, 8)), Sym("picom")));
        c.put("PICOMETER:GIGAMETER", Mul(Pow(10, 21), Sym("picom")));
        c.put("PICOMETER:HECTOMETER", Mul(Pow(10, 14), Sym("picom")));
        c.put("PICOMETER:INCH", Mul(Mul(Int(254), Pow(10, 8)), Sym("picom")));
        c.put("PICOMETER:KILOMETER", Mul(Pow(10, 15), Sym("picom")));
        c.put("PICOMETER:LIGHTYEAR", Mul(Mul(Int("94607304725808"), Pow(10, 14)), Sym("picom")));
        c.put("PICOMETER:LINE", Mul(Rat(Mul(Int(635), Pow(10, 7)), Int(3)), Sym("picom")));
        c.put("PICOMETER:MEGAMETER", Mul(Pow(10, 18), Sym("picom")));
        c.put("PICOMETER:METER", Mul(Pow(10, 12), Sym("picom")));
        c.put("PICOMETER:MICROMETER", Mul(Pow(10, 6), Sym("picom")));
        c.put("PICOMETER:MILE", Mul(Mul(Int(1609344), Pow(10, 9)), Sym("picom")));
        c.put("PICOMETER:MILLIMETER", Mul(Pow(10, 9), Sym("picom")));
        c.put("PICOMETER:NANOMETER", Mul(Int(1000), Sym("picom")));
        c.put("PICOMETER:PARSEC", Mul(Mul(Int(30856776), Pow(10, 21)), Sym("picom")));
        c.put("PICOMETER:PETAMETER", Mul(Pow(10, 27), Sym("picom")));
        c.put("PICOMETER:POINT", Mul(Rat(Mul(Int(3175), Pow(10, 6)), Int(9)), Sym("picom")));
        c.put("PICOMETER:TERAMETER", Mul(Pow(10, 24), Sym("picom")));
        c.put("PICOMETER:THOU", Mul(Mul(Int(254), Pow(10, 5)), Sym("picom")));
        c.put("PICOMETER:YARD", Mul(Mul(Int(9144), Pow(10, 8)), Sym("picom")));
        c.put("PICOMETER:YOCTOMETER", Mul(Rat(Int(1), Pow(10, 12)), Sym("picom")));
        c.put("PICOMETER:YOTTAMETER", Mul(Pow(10, 36), Sym("picom")));
        c.put("PICOMETER:ZEPTOMETER", Mul(Rat(Int(1), Pow(10, 9)), Sym("picom")));
        c.put("PICOMETER:ZETTAMETER", Mul(Pow(10, 33), Sym("picom")));
        c.put("POINT:ANGSTROM", Mul(Rat(Int(9), Mul(Int(3175), Pow(10, 4))), Sym("pt")));
        c.put("POINT:ASTRONMICALUNIT", Mul(Rat(Mul(Int("53855233452"), Pow(10, 6)), Int(127)), Sym("pt")));
        c.put("POINT:ATTOMETER", Mul(Rat(Int(9), Mul(Int(3175), Pow(10, 12))), Sym("pt")));
        c.put("POINT:CENTIMETER", Mul(Rat(Int(3600), Int(127)), Sym("pt")));
        c.put("POINT:DECAMETER", Mul(Rat(Mul(Int(36), Pow(10, 5)), Int(127)), Sym("pt")));
        c.put("POINT:DECIMETER", Mul(Rat(Int(36000), Int(127)), Sym("pt")));
        c.put("POINT:EXAMETER", Mul(Rat(Mul(Int(36), Pow(10, 22)), Int(127)), Sym("pt")));
        c.put("POINT:FEMTOMETER", Mul(Rat(Int(9), Mul(Int(3175), Pow(10, 9))), Sym("pt")));
        c.put("POINT:FOOT", Mul(Int(864), Sym("pt")));
        c.put("POINT:GIGAMETER", Mul(Rat(Mul(Int(36), Pow(10, 13)), Int(127)), Sym("pt")));
        c.put("POINT:HECTOMETER", Mul(Rat(Mul(Int(36), Pow(10, 6)), Int(127)), Sym("pt")));
        c.put("POINT:INCH", Mul(Int(72), Sym("pt")));
        c.put("POINT:KILOMETER", Mul(Rat(Mul(Int(36), Pow(10, 7)), Int(127)), Sym("pt")));
        c.put("POINT:LIGHTYEAR", Mul(Rat(Mul(Int("3405862970129088"), Pow(10, 6)), Int(127)), Sym("pt")));
        c.put("POINT:LINE", Mul(Int(6), Sym("pt")));
        c.put("POINT:MEGAMETER", Mul(Rat(Mul(Int(36), Pow(10, 10)), Int(127)), Sym("pt")));
        c.put("POINT:METER", Mul(Rat(Mul(Int(36), Pow(10, 4)), Int(127)), Sym("pt")));
        c.put("POINT:MICROMETER", Mul(Rat(Int(9), Int(3175)), Sym("pt")));
        c.put("POINT:MILE", Mul(Int(4561920), Sym("pt")));
        c.put("POINT:MILLIMETER", Mul(Rat(Int(360), Int(127)), Sym("pt")));
        c.put("POINT:NANOMETER", Mul(Rat(Int(9), Int(3175000)), Sym("pt")));
        c.put("POINT:PARSEC", Mul(Rat(Mul(Int(1110843936), Pow(10, 13)), Int(127)), Sym("pt")));
        c.put("POINT:PETAMETER", Mul(Rat(Mul(Int(36), Pow(10, 19)), Int(127)), Sym("pt")));
        c.put("POINT:PICOMETER", Mul(Rat(Int(9), Mul(Int(3175), Pow(10, 6))), Sym("pt")));
        c.put("POINT:TERAMETER", Mul(Rat(Mul(Int(36), Pow(10, 16)), Int(127)), Sym("pt")));
        c.put("POINT:THOU", Mul(Rat(Int(9), Int(125)), Sym("pt")));
        c.put("POINT:YARD", Mul(Int(2592), Sym("pt")));
        c.put("POINT:YOCTOMETER", Mul(Rat(Int(9), Mul(Int(3175), Pow(10, 18))), Sym("pt")));
        c.put("POINT:YOTTAMETER", Mul(Rat(Mul(Int(36), Pow(10, 28)), Int(127)), Sym("pt")));
        c.put("POINT:ZEPTOMETER", Mul(Rat(Int(9), Mul(Int(3175), Pow(10, 15))), Sym("pt")));
        c.put("POINT:ZETTAMETER", Mul(Rat(Mul(Int(36), Pow(10, 25)), Int(127)), Sym("pt")));
        c.put("TERAMETER:ANGSTROM", Mul(Rat(Int(1), Pow(10, 22)), Sym("teram")));
        c.put("TERAMETER:ASTRONMICALUNIT", Mul(Rat(Int(1495978707), Pow(10, 10)), Sym("teram")));
        c.put("TERAMETER:ATTOMETER", Mul(Rat(Int(1), Pow(10, 30)), Sym("teram")));
        c.put("TERAMETER:CENTIMETER", Mul(Rat(Int(1), Pow(10, 14)), Sym("teram")));
        c.put("TERAMETER:DECAMETER", Mul(Rat(Int(1), Pow(10, 11)), Sym("teram")));
        c.put("TERAMETER:DECIMETER", Mul(Rat(Int(1), Pow(10, 13)), Sym("teram")));
        c.put("TERAMETER:EXAMETER", Mul(Pow(10, 6), Sym("teram")));
        c.put("TERAMETER:FEMTOMETER", Mul(Rat(Int(1), Pow(10, 27)), Sym("teram")));
        c.put("TERAMETER:FOOT", Mul(Rat(Int(381), Mul(Int(125), Pow(10, 13))), Sym("teram")));
        c.put("TERAMETER:GIGAMETER", Mul(Rat(Int(1), Int(1000)), Sym("teram")));
        c.put("TERAMETER:HECTOMETER", Mul(Rat(Int(1), Pow(10, 10)), Sym("teram")));
        c.put("TERAMETER:INCH", Mul(Rat(Int(127), Mul(Int(5), Pow(10, 15))), Sym("teram")));
        c.put("TERAMETER:KILOMETER", Mul(Rat(Int(1), Pow(10, 9)), Sym("teram")));
        c.put("TERAMETER:LIGHTYEAR", Mul(Rat(Int("5912956545363"), Mul(Int(625), Pow(10, 6))), Sym("teram")));
        c.put("TERAMETER:LINE", Mul(Rat(Int(127), Mul(Int(6), Pow(10, 16))), Sym("teram")));
        c.put("TERAMETER:MEGAMETER", Mul(Rat(Int(1), Pow(10, 6)), Sym("teram")));
        c.put("TERAMETER:METER", Mul(Rat(Int(1), Pow(10, 12)), Sym("teram")));
        c.put("TERAMETER:MICROMETER", Mul(Rat(Int(1), Pow(10, 18)), Sym("teram")));
        c.put("TERAMETER:MILE", Mul(Rat(Int(12573), Mul(Int(78125), Pow(10, 8))), Sym("teram")));
        c.put("TERAMETER:MILLIMETER", Mul(Rat(Int(1), Pow(10, 15)), Sym("teram")));
        c.put("TERAMETER:NANOMETER", Mul(Rat(Int(1), Pow(10, 21)), Sym("teram")));
        c.put("TERAMETER:PARSEC", Mul(Rat(Int(3857097), Int(125)), Sym("teram")));
        c.put("TERAMETER:PETAMETER", Mul(Int(1000), Sym("teram")));
        c.put("TERAMETER:PICOMETER", Mul(Rat(Int(1), Pow(10, 24)), Sym("teram")));
        c.put("TERAMETER:POINT", Mul(Rat(Int(127), Mul(Int(36), Pow(10, 16))), Sym("teram")));
        c.put("TERAMETER:THOU", Mul(Rat(Int(127), Mul(Int(5), Pow(10, 18))), Sym("teram")));
        c.put("TERAMETER:YARD", Mul(Rat(Int(1143), Mul(Int(125), Pow(10, 13))), Sym("teram")));
        c.put("TERAMETER:YOCTOMETER", Mul(Rat(Int(1), Pow(10, 36)), Sym("teram")));
        c.put("TERAMETER:YOTTAMETER", Mul(Pow(10, 12), Sym("teram")));
        c.put("TERAMETER:ZEPTOMETER", Mul(Rat(Int(1), Pow(10, 33)), Sym("teram")));
        c.put("TERAMETER:ZETTAMETER", Mul(Pow(10, 9), Sym("teram")));
        c.put("THOU:ANGSTROM", Mul(Rat(Int(1), Int(254000)), Sym("thou")));
        c.put("THOU:ASTRONMICALUNIT", Mul(Rat(Mul(Int("7479893535"), Pow(10, 8)), Int(127)), Sym("thou")));
        c.put("THOU:ATTOMETER", Mul(Rat(Int(1), Mul(Int(254), Pow(10, 11))), Sym("thou")));
        c.put("THOU:CENTIMETER", Mul(Rat(Mul(Int(5), Pow(10, 4)), Int(127)), Sym("thou")));
        c.put("THOU:DECAMETER", Mul(Rat(Mul(Int(5), Pow(10, 7)), Int(127)), Sym("thou")));
        c.put("THOU:DECIMETER", Mul(Rat(Mul(Int(5), Pow(10, 5)), Int(127)), Sym("thou")));
        c.put("THOU:EXAMETER", Mul(Rat(Mul(Int(5), Pow(10, 24)), Int(127)), Sym("thou")));
        c.put("THOU:FEMTOMETER", Mul(Rat(Int(1), Mul(Int(254), Pow(10, 8))), Sym("thou")));
        c.put("THOU:FOOT", Mul(Int(12000), Sym("thou")));
        c.put("THOU:GIGAMETER", Mul(Rat(Mul(Int(5), Pow(10, 15)), Int(127)), Sym("thou")));
        c.put("THOU:HECTOMETER", Mul(Rat(Mul(Int(5), Pow(10, 8)), Int(127)), Sym("thou")));
        c.put("THOU:INCH", Mul(Int(1000), Sym("thou")));
        c.put("THOU:KILOMETER", Mul(Rat(Mul(Int(5), Pow(10, 9)), Int(127)), Sym("thou")));
        c.put("THOU:LIGHTYEAR", Mul(Rat(Mul(Int("47303652362904"), Pow(10, 9)), Int(127)), Sym("thou")));
        c.put("THOU:LINE", Mul(Rat(Int(250), Int(3)), Sym("thou")));
        c.put("THOU:MEGAMETER", Mul(Rat(Mul(Int(5), Pow(10, 12)), Int(127)), Sym("thou")));
        c.put("THOU:METER", Mul(Rat(Mul(Int(5), Pow(10, 6)), Int(127)), Sym("thou")));
        c.put("THOU:MICROMETER", Mul(Rat(Int(5), Int(127)), Sym("thou")));
        c.put("THOU:MILE", Mul(Mul(Int(6336), Pow(10, 4)), Sym("thou")));
        c.put("THOU:MILLIMETER", Mul(Rat(Int(5000), Int(127)), Sym("thou")));
        c.put("THOU:NANOMETER", Mul(Rat(Int(1), Int(25400)), Sym("thou")));
        c.put("THOU:PARSEC", Mul(Rat(Mul(Int(15428388), Pow(10, 16)), Int(127)), Sym("thou")));
        c.put("THOU:PETAMETER", Mul(Rat(Mul(Int(5), Pow(10, 21)), Int(127)), Sym("thou")));
        c.put("THOU:PICOMETER", Mul(Rat(Int(1), Mul(Int(254), Pow(10, 5))), Sym("thou")));
        c.put("THOU:POINT", Mul(Rat(Int(125), Int(9)), Sym("thou")));
        c.put("THOU:TERAMETER", Mul(Rat(Mul(Int(5), Pow(10, 18)), Int(127)), Sym("thou")));
        c.put("THOU:YARD", Mul(Int(36000), Sym("thou")));
        c.put("THOU:YOCTOMETER", Mul(Rat(Int(1), Mul(Int(254), Pow(10, 17))), Sym("thou")));
        c.put("THOU:YOTTAMETER", Mul(Rat(Mul(Int(5), Pow(10, 30)), Int(127)), Sym("thou")));
        c.put("THOU:ZEPTOMETER", Mul(Rat(Int(1), Mul(Int(254), Pow(10, 14))), Sym("thou")));
        c.put("THOU:ZETTAMETER", Mul(Rat(Mul(Int(5), Pow(10, 27)), Int(127)), Sym("thou")));
        c.put("YARD:ANGSTROM", Mul(Rat(Int(1), Mul(Int(9144), Pow(10, 6))), Sym("yd")));
        c.put("YARD:ASTRONMICALUNIT", Mul(Rat(Int("62332446125000"), Int(381)), Sym("yd")));
        c.put("YARD:ATTOMETER", Mul(Rat(Int(1), Mul(Int(9144), Pow(10, 14))), Sym("yd")));
        c.put("YARD:CENTIMETER", Mul(Rat(Int(25), Int(2286)), Sym("yd")));
        c.put("YARD:DECAMETER", Mul(Rat(Int(12500), Int(1143)), Sym("yd")));
        c.put("YARD:DECIMETER", Mul(Rat(Int(125), Int(1143)), Sym("yd")));
        c.put("YARD:EXAMETER", Mul(Rat(Mul(Int(125), Pow(10, 19)), Int(1143)), Sym("yd")));
        c.put("YARD:FEMTOMETER", Mul(Rat(Int(1), Mul(Int(9144), Pow(10, 11))), Sym("yd")));
        c.put("YARD:FOOT", Mul(Rat(Int(1), Int(3)), Sym("yd")));
        c.put("YARD:GIGAMETER", Mul(Rat(Mul(Int(125), Pow(10, 10)), Int(1143)), Sym("yd")));
        c.put("YARD:HECTOMETER", Mul(Rat(Int(125000), Int(1143)), Sym("yd")));
        c.put("YARD:INCH", Mul(Rat(Int(1), Int(36)), Sym("yd")));
        c.put("YARD:KILOMETER", Mul(Rat(Mul(Int(125), Pow(10, 4)), Int(1143)), Sym("yd")));
        c.put("YARD:LIGHTYEAR", Mul(Rat(Mul(Int("1313990343414"), Pow(10, 6)), Int(127)), Sym("yd")));
        c.put("YARD:LINE", Mul(Rat(Int(1), Int(432)), Sym("yd")));
        c.put("YARD:MEGAMETER", Mul(Rat(Mul(Int(125), Pow(10, 7)), Int(1143)), Sym("yd")));
        c.put("YARD:METER", Mul(Rat(Int(1250), Int(1143)), Sym("yd")));
        c.put("YARD:MICROMETER", Mul(Rat(Int(1), Int(914400)), Sym("yd")));
        c.put("YARD:MILE", Mul(Int(1760), Sym("yd")));
        c.put("YARD:MILLIMETER", Mul(Rat(Int(5), Int(4572)), Sym("yd")));
        c.put("YARD:NANOMETER", Mul(Rat(Int(1), Mul(Int(9144), Pow(10, 5))), Sym("yd")));
        c.put("YARD:PARSEC", Mul(Rat(Mul(Int(1285699), Pow(10, 13)), Int(381)), Sym("yd")));
        c.put("YARD:PETAMETER", Mul(Rat(Mul(Int(125), Pow(10, 16)), Int(1143)), Sym("yd")));
        c.put("YARD:PICOMETER", Mul(Rat(Int(1), Mul(Int(9144), Pow(10, 8))), Sym("yd")));
        c.put("YARD:POINT", Mul(Rat(Int(1), Int(2592)), Sym("yd")));
        c.put("YARD:TERAMETER", Mul(Rat(Mul(Int(125), Pow(10, 13)), Int(1143)), Sym("yd")));
        c.put("YARD:THOU", Mul(Rat(Int(1), Int(36000)), Sym("yd")));
        c.put("YARD:YOCTOMETER", Mul(Rat(Int(1), Mul(Int(9144), Pow(10, 20))), Sym("yd")));
        c.put("YARD:YOTTAMETER", Mul(Rat(Mul(Int(125), Pow(10, 25)), Int(1143)), Sym("yd")));
        c.put("YARD:ZEPTOMETER", Mul(Rat(Int(1), Mul(Int(9144), Pow(10, 17))), Sym("yd")));
        c.put("YARD:ZETTAMETER", Mul(Rat(Mul(Int(125), Pow(10, 22)), Int(1143)), Sym("yd")));
        c.put("YOCTOMETER:ANGSTROM", Mul(Pow(10, 14), Sym("yoctom")));
        c.put("YOCTOMETER:ASTRONMICALUNIT", Mul(Mul(Int(1495978707), Pow(10, 26)), Sym("yoctom")));
        c.put("YOCTOMETER:ATTOMETER", Mul(Pow(10, 6), Sym("yoctom")));
        c.put("YOCTOMETER:CENTIMETER", Mul(Pow(10, 22), Sym("yoctom")));
        c.put("YOCTOMETER:DECAMETER", Mul(Pow(10, 25), Sym("yoctom")));
        c.put("YOCTOMETER:DECIMETER", Mul(Pow(10, 23), Sym("yoctom")));
        c.put("YOCTOMETER:EXAMETER", Mul(Pow(10, 42), Sym("yoctom")));
        c.put("YOCTOMETER:FEMTOMETER", Mul(Pow(10, 9), Sym("yoctom")));
        c.put("YOCTOMETER:FOOT", Mul(Mul(Int(3048), Pow(10, 20)), Sym("yoctom")));
        c.put("YOCTOMETER:GIGAMETER", Mul(Pow(10, 33), Sym("yoctom")));
        c.put("YOCTOMETER:HECTOMETER", Mul(Pow(10, 26), Sym("yoctom")));
        c.put("YOCTOMETER:INCH", Mul(Mul(Int(254), Pow(10, 20)), Sym("yoctom")));
        c.put("YOCTOMETER:KILOMETER", Mul(Pow(10, 27), Sym("yoctom")));
        c.put("YOCTOMETER:LIGHTYEAR", Mul(Mul(Int("94607304725808"), Pow(10, 26)), Sym("yoctom")));
        c.put("YOCTOMETER:LINE", Mul(Rat(Mul(Int(635), Pow(10, 19)), Int(3)), Sym("yoctom")));
        c.put("YOCTOMETER:MEGAMETER", Mul(Pow(10, 30), Sym("yoctom")));
        c.put("YOCTOMETER:METER", Mul(Pow(10, 24), Sym("yoctom")));
        c.put("YOCTOMETER:MICROMETER", Mul(Pow(10, 18), Sym("yoctom")));
        c.put("YOCTOMETER:MILE", Mul(Mul(Int(1609344), Pow(10, 21)), Sym("yoctom")));
        c.put("YOCTOMETER:MILLIMETER", Mul(Pow(10, 21), Sym("yoctom")));
        c.put("YOCTOMETER:NANOMETER", Mul(Pow(10, 15), Sym("yoctom")));
        c.put("YOCTOMETER:PARSEC", Mul(Mul(Int(30856776), Pow(10, 33)), Sym("yoctom")));
        c.put("YOCTOMETER:PETAMETER", Mul(Pow(10, 39), Sym("yoctom")));
        c.put("YOCTOMETER:PICOMETER", Mul(Pow(10, 12), Sym("yoctom")));
        c.put("YOCTOMETER:POINT", Mul(Rat(Mul(Int(3175), Pow(10, 18)), Int(9)), Sym("yoctom")));
        c.put("YOCTOMETER:TERAMETER", Mul(Pow(10, 36), Sym("yoctom")));
        c.put("YOCTOMETER:THOU", Mul(Mul(Int(254), Pow(10, 17)), Sym("yoctom")));
        c.put("YOCTOMETER:YARD", Mul(Mul(Int(9144), Pow(10, 20)), Sym("yoctom")));
        c.put("YOCTOMETER:YOTTAMETER", Mul(Pow(10, 48), Sym("yoctom")));
        c.put("YOCTOMETER:ZEPTOMETER", Mul(Int(1000), Sym("yoctom")));
        c.put("YOCTOMETER:ZETTAMETER", Mul(Pow(10, 45), Sym("yoctom")));
        c.put("YOTTAMETER:ANGSTROM", Mul(Rat(Int(1), Pow(10, 34)), Sym("yottam")));
        c.put("YOTTAMETER:ASTRONMICALUNIT", Mul(Rat(Int(1495978707), Pow(10, 22)), Sym("yottam")));
        c.put("YOTTAMETER:ATTOMETER", Mul(Rat(Int(1), Pow(10, 42)), Sym("yottam")));
        c.put("YOTTAMETER:CENTIMETER", Mul(Rat(Int(1), Pow(10, 26)), Sym("yottam")));
        c.put("YOTTAMETER:DECAMETER", Mul(Rat(Int(1), Pow(10, 23)), Sym("yottam")));
        c.put("YOTTAMETER:DECIMETER", Mul(Rat(Int(1), Pow(10, 25)), Sym("yottam")));
        c.put("YOTTAMETER:EXAMETER", Mul(Rat(Int(1), Pow(10, 6)), Sym("yottam")));
        c.put("YOTTAMETER:FEMTOMETER", Mul(Rat(Int(1), Pow(10, 39)), Sym("yottam")));
        c.put("YOTTAMETER:FOOT", Mul(Rat(Int(381), Mul(Int(125), Pow(10, 25))), Sym("yottam")));
        c.put("YOTTAMETER:GIGAMETER", Mul(Rat(Int(1), Pow(10, 15)), Sym("yottam")));
        c.put("YOTTAMETER:HECTOMETER", Mul(Rat(Int(1), Pow(10, 22)), Sym("yottam")));
        c.put("YOTTAMETER:INCH", Mul(Rat(Int(127), Mul(Int(5), Pow(10, 27))), Sym("yottam")));
        c.put("YOTTAMETER:KILOMETER", Mul(Rat(Int(1), Pow(10, 21)), Sym("yottam")));
        c.put("YOTTAMETER:LIGHTYEAR", Mul(Rat(Int("5912956545363"), Mul(Int(625), Pow(10, 18))), Sym("yottam")));
        c.put("YOTTAMETER:LINE", Mul(Rat(Int(127), Mul(Int(6), Pow(10, 28))), Sym("yottam")));
        c.put("YOTTAMETER:MEGAMETER", Mul(Rat(Int(1), Pow(10, 18)), Sym("yottam")));
        c.put("YOTTAMETER:METER", Mul(Rat(Int(1), Pow(10, 24)), Sym("yottam")));
        c.put("YOTTAMETER:MICROMETER", Mul(Rat(Int(1), Pow(10, 30)), Sym("yottam")));
        c.put("YOTTAMETER:MILE", Mul(Rat(Int(12573), Mul(Int(78125), Pow(10, 20))), Sym("yottam")));
        c.put("YOTTAMETER:MILLIMETER", Mul(Rat(Int(1), Pow(10, 27)), Sym("yottam")));
        c.put("YOTTAMETER:NANOMETER", Mul(Rat(Int(1), Pow(10, 33)), Sym("yottam")));
        c.put("YOTTAMETER:PARSEC", Mul(Rat(Int(3857097), Mul(Int(125), Pow(10, 12))), Sym("yottam")));
        c.put("YOTTAMETER:PETAMETER", Mul(Rat(Int(1), Pow(10, 9)), Sym("yottam")));
        c.put("YOTTAMETER:PICOMETER", Mul(Rat(Int(1), Pow(10, 36)), Sym("yottam")));
        c.put("YOTTAMETER:POINT", Mul(Rat(Int(127), Mul(Int(36), Pow(10, 28))), Sym("yottam")));
        c.put("YOTTAMETER:TERAMETER", Mul(Rat(Int(1), Pow(10, 12)), Sym("yottam")));
        c.put("YOTTAMETER:THOU", Mul(Rat(Int(127), Mul(Int(5), Pow(10, 30))), Sym("yottam")));
        c.put("YOTTAMETER:YARD", Mul(Rat(Int(1143), Mul(Int(125), Pow(10, 25))), Sym("yottam")));
        c.put("YOTTAMETER:YOCTOMETER", Mul(Rat(Int(1), Pow(10, 48)), Sym("yottam")));
        c.put("YOTTAMETER:ZEPTOMETER", Mul(Rat(Int(1), Pow(10, 45)), Sym("yottam")));
        c.put("YOTTAMETER:ZETTAMETER", Mul(Rat(Int(1), Int(1000)), Sym("yottam")));
        c.put("ZEPTOMETER:ANGSTROM", Mul(Pow(10, 11), Sym("zeptom")));
        c.put("ZEPTOMETER:ASTRONMICALUNIT", Mul(Mul(Int(1495978707), Pow(10, 23)), Sym("zeptom")));
        c.put("ZEPTOMETER:ATTOMETER", Mul(Int(1000), Sym("zeptom")));
        c.put("ZEPTOMETER:CENTIMETER", Mul(Pow(10, 19), Sym("zeptom")));
        c.put("ZEPTOMETER:DECAMETER", Mul(Pow(10, 22), Sym("zeptom")));
        c.put("ZEPTOMETER:DECIMETER", Mul(Pow(10, 20), Sym("zeptom")));
        c.put("ZEPTOMETER:EXAMETER", Mul(Pow(10, 39), Sym("zeptom")));
        c.put("ZEPTOMETER:FEMTOMETER", Mul(Pow(10, 6), Sym("zeptom")));
        c.put("ZEPTOMETER:FOOT", Mul(Mul(Int(3048), Pow(10, 17)), Sym("zeptom")));
        c.put("ZEPTOMETER:GIGAMETER", Mul(Pow(10, 30), Sym("zeptom")));
        c.put("ZEPTOMETER:HECTOMETER", Mul(Pow(10, 23), Sym("zeptom")));
        c.put("ZEPTOMETER:INCH", Mul(Mul(Int(254), Pow(10, 17)), Sym("zeptom")));
        c.put("ZEPTOMETER:KILOMETER", Mul(Pow(10, 24), Sym("zeptom")));
        c.put("ZEPTOMETER:LIGHTYEAR", Mul(Mul(Int("94607304725808"), Pow(10, 23)), Sym("zeptom")));
        c.put("ZEPTOMETER:LINE", Mul(Rat(Mul(Int(635), Pow(10, 16)), Int(3)), Sym("zeptom")));
        c.put("ZEPTOMETER:MEGAMETER", Mul(Pow(10, 27), Sym("zeptom")));
        c.put("ZEPTOMETER:METER", Mul(Pow(10, 21), Sym("zeptom")));
        c.put("ZEPTOMETER:MICROMETER", Mul(Pow(10, 15), Sym("zeptom")));
        c.put("ZEPTOMETER:MILE", Mul(Mul(Int(1609344), Pow(10, 18)), Sym("zeptom")));
        c.put("ZEPTOMETER:MILLIMETER", Mul(Pow(10, 18), Sym("zeptom")));
        c.put("ZEPTOMETER:NANOMETER", Mul(Pow(10, 12), Sym("zeptom")));
        c.put("ZEPTOMETER:PARSEC", Mul(Mul(Int(30856776), Pow(10, 30)), Sym("zeptom")));
        c.put("ZEPTOMETER:PETAMETER", Mul(Pow(10, 36), Sym("zeptom")));
        c.put("ZEPTOMETER:PICOMETER", Mul(Pow(10, 9), Sym("zeptom")));
        c.put("ZEPTOMETER:POINT", Mul(Rat(Mul(Int(3175), Pow(10, 15)), Int(9)), Sym("zeptom")));
        c.put("ZEPTOMETER:TERAMETER", Mul(Pow(10, 33), Sym("zeptom")));
        c.put("ZEPTOMETER:THOU", Mul(Mul(Int(254), Pow(10, 14)), Sym("zeptom")));
        c.put("ZEPTOMETER:YARD", Mul(Mul(Int(9144), Pow(10, 17)), Sym("zeptom")));
        c.put("ZEPTOMETER:YOCTOMETER", Mul(Rat(Int(1), Int(1000)), Sym("zeptom")));
        c.put("ZEPTOMETER:YOTTAMETER", Mul(Pow(10, 45), Sym("zeptom")));
        c.put("ZEPTOMETER:ZETTAMETER", Mul(Pow(10, 42), Sym("zeptom")));
        c.put("ZETTAMETER:ANGSTROM", Mul(Rat(Int(1), Pow(10, 31)), Sym("zettam")));
        c.put("ZETTAMETER:ASTRONMICALUNIT", Mul(Rat(Int(1495978707), Pow(10, 19)), Sym("zettam")));
        c.put("ZETTAMETER:ATTOMETER", Mul(Rat(Int(1), Pow(10, 39)), Sym("zettam")));
        c.put("ZETTAMETER:CENTIMETER", Mul(Rat(Int(1), Pow(10, 23)), Sym("zettam")));
        c.put("ZETTAMETER:DECAMETER", Mul(Rat(Int(1), Pow(10, 20)), Sym("zettam")));
        c.put("ZETTAMETER:DECIMETER", Mul(Rat(Int(1), Pow(10, 22)), Sym("zettam")));
        c.put("ZETTAMETER:EXAMETER", Mul(Rat(Int(1), Int(1000)), Sym("zettam")));
        c.put("ZETTAMETER:FEMTOMETER", Mul(Rat(Int(1), Pow(10, 36)), Sym("zettam")));
        c.put("ZETTAMETER:FOOT", Mul(Rat(Int(381), Mul(Int(125), Pow(10, 22))), Sym("zettam")));
        c.put("ZETTAMETER:GIGAMETER", Mul(Rat(Int(1), Pow(10, 12)), Sym("zettam")));
        c.put("ZETTAMETER:HECTOMETER", Mul(Rat(Int(1), Pow(10, 19)), Sym("zettam")));
        c.put("ZETTAMETER:INCH", Mul(Rat(Int(127), Mul(Int(5), Pow(10, 24))), Sym("zettam")));
        c.put("ZETTAMETER:KILOMETER", Mul(Rat(Int(1), Pow(10, 18)), Sym("zettam")));
        c.put("ZETTAMETER:LIGHTYEAR", Mul(Rat(Int("5912956545363"), Mul(Int(625), Pow(10, 15))), Sym("zettam")));
        c.put("ZETTAMETER:LINE", Mul(Rat(Int(127), Mul(Int(6), Pow(10, 25))), Sym("zettam")));
        c.put("ZETTAMETER:MEGAMETER", Mul(Rat(Int(1), Pow(10, 15)), Sym("zettam")));
        c.put("ZETTAMETER:METER", Mul(Rat(Int(1), Pow(10, 21)), Sym("zettam")));
        c.put("ZETTAMETER:MICROMETER", Mul(Rat(Int(1), Pow(10, 27)), Sym("zettam")));
        c.put("ZETTAMETER:MILE", Mul(Rat(Int(12573), Mul(Int(78125), Pow(10, 17))), Sym("zettam")));
        c.put("ZETTAMETER:MILLIMETER", Mul(Rat(Int(1), Pow(10, 24)), Sym("zettam")));
        c.put("ZETTAMETER:NANOMETER", Mul(Rat(Int(1), Pow(10, 30)), Sym("zettam")));
        c.put("ZETTAMETER:PARSEC", Mul(Rat(Int(3857097), Mul(Int(125), Pow(10, 9))), Sym("zettam")));
        c.put("ZETTAMETER:PETAMETER", Mul(Rat(Int(1), Pow(10, 6)), Sym("zettam")));
        c.put("ZETTAMETER:PICOMETER", Mul(Rat(Int(1), Pow(10, 33)), Sym("zettam")));
        c.put("ZETTAMETER:POINT", Mul(Rat(Int(127), Mul(Int(36), Pow(10, 25))), Sym("zettam")));
        c.put("ZETTAMETER:TERAMETER", Mul(Rat(Int(1), Pow(10, 9)), Sym("zettam")));
        c.put("ZETTAMETER:THOU", Mul(Rat(Int(127), Mul(Int(5), Pow(10, 27))), Sym("zettam")));
        c.put("ZETTAMETER:YARD", Mul(Rat(Int(1143), Mul(Int(125), Pow(10, 22))), Sym("zettam")));
        c.put("ZETTAMETER:YOCTOMETER", Mul(Rat(Int(1), Pow(10, 45)), Sym("zettam")));
        c.put("ZETTAMETER:YOTTAMETER", Mul(Int(1000), Sym("zettam")));
        c.put("ZETTAMETER:ZEPTOMETER", Mul(Rat(Int(1), Pow(10, 42)), Sym("zettam")));
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
            setUnit(UnitsLength.valueOf(target));
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

