/*
 *   $Id$
 * 
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 * 
 */

#ifndef CLASS_PERMISSIONS
#define CLASS_PERMISSIONS

#include <omero/fwd.ice>
#include <omero/model/IObject.ice>

module omero { 
  module model { 

    /*
     * Row-level permissions definition available on
     * every OMERO.blitz type. Represents a similar 
     * logic to the Unix filesystem.
     */
    class Permissions
    {
      // Internal representation. May change!
      long perm1;
      
      bool isUserRead();
      bool isUserWrite();
      bool isGroupRead();
      bool isGroupWrite();
      bool isWorldRead();
      bool isWorldWrite();
      bool isLocked();

      void setUserRead(bool value);
      void setUserWrite(bool value);
      void setGroupRead(bool value);
      void setGroupWrite(bool value);
      void setWorldRead(bool value);
      void setWorldWrite(bool value);
      void setLocked(bool value);
    };
  };
};
#endif 
