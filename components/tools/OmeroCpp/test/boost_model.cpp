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

BOOST_AUTO_TEST_CASE( Virtual )
{
  ImagePtr img = new ImageI();
  ImageIPtr imgI = new ImageI();
  img->unload();
  imgI->unload();
}

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
  ImageIPtr img = new ImageI(new omero::CLong(1),false);
  BOOST_CHECK( !(img->loaded) );
  BOOST_CHECK( !(img->datasetLinksLoaded) );
}

BOOST_AUTO_TEST_CASE( UnloadCheckPtr )
{
  Fixture f;
  ImageIPtr img = new ImageI();
  BOOST_CHECK( img->loaded );
  // operator bool() is overloaded
  BOOST_CHECK( img->details ); // details are auto instantiated
  BOOST_CHECK( ! img->name ); // no other single-valued field is
  img->unload();
  BOOST_CHECK( !img->loaded );
  BOOST_CHECK( !img->details );
}

BOOST_AUTO_TEST_CASE( UnloadField )
{
  Fixture f;
  ImageIPtr img = new ImageI();
  BOOST_CHECK( img->details );
  img->unloadDetails();
  BOOST_CHECK( ! img->details );
}

BOOST_AUTO_TEST_CASE( Sequences )
{
  Fixture f;
  ImageIPtr img = new ImageI();
  BOOST_CHECK( img->annotationsLoaded );
  img->annotations.push_back((ImageAnnotationPtr)0);
  img->unload();
  BOOST_CHECK( !img->annotationsLoaded );
  img->annotations.push_back((ImageAnnotationPtr)0);
}

BOOST_AUTO_TEST_CASE( Accessors )
{
  Fixture f;
  RStringPtr name = new CString("name");
  ImageIPtr img = new ImageI();
  BOOST_CHECK( !img->name );
  img->name = name;
  BOOST_CHECK( img->name );
  RStringPtr str = img->getName();
  BOOST_CHECK( str->val == "name" );
  BOOST_CHECK( str == name );

  img->setName(new RString("name2"));
  BOOST_CHECK( img->getName()->val == "name2" );
  BOOST_CHECK( img->getName() );

  img->unload();
  BOOST_CHECK( !img->name );
  
}

BOOST_AUTO_TEST_CASE( UnloadedAccessThrows )
{
  Fixture f;
  ImageIPtr unloaded = new ImageI(new omero::CLong(1),false);
  BOOST_CHECK_THROW( unloaded->getName(), omero::UnloadedEntityException );
}

BOOST_AUTO_TEST_CASE( Iterators )
{
  Fixture f;

  DatasetIPtr d = new DatasetI();
  ImageIPtr image = new ImageI();
  image->loaded = true;
  image->linkDataset(d);
  ImageDatasetLinksSeq::iterator it= image->beginDatasetLinks();
  int count = 0;
  for (;it != image->endDatasetLinks(); ++it) {    
    count++;
  }
  BOOST_CHECK( count == 1 );
}

BOOST_AUTO_TEST_CASE( ClearSet )
{
  Fixture f;
  ImageIPtr img = new ImageI();
  BOOST_CHECK( img->pixelsLoaded );
  img->addPixels( new PixelsI() );
  BOOST_CHECK( 1==img->sizeOfPixels() );
  img->clearPixels();
  BOOST_CHECK( img->pixelsLoaded );
  BOOST_CHECK( 0==img->sizeOfPixels() );
}

BOOST_AUTO_TEST_CASE( UnloadSet )
{
  Fixture f;
  ImageIPtr img = new ImageI();
  BOOST_CHECK( img->pixelsLoaded );
  img->addPixels( new PixelsI() );
  BOOST_CHECK( 1==img->sizeOfPixels() );
  img->unloadPixels();
  BOOST_CHECK( ! img->pixelsLoaded );
  // Can't check size BOOST_CHECK( 0==img->sizeOfPixels() );
}

BOOST_AUTO_TEST_CASE( RemoveFromSet )
{
  Fixture f;
  PixelsIPtr pix = new PixelsI();
  ImageIPtr img = new ImageI();
  BOOST_CHECK( img->pixelsLoaded );

  img->addPixels( pix );
  BOOST_CHECK( 1==img->sizeOfPixels() );

  img->removePixels( pix );
  BOOST_CHECK( 0==img->sizeOfPixels() );
}

BOOST_AUTO_TEST_CASE( LinkGroupAndUser )
{
  Fixture f;

  ExperimenterIPtr user = new ExperimenterI();
  ExperimenterGroupIPtr group = new ExperimenterGroupI();
  GroupExperimenterMapIPtr map = new GroupExperimenterMapI();

  map->id = new omero::CLong(1);
  map->link(group,user);
  user->addGroupExperimenterMap( map, false );
  group->addGroupExperimenterMap( map, false );

  typedef ExperimenterGroupExperimenterMapSeq::iterator egm_it; 
  egm_it beg = user->beginGroupExperimenterMap();
  egm_it end = user->endGroupExperimenterMap();
  int count = 0 ;
  for( ; beg != end; beg++ ) {
    ++count;
  }
  BOOST_CHECK( count == 1 );

}

BOOST_AUTO_TEST_CASE( LinkViaMap )
{
  Fixture f;
  ExperimenterIPtr user = new ExperimenterI();
  user->setFirstName(new omero::CString("test"));
  user->setLastName(new omero::CString("user"));
  user->setOmeName(new omero::CString("UUID"));
  
  // possibly setOmeName() and setOmeName(string) ??
  // and then don't need OMERO/types.h
  
  ExperimenterGroupIPtr group = new ExperimenterGroupI();
  // TODOuser->linkExperimenterGroup(group);
  GroupExperimenterMapIPtr map = new GroupExperimenterMapI();
  map->parent = group;
  map->child  = user;
}

BOOST_AUTO_TEST_CASE( LinkingAndUnlinking )
{
  Fixture f;

  DatasetImageLinkIPtr dil;

  DatasetIPtr d = new DatasetI();
  ImageIPtr   i = new ImageI();
  
  d->linkImage(i);
  BOOST_CHECK( d->sizeOfImageLinks() == 1 );
  d->unlinkImage(i);
  BOOST_CHECK( d->sizeOfImageLinks() == 0 );

  d = new DatasetI();
  i = new ImageI();
  d->linkImage(i);
  BOOST_CHECK( i->sizeOfDatasetLinks() == 1 );
  i->unlinkDataset(d);
  BOOST_CHECK( d->sizeOfImageLinks() == 0 );

  d = new DatasetI();
  i = new ImageI();
  dil = new DatasetImageLinkI();
  dil->link(d,i);
  d->addDatasetImageLink(dil, false);
  BOOST_CHECK( d->sizeOfImageLinks() == 1 );
  BOOST_CHECK( i->sizeOfDatasetLinks() == 0 );

}

BOOST_AUTO_TEST_CASE( UnloadedEntityTermination ) {

  Fixture f;

  ProjectDatasetLinkIPtr pDL = new ProjectDatasetLinkI();
  ProjectIPtr p = new ProjectI();
  DatasetIPtr d = new DatasetI();
  //pDL->link(p,d);
  d->unload();
  omero::model::IObjectPtr             theChild =
  omero::model::IObjectPtr::dynamicCast(pDL->getChild());
  cout << "theChild is: " << theChild << "... pDS" << endl;
  omero::model::DatasetIPtr            pDS =
  omero::model::DatasetIPtr::dynamicCast(theChild);

}
