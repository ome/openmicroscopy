/*
 * org.openmicroscopy.shoola.env.data.views.TestBatchCall
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2010 Glencoe Software, Inc. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.env.data.views;


//Java imports

//Third-party libraries
import junit.framework.TestCase;


//Application-internal dependencies

/**
 * Routine unit test for {@link ScriptBatchCall}.
 *
 * @version Beta4.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since Beta4.2.0
 */
public class TestProcessBatchCall
    extends TestCase
{

    private ProcessBatchCall   target;  //Object under test.


    protected void setUp()
    {
        // nothing.
    }

    public void testNullReturnedOnInitialize() throws Exception
    {
        target = new ProcessBatchCall("test") {
            @Override
            protected ProcessCallback initialize() throws Exception {
                return null;
            }
        };
        target.doStep();
    }

    /*
    public void testThrowsOnInitialize() throws Exception
    {
        target = new ProcessBatchCall("test") {
            @Override
            protected ProcessCallback initialize() throws Exception {
                //throw new RuntimeException("boom");
            }
        };
        target.doStep();
    }
    */

}
