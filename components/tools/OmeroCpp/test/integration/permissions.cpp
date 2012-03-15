/*
 * Copyright 2012 Glencoe Software, Inc. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
#include <omero/fixture.h>
#include <omero/callbacks.h>
#include <omero/all.h>
#include <omero/cmd/Graphs.h>
#include <omero/model/CommentAnnotationI.h>
#include <string>
#include <map>

using namespace std;
using namespace omero;
using namespace omero::api;
using namespace omero::cmd;
using namespace omero::callbacks;
using namespace omero::model;
using namespace omero::rtypes;
using namespace omero::sys;

TEST( PermissionsTest, testImmutablePermissions ) {

    PermissionsIPtr p = new PermissionsI();
    p->setPerm1(1L);
    p->setWorldRead(true);

    p = new PermissionsI();
    p->ice_postUnmarshal();

    ASSERT_THROW(p->setPerm1(1L), omero::ClientError);
    ASSERT_THROW(p->setWorldRead(true), omero::ClientError);

    Fixture f;
    const omero::client_ptr client = f.login();
    ServiceFactoryPrx sf = client->getSession();
    IUpdatePrx iupdate = sf->getUpdateService();

    CommentAnnotationPtr c = new CommentAnnotationI();
    c = CommentAnnotationPtr::dynamicCast( iupdate->saveAndReturnObject( c ) );
    p = PermissionsIPtr::dynamicCast( c->getDetails()->getPermissions() );

    ASSERT_THROW(p->setPerm1(1L), omero::ClientError);
    ASSERT_THROW(p->setWorldRead(true), omero::ClientError);

}

TEST( PermissionsTest, testDisallow ) {

    PermissionsIPtr p = new PermissionsI();
    ASSERT_TRUE(p->canAnnotate());
    ASSERT_TRUE(p->canEdit());

}
