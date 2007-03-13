#include <cppunit/extensions/HelperMacros.h>
#include <vector>
#include <stdexcept>

class MyTest : public CppUnit::TestFixture {
  CPPUNIT_TEST_SUITE( MyTest );
  CPPUNIT_TEST( testFails );
  CPPUNIT_TEST_EXCEPTION( testVectorAtThrow, std::out_of_range );
  CPPUNIT_TEST_SUITE_END();

public:
  void testFails()
  {
    CPPUNIT_ASSERT(false);
  }

  void testVectorAtThrow()
  {
    std::vector<int> v;
    v.at( 1 );     // must throw exception std::invalid_argument
  }
};
CPPUNIT_TEST_SUITE_REGISTRATION( MyTest );
