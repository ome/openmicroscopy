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


        static std::map<omero::model::enums::UnitsFrequency, std::string> makeSymbols(){
            std::map<omero::model::enums::UnitsFrequency, std::string> s;
            s[omero::model::enums::AHZ] = "aHz";
            s[omero::model::enums::CHZ] = "cHz";
            s[omero::model::enums::DAHZ] = "daHz";
            s[omero::model::enums::DHZ] = "dHz";
            s[omero::model::enums::EXAHZ] = "EHz";
            s[omero::model::enums::FHZ] = "fHz";
            s[omero::model::enums::GIGAHZ] = "GHz";
            s[omero::model::enums::HHZ] = "hHz";
            s[omero::model::enums::HZ] = "Hz";
            s[omero::model::enums::KHZ] = "kHz";
            s[omero::model::enums::MEGAHZ] = "MHz";
            s[omero::model::enums::MHZ] = "mHz";
            s[omero::model::enums::MICROHZ] = "µHz";
            s[omero::model::enums::NHZ] = "nHz";
            s[omero::model::enums::PETAHZ] = "PHz";
            s[omero::model::enums::PHZ] = "pHz";
            s[omero::model::enums::TERAHZ] = "THz";
            s[omero::model::enums::YHZ] = "yHz";
            s[omero::model::enums::YOTTAHZ] = "YHz";
            s[omero::model::enums::ZETTAHZ] = "ZHz";
            s[omero::model::enums::ZHZ] = "zHz";
            return s;
        };

        std::map<omero::model::enums::UnitsFrequency, std::string> FrequencyI::SYMBOLS = makeSymbols();

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

