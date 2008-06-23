package ome.formats.test;

import java.io.RandomAccessFile;
import loci.formats.*;
import loci.formats.meta.*;

public class Test {
  public static void main(String[] args) throws Exception {
      
    System.out.println(args[0]);
    System.out.println(args[1]);
    System.out.println(args[2]);
    
    MinMaxCalculator r =
      new MinMaxCalculator(new ChannelSeparator(new ChannelFiller(new ImageReader())));
    r.setId(args[0]);
    
    r.setSeries(0);
    RandomAccessFile out = new RandomAccessFile(args[2], "rw");
    byte[] buf = new byte[r.getSizeX() * r.getSizeY() * r.getEffectiveSizeC() *
      FormatTools.getBytesPerPixel(r.getPixelType())];
    for (int i=0; i<r.getImageCount(); i++) {
      r.openBytes(i, buf);
      out.write(buf);
    }
    out.close();
    for (int c=0; c<r.getSizeC(); c++) {
      System.out.println("channel " + c + " min = " + r.getChannelGlobalMinimum(c));
      System.out.println("channel " + c + " max = " + r.getChannelGlobalMaximum(c));
    }
    
    
    r.setSeries(1);
    out = new RandomAccessFile(args[2], "rw");
    buf = new byte[r.getSizeX() * r.getSizeY() * r.getEffectiveSizeC() *
      FormatTools.getBytesPerPixel(r.getPixelType())];
    for (int i=0; i<r.getImageCount(); i++) {
      r.openBytes(i, buf);
      out.write(buf);
    }
    out.close();
    for (int c=0; c<r.getSizeC(); c++) {
      System.out.println("channel " + c + " min = " + r.getChannelGlobalMinimum(c));
      System.out.println("channel " + c + " max = " + r.getChannelGlobalMaximum(c));
    }
  }
}
