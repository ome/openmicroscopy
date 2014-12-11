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

::Ice::Object* IceInternal::upCast(::omero::model::PowerI* t) { return t; }

namespace omero {

    namespace model {

        std::map<omero::model::enums::UnitsPower, std::string> PowerI::SYMBOLS= {
            {omero::model::enums::UnitsPower::AW, "aW"},
            {omero::model::enums::UnitsPower::CW, "cW"},
            {omero::model::enums::UnitsPower::DAW, "daW"},
            {omero::model::enums::UnitsPower::DW, "dW"},
            {omero::model::enums::UnitsPower::EXAW, "EW"},
            {omero::model::enums::UnitsPower::FW, "fW"},
            {omero::model::enums::UnitsPower::GIGAW, "GW"},
            {omero::model::enums::UnitsPower::HW, "hW"},
            {omero::model::enums::UnitsPower::KW, "kW"},
            {omero::model::enums::UnitsPower::MEGAW, "MW"},
            {omero::model::enums::UnitsPower::MICROW, "µW"},
            {omero::model::enums::UnitsPower::MW, "mW"},
            {omero::model::enums::UnitsPower::NW, "nW"},
            {omero::model::enums::UnitsPower::PETAW, "PW"},
            {omero::model::enums::UnitsPower::PW, "pW"},
            {omero::model::enums::UnitsPower::TERAW, "TW"},
            {omero::model::enums::UnitsPower::W, "W"},
            {omero::model::enums::UnitsPower::YOTTAW, "YW"},
            {omero::model::enums::UnitsPower::YW, "yW"},
            {omero::model::enums::UnitsPower::ZETTAW, "ZW"},
            {omero::model::enums::UnitsPower::ZW, "zW"},
        };

        PowerI::~PowerI() {}

        PowerI::PowerI() : Power() {
        }

        Ice::Double PowerI::getValue(const Ice::Current& /* current */) {
            return value;
        }

        void PowerI::setValue(Ice::Double _value, const Ice::Current& /* current */) {
            value = _value;
        }

        omero::model::enums::UnitsPower PowerI::getUnit(const Ice::Current& /* current */) {
            return unit;
        }

        void PowerI::setUnit(omero::model::enums::UnitsPower _unit, const Ice::Current& /* current */) {
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

