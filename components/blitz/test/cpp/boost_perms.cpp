/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#include <boost_fixture.h>

using namespace omero::model;
using namespace OMERO;
using namespace omero;
using namespace std;

BOOST_AUTO_TEST_CASE( Permissions )
{
  Fixture f;
  PermissionsIPtr p = new PermissionsI();

  // The default
  BOOST_CHECK( ! p->isUserRead() );
  BOOST_CHECK( ! p->isUserWrite() );
  BOOST_CHECK( ! p->isGroupRead() );
  BOOST_CHECK( ! p->isGroupWrite() );
  BOOST_CHECK( ! p->isWorldRead() );
  BOOST_CHECK( ! p->isWorldWrite() );
  BOOST_CHECK( ! p->isGroupRead() );
  BOOST_CHECK( ! p->isLocked() );
  
  p->setUserRead(false);
  BOOST_CHECK( !p->isUserRead() );
  p->setGroupWrite(true);
  BOOST_CHECK( p->isGroupWrite() );
  p->setLocked(true);
  BOOST_CHECK( p->isLocked() );

  // Now reverse each of the above
  p->setUserRead(true);
  BOOST_CHECK( p->isUserRead() );
  p->setGroupWrite(false);
  BOOST_CHECK( !p->isGroupWrite() );
  p->setLocked(false);
  BOOST_CHECK( !p->isLocked() );

}


