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

::Ice::Object* IceInternal::upCast(::omero::model::ElectricPotentialI* t) { return t; }

namespace omero {

    namespace model {


        static std::map<omero::model::enums::UnitsElectricPotential, std::string> makeSymbols(){
            std::map<omero::model::enums::UnitsElectricPotential, std::string> s;
            s[omero::model::enums::AV] = "aV";
            s[omero::model::enums::CV] = "cV";
            s[omero::model::enums::DAV] = "daV";
            s[omero::model::enums::DV] = "dV";
            s[omero::model::enums::EXAV] = "EV";
            s[omero::model::enums::FV] = "fV";
            s[omero::model::enums::GIGAV] = "GV";
            s[omero::model::enums::HV] = "hV";
            s[omero::model::enums::KV] = "kV";
            s[omero::model::enums::MEGAV] = "MV";
            s[omero::model::enums::MICROV] = "µV";
            s[omero::model::enums::MV] = "mV";
            s[omero::model::enums::NV] = "nV";
            s[omero::model::enums::PETAV] = "PV";
            s[omero::model::enums::PV] = "pV";
            s[omero::model::enums::TERAV] = "TV";
            s[omero::model::enums::V] = "V";
            s[omero::model::enums::YOTTAV] = "YV";
            s[omero::model::enums::YV] = "yV";
            s[omero::model::enums::ZETTAV] = "ZV";
            s[omero::model::enums::ZV] = "zV";
            return s;
        };

        std::map<omero::model::enums::UnitsElectricPotential, std::string> ElectricPotentialI::SYMBOLS = makeSymbols();

        ElectricPotentialI::~ElectricPotentialI() {}

        ElectricPotentialI::ElectricPotentialI() : ElectricPotential() {
        }

        Ice::Double ElectricPotentialI::getValue(const Ice::Current& /* current */) {
            return value;
        }

        void ElectricPotentialI::setValue(Ice::Double _value, const Ice::Current& /* current */) {
            value = _value;
        }

        omero::model::enums::UnitsElectricPotential ElectricPotentialI::getUnit(const Ice::Current& /* current */) {
            return unit;
        }

        void ElectricPotentialI::setUnit(omero::model::enums::UnitsElectricPotential _unit, const Ice::Current& /* current */) {
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

