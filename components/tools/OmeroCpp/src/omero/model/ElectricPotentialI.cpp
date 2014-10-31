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

        ElectricPotentialI::~ElectricPotentialI() {}

        ElectricPotentialI::ElectricPotentialI() : ElectricPotential() {
        }

        Ice::Double ElectricPotentialI::getValue(const Ice::Current& /* current */) {
            return value;
        }

        void ElectricPotentialI::setValue(Ice::Double _value, const Ice::Current& /* current */) {
            value = _value;
        }

        UnitsElectricPotentialPtr ElectricPotentialI::getUnit(const Ice::Current& /* current */) {
            return unit;
        }

        void ElectricPotentialI::setUnit(const UnitsElectricPotentialPtr& _unit, const Ice::Current& /* current */) {
            unit = _unit;
        }

        ElectricPotentialPtr ElectricPotentialI::copy(const Ice::Current& /* current */) {
            ElectricPotentialPtr copy = new ElectricPotentialI();
            copy->setValue(getValue());
            copy->setUnit(getUnit());
            return copy;
        }
    }
}
