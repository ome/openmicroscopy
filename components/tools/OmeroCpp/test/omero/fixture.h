/*
 *   Copyright 2007-2012 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_FIXTURE_H
#define OMERO_FIXTURE_H

// domain
#include <omero/all.h>
#include <omero/client.h>
#include <omero/RTypesI.h>
#include <omero/ClientErrors.h>
#include <omero/ServerErrors.h>
#include <omero/model/ImageI.h>
#include <omero/model/PixelsI.h>
#include <omero/model/ExperimenterI.h>

// gtest
#include "gtest/gtest.h"

#ifdef LINUX
// stackframe
#include <execinfo.h>
#endif

// std
#include <exception>
#include <cstdlib>
#include <string>
#include <vector>

omero::model::ImagePtr new_ImageI();

struct Fixture
{
    protected:
        std::vector<omero::client*> clients;
    public:
        Fixture();
        ~Fixture();
        void show_stackframe();
        void printUnexpected();
        bool passed();
        std::string uuid();
        omero::client_ptr login(
                const std::string& username = std::string(),
                const std::string& password = std::string());
        omero::client_ptr root_login();
	omero::model::ExperimenterPtr newUser(const omero::api::IAdminPrx& admin, const omero::model::ExperimenterGroupPtr& g = omero::model::ExperimenterGroupPtr());
	omero::model::PixelsIPtr pixels();
};

#endif // OMERO_FIXTURE_H
