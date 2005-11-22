package ome.util.utests;

import java.util.List;
import java.util.Map;
import ome.testing.SqlPropertiesParser;


import junit.framework.TestCase;

public class SqlPropertiesParserTest extends TestCase {

    public void testSimpleRead(){
        Map m = SqlPropertiesParser.parse(new String[]{"test_data.properties"});
        assertTrue(m.get("simple.test") instanceof List);
    }

}
