/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#include <omero/model/PermissionsI.h>
#include <omero/fixture.h>

TEST(ClientTest, UnconfiguredClient )
{
  Fixture f;
  int argc = 1;
  char* argv[] = {(char*)"--omero.host=localhost", 0};
  omero::client(argc, argv);
}

TEST(ClientTest, ClientWithInitializationData )
{
  Fixture f;
  int argc = 0;
  char** argv = {0};
  Ice::InitializationData id;
  id.properties = Ice::createProperties();
  id.properties->setProperty("omero.host","localhost");
  omero::client(argc,argv,id);
}

TEST(ClientTest, ClientWithInitializationData2 )
{
  Fixture f;
  int argc = 2;
  char* argv[] = {"program", "--omero.host=localhost",0};
  Ice::StringSeq args = Ice::argsToStringSeq(argc, argv);
  Ice::InitializationData id;
  id.properties = Ice::createProperties(argc, argv);
  id.properties->parseCommandLineOptions("omero", args);
  omero::client c(id);
  std::string s = c.getProperty("omero.host");
  ASSERT_EQ("localhost", s);
}
