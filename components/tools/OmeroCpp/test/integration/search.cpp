/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */
#include <IceUtil/UUID.h>
#include <Glacier2/Glacier2.h>
#include <omero/fixture.h>
#include <time.h>
#include <omero/Collections.h>
#include <omero/api/IAdmin.h>
#include <omero/model/ImageI.h>
#include <omero/model/ImageAnnotationLinkI.h>
#include <omero/model/BooleanAnnotationI.h>
#include <omero/model/DoubleAnnotationI.h>
#include <omero/model/FileAnnotationI.h>
#include <omero/model/LongAnnotationI.h>
#include <omero/model/OriginalFileI.h>
#include <omero/model/TagAnnotationI.h>
#include <omero/model/CommentAnnotationI.h>
#include <omero/model/ExperimenterGroupI.h>
#include <omero/model/ExperimenterI.h>

using namespace std;
using namespace omero;
using namespace omero::api;
using namespace omero::model;
using namespace omero::rtypes;
using namespace omero::sys;

void byAnnotatedWith(SearchPrx search, AnnotationPtr a) {
    omero::api::AnnotationList list;
    list.push_back(a);
    search->byAnnotatedWith(list);
}
omero::sys::LongList ids(long id){
    omero::sys::LongList ll;
    ll.push_back(id);
    return ll;
}
omero::sys::LongList ids(long id, long id2){
    omero::sys::LongList ll = ids(id);
    ll.push_back(id2);
    return ll;
}
StringSet stringSet(string s) {
    StringSet ss;
    ss.push_back(s);
    return ss;
}
StringSet stringSet(string s, string s2){
    StringSet ss = stringSet(s);
    ss.push_back(s2);
    return ss;
}

StringSet stringSet(string s, string s2, string s3){
    StringSet ss = stringSet(s, s2);
    ss.push_back(s3);
    return ss;
}

class SearchFixture : virtual public Fixture {
public:
    ExperimenterGroupPtr group() {
        return new ExperimenterGroupI(admin()->getEventContext()->groupId, false);
    }
    ServiceFactoryPrx sf() {
        return client->getSession();
    }
    SearchPrx search() {
        return sf()->createSearchService();
    }
    SearchPrx rootSearch() {
        return root->getSession()->createSearchService();
    }
    IAdminPrx admin() {
        return sf()->getAdminService();
    }
    IQueryPrx query() {
        return sf()->getQueryService();
    }
    IUpdatePrx update() {
        return sf()->getUpdateService();
    }
    IAdminPrx rootAdmin() {
        return root->getSession()->getAdminService();
    }
    IUpdatePrx rootUpdate() {
        return root->getSession()->getUpdateService();
    }
    OriginalFileIPtr createFile() {
        OriginalFileIPtr file = new OriginalFileI();
        file->setSize(rlong(0));
        file->setName(rstring(""));
        file->setPath(rstring("/"));
        file->setHash(rstring(""));
        return file;
    }
};

/*
 * Clears one result from the current queue.
 */
void
assertResults(int count, SearchPrx& search, bool exact = true)
{
    if (count > 0) {
        ASSERT_TRUE( search->hasNext() );
        if (exact) {
            ASSERT_EQ(static_cast<unsigned int>(count), search->results().size());
        } else {
            ASSERT_GE(search->results().size(), static_cast<unsigned int>(count));
        }
    } else {
        if (search->hasNext()) {
            ASSERT_EQ(0U, search->results().size());
        }
    }
}

void
assertAtLeastResults(int count, SearchPrx& search)
{
  assertResults(count, search, false);
}

TEST(SearchTest, RootSearch )
{
    try {
        SearchFixture f;
        f.login();

        SearchPrx search = f.search();
        search->onlyType("Experimenter");
        search->byFullText("root");
        if (search->hasNext()) {
            ExperimenterIPtr e = ExperimenterIPtr::dynamicCast(search->next());
        }
    } catch (const omero::InternalException& ie) {
        FAIL() << "internal exception:"+ie.message;
    } catch (const omero::ApiUsageException& aue) {
        FAIL() << "api usage exception thrown:" << aue.message;
    } catch (const Ice::UnknownException& ue) {
        cout << ue << endl;
        FAIL() << "unknown exception thrown";
    }
}

TEST(SearchTest, IQuerySearch )
{
    SearchFixture f;
    f.login();

    IUpdatePrx update = f.update();

    string uuid = f.uuid();
    ImagePtr i = new_ImageI();
    i->setName(rstring(uuid));
    i = ImagePtr::dynamicCast( update->saveAndReturnObject(i) );

    f.rootUpdate()->indexObject(i);

    // IQuery provides a simple, stateless method for search
    IObjectList list;
    list = f.query()->findAllByFullText("Image",uuid,0);
    ASSERT_EQ(1U, list.size());
}


TEST(SearchTest, Filtering )
{
    try {
        SearchFixture f;
        f.login();

        string uuid = f.uuid();
        ImagePtr i = new_ImageI();
        i->setName( rstring(uuid) );

        IObjectPtr obj =  f.update()->saveAndReturnObject(i);
        f.rootUpdate()->indexObject(obj);

        SearchPrx search = f.search();
        search->onlyType("Image");

        // Search without filter
        search->byFullText(uuid);
        assertResults(1, search);

        // Add id filter
        omero::sys::LongList ids;
        ids.push_back(obj->getId()->getValue());
        search->onlyIds(ids);
        search->byFullText(uuid);
        assertResults(1, search);

        // Add failing id filter
        ids.clear();
        ids.push_back(-1L);
        search->onlyIds(ids);
        search->byFullText(uuid);
        assertResults(0, search);

        // Reset for coming searches
        ids.clear();
        ids.push_back(-1L);
        search->onlyIds(ids);

        // Add user filter
        DetailsIPtr rootonly = new DetailsI();
        rootonly->setOwner( new ExperimenterI(0L, false) );
        search->onlyOwnedBy( rootonly );
        search->byFullText( uuid );
        assertResults(0, search);

        // Reset for coming searches
        search->onlyOwnedBy(DetailsIPtr());

    } catch (const omero::InternalException& ie) {
        FAIL() << "internal exception:"+ie.message;
    } catch (const omero::ApiUsageException& aue) {
        FAIL() << "api usage exception thrown:" << aue.message;
    } catch (const Ice::UnknownException& ue) {
        cout << ue << endl;
        FAIL() << "unknown exception thrown";
    }
}

// ===============================================================
// Below this point is an exact copy of SearchTest.java from r2265
// ===============================================================

// by<Query>
// =========================================================================
// This section tests each query method with various combinations of
// restrictions

TEST( SearchTest, testByGroupForTags ) {
    try {
    // Set up user and group
    SearchFixture f;

    ExperimenterPtr initialUser = f.newUser();
    f.login(initialUser->getOmeName()->getValue());
    ExperimenterPtr secondUser = f.newUser(f.group());
    ExperimenterGroupPtr initialGroup = f.group();

    SearchFixture f2;
    f2.login(secondUser->getOmeName()->getValue());

    PermissionsPtr perms = new PermissionsI();
    perms->setGroupRead(true);
    perms->setGroupWrite(true);
    perms->setWorldRead(false);
    perms->setWorldWrite(false);
    f.rootAdmin()->changePermissions(initialGroup, perms);

    string groupStr = f.uuid();
    string tagStr = f.uuid();

    TagAnnotationIPtr tag = new TagAnnotationI();
    tag->setTextValue(rstring(tagStr));

    TagAnnotationIPtr grp = new TagAnnotationI();
    grp->setTextValue(rstring(groupStr));

    tag->linkAnnotation(grp);
    tag = TagAnnotationIPtr::dynamicCast(f.update()->saveAndReturnObject(tag));

    SearchPrx search = f.search();
    search->byGroupForTags(groupStr);
    assertResults(1, search);

    // Make another one
    groupStr = f.uuid();
    grp = new TagAnnotationI();
    grp->setTextValue(rstring(groupStr));
    tag->linkAnnotation(grp);
    tag = TagAnnotationIPtr::dynamicCast(f.update()->saveAndReturnObject(tag));

    // Now we are sure that there are two taggroups in the db;
    // this should return all two then
    search->byGroupForTags(string()); // ERROR Need to pass null
    search->setBatchSize(2);
    assertResults(2, search);
    while (search->hasNext()) {
        search->results(); // Clear search
    }

    // Let's now add the tag to another tag group as another user
    // and try to filter out those results

    grp = new TagAnnotationI();
    groupStr = f2.uuid();
    grp->setTextValue(rstring(groupStr));
    tag->linkAnnotation(grp);
    tag = TagAnnotationIPtr::dynamicCast(f.update()->saveAndReturnObject(tag));

    // All queries finished?
    ASSERT_EQ(0, search->activeQueries());
    assertResults(0, search);

    DetailsIPtr d = new DetailsI();
    d->setOwner(new ExperimenterI(secondUser->getId(), false));
    search->onlyOwnedBy(d);
    search->byGroupForTags(groupStr);
    ASSERT_FALSE( search->hasNext() );

    d->setOwner(initialUser);
    search->onlyOwnedBy(d);
    search->byGroupForTags(groupStr);
    assertResults(1, search);

    search->onlyOwnedBy(DetailsIPtr());
    search->byGroupForTags(groupStr);
    assertResults(1, search);
    } catch (const omero::InternalException& ie) {
        FAIL() << "internal exception:"+ie.message;
    } catch (const omero::ApiUsageException& aue) {
        FAIL() << "api usage exception thrown:" << aue.message;
    } catch (const Ice::UnknownException& ue) {
        cout << ue << endl;
        FAIL() << "unknown exception thrown";
    }
}

TEST(SearchTest, testByTagForGroup ) {
    try {
    // Set up user and group
    SearchFixture f;
    ExperimenterPtr initialUser = f.newUser();

    f.login(initialUser->getOmeName()->getValue());
    ExperimenterPtr secondUser = f.newUser(f.group());
    ExperimenterGroupPtr initialGroup = f.group();

    SearchFixture f2;
    f2.login(secondUser->getOmeName()->getValue());

    PermissionsPtr perms = new PermissionsI();
    perms->setGroupRead(true);
    perms->setGroupWrite(true);
    perms->setWorldRead(false);
    perms->setWorldWrite(false);
    f.rootAdmin()->changePermissions(initialGroup, perms);

    string groupStr = f.uuid();
    string tagStr = f.uuid();

    TagAnnotationIPtr tag = new TagAnnotationI();
    tag->setTextValue(rstring(tagStr));

    TagAnnotationIPtr grp = new TagAnnotationI();
    grp->setTextValue(rstring(groupStr));

    tag->linkAnnotation(grp);
    tag = TagAnnotationIPtr::dynamicCast(f.update()->saveAndReturnObject(tag));

    SearchPrx search = f.search();
    search->byTagForGroups(tagStr);
    assertResults(1, search);

    // Make another one
    tagStr = f.uuid();
    tag = new TagAnnotationI();
    tag->linkAnnotation(grp);
    tag = TagAnnotationIPtr::dynamicCast(f.update()->saveAndReturnObject(tag));

    // Now we are sure that there are two tags for the one group;
    // this should return all two then
    search->byTagForGroups(string()); // ERROR
    search->setBatchSize(2);
    assertResults(2, search);
    while (search->hasNext()) {
        search->results(); // Clear search
    }

    // Let's now add another tag to the tag group as another user
    // and try to filter out those results

    tag = new TagAnnotationI();
    tagStr = f2.uuid();
    tag->setTextValue(rstring(tagStr));
    tag->linkAnnotation(grp);
    tag = TagAnnotationIPtr::dynamicCast(f2.update()->saveAndReturnObject(tag));

    // All queries finished?
    ASSERT_EQ(0, search->activeQueries());
    assertResults(0, search);

    DetailsIPtr d = new DetailsI();
    d->setOwner(new ExperimenterI(initialUser->getId(), false));
    search->onlyOwnedBy(d);
    search->byTagForGroups(tagStr);
    assertResults(0, search);

    // ticket:2067
    d->setOwner(new ExperimenterI(secondUser->getId(), false));
    search->onlyOwnedBy(d);
    search->byTagForGroups(tagStr);
    assertResults(1, search);

    // ticket:2067
    search->onlyOwnedBy(DetailsIPtr());
    search->byTagForGroups(tagStr);
    assertResults(1, search);
    } catch (const omero::InternalException& ie) {
        FAIL() << "internal exception:"+ie.message;
    } catch (const omero::ApiUsageException& aue) {
        FAIL() << "api usage exception thrown:" << aue.message;
    } catch (const Ice::UnknownException& ue) {
        cout << ue << endl;
        FAIL() << "unknown exception thrown";
    }
}

TEST(SearchTest, testSimpleFullTextSearch ) {

    try {
    SearchFixture f;
    f.login();

    ImagePtr i = new_ImageI();
    i->setName(rstring(f.uuid()));
    i = ImagePtr::dynamicCast(f.update()->saveAndReturnObject(i));

    f.rootUpdate()->indexObject(i);

    SearchPrx search = f.search();
    search->onlyType("Image");
    search->byFullText(i->getName()->getValue());
    int count = 0;
    IObjectPtr obj;
    while (search->hasNext()) {
        obj = search->next();
        count++;
        ASSERT_TRUE( obj );
    }
    ASSERT_EQ(1, count);

    search->onlyType("Image");
    search->byFullText(i->getName()->getValue());
    assertResults(1, search);

    search->close();
    } catch (const omero::InternalException& ie) {
        FAIL() << "internal exception:"+ie.message;
    } catch (const omero::ApiUsageException& aue) {
        FAIL() << "api usage exception thrown:" << aue.message;
    } catch (const Ice::UnknownException& ue) {
        cout << ue << endl;
        FAIL() << "unknown exception thrown";
    }
}


namespace
{
    vector<string> sa(const char* s1 = 0, const char* s2 = 0) {
        vector<string> v;
        if (s1)
            v.push_back(s1);
        if (s2)
            v.push_back(s2);
    
        return v;
    }
}

TEST(SearchTest, testSomeMustNone ) {
    SearchFixture f;
    f.login();

    ImagePtr i = new_ImageI();
    i->setName(rstring("abc def ghi"));
    i = ImagePtr::dynamicCast(f.update()->saveAndReturnObject(i));
    f.rootUpdate()->indexObject(i);

    SearchPrx search = f.search();
    search->onlyType("Image");

    // Make sure we can find it simply
    search->bySomeMustNone(sa("abc"), sa(), sa());
    ASSERT_GE(search->results().size(), 1);

    //
    // Now we'll try more complicated queries
    //

    // This should return nothing since none is contained
    search->bySomeMustNone(sa("abc"), sa(), sa("def"));
    assertResults(0, search);

    // but if the none is not contained should be ok.
    search->bySomeMustNone(sa("abc"), sa("abc"), sa("jkl"));
    assertAtLeastResults(1, search);

    // Simple must query
    search->bySomeMustNone(sa(), sa("abc"), sa());
    assertAtLeastResults(1, search);

    // same, but with a matching none
    search->bySomeMustNone(sa(), sa("abc"), sa("def"));
    assertResults(0, search);

    // same again, but with non-matching none
    search->bySomeMustNone(sa(), sa("abc"), sa("jkl"));
    assertAtLeastResults(1, search);

    //
    // Mixing some and must
    //

    // Present must
    search->bySomeMustNone(sa("abc"), sa("def"), sa());
    assertAtLeastResults(1, search);

    // Missing must
    search->bySomeMustNone(sa("abc"), sa("jkl"), sa());
    assertResults(0, search);

    // Present must, missing some
    search->bySomeMustNone(sa("jkl"), sa("def"), sa());
    assertAtLeastResults(1, search);

    //
    // Using wildcards
    //

    // some with wildcard
    search->bySomeMustNone(sa("ab*"), sa(), sa());
    assertAtLeastResults(1, search);

    // must with wildcard
    search->bySomeMustNone(sa(), sa("ab*"), sa());
    assertAtLeastResults(1, search);

    // none with wildcard
    search->bySomeMustNone(sa(), sa(), sa("ab*"));
    assertResults(0, search);

    //
    // Multiterms
    //

    search->bySomeMustNone(sa("abc", "def"), sa(), sa());
    assertAtLeastResults(1, search);

    search->bySomeMustNone(sa(), sa("abc", "def"), sa());
    assertAtLeastResults(1, search);

    search->bySomeMustNone(sa(), sa(), sa("abc", "def"));
    assertResults(0, search);

    search->bySomeMustNone(sa("ghi", "123"), sa("abc", "def"), sa());
    assertAtLeastResults(1, search);

    search->bySomeMustNone(sa("ghi", "123"), sa("abc", "def"), sa("456"));
    assertAtLeastResults(1, search);

    search->bySomeMustNone(sa("ghi", "123"), sa("abc", "456"), sa("456"));
    assertResults(0, search);

    //
    // Completely empty
    //
    try {
        search->bySomeMustNone(sa(), sa(), sa());
        FAIL() << "Should throw";
    } catch (ApiUsageException aue) {
        // ok
    }

    try {
        search->bySomeMustNone(sa(), sa(), sa());
        FAIL() << "Should throw";
    } catch (ApiUsageException aue) {
        // ok
    }

    try {
        search->bySomeMustNone(sa(""), sa(), sa());
        FAIL() << "Should throw";
    } catch (ApiUsageException aue) {
        // ok
    }

    //
    // Queries with spaces
    // For the moment these return as expected since the parser splits into
    // keywords.
    //
    search->bySomeMustNone(sa("\"abc def\""), sa(), sa());
    assertAtLeastResults(1, search);
}


TEST(SearchTest, testAnnotatedWith ) {
    try {
    SearchFixture f;
    f.login();

    string uuid = f.uuid();;
    ImagePtr i = new_ImageI();
    i->setName(rstring(uuid));
    TagAnnotationIPtr tag = new TagAnnotationI();
    tag->setTextValue(rstring(uuid));
    i->linkAnnotation(tag);
    i = ImagePtr::dynamicCast(f.update()->saveAndReturnObject(i));
    f.rootUpdate()->indexObject(i);

    SearchPrx search = f.search();
    search->onlyType("Image");
    TagAnnotationIPtr example = new TagAnnotationI();
    example->setTextValue(rstring(uuid));
    byAnnotatedWith(search, example);

    assertResults(1, search);

    OriginalFileIPtr file1 = f.createFile();
    file1 = OriginalFileIPtr::dynamicCast(f.update()->saveAndReturnObject(file1));
    OriginalFileIPtr file2 = f.createFile();
    file2 = OriginalFileIPtr::dynamicCast(f.update()->saveAndReturnObject(file2));
    FileAnnotationIPtr fa1 = new FileAnnotationI();
    fa1->setFile(file1);
    i->linkAnnotation(fa1);
    FileAnnotationIPtr fa2 = new FileAnnotationI();
    fa2->setFile(file2);
    i->linkAnnotation(fa2);
    i =  ImagePtr::dynamicCast(f.update()->saveAndReturnObject(i));
    f.rootUpdate()->indexObject(i);

    // Properly uses the id
    FileAnnotationIPtr ex2 = new FileAnnotationI();
    ex2->setFile(new OriginalFileI(file2->getId(), false));
    byAnnotatedWith(search, ex2);
    assertResults(1, search);

    // Now check if an empty example return results
    byAnnotatedWith(search, new FileAnnotationI());
    assertAtLeastResults(1, search);

    // Finding by superclass
    CommentAnnotationIPtr txtAnn = new CommentAnnotationI();
    txtAnn->setTextValue(rstring(uuid));
    byAnnotatedWith(search, txtAnn);
    assertResults(1, search);
    } catch (const omero::InternalException& ie) {
        FAIL() << "internal exception:"+ie.message;
    } catch (const omero::ApiUsageException& aue) {
        FAIL() << "api usage exception thrown:" << aue.message;
    } catch (const Ice::UnknownException& ue) {
        cout << ue << endl;
        FAIL() << "unknown exception thrown";
    }
}

TEST(SearchTest, testAnnotatedWithMultiple ) {
    try {
    ImagePtr i1 = new_ImageI();
    i1->setName( rstring("i1") );
    ImagePtr i2 = new_ImageI();
    i2->setName( rstring("i2") );

    SearchFixture f;
    f.login();

    string uuid = f.uuid();;
    TagAnnotationIPtr ta = new TagAnnotationI();
    ta->setTextValue(rstring(uuid));
    BooleanAnnotationIPtr ba = new BooleanAnnotationI();
    ba->setBoolValue(rbool(false));
    i1->linkAnnotation(ta);
    i2->linkAnnotation(ta);
    i2->linkAnnotation(ba);

    i1 = ImagePtr::dynamicCast(f.update()->saveAndReturnObject(i1));
    i2 = ImagePtr::dynamicCast(f.update()->saveAndReturnObject(i2));

    ta = new TagAnnotationI();
    ta->setTextValue(rstring(uuid));
    ba = new BooleanAnnotationI();
    ba->setBoolValue(rbool(false));

    SearchPrx search = f.search();
    search->onlyType("Image");

    byAnnotatedWith(search, ta);
    assertResults(2, search);

    byAnnotatedWith(search, ba);
    assertAtLeastResults(1, search);

    omero::api::AnnotationList list;
    list.push_back(ta);
    list.push_back(ba);
    search->byAnnotatedWith(list);
    assertResults(1, search);
    } catch (const omero::InternalException& ie) {
        FAIL() << "internal exception:" << ie.message;
    } catch (const omero::ApiUsageException& aue) {
        FAIL() << "api usage exception thrown:" << aue.message;
    } catch (const Ice::UnknownException& ue) {
        cout << ue << endl;
        FAIL() << "unknown exception thrown";
    }

}

// restrictions methods
// ========================================================================
// The tests in the following sections should include all the by* methods
// each testing a specific restriction

TEST(SearchTest, testOnlyIds ) {

    // ignored by
    // byTagForGroups, byGroupForTags

    SearchFixture f;
    f.login();

    string uuid = f.uuid();;
    ImagePtr i1 = new_ImageI();
    i1->setName( rstring(uuid) );
    ImagePtr i2 = new_ImageI();
    i2->setName(rstring(uuid));
    TagAnnotationIPtr tag = new TagAnnotationI();
    tag->setTextValue(rstring(uuid));
    i1->linkAnnotation(tag);
    i2->linkAnnotation(tag);
    i1 = ImagePtr::dynamicCast(f.update()->saveAndReturnObject(i1));
    i2 = ImagePtr::dynamicCast(f.update()->saveAndReturnObject(i2));
    tag = new TagAnnotationI();
    tag->setTextValue(rstring(uuid));
    f.rootUpdate()->indexObject(i1);
    f.rootUpdate()->indexObject(i2);

    SearchPrx search = f.search();
    search->onlyType("Image");

    // Regular search
    // full text
    search->byFullText(uuid);
    assertResults(2, search);
    // annotated with
    byAnnotatedWith(search, tag);
    assertResults(2, search);

    // Restrict to one id
    search->onlyIds(ids(i1->getId()->getValue()));
    // full text
    search->byFullText(uuid);
    assertResults(1, search);
    // annotated with
    byAnnotatedWith(search, tag);
    assertResults(1, search);

    // Restrict to both ids
    search->onlyIds(ids(i1->getId()->getValue(), i2->getId()->getValue()));
    // full text
    search->byFullText(uuid);
    assertResults(2, search);
    // annotated with
    byAnnotatedWith(search, tag);
    assertResults(2, search);

    // Restrict to unknown ids
    search->onlyIds(ids(-1L, -2L));
    // full text
    search->byFullText(uuid);
    assertResults(0, search);
    // annotated with
    byAnnotatedWith(search, tag);
    assertResults(0, search);

    // FIXME: No Ice null collections for C++. (Review for ticket:2067)
    // Tue Aug 10 09:07:50 BST 2010 -- Chris Allan <callan@blackcat.ca>
    // unrestrict
    //search->onlyIds(omero::sys::LongList());
    // full text
    //search->byFullText(uuid);
    //assertResults(2, search);
    // annotated with
    //byAnnotatedWith(search, tag);
    //assertResults(2, search);
}

TEST(SearchTest, testOnlyOwnedByOwner ) {

    SearchFixture f;

    ExperimenterPtr e = f.newUser();
    f.login(e->getOmeName()->getValue());

    DetailsIPtr user = new DetailsI();
    user->setOwner(e);

    string name = f.uuid();;
    ImagePtr i = new_ImageI();
    i->setName( rstring(name) );
    TagAnnotationIPtr tag = new TagAnnotationI();
    tag->setTextValue(rstring(name));
    i->linkAnnotation(tag);
    TagAnnotationIPtr grp = new TagAnnotationI();
    grp->setTextValue(rstring(name));
    tag->linkAnnotation(grp);
    i = ImagePtr::dynamicCast(f.update()->saveAndReturnObject(i));
    // Recreating instance as example
    tag = new TagAnnotationI();
    tag->setTextValue(rstring(name));
    f.rootUpdate()->indexObject(i);

    long id = f.rootAdmin()->getEventContext()->userId;
    ExperimenterPtr self = new ExperimenterI(rlong(id), false);
    DetailsIPtr rootd = new DetailsI();
    rootd->setOwner(self);

    SearchPrx search = f.search();
    search->onlyType("Image");

    // With no restriction it should be found.
    // full text
    search->byFullText(name);
    assertResults(1, search);
    // annotated with
    byAnnotatedWith(search, tag);
    assertResults(1, search);
    // tag for group
    search->byTagForGroups(name);
    assertResults(1, search);
    // group for tag
    search->byGroupForTags(name);
    assertResults(1, search);

    // Restrict only to root, and then shouldn't be found
    search->notOwnedBy(DetailsIPtr());
    search->onlyOwnedBy(rootd);
    // full text
    search->byFullText(name);
    ASSERT_FALSE(search->hasNext());
    // annotated with
    byAnnotatedWith(search, tag);
    assertResults(0, search);
    // tag for group
    search->byTagForGroups(name);
    assertResults(0, search);
    // group for tags
    search->byGroupForTags(name);
    assertResults(0, search);

    // Restrict to not root, and then should be found again.
    search->onlyOwnedBy(DetailsIPtr());
    search->notOwnedBy(rootd);
    // full text
    search->byFullText(name);
    assertResults(1, search);
    // annotated with
    byAnnotatedWith(search, tag);
    assertResults(1, search);
    // tag for group
    search->byTagForGroups(name);
    assertResults(1, search);
    // group for tags
    search->byGroupForTags(name);
    assertResults(1, search);

    // Now restrict to the user, and again one
    search->notOwnedBy(DetailsIPtr());
    search->onlyOwnedBy(user);
    // full text
    search->byFullText(name);
    assertResults(1, search);
    // annotated with
    byAnnotatedWith(search, tag);
    assertResults(1, search);
    // tag for group
    search->byTagForGroups(name);
    assertResults(1, search);
    // group for tags
    search->byGroupForTags(name);
    assertResults(1, search);

    // But not-user should return nothing
    search->notOwnedBy(user);
    search->onlyOwnedBy(DetailsIPtr());
    // full text
    search->byFullText(name);
    assertResults(0, search);
    // annotated with
    byAnnotatedWith(search, tag);
    assertResults(0, search);
    // tag for group
    search->byTagForGroups(name);
    assertResults(0, search);
    // group for tags
    search->byGroupForTags(name);
    assertResults(0, search);
}

TEST(SearchTest, testOnlyOwnedByGroup ) {

    SearchFixture f;
    ExperimenterPtr e = f.newUser();

    f.login(e->getOmeName()->getValue());
    ExperimenterGroupIPtr g = new ExperimenterGroupI
        (rlong(f.admin()->getEventContext()->groupId), false);

    DetailsIPtr user = new DetailsI();
    user->setGroup( g );

    string name = f.uuid();
    ImagePtr i = new_ImageI();
    i->setName( rstring(name) );
    TagAnnotationIPtr tag = new TagAnnotationI();
    tag->setTextValue(rstring(name));
    i->linkAnnotation(tag);
    TagAnnotationIPtr grp = new TagAnnotationI();
    grp->setTextValue(rstring(name));
    tag->linkAnnotation(grp);
    i = ImagePtr::dynamicCast(f.update()->saveAndReturnObject(i));
    // Recreating instance as example
    tag = new TagAnnotationI();
    tag->setTextValue(rstring(name));
    f.rootUpdate()->indexObject(i);

    long id = f.rootAdmin()->getEventContext()->groupId;
    ExperimenterGroupIPtr self = new ExperimenterGroupI(rlong(id), false);
    DetailsIPtr rootd = new DetailsI();
    rootd->setGroup(self);

    SearchPrx search = f.search();
    search->onlyType("Image");

    // With no restriction it should be found.
    // full text
    search->byFullText(name);
    assertResults(1, search);
    // annotated with
    byAnnotatedWith(search, tag);
    assertResults(1, search);
    // tag for group
    search->byTagForGroups(name);
    assertResults(1, search);
    // group for tags
    search->byGroupForTags(name);
    assertResults(1, search);

    // Restrict only to root, and then shouldn't be found
    search->onlyOwnedBy(rootd);
    search->notOwnedBy(DetailsIPtr());
    // full text
    search->byFullText(name);
    ASSERT_FALSE(search->hasNext());
    // annotated with
    byAnnotatedWith(search, tag);
    assertResults(0, search);
    // tag for group
    search->byTagForGroups(name);
    assertResults(0, search);
    // group for tags
    search->byGroupForTags(name);
    assertResults(0, search);

    // Restrict to not root, and then should be found again.
    search->onlyOwnedBy(DetailsIPtr());
    search->notOwnedBy(rootd);
    // full text
    search->byFullText(name);
    assertResults(1, search);
    // annotated with
    byAnnotatedWith(search, tag);
    assertResults(1, search);
    // tag for group
    search->byTagForGroups(name);
    assertResults(1, search);
    // group for tags
    search->byGroupForTags(name);
    assertResults(1, search);

    // Now restrict to the user, and again one
    search->onlyOwnedBy(user);
    search->notOwnedBy(DetailsIPtr());
    // full text
    search->byFullText(name);
    assertResults(1, search);
    // annotated with
    byAnnotatedWith(search, tag);
    assertResults(1, search);
    // tag for group
    search->byTagForGroups(name);
    assertResults(1, search);
    // group for tags
    search->byGroupForTags(name);
    assertResults(1, search);

    // But not-user should return nothing
    search->notOwnedBy(user);
    search->onlyOwnedBy(DetailsIPtr());
    // full text
    search->byFullText(name);
    assertResults(0, search);
    // annotated with
    byAnnotatedWith(search, tag);
    assertResults(0, search);
    // tag for group
    search->byTagForGroups(name);
    assertResults(0, search);
    // group for tags
    search->byGroupForTags(name);
    assertResults(0, search);
}

namespace
{
    omero::RTimePtr oneHourAgo() {
        time_t start = time (NULL);
        tm* mn = localtime(&start);
        mn->tm_hour = mn->tm_hour - 1;
        Ice::Long millis = mktime(mn) * 1000;
        return rtime(millis);
    }

    omero::RTimePtr inOneHour() {
        time_t start = time (NULL);
        tm* mn = localtime(&start);
        mn->tm_hour = mn->tm_hour + 1;
        Ice::Long millis = mktime(mn) * 1000;
        return rtime(millis);
    }

    omero::RTimePtr now() {
        time_t start = time (NULL);
        tm* mn = localtime(&start);
        Ice::Long millis = mktime(mn) * 1000;
        return rtime(millis);
    }
}

TEST(SearchTest, testOnlyCreateBetween ) {
    SearchFixture f;
    f.login();

    string name = f.uuid();;
    RTimePtr start = now();
    ImagePtr i = new_ImageI();
    i->setName(rstring(name));
    TagAnnotationIPtr tag = new TagAnnotationI();
    tag->setTextValue(rstring(name));
    i->linkAnnotation(tag);
    TagAnnotationIPtr grp = new TagAnnotationI();
    grp->setTextValue(rstring(name));
    tag->linkAnnotation(grp);
    i = ImagePtr::dynamicCast(f.update()->saveAndReturnObject(i));
    tag = new TagAnnotationI();
    tag->setTextValue(rstring(name));
    f.rootUpdate()->indexObject(i);

    SearchPrx search = f.search();
    search->onlyType("Image");

    // Find the Image
    // full text
    search->byFullText(name);
    assertResults(1, search);
    // annotated with
    byAnnotatedWith(search, tag);
    assertResults(1, search);
    // tag for group
    search->byTagForGroups(name);
    assertResults(1, search);
    // group for tags
    search->byGroupForTags(name);
    assertResults(1, search);

    // Now restrict the search to past
    search->onlyCreatedBetween(omero::RTimePtr(), oneHourAgo());
    // full text
    search->byFullText(name);
    ASSERT_FALSE(search->hasNext());
    // annotated with
    byAnnotatedWith(search, tag);
    assertResults(0, search);
    // tag for group
    search->byTagForGroups(name);
    assertResults(0, search);
    // group for tags
    search->byGroupForTags(name);
    assertResults(0, search);

    // Future
    search->onlyCreatedBetween(inOneHour(), omero::RTimePtr());
    // full text
    search->byFullText(name);
    ASSERT_FALSE(search->hasNext());
    // annotated with
    byAnnotatedWith(search, tag);
    assertResults(0, search);
    // tag for group
    search->byTagForGroups(name);
    assertResults(0, search);
    // group for tags
    search->byGroupForTags(name);
    assertResults(0, search);

    // 2 hour period around now
    search->onlyCreatedBetween(oneHourAgo(), inOneHour());
    // full text
    search->byFullText(name);
    assertResults(1, search);
    // annotated with
    byAnnotatedWith(search, tag);
    assertResults(1, search);
    // tag for group
    search->byTagForGroups(name);
    assertResults(1, search);
    // group for tags
    search->byGroupForTags(name);
    assertResults(1, search);

    // Starting at now old 'now'
    search->onlyCreatedBetween(omero::RTimePtr(), start);
    // full text
    search->byFullText(name);
    ASSERT_FALSE(search->hasNext());
    // annotated with
    byAnnotatedWith(search, tag);
    assertResults(0, search);
    // tag for group
    search->byTagForGroups(name);
    assertResults(0, search);
    // group for tags
    search->byGroupForTags(name);
    assertResults(0, search);

    // Open them up again and should be found
    search->onlyCreatedBetween(omero::RTimePtr(), omero::RTimePtr());
    // full text
    search->byFullText(name);
    assertResults(1, search);
    // annotated with
    byAnnotatedWith(search, tag);
    assertResults(1, search);
    // tag for group
    search->byTagForGroups(name);
    assertResults(1, search);
    // group for tags
    search->byGroupForTags(name);
    assertResults(1, search);
}

TEST(SearchTest, testOnlyModifiedBetween ) {

    // Ignored by
    // byTagForGroups, byGroupForTags (tags are immutable) results always 1

    SearchFixture f;
    f.login();

    string name = f.uuid();;
    RTimePtr start = now();
    ImagePtr i = new_ImageI();
    i->setName(rstring(name));
    TagAnnotationIPtr tag = new TagAnnotationI();
    tag->setTextValue(rstring(name));
    i->linkAnnotation(tag);
    TagAnnotationIPtr grp = new TagAnnotationI();
    grp->setTextValue(rstring(name));
    tag->linkAnnotation(grp);
    i = ImagePtr::dynamicCast(f.update()->saveAndReturnObject(i));
    tag = new TagAnnotationI();
    tag->setTextValue(rstring(name));
    f.rootUpdate()->indexObject(i);

    SearchPrx search = f.search();
    search->onlyType("Image");

    // Find the Image
    // full text
    search->byFullText(name);
    assertResults(1, search);
    // annotated with
    byAnnotatedWith(search, tag);
    assertResults(1, search);
    // tag for group
    search->byTagForGroups(name);
    assertResults(1, search);
    // group for tags
    search->byGroupForTags(name);
    assertResults(1, search);

    // Now restrict the search to past
    search->onlyModifiedBetween(omero::RTimePtr(), oneHourAgo());
    // full text
    search->byFullText(name);
    ASSERT_FALSE(search->hasNext());
    // annotated with
    byAnnotatedWith(search, tag);
    assertResults(0, search);
    // group for tags
    search->byGroupForTags(name);
    assertResults(0, search);
    // tag for group
    search->byTagForGroups(name);
    assertResults(0, search);

    // Future
    search->onlyModifiedBetween(inOneHour(), omero::RTimePtr());
    // full text
    search->byFullText(name);
    ASSERT_FALSE(search->hasNext());
    // annotated with
    byAnnotatedWith(search, tag);
    assertResults(0, search);
    // tag for group
    search->byTagForGroups(name);
    assertResults(0, search);
    // group for tags
    search->byGroupForTags(name);
    assertResults(0, search);

    // 2 hour period around now
    search->onlyModifiedBetween(oneHourAgo(), inOneHour());
    // full text
    search->byFullText(name);
    assertResults(1, search);
    // annotated with
    byAnnotatedWith(search, tag);
    assertResults(1, search);
    // tag for group
    search->byTagForGroups(name);
    assertResults(1, search);
    // group for tags
    search->byGroupForTags(name);
    assertResults(1, search);

    // Starting at now old 'now'
    search->onlyModifiedBetween(omero::RTimePtr(), start);
    // full text
    search->byFullText(name);
    ASSERT_FALSE(search->hasNext());
    // annotated with
    byAnnotatedWith(search, tag);
    assertResults(0, search);
    // tag for group
    search->byTagForGroups(name);
    assertResults(0, search);
    // group for tags
    search->byGroupForTags(name);
    assertResults(0, search);

    // Open them up again and should be found
    search->onlyModifiedBetween(omero::RTimePtr(), omero::RTimePtr());
    // full text
    search->byFullText(name);
    assertResults(1, search);
    // annotated with
    byAnnotatedWith(search, tag);
    assertResults(1, search);
    // tag for group
    search->byTagForGroups(name);
    assertResults(1, search);
    // group for tags
    search->byGroupForTags(name);
    assertResults(1, search);
}

TEST(SearchTest, testOnlyAnnotatedBetween ) {

    SearchFixture f;
    f.login();

    string name = f.uuid();;
    RTimePtr start = now();
    ImagePtr i = new_ImageI();
    i->setName(rstring(name));
    TagAnnotationIPtr tag = new TagAnnotationI();
    tag->setTextValue(rstring(name));
    i->linkAnnotation(tag);
    TagAnnotationIPtr grp = new TagAnnotationI();
    grp->setTextValue(rstring(name));
    tag->linkAnnotation(grp);
    i = ImagePtr::dynamicCast(f.update()->saveAndReturnObject(i));
    tag = new TagAnnotationI();
    tag->setTextValue(rstring(name));
    f.rootUpdate()->indexObject(i);

    SearchPrx search = f.search();
    search->onlyType("Image");

    // Find the Image
    // full text
    search->byFullText(name);
    assertResults(1, search);
    // annotated with
    byAnnotatedWith(search, tag);
    assertResults(1, search);
    // tag for group
    search->byTagForGroups(name);
    assertResults(1, search);
    // group for tags
    search->byGroupForTags(name);
    assertResults(1, search);

    // Now restrict the search to past
    search->onlyAnnotatedBetween(omero::RTimePtr(), oneHourAgo());
    // full text
    search->byFullText(name);
    ASSERT_FALSE(search->hasNext());
    // annotated with
    byAnnotatedWith(search, tag);
    assertResults(0, search);
    // tag for group
    search->byTagForGroups(name);
    assertResults(0, search);
    // group for tags
    search->byGroupForTags(name);
    assertResults(0, search);

    // Future
    search->onlyAnnotatedBetween(inOneHour(), omero::RTimePtr());
    // full text
    search->byFullText(name);
    ASSERT_FALSE(search->hasNext());
    // annotated with
    byAnnotatedWith(search, tag);
    assertResults(0, search);
    // tag for group
    search->byTagForGroups(name);
    assertResults(0, search);
    // group for tags
    search->byGroupForTags(name);
    assertResults(0, search);

    // 2 hour period around now
    search->onlyAnnotatedBetween(oneHourAgo(), inOneHour());
    // full text
    search->byFullText(name);
    assertResults(1, search);
    // annotated with
    byAnnotatedWith(search, tag);
    assertResults(1, search);
    // tag for group
    search->byTagForGroups(name);
    assertResults(1, search);
    // group for tags
    search->byGroupForTags(name);
    assertResults(1, search);

    // Starting at now old 'now()'
    search->onlyAnnotatedBetween(omero::RTimePtr(), start);
    // full text
    search->byFullText(name);
    ASSERT_FALSE(search->hasNext());
    // annotated with
    byAnnotatedWith(search, tag);
    assertResults(0, search);
    // tag for group
    search->byTagForGroups(name);
    assertResults(0, search);
    // group for tags
    search->byGroupForTags(name);
    assertResults(0, search);

    // Open them up again and should be found
    search->onlyAnnotatedBetween(omero::RTimePtr(), omero::RTimePtr());
    // full text
    search->byFullText(name);
    assertResults(1, search);
    // annotated with
    byAnnotatedWith(search, tag);
    assertResults(1, search);
    // tag for group
    search->byTagForGroups(name);
    assertResults(1, search);
    // group for tags
    search->byGroupForTags(name);
    assertResults(1, search);
}

TEST(SearchTest, testOnlyAnnotatedBy ) {

    SearchFixture f;
    f.login();

    string name = f.uuid();
    string tag = f.uuid();
    ImagePtr i = new_ImageI();
    i->setName(rstring(name));
    TagAnnotationIPtr t = new TagAnnotationI();
    t->setTextValue(rstring(tag));
    i->linkAnnotation(t);
    TagAnnotationIPtr grp = new TagAnnotationI();
    grp->setTextValue(rstring(tag));
    t->linkAnnotation(grp);
    i = ImagePtr::dynamicCast(f.update()->saveAndReturnObject(i));
    t = new TagAnnotationI();
    t->setTextValue(rstring(tag));
    f.rootUpdate()->indexObject(i);

    SearchPrx search = f.search();
    search->onlyType("Image");

    // Find the annotation
    // full text
    search->byFullText(name);
    assertResults(1, search);
    // annotated with
    byAnnotatedWith(search, t);
    assertResults(1, search);
    // tag for group
    search->byTagForGroups(tag);
    assertResults(1, search);
    // group for tags
    search->byGroupForTags(tag);
    assertResults(1, search);

    // But if we restrict it to another user, there should be none
    ExperimenterPtr e = f.newUser();
    DetailsIPtr d = new DetailsI();
    d->setOwner(e);
    search->onlyAnnotatedBy(d);
    search->notAnnotatedBy(DetailsIPtr());
    // full text
    search->byFullText(name);
    ASSERT_FALSE(search->hasNext());
    // annotated with
    byAnnotatedWith(search, t);
    assertResults(0, search);
    // tag for group
    search->byTagForGroups(tag);
    assertResults(0, search);
    // group for tags
    search->byGroupForTags(tag);
    assertResults(0, search);

    // Reversing the ownership should give results
    search->onlyAnnotatedBy(DetailsIPtr());
    search->notAnnotatedBy(d);
    // full text
    search->byFullText(name);
    assertResults(1, search);
    // annotated with
    byAnnotatedWith(search, t);
    assertResults(1, search);
    // tag for group
    search->byTagForGroups(tag);
    assertResults(1, search);
    // group for tags
    search->byGroupForTags(tag);
    assertResults(1, search);
}

TEST(SearchTest, testOnlyAnnotatedWith ) {

    // ignored by byAnnotatedWith
    // ignored by byTagForGroups, byGroupForTags

    SearchFixture f;
    f.login();

    string name = f.uuid();
    ImagePtr i = new_ImageI();
    i->setName(rstring(name));
    i = ImagePtr::dynamicCast(f.update()->saveAndReturnObject(i));

    f.rootUpdate()->indexObject(i);

    SearchPrx search = f.search();

    // Search for tagged image, which shouldn't be there
    search->onlyAnnotatedWith(stringSet("TagAnnotation"));
    search->onlyType("Image");
    search->byFullText(name);
    ASSERT_FALSE(search->hasNext());

    // But if we ask for Images which aren't annotated it should appear
    search->onlyAnnotatedWith(StringSet());
    search->onlyType("Image");
    search->byFullText(name);
    assertResults(1, search);

    // Now let's tag it and see if it shows up
    TagAnnotationIPtr t = new TagAnnotationI();
    t->setTextValue(rstring(f.uuid()));
    t = TagAnnotationIPtr::dynamicCast(f.update()->saveAndReturnObject(t));

    ImageAnnotationLinkIPtr link = new ImageAnnotationLinkI();
    link->setChild( t );
    link->setParent( i );
    f.update()->saveObject(link);

    f.rootUpdate()->indexObject(i);

    // Since we're looking for "no annotations" there should be no results
    search->byFullText(name);
    ASSERT_FALSE(search->hasNext());

    // And if we turn the annotations back on?
    search->onlyAnnotatedWith(stringSet("TagAnnotation"));
    search->byFullText(name);
    assertResults(1, search);
}

// Test failing due to Hibernate bug
// https://hibernate.onjira.com/browse/HHH-879
TEST(SearchTest, DISABLED_testOnlyAnnotatedWithMultiple ) {

    try {
    SearchFixture f;
    f.login();

    string name = f.uuid();;
    ImagePtr onlyTag = new_ImageI();
    onlyTag->setName( rstring(name) );
    ImagePtr onlyBool = new_ImageI();
    onlyBool->setName( rstring(name) );
    ImagePtr both = new_ImageI();
    both->setName( rstring(name) );

    TagAnnotationIPtr tag = new TagAnnotationI();
    tag->setTextValue(rstring("tag"));
    BooleanAnnotationIPtr b = new BooleanAnnotationI();
    b->setBoolValue(rbool(false));

    onlyTag->linkAnnotation(tag);
    both->linkAnnotation(tag);
    both->linkAnnotation(b);
    onlyBool->linkAnnotation(b);

    IObjectList arr;
    arr.push_back(onlyTag);
    arr.push_back(onlyBool);
    arr.push_back(both);
    arr = f.update()->saveAndReturnArray(arr);
    IObjectList::iterator beg = arr.begin();
    while (beg != arr.end()) {
        f.rootUpdate()->indexObject(*beg++);
    }

    SearchPrx search = f.search();
    search->onlyType("Image");

    search->onlyAnnotatedWith(stringSet("TagAnnotation"));
    search->byFullText(name);
    ASSERT_EQ(2U, search->results().size());

    search->onlyAnnotatedWith(stringSet("BooleanAnnotation"));
    search->byFullText(name);
    ASSERT_EQ(2U, search->results().size());

    search->onlyAnnotatedWith(stringSet("BooleanAnnotation", "TagAnnotation"));
    search->byFullText(name);
    assertResults(1, search);
    } catch (const omero::InternalException& ie) {
        FAIL() << "internal exception:"+ie.message;
    } catch (const omero::ApiUsageException& aue) {
        FAIL() << "api usage exception thrown:" << aue.message;
    } catch (const Ice::UnknownException& ue) {
        cout << ue << endl;
        FAIL() << "unknown exception thrown";
    }
}

// other
// =========================================================================

TEST(SearchTest, testMergedBatches ) {

    SearchFixture f;
    f.login();

    string uuid1 = f.uuid();
    string uuid2 = f.uuid();
    ImagePtr i1 = new_ImageI();
    i1->setName( rstring(uuid1) );
    ImagePtr i2 = new_ImageI();
    i2->setName( rstring(uuid2) );
    i1 = ImagePtr::dynamicCast( f.update()->saveAndReturnObject(i1) );
    i2 = ImagePtr::dynamicCast( f.update()->saveAndReturnObject(i2) );
    f.rootUpdate()->indexObject(i1);
    f.rootUpdate()->indexObject(i2);

    SearchPrx search = f.search();
    search->onlyType("Image");
    search->byFullText(uuid1);
    assertResults(1, search);

    search->byFullText(uuid2);
    assertResults(1, search);

    search->bySomeMustNone(stringSet(uuid1, uuid2), StringSet(), StringSet());
    assertResults(2, search);

    // Everything looks ok, now try with batch
    search->setMergedBatches(true);
    search->byFullText(uuid1);
    search->byFullText(uuid2);
    assertResults(2, search);
}

#define assertImageResults(images, search, descending) \
    for (size_t i = descending? images.size() -1 : 0; i < images.size() && search->hasNext(); i += descending? -1 : 1) { \
        string expectedDesc = images[i]->getDescription()->getValue(); \
        string actualDesc = ImagePtr::dynamicCast(search->next())->getDescription()->getValue(); \
        ASSERT_EQ(expectedDesc, actualDesc); \
    }

#define assertImageResultsList(images, search, is) \
    for (size_t i = 0; i < images.size() && search->hasNext(); i++) { \
        string expectedDesc = images[is[i]]->getDescription()->getValue(); \
        string actualDesc = ImagePtr::dynamicCast(search->next())->getDescription()->getValue(); \
        ASSERT_EQ(expectedDesc, actualDesc); \
    }

TEST(SearchTest, testOrderBy) {

    SearchFixture f;
    f.login();
    
    string uuid = f.uuid();
    
    TagAnnotationIPtr tag = new TagAnnotationI();
    tag->setTextValue(rstring(uuid));
    
    // create some test images
    vector<ImagePtr> images;
    for (int i = 0; i < 2; ++i) {
        ImagePtr image = new_ImageI();
        image->setName(rstring(uuid));
        char desc[] = "a";
        desc[0] += i;
        image->setDescription(rstring(desc));
        image->linkAnnotation(tag);
        images.push_back(image);
        
        images[i] = ImagePtr::dynamicCast(f.update()->saveAndReturnObject(images[i]));
    }
    
    for (size_t i = 0; i < images.size(); i++)
        f.rootUpdate()->indexObject(images[i]);
    
    tag = new TagAnnotationI();
    tag->setTextValue(rstring(uuid));

    SearchPrx search = f.search();
    search->onlyType("Image");

    // Order by description
    search->unordered();
    search->addOrderByDesc("description");
    
    // full text
    search->byFullText(uuid);
    assertImageResults(images, search, false);
    
    // annotated with
    byAnnotatedWith(search, tag);
    assertImageResults(images, search, true);

    // Order by descript asc
    search->unordered();
    search->addOrderByAsc("description");
    
    // full text
    search->byFullText(uuid);
    assertImageResults(images, search, false);
    
    // annotated with
    byAnnotatedWith(search, tag);
    assertImageResults(images, search, false);

    // Ordered by id
    search->unordered();
    search->addOrderByDesc("id");
    
    // full text
    search->byFullText(uuid);
    assertImageResults(images, search, false);
    
    // annotated with
    byAnnotatedWith(search, tag);
    assertImageResults(images, search, true);

    // Ordered by creation event id
    search->unordered();
    search->addOrderByDesc("details.creationEvent.id");
    
    // full text
    search->byFullText(uuid);
    assertImageResults(images, search, false);
    
    // annotated with
    byAnnotatedWith(search, tag);
    assertImageResults(images, search, true);

    // ordered by creation event time
    search->unordered();
    search->addOrderByDesc("details.creationEvent.time");
    
    // annotated with
    byAnnotatedWith(search, tag);
    assertImageResults(images, search, true);

    // To test multiple sort fields, we add another image with an "a"
    // description, which should could before the other image with the "a"
    // description if we reverse the id order

    ImagePtr i3 = new_ImageI();
    i3->setName(rstring(uuid));
    i3->setDescription(rstring("a"));
    i3->linkAnnotation(tag);
    i3 = ImagePtr::dynamicCast(f.update()->saveAndReturnObject(i3));
    images.push_back(i3);
    
    f.rootUpdate()->indexObject(i3);
    
    tag = new TagAnnotationI();
    tag->setTextValue(rstring(uuid));

    // multi-ordering
    search->unordered();
    search->addOrderByAsc("description");
    search->addOrderByDesc("id");
    
    // annotated with
    byAnnotatedWith(search, tag);
    int is[] = {2, 0, 1};
    assertImageResultsList(images, search, is);
    
    // full text
    search->byFullText(uuid);
    assertImageResults(images, search, false);
}

TEST(SearchTest, testFetchAnnotations ) {
    try {
    SearchFixture f;
    f.login();

    string uuid = f.uuid();;
    ImagePtr i = new_ImageI();
    i->setName(rstring(uuid));
    TagAnnotationIPtr tag = new TagAnnotationI();
    tag->setTextValue(rstring(uuid));
    LongAnnotationIPtr la = new LongAnnotationI();
    la->setLongValue(rlong(1L));
    DoubleAnnotationIPtr da = new DoubleAnnotationI();
    da->setDoubleValue(rdouble(0.0));
    i->linkAnnotation(tag);
    i->linkAnnotation(la);
    i->linkAnnotation(da);
    i = ImagePtr::dynamicCast(f.update()->saveAndReturnObject(i));
    tag = new TagAnnotationI();
    tag->setTextValue(rstring(uuid));
    f.rootUpdate()->indexObject(i);

    SearchPrx search = f.search();
    search->onlyType("Image");

    // No fetch returns empty annotations
    // full text
    search->byFullText(uuid);
    ImagePtr t = ImagePtr::dynamicCast( search->results().at(0) );
    ASSERT_EQ(-1, t->sizeOfAnnotationLinks());
    // annotated with
    byAnnotatedWith(search, tag);
    t = ImagePtr::dynamicCast( search->results().at(0) );
    ASSERT_EQ(-1, t->sizeOfAnnotationLinks());

    // Fetch only a given type
    search->fetchAnnotations(stringSet("TagAnnotation"));
    // annotated with
    byAnnotatedWith(search, tag);
    t = ImagePtr::dynamicCast( search->results().at(0) );
    ASSERT_EQ(1, t->sizeOfAnnotationLinks());
    // full text
    search->byFullText(uuid);
    t = ImagePtr::dynamicCast( search->results().at(0) );
    ASSERT_EQ(3, t->sizeOfAnnotationLinks());

    // fetch only a given type different from annotated-with type
    search->fetchAnnotations(stringSet("DoubleAnnotation"));
    // annotated with
    byAnnotatedWith(search, tag);
    t = ImagePtr::dynamicCast( search->results().at(0) );
    ASSERT_EQ(1, t->sizeOfAnnotationLinks());
    // full text
    search->byFullText(uuid);
    t = ImagePtr::dynamicCast( search->results().at(0) );
    ASSERT_EQ(3, t->sizeOfAnnotationLinks());

    // fetch two types
    search->fetchAnnotations(stringSet("TagAnnotation", "DoubleAnnotation"));
    // annotated with
    byAnnotatedWith(search, tag);
    t = ImagePtr::dynamicCast( search->results().at(0) );
    ASSERT_EQ(2, t->sizeOfAnnotationLinks());
    // full text
    search->byFullText(uuid);
    t = ImagePtr::dynamicCast( search->results().at(0) );
    ASSERT_EQ(3, t->sizeOfAnnotationLinks());

    // Fetch all
    // FIXME: "Annotation" causes an IceMapper error, had to use the full
    // exhaustive list of annotation classes being used. (ticket:2067)
    // Tue Aug 10 11:06:46 BST 2010 -- Chris Allan <callan@blackcat.ca>
    search->fetchAnnotations(stringSet("TagAnnotation", "LongAnnotation", "DoubleAnnotation"));
    // annotated with
    byAnnotatedWith(search, tag);
    assertResults(1, search);
    // TODO t = ImagePtr::dynamicCast( search->results().get(0) );
    // TODO ASSERT_EQ(3, t->sizeOfAnnotationLinks());
    // full text
    search->byFullText(uuid);
    t = ImagePtr::dynamicCast( search->results().at(0) );
    ASSERT_EQ(3, t->sizeOfAnnotationLinks());

    // resave and see if there is data loss
    search->fetchAnnotations(stringSet("TagAnnotation"));
    byAnnotatedWith(search, tag);
    t = ImagePtr::dynamicCast(search->next());
    FileAnnotationIPtr fa = new FileAnnotationI();
    ImageAnnotationLinkIPtr link = new ImageAnnotationLinkI();
    link->setParent(new ImageI(t->getId()->getValue(), false));
    link->setChild(fa);
    f.update()->saveObject(link);
    ParametersPtr params = new Parameters();
    params->map = ParamMap();
    params->map["id"] = t->getId();
    t = ImagePtr::dynamicCast(f.query()->findByQuery
        ("select t from Image t join fetch t.annotationLinks where t.id = :id",
        params));
    ASSERT_EQ(4, t->sizeOfAnnotationLinks());
    } catch (const omero::InternalException& ie) {
        FAIL() << "internal exception:"+ie.message;
    } catch (const omero::ApiUsageException& aue) {
        FAIL() << "api usage exception thrown:" << aue.message;
    } catch (const Ice::UnknownException& ue) {
        cout << ue << endl;
        FAIL() << "unknown exception thrown";
    }
}

TEST(SearchTest, testCommentAnnotationDoesntTryToLoadUpdateEvent ) {
    SearchFixture f;
    f.login();

    string uuid = f.uuid();;
    CommentAnnotationIPtr ta = new CommentAnnotationI();
    ta->setTextValue(rstring(uuid));
    ta = CommentAnnotationIPtr::dynamicCast(f.update()->saveAndReturnObject(ta));
    f.rootUpdate()->indexObject(ta);

    SearchPrx search = f.search();
    search->onlyType("CommentAnnotation");
    search->byFullText(uuid);
    assertResults(1, search);
}

// bugs
// =========================================================================

// Test failing due to OMERO server bug
// https://trac.openmicroscopy.org.uk/ome/ticket/10408
TEST(SearchTest, DISABLED_testExperimenterDoesntTryToLoadOwner ) {
    SearchFixture f;
    SearchPrx search = f.search();
    search->onlyType("Experimenter");
    search->byFullText("root");
    assertAtLeastResults(1, search);
}

// Test failing due to OMERO server bug
// https://trac.openmicroscopy.org.uk/ome/ticket/10408
TEST(SearchTest, DISABLED_testLookingForExperimenterWithOwner ) {
    SearchFixture f;
    SearchPrx search = f.search();
    search->onlyType("Experimenter");

    // Just root should work
    search->byFullText("root");
    assertAtLeastResults(1, search);

    // And filtered on "owner" (experimenter has none) should work, too.
    DetailsIPtr d = new DetailsI();
    d->setOwner(new ExperimenterI(0L, false));
    search->onlyOwnedBy(d);
    search->byFullText("root");
    assertAtLeastResults(1, search);
}
