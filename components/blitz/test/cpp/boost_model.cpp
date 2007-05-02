/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#include <boost_fixture.h>

using namespace omero::model;
using namespace OMERO;
using namespace omero;
using namespace std;

BOOST_AUTO_TEST_CASE( Toggle )
{
  Fixture f;
  PixelsIPtr pix = new PixelsI();
  BOOST_CHECK( pix->settingsLoaded );
  pix->toggleCollectionsLoaded( false );
  BOOST_CHECK( !(pix->settingsLoaded) );
  pix->toggleCollectionsLoaded( true );
  BOOST_CHECK( pix->settingsLoaded );
}

BOOST_AUTO_TEST_CASE( SimpleCtor )
{
  Fixture f;
  ImageIPtr img = new ImageI();
  BOOST_CHECK( img->loaded );
  BOOST_CHECK( img->pixelsLoaded );
}

BOOST_AUTO_TEST_CASE( UnloadedCtor )
{
  Fixture f;
  ImageIPtr img = new ImageI(new OMERO::CLong(1),false);
  BOOST_CHECK( !(img->loaded) );
  BOOST_CHECK( !(img->datasetLinksLoaded) );
}

BOOST_AUTO_TEST_CASE( Unload )
{
  Fixture f;
  ImageIPtr img = new ImageI();
  BOOST_CHECK( img->loaded );
  img->details->owner;
  // operator bool() is overloaded
  BOOST_CHECK( img->details ); // details are auto instantiated
  BOOST_CHECK( ! img->name ); // no other single-valued field is
  BOOST_CHECK( img->annotationsLoaded );
  BOOST_CHECK( img->annotations != (ImageAnnotationsSeq)0 );
  img->unload();
  BOOST_CHECK( ! (img->loaded) );
  BOOST_CHECK( ! (img->details) );
  BOOST_CHECK( ! (img->annotationsLoaded) );
  BOOST_CHECK( ! (img->annotations != (ImageAnnotationsSeq)0) );
}


BOOST_AUTO_TEST_CASE( Accessors )
{
  Fixture f;
  RStringPtr name = new CString("name");
  ImageIPtr img = new ImageI();
  BOOST_CHECK( img->name == (RStringPtr)0 );
  img->name = name;
  BOOST_CHECK( img->name != (RStringPtr)0 );
  RStringPtr str = img->getName();
  BOOST_CHECK( str->val == "name" );
  BOOST_CHECK( str == name );

  img->setName(new RString("name2",false));
  BOOST_CHECK( img->getName()->val == "name2" );
  BOOST_CHECK( ! img->getName()->null );
  
}

BOOST_AUTO_TEST_CASE( UnloadedAccessThrows )
{
  Fixture f;
  ImageIPtr unloaded = new ImageI(new OMERO::CLong(1),false);
  BOOST_CHECK_THROW( unloaded->getName(), omero::UnloadedEntityException );
}

BOOST_AUTO_TEST_CASE( Iterators )
{
  Fixture f;
  Ice::Current curr;
  ImageIPtr image = new ImageI();
  //    image->unload(curr);
  //    image->loaded = true;
  ImageDatasetLinksSeq::iterator it= image->beginDatasetLinks();
  for (;it != image->endDatasetLinks(); ++it) {    
    cout << ".";
  }
  cout << endl;
  //throw "oops";
}


BOOST_AUTO_TEST_CASE( LinkGroupAndUser )
{
  Fixture f;
  ExperimenterIPtr user = new ExperimenterI();
  ExperimenterGroupIPtr group = new ExperimenterGroupI();
  GroupExperimenterMapIPtr map = new GroupExperimenterMapI();
  map->id = new OMERO::CLong(1);
  user->addGroupExperimenterMap( map, true );
  group->addGroupExperimenterMap( map, true );
  ExperimenterGroupExperimenterMapSeq::iterator beg = user->beginGroupExperimenterMap();
  ExperimenterGroupExperimenterMapSeq::iterator end = user->endGroupExperimenterMap();
  for( ; beg != end; beg++ ) {
    cout << (*beg)->id << endl;
  }

}

BOOST_AUTO_TEST_CASE( LinkViaMap )
{
  Fixture f;
  ExperimenterIPtr user = new ExperimenterI();
  user->setFirstName(new OMERO::CString("test"));
  user->setLastName(new OMERO::CString("user"));
  user->setOmeName(new OMERO::CString("UUID"));
  
  // possibly setOmeName() and setOmeName(string) ??
  // and then don't need OMERO/types.h
  
  ExperimenterGroupIPtr group = new ExperimenterGroupI();
  // TODOuser->linkExperimenterGroup(group);
  GroupExperimenterMapIPtr map = new GroupExperimenterMapI();
  map->parent = group;
  map->child  = user;
}

