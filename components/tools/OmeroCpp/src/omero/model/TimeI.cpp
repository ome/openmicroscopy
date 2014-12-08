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

        std::map<omero::model::enums::UnitsTime, std::string> TimeI::SYMBOLS= {
            {omero::model::enums::UnitsTime::AS, "as"},
            {omero::model::enums::UnitsTime::CS, "cs"},
            {omero::model::enums::UnitsTime::D, "d"},
            {omero::model::enums::UnitsTime::DAS, "das"},
            {omero::model::enums::UnitsTime::DS, "ds"},
            {omero::model::enums::UnitsTime::EXAS, "Es"},
            {omero::model::enums::UnitsTime::FS, "fs"},
            {omero::model::enums::UnitsTime::GIGAS, "Gs"},
            {omero::model::enums::UnitsTime::H, "h"},
            {omero::model::enums::UnitsTime::HS, "hs"},
            {omero::model::enums::UnitsTime::KS, "ks"},
            {omero::model::enums::UnitsTime::MEGAS, "Ms"},
            {omero::model::enums::UnitsTime::MICROS, "µs"},
            {omero::model::enums::UnitsTime::MIN, "min"},
            {omero::model::enums::UnitsTime::MS, "ms"},
            {omero::model::enums::UnitsTime::NS, "ns"},
            {omero::model::enums::UnitsTime::PETAS, "Ps"},
            {omero::model::enums::UnitsTime::PS, "ps"},
            {omero::model::enums::UnitsTime::S, "s"},
            {omero::model::enums::UnitsTime::TERAS, "Ts"},
            {omero::model::enums::UnitsTime::YOTTAS, "Ys"},
            {omero::model::enums::UnitsTime::YS, "ys"},
            {omero::model::enums::UnitsTime::ZETTAS, "Zs"},
            {omero::model::enums::UnitsTime::ZS, "zs"},
        };

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

