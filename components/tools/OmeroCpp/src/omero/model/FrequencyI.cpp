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
            s[omero::model::enums::ATTOHERTZ] = "aHz";
            s[omero::model::enums::CENTIHERTZ] = "cHz";
            s[omero::model::enums::DECAHERTZ] = "daHz";
            s[omero::model::enums::DECIHERTZ] = "dHz";
            s[omero::model::enums::EXAHERTZ] = "EHz";
            s[omero::model::enums::FEMTOHERTZ] = "fHz";
            s[omero::model::enums::GIGAHERTZ] = "GHz";
            s[omero::model::enums::HECTOHERTZ] = "hHz";
            s[omero::model::enums::HERTZ] = "Hz";
            s[omero::model::enums::KILOHERTZ] = "kHz";
            s[omero::model::enums::MEGAHERTZ] = "MHz";
            s[omero::model::enums::MICROHERTZ] = "µHz";
            s[omero::model::enums::MILLIHERTZ] = "mHz";
            s[omero::model::enums::NANOHERTZ] = "nHz";
            s[omero::model::enums::PETAHERTZ] = "PHz";
            s[omero::model::enums::PICOHERTZ] = "pHz";
            s[omero::model::enums::TERAHERTZ] = "THz";
            s[omero::model::enums::YOCTOHERTZ] = "yHz";
            s[omero::model::enums::YOTTAHERTZ] = "YHz";
            s[omero::model::enums::ZEPTOHERTZ] = "zHz";
            s[omero::model::enums::ZETTAHERTZ] = "ZHz";
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

