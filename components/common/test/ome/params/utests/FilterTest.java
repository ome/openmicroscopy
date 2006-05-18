package ome.params.utests;

import org.testng.annotations.*;
import junit.framework.TestCase;

import ome.conditions.ValidationException;
import ome.parameters.Filter;
import ome.parameters.Page;

public class FilterTest extends TestCase 
{

  @Test
    public void test_simplePage() throws Exception
    {
        Page p = new Page(0,10);
        assertTrue( p.limit() == 10);
        assertTrue( p.offset() == 0);
    }
    
    
  @Test
    public void test_simpleFilter() throws Exception
    {
        Filter f = new Filter();
        f.page( 0, 10 );
        assertTrue( 0 == f.firstResult() );
        assertTrue( 10 == f.maxResults() );
    }
    
    // Error tests
  @Test
  @ExpectedExceptions(ValidationException.class)
    public void test_errorPage() throws Exception
    {
        Page p = new Page(-1,-1);
    }
    
    
  @Test
  @ExpectedExceptions(ValidationException.class)
    public void test_errorFilter() throws Exception
    {
        Filter f = new Filter();
        f.page( -1, -1 );
    }
        
}
