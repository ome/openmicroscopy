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

#include <omero/model/LengthI.h>
#include <omero/ClientErrors.h>

::Ice::Object* IceInternal::upCast(::omero::model::LengthI* t) { return t; }

using namespace omero::conversions;

typedef omero::conversion_types::ConversionPtr ConversionPtr;
typedef omero::model::enums::UnitsLength UnitsLength;

namespace omero {

    namespace model {

        static std::map<UnitsLength, ConversionPtr> createMapANGSTROM() {
            std::map<UnitsLength, ConversionPtr> c;
            c[enums::ASTRONOMICALUNIT] = Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 12))), Sym("ang"));
            c[enums::ATTOMETER] = Mul(Pow(10, 8), Sym("ang"));
            c[enums::CENTIMETER] = Mul(Rat(Int(1), Pow(10, 8)), Sym("ang"));
            c[enums::DECAMETER] = Mul(Rat(Int(1), Pow(10, 11)), Sym("ang"));
            c[enums::DECIMETER] = Mul(Rat(Int(1), Pow(10, 9)), Sym("ang"));
            c[enums::EXAMETER] = Mul(Rat(Int(1), Pow(10, 28)), Sym("ang"));
            c[enums::FEMTOMETER] = Mul(Pow(10, 5), Sym("ang"));
            c[enums::FOOT] = Mul(Rat(Int(393701), Mul(Int(12), Pow(10, 14))), Sym("ang"));
            c[enums::GIGAMETER] = Mul(Rat(Int(1), Pow(10, 19)), Sym("ang"));
            c[enums::HECTOMETER] = Mul(Rat(Int(1), Pow(10, 12)), Sym("ang"));
            c[enums::INCH] = Mul(Rat(Int(393701), Pow(10, 14)), Sym("ang"));
            c[enums::KILOMETER] = Mul(Rat(Int(1), Pow(10, 13)), Sym("ang"));
            c[enums::LIGHTYEAR] = Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 12))), Sym("ang"));
            c[enums::LINE] = Mul(Rat(Int(1181103), Mul(Int(25), Pow(10, 12))), Sym("ang"));
            c[enums::MEGAMETER] = Mul(Rat(Int(1), Pow(10, 16)), Sym("ang"));
            c[enums::METER] = Mul(Rat(Int(1), Pow(10, 10)), Sym("ang"));
            c[enums::MICROMETER] = Mul(Rat(Int(1), Pow(10, 4)), Sym("ang"));
            c[enums::MILE] = Mul(Rat(Int(35791), Mul(Int(576), Pow(10, 15))), Sym("ang"));
            c[enums::MILLIMETER] = Mul(Rat(Int(1), Pow(10, 7)), Sym("ang"));
            c[enums::NANOMETER] = Mul(Rat(Int(1), Int(10)), Sym("ang"));
            c[enums::PARSEC] = Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 19))), Sym("ang"));
            c[enums::PETAMETER] = Mul(Rat(Int(1), Pow(10, 25)), Sym("ang"));
            c[enums::PICOMETER] = Mul(Int(100), Sym("ang"));
            c[enums::POINT] = Mul(Rat(Int(3543309), Mul(Int(125), Pow(10, 11))), Sym("ang"));
            c[enums::TERAMETER] = Mul(Rat(Int(1), Pow(10, 22)), Sym("ang"));
            c[enums::THOU] = Mul(Rat(Int(393701), Pow(10, 17)), Sym("ang"));
            c[enums::YARD] = Mul(Rat(Int(393701), Mul(Int(36), Pow(10, 14))), Sym("ang"));
            c[enums::YOCTOMETER] = Mul(Pow(10, 14), Sym("ang"));
            c[enums::YOTTAMETER] = Mul(Rat(Int(1), Pow(10, 34)), Sym("ang"));
            c[enums::ZEPTOMETER] = Mul(Pow(10, 11), Sym("ang"));
            c[enums::ZETTAMETER] = Mul(Rat(Int(1), Pow(10, 31)), Sym("ang"));
            return c;
        }

        static std::map<UnitsLength, ConversionPtr> createMapASTRONOMICALUNIT() {
            std::map<UnitsLength, ConversionPtr> c;
            c[enums::ANGSTROM] = Mul(Mul(Int(1495978707), Pow(10, 12)), Sym("ua"));
            c[enums::ATTOMETER] = Mul(Mul(Int(1495978707), Pow(10, 20)), Sym("ua"));
            c[enums::CENTIMETER] = Mul(Mul(Int(1495978707), Pow(10, 4)), Sym("ua"));
            c[enums::DECAMETER] = Mul(Int("14959787070"), Sym("ua"));
            c[enums::DECIMETER] = Mul(Int("1495978707000"), Sym("ua"));
            c[enums::EXAMETER] = Mul(Rat(Int(1495978707), Pow(10, 16)), Sym("ua"));
            c[enums::FEMTOMETER] = Mul(Mul(Int(1495978707), Pow(10, 17)), Sym("ua"));
            c[enums::FOOT] = Mul(Rat(Int("196322770974869"), Int(400)), Sym("ua"));
            c[enums::GIGAMETER] = Mul(Rat(Int(1495978707), Pow(10, 7)), Sym("ua"));
            c[enums::HECTOMETER] = Mul(Int(1495978707), Sym("ua"));
            c[enums::INCH] = Mul(Rat(Int("588968312924607"), Int(100)), Sym("ua"));
            c[enums::KILOMETER] = Mul(Rat(Int(1495978707), Int(10)), Sym("ua"));
            c[enums::LIGHTYEAR] = Mul(Rat(Int(6830953), Int("431996825232")), Sym("ua"));
            c[enums::LINE] = Mul(Rat(Int("1766904938773821"), Int(25)), Sym("ua"));
            c[enums::MEGAMETER] = Mul(Rat(Int(1495978707), Pow(10, 4)), Sym("ua"));
            c[enums::METER] = Mul(Int("149597870700"), Sym("ua"));
            c[enums::MICROMETER] = Mul(Mul(Int(1495978707), Pow(10, 8)), Sym("ua"));
            c[enums::MILE] = Mul(Rat(Int("17847524634079"), Int(192000)), Sym("ua"));
            c[enums::MILLIMETER] = Mul(Mul(Int(1495978707), Pow(10, 5)), Sym("ua"));
            c[enums::NANOMETER] = Mul(Mul(Int(1495978707), Pow(10, 11)), Sym("ua"));
            c[enums::PARSEC] = Mul(Rat(Int(498659569), Mul(Int(10285592), Pow(10, 7))), Sym("ua"));
            c[enums::PETAMETER] = Mul(Rat(Int(1495978707), Pow(10, 13)), Sym("ua"));
            c[enums::PICOMETER] = Mul(Mul(Int(1495978707), Pow(10, 14)), Sym("ua"));
            c[enums::POINT] = Mul(Rat(Int("10601429632642926"), Int(25)), Sym("ua"));
            c[enums::TERAMETER] = Mul(Rat(Int(1495978707), Pow(10, 10)), Sym("ua"));
            c[enums::THOU] = Mul(Rat(Int("588968312924607"), Pow(10, 5)), Sym("ua"));
            c[enums::YARD] = Mul(Rat(Int("196322770974869"), Int(1200)), Sym("ua"));
            c[enums::YOCTOMETER] = Mul(Mul(Int(1495978707), Pow(10, 26)), Sym("ua"));
            c[enums::YOTTAMETER] = Mul(Rat(Int(1495978707), Pow(10, 22)), Sym("ua"));
            c[enums::ZEPTOMETER] = Mul(Mul(Int(1495978707), Pow(10, 23)), Sym("ua"));
            c[enums::ZETTAMETER] = Mul(Rat(Int(1495978707), Pow(10, 19)), Sym("ua"));
            return c;
        }

        static std::map<UnitsLength, ConversionPtr> createMapATTOMETER() {
            std::map<UnitsLength, ConversionPtr> c;
            c[enums::ANGSTROM] = Mul(Rat(Int(1), Pow(10, 8)), Sym("attom"));
            c[enums::ASTRONOMICALUNIT] = Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 20))), Sym("attom"));
            c[enums::CENTIMETER] = Mul(Rat(Int(1), Pow(10, 16)), Sym("attom"));
            c[enums::DECAMETER] = Mul(Rat(Int(1), Pow(10, 19)), Sym("attom"));
            c[enums::DECIMETER] = Mul(Rat(Int(1), Pow(10, 17)), Sym("attom"));
            c[enums::EXAMETER] = Mul(Rat(Int(1), Pow(10, 36)), Sym("attom"));
            c[enums::FEMTOMETER] = Mul(Rat(Int(1), Int(1000)), Sym("attom"));
            c[enums::FOOT] = Mul(Rat(Int(393701), Mul(Int(12), Pow(10, 22))), Sym("attom"));
            c[enums::GIGAMETER] = Mul(Rat(Int(1), Pow(10, 27)), Sym("attom"));
            c[enums::HECTOMETER] = Mul(Rat(Int(1), Pow(10, 20)), Sym("attom"));
            c[enums::INCH] = Mul(Rat(Int(393701), Pow(10, 22)), Sym("attom"));
            c[enums::KILOMETER] = Mul(Rat(Int(1), Pow(10, 21)), Sym("attom"));
            c[enums::LIGHTYEAR] = Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 20))), Sym("attom"));
            c[enums::LINE] = Mul(Rat(Int(1181103), Mul(Int(25), Pow(10, 20))), Sym("attom"));
            c[enums::MEGAMETER] = Mul(Rat(Int(1), Pow(10, 24)), Sym("attom"));
            c[enums::METER] = Mul(Rat(Int(1), Pow(10, 18)), Sym("attom"));
            c[enums::MICROMETER] = Mul(Rat(Int(1), Pow(10, 12)), Sym("attom"));
            c[enums::MILE] = Mul(Rat(Int(35791), Mul(Int(576), Pow(10, 23))), Sym("attom"));
            c[enums::MILLIMETER] = Mul(Rat(Int(1), Pow(10, 15)), Sym("attom"));
            c[enums::NANOMETER] = Mul(Rat(Int(1), Pow(10, 9)), Sym("attom"));
            c[enums::PARSEC] = Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 27))), Sym("attom"));
            c[enums::PETAMETER] = Mul(Rat(Int(1), Pow(10, 33)), Sym("attom"));
            c[enums::PICOMETER] = Mul(Rat(Int(1), Pow(10, 6)), Sym("attom"));
            c[enums::POINT] = Mul(Rat(Int(3543309), Mul(Int(125), Pow(10, 19))), Sym("attom"));
            c[enums::TERAMETER] = Mul(Rat(Int(1), Pow(10, 30)), Sym("attom"));
            c[enums::THOU] = Mul(Rat(Int(393701), Pow(10, 25)), Sym("attom"));
            c[enums::YARD] = Mul(Rat(Int(393701), Mul(Int(36), Pow(10, 22))), Sym("attom"));
            c[enums::YOCTOMETER] = Mul(Pow(10, 6), Sym("attom"));
            c[enums::YOTTAMETER] = Mul(Rat(Int(1), Pow(10, 42)), Sym("attom"));
            c[enums::ZEPTOMETER] = Mul(Int(1000), Sym("attom"));
            c[enums::ZETTAMETER] = Mul(Rat(Int(1), Pow(10, 39)), Sym("attom"));
            return c;
        }

        static std::map<UnitsLength, ConversionPtr> createMapCENTIMETER() {
            std::map<UnitsLength, ConversionPtr> c;
            c[enums::ANGSTROM] = Mul(Pow(10, 8), Sym("centim"));
            c[enums::ASTRONOMICALUNIT] = Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 4))), Sym("centim"));
            c[enums::ATTOMETER] = Mul(Pow(10, 16), Sym("centim"));
            c[enums::DECAMETER] = Mul(Rat(Int(1), Int(1000)), Sym("centim"));
            c[enums::DECIMETER] = Mul(Rat(Int(1), Int(10)), Sym("centim"));
            c[enums::EXAMETER] = Mul(Rat(Int(1), Pow(10, 20)), Sym("centim"));
            c[enums::FEMTOMETER] = Mul(Pow(10, 13), Sym("centim"));
            c[enums::FOOT] = Mul(Rat(Int(393701), Mul(Int(12), Pow(10, 6))), Sym("centim"));
            c[enums::GIGAMETER] = Mul(Rat(Int(1), Pow(10, 11)), Sym("centim"));
            c[enums::HECTOMETER] = Mul(Rat(Int(1), Pow(10, 4)), Sym("centim"));
            c[enums::INCH] = Mul(Rat(Int(393701), Pow(10, 6)), Sym("centim"));
            c[enums::KILOMETER] = Mul(Rat(Int(1), Pow(10, 5)), Sym("centim"));
            c[enums::LIGHTYEAR] = Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 4))), Sym("centim"));
            c[enums::LINE] = Mul(Rat(Int(1181103), Mul(Int(25), Pow(10, 4))), Sym("centim"));
            c[enums::MEGAMETER] = Mul(Rat(Int(1), Pow(10, 8)), Sym("centim"));
            c[enums::METER] = Mul(Rat(Int(1), Int(100)), Sym("centim"));
            c[enums::MICROMETER] = Mul(Pow(10, 4), Sym("centim"));
            c[enums::MILE] = Mul(Rat(Int(35791), Mul(Int(576), Pow(10, 7))), Sym("centim"));
            c[enums::MILLIMETER] = Mul(Int(10), Sym("centim"));
            c[enums::NANOMETER] = Mul(Pow(10, 7), Sym("centim"));
            c[enums::PARSEC] = Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 11))), Sym("centim"));
            c[enums::PETAMETER] = Mul(Rat(Int(1), Pow(10, 17)), Sym("centim"));
            c[enums::PICOMETER] = Mul(Pow(10, 10), Sym("centim"));
            c[enums::POINT] = Mul(Rat(Int(3543309), Int(125000)), Sym("centim"));
            c[enums::TERAMETER] = Mul(Rat(Int(1), Pow(10, 14)), Sym("centim"));
            c[enums::THOU] = Mul(Rat(Int(393701), Pow(10, 9)), Sym("centim"));
            c[enums::YARD] = Mul(Rat(Int(393701), Mul(Int(36), Pow(10, 6))), Sym("centim"));
            c[enums::YOCTOMETER] = Mul(Pow(10, 22), Sym("centim"));
            c[enums::YOTTAMETER] = Mul(Rat(Int(1), Pow(10, 26)), Sym("centim"));
            c[enums::ZEPTOMETER] = Mul(Pow(10, 19), Sym("centim"));
            c[enums::ZETTAMETER] = Mul(Rat(Int(1), Pow(10, 23)), Sym("centim"));
            return c;
        }

        static std::map<UnitsLength, ConversionPtr> createMapDECAMETER() {
            std::map<UnitsLength, ConversionPtr> c;
            c[enums::ANGSTROM] = Mul(Pow(10, 11), Sym("decam"));
            c[enums::ASTRONOMICALUNIT] = Mul(Rat(Int(1), Int("14959787070")), Sym("decam"));
            c[enums::ATTOMETER] = Mul(Pow(10, 19), Sym("decam"));
            c[enums::CENTIMETER] = Mul(Int(1000), Sym("decam"));
            c[enums::DECIMETER] = Mul(Int(100), Sym("decam"));
            c[enums::EXAMETER] = Mul(Rat(Int(1), Pow(10, 17)), Sym("decam"));
            c[enums::FEMTOMETER] = Mul(Pow(10, 16), Sym("decam"));
            c[enums::FOOT] = Mul(Rat(Int(393701), Int(12000)), Sym("decam"));
            c[enums::GIGAMETER] = Mul(Rat(Int(1), Pow(10, 8)), Sym("decam"));
            c[enums::HECTOMETER] = Mul(Rat(Int(1), Int(10)), Sym("decam"));
            c[enums::INCH] = Mul(Rat(Int(393701), Int(1000)), Sym("decam"));
            c[enums::KILOMETER] = Mul(Rat(Int(1), Int(100)), Sym("decam"));
            c[enums::LIGHTYEAR] = Mul(Rat(Int(1), Int("946073047258080")), Sym("decam"));
            c[enums::LINE] = Mul(Rat(Int(1181103), Int(250)), Sym("decam"));
            c[enums::MEGAMETER] = Mul(Rat(Int(1), Pow(10, 5)), Sym("decam"));
            c[enums::METER] = Mul(Int(10), Sym("decam"));
            c[enums::MICROMETER] = Mul(Pow(10, 7), Sym("decam"));
            c[enums::MILE] = Mul(Rat(Int(35791), Mul(Int(576), Pow(10, 4))), Sym("decam"));
            c[enums::MILLIMETER] = Mul(Pow(10, 4), Sym("decam"));
            c[enums::NANOMETER] = Mul(Pow(10, 10), Sym("decam"));
            c[enums::PARSEC] = Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 8))), Sym("decam"));
            c[enums::PETAMETER] = Mul(Rat(Int(1), Pow(10, 14)), Sym("decam"));
            c[enums::PICOMETER] = Mul(Pow(10, 13), Sym("decam"));
            c[enums::POINT] = Mul(Rat(Int(3543309), Int(125)), Sym("decam"));
            c[enums::TERAMETER] = Mul(Rat(Int(1), Pow(10, 11)), Sym("decam"));
            c[enums::THOU] = Mul(Rat(Int(393701), Pow(10, 6)), Sym("decam"));
            c[enums::YARD] = Mul(Rat(Int(393701), Int(36000)), Sym("decam"));
            c[enums::YOCTOMETER] = Mul(Pow(10, 25), Sym("decam"));
            c[enums::YOTTAMETER] = Mul(Rat(Int(1), Pow(10, 23)), Sym("decam"));
            c[enums::ZEPTOMETER] = Mul(Pow(10, 22), Sym("decam"));
            c[enums::ZETTAMETER] = Mul(Rat(Int(1), Pow(10, 20)), Sym("decam"));
            return c;
        }

        static std::map<UnitsLength, ConversionPtr> createMapDECIMETER() {
            std::map<UnitsLength, ConversionPtr> c;
            c[enums::ANGSTROM] = Mul(Pow(10, 9), Sym("decim"));
            c[enums::ASTRONOMICALUNIT] = Mul(Rat(Int(1), Int("1495978707000")), Sym("decim"));
            c[enums::ATTOMETER] = Mul(Pow(10, 17), Sym("decim"));
            c[enums::CENTIMETER] = Mul(Int(10), Sym("decim"));
            c[enums::DECAMETER] = Mul(Rat(Int(1), Int(100)), Sym("decim"));
            c[enums::EXAMETER] = Mul(Rat(Int(1), Pow(10, 19)), Sym("decim"));
            c[enums::FEMTOMETER] = Mul(Pow(10, 14), Sym("decim"));
            c[enums::FOOT] = Mul(Rat(Int(393701), Mul(Int(12), Pow(10, 5))), Sym("decim"));
            c[enums::GIGAMETER] = Mul(Rat(Int(1), Pow(10, 10)), Sym("decim"));
            c[enums::HECTOMETER] = Mul(Rat(Int(1), Int(1000)), Sym("decim"));
            c[enums::INCH] = Mul(Rat(Int(393701), Pow(10, 5)), Sym("decim"));
            c[enums::KILOMETER] = Mul(Rat(Int(1), Pow(10, 4)), Sym("decim"));
            c[enums::LIGHTYEAR] = Mul(Rat(Int(1), Int("94607304725808000")), Sym("decim"));
            c[enums::LINE] = Mul(Rat(Int(1181103), Int(25000)), Sym("decim"));
            c[enums::MEGAMETER] = Mul(Rat(Int(1), Pow(10, 7)), Sym("decim"));
            c[enums::METER] = Mul(Rat(Int(1), Int(10)), Sym("decim"));
            c[enums::MICROMETER] = Mul(Pow(10, 5), Sym("decim"));
            c[enums::MILE] = Mul(Rat(Int(35791), Mul(Int(576), Pow(10, 6))), Sym("decim"));
            c[enums::MILLIMETER] = Mul(Int(100), Sym("decim"));
            c[enums::NANOMETER] = Mul(Pow(10, 8), Sym("decim"));
            c[enums::PARSEC] = Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 10))), Sym("decim"));
            c[enums::PETAMETER] = Mul(Rat(Int(1), Pow(10, 16)), Sym("decim"));
            c[enums::PICOMETER] = Mul(Pow(10, 11), Sym("decim"));
            c[enums::POINT] = Mul(Rat(Int(3543309), Int(12500)), Sym("decim"));
            c[enums::TERAMETER] = Mul(Rat(Int(1), Pow(10, 13)), Sym("decim"));
            c[enums::THOU] = Mul(Rat(Int(393701), Pow(10, 8)), Sym("decim"));
            c[enums::YARD] = Mul(Rat(Int(393701), Mul(Int(36), Pow(10, 5))), Sym("decim"));
            c[enums::YOCTOMETER] = Mul(Pow(10, 23), Sym("decim"));
            c[enums::YOTTAMETER] = Mul(Rat(Int(1), Pow(10, 25)), Sym("decim"));
            c[enums::ZEPTOMETER] = Mul(Pow(10, 20), Sym("decim"));
            c[enums::ZETTAMETER] = Mul(Rat(Int(1), Pow(10, 22)), Sym("decim"));
            return c;
        }

        static std::map<UnitsLength, ConversionPtr> createMapEXAMETER() {
            std::map<UnitsLength, ConversionPtr> c;
            c[enums::ANGSTROM] = Mul(Pow(10, 28), Sym("exam"));
            c[enums::ASTRONOMICALUNIT] = Mul(Rat(Pow(10, 16), Int(1495978707)), Sym("exam"));
            c[enums::ATTOMETER] = Mul(Pow(10, 36), Sym("exam"));
            c[enums::CENTIMETER] = Mul(Pow(10, 20), Sym("exam"));
            c[enums::DECAMETER] = Mul(Pow(10, 17), Sym("exam"));
            c[enums::DECIMETER] = Mul(Pow(10, 19), Sym("exam"));
            c[enums::FEMTOMETER] = Mul(Pow(10, 33), Sym("exam"));
            c[enums::FOOT] = Mul(Rat(Mul(Int(9842525), Pow(10, 12)), Int(3)), Sym("exam"));
            c[enums::GIGAMETER] = Mul(Pow(10, 9), Sym("exam"));
            c[enums::HECTOMETER] = Mul(Pow(10, 16), Sym("exam"));
            c[enums::INCH] = Mul(Mul(Int(393701), Pow(10, 14)), Sym("exam"));
            c[enums::KILOMETER] = Mul(Pow(10, 15), Sym("exam"));
            c[enums::LIGHTYEAR] = Mul(Rat(Mul(Int(625), Pow(10, 12)), Int("5912956545363")), Sym("exam"));
            c[enums::LINE] = Mul(Mul(Int(4724412), Pow(10, 14)), Sym("exam"));
            c[enums::MEGAMETER] = Mul(Pow(10, 12), Sym("exam"));
            c[enums::METER] = Mul(Pow(10, 18), Sym("exam"));
            c[enums::MICROMETER] = Mul(Pow(10, 24), Sym("exam"));
            c[enums::MILE] = Mul(Rat(Mul(Int(559234375), Pow(10, 7)), Int(9)), Sym("exam"));
            c[enums::MILLIMETER] = Mul(Pow(10, 21), Sym("exam"));
            c[enums::NANOMETER] = Mul(Pow(10, 27), Sym("exam"));
            c[enums::PARSEC] = Mul(Rat(Mul(Int(125), Pow(10, 6)), Int(3857097)), Sym("exam"));
            c[enums::PETAMETER] = Mul(Int(1000), Sym("exam"));
            c[enums::PICOMETER] = Mul(Pow(10, 30), Sym("exam"));
            c[enums::POINT] = Mul(Mul(Int(28346472), Pow(10, 14)), Sym("exam"));
            c[enums::TERAMETER] = Mul(Pow(10, 6), Sym("exam"));
            c[enums::THOU] = Mul(Mul(Int(393701), Pow(10, 11)), Sym("exam"));
            c[enums::YARD] = Mul(Rat(Mul(Int(9842525), Pow(10, 12)), Int(9)), Sym("exam"));
            c[enums::YOCTOMETER] = Mul(Pow(10, 42), Sym("exam"));
            c[enums::YOTTAMETER] = Mul(Rat(Int(1), Pow(10, 6)), Sym("exam"));
            c[enums::ZEPTOMETER] = Mul(Pow(10, 39), Sym("exam"));
            c[enums::ZETTAMETER] = Mul(Rat(Int(1), Int(1000)), Sym("exam"));
            return c;
        }

        static std::map<UnitsLength, ConversionPtr> createMapFEMTOMETER() {
            std::map<UnitsLength, ConversionPtr> c;
            c[enums::ANGSTROM] = Mul(Rat(Int(1), Pow(10, 5)), Sym("femtom"));
            c[enums::ASTRONOMICALUNIT] = Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 17))), Sym("femtom"));
            c[enums::ATTOMETER] = Mul(Int(1000), Sym("femtom"));
            c[enums::CENTIMETER] = Mul(Rat(Int(1), Pow(10, 13)), Sym("femtom"));
            c[enums::DECAMETER] = Mul(Rat(Int(1), Pow(10, 16)), Sym("femtom"));
            c[enums::DECIMETER] = Mul(Rat(Int(1), Pow(10, 14)), Sym("femtom"));
            c[enums::EXAMETER] = Mul(Rat(Int(1), Pow(10, 33)), Sym("femtom"));
            c[enums::FOOT] = Mul(Rat(Int(393701), Mul(Int(12), Pow(10, 19))), Sym("femtom"));
            c[enums::GIGAMETER] = Mul(Rat(Int(1), Pow(10, 24)), Sym("femtom"));
            c[enums::HECTOMETER] = Mul(Rat(Int(1), Pow(10, 17)), Sym("femtom"));
            c[enums::INCH] = Mul(Rat(Int(393701), Pow(10, 19)), Sym("femtom"));
            c[enums::KILOMETER] = Mul(Rat(Int(1), Pow(10, 18)), Sym("femtom"));
            c[enums::LIGHTYEAR] = Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 17))), Sym("femtom"));
            c[enums::LINE] = Mul(Rat(Int(1181103), Mul(Int(25), Pow(10, 17))), Sym("femtom"));
            c[enums::MEGAMETER] = Mul(Rat(Int(1), Pow(10, 21)), Sym("femtom"));
            c[enums::METER] = Mul(Rat(Int(1), Pow(10, 15)), Sym("femtom"));
            c[enums::MICROMETER] = Mul(Rat(Int(1), Pow(10, 9)), Sym("femtom"));
            c[enums::MILE] = Mul(Rat(Int(35791), Mul(Int(576), Pow(10, 20))), Sym("femtom"));
            c[enums::MILLIMETER] = Mul(Rat(Int(1), Pow(10, 12)), Sym("femtom"));
            c[enums::NANOMETER] = Mul(Rat(Int(1), Pow(10, 6)), Sym("femtom"));
            c[enums::PARSEC] = Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 24))), Sym("femtom"));
            c[enums::PETAMETER] = Mul(Rat(Int(1), Pow(10, 30)), Sym("femtom"));
            c[enums::PICOMETER] = Mul(Rat(Int(1), Int(1000)), Sym("femtom"));
            c[enums::POINT] = Mul(Rat(Int(3543309), Mul(Int(125), Pow(10, 16))), Sym("femtom"));
            c[enums::TERAMETER] = Mul(Rat(Int(1), Pow(10, 27)), Sym("femtom"));
            c[enums::THOU] = Mul(Rat(Int(393701), Pow(10, 22)), Sym("femtom"));
            c[enums::YARD] = Mul(Rat(Int(393701), Mul(Int(36), Pow(10, 19))), Sym("femtom"));
            c[enums::YOCTOMETER] = Mul(Pow(10, 9), Sym("femtom"));
            c[enums::YOTTAMETER] = Mul(Rat(Int(1), Pow(10, 39)), Sym("femtom"));
            c[enums::ZEPTOMETER] = Mul(Pow(10, 6), Sym("femtom"));
            c[enums::ZETTAMETER] = Mul(Rat(Int(1), Pow(10, 36)), Sym("femtom"));
            return c;
        }

        static std::map<UnitsLength, ConversionPtr> createMapFOOT() {
            std::map<UnitsLength, ConversionPtr> c;
            c[enums::ANGSTROM] = Mul(Rat(Mul(Int(12), Pow(10, 14)), Int(393701)), Sym("ft"));
            c[enums::ASTRONOMICALUNIT] = Mul(Rat(Int(400), Int("196322770974869")), Sym("ft"));
            c[enums::ATTOMETER] = Mul(Rat(Mul(Int(12), Pow(10, 22)), Int(393701)), Sym("ft"));
            c[enums::CENTIMETER] = Mul(Rat(Mul(Int(12), Pow(10, 6)), Int(393701)), Sym("ft"));
            c[enums::DECAMETER] = Mul(Rat(Int(12000), Int(393701)), Sym("ft"));
            c[enums::DECIMETER] = Mul(Rat(Mul(Int(12), Pow(10, 5)), Int(393701)), Sym("ft"));
            c[enums::EXAMETER] = Mul(Rat(Int(3), Mul(Int(9842525), Pow(10, 12))), Sym("ft"));
            c[enums::FEMTOMETER] = Mul(Rat(Mul(Int(12), Pow(10, 19)), Int(393701)), Sym("ft"));
            c[enums::GIGAMETER] = Mul(Rat(Int(3), Int("9842525000")), Sym("ft"));
            c[enums::HECTOMETER] = Mul(Rat(Int(1200), Int(393701)), Sym("ft"));
            c[enums::INCH] = Mul(Int(12), Sym("ft"));
            c[enums::KILOMETER] = Mul(Rat(Int(120), Int(393701)), Sym("ft"));
            c[enums::LIGHTYEAR] = Mul(Rat(Int(25), Int("775978968288652821")), Sym("ft"));
            c[enums::LINE] = Mul(Int(144), Sym("ft"));
            c[enums::MEGAMETER] = Mul(Rat(Int(3), Int(9842525)), Sym("ft"));
            c[enums::METER] = Mul(Rat(Mul(Int(12), Pow(10, 4)), Int(393701)), Sym("ft"));
            c[enums::MICROMETER] = Mul(Rat(Mul(Int(12), Pow(10, 10)), Int(393701)), Sym("ft"));
            c[enums::MILE] = Mul(Rat(Int(1), Int(5280)), Sym("ft"));
            c[enums::MILLIMETER] = Mul(Rat(Mul(Int(12), Pow(10, 7)), Int(393701)), Sym("ft"));
            c[enums::NANOMETER] = Mul(Rat(Mul(Int(12), Pow(10, 13)), Int(393701)), Sym("ft"));
            c[enums::PARSEC] = Mul(Rat(Int(1), Mul(Int("1012361963998"), Pow(10, 5))), Sym("ft"));
            c[enums::PETAMETER] = Mul(Rat(Int(3), Mul(Int(9842525), Pow(10, 9))), Sym("ft"));
            c[enums::PICOMETER] = Mul(Rat(Mul(Int(12), Pow(10, 16)), Int(393701)), Sym("ft"));
            c[enums::POINT] = Mul(Int(864), Sym("ft"));
            c[enums::TERAMETER] = Mul(Rat(Int(3), Mul(Int(9842525), Pow(10, 6))), Sym("ft"));
            c[enums::THOU] = Mul(Rat(Int(3), Int(250)), Sym("ft"));
            c[enums::YARD] = Mul(Rat(Int(1), Int(3)), Sym("ft"));
            c[enums::YOCTOMETER] = Mul(Rat(Mul(Int(12), Pow(10, 28)), Int(393701)), Sym("ft"));
            c[enums::YOTTAMETER] = Mul(Rat(Int(3), Mul(Int(9842525), Pow(10, 18))), Sym("ft"));
            c[enums::ZEPTOMETER] = Mul(Rat(Mul(Int(12), Pow(10, 25)), Int(393701)), Sym("ft"));
            c[enums::ZETTAMETER] = Mul(Rat(Int(3), Mul(Int(9842525), Pow(10, 15))), Sym("ft"));
            return c;
        }

        static std::map<UnitsLength, ConversionPtr> createMapGIGAMETER() {
            std::map<UnitsLength, ConversionPtr> c;
            c[enums::ANGSTROM] = Mul(Pow(10, 19), Sym("gigam"));
            c[enums::ASTRONOMICALUNIT] = Mul(Rat(Pow(10, 7), Int(1495978707)), Sym("gigam"));
            c[enums::ATTOMETER] = Mul(Pow(10, 27), Sym("gigam"));
            c[enums::CENTIMETER] = Mul(Pow(10, 11), Sym("gigam"));
            c[enums::DECAMETER] = Mul(Pow(10, 8), Sym("gigam"));
            c[enums::DECIMETER] = Mul(Pow(10, 10), Sym("gigam"));
            c[enums::EXAMETER] = Mul(Rat(Int(1), Pow(10, 9)), Sym("gigam"));
            c[enums::FEMTOMETER] = Mul(Pow(10, 24), Sym("gigam"));
            c[enums::FOOT] = Mul(Rat(Int("9842525000"), Int(3)), Sym("gigam"));
            c[enums::HECTOMETER] = Mul(Pow(10, 7), Sym("gigam"));
            c[enums::INCH] = Mul(Mul(Int(393701), Pow(10, 5)), Sym("gigam"));
            c[enums::KILOMETER] = Mul(Pow(10, 6), Sym("gigam"));
            c[enums::LIGHTYEAR] = Mul(Rat(Int(625000), Int("5912956545363")), Sym("gigam"));
            c[enums::LINE] = Mul(Mul(Int(4724412), Pow(10, 5)), Sym("gigam"));
            c[enums::MEGAMETER] = Mul(Int(1000), Sym("gigam"));
            c[enums::METER] = Mul(Pow(10, 9), Sym("gigam"));
            c[enums::MICROMETER] = Mul(Pow(10, 15), Sym("gigam"));
            c[enums::MILE] = Mul(Rat(Int(22369375), Int(36)), Sym("gigam"));
            c[enums::MILLIMETER] = Mul(Pow(10, 12), Sym("gigam"));
            c[enums::NANOMETER] = Mul(Pow(10, 18), Sym("gigam"));
            c[enums::PARSEC] = Mul(Rat(Int(1), Int(30856776)), Sym("gigam"));
            c[enums::PETAMETER] = Mul(Rat(Int(1), Pow(10, 6)), Sym("gigam"));
            c[enums::PICOMETER] = Mul(Pow(10, 21), Sym("gigam"));
            c[enums::POINT] = Mul(Mul(Int(28346472), Pow(10, 5)), Sym("gigam"));
            c[enums::TERAMETER] = Mul(Rat(Int(1), Int(1000)), Sym("gigam"));
            c[enums::THOU] = Mul(Int(39370100), Sym("gigam"));
            c[enums::YARD] = Mul(Rat(Int("9842525000"), Int(9)), Sym("gigam"));
            c[enums::YOCTOMETER] = Mul(Pow(10, 33), Sym("gigam"));
            c[enums::YOTTAMETER] = Mul(Rat(Int(1), Pow(10, 15)), Sym("gigam"));
            c[enums::ZEPTOMETER] = Mul(Pow(10, 30), Sym("gigam"));
            c[enums::ZETTAMETER] = Mul(Rat(Int(1), Pow(10, 12)), Sym("gigam"));
            return c;
        }

        static std::map<UnitsLength, ConversionPtr> createMapHECTOMETER() {
            std::map<UnitsLength, ConversionPtr> c;
            c[enums::ANGSTROM] = Mul(Pow(10, 12), Sym("hectom"));
            c[enums::ASTRONOMICALUNIT] = Mul(Rat(Int(1), Int(1495978707)), Sym("hectom"));
            c[enums::ATTOMETER] = Mul(Pow(10, 20), Sym("hectom"));
            c[enums::CENTIMETER] = Mul(Pow(10, 4), Sym("hectom"));
            c[enums::DECAMETER] = Mul(Int(10), Sym("hectom"));
            c[enums::DECIMETER] = Mul(Int(1000), Sym("hectom"));
            c[enums::EXAMETER] = Mul(Rat(Int(1), Pow(10, 16)), Sym("hectom"));
            c[enums::FEMTOMETER] = Mul(Pow(10, 17), Sym("hectom"));
            c[enums::FOOT] = Mul(Rat(Int(393701), Int(1200)), Sym("hectom"));
            c[enums::GIGAMETER] = Mul(Rat(Int(1), Pow(10, 7)), Sym("hectom"));
            c[enums::INCH] = Mul(Rat(Int(393701), Int(100)), Sym("hectom"));
            c[enums::KILOMETER] = Mul(Rat(Int(1), Int(10)), Sym("hectom"));
            c[enums::LIGHTYEAR] = Mul(Rat(Int(1), Int("94607304725808")), Sym("hectom"));
            c[enums::LINE] = Mul(Rat(Int(1181103), Int(25)), Sym("hectom"));
            c[enums::MEGAMETER] = Mul(Rat(Int(1), Pow(10, 4)), Sym("hectom"));
            c[enums::METER] = Mul(Int(100), Sym("hectom"));
            c[enums::MICROMETER] = Mul(Pow(10, 8), Sym("hectom"));
            c[enums::MILE] = Mul(Rat(Int(35791), Int(576000)), Sym("hectom"));
            c[enums::MILLIMETER] = Mul(Pow(10, 5), Sym("hectom"));
            c[enums::NANOMETER] = Mul(Pow(10, 11), Sym("hectom"));
            c[enums::PARSEC] = Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 7))), Sym("hectom"));
            c[enums::PETAMETER] = Mul(Rat(Int(1), Pow(10, 13)), Sym("hectom"));
            c[enums::PICOMETER] = Mul(Pow(10, 14), Sym("hectom"));
            c[enums::POINT] = Mul(Rat(Int(7086618), Int(25)), Sym("hectom"));
            c[enums::TERAMETER] = Mul(Rat(Int(1), Pow(10, 10)), Sym("hectom"));
            c[enums::THOU] = Mul(Rat(Int(393701), Pow(10, 5)), Sym("hectom"));
            c[enums::YARD] = Mul(Rat(Int(393701), Int(3600)), Sym("hectom"));
            c[enums::YOCTOMETER] = Mul(Pow(10, 26), Sym("hectom"));
            c[enums::YOTTAMETER] = Mul(Rat(Int(1), Pow(10, 22)), Sym("hectom"));
            c[enums::ZEPTOMETER] = Mul(Pow(10, 23), Sym("hectom"));
            c[enums::ZETTAMETER] = Mul(Rat(Int(1), Pow(10, 19)), Sym("hectom"));
            return c;
        }

        static std::map<UnitsLength, ConversionPtr> createMapINCH() {
            std::map<UnitsLength, ConversionPtr> c;
            c[enums::ANGSTROM] = Mul(Rat(Pow(10, 14), Int(393701)), Sym("in"));
            c[enums::ASTRONOMICALUNIT] = Mul(Rat(Int(100), Int("588968312924607")), Sym("in"));
            c[enums::ATTOMETER] = Mul(Rat(Pow(10, 22), Int(393701)), Sym("in"));
            c[enums::CENTIMETER] = Mul(Rat(Pow(10, 6), Int(393701)), Sym("in"));
            c[enums::DECAMETER] = Mul(Rat(Int(1000), Int(393701)), Sym("in"));
            c[enums::DECIMETER] = Mul(Rat(Pow(10, 5), Int(393701)), Sym("in"));
            c[enums::EXAMETER] = Mul(Rat(Int(1), Mul(Int(393701), Pow(10, 14))), Sym("in"));
            c[enums::FEMTOMETER] = Mul(Rat(Pow(10, 19), Int(393701)), Sym("in"));
            c[enums::FOOT] = Mul(Rat(Int(1), Int(12)), Sym("in"));
            c[enums::GIGAMETER] = Mul(Rat(Int(1), Mul(Int(393701), Pow(10, 5))), Sym("in"));
            c[enums::HECTOMETER] = Mul(Rat(Int(100), Int(393701)), Sym("in"));
            c[enums::KILOMETER] = Mul(Rat(Int(10), Int(393701)), Sym("in"));
            c[enums::LIGHTYEAR] = Mul(Rat(Int(25), Int("9311747619463833852")), Sym("in"));
            c[enums::LINE] = Mul(Int(12), Sym("in"));
            c[enums::MEGAMETER] = Mul(Rat(Int(1), Int(39370100)), Sym("in"));
            c[enums::METER] = Mul(Rat(Pow(10, 4), Int(393701)), Sym("in"));
            c[enums::MICROMETER] = Mul(Rat(Pow(10, 10), Int(393701)), Sym("in"));
            c[enums::MILE] = Mul(Rat(Int(1), Int(63360)), Sym("in"));
            c[enums::MILLIMETER] = Mul(Rat(Pow(10, 7), Int(393701)), Sym("in"));
            c[enums::NANOMETER] = Mul(Rat(Pow(10, 13), Int(393701)), Sym("in"));
            c[enums::PARSEC] = Mul(Rat(Int(1), Mul(Int("12148343567976"), Pow(10, 5))), Sym("in"));
            c[enums::PETAMETER] = Mul(Rat(Int(1), Mul(Int(393701), Pow(10, 11))), Sym("in"));
            c[enums::PICOMETER] = Mul(Rat(Pow(10, 16), Int(393701)), Sym("in"));
            c[enums::POINT] = Mul(Int(72), Sym("in"));
            c[enums::TERAMETER] = Mul(Rat(Int(1), Mul(Int(393701), Pow(10, 8))), Sym("in"));
            c[enums::THOU] = Mul(Rat(Int(1), Int(1000)), Sym("in"));
            c[enums::YARD] = Mul(Rat(Int(1), Int(36)), Sym("in"));
            c[enums::YOCTOMETER] = Mul(Rat(Pow(10, 28), Int(393701)), Sym("in"));
            c[enums::YOTTAMETER] = Mul(Rat(Int(1), Mul(Int(393701), Pow(10, 20))), Sym("in"));
            c[enums::ZEPTOMETER] = Mul(Rat(Pow(10, 25), Int(393701)), Sym("in"));
            c[enums::ZETTAMETER] = Mul(Rat(Int(1), Mul(Int(393701), Pow(10, 17))), Sym("in"));
            return c;
        }

        static std::map<UnitsLength, ConversionPtr> createMapKILOMETER() {
            std::map<UnitsLength, ConversionPtr> c;
            c[enums::ANGSTROM] = Mul(Pow(10, 13), Sym("kilom"));
            c[enums::ASTRONOMICALUNIT] = Mul(Rat(Int(10), Int(1495978707)), Sym("kilom"));
            c[enums::ATTOMETER] = Mul(Pow(10, 21), Sym("kilom"));
            c[enums::CENTIMETER] = Mul(Pow(10, 5), Sym("kilom"));
            c[enums::DECAMETER] = Mul(Int(100), Sym("kilom"));
            c[enums::DECIMETER] = Mul(Pow(10, 4), Sym("kilom"));
            c[enums::EXAMETER] = Mul(Rat(Int(1), Pow(10, 15)), Sym("kilom"));
            c[enums::FEMTOMETER] = Mul(Pow(10, 18), Sym("kilom"));
            c[enums::FOOT] = Mul(Rat(Int(393701), Int(120)), Sym("kilom"));
            c[enums::GIGAMETER] = Mul(Rat(Int(1), Pow(10, 6)), Sym("kilom"));
            c[enums::HECTOMETER] = Mul(Int(10), Sym("kilom"));
            c[enums::INCH] = Mul(Rat(Int(393701), Int(10)), Sym("kilom"));
            c[enums::LIGHTYEAR] = Mul(Rat(Int(5), Int("47303652362904")), Sym("kilom"));
            c[enums::LINE] = Mul(Rat(Int(2362206), Int(5)), Sym("kilom"));
            c[enums::MEGAMETER] = Mul(Rat(Int(1), Int(1000)), Sym("kilom"));
            c[enums::METER] = Mul(Int(1000), Sym("kilom"));
            c[enums::MICROMETER] = Mul(Pow(10, 9), Sym("kilom"));
            c[enums::MILE] = Mul(Rat(Int(35791), Int(57600)), Sym("kilom"));
            c[enums::MILLIMETER] = Mul(Pow(10, 6), Sym("kilom"));
            c[enums::NANOMETER] = Mul(Pow(10, 12), Sym("kilom"));
            c[enums::PARSEC] = Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 6))), Sym("kilom"));
            c[enums::PETAMETER] = Mul(Rat(Int(1), Pow(10, 12)), Sym("kilom"));
            c[enums::PICOMETER] = Mul(Pow(10, 15), Sym("kilom"));
            c[enums::POINT] = Mul(Rat(Int(14173236), Int(5)), Sym("kilom"));
            c[enums::TERAMETER] = Mul(Rat(Int(1), Pow(10, 9)), Sym("kilom"));
            c[enums::THOU] = Mul(Rat(Int(393701), Pow(10, 4)), Sym("kilom"));
            c[enums::YARD] = Mul(Rat(Int(393701), Int(360)), Sym("kilom"));
            c[enums::YOCTOMETER] = Mul(Pow(10, 27), Sym("kilom"));
            c[enums::YOTTAMETER] = Mul(Rat(Int(1), Pow(10, 21)), Sym("kilom"));
            c[enums::ZEPTOMETER] = Mul(Pow(10, 24), Sym("kilom"));
            c[enums::ZETTAMETER] = Mul(Rat(Int(1), Pow(10, 18)), Sym("kilom"));
            return c;
        }

        static std::map<UnitsLength, ConversionPtr> createMapLIGHTYEAR() {
            std::map<UnitsLength, ConversionPtr> c;
            c[enums::ANGSTROM] = Mul(Mul(Int("94607304725808"), Pow(10, 12)), Sym("ly"));
            c[enums::ASTRONOMICALUNIT] = Mul(Rat(Int("431996825232"), Int(6830953)), Sym("ly"));
            c[enums::ATTOMETER] = Mul(Mul(Int("94607304725808"), Pow(10, 20)), Sym("ly"));
            c[enums::CENTIMETER] = Mul(Mul(Int("94607304725808"), Pow(10, 4)), Sym("ly"));
            c[enums::DECAMETER] = Mul(Int("946073047258080"), Sym("ly"));
            c[enums::DECIMETER] = Mul(Int("94607304725808000"), Sym("ly"));
            c[enums::EXAMETER] = Mul(Rat(Int("5912956545363"), Mul(Int(625), Pow(10, 12))), Sym("ly"));
            c[enums::FEMTOMETER] = Mul(Mul(Int("94607304725808"), Pow(10, 17)), Sym("ly"));
            c[enums::FOOT] = Mul(Rat(Int("775978968288652821"), Int(25)), Sym("ly"));
            c[enums::GIGAMETER] = Mul(Rat(Int("5912956545363"), Int(625000)), Sym("ly"));
            c[enums::HECTOMETER] = Mul(Int("94607304725808"), Sym("ly"));
            c[enums::INCH] = Mul(Rat(Int("9311747619463833852"), Int(25)), Sym("ly"));
            c[enums::KILOMETER] = Mul(Rat(Int("47303652362904"), Int(5)), Sym("ly"));
            c[enums::LINE] = Mul(Rat(Int("111740971433566006224"), Int(25)), Sym("ly"));
            c[enums::MEGAMETER] = Mul(Rat(Int("5912956545363"), Int(625)), Sym("ly"));
            c[enums::METER] = Mul(Int("9460730472580800"), Sym("ly"));
            c[enums::MICROMETER] = Mul(Mul(Int("94607304725808"), Pow(10, 8)), Sym("ly"));
            c[enums::MILE] = Mul(Rat(Int("23514514190565237"), Int(4000)), Sym("ly"));
            c[enums::MILLIMETER] = Mul(Mul(Int("94607304725808"), Pow(10, 5)), Sym("ly"));
            c[enums::NANOMETER] = Mul(Mul(Int("94607304725808"), Pow(10, 11)), Sym("ly"));
            c[enums::PARSEC] = Mul(Rat(Int("1970985515121"), Mul(Int(6428495), Pow(10, 6))), Sym("ly"));
            c[enums::PETAMETER] = Mul(Rat(Int("5912956545363"), Mul(Int(625), Pow(10, 9))), Sym("ly"));
            c[enums::PICOMETER] = Mul(Mul(Int("94607304725808"), Pow(10, 14)), Sym("ly"));
            c[enums::POINT] = Mul(Rat(Int("670445828601396037344"), Int(25)), Sym("ly"));
            c[enums::TERAMETER] = Mul(Rat(Int("5912956545363"), Mul(Int(625), Pow(10, 6))), Sym("ly"));
            c[enums::THOU] = Mul(Rat(Int("2327936904865958463"), Int(6250)), Sym("ly"));
            c[enums::YARD] = Mul(Rat(Int("258659656096217607"), Int(25)), Sym("ly"));
            c[enums::YOCTOMETER] = Mul(Mul(Int("94607304725808"), Pow(10, 26)), Sym("ly"));
            c[enums::YOTTAMETER] = Mul(Rat(Int("5912956545363"), Mul(Int(625), Pow(10, 18))), Sym("ly"));
            c[enums::ZEPTOMETER] = Mul(Mul(Int("94607304725808"), Pow(10, 23)), Sym("ly"));
            c[enums::ZETTAMETER] = Mul(Rat(Int("5912956545363"), Mul(Int(625), Pow(10, 15))), Sym("ly"));
            return c;
        }

        static std::map<UnitsLength, ConversionPtr> createMapLINE() {
            std::map<UnitsLength, ConversionPtr> c;
            c[enums::ANGSTROM] = Mul(Rat(Mul(Int(25), Pow(10, 12)), Int(1181103)), Sym("li"));
            c[enums::ASTRONOMICALUNIT] = Mul(Rat(Int(25), Int("1766904938773821")), Sym("li"));
            c[enums::ATTOMETER] = Mul(Rat(Mul(Int(25), Pow(10, 20)), Int(1181103)), Sym("li"));
            c[enums::CENTIMETER] = Mul(Rat(Mul(Int(25), Pow(10, 4)), Int(1181103)), Sym("li"));
            c[enums::DECAMETER] = Mul(Rat(Int(250), Int(1181103)), Sym("li"));
            c[enums::DECIMETER] = Mul(Rat(Int(25000), Int(1181103)), Sym("li"));
            c[enums::EXAMETER] = Mul(Rat(Int(1), Mul(Int(4724412), Pow(10, 14))), Sym("li"));
            c[enums::FEMTOMETER] = Mul(Rat(Mul(Int(25), Pow(10, 17)), Int(1181103)), Sym("li"));
            c[enums::FOOT] = Mul(Rat(Int(1), Int(144)), Sym("li"));
            c[enums::GIGAMETER] = Mul(Rat(Int(1), Mul(Int(4724412), Pow(10, 5))), Sym("li"));
            c[enums::HECTOMETER] = Mul(Rat(Int(25), Int(1181103)), Sym("li"));
            c[enums::INCH] = Mul(Rat(Int(1), Int(12)), Sym("li"));
            c[enums::KILOMETER] = Mul(Rat(Int(5), Int(2362206)), Sym("li"));
            c[enums::LIGHTYEAR] = Mul(Rat(Int(25), Int("111740971433566006224")), Sym("li"));
            c[enums::MEGAMETER] = Mul(Rat(Int(1), Int(472441200)), Sym("li"));
            c[enums::METER] = Mul(Rat(Int(2500), Int(1181103)), Sym("li"));
            c[enums::MICROMETER] = Mul(Rat(Mul(Int(25), Pow(10, 8)), Int(1181103)), Sym("li"));
            c[enums::MILE] = Mul(Rat(Int(1), Int(760320)), Sym("li"));
            c[enums::MILLIMETER] = Mul(Rat(Mul(Int(25), Pow(10, 5)), Int(1181103)), Sym("li"));
            c[enums::NANOMETER] = Mul(Rat(Mul(Int(25), Pow(10, 11)), Int(1181103)), Sym("li"));
            c[enums::PARSEC] = Mul(Rat(Int(1), Mul(Int("145780122815712"), Pow(10, 5))), Sym("li"));
            c[enums::PETAMETER] = Mul(Rat(Int(1), Mul(Int(4724412), Pow(10, 11))), Sym("li"));
            c[enums::PICOMETER] = Mul(Rat(Mul(Int(25), Pow(10, 14)), Int(1181103)), Sym("li"));
            c[enums::POINT] = Mul(Int(6), Sym("li"));
            c[enums::TERAMETER] = Mul(Rat(Int(1), Mul(Int(4724412), Pow(10, 8))), Sym("li"));
            c[enums::THOU] = Mul(Rat(Int(1), Int(12000)), Sym("li"));
            c[enums::YARD] = Mul(Rat(Int(1), Int(432)), Sym("li"));
            c[enums::YOCTOMETER] = Mul(Rat(Mul(Int(25), Pow(10, 26)), Int(1181103)), Sym("li"));
            c[enums::YOTTAMETER] = Mul(Rat(Int(1), Mul(Int(4724412), Pow(10, 20))), Sym("li"));
            c[enums::ZEPTOMETER] = Mul(Rat(Mul(Int(25), Pow(10, 23)), Int(1181103)), Sym("li"));
            c[enums::ZETTAMETER] = Mul(Rat(Int(1), Mul(Int(4724412), Pow(10, 17))), Sym("li"));
            return c;
        }

        static std::map<UnitsLength, ConversionPtr> createMapMEGAMETER() {
            std::map<UnitsLength, ConversionPtr> c;
            c[enums::ANGSTROM] = Mul(Pow(10, 16), Sym("megam"));
            c[enums::ASTRONOMICALUNIT] = Mul(Rat(Pow(10, 4), Int(1495978707)), Sym("megam"));
            c[enums::ATTOMETER] = Mul(Pow(10, 24), Sym("megam"));
            c[enums::CENTIMETER] = Mul(Pow(10, 8), Sym("megam"));
            c[enums::DECAMETER] = Mul(Pow(10, 5), Sym("megam"));
            c[enums::DECIMETER] = Mul(Pow(10, 7), Sym("megam"));
            c[enums::EXAMETER] = Mul(Rat(Int(1), Pow(10, 12)), Sym("megam"));
            c[enums::FEMTOMETER] = Mul(Pow(10, 21), Sym("megam"));
            c[enums::FOOT] = Mul(Rat(Int(9842525), Int(3)), Sym("megam"));
            c[enums::GIGAMETER] = Mul(Rat(Int(1), Int(1000)), Sym("megam"));
            c[enums::HECTOMETER] = Mul(Pow(10, 4), Sym("megam"));
            c[enums::INCH] = Mul(Int(39370100), Sym("megam"));
            c[enums::KILOMETER] = Mul(Int(1000), Sym("megam"));
            c[enums::LIGHTYEAR] = Mul(Rat(Int(625), Int("5912956545363")), Sym("megam"));
            c[enums::LINE] = Mul(Int(472441200), Sym("megam"));
            c[enums::METER] = Mul(Pow(10, 6), Sym("megam"));
            c[enums::MICROMETER] = Mul(Pow(10, 12), Sym("megam"));
            c[enums::MILE] = Mul(Rat(Int(178955), Int(288)), Sym("megam"));
            c[enums::MILLIMETER] = Mul(Pow(10, 9), Sym("megam"));
            c[enums::NANOMETER] = Mul(Pow(10, 15), Sym("megam"));
            c[enums::PARSEC] = Mul(Rat(Int(1), Int("30856776000")), Sym("megam"));
            c[enums::PETAMETER] = Mul(Rat(Int(1), Pow(10, 9)), Sym("megam"));
            c[enums::PICOMETER] = Mul(Pow(10, 18), Sym("megam"));
            c[enums::POINT] = Mul(Int("2834647200"), Sym("megam"));
            c[enums::TERAMETER] = Mul(Rat(Int(1), Pow(10, 6)), Sym("megam"));
            c[enums::THOU] = Mul(Rat(Int(393701), Int(10)), Sym("megam"));
            c[enums::YARD] = Mul(Rat(Int(9842525), Int(9)), Sym("megam"));
            c[enums::YOCTOMETER] = Mul(Pow(10, 30), Sym("megam"));
            c[enums::YOTTAMETER] = Mul(Rat(Int(1), Pow(10, 18)), Sym("megam"));
            c[enums::ZEPTOMETER] = Mul(Pow(10, 27), Sym("megam"));
            c[enums::ZETTAMETER] = Mul(Rat(Int(1), Pow(10, 15)), Sym("megam"));
            return c;
        }

        static std::map<UnitsLength, ConversionPtr> createMapMETER() {
            std::map<UnitsLength, ConversionPtr> c;
            c[enums::ANGSTROM] = Mul(Pow(10, 10), Sym("m"));
            c[enums::ASTRONOMICALUNIT] = Mul(Rat(Int(1), Int("149597870700")), Sym("m"));
            c[enums::ATTOMETER] = Mul(Pow(10, 18), Sym("m"));
            c[enums::CENTIMETER] = Mul(Int(100), Sym("m"));
            c[enums::DECAMETER] = Mul(Rat(Int(1), Int(10)), Sym("m"));
            c[enums::DECIMETER] = Mul(Int(10), Sym("m"));
            c[enums::EXAMETER] = Mul(Rat(Int(1), Pow(10, 18)), Sym("m"));
            c[enums::FEMTOMETER] = Mul(Pow(10, 15), Sym("m"));
            c[enums::FOOT] = Mul(Rat(Int(393701), Mul(Int(12), Pow(10, 4))), Sym("m"));
            c[enums::GIGAMETER] = Mul(Rat(Int(1), Pow(10, 9)), Sym("m"));
            c[enums::HECTOMETER] = Mul(Rat(Int(1), Int(100)), Sym("m"));
            c[enums::INCH] = Mul(Rat(Int(393701), Pow(10, 4)), Sym("m"));
            c[enums::KILOMETER] = Mul(Rat(Int(1), Int(1000)), Sym("m"));
            c[enums::LIGHTYEAR] = Mul(Rat(Int(1), Int("9460730472580800")), Sym("m"));
            c[enums::LINE] = Mul(Rat(Int(1181103), Int(2500)), Sym("m"));
            c[enums::MEGAMETER] = Mul(Rat(Int(1), Pow(10, 6)), Sym("m"));
            c[enums::MICROMETER] = Mul(Pow(10, 6), Sym("m"));
            c[enums::MILE] = Mul(Rat(Int(35791), Mul(Int(576), Pow(10, 5))), Sym("m"));
            c[enums::MILLIMETER] = Mul(Int(1000), Sym("m"));
            c[enums::NANOMETER] = Mul(Pow(10, 9), Sym("m"));
            c[enums::PARSEC] = Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 9))), Sym("m"));
            c[enums::PETAMETER] = Mul(Rat(Int(1), Pow(10, 15)), Sym("m"));
            c[enums::PICOMETER] = Mul(Pow(10, 12), Sym("m"));
            c[enums::POINT] = Mul(Rat(Int(3543309), Int(1250)), Sym("m"));
            c[enums::TERAMETER] = Mul(Rat(Int(1), Pow(10, 12)), Sym("m"));
            c[enums::THOU] = Mul(Rat(Int(393701), Pow(10, 7)), Sym("m"));
            c[enums::YARD] = Mul(Rat(Int(393701), Mul(Int(36), Pow(10, 4))), Sym("m"));
            c[enums::YOCTOMETER] = Mul(Pow(10, 24), Sym("m"));
            c[enums::YOTTAMETER] = Mul(Rat(Int(1), Pow(10, 24)), Sym("m"));
            c[enums::ZEPTOMETER] = Mul(Pow(10, 21), Sym("m"));
            c[enums::ZETTAMETER] = Mul(Rat(Int(1), Pow(10, 21)), Sym("m"));
            return c;
        }

        static std::map<UnitsLength, ConversionPtr> createMapMICROMETER() {
            std::map<UnitsLength, ConversionPtr> c;
            c[enums::ANGSTROM] = Mul(Pow(10, 4), Sym("microm"));
            c[enums::ASTRONOMICALUNIT] = Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 8))), Sym("microm"));
            c[enums::ATTOMETER] = Mul(Pow(10, 12), Sym("microm"));
            c[enums::CENTIMETER] = Mul(Rat(Int(1), Pow(10, 4)), Sym("microm"));
            c[enums::DECAMETER] = Mul(Rat(Int(1), Pow(10, 7)), Sym("microm"));
            c[enums::DECIMETER] = Mul(Rat(Int(1), Pow(10, 5)), Sym("microm"));
            c[enums::EXAMETER] = Mul(Rat(Int(1), Pow(10, 24)), Sym("microm"));
            c[enums::FEMTOMETER] = Mul(Pow(10, 9), Sym("microm"));
            c[enums::FOOT] = Mul(Rat(Int(393701), Mul(Int(12), Pow(10, 10))), Sym("microm"));
            c[enums::GIGAMETER] = Mul(Rat(Int(1), Pow(10, 15)), Sym("microm"));
            c[enums::HECTOMETER] = Mul(Rat(Int(1), Pow(10, 8)), Sym("microm"));
            c[enums::INCH] = Mul(Rat(Int(393701), Pow(10, 10)), Sym("microm"));
            c[enums::KILOMETER] = Mul(Rat(Int(1), Pow(10, 9)), Sym("microm"));
            c[enums::LIGHTYEAR] = Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 8))), Sym("microm"));
            c[enums::LINE] = Mul(Rat(Int(1181103), Mul(Int(25), Pow(10, 8))), Sym("microm"));
            c[enums::MEGAMETER] = Mul(Rat(Int(1), Pow(10, 12)), Sym("microm"));
            c[enums::METER] = Mul(Rat(Int(1), Pow(10, 6)), Sym("microm"));
            c[enums::MILE] = Mul(Rat(Int(35791), Mul(Int(576), Pow(10, 11))), Sym("microm"));
            c[enums::MILLIMETER] = Mul(Rat(Int(1), Int(1000)), Sym("microm"));
            c[enums::NANOMETER] = Mul(Int(1000), Sym("microm"));
            c[enums::PARSEC] = Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 15))), Sym("microm"));
            c[enums::PETAMETER] = Mul(Rat(Int(1), Pow(10, 21)), Sym("microm"));
            c[enums::PICOMETER] = Mul(Pow(10, 6), Sym("microm"));
            c[enums::POINT] = Mul(Rat(Int(3543309), Mul(Int(125), Pow(10, 7))), Sym("microm"));
            c[enums::TERAMETER] = Mul(Rat(Int(1), Pow(10, 18)), Sym("microm"));
            c[enums::THOU] = Mul(Rat(Int(393701), Pow(10, 13)), Sym("microm"));
            c[enums::YARD] = Mul(Rat(Int(393701), Mul(Int(36), Pow(10, 10))), Sym("microm"));
            c[enums::YOCTOMETER] = Mul(Pow(10, 18), Sym("microm"));
            c[enums::YOTTAMETER] = Mul(Rat(Int(1), Pow(10, 30)), Sym("microm"));
            c[enums::ZEPTOMETER] = Mul(Pow(10, 15), Sym("microm"));
            c[enums::ZETTAMETER] = Mul(Rat(Int(1), Pow(10, 27)), Sym("microm"));
            return c;
        }

        static std::map<UnitsLength, ConversionPtr> createMapMILE() {
            std::map<UnitsLength, ConversionPtr> c;
            c[enums::ANGSTROM] = Mul(Rat(Mul(Int(576), Pow(10, 15)), Int(35791)), Sym("mi"));
            c[enums::ASTRONOMICALUNIT] = Mul(Rat(Int(192000), Int("17847524634079")), Sym("mi"));
            c[enums::ATTOMETER] = Mul(Rat(Mul(Int(576), Pow(10, 23)), Int(35791)), Sym("mi"));
            c[enums::CENTIMETER] = Mul(Rat(Mul(Int(576), Pow(10, 7)), Int(35791)), Sym("mi"));
            c[enums::DECAMETER] = Mul(Rat(Mul(Int(576), Pow(10, 4)), Int(35791)), Sym("mi"));
            c[enums::DECIMETER] = Mul(Rat(Mul(Int(576), Pow(10, 6)), Int(35791)), Sym("mi"));
            c[enums::EXAMETER] = Mul(Rat(Int(9), Mul(Int(559234375), Pow(10, 7))), Sym("mi"));
            c[enums::FEMTOMETER] = Mul(Rat(Mul(Int(576), Pow(10, 20)), Int(35791)), Sym("mi"));
            c[enums::FOOT] = Mul(Int(5280), Sym("mi"));
            c[enums::GIGAMETER] = Mul(Rat(Int(36), Int(22369375)), Sym("mi"));
            c[enums::HECTOMETER] = Mul(Rat(Int(576000), Int(35791)), Sym("mi"));
            c[enums::INCH] = Mul(Int(63360), Sym("mi"));
            c[enums::KILOMETER] = Mul(Rat(Int(57600), Int(35791)), Sym("mi"));
            c[enums::LIGHTYEAR] = Mul(Rat(Int(4000), Int("23514514190565237")), Sym("mi"));
            c[enums::LINE] = Mul(Int(760320), Sym("mi"));
            c[enums::MEGAMETER] = Mul(Rat(Int(288), Int(178955)), Sym("mi"));
            c[enums::METER] = Mul(Rat(Mul(Int(576), Pow(10, 5)), Int(35791)), Sym("mi"));
            c[enums::MICROMETER] = Mul(Rat(Mul(Int(576), Pow(10, 11)), Int(35791)), Sym("mi"));
            c[enums::MILLIMETER] = Mul(Rat(Mul(Int(576), Pow(10, 8)), Int(35791)), Sym("mi"));
            c[enums::NANOMETER] = Mul(Rat(Mul(Int(576), Pow(10, 14)), Int(35791)), Sym("mi"));
            c[enums::PARSEC] = Mul(Rat(Int(3), Int("57520566136250")), Sym("mi"));
            c[enums::PETAMETER] = Mul(Rat(Int(9), Mul(Int(559234375), Pow(10, 4))), Sym("mi"));
            c[enums::PICOMETER] = Mul(Rat(Mul(Int(576), Pow(10, 17)), Int(35791)), Sym("mi"));
            c[enums::POINT] = Mul(Int(4561920), Sym("mi"));
            c[enums::TERAMETER] = Mul(Rat(Int(9), Int("5592343750")), Sym("mi"));
            c[enums::THOU] = Mul(Rat(Int(1584), Int(25)), Sym("mi"));
            c[enums::YARD] = Mul(Int(1760), Sym("mi"));
            c[enums::YOCTOMETER] = Mul(Rat(Mul(Int(576), Pow(10, 29)), Int(35791)), Sym("mi"));
            c[enums::YOTTAMETER] = Mul(Rat(Int(9), Mul(Int(559234375), Pow(10, 13))), Sym("mi"));
            c[enums::ZEPTOMETER] = Mul(Rat(Mul(Int(576), Pow(10, 26)), Int(35791)), Sym("mi"));
            c[enums::ZETTAMETER] = Mul(Rat(Int(9), Mul(Int(559234375), Pow(10, 10))), Sym("mi"));
            return c;
        }

        static std::map<UnitsLength, ConversionPtr> createMapMILLIMETER() {
            std::map<UnitsLength, ConversionPtr> c;
            c[enums::ANGSTROM] = Mul(Pow(10, 7), Sym("millim"));
            c[enums::ASTRONOMICALUNIT] = Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 5))), Sym("millim"));
            c[enums::ATTOMETER] = Mul(Pow(10, 15), Sym("millim"));
            c[enums::CENTIMETER] = Mul(Rat(Int(1), Int(10)), Sym("millim"));
            c[enums::DECAMETER] = Mul(Rat(Int(1), Pow(10, 4)), Sym("millim"));
            c[enums::DECIMETER] = Mul(Rat(Int(1), Int(100)), Sym("millim"));
            c[enums::EXAMETER] = Mul(Rat(Int(1), Pow(10, 21)), Sym("millim"));
            c[enums::FEMTOMETER] = Mul(Pow(10, 12), Sym("millim"));
            c[enums::FOOT] = Mul(Rat(Int(393701), Mul(Int(12), Pow(10, 7))), Sym("millim"));
            c[enums::GIGAMETER] = Mul(Rat(Int(1), Pow(10, 12)), Sym("millim"));
            c[enums::HECTOMETER] = Mul(Rat(Int(1), Pow(10, 5)), Sym("millim"));
            c[enums::INCH] = Mul(Rat(Int(393701), Pow(10, 7)), Sym("millim"));
            c[enums::KILOMETER] = Mul(Rat(Int(1), Pow(10, 6)), Sym("millim"));
            c[enums::LIGHTYEAR] = Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 5))), Sym("millim"));
            c[enums::LINE] = Mul(Rat(Int(1181103), Mul(Int(25), Pow(10, 5))), Sym("millim"));
            c[enums::MEGAMETER] = Mul(Rat(Int(1), Pow(10, 9)), Sym("millim"));
            c[enums::METER] = Mul(Rat(Int(1), Int(1000)), Sym("millim"));
            c[enums::MICROMETER] = Mul(Int(1000), Sym("millim"));
            c[enums::MILE] = Mul(Rat(Int(35791), Mul(Int(576), Pow(10, 8))), Sym("millim"));
            c[enums::NANOMETER] = Mul(Pow(10, 6), Sym("millim"));
            c[enums::PARSEC] = Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 12))), Sym("millim"));
            c[enums::PETAMETER] = Mul(Rat(Int(1), Pow(10, 18)), Sym("millim"));
            c[enums::PICOMETER] = Mul(Pow(10, 9), Sym("millim"));
            c[enums::POINT] = Mul(Rat(Int(3543309), Mul(Int(125), Pow(10, 4))), Sym("millim"));
            c[enums::TERAMETER] = Mul(Rat(Int(1), Pow(10, 15)), Sym("millim"));
            c[enums::THOU] = Mul(Rat(Int(393701), Pow(10, 10)), Sym("millim"));
            c[enums::YARD] = Mul(Rat(Int(393701), Mul(Int(36), Pow(10, 7))), Sym("millim"));
            c[enums::YOCTOMETER] = Mul(Pow(10, 21), Sym("millim"));
            c[enums::YOTTAMETER] = Mul(Rat(Int(1), Pow(10, 27)), Sym("millim"));
            c[enums::ZEPTOMETER] = Mul(Pow(10, 18), Sym("millim"));
            c[enums::ZETTAMETER] = Mul(Rat(Int(1), Pow(10, 24)), Sym("millim"));
            return c;
        }

        static std::map<UnitsLength, ConversionPtr> createMapNANOMETER() {
            std::map<UnitsLength, ConversionPtr> c;
            c[enums::ANGSTROM] = Mul(Int(10), Sym("nanom"));
            c[enums::ASTRONOMICALUNIT] = Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 11))), Sym("nanom"));
            c[enums::ATTOMETER] = Mul(Pow(10, 9), Sym("nanom"));
            c[enums::CENTIMETER] = Mul(Rat(Int(1), Pow(10, 7)), Sym("nanom"));
            c[enums::DECAMETER] = Mul(Rat(Int(1), Pow(10, 10)), Sym("nanom"));
            c[enums::DECIMETER] = Mul(Rat(Int(1), Pow(10, 8)), Sym("nanom"));
            c[enums::EXAMETER] = Mul(Rat(Int(1), Pow(10, 27)), Sym("nanom"));
            c[enums::FEMTOMETER] = Mul(Pow(10, 6), Sym("nanom"));
            c[enums::FOOT] = Mul(Rat(Int(393701), Mul(Int(12), Pow(10, 13))), Sym("nanom"));
            c[enums::GIGAMETER] = Mul(Rat(Int(1), Pow(10, 18)), Sym("nanom"));
            c[enums::HECTOMETER] = Mul(Rat(Int(1), Pow(10, 11)), Sym("nanom"));
            c[enums::INCH] = Mul(Rat(Int(393701), Pow(10, 13)), Sym("nanom"));
            c[enums::KILOMETER] = Mul(Rat(Int(1), Pow(10, 12)), Sym("nanom"));
            c[enums::LIGHTYEAR] = Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 11))), Sym("nanom"));
            c[enums::LINE] = Mul(Rat(Int(1181103), Mul(Int(25), Pow(10, 11))), Sym("nanom"));
            c[enums::MEGAMETER] = Mul(Rat(Int(1), Pow(10, 15)), Sym("nanom"));
            c[enums::METER] = Mul(Rat(Int(1), Pow(10, 9)), Sym("nanom"));
            c[enums::MICROMETER] = Mul(Rat(Int(1), Int(1000)), Sym("nanom"));
            c[enums::MILE] = Mul(Rat(Int(35791), Mul(Int(576), Pow(10, 14))), Sym("nanom"));
            c[enums::MILLIMETER] = Mul(Rat(Int(1), Pow(10, 6)), Sym("nanom"));
            c[enums::PARSEC] = Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 18))), Sym("nanom"));
            c[enums::PETAMETER] = Mul(Rat(Int(1), Pow(10, 24)), Sym("nanom"));
            c[enums::PICOMETER] = Mul(Int(1000), Sym("nanom"));
            c[enums::POINT] = Mul(Rat(Int(3543309), Mul(Int(125), Pow(10, 10))), Sym("nanom"));
            c[enums::TERAMETER] = Mul(Rat(Int(1), Pow(10, 21)), Sym("nanom"));
            c[enums::THOU] = Mul(Rat(Int(393701), Pow(10, 16)), Sym("nanom"));
            c[enums::YARD] = Mul(Rat(Int(393701), Mul(Int(36), Pow(10, 13))), Sym("nanom"));
            c[enums::YOCTOMETER] = Mul(Pow(10, 15), Sym("nanom"));
            c[enums::YOTTAMETER] = Mul(Rat(Int(1), Pow(10, 33)), Sym("nanom"));
            c[enums::ZEPTOMETER] = Mul(Pow(10, 12), Sym("nanom"));
            c[enums::ZETTAMETER] = Mul(Rat(Int(1), Pow(10, 30)), Sym("nanom"));
            return c;
        }

        static std::map<UnitsLength, ConversionPtr> createMapPARSEC() {
            std::map<UnitsLength, ConversionPtr> c;
            c[enums::ANGSTROM] = Mul(Mul(Int(30856776), Pow(10, 19)), Sym("pc"));
            c[enums::ASTRONOMICALUNIT] = Mul(Rat(Mul(Int(10285592), Pow(10, 7)), Int(498659569)), Sym("pc"));
            c[enums::ATTOMETER] = Mul(Mul(Int(30856776), Pow(10, 27)), Sym("pc"));
            c[enums::CENTIMETER] = Mul(Mul(Int(30856776), Pow(10, 11)), Sym("pc"));
            c[enums::DECAMETER] = Mul(Mul(Int(30856776), Pow(10, 8)), Sym("pc"));
            c[enums::DECIMETER] = Mul(Mul(Int(30856776), Pow(10, 10)), Sym("pc"));
            c[enums::EXAMETER] = Mul(Rat(Int(3857097), Mul(Int(125), Pow(10, 6))), Sym("pc"));
            c[enums::FEMTOMETER] = Mul(Mul(Int(30856776), Pow(10, 24)), Sym("pc"));
            c[enums::FOOT] = Mul(Mul(Int("1012361963998"), Pow(10, 5)), Sym("pc"));
            c[enums::GIGAMETER] = Mul(Int(30856776), Sym("pc"));
            c[enums::HECTOMETER] = Mul(Mul(Int(30856776), Pow(10, 7)), Sym("pc"));
            c[enums::INCH] = Mul(Mul(Int("12148343567976"), Pow(10, 5)), Sym("pc"));
            c[enums::KILOMETER] = Mul(Mul(Int(30856776), Pow(10, 6)), Sym("pc"));
            c[enums::LIGHTYEAR] = Mul(Rat(Mul(Int(6428495), Pow(10, 6)), Int("1970985515121")), Sym("pc"));
            c[enums::LINE] = Mul(Mul(Int("145780122815712"), Pow(10, 5)), Sym("pc"));
            c[enums::MEGAMETER] = Mul(Int("30856776000"), Sym("pc"));
            c[enums::METER] = Mul(Mul(Int(30856776), Pow(10, 9)), Sym("pc"));
            c[enums::MICROMETER] = Mul(Mul(Int(30856776), Pow(10, 15)), Sym("pc"));
            c[enums::MILE] = Mul(Rat(Int("57520566136250"), Int(3)), Sym("pc"));
            c[enums::MILLIMETER] = Mul(Mul(Int(30856776), Pow(10, 12)), Sym("pc"));
            c[enums::NANOMETER] = Mul(Mul(Int(30856776), Pow(10, 18)), Sym("pc"));
            c[enums::PETAMETER] = Mul(Rat(Int(3857097), Int(125000)), Sym("pc"));
            c[enums::PICOMETER] = Mul(Mul(Int(30856776), Pow(10, 21)), Sym("pc"));
            c[enums::POINT] = Mul(Mul(Int("874680736894272"), Pow(10, 5)), Sym("pc"));
            c[enums::TERAMETER] = Mul(Rat(Int(3857097), Int(125)), Sym("pc"));
            c[enums::THOU] = Mul(Int("1214834356797600"), Sym("pc"));
            c[enums::YARD] = Mul(Rat(Mul(Int("1012361963998"), Pow(10, 5)), Int(3)), Sym("pc"));
            c[enums::YOCTOMETER] = Mul(Mul(Int(30856776), Pow(10, 33)), Sym("pc"));
            c[enums::YOTTAMETER] = Mul(Rat(Int(3857097), Mul(Int(125), Pow(10, 12))), Sym("pc"));
            c[enums::ZEPTOMETER] = Mul(Mul(Int(30856776), Pow(10, 30)), Sym("pc"));
            c[enums::ZETTAMETER] = Mul(Rat(Int(3857097), Mul(Int(125), Pow(10, 9))), Sym("pc"));
            return c;
        }

        static std::map<UnitsLength, ConversionPtr> createMapPETAMETER() {
            std::map<UnitsLength, ConversionPtr> c;
            c[enums::ANGSTROM] = Mul(Pow(10, 25), Sym("petam"));
            c[enums::ASTRONOMICALUNIT] = Mul(Rat(Pow(10, 13), Int(1495978707)), Sym("petam"));
            c[enums::ATTOMETER] = Mul(Pow(10, 33), Sym("petam"));
            c[enums::CENTIMETER] = Mul(Pow(10, 17), Sym("petam"));
            c[enums::DECAMETER] = Mul(Pow(10, 14), Sym("petam"));
            c[enums::DECIMETER] = Mul(Pow(10, 16), Sym("petam"));
            c[enums::EXAMETER] = Mul(Rat(Int(1), Int(1000)), Sym("petam"));
            c[enums::FEMTOMETER] = Mul(Pow(10, 30), Sym("petam"));
            c[enums::FOOT] = Mul(Rat(Mul(Int(9842525), Pow(10, 9)), Int(3)), Sym("petam"));
            c[enums::GIGAMETER] = Mul(Pow(10, 6), Sym("petam"));
            c[enums::HECTOMETER] = Mul(Pow(10, 13), Sym("petam"));
            c[enums::INCH] = Mul(Mul(Int(393701), Pow(10, 11)), Sym("petam"));
            c[enums::KILOMETER] = Mul(Pow(10, 12), Sym("petam"));
            c[enums::LIGHTYEAR] = Mul(Rat(Mul(Int(625), Pow(10, 9)), Int("5912956545363")), Sym("petam"));
            c[enums::LINE] = Mul(Mul(Int(4724412), Pow(10, 11)), Sym("petam"));
            c[enums::MEGAMETER] = Mul(Pow(10, 9), Sym("petam"));
            c[enums::METER] = Mul(Pow(10, 15), Sym("petam"));
            c[enums::MICROMETER] = Mul(Pow(10, 21), Sym("petam"));
            c[enums::MILE] = Mul(Rat(Mul(Int(559234375), Pow(10, 4)), Int(9)), Sym("petam"));
            c[enums::MILLIMETER] = Mul(Pow(10, 18), Sym("petam"));
            c[enums::NANOMETER] = Mul(Pow(10, 24), Sym("petam"));
            c[enums::PARSEC] = Mul(Rat(Int(125000), Int(3857097)), Sym("petam"));
            c[enums::PICOMETER] = Mul(Pow(10, 27), Sym("petam"));
            c[enums::POINT] = Mul(Mul(Int(28346472), Pow(10, 11)), Sym("petam"));
            c[enums::TERAMETER] = Mul(Int(1000), Sym("petam"));
            c[enums::THOU] = Mul(Mul(Int(393701), Pow(10, 8)), Sym("petam"));
            c[enums::YARD] = Mul(Rat(Mul(Int(9842525), Pow(10, 9)), Int(9)), Sym("petam"));
            c[enums::YOCTOMETER] = Mul(Pow(10, 39), Sym("petam"));
            c[enums::YOTTAMETER] = Mul(Rat(Int(1), Pow(10, 9)), Sym("petam"));
            c[enums::ZEPTOMETER] = Mul(Pow(10, 36), Sym("petam"));
            c[enums::ZETTAMETER] = Mul(Rat(Int(1), Pow(10, 6)), Sym("petam"));
            return c;
        }

        static std::map<UnitsLength, ConversionPtr> createMapPICOMETER() {
            std::map<UnitsLength, ConversionPtr> c;
            c[enums::ANGSTROM] = Mul(Rat(Int(1), Int(100)), Sym("picom"));
            c[enums::ASTRONOMICALUNIT] = Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 14))), Sym("picom"));
            c[enums::ATTOMETER] = Mul(Pow(10, 6), Sym("picom"));
            c[enums::CENTIMETER] = Mul(Rat(Int(1), Pow(10, 10)), Sym("picom"));
            c[enums::DECAMETER] = Mul(Rat(Int(1), Pow(10, 13)), Sym("picom"));
            c[enums::DECIMETER] = Mul(Rat(Int(1), Pow(10, 11)), Sym("picom"));
            c[enums::EXAMETER] = Mul(Rat(Int(1), Pow(10, 30)), Sym("picom"));
            c[enums::FEMTOMETER] = Mul(Int(1000), Sym("picom"));
            c[enums::FOOT] = Mul(Rat(Int(393701), Mul(Int(12), Pow(10, 16))), Sym("picom"));
            c[enums::GIGAMETER] = Mul(Rat(Int(1), Pow(10, 21)), Sym("picom"));
            c[enums::HECTOMETER] = Mul(Rat(Int(1), Pow(10, 14)), Sym("picom"));
            c[enums::INCH] = Mul(Rat(Int(393701), Pow(10, 16)), Sym("picom"));
            c[enums::KILOMETER] = Mul(Rat(Int(1), Pow(10, 15)), Sym("picom"));
            c[enums::LIGHTYEAR] = Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 14))), Sym("picom"));
            c[enums::LINE] = Mul(Rat(Int(1181103), Mul(Int(25), Pow(10, 14))), Sym("picom"));
            c[enums::MEGAMETER] = Mul(Rat(Int(1), Pow(10, 18)), Sym("picom"));
            c[enums::METER] = Mul(Rat(Int(1), Pow(10, 12)), Sym("picom"));
            c[enums::MICROMETER] = Mul(Rat(Int(1), Pow(10, 6)), Sym("picom"));
            c[enums::MILE] = Mul(Rat(Int(35791), Mul(Int(576), Pow(10, 17))), Sym("picom"));
            c[enums::MILLIMETER] = Mul(Rat(Int(1), Pow(10, 9)), Sym("picom"));
            c[enums::NANOMETER] = Mul(Rat(Int(1), Int(1000)), Sym("picom"));
            c[enums::PARSEC] = Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 21))), Sym("picom"));
            c[enums::PETAMETER] = Mul(Rat(Int(1), Pow(10, 27)), Sym("picom"));
            c[enums::POINT] = Mul(Rat(Int(3543309), Mul(Int(125), Pow(10, 13))), Sym("picom"));
            c[enums::TERAMETER] = Mul(Rat(Int(1), Pow(10, 24)), Sym("picom"));
            c[enums::THOU] = Mul(Rat(Int(393701), Pow(10, 19)), Sym("picom"));
            c[enums::YARD] = Mul(Rat(Int(393701), Mul(Int(36), Pow(10, 16))), Sym("picom"));
            c[enums::YOCTOMETER] = Mul(Pow(10, 12), Sym("picom"));
            c[enums::YOTTAMETER] = Mul(Rat(Int(1), Pow(10, 36)), Sym("picom"));
            c[enums::ZEPTOMETER] = Mul(Pow(10, 9), Sym("picom"));
            c[enums::ZETTAMETER] = Mul(Rat(Int(1), Pow(10, 33)), Sym("picom"));
            return c;
        }

        static std::map<UnitsLength, ConversionPtr> createMapPOINT() {
            std::map<UnitsLength, ConversionPtr> c;
            c[enums::ANGSTROM] = Mul(Rat(Mul(Int(125), Pow(10, 11)), Int(3543309)), Sym("pt"));
            c[enums::ASTRONOMICALUNIT] = Mul(Rat(Int(25), Int("10601429632642926")), Sym("pt"));
            c[enums::ATTOMETER] = Mul(Rat(Mul(Int(125), Pow(10, 19)), Int(3543309)), Sym("pt"));
            c[enums::CENTIMETER] = Mul(Rat(Int(125000), Int(3543309)), Sym("pt"));
            c[enums::DECAMETER] = Mul(Rat(Int(125), Int(3543309)), Sym("pt"));
            c[enums::DECIMETER] = Mul(Rat(Int(12500), Int(3543309)), Sym("pt"));
            c[enums::EXAMETER] = Mul(Rat(Int(1), Mul(Int(28346472), Pow(10, 14))), Sym("pt"));
            c[enums::FEMTOMETER] = Mul(Rat(Mul(Int(125), Pow(10, 16)), Int(3543309)), Sym("pt"));
            c[enums::FOOT] = Mul(Rat(Int(1), Int(864)), Sym("pt"));
            c[enums::GIGAMETER] = Mul(Rat(Int(1), Mul(Int(28346472), Pow(10, 5))), Sym("pt"));
            c[enums::HECTOMETER] = Mul(Rat(Int(25), Int(7086618)), Sym("pt"));
            c[enums::INCH] = Mul(Rat(Int(1), Int(72)), Sym("pt"));
            c[enums::KILOMETER] = Mul(Rat(Int(5), Int(14173236)), Sym("pt"));
            c[enums::LIGHTYEAR] = Mul(Rat(Int(25), Int("670445828601396037344")), Sym("pt"));
            c[enums::LINE] = Mul(Rat(Int(1), Int(6)), Sym("pt"));
            c[enums::MEGAMETER] = Mul(Rat(Int(1), Int("2834647200")), Sym("pt"));
            c[enums::METER] = Mul(Rat(Int(1250), Int(3543309)), Sym("pt"));
            c[enums::MICROMETER] = Mul(Rat(Mul(Int(125), Pow(10, 7)), Int(3543309)), Sym("pt"));
            c[enums::MILE] = Mul(Rat(Int(1), Int(4561920)), Sym("pt"));
            c[enums::MILLIMETER] = Mul(Rat(Mul(Int(125), Pow(10, 4)), Int(3543309)), Sym("pt"));
            c[enums::NANOMETER] = Mul(Rat(Mul(Int(125), Pow(10, 10)), Int(3543309)), Sym("pt"));
            c[enums::PARSEC] = Mul(Rat(Int(1), Mul(Int("874680736894272"), Pow(10, 5))), Sym("pt"));
            c[enums::PETAMETER] = Mul(Rat(Int(1), Mul(Int(28346472), Pow(10, 11))), Sym("pt"));
            c[enums::PICOMETER] = Mul(Rat(Mul(Int(125), Pow(10, 13)), Int(3543309)), Sym("pt"));
            c[enums::TERAMETER] = Mul(Rat(Int(1), Mul(Int(28346472), Pow(10, 8))), Sym("pt"));
            c[enums::THOU] = Mul(Rat(Int(1), Int(72000)), Sym("pt"));
            c[enums::YARD] = Mul(Rat(Int(1), Int(2592)), Sym("pt"));
            c[enums::YOCTOMETER] = Mul(Rat(Mul(Int(125), Pow(10, 25)), Int(3543309)), Sym("pt"));
            c[enums::YOTTAMETER] = Mul(Rat(Int(1), Mul(Int(28346472), Pow(10, 20))), Sym("pt"));
            c[enums::ZEPTOMETER] = Mul(Rat(Mul(Int(125), Pow(10, 22)), Int(3543309)), Sym("pt"));
            c[enums::ZETTAMETER] = Mul(Rat(Int(1), Mul(Int(28346472), Pow(10, 17))), Sym("pt"));
            return c;
        }

        static std::map<UnitsLength, ConversionPtr> createMapTERAMETER() {
            std::map<UnitsLength, ConversionPtr> c;
            c[enums::ANGSTROM] = Mul(Pow(10, 22), Sym("teram"));
            c[enums::ASTRONOMICALUNIT] = Mul(Rat(Pow(10, 10), Int(1495978707)), Sym("teram"));
            c[enums::ATTOMETER] = Mul(Pow(10, 30), Sym("teram"));
            c[enums::CENTIMETER] = Mul(Pow(10, 14), Sym("teram"));
            c[enums::DECAMETER] = Mul(Pow(10, 11), Sym("teram"));
            c[enums::DECIMETER] = Mul(Pow(10, 13), Sym("teram"));
            c[enums::EXAMETER] = Mul(Rat(Int(1), Pow(10, 6)), Sym("teram"));
            c[enums::FEMTOMETER] = Mul(Pow(10, 27), Sym("teram"));
            c[enums::FOOT] = Mul(Rat(Mul(Int(9842525), Pow(10, 6)), Int(3)), Sym("teram"));
            c[enums::GIGAMETER] = Mul(Int(1000), Sym("teram"));
            c[enums::HECTOMETER] = Mul(Pow(10, 10), Sym("teram"));
            c[enums::INCH] = Mul(Mul(Int(393701), Pow(10, 8)), Sym("teram"));
            c[enums::KILOMETER] = Mul(Pow(10, 9), Sym("teram"));
            c[enums::LIGHTYEAR] = Mul(Rat(Mul(Int(625), Pow(10, 6)), Int("5912956545363")), Sym("teram"));
            c[enums::LINE] = Mul(Mul(Int(4724412), Pow(10, 8)), Sym("teram"));
            c[enums::MEGAMETER] = Mul(Pow(10, 6), Sym("teram"));
            c[enums::METER] = Mul(Pow(10, 12), Sym("teram"));
            c[enums::MICROMETER] = Mul(Pow(10, 18), Sym("teram"));
            c[enums::MILE] = Mul(Rat(Int("5592343750"), Int(9)), Sym("teram"));
            c[enums::MILLIMETER] = Mul(Pow(10, 15), Sym("teram"));
            c[enums::NANOMETER] = Mul(Pow(10, 21), Sym("teram"));
            c[enums::PARSEC] = Mul(Rat(Int(125), Int(3857097)), Sym("teram"));
            c[enums::PETAMETER] = Mul(Rat(Int(1), Int(1000)), Sym("teram"));
            c[enums::PICOMETER] = Mul(Pow(10, 24), Sym("teram"));
            c[enums::POINT] = Mul(Mul(Int(28346472), Pow(10, 8)), Sym("teram"));
            c[enums::THOU] = Mul(Mul(Int(393701), Pow(10, 5)), Sym("teram"));
            c[enums::YARD] = Mul(Rat(Mul(Int(9842525), Pow(10, 6)), Int(9)), Sym("teram"));
            c[enums::YOCTOMETER] = Mul(Pow(10, 36), Sym("teram"));
            c[enums::YOTTAMETER] = Mul(Rat(Int(1), Pow(10, 12)), Sym("teram"));
            c[enums::ZEPTOMETER] = Mul(Pow(10, 33), Sym("teram"));
            c[enums::ZETTAMETER] = Mul(Rat(Int(1), Pow(10, 9)), Sym("teram"));
            return c;
        }

        static std::map<UnitsLength, ConversionPtr> createMapTHOU() {
            std::map<UnitsLength, ConversionPtr> c;
            c[enums::ANGSTROM] = Mul(Rat(Pow(10, 17), Int(393701)), Sym("thou"));
            c[enums::ASTRONOMICALUNIT] = Mul(Rat(Pow(10, 5), Int("588968312924607")), Sym("thou"));
            c[enums::ATTOMETER] = Mul(Rat(Pow(10, 25), Int(393701)), Sym("thou"));
            c[enums::CENTIMETER] = Mul(Rat(Pow(10, 9), Int(393701)), Sym("thou"));
            c[enums::DECAMETER] = Mul(Rat(Pow(10, 6), Int(393701)), Sym("thou"));
            c[enums::DECIMETER] = Mul(Rat(Pow(10, 8), Int(393701)), Sym("thou"));
            c[enums::EXAMETER] = Mul(Rat(Int(1), Mul(Int(393701), Pow(10, 11))), Sym("thou"));
            c[enums::FEMTOMETER] = Mul(Rat(Pow(10, 22), Int(393701)), Sym("thou"));
            c[enums::FOOT] = Mul(Rat(Int(250), Int(3)), Sym("thou"));
            c[enums::GIGAMETER] = Mul(Rat(Int(1), Int(39370100)), Sym("thou"));
            c[enums::HECTOMETER] = Mul(Rat(Pow(10, 5), Int(393701)), Sym("thou"));
            c[enums::INCH] = Mul(Int(1000), Sym("thou"));
            c[enums::KILOMETER] = Mul(Rat(Pow(10, 4), Int(393701)), Sym("thou"));
            c[enums::LIGHTYEAR] = Mul(Rat(Int(6250), Int("2327936904865958463")), Sym("thou"));
            c[enums::LINE] = Mul(Int(12000), Sym("thou"));
            c[enums::MEGAMETER] = Mul(Rat(Int(10), Int(393701)), Sym("thou"));
            c[enums::METER] = Mul(Rat(Pow(10, 7), Int(393701)), Sym("thou"));
            c[enums::MICROMETER] = Mul(Rat(Pow(10, 13), Int(393701)), Sym("thou"));
            c[enums::MILE] = Mul(Rat(Int(25), Int(1584)), Sym("thou"));
            c[enums::MILLIMETER] = Mul(Rat(Pow(10, 10), Int(393701)), Sym("thou"));
            c[enums::NANOMETER] = Mul(Rat(Pow(10, 16), Int(393701)), Sym("thou"));
            c[enums::PARSEC] = Mul(Rat(Int(1), Int("1214834356797600")), Sym("thou"));
            c[enums::PETAMETER] = Mul(Rat(Int(1), Mul(Int(393701), Pow(10, 8))), Sym("thou"));
            c[enums::PICOMETER] = Mul(Rat(Pow(10, 19), Int(393701)), Sym("thou"));
            c[enums::POINT] = Mul(Int(72000), Sym("thou"));
            c[enums::TERAMETER] = Mul(Rat(Int(1), Mul(Int(393701), Pow(10, 5))), Sym("thou"));
            c[enums::YARD] = Mul(Rat(Int(250), Int(9)), Sym("thou"));
            c[enums::YOCTOMETER] = Mul(Rat(Pow(10, 31), Int(393701)), Sym("thou"));
            c[enums::YOTTAMETER] = Mul(Rat(Int(1), Mul(Int(393701), Pow(10, 17))), Sym("thou"));
            c[enums::ZEPTOMETER] = Mul(Rat(Pow(10, 28), Int(393701)), Sym("thou"));
            c[enums::ZETTAMETER] = Mul(Rat(Int(1), Mul(Int(393701), Pow(10, 14))), Sym("thou"));
            return c;
        }

        static std::map<UnitsLength, ConversionPtr> createMapYARD() {
            std::map<UnitsLength, ConversionPtr> c;
            c[enums::ANGSTROM] = Mul(Rat(Mul(Int(36), Pow(10, 14)), Int(393701)), Sym("yd"));
            c[enums::ASTRONOMICALUNIT] = Mul(Rat(Int(1200), Int("196322770974869")), Sym("yd"));
            c[enums::ATTOMETER] = Mul(Rat(Mul(Int(36), Pow(10, 22)), Int(393701)), Sym("yd"));
            c[enums::CENTIMETER] = Mul(Rat(Mul(Int(36), Pow(10, 6)), Int(393701)), Sym("yd"));
            c[enums::DECAMETER] = Mul(Rat(Int(36000), Int(393701)), Sym("yd"));
            c[enums::DECIMETER] = Mul(Rat(Mul(Int(36), Pow(10, 5)), Int(393701)), Sym("yd"));
            c[enums::EXAMETER] = Mul(Rat(Int(9), Mul(Int(9842525), Pow(10, 12))), Sym("yd"));
            c[enums::FEMTOMETER] = Mul(Rat(Mul(Int(36), Pow(10, 19)), Int(393701)), Sym("yd"));
            c[enums::FOOT] = Mul(Int(3), Sym("yd"));
            c[enums::GIGAMETER] = Mul(Rat(Int(9), Int("9842525000")), Sym("yd"));
            c[enums::HECTOMETER] = Mul(Rat(Int(3600), Int(393701)), Sym("yd"));
            c[enums::INCH] = Mul(Int(36), Sym("yd"));
            c[enums::KILOMETER] = Mul(Rat(Int(360), Int(393701)), Sym("yd"));
            c[enums::LIGHTYEAR] = Mul(Rat(Int(25), Int("258659656096217607")), Sym("yd"));
            c[enums::LINE] = Mul(Int(432), Sym("yd"));
            c[enums::MEGAMETER] = Mul(Rat(Int(9), Int(9842525)), Sym("yd"));
            c[enums::METER] = Mul(Rat(Mul(Int(36), Pow(10, 4)), Int(393701)), Sym("yd"));
            c[enums::MICROMETER] = Mul(Rat(Mul(Int(36), Pow(10, 10)), Int(393701)), Sym("yd"));
            c[enums::MILE] = Mul(Rat(Int(1), Int(1760)), Sym("yd"));
            c[enums::MILLIMETER] = Mul(Rat(Mul(Int(36), Pow(10, 7)), Int(393701)), Sym("yd"));
            c[enums::NANOMETER] = Mul(Rat(Mul(Int(36), Pow(10, 13)), Int(393701)), Sym("yd"));
            c[enums::PARSEC] = Mul(Rat(Int(3), Mul(Int("1012361963998"), Pow(10, 5))), Sym("yd"));
            c[enums::PETAMETER] = Mul(Rat(Int(9), Mul(Int(9842525), Pow(10, 9))), Sym("yd"));
            c[enums::PICOMETER] = Mul(Rat(Mul(Int(36), Pow(10, 16)), Int(393701)), Sym("yd"));
            c[enums::POINT] = Mul(Int(2592), Sym("yd"));
            c[enums::TERAMETER] = Mul(Rat(Int(9), Mul(Int(9842525), Pow(10, 6))), Sym("yd"));
            c[enums::THOU] = Mul(Rat(Int(9), Int(250)), Sym("yd"));
            c[enums::YOCTOMETER] = Mul(Rat(Mul(Int(36), Pow(10, 28)), Int(393701)), Sym("yd"));
            c[enums::YOTTAMETER] = Mul(Rat(Int(9), Mul(Int(9842525), Pow(10, 18))), Sym("yd"));
            c[enums::ZEPTOMETER] = Mul(Rat(Mul(Int(36), Pow(10, 25)), Int(393701)), Sym("yd"));
            c[enums::ZETTAMETER] = Mul(Rat(Int(9), Mul(Int(9842525), Pow(10, 15))), Sym("yd"));
            return c;
        }

        static std::map<UnitsLength, ConversionPtr> createMapYOCTOMETER() {
            std::map<UnitsLength, ConversionPtr> c;
            c[enums::ANGSTROM] = Mul(Rat(Int(1), Pow(10, 14)), Sym("yoctom"));
            c[enums::ASTRONOMICALUNIT] = Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 26))), Sym("yoctom"));
            c[enums::ATTOMETER] = Mul(Rat(Int(1), Pow(10, 6)), Sym("yoctom"));
            c[enums::CENTIMETER] = Mul(Rat(Int(1), Pow(10, 22)), Sym("yoctom"));
            c[enums::DECAMETER] = Mul(Rat(Int(1), Pow(10, 25)), Sym("yoctom"));
            c[enums::DECIMETER] = Mul(Rat(Int(1), Pow(10, 23)), Sym("yoctom"));
            c[enums::EXAMETER] = Mul(Rat(Int(1), Pow(10, 42)), Sym("yoctom"));
            c[enums::FEMTOMETER] = Mul(Rat(Int(1), Pow(10, 9)), Sym("yoctom"));
            c[enums::FOOT] = Mul(Rat(Int(393701), Mul(Int(12), Pow(10, 28))), Sym("yoctom"));
            c[enums::GIGAMETER] = Mul(Rat(Int(1), Pow(10, 33)), Sym("yoctom"));
            c[enums::HECTOMETER] = Mul(Rat(Int(1), Pow(10, 26)), Sym("yoctom"));
            c[enums::INCH] = Mul(Rat(Int(393701), Pow(10, 28)), Sym("yoctom"));
            c[enums::KILOMETER] = Mul(Rat(Int(1), Pow(10, 27)), Sym("yoctom"));
            c[enums::LIGHTYEAR] = Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 26))), Sym("yoctom"));
            c[enums::LINE] = Mul(Rat(Int(1181103), Mul(Int(25), Pow(10, 26))), Sym("yoctom"));
            c[enums::MEGAMETER] = Mul(Rat(Int(1), Pow(10, 30)), Sym("yoctom"));
            c[enums::METER] = Mul(Rat(Int(1), Pow(10, 24)), Sym("yoctom"));
            c[enums::MICROMETER] = Mul(Rat(Int(1), Pow(10, 18)), Sym("yoctom"));
            c[enums::MILE] = Mul(Rat(Int(35791), Mul(Int(576), Pow(10, 29))), Sym("yoctom"));
            c[enums::MILLIMETER] = Mul(Rat(Int(1), Pow(10, 21)), Sym("yoctom"));
            c[enums::NANOMETER] = Mul(Rat(Int(1), Pow(10, 15)), Sym("yoctom"));
            c[enums::PARSEC] = Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 33))), Sym("yoctom"));
            c[enums::PETAMETER] = Mul(Rat(Int(1), Pow(10, 39)), Sym("yoctom"));
            c[enums::PICOMETER] = Mul(Rat(Int(1), Pow(10, 12)), Sym("yoctom"));
            c[enums::POINT] = Mul(Rat(Int(3543309), Mul(Int(125), Pow(10, 25))), Sym("yoctom"));
            c[enums::TERAMETER] = Mul(Rat(Int(1), Pow(10, 36)), Sym("yoctom"));
            c[enums::THOU] = Mul(Rat(Int(393701), Pow(10, 31)), Sym("yoctom"));
            c[enums::YARD] = Mul(Rat(Int(393701), Mul(Int(36), Pow(10, 28))), Sym("yoctom"));
            c[enums::YOTTAMETER] = Mul(Rat(Int(1), Pow(10, 48)), Sym("yoctom"));
            c[enums::ZEPTOMETER] = Mul(Rat(Int(1), Int(1000)), Sym("yoctom"));
            c[enums::ZETTAMETER] = Mul(Rat(Int(1), Pow(10, 45)), Sym("yoctom"));
            return c;
        }

        static std::map<UnitsLength, ConversionPtr> createMapYOTTAMETER() {
            std::map<UnitsLength, ConversionPtr> c;
            c[enums::ANGSTROM] = Mul(Pow(10, 34), Sym("yottam"));
            c[enums::ASTRONOMICALUNIT] = Mul(Rat(Pow(10, 22), Int(1495978707)), Sym("yottam"));
            c[enums::ATTOMETER] = Mul(Pow(10, 42), Sym("yottam"));
            c[enums::CENTIMETER] = Mul(Pow(10, 26), Sym("yottam"));
            c[enums::DECAMETER] = Mul(Pow(10, 23), Sym("yottam"));
            c[enums::DECIMETER] = Mul(Pow(10, 25), Sym("yottam"));
            c[enums::EXAMETER] = Mul(Pow(10, 6), Sym("yottam"));
            c[enums::FEMTOMETER] = Mul(Pow(10, 39), Sym("yottam"));
            c[enums::FOOT] = Mul(Rat(Mul(Int(9842525), Pow(10, 18)), Int(3)), Sym("yottam"));
            c[enums::GIGAMETER] = Mul(Pow(10, 15), Sym("yottam"));
            c[enums::HECTOMETER] = Mul(Pow(10, 22), Sym("yottam"));
            c[enums::INCH] = Mul(Mul(Int(393701), Pow(10, 20)), Sym("yottam"));
            c[enums::KILOMETER] = Mul(Pow(10, 21), Sym("yottam"));
            c[enums::LIGHTYEAR] = Mul(Rat(Mul(Int(625), Pow(10, 18)), Int("5912956545363")), Sym("yottam"));
            c[enums::LINE] = Mul(Mul(Int(4724412), Pow(10, 20)), Sym("yottam"));
            c[enums::MEGAMETER] = Mul(Pow(10, 18), Sym("yottam"));
            c[enums::METER] = Mul(Pow(10, 24), Sym("yottam"));
            c[enums::MICROMETER] = Mul(Pow(10, 30), Sym("yottam"));
            c[enums::MILE] = Mul(Rat(Mul(Int(559234375), Pow(10, 13)), Int(9)), Sym("yottam"));
            c[enums::MILLIMETER] = Mul(Pow(10, 27), Sym("yottam"));
            c[enums::NANOMETER] = Mul(Pow(10, 33), Sym("yottam"));
            c[enums::PARSEC] = Mul(Rat(Mul(Int(125), Pow(10, 12)), Int(3857097)), Sym("yottam"));
            c[enums::PETAMETER] = Mul(Pow(10, 9), Sym("yottam"));
            c[enums::PICOMETER] = Mul(Pow(10, 36), Sym("yottam"));
            c[enums::POINT] = Mul(Mul(Int(28346472), Pow(10, 20)), Sym("yottam"));
            c[enums::TERAMETER] = Mul(Pow(10, 12), Sym("yottam"));
            c[enums::THOU] = Mul(Mul(Int(393701), Pow(10, 17)), Sym("yottam"));
            c[enums::YARD] = Mul(Rat(Mul(Int(9842525), Pow(10, 18)), Int(9)), Sym("yottam"));
            c[enums::YOCTOMETER] = Mul(Pow(10, 48), Sym("yottam"));
            c[enums::ZEPTOMETER] = Mul(Pow(10, 45), Sym("yottam"));
            c[enums::ZETTAMETER] = Mul(Int(1000), Sym("yottam"));
            return c;
        }

        static std::map<UnitsLength, ConversionPtr> createMapZEPTOMETER() {
            std::map<UnitsLength, ConversionPtr> c;
            c[enums::ANGSTROM] = Mul(Rat(Int(1), Pow(10, 11)), Sym("zeptom"));
            c[enums::ASTRONOMICALUNIT] = Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 23))), Sym("zeptom"));
            c[enums::ATTOMETER] = Mul(Rat(Int(1), Int(1000)), Sym("zeptom"));
            c[enums::CENTIMETER] = Mul(Rat(Int(1), Pow(10, 19)), Sym("zeptom"));
            c[enums::DECAMETER] = Mul(Rat(Int(1), Pow(10, 22)), Sym("zeptom"));
            c[enums::DECIMETER] = Mul(Rat(Int(1), Pow(10, 20)), Sym("zeptom"));
            c[enums::EXAMETER] = Mul(Rat(Int(1), Pow(10, 39)), Sym("zeptom"));
            c[enums::FEMTOMETER] = Mul(Rat(Int(1), Pow(10, 6)), Sym("zeptom"));
            c[enums::FOOT] = Mul(Rat(Int(393701), Mul(Int(12), Pow(10, 25))), Sym("zeptom"));
            c[enums::GIGAMETER] = Mul(Rat(Int(1), Pow(10, 30)), Sym("zeptom"));
            c[enums::HECTOMETER] = Mul(Rat(Int(1), Pow(10, 23)), Sym("zeptom"));
            c[enums::INCH] = Mul(Rat(Int(393701), Pow(10, 25)), Sym("zeptom"));
            c[enums::KILOMETER] = Mul(Rat(Int(1), Pow(10, 24)), Sym("zeptom"));
            c[enums::LIGHTYEAR] = Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 23))), Sym("zeptom"));
            c[enums::LINE] = Mul(Rat(Int(1181103), Mul(Int(25), Pow(10, 23))), Sym("zeptom"));
            c[enums::MEGAMETER] = Mul(Rat(Int(1), Pow(10, 27)), Sym("zeptom"));
            c[enums::METER] = Mul(Rat(Int(1), Pow(10, 21)), Sym("zeptom"));
            c[enums::MICROMETER] = Mul(Rat(Int(1), Pow(10, 15)), Sym("zeptom"));
            c[enums::MILE] = Mul(Rat(Int(35791), Mul(Int(576), Pow(10, 26))), Sym("zeptom"));
            c[enums::MILLIMETER] = Mul(Rat(Int(1), Pow(10, 18)), Sym("zeptom"));
            c[enums::NANOMETER] = Mul(Rat(Int(1), Pow(10, 12)), Sym("zeptom"));
            c[enums::PARSEC] = Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 30))), Sym("zeptom"));
            c[enums::PETAMETER] = Mul(Rat(Int(1), Pow(10, 36)), Sym("zeptom"));
            c[enums::PICOMETER] = Mul(Rat(Int(1), Pow(10, 9)), Sym("zeptom"));
            c[enums::POINT] = Mul(Rat(Int(3543309), Mul(Int(125), Pow(10, 22))), Sym("zeptom"));
            c[enums::TERAMETER] = Mul(Rat(Int(1), Pow(10, 33)), Sym("zeptom"));
            c[enums::THOU] = Mul(Rat(Int(393701), Pow(10, 28)), Sym("zeptom"));
            c[enums::YARD] = Mul(Rat(Int(393701), Mul(Int(36), Pow(10, 25))), Sym("zeptom"));
            c[enums::YOCTOMETER] = Mul(Int(1000), Sym("zeptom"));
            c[enums::YOTTAMETER] = Mul(Rat(Int(1), Pow(10, 45)), Sym("zeptom"));
            c[enums::ZETTAMETER] = Mul(Rat(Int(1), Pow(10, 42)), Sym("zeptom"));
            return c;
        }

        static std::map<UnitsLength, ConversionPtr> createMapZETTAMETER() {
            std::map<UnitsLength, ConversionPtr> c;
            c[enums::ANGSTROM] = Mul(Pow(10, 31), Sym("zettam"));
            c[enums::ASTRONOMICALUNIT] = Mul(Rat(Pow(10, 19), Int(1495978707)), Sym("zettam"));
            c[enums::ATTOMETER] = Mul(Pow(10, 39), Sym("zettam"));
            c[enums::CENTIMETER] = Mul(Pow(10, 23), Sym("zettam"));
            c[enums::DECAMETER] = Mul(Pow(10, 20), Sym("zettam"));
            c[enums::DECIMETER] = Mul(Pow(10, 22), Sym("zettam"));
            c[enums::EXAMETER] = Mul(Int(1000), Sym("zettam"));
            c[enums::FEMTOMETER] = Mul(Pow(10, 36), Sym("zettam"));
            c[enums::FOOT] = Mul(Rat(Mul(Int(9842525), Pow(10, 15)), Int(3)), Sym("zettam"));
            c[enums::GIGAMETER] = Mul(Pow(10, 12), Sym("zettam"));
            c[enums::HECTOMETER] = Mul(Pow(10, 19), Sym("zettam"));
            c[enums::INCH] = Mul(Mul(Int(393701), Pow(10, 17)), Sym("zettam"));
            c[enums::KILOMETER] = Mul(Pow(10, 18), Sym("zettam"));
            c[enums::LIGHTYEAR] = Mul(Rat(Mul(Int(625), Pow(10, 15)), Int("5912956545363")), Sym("zettam"));
            c[enums::LINE] = Mul(Mul(Int(4724412), Pow(10, 17)), Sym("zettam"));
            c[enums::MEGAMETER] = Mul(Pow(10, 15), Sym("zettam"));
            c[enums::METER] = Mul(Pow(10, 21), Sym("zettam"));
            c[enums::MICROMETER] = Mul(Pow(10, 27), Sym("zettam"));
            c[enums::MILE] = Mul(Rat(Mul(Int(559234375), Pow(10, 10)), Int(9)), Sym("zettam"));
            c[enums::MILLIMETER] = Mul(Pow(10, 24), Sym("zettam"));
            c[enums::NANOMETER] = Mul(Pow(10, 30), Sym("zettam"));
            c[enums::PARSEC] = Mul(Rat(Mul(Int(125), Pow(10, 9)), Int(3857097)), Sym("zettam"));
            c[enums::PETAMETER] = Mul(Pow(10, 6), Sym("zettam"));
            c[enums::PICOMETER] = Mul(Pow(10, 33), Sym("zettam"));
            c[enums::POINT] = Mul(Mul(Int(28346472), Pow(10, 17)), Sym("zettam"));
            c[enums::TERAMETER] = Mul(Pow(10, 9), Sym("zettam"));
            c[enums::THOU] = Mul(Mul(Int(393701), Pow(10, 14)), Sym("zettam"));
            c[enums::YARD] = Mul(Rat(Mul(Int(9842525), Pow(10, 15)), Int(9)), Sym("zettam"));
            c[enums::YOCTOMETER] = Mul(Pow(10, 45), Sym("zettam"));
            c[enums::YOTTAMETER] = Mul(Rat(Int(1), Int(1000)), Sym("zettam"));
            c[enums::ZEPTOMETER] = Mul(Pow(10, 42), Sym("zettam"));
            return c;
        }

        static std::map<UnitsLength,
            std::map<UnitsLength, ConversionPtr> > makeConversions() {
            std::map<UnitsLength, std::map<UnitsLength, ConversionPtr> > c;
            c[enums::ANGSTROM] = createMapANGSTROM();
            c[enums::ASTRONOMICALUNIT] = createMapASTRONOMICALUNIT();
            c[enums::ATTOMETER] = createMapATTOMETER();
            c[enums::CENTIMETER] = createMapCENTIMETER();
            c[enums::DECAMETER] = createMapDECAMETER();
            c[enums::DECIMETER] = createMapDECIMETER();
            c[enums::EXAMETER] = createMapEXAMETER();
            c[enums::FEMTOMETER] = createMapFEMTOMETER();
            c[enums::FOOT] = createMapFOOT();
            c[enums::GIGAMETER] = createMapGIGAMETER();
            c[enums::HECTOMETER] = createMapHECTOMETER();
            c[enums::INCH] = createMapINCH();
            c[enums::KILOMETER] = createMapKILOMETER();
            c[enums::LIGHTYEAR] = createMapLIGHTYEAR();
            c[enums::LINE] = createMapLINE();
            c[enums::MEGAMETER] = createMapMEGAMETER();
            c[enums::METER] = createMapMETER();
            c[enums::MICROMETER] = createMapMICROMETER();
            c[enums::MILE] = createMapMILE();
            c[enums::MILLIMETER] = createMapMILLIMETER();
            c[enums::NANOMETER] = createMapNANOMETER();
            c[enums::PARSEC] = createMapPARSEC();
            c[enums::PETAMETER] = createMapPETAMETER();
            c[enums::PICOMETER] = createMapPICOMETER();
            c[enums::POINT] = createMapPOINT();
            c[enums::TERAMETER] = createMapTERAMETER();
            c[enums::THOU] = createMapTHOU();
            c[enums::YARD] = createMapYARD();
            c[enums::YOCTOMETER] = createMapYOCTOMETER();
            c[enums::YOTTAMETER] = createMapYOTTAMETER();
            c[enums::ZEPTOMETER] = createMapZEPTOMETER();
            c[enums::ZETTAMETER] = createMapZETTAMETER();
            return c;
        }

        static std::map<UnitsLength, std::string> makeSymbols(){
            std::map<UnitsLength, std::string> s;
            s[enums::ANGSTROM] = "Å";
            s[enums::ASTRONOMICALUNIT] = "ua";
            s[enums::ATTOMETER] = "am";
            s[enums::CENTIMETER] = "cm";
            s[enums::DECAMETER] = "dam";
            s[enums::DECIMETER] = "dm";
            s[enums::EXAMETER] = "Em";
            s[enums::FEMTOMETER] = "fm";
            s[enums::FOOT] = "ft";
            s[enums::GIGAMETER] = "Gm";
            s[enums::HECTOMETER] = "hm";
            s[enums::INCH] = "in";
            s[enums::KILOMETER] = "km";
            s[enums::LIGHTYEAR] = "ly";
            s[enums::LINE] = "li";
            s[enums::MEGAMETER] = "Mm";
            s[enums::METER] = "m";
            s[enums::MICROMETER] = "µm";
            s[enums::MILE] = "mi";
            s[enums::MILLIMETER] = "mm";
            s[enums::NANOMETER] = "nm";
            s[enums::PARSEC] = "pc";
            s[enums::PETAMETER] = "Pm";
            s[enums::PICOMETER] = "pm";
            s[enums::PIXEL] = "pixel";
            s[enums::POINT] = "pt";
            s[enums::REFERENCEFRAME] = "reference frame";
            s[enums::TERAMETER] = "Tm";
            s[enums::THOU] = "thou";
            s[enums::YARD] = "yd";
            s[enums::YOCTOMETER] = "ym";
            s[enums::YOTTAMETER] = "Ym";
            s[enums::ZEPTOMETER] = "zm";
            s[enums::ZETTAMETER] = "Zm";
            return s;
        }

        std::map<UnitsLength,
            std::map<UnitsLength, ConversionPtr> > LengthI::CONVERSIONS = makeConversions();

        std::map<UnitsLength, std::string> LengthI::SYMBOLS = makeSymbols();

        LengthI::~LengthI() {}

        LengthI::LengthI() : Length() {
        }

        LengthI::LengthI(const double& value, const UnitsLength& unit) : Length() {
            setValue(value);
            setUnit(unit);
        }

        LengthI::LengthI(const LengthPtr& value, const UnitsLength& target) : Length() {
            double orig = value->getValue();
            UnitsLength source = value->getUnit();
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

        Ice::Double LengthI::getValue(const Ice::Current& /* current */) {
            return value;
        }

        void LengthI::setValue(Ice::Double _value, const Ice::Current& /* current */) {
            value = _value;
        }

        UnitsLength LengthI::getUnit(const Ice::Current& /* current */) {
            return unit;
        }

        void LengthI::setUnit(UnitsLength _unit, const Ice::Current& /* current */) {
            unit = _unit;
        }

        std::string LengthI::getSymbol(const Ice::Current& /* current */) {
            return SYMBOLS[unit];
        }

        LengthPtr LengthI::copy(const Ice::Current& /* current */) {
            LengthPtr copy = new LengthI();
            copy->setValue(getValue());
            copy->setUnit(getUnit());
            return copy;
        }
    }
}

