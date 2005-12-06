package ome.io.nio;


public class Helper
{
    /* FIXME: This is a *hack*, it needs to be replaced with real
     * configuration option. -Chris
     */
    private static final String root = "/OME/OMEIS/";

    public static String getPixelsPath(Integer id)
    {
        return getPath("Pixels/", id);
    }
    
    public static String getFilesPath(Integer id)
    {
        return getPath("Files/", id);
    }
    
    private static String getPath(String prefix, Integer id)
    {
        String suffix = "";
        Integer remaining = id;
        Integer dirno = 0;
        Integer i = 0;
        
        if (id == null)
            throw new NullPointerException("Expecting a not-null id.");

        while (remaining > 999)
        {
            if (i > 0)
            {
                dirno = remaining % 1000;
                suffix = dirno + "/" + suffix;
            }

            remaining /= 1000;
            i++;
        }
        
        return root + prefix + suffix + id;
    }
}
