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


        static std::map<omero::model::enums::UnitsPower, std::string> makeSymbols(){
            std::map<omero::model::enums::UnitsPower, std::string> s;
            s[omero::model::enums::ATTOWATT] = "aW";
            s[omero::model::enums::CENTIWATT] = "cW";
            s[omero::model::enums::DECIWATT] = "dW";
            s[omero::model::enums::DEKAWATT] = "daW";
            s[omero::model::enums::EXAWATT] = "EW";
            s[omero::model::enums::FEMTOWATT] = "fW";
            s[omero::model::enums::GIGAWATT] = "GW";
            s[omero::model::enums::HECTOWATT] = "hW";
            s[omero::model::enums::KILOWATT] = "kW";
            s[omero::model::enums::MEGAWATT] = "MW";
            s[omero::model::enums::MICROWATT] = "µW";
            s[omero::model::enums::MILLIWATT] = "mW";
            s[omero::model::enums::NANOWATT] = "nW";
            s[omero::model::enums::PETAWATT] = "PW";
            s[omero::model::enums::PICOWATT] = "pW";
            s[omero::model::enums::TERAWATT] = "TW";
            s[omero::model::enums::WATT] = "W";
            s[omero::model::enums::YOCTOWATT] = "yW";
            s[omero::model::enums::YOTTAWATT] = "YW";
            s[omero::model::enums::ZEPTOWATT] = "zW";
            s[omero::model::enums::ZETTAWATT] = "ZW";
            return s;
        };

        std::map<omero::model::enums::UnitsPower, std::string> PowerI::SYMBOLS = makeSymbols();

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

