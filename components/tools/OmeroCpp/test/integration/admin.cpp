/*
 * Copyright (C) 2013 Glencoe Software, Inc. All rights reserved.
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
#include <omero/API.h>
#include <omero/Collections.h>


using namespace std;
using namespace omero;
using namespace omero::api;
using namespace omero::model;


TEST(AdminTest, getGroup) {
    Fixture f;
    f.login();
    
    ServiceFactoryPrx sf = f.client->getSession();
    IAdminPrx admin = sf->getAdminService();
    
    ExperimenterGroupList groups = admin->lookupGroups();
    ExperimenterGroupPtr g = admin->getGroup(groups[0]->getId()->getValue());
    
    ASSERT_EQ(g->getId()->getValue(), groups[0]->getId()->getValue());
    ASSERT_GT(g->sizeOfGroupExperimenterMap(), 0);
}
