/*
 * Copyright (C) 2014 University of Dundee & Open Microscopy Environment
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

#include <omero/model/PressureI.h>
#include <omero/ClientErrors.h>

::Ice::Object* IceInternal::upCast(::omero::model::PressureI* t) { return t; }

using namespace omero::conversions;

typedef omero::model::enums::UnitsPressure UnitsPressure;

namespace omero {

    namespace model {

        static std::map<UnitsPressure, ConversionPtr> createMapATMOSPHERE() {
            std::map<UnitsPressure, ConversionPtr> c;
            c[enums::ATTOPASCAL] = Mul(Mul(Int(101325), Pow(10, 18)), Sym("atm"));
            c[enums::BAR] = Mul(Rat(Int(4053), Int(4000)), Sym("atm"));
            c[enums::CENTIBAR] = Mul(Rat(Int(4053), Int(40)), Sym("atm"));
            c[enums::CENTIPASCAL] = Mul(Int(10132500), Sym("atm"));
            c[enums::DECAPASCAL] = Mul(Rat(Int(20265), Int(2)), Sym("atm"));
            c[enums::DECIBAR] = Mul(Rat(Int(4053), Int(400)), Sym("atm"));
            c[enums::DECIPASCAL] = Mul(Int(1013250), Sym("atm"));
            c[enums::EXAPASCAL] = Mul(Rat(Int(4053), Mul(Int(4), Pow(10, 16))), Sym("atm"));
            c[enums::FEMTOPASCAL] = Mul(Mul(Int(101325), Pow(10, 15)), Sym("atm"));
            c[enums::GIGAPASCAL] = Mul(Rat(Int(4053), Mul(Int(4), Pow(10, 7))), Sym("atm"));
            c[enums::HECTOPASCAL] = Mul(Rat(Int(4053), Int(4)), Sym("atm"));
            c[enums::KILOBAR] = Mul(Rat(Int(4053), Mul(Int(4), Pow(10, 6))), Sym("atm"));
            c[enums::KILOPASCAL] = Mul(Rat(Int(4053), Int(40)), Sym("atm"));
            c[enums::MEGABAR] = Mul(Rat(Int(4053), Mul(Int(4), Pow(10, 9))), Sym("atm"));
            c[enums::MEGAPASCAL] = Mul(Rat(Int(4053), Mul(Int(4), Pow(10, 4))), Sym("atm"));
            c[enums::MICROPASCAL] = Mul(Mul(Int(101325), Pow(10, 6)), Sym("atm"));
            c[enums::MILLIBAR] = Mul(Rat(Int(4053), Int(4)), Sym("atm"));
            c[enums::MILLIPASCAL] = Mul(Int(101325000), Sym("atm"));
            c[enums::MILLITORR] = Mul(Rat(Int(19), Int(25)), Sym("atm"));
            c[enums::MMHG] = Mul(Rat(Mul(Int(965), Pow(10, 9)), Int(1269737023)), Sym("atm"));
            c[enums::NANOPASCAL] = Mul(Mul(Int(101325), Pow(10, 9)), Sym("atm"));
            c[enums::PETAPASCAL] = Mul(Rat(Int(4053), Mul(Int(4), Pow(10, 13))), Sym("atm"));
            c[enums::PICOPASCAL] = Mul(Mul(Int(101325), Pow(10, 12)), Sym("atm"));
            c[enums::PSI] = Mul(Rat(Mul(Int(120625), Pow(10, 9)), Int("8208044396629")), Sym("atm"));
            c[enums::Pascal] = Mul(Int(101325), Sym("atm"));
            c[enums::TERAPASCAL] = Mul(Rat(Int(4053), Mul(Int(4), Pow(10, 10))), Sym("atm"));
            c[enums::TORR] = Mul(Int(760), Sym("atm"));
            c[enums::YOCTOPASCAL] = Mul(Mul(Int(101325), Pow(10, 24)), Sym("atm"));
            c[enums::YOTTAPASCAL] = Mul(Rat(Int(4053), Mul(Int(4), Pow(10, 22))), Sym("atm"));
            c[enums::ZEPTOPASCAL] = Mul(Mul(Int(101325), Pow(10, 21)), Sym("atm"));
            c[enums::ZETTAPASCAL] = Mul(Rat(Int(4053), Mul(Int(4), Pow(10, 19))), Sym("atm"));
            return c;
        }

        static std::map<UnitsPressure, ConversionPtr> createMapATTOPASCAL() {
            std::map<UnitsPressure, ConversionPtr> c;
            c[enums::ATMOSPHERE] = Mul(Rat(Int(1), Mul(Int(101325), Pow(10, 18))), Sym("attopa"));
            c[enums::BAR] = Mul(Rat(Int(1), Pow(10, 23)), Sym("attopa"));
            c[enums::CENTIBAR] = Mul(Rat(Int(1), Pow(10, 21)), Sym("attopa"));
            c[enums::CENTIPASCAL] = Mul(Rat(Int(1), Pow(10, 16)), Sym("attopa"));
            c[enums::DECAPASCAL] = Mul(Rat(Int(1), Pow(10, 19)), Sym("attopa"));
            c[enums::DECIBAR] = Mul(Rat(Int(1), Pow(10, 22)), Sym("attopa"));
            c[enums::DECIPASCAL] = Mul(Rat(Int(1), Pow(10, 17)), Sym("attopa"));
            c[enums::EXAPASCAL] = Mul(Rat(Int(1), Pow(10, 36)), Sym("attopa"));
            c[enums::FEMTOPASCAL] = Mul(Rat(Int(1), Int(1000)), Sym("attopa"));
            c[enums::GIGAPASCAL] = Mul(Rat(Int(1), Pow(10, 27)), Sym("attopa"));
            c[enums::HECTOPASCAL] = Mul(Rat(Int(1), Pow(10, 20)), Sym("attopa"));
            c[enums::KILOBAR] = Mul(Rat(Int(1), Pow(10, 26)), Sym("attopa"));
            c[enums::KILOPASCAL] = Mul(Rat(Int(1), Pow(10, 21)), Sym("attopa"));
            c[enums::MEGABAR] = Mul(Rat(Int(1), Pow(10, 29)), Sym("attopa"));
            c[enums::MEGAPASCAL] = Mul(Rat(Int(1), Pow(10, 24)), Sym("attopa"));
            c[enums::MICROPASCAL] = Mul(Rat(Int(1), Pow(10, 12)), Sym("attopa"));
            c[enums::MILLIBAR] = Mul(Rat(Int(1), Pow(10, 20)), Sym("attopa"));
            c[enums::MILLIPASCAL] = Mul(Rat(Int(1), Pow(10, 15)), Sym("attopa"));
            c[enums::MILLITORR] = Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 18))), Sym("attopa"));
            c[enums::MMHG] = Mul(Rat(Int(1), Mul(Int("133322387415"), Pow(10, 9))), Sym("attopa"));
            c[enums::NANOPASCAL] = Mul(Rat(Int(1), Pow(10, 9)), Sym("attopa"));
            c[enums::PETAPASCAL] = Mul(Rat(Int(1), Pow(10, 33)), Sym("attopa"));
            c[enums::PICOPASCAL] = Mul(Rat(Int(1), Pow(10, 6)), Sym("attopa"));
            c[enums::PSI] = Mul(Rat(Int(1), Mul(Int("689475729316836"), Pow(10, 7))), Sym("attopa"));
            c[enums::Pascal] = Mul(Rat(Int(1), Pow(10, 18)), Sym("attopa"));
            c[enums::TERAPASCAL] = Mul(Rat(Int(1), Pow(10, 30)), Sym("attopa"));
            c[enums::TORR] = Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 15))), Sym("attopa"));
            c[enums::YOCTOPASCAL] = Mul(Pow(10, 6), Sym("attopa"));
            c[enums::YOTTAPASCAL] = Mul(Rat(Int(1), Pow(10, 42)), Sym("attopa"));
            c[enums::ZEPTOPASCAL] = Mul(Int(1000), Sym("attopa"));
            c[enums::ZETTAPASCAL] = Mul(Rat(Int(1), Pow(10, 39)), Sym("attopa"));
            return c;
        }

        static std::map<UnitsPressure, ConversionPtr> createMapBAR() {
            std::map<UnitsPressure, ConversionPtr> c;
            c[enums::ATMOSPHERE] = Mul(Rat(Int(4000), Int(4053)), Sym("bar"));
            c[enums::ATTOPASCAL] = Mul(Pow(10, 23), Sym("bar"));
            c[enums::CENTIBAR] = Mul(Int(100), Sym("bar"));
            c[enums::CENTIPASCAL] = Mul(Pow(10, 7), Sym("bar"));
            c[enums::DECAPASCAL] = Mul(Pow(10, 4), Sym("bar"));
            c[enums::DECIBAR] = Mul(Int(10), Sym("bar"));
            c[enums::DECIPASCAL] = Mul(Pow(10, 6), Sym("bar"));
            c[enums::EXAPASCAL] = Mul(Rat(Int(1), Pow(10, 13)), Sym("bar"));
            c[enums::FEMTOPASCAL] = Mul(Pow(10, 20), Sym("bar"));
            c[enums::GIGAPASCAL] = Mul(Rat(Int(1), Pow(10, 4)), Sym("bar"));
            c[enums::HECTOPASCAL] = Mul(Int(1000), Sym("bar"));
            c[enums::KILOBAR] = Mul(Rat(Int(1), Int(1000)), Sym("bar"));
            c[enums::KILOPASCAL] = Mul(Int(100), Sym("bar"));
            c[enums::MEGABAR] = Mul(Rat(Int(1), Pow(10, 6)), Sym("bar"));
            c[enums::MEGAPASCAL] = Mul(Rat(Int(1), Int(10)), Sym("bar"));
            c[enums::MICROPASCAL] = Mul(Pow(10, 11), Sym("bar"));
            c[enums::MILLIBAR] = Mul(Int(1000), Sym("bar"));
            c[enums::MILLIPASCAL] = Mul(Pow(10, 8), Sym("bar"));
            c[enums::MILLITORR] = Mul(Rat(Int(3040), Int(4053)), Sym("bar"));
            c[enums::MMHG] = Mul(Rat(Mul(Int(2), Pow(10, 13)), Int("26664477483")), Sym("bar"));
            c[enums::NANOPASCAL] = Mul(Pow(10, 14), Sym("bar"));
            c[enums::PETAPASCAL] = Mul(Rat(Int(1), Pow(10, 10)), Sym("bar"));
            c[enums::PICOPASCAL] = Mul(Pow(10, 17), Sym("bar"));
            c[enums::PSI] = Mul(Rat(Mul(Int(25), Pow(10, 14)), Int("172368932329209")), Sym("bar"));
            c[enums::Pascal] = Mul(Pow(10, 5), Sym("bar"));
            c[enums::TERAPASCAL] = Mul(Rat(Int(1), Pow(10, 7)), Sym("bar"));
            c[enums::TORR] = Mul(Rat(Mul(Int(304), Pow(10, 4)), Int(4053)), Sym("bar"));
            c[enums::YOCTOPASCAL] = Mul(Pow(10, 29), Sym("bar"));
            c[enums::YOTTAPASCAL] = Mul(Rat(Int(1), Pow(10, 19)), Sym("bar"));
            c[enums::ZEPTOPASCAL] = Mul(Pow(10, 26), Sym("bar"));
            c[enums::ZETTAPASCAL] = Mul(Rat(Int(1), Pow(10, 16)), Sym("bar"));
            return c;
        }

        static std::map<UnitsPressure, ConversionPtr> createMapCENTIBAR() {
            std::map<UnitsPressure, ConversionPtr> c;
            c[enums::ATMOSPHERE] = Mul(Rat(Int(40), Int(4053)), Sym("cbar"));
            c[enums::ATTOPASCAL] = Mul(Pow(10, 21), Sym("cbar"));
            c[enums::BAR] = Mul(Rat(Int(1), Int(100)), Sym("cbar"));
            c[enums::CENTIPASCAL] = Mul(Pow(10, 5), Sym("cbar"));
            c[enums::DECAPASCAL] = Mul(Int(100), Sym("cbar"));
            c[enums::DECIBAR] = Mul(Rat(Int(1), Int(10)), Sym("cbar"));
            c[enums::DECIPASCAL] = Mul(Pow(10, 4), Sym("cbar"));
            c[enums::EXAPASCAL] = Mul(Rat(Int(1), Pow(10, 15)), Sym("cbar"));
            c[enums::FEMTOPASCAL] = Mul(Pow(10, 18), Sym("cbar"));
            c[enums::GIGAPASCAL] = Mul(Rat(Int(1), Pow(10, 6)), Sym("cbar"));
            c[enums::HECTOPASCAL] = Mul(Int(10), Sym("cbar"));
            c[enums::KILOBAR] = Mul(Rat(Int(1), Pow(10, 5)), Sym("cbar"));
            c[enums::KILOPASCAL] = Sym("cbar");
            c[enums::MEGABAR] = Mul(Rat(Int(1), Pow(10, 8)), Sym("cbar"));
            c[enums::MEGAPASCAL] = Mul(Rat(Int(1), Int(1000)), Sym("cbar"));
            c[enums::MICROPASCAL] = Mul(Pow(10, 9), Sym("cbar"));
            c[enums::MILLIBAR] = Mul(Int(10), Sym("cbar"));
            c[enums::MILLIPASCAL] = Mul(Pow(10, 6), Sym("cbar"));
            c[enums::MILLITORR] = Mul(Rat(Int(152), Int(20265)), Sym("cbar"));
            c[enums::MMHG] = Mul(Rat(Mul(Int(2), Pow(10, 11)), Int("26664477483")), Sym("cbar"));
            c[enums::NANOPASCAL] = Mul(Pow(10, 12), Sym("cbar"));
            c[enums::PETAPASCAL] = Mul(Rat(Int(1), Pow(10, 12)), Sym("cbar"));
            c[enums::PICOPASCAL] = Mul(Pow(10, 15), Sym("cbar"));
            c[enums::PSI] = Mul(Rat(Mul(Int(25), Pow(10, 12)), Int("172368932329209")), Sym("cbar"));
            c[enums::Pascal] = Mul(Int(1000), Sym("cbar"));
            c[enums::TERAPASCAL] = Mul(Rat(Int(1), Pow(10, 9)), Sym("cbar"));
            c[enums::TORR] = Mul(Rat(Int(30400), Int(4053)), Sym("cbar"));
            c[enums::YOCTOPASCAL] = Mul(Pow(10, 27), Sym("cbar"));
            c[enums::YOTTAPASCAL] = Mul(Rat(Int(1), Pow(10, 21)), Sym("cbar"));
            c[enums::ZEPTOPASCAL] = Mul(Pow(10, 24), Sym("cbar"));
            c[enums::ZETTAPASCAL] = Mul(Rat(Int(1), Pow(10, 18)), Sym("cbar"));
            return c;
        }

        static std::map<UnitsPressure, ConversionPtr> createMapCENTIPASCAL() {
            std::map<UnitsPressure, ConversionPtr> c;
            c[enums::ATMOSPHERE] = Mul(Rat(Int(1), Int(10132500)), Sym("centipa"));
            c[enums::ATTOPASCAL] = Mul(Pow(10, 16), Sym("centipa"));
            c[enums::BAR] = Mul(Rat(Int(1), Pow(10, 7)), Sym("centipa"));
            c[enums::CENTIBAR] = Mul(Rat(Int(1), Pow(10, 5)), Sym("centipa"));
            c[enums::DECAPASCAL] = Mul(Rat(Int(1), Int(1000)), Sym("centipa"));
            c[enums::DECIBAR] = Mul(Rat(Int(1), Pow(10, 6)), Sym("centipa"));
            c[enums::DECIPASCAL] = Mul(Rat(Int(1), Int(10)), Sym("centipa"));
            c[enums::EXAPASCAL] = Mul(Rat(Int(1), Pow(10, 20)), Sym("centipa"));
            c[enums::FEMTOPASCAL] = Mul(Pow(10, 13), Sym("centipa"));
            c[enums::GIGAPASCAL] = Mul(Rat(Int(1), Pow(10, 11)), Sym("centipa"));
            c[enums::HECTOPASCAL] = Mul(Rat(Int(1), Pow(10, 4)), Sym("centipa"));
            c[enums::KILOBAR] = Mul(Rat(Int(1), Pow(10, 10)), Sym("centipa"));
            c[enums::KILOPASCAL] = Mul(Rat(Int(1), Pow(10, 5)), Sym("centipa"));
            c[enums::MEGABAR] = Mul(Rat(Int(1), Pow(10, 13)), Sym("centipa"));
            c[enums::MEGAPASCAL] = Mul(Rat(Int(1), Pow(10, 8)), Sym("centipa"));
            c[enums::MICROPASCAL] = Mul(Pow(10, 4), Sym("centipa"));
            c[enums::MILLIBAR] = Mul(Rat(Int(1), Pow(10, 4)), Sym("centipa"));
            c[enums::MILLIPASCAL] = Mul(Int(10), Sym("centipa"));
            c[enums::MILLITORR] = Mul(Rat(Int(19), Int(253312500)), Sym("centipa"));
            c[enums::MMHG] = Mul(Rat(Mul(Int(2), Pow(10, 6)), Int("26664477483")), Sym("centipa"));
            c[enums::NANOPASCAL] = Mul(Pow(10, 7), Sym("centipa"));
            c[enums::PETAPASCAL] = Mul(Rat(Int(1), Pow(10, 17)), Sym("centipa"));
            c[enums::PICOPASCAL] = Mul(Pow(10, 10), Sym("centipa"));
            c[enums::PSI] = Mul(Rat(Mul(Int(25), Pow(10, 7)), Int("172368932329209")), Sym("centipa"));
            c[enums::Pascal] = Mul(Rat(Int(1), Int(100)), Sym("centipa"));
            c[enums::TERAPASCAL] = Mul(Rat(Int(1), Pow(10, 14)), Sym("centipa"));
            c[enums::TORR] = Mul(Rat(Int(38), Int(506625)), Sym("centipa"));
            c[enums::YOCTOPASCAL] = Mul(Pow(10, 22), Sym("centipa"));
            c[enums::YOTTAPASCAL] = Mul(Rat(Int(1), Pow(10, 26)), Sym("centipa"));
            c[enums::ZEPTOPASCAL] = Mul(Pow(10, 19), Sym("centipa"));
            c[enums::ZETTAPASCAL] = Mul(Rat(Int(1), Pow(10, 23)), Sym("centipa"));
            return c;
        }

        static std::map<UnitsPressure, ConversionPtr> createMapDECAPASCAL() {
            std::map<UnitsPressure, ConversionPtr> c;
            c[enums::ATMOSPHERE] = Mul(Rat(Int(2), Int(20265)), Sym("decapa"));
            c[enums::ATTOPASCAL] = Mul(Pow(10, 19), Sym("decapa"));
            c[enums::BAR] = Mul(Rat(Int(1), Pow(10, 4)), Sym("decapa"));
            c[enums::CENTIBAR] = Mul(Rat(Int(1), Int(100)), Sym("decapa"));
            c[enums::CENTIPASCAL] = Mul(Int(1000), Sym("decapa"));
            c[enums::DECIBAR] = Mul(Rat(Int(1), Int(1000)), Sym("decapa"));
            c[enums::DECIPASCAL] = Mul(Int(100), Sym("decapa"));
            c[enums::EXAPASCAL] = Mul(Rat(Int(1), Pow(10, 17)), Sym("decapa"));
            c[enums::FEMTOPASCAL] = Mul(Pow(10, 16), Sym("decapa"));
            c[enums::GIGAPASCAL] = Mul(Rat(Int(1), Pow(10, 8)), Sym("decapa"));
            c[enums::HECTOPASCAL] = Mul(Rat(Int(1), Int(10)), Sym("decapa"));
            c[enums::KILOBAR] = Mul(Rat(Int(1), Pow(10, 7)), Sym("decapa"));
            c[enums::KILOPASCAL] = Mul(Rat(Int(1), Int(100)), Sym("decapa"));
            c[enums::MEGABAR] = Mul(Rat(Int(1), Pow(10, 10)), Sym("decapa"));
            c[enums::MEGAPASCAL] = Mul(Rat(Int(1), Pow(10, 5)), Sym("decapa"));
            c[enums::MICROPASCAL] = Mul(Pow(10, 7), Sym("decapa"));
            c[enums::MILLIBAR] = Mul(Rat(Int(1), Int(10)), Sym("decapa"));
            c[enums::MILLIPASCAL] = Mul(Pow(10, 4), Sym("decapa"));
            c[enums::MILLITORR] = Mul(Rat(Int(38), Int(506625)), Sym("decapa"));
            c[enums::MMHG] = Mul(Rat(Mul(Int(2), Pow(10, 9)), Int("26664477483")), Sym("decapa"));
            c[enums::NANOPASCAL] = Mul(Pow(10, 10), Sym("decapa"));
            c[enums::PETAPASCAL] = Mul(Rat(Int(1), Pow(10, 14)), Sym("decapa"));
            c[enums::PICOPASCAL] = Mul(Pow(10, 13), Sym("decapa"));
            c[enums::PSI] = Mul(Rat(Mul(Int(25), Pow(10, 10)), Int("172368932329209")), Sym("decapa"));
            c[enums::Pascal] = Mul(Int(10), Sym("decapa"));
            c[enums::TERAPASCAL] = Mul(Rat(Int(1), Pow(10, 11)), Sym("decapa"));
            c[enums::TORR] = Mul(Rat(Int(304), Int(4053)), Sym("decapa"));
            c[enums::YOCTOPASCAL] = Mul(Pow(10, 25), Sym("decapa"));
            c[enums::YOTTAPASCAL] = Mul(Rat(Int(1), Pow(10, 23)), Sym("decapa"));
            c[enums::ZEPTOPASCAL] = Mul(Pow(10, 22), Sym("decapa"));
            c[enums::ZETTAPASCAL] = Mul(Rat(Int(1), Pow(10, 20)), Sym("decapa"));
            return c;
        }

        static std::map<UnitsPressure, ConversionPtr> createMapDECIBAR() {
            std::map<UnitsPressure, ConversionPtr> c;
            c[enums::ATMOSPHERE] = Mul(Rat(Int(400), Int(4053)), Sym("dbar"));
            c[enums::ATTOPASCAL] = Mul(Pow(10, 22), Sym("dbar"));
            c[enums::BAR] = Mul(Rat(Int(1), Int(10)), Sym("dbar"));
            c[enums::CENTIBAR] = Mul(Int(10), Sym("dbar"));
            c[enums::CENTIPASCAL] = Mul(Pow(10, 6), Sym("dbar"));
            c[enums::DECAPASCAL] = Mul(Int(1000), Sym("dbar"));
            c[enums::DECIPASCAL] = Mul(Pow(10, 5), Sym("dbar"));
            c[enums::EXAPASCAL] = Mul(Rat(Int(1), Pow(10, 14)), Sym("dbar"));
            c[enums::FEMTOPASCAL] = Mul(Pow(10, 19), Sym("dbar"));
            c[enums::GIGAPASCAL] = Mul(Rat(Int(1), Pow(10, 5)), Sym("dbar"));
            c[enums::HECTOPASCAL] = Mul(Int(100), Sym("dbar"));
            c[enums::KILOBAR] = Mul(Rat(Int(1), Pow(10, 4)), Sym("dbar"));
            c[enums::KILOPASCAL] = Mul(Int(10), Sym("dbar"));
            c[enums::MEGABAR] = Mul(Rat(Int(1), Pow(10, 7)), Sym("dbar"));
            c[enums::MEGAPASCAL] = Mul(Rat(Int(1), Int(100)), Sym("dbar"));
            c[enums::MICROPASCAL] = Mul(Pow(10, 10), Sym("dbar"));
            c[enums::MILLIBAR] = Mul(Int(100), Sym("dbar"));
            c[enums::MILLIPASCAL] = Mul(Pow(10, 7), Sym("dbar"));
            c[enums::MILLITORR] = Mul(Rat(Int(304), Int(4053)), Sym("dbar"));
            c[enums::MMHG] = Mul(Rat(Mul(Int(2), Pow(10, 12)), Int("26664477483")), Sym("dbar"));
            c[enums::NANOPASCAL] = Mul(Pow(10, 13), Sym("dbar"));
            c[enums::PETAPASCAL] = Mul(Rat(Int(1), Pow(10, 11)), Sym("dbar"));
            c[enums::PICOPASCAL] = Mul(Pow(10, 16), Sym("dbar"));
            c[enums::PSI] = Mul(Rat(Mul(Int(25), Pow(10, 13)), Int("172368932329209")), Sym("dbar"));
            c[enums::Pascal] = Mul(Pow(10, 4), Sym("dbar"));
            c[enums::TERAPASCAL] = Mul(Rat(Int(1), Pow(10, 8)), Sym("dbar"));
            c[enums::TORR] = Mul(Rat(Int(304000), Int(4053)), Sym("dbar"));
            c[enums::YOCTOPASCAL] = Mul(Pow(10, 28), Sym("dbar"));
            c[enums::YOTTAPASCAL] = Mul(Rat(Int(1), Pow(10, 20)), Sym("dbar"));
            c[enums::ZEPTOPASCAL] = Mul(Pow(10, 25), Sym("dbar"));
            c[enums::ZETTAPASCAL] = Mul(Rat(Int(1), Pow(10, 17)), Sym("dbar"));
            return c;
        }

        static std::map<UnitsPressure, ConversionPtr> createMapDECIPASCAL() {
            std::map<UnitsPressure, ConversionPtr> c;
            c[enums::ATMOSPHERE] = Mul(Rat(Int(1), Int(1013250)), Sym("decipa"));
            c[enums::ATTOPASCAL] = Mul(Pow(10, 17), Sym("decipa"));
            c[enums::BAR] = Mul(Rat(Int(1), Pow(10, 6)), Sym("decipa"));
            c[enums::CENTIBAR] = Mul(Rat(Int(1), Pow(10, 4)), Sym("decipa"));
            c[enums::CENTIPASCAL] = Mul(Int(10), Sym("decipa"));
            c[enums::DECAPASCAL] = Mul(Rat(Int(1), Int(100)), Sym("decipa"));
            c[enums::DECIBAR] = Mul(Rat(Int(1), Pow(10, 5)), Sym("decipa"));
            c[enums::EXAPASCAL] = Mul(Rat(Int(1), Pow(10, 19)), Sym("decipa"));
            c[enums::FEMTOPASCAL] = Mul(Pow(10, 14), Sym("decipa"));
            c[enums::GIGAPASCAL] = Mul(Rat(Int(1), Pow(10, 10)), Sym("decipa"));
            c[enums::HECTOPASCAL] = Mul(Rat(Int(1), Int(1000)), Sym("decipa"));
            c[enums::KILOBAR] = Mul(Rat(Int(1), Pow(10, 9)), Sym("decipa"));
            c[enums::KILOPASCAL] = Mul(Rat(Int(1), Pow(10, 4)), Sym("decipa"));
            c[enums::MEGABAR] = Mul(Rat(Int(1), Pow(10, 12)), Sym("decipa"));
            c[enums::MEGAPASCAL] = Mul(Rat(Int(1), Pow(10, 7)), Sym("decipa"));
            c[enums::MICROPASCAL] = Mul(Pow(10, 5), Sym("decipa"));
            c[enums::MILLIBAR] = Mul(Rat(Int(1), Int(1000)), Sym("decipa"));
            c[enums::MILLIPASCAL] = Mul(Int(100), Sym("decipa"));
            c[enums::MILLITORR] = Mul(Rat(Int(19), Int(25331250)), Sym("decipa"));
            c[enums::MMHG] = Mul(Rat(Mul(Int(2), Pow(10, 7)), Int("26664477483")), Sym("decipa"));
            c[enums::NANOPASCAL] = Mul(Pow(10, 8), Sym("decipa"));
            c[enums::PETAPASCAL] = Mul(Rat(Int(1), Pow(10, 16)), Sym("decipa"));
            c[enums::PICOPASCAL] = Mul(Pow(10, 11), Sym("decipa"));
            c[enums::PSI] = Mul(Rat(Mul(Int(25), Pow(10, 8)), Int("172368932329209")), Sym("decipa"));
            c[enums::Pascal] = Mul(Rat(Int(1), Int(10)), Sym("decipa"));
            c[enums::TERAPASCAL] = Mul(Rat(Int(1), Pow(10, 13)), Sym("decipa"));
            c[enums::TORR] = Mul(Rat(Int(76), Int(101325)), Sym("decipa"));
            c[enums::YOCTOPASCAL] = Mul(Pow(10, 23), Sym("decipa"));
            c[enums::YOTTAPASCAL] = Mul(Rat(Int(1), Pow(10, 25)), Sym("decipa"));
            c[enums::ZEPTOPASCAL] = Mul(Pow(10, 20), Sym("decipa"));
            c[enums::ZETTAPASCAL] = Mul(Rat(Int(1), Pow(10, 22)), Sym("decipa"));
            return c;
        }

        static std::map<UnitsPressure, ConversionPtr> createMapEXAPASCAL() {
            std::map<UnitsPressure, ConversionPtr> c;
            c[enums::ATMOSPHERE] = Mul(Rat(Mul(Int(4), Pow(10, 16)), Int(4053)), Sym("exapa"));
            c[enums::ATTOPASCAL] = Mul(Pow(10, 36), Sym("exapa"));
            c[enums::BAR] = Mul(Pow(10, 13), Sym("exapa"));
            c[enums::CENTIBAR] = Mul(Pow(10, 15), Sym("exapa"));
            c[enums::CENTIPASCAL] = Mul(Pow(10, 20), Sym("exapa"));
            c[enums::DECAPASCAL] = Mul(Pow(10, 17), Sym("exapa"));
            c[enums::DECIBAR] = Mul(Pow(10, 14), Sym("exapa"));
            c[enums::DECIPASCAL] = Mul(Pow(10, 19), Sym("exapa"));
            c[enums::FEMTOPASCAL] = Mul(Pow(10, 33), Sym("exapa"));
            c[enums::GIGAPASCAL] = Mul(Pow(10, 9), Sym("exapa"));
            c[enums::HECTOPASCAL] = Mul(Pow(10, 16), Sym("exapa"));
            c[enums::KILOBAR] = Mul(Pow(10, 10), Sym("exapa"));
            c[enums::KILOPASCAL] = Mul(Pow(10, 15), Sym("exapa"));
            c[enums::MEGABAR] = Mul(Pow(10, 7), Sym("exapa"));
            c[enums::MEGAPASCAL] = Mul(Pow(10, 12), Sym("exapa"));
            c[enums::MICROPASCAL] = Mul(Pow(10, 24), Sym("exapa"));
            c[enums::MILLIBAR] = Mul(Pow(10, 16), Sym("exapa"));
            c[enums::MILLIPASCAL] = Mul(Pow(10, 21), Sym("exapa"));
            c[enums::MILLITORR] = Mul(Rat(Mul(Int(304), Pow(10, 14)), Int(4053)), Sym("exapa"));
            c[enums::MMHG] = Mul(Rat(Mul(Int(2), Pow(10, 26)), Int("26664477483")), Sym("exapa"));
            c[enums::NANOPASCAL] = Mul(Pow(10, 27), Sym("exapa"));
            c[enums::PETAPASCAL] = Mul(Int(1000), Sym("exapa"));
            c[enums::PICOPASCAL] = Mul(Pow(10, 30), Sym("exapa"));
            c[enums::PSI] = Mul(Rat(Mul(Int(25), Pow(10, 27)), Int("172368932329209")), Sym("exapa"));
            c[enums::Pascal] = Mul(Pow(10, 18), Sym("exapa"));
            c[enums::TERAPASCAL] = Mul(Pow(10, 6), Sym("exapa"));
            c[enums::TORR] = Mul(Rat(Mul(Int(304), Pow(10, 17)), Int(4053)), Sym("exapa"));
            c[enums::YOCTOPASCAL] = Mul(Pow(10, 42), Sym("exapa"));
            c[enums::YOTTAPASCAL] = Mul(Rat(Int(1), Pow(10, 6)), Sym("exapa"));
            c[enums::ZEPTOPASCAL] = Mul(Pow(10, 39), Sym("exapa"));
            c[enums::ZETTAPASCAL] = Mul(Rat(Int(1), Int(1000)), Sym("exapa"));
            return c;
        }

        static std::map<UnitsPressure, ConversionPtr> createMapFEMTOPASCAL() {
            std::map<UnitsPressure, ConversionPtr> c;
            c[enums::ATMOSPHERE] = Mul(Rat(Int(1), Mul(Int(101325), Pow(10, 15))), Sym("femtopa"));
            c[enums::ATTOPASCAL] = Mul(Int(1000), Sym("femtopa"));
            c[enums::BAR] = Mul(Rat(Int(1), Pow(10, 20)), Sym("femtopa"));
            c[enums::CENTIBAR] = Mul(Rat(Int(1), Pow(10, 18)), Sym("femtopa"));
            c[enums::CENTIPASCAL] = Mul(Rat(Int(1), Pow(10, 13)), Sym("femtopa"));
            c[enums::DECAPASCAL] = Mul(Rat(Int(1), Pow(10, 16)), Sym("femtopa"));
            c[enums::DECIBAR] = Mul(Rat(Int(1), Pow(10, 19)), Sym("femtopa"));
            c[enums::DECIPASCAL] = Mul(Rat(Int(1), Pow(10, 14)), Sym("femtopa"));
            c[enums::EXAPASCAL] = Mul(Rat(Int(1), Pow(10, 33)), Sym("femtopa"));
            c[enums::GIGAPASCAL] = Mul(Rat(Int(1), Pow(10, 24)), Sym("femtopa"));
            c[enums::HECTOPASCAL] = Mul(Rat(Int(1), Pow(10, 17)), Sym("femtopa"));
            c[enums::KILOBAR] = Mul(Rat(Int(1), Pow(10, 23)), Sym("femtopa"));
            c[enums::KILOPASCAL] = Mul(Rat(Int(1), Pow(10, 18)), Sym("femtopa"));
            c[enums::MEGABAR] = Mul(Rat(Int(1), Pow(10, 26)), Sym("femtopa"));
            c[enums::MEGAPASCAL] = Mul(Rat(Int(1), Pow(10, 21)), Sym("femtopa"));
            c[enums::MICROPASCAL] = Mul(Rat(Int(1), Pow(10, 9)), Sym("femtopa"));
            c[enums::MILLIBAR] = Mul(Rat(Int(1), Pow(10, 17)), Sym("femtopa"));
            c[enums::MILLIPASCAL] = Mul(Rat(Int(1), Pow(10, 12)), Sym("femtopa"));
            c[enums::MILLITORR] = Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 15))), Sym("femtopa"));
            c[enums::MMHG] = Mul(Rat(Int(1), Mul(Int("133322387415"), Pow(10, 6))), Sym("femtopa"));
            c[enums::NANOPASCAL] = Mul(Rat(Int(1), Pow(10, 6)), Sym("femtopa"));
            c[enums::PETAPASCAL] = Mul(Rat(Int(1), Pow(10, 30)), Sym("femtopa"));
            c[enums::PICOPASCAL] = Mul(Rat(Int(1), Int(1000)), Sym("femtopa"));
            c[enums::PSI] = Mul(Rat(Int(1), Mul(Int("689475729316836"), Pow(10, 4))), Sym("femtopa"));
            c[enums::Pascal] = Mul(Rat(Int(1), Pow(10, 15)), Sym("femtopa"));
            c[enums::TERAPASCAL] = Mul(Rat(Int(1), Pow(10, 27)), Sym("femtopa"));
            c[enums::TORR] = Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 12))), Sym("femtopa"));
            c[enums::YOCTOPASCAL] = Mul(Pow(10, 9), Sym("femtopa"));
            c[enums::YOTTAPASCAL] = Mul(Rat(Int(1), Pow(10, 39)), Sym("femtopa"));
            c[enums::ZEPTOPASCAL] = Mul(Pow(10, 6), Sym("femtopa"));
            c[enums::ZETTAPASCAL] = Mul(Rat(Int(1), Pow(10, 36)), Sym("femtopa"));
            return c;
        }

        static std::map<UnitsPressure, ConversionPtr> createMapGIGAPASCAL() {
            std::map<UnitsPressure, ConversionPtr> c;
            c[enums::ATMOSPHERE] = Mul(Rat(Mul(Int(4), Pow(10, 7)), Int(4053)), Sym("gigapa"));
            c[enums::ATTOPASCAL] = Mul(Pow(10, 27), Sym("gigapa"));
            c[enums::BAR] = Mul(Pow(10, 4), Sym("gigapa"));
            c[enums::CENTIBAR] = Mul(Pow(10, 6), Sym("gigapa"));
            c[enums::CENTIPASCAL] = Mul(Pow(10, 11), Sym("gigapa"));
            c[enums::DECAPASCAL] = Mul(Pow(10, 8), Sym("gigapa"));
            c[enums::DECIBAR] = Mul(Pow(10, 5), Sym("gigapa"));
            c[enums::DECIPASCAL] = Mul(Pow(10, 10), Sym("gigapa"));
            c[enums::EXAPASCAL] = Mul(Rat(Int(1), Pow(10, 9)), Sym("gigapa"));
            c[enums::FEMTOPASCAL] = Mul(Pow(10, 24), Sym("gigapa"));
            c[enums::HECTOPASCAL] = Mul(Pow(10, 7), Sym("gigapa"));
            c[enums::KILOBAR] = Mul(Int(10), Sym("gigapa"));
            c[enums::KILOPASCAL] = Mul(Pow(10, 6), Sym("gigapa"));
            c[enums::MEGABAR] = Mul(Rat(Int(1), Int(100)), Sym("gigapa"));
            c[enums::MEGAPASCAL] = Mul(Int(1000), Sym("gigapa"));
            c[enums::MICROPASCAL] = Mul(Pow(10, 15), Sym("gigapa"));
            c[enums::MILLIBAR] = Mul(Pow(10, 7), Sym("gigapa"));
            c[enums::MILLIPASCAL] = Mul(Pow(10, 12), Sym("gigapa"));
            c[enums::MILLITORR] = Mul(Rat(Mul(Int(304), Pow(10, 5)), Int(4053)), Sym("gigapa"));
            c[enums::MMHG] = Mul(Rat(Mul(Int(2), Pow(10, 17)), Int("26664477483")), Sym("gigapa"));
            c[enums::NANOPASCAL] = Mul(Pow(10, 18), Sym("gigapa"));
            c[enums::PETAPASCAL] = Mul(Rat(Int(1), Pow(10, 6)), Sym("gigapa"));
            c[enums::PICOPASCAL] = Mul(Pow(10, 21), Sym("gigapa"));
            c[enums::PSI] = Mul(Rat(Mul(Int(25), Pow(10, 18)), Int("172368932329209")), Sym("gigapa"));
            c[enums::Pascal] = Mul(Pow(10, 9), Sym("gigapa"));
            c[enums::TERAPASCAL] = Mul(Rat(Int(1), Int(1000)), Sym("gigapa"));
            c[enums::TORR] = Mul(Rat(Mul(Int(304), Pow(10, 8)), Int(4053)), Sym("gigapa"));
            c[enums::YOCTOPASCAL] = Mul(Pow(10, 33), Sym("gigapa"));
            c[enums::YOTTAPASCAL] = Mul(Rat(Int(1), Pow(10, 15)), Sym("gigapa"));
            c[enums::ZEPTOPASCAL] = Mul(Pow(10, 30), Sym("gigapa"));
            c[enums::ZETTAPASCAL] = Mul(Rat(Int(1), Pow(10, 12)), Sym("gigapa"));
            return c;
        }

        static std::map<UnitsPressure, ConversionPtr> createMapHECTOPASCAL() {
            std::map<UnitsPressure, ConversionPtr> c;
            c[enums::ATMOSPHERE] = Mul(Rat(Int(4), Int(4053)), Sym("hectopa"));
            c[enums::ATTOPASCAL] = Mul(Pow(10, 20), Sym("hectopa"));
            c[enums::BAR] = Mul(Rat(Int(1), Int(1000)), Sym("hectopa"));
            c[enums::CENTIBAR] = Mul(Rat(Int(1), Int(10)), Sym("hectopa"));
            c[enums::CENTIPASCAL] = Mul(Pow(10, 4), Sym("hectopa"));
            c[enums::DECAPASCAL] = Mul(Int(10), Sym("hectopa"));
            c[enums::DECIBAR] = Mul(Rat(Int(1), Int(100)), Sym("hectopa"));
            c[enums::DECIPASCAL] = Mul(Int(1000), Sym("hectopa"));
            c[enums::EXAPASCAL] = Mul(Rat(Int(1), Pow(10, 16)), Sym("hectopa"));
            c[enums::FEMTOPASCAL] = Mul(Pow(10, 17), Sym("hectopa"));
            c[enums::GIGAPASCAL] = Mul(Rat(Int(1), Pow(10, 7)), Sym("hectopa"));
            c[enums::KILOBAR] = Mul(Rat(Int(1), Pow(10, 6)), Sym("hectopa"));
            c[enums::KILOPASCAL] = Mul(Rat(Int(1), Int(10)), Sym("hectopa"));
            c[enums::MEGABAR] = Mul(Rat(Int(1), Pow(10, 9)), Sym("hectopa"));
            c[enums::MEGAPASCAL] = Mul(Rat(Int(1), Pow(10, 4)), Sym("hectopa"));
            c[enums::MICROPASCAL] = Mul(Pow(10, 8), Sym("hectopa"));
            c[enums::MILLIBAR] = Sym("hectopa");
            c[enums::MILLIPASCAL] = Mul(Pow(10, 5), Sym("hectopa"));
            c[enums::MILLITORR] = Mul(Rat(Int(76), Int(101325)), Sym("hectopa"));
            c[enums::MMHG] = Mul(Rat(Mul(Int(2), Pow(10, 10)), Int("26664477483")), Sym("hectopa"));
            c[enums::NANOPASCAL] = Mul(Pow(10, 11), Sym("hectopa"));
            c[enums::PETAPASCAL] = Mul(Rat(Int(1), Pow(10, 13)), Sym("hectopa"));
            c[enums::PICOPASCAL] = Mul(Pow(10, 14), Sym("hectopa"));
            c[enums::PSI] = Mul(Rat(Mul(Int(25), Pow(10, 11)), Int("172368932329209")), Sym("hectopa"));
            c[enums::Pascal] = Mul(Int(100), Sym("hectopa"));
            c[enums::TERAPASCAL] = Mul(Rat(Int(1), Pow(10, 10)), Sym("hectopa"));
            c[enums::TORR] = Mul(Rat(Int(3040), Int(4053)), Sym("hectopa"));
            c[enums::YOCTOPASCAL] = Mul(Pow(10, 26), Sym("hectopa"));
            c[enums::YOTTAPASCAL] = Mul(Rat(Int(1), Pow(10, 22)), Sym("hectopa"));
            c[enums::ZEPTOPASCAL] = Mul(Pow(10, 23), Sym("hectopa"));
            c[enums::ZETTAPASCAL] = Mul(Rat(Int(1), Pow(10, 19)), Sym("hectopa"));
            return c;
        }

        static std::map<UnitsPressure, ConversionPtr> createMapKILOBAR() {
            std::map<UnitsPressure, ConversionPtr> c;
            c[enums::ATMOSPHERE] = Mul(Rat(Mul(Int(4), Pow(10, 6)), Int(4053)), Sym("kbar"));
            c[enums::ATTOPASCAL] = Mul(Pow(10, 26), Sym("kbar"));
            c[enums::BAR] = Mul(Int(1000), Sym("kbar"));
            c[enums::CENTIBAR] = Mul(Pow(10, 5), Sym("kbar"));
            c[enums::CENTIPASCAL] = Mul(Pow(10, 10), Sym("kbar"));
            c[enums::DECAPASCAL] = Mul(Pow(10, 7), Sym("kbar"));
            c[enums::DECIBAR] = Mul(Pow(10, 4), Sym("kbar"));
            c[enums::DECIPASCAL] = Mul(Pow(10, 9), Sym("kbar"));
            c[enums::EXAPASCAL] = Mul(Rat(Int(1), Pow(10, 10)), Sym("kbar"));
            c[enums::FEMTOPASCAL] = Mul(Pow(10, 23), Sym("kbar"));
            c[enums::GIGAPASCAL] = Mul(Rat(Int(1), Int(10)), Sym("kbar"));
            c[enums::HECTOPASCAL] = Mul(Pow(10, 6), Sym("kbar"));
            c[enums::KILOPASCAL] = Mul(Pow(10, 5), Sym("kbar"));
            c[enums::MEGABAR] = Mul(Rat(Int(1), Int(1000)), Sym("kbar"));
            c[enums::MEGAPASCAL] = Mul(Int(100), Sym("kbar"));
            c[enums::MICROPASCAL] = Mul(Pow(10, 14), Sym("kbar"));
            c[enums::MILLIBAR] = Mul(Pow(10, 6), Sym("kbar"));
            c[enums::MILLIPASCAL] = Mul(Pow(10, 11), Sym("kbar"));
            c[enums::MILLITORR] = Mul(Rat(Mul(Int(304), Pow(10, 4)), Int(4053)), Sym("kbar"));
            c[enums::MMHG] = Mul(Rat(Mul(Int(2), Pow(10, 16)), Int("26664477483")), Sym("kbar"));
            c[enums::NANOPASCAL] = Mul(Pow(10, 17), Sym("kbar"));
            c[enums::PETAPASCAL] = Mul(Rat(Int(1), Pow(10, 7)), Sym("kbar"));
            c[enums::PICOPASCAL] = Mul(Pow(10, 20), Sym("kbar"));
            c[enums::PSI] = Mul(Rat(Mul(Int(25), Pow(10, 17)), Int("172368932329209")), Sym("kbar"));
            c[enums::Pascal] = Mul(Pow(10, 8), Sym("kbar"));
            c[enums::TERAPASCAL] = Mul(Rat(Int(1), Pow(10, 4)), Sym("kbar"));
            c[enums::TORR] = Mul(Rat(Mul(Int(304), Pow(10, 7)), Int(4053)), Sym("kbar"));
            c[enums::YOCTOPASCAL] = Mul(Pow(10, 32), Sym("kbar"));
            c[enums::YOTTAPASCAL] = Mul(Rat(Int(1), Pow(10, 16)), Sym("kbar"));
            c[enums::ZEPTOPASCAL] = Mul(Pow(10, 29), Sym("kbar"));
            c[enums::ZETTAPASCAL] = Mul(Rat(Int(1), Pow(10, 13)), Sym("kbar"));
            return c;
        }

        static std::map<UnitsPressure, ConversionPtr> createMapKILOPASCAL() {
            std::map<UnitsPressure, ConversionPtr> c;
            c[enums::ATMOSPHERE] = Mul(Rat(Int(40), Int(4053)), Sym("kilopa"));
            c[enums::ATTOPASCAL] = Mul(Pow(10, 21), Sym("kilopa"));
            c[enums::BAR] = Mul(Rat(Int(1), Int(100)), Sym("kilopa"));
            c[enums::CENTIBAR] = Sym("kilopa");
            c[enums::CENTIPASCAL] = Mul(Pow(10, 5), Sym("kilopa"));
            c[enums::DECAPASCAL] = Mul(Int(100), Sym("kilopa"));
            c[enums::DECIBAR] = Mul(Rat(Int(1), Int(10)), Sym("kilopa"));
            c[enums::DECIPASCAL] = Mul(Pow(10, 4), Sym("kilopa"));
            c[enums::EXAPASCAL] = Mul(Rat(Int(1), Pow(10, 15)), Sym("kilopa"));
            c[enums::FEMTOPASCAL] = Mul(Pow(10, 18), Sym("kilopa"));
            c[enums::GIGAPASCAL] = Mul(Rat(Int(1), Pow(10, 6)), Sym("kilopa"));
            c[enums::HECTOPASCAL] = Mul(Int(10), Sym("kilopa"));
            c[enums::KILOBAR] = Mul(Rat(Int(1), Pow(10, 5)), Sym("kilopa"));
            c[enums::MEGABAR] = Mul(Rat(Int(1), Pow(10, 8)), Sym("kilopa"));
            c[enums::MEGAPASCAL] = Mul(Rat(Int(1), Int(1000)), Sym("kilopa"));
            c[enums::MICROPASCAL] = Mul(Pow(10, 9), Sym("kilopa"));
            c[enums::MILLIBAR] = Mul(Int(10), Sym("kilopa"));
            c[enums::MILLIPASCAL] = Mul(Pow(10, 6), Sym("kilopa"));
            c[enums::MILLITORR] = Mul(Rat(Int(152), Int(20265)), Sym("kilopa"));
            c[enums::MMHG] = Mul(Rat(Mul(Int(2), Pow(10, 11)), Int("26664477483")), Sym("kilopa"));
            c[enums::NANOPASCAL] = Mul(Pow(10, 12), Sym("kilopa"));
            c[enums::PETAPASCAL] = Mul(Rat(Int(1), Pow(10, 12)), Sym("kilopa"));
            c[enums::PICOPASCAL] = Mul(Pow(10, 15), Sym("kilopa"));
            c[enums::PSI] = Mul(Rat(Mul(Int(25), Pow(10, 12)), Int("172368932329209")), Sym("kilopa"));
            c[enums::Pascal] = Mul(Int(1000), Sym("kilopa"));
            c[enums::TERAPASCAL] = Mul(Rat(Int(1), Pow(10, 9)), Sym("kilopa"));
            c[enums::TORR] = Mul(Rat(Int(30400), Int(4053)), Sym("kilopa"));
            c[enums::YOCTOPASCAL] = Mul(Pow(10, 27), Sym("kilopa"));
            c[enums::YOTTAPASCAL] = Mul(Rat(Int(1), Pow(10, 21)), Sym("kilopa"));
            c[enums::ZEPTOPASCAL] = Mul(Pow(10, 24), Sym("kilopa"));
            c[enums::ZETTAPASCAL] = Mul(Rat(Int(1), Pow(10, 18)), Sym("kilopa"));
            return c;
        }

        static std::map<UnitsPressure, ConversionPtr> createMapMEGABAR() {
            std::map<UnitsPressure, ConversionPtr> c;
            c[enums::ATMOSPHERE] = Mul(Rat(Mul(Int(4), Pow(10, 9)), Int(4053)), Sym("megabar"));
            c[enums::ATTOPASCAL] = Mul(Pow(10, 29), Sym("megabar"));
            c[enums::BAR] = Mul(Pow(10, 6), Sym("megabar"));
            c[enums::CENTIBAR] = Mul(Pow(10, 8), Sym("megabar"));
            c[enums::CENTIPASCAL] = Mul(Pow(10, 13), Sym("megabar"));
            c[enums::DECAPASCAL] = Mul(Pow(10, 10), Sym("megabar"));
            c[enums::DECIBAR] = Mul(Pow(10, 7), Sym("megabar"));
            c[enums::DECIPASCAL] = Mul(Pow(10, 12), Sym("megabar"));
            c[enums::EXAPASCAL] = Mul(Rat(Int(1), Pow(10, 7)), Sym("megabar"));
            c[enums::FEMTOPASCAL] = Mul(Pow(10, 26), Sym("megabar"));
            c[enums::GIGAPASCAL] = Mul(Int(100), Sym("megabar"));
            c[enums::HECTOPASCAL] = Mul(Pow(10, 9), Sym("megabar"));
            c[enums::KILOBAR] = Mul(Int(1000), Sym("megabar"));
            c[enums::KILOPASCAL] = Mul(Pow(10, 8), Sym("megabar"));
            c[enums::MEGAPASCAL] = Mul(Pow(10, 5), Sym("megabar"));
            c[enums::MICROPASCAL] = Mul(Pow(10, 17), Sym("megabar"));
            c[enums::MILLIBAR] = Mul(Pow(10, 9), Sym("megabar"));
            c[enums::MILLIPASCAL] = Mul(Pow(10, 14), Sym("megabar"));
            c[enums::MILLITORR] = Mul(Rat(Mul(Int(304), Pow(10, 7)), Int(4053)), Sym("megabar"));
            c[enums::MMHG] = Mul(Rat(Mul(Int(2), Pow(10, 19)), Int("26664477483")), Sym("megabar"));
            c[enums::NANOPASCAL] = Mul(Pow(10, 20), Sym("megabar"));
            c[enums::PETAPASCAL] = Mul(Rat(Int(1), Pow(10, 4)), Sym("megabar"));
            c[enums::PICOPASCAL] = Mul(Pow(10, 23), Sym("megabar"));
            c[enums::PSI] = Mul(Rat(Mul(Int(25), Pow(10, 20)), Int("172368932329209")), Sym("megabar"));
            c[enums::Pascal] = Mul(Pow(10, 11), Sym("megabar"));
            c[enums::TERAPASCAL] = Mul(Rat(Int(1), Int(10)), Sym("megabar"));
            c[enums::TORR] = Mul(Rat(Mul(Int(304), Pow(10, 10)), Int(4053)), Sym("megabar"));
            c[enums::YOCTOPASCAL] = Mul(Pow(10, 35), Sym("megabar"));
            c[enums::YOTTAPASCAL] = Mul(Rat(Int(1), Pow(10, 13)), Sym("megabar"));
            c[enums::ZEPTOPASCAL] = Mul(Pow(10, 32), Sym("megabar"));
            c[enums::ZETTAPASCAL] = Mul(Rat(Int(1), Pow(10, 10)), Sym("megabar"));
            return c;
        }

        static std::map<UnitsPressure, ConversionPtr> createMapMEGAPASCAL() {
            std::map<UnitsPressure, ConversionPtr> c;
            c[enums::ATMOSPHERE] = Mul(Rat(Mul(Int(4), Pow(10, 4)), Int(4053)), Sym("megapa"));
            c[enums::ATTOPASCAL] = Mul(Pow(10, 24), Sym("megapa"));
            c[enums::BAR] = Mul(Int(10), Sym("megapa"));
            c[enums::CENTIBAR] = Mul(Int(1000), Sym("megapa"));
            c[enums::CENTIPASCAL] = Mul(Pow(10, 8), Sym("megapa"));
            c[enums::DECAPASCAL] = Mul(Pow(10, 5), Sym("megapa"));
            c[enums::DECIBAR] = Mul(Int(100), Sym("megapa"));
            c[enums::DECIPASCAL] = Mul(Pow(10, 7), Sym("megapa"));
            c[enums::EXAPASCAL] = Mul(Rat(Int(1), Pow(10, 12)), Sym("megapa"));
            c[enums::FEMTOPASCAL] = Mul(Pow(10, 21), Sym("megapa"));
            c[enums::GIGAPASCAL] = Mul(Rat(Int(1), Int(1000)), Sym("megapa"));
            c[enums::HECTOPASCAL] = Mul(Pow(10, 4), Sym("megapa"));
            c[enums::KILOBAR] = Mul(Rat(Int(1), Int(100)), Sym("megapa"));
            c[enums::KILOPASCAL] = Mul(Int(1000), Sym("megapa"));
            c[enums::MEGABAR] = Mul(Rat(Int(1), Pow(10, 5)), Sym("megapa"));
            c[enums::MICROPASCAL] = Mul(Pow(10, 12), Sym("megapa"));
            c[enums::MILLIBAR] = Mul(Pow(10, 4), Sym("megapa"));
            c[enums::MILLIPASCAL] = Mul(Pow(10, 9), Sym("megapa"));
            c[enums::MILLITORR] = Mul(Rat(Int(30400), Int(4053)), Sym("megapa"));
            c[enums::MMHG] = Mul(Rat(Mul(Int(2), Pow(10, 14)), Int("26664477483")), Sym("megapa"));
            c[enums::NANOPASCAL] = Mul(Pow(10, 15), Sym("megapa"));
            c[enums::PETAPASCAL] = Mul(Rat(Int(1), Pow(10, 9)), Sym("megapa"));
            c[enums::PICOPASCAL] = Mul(Pow(10, 18), Sym("megapa"));
            c[enums::PSI] = Mul(Rat(Mul(Int(25), Pow(10, 15)), Int("172368932329209")), Sym("megapa"));
            c[enums::Pascal] = Mul(Pow(10, 6), Sym("megapa"));
            c[enums::TERAPASCAL] = Mul(Rat(Int(1), Pow(10, 6)), Sym("megapa"));
            c[enums::TORR] = Mul(Rat(Mul(Int(304), Pow(10, 5)), Int(4053)), Sym("megapa"));
            c[enums::YOCTOPASCAL] = Mul(Pow(10, 30), Sym("megapa"));
            c[enums::YOTTAPASCAL] = Mul(Rat(Int(1), Pow(10, 18)), Sym("megapa"));
            c[enums::ZEPTOPASCAL] = Mul(Pow(10, 27), Sym("megapa"));
            c[enums::ZETTAPASCAL] = Mul(Rat(Int(1), Pow(10, 15)), Sym("megapa"));
            return c;
        }

        static std::map<UnitsPressure, ConversionPtr> createMapMICROPASCAL() {
            std::map<UnitsPressure, ConversionPtr> c;
            c[enums::ATMOSPHERE] = Mul(Rat(Int(1), Mul(Int(101325), Pow(10, 6))), Sym("micropa"));
            c[enums::ATTOPASCAL] = Mul(Pow(10, 12), Sym("micropa"));
            c[enums::BAR] = Mul(Rat(Int(1), Pow(10, 11)), Sym("micropa"));
            c[enums::CENTIBAR] = Mul(Rat(Int(1), Pow(10, 9)), Sym("micropa"));
            c[enums::CENTIPASCAL] = Mul(Rat(Int(1), Pow(10, 4)), Sym("micropa"));
            c[enums::DECAPASCAL] = Mul(Rat(Int(1), Pow(10, 7)), Sym("micropa"));
            c[enums::DECIBAR] = Mul(Rat(Int(1), Pow(10, 10)), Sym("micropa"));
            c[enums::DECIPASCAL] = Mul(Rat(Int(1), Pow(10, 5)), Sym("micropa"));
            c[enums::EXAPASCAL] = Mul(Rat(Int(1), Pow(10, 24)), Sym("micropa"));
            c[enums::FEMTOPASCAL] = Mul(Pow(10, 9), Sym("micropa"));
            c[enums::GIGAPASCAL] = Mul(Rat(Int(1), Pow(10, 15)), Sym("micropa"));
            c[enums::HECTOPASCAL] = Mul(Rat(Int(1), Pow(10, 8)), Sym("micropa"));
            c[enums::KILOBAR] = Mul(Rat(Int(1), Pow(10, 14)), Sym("micropa"));
            c[enums::KILOPASCAL] = Mul(Rat(Int(1), Pow(10, 9)), Sym("micropa"));
            c[enums::MEGABAR] = Mul(Rat(Int(1), Pow(10, 17)), Sym("micropa"));
            c[enums::MEGAPASCAL] = Mul(Rat(Int(1), Pow(10, 12)), Sym("micropa"));
            c[enums::MILLIBAR] = Mul(Rat(Int(1), Pow(10, 8)), Sym("micropa"));
            c[enums::MILLIPASCAL] = Mul(Rat(Int(1), Int(1000)), Sym("micropa"));
            c[enums::MILLITORR] = Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 6))), Sym("micropa"));
            c[enums::MMHG] = Mul(Rat(Int(200), Int("26664477483")), Sym("micropa"));
            c[enums::NANOPASCAL] = Mul(Int(1000), Sym("micropa"));
            c[enums::PETAPASCAL] = Mul(Rat(Int(1), Pow(10, 21)), Sym("micropa"));
            c[enums::PICOPASCAL] = Mul(Pow(10, 6), Sym("micropa"));
            c[enums::PSI] = Mul(Rat(Int(25000), Int("172368932329209")), Sym("micropa"));
            c[enums::Pascal] = Mul(Rat(Int(1), Pow(10, 6)), Sym("micropa"));
            c[enums::TERAPASCAL] = Mul(Rat(Int(1), Pow(10, 18)), Sym("micropa"));
            c[enums::TORR] = Mul(Rat(Int(19), Int("2533125000")), Sym("micropa"));
            c[enums::YOCTOPASCAL] = Mul(Pow(10, 18), Sym("micropa"));
            c[enums::YOTTAPASCAL] = Mul(Rat(Int(1), Pow(10, 30)), Sym("micropa"));
            c[enums::ZEPTOPASCAL] = Mul(Pow(10, 15), Sym("micropa"));
            c[enums::ZETTAPASCAL] = Mul(Rat(Int(1), Pow(10, 27)), Sym("micropa"));
            return c;
        }

        static std::map<UnitsPressure, ConversionPtr> createMapMILLIBAR() {
            std::map<UnitsPressure, ConversionPtr> c;
            c[enums::ATMOSPHERE] = Mul(Rat(Int(4), Int(4053)), Sym("mbar"));
            c[enums::ATTOPASCAL] = Mul(Pow(10, 20), Sym("mbar"));
            c[enums::BAR] = Mul(Rat(Int(1), Int(1000)), Sym("mbar"));
            c[enums::CENTIBAR] = Mul(Rat(Int(1), Int(10)), Sym("mbar"));
            c[enums::CENTIPASCAL] = Mul(Pow(10, 4), Sym("mbar"));
            c[enums::DECAPASCAL] = Mul(Int(10), Sym("mbar"));
            c[enums::DECIBAR] = Mul(Rat(Int(1), Int(100)), Sym("mbar"));
            c[enums::DECIPASCAL] = Mul(Int(1000), Sym("mbar"));
            c[enums::EXAPASCAL] = Mul(Rat(Int(1), Pow(10, 16)), Sym("mbar"));
            c[enums::FEMTOPASCAL] = Mul(Pow(10, 17), Sym("mbar"));
            c[enums::GIGAPASCAL] = Mul(Rat(Int(1), Pow(10, 7)), Sym("mbar"));
            c[enums::HECTOPASCAL] = Sym("mbar");
            c[enums::KILOBAR] = Mul(Rat(Int(1), Pow(10, 6)), Sym("mbar"));
            c[enums::KILOPASCAL] = Mul(Rat(Int(1), Int(10)), Sym("mbar"));
            c[enums::MEGABAR] = Mul(Rat(Int(1), Pow(10, 9)), Sym("mbar"));
            c[enums::MEGAPASCAL] = Mul(Rat(Int(1), Pow(10, 4)), Sym("mbar"));
            c[enums::MICROPASCAL] = Mul(Pow(10, 8), Sym("mbar"));
            c[enums::MILLIPASCAL] = Mul(Pow(10, 5), Sym("mbar"));
            c[enums::MILLITORR] = Mul(Rat(Int(76), Int(101325)), Sym("mbar"));
            c[enums::MMHG] = Mul(Rat(Mul(Int(2), Pow(10, 10)), Int("26664477483")), Sym("mbar"));
            c[enums::NANOPASCAL] = Mul(Pow(10, 11), Sym("mbar"));
            c[enums::PETAPASCAL] = Mul(Rat(Int(1), Pow(10, 13)), Sym("mbar"));
            c[enums::PICOPASCAL] = Mul(Pow(10, 14), Sym("mbar"));
            c[enums::PSI] = Mul(Rat(Mul(Int(25), Pow(10, 11)), Int("172368932329209")), Sym("mbar"));
            c[enums::Pascal] = Mul(Int(100), Sym("mbar"));
            c[enums::TERAPASCAL] = Mul(Rat(Int(1), Pow(10, 10)), Sym("mbar"));
            c[enums::TORR] = Mul(Rat(Int(3040), Int(4053)), Sym("mbar"));
            c[enums::YOCTOPASCAL] = Mul(Pow(10, 26), Sym("mbar"));
            c[enums::YOTTAPASCAL] = Mul(Rat(Int(1), Pow(10, 22)), Sym("mbar"));
            c[enums::ZEPTOPASCAL] = Mul(Pow(10, 23), Sym("mbar"));
            c[enums::ZETTAPASCAL] = Mul(Rat(Int(1), Pow(10, 19)), Sym("mbar"));
            return c;
        }

        static std::map<UnitsPressure, ConversionPtr> createMapMILLIPASCAL() {
            std::map<UnitsPressure, ConversionPtr> c;
            c[enums::ATMOSPHERE] = Mul(Rat(Int(1), Int(101325000)), Sym("millipa"));
            c[enums::ATTOPASCAL] = Mul(Pow(10, 15), Sym("millipa"));
            c[enums::BAR] = Mul(Rat(Int(1), Pow(10, 8)), Sym("millipa"));
            c[enums::CENTIBAR] = Mul(Rat(Int(1), Pow(10, 6)), Sym("millipa"));
            c[enums::CENTIPASCAL] = Mul(Rat(Int(1), Int(10)), Sym("millipa"));
            c[enums::DECAPASCAL] = Mul(Rat(Int(1), Pow(10, 4)), Sym("millipa"));
            c[enums::DECIBAR] = Mul(Rat(Int(1), Pow(10, 7)), Sym("millipa"));
            c[enums::DECIPASCAL] = Mul(Rat(Int(1), Int(100)), Sym("millipa"));
            c[enums::EXAPASCAL] = Mul(Rat(Int(1), Pow(10, 21)), Sym("millipa"));
            c[enums::FEMTOPASCAL] = Mul(Pow(10, 12), Sym("millipa"));
            c[enums::GIGAPASCAL] = Mul(Rat(Int(1), Pow(10, 12)), Sym("millipa"));
            c[enums::HECTOPASCAL] = Mul(Rat(Int(1), Pow(10, 5)), Sym("millipa"));
            c[enums::KILOBAR] = Mul(Rat(Int(1), Pow(10, 11)), Sym("millipa"));
            c[enums::KILOPASCAL] = Mul(Rat(Int(1), Pow(10, 6)), Sym("millipa"));
            c[enums::MEGABAR] = Mul(Rat(Int(1), Pow(10, 14)), Sym("millipa"));
            c[enums::MEGAPASCAL] = Mul(Rat(Int(1), Pow(10, 9)), Sym("millipa"));
            c[enums::MICROPASCAL] = Mul(Int(1000), Sym("millipa"));
            c[enums::MILLIBAR] = Mul(Rat(Int(1), Pow(10, 5)), Sym("millipa"));
            c[enums::MILLITORR] = Mul(Rat(Int(19), Int("2533125000")), Sym("millipa"));
            c[enums::MMHG] = Mul(Rat(Mul(Int(2), Pow(10, 5)), Int("26664477483")), Sym("millipa"));
            c[enums::NANOPASCAL] = Mul(Pow(10, 6), Sym("millipa"));
            c[enums::PETAPASCAL] = Mul(Rat(Int(1), Pow(10, 18)), Sym("millipa"));
            c[enums::PICOPASCAL] = Mul(Pow(10, 9), Sym("millipa"));
            c[enums::PSI] = Mul(Rat(Mul(Int(25), Pow(10, 6)), Int("172368932329209")), Sym("millipa"));
            c[enums::Pascal] = Mul(Rat(Int(1), Int(1000)), Sym("millipa"));
            c[enums::TERAPASCAL] = Mul(Rat(Int(1), Pow(10, 15)), Sym("millipa"));
            c[enums::TORR] = Mul(Rat(Int(19), Int(2533125)), Sym("millipa"));
            c[enums::YOCTOPASCAL] = Mul(Pow(10, 21), Sym("millipa"));
            c[enums::YOTTAPASCAL] = Mul(Rat(Int(1), Pow(10, 27)), Sym("millipa"));
            c[enums::ZEPTOPASCAL] = Mul(Pow(10, 18), Sym("millipa"));
            c[enums::ZETTAPASCAL] = Mul(Rat(Int(1), Pow(10, 24)), Sym("millipa"));
            return c;
        }

        static std::map<UnitsPressure, ConversionPtr> createMapMILLITORR() {
            std::map<UnitsPressure, ConversionPtr> c;
            c[enums::ATMOSPHERE] = Mul(Rat(Int(25), Int(19)), Sym("mtorr"));
            c[enums::ATTOPASCAL] = Mul(Rat(Mul(Int(2533125), Pow(10, 18)), Int(19)), Sym("mtorr"));
            c[enums::BAR] = Mul(Rat(Int(4053), Int(3040)), Sym("mtorr"));
            c[enums::CENTIBAR] = Mul(Rat(Int(20265), Int(152)), Sym("mtorr"));
            c[enums::CENTIPASCAL] = Mul(Rat(Int(253312500), Int(19)), Sym("mtorr"));
            c[enums::DECAPASCAL] = Mul(Rat(Int(506625), Int(38)), Sym("mtorr"));
            c[enums::DECIBAR] = Mul(Rat(Int(4053), Int(304)), Sym("mtorr"));
            c[enums::DECIPASCAL] = Mul(Rat(Int(25331250), Int(19)), Sym("mtorr"));
            c[enums::EXAPASCAL] = Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 14))), Sym("mtorr"));
            c[enums::FEMTOPASCAL] = Mul(Rat(Mul(Int(2533125), Pow(10, 15)), Int(19)), Sym("mtorr"));
            c[enums::GIGAPASCAL] = Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 5))), Sym("mtorr"));
            c[enums::HECTOPASCAL] = Mul(Rat(Int(101325), Int(76)), Sym("mtorr"));
            c[enums::KILOBAR] = Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 4))), Sym("mtorr"));
            c[enums::KILOPASCAL] = Mul(Rat(Int(20265), Int(152)), Sym("mtorr"));
            c[enums::MEGABAR] = Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 7))), Sym("mtorr"));
            c[enums::MEGAPASCAL] = Mul(Rat(Int(4053), Int(30400)), Sym("mtorr"));
            c[enums::MICROPASCAL] = Mul(Rat(Mul(Int(2533125), Pow(10, 6)), Int(19)), Sym("mtorr"));
            c[enums::MILLIBAR] = Mul(Rat(Int(101325), Int(76)), Sym("mtorr"));
            c[enums::MILLIPASCAL] = Mul(Rat(Int("2533125000"), Int(19)), Sym("mtorr"));
            c[enums::MMHG] = Mul(Rat(Mul(Int(24125), Pow(10, 9)), Int("24125003437")), Sym("mtorr"));
            c[enums::NANOPASCAL] = Mul(Rat(Mul(Int(2533125), Pow(10, 9)), Int(19)), Sym("mtorr"));
            c[enums::PETAPASCAL] = Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 11))), Sym("mtorr"));
            c[enums::PICOPASCAL] = Mul(Rat(Mul(Int(2533125), Pow(10, 12)), Int(19)), Sym("mtorr"));
            c[enums::PSI] = Mul(Rat(Mul(Int(3015625), Pow(10, 9)), Int("155952843535951")), Sym("mtorr"));
            c[enums::Pascal] = Mul(Rat(Int(2533125), Int(19)), Sym("mtorr"));
            c[enums::TERAPASCAL] = Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 8))), Sym("mtorr"));
            c[enums::TORR] = Mul(Int(1000), Sym("mtorr"));
            c[enums::YOCTOPASCAL] = Mul(Rat(Mul(Int(2533125), Pow(10, 24)), Int(19)), Sym("mtorr"));
            c[enums::YOTTAPASCAL] = Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 20))), Sym("mtorr"));
            c[enums::ZEPTOPASCAL] = Mul(Rat(Mul(Int(2533125), Pow(10, 21)), Int(19)), Sym("mtorr"));
            c[enums::ZETTAPASCAL] = Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 17))), Sym("mtorr"));
            return c;
        }

        static std::map<UnitsPressure, ConversionPtr> createMapMMHG() {
            std::map<UnitsPressure, ConversionPtr> c;
            c[enums::ATMOSPHERE] = Mul(Rat(Int(1269737023), Mul(Int(965), Pow(10, 9))), Sym("mmhg"));
            c[enums::ATTOPASCAL] = Mul(Mul(Int("133322387415"), Pow(10, 9)), Sym("mmhg"));
            c[enums::BAR] = Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 13))), Sym("mmhg"));
            c[enums::CENTIBAR] = Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 11))), Sym("mmhg"));
            c[enums::CENTIPASCAL] = Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 6))), Sym("mmhg"));
            c[enums::DECAPASCAL] = Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 9))), Sym("mmhg"));
            c[enums::DECIBAR] = Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 12))), Sym("mmhg"));
            c[enums::DECIPASCAL] = Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 7))), Sym("mmhg"));
            c[enums::EXAPASCAL] = Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 26))), Sym("mmhg"));
            c[enums::FEMTOPASCAL] = Mul(Mul(Int("133322387415"), Pow(10, 6)), Sym("mmhg"));
            c[enums::GIGAPASCAL] = Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 17))), Sym("mmhg"));
            c[enums::HECTOPASCAL] = Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 10))), Sym("mmhg"));
            c[enums::KILOBAR] = Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 16))), Sym("mmhg"));
            c[enums::KILOPASCAL] = Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 11))), Sym("mmhg"));
            c[enums::MEGABAR] = Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 19))), Sym("mmhg"));
            c[enums::MEGAPASCAL] = Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 14))), Sym("mmhg"));
            c[enums::MICROPASCAL] = Mul(Rat(Int("26664477483"), Int(200)), Sym("mmhg"));
            c[enums::MILLIBAR] = Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 10))), Sym("mmhg"));
            c[enums::MILLIPASCAL] = Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 5))), Sym("mmhg"));
            c[enums::MILLITORR] = Mul(Rat(Int("24125003437"), Mul(Int(24125), Pow(10, 9))), Sym("mmhg"));
            c[enums::NANOPASCAL] = Mul(Int("133322387415"), Sym("mmhg"));
            c[enums::PETAPASCAL] = Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 23))), Sym("mmhg"));
            c[enums::PICOPASCAL] = Mul(Int("133322387415000"), Sym("mmhg"));
            c[enums::PSI] = Mul(Rat(Int("158717127875"), Int("8208044396629")), Sym("mmhg"));
            c[enums::Pascal] = Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 8))), Sym("mmhg"));
            c[enums::TERAPASCAL] = Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 20))), Sym("mmhg"));
            c[enums::TORR] = Mul(Rat(Int("24125003437"), Mul(Int(24125), Pow(10, 6))), Sym("mmhg"));
            c[enums::YOCTOPASCAL] = Mul(Mul(Int("133322387415"), Pow(10, 15)), Sym("mmhg"));
            c[enums::YOTTAPASCAL] = Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 32))), Sym("mmhg"));
            c[enums::ZEPTOPASCAL] = Mul(Mul(Int("133322387415"), Pow(10, 12)), Sym("mmhg"));
            c[enums::ZETTAPASCAL] = Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 29))), Sym("mmhg"));
            return c;
        }

        static std::map<UnitsPressure, ConversionPtr> createMapNANOPASCAL() {
            std::map<UnitsPressure, ConversionPtr> c;
            c[enums::ATMOSPHERE] = Mul(Rat(Int(1), Mul(Int(101325), Pow(10, 9))), Sym("nanopa"));
            c[enums::ATTOPASCAL] = Mul(Pow(10, 9), Sym("nanopa"));
            c[enums::BAR] = Mul(Rat(Int(1), Pow(10, 14)), Sym("nanopa"));
            c[enums::CENTIBAR] = Mul(Rat(Int(1), Pow(10, 12)), Sym("nanopa"));
            c[enums::CENTIPASCAL] = Mul(Rat(Int(1), Pow(10, 7)), Sym("nanopa"));
            c[enums::DECAPASCAL] = Mul(Rat(Int(1), Pow(10, 10)), Sym("nanopa"));
            c[enums::DECIBAR] = Mul(Rat(Int(1), Pow(10, 13)), Sym("nanopa"));
            c[enums::DECIPASCAL] = Mul(Rat(Int(1), Pow(10, 8)), Sym("nanopa"));
            c[enums::EXAPASCAL] = Mul(Rat(Int(1), Pow(10, 27)), Sym("nanopa"));
            c[enums::FEMTOPASCAL] = Mul(Pow(10, 6), Sym("nanopa"));
            c[enums::GIGAPASCAL] = Mul(Rat(Int(1), Pow(10, 18)), Sym("nanopa"));
            c[enums::HECTOPASCAL] = Mul(Rat(Int(1), Pow(10, 11)), Sym("nanopa"));
            c[enums::KILOBAR] = Mul(Rat(Int(1), Pow(10, 17)), Sym("nanopa"));
            c[enums::KILOPASCAL] = Mul(Rat(Int(1), Pow(10, 12)), Sym("nanopa"));
            c[enums::MEGABAR] = Mul(Rat(Int(1), Pow(10, 20)), Sym("nanopa"));
            c[enums::MEGAPASCAL] = Mul(Rat(Int(1), Pow(10, 15)), Sym("nanopa"));
            c[enums::MICROPASCAL] = Mul(Rat(Int(1), Int(1000)), Sym("nanopa"));
            c[enums::MILLIBAR] = Mul(Rat(Int(1), Pow(10, 11)), Sym("nanopa"));
            c[enums::MILLIPASCAL] = Mul(Rat(Int(1), Pow(10, 6)), Sym("nanopa"));
            c[enums::MILLITORR] = Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 9))), Sym("nanopa"));
            c[enums::MMHG] = Mul(Rat(Int(1), Int("133322387415")), Sym("nanopa"));
            c[enums::PETAPASCAL] = Mul(Rat(Int(1), Pow(10, 24)), Sym("nanopa"));
            c[enums::PICOPASCAL] = Mul(Int(1000), Sym("nanopa"));
            c[enums::PSI] = Mul(Rat(Int(25), Int("172368932329209")), Sym("nanopa"));
            c[enums::Pascal] = Mul(Rat(Int(1), Pow(10, 9)), Sym("nanopa"));
            c[enums::TERAPASCAL] = Mul(Rat(Int(1), Pow(10, 21)), Sym("nanopa"));
            c[enums::TORR] = Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 6))), Sym("nanopa"));
            c[enums::YOCTOPASCAL] = Mul(Pow(10, 15), Sym("nanopa"));
            c[enums::YOTTAPASCAL] = Mul(Rat(Int(1), Pow(10, 33)), Sym("nanopa"));
            c[enums::ZEPTOPASCAL] = Mul(Pow(10, 12), Sym("nanopa"));
            c[enums::ZETTAPASCAL] = Mul(Rat(Int(1), Pow(10, 30)), Sym("nanopa"));
            return c;
        }

        static std::map<UnitsPressure, ConversionPtr> createMapPETAPASCAL() {
            std::map<UnitsPressure, ConversionPtr> c;
            c[enums::ATMOSPHERE] = Mul(Rat(Mul(Int(4), Pow(10, 13)), Int(4053)), Sym("petapa"));
            c[enums::ATTOPASCAL] = Mul(Pow(10, 33), Sym("petapa"));
            c[enums::BAR] = Mul(Pow(10, 10), Sym("petapa"));
            c[enums::CENTIBAR] = Mul(Pow(10, 12), Sym("petapa"));
            c[enums::CENTIPASCAL] = Mul(Pow(10, 17), Sym("petapa"));
            c[enums::DECAPASCAL] = Mul(Pow(10, 14), Sym("petapa"));
            c[enums::DECIBAR] = Mul(Pow(10, 11), Sym("petapa"));
            c[enums::DECIPASCAL] = Mul(Pow(10, 16), Sym("petapa"));
            c[enums::EXAPASCAL] = Mul(Rat(Int(1), Int(1000)), Sym("petapa"));
            c[enums::FEMTOPASCAL] = Mul(Pow(10, 30), Sym("petapa"));
            c[enums::GIGAPASCAL] = Mul(Pow(10, 6), Sym("petapa"));
            c[enums::HECTOPASCAL] = Mul(Pow(10, 13), Sym("petapa"));
            c[enums::KILOBAR] = Mul(Pow(10, 7), Sym("petapa"));
            c[enums::KILOPASCAL] = Mul(Pow(10, 12), Sym("petapa"));
            c[enums::MEGABAR] = Mul(Pow(10, 4), Sym("petapa"));
            c[enums::MEGAPASCAL] = Mul(Pow(10, 9), Sym("petapa"));
            c[enums::MICROPASCAL] = Mul(Pow(10, 21), Sym("petapa"));
            c[enums::MILLIBAR] = Mul(Pow(10, 13), Sym("petapa"));
            c[enums::MILLIPASCAL] = Mul(Pow(10, 18), Sym("petapa"));
            c[enums::MILLITORR] = Mul(Rat(Mul(Int(304), Pow(10, 11)), Int(4053)), Sym("petapa"));
            c[enums::MMHG] = Mul(Rat(Mul(Int(2), Pow(10, 23)), Int("26664477483")), Sym("petapa"));
            c[enums::NANOPASCAL] = Mul(Pow(10, 24), Sym("petapa"));
            c[enums::PICOPASCAL] = Mul(Pow(10, 27), Sym("petapa"));
            c[enums::PSI] = Mul(Rat(Mul(Int(25), Pow(10, 24)), Int("172368932329209")), Sym("petapa"));
            c[enums::Pascal] = Mul(Pow(10, 15), Sym("petapa"));
            c[enums::TERAPASCAL] = Mul(Int(1000), Sym("petapa"));
            c[enums::TORR] = Mul(Rat(Mul(Int(304), Pow(10, 14)), Int(4053)), Sym("petapa"));
            c[enums::YOCTOPASCAL] = Mul(Pow(10, 39), Sym("petapa"));
            c[enums::YOTTAPASCAL] = Mul(Rat(Int(1), Pow(10, 9)), Sym("petapa"));
            c[enums::ZEPTOPASCAL] = Mul(Pow(10, 36), Sym("petapa"));
            c[enums::ZETTAPASCAL] = Mul(Rat(Int(1), Pow(10, 6)), Sym("petapa"));
            return c;
        }

        static std::map<UnitsPressure, ConversionPtr> createMapPICOPASCAL() {
            std::map<UnitsPressure, ConversionPtr> c;
            c[enums::ATMOSPHERE] = Mul(Rat(Int(1), Mul(Int(101325), Pow(10, 12))), Sym("picopa"));
            c[enums::ATTOPASCAL] = Mul(Pow(10, 6), Sym("picopa"));
            c[enums::BAR] = Mul(Rat(Int(1), Pow(10, 17)), Sym("picopa"));
            c[enums::CENTIBAR] = Mul(Rat(Int(1), Pow(10, 15)), Sym("picopa"));
            c[enums::CENTIPASCAL] = Mul(Rat(Int(1), Pow(10, 10)), Sym("picopa"));
            c[enums::DECAPASCAL] = Mul(Rat(Int(1), Pow(10, 13)), Sym("picopa"));
            c[enums::DECIBAR] = Mul(Rat(Int(1), Pow(10, 16)), Sym("picopa"));
            c[enums::DECIPASCAL] = Mul(Rat(Int(1), Pow(10, 11)), Sym("picopa"));
            c[enums::EXAPASCAL] = Mul(Rat(Int(1), Pow(10, 30)), Sym("picopa"));
            c[enums::FEMTOPASCAL] = Mul(Int(1000), Sym("picopa"));
            c[enums::GIGAPASCAL] = Mul(Rat(Int(1), Pow(10, 21)), Sym("picopa"));
            c[enums::HECTOPASCAL] = Mul(Rat(Int(1), Pow(10, 14)), Sym("picopa"));
            c[enums::KILOBAR] = Mul(Rat(Int(1), Pow(10, 20)), Sym("picopa"));
            c[enums::KILOPASCAL] = Mul(Rat(Int(1), Pow(10, 15)), Sym("picopa"));
            c[enums::MEGABAR] = Mul(Rat(Int(1), Pow(10, 23)), Sym("picopa"));
            c[enums::MEGAPASCAL] = Mul(Rat(Int(1), Pow(10, 18)), Sym("picopa"));
            c[enums::MICROPASCAL] = Mul(Rat(Int(1), Pow(10, 6)), Sym("picopa"));
            c[enums::MILLIBAR] = Mul(Rat(Int(1), Pow(10, 14)), Sym("picopa"));
            c[enums::MILLIPASCAL] = Mul(Rat(Int(1), Pow(10, 9)), Sym("picopa"));
            c[enums::MILLITORR] = Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 12))), Sym("picopa"));
            c[enums::MMHG] = Mul(Rat(Int(1), Int("133322387415000")), Sym("picopa"));
            c[enums::NANOPASCAL] = Mul(Rat(Int(1), Int(1000)), Sym("picopa"));
            c[enums::PETAPASCAL] = Mul(Rat(Int(1), Pow(10, 27)), Sym("picopa"));
            c[enums::PSI] = Mul(Rat(Int(1), Int("6894757293168360")), Sym("picopa"));
            c[enums::Pascal] = Mul(Rat(Int(1), Pow(10, 12)), Sym("picopa"));
            c[enums::TERAPASCAL] = Mul(Rat(Int(1), Pow(10, 24)), Sym("picopa"));
            c[enums::TORR] = Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 9))), Sym("picopa"));
            c[enums::YOCTOPASCAL] = Mul(Pow(10, 12), Sym("picopa"));
            c[enums::YOTTAPASCAL] = Mul(Rat(Int(1), Pow(10, 36)), Sym("picopa"));
            c[enums::ZEPTOPASCAL] = Mul(Pow(10, 9), Sym("picopa"));
            c[enums::ZETTAPASCAL] = Mul(Rat(Int(1), Pow(10, 33)), Sym("picopa"));
            return c;
        }

        static std::map<UnitsPressure, ConversionPtr> createMapPSI() {
            std::map<UnitsPressure, ConversionPtr> c;
            c[enums::ATMOSPHERE] = Mul(Rat(Int("8208044396629"), Mul(Int(120625), Pow(10, 9))), Sym("psi"));
            c[enums::ATTOPASCAL] = Mul(Mul(Int("689475729316836"), Pow(10, 7)), Sym("psi"));
            c[enums::BAR] = Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 14))), Sym("psi"));
            c[enums::CENTIBAR] = Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 12))), Sym("psi"));
            c[enums::CENTIPASCAL] = Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 7))), Sym("psi"));
            c[enums::DECAPASCAL] = Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 10))), Sym("psi"));
            c[enums::DECIBAR] = Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 13))), Sym("psi"));
            c[enums::DECIPASCAL] = Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 8))), Sym("psi"));
            c[enums::EXAPASCAL] = Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 27))), Sym("psi"));
            c[enums::FEMTOPASCAL] = Mul(Mul(Int("689475729316836"), Pow(10, 4)), Sym("psi"));
            c[enums::GIGAPASCAL] = Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 18))), Sym("psi"));
            c[enums::HECTOPASCAL] = Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 11))), Sym("psi"));
            c[enums::KILOBAR] = Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 17))), Sym("psi"));
            c[enums::KILOPASCAL] = Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 12))), Sym("psi"));
            c[enums::MEGABAR] = Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 20))), Sym("psi"));
            c[enums::MEGAPASCAL] = Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 15))), Sym("psi"));
            c[enums::MICROPASCAL] = Mul(Rat(Int("172368932329209"), Int(25000)), Sym("psi"));
            c[enums::MILLIBAR] = Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 11))), Sym("psi"));
            c[enums::MILLIPASCAL] = Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 6))), Sym("psi"));
            c[enums::MILLITORR] = Mul(Rat(Int("155952843535951"), Mul(Int(3015625), Pow(10, 9))), Sym("psi"));
            c[enums::MMHG] = Mul(Rat(Int("8208044396629"), Int("158717127875")), Sym("psi"));
            c[enums::NANOPASCAL] = Mul(Rat(Int("172368932329209"), Int(25)), Sym("psi"));
            c[enums::PETAPASCAL] = Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 24))), Sym("psi"));
            c[enums::PICOPASCAL] = Mul(Int("6894757293168360"), Sym("psi"));
            c[enums::Pascal] = Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 9))), Sym("psi"));
            c[enums::TERAPASCAL] = Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 21))), Sym("psi"));
            c[enums::TORR] = Mul(Rat(Int("155952843535951"), Mul(Int(3015625), Pow(10, 6))), Sym("psi"));
            c[enums::YOCTOPASCAL] = Mul(Mul(Int("689475729316836"), Pow(10, 13)), Sym("psi"));
            c[enums::YOTTAPASCAL] = Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 33))), Sym("psi"));
            c[enums::ZEPTOPASCAL] = Mul(Mul(Int("689475729316836"), Pow(10, 10)), Sym("psi"));
            c[enums::ZETTAPASCAL] = Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 30))), Sym("psi"));
            return c;
        }

        static std::map<UnitsPressure, ConversionPtr> createMapPascal() {
            std::map<UnitsPressure, ConversionPtr> c;
            c[enums::ATMOSPHERE] = Mul(Rat(Int(1), Int(101325)), Sym("pa"));
            c[enums::ATTOPASCAL] = Mul(Pow(10, 18), Sym("pa"));
            c[enums::BAR] = Mul(Rat(Int(1), Pow(10, 5)), Sym("pa"));
            c[enums::CENTIBAR] = Mul(Rat(Int(1), Int(1000)), Sym("pa"));
            c[enums::CENTIPASCAL] = Mul(Int(100), Sym("pa"));
            c[enums::DECAPASCAL] = Mul(Rat(Int(1), Int(10)), Sym("pa"));
            c[enums::DECIBAR] = Mul(Rat(Int(1), Pow(10, 4)), Sym("pa"));
            c[enums::DECIPASCAL] = Mul(Int(10), Sym("pa"));
            c[enums::EXAPASCAL] = Mul(Rat(Int(1), Pow(10, 18)), Sym("pa"));
            c[enums::FEMTOPASCAL] = Mul(Pow(10, 15), Sym("pa"));
            c[enums::GIGAPASCAL] = Mul(Rat(Int(1), Pow(10, 9)), Sym("pa"));
            c[enums::HECTOPASCAL] = Mul(Rat(Int(1), Int(100)), Sym("pa"));
            c[enums::KILOBAR] = Mul(Rat(Int(1), Pow(10, 8)), Sym("pa"));
            c[enums::KILOPASCAL] = Mul(Rat(Int(1), Int(1000)), Sym("pa"));
            c[enums::MEGABAR] = Mul(Rat(Int(1), Pow(10, 11)), Sym("pa"));
            c[enums::MEGAPASCAL] = Mul(Rat(Int(1), Pow(10, 6)), Sym("pa"));
            c[enums::MICROPASCAL] = Mul(Pow(10, 6), Sym("pa"));
            c[enums::MILLIBAR] = Mul(Rat(Int(1), Int(100)), Sym("pa"));
            c[enums::MILLIPASCAL] = Mul(Int(1000), Sym("pa"));
            c[enums::MILLITORR] = Mul(Rat(Int(19), Int(2533125)), Sym("pa"));
            c[enums::MMHG] = Mul(Rat(Mul(Int(2), Pow(10, 8)), Int("26664477483")), Sym("pa"));
            c[enums::NANOPASCAL] = Mul(Pow(10, 9), Sym("pa"));
            c[enums::PETAPASCAL] = Mul(Rat(Int(1), Pow(10, 15)), Sym("pa"));
            c[enums::PICOPASCAL] = Mul(Pow(10, 12), Sym("pa"));
            c[enums::PSI] = Mul(Rat(Mul(Int(25), Pow(10, 9)), Int("172368932329209")), Sym("pa"));
            c[enums::TERAPASCAL] = Mul(Rat(Int(1), Pow(10, 12)), Sym("pa"));
            c[enums::TORR] = Mul(Rat(Int(152), Int(20265)), Sym("pa"));
            c[enums::YOCTOPASCAL] = Mul(Pow(10, 24), Sym("pa"));
            c[enums::YOTTAPASCAL] = Mul(Rat(Int(1), Pow(10, 24)), Sym("pa"));
            c[enums::ZEPTOPASCAL] = Mul(Pow(10, 21), Sym("pa"));
            c[enums::ZETTAPASCAL] = Mul(Rat(Int(1), Pow(10, 21)), Sym("pa"));
            return c;
        }

        static std::map<UnitsPressure, ConversionPtr> createMapTERAPASCAL() {
            std::map<UnitsPressure, ConversionPtr> c;
            c[enums::ATMOSPHERE] = Mul(Rat(Mul(Int(4), Pow(10, 10)), Int(4053)), Sym("terapa"));
            c[enums::ATTOPASCAL] = Mul(Pow(10, 30), Sym("terapa"));
            c[enums::BAR] = Mul(Pow(10, 7), Sym("terapa"));
            c[enums::CENTIBAR] = Mul(Pow(10, 9), Sym("terapa"));
            c[enums::CENTIPASCAL] = Mul(Pow(10, 14), Sym("terapa"));
            c[enums::DECAPASCAL] = Mul(Pow(10, 11), Sym("terapa"));
            c[enums::DECIBAR] = Mul(Pow(10, 8), Sym("terapa"));
            c[enums::DECIPASCAL] = Mul(Pow(10, 13), Sym("terapa"));
            c[enums::EXAPASCAL] = Mul(Rat(Int(1), Pow(10, 6)), Sym("terapa"));
            c[enums::FEMTOPASCAL] = Mul(Pow(10, 27), Sym("terapa"));
            c[enums::GIGAPASCAL] = Mul(Int(1000), Sym("terapa"));
            c[enums::HECTOPASCAL] = Mul(Pow(10, 10), Sym("terapa"));
            c[enums::KILOBAR] = Mul(Pow(10, 4), Sym("terapa"));
            c[enums::KILOPASCAL] = Mul(Pow(10, 9), Sym("terapa"));
            c[enums::MEGABAR] = Mul(Int(10), Sym("terapa"));
            c[enums::MEGAPASCAL] = Mul(Pow(10, 6), Sym("terapa"));
            c[enums::MICROPASCAL] = Mul(Pow(10, 18), Sym("terapa"));
            c[enums::MILLIBAR] = Mul(Pow(10, 10), Sym("terapa"));
            c[enums::MILLIPASCAL] = Mul(Pow(10, 15), Sym("terapa"));
            c[enums::MILLITORR] = Mul(Rat(Mul(Int(304), Pow(10, 8)), Int(4053)), Sym("terapa"));
            c[enums::MMHG] = Mul(Rat(Mul(Int(2), Pow(10, 20)), Int("26664477483")), Sym("terapa"));
            c[enums::NANOPASCAL] = Mul(Pow(10, 21), Sym("terapa"));
            c[enums::PETAPASCAL] = Mul(Rat(Int(1), Int(1000)), Sym("terapa"));
            c[enums::PICOPASCAL] = Mul(Pow(10, 24), Sym("terapa"));
            c[enums::PSI] = Mul(Rat(Mul(Int(25), Pow(10, 21)), Int("172368932329209")), Sym("terapa"));
            c[enums::Pascal] = Mul(Pow(10, 12), Sym("terapa"));
            c[enums::TORR] = Mul(Rat(Mul(Int(304), Pow(10, 11)), Int(4053)), Sym("terapa"));
            c[enums::YOCTOPASCAL] = Mul(Pow(10, 36), Sym("terapa"));
            c[enums::YOTTAPASCAL] = Mul(Rat(Int(1), Pow(10, 12)), Sym("terapa"));
            c[enums::ZEPTOPASCAL] = Mul(Pow(10, 33), Sym("terapa"));
            c[enums::ZETTAPASCAL] = Mul(Rat(Int(1), Pow(10, 9)), Sym("terapa"));
            return c;
        }

        static std::map<UnitsPressure, ConversionPtr> createMapTORR() {
            std::map<UnitsPressure, ConversionPtr> c;
            c[enums::ATMOSPHERE] = Mul(Rat(Int(1), Int(760)), Sym("torr"));
            c[enums::ATTOPASCAL] = Mul(Rat(Mul(Int(2533125), Pow(10, 15)), Int(19)), Sym("torr"));
            c[enums::BAR] = Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 4))), Sym("torr"));
            c[enums::CENTIBAR] = Mul(Rat(Int(4053), Int(30400)), Sym("torr"));
            c[enums::CENTIPASCAL] = Mul(Rat(Int(506625), Int(38)), Sym("torr"));
            c[enums::DECAPASCAL] = Mul(Rat(Int(4053), Int(304)), Sym("torr"));
            c[enums::DECIBAR] = Mul(Rat(Int(4053), Int(304000)), Sym("torr"));
            c[enums::DECIPASCAL] = Mul(Rat(Int(101325), Int(76)), Sym("torr"));
            c[enums::EXAPASCAL] = Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 17))), Sym("torr"));
            c[enums::FEMTOPASCAL] = Mul(Rat(Mul(Int(2533125), Pow(10, 12)), Int(19)), Sym("torr"));
            c[enums::GIGAPASCAL] = Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 8))), Sym("torr"));
            c[enums::HECTOPASCAL] = Mul(Rat(Int(4053), Int(3040)), Sym("torr"));
            c[enums::KILOBAR] = Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 7))), Sym("torr"));
            c[enums::KILOPASCAL] = Mul(Rat(Int(4053), Int(30400)), Sym("torr"));
            c[enums::MEGABAR] = Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 10))), Sym("torr"));
            c[enums::MEGAPASCAL] = Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 5))), Sym("torr"));
            c[enums::MICROPASCAL] = Mul(Rat(Int("2533125000"), Int(19)), Sym("torr"));
            c[enums::MILLIBAR] = Mul(Rat(Int(4053), Int(3040)), Sym("torr"));
            c[enums::MILLIPASCAL] = Mul(Rat(Int(2533125), Int(19)), Sym("torr"));
            c[enums::MILLITORR] = Mul(Rat(Int(1), Int(1000)), Sym("torr"));
            c[enums::MMHG] = Mul(Rat(Mul(Int(24125), Pow(10, 6)), Int("24125003437")), Sym("torr"));
            c[enums::NANOPASCAL] = Mul(Rat(Mul(Int(2533125), Pow(10, 6)), Int(19)), Sym("torr"));
            c[enums::PETAPASCAL] = Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 14))), Sym("torr"));
            c[enums::PICOPASCAL] = Mul(Rat(Mul(Int(2533125), Pow(10, 9)), Int(19)), Sym("torr"));
            c[enums::PSI] = Mul(Rat(Mul(Int(3015625), Pow(10, 6)), Int("155952843535951")), Sym("torr"));
            c[enums::Pascal] = Mul(Rat(Int(20265), Int(152)), Sym("torr"));
            c[enums::TERAPASCAL] = Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 11))), Sym("torr"));
            c[enums::YOCTOPASCAL] = Mul(Rat(Mul(Int(2533125), Pow(10, 21)), Int(19)), Sym("torr"));
            c[enums::YOTTAPASCAL] = Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 23))), Sym("torr"));
            c[enums::ZEPTOPASCAL] = Mul(Rat(Mul(Int(2533125), Pow(10, 18)), Int(19)), Sym("torr"));
            c[enums::ZETTAPASCAL] = Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 20))), Sym("torr"));
            return c;
        }

        static std::map<UnitsPressure, ConversionPtr> createMapYOCTOPASCAL() {
            std::map<UnitsPressure, ConversionPtr> c;
            c[enums::ATMOSPHERE] = Mul(Rat(Int(1), Mul(Int(101325), Pow(10, 24))), Sym("yoctopa"));
            c[enums::ATTOPASCAL] = Mul(Rat(Int(1), Pow(10, 6)), Sym("yoctopa"));
            c[enums::BAR] = Mul(Rat(Int(1), Pow(10, 29)), Sym("yoctopa"));
            c[enums::CENTIBAR] = Mul(Rat(Int(1), Pow(10, 27)), Sym("yoctopa"));
            c[enums::CENTIPASCAL] = Mul(Rat(Int(1), Pow(10, 22)), Sym("yoctopa"));
            c[enums::DECAPASCAL] = Mul(Rat(Int(1), Pow(10, 25)), Sym("yoctopa"));
            c[enums::DECIBAR] = Mul(Rat(Int(1), Pow(10, 28)), Sym("yoctopa"));
            c[enums::DECIPASCAL] = Mul(Rat(Int(1), Pow(10, 23)), Sym("yoctopa"));
            c[enums::EXAPASCAL] = Mul(Rat(Int(1), Pow(10, 42)), Sym("yoctopa"));
            c[enums::FEMTOPASCAL] = Mul(Rat(Int(1), Pow(10, 9)), Sym("yoctopa"));
            c[enums::GIGAPASCAL] = Mul(Rat(Int(1), Pow(10, 33)), Sym("yoctopa"));
            c[enums::HECTOPASCAL] = Mul(Rat(Int(1), Pow(10, 26)), Sym("yoctopa"));
            c[enums::KILOBAR] = Mul(Rat(Int(1), Pow(10, 32)), Sym("yoctopa"));
            c[enums::KILOPASCAL] = Mul(Rat(Int(1), Pow(10, 27)), Sym("yoctopa"));
            c[enums::MEGABAR] = Mul(Rat(Int(1), Pow(10, 35)), Sym("yoctopa"));
            c[enums::MEGAPASCAL] = Mul(Rat(Int(1), Pow(10, 30)), Sym("yoctopa"));
            c[enums::MICROPASCAL] = Mul(Rat(Int(1), Pow(10, 18)), Sym("yoctopa"));
            c[enums::MILLIBAR] = Mul(Rat(Int(1), Pow(10, 26)), Sym("yoctopa"));
            c[enums::MILLIPASCAL] = Mul(Rat(Int(1), Pow(10, 21)), Sym("yoctopa"));
            c[enums::MILLITORR] = Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 24))), Sym("yoctopa"));
            c[enums::MMHG] = Mul(Rat(Int(1), Mul(Int("133322387415"), Pow(10, 15))), Sym("yoctopa"));
            c[enums::NANOPASCAL] = Mul(Rat(Int(1), Pow(10, 15)), Sym("yoctopa"));
            c[enums::PETAPASCAL] = Mul(Rat(Int(1), Pow(10, 39)), Sym("yoctopa"));
            c[enums::PICOPASCAL] = Mul(Rat(Int(1), Pow(10, 12)), Sym("yoctopa"));
            c[enums::PSI] = Mul(Rat(Int(1), Mul(Int("689475729316836"), Pow(10, 13))), Sym("yoctopa"));
            c[enums::Pascal] = Mul(Rat(Int(1), Pow(10, 24)), Sym("yoctopa"));
            c[enums::TERAPASCAL] = Mul(Rat(Int(1), Pow(10, 36)), Sym("yoctopa"));
            c[enums::TORR] = Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 21))), Sym("yoctopa"));
            c[enums::YOTTAPASCAL] = Mul(Rat(Int(1), Pow(10, 48)), Sym("yoctopa"));
            c[enums::ZEPTOPASCAL] = Mul(Rat(Int(1), Int(1000)), Sym("yoctopa"));
            c[enums::ZETTAPASCAL] = Mul(Rat(Int(1), Pow(10, 45)), Sym("yoctopa"));
            return c;
        }

        static std::map<UnitsPressure, ConversionPtr> createMapYOTTAPASCAL() {
            std::map<UnitsPressure, ConversionPtr> c;
            c[enums::ATMOSPHERE] = Mul(Rat(Mul(Int(4), Pow(10, 22)), Int(4053)), Sym("yottapa"));
            c[enums::ATTOPASCAL] = Mul(Pow(10, 42), Sym("yottapa"));
            c[enums::BAR] = Mul(Pow(10, 19), Sym("yottapa"));
            c[enums::CENTIBAR] = Mul(Pow(10, 21), Sym("yottapa"));
            c[enums::CENTIPASCAL] = Mul(Pow(10, 26), Sym("yottapa"));
            c[enums::DECAPASCAL] = Mul(Pow(10, 23), Sym("yottapa"));
            c[enums::DECIBAR] = Mul(Pow(10, 20), Sym("yottapa"));
            c[enums::DECIPASCAL] = Mul(Pow(10, 25), Sym("yottapa"));
            c[enums::EXAPASCAL] = Mul(Pow(10, 6), Sym("yottapa"));
            c[enums::FEMTOPASCAL] = Mul(Pow(10, 39), Sym("yottapa"));
            c[enums::GIGAPASCAL] = Mul(Pow(10, 15), Sym("yottapa"));
            c[enums::HECTOPASCAL] = Mul(Pow(10, 22), Sym("yottapa"));
            c[enums::KILOBAR] = Mul(Pow(10, 16), Sym("yottapa"));
            c[enums::KILOPASCAL] = Mul(Pow(10, 21), Sym("yottapa"));
            c[enums::MEGABAR] = Mul(Pow(10, 13), Sym("yottapa"));
            c[enums::MEGAPASCAL] = Mul(Pow(10, 18), Sym("yottapa"));
            c[enums::MICROPASCAL] = Mul(Pow(10, 30), Sym("yottapa"));
            c[enums::MILLIBAR] = Mul(Pow(10, 22), Sym("yottapa"));
            c[enums::MILLIPASCAL] = Mul(Pow(10, 27), Sym("yottapa"));
            c[enums::MILLITORR] = Mul(Rat(Mul(Int(304), Pow(10, 20)), Int(4053)), Sym("yottapa"));
            c[enums::MMHG] = Mul(Rat(Mul(Int(2), Pow(10, 32)), Int("26664477483")), Sym("yottapa"));
            c[enums::NANOPASCAL] = Mul(Pow(10, 33), Sym("yottapa"));
            c[enums::PETAPASCAL] = Mul(Pow(10, 9), Sym("yottapa"));
            c[enums::PICOPASCAL] = Mul(Pow(10, 36), Sym("yottapa"));
            c[enums::PSI] = Mul(Rat(Mul(Int(25), Pow(10, 33)), Int("172368932329209")), Sym("yottapa"));
            c[enums::Pascal] = Mul(Pow(10, 24), Sym("yottapa"));
            c[enums::TERAPASCAL] = Mul(Pow(10, 12), Sym("yottapa"));
            c[enums::TORR] = Mul(Rat(Mul(Int(304), Pow(10, 23)), Int(4053)), Sym("yottapa"));
            c[enums::YOCTOPASCAL] = Mul(Pow(10, 48), Sym("yottapa"));
            c[enums::ZEPTOPASCAL] = Mul(Pow(10, 45), Sym("yottapa"));
            c[enums::ZETTAPASCAL] = Mul(Int(1000), Sym("yottapa"));
            return c;
        }

        static std::map<UnitsPressure, ConversionPtr> createMapZEPTOPASCAL() {
            std::map<UnitsPressure, ConversionPtr> c;
            c[enums::ATMOSPHERE] = Mul(Rat(Int(1), Mul(Int(101325), Pow(10, 21))), Sym("zeptopa"));
            c[enums::ATTOPASCAL] = Mul(Rat(Int(1), Int(1000)), Sym("zeptopa"));
            c[enums::BAR] = Mul(Rat(Int(1), Pow(10, 26)), Sym("zeptopa"));
            c[enums::CENTIBAR] = Mul(Rat(Int(1), Pow(10, 24)), Sym("zeptopa"));
            c[enums::CENTIPASCAL] = Mul(Rat(Int(1), Pow(10, 19)), Sym("zeptopa"));
            c[enums::DECAPASCAL] = Mul(Rat(Int(1), Pow(10, 22)), Sym("zeptopa"));
            c[enums::DECIBAR] = Mul(Rat(Int(1), Pow(10, 25)), Sym("zeptopa"));
            c[enums::DECIPASCAL] = Mul(Rat(Int(1), Pow(10, 20)), Sym("zeptopa"));
            c[enums::EXAPASCAL] = Mul(Rat(Int(1), Pow(10, 39)), Sym("zeptopa"));
            c[enums::FEMTOPASCAL] = Mul(Rat(Int(1), Pow(10, 6)), Sym("zeptopa"));
            c[enums::GIGAPASCAL] = Mul(Rat(Int(1), Pow(10, 30)), Sym("zeptopa"));
            c[enums::HECTOPASCAL] = Mul(Rat(Int(1), Pow(10, 23)), Sym("zeptopa"));
            c[enums::KILOBAR] = Mul(Rat(Int(1), Pow(10, 29)), Sym("zeptopa"));
            c[enums::KILOPASCAL] = Mul(Rat(Int(1), Pow(10, 24)), Sym("zeptopa"));
            c[enums::MEGABAR] = Mul(Rat(Int(1), Pow(10, 32)), Sym("zeptopa"));
            c[enums::MEGAPASCAL] = Mul(Rat(Int(1), Pow(10, 27)), Sym("zeptopa"));
            c[enums::MICROPASCAL] = Mul(Rat(Int(1), Pow(10, 15)), Sym("zeptopa"));
            c[enums::MILLIBAR] = Mul(Rat(Int(1), Pow(10, 23)), Sym("zeptopa"));
            c[enums::MILLIPASCAL] = Mul(Rat(Int(1), Pow(10, 18)), Sym("zeptopa"));
            c[enums::MILLITORR] = Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 21))), Sym("zeptopa"));
            c[enums::MMHG] = Mul(Rat(Int(1), Mul(Int("133322387415"), Pow(10, 12))), Sym("zeptopa"));
            c[enums::NANOPASCAL] = Mul(Rat(Int(1), Pow(10, 12)), Sym("zeptopa"));
            c[enums::PETAPASCAL] = Mul(Rat(Int(1), Pow(10, 36)), Sym("zeptopa"));
            c[enums::PICOPASCAL] = Mul(Rat(Int(1), Pow(10, 9)), Sym("zeptopa"));
            c[enums::PSI] = Mul(Rat(Int(1), Mul(Int("689475729316836"), Pow(10, 10))), Sym("zeptopa"));
            c[enums::Pascal] = Mul(Rat(Int(1), Pow(10, 21)), Sym("zeptopa"));
            c[enums::TERAPASCAL] = Mul(Rat(Int(1), Pow(10, 33)), Sym("zeptopa"));
            c[enums::TORR] = Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 18))), Sym("zeptopa"));
            c[enums::YOCTOPASCAL] = Mul(Int(1000), Sym("zeptopa"));
            c[enums::YOTTAPASCAL] = Mul(Rat(Int(1), Pow(10, 45)), Sym("zeptopa"));
            c[enums::ZETTAPASCAL] = Mul(Rat(Int(1), Pow(10, 42)), Sym("zeptopa"));
            return c;
        }

        static std::map<UnitsPressure, ConversionPtr> createMapZETTAPASCAL() {
            std::map<UnitsPressure, ConversionPtr> c;
            c[enums::ATMOSPHERE] = Mul(Rat(Mul(Int(4), Pow(10, 19)), Int(4053)), Sym("zettapa"));
            c[enums::ATTOPASCAL] = Mul(Pow(10, 39), Sym("zettapa"));
            c[enums::BAR] = Mul(Pow(10, 16), Sym("zettapa"));
            c[enums::CENTIBAR] = Mul(Pow(10, 18), Sym("zettapa"));
            c[enums::CENTIPASCAL] = Mul(Pow(10, 23), Sym("zettapa"));
            c[enums::DECAPASCAL] = Mul(Pow(10, 20), Sym("zettapa"));
            c[enums::DECIBAR] = Mul(Pow(10, 17), Sym("zettapa"));
            c[enums::DECIPASCAL] = Mul(Pow(10, 22), Sym("zettapa"));
            c[enums::EXAPASCAL] = Mul(Int(1000), Sym("zettapa"));
            c[enums::FEMTOPASCAL] = Mul(Pow(10, 36), Sym("zettapa"));
            c[enums::GIGAPASCAL] = Mul(Pow(10, 12), Sym("zettapa"));
            c[enums::HECTOPASCAL] = Mul(Pow(10, 19), Sym("zettapa"));
            c[enums::KILOBAR] = Mul(Pow(10, 13), Sym("zettapa"));
            c[enums::KILOPASCAL] = Mul(Pow(10, 18), Sym("zettapa"));
            c[enums::MEGABAR] = Mul(Pow(10, 10), Sym("zettapa"));
            c[enums::MEGAPASCAL] = Mul(Pow(10, 15), Sym("zettapa"));
            c[enums::MICROPASCAL] = Mul(Pow(10, 27), Sym("zettapa"));
            c[enums::MILLIBAR] = Mul(Pow(10, 19), Sym("zettapa"));
            c[enums::MILLIPASCAL] = Mul(Pow(10, 24), Sym("zettapa"));
            c[enums::MILLITORR] = Mul(Rat(Mul(Int(304), Pow(10, 17)), Int(4053)), Sym("zettapa"));
            c[enums::MMHG] = Mul(Rat(Mul(Int(2), Pow(10, 29)), Int("26664477483")), Sym("zettapa"));
            c[enums::NANOPASCAL] = Mul(Pow(10, 30), Sym("zettapa"));
            c[enums::PETAPASCAL] = Mul(Pow(10, 6), Sym("zettapa"));
            c[enums::PICOPASCAL] = Mul(Pow(10, 33), Sym("zettapa"));
            c[enums::PSI] = Mul(Rat(Mul(Int(25), Pow(10, 30)), Int("172368932329209")), Sym("zettapa"));
            c[enums::Pascal] = Mul(Pow(10, 21), Sym("zettapa"));
            c[enums::TERAPASCAL] = Mul(Pow(10, 9), Sym("zettapa"));
            c[enums::TORR] = Mul(Rat(Mul(Int(304), Pow(10, 20)), Int(4053)), Sym("zettapa"));
            c[enums::YOCTOPASCAL] = Mul(Pow(10, 45), Sym("zettapa"));
            c[enums::YOTTAPASCAL] = Mul(Rat(Int(1), Int(1000)), Sym("zettapa"));
            c[enums::ZEPTOPASCAL] = Mul(Pow(10, 42), Sym("zettapa"));
            return c;
        }

        static std::map<UnitsPressure,
            std::map<UnitsPressure, ConversionPtr> > makeConversions() {
            std::map<UnitsPressure, std::map<UnitsPressure, ConversionPtr> > c;
            c[enums::ATMOSPHERE] = createMapATMOSPHERE();
            c[enums::ATTOPASCAL] = createMapATTOPASCAL();
            c[enums::BAR] = createMapBAR();
            c[enums::CENTIBAR] = createMapCENTIBAR();
            c[enums::CENTIPASCAL] = createMapCENTIPASCAL();
            c[enums::DECAPASCAL] = createMapDECAPASCAL();
            c[enums::DECIBAR] = createMapDECIBAR();
            c[enums::DECIPASCAL] = createMapDECIPASCAL();
            c[enums::EXAPASCAL] = createMapEXAPASCAL();
            c[enums::FEMTOPASCAL] = createMapFEMTOPASCAL();
            c[enums::GIGAPASCAL] = createMapGIGAPASCAL();
            c[enums::HECTOPASCAL] = createMapHECTOPASCAL();
            c[enums::KILOBAR] = createMapKILOBAR();
            c[enums::KILOPASCAL] = createMapKILOPASCAL();
            c[enums::MEGABAR] = createMapMEGABAR();
            c[enums::MEGAPASCAL] = createMapMEGAPASCAL();
            c[enums::MICROPASCAL] = createMapMICROPASCAL();
            c[enums::MILLIBAR] = createMapMILLIBAR();
            c[enums::MILLIPASCAL] = createMapMILLIPASCAL();
            c[enums::MILLITORR] = createMapMILLITORR();
            c[enums::MMHG] = createMapMMHG();
            c[enums::NANOPASCAL] = createMapNANOPASCAL();
            c[enums::PETAPASCAL] = createMapPETAPASCAL();
            c[enums::PICOPASCAL] = createMapPICOPASCAL();
            c[enums::PSI] = createMapPSI();
            c[enums::Pascal] = createMapPascal();
            c[enums::TERAPASCAL] = createMapTERAPASCAL();
            c[enums::TORR] = createMapTORR();
            c[enums::YOCTOPASCAL] = createMapYOCTOPASCAL();
            c[enums::YOTTAPASCAL] = createMapYOTTAPASCAL();
            c[enums::ZEPTOPASCAL] = createMapZEPTOPASCAL();
            c[enums::ZETTAPASCAL] = createMapZETTAPASCAL();
            return c;
        }

        static std::map<UnitsPressure, std::string> makeSymbols(){
            std::map<UnitsPressure, std::string> s;
            s[enums::ATMOSPHERE] = "atm";
            s[enums::ATTOPASCAL] = "aPa";
            s[enums::BAR] = "bar";
            s[enums::CENTIBAR] = "cbar";
            s[enums::CENTIPASCAL] = "cPa";
            s[enums::DECAPASCAL] = "daPa";
            s[enums::DECIBAR] = "dbar";
            s[enums::DECIPASCAL] = "dPa";
            s[enums::EXAPASCAL] = "EPa";
            s[enums::FEMTOPASCAL] = "fPa";
            s[enums::GIGAPASCAL] = "GPa";
            s[enums::HECTOPASCAL] = "hPa";
            s[enums::KILOBAR] = "kbar";
            s[enums::KILOPASCAL] = "kPa";
            s[enums::MEGABAR] = "Mbar";
            s[enums::MEGAPASCAL] = "MPa";
            s[enums::MICROPASCAL] = "µPa";
            s[enums::MILLIBAR] = "mbar";
            s[enums::MILLIPASCAL] = "mPa";
            s[enums::MILLITORR] = "mTorr";
            s[enums::MMHG] = "mm Hg";
            s[enums::NANOPASCAL] = "nPa";
            s[enums::PETAPASCAL] = "PPa";
            s[enums::PICOPASCAL] = "pPa";
            s[enums::PSI] = "psi";
            s[enums::Pascal] = "Pa";
            s[enums::TERAPASCAL] = "TPa";
            s[enums::TORR] = "Torr";
            s[enums::YOCTOPASCAL] = "yPa";
            s[enums::YOTTAPASCAL] = "YPa";
            s[enums::ZEPTOPASCAL] = "zPa";
            s[enums::ZETTAPASCAL] = "ZPa";
            return s;
        }

        std::map<UnitsPressure,
            std::map<UnitsPressure, ConversionPtr> > PressureI::CONVERSIONS = makeConversions();

        std::map<UnitsPressure, std::string> PressureI::SYMBOLS = makeSymbols();

        PressureI::~PressureI() {}

        PressureI::PressureI() : Pressure() {
        }

        PressureI::PressureI(const double& value, const UnitsPressure& unit) : Pressure() {
            setValue(value);
            setUnit(unit);
        }

        PressureI::PressureI(const PressurePtr& value, const UnitsPressure& target) : Pressure() {
            double orig = value->getValue();
            UnitsPressure source = value->getUnit();
            if (target == source) {
                // No conversion needed
                setValue(orig);
                setUnit(target);
            } else {
                ConversionPtr conversion = CONVERSIONS[source][target];
                if (!conversion) {
                    std::stringstream ss;
                    ss << orig << " " << source;
                    ss << "cannot be converted to " << target;
                    throw omero::ClientError(__FILE__, __LINE__, ss.str().c_str());
                }
                double converted = conversion->convert(orig);
                setValue(converted);
                setUnit(target);
            }
        }

        Ice::Double PressureI::getValue(const Ice::Current& /* current */) {
            return value;
        }

        void PressureI::setValue(Ice::Double _value, const Ice::Current& /* current */) {
            value = _value;
        }

        UnitsPressure PressureI::getUnit(const Ice::Current& /* current */) {
            return unit;
        }

        void PressureI::setUnit(UnitsPressure _unit, const Ice::Current& /* current */) {
            unit = _unit;
        }

        std::string PressureI::getSymbol(const Ice::Current& /* current */) {
            return SYMBOLS[unit];
        }

        PressurePtr PressureI::copy(const Ice::Current& /* current */) {
            PressurePtr copy = new PressureI();
            copy->setValue(getValue());
            copy->setUnit(getUnit());
            return copy;
        }
    }
}

