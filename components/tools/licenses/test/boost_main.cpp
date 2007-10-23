/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

// Use --report_level=detailed for more information

// Don't add this to your test.
// It can only be defined once.
#define BOOST_TEST_MAIN
#define BOOST_TEST_DYN_LINK
#include <boost/test/unit_test.hpp>

#include <omero/client.h>

using namespace omero::model;
using namespace std;

BOOST_AUTO_TEST_CASE( test )
{
  BOOST_CHECK( true );
}

