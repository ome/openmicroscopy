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

import omero.model.enums.UnitsPressure;

/**
 * Blitz wrapper around the {@link ome.model.units.Pressure} class.
 * Like {@link Details} and {@link Permissions}, this object
 * is embedded into other objects and does not have a full life
 * cycle of its own.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 */
public class PressureI extends Pressure implements ModelBased {

    private static final long serialVersionUID = 1L;

    private static Map<UnitsPressure, Conversion> createMapATMOSPHERE() {
        EnumMap<UnitsPressure, Conversion> c =
            new EnumMap<UnitsPressure, Conversion>(UnitsPressure.class);
        c.put(UnitsPressure.ATTOPASCAL, Mul(Mul(Int(101325), Pow(10, 18)), Sym("atm")));
        c.put(UnitsPressure.BAR, Mul(Rat(Int(4053), Int(4000)), Sym("atm")));
        c.put(UnitsPressure.CENTIBAR, Mul(Rat(Int(4053), Int(40)), Sym("atm")));
        c.put(UnitsPressure.CENTIPASCAL, Mul(Int(10132500), Sym("atm")));
        c.put(UnitsPressure.DECAPASCAL, Mul(Rat(Int(20265), Int(2)), Sym("atm")));
        c.put(UnitsPressure.DECIBAR, Mul(Rat(Int(4053), Int(400)), Sym("atm")));
        c.put(UnitsPressure.DECIPASCAL, Mul(Int(1013250), Sym("atm")));
        c.put(UnitsPressure.EXAPASCAL, Mul(Rat(Int(4053), Mul(Int(4), Pow(10, 16))), Sym("atm")));
        c.put(UnitsPressure.FEMTOPASCAL, Mul(Mul(Int(101325), Pow(10, 15)), Sym("atm")));
        c.put(UnitsPressure.GIGAPASCAL, Mul(Rat(Int(4053), Mul(Int(4), Pow(10, 7))), Sym("atm")));
        c.put(UnitsPressure.HECTOPASCAL, Mul(Rat(Int(4053), Int(4)), Sym("atm")));
        c.put(UnitsPressure.KILOBAR, Mul(Rat(Int(4053), Mul(Int(4), Pow(10, 6))), Sym("atm")));
        c.put(UnitsPressure.KILOPASCAL, Mul(Rat(Int(4053), Int(40)), Sym("atm")));
        c.put(UnitsPressure.MEGABAR, Mul(Rat(Int(4053), Mul(Int(4), Pow(10, 9))), Sym("atm")));
        c.put(UnitsPressure.MEGAPASCAL, Mul(Rat(Int(4053), Mul(Int(4), Pow(10, 4))), Sym("atm")));
        c.put(UnitsPressure.MICROPASCAL, Mul(Mul(Int(101325), Pow(10, 6)), Sym("atm")));
        c.put(UnitsPressure.MILLIBAR, Mul(Rat(Int(4053), Int(4)), Sym("atm")));
        c.put(UnitsPressure.MILLIPASCAL, Mul(Int(101325000), Sym("atm")));
        c.put(UnitsPressure.MILLITORR, Mul(Rat(Int(19), Int(25)), Sym("atm")));
        c.put(UnitsPressure.MMHG, Mul(Rat(Mul(Int(965), Pow(10, 9)), Int(1269737023)), Sym("atm")));
        c.put(UnitsPressure.NANOPASCAL, Mul(Mul(Int(101325), Pow(10, 9)), Sym("atm")));
        c.put(UnitsPressure.PETAPASCAL, Mul(Rat(Int(4053), Mul(Int(4), Pow(10, 13))), Sym("atm")));
        c.put(UnitsPressure.PICOPASCAL, Mul(Mul(Int(101325), Pow(10, 12)), Sym("atm")));
        c.put(UnitsPressure.PSI, Mul(Rat(Mul(Int(120625), Pow(10, 9)), Int("8208044396629")), Sym("atm")));
        c.put(UnitsPressure.Pascal, Mul(Int(101325), Sym("atm")));
        c.put(UnitsPressure.TERAPASCAL, Mul(Rat(Int(4053), Mul(Int(4), Pow(10, 10))), Sym("atm")));
        c.put(UnitsPressure.TORR, Mul(Int(760), Sym("atm")));
        c.put(UnitsPressure.YOCTOPASCAL, Mul(Mul(Int(101325), Pow(10, 24)), Sym("atm")));
        c.put(UnitsPressure.YOTTAPASCAL, Mul(Rat(Int(4053), Mul(Int(4), Pow(10, 22))), Sym("atm")));
        c.put(UnitsPressure.ZEPTOPASCAL, Mul(Mul(Int(101325), Pow(10, 21)), Sym("atm")));
        c.put(UnitsPressure.ZETTAPASCAL, Mul(Rat(Int(4053), Mul(Int(4), Pow(10, 19))), Sym("atm")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsPressure, Conversion> createMapATTOPASCAL() {
        EnumMap<UnitsPressure, Conversion> c =
            new EnumMap<UnitsPressure, Conversion>(UnitsPressure.class);
        c.put(UnitsPressure.ATMOSPHERE, Mul(Rat(Int(1), Mul(Int(101325), Pow(10, 18))), Sym("attopa")));
        c.put(UnitsPressure.BAR, Mul(Rat(Int(1), Pow(10, 23)), Sym("attopa")));
        c.put(UnitsPressure.CENTIBAR, Mul(Rat(Int(1), Pow(10, 21)), Sym("attopa")));
        c.put(UnitsPressure.CENTIPASCAL, Mul(Rat(Int(1), Pow(10, 16)), Sym("attopa")));
        c.put(UnitsPressure.DECAPASCAL, Mul(Rat(Int(1), Pow(10, 19)), Sym("attopa")));
        c.put(UnitsPressure.DECIBAR, Mul(Rat(Int(1), Pow(10, 22)), Sym("attopa")));
        c.put(UnitsPressure.DECIPASCAL, Mul(Rat(Int(1), Pow(10, 17)), Sym("attopa")));
        c.put(UnitsPressure.EXAPASCAL, Mul(Rat(Int(1), Pow(10, 36)), Sym("attopa")));
        c.put(UnitsPressure.FEMTOPASCAL, Mul(Rat(Int(1), Int(1000)), Sym("attopa")));
        c.put(UnitsPressure.GIGAPASCAL, Mul(Rat(Int(1), Pow(10, 27)), Sym("attopa")));
        c.put(UnitsPressure.HECTOPASCAL, Mul(Rat(Int(1), Pow(10, 20)), Sym("attopa")));
        c.put(UnitsPressure.KILOBAR, Mul(Rat(Int(1), Pow(10, 26)), Sym("attopa")));
        c.put(UnitsPressure.KILOPASCAL, Mul(Rat(Int(1), Pow(10, 21)), Sym("attopa")));
        c.put(UnitsPressure.MEGABAR, Mul(Rat(Int(1), Pow(10, 29)), Sym("attopa")));
        c.put(UnitsPressure.MEGAPASCAL, Mul(Rat(Int(1), Pow(10, 24)), Sym("attopa")));
        c.put(UnitsPressure.MICROPASCAL, Mul(Rat(Int(1), Pow(10, 12)), Sym("attopa")));
        c.put(UnitsPressure.MILLIBAR, Mul(Rat(Int(1), Pow(10, 20)), Sym("attopa")));
        c.put(UnitsPressure.MILLIPASCAL, Mul(Rat(Int(1), Pow(10, 15)), Sym("attopa")));
        c.put(UnitsPressure.MILLITORR, Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 18))), Sym("attopa")));
        c.put(UnitsPressure.MMHG, Mul(Rat(Int(1), Mul(Int("133322387415"), Pow(10, 9))), Sym("attopa")));
        c.put(UnitsPressure.NANOPASCAL, Mul(Rat(Int(1), Pow(10, 9)), Sym("attopa")));
        c.put(UnitsPressure.PETAPASCAL, Mul(Rat(Int(1), Pow(10, 33)), Sym("attopa")));
        c.put(UnitsPressure.PICOPASCAL, Mul(Rat(Int(1), Pow(10, 6)), Sym("attopa")));
        c.put(UnitsPressure.PSI, Mul(Rat(Int(1), Mul(Int("689475729316836"), Pow(10, 7))), Sym("attopa")));
        c.put(UnitsPressure.Pascal, Mul(Rat(Int(1), Pow(10, 18)), Sym("attopa")));
        c.put(UnitsPressure.TERAPASCAL, Mul(Rat(Int(1), Pow(10, 30)), Sym("attopa")));
        c.put(UnitsPressure.TORR, Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 15))), Sym("attopa")));
        c.put(UnitsPressure.YOCTOPASCAL, Mul(Pow(10, 6), Sym("attopa")));
        c.put(UnitsPressure.YOTTAPASCAL, Mul(Rat(Int(1), Pow(10, 42)), Sym("attopa")));
        c.put(UnitsPressure.ZEPTOPASCAL, Mul(Int(1000), Sym("attopa")));
        c.put(UnitsPressure.ZETTAPASCAL, Mul(Rat(Int(1), Pow(10, 39)), Sym("attopa")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsPressure, Conversion> createMapBAR() {
        EnumMap<UnitsPressure, Conversion> c =
            new EnumMap<UnitsPressure, Conversion>(UnitsPressure.class);
        c.put(UnitsPressure.ATMOSPHERE, Mul(Rat(Int(4000), Int(4053)), Sym("bar")));
        c.put(UnitsPressure.ATTOPASCAL, Mul(Pow(10, 23), Sym("bar")));
        c.put(UnitsPressure.CENTIBAR, Mul(Int(100), Sym("bar")));
        c.put(UnitsPressure.CENTIPASCAL, Mul(Pow(10, 7), Sym("bar")));
        c.put(UnitsPressure.DECAPASCAL, Mul(Pow(10, 4), Sym("bar")));
        c.put(UnitsPressure.DECIBAR, Mul(Int(10), Sym("bar")));
        c.put(UnitsPressure.DECIPASCAL, Mul(Pow(10, 6), Sym("bar")));
        c.put(UnitsPressure.EXAPASCAL, Mul(Rat(Int(1), Pow(10, 13)), Sym("bar")));
        c.put(UnitsPressure.FEMTOPASCAL, Mul(Pow(10, 20), Sym("bar")));
        c.put(UnitsPressure.GIGAPASCAL, Mul(Rat(Int(1), Pow(10, 4)), Sym("bar")));
        c.put(UnitsPressure.HECTOPASCAL, Mul(Int(1000), Sym("bar")));
        c.put(UnitsPressure.KILOBAR, Mul(Rat(Int(1), Int(1000)), Sym("bar")));
        c.put(UnitsPressure.KILOPASCAL, Mul(Int(100), Sym("bar")));
        c.put(UnitsPressure.MEGABAR, Mul(Rat(Int(1), Pow(10, 6)), Sym("bar")));
        c.put(UnitsPressure.MEGAPASCAL, Mul(Rat(Int(1), Int(10)), Sym("bar")));
        c.put(UnitsPressure.MICROPASCAL, Mul(Pow(10, 11), Sym("bar")));
        c.put(UnitsPressure.MILLIBAR, Mul(Int(1000), Sym("bar")));
        c.put(UnitsPressure.MILLIPASCAL, Mul(Pow(10, 8), Sym("bar")));
        c.put(UnitsPressure.MILLITORR, Mul(Rat(Int(3040), Int(4053)), Sym("bar")));
        c.put(UnitsPressure.MMHG, Mul(Rat(Mul(Int(2), Pow(10, 13)), Int("26664477483")), Sym("bar")));
        c.put(UnitsPressure.NANOPASCAL, Mul(Pow(10, 14), Sym("bar")));
        c.put(UnitsPressure.PETAPASCAL, Mul(Rat(Int(1), Pow(10, 10)), Sym("bar")));
        c.put(UnitsPressure.PICOPASCAL, Mul(Pow(10, 17), Sym("bar")));
        c.put(UnitsPressure.PSI, Mul(Rat(Mul(Int(25), Pow(10, 14)), Int("172368932329209")), Sym("bar")));
        c.put(UnitsPressure.Pascal, Mul(Pow(10, 5), Sym("bar")));
        c.put(UnitsPressure.TERAPASCAL, Mul(Rat(Int(1), Pow(10, 7)), Sym("bar")));
        c.put(UnitsPressure.TORR, Mul(Rat(Mul(Int(304), Pow(10, 4)), Int(4053)), Sym("bar")));
        c.put(UnitsPressure.YOCTOPASCAL, Mul(Pow(10, 29), Sym("bar")));
        c.put(UnitsPressure.YOTTAPASCAL, Mul(Rat(Int(1), Pow(10, 19)), Sym("bar")));
        c.put(UnitsPressure.ZEPTOPASCAL, Mul(Pow(10, 26), Sym("bar")));
        c.put(UnitsPressure.ZETTAPASCAL, Mul(Rat(Int(1), Pow(10, 16)), Sym("bar")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsPressure, Conversion> createMapCENTIBAR() {
        EnumMap<UnitsPressure, Conversion> c =
            new EnumMap<UnitsPressure, Conversion>(UnitsPressure.class);
        c.put(UnitsPressure.ATMOSPHERE, Mul(Rat(Int(40), Int(4053)), Sym("cbar")));
        c.put(UnitsPressure.ATTOPASCAL, Mul(Pow(10, 21), Sym("cbar")));
        c.put(UnitsPressure.BAR, Mul(Rat(Int(1), Int(100)), Sym("cbar")));
        c.put(UnitsPressure.CENTIPASCAL, Mul(Pow(10, 5), Sym("cbar")));
        c.put(UnitsPressure.DECAPASCAL, Mul(Int(100), Sym("cbar")));
        c.put(UnitsPressure.DECIBAR, Mul(Rat(Int(1), Int(10)), Sym("cbar")));
        c.put(UnitsPressure.DECIPASCAL, Mul(Pow(10, 4), Sym("cbar")));
        c.put(UnitsPressure.EXAPASCAL, Mul(Rat(Int(1), Pow(10, 15)), Sym("cbar")));
        c.put(UnitsPressure.FEMTOPASCAL, Mul(Pow(10, 18), Sym("cbar")));
        c.put(UnitsPressure.GIGAPASCAL, Mul(Rat(Int(1), Pow(10, 6)), Sym("cbar")));
        c.put(UnitsPressure.HECTOPASCAL, Mul(Int(10), Sym("cbar")));
        c.put(UnitsPressure.KILOBAR, Mul(Rat(Int(1), Pow(10, 5)), Sym("cbar")));
        c.put(UnitsPressure.KILOPASCAL, Sym("cbar"));
        c.put(UnitsPressure.MEGABAR, Mul(Rat(Int(1), Pow(10, 8)), Sym("cbar")));
        c.put(UnitsPressure.MEGAPASCAL, Mul(Rat(Int(1), Int(1000)), Sym("cbar")));
        c.put(UnitsPressure.MICROPASCAL, Mul(Pow(10, 9), Sym("cbar")));
        c.put(UnitsPressure.MILLIBAR, Mul(Int(10), Sym("cbar")));
        c.put(UnitsPressure.MILLIPASCAL, Mul(Pow(10, 6), Sym("cbar")));
        c.put(UnitsPressure.MILLITORR, Mul(Rat(Int(152), Int(20265)), Sym("cbar")));
        c.put(UnitsPressure.MMHG, Mul(Rat(Mul(Int(2), Pow(10, 11)), Int("26664477483")), Sym("cbar")));
        c.put(UnitsPressure.NANOPASCAL, Mul(Pow(10, 12), Sym("cbar")));
        c.put(UnitsPressure.PETAPASCAL, Mul(Rat(Int(1), Pow(10, 12)), Sym("cbar")));
        c.put(UnitsPressure.PICOPASCAL, Mul(Pow(10, 15), Sym("cbar")));
        c.put(UnitsPressure.PSI, Mul(Rat(Mul(Int(25), Pow(10, 12)), Int("172368932329209")), Sym("cbar")));
        c.put(UnitsPressure.Pascal, Mul(Int(1000), Sym("cbar")));
        c.put(UnitsPressure.TERAPASCAL, Mul(Rat(Int(1), Pow(10, 9)), Sym("cbar")));
        c.put(UnitsPressure.TORR, Mul(Rat(Int(30400), Int(4053)), Sym("cbar")));
        c.put(UnitsPressure.YOCTOPASCAL, Mul(Pow(10, 27), Sym("cbar")));
        c.put(UnitsPressure.YOTTAPASCAL, Mul(Rat(Int(1), Pow(10, 21)), Sym("cbar")));
        c.put(UnitsPressure.ZEPTOPASCAL, Mul(Pow(10, 24), Sym("cbar")));
        c.put(UnitsPressure.ZETTAPASCAL, Mul(Rat(Int(1), Pow(10, 18)), Sym("cbar")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsPressure, Conversion> createMapCENTIPASCAL() {
        EnumMap<UnitsPressure, Conversion> c =
            new EnumMap<UnitsPressure, Conversion>(UnitsPressure.class);
        c.put(UnitsPressure.ATMOSPHERE, Mul(Rat(Int(1), Int(10132500)), Sym("centipa")));
        c.put(UnitsPressure.ATTOPASCAL, Mul(Pow(10, 16), Sym("centipa")));
        c.put(UnitsPressure.BAR, Mul(Rat(Int(1), Pow(10, 7)), Sym("centipa")));
        c.put(UnitsPressure.CENTIBAR, Mul(Rat(Int(1), Pow(10, 5)), Sym("centipa")));
        c.put(UnitsPressure.DECAPASCAL, Mul(Rat(Int(1), Int(1000)), Sym("centipa")));
        c.put(UnitsPressure.DECIBAR, Mul(Rat(Int(1), Pow(10, 6)), Sym("centipa")));
        c.put(UnitsPressure.DECIPASCAL, Mul(Rat(Int(1), Int(10)), Sym("centipa")));
        c.put(UnitsPressure.EXAPASCAL, Mul(Rat(Int(1), Pow(10, 20)), Sym("centipa")));
        c.put(UnitsPressure.FEMTOPASCAL, Mul(Pow(10, 13), Sym("centipa")));
        c.put(UnitsPressure.GIGAPASCAL, Mul(Rat(Int(1), Pow(10, 11)), Sym("centipa")));
        c.put(UnitsPressure.HECTOPASCAL, Mul(Rat(Int(1), Pow(10, 4)), Sym("centipa")));
        c.put(UnitsPressure.KILOBAR, Mul(Rat(Int(1), Pow(10, 10)), Sym("centipa")));
        c.put(UnitsPressure.KILOPASCAL, Mul(Rat(Int(1), Pow(10, 5)), Sym("centipa")));
        c.put(UnitsPressure.MEGABAR, Mul(Rat(Int(1), Pow(10, 13)), Sym("centipa")));
        c.put(UnitsPressure.MEGAPASCAL, Mul(Rat(Int(1), Pow(10, 8)), Sym("centipa")));
        c.put(UnitsPressure.MICROPASCAL, Mul(Pow(10, 4), Sym("centipa")));
        c.put(UnitsPressure.MILLIBAR, Mul(Rat(Int(1), Pow(10, 4)), Sym("centipa")));
        c.put(UnitsPressure.MILLIPASCAL, Mul(Int(10), Sym("centipa")));
        c.put(UnitsPressure.MILLITORR, Mul(Rat(Int(19), Int(253312500)), Sym("centipa")));
        c.put(UnitsPressure.MMHG, Mul(Rat(Mul(Int(2), Pow(10, 6)), Int("26664477483")), Sym("centipa")));
        c.put(UnitsPressure.NANOPASCAL, Mul(Pow(10, 7), Sym("centipa")));
        c.put(UnitsPressure.PETAPASCAL, Mul(Rat(Int(1), Pow(10, 17)), Sym("centipa")));
        c.put(UnitsPressure.PICOPASCAL, Mul(Pow(10, 10), Sym("centipa")));
        c.put(UnitsPressure.PSI, Mul(Rat(Mul(Int(25), Pow(10, 7)), Int("172368932329209")), Sym("centipa")));
        c.put(UnitsPressure.Pascal, Mul(Rat(Int(1), Int(100)), Sym("centipa")));
        c.put(UnitsPressure.TERAPASCAL, Mul(Rat(Int(1), Pow(10, 14)), Sym("centipa")));
        c.put(UnitsPressure.TORR, Mul(Rat(Int(38), Int(506625)), Sym("centipa")));
        c.put(UnitsPressure.YOCTOPASCAL, Mul(Pow(10, 22), Sym("centipa")));
        c.put(UnitsPressure.YOTTAPASCAL, Mul(Rat(Int(1), Pow(10, 26)), Sym("centipa")));
        c.put(UnitsPressure.ZEPTOPASCAL, Mul(Pow(10, 19), Sym("centipa")));
        c.put(UnitsPressure.ZETTAPASCAL, Mul(Rat(Int(1), Pow(10, 23)), Sym("centipa")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsPressure, Conversion> createMapDECAPASCAL() {
        EnumMap<UnitsPressure, Conversion> c =
            new EnumMap<UnitsPressure, Conversion>(UnitsPressure.class);
        c.put(UnitsPressure.ATMOSPHERE, Mul(Rat(Int(2), Int(20265)), Sym("decapa")));
        c.put(UnitsPressure.ATTOPASCAL, Mul(Pow(10, 19), Sym("decapa")));
        c.put(UnitsPressure.BAR, Mul(Rat(Int(1), Pow(10, 4)), Sym("decapa")));
        c.put(UnitsPressure.CENTIBAR, Mul(Rat(Int(1), Int(100)), Sym("decapa")));
        c.put(UnitsPressure.CENTIPASCAL, Mul(Int(1000), Sym("decapa")));
        c.put(UnitsPressure.DECIBAR, Mul(Rat(Int(1), Int(1000)), Sym("decapa")));
        c.put(UnitsPressure.DECIPASCAL, Mul(Int(100), Sym("decapa")));
        c.put(UnitsPressure.EXAPASCAL, Mul(Rat(Int(1), Pow(10, 17)), Sym("decapa")));
        c.put(UnitsPressure.FEMTOPASCAL, Mul(Pow(10, 16), Sym("decapa")));
        c.put(UnitsPressure.GIGAPASCAL, Mul(Rat(Int(1), Pow(10, 8)), Sym("decapa")));
        c.put(UnitsPressure.HECTOPASCAL, Mul(Rat(Int(1), Int(10)), Sym("decapa")));
        c.put(UnitsPressure.KILOBAR, Mul(Rat(Int(1), Pow(10, 7)), Sym("decapa")));
        c.put(UnitsPressure.KILOPASCAL, Mul(Rat(Int(1), Int(100)), Sym("decapa")));
        c.put(UnitsPressure.MEGABAR, Mul(Rat(Int(1), Pow(10, 10)), Sym("decapa")));
        c.put(UnitsPressure.MEGAPASCAL, Mul(Rat(Int(1), Pow(10, 5)), Sym("decapa")));
        c.put(UnitsPressure.MICROPASCAL, Mul(Pow(10, 7), Sym("decapa")));
        c.put(UnitsPressure.MILLIBAR, Mul(Rat(Int(1), Int(10)), Sym("decapa")));
        c.put(UnitsPressure.MILLIPASCAL, Mul(Pow(10, 4), Sym("decapa")));
        c.put(UnitsPressure.MILLITORR, Mul(Rat(Int(38), Int(506625)), Sym("decapa")));
        c.put(UnitsPressure.MMHG, Mul(Rat(Mul(Int(2), Pow(10, 9)), Int("26664477483")), Sym("decapa")));
        c.put(UnitsPressure.NANOPASCAL, Mul(Pow(10, 10), Sym("decapa")));
        c.put(UnitsPressure.PETAPASCAL, Mul(Rat(Int(1), Pow(10, 14)), Sym("decapa")));
        c.put(UnitsPressure.PICOPASCAL, Mul(Pow(10, 13), Sym("decapa")));
        c.put(UnitsPressure.PSI, Mul(Rat(Mul(Int(25), Pow(10, 10)), Int("172368932329209")), Sym("decapa")));
        c.put(UnitsPressure.Pascal, Mul(Int(10), Sym("decapa")));
        c.put(UnitsPressure.TERAPASCAL, Mul(Rat(Int(1), Pow(10, 11)), Sym("decapa")));
        c.put(UnitsPressure.TORR, Mul(Rat(Int(304), Int(4053)), Sym("decapa")));
        c.put(UnitsPressure.YOCTOPASCAL, Mul(Pow(10, 25), Sym("decapa")));
        c.put(UnitsPressure.YOTTAPASCAL, Mul(Rat(Int(1), Pow(10, 23)), Sym("decapa")));
        c.put(UnitsPressure.ZEPTOPASCAL, Mul(Pow(10, 22), Sym("decapa")));
        c.put(UnitsPressure.ZETTAPASCAL, Mul(Rat(Int(1), Pow(10, 20)), Sym("decapa")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsPressure, Conversion> createMapDECIBAR() {
        EnumMap<UnitsPressure, Conversion> c =
            new EnumMap<UnitsPressure, Conversion>(UnitsPressure.class);
        c.put(UnitsPressure.ATMOSPHERE, Mul(Rat(Int(400), Int(4053)), Sym("dbar")));
        c.put(UnitsPressure.ATTOPASCAL, Mul(Pow(10, 22), Sym("dbar")));
        c.put(UnitsPressure.BAR, Mul(Rat(Int(1), Int(10)), Sym("dbar")));
        c.put(UnitsPressure.CENTIBAR, Mul(Int(10), Sym("dbar")));
        c.put(UnitsPressure.CENTIPASCAL, Mul(Pow(10, 6), Sym("dbar")));
        c.put(UnitsPressure.DECAPASCAL, Mul(Int(1000), Sym("dbar")));
        c.put(UnitsPressure.DECIPASCAL, Mul(Pow(10, 5), Sym("dbar")));
        c.put(UnitsPressure.EXAPASCAL, Mul(Rat(Int(1), Pow(10, 14)), Sym("dbar")));
        c.put(UnitsPressure.FEMTOPASCAL, Mul(Pow(10, 19), Sym("dbar")));
        c.put(UnitsPressure.GIGAPASCAL, Mul(Rat(Int(1), Pow(10, 5)), Sym("dbar")));
        c.put(UnitsPressure.HECTOPASCAL, Mul(Int(100), Sym("dbar")));
        c.put(UnitsPressure.KILOBAR, Mul(Rat(Int(1), Pow(10, 4)), Sym("dbar")));
        c.put(UnitsPressure.KILOPASCAL, Mul(Int(10), Sym("dbar")));
        c.put(UnitsPressure.MEGABAR, Mul(Rat(Int(1), Pow(10, 7)), Sym("dbar")));
        c.put(UnitsPressure.MEGAPASCAL, Mul(Rat(Int(1), Int(100)), Sym("dbar")));
        c.put(UnitsPressure.MICROPASCAL, Mul(Pow(10, 10), Sym("dbar")));
        c.put(UnitsPressure.MILLIBAR, Mul(Int(100), Sym("dbar")));
        c.put(UnitsPressure.MILLIPASCAL, Mul(Pow(10, 7), Sym("dbar")));
        c.put(UnitsPressure.MILLITORR, Mul(Rat(Int(304), Int(4053)), Sym("dbar")));
        c.put(UnitsPressure.MMHG, Mul(Rat(Mul(Int(2), Pow(10, 12)), Int("26664477483")), Sym("dbar")));
        c.put(UnitsPressure.NANOPASCAL, Mul(Pow(10, 13), Sym("dbar")));
        c.put(UnitsPressure.PETAPASCAL, Mul(Rat(Int(1), Pow(10, 11)), Sym("dbar")));
        c.put(UnitsPressure.PICOPASCAL, Mul(Pow(10, 16), Sym("dbar")));
        c.put(UnitsPressure.PSI, Mul(Rat(Mul(Int(25), Pow(10, 13)), Int("172368932329209")), Sym("dbar")));
        c.put(UnitsPressure.Pascal, Mul(Pow(10, 4), Sym("dbar")));
        c.put(UnitsPressure.TERAPASCAL, Mul(Rat(Int(1), Pow(10, 8)), Sym("dbar")));
        c.put(UnitsPressure.TORR, Mul(Rat(Int(304000), Int(4053)), Sym("dbar")));
        c.put(UnitsPressure.YOCTOPASCAL, Mul(Pow(10, 28), Sym("dbar")));
        c.put(UnitsPressure.YOTTAPASCAL, Mul(Rat(Int(1), Pow(10, 20)), Sym("dbar")));
        c.put(UnitsPressure.ZEPTOPASCAL, Mul(Pow(10, 25), Sym("dbar")));
        c.put(UnitsPressure.ZETTAPASCAL, Mul(Rat(Int(1), Pow(10, 17)), Sym("dbar")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsPressure, Conversion> createMapDECIPASCAL() {
        EnumMap<UnitsPressure, Conversion> c =
            new EnumMap<UnitsPressure, Conversion>(UnitsPressure.class);
        c.put(UnitsPressure.ATMOSPHERE, Mul(Rat(Int(1), Int(1013250)), Sym("decipa")));
        c.put(UnitsPressure.ATTOPASCAL, Mul(Pow(10, 17), Sym("decipa")));
        c.put(UnitsPressure.BAR, Mul(Rat(Int(1), Pow(10, 6)), Sym("decipa")));
        c.put(UnitsPressure.CENTIBAR, Mul(Rat(Int(1), Pow(10, 4)), Sym("decipa")));
        c.put(UnitsPressure.CENTIPASCAL, Mul(Int(10), Sym("decipa")));
        c.put(UnitsPressure.DECAPASCAL, Mul(Rat(Int(1), Int(100)), Sym("decipa")));
        c.put(UnitsPressure.DECIBAR, Mul(Rat(Int(1), Pow(10, 5)), Sym("decipa")));
        c.put(UnitsPressure.EXAPASCAL, Mul(Rat(Int(1), Pow(10, 19)), Sym("decipa")));
        c.put(UnitsPressure.FEMTOPASCAL, Mul(Pow(10, 14), Sym("decipa")));
        c.put(UnitsPressure.GIGAPASCAL, Mul(Rat(Int(1), Pow(10, 10)), Sym("decipa")));
        c.put(UnitsPressure.HECTOPASCAL, Mul(Rat(Int(1), Int(1000)), Sym("decipa")));
        c.put(UnitsPressure.KILOBAR, Mul(Rat(Int(1), Pow(10, 9)), Sym("decipa")));
        c.put(UnitsPressure.KILOPASCAL, Mul(Rat(Int(1), Pow(10, 4)), Sym("decipa")));
        c.put(UnitsPressure.MEGABAR, Mul(Rat(Int(1), Pow(10, 12)), Sym("decipa")));
        c.put(UnitsPressure.MEGAPASCAL, Mul(Rat(Int(1), Pow(10, 7)), Sym("decipa")));
        c.put(UnitsPressure.MICROPASCAL, Mul(Pow(10, 5), Sym("decipa")));
        c.put(UnitsPressure.MILLIBAR, Mul(Rat(Int(1), Int(1000)), Sym("decipa")));
        c.put(UnitsPressure.MILLIPASCAL, Mul(Int(100), Sym("decipa")));
        c.put(UnitsPressure.MILLITORR, Mul(Rat(Int(19), Int(25331250)), Sym("decipa")));
        c.put(UnitsPressure.MMHG, Mul(Rat(Mul(Int(2), Pow(10, 7)), Int("26664477483")), Sym("decipa")));
        c.put(UnitsPressure.NANOPASCAL, Mul(Pow(10, 8), Sym("decipa")));
        c.put(UnitsPressure.PETAPASCAL, Mul(Rat(Int(1), Pow(10, 16)), Sym("decipa")));
        c.put(UnitsPressure.PICOPASCAL, Mul(Pow(10, 11), Sym("decipa")));
        c.put(UnitsPressure.PSI, Mul(Rat(Mul(Int(25), Pow(10, 8)), Int("172368932329209")), Sym("decipa")));
        c.put(UnitsPressure.Pascal, Mul(Rat(Int(1), Int(10)), Sym("decipa")));
        c.put(UnitsPressure.TERAPASCAL, Mul(Rat(Int(1), Pow(10, 13)), Sym("decipa")));
        c.put(UnitsPressure.TORR, Mul(Rat(Int(76), Int(101325)), Sym("decipa")));
        c.put(UnitsPressure.YOCTOPASCAL, Mul(Pow(10, 23), Sym("decipa")));
        c.put(UnitsPressure.YOTTAPASCAL, Mul(Rat(Int(1), Pow(10, 25)), Sym("decipa")));
        c.put(UnitsPressure.ZEPTOPASCAL, Mul(Pow(10, 20), Sym("decipa")));
        c.put(UnitsPressure.ZETTAPASCAL, Mul(Rat(Int(1), Pow(10, 22)), Sym("decipa")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsPressure, Conversion> createMapEXAPASCAL() {
        EnumMap<UnitsPressure, Conversion> c =
            new EnumMap<UnitsPressure, Conversion>(UnitsPressure.class);
        c.put(UnitsPressure.ATMOSPHERE, Mul(Rat(Mul(Int(4), Pow(10, 16)), Int(4053)), Sym("exapa")));
        c.put(UnitsPressure.ATTOPASCAL, Mul(Pow(10, 36), Sym("exapa")));
        c.put(UnitsPressure.BAR, Mul(Pow(10, 13), Sym("exapa")));
        c.put(UnitsPressure.CENTIBAR, Mul(Pow(10, 15), Sym("exapa")));
        c.put(UnitsPressure.CENTIPASCAL, Mul(Pow(10, 20), Sym("exapa")));
        c.put(UnitsPressure.DECAPASCAL, Mul(Pow(10, 17), Sym("exapa")));
        c.put(UnitsPressure.DECIBAR, Mul(Pow(10, 14), Sym("exapa")));
        c.put(UnitsPressure.DECIPASCAL, Mul(Pow(10, 19), Sym("exapa")));
        c.put(UnitsPressure.FEMTOPASCAL, Mul(Pow(10, 33), Sym("exapa")));
        c.put(UnitsPressure.GIGAPASCAL, Mul(Pow(10, 9), Sym("exapa")));
        c.put(UnitsPressure.HECTOPASCAL, Mul(Pow(10, 16), Sym("exapa")));
        c.put(UnitsPressure.KILOBAR, Mul(Pow(10, 10), Sym("exapa")));
        c.put(UnitsPressure.KILOPASCAL, Mul(Pow(10, 15), Sym("exapa")));
        c.put(UnitsPressure.MEGABAR, Mul(Pow(10, 7), Sym("exapa")));
        c.put(UnitsPressure.MEGAPASCAL, Mul(Pow(10, 12), Sym("exapa")));
        c.put(UnitsPressure.MICROPASCAL, Mul(Pow(10, 24), Sym("exapa")));
        c.put(UnitsPressure.MILLIBAR, Mul(Pow(10, 16), Sym("exapa")));
        c.put(UnitsPressure.MILLIPASCAL, Mul(Pow(10, 21), Sym("exapa")));
        c.put(UnitsPressure.MILLITORR, Mul(Rat(Mul(Int(304), Pow(10, 14)), Int(4053)), Sym("exapa")));
        c.put(UnitsPressure.MMHG, Mul(Rat(Mul(Int(2), Pow(10, 26)), Int("26664477483")), Sym("exapa")));
        c.put(UnitsPressure.NANOPASCAL, Mul(Pow(10, 27), Sym("exapa")));
        c.put(UnitsPressure.PETAPASCAL, Mul(Int(1000), Sym("exapa")));
        c.put(UnitsPressure.PICOPASCAL, Mul(Pow(10, 30), Sym("exapa")));
        c.put(UnitsPressure.PSI, Mul(Rat(Mul(Int(25), Pow(10, 27)), Int("172368932329209")), Sym("exapa")));
        c.put(UnitsPressure.Pascal, Mul(Pow(10, 18), Sym("exapa")));
        c.put(UnitsPressure.TERAPASCAL, Mul(Pow(10, 6), Sym("exapa")));
        c.put(UnitsPressure.TORR, Mul(Rat(Mul(Int(304), Pow(10, 17)), Int(4053)), Sym("exapa")));
        c.put(UnitsPressure.YOCTOPASCAL, Mul(Pow(10, 42), Sym("exapa")));
        c.put(UnitsPressure.YOTTAPASCAL, Mul(Rat(Int(1), Pow(10, 6)), Sym("exapa")));
        c.put(UnitsPressure.ZEPTOPASCAL, Mul(Pow(10, 39), Sym("exapa")));
        c.put(UnitsPressure.ZETTAPASCAL, Mul(Rat(Int(1), Int(1000)), Sym("exapa")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsPressure, Conversion> createMapFEMTOPASCAL() {
        EnumMap<UnitsPressure, Conversion> c =
            new EnumMap<UnitsPressure, Conversion>(UnitsPressure.class);
        c.put(UnitsPressure.ATMOSPHERE, Mul(Rat(Int(1), Mul(Int(101325), Pow(10, 15))), Sym("femtopa")));
        c.put(UnitsPressure.ATTOPASCAL, Mul(Int(1000), Sym("femtopa")));
        c.put(UnitsPressure.BAR, Mul(Rat(Int(1), Pow(10, 20)), Sym("femtopa")));
        c.put(UnitsPressure.CENTIBAR, Mul(Rat(Int(1), Pow(10, 18)), Sym("femtopa")));
        c.put(UnitsPressure.CENTIPASCAL, Mul(Rat(Int(1), Pow(10, 13)), Sym("femtopa")));
        c.put(UnitsPressure.DECAPASCAL, Mul(Rat(Int(1), Pow(10, 16)), Sym("femtopa")));
        c.put(UnitsPressure.DECIBAR, Mul(Rat(Int(1), Pow(10, 19)), Sym("femtopa")));
        c.put(UnitsPressure.DECIPASCAL, Mul(Rat(Int(1), Pow(10, 14)), Sym("femtopa")));
        c.put(UnitsPressure.EXAPASCAL, Mul(Rat(Int(1), Pow(10, 33)), Sym("femtopa")));
        c.put(UnitsPressure.GIGAPASCAL, Mul(Rat(Int(1), Pow(10, 24)), Sym("femtopa")));
        c.put(UnitsPressure.HECTOPASCAL, Mul(Rat(Int(1), Pow(10, 17)), Sym("femtopa")));
        c.put(UnitsPressure.KILOBAR, Mul(Rat(Int(1), Pow(10, 23)), Sym("femtopa")));
        c.put(UnitsPressure.KILOPASCAL, Mul(Rat(Int(1), Pow(10, 18)), Sym("femtopa")));
        c.put(UnitsPressure.MEGABAR, Mul(Rat(Int(1), Pow(10, 26)), Sym("femtopa")));
        c.put(UnitsPressure.MEGAPASCAL, Mul(Rat(Int(1), Pow(10, 21)), Sym("femtopa")));
        c.put(UnitsPressure.MICROPASCAL, Mul(Rat(Int(1), Pow(10, 9)), Sym("femtopa")));
        c.put(UnitsPressure.MILLIBAR, Mul(Rat(Int(1), Pow(10, 17)), Sym("femtopa")));
        c.put(UnitsPressure.MILLIPASCAL, Mul(Rat(Int(1), Pow(10, 12)), Sym("femtopa")));
        c.put(UnitsPressure.MILLITORR, Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 15))), Sym("femtopa")));
        c.put(UnitsPressure.MMHG, Mul(Rat(Int(1), Mul(Int("133322387415"), Pow(10, 6))), Sym("femtopa")));
        c.put(UnitsPressure.NANOPASCAL, Mul(Rat(Int(1), Pow(10, 6)), Sym("femtopa")));
        c.put(UnitsPressure.PETAPASCAL, Mul(Rat(Int(1), Pow(10, 30)), Sym("femtopa")));
        c.put(UnitsPressure.PICOPASCAL, Mul(Rat(Int(1), Int(1000)), Sym("femtopa")));
        c.put(UnitsPressure.PSI, Mul(Rat(Int(1), Mul(Int("689475729316836"), Pow(10, 4))), Sym("femtopa")));
        c.put(UnitsPressure.Pascal, Mul(Rat(Int(1), Pow(10, 15)), Sym("femtopa")));
        c.put(UnitsPressure.TERAPASCAL, Mul(Rat(Int(1), Pow(10, 27)), Sym("femtopa")));
        c.put(UnitsPressure.TORR, Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 12))), Sym("femtopa")));
        c.put(UnitsPressure.YOCTOPASCAL, Mul(Pow(10, 9), Sym("femtopa")));
        c.put(UnitsPressure.YOTTAPASCAL, Mul(Rat(Int(1), Pow(10, 39)), Sym("femtopa")));
        c.put(UnitsPressure.ZEPTOPASCAL, Mul(Pow(10, 6), Sym("femtopa")));
        c.put(UnitsPressure.ZETTAPASCAL, Mul(Rat(Int(1), Pow(10, 36)), Sym("femtopa")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsPressure, Conversion> createMapGIGAPASCAL() {
        EnumMap<UnitsPressure, Conversion> c =
            new EnumMap<UnitsPressure, Conversion>(UnitsPressure.class);
        c.put(UnitsPressure.ATMOSPHERE, Mul(Rat(Mul(Int(4), Pow(10, 7)), Int(4053)), Sym("gigapa")));
        c.put(UnitsPressure.ATTOPASCAL, Mul(Pow(10, 27), Sym("gigapa")));
        c.put(UnitsPressure.BAR, Mul(Pow(10, 4), Sym("gigapa")));
        c.put(UnitsPressure.CENTIBAR, Mul(Pow(10, 6), Sym("gigapa")));
        c.put(UnitsPressure.CENTIPASCAL, Mul(Pow(10, 11), Sym("gigapa")));
        c.put(UnitsPressure.DECAPASCAL, Mul(Pow(10, 8), Sym("gigapa")));
        c.put(UnitsPressure.DECIBAR, Mul(Pow(10, 5), Sym("gigapa")));
        c.put(UnitsPressure.DECIPASCAL, Mul(Pow(10, 10), Sym("gigapa")));
        c.put(UnitsPressure.EXAPASCAL, Mul(Rat(Int(1), Pow(10, 9)), Sym("gigapa")));
        c.put(UnitsPressure.FEMTOPASCAL, Mul(Pow(10, 24), Sym("gigapa")));
        c.put(UnitsPressure.HECTOPASCAL, Mul(Pow(10, 7), Sym("gigapa")));
        c.put(UnitsPressure.KILOBAR, Mul(Int(10), Sym("gigapa")));
        c.put(UnitsPressure.KILOPASCAL, Mul(Pow(10, 6), Sym("gigapa")));
        c.put(UnitsPressure.MEGABAR, Mul(Rat(Int(1), Int(100)), Sym("gigapa")));
        c.put(UnitsPressure.MEGAPASCAL, Mul(Int(1000), Sym("gigapa")));
        c.put(UnitsPressure.MICROPASCAL, Mul(Pow(10, 15), Sym("gigapa")));
        c.put(UnitsPressure.MILLIBAR, Mul(Pow(10, 7), Sym("gigapa")));
        c.put(UnitsPressure.MILLIPASCAL, Mul(Pow(10, 12), Sym("gigapa")));
        c.put(UnitsPressure.MILLITORR, Mul(Rat(Mul(Int(304), Pow(10, 5)), Int(4053)), Sym("gigapa")));
        c.put(UnitsPressure.MMHG, Mul(Rat(Mul(Int(2), Pow(10, 17)), Int("26664477483")), Sym("gigapa")));
        c.put(UnitsPressure.NANOPASCAL, Mul(Pow(10, 18), Sym("gigapa")));
        c.put(UnitsPressure.PETAPASCAL, Mul(Rat(Int(1), Pow(10, 6)), Sym("gigapa")));
        c.put(UnitsPressure.PICOPASCAL, Mul(Pow(10, 21), Sym("gigapa")));
        c.put(UnitsPressure.PSI, Mul(Rat(Mul(Int(25), Pow(10, 18)), Int("172368932329209")), Sym("gigapa")));
        c.put(UnitsPressure.Pascal, Mul(Pow(10, 9), Sym("gigapa")));
        c.put(UnitsPressure.TERAPASCAL, Mul(Rat(Int(1), Int(1000)), Sym("gigapa")));
        c.put(UnitsPressure.TORR, Mul(Rat(Mul(Int(304), Pow(10, 8)), Int(4053)), Sym("gigapa")));
        c.put(UnitsPressure.YOCTOPASCAL, Mul(Pow(10, 33), Sym("gigapa")));
        c.put(UnitsPressure.YOTTAPASCAL, Mul(Rat(Int(1), Pow(10, 15)), Sym("gigapa")));
        c.put(UnitsPressure.ZEPTOPASCAL, Mul(Pow(10, 30), Sym("gigapa")));
        c.put(UnitsPressure.ZETTAPASCAL, Mul(Rat(Int(1), Pow(10, 12)), Sym("gigapa")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsPressure, Conversion> createMapHECTOPASCAL() {
        EnumMap<UnitsPressure, Conversion> c =
            new EnumMap<UnitsPressure, Conversion>(UnitsPressure.class);
        c.put(UnitsPressure.ATMOSPHERE, Mul(Rat(Int(4), Int(4053)), Sym("hectopa")));
        c.put(UnitsPressure.ATTOPASCAL, Mul(Pow(10, 20), Sym("hectopa")));
        c.put(UnitsPressure.BAR, Mul(Rat(Int(1), Int(1000)), Sym("hectopa")));
        c.put(UnitsPressure.CENTIBAR, Mul(Rat(Int(1), Int(10)), Sym("hectopa")));
        c.put(UnitsPressure.CENTIPASCAL, Mul(Pow(10, 4), Sym("hectopa")));
        c.put(UnitsPressure.DECAPASCAL, Mul(Int(10), Sym("hectopa")));
        c.put(UnitsPressure.DECIBAR, Mul(Rat(Int(1), Int(100)), Sym("hectopa")));
        c.put(UnitsPressure.DECIPASCAL, Mul(Int(1000), Sym("hectopa")));
        c.put(UnitsPressure.EXAPASCAL, Mul(Rat(Int(1), Pow(10, 16)), Sym("hectopa")));
        c.put(UnitsPressure.FEMTOPASCAL, Mul(Pow(10, 17), Sym("hectopa")));
        c.put(UnitsPressure.GIGAPASCAL, Mul(Rat(Int(1), Pow(10, 7)), Sym("hectopa")));
        c.put(UnitsPressure.KILOBAR, Mul(Rat(Int(1), Pow(10, 6)), Sym("hectopa")));
        c.put(UnitsPressure.KILOPASCAL, Mul(Rat(Int(1), Int(10)), Sym("hectopa")));
        c.put(UnitsPressure.MEGABAR, Mul(Rat(Int(1), Pow(10, 9)), Sym("hectopa")));
        c.put(UnitsPressure.MEGAPASCAL, Mul(Rat(Int(1), Pow(10, 4)), Sym("hectopa")));
        c.put(UnitsPressure.MICROPASCAL, Mul(Pow(10, 8), Sym("hectopa")));
        c.put(UnitsPressure.MILLIBAR, Sym("hectopa"));
        c.put(UnitsPressure.MILLIPASCAL, Mul(Pow(10, 5), Sym("hectopa")));
        c.put(UnitsPressure.MILLITORR, Mul(Rat(Int(76), Int(101325)), Sym("hectopa")));
        c.put(UnitsPressure.MMHG, Mul(Rat(Mul(Int(2), Pow(10, 10)), Int("26664477483")), Sym("hectopa")));
        c.put(UnitsPressure.NANOPASCAL, Mul(Pow(10, 11), Sym("hectopa")));
        c.put(UnitsPressure.PETAPASCAL, Mul(Rat(Int(1), Pow(10, 13)), Sym("hectopa")));
        c.put(UnitsPressure.PICOPASCAL, Mul(Pow(10, 14), Sym("hectopa")));
        c.put(UnitsPressure.PSI, Mul(Rat(Mul(Int(25), Pow(10, 11)), Int("172368932329209")), Sym("hectopa")));
        c.put(UnitsPressure.Pascal, Mul(Int(100), Sym("hectopa")));
        c.put(UnitsPressure.TERAPASCAL, Mul(Rat(Int(1), Pow(10, 10)), Sym("hectopa")));
        c.put(UnitsPressure.TORR, Mul(Rat(Int(3040), Int(4053)), Sym("hectopa")));
        c.put(UnitsPressure.YOCTOPASCAL, Mul(Pow(10, 26), Sym("hectopa")));
        c.put(UnitsPressure.YOTTAPASCAL, Mul(Rat(Int(1), Pow(10, 22)), Sym("hectopa")));
        c.put(UnitsPressure.ZEPTOPASCAL, Mul(Pow(10, 23), Sym("hectopa")));
        c.put(UnitsPressure.ZETTAPASCAL, Mul(Rat(Int(1), Pow(10, 19)), Sym("hectopa")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsPressure, Conversion> createMapKILOBAR() {
        EnumMap<UnitsPressure, Conversion> c =
            new EnumMap<UnitsPressure, Conversion>(UnitsPressure.class);
        c.put(UnitsPressure.ATMOSPHERE, Mul(Rat(Mul(Int(4), Pow(10, 6)), Int(4053)), Sym("kbar")));
        c.put(UnitsPressure.ATTOPASCAL, Mul(Pow(10, 26), Sym("kbar")));
        c.put(UnitsPressure.BAR, Mul(Int(1000), Sym("kbar")));
        c.put(UnitsPressure.CENTIBAR, Mul(Pow(10, 5), Sym("kbar")));
        c.put(UnitsPressure.CENTIPASCAL, Mul(Pow(10, 10), Sym("kbar")));
        c.put(UnitsPressure.DECAPASCAL, Mul(Pow(10, 7), Sym("kbar")));
        c.put(UnitsPressure.DECIBAR, Mul(Pow(10, 4), Sym("kbar")));
        c.put(UnitsPressure.DECIPASCAL, Mul(Pow(10, 9), Sym("kbar")));
        c.put(UnitsPressure.EXAPASCAL, Mul(Rat(Int(1), Pow(10, 10)), Sym("kbar")));
        c.put(UnitsPressure.FEMTOPASCAL, Mul(Pow(10, 23), Sym("kbar")));
        c.put(UnitsPressure.GIGAPASCAL, Mul(Rat(Int(1), Int(10)), Sym("kbar")));
        c.put(UnitsPressure.HECTOPASCAL, Mul(Pow(10, 6), Sym("kbar")));
        c.put(UnitsPressure.KILOPASCAL, Mul(Pow(10, 5), Sym("kbar")));
        c.put(UnitsPressure.MEGABAR, Mul(Rat(Int(1), Int(1000)), Sym("kbar")));
        c.put(UnitsPressure.MEGAPASCAL, Mul(Int(100), Sym("kbar")));
        c.put(UnitsPressure.MICROPASCAL, Mul(Pow(10, 14), Sym("kbar")));
        c.put(UnitsPressure.MILLIBAR, Mul(Pow(10, 6), Sym("kbar")));
        c.put(UnitsPressure.MILLIPASCAL, Mul(Pow(10, 11), Sym("kbar")));
        c.put(UnitsPressure.MILLITORR, Mul(Rat(Mul(Int(304), Pow(10, 4)), Int(4053)), Sym("kbar")));
        c.put(UnitsPressure.MMHG, Mul(Rat(Mul(Int(2), Pow(10, 16)), Int("26664477483")), Sym("kbar")));
        c.put(UnitsPressure.NANOPASCAL, Mul(Pow(10, 17), Sym("kbar")));
        c.put(UnitsPressure.PETAPASCAL, Mul(Rat(Int(1), Pow(10, 7)), Sym("kbar")));
        c.put(UnitsPressure.PICOPASCAL, Mul(Pow(10, 20), Sym("kbar")));
        c.put(UnitsPressure.PSI, Mul(Rat(Mul(Int(25), Pow(10, 17)), Int("172368932329209")), Sym("kbar")));
        c.put(UnitsPressure.Pascal, Mul(Pow(10, 8), Sym("kbar")));
        c.put(UnitsPressure.TERAPASCAL, Mul(Rat(Int(1), Pow(10, 4)), Sym("kbar")));
        c.put(UnitsPressure.TORR, Mul(Rat(Mul(Int(304), Pow(10, 7)), Int(4053)), Sym("kbar")));
        c.put(UnitsPressure.YOCTOPASCAL, Mul(Pow(10, 32), Sym("kbar")));
        c.put(UnitsPressure.YOTTAPASCAL, Mul(Rat(Int(1), Pow(10, 16)), Sym("kbar")));
        c.put(UnitsPressure.ZEPTOPASCAL, Mul(Pow(10, 29), Sym("kbar")));
        c.put(UnitsPressure.ZETTAPASCAL, Mul(Rat(Int(1), Pow(10, 13)), Sym("kbar")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsPressure, Conversion> createMapKILOPASCAL() {
        EnumMap<UnitsPressure, Conversion> c =
            new EnumMap<UnitsPressure, Conversion>(UnitsPressure.class);
        c.put(UnitsPressure.ATMOSPHERE, Mul(Rat(Int(40), Int(4053)), Sym("kilopa")));
        c.put(UnitsPressure.ATTOPASCAL, Mul(Pow(10, 21), Sym("kilopa")));
        c.put(UnitsPressure.BAR, Mul(Rat(Int(1), Int(100)), Sym("kilopa")));
        c.put(UnitsPressure.CENTIBAR, Sym("kilopa"));
        c.put(UnitsPressure.CENTIPASCAL, Mul(Pow(10, 5), Sym("kilopa")));
        c.put(UnitsPressure.DECAPASCAL, Mul(Int(100), Sym("kilopa")));
        c.put(UnitsPressure.DECIBAR, Mul(Rat(Int(1), Int(10)), Sym("kilopa")));
        c.put(UnitsPressure.DECIPASCAL, Mul(Pow(10, 4), Sym("kilopa")));
        c.put(UnitsPressure.EXAPASCAL, Mul(Rat(Int(1), Pow(10, 15)), Sym("kilopa")));
        c.put(UnitsPressure.FEMTOPASCAL, Mul(Pow(10, 18), Sym("kilopa")));
        c.put(UnitsPressure.GIGAPASCAL, Mul(Rat(Int(1), Pow(10, 6)), Sym("kilopa")));
        c.put(UnitsPressure.HECTOPASCAL, Mul(Int(10), Sym("kilopa")));
        c.put(UnitsPressure.KILOBAR, Mul(Rat(Int(1), Pow(10, 5)), Sym("kilopa")));
        c.put(UnitsPressure.MEGABAR, Mul(Rat(Int(1), Pow(10, 8)), Sym("kilopa")));
        c.put(UnitsPressure.MEGAPASCAL, Mul(Rat(Int(1), Int(1000)), Sym("kilopa")));
        c.put(UnitsPressure.MICROPASCAL, Mul(Pow(10, 9), Sym("kilopa")));
        c.put(UnitsPressure.MILLIBAR, Mul(Int(10), Sym("kilopa")));
        c.put(UnitsPressure.MILLIPASCAL, Mul(Pow(10, 6), Sym("kilopa")));
        c.put(UnitsPressure.MILLITORR, Mul(Rat(Int(152), Int(20265)), Sym("kilopa")));
        c.put(UnitsPressure.MMHG, Mul(Rat(Mul(Int(2), Pow(10, 11)), Int("26664477483")), Sym("kilopa")));
        c.put(UnitsPressure.NANOPASCAL, Mul(Pow(10, 12), Sym("kilopa")));
        c.put(UnitsPressure.PETAPASCAL, Mul(Rat(Int(1), Pow(10, 12)), Sym("kilopa")));
        c.put(UnitsPressure.PICOPASCAL, Mul(Pow(10, 15), Sym("kilopa")));
        c.put(UnitsPressure.PSI, Mul(Rat(Mul(Int(25), Pow(10, 12)), Int("172368932329209")), Sym("kilopa")));
        c.put(UnitsPressure.Pascal, Mul(Int(1000), Sym("kilopa")));
        c.put(UnitsPressure.TERAPASCAL, Mul(Rat(Int(1), Pow(10, 9)), Sym("kilopa")));
        c.put(UnitsPressure.TORR, Mul(Rat(Int(30400), Int(4053)), Sym("kilopa")));
        c.put(UnitsPressure.YOCTOPASCAL, Mul(Pow(10, 27), Sym("kilopa")));
        c.put(UnitsPressure.YOTTAPASCAL, Mul(Rat(Int(1), Pow(10, 21)), Sym("kilopa")));
        c.put(UnitsPressure.ZEPTOPASCAL, Mul(Pow(10, 24), Sym("kilopa")));
        c.put(UnitsPressure.ZETTAPASCAL, Mul(Rat(Int(1), Pow(10, 18)), Sym("kilopa")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsPressure, Conversion> createMapMEGABAR() {
        EnumMap<UnitsPressure, Conversion> c =
            new EnumMap<UnitsPressure, Conversion>(UnitsPressure.class);
        c.put(UnitsPressure.ATMOSPHERE, Mul(Rat(Mul(Int(4), Pow(10, 9)), Int(4053)), Sym("megabar")));
        c.put(UnitsPressure.ATTOPASCAL, Mul(Pow(10, 29), Sym("megabar")));
        c.put(UnitsPressure.BAR, Mul(Pow(10, 6), Sym("megabar")));
        c.put(UnitsPressure.CENTIBAR, Mul(Pow(10, 8), Sym("megabar")));
        c.put(UnitsPressure.CENTIPASCAL, Mul(Pow(10, 13), Sym("megabar")));
        c.put(UnitsPressure.DECAPASCAL, Mul(Pow(10, 10), Sym("megabar")));
        c.put(UnitsPressure.DECIBAR, Mul(Pow(10, 7), Sym("megabar")));
        c.put(UnitsPressure.DECIPASCAL, Mul(Pow(10, 12), Sym("megabar")));
        c.put(UnitsPressure.EXAPASCAL, Mul(Rat(Int(1), Pow(10, 7)), Sym("megabar")));
        c.put(UnitsPressure.FEMTOPASCAL, Mul(Pow(10, 26), Sym("megabar")));
        c.put(UnitsPressure.GIGAPASCAL, Mul(Int(100), Sym("megabar")));
        c.put(UnitsPressure.HECTOPASCAL, Mul(Pow(10, 9), Sym("megabar")));
        c.put(UnitsPressure.KILOBAR, Mul(Int(1000), Sym("megabar")));
        c.put(UnitsPressure.KILOPASCAL, Mul(Pow(10, 8), Sym("megabar")));
        c.put(UnitsPressure.MEGAPASCAL, Mul(Pow(10, 5), Sym("megabar")));
        c.put(UnitsPressure.MICROPASCAL, Mul(Pow(10, 17), Sym("megabar")));
        c.put(UnitsPressure.MILLIBAR, Mul(Pow(10, 9), Sym("megabar")));
        c.put(UnitsPressure.MILLIPASCAL, Mul(Pow(10, 14), Sym("megabar")));
        c.put(UnitsPressure.MILLITORR, Mul(Rat(Mul(Int(304), Pow(10, 7)), Int(4053)), Sym("megabar")));
        c.put(UnitsPressure.MMHG, Mul(Rat(Mul(Int(2), Pow(10, 19)), Int("26664477483")), Sym("megabar")));
        c.put(UnitsPressure.NANOPASCAL, Mul(Pow(10, 20), Sym("megabar")));
        c.put(UnitsPressure.PETAPASCAL, Mul(Rat(Int(1), Pow(10, 4)), Sym("megabar")));
        c.put(UnitsPressure.PICOPASCAL, Mul(Pow(10, 23), Sym("megabar")));
        c.put(UnitsPressure.PSI, Mul(Rat(Mul(Int(25), Pow(10, 20)), Int("172368932329209")), Sym("megabar")));
        c.put(UnitsPressure.Pascal, Mul(Pow(10, 11), Sym("megabar")));
        c.put(UnitsPressure.TERAPASCAL, Mul(Rat(Int(1), Int(10)), Sym("megabar")));
        c.put(UnitsPressure.TORR, Mul(Rat(Mul(Int(304), Pow(10, 10)), Int(4053)), Sym("megabar")));
        c.put(UnitsPressure.YOCTOPASCAL, Mul(Pow(10, 35), Sym("megabar")));
        c.put(UnitsPressure.YOTTAPASCAL, Mul(Rat(Int(1), Pow(10, 13)), Sym("megabar")));
        c.put(UnitsPressure.ZEPTOPASCAL, Mul(Pow(10, 32), Sym("megabar")));
        c.put(UnitsPressure.ZETTAPASCAL, Mul(Rat(Int(1), Pow(10, 10)), Sym("megabar")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsPressure, Conversion> createMapMEGAPASCAL() {
        EnumMap<UnitsPressure, Conversion> c =
            new EnumMap<UnitsPressure, Conversion>(UnitsPressure.class);
        c.put(UnitsPressure.ATMOSPHERE, Mul(Rat(Mul(Int(4), Pow(10, 4)), Int(4053)), Sym("megapa")));
        c.put(UnitsPressure.ATTOPASCAL, Mul(Pow(10, 24), Sym("megapa")));
        c.put(UnitsPressure.BAR, Mul(Int(10), Sym("megapa")));
        c.put(UnitsPressure.CENTIBAR, Mul(Int(1000), Sym("megapa")));
        c.put(UnitsPressure.CENTIPASCAL, Mul(Pow(10, 8), Sym("megapa")));
        c.put(UnitsPressure.DECAPASCAL, Mul(Pow(10, 5), Sym("megapa")));
        c.put(UnitsPressure.DECIBAR, Mul(Int(100), Sym("megapa")));
        c.put(UnitsPressure.DECIPASCAL, Mul(Pow(10, 7), Sym("megapa")));
        c.put(UnitsPressure.EXAPASCAL, Mul(Rat(Int(1), Pow(10, 12)), Sym("megapa")));
        c.put(UnitsPressure.FEMTOPASCAL, Mul(Pow(10, 21), Sym("megapa")));
        c.put(UnitsPressure.GIGAPASCAL, Mul(Rat(Int(1), Int(1000)), Sym("megapa")));
        c.put(UnitsPressure.HECTOPASCAL, Mul(Pow(10, 4), Sym("megapa")));
        c.put(UnitsPressure.KILOBAR, Mul(Rat(Int(1), Int(100)), Sym("megapa")));
        c.put(UnitsPressure.KILOPASCAL, Mul(Int(1000), Sym("megapa")));
        c.put(UnitsPressure.MEGABAR, Mul(Rat(Int(1), Pow(10, 5)), Sym("megapa")));
        c.put(UnitsPressure.MICROPASCAL, Mul(Pow(10, 12), Sym("megapa")));
        c.put(UnitsPressure.MILLIBAR, Mul(Pow(10, 4), Sym("megapa")));
        c.put(UnitsPressure.MILLIPASCAL, Mul(Pow(10, 9), Sym("megapa")));
        c.put(UnitsPressure.MILLITORR, Mul(Rat(Int(30400), Int(4053)), Sym("megapa")));
        c.put(UnitsPressure.MMHG, Mul(Rat(Mul(Int(2), Pow(10, 14)), Int("26664477483")), Sym("megapa")));
        c.put(UnitsPressure.NANOPASCAL, Mul(Pow(10, 15), Sym("megapa")));
        c.put(UnitsPressure.PETAPASCAL, Mul(Rat(Int(1), Pow(10, 9)), Sym("megapa")));
        c.put(UnitsPressure.PICOPASCAL, Mul(Pow(10, 18), Sym("megapa")));
        c.put(UnitsPressure.PSI, Mul(Rat(Mul(Int(25), Pow(10, 15)), Int("172368932329209")), Sym("megapa")));
        c.put(UnitsPressure.Pascal, Mul(Pow(10, 6), Sym("megapa")));
        c.put(UnitsPressure.TERAPASCAL, Mul(Rat(Int(1), Pow(10, 6)), Sym("megapa")));
        c.put(UnitsPressure.TORR, Mul(Rat(Mul(Int(304), Pow(10, 5)), Int(4053)), Sym("megapa")));
        c.put(UnitsPressure.YOCTOPASCAL, Mul(Pow(10, 30), Sym("megapa")));
        c.put(UnitsPressure.YOTTAPASCAL, Mul(Rat(Int(1), Pow(10, 18)), Sym("megapa")));
        c.put(UnitsPressure.ZEPTOPASCAL, Mul(Pow(10, 27), Sym("megapa")));
        c.put(UnitsPressure.ZETTAPASCAL, Mul(Rat(Int(1), Pow(10, 15)), Sym("megapa")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsPressure, Conversion> createMapMICROPASCAL() {
        EnumMap<UnitsPressure, Conversion> c =
            new EnumMap<UnitsPressure, Conversion>(UnitsPressure.class);
        c.put(UnitsPressure.ATMOSPHERE, Mul(Rat(Int(1), Mul(Int(101325), Pow(10, 6))), Sym("micropa")));
        c.put(UnitsPressure.ATTOPASCAL, Mul(Pow(10, 12), Sym("micropa")));
        c.put(UnitsPressure.BAR, Mul(Rat(Int(1), Pow(10, 11)), Sym("micropa")));
        c.put(UnitsPressure.CENTIBAR, Mul(Rat(Int(1), Pow(10, 9)), Sym("micropa")));
        c.put(UnitsPressure.CENTIPASCAL, Mul(Rat(Int(1), Pow(10, 4)), Sym("micropa")));
        c.put(UnitsPressure.DECAPASCAL, Mul(Rat(Int(1), Pow(10, 7)), Sym("micropa")));
        c.put(UnitsPressure.DECIBAR, Mul(Rat(Int(1), Pow(10, 10)), Sym("micropa")));
        c.put(UnitsPressure.DECIPASCAL, Mul(Rat(Int(1), Pow(10, 5)), Sym("micropa")));
        c.put(UnitsPressure.EXAPASCAL, Mul(Rat(Int(1), Pow(10, 24)), Sym("micropa")));
        c.put(UnitsPressure.FEMTOPASCAL, Mul(Pow(10, 9), Sym("micropa")));
        c.put(UnitsPressure.GIGAPASCAL, Mul(Rat(Int(1), Pow(10, 15)), Sym("micropa")));
        c.put(UnitsPressure.HECTOPASCAL, Mul(Rat(Int(1), Pow(10, 8)), Sym("micropa")));
        c.put(UnitsPressure.KILOBAR, Mul(Rat(Int(1), Pow(10, 14)), Sym("micropa")));
        c.put(UnitsPressure.KILOPASCAL, Mul(Rat(Int(1), Pow(10, 9)), Sym("micropa")));
        c.put(UnitsPressure.MEGABAR, Mul(Rat(Int(1), Pow(10, 17)), Sym("micropa")));
        c.put(UnitsPressure.MEGAPASCAL, Mul(Rat(Int(1), Pow(10, 12)), Sym("micropa")));
        c.put(UnitsPressure.MILLIBAR, Mul(Rat(Int(1), Pow(10, 8)), Sym("micropa")));
        c.put(UnitsPressure.MILLIPASCAL, Mul(Rat(Int(1), Int(1000)), Sym("micropa")));
        c.put(UnitsPressure.MILLITORR, Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 6))), Sym("micropa")));
        c.put(UnitsPressure.MMHG, Mul(Rat(Int(200), Int("26664477483")), Sym("micropa")));
        c.put(UnitsPressure.NANOPASCAL, Mul(Int(1000), Sym("micropa")));
        c.put(UnitsPressure.PETAPASCAL, Mul(Rat(Int(1), Pow(10, 21)), Sym("micropa")));
        c.put(UnitsPressure.PICOPASCAL, Mul(Pow(10, 6), Sym("micropa")));
        c.put(UnitsPressure.PSI, Mul(Rat(Int(25000), Int("172368932329209")), Sym("micropa")));
        c.put(UnitsPressure.Pascal, Mul(Rat(Int(1), Pow(10, 6)), Sym("micropa")));
        c.put(UnitsPressure.TERAPASCAL, Mul(Rat(Int(1), Pow(10, 18)), Sym("micropa")));
        c.put(UnitsPressure.TORR, Mul(Rat(Int(19), Int("2533125000")), Sym("micropa")));
        c.put(UnitsPressure.YOCTOPASCAL, Mul(Pow(10, 18), Sym("micropa")));
        c.put(UnitsPressure.YOTTAPASCAL, Mul(Rat(Int(1), Pow(10, 30)), Sym("micropa")));
        c.put(UnitsPressure.ZEPTOPASCAL, Mul(Pow(10, 15), Sym("micropa")));
        c.put(UnitsPressure.ZETTAPASCAL, Mul(Rat(Int(1), Pow(10, 27)), Sym("micropa")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsPressure, Conversion> createMapMILLIBAR() {
        EnumMap<UnitsPressure, Conversion> c =
            new EnumMap<UnitsPressure, Conversion>(UnitsPressure.class);
        c.put(UnitsPressure.ATMOSPHERE, Mul(Rat(Int(4), Int(4053)), Sym("mbar")));
        c.put(UnitsPressure.ATTOPASCAL, Mul(Pow(10, 20), Sym("mbar")));
        c.put(UnitsPressure.BAR, Mul(Rat(Int(1), Int(1000)), Sym("mbar")));
        c.put(UnitsPressure.CENTIBAR, Mul(Rat(Int(1), Int(10)), Sym("mbar")));
        c.put(UnitsPressure.CENTIPASCAL, Mul(Pow(10, 4), Sym("mbar")));
        c.put(UnitsPressure.DECAPASCAL, Mul(Int(10), Sym("mbar")));
        c.put(UnitsPressure.DECIBAR, Mul(Rat(Int(1), Int(100)), Sym("mbar")));
        c.put(UnitsPressure.DECIPASCAL, Mul(Int(1000), Sym("mbar")));
        c.put(UnitsPressure.EXAPASCAL, Mul(Rat(Int(1), Pow(10, 16)), Sym("mbar")));
        c.put(UnitsPressure.FEMTOPASCAL, Mul(Pow(10, 17), Sym("mbar")));
        c.put(UnitsPressure.GIGAPASCAL, Mul(Rat(Int(1), Pow(10, 7)), Sym("mbar")));
        c.put(UnitsPressure.HECTOPASCAL, Sym("mbar"));
        c.put(UnitsPressure.KILOBAR, Mul(Rat(Int(1), Pow(10, 6)), Sym("mbar")));
        c.put(UnitsPressure.KILOPASCAL, Mul(Rat(Int(1), Int(10)), Sym("mbar")));
        c.put(UnitsPressure.MEGABAR, Mul(Rat(Int(1), Pow(10, 9)), Sym("mbar")));
        c.put(UnitsPressure.MEGAPASCAL, Mul(Rat(Int(1), Pow(10, 4)), Sym("mbar")));
        c.put(UnitsPressure.MICROPASCAL, Mul(Pow(10, 8), Sym("mbar")));
        c.put(UnitsPressure.MILLIPASCAL, Mul(Pow(10, 5), Sym("mbar")));
        c.put(UnitsPressure.MILLITORR, Mul(Rat(Int(76), Int(101325)), Sym("mbar")));
        c.put(UnitsPressure.MMHG, Mul(Rat(Mul(Int(2), Pow(10, 10)), Int("26664477483")), Sym("mbar")));
        c.put(UnitsPressure.NANOPASCAL, Mul(Pow(10, 11), Sym("mbar")));
        c.put(UnitsPressure.PETAPASCAL, Mul(Rat(Int(1), Pow(10, 13)), Sym("mbar")));
        c.put(UnitsPressure.PICOPASCAL, Mul(Pow(10, 14), Sym("mbar")));
        c.put(UnitsPressure.PSI, Mul(Rat(Mul(Int(25), Pow(10, 11)), Int("172368932329209")), Sym("mbar")));
        c.put(UnitsPressure.Pascal, Mul(Int(100), Sym("mbar")));
        c.put(UnitsPressure.TERAPASCAL, Mul(Rat(Int(1), Pow(10, 10)), Sym("mbar")));
        c.put(UnitsPressure.TORR, Mul(Rat(Int(3040), Int(4053)), Sym("mbar")));
        c.put(UnitsPressure.YOCTOPASCAL, Mul(Pow(10, 26), Sym("mbar")));
        c.put(UnitsPressure.YOTTAPASCAL, Mul(Rat(Int(1), Pow(10, 22)), Sym("mbar")));
        c.put(UnitsPressure.ZEPTOPASCAL, Mul(Pow(10, 23), Sym("mbar")));
        c.put(UnitsPressure.ZETTAPASCAL, Mul(Rat(Int(1), Pow(10, 19)), Sym("mbar")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsPressure, Conversion> createMapMILLIPASCAL() {
        EnumMap<UnitsPressure, Conversion> c =
            new EnumMap<UnitsPressure, Conversion>(UnitsPressure.class);
        c.put(UnitsPressure.ATMOSPHERE, Mul(Rat(Int(1), Int(101325000)), Sym("millipa")));
        c.put(UnitsPressure.ATTOPASCAL, Mul(Pow(10, 15), Sym("millipa")));
        c.put(UnitsPressure.BAR, Mul(Rat(Int(1), Pow(10, 8)), Sym("millipa")));
        c.put(UnitsPressure.CENTIBAR, Mul(Rat(Int(1), Pow(10, 6)), Sym("millipa")));
        c.put(UnitsPressure.CENTIPASCAL, Mul(Rat(Int(1), Int(10)), Sym("millipa")));
        c.put(UnitsPressure.DECAPASCAL, Mul(Rat(Int(1), Pow(10, 4)), Sym("millipa")));
        c.put(UnitsPressure.DECIBAR, Mul(Rat(Int(1), Pow(10, 7)), Sym("millipa")));
        c.put(UnitsPressure.DECIPASCAL, Mul(Rat(Int(1), Int(100)), Sym("millipa")));
        c.put(UnitsPressure.EXAPASCAL, Mul(Rat(Int(1), Pow(10, 21)), Sym("millipa")));
        c.put(UnitsPressure.FEMTOPASCAL, Mul(Pow(10, 12), Sym("millipa")));
        c.put(UnitsPressure.GIGAPASCAL, Mul(Rat(Int(1), Pow(10, 12)), Sym("millipa")));
        c.put(UnitsPressure.HECTOPASCAL, Mul(Rat(Int(1), Pow(10, 5)), Sym("millipa")));
        c.put(UnitsPressure.KILOBAR, Mul(Rat(Int(1), Pow(10, 11)), Sym("millipa")));
        c.put(UnitsPressure.KILOPASCAL, Mul(Rat(Int(1), Pow(10, 6)), Sym("millipa")));
        c.put(UnitsPressure.MEGABAR, Mul(Rat(Int(1), Pow(10, 14)), Sym("millipa")));
        c.put(UnitsPressure.MEGAPASCAL, Mul(Rat(Int(1), Pow(10, 9)), Sym("millipa")));
        c.put(UnitsPressure.MICROPASCAL, Mul(Int(1000), Sym("millipa")));
        c.put(UnitsPressure.MILLIBAR, Mul(Rat(Int(1), Pow(10, 5)), Sym("millipa")));
        c.put(UnitsPressure.MILLITORR, Mul(Rat(Int(19), Int("2533125000")), Sym("millipa")));
        c.put(UnitsPressure.MMHG, Mul(Rat(Mul(Int(2), Pow(10, 5)), Int("26664477483")), Sym("millipa")));
        c.put(UnitsPressure.NANOPASCAL, Mul(Pow(10, 6), Sym("millipa")));
        c.put(UnitsPressure.PETAPASCAL, Mul(Rat(Int(1), Pow(10, 18)), Sym("millipa")));
        c.put(UnitsPressure.PICOPASCAL, Mul(Pow(10, 9), Sym("millipa")));
        c.put(UnitsPressure.PSI, Mul(Rat(Mul(Int(25), Pow(10, 6)), Int("172368932329209")), Sym("millipa")));
        c.put(UnitsPressure.Pascal, Mul(Rat(Int(1), Int(1000)), Sym("millipa")));
        c.put(UnitsPressure.TERAPASCAL, Mul(Rat(Int(1), Pow(10, 15)), Sym("millipa")));
        c.put(UnitsPressure.TORR, Mul(Rat(Int(19), Int(2533125)), Sym("millipa")));
        c.put(UnitsPressure.YOCTOPASCAL, Mul(Pow(10, 21), Sym("millipa")));
        c.put(UnitsPressure.YOTTAPASCAL, Mul(Rat(Int(1), Pow(10, 27)), Sym("millipa")));
        c.put(UnitsPressure.ZEPTOPASCAL, Mul(Pow(10, 18), Sym("millipa")));
        c.put(UnitsPressure.ZETTAPASCAL, Mul(Rat(Int(1), Pow(10, 24)), Sym("millipa")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsPressure, Conversion> createMapMILLITORR() {
        EnumMap<UnitsPressure, Conversion> c =
            new EnumMap<UnitsPressure, Conversion>(UnitsPressure.class);
        c.put(UnitsPressure.ATMOSPHERE, Mul(Rat(Int(25), Int(19)), Sym("mtorr")));
        c.put(UnitsPressure.ATTOPASCAL, Mul(Rat(Mul(Int(2533125), Pow(10, 18)), Int(19)), Sym("mtorr")));
        c.put(UnitsPressure.BAR, Mul(Rat(Int(4053), Int(3040)), Sym("mtorr")));
        c.put(UnitsPressure.CENTIBAR, Mul(Rat(Int(20265), Int(152)), Sym("mtorr")));
        c.put(UnitsPressure.CENTIPASCAL, Mul(Rat(Int(253312500), Int(19)), Sym("mtorr")));
        c.put(UnitsPressure.DECAPASCAL, Mul(Rat(Int(506625), Int(38)), Sym("mtorr")));
        c.put(UnitsPressure.DECIBAR, Mul(Rat(Int(4053), Int(304)), Sym("mtorr")));
        c.put(UnitsPressure.DECIPASCAL, Mul(Rat(Int(25331250), Int(19)), Sym("mtorr")));
        c.put(UnitsPressure.EXAPASCAL, Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 14))), Sym("mtorr")));
        c.put(UnitsPressure.FEMTOPASCAL, Mul(Rat(Mul(Int(2533125), Pow(10, 15)), Int(19)), Sym("mtorr")));
        c.put(UnitsPressure.GIGAPASCAL, Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 5))), Sym("mtorr")));
        c.put(UnitsPressure.HECTOPASCAL, Mul(Rat(Int(101325), Int(76)), Sym("mtorr")));
        c.put(UnitsPressure.KILOBAR, Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 4))), Sym("mtorr")));
        c.put(UnitsPressure.KILOPASCAL, Mul(Rat(Int(20265), Int(152)), Sym("mtorr")));
        c.put(UnitsPressure.MEGABAR, Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 7))), Sym("mtorr")));
        c.put(UnitsPressure.MEGAPASCAL, Mul(Rat(Int(4053), Int(30400)), Sym("mtorr")));
        c.put(UnitsPressure.MICROPASCAL, Mul(Rat(Mul(Int(2533125), Pow(10, 6)), Int(19)), Sym("mtorr")));
        c.put(UnitsPressure.MILLIBAR, Mul(Rat(Int(101325), Int(76)), Sym("mtorr")));
        c.put(UnitsPressure.MILLIPASCAL, Mul(Rat(Int("2533125000"), Int(19)), Sym("mtorr")));
        c.put(UnitsPressure.MMHG, Mul(Rat(Mul(Int(24125), Pow(10, 9)), Int("24125003437")), Sym("mtorr")));
        c.put(UnitsPressure.NANOPASCAL, Mul(Rat(Mul(Int(2533125), Pow(10, 9)), Int(19)), Sym("mtorr")));
        c.put(UnitsPressure.PETAPASCAL, Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 11))), Sym("mtorr")));
        c.put(UnitsPressure.PICOPASCAL, Mul(Rat(Mul(Int(2533125), Pow(10, 12)), Int(19)), Sym("mtorr")));
        c.put(UnitsPressure.PSI, Mul(Rat(Mul(Int(3015625), Pow(10, 9)), Int("155952843535951")), Sym("mtorr")));
        c.put(UnitsPressure.Pascal, Mul(Rat(Int(2533125), Int(19)), Sym("mtorr")));
        c.put(UnitsPressure.TERAPASCAL, Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 8))), Sym("mtorr")));
        c.put(UnitsPressure.TORR, Mul(Int(1000), Sym("mtorr")));
        c.put(UnitsPressure.YOCTOPASCAL, Mul(Rat(Mul(Int(2533125), Pow(10, 24)), Int(19)), Sym("mtorr")));
        c.put(UnitsPressure.YOTTAPASCAL, Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 20))), Sym("mtorr")));
        c.put(UnitsPressure.ZEPTOPASCAL, Mul(Rat(Mul(Int(2533125), Pow(10, 21)), Int(19)), Sym("mtorr")));
        c.put(UnitsPressure.ZETTAPASCAL, Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 17))), Sym("mtorr")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsPressure, Conversion> createMapMMHG() {
        EnumMap<UnitsPressure, Conversion> c =
            new EnumMap<UnitsPressure, Conversion>(UnitsPressure.class);
        c.put(UnitsPressure.ATMOSPHERE, Mul(Rat(Int(1269737023), Mul(Int(965), Pow(10, 9))), Sym("mmhg")));
        c.put(UnitsPressure.ATTOPASCAL, Mul(Mul(Int("133322387415"), Pow(10, 9)), Sym("mmhg")));
        c.put(UnitsPressure.BAR, Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 13))), Sym("mmhg")));
        c.put(UnitsPressure.CENTIBAR, Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 11))), Sym("mmhg")));
        c.put(UnitsPressure.CENTIPASCAL, Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 6))), Sym("mmhg")));
        c.put(UnitsPressure.DECAPASCAL, Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 9))), Sym("mmhg")));
        c.put(UnitsPressure.DECIBAR, Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 12))), Sym("mmhg")));
        c.put(UnitsPressure.DECIPASCAL, Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 7))), Sym("mmhg")));
        c.put(UnitsPressure.EXAPASCAL, Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 26))), Sym("mmhg")));
        c.put(UnitsPressure.FEMTOPASCAL, Mul(Mul(Int("133322387415"), Pow(10, 6)), Sym("mmhg")));
        c.put(UnitsPressure.GIGAPASCAL, Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 17))), Sym("mmhg")));
        c.put(UnitsPressure.HECTOPASCAL, Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 10))), Sym("mmhg")));
        c.put(UnitsPressure.KILOBAR, Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 16))), Sym("mmhg")));
        c.put(UnitsPressure.KILOPASCAL, Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 11))), Sym("mmhg")));
        c.put(UnitsPressure.MEGABAR, Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 19))), Sym("mmhg")));
        c.put(UnitsPressure.MEGAPASCAL, Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 14))), Sym("mmhg")));
        c.put(UnitsPressure.MICROPASCAL, Mul(Rat(Int("26664477483"), Int(200)), Sym("mmhg")));
        c.put(UnitsPressure.MILLIBAR, Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 10))), Sym("mmhg")));
        c.put(UnitsPressure.MILLIPASCAL, Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 5))), Sym("mmhg")));
        c.put(UnitsPressure.MILLITORR, Mul(Rat(Int("24125003437"), Mul(Int(24125), Pow(10, 9))), Sym("mmhg")));
        c.put(UnitsPressure.NANOPASCAL, Mul(Int("133322387415"), Sym("mmhg")));
        c.put(UnitsPressure.PETAPASCAL, Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 23))), Sym("mmhg")));
        c.put(UnitsPressure.PICOPASCAL, Mul(Int("133322387415000"), Sym("mmhg")));
        c.put(UnitsPressure.PSI, Mul(Rat(Int("158717127875"), Int("8208044396629")), Sym("mmhg")));
        c.put(UnitsPressure.Pascal, Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 8))), Sym("mmhg")));
        c.put(UnitsPressure.TERAPASCAL, Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 20))), Sym("mmhg")));
        c.put(UnitsPressure.TORR, Mul(Rat(Int("24125003437"), Mul(Int(24125), Pow(10, 6))), Sym("mmhg")));
        c.put(UnitsPressure.YOCTOPASCAL, Mul(Mul(Int("133322387415"), Pow(10, 15)), Sym("mmhg")));
        c.put(UnitsPressure.YOTTAPASCAL, Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 32))), Sym("mmhg")));
        c.put(UnitsPressure.ZEPTOPASCAL, Mul(Mul(Int("133322387415"), Pow(10, 12)), Sym("mmhg")));
        c.put(UnitsPressure.ZETTAPASCAL, Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 29))), Sym("mmhg")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsPressure, Conversion> createMapNANOPASCAL() {
        EnumMap<UnitsPressure, Conversion> c =
            new EnumMap<UnitsPressure, Conversion>(UnitsPressure.class);
        c.put(UnitsPressure.ATMOSPHERE, Mul(Rat(Int(1), Mul(Int(101325), Pow(10, 9))), Sym("nanopa")));
        c.put(UnitsPressure.ATTOPASCAL, Mul(Pow(10, 9), Sym("nanopa")));
        c.put(UnitsPressure.BAR, Mul(Rat(Int(1), Pow(10, 14)), Sym("nanopa")));
        c.put(UnitsPressure.CENTIBAR, Mul(Rat(Int(1), Pow(10, 12)), Sym("nanopa")));
        c.put(UnitsPressure.CENTIPASCAL, Mul(Rat(Int(1), Pow(10, 7)), Sym("nanopa")));
        c.put(UnitsPressure.DECAPASCAL, Mul(Rat(Int(1), Pow(10, 10)), Sym("nanopa")));
        c.put(UnitsPressure.DECIBAR, Mul(Rat(Int(1), Pow(10, 13)), Sym("nanopa")));
        c.put(UnitsPressure.DECIPASCAL, Mul(Rat(Int(1), Pow(10, 8)), Sym("nanopa")));
        c.put(UnitsPressure.EXAPASCAL, Mul(Rat(Int(1), Pow(10, 27)), Sym("nanopa")));
        c.put(UnitsPressure.FEMTOPASCAL, Mul(Pow(10, 6), Sym("nanopa")));
        c.put(UnitsPressure.GIGAPASCAL, Mul(Rat(Int(1), Pow(10, 18)), Sym("nanopa")));
        c.put(UnitsPressure.HECTOPASCAL, Mul(Rat(Int(1), Pow(10, 11)), Sym("nanopa")));
        c.put(UnitsPressure.KILOBAR, Mul(Rat(Int(1), Pow(10, 17)), Sym("nanopa")));
        c.put(UnitsPressure.KILOPASCAL, Mul(Rat(Int(1), Pow(10, 12)), Sym("nanopa")));
        c.put(UnitsPressure.MEGABAR, Mul(Rat(Int(1), Pow(10, 20)), Sym("nanopa")));
        c.put(UnitsPressure.MEGAPASCAL, Mul(Rat(Int(1), Pow(10, 15)), Sym("nanopa")));
        c.put(UnitsPressure.MICROPASCAL, Mul(Rat(Int(1), Int(1000)), Sym("nanopa")));
        c.put(UnitsPressure.MILLIBAR, Mul(Rat(Int(1), Pow(10, 11)), Sym("nanopa")));
        c.put(UnitsPressure.MILLIPASCAL, Mul(Rat(Int(1), Pow(10, 6)), Sym("nanopa")));
        c.put(UnitsPressure.MILLITORR, Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 9))), Sym("nanopa")));
        c.put(UnitsPressure.MMHG, Mul(Rat(Int(1), Int("133322387415")), Sym("nanopa")));
        c.put(UnitsPressure.PETAPASCAL, Mul(Rat(Int(1), Pow(10, 24)), Sym("nanopa")));
        c.put(UnitsPressure.PICOPASCAL, Mul(Int(1000), Sym("nanopa")));
        c.put(UnitsPressure.PSI, Mul(Rat(Int(25), Int("172368932329209")), Sym("nanopa")));
        c.put(UnitsPressure.Pascal, Mul(Rat(Int(1), Pow(10, 9)), Sym("nanopa")));
        c.put(UnitsPressure.TERAPASCAL, Mul(Rat(Int(1), Pow(10, 21)), Sym("nanopa")));
        c.put(UnitsPressure.TORR, Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 6))), Sym("nanopa")));
        c.put(UnitsPressure.YOCTOPASCAL, Mul(Pow(10, 15), Sym("nanopa")));
        c.put(UnitsPressure.YOTTAPASCAL, Mul(Rat(Int(1), Pow(10, 33)), Sym("nanopa")));
        c.put(UnitsPressure.ZEPTOPASCAL, Mul(Pow(10, 12), Sym("nanopa")));
        c.put(UnitsPressure.ZETTAPASCAL, Mul(Rat(Int(1), Pow(10, 30)), Sym("nanopa")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsPressure, Conversion> createMapPETAPASCAL() {
        EnumMap<UnitsPressure, Conversion> c =
            new EnumMap<UnitsPressure, Conversion>(UnitsPressure.class);
        c.put(UnitsPressure.ATMOSPHERE, Mul(Rat(Mul(Int(4), Pow(10, 13)), Int(4053)), Sym("petapa")));
        c.put(UnitsPressure.ATTOPASCAL, Mul(Pow(10, 33), Sym("petapa")));
        c.put(UnitsPressure.BAR, Mul(Pow(10, 10), Sym("petapa")));
        c.put(UnitsPressure.CENTIBAR, Mul(Pow(10, 12), Sym("petapa")));
        c.put(UnitsPressure.CENTIPASCAL, Mul(Pow(10, 17), Sym("petapa")));
        c.put(UnitsPressure.DECAPASCAL, Mul(Pow(10, 14), Sym("petapa")));
        c.put(UnitsPressure.DECIBAR, Mul(Pow(10, 11), Sym("petapa")));
        c.put(UnitsPressure.DECIPASCAL, Mul(Pow(10, 16), Sym("petapa")));
        c.put(UnitsPressure.EXAPASCAL, Mul(Rat(Int(1), Int(1000)), Sym("petapa")));
        c.put(UnitsPressure.FEMTOPASCAL, Mul(Pow(10, 30), Sym("petapa")));
        c.put(UnitsPressure.GIGAPASCAL, Mul(Pow(10, 6), Sym("petapa")));
        c.put(UnitsPressure.HECTOPASCAL, Mul(Pow(10, 13), Sym("petapa")));
        c.put(UnitsPressure.KILOBAR, Mul(Pow(10, 7), Sym("petapa")));
        c.put(UnitsPressure.KILOPASCAL, Mul(Pow(10, 12), Sym("petapa")));
        c.put(UnitsPressure.MEGABAR, Mul(Pow(10, 4), Sym("petapa")));
        c.put(UnitsPressure.MEGAPASCAL, Mul(Pow(10, 9), Sym("petapa")));
        c.put(UnitsPressure.MICROPASCAL, Mul(Pow(10, 21), Sym("petapa")));
        c.put(UnitsPressure.MILLIBAR, Mul(Pow(10, 13), Sym("petapa")));
        c.put(UnitsPressure.MILLIPASCAL, Mul(Pow(10, 18), Sym("petapa")));
        c.put(UnitsPressure.MILLITORR, Mul(Rat(Mul(Int(304), Pow(10, 11)), Int(4053)), Sym("petapa")));
        c.put(UnitsPressure.MMHG, Mul(Rat(Mul(Int(2), Pow(10, 23)), Int("26664477483")), Sym("petapa")));
        c.put(UnitsPressure.NANOPASCAL, Mul(Pow(10, 24), Sym("petapa")));
        c.put(UnitsPressure.PICOPASCAL, Mul(Pow(10, 27), Sym("petapa")));
        c.put(UnitsPressure.PSI, Mul(Rat(Mul(Int(25), Pow(10, 24)), Int("172368932329209")), Sym("petapa")));
        c.put(UnitsPressure.Pascal, Mul(Pow(10, 15), Sym("petapa")));
        c.put(UnitsPressure.TERAPASCAL, Mul(Int(1000), Sym("petapa")));
        c.put(UnitsPressure.TORR, Mul(Rat(Mul(Int(304), Pow(10, 14)), Int(4053)), Sym("petapa")));
        c.put(UnitsPressure.YOCTOPASCAL, Mul(Pow(10, 39), Sym("petapa")));
        c.put(UnitsPressure.YOTTAPASCAL, Mul(Rat(Int(1), Pow(10, 9)), Sym("petapa")));
        c.put(UnitsPressure.ZEPTOPASCAL, Mul(Pow(10, 36), Sym("petapa")));
        c.put(UnitsPressure.ZETTAPASCAL, Mul(Rat(Int(1), Pow(10, 6)), Sym("petapa")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsPressure, Conversion> createMapPICOPASCAL() {
        EnumMap<UnitsPressure, Conversion> c =
            new EnumMap<UnitsPressure, Conversion>(UnitsPressure.class);
        c.put(UnitsPressure.ATMOSPHERE, Mul(Rat(Int(1), Mul(Int(101325), Pow(10, 12))), Sym("picopa")));
        c.put(UnitsPressure.ATTOPASCAL, Mul(Pow(10, 6), Sym("picopa")));
        c.put(UnitsPressure.BAR, Mul(Rat(Int(1), Pow(10, 17)), Sym("picopa")));
        c.put(UnitsPressure.CENTIBAR, Mul(Rat(Int(1), Pow(10, 15)), Sym("picopa")));
        c.put(UnitsPressure.CENTIPASCAL, Mul(Rat(Int(1), Pow(10, 10)), Sym("picopa")));
        c.put(UnitsPressure.DECAPASCAL, Mul(Rat(Int(1), Pow(10, 13)), Sym("picopa")));
        c.put(UnitsPressure.DECIBAR, Mul(Rat(Int(1), Pow(10, 16)), Sym("picopa")));
        c.put(UnitsPressure.DECIPASCAL, Mul(Rat(Int(1), Pow(10, 11)), Sym("picopa")));
        c.put(UnitsPressure.EXAPASCAL, Mul(Rat(Int(1), Pow(10, 30)), Sym("picopa")));
        c.put(UnitsPressure.FEMTOPASCAL, Mul(Int(1000), Sym("picopa")));
        c.put(UnitsPressure.GIGAPASCAL, Mul(Rat(Int(1), Pow(10, 21)), Sym("picopa")));
        c.put(UnitsPressure.HECTOPASCAL, Mul(Rat(Int(1), Pow(10, 14)), Sym("picopa")));
        c.put(UnitsPressure.KILOBAR, Mul(Rat(Int(1), Pow(10, 20)), Sym("picopa")));
        c.put(UnitsPressure.KILOPASCAL, Mul(Rat(Int(1), Pow(10, 15)), Sym("picopa")));
        c.put(UnitsPressure.MEGABAR, Mul(Rat(Int(1), Pow(10, 23)), Sym("picopa")));
        c.put(UnitsPressure.MEGAPASCAL, Mul(Rat(Int(1), Pow(10, 18)), Sym("picopa")));
        c.put(UnitsPressure.MICROPASCAL, Mul(Rat(Int(1), Pow(10, 6)), Sym("picopa")));
        c.put(UnitsPressure.MILLIBAR, Mul(Rat(Int(1), Pow(10, 14)), Sym("picopa")));
        c.put(UnitsPressure.MILLIPASCAL, Mul(Rat(Int(1), Pow(10, 9)), Sym("picopa")));
        c.put(UnitsPressure.MILLITORR, Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 12))), Sym("picopa")));
        c.put(UnitsPressure.MMHG, Mul(Rat(Int(1), Int("133322387415000")), Sym("picopa")));
        c.put(UnitsPressure.NANOPASCAL, Mul(Rat(Int(1), Int(1000)), Sym("picopa")));
        c.put(UnitsPressure.PETAPASCAL, Mul(Rat(Int(1), Pow(10, 27)), Sym("picopa")));
        c.put(UnitsPressure.PSI, Mul(Rat(Int(1), Int("6894757293168360")), Sym("picopa")));
        c.put(UnitsPressure.Pascal, Mul(Rat(Int(1), Pow(10, 12)), Sym("picopa")));
        c.put(UnitsPressure.TERAPASCAL, Mul(Rat(Int(1), Pow(10, 24)), Sym("picopa")));
        c.put(UnitsPressure.TORR, Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 9))), Sym("picopa")));
        c.put(UnitsPressure.YOCTOPASCAL, Mul(Pow(10, 12), Sym("picopa")));
        c.put(UnitsPressure.YOTTAPASCAL, Mul(Rat(Int(1), Pow(10, 36)), Sym("picopa")));
        c.put(UnitsPressure.ZEPTOPASCAL, Mul(Pow(10, 9), Sym("picopa")));
        c.put(UnitsPressure.ZETTAPASCAL, Mul(Rat(Int(1), Pow(10, 33)), Sym("picopa")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsPressure, Conversion> createMapPSI() {
        EnumMap<UnitsPressure, Conversion> c =
            new EnumMap<UnitsPressure, Conversion>(UnitsPressure.class);
        c.put(UnitsPressure.ATMOSPHERE, Mul(Rat(Int("8208044396629"), Mul(Int(120625), Pow(10, 9))), Sym("psi")));
        c.put(UnitsPressure.ATTOPASCAL, Mul(Mul(Int("689475729316836"), Pow(10, 7)), Sym("psi")));
        c.put(UnitsPressure.BAR, Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 14))), Sym("psi")));
        c.put(UnitsPressure.CENTIBAR, Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 12))), Sym("psi")));
        c.put(UnitsPressure.CENTIPASCAL, Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 7))), Sym("psi")));
        c.put(UnitsPressure.DECAPASCAL, Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 10))), Sym("psi")));
        c.put(UnitsPressure.DECIBAR, Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 13))), Sym("psi")));
        c.put(UnitsPressure.DECIPASCAL, Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 8))), Sym("psi")));
        c.put(UnitsPressure.EXAPASCAL, Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 27))), Sym("psi")));
        c.put(UnitsPressure.FEMTOPASCAL, Mul(Mul(Int("689475729316836"), Pow(10, 4)), Sym("psi")));
        c.put(UnitsPressure.GIGAPASCAL, Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 18))), Sym("psi")));
        c.put(UnitsPressure.HECTOPASCAL, Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 11))), Sym("psi")));
        c.put(UnitsPressure.KILOBAR, Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 17))), Sym("psi")));
        c.put(UnitsPressure.KILOPASCAL, Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 12))), Sym("psi")));
        c.put(UnitsPressure.MEGABAR, Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 20))), Sym("psi")));
        c.put(UnitsPressure.MEGAPASCAL, Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 15))), Sym("psi")));
        c.put(UnitsPressure.MICROPASCAL, Mul(Rat(Int("172368932329209"), Int(25000)), Sym("psi")));
        c.put(UnitsPressure.MILLIBAR, Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 11))), Sym("psi")));
        c.put(UnitsPressure.MILLIPASCAL, Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 6))), Sym("psi")));
        c.put(UnitsPressure.MILLITORR, Mul(Rat(Int("155952843535951"), Mul(Int(3015625), Pow(10, 9))), Sym("psi")));
        c.put(UnitsPressure.MMHG, Mul(Rat(Int("8208044396629"), Int("158717127875")), Sym("psi")));
        c.put(UnitsPressure.NANOPASCAL, Mul(Rat(Int("172368932329209"), Int(25)), Sym("psi")));
        c.put(UnitsPressure.PETAPASCAL, Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 24))), Sym("psi")));
        c.put(UnitsPressure.PICOPASCAL, Mul(Int("6894757293168360"), Sym("psi")));
        c.put(UnitsPressure.Pascal, Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 9))), Sym("psi")));
        c.put(UnitsPressure.TERAPASCAL, Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 21))), Sym("psi")));
        c.put(UnitsPressure.TORR, Mul(Rat(Int("155952843535951"), Mul(Int(3015625), Pow(10, 6))), Sym("psi")));
        c.put(UnitsPressure.YOCTOPASCAL, Mul(Mul(Int("689475729316836"), Pow(10, 13)), Sym("psi")));
        c.put(UnitsPressure.YOTTAPASCAL, Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 33))), Sym("psi")));
        c.put(UnitsPressure.ZEPTOPASCAL, Mul(Mul(Int("689475729316836"), Pow(10, 10)), Sym("psi")));
        c.put(UnitsPressure.ZETTAPASCAL, Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 30))), Sym("psi")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsPressure, Conversion> createMapPascal() {
        EnumMap<UnitsPressure, Conversion> c =
            new EnumMap<UnitsPressure, Conversion>(UnitsPressure.class);
        c.put(UnitsPressure.ATMOSPHERE, Mul(Rat(Int(1), Int(101325)), Sym("pa")));
        c.put(UnitsPressure.ATTOPASCAL, Mul(Pow(10, 18), Sym("pa")));
        c.put(UnitsPressure.BAR, Mul(Rat(Int(1), Pow(10, 5)), Sym("pa")));
        c.put(UnitsPressure.CENTIBAR, Mul(Rat(Int(1), Int(1000)), Sym("pa")));
        c.put(UnitsPressure.CENTIPASCAL, Mul(Int(100), Sym("pa")));
        c.put(UnitsPressure.DECAPASCAL, Mul(Rat(Int(1), Int(10)), Sym("pa")));
        c.put(UnitsPressure.DECIBAR, Mul(Rat(Int(1), Pow(10, 4)), Sym("pa")));
        c.put(UnitsPressure.DECIPASCAL, Mul(Int(10), Sym("pa")));
        c.put(UnitsPressure.EXAPASCAL, Mul(Rat(Int(1), Pow(10, 18)), Sym("pa")));
        c.put(UnitsPressure.FEMTOPASCAL, Mul(Pow(10, 15), Sym("pa")));
        c.put(UnitsPressure.GIGAPASCAL, Mul(Rat(Int(1), Pow(10, 9)), Sym("pa")));
        c.put(UnitsPressure.HECTOPASCAL, Mul(Rat(Int(1), Int(100)), Sym("pa")));
        c.put(UnitsPressure.KILOBAR, Mul(Rat(Int(1), Pow(10, 8)), Sym("pa")));
        c.put(UnitsPressure.KILOPASCAL, Mul(Rat(Int(1), Int(1000)), Sym("pa")));
        c.put(UnitsPressure.MEGABAR, Mul(Rat(Int(1), Pow(10, 11)), Sym("pa")));
        c.put(UnitsPressure.MEGAPASCAL, Mul(Rat(Int(1), Pow(10, 6)), Sym("pa")));
        c.put(UnitsPressure.MICROPASCAL, Mul(Pow(10, 6), Sym("pa")));
        c.put(UnitsPressure.MILLIBAR, Mul(Rat(Int(1), Int(100)), Sym("pa")));
        c.put(UnitsPressure.MILLIPASCAL, Mul(Int(1000), Sym("pa")));
        c.put(UnitsPressure.MILLITORR, Mul(Rat(Int(19), Int(2533125)), Sym("pa")));
        c.put(UnitsPressure.MMHG, Mul(Rat(Mul(Int(2), Pow(10, 8)), Int("26664477483")), Sym("pa")));
        c.put(UnitsPressure.NANOPASCAL, Mul(Pow(10, 9), Sym("pa")));
        c.put(UnitsPressure.PETAPASCAL, Mul(Rat(Int(1), Pow(10, 15)), Sym("pa")));
        c.put(UnitsPressure.PICOPASCAL, Mul(Pow(10, 12), Sym("pa")));
        c.put(UnitsPressure.PSI, Mul(Rat(Mul(Int(25), Pow(10, 9)), Int("172368932329209")), Sym("pa")));
        c.put(UnitsPressure.TERAPASCAL, Mul(Rat(Int(1), Pow(10, 12)), Sym("pa")));
        c.put(UnitsPressure.TORR, Mul(Rat(Int(152), Int(20265)), Sym("pa")));
        c.put(UnitsPressure.YOCTOPASCAL, Mul(Pow(10, 24), Sym("pa")));
        c.put(UnitsPressure.YOTTAPASCAL, Mul(Rat(Int(1), Pow(10, 24)), Sym("pa")));
        c.put(UnitsPressure.ZEPTOPASCAL, Mul(Pow(10, 21), Sym("pa")));
        c.put(UnitsPressure.ZETTAPASCAL, Mul(Rat(Int(1), Pow(10, 21)), Sym("pa")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsPressure, Conversion> createMapTERAPASCAL() {
        EnumMap<UnitsPressure, Conversion> c =
            new EnumMap<UnitsPressure, Conversion>(UnitsPressure.class);
        c.put(UnitsPressure.ATMOSPHERE, Mul(Rat(Mul(Int(4), Pow(10, 10)), Int(4053)), Sym("terapa")));
        c.put(UnitsPressure.ATTOPASCAL, Mul(Pow(10, 30), Sym("terapa")));
        c.put(UnitsPressure.BAR, Mul(Pow(10, 7), Sym("terapa")));
        c.put(UnitsPressure.CENTIBAR, Mul(Pow(10, 9), Sym("terapa")));
        c.put(UnitsPressure.CENTIPASCAL, Mul(Pow(10, 14), Sym("terapa")));
        c.put(UnitsPressure.DECAPASCAL, Mul(Pow(10, 11), Sym("terapa")));
        c.put(UnitsPressure.DECIBAR, Mul(Pow(10, 8), Sym("terapa")));
        c.put(UnitsPressure.DECIPASCAL, Mul(Pow(10, 13), Sym("terapa")));
        c.put(UnitsPressure.EXAPASCAL, Mul(Rat(Int(1), Pow(10, 6)), Sym("terapa")));
        c.put(UnitsPressure.FEMTOPASCAL, Mul(Pow(10, 27), Sym("terapa")));
        c.put(UnitsPressure.GIGAPASCAL, Mul(Int(1000), Sym("terapa")));
        c.put(UnitsPressure.HECTOPASCAL, Mul(Pow(10, 10), Sym("terapa")));
        c.put(UnitsPressure.KILOBAR, Mul(Pow(10, 4), Sym("terapa")));
        c.put(UnitsPressure.KILOPASCAL, Mul(Pow(10, 9), Sym("terapa")));
        c.put(UnitsPressure.MEGABAR, Mul(Int(10), Sym("terapa")));
        c.put(UnitsPressure.MEGAPASCAL, Mul(Pow(10, 6), Sym("terapa")));
        c.put(UnitsPressure.MICROPASCAL, Mul(Pow(10, 18), Sym("terapa")));
        c.put(UnitsPressure.MILLIBAR, Mul(Pow(10, 10), Sym("terapa")));
        c.put(UnitsPressure.MILLIPASCAL, Mul(Pow(10, 15), Sym("terapa")));
        c.put(UnitsPressure.MILLITORR, Mul(Rat(Mul(Int(304), Pow(10, 8)), Int(4053)), Sym("terapa")));
        c.put(UnitsPressure.MMHG, Mul(Rat(Mul(Int(2), Pow(10, 20)), Int("26664477483")), Sym("terapa")));
        c.put(UnitsPressure.NANOPASCAL, Mul(Pow(10, 21), Sym("terapa")));
        c.put(UnitsPressure.PETAPASCAL, Mul(Rat(Int(1), Int(1000)), Sym("terapa")));
        c.put(UnitsPressure.PICOPASCAL, Mul(Pow(10, 24), Sym("terapa")));
        c.put(UnitsPressure.PSI, Mul(Rat(Mul(Int(25), Pow(10, 21)), Int("172368932329209")), Sym("terapa")));
        c.put(UnitsPressure.Pascal, Mul(Pow(10, 12), Sym("terapa")));
        c.put(UnitsPressure.TORR, Mul(Rat(Mul(Int(304), Pow(10, 11)), Int(4053)), Sym("terapa")));
        c.put(UnitsPressure.YOCTOPASCAL, Mul(Pow(10, 36), Sym("terapa")));
        c.put(UnitsPressure.YOTTAPASCAL, Mul(Rat(Int(1), Pow(10, 12)), Sym("terapa")));
        c.put(UnitsPressure.ZEPTOPASCAL, Mul(Pow(10, 33), Sym("terapa")));
        c.put(UnitsPressure.ZETTAPASCAL, Mul(Rat(Int(1), Pow(10, 9)), Sym("terapa")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsPressure, Conversion> createMapTORR() {
        EnumMap<UnitsPressure, Conversion> c =
            new EnumMap<UnitsPressure, Conversion>(UnitsPressure.class);
        c.put(UnitsPressure.ATMOSPHERE, Mul(Rat(Int(1), Int(760)), Sym("torr")));
        c.put(UnitsPressure.ATTOPASCAL, Mul(Rat(Mul(Int(2533125), Pow(10, 15)), Int(19)), Sym("torr")));
        c.put(UnitsPressure.BAR, Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 4))), Sym("torr")));
        c.put(UnitsPressure.CENTIBAR, Mul(Rat(Int(4053), Int(30400)), Sym("torr")));
        c.put(UnitsPressure.CENTIPASCAL, Mul(Rat(Int(506625), Int(38)), Sym("torr")));
        c.put(UnitsPressure.DECAPASCAL, Mul(Rat(Int(4053), Int(304)), Sym("torr")));
        c.put(UnitsPressure.DECIBAR, Mul(Rat(Int(4053), Int(304000)), Sym("torr")));
        c.put(UnitsPressure.DECIPASCAL, Mul(Rat(Int(101325), Int(76)), Sym("torr")));
        c.put(UnitsPressure.EXAPASCAL, Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 17))), Sym("torr")));
        c.put(UnitsPressure.FEMTOPASCAL, Mul(Rat(Mul(Int(2533125), Pow(10, 12)), Int(19)), Sym("torr")));
        c.put(UnitsPressure.GIGAPASCAL, Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 8))), Sym("torr")));
        c.put(UnitsPressure.HECTOPASCAL, Mul(Rat(Int(4053), Int(3040)), Sym("torr")));
        c.put(UnitsPressure.KILOBAR, Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 7))), Sym("torr")));
        c.put(UnitsPressure.KILOPASCAL, Mul(Rat(Int(4053), Int(30400)), Sym("torr")));
        c.put(UnitsPressure.MEGABAR, Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 10))), Sym("torr")));
        c.put(UnitsPressure.MEGAPASCAL, Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 5))), Sym("torr")));
        c.put(UnitsPressure.MICROPASCAL, Mul(Rat(Int("2533125000"), Int(19)), Sym("torr")));
        c.put(UnitsPressure.MILLIBAR, Mul(Rat(Int(4053), Int(3040)), Sym("torr")));
        c.put(UnitsPressure.MILLIPASCAL, Mul(Rat(Int(2533125), Int(19)), Sym("torr")));
        c.put(UnitsPressure.MILLITORR, Mul(Rat(Int(1), Int(1000)), Sym("torr")));
        c.put(UnitsPressure.MMHG, Mul(Rat(Mul(Int(24125), Pow(10, 6)), Int("24125003437")), Sym("torr")));
        c.put(UnitsPressure.NANOPASCAL, Mul(Rat(Mul(Int(2533125), Pow(10, 6)), Int(19)), Sym("torr")));
        c.put(UnitsPressure.PETAPASCAL, Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 14))), Sym("torr")));
        c.put(UnitsPressure.PICOPASCAL, Mul(Rat(Mul(Int(2533125), Pow(10, 9)), Int(19)), Sym("torr")));
        c.put(UnitsPressure.PSI, Mul(Rat(Mul(Int(3015625), Pow(10, 6)), Int("155952843535951")), Sym("torr")));
        c.put(UnitsPressure.Pascal, Mul(Rat(Int(20265), Int(152)), Sym("torr")));
        c.put(UnitsPressure.TERAPASCAL, Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 11))), Sym("torr")));
        c.put(UnitsPressure.YOCTOPASCAL, Mul(Rat(Mul(Int(2533125), Pow(10, 21)), Int(19)), Sym("torr")));
        c.put(UnitsPressure.YOTTAPASCAL, Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 23))), Sym("torr")));
        c.put(UnitsPressure.ZEPTOPASCAL, Mul(Rat(Mul(Int(2533125), Pow(10, 18)), Int(19)), Sym("torr")));
        c.put(UnitsPressure.ZETTAPASCAL, Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 20))), Sym("torr")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsPressure, Conversion> createMapYOCTOPASCAL() {
        EnumMap<UnitsPressure, Conversion> c =
            new EnumMap<UnitsPressure, Conversion>(UnitsPressure.class);
        c.put(UnitsPressure.ATMOSPHERE, Mul(Rat(Int(1), Mul(Int(101325), Pow(10, 24))), Sym("yoctopa")));
        c.put(UnitsPressure.ATTOPASCAL, Mul(Rat(Int(1), Pow(10, 6)), Sym("yoctopa")));
        c.put(UnitsPressure.BAR, Mul(Rat(Int(1), Pow(10, 29)), Sym("yoctopa")));
        c.put(UnitsPressure.CENTIBAR, Mul(Rat(Int(1), Pow(10, 27)), Sym("yoctopa")));
        c.put(UnitsPressure.CENTIPASCAL, Mul(Rat(Int(1), Pow(10, 22)), Sym("yoctopa")));
        c.put(UnitsPressure.DECAPASCAL, Mul(Rat(Int(1), Pow(10, 25)), Sym("yoctopa")));
        c.put(UnitsPressure.DECIBAR, Mul(Rat(Int(1), Pow(10, 28)), Sym("yoctopa")));
        c.put(UnitsPressure.DECIPASCAL, Mul(Rat(Int(1), Pow(10, 23)), Sym("yoctopa")));
        c.put(UnitsPressure.EXAPASCAL, Mul(Rat(Int(1), Pow(10, 42)), Sym("yoctopa")));
        c.put(UnitsPressure.FEMTOPASCAL, Mul(Rat(Int(1), Pow(10, 9)), Sym("yoctopa")));
        c.put(UnitsPressure.GIGAPASCAL, Mul(Rat(Int(1), Pow(10, 33)), Sym("yoctopa")));
        c.put(UnitsPressure.HECTOPASCAL, Mul(Rat(Int(1), Pow(10, 26)), Sym("yoctopa")));
        c.put(UnitsPressure.KILOBAR, Mul(Rat(Int(1), Pow(10, 32)), Sym("yoctopa")));
        c.put(UnitsPressure.KILOPASCAL, Mul(Rat(Int(1), Pow(10, 27)), Sym("yoctopa")));
        c.put(UnitsPressure.MEGABAR, Mul(Rat(Int(1), Pow(10, 35)), Sym("yoctopa")));
        c.put(UnitsPressure.MEGAPASCAL, Mul(Rat(Int(1), Pow(10, 30)), Sym("yoctopa")));
        c.put(UnitsPressure.MICROPASCAL, Mul(Rat(Int(1), Pow(10, 18)), Sym("yoctopa")));
        c.put(UnitsPressure.MILLIBAR, Mul(Rat(Int(1), Pow(10, 26)), Sym("yoctopa")));
        c.put(UnitsPressure.MILLIPASCAL, Mul(Rat(Int(1), Pow(10, 21)), Sym("yoctopa")));
        c.put(UnitsPressure.MILLITORR, Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 24))), Sym("yoctopa")));
        c.put(UnitsPressure.MMHG, Mul(Rat(Int(1), Mul(Int("133322387415"), Pow(10, 15))), Sym("yoctopa")));
        c.put(UnitsPressure.NANOPASCAL, Mul(Rat(Int(1), Pow(10, 15)), Sym("yoctopa")));
        c.put(UnitsPressure.PETAPASCAL, Mul(Rat(Int(1), Pow(10, 39)), Sym("yoctopa")));
        c.put(UnitsPressure.PICOPASCAL, Mul(Rat(Int(1), Pow(10, 12)), Sym("yoctopa")));
        c.put(UnitsPressure.PSI, Mul(Rat(Int(1), Mul(Int("689475729316836"), Pow(10, 13))), Sym("yoctopa")));
        c.put(UnitsPressure.Pascal, Mul(Rat(Int(1), Pow(10, 24)), Sym("yoctopa")));
        c.put(UnitsPressure.TERAPASCAL, Mul(Rat(Int(1), Pow(10, 36)), Sym("yoctopa")));
        c.put(UnitsPressure.TORR, Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 21))), Sym("yoctopa")));
        c.put(UnitsPressure.YOTTAPASCAL, Mul(Rat(Int(1), Pow(10, 48)), Sym("yoctopa")));
        c.put(UnitsPressure.ZEPTOPASCAL, Mul(Rat(Int(1), Int(1000)), Sym("yoctopa")));
        c.put(UnitsPressure.ZETTAPASCAL, Mul(Rat(Int(1), Pow(10, 45)), Sym("yoctopa")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsPressure, Conversion> createMapYOTTAPASCAL() {
        EnumMap<UnitsPressure, Conversion> c =
            new EnumMap<UnitsPressure, Conversion>(UnitsPressure.class);
        c.put(UnitsPressure.ATMOSPHERE, Mul(Rat(Mul(Int(4), Pow(10, 22)), Int(4053)), Sym("yottapa")));
        c.put(UnitsPressure.ATTOPASCAL, Mul(Pow(10, 42), Sym("yottapa")));
        c.put(UnitsPressure.BAR, Mul(Pow(10, 19), Sym("yottapa")));
        c.put(UnitsPressure.CENTIBAR, Mul(Pow(10, 21), Sym("yottapa")));
        c.put(UnitsPressure.CENTIPASCAL, Mul(Pow(10, 26), Sym("yottapa")));
        c.put(UnitsPressure.DECAPASCAL, Mul(Pow(10, 23), Sym("yottapa")));
        c.put(UnitsPressure.DECIBAR, Mul(Pow(10, 20), Sym("yottapa")));
        c.put(UnitsPressure.DECIPASCAL, Mul(Pow(10, 25), Sym("yottapa")));
        c.put(UnitsPressure.EXAPASCAL, Mul(Pow(10, 6), Sym("yottapa")));
        c.put(UnitsPressure.FEMTOPASCAL, Mul(Pow(10, 39), Sym("yottapa")));
        c.put(UnitsPressure.GIGAPASCAL, Mul(Pow(10, 15), Sym("yottapa")));
        c.put(UnitsPressure.HECTOPASCAL, Mul(Pow(10, 22), Sym("yottapa")));
        c.put(UnitsPressure.KILOBAR, Mul(Pow(10, 16), Sym("yottapa")));
        c.put(UnitsPressure.KILOPASCAL, Mul(Pow(10, 21), Sym("yottapa")));
        c.put(UnitsPressure.MEGABAR, Mul(Pow(10, 13), Sym("yottapa")));
        c.put(UnitsPressure.MEGAPASCAL, Mul(Pow(10, 18), Sym("yottapa")));
        c.put(UnitsPressure.MICROPASCAL, Mul(Pow(10, 30), Sym("yottapa")));
        c.put(UnitsPressure.MILLIBAR, Mul(Pow(10, 22), Sym("yottapa")));
        c.put(UnitsPressure.MILLIPASCAL, Mul(Pow(10, 27), Sym("yottapa")));
        c.put(UnitsPressure.MILLITORR, Mul(Rat(Mul(Int(304), Pow(10, 20)), Int(4053)), Sym("yottapa")));
        c.put(UnitsPressure.MMHG, Mul(Rat(Mul(Int(2), Pow(10, 32)), Int("26664477483")), Sym("yottapa")));
        c.put(UnitsPressure.NANOPASCAL, Mul(Pow(10, 33), Sym("yottapa")));
        c.put(UnitsPressure.PETAPASCAL, Mul(Pow(10, 9), Sym("yottapa")));
        c.put(UnitsPressure.PICOPASCAL, Mul(Pow(10, 36), Sym("yottapa")));
        c.put(UnitsPressure.PSI, Mul(Rat(Mul(Int(25), Pow(10, 33)), Int("172368932329209")), Sym("yottapa")));
        c.put(UnitsPressure.Pascal, Mul(Pow(10, 24), Sym("yottapa")));
        c.put(UnitsPressure.TERAPASCAL, Mul(Pow(10, 12), Sym("yottapa")));
        c.put(UnitsPressure.TORR, Mul(Rat(Mul(Int(304), Pow(10, 23)), Int(4053)), Sym("yottapa")));
        c.put(UnitsPressure.YOCTOPASCAL, Mul(Pow(10, 48), Sym("yottapa")));
        c.put(UnitsPressure.ZEPTOPASCAL, Mul(Pow(10, 45), Sym("yottapa")));
        c.put(UnitsPressure.ZETTAPASCAL, Mul(Int(1000), Sym("yottapa")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsPressure, Conversion> createMapZEPTOPASCAL() {
        EnumMap<UnitsPressure, Conversion> c =
            new EnumMap<UnitsPressure, Conversion>(UnitsPressure.class);
        c.put(UnitsPressure.ATMOSPHERE, Mul(Rat(Int(1), Mul(Int(101325), Pow(10, 21))), Sym("zeptopa")));
        c.put(UnitsPressure.ATTOPASCAL, Mul(Rat(Int(1), Int(1000)), Sym("zeptopa")));
        c.put(UnitsPressure.BAR, Mul(Rat(Int(1), Pow(10, 26)), Sym("zeptopa")));
        c.put(UnitsPressure.CENTIBAR, Mul(Rat(Int(1), Pow(10, 24)), Sym("zeptopa")));
        c.put(UnitsPressure.CENTIPASCAL, Mul(Rat(Int(1), Pow(10, 19)), Sym("zeptopa")));
        c.put(UnitsPressure.DECAPASCAL, Mul(Rat(Int(1), Pow(10, 22)), Sym("zeptopa")));
        c.put(UnitsPressure.DECIBAR, Mul(Rat(Int(1), Pow(10, 25)), Sym("zeptopa")));
        c.put(UnitsPressure.DECIPASCAL, Mul(Rat(Int(1), Pow(10, 20)), Sym("zeptopa")));
        c.put(UnitsPressure.EXAPASCAL, Mul(Rat(Int(1), Pow(10, 39)), Sym("zeptopa")));
        c.put(UnitsPressure.FEMTOPASCAL, Mul(Rat(Int(1), Pow(10, 6)), Sym("zeptopa")));
        c.put(UnitsPressure.GIGAPASCAL, Mul(Rat(Int(1), Pow(10, 30)), Sym("zeptopa")));
        c.put(UnitsPressure.HECTOPASCAL, Mul(Rat(Int(1), Pow(10, 23)), Sym("zeptopa")));
        c.put(UnitsPressure.KILOBAR, Mul(Rat(Int(1), Pow(10, 29)), Sym("zeptopa")));
        c.put(UnitsPressure.KILOPASCAL, Mul(Rat(Int(1), Pow(10, 24)), Sym("zeptopa")));
        c.put(UnitsPressure.MEGABAR, Mul(Rat(Int(1), Pow(10, 32)), Sym("zeptopa")));
        c.put(UnitsPressure.MEGAPASCAL, Mul(Rat(Int(1), Pow(10, 27)), Sym("zeptopa")));
        c.put(UnitsPressure.MICROPASCAL, Mul(Rat(Int(1), Pow(10, 15)), Sym("zeptopa")));
        c.put(UnitsPressure.MILLIBAR, Mul(Rat(Int(1), Pow(10, 23)), Sym("zeptopa")));
        c.put(UnitsPressure.MILLIPASCAL, Mul(Rat(Int(1), Pow(10, 18)), Sym("zeptopa")));
        c.put(UnitsPressure.MILLITORR, Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 21))), Sym("zeptopa")));
        c.put(UnitsPressure.MMHG, Mul(Rat(Int(1), Mul(Int("133322387415"), Pow(10, 12))), Sym("zeptopa")));
        c.put(UnitsPressure.NANOPASCAL, Mul(Rat(Int(1), Pow(10, 12)), Sym("zeptopa")));
        c.put(UnitsPressure.PETAPASCAL, Mul(Rat(Int(1), Pow(10, 36)), Sym("zeptopa")));
        c.put(UnitsPressure.PICOPASCAL, Mul(Rat(Int(1), Pow(10, 9)), Sym("zeptopa")));
        c.put(UnitsPressure.PSI, Mul(Rat(Int(1), Mul(Int("689475729316836"), Pow(10, 10))), Sym("zeptopa")));
        c.put(UnitsPressure.Pascal, Mul(Rat(Int(1), Pow(10, 21)), Sym("zeptopa")));
        c.put(UnitsPressure.TERAPASCAL, Mul(Rat(Int(1), Pow(10, 33)), Sym("zeptopa")));
        c.put(UnitsPressure.TORR, Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 18))), Sym("zeptopa")));
        c.put(UnitsPressure.YOCTOPASCAL, Mul(Int(1000), Sym("zeptopa")));
        c.put(UnitsPressure.YOTTAPASCAL, Mul(Rat(Int(1), Pow(10, 45)), Sym("zeptopa")));
        c.put(UnitsPressure.ZETTAPASCAL, Mul(Rat(Int(1), Pow(10, 42)), Sym("zeptopa")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsPressure, Conversion> createMapZETTAPASCAL() {
        EnumMap<UnitsPressure, Conversion> c =
            new EnumMap<UnitsPressure, Conversion>(UnitsPressure.class);
        c.put(UnitsPressure.ATMOSPHERE, Mul(Rat(Mul(Int(4), Pow(10, 19)), Int(4053)), Sym("zettapa")));
        c.put(UnitsPressure.ATTOPASCAL, Mul(Pow(10, 39), Sym("zettapa")));
        c.put(UnitsPressure.BAR, Mul(Pow(10, 16), Sym("zettapa")));
        c.put(UnitsPressure.CENTIBAR, Mul(Pow(10, 18), Sym("zettapa")));
        c.put(UnitsPressure.CENTIPASCAL, Mul(Pow(10, 23), Sym("zettapa")));
        c.put(UnitsPressure.DECAPASCAL, Mul(Pow(10, 20), Sym("zettapa")));
        c.put(UnitsPressure.DECIBAR, Mul(Pow(10, 17), Sym("zettapa")));
        c.put(UnitsPressure.DECIPASCAL, Mul(Pow(10, 22), Sym("zettapa")));
        c.put(UnitsPressure.EXAPASCAL, Mul(Int(1000), Sym("zettapa")));
        c.put(UnitsPressure.FEMTOPASCAL, Mul(Pow(10, 36), Sym("zettapa")));
        c.put(UnitsPressure.GIGAPASCAL, Mul(Pow(10, 12), Sym("zettapa")));
        c.put(UnitsPressure.HECTOPASCAL, Mul(Pow(10, 19), Sym("zettapa")));
        c.put(UnitsPressure.KILOBAR, Mul(Pow(10, 13), Sym("zettapa")));
        c.put(UnitsPressure.KILOPASCAL, Mul(Pow(10, 18), Sym("zettapa")));
        c.put(UnitsPressure.MEGABAR, Mul(Pow(10, 10), Sym("zettapa")));
        c.put(UnitsPressure.MEGAPASCAL, Mul(Pow(10, 15), Sym("zettapa")));
        c.put(UnitsPressure.MICROPASCAL, Mul(Pow(10, 27), Sym("zettapa")));
        c.put(UnitsPressure.MILLIBAR, Mul(Pow(10, 19), Sym("zettapa")));
        c.put(UnitsPressure.MILLIPASCAL, Mul(Pow(10, 24), Sym("zettapa")));
        c.put(UnitsPressure.MILLITORR, Mul(Rat(Mul(Int(304), Pow(10, 17)), Int(4053)), Sym("zettapa")));
        c.put(UnitsPressure.MMHG, Mul(Rat(Mul(Int(2), Pow(10, 29)), Int("26664477483")), Sym("zettapa")));
        c.put(UnitsPressure.NANOPASCAL, Mul(Pow(10, 30), Sym("zettapa")));
        c.put(UnitsPressure.PETAPASCAL, Mul(Pow(10, 6), Sym("zettapa")));
        c.put(UnitsPressure.PICOPASCAL, Mul(Pow(10, 33), Sym("zettapa")));
        c.put(UnitsPressure.PSI, Mul(Rat(Mul(Int(25), Pow(10, 30)), Int("172368932329209")), Sym("zettapa")));
        c.put(UnitsPressure.Pascal, Mul(Pow(10, 21), Sym("zettapa")));
        c.put(UnitsPressure.TERAPASCAL, Mul(Pow(10, 9), Sym("zettapa")));
        c.put(UnitsPressure.TORR, Mul(Rat(Mul(Int(304), Pow(10, 20)), Int(4053)), Sym("zettapa")));
        c.put(UnitsPressure.YOCTOPASCAL, Mul(Pow(10, 45), Sym("zettapa")));
        c.put(UnitsPressure.YOTTAPASCAL, Mul(Rat(Int(1), Int(1000)), Sym("zettapa")));
        c.put(UnitsPressure.ZEPTOPASCAL, Mul(Pow(10, 42), Sym("zettapa")));
        return Collections.unmodifiableMap(c);
    }

    private static final Map<UnitsPressure, Map<UnitsPressure, Conversion>> conversions;
    static {

        Map<UnitsPressure, Map<UnitsPressure, Conversion>> c
            = new EnumMap<UnitsPressure, Map<UnitsPressure, Conversion>>(UnitsPressure.class);

        c.put(UnitsPressure.ATMOSPHERE, createMapATMOSPHERE());
        c.put(UnitsPressure.ATTOPASCAL, createMapATTOPASCAL());
        c.put(UnitsPressure.BAR, createMapBAR());
        c.put(UnitsPressure.CENTIBAR, createMapCENTIBAR());
        c.put(UnitsPressure.CENTIPASCAL, createMapCENTIPASCAL());
        c.put(UnitsPressure.DECAPASCAL, createMapDECAPASCAL());
        c.put(UnitsPressure.DECIBAR, createMapDECIBAR());
        c.put(UnitsPressure.DECIPASCAL, createMapDECIPASCAL());
        c.put(UnitsPressure.EXAPASCAL, createMapEXAPASCAL());
        c.put(UnitsPressure.FEMTOPASCAL, createMapFEMTOPASCAL());
        c.put(UnitsPressure.GIGAPASCAL, createMapGIGAPASCAL());
        c.put(UnitsPressure.HECTOPASCAL, createMapHECTOPASCAL());
        c.put(UnitsPressure.KILOBAR, createMapKILOBAR());
        c.put(UnitsPressure.KILOPASCAL, createMapKILOPASCAL());
        c.put(UnitsPressure.MEGABAR, createMapMEGABAR());
        c.put(UnitsPressure.MEGAPASCAL, createMapMEGAPASCAL());
        c.put(UnitsPressure.MICROPASCAL, createMapMICROPASCAL());
        c.put(UnitsPressure.MILLIBAR, createMapMILLIBAR());
        c.put(UnitsPressure.MILLIPASCAL, createMapMILLIPASCAL());
        c.put(UnitsPressure.MILLITORR, createMapMILLITORR());
        c.put(UnitsPressure.MMHG, createMapMMHG());
        c.put(UnitsPressure.NANOPASCAL, createMapNANOPASCAL());
        c.put(UnitsPressure.PETAPASCAL, createMapPETAPASCAL());
        c.put(UnitsPressure.PICOPASCAL, createMapPICOPASCAL());
        c.put(UnitsPressure.PSI, createMapPSI());
        c.put(UnitsPressure.Pascal, createMapPascal());
        c.put(UnitsPressure.TERAPASCAL, createMapTERAPASCAL());
        c.put(UnitsPressure.TORR, createMapTORR());
        c.put(UnitsPressure.YOCTOPASCAL, createMapYOCTOPASCAL());
        c.put(UnitsPressure.YOTTAPASCAL, createMapYOTTAPASCAL());
        c.put(UnitsPressure.ZEPTOPASCAL, createMapZEPTOPASCAL());
        c.put(UnitsPressure.ZETTAPASCAL, createMapZETTAPASCAL());
        conversions = Collections.unmodifiableMap(c);
    }

    private static final Map<UnitsPressure, String> SYMBOLS;
    static {
        Map<UnitsPressure, String> s = new HashMap<UnitsPressure, String>();
        s.put(UnitsPressure.ATMOSPHERE, "atm");
        s.put(UnitsPressure.ATTOPASCAL, "aPa");
        s.put(UnitsPressure.BAR, "bar");
        s.put(UnitsPressure.CENTIBAR, "cbar");
        s.put(UnitsPressure.CENTIPASCAL, "cPa");
        s.put(UnitsPressure.DECAPASCAL, "daPa");
        s.put(UnitsPressure.DECIBAR, "dbar");
        s.put(UnitsPressure.DECIPASCAL, "dPa");
        s.put(UnitsPressure.EXAPASCAL, "EPa");
        s.put(UnitsPressure.FEMTOPASCAL, "fPa");
        s.put(UnitsPressure.GIGAPASCAL, "GPa");
        s.put(UnitsPressure.HECTOPASCAL, "hPa");
        s.put(UnitsPressure.KILOBAR, "kbar");
        s.put(UnitsPressure.KILOPASCAL, "kPa");
        s.put(UnitsPressure.MEGABAR, "Mbar");
        s.put(UnitsPressure.MEGAPASCAL, "MPa");
        s.put(UnitsPressure.MICROPASCAL, "µPa");
        s.put(UnitsPressure.MILLIBAR, "mbar");
        s.put(UnitsPressure.MILLIPASCAL, "mPa");
        s.put(UnitsPressure.MILLITORR, "mTorr");
        s.put(UnitsPressure.MMHG, "mm Hg");
        s.put(UnitsPressure.NANOPASCAL, "nPa");
        s.put(UnitsPressure.PETAPASCAL, "PPa");
        s.put(UnitsPressure.PICOPASCAL, "pPa");
        s.put(UnitsPressure.PSI, "psi");
        s.put(UnitsPressure.Pascal, "Pa");
        s.put(UnitsPressure.TERAPASCAL, "TPa");
        s.put(UnitsPressure.TORR, "Torr");
        s.put(UnitsPressure.YOCTOPASCAL, "yPa");
        s.put(UnitsPressure.YOTTAPASCAL, "YPa");
        s.put(UnitsPressure.ZEPTOPASCAL, "zPa");
        s.put(UnitsPressure.ZETTAPASCAL, "ZPa");
        SYMBOLS = s;
    }

    public static String lookupSymbol(UnitsPressure unit) {
        return SYMBOLS.get(unit);
    }

    public static final Ice.ObjectFactory makeFactory(final omero.client client) {

        return new Ice.ObjectFactory() {

            public Ice.Object create(String arg0) {
                return new PressureI();
            }

            public void destroy() {
                // no-op
            }

        };
    };

    //
    // CONVERSIONS
    //

    public static ome.xml.model.enums.UnitsPressure makeXMLUnit(String unit) {
        try {
            return ome.xml.model.enums.UnitsPressure
                    .fromString((String) unit);
        } catch (EnumerationException e) {
            throw new RuntimeException("Bad Pressure unit: " + unit, e);
        }
    }

    public static ome.units.quantity.Pressure makeXMLQuantity(double d, String unit) {
        ome.units.unit.Unit<ome.units.quantity.Pressure> units =
                ome.xml.model.enums.handlers.UnitsPressureEnumHandler
                        .getBaseUnit(makeXMLUnit(unit));
        return new ome.units.quantity.Pressure(d, units);
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
   public static ome.units.quantity.Pressure convert(Pressure t) {
       if (t == null) {
           return null;
       }

       Double v = t.getValue();
       // Use the code/symbol-mapping in the ome.model.enums files
       // to convert to the specification value.
       String u = ome.model.enums.UnitsPressure.valueOf(
               t.getUnit().toString()).getSymbol();
       ome.xml.model.enums.UnitsPressure units = makeXMLUnit(u);
       ome.units.unit.Unit<ome.units.quantity.Pressure> units2 =
               ome.xml.model.enums.handlers.UnitsPressureEnumHandler
                       .getBaseUnit(units);

       return new ome.units.quantity.Pressure(v, units2);
   }


    //
    // REGULAR ICE CLASS
    //

    public final static Ice.ObjectFactory Factory = makeFactory(null);

    public PressureI() {
        super();
    }

    public PressureI(double d, UnitsPressure unit) {
        super();
        this.setUnit(unit);
        this.setValue(d);
    }

    public PressureI(double d,
            Unit<ome.units.quantity.Pressure> unit) {
        this(d, ome.model.enums.UnitsPressure.bySymbol(unit.getSymbol()));
    }

   /**
    * Copy constructor that converts the given {@link omero.model.Pressure}
    * based on the given ome-xml enum
    */
   public PressureI(Pressure value, Unit<ome.units.quantity.Pressure> ul) throws BigResult {
       this(value,
            ome.model.enums.UnitsPressure.bySymbol(ul.getSymbol()).toString());
   }

   /**
    * Copy constructor that converts the given {@link omero.model.Pressure}
    * based on the given ome.model enum
    */
   public PressureI(double d, ome.model.enums.UnitsPressure ul) {
        this(d, UnitsPressure.valueOf(ul.toString()));
    }

   /**
    * Copy constructor that converts the given {@link omero.model.Pressure}
    * based on the given enum string.
    *
    * @param target String representation of the CODE enum
    */
    public PressureI(Pressure value, String target) throws BigResult {
       String source = value.getUnit().toString();
       if (target.equals(source)) {
           setValue(value.getValue());
           setUnit(value.getUnit());
        } else {
            UnitsPressure targetUnit = UnitsPressure.valueOf(target);
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
    public PressureI(Pressure value, UnitsPressure target) throws BigResult {
        this(value, target.toString());
    }

    /**
     * Convert a Bio-Formats {@link Length} to an OMERO Length.
     */
    public PressureI(ome.units.quantity.Pressure value) {
        ome.model.enums.UnitsPressure internal =
            ome.model.enums.UnitsPressure.bySymbol(value.unit().getSymbol());
        UnitsPressure ul = UnitsPressure.valueOf(internal.toString());
        setValue(value.value().doubleValue());
        setUnit(ul);
    }

    public double getValue(Ice.Current current) {
        return this.value;
    }

    public void setValue(double value , Ice.Current current) {
        this.value = value;
    }

    public UnitsPressure getUnit(Ice.Current current) {
        return this.unit;
    }

    public void setUnit(UnitsPressure unit, Ice.Current current) {
        this.unit = unit;
    }

    public String getSymbol(Ice.Current current) {
        return SYMBOLS.get(this.unit);
    }

    public Pressure copy(Ice.Current ignore) {
        PressureI copy = new PressureI();
        copy.setValue(getValue());
        copy.setUnit(getUnit());
        return copy;
    }

    @Override
    public void copyObject(Filterable model, ModelMapper mapper) {
        if (model instanceof ome.model.units.Pressure) {
            ome.model.units.Pressure t = (ome.model.units.Pressure) model;
            this.value = t.getValue();
            this.unit = UnitsPressure.valueOf(t.getUnit().toString());
        } else {
            throw new IllegalArgumentException(
              "Pressure cannot copy from " +
              (model==null ? "null" : model.getClass().getName()));
        }
    }

    @Override
    public Filterable fillObject(ReverseModelMapper mapper) {
        ome.model.enums.UnitsPressure ut = ome.model.enums.UnitsPressure.valueOf(getUnit().toString());
        ome.model.units.Pressure t = new ome.model.units.Pressure(getValue(), ut);
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
        return "Pressure(" + value + " " + unit + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Pressure other = (Pressure) obj;
        if (unit != other.unit)
            return false;
        if (Double.doubleToLongBits(value) != Double
                .doubleToLongBits(other.value))
            return false;
        return true;
    }

}

