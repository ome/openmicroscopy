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

        LengthPtr LengthI::copy(const Ice::Current& /* current */) {
            LengthPtr copy = new LengthI();
            copy->setValue(getValue());
            copy->setUnit(getUnit());
            return copy;
        }
    }
}

