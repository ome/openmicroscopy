/*
 *   Copyright (C) <year> University of Dundee & Open Microscopy Environment.
 *   All rights reserved.
 *
 *   Use is subject to license terms supplied in LICENSE.txt
 */

 //the above license blurb is required


package org.openmicroscopy.package.ClassName;

//follow the pattern below for imports

import java.lang.reflect.*;            //avoid *, specify actual classes if possible
import javax.swing.Action;             //follow alphabetical order
import javax.swing.JFrame;

import org.open.source.ClassX;
import org.open.source.lib.ClassY;

import org.openmicroscopy.pkg.ClassX;

//REMEMBER:
// * line length is 80 chars (may be more occasionally for long string litterals)
// * no hard-tabs; use soft-tabs of 4 spaces

/**
 * Put class description here.
 * Format descriptions using (basic) HTML as needed.
 * Use {@link target name} to reference fields and methods.
 * Use the "code" tag to enclose any symbol that is contained in the source
 * code and that is not to be referenced -- for example, a keyword or a
 * variable name.
 *
 * Excluding the author clause, which is optional, all other clauses
 * below are required. Just cut and paste.
 *
 * @author Your Name, user at example.com
 * @since OMERO-Beta4.4
 */
public abstract class Template
    extends BaseClass
    implements Interface
{

    /** One-line description may have this style. */
    public static final int A_CONSTANT = 1;

    /**
     * If you need more than one line to describe a member field, use
     * this common style.
     */
    private int aMemberField;
    private String anotherField;

    /**
     * Put method description here.
     * Method signature is described following the order below:
     *
     * @param param1 A tab before description.
     * @param param2 A tab before description.
     * @return Object A tab before description.
     * @throws MyException A tab before description
     */
    public Object aMethod(String param1, int param2) //a space after commas
        throws MyException //throws clause on next line
    {
        int k = 0;      //a space between each symbol pair, no space before semicolon
                        //optional line after vars declaration
        while (k < 10) {    //a space between keywords and parentheses
            if (k == 3 || k == -1) { //a space between condition and operator
                byte x = (byte) k++;    //a space between cast and symbols
                break;
            }
            else {
                k = methodX(par1, par2, par3); //a space after commas
            }
        }
        try {
            //some more code...
        }
        catch (Exception e) {
            //exception handling code...
        }

        switch (k) {
            case 0:
                methodY();
                methodZ();
                break;
            case 1:
                methodZ();    // never on the same line as the 'case' statement
                break;
            default:
                // do something else
        }
    }

}
