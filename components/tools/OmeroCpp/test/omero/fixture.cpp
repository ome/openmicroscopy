
/*
 *   Copyright 2007-2012 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#include <algorithm>
#include <omero/fixture.h>
#include <omero/model/LengthI.h>
#include <omero/model/PixelsTypeI.h>
#include <omero/model/PhotometricInterpretationI.h>
#include <omero/model/AcquisitionModeI.h>
#include <omero/model/DimensionOrderI.h>
#include <omero/model/ChannelI.h>
#include <omero/model/LogicalChannelI.h>
#include <omero/model/StatsInfoI.h>
#include <omero/model/PlaneInfoI.h>
#include <omero/util/uuid.h>

using namespace omero::api;
using namespace omero::model;
using namespace omero::model::enums;
using namespace omero::rtypes;

omero::model::ImagePtr new_ImageI()
{
    omero::model::ImagePtr img = new omero::model::ImageI();
    return img;
}

Fixture::Fixture()
{
    client = new omero::client();
    std::string rootpass = client->getProperty("omero.rootpass");
    logout();
    login("root", rootpass);
    root = client;
    client = omero::client_ptr();
}

void Fixture::logout()
{
    root = NULL;
    client = NULL;
}


void Fixture::show_stackframe() {
#ifdef LINUX
  void *trace[16];
  char **messages = (char **)NULL;
  int i, trace_size = 0;

  trace_size = backtrace(trace, 16);
  messages = backtrace_symbols(trace, trace_size);
  printf("[bt] Execution path:\n");
  for (i=0; i<trace_size; ++i)
    printf("[bt] %s\n", messages[i]);
#endif
}

std::string Fixture::uuid()
{
  std::string s = omero::util::generate_uuid();
    std::replace(s.begin(), s.end(), '-', 'X');
    return s;
}

void Fixture::printUnexpected()
{
  /* Need printStackTrace.h for this
  char* buf = new char[1024];
  printStackTrace(buf, 1024);
  std::cout << "Trace:" << buf << std::endl;
  delete[] buf;
  */
}

void Fixture::login(const std::string& username, const std::string& password) {
    client = new omero::client();
    client->createSession(username, password);
    client->getSession()->closeOnDestroy();
}

void Fixture::login(const omero::model::ExperimenterPtr& user, const std::string& password) {
    this->login(user->getOmeName()->getValue(), password);
}

omero::model::ExperimenterPtr Fixture::newUser(const omero::model::ExperimenterGroupPtr& _g) {

    IAdminPrx admin = root->getSession()->getAdminService();
    omero::model::ExperimenterGroupPtr g(_g);
    omero::RStringPtr name = omero::rtypes::rstring(uuid());
    omero::RStringPtr groupName = name;
    long gid = -1;
    if (!g) {
        g = new omero::model::ExperimenterGroupI();
        g->setName( name );
        g->setLdap( rbool(false) );
        gid = admin->createGroup(g);
    } else {
        gid = g->getId()->getValue();
        groupName = admin->getGroup(gid)->getName();
    }
    omero::model::ExperimenterPtr e = new omero::model::ExperimenterI();
    e->setOmeName( name );
    e->setFirstName( name );
    e->setLastName( name );
    e->setLdap( rbool(false) );
    std::vector<ExperimenterGroupPtr> groups;
    omero::model::ExperimenterGroupPtr userGroup = admin->lookupGroup("user");
    groups.push_back(userGroup);
    long id = admin->createExperimenterWithPassword(e, name, g, groups);
    return admin->getExperimenter(id);
}

omero::model::ExperimenterGroupPtr Fixture::newGroup(const std::string& perms) {
    IAdminPrx admin = root->getSession()->getAdminService();
    std::string gname = uuid();
    ExperimenterGroupPtr group = new ExperimenterGroupI();
    group->setName( rstring(gname) );
    group->setLdap( rbool(false) );
    if (!perms.empty()) {
        group->getDetails()->setPermissions( new PermissionsI(perms) );
    }
    long gid = admin->createGroup(group);
    group = admin->getGroup(gid);
    return group;
}

void Fixture::addExperimenter(
        const omero::model::ExperimenterGroupPtr& group,
        const omero::model::ExperimenterPtr& user) {

        IAdminPrx admin = root->getSession()->getAdminService();
        std::vector<ExperimenterGroupPtr> groups;
        groups.push_back(group);
        admin->addGroups(user, groups);
}

omero::model::PixelsIPtr Fixture::pixels() {
    PixelsIPtr pix = new PixelsI();
    PixelsTypePtr pt = new PixelsTypeI();
    PhotometricInterpretationIPtr pi = new PhotometricInterpretationI();
    AcquisitionModeIPtr mode = new AcquisitionModeI();
    DimensionOrderIPtr d0 = new DimensionOrderI();
    ChannelIPtr c = new ChannelI();
    LogicalChannelIPtr lc = new LogicalChannelI();
    StatsInfoIPtr si = new StatsInfoI();
    PlaneInfoIPtr pl = new PlaneInfoI();

    mode->setValue( rstring("Wide-field") );
    pi->setValue( rstring("RGB") );
    pt->setValue( rstring("int8") );
    d0->setValue( rstring("XYZTC") );

    lc->setPhotometricInterpretation( pi );

    UnitsLength mm = omero::model::enums::MILLIMETER;
    LengthPtr mm1 = new LengthI();
    mm1->setUnit(mm);
    mm1->setValue(1.0);

    pix->setSizeX( rint(1) );
    pix->setSizeY( rint(1) );
    pix->setSizeZ( rint(1) );
    pix->setSizeT( rint(1) );
    pix->setSizeC( rint(1) );
    pix->setSha1 (rstring("09bc7b2dcc9a510f4ab3a40c47f7a4cb77954356") ); // for "pixels"
    pix->setPixelsType( pt );
    pix->setDimensionOrder( d0 );
    pix->setPhysicalSizeX(mm1);
    pix->setPhysicalSizeY(mm1);
    pix->setPhysicalSizeZ(mm1);

    pix->addChannel( c );
    c->setLogicalChannel( lc) ;
    return pix;
}
