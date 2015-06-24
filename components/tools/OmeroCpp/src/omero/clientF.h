/*
 *   Copyright 2007-2012 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_CLIENTF_H
#define OMERO_CLIENTF_H

#include <IceUtil/Config.h>
#include <Ice/Handle.h>
#include <omero/IceNoWarnPush.h>
#include <Ice/Object.h>
#include <omero/IceNoWarnPop.h>

#ifndef OMERO_CLIENT
#   ifdef OMERO_CLIENT_EXPORTS
#       define OMERO_CLIENT ICE_DECLSPEC_EXPORT
#   else
#       define OMERO_CLIENT ICE_DECLSPEC_IMPORT
#   endif
#endif

namespace omero {
    class client;
    class CallbackI;
}

namespace IceInternal {
  OMERO_CLIENT ::Ice::Object* upCast(::omero::CallbackI*);
}

namespace omero {
    /*
     * Typedef for using Ice's smart pointer reference counting
     * infrastructure.
     *
     *  omero::client_ptr client1 = new omero::client("localhost");
     *  omero::client_ptr client2 = new omero::client("localhost", port);
     */
    typedef IceUtil::Handle<client> client_ptr;

    typedef IceInternal::Handle<CallbackI> CallbackIPtr;
}
#endif // OMERO_CLIENTF_H
