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

#include <omero/model/TimeI.h>
#include <omero/ClientErrors.h>

::Ice::Object* IceInternal::upCast(::omero::model::TimeI* t) { return t; }

using namespace omero::conversions;

typedef omero::conversion_types::ConversionPtr ConversionPtr;
typedef omero::model::enums::UnitsTime UnitsTime;

namespace omero {

    namespace model {

        static std::map<UnitsTime, ConversionPtr> createMapATTOSECOND() {
            std::map<UnitsTime, ConversionPtr> c;
            c[enums::CENTISECOND] = Mul(Rat(Int(1), Pow(10, 16)), Sym("attos"));
            c[enums::DAY] = Mul(Rat(Int(1), Mul(Int(864), Pow(10, 20))), Sym("attos"));
            c[enums::DECASECOND] = Mul(Rat(Int(1), Pow(10, 19)), Sym("attos"));
            c[enums::DECISECOND] = Mul(Rat(Int(1), Pow(10, 17)), Sym("attos"));
            c[enums::EXASECOND] = Mul(Rat(Int(1), Pow(10, 36)), Sym("attos"));
            c[enums::FEMTOSECOND] = Mul(Rat(Int(1), Int(1000)), Sym("attos"));
            c[enums::GIGASECOND] = Mul(Rat(Int(1), Pow(10, 27)), Sym("attos"));
            c[enums::HECTOSECOND] = Mul(Rat(Int(1), Pow(10, 20)), Sym("attos"));
            c[enums::HOUR] = Mul(Rat(Int(1), Mul(Int(36), Pow(10, 20))), Sym("attos"));
            c[enums::KILOSECOND] = Mul(Rat(Int(1), Pow(10, 21)), Sym("attos"));
            c[enums::MEGASECOND] = Mul(Rat(Int(1), Pow(10, 24)), Sym("attos"));
            c[enums::MICROSECOND] = Mul(Rat(Int(1), Pow(10, 12)), Sym("attos"));
            c[enums::MILLISECOND] = Mul(Rat(Int(1), Pow(10, 15)), Sym("attos"));
            c[enums::MINUTE] = Mul(Rat(Int(1), Mul(Int(6), Pow(10, 19))), Sym("attos"));
            c[enums::NANOSECOND] = Mul(Rat(Int(1), Pow(10, 9)), Sym("attos"));
            c[enums::PETASECOND] = Mul(Rat(Int(1), Pow(10, 33)), Sym("attos"));
            c[enums::PICOSECOND] = Mul(Rat(Int(1), Pow(10, 6)), Sym("attos"));
            c[enums::SECOND] = Mul(Rat(Int(1), Pow(10, 18)), Sym("attos"));
            c[enums::TERASECOND] = Mul(Rat(Int(1), Pow(10, 30)), Sym("attos"));
            c[enums::YOCTOSECOND] = Mul(Pow(10, 6), Sym("attos"));
            c[enums::YOTTASECOND] = Mul(Rat(Int(1), Pow(10, 42)), Sym("attos"));
            c[enums::ZEPTOSECOND] = Mul(Int(1000), Sym("attos"));
            c[enums::ZETTASECOND] = Mul(Rat(Int(1), Pow(10, 39)), Sym("attos"));
            return c;
        }

        static std::map<UnitsTime, ConversionPtr> createMapCENTISECOND() {
            std::map<UnitsTime, ConversionPtr> c;
            c[enums::ATTOSECOND] = Mul(Pow(10, 16), Sym("centis"));
            c[enums::DAY] = Mul(Rat(Int(1), Mul(Int(864), Pow(10, 4))), Sym("centis"));
            c[enums::DECASECOND] = Mul(Rat(Int(1), Int(1000)), Sym("centis"));
            c[enums::DECISECOND] = Mul(Rat(Int(1), Int(10)), Sym("centis"));
            c[enums::EXASECOND] = Mul(Rat(Int(1), Pow(10, 20)), Sym("centis"));
            c[enums::FEMTOSECOND] = Mul(Pow(10, 13), Sym("centis"));
            c[enums::GIGASECOND] = Mul(Rat(Int(1), Pow(10, 11)), Sym("centis"));
            c[enums::HECTOSECOND] = Mul(Rat(Int(1), Pow(10, 4)), Sym("centis"));
            c[enums::HOUR] = Mul(Rat(Int(1), Mul(Int(36), Pow(10, 4))), Sym("centis"));
            c[enums::KILOSECOND] = Mul(Rat(Int(1), Pow(10, 5)), Sym("centis"));
            c[enums::MEGASECOND] = Mul(Rat(Int(1), Pow(10, 8)), Sym("centis"));
            c[enums::MICROSECOND] = Mul(Pow(10, 4), Sym("centis"));
            c[enums::MILLISECOND] = Mul(Int(10), Sym("centis"));
            c[enums::MINUTE] = Mul(Rat(Int(1), Int(6000)), Sym("centis"));
            c[enums::NANOSECOND] = Mul(Pow(10, 7), Sym("centis"));
            c[enums::PETASECOND] = Mul(Rat(Int(1), Pow(10, 17)), Sym("centis"));
            c[enums::PICOSECOND] = Mul(Pow(10, 10), Sym("centis"));
            c[enums::SECOND] = Mul(Rat(Int(1), Int(100)), Sym("centis"));
            c[enums::TERASECOND] = Mul(Rat(Int(1), Pow(10, 14)), Sym("centis"));
            c[enums::YOCTOSECOND] = Mul(Pow(10, 22), Sym("centis"));
            c[enums::YOTTASECOND] = Mul(Rat(Int(1), Pow(10, 26)), Sym("centis"));
            c[enums::ZEPTOSECOND] = Mul(Pow(10, 19), Sym("centis"));
            c[enums::ZETTASECOND] = Mul(Rat(Int(1), Pow(10, 23)), Sym("centis"));
            return c;
        }

        static std::map<UnitsTime, ConversionPtr> createMapDAY() {
            std::map<UnitsTime, ConversionPtr> c;
            c[enums::ATTOSECOND] = Mul(Mul(Int(864), Pow(10, 20)), Sym("d"));
            c[enums::CENTISECOND] = Mul(Mul(Int(864), Pow(10, 4)), Sym("d"));
            c[enums::DECASECOND] = Mul(Int(8640), Sym("d"));
            c[enums::DECISECOND] = Mul(Int(864000), Sym("d"));
            c[enums::EXASECOND] = Mul(Rat(Int(27), Mul(Int(3125), Pow(10, 11))), Sym("d"));
            c[enums::FEMTOSECOND] = Mul(Mul(Int(864), Pow(10, 17)), Sym("d"));
            c[enums::GIGASECOND] = Mul(Rat(Int(27), Int(312500)), Sym("d"));
            c[enums::HECTOSECOND] = Mul(Int(864), Sym("d"));
            c[enums::HOUR] = Mul(Int(24), Sym("d"));
            c[enums::KILOSECOND] = Mul(Rat(Int(432), Int(5)), Sym("d"));
            c[enums::MEGASECOND] = Mul(Rat(Int(54), Int(625)), Sym("d"));
            c[enums::MICROSECOND] = Mul(Mul(Int(864), Pow(10, 8)), Sym("d"));
            c[enums::MILLISECOND] = Mul(Mul(Int(864), Pow(10, 5)), Sym("d"));
            c[enums::MINUTE] = Mul(Int(1440), Sym("d"));
            c[enums::NANOSECOND] = Mul(Mul(Int(864), Pow(10, 11)), Sym("d"));
            c[enums::PETASECOND] = Mul(Rat(Int(27), Mul(Int(3125), Pow(10, 8))), Sym("d"));
            c[enums::PICOSECOND] = Mul(Mul(Int(864), Pow(10, 14)), Sym("d"));
            c[enums::SECOND] = Mul(Int(86400), Sym("d"));
            c[enums::TERASECOND] = Mul(Rat(Int(27), Mul(Int(3125), Pow(10, 5))), Sym("d"));
            c[enums::YOCTOSECOND] = Mul(Mul(Int(864), Pow(10, 26)), Sym("d"));
            c[enums::YOTTASECOND] = Mul(Rat(Int(27), Mul(Int(3125), Pow(10, 17))), Sym("d"));
            c[enums::ZEPTOSECOND] = Mul(Mul(Int(864), Pow(10, 23)), Sym("d"));
            c[enums::ZETTASECOND] = Mul(Rat(Int(27), Mul(Int(3125), Pow(10, 14))), Sym("d"));
            return c;
        }

        static std::map<UnitsTime, ConversionPtr> createMapDECASECOND() {
            std::map<UnitsTime, ConversionPtr> c;
            c[enums::ATTOSECOND] = Mul(Pow(10, 19), Sym("decas"));
            c[enums::CENTISECOND] = Mul(Int(1000), Sym("decas"));
            c[enums::DAY] = Mul(Rat(Int(1), Int(8640)), Sym("decas"));
            c[enums::DECISECOND] = Mul(Int(100), Sym("decas"));
            c[enums::EXASECOND] = Mul(Rat(Int(1), Pow(10, 17)), Sym("decas"));
            c[enums::FEMTOSECOND] = Mul(Pow(10, 16), Sym("decas"));
            c[enums::GIGASECOND] = Mul(Rat(Int(1), Pow(10, 8)), Sym("decas"));
            c[enums::HECTOSECOND] = Mul(Rat(Int(1), Int(10)), Sym("decas"));
            c[enums::HOUR] = Mul(Rat(Int(1), Int(360)), Sym("decas"));
            c[enums::KILOSECOND] = Mul(Rat(Int(1), Int(100)), Sym("decas"));
            c[enums::MEGASECOND] = Mul(Rat(Int(1), Pow(10, 5)), Sym("decas"));
            c[enums::MICROSECOND] = Mul(Pow(10, 7), Sym("decas"));
            c[enums::MILLISECOND] = Mul(Pow(10, 4), Sym("decas"));
            c[enums::MINUTE] = Mul(Rat(Int(1), Int(6)), Sym("decas"));
            c[enums::NANOSECOND] = Mul(Pow(10, 10), Sym("decas"));
            c[enums::PETASECOND] = Mul(Rat(Int(1), Pow(10, 14)), Sym("decas"));
            c[enums::PICOSECOND] = Mul(Pow(10, 13), Sym("decas"));
            c[enums::SECOND] = Mul(Int(10), Sym("decas"));
            c[enums::TERASECOND] = Mul(Rat(Int(1), Pow(10, 11)), Sym("decas"));
            c[enums::YOCTOSECOND] = Mul(Pow(10, 25), Sym("decas"));
            c[enums::YOTTASECOND] = Mul(Rat(Int(1), Pow(10, 23)), Sym("decas"));
            c[enums::ZEPTOSECOND] = Mul(Pow(10, 22), Sym("decas"));
            c[enums::ZETTASECOND] = Mul(Rat(Int(1), Pow(10, 20)), Sym("decas"));
            return c;
        }

        static std::map<UnitsTime, ConversionPtr> createMapDECISECOND() {
            std::map<UnitsTime, ConversionPtr> c;
            c[enums::ATTOSECOND] = Mul(Pow(10, 17), Sym("decis"));
            c[enums::CENTISECOND] = Mul(Int(10), Sym("decis"));
            c[enums::DAY] = Mul(Rat(Int(1), Int(864000)), Sym("decis"));
            c[enums::DECASECOND] = Mul(Rat(Int(1), Int(100)), Sym("decis"));
            c[enums::EXASECOND] = Mul(Rat(Int(1), Pow(10, 19)), Sym("decis"));
            c[enums::FEMTOSECOND] = Mul(Pow(10, 14), Sym("decis"));
            c[enums::GIGASECOND] = Mul(Rat(Int(1), Pow(10, 10)), Sym("decis"));
            c[enums::HECTOSECOND] = Mul(Rat(Int(1), Int(1000)), Sym("decis"));
            c[enums::HOUR] = Mul(Rat(Int(1), Int(36000)), Sym("decis"));
            c[enums::KILOSECOND] = Mul(Rat(Int(1), Pow(10, 4)), Sym("decis"));
            c[enums::MEGASECOND] = Mul(Rat(Int(1), Pow(10, 7)), Sym("decis"));
            c[enums::MICROSECOND] = Mul(Pow(10, 5), Sym("decis"));
            c[enums::MILLISECOND] = Mul(Int(100), Sym("decis"));
            c[enums::MINUTE] = Mul(Rat(Int(1), Int(600)), Sym("decis"));
            c[enums::NANOSECOND] = Mul(Pow(10, 8), Sym("decis"));
            c[enums::PETASECOND] = Mul(Rat(Int(1), Pow(10, 16)), Sym("decis"));
            c[enums::PICOSECOND] = Mul(Pow(10, 11), Sym("decis"));
            c[enums::SECOND] = Mul(Rat(Int(1), Int(10)), Sym("decis"));
            c[enums::TERASECOND] = Mul(Rat(Int(1), Pow(10, 13)), Sym("decis"));
            c[enums::YOCTOSECOND] = Mul(Pow(10, 23), Sym("decis"));
            c[enums::YOTTASECOND] = Mul(Rat(Int(1), Pow(10, 25)), Sym("decis"));
            c[enums::ZEPTOSECOND] = Mul(Pow(10, 20), Sym("decis"));
            c[enums::ZETTASECOND] = Mul(Rat(Int(1), Pow(10, 22)), Sym("decis"));
            return c;
        }

        static std::map<UnitsTime, ConversionPtr> createMapEXASECOND() {
            std::map<UnitsTime, ConversionPtr> c;
            c[enums::ATTOSECOND] = Mul(Pow(10, 36), Sym("exas"));
            c[enums::CENTISECOND] = Mul(Pow(10, 20), Sym("exas"));
            c[enums::DAY] = Mul(Rat(Mul(Int(3125), Pow(10, 11)), Int(27)), Sym("exas"));
            c[enums::DECASECOND] = Mul(Pow(10, 17), Sym("exas"));
            c[enums::DECISECOND] = Mul(Pow(10, 19), Sym("exas"));
            c[enums::FEMTOSECOND] = Mul(Pow(10, 33), Sym("exas"));
            c[enums::GIGASECOND] = Mul(Pow(10, 9), Sym("exas"));
            c[enums::HECTOSECOND] = Mul(Pow(10, 16), Sym("exas"));
            c[enums::HOUR] = Mul(Rat(Mul(Int(25), Pow(10, 14)), Int(9)), Sym("exas"));
            c[enums::KILOSECOND] = Mul(Pow(10, 15), Sym("exas"));
            c[enums::MEGASECOND] = Mul(Pow(10, 12), Sym("exas"));
            c[enums::MICROSECOND] = Mul(Pow(10, 24), Sym("exas"));
            c[enums::MILLISECOND] = Mul(Pow(10, 21), Sym("exas"));
            c[enums::MINUTE] = Mul(Rat(Mul(Int(5), Pow(10, 16)), Int(3)), Sym("exas"));
            c[enums::NANOSECOND] = Mul(Pow(10, 27), Sym("exas"));
            c[enums::PETASECOND] = Mul(Int(1000), Sym("exas"));
            c[enums::PICOSECOND] = Mul(Pow(10, 30), Sym("exas"));
            c[enums::SECOND] = Mul(Pow(10, 18), Sym("exas"));
            c[enums::TERASECOND] = Mul(Pow(10, 6), Sym("exas"));
            c[enums::YOCTOSECOND] = Mul(Pow(10, 42), Sym("exas"));
            c[enums::YOTTASECOND] = Mul(Rat(Int(1), Pow(10, 6)), Sym("exas"));
            c[enums::ZEPTOSECOND] = Mul(Pow(10, 39), Sym("exas"));
            c[enums::ZETTASECOND] = Mul(Rat(Int(1), Int(1000)), Sym("exas"));
            return c;
        }

        static std::map<UnitsTime, ConversionPtr> createMapFEMTOSECOND() {
            std::map<UnitsTime, ConversionPtr> c;
            c[enums::ATTOSECOND] = Mul(Int(1000), Sym("femtos"));
            c[enums::CENTISECOND] = Mul(Rat(Int(1), Pow(10, 13)), Sym("femtos"));
            c[enums::DAY] = Mul(Rat(Int(1), Mul(Int(864), Pow(10, 17))), Sym("femtos"));
            c[enums::DECASECOND] = Mul(Rat(Int(1), Pow(10, 16)), Sym("femtos"));
            c[enums::DECISECOND] = Mul(Rat(Int(1), Pow(10, 14)), Sym("femtos"));
            c[enums::EXASECOND] = Mul(Rat(Int(1), Pow(10, 33)), Sym("femtos"));
            c[enums::GIGASECOND] = Mul(Rat(Int(1), Pow(10, 24)), Sym("femtos"));
            c[enums::HECTOSECOND] = Mul(Rat(Int(1), Pow(10, 17)), Sym("femtos"));
            c[enums::HOUR] = Mul(Rat(Int(1), Mul(Int(36), Pow(10, 17))), Sym("femtos"));
            c[enums::KILOSECOND] = Mul(Rat(Int(1), Pow(10, 18)), Sym("femtos"));
            c[enums::MEGASECOND] = Mul(Rat(Int(1), Pow(10, 21)), Sym("femtos"));
            c[enums::MICROSECOND] = Mul(Rat(Int(1), Pow(10, 9)), Sym("femtos"));
            c[enums::MILLISECOND] = Mul(Rat(Int(1), Pow(10, 12)), Sym("femtos"));
            c[enums::MINUTE] = Mul(Rat(Int(1), Mul(Int(6), Pow(10, 16))), Sym("femtos"));
            c[enums::NANOSECOND] = Mul(Rat(Int(1), Pow(10, 6)), Sym("femtos"));
            c[enums::PETASECOND] = Mul(Rat(Int(1), Pow(10, 30)), Sym("femtos"));
            c[enums::PICOSECOND] = Mul(Rat(Int(1), Int(1000)), Sym("femtos"));
            c[enums::SECOND] = Mul(Rat(Int(1), Pow(10, 15)), Sym("femtos"));
            c[enums::TERASECOND] = Mul(Rat(Int(1), Pow(10, 27)), Sym("femtos"));
            c[enums::YOCTOSECOND] = Mul(Pow(10, 9), Sym("femtos"));
            c[enums::YOTTASECOND] = Mul(Rat(Int(1), Pow(10, 39)), Sym("femtos"));
            c[enums::ZEPTOSECOND] = Mul(Pow(10, 6), Sym("femtos"));
            c[enums::ZETTASECOND] = Mul(Rat(Int(1), Pow(10, 36)), Sym("femtos"));
            return c;
        }

        static std::map<UnitsTime, ConversionPtr> createMapGIGASECOND() {
            std::map<UnitsTime, ConversionPtr> c;
            c[enums::ATTOSECOND] = Mul(Pow(10, 27), Sym("gigas"));
            c[enums::CENTISECOND] = Mul(Pow(10, 11), Sym("gigas"));
            c[enums::DAY] = Mul(Rat(Int(312500), Int(27)), Sym("gigas"));
            c[enums::DECASECOND] = Mul(Pow(10, 8), Sym("gigas"));
            c[enums::DECISECOND] = Mul(Pow(10, 10), Sym("gigas"));
            c[enums::EXASECOND] = Mul(Rat(Int(1), Pow(10, 9)), Sym("gigas"));
            c[enums::FEMTOSECOND] = Mul(Pow(10, 24), Sym("gigas"));
            c[enums::HECTOSECOND] = Mul(Pow(10, 7), Sym("gigas"));
            c[enums::HOUR] = Mul(Rat(Mul(Int(25), Pow(10, 5)), Int(9)), Sym("gigas"));
            c[enums::KILOSECOND] = Mul(Pow(10, 6), Sym("gigas"));
            c[enums::MEGASECOND] = Mul(Int(1000), Sym("gigas"));
            c[enums::MICROSECOND] = Mul(Pow(10, 15), Sym("gigas"));
            c[enums::MILLISECOND] = Mul(Pow(10, 12), Sym("gigas"));
            c[enums::MINUTE] = Mul(Rat(Mul(Int(5), Pow(10, 7)), Int(3)), Sym("gigas"));
            c[enums::NANOSECOND] = Mul(Pow(10, 18), Sym("gigas"));
            c[enums::PETASECOND] = Mul(Rat(Int(1), Pow(10, 6)), Sym("gigas"));
            c[enums::PICOSECOND] = Mul(Pow(10, 21), Sym("gigas"));
            c[enums::SECOND] = Mul(Pow(10, 9), Sym("gigas"));
            c[enums::TERASECOND] = Mul(Rat(Int(1), Int(1000)), Sym("gigas"));
            c[enums::YOCTOSECOND] = Mul(Pow(10, 33), Sym("gigas"));
            c[enums::YOTTASECOND] = Mul(Rat(Int(1), Pow(10, 15)), Sym("gigas"));
            c[enums::ZEPTOSECOND] = Mul(Pow(10, 30), Sym("gigas"));
            c[enums::ZETTASECOND] = Mul(Rat(Int(1), Pow(10, 12)), Sym("gigas"));
            return c;
        }

        static std::map<UnitsTime, ConversionPtr> createMapHECTOSECOND() {
            std::map<UnitsTime, ConversionPtr> c;
            c[enums::ATTOSECOND] = Mul(Pow(10, 20), Sym("hectos"));
            c[enums::CENTISECOND] = Mul(Pow(10, 4), Sym("hectos"));
            c[enums::DAY] = Mul(Rat(Int(1), Int(864)), Sym("hectos"));
            c[enums::DECASECOND] = Mul(Int(10), Sym("hectos"));
            c[enums::DECISECOND] = Mul(Int(1000), Sym("hectos"));
            c[enums::EXASECOND] = Mul(Rat(Int(1), Pow(10, 16)), Sym("hectos"));
            c[enums::FEMTOSECOND] = Mul(Pow(10, 17), Sym("hectos"));
            c[enums::GIGASECOND] = Mul(Rat(Int(1), Pow(10, 7)), Sym("hectos"));
            c[enums::HOUR] = Mul(Rat(Int(1), Int(36)), Sym("hectos"));
            c[enums::KILOSECOND] = Mul(Rat(Int(1), Int(10)), Sym("hectos"));
            c[enums::MEGASECOND] = Mul(Rat(Int(1), Pow(10, 4)), Sym("hectos"));
            c[enums::MICROSECOND] = Mul(Pow(10, 8), Sym("hectos"));
            c[enums::MILLISECOND] = Mul(Pow(10, 5), Sym("hectos"));
            c[enums::MINUTE] = Mul(Rat(Int(5), Int(3)), Sym("hectos"));
            c[enums::NANOSECOND] = Mul(Pow(10, 11), Sym("hectos"));
            c[enums::PETASECOND] = Mul(Rat(Int(1), Pow(10, 13)), Sym("hectos"));
            c[enums::PICOSECOND] = Mul(Pow(10, 14), Sym("hectos"));
            c[enums::SECOND] = Mul(Int(100), Sym("hectos"));
            c[enums::TERASECOND] = Mul(Rat(Int(1), Pow(10, 10)), Sym("hectos"));
            c[enums::YOCTOSECOND] = Mul(Pow(10, 26), Sym("hectos"));
            c[enums::YOTTASECOND] = Mul(Rat(Int(1), Pow(10, 22)), Sym("hectos"));
            c[enums::ZEPTOSECOND] = Mul(Pow(10, 23), Sym("hectos"));
            c[enums::ZETTASECOND] = Mul(Rat(Int(1), Pow(10, 19)), Sym("hectos"));
            return c;
        }

        static std::map<UnitsTime, ConversionPtr> createMapHOUR() {
            std::map<UnitsTime, ConversionPtr> c;
            c[enums::ATTOSECOND] = Mul(Mul(Int(36), Pow(10, 20)), Sym("h"));
            c[enums::CENTISECOND] = Mul(Mul(Int(36), Pow(10, 4)), Sym("h"));
            c[enums::DAY] = Mul(Rat(Int(1), Int(24)), Sym("h"));
            c[enums::DECASECOND] = Mul(Int(360), Sym("h"));
            c[enums::DECISECOND] = Mul(Int(36000), Sym("h"));
            c[enums::EXASECOND] = Mul(Rat(Int(9), Mul(Int(25), Pow(10, 14))), Sym("h"));
            c[enums::FEMTOSECOND] = Mul(Mul(Int(36), Pow(10, 17)), Sym("h"));
            c[enums::GIGASECOND] = Mul(Rat(Int(9), Mul(Int(25), Pow(10, 5))), Sym("h"));
            c[enums::HECTOSECOND] = Mul(Int(36), Sym("h"));
            c[enums::KILOSECOND] = Mul(Rat(Int(18), Int(5)), Sym("h"));
            c[enums::MEGASECOND] = Mul(Rat(Int(9), Int(2500)), Sym("h"));
            c[enums::MICROSECOND] = Mul(Mul(Int(36), Pow(10, 8)), Sym("h"));
            c[enums::MILLISECOND] = Mul(Mul(Int(36), Pow(10, 5)), Sym("h"));
            c[enums::MINUTE] = Mul(Int(60), Sym("h"));
            c[enums::NANOSECOND] = Mul(Mul(Int(36), Pow(10, 11)), Sym("h"));
            c[enums::PETASECOND] = Mul(Rat(Int(9), Mul(Int(25), Pow(10, 11))), Sym("h"));
            c[enums::PICOSECOND] = Mul(Mul(Int(36), Pow(10, 14)), Sym("h"));
            c[enums::SECOND] = Mul(Int(3600), Sym("h"));
            c[enums::TERASECOND] = Mul(Rat(Int(9), Mul(Int(25), Pow(10, 8))), Sym("h"));
            c[enums::YOCTOSECOND] = Mul(Mul(Int(36), Pow(10, 26)), Sym("h"));
            c[enums::YOTTASECOND] = Mul(Rat(Int(9), Mul(Int(25), Pow(10, 20))), Sym("h"));
            c[enums::ZEPTOSECOND] = Mul(Mul(Int(36), Pow(10, 23)), Sym("h"));
            c[enums::ZETTASECOND] = Mul(Rat(Int(9), Mul(Int(25), Pow(10, 17))), Sym("h"));
            return c;
        }

        static std::map<UnitsTime, ConversionPtr> createMapKILOSECOND() {
            std::map<UnitsTime, ConversionPtr> c;
            c[enums::ATTOSECOND] = Mul(Pow(10, 21), Sym("kilos"));
            c[enums::CENTISECOND] = Mul(Pow(10, 5), Sym("kilos"));
            c[enums::DAY] = Mul(Rat(Int(5), Int(432)), Sym("kilos"));
            c[enums::DECASECOND] = Mul(Int(100), Sym("kilos"));
            c[enums::DECISECOND] = Mul(Pow(10, 4), Sym("kilos"));
            c[enums::EXASECOND] = Mul(Rat(Int(1), Pow(10, 15)), Sym("kilos"));
            c[enums::FEMTOSECOND] = Mul(Pow(10, 18), Sym("kilos"));
            c[enums::GIGASECOND] = Mul(Rat(Int(1), Pow(10, 6)), Sym("kilos"));
            c[enums::HECTOSECOND] = Mul(Int(10), Sym("kilos"));
            c[enums::HOUR] = Mul(Rat(Int(5), Int(18)), Sym("kilos"));
            c[enums::MEGASECOND] = Mul(Rat(Int(1), Int(1000)), Sym("kilos"));
            c[enums::MICROSECOND] = Mul(Pow(10, 9), Sym("kilos"));
            c[enums::MILLISECOND] = Mul(Pow(10, 6), Sym("kilos"));
            c[enums::MINUTE] = Mul(Rat(Int(50), Int(3)), Sym("kilos"));
            c[enums::NANOSECOND] = Mul(Pow(10, 12), Sym("kilos"));
            c[enums::PETASECOND] = Mul(Rat(Int(1), Pow(10, 12)), Sym("kilos"));
            c[enums::PICOSECOND] = Mul(Pow(10, 15), Sym("kilos"));
            c[enums::SECOND] = Mul(Int(1000), Sym("kilos"));
            c[enums::TERASECOND] = Mul(Rat(Int(1), Pow(10, 9)), Sym("kilos"));
            c[enums::YOCTOSECOND] = Mul(Pow(10, 27), Sym("kilos"));
            c[enums::YOTTASECOND] = Mul(Rat(Int(1), Pow(10, 21)), Sym("kilos"));
            c[enums::ZEPTOSECOND] = Mul(Pow(10, 24), Sym("kilos"));
            c[enums::ZETTASECOND] = Mul(Rat(Int(1), Pow(10, 18)), Sym("kilos"));
            return c;
        }

        static std::map<UnitsTime, ConversionPtr> createMapMEGASECOND() {
            std::map<UnitsTime, ConversionPtr> c;
            c[enums::ATTOSECOND] = Mul(Pow(10, 24), Sym("megas"));
            c[enums::CENTISECOND] = Mul(Pow(10, 8), Sym("megas"));
            c[enums::DAY] = Mul(Rat(Int(625), Int(54)), Sym("megas"));
            c[enums::DECASECOND] = Mul(Pow(10, 5), Sym("megas"));
            c[enums::DECISECOND] = Mul(Pow(10, 7), Sym("megas"));
            c[enums::EXASECOND] = Mul(Rat(Int(1), Pow(10, 12)), Sym("megas"));
            c[enums::FEMTOSECOND] = Mul(Pow(10, 21), Sym("megas"));
            c[enums::GIGASECOND] = Mul(Rat(Int(1), Int(1000)), Sym("megas"));
            c[enums::HECTOSECOND] = Mul(Pow(10, 4), Sym("megas"));
            c[enums::HOUR] = Mul(Rat(Int(2500), Int(9)), Sym("megas"));
            c[enums::KILOSECOND] = Mul(Int(1000), Sym("megas"));
            c[enums::MICROSECOND] = Mul(Pow(10, 12), Sym("megas"));
            c[enums::MILLISECOND] = Mul(Pow(10, 9), Sym("megas"));
            c[enums::MINUTE] = Mul(Rat(Mul(Int(5), Pow(10, 4)), Int(3)), Sym("megas"));
            c[enums::NANOSECOND] = Mul(Pow(10, 15), Sym("megas"));
            c[enums::PETASECOND] = Mul(Rat(Int(1), Pow(10, 9)), Sym("megas"));
            c[enums::PICOSECOND] = Mul(Pow(10, 18), Sym("megas"));
            c[enums::SECOND] = Mul(Pow(10, 6), Sym("megas"));
            c[enums::TERASECOND] = Mul(Rat(Int(1), Pow(10, 6)), Sym("megas"));
            c[enums::YOCTOSECOND] = Mul(Pow(10, 30), Sym("megas"));
            c[enums::YOTTASECOND] = Mul(Rat(Int(1), Pow(10, 18)), Sym("megas"));
            c[enums::ZEPTOSECOND] = Mul(Pow(10, 27), Sym("megas"));
            c[enums::ZETTASECOND] = Mul(Rat(Int(1), Pow(10, 15)), Sym("megas"));
            return c;
        }

        static std::map<UnitsTime, ConversionPtr> createMapMICROSECOND() {
            std::map<UnitsTime, ConversionPtr> c;
            c[enums::ATTOSECOND] = Mul(Pow(10, 12), Sym("micros"));
            c[enums::CENTISECOND] = Mul(Rat(Int(1), Pow(10, 4)), Sym("micros"));
            c[enums::DAY] = Mul(Rat(Int(1), Mul(Int(864), Pow(10, 8))), Sym("micros"));
            c[enums::DECASECOND] = Mul(Rat(Int(1), Pow(10, 7)), Sym("micros"));
            c[enums::DECISECOND] = Mul(Rat(Int(1), Pow(10, 5)), Sym("micros"));
            c[enums::EXASECOND] = Mul(Rat(Int(1), Pow(10, 24)), Sym("micros"));
            c[enums::FEMTOSECOND] = Mul(Pow(10, 9), Sym("micros"));
            c[enums::GIGASECOND] = Mul(Rat(Int(1), Pow(10, 15)), Sym("micros"));
            c[enums::HECTOSECOND] = Mul(Rat(Int(1), Pow(10, 8)), Sym("micros"));
            c[enums::HOUR] = Mul(Rat(Int(1), Mul(Int(36), Pow(10, 8))), Sym("micros"));
            c[enums::KILOSECOND] = Mul(Rat(Int(1), Pow(10, 9)), Sym("micros"));
            c[enums::MEGASECOND] = Mul(Rat(Int(1), Pow(10, 12)), Sym("micros"));
            c[enums::MILLISECOND] = Mul(Rat(Int(1), Int(1000)), Sym("micros"));
            c[enums::MINUTE] = Mul(Rat(Int(1), Mul(Int(6), Pow(10, 7))), Sym("micros"));
            c[enums::NANOSECOND] = Mul(Int(1000), Sym("micros"));
            c[enums::PETASECOND] = Mul(Rat(Int(1), Pow(10, 21)), Sym("micros"));
            c[enums::PICOSECOND] = Mul(Pow(10, 6), Sym("micros"));
            c[enums::SECOND] = Mul(Rat(Int(1), Pow(10, 6)), Sym("micros"));
            c[enums::TERASECOND] = Mul(Rat(Int(1), Pow(10, 18)), Sym("micros"));
            c[enums::YOCTOSECOND] = Mul(Pow(10, 18), Sym("micros"));
            c[enums::YOTTASECOND] = Mul(Rat(Int(1), Pow(10, 30)), Sym("micros"));
            c[enums::ZEPTOSECOND] = Mul(Pow(10, 15), Sym("micros"));
            c[enums::ZETTASECOND] = Mul(Rat(Int(1), Pow(10, 27)), Sym("micros"));
            return c;
        }

        static std::map<UnitsTime, ConversionPtr> createMapMILLISECOND() {
            std::map<UnitsTime, ConversionPtr> c;
            c[enums::ATTOSECOND] = Mul(Pow(10, 15), Sym("millis"));
            c[enums::CENTISECOND] = Mul(Rat(Int(1), Int(10)), Sym("millis"));
            c[enums::DAY] = Mul(Rat(Int(1), Mul(Int(864), Pow(10, 5))), Sym("millis"));
            c[enums::DECASECOND] = Mul(Rat(Int(1), Pow(10, 4)), Sym("millis"));
            c[enums::DECISECOND] = Mul(Rat(Int(1), Int(100)), Sym("millis"));
            c[enums::EXASECOND] = Mul(Rat(Int(1), Pow(10, 21)), Sym("millis"));
            c[enums::FEMTOSECOND] = Mul(Pow(10, 12), Sym("millis"));
            c[enums::GIGASECOND] = Mul(Rat(Int(1), Pow(10, 12)), Sym("millis"));
            c[enums::HECTOSECOND] = Mul(Rat(Int(1), Pow(10, 5)), Sym("millis"));
            c[enums::HOUR] = Mul(Rat(Int(1), Mul(Int(36), Pow(10, 5))), Sym("millis"));
            c[enums::KILOSECOND] = Mul(Rat(Int(1), Pow(10, 6)), Sym("millis"));
            c[enums::MEGASECOND] = Mul(Rat(Int(1), Pow(10, 9)), Sym("millis"));
            c[enums::MICROSECOND] = Mul(Int(1000), Sym("millis"));
            c[enums::MINUTE] = Mul(Rat(Int(1), Mul(Int(6), Pow(10, 4))), Sym("millis"));
            c[enums::NANOSECOND] = Mul(Pow(10, 6), Sym("millis"));
            c[enums::PETASECOND] = Mul(Rat(Int(1), Pow(10, 18)), Sym("millis"));
            c[enums::PICOSECOND] = Mul(Pow(10, 9), Sym("millis"));
            c[enums::SECOND] = Mul(Rat(Int(1), Int(1000)), Sym("millis"));
            c[enums::TERASECOND] = Mul(Rat(Int(1), Pow(10, 15)), Sym("millis"));
            c[enums::YOCTOSECOND] = Mul(Pow(10, 21), Sym("millis"));
            c[enums::YOTTASECOND] = Mul(Rat(Int(1), Pow(10, 27)), Sym("millis"));
            c[enums::ZEPTOSECOND] = Mul(Pow(10, 18), Sym("millis"));
            c[enums::ZETTASECOND] = Mul(Rat(Int(1), Pow(10, 24)), Sym("millis"));
            return c;
        }

        static std::map<UnitsTime, ConversionPtr> createMapMINUTE() {
            std::map<UnitsTime, ConversionPtr> c;
            c[enums::ATTOSECOND] = Mul(Mul(Int(6), Pow(10, 19)), Sym("m"));
            c[enums::CENTISECOND] = Mul(Int(6000), Sym("m"));
            c[enums::DAY] = Mul(Rat(Int(1), Int(1440)), Sym("m"));
            c[enums::DECASECOND] = Mul(Int(6), Sym("m"));
            c[enums::DECISECOND] = Mul(Int(600), Sym("m"));
            c[enums::EXASECOND] = Mul(Rat(Int(3), Mul(Int(5), Pow(10, 16))), Sym("m"));
            c[enums::FEMTOSECOND] = Mul(Mul(Int(6), Pow(10, 16)), Sym("m"));
            c[enums::GIGASECOND] = Mul(Rat(Int(3), Mul(Int(5), Pow(10, 7))), Sym("m"));
            c[enums::HECTOSECOND] = Mul(Rat(Int(3), Int(5)), Sym("m"));
            c[enums::HOUR] = Mul(Rat(Int(1), Int(60)), Sym("m"));
            c[enums::KILOSECOND] = Mul(Rat(Int(3), Int(50)), Sym("m"));
            c[enums::MEGASECOND] = Mul(Rat(Int(3), Mul(Int(5), Pow(10, 4))), Sym("m"));
            c[enums::MICROSECOND] = Mul(Mul(Int(6), Pow(10, 7)), Sym("m"));
            c[enums::MILLISECOND] = Mul(Mul(Int(6), Pow(10, 4)), Sym("m"));
            c[enums::NANOSECOND] = Mul(Mul(Int(6), Pow(10, 10)), Sym("m"));
            c[enums::PETASECOND] = Mul(Rat(Int(3), Mul(Int(5), Pow(10, 13))), Sym("m"));
            c[enums::PICOSECOND] = Mul(Mul(Int(6), Pow(10, 13)), Sym("m"));
            c[enums::SECOND] = Mul(Int(60), Sym("m"));
            c[enums::TERASECOND] = Mul(Rat(Int(3), Mul(Int(5), Pow(10, 10))), Sym("m"));
            c[enums::YOCTOSECOND] = Mul(Mul(Int(6), Pow(10, 25)), Sym("m"));
            c[enums::YOTTASECOND] = Mul(Rat(Int(3), Mul(Int(5), Pow(10, 22))), Sym("m"));
            c[enums::ZEPTOSECOND] = Mul(Mul(Int(6), Pow(10, 22)), Sym("m"));
            c[enums::ZETTASECOND] = Mul(Rat(Int(3), Mul(Int(5), Pow(10, 19))), Sym("m"));
            return c;
        }

        static std::map<UnitsTime, ConversionPtr> createMapNANOSECOND() {
            std::map<UnitsTime, ConversionPtr> c;
            c[enums::ATTOSECOND] = Mul(Pow(10, 9), Sym("nanos"));
            c[enums::CENTISECOND] = Mul(Rat(Int(1), Pow(10, 7)), Sym("nanos"));
            c[enums::DAY] = Mul(Rat(Int(1), Mul(Int(864), Pow(10, 11))), Sym("nanos"));
            c[enums::DECASECOND] = Mul(Rat(Int(1), Pow(10, 10)), Sym("nanos"));
            c[enums::DECISECOND] = Mul(Rat(Int(1), Pow(10, 8)), Sym("nanos"));
            c[enums::EXASECOND] = Mul(Rat(Int(1), Pow(10, 27)), Sym("nanos"));
            c[enums::FEMTOSECOND] = Mul(Pow(10, 6), Sym("nanos"));
            c[enums::GIGASECOND] = Mul(Rat(Int(1), Pow(10, 18)), Sym("nanos"));
            c[enums::HECTOSECOND] = Mul(Rat(Int(1), Pow(10, 11)), Sym("nanos"));
            c[enums::HOUR] = Mul(Rat(Int(1), Mul(Int(36), Pow(10, 11))), Sym("nanos"));
            c[enums::KILOSECOND] = Mul(Rat(Int(1), Pow(10, 12)), Sym("nanos"));
            c[enums::MEGASECOND] = Mul(Rat(Int(1), Pow(10, 15)), Sym("nanos"));
            c[enums::MICROSECOND] = Mul(Rat(Int(1), Int(1000)), Sym("nanos"));
            c[enums::MILLISECOND] = Mul(Rat(Int(1), Pow(10, 6)), Sym("nanos"));
            c[enums::MINUTE] = Mul(Rat(Int(1), Mul(Int(6), Pow(10, 10))), Sym("nanos"));
            c[enums::PETASECOND] = Mul(Rat(Int(1), Pow(10, 24)), Sym("nanos"));
            c[enums::PICOSECOND] = Mul(Int(1000), Sym("nanos"));
            c[enums::SECOND] = Mul(Rat(Int(1), Pow(10, 9)), Sym("nanos"));
            c[enums::TERASECOND] = Mul(Rat(Int(1), Pow(10, 21)), Sym("nanos"));
            c[enums::YOCTOSECOND] = Mul(Pow(10, 15), Sym("nanos"));
            c[enums::YOTTASECOND] = Mul(Rat(Int(1), Pow(10, 33)), Sym("nanos"));
            c[enums::ZEPTOSECOND] = Mul(Pow(10, 12), Sym("nanos"));
            c[enums::ZETTASECOND] = Mul(Rat(Int(1), Pow(10, 30)), Sym("nanos"));
            return c;
        }

        static std::map<UnitsTime, ConversionPtr> createMapPETASECOND() {
            std::map<UnitsTime, ConversionPtr> c;
            c[enums::ATTOSECOND] = Mul(Pow(10, 33), Sym("petas"));
            c[enums::CENTISECOND] = Mul(Pow(10, 17), Sym("petas"));
            c[enums::DAY] = Mul(Rat(Mul(Int(3125), Pow(10, 8)), Int(27)), Sym("petas"));
            c[enums::DECASECOND] = Mul(Pow(10, 14), Sym("petas"));
            c[enums::DECISECOND] = Mul(Pow(10, 16), Sym("petas"));
            c[enums::EXASECOND] = Mul(Rat(Int(1), Int(1000)), Sym("petas"));
            c[enums::FEMTOSECOND] = Mul(Pow(10, 30), Sym("petas"));
            c[enums::GIGASECOND] = Mul(Pow(10, 6), Sym("petas"));
            c[enums::HECTOSECOND] = Mul(Pow(10, 13), Sym("petas"));
            c[enums::HOUR] = Mul(Rat(Mul(Int(25), Pow(10, 11)), Int(9)), Sym("petas"));
            c[enums::KILOSECOND] = Mul(Pow(10, 12), Sym("petas"));
            c[enums::MEGASECOND] = Mul(Pow(10, 9), Sym("petas"));
            c[enums::MICROSECOND] = Mul(Pow(10, 21), Sym("petas"));
            c[enums::MILLISECOND] = Mul(Pow(10, 18), Sym("petas"));
            c[enums::MINUTE] = Mul(Rat(Mul(Int(5), Pow(10, 13)), Int(3)), Sym("petas"));
            c[enums::NANOSECOND] = Mul(Pow(10, 24), Sym("petas"));
            c[enums::PICOSECOND] = Mul(Pow(10, 27), Sym("petas"));
            c[enums::SECOND] = Mul(Pow(10, 15), Sym("petas"));
            c[enums::TERASECOND] = Mul(Int(1000), Sym("petas"));
            c[enums::YOCTOSECOND] = Mul(Pow(10, 39), Sym("petas"));
            c[enums::YOTTASECOND] = Mul(Rat(Int(1), Pow(10, 9)), Sym("petas"));
            c[enums::ZEPTOSECOND] = Mul(Pow(10, 36), Sym("petas"));
            c[enums::ZETTASECOND] = Mul(Rat(Int(1), Pow(10, 6)), Sym("petas"));
            return c;
        }

        static std::map<UnitsTime, ConversionPtr> createMapPICOSECOND() {
            std::map<UnitsTime, ConversionPtr> c;
            c[enums::ATTOSECOND] = Mul(Pow(10, 6), Sym("picos"));
            c[enums::CENTISECOND] = Mul(Rat(Int(1), Pow(10, 10)), Sym("picos"));
            c[enums::DAY] = Mul(Rat(Int(1), Mul(Int(864), Pow(10, 14))), Sym("picos"));
            c[enums::DECASECOND] = Mul(Rat(Int(1), Pow(10, 13)), Sym("picos"));
            c[enums::DECISECOND] = Mul(Rat(Int(1), Pow(10, 11)), Sym("picos"));
            c[enums::EXASECOND] = Mul(Rat(Int(1), Pow(10, 30)), Sym("picos"));
            c[enums::FEMTOSECOND] = Mul(Int(1000), Sym("picos"));
            c[enums::GIGASECOND] = Mul(Rat(Int(1), Pow(10, 21)), Sym("picos"));
            c[enums::HECTOSECOND] = Mul(Rat(Int(1), Pow(10, 14)), Sym("picos"));
            c[enums::HOUR] = Mul(Rat(Int(1), Mul(Int(36), Pow(10, 14))), Sym("picos"));
            c[enums::KILOSECOND] = Mul(Rat(Int(1), Pow(10, 15)), Sym("picos"));
            c[enums::MEGASECOND] = Mul(Rat(Int(1), Pow(10, 18)), Sym("picos"));
            c[enums::MICROSECOND] = Mul(Rat(Int(1), Pow(10, 6)), Sym("picos"));
            c[enums::MILLISECOND] = Mul(Rat(Int(1), Pow(10, 9)), Sym("picos"));
            c[enums::MINUTE] = Mul(Rat(Int(1), Mul(Int(6), Pow(10, 13))), Sym("picos"));
            c[enums::NANOSECOND] = Mul(Rat(Int(1), Int(1000)), Sym("picos"));
            c[enums::PETASECOND] = Mul(Rat(Int(1), Pow(10, 27)), Sym("picos"));
            c[enums::SECOND] = Mul(Rat(Int(1), Pow(10, 12)), Sym("picos"));
            c[enums::TERASECOND] = Mul(Rat(Int(1), Pow(10, 24)), Sym("picos"));
            c[enums::YOCTOSECOND] = Mul(Pow(10, 12), Sym("picos"));
            c[enums::YOTTASECOND] = Mul(Rat(Int(1), Pow(10, 36)), Sym("picos"));
            c[enums::ZEPTOSECOND] = Mul(Pow(10, 9), Sym("picos"));
            c[enums::ZETTASECOND] = Mul(Rat(Int(1), Pow(10, 33)), Sym("picos"));
            return c;
        }

        static std::map<UnitsTime, ConversionPtr> createMapSECOND() {
            std::map<UnitsTime, ConversionPtr> c;
            c[enums::ATTOSECOND] = Mul(Pow(10, 18), Sym("s"));
            c[enums::CENTISECOND] = Mul(Int(100), Sym("s"));
            c[enums::DAY] = Mul(Rat(Int(1), Int(86400)), Sym("s"));
            c[enums::DECASECOND] = Mul(Rat(Int(1), Int(10)), Sym("s"));
            c[enums::DECISECOND] = Mul(Int(10), Sym("s"));
            c[enums::EXASECOND] = Mul(Rat(Int(1), Pow(10, 18)), Sym("s"));
            c[enums::FEMTOSECOND] = Mul(Pow(10, 15), Sym("s"));
            c[enums::GIGASECOND] = Mul(Rat(Int(1), Pow(10, 9)), Sym("s"));
            c[enums::HECTOSECOND] = Mul(Rat(Int(1), Int(100)), Sym("s"));
            c[enums::HOUR] = Mul(Rat(Int(1), Int(3600)), Sym("s"));
            c[enums::KILOSECOND] = Mul(Rat(Int(1), Int(1000)), Sym("s"));
            c[enums::MEGASECOND] = Mul(Rat(Int(1), Pow(10, 6)), Sym("s"));
            c[enums::MICROSECOND] = Mul(Pow(10, 6), Sym("s"));
            c[enums::MILLISECOND] = Mul(Int(1000), Sym("s"));
            c[enums::MINUTE] = Mul(Rat(Int(1), Int(60)), Sym("s"));
            c[enums::NANOSECOND] = Mul(Pow(10, 9), Sym("s"));
            c[enums::PETASECOND] = Mul(Rat(Int(1), Pow(10, 15)), Sym("s"));
            c[enums::PICOSECOND] = Mul(Pow(10, 12), Sym("s"));
            c[enums::TERASECOND] = Mul(Rat(Int(1), Pow(10, 12)), Sym("s"));
            c[enums::YOCTOSECOND] = Mul(Pow(10, 24), Sym("s"));
            c[enums::YOTTASECOND] = Mul(Rat(Int(1), Pow(10, 24)), Sym("s"));
            c[enums::ZEPTOSECOND] = Mul(Pow(10, 21), Sym("s"));
            c[enums::ZETTASECOND] = Mul(Rat(Int(1), Pow(10, 21)), Sym("s"));
            return c;
        }

        static std::map<UnitsTime, ConversionPtr> createMapTERASECOND() {
            std::map<UnitsTime, ConversionPtr> c;
            c[enums::ATTOSECOND] = Mul(Pow(10, 30), Sym("teras"));
            c[enums::CENTISECOND] = Mul(Pow(10, 14), Sym("teras"));
            c[enums::DAY] = Mul(Rat(Mul(Int(3125), Pow(10, 5)), Int(27)), Sym("teras"));
            c[enums::DECASECOND] = Mul(Pow(10, 11), Sym("teras"));
            c[enums::DECISECOND] = Mul(Pow(10, 13), Sym("teras"));
            c[enums::EXASECOND] = Mul(Rat(Int(1), Pow(10, 6)), Sym("teras"));
            c[enums::FEMTOSECOND] = Mul(Pow(10, 27), Sym("teras"));
            c[enums::GIGASECOND] = Mul(Int(1000), Sym("teras"));
            c[enums::HECTOSECOND] = Mul(Pow(10, 10), Sym("teras"));
            c[enums::HOUR] = Mul(Rat(Mul(Int(25), Pow(10, 8)), Int(9)), Sym("teras"));
            c[enums::KILOSECOND] = Mul(Pow(10, 9), Sym("teras"));
            c[enums::MEGASECOND] = Mul(Pow(10, 6), Sym("teras"));
            c[enums::MICROSECOND] = Mul(Pow(10, 18), Sym("teras"));
            c[enums::MILLISECOND] = Mul(Pow(10, 15), Sym("teras"));
            c[enums::MINUTE] = Mul(Rat(Mul(Int(5), Pow(10, 10)), Int(3)), Sym("teras"));
            c[enums::NANOSECOND] = Mul(Pow(10, 21), Sym("teras"));
            c[enums::PETASECOND] = Mul(Rat(Int(1), Int(1000)), Sym("teras"));
            c[enums::PICOSECOND] = Mul(Pow(10, 24), Sym("teras"));
            c[enums::SECOND] = Mul(Pow(10, 12), Sym("teras"));
            c[enums::YOCTOSECOND] = Mul(Pow(10, 36), Sym("teras"));
            c[enums::YOTTASECOND] = Mul(Rat(Int(1), Pow(10, 12)), Sym("teras"));
            c[enums::ZEPTOSECOND] = Mul(Pow(10, 33), Sym("teras"));
            c[enums::ZETTASECOND] = Mul(Rat(Int(1), Pow(10, 9)), Sym("teras"));
            return c;
        }

        static std::map<UnitsTime, ConversionPtr> createMapYOCTOSECOND() {
            std::map<UnitsTime, ConversionPtr> c;
            c[enums::ATTOSECOND] = Mul(Rat(Int(1), Pow(10, 6)), Sym("yoctos"));
            c[enums::CENTISECOND] = Mul(Rat(Int(1), Pow(10, 22)), Sym("yoctos"));
            c[enums::DAY] = Mul(Rat(Int(1), Mul(Int(864), Pow(10, 26))), Sym("yoctos"));
            c[enums::DECASECOND] = Mul(Rat(Int(1), Pow(10, 25)), Sym("yoctos"));
            c[enums::DECISECOND] = Mul(Rat(Int(1), Pow(10, 23)), Sym("yoctos"));
            c[enums::EXASECOND] = Mul(Rat(Int(1), Pow(10, 42)), Sym("yoctos"));
            c[enums::FEMTOSECOND] = Mul(Rat(Int(1), Pow(10, 9)), Sym("yoctos"));
            c[enums::GIGASECOND] = Mul(Rat(Int(1), Pow(10, 33)), Sym("yoctos"));
            c[enums::HECTOSECOND] = Mul(Rat(Int(1), Pow(10, 26)), Sym("yoctos"));
            c[enums::HOUR] = Mul(Rat(Int(1), Mul(Int(36), Pow(10, 26))), Sym("yoctos"));
            c[enums::KILOSECOND] = Mul(Rat(Int(1), Pow(10, 27)), Sym("yoctos"));
            c[enums::MEGASECOND] = Mul(Rat(Int(1), Pow(10, 30)), Sym("yoctos"));
            c[enums::MICROSECOND] = Mul(Rat(Int(1), Pow(10, 18)), Sym("yoctos"));
            c[enums::MILLISECOND] = Mul(Rat(Int(1), Pow(10, 21)), Sym("yoctos"));
            c[enums::MINUTE] = Mul(Rat(Int(1), Mul(Int(6), Pow(10, 25))), Sym("yoctos"));
            c[enums::NANOSECOND] = Mul(Rat(Int(1), Pow(10, 15)), Sym("yoctos"));
            c[enums::PETASECOND] = Mul(Rat(Int(1), Pow(10, 39)), Sym("yoctos"));
            c[enums::PICOSECOND] = Mul(Rat(Int(1), Pow(10, 12)), Sym("yoctos"));
            c[enums::SECOND] = Mul(Rat(Int(1), Pow(10, 24)), Sym("yoctos"));
            c[enums::TERASECOND] = Mul(Rat(Int(1), Pow(10, 36)), Sym("yoctos"));
            c[enums::YOTTASECOND] = Mul(Rat(Int(1), Pow(10, 48)), Sym("yoctos"));
            c[enums::ZEPTOSECOND] = Mul(Rat(Int(1), Int(1000)), Sym("yoctos"));
            c[enums::ZETTASECOND] = Mul(Rat(Int(1), Pow(10, 45)), Sym("yoctos"));
            return c;
        }

        static std::map<UnitsTime, ConversionPtr> createMapYOTTASECOND() {
            std::map<UnitsTime, ConversionPtr> c;
            c[enums::ATTOSECOND] = Mul(Pow(10, 42), Sym("yottas"));
            c[enums::CENTISECOND] = Mul(Pow(10, 26), Sym("yottas"));
            c[enums::DAY] = Mul(Rat(Mul(Int(3125), Pow(10, 17)), Int(27)), Sym("yottas"));
            c[enums::DECASECOND] = Mul(Pow(10, 23), Sym("yottas"));
            c[enums::DECISECOND] = Mul(Pow(10, 25), Sym("yottas"));
            c[enums::EXASECOND] = Mul(Pow(10, 6), Sym("yottas"));
            c[enums::FEMTOSECOND] = Mul(Pow(10, 39), Sym("yottas"));
            c[enums::GIGASECOND] = Mul(Pow(10, 15), Sym("yottas"));
            c[enums::HECTOSECOND] = Mul(Pow(10, 22), Sym("yottas"));
            c[enums::HOUR] = Mul(Rat(Mul(Int(25), Pow(10, 20)), Int(9)), Sym("yottas"));
            c[enums::KILOSECOND] = Mul(Pow(10, 21), Sym("yottas"));
            c[enums::MEGASECOND] = Mul(Pow(10, 18), Sym("yottas"));
            c[enums::MICROSECOND] = Mul(Pow(10, 30), Sym("yottas"));
            c[enums::MILLISECOND] = Mul(Pow(10, 27), Sym("yottas"));
            c[enums::MINUTE] = Mul(Rat(Mul(Int(5), Pow(10, 22)), Int(3)), Sym("yottas"));
            c[enums::NANOSECOND] = Mul(Pow(10, 33), Sym("yottas"));
            c[enums::PETASECOND] = Mul(Pow(10, 9), Sym("yottas"));
            c[enums::PICOSECOND] = Mul(Pow(10, 36), Sym("yottas"));
            c[enums::SECOND] = Mul(Pow(10, 24), Sym("yottas"));
            c[enums::TERASECOND] = Mul(Pow(10, 12), Sym("yottas"));
            c[enums::YOCTOSECOND] = Mul(Pow(10, 48), Sym("yottas"));
            c[enums::ZEPTOSECOND] = Mul(Pow(10, 45), Sym("yottas"));
            c[enums::ZETTASECOND] = Mul(Int(1000), Sym("yottas"));
            return c;
        }

        static std::map<UnitsTime, ConversionPtr> createMapZEPTOSECOND() {
            std::map<UnitsTime, ConversionPtr> c;
            c[enums::ATTOSECOND] = Mul(Rat(Int(1), Int(1000)), Sym("zeptos"));
            c[enums::CENTISECOND] = Mul(Rat(Int(1), Pow(10, 19)), Sym("zeptos"));
            c[enums::DAY] = Mul(Rat(Int(1), Mul(Int(864), Pow(10, 23))), Sym("zeptos"));
            c[enums::DECASECOND] = Mul(Rat(Int(1), Pow(10, 22)), Sym("zeptos"));
            c[enums::DECISECOND] = Mul(Rat(Int(1), Pow(10, 20)), Sym("zeptos"));
            c[enums::EXASECOND] = Mul(Rat(Int(1), Pow(10, 39)), Sym("zeptos"));
            c[enums::FEMTOSECOND] = Mul(Rat(Int(1), Pow(10, 6)), Sym("zeptos"));
            c[enums::GIGASECOND] = Mul(Rat(Int(1), Pow(10, 30)), Sym("zeptos"));
            c[enums::HECTOSECOND] = Mul(Rat(Int(1), Pow(10, 23)), Sym("zeptos"));
            c[enums::HOUR] = Mul(Rat(Int(1), Mul(Int(36), Pow(10, 23))), Sym("zeptos"));
            c[enums::KILOSECOND] = Mul(Rat(Int(1), Pow(10, 24)), Sym("zeptos"));
            c[enums::MEGASECOND] = Mul(Rat(Int(1), Pow(10, 27)), Sym("zeptos"));
            c[enums::MICROSECOND] = Mul(Rat(Int(1), Pow(10, 15)), Sym("zeptos"));
            c[enums::MILLISECOND] = Mul(Rat(Int(1), Pow(10, 18)), Sym("zeptos"));
            c[enums::MINUTE] = Mul(Rat(Int(1), Mul(Int(6), Pow(10, 22))), Sym("zeptos"));
            c[enums::NANOSECOND] = Mul(Rat(Int(1), Pow(10, 12)), Sym("zeptos"));
            c[enums::PETASECOND] = Mul(Rat(Int(1), Pow(10, 36)), Sym("zeptos"));
            c[enums::PICOSECOND] = Mul(Rat(Int(1), Pow(10, 9)), Sym("zeptos"));
            c[enums::SECOND] = Mul(Rat(Int(1), Pow(10, 21)), Sym("zeptos"));
            c[enums::TERASECOND] = Mul(Rat(Int(1), Pow(10, 33)), Sym("zeptos"));
            c[enums::YOCTOSECOND] = Mul(Int(1000), Sym("zeptos"));
            c[enums::YOTTASECOND] = Mul(Rat(Int(1), Pow(10, 45)), Sym("zeptos"));
            c[enums::ZETTASECOND] = Mul(Rat(Int(1), Pow(10, 42)), Sym("zeptos"));
            return c;
        }

        static std::map<UnitsTime, ConversionPtr> createMapZETTASECOND() {
            std::map<UnitsTime, ConversionPtr> c;
            c[enums::ATTOSECOND] = Mul(Pow(10, 39), Sym("zettas"));
            c[enums::CENTISECOND] = Mul(Pow(10, 23), Sym("zettas"));
            c[enums::DAY] = Mul(Rat(Mul(Int(3125), Pow(10, 14)), Int(27)), Sym("zettas"));
            c[enums::DECASECOND] = Mul(Pow(10, 20), Sym("zettas"));
            c[enums::DECISECOND] = Mul(Pow(10, 22), Sym("zettas"));
            c[enums::EXASECOND] = Mul(Int(1000), Sym("zettas"));
            c[enums::FEMTOSECOND] = Mul(Pow(10, 36), Sym("zettas"));
            c[enums::GIGASECOND] = Mul(Pow(10, 12), Sym("zettas"));
            c[enums::HECTOSECOND] = Mul(Pow(10, 19), Sym("zettas"));
            c[enums::HOUR] = Mul(Rat(Mul(Int(25), Pow(10, 17)), Int(9)), Sym("zettas"));
            c[enums::KILOSECOND] = Mul(Pow(10, 18), Sym("zettas"));
            c[enums::MEGASECOND] = Mul(Pow(10, 15), Sym("zettas"));
            c[enums::MICROSECOND] = Mul(Pow(10, 27), Sym("zettas"));
            c[enums::MILLISECOND] = Mul(Pow(10, 24), Sym("zettas"));
            c[enums::MINUTE] = Mul(Rat(Mul(Int(5), Pow(10, 19)), Int(3)), Sym("zettas"));
            c[enums::NANOSECOND] = Mul(Pow(10, 30), Sym("zettas"));
            c[enums::PETASECOND] = Mul(Pow(10, 6), Sym("zettas"));
            c[enums::PICOSECOND] = Mul(Pow(10, 33), Sym("zettas"));
            c[enums::SECOND] = Mul(Pow(10, 21), Sym("zettas"));
            c[enums::TERASECOND] = Mul(Pow(10, 9), Sym("zettas"));
            c[enums::YOCTOSECOND] = Mul(Pow(10, 45), Sym("zettas"));
            c[enums::YOTTASECOND] = Mul(Rat(Int(1), Int(1000)), Sym("zettas"));
            c[enums::ZEPTOSECOND] = Mul(Pow(10, 42), Sym("zettas"));
            return c;
        }

        static std::map<UnitsTime,
            std::map<UnitsTime, ConversionPtr> > makeConversions() {
            std::map<UnitsTime, std::map<UnitsTime, ConversionPtr> > c;
            c[enums::ATTOSECOND] = createMapATTOSECOND();
            c[enums::CENTISECOND] = createMapCENTISECOND();
            c[enums::DAY] = createMapDAY();
            c[enums::DECASECOND] = createMapDECASECOND();
            c[enums::DECISECOND] = createMapDECISECOND();
            c[enums::EXASECOND] = createMapEXASECOND();
            c[enums::FEMTOSECOND] = createMapFEMTOSECOND();
            c[enums::GIGASECOND] = createMapGIGASECOND();
            c[enums::HECTOSECOND] = createMapHECTOSECOND();
            c[enums::HOUR] = createMapHOUR();
            c[enums::KILOSECOND] = createMapKILOSECOND();
            c[enums::MEGASECOND] = createMapMEGASECOND();
            c[enums::MICROSECOND] = createMapMICROSECOND();
            c[enums::MILLISECOND] = createMapMILLISECOND();
            c[enums::MINUTE] = createMapMINUTE();
            c[enums::NANOSECOND] = createMapNANOSECOND();
            c[enums::PETASECOND] = createMapPETASECOND();
            c[enums::PICOSECOND] = createMapPICOSECOND();
            c[enums::SECOND] = createMapSECOND();
            c[enums::TERASECOND] = createMapTERASECOND();
            c[enums::YOCTOSECOND] = createMapYOCTOSECOND();
            c[enums::YOTTASECOND] = createMapYOTTASECOND();
            c[enums::ZEPTOSECOND] = createMapZEPTOSECOND();
            c[enums::ZETTASECOND] = createMapZETTASECOND();
            return c;
        }

        static std::map<UnitsTime, std::string> makeSymbols(){
            std::map<UnitsTime, std::string> s;
            s[enums::ATTOSECOND] = "as";
            s[enums::CENTISECOND] = "cs";
            s[enums::DAY] = "d";
            s[enums::DECASECOND] = "das";
            s[enums::DECISECOND] = "ds";
            s[enums::EXASECOND] = "Es";
            s[enums::FEMTOSECOND] = "fs";
            s[enums::GIGASECOND] = "Gs";
            s[enums::HECTOSECOND] = "hs";
            s[enums::HOUR] = "h";
            s[enums::KILOSECOND] = "ks";
            s[enums::MEGASECOND] = "Ms";
            s[enums::MICROSECOND] = "µs";
            s[enums::MILLISECOND] = "ms";
            s[enums::MINUTE] = "min";
            s[enums::NANOSECOND] = "ns";
            s[enums::PETASECOND] = "Ps";
            s[enums::PICOSECOND] = "ps";
            s[enums::SECOND] = "s";
            s[enums::TERASECOND] = "Ts";
            s[enums::YOCTOSECOND] = "ys";
            s[enums::YOTTASECOND] = "Ys";
            s[enums::ZEPTOSECOND] = "zs";
            s[enums::ZETTASECOND] = "Zs";
            return s;
        }

        std::map<UnitsTime,
            std::map<UnitsTime, ConversionPtr> > TimeI::CONVERSIONS = makeConversions();

        std::map<UnitsTime, std::string> TimeI::SYMBOLS = makeSymbols();

        TimeI::~TimeI() {}

        TimeI::TimeI() : Time() {
        }

        TimeI::TimeI(const double& value, const UnitsTime& unit) : Time() {
            setValue(value);
            setUnit(unit);
        }

        TimeI::TimeI(const TimePtr& value, const UnitsTime& target) : Time() {
            double orig = value->getValue();
            UnitsTime source = value->getUnit();
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

        Ice::Double TimeI::getValue(const Ice::Current& /* current */) {
            return value;
        }

        void TimeI::setValue(Ice::Double _value, const Ice::Current& /* current */) {
            value = _value;
        }

        UnitsTime TimeI::getUnit(const Ice::Current& /* current */) {
            return unit;
        }

        void TimeI::setUnit(UnitsTime _unit, const Ice::Current& /* current */) {
            unit = _unit;
        }

        std::string TimeI::getSymbol(const Ice::Current& /* current */) {
            return SYMBOLS[unit];
        }

        TimePtr TimeI::copy(const Ice::Current& /* current */) {
            TimePtr copy = new TimeI();
            copy->setValue(getValue());
            copy->setUnit(getUnit());
            return copy;
        }
    }
}

