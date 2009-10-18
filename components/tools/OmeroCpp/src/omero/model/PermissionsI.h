/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef PERMISSIONSI_H
#define PERMISSIONSI_H

#include <omero/model/Permissions.h>
#include <IceUtil/Handle.h>
#include <Ice/Config.h>
#include <iostream>
#include <string>
#include <vector>

#ifndef OMERO_API
#   ifdef OMERO_API_EXPORTS
#       define OMERO_API ICE_DECLSPEC_EXPORT
#   else
#       define OMERO_API ICE_DECLSPEC_IMPORT
#   endif
#endif

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
class OMERO_API PermissionsI : virtual public Permissions {

protected:
    ~PermissionsI(); // protected as outlined in Ice docs.
    bool granted(int mask, int shift);
    void set(int mask, int shift, bool value);
public:

    PermissionsI();

    /*
     * Central methods. The optional argument is a requirement
     * of the Ice runtime and can safely be omitted. The only
     * methods which are not self-explanatory are isLocked
     * and setLocked. These relate to whether or not the server
     * will allow certain actions on the holding type. This
     * would be of interest to know if the owner of a given
     * entity can be changed, or if it can have its read
     * permissions revoked. Doing so can cause strange
     * behavior for other users. It is always best to start
     * with the most strict permissions for objects, and
     * later elevate them.
     */
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

    // Do not use !
    Ice::Long getPerm1(const Ice::Current& current = Ice::Current()) {
        return  perm1 ;
    }

    // Do not use !
    void setPerm1(Ice::Long _perm1, const Ice::Current& current = Ice::Current()) {
        perm1 =  _perm1 ;

    }

    // Meaningless for Permissions. No complex state.
    void unload(const Ice::Current& c = Ice::Current());

  };

  typedef IceUtil::Handle<PermissionsI> PermissionsIPtr;

 }
}
#endif // PERMISSIONSI_H
