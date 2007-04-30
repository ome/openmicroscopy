TESTS = boost cppunit
check_PROGRAMS = $(TESTS)

our_libp = -L@top_srcdir@/src/cpp/.libs
our_libs = -lOMERO_common -lOMERO_client

if BOOST_TEST
boost_SOURCES = boost_fixture.cpp boost_example.cpp boost_model.cpp 
boost_CXXFLAGS = -g -O0 -I../../src/cpp/ -I../../src/cpp/generated 
boost_LDFLAGS = -Wl,-Map=.map $(our_libp) -lstdc++ -lIce -lIceUtil -lSlice -lGlacier2 -dl -lboost_unit_test_framework $(our_libs)
else
boost_SOURCES = no_op.cpp
endif

if CPPUNIT
cppunit_SOURCES = run.cpp cppunit_example.cpp
cppunit_CXXFLAGS = -g -O0 -I../../src/cpp/ -I../../src/cpp/generated $(CPPUNIT_CFLAGS)
cppunit_LDFLAGS = $(our_libp) -lstdc++ -lIce -lIceUtil -lSlice -lGlacier2 -dl $(CPPUNIT_LIBS) $(our_libs)
else
cppunit_SOURCES = no_op.cpp
endif

all: boost cppunit run

run:
	./boost
	./cppunit
