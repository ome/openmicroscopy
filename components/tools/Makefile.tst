#
# unit_tests and integration_tests must be defined.
#

TESTS = boost
check_PROGRAMS = $(TESTS)
bin_PROGRAMS = blitzdemo

OmeroCpp ?= ../../OmeroCpp

our_libp = -L$(OmeroCpp)/src/.libs
our_libs = -lOMERO_common -lOMERO_client -lstdc++ -lIce -lIceUtil -lSlice -lGlacier2
our_incl = -I$(OmeroCpp)/src -I$(OmeroCpp)/src/slice_generated -I$(OmeroCpp)/target/temp

if BOOST_TEST
boost_SOURCES = $(unit_tests)
boost_CXXFLAGS = -g -O0 $(our_incl)
boost_LDFLAGS = $(our_libp) -dl -lboost_unit_test_framework $(our_libs)
else
boost_SOURCES = no_op.cpp
endif

if BOOST_TEST
blitzdemo_SOURCES = $(integration_tests)
blitzdemo_CXXFLAGS = -g -O0 $(our_incl)
blitzdemo_LDFLAGS = $(our_libp) -dl -lboost_unit_test_framework $(our_libs)
else
blitzdemo_SOURCES = no_op.cpp
endif


all: boost run

run:
	./boost

integration: blitzdemo
	./blitzdemo
