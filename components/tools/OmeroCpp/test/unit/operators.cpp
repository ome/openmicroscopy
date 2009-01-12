/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#include <omero/model/ImageI.h>
#include <boost_fixture.h>

using omero::model::ImagePtr;

BOOST_AUTO_TEST_CASE( EqualityOperatorOnPointers )
{
  Fixture f;
  ImagePtr img1 = new_ImageI();
  ImagePtr img2 = new_ImageI();
  BOOST_CHECK( img1 != img2 );
}

BOOST_AUTO_TEST_CASE( EqualityOperatorOnRawObject )
{
  Fixture f;
  ImagePtr img1 = new_ImageI();
  ImagePtr img2 = new_ImageI();
  // BOOST_CHECK( *img1 != *img2 );
}

