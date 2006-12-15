/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.dynamic;

public class BinaryServiceImpl implements BinaryService {

    public byte[] getClass(String name) {
        CodeGeneration a = new CodeGeneration();
        return a.getClassFromDB(name);
    }

}
