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
  int argc = 0;
  char** argv = new char*[0];
  omero::client(argc,argv);
}

BOOST_AUTO_TEST_CASE( ClientWithInitializationData )
{
  Fixture f;
  int argc = 0;
  char** argv = new char*[0];
  Ice::InitializationData id;
  id.properties = Ice::createProperties();
  id.properties->setProperty("foo","bar");
  BOOST_CHECK( id.properties->getProperty("foo") == "bar" );
  omero::client(argc,argv,id);
}

BOOST_AUTO_TEST_CASE( ClientWithInitializationData2 )
{
  Fixture f;
  int argc = 1;
  char* argv[] = {"--foo=bar",0};
  Ice::InitializationData id;
  id.properties = Ice::createProperties(argc,argv); // #2
  std::string s = id.properties->getProperty("foo");
  // BOOST_CHECK_MESSAGE( s == "bar", s + " should be bar" );
  // This doesn't work. Ice currently only filters --Ice properties.
  omero::client(argc,argv,id);
}

