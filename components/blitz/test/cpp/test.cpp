#include <cppunit/extensions/HelperMacros.h>
#include <vector>
#include <stdexcept>
#include <OMERO/client.h>

using namespace omero::model;

class MyTest : public CppUnit::TestFixture {
  CPPUNIT_TEST_SUITE( MyTest );
  CPPUNIT_TEST( testModel );
  CPPUNIT_TEST( testIterators );
  CPPUNIT_TEST_FAIL( testFails );
  CPPUNIT_TEST_EXCEPTION( testVectorAtThrow, std::out_of_range );
  CPPUNIT_TEST_SUITE_END();


public:

  void testModel()
  {
    ExperimenterIPtr user = new ExperimenterI();
    user->setFirstName(new OMERO::CString("test"));
    user->setLastName(new OMERO::CString("user"));
    user->setOmeName(new OMERO::CString("UUID"));

    // possibly setOmeName() and setOmeName(string) ??
    // and then don't need OMERO/types.h

    ExperimenterGroupIPtr group = new ExperimenterGroupI();
    // TODOuser->linkExperimenterGroup(group);
    GroupExperimenterMapIPtr map = new GroupExperimenterMapI();
    map->parent = group;
    map->child  = user;
  }

  typedef std::vector<int> v_int;
  typedef v_int::iterator v_int_it;
  void testIterators()
  {
    v_int v; 
    v.push_back(1);
    v.push_back(2);
    
    v_int addTo;
    v_int_it loc = addTo.end();
    v_int_it beg = v.begin();
    v_int_it end = v.end();

    addTo.insert(loc,beg,end);
  }

  void testFails()
  {
    CPPUNIT_ASSERT(false);
  }

  void testVectorAtThrow()
  {
    v_int v;
    v.at( 1 );     // must throw exception std::invalid_argument
  }
};
CPPUNIT_TEST_SUITE_REGISTRATION( MyTest );
