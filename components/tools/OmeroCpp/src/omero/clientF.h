/*
 *   Copyright 2007-2012 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_CLIENTF_H
#define OMERO_CLIENTF_H

#include <IceUtil/Config.h>
#if ICE_INT_VERSION / 100 >= 304
#   include <Ice/Handle.h>
#   include <Ice/Object.h>
#else
#   include <IceUtil/Handle.h>
#endif

#ifndef OMERO_API
#   ifdef OMERO_API_EXPORTS
#       define OMERO_API ICE_DECLSPEC_EXPORT
#   else
#       define OMERO_API ICE_DECLSPEC_IMPORT
#   endif
#endif

namespace omero {
    class client;
    class CallbackI;
}

#if ICE_INT_VERSION / 100 >= 304
namespace IceInternal {
  OMERO_API ::Ice::Object* upCast(::omero::CallbackI*);
}
#endif

namespace omero {
    /*
     * Typedef for using Ice's smart pointer reference counting
     * infrastructure.
     *
     *  omero::client_ptr client1 = new omero::client("localhost");
     *  omero::client_ptr client2 = new omero::client("localhost", port);
     */
    typedef IceUtil::Handle<client> client_ptr;

#if ICE_INT_VERSION / 100 >= 304
    typedef IceInternal::Handle<CallbackI> CallbackIPtr;
#else
    typedef IceUtil::Handle<CallbackI> CallbackIPtr;
#endif

}
#endif // OMERO_CLIENTF_H
