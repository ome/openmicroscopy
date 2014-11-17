/*
 * Copyright (C) 2014 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
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
#include <omero/model/ExperimenterGroupI.h>
#include <omero/sys/ParametersI.h>
#include <omero/util/uuid.h>

using namespace std;
using namespace omero::api;
using namespace omero::model;
using namespace omero::sys;
using namespace omero::rtypes;
using namespace omero::util;


static void assertFooBar(const ExperimenterGroupPtr& group)
{
    NamedValue nv = group->getConfig()[0];
    string bar = omero::RStringPtr::dynamicCast(nv.value)->getValue();
    ASSERT_EQ("foo", nv.name);
    ASSERT_EQ("bar", bar);
}

TEST(MapAnnotationTest, mapStringField)
{

    Fixture f;
    f.login();

    ServiceFactoryPrx sf = f.root->getSession();
    IQueryPrx q = sf->getQueryService();
    IUpdatePrx u = sf->getUpdateService();

    string uuid = generate_uuid();
    ExperimenterGroupPtr group = new ExperimenterGroupI();
    ParametersIPtr params = new ParametersI();
    NamedValueList map;
    NamedValue nv;
    nv.name = "foo";
    nv.value = rstring("bar");
    group->setName(rstring(uuid));
    map.push_back(nv);
    // Setting must happen after map updated, since a copy is made
    group->setConfig(map);
    assertFooBar(group);

    group = ExperimenterGroupPtr::dynamicCast(u->saveAndReturnObject(group));
    params->addId(group->getId()->getValue());
    group = ExperimenterGroupPtr::dynamicCast(q->findByQuery(
            "select g from ExperimenterGroup g left outer join fetch g.config where g.id = :id", params));
    assertFooBar(group);
}
