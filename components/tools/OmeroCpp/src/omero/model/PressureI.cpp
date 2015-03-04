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


        static std::map<omero::model::enums::UnitsPressure, std::string> makeSymbols(){
            std::map<omero::model::enums::UnitsPressure, std::string> s;
            s[omero::model::enums::ATMOSPHERE] = "atm";
            s[omero::model::enums::ATTOPASCAL] = "aPa";
            s[omero::model::enums::BAR] = "bar";
            s[omero::model::enums::CENTIBAR] = "cbar";
            s[omero::model::enums::CENTIPASCAL] = "cPa";
            s[omero::model::enums::DECAPASCAL] = "daPa";
            s[omero::model::enums::DECIBAR] = "dbar";
            s[omero::model::enums::DECIPASCAL] = "dPa";
            s[omero::model::enums::EXAPASCAL] = "EPa";
            s[omero::model::enums::FEMTOPASCAL] = "fPa";
            s[omero::model::enums::GIGAPASCAL] = "GPa";
            s[omero::model::enums::HECTOPASCAL] = "hPa";
            s[omero::model::enums::KILOBAR] = "kbar";
            s[omero::model::enums::KILOPASCAL] = "kPa";
            s[omero::model::enums::MEGABAR] = "Mbar";
            s[omero::model::enums::MEGAPASCAL] = "MPa";
            s[omero::model::enums::MICROPASCAL] = "µPa";
            s[omero::model::enums::MILLIBAR] = "mbar";
            s[omero::model::enums::MILLIPASCAL] = "mPa";
            s[omero::model::enums::MILLITORR] = "mTorr";
            s[omero::model::enums::MMHG] = "mm Hg";
            s[omero::model::enums::NANOPASCAL] = "nPa";
            s[omero::model::enums::PETAPASCAL] = "PPa";
            s[omero::model::enums::PICOPASCAL] = "pPa";
            s[omero::model::enums::PSI] = "psi";
            s[omero::model::enums::Pascal] = "Pa";
            s[omero::model::enums::TERAPASCAL] = "TPa";
            s[omero::model::enums::TORR] = "Torr";
            s[omero::model::enums::YOCTOPASCAL] = "yPa";
            s[omero::model::enums::YOTTAPASCAL] = "YPa";
            s[omero::model::enums::ZEPTOPASCAL] = "zPa";
            s[omero::model::enums::ZETTAPASCAL] = "ZPa";
            return s;
        };

        std::map<omero::model::enums::UnitsPressure, std::string> PressureI::SYMBOLS = makeSymbols();

        PressureI::~PressureI() {}

        PressureI::PressureI() : Pressure() {
        }

        Ice::Double PressureI::getValue(const Ice::Current& /* current */) {
            return value;
        }

        void PressureI::setValue(Ice::Double _value, const Ice::Current& /* current */) {
            value = _value;
        }

        omero::model::enums::UnitsPressure PressureI::getUnit(const Ice::Current& /* current */) {
            return unit;
        }

        void PressureI::setUnit(omero::model::enums::UnitsPressure _unit, const Ice::Current& /* current */) {
            unit = _unit;
        }

        std::string PressureI::getSymbol(const Ice::Current& /* current */) {
            return SYMBOLS[unit];
        }

        PressurePtr PressureI::copy(const Ice::Current& /* current */) {
            PressurePtr copy = new PressureI();
            copy->setValue(getValue());
            copy->setUnit(getUnit());
            return copy;
        }
    }
}

