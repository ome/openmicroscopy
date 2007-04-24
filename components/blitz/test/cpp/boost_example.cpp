/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

// Don't add this to your test.
// It can only be defined once.
#define BOOST_AUTO_TEST_MAIN

#include <boost/test/auto_unit_test.hpp>
#include <OMERO/client.h>
#include <boost_fixture.h>

using namespace omero::model;
using namespace std;

BOOST_AUTO_TEST_CASE( test )
{
  BOOST_CHECK( true );
  // BOOST_ERROR( "Some error" );
}

/* Does not work (yet)
BOOST_AUTO_TEST_CASE( throws ) 
{
  current_test_case().set_expected_failures(std:string);
  throw "whoops";
}
*/
