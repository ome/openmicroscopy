TESTS = test
check_PROGRAMS = $(TESTS)
test_SOURCES = run.cpp test.cpp
test_CXXFLAGS = $(CPPUNIT_CFLAGS)
test_LDFLAGS = -dl $(CPPUNIT_LIBS)

all: test run

run:
	./test