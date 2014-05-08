/*
 *   $Id$
 *
 *   Copyright 2007,2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#include <omero/model/DetailsI.h>

#if ICE_INT_VERSION / 100 >= 304
::Ice::Object* IceInternal::upCast(::omero::model::DetailsI* p) { return p; }
#endif

namespace omero {

    namespace model {

	DetailsI::DetailsI(
                const omero::client* client)
                    : Details(), client(client) {
            if (client) {
                session = client->getSession();
            }
        }
	DetailsI::~DetailsI() {}

    }

}
