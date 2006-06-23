package ome.server.utests;

import org.testng.annotations.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import ome.conditions.ApiUsageException;
import ome.model.containers.Project;
import ome.parameters.Parameters;
import ome.parameters.QueryParameter;
import ome.services.query.ClassQuerySource;
import ome.services.query.CollectionQueryParameterDef;
import ome.services.query.NullQuerySource;
import ome.services.query.PojosLoadHierarchyQueryDefinition;
import ome.services.query.Query;
import ome.services.query.QueryException;
import ome.services.query.QueryFactory;
import ome.services.query.QueryParameterDef;
import ome.services.query.QuerySource;
import ome.services.query.StringQuerySource;

import junit.framework.TestCase;

public class QueryParametersTest extends TestCase
{
    
    // ~ Exceptions
    // =========================================================================

    private static final String A = "1";
    private static final String B = "2";
    
    private QueryParameterDef def;
    private QueryParameter param;

    @Test
    @ExpectedExceptions( ApiUsageException.class )
    public void test_queryParameterWithNullName() throws Exception
    {
        def = new QueryParameterDef(A,String.class,false);
        param  = new QueryParameter(null,String.class,null);
        def.errorIfInvalid(param);
    }

    @Test
    @ExpectedExceptions( ApiUsageException.class )
    public void test_queryParameterWithDifferentName() throws Exception
    {
        def = new QueryParameterDef(A,String.class,false);
        param  = new QueryParameter(B,String.class,null);
        def.errorIfInvalid(param);
    }
    
    @Test
    @ExpectedExceptions( ApiUsageException.class )
    public void test_requiredQueryParameterWithNull() throws Exception
    {
        def = new QueryParameterDef(A,String.class,false);
        param  = new QueryParameter(A,String.class,null);
        def.errorIfInvalid(param);
    }
    
    @Test
    @ExpectedExceptions( ApiUsageException.class )
    public void test_queryParameterWithNullType() throws Exception
    {
        def = new QueryParameterDef(A,String.class,false);
        param  = new QueryParameter(A,null,null);
        def.errorIfInvalid(param);
    }

    @Test
    @ExpectedExceptions( ApiUsageException.class )
    public void test_RequiredNullLongCollection() throws Exception
    {
        def = new CollectionQueryParameterDef(A,false,Long.class);
        param = new QueryParameter(A,Set.class,null);
        def.errorIfInvalid(param);
    }

    @Test
    @ExpectedExceptions( ApiUsageException.class )
    public void test_RequiredEmptyLongCollection() throws Exception
    {
        def = new CollectionQueryParameterDef(A,false,Long.class);
        param = new QueryParameter(A,Set.class,Collections.EMPTY_SET);
        def.errorIfInvalid(param);
    }

    
    // ~ Examples
    // =========================================================================

    @Test
    public void test_matchingString() throws Exception
    {
        def = new QueryParameterDef(A,String.class,false);
        param = new QueryParameter(A,String.class,"");
        def.errorIfInvalid(param);
    }

    @Test
    public void test_optionalNullString() throws Exception
    {
        def = new QueryParameterDef(A,String.class,true);
        param = new QueryParameter(A,String.class,null);
        def.errorIfInvalid(param);
    }
    
    @Test
    public void test_LongCollection() throws Exception
    {
        def = new CollectionQueryParameterDef(A,false,Long.class);
        param = new QueryParameter(A,Set.class,Collections.singleton(1L));
        def.errorIfInvalid(param);
    }

    @Test
    public void test_NullLongCollection() throws Exception
    {
        def = new CollectionQueryParameterDef(A,true,Long.class);
        param = new QueryParameter(A,Set.class,null);
        def.errorIfInvalid(param);
    }

    @Test
    public void test_EmptyLongCollection() throws Exception
    {
        def = new CollectionQueryParameterDef(A,true,Long.class);
        param = new QueryParameter(A,Set.class,Collections.EMPTY_SET);
        def.errorIfInvalid(param);
    }
    
}
