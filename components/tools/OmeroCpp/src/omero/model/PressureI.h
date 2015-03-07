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

#ifndef OMERO_MODEL_PRESSUREI_H
#define OMERO_MODEL_PRESSUREI_H

#include <omero/IceNoWarnPush.h>
#include <omero/model/Pressure.h>
#include <omero/model/Units.h>
#include <omero/IceNoWarnPop.h>

#include <omero/conversions.h>

#ifndef OMERO_CLIENT
#   ifdef OMERO_CLIENT_EXPORTS
#       define OMERO_CLIENT ICE_DECLSPEC_EXPORT
#   else
#       define OMERO_CLIENT ICE_DECLSPEC_IMPORT
#   endif
#endif

namespace omero {
  namespace model {
    class PressureI;
  }
}

namespace IceInternal {
  OMERO_CLIENT ::Ice::Object* upCast(::omero::model::PressureI*);
}

namespace omero {
  namespace model {

    typedef IceInternal::Handle<PressureI> PressureIPtr;

    class OMERO_CLIENT PressureI : virtual public Pressure {

    protected:
        virtual ~PressureI(); // protected as outlined in Ice docs.
        static std::map<enums::UnitsPressure,
            std::map<enums::UnitsPressure,
                omero::conversion_types::ConversionPtr> > CONVERSIONS;
        static std::map<enums::UnitsPressure, std::string> SYMBOLS;

    public:

        static std::string lookupSymbol(enums::UnitsPressure unit) {
            return SYMBOLS[unit];
        }

        PressureI();

        PressureI(const double& value, const enums::UnitsPressure& unit);

        // Conversion constructor
        PressureI(const PressurePtr& value, const enums::UnitsPressure& target);

        virtual Ice::Double getValue(
                const Ice::Current& current = Ice::Current());

        virtual void setValue(
                Ice::Double value,
                const Ice::Current& current = Ice::Current());

        virtual enums::UnitsPressure getUnit(
                const Ice::Current& current = Ice::Current());

        virtual void setUnit(
                enums::UnitsPressure unit,
                const Ice::Current& current = Ice::Current());

        virtual std::string getSymbol(
                const Ice::Current& current = Ice::Current());

        virtual PressurePtr copy(
                const Ice::Current& = Ice::Current());

    };
  }
}
#endif // OMERO_MODEL_PRESSUREI_H

