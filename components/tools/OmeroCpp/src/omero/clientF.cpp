/*
   Callback implementations.

   Copyright 2010 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

*/

#include <omero/clientF.h>
#include <omero/client.h>

::Ice::Object* IceInternal::upCast(::omero::CallbackI* p) { return p; }
