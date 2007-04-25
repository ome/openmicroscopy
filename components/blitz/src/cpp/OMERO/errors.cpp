/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#include <Error.h>

using namespace std;

void omero::ClientError::ice_print(ostream& out) const
{
  Exception::ice_print(out);
  out << ":\nClient Error";
  if(!message.empty())
    {
      out << ":\n" << message;
    }
}; 

