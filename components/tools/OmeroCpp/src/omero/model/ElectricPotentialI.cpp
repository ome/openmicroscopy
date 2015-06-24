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

#include <omero/model/ElectricPotentialI.h>
#include <omero/ClientErrors.h>

::Ice::Object* IceInternal::upCast(::omero::model::ElectricPotentialI* t) { return t; }

using namespace omero::conversions;

typedef omero::model::enums::UnitsElectricPotential UnitsElectricPotential;

namespace omero {

    namespace model {

        static std::map<UnitsElectricPotential, ConversionPtr> createMapATTOVOLT() {
            std::map<UnitsElectricPotential, ConversionPtr> c;
            c[enums::CENTIVOLT] = Mul(Rat(Int(1), Pow(10, 16)), Sym("attov"));
            c[enums::DECAVOLT] = Mul(Rat(Int(1), Pow(10, 19)), Sym("attov"));
            c[enums::DECIVOLT] = Mul(Rat(Int(1), Pow(10, 17)), Sym("attov"));
            c[enums::EXAVOLT] = Mul(Rat(Int(1), Pow(10, 36)), Sym("attov"));
            c[enums::FEMTOVOLT] = Mul(Rat(Int(1), Int(1000)), Sym("attov"));
            c[enums::GIGAVOLT] = Mul(Rat(Int(1), Pow(10, 27)), Sym("attov"));
            c[enums::HECTOVOLT] = Mul(Rat(Int(1), Pow(10, 20)), Sym("attov"));
            c[enums::KILOVOLT] = Mul(Rat(Int(1), Pow(10, 21)), Sym("attov"));
            c[enums::MEGAVOLT] = Mul(Rat(Int(1), Pow(10, 24)), Sym("attov"));
            c[enums::MICROVOLT] = Mul(Rat(Int(1), Pow(10, 12)), Sym("attov"));
            c[enums::MILLIVOLT] = Mul(Rat(Int(1), Pow(10, 15)), Sym("attov"));
            c[enums::NANOVOLT] = Mul(Rat(Int(1), Pow(10, 9)), Sym("attov"));
            c[enums::PETAVOLT] = Mul(Rat(Int(1), Pow(10, 33)), Sym("attov"));
            c[enums::PICOVOLT] = Mul(Rat(Int(1), Pow(10, 6)), Sym("attov"));
            c[enums::TERAVOLT] = Mul(Rat(Int(1), Pow(10, 30)), Sym("attov"));
            c[enums::VOLT] = Mul(Rat(Int(1), Pow(10, 18)), Sym("attov"));
            c[enums::YOCTOVOLT] = Mul(Pow(10, 6), Sym("attov"));
            c[enums::YOTTAVOLT] = Mul(Rat(Int(1), Pow(10, 42)), Sym("attov"));
            c[enums::ZEPTOVOLT] = Mul(Int(1000), Sym("attov"));
            c[enums::ZETTAVOLT] = Mul(Rat(Int(1), Pow(10, 39)), Sym("attov"));
            return c;
        }

        static std::map<UnitsElectricPotential, ConversionPtr> createMapCENTIVOLT() {
            std::map<UnitsElectricPotential, ConversionPtr> c;
            c[enums::ATTOVOLT] = Mul(Pow(10, 16), Sym("centiv"));
            c[enums::DECAVOLT] = Mul(Rat(Int(1), Int(1000)), Sym("centiv"));
            c[enums::DECIVOLT] = Mul(Rat(Int(1), Int(10)), Sym("centiv"));
            c[enums::EXAVOLT] = Mul(Rat(Int(1), Pow(10, 20)), Sym("centiv"));
            c[enums::FEMTOVOLT] = Mul(Pow(10, 13), Sym("centiv"));
            c[enums::GIGAVOLT] = Mul(Rat(Int(1), Pow(10, 11)), Sym("centiv"));
            c[enums::HECTOVOLT] = Mul(Rat(Int(1), Pow(10, 4)), Sym("centiv"));
            c[enums::KILOVOLT] = Mul(Rat(Int(1), Pow(10, 5)), Sym("centiv"));
            c[enums::MEGAVOLT] = Mul(Rat(Int(1), Pow(10, 8)), Sym("centiv"));
            c[enums::MICROVOLT] = Mul(Pow(10, 4), Sym("centiv"));
            c[enums::MILLIVOLT] = Mul(Int(10), Sym("centiv"));
            c[enums::NANOVOLT] = Mul(Pow(10, 7), Sym("centiv"));
            c[enums::PETAVOLT] = Mul(Rat(Int(1), Pow(10, 17)), Sym("centiv"));
            c[enums::PICOVOLT] = Mul(Pow(10, 10), Sym("centiv"));
            c[enums::TERAVOLT] = Mul(Rat(Int(1), Pow(10, 14)), Sym("centiv"));
            c[enums::VOLT] = Mul(Rat(Int(1), Int(100)), Sym("centiv"));
            c[enums::YOCTOVOLT] = Mul(Pow(10, 22), Sym("centiv"));
            c[enums::YOTTAVOLT] = Mul(Rat(Int(1), Pow(10, 26)), Sym("centiv"));
            c[enums::ZEPTOVOLT] = Mul(Pow(10, 19), Sym("centiv"));
            c[enums::ZETTAVOLT] = Mul(Rat(Int(1), Pow(10, 23)), Sym("centiv"));
            return c;
        }

        static std::map<UnitsElectricPotential, ConversionPtr> createMapDECAVOLT() {
            std::map<UnitsElectricPotential, ConversionPtr> c;
            c[enums::ATTOVOLT] = Mul(Pow(10, 19), Sym("decav"));
            c[enums::CENTIVOLT] = Mul(Int(1000), Sym("decav"));
            c[enums::DECIVOLT] = Mul(Int(100), Sym("decav"));
            c[enums::EXAVOLT] = Mul(Rat(Int(1), Pow(10, 17)), Sym("decav"));
            c[enums::FEMTOVOLT] = Mul(Pow(10, 16), Sym("decav"));
            c[enums::GIGAVOLT] = Mul(Rat(Int(1), Pow(10, 8)), Sym("decav"));
            c[enums::HECTOVOLT] = Mul(Rat(Int(1), Int(10)), Sym("decav"));
            c[enums::KILOVOLT] = Mul(Rat(Int(1), Int(100)), Sym("decav"));
            c[enums::MEGAVOLT] = Mul(Rat(Int(1), Pow(10, 5)), Sym("decav"));
            c[enums::MICROVOLT] = Mul(Pow(10, 7), Sym("decav"));
            c[enums::MILLIVOLT] = Mul(Pow(10, 4), Sym("decav"));
            c[enums::NANOVOLT] = Mul(Pow(10, 10), Sym("decav"));
            c[enums::PETAVOLT] = Mul(Rat(Int(1), Pow(10, 14)), Sym("decav"));
            c[enums::PICOVOLT] = Mul(Pow(10, 13), Sym("decav"));
            c[enums::TERAVOLT] = Mul(Rat(Int(1), Pow(10, 11)), Sym("decav"));
            c[enums::VOLT] = Mul(Int(10), Sym("decav"));
            c[enums::YOCTOVOLT] = Mul(Pow(10, 25), Sym("decav"));
            c[enums::YOTTAVOLT] = Mul(Rat(Int(1), Pow(10, 23)), Sym("decav"));
            c[enums::ZEPTOVOLT] = Mul(Pow(10, 22), Sym("decav"));
            c[enums::ZETTAVOLT] = Mul(Rat(Int(1), Pow(10, 20)), Sym("decav"));
            return c;
        }

        static std::map<UnitsElectricPotential, ConversionPtr> createMapDECIVOLT() {
            std::map<UnitsElectricPotential, ConversionPtr> c;
            c[enums::ATTOVOLT] = Mul(Pow(10, 17), Sym("deciv"));
            c[enums::CENTIVOLT] = Mul(Int(10), Sym("deciv"));
            c[enums::DECAVOLT] = Mul(Rat(Int(1), Int(100)), Sym("deciv"));
            c[enums::EXAVOLT] = Mul(Rat(Int(1), Pow(10, 19)), Sym("deciv"));
            c[enums::FEMTOVOLT] = Mul(Pow(10, 14), Sym("deciv"));
            c[enums::GIGAVOLT] = Mul(Rat(Int(1), Pow(10, 10)), Sym("deciv"));
            c[enums::HECTOVOLT] = Mul(Rat(Int(1), Int(1000)), Sym("deciv"));
            c[enums::KILOVOLT] = Mul(Rat(Int(1), Pow(10, 4)), Sym("deciv"));
            c[enums::MEGAVOLT] = Mul(Rat(Int(1), Pow(10, 7)), Sym("deciv"));
            c[enums::MICROVOLT] = Mul(Pow(10, 5), Sym("deciv"));
            c[enums::MILLIVOLT] = Mul(Int(100), Sym("deciv"));
            c[enums::NANOVOLT] = Mul(Pow(10, 8), Sym("deciv"));
            c[enums::PETAVOLT] = Mul(Rat(Int(1), Pow(10, 16)), Sym("deciv"));
            c[enums::PICOVOLT] = Mul(Pow(10, 11), Sym("deciv"));
            c[enums::TERAVOLT] = Mul(Rat(Int(1), Pow(10, 13)), Sym("deciv"));
            c[enums::VOLT] = Mul(Rat(Int(1), Int(10)), Sym("deciv"));
            c[enums::YOCTOVOLT] = Mul(Pow(10, 23), Sym("deciv"));
            c[enums::YOTTAVOLT] = Mul(Rat(Int(1), Pow(10, 25)), Sym("deciv"));
            c[enums::ZEPTOVOLT] = Mul(Pow(10, 20), Sym("deciv"));
            c[enums::ZETTAVOLT] = Mul(Rat(Int(1), Pow(10, 22)), Sym("deciv"));
            return c;
        }

        static std::map<UnitsElectricPotential, ConversionPtr> createMapEXAVOLT() {
            std::map<UnitsElectricPotential, ConversionPtr> c;
            c[enums::ATTOVOLT] = Mul(Pow(10, 36), Sym("exav"));
            c[enums::CENTIVOLT] = Mul(Pow(10, 20), Sym("exav"));
            c[enums::DECAVOLT] = Mul(Pow(10, 17), Sym("exav"));
            c[enums::DECIVOLT] = Mul(Pow(10, 19), Sym("exav"));
            c[enums::FEMTOVOLT] = Mul(Pow(10, 33), Sym("exav"));
            c[enums::GIGAVOLT] = Mul(Pow(10, 9), Sym("exav"));
            c[enums::HECTOVOLT] = Mul(Pow(10, 16), Sym("exav"));
            c[enums::KILOVOLT] = Mul(Pow(10, 15), Sym("exav"));
            c[enums::MEGAVOLT] = Mul(Pow(10, 12), Sym("exav"));
            c[enums::MICROVOLT] = Mul(Pow(10, 24), Sym("exav"));
            c[enums::MILLIVOLT] = Mul(Pow(10, 21), Sym("exav"));
            c[enums::NANOVOLT] = Mul(Pow(10, 27), Sym("exav"));
            c[enums::PETAVOLT] = Mul(Int(1000), Sym("exav"));
            c[enums::PICOVOLT] = Mul(Pow(10, 30), Sym("exav"));
            c[enums::TERAVOLT] = Mul(Pow(10, 6), Sym("exav"));
            c[enums::VOLT] = Mul(Pow(10, 18), Sym("exav"));
            c[enums::YOCTOVOLT] = Mul(Pow(10, 42), Sym("exav"));
            c[enums::YOTTAVOLT] = Mul(Rat(Int(1), Pow(10, 6)), Sym("exav"));
            c[enums::ZEPTOVOLT] = Mul(Pow(10, 39), Sym("exav"));
            c[enums::ZETTAVOLT] = Mul(Rat(Int(1), Int(1000)), Sym("exav"));
            return c;
        }

        static std::map<UnitsElectricPotential, ConversionPtr> createMapFEMTOVOLT() {
            std::map<UnitsElectricPotential, ConversionPtr> c;
            c[enums::ATTOVOLT] = Mul(Int(1000), Sym("femtov"));
            c[enums::CENTIVOLT] = Mul(Rat(Int(1), Pow(10, 13)), Sym("femtov"));
            c[enums::DECAVOLT] = Mul(Rat(Int(1), Pow(10, 16)), Sym("femtov"));
            c[enums::DECIVOLT] = Mul(Rat(Int(1), Pow(10, 14)), Sym("femtov"));
            c[enums::EXAVOLT] = Mul(Rat(Int(1), Pow(10, 33)), Sym("femtov"));
            c[enums::GIGAVOLT] = Mul(Rat(Int(1), Pow(10, 24)), Sym("femtov"));
            c[enums::HECTOVOLT] = Mul(Rat(Int(1), Pow(10, 17)), Sym("femtov"));
            c[enums::KILOVOLT] = Mul(Rat(Int(1), Pow(10, 18)), Sym("femtov"));
            c[enums::MEGAVOLT] = Mul(Rat(Int(1), Pow(10, 21)), Sym("femtov"));
            c[enums::MICROVOLT] = Mul(Rat(Int(1), Pow(10, 9)), Sym("femtov"));
            c[enums::MILLIVOLT] = Mul(Rat(Int(1), Pow(10, 12)), Sym("femtov"));
            c[enums::NANOVOLT] = Mul(Rat(Int(1), Pow(10, 6)), Sym("femtov"));
            c[enums::PETAVOLT] = Mul(Rat(Int(1), Pow(10, 30)), Sym("femtov"));
            c[enums::PICOVOLT] = Mul(Rat(Int(1), Int(1000)), Sym("femtov"));
            c[enums::TERAVOLT] = Mul(Rat(Int(1), Pow(10, 27)), Sym("femtov"));
            c[enums::VOLT] = Mul(Rat(Int(1), Pow(10, 15)), Sym("femtov"));
            c[enums::YOCTOVOLT] = Mul(Pow(10, 9), Sym("femtov"));
            c[enums::YOTTAVOLT] = Mul(Rat(Int(1), Pow(10, 39)), Sym("femtov"));
            c[enums::ZEPTOVOLT] = Mul(Pow(10, 6), Sym("femtov"));
            c[enums::ZETTAVOLT] = Mul(Rat(Int(1), Pow(10, 36)), Sym("femtov"));
            return c;
        }

        static std::map<UnitsElectricPotential, ConversionPtr> createMapGIGAVOLT() {
            std::map<UnitsElectricPotential, ConversionPtr> c;
            c[enums::ATTOVOLT] = Mul(Pow(10, 27), Sym("gigav"));
            c[enums::CENTIVOLT] = Mul(Pow(10, 11), Sym("gigav"));
            c[enums::DECAVOLT] = Mul(Pow(10, 8), Sym("gigav"));
            c[enums::DECIVOLT] = Mul(Pow(10, 10), Sym("gigav"));
            c[enums::EXAVOLT] = Mul(Rat(Int(1), Pow(10, 9)), Sym("gigav"));
            c[enums::FEMTOVOLT] = Mul(Pow(10, 24), Sym("gigav"));
            c[enums::HECTOVOLT] = Mul(Pow(10, 7), Sym("gigav"));
            c[enums::KILOVOLT] = Mul(Pow(10, 6), Sym("gigav"));
            c[enums::MEGAVOLT] = Mul(Int(1000), Sym("gigav"));
            c[enums::MICROVOLT] = Mul(Pow(10, 15), Sym("gigav"));
            c[enums::MILLIVOLT] = Mul(Pow(10, 12), Sym("gigav"));
            c[enums::NANOVOLT] = Mul(Pow(10, 18), Sym("gigav"));
            c[enums::PETAVOLT] = Mul(Rat(Int(1), Pow(10, 6)), Sym("gigav"));
            c[enums::PICOVOLT] = Mul(Pow(10, 21), Sym("gigav"));
            c[enums::TERAVOLT] = Mul(Rat(Int(1), Int(1000)), Sym("gigav"));
            c[enums::VOLT] = Mul(Pow(10, 9), Sym("gigav"));
            c[enums::YOCTOVOLT] = Mul(Pow(10, 33), Sym("gigav"));
            c[enums::YOTTAVOLT] = Mul(Rat(Int(1), Pow(10, 15)), Sym("gigav"));
            c[enums::ZEPTOVOLT] = Mul(Pow(10, 30), Sym("gigav"));
            c[enums::ZETTAVOLT] = Mul(Rat(Int(1), Pow(10, 12)), Sym("gigav"));
            return c;
        }

        static std::map<UnitsElectricPotential, ConversionPtr> createMapHECTOVOLT() {
            std::map<UnitsElectricPotential, ConversionPtr> c;
            c[enums::ATTOVOLT] = Mul(Pow(10, 20), Sym("hectov"));
            c[enums::CENTIVOLT] = Mul(Pow(10, 4), Sym("hectov"));
            c[enums::DECAVOLT] = Mul(Int(10), Sym("hectov"));
            c[enums::DECIVOLT] = Mul(Int(1000), Sym("hectov"));
            c[enums::EXAVOLT] = Mul(Rat(Int(1), Pow(10, 16)), Sym("hectov"));
            c[enums::FEMTOVOLT] = Mul(Pow(10, 17), Sym("hectov"));
            c[enums::GIGAVOLT] = Mul(Rat(Int(1), Pow(10, 7)), Sym("hectov"));
            c[enums::KILOVOLT] = Mul(Rat(Int(1), Int(10)), Sym("hectov"));
            c[enums::MEGAVOLT] = Mul(Rat(Int(1), Pow(10, 4)), Sym("hectov"));
            c[enums::MICROVOLT] = Mul(Pow(10, 8), Sym("hectov"));
            c[enums::MILLIVOLT] = Mul(Pow(10, 5), Sym("hectov"));
            c[enums::NANOVOLT] = Mul(Pow(10, 11), Sym("hectov"));
            c[enums::PETAVOLT] = Mul(Rat(Int(1), Pow(10, 13)), Sym("hectov"));
            c[enums::PICOVOLT] = Mul(Pow(10, 14), Sym("hectov"));
            c[enums::TERAVOLT] = Mul(Rat(Int(1), Pow(10, 10)), Sym("hectov"));
            c[enums::VOLT] = Mul(Int(100), Sym("hectov"));
            c[enums::YOCTOVOLT] = Mul(Pow(10, 26), Sym("hectov"));
            c[enums::YOTTAVOLT] = Mul(Rat(Int(1), Pow(10, 22)), Sym("hectov"));
            c[enums::ZEPTOVOLT] = Mul(Pow(10, 23), Sym("hectov"));
            c[enums::ZETTAVOLT] = Mul(Rat(Int(1), Pow(10, 19)), Sym("hectov"));
            return c;
        }

        static std::map<UnitsElectricPotential, ConversionPtr> createMapKILOVOLT() {
            std::map<UnitsElectricPotential, ConversionPtr> c;
            c[enums::ATTOVOLT] = Mul(Pow(10, 21), Sym("kilov"));
            c[enums::CENTIVOLT] = Mul(Pow(10, 5), Sym("kilov"));
            c[enums::DECAVOLT] = Mul(Int(100), Sym("kilov"));
            c[enums::DECIVOLT] = Mul(Pow(10, 4), Sym("kilov"));
            c[enums::EXAVOLT] = Mul(Rat(Int(1), Pow(10, 15)), Sym("kilov"));
            c[enums::FEMTOVOLT] = Mul(Pow(10, 18), Sym("kilov"));
            c[enums::GIGAVOLT] = Mul(Rat(Int(1), Pow(10, 6)), Sym("kilov"));
            c[enums::HECTOVOLT] = Mul(Int(10), Sym("kilov"));
            c[enums::MEGAVOLT] = Mul(Rat(Int(1), Int(1000)), Sym("kilov"));
            c[enums::MICROVOLT] = Mul(Pow(10, 9), Sym("kilov"));
            c[enums::MILLIVOLT] = Mul(Pow(10, 6), Sym("kilov"));
            c[enums::NANOVOLT] = Mul(Pow(10, 12), Sym("kilov"));
            c[enums::PETAVOLT] = Mul(Rat(Int(1), Pow(10, 12)), Sym("kilov"));
            c[enums::PICOVOLT] = Mul(Pow(10, 15), Sym("kilov"));
            c[enums::TERAVOLT] = Mul(Rat(Int(1), Pow(10, 9)), Sym("kilov"));
            c[enums::VOLT] = Mul(Int(1000), Sym("kilov"));
            c[enums::YOCTOVOLT] = Mul(Pow(10, 27), Sym("kilov"));
            c[enums::YOTTAVOLT] = Mul(Rat(Int(1), Pow(10, 21)), Sym("kilov"));
            c[enums::ZEPTOVOLT] = Mul(Pow(10, 24), Sym("kilov"));
            c[enums::ZETTAVOLT] = Mul(Rat(Int(1), Pow(10, 18)), Sym("kilov"));
            return c;
        }

        static std::map<UnitsElectricPotential, ConversionPtr> createMapMEGAVOLT() {
            std::map<UnitsElectricPotential, ConversionPtr> c;
            c[enums::ATTOVOLT] = Mul(Pow(10, 24), Sym("megav"));
            c[enums::CENTIVOLT] = Mul(Pow(10, 8), Sym("megav"));
            c[enums::DECAVOLT] = Mul(Pow(10, 5), Sym("megav"));
            c[enums::DECIVOLT] = Mul(Pow(10, 7), Sym("megav"));
            c[enums::EXAVOLT] = Mul(Rat(Int(1), Pow(10, 12)), Sym("megav"));
            c[enums::FEMTOVOLT] = Mul(Pow(10, 21), Sym("megav"));
            c[enums::GIGAVOLT] = Mul(Rat(Int(1), Int(1000)), Sym("megav"));
            c[enums::HECTOVOLT] = Mul(Pow(10, 4), Sym("megav"));
            c[enums::KILOVOLT] = Mul(Int(1000), Sym("megav"));
            c[enums::MICROVOLT] = Mul(Pow(10, 12), Sym("megav"));
            c[enums::MILLIVOLT] = Mul(Pow(10, 9), Sym("megav"));
            c[enums::NANOVOLT] = Mul(Pow(10, 15), Sym("megav"));
            c[enums::PETAVOLT] = Mul(Rat(Int(1), Pow(10, 9)), Sym("megav"));
            c[enums::PICOVOLT] = Mul(Pow(10, 18), Sym("megav"));
            c[enums::TERAVOLT] = Mul(Rat(Int(1), Pow(10, 6)), Sym("megav"));
            c[enums::VOLT] = Mul(Pow(10, 6), Sym("megav"));
            c[enums::YOCTOVOLT] = Mul(Pow(10, 30), Sym("megav"));
            c[enums::YOTTAVOLT] = Mul(Rat(Int(1), Pow(10, 18)), Sym("megav"));
            c[enums::ZEPTOVOLT] = Mul(Pow(10, 27), Sym("megav"));
            c[enums::ZETTAVOLT] = Mul(Rat(Int(1), Pow(10, 15)), Sym("megav"));
            return c;
        }

        static std::map<UnitsElectricPotential, ConversionPtr> createMapMICROVOLT() {
            std::map<UnitsElectricPotential, ConversionPtr> c;
            c[enums::ATTOVOLT] = Mul(Pow(10, 12), Sym("microv"));
            c[enums::CENTIVOLT] = Mul(Rat(Int(1), Pow(10, 4)), Sym("microv"));
            c[enums::DECAVOLT] = Mul(Rat(Int(1), Pow(10, 7)), Sym("microv"));
            c[enums::DECIVOLT] = Mul(Rat(Int(1), Pow(10, 5)), Sym("microv"));
            c[enums::EXAVOLT] = Mul(Rat(Int(1), Pow(10, 24)), Sym("microv"));
            c[enums::FEMTOVOLT] = Mul(Pow(10, 9), Sym("microv"));
            c[enums::GIGAVOLT] = Mul(Rat(Int(1), Pow(10, 15)), Sym("microv"));
            c[enums::HECTOVOLT] = Mul(Rat(Int(1), Pow(10, 8)), Sym("microv"));
            c[enums::KILOVOLT] = Mul(Rat(Int(1), Pow(10, 9)), Sym("microv"));
            c[enums::MEGAVOLT] = Mul(Rat(Int(1), Pow(10, 12)), Sym("microv"));
            c[enums::MILLIVOLT] = Mul(Rat(Int(1), Int(1000)), Sym("microv"));
            c[enums::NANOVOLT] = Mul(Int(1000), Sym("microv"));
            c[enums::PETAVOLT] = Mul(Rat(Int(1), Pow(10, 21)), Sym("microv"));
            c[enums::PICOVOLT] = Mul(Pow(10, 6), Sym("microv"));
            c[enums::TERAVOLT] = Mul(Rat(Int(1), Pow(10, 18)), Sym("microv"));
            c[enums::VOLT] = Mul(Rat(Int(1), Pow(10, 6)), Sym("microv"));
            c[enums::YOCTOVOLT] = Mul(Pow(10, 18), Sym("microv"));
            c[enums::YOTTAVOLT] = Mul(Rat(Int(1), Pow(10, 30)), Sym("microv"));
            c[enums::ZEPTOVOLT] = Mul(Pow(10, 15), Sym("microv"));
            c[enums::ZETTAVOLT] = Mul(Rat(Int(1), Pow(10, 27)), Sym("microv"));
            return c;
        }

        static std::map<UnitsElectricPotential, ConversionPtr> createMapMILLIVOLT() {
            std::map<UnitsElectricPotential, ConversionPtr> c;
            c[enums::ATTOVOLT] = Mul(Pow(10, 15), Sym("milliv"));
            c[enums::CENTIVOLT] = Mul(Rat(Int(1), Int(10)), Sym("milliv"));
            c[enums::DECAVOLT] = Mul(Rat(Int(1), Pow(10, 4)), Sym("milliv"));
            c[enums::DECIVOLT] = Mul(Rat(Int(1), Int(100)), Sym("milliv"));
            c[enums::EXAVOLT] = Mul(Rat(Int(1), Pow(10, 21)), Sym("milliv"));
            c[enums::FEMTOVOLT] = Mul(Pow(10, 12), Sym("milliv"));
            c[enums::GIGAVOLT] = Mul(Rat(Int(1), Pow(10, 12)), Sym("milliv"));
            c[enums::HECTOVOLT] = Mul(Rat(Int(1), Pow(10, 5)), Sym("milliv"));
            c[enums::KILOVOLT] = Mul(Rat(Int(1), Pow(10, 6)), Sym("milliv"));
            c[enums::MEGAVOLT] = Mul(Rat(Int(1), Pow(10, 9)), Sym("milliv"));
            c[enums::MICROVOLT] = Mul(Int(1000), Sym("milliv"));
            c[enums::NANOVOLT] = Mul(Pow(10, 6), Sym("milliv"));
            c[enums::PETAVOLT] = Mul(Rat(Int(1), Pow(10, 18)), Sym("milliv"));
            c[enums::PICOVOLT] = Mul(Pow(10, 9), Sym("milliv"));
            c[enums::TERAVOLT] = Mul(Rat(Int(1), Pow(10, 15)), Sym("milliv"));
            c[enums::VOLT] = Mul(Rat(Int(1), Int(1000)), Sym("milliv"));
            c[enums::YOCTOVOLT] = Mul(Pow(10, 21), Sym("milliv"));
            c[enums::YOTTAVOLT] = Mul(Rat(Int(1), Pow(10, 27)), Sym("milliv"));
            c[enums::ZEPTOVOLT] = Mul(Pow(10, 18), Sym("milliv"));
            c[enums::ZETTAVOLT] = Mul(Rat(Int(1), Pow(10, 24)), Sym("milliv"));
            return c;
        }

        static std::map<UnitsElectricPotential, ConversionPtr> createMapNANOVOLT() {
            std::map<UnitsElectricPotential, ConversionPtr> c;
            c[enums::ATTOVOLT] = Mul(Pow(10, 9), Sym("nanov"));
            c[enums::CENTIVOLT] = Mul(Rat(Int(1), Pow(10, 7)), Sym("nanov"));
            c[enums::DECAVOLT] = Mul(Rat(Int(1), Pow(10, 10)), Sym("nanov"));
            c[enums::DECIVOLT] = Mul(Rat(Int(1), Pow(10, 8)), Sym("nanov"));
            c[enums::EXAVOLT] = Mul(Rat(Int(1), Pow(10, 27)), Sym("nanov"));
            c[enums::FEMTOVOLT] = Mul(Pow(10, 6), Sym("nanov"));
            c[enums::GIGAVOLT] = Mul(Rat(Int(1), Pow(10, 18)), Sym("nanov"));
            c[enums::HECTOVOLT] = Mul(Rat(Int(1), Pow(10, 11)), Sym("nanov"));
            c[enums::KILOVOLT] = Mul(Rat(Int(1), Pow(10, 12)), Sym("nanov"));
            c[enums::MEGAVOLT] = Mul(Rat(Int(1), Pow(10, 15)), Sym("nanov"));
            c[enums::MICROVOLT] = Mul(Rat(Int(1), Int(1000)), Sym("nanov"));
            c[enums::MILLIVOLT] = Mul(Rat(Int(1), Pow(10, 6)), Sym("nanov"));
            c[enums::PETAVOLT] = Mul(Rat(Int(1), Pow(10, 24)), Sym("nanov"));
            c[enums::PICOVOLT] = Mul(Int(1000), Sym("nanov"));
            c[enums::TERAVOLT] = Mul(Rat(Int(1), Pow(10, 21)), Sym("nanov"));
            c[enums::VOLT] = Mul(Rat(Int(1), Pow(10, 9)), Sym("nanov"));
            c[enums::YOCTOVOLT] = Mul(Pow(10, 15), Sym("nanov"));
            c[enums::YOTTAVOLT] = Mul(Rat(Int(1), Pow(10, 33)), Sym("nanov"));
            c[enums::ZEPTOVOLT] = Mul(Pow(10, 12), Sym("nanov"));
            c[enums::ZETTAVOLT] = Mul(Rat(Int(1), Pow(10, 30)), Sym("nanov"));
            return c;
        }

        static std::map<UnitsElectricPotential, ConversionPtr> createMapPETAVOLT() {
            std::map<UnitsElectricPotential, ConversionPtr> c;
            c[enums::ATTOVOLT] = Mul(Pow(10, 33), Sym("petav"));
            c[enums::CENTIVOLT] = Mul(Pow(10, 17), Sym("petav"));
            c[enums::DECAVOLT] = Mul(Pow(10, 14), Sym("petav"));
            c[enums::DECIVOLT] = Mul(Pow(10, 16), Sym("petav"));
            c[enums::EXAVOLT] = Mul(Rat(Int(1), Int(1000)), Sym("petav"));
            c[enums::FEMTOVOLT] = Mul(Pow(10, 30), Sym("petav"));
            c[enums::GIGAVOLT] = Mul(Pow(10, 6), Sym("petav"));
            c[enums::HECTOVOLT] = Mul(Pow(10, 13), Sym("petav"));
            c[enums::KILOVOLT] = Mul(Pow(10, 12), Sym("petav"));
            c[enums::MEGAVOLT] = Mul(Pow(10, 9), Sym("petav"));
            c[enums::MICROVOLT] = Mul(Pow(10, 21), Sym("petav"));
            c[enums::MILLIVOLT] = Mul(Pow(10, 18), Sym("petav"));
            c[enums::NANOVOLT] = Mul(Pow(10, 24), Sym("petav"));
            c[enums::PICOVOLT] = Mul(Pow(10, 27), Sym("petav"));
            c[enums::TERAVOLT] = Mul(Int(1000), Sym("petav"));
            c[enums::VOLT] = Mul(Pow(10, 15), Sym("petav"));
            c[enums::YOCTOVOLT] = Mul(Pow(10, 39), Sym("petav"));
            c[enums::YOTTAVOLT] = Mul(Rat(Int(1), Pow(10, 9)), Sym("petav"));
            c[enums::ZEPTOVOLT] = Mul(Pow(10, 36), Sym("petav"));
            c[enums::ZETTAVOLT] = Mul(Rat(Int(1), Pow(10, 6)), Sym("petav"));
            return c;
        }

        static std::map<UnitsElectricPotential, ConversionPtr> createMapPICOVOLT() {
            std::map<UnitsElectricPotential, ConversionPtr> c;
            c[enums::ATTOVOLT] = Mul(Pow(10, 6), Sym("picov"));
            c[enums::CENTIVOLT] = Mul(Rat(Int(1), Pow(10, 10)), Sym("picov"));
            c[enums::DECAVOLT] = Mul(Rat(Int(1), Pow(10, 13)), Sym("picov"));
            c[enums::DECIVOLT] = Mul(Rat(Int(1), Pow(10, 11)), Sym("picov"));
            c[enums::EXAVOLT] = Mul(Rat(Int(1), Pow(10, 30)), Sym("picov"));
            c[enums::FEMTOVOLT] = Mul(Int(1000), Sym("picov"));
            c[enums::GIGAVOLT] = Mul(Rat(Int(1), Pow(10, 21)), Sym("picov"));
            c[enums::HECTOVOLT] = Mul(Rat(Int(1), Pow(10, 14)), Sym("picov"));
            c[enums::KILOVOLT] = Mul(Rat(Int(1), Pow(10, 15)), Sym("picov"));
            c[enums::MEGAVOLT] = Mul(Rat(Int(1), Pow(10, 18)), Sym("picov"));
            c[enums::MICROVOLT] = Mul(Rat(Int(1), Pow(10, 6)), Sym("picov"));
            c[enums::MILLIVOLT] = Mul(Rat(Int(1), Pow(10, 9)), Sym("picov"));
            c[enums::NANOVOLT] = Mul(Rat(Int(1), Int(1000)), Sym("picov"));
            c[enums::PETAVOLT] = Mul(Rat(Int(1), Pow(10, 27)), Sym("picov"));
            c[enums::TERAVOLT] = Mul(Rat(Int(1), Pow(10, 24)), Sym("picov"));
            c[enums::VOLT] = Mul(Rat(Int(1), Pow(10, 12)), Sym("picov"));
            c[enums::YOCTOVOLT] = Mul(Pow(10, 12), Sym("picov"));
            c[enums::YOTTAVOLT] = Mul(Rat(Int(1), Pow(10, 36)), Sym("picov"));
            c[enums::ZEPTOVOLT] = Mul(Pow(10, 9), Sym("picov"));
            c[enums::ZETTAVOLT] = Mul(Rat(Int(1), Pow(10, 33)), Sym("picov"));
            return c;
        }

        static std::map<UnitsElectricPotential, ConversionPtr> createMapTERAVOLT() {
            std::map<UnitsElectricPotential, ConversionPtr> c;
            c[enums::ATTOVOLT] = Mul(Pow(10, 30), Sym("terav"));
            c[enums::CENTIVOLT] = Mul(Pow(10, 14), Sym("terav"));
            c[enums::DECAVOLT] = Mul(Pow(10, 11), Sym("terav"));
            c[enums::DECIVOLT] = Mul(Pow(10, 13), Sym("terav"));
            c[enums::EXAVOLT] = Mul(Rat(Int(1), Pow(10, 6)), Sym("terav"));
            c[enums::FEMTOVOLT] = Mul(Pow(10, 27), Sym("terav"));
            c[enums::GIGAVOLT] = Mul(Int(1000), Sym("terav"));
            c[enums::HECTOVOLT] = Mul(Pow(10, 10), Sym("terav"));
            c[enums::KILOVOLT] = Mul(Pow(10, 9), Sym("terav"));
            c[enums::MEGAVOLT] = Mul(Pow(10, 6), Sym("terav"));
            c[enums::MICROVOLT] = Mul(Pow(10, 18), Sym("terav"));
            c[enums::MILLIVOLT] = Mul(Pow(10, 15), Sym("terav"));
            c[enums::NANOVOLT] = Mul(Pow(10, 21), Sym("terav"));
            c[enums::PETAVOLT] = Mul(Rat(Int(1), Int(1000)), Sym("terav"));
            c[enums::PICOVOLT] = Mul(Pow(10, 24), Sym("terav"));
            c[enums::VOLT] = Mul(Pow(10, 12), Sym("terav"));
            c[enums::YOCTOVOLT] = Mul(Pow(10, 36), Sym("terav"));
            c[enums::YOTTAVOLT] = Mul(Rat(Int(1), Pow(10, 12)), Sym("terav"));
            c[enums::ZEPTOVOLT] = Mul(Pow(10, 33), Sym("terav"));
            c[enums::ZETTAVOLT] = Mul(Rat(Int(1), Pow(10, 9)), Sym("terav"));
            return c;
        }

        static std::map<UnitsElectricPotential, ConversionPtr> createMapVOLT() {
            std::map<UnitsElectricPotential, ConversionPtr> c;
            c[enums::ATTOVOLT] = Mul(Pow(10, 18), Sym("v"));
            c[enums::CENTIVOLT] = Mul(Int(100), Sym("v"));
            c[enums::DECAVOLT] = Mul(Rat(Int(1), Int(10)), Sym("v"));
            c[enums::DECIVOLT] = Mul(Int(10), Sym("v"));
            c[enums::EXAVOLT] = Mul(Rat(Int(1), Pow(10, 18)), Sym("v"));
            c[enums::FEMTOVOLT] = Mul(Pow(10, 15), Sym("v"));
            c[enums::GIGAVOLT] = Mul(Rat(Int(1), Pow(10, 9)), Sym("v"));
            c[enums::HECTOVOLT] = Mul(Rat(Int(1), Int(100)), Sym("v"));
            c[enums::KILOVOLT] = Mul(Rat(Int(1), Int(1000)), Sym("v"));
            c[enums::MEGAVOLT] = Mul(Rat(Int(1), Pow(10, 6)), Sym("v"));
            c[enums::MICROVOLT] = Mul(Pow(10, 6), Sym("v"));
            c[enums::MILLIVOLT] = Mul(Int(1000), Sym("v"));
            c[enums::NANOVOLT] = Mul(Pow(10, 9), Sym("v"));
            c[enums::PETAVOLT] = Mul(Rat(Int(1), Pow(10, 15)), Sym("v"));
            c[enums::PICOVOLT] = Mul(Pow(10, 12), Sym("v"));
            c[enums::TERAVOLT] = Mul(Rat(Int(1), Pow(10, 12)), Sym("v"));
            c[enums::YOCTOVOLT] = Mul(Pow(10, 24), Sym("v"));
            c[enums::YOTTAVOLT] = Mul(Rat(Int(1), Pow(10, 24)), Sym("v"));
            c[enums::ZEPTOVOLT] = Mul(Pow(10, 21), Sym("v"));
            c[enums::ZETTAVOLT] = Mul(Rat(Int(1), Pow(10, 21)), Sym("v"));
            return c;
        }

        static std::map<UnitsElectricPotential, ConversionPtr> createMapYOCTOVOLT() {
            std::map<UnitsElectricPotential, ConversionPtr> c;
            c[enums::ATTOVOLT] = Mul(Rat(Int(1), Pow(10, 6)), Sym("yoctov"));
            c[enums::CENTIVOLT] = Mul(Rat(Int(1), Pow(10, 22)), Sym("yoctov"));
            c[enums::DECAVOLT] = Mul(Rat(Int(1), Pow(10, 25)), Sym("yoctov"));
            c[enums::DECIVOLT] = Mul(Rat(Int(1), Pow(10, 23)), Sym("yoctov"));
            c[enums::EXAVOLT] = Mul(Rat(Int(1), Pow(10, 42)), Sym("yoctov"));
            c[enums::FEMTOVOLT] = Mul(Rat(Int(1), Pow(10, 9)), Sym("yoctov"));
            c[enums::GIGAVOLT] = Mul(Rat(Int(1), Pow(10, 33)), Sym("yoctov"));
            c[enums::HECTOVOLT] = Mul(Rat(Int(1), Pow(10, 26)), Sym("yoctov"));
            c[enums::KILOVOLT] = Mul(Rat(Int(1), Pow(10, 27)), Sym("yoctov"));
            c[enums::MEGAVOLT] = Mul(Rat(Int(1), Pow(10, 30)), Sym("yoctov"));
            c[enums::MICROVOLT] = Mul(Rat(Int(1), Pow(10, 18)), Sym("yoctov"));
            c[enums::MILLIVOLT] = Mul(Rat(Int(1), Pow(10, 21)), Sym("yoctov"));
            c[enums::NANOVOLT] = Mul(Rat(Int(1), Pow(10, 15)), Sym("yoctov"));
            c[enums::PETAVOLT] = Mul(Rat(Int(1), Pow(10, 39)), Sym("yoctov"));
            c[enums::PICOVOLT] = Mul(Rat(Int(1), Pow(10, 12)), Sym("yoctov"));
            c[enums::TERAVOLT] = Mul(Rat(Int(1), Pow(10, 36)), Sym("yoctov"));
            c[enums::VOLT] = Mul(Rat(Int(1), Pow(10, 24)), Sym("yoctov"));
            c[enums::YOTTAVOLT] = Mul(Rat(Int(1), Pow(10, 48)), Sym("yoctov"));
            c[enums::ZEPTOVOLT] = Mul(Rat(Int(1), Int(1000)), Sym("yoctov"));
            c[enums::ZETTAVOLT] = Mul(Rat(Int(1), Pow(10, 45)), Sym("yoctov"));
            return c;
        }

        static std::map<UnitsElectricPotential, ConversionPtr> createMapYOTTAVOLT() {
            std::map<UnitsElectricPotential, ConversionPtr> c;
            c[enums::ATTOVOLT] = Mul(Pow(10, 42), Sym("yottav"));
            c[enums::CENTIVOLT] = Mul(Pow(10, 26), Sym("yottav"));
            c[enums::DECAVOLT] = Mul(Pow(10, 23), Sym("yottav"));
            c[enums::DECIVOLT] = Mul(Pow(10, 25), Sym("yottav"));
            c[enums::EXAVOLT] = Mul(Pow(10, 6), Sym("yottav"));
            c[enums::FEMTOVOLT] = Mul(Pow(10, 39), Sym("yottav"));
            c[enums::GIGAVOLT] = Mul(Pow(10, 15), Sym("yottav"));
            c[enums::HECTOVOLT] = Mul(Pow(10, 22), Sym("yottav"));
            c[enums::KILOVOLT] = Mul(Pow(10, 21), Sym("yottav"));
            c[enums::MEGAVOLT] = Mul(Pow(10, 18), Sym("yottav"));
            c[enums::MICROVOLT] = Mul(Pow(10, 30), Sym("yottav"));
            c[enums::MILLIVOLT] = Mul(Pow(10, 27), Sym("yottav"));
            c[enums::NANOVOLT] = Mul(Pow(10, 33), Sym("yottav"));
            c[enums::PETAVOLT] = Mul(Pow(10, 9), Sym("yottav"));
            c[enums::PICOVOLT] = Mul(Pow(10, 36), Sym("yottav"));
            c[enums::TERAVOLT] = Mul(Pow(10, 12), Sym("yottav"));
            c[enums::VOLT] = Mul(Pow(10, 24), Sym("yottav"));
            c[enums::YOCTOVOLT] = Mul(Pow(10, 48), Sym("yottav"));
            c[enums::ZEPTOVOLT] = Mul(Pow(10, 45), Sym("yottav"));
            c[enums::ZETTAVOLT] = Mul(Int(1000), Sym("yottav"));
            return c;
        }

        static std::map<UnitsElectricPotential, ConversionPtr> createMapZEPTOVOLT() {
            std::map<UnitsElectricPotential, ConversionPtr> c;
            c[enums::ATTOVOLT] = Mul(Rat(Int(1), Int(1000)), Sym("zeptov"));
            c[enums::CENTIVOLT] = Mul(Rat(Int(1), Pow(10, 19)), Sym("zeptov"));
            c[enums::DECAVOLT] = Mul(Rat(Int(1), Pow(10, 22)), Sym("zeptov"));
            c[enums::DECIVOLT] = Mul(Rat(Int(1), Pow(10, 20)), Sym("zeptov"));
            c[enums::EXAVOLT] = Mul(Rat(Int(1), Pow(10, 39)), Sym("zeptov"));
            c[enums::FEMTOVOLT] = Mul(Rat(Int(1), Pow(10, 6)), Sym("zeptov"));
            c[enums::GIGAVOLT] = Mul(Rat(Int(1), Pow(10, 30)), Sym("zeptov"));
            c[enums::HECTOVOLT] = Mul(Rat(Int(1), Pow(10, 23)), Sym("zeptov"));
            c[enums::KILOVOLT] = Mul(Rat(Int(1), Pow(10, 24)), Sym("zeptov"));
            c[enums::MEGAVOLT] = Mul(Rat(Int(1), Pow(10, 27)), Sym("zeptov"));
            c[enums::MICROVOLT] = Mul(Rat(Int(1), Pow(10, 15)), Sym("zeptov"));
            c[enums::MILLIVOLT] = Mul(Rat(Int(1), Pow(10, 18)), Sym("zeptov"));
            c[enums::NANOVOLT] = Mul(Rat(Int(1), Pow(10, 12)), Sym("zeptov"));
            c[enums::PETAVOLT] = Mul(Rat(Int(1), Pow(10, 36)), Sym("zeptov"));
            c[enums::PICOVOLT] = Mul(Rat(Int(1), Pow(10, 9)), Sym("zeptov"));
            c[enums::TERAVOLT] = Mul(Rat(Int(1), Pow(10, 33)), Sym("zeptov"));
            c[enums::VOLT] = Mul(Rat(Int(1), Pow(10, 21)), Sym("zeptov"));
            c[enums::YOCTOVOLT] = Mul(Int(1000), Sym("zeptov"));
            c[enums::YOTTAVOLT] = Mul(Rat(Int(1), Pow(10, 45)), Sym("zeptov"));
            c[enums::ZETTAVOLT] = Mul(Rat(Int(1), Pow(10, 42)), Sym("zeptov"));
            return c;
        }

        static std::map<UnitsElectricPotential, ConversionPtr> createMapZETTAVOLT() {
            std::map<UnitsElectricPotential, ConversionPtr> c;
            c[enums::ATTOVOLT] = Mul(Pow(10, 39), Sym("zettav"));
            c[enums::CENTIVOLT] = Mul(Pow(10, 23), Sym("zettav"));
            c[enums::DECAVOLT] = Mul(Pow(10, 20), Sym("zettav"));
            c[enums::DECIVOLT] = Mul(Pow(10, 22), Sym("zettav"));
            c[enums::EXAVOLT] = Mul(Int(1000), Sym("zettav"));
            c[enums::FEMTOVOLT] = Mul(Pow(10, 36), Sym("zettav"));
            c[enums::GIGAVOLT] = Mul(Pow(10, 12), Sym("zettav"));
            c[enums::HECTOVOLT] = Mul(Pow(10, 19), Sym("zettav"));
            c[enums::KILOVOLT] = Mul(Pow(10, 18), Sym("zettav"));
            c[enums::MEGAVOLT] = Mul(Pow(10, 15), Sym("zettav"));
            c[enums::MICROVOLT] = Mul(Pow(10, 27), Sym("zettav"));
            c[enums::MILLIVOLT] = Mul(Pow(10, 24), Sym("zettav"));
            c[enums::NANOVOLT] = Mul(Pow(10, 30), Sym("zettav"));
            c[enums::PETAVOLT] = Mul(Pow(10, 6), Sym("zettav"));
            c[enums::PICOVOLT] = Mul(Pow(10, 33), Sym("zettav"));
            c[enums::TERAVOLT] = Mul(Pow(10, 9), Sym("zettav"));
            c[enums::VOLT] = Mul(Pow(10, 21), Sym("zettav"));
            c[enums::YOCTOVOLT] = Mul(Pow(10, 45), Sym("zettav"));
            c[enums::YOTTAVOLT] = Mul(Rat(Int(1), Int(1000)), Sym("zettav"));
            c[enums::ZEPTOVOLT] = Mul(Pow(10, 42), Sym("zettav"));
            return c;
        }

        static std::map<UnitsElectricPotential,
            std::map<UnitsElectricPotential, ConversionPtr> > makeConversions() {
            std::map<UnitsElectricPotential, std::map<UnitsElectricPotential, ConversionPtr> > c;
            c[enums::ATTOVOLT] = createMapATTOVOLT();
            c[enums::CENTIVOLT] = createMapCENTIVOLT();
            c[enums::DECAVOLT] = createMapDECAVOLT();
            c[enums::DECIVOLT] = createMapDECIVOLT();
            c[enums::EXAVOLT] = createMapEXAVOLT();
            c[enums::FEMTOVOLT] = createMapFEMTOVOLT();
            c[enums::GIGAVOLT] = createMapGIGAVOLT();
            c[enums::HECTOVOLT] = createMapHECTOVOLT();
            c[enums::KILOVOLT] = createMapKILOVOLT();
            c[enums::MEGAVOLT] = createMapMEGAVOLT();
            c[enums::MICROVOLT] = createMapMICROVOLT();
            c[enums::MILLIVOLT] = createMapMILLIVOLT();
            c[enums::NANOVOLT] = createMapNANOVOLT();
            c[enums::PETAVOLT] = createMapPETAVOLT();
            c[enums::PICOVOLT] = createMapPICOVOLT();
            c[enums::TERAVOLT] = createMapTERAVOLT();
            c[enums::VOLT] = createMapVOLT();
            c[enums::YOCTOVOLT] = createMapYOCTOVOLT();
            c[enums::YOTTAVOLT] = createMapYOTTAVOLT();
            c[enums::ZEPTOVOLT] = createMapZEPTOVOLT();
            c[enums::ZETTAVOLT] = createMapZETTAVOLT();
            return c;
        }

        static std::map<UnitsElectricPotential, std::string> makeSymbols(){
            std::map<UnitsElectricPotential, std::string> s;
            s[enums::ATTOVOLT] = "aV";
            s[enums::CENTIVOLT] = "cV";
            s[enums::DECAVOLT] = "daV";
            s[enums::DECIVOLT] = "dV";
            s[enums::EXAVOLT] = "EV";
            s[enums::FEMTOVOLT] = "fV";
            s[enums::GIGAVOLT] = "GV";
            s[enums::HECTOVOLT] = "hV";
            s[enums::KILOVOLT] = "kV";
            s[enums::MEGAVOLT] = "MV";
            s[enums::MICROVOLT] = "µV";
            s[enums::MILLIVOLT] = "mV";
            s[enums::NANOVOLT] = "nV";
            s[enums::PETAVOLT] = "PV";
            s[enums::PICOVOLT] = "pV";
            s[enums::TERAVOLT] = "TV";
            s[enums::VOLT] = "V";
            s[enums::YOCTOVOLT] = "yV";
            s[enums::YOTTAVOLT] = "YV";
            s[enums::ZEPTOVOLT] = "zV";
            s[enums::ZETTAVOLT] = "ZV";
            return s;
        }

        std::map<UnitsElectricPotential,
            std::map<UnitsElectricPotential, ConversionPtr> > ElectricPotentialI::CONVERSIONS = makeConversions();

        std::map<UnitsElectricPotential, std::string> ElectricPotentialI::SYMBOLS = makeSymbols();

        ElectricPotentialI::~ElectricPotentialI() {}

        ElectricPotentialI::ElectricPotentialI() : ElectricPotential() {
        }

        ElectricPotentialI::ElectricPotentialI(const double& value, const UnitsElectricPotential& unit) : ElectricPotential() {
            setValue(value);
            setUnit(unit);
        }

        ElectricPotentialI::ElectricPotentialI(const ElectricPotentialPtr& value, const UnitsElectricPotential& target) : ElectricPotential() {
            double orig = value->getValue();
            UnitsElectricPotential source = value->getUnit();
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

        Ice::Double ElectricPotentialI::getValue(const Ice::Current& /* current */) {
            return value;
        }

        void ElectricPotentialI::setValue(Ice::Double _value, const Ice::Current& /* current */) {
            value = _value;
        }

        UnitsElectricPotential ElectricPotentialI::getUnit(const Ice::Current& /* current */) {
            return unit;
        }

        void ElectricPotentialI::setUnit(UnitsElectricPotential _unit, const Ice::Current& /* current */) {
            unit = _unit;
        }

        std::string ElectricPotentialI::getSymbol(const Ice::Current& /* current */) {
            return SYMBOLS[unit];
        }

        ElectricPotentialPtr ElectricPotentialI::copy(const Ice::Current& /* current */) {
            ElectricPotentialPtr copy = new ElectricPotentialI();
            copy->setValue(getValue());
            copy->setUnit(getUnit());
            return copy;
        }
    }
}

