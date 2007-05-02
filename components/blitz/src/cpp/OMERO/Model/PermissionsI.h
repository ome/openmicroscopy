/*
 *   $Id$
 * 
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 * 
 */

#include <OMERO/Model.h>
#include <Ice/Config.h>
#include <iostream>
#include <string>
#include <vector>

#ifndef PERMISSIONSI_H
#define PERMISSIONSI_H

namespace omero { namespace model {

  /*
   * Blitz wrapper for the permissions related to 
   * an entity. Though the internal state is made
   * public (see http://www.zeroc.com/forums/showthread.php?t=3084)
   * it is only intended for clients to use the methods:
   * 
   *  -- [is|set][User|Group|World][Read|Write]()
   *  -- [is|set]Locked
   *
   */
class PermissionsI : public Permissions { 

protected:
    ~PermissionsI(); // protected as outlined in docs.
    bool granted(int mask, int shift);
    void set(int mask, int shift, bool value);
public:

   /**
    * Default no-args constructor which manages the proper "loaded"
    * status of all {@link Collection}s by manually initializing them all
    * to an empty {@link Collection} of the approrpriate type.
    */
    PermissionsI();
    PermissionsI(omero::RLongPtr idPtr, bool isLoaded = false);
    void unload(const Ice::Current& c = Ice::Current());
    bool isUserRead(const Ice::Current& c = Ice::Current());
    bool isUserWrite(const Ice::Current& c = Ice::Current());
    bool isGroupRead(const Ice::Current& c = Ice::Current());
    bool isGroupWrite(const Ice::Current& c = Ice::Current());
    bool isWorldRead(const Ice::Current& c = Ice::Current());
    bool isWorldWrite(const Ice::Current& c = Ice::Current());
    bool isLocked(const Ice::Current& c = Ice::Current());
    void setUserRead(bool value, const Ice::Current& c = Ice::Current());
    void setUserWrite(bool value, const Ice::Current& c = Ice::Current());
    void setGroupRead(bool value, const Ice::Current& c = Ice::Current());
    void setGroupWrite(bool value, const Ice::Current& c = Ice::Current());
    void setWorldRead(bool value, const Ice::Current& c = Ice::Current());
    void setWorldWrite(bool value, const Ice::Current& c = Ice::Current());
    void setLocked(bool value, const Ice::Current& c = Ice::Current());

    long getPerm1() {
        return  perm1 ;
    }
    
    void setPerm1(long _perm1) {
        perm1 =  _perm1 ;
         
    }
 
  };

 typedef IceUtil::Handle<PermissionsI> PermissionsIPtr;

 }
}
#endif // PERMISSIONSI_H
 
