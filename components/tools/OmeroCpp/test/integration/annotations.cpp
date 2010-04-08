/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */
#include <IceUtil/UUID.h>
#include <boost_fixture.h>
#include <omero/model/TagAnnotationI.h>
#include <omero/model/ImageAnnotationLinkI.h>
#include <omero/model/ImageI.h>
#include <omero/model/FormatI.h>
#include <omero/model/OriginalFileI.h>
#include <omero/model/FileAnnotationI.h>

#include <stdio.h>
#include <fstream>
#include <iostream>

using namespace std;
using namespace omero::api;
using namespace omero::model;
using namespace omero::sys;
using namespace omero::rtypes;

BOOST_AUTO_TEST_CASE( tagAnnotation )
{
    try {

	Fixture f;
	const omero::client_ptr client = f.login();
	ServiceFactoryPrx sf = client->getSession();
	IQueryPrx q = sf->getQueryService();
	IUpdatePrx u = sf->getUpdateService();

	TagAnnotationIPtr tag = new TagAnnotationI();
	tag->setTextValue(rstring("my-first-tag"));

	string uuid = IceUtil::generateUUID();
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
	BOOST_CHECK_EQUAL( "my-first-tag", tag->getTextValue()->getValue() );

    } catch (omero::ApiUsageException& aue) {
	cout << aue.message <<endl;
	throw;
    }
}

BOOST_AUTO_TEST_CASE( fileAnnotation )
{
    try {

	Fixture f;
	const omero::client_ptr client = f.login();
	ServiceFactoryPrx sf = client->getSession();
	IQueryPrx q = sf->getQueryService();
	IUpdatePrx u = sf->getUpdateService();

	// Create temp file
	string unique_content = IceUtil::generateUUID();
	char * pointer = "testXXXXXX";
	mkstemp(pointer);

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
	FormatIPtr format = new FormatI();
	format->setValue(rstring("text/xml"));
	OriginalFileIPtr file = new OriginalFileI();
	file->setFormat(format);
	file->setName(rstring("my-file.xml"));
	file->setPath(rstring("/tmp"));
	file->setSha1(rstring("foo"));
	file->setSize(rlong(size));
	file = OriginalFileIPtr::dynamicCast(u->saveAndReturnObject(file));

	// Upload file
	RawFileStorePrx rfs = sf->createRawFileStore();
	rfs->setFileId(file->getId()->getValue());
	rfs->write(buf, 0, buf.size());
	rfs->close();

	FileAnnotationIPtr attachment = new FileAnnotationI();
	attachment->setFile(file);

	string uuid = IceUtil::generateUUID();
	ImageIPtr i = ImageIPtr::dynamicCast(new_ImageI());
	i->setName(rstring(uuid));
	i->linkAnnotation(attachment);
	u->saveObject(i);

	i = ImageIPtr::dynamicCast(
				   q->findByQuery(
						  "select i from Image i "
						  "join fetch i.annotationLinks l "
						  "join fetch l.child where i.name = '" + uuid +"'", 0));
	ImageAnnotationLinkIPtr link = ImageAnnotationLinkIPtr::dynamicCast(i->beginAnnotationLinks()[0]);
	AnnotationPtr a = link->getChild();
	attachment = FileAnnotationIPtr::dynamicCast(a);

    } catch (omero::ApiUsageException& aue) {
	cout << aue.message << endl;
	cout << aue.serverStackTrace << endl;
	BOOST_ERROR( "api usage exception");
    }
}

BOOST_AUTO_TEST_CASE( annotationImmutability )
{
    try {

	Fixture f;
	const omero::client_ptr client = f.login();
	ServiceFactoryPrx sf = client->getSession();
	IQueryPrx q = sf->getQueryService();
	IUpdatePrx u = sf->getUpdateService();

	TagAnnotationIPtr tag = new TagAnnotationI();
	tag->setTextValue(rstring("immutable-tag"));

	ImageIPtr i = ImageIPtr::dynamicCast(new_ImageI());
	i->setName(rstring("tagged-image"));
	i->linkAnnotation(tag);
	i = ImageIPtr::dynamicCast( u->saveAndReturnObject(i) );
	tag = TagAnnotationIPtr::dynamicCast( i->copyAnnotationLinks()[0]->getChild() );

	tag->setTextValue( rstring("modified-tag") );
	tag = TagAnnotationIPtr::dynamicCast( u->saveAndReturnObject( tag ) );
	tag = TagAnnotationIPtr::dynamicCast( q->get("TagAnnotation", tag->getId()->getValue()) );

	BOOST_CHECK_MESSAGE( tag->getTextValue()->getValue() == "immutable-tag", tag->getTextValue()->getValue() );

	// See #878
        // Annotation.ns is currently modifiable.
	tag->setNs( rstring("modified-name") );
	tag = TagAnnotationIPtr::dynamicCast( u->saveAndReturnObject( tag ) );
	tag = TagAnnotationIPtr::dynamicCast( q->get("TagAnnotation", tag->getId()->getValue()) );

	BOOST_CHECK_MESSAGE( tag->getNs()->getValue() == "modified-name", tag->getNs() );

    } catch (omero::ApiUsageException& aue) {
	cout << aue.message <<endl;
	throw;
    }
}
