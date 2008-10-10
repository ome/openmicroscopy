/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */
#include <IceUtil/UUID.h>
#include <Glacier2/Glacier2.h>
#include <boost_fixture.h>
#include <time.h>
#include <omero/Collections.h>

using namespace std;
using namespace omero;
using namespace omero::api;
using namespace omero::model;
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

class SearchFixture {
    string _name;
    Fixture f;
    const omero::client* client;
    ServiceFactoryPrx sf;
public:
    SearchFixture() {
    }
    SearchFixture(string name) {
	_name = name;
    }
    void init() {
	if (!sf) {
	    if (_name.empty()) {
		client = f.login();
	    } else if (_name == "root") {
		client = f.root_login();
	    } else {
		client = f.login(_name);
	    }
	    sf = (*client).getSession();
	}
    }
    SearchPrx search() {
	init();
	return sf->createSearchService();
    }
    IAdminPrx admin() {
	init();
	return sf->getAdminService();
    }
    IQueryPrx query() {
	init();
	return sf->getQueryService();
    }
    IUpdatePrx update() {
	init();
	return sf->getUpdateService();
    }
    string uuid() {
	return f.uuid();
    }
    OriginalFileIPtr createFile() {
	OriginalFileIPtr file = new OriginalFileI();
	return file;
    }
    ExperimenterIPtr newUser() {
	ExperimenterIPtr e = new ExperimenterI();
	e->setOmeName( new omero::RString(uuid()) );
	e->setFirstName( new omero::RString("name") );
	e->setLastName( new omero::RString("name") );
	long id = admin()->createUser(e, "default");
	return ExperimenterIPtr::dynamicCast(query()->get("Experimenter",id));
    }
};

/*
 * Clears one result from the current queue.
 */
#define assertResults(count, search) _assertResults(__LINE__, count, search, true) 
#define assertAtLeastResults(count, search) _assertResults(__LINE__, count, search, false) 
void _assertResults(int line, int count, SearchPrx search, bool exact) {
    stringstream out;
    out << "line " << line << ":";
    if (count  > 0) {
	out << "Search should have results" << endl;
        BOOST_CHECK_MESSAGE( search->hasNext(), out.str());
        if (search->hasNext()) {
	    if (exact) {
		BOOST_CHECK_EQUAL( count, search->results().size() );
	    } else {
		BOOST_CHECK_MESSAGE( search->results().size() > count, "Not enough results");
	    }
        }
    } else {
	out << "Search shouldn't have results. Found";
	if (search->hasNext()) {
            int size = search->results().size();
	    out << size << endl;
            BOOST_ERROR( out.str() );
        }
    }
}

BOOST_AUTO_TEST_CASE( RootSearch )
{
    try {
        SearchFixture f;

        SearchPrx search = f.search();
        search->onlyType("Experimenter");
        search->byFullText("root");
        if (search->hasNext()) {
            ExperimenterIPtr e = ExperimenterIPtr::dynamicCast(search->next());
        }

    } catch (const omero::ApiUsageException& aue) {
        cout << aue.message <<endl;
	BOOST_ERROR ( "api usage exception thrown" );
    } catch (const Ice::UnknownException& ue) {
        cout << ue << endl;
	BOOST_ERROR( "unknown exception thrown" );
    }
}

BOOST_AUTO_TEST_CASE( IQuerySearch )
{
    try {
        SearchFixture f;
	SearchFixture root("root");
        IUpdatePrx update = f.update();

	string uuid = f.uuid();
        ImageIPtr i = new ImageI();
        i->setName(new omero::RString(uuid));
	i = ImageIPtr::dynamicCast( update->saveAndReturnObject(i) );

	/*
	 *
	 */
	try {
	    root.update()->indexObject(i);
	} catch (const Glacier2::PermissionDeniedException& pde) {
	    BOOST_ERROR("permission denied:"+pde.reason);
	}

	/*
	 * IQuery provides a simple, stateless method for search
	 */
        IObjectList list;
        list = f.query()->findAllByFullText("Image",uuid,0);
	BOOST_CHECK_EQUAL( 1, list.size() );
	
    } catch (const omero::ApiUsageException& aue) {
        cout << aue.message <<endl;
	BOOST_ERROR ( "api usage exception thrown" );
    } catch (const Ice::UnknownException& ue) {
        cout << ue << endl;
	BOOST_ERROR( "unknown exception thrown" );
    }
}


BOOST_AUTO_TEST_CASE( Filtering )
{
    try {
        SearchFixture f;
	SearchFixture root;

	string uuid = f.uuid();
	ImageIPtr i = new ImageI();
        i->setName( new omero::RString(uuid) );

        IObjectPtr obj =  f.update()->saveAndReturnObject(i);
        root.update()->indexObject(obj);

        SearchPrx search = f.search();
        search->onlyType("Image");

        // Search without filter
        search->byFullText(uuid);
        assertResults(1, search);

        // Add id filter
        omero::sys::LongList ids;
        ids.push_back(obj->getId()->val);
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
        search->onlyIds(ids);

        // Add user filter
        DetailsIPtr rootonly = new DetailsI();
        rootonly->owner = new ExperimenterI(0L, false);
        search->onlyOwnedBy( rootonly );
        search->byFullText( uuid );
        assertResults(0, search);

        // Reset for coming searches
        search->onlyOwnedBy(DetailsIPtr());


    } catch (const omero::InternalException& ie) {
	BOOST_ERROR ( "internal exception:"+ie.message );
    } catch (const omero::ApiUsageException& aue) {
	BOOST_ERROR ( "api usage exception thrown:" + aue.message );
    } catch (const Ice::UnknownException& ue) {
	cout << ue << endl;
	BOOST_ERROR( "unknown exception thrown");
    }
}

// ===============================================================
// Below this point is an exact copy of SearchTest.java from r2265
// ===============================================================

// by<Query>
// =========================================================================
// This section tests each query method with various combinations of
// restrictions

BOOST_AUTO_TEST_CASE ( testByGroupForTags ) {
    SearchFixture f;
    SearchFixture root("root");
    string groupStr = f.uuid();
    string tagStr = f.uuid();;

    TagAnnotationIPtr tag = new TagAnnotationI();
    tag->setTextValue(new omero::RString(tagStr));

    TagAnnotationIPtr grp = new TagAnnotationI();
    grp->setTextValue(new omero::RString(groupStr));

    tag->linkAnnotation(grp);
    tag = TagAnnotationIPtr::dynamicCast(f.update()->saveAndReturnObject(tag));

    SearchPrx search = f.search();
    search->byGroupForTags(groupStr);
    assertResults(1, search);

    // Make another one
    groupStr = f.uuid();;
    grp = new TagAnnotationI();
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

    long oldUser = f.admin()->getEventContext()->userId;
    DetailsIPtr d = new DetailsI();
    d->setOwner(new ExperimenterI(new omero::RLong(oldUser), false));

    ExperimenterIPtr e = root.newUser();
    SearchFixture f2(e->getOmeName()->val);
    grp = new TagAnnotationI();
    groupStr = f2.uuid();;
    grp->setTextValue(new omero::RString(groupStr));
    tag->linkAnnotation(grp);
    tag = TagAnnotationIPtr::dynamicCast(f2.update()->saveAndReturnObject(tag));

    // All queries finished?
    BOOST_CHECK_EQUAL(0, search->activeQueries());
    assertResults(0, search);

    search->onlyOwnedBy(d);
    search->byGroupForTags(groupStr);
    BOOST_CHECK( ! search->hasNext());

    d->setOwner(e);
    search->onlyOwnedBy(d);
    search->byGroupForTags(groupStr);
    assertResults(1, search);

    search->onlyOwnedBy(DetailsIPtr());
    search->byGroupForTags(groupStr);
    assertResults(1, search);
}

BOOST_AUTO_TEST_CASE( testByTagForGroup ) {
    SearchFixture f;
    string groupStr = f.uuid();;
    string tagStr = f.uuid();;

    TagAnnotationIPtr tag = new TagAnnotationI();
    tag->setTextValue(new omero::RString(tagStr));

    TagAnnotationIPtr grp = new TagAnnotationI();
    grp->setTextValue(new omero::RString(groupStr));

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

    long oldUser = f.admin()->getEventContext()->userId;
    DetailsIPtr d = new DetailsI();
    d->setOwner(new ExperimenterI(new omero::RLong(oldUser), false));

    SearchFixture root("root");
    ExperimenterIPtr e = root.newUser();
    SearchFixture f2(e->getOmeName()->val);
    tag = new TagAnnotationI();
    tagStr = f2.uuid();;
    tag->setTextValue(new omero::RString(tagStr));
    tag->linkAnnotation(new TagAnnotationI(grp->getId(), false));
    tag = TagAnnotationIPtr::dynamicCast(f2.update()->saveAndReturnObject(tag));

    // All queries finished?
    BOOST_CHECK_EQUAL(0, search->activeQueries());
    assertResults(0, search);

    search->onlyOwnedBy(d);
    search->byTagForGroups(tagStr);
    assertResults(0, search);

    d->setOwner(e);
    search->onlyOwnedBy(d);
    search->byTagForGroups(tagStr);
    assertResults(1, search);

    search->onlyOwnedBy(DetailsIPtr());
    search->byTagForGroups(tagStr);
    assertResults(1, search);

}

BOOST_AUTO_TEST_CASE( testSimpleFullTextSearch ) {
    
    SearchFixture f;
    SearchFixture root("root");
    ImageIPtr i = new ImageI();
    i->setName(new omero::RString(f.uuid()));
    i = ImageIPtr::dynamicCast(f.update()->saveAndReturnObject(i));

    root.update()->indexObject(i);

    SearchPrx search = f.search();
    search->onlyType("Image");
    search->byFullText(i->getName()->val);
    int count = 0;
    IObjectPtr obj;
    while (search->hasNext()) {
	obj = search->next();
	count++;
	BOOST_CHECK( obj );
    }
    BOOST_CHECK(count == 1);
    search->close();

    search->onlyType("Image");
    search->byFullText(i->getName()->val);
    assertResults(1, search);

    search->close();
}

/*

vector<string> sa(string array...) {
    vector<string> v;
    static const unsigned int arraySize = sizeof array / sizeof *array ;
    for(int x=0; x < arraySize; x++) {
	v.push_back(array[x]);
    }
    return v;
}

BOOST_AUTO_TEST_CASE( testSomeMustNone ) {
    string contained[] = { "abc", "def", "ghi", "123" };
    string missing[] =  { "jkl", "mno", "pqr", "456" };

    SearchFixture f;
    SearchFixture root("root");

    ImageIPtr i = new ImageI();
    i->setName(new omero::RString("abc def ghi"));
    i = ImageIPtr::dynamicCast(f.update()->saveAndReturnObject(i));
    root.update()->indexObject(i);

    SearchPrx search = f.search();
    search->onlyType("Image");

    // Make sure we can find it simply
    search->bySomeMustNone(sa("abc"), sa(), sa());
    BOOST_CHECK(search->results().size() >= 1);

    //
    // Now we'll try more complicated queries
    //

    // This should return nothing since none is contained
    search->bySomeMustNone(sa("abc"), sa(), sa("def"));
    assertResults(search, 0);

    // but if the none is not contained should be ok.
    search->bySomeMustNone(sa("abc"), sa("abc"), sa("jkl"));
    assertAtLeastResults(1, search);

    // Simple must query
    search->bySomeMustNone(sa(), sa("abc"), sa());
    assertAtLeastResults(1, search);

    // same, but with a matching none
    search->bySomeMustNone(sa(), sa("abc"), sa("def"));
    assertResults(search, 0);

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
    assertResults(search, 0);

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

    search->bySomeMustNone(sa("abc", "def"), null, null);
    assertAtLeastResults(1, search);

    search->bySomeMustNone(null, sa("abc", "def"), null);
    assertAtLeastResults(1, search);

    search->bySomeMustNone(null, null, sa("abc", "def"));
    assertResults(0, search);

    search->bySomeMustNone(sa("ghi", "123"), sa("abc", "def"), null);
    assertAtLeastResults(1, search);

    search->bySomeMustNone(sa("ghi", "123"), sa("abc", "def"), sa("456"));
    assertAtLeastResults(1, search);

    search->bySomeMustNone(sa("ghi", "123"), sa("abc", "456"), sa("456"));
    assertResults(0, search);

    //
    // Completely empty
    //
    try {
	search->bySomeMustNone(null, null, null);
	fail("Should throw");
    } catch (ApiUsageException aue) {
	// ok
    }

    try {
	search->bySomeMustNone(sa(), null, null);
	fail("Should throw");
    } catch (ApiUsageException aue) {
	// ok
    }

    try {
	search->bySomeMustNone(sa(""), null, null);
	fail("Should throw");
    } catch (ApiUsageException aue) {
	// ok
    }

    //
    // Queries with spaces
    // For the moment these return as expected since the parser splits into
    // keywords.
    //
    search->bySomeMustNone(sa("\"abc def\""), null, null);
    assertAtLeastResults(1, search);
}

*/

BOOST_AUTO_TEST_CASE( testAnnotatedWith ) {
    SearchFixture f;
    SearchFixture root("root");
    string uuid = f.uuid();;
    ImageIPtr i = new ImageI();
    i->setName(new omero::RString(uuid));
    TagAnnotationIPtr tag = new TagAnnotationI();
    tag->setTextValue(new omero::RString(uuid));
    i->linkAnnotation(tag);
    i = ImageIPtr::dynamicCast(f.update()->saveAndReturnObject(i));
    root.update()->indexObject(i);

    SearchPrx search = f.search();
    search->onlyType("Image");
    TagAnnotationIPtr example = new TagAnnotationI();
    example->setTextValue(new omero::RString(uuid));
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
    i =  ImageIPtr::dynamicCast(f.update()->saveAndReturnObject(i));
    root.update()->indexObject(i);

    // Properly uses the id
    FileAnnotationIPtr ex2 = new FileAnnotationI();
    ex2->setFile(new OriginalFileI(file2->getId(), false));
    byAnnotatedWith(search, ex2);
    assertResults(1, search);

    // Now check if an empty example return results
    byAnnotatedWith(search, new FileAnnotationI());
    assertAtLeastResults(1, search);

    // Finding by superclass
    TextAnnotationIPtr txtAnn = new TextAnnotationI();
    txtAnn->setTextValue(new omero::RString(uuid));
    byAnnotatedWith(search, txtAnn);
    assertResults(1, search);
}

BOOST_AUTO_TEST_CASE( testAnnotatedWithNamespace ) {
    BOOST_CHECK_MESSAGE( false, "via namespace");
}

BOOST_AUTO_TEST_CASE( testAnnotatedWithMultiple ) {
    ImageIPtr i1 = new ImageI();
    i1->setName( new omero::RString("i1") );
    ImageIPtr i2 = new ImageI();
    i2->setName( new omero::RString("i2") );

    SearchFixture f;
    string uuid = f.uuid();;
    TagAnnotationIPtr ta = new TagAnnotationI();
    ta->setTextValue(new omero::RString(uuid));
    BooleanAnnotationIPtr ba = new BooleanAnnotationI();
    ba->setBoolValue(false);
    i1->linkAnnotation(ta);
    i2->linkAnnotation(ta);
    i2->linkAnnotation(ba);

    i1 = ImageIPtr::dynamicCast(f.update()->saveAndReturnObject(i1));
    i2 = ImageIPtr::dynamicCast(f.update()->saveAndReturnObject(i2));

    ta = new TagAnnotationI();
    ta->setTextValue(new omero::RString(uuid));
    ba = new BooleanAnnotationI();
    ba->setBoolValue(false);

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

}

// restrictions methods
// ========================================================================
// The tests in the following sections should include all the by* methods
// each testing a specific restriction

BOOST_AUTO_TEST_CASE( testOnlyIds ) {

    // ignored by
    // byTagForGroups, byGroupForTags

    SearchFixture f;
    SearchFixture root("root");
    string uuid = f.uuid();;
    ImageIPtr i1 = new ImageI();
    i1->setName( new omero::RString(uuid) );
    ImageIPtr i2 = new ImageI();
    i2->setName(new omero::RString(uuid));
    TagAnnotationIPtr tag = new TagAnnotationI();
    tag->setTextValue(new omero::RString(uuid));
    i1->linkAnnotation(tag);
    i2->linkAnnotation(tag);
    i1 = ImageIPtr::dynamicCast(f.update()->saveAndReturnObject(i1));
    i2 = ImageIPtr::dynamicCast(f.update()->saveAndReturnObject(i2));
    tag = new TagAnnotationI();
    tag->setTextValue(new omero::RString(uuid));
    root.update()->indexObject(i1);
    root.update()->indexObject(i2);

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
    search->onlyIds(ids(i1->getId()));
    // full text
    search->byFullText(uuid);
    assertResults(1, search);
    // annotated with
    byAnnotatedWith(search, tag);
    assertResults(1, search);

    // Restrict to both ids
    search->onlyIds(ids(i1->getId(), i2->getId()));
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

    // unrestrict
    search->onlyIds(omero::sys::LongList()); // ERROR
    // full text
    search->byFullText(uuid);
    assertResults(2, search);
    // annotated with
    byAnnotatedWith(search, tag);
    assertResults(2, search);
}

BOOST_AUTO_TEST_CASE( testOnlyOwnedByOwner ) {

    SearchFixture root("root");
    ExperimenterIPtr e = root.newUser();
    SearchFixture f(e->getOmeName()->val);
    DetailsIPtr user = new DetailsI();
    user->setOwner(e);

    string name = f.uuid();;
    ImageIPtr i = new ImageI();
    i->setName( new omero::RString(name) );
    TagAnnotationIPtr tag = new TagAnnotationI();
    tag->setTextValue(new omero::RString(name));
    i->linkAnnotation(tag);
    TagAnnotationIPtr grp = new TagAnnotationI();
    grp->setTextValue(new omero::RString(name));
    tag->linkAnnotation(grp);
    i = ImageIPtr::dynamicCast(f.update()->saveAndReturnObject(i));
    // Recreating instance as example
    tag = new TagAnnotationI();
    tag->setTextValue(new omero::RString(name));
    root.update()->indexObject(i);

    long id = f.admin()->getEventContext()->userId;
    ExperimenterIPtr self = new ExperimenterI(new omero::RLong(id), false);
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
    BOOST_CHECK( ! search->hasNext());
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

BOOST_AUTO_TEST_CASE( testOnlyOwnedByGroup ) {
    
    SearchFixture root("root");
    ExperimenterIPtr e = root.newUser();
    SearchFixture f(e->getOmeName()->val);
    ExperimenterGroupIPtr g = new ExperimenterGroupI
	(new omero::RLong(f.admin()->getEventContext()->groupId), false);
    
    DetailsIPtr user = new DetailsI();
    user->group = g;
    
    string name = f.uuid();
    ImageIPtr i = new ImageI();
    i->setName( new omero::RString(name) );
    TagAnnotationIPtr tag = new TagAnnotationI();
    tag->setTextValue(new omero::RString(name));
    i->linkAnnotation(tag);
    TagAnnotationIPtr grp = new TagAnnotationI();
    grp->setTextValue(new omero::RString(name));
    tag->linkAnnotation(grp);
    i = ImageIPtr::dynamicCast(f.update()->saveAndReturnObject(i));
    // Recreating instance as example
    tag = new TagAnnotationI();
    tag->setTextValue(new omero::RString(name));
    root.update()->indexObject(i);

    long id = f.admin()->getEventContext()->groupId;
    ExperimenterGroupIPtr self = new ExperimenterGroupI(new omero::RLong(id), false);
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
    BOOST_CHECK( ! search->hasNext());
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

omero::RTimePtr oneHourAgo() {
    time_t start = time (NULL);
    tm* mn = localtime(&start);
    mn->tm_hour = mn->tm_hour - 1;
    Ice::Long millis = mktime(mn) * 1000;
    return new omero::RTime(millis);
}

omero::RTimePtr inOneHour() {
    time_t start = time (NULL);
    tm* mn = localtime(&start);
    mn->tm_hour = mn->tm_hour + 1;
    Ice::Long millis = mktime(mn) * 1000;
    return new omero::RTime(millis);
}

omero::RTimePtr now() {
    time_t start = time (NULL);
    tm* mn = localtime(&start);
    Ice::Long millis = mktime(mn) * 1000;
    return new omero::RTime(millis);
}

BOOST_AUTO_TEST_CASE( testOnlyCreateBetween ) {
    SearchFixture f;
    SearchFixture root("root");
    string name = f.uuid();;
    ImageIPtr i = new ImageI();
    i->setName(new omero::RString(name));
    TagAnnotationIPtr tag = new TagAnnotationI();
    tag->setTextValue(new omero::RString(name));
    i->linkAnnotation(tag);
    TagAnnotationIPtr grp = new TagAnnotationI();
    grp->setTextValue(new omero::RString(name));
    tag->linkAnnotation(grp);
    i = ImageIPtr::dynamicCast(f.update()->saveAndReturnObject(i));
    tag = new TagAnnotationI();
    tag->setTextValue(new omero::RString(name));
    root.update()->indexObject(i);

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
    BOOST_CHECK( ! search->hasNext());
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
    BOOST_CHECK( ! search->hasNext());
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
    search->onlyCreatedBetween(omero::RTimePtr(), now());
    // full text
    search->byFullText(name);
    BOOST_CHECK( ! search->hasNext());
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

BOOST_AUTO_TEST_CASE( testOnlyModifiedBetween ) {

    // Ignored by
    // byTagForGroups, byGroupForTags (tags are immutable) results always 1

    SearchFixture f;
    SearchFixture root("root");
    string name = f.uuid();;
    ImageIPtr i = new ImageI();
    i->setName(new omero::RString(name));
    TagAnnotationIPtr tag = new TagAnnotationI();
    tag->setTextValue(new omero::RString(name));
    i->linkAnnotation(tag);
    TagAnnotationIPtr grp = new TagAnnotationI();
    grp->setTextValue(new omero::RString(name));
    tag->linkAnnotation(grp);
    i = ImageIPtr::dynamicCast(f.update()->saveAndReturnObject(i));
    tag = new TagAnnotationI();
    tag->setTextValue(new omero::RString(name));
    root.update()->indexObject(i);

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
    BOOST_CHECK( ! search->hasNext());
    // annotated with
    byAnnotatedWith(search, tag);
    assertResults(0, search);
    // group for tags
    search->byGroupForTags(name);
    assertResults(1, search);
    // tag for group
    search->byTagForGroups(name);
    assertResults(1, search);

    // Future
    search->onlyModifiedBetween(inOneHour(), omero::RTimePtr());
    // full text
    search->byFullText(name);
    BOOST_CHECK( ! search->hasNext());
    // annotated with
    byAnnotatedWith(search, tag);
    assertResults(0, search);
    // tag for group
    search->byTagForGroups(name);
    assertResults(1, search);
    // group for tags
    search->byGroupForTags(name);
    assertResults(1, search);

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
    search->onlyModifiedBetween(omero::RTimePtr(), now());
    // full text
    search->byFullText(name);
    BOOST_CHECK( ! search->hasNext());
    // annotated with
    byAnnotatedWith(search, tag);
    assertResults(0, search);
    // tag for group
    search->byTagForGroups(name);
    assertResults(1, search);
    // group for tags
    search->byGroupForTags(name);
    assertResults(1, search);

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

BOOST_AUTO_TEST_CASE( testOnlyAnnotatedBetween ) {

    SearchFixture f;
    SearchFixture root("root");
    string name = f.uuid();;
    ImageIPtr i = new ImageI();
    i->setName(new omero::RString(name));
    TagAnnotationIPtr tag = new TagAnnotationI();
    tag->setTextValue(new omero::RString(name));
    i->linkAnnotation(tag);
    TagAnnotationIPtr grp = new TagAnnotationI();
    grp->setTextValue(new omero::RString(name));
    tag->linkAnnotation(grp);
    i = ImageIPtr::dynamicCast(f.update()->saveAndReturnObject(i));
    tag = new TagAnnotationI();
    tag->setTextValue(new omero::RString(name));
    root.update()->indexObject(i);

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
    BOOST_CHECK( ! search->hasNext());
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
    BOOST_CHECK( ! search->hasNext());
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
    search->onlyAnnotatedBetween(omero::RTimePtr(), now());
    // full text
    search->byFullText(name);
    BOOST_CHECK( ! search->hasNext());
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

BOOST_AUTO_TEST_CASE( testOnlyAnnotatedBy ) {

    SearchFixture f;
    SearchFixture root("root");
    string name = f.uuid();
    string tag = f.uuid();
    ImageIPtr i = new ImageI();
    i->setName(new omero::RString(name));
    TagAnnotationIPtr t = new TagAnnotationI();
    t->setTextValue(new omero::RString(tag));
    i->linkAnnotation(t);
    TagAnnotationIPtr grp = new TagAnnotationI();
    grp->setTextValue(new omero::RString(tag));
    t->linkAnnotation(grp);
    i = ImageIPtr::dynamicCast(f.update()->saveAndReturnObject(i));
    t = new TagAnnotationI();
    t->setTextValue(new omero::RString(tag));
    root.update()->indexObject(i);

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
    ExperimenterIPtr e = root.newUser();
    DetailsIPtr d = new DetailsI();
    d->setOwner(e);
    search->onlyAnnotatedBy(d);
    search->notAnnotatedBy(DetailsIPtr());
    // full text
    search->byFullText(name);
    BOOST_CHECK( ! search->hasNext());
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

BOOST_AUTO_TEST_CASE( testOnlyAnnotatedWith ) {

    // ignored by byAnnotatedWith
    // ignored by byTagForGroups, byGroupForTags

    SearchFixture f;
    SearchFixture root("root");
    string name = f.uuid();
    ImageIPtr i = new ImageI();
    i->setName(new omero::RString(name));
    i = ImageIPtr::dynamicCast(f.update()->saveAndReturnObject(i));

    root.update()->indexObject(i);

    SearchPrx search = f.search();

    // Search for tagged image, which shouldn't be there
    search->onlyAnnotatedWith(stringSet("TagAnnotation"));
    search->onlyType("Image");
    search->byFullText(name);
    BOOST_CHECK( ! search->hasNext());

    // But if we ask for Images which aren't annotated it should appear
    search->onlyAnnotatedWith(StringSet());
    search->onlyType("Image");
    search->byFullText(name);
    assertResults(1, search);

    // Now let's tag it and see if it shows up
    TagAnnotationIPtr t = new TagAnnotationI();
    t->setTextValue(new omero::RString(f.uuid()));
    t = TagAnnotationIPtr::dynamicCast(f.update()->saveAndReturnObject(t));

    ImageAnnotationLinkIPtr link = new ImageAnnotationLinkI();
    link->setChild( t );
    link->setParent( i );
    f.update()->saveObject(link);

    root.update()->indexObject(i);

    // Since we're looking for "no annotations" there should be no results
    search->byFullText(name);
    BOOST_CHECK( ! search->hasNext());

    // And if we turn the annotations back on?
    search->onlyAnnotatedWith(stringSet("TagAnnotation"));
    search->byFullText(name);
    assertResults(1, search);
}

BOOST_AUTO_TEST_CASE( testOnlyAnnotatedWithMultiple ) {

    SearchFixture f;
    SearchFixture root("root");
    
    string name = f.uuid();;
    ImageIPtr onlyTag = new ImageI();
    onlyTag->setName( new omero::RString(name) );
    ImageIPtr onlyBool = new ImageI();
    onlyBool->setName( new omero::RString(name) );
    ImageIPtr both = new ImageI();
    both->setName( new omero::RString(name) );

    TagAnnotationIPtr tag = new TagAnnotationI();
    tag->setTextValue(new omero::RString("tag"));
    BooleanAnnotationIPtr b = new BooleanAnnotationI();
    b->setBoolValue(new omero::RBool(false));

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
	root.update()->indexObject(*beg++);
    }

    SearchPrx search = f.search();
    search->onlyType("Image");

    search->onlyAnnotatedWith(stringSet("TagAnnotation"));
    search->byFullText(name);
    BOOST_CHECK_EQUAL(2, search->results().size());

    search->onlyAnnotatedWith(stringSet("BooleanAnnotation"));
    search->byFullText(name);
    BOOST_CHECK_EQUAL(2, search->results().size());

    search->onlyAnnotatedWith(stringSet("BooleanAnnotation", "TagAnnotation"));
    search->byFullText(name);
    assertResults(1, search);

}

// other
// =========================================================================

BOOST_AUTO_TEST_CASE( testMergedBatches ) {

    SearchFixture f;
    SearchFixture root("root");
    string uuid1 = f.uuid();
    string uuid2 = f.uuid();
    ImageIPtr i1 = new ImageI();
    i1->setName( new omero::RString(uuid1) );
    ImageIPtr i2 = new ImageI();
    i2->setName( new omero::RString(uuid2) );
    i1 = ImageIPtr::dynamicCast( f.update()->saveAndReturnObject(i1) );
    i2 = ImageIPtr::dynamicCast( f.update()->saveAndReturnObject(i2) );
    root.update()->indexObject(i1);
    root.update()->indexObject(i2);

    SearchPrx search = f.search();
    search->onlyType("Image");
    search->byFullText(uuid1);
    assertResults(1, search);

    search->byFullText(uuid2);
    assertResults(1, search);

    // FIXME search->bySomeMustNone(sa(uuid1, uuid2), null, null);
    assertResults(2, search);

    // Everything looks ok, now try with batch
    search->setMergedBatches(true);
    search->byFullText(uuid1);
    search->byFullText(uuid2);
    assertResults(2, search);
}

/* FIXME
BOOST_AUTO_TEST_CASE ( testOrderBy ) {

    SearchFixture f;
    SearchFixture root("root");
    string uuid = f.uuid();
    TagAnnotationIPtr tag = new TagAnnotationI();
    tag->setTextValue(new omero::RString(uuid));
    ImageIPtr i1 = new ImageI();
    i1->setName(new omero::RString(uuid));
    i1->setDescription(new omero::RString("a"));
    i1->linkAnnotation(tag);
    ImageIPtr i2 = new ImageI();
    i2->setName(new omero::RString(uuid));
    i2->setDescription(new omero::RString("b"));
    i2->linkAnnotation(tag);
    i1 = ImageIPtr::dynamicCast(f.update()->saveAndReturnObject(i1));
    // FIXME Thread.sleep(2000L); // Waiting to test creation time ordering better
    i2 = ImageIPtr::dynamicCast(f.update()->saveAndReturnObject(i2));
    root.update()->indexObject(i1);
    root.update()->indexObject(i2);
    tag = new TagAnnotationI();
    tag->setTextValue(new omero::RString(uuid));

    SearchPrx search = f.search();
    search->onlyType("Image");

    // Order by description desc
    search->unordered();
    search->addOrderByDesc("description");
    // full text
    search->byFullText(uuid);
    StringSet desc;
    desc.push_back(i2->getDescription()->val);
    desc.push_back(i1->getDescription()->val);
    while (search->hasNext()) {
	BOOST_CHECK_EQUAL(desc.remove(0), ((Image) search->next())
		     .getDescription());
    }
    // annotated with
    byAnnotatedWith(search, tag);
    desc = new ArrayList<string>();
    desc.add(i2.getDescription());
    desc.add(i1.getDescription());
    while (search->hasNext()) {
	BOOST_CHECK_EQUAL(desc.remove(0), ((Image) search->next())
		     .getDescription());
    }

    // Order by descript asc
    search->unordered();
    search->addOrderByAsc("description");
    // full text
    search->byFullText(uuid);
    List<string> asc = new ArrayList<string>();
    asc.add(i1.getDescription());
    asc.add(i2.getDescription());
    while (search->hasNext()) {
	BOOST_CHECK_EQUAL(asc.remove(0), ((Image) search->next())
		     .getDescription());
    }
    // annotated with
    byAnnotatedWith(search, tag);
    asc = new ArrayList<string>();
    asc.add(i1.getDescription());
    asc.add(i2.getDescription());
    while (search->hasNext()) {
	BOOST_CHECK_EQUAL(asc.remove(0), ((Image) search->next())
		     .getDescription());
    }

    // Ordered by id
    search->unordered();
    search->addOrderByDesc("id");
    // full text
    search->byFullText(uuid);
    List<Long> ids = new ArrayList<Long>();
    ids.add(i2.getId());
    ids.add(i1.getId());
    while (search->hasNext()) {
	BOOST_CHECK_EQUAL(ids.remove(0), search->next().getId());
    }
    // annotated with
    byAnnotatedWith(search, tag);
    ids = new ArrayList<Long>();
    ids.add(i2.getId());
    ids.add(i1.getId());
    while (search->hasNext()) {
	BOOST_CHECK_EQUAL(ids.remove(0), search->next().getId());
    }

    // Ordered by creation event id
    search->unordered();
    search->addOrderByDesc("details.creationEvent.id");
    // full text
    search->byFullText(uuid);
    ids = new ArrayList<Long>();
    ids.add(i2.getId());
    ids.add(i1.getId());
    while (search->hasNext()) {
	BOOST_CHECK_EQUAL(ids.remove(0), search->next().getId());
    }
    // annotated with
    byAnnotatedWith(search, tag);
    ids = new ArrayList<Long>();
    ids.add(i2.getId());
    ids.add(i1.getId());
    while (search->hasNext()) {
	BOOST_CHECK_EQUAL(ids.remove(0), search->next().getId());
    }

    // ordered by creation event time
    search->unordered();
    search->addOrderByDesc("details.creationEvent.time");
    // full text
    search->byFullText(uuid);
    ids = new ArrayList<Long>();
    ids.add(i2.getId());
    ids.add(i1.getId());
    while (search->hasNext()) {
	BOOST_CHECK_EQUAL(ids.remove(0), search->next().getId());
    }
    // annotated with
    byAnnotatedWith(search, tag);
    ids = new ArrayList<Long>();
    ids.add(i2.getId());
    ids.add(i1.getId());
    while (search->hasNext()) {
	BOOST_CHECK_EQUAL(ids.remove(0), search->next().getId());
    }

    // To test multiple sort fields, we add another image with an "a"
    // description, which should could before the other image with the "a"
    // description if we reverse the id order

    Image i3 = new ImageI();
    i3->setName(new omero::RString(uuid));
    i3->setDescription("a");
    i3->linkAnnotation(tag);
    i3 = f.update()->saveAndReturnObject(i3);
    root.update()->indexObject(i3);
    loginRoot();
    tag = new TagAnnotationI();
    tag->setTextValue(new omero::RString(uuid));

    // multi-ordering
    search->unordered();
    search->addOrderByAsc("description");
    search->addOrderByDesc("id");
    // annotated with
    byAnnotatedWith(search, tag);
    List<Long> multi = new ArrayList<Long>();
    multi.add(i3.getId());
    multi.add(i1.getId());
    multi.add(i2.getId());
    while (search->hasNext()) {
	BOOST_CHECK_EQUAL(multi.remove(0), search->next().getId());
    }
    // full text
    search->byFullText(uuid);
    multi = new ArrayList<Long>();
    multi.add(i3.getId());
    multi.add(i1.getId());
    multi.add(i2.getId());
    while (search->hasNext()) {
	BOOST_CHECK_EQUAL(multi.remove(0), search->next().getId());
    }

}
*/

BOOST_AUTO_TEST_CASE( testFetchAnnotations ) {
    SearchFixture f;
    SearchFixture root("root");
    string uuid = f.uuid();;
    ImageIPtr i = new ImageI();
    i->setName(new omero::RString(uuid));
    TagAnnotationIPtr tag = new TagAnnotationI();
    tag->setTextValue(new omero::RString(uuid));
    LongAnnotationIPtr la = new LongAnnotationI();
    la->setLongValue(new omero::RLong(1L));
    DoubleAnnotationIPtr da = new DoubleAnnotationI();
    da->setDoubleValue(new omero::RDouble(0.0));
    i->linkAnnotation(tag);
    i->linkAnnotation(la);
    i->linkAnnotation(da);
    i = ImageIPtr::dynamicCast(f.update()->saveAndReturnObject(i));
    tag = new TagAnnotationI();
    tag->setTextValue(new omero::RString(uuid));
    root.update()->indexObject(i);

    SearchPrx search = f.search();
    search->onlyType("Image");

    // No fetch returns empty annotations
    // full text
    search->byFullText(uuid);
    ImageIPtr t = ImageIPtr::dynamicCast( search->results().at(0) );
    BOOST_CHECK_EQUAL(-1, t->sizeOfAnnotationLinks());
    // annotated with
    byAnnotatedWith(search, tag);
    t = ImageIPtr::dynamicCast( search->results().at(0) );
    BOOST_CHECK_EQUAL(-1, t->sizeOfAnnotationLinks());

    // Fetch only a given type
    search->fetchAnnotations(stringSet("TagAnnotation"));
    // annotated with
    byAnnotatedWith(search, tag);
    t = ImageIPtr::dynamicCast( search->results().at(0) );
    BOOST_CHECK_EQUAL(1, t->sizeOfAnnotationLinks());
    // full text
    search->byFullText(uuid);
    t = ImageIPtr::dynamicCast( search->results().at(0) );
    BOOST_CHECK_EQUAL(3, t->sizeOfAnnotationLinks());

    // fetch only a given type different from annotated-with type
    search->fetchAnnotations(stringSet("DoubleAnnotation"));
    // annotated with
    byAnnotatedWith(search, tag);
    t = ImageIPtr::dynamicCast( search->results().at(0) );
    BOOST_CHECK_EQUAL(1, t->sizeOfAnnotationLinks());
    // full text
    search->byFullText(uuid);
    t = ImageIPtr::dynamicCast( search->results().at(0) );
    BOOST_CHECK_EQUAL(3, t->sizeOfAnnotationLinks());

    // fetch two types
    search->fetchAnnotations(stringSet("TagAnnotation", "DoubleAnnotation"));
    // annotated with
    byAnnotatedWith(search, tag);
    t = ImageIPtr::dynamicCast( search->results().at(0) );
    BOOST_CHECK_EQUAL(2, t->sizeOfAnnotationLinks());
    // full text
    search->byFullText(uuid);
    t = ImageIPtr::dynamicCast( search->results().at(0) );
    BOOST_CHECK_EQUAL(3, t->sizeOfAnnotationLinks());

    // Fetch all
    search->fetchAnnotations(stringSet("Annotation"));
    // annotated with
    byAnnotatedWith(search, tag);
    assertResults(0, search);
    // TODO t = ImageIPtr::dynamicCast( search->results().get(0) );
    // TODO BOOST_CHECK_EQUAL(3, t->sizeOfAnnotationLinks());
    // full text
    search->byFullText(uuid);
    t = ImageIPtr::dynamicCast( search->results().at(0) );
    BOOST_CHECK_EQUAL(3, t->sizeOfAnnotationLinks());

    // resave and see if there is data loss
    search->fetchAnnotations(stringSet("TagAnnotation"));
    byAnnotatedWith(search, tag);
    t = ImageIPtr::dynamicCast(search->next());
    FileAnnotationIPtr fa = new FileAnnotationI();
    t->linkAnnotation(fa);
    f.update()->saveObject(t);
    ParametersPtr params = new Parameters();
    params->map = ParamMap();
    params->map["id"] = t->getId();
    t = ImageIPtr::dynamicCast(f.query()->findByQuery
	("select t from Image t join fetch t.annotationLinks where t.id = :id",
	 params));
    BOOST_CHECK_EQUAL(4, t->sizeOfAnnotationLinks());
}

// bugs
// =========================================================================

BOOST_AUTO_TEST_CASE( testTextAnnotationDoesntTryToLoadUpdateEvent ) {
    SearchFixture f;
    SearchFixture root("root");
    string uuid = f.uuid();;
    TextAnnotationIPtr ta = new TextAnnotationI();
    ta->setTextValue(new omero::RString(uuid));
    ta = TextAnnotationIPtr::dynamicCast(f.update()->saveAndReturnObject(ta));
    root.update()->indexObject(ta);

    SearchPrx search = f.search();
    search->onlyType("TextAnnotation");
    search->byFullText(uuid);
    assertResults(1, search);
}

BOOST_AUTO_TEST_CASE( testExperimenterDoesntTryToLoadOwner ) {
    SearchFixture f;
    SearchPrx search = f.search();
    search->onlyType("Experimenter");
    search->byFullText("root");
    assertAtLeastResults(1, search);
}

BOOST_AUTO_TEST_CASE( testLookingForExperimenterWithOwner ) {
    SearchFixture f;
    SearchPrx search = f.search();
    search->onlyType("Experimenter");
    
    // Just root should work
    search->byFullText("root");
    search->next();

    // And filtered on "owner" (experimenter has none) should work, too.
    DetailsIPtr d = new DetailsI();
    d->setOwner(new ExperimenterI(0L, false));
    search->onlyOwnedBy(d);
    search->byFullText("root");
    search->next();
}
