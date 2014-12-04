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

#ifndef OMERO_MODEL_TEMPERATUREI_H
#define OMERO_MODEL_TEMPERATUREI_H

#include <omero/IceNoWarnPush.h>
#include <omero/model/Temperature.h>
#include <omero/model/Units.h>
#include <omero/IceNoWarnPop.h>

#ifndef OMERO_CLIENT
#   ifdef OMERO_CLIENT_EXPORTS
#       define OMERO_CLIENT ICE_DECLSPEC_EXPORT
#   else
#       define OMERO_CLIENT ICE_DECLSPEC_IMPORT
#   endif
#endif

namespace omero {
  namespace model {
    class TemperatureI;
  }
}

namespace IceInternal {
  OMERO_CLIENT ::Ice::Object* upCast(::omero::model::TemperatureI*);
}

namespace omero {
  namespace model {

    typedef IceInternal::Handle<TemperatureI> TemperatureIPtr;

    class OMERO_CLIENT TemperatureI : virtual public Temperature {

    protected:
        virtual ~TemperatureI(); // protected as outlined in Ice docs.
        static std::map<omero::model::enums::UnitsTemperature, std::string> SYMBOLS;

    public:
        TemperatureI();

        virtual Ice::Double getValue(
                const Ice::Current& current = Ice::Current());

        virtual void setValue(
                Ice::Double value,
                const Ice::Current& current = Ice::Current());

        virtual omero::model::enums::UnitsTemperature getUnit(
                const Ice::Current& current = Ice::Current());

        virtual void setUnit(
                omero::model::enums::UnitsTemperature unit,
                const Ice::Current& current = Ice::Current());

        virtual std::string getSymbol(
                const Ice::Current& current = Ice::Current());

        virtual TemperaturePtr copy(
                const Ice::Current& = Ice::Current());

    };
  }
}
#endif // OMERO_MODEL_TEMPERATUREI_H

