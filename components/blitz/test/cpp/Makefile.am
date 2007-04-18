TESTS = test
check_PROGRAMS = $(TESTS)

if CPPUNIT
test_SOURCES = run.cpp test.cpp
test_CXXFLAGS = -I../../src/cpp/ -I../../src/cpp/generated $(CPPUNIT_CFLAGS)
test_LDFLAGS = -lstdc++ -lOMERO_client -lOMERO_common -lIce -lIceUtil -lSlice -lGlacier2 -dl $(CPPUNIT_LIBS)

else
test_SOURCES = nocppunit.cpp
endif

all: test run

run:
	./test

