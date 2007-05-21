TESTS = boost cppunit
check_PROGRAMS = $(TESTS)
bin_PROGRAMS = blitzdemo

our_libp = -L@top_srcdir@/src/.libs
our_libs = -lOMERO_common -lOMERO_client -lstdc++ -lIce -lIceUtil -lSlice -lGlacier2
our_incl = -I../src -I../src/slice_generated -I../target/temp

if BOOST_TEST
boost_SOURCES = boost_fixture.cpp boost_example.cpp boost_model.cpp boost_perms.cpp boost_client.cpp boost_operators.cpp boost_api.cpp
boost_CXXFLAGS = -g -O0 $(our_incl)
boost_LDFLAGS = $(our_libp) -dl -lboost_unit_test_framework $(our_libs)
else
boost_SOURCES = no_op.cpp
endif


if CPPUNIT
cppunit_SOURCES = run.cpp cppunit_example.cpp
cppunit_CXXFLAGS = -g -O0 $(our_incl) $(CPPUNIT_CFLAGS)
cppunit_LDFLAGS = $(our_libp) -dl $(CPPUNIT_LIBS) $(our_libs)
else
cppunit_SOURCES = no_op.cpp
endif


if BOOST_TEST
blitzdemo_SOURCES = blitzdemo.cpp
blitzdemo_CXXFLAGS = -g -O0 $(our_incl)
blitzdemo_LDFLAGS = $(our_libp) -dl -lboost_unit_test_framework $(our_libs)
else
blitzdemo_SOURCES = no_op.cpp
endif


all: boost cppunit run

run:
	./boost
	./cppunit

integration: blitzdemo
	./blitzdemo
