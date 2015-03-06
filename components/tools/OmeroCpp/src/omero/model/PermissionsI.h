/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef PERMISSIONSI_H
#define PERMISSIONSI_H

#include <omero/IceNoWarnPush.h>
#include <omero/model/Permissions.h>
#include <omero/IceNoWarnPop.h>
#include <Ice/Config.h>
#include <IceUtil/Config.h>
#include <Ice/Handle.h>
#include <iostream>
#include <string>
#include <vector>

#ifndef OMERO_CLIENT
#   ifdef OMERO_CLIENT_EXPORTS
#       define OMERO_CLIENT ICE_DECLSPEC_EXPORT
#   else
#       define OMERO_CLIENT ICE_DECLSPEC_IMPORT
#   endif
#endif

namespace omero {
  namespace model {
    class PermissionsI;
  }
}

namespace IceInternal {
  OMERO_CLIENT ::Ice::Object* upCast(::omero::model::PermissionsI*);
}

namespace omero {
  namespace model {

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

  typedef IceInternal::Handle<PermissionsI> PermissionsIPtr;

class OMERO_CLIENT PermissionsI : virtual public Permissions {

protected:
    virtual ~PermissionsI(); // protected as outlined in Ice docs.
    bool granted(int mask, int shift);
    void set(int mask, int shift, bool value);
    void throwIfImmutable();
    bool __immutable;
public:

    PermissionsI(const std::string& perms = "");
    virtual void ice_postUnmarshal(); // For setting __immutable

    virtual bool isRestricted(const std::string& restriction, const Ice::Current& current = Ice::Current());
    virtual bool isDisallow(const int restriction, const Ice::Current& current = Ice::Current());
    virtual bool canAnnotate(const Ice::Current& current = Ice::Current());
    virtual bool canDelete(const Ice::Current& current = Ice::Current());
    virtual bool canEdit(const Ice::Current& current = Ice::Current());
    virtual bool canLink(const Ice::Current& current = Ice::Current());

    /*
     * Central methods. The optional argument is a requirement
     * of the Ice runtime and can safely be omitted.
     */
    virtual bool isUserRead(const Ice::Current& c = Ice::Current());
    virtual bool isUserAnnotate(const Ice::Current& c = Ice::Current());
    virtual bool isUserWrite(const Ice::Current& c = Ice::Current());
    virtual bool isGroupRead(const Ice::Current& c = Ice::Current());
    virtual bool isGroupAnnotate(const Ice::Current& c = Ice::Current());
    virtual bool isGroupWrite(const Ice::Current& c = Ice::Current());
    virtual bool isWorldRead(const Ice::Current& c = Ice::Current());
    virtual bool isWorldAnnotate(const Ice::Current& c = Ice::Current());
    virtual bool isWorldWrite(const Ice::Current& c = Ice::Current());
    virtual void setUserRead(bool value, const Ice::Current& c = Ice::Current());
    virtual void setUserAnnotate(bool value, const Ice::Current& c = Ice::Current());
    virtual void setUserWrite(bool value, const Ice::Current& c = Ice::Current());
    virtual void setGroupRead(bool value, const Ice::Current& c = Ice::Current());
    virtual void setGroupAnnotate(bool value, const Ice::Current& c = Ice::Current());
    virtual void setGroupWrite(bool value, const Ice::Current& c = Ice::Current());
    virtual void setWorldRead(bool value, const Ice::Current& c = Ice::Current());
    virtual void setWorldAnnotate(bool value, const Ice::Current& c = Ice::Current());
    virtual void setWorldWrite(bool value, const Ice::Current& c = Ice::Current());

    // Do not use !
    virtual Ice::Long getPerm1(const Ice::Current& current = Ice::Current());

    // Do not use !
    virtual void setPerm1(Ice::Long _perm1, const Ice::Current& current = Ice::Current());

    // Meaningless for Permissions. No complex state.
    virtual void unload(const Ice::Current& c = Ice::Current());

  };

 }
}
#endif // PERMISSIONSI_H
