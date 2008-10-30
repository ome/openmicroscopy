/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#include <omero/model/PermissionsI.h>
#include <boost_fixture.h>

BOOST_AUTO_TEST_CASE( UnconfiguredClient )
{
  Fixture f;
  int argc = 1;
  char* argv[] = {"--omero.host=localhost", 0};
  omero::client(argc,argv);
}

BOOST_AUTO_TEST_CASE( ClientWithInitializationData )
{
  Fixture f;
  int argc = 0;
  char** argv = new char*[0];
  Ice::InitializationData id;
  id.properties = Ice::createProperties();
  id.properties->setProperty("omero.host","localhost");
  omero::client(argc,argv,id);
}

BOOST_AUTO_TEST_CASE( ClientWithInitializationData2 )
{
  Fixture f;
  int argc = 1;
  char* argv[] = {"program", "--omero.host=localhost",0};
  Ice::InitializationData id;
  id.properties = Ice::createProperties(argc,argv); // #2
  std::string s = id.properties->getProperty("omero.host");
  BOOST_CHECK_MESSAGE( s == "localhost", s + " should be localhost" );
  omero::client(argc,argv,id);
}

