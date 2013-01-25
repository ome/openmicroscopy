/*
 * Copyright (C) 2012 Glencoe Software, Inc. All rights reserved.
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
#include <omero/callbacks.h>
#include <omero/all.h>
#include <string>
#include <map>
#include <IceUtil/RecMutex.h>
#include <IceUtil/Config.h>
#if ICE_INT_VERSION / 100 >= 304
#   include <Ice/Handle.h>
#else
#   include <IceUtil/Handle.h>
#endif
#include <omero/util/concurrency.h>

using namespace std;
using namespace omero;
using namespace omero::api;
using namespace omero::cmd;
using namespace omero::callbacks;
using namespace omero::model;
using namespace omero::rtypes;
using namespace omero::sys;

class TestCB;

#if ICE_INT_VERSION / 100 >= 304
typedef IceInternal::Handle<TestCB> TestCBPtr;
#else
typedef IceUtil::Handle<TestCB> TestCBPtr;
#endif

class TestCB: virtual public CmdCallbackI {
private:
    // Preventing copy-construction and assigning by value.
    TestCB& operator=(const TestCB& rv);
    TestCB(TestCB&);
    IceUtil::RecMutex mutex;
public:
    omero::util::concurrency::Event event;
    int steps;
    int finished;

    TestCB(const omero::client_ptr client, const HandlePrx& handle) :
        CmdCallbackI(client, handle), steps(0), finished(0) {}
    ~TestCB(){}

    virtual void step(int complete, int total, const Ice::Current& current = Ice::Current()) {
	IceUtil::RecMutex::Lock lock(mutex);
        steps++;
    }

    virtual void onFinished(const ResponsePtr& rsp,
            const StatusPtr& s, const Ice::Current& current = Ice::Current()) {
        IceUtil::RecMutex::Lock lock(mutex);
        finished++;
        event.set();
    }

    void assertSteps(int expected) {
        IceUtil::RecMutex::Lock lock(mutex);
        ASSERT_EQ(expected, steps);

    }

    void assertFinished(int expectedSteps = -1) {
        IceUtil::RecMutex::Lock lock(mutex);
        ASSERT_EQ(1, finished);
        ASSERT_FALSE(isCancelled());
        ASSERT_FALSE(isFailure());
        ResponsePtr rsp = getResponse();
        if (!rsp) {
            FAIL() << "null response";
        }
        ERRPtr err = ERRPtr::dynamicCast(rsp);
        if (err) {
            stringstream ss;
            omero::cmd::StringMap::iterator it;
            for (it=err->parameters.begin(); it != err->parameters.end(); it++ ) {
                ss << (*it).first << " => " << (*it).second << endl;
            }
            FAIL()
             << "ERR!"
             << "cat:" << err->category << "\n"
             << "name:" << err->name << "\n"
             << "params:" << ss << "\n";
        }

        if (expectedSteps >= 0) {
            assertSteps(expectedSteps);
        }
    }

    void assertCancelled() {
        IceUtil::RecMutex::Lock lock(mutex);
        ASSERT_EQ(1, finished);
        ASSERT_TRUE(isCancelled());
    }
};

class CBFixture : virtual public Fixture {
public:
    TestCBPtr run(const RequestPtr& req) {
        ExperimenterPtr user = newUser();
        client_ptr client = login(user->getOmeName()->getValue());
        HandlePrx handle = client->getSession()->submit(req);
        return new TestCB(client, handle);
    }

    // Timing
    // =========================================================================

    TestCBPtr timing(int millis, int steps) {
        omero::cmd::TimingPtr t = new Timing();
        t->millisPerStep = millis;
        t->steps = steps;
        return run(t);
    }

    // DoAll
    // =========================================================================

    TestCBPtr doAllOfNothing() {
        return run(new omero::cmd::DoAll());
    }

    TestCBPtr doAllTiming(int count) {
        omero::cmd::RequestList timings;
        for (int i = 0; i < count; i++) {
            omero::cmd::TimingPtr t = new omero::cmd::Timing();
            t->steps = 3;
            t->millisPerStep = 2;
            timings.push_back(t);
        }
        omero::cmd::DoAllPtr all = new omero::cmd::DoAll();
        all->requests = timings;
        return run(all);
    }

};

TEST(CmdCallbackTest, testTimingFinishesOnLatch) {
    CBFixture f;
    TestCBPtr cb = f.timing(25, 4 * 10); // Runs 1 second
    cb->event.wait(IceUtil::Time::milliSeconds(1500));
    ASSERT_EQ(1, cb->finished);
    cb->assertFinished(10); // Modulus-10
}

TEST(CmdCallbackTest, testTimingFinishesOnBlock) {
    CBFixture f;
    TestCBPtr cb = f.timing(25, 4 * 10); // Runs 1 second
    cb->block(1500);
    cb->assertFinished(10); // Modulus-10
}

TEST(CmdCallbackTest, testTimingFinishesOnLoop) {
    CBFixture f;
    TestCBPtr cb = f.timing(25, 4 * 10); // Runs 1 second
    cb->loop(3, 500);
    cb->assertFinished(10); // Modulus-10
}

TEST(CmdCallbackTest, testDoNothingFinishesOnLatch) {
    CBFixture f;
    TestCBPtr cb = f.doAllOfNothing();
    cb->event.wait(IceUtil::Time::milliSeconds(5000));
    cb->assertCancelled();
}

TEST(CmdCallbackTest, testDoNothingFinishesOnLoop) {
    CBFixture f;
    TestCBPtr cb = f.doAllOfNothing();
    cb->loop(5, 1000);
    cb->assertCancelled();
}

TEST(CmdCallbackTest, testDoAllTimingFinishesOnLoop) {
    CBFixture f;
    TestCBPtr cb = f.doAllTiming(5);
    cb->loop(5, 1000);
    cb->assertFinished();
    // For some reason the number of steps is varying between 10 and 15
}
