/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#include <omero/model/PermissionsI.h>
#include <omero/fixture.h>
#include <omero/ClientErrors.h>

using namespace omero;
using namespace omero::model;

TEST( PermsTest, Perm1 )
{
  omero::model::PermissionsIPtr p = new omero::model::PermissionsI();

  // The default
  ASSERT_TRUE( p->isUserRead() );
  ASSERT_TRUE( p->isUserWrite() );
  ASSERT_TRUE( p->isGroupRead() );
  ASSERT_TRUE( p->isGroupWrite() );
  ASSERT_TRUE( p->isWorldRead() );
  ASSERT_TRUE( p->isWorldWrite() );

  // All off
  p->setPerm1( 0L );
  ASSERT_TRUE( ! p->isUserRead() );
  ASSERT_TRUE( ! p->isUserWrite() );
  ASSERT_TRUE( ! p->isGroupRead() );
  ASSERT_TRUE( ! p->isGroupWrite() );
  ASSERT_TRUE( ! p->isWorldRead() );
  ASSERT_TRUE( ! p->isWorldWrite() );

  // All on
  p->setPerm1( -1L );
  ASSERT_TRUE( p->isUserRead() );
  ASSERT_TRUE( p->isUserWrite() );
  ASSERT_TRUE( p->isGroupRead() );
  ASSERT_TRUE( p->isGroupWrite() );
  ASSERT_TRUE( p->isWorldRead() );
  ASSERT_TRUE( p->isWorldWrite() );

  // Various swaps
  p->setUserRead(false);
  ASSERT_TRUE( !p->isUserRead() );
  p->setGroupWrite(true);
  ASSERT_TRUE( p->isGroupWrite() );

  // Now reverse each of the above
  p->setUserRead(true);
  ASSERT_TRUE( p->isUserRead() );
  p->setGroupWrite(false);
  ASSERT_TRUE( !p->isGroupWrite() );

}

TEST( PermsTest, invalidCreate ) {
    bool exceptionThrown = false;
    try {
        PermissionsIPtr p = new PermissionsI("r");
    }
    catch (ClientError e) {
        exceptionThrown = true;
    }

    ASSERT_TRUE(exceptionThrown);
}

static void testStringPerms( const char* perms, bool allOn ) {
    PermissionsIPtr p = new PermissionsI(perms);
    ASSERT_EQ(p->isUserRead(), allOn);
    ASSERT_EQ(p->isUserWrite(), allOn);
    ASSERT_EQ(p->isGroupRead(), allOn);
    ASSERT_EQ(p->isGroupWrite(), allOn);
    ASSERT_EQ(p->isWorldRead(), allOn);
    ASSERT_EQ(p->isWorldWrite(), allOn);
}

TEST( PermsTest, createFromString ) {
    testStringPerms("rwrwrw", true);
    testStringPerms("RWRWRW", true);
    testStringPerms("------", false);
}
