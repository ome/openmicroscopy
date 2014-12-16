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
#include <Ice/Handle.h>
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

typedef CallbackWrapper<TestCB> TestCBPtr;

class TestCB: virtual public CmdCallbackI {
private:
    // Preventing copy-construction and assigning by value.
    TestCB& operator=(const TestCB& rv);
    TestCB(TestCB&);
public:
    int steps;
    int finished;

    TestCB(const omero::client_ptr client, const HandlePrx& handle) :
        CmdCallbackI(client, handle), steps(0), finished(0) {
        }

    ~TestCB(){}

    // Expose protected event member.
    omero::util::concurrency::Event&
    getEvent() {
      return event;
    }

    virtual void step(int /*complete*/, int /*total*/, const Ice::Current&) {
        IceUtil::RecMutex::Lock lock(mutex);
        steps++;
    }

    virtual void onFinished(const ResponsePtr&,
            const StatusPtr&, const Ice::Current&) {
        IceUtil::RecMutex::Lock lock(mutex);
        finished++;
        event.set();
    }

    void assertSteps() {
        IceUtil::RecMutex::Lock lock(mutex);

        // Not guranteed to get called for all steps, as the callback can
        // get added on the server after the operation has already started
        // if there is network latency
        ASSERT_GE(steps, 1);
    }

    void assertFinished(bool testSteps = true) {
        try {
            IceUtil::RecMutex::Lock lock(mutex);
            if (0 == finished) {
                // If things work as expected, then
                // the callback is added to the server
                // in time for onFinished to be called.
                // If that has failed, then we try again
                // since there seems to be some race
                // condition in C++.
                poll();
            }
            ASSERT_GT(finished, 0);
            ASSERT_FALSE(isCancelled());
            ASSERT_FALSE(isFailure());
            ResponsePtr rsp = getResponse();
            if (!rsp) {
                FAIL() << "null response";
            }
            ERRPtr err = ERRPtr::dynamicCast(rsp);
            if (err) {
                ostringstream ss;
                omero::cmd::StringMap::iterator it;
                for (it=err->parameters.begin(); it != err->parameters.end(); it++ ) {
                    ss << (*it).first << " => " << (*it).second << endl;
                }
                FAIL()
                << "ERR!"
                << "cat:" << err->category << "\n"
                << "name:" << err->name << "\n"
                << "params:" << ss.str() << "\n";
            }

            if (testSteps) {
                assertSteps();
            }
        } catch (const omero::ValidationException& ve) {
            FAIL() << "validation exception:" << ve.message;
        }
    }

    void assertCancelled() {
        try {
            IceUtil::RecMutex::Lock lock(mutex);
            if (0 == finished) {
                // If things work as expected, then
                // the callback is added to the server
                // in time for onFinished to be called.
                // If that has failed, then we try again
                // since there seems to be some race
                // condition in C++.
                poll();
            }
            ASSERT_GT(finished, 0);
            ASSERT_TRUE(isCancelled());
        } catch (const omero::ValidationException& ve) {
            FAIL() << "validation exception:" << ve.message;
        }
    }
};

class CBFixture : virtual public Fixture {
public:

    TestCBPtr run(const RequestPtr& req, int addCbDelay = 0) {
        ExperimenterGroupPtr group = newGroup("rwr---");
        ExperimenterPtr user = newUser(group);
        login(user->getOmeName()->getValue(), user->getOmeName()->getValue());
        HandlePrx handle = client->getSession()->submit(req);
        TestCBPtr rv = new TestCB(client, handle);

        if (addCbDelay > 0) {
            omero::util::concurrency::Event event;
            event.wait(IceUtil::Time::milliSeconds(addCbDelay));
        }

        return rv;
    }

    // Timing
    // =========================================================================

    TestCBPtr timing(int millis, int steps, int addCbDelay = 0) {
        omero::cmd::TimingPtr t = new Timing();
        t->millisPerStep = millis;
        t->steps = steps;
        return run(t, addCbDelay);
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
            t->millisPerStep = 50;
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
    cb->getEvent().wait(IceUtil::Time::milliSeconds(1500));
    cb->assertFinished();
}

TEST(CmdCallbackTest, testTimingFinishesOnBlock) {
    CBFixture f;
    TestCBPtr cb = f.timing(25, 4 * 10); // Runs 1 second
    cb->block(1500);
    cb->assertFinished();
}

TEST(CmdCallbackTest, testTimingFinishesOnLoop) {
    CBFixture f;
    TestCBPtr cb = f.timing(25, 4 * 10); // Runs 1 second
    cb->loop(3, 500);
    cb->assertFinished();
}

TEST(CmdCallbackTest, testDoNothingFinishesOnLatch) {
    CBFixture f;
    TestCBPtr cb = f.doAllOfNothing();
    cb->getEvent().wait(IceUtil::Time::milliSeconds(5000));
    cb->assertCancelled();
}

TEST(CmdCallbackTest, testDoNothingFinishesOnLoop) {
    CBFixture f;
    TestCBPtr cb = f.doAllOfNothing();
    cb->loop(5, 1000);
    cb->assertCancelled();
}

TEST(CmdCallbackTest, testDoAllTimingFinishesOnLoop) {
    try {
        CBFixture f;
        TestCBPtr cb = f.doAllTiming(5);
        cb->loop(5, 1000);
        cb->assertFinished();
        // For some reason the number of steps is varying between 10 and 15
    } catch (const Ice::ConnectionLostException& cle) {
        FAIL() << "connection lost: " << cle << endl;
    }
}

TEST(CmdCallbackTest, testAddAfterFinish) {
    CBFixture f;
    TestCBPtr cb = f.timing(25, 4 * 10, 1200); // Runs 1 second
    cb->getEvent().wait(IceUtil::Time::milliSeconds(1500));
    cb->assertFinished(false);
}
