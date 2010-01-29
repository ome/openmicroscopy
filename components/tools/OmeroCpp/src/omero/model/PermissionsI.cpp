/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#include <omero/model/PermissionsI.h>

namespace omero {

    namespace model {

	PermissionsI::~PermissionsI() {}
	PermissionsI::PermissionsI() : Permissions() {
	    perm1 = -1L;
	}

	// shift 8; mask 4
	bool PermissionsI::isUserRead(const Ice::Current& c) {
	    return granted(4,8);
	}
	void PermissionsI::setUserRead(bool value, const Ice::Current& c) {
	    set(4,8, value);
	}

	// shift 8; mask 2
	bool PermissionsI::isUserWrite(const Ice::Current& c) {
	    return granted(2,8);
	}
	void PermissionsI::setUserWrite(bool value, const Ice::Current& c) {
	    set(2,8, value);
	}

	// shift 4; mask 4
	bool PermissionsI::isGroupRead(const Ice::Current& c) {
	    return granted(4,4);
	}
	void PermissionsI::setGroupRead(bool value, const Ice::Current& c) {
	    set(4,4, value);
	}

	// shift 4; mask 2
	bool PermissionsI::isGroupWrite(const Ice::Current& c) {
	    return granted(2,4);
	}
	void PermissionsI::setGroupWrite(bool value, const Ice::Current& c) {
	    set(2,4, value);
	}

	// shift 0; mask 4
	bool PermissionsI::isWorldRead(const Ice::Current& c) {
	    return granted(4,0);
	}
	void PermissionsI::setWorldRead(bool value, const Ice::Current& c) {
	    set(4,0, value);
	}

	// shift 0; mask 2
	bool PermissionsI::isWorldWrite(const Ice::Current& c) {
	    return granted(2,0);
	}
	void PermissionsI::setWorldWrite(bool value, const Ice::Current& c) {
	    set(2,0, value);
	}

	// bit 18
	bool PermissionsI::isLocked(const Ice::Current& c) {
	    return !granted(1,18); // Here we use the granted
	    // logic but without a shift. The not is because
	    // flags are stored with reverse semantics.
	}
	void PermissionsI::setLocked(bool value, const Ice::Current& c) {
	    set(1,18,!value); // Here we use the set
	    // logic but without a shift. The not is because
	    // flags are stored with reverse semantics.
	}

	bool PermissionsI::granted(int mask, int shift) {
	    return (perm1 & (mask<<shift) ) == (mask<<shift);
	}

	void PermissionsI::set(int mask, int shift, bool on) {
	    if (on) {
		perm1 = perm1 | ( 0L  | (mask<<shift) );
	    } else {
		perm1 = perm1 & ( -1L ^ (mask<<shift) );
	    }
	}

    }
} //End omero::model
