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
// From PojoOptionsTest
//

BOOST_AUTO_TEST_CASE( Basics )
{
    ParametersIPtr p = new ParametersI();
    p->exp(rlong(1));
    p->grp(rlong(1));
    p->endTime(rtime(1));
}

BOOST_AUTO_TEST_CASE( Defaults )
{
    ParametersIPtr p = new ParametersI();
    // Removed: BOOST_CHECK( ! p->isLeaves() );
    BOOST_CHECK( ! p->isGroup() );
    BOOST_CHECK( ! p->isExperimenter() );
    BOOST_CHECK( ! p->isEndTime() );
    BOOST_CHECK( ! p->isStartTime() );
    BOOST_CHECK( ! p->isPagination() );
}

BOOST_AUTO_TEST_CASE( Experimenter )
{
    ParametersIPtr p = new ParametersI();
    p->exp(rlong(1));
    BOOST_CHECK( p->isExperimenter() );
    BOOST_CHECK( 1L == p->getExperimenter()->getValue() );
    p->allExps();
    BOOST_CHECK( 0 == p->getExperimenter() );
    BOOST_CHECK( ! p->isExperimenter() );
}

BOOST_AUTO_TEST_CASE( ExperimenterGroup )
{
    ParametersIPtr p = new ParametersI();
    p->grp(rlong(1));
    BOOST_CHECK( p->isGroup() );
    BOOST_CHECK( 1L == p->getGroup()->getValue() );
    p->allGrps();
    BOOST_CHECK( 0 == p->getGroup() );
}



//
// Parameters.theFilter.limit, offset
//

BOOST_AUTO_TEST_CASE( FilterLimitOffset )
{
    ParametersIPtr p = new ParametersI();
    p->noPage();
    BOOST_CHECK( ! p->theFilter);
    p->page(2,3);
    BOOST_CHECK_EQUAL(2, p->theFilter->offset->getValue());
    BOOST_CHECK_EQUAL(3, p->theFilter->limit->getValue());
    p->noPage();
    BOOST_CHECK( ! p->isPagination() );
    BOOST_CHECK( ! p->theFilter->offset);
    BOOST_CHECK( ! p->theFilter->limit);
    BOOST_CHECK( ! p->getLimit());
    BOOST_CHECK( ! p->getOffset());
}

BOOST_AUTO_TEST_CASE( FilterUnique )
{
    ParametersIPtr p = new ParametersI();
    BOOST_CHECK( 0 == p->getUnique() );
    BOOST_CHECK( p->unique()->getUnique()->getValue() );
    BOOST_CHECK( ! p->noUnique()->getUnique()->getValue() );
}

//
// Parameters.theFilter.ownerId, groupId
//

BOOST_AUTO_TEST_CASE( OwnerId )
{
    ParametersIPtr p = new ParametersI();
    BOOST_CHECK( ! p->theFilter );
    p->exp(rlong(1));
    BOOST_CHECK( p->theFilter );
    BOOST_CHECK( p->theFilter->ownerId );
    BOOST_CHECK( 1 == p->getExperimenter()->getValue() );
    BOOST_CHECK( ! p->allExps()->getExperimenter() );
    BOOST_CHECK( p->theFilter );
}

BOOST_AUTO_TEST_CASE( GroupId )
{
    ParametersIPtr p = new ParametersI();
    BOOST_CHECK( ! p->theFilter );
    p->grp(rlong(1));
    BOOST_CHECK( p->theFilter );
    BOOST_CHECK( p->theFilter->groupId );
    BOOST_CHECK( 1 == p->getGroup()->getValue() );
    BOOST_CHECK( ! p->allGrps()->getGroup() );
    BOOST_CHECK( p->theFilter );
}

//
// Parameters.theFilter.startTime, endTime
//

BOOST_AUTO_TEST_CASE( Times )
{
    ParametersIPtr p = new ParametersI();
    BOOST_CHECK( ! p->theFilter );
    p->startTime(rtime(0));
    BOOST_CHECK( p->theFilter );
    BOOST_CHECK( p->theFilter->startTime );
    p->endTime(rtime(1));
    BOOST_CHECK( p->theFilter->endTime );
    p->allTimes();
    BOOST_CHECK( p->theFilter );
    BOOST_CHECK( ! p->theFilter->startTime );
    BOOST_CHECK( ! p->theFilter->endTime );
}

//
// Parameters.theOptions
//

BOOST_AUTO_TEST_CASE( OptionsAcquisitionData )
{
    ParametersIPtr p = new ParametersI();
    BOOST_CHECK( ! p->getAcquisitionData() );
    BOOST_CHECK( p->acquisitionData()->getAcquisitionData() );
    BOOST_CHECK( p->noAcquisitionData()->getAcquisitionData() );
}

BOOST_AUTO_TEST_CASE( OptionsLeaves )
{
    ParametersIPtr p = new ParametersI();
    BOOST_CHECK( ! p->getLeaves() );
    BOOST_CHECK( p->leaves()->getLeaves() );
    BOOST_CHECK( p->noLeaves()->getLeaves() );
}

BOOST_AUTO_TEST_CASE( OptionsOrphan )
{
    ParametersIPtr p = new ParametersI();
    BOOST_CHECK( ! p->getOrphan() );
    BOOST_CHECK( p->orphan()->getOrphan() );
    BOOST_CHECK( p->noOrphan()->getOrphan() );
}

//
// Parameters.map
//

BOOST_AUTO_TEST_CASE( AddBasicString )
{
    ParametersIPtr p = new ParametersI();
    p->add("string", rstring("a"));
    BOOST_CHECK_EQUAL("a", RStringPtr::dynamicCast(p->map["string"])->getValue());
}

BOOST_AUTO_TEST_CASE( AddBasicInt )
{
    ParametersIPtr p = new ParametersI();
    p->add("int", rint(1));
    BOOST_CHECK_EQUAL(1, RIntPtr::dynamicCast(p->map["int"])->getValue());
}

BOOST_AUTO_TEST_CASE( AddIdRaw )
{
    ParametersIPtr p = new ParametersI();
    p->addId(1);
    BOOST_CHECK_EQUAL(1, RLongPtr::dynamicCast(p->map["id"])->getValue());
}

BOOST_AUTO_TEST_CASE( AddIdRType )
{
    ParametersIPtr p = new ParametersI();
    p->addId(rlong(1));
    BOOST_CHECK_EQUAL(1, RLongPtr::dynamicCast(p->map["id"])->getValue());
}

BOOST_AUTO_TEST_CASE( AddLongRaw )
{
    ParametersIPtr p = new ParametersI();
    p->addLong("long",1L);
    BOOST_CHECK_EQUAL(1, RLongPtr::dynamicCast(p->map["long"])->getValue());
}

BOOST_AUTO_TEST_CASE( AddLongRType )
{
    ParametersIPtr p = new ParametersI();
    p->addLong("long",rlong(1L));
    BOOST_CHECK_EQUAL(1, RLongPtr::dynamicCast(p->map["long"])->getValue());
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

    // Searching is broken because of the definition of equality
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
