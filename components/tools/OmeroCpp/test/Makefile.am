TESTS = unit
check_PROGRAMS = $(TESTS)
bin_PROGRAMS = integration

export BOOST_TEST_DYN_LINK ?= 1
export ICE_CONFIG ?= ../../../../etc/local.properties

our_libp = -L@top_srcdir@/src/.libs
our_libs = -lOMERO_common -lOMERO_client -lstdc++ -lIce -lIceUtil -lSlice -lGlacier2 #-lpthread
our_incl = -I../src -I../src/slice_generated -I../target/temp

if BOOST_TEST
unit_SOURCES = boost_main.cpp boost_fixture.cpp boost_model.cpp boost_perms.cpp boost_client.cpp boost_operators.cpp
unit_CXXFLAGS = -g -O0 $(our_incl)
unit_LDFLAGS = $(our_libp) -dl -lboost_unit_test_framework-mt-d $(our_libs)
else
unit_SOURCES = no_op.cpp
endif

if BOOST_TEST
integration_SOURCES = boost_main.cpp boost_fixture.cpp boost_scripts.cpp boost_search.cpp boost_sessions.cpp boost_annotations.cpp boost_counts.cpp boost_beta3.cpp
integration_CXXFLAGS = -g -O0 $(our_incl)
integration_LDFLAGS = $(our_libp) -dl -lboost_unit_test_framework-mt-d $(our_libs)
else
integration_SOURCES = no_op.cpp
endif


all: unit integration run

run:
	./unit ; ICE_CONFIG=$(ICE_CONFIG) ./integration
