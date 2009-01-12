/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_BOOST_FIXTURE_H
#define OMERO_BOOST_FIXTURE_H

// domain
#include <omero/client.h>
#include <omero/RTypesI.h>
#include <omero/ClientErrors.h>
#include <omero/ServerErrors.h>
#include <omero/model/ImageI.h>
#include <omero/model/ExperimenterI.h>

// boost
#include <boost/test/unit_test.hpp>
#include <boost/test/unit_test_log.hpp>
#include <boost/test/framework.hpp>
#include <boost/test/results_collector.hpp>
#include <boost/test/results_reporter.hpp>
#include <boost/test/unit_test_monitor.hpp>
#ifdef LINUX
// stackframe
#include <execinfo.h>
#endif
// std
#include <exception>
#include <cstdlib>
#include <string>
#include <vector>

// see first_failed_assertion dbg hook

namespace b_ut = boost::unit_test;

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
        b_ut::test_case const & current();
        b_ut::unit_test_monitor_t& monitor();
        b_ut::unit_test_log_t& log();
        bool passed();
        std::string uuid();
        omero::client_ptr login(
                const std::string& username = std::string(),
                const std::string& password = std::string());
        omero::client_ptr root_login();
};

//
// Not functional
//

#define WITH_FIXTURE_TEST( name )             \
BOOST_AUTO_TEST_CASE( name )                  \
{ Fixture f;

#endif // OMERO_BOOST_FIXTURE_H
