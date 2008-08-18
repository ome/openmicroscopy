/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#include <omero/model/ImageI.h>
#include <boost_fixture.h>

using omero::model::ImageI;
using omero::model::ImageIPtr;

BOOST_AUTO_TEST_CASE( EqualityOperatorOnPointers )
{
  Fixture f;
  ImageIPtr img1 = new ImageI();
  ImageIPtr img2 = new ImageI();
  BOOST_CHECK( img1 != img2 );
}

BOOST_AUTO_TEST_CASE( EqualityOperatorOnRawObject )
{
  Fixture f;
  ImageIPtr img1 = new ImageI();
  ImageIPtr img2 = new ImageI();
  // BOOST_CHECK( *img1 != *img2 );
}

