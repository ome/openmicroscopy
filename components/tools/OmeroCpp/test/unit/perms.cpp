/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#include <omero/model/PermissionsI.h>
#include <omero/fixture.h>

TEST( PermsTest, Perm1 )
{
  Fixture f;
  omero::model::PermissionsIPtr p = new omero::model::PermissionsI();

  // The default
  EXPECT_TRUE( p->isUserRead() );
  EXPECT_TRUE( p->isUserWrite() );
  EXPECT_TRUE( p->isGroupRead() );
  EXPECT_TRUE( p->isGroupWrite() );
  EXPECT_TRUE( p->isWorldRead() );
  EXPECT_TRUE( p->isWorldWrite() );

  // All off
  p->setPerm1( 0L );
  EXPECT_TRUE( ! p->isUserRead() );
  EXPECT_TRUE( ! p->isUserWrite() );
  EXPECT_TRUE( ! p->isGroupRead() );
  EXPECT_TRUE( ! p->isGroupWrite() );
  EXPECT_TRUE( ! p->isWorldRead() );
  EXPECT_TRUE( ! p->isWorldWrite() );
  
  // All on
  p->setPerm1( -1L );
  EXPECT_TRUE( p->isUserRead() );
  EXPECT_TRUE( p->isUserWrite() );
  EXPECT_TRUE( p->isGroupRead() );
  EXPECT_TRUE( p->isGroupWrite() );
  EXPECT_TRUE( p->isWorldRead() );
  EXPECT_TRUE( p->isWorldWrite() );
  
  // Various swaps
  p->setUserRead(false);
  EXPECT_TRUE( !p->isUserRead() );
  p->setGroupWrite(true);
  EXPECT_TRUE( p->isGroupWrite() );

  // Now reverse each of the above
  p->setUserRead(true);
  EXPECT_TRUE( p->isUserRead() );
  p->setGroupWrite(false);
  EXPECT_TRUE( !p->isGroupWrite() );

}
