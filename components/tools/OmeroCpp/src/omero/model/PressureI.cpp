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

#include <omero/model/PressureI.h>

::Ice::Object* IceInternal::upCast(::omero::model::PressureI* t) { return t; }

namespace omero {

    namespace model {

        PressureI::~PressureI() {}

        PressureI::PressureI() : Pressure() {
        }

        Ice::Double PressureI::getValue(const Ice::Current& /* current */) {
            return value;
        }

        void PressureI::setValue(Ice::Double _value, const Ice::Current& /* current */) {
            value = _value;
        }

        UnitsPressurePtr PressureI::getUnit(const Ice::Current& /* current */) {
            return unit;
        }

        void PressureI::setUnit(const UnitsPressurePtr& _unit, const Ice::Current& /* current */) {
            unit = _unit;
        }

        PressurePtr PressureI::copy(const Ice::Current& /* current */) {
            PressurePtr copy = new PressureI();
            copy->setValue(getValue());
            copy->setUnit(getUnit());
            return copy;
        }
    }
}
