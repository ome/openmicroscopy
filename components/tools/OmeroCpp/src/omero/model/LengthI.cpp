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

::Ice::Object* IceInternal::upCast(::omero::model::LengthI* t) { return t; }

namespace omero {

    namespace model {


        static std::map<omero::model::enums::UnitsLength, std::string> makeSymbols(){
            std::map<omero::model::enums::UnitsLength, std::string> s;
            s[omero::model::enums::ANGSTROM] = "Å";
            s[omero::model::enums::ASTRONOMICALUNIT] = "ua";
            s[omero::model::enums::ATTOMETER] = "am";
            s[omero::model::enums::CENTIMETER] = "cm";
            s[omero::model::enums::DECAMETER] = "dam";
            s[omero::model::enums::DECIMETER] = "dm";
            s[omero::model::enums::EXAMETER] = "Em";
            s[omero::model::enums::FEMTOMETER] = "fm";
            s[omero::model::enums::FOOT] = "ft";
            s[omero::model::enums::GIGAMETER] = "Gm";
            s[omero::model::enums::HECTOMETER] = "hm";
            s[omero::model::enums::INCH] = "in";
            s[omero::model::enums::KILOMETER] = "km";
            s[omero::model::enums::LIGHTYEAR] = "ly";
            s[omero::model::enums::LINE] = "li";
            s[omero::model::enums::MEGAMETER] = "Mm";
            s[omero::model::enums::METER] = "m";
            s[omero::model::enums::MICROMETER] = "µm";
            s[omero::model::enums::MILE] = "mi";
            s[omero::model::enums::MILLIMETER] = "mm";
            s[omero::model::enums::NANOMETER] = "nm";
            s[omero::model::enums::PARSEC] = "pc";
            s[omero::model::enums::PETAMETER] = "Pm";
            s[omero::model::enums::PICOMETER] = "pm";
            s[omero::model::enums::PIXEL] = "pixel";
            s[omero::model::enums::POINT] = "pt";
            s[omero::model::enums::REFERENCEFRAME] = "reference frame";
            s[omero::model::enums::TERAMETER] = "Tm";
            s[omero::model::enums::THOU] = "thou";
            s[omero::model::enums::YARD] = "yd";
            s[omero::model::enums::YOCTOMETER] = "ym";
            s[omero::model::enums::YOTTAMETER] = "Ym";
            s[omero::model::enums::ZEPTOMETER] = "zm";
            s[omero::model::enums::ZETTAMETER] = "Zm";
            return s;
        };

        std::map<omero::model::enums::UnitsLength, std::string> LengthI::SYMBOLS = makeSymbols();

        LengthI::~LengthI() {}

        LengthI::LengthI() : Length() {
        }

        Ice::Double LengthI::getValue(const Ice::Current& /* current */) {
            return value;
        }

        void LengthI::setValue(Ice::Double _value, const Ice::Current& /* current */) {
            value = _value;
        }

        omero::model::enums::UnitsLength LengthI::getUnit(const Ice::Current& /* current */) {
            return unit;
        }

        void LengthI::setUnit(omero::model::enums::UnitsLength _unit, const Ice::Current& /* current */) {
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

