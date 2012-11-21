/*
 * ome.formats.test.util.IniWritingInterceptor
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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

package ome.formats.test.util;

import java.io.File;
import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

public class IniWritingInterceptor implements MethodInterceptor
{
    /** The initiation file we're writing to. */
    private TestEngineIniFile iniFile;

    /** The source file we're working on. */
    private File sourceFile;

    public void setIniFile(TestEngineIniFile iniFile)
    {
        this.iniFile = iniFile;
    }

    public void setSourceFile(File sourceFile)
    {
        this.sourceFile = sourceFile;
    }

    public Object invoke(MethodInvocation invocation) throws Throwable
    {
        if (sourceFile != null)
        {
            Method m = invocation.getMethod();
            Class<?>[] parameterTypes = m.getParameterTypes();
            Object[] arguments = invocation.getArguments();
            String argumentString = "";

            if (!m.getName().equals("setReader")
                    && !m.getName().equals("setPlane")
                    && !m.getName().equals("addImageToDataset")
                    && !m.getName().equals("addBooleanAnnotationToPixels")
                    && !m.getName().equals("populateSHA1"))
            {
                for (int i = 0; i < arguments.length; i++)
                {
                    argumentString += String.format("%s(%s), ",
                            parameterTypes[i].getName(), arguments[i]);
                }
                iniFile.testValue(sourceFile.getName(), m.getName(), argumentString);
            }
        }
        Object retVal = invocation.proceed();
        return retVal;
    }
}
