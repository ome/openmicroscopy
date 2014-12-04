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

::Ice::Object* IceInternal::upCast(::omero::model::TimeI* t) { return t; }

namespace omero {

    namespace model {


        static std::map<omero::model::enums::UnitsTime, std::string> makeSymbols(){
            std::map<omero::model::enums::UnitsTime, std::string> s;
            s[omero::model::enums::AS] = "as";
            s[omero::model::enums::CS] = "cs";
            s[omero::model::enums::D] = "d";
            s[omero::model::enums::DAS] = "das";
            s[omero::model::enums::DS] = "ds";
            s[omero::model::enums::EXAS] = "Es";
            s[omero::model::enums::FS] = "fs";
            s[omero::model::enums::GIGAS] = "Gs";
            s[omero::model::enums::H] = "h";
            s[omero::model::enums::HS] = "hs";
            s[omero::model::enums::KS] = "ks";
            s[omero::model::enums::MEGAS] = "Ms";
            s[omero::model::enums::MICROS] = "µs";
            s[omero::model::enums::MIN] = "min";
            s[omero::model::enums::MS] = "ms";
            s[omero::model::enums::NS] = "ns";
            s[omero::model::enums::PETAS] = "Ps";
            s[omero::model::enums::PS] = "ps";
            s[omero::model::enums::S] = "s";
            s[omero::model::enums::TERAS] = "Ts";
            s[omero::model::enums::YOTTAS] = "Ys";
            s[omero::model::enums::YS] = "ys";
            s[omero::model::enums::ZETTAS] = "Zs";
            s[omero::model::enums::ZS] = "zs";
            return s;
        };

        std::map<omero::model::enums::UnitsTime, std::string> TimeI::SYMBOLS = makeSymbols();

        TimeI::~TimeI() {}

        TimeI::TimeI() : Time() {
        }

        Ice::Double TimeI::getValue(const Ice::Current& /* current */) {
            return value;
        }

        void TimeI::setValue(Ice::Double _value, const Ice::Current& /* current */) {
            value = _value;
        }

        omero::model::enums::UnitsTime TimeI::getUnit(const Ice::Current& /* current */) {
            return unit;
        }

        void TimeI::setUnit(omero::model::enums::UnitsTime _unit, const Ice::Current& /* current */) {
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

