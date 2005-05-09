/*
 * org.openmicroscopy.shoola.omeis.TMP
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.omeis;


import org.openmicroscopy.shoola.omeis.services.PixelsReader;
import org.openmicroscopy.shoola.omeis.transport.HttpChannel;
import org.openmicroscopy.shoola.util.concur.tasks.AsyncProcessor;
import org.openmicroscopy.shoola.util.concur.tasks.CmdProcessor;
import org.openmicroscopy.shoola.util.concur.tasks.Future;
import org.openmicroscopy.shoola.util.mem.ByteArray;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">
 *                  a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class TMP
{

    static String sessionKey = "c2d2ac1d62f9879b73c220b16bfecc97";
    static String url = "http://valewalker.openmicroscopy.org.uk/cgi-bin/omeis";
    static long pixelsID = 44;
    static int  planeSize = 256*256*2;
    
    static CmdProcessor cmdProcessor = new AsyncProcessor();
    
    
    static PixelsReader getService()
        throws Exception
    {
        ServiceDescriptor srvDesc = new ServiceDescriptor(sessionKey, url, 
                                    HttpChannel.CONNECTION_PER_REQUEST, -1, -1);   
        return OMEISRegistry.getPixelsReader(srvDesc);
    }
    
    
    PixelsReader omeis;
    TMP_Fetcher[] fetchers;
    Future[] futures;
    TMP_Stats stats; 
    
    TMP(int fetchNbr)
        throws Exception
    {
        omeis = getService();
        fetchers = new TMP_Fetcher[fetchNbr];
        for (int i = 0; i < fetchers.length; ++i)
            fetchers[i] = new TMP_Fetcher(omeis, pixelsID, 0, 0, 0, planeSize);
        futures = new Future[fetchNbr];
        stats = new TMP_Stats();
    }
    
    void startFetches()
    {
        stats.startTotal();
        for (int i = 0; i < futures.length; ++i)
            futures[i] = cmdProcessor.exec(fetchers[i]);
    }
    
    void waitForCompletion()
        throws Exception
    {
        for (int i = 0; i < futures.length; ++i)
            futures[i].getResult();
        stats.endTotal();
    }
    
    void printStats()
    {
        System.out.println("-------------------------------------------------");
        System.out.println("FETCHES: "+fetchers.length+" in "+stats);
        for (int i = 0; i < fetchers.length; ++i)
            System.out.println("        "+i+" -> "+fetchers[i].stats);
        System.out.println("-------------------------------------------------");
    }
    
    
    public static void main(String[] args)
        throws Exception
    {
        TMP test;
        for (int i = 1; i < 11; ++i) {
            test = new TMP(i);
            test.startFetches();
            test.waitForCompletion();
            test.printStats();
        }
    }
    
}
