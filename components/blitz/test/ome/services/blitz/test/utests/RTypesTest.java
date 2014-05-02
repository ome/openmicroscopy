/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.test.utests;

import static omero.rtypes.rarray;
import static omero.rtypes.rbool;
import static omero.rtypes.rdouble;
import static omero.rtypes.rfloat;
import static omero.rtypes.rint;
import static omero.rtypes.rinternal;
import static omero.rtypes.rlist;
import static omero.rtypes.rlong;
import static omero.rtypes.rmap;
import static omero.rtypes.robject;
import static omero.rtypes.rset;
import static omero.rtypes.rstring;
import static omero.rtypes.rtime;
import static omero.rtypes.rtype;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import omero.ClientError;
import omero.RList;
import omero.RLong;
import omero.RMap;
import omero.RSet;
import omero.RString;
import omero.RType;
import omero.grid.JobParams;
import omero.model.ImageI;

import org.testng.annotations.Test;

public class RTypesTest{

    @Test
    public void testConversionMethod() {
        assertNull(rtype(null));
	assertEquals(rlong(1), rtype(rlong(1))); // Returns self
        assertEquals(rbool(true), rtype(Boolean.valueOf(true)));
        assertEquals(rdouble(0), rtype(Double.valueOf(0)));
        assertEquals(rfloat(0), rtype(Float.valueOf(0)));
        assertEquals(rlong(0), rtype(Long.valueOf(0)));
        assertEquals(rint(0), rtype(Integer.valueOf(0)));
        assertEquals(rstring("string"), rtype("string"));
        long time = System.currentTimeMillis();
        assertEquals(rtime(time), rtype(new Timestamp(time)));
        rtype(new ImageI());
        rtype(new JobParams());
        rtype(new HashSet(Arrays.asList(rlong(1))));
        rtype(Arrays.asList(rlong(2)));
        rtype(new HashMap());
        try {
            rtype(new RType[] {});
            fail("Shouldn't be able to handle this yet");
        } catch (ClientError ce) {
            // ok
        }
    }
    
    @Test
    public void testObjectCreationEqualsAndHash() {

        // RBool
        omero.RBool true1 = rbool(true);
        omero.RBool true2 = rbool(true);
        omero.RBool false1 = rbool(false);
        omero.RBool false2 = rbool(false);
        assertTrue(true1 == true2);
        assertTrue(false1 == false2);
        assertTrue(true1.getValue());
        assertTrue(!false1.getValue());
        assertTrue(true1.equals(true2));
        assertTrue(!true1.equals(false1));

        // RDouble
        omero.RDouble double_zero1 = rdouble(0.0);
        omero.RDouble double_zero2 = rdouble(0.0);
        omero.RDouble double_notzero1 = rdouble(1.1);
        omero.RDouble double_notzero1b = rdouble(1.1);
        omero.RDouble double_notzero2 = rdouble(2.2);
        assertTrue(double_zero1.getValue() == 0.0);
        assertTrue(double_notzero1.getValue() == 1.1);
        assertTrue(double_zero1.equals(double_zero2));
        assertTrue(!double_zero1.equals(double_notzero1));
        assertTrue(double_notzero1.equals(double_notzero1b));
        assertTrue(!double_notzero1.equals(double_notzero2));

        // RFloat
        omero.RFloat float_zero1 = rfloat(0.0f);
        omero.RFloat float_zero2 = rfloat(0.0f);
        omero.RFloat float_notzero1 = rfloat(1.1f);
        omero.RFloat float_notzero1b = rfloat(1.1f);
        omero.RFloat float_notzero2 = rfloat(2.2f);
        assertTrue(float_zero1.getValue() == 0.0);
        assertTrue(float_notzero1.getValue() == 1.1f);
        assertTrue(float_zero1.equals(float_zero2));
        assertTrue(!float_zero1.equals(float_notzero1));
        assertTrue(float_notzero1.equals(float_notzero1b));
        assertTrue(!float_notzero1.equals(float_notzero2));

        // RInt
        omero.RInt int_zero1 = rint(0);
        omero.RInt int_zero2 = rint(0);
        omero.RInt int_notzero1 = rint(1);
        omero.RInt int_notzero1b = rint(1);
        omero.RInt int_notzero2 = rint(2);
        assertTrue(int_zero1.getValue() == 0);
        assertTrue(int_notzero1.getValue() == 1);
        assertTrue(int_zero1.equals(int_zero2));
        assertTrue(!int_zero1.equals(int_notzero1));
        assertTrue(int_notzero1.equals(int_notzero1b));
        assertTrue(!int_notzero1.equals(int_notzero2));

        // RLong
        omero.RLong long_zero1 = rlong(0);
        omero.RLong long_zero2 = rlong(0);
        omero.RLong long_notzero1 = rlong(1);
        omero.RLong long_notzero1b = rlong(1);
        omero.RLong long_notzero2 = rlong(2);
        assertTrue(long_zero1.getValue() == 0);
        assertTrue(long_notzero1.getValue() == 1);
        assertTrue(long_zero1.equals(long_zero2));
        assertTrue(!long_zero1.equals(long_notzero1));
        assertTrue(long_notzero1.equals(long_notzero1b));
        assertTrue(!long_notzero1.equals(long_notzero2));

        // RTime
        omero.RTime time_zero1 = rtime(0);
        omero.RTime time_zero2 = rtime(0);
        omero.RTime time_notzero1 = rtime(1);
        omero.RTime time_notzero1b = rtime(1);
        omero.RTime time_notzero2 = rtime(2);
        assertTrue(time_zero1.getValue() == 0);
        assertTrue(time_notzero1.getValue() == 1);
        assertTrue(time_zero1.equals(time_zero2));
        assertTrue(!time_zero1.equals(time_notzero1));
        assertTrue(time_notzero1.equals(time_notzero1b));
        assertTrue(!time_notzero1.equals(time_notzero2));

        // RInternal
        omero.RInternal internal_null1 = rinternal(null);
        omero.RInternal internal_null2 = rinternal(null);
        omero.RInternal internal_notnull1 = rinternal(new omero.grid.JobParams());
        omero.RInternal internal_notnull2 = rinternal(new omero.grid.JobParams());
        assertTrue(internal_null1 == internal_null2);
        assertTrue(internal_null1.equals(internal_null2));
        assertTrue(!internal_null1.equals(internal_notnull2));
        assertTrue(internal_notnull1.equals(internal_notnull1));
        assertTrue(!internal_notnull1.equals(internal_notnull2));

        // RObject
        omero.RObject object_null1 = robject(null);
        omero.RObject object_null2 = robject(null);
        omero.RObject object_notnull1 = robject(new omero.model.ImageI());
        omero.RObject object_notnull2 = robject(new omero.model.ImageI());
        assertTrue(object_null1 == object_null2);
        assertTrue(object_null1.equals(object_null2));
        assertTrue(!object_null1.equals(object_notnull2));
        assertTrue(object_notnull1.equals(object_notnull1));
        assertTrue(!object_notnull1.equals(object_notnull2));

        // RString
        omero.RString string_null1 = rstring(null);
        omero.RString string_null2 = rstring(null);
        omero.RString string_notnull1 = rstring("str1");
        omero.RString string_notnull1b = rstring("str1");
        omero.RString string_notnull2 = rstring("str2");
        assertTrue(string_null1 == string_null2);
        assertTrue(string_null1.equals(string_null2));
        assertTrue(!string_null1.equals(string_notnull2));
        assertTrue(string_notnull1.equals(string_notnull1));
        assertTrue(!string_notnull1.equals(string_notnull2));
        assertTrue(string_notnull1.equals(string_notnull1b));

    }

    List<RType> ids = new ArrayList<RType>();
    {
        ids.add(rlong(1));
    }

    @Test
    public void testArrayCreationEqualsHash() {

        omero.RArray array_notnull1 = rarray(ids);
        omero.RArray array_notnull2 = rarray(ids);
        // Equals based on content
        assertTrue(!array_notnull1.equals(array_notnull2));
        // But content is copied!
        assertTrue(array_notnull1.getValue() != array_notnull2.getValue());

        omero.RArray array_null1 = rarray();
        omero.RArray array_null2 = rarray((RType[]) null);
        omero.RArray array_null3 = rarray((Collection<RType>) null);

        // All different since the contents are mutable.
        assertTrue(!array_null1.equals(array_notnull1));
        assertTrue(!array_null1.equals(array_null2));
        assertTrue(!array_null1.equals(array_null3));

    }

    @Test
    public void testListCreationEqualsHash() {

        omero.RList list_notnull1 = rlist(ids);
        omero.RList list_notnull2 = rlist(ids);
        // Equals based on content
        assertTrue(!list_notnull1.equals(list_notnull2));
        // But content is copied!
        assertTrue(list_notnull1.getValue() != list_notnull2.getValue());

        omero.RList list_null1 = rlist();
        omero.RList list_null2 = rlist((RType[]) null);
        omero.RList list_null3 = rlist((Collection<RType>) null);

        // All different since the contents are mutable.
        assertTrue(!list_null1.equals(list_notnull1));
        assertTrue(!list_null1.equals(list_null2));
        assertTrue(!list_null1.equals(list_null3));

    }

    @Test
    public void testSetCreationEqualsHash() {

        omero.RSet set_notnull1 = rset(ids);
        omero.RSet set_notnull2 = rset(ids);
        // Equals based on content
        assertTrue(!set_notnull1.equals(set_notnull2));
        // But content is copied!
        assertTrue(set_notnull1.getValue() != set_notnull2.getValue());

        omero.RSet set_null1 = rset();
        omero.RSet set_null2 = rset((RType[]) null);
        omero.RSet set_null3 = rset((Collection<RType>) null);

        // All different since the contents are mutable.
        assertTrue(!set_null1.equals(set_notnull1));
        assertTrue(!set_null1.equals(set_null2));
        assertTrue(!set_null1.equals(set_null3));

    }

    @Test
    public void testMapCreationEqualsHash() {

        RLong id = rlong(1L);
        omero.RMap map_notnull1 = rmap("ids", id);
        omero.RMap map_notnull2 = rmap("ids", id);
        // Equals based on content
        assertTrue(map_notnull1.equals(map_notnull2)); // TODO different with
                                                        // maps
        // But content is copied!
        assertTrue(map_notnull1.getValue() != map_notnull2.getValue());

        omero.RMap map_null1 = rmap();
        omero.RMap map_null2 = rmap((Map<String, RType>) null);

        // All different since the contents are mutable.
        assertTrue(!map_null1.equals(map_notnull1));
        assertTrue(map_null1.equals(map_null2)); // TODO Different with maps

    }

    @Test
    public void testWrapRListString() {
        List<String> input = Arrays.asList("A", "B");
        RList rlist = (RList) omero.rtypes.wrap(input);
        assertEquals(2, rlist.getValue().size());
        assertEquals("A", ((RString) rlist.getValue().get(0)).getValue());
        assertEquals("B", ((RString) rlist.getValue().get(1)).getValue());
    }
 
    @SuppressWarnings("unchecked")
    @Test
    public void testWrapUnwrapMap() {
        Map<String, Object> input = new HashMap<String, Object>();
        input.put("int", new Integer(0));
        input.put("float", new Float(0.0f));
        RMap output = (RMap) omero.rtypes.wrap(input);
        Map<String, Object> test = (Map<String, Object>) omero.rtypes.unwrap(output);
        assertEquals(new Integer(0), test.get("int"));
        assertEquals(new Float(0.0f), test.get("float"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testMoreComplexeWrapping() {
        // Create a bunch of wacky structures
        Map<String, List<Map<String, Set<Object>>>> wow
            = new HashMap<String, List<Map<String, Set<Object>>>>();
        List<Map<String, Set<Object>>> list = new ArrayList<Map<String, Set<Object>>>();
        Map<String, Set<Object>> map1 = new HashMap<String, Set<Object>>();
        Set<Object> set = new HashSet<Object>();

        // Now put everything in everything
        set.add(new Integer(3));
        set.add(new Double(0.0d));
        set.add(new Timestamp(0l));
        set.add(new Long(0L));
        set.add(new String("string"));
        /*
        Due to the key semantics of hash sets, placing these in the set
        leads to stackoverflow semantics. A IdentityHashSet is needed.
        set.add(map1);
        set.add(list);
        set.add(wow);
        */
        set.add(new Object[]{ list, map1, set});
        
        map1.put("set", set);
        map1.put("set2", set);

        list.add(map1);
        wow.put("list", list);

        RMap mapOut = (RMap) omero.rtypes.wrap(wow);
        RList listOut = (RList) mapOut.getValue().get("list");
        RMap map1Out = (RMap) listOut.getValue().get(0);
        RSet setOut = (RSet) map1Out.getValue().get("set");
        set = (Set<Object>) omero.rtypes.unwrap(setOut);

        wow = (Map<String, List<Map<String, Set<Object>>>>) omero.rtypes.unwrap(mapOut);
        list = wow.get("list");
        map1 = list.get(0);
        set = map1.get("set");
        assertTrue(set.contains(new Integer(3)));
    }
}
