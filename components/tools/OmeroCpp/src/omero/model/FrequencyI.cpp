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

#include <omero/model/FrequencyI.h>
#include <omero/ClientErrors.h>

::Ice::Object* IceInternal::upCast(::omero::model::FrequencyI* t) { return t; }

using namespace omero::conversions;

typedef omero::conversion_types::ConversionPtr ConversionPtr;
typedef omero::model::enums::UnitsFrequency UnitsFrequency;

namespace omero {

    namespace model {

        static std::map<UnitsFrequency, ConversionPtr> createMapATTOHERTZ() {
            std::map<UnitsFrequency, ConversionPtr> c;
            c[enums::CENTIHERTZ] = Mul(Rat(Int(1), Pow(10, 16)), Sym("attohz"));
            c[enums::DECAHERTZ] = Mul(Rat(Int(1), Pow(10, 19)), Sym("attohz"));
            c[enums::DECIHERTZ] = Mul(Rat(Int(1), Pow(10, 17)), Sym("attohz"));
            c[enums::EXAHERTZ] = Mul(Rat(Int(1), Pow(10, 36)), Sym("attohz"));
            c[enums::FEMTOHERTZ] = Mul(Rat(Int(1), Int(1000)), Sym("attohz"));
            c[enums::GIGAHERTZ] = Mul(Rat(Int(1), Pow(10, 27)), Sym("attohz"));
            c[enums::HECTOHERTZ] = Mul(Rat(Int(1), Pow(10, 20)), Sym("attohz"));
            c[enums::HERTZ] = Mul(Rat(Int(1), Pow(10, 18)), Sym("attohz"));
            c[enums::KILOHERTZ] = Mul(Rat(Int(1), Pow(10, 21)), Sym("attohz"));
            c[enums::MEGAHERTZ] = Mul(Rat(Int(1), Pow(10, 24)), Sym("attohz"));
            c[enums::MICROHERTZ] = Mul(Rat(Int(1), Pow(10, 12)), Sym("attohz"));
            c[enums::MILLIHERTZ] = Mul(Rat(Int(1), Pow(10, 15)), Sym("attohz"));
            c[enums::NANOHERTZ] = Mul(Rat(Int(1), Pow(10, 9)), Sym("attohz"));
            c[enums::PETAHERTZ] = Mul(Rat(Int(1), Pow(10, 33)), Sym("attohz"));
            c[enums::PICOHERTZ] = Mul(Rat(Int(1), Pow(10, 6)), Sym("attohz"));
            c[enums::TERAHERTZ] = Mul(Rat(Int(1), Pow(10, 30)), Sym("attohz"));
            c[enums::YOCTOHERTZ] = Mul(Pow(10, 6), Sym("attohz"));
            c[enums::YOTTAHERTZ] = Mul(Rat(Int(1), Pow(10, 42)), Sym("attohz"));
            c[enums::ZEPTOHERTZ] = Mul(Int(1000), Sym("attohz"));
            c[enums::ZETTAHERTZ] = Mul(Rat(Int(1), Pow(10, 39)), Sym("attohz"));
            return c;
        }

        static std::map<UnitsFrequency, ConversionPtr> createMapCENTIHERTZ() {
            std::map<UnitsFrequency, ConversionPtr> c;
            c[enums::ATTOHERTZ] = Mul(Pow(10, 16), Sym("centihz"));
            c[enums::DECAHERTZ] = Mul(Rat(Int(1), Int(1000)), Sym("centihz"));
            c[enums::DECIHERTZ] = Mul(Rat(Int(1), Int(10)), Sym("centihz"));
            c[enums::EXAHERTZ] = Mul(Rat(Int(1), Pow(10, 20)), Sym("centihz"));
            c[enums::FEMTOHERTZ] = Mul(Pow(10, 13), Sym("centihz"));
            c[enums::GIGAHERTZ] = Mul(Rat(Int(1), Pow(10, 11)), Sym("centihz"));
            c[enums::HECTOHERTZ] = Mul(Rat(Int(1), Pow(10, 4)), Sym("centihz"));
            c[enums::HERTZ] = Mul(Rat(Int(1), Int(100)), Sym("centihz"));
            c[enums::KILOHERTZ] = Mul(Rat(Int(1), Pow(10, 5)), Sym("centihz"));
            c[enums::MEGAHERTZ] = Mul(Rat(Int(1), Pow(10, 8)), Sym("centihz"));
            c[enums::MICROHERTZ] = Mul(Pow(10, 4), Sym("centihz"));
            c[enums::MILLIHERTZ] = Mul(Int(10), Sym("centihz"));
            c[enums::NANOHERTZ] = Mul(Pow(10, 7), Sym("centihz"));
            c[enums::PETAHERTZ] = Mul(Rat(Int(1), Pow(10, 17)), Sym("centihz"));
            c[enums::PICOHERTZ] = Mul(Pow(10, 10), Sym("centihz"));
            c[enums::TERAHERTZ] = Mul(Rat(Int(1), Pow(10, 14)), Sym("centihz"));
            c[enums::YOCTOHERTZ] = Mul(Pow(10, 22), Sym("centihz"));
            c[enums::YOTTAHERTZ] = Mul(Rat(Int(1), Pow(10, 26)), Sym("centihz"));
            c[enums::ZEPTOHERTZ] = Mul(Pow(10, 19), Sym("centihz"));
            c[enums::ZETTAHERTZ] = Mul(Rat(Int(1), Pow(10, 23)), Sym("centihz"));
            return c;
        }

        static std::map<UnitsFrequency, ConversionPtr> createMapDECAHERTZ() {
            std::map<UnitsFrequency, ConversionPtr> c;
            c[enums::ATTOHERTZ] = Mul(Pow(10, 19), Sym("decahz"));
            c[enums::CENTIHERTZ] = Mul(Int(1000), Sym("decahz"));
            c[enums::DECIHERTZ] = Mul(Int(100), Sym("decahz"));
            c[enums::EXAHERTZ] = Mul(Rat(Int(1), Pow(10, 17)), Sym("decahz"));
            c[enums::FEMTOHERTZ] = Mul(Pow(10, 16), Sym("decahz"));
            c[enums::GIGAHERTZ] = Mul(Rat(Int(1), Pow(10, 8)), Sym("decahz"));
            c[enums::HECTOHERTZ] = Mul(Rat(Int(1), Int(10)), Sym("decahz"));
            c[enums::HERTZ] = Mul(Int(10), Sym("decahz"));
            c[enums::KILOHERTZ] = Mul(Rat(Int(1), Int(100)), Sym("decahz"));
            c[enums::MEGAHERTZ] = Mul(Rat(Int(1), Pow(10, 5)), Sym("decahz"));
            c[enums::MICROHERTZ] = Mul(Pow(10, 7), Sym("decahz"));
            c[enums::MILLIHERTZ] = Mul(Pow(10, 4), Sym("decahz"));
            c[enums::NANOHERTZ] = Mul(Pow(10, 10), Sym("decahz"));
            c[enums::PETAHERTZ] = Mul(Rat(Int(1), Pow(10, 14)), Sym("decahz"));
            c[enums::PICOHERTZ] = Mul(Pow(10, 13), Sym("decahz"));
            c[enums::TERAHERTZ] = Mul(Rat(Int(1), Pow(10, 11)), Sym("decahz"));
            c[enums::YOCTOHERTZ] = Mul(Pow(10, 25), Sym("decahz"));
            c[enums::YOTTAHERTZ] = Mul(Rat(Int(1), Pow(10, 23)), Sym("decahz"));
            c[enums::ZEPTOHERTZ] = Mul(Pow(10, 22), Sym("decahz"));
            c[enums::ZETTAHERTZ] = Mul(Rat(Int(1), Pow(10, 20)), Sym("decahz"));
            return c;
        }

        static std::map<UnitsFrequency, ConversionPtr> createMapDECIHERTZ() {
            std::map<UnitsFrequency, ConversionPtr> c;
            c[enums::ATTOHERTZ] = Mul(Pow(10, 17), Sym("decihz"));
            c[enums::CENTIHERTZ] = Mul(Int(10), Sym("decihz"));
            c[enums::DECAHERTZ] = Mul(Rat(Int(1), Int(100)), Sym("decihz"));
            c[enums::EXAHERTZ] = Mul(Rat(Int(1), Pow(10, 19)), Sym("decihz"));
            c[enums::FEMTOHERTZ] = Mul(Pow(10, 14), Sym("decihz"));
            c[enums::GIGAHERTZ] = Mul(Rat(Int(1), Pow(10, 10)), Sym("decihz"));
            c[enums::HECTOHERTZ] = Mul(Rat(Int(1), Int(1000)), Sym("decihz"));
            c[enums::HERTZ] = Mul(Rat(Int(1), Int(10)), Sym("decihz"));
            c[enums::KILOHERTZ] = Mul(Rat(Int(1), Pow(10, 4)), Sym("decihz"));
            c[enums::MEGAHERTZ] = Mul(Rat(Int(1), Pow(10, 7)), Sym("decihz"));
            c[enums::MICROHERTZ] = Mul(Pow(10, 5), Sym("decihz"));
            c[enums::MILLIHERTZ] = Mul(Int(100), Sym("decihz"));
            c[enums::NANOHERTZ] = Mul(Pow(10, 8), Sym("decihz"));
            c[enums::PETAHERTZ] = Mul(Rat(Int(1), Pow(10, 16)), Sym("decihz"));
            c[enums::PICOHERTZ] = Mul(Pow(10, 11), Sym("decihz"));
            c[enums::TERAHERTZ] = Mul(Rat(Int(1), Pow(10, 13)), Sym("decihz"));
            c[enums::YOCTOHERTZ] = Mul(Pow(10, 23), Sym("decihz"));
            c[enums::YOTTAHERTZ] = Mul(Rat(Int(1), Pow(10, 25)), Sym("decihz"));
            c[enums::ZEPTOHERTZ] = Mul(Pow(10, 20), Sym("decihz"));
            c[enums::ZETTAHERTZ] = Mul(Rat(Int(1), Pow(10, 22)), Sym("decihz"));
            return c;
        }

        static std::map<UnitsFrequency, ConversionPtr> createMapEXAHERTZ() {
            std::map<UnitsFrequency, ConversionPtr> c;
            c[enums::ATTOHERTZ] = Mul(Pow(10, 36), Sym("exahz"));
            c[enums::CENTIHERTZ] = Mul(Pow(10, 20), Sym("exahz"));
            c[enums::DECAHERTZ] = Mul(Pow(10, 17), Sym("exahz"));
            c[enums::DECIHERTZ] = Mul(Pow(10, 19), Sym("exahz"));
            c[enums::FEMTOHERTZ] = Mul(Pow(10, 33), Sym("exahz"));
            c[enums::GIGAHERTZ] = Mul(Pow(10, 9), Sym("exahz"));
            c[enums::HECTOHERTZ] = Mul(Pow(10, 16), Sym("exahz"));
            c[enums::HERTZ] = Mul(Pow(10, 18), Sym("exahz"));
            c[enums::KILOHERTZ] = Mul(Pow(10, 15), Sym("exahz"));
            c[enums::MEGAHERTZ] = Mul(Pow(10, 12), Sym("exahz"));
            c[enums::MICROHERTZ] = Mul(Pow(10, 24), Sym("exahz"));
            c[enums::MILLIHERTZ] = Mul(Pow(10, 21), Sym("exahz"));
            c[enums::NANOHERTZ] = Mul(Pow(10, 27), Sym("exahz"));
            c[enums::PETAHERTZ] = Mul(Int(1000), Sym("exahz"));
            c[enums::PICOHERTZ] = Mul(Pow(10, 30), Sym("exahz"));
            c[enums::TERAHERTZ] = Mul(Pow(10, 6), Sym("exahz"));
            c[enums::YOCTOHERTZ] = Mul(Pow(10, 42), Sym("exahz"));
            c[enums::YOTTAHERTZ] = Mul(Rat(Int(1), Pow(10, 6)), Sym("exahz"));
            c[enums::ZEPTOHERTZ] = Mul(Pow(10, 39), Sym("exahz"));
            c[enums::ZETTAHERTZ] = Mul(Rat(Int(1), Int(1000)), Sym("exahz"));
            return c;
        }

        static std::map<UnitsFrequency, ConversionPtr> createMapFEMTOHERTZ() {
            std::map<UnitsFrequency, ConversionPtr> c;
            c[enums::ATTOHERTZ] = Mul(Int(1000), Sym("femtohz"));
            c[enums::CENTIHERTZ] = Mul(Rat(Int(1), Pow(10, 13)), Sym("femtohz"));
            c[enums::DECAHERTZ] = Mul(Rat(Int(1), Pow(10, 16)), Sym("femtohz"));
            c[enums::DECIHERTZ] = Mul(Rat(Int(1), Pow(10, 14)), Sym("femtohz"));
            c[enums::EXAHERTZ] = Mul(Rat(Int(1), Pow(10, 33)), Sym("femtohz"));
            c[enums::GIGAHERTZ] = Mul(Rat(Int(1), Pow(10, 24)), Sym("femtohz"));
            c[enums::HECTOHERTZ] = Mul(Rat(Int(1), Pow(10, 17)), Sym("femtohz"));
            c[enums::HERTZ] = Mul(Rat(Int(1), Pow(10, 15)), Sym("femtohz"));
            c[enums::KILOHERTZ] = Mul(Rat(Int(1), Pow(10, 18)), Sym("femtohz"));
            c[enums::MEGAHERTZ] = Mul(Rat(Int(1), Pow(10, 21)), Sym("femtohz"));
            c[enums::MICROHERTZ] = Mul(Rat(Int(1), Pow(10, 9)), Sym("femtohz"));
            c[enums::MILLIHERTZ] = Mul(Rat(Int(1), Pow(10, 12)), Sym("femtohz"));
            c[enums::NANOHERTZ] = Mul(Rat(Int(1), Pow(10, 6)), Sym("femtohz"));
            c[enums::PETAHERTZ] = Mul(Rat(Int(1), Pow(10, 30)), Sym("femtohz"));
            c[enums::PICOHERTZ] = Mul(Rat(Int(1), Int(1000)), Sym("femtohz"));
            c[enums::TERAHERTZ] = Mul(Rat(Int(1), Pow(10, 27)), Sym("femtohz"));
            c[enums::YOCTOHERTZ] = Mul(Pow(10, 9), Sym("femtohz"));
            c[enums::YOTTAHERTZ] = Mul(Rat(Int(1), Pow(10, 39)), Sym("femtohz"));
            c[enums::ZEPTOHERTZ] = Mul(Pow(10, 6), Sym("femtohz"));
            c[enums::ZETTAHERTZ] = Mul(Rat(Int(1), Pow(10, 36)), Sym("femtohz"));
            return c;
        }

        static std::map<UnitsFrequency, ConversionPtr> createMapGIGAHERTZ() {
            std::map<UnitsFrequency, ConversionPtr> c;
            c[enums::ATTOHERTZ] = Mul(Pow(10, 27), Sym("gigahz"));
            c[enums::CENTIHERTZ] = Mul(Pow(10, 11), Sym("gigahz"));
            c[enums::DECAHERTZ] = Mul(Pow(10, 8), Sym("gigahz"));
            c[enums::DECIHERTZ] = Mul(Pow(10, 10), Sym("gigahz"));
            c[enums::EXAHERTZ] = Mul(Rat(Int(1), Pow(10, 9)), Sym("gigahz"));
            c[enums::FEMTOHERTZ] = Mul(Pow(10, 24), Sym("gigahz"));
            c[enums::HECTOHERTZ] = Mul(Pow(10, 7), Sym("gigahz"));
            c[enums::HERTZ] = Mul(Pow(10, 9), Sym("gigahz"));
            c[enums::KILOHERTZ] = Mul(Pow(10, 6), Sym("gigahz"));
            c[enums::MEGAHERTZ] = Mul(Int(1000), Sym("gigahz"));
            c[enums::MICROHERTZ] = Mul(Pow(10, 15), Sym("gigahz"));
            c[enums::MILLIHERTZ] = Mul(Pow(10, 12), Sym("gigahz"));
            c[enums::NANOHERTZ] = Mul(Pow(10, 18), Sym("gigahz"));
            c[enums::PETAHERTZ] = Mul(Rat(Int(1), Pow(10, 6)), Sym("gigahz"));
            c[enums::PICOHERTZ] = Mul(Pow(10, 21), Sym("gigahz"));
            c[enums::TERAHERTZ] = Mul(Rat(Int(1), Int(1000)), Sym("gigahz"));
            c[enums::YOCTOHERTZ] = Mul(Pow(10, 33), Sym("gigahz"));
            c[enums::YOTTAHERTZ] = Mul(Rat(Int(1), Pow(10, 15)), Sym("gigahz"));
            c[enums::ZEPTOHERTZ] = Mul(Pow(10, 30), Sym("gigahz"));
            c[enums::ZETTAHERTZ] = Mul(Rat(Int(1), Pow(10, 12)), Sym("gigahz"));
            return c;
        }

        static std::map<UnitsFrequency, ConversionPtr> createMapHECTOHERTZ() {
            std::map<UnitsFrequency, ConversionPtr> c;
            c[enums::ATTOHERTZ] = Mul(Pow(10, 20), Sym("hectohz"));
            c[enums::CENTIHERTZ] = Mul(Pow(10, 4), Sym("hectohz"));
            c[enums::DECAHERTZ] = Mul(Int(10), Sym("hectohz"));
            c[enums::DECIHERTZ] = Mul(Int(1000), Sym("hectohz"));
            c[enums::EXAHERTZ] = Mul(Rat(Int(1), Pow(10, 16)), Sym("hectohz"));
            c[enums::FEMTOHERTZ] = Mul(Pow(10, 17), Sym("hectohz"));
            c[enums::GIGAHERTZ] = Mul(Rat(Int(1), Pow(10, 7)), Sym("hectohz"));
            c[enums::HERTZ] = Mul(Int(100), Sym("hectohz"));
            c[enums::KILOHERTZ] = Mul(Rat(Int(1), Int(10)), Sym("hectohz"));
            c[enums::MEGAHERTZ] = Mul(Rat(Int(1), Pow(10, 4)), Sym("hectohz"));
            c[enums::MICROHERTZ] = Mul(Pow(10, 8), Sym("hectohz"));
            c[enums::MILLIHERTZ] = Mul(Pow(10, 5), Sym("hectohz"));
            c[enums::NANOHERTZ] = Mul(Pow(10, 11), Sym("hectohz"));
            c[enums::PETAHERTZ] = Mul(Rat(Int(1), Pow(10, 13)), Sym("hectohz"));
            c[enums::PICOHERTZ] = Mul(Pow(10, 14), Sym("hectohz"));
            c[enums::TERAHERTZ] = Mul(Rat(Int(1), Pow(10, 10)), Sym("hectohz"));
            c[enums::YOCTOHERTZ] = Mul(Pow(10, 26), Sym("hectohz"));
            c[enums::YOTTAHERTZ] = Mul(Rat(Int(1), Pow(10, 22)), Sym("hectohz"));
            c[enums::ZEPTOHERTZ] = Mul(Pow(10, 23), Sym("hectohz"));
            c[enums::ZETTAHERTZ] = Mul(Rat(Int(1), Pow(10, 19)), Sym("hectohz"));
            return c;
        }

        static std::map<UnitsFrequency, ConversionPtr> createMapHERTZ() {
            std::map<UnitsFrequency, ConversionPtr> c;
            c[enums::ATTOHERTZ] = Mul(Pow(10, 18), Sym("hz"));
            c[enums::CENTIHERTZ] = Mul(Int(100), Sym("hz"));
            c[enums::DECAHERTZ] = Mul(Rat(Int(1), Int(10)), Sym("hz"));
            c[enums::DECIHERTZ] = Mul(Int(10), Sym("hz"));
            c[enums::EXAHERTZ] = Mul(Rat(Int(1), Pow(10, 18)), Sym("hz"));
            c[enums::FEMTOHERTZ] = Mul(Pow(10, 15), Sym("hz"));
            c[enums::GIGAHERTZ] = Mul(Rat(Int(1), Pow(10, 9)), Sym("hz"));
            c[enums::HECTOHERTZ] = Mul(Rat(Int(1), Int(100)), Sym("hz"));
            c[enums::KILOHERTZ] = Mul(Rat(Int(1), Int(1000)), Sym("hz"));
            c[enums::MEGAHERTZ] = Mul(Rat(Int(1), Pow(10, 6)), Sym("hz"));
            c[enums::MICROHERTZ] = Mul(Pow(10, 6), Sym("hz"));
            c[enums::MILLIHERTZ] = Mul(Int(1000), Sym("hz"));
            c[enums::NANOHERTZ] = Mul(Pow(10, 9), Sym("hz"));
            c[enums::PETAHERTZ] = Mul(Rat(Int(1), Pow(10, 15)), Sym("hz"));
            c[enums::PICOHERTZ] = Mul(Pow(10, 12), Sym("hz"));
            c[enums::TERAHERTZ] = Mul(Rat(Int(1), Pow(10, 12)), Sym("hz"));
            c[enums::YOCTOHERTZ] = Mul(Pow(10, 24), Sym("hz"));
            c[enums::YOTTAHERTZ] = Mul(Rat(Int(1), Pow(10, 24)), Sym("hz"));
            c[enums::ZEPTOHERTZ] = Mul(Pow(10, 21), Sym("hz"));
            c[enums::ZETTAHERTZ] = Mul(Rat(Int(1), Pow(10, 21)), Sym("hz"));
            return c;
        }

        static std::map<UnitsFrequency, ConversionPtr> createMapKILOHERTZ() {
            std::map<UnitsFrequency, ConversionPtr> c;
            c[enums::ATTOHERTZ] = Mul(Pow(10, 21), Sym("kilohz"));
            c[enums::CENTIHERTZ] = Mul(Pow(10, 5), Sym("kilohz"));
            c[enums::DECAHERTZ] = Mul(Int(100), Sym("kilohz"));
            c[enums::DECIHERTZ] = Mul(Pow(10, 4), Sym("kilohz"));
            c[enums::EXAHERTZ] = Mul(Rat(Int(1), Pow(10, 15)), Sym("kilohz"));
            c[enums::FEMTOHERTZ] = Mul(Pow(10, 18), Sym("kilohz"));
            c[enums::GIGAHERTZ] = Mul(Rat(Int(1), Pow(10, 6)), Sym("kilohz"));
            c[enums::HECTOHERTZ] = Mul(Int(10), Sym("kilohz"));
            c[enums::HERTZ] = Mul(Int(1000), Sym("kilohz"));
            c[enums::MEGAHERTZ] = Mul(Rat(Int(1), Int(1000)), Sym("kilohz"));
            c[enums::MICROHERTZ] = Mul(Pow(10, 9), Sym("kilohz"));
            c[enums::MILLIHERTZ] = Mul(Pow(10, 6), Sym("kilohz"));
            c[enums::NANOHERTZ] = Mul(Pow(10, 12), Sym("kilohz"));
            c[enums::PETAHERTZ] = Mul(Rat(Int(1), Pow(10, 12)), Sym("kilohz"));
            c[enums::PICOHERTZ] = Mul(Pow(10, 15), Sym("kilohz"));
            c[enums::TERAHERTZ] = Mul(Rat(Int(1), Pow(10, 9)), Sym("kilohz"));
            c[enums::YOCTOHERTZ] = Mul(Pow(10, 27), Sym("kilohz"));
            c[enums::YOTTAHERTZ] = Mul(Rat(Int(1), Pow(10, 21)), Sym("kilohz"));
            c[enums::ZEPTOHERTZ] = Mul(Pow(10, 24), Sym("kilohz"));
            c[enums::ZETTAHERTZ] = Mul(Rat(Int(1), Pow(10, 18)), Sym("kilohz"));
            return c;
        }

        static std::map<UnitsFrequency, ConversionPtr> createMapMEGAHERTZ() {
            std::map<UnitsFrequency, ConversionPtr> c;
            c[enums::ATTOHERTZ] = Mul(Pow(10, 24), Sym("megahz"));
            c[enums::CENTIHERTZ] = Mul(Pow(10, 8), Sym("megahz"));
            c[enums::DECAHERTZ] = Mul(Pow(10, 5), Sym("megahz"));
            c[enums::DECIHERTZ] = Mul(Pow(10, 7), Sym("megahz"));
            c[enums::EXAHERTZ] = Mul(Rat(Int(1), Pow(10, 12)), Sym("megahz"));
            c[enums::FEMTOHERTZ] = Mul(Pow(10, 21), Sym("megahz"));
            c[enums::GIGAHERTZ] = Mul(Rat(Int(1), Int(1000)), Sym("megahz"));
            c[enums::HECTOHERTZ] = Mul(Pow(10, 4), Sym("megahz"));
            c[enums::HERTZ] = Mul(Pow(10, 6), Sym("megahz"));
            c[enums::KILOHERTZ] = Mul(Int(1000), Sym("megahz"));
            c[enums::MICROHERTZ] = Mul(Pow(10, 12), Sym("megahz"));
            c[enums::MILLIHERTZ] = Mul(Pow(10, 9), Sym("megahz"));
            c[enums::NANOHERTZ] = Mul(Pow(10, 15), Sym("megahz"));
            c[enums::PETAHERTZ] = Mul(Rat(Int(1), Pow(10, 9)), Sym("megahz"));
            c[enums::PICOHERTZ] = Mul(Pow(10, 18), Sym("megahz"));
            c[enums::TERAHERTZ] = Mul(Rat(Int(1), Pow(10, 6)), Sym("megahz"));
            c[enums::YOCTOHERTZ] = Mul(Pow(10, 30), Sym("megahz"));
            c[enums::YOTTAHERTZ] = Mul(Rat(Int(1), Pow(10, 18)), Sym("megahz"));
            c[enums::ZEPTOHERTZ] = Mul(Pow(10, 27), Sym("megahz"));
            c[enums::ZETTAHERTZ] = Mul(Rat(Int(1), Pow(10, 15)), Sym("megahz"));
            return c;
        }

        static std::map<UnitsFrequency, ConversionPtr> createMapMICROHERTZ() {
            std::map<UnitsFrequency, ConversionPtr> c;
            c[enums::ATTOHERTZ] = Mul(Pow(10, 12), Sym("microhz"));
            c[enums::CENTIHERTZ] = Mul(Rat(Int(1), Pow(10, 4)), Sym("microhz"));
            c[enums::DECAHERTZ] = Mul(Rat(Int(1), Pow(10, 7)), Sym("microhz"));
            c[enums::DECIHERTZ] = Mul(Rat(Int(1), Pow(10, 5)), Sym("microhz"));
            c[enums::EXAHERTZ] = Mul(Rat(Int(1), Pow(10, 24)), Sym("microhz"));
            c[enums::FEMTOHERTZ] = Mul(Pow(10, 9), Sym("microhz"));
            c[enums::GIGAHERTZ] = Mul(Rat(Int(1), Pow(10, 15)), Sym("microhz"));
            c[enums::HECTOHERTZ] = Mul(Rat(Int(1), Pow(10, 8)), Sym("microhz"));
            c[enums::HERTZ] = Mul(Rat(Int(1), Pow(10, 6)), Sym("microhz"));
            c[enums::KILOHERTZ] = Mul(Rat(Int(1), Pow(10, 9)), Sym("microhz"));
            c[enums::MEGAHERTZ] = Mul(Rat(Int(1), Pow(10, 12)), Sym("microhz"));
            c[enums::MILLIHERTZ] = Mul(Rat(Int(1), Int(1000)), Sym("microhz"));
            c[enums::NANOHERTZ] = Mul(Int(1000), Sym("microhz"));
            c[enums::PETAHERTZ] = Mul(Rat(Int(1), Pow(10, 21)), Sym("microhz"));
            c[enums::PICOHERTZ] = Mul(Pow(10, 6), Sym("microhz"));
            c[enums::TERAHERTZ] = Mul(Rat(Int(1), Pow(10, 18)), Sym("microhz"));
            c[enums::YOCTOHERTZ] = Mul(Pow(10, 18), Sym("microhz"));
            c[enums::YOTTAHERTZ] = Mul(Rat(Int(1), Pow(10, 30)), Sym("microhz"));
            c[enums::ZEPTOHERTZ] = Mul(Pow(10, 15), Sym("microhz"));
            c[enums::ZETTAHERTZ] = Mul(Rat(Int(1), Pow(10, 27)), Sym("microhz"));
            return c;
        }

        static std::map<UnitsFrequency, ConversionPtr> createMapMILLIHERTZ() {
            std::map<UnitsFrequency, ConversionPtr> c;
            c[enums::ATTOHERTZ] = Mul(Pow(10, 15), Sym("millihz"));
            c[enums::CENTIHERTZ] = Mul(Rat(Int(1), Int(10)), Sym("millihz"));
            c[enums::DECAHERTZ] = Mul(Rat(Int(1), Pow(10, 4)), Sym("millihz"));
            c[enums::DECIHERTZ] = Mul(Rat(Int(1), Int(100)), Sym("millihz"));
            c[enums::EXAHERTZ] = Mul(Rat(Int(1), Pow(10, 21)), Sym("millihz"));
            c[enums::FEMTOHERTZ] = Mul(Pow(10, 12), Sym("millihz"));
            c[enums::GIGAHERTZ] = Mul(Rat(Int(1), Pow(10, 12)), Sym("millihz"));
            c[enums::HECTOHERTZ] = Mul(Rat(Int(1), Pow(10, 5)), Sym("millihz"));
            c[enums::HERTZ] = Mul(Rat(Int(1), Int(1000)), Sym("millihz"));
            c[enums::KILOHERTZ] = Mul(Rat(Int(1), Pow(10, 6)), Sym("millihz"));
            c[enums::MEGAHERTZ] = Mul(Rat(Int(1), Pow(10, 9)), Sym("millihz"));
            c[enums::MICROHERTZ] = Mul(Int(1000), Sym("millihz"));
            c[enums::NANOHERTZ] = Mul(Pow(10, 6), Sym("millihz"));
            c[enums::PETAHERTZ] = Mul(Rat(Int(1), Pow(10, 18)), Sym("millihz"));
            c[enums::PICOHERTZ] = Mul(Pow(10, 9), Sym("millihz"));
            c[enums::TERAHERTZ] = Mul(Rat(Int(1), Pow(10, 15)), Sym("millihz"));
            c[enums::YOCTOHERTZ] = Mul(Pow(10, 21), Sym("millihz"));
            c[enums::YOTTAHERTZ] = Mul(Rat(Int(1), Pow(10, 27)), Sym("millihz"));
            c[enums::ZEPTOHERTZ] = Mul(Pow(10, 18), Sym("millihz"));
            c[enums::ZETTAHERTZ] = Mul(Rat(Int(1), Pow(10, 24)), Sym("millihz"));
            return c;
        }

        static std::map<UnitsFrequency, ConversionPtr> createMapNANOHERTZ() {
            std::map<UnitsFrequency, ConversionPtr> c;
            c[enums::ATTOHERTZ] = Mul(Pow(10, 9), Sym("nanohz"));
            c[enums::CENTIHERTZ] = Mul(Rat(Int(1), Pow(10, 7)), Sym("nanohz"));
            c[enums::DECAHERTZ] = Mul(Rat(Int(1), Pow(10, 10)), Sym("nanohz"));
            c[enums::DECIHERTZ] = Mul(Rat(Int(1), Pow(10, 8)), Sym("nanohz"));
            c[enums::EXAHERTZ] = Mul(Rat(Int(1), Pow(10, 27)), Sym("nanohz"));
            c[enums::FEMTOHERTZ] = Mul(Pow(10, 6), Sym("nanohz"));
            c[enums::GIGAHERTZ] = Mul(Rat(Int(1), Pow(10, 18)), Sym("nanohz"));
            c[enums::HECTOHERTZ] = Mul(Rat(Int(1), Pow(10, 11)), Sym("nanohz"));
            c[enums::HERTZ] = Mul(Rat(Int(1), Pow(10, 9)), Sym("nanohz"));
            c[enums::KILOHERTZ] = Mul(Rat(Int(1), Pow(10, 12)), Sym("nanohz"));
            c[enums::MEGAHERTZ] = Mul(Rat(Int(1), Pow(10, 15)), Sym("nanohz"));
            c[enums::MICROHERTZ] = Mul(Rat(Int(1), Int(1000)), Sym("nanohz"));
            c[enums::MILLIHERTZ] = Mul(Rat(Int(1), Pow(10, 6)), Sym("nanohz"));
            c[enums::PETAHERTZ] = Mul(Rat(Int(1), Pow(10, 24)), Sym("nanohz"));
            c[enums::PICOHERTZ] = Mul(Int(1000), Sym("nanohz"));
            c[enums::TERAHERTZ] = Mul(Rat(Int(1), Pow(10, 21)), Sym("nanohz"));
            c[enums::YOCTOHERTZ] = Mul(Pow(10, 15), Sym("nanohz"));
            c[enums::YOTTAHERTZ] = Mul(Rat(Int(1), Pow(10, 33)), Sym("nanohz"));
            c[enums::ZEPTOHERTZ] = Mul(Pow(10, 12), Sym("nanohz"));
            c[enums::ZETTAHERTZ] = Mul(Rat(Int(1), Pow(10, 30)), Sym("nanohz"));
            return c;
        }

        static std::map<UnitsFrequency, ConversionPtr> createMapPETAHERTZ() {
            std::map<UnitsFrequency, ConversionPtr> c;
            c[enums::ATTOHERTZ] = Mul(Pow(10, 33), Sym("petahz"));
            c[enums::CENTIHERTZ] = Mul(Pow(10, 17), Sym("petahz"));
            c[enums::DECAHERTZ] = Mul(Pow(10, 14), Sym("petahz"));
            c[enums::DECIHERTZ] = Mul(Pow(10, 16), Sym("petahz"));
            c[enums::EXAHERTZ] = Mul(Rat(Int(1), Int(1000)), Sym("petahz"));
            c[enums::FEMTOHERTZ] = Mul(Pow(10, 30), Sym("petahz"));
            c[enums::GIGAHERTZ] = Mul(Pow(10, 6), Sym("petahz"));
            c[enums::HECTOHERTZ] = Mul(Pow(10, 13), Sym("petahz"));
            c[enums::HERTZ] = Mul(Pow(10, 15), Sym("petahz"));
            c[enums::KILOHERTZ] = Mul(Pow(10, 12), Sym("petahz"));
            c[enums::MEGAHERTZ] = Mul(Pow(10, 9), Sym("petahz"));
            c[enums::MICROHERTZ] = Mul(Pow(10, 21), Sym("petahz"));
            c[enums::MILLIHERTZ] = Mul(Pow(10, 18), Sym("petahz"));
            c[enums::NANOHERTZ] = Mul(Pow(10, 24), Sym("petahz"));
            c[enums::PICOHERTZ] = Mul(Pow(10, 27), Sym("petahz"));
            c[enums::TERAHERTZ] = Mul(Int(1000), Sym("petahz"));
            c[enums::YOCTOHERTZ] = Mul(Pow(10, 39), Sym("petahz"));
            c[enums::YOTTAHERTZ] = Mul(Rat(Int(1), Pow(10, 9)), Sym("petahz"));
            c[enums::ZEPTOHERTZ] = Mul(Pow(10, 36), Sym("petahz"));
            c[enums::ZETTAHERTZ] = Mul(Rat(Int(1), Pow(10, 6)), Sym("petahz"));
            return c;
        }

        static std::map<UnitsFrequency, ConversionPtr> createMapPICOHERTZ() {
            std::map<UnitsFrequency, ConversionPtr> c;
            c[enums::ATTOHERTZ] = Mul(Pow(10, 6), Sym("picohz"));
            c[enums::CENTIHERTZ] = Mul(Rat(Int(1), Pow(10, 10)), Sym("picohz"));
            c[enums::DECAHERTZ] = Mul(Rat(Int(1), Pow(10, 13)), Sym("picohz"));
            c[enums::DECIHERTZ] = Mul(Rat(Int(1), Pow(10, 11)), Sym("picohz"));
            c[enums::EXAHERTZ] = Mul(Rat(Int(1), Pow(10, 30)), Sym("picohz"));
            c[enums::FEMTOHERTZ] = Mul(Int(1000), Sym("picohz"));
            c[enums::GIGAHERTZ] = Mul(Rat(Int(1), Pow(10, 21)), Sym("picohz"));
            c[enums::HECTOHERTZ] = Mul(Rat(Int(1), Pow(10, 14)), Sym("picohz"));
            c[enums::HERTZ] = Mul(Rat(Int(1), Pow(10, 12)), Sym("picohz"));
            c[enums::KILOHERTZ] = Mul(Rat(Int(1), Pow(10, 15)), Sym("picohz"));
            c[enums::MEGAHERTZ] = Mul(Rat(Int(1), Pow(10, 18)), Sym("picohz"));
            c[enums::MICROHERTZ] = Mul(Rat(Int(1), Pow(10, 6)), Sym("picohz"));
            c[enums::MILLIHERTZ] = Mul(Rat(Int(1), Pow(10, 9)), Sym("picohz"));
            c[enums::NANOHERTZ] = Mul(Rat(Int(1), Int(1000)), Sym("picohz"));
            c[enums::PETAHERTZ] = Mul(Rat(Int(1), Pow(10, 27)), Sym("picohz"));
            c[enums::TERAHERTZ] = Mul(Rat(Int(1), Pow(10, 24)), Sym("picohz"));
            c[enums::YOCTOHERTZ] = Mul(Pow(10, 12), Sym("picohz"));
            c[enums::YOTTAHERTZ] = Mul(Rat(Int(1), Pow(10, 36)), Sym("picohz"));
            c[enums::ZEPTOHERTZ] = Mul(Pow(10, 9), Sym("picohz"));
            c[enums::ZETTAHERTZ] = Mul(Rat(Int(1), Pow(10, 33)), Sym("picohz"));
            return c;
        }

        static std::map<UnitsFrequency, ConversionPtr> createMapTERAHERTZ() {
            std::map<UnitsFrequency, ConversionPtr> c;
            c[enums::ATTOHERTZ] = Mul(Pow(10, 30), Sym("terahz"));
            c[enums::CENTIHERTZ] = Mul(Pow(10, 14), Sym("terahz"));
            c[enums::DECAHERTZ] = Mul(Pow(10, 11), Sym("terahz"));
            c[enums::DECIHERTZ] = Mul(Pow(10, 13), Sym("terahz"));
            c[enums::EXAHERTZ] = Mul(Rat(Int(1), Pow(10, 6)), Sym("terahz"));
            c[enums::FEMTOHERTZ] = Mul(Pow(10, 27), Sym("terahz"));
            c[enums::GIGAHERTZ] = Mul(Int(1000), Sym("terahz"));
            c[enums::HECTOHERTZ] = Mul(Pow(10, 10), Sym("terahz"));
            c[enums::HERTZ] = Mul(Pow(10, 12), Sym("terahz"));
            c[enums::KILOHERTZ] = Mul(Pow(10, 9), Sym("terahz"));
            c[enums::MEGAHERTZ] = Mul(Pow(10, 6), Sym("terahz"));
            c[enums::MICROHERTZ] = Mul(Pow(10, 18), Sym("terahz"));
            c[enums::MILLIHERTZ] = Mul(Pow(10, 15), Sym("terahz"));
            c[enums::NANOHERTZ] = Mul(Pow(10, 21), Sym("terahz"));
            c[enums::PETAHERTZ] = Mul(Rat(Int(1), Int(1000)), Sym("terahz"));
            c[enums::PICOHERTZ] = Mul(Pow(10, 24), Sym("terahz"));
            c[enums::YOCTOHERTZ] = Mul(Pow(10, 36), Sym("terahz"));
            c[enums::YOTTAHERTZ] = Mul(Rat(Int(1), Pow(10, 12)), Sym("terahz"));
            c[enums::ZEPTOHERTZ] = Mul(Pow(10, 33), Sym("terahz"));
            c[enums::ZETTAHERTZ] = Mul(Rat(Int(1), Pow(10, 9)), Sym("terahz"));
            return c;
        }

        static std::map<UnitsFrequency, ConversionPtr> createMapYOCTOHERTZ() {
            std::map<UnitsFrequency, ConversionPtr> c;
            c[enums::ATTOHERTZ] = Mul(Rat(Int(1), Pow(10, 6)), Sym("yoctohz"));
            c[enums::CENTIHERTZ] = Mul(Rat(Int(1), Pow(10, 22)), Sym("yoctohz"));
            c[enums::DECAHERTZ] = Mul(Rat(Int(1), Pow(10, 25)), Sym("yoctohz"));
            c[enums::DECIHERTZ] = Mul(Rat(Int(1), Pow(10, 23)), Sym("yoctohz"));
            c[enums::EXAHERTZ] = Mul(Rat(Int(1), Pow(10, 42)), Sym("yoctohz"));
            c[enums::FEMTOHERTZ] = Mul(Rat(Int(1), Pow(10, 9)), Sym("yoctohz"));
            c[enums::GIGAHERTZ] = Mul(Rat(Int(1), Pow(10, 33)), Sym("yoctohz"));
            c[enums::HECTOHERTZ] = Mul(Rat(Int(1), Pow(10, 26)), Sym("yoctohz"));
            c[enums::HERTZ] = Mul(Rat(Int(1), Pow(10, 24)), Sym("yoctohz"));
            c[enums::KILOHERTZ] = Mul(Rat(Int(1), Pow(10, 27)), Sym("yoctohz"));
            c[enums::MEGAHERTZ] = Mul(Rat(Int(1), Pow(10, 30)), Sym("yoctohz"));
            c[enums::MICROHERTZ] = Mul(Rat(Int(1), Pow(10, 18)), Sym("yoctohz"));
            c[enums::MILLIHERTZ] = Mul(Rat(Int(1), Pow(10, 21)), Sym("yoctohz"));
            c[enums::NANOHERTZ] = Mul(Rat(Int(1), Pow(10, 15)), Sym("yoctohz"));
            c[enums::PETAHERTZ] = Mul(Rat(Int(1), Pow(10, 39)), Sym("yoctohz"));
            c[enums::PICOHERTZ] = Mul(Rat(Int(1), Pow(10, 12)), Sym("yoctohz"));
            c[enums::TERAHERTZ] = Mul(Rat(Int(1), Pow(10, 36)), Sym("yoctohz"));
            c[enums::YOTTAHERTZ] = Mul(Rat(Int(1), Pow(10, 48)), Sym("yoctohz"));
            c[enums::ZEPTOHERTZ] = Mul(Rat(Int(1), Int(1000)), Sym("yoctohz"));
            c[enums::ZETTAHERTZ] = Mul(Rat(Int(1), Pow(10, 45)), Sym("yoctohz"));
            return c;
        }

        static std::map<UnitsFrequency, ConversionPtr> createMapYOTTAHERTZ() {
            std::map<UnitsFrequency, ConversionPtr> c;
            c[enums::ATTOHERTZ] = Mul(Pow(10, 42), Sym("yottahz"));
            c[enums::CENTIHERTZ] = Mul(Pow(10, 26), Sym("yottahz"));
            c[enums::DECAHERTZ] = Mul(Pow(10, 23), Sym("yottahz"));
            c[enums::DECIHERTZ] = Mul(Pow(10, 25), Sym("yottahz"));
            c[enums::EXAHERTZ] = Mul(Pow(10, 6), Sym("yottahz"));
            c[enums::FEMTOHERTZ] = Mul(Pow(10, 39), Sym("yottahz"));
            c[enums::GIGAHERTZ] = Mul(Pow(10, 15), Sym("yottahz"));
            c[enums::HECTOHERTZ] = Mul(Pow(10, 22), Sym("yottahz"));
            c[enums::HERTZ] = Mul(Pow(10, 24), Sym("yottahz"));
            c[enums::KILOHERTZ] = Mul(Pow(10, 21), Sym("yottahz"));
            c[enums::MEGAHERTZ] = Mul(Pow(10, 18), Sym("yottahz"));
            c[enums::MICROHERTZ] = Mul(Pow(10, 30), Sym("yottahz"));
            c[enums::MILLIHERTZ] = Mul(Pow(10, 27), Sym("yottahz"));
            c[enums::NANOHERTZ] = Mul(Pow(10, 33), Sym("yottahz"));
            c[enums::PETAHERTZ] = Mul(Pow(10, 9), Sym("yottahz"));
            c[enums::PICOHERTZ] = Mul(Pow(10, 36), Sym("yottahz"));
            c[enums::TERAHERTZ] = Mul(Pow(10, 12), Sym("yottahz"));
            c[enums::YOCTOHERTZ] = Mul(Pow(10, 48), Sym("yottahz"));
            c[enums::ZEPTOHERTZ] = Mul(Pow(10, 45), Sym("yottahz"));
            c[enums::ZETTAHERTZ] = Mul(Int(1000), Sym("yottahz"));
            return c;
        }

        static std::map<UnitsFrequency, ConversionPtr> createMapZEPTOHERTZ() {
            std::map<UnitsFrequency, ConversionPtr> c;
            c[enums::ATTOHERTZ] = Mul(Rat(Int(1), Int(1000)), Sym("zeptohz"));
            c[enums::CENTIHERTZ] = Mul(Rat(Int(1), Pow(10, 19)), Sym("zeptohz"));
            c[enums::DECAHERTZ] = Mul(Rat(Int(1), Pow(10, 22)), Sym("zeptohz"));
            c[enums::DECIHERTZ] = Mul(Rat(Int(1), Pow(10, 20)), Sym("zeptohz"));
            c[enums::EXAHERTZ] = Mul(Rat(Int(1), Pow(10, 39)), Sym("zeptohz"));
            c[enums::FEMTOHERTZ] = Mul(Rat(Int(1), Pow(10, 6)), Sym("zeptohz"));
            c[enums::GIGAHERTZ] = Mul(Rat(Int(1), Pow(10, 30)), Sym("zeptohz"));
            c[enums::HECTOHERTZ] = Mul(Rat(Int(1), Pow(10, 23)), Sym("zeptohz"));
            c[enums::HERTZ] = Mul(Rat(Int(1), Pow(10, 21)), Sym("zeptohz"));
            c[enums::KILOHERTZ] = Mul(Rat(Int(1), Pow(10, 24)), Sym("zeptohz"));
            c[enums::MEGAHERTZ] = Mul(Rat(Int(1), Pow(10, 27)), Sym("zeptohz"));
            c[enums::MICROHERTZ] = Mul(Rat(Int(1), Pow(10, 15)), Sym("zeptohz"));
            c[enums::MILLIHERTZ] = Mul(Rat(Int(1), Pow(10, 18)), Sym("zeptohz"));
            c[enums::NANOHERTZ] = Mul(Rat(Int(1), Pow(10, 12)), Sym("zeptohz"));
            c[enums::PETAHERTZ] = Mul(Rat(Int(1), Pow(10, 36)), Sym("zeptohz"));
            c[enums::PICOHERTZ] = Mul(Rat(Int(1), Pow(10, 9)), Sym("zeptohz"));
            c[enums::TERAHERTZ] = Mul(Rat(Int(1), Pow(10, 33)), Sym("zeptohz"));
            c[enums::YOCTOHERTZ] = Mul(Int(1000), Sym("zeptohz"));
            c[enums::YOTTAHERTZ] = Mul(Rat(Int(1), Pow(10, 45)), Sym("zeptohz"));
            c[enums::ZETTAHERTZ] = Mul(Rat(Int(1), Pow(10, 42)), Sym("zeptohz"));
            return c;
        }

        static std::map<UnitsFrequency, ConversionPtr> createMapZETTAHERTZ() {
            std::map<UnitsFrequency, ConversionPtr> c;
            c[enums::ATTOHERTZ] = Mul(Pow(10, 39), Sym("zettahz"));
            c[enums::CENTIHERTZ] = Mul(Pow(10, 23), Sym("zettahz"));
            c[enums::DECAHERTZ] = Mul(Pow(10, 20), Sym("zettahz"));
            c[enums::DECIHERTZ] = Mul(Pow(10, 22), Sym("zettahz"));
            c[enums::EXAHERTZ] = Mul(Int(1000), Sym("zettahz"));
            c[enums::FEMTOHERTZ] = Mul(Pow(10, 36), Sym("zettahz"));
            c[enums::GIGAHERTZ] = Mul(Pow(10, 12), Sym("zettahz"));
            c[enums::HECTOHERTZ] = Mul(Pow(10, 19), Sym("zettahz"));
            c[enums::HERTZ] = Mul(Pow(10, 21), Sym("zettahz"));
            c[enums::KILOHERTZ] = Mul(Pow(10, 18), Sym("zettahz"));
            c[enums::MEGAHERTZ] = Mul(Pow(10, 15), Sym("zettahz"));
            c[enums::MICROHERTZ] = Mul(Pow(10, 27), Sym("zettahz"));
            c[enums::MILLIHERTZ] = Mul(Pow(10, 24), Sym("zettahz"));
            c[enums::NANOHERTZ] = Mul(Pow(10, 30), Sym("zettahz"));
            c[enums::PETAHERTZ] = Mul(Pow(10, 6), Sym("zettahz"));
            c[enums::PICOHERTZ] = Mul(Pow(10, 33), Sym("zettahz"));
            c[enums::TERAHERTZ] = Mul(Pow(10, 9), Sym("zettahz"));
            c[enums::YOCTOHERTZ] = Mul(Pow(10, 45), Sym("zettahz"));
            c[enums::YOTTAHERTZ] = Mul(Rat(Int(1), Int(1000)), Sym("zettahz"));
            c[enums::ZEPTOHERTZ] = Mul(Pow(10, 42), Sym("zettahz"));
            return c;
        }

        static std::map<UnitsFrequency,
            std::map<UnitsFrequency, ConversionPtr> > makeConversions() {
            std::map<UnitsFrequency, std::map<UnitsFrequency, ConversionPtr> > c;
            c[enums::ATTOHERTZ] = createMapATTOHERTZ();
            c[enums::CENTIHERTZ] = createMapCENTIHERTZ();
            c[enums::DECAHERTZ] = createMapDECAHERTZ();
            c[enums::DECIHERTZ] = createMapDECIHERTZ();
            c[enums::EXAHERTZ] = createMapEXAHERTZ();
            c[enums::FEMTOHERTZ] = createMapFEMTOHERTZ();
            c[enums::GIGAHERTZ] = createMapGIGAHERTZ();
            c[enums::HECTOHERTZ] = createMapHECTOHERTZ();
            c[enums::HERTZ] = createMapHERTZ();
            c[enums::KILOHERTZ] = createMapKILOHERTZ();
            c[enums::MEGAHERTZ] = createMapMEGAHERTZ();
            c[enums::MICROHERTZ] = createMapMICROHERTZ();
            c[enums::MILLIHERTZ] = createMapMILLIHERTZ();
            c[enums::NANOHERTZ] = createMapNANOHERTZ();
            c[enums::PETAHERTZ] = createMapPETAHERTZ();
            c[enums::PICOHERTZ] = createMapPICOHERTZ();
            c[enums::TERAHERTZ] = createMapTERAHERTZ();
            c[enums::YOCTOHERTZ] = createMapYOCTOHERTZ();
            c[enums::YOTTAHERTZ] = createMapYOTTAHERTZ();
            c[enums::ZEPTOHERTZ] = createMapZEPTOHERTZ();
            c[enums::ZETTAHERTZ] = createMapZETTAHERTZ();
            return c;
        }

        static std::map<UnitsFrequency, std::string> makeSymbols(){
            std::map<UnitsFrequency, std::string> s;
            s[enums::ATTOHERTZ] = "aHz";
            s[enums::CENTIHERTZ] = "cHz";
            s[enums::DECAHERTZ] = "daHz";
            s[enums::DECIHERTZ] = "dHz";
            s[enums::EXAHERTZ] = "EHz";
            s[enums::FEMTOHERTZ] = "fHz";
            s[enums::GIGAHERTZ] = "GHz";
            s[enums::HECTOHERTZ] = "hHz";
            s[enums::HERTZ] = "Hz";
            s[enums::KILOHERTZ] = "kHz";
            s[enums::MEGAHERTZ] = "MHz";
            s[enums::MICROHERTZ] = "µHz";
            s[enums::MILLIHERTZ] = "mHz";
            s[enums::NANOHERTZ] = "nHz";
            s[enums::PETAHERTZ] = "PHz";
            s[enums::PICOHERTZ] = "pHz";
            s[enums::TERAHERTZ] = "THz";
            s[enums::YOCTOHERTZ] = "yHz";
            s[enums::YOTTAHERTZ] = "YHz";
            s[enums::ZEPTOHERTZ] = "zHz";
            s[enums::ZETTAHERTZ] = "ZHz";
            return s;
        }

        std::map<UnitsFrequency,
            std::map<UnitsFrequency, ConversionPtr> > FrequencyI::CONVERSIONS = makeConversions();

        std::map<UnitsFrequency, std::string> FrequencyI::SYMBOLS = makeSymbols();

        FrequencyI::~FrequencyI() {}

        FrequencyI::FrequencyI() : Frequency() {
        }

        FrequencyI::FrequencyI(const double& value, const UnitsFrequency& unit) : Frequency() {
            setValue(value);
            setUnit(unit);
        }

        FrequencyI::FrequencyI(const FrequencyPtr& value, const UnitsFrequency& target) : Frequency() {
            double orig = value->getValue();
            UnitsFrequency source = value->getUnit();
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

        Ice::Double FrequencyI::getValue(const Ice::Current& /* current */) {
            return value;
        }

        void FrequencyI::setValue(Ice::Double _value, const Ice::Current& /* current */) {
            value = _value;
        }

        UnitsFrequency FrequencyI::getUnit(const Ice::Current& /* current */) {
            return unit;
        }

        void FrequencyI::setUnit(UnitsFrequency _unit, const Ice::Current& /* current */) {
            unit = _unit;
        }

        std::string FrequencyI::getSymbol(const Ice::Current& /* current */) {
            return SYMBOLS[unit];
        }

        FrequencyPtr FrequencyI::copy(const Ice::Current& /* current */) {
            FrequencyPtr copy = new FrequencyI();
            copy->setValue(getValue());
            copy->setUnit(getUnit());
            return copy;
        }
    }
}

