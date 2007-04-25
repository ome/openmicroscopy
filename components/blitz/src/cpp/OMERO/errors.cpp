/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#include <OMERO/Error.h>

using namespace std;

#define ERROR(TYPE)                                          \
void omero::TYPE::ice_print(ostream& out) const              \
{                                                            \
  Exception::ice_print(out);                                 \
  out << ":\nTYPE";                                          \
  if(!message.empty())                                       \
    {                                                        \
      out << ":\n" << message;                               \
    }                                                        \
};                                                           \
//

ERROR(ClientError)
ERROR(UnloadedEntityException)
ERROR(UnloadedCollectionException)

