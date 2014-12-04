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
            s[omero::model::enums::APA] = "aPa";
            s[omero::model::enums::ATM] = "atm";
            s[omero::model::enums::BAR] = "bar";
            s[omero::model::enums::CBAR] = "cbar";
            s[omero::model::enums::CPA] = "cPa";
            s[omero::model::enums::DAPA] = "daPa";
            s[omero::model::enums::DBAR] = "dbar";
            s[omero::model::enums::DPA] = "dPa";
            s[omero::model::enums::EXAPA] = "EPa";
            s[omero::model::enums::FPA] = "fPa";
            s[omero::model::enums::GIGAPA] = "GPa";
            s[omero::model::enums::HPA] = "hPa";
            s[omero::model::enums::KBAR] = "kBar";
            s[omero::model::enums::KPA] = "kPa";
            s[omero::model::enums::MBAR] = "mbar";
            s[omero::model::enums::MEGABAR] = "Mbar";
            s[omero::model::enums::MEGAPA] = "MPa";
            s[omero::model::enums::MICROPA] = "µPa";
            s[omero::model::enums::MMHG] = "mm Hg";
            s[omero::model::enums::MPA] = "mPa";
            s[omero::model::enums::MTORR] = "mTorr";
            s[omero::model::enums::NPA] = "nPa";
            s[omero::model::enums::PA] = "Pa";
            s[omero::model::enums::PETAPA] = "PPa";
            s[omero::model::enums::PPA] = "pPa";
            s[omero::model::enums::PSI] = "psi";
            s[omero::model::enums::TERAPA] = "TPa";
            s[omero::model::enums::TORR] = "Torr";
            s[omero::model::enums::YOTTAPA] = "YPa";
            s[omero::model::enums::YPA] = "yPa";
            s[omero::model::enums::ZETTAPA] = "ZPa";
            s[omero::model::enums::ZPA] = "zPa";
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

