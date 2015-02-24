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

    private static final Map<String, Conversion> conversions;
    static {
        Map<String, Conversion> c = new HashMap<String, Conversion>();

        c.put("ATHMOSPHERE:ATTOPASCAL", Mul(Rat(Int(1), Mul(Int(101325), Pow(10, 18))), Sym("atm")));
        c.put("ATHMOSPHERE:BAR", Mul(Rat(Int(4000), Int(4053)), Sym("atm")));
        c.put("ATHMOSPHERE:CENTIBAR", Mul(Rat(Int(40), Int(4053)), Sym("atm")));
        c.put("ATHMOSPHERE:CENTIPASCAL", Mul(Rat(Int(1), Int(10132500)), Sym("atm")));
        c.put("ATHMOSPHERE:DECAPASCAL", Mul(Rat(Int(2), Int(20265)), Sym("atm")));
        c.put("ATHMOSPHERE:DECIBAR", Mul(Rat(Int(400), Int(4053)), Sym("atm")));
        c.put("ATHMOSPHERE:DECIPASCAL", Mul(Rat(Int(1), Int(1013250)), Sym("atm")));
        c.put("ATHMOSPHERE:EXAPASCAL", Mul(Rat(Mul(Int(4), Pow(10, 16)), Int(4053)), Sym("atm")));
        c.put("ATHMOSPHERE:FEMTOPASCAL", Mul(Rat(Int(1), Mul(Int(101325), Pow(10, 15))), Sym("atm")));
        c.put("ATHMOSPHERE:GIGAPASCAL", Mul(Rat(Mul(Int(4), Pow(10, 7)), Int(4053)), Sym("atm")));
        c.put("ATHMOSPHERE:HECTOPASCAL", Mul(Rat(Int(4), Int(4053)), Sym("atm")));
        c.put("ATHMOSPHERE:KILOBAR", Mul(Rat(Mul(Int(4), Pow(10, 6)), Int(4053)), Sym("atm")));
        c.put("ATHMOSPHERE:KILOPASCAL", Mul(Rat(Int(40), Int(4053)), Sym("atm")));
        c.put("ATHMOSPHERE:MEGABAR", Mul(Rat(Mul(Int(4), Pow(10, 9)), Int(4053)), Sym("atm")));
        c.put("ATHMOSPHERE:MEGAPASCAL", Mul(Rat(Mul(Int(4), Pow(10, 4)), Int(4053)), Sym("atm")));
        c.put("ATHMOSPHERE:MICROPASCAL", Mul(Rat(Int(1), Mul(Int(101325), Pow(10, 6))), Sym("atm")));
        c.put("ATHMOSPHERE:MILLIBAR", Mul(Rat(Int(4), Int(4053)), Sym("atm")));
        c.put("ATHMOSPHERE:MILLIPASCAL", Mul(Rat(Int(1), Int(101325000)), Sym("atm")));
        c.put("ATHMOSPHERE:MILLITORR", Mul(Rat(Int(1), Mul(Int(76), Pow(10, 4))), Sym("atm")));
        c.put("ATHMOSPHERE:MMHG", Mul(Rat(Int(1269737023), Mul(Int(965), Pow(10, 9))), Sym("atm")));
        c.put("ATHMOSPHERE:NANOPASCAL", Mul(Rat(Int(1), Mul(Int(101325), Pow(10, 9))), Sym("atm")));
        c.put("ATHMOSPHERE:PASCAL", Mul(Rat(Int(1), Int(101325)), Sym("atm")));
        c.put("ATHMOSPHERE:PETAPASCAL", Mul(Rat(Mul(Int(4), Pow(10, 13)), Int(4053)), Sym("atm")));
        c.put("ATHMOSPHERE:PICOPASCAL", Mul(Rat(Int(1), Mul(Int(101325), Pow(10, 12))), Sym("atm")));
        c.put("ATHMOSPHERE:PSI", Mul(Rat(Int("8208044396629"), Mul(Int(120625), Pow(10, 9))), Sym("atm")));
        c.put("ATHMOSPHERE:TERAPASCAL", Mul(Rat(Mul(Int(4), Pow(10, 10)), Int(4053)), Sym("atm")));
        c.put("ATHMOSPHERE:TORR", Mul(Rat(Int(1), Int(760)), Sym("atm")));
        c.put("ATHMOSPHERE:YOCTOPASCAL", Mul(Rat(Int(1), Mul(Int(101325), Pow(10, 24))), Sym("atm")));
        c.put("ATHMOSPHERE:YOTTAPASCAL", Mul(Rat(Mul(Int(4), Pow(10, 22)), Int(4053)), Sym("atm")));
        c.put("ATHMOSPHERE:ZEPTOPASCAL", Mul(Rat(Int(1), Mul(Int(101325), Pow(10, 21))), Sym("atm")));
        c.put("ATHMOSPHERE:ZETTAPASCAL", Mul(Rat(Mul(Int(4), Pow(10, 19)), Int(4053)), Sym("atm")));
        c.put("ATTOPASCAL:ATHMOSPHERE", Mul(Mul(Int(101325), Pow(10, 18)), Sym("attopa")));
        c.put("ATTOPASCAL:BAR", Mul(Pow(10, 23), Sym("attopa")));
        c.put("ATTOPASCAL:CENTIBAR", Mul(Pow(10, 21), Sym("attopa")));
        c.put("ATTOPASCAL:CENTIPASCAL", Mul(Pow(10, 16), Sym("attopa")));
        c.put("ATTOPASCAL:DECAPASCAL", Mul(Pow(10, 19), Sym("attopa")));
        c.put("ATTOPASCAL:DECIBAR", Mul(Pow(10, 22), Sym("attopa")));
        c.put("ATTOPASCAL:DECIPASCAL", Mul(Pow(10, 17), Sym("attopa")));
        c.put("ATTOPASCAL:EXAPASCAL", Mul(Pow(10, 36), Sym("attopa")));
        c.put("ATTOPASCAL:FEMTOPASCAL", Mul(Int(1000), Sym("attopa")));
        c.put("ATTOPASCAL:GIGAPASCAL", Mul(Pow(10, 27), Sym("attopa")));
        c.put("ATTOPASCAL:HECTOPASCAL", Mul(Pow(10, 20), Sym("attopa")));
        c.put("ATTOPASCAL:KILOBAR", Mul(Pow(10, 26), Sym("attopa")));
        c.put("ATTOPASCAL:KILOPASCAL", Mul(Pow(10, 21), Sym("attopa")));
        c.put("ATTOPASCAL:MEGABAR", Mul(Pow(10, 29), Sym("attopa")));
        c.put("ATTOPASCAL:MEGAPASCAL", Mul(Pow(10, 24), Sym("attopa")));
        c.put("ATTOPASCAL:MICROPASCAL", Mul(Pow(10, 12), Sym("attopa")));
        c.put("ATTOPASCAL:MILLIBAR", Mul(Pow(10, 20), Sym("attopa")));
        c.put("ATTOPASCAL:MILLIPASCAL", Mul(Pow(10, 15), Sym("attopa")));
        c.put("ATTOPASCAL:MILLITORR", Mul(Rat(Mul(Int(2533125), Pow(10, 12)), Int(19)), Sym("attopa")));
        c.put("ATTOPASCAL:MMHG", Mul(Mul(Int("133322387415"), Pow(10, 9)), Sym("attopa")));
        c.put("ATTOPASCAL:NANOPASCAL", Mul(Pow(10, 9), Sym("attopa")));
        c.put("ATTOPASCAL:PASCAL", Mul(Pow(10, 18), Sym("attopa")));
        c.put("ATTOPASCAL:PETAPASCAL", Mul(Pow(10, 33), Sym("attopa")));
        c.put("ATTOPASCAL:PICOPASCAL", Mul(Pow(10, 6), Sym("attopa")));
        c.put("ATTOPASCAL:PSI", Mul(Mul(Int("689475729316836"), Pow(10, 7)), Sym("attopa")));
        c.put("ATTOPASCAL:TERAPASCAL", Mul(Pow(10, 30), Sym("attopa")));
        c.put("ATTOPASCAL:TORR", Mul(Rat(Mul(Int(2533125), Pow(10, 15)), Int(19)), Sym("attopa")));
        c.put("ATTOPASCAL:YOCTOPASCAL", Mul(Rat(Int(1), Pow(10, 6)), Sym("attopa")));
        c.put("ATTOPASCAL:YOTTAPASCAL", Mul(Pow(10, 42), Sym("attopa")));
        c.put("ATTOPASCAL:ZEPTOPASCAL", Mul(Rat(Int(1), Int(1000)), Sym("attopa")));
        c.put("ATTOPASCAL:ZETTAPASCAL", Mul(Pow(10, 39), Sym("attopa")));
        c.put("BAR:ATHMOSPHERE", Mul(Rat(Int(4053), Int(4000)), Sym("bar")));
        c.put("BAR:ATTOPASCAL", Mul(Rat(Int(1), Pow(10, 23)), Sym("bar")));
        c.put("BAR:CENTIBAR", Mul(Rat(Int(1), Int(100)), Sym("bar")));
        c.put("BAR:CENTIPASCAL", Mul(Rat(Int(1), Pow(10, 7)), Sym("bar")));
        c.put("BAR:DECAPASCAL", Mul(Rat(Int(1), Pow(10, 4)), Sym("bar")));
        c.put("BAR:DECIBAR", Mul(Rat(Int(1), Int(10)), Sym("bar")));
        c.put("BAR:DECIPASCAL", Mul(Rat(Int(1), Pow(10, 6)), Sym("bar")));
        c.put("BAR:EXAPASCAL", Mul(Pow(10, 13), Sym("bar")));
        c.put("BAR:FEMTOPASCAL", Mul(Rat(Int(1), Pow(10, 20)), Sym("bar")));
        c.put("BAR:GIGAPASCAL", Mul(Pow(10, 4), Sym("bar")));
        c.put("BAR:HECTOPASCAL", Mul(Rat(Int(1), Int(1000)), Sym("bar")));
        c.put("BAR:KILOBAR", Mul(Int(1000), Sym("bar")));
        c.put("BAR:KILOPASCAL", Mul(Rat(Int(1), Int(100)), Sym("bar")));
        c.put("BAR:MEGABAR", Mul(Pow(10, 6), Sym("bar")));
        c.put("BAR:MEGAPASCAL", Mul(Int(10), Sym("bar")));
        c.put("BAR:MICROPASCAL", Mul(Rat(Int(1), Pow(10, 11)), Sym("bar")));
        c.put("BAR:MILLIBAR", Mul(Rat(Int(1), Int(1000)), Sym("bar")));
        c.put("BAR:MILLIPASCAL", Mul(Rat(Int(1), Pow(10, 8)), Sym("bar")));
        c.put("BAR:MILLITORR", Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 7))), Sym("bar")));
        c.put("BAR:MMHG", Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 13))), Sym("bar")));
        c.put("BAR:NANOPASCAL", Mul(Rat(Int(1), Pow(10, 14)), Sym("bar")));
        c.put("BAR:PASCAL", Mul(Rat(Int(1), Pow(10, 5)), Sym("bar")));
        c.put("BAR:PETAPASCAL", Mul(Pow(10, 10), Sym("bar")));
        c.put("BAR:PICOPASCAL", Mul(Rat(Int(1), Pow(10, 17)), Sym("bar")));
        c.put("BAR:PSI", Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 14))), Sym("bar")));
        c.put("BAR:TERAPASCAL", Mul(Pow(10, 7), Sym("bar")));
        c.put("BAR:TORR", Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 4))), Sym("bar")));
        c.put("BAR:YOCTOPASCAL", Mul(Rat(Int(1), Pow(10, 29)), Sym("bar")));
        c.put("BAR:YOTTAPASCAL", Mul(Pow(10, 19), Sym("bar")));
        c.put("BAR:ZEPTOPASCAL", Mul(Rat(Int(1), Pow(10, 26)), Sym("bar")));
        c.put("BAR:ZETTAPASCAL", Mul(Pow(10, 16), Sym("bar")));
        c.put("CENTIBAR:ATHMOSPHERE", Mul(Rat(Int(4053), Int(40)), Sym("cbar")));
        c.put("CENTIBAR:ATTOPASCAL", Mul(Rat(Int(1), Pow(10, 21)), Sym("cbar")));
        c.put("CENTIBAR:BAR", Mul(Int(100), Sym("cbar")));
        c.put("CENTIBAR:CENTIPASCAL", Mul(Rat(Int(1), Pow(10, 5)), Sym("cbar")));
        c.put("CENTIBAR:DECAPASCAL", Mul(Rat(Int(1), Int(100)), Sym("cbar")));
        c.put("CENTIBAR:DECIBAR", Mul(Int(10), Sym("cbar")));
        c.put("CENTIBAR:DECIPASCAL", Mul(Rat(Int(1), Pow(10, 4)), Sym("cbar")));
        c.put("CENTIBAR:EXAPASCAL", Mul(Pow(10, 15), Sym("cbar")));
        c.put("CENTIBAR:FEMTOPASCAL", Mul(Rat(Int(1), Pow(10, 18)), Sym("cbar")));
        c.put("CENTIBAR:GIGAPASCAL", Mul(Pow(10, 6), Sym("cbar")));
        c.put("CENTIBAR:HECTOPASCAL", Mul(Rat(Int(1), Int(10)), Sym("cbar")));
        c.put("CENTIBAR:KILOBAR", Mul(Pow(10, 5), Sym("cbar")));
        c.put("CENTIBAR:KILOPASCAL", Sym("cbar"));
        c.put("CENTIBAR:MEGABAR", Mul(Pow(10, 8), Sym("cbar")));
        c.put("CENTIBAR:MEGAPASCAL", Mul(Int(1000), Sym("cbar")));
        c.put("CENTIBAR:MICROPASCAL", Mul(Rat(Int(1), Pow(10, 9)), Sym("cbar")));
        c.put("CENTIBAR:MILLIBAR", Mul(Rat(Int(1), Int(10)), Sym("cbar")));
        c.put("CENTIBAR:MILLIPASCAL", Mul(Rat(Int(1), Pow(10, 6)), Sym("cbar")));
        c.put("CENTIBAR:MILLITORR", Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 5))), Sym("cbar")));
        c.put("CENTIBAR:MMHG", Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 11))), Sym("cbar")));
        c.put("CENTIBAR:NANOPASCAL", Mul(Rat(Int(1), Pow(10, 12)), Sym("cbar")));
        c.put("CENTIBAR:PASCAL", Mul(Rat(Int(1), Int(1000)), Sym("cbar")));
        c.put("CENTIBAR:PETAPASCAL", Mul(Pow(10, 12), Sym("cbar")));
        c.put("CENTIBAR:PICOPASCAL", Mul(Rat(Int(1), Pow(10, 15)), Sym("cbar")));
        c.put("CENTIBAR:PSI", Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 12))), Sym("cbar")));
        c.put("CENTIBAR:TERAPASCAL", Mul(Pow(10, 9), Sym("cbar")));
        c.put("CENTIBAR:TORR", Mul(Rat(Int(4053), Int(30400)), Sym("cbar")));
        c.put("CENTIBAR:YOCTOPASCAL", Mul(Rat(Int(1), Pow(10, 27)), Sym("cbar")));
        c.put("CENTIBAR:YOTTAPASCAL", Mul(Pow(10, 21), Sym("cbar")));
        c.put("CENTIBAR:ZEPTOPASCAL", Mul(Rat(Int(1), Pow(10, 24)), Sym("cbar")));
        c.put("CENTIBAR:ZETTAPASCAL", Mul(Pow(10, 18), Sym("cbar")));
        c.put("CENTIPASCAL:ATHMOSPHERE", Mul(Int(10132500), Sym("centipa")));
        c.put("CENTIPASCAL:ATTOPASCAL", Mul(Rat(Int(1), Pow(10, 16)), Sym("centipa")));
        c.put("CENTIPASCAL:BAR", Mul(Pow(10, 7), Sym("centipa")));
        c.put("CENTIPASCAL:CENTIBAR", Mul(Pow(10, 5), Sym("centipa")));
        c.put("CENTIPASCAL:DECAPASCAL", Mul(Int(1000), Sym("centipa")));
        c.put("CENTIPASCAL:DECIBAR", Mul(Pow(10, 6), Sym("centipa")));
        c.put("CENTIPASCAL:DECIPASCAL", Mul(Int(10), Sym("centipa")));
        c.put("CENTIPASCAL:EXAPASCAL", Mul(Pow(10, 20), Sym("centipa")));
        c.put("CENTIPASCAL:FEMTOPASCAL", Mul(Rat(Int(1), Pow(10, 13)), Sym("centipa")));
        c.put("CENTIPASCAL:GIGAPASCAL", Mul(Pow(10, 11), Sym("centipa")));
        c.put("CENTIPASCAL:HECTOPASCAL", Mul(Pow(10, 4), Sym("centipa")));
        c.put("CENTIPASCAL:KILOBAR", Mul(Pow(10, 10), Sym("centipa")));
        c.put("CENTIPASCAL:KILOPASCAL", Mul(Pow(10, 5), Sym("centipa")));
        c.put("CENTIPASCAL:MEGABAR", Mul(Pow(10, 13), Sym("centipa")));
        c.put("CENTIPASCAL:MEGAPASCAL", Mul(Pow(10, 8), Sym("centipa")));
        c.put("CENTIPASCAL:MICROPASCAL", Mul(Rat(Int(1), Pow(10, 4)), Sym("centipa")));
        c.put("CENTIPASCAL:MILLIBAR", Mul(Pow(10, 4), Sym("centipa")));
        c.put("CENTIPASCAL:MILLIPASCAL", Mul(Rat(Int(1), Int(10)), Sym("centipa")));
        c.put("CENTIPASCAL:MILLITORR", Mul(Rat(Int(4053), Int(304)), Sym("centipa")));
        c.put("CENTIPASCAL:MMHG", Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 6))), Sym("centipa")));
        c.put("CENTIPASCAL:NANOPASCAL", Mul(Rat(Int(1), Pow(10, 7)), Sym("centipa")));
        c.put("CENTIPASCAL:PASCAL", Mul(Int(100), Sym("centipa")));
        c.put("CENTIPASCAL:PETAPASCAL", Mul(Pow(10, 17), Sym("centipa")));
        c.put("CENTIPASCAL:PICOPASCAL", Mul(Rat(Int(1), Pow(10, 10)), Sym("centipa")));
        c.put("CENTIPASCAL:PSI", Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 7))), Sym("centipa")));
        c.put("CENTIPASCAL:TERAPASCAL", Mul(Pow(10, 14), Sym("centipa")));
        c.put("CENTIPASCAL:TORR", Mul(Rat(Int(506625), Int(38)), Sym("centipa")));
        c.put("CENTIPASCAL:YOCTOPASCAL", Mul(Rat(Int(1), Pow(10, 22)), Sym("centipa")));
        c.put("CENTIPASCAL:YOTTAPASCAL", Mul(Pow(10, 26), Sym("centipa")));
        c.put("CENTIPASCAL:ZEPTOPASCAL", Mul(Rat(Int(1), Pow(10, 19)), Sym("centipa")));
        c.put("CENTIPASCAL:ZETTAPASCAL", Mul(Pow(10, 23), Sym("centipa")));
        c.put("DECAPASCAL:ATHMOSPHERE", Mul(Rat(Int(20265), Int(2)), Sym("decapa")));
        c.put("DECAPASCAL:ATTOPASCAL", Mul(Rat(Int(1), Pow(10, 19)), Sym("decapa")));
        c.put("DECAPASCAL:BAR", Mul(Pow(10, 4), Sym("decapa")));
        c.put("DECAPASCAL:CENTIBAR", Mul(Int(100), Sym("decapa")));
        c.put("DECAPASCAL:CENTIPASCAL", Mul(Rat(Int(1), Int(1000)), Sym("decapa")));
        c.put("DECAPASCAL:DECIBAR", Mul(Int(1000), Sym("decapa")));
        c.put("DECAPASCAL:DECIPASCAL", Mul(Rat(Int(1), Int(100)), Sym("decapa")));
        c.put("DECAPASCAL:EXAPASCAL", Mul(Pow(10, 17), Sym("decapa")));
        c.put("DECAPASCAL:FEMTOPASCAL", Mul(Rat(Int(1), Pow(10, 16)), Sym("decapa")));
        c.put("DECAPASCAL:GIGAPASCAL", Mul(Pow(10, 8), Sym("decapa")));
        c.put("DECAPASCAL:HECTOPASCAL", Mul(Int(10), Sym("decapa")));
        c.put("DECAPASCAL:KILOBAR", Mul(Pow(10, 7), Sym("decapa")));
        c.put("DECAPASCAL:KILOPASCAL", Mul(Int(100), Sym("decapa")));
        c.put("DECAPASCAL:MEGABAR", Mul(Pow(10, 10), Sym("decapa")));
        c.put("DECAPASCAL:MEGAPASCAL", Mul(Pow(10, 5), Sym("decapa")));
        c.put("DECAPASCAL:MICROPASCAL", Mul(Rat(Int(1), Pow(10, 7)), Sym("decapa")));
        c.put("DECAPASCAL:MILLIBAR", Mul(Int(10), Sym("decapa")));
        c.put("DECAPASCAL:MILLIPASCAL", Mul(Rat(Int(1), Pow(10, 4)), Sym("decapa")));
        c.put("DECAPASCAL:MILLITORR", Mul(Rat(Int(4053), Int(304000)), Sym("decapa")));
        c.put("DECAPASCAL:MMHG", Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 9))), Sym("decapa")));
        c.put("DECAPASCAL:NANOPASCAL", Mul(Rat(Int(1), Pow(10, 10)), Sym("decapa")));
        c.put("DECAPASCAL:PASCAL", Mul(Rat(Int(1), Int(10)), Sym("decapa")));
        c.put("DECAPASCAL:PETAPASCAL", Mul(Pow(10, 14), Sym("decapa")));
        c.put("DECAPASCAL:PICOPASCAL", Mul(Rat(Int(1), Pow(10, 13)), Sym("decapa")));
        c.put("DECAPASCAL:PSI", Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 10))), Sym("decapa")));
        c.put("DECAPASCAL:TERAPASCAL", Mul(Pow(10, 11), Sym("decapa")));
        c.put("DECAPASCAL:TORR", Mul(Rat(Int(4053), Int(304)), Sym("decapa")));
        c.put("DECAPASCAL:YOCTOPASCAL", Mul(Rat(Int(1), Pow(10, 25)), Sym("decapa")));
        c.put("DECAPASCAL:YOTTAPASCAL", Mul(Pow(10, 23), Sym("decapa")));
        c.put("DECAPASCAL:ZEPTOPASCAL", Mul(Rat(Int(1), Pow(10, 22)), Sym("decapa")));
        c.put("DECAPASCAL:ZETTAPASCAL", Mul(Pow(10, 20), Sym("decapa")));
        c.put("DECIBAR:ATHMOSPHERE", Mul(Rat(Int(4053), Int(400)), Sym("dbar")));
        c.put("DECIBAR:ATTOPASCAL", Mul(Rat(Int(1), Pow(10, 22)), Sym("dbar")));
        c.put("DECIBAR:BAR", Mul(Int(10), Sym("dbar")));
        c.put("DECIBAR:CENTIBAR", Mul(Rat(Int(1), Int(10)), Sym("dbar")));
        c.put("DECIBAR:CENTIPASCAL", Mul(Rat(Int(1), Pow(10, 6)), Sym("dbar")));
        c.put("DECIBAR:DECAPASCAL", Mul(Rat(Int(1), Int(1000)), Sym("dbar")));
        c.put("DECIBAR:DECIPASCAL", Mul(Rat(Int(1), Pow(10, 5)), Sym("dbar")));
        c.put("DECIBAR:EXAPASCAL", Mul(Pow(10, 14), Sym("dbar")));
        c.put("DECIBAR:FEMTOPASCAL", Mul(Rat(Int(1), Pow(10, 19)), Sym("dbar")));
        c.put("DECIBAR:GIGAPASCAL", Mul(Pow(10, 5), Sym("dbar")));
        c.put("DECIBAR:HECTOPASCAL", Mul(Rat(Int(1), Int(100)), Sym("dbar")));
        c.put("DECIBAR:KILOBAR", Mul(Pow(10, 4), Sym("dbar")));
        c.put("DECIBAR:KILOPASCAL", Mul(Rat(Int(1), Int(10)), Sym("dbar")));
        c.put("DECIBAR:MEGABAR", Mul(Pow(10, 7), Sym("dbar")));
        c.put("DECIBAR:MEGAPASCAL", Mul(Int(100), Sym("dbar")));
        c.put("DECIBAR:MICROPASCAL", Mul(Rat(Int(1), Pow(10, 10)), Sym("dbar")));
        c.put("DECIBAR:MILLIBAR", Mul(Rat(Int(1), Int(100)), Sym("dbar")));
        c.put("DECIBAR:MILLIPASCAL", Mul(Rat(Int(1), Pow(10, 7)), Sym("dbar")));
        c.put("DECIBAR:MILLITORR", Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 6))), Sym("dbar")));
        c.put("DECIBAR:MMHG", Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 12))), Sym("dbar")));
        c.put("DECIBAR:NANOPASCAL", Mul(Rat(Int(1), Pow(10, 13)), Sym("dbar")));
        c.put("DECIBAR:PASCAL", Mul(Rat(Int(1), Pow(10, 4)), Sym("dbar")));
        c.put("DECIBAR:PETAPASCAL", Mul(Pow(10, 11), Sym("dbar")));
        c.put("DECIBAR:PICOPASCAL", Mul(Rat(Int(1), Pow(10, 16)), Sym("dbar")));
        c.put("DECIBAR:PSI", Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 13))), Sym("dbar")));
        c.put("DECIBAR:TERAPASCAL", Mul(Pow(10, 8), Sym("dbar")));
        c.put("DECIBAR:TORR", Mul(Rat(Int(4053), Int(304000)), Sym("dbar")));
        c.put("DECIBAR:YOCTOPASCAL", Mul(Rat(Int(1), Pow(10, 28)), Sym("dbar")));
        c.put("DECIBAR:YOTTAPASCAL", Mul(Pow(10, 20), Sym("dbar")));
        c.put("DECIBAR:ZEPTOPASCAL", Mul(Rat(Int(1), Pow(10, 25)), Sym("dbar")));
        c.put("DECIBAR:ZETTAPASCAL", Mul(Pow(10, 17), Sym("dbar")));
        c.put("DECIPASCAL:ATHMOSPHERE", Mul(Int(1013250), Sym("decipa")));
        c.put("DECIPASCAL:ATTOPASCAL", Mul(Rat(Int(1), Pow(10, 17)), Sym("decipa")));
        c.put("DECIPASCAL:BAR", Mul(Pow(10, 6), Sym("decipa")));
        c.put("DECIPASCAL:CENTIBAR", Mul(Pow(10, 4), Sym("decipa")));
        c.put("DECIPASCAL:CENTIPASCAL", Mul(Rat(Int(1), Int(10)), Sym("decipa")));
        c.put("DECIPASCAL:DECAPASCAL", Mul(Int(100), Sym("decipa")));
        c.put("DECIPASCAL:DECIBAR", Mul(Pow(10, 5), Sym("decipa")));
        c.put("DECIPASCAL:EXAPASCAL", Mul(Pow(10, 19), Sym("decipa")));
        c.put("DECIPASCAL:FEMTOPASCAL", Mul(Rat(Int(1), Pow(10, 14)), Sym("decipa")));
        c.put("DECIPASCAL:GIGAPASCAL", Mul(Pow(10, 10), Sym("decipa")));
        c.put("DECIPASCAL:HECTOPASCAL", Mul(Int(1000), Sym("decipa")));
        c.put("DECIPASCAL:KILOBAR", Mul(Pow(10, 9), Sym("decipa")));
        c.put("DECIPASCAL:KILOPASCAL", Mul(Pow(10, 4), Sym("decipa")));
        c.put("DECIPASCAL:MEGABAR", Mul(Pow(10, 12), Sym("decipa")));
        c.put("DECIPASCAL:MEGAPASCAL", Mul(Pow(10, 7), Sym("decipa")));
        c.put("DECIPASCAL:MICROPASCAL", Mul(Rat(Int(1), Pow(10, 5)), Sym("decipa")));
        c.put("DECIPASCAL:MILLIBAR", Mul(Int(1000), Sym("decipa")));
        c.put("DECIPASCAL:MILLIPASCAL", Mul(Rat(Int(1), Int(100)), Sym("decipa")));
        c.put("DECIPASCAL:MILLITORR", Mul(Rat(Int(4053), Int(3040)), Sym("decipa")));
        c.put("DECIPASCAL:MMHG", Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 7))), Sym("decipa")));
        c.put("DECIPASCAL:NANOPASCAL", Mul(Rat(Int(1), Pow(10, 8)), Sym("decipa")));
        c.put("DECIPASCAL:PASCAL", Mul(Int(10), Sym("decipa")));
        c.put("DECIPASCAL:PETAPASCAL", Mul(Pow(10, 16), Sym("decipa")));
        c.put("DECIPASCAL:PICOPASCAL", Mul(Rat(Int(1), Pow(10, 11)), Sym("decipa")));
        c.put("DECIPASCAL:PSI", Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 8))), Sym("decipa")));
        c.put("DECIPASCAL:TERAPASCAL", Mul(Pow(10, 13), Sym("decipa")));
        c.put("DECIPASCAL:TORR", Mul(Rat(Int(101325), Int(76)), Sym("decipa")));
        c.put("DECIPASCAL:YOCTOPASCAL", Mul(Rat(Int(1), Pow(10, 23)), Sym("decipa")));
        c.put("DECIPASCAL:YOTTAPASCAL", Mul(Pow(10, 25), Sym("decipa")));
        c.put("DECIPASCAL:ZEPTOPASCAL", Mul(Rat(Int(1), Pow(10, 20)), Sym("decipa")));
        c.put("DECIPASCAL:ZETTAPASCAL", Mul(Pow(10, 22), Sym("decipa")));
        c.put("EXAPASCAL:ATHMOSPHERE", Mul(Rat(Int(4053), Mul(Int(4), Pow(10, 16))), Sym("exapa")));
        c.put("EXAPASCAL:ATTOPASCAL", Mul(Rat(Int(1), Pow(10, 36)), Sym("exapa")));
        c.put("EXAPASCAL:BAR", Mul(Rat(Int(1), Pow(10, 13)), Sym("exapa")));
        c.put("EXAPASCAL:CENTIBAR", Mul(Rat(Int(1), Pow(10, 15)), Sym("exapa")));
        c.put("EXAPASCAL:CENTIPASCAL", Mul(Rat(Int(1), Pow(10, 20)), Sym("exapa")));
        c.put("EXAPASCAL:DECAPASCAL", Mul(Rat(Int(1), Pow(10, 17)), Sym("exapa")));
        c.put("EXAPASCAL:DECIBAR", Mul(Rat(Int(1), Pow(10, 14)), Sym("exapa")));
        c.put("EXAPASCAL:DECIPASCAL", Mul(Rat(Int(1), Pow(10, 19)), Sym("exapa")));
        c.put("EXAPASCAL:FEMTOPASCAL", Mul(Rat(Int(1), Pow(10, 33)), Sym("exapa")));
        c.put("EXAPASCAL:GIGAPASCAL", Mul(Rat(Int(1), Pow(10, 9)), Sym("exapa")));
        c.put("EXAPASCAL:HECTOPASCAL", Mul(Rat(Int(1), Pow(10, 16)), Sym("exapa")));
        c.put("EXAPASCAL:KILOBAR", Mul(Rat(Int(1), Pow(10, 10)), Sym("exapa")));
        c.put("EXAPASCAL:KILOPASCAL", Mul(Rat(Int(1), Pow(10, 15)), Sym("exapa")));
        c.put("EXAPASCAL:MEGABAR", Mul(Rat(Int(1), Pow(10, 7)), Sym("exapa")));
        c.put("EXAPASCAL:MEGAPASCAL", Mul(Rat(Int(1), Pow(10, 12)), Sym("exapa")));
        c.put("EXAPASCAL:MICROPASCAL", Mul(Rat(Int(1), Pow(10, 24)), Sym("exapa")));
        c.put("EXAPASCAL:MILLIBAR", Mul(Rat(Int(1), Pow(10, 16)), Sym("exapa")));
        c.put("EXAPASCAL:MILLIPASCAL", Mul(Rat(Int(1), Pow(10, 21)), Sym("exapa")));
        c.put("EXAPASCAL:MILLITORR", Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 20))), Sym("exapa")));
        c.put("EXAPASCAL:MMHG", Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 26))), Sym("exapa")));
        c.put("EXAPASCAL:NANOPASCAL", Mul(Rat(Int(1), Pow(10, 27)), Sym("exapa")));
        c.put("EXAPASCAL:PASCAL", Mul(Rat(Int(1), Pow(10, 18)), Sym("exapa")));
        c.put("EXAPASCAL:PETAPASCAL", Mul(Rat(Int(1), Int(1000)), Sym("exapa")));
        c.put("EXAPASCAL:PICOPASCAL", Mul(Rat(Int(1), Pow(10, 30)), Sym("exapa")));
        c.put("EXAPASCAL:PSI", Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 27))), Sym("exapa")));
        c.put("EXAPASCAL:TERAPASCAL", Mul(Rat(Int(1), Pow(10, 6)), Sym("exapa")));
        c.put("EXAPASCAL:TORR", Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 17))), Sym("exapa")));
        c.put("EXAPASCAL:YOCTOPASCAL", Mul(Rat(Int(1), Pow(10, 42)), Sym("exapa")));
        c.put("EXAPASCAL:YOTTAPASCAL", Mul(Pow(10, 6), Sym("exapa")));
        c.put("EXAPASCAL:ZEPTOPASCAL", Mul(Rat(Int(1), Pow(10, 39)), Sym("exapa")));
        c.put("EXAPASCAL:ZETTAPASCAL", Mul(Int(1000), Sym("exapa")));
        c.put("FEMTOPASCAL:ATHMOSPHERE", Mul(Mul(Int(101325), Pow(10, 15)), Sym("femtopa")));
        c.put("FEMTOPASCAL:ATTOPASCAL", Mul(Rat(Int(1), Int(1000)), Sym("femtopa")));
        c.put("FEMTOPASCAL:BAR", Mul(Pow(10, 20), Sym("femtopa")));
        c.put("FEMTOPASCAL:CENTIBAR", Mul(Pow(10, 18), Sym("femtopa")));
        c.put("FEMTOPASCAL:CENTIPASCAL", Mul(Pow(10, 13), Sym("femtopa")));
        c.put("FEMTOPASCAL:DECAPASCAL", Mul(Pow(10, 16), Sym("femtopa")));
        c.put("FEMTOPASCAL:DECIBAR", Mul(Pow(10, 19), Sym("femtopa")));
        c.put("FEMTOPASCAL:DECIPASCAL", Mul(Pow(10, 14), Sym("femtopa")));
        c.put("FEMTOPASCAL:EXAPASCAL", Mul(Pow(10, 33), Sym("femtopa")));
        c.put("FEMTOPASCAL:GIGAPASCAL", Mul(Pow(10, 24), Sym("femtopa")));
        c.put("FEMTOPASCAL:HECTOPASCAL", Mul(Pow(10, 17), Sym("femtopa")));
        c.put("FEMTOPASCAL:KILOBAR", Mul(Pow(10, 23), Sym("femtopa")));
        c.put("FEMTOPASCAL:KILOPASCAL", Mul(Pow(10, 18), Sym("femtopa")));
        c.put("FEMTOPASCAL:MEGABAR", Mul(Pow(10, 26), Sym("femtopa")));
        c.put("FEMTOPASCAL:MEGAPASCAL", Mul(Pow(10, 21), Sym("femtopa")));
        c.put("FEMTOPASCAL:MICROPASCAL", Mul(Pow(10, 9), Sym("femtopa")));
        c.put("FEMTOPASCAL:MILLIBAR", Mul(Pow(10, 17), Sym("femtopa")));
        c.put("FEMTOPASCAL:MILLIPASCAL", Mul(Pow(10, 12), Sym("femtopa")));
        c.put("FEMTOPASCAL:MILLITORR", Mul(Rat(Mul(Int(2533125), Pow(10, 9)), Int(19)), Sym("femtopa")));
        c.put("FEMTOPASCAL:MMHG", Mul(Mul(Int("133322387415"), Pow(10, 6)), Sym("femtopa")));
        c.put("FEMTOPASCAL:NANOPASCAL", Mul(Pow(10, 6), Sym("femtopa")));
        c.put("FEMTOPASCAL:PASCAL", Mul(Pow(10, 15), Sym("femtopa")));
        c.put("FEMTOPASCAL:PETAPASCAL", Mul(Pow(10, 30), Sym("femtopa")));
        c.put("FEMTOPASCAL:PICOPASCAL", Mul(Int(1000), Sym("femtopa")));
        c.put("FEMTOPASCAL:PSI", Mul(Mul(Int("689475729316836"), Pow(10, 4)), Sym("femtopa")));
        c.put("FEMTOPASCAL:TERAPASCAL", Mul(Pow(10, 27), Sym("femtopa")));
        c.put("FEMTOPASCAL:TORR", Mul(Rat(Mul(Int(2533125), Pow(10, 12)), Int(19)), Sym("femtopa")));
        c.put("FEMTOPASCAL:YOCTOPASCAL", Mul(Rat(Int(1), Pow(10, 9)), Sym("femtopa")));
        c.put("FEMTOPASCAL:YOTTAPASCAL", Mul(Pow(10, 39), Sym("femtopa")));
        c.put("FEMTOPASCAL:ZEPTOPASCAL", Mul(Rat(Int(1), Pow(10, 6)), Sym("femtopa")));
        c.put("FEMTOPASCAL:ZETTAPASCAL", Mul(Pow(10, 36), Sym("femtopa")));
        c.put("GIGAPASCAL:ATHMOSPHERE", Mul(Rat(Int(4053), Mul(Int(4), Pow(10, 7))), Sym("gigapa")));
        c.put("GIGAPASCAL:ATTOPASCAL", Mul(Rat(Int(1), Pow(10, 27)), Sym("gigapa")));
        c.put("GIGAPASCAL:BAR", Mul(Rat(Int(1), Pow(10, 4)), Sym("gigapa")));
        c.put("GIGAPASCAL:CENTIBAR", Mul(Rat(Int(1), Pow(10, 6)), Sym("gigapa")));
        c.put("GIGAPASCAL:CENTIPASCAL", Mul(Rat(Int(1), Pow(10, 11)), Sym("gigapa")));
        c.put("GIGAPASCAL:DECAPASCAL", Mul(Rat(Int(1), Pow(10, 8)), Sym("gigapa")));
        c.put("GIGAPASCAL:DECIBAR", Mul(Rat(Int(1), Pow(10, 5)), Sym("gigapa")));
        c.put("GIGAPASCAL:DECIPASCAL", Mul(Rat(Int(1), Pow(10, 10)), Sym("gigapa")));
        c.put("GIGAPASCAL:EXAPASCAL", Mul(Pow(10, 9), Sym("gigapa")));
        c.put("GIGAPASCAL:FEMTOPASCAL", Mul(Rat(Int(1), Pow(10, 24)), Sym("gigapa")));
        c.put("GIGAPASCAL:HECTOPASCAL", Mul(Rat(Int(1), Pow(10, 7)), Sym("gigapa")));
        c.put("GIGAPASCAL:KILOBAR", Mul(Rat(Int(1), Int(10)), Sym("gigapa")));
        c.put("GIGAPASCAL:KILOPASCAL", Mul(Rat(Int(1), Pow(10, 6)), Sym("gigapa")));
        c.put("GIGAPASCAL:MEGABAR", Mul(Int(100), Sym("gigapa")));
        c.put("GIGAPASCAL:MEGAPASCAL", Mul(Rat(Int(1), Int(1000)), Sym("gigapa")));
        c.put("GIGAPASCAL:MICROPASCAL", Mul(Rat(Int(1), Pow(10, 15)), Sym("gigapa")));
        c.put("GIGAPASCAL:MILLIBAR", Mul(Rat(Int(1), Pow(10, 7)), Sym("gigapa")));
        c.put("GIGAPASCAL:MILLIPASCAL", Mul(Rat(Int(1), Pow(10, 12)), Sym("gigapa")));
        c.put("GIGAPASCAL:MILLITORR", Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 11))), Sym("gigapa")));
        c.put("GIGAPASCAL:MMHG", Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 17))), Sym("gigapa")));
        c.put("GIGAPASCAL:NANOPASCAL", Mul(Rat(Int(1), Pow(10, 18)), Sym("gigapa")));
        c.put("GIGAPASCAL:PASCAL", Mul(Rat(Int(1), Pow(10, 9)), Sym("gigapa")));
        c.put("GIGAPASCAL:PETAPASCAL", Mul(Pow(10, 6), Sym("gigapa")));
        c.put("GIGAPASCAL:PICOPASCAL", Mul(Rat(Int(1), Pow(10, 21)), Sym("gigapa")));
        c.put("GIGAPASCAL:PSI", Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 18))), Sym("gigapa")));
        c.put("GIGAPASCAL:TERAPASCAL", Mul(Int(1000), Sym("gigapa")));
        c.put("GIGAPASCAL:TORR", Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 8))), Sym("gigapa")));
        c.put("GIGAPASCAL:YOCTOPASCAL", Mul(Rat(Int(1), Pow(10, 33)), Sym("gigapa")));
        c.put("GIGAPASCAL:YOTTAPASCAL", Mul(Pow(10, 15), Sym("gigapa")));
        c.put("GIGAPASCAL:ZEPTOPASCAL", Mul(Rat(Int(1), Pow(10, 30)), Sym("gigapa")));
        c.put("GIGAPASCAL:ZETTAPASCAL", Mul(Pow(10, 12), Sym("gigapa")));
        c.put("HECTOPASCAL:ATHMOSPHERE", Mul(Rat(Int(4053), Int(4)), Sym("hectopa")));
        c.put("HECTOPASCAL:ATTOPASCAL", Mul(Rat(Int(1), Pow(10, 20)), Sym("hectopa")));
        c.put("HECTOPASCAL:BAR", Mul(Int(1000), Sym("hectopa")));
        c.put("HECTOPASCAL:CENTIBAR", Mul(Int(10), Sym("hectopa")));
        c.put("HECTOPASCAL:CENTIPASCAL", Mul(Rat(Int(1), Pow(10, 4)), Sym("hectopa")));
        c.put("HECTOPASCAL:DECAPASCAL", Mul(Rat(Int(1), Int(10)), Sym("hectopa")));
        c.put("HECTOPASCAL:DECIBAR", Mul(Int(100), Sym("hectopa")));
        c.put("HECTOPASCAL:DECIPASCAL", Mul(Rat(Int(1), Int(1000)), Sym("hectopa")));
        c.put("HECTOPASCAL:EXAPASCAL", Mul(Pow(10, 16), Sym("hectopa")));
        c.put("HECTOPASCAL:FEMTOPASCAL", Mul(Rat(Int(1), Pow(10, 17)), Sym("hectopa")));
        c.put("HECTOPASCAL:GIGAPASCAL", Mul(Pow(10, 7), Sym("hectopa")));
        c.put("HECTOPASCAL:KILOBAR", Mul(Pow(10, 6), Sym("hectopa")));
        c.put("HECTOPASCAL:KILOPASCAL", Mul(Int(10), Sym("hectopa")));
        c.put("HECTOPASCAL:MEGABAR", Mul(Pow(10, 9), Sym("hectopa")));
        c.put("HECTOPASCAL:MEGAPASCAL", Mul(Pow(10, 4), Sym("hectopa")));
        c.put("HECTOPASCAL:MICROPASCAL", Mul(Rat(Int(1), Pow(10, 8)), Sym("hectopa")));
        c.put("HECTOPASCAL:MILLIBAR", Sym("hectopa"));
        c.put("HECTOPASCAL:MILLIPASCAL", Mul(Rat(Int(1), Pow(10, 5)), Sym("hectopa")));
        c.put("HECTOPASCAL:MILLITORR", Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 4))), Sym("hectopa")));
        c.put("HECTOPASCAL:MMHG", Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 10))), Sym("hectopa")));
        c.put("HECTOPASCAL:NANOPASCAL", Mul(Rat(Int(1), Pow(10, 11)), Sym("hectopa")));
        c.put("HECTOPASCAL:PASCAL", Mul(Rat(Int(1), Int(100)), Sym("hectopa")));
        c.put("HECTOPASCAL:PETAPASCAL", Mul(Pow(10, 13), Sym("hectopa")));
        c.put("HECTOPASCAL:PICOPASCAL", Mul(Rat(Int(1), Pow(10, 14)), Sym("hectopa")));
        c.put("HECTOPASCAL:PSI", Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 11))), Sym("hectopa")));
        c.put("HECTOPASCAL:TERAPASCAL", Mul(Pow(10, 10), Sym("hectopa")));
        c.put("HECTOPASCAL:TORR", Mul(Rat(Int(4053), Int(3040)), Sym("hectopa")));
        c.put("HECTOPASCAL:YOCTOPASCAL", Mul(Rat(Int(1), Pow(10, 26)), Sym("hectopa")));
        c.put("HECTOPASCAL:YOTTAPASCAL", Mul(Pow(10, 22), Sym("hectopa")));
        c.put("HECTOPASCAL:ZEPTOPASCAL", Mul(Rat(Int(1), Pow(10, 23)), Sym("hectopa")));
        c.put("HECTOPASCAL:ZETTAPASCAL", Mul(Pow(10, 19), Sym("hectopa")));
        c.put("KILOBAR:ATHMOSPHERE", Mul(Rat(Int(4053), Mul(Int(4), Pow(10, 6))), Sym("kbar")));
        c.put("KILOBAR:ATTOPASCAL", Mul(Rat(Int(1), Pow(10, 26)), Sym("kbar")));
        c.put("KILOBAR:BAR", Mul(Rat(Int(1), Int(1000)), Sym("kbar")));
        c.put("KILOBAR:CENTIBAR", Mul(Rat(Int(1), Pow(10, 5)), Sym("kbar")));
        c.put("KILOBAR:CENTIPASCAL", Mul(Rat(Int(1), Pow(10, 10)), Sym("kbar")));
        c.put("KILOBAR:DECAPASCAL", Mul(Rat(Int(1), Pow(10, 7)), Sym("kbar")));
        c.put("KILOBAR:DECIBAR", Mul(Rat(Int(1), Pow(10, 4)), Sym("kbar")));
        c.put("KILOBAR:DECIPASCAL", Mul(Rat(Int(1), Pow(10, 9)), Sym("kbar")));
        c.put("KILOBAR:EXAPASCAL", Mul(Pow(10, 10), Sym("kbar")));
        c.put("KILOBAR:FEMTOPASCAL", Mul(Rat(Int(1), Pow(10, 23)), Sym("kbar")));
        c.put("KILOBAR:GIGAPASCAL", Mul(Int(10), Sym("kbar")));
        c.put("KILOBAR:HECTOPASCAL", Mul(Rat(Int(1), Pow(10, 6)), Sym("kbar")));
        c.put("KILOBAR:KILOPASCAL", Mul(Rat(Int(1), Pow(10, 5)), Sym("kbar")));
        c.put("KILOBAR:MEGABAR", Mul(Int(1000), Sym("kbar")));
        c.put("KILOBAR:MEGAPASCAL", Mul(Rat(Int(1), Int(100)), Sym("kbar")));
        c.put("KILOBAR:MICROPASCAL", Mul(Rat(Int(1), Pow(10, 14)), Sym("kbar")));
        c.put("KILOBAR:MILLIBAR", Mul(Rat(Int(1), Pow(10, 6)), Sym("kbar")));
        c.put("KILOBAR:MILLIPASCAL", Mul(Rat(Int(1), Pow(10, 11)), Sym("kbar")));
        c.put("KILOBAR:MILLITORR", Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 10))), Sym("kbar")));
        c.put("KILOBAR:MMHG", Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 16))), Sym("kbar")));
        c.put("KILOBAR:NANOPASCAL", Mul(Rat(Int(1), Pow(10, 17)), Sym("kbar")));
        c.put("KILOBAR:PASCAL", Mul(Rat(Int(1), Pow(10, 8)), Sym("kbar")));
        c.put("KILOBAR:PETAPASCAL", Mul(Pow(10, 7), Sym("kbar")));
        c.put("KILOBAR:PICOPASCAL", Mul(Rat(Int(1), Pow(10, 20)), Sym("kbar")));
        c.put("KILOBAR:PSI", Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 17))), Sym("kbar")));
        c.put("KILOBAR:TERAPASCAL", Mul(Pow(10, 4), Sym("kbar")));
        c.put("KILOBAR:TORR", Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 7))), Sym("kbar")));
        c.put("KILOBAR:YOCTOPASCAL", Mul(Rat(Int(1), Pow(10, 32)), Sym("kbar")));
        c.put("KILOBAR:YOTTAPASCAL", Mul(Pow(10, 16), Sym("kbar")));
        c.put("KILOBAR:ZEPTOPASCAL", Mul(Rat(Int(1), Pow(10, 29)), Sym("kbar")));
        c.put("KILOBAR:ZETTAPASCAL", Mul(Pow(10, 13), Sym("kbar")));
        c.put("KILOPASCAL:ATHMOSPHERE", Mul(Rat(Int(4053), Int(40)), Sym("kilopa")));
        c.put("KILOPASCAL:ATTOPASCAL", Mul(Rat(Int(1), Pow(10, 21)), Sym("kilopa")));
        c.put("KILOPASCAL:BAR", Mul(Int(100), Sym("kilopa")));
        c.put("KILOPASCAL:CENTIBAR", Sym("kilopa"));
        c.put("KILOPASCAL:CENTIPASCAL", Mul(Rat(Int(1), Pow(10, 5)), Sym("kilopa")));
        c.put("KILOPASCAL:DECAPASCAL", Mul(Rat(Int(1), Int(100)), Sym("kilopa")));
        c.put("KILOPASCAL:DECIBAR", Mul(Int(10), Sym("kilopa")));
        c.put("KILOPASCAL:DECIPASCAL", Mul(Rat(Int(1), Pow(10, 4)), Sym("kilopa")));
        c.put("KILOPASCAL:EXAPASCAL", Mul(Pow(10, 15), Sym("kilopa")));
        c.put("KILOPASCAL:FEMTOPASCAL", Mul(Rat(Int(1), Pow(10, 18)), Sym("kilopa")));
        c.put("KILOPASCAL:GIGAPASCAL", Mul(Pow(10, 6), Sym("kilopa")));
        c.put("KILOPASCAL:HECTOPASCAL", Mul(Rat(Int(1), Int(10)), Sym("kilopa")));
        c.put("KILOPASCAL:KILOBAR", Mul(Pow(10, 5), Sym("kilopa")));
        c.put("KILOPASCAL:MEGABAR", Mul(Pow(10, 8), Sym("kilopa")));
        c.put("KILOPASCAL:MEGAPASCAL", Mul(Int(1000), Sym("kilopa")));
        c.put("KILOPASCAL:MICROPASCAL", Mul(Rat(Int(1), Pow(10, 9)), Sym("kilopa")));
        c.put("KILOPASCAL:MILLIBAR", Mul(Rat(Int(1), Int(10)), Sym("kilopa")));
        c.put("KILOPASCAL:MILLIPASCAL", Mul(Rat(Int(1), Pow(10, 6)), Sym("kilopa")));
        c.put("KILOPASCAL:MILLITORR", Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 5))), Sym("kilopa")));
        c.put("KILOPASCAL:MMHG", Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 11))), Sym("kilopa")));
        c.put("KILOPASCAL:NANOPASCAL", Mul(Rat(Int(1), Pow(10, 12)), Sym("kilopa")));
        c.put("KILOPASCAL:PASCAL", Mul(Rat(Int(1), Int(1000)), Sym("kilopa")));
        c.put("KILOPASCAL:PETAPASCAL", Mul(Pow(10, 12), Sym("kilopa")));
        c.put("KILOPASCAL:PICOPASCAL", Mul(Rat(Int(1), Pow(10, 15)), Sym("kilopa")));
        c.put("KILOPASCAL:PSI", Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 12))), Sym("kilopa")));
        c.put("KILOPASCAL:TERAPASCAL", Mul(Pow(10, 9), Sym("kilopa")));
        c.put("KILOPASCAL:TORR", Mul(Rat(Int(4053), Int(30400)), Sym("kilopa")));
        c.put("KILOPASCAL:YOCTOPASCAL", Mul(Rat(Int(1), Pow(10, 27)), Sym("kilopa")));
        c.put("KILOPASCAL:YOTTAPASCAL", Mul(Pow(10, 21), Sym("kilopa")));
        c.put("KILOPASCAL:ZEPTOPASCAL", Mul(Rat(Int(1), Pow(10, 24)), Sym("kilopa")));
        c.put("KILOPASCAL:ZETTAPASCAL", Mul(Pow(10, 18), Sym("kilopa")));
        c.put("MEGABAR:ATHMOSPHERE", Mul(Rat(Int(4053), Mul(Int(4), Pow(10, 9))), Sym("megabar")));
        c.put("MEGABAR:ATTOPASCAL", Mul(Rat(Int(1), Pow(10, 29)), Sym("megabar")));
        c.put("MEGABAR:BAR", Mul(Rat(Int(1), Pow(10, 6)), Sym("megabar")));
        c.put("MEGABAR:CENTIBAR", Mul(Rat(Int(1), Pow(10, 8)), Sym("megabar")));
        c.put("MEGABAR:CENTIPASCAL", Mul(Rat(Int(1), Pow(10, 13)), Sym("megabar")));
        c.put("MEGABAR:DECAPASCAL", Mul(Rat(Int(1), Pow(10, 10)), Sym("megabar")));
        c.put("MEGABAR:DECIBAR", Mul(Rat(Int(1), Pow(10, 7)), Sym("megabar")));
        c.put("MEGABAR:DECIPASCAL", Mul(Rat(Int(1), Pow(10, 12)), Sym("megabar")));
        c.put("MEGABAR:EXAPASCAL", Mul(Pow(10, 7), Sym("megabar")));
        c.put("MEGABAR:FEMTOPASCAL", Mul(Rat(Int(1), Pow(10, 26)), Sym("megabar")));
        c.put("MEGABAR:GIGAPASCAL", Mul(Rat(Int(1), Int(100)), Sym("megabar")));
        c.put("MEGABAR:HECTOPASCAL", Mul(Rat(Int(1), Pow(10, 9)), Sym("megabar")));
        c.put("MEGABAR:KILOBAR", Mul(Rat(Int(1), Int(1000)), Sym("megabar")));
        c.put("MEGABAR:KILOPASCAL", Mul(Rat(Int(1), Pow(10, 8)), Sym("megabar")));
        c.put("MEGABAR:MEGAPASCAL", Mul(Rat(Int(1), Pow(10, 5)), Sym("megabar")));
        c.put("MEGABAR:MICROPASCAL", Mul(Rat(Int(1), Pow(10, 17)), Sym("megabar")));
        c.put("MEGABAR:MILLIBAR", Mul(Rat(Int(1), Pow(10, 9)), Sym("megabar")));
        c.put("MEGABAR:MILLIPASCAL", Mul(Rat(Int(1), Pow(10, 14)), Sym("megabar")));
        c.put("MEGABAR:MILLITORR", Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 13))), Sym("megabar")));
        c.put("MEGABAR:MMHG", Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 19))), Sym("megabar")));
        c.put("MEGABAR:NANOPASCAL", Mul(Rat(Int(1), Pow(10, 20)), Sym("megabar")));
        c.put("MEGABAR:PASCAL", Mul(Rat(Int(1), Pow(10, 11)), Sym("megabar")));
        c.put("MEGABAR:PETAPASCAL", Mul(Pow(10, 4), Sym("megabar")));
        c.put("MEGABAR:PICOPASCAL", Mul(Rat(Int(1), Pow(10, 23)), Sym("megabar")));
        c.put("MEGABAR:PSI", Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 20))), Sym("megabar")));
        c.put("MEGABAR:TERAPASCAL", Mul(Int(10), Sym("megabar")));
        c.put("MEGABAR:TORR", Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 10))), Sym("megabar")));
        c.put("MEGABAR:YOCTOPASCAL", Mul(Rat(Int(1), Pow(10, 35)), Sym("megabar")));
        c.put("MEGABAR:YOTTAPASCAL", Mul(Pow(10, 13), Sym("megabar")));
        c.put("MEGABAR:ZEPTOPASCAL", Mul(Rat(Int(1), Pow(10, 32)), Sym("megabar")));
        c.put("MEGABAR:ZETTAPASCAL", Mul(Pow(10, 10), Sym("megabar")));
        c.put("MEGAPASCAL:ATHMOSPHERE", Mul(Rat(Int(4053), Mul(Int(4), Pow(10, 4))), Sym("megapa")));
        c.put("MEGAPASCAL:ATTOPASCAL", Mul(Rat(Int(1), Pow(10, 24)), Sym("megapa")));
        c.put("MEGAPASCAL:BAR", Mul(Rat(Int(1), Int(10)), Sym("megapa")));
        c.put("MEGAPASCAL:CENTIBAR", Mul(Rat(Int(1), Int(1000)), Sym("megapa")));
        c.put("MEGAPASCAL:CENTIPASCAL", Mul(Rat(Int(1), Pow(10, 8)), Sym("megapa")));
        c.put("MEGAPASCAL:DECAPASCAL", Mul(Rat(Int(1), Pow(10, 5)), Sym("megapa")));
        c.put("MEGAPASCAL:DECIBAR", Mul(Rat(Int(1), Int(100)), Sym("megapa")));
        c.put("MEGAPASCAL:DECIPASCAL", Mul(Rat(Int(1), Pow(10, 7)), Sym("megapa")));
        c.put("MEGAPASCAL:EXAPASCAL", Mul(Pow(10, 12), Sym("megapa")));
        c.put("MEGAPASCAL:FEMTOPASCAL", Mul(Rat(Int(1), Pow(10, 21)), Sym("megapa")));
        c.put("MEGAPASCAL:GIGAPASCAL", Mul(Int(1000), Sym("megapa")));
        c.put("MEGAPASCAL:HECTOPASCAL", Mul(Rat(Int(1), Pow(10, 4)), Sym("megapa")));
        c.put("MEGAPASCAL:KILOBAR", Mul(Int(100), Sym("megapa")));
        c.put("MEGAPASCAL:KILOPASCAL", Mul(Rat(Int(1), Int(1000)), Sym("megapa")));
        c.put("MEGAPASCAL:MEGABAR", Mul(Pow(10, 5), Sym("megapa")));
        c.put("MEGAPASCAL:MICROPASCAL", Mul(Rat(Int(1), Pow(10, 12)), Sym("megapa")));
        c.put("MEGAPASCAL:MILLIBAR", Mul(Rat(Int(1), Pow(10, 4)), Sym("megapa")));
        c.put("MEGAPASCAL:MILLIPASCAL", Mul(Rat(Int(1), Pow(10, 9)), Sym("megapa")));
        c.put("MEGAPASCAL:MILLITORR", Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 8))), Sym("megapa")));
        c.put("MEGAPASCAL:MMHG", Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 14))), Sym("megapa")));
        c.put("MEGAPASCAL:NANOPASCAL", Mul(Rat(Int(1), Pow(10, 15)), Sym("megapa")));
        c.put("MEGAPASCAL:PASCAL", Mul(Rat(Int(1), Pow(10, 6)), Sym("megapa")));
        c.put("MEGAPASCAL:PETAPASCAL", Mul(Pow(10, 9), Sym("megapa")));
        c.put("MEGAPASCAL:PICOPASCAL", Mul(Rat(Int(1), Pow(10, 18)), Sym("megapa")));
        c.put("MEGAPASCAL:PSI", Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 15))), Sym("megapa")));
        c.put("MEGAPASCAL:TERAPASCAL", Mul(Pow(10, 6), Sym("megapa")));
        c.put("MEGAPASCAL:TORR", Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 5))), Sym("megapa")));
        c.put("MEGAPASCAL:YOCTOPASCAL", Mul(Rat(Int(1), Pow(10, 30)), Sym("megapa")));
        c.put("MEGAPASCAL:YOTTAPASCAL", Mul(Pow(10, 18), Sym("megapa")));
        c.put("MEGAPASCAL:ZEPTOPASCAL", Mul(Rat(Int(1), Pow(10, 27)), Sym("megapa")));
        c.put("MEGAPASCAL:ZETTAPASCAL", Mul(Pow(10, 15), Sym("megapa")));
        c.put("MICROPASCAL:ATHMOSPHERE", Mul(Mul(Int(101325), Pow(10, 6)), Sym("micropa")));
        c.put("MICROPASCAL:ATTOPASCAL", Mul(Rat(Int(1), Pow(10, 12)), Sym("micropa")));
        c.put("MICROPASCAL:BAR", Mul(Pow(10, 11), Sym("micropa")));
        c.put("MICROPASCAL:CENTIBAR", Mul(Pow(10, 9), Sym("micropa")));
        c.put("MICROPASCAL:CENTIPASCAL", Mul(Pow(10, 4), Sym("micropa")));
        c.put("MICROPASCAL:DECAPASCAL", Mul(Pow(10, 7), Sym("micropa")));
        c.put("MICROPASCAL:DECIBAR", Mul(Pow(10, 10), Sym("micropa")));
        c.put("MICROPASCAL:DECIPASCAL", Mul(Pow(10, 5), Sym("micropa")));
        c.put("MICROPASCAL:EXAPASCAL", Mul(Pow(10, 24), Sym("micropa")));
        c.put("MICROPASCAL:FEMTOPASCAL", Mul(Rat(Int(1), Pow(10, 9)), Sym("micropa")));
        c.put("MICROPASCAL:GIGAPASCAL", Mul(Pow(10, 15), Sym("micropa")));
        c.put("MICROPASCAL:HECTOPASCAL", Mul(Pow(10, 8), Sym("micropa")));
        c.put("MICROPASCAL:KILOBAR", Mul(Pow(10, 14), Sym("micropa")));
        c.put("MICROPASCAL:KILOPASCAL", Mul(Pow(10, 9), Sym("micropa")));
        c.put("MICROPASCAL:MEGABAR", Mul(Pow(10, 17), Sym("micropa")));
        c.put("MICROPASCAL:MEGAPASCAL", Mul(Pow(10, 12), Sym("micropa")));
        c.put("MICROPASCAL:MILLIBAR", Mul(Pow(10, 8), Sym("micropa")));
        c.put("MICROPASCAL:MILLIPASCAL", Mul(Int(1000), Sym("micropa")));
        c.put("MICROPASCAL:MILLITORR", Mul(Rat(Int(2533125), Int(19)), Sym("micropa")));
        c.put("MICROPASCAL:MMHG", Mul(Rat(Int("26664477483"), Int(200)), Sym("micropa")));
        c.put("MICROPASCAL:NANOPASCAL", Mul(Rat(Int(1), Int(1000)), Sym("micropa")));
        c.put("MICROPASCAL:PASCAL", Mul(Pow(10, 6), Sym("micropa")));
        c.put("MICROPASCAL:PETAPASCAL", Mul(Pow(10, 21), Sym("micropa")));
        c.put("MICROPASCAL:PICOPASCAL", Mul(Rat(Int(1), Pow(10, 6)), Sym("micropa")));
        c.put("MICROPASCAL:PSI", Mul(Rat(Int("172368932329209"), Int(25000)), Sym("micropa")));
        c.put("MICROPASCAL:TERAPASCAL", Mul(Pow(10, 18), Sym("micropa")));
        c.put("MICROPASCAL:TORR", Mul(Rat(Int("2533125000"), Int(19)), Sym("micropa")));
        c.put("MICROPASCAL:YOCTOPASCAL", Mul(Rat(Int(1), Pow(10, 18)), Sym("micropa")));
        c.put("MICROPASCAL:YOTTAPASCAL", Mul(Pow(10, 30), Sym("micropa")));
        c.put("MICROPASCAL:ZEPTOPASCAL", Mul(Rat(Int(1), Pow(10, 15)), Sym("micropa")));
        c.put("MICROPASCAL:ZETTAPASCAL", Mul(Pow(10, 27), Sym("micropa")));
        c.put("MILLIBAR:ATHMOSPHERE", Mul(Rat(Int(4053), Int(4)), Sym("mbar")));
        c.put("MILLIBAR:ATTOPASCAL", Mul(Rat(Int(1), Pow(10, 20)), Sym("mbar")));
        c.put("MILLIBAR:BAR", Mul(Int(1000), Sym("mbar")));
        c.put("MILLIBAR:CENTIBAR", Mul(Int(10), Sym("mbar")));
        c.put("MILLIBAR:CENTIPASCAL", Mul(Rat(Int(1), Pow(10, 4)), Sym("mbar")));
        c.put("MILLIBAR:DECAPASCAL", Mul(Rat(Int(1), Int(10)), Sym("mbar")));
        c.put("MILLIBAR:DECIBAR", Mul(Int(100), Sym("mbar")));
        c.put("MILLIBAR:DECIPASCAL", Mul(Rat(Int(1), Int(1000)), Sym("mbar")));
        c.put("MILLIBAR:EXAPASCAL", Mul(Pow(10, 16), Sym("mbar")));
        c.put("MILLIBAR:FEMTOPASCAL", Mul(Rat(Int(1), Pow(10, 17)), Sym("mbar")));
        c.put("MILLIBAR:GIGAPASCAL", Mul(Pow(10, 7), Sym("mbar")));
        c.put("MILLIBAR:HECTOPASCAL", Sym("mbar"));
        c.put("MILLIBAR:KILOBAR", Mul(Pow(10, 6), Sym("mbar")));
        c.put("MILLIBAR:KILOPASCAL", Mul(Int(10), Sym("mbar")));
        c.put("MILLIBAR:MEGABAR", Mul(Pow(10, 9), Sym("mbar")));
        c.put("MILLIBAR:MEGAPASCAL", Mul(Pow(10, 4), Sym("mbar")));
        c.put("MILLIBAR:MICROPASCAL", Mul(Rat(Int(1), Pow(10, 8)), Sym("mbar")));
        c.put("MILLIBAR:MILLIPASCAL", Mul(Rat(Int(1), Pow(10, 5)), Sym("mbar")));
        c.put("MILLIBAR:MILLITORR", Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 4))), Sym("mbar")));
        c.put("MILLIBAR:MMHG", Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 10))), Sym("mbar")));
        c.put("MILLIBAR:NANOPASCAL", Mul(Rat(Int(1), Pow(10, 11)), Sym("mbar")));
        c.put("MILLIBAR:PASCAL", Mul(Rat(Int(1), Int(100)), Sym("mbar")));
        c.put("MILLIBAR:PETAPASCAL", Mul(Pow(10, 13), Sym("mbar")));
        c.put("MILLIBAR:PICOPASCAL", Mul(Rat(Int(1), Pow(10, 14)), Sym("mbar")));
        c.put("MILLIBAR:PSI", Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 11))), Sym("mbar")));
        c.put("MILLIBAR:TERAPASCAL", Mul(Pow(10, 10), Sym("mbar")));
        c.put("MILLIBAR:TORR", Mul(Rat(Int(4053), Int(3040)), Sym("mbar")));
        c.put("MILLIBAR:YOCTOPASCAL", Mul(Rat(Int(1), Pow(10, 26)), Sym("mbar")));
        c.put("MILLIBAR:YOTTAPASCAL", Mul(Pow(10, 22), Sym("mbar")));
        c.put("MILLIBAR:ZEPTOPASCAL", Mul(Rat(Int(1), Pow(10, 23)), Sym("mbar")));
        c.put("MILLIBAR:ZETTAPASCAL", Mul(Pow(10, 19), Sym("mbar")));
        c.put("MILLIPASCAL:ATHMOSPHERE", Mul(Int(101325000), Sym("millipa")));
        c.put("MILLIPASCAL:ATTOPASCAL", Mul(Rat(Int(1), Pow(10, 15)), Sym("millipa")));
        c.put("MILLIPASCAL:BAR", Mul(Pow(10, 8), Sym("millipa")));
        c.put("MILLIPASCAL:CENTIBAR", Mul(Pow(10, 6), Sym("millipa")));
        c.put("MILLIPASCAL:CENTIPASCAL", Mul(Int(10), Sym("millipa")));
        c.put("MILLIPASCAL:DECAPASCAL", Mul(Pow(10, 4), Sym("millipa")));
        c.put("MILLIPASCAL:DECIBAR", Mul(Pow(10, 7), Sym("millipa")));
        c.put("MILLIPASCAL:DECIPASCAL", Mul(Int(100), Sym("millipa")));
        c.put("MILLIPASCAL:EXAPASCAL", Mul(Pow(10, 21), Sym("millipa")));
        c.put("MILLIPASCAL:FEMTOPASCAL", Mul(Rat(Int(1), Pow(10, 12)), Sym("millipa")));
        c.put("MILLIPASCAL:GIGAPASCAL", Mul(Pow(10, 12), Sym("millipa")));
        c.put("MILLIPASCAL:HECTOPASCAL", Mul(Pow(10, 5), Sym("millipa")));
        c.put("MILLIPASCAL:KILOBAR", Mul(Pow(10, 11), Sym("millipa")));
        c.put("MILLIPASCAL:KILOPASCAL", Mul(Pow(10, 6), Sym("millipa")));
        c.put("MILLIPASCAL:MEGABAR", Mul(Pow(10, 14), Sym("millipa")));
        c.put("MILLIPASCAL:MEGAPASCAL", Mul(Pow(10, 9), Sym("millipa")));
        c.put("MILLIPASCAL:MICROPASCAL", Mul(Rat(Int(1), Int(1000)), Sym("millipa")));
        c.put("MILLIPASCAL:MILLIBAR", Mul(Pow(10, 5), Sym("millipa")));
        c.put("MILLIPASCAL:MILLITORR", Mul(Rat(Int(20265), Int(152)), Sym("millipa")));
        c.put("MILLIPASCAL:MMHG", Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 5))), Sym("millipa")));
        c.put("MILLIPASCAL:NANOPASCAL", Mul(Rat(Int(1), Pow(10, 6)), Sym("millipa")));
        c.put("MILLIPASCAL:PASCAL", Mul(Int(1000), Sym("millipa")));
        c.put("MILLIPASCAL:PETAPASCAL", Mul(Pow(10, 18), Sym("millipa")));
        c.put("MILLIPASCAL:PICOPASCAL", Mul(Rat(Int(1), Pow(10, 9)), Sym("millipa")));
        c.put("MILLIPASCAL:PSI", Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 6))), Sym("millipa")));
        c.put("MILLIPASCAL:TERAPASCAL", Mul(Pow(10, 15), Sym("millipa")));
        c.put("MILLIPASCAL:TORR", Mul(Rat(Int(2533125), Int(19)), Sym("millipa")));
        c.put("MILLIPASCAL:YOCTOPASCAL", Mul(Rat(Int(1), Pow(10, 21)), Sym("millipa")));
        c.put("MILLIPASCAL:YOTTAPASCAL", Mul(Pow(10, 27), Sym("millipa")));
        c.put("MILLIPASCAL:ZEPTOPASCAL", Mul(Rat(Int(1), Pow(10, 18)), Sym("millipa")));
        c.put("MILLIPASCAL:ZETTAPASCAL", Mul(Pow(10, 24), Sym("millipa")));
        c.put("MILLITORR:ATHMOSPHERE", Mul(Mul(Int(76), Pow(10, 4)), Sym("mtorr")));
        c.put("MILLITORR:ATTOPASCAL", Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 12))), Sym("mtorr")));
        c.put("MILLITORR:BAR", Mul(Rat(Mul(Int(304), Pow(10, 7)), Int(4053)), Sym("mtorr")));
        c.put("MILLITORR:CENTIBAR", Mul(Rat(Mul(Int(304), Pow(10, 5)), Int(4053)), Sym("mtorr")));
        c.put("MILLITORR:CENTIPASCAL", Mul(Rat(Int(304), Int(4053)), Sym("mtorr")));
        c.put("MILLITORR:DECAPASCAL", Mul(Rat(Int(304000), Int(4053)), Sym("mtorr")));
        c.put("MILLITORR:DECIBAR", Mul(Rat(Mul(Int(304), Pow(10, 6)), Int(4053)), Sym("mtorr")));
        c.put("MILLITORR:DECIPASCAL", Mul(Rat(Int(3040), Int(4053)), Sym("mtorr")));
        c.put("MILLITORR:EXAPASCAL", Mul(Rat(Mul(Int(304), Pow(10, 20)), Int(4053)), Sym("mtorr")));
        c.put("MILLITORR:FEMTOPASCAL", Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 9))), Sym("mtorr")));
        c.put("MILLITORR:GIGAPASCAL", Mul(Rat(Mul(Int(304), Pow(10, 11)), Int(4053)), Sym("mtorr")));
        c.put("MILLITORR:HECTOPASCAL", Mul(Rat(Mul(Int(304), Pow(10, 4)), Int(4053)), Sym("mtorr")));
        c.put("MILLITORR:KILOBAR", Mul(Rat(Mul(Int(304), Pow(10, 10)), Int(4053)), Sym("mtorr")));
        c.put("MILLITORR:KILOPASCAL", Mul(Rat(Mul(Int(304), Pow(10, 5)), Int(4053)), Sym("mtorr")));
        c.put("MILLITORR:MEGABAR", Mul(Rat(Mul(Int(304), Pow(10, 13)), Int(4053)), Sym("mtorr")));
        c.put("MILLITORR:MEGAPASCAL", Mul(Rat(Mul(Int(304), Pow(10, 8)), Int(4053)), Sym("mtorr")));
        c.put("MILLITORR:MICROPASCAL", Mul(Rat(Int(19), Int(2533125)), Sym("mtorr")));
        c.put("MILLITORR:MILLIBAR", Mul(Rat(Mul(Int(304), Pow(10, 4)), Int(4053)), Sym("mtorr")));
        c.put("MILLITORR:MILLIPASCAL", Mul(Rat(Int(152), Int(20265)), Sym("mtorr")));
        c.put("MILLITORR:MMHG", Mul(Rat(Int("24125003437"), Int(24125000)), Sym("mtorr")));
        c.put("MILLITORR:NANOPASCAL", Mul(Rat(Int(19), Int("2533125000")), Sym("mtorr")));
        c.put("MILLITORR:PASCAL", Mul(Rat(Int(30400), Int(4053)), Sym("mtorr")));
        c.put("MILLITORR:PETAPASCAL", Mul(Rat(Mul(Int(304), Pow(10, 17)), Int(4053)), Sym("mtorr")));
        c.put("MILLITORR:PICOPASCAL", Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 6))), Sym("mtorr")));
        c.put("MILLITORR:PSI", Mul(Rat(Int("155952843535951"), Int("3015625000")), Sym("mtorr")));
        c.put("MILLITORR:TERAPASCAL", Mul(Rat(Mul(Int(304), Pow(10, 14)), Int(4053)), Sym("mtorr")));
        c.put("MILLITORR:TORR", Mul(Int(1000), Sym("mtorr")));
        c.put("MILLITORR:YOCTOPASCAL", Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 18))), Sym("mtorr")));
        c.put("MILLITORR:YOTTAPASCAL", Mul(Rat(Mul(Int(304), Pow(10, 26)), Int(4053)), Sym("mtorr")));
        c.put("MILLITORR:ZEPTOPASCAL", Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 15))), Sym("mtorr")));
        c.put("MILLITORR:ZETTAPASCAL", Mul(Rat(Mul(Int(304), Pow(10, 23)), Int(4053)), Sym("mtorr")));
        c.put("MMHG:ATHMOSPHERE", Mul(Rat(Mul(Int(965), Pow(10, 9)), Int(1269737023)), Sym("mmhg")));
        c.put("MMHG:ATTOPASCAL", Mul(Rat(Int(1), Mul(Int("133322387415"), Pow(10, 9))), Sym("mmhg")));
        c.put("MMHG:BAR", Mul(Rat(Mul(Int(2), Pow(10, 13)), Int("26664477483")), Sym("mmhg")));
        c.put("MMHG:CENTIBAR", Mul(Rat(Mul(Int(2), Pow(10, 11)), Int("26664477483")), Sym("mmhg")));
        c.put("MMHG:CENTIPASCAL", Mul(Rat(Mul(Int(2), Pow(10, 6)), Int("26664477483")), Sym("mmhg")));
        c.put("MMHG:DECAPASCAL", Mul(Rat(Mul(Int(2), Pow(10, 9)), Int("26664477483")), Sym("mmhg")));
        c.put("MMHG:DECIBAR", Mul(Rat(Mul(Int(2), Pow(10, 12)), Int("26664477483")), Sym("mmhg")));
        c.put("MMHG:DECIPASCAL", Mul(Rat(Mul(Int(2), Pow(10, 7)), Int("26664477483")), Sym("mmhg")));
        c.put("MMHG:EXAPASCAL", Mul(Rat(Mul(Int(2), Pow(10, 26)), Int("26664477483")), Sym("mmhg")));
        c.put("MMHG:FEMTOPASCAL", Mul(Rat(Int(1), Mul(Int("133322387415"), Pow(10, 6))), Sym("mmhg")));
        c.put("MMHG:GIGAPASCAL", Mul(Rat(Mul(Int(2), Pow(10, 17)), Int("26664477483")), Sym("mmhg")));
        c.put("MMHG:HECTOPASCAL", Mul(Rat(Mul(Int(2), Pow(10, 10)), Int("26664477483")), Sym("mmhg")));
        c.put("MMHG:KILOBAR", Mul(Rat(Mul(Int(2), Pow(10, 16)), Int("26664477483")), Sym("mmhg")));
        c.put("MMHG:KILOPASCAL", Mul(Rat(Mul(Int(2), Pow(10, 11)), Int("26664477483")), Sym("mmhg")));
        c.put("MMHG:MEGABAR", Mul(Rat(Mul(Int(2), Pow(10, 19)), Int("26664477483")), Sym("mmhg")));
        c.put("MMHG:MEGAPASCAL", Mul(Rat(Mul(Int(2), Pow(10, 14)), Int("26664477483")), Sym("mmhg")));
        c.put("MMHG:MICROPASCAL", Mul(Rat(Int(200), Int("26664477483")), Sym("mmhg")));
        c.put("MMHG:MILLIBAR", Mul(Rat(Mul(Int(2), Pow(10, 10)), Int("26664477483")), Sym("mmhg")));
        c.put("MMHG:MILLIPASCAL", Mul(Rat(Mul(Int(2), Pow(10, 5)), Int("26664477483")), Sym("mmhg")));
        c.put("MMHG:MILLITORR", Mul(Rat(Int(24125000), Int("24125003437")), Sym("mmhg")));
        c.put("MMHG:NANOPASCAL", Mul(Rat(Int(1), Int("133322387415")), Sym("mmhg")));
        c.put("MMHG:PASCAL", Mul(Rat(Mul(Int(2), Pow(10, 8)), Int("26664477483")), Sym("mmhg")));
        c.put("MMHG:PETAPASCAL", Mul(Rat(Mul(Int(2), Pow(10, 23)), Int("26664477483")), Sym("mmhg")));
        c.put("MMHG:PICOPASCAL", Mul(Rat(Int(1), Int("133322387415000")), Sym("mmhg")));
        c.put("MMHG:PSI", Mul(Rat(Int("8208044396629"), Int("158717127875")), Sym("mmhg")));
        c.put("MMHG:TERAPASCAL", Mul(Rat(Mul(Int(2), Pow(10, 20)), Int("26664477483")), Sym("mmhg")));
        c.put("MMHG:TORR", Mul(Rat(Mul(Int(24125), Pow(10, 6)), Int("24125003437")), Sym("mmhg")));
        c.put("MMHG:YOCTOPASCAL", Mul(Rat(Int(1), Mul(Int("133322387415"), Pow(10, 15))), Sym("mmhg")));
        c.put("MMHG:YOTTAPASCAL", Mul(Rat(Mul(Int(2), Pow(10, 32)), Int("26664477483")), Sym("mmhg")));
        c.put("MMHG:ZEPTOPASCAL", Mul(Rat(Int(1), Mul(Int("133322387415"), Pow(10, 12))), Sym("mmhg")));
        c.put("MMHG:ZETTAPASCAL", Mul(Rat(Mul(Int(2), Pow(10, 29)), Int("26664477483")), Sym("mmhg")));
        c.put("NANOPASCAL:ATHMOSPHERE", Mul(Mul(Int(101325), Pow(10, 9)), Sym("nanopa")));
        c.put("NANOPASCAL:ATTOPASCAL", Mul(Rat(Int(1), Pow(10, 9)), Sym("nanopa")));
        c.put("NANOPASCAL:BAR", Mul(Pow(10, 14), Sym("nanopa")));
        c.put("NANOPASCAL:CENTIBAR", Mul(Pow(10, 12), Sym("nanopa")));
        c.put("NANOPASCAL:CENTIPASCAL", Mul(Pow(10, 7), Sym("nanopa")));
        c.put("NANOPASCAL:DECAPASCAL", Mul(Pow(10, 10), Sym("nanopa")));
        c.put("NANOPASCAL:DECIBAR", Mul(Pow(10, 13), Sym("nanopa")));
        c.put("NANOPASCAL:DECIPASCAL", Mul(Pow(10, 8), Sym("nanopa")));
        c.put("NANOPASCAL:EXAPASCAL", Mul(Pow(10, 27), Sym("nanopa")));
        c.put("NANOPASCAL:FEMTOPASCAL", Mul(Rat(Int(1), Pow(10, 6)), Sym("nanopa")));
        c.put("NANOPASCAL:GIGAPASCAL", Mul(Pow(10, 18), Sym("nanopa")));
        c.put("NANOPASCAL:HECTOPASCAL", Mul(Pow(10, 11), Sym("nanopa")));
        c.put("NANOPASCAL:KILOBAR", Mul(Pow(10, 17), Sym("nanopa")));
        c.put("NANOPASCAL:KILOPASCAL", Mul(Pow(10, 12), Sym("nanopa")));
        c.put("NANOPASCAL:MEGABAR", Mul(Pow(10, 20), Sym("nanopa")));
        c.put("NANOPASCAL:MEGAPASCAL", Mul(Pow(10, 15), Sym("nanopa")));
        c.put("NANOPASCAL:MICROPASCAL", Mul(Int(1000), Sym("nanopa")));
        c.put("NANOPASCAL:MILLIBAR", Mul(Pow(10, 11), Sym("nanopa")));
        c.put("NANOPASCAL:MILLIPASCAL", Mul(Pow(10, 6), Sym("nanopa")));
        c.put("NANOPASCAL:MILLITORR", Mul(Rat(Int("2533125000"), Int(19)), Sym("nanopa")));
        c.put("NANOPASCAL:MMHG", Mul(Int("133322387415"), Sym("nanopa")));
        c.put("NANOPASCAL:PASCAL", Mul(Pow(10, 9), Sym("nanopa")));
        c.put("NANOPASCAL:PETAPASCAL", Mul(Pow(10, 24), Sym("nanopa")));
        c.put("NANOPASCAL:PICOPASCAL", Mul(Rat(Int(1), Int(1000)), Sym("nanopa")));
        c.put("NANOPASCAL:PSI", Mul(Rat(Int("172368932329209"), Int(25)), Sym("nanopa")));
        c.put("NANOPASCAL:TERAPASCAL", Mul(Pow(10, 21), Sym("nanopa")));
        c.put("NANOPASCAL:TORR", Mul(Rat(Mul(Int(2533125), Pow(10, 6)), Int(19)), Sym("nanopa")));
        c.put("NANOPASCAL:YOCTOPASCAL", Mul(Rat(Int(1), Pow(10, 15)), Sym("nanopa")));
        c.put("NANOPASCAL:YOTTAPASCAL", Mul(Pow(10, 33), Sym("nanopa")));
        c.put("NANOPASCAL:ZEPTOPASCAL", Mul(Rat(Int(1), Pow(10, 12)), Sym("nanopa")));
        c.put("NANOPASCAL:ZETTAPASCAL", Mul(Pow(10, 30), Sym("nanopa")));
        c.put("PASCAL:ATHMOSPHERE", Mul(Int(101325), Sym("pa")));
        c.put("PASCAL:ATTOPASCAL", Mul(Rat(Int(1), Pow(10, 18)), Sym("pa")));
        c.put("PASCAL:BAR", Mul(Pow(10, 5), Sym("pa")));
        c.put("PASCAL:CENTIBAR", Mul(Int(1000), Sym("pa")));
        c.put("PASCAL:CENTIPASCAL", Mul(Rat(Int(1), Int(100)), Sym("pa")));
        c.put("PASCAL:DECAPASCAL", Mul(Int(10), Sym("pa")));
        c.put("PASCAL:DECIBAR", Mul(Pow(10, 4), Sym("pa")));
        c.put("PASCAL:DECIPASCAL", Mul(Rat(Int(1), Int(10)), Sym("pa")));
        c.put("PASCAL:EXAPASCAL", Mul(Pow(10, 18), Sym("pa")));
        c.put("PASCAL:FEMTOPASCAL", Mul(Rat(Int(1), Pow(10, 15)), Sym("pa")));
        c.put("PASCAL:GIGAPASCAL", Mul(Pow(10, 9), Sym("pa")));
        c.put("PASCAL:HECTOPASCAL", Mul(Int(100), Sym("pa")));
        c.put("PASCAL:KILOBAR", Mul(Pow(10, 8), Sym("pa")));
        c.put("PASCAL:KILOPASCAL", Mul(Int(1000), Sym("pa")));
        c.put("PASCAL:MEGABAR", Mul(Pow(10, 11), Sym("pa")));
        c.put("PASCAL:MEGAPASCAL", Mul(Pow(10, 6), Sym("pa")));
        c.put("PASCAL:MICROPASCAL", Mul(Rat(Int(1), Pow(10, 6)), Sym("pa")));
        c.put("PASCAL:MILLIBAR", Mul(Int(100), Sym("pa")));
        c.put("PASCAL:MILLIPASCAL", Mul(Rat(Int(1), Int(1000)), Sym("pa")));
        c.put("PASCAL:MILLITORR", Mul(Rat(Int(4053), Int(30400)), Sym("pa")));
        c.put("PASCAL:MMHG", Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 8))), Sym("pa")));
        c.put("PASCAL:NANOPASCAL", Mul(Rat(Int(1), Pow(10, 9)), Sym("pa")));
        c.put("PASCAL:PETAPASCAL", Mul(Pow(10, 15), Sym("pa")));
        c.put("PASCAL:PICOPASCAL", Mul(Rat(Int(1), Pow(10, 12)), Sym("pa")));
        c.put("PASCAL:PSI", Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 9))), Sym("pa")));
        c.put("PASCAL:TERAPASCAL", Mul(Pow(10, 12), Sym("pa")));
        c.put("PASCAL:TORR", Mul(Rat(Int(20265), Int(152)), Sym("pa")));
        c.put("PASCAL:YOCTOPASCAL", Mul(Rat(Int(1), Pow(10, 24)), Sym("pa")));
        c.put("PASCAL:YOTTAPASCAL", Mul(Pow(10, 24), Sym("pa")));
        c.put("PASCAL:ZEPTOPASCAL", Mul(Rat(Int(1), Pow(10, 21)), Sym("pa")));
        c.put("PASCAL:ZETTAPASCAL", Mul(Pow(10, 21), Sym("pa")));
        c.put("PETAPASCAL:ATHMOSPHERE", Mul(Rat(Int(4053), Mul(Int(4), Pow(10, 13))), Sym("petapa")));
        c.put("PETAPASCAL:ATTOPASCAL", Mul(Rat(Int(1), Pow(10, 33)), Sym("petapa")));
        c.put("PETAPASCAL:BAR", Mul(Rat(Int(1), Pow(10, 10)), Sym("petapa")));
        c.put("PETAPASCAL:CENTIBAR", Mul(Rat(Int(1), Pow(10, 12)), Sym("petapa")));
        c.put("PETAPASCAL:CENTIPASCAL", Mul(Rat(Int(1), Pow(10, 17)), Sym("petapa")));
        c.put("PETAPASCAL:DECAPASCAL", Mul(Rat(Int(1), Pow(10, 14)), Sym("petapa")));
        c.put("PETAPASCAL:DECIBAR", Mul(Rat(Int(1), Pow(10, 11)), Sym("petapa")));
        c.put("PETAPASCAL:DECIPASCAL", Mul(Rat(Int(1), Pow(10, 16)), Sym("petapa")));
        c.put("PETAPASCAL:EXAPASCAL", Mul(Int(1000), Sym("petapa")));
        c.put("PETAPASCAL:FEMTOPASCAL", Mul(Rat(Int(1), Pow(10, 30)), Sym("petapa")));
        c.put("PETAPASCAL:GIGAPASCAL", Mul(Rat(Int(1), Pow(10, 6)), Sym("petapa")));
        c.put("PETAPASCAL:HECTOPASCAL", Mul(Rat(Int(1), Pow(10, 13)), Sym("petapa")));
        c.put("PETAPASCAL:KILOBAR", Mul(Rat(Int(1), Pow(10, 7)), Sym("petapa")));
        c.put("PETAPASCAL:KILOPASCAL", Mul(Rat(Int(1), Pow(10, 12)), Sym("petapa")));
        c.put("PETAPASCAL:MEGABAR", Mul(Rat(Int(1), Pow(10, 4)), Sym("petapa")));
        c.put("PETAPASCAL:MEGAPASCAL", Mul(Rat(Int(1), Pow(10, 9)), Sym("petapa")));
        c.put("PETAPASCAL:MICROPASCAL", Mul(Rat(Int(1), Pow(10, 21)), Sym("petapa")));
        c.put("PETAPASCAL:MILLIBAR", Mul(Rat(Int(1), Pow(10, 13)), Sym("petapa")));
        c.put("PETAPASCAL:MILLIPASCAL", Mul(Rat(Int(1), Pow(10, 18)), Sym("petapa")));
        c.put("PETAPASCAL:MILLITORR", Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 17))), Sym("petapa")));
        c.put("PETAPASCAL:MMHG", Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 23))), Sym("petapa")));
        c.put("PETAPASCAL:NANOPASCAL", Mul(Rat(Int(1), Pow(10, 24)), Sym("petapa")));
        c.put("PETAPASCAL:PASCAL", Mul(Rat(Int(1), Pow(10, 15)), Sym("petapa")));
        c.put("PETAPASCAL:PICOPASCAL", Mul(Rat(Int(1), Pow(10, 27)), Sym("petapa")));
        c.put("PETAPASCAL:PSI", Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 24))), Sym("petapa")));
        c.put("PETAPASCAL:TERAPASCAL", Mul(Rat(Int(1), Int(1000)), Sym("petapa")));
        c.put("PETAPASCAL:TORR", Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 14))), Sym("petapa")));
        c.put("PETAPASCAL:YOCTOPASCAL", Mul(Rat(Int(1), Pow(10, 39)), Sym("petapa")));
        c.put("PETAPASCAL:YOTTAPASCAL", Mul(Pow(10, 9), Sym("petapa")));
        c.put("PETAPASCAL:ZEPTOPASCAL", Mul(Rat(Int(1), Pow(10, 36)), Sym("petapa")));
        c.put("PETAPASCAL:ZETTAPASCAL", Mul(Pow(10, 6), Sym("petapa")));
        c.put("PICOPASCAL:ATHMOSPHERE", Mul(Mul(Int(101325), Pow(10, 12)), Sym("picopa")));
        c.put("PICOPASCAL:ATTOPASCAL", Mul(Rat(Int(1), Pow(10, 6)), Sym("picopa")));
        c.put("PICOPASCAL:BAR", Mul(Pow(10, 17), Sym("picopa")));
        c.put("PICOPASCAL:CENTIBAR", Mul(Pow(10, 15), Sym("picopa")));
        c.put("PICOPASCAL:CENTIPASCAL", Mul(Pow(10, 10), Sym("picopa")));
        c.put("PICOPASCAL:DECAPASCAL", Mul(Pow(10, 13), Sym("picopa")));
        c.put("PICOPASCAL:DECIBAR", Mul(Pow(10, 16), Sym("picopa")));
        c.put("PICOPASCAL:DECIPASCAL", Mul(Pow(10, 11), Sym("picopa")));
        c.put("PICOPASCAL:EXAPASCAL", Mul(Pow(10, 30), Sym("picopa")));
        c.put("PICOPASCAL:FEMTOPASCAL", Mul(Rat(Int(1), Int(1000)), Sym("picopa")));
        c.put("PICOPASCAL:GIGAPASCAL", Mul(Pow(10, 21), Sym("picopa")));
        c.put("PICOPASCAL:HECTOPASCAL", Mul(Pow(10, 14), Sym("picopa")));
        c.put("PICOPASCAL:KILOBAR", Mul(Pow(10, 20), Sym("picopa")));
        c.put("PICOPASCAL:KILOPASCAL", Mul(Pow(10, 15), Sym("picopa")));
        c.put("PICOPASCAL:MEGABAR", Mul(Pow(10, 23), Sym("picopa")));
        c.put("PICOPASCAL:MEGAPASCAL", Mul(Pow(10, 18), Sym("picopa")));
        c.put("PICOPASCAL:MICROPASCAL", Mul(Pow(10, 6), Sym("picopa")));
        c.put("PICOPASCAL:MILLIBAR", Mul(Pow(10, 14), Sym("picopa")));
        c.put("PICOPASCAL:MILLIPASCAL", Mul(Pow(10, 9), Sym("picopa")));
        c.put("PICOPASCAL:MILLITORR", Mul(Rat(Mul(Int(2533125), Pow(10, 6)), Int(19)), Sym("picopa")));
        c.put("PICOPASCAL:MMHG", Mul(Int("133322387415000"), Sym("picopa")));
        c.put("PICOPASCAL:NANOPASCAL", Mul(Int(1000), Sym("picopa")));
        c.put("PICOPASCAL:PASCAL", Mul(Pow(10, 12), Sym("picopa")));
        c.put("PICOPASCAL:PETAPASCAL", Mul(Pow(10, 27), Sym("picopa")));
        c.put("PICOPASCAL:PSI", Mul(Int("6894757293168360"), Sym("picopa")));
        c.put("PICOPASCAL:TERAPASCAL", Mul(Pow(10, 24), Sym("picopa")));
        c.put("PICOPASCAL:TORR", Mul(Rat(Mul(Int(2533125), Pow(10, 9)), Int(19)), Sym("picopa")));
        c.put("PICOPASCAL:YOCTOPASCAL", Mul(Rat(Int(1), Pow(10, 12)), Sym("picopa")));
        c.put("PICOPASCAL:YOTTAPASCAL", Mul(Pow(10, 36), Sym("picopa")));
        c.put("PICOPASCAL:ZEPTOPASCAL", Mul(Rat(Int(1), Pow(10, 9)), Sym("picopa")));
        c.put("PICOPASCAL:ZETTAPASCAL", Mul(Pow(10, 33), Sym("picopa")));
        c.put("PSI:ATHMOSPHERE", Mul(Rat(Mul(Int(120625), Pow(10, 9)), Int("8208044396629")), Sym("psi")));
        c.put("PSI:ATTOPASCAL", Mul(Rat(Int(1), Mul(Int("689475729316836"), Pow(10, 7))), Sym("psi")));
        c.put("PSI:BAR", Mul(Rat(Mul(Int(25), Pow(10, 14)), Int("172368932329209")), Sym("psi")));
        c.put("PSI:CENTIBAR", Mul(Rat(Mul(Int(25), Pow(10, 12)), Int("172368932329209")), Sym("psi")));
        c.put("PSI:CENTIPASCAL", Mul(Rat(Mul(Int(25), Pow(10, 7)), Int("172368932329209")), Sym("psi")));
        c.put("PSI:DECAPASCAL", Mul(Rat(Mul(Int(25), Pow(10, 10)), Int("172368932329209")), Sym("psi")));
        c.put("PSI:DECIBAR", Mul(Rat(Mul(Int(25), Pow(10, 13)), Int("172368932329209")), Sym("psi")));
        c.put("PSI:DECIPASCAL", Mul(Rat(Mul(Int(25), Pow(10, 8)), Int("172368932329209")), Sym("psi")));
        c.put("PSI:EXAPASCAL", Mul(Rat(Mul(Int(25), Pow(10, 27)), Int("172368932329209")), Sym("psi")));
        c.put("PSI:FEMTOPASCAL", Mul(Rat(Int(1), Mul(Int("689475729316836"), Pow(10, 4))), Sym("psi")));
        c.put("PSI:GIGAPASCAL", Mul(Rat(Mul(Int(25), Pow(10, 18)), Int("172368932329209")), Sym("psi")));
        c.put("PSI:HECTOPASCAL", Mul(Rat(Mul(Int(25), Pow(10, 11)), Int("172368932329209")), Sym("psi")));
        c.put("PSI:KILOBAR", Mul(Rat(Mul(Int(25), Pow(10, 17)), Int("172368932329209")), Sym("psi")));
        c.put("PSI:KILOPASCAL", Mul(Rat(Mul(Int(25), Pow(10, 12)), Int("172368932329209")), Sym("psi")));
        c.put("PSI:MEGABAR", Mul(Rat(Mul(Int(25), Pow(10, 20)), Int("172368932329209")), Sym("psi")));
        c.put("PSI:MEGAPASCAL", Mul(Rat(Mul(Int(25), Pow(10, 15)), Int("172368932329209")), Sym("psi")));
        c.put("PSI:MICROPASCAL", Mul(Rat(Int(25000), Int("172368932329209")), Sym("psi")));
        c.put("PSI:MILLIBAR", Mul(Rat(Mul(Int(25), Pow(10, 11)), Int("172368932329209")), Sym("psi")));
        c.put("PSI:MILLIPASCAL", Mul(Rat(Mul(Int(25), Pow(10, 6)), Int("172368932329209")), Sym("psi")));
        c.put("PSI:MILLITORR", Mul(Rat(Int("3015625000"), Int("155952843535951")), Sym("psi")));
        c.put("PSI:MMHG", Mul(Rat(Int("158717127875"), Int("8208044396629")), Sym("psi")));
        c.put("PSI:NANOPASCAL", Mul(Rat(Int(25), Int("172368932329209")), Sym("psi")));
        c.put("PSI:PASCAL", Mul(Rat(Mul(Int(25), Pow(10, 9)), Int("172368932329209")), Sym("psi")));
        c.put("PSI:PETAPASCAL", Mul(Rat(Mul(Int(25), Pow(10, 24)), Int("172368932329209")), Sym("psi")));
        c.put("PSI:PICOPASCAL", Mul(Rat(Int(1), Int("6894757293168360")), Sym("psi")));
        c.put("PSI:TERAPASCAL", Mul(Rat(Mul(Int(25), Pow(10, 21)), Int("172368932329209")), Sym("psi")));
        c.put("PSI:TORR", Mul(Rat(Mul(Int(3015625), Pow(10, 6)), Int("155952843535951")), Sym("psi")));
        c.put("PSI:YOCTOPASCAL", Mul(Rat(Int(1), Mul(Int("689475729316836"), Pow(10, 13))), Sym("psi")));
        c.put("PSI:YOTTAPASCAL", Mul(Rat(Mul(Int(25), Pow(10, 33)), Int("172368932329209")), Sym("psi")));
        c.put("PSI:ZEPTOPASCAL", Mul(Rat(Int(1), Mul(Int("689475729316836"), Pow(10, 10))), Sym("psi")));
        c.put("PSI:ZETTAPASCAL", Mul(Rat(Mul(Int(25), Pow(10, 30)), Int("172368932329209")), Sym("psi")));
        c.put("TERAPASCAL:ATHMOSPHERE", Mul(Rat(Int(4053), Mul(Int(4), Pow(10, 10))), Sym("terapa")));
        c.put("TERAPASCAL:ATTOPASCAL", Mul(Rat(Int(1), Pow(10, 30)), Sym("terapa")));
        c.put("TERAPASCAL:BAR", Mul(Rat(Int(1), Pow(10, 7)), Sym("terapa")));
        c.put("TERAPASCAL:CENTIBAR", Mul(Rat(Int(1), Pow(10, 9)), Sym("terapa")));
        c.put("TERAPASCAL:CENTIPASCAL", Mul(Rat(Int(1), Pow(10, 14)), Sym("terapa")));
        c.put("TERAPASCAL:DECAPASCAL", Mul(Rat(Int(1), Pow(10, 11)), Sym("terapa")));
        c.put("TERAPASCAL:DECIBAR", Mul(Rat(Int(1), Pow(10, 8)), Sym("terapa")));
        c.put("TERAPASCAL:DECIPASCAL", Mul(Rat(Int(1), Pow(10, 13)), Sym("terapa")));
        c.put("TERAPASCAL:EXAPASCAL", Mul(Pow(10, 6), Sym("terapa")));
        c.put("TERAPASCAL:FEMTOPASCAL", Mul(Rat(Int(1), Pow(10, 27)), Sym("terapa")));
        c.put("TERAPASCAL:GIGAPASCAL", Mul(Rat(Int(1), Int(1000)), Sym("terapa")));
        c.put("TERAPASCAL:HECTOPASCAL", Mul(Rat(Int(1), Pow(10, 10)), Sym("terapa")));
        c.put("TERAPASCAL:KILOBAR", Mul(Rat(Int(1), Pow(10, 4)), Sym("terapa")));
        c.put("TERAPASCAL:KILOPASCAL", Mul(Rat(Int(1), Pow(10, 9)), Sym("terapa")));
        c.put("TERAPASCAL:MEGABAR", Mul(Rat(Int(1), Int(10)), Sym("terapa")));
        c.put("TERAPASCAL:MEGAPASCAL", Mul(Rat(Int(1), Pow(10, 6)), Sym("terapa")));
        c.put("TERAPASCAL:MICROPASCAL", Mul(Rat(Int(1), Pow(10, 18)), Sym("terapa")));
        c.put("TERAPASCAL:MILLIBAR", Mul(Rat(Int(1), Pow(10, 10)), Sym("terapa")));
        c.put("TERAPASCAL:MILLIPASCAL", Mul(Rat(Int(1), Pow(10, 15)), Sym("terapa")));
        c.put("TERAPASCAL:MILLITORR", Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 14))), Sym("terapa")));
        c.put("TERAPASCAL:MMHG", Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 20))), Sym("terapa")));
        c.put("TERAPASCAL:NANOPASCAL", Mul(Rat(Int(1), Pow(10, 21)), Sym("terapa")));
        c.put("TERAPASCAL:PASCAL", Mul(Rat(Int(1), Pow(10, 12)), Sym("terapa")));
        c.put("TERAPASCAL:PETAPASCAL", Mul(Int(1000), Sym("terapa")));
        c.put("TERAPASCAL:PICOPASCAL", Mul(Rat(Int(1), Pow(10, 24)), Sym("terapa")));
        c.put("TERAPASCAL:PSI", Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 21))), Sym("terapa")));
        c.put("TERAPASCAL:TORR", Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 11))), Sym("terapa")));
        c.put("TERAPASCAL:YOCTOPASCAL", Mul(Rat(Int(1), Pow(10, 36)), Sym("terapa")));
        c.put("TERAPASCAL:YOTTAPASCAL", Mul(Pow(10, 12), Sym("terapa")));
        c.put("TERAPASCAL:ZEPTOPASCAL", Mul(Rat(Int(1), Pow(10, 33)), Sym("terapa")));
        c.put("TERAPASCAL:ZETTAPASCAL", Mul(Pow(10, 9), Sym("terapa")));
        c.put("TORR:ATHMOSPHERE", Mul(Int(760), Sym("torr")));
        c.put("TORR:ATTOPASCAL", Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 15))), Sym("torr")));
        c.put("TORR:BAR", Mul(Rat(Mul(Int(304), Pow(10, 4)), Int(4053)), Sym("torr")));
        c.put("TORR:CENTIBAR", Mul(Rat(Int(30400), Int(4053)), Sym("torr")));
        c.put("TORR:CENTIPASCAL", Mul(Rat(Int(38), Int(506625)), Sym("torr")));
        c.put("TORR:DECAPASCAL", Mul(Rat(Int(304), Int(4053)), Sym("torr")));
        c.put("TORR:DECIBAR", Mul(Rat(Int(304000), Int(4053)), Sym("torr")));
        c.put("TORR:DECIPASCAL", Mul(Rat(Int(76), Int(101325)), Sym("torr")));
        c.put("TORR:EXAPASCAL", Mul(Rat(Mul(Int(304), Pow(10, 17)), Int(4053)), Sym("torr")));
        c.put("TORR:FEMTOPASCAL", Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 12))), Sym("torr")));
        c.put("TORR:GIGAPASCAL", Mul(Rat(Mul(Int(304), Pow(10, 8)), Int(4053)), Sym("torr")));
        c.put("TORR:HECTOPASCAL", Mul(Rat(Int(3040), Int(4053)), Sym("torr")));
        c.put("TORR:KILOBAR", Mul(Rat(Mul(Int(304), Pow(10, 7)), Int(4053)), Sym("torr")));
        c.put("TORR:KILOPASCAL", Mul(Rat(Int(30400), Int(4053)), Sym("torr")));
        c.put("TORR:MEGABAR", Mul(Rat(Mul(Int(304), Pow(10, 10)), Int(4053)), Sym("torr")));
        c.put("TORR:MEGAPASCAL", Mul(Rat(Mul(Int(304), Pow(10, 5)), Int(4053)), Sym("torr")));
        c.put("TORR:MICROPASCAL", Mul(Rat(Int(19), Int("2533125000")), Sym("torr")));
        c.put("TORR:MILLIBAR", Mul(Rat(Int(3040), Int(4053)), Sym("torr")));
        c.put("TORR:MILLIPASCAL", Mul(Rat(Int(19), Int(2533125)), Sym("torr")));
        c.put("TORR:MILLITORR", Mul(Rat(Int(1), Int(1000)), Sym("torr")));
        c.put("TORR:MMHG", Mul(Rat(Int("24125003437"), Mul(Int(24125), Pow(10, 6))), Sym("torr")));
        c.put("TORR:NANOPASCAL", Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 6))), Sym("torr")));
        c.put("TORR:PASCAL", Mul(Rat(Int(152), Int(20265)), Sym("torr")));
        c.put("TORR:PETAPASCAL", Mul(Rat(Mul(Int(304), Pow(10, 14)), Int(4053)), Sym("torr")));
        c.put("TORR:PICOPASCAL", Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 9))), Sym("torr")));
        c.put("TORR:PSI", Mul(Rat(Int("155952843535951"), Mul(Int(3015625), Pow(10, 6))), Sym("torr")));
        c.put("TORR:TERAPASCAL", Mul(Rat(Mul(Int(304), Pow(10, 11)), Int(4053)), Sym("torr")));
        c.put("TORR:YOCTOPASCAL", Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 21))), Sym("torr")));
        c.put("TORR:YOTTAPASCAL", Mul(Rat(Mul(Int(304), Pow(10, 23)), Int(4053)), Sym("torr")));
        c.put("TORR:ZEPTOPASCAL", Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 18))), Sym("torr")));
        c.put("TORR:ZETTAPASCAL", Mul(Rat(Mul(Int(304), Pow(10, 20)), Int(4053)), Sym("torr")));
        c.put("YOCTOPASCAL:ATHMOSPHERE", Mul(Mul(Int(101325), Pow(10, 24)), Sym("yoctopa")));
        c.put("YOCTOPASCAL:ATTOPASCAL", Mul(Pow(10, 6), Sym("yoctopa")));
        c.put("YOCTOPASCAL:BAR", Mul(Pow(10, 29), Sym("yoctopa")));
        c.put("YOCTOPASCAL:CENTIBAR", Mul(Pow(10, 27), Sym("yoctopa")));
        c.put("YOCTOPASCAL:CENTIPASCAL", Mul(Pow(10, 22), Sym("yoctopa")));
        c.put("YOCTOPASCAL:DECAPASCAL", Mul(Pow(10, 25), Sym("yoctopa")));
        c.put("YOCTOPASCAL:DECIBAR", Mul(Pow(10, 28), Sym("yoctopa")));
        c.put("YOCTOPASCAL:DECIPASCAL", Mul(Pow(10, 23), Sym("yoctopa")));
        c.put("YOCTOPASCAL:EXAPASCAL", Mul(Pow(10, 42), Sym("yoctopa")));
        c.put("YOCTOPASCAL:FEMTOPASCAL", Mul(Pow(10, 9), Sym("yoctopa")));
        c.put("YOCTOPASCAL:GIGAPASCAL", Mul(Pow(10, 33), Sym("yoctopa")));
        c.put("YOCTOPASCAL:HECTOPASCAL", Mul(Pow(10, 26), Sym("yoctopa")));
        c.put("YOCTOPASCAL:KILOBAR", Mul(Pow(10, 32), Sym("yoctopa")));
        c.put("YOCTOPASCAL:KILOPASCAL", Mul(Pow(10, 27), Sym("yoctopa")));
        c.put("YOCTOPASCAL:MEGABAR", Mul(Pow(10, 35), Sym("yoctopa")));
        c.put("YOCTOPASCAL:MEGAPASCAL", Mul(Pow(10, 30), Sym("yoctopa")));
        c.put("YOCTOPASCAL:MICROPASCAL", Mul(Pow(10, 18), Sym("yoctopa")));
        c.put("YOCTOPASCAL:MILLIBAR", Mul(Pow(10, 26), Sym("yoctopa")));
        c.put("YOCTOPASCAL:MILLIPASCAL", Mul(Pow(10, 21), Sym("yoctopa")));
        c.put("YOCTOPASCAL:MILLITORR", Mul(Rat(Mul(Int(2533125), Pow(10, 18)), Int(19)), Sym("yoctopa")));
        c.put("YOCTOPASCAL:MMHG", Mul(Mul(Int("133322387415"), Pow(10, 15)), Sym("yoctopa")));
        c.put("YOCTOPASCAL:NANOPASCAL", Mul(Pow(10, 15), Sym("yoctopa")));
        c.put("YOCTOPASCAL:PASCAL", Mul(Pow(10, 24), Sym("yoctopa")));
        c.put("YOCTOPASCAL:PETAPASCAL", Mul(Pow(10, 39), Sym("yoctopa")));
        c.put("YOCTOPASCAL:PICOPASCAL", Mul(Pow(10, 12), Sym("yoctopa")));
        c.put("YOCTOPASCAL:PSI", Mul(Mul(Int("689475729316836"), Pow(10, 13)), Sym("yoctopa")));
        c.put("YOCTOPASCAL:TERAPASCAL", Mul(Pow(10, 36), Sym("yoctopa")));
        c.put("YOCTOPASCAL:TORR", Mul(Rat(Mul(Int(2533125), Pow(10, 21)), Int(19)), Sym("yoctopa")));
        c.put("YOCTOPASCAL:YOTTAPASCAL", Mul(Pow(10, 48), Sym("yoctopa")));
        c.put("YOCTOPASCAL:ZEPTOPASCAL", Mul(Int(1000), Sym("yoctopa")));
        c.put("YOCTOPASCAL:ZETTAPASCAL", Mul(Pow(10, 45), Sym("yoctopa")));
        c.put("YOTTAPASCAL:ATHMOSPHERE", Mul(Rat(Int(4053), Mul(Int(4), Pow(10, 22))), Sym("yottapa")));
        c.put("YOTTAPASCAL:ATTOPASCAL", Mul(Rat(Int(1), Pow(10, 42)), Sym("yottapa")));
        c.put("YOTTAPASCAL:BAR", Mul(Rat(Int(1), Pow(10, 19)), Sym("yottapa")));
        c.put("YOTTAPASCAL:CENTIBAR", Mul(Rat(Int(1), Pow(10, 21)), Sym("yottapa")));
        c.put("YOTTAPASCAL:CENTIPASCAL", Mul(Rat(Int(1), Pow(10, 26)), Sym("yottapa")));
        c.put("YOTTAPASCAL:DECAPASCAL", Mul(Rat(Int(1), Pow(10, 23)), Sym("yottapa")));
        c.put("YOTTAPASCAL:DECIBAR", Mul(Rat(Int(1), Pow(10, 20)), Sym("yottapa")));
        c.put("YOTTAPASCAL:DECIPASCAL", Mul(Rat(Int(1), Pow(10, 25)), Sym("yottapa")));
        c.put("YOTTAPASCAL:EXAPASCAL", Mul(Rat(Int(1), Pow(10, 6)), Sym("yottapa")));
        c.put("YOTTAPASCAL:FEMTOPASCAL", Mul(Rat(Int(1), Pow(10, 39)), Sym("yottapa")));
        c.put("YOTTAPASCAL:GIGAPASCAL", Mul(Rat(Int(1), Pow(10, 15)), Sym("yottapa")));
        c.put("YOTTAPASCAL:HECTOPASCAL", Mul(Rat(Int(1), Pow(10, 22)), Sym("yottapa")));
        c.put("YOTTAPASCAL:KILOBAR", Mul(Rat(Int(1), Pow(10, 16)), Sym("yottapa")));
        c.put("YOTTAPASCAL:KILOPASCAL", Mul(Rat(Int(1), Pow(10, 21)), Sym("yottapa")));
        c.put("YOTTAPASCAL:MEGABAR", Mul(Rat(Int(1), Pow(10, 13)), Sym("yottapa")));
        c.put("YOTTAPASCAL:MEGAPASCAL", Mul(Rat(Int(1), Pow(10, 18)), Sym("yottapa")));
        c.put("YOTTAPASCAL:MICROPASCAL", Mul(Rat(Int(1), Pow(10, 30)), Sym("yottapa")));
        c.put("YOTTAPASCAL:MILLIBAR", Mul(Rat(Int(1), Pow(10, 22)), Sym("yottapa")));
        c.put("YOTTAPASCAL:MILLIPASCAL", Mul(Rat(Int(1), Pow(10, 27)), Sym("yottapa")));
        c.put("YOTTAPASCAL:MILLITORR", Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 26))), Sym("yottapa")));
        c.put("YOTTAPASCAL:MMHG", Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 32))), Sym("yottapa")));
        c.put("YOTTAPASCAL:NANOPASCAL", Mul(Rat(Int(1), Pow(10, 33)), Sym("yottapa")));
        c.put("YOTTAPASCAL:PASCAL", Mul(Rat(Int(1), Pow(10, 24)), Sym("yottapa")));
        c.put("YOTTAPASCAL:PETAPASCAL", Mul(Rat(Int(1), Pow(10, 9)), Sym("yottapa")));
        c.put("YOTTAPASCAL:PICOPASCAL", Mul(Rat(Int(1), Pow(10, 36)), Sym("yottapa")));
        c.put("YOTTAPASCAL:PSI", Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 33))), Sym("yottapa")));
        c.put("YOTTAPASCAL:TERAPASCAL", Mul(Rat(Int(1), Pow(10, 12)), Sym("yottapa")));
        c.put("YOTTAPASCAL:TORR", Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 23))), Sym("yottapa")));
        c.put("YOTTAPASCAL:YOCTOPASCAL", Mul(Rat(Int(1), Pow(10, 48)), Sym("yottapa")));
        c.put("YOTTAPASCAL:ZEPTOPASCAL", Mul(Rat(Int(1), Pow(10, 45)), Sym("yottapa")));
        c.put("YOTTAPASCAL:ZETTAPASCAL", Mul(Rat(Int(1), Int(1000)), Sym("yottapa")));
        c.put("ZEPTOPASCAL:ATHMOSPHERE", Mul(Mul(Int(101325), Pow(10, 21)), Sym("zeptopa")));
        c.put("ZEPTOPASCAL:ATTOPASCAL", Mul(Int(1000), Sym("zeptopa")));
        c.put("ZEPTOPASCAL:BAR", Mul(Pow(10, 26), Sym("zeptopa")));
        c.put("ZEPTOPASCAL:CENTIBAR", Mul(Pow(10, 24), Sym("zeptopa")));
        c.put("ZEPTOPASCAL:CENTIPASCAL", Mul(Pow(10, 19), Sym("zeptopa")));
        c.put("ZEPTOPASCAL:DECAPASCAL", Mul(Pow(10, 22), Sym("zeptopa")));
        c.put("ZEPTOPASCAL:DECIBAR", Mul(Pow(10, 25), Sym("zeptopa")));
        c.put("ZEPTOPASCAL:DECIPASCAL", Mul(Pow(10, 20), Sym("zeptopa")));
        c.put("ZEPTOPASCAL:EXAPASCAL", Mul(Pow(10, 39), Sym("zeptopa")));
        c.put("ZEPTOPASCAL:FEMTOPASCAL", Mul(Pow(10, 6), Sym("zeptopa")));
        c.put("ZEPTOPASCAL:GIGAPASCAL", Mul(Pow(10, 30), Sym("zeptopa")));
        c.put("ZEPTOPASCAL:HECTOPASCAL", Mul(Pow(10, 23), Sym("zeptopa")));
        c.put("ZEPTOPASCAL:KILOBAR", Mul(Pow(10, 29), Sym("zeptopa")));
        c.put("ZEPTOPASCAL:KILOPASCAL", Mul(Pow(10, 24), Sym("zeptopa")));
        c.put("ZEPTOPASCAL:MEGABAR", Mul(Pow(10, 32), Sym("zeptopa")));
        c.put("ZEPTOPASCAL:MEGAPASCAL", Mul(Pow(10, 27), Sym("zeptopa")));
        c.put("ZEPTOPASCAL:MICROPASCAL", Mul(Pow(10, 15), Sym("zeptopa")));
        c.put("ZEPTOPASCAL:MILLIBAR", Mul(Pow(10, 23), Sym("zeptopa")));
        c.put("ZEPTOPASCAL:MILLIPASCAL", Mul(Pow(10, 18), Sym("zeptopa")));
        c.put("ZEPTOPASCAL:MILLITORR", Mul(Rat(Mul(Int(2533125), Pow(10, 15)), Int(19)), Sym("zeptopa")));
        c.put("ZEPTOPASCAL:MMHG", Mul(Mul(Int("133322387415"), Pow(10, 12)), Sym("zeptopa")));
        c.put("ZEPTOPASCAL:NANOPASCAL", Mul(Pow(10, 12), Sym("zeptopa")));
        c.put("ZEPTOPASCAL:PASCAL", Mul(Pow(10, 21), Sym("zeptopa")));
        c.put("ZEPTOPASCAL:PETAPASCAL", Mul(Pow(10, 36), Sym("zeptopa")));
        c.put("ZEPTOPASCAL:PICOPASCAL", Mul(Pow(10, 9), Sym("zeptopa")));
        c.put("ZEPTOPASCAL:PSI", Mul(Mul(Int("689475729316836"), Pow(10, 10)), Sym("zeptopa")));
        c.put("ZEPTOPASCAL:TERAPASCAL", Mul(Pow(10, 33), Sym("zeptopa")));
        c.put("ZEPTOPASCAL:TORR", Mul(Rat(Mul(Int(2533125), Pow(10, 18)), Int(19)), Sym("zeptopa")));
        c.put("ZEPTOPASCAL:YOCTOPASCAL", Mul(Rat(Int(1), Int(1000)), Sym("zeptopa")));
        c.put("ZEPTOPASCAL:YOTTAPASCAL", Mul(Pow(10, 45), Sym("zeptopa")));
        c.put("ZEPTOPASCAL:ZETTAPASCAL", Mul(Pow(10, 42), Sym("zeptopa")));
        c.put("ZETTAPASCAL:ATHMOSPHERE", Mul(Rat(Int(4053), Mul(Int(4), Pow(10, 19))), Sym("zettapa")));
        c.put("ZETTAPASCAL:ATTOPASCAL", Mul(Rat(Int(1), Pow(10, 39)), Sym("zettapa")));
        c.put("ZETTAPASCAL:BAR", Mul(Rat(Int(1), Pow(10, 16)), Sym("zettapa")));
        c.put("ZETTAPASCAL:CENTIBAR", Mul(Rat(Int(1), Pow(10, 18)), Sym("zettapa")));
        c.put("ZETTAPASCAL:CENTIPASCAL", Mul(Rat(Int(1), Pow(10, 23)), Sym("zettapa")));
        c.put("ZETTAPASCAL:DECAPASCAL", Mul(Rat(Int(1), Pow(10, 20)), Sym("zettapa")));
        c.put("ZETTAPASCAL:DECIBAR", Mul(Rat(Int(1), Pow(10, 17)), Sym("zettapa")));
        c.put("ZETTAPASCAL:DECIPASCAL", Mul(Rat(Int(1), Pow(10, 22)), Sym("zettapa")));
        c.put("ZETTAPASCAL:EXAPASCAL", Mul(Rat(Int(1), Int(1000)), Sym("zettapa")));
        c.put("ZETTAPASCAL:FEMTOPASCAL", Mul(Rat(Int(1), Pow(10, 36)), Sym("zettapa")));
        c.put("ZETTAPASCAL:GIGAPASCAL", Mul(Rat(Int(1), Pow(10, 12)), Sym("zettapa")));
        c.put("ZETTAPASCAL:HECTOPASCAL", Mul(Rat(Int(1), Pow(10, 19)), Sym("zettapa")));
        c.put("ZETTAPASCAL:KILOBAR", Mul(Rat(Int(1), Pow(10, 13)), Sym("zettapa")));
        c.put("ZETTAPASCAL:KILOPASCAL", Mul(Rat(Int(1), Pow(10, 18)), Sym("zettapa")));
        c.put("ZETTAPASCAL:MEGABAR", Mul(Rat(Int(1), Pow(10, 10)), Sym("zettapa")));
        c.put("ZETTAPASCAL:MEGAPASCAL", Mul(Rat(Int(1), Pow(10, 15)), Sym("zettapa")));
        c.put("ZETTAPASCAL:MICROPASCAL", Mul(Rat(Int(1), Pow(10, 27)), Sym("zettapa")));
        c.put("ZETTAPASCAL:MILLIBAR", Mul(Rat(Int(1), Pow(10, 19)), Sym("zettapa")));
        c.put("ZETTAPASCAL:MILLIPASCAL", Mul(Rat(Int(1), Pow(10, 24)), Sym("zettapa")));
        c.put("ZETTAPASCAL:MILLITORR", Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 23))), Sym("zettapa")));
        c.put("ZETTAPASCAL:MMHG", Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 29))), Sym("zettapa")));
        c.put("ZETTAPASCAL:NANOPASCAL", Mul(Rat(Int(1), Pow(10, 30)), Sym("zettapa")));
        c.put("ZETTAPASCAL:PASCAL", Mul(Rat(Int(1), Pow(10, 21)), Sym("zettapa")));
        c.put("ZETTAPASCAL:PETAPASCAL", Mul(Rat(Int(1), Pow(10, 6)), Sym("zettapa")));
        c.put("ZETTAPASCAL:PICOPASCAL", Mul(Rat(Int(1), Pow(10, 33)), Sym("zettapa")));
        c.put("ZETTAPASCAL:PSI", Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 30))), Sym("zettapa")));
        c.put("ZETTAPASCAL:TERAPASCAL", Mul(Rat(Int(1), Pow(10, 9)), Sym("zettapa")));
        c.put("ZETTAPASCAL:TORR", Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 20))), Sym("zettapa")));
        c.put("ZETTAPASCAL:YOCTOPASCAL", Mul(Rat(Int(1), Pow(10, 45)), Sym("zettapa")));
        c.put("ZETTAPASCAL:YOTTAPASCAL", Mul(Int(1000), Sym("zettapa")));
        c.put("ZETTAPASCAL:ZEPTOPASCAL", Mul(Rat(Int(1), Pow(10, 42)), Sym("zettapa")));
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
            setUnit(UnitsPressure.valueOf(target));
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

