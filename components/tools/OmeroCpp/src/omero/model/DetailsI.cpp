/*
 *   $Id$
 *
 *   Copyright 2007,2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#include <omero/model/DetailsI.h>
#include <omero/API.h>

namespace omero {

    namespace model {

	DetailsI::DetailsI(
                const omero::client_ptr& client)
                    : Details(), client(client) {
            if (client) {
                session = client->getSession();
            }
        }
	DetailsI::~DetailsI() {}

    }

}
