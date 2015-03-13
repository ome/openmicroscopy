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

#include <omero/model/PowerI.h>
#include <omero/ClientErrors.h>

::Ice::Object* IceInternal::upCast(::omero::model::PowerI* t) { return t; }

using namespace omero::conversions;

typedef omero::model::enums::UnitsPower UnitsPower;

namespace omero {

    namespace model {

        static std::map<UnitsPower, ConversionPtr> createMapATTOWATT() {
            std::map<UnitsPower, ConversionPtr> c;
            c[enums::CENTIWATT] = Mul(Rat(Int(1), Pow(10, 16)), Sym("attow"));
            c[enums::DECAWATT] = Mul(Rat(Int(1), Pow(10, 19)), Sym("attow"));
            c[enums::DECIWATT] = Mul(Rat(Int(1), Pow(10, 17)), Sym("attow"));
            c[enums::EXAWATT] = Mul(Rat(Int(1), Pow(10, 36)), Sym("attow"));
            c[enums::FEMTOWATT] = Mul(Rat(Int(1), Int(1000)), Sym("attow"));
            c[enums::GIGAWATT] = Mul(Rat(Int(1), Pow(10, 27)), Sym("attow"));
            c[enums::HECTOWATT] = Mul(Rat(Int(1), Pow(10, 20)), Sym("attow"));
            c[enums::KILOWATT] = Mul(Rat(Int(1), Pow(10, 21)), Sym("attow"));
            c[enums::MEGAWATT] = Mul(Rat(Int(1), Pow(10, 24)), Sym("attow"));
            c[enums::MICROWATT] = Mul(Rat(Int(1), Pow(10, 12)), Sym("attow"));
            c[enums::MILLIWATT] = Mul(Rat(Int(1), Pow(10, 15)), Sym("attow"));
            c[enums::NANOWATT] = Mul(Rat(Int(1), Pow(10, 9)), Sym("attow"));
            c[enums::PETAWATT] = Mul(Rat(Int(1), Pow(10, 33)), Sym("attow"));
            c[enums::PICOWATT] = Mul(Rat(Int(1), Pow(10, 6)), Sym("attow"));
            c[enums::TERAWATT] = Mul(Rat(Int(1), Pow(10, 30)), Sym("attow"));
            c[enums::WATT] = Mul(Rat(Int(1), Pow(10, 18)), Sym("attow"));
            c[enums::YOCTOWATT] = Mul(Pow(10, 6), Sym("attow"));
            c[enums::YOTTAWATT] = Mul(Rat(Int(1), Pow(10, 42)), Sym("attow"));
            c[enums::ZEPTOWATT] = Mul(Int(1000), Sym("attow"));
            c[enums::ZETTAWATT] = Mul(Rat(Int(1), Pow(10, 39)), Sym("attow"));
            return c;
        }

        static std::map<UnitsPower, ConversionPtr> createMapCENTIWATT() {
            std::map<UnitsPower, ConversionPtr> c;
            c[enums::ATTOWATT] = Mul(Pow(10, 16), Sym("centiw"));
            c[enums::DECAWATT] = Mul(Rat(Int(1), Int(1000)), Sym("centiw"));
            c[enums::DECIWATT] = Mul(Rat(Int(1), Int(10)), Sym("centiw"));
            c[enums::EXAWATT] = Mul(Rat(Int(1), Pow(10, 20)), Sym("centiw"));
            c[enums::FEMTOWATT] = Mul(Pow(10, 13), Sym("centiw"));
            c[enums::GIGAWATT] = Mul(Rat(Int(1), Pow(10, 11)), Sym("centiw"));
            c[enums::HECTOWATT] = Mul(Rat(Int(1), Pow(10, 4)), Sym("centiw"));
            c[enums::KILOWATT] = Mul(Rat(Int(1), Pow(10, 5)), Sym("centiw"));
            c[enums::MEGAWATT] = Mul(Rat(Int(1), Pow(10, 8)), Sym("centiw"));
            c[enums::MICROWATT] = Mul(Pow(10, 4), Sym("centiw"));
            c[enums::MILLIWATT] = Mul(Int(10), Sym("centiw"));
            c[enums::NANOWATT] = Mul(Pow(10, 7), Sym("centiw"));
            c[enums::PETAWATT] = Mul(Rat(Int(1), Pow(10, 17)), Sym("centiw"));
            c[enums::PICOWATT] = Mul(Pow(10, 10), Sym("centiw"));
            c[enums::TERAWATT] = Mul(Rat(Int(1), Pow(10, 14)), Sym("centiw"));
            c[enums::WATT] = Mul(Rat(Int(1), Int(100)), Sym("centiw"));
            c[enums::YOCTOWATT] = Mul(Pow(10, 22), Sym("centiw"));
            c[enums::YOTTAWATT] = Mul(Rat(Int(1), Pow(10, 26)), Sym("centiw"));
            c[enums::ZEPTOWATT] = Mul(Pow(10, 19), Sym("centiw"));
            c[enums::ZETTAWATT] = Mul(Rat(Int(1), Pow(10, 23)), Sym("centiw"));
            return c;
        }

        static std::map<UnitsPower, ConversionPtr> createMapDECAWATT() {
            std::map<UnitsPower, ConversionPtr> c;
            c[enums::ATTOWATT] = Mul(Pow(10, 19), Sym("decaw"));
            c[enums::CENTIWATT] = Mul(Int(1000), Sym("decaw"));
            c[enums::DECIWATT] = Mul(Int(100), Sym("decaw"));
            c[enums::EXAWATT] = Mul(Rat(Int(1), Pow(10, 17)), Sym("decaw"));
            c[enums::FEMTOWATT] = Mul(Pow(10, 16), Sym("decaw"));
            c[enums::GIGAWATT] = Mul(Rat(Int(1), Pow(10, 8)), Sym("decaw"));
            c[enums::HECTOWATT] = Mul(Rat(Int(1), Int(10)), Sym("decaw"));
            c[enums::KILOWATT] = Mul(Rat(Int(1), Int(100)), Sym("decaw"));
            c[enums::MEGAWATT] = Mul(Rat(Int(1), Pow(10, 5)), Sym("decaw"));
            c[enums::MICROWATT] = Mul(Pow(10, 7), Sym("decaw"));
            c[enums::MILLIWATT] = Mul(Pow(10, 4), Sym("decaw"));
            c[enums::NANOWATT] = Mul(Pow(10, 10), Sym("decaw"));
            c[enums::PETAWATT] = Mul(Rat(Int(1), Pow(10, 14)), Sym("decaw"));
            c[enums::PICOWATT] = Mul(Pow(10, 13), Sym("decaw"));
            c[enums::TERAWATT] = Mul(Rat(Int(1), Pow(10, 11)), Sym("decaw"));
            c[enums::WATT] = Mul(Int(10), Sym("decaw"));
            c[enums::YOCTOWATT] = Mul(Pow(10, 25), Sym("decaw"));
            c[enums::YOTTAWATT] = Mul(Rat(Int(1), Pow(10, 23)), Sym("decaw"));
            c[enums::ZEPTOWATT] = Mul(Pow(10, 22), Sym("decaw"));
            c[enums::ZETTAWATT] = Mul(Rat(Int(1), Pow(10, 20)), Sym("decaw"));
            return c;
        }

        static std::map<UnitsPower, ConversionPtr> createMapDECIWATT() {
            std::map<UnitsPower, ConversionPtr> c;
            c[enums::ATTOWATT] = Mul(Pow(10, 17), Sym("deciw"));
            c[enums::CENTIWATT] = Mul(Int(10), Sym("deciw"));
            c[enums::DECAWATT] = Mul(Rat(Int(1), Int(100)), Sym("deciw"));
            c[enums::EXAWATT] = Mul(Rat(Int(1), Pow(10, 19)), Sym("deciw"));
            c[enums::FEMTOWATT] = Mul(Pow(10, 14), Sym("deciw"));
            c[enums::GIGAWATT] = Mul(Rat(Int(1), Pow(10, 10)), Sym("deciw"));
            c[enums::HECTOWATT] = Mul(Rat(Int(1), Int(1000)), Sym("deciw"));
            c[enums::KILOWATT] = Mul(Rat(Int(1), Pow(10, 4)), Sym("deciw"));
            c[enums::MEGAWATT] = Mul(Rat(Int(1), Pow(10, 7)), Sym("deciw"));
            c[enums::MICROWATT] = Mul(Pow(10, 5), Sym("deciw"));
            c[enums::MILLIWATT] = Mul(Int(100), Sym("deciw"));
            c[enums::NANOWATT] = Mul(Pow(10, 8), Sym("deciw"));
            c[enums::PETAWATT] = Mul(Rat(Int(1), Pow(10, 16)), Sym("deciw"));
            c[enums::PICOWATT] = Mul(Pow(10, 11), Sym("deciw"));
            c[enums::TERAWATT] = Mul(Rat(Int(1), Pow(10, 13)), Sym("deciw"));
            c[enums::WATT] = Mul(Rat(Int(1), Int(10)), Sym("deciw"));
            c[enums::YOCTOWATT] = Mul(Pow(10, 23), Sym("deciw"));
            c[enums::YOTTAWATT] = Mul(Rat(Int(1), Pow(10, 25)), Sym("deciw"));
            c[enums::ZEPTOWATT] = Mul(Pow(10, 20), Sym("deciw"));
            c[enums::ZETTAWATT] = Mul(Rat(Int(1), Pow(10, 22)), Sym("deciw"));
            return c;
        }

        static std::map<UnitsPower, ConversionPtr> createMapEXAWATT() {
            std::map<UnitsPower, ConversionPtr> c;
            c[enums::ATTOWATT] = Mul(Pow(10, 36), Sym("exaw"));
            c[enums::CENTIWATT] = Mul(Pow(10, 20), Sym("exaw"));
            c[enums::DECAWATT] = Mul(Pow(10, 17), Sym("exaw"));
            c[enums::DECIWATT] = Mul(Pow(10, 19), Sym("exaw"));
            c[enums::FEMTOWATT] = Mul(Pow(10, 33), Sym("exaw"));
            c[enums::GIGAWATT] = Mul(Pow(10, 9), Sym("exaw"));
            c[enums::HECTOWATT] = Mul(Pow(10, 16), Sym("exaw"));
            c[enums::KILOWATT] = Mul(Pow(10, 15), Sym("exaw"));
            c[enums::MEGAWATT] = Mul(Pow(10, 12), Sym("exaw"));
            c[enums::MICROWATT] = Mul(Pow(10, 24), Sym("exaw"));
            c[enums::MILLIWATT] = Mul(Pow(10, 21), Sym("exaw"));
            c[enums::NANOWATT] = Mul(Pow(10, 27), Sym("exaw"));
            c[enums::PETAWATT] = Mul(Int(1000), Sym("exaw"));
            c[enums::PICOWATT] = Mul(Pow(10, 30), Sym("exaw"));
            c[enums::TERAWATT] = Mul(Pow(10, 6), Sym("exaw"));
            c[enums::WATT] = Mul(Pow(10, 18), Sym("exaw"));
            c[enums::YOCTOWATT] = Mul(Pow(10, 42), Sym("exaw"));
            c[enums::YOTTAWATT] = Mul(Rat(Int(1), Pow(10, 6)), Sym("exaw"));
            c[enums::ZEPTOWATT] = Mul(Pow(10, 39), Sym("exaw"));
            c[enums::ZETTAWATT] = Mul(Rat(Int(1), Int(1000)), Sym("exaw"));
            return c;
        }

        static std::map<UnitsPower, ConversionPtr> createMapFEMTOWATT() {
            std::map<UnitsPower, ConversionPtr> c;
            c[enums::ATTOWATT] = Mul(Int(1000), Sym("femtow"));
            c[enums::CENTIWATT] = Mul(Rat(Int(1), Pow(10, 13)), Sym("femtow"));
            c[enums::DECAWATT] = Mul(Rat(Int(1), Pow(10, 16)), Sym("femtow"));
            c[enums::DECIWATT] = Mul(Rat(Int(1), Pow(10, 14)), Sym("femtow"));
            c[enums::EXAWATT] = Mul(Rat(Int(1), Pow(10, 33)), Sym("femtow"));
            c[enums::GIGAWATT] = Mul(Rat(Int(1), Pow(10, 24)), Sym("femtow"));
            c[enums::HECTOWATT] = Mul(Rat(Int(1), Pow(10, 17)), Sym("femtow"));
            c[enums::KILOWATT] = Mul(Rat(Int(1), Pow(10, 18)), Sym("femtow"));
            c[enums::MEGAWATT] = Mul(Rat(Int(1), Pow(10, 21)), Sym("femtow"));
            c[enums::MICROWATT] = Mul(Rat(Int(1), Pow(10, 9)), Sym("femtow"));
            c[enums::MILLIWATT] = Mul(Rat(Int(1), Pow(10, 12)), Sym("femtow"));
            c[enums::NANOWATT] = Mul(Rat(Int(1), Pow(10, 6)), Sym("femtow"));
            c[enums::PETAWATT] = Mul(Rat(Int(1), Pow(10, 30)), Sym("femtow"));
            c[enums::PICOWATT] = Mul(Rat(Int(1), Int(1000)), Sym("femtow"));
            c[enums::TERAWATT] = Mul(Rat(Int(1), Pow(10, 27)), Sym("femtow"));
            c[enums::WATT] = Mul(Rat(Int(1), Pow(10, 15)), Sym("femtow"));
            c[enums::YOCTOWATT] = Mul(Pow(10, 9), Sym("femtow"));
            c[enums::YOTTAWATT] = Mul(Rat(Int(1), Pow(10, 39)), Sym("femtow"));
            c[enums::ZEPTOWATT] = Mul(Pow(10, 6), Sym("femtow"));
            c[enums::ZETTAWATT] = Mul(Rat(Int(1), Pow(10, 36)), Sym("femtow"));
            return c;
        }

        static std::map<UnitsPower, ConversionPtr> createMapGIGAWATT() {
            std::map<UnitsPower, ConversionPtr> c;
            c[enums::ATTOWATT] = Mul(Pow(10, 27), Sym("gigaw"));
            c[enums::CENTIWATT] = Mul(Pow(10, 11), Sym("gigaw"));
            c[enums::DECAWATT] = Mul(Pow(10, 8), Sym("gigaw"));
            c[enums::DECIWATT] = Mul(Pow(10, 10), Sym("gigaw"));
            c[enums::EXAWATT] = Mul(Rat(Int(1), Pow(10, 9)), Sym("gigaw"));
            c[enums::FEMTOWATT] = Mul(Pow(10, 24), Sym("gigaw"));
            c[enums::HECTOWATT] = Mul(Pow(10, 7), Sym("gigaw"));
            c[enums::KILOWATT] = Mul(Pow(10, 6), Sym("gigaw"));
            c[enums::MEGAWATT] = Mul(Int(1000), Sym("gigaw"));
            c[enums::MICROWATT] = Mul(Pow(10, 15), Sym("gigaw"));
            c[enums::MILLIWATT] = Mul(Pow(10, 12), Sym("gigaw"));
            c[enums::NANOWATT] = Mul(Pow(10, 18), Sym("gigaw"));
            c[enums::PETAWATT] = Mul(Rat(Int(1), Pow(10, 6)), Sym("gigaw"));
            c[enums::PICOWATT] = Mul(Pow(10, 21), Sym("gigaw"));
            c[enums::TERAWATT] = Mul(Rat(Int(1), Int(1000)), Sym("gigaw"));
            c[enums::WATT] = Mul(Pow(10, 9), Sym("gigaw"));
            c[enums::YOCTOWATT] = Mul(Pow(10, 33), Sym("gigaw"));
            c[enums::YOTTAWATT] = Mul(Rat(Int(1), Pow(10, 15)), Sym("gigaw"));
            c[enums::ZEPTOWATT] = Mul(Pow(10, 30), Sym("gigaw"));
            c[enums::ZETTAWATT] = Mul(Rat(Int(1), Pow(10, 12)), Sym("gigaw"));
            return c;
        }

        static std::map<UnitsPower, ConversionPtr> createMapHECTOWATT() {
            std::map<UnitsPower, ConversionPtr> c;
            c[enums::ATTOWATT] = Mul(Pow(10, 20), Sym("hectow"));
            c[enums::CENTIWATT] = Mul(Pow(10, 4), Sym("hectow"));
            c[enums::DECAWATT] = Mul(Int(10), Sym("hectow"));
            c[enums::DECIWATT] = Mul(Int(1000), Sym("hectow"));
            c[enums::EXAWATT] = Mul(Rat(Int(1), Pow(10, 16)), Sym("hectow"));
            c[enums::FEMTOWATT] = Mul(Pow(10, 17), Sym("hectow"));
            c[enums::GIGAWATT] = Mul(Rat(Int(1), Pow(10, 7)), Sym("hectow"));
            c[enums::KILOWATT] = Mul(Rat(Int(1), Int(10)), Sym("hectow"));
            c[enums::MEGAWATT] = Mul(Rat(Int(1), Pow(10, 4)), Sym("hectow"));
            c[enums::MICROWATT] = Mul(Pow(10, 8), Sym("hectow"));
            c[enums::MILLIWATT] = Mul(Pow(10, 5), Sym("hectow"));
            c[enums::NANOWATT] = Mul(Pow(10, 11), Sym("hectow"));
            c[enums::PETAWATT] = Mul(Rat(Int(1), Pow(10, 13)), Sym("hectow"));
            c[enums::PICOWATT] = Mul(Pow(10, 14), Sym("hectow"));
            c[enums::TERAWATT] = Mul(Rat(Int(1), Pow(10, 10)), Sym("hectow"));
            c[enums::WATT] = Mul(Int(100), Sym("hectow"));
            c[enums::YOCTOWATT] = Mul(Pow(10, 26), Sym("hectow"));
            c[enums::YOTTAWATT] = Mul(Rat(Int(1), Pow(10, 22)), Sym("hectow"));
            c[enums::ZEPTOWATT] = Mul(Pow(10, 23), Sym("hectow"));
            c[enums::ZETTAWATT] = Mul(Rat(Int(1), Pow(10, 19)), Sym("hectow"));
            return c;
        }

        static std::map<UnitsPower, ConversionPtr> createMapKILOWATT() {
            std::map<UnitsPower, ConversionPtr> c;
            c[enums::ATTOWATT] = Mul(Pow(10, 21), Sym("kilow"));
            c[enums::CENTIWATT] = Mul(Pow(10, 5), Sym("kilow"));
            c[enums::DECAWATT] = Mul(Int(100), Sym("kilow"));
            c[enums::DECIWATT] = Mul(Pow(10, 4), Sym("kilow"));
            c[enums::EXAWATT] = Mul(Rat(Int(1), Pow(10, 15)), Sym("kilow"));
            c[enums::FEMTOWATT] = Mul(Pow(10, 18), Sym("kilow"));
            c[enums::GIGAWATT] = Mul(Rat(Int(1), Pow(10, 6)), Sym("kilow"));
            c[enums::HECTOWATT] = Mul(Int(10), Sym("kilow"));
            c[enums::MEGAWATT] = Mul(Rat(Int(1), Int(1000)), Sym("kilow"));
            c[enums::MICROWATT] = Mul(Pow(10, 9), Sym("kilow"));
            c[enums::MILLIWATT] = Mul(Pow(10, 6), Sym("kilow"));
            c[enums::NANOWATT] = Mul(Pow(10, 12), Sym("kilow"));
            c[enums::PETAWATT] = Mul(Rat(Int(1), Pow(10, 12)), Sym("kilow"));
            c[enums::PICOWATT] = Mul(Pow(10, 15), Sym("kilow"));
            c[enums::TERAWATT] = Mul(Rat(Int(1), Pow(10, 9)), Sym("kilow"));
            c[enums::WATT] = Mul(Int(1000), Sym("kilow"));
            c[enums::YOCTOWATT] = Mul(Pow(10, 27), Sym("kilow"));
            c[enums::YOTTAWATT] = Mul(Rat(Int(1), Pow(10, 21)), Sym("kilow"));
            c[enums::ZEPTOWATT] = Mul(Pow(10, 24), Sym("kilow"));
            c[enums::ZETTAWATT] = Mul(Rat(Int(1), Pow(10, 18)), Sym("kilow"));
            return c;
        }

        static std::map<UnitsPower, ConversionPtr> createMapMEGAWATT() {
            std::map<UnitsPower, ConversionPtr> c;
            c[enums::ATTOWATT] = Mul(Pow(10, 24), Sym("megaw"));
            c[enums::CENTIWATT] = Mul(Pow(10, 8), Sym("megaw"));
            c[enums::DECAWATT] = Mul(Pow(10, 5), Sym("megaw"));
            c[enums::DECIWATT] = Mul(Pow(10, 7), Sym("megaw"));
            c[enums::EXAWATT] = Mul(Rat(Int(1), Pow(10, 12)), Sym("megaw"));
            c[enums::FEMTOWATT] = Mul(Pow(10, 21), Sym("megaw"));
            c[enums::GIGAWATT] = Mul(Rat(Int(1), Int(1000)), Sym("megaw"));
            c[enums::HECTOWATT] = Mul(Pow(10, 4), Sym("megaw"));
            c[enums::KILOWATT] = Mul(Int(1000), Sym("megaw"));
            c[enums::MICROWATT] = Mul(Pow(10, 12), Sym("megaw"));
            c[enums::MILLIWATT] = Mul(Pow(10, 9), Sym("megaw"));
            c[enums::NANOWATT] = Mul(Pow(10, 15), Sym("megaw"));
            c[enums::PETAWATT] = Mul(Rat(Int(1), Pow(10, 9)), Sym("megaw"));
            c[enums::PICOWATT] = Mul(Pow(10, 18), Sym("megaw"));
            c[enums::TERAWATT] = Mul(Rat(Int(1), Pow(10, 6)), Sym("megaw"));
            c[enums::WATT] = Mul(Pow(10, 6), Sym("megaw"));
            c[enums::YOCTOWATT] = Mul(Pow(10, 30), Sym("megaw"));
            c[enums::YOTTAWATT] = Mul(Rat(Int(1), Pow(10, 18)), Sym("megaw"));
            c[enums::ZEPTOWATT] = Mul(Pow(10, 27), Sym("megaw"));
            c[enums::ZETTAWATT] = Mul(Rat(Int(1), Pow(10, 15)), Sym("megaw"));
            return c;
        }

        static std::map<UnitsPower, ConversionPtr> createMapMICROWATT() {
            std::map<UnitsPower, ConversionPtr> c;
            c[enums::ATTOWATT] = Mul(Pow(10, 12), Sym("microw"));
            c[enums::CENTIWATT] = Mul(Rat(Int(1), Pow(10, 4)), Sym("microw"));
            c[enums::DECAWATT] = Mul(Rat(Int(1), Pow(10, 7)), Sym("microw"));
            c[enums::DECIWATT] = Mul(Rat(Int(1), Pow(10, 5)), Sym("microw"));
            c[enums::EXAWATT] = Mul(Rat(Int(1), Pow(10, 24)), Sym("microw"));
            c[enums::FEMTOWATT] = Mul(Pow(10, 9), Sym("microw"));
            c[enums::GIGAWATT] = Mul(Rat(Int(1), Pow(10, 15)), Sym("microw"));
            c[enums::HECTOWATT] = Mul(Rat(Int(1), Pow(10, 8)), Sym("microw"));
            c[enums::KILOWATT] = Mul(Rat(Int(1), Pow(10, 9)), Sym("microw"));
            c[enums::MEGAWATT] = Mul(Rat(Int(1), Pow(10, 12)), Sym("microw"));
            c[enums::MILLIWATT] = Mul(Rat(Int(1), Int(1000)), Sym("microw"));
            c[enums::NANOWATT] = Mul(Int(1000), Sym("microw"));
            c[enums::PETAWATT] = Mul(Rat(Int(1), Pow(10, 21)), Sym("microw"));
            c[enums::PICOWATT] = Mul(Pow(10, 6), Sym("microw"));
            c[enums::TERAWATT] = Mul(Rat(Int(1), Pow(10, 18)), Sym("microw"));
            c[enums::WATT] = Mul(Rat(Int(1), Pow(10, 6)), Sym("microw"));
            c[enums::YOCTOWATT] = Mul(Pow(10, 18), Sym("microw"));
            c[enums::YOTTAWATT] = Mul(Rat(Int(1), Pow(10, 30)), Sym("microw"));
            c[enums::ZEPTOWATT] = Mul(Pow(10, 15), Sym("microw"));
            c[enums::ZETTAWATT] = Mul(Rat(Int(1), Pow(10, 27)), Sym("microw"));
            return c;
        }

        static std::map<UnitsPower, ConversionPtr> createMapMILLIWATT() {
            std::map<UnitsPower, ConversionPtr> c;
            c[enums::ATTOWATT] = Mul(Pow(10, 15), Sym("milliw"));
            c[enums::CENTIWATT] = Mul(Rat(Int(1), Int(10)), Sym("milliw"));
            c[enums::DECAWATT] = Mul(Rat(Int(1), Pow(10, 4)), Sym("milliw"));
            c[enums::DECIWATT] = Mul(Rat(Int(1), Int(100)), Sym("milliw"));
            c[enums::EXAWATT] = Mul(Rat(Int(1), Pow(10, 21)), Sym("milliw"));
            c[enums::FEMTOWATT] = Mul(Pow(10, 12), Sym("milliw"));
            c[enums::GIGAWATT] = Mul(Rat(Int(1), Pow(10, 12)), Sym("milliw"));
            c[enums::HECTOWATT] = Mul(Rat(Int(1), Pow(10, 5)), Sym("milliw"));
            c[enums::KILOWATT] = Mul(Rat(Int(1), Pow(10, 6)), Sym("milliw"));
            c[enums::MEGAWATT] = Mul(Rat(Int(1), Pow(10, 9)), Sym("milliw"));
            c[enums::MICROWATT] = Mul(Int(1000), Sym("milliw"));
            c[enums::NANOWATT] = Mul(Pow(10, 6), Sym("milliw"));
            c[enums::PETAWATT] = Mul(Rat(Int(1), Pow(10, 18)), Sym("milliw"));
            c[enums::PICOWATT] = Mul(Pow(10, 9), Sym("milliw"));
            c[enums::TERAWATT] = Mul(Rat(Int(1), Pow(10, 15)), Sym("milliw"));
            c[enums::WATT] = Mul(Rat(Int(1), Int(1000)), Sym("milliw"));
            c[enums::YOCTOWATT] = Mul(Pow(10, 21), Sym("milliw"));
            c[enums::YOTTAWATT] = Mul(Rat(Int(1), Pow(10, 27)), Sym("milliw"));
            c[enums::ZEPTOWATT] = Mul(Pow(10, 18), Sym("milliw"));
            c[enums::ZETTAWATT] = Mul(Rat(Int(1), Pow(10, 24)), Sym("milliw"));
            return c;
        }

        static std::map<UnitsPower, ConversionPtr> createMapNANOWATT() {
            std::map<UnitsPower, ConversionPtr> c;
            c[enums::ATTOWATT] = Mul(Pow(10, 9), Sym("nanow"));
            c[enums::CENTIWATT] = Mul(Rat(Int(1), Pow(10, 7)), Sym("nanow"));
            c[enums::DECAWATT] = Mul(Rat(Int(1), Pow(10, 10)), Sym("nanow"));
            c[enums::DECIWATT] = Mul(Rat(Int(1), Pow(10, 8)), Sym("nanow"));
            c[enums::EXAWATT] = Mul(Rat(Int(1), Pow(10, 27)), Sym("nanow"));
            c[enums::FEMTOWATT] = Mul(Pow(10, 6), Sym("nanow"));
            c[enums::GIGAWATT] = Mul(Rat(Int(1), Pow(10, 18)), Sym("nanow"));
            c[enums::HECTOWATT] = Mul(Rat(Int(1), Pow(10, 11)), Sym("nanow"));
            c[enums::KILOWATT] = Mul(Rat(Int(1), Pow(10, 12)), Sym("nanow"));
            c[enums::MEGAWATT] = Mul(Rat(Int(1), Pow(10, 15)), Sym("nanow"));
            c[enums::MICROWATT] = Mul(Rat(Int(1), Int(1000)), Sym("nanow"));
            c[enums::MILLIWATT] = Mul(Rat(Int(1), Pow(10, 6)), Sym("nanow"));
            c[enums::PETAWATT] = Mul(Rat(Int(1), Pow(10, 24)), Sym("nanow"));
            c[enums::PICOWATT] = Mul(Int(1000), Sym("nanow"));
            c[enums::TERAWATT] = Mul(Rat(Int(1), Pow(10, 21)), Sym("nanow"));
            c[enums::WATT] = Mul(Rat(Int(1), Pow(10, 9)), Sym("nanow"));
            c[enums::YOCTOWATT] = Mul(Pow(10, 15), Sym("nanow"));
            c[enums::YOTTAWATT] = Mul(Rat(Int(1), Pow(10, 33)), Sym("nanow"));
            c[enums::ZEPTOWATT] = Mul(Pow(10, 12), Sym("nanow"));
            c[enums::ZETTAWATT] = Mul(Rat(Int(1), Pow(10, 30)), Sym("nanow"));
            return c;
        }

        static std::map<UnitsPower, ConversionPtr> createMapPETAWATT() {
            std::map<UnitsPower, ConversionPtr> c;
            c[enums::ATTOWATT] = Mul(Pow(10, 33), Sym("petaw"));
            c[enums::CENTIWATT] = Mul(Pow(10, 17), Sym("petaw"));
            c[enums::DECAWATT] = Mul(Pow(10, 14), Sym("petaw"));
            c[enums::DECIWATT] = Mul(Pow(10, 16), Sym("petaw"));
            c[enums::EXAWATT] = Mul(Rat(Int(1), Int(1000)), Sym("petaw"));
            c[enums::FEMTOWATT] = Mul(Pow(10, 30), Sym("petaw"));
            c[enums::GIGAWATT] = Mul(Pow(10, 6), Sym("petaw"));
            c[enums::HECTOWATT] = Mul(Pow(10, 13), Sym("petaw"));
            c[enums::KILOWATT] = Mul(Pow(10, 12), Sym("petaw"));
            c[enums::MEGAWATT] = Mul(Pow(10, 9), Sym("petaw"));
            c[enums::MICROWATT] = Mul(Pow(10, 21), Sym("petaw"));
            c[enums::MILLIWATT] = Mul(Pow(10, 18), Sym("petaw"));
            c[enums::NANOWATT] = Mul(Pow(10, 24), Sym("petaw"));
            c[enums::PICOWATT] = Mul(Pow(10, 27), Sym("petaw"));
            c[enums::TERAWATT] = Mul(Int(1000), Sym("petaw"));
            c[enums::WATT] = Mul(Pow(10, 15), Sym("petaw"));
            c[enums::YOCTOWATT] = Mul(Pow(10, 39), Sym("petaw"));
            c[enums::YOTTAWATT] = Mul(Rat(Int(1), Pow(10, 9)), Sym("petaw"));
            c[enums::ZEPTOWATT] = Mul(Pow(10, 36), Sym("petaw"));
            c[enums::ZETTAWATT] = Mul(Rat(Int(1), Pow(10, 6)), Sym("petaw"));
            return c;
        }

        static std::map<UnitsPower, ConversionPtr> createMapPICOWATT() {
            std::map<UnitsPower, ConversionPtr> c;
            c[enums::ATTOWATT] = Mul(Pow(10, 6), Sym("picow"));
            c[enums::CENTIWATT] = Mul(Rat(Int(1), Pow(10, 10)), Sym("picow"));
            c[enums::DECAWATT] = Mul(Rat(Int(1), Pow(10, 13)), Sym("picow"));
            c[enums::DECIWATT] = Mul(Rat(Int(1), Pow(10, 11)), Sym("picow"));
            c[enums::EXAWATT] = Mul(Rat(Int(1), Pow(10, 30)), Sym("picow"));
            c[enums::FEMTOWATT] = Mul(Int(1000), Sym("picow"));
            c[enums::GIGAWATT] = Mul(Rat(Int(1), Pow(10, 21)), Sym("picow"));
            c[enums::HECTOWATT] = Mul(Rat(Int(1), Pow(10, 14)), Sym("picow"));
            c[enums::KILOWATT] = Mul(Rat(Int(1), Pow(10, 15)), Sym("picow"));
            c[enums::MEGAWATT] = Mul(Rat(Int(1), Pow(10, 18)), Sym("picow"));
            c[enums::MICROWATT] = Mul(Rat(Int(1), Pow(10, 6)), Sym("picow"));
            c[enums::MILLIWATT] = Mul(Rat(Int(1), Pow(10, 9)), Sym("picow"));
            c[enums::NANOWATT] = Mul(Rat(Int(1), Int(1000)), Sym("picow"));
            c[enums::PETAWATT] = Mul(Rat(Int(1), Pow(10, 27)), Sym("picow"));
            c[enums::TERAWATT] = Mul(Rat(Int(1), Pow(10, 24)), Sym("picow"));
            c[enums::WATT] = Mul(Rat(Int(1), Pow(10, 12)), Sym("picow"));
            c[enums::YOCTOWATT] = Mul(Pow(10, 12), Sym("picow"));
            c[enums::YOTTAWATT] = Mul(Rat(Int(1), Pow(10, 36)), Sym("picow"));
            c[enums::ZEPTOWATT] = Mul(Pow(10, 9), Sym("picow"));
            c[enums::ZETTAWATT] = Mul(Rat(Int(1), Pow(10, 33)), Sym("picow"));
            return c;
        }

        static std::map<UnitsPower, ConversionPtr> createMapTERAWATT() {
            std::map<UnitsPower, ConversionPtr> c;
            c[enums::ATTOWATT] = Mul(Pow(10, 30), Sym("teraw"));
            c[enums::CENTIWATT] = Mul(Pow(10, 14), Sym("teraw"));
            c[enums::DECAWATT] = Mul(Pow(10, 11), Sym("teraw"));
            c[enums::DECIWATT] = Mul(Pow(10, 13), Sym("teraw"));
            c[enums::EXAWATT] = Mul(Rat(Int(1), Pow(10, 6)), Sym("teraw"));
            c[enums::FEMTOWATT] = Mul(Pow(10, 27), Sym("teraw"));
            c[enums::GIGAWATT] = Mul(Int(1000), Sym("teraw"));
            c[enums::HECTOWATT] = Mul(Pow(10, 10), Sym("teraw"));
            c[enums::KILOWATT] = Mul(Pow(10, 9), Sym("teraw"));
            c[enums::MEGAWATT] = Mul(Pow(10, 6), Sym("teraw"));
            c[enums::MICROWATT] = Mul(Pow(10, 18), Sym("teraw"));
            c[enums::MILLIWATT] = Mul(Pow(10, 15), Sym("teraw"));
            c[enums::NANOWATT] = Mul(Pow(10, 21), Sym("teraw"));
            c[enums::PETAWATT] = Mul(Rat(Int(1), Int(1000)), Sym("teraw"));
            c[enums::PICOWATT] = Mul(Pow(10, 24), Sym("teraw"));
            c[enums::WATT] = Mul(Pow(10, 12), Sym("teraw"));
            c[enums::YOCTOWATT] = Mul(Pow(10, 36), Sym("teraw"));
            c[enums::YOTTAWATT] = Mul(Rat(Int(1), Pow(10, 12)), Sym("teraw"));
            c[enums::ZEPTOWATT] = Mul(Pow(10, 33), Sym("teraw"));
            c[enums::ZETTAWATT] = Mul(Rat(Int(1), Pow(10, 9)), Sym("teraw"));
            return c;
        }

        static std::map<UnitsPower, ConversionPtr> createMapWATT() {
            std::map<UnitsPower, ConversionPtr> c;
            c[enums::ATTOWATT] = Mul(Pow(10, 18), Sym("w"));
            c[enums::CENTIWATT] = Mul(Int(100), Sym("w"));
            c[enums::DECAWATT] = Mul(Rat(Int(1), Int(10)), Sym("w"));
            c[enums::DECIWATT] = Mul(Int(10), Sym("w"));
            c[enums::EXAWATT] = Mul(Rat(Int(1), Pow(10, 18)), Sym("w"));
            c[enums::FEMTOWATT] = Mul(Pow(10, 15), Sym("w"));
            c[enums::GIGAWATT] = Mul(Rat(Int(1), Pow(10, 9)), Sym("w"));
            c[enums::HECTOWATT] = Mul(Rat(Int(1), Int(100)), Sym("w"));
            c[enums::KILOWATT] = Mul(Rat(Int(1), Int(1000)), Sym("w"));
            c[enums::MEGAWATT] = Mul(Rat(Int(1), Pow(10, 6)), Sym("w"));
            c[enums::MICROWATT] = Mul(Pow(10, 6), Sym("w"));
            c[enums::MILLIWATT] = Mul(Int(1000), Sym("w"));
            c[enums::NANOWATT] = Mul(Pow(10, 9), Sym("w"));
            c[enums::PETAWATT] = Mul(Rat(Int(1), Pow(10, 15)), Sym("w"));
            c[enums::PICOWATT] = Mul(Pow(10, 12), Sym("w"));
            c[enums::TERAWATT] = Mul(Rat(Int(1), Pow(10, 12)), Sym("w"));
            c[enums::YOCTOWATT] = Mul(Pow(10, 24), Sym("w"));
            c[enums::YOTTAWATT] = Mul(Rat(Int(1), Pow(10, 24)), Sym("w"));
            c[enums::ZEPTOWATT] = Mul(Pow(10, 21), Sym("w"));
            c[enums::ZETTAWATT] = Mul(Rat(Int(1), Pow(10, 21)), Sym("w"));
            return c;
        }

        static std::map<UnitsPower, ConversionPtr> createMapYOCTOWATT() {
            std::map<UnitsPower, ConversionPtr> c;
            c[enums::ATTOWATT] = Mul(Rat(Int(1), Pow(10, 6)), Sym("yoctow"));
            c[enums::CENTIWATT] = Mul(Rat(Int(1), Pow(10, 22)), Sym("yoctow"));
            c[enums::DECAWATT] = Mul(Rat(Int(1), Pow(10, 25)), Sym("yoctow"));
            c[enums::DECIWATT] = Mul(Rat(Int(1), Pow(10, 23)), Sym("yoctow"));
            c[enums::EXAWATT] = Mul(Rat(Int(1), Pow(10, 42)), Sym("yoctow"));
            c[enums::FEMTOWATT] = Mul(Rat(Int(1), Pow(10, 9)), Sym("yoctow"));
            c[enums::GIGAWATT] = Mul(Rat(Int(1), Pow(10, 33)), Sym("yoctow"));
            c[enums::HECTOWATT] = Mul(Rat(Int(1), Pow(10, 26)), Sym("yoctow"));
            c[enums::KILOWATT] = Mul(Rat(Int(1), Pow(10, 27)), Sym("yoctow"));
            c[enums::MEGAWATT] = Mul(Rat(Int(1), Pow(10, 30)), Sym("yoctow"));
            c[enums::MICROWATT] = Mul(Rat(Int(1), Pow(10, 18)), Sym("yoctow"));
            c[enums::MILLIWATT] = Mul(Rat(Int(1), Pow(10, 21)), Sym("yoctow"));
            c[enums::NANOWATT] = Mul(Rat(Int(1), Pow(10, 15)), Sym("yoctow"));
            c[enums::PETAWATT] = Mul(Rat(Int(1), Pow(10, 39)), Sym("yoctow"));
            c[enums::PICOWATT] = Mul(Rat(Int(1), Pow(10, 12)), Sym("yoctow"));
            c[enums::TERAWATT] = Mul(Rat(Int(1), Pow(10, 36)), Sym("yoctow"));
            c[enums::WATT] = Mul(Rat(Int(1), Pow(10, 24)), Sym("yoctow"));
            c[enums::YOTTAWATT] = Mul(Rat(Int(1), Pow(10, 48)), Sym("yoctow"));
            c[enums::ZEPTOWATT] = Mul(Rat(Int(1), Int(1000)), Sym("yoctow"));
            c[enums::ZETTAWATT] = Mul(Rat(Int(1), Pow(10, 45)), Sym("yoctow"));
            return c;
        }

        static std::map<UnitsPower, ConversionPtr> createMapYOTTAWATT() {
            std::map<UnitsPower, ConversionPtr> c;
            c[enums::ATTOWATT] = Mul(Pow(10, 42), Sym("yottaw"));
            c[enums::CENTIWATT] = Mul(Pow(10, 26), Sym("yottaw"));
            c[enums::DECAWATT] = Mul(Pow(10, 23), Sym("yottaw"));
            c[enums::DECIWATT] = Mul(Pow(10, 25), Sym("yottaw"));
            c[enums::EXAWATT] = Mul(Pow(10, 6), Sym("yottaw"));
            c[enums::FEMTOWATT] = Mul(Pow(10, 39), Sym("yottaw"));
            c[enums::GIGAWATT] = Mul(Pow(10, 15), Sym("yottaw"));
            c[enums::HECTOWATT] = Mul(Pow(10, 22), Sym("yottaw"));
            c[enums::KILOWATT] = Mul(Pow(10, 21), Sym("yottaw"));
            c[enums::MEGAWATT] = Mul(Pow(10, 18), Sym("yottaw"));
            c[enums::MICROWATT] = Mul(Pow(10, 30), Sym("yottaw"));
            c[enums::MILLIWATT] = Mul(Pow(10, 27), Sym("yottaw"));
            c[enums::NANOWATT] = Mul(Pow(10, 33), Sym("yottaw"));
            c[enums::PETAWATT] = Mul(Pow(10, 9), Sym("yottaw"));
            c[enums::PICOWATT] = Mul(Pow(10, 36), Sym("yottaw"));
            c[enums::TERAWATT] = Mul(Pow(10, 12), Sym("yottaw"));
            c[enums::WATT] = Mul(Pow(10, 24), Sym("yottaw"));
            c[enums::YOCTOWATT] = Mul(Pow(10, 48), Sym("yottaw"));
            c[enums::ZEPTOWATT] = Mul(Pow(10, 45), Sym("yottaw"));
            c[enums::ZETTAWATT] = Mul(Int(1000), Sym("yottaw"));
            return c;
        }

        static std::map<UnitsPower, ConversionPtr> createMapZEPTOWATT() {
            std::map<UnitsPower, ConversionPtr> c;
            c[enums::ATTOWATT] = Mul(Rat(Int(1), Int(1000)), Sym("zeptow"));
            c[enums::CENTIWATT] = Mul(Rat(Int(1), Pow(10, 19)), Sym("zeptow"));
            c[enums::DECAWATT] = Mul(Rat(Int(1), Pow(10, 22)), Sym("zeptow"));
            c[enums::DECIWATT] = Mul(Rat(Int(1), Pow(10, 20)), Sym("zeptow"));
            c[enums::EXAWATT] = Mul(Rat(Int(1), Pow(10, 39)), Sym("zeptow"));
            c[enums::FEMTOWATT] = Mul(Rat(Int(1), Pow(10, 6)), Sym("zeptow"));
            c[enums::GIGAWATT] = Mul(Rat(Int(1), Pow(10, 30)), Sym("zeptow"));
            c[enums::HECTOWATT] = Mul(Rat(Int(1), Pow(10, 23)), Sym("zeptow"));
            c[enums::KILOWATT] = Mul(Rat(Int(1), Pow(10, 24)), Sym("zeptow"));
            c[enums::MEGAWATT] = Mul(Rat(Int(1), Pow(10, 27)), Sym("zeptow"));
            c[enums::MICROWATT] = Mul(Rat(Int(1), Pow(10, 15)), Sym("zeptow"));
            c[enums::MILLIWATT] = Mul(Rat(Int(1), Pow(10, 18)), Sym("zeptow"));
            c[enums::NANOWATT] = Mul(Rat(Int(1), Pow(10, 12)), Sym("zeptow"));
            c[enums::PETAWATT] = Mul(Rat(Int(1), Pow(10, 36)), Sym("zeptow"));
            c[enums::PICOWATT] = Mul(Rat(Int(1), Pow(10, 9)), Sym("zeptow"));
            c[enums::TERAWATT] = Mul(Rat(Int(1), Pow(10, 33)), Sym("zeptow"));
            c[enums::WATT] = Mul(Rat(Int(1), Pow(10, 21)), Sym("zeptow"));
            c[enums::YOCTOWATT] = Mul(Int(1000), Sym("zeptow"));
            c[enums::YOTTAWATT] = Mul(Rat(Int(1), Pow(10, 45)), Sym("zeptow"));
            c[enums::ZETTAWATT] = Mul(Rat(Int(1), Pow(10, 42)), Sym("zeptow"));
            return c;
        }

        static std::map<UnitsPower, ConversionPtr> createMapZETTAWATT() {
            std::map<UnitsPower, ConversionPtr> c;
            c[enums::ATTOWATT] = Mul(Pow(10, 39), Sym("zettaw"));
            c[enums::CENTIWATT] = Mul(Pow(10, 23), Sym("zettaw"));
            c[enums::DECAWATT] = Mul(Pow(10, 20), Sym("zettaw"));
            c[enums::DECIWATT] = Mul(Pow(10, 22), Sym("zettaw"));
            c[enums::EXAWATT] = Mul(Int(1000), Sym("zettaw"));
            c[enums::FEMTOWATT] = Mul(Pow(10, 36), Sym("zettaw"));
            c[enums::GIGAWATT] = Mul(Pow(10, 12), Sym("zettaw"));
            c[enums::HECTOWATT] = Mul(Pow(10, 19), Sym("zettaw"));
            c[enums::KILOWATT] = Mul(Pow(10, 18), Sym("zettaw"));
            c[enums::MEGAWATT] = Mul(Pow(10, 15), Sym("zettaw"));
            c[enums::MICROWATT] = Mul(Pow(10, 27), Sym("zettaw"));
            c[enums::MILLIWATT] = Mul(Pow(10, 24), Sym("zettaw"));
            c[enums::NANOWATT] = Mul(Pow(10, 30), Sym("zettaw"));
            c[enums::PETAWATT] = Mul(Pow(10, 6), Sym("zettaw"));
            c[enums::PICOWATT] = Mul(Pow(10, 33), Sym("zettaw"));
            c[enums::TERAWATT] = Mul(Pow(10, 9), Sym("zettaw"));
            c[enums::WATT] = Mul(Pow(10, 21), Sym("zettaw"));
            c[enums::YOCTOWATT] = Mul(Pow(10, 45), Sym("zettaw"));
            c[enums::YOTTAWATT] = Mul(Rat(Int(1), Int(1000)), Sym("zettaw"));
            c[enums::ZEPTOWATT] = Mul(Pow(10, 42), Sym("zettaw"));
            return c;
        }

        static std::map<UnitsPower,
            std::map<UnitsPower, ConversionPtr> > makeConversions() {
            std::map<UnitsPower, std::map<UnitsPower, ConversionPtr> > c;
            c[enums::ATTOWATT] = createMapATTOWATT();
            c[enums::CENTIWATT] = createMapCENTIWATT();
            c[enums::DECAWATT] = createMapDECAWATT();
            c[enums::DECIWATT] = createMapDECIWATT();
            c[enums::EXAWATT] = createMapEXAWATT();
            c[enums::FEMTOWATT] = createMapFEMTOWATT();
            c[enums::GIGAWATT] = createMapGIGAWATT();
            c[enums::HECTOWATT] = createMapHECTOWATT();
            c[enums::KILOWATT] = createMapKILOWATT();
            c[enums::MEGAWATT] = createMapMEGAWATT();
            c[enums::MICROWATT] = createMapMICROWATT();
            c[enums::MILLIWATT] = createMapMILLIWATT();
            c[enums::NANOWATT] = createMapNANOWATT();
            c[enums::PETAWATT] = createMapPETAWATT();
            c[enums::PICOWATT] = createMapPICOWATT();
            c[enums::TERAWATT] = createMapTERAWATT();
            c[enums::WATT] = createMapWATT();
            c[enums::YOCTOWATT] = createMapYOCTOWATT();
            c[enums::YOTTAWATT] = createMapYOTTAWATT();
            c[enums::ZEPTOWATT] = createMapZEPTOWATT();
            c[enums::ZETTAWATT] = createMapZETTAWATT();
            return c;
        }

        static std::map<UnitsPower, std::string> makeSymbols(){
            std::map<UnitsPower, std::string> s;
            s[enums::ATTOWATT] = "aW";
            s[enums::CENTIWATT] = "cW";
            s[enums::DECAWATT] = "daW";
            s[enums::DECIWATT] = "dW";
            s[enums::EXAWATT] = "EW";
            s[enums::FEMTOWATT] = "fW";
            s[enums::GIGAWATT] = "GW";
            s[enums::HECTOWATT] = "hW";
            s[enums::KILOWATT] = "kW";
            s[enums::MEGAWATT] = "MW";
            s[enums::MICROWATT] = "µW";
            s[enums::MILLIWATT] = "mW";
            s[enums::NANOWATT] = "nW";
            s[enums::PETAWATT] = "PW";
            s[enums::PICOWATT] = "pW";
            s[enums::TERAWATT] = "TW";
            s[enums::WATT] = "W";
            s[enums::YOCTOWATT] = "yW";
            s[enums::YOTTAWATT] = "YW";
            s[enums::ZEPTOWATT] = "zW";
            s[enums::ZETTAWATT] = "ZW";
            return s;
        }

        std::map<UnitsPower,
            std::map<UnitsPower, ConversionPtr> > PowerI::CONVERSIONS = makeConversions();

        std::map<UnitsPower, std::string> PowerI::SYMBOLS = makeSymbols();

        PowerI::~PowerI() {}

        PowerI::PowerI() : Power() {
        }

        PowerI::PowerI(const double& value, const UnitsPower& unit) : Power() {
            setValue(value);
            setUnit(unit);
        }

        PowerI::PowerI(const PowerPtr& value, const UnitsPower& target) : Power() {
            double orig = value->getValue();
            UnitsPower source = value->getUnit();
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

        Ice::Double PowerI::getValue(const Ice::Current& /* current */) {
            return value;
        }

        void PowerI::setValue(Ice::Double _value, const Ice::Current& /* current */) {
            value = _value;
        }

        UnitsPower PowerI::getUnit(const Ice::Current& /* current */) {
            return unit;
        }

        void PowerI::setUnit(UnitsPower _unit, const Ice::Current& /* current */) {
            unit = _unit;
        }

        std::string PowerI::getSymbol(const Ice::Current& /* current */) {
            return SYMBOLS[unit];
        }

        PowerPtr PowerI::copy(const Ice::Current& /* current */) {
            PowerPtr copy = new PowerI();
            copy->setValue(getValue());
            copy->setUnit(getUnit());
            return copy;
        }
    }
}

