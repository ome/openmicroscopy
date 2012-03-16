To add a test, add a cpp file under either the test/unit
or test/integration directory. Do not add a main method
but rather use the TEST() macro -- available from either
the gtest.h header or the omero/fixture.h -- to create
a test:

TEST(TestName, methodName) {
    ASSERT_EQ(1, 1);
    ASSERT_NE(1, 2);
    ASSERT_GT(2, 1);
    ASSERT_LE(1, 1);
    FAIL() << "This will never succeed.";
}

These can be run either manually:

    DYLD_LIBRARY_PATH=. test/integration.exe
    DYLD_LIBRARY_PATH=. test/unit.exe --gtest_filter=Model*

or via the build:

    ./build.py -f components/tools/OmeroCpp/build.xml integration
    ./build.py -f components/tools/OmeroCpp/build.xml test -DTEST=Model*
