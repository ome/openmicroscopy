TESTS = test
check_PROGRAMS = $(TESTS)

if CPPUNIT
test_SOURCES = run.cpp test.cpp
test_CXXFLAGS = $(CPPUNIT_CFLAGS)
test_LDFLAGS = -dl $(CPPUNIT_LIBS)
else
test_SOURCES = nocppunit.cpp
endif

all: test run

run:
	./test

