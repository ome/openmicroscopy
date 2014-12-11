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

        std::map<omero::model::enums::UnitsElectricPotential, std::string> ElectricPotentialI::SYMBOLS= {
            {omero::model::enums::UnitsElectricPotential::AV, "aV"},
            {omero::model::enums::UnitsElectricPotential::CV, "cV"},
            {omero::model::enums::UnitsElectricPotential::DAV, "daV"},
            {omero::model::enums::UnitsElectricPotential::DV, "dV"},
            {omero::model::enums::UnitsElectricPotential::EXAV, "EV"},
            {omero::model::enums::UnitsElectricPotential::FV, "fV"},
            {omero::model::enums::UnitsElectricPotential::GIGAV, "GV"},
            {omero::model::enums::UnitsElectricPotential::HV, "hV"},
            {omero::model::enums::UnitsElectricPotential::KV, "kV"},
            {omero::model::enums::UnitsElectricPotential::MEGAV, "MV"},
            {omero::model::enums::UnitsElectricPotential::MICROV, "µV"},
            {omero::model::enums::UnitsElectricPotential::MV, "mV"},
            {omero::model::enums::UnitsElectricPotential::NV, "nV"},
            {omero::model::enums::UnitsElectricPotential::PETAV, "PV"},
            {omero::model::enums::UnitsElectricPotential::PV, "pV"},
            {omero::model::enums::UnitsElectricPotential::TERAV, "TV"},
            {omero::model::enums::UnitsElectricPotential::V, "V"},
            {omero::model::enums::UnitsElectricPotential::YOTTAV, "YV"},
            {omero::model::enums::UnitsElectricPotential::YV, "yV"},
            {omero::model::enums::UnitsElectricPotential::ZETTAV, "ZV"},
            {omero::model::enums::UnitsElectricPotential::ZV, "zV"},
        };

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

