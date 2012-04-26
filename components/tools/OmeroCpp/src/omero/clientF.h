/*
 *   Copyright 2007-2012 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_CLIENTF_H
#define OMERO_CLIENTF_H

#include <IceUtil/Handle.h>

namespace omero {

    /*
     * Forward definitions and handles
     */

    class client;

    /*
     * Typedef for using Ice's smart pointer reference counting
     * infrastructure.
     *
     *  omero::client_ptr client1 = new omero::client("localhost");
     *  omero::client_ptr client2 = new omero::client("localhost", port);
     */
    typedef IceUtil::Handle<client> client_ptr;

    class CallbackI;

    typedef IceUtil::Handle<CallbackI> CallbackIPtr;

}
#endif // OMERO_CLIENTF_H
