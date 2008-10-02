/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#include <omero/sys/ParametersI.h>

namespace omero {

    namespace sys {

        ParametersI::~ParametersI() {}

        ParametersI::ParametersI() : Parameters() {
            map = omero::sys::ParamMap();
        }

        ParametersIPtr ParametersI::addId(Ice::Long id, const Ice::Current& current) {
            map["id"] = new omero::RLong(id);
        }

    }

}
