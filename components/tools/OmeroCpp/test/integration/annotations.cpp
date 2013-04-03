/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */
#include <omero/fixture.h>
#include <omero/model/TagAnnotationI.h>
#include <omero/model/ImageAnnotationLinkI.h>
#include <omero/model/ImageI.h>
#include <omero/model/FormatI.h>
#include <omero/model/OriginalFileI.h>
#include <omero/model/FileAnnotationI.h>

#include <stdio.h>
#include <fstream>
#include <iostream>

//http://msdn.microsoft.com/en-us/library/t8ex5e91(VS.80).aspx
#ifdef _WIN32
#include <stdio.h>
#include <io.h>
#endif

#include <boost/uuid/uuid.hpp>
#include <boost/uuid/uuid_generators.hpp>
#include <boost/uuid/uuid_io.hpp>

using namespace std;
using namespace omero::api;
using namespace omero::model;
using namespace omero::sys;
using namespace omero::rtypes;

TEST(AnnotationTest, tagAnnotation )
{
    try {

        Fixture f;
        f.login();

        ServiceFactoryPrx sf = f.client->getSession();
        IQueryPrx q = sf->getQueryService();
        IUpdatePrx u = sf->getUpdateService();

        TagAnnotationIPtr tag = new TagAnnotationI();
        tag->setTextValue(rstring("my-first-tag"));

        boost::uuids::uuid newuuid = boost::uuids::random_generator()();
        string uuid = boost::uuids::to_string(newuuid);
        ImageIPtr i = ImageIPtr::dynamicCast(new_ImageI());
        i->setName(rstring(uuid));
        i->linkAnnotation(tag);
        u->saveObject(i);

        i = ImageIPtr::dynamicCast(
                q->findByQuery(
                        "select i from Image i "
                        "join fetch i.annotationLinks l "
                        "join fetch l.child where i.name = '" + uuid +"'", 0));
        ImageAnnotationLinkIPtr link = ImageAnnotationLinkIPtr::dynamicCast(i->beginAnnotationLinks()[0]);
        AnnotationPtr a = link->getChild();
        tag = TagAnnotationIPtr::dynamicCast(a);
        ASSERT_EQ( "my-first-tag", tag->getTextValue()->getValue() );

    } catch (omero::ApiUsageException& aue) {
        cout << aue.message <<endl;
        throw;
    }
}

TEST(AnnotationTest, fileAnnotation )
{
    try {

        Fixture f;
        f.login();

        ServiceFactoryPrx sf = f.client->getSession();
        IQueryPrx q = sf->getQueryService();
        IUpdatePrx u = sf->getUpdateService();

        // Create temp file
        char pointer[]="tmpXXXXXX";
#ifdef _WIN32
        int err;
        err = _mktemp_s(pointer, 10); // Length plus one for null
        ASSERT_FALSE( err );
#else
        mkstemp(pointer);
#endif

	boost::uuids::uuid newuuid = boost::uuids::random_generator()();
        string unique_content = boost::uuids::to_string(newuuid);
        {
            ofstream out(pointer);
            out << "<xml>" << endl;
            out << "  " << unique_content << endl;
            out << "</xml>" << endl;
        }


        long size;
        Ice::ByteSeq buf;
        ifstream in(pointer, ios::binary);
        if (!in.good() || in.eof() || !in.is_open()) {
            size = 0;
        } else {
            ifstream::pos_type beg = in.tellg();
            in.seekg(0, ios_base::end);
            ifstream::pos_type end = in.tellg();
            size = static_cast<long>(end - beg);

            in.seekg(0, ios_base::beg);
            istream_iterator<Ice::Byte> b(in), e;
            vector<Ice::Byte> v (b, e);
            buf = v;
        }

        // Create file object
        OriginalFilePtr file = new OriginalFileI();
        file->setMimetype(rstring("text/xml"));
        file->setName(rstring("my-file.xml"));
        file->setPath(rstring("/tmp"));
        file->setSha1(rstring("foo"));
        file->setSize(rlong(size));
        file = OriginalFilePtr::dynamicCast(u->saveAndReturnObject(file));

        // Upload file
        RawFileStorePrx rfs = sf->createRawFileStore();
        rfs->setFileId(file->getId()->getValue());
        rfs->write(buf, 0, buf.size());
        file = rfs->save(); // Updates the event to prevent OptimisticLockExceptions
        rfs->close();

        FileAnnotationPtr attachment = new FileAnnotationI();
        attachment->setFile(file);

        boost::uuids::uuid newuuid = boost::uuids::random_generator()();
        string uuid = boost::uuid::to_string(uuid);
        ImageIPtr i = ImageIPtr::dynamicCast(new_ImageI());
        i->setName(rstring(uuid));
        i->linkAnnotation(attachment);
        u->saveObject(i);

        i = ImageIPtr::dynamicCast(
                q->findByQuery(
                        "select i from Image i "
                        "join fetch i.annotationLinks l "
                        "join fetch l.child where i.name = '" + uuid +"'", 0));
        ImageAnnotationLinkPtr link = ImageAnnotationLinkPtr::dynamicCast(i->beginAnnotationLinks()[0]);
        AnnotationPtr a = link->getChild();
        attachment = FileAnnotationPtr::dynamicCast(a);

    } catch (omero::OptimisticLockException& ole) {
        FAIL() << ole.message;
    } catch (omero::ApiUsageException& aue) {
        cout << aue.message << endl;
        cout << aue.serverStackTrace << endl;
        FAIL() << "api usage exception";
    }
}
