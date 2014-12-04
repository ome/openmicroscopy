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

        std::map<omero::model::enums::UnitsLength, std::string> LengthI::SYMBOLS= {
            {omero::model::enums::UnitsLength::AM, "am"},
            {omero::model::enums::UnitsLength::ANGSTROM, "Å"},
            {omero::model::enums::UnitsLength::CM, "cm"},
            {omero::model::enums::UnitsLength::DAM, "dam"},
            {omero::model::enums::UnitsLength::DM, "dm"},
            {omero::model::enums::UnitsLength::EXAM, "Em"},
            {omero::model::enums::UnitsLength::FM, "fm"},
            {omero::model::enums::UnitsLength::FOOT, "ft"},
            {omero::model::enums::UnitsLength::GIGAM, "Gm"},
            {omero::model::enums::UnitsLength::HM, "hm"},
            {omero::model::enums::UnitsLength::INCH, "in"},
            {omero::model::enums::UnitsLength::KM, "km"},
            {omero::model::enums::UnitsLength::LINE, "li"},
            {omero::model::enums::UnitsLength::LY, "ly"},
            {omero::model::enums::UnitsLength::M, "m"},
            {omero::model::enums::UnitsLength::MEGAM, "Mm"},
            {omero::model::enums::UnitsLength::MICROM, "µm"},
            {omero::model::enums::UnitsLength::MILE, "mi"},
            {omero::model::enums::UnitsLength::MM, "mm"},
            {omero::model::enums::UnitsLength::NM, "nm"},
            {omero::model::enums::UnitsLength::PC, "pc"},
            {omero::model::enums::UnitsLength::PETAM, "Pm"},
            {omero::model::enums::UnitsLength::PIXEL, "pixel"},
            {omero::model::enums::UnitsLength::PM, "pm"},
            {omero::model::enums::UnitsLength::POINT, "pt"},
            {omero::model::enums::UnitsLength::REFERENCEFRAME, "reference frame"},
            {omero::model::enums::UnitsLength::TERAM, "Tm"},
            {omero::model::enums::UnitsLength::THOU, "thou"},
            {omero::model::enums::UnitsLength::UA, "ua"},
            {omero::model::enums::UnitsLength::YARD, "yd"},
            {omero::model::enums::UnitsLength::YM, "ym"},
            {omero::model::enums::UnitsLength::YOTTAM, "Ym"},
            {omero::model::enums::UnitsLength::ZETTAM, "Zm"},
            {omero::model::enums::UnitsLength::ZM, "zm"},
        };

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

