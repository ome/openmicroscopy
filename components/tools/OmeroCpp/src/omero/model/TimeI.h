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

#ifndef OMERO_MODEL_TIMEI_H
#define OMERO_MODEL_TIMEI_H

#include <omero/model/Time.h>
#include <omero/model/UnitsTime.h>

#ifndef OMERO_CLIENT
#   ifdef OMERO_CLIENT_EXPORTS
#       define OMERO_CLIENT ICE_DECLSPEC_EXPORT
#   else
#       define OMERO_CLIENT ICE_DECLSPEC_IMPORT
#   endif
#endif

namespace omero {
  namespace model {
    class TimeI;
  }
}

namespace IceInternal {
  OMERO_CLIENT ::Ice::Object* upCast(::omero::model::TimeI*);
}

namespace omero {
  namespace model {

    typedef IceInternal::Handle<TimeI> TimeIPtr;

    class OMERO_CLIENT TimeI : virtual public Time {

    protected:
        virtual ~TimeI(); // protected as outlined in Ice docs.

    public:
        TimeI();

        virtual Ice::Double getValue(const Ice::Current& current = Ice::Current());

        virtual void setValue(Ice::Double value, const Ice::Current& current = Ice::Current());

        virtual UnitsTimePtr getUnit(const Ice::Current& current = Ice::Current());

        virtual void setUnit(const UnitsTimePtr& time, const Ice::Current& current = Ice::Current());

        virtual TimePtr copy(const Ice::Current& = Ice::Current());

    };
  }
}
#endif // OMERO_MODEL_TIMEI_H
