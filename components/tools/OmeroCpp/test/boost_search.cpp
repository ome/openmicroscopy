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

using namespace std;
using namespace omero;
using namespace omero::api;
using namespace omero::model;
using namespace omero::sys;

/*
 * Clears one result from the current queue.
 */
#define assertResults(count, search) _assertResults(__LINE__, count, search) 

void _assertResults(int line, int count, SearchPrx search) {
    stringstream out;
    out << "line " << line << ":";
    if (count  > 0) {
	out << "Search should have results" << endl;
        BOOST_CHECK_MESSAGE( search->hasNext(), out.str());
        if (search->hasNext()) {
            BOOST_CHECK_EQUAL( count, search->results().size() );
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
        Fixture f;

	const omero::client* client = f.login();
	ServiceFactoryPrx sf = (*client).getSession();
        SearchPrx search = sf->createSearchService();
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
        Fixture f;

	const omero::client* client = f.login();
	ServiceFactoryPrx sf = (*client).getSession();
        IUpdatePrx update = sf->getUpdateService();

        string uuid(IceUtil::generateUUID());
        ImageIPtr i = new ImageI();
        i->setName(new omero::RString(uuid));
	i = ImageIPtr::dynamicCast( update->saveAndReturnObject(i) );

	/*
	 *
	 */
	try {
	    const omero::client* root = f.root_login();
	    (*root).getSession()->getUpdateService()->indexObject(i);
	} catch (const Glacier2::PermissionDeniedException& pde) {
	    BOOST_ERROR("permission denied:"+pde.reason);
	}

	/*
	 * IQuery provides a simple, stateless method for search
	 */
        IObjectList list;
        list = sf->getQueryService()->findAllByFullText("Image",uuid,0);
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
        Fixture f;

	const omero::client* client = f.login();
        const omero::client* root = f.root_login();

	ServiceFactoryPrx sf = (*client).getSession();

	string uuid = f.uuid();
	ImageIPtr i = new ImageI();
        i->name = new RString(uuid);

        IObjectPtr obj =  sf->getUpdateService()->saveAndReturnObject(i);
        (*root).getSession()->getUpdateService()->indexObject(obj);

        SearchPrx search = sf->createSearchService();
        search->onlyType("Image");

        // Search without filter
        search->byFullText(uuid);
        assertResults(1, search);

        // Add id filter
        LongList ids;
        ids.push_back(obj->id->val);
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

/*
BOOST_AUTO_TEST_CASE ( testSerialization ) {
    Fixture f;
    const omero::client* client = f.login();
    SearchPrx search = (*client).getSession()->createSearchService();

    search->onlyType("Experimenter");
    search.byFullText("root");
    search.hasNext();
    Search internal = search;
    while (internal instanceof Advised) {
	internal = (Search) ((Advised) search).getTargetSource()
	    .getTarget();
    }
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(baos);
    oos.writeObject(internal);
}

// by<Query>
// =========================================================================
// This section tests each query method with various combinations of
// restrictions

BOOST_AUTO_TEST_CASE ( testByGroupForTags ) {
    String groupStr = uuid();
    String tagStr = uuid();

    TagAnnotation tag = new TagAnnotation();
    tag.setTextValue(tagStr);

    TagAnnotation grp = new TagAnnotation();
    grp.setTextValue(groupStr);

    tag.linkAnnotation(grp);
    tag = iUpdate.saveAndReturnObject(tag);

    Fixture f;
    const omero::client* client = f.login();
    SearchPrx search = (*client).getSession()->createSearchService();
    search.byGroupForTags(groupStr);
    assertEquals(1, search.results().size());

    // Make another one
    groupStr = uuid();
    grp = new TagAnnotation();
    tag.linkAnnotation(grp);
    tag = iUpdate.saveAndReturnObject(tag);

    // Now we are sure that there are two taggroups in the db;
    // this should return all two then
    search.byGroupForTags(null);
    search.setBatchSize(2);
    assertEquals(2, search.results().size());
    while (search.hasNext()) {
	search.results(); // Clear search
    }

    // Let's now add the tag to another tag group as another user
    // and try to filter out those results

    long oldUser = iAdmin.getEventContext().getCurrentUserId();
    Details d = Details.create();
    d.setOwner(new Experimenter(oldUser, false));

    Experimenter e = loginNewUser();
    grp = new TagAnnotation();
    groupStr = uuid();
    grp.setTextValue(groupStr);
    tag.linkAnnotation(grp);
    tag = iUpdate.saveAndReturnObject(tag);

    // All queries finished?
    assertEquals(0, search.activeQueries());
    assertFalse(search.hasNext());

    search.onlyOwnedBy(d);
    search.byGroupForTags(groupStr);
    assertFalse(search.hasNext());

    d.setOwner(e);
    search.onlyOwnedBy(d);
    search.byGroupForTags(groupStr);
    assertEquals(1, search.results().size());

    search.onlyOwnedBy(null);
    search.byGroupForTags(groupStr);
    assertEquals(1, search.results().size());
}

BOOST_AUTO_TEST_CASE( testByTagForGroup ) {
    String groupStr = uuid();
    String tagStr = uuid();

    TagAnnotation tag = new TagAnnotation();
    tag.setTextValue(tagStr);

    TagAnnotation grp = new TagAnnotation();
    grp.setTextValue(groupStr);

    tag.linkAnnotation(grp);
    tag = iUpdate.saveAndReturnObject(tag);

    Fixture f;
const omero::client* client = f.login();
SearchPrx search = (*client).getSession()->createSearchService();
    search.byTagForGroups(tagStr);
    assertEquals(1, search.results().size());

    // Make another one
    tagStr = uuid();
    tag = new TagAnnotation();
    tag.linkAnnotation(grp);
    tag = iUpdate.saveAndReturnObject(tag);

    // Now we are sure that there are two tags for the one group;
    // this should return all two then
    search.byTagForGroups(null);
    search.setBatchSize(2);
    assertEquals(2, search.results().size());
    while (search.hasNext()) {
	search.results(); // Clear search
    }

    // Let's now add another tag to the tag group as another user
    // and try to filter out those results

    long oldUser = iAdmin.getEventContext().getCurrentUserId();
    Details d = Details.create();
    d.setOwner(new Experimenter(oldUser, false));

    Experimenter e = loginNewUser();
    tag = new TagAnnotation();
    tagStr = uuid();
    tag.setTextValue(tagStr);
    tag.linkAnnotation(grp.proxy());
    tag = iUpdate.saveAndReturnObject(tag);

    // All queries finished?
    assertEquals(0, search.activeQueries());
    assertFalse(search.hasNext());

    search.onlyOwnedBy(d);
    search.byTagForGroups(tagStr);
    assertFalse(search.hasNext());

    d.setOwner(e);
    search.onlyOwnedBy(d);
    search.byTagForGroups(tagStr);
    assertEquals(1, search.results().size());

    search.onlyOwnedBy(null);
    search.byTagForGroups(tagStr);
    assertEquals(1, search.results().size());

}

BOOST_AUTO_TEST_CASE( testSimpleFullTextSearch ) {

    Image i = new Image();
    i.setName(uuid());
    i = iUpdate.saveAndReturnObject(i);

    iUpdate.indexObject(i);
    loginRoot();

    Fixture f;
const omero::client* client = f.login();
SearchPrx search = (*client).getSession()->createSearchService();
    search.onlyType(Image.class);
    search.byFullText(i.getName());
    int count = 0;
    while (search.hasNext()) {
	IObject obj = search.next();
	count++;
	assertNotNull(obj);
    }
    assertTrue(count == 1);
    search.close();

    search.onlyType(Image.class);
    search.byFullText(i.getName());
    assertResults(search, 1);

    search.close();
}

String[] sa(String... arr) {
    return arr;
}

BOOST_AUTO_TEST_CASE( testSomeMustNone ) {
    final String[] contained = new String[] { "abc", "def", "ghi", "123" };
    final String[] missing = new String[] { "jkl", "mno", "pqr", "456" };

    Image i = new Image();
    i.setName("abc def ghi");
    i = iUpdate.saveAndReturnObject(i);
    iUpdate.indexObject(i);
    loginRoot();

    final Fixture f;
const omero::client* client = f.login();
SearchPrx search = (*client).getSession()->createSearchService();
    search.onlyType(Image.class);

    // Make sure we can find it simply
    search.bySomeMustNone(sa("abc"), sa(), sa());
    assertTrue(search.results().size() >= 1);

    //
    // Now we'll try more complicated queries
    //

    // This should return nothing since none is contained
    search.bySomeMustNone(sa("abc"), sa(), sa("def"));
    assertResults(search, 0);

    // but if the none is not contained should be ok.
    search.bySomeMustNone(sa("abc"), sa("abc"), sa("jkl"));
    assertAtLeastResults(search, 1);

    // Simple must query
    search.bySomeMustNone(sa(), sa("abc"), sa());
    assertAtLeastResults(search, 1);

    // same, but with a matching none
    search.bySomeMustNone(sa(), sa("abc"), sa("def"));
    assertResults(search, 0);

    // same again, but with non-matching none
    search.bySomeMustNone(sa(), sa("abc"), sa("jkl"));
    assertAtLeastResults(search, 1);

    //
    // Mixing some and must
    //

    // Present must
    search.bySomeMustNone(sa("abc"), sa("def"), sa());
    assertAtLeastResults(search, 1);

    // Missing must
    search.bySomeMustNone(sa("abc"), sa("jkl"), sa());
    assertResults(search, 0);

    // Present must, missing some
    search.bySomeMustNone(sa("jkl"), sa("def"), sa());
    assertAtLeastResults(search, 1);

    //
    // Using wildcards
    //

    // some with wildcard
    search.bySomeMustNone(sa("ab*"), sa(), sa());
    assertAtLeastResults(search, 1);

    // must with wildcard
    search.bySomeMustNone(sa(), sa("ab*"), sa());
    assertAtLeastResults(search, 1);

    // none with wildcard
    search.bySomeMustNone(sa(), sa(), sa("ab*"));
    assertResults(search, 0);

    //
    // Multiterms
    //

    search.bySomeMustNone(sa("abc", "def"), null, null);
    assertAtLeastResults(search, 1);

    search.bySomeMustNone(null, sa("abc", "def"), null);
    assertAtLeastResults(search, 1);

    search.bySomeMustNone(null, null, sa("abc", "def"));
    assertResults(search, 0);

    search.bySomeMustNone(sa("ghi", "123"), sa("abc", "def"), null);
    assertAtLeastResults(search, 1);

    search.bySomeMustNone(sa("ghi", "123"), sa("abc", "def"), sa("456"));
    assertAtLeastResults(search, 1);

    search.bySomeMustNone(sa("ghi", "123"), sa("abc", "456"), sa("456"));
    assertResults(search, 0);

    //
    // Completely empty
    //
    try {
	search.bySomeMustNone(null, null, null);
	fail("Should throw");
    } catch (ApiUsageException aue) {
	// ok
    }

    try {
	search.bySomeMustNone(sa(), null, null);
	fail("Should throw");
    } catch (ApiUsageException aue) {
	// ok
    }

    try {
	search.bySomeMustNone(sa(""), null, null);
	fail("Should throw");
    } catch (ApiUsageException aue) {
	// ok
    }

    //
    // Queries with spaces
    // For the moment these return as expected since the parser splits into
    // keywords.
    //
    search.bySomeMustNone(sa("\"abc def\""), null, null);
    assertAtLeastResults(search, 1);
}

BOOST_AUTO_TEST_CASE( testAnnotatedWith ) {

    String uuid = uuid();
    Image i = new Image(uuid);
    TagAnnotation tag = new TagAnnotation();
    tag.setTextValue(uuid);
    i.linkAnnotation(tag);
    i = iUpdate.saveAndReturnObject(i);
    iUpdate.indexObject(i);
    loginRoot();

    Fixture f;
const omero::client* client = f.login();
SearchPrx search = (*client).getSession()->createSearchService();
    search.onlyType(Image.class);
    TagAnnotation example = new TagAnnotation();
    example.setTextValue(uuid);
    search.byAnnotatedWith(example);

    assertResults(search, 1);

    OriginalFile file1 = ObjectFactory.createFile();
    file1 = iUpdate.saveAndReturnObject(file1);
    OriginalFile file2 = ObjectFactory.createFile();
    file2 = iUpdate.saveAndReturnObject(file2);
    FileAnnotation fa1 = new FileAnnotation();
    fa1.setFile(file1);
    i.linkAnnotation(fa1);
    FileAnnotation fa2 = new FileAnnotation();
    fa2.setFile(file2);
    i.linkAnnotation(fa2);
    i = iUpdate.saveAndReturnObject(i);
    iUpdate.indexObject(i);
    loginRoot();

    // Properly uses the id
    FileAnnotation ex2 = new FileAnnotation();
    ex2.setFile(new OriginalFile(file2.getId(), false));
    search.byAnnotatedWith(ex2);
    assertResults(search, 1);

    // Now check if an empty example return results
    search.byAnnotatedWith(new FileAnnotation());
    assertAtLeastResults(search, 1);

    // Finding by superclass
    TextAnnotation txtAnn = new TextAnnotation();
    txtAnn.setTextValue(uuid);
    search.byAnnotatedWith(txtAnn);
    assertResults(search, 1);
}

BOOST_AUTO_TEST_CASE( testAnnotatedWithNamespace ) {
    fail("via namespace");
}

BOOST_AUTO_TEST_CASE( testAnnotatedWithMultiple ) {
    Image i1 = new Image("i1");
    Image i2 = new Image("i2");

    String uuid = uuid();
    TagAnnotation ta = new TagAnnotation();
    ta.setTextValue(uuid);
    BooleanAnnotation ba = new BooleanAnnotation();
    ba.setBoolValue(false);
    i1.linkAnnotation(ta);
    i2.linkAnnotation(ta);
    i2.linkAnnotation(ba);

    i1 = iUpdate.saveAndReturnObject(i1);
    i2 = iUpdate.saveAndReturnObject(i2);

    ta = new TagAnnotation();
    ta.setTextValue(uuid);
    ba = new BooleanAnnotation();
    ba.setBoolValue(false);

    Fixture f;
const omero::client* client = f.login();
SearchPrx search = (*client).getSession()->createSearchService();
    search.onlyType(Image.class);

    search.byAnnotatedWith(ta);
    assertResults(search, 2);

    search.byAnnotatedWith(ba);
    assertAtLeastResults(search, 1);

    search.byAnnotatedWith(ta, ba);
    assertResults(search, 1);

}

// restrictions methods
// ========================================================================
// The tests in the following sections should include all the by* methods
// each testing a specific restriction

BOOST_AUTO_TEST_CASE( testOnlyIds ) {

    // ignored by
    // byTagForGroups, byGroupForTags

    String uuid = uuid();
    Image i1 = new Image(uuid);
    Image i2 = new Image(uuid);
    TagAnnotation tag = new TagAnnotation();
    tag.setTextValue(uuid);
    i1.linkAnnotation(tag);
    i2.linkAnnotation(tag);
    i1 = iUpdate.saveAndReturnObject(i1);
    i2 = iUpdate.saveAndReturnObject(i2);
    tag = new TagAnnotation();
    tag.setTextValue(uuid);
    iUpdate.indexObject(i1);
    iUpdate.indexObject(i2);
    loginRoot();

    Fixture f;
const omero::client* client = f.login();
SearchPrx search = (*client).getSession()->createSearchService();
    search.onlyType(Image.class);

    // Regular search
    // full text
    search.byFullText(uuid);
    assertResults(search, 2);
    // annotated with
    search.byAnnotatedWith(tag);
    assertResults(search, 2);

    // Restrict to one id
    search.onlyIds(i1.getId());
    // full text
    search.byFullText(uuid);
    assertResults(search, 1);
    // annotated with
    search.byAnnotatedWith(tag);
    assertResults(search, 1);

    // Restrict to both ids
    search.onlyIds(i1.getId(), i2.getId());
    // full text
    search.byFullText(uuid);
    assertResults(search, 2);
    // annotated with
    search.byAnnotatedWith(tag);
    assertResults(search, 2);

    // Restrict to unknown ids
    search.onlyIds(-1L, -2L, -3L);
    // full text
    search.byFullText(uuid);
    assertResults(search, 0);
    // annotated with
    search.byAnnotatedWith(tag);
    assertResults(search, 0);

    // unrestrict
    search.onlyIds(null);
    // full text
    search.byFullText(uuid);
    assertResults(search, 2);
    // annotated with
    search.byAnnotatedWith(tag);
    assertResults(search, 2);
}

BOOST_AUTO_TEST_CASE( testOnlyOwnedByOwner ) {

    Experimenter e = loginNewUser();
    Details user = Details.create();
    user.setOwner(e);

    String name = uuid();
    Image i = new Image(name);
    TagAnnotation tag = new TagAnnotation();
    tag.setTextValue(name);
    i.linkAnnotation(tag);
    TagAnnotation grp = new TagAnnotation();
    grp.setTextValue(name);
    tag.linkAnnotation(grp);
    i = iUpdate.saveAndReturnObject(i);
    // Recreating instance as example
    tag = new TagAnnotation();
    tag.setTextValue(name);
    iUpdate.indexObject(i);

    loginRoot();
    long id = iAdmin.getEventContext().getCurrentUserId();
    Experimenter self = new Experimenter(id, false);
    Details root = Details.create();
    root.setOwner(self);

    Fixture f;
const omero::client* client = f.login();
SearchPrx search = (*client).getSession()->createSearchService();
    search.onlyType(Image.class);

    // With no restriction it should be found.
    // full text
    search.byFullText(name);
    assertEquals(1, search.results().size());
    // annotated with
    search.byAnnotatedWith(tag);
    assertResults(search, 1);
    // tag for group
    search.byTagForGroups(name);
    assertResults(search, 1);
    // group for tag
    search.byGroupForTags(name);
    assertResults(search, 1);

    // Restrict only to root, and then shouldn't be found
    search.notOwnedBy(null);
    search.onlyOwnedBy(root);
    // full text
    search.byFullText(name);
    assertFalse(search.hasNext());
    // annotated with
    search.byAnnotatedWith(tag);
    assertResults(search, 0);
    // tag for group
    search.byTagForGroups(name);
    assertResults(search, 0);
    // group for tags
    search.byGroupForTags(name);
    assertResults(search, 0);

    // Restrict to not root, and then should be found again.
    search.onlyOwnedBy(null);
    search.notOwnedBy(root);
    // full text
    search.byFullText(name);
    assertResults(search, 1);
    // annotated with
    search.byAnnotatedWith(tag);
    assertResults(search, 1);
    // tag for group
    search.byTagForGroups(name);
    assertResults(search, 1);
    // group for tags
    search.byGroupForTags(name);
    assertResults(search, 1);

    // Now restrict to the user, and again one
    search.notOwnedBy(null);
    search.onlyOwnedBy(user);
    // full text
    search.byFullText(name);
    assertEquals(1, search.results().size());
    // annotated with
    search.byAnnotatedWith(tag);
    assertResults(search, 1);
    // tag for group
    search.byTagForGroups(name);
    assertResults(search, 1);
    // group for tags
    search.byGroupForTags(name);
    assertResults(search, 1);

    // But not-user should return nothing
    search.notOwnedBy(user);
    search.onlyOwnedBy(null);
    // full text
    search.byFullText(name);
    assertResults(search, 0);
    // annotated with
    search.byAnnotatedWith(tag);
    assertResults(search, 0);
    // tag for group
    search.byTagForGroups(name);
    assertResults(search, 0);
    // group for tags
    search.byGroupForTags(name);
    assertResults(search, 0);
}

BOOST_AUTO_TEST_CASE( testOnlyOwnedByGroup ) {

    Experimenter e = loginNewUser();
    ExperimenterGroup g = new ExperimenterGroup(iAdmin.getEventContext()
						.getCurrentGroupId(), false);
    Details user = Details.create();
    user.setGroup(g);

    String name = uuid();
    Image i = new Image(name);
    TagAnnotation tag = new TagAnnotation();
    tag.setTextValue(name);
    i.linkAnnotation(tag);
    TagAnnotation grp = new TagAnnotation();
    grp.setTextValue(name);
    tag.linkAnnotation(grp);
    i = iUpdate.saveAndReturnObject(i);
    // Recreating instance as example
    tag = new TagAnnotation();
    tag.setTextValue(name);
    iUpdate.indexObject(i);

    loginRoot();
    long id = iAdmin.getEventContext().getCurrentGroupId();
    ExperimenterGroup self = new ExperimenterGroup(id, false);
    Details root = Details.create();
    root.setGroup(self);

    Fixture f;
const omero::client* client = f.login();
SearchPrx search = (*client).getSession()->createSearchService();
    search.onlyType(Image.class);

    // With no restriction it should be found.
    // full text
    search.byFullText(name);
    assertEquals(1, search.results().size());
    // annotated with
    search.byAnnotatedWith(tag);
    assertResults(search, 1);
    // tag for group
    search.byTagForGroups(name);
    assertResults(search, 1);
    // group for tags
    search.byGroupForTags(name);
    assertResults(search, 1);

    // Restrict only to root, and then shouldn't be found
    search.onlyOwnedBy(root);
    search.notOwnedBy(null);
    // full text
    search.byFullText(name);
    assertFalse(search.hasNext());
    // annotated with
    search.byAnnotatedWith(tag);
    assertResults(search, 0);
    // tag for group
    search.byTagForGroups(name);
    assertResults(search, 0);
    // group for tags
    search.byGroupForTags(name);
    assertResults(search, 0);

    // Restrict to not root, and then should be found again.
    search.onlyOwnedBy(null);
    search.notOwnedBy(root);
    // full text
    search.byFullText(name);
    assertResults(search, 1);
    // annotated with
    search.byAnnotatedWith(tag);
    assertResults(search, 1);
    // tag for group
    search.byTagForGroups(name);
    assertResults(search, 1);
    // group for tags
    search.byGroupForTags(name);
    assertResults(search, 1);

    // Now restrict to the user, and again one
    search.onlyOwnedBy(user);
    search.notOwnedBy(null);
    // full text
    search.byFullText(name);
    assertEquals(1, search.results().size());
    // annotated with
    search.byAnnotatedWith(tag);
    assertResults(search, 1);
    // tag for group
    search.byTagForGroups(name);
    assertResults(search, 1);
    // group for tags
    search.byGroupForTags(name);
    assertResults(search, 1);

    // But not-user should return nothing
    search.notOwnedBy(user);
    search.onlyOwnedBy(null);
    // full text
    search.byFullText(name);
    assertResults(search, 0);
    // annotated with
    search.byAnnotatedWith(tag);
    assertResults(search, 0);
    // tag for group
    search.byTagForGroups(name);
    assertResults(search, 0);
    // group for tags
    search.byGroupForTags(name);
    assertResults(search, 0);
}

static Timestamp oneHourAgo, inOneHour, now;
static {
    Calendar today = Calendar.getInstance();
    today.set(Calendar.HOUR, today.get(Calendar.HOUR) - 1);
    oneHourAgo = new Timestamp(today.getTimeInMillis());
    today = Calendar.getInstance();
    today.set(Calendar.HOUR, today.get(Calendar.HOUR) + 1);
    inOneHour = new Timestamp(today.getTimeInMillis());
    now = new Timestamp(System.currentTimeMillis());
}

BOOST_AUTO_TEST_CASE( testOnlyCreateBetween ) {

    String name = uuid();
    Image i = new Image();
    i.setName(name);
    TagAnnotation tag = new TagAnnotation();
    tag.setTextValue(name);
    i.linkAnnotation(tag);
    TagAnnotation grp = new TagAnnotation();
    grp.setTextValue(name);
    tag.linkAnnotation(grp);
    i = iUpdate.saveAndReturnObject(i);
    tag = new TagAnnotation();
    tag.setTextValue(name);
    iUpdate.indexObject(i);
    loginRoot();

    Fixture f;
const omero::client* client = f.login();
SearchPrx search = (*client).getSession()->createSearchService();
    search.onlyType(Image.class);

    // Find the Image
    // full text
    search.byFullText(name);
    assertEquals(1, search.results().size());
    // annotated with
    search.byAnnotatedWith(tag);
    assertResults(search, 1);
    // tag for group
    search.byTagForGroups(name);
    assertResults(search, 1);
    // group for tags
    search.byGroupForTags(name);
    assertResults(search, 1);

    // Now restrict the search to past
    search.onlyCreatedBetween(null, oneHourAgo);
    // full text
    search.byFullText(name);
    assertFalse(search.hasNext());
    // annotated with
    search.byAnnotatedWith(tag);
    assertResults(search, 0);
    // tag for group
    search.byTagForGroups(name);
    assertResults(search, 0);
    // group for tags
    search.byGroupForTags(name);
    assertResults(search, 0);

    // Future
    search.onlyCreatedBetween(inOneHour, null);
    // full text
    search.byFullText(name);
    assertFalse(search.hasNext());
    // annotated with
    search.byAnnotatedWith(tag);
    assertResults(search, 0);
    // tag for group
    search.byTagForGroups(name);
    assertResults(search, 0);
    // group for tags
    search.byGroupForTags(name);
    assertResults(search, 0);

    // 2 hour period around now
    search.onlyCreatedBetween(oneHourAgo, inOneHour);
    // full text
    search.byFullText(name);
    assertEquals(1, search.results().size());
    // annotated with
    search.byAnnotatedWith(tag);
    assertResults(search, 1);
    // tag for group
    search.byTagForGroups(name);
    assertResults(search, 1);
    // group for tags
    search.byGroupForTags(name);
    assertResults(search, 1);

    // Starting at now old 'now'
    search.onlyCreatedBetween(null, now);
    // full text
    search.byFullText(name);
    assertFalse(search.hasNext());
    // annotated with
    search.byAnnotatedWith(tag);
    assertResults(search, 0);
    // tag for group
    search.byTagForGroups(name);
    assertResults(search, 0);
    // group for tags
    search.byGroupForTags(name);
    assertResults(search, 0);

    // Open them up again and should be found
    search.onlyCreatedBetween(null, null);
    // full text
    search.byFullText(name);
    assertEquals(1, search.results().size());
    // annotated with
    search.byAnnotatedWith(tag);
    assertResults(search, 1);
    // tag for group
    search.byTagForGroups(name);
    assertResults(search, 1);
    // group for tags
    search.byGroupForTags(name);
    assertResults(search, 1);
}

BOOST_AUTO_TEST_CASE( testOnlyModifiedBetween ) {

    // Ignored by
    // byTagForGroups, byGroupForTags (tags are immutable) results always 1

    String name = uuid();
    Image i = new Image();
    i.setName(name);
    TagAnnotation tag = new TagAnnotation();
    tag.setTextValue(name);
    i.linkAnnotation(tag);
    TagAnnotation grp = new TagAnnotation();
    grp.setTextValue(name);
    tag.linkAnnotation(grp);
    i = iUpdate.saveAndReturnObject(i);
    tag = new TagAnnotation();
    tag.setTextValue(name);
    iUpdate.indexObject(i);
    loginRoot();

    Fixture f;
const omero::client* client = f.login();
SearchPrx search = (*client).getSession()->createSearchService();
    search.onlyType(Image.class);

    // Find the Image
    // full text
    search.byFullText(name);
    assertEquals(1, search.results().size());
    // annotated with
    search.byAnnotatedWith(tag);
    assertResults(search, 1);
    // tag for group
    search.byTagForGroups(name);
    assertResults(search, 1);
    // group for tags
    search.byGroupForTags(name);
    assertResults(search, 1);

    // Now restrict the search to past
    search.onlyModifiedBetween(null, oneHourAgo);
    // full text
    search.byFullText(name);
    assertFalse(search.hasNext());
    // annotated with
    search.byAnnotatedWith(tag);
    assertResults(search, 0);
    // group for tags
    search.byGroupForTags(name);
    assertResults(search, 1);
    // tag for group
    search.byTagForGroups(name);
    assertResults(search, 1);

    // Future
    search.onlyModifiedBetween(inOneHour, null);
    // full text
    search.byFullText(name);
    assertFalse(search.hasNext());
    // annotated with
    search.byAnnotatedWith(tag);
    assertResults(search, 0);
    // tag for group
    search.byTagForGroups(name);
    assertResults(search, 1);
    // group for tags
    search.byGroupForTags(name);
    assertResults(search, 1);

    // 2 hour period around now
    search.onlyModifiedBetween(oneHourAgo, inOneHour);
    // full text
    search.byFullText(name);
    assertEquals(1, search.results().size());
    // annotated with
    search.byAnnotatedWith(tag);
    assertResults(search, 1);
    // tag for group
    search.byTagForGroups(name);
    assertResults(search, 1);
    // group for tags
    search.byGroupForTags(name);
    assertResults(search, 1);

    // Starting at now old 'now'
    search.onlyModifiedBetween(null, now);
    // full text
    search.byFullText(name);
    assertFalse(search.hasNext());
    // annotated with
    search.byAnnotatedWith(tag);
    assertResults(search, 0);
    // tag for group
    search.byTagForGroups(name);
    assertResults(search, 1);
    // group for tags
    search.byGroupForTags(name);
    assertResults(search, 1);

    // Open them up again and should be found
    search.onlyModifiedBetween(null, null);
    // full text
    search.byFullText(name);
    assertEquals(1, search.results().size());
    // annotated with
    search.byAnnotatedWith(tag);
    assertResults(search, 1);
    // tag for group
    search.byTagForGroups(name);
    assertResults(search, 1);
    // group for tags
    search.byGroupForTags(name);
    assertResults(search, 1);
}

BOOST_AUTO_TEST_CASE( testOnlyAnnotatedBetween ) {

    String name = uuid();
    Image i = new Image();
    i.setName(name);
    TagAnnotation tag = new TagAnnotation();
    tag.setTextValue(name);
    i.linkAnnotation(tag);
    TagAnnotation grp = new TagAnnotation();
    grp.setTextValue(name);
    tag.linkAnnotation(grp);
    i = iUpdate.saveAndReturnObject(i);
    tag = new TagAnnotation();
    tag.setTextValue(name);
    iUpdate.indexObject(i);
    loginRoot();

    Fixture f;
const omero::client* client = f.login();
SearchPrx search = (*client).getSession()->createSearchService();
    search.onlyType(Image.class);

    // Find the Image
    // full text
    search.byFullText(name);
    assertEquals(1, search.results().size());
    // annotated with
    search.byAnnotatedWith(tag);
    assertResults(search, 1);
    // tag for group
    search.byTagForGroups(name);
    assertResults(search, 1);
    // group for tags
    search.byGroupForTags(name);
    assertResults(search, 1);

    // Now restrict the search to past
    search.onlyAnnotatedBetween(null, oneHourAgo);
    // full text
    search.byFullText(name);
    assertFalse(search.hasNext());
    // annotated with
    search.byAnnotatedWith(tag);
    assertResults(search, 0);
    // tag for group
    search.byTagForGroups(name);
    assertResults(search, 0);
    // group for tags
    search.byGroupForTags(name);
    assertResults(search, 0);

    // Future
    search.onlyAnnotatedBetween(inOneHour, null);
    // full text
    search.byFullText(name);
    assertFalse(search.hasNext());
    // annotated with
    search.byAnnotatedWith(tag);
    assertResults(search, 0);
    // tag for group
    search.byTagForGroups(name);
    assertResults(search, 0);
    // group for tags
    search.byGroupForTags(name);
    assertResults(search, 0);

    // 2 hour period around now
    search.onlyAnnotatedBetween(oneHourAgo, inOneHour);
    // full text
    search.byFullText(name);
    assertEquals(1, search.results().size());
    // annotated with
    search.byAnnotatedWith(tag);
    assertResults(search, 1);
    // tag for group
    search.byTagForGroups(name);
    assertResults(search, 1);
    // group for tags
    search.byGroupForTags(name);
    assertResults(search, 1);

    // Starting at now old 'now'
    search.onlyAnnotatedBetween(null, now);
    // full text
    search.byFullText(name);
    assertFalse(search.hasNext());
    // annotated with
    search.byAnnotatedWith(tag);
    assertResults(search, 0);
    // tag for group
    search.byTagForGroups(name);
    assertResults(search, 0);
    // group for tags
    search.byGroupForTags(name);
    assertResults(search, 0);

    // Open them up again and should be found
    search.onlyAnnotatedBetween(null, null);
    // full text
    search.byFullText(name);
    assertEquals(1, search.results().size());
    // annotated with
    search.byAnnotatedWith(tag);
    assertResults(search, 1);
    // tag for group
    search.byTagForGroups(name);
    assertResults(search, 1);
    // group for tags
    search.byGroupForTags(name);
    assertResults(search, 1);
}

BOOST_AUTO_TEST_CASE( testOnlyAnnotatedBy ) {
    String name = uuid();
    String tag = uuid();
    Image i = new Image();
    i.setName(name);
    TagAnnotation t = new TagAnnotation();
    t.setTextValue(tag);
    i.linkAnnotation(t);
    TagAnnotation grp = new TagAnnotation();
    grp.setTextValue(tag);
    t.linkAnnotation(grp);
    i = iUpdate.saveAndReturnObject(i);
    t = new TagAnnotation();
    t.setTextValue(tag);
    iUpdate.indexObject(i);
    loginRoot();

    Fixture f;
const omero::client* client = f.login();
SearchPrx search = (*client).getSession()->createSearchService();
    search.onlyType(Image.class);

    // Find the annotation
    // full text
    search.byFullText(name);
    assertEquals(1, search.results().size());
    // annotated with
    search.byAnnotatedWith(t);
    assertResults(search, 1);
    // tag for group
    search.byTagForGroups(tag);
    assertResults(search, 1);
    // group for tags
    search.byGroupForTags(tag);
    assertResults(search, 1);

    // But if we restrict it to another user, there should be none
    Experimenter e = loginNewUser();
    Details d = Details.create();
    d.setOwner(e);
    search.onlyAnnotatedBy(d);
    search.notAnnotatedBy(null);
    // full text
    search.byFullText(name);
    assertFalse(search.hasNext());
    // annotated with
    search.byAnnotatedWith(t);
    assertResults(search, 0);
    // tag for group
    search.byTagForGroups(tag);
    assertResults(search, 0);
    // group for tags
    search.byGroupForTags(tag);
    assertResults(search, 0);

    // Reversing the ownership should give results
    search.onlyAnnotatedBy(null);
    search.notAnnotatedBy(d);
    // full text
    search.byFullText(name);
    assertResults(search, 1);
    // annotated with
    search.byAnnotatedWith(t);
    assertResults(search, 1);
    // tag for group
    search.byTagForGroups(tag);
    assertResults(search, 1);
    // group for tags
    search.byGroupForTags(tag);
    assertResults(search, 1);
}

BOOST_AUTO_TEST_CASE( testOnlyAnnotatedWith ) {

    // ignored by byAnnotatedWith
    // ignored by byTagForGroups, byGroupForTags

    String name = uuid();
    Image i = new Image();
    i.setName(name);
    i = iUpdate.saveAndReturnObject(i);

    iUpdate.indexObject(i);
    loginRoot();

    Fixture f;
const omero::client* client = f.login();
SearchPrx search = (*client).getSession()->createSearchService();

    // Search for tagged image, which shouldn't be there
    search.onlyAnnotatedWith(TagAnnotation.class);
    search.onlyType(Image.class);
    search.byFullText(name);
    assertFalse(search.hasNext());

    // But if we ask for Images which aren't annotated it should appear
    search.onlyAnnotatedWith(new Class[] {});
    search.onlyType(Image.class);
    search.byFullText(name);
    assertEquals(1, search.results().size());

    // Now let's tag it and see if it shows up
    TagAnnotation t = new TagAnnotation();
    t.setTextValue(uuid());
    t = iUpdate.saveAndReturnObject(t);

    ImageAnnotationLink link = new ImageAnnotationLink(i, t);
    iUpdate.saveObject(link);

    iUpdate.indexObject(i);
    loginRoot();

    // Since we're looking for "no annotations" there should be no results
    search.byFullText(name);
    assertFalse(search.hasNext());

    // And if we turn the annotations back on?
    search.onlyAnnotatedWith(TagAnnotation.class);
    search.byFullText(name);
    assertEquals(1, search.results().size());
}

BOOST_AUTO_TEST_CASE( testOnlyAnnotatedWithMultiple ) {
    String name = uuid();
    Image onlyTag = new Image(name);
    Image onlyBool = new Image(name);
    Image both = new Image(name);

    TagAnnotation tag = new TagAnnotation();
    tag.setTextValue("tag");
    BooleanAnnotation bool = new BooleanAnnotation();
    bool.setBoolValue(false);

    onlyTag.linkAnnotation(tag);
    both.linkAnnotation(tag);
    both.linkAnnotation(bool);
    onlyBool.linkAnnotation(bool);

    IObject[] arr = iUpdate.saveAndReturnArray(new IObject[] { onlyTag,
                onlyBool, both });
    for (IObject object : arr) {
	iUpdate.indexObject(object);
    }
    loginRoot();

    Fixture f;
const omero::client* client = f.login();
SearchPrx search = (*client).getSession()->createSearchService();
    search.onlyType(Image.class);

    search.onlyAnnotatedWith(TagAnnotation.class);
    search.byFullText(name);
    assertEquals(2, search.results().size());

    search.onlyAnnotatedWith(BooleanAnnotation.class);
    search.byFullText(name);
    assertEquals(2, search.results().size());

    search.onlyAnnotatedWith(BooleanAnnotation.class, TagAnnotation.class);
    search.byFullText(name);
    assertEquals(1, search.results().size());

}

// other
// =========================================================================

BOOST_AUTO_TEST_CASE( testMergedBatches ) {
    String uuid1 = uuid(), uuid2 = uuid();
    Image i1 = new Image(uuid1);
    Image i2 = new Image(uuid2);
    i1 = iUpdate.saveAndReturnObject(i1);
    i2 = iUpdate.saveAndReturnObject(i2);
    iUpdate.indexObject(i1);
    iUpdate.indexObject(i2);
    loginRoot();

    Fixture f;
const omero::client* client = f.login();
SearchPrx search = (*client).getSession()->createSearchService();
    search.onlyType(Image.class);
    search.byFullText(uuid1);
    assertResults(search, 1);

    search.byFullText(uuid2);
    assertResults(search, 1);

    search.bySomeMustNone(sa(uuid1, uuid2), null, null);
    assertResults(search, 2);

    // Everything looks ok, now try with batch
    search.setMergedBatches(true);
    search.byFullText(uuid1);
    search.byFullText(uuid2);
    assertResults(search, 2);
}

BOOSTXXX testOrderBy() ) {
    String uuid = uuid();
    TagAnnotation tag = new TagAnnotation();
    tag.setTextValue(uuid);
    Image i1 = new Image(uuid);
    i1.setDescription("a");
    i1.linkAnnotation(tag);
    Image i2 = new Image(uuid);
    i2.setDescription("b");
    i2.linkAnnotation(tag);
    i1 = iUpdate.saveAndReturnObject(i1);
    Thread.sleep(2000L); // Waiting to test creation time ordering better
    i2 = iUpdate.saveAndReturnObject(i2);
    iUpdate.indexObject(i1);
    iUpdate.indexObject(i2);
    loginRoot();
    tag = new TagAnnotation();
    tag.setTextValue(uuid);

    Fixture f;
const omero::client* client = f.login();
SearchPrx search = (*client).getSession()->createSearchService();
    search.onlyType(Image.class);

    // Order by description desc
    search.unordered();
    search.addOrderByDesc("description");
    // full text
    search.byFullText(uuid);
    List<String> desc = new ArrayList<String>();
    desc.add(i2.getDescription());
    desc.add(i1.getDescription());
    while (search.hasNext()) {
	assertEquals(desc.remove(0), ((Image) search.next())
		     .getDescription());
    }
    // annotated with
    search.byAnnotatedWith(tag);
    desc = new ArrayList<String>();
    desc.add(i2.getDescription());
    desc.add(i1.getDescription());
    while (search.hasNext()) {
	assertEquals(desc.remove(0), ((Image) search.next())
		     .getDescription());
    }

    // Order by descript asc
    search.unordered();
    search.addOrderByAsc("description");
    // full text
    search.byFullText(uuid);
    List<String> asc = new ArrayList<String>();
    asc.add(i1.getDescription());
    asc.add(i2.getDescription());
    while (search.hasNext()) {
	assertEquals(asc.remove(0), ((Image) search.next())
		     .getDescription());
    }
    // annotated with
    search.byAnnotatedWith(tag);
    asc = new ArrayList<String>();
    asc.add(i1.getDescription());
    asc.add(i2.getDescription());
    while (search.hasNext()) {
	assertEquals(asc.remove(0), ((Image) search.next())
		     .getDescription());
    }

    // Ordered by id
    search.unordered();
    search.addOrderByDesc("id");
    // full text
    search.byFullText(uuid);
    List<Long> ids = new ArrayList<Long>();
    ids.add(i2.getId());
    ids.add(i1.getId());
    while (search.hasNext()) {
	assertEquals(ids.remove(0), search.next().getId());
    }
    // annotated with
    search.byAnnotatedWith(tag);
    ids = new ArrayList<Long>();
    ids.add(i2.getId());
    ids.add(i1.getId());
    while (search.hasNext()) {
	assertEquals(ids.remove(0), search.next().getId());
    }

    // Ordered by creation event id
    search.unordered();
    search.addOrderByDesc("details.creationEvent.id");
    // full text
    search.byFullText(uuid);
    ids = new ArrayList<Long>();
    ids.add(i2.getId());
    ids.add(i1.getId());
    while (search.hasNext()) {
	assertEquals(ids.remove(0), search.next().getId());
    }
    // annotated with
    search.byAnnotatedWith(tag);
    ids = new ArrayList<Long>();
    ids.add(i2.getId());
    ids.add(i1.getId());
    while (search.hasNext()) {
	assertEquals(ids.remove(0), search.next().getId());
    }

    // ordered by creation event time
    search.unordered();
    search.addOrderByDesc("details.creationEvent.time");
    // full text
    search.byFullText(uuid);
    ids = new ArrayList<Long>();
    ids.add(i2.getId());
    ids.add(i1.getId());
    while (search.hasNext()) {
	assertEquals(ids.remove(0), search.next().getId());
    }
    // annotated with
    search.byAnnotatedWith(tag);
    ids = new ArrayList<Long>();
    ids.add(i2.getId());
    ids.add(i1.getId());
    while (search.hasNext()) {
	assertEquals(ids.remove(0), search.next().getId());
    }

    // To test multiple sort fields, we add another image with an "a"
    // description, which should could before the other image with the "a"
    // description if we reverse the id order

    Image i3 = new Image(uuid);
    i3.setDescription("a");
    i3.linkAnnotation(tag);
    i3 = iUpdate.saveAndReturnObject(i3);
    iUpdate.indexObject(i3);
    loginRoot();
    tag = new TagAnnotation();
    tag.setTextValue(uuid);

    // multi-ordering
    search.unordered();
    search.addOrderByAsc("description");
    search.addOrderByDesc("id");
    // annotated with
    search.byAnnotatedWith(tag);
    List<Long> multi = new ArrayList<Long>();
    multi.add(i3.getId());
    multi.add(i1.getId());
    multi.add(i2.getId());
    while (search.hasNext()) {
	assertEquals(multi.remove(0), search.next().getId());
    }
    // full text
    search.byFullText(uuid);
    multi = new ArrayList<Long>();
    multi.add(i3.getId());
    multi.add(i1.getId());
    multi.add(i2.getId());
    while (search.hasNext()) {
	assertEquals(multi.remove(0), search.next().getId());
    }

			 }

BOOST_AUTO_TEST_CASE( testFetchAnnotations ) {
    String uuid = uuid();
    Image i = new Image(uuid);
    TagAnnotation tag = new TagAnnotation();
    tag.setTextValue(uuid);
    LongAnnotation la = new LongAnnotation();
    la.setLongValue(1L);
    DoubleAnnotation da = new DoubleAnnotation();
    da.setDoubleValue(0.0);
    i.linkAnnotation(tag);
    i.linkAnnotation(la);
    i.linkAnnotation(da);
    i = iUpdate.saveAndReturnObject(i);
    tag = new TagAnnotation();
    tag.setTextValue(uuid);
    iUpdate.indexObject(i);
    loginRoot();

    Fixture f;
const omero::client* client = f.login();
SearchPrx search = (*client).getSession()->createSearchService();
    search.onlyType(Image.class);

    // No fetch returns empty annotations
    // full text
    search.byFullText(uuid);
    Image t = (Image) search.results().get(0);
    assertEquals(-1, t.sizeOfAnnotationLinks());
    // annotated with
    search.byAnnotatedWith(tag);
    t = (Image) search.results().get(0);
    assertEquals(-1, t.sizeOfAnnotationLinks());

    // Fetch only a given type
    search.fetchAnnotations(TagAnnotation.class);
    // annotated with
    search.byAnnotatedWith(tag);
    t = (Image) search.results().get(0);
    assertEquals(1, t.sizeOfAnnotationLinks());
    // full text
    search.byFullText(uuid);
    t = (Image) search.results().get(0);
    assertEquals(3, t.sizeOfAnnotationLinks());

    // fetch only a given type different from annotated-with type
    search.fetchAnnotations(DoubleAnnotation.class);
    // annotated with
    search.byAnnotatedWith(tag);
    t = (Image) search.results().get(0);
    assertEquals(1, t.sizeOfAnnotationLinks());
    // full text
    search.byFullText(uuid);
    t = (Image) search.results().get(0);
    assertEquals(3, t.sizeOfAnnotationLinks());

    // fetch two types
    search.fetchAnnotations(TagAnnotation.class, DoubleAnnotation.class);
    // annotated with
    search.byAnnotatedWith(tag);
    t = (Image) search.results().get(0);
    assertEquals(2, t.sizeOfAnnotationLinks());
    // full text
    search.byFullText(uuid);
    t = (Image) search.results().get(0);
    assertEquals(3, t.sizeOfAnnotationLinks());

    // Fetch all
    search.fetchAnnotations(Annotation.class);
    // annotated with
    search.byAnnotatedWith(tag);
    assertResults(search, 0);
    // TODO t = (Image) search.results().get(0);
    // TODO assertEquals(3, t.sizeOfAnnotationLinks());
    // full text
    search.byFullText(uuid);
    t = (Image) search.results().get(0);
    assertEquals(3, t.sizeOfAnnotationLinks());

    // resave and see if there is data loss
    search.fetchAnnotations(TagAnnotation.class);
    search.byAnnotatedWith(tag);
    t = (Image) search.next();
    FileAnnotation f = new FileAnnotation();
    t.linkAnnotation(f);
    iUpdate.saveObject(t);
    t = iQuery
	.findByQuery(
		     "select t from Image t join fetch t.annotationLinks where t.id = :id",
		     new Parameters().addId(t.getId()));
    assertEquals(4, t.sizeOfAnnotationLinks());
}

// bugs
// =========================================================================

BOOST_AUTO_TEST_CASE( testTextAnnotationDoesntTryToLoadUpdateEvent ) {
    String uuid = uuid();
    TextAnnotation ta = new TextAnnotation();
    ta.setTextValue(uuid);
    ta = iUpdate.saveAndReturnObject(ta);
    iUpdate.indexObject(ta);
    loginRoot();

    Fixture f;
const omero::client* client = f.login();
SearchPrx search = (*client).getSession()->createSearchService();
    search.onlyType(TextAnnotation.class);
    search.byFullText(uuid);
    assertResults(search, 1);
}

BOOST_AUTO_TEST_CASE( testExperimenterDoesntTryToLoadOwner ) {
    Fixture f;
const omero::client* client = f.login();
SearchPrx search = (*client).getSession()->createSearchService();
    search->onlyType("Experimenter");
	search->byFullText("root");
    assertAtLeastResults(search, 1);
}

BOOST_AUTO_TEST_CASE( testLookingForExperimenterWithOwner ) {
    Fixture f;
const omero::client* client = f.login();
SearchPrx search = (*client).getSession()->createSearchService();
    search->onlyType("Experimenter");

    // Just root should work
	search->byFullText("root");
    search.next();

    // And filtered on "owner" (experimenter has none) should work, too.
    Details d = Details.create();
    d.setOwner(new Experimenter(0L, false));
    search.onlyOwnedBy(d);
	search->byFullText("root");
    search.next();
    }
*/
