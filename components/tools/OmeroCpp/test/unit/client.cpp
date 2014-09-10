/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#include <map>
#include <string>
#include <omero/model/PermissionsI.h>
#include <omero/fixture.h>

using namespace std;

TEST(ClientTest, UnconfiguredClient )
{
  int argc = 1;
  std::string host("--omero.host=localhost");
  char* argv[] = {&host[0], 0};
  omero::client_ptr c = new omero::client(argc, argv);
}

TEST(ClientTest, ClientWithInitializationData )
{
  int argc = 0;
  char** argv = {0};
  Ice::InitializationData id;
  id.properties = Ice::createProperties();
  id.properties->setProperty("omero.host","localhost");
  omero::client_ptr c = new omero::client(argc,argv,id);
}

TEST(ClientTest, ClientWithInitializationData2 )
{
  int argc = 2;
  const char* argv[] = {"program", "--omero.host=localhost",0};
  Ice::StringSeq args = Ice::argsToStringSeq(argc, const_cast<char**>(argv));
  Ice::InitializationData id;
  id.properties = Ice::createProperties(argc, const_cast<char**>(argv));
  id.properties->parseCommandLineOptions("omero", args);
  omero::client_ptr c = new omero::client(id);
  std::string s = c->getProperty("omero.host");
  ASSERT_EQ("localhost", s);
}

TEST(ClientTest, BlockSizeDefault)
{
  int argc = 0;
  char* argv[] = {0};
  omero::client_ptr c = new omero::client(argc, argv);
  ASSERT_EQ(5000000, c->getDefaultBlockSize());
}

TEST(ClientTest, BlockSize1MB)
{

  int argc = 1;
  std::string blocksize("--omero.block_size=1000000");
  char* argv[] = {&blocksize[0], 0};
  omero::client_ptr c = new omero::client(argc, argv);
  ASSERT_EQ(1000000, c->getDefaultBlockSize());
}

TEST(ClientTest, testCreateFromMap)
{
    map<string, string> props;
    props["omero.host"] = "localhost";

    omero::client_ptr client = new omero::client(props, false);
    std::string s = client->getProperty("omero.host");
    ASSERT_EQ("localhost", s);
}
