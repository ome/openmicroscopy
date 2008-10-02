/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_SYS_PARAMETERSI_H
#define OMERO_SYS_PARAMETERSI_H

#include <omero/System.h>
#include <IceUtil/Handle.h>
#include <Ice/Config.h>
#include <iostream>
#include <string>
#include <vector>

namespace omero {

    namespace sys {

        class ParametersI; // Forward
        typedef IceUtil::Handle<ParametersI> ParametersIPtr;

        /*
         *
         */
        class ParametersI : virtual public Parameters {

    protected:
        ~ParametersI(); // protected as outlined in Ice docs.
    public:

        ParametersI();

        ParametersIPtr addId(Ice::Long id, const Ice::Current& c = Ice::Current());

        };

    }

}
#endif // OMERO_SYS_PARAMETERSI_H

