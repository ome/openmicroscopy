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

#ifndef OMERO_MODEL_ELECTRICPOTENTIALI_H
#define OMERO_MODEL_ELECTRICPOTENTIALI_H

#include <omero/IceNoWarnPush.h>
#include <omero/model/ElectricPotential.h>
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
    class ElectricPotentialI;
  }
}

namespace IceInternal {
  OMERO_CLIENT ::Ice::Object* upCast(::omero::model::ElectricPotentialI*);
}

namespace omero {
  namespace model {

    typedef IceInternal::Handle<ElectricPotentialI> ElectricPotentialIPtr;

    class OMERO_CLIENT ElectricPotentialI : virtual public ElectricPotential {

    protected:
        virtual ~ElectricPotentialI(); // protected as outlined in Ice docs.
        static std::map<omero::model::enums::UnitsElectricPotential, std::string> SYMBOLS;

    public:
        ElectricPotentialI();

        virtual Ice::Double getValue(
                const Ice::Current& current = Ice::Current());

        virtual void setValue(
                Ice::Double value,
                const Ice::Current& current = Ice::Current());

        virtual omero::model::enums::UnitsElectricPotential getUnit(
                const Ice::Current& current = Ice::Current());

        virtual void setUnit(
                omero::model::enums::UnitsElectricPotential unit,
                const Ice::Current& current = Ice::Current());

        virtual std::string getSymbol(
                const Ice::Current& current = Ice::Current());

        virtual ElectricPotentialPtr copy(
                const Ice::Current& = Ice::Current());

    };
  }
}
#endif // OMERO_MODEL_ELECTRICPOTENTIALI_H

