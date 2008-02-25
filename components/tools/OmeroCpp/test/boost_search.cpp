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
void assertResults(int count, SearchPrx search) {
    if (count  > 0) {
        BOOST_CHECK_MESSAGE( search->hasNext(), "Search should have results." );
        if (search->hasNext()) {
            BOOST_CHECK_EQUAL( count, search->results().size() );
        }
    } else {
        BOOST_CHECK_MESSAGE( ! search->hasNext(), "Search shouldn't have results." );
        if (search->hasNext()) {
            int size = search->results().size();
            BOOST_ERROR( size + " results found when should be none." );
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


    } catch (const omero::ApiUsageException& aue) {
        cout << aue.message <<endl;
	BOOST_ERROR ( "api usage exception thrown" );
    } catch (const Ice::UnknownException& ue) {
        cout << ue << endl;
	BOOST_ERROR( "unknown exception thrown" );
    }
}
