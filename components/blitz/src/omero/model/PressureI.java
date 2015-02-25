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

    private static final Map<UnitsPressure, Map<UnitsPressure, Conversion>> conversions;
    static {

        EnumMap<UnitsPressure, EnumMap<UnitsPressure, Conversion>> c
            = new EnumMap<UnitsPressure, EnumMap<UnitsPressure, Conversion>>(UnitsPressure.class);

        for (UnitsPressure e : UnitsPressure.values()) {
            c.put(e, new EnumMap<UnitsPressure, Conversion>(UnitsPressure.class));
        }

        c.get(UnitsPressure.ATHMOSPHERE).put(UnitsPressure.ATTOPASCAL, Mul(Rat(Int(1), Mul(Int(101325), Pow(10, 18))), Sym("atm")));
        c.get(UnitsPressure.ATHMOSPHERE).put(UnitsPressure.BAR, Mul(Rat(Int(4000), Int(4053)), Sym("atm")));
        c.get(UnitsPressure.ATHMOSPHERE).put(UnitsPressure.CENTIBAR, Mul(Rat(Int(40), Int(4053)), Sym("atm")));
        c.get(UnitsPressure.ATHMOSPHERE).put(UnitsPressure.CENTIPASCAL, Mul(Rat(Int(1), Int(10132500)), Sym("atm")));
        c.get(UnitsPressure.ATHMOSPHERE).put(UnitsPressure.DECAPASCAL, Mul(Rat(Int(2), Int(20265)), Sym("atm")));
        c.get(UnitsPressure.ATHMOSPHERE).put(UnitsPressure.DECIBAR, Mul(Rat(Int(400), Int(4053)), Sym("atm")));
        c.get(UnitsPressure.ATHMOSPHERE).put(UnitsPressure.DECIPASCAL, Mul(Rat(Int(1), Int(1013250)), Sym("atm")));
        c.get(UnitsPressure.ATHMOSPHERE).put(UnitsPressure.EXAPASCAL, Mul(Rat(Mul(Int(4), Pow(10, 16)), Int(4053)), Sym("atm")));
        c.get(UnitsPressure.ATHMOSPHERE).put(UnitsPressure.FEMTOPASCAL, Mul(Rat(Int(1), Mul(Int(101325), Pow(10, 15))), Sym("atm")));
        c.get(UnitsPressure.ATHMOSPHERE).put(UnitsPressure.GIGAPASCAL, Mul(Rat(Mul(Int(4), Pow(10, 7)), Int(4053)), Sym("atm")));
        c.get(UnitsPressure.ATHMOSPHERE).put(UnitsPressure.HECTOPASCAL, Mul(Rat(Int(4), Int(4053)), Sym("atm")));
        c.get(UnitsPressure.ATHMOSPHERE).put(UnitsPressure.KILOBAR, Mul(Rat(Mul(Int(4), Pow(10, 6)), Int(4053)), Sym("atm")));
        c.get(UnitsPressure.ATHMOSPHERE).put(UnitsPressure.KILOPASCAL, Mul(Rat(Int(40), Int(4053)), Sym("atm")));
        c.get(UnitsPressure.ATHMOSPHERE).put(UnitsPressure.MEGABAR, Mul(Rat(Mul(Int(4), Pow(10, 9)), Int(4053)), Sym("atm")));
        c.get(UnitsPressure.ATHMOSPHERE).put(UnitsPressure.MEGAPASCAL, Mul(Rat(Mul(Int(4), Pow(10, 4)), Int(4053)), Sym("atm")));
        c.get(UnitsPressure.ATHMOSPHERE).put(UnitsPressure.MICROPASCAL, Mul(Rat(Int(1), Mul(Int(101325), Pow(10, 6))), Sym("atm")));
        c.get(UnitsPressure.ATHMOSPHERE).put(UnitsPressure.MILLIBAR, Mul(Rat(Int(4), Int(4053)), Sym("atm")));
        c.get(UnitsPressure.ATHMOSPHERE).put(UnitsPressure.MILLIPASCAL, Mul(Rat(Int(1), Int(101325000)), Sym("atm")));
        c.get(UnitsPressure.ATHMOSPHERE).put(UnitsPressure.MILLITORR, Mul(Rat(Int(1), Mul(Int(76), Pow(10, 4))), Sym("atm")));
        c.get(UnitsPressure.ATHMOSPHERE).put(UnitsPressure.MMHG, Mul(Rat(Int(1269737023), Mul(Int(965), Pow(10, 9))), Sym("atm")));
        c.get(UnitsPressure.ATHMOSPHERE).put(UnitsPressure.NANOPASCAL, Mul(Rat(Int(1), Mul(Int(101325), Pow(10, 9))), Sym("atm")));
        c.get(UnitsPressure.ATHMOSPHERE).put(UnitsPressure.PASCAL, Mul(Rat(Int(1), Int(101325)), Sym("atm")));
        c.get(UnitsPressure.ATHMOSPHERE).put(UnitsPressure.PETAPASCAL, Mul(Rat(Mul(Int(4), Pow(10, 13)), Int(4053)), Sym("atm")));
        c.get(UnitsPressure.ATHMOSPHERE).put(UnitsPressure.PICOPASCAL, Mul(Rat(Int(1), Mul(Int(101325), Pow(10, 12))), Sym("atm")));
        c.get(UnitsPressure.ATHMOSPHERE).put(UnitsPressure.PSI, Mul(Rat(Int("8208044396629"), Mul(Int(120625), Pow(10, 9))), Sym("atm")));
        c.get(UnitsPressure.ATHMOSPHERE).put(UnitsPressure.TERAPASCAL, Mul(Rat(Mul(Int(4), Pow(10, 10)), Int(4053)), Sym("atm")));
        c.get(UnitsPressure.ATHMOSPHERE).put(UnitsPressure.TORR, Mul(Rat(Int(1), Int(760)), Sym("atm")));
        c.get(UnitsPressure.ATHMOSPHERE).put(UnitsPressure.YOCTOPASCAL, Mul(Rat(Int(1), Mul(Int(101325), Pow(10, 24))), Sym("atm")));
        c.get(UnitsPressure.ATHMOSPHERE).put(UnitsPressure.YOTTAPASCAL, Mul(Rat(Mul(Int(4), Pow(10, 22)), Int(4053)), Sym("atm")));
        c.get(UnitsPressure.ATHMOSPHERE).put(UnitsPressure.ZEPTOPASCAL, Mul(Rat(Int(1), Mul(Int(101325), Pow(10, 21))), Sym("atm")));
        c.get(UnitsPressure.ATHMOSPHERE).put(UnitsPressure.ZETTAPASCAL, Mul(Rat(Mul(Int(4), Pow(10, 19)), Int(4053)), Sym("atm")));
        c.get(UnitsPressure.ATTOPASCAL).put(UnitsPressure.ATHMOSPHERE, Mul(Mul(Int(101325), Pow(10, 18)), Sym("attopa")));
        c.get(UnitsPressure.ATTOPASCAL).put(UnitsPressure.BAR, Mul(Pow(10, 23), Sym("attopa")));
        c.get(UnitsPressure.ATTOPASCAL).put(UnitsPressure.CENTIBAR, Mul(Pow(10, 21), Sym("attopa")));
        c.get(UnitsPressure.ATTOPASCAL).put(UnitsPressure.CENTIPASCAL, Mul(Pow(10, 16), Sym("attopa")));
        c.get(UnitsPressure.ATTOPASCAL).put(UnitsPressure.DECAPASCAL, Mul(Pow(10, 19), Sym("attopa")));
        c.get(UnitsPressure.ATTOPASCAL).put(UnitsPressure.DECIBAR, Mul(Pow(10, 22), Sym("attopa")));
        c.get(UnitsPressure.ATTOPASCAL).put(UnitsPressure.DECIPASCAL, Mul(Pow(10, 17), Sym("attopa")));
        c.get(UnitsPressure.ATTOPASCAL).put(UnitsPressure.EXAPASCAL, Mul(Pow(10, 36), Sym("attopa")));
        c.get(UnitsPressure.ATTOPASCAL).put(UnitsPressure.FEMTOPASCAL, Mul(Int(1000), Sym("attopa")));
        c.get(UnitsPressure.ATTOPASCAL).put(UnitsPressure.GIGAPASCAL, Mul(Pow(10, 27), Sym("attopa")));
        c.get(UnitsPressure.ATTOPASCAL).put(UnitsPressure.HECTOPASCAL, Mul(Pow(10, 20), Sym("attopa")));
        c.get(UnitsPressure.ATTOPASCAL).put(UnitsPressure.KILOBAR, Mul(Pow(10, 26), Sym("attopa")));
        c.get(UnitsPressure.ATTOPASCAL).put(UnitsPressure.KILOPASCAL, Mul(Pow(10, 21), Sym("attopa")));
        c.get(UnitsPressure.ATTOPASCAL).put(UnitsPressure.MEGABAR, Mul(Pow(10, 29), Sym("attopa")));
        c.get(UnitsPressure.ATTOPASCAL).put(UnitsPressure.MEGAPASCAL, Mul(Pow(10, 24), Sym("attopa")));
        c.get(UnitsPressure.ATTOPASCAL).put(UnitsPressure.MICROPASCAL, Mul(Pow(10, 12), Sym("attopa")));
        c.get(UnitsPressure.ATTOPASCAL).put(UnitsPressure.MILLIBAR, Mul(Pow(10, 20), Sym("attopa")));
        c.get(UnitsPressure.ATTOPASCAL).put(UnitsPressure.MILLIPASCAL, Mul(Pow(10, 15), Sym("attopa")));
        c.get(UnitsPressure.ATTOPASCAL).put(UnitsPressure.MILLITORR, Mul(Rat(Mul(Int(2533125), Pow(10, 12)), Int(19)), Sym("attopa")));
        c.get(UnitsPressure.ATTOPASCAL).put(UnitsPressure.MMHG, Mul(Mul(Int("133322387415"), Pow(10, 9)), Sym("attopa")));
        c.get(UnitsPressure.ATTOPASCAL).put(UnitsPressure.NANOPASCAL, Mul(Pow(10, 9), Sym("attopa")));
        c.get(UnitsPressure.ATTOPASCAL).put(UnitsPressure.PASCAL, Mul(Pow(10, 18), Sym("attopa")));
        c.get(UnitsPressure.ATTOPASCAL).put(UnitsPressure.PETAPASCAL, Mul(Pow(10, 33), Sym("attopa")));
        c.get(UnitsPressure.ATTOPASCAL).put(UnitsPressure.PICOPASCAL, Mul(Pow(10, 6), Sym("attopa")));
        c.get(UnitsPressure.ATTOPASCAL).put(UnitsPressure.PSI, Mul(Mul(Int("689475729316836"), Pow(10, 7)), Sym("attopa")));
        c.get(UnitsPressure.ATTOPASCAL).put(UnitsPressure.TERAPASCAL, Mul(Pow(10, 30), Sym("attopa")));
        c.get(UnitsPressure.ATTOPASCAL).put(UnitsPressure.TORR, Mul(Rat(Mul(Int(2533125), Pow(10, 15)), Int(19)), Sym("attopa")));
        c.get(UnitsPressure.ATTOPASCAL).put(UnitsPressure.YOCTOPASCAL, Mul(Rat(Int(1), Pow(10, 6)), Sym("attopa")));
        c.get(UnitsPressure.ATTOPASCAL).put(UnitsPressure.YOTTAPASCAL, Mul(Pow(10, 42), Sym("attopa")));
        c.get(UnitsPressure.ATTOPASCAL).put(UnitsPressure.ZEPTOPASCAL, Mul(Rat(Int(1), Int(1000)), Sym("attopa")));
        c.get(UnitsPressure.ATTOPASCAL).put(UnitsPressure.ZETTAPASCAL, Mul(Pow(10, 39), Sym("attopa")));
        c.get(UnitsPressure.BAR).put(UnitsPressure.ATHMOSPHERE, Mul(Rat(Int(4053), Int(4000)), Sym("bar")));
        c.get(UnitsPressure.BAR).put(UnitsPressure.ATTOPASCAL, Mul(Rat(Int(1), Pow(10, 23)), Sym("bar")));
        c.get(UnitsPressure.BAR).put(UnitsPressure.CENTIBAR, Mul(Rat(Int(1), Int(100)), Sym("bar")));
        c.get(UnitsPressure.BAR).put(UnitsPressure.CENTIPASCAL, Mul(Rat(Int(1), Pow(10, 7)), Sym("bar")));
        c.get(UnitsPressure.BAR).put(UnitsPressure.DECAPASCAL, Mul(Rat(Int(1), Pow(10, 4)), Sym("bar")));
        c.get(UnitsPressure.BAR).put(UnitsPressure.DECIBAR, Mul(Rat(Int(1), Int(10)), Sym("bar")));
        c.get(UnitsPressure.BAR).put(UnitsPressure.DECIPASCAL, Mul(Rat(Int(1), Pow(10, 6)), Sym("bar")));
        c.get(UnitsPressure.BAR).put(UnitsPressure.EXAPASCAL, Mul(Pow(10, 13), Sym("bar")));
        c.get(UnitsPressure.BAR).put(UnitsPressure.FEMTOPASCAL, Mul(Rat(Int(1), Pow(10, 20)), Sym("bar")));
        c.get(UnitsPressure.BAR).put(UnitsPressure.GIGAPASCAL, Mul(Pow(10, 4), Sym("bar")));
        c.get(UnitsPressure.BAR).put(UnitsPressure.HECTOPASCAL, Mul(Rat(Int(1), Int(1000)), Sym("bar")));
        c.get(UnitsPressure.BAR).put(UnitsPressure.KILOBAR, Mul(Int(1000), Sym("bar")));
        c.get(UnitsPressure.BAR).put(UnitsPressure.KILOPASCAL, Mul(Rat(Int(1), Int(100)), Sym("bar")));
        c.get(UnitsPressure.BAR).put(UnitsPressure.MEGABAR, Mul(Pow(10, 6), Sym("bar")));
        c.get(UnitsPressure.BAR).put(UnitsPressure.MEGAPASCAL, Mul(Int(10), Sym("bar")));
        c.get(UnitsPressure.BAR).put(UnitsPressure.MICROPASCAL, Mul(Rat(Int(1), Pow(10, 11)), Sym("bar")));
        c.get(UnitsPressure.BAR).put(UnitsPressure.MILLIBAR, Mul(Rat(Int(1), Int(1000)), Sym("bar")));
        c.get(UnitsPressure.BAR).put(UnitsPressure.MILLIPASCAL, Mul(Rat(Int(1), Pow(10, 8)), Sym("bar")));
        c.get(UnitsPressure.BAR).put(UnitsPressure.MILLITORR, Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 7))), Sym("bar")));
        c.get(UnitsPressure.BAR).put(UnitsPressure.MMHG, Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 13))), Sym("bar")));
        c.get(UnitsPressure.BAR).put(UnitsPressure.NANOPASCAL, Mul(Rat(Int(1), Pow(10, 14)), Sym("bar")));
        c.get(UnitsPressure.BAR).put(UnitsPressure.PASCAL, Mul(Rat(Int(1), Pow(10, 5)), Sym("bar")));
        c.get(UnitsPressure.BAR).put(UnitsPressure.PETAPASCAL, Mul(Pow(10, 10), Sym("bar")));
        c.get(UnitsPressure.BAR).put(UnitsPressure.PICOPASCAL, Mul(Rat(Int(1), Pow(10, 17)), Sym("bar")));
        c.get(UnitsPressure.BAR).put(UnitsPressure.PSI, Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 14))), Sym("bar")));
        c.get(UnitsPressure.BAR).put(UnitsPressure.TERAPASCAL, Mul(Pow(10, 7), Sym("bar")));
        c.get(UnitsPressure.BAR).put(UnitsPressure.TORR, Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 4))), Sym("bar")));
        c.get(UnitsPressure.BAR).put(UnitsPressure.YOCTOPASCAL, Mul(Rat(Int(1), Pow(10, 29)), Sym("bar")));
        c.get(UnitsPressure.BAR).put(UnitsPressure.YOTTAPASCAL, Mul(Pow(10, 19), Sym("bar")));
        c.get(UnitsPressure.BAR).put(UnitsPressure.ZEPTOPASCAL, Mul(Rat(Int(1), Pow(10, 26)), Sym("bar")));
        c.get(UnitsPressure.BAR).put(UnitsPressure.ZETTAPASCAL, Mul(Pow(10, 16), Sym("bar")));
        c.get(UnitsPressure.CENTIBAR).put(UnitsPressure.ATHMOSPHERE, Mul(Rat(Int(4053), Int(40)), Sym("cbar")));
        c.get(UnitsPressure.CENTIBAR).put(UnitsPressure.ATTOPASCAL, Mul(Rat(Int(1), Pow(10, 21)), Sym("cbar")));
        c.get(UnitsPressure.CENTIBAR).put(UnitsPressure.BAR, Mul(Int(100), Sym("cbar")));
        c.get(UnitsPressure.CENTIBAR).put(UnitsPressure.CENTIPASCAL, Mul(Rat(Int(1), Pow(10, 5)), Sym("cbar")));
        c.get(UnitsPressure.CENTIBAR).put(UnitsPressure.DECAPASCAL, Mul(Rat(Int(1), Int(100)), Sym("cbar")));
        c.get(UnitsPressure.CENTIBAR).put(UnitsPressure.DECIBAR, Mul(Int(10), Sym("cbar")));
        c.get(UnitsPressure.CENTIBAR).put(UnitsPressure.DECIPASCAL, Mul(Rat(Int(1), Pow(10, 4)), Sym("cbar")));
        c.get(UnitsPressure.CENTIBAR).put(UnitsPressure.EXAPASCAL, Mul(Pow(10, 15), Sym("cbar")));
        c.get(UnitsPressure.CENTIBAR).put(UnitsPressure.FEMTOPASCAL, Mul(Rat(Int(1), Pow(10, 18)), Sym("cbar")));
        c.get(UnitsPressure.CENTIBAR).put(UnitsPressure.GIGAPASCAL, Mul(Pow(10, 6), Sym("cbar")));
        c.get(UnitsPressure.CENTIBAR).put(UnitsPressure.HECTOPASCAL, Mul(Rat(Int(1), Int(10)), Sym("cbar")));
        c.get(UnitsPressure.CENTIBAR).put(UnitsPressure.KILOBAR, Mul(Pow(10, 5), Sym("cbar")));
        c.get(UnitsPressure.CENTIBAR).put(UnitsPressure.KILOPASCAL, Sym("cbar"));
        c.get(UnitsPressure.CENTIBAR).put(UnitsPressure.MEGABAR, Mul(Pow(10, 8), Sym("cbar")));
        c.get(UnitsPressure.CENTIBAR).put(UnitsPressure.MEGAPASCAL, Mul(Int(1000), Sym("cbar")));
        c.get(UnitsPressure.CENTIBAR).put(UnitsPressure.MICROPASCAL, Mul(Rat(Int(1), Pow(10, 9)), Sym("cbar")));
        c.get(UnitsPressure.CENTIBAR).put(UnitsPressure.MILLIBAR, Mul(Rat(Int(1), Int(10)), Sym("cbar")));
        c.get(UnitsPressure.CENTIBAR).put(UnitsPressure.MILLIPASCAL, Mul(Rat(Int(1), Pow(10, 6)), Sym("cbar")));
        c.get(UnitsPressure.CENTIBAR).put(UnitsPressure.MILLITORR, Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 5))), Sym("cbar")));
        c.get(UnitsPressure.CENTIBAR).put(UnitsPressure.MMHG, Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 11))), Sym("cbar")));
        c.get(UnitsPressure.CENTIBAR).put(UnitsPressure.NANOPASCAL, Mul(Rat(Int(1), Pow(10, 12)), Sym("cbar")));
        c.get(UnitsPressure.CENTIBAR).put(UnitsPressure.PASCAL, Mul(Rat(Int(1), Int(1000)), Sym("cbar")));
        c.get(UnitsPressure.CENTIBAR).put(UnitsPressure.PETAPASCAL, Mul(Pow(10, 12), Sym("cbar")));
        c.get(UnitsPressure.CENTIBAR).put(UnitsPressure.PICOPASCAL, Mul(Rat(Int(1), Pow(10, 15)), Sym("cbar")));
        c.get(UnitsPressure.CENTIBAR).put(UnitsPressure.PSI, Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 12))), Sym("cbar")));
        c.get(UnitsPressure.CENTIBAR).put(UnitsPressure.TERAPASCAL, Mul(Pow(10, 9), Sym("cbar")));
        c.get(UnitsPressure.CENTIBAR).put(UnitsPressure.TORR, Mul(Rat(Int(4053), Int(30400)), Sym("cbar")));
        c.get(UnitsPressure.CENTIBAR).put(UnitsPressure.YOCTOPASCAL, Mul(Rat(Int(1), Pow(10, 27)), Sym("cbar")));
        c.get(UnitsPressure.CENTIBAR).put(UnitsPressure.YOTTAPASCAL, Mul(Pow(10, 21), Sym("cbar")));
        c.get(UnitsPressure.CENTIBAR).put(UnitsPressure.ZEPTOPASCAL, Mul(Rat(Int(1), Pow(10, 24)), Sym("cbar")));
        c.get(UnitsPressure.CENTIBAR).put(UnitsPressure.ZETTAPASCAL, Mul(Pow(10, 18), Sym("cbar")));
        c.get(UnitsPressure.CENTIPASCAL).put(UnitsPressure.ATHMOSPHERE, Mul(Int(10132500), Sym("centipa")));
        c.get(UnitsPressure.CENTIPASCAL).put(UnitsPressure.ATTOPASCAL, Mul(Rat(Int(1), Pow(10, 16)), Sym("centipa")));
        c.get(UnitsPressure.CENTIPASCAL).put(UnitsPressure.BAR, Mul(Pow(10, 7), Sym("centipa")));
        c.get(UnitsPressure.CENTIPASCAL).put(UnitsPressure.CENTIBAR, Mul(Pow(10, 5), Sym("centipa")));
        c.get(UnitsPressure.CENTIPASCAL).put(UnitsPressure.DECAPASCAL, Mul(Int(1000), Sym("centipa")));
        c.get(UnitsPressure.CENTIPASCAL).put(UnitsPressure.DECIBAR, Mul(Pow(10, 6), Sym("centipa")));
        c.get(UnitsPressure.CENTIPASCAL).put(UnitsPressure.DECIPASCAL, Mul(Int(10), Sym("centipa")));
        c.get(UnitsPressure.CENTIPASCAL).put(UnitsPressure.EXAPASCAL, Mul(Pow(10, 20), Sym("centipa")));
        c.get(UnitsPressure.CENTIPASCAL).put(UnitsPressure.FEMTOPASCAL, Mul(Rat(Int(1), Pow(10, 13)), Sym("centipa")));
        c.get(UnitsPressure.CENTIPASCAL).put(UnitsPressure.GIGAPASCAL, Mul(Pow(10, 11), Sym("centipa")));
        c.get(UnitsPressure.CENTIPASCAL).put(UnitsPressure.HECTOPASCAL, Mul(Pow(10, 4), Sym("centipa")));
        c.get(UnitsPressure.CENTIPASCAL).put(UnitsPressure.KILOBAR, Mul(Pow(10, 10), Sym("centipa")));
        c.get(UnitsPressure.CENTIPASCAL).put(UnitsPressure.KILOPASCAL, Mul(Pow(10, 5), Sym("centipa")));
        c.get(UnitsPressure.CENTIPASCAL).put(UnitsPressure.MEGABAR, Mul(Pow(10, 13), Sym("centipa")));
        c.get(UnitsPressure.CENTIPASCAL).put(UnitsPressure.MEGAPASCAL, Mul(Pow(10, 8), Sym("centipa")));
        c.get(UnitsPressure.CENTIPASCAL).put(UnitsPressure.MICROPASCAL, Mul(Rat(Int(1), Pow(10, 4)), Sym("centipa")));
        c.get(UnitsPressure.CENTIPASCAL).put(UnitsPressure.MILLIBAR, Mul(Pow(10, 4), Sym("centipa")));
        c.get(UnitsPressure.CENTIPASCAL).put(UnitsPressure.MILLIPASCAL, Mul(Rat(Int(1), Int(10)), Sym("centipa")));
        c.get(UnitsPressure.CENTIPASCAL).put(UnitsPressure.MILLITORR, Mul(Rat(Int(4053), Int(304)), Sym("centipa")));
        c.get(UnitsPressure.CENTIPASCAL).put(UnitsPressure.MMHG, Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 6))), Sym("centipa")));
        c.get(UnitsPressure.CENTIPASCAL).put(UnitsPressure.NANOPASCAL, Mul(Rat(Int(1), Pow(10, 7)), Sym("centipa")));
        c.get(UnitsPressure.CENTIPASCAL).put(UnitsPressure.PASCAL, Mul(Int(100), Sym("centipa")));
        c.get(UnitsPressure.CENTIPASCAL).put(UnitsPressure.PETAPASCAL, Mul(Pow(10, 17), Sym("centipa")));
        c.get(UnitsPressure.CENTIPASCAL).put(UnitsPressure.PICOPASCAL, Mul(Rat(Int(1), Pow(10, 10)), Sym("centipa")));
        c.get(UnitsPressure.CENTIPASCAL).put(UnitsPressure.PSI, Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 7))), Sym("centipa")));
        c.get(UnitsPressure.CENTIPASCAL).put(UnitsPressure.TERAPASCAL, Mul(Pow(10, 14), Sym("centipa")));
        c.get(UnitsPressure.CENTIPASCAL).put(UnitsPressure.TORR, Mul(Rat(Int(506625), Int(38)), Sym("centipa")));
        c.get(UnitsPressure.CENTIPASCAL).put(UnitsPressure.YOCTOPASCAL, Mul(Rat(Int(1), Pow(10, 22)), Sym("centipa")));
        c.get(UnitsPressure.CENTIPASCAL).put(UnitsPressure.YOTTAPASCAL, Mul(Pow(10, 26), Sym("centipa")));
        c.get(UnitsPressure.CENTIPASCAL).put(UnitsPressure.ZEPTOPASCAL, Mul(Rat(Int(1), Pow(10, 19)), Sym("centipa")));
        c.get(UnitsPressure.CENTIPASCAL).put(UnitsPressure.ZETTAPASCAL, Mul(Pow(10, 23), Sym("centipa")));
        c.get(UnitsPressure.DECAPASCAL).put(UnitsPressure.ATHMOSPHERE, Mul(Rat(Int(20265), Int(2)), Sym("decapa")));
        c.get(UnitsPressure.DECAPASCAL).put(UnitsPressure.ATTOPASCAL, Mul(Rat(Int(1), Pow(10, 19)), Sym("decapa")));
        c.get(UnitsPressure.DECAPASCAL).put(UnitsPressure.BAR, Mul(Pow(10, 4), Sym("decapa")));
        c.get(UnitsPressure.DECAPASCAL).put(UnitsPressure.CENTIBAR, Mul(Int(100), Sym("decapa")));
        c.get(UnitsPressure.DECAPASCAL).put(UnitsPressure.CENTIPASCAL, Mul(Rat(Int(1), Int(1000)), Sym("decapa")));
        c.get(UnitsPressure.DECAPASCAL).put(UnitsPressure.DECIBAR, Mul(Int(1000), Sym("decapa")));
        c.get(UnitsPressure.DECAPASCAL).put(UnitsPressure.DECIPASCAL, Mul(Rat(Int(1), Int(100)), Sym("decapa")));
        c.get(UnitsPressure.DECAPASCAL).put(UnitsPressure.EXAPASCAL, Mul(Pow(10, 17), Sym("decapa")));
        c.get(UnitsPressure.DECAPASCAL).put(UnitsPressure.FEMTOPASCAL, Mul(Rat(Int(1), Pow(10, 16)), Sym("decapa")));
        c.get(UnitsPressure.DECAPASCAL).put(UnitsPressure.GIGAPASCAL, Mul(Pow(10, 8), Sym("decapa")));
        c.get(UnitsPressure.DECAPASCAL).put(UnitsPressure.HECTOPASCAL, Mul(Int(10), Sym("decapa")));
        c.get(UnitsPressure.DECAPASCAL).put(UnitsPressure.KILOBAR, Mul(Pow(10, 7), Sym("decapa")));
        c.get(UnitsPressure.DECAPASCAL).put(UnitsPressure.KILOPASCAL, Mul(Int(100), Sym("decapa")));
        c.get(UnitsPressure.DECAPASCAL).put(UnitsPressure.MEGABAR, Mul(Pow(10, 10), Sym("decapa")));
        c.get(UnitsPressure.DECAPASCAL).put(UnitsPressure.MEGAPASCAL, Mul(Pow(10, 5), Sym("decapa")));
        c.get(UnitsPressure.DECAPASCAL).put(UnitsPressure.MICROPASCAL, Mul(Rat(Int(1), Pow(10, 7)), Sym("decapa")));
        c.get(UnitsPressure.DECAPASCAL).put(UnitsPressure.MILLIBAR, Mul(Int(10), Sym("decapa")));
        c.get(UnitsPressure.DECAPASCAL).put(UnitsPressure.MILLIPASCAL, Mul(Rat(Int(1), Pow(10, 4)), Sym("decapa")));
        c.get(UnitsPressure.DECAPASCAL).put(UnitsPressure.MILLITORR, Mul(Rat(Int(4053), Int(304000)), Sym("decapa")));
        c.get(UnitsPressure.DECAPASCAL).put(UnitsPressure.MMHG, Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 9))), Sym("decapa")));
        c.get(UnitsPressure.DECAPASCAL).put(UnitsPressure.NANOPASCAL, Mul(Rat(Int(1), Pow(10, 10)), Sym("decapa")));
        c.get(UnitsPressure.DECAPASCAL).put(UnitsPressure.PASCAL, Mul(Rat(Int(1), Int(10)), Sym("decapa")));
        c.get(UnitsPressure.DECAPASCAL).put(UnitsPressure.PETAPASCAL, Mul(Pow(10, 14), Sym("decapa")));
        c.get(UnitsPressure.DECAPASCAL).put(UnitsPressure.PICOPASCAL, Mul(Rat(Int(1), Pow(10, 13)), Sym("decapa")));
        c.get(UnitsPressure.DECAPASCAL).put(UnitsPressure.PSI, Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 10))), Sym("decapa")));
        c.get(UnitsPressure.DECAPASCAL).put(UnitsPressure.TERAPASCAL, Mul(Pow(10, 11), Sym("decapa")));
        c.get(UnitsPressure.DECAPASCAL).put(UnitsPressure.TORR, Mul(Rat(Int(4053), Int(304)), Sym("decapa")));
        c.get(UnitsPressure.DECAPASCAL).put(UnitsPressure.YOCTOPASCAL, Mul(Rat(Int(1), Pow(10, 25)), Sym("decapa")));
        c.get(UnitsPressure.DECAPASCAL).put(UnitsPressure.YOTTAPASCAL, Mul(Pow(10, 23), Sym("decapa")));
        c.get(UnitsPressure.DECAPASCAL).put(UnitsPressure.ZEPTOPASCAL, Mul(Rat(Int(1), Pow(10, 22)), Sym("decapa")));
        c.get(UnitsPressure.DECAPASCAL).put(UnitsPressure.ZETTAPASCAL, Mul(Pow(10, 20), Sym("decapa")));
        c.get(UnitsPressure.DECIBAR).put(UnitsPressure.ATHMOSPHERE, Mul(Rat(Int(4053), Int(400)), Sym("dbar")));
        c.get(UnitsPressure.DECIBAR).put(UnitsPressure.ATTOPASCAL, Mul(Rat(Int(1), Pow(10, 22)), Sym("dbar")));
        c.get(UnitsPressure.DECIBAR).put(UnitsPressure.BAR, Mul(Int(10), Sym("dbar")));
        c.get(UnitsPressure.DECIBAR).put(UnitsPressure.CENTIBAR, Mul(Rat(Int(1), Int(10)), Sym("dbar")));
        c.get(UnitsPressure.DECIBAR).put(UnitsPressure.CENTIPASCAL, Mul(Rat(Int(1), Pow(10, 6)), Sym("dbar")));
        c.get(UnitsPressure.DECIBAR).put(UnitsPressure.DECAPASCAL, Mul(Rat(Int(1), Int(1000)), Sym("dbar")));
        c.get(UnitsPressure.DECIBAR).put(UnitsPressure.DECIPASCAL, Mul(Rat(Int(1), Pow(10, 5)), Sym("dbar")));
        c.get(UnitsPressure.DECIBAR).put(UnitsPressure.EXAPASCAL, Mul(Pow(10, 14), Sym("dbar")));
        c.get(UnitsPressure.DECIBAR).put(UnitsPressure.FEMTOPASCAL, Mul(Rat(Int(1), Pow(10, 19)), Sym("dbar")));
        c.get(UnitsPressure.DECIBAR).put(UnitsPressure.GIGAPASCAL, Mul(Pow(10, 5), Sym("dbar")));
        c.get(UnitsPressure.DECIBAR).put(UnitsPressure.HECTOPASCAL, Mul(Rat(Int(1), Int(100)), Sym("dbar")));
        c.get(UnitsPressure.DECIBAR).put(UnitsPressure.KILOBAR, Mul(Pow(10, 4), Sym("dbar")));
        c.get(UnitsPressure.DECIBAR).put(UnitsPressure.KILOPASCAL, Mul(Rat(Int(1), Int(10)), Sym("dbar")));
        c.get(UnitsPressure.DECIBAR).put(UnitsPressure.MEGABAR, Mul(Pow(10, 7), Sym("dbar")));
        c.get(UnitsPressure.DECIBAR).put(UnitsPressure.MEGAPASCAL, Mul(Int(100), Sym("dbar")));
        c.get(UnitsPressure.DECIBAR).put(UnitsPressure.MICROPASCAL, Mul(Rat(Int(1), Pow(10, 10)), Sym("dbar")));
        c.get(UnitsPressure.DECIBAR).put(UnitsPressure.MILLIBAR, Mul(Rat(Int(1), Int(100)), Sym("dbar")));
        c.get(UnitsPressure.DECIBAR).put(UnitsPressure.MILLIPASCAL, Mul(Rat(Int(1), Pow(10, 7)), Sym("dbar")));
        c.get(UnitsPressure.DECIBAR).put(UnitsPressure.MILLITORR, Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 6))), Sym("dbar")));
        c.get(UnitsPressure.DECIBAR).put(UnitsPressure.MMHG, Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 12))), Sym("dbar")));
        c.get(UnitsPressure.DECIBAR).put(UnitsPressure.NANOPASCAL, Mul(Rat(Int(1), Pow(10, 13)), Sym("dbar")));
        c.get(UnitsPressure.DECIBAR).put(UnitsPressure.PASCAL, Mul(Rat(Int(1), Pow(10, 4)), Sym("dbar")));
        c.get(UnitsPressure.DECIBAR).put(UnitsPressure.PETAPASCAL, Mul(Pow(10, 11), Sym("dbar")));
        c.get(UnitsPressure.DECIBAR).put(UnitsPressure.PICOPASCAL, Mul(Rat(Int(1), Pow(10, 16)), Sym("dbar")));
        c.get(UnitsPressure.DECIBAR).put(UnitsPressure.PSI, Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 13))), Sym("dbar")));
        c.get(UnitsPressure.DECIBAR).put(UnitsPressure.TERAPASCAL, Mul(Pow(10, 8), Sym("dbar")));
        c.get(UnitsPressure.DECIBAR).put(UnitsPressure.TORR, Mul(Rat(Int(4053), Int(304000)), Sym("dbar")));
        c.get(UnitsPressure.DECIBAR).put(UnitsPressure.YOCTOPASCAL, Mul(Rat(Int(1), Pow(10, 28)), Sym("dbar")));
        c.get(UnitsPressure.DECIBAR).put(UnitsPressure.YOTTAPASCAL, Mul(Pow(10, 20), Sym("dbar")));
        c.get(UnitsPressure.DECIBAR).put(UnitsPressure.ZEPTOPASCAL, Mul(Rat(Int(1), Pow(10, 25)), Sym("dbar")));
        c.get(UnitsPressure.DECIBAR).put(UnitsPressure.ZETTAPASCAL, Mul(Pow(10, 17), Sym("dbar")));
        c.get(UnitsPressure.DECIPASCAL).put(UnitsPressure.ATHMOSPHERE, Mul(Int(1013250), Sym("decipa")));
        c.get(UnitsPressure.DECIPASCAL).put(UnitsPressure.ATTOPASCAL, Mul(Rat(Int(1), Pow(10, 17)), Sym("decipa")));
        c.get(UnitsPressure.DECIPASCAL).put(UnitsPressure.BAR, Mul(Pow(10, 6), Sym("decipa")));
        c.get(UnitsPressure.DECIPASCAL).put(UnitsPressure.CENTIBAR, Mul(Pow(10, 4), Sym("decipa")));
        c.get(UnitsPressure.DECIPASCAL).put(UnitsPressure.CENTIPASCAL, Mul(Rat(Int(1), Int(10)), Sym("decipa")));
        c.get(UnitsPressure.DECIPASCAL).put(UnitsPressure.DECAPASCAL, Mul(Int(100), Sym("decipa")));
        c.get(UnitsPressure.DECIPASCAL).put(UnitsPressure.DECIBAR, Mul(Pow(10, 5), Sym("decipa")));
        c.get(UnitsPressure.DECIPASCAL).put(UnitsPressure.EXAPASCAL, Mul(Pow(10, 19), Sym("decipa")));
        c.get(UnitsPressure.DECIPASCAL).put(UnitsPressure.FEMTOPASCAL, Mul(Rat(Int(1), Pow(10, 14)), Sym("decipa")));
        c.get(UnitsPressure.DECIPASCAL).put(UnitsPressure.GIGAPASCAL, Mul(Pow(10, 10), Sym("decipa")));
        c.get(UnitsPressure.DECIPASCAL).put(UnitsPressure.HECTOPASCAL, Mul(Int(1000), Sym("decipa")));
        c.get(UnitsPressure.DECIPASCAL).put(UnitsPressure.KILOBAR, Mul(Pow(10, 9), Sym("decipa")));
        c.get(UnitsPressure.DECIPASCAL).put(UnitsPressure.KILOPASCAL, Mul(Pow(10, 4), Sym("decipa")));
        c.get(UnitsPressure.DECIPASCAL).put(UnitsPressure.MEGABAR, Mul(Pow(10, 12), Sym("decipa")));
        c.get(UnitsPressure.DECIPASCAL).put(UnitsPressure.MEGAPASCAL, Mul(Pow(10, 7), Sym("decipa")));
        c.get(UnitsPressure.DECIPASCAL).put(UnitsPressure.MICROPASCAL, Mul(Rat(Int(1), Pow(10, 5)), Sym("decipa")));
        c.get(UnitsPressure.DECIPASCAL).put(UnitsPressure.MILLIBAR, Mul(Int(1000), Sym("decipa")));
        c.get(UnitsPressure.DECIPASCAL).put(UnitsPressure.MILLIPASCAL, Mul(Rat(Int(1), Int(100)), Sym("decipa")));
        c.get(UnitsPressure.DECIPASCAL).put(UnitsPressure.MILLITORR, Mul(Rat(Int(4053), Int(3040)), Sym("decipa")));
        c.get(UnitsPressure.DECIPASCAL).put(UnitsPressure.MMHG, Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 7))), Sym("decipa")));
        c.get(UnitsPressure.DECIPASCAL).put(UnitsPressure.NANOPASCAL, Mul(Rat(Int(1), Pow(10, 8)), Sym("decipa")));
        c.get(UnitsPressure.DECIPASCAL).put(UnitsPressure.PASCAL, Mul(Int(10), Sym("decipa")));
        c.get(UnitsPressure.DECIPASCAL).put(UnitsPressure.PETAPASCAL, Mul(Pow(10, 16), Sym("decipa")));
        c.get(UnitsPressure.DECIPASCAL).put(UnitsPressure.PICOPASCAL, Mul(Rat(Int(1), Pow(10, 11)), Sym("decipa")));
        c.get(UnitsPressure.DECIPASCAL).put(UnitsPressure.PSI, Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 8))), Sym("decipa")));
        c.get(UnitsPressure.DECIPASCAL).put(UnitsPressure.TERAPASCAL, Mul(Pow(10, 13), Sym("decipa")));
        c.get(UnitsPressure.DECIPASCAL).put(UnitsPressure.TORR, Mul(Rat(Int(101325), Int(76)), Sym("decipa")));
        c.get(UnitsPressure.DECIPASCAL).put(UnitsPressure.YOCTOPASCAL, Mul(Rat(Int(1), Pow(10, 23)), Sym("decipa")));
        c.get(UnitsPressure.DECIPASCAL).put(UnitsPressure.YOTTAPASCAL, Mul(Pow(10, 25), Sym("decipa")));
        c.get(UnitsPressure.DECIPASCAL).put(UnitsPressure.ZEPTOPASCAL, Mul(Rat(Int(1), Pow(10, 20)), Sym("decipa")));
        c.get(UnitsPressure.DECIPASCAL).put(UnitsPressure.ZETTAPASCAL, Mul(Pow(10, 22), Sym("decipa")));
        c.get(UnitsPressure.EXAPASCAL).put(UnitsPressure.ATHMOSPHERE, Mul(Rat(Int(4053), Mul(Int(4), Pow(10, 16))), Sym("exapa")));
        c.get(UnitsPressure.EXAPASCAL).put(UnitsPressure.ATTOPASCAL, Mul(Rat(Int(1), Pow(10, 36)), Sym("exapa")));
        c.get(UnitsPressure.EXAPASCAL).put(UnitsPressure.BAR, Mul(Rat(Int(1), Pow(10, 13)), Sym("exapa")));
        c.get(UnitsPressure.EXAPASCAL).put(UnitsPressure.CENTIBAR, Mul(Rat(Int(1), Pow(10, 15)), Sym("exapa")));
        c.get(UnitsPressure.EXAPASCAL).put(UnitsPressure.CENTIPASCAL, Mul(Rat(Int(1), Pow(10, 20)), Sym("exapa")));
        c.get(UnitsPressure.EXAPASCAL).put(UnitsPressure.DECAPASCAL, Mul(Rat(Int(1), Pow(10, 17)), Sym("exapa")));
        c.get(UnitsPressure.EXAPASCAL).put(UnitsPressure.DECIBAR, Mul(Rat(Int(1), Pow(10, 14)), Sym("exapa")));
        c.get(UnitsPressure.EXAPASCAL).put(UnitsPressure.DECIPASCAL, Mul(Rat(Int(1), Pow(10, 19)), Sym("exapa")));
        c.get(UnitsPressure.EXAPASCAL).put(UnitsPressure.FEMTOPASCAL, Mul(Rat(Int(1), Pow(10, 33)), Sym("exapa")));
        c.get(UnitsPressure.EXAPASCAL).put(UnitsPressure.GIGAPASCAL, Mul(Rat(Int(1), Pow(10, 9)), Sym("exapa")));
        c.get(UnitsPressure.EXAPASCAL).put(UnitsPressure.HECTOPASCAL, Mul(Rat(Int(1), Pow(10, 16)), Sym("exapa")));
        c.get(UnitsPressure.EXAPASCAL).put(UnitsPressure.KILOBAR, Mul(Rat(Int(1), Pow(10, 10)), Sym("exapa")));
        c.get(UnitsPressure.EXAPASCAL).put(UnitsPressure.KILOPASCAL, Mul(Rat(Int(1), Pow(10, 15)), Sym("exapa")));
        c.get(UnitsPressure.EXAPASCAL).put(UnitsPressure.MEGABAR, Mul(Rat(Int(1), Pow(10, 7)), Sym("exapa")));
        c.get(UnitsPressure.EXAPASCAL).put(UnitsPressure.MEGAPASCAL, Mul(Rat(Int(1), Pow(10, 12)), Sym("exapa")));
        c.get(UnitsPressure.EXAPASCAL).put(UnitsPressure.MICROPASCAL, Mul(Rat(Int(1), Pow(10, 24)), Sym("exapa")));
        c.get(UnitsPressure.EXAPASCAL).put(UnitsPressure.MILLIBAR, Mul(Rat(Int(1), Pow(10, 16)), Sym("exapa")));
        c.get(UnitsPressure.EXAPASCAL).put(UnitsPressure.MILLIPASCAL, Mul(Rat(Int(1), Pow(10, 21)), Sym("exapa")));
        c.get(UnitsPressure.EXAPASCAL).put(UnitsPressure.MILLITORR, Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 20))), Sym("exapa")));
        c.get(UnitsPressure.EXAPASCAL).put(UnitsPressure.MMHG, Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 26))), Sym("exapa")));
        c.get(UnitsPressure.EXAPASCAL).put(UnitsPressure.NANOPASCAL, Mul(Rat(Int(1), Pow(10, 27)), Sym("exapa")));
        c.get(UnitsPressure.EXAPASCAL).put(UnitsPressure.PASCAL, Mul(Rat(Int(1), Pow(10, 18)), Sym("exapa")));
        c.get(UnitsPressure.EXAPASCAL).put(UnitsPressure.PETAPASCAL, Mul(Rat(Int(1), Int(1000)), Sym("exapa")));
        c.get(UnitsPressure.EXAPASCAL).put(UnitsPressure.PICOPASCAL, Mul(Rat(Int(1), Pow(10, 30)), Sym("exapa")));
        c.get(UnitsPressure.EXAPASCAL).put(UnitsPressure.PSI, Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 27))), Sym("exapa")));
        c.get(UnitsPressure.EXAPASCAL).put(UnitsPressure.TERAPASCAL, Mul(Rat(Int(1), Pow(10, 6)), Sym("exapa")));
        c.get(UnitsPressure.EXAPASCAL).put(UnitsPressure.TORR, Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 17))), Sym("exapa")));
        c.get(UnitsPressure.EXAPASCAL).put(UnitsPressure.YOCTOPASCAL, Mul(Rat(Int(1), Pow(10, 42)), Sym("exapa")));
        c.get(UnitsPressure.EXAPASCAL).put(UnitsPressure.YOTTAPASCAL, Mul(Pow(10, 6), Sym("exapa")));
        c.get(UnitsPressure.EXAPASCAL).put(UnitsPressure.ZEPTOPASCAL, Mul(Rat(Int(1), Pow(10, 39)), Sym("exapa")));
        c.get(UnitsPressure.EXAPASCAL).put(UnitsPressure.ZETTAPASCAL, Mul(Int(1000), Sym("exapa")));
        c.get(UnitsPressure.FEMTOPASCAL).put(UnitsPressure.ATHMOSPHERE, Mul(Mul(Int(101325), Pow(10, 15)), Sym("femtopa")));
        c.get(UnitsPressure.FEMTOPASCAL).put(UnitsPressure.ATTOPASCAL, Mul(Rat(Int(1), Int(1000)), Sym("femtopa")));
        c.get(UnitsPressure.FEMTOPASCAL).put(UnitsPressure.BAR, Mul(Pow(10, 20), Sym("femtopa")));
        c.get(UnitsPressure.FEMTOPASCAL).put(UnitsPressure.CENTIBAR, Mul(Pow(10, 18), Sym("femtopa")));
        c.get(UnitsPressure.FEMTOPASCAL).put(UnitsPressure.CENTIPASCAL, Mul(Pow(10, 13), Sym("femtopa")));
        c.get(UnitsPressure.FEMTOPASCAL).put(UnitsPressure.DECAPASCAL, Mul(Pow(10, 16), Sym("femtopa")));
        c.get(UnitsPressure.FEMTOPASCAL).put(UnitsPressure.DECIBAR, Mul(Pow(10, 19), Sym("femtopa")));
        c.get(UnitsPressure.FEMTOPASCAL).put(UnitsPressure.DECIPASCAL, Mul(Pow(10, 14), Sym("femtopa")));
        c.get(UnitsPressure.FEMTOPASCAL).put(UnitsPressure.EXAPASCAL, Mul(Pow(10, 33), Sym("femtopa")));
        c.get(UnitsPressure.FEMTOPASCAL).put(UnitsPressure.GIGAPASCAL, Mul(Pow(10, 24), Sym("femtopa")));
        c.get(UnitsPressure.FEMTOPASCAL).put(UnitsPressure.HECTOPASCAL, Mul(Pow(10, 17), Sym("femtopa")));
        c.get(UnitsPressure.FEMTOPASCAL).put(UnitsPressure.KILOBAR, Mul(Pow(10, 23), Sym("femtopa")));
        c.get(UnitsPressure.FEMTOPASCAL).put(UnitsPressure.KILOPASCAL, Mul(Pow(10, 18), Sym("femtopa")));
        c.get(UnitsPressure.FEMTOPASCAL).put(UnitsPressure.MEGABAR, Mul(Pow(10, 26), Sym("femtopa")));
        c.get(UnitsPressure.FEMTOPASCAL).put(UnitsPressure.MEGAPASCAL, Mul(Pow(10, 21), Sym("femtopa")));
        c.get(UnitsPressure.FEMTOPASCAL).put(UnitsPressure.MICROPASCAL, Mul(Pow(10, 9), Sym("femtopa")));
        c.get(UnitsPressure.FEMTOPASCAL).put(UnitsPressure.MILLIBAR, Mul(Pow(10, 17), Sym("femtopa")));
        c.get(UnitsPressure.FEMTOPASCAL).put(UnitsPressure.MILLIPASCAL, Mul(Pow(10, 12), Sym("femtopa")));
        c.get(UnitsPressure.FEMTOPASCAL).put(UnitsPressure.MILLITORR, Mul(Rat(Mul(Int(2533125), Pow(10, 9)), Int(19)), Sym("femtopa")));
        c.get(UnitsPressure.FEMTOPASCAL).put(UnitsPressure.MMHG, Mul(Mul(Int("133322387415"), Pow(10, 6)), Sym("femtopa")));
        c.get(UnitsPressure.FEMTOPASCAL).put(UnitsPressure.NANOPASCAL, Mul(Pow(10, 6), Sym("femtopa")));
        c.get(UnitsPressure.FEMTOPASCAL).put(UnitsPressure.PASCAL, Mul(Pow(10, 15), Sym("femtopa")));
        c.get(UnitsPressure.FEMTOPASCAL).put(UnitsPressure.PETAPASCAL, Mul(Pow(10, 30), Sym("femtopa")));
        c.get(UnitsPressure.FEMTOPASCAL).put(UnitsPressure.PICOPASCAL, Mul(Int(1000), Sym("femtopa")));
        c.get(UnitsPressure.FEMTOPASCAL).put(UnitsPressure.PSI, Mul(Mul(Int("689475729316836"), Pow(10, 4)), Sym("femtopa")));
        c.get(UnitsPressure.FEMTOPASCAL).put(UnitsPressure.TERAPASCAL, Mul(Pow(10, 27), Sym("femtopa")));
        c.get(UnitsPressure.FEMTOPASCAL).put(UnitsPressure.TORR, Mul(Rat(Mul(Int(2533125), Pow(10, 12)), Int(19)), Sym("femtopa")));
        c.get(UnitsPressure.FEMTOPASCAL).put(UnitsPressure.YOCTOPASCAL, Mul(Rat(Int(1), Pow(10, 9)), Sym("femtopa")));
        c.get(UnitsPressure.FEMTOPASCAL).put(UnitsPressure.YOTTAPASCAL, Mul(Pow(10, 39), Sym("femtopa")));
        c.get(UnitsPressure.FEMTOPASCAL).put(UnitsPressure.ZEPTOPASCAL, Mul(Rat(Int(1), Pow(10, 6)), Sym("femtopa")));
        c.get(UnitsPressure.FEMTOPASCAL).put(UnitsPressure.ZETTAPASCAL, Mul(Pow(10, 36), Sym("femtopa")));
        c.get(UnitsPressure.GIGAPASCAL).put(UnitsPressure.ATHMOSPHERE, Mul(Rat(Int(4053), Mul(Int(4), Pow(10, 7))), Sym("gigapa")));
        c.get(UnitsPressure.GIGAPASCAL).put(UnitsPressure.ATTOPASCAL, Mul(Rat(Int(1), Pow(10, 27)), Sym("gigapa")));
        c.get(UnitsPressure.GIGAPASCAL).put(UnitsPressure.BAR, Mul(Rat(Int(1), Pow(10, 4)), Sym("gigapa")));
        c.get(UnitsPressure.GIGAPASCAL).put(UnitsPressure.CENTIBAR, Mul(Rat(Int(1), Pow(10, 6)), Sym("gigapa")));
        c.get(UnitsPressure.GIGAPASCAL).put(UnitsPressure.CENTIPASCAL, Mul(Rat(Int(1), Pow(10, 11)), Sym("gigapa")));
        c.get(UnitsPressure.GIGAPASCAL).put(UnitsPressure.DECAPASCAL, Mul(Rat(Int(1), Pow(10, 8)), Sym("gigapa")));
        c.get(UnitsPressure.GIGAPASCAL).put(UnitsPressure.DECIBAR, Mul(Rat(Int(1), Pow(10, 5)), Sym("gigapa")));
        c.get(UnitsPressure.GIGAPASCAL).put(UnitsPressure.DECIPASCAL, Mul(Rat(Int(1), Pow(10, 10)), Sym("gigapa")));
        c.get(UnitsPressure.GIGAPASCAL).put(UnitsPressure.EXAPASCAL, Mul(Pow(10, 9), Sym("gigapa")));
        c.get(UnitsPressure.GIGAPASCAL).put(UnitsPressure.FEMTOPASCAL, Mul(Rat(Int(1), Pow(10, 24)), Sym("gigapa")));
        c.get(UnitsPressure.GIGAPASCAL).put(UnitsPressure.HECTOPASCAL, Mul(Rat(Int(1), Pow(10, 7)), Sym("gigapa")));
        c.get(UnitsPressure.GIGAPASCAL).put(UnitsPressure.KILOBAR, Mul(Rat(Int(1), Int(10)), Sym("gigapa")));
        c.get(UnitsPressure.GIGAPASCAL).put(UnitsPressure.KILOPASCAL, Mul(Rat(Int(1), Pow(10, 6)), Sym("gigapa")));
        c.get(UnitsPressure.GIGAPASCAL).put(UnitsPressure.MEGABAR, Mul(Int(100), Sym("gigapa")));
        c.get(UnitsPressure.GIGAPASCAL).put(UnitsPressure.MEGAPASCAL, Mul(Rat(Int(1), Int(1000)), Sym("gigapa")));
        c.get(UnitsPressure.GIGAPASCAL).put(UnitsPressure.MICROPASCAL, Mul(Rat(Int(1), Pow(10, 15)), Sym("gigapa")));
        c.get(UnitsPressure.GIGAPASCAL).put(UnitsPressure.MILLIBAR, Mul(Rat(Int(1), Pow(10, 7)), Sym("gigapa")));
        c.get(UnitsPressure.GIGAPASCAL).put(UnitsPressure.MILLIPASCAL, Mul(Rat(Int(1), Pow(10, 12)), Sym("gigapa")));
        c.get(UnitsPressure.GIGAPASCAL).put(UnitsPressure.MILLITORR, Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 11))), Sym("gigapa")));
        c.get(UnitsPressure.GIGAPASCAL).put(UnitsPressure.MMHG, Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 17))), Sym("gigapa")));
        c.get(UnitsPressure.GIGAPASCAL).put(UnitsPressure.NANOPASCAL, Mul(Rat(Int(1), Pow(10, 18)), Sym("gigapa")));
        c.get(UnitsPressure.GIGAPASCAL).put(UnitsPressure.PASCAL, Mul(Rat(Int(1), Pow(10, 9)), Sym("gigapa")));
        c.get(UnitsPressure.GIGAPASCAL).put(UnitsPressure.PETAPASCAL, Mul(Pow(10, 6), Sym("gigapa")));
        c.get(UnitsPressure.GIGAPASCAL).put(UnitsPressure.PICOPASCAL, Mul(Rat(Int(1), Pow(10, 21)), Sym("gigapa")));
        c.get(UnitsPressure.GIGAPASCAL).put(UnitsPressure.PSI, Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 18))), Sym("gigapa")));
        c.get(UnitsPressure.GIGAPASCAL).put(UnitsPressure.TERAPASCAL, Mul(Int(1000), Sym("gigapa")));
        c.get(UnitsPressure.GIGAPASCAL).put(UnitsPressure.TORR, Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 8))), Sym("gigapa")));
        c.get(UnitsPressure.GIGAPASCAL).put(UnitsPressure.YOCTOPASCAL, Mul(Rat(Int(1), Pow(10, 33)), Sym("gigapa")));
        c.get(UnitsPressure.GIGAPASCAL).put(UnitsPressure.YOTTAPASCAL, Mul(Pow(10, 15), Sym("gigapa")));
        c.get(UnitsPressure.GIGAPASCAL).put(UnitsPressure.ZEPTOPASCAL, Mul(Rat(Int(1), Pow(10, 30)), Sym("gigapa")));
        c.get(UnitsPressure.GIGAPASCAL).put(UnitsPressure.ZETTAPASCAL, Mul(Pow(10, 12), Sym("gigapa")));
        c.get(UnitsPressure.HECTOPASCAL).put(UnitsPressure.ATHMOSPHERE, Mul(Rat(Int(4053), Int(4)), Sym("hectopa")));
        c.get(UnitsPressure.HECTOPASCAL).put(UnitsPressure.ATTOPASCAL, Mul(Rat(Int(1), Pow(10, 20)), Sym("hectopa")));
        c.get(UnitsPressure.HECTOPASCAL).put(UnitsPressure.BAR, Mul(Int(1000), Sym("hectopa")));
        c.get(UnitsPressure.HECTOPASCAL).put(UnitsPressure.CENTIBAR, Mul(Int(10), Sym("hectopa")));
        c.get(UnitsPressure.HECTOPASCAL).put(UnitsPressure.CENTIPASCAL, Mul(Rat(Int(1), Pow(10, 4)), Sym("hectopa")));
        c.get(UnitsPressure.HECTOPASCAL).put(UnitsPressure.DECAPASCAL, Mul(Rat(Int(1), Int(10)), Sym("hectopa")));
        c.get(UnitsPressure.HECTOPASCAL).put(UnitsPressure.DECIBAR, Mul(Int(100), Sym("hectopa")));
        c.get(UnitsPressure.HECTOPASCAL).put(UnitsPressure.DECIPASCAL, Mul(Rat(Int(1), Int(1000)), Sym("hectopa")));
        c.get(UnitsPressure.HECTOPASCAL).put(UnitsPressure.EXAPASCAL, Mul(Pow(10, 16), Sym("hectopa")));
        c.get(UnitsPressure.HECTOPASCAL).put(UnitsPressure.FEMTOPASCAL, Mul(Rat(Int(1), Pow(10, 17)), Sym("hectopa")));
        c.get(UnitsPressure.HECTOPASCAL).put(UnitsPressure.GIGAPASCAL, Mul(Pow(10, 7), Sym("hectopa")));
        c.get(UnitsPressure.HECTOPASCAL).put(UnitsPressure.KILOBAR, Mul(Pow(10, 6), Sym("hectopa")));
        c.get(UnitsPressure.HECTOPASCAL).put(UnitsPressure.KILOPASCAL, Mul(Int(10), Sym("hectopa")));
        c.get(UnitsPressure.HECTOPASCAL).put(UnitsPressure.MEGABAR, Mul(Pow(10, 9), Sym("hectopa")));
        c.get(UnitsPressure.HECTOPASCAL).put(UnitsPressure.MEGAPASCAL, Mul(Pow(10, 4), Sym("hectopa")));
        c.get(UnitsPressure.HECTOPASCAL).put(UnitsPressure.MICROPASCAL, Mul(Rat(Int(1), Pow(10, 8)), Sym("hectopa")));
        c.get(UnitsPressure.HECTOPASCAL).put(UnitsPressure.MILLIBAR, Sym("hectopa"));
        c.get(UnitsPressure.HECTOPASCAL).put(UnitsPressure.MILLIPASCAL, Mul(Rat(Int(1), Pow(10, 5)), Sym("hectopa")));
        c.get(UnitsPressure.HECTOPASCAL).put(UnitsPressure.MILLITORR, Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 4))), Sym("hectopa")));
        c.get(UnitsPressure.HECTOPASCAL).put(UnitsPressure.MMHG, Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 10))), Sym("hectopa")));
        c.get(UnitsPressure.HECTOPASCAL).put(UnitsPressure.NANOPASCAL, Mul(Rat(Int(1), Pow(10, 11)), Sym("hectopa")));
        c.get(UnitsPressure.HECTOPASCAL).put(UnitsPressure.PASCAL, Mul(Rat(Int(1), Int(100)), Sym("hectopa")));
        c.get(UnitsPressure.HECTOPASCAL).put(UnitsPressure.PETAPASCAL, Mul(Pow(10, 13), Sym("hectopa")));
        c.get(UnitsPressure.HECTOPASCAL).put(UnitsPressure.PICOPASCAL, Mul(Rat(Int(1), Pow(10, 14)), Sym("hectopa")));
        c.get(UnitsPressure.HECTOPASCAL).put(UnitsPressure.PSI, Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 11))), Sym("hectopa")));
        c.get(UnitsPressure.HECTOPASCAL).put(UnitsPressure.TERAPASCAL, Mul(Pow(10, 10), Sym("hectopa")));
        c.get(UnitsPressure.HECTOPASCAL).put(UnitsPressure.TORR, Mul(Rat(Int(4053), Int(3040)), Sym("hectopa")));
        c.get(UnitsPressure.HECTOPASCAL).put(UnitsPressure.YOCTOPASCAL, Mul(Rat(Int(1), Pow(10, 26)), Sym("hectopa")));
        c.get(UnitsPressure.HECTOPASCAL).put(UnitsPressure.YOTTAPASCAL, Mul(Pow(10, 22), Sym("hectopa")));
        c.get(UnitsPressure.HECTOPASCAL).put(UnitsPressure.ZEPTOPASCAL, Mul(Rat(Int(1), Pow(10, 23)), Sym("hectopa")));
        c.get(UnitsPressure.HECTOPASCAL).put(UnitsPressure.ZETTAPASCAL, Mul(Pow(10, 19), Sym("hectopa")));
        c.get(UnitsPressure.KILOBAR).put(UnitsPressure.ATHMOSPHERE, Mul(Rat(Int(4053), Mul(Int(4), Pow(10, 6))), Sym("kbar")));
        c.get(UnitsPressure.KILOBAR).put(UnitsPressure.ATTOPASCAL, Mul(Rat(Int(1), Pow(10, 26)), Sym("kbar")));
        c.get(UnitsPressure.KILOBAR).put(UnitsPressure.BAR, Mul(Rat(Int(1), Int(1000)), Sym("kbar")));
        c.get(UnitsPressure.KILOBAR).put(UnitsPressure.CENTIBAR, Mul(Rat(Int(1), Pow(10, 5)), Sym("kbar")));
        c.get(UnitsPressure.KILOBAR).put(UnitsPressure.CENTIPASCAL, Mul(Rat(Int(1), Pow(10, 10)), Sym("kbar")));
        c.get(UnitsPressure.KILOBAR).put(UnitsPressure.DECAPASCAL, Mul(Rat(Int(1), Pow(10, 7)), Sym("kbar")));
        c.get(UnitsPressure.KILOBAR).put(UnitsPressure.DECIBAR, Mul(Rat(Int(1), Pow(10, 4)), Sym("kbar")));
        c.get(UnitsPressure.KILOBAR).put(UnitsPressure.DECIPASCAL, Mul(Rat(Int(1), Pow(10, 9)), Sym("kbar")));
        c.get(UnitsPressure.KILOBAR).put(UnitsPressure.EXAPASCAL, Mul(Pow(10, 10), Sym("kbar")));
        c.get(UnitsPressure.KILOBAR).put(UnitsPressure.FEMTOPASCAL, Mul(Rat(Int(1), Pow(10, 23)), Sym("kbar")));
        c.get(UnitsPressure.KILOBAR).put(UnitsPressure.GIGAPASCAL, Mul(Int(10), Sym("kbar")));
        c.get(UnitsPressure.KILOBAR).put(UnitsPressure.HECTOPASCAL, Mul(Rat(Int(1), Pow(10, 6)), Sym("kbar")));
        c.get(UnitsPressure.KILOBAR).put(UnitsPressure.KILOPASCAL, Mul(Rat(Int(1), Pow(10, 5)), Sym("kbar")));
        c.get(UnitsPressure.KILOBAR).put(UnitsPressure.MEGABAR, Mul(Int(1000), Sym("kbar")));
        c.get(UnitsPressure.KILOBAR).put(UnitsPressure.MEGAPASCAL, Mul(Rat(Int(1), Int(100)), Sym("kbar")));
        c.get(UnitsPressure.KILOBAR).put(UnitsPressure.MICROPASCAL, Mul(Rat(Int(1), Pow(10, 14)), Sym("kbar")));
        c.get(UnitsPressure.KILOBAR).put(UnitsPressure.MILLIBAR, Mul(Rat(Int(1), Pow(10, 6)), Sym("kbar")));
        c.get(UnitsPressure.KILOBAR).put(UnitsPressure.MILLIPASCAL, Mul(Rat(Int(1), Pow(10, 11)), Sym("kbar")));
        c.get(UnitsPressure.KILOBAR).put(UnitsPressure.MILLITORR, Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 10))), Sym("kbar")));
        c.get(UnitsPressure.KILOBAR).put(UnitsPressure.MMHG, Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 16))), Sym("kbar")));
        c.get(UnitsPressure.KILOBAR).put(UnitsPressure.NANOPASCAL, Mul(Rat(Int(1), Pow(10, 17)), Sym("kbar")));
        c.get(UnitsPressure.KILOBAR).put(UnitsPressure.PASCAL, Mul(Rat(Int(1), Pow(10, 8)), Sym("kbar")));
        c.get(UnitsPressure.KILOBAR).put(UnitsPressure.PETAPASCAL, Mul(Pow(10, 7), Sym("kbar")));
        c.get(UnitsPressure.KILOBAR).put(UnitsPressure.PICOPASCAL, Mul(Rat(Int(1), Pow(10, 20)), Sym("kbar")));
        c.get(UnitsPressure.KILOBAR).put(UnitsPressure.PSI, Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 17))), Sym("kbar")));
        c.get(UnitsPressure.KILOBAR).put(UnitsPressure.TERAPASCAL, Mul(Pow(10, 4), Sym("kbar")));
        c.get(UnitsPressure.KILOBAR).put(UnitsPressure.TORR, Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 7))), Sym("kbar")));
        c.get(UnitsPressure.KILOBAR).put(UnitsPressure.YOCTOPASCAL, Mul(Rat(Int(1), Pow(10, 32)), Sym("kbar")));
        c.get(UnitsPressure.KILOBAR).put(UnitsPressure.YOTTAPASCAL, Mul(Pow(10, 16), Sym("kbar")));
        c.get(UnitsPressure.KILOBAR).put(UnitsPressure.ZEPTOPASCAL, Mul(Rat(Int(1), Pow(10, 29)), Sym("kbar")));
        c.get(UnitsPressure.KILOBAR).put(UnitsPressure.ZETTAPASCAL, Mul(Pow(10, 13), Sym("kbar")));
        c.get(UnitsPressure.KILOPASCAL).put(UnitsPressure.ATHMOSPHERE, Mul(Rat(Int(4053), Int(40)), Sym("kilopa")));
        c.get(UnitsPressure.KILOPASCAL).put(UnitsPressure.ATTOPASCAL, Mul(Rat(Int(1), Pow(10, 21)), Sym("kilopa")));
        c.get(UnitsPressure.KILOPASCAL).put(UnitsPressure.BAR, Mul(Int(100), Sym("kilopa")));
        c.get(UnitsPressure.KILOPASCAL).put(UnitsPressure.CENTIBAR, Sym("kilopa"));
        c.get(UnitsPressure.KILOPASCAL).put(UnitsPressure.CENTIPASCAL, Mul(Rat(Int(1), Pow(10, 5)), Sym("kilopa")));
        c.get(UnitsPressure.KILOPASCAL).put(UnitsPressure.DECAPASCAL, Mul(Rat(Int(1), Int(100)), Sym("kilopa")));
        c.get(UnitsPressure.KILOPASCAL).put(UnitsPressure.DECIBAR, Mul(Int(10), Sym("kilopa")));
        c.get(UnitsPressure.KILOPASCAL).put(UnitsPressure.DECIPASCAL, Mul(Rat(Int(1), Pow(10, 4)), Sym("kilopa")));
        c.get(UnitsPressure.KILOPASCAL).put(UnitsPressure.EXAPASCAL, Mul(Pow(10, 15), Sym("kilopa")));
        c.get(UnitsPressure.KILOPASCAL).put(UnitsPressure.FEMTOPASCAL, Mul(Rat(Int(1), Pow(10, 18)), Sym("kilopa")));
        c.get(UnitsPressure.KILOPASCAL).put(UnitsPressure.GIGAPASCAL, Mul(Pow(10, 6), Sym("kilopa")));
        c.get(UnitsPressure.KILOPASCAL).put(UnitsPressure.HECTOPASCAL, Mul(Rat(Int(1), Int(10)), Sym("kilopa")));
        c.get(UnitsPressure.KILOPASCAL).put(UnitsPressure.KILOBAR, Mul(Pow(10, 5), Sym("kilopa")));
        c.get(UnitsPressure.KILOPASCAL).put(UnitsPressure.MEGABAR, Mul(Pow(10, 8), Sym("kilopa")));
        c.get(UnitsPressure.KILOPASCAL).put(UnitsPressure.MEGAPASCAL, Mul(Int(1000), Sym("kilopa")));
        c.get(UnitsPressure.KILOPASCAL).put(UnitsPressure.MICROPASCAL, Mul(Rat(Int(1), Pow(10, 9)), Sym("kilopa")));
        c.get(UnitsPressure.KILOPASCAL).put(UnitsPressure.MILLIBAR, Mul(Rat(Int(1), Int(10)), Sym("kilopa")));
        c.get(UnitsPressure.KILOPASCAL).put(UnitsPressure.MILLIPASCAL, Mul(Rat(Int(1), Pow(10, 6)), Sym("kilopa")));
        c.get(UnitsPressure.KILOPASCAL).put(UnitsPressure.MILLITORR, Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 5))), Sym("kilopa")));
        c.get(UnitsPressure.KILOPASCAL).put(UnitsPressure.MMHG, Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 11))), Sym("kilopa")));
        c.get(UnitsPressure.KILOPASCAL).put(UnitsPressure.NANOPASCAL, Mul(Rat(Int(1), Pow(10, 12)), Sym("kilopa")));
        c.get(UnitsPressure.KILOPASCAL).put(UnitsPressure.PASCAL, Mul(Rat(Int(1), Int(1000)), Sym("kilopa")));
        c.get(UnitsPressure.KILOPASCAL).put(UnitsPressure.PETAPASCAL, Mul(Pow(10, 12), Sym("kilopa")));
        c.get(UnitsPressure.KILOPASCAL).put(UnitsPressure.PICOPASCAL, Mul(Rat(Int(1), Pow(10, 15)), Sym("kilopa")));
        c.get(UnitsPressure.KILOPASCAL).put(UnitsPressure.PSI, Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 12))), Sym("kilopa")));
        c.get(UnitsPressure.KILOPASCAL).put(UnitsPressure.TERAPASCAL, Mul(Pow(10, 9), Sym("kilopa")));
        c.get(UnitsPressure.KILOPASCAL).put(UnitsPressure.TORR, Mul(Rat(Int(4053), Int(30400)), Sym("kilopa")));
        c.get(UnitsPressure.KILOPASCAL).put(UnitsPressure.YOCTOPASCAL, Mul(Rat(Int(1), Pow(10, 27)), Sym("kilopa")));
        c.get(UnitsPressure.KILOPASCAL).put(UnitsPressure.YOTTAPASCAL, Mul(Pow(10, 21), Sym("kilopa")));
        c.get(UnitsPressure.KILOPASCAL).put(UnitsPressure.ZEPTOPASCAL, Mul(Rat(Int(1), Pow(10, 24)), Sym("kilopa")));
        c.get(UnitsPressure.KILOPASCAL).put(UnitsPressure.ZETTAPASCAL, Mul(Pow(10, 18), Sym("kilopa")));
        c.get(UnitsPressure.MEGABAR).put(UnitsPressure.ATHMOSPHERE, Mul(Rat(Int(4053), Mul(Int(4), Pow(10, 9))), Sym("megabar")));
        c.get(UnitsPressure.MEGABAR).put(UnitsPressure.ATTOPASCAL, Mul(Rat(Int(1), Pow(10, 29)), Sym("megabar")));
        c.get(UnitsPressure.MEGABAR).put(UnitsPressure.BAR, Mul(Rat(Int(1), Pow(10, 6)), Sym("megabar")));
        c.get(UnitsPressure.MEGABAR).put(UnitsPressure.CENTIBAR, Mul(Rat(Int(1), Pow(10, 8)), Sym("megabar")));
        c.get(UnitsPressure.MEGABAR).put(UnitsPressure.CENTIPASCAL, Mul(Rat(Int(1), Pow(10, 13)), Sym("megabar")));
        c.get(UnitsPressure.MEGABAR).put(UnitsPressure.DECAPASCAL, Mul(Rat(Int(1), Pow(10, 10)), Sym("megabar")));
        c.get(UnitsPressure.MEGABAR).put(UnitsPressure.DECIBAR, Mul(Rat(Int(1), Pow(10, 7)), Sym("megabar")));
        c.get(UnitsPressure.MEGABAR).put(UnitsPressure.DECIPASCAL, Mul(Rat(Int(1), Pow(10, 12)), Sym("megabar")));
        c.get(UnitsPressure.MEGABAR).put(UnitsPressure.EXAPASCAL, Mul(Pow(10, 7), Sym("megabar")));
        c.get(UnitsPressure.MEGABAR).put(UnitsPressure.FEMTOPASCAL, Mul(Rat(Int(1), Pow(10, 26)), Sym("megabar")));
        c.get(UnitsPressure.MEGABAR).put(UnitsPressure.GIGAPASCAL, Mul(Rat(Int(1), Int(100)), Sym("megabar")));
        c.get(UnitsPressure.MEGABAR).put(UnitsPressure.HECTOPASCAL, Mul(Rat(Int(1), Pow(10, 9)), Sym("megabar")));
        c.get(UnitsPressure.MEGABAR).put(UnitsPressure.KILOBAR, Mul(Rat(Int(1), Int(1000)), Sym("megabar")));
        c.get(UnitsPressure.MEGABAR).put(UnitsPressure.KILOPASCAL, Mul(Rat(Int(1), Pow(10, 8)), Sym("megabar")));
        c.get(UnitsPressure.MEGABAR).put(UnitsPressure.MEGAPASCAL, Mul(Rat(Int(1), Pow(10, 5)), Sym("megabar")));
        c.get(UnitsPressure.MEGABAR).put(UnitsPressure.MICROPASCAL, Mul(Rat(Int(1), Pow(10, 17)), Sym("megabar")));
        c.get(UnitsPressure.MEGABAR).put(UnitsPressure.MILLIBAR, Mul(Rat(Int(1), Pow(10, 9)), Sym("megabar")));
        c.get(UnitsPressure.MEGABAR).put(UnitsPressure.MILLIPASCAL, Mul(Rat(Int(1), Pow(10, 14)), Sym("megabar")));
        c.get(UnitsPressure.MEGABAR).put(UnitsPressure.MILLITORR, Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 13))), Sym("megabar")));
        c.get(UnitsPressure.MEGABAR).put(UnitsPressure.MMHG, Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 19))), Sym("megabar")));
        c.get(UnitsPressure.MEGABAR).put(UnitsPressure.NANOPASCAL, Mul(Rat(Int(1), Pow(10, 20)), Sym("megabar")));
        c.get(UnitsPressure.MEGABAR).put(UnitsPressure.PASCAL, Mul(Rat(Int(1), Pow(10, 11)), Sym("megabar")));
        c.get(UnitsPressure.MEGABAR).put(UnitsPressure.PETAPASCAL, Mul(Pow(10, 4), Sym("megabar")));
        c.get(UnitsPressure.MEGABAR).put(UnitsPressure.PICOPASCAL, Mul(Rat(Int(1), Pow(10, 23)), Sym("megabar")));
        c.get(UnitsPressure.MEGABAR).put(UnitsPressure.PSI, Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 20))), Sym("megabar")));
        c.get(UnitsPressure.MEGABAR).put(UnitsPressure.TERAPASCAL, Mul(Int(10), Sym("megabar")));
        c.get(UnitsPressure.MEGABAR).put(UnitsPressure.TORR, Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 10))), Sym("megabar")));
        c.get(UnitsPressure.MEGABAR).put(UnitsPressure.YOCTOPASCAL, Mul(Rat(Int(1), Pow(10, 35)), Sym("megabar")));
        c.get(UnitsPressure.MEGABAR).put(UnitsPressure.YOTTAPASCAL, Mul(Pow(10, 13), Sym("megabar")));
        c.get(UnitsPressure.MEGABAR).put(UnitsPressure.ZEPTOPASCAL, Mul(Rat(Int(1), Pow(10, 32)), Sym("megabar")));
        c.get(UnitsPressure.MEGABAR).put(UnitsPressure.ZETTAPASCAL, Mul(Pow(10, 10), Sym("megabar")));
        c.get(UnitsPressure.MEGAPASCAL).put(UnitsPressure.ATHMOSPHERE, Mul(Rat(Int(4053), Mul(Int(4), Pow(10, 4))), Sym("megapa")));
        c.get(UnitsPressure.MEGAPASCAL).put(UnitsPressure.ATTOPASCAL, Mul(Rat(Int(1), Pow(10, 24)), Sym("megapa")));
        c.get(UnitsPressure.MEGAPASCAL).put(UnitsPressure.BAR, Mul(Rat(Int(1), Int(10)), Sym("megapa")));
        c.get(UnitsPressure.MEGAPASCAL).put(UnitsPressure.CENTIBAR, Mul(Rat(Int(1), Int(1000)), Sym("megapa")));
        c.get(UnitsPressure.MEGAPASCAL).put(UnitsPressure.CENTIPASCAL, Mul(Rat(Int(1), Pow(10, 8)), Sym("megapa")));
        c.get(UnitsPressure.MEGAPASCAL).put(UnitsPressure.DECAPASCAL, Mul(Rat(Int(1), Pow(10, 5)), Sym("megapa")));
        c.get(UnitsPressure.MEGAPASCAL).put(UnitsPressure.DECIBAR, Mul(Rat(Int(1), Int(100)), Sym("megapa")));
        c.get(UnitsPressure.MEGAPASCAL).put(UnitsPressure.DECIPASCAL, Mul(Rat(Int(1), Pow(10, 7)), Sym("megapa")));
        c.get(UnitsPressure.MEGAPASCAL).put(UnitsPressure.EXAPASCAL, Mul(Pow(10, 12), Sym("megapa")));
        c.get(UnitsPressure.MEGAPASCAL).put(UnitsPressure.FEMTOPASCAL, Mul(Rat(Int(1), Pow(10, 21)), Sym("megapa")));
        c.get(UnitsPressure.MEGAPASCAL).put(UnitsPressure.GIGAPASCAL, Mul(Int(1000), Sym("megapa")));
        c.get(UnitsPressure.MEGAPASCAL).put(UnitsPressure.HECTOPASCAL, Mul(Rat(Int(1), Pow(10, 4)), Sym("megapa")));
        c.get(UnitsPressure.MEGAPASCAL).put(UnitsPressure.KILOBAR, Mul(Int(100), Sym("megapa")));
        c.get(UnitsPressure.MEGAPASCAL).put(UnitsPressure.KILOPASCAL, Mul(Rat(Int(1), Int(1000)), Sym("megapa")));
        c.get(UnitsPressure.MEGAPASCAL).put(UnitsPressure.MEGABAR, Mul(Pow(10, 5), Sym("megapa")));
        c.get(UnitsPressure.MEGAPASCAL).put(UnitsPressure.MICROPASCAL, Mul(Rat(Int(1), Pow(10, 12)), Sym("megapa")));
        c.get(UnitsPressure.MEGAPASCAL).put(UnitsPressure.MILLIBAR, Mul(Rat(Int(1), Pow(10, 4)), Sym("megapa")));
        c.get(UnitsPressure.MEGAPASCAL).put(UnitsPressure.MILLIPASCAL, Mul(Rat(Int(1), Pow(10, 9)), Sym("megapa")));
        c.get(UnitsPressure.MEGAPASCAL).put(UnitsPressure.MILLITORR, Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 8))), Sym("megapa")));
        c.get(UnitsPressure.MEGAPASCAL).put(UnitsPressure.MMHG, Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 14))), Sym("megapa")));
        c.get(UnitsPressure.MEGAPASCAL).put(UnitsPressure.NANOPASCAL, Mul(Rat(Int(1), Pow(10, 15)), Sym("megapa")));
        c.get(UnitsPressure.MEGAPASCAL).put(UnitsPressure.PASCAL, Mul(Rat(Int(1), Pow(10, 6)), Sym("megapa")));
        c.get(UnitsPressure.MEGAPASCAL).put(UnitsPressure.PETAPASCAL, Mul(Pow(10, 9), Sym("megapa")));
        c.get(UnitsPressure.MEGAPASCAL).put(UnitsPressure.PICOPASCAL, Mul(Rat(Int(1), Pow(10, 18)), Sym("megapa")));
        c.get(UnitsPressure.MEGAPASCAL).put(UnitsPressure.PSI, Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 15))), Sym("megapa")));
        c.get(UnitsPressure.MEGAPASCAL).put(UnitsPressure.TERAPASCAL, Mul(Pow(10, 6), Sym("megapa")));
        c.get(UnitsPressure.MEGAPASCAL).put(UnitsPressure.TORR, Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 5))), Sym("megapa")));
        c.get(UnitsPressure.MEGAPASCAL).put(UnitsPressure.YOCTOPASCAL, Mul(Rat(Int(1), Pow(10, 30)), Sym("megapa")));
        c.get(UnitsPressure.MEGAPASCAL).put(UnitsPressure.YOTTAPASCAL, Mul(Pow(10, 18), Sym("megapa")));
        c.get(UnitsPressure.MEGAPASCAL).put(UnitsPressure.ZEPTOPASCAL, Mul(Rat(Int(1), Pow(10, 27)), Sym("megapa")));
        c.get(UnitsPressure.MEGAPASCAL).put(UnitsPressure.ZETTAPASCAL, Mul(Pow(10, 15), Sym("megapa")));
        c.get(UnitsPressure.MICROPASCAL).put(UnitsPressure.ATHMOSPHERE, Mul(Mul(Int(101325), Pow(10, 6)), Sym("micropa")));
        c.get(UnitsPressure.MICROPASCAL).put(UnitsPressure.ATTOPASCAL, Mul(Rat(Int(1), Pow(10, 12)), Sym("micropa")));
        c.get(UnitsPressure.MICROPASCAL).put(UnitsPressure.BAR, Mul(Pow(10, 11), Sym("micropa")));
        c.get(UnitsPressure.MICROPASCAL).put(UnitsPressure.CENTIBAR, Mul(Pow(10, 9), Sym("micropa")));
        c.get(UnitsPressure.MICROPASCAL).put(UnitsPressure.CENTIPASCAL, Mul(Pow(10, 4), Sym("micropa")));
        c.get(UnitsPressure.MICROPASCAL).put(UnitsPressure.DECAPASCAL, Mul(Pow(10, 7), Sym("micropa")));
        c.get(UnitsPressure.MICROPASCAL).put(UnitsPressure.DECIBAR, Mul(Pow(10, 10), Sym("micropa")));
        c.get(UnitsPressure.MICROPASCAL).put(UnitsPressure.DECIPASCAL, Mul(Pow(10, 5), Sym("micropa")));
        c.get(UnitsPressure.MICROPASCAL).put(UnitsPressure.EXAPASCAL, Mul(Pow(10, 24), Sym("micropa")));
        c.get(UnitsPressure.MICROPASCAL).put(UnitsPressure.FEMTOPASCAL, Mul(Rat(Int(1), Pow(10, 9)), Sym("micropa")));
        c.get(UnitsPressure.MICROPASCAL).put(UnitsPressure.GIGAPASCAL, Mul(Pow(10, 15), Sym("micropa")));
        c.get(UnitsPressure.MICROPASCAL).put(UnitsPressure.HECTOPASCAL, Mul(Pow(10, 8), Sym("micropa")));
        c.get(UnitsPressure.MICROPASCAL).put(UnitsPressure.KILOBAR, Mul(Pow(10, 14), Sym("micropa")));
        c.get(UnitsPressure.MICROPASCAL).put(UnitsPressure.KILOPASCAL, Mul(Pow(10, 9), Sym("micropa")));
        c.get(UnitsPressure.MICROPASCAL).put(UnitsPressure.MEGABAR, Mul(Pow(10, 17), Sym("micropa")));
        c.get(UnitsPressure.MICROPASCAL).put(UnitsPressure.MEGAPASCAL, Mul(Pow(10, 12), Sym("micropa")));
        c.get(UnitsPressure.MICROPASCAL).put(UnitsPressure.MILLIBAR, Mul(Pow(10, 8), Sym("micropa")));
        c.get(UnitsPressure.MICROPASCAL).put(UnitsPressure.MILLIPASCAL, Mul(Int(1000), Sym("micropa")));
        c.get(UnitsPressure.MICROPASCAL).put(UnitsPressure.MILLITORR, Mul(Rat(Int(2533125), Int(19)), Sym("micropa")));
        c.get(UnitsPressure.MICROPASCAL).put(UnitsPressure.MMHG, Mul(Rat(Int("26664477483"), Int(200)), Sym("micropa")));
        c.get(UnitsPressure.MICROPASCAL).put(UnitsPressure.NANOPASCAL, Mul(Rat(Int(1), Int(1000)), Sym("micropa")));
        c.get(UnitsPressure.MICROPASCAL).put(UnitsPressure.PASCAL, Mul(Pow(10, 6), Sym("micropa")));
        c.get(UnitsPressure.MICROPASCAL).put(UnitsPressure.PETAPASCAL, Mul(Pow(10, 21), Sym("micropa")));
        c.get(UnitsPressure.MICROPASCAL).put(UnitsPressure.PICOPASCAL, Mul(Rat(Int(1), Pow(10, 6)), Sym("micropa")));
        c.get(UnitsPressure.MICROPASCAL).put(UnitsPressure.PSI, Mul(Rat(Int("172368932329209"), Int(25000)), Sym("micropa")));
        c.get(UnitsPressure.MICROPASCAL).put(UnitsPressure.TERAPASCAL, Mul(Pow(10, 18), Sym("micropa")));
        c.get(UnitsPressure.MICROPASCAL).put(UnitsPressure.TORR, Mul(Rat(Int("2533125000"), Int(19)), Sym("micropa")));
        c.get(UnitsPressure.MICROPASCAL).put(UnitsPressure.YOCTOPASCAL, Mul(Rat(Int(1), Pow(10, 18)), Sym("micropa")));
        c.get(UnitsPressure.MICROPASCAL).put(UnitsPressure.YOTTAPASCAL, Mul(Pow(10, 30), Sym("micropa")));
        c.get(UnitsPressure.MICROPASCAL).put(UnitsPressure.ZEPTOPASCAL, Mul(Rat(Int(1), Pow(10, 15)), Sym("micropa")));
        c.get(UnitsPressure.MICROPASCAL).put(UnitsPressure.ZETTAPASCAL, Mul(Pow(10, 27), Sym("micropa")));
        c.get(UnitsPressure.MILLIBAR).put(UnitsPressure.ATHMOSPHERE, Mul(Rat(Int(4053), Int(4)), Sym("mbar")));
        c.get(UnitsPressure.MILLIBAR).put(UnitsPressure.ATTOPASCAL, Mul(Rat(Int(1), Pow(10, 20)), Sym("mbar")));
        c.get(UnitsPressure.MILLIBAR).put(UnitsPressure.BAR, Mul(Int(1000), Sym("mbar")));
        c.get(UnitsPressure.MILLIBAR).put(UnitsPressure.CENTIBAR, Mul(Int(10), Sym("mbar")));
        c.get(UnitsPressure.MILLIBAR).put(UnitsPressure.CENTIPASCAL, Mul(Rat(Int(1), Pow(10, 4)), Sym("mbar")));
        c.get(UnitsPressure.MILLIBAR).put(UnitsPressure.DECAPASCAL, Mul(Rat(Int(1), Int(10)), Sym("mbar")));
        c.get(UnitsPressure.MILLIBAR).put(UnitsPressure.DECIBAR, Mul(Int(100), Sym("mbar")));
        c.get(UnitsPressure.MILLIBAR).put(UnitsPressure.DECIPASCAL, Mul(Rat(Int(1), Int(1000)), Sym("mbar")));
        c.get(UnitsPressure.MILLIBAR).put(UnitsPressure.EXAPASCAL, Mul(Pow(10, 16), Sym("mbar")));
        c.get(UnitsPressure.MILLIBAR).put(UnitsPressure.FEMTOPASCAL, Mul(Rat(Int(1), Pow(10, 17)), Sym("mbar")));
        c.get(UnitsPressure.MILLIBAR).put(UnitsPressure.GIGAPASCAL, Mul(Pow(10, 7), Sym("mbar")));
        c.get(UnitsPressure.MILLIBAR).put(UnitsPressure.HECTOPASCAL, Sym("mbar"));
        c.get(UnitsPressure.MILLIBAR).put(UnitsPressure.KILOBAR, Mul(Pow(10, 6), Sym("mbar")));
        c.get(UnitsPressure.MILLIBAR).put(UnitsPressure.KILOPASCAL, Mul(Int(10), Sym("mbar")));
        c.get(UnitsPressure.MILLIBAR).put(UnitsPressure.MEGABAR, Mul(Pow(10, 9), Sym("mbar")));
        c.get(UnitsPressure.MILLIBAR).put(UnitsPressure.MEGAPASCAL, Mul(Pow(10, 4), Sym("mbar")));
        c.get(UnitsPressure.MILLIBAR).put(UnitsPressure.MICROPASCAL, Mul(Rat(Int(1), Pow(10, 8)), Sym("mbar")));
        c.get(UnitsPressure.MILLIBAR).put(UnitsPressure.MILLIPASCAL, Mul(Rat(Int(1), Pow(10, 5)), Sym("mbar")));
        c.get(UnitsPressure.MILLIBAR).put(UnitsPressure.MILLITORR, Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 4))), Sym("mbar")));
        c.get(UnitsPressure.MILLIBAR).put(UnitsPressure.MMHG, Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 10))), Sym("mbar")));
        c.get(UnitsPressure.MILLIBAR).put(UnitsPressure.NANOPASCAL, Mul(Rat(Int(1), Pow(10, 11)), Sym("mbar")));
        c.get(UnitsPressure.MILLIBAR).put(UnitsPressure.PASCAL, Mul(Rat(Int(1), Int(100)), Sym("mbar")));
        c.get(UnitsPressure.MILLIBAR).put(UnitsPressure.PETAPASCAL, Mul(Pow(10, 13), Sym("mbar")));
        c.get(UnitsPressure.MILLIBAR).put(UnitsPressure.PICOPASCAL, Mul(Rat(Int(1), Pow(10, 14)), Sym("mbar")));
        c.get(UnitsPressure.MILLIBAR).put(UnitsPressure.PSI, Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 11))), Sym("mbar")));
        c.get(UnitsPressure.MILLIBAR).put(UnitsPressure.TERAPASCAL, Mul(Pow(10, 10), Sym("mbar")));
        c.get(UnitsPressure.MILLIBAR).put(UnitsPressure.TORR, Mul(Rat(Int(4053), Int(3040)), Sym("mbar")));
        c.get(UnitsPressure.MILLIBAR).put(UnitsPressure.YOCTOPASCAL, Mul(Rat(Int(1), Pow(10, 26)), Sym("mbar")));
        c.get(UnitsPressure.MILLIBAR).put(UnitsPressure.YOTTAPASCAL, Mul(Pow(10, 22), Sym("mbar")));
        c.get(UnitsPressure.MILLIBAR).put(UnitsPressure.ZEPTOPASCAL, Mul(Rat(Int(1), Pow(10, 23)), Sym("mbar")));
        c.get(UnitsPressure.MILLIBAR).put(UnitsPressure.ZETTAPASCAL, Mul(Pow(10, 19), Sym("mbar")));
        c.get(UnitsPressure.MILLIPASCAL).put(UnitsPressure.ATHMOSPHERE, Mul(Int(101325000), Sym("millipa")));
        c.get(UnitsPressure.MILLIPASCAL).put(UnitsPressure.ATTOPASCAL, Mul(Rat(Int(1), Pow(10, 15)), Sym("millipa")));
        c.get(UnitsPressure.MILLIPASCAL).put(UnitsPressure.BAR, Mul(Pow(10, 8), Sym("millipa")));
        c.get(UnitsPressure.MILLIPASCAL).put(UnitsPressure.CENTIBAR, Mul(Pow(10, 6), Sym("millipa")));
        c.get(UnitsPressure.MILLIPASCAL).put(UnitsPressure.CENTIPASCAL, Mul(Int(10), Sym("millipa")));
        c.get(UnitsPressure.MILLIPASCAL).put(UnitsPressure.DECAPASCAL, Mul(Pow(10, 4), Sym("millipa")));
        c.get(UnitsPressure.MILLIPASCAL).put(UnitsPressure.DECIBAR, Mul(Pow(10, 7), Sym("millipa")));
        c.get(UnitsPressure.MILLIPASCAL).put(UnitsPressure.DECIPASCAL, Mul(Int(100), Sym("millipa")));
        c.get(UnitsPressure.MILLIPASCAL).put(UnitsPressure.EXAPASCAL, Mul(Pow(10, 21), Sym("millipa")));
        c.get(UnitsPressure.MILLIPASCAL).put(UnitsPressure.FEMTOPASCAL, Mul(Rat(Int(1), Pow(10, 12)), Sym("millipa")));
        c.get(UnitsPressure.MILLIPASCAL).put(UnitsPressure.GIGAPASCAL, Mul(Pow(10, 12), Sym("millipa")));
        c.get(UnitsPressure.MILLIPASCAL).put(UnitsPressure.HECTOPASCAL, Mul(Pow(10, 5), Sym("millipa")));
        c.get(UnitsPressure.MILLIPASCAL).put(UnitsPressure.KILOBAR, Mul(Pow(10, 11), Sym("millipa")));
        c.get(UnitsPressure.MILLIPASCAL).put(UnitsPressure.KILOPASCAL, Mul(Pow(10, 6), Sym("millipa")));
        c.get(UnitsPressure.MILLIPASCAL).put(UnitsPressure.MEGABAR, Mul(Pow(10, 14), Sym("millipa")));
        c.get(UnitsPressure.MILLIPASCAL).put(UnitsPressure.MEGAPASCAL, Mul(Pow(10, 9), Sym("millipa")));
        c.get(UnitsPressure.MILLIPASCAL).put(UnitsPressure.MICROPASCAL, Mul(Rat(Int(1), Int(1000)), Sym("millipa")));
        c.get(UnitsPressure.MILLIPASCAL).put(UnitsPressure.MILLIBAR, Mul(Pow(10, 5), Sym("millipa")));
        c.get(UnitsPressure.MILLIPASCAL).put(UnitsPressure.MILLITORR, Mul(Rat(Int(20265), Int(152)), Sym("millipa")));
        c.get(UnitsPressure.MILLIPASCAL).put(UnitsPressure.MMHG, Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 5))), Sym("millipa")));
        c.get(UnitsPressure.MILLIPASCAL).put(UnitsPressure.NANOPASCAL, Mul(Rat(Int(1), Pow(10, 6)), Sym("millipa")));
        c.get(UnitsPressure.MILLIPASCAL).put(UnitsPressure.PASCAL, Mul(Int(1000), Sym("millipa")));
        c.get(UnitsPressure.MILLIPASCAL).put(UnitsPressure.PETAPASCAL, Mul(Pow(10, 18), Sym("millipa")));
        c.get(UnitsPressure.MILLIPASCAL).put(UnitsPressure.PICOPASCAL, Mul(Rat(Int(1), Pow(10, 9)), Sym("millipa")));
        c.get(UnitsPressure.MILLIPASCAL).put(UnitsPressure.PSI, Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 6))), Sym("millipa")));
        c.get(UnitsPressure.MILLIPASCAL).put(UnitsPressure.TERAPASCAL, Mul(Pow(10, 15), Sym("millipa")));
        c.get(UnitsPressure.MILLIPASCAL).put(UnitsPressure.TORR, Mul(Rat(Int(2533125), Int(19)), Sym("millipa")));
        c.get(UnitsPressure.MILLIPASCAL).put(UnitsPressure.YOCTOPASCAL, Mul(Rat(Int(1), Pow(10, 21)), Sym("millipa")));
        c.get(UnitsPressure.MILLIPASCAL).put(UnitsPressure.YOTTAPASCAL, Mul(Pow(10, 27), Sym("millipa")));
        c.get(UnitsPressure.MILLIPASCAL).put(UnitsPressure.ZEPTOPASCAL, Mul(Rat(Int(1), Pow(10, 18)), Sym("millipa")));
        c.get(UnitsPressure.MILLIPASCAL).put(UnitsPressure.ZETTAPASCAL, Mul(Pow(10, 24), Sym("millipa")));
        c.get(UnitsPressure.MILLITORR).put(UnitsPressure.ATHMOSPHERE, Mul(Mul(Int(76), Pow(10, 4)), Sym("mtorr")));
        c.get(UnitsPressure.MILLITORR).put(UnitsPressure.ATTOPASCAL, Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 12))), Sym("mtorr")));
        c.get(UnitsPressure.MILLITORR).put(UnitsPressure.BAR, Mul(Rat(Mul(Int(304), Pow(10, 7)), Int(4053)), Sym("mtorr")));
        c.get(UnitsPressure.MILLITORR).put(UnitsPressure.CENTIBAR, Mul(Rat(Mul(Int(304), Pow(10, 5)), Int(4053)), Sym("mtorr")));
        c.get(UnitsPressure.MILLITORR).put(UnitsPressure.CENTIPASCAL, Mul(Rat(Int(304), Int(4053)), Sym("mtorr")));
        c.get(UnitsPressure.MILLITORR).put(UnitsPressure.DECAPASCAL, Mul(Rat(Int(304000), Int(4053)), Sym("mtorr")));
        c.get(UnitsPressure.MILLITORR).put(UnitsPressure.DECIBAR, Mul(Rat(Mul(Int(304), Pow(10, 6)), Int(4053)), Sym("mtorr")));
        c.get(UnitsPressure.MILLITORR).put(UnitsPressure.DECIPASCAL, Mul(Rat(Int(3040), Int(4053)), Sym("mtorr")));
        c.get(UnitsPressure.MILLITORR).put(UnitsPressure.EXAPASCAL, Mul(Rat(Mul(Int(304), Pow(10, 20)), Int(4053)), Sym("mtorr")));
        c.get(UnitsPressure.MILLITORR).put(UnitsPressure.FEMTOPASCAL, Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 9))), Sym("mtorr")));
        c.get(UnitsPressure.MILLITORR).put(UnitsPressure.GIGAPASCAL, Mul(Rat(Mul(Int(304), Pow(10, 11)), Int(4053)), Sym("mtorr")));
        c.get(UnitsPressure.MILLITORR).put(UnitsPressure.HECTOPASCAL, Mul(Rat(Mul(Int(304), Pow(10, 4)), Int(4053)), Sym("mtorr")));
        c.get(UnitsPressure.MILLITORR).put(UnitsPressure.KILOBAR, Mul(Rat(Mul(Int(304), Pow(10, 10)), Int(4053)), Sym("mtorr")));
        c.get(UnitsPressure.MILLITORR).put(UnitsPressure.KILOPASCAL, Mul(Rat(Mul(Int(304), Pow(10, 5)), Int(4053)), Sym("mtorr")));
        c.get(UnitsPressure.MILLITORR).put(UnitsPressure.MEGABAR, Mul(Rat(Mul(Int(304), Pow(10, 13)), Int(4053)), Sym("mtorr")));
        c.get(UnitsPressure.MILLITORR).put(UnitsPressure.MEGAPASCAL, Mul(Rat(Mul(Int(304), Pow(10, 8)), Int(4053)), Sym("mtorr")));
        c.get(UnitsPressure.MILLITORR).put(UnitsPressure.MICROPASCAL, Mul(Rat(Int(19), Int(2533125)), Sym("mtorr")));
        c.get(UnitsPressure.MILLITORR).put(UnitsPressure.MILLIBAR, Mul(Rat(Mul(Int(304), Pow(10, 4)), Int(4053)), Sym("mtorr")));
        c.get(UnitsPressure.MILLITORR).put(UnitsPressure.MILLIPASCAL, Mul(Rat(Int(152), Int(20265)), Sym("mtorr")));
        c.get(UnitsPressure.MILLITORR).put(UnitsPressure.MMHG, Mul(Rat(Int("24125003437"), Int(24125000)), Sym("mtorr")));
        c.get(UnitsPressure.MILLITORR).put(UnitsPressure.NANOPASCAL, Mul(Rat(Int(19), Int("2533125000")), Sym("mtorr")));
        c.get(UnitsPressure.MILLITORR).put(UnitsPressure.PASCAL, Mul(Rat(Int(30400), Int(4053)), Sym("mtorr")));
        c.get(UnitsPressure.MILLITORR).put(UnitsPressure.PETAPASCAL, Mul(Rat(Mul(Int(304), Pow(10, 17)), Int(4053)), Sym("mtorr")));
        c.get(UnitsPressure.MILLITORR).put(UnitsPressure.PICOPASCAL, Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 6))), Sym("mtorr")));
        c.get(UnitsPressure.MILLITORR).put(UnitsPressure.PSI, Mul(Rat(Int("155952843535951"), Int("3015625000")), Sym("mtorr")));
        c.get(UnitsPressure.MILLITORR).put(UnitsPressure.TERAPASCAL, Mul(Rat(Mul(Int(304), Pow(10, 14)), Int(4053)), Sym("mtorr")));
        c.get(UnitsPressure.MILLITORR).put(UnitsPressure.TORR, Mul(Int(1000), Sym("mtorr")));
        c.get(UnitsPressure.MILLITORR).put(UnitsPressure.YOCTOPASCAL, Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 18))), Sym("mtorr")));
        c.get(UnitsPressure.MILLITORR).put(UnitsPressure.YOTTAPASCAL, Mul(Rat(Mul(Int(304), Pow(10, 26)), Int(4053)), Sym("mtorr")));
        c.get(UnitsPressure.MILLITORR).put(UnitsPressure.ZEPTOPASCAL, Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 15))), Sym("mtorr")));
        c.get(UnitsPressure.MILLITORR).put(UnitsPressure.ZETTAPASCAL, Mul(Rat(Mul(Int(304), Pow(10, 23)), Int(4053)), Sym("mtorr")));
        c.get(UnitsPressure.MMHG).put(UnitsPressure.ATHMOSPHERE, Mul(Rat(Mul(Int(965), Pow(10, 9)), Int(1269737023)), Sym("mmhg")));
        c.get(UnitsPressure.MMHG).put(UnitsPressure.ATTOPASCAL, Mul(Rat(Int(1), Mul(Int("133322387415"), Pow(10, 9))), Sym("mmhg")));
        c.get(UnitsPressure.MMHG).put(UnitsPressure.BAR, Mul(Rat(Mul(Int(2), Pow(10, 13)), Int("26664477483")), Sym("mmhg")));
        c.get(UnitsPressure.MMHG).put(UnitsPressure.CENTIBAR, Mul(Rat(Mul(Int(2), Pow(10, 11)), Int("26664477483")), Sym("mmhg")));
        c.get(UnitsPressure.MMHG).put(UnitsPressure.CENTIPASCAL, Mul(Rat(Mul(Int(2), Pow(10, 6)), Int("26664477483")), Sym("mmhg")));
        c.get(UnitsPressure.MMHG).put(UnitsPressure.DECAPASCAL, Mul(Rat(Mul(Int(2), Pow(10, 9)), Int("26664477483")), Sym("mmhg")));
        c.get(UnitsPressure.MMHG).put(UnitsPressure.DECIBAR, Mul(Rat(Mul(Int(2), Pow(10, 12)), Int("26664477483")), Sym("mmhg")));
        c.get(UnitsPressure.MMHG).put(UnitsPressure.DECIPASCAL, Mul(Rat(Mul(Int(2), Pow(10, 7)), Int("26664477483")), Sym("mmhg")));
        c.get(UnitsPressure.MMHG).put(UnitsPressure.EXAPASCAL, Mul(Rat(Mul(Int(2), Pow(10, 26)), Int("26664477483")), Sym("mmhg")));
        c.get(UnitsPressure.MMHG).put(UnitsPressure.FEMTOPASCAL, Mul(Rat(Int(1), Mul(Int("133322387415"), Pow(10, 6))), Sym("mmhg")));
        c.get(UnitsPressure.MMHG).put(UnitsPressure.GIGAPASCAL, Mul(Rat(Mul(Int(2), Pow(10, 17)), Int("26664477483")), Sym("mmhg")));
        c.get(UnitsPressure.MMHG).put(UnitsPressure.HECTOPASCAL, Mul(Rat(Mul(Int(2), Pow(10, 10)), Int("26664477483")), Sym("mmhg")));
        c.get(UnitsPressure.MMHG).put(UnitsPressure.KILOBAR, Mul(Rat(Mul(Int(2), Pow(10, 16)), Int("26664477483")), Sym("mmhg")));
        c.get(UnitsPressure.MMHG).put(UnitsPressure.KILOPASCAL, Mul(Rat(Mul(Int(2), Pow(10, 11)), Int("26664477483")), Sym("mmhg")));
        c.get(UnitsPressure.MMHG).put(UnitsPressure.MEGABAR, Mul(Rat(Mul(Int(2), Pow(10, 19)), Int("26664477483")), Sym("mmhg")));
        c.get(UnitsPressure.MMHG).put(UnitsPressure.MEGAPASCAL, Mul(Rat(Mul(Int(2), Pow(10, 14)), Int("26664477483")), Sym("mmhg")));
        c.get(UnitsPressure.MMHG).put(UnitsPressure.MICROPASCAL, Mul(Rat(Int(200), Int("26664477483")), Sym("mmhg")));
        c.get(UnitsPressure.MMHG).put(UnitsPressure.MILLIBAR, Mul(Rat(Mul(Int(2), Pow(10, 10)), Int("26664477483")), Sym("mmhg")));
        c.get(UnitsPressure.MMHG).put(UnitsPressure.MILLIPASCAL, Mul(Rat(Mul(Int(2), Pow(10, 5)), Int("26664477483")), Sym("mmhg")));
        c.get(UnitsPressure.MMHG).put(UnitsPressure.MILLITORR, Mul(Rat(Int(24125000), Int("24125003437")), Sym("mmhg")));
        c.get(UnitsPressure.MMHG).put(UnitsPressure.NANOPASCAL, Mul(Rat(Int(1), Int("133322387415")), Sym("mmhg")));
        c.get(UnitsPressure.MMHG).put(UnitsPressure.PASCAL, Mul(Rat(Mul(Int(2), Pow(10, 8)), Int("26664477483")), Sym("mmhg")));
        c.get(UnitsPressure.MMHG).put(UnitsPressure.PETAPASCAL, Mul(Rat(Mul(Int(2), Pow(10, 23)), Int("26664477483")), Sym("mmhg")));
        c.get(UnitsPressure.MMHG).put(UnitsPressure.PICOPASCAL, Mul(Rat(Int(1), Int("133322387415000")), Sym("mmhg")));
        c.get(UnitsPressure.MMHG).put(UnitsPressure.PSI, Mul(Rat(Int("8208044396629"), Int("158717127875")), Sym("mmhg")));
        c.get(UnitsPressure.MMHG).put(UnitsPressure.TERAPASCAL, Mul(Rat(Mul(Int(2), Pow(10, 20)), Int("26664477483")), Sym("mmhg")));
        c.get(UnitsPressure.MMHG).put(UnitsPressure.TORR, Mul(Rat(Mul(Int(24125), Pow(10, 6)), Int("24125003437")), Sym("mmhg")));
        c.get(UnitsPressure.MMHG).put(UnitsPressure.YOCTOPASCAL, Mul(Rat(Int(1), Mul(Int("133322387415"), Pow(10, 15))), Sym("mmhg")));
        c.get(UnitsPressure.MMHG).put(UnitsPressure.YOTTAPASCAL, Mul(Rat(Mul(Int(2), Pow(10, 32)), Int("26664477483")), Sym("mmhg")));
        c.get(UnitsPressure.MMHG).put(UnitsPressure.ZEPTOPASCAL, Mul(Rat(Int(1), Mul(Int("133322387415"), Pow(10, 12))), Sym("mmhg")));
        c.get(UnitsPressure.MMHG).put(UnitsPressure.ZETTAPASCAL, Mul(Rat(Mul(Int(2), Pow(10, 29)), Int("26664477483")), Sym("mmhg")));
        c.get(UnitsPressure.NANOPASCAL).put(UnitsPressure.ATHMOSPHERE, Mul(Mul(Int(101325), Pow(10, 9)), Sym("nanopa")));
        c.get(UnitsPressure.NANOPASCAL).put(UnitsPressure.ATTOPASCAL, Mul(Rat(Int(1), Pow(10, 9)), Sym("nanopa")));
        c.get(UnitsPressure.NANOPASCAL).put(UnitsPressure.BAR, Mul(Pow(10, 14), Sym("nanopa")));
        c.get(UnitsPressure.NANOPASCAL).put(UnitsPressure.CENTIBAR, Mul(Pow(10, 12), Sym("nanopa")));
        c.get(UnitsPressure.NANOPASCAL).put(UnitsPressure.CENTIPASCAL, Mul(Pow(10, 7), Sym("nanopa")));
        c.get(UnitsPressure.NANOPASCAL).put(UnitsPressure.DECAPASCAL, Mul(Pow(10, 10), Sym("nanopa")));
        c.get(UnitsPressure.NANOPASCAL).put(UnitsPressure.DECIBAR, Mul(Pow(10, 13), Sym("nanopa")));
        c.get(UnitsPressure.NANOPASCAL).put(UnitsPressure.DECIPASCAL, Mul(Pow(10, 8), Sym("nanopa")));
        c.get(UnitsPressure.NANOPASCAL).put(UnitsPressure.EXAPASCAL, Mul(Pow(10, 27), Sym("nanopa")));
        c.get(UnitsPressure.NANOPASCAL).put(UnitsPressure.FEMTOPASCAL, Mul(Rat(Int(1), Pow(10, 6)), Sym("nanopa")));
        c.get(UnitsPressure.NANOPASCAL).put(UnitsPressure.GIGAPASCAL, Mul(Pow(10, 18), Sym("nanopa")));
        c.get(UnitsPressure.NANOPASCAL).put(UnitsPressure.HECTOPASCAL, Mul(Pow(10, 11), Sym("nanopa")));
        c.get(UnitsPressure.NANOPASCAL).put(UnitsPressure.KILOBAR, Mul(Pow(10, 17), Sym("nanopa")));
        c.get(UnitsPressure.NANOPASCAL).put(UnitsPressure.KILOPASCAL, Mul(Pow(10, 12), Sym("nanopa")));
        c.get(UnitsPressure.NANOPASCAL).put(UnitsPressure.MEGABAR, Mul(Pow(10, 20), Sym("nanopa")));
        c.get(UnitsPressure.NANOPASCAL).put(UnitsPressure.MEGAPASCAL, Mul(Pow(10, 15), Sym("nanopa")));
        c.get(UnitsPressure.NANOPASCAL).put(UnitsPressure.MICROPASCAL, Mul(Int(1000), Sym("nanopa")));
        c.get(UnitsPressure.NANOPASCAL).put(UnitsPressure.MILLIBAR, Mul(Pow(10, 11), Sym("nanopa")));
        c.get(UnitsPressure.NANOPASCAL).put(UnitsPressure.MILLIPASCAL, Mul(Pow(10, 6), Sym("nanopa")));
        c.get(UnitsPressure.NANOPASCAL).put(UnitsPressure.MILLITORR, Mul(Rat(Int("2533125000"), Int(19)), Sym("nanopa")));
        c.get(UnitsPressure.NANOPASCAL).put(UnitsPressure.MMHG, Mul(Int("133322387415"), Sym("nanopa")));
        c.get(UnitsPressure.NANOPASCAL).put(UnitsPressure.PASCAL, Mul(Pow(10, 9), Sym("nanopa")));
        c.get(UnitsPressure.NANOPASCAL).put(UnitsPressure.PETAPASCAL, Mul(Pow(10, 24), Sym("nanopa")));
        c.get(UnitsPressure.NANOPASCAL).put(UnitsPressure.PICOPASCAL, Mul(Rat(Int(1), Int(1000)), Sym("nanopa")));
        c.get(UnitsPressure.NANOPASCAL).put(UnitsPressure.PSI, Mul(Rat(Int("172368932329209"), Int(25)), Sym("nanopa")));
        c.get(UnitsPressure.NANOPASCAL).put(UnitsPressure.TERAPASCAL, Mul(Pow(10, 21), Sym("nanopa")));
        c.get(UnitsPressure.NANOPASCAL).put(UnitsPressure.TORR, Mul(Rat(Mul(Int(2533125), Pow(10, 6)), Int(19)), Sym("nanopa")));
        c.get(UnitsPressure.NANOPASCAL).put(UnitsPressure.YOCTOPASCAL, Mul(Rat(Int(1), Pow(10, 15)), Sym("nanopa")));
        c.get(UnitsPressure.NANOPASCAL).put(UnitsPressure.YOTTAPASCAL, Mul(Pow(10, 33), Sym("nanopa")));
        c.get(UnitsPressure.NANOPASCAL).put(UnitsPressure.ZEPTOPASCAL, Mul(Rat(Int(1), Pow(10, 12)), Sym("nanopa")));
        c.get(UnitsPressure.NANOPASCAL).put(UnitsPressure.ZETTAPASCAL, Mul(Pow(10, 30), Sym("nanopa")));
        c.get(UnitsPressure.PASCAL).put(UnitsPressure.ATHMOSPHERE, Mul(Int(101325), Sym("pa")));
        c.get(UnitsPressure.PASCAL).put(UnitsPressure.ATTOPASCAL, Mul(Rat(Int(1), Pow(10, 18)), Sym("pa")));
        c.get(UnitsPressure.PASCAL).put(UnitsPressure.BAR, Mul(Pow(10, 5), Sym("pa")));
        c.get(UnitsPressure.PASCAL).put(UnitsPressure.CENTIBAR, Mul(Int(1000), Sym("pa")));
        c.get(UnitsPressure.PASCAL).put(UnitsPressure.CENTIPASCAL, Mul(Rat(Int(1), Int(100)), Sym("pa")));
        c.get(UnitsPressure.PASCAL).put(UnitsPressure.DECAPASCAL, Mul(Int(10), Sym("pa")));
        c.get(UnitsPressure.PASCAL).put(UnitsPressure.DECIBAR, Mul(Pow(10, 4), Sym("pa")));
        c.get(UnitsPressure.PASCAL).put(UnitsPressure.DECIPASCAL, Mul(Rat(Int(1), Int(10)), Sym("pa")));
        c.get(UnitsPressure.PASCAL).put(UnitsPressure.EXAPASCAL, Mul(Pow(10, 18), Sym("pa")));
        c.get(UnitsPressure.PASCAL).put(UnitsPressure.FEMTOPASCAL, Mul(Rat(Int(1), Pow(10, 15)), Sym("pa")));
        c.get(UnitsPressure.PASCAL).put(UnitsPressure.GIGAPASCAL, Mul(Pow(10, 9), Sym("pa")));
        c.get(UnitsPressure.PASCAL).put(UnitsPressure.HECTOPASCAL, Mul(Int(100), Sym("pa")));
        c.get(UnitsPressure.PASCAL).put(UnitsPressure.KILOBAR, Mul(Pow(10, 8), Sym("pa")));
        c.get(UnitsPressure.PASCAL).put(UnitsPressure.KILOPASCAL, Mul(Int(1000), Sym("pa")));
        c.get(UnitsPressure.PASCAL).put(UnitsPressure.MEGABAR, Mul(Pow(10, 11), Sym("pa")));
        c.get(UnitsPressure.PASCAL).put(UnitsPressure.MEGAPASCAL, Mul(Pow(10, 6), Sym("pa")));
        c.get(UnitsPressure.PASCAL).put(UnitsPressure.MICROPASCAL, Mul(Rat(Int(1), Pow(10, 6)), Sym("pa")));
        c.get(UnitsPressure.PASCAL).put(UnitsPressure.MILLIBAR, Mul(Int(100), Sym("pa")));
        c.get(UnitsPressure.PASCAL).put(UnitsPressure.MILLIPASCAL, Mul(Rat(Int(1), Int(1000)), Sym("pa")));
        c.get(UnitsPressure.PASCAL).put(UnitsPressure.MILLITORR, Mul(Rat(Int(4053), Int(30400)), Sym("pa")));
        c.get(UnitsPressure.PASCAL).put(UnitsPressure.MMHG, Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 8))), Sym("pa")));
        c.get(UnitsPressure.PASCAL).put(UnitsPressure.NANOPASCAL, Mul(Rat(Int(1), Pow(10, 9)), Sym("pa")));
        c.get(UnitsPressure.PASCAL).put(UnitsPressure.PETAPASCAL, Mul(Pow(10, 15), Sym("pa")));
        c.get(UnitsPressure.PASCAL).put(UnitsPressure.PICOPASCAL, Mul(Rat(Int(1), Pow(10, 12)), Sym("pa")));
        c.get(UnitsPressure.PASCAL).put(UnitsPressure.PSI, Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 9))), Sym("pa")));
        c.get(UnitsPressure.PASCAL).put(UnitsPressure.TERAPASCAL, Mul(Pow(10, 12), Sym("pa")));
        c.get(UnitsPressure.PASCAL).put(UnitsPressure.TORR, Mul(Rat(Int(20265), Int(152)), Sym("pa")));
        c.get(UnitsPressure.PASCAL).put(UnitsPressure.YOCTOPASCAL, Mul(Rat(Int(1), Pow(10, 24)), Sym("pa")));
        c.get(UnitsPressure.PASCAL).put(UnitsPressure.YOTTAPASCAL, Mul(Pow(10, 24), Sym("pa")));
        c.get(UnitsPressure.PASCAL).put(UnitsPressure.ZEPTOPASCAL, Mul(Rat(Int(1), Pow(10, 21)), Sym("pa")));
        c.get(UnitsPressure.PASCAL).put(UnitsPressure.ZETTAPASCAL, Mul(Pow(10, 21), Sym("pa")));
        c.get(UnitsPressure.PETAPASCAL).put(UnitsPressure.ATHMOSPHERE, Mul(Rat(Int(4053), Mul(Int(4), Pow(10, 13))), Sym("petapa")));
        c.get(UnitsPressure.PETAPASCAL).put(UnitsPressure.ATTOPASCAL, Mul(Rat(Int(1), Pow(10, 33)), Sym("petapa")));
        c.get(UnitsPressure.PETAPASCAL).put(UnitsPressure.BAR, Mul(Rat(Int(1), Pow(10, 10)), Sym("petapa")));
        c.get(UnitsPressure.PETAPASCAL).put(UnitsPressure.CENTIBAR, Mul(Rat(Int(1), Pow(10, 12)), Sym("petapa")));
        c.get(UnitsPressure.PETAPASCAL).put(UnitsPressure.CENTIPASCAL, Mul(Rat(Int(1), Pow(10, 17)), Sym("petapa")));
        c.get(UnitsPressure.PETAPASCAL).put(UnitsPressure.DECAPASCAL, Mul(Rat(Int(1), Pow(10, 14)), Sym("petapa")));
        c.get(UnitsPressure.PETAPASCAL).put(UnitsPressure.DECIBAR, Mul(Rat(Int(1), Pow(10, 11)), Sym("petapa")));
        c.get(UnitsPressure.PETAPASCAL).put(UnitsPressure.DECIPASCAL, Mul(Rat(Int(1), Pow(10, 16)), Sym("petapa")));
        c.get(UnitsPressure.PETAPASCAL).put(UnitsPressure.EXAPASCAL, Mul(Int(1000), Sym("petapa")));
        c.get(UnitsPressure.PETAPASCAL).put(UnitsPressure.FEMTOPASCAL, Mul(Rat(Int(1), Pow(10, 30)), Sym("petapa")));
        c.get(UnitsPressure.PETAPASCAL).put(UnitsPressure.GIGAPASCAL, Mul(Rat(Int(1), Pow(10, 6)), Sym("petapa")));
        c.get(UnitsPressure.PETAPASCAL).put(UnitsPressure.HECTOPASCAL, Mul(Rat(Int(1), Pow(10, 13)), Sym("petapa")));
        c.get(UnitsPressure.PETAPASCAL).put(UnitsPressure.KILOBAR, Mul(Rat(Int(1), Pow(10, 7)), Sym("petapa")));
        c.get(UnitsPressure.PETAPASCAL).put(UnitsPressure.KILOPASCAL, Mul(Rat(Int(1), Pow(10, 12)), Sym("petapa")));
        c.get(UnitsPressure.PETAPASCAL).put(UnitsPressure.MEGABAR, Mul(Rat(Int(1), Pow(10, 4)), Sym("petapa")));
        c.get(UnitsPressure.PETAPASCAL).put(UnitsPressure.MEGAPASCAL, Mul(Rat(Int(1), Pow(10, 9)), Sym("petapa")));
        c.get(UnitsPressure.PETAPASCAL).put(UnitsPressure.MICROPASCAL, Mul(Rat(Int(1), Pow(10, 21)), Sym("petapa")));
        c.get(UnitsPressure.PETAPASCAL).put(UnitsPressure.MILLIBAR, Mul(Rat(Int(1), Pow(10, 13)), Sym("petapa")));
        c.get(UnitsPressure.PETAPASCAL).put(UnitsPressure.MILLIPASCAL, Mul(Rat(Int(1), Pow(10, 18)), Sym("petapa")));
        c.get(UnitsPressure.PETAPASCAL).put(UnitsPressure.MILLITORR, Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 17))), Sym("petapa")));
        c.get(UnitsPressure.PETAPASCAL).put(UnitsPressure.MMHG, Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 23))), Sym("petapa")));
        c.get(UnitsPressure.PETAPASCAL).put(UnitsPressure.NANOPASCAL, Mul(Rat(Int(1), Pow(10, 24)), Sym("petapa")));
        c.get(UnitsPressure.PETAPASCAL).put(UnitsPressure.PASCAL, Mul(Rat(Int(1), Pow(10, 15)), Sym("petapa")));
        c.get(UnitsPressure.PETAPASCAL).put(UnitsPressure.PICOPASCAL, Mul(Rat(Int(1), Pow(10, 27)), Sym("petapa")));
        c.get(UnitsPressure.PETAPASCAL).put(UnitsPressure.PSI, Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 24))), Sym("petapa")));
        c.get(UnitsPressure.PETAPASCAL).put(UnitsPressure.TERAPASCAL, Mul(Rat(Int(1), Int(1000)), Sym("petapa")));
        c.get(UnitsPressure.PETAPASCAL).put(UnitsPressure.TORR, Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 14))), Sym("petapa")));
        c.get(UnitsPressure.PETAPASCAL).put(UnitsPressure.YOCTOPASCAL, Mul(Rat(Int(1), Pow(10, 39)), Sym("petapa")));
        c.get(UnitsPressure.PETAPASCAL).put(UnitsPressure.YOTTAPASCAL, Mul(Pow(10, 9), Sym("petapa")));
        c.get(UnitsPressure.PETAPASCAL).put(UnitsPressure.ZEPTOPASCAL, Mul(Rat(Int(1), Pow(10, 36)), Sym("petapa")));
        c.get(UnitsPressure.PETAPASCAL).put(UnitsPressure.ZETTAPASCAL, Mul(Pow(10, 6), Sym("petapa")));
        c.get(UnitsPressure.PICOPASCAL).put(UnitsPressure.ATHMOSPHERE, Mul(Mul(Int(101325), Pow(10, 12)), Sym("picopa")));
        c.get(UnitsPressure.PICOPASCAL).put(UnitsPressure.ATTOPASCAL, Mul(Rat(Int(1), Pow(10, 6)), Sym("picopa")));
        c.get(UnitsPressure.PICOPASCAL).put(UnitsPressure.BAR, Mul(Pow(10, 17), Sym("picopa")));
        c.get(UnitsPressure.PICOPASCAL).put(UnitsPressure.CENTIBAR, Mul(Pow(10, 15), Sym("picopa")));
        c.get(UnitsPressure.PICOPASCAL).put(UnitsPressure.CENTIPASCAL, Mul(Pow(10, 10), Sym("picopa")));
        c.get(UnitsPressure.PICOPASCAL).put(UnitsPressure.DECAPASCAL, Mul(Pow(10, 13), Sym("picopa")));
        c.get(UnitsPressure.PICOPASCAL).put(UnitsPressure.DECIBAR, Mul(Pow(10, 16), Sym("picopa")));
        c.get(UnitsPressure.PICOPASCAL).put(UnitsPressure.DECIPASCAL, Mul(Pow(10, 11), Sym("picopa")));
        c.get(UnitsPressure.PICOPASCAL).put(UnitsPressure.EXAPASCAL, Mul(Pow(10, 30), Sym("picopa")));
        c.get(UnitsPressure.PICOPASCAL).put(UnitsPressure.FEMTOPASCAL, Mul(Rat(Int(1), Int(1000)), Sym("picopa")));
        c.get(UnitsPressure.PICOPASCAL).put(UnitsPressure.GIGAPASCAL, Mul(Pow(10, 21), Sym("picopa")));
        c.get(UnitsPressure.PICOPASCAL).put(UnitsPressure.HECTOPASCAL, Mul(Pow(10, 14), Sym("picopa")));
        c.get(UnitsPressure.PICOPASCAL).put(UnitsPressure.KILOBAR, Mul(Pow(10, 20), Sym("picopa")));
        c.get(UnitsPressure.PICOPASCAL).put(UnitsPressure.KILOPASCAL, Mul(Pow(10, 15), Sym("picopa")));
        c.get(UnitsPressure.PICOPASCAL).put(UnitsPressure.MEGABAR, Mul(Pow(10, 23), Sym("picopa")));
        c.get(UnitsPressure.PICOPASCAL).put(UnitsPressure.MEGAPASCAL, Mul(Pow(10, 18), Sym("picopa")));
        c.get(UnitsPressure.PICOPASCAL).put(UnitsPressure.MICROPASCAL, Mul(Pow(10, 6), Sym("picopa")));
        c.get(UnitsPressure.PICOPASCAL).put(UnitsPressure.MILLIBAR, Mul(Pow(10, 14), Sym("picopa")));
        c.get(UnitsPressure.PICOPASCAL).put(UnitsPressure.MILLIPASCAL, Mul(Pow(10, 9), Sym("picopa")));
        c.get(UnitsPressure.PICOPASCAL).put(UnitsPressure.MILLITORR, Mul(Rat(Mul(Int(2533125), Pow(10, 6)), Int(19)), Sym("picopa")));
        c.get(UnitsPressure.PICOPASCAL).put(UnitsPressure.MMHG, Mul(Int("133322387415000"), Sym("picopa")));
        c.get(UnitsPressure.PICOPASCAL).put(UnitsPressure.NANOPASCAL, Mul(Int(1000), Sym("picopa")));
        c.get(UnitsPressure.PICOPASCAL).put(UnitsPressure.PASCAL, Mul(Pow(10, 12), Sym("picopa")));
        c.get(UnitsPressure.PICOPASCAL).put(UnitsPressure.PETAPASCAL, Mul(Pow(10, 27), Sym("picopa")));
        c.get(UnitsPressure.PICOPASCAL).put(UnitsPressure.PSI, Mul(Int("6894757293168360"), Sym("picopa")));
        c.get(UnitsPressure.PICOPASCAL).put(UnitsPressure.TERAPASCAL, Mul(Pow(10, 24), Sym("picopa")));
        c.get(UnitsPressure.PICOPASCAL).put(UnitsPressure.TORR, Mul(Rat(Mul(Int(2533125), Pow(10, 9)), Int(19)), Sym("picopa")));
        c.get(UnitsPressure.PICOPASCAL).put(UnitsPressure.YOCTOPASCAL, Mul(Rat(Int(1), Pow(10, 12)), Sym("picopa")));
        c.get(UnitsPressure.PICOPASCAL).put(UnitsPressure.YOTTAPASCAL, Mul(Pow(10, 36), Sym("picopa")));
        c.get(UnitsPressure.PICOPASCAL).put(UnitsPressure.ZEPTOPASCAL, Mul(Rat(Int(1), Pow(10, 9)), Sym("picopa")));
        c.get(UnitsPressure.PICOPASCAL).put(UnitsPressure.ZETTAPASCAL, Mul(Pow(10, 33), Sym("picopa")));
        c.get(UnitsPressure.PSI).put(UnitsPressure.ATHMOSPHERE, Mul(Rat(Mul(Int(120625), Pow(10, 9)), Int("8208044396629")), Sym("psi")));
        c.get(UnitsPressure.PSI).put(UnitsPressure.ATTOPASCAL, Mul(Rat(Int(1), Mul(Int("689475729316836"), Pow(10, 7))), Sym("psi")));
        c.get(UnitsPressure.PSI).put(UnitsPressure.BAR, Mul(Rat(Mul(Int(25), Pow(10, 14)), Int("172368932329209")), Sym("psi")));
        c.get(UnitsPressure.PSI).put(UnitsPressure.CENTIBAR, Mul(Rat(Mul(Int(25), Pow(10, 12)), Int("172368932329209")), Sym("psi")));
        c.get(UnitsPressure.PSI).put(UnitsPressure.CENTIPASCAL, Mul(Rat(Mul(Int(25), Pow(10, 7)), Int("172368932329209")), Sym("psi")));
        c.get(UnitsPressure.PSI).put(UnitsPressure.DECAPASCAL, Mul(Rat(Mul(Int(25), Pow(10, 10)), Int("172368932329209")), Sym("psi")));
        c.get(UnitsPressure.PSI).put(UnitsPressure.DECIBAR, Mul(Rat(Mul(Int(25), Pow(10, 13)), Int("172368932329209")), Sym("psi")));
        c.get(UnitsPressure.PSI).put(UnitsPressure.DECIPASCAL, Mul(Rat(Mul(Int(25), Pow(10, 8)), Int("172368932329209")), Sym("psi")));
        c.get(UnitsPressure.PSI).put(UnitsPressure.EXAPASCAL, Mul(Rat(Mul(Int(25), Pow(10, 27)), Int("172368932329209")), Sym("psi")));
        c.get(UnitsPressure.PSI).put(UnitsPressure.FEMTOPASCAL, Mul(Rat(Int(1), Mul(Int("689475729316836"), Pow(10, 4))), Sym("psi")));
        c.get(UnitsPressure.PSI).put(UnitsPressure.GIGAPASCAL, Mul(Rat(Mul(Int(25), Pow(10, 18)), Int("172368932329209")), Sym("psi")));
        c.get(UnitsPressure.PSI).put(UnitsPressure.HECTOPASCAL, Mul(Rat(Mul(Int(25), Pow(10, 11)), Int("172368932329209")), Sym("psi")));
        c.get(UnitsPressure.PSI).put(UnitsPressure.KILOBAR, Mul(Rat(Mul(Int(25), Pow(10, 17)), Int("172368932329209")), Sym("psi")));
        c.get(UnitsPressure.PSI).put(UnitsPressure.KILOPASCAL, Mul(Rat(Mul(Int(25), Pow(10, 12)), Int("172368932329209")), Sym("psi")));
        c.get(UnitsPressure.PSI).put(UnitsPressure.MEGABAR, Mul(Rat(Mul(Int(25), Pow(10, 20)), Int("172368932329209")), Sym("psi")));
        c.get(UnitsPressure.PSI).put(UnitsPressure.MEGAPASCAL, Mul(Rat(Mul(Int(25), Pow(10, 15)), Int("172368932329209")), Sym("psi")));
        c.get(UnitsPressure.PSI).put(UnitsPressure.MICROPASCAL, Mul(Rat(Int(25000), Int("172368932329209")), Sym("psi")));
        c.get(UnitsPressure.PSI).put(UnitsPressure.MILLIBAR, Mul(Rat(Mul(Int(25), Pow(10, 11)), Int("172368932329209")), Sym("psi")));
        c.get(UnitsPressure.PSI).put(UnitsPressure.MILLIPASCAL, Mul(Rat(Mul(Int(25), Pow(10, 6)), Int("172368932329209")), Sym("psi")));
        c.get(UnitsPressure.PSI).put(UnitsPressure.MILLITORR, Mul(Rat(Int("3015625000"), Int("155952843535951")), Sym("psi")));
        c.get(UnitsPressure.PSI).put(UnitsPressure.MMHG, Mul(Rat(Int("158717127875"), Int("8208044396629")), Sym("psi")));
        c.get(UnitsPressure.PSI).put(UnitsPressure.NANOPASCAL, Mul(Rat(Int(25), Int("172368932329209")), Sym("psi")));
        c.get(UnitsPressure.PSI).put(UnitsPressure.PASCAL, Mul(Rat(Mul(Int(25), Pow(10, 9)), Int("172368932329209")), Sym("psi")));
        c.get(UnitsPressure.PSI).put(UnitsPressure.PETAPASCAL, Mul(Rat(Mul(Int(25), Pow(10, 24)), Int("172368932329209")), Sym("psi")));
        c.get(UnitsPressure.PSI).put(UnitsPressure.PICOPASCAL, Mul(Rat(Int(1), Int("6894757293168360")), Sym("psi")));
        c.get(UnitsPressure.PSI).put(UnitsPressure.TERAPASCAL, Mul(Rat(Mul(Int(25), Pow(10, 21)), Int("172368932329209")), Sym("psi")));
        c.get(UnitsPressure.PSI).put(UnitsPressure.TORR, Mul(Rat(Mul(Int(3015625), Pow(10, 6)), Int("155952843535951")), Sym("psi")));
        c.get(UnitsPressure.PSI).put(UnitsPressure.YOCTOPASCAL, Mul(Rat(Int(1), Mul(Int("689475729316836"), Pow(10, 13))), Sym("psi")));
        c.get(UnitsPressure.PSI).put(UnitsPressure.YOTTAPASCAL, Mul(Rat(Mul(Int(25), Pow(10, 33)), Int("172368932329209")), Sym("psi")));
        c.get(UnitsPressure.PSI).put(UnitsPressure.ZEPTOPASCAL, Mul(Rat(Int(1), Mul(Int("689475729316836"), Pow(10, 10))), Sym("psi")));
        c.get(UnitsPressure.PSI).put(UnitsPressure.ZETTAPASCAL, Mul(Rat(Mul(Int(25), Pow(10, 30)), Int("172368932329209")), Sym("psi")));
        c.get(UnitsPressure.TERAPASCAL).put(UnitsPressure.ATHMOSPHERE, Mul(Rat(Int(4053), Mul(Int(4), Pow(10, 10))), Sym("terapa")));
        c.get(UnitsPressure.TERAPASCAL).put(UnitsPressure.ATTOPASCAL, Mul(Rat(Int(1), Pow(10, 30)), Sym("terapa")));
        c.get(UnitsPressure.TERAPASCAL).put(UnitsPressure.BAR, Mul(Rat(Int(1), Pow(10, 7)), Sym("terapa")));
        c.get(UnitsPressure.TERAPASCAL).put(UnitsPressure.CENTIBAR, Mul(Rat(Int(1), Pow(10, 9)), Sym("terapa")));
        c.get(UnitsPressure.TERAPASCAL).put(UnitsPressure.CENTIPASCAL, Mul(Rat(Int(1), Pow(10, 14)), Sym("terapa")));
        c.get(UnitsPressure.TERAPASCAL).put(UnitsPressure.DECAPASCAL, Mul(Rat(Int(1), Pow(10, 11)), Sym("terapa")));
        c.get(UnitsPressure.TERAPASCAL).put(UnitsPressure.DECIBAR, Mul(Rat(Int(1), Pow(10, 8)), Sym("terapa")));
        c.get(UnitsPressure.TERAPASCAL).put(UnitsPressure.DECIPASCAL, Mul(Rat(Int(1), Pow(10, 13)), Sym("terapa")));
        c.get(UnitsPressure.TERAPASCAL).put(UnitsPressure.EXAPASCAL, Mul(Pow(10, 6), Sym("terapa")));
        c.get(UnitsPressure.TERAPASCAL).put(UnitsPressure.FEMTOPASCAL, Mul(Rat(Int(1), Pow(10, 27)), Sym("terapa")));
        c.get(UnitsPressure.TERAPASCAL).put(UnitsPressure.GIGAPASCAL, Mul(Rat(Int(1), Int(1000)), Sym("terapa")));
        c.get(UnitsPressure.TERAPASCAL).put(UnitsPressure.HECTOPASCAL, Mul(Rat(Int(1), Pow(10, 10)), Sym("terapa")));
        c.get(UnitsPressure.TERAPASCAL).put(UnitsPressure.KILOBAR, Mul(Rat(Int(1), Pow(10, 4)), Sym("terapa")));
        c.get(UnitsPressure.TERAPASCAL).put(UnitsPressure.KILOPASCAL, Mul(Rat(Int(1), Pow(10, 9)), Sym("terapa")));
        c.get(UnitsPressure.TERAPASCAL).put(UnitsPressure.MEGABAR, Mul(Rat(Int(1), Int(10)), Sym("terapa")));
        c.get(UnitsPressure.TERAPASCAL).put(UnitsPressure.MEGAPASCAL, Mul(Rat(Int(1), Pow(10, 6)), Sym("terapa")));
        c.get(UnitsPressure.TERAPASCAL).put(UnitsPressure.MICROPASCAL, Mul(Rat(Int(1), Pow(10, 18)), Sym("terapa")));
        c.get(UnitsPressure.TERAPASCAL).put(UnitsPressure.MILLIBAR, Mul(Rat(Int(1), Pow(10, 10)), Sym("terapa")));
        c.get(UnitsPressure.TERAPASCAL).put(UnitsPressure.MILLIPASCAL, Mul(Rat(Int(1), Pow(10, 15)), Sym("terapa")));
        c.get(UnitsPressure.TERAPASCAL).put(UnitsPressure.MILLITORR, Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 14))), Sym("terapa")));
        c.get(UnitsPressure.TERAPASCAL).put(UnitsPressure.MMHG, Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 20))), Sym("terapa")));
        c.get(UnitsPressure.TERAPASCAL).put(UnitsPressure.NANOPASCAL, Mul(Rat(Int(1), Pow(10, 21)), Sym("terapa")));
        c.get(UnitsPressure.TERAPASCAL).put(UnitsPressure.PASCAL, Mul(Rat(Int(1), Pow(10, 12)), Sym("terapa")));
        c.get(UnitsPressure.TERAPASCAL).put(UnitsPressure.PETAPASCAL, Mul(Int(1000), Sym("terapa")));
        c.get(UnitsPressure.TERAPASCAL).put(UnitsPressure.PICOPASCAL, Mul(Rat(Int(1), Pow(10, 24)), Sym("terapa")));
        c.get(UnitsPressure.TERAPASCAL).put(UnitsPressure.PSI, Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 21))), Sym("terapa")));
        c.get(UnitsPressure.TERAPASCAL).put(UnitsPressure.TORR, Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 11))), Sym("terapa")));
        c.get(UnitsPressure.TERAPASCAL).put(UnitsPressure.YOCTOPASCAL, Mul(Rat(Int(1), Pow(10, 36)), Sym("terapa")));
        c.get(UnitsPressure.TERAPASCAL).put(UnitsPressure.YOTTAPASCAL, Mul(Pow(10, 12), Sym("terapa")));
        c.get(UnitsPressure.TERAPASCAL).put(UnitsPressure.ZEPTOPASCAL, Mul(Rat(Int(1), Pow(10, 33)), Sym("terapa")));
        c.get(UnitsPressure.TERAPASCAL).put(UnitsPressure.ZETTAPASCAL, Mul(Pow(10, 9), Sym("terapa")));
        c.get(UnitsPressure.TORR).put(UnitsPressure.ATHMOSPHERE, Mul(Int(760), Sym("torr")));
        c.get(UnitsPressure.TORR).put(UnitsPressure.ATTOPASCAL, Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 15))), Sym("torr")));
        c.get(UnitsPressure.TORR).put(UnitsPressure.BAR, Mul(Rat(Mul(Int(304), Pow(10, 4)), Int(4053)), Sym("torr")));
        c.get(UnitsPressure.TORR).put(UnitsPressure.CENTIBAR, Mul(Rat(Int(30400), Int(4053)), Sym("torr")));
        c.get(UnitsPressure.TORR).put(UnitsPressure.CENTIPASCAL, Mul(Rat(Int(38), Int(506625)), Sym("torr")));
        c.get(UnitsPressure.TORR).put(UnitsPressure.DECAPASCAL, Mul(Rat(Int(304), Int(4053)), Sym("torr")));
        c.get(UnitsPressure.TORR).put(UnitsPressure.DECIBAR, Mul(Rat(Int(304000), Int(4053)), Sym("torr")));
        c.get(UnitsPressure.TORR).put(UnitsPressure.DECIPASCAL, Mul(Rat(Int(76), Int(101325)), Sym("torr")));
        c.get(UnitsPressure.TORR).put(UnitsPressure.EXAPASCAL, Mul(Rat(Mul(Int(304), Pow(10, 17)), Int(4053)), Sym("torr")));
        c.get(UnitsPressure.TORR).put(UnitsPressure.FEMTOPASCAL, Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 12))), Sym("torr")));
        c.get(UnitsPressure.TORR).put(UnitsPressure.GIGAPASCAL, Mul(Rat(Mul(Int(304), Pow(10, 8)), Int(4053)), Sym("torr")));
        c.get(UnitsPressure.TORR).put(UnitsPressure.HECTOPASCAL, Mul(Rat(Int(3040), Int(4053)), Sym("torr")));
        c.get(UnitsPressure.TORR).put(UnitsPressure.KILOBAR, Mul(Rat(Mul(Int(304), Pow(10, 7)), Int(4053)), Sym("torr")));
        c.get(UnitsPressure.TORR).put(UnitsPressure.KILOPASCAL, Mul(Rat(Int(30400), Int(4053)), Sym("torr")));
        c.get(UnitsPressure.TORR).put(UnitsPressure.MEGABAR, Mul(Rat(Mul(Int(304), Pow(10, 10)), Int(4053)), Sym("torr")));
        c.get(UnitsPressure.TORR).put(UnitsPressure.MEGAPASCAL, Mul(Rat(Mul(Int(304), Pow(10, 5)), Int(4053)), Sym("torr")));
        c.get(UnitsPressure.TORR).put(UnitsPressure.MICROPASCAL, Mul(Rat(Int(19), Int("2533125000")), Sym("torr")));
        c.get(UnitsPressure.TORR).put(UnitsPressure.MILLIBAR, Mul(Rat(Int(3040), Int(4053)), Sym("torr")));
        c.get(UnitsPressure.TORR).put(UnitsPressure.MILLIPASCAL, Mul(Rat(Int(19), Int(2533125)), Sym("torr")));
        c.get(UnitsPressure.TORR).put(UnitsPressure.MILLITORR, Mul(Rat(Int(1), Int(1000)), Sym("torr")));
        c.get(UnitsPressure.TORR).put(UnitsPressure.MMHG, Mul(Rat(Int("24125003437"), Mul(Int(24125), Pow(10, 6))), Sym("torr")));
        c.get(UnitsPressure.TORR).put(UnitsPressure.NANOPASCAL, Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 6))), Sym("torr")));
        c.get(UnitsPressure.TORR).put(UnitsPressure.PASCAL, Mul(Rat(Int(152), Int(20265)), Sym("torr")));
        c.get(UnitsPressure.TORR).put(UnitsPressure.PETAPASCAL, Mul(Rat(Mul(Int(304), Pow(10, 14)), Int(4053)), Sym("torr")));
        c.get(UnitsPressure.TORR).put(UnitsPressure.PICOPASCAL, Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 9))), Sym("torr")));
        c.get(UnitsPressure.TORR).put(UnitsPressure.PSI, Mul(Rat(Int("155952843535951"), Mul(Int(3015625), Pow(10, 6))), Sym("torr")));
        c.get(UnitsPressure.TORR).put(UnitsPressure.TERAPASCAL, Mul(Rat(Mul(Int(304), Pow(10, 11)), Int(4053)), Sym("torr")));
        c.get(UnitsPressure.TORR).put(UnitsPressure.YOCTOPASCAL, Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 21))), Sym("torr")));
        c.get(UnitsPressure.TORR).put(UnitsPressure.YOTTAPASCAL, Mul(Rat(Mul(Int(304), Pow(10, 23)), Int(4053)), Sym("torr")));
        c.get(UnitsPressure.TORR).put(UnitsPressure.ZEPTOPASCAL, Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 18))), Sym("torr")));
        c.get(UnitsPressure.TORR).put(UnitsPressure.ZETTAPASCAL, Mul(Rat(Mul(Int(304), Pow(10, 20)), Int(4053)), Sym("torr")));
        c.get(UnitsPressure.YOCTOPASCAL).put(UnitsPressure.ATHMOSPHERE, Mul(Mul(Int(101325), Pow(10, 24)), Sym("yoctopa")));
        c.get(UnitsPressure.YOCTOPASCAL).put(UnitsPressure.ATTOPASCAL, Mul(Pow(10, 6), Sym("yoctopa")));
        c.get(UnitsPressure.YOCTOPASCAL).put(UnitsPressure.BAR, Mul(Pow(10, 29), Sym("yoctopa")));
        c.get(UnitsPressure.YOCTOPASCAL).put(UnitsPressure.CENTIBAR, Mul(Pow(10, 27), Sym("yoctopa")));
        c.get(UnitsPressure.YOCTOPASCAL).put(UnitsPressure.CENTIPASCAL, Mul(Pow(10, 22), Sym("yoctopa")));
        c.get(UnitsPressure.YOCTOPASCAL).put(UnitsPressure.DECAPASCAL, Mul(Pow(10, 25), Sym("yoctopa")));
        c.get(UnitsPressure.YOCTOPASCAL).put(UnitsPressure.DECIBAR, Mul(Pow(10, 28), Sym("yoctopa")));
        c.get(UnitsPressure.YOCTOPASCAL).put(UnitsPressure.DECIPASCAL, Mul(Pow(10, 23), Sym("yoctopa")));
        c.get(UnitsPressure.YOCTOPASCAL).put(UnitsPressure.EXAPASCAL, Mul(Pow(10, 42), Sym("yoctopa")));
        c.get(UnitsPressure.YOCTOPASCAL).put(UnitsPressure.FEMTOPASCAL, Mul(Pow(10, 9), Sym("yoctopa")));
        c.get(UnitsPressure.YOCTOPASCAL).put(UnitsPressure.GIGAPASCAL, Mul(Pow(10, 33), Sym("yoctopa")));
        c.get(UnitsPressure.YOCTOPASCAL).put(UnitsPressure.HECTOPASCAL, Mul(Pow(10, 26), Sym("yoctopa")));
        c.get(UnitsPressure.YOCTOPASCAL).put(UnitsPressure.KILOBAR, Mul(Pow(10, 32), Sym("yoctopa")));
        c.get(UnitsPressure.YOCTOPASCAL).put(UnitsPressure.KILOPASCAL, Mul(Pow(10, 27), Sym("yoctopa")));
        c.get(UnitsPressure.YOCTOPASCAL).put(UnitsPressure.MEGABAR, Mul(Pow(10, 35), Sym("yoctopa")));
        c.get(UnitsPressure.YOCTOPASCAL).put(UnitsPressure.MEGAPASCAL, Mul(Pow(10, 30), Sym("yoctopa")));
        c.get(UnitsPressure.YOCTOPASCAL).put(UnitsPressure.MICROPASCAL, Mul(Pow(10, 18), Sym("yoctopa")));
        c.get(UnitsPressure.YOCTOPASCAL).put(UnitsPressure.MILLIBAR, Mul(Pow(10, 26), Sym("yoctopa")));
        c.get(UnitsPressure.YOCTOPASCAL).put(UnitsPressure.MILLIPASCAL, Mul(Pow(10, 21), Sym("yoctopa")));
        c.get(UnitsPressure.YOCTOPASCAL).put(UnitsPressure.MILLITORR, Mul(Rat(Mul(Int(2533125), Pow(10, 18)), Int(19)), Sym("yoctopa")));
        c.get(UnitsPressure.YOCTOPASCAL).put(UnitsPressure.MMHG, Mul(Mul(Int("133322387415"), Pow(10, 15)), Sym("yoctopa")));
        c.get(UnitsPressure.YOCTOPASCAL).put(UnitsPressure.NANOPASCAL, Mul(Pow(10, 15), Sym("yoctopa")));
        c.get(UnitsPressure.YOCTOPASCAL).put(UnitsPressure.PASCAL, Mul(Pow(10, 24), Sym("yoctopa")));
        c.get(UnitsPressure.YOCTOPASCAL).put(UnitsPressure.PETAPASCAL, Mul(Pow(10, 39), Sym("yoctopa")));
        c.get(UnitsPressure.YOCTOPASCAL).put(UnitsPressure.PICOPASCAL, Mul(Pow(10, 12), Sym("yoctopa")));
        c.get(UnitsPressure.YOCTOPASCAL).put(UnitsPressure.PSI, Mul(Mul(Int("689475729316836"), Pow(10, 13)), Sym("yoctopa")));
        c.get(UnitsPressure.YOCTOPASCAL).put(UnitsPressure.TERAPASCAL, Mul(Pow(10, 36), Sym("yoctopa")));
        c.get(UnitsPressure.YOCTOPASCAL).put(UnitsPressure.TORR, Mul(Rat(Mul(Int(2533125), Pow(10, 21)), Int(19)), Sym("yoctopa")));
        c.get(UnitsPressure.YOCTOPASCAL).put(UnitsPressure.YOTTAPASCAL, Mul(Pow(10, 48), Sym("yoctopa")));
        c.get(UnitsPressure.YOCTOPASCAL).put(UnitsPressure.ZEPTOPASCAL, Mul(Int(1000), Sym("yoctopa")));
        c.get(UnitsPressure.YOCTOPASCAL).put(UnitsPressure.ZETTAPASCAL, Mul(Pow(10, 45), Sym("yoctopa")));
        c.get(UnitsPressure.YOTTAPASCAL).put(UnitsPressure.ATHMOSPHERE, Mul(Rat(Int(4053), Mul(Int(4), Pow(10, 22))), Sym("yottapa")));
        c.get(UnitsPressure.YOTTAPASCAL).put(UnitsPressure.ATTOPASCAL, Mul(Rat(Int(1), Pow(10, 42)), Sym("yottapa")));
        c.get(UnitsPressure.YOTTAPASCAL).put(UnitsPressure.BAR, Mul(Rat(Int(1), Pow(10, 19)), Sym("yottapa")));
        c.get(UnitsPressure.YOTTAPASCAL).put(UnitsPressure.CENTIBAR, Mul(Rat(Int(1), Pow(10, 21)), Sym("yottapa")));
        c.get(UnitsPressure.YOTTAPASCAL).put(UnitsPressure.CENTIPASCAL, Mul(Rat(Int(1), Pow(10, 26)), Sym("yottapa")));
        c.get(UnitsPressure.YOTTAPASCAL).put(UnitsPressure.DECAPASCAL, Mul(Rat(Int(1), Pow(10, 23)), Sym("yottapa")));
        c.get(UnitsPressure.YOTTAPASCAL).put(UnitsPressure.DECIBAR, Mul(Rat(Int(1), Pow(10, 20)), Sym("yottapa")));
        c.get(UnitsPressure.YOTTAPASCAL).put(UnitsPressure.DECIPASCAL, Mul(Rat(Int(1), Pow(10, 25)), Sym("yottapa")));
        c.get(UnitsPressure.YOTTAPASCAL).put(UnitsPressure.EXAPASCAL, Mul(Rat(Int(1), Pow(10, 6)), Sym("yottapa")));
        c.get(UnitsPressure.YOTTAPASCAL).put(UnitsPressure.FEMTOPASCAL, Mul(Rat(Int(1), Pow(10, 39)), Sym("yottapa")));
        c.get(UnitsPressure.YOTTAPASCAL).put(UnitsPressure.GIGAPASCAL, Mul(Rat(Int(1), Pow(10, 15)), Sym("yottapa")));
        c.get(UnitsPressure.YOTTAPASCAL).put(UnitsPressure.HECTOPASCAL, Mul(Rat(Int(1), Pow(10, 22)), Sym("yottapa")));
        c.get(UnitsPressure.YOTTAPASCAL).put(UnitsPressure.KILOBAR, Mul(Rat(Int(1), Pow(10, 16)), Sym("yottapa")));
        c.get(UnitsPressure.YOTTAPASCAL).put(UnitsPressure.KILOPASCAL, Mul(Rat(Int(1), Pow(10, 21)), Sym("yottapa")));
        c.get(UnitsPressure.YOTTAPASCAL).put(UnitsPressure.MEGABAR, Mul(Rat(Int(1), Pow(10, 13)), Sym("yottapa")));
        c.get(UnitsPressure.YOTTAPASCAL).put(UnitsPressure.MEGAPASCAL, Mul(Rat(Int(1), Pow(10, 18)), Sym("yottapa")));
        c.get(UnitsPressure.YOTTAPASCAL).put(UnitsPressure.MICROPASCAL, Mul(Rat(Int(1), Pow(10, 30)), Sym("yottapa")));
        c.get(UnitsPressure.YOTTAPASCAL).put(UnitsPressure.MILLIBAR, Mul(Rat(Int(1), Pow(10, 22)), Sym("yottapa")));
        c.get(UnitsPressure.YOTTAPASCAL).put(UnitsPressure.MILLIPASCAL, Mul(Rat(Int(1), Pow(10, 27)), Sym("yottapa")));
        c.get(UnitsPressure.YOTTAPASCAL).put(UnitsPressure.MILLITORR, Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 26))), Sym("yottapa")));
        c.get(UnitsPressure.YOTTAPASCAL).put(UnitsPressure.MMHG, Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 32))), Sym("yottapa")));
        c.get(UnitsPressure.YOTTAPASCAL).put(UnitsPressure.NANOPASCAL, Mul(Rat(Int(1), Pow(10, 33)), Sym("yottapa")));
        c.get(UnitsPressure.YOTTAPASCAL).put(UnitsPressure.PASCAL, Mul(Rat(Int(1), Pow(10, 24)), Sym("yottapa")));
        c.get(UnitsPressure.YOTTAPASCAL).put(UnitsPressure.PETAPASCAL, Mul(Rat(Int(1), Pow(10, 9)), Sym("yottapa")));
        c.get(UnitsPressure.YOTTAPASCAL).put(UnitsPressure.PICOPASCAL, Mul(Rat(Int(1), Pow(10, 36)), Sym("yottapa")));
        c.get(UnitsPressure.YOTTAPASCAL).put(UnitsPressure.PSI, Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 33))), Sym("yottapa")));
        c.get(UnitsPressure.YOTTAPASCAL).put(UnitsPressure.TERAPASCAL, Mul(Rat(Int(1), Pow(10, 12)), Sym("yottapa")));
        c.get(UnitsPressure.YOTTAPASCAL).put(UnitsPressure.TORR, Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 23))), Sym("yottapa")));
        c.get(UnitsPressure.YOTTAPASCAL).put(UnitsPressure.YOCTOPASCAL, Mul(Rat(Int(1), Pow(10, 48)), Sym("yottapa")));
        c.get(UnitsPressure.YOTTAPASCAL).put(UnitsPressure.ZEPTOPASCAL, Mul(Rat(Int(1), Pow(10, 45)), Sym("yottapa")));
        c.get(UnitsPressure.YOTTAPASCAL).put(UnitsPressure.ZETTAPASCAL, Mul(Rat(Int(1), Int(1000)), Sym("yottapa")));
        c.get(UnitsPressure.ZEPTOPASCAL).put(UnitsPressure.ATHMOSPHERE, Mul(Mul(Int(101325), Pow(10, 21)), Sym("zeptopa")));
        c.get(UnitsPressure.ZEPTOPASCAL).put(UnitsPressure.ATTOPASCAL, Mul(Int(1000), Sym("zeptopa")));
        c.get(UnitsPressure.ZEPTOPASCAL).put(UnitsPressure.BAR, Mul(Pow(10, 26), Sym("zeptopa")));
        c.get(UnitsPressure.ZEPTOPASCAL).put(UnitsPressure.CENTIBAR, Mul(Pow(10, 24), Sym("zeptopa")));
        c.get(UnitsPressure.ZEPTOPASCAL).put(UnitsPressure.CENTIPASCAL, Mul(Pow(10, 19), Sym("zeptopa")));
        c.get(UnitsPressure.ZEPTOPASCAL).put(UnitsPressure.DECAPASCAL, Mul(Pow(10, 22), Sym("zeptopa")));
        c.get(UnitsPressure.ZEPTOPASCAL).put(UnitsPressure.DECIBAR, Mul(Pow(10, 25), Sym("zeptopa")));
        c.get(UnitsPressure.ZEPTOPASCAL).put(UnitsPressure.DECIPASCAL, Mul(Pow(10, 20), Sym("zeptopa")));
        c.get(UnitsPressure.ZEPTOPASCAL).put(UnitsPressure.EXAPASCAL, Mul(Pow(10, 39), Sym("zeptopa")));
        c.get(UnitsPressure.ZEPTOPASCAL).put(UnitsPressure.FEMTOPASCAL, Mul(Pow(10, 6), Sym("zeptopa")));
        c.get(UnitsPressure.ZEPTOPASCAL).put(UnitsPressure.GIGAPASCAL, Mul(Pow(10, 30), Sym("zeptopa")));
        c.get(UnitsPressure.ZEPTOPASCAL).put(UnitsPressure.HECTOPASCAL, Mul(Pow(10, 23), Sym("zeptopa")));
        c.get(UnitsPressure.ZEPTOPASCAL).put(UnitsPressure.KILOBAR, Mul(Pow(10, 29), Sym("zeptopa")));
        c.get(UnitsPressure.ZEPTOPASCAL).put(UnitsPressure.KILOPASCAL, Mul(Pow(10, 24), Sym("zeptopa")));
        c.get(UnitsPressure.ZEPTOPASCAL).put(UnitsPressure.MEGABAR, Mul(Pow(10, 32), Sym("zeptopa")));
        c.get(UnitsPressure.ZEPTOPASCAL).put(UnitsPressure.MEGAPASCAL, Mul(Pow(10, 27), Sym("zeptopa")));
        c.get(UnitsPressure.ZEPTOPASCAL).put(UnitsPressure.MICROPASCAL, Mul(Pow(10, 15), Sym("zeptopa")));
        c.get(UnitsPressure.ZEPTOPASCAL).put(UnitsPressure.MILLIBAR, Mul(Pow(10, 23), Sym("zeptopa")));
        c.get(UnitsPressure.ZEPTOPASCAL).put(UnitsPressure.MILLIPASCAL, Mul(Pow(10, 18), Sym("zeptopa")));
        c.get(UnitsPressure.ZEPTOPASCAL).put(UnitsPressure.MILLITORR, Mul(Rat(Mul(Int(2533125), Pow(10, 15)), Int(19)), Sym("zeptopa")));
        c.get(UnitsPressure.ZEPTOPASCAL).put(UnitsPressure.MMHG, Mul(Mul(Int("133322387415"), Pow(10, 12)), Sym("zeptopa")));
        c.get(UnitsPressure.ZEPTOPASCAL).put(UnitsPressure.NANOPASCAL, Mul(Pow(10, 12), Sym("zeptopa")));
        c.get(UnitsPressure.ZEPTOPASCAL).put(UnitsPressure.PASCAL, Mul(Pow(10, 21), Sym("zeptopa")));
        c.get(UnitsPressure.ZEPTOPASCAL).put(UnitsPressure.PETAPASCAL, Mul(Pow(10, 36), Sym("zeptopa")));
        c.get(UnitsPressure.ZEPTOPASCAL).put(UnitsPressure.PICOPASCAL, Mul(Pow(10, 9), Sym("zeptopa")));
        c.get(UnitsPressure.ZEPTOPASCAL).put(UnitsPressure.PSI, Mul(Mul(Int("689475729316836"), Pow(10, 10)), Sym("zeptopa")));
        c.get(UnitsPressure.ZEPTOPASCAL).put(UnitsPressure.TERAPASCAL, Mul(Pow(10, 33), Sym("zeptopa")));
        c.get(UnitsPressure.ZEPTOPASCAL).put(UnitsPressure.TORR, Mul(Rat(Mul(Int(2533125), Pow(10, 18)), Int(19)), Sym("zeptopa")));
        c.get(UnitsPressure.ZEPTOPASCAL).put(UnitsPressure.YOCTOPASCAL, Mul(Rat(Int(1), Int(1000)), Sym("zeptopa")));
        c.get(UnitsPressure.ZEPTOPASCAL).put(UnitsPressure.YOTTAPASCAL, Mul(Pow(10, 45), Sym("zeptopa")));
        c.get(UnitsPressure.ZEPTOPASCAL).put(UnitsPressure.ZETTAPASCAL, Mul(Pow(10, 42), Sym("zeptopa")));
        c.get(UnitsPressure.ZETTAPASCAL).put(UnitsPressure.ATHMOSPHERE, Mul(Rat(Int(4053), Mul(Int(4), Pow(10, 19))), Sym("zettapa")));
        c.get(UnitsPressure.ZETTAPASCAL).put(UnitsPressure.ATTOPASCAL, Mul(Rat(Int(1), Pow(10, 39)), Sym("zettapa")));
        c.get(UnitsPressure.ZETTAPASCAL).put(UnitsPressure.BAR, Mul(Rat(Int(1), Pow(10, 16)), Sym("zettapa")));
        c.get(UnitsPressure.ZETTAPASCAL).put(UnitsPressure.CENTIBAR, Mul(Rat(Int(1), Pow(10, 18)), Sym("zettapa")));
        c.get(UnitsPressure.ZETTAPASCAL).put(UnitsPressure.CENTIPASCAL, Mul(Rat(Int(1), Pow(10, 23)), Sym("zettapa")));
        c.get(UnitsPressure.ZETTAPASCAL).put(UnitsPressure.DECAPASCAL, Mul(Rat(Int(1), Pow(10, 20)), Sym("zettapa")));
        c.get(UnitsPressure.ZETTAPASCAL).put(UnitsPressure.DECIBAR, Mul(Rat(Int(1), Pow(10, 17)), Sym("zettapa")));
        c.get(UnitsPressure.ZETTAPASCAL).put(UnitsPressure.DECIPASCAL, Mul(Rat(Int(1), Pow(10, 22)), Sym("zettapa")));
        c.get(UnitsPressure.ZETTAPASCAL).put(UnitsPressure.EXAPASCAL, Mul(Rat(Int(1), Int(1000)), Sym("zettapa")));
        c.get(UnitsPressure.ZETTAPASCAL).put(UnitsPressure.FEMTOPASCAL, Mul(Rat(Int(1), Pow(10, 36)), Sym("zettapa")));
        c.get(UnitsPressure.ZETTAPASCAL).put(UnitsPressure.GIGAPASCAL, Mul(Rat(Int(1), Pow(10, 12)), Sym("zettapa")));
        c.get(UnitsPressure.ZETTAPASCAL).put(UnitsPressure.HECTOPASCAL, Mul(Rat(Int(1), Pow(10, 19)), Sym("zettapa")));
        c.get(UnitsPressure.ZETTAPASCAL).put(UnitsPressure.KILOBAR, Mul(Rat(Int(1), Pow(10, 13)), Sym("zettapa")));
        c.get(UnitsPressure.ZETTAPASCAL).put(UnitsPressure.KILOPASCAL, Mul(Rat(Int(1), Pow(10, 18)), Sym("zettapa")));
        c.get(UnitsPressure.ZETTAPASCAL).put(UnitsPressure.MEGABAR, Mul(Rat(Int(1), Pow(10, 10)), Sym("zettapa")));
        c.get(UnitsPressure.ZETTAPASCAL).put(UnitsPressure.MEGAPASCAL, Mul(Rat(Int(1), Pow(10, 15)), Sym("zettapa")));
        c.get(UnitsPressure.ZETTAPASCAL).put(UnitsPressure.MICROPASCAL, Mul(Rat(Int(1), Pow(10, 27)), Sym("zettapa")));
        c.get(UnitsPressure.ZETTAPASCAL).put(UnitsPressure.MILLIBAR, Mul(Rat(Int(1), Pow(10, 19)), Sym("zettapa")));
        c.get(UnitsPressure.ZETTAPASCAL).put(UnitsPressure.MILLIPASCAL, Mul(Rat(Int(1), Pow(10, 24)), Sym("zettapa")));
        c.get(UnitsPressure.ZETTAPASCAL).put(UnitsPressure.MILLITORR, Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 23))), Sym("zettapa")));
        c.get(UnitsPressure.ZETTAPASCAL).put(UnitsPressure.MMHG, Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 29))), Sym("zettapa")));
        c.get(UnitsPressure.ZETTAPASCAL).put(UnitsPressure.NANOPASCAL, Mul(Rat(Int(1), Pow(10, 30)), Sym("zettapa")));
        c.get(UnitsPressure.ZETTAPASCAL).put(UnitsPressure.PASCAL, Mul(Rat(Int(1), Pow(10, 21)), Sym("zettapa")));
        c.get(UnitsPressure.ZETTAPASCAL).put(UnitsPressure.PETAPASCAL, Mul(Rat(Int(1), Pow(10, 6)), Sym("zettapa")));
        c.get(UnitsPressure.ZETTAPASCAL).put(UnitsPressure.PICOPASCAL, Mul(Rat(Int(1), Pow(10, 33)), Sym("zettapa")));
        c.get(UnitsPressure.ZETTAPASCAL).put(UnitsPressure.PSI, Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 30))), Sym("zettapa")));
        c.get(UnitsPressure.ZETTAPASCAL).put(UnitsPressure.TERAPASCAL, Mul(Rat(Int(1), Pow(10, 9)), Sym("zettapa")));
        c.get(UnitsPressure.ZETTAPASCAL).put(UnitsPressure.TORR, Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 20))), Sym("zettapa")));
        c.get(UnitsPressure.ZETTAPASCAL).put(UnitsPressure.YOCTOPASCAL, Mul(Rat(Int(1), Pow(10, 45)), Sym("zettapa")));
        c.get(UnitsPressure.ZETTAPASCAL).put(UnitsPressure.YOTTAPASCAL, Mul(Int(1000), Sym("zettapa")));
        c.get(UnitsPressure.ZETTAPASCAL).put(UnitsPressure.ZEPTOPASCAL, Mul(Rat(Int(1), Pow(10, 42)), Sym("zettapa")));
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
        s.put(UnitsPressure.KILOBAR, "kBar");
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

