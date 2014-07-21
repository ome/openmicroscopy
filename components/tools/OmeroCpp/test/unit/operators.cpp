/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#include <omero/model/ImageI.h>
#include <omero/fixture.h>

using omero::model::ImagePtr;

TEST(OperatorsTest, EqualityOperatorOnPointers)
{
  ImagePtr img1 = new_ImageI();
  ImagePtr img2 = new_ImageI();
  ASSERT_TRUE( img1 != img2 );
}

TEST(OperatorsTest, EqualityOperatorOnRawObject)
{
  ImagePtr img1 = new_ImageI();
  ImagePtr img2 = new_ImageI();
  // CHECK( *img1 != *img2 );
}

