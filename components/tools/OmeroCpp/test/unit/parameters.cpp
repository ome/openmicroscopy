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
    // Removed: EXPECT_TRUE( ! p->isLeaves() );
    EXPECT_TRUE( ! p->isGroup() );
    EXPECT_TRUE( ! p->isExperimenter() );
    EXPECT_TRUE( ! p->isEndTime() );
    EXPECT_TRUE( ! p->isStartTime() );
    EXPECT_TRUE( ! p->isPagination() );
}

TEST( ParametersTest, Experimenter )
{
    ParametersIPtr p = new ParametersI();
    p->exp(rlong(1));
    EXPECT_TRUE( p->isExperimenter() );
    EXPECT_TRUE( 1L == p->getExperimenter()->getValue() );
    p->allExps();
    EXPECT_TRUE( 0 == p->getExperimenter() );
    EXPECT_TRUE( ! p->isExperimenter() );
}

TEST( ParametersTest, ExperimenterGroup )
{
    ParametersIPtr p = new ParametersI();
    p->grp(rlong(1));
    EXPECT_TRUE( p->isGroup() );
    EXPECT_TRUE( 1L == p->getGroup()->getValue() );
    p->allGrps();
    EXPECT_TRUE( 0 == p->getGroup() );
}



//
// Parameters.theFilter.limit, offset
//

TEST( ParametersTest, FilterLimitOffset )
{
    ParametersIPtr p = new ParametersI();
    p->noPage();
    EXPECT_TRUE( ! p->theFilter);
    p->page(2,3);
    EXPECT_EQ(2, p->theFilter->offset->getValue());
    EXPECT_EQ(3, p->theFilter->limit->getValue());
    p->noPage();
    EXPECT_TRUE( ! p->isPagination() );
    EXPECT_TRUE( ! p->theFilter->offset);
    EXPECT_TRUE( ! p->theFilter->limit);
    EXPECT_TRUE( ! p->getLimit());
    EXPECT_TRUE( ! p->getOffset());
}

TEST( ParametersTest, FilterUnique )
{
    ParametersIPtr p = new ParametersI();
    EXPECT_TRUE( 0 == p->getUnique() );
    EXPECT_TRUE( p->unique()->getUnique()->getValue() );
    EXPECT_TRUE( ! p->noUnique()->getUnique()->getValue() );
}

//
// Parameters.theFilter.ownerId, groupId
//

TEST( ParametersTest, OwnerId )
{
    ParametersIPtr p = new ParametersI();
    EXPECT_TRUE( ! p->theFilter );
    p->exp(rlong(1));
    EXPECT_TRUE( p->theFilter );
    EXPECT_TRUE( p->theFilter->ownerId );
    EXPECT_TRUE( 1 == p->getExperimenter()->getValue() );
    EXPECT_TRUE( ! p->allExps()->getExperimenter() );
    EXPECT_TRUE( p->theFilter );
}

TEST( ParametersTest, GroupId )
{
    ParametersIPtr p = new ParametersI();
    EXPECT_TRUE( ! p->theFilter );
    p->grp(rlong(1));
    EXPECT_TRUE( p->theFilter );
    EXPECT_TRUE( p->theFilter->groupId );
    EXPECT_TRUE( 1 == p->getGroup()->getValue() );
    EXPECT_TRUE( ! p->allGrps()->getGroup() );
    EXPECT_TRUE( p->theFilter );
}

//
// Parameters.theFilter.startTime, endTime
//

TEST( ParametersTest, Times )
{
    ParametersIPtr p = new ParametersI();
    EXPECT_TRUE( ! p->theFilter );
    p->startTime(rtime(0));
    EXPECT_TRUE( p->theFilter );
    EXPECT_TRUE( p->theFilter->startTime );
    p->endTime(rtime(1));
    EXPECT_TRUE( p->theFilter->endTime );
    p->allTimes();
    EXPECT_TRUE( p->theFilter );
    EXPECT_TRUE( ! p->theFilter->startTime );
    EXPECT_TRUE( ! p->theFilter->endTime );
}

//
// Parameters.theOptions
//

TEST( ParametersTest, OptionsAcquisitionData )
{
    ParametersIPtr p = new ParametersI();
    EXPECT_TRUE( ! p->getAcquisitionData() );
    EXPECT_TRUE( p->acquisitionData()->getAcquisitionData() );
    EXPECT_TRUE( p->noAcquisitionData()->getAcquisitionData() );
}

TEST( ParametersTest, OptionsLeaves )
{
    ParametersIPtr p = new ParametersI();
    EXPECT_TRUE( ! p->getLeaves() );
    EXPECT_TRUE( p->leaves()->getLeaves() );
    EXPECT_TRUE( p->noLeaves()->getLeaves() );
}

TEST( ParametersTest, OptionsOrphan )
{
    ParametersIPtr p = new ParametersI();
    EXPECT_TRUE( ! p->getOrphan() );
    EXPECT_TRUE( p->orphan()->getOrphan() );
    EXPECT_TRUE( p->noOrphan()->getOrphan() );
}

//
// Parameters.map
//

TEST( ParametersTest, AddBasicString )
{
    ParametersIPtr p = new ParametersI();
    p->add("string", rstring("a"));
    EXPECT_EQ("a", RStringPtr::dynamicCast(p->map["string"])->getValue());
}

TEST( ParametersTest, AddBasicInt )
{
    ParametersIPtr p = new ParametersI();
    p->add("int", rint(1));
    EXPECT_EQ(1, RIntPtr::dynamicCast(p->map["int"])->getValue());
}

TEST( ParametersTest, AddIdRaw )
{
    ParametersIPtr p = new ParametersI();
    p->addId(1);
    EXPECT_EQ(1, RLongPtr::dynamicCast(p->map["id"])->getValue());
}

TEST( ParametersTest, AddIdRType )
{
    ParametersIPtr p = new ParametersI();
    p->addId(rlong(1));
    EXPECT_EQ(1, RLongPtr::dynamicCast(p->map["id"])->getValue());
}

TEST( ParametersTest, AddLongRaw )
{
    ParametersIPtr p = new ParametersI();
    p->addLong("long",1L);
    EXPECT_EQ(1, RLongPtr::dynamicCast(p->map["long"])->getValue());
}

TEST( ParametersTest, AddLongRType )
{
    ParametersIPtr p = new ParametersI();
    p->addLong("long",rlong(1L));
    EXPECT_EQ(1, RLongPtr::dynamicCast(p->map["long"])->getValue());
}

void find(long i, omero::RListPtr test) {

    EXPECT_TRUE(test);
    omero::RTypeSeq seq = test->getValue();

    omero::RTypeSeq::iterator found;
    omero::RTypeSeq::iterator beg = seq.begin();
    omero::RTypeSeq::iterator end = seq.end();

    int count = 0;
    for (;beg!=end;beg++) {
        count++;
	omero::RTypePtr t = *beg;
    }
    EXPECT_NE(0, count);
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
