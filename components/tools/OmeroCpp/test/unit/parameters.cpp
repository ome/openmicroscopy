/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#include <algorithm>
#include <omero/fixture.h>
#include <omero/sys/ParametersI.h>

using namespace omero::rtypes;
using namespace omero::sys;
using namespace omero;
using namespace std;

//
// From PojoOptionsTest
//

TEST( ParametersTest, Basics )
{
    ParametersIPtr p = new ParametersI();
    p->exp(rlong(1));
    p->grp(rlong(1));
    p->endTime(rtime(1));
}

TEST( ParametersTest, Defaults )
{
    ParametersIPtr p = new ParametersI();
    // Removed: ASSERT_TRUE( ! p->isLeaves() );
    ASSERT_TRUE( ! p->isGroup() );
    ASSERT_TRUE( ! p->isExperimenter() );
    ASSERT_TRUE( ! p->isEndTime() );
    ASSERT_TRUE( ! p->isStartTime() );
    ASSERT_TRUE( ! p->isPagination() );
}

TEST( ParametersTest, Experimenter )
{
    ParametersIPtr p = new ParametersI();
    p->exp(rlong(1));
    ASSERT_TRUE( p->isExperimenter() );
    ASSERT_TRUE( 1L == p->getExperimenter()->getValue() );
    p->allExps();
    ASSERT_TRUE( ! p->isExperimenter() );
}

TEST( ParametersTest, ExperimenterGroup )
{
    ParametersIPtr p = new ParametersI();
    p->grp(rlong(1));
    ASSERT_TRUE( p->isGroup() );
    ASSERT_TRUE( 1L == p->getGroup()->getValue() );
    p->allGrps();
    ASSERT_TRUE( !p->getGroup() );
}



//
// Parameters.theFilter.limit, offset
//

TEST( ParametersTest, FilterLimitOffset )
{
    ParametersIPtr p = new ParametersI();
    p->noPage();
    ASSERT_TRUE( ! p->theFilter);
    p->page(2,3);
    ASSERT_EQ(2, p->theFilter->offset->getValue());
    ASSERT_EQ(3, p->theFilter->limit->getValue());
    p->noPage();
    ASSERT_TRUE( ! p->isPagination() );
    ASSERT_TRUE( ! p->theFilter->offset);
    ASSERT_TRUE( ! p->theFilter->limit);
    ASSERT_TRUE( ! p->getLimit());
    ASSERT_TRUE( ! p->getOffset());
}

TEST( ParametersTest, FilterUnique )
{
    ParametersIPtr p = new ParametersI();
    ASSERT_TRUE( !p->getUnique() );
    ASSERT_TRUE( p->unique()->getUnique()->getValue() );
    ASSERT_TRUE( ! p->noUnique()->getUnique()->getValue() );
}

//
// Parameters.theFilter.ownerId, groupId
//

TEST( ParametersTest, OwnerId )
{
    ParametersIPtr p = new ParametersI();
    ASSERT_TRUE( ! p->theFilter );
    p->exp(rlong(1));
    ASSERT_TRUE( p->theFilter );
    ASSERT_TRUE( p->theFilter->ownerId );
    ASSERT_TRUE( 1 == p->getExperimenter()->getValue() );
    ASSERT_TRUE( ! p->allExps()->getExperimenter() );
    ASSERT_TRUE( p->theFilter );
}

TEST( ParametersTest, GroupId )
{
    ParametersIPtr p = new ParametersI();
    ASSERT_TRUE( ! p->theFilter );
    p->grp(rlong(1));
    ASSERT_TRUE( p->theFilter );
    ASSERT_TRUE( p->theFilter->groupId );
    ASSERT_TRUE( 1 == p->getGroup()->getValue() );
    ASSERT_TRUE( ! p->allGrps()->getGroup() );
    ASSERT_TRUE( p->theFilter );
}

//
// Parameters.theFilter.startTime, endTime
//

TEST( ParametersTest, Times )
{
    ParametersIPtr p = new ParametersI();
    ASSERT_TRUE( ! p->theFilter );
    p->startTime(rtime(0));
    ASSERT_TRUE( p->theFilter );
    ASSERT_TRUE( p->theFilter->startTime );
    p->endTime(rtime(1));
    ASSERT_TRUE( p->theFilter->endTime );
    p->allTimes();
    ASSERT_TRUE( p->theFilter );
    ASSERT_TRUE( ! p->theFilter->startTime );
    ASSERT_TRUE( ! p->theFilter->endTime );
}

//
// Parameters.theOptions
//

TEST( ParametersTest, OptionsAcquisitionData )
{
    ParametersIPtr p = new ParametersI();
    ASSERT_TRUE( ! p->getAcquisitionData() );
    ASSERT_TRUE( p->acquisitionData()->getAcquisitionData() );
    ASSERT_TRUE( p->noAcquisitionData()->getAcquisitionData() );
}

TEST( ParametersTest, OptionsLeaves )
{
    ParametersIPtr p = new ParametersI();
    ASSERT_TRUE( ! p->getLeaves() );
    ASSERT_TRUE( p->leaves()->getLeaves() );
    ASSERT_TRUE( p->noLeaves()->getLeaves() );
}

TEST( ParametersTest, OptionsOrphan )
{
    ParametersIPtr p = new ParametersI();
    ASSERT_TRUE( ! p->getOrphan() );
    ASSERT_TRUE( p->orphan()->getOrphan() );
    ASSERT_TRUE( p->noOrphan()->getOrphan() );
}

//
// Parameters.map
//

TEST( ParametersTest, AddBasicString )
{
    ParametersIPtr p = new ParametersI();
    p->add("string", rstring("a"));
    ASSERT_EQ("a", RStringPtr::dynamicCast(p->map["string"])->getValue());
}

TEST( ParametersTest, AddBasicInt )
{
    ParametersIPtr p = new ParametersI();
    p->add("int", rint(1));
    ASSERT_EQ(1, RIntPtr::dynamicCast(p->map["int"])->getValue());
}

TEST( ParametersTest, AddIdRaw )
{
    ParametersIPtr p = new ParametersI();
    p->addId(1);
    ASSERT_EQ(1, RLongPtr::dynamicCast(p->map["id"])->getValue());
}

TEST( ParametersTest, AddIdRType )
{
    ParametersIPtr p = new ParametersI();
    p->addId(rlong(1));
    ASSERT_EQ(1, RLongPtr::dynamicCast(p->map["id"])->getValue());
}

TEST( ParametersTest, AddLongRaw )
{
    ParametersIPtr p = new ParametersI();
    p->addLong("long",1L);
    ASSERT_EQ(1, RLongPtr::dynamicCast(p->map["long"])->getValue());
}

TEST( ParametersTest, AddLongRType )
{
    ParametersIPtr p = new ParametersI();
    p->addLong("long",rlong(1L));
    ASSERT_EQ(1, RLongPtr::dynamicCast(p->map["long"])->getValue());
}

static void find(long i, omero::RListPtr test) {

    ASSERT_TRUE(test);
    omero::RTypeSeq seq = test->getValue();

    omero::RTypeSeq::iterator found;
    omero::RTypeSeq::iterator beg = seq.begin();
    omero::RTypeSeq::iterator end = seq.end();

    int count = 0;
    for (;beg!=end;beg++) {
        omero::RTypePtr t = *beg;
        omero::RLongPtr l = omero::RLongPtr::dynamicCast(t);
        if (l->getValue() == i) count++;
    }
    ASSERT_NE(0, count);
}

TEST( ParametersTest, AddIds )
{
    ParametersIPtr p = new ParametersI();
    omero::sys::LongList list;
    list.push_back(1);
    list.push_back(2);
    p->addIds(list);
    RListPtr test = RListPtr::dynamicCast( p->map["ids"] );
    find(1, test);
    find(2, test);
}

TEST( ParametersTest, AddLongs )
{
    ParametersIPtr p = new ParametersI();
    omero::sys::LongList list;
    list.push_back(1);
    list.push_back(2);
    p->addLongs("longs", list);
    RListPtr test = RListPtr::dynamicCast( p->map["longs"] );
    find(1, test);
    find(2, test);
}
