/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#include <boost_fixture.h>
#include <omero/sys/ParametersI.h>

using namespace omero::sys;
using namespace std;

BOOST_AUTO_TEST_CASE( AddId )
{
    ParametersIPtr p = new ParametersI();
    cout << " created " << endl;
    p->addId(1L);
    cout << " added " << endl;
}

