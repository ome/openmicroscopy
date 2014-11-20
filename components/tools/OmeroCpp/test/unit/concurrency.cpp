#include <list>

#include <omero/IceNoWarnPush.h>
#include <IceUtil/Thread.h>
#include <IceUtil/Config.h>
#include <IceUtil/Handle.h>
#include <omero/IceNoWarnPop.h>

#include <omero/util/concurrency.h>
#include <omero/fixture.h>

using namespace omero::util::concurrency;
using namespace omero;
using namespace std;

class BaseThread;

typedef IceUtil::Handle<BaseThread> BaseThreadPtr;

class BaseThread : public IceUtil::Thread {
public:
    bool passed;
    IceUtil::Time ms(long ms) {
        return IceUtil::Time::milliSeconds(ms);
    };
};

Event event;

class ReaderThread : public BaseThread {
    virtual void run() {
        if (event.wait(ms(1000))) {
            passed = true;
        }
    }
};

class WriterThread : public BaseThread {
    virtual void run() {
        event.set();
        passed = true;
    }
};

TEST(ConcurrencyTest, testEvent )
{
        list<BaseThreadPtr> rs;
        list<IceUtil::ThreadControl> rcs;
        for (int i = 0; i < 10; i++) {
            BaseThreadPtr r = new ReaderThread();
            rs.push_back(r);
            rcs.push_back(r->start());
        }

        BaseThreadPtr w = new WriterThread();
        IceUtil::ThreadControl wc = w->start();
        wc.join();
        ASSERT_TRUE( (*w).passed );

        for (int i = 0; i < 10; i++) {
            BaseThreadPtr r = rs.front();
            IceUtil::ThreadControl tc = rcs.front();
            rs.pop_front();
            rcs.pop_front();
            tc.join();
            ASSERT_TRUE( (*r).passed );
        }
}

TEST(ConcurrencyTest, testEventFlag)
{
    Event event;
    ASSERT_FALSE(event.isSet());

    event.set();
    ASSERT_TRUE(event.isSet());

    event.clear();
    ASSERT_FALSE(event.isSet());

    event.set();
    ASSERT_TRUE(event.wait(IceUtil::Time::milliSeconds(100)));
}
