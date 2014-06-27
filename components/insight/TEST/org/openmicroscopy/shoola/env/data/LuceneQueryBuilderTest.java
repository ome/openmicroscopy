/*
 * Copyright (C) 2014 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.openmicroscopy.shoola.env.data;

import java.util.ArrayList;
import java.util.List;

import org.openmicroscopy.shoola.util.ui.search.InvalidQueryException;
import org.openmicroscopy.shoola.util.ui.search.LuceneQueryBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test(testName = "LuceneQueryBuilderTest", enabled=false)
public class LuceneQueryBuilderTest {

    @Test(testName="buildQueryString")
    void testBuildQueryString() throws InvalidQueryException {
        
        String raw, expected;
        List<String> fields = new ArrayList<String>();
        
        
     // +        # No fields are provided
     // ----------------------------------
        
     // +        assertQuery("dv", [], "dv", False)
        raw = "dv";  expected = "dv";
        checkQuery(fields, raw, expected);
        
     // +        assertQuery("test dv", [], "test dv", False)
        raw = "test dv";  expected = "test dv";
        checkQuery(fields, raw, expected);
        
     // +        assertQuery("*test dv", [], "*test dv", True)           # * wildcards
        raw = "*test dv";  expected = "*test dv";
        checkQuery(fields, raw, expected);
        
     // +        assertQuery("test *dv", [], "test *dv", True)
        raw = "test *dv";  expected = "test *dv";
        checkQuery(fields, raw, expected);
        
     // +        assertQuery("?test dv", [], "?test dv", True)           # ? wildcards
        raw = "?test dv";  expected = "?test dv";
        checkQuery(fields, raw, expected);
        
     // +        assertQuery("test ?dv", [], "test ?dv", True)
        raw = "test ?dv";  expected = "test ?dv";
        checkQuery(fields, raw, expected);
        
     // +        assertQuery("test * dv", [], "test dv", False)          # single wildcards ignored
        raw = "test * dv";  expected = "test dv";
        checkQuery(fields, raw, expected);
        
     // +        assertQuery("test *.dv", [], "test dv", False)
        raw = "test *.dv";  expected = "test dv";
        checkQuery(fields, raw, expected);
        
     // +        assertQuery('test "*dv"', [], 'test "*dv"', False)      # wildcards have no effect in "*quotes"
        raw = "test \"*dv\"";  expected = "test \"*dv\"";
        checkQuery(fields, raw, expected);
        
     // +        assertQuery('"?test *dv"', [], '"?test *dv"', False)
        raw = "\"?test *dv\"";  expected = "\"?test *dv\"";
        checkQuery(fields, raw, expected);
        
     // +        assertQuery('(test-dv}', [], 'test dv', False)            # strip all non-alpha-numerics
        raw = "test-dv";  expected = "test dv )";
        checkQuery(fields, raw, expected);
        
     // +        assertQuery('*test_dv', [], '*test_dv', True)           # except wildcards (and underscores?)
        raw = "*test_dv";  expected = "*test_dv";
        checkQuery(fields, raw, expected);
        
     // +        assertQuery("test AND dv", [], "test AND dv", False)    # AND operator is preserved
        raw = "test AND dv";  expected = "test AND dv";
        checkQuery(fields, raw, expected);
       
        
        
     // +        # single field
     // ----------------------------------
        fields.add("name");
        
     // +        assertQuery("dv", ['name'], "(name:dv)", False)
        raw = "dv";  expected = "name:dv";
        checkQuery(fields, raw, expected);
        
     // +        assertQuery("test dv", ['name'], "(name:test name:dv)", False)
        raw = "test dv";  expected = "name:test name:dv";
        checkQuery(fields, raw, expected);
        
     // +        assertQuery("*test dv", ['name'], "(name:*test name:dv)", True)         # * wildcards
        raw = "*test dv";  expected = "name:*test name:dv";
        checkQuery(fields, raw, expected);
        
     // +        assertQuery("test *dv", ['name'], "(name:test name:*dv)", True)
        raw = "test *dv";  expected = "name:test name:*dv";
        checkQuery(fields, raw, expected);
     // +        assertQuery("?test dv", ['name'], "(name:?test name:dv)", True)         # ? wildcards
        raw = "?test dv";  expected = "name:?test name:dv";
        checkQuery(fields, raw, expected);
        
     // +        assertQuery("test ?dv", ['name'], "(name:test name:?dv)", True)
        raw = "test ?dv";  expected = "name:test name:?dv";
        checkQuery(fields, raw, expected);
     // +        assertQuery("test * dv", ['name'], "(name:test name:dv)", False)        # single wildcards ignored
        raw = "test * dv";  expected = "name:test name:dv";
        checkQuery(fields, raw, expected);
        
     // +        assertQuery("test *.dv", ['name'], "(name:test name:dv)", False)
        raw = "test *.dv";  expected = "name:test name:dv";
        checkQuery(fields, raw, expected);
        
     // +        assertQuery('test "*dv"', ['name'], '(name:test name:"*dv")', False)    # wildcards have no effect in "*quotes"
        raw = "test \"*dv\"";  expected = "name:test name:*dv";
        checkQuery(fields, raw, expected);
        
     // +        assertQuery('"?test *dv"', ['name'], '(name:"?test *dv")', False)
        raw = "\"?test *.dv\"";  expected = "name:\"?test dv\"";
        checkQuery(fields, raw, expected);
        
     // +        assertQuery('(test-dv}', ['name'], '(name:test name:dv)', False)        # strip all non-alpha-numerics
        raw = "(test-dv}";  expected = "name:test name:dv";
        checkQuery(fields, raw, expected);
        
     // +        assertQuery('*test_dv', ['name'], '(name:*test_dv)', True)               # except wildcards (and underscores?)
        raw = "*test_dv";  expected = "name:*test_dv";
        checkQuery(fields, raw, expected);
        
     // +        assertQuery("test AND dv", ['name'], "(name:test AND name:dv)", False)  # AND operator is preserved
        raw = "test AND dv";  expected = "name:test AND name:dv";
        checkQuery(fields, raw, expected);
        
        
     // +
     // +        # multiple fields
     // ----------------------------------
     // +        f = ['name', 'description']
     // +        assertQuery("dv", f, "(name:dv) OR (description:dv)", False)
     // +        assertQuery("test dv", f, "(name:test name:dv) OR (description:test description:dv)", False)
     // +        assertQuery("*test dv", f, "(name:*test name:dv) OR (description:*test description:dv)", True)         # * wildcards
     // +        assertQuery("test *dv", f, "(name:test name:*dv) OR (description:test description:*dv)", True)
        
    }
    
    private void checkQuery(List<String> fields, String raw, String expected) throws InvalidQueryException {
        System.out.println("Checking query: "+raw);
        String processed = LuceneQueryBuilder.buildLuceneQuery(fields, raw);
        Assert.assertEquals(processed, expected);
    }
}
