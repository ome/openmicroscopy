/*
 * ome.io.nio.itests.Helper
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.io.nio.itests;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * @author callan
 *
 */
public class Helper
{
    private static MessageDigest newSha1MessageDigest()
    {
        MessageDigest md;
        
        try
        {
            md = MessageDigest.getInstance("SHA-1");
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException(
                    "Required SHA-1 message digest algorithm unavailable.");
        }
        
        md.reset();
        return md;
    }
    
    public static byte[] calculateMessageDigest(ByteBuffer buffer)
    {
        MessageDigest md = newSha1MessageDigest();
        md.update(buffer);
        return md.digest();
    }
    
    public static byte[] calculateMessageDigest(byte[] buffer)
    {
        MessageDigest md = newSha1MessageDigest();
        md.update(buffer);
        return md.digest();
    }
    
    /**
     * Convenience method to convert a byte to a hex string.
     *
     * @param data the byte to convert
     * @return String the converted byte
     */
     public static String byteToHex(byte data)
     {
         StringBuffer buf = new StringBuffer();
         buf.append(toHexChar((data >>> 4) & 0x0F));
         buf.append(toHexChar(data & 0x0F));
         return buf.toString();
     }
     
     /**
     * Convenience method to convert an int to a hex char.
     *
     * @param i the int to convert
     * @return char the converted char
     */
     public static char toHexChar(int i)
     {
         if ((0 <= i) && (i <= 9))
             return (char) ('0' + i);
         else
             return (char) ('a' + (i - 10));
     }
     
     /**
     * Convenience method to convert a byte array to a hex string.
     *
     * @param data the byte[] to convert
     * @return String the converted byte[]
     */
     public static String bytesToHex(byte[] data) {
         StringBuffer buf = new StringBuffer();
         for (int i = 0; i < data.length; i++)
         {
             buf.append(byteToHex(data[i]));
         }
         return (buf.toString());
     }
}
