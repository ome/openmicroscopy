/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#include <omero/fixture.h>
#include <iterator>
#include <algorithm>
#include <omero/API.h>
#include <omero/System.h>

using namespace omero::rtypes;

// DISABLED
// cgb taken out the std output

TEST(ApiTest, VectorArgs )
{
  Ice::Long ids[] = {1L, 2L, 3L};
  omero::sys::LongList idList(ids,ids+3);
  //copy(idList.begin(), idList.end(), std::ostream_iterator<Ice::Long>(std::cout, "\n"));
  omero::sys::ParamMap pm;
  pm["user"] = rlong(1L);

  // Need a way to mock
  //omero::api::IPojosPrx pojoPrx();
  //pojoPrx->loadContainerHierarchies
  //("Dataset", idsList, pm);
}
