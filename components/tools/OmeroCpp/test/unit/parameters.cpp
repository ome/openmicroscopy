/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#include <algorithm>
#include <boost_fixture.h>
#include <omero/sys/ParametersI.h>

using namespace omero::rtypes;
using namespace omero::sys;
using namespace omero;
using namespace std;

//
// Parameters.theFilter
//

BOOST_AUTO_TEST_CASE( FilterTest )
{
    ParametersIPtr p = new ParametersI();
    p->noPage();
    BOOST_CHECK( ! p->theFilter);
    p->page(2,3);
    BOOST_CHECK_EQUAL(rint(2), p->theFilter->offset);
    BOOST_CHECK_EQUAL(rint(3), p->theFilter->limit);
    p->noPage();
    BOOST_CHECK( ! p->theFilter);
}

//
// Parameters.map
//

BOOST_AUTO_TEST_CASE( AddBasicString )
{
    ParametersIPtr p = new ParametersI();
    p->add("string", rstring("a"));
    BOOST_CHECK_EQUAL(rstring("a"), p->map["string"]);
}

BOOST_AUTO_TEST_CASE( AddBasicInt )
{
    ParametersIPtr p = new ParametersI();
    p->add("int", rint(1));
    BOOST_CHECK_EQUAL(rint(1), p->map["int"]);
}

BOOST_AUTO_TEST_CASE( AddIdRaw )
{
    ParametersIPtr p = new ParametersI();
    p->addId(1);
    BOOST_CHECK_EQUAL(rlong(1), p->map["id"]);
}

BOOST_AUTO_TEST_CASE( AddIdRType )
{
    ParametersIPtr p = new ParametersI();
    p->addId(rlong(1));
    BOOST_CHECK_EQUAL(rlong(1), p->map["id"]);
}

BOOST_AUTO_TEST_CASE( AddLongRaw )
{
    ParametersIPtr p = new ParametersI();
    p->addLong("long",1L);
    BOOST_CHECK_EQUAL(rlong(1), p->map["long"]);
}

BOOST_AUTO_TEST_CASE( AddLongRType )
{
    ParametersIPtr p = new ParametersI();
    p->addLong("long",rlong(1L));
    BOOST_CHECK_EQUAL(rlong(1), p->map["long"]);
}

BOOST_AUTO_TEST_CASE( AddIds )
{
    ParametersIPtr p = new ParametersI();
    omero::sys::LongList list;
    list.push_back(1);
    list.push_back(2);
    p->addIds(list);

    RListPtr test = RListPtr::dynamicCast( p->map["ids"] );

    omero::RTypeSeq::iterator found;
    omero::RTypeSeq::iterator beg = test->getValue().begin();
    omero::RTypeSeq::iterator end = test->getValue().end();

    found = find(beg, end, rlong(1));
    BOOST_CHECK( found != end );

    beg = test->getValue().begin();
    found = find(beg, end, rlong(2));
    BOOST_CHECK( found != end );
}

BOOST_AUTO_TEST_CASE( AddLongs )
{
    ParametersIPtr p = new ParametersI();
    omero::sys::LongList list;
    list.push_back(1);
    list.push_back(2);
    p->addLongs("longs", list);

    RListPtr test = RListPtr::dynamicCast( p->map["longs"] );

    omero::RTypeSeq::iterator found;
    omero::RTypeSeq::iterator beg = test->getValue().begin();
    omero::RTypeSeq::iterator end= test->getValue().end();

    found = find(beg, end, rlong(1));
    BOOST_CHECK( found != end );

    beg = test->getValue().begin();
    found = find(beg, end, rlong(2));
    BOOST_CHECK( found != end );
}
