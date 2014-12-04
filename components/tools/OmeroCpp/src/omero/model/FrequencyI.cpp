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

#include <omero/model/FrequencyI.h>

::Ice::Object* IceInternal::upCast(::omero::model::FrequencyI* t) { return t; }

namespace omero {

    namespace model {

        std::map<omero::model::enums::UnitsFrequency, std::string> FrequencyI::SYMBOLS= {
            {omero::model::enums::UnitsFrequency::AHZ, "aHz"},
            {omero::model::enums::UnitsFrequency::CHZ, "cHz"},
            {omero::model::enums::UnitsFrequency::DAHZ, "daHz"},
            {omero::model::enums::UnitsFrequency::DHZ, "dHz"},
            {omero::model::enums::UnitsFrequency::EXAHZ, "EHz"},
            {omero::model::enums::UnitsFrequency::FHZ, "fHz"},
            {omero::model::enums::UnitsFrequency::GIGAHZ, "GHz"},
            {omero::model::enums::UnitsFrequency::HHZ, "hHz"},
            {omero::model::enums::UnitsFrequency::HZ, "Hz"},
            {omero::model::enums::UnitsFrequency::KHZ, "kHz"},
            {omero::model::enums::UnitsFrequency::MEGAHZ, "MHz"},
            {omero::model::enums::UnitsFrequency::MHZ, "mHz"},
            {omero::model::enums::UnitsFrequency::MICROHZ, "µHz"},
            {omero::model::enums::UnitsFrequency::NHZ, "nHz"},
            {omero::model::enums::UnitsFrequency::PETAHZ, "PHz"},
            {omero::model::enums::UnitsFrequency::PHZ, "pHz"},
            {omero::model::enums::UnitsFrequency::TERAHZ, "THz"},
            {omero::model::enums::UnitsFrequency::YHZ, "yHz"},
            {omero::model::enums::UnitsFrequency::YOTTAHZ, "YHz"},
            {omero::model::enums::UnitsFrequency::ZETTAHZ, "ZHz"},
            {omero::model::enums::UnitsFrequency::ZHZ, "zHz"},
        };

        FrequencyI::~FrequencyI() {}

        FrequencyI::FrequencyI() : Frequency() {
        }

        Ice::Double FrequencyI::getValue(const Ice::Current& /* current */) {
            return value;
        }

        void FrequencyI::setValue(Ice::Double _value, const Ice::Current& /* current */) {
            value = _value;
        }

        omero::model::enums::UnitsFrequency FrequencyI::getUnit(const Ice::Current& /* current */) {
            return unit;
        }

        void FrequencyI::setUnit(omero::model::enums::UnitsFrequency _unit, const Ice::Current& /* current */) {
            unit = _unit;
        }

        std::string FrequencyI::getSymbol(const Ice::Current& /* current */) {
            return SYMBOLS[unit];
        }

        FrequencyPtr FrequencyI::copy(const Ice::Current& /* current */) {
            FrequencyPtr copy = new FrequencyI();
            copy->setValue(getValue());
            copy->setUnit(getUnit());
            return copy;
        }
    }
}

