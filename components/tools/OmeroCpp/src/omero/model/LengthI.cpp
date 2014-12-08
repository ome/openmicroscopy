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
            s[omero::model::enums::AM] = "am";
            s[omero::model::enums::ANGSTROM] = "Å";
            s[omero::model::enums::CM] = "cm";
            s[omero::model::enums::DAM] = "dam";
            s[omero::model::enums::DM] = "dm";
            s[omero::model::enums::EXAM] = "Em";
            s[omero::model::enums::FM] = "fm";
            s[omero::model::enums::FOOT] = "ft";
            s[omero::model::enums::GIGAM] = "Gm";
            s[omero::model::enums::HM] = "hm";
            s[omero::model::enums::INCH] = "in";
            s[omero::model::enums::KM] = "km";
            s[omero::model::enums::LINE] = "li";
            s[omero::model::enums::LY] = "ly";
            s[omero::model::enums::M] = "m";
            s[omero::model::enums::MEGAM] = "Mm";
            s[omero::model::enums::MICROM] = "µm";
            s[omero::model::enums::MILE] = "mi";
            s[omero::model::enums::MM] = "mm";
            s[omero::model::enums::NM] = "nm";
            s[omero::model::enums::PC] = "pc";
            s[omero::model::enums::PETAM] = "Pm";
            s[omero::model::enums::PIXEL] = "pixel";
            s[omero::model::enums::PM] = "pm";
            s[omero::model::enums::POINT] = "pt";
            s[omero::model::enums::REFERENCEFRAME] = "reference frame";
            s[omero::model::enums::TERAM] = "Tm";
            s[omero::model::enums::THOU] = "thou";
            s[omero::model::enums::UA] = "ua";
            s[omero::model::enums::YARD] = "yd";
            s[omero::model::enums::YM] = "ym";
            s[omero::model::enums::YOTTAM] = "Ym";
            s[omero::model::enums::ZETTAM] = "Zm";
            s[omero::model::enums::ZM] = "zm";
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

