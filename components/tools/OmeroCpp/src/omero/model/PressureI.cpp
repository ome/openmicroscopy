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

        std::map<omero::model::enums::UnitsPressure, std::string> PressureI::SYMBOLS= {
            {omero::model::enums::UnitsPressure::APA, "aPa"},
            {omero::model::enums::UnitsPressure::ATM, "atm"},
            {omero::model::enums::UnitsPressure::BAR, "bar"},
            {omero::model::enums::UnitsPressure::CBAR, "cbar"},
            {omero::model::enums::UnitsPressure::CPA, "cPa"},
            {omero::model::enums::UnitsPressure::DAPA, "daPa"},
            {omero::model::enums::UnitsPressure::DBAR, "dbar"},
            {omero::model::enums::UnitsPressure::DPA, "dPa"},
            {omero::model::enums::UnitsPressure::EXAPA, "EPa"},
            {omero::model::enums::UnitsPressure::FPA, "fPa"},
            {omero::model::enums::UnitsPressure::GIGAPA, "GPa"},
            {omero::model::enums::UnitsPressure::HPA, "hPa"},
            {omero::model::enums::UnitsPressure::KBAR, "kBar"},
            {omero::model::enums::UnitsPressure::KPA, "kPa"},
            {omero::model::enums::UnitsPressure::MBAR, "mbar"},
            {omero::model::enums::UnitsPressure::MEGABAR, "Mbar"},
            {omero::model::enums::UnitsPressure::MEGAPA, "MPa"},
            {omero::model::enums::UnitsPressure::MICROPA, "µPa"},
            {omero::model::enums::UnitsPressure::MMHG, "mm Hg"},
            {omero::model::enums::UnitsPressure::MPA, "mPa"},
            {omero::model::enums::UnitsPressure::MTORR, "mTorr"},
            {omero::model::enums::UnitsPressure::NPA, "nPa"},
            {omero::model::enums::UnitsPressure::PA, "Pa"},
            {omero::model::enums::UnitsPressure::PETAPA, "PPa"},
            {omero::model::enums::UnitsPressure::PPA, "pPa"},
            {omero::model::enums::UnitsPressure::PSI, "psi"},
            {omero::model::enums::UnitsPressure::TERAPA, "TPa"},
            {omero::model::enums::UnitsPressure::TORR, "Torr"},
            {omero::model::enums::UnitsPressure::YOTTAPA, "YPa"},
            {omero::model::enums::UnitsPressure::YPA, "yPa"},
            {omero::model::enums::UnitsPressure::ZETTAPA, "ZPa"},
            {omero::model::enums::UnitsPressure::ZPA, "zPa"},
        };

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

