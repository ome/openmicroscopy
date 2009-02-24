/*
 * Copyright (c) 2005 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 
 * - Redistribution of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * 
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES,
 * INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN
 * MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR
 * ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR
 * DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE
 * DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY,
 * ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF
 * SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * 
 * You acknowledge that this software is not designed or intended for use
 * in the design, construction, operation or maintenance of any nuclear
 * facility.
 * 
 * Sun gratefully acknowledges that this software was originally authored
 * and developed by Kenneth Bradley Russell and Christopher John Kline.
 */


import java.io.*;

import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import com.sun.opengl.util.*;
import com.sun.opengl.util.texture.*;

/** Demonstrates how the TextureIO subsystem may be used to convert
    textures between multiple file formats, including texture
    compression where available. */

public class TextureConvert {
  public static void main(String[] args) throws IOException {
    if (args.length != 2) {
      System.out.println("Usage: java demos.texture.TextureConvert [input file] [output file]");
      System.out.println("Converts texture from input file format to output file format.");
      System.out.println("If output file format is DDS, compresses texture with DXT3 compression");
      System.out.println("if available.");
      System.exit(1);
    }

    String inputFile  = args[0];
    String outputFile = args[1];

    // Make a pbuffer to get an offscreen context
    if (!GLDrawableFactory.getFactory().canCreateGLPbuffer()) {
      System.out.println("Pbuffer support not available (required to run this demo)");
      System.exit(1);
    }
    GLCapabilities caps = new GLCapabilities();
    caps.setDoubleBuffered(false);
    GLPbuffer pbuffer = GLDrawableFactory.getFactory().createGLPbuffer(caps, null, 2, 2, null);
    pbuffer.getContext().makeCurrent();
    GL gl = pbuffer.getGL();

    boolean attemptCompression = false;
    if (TextureIO.DDS.equals(FileUtil.getFileSuffix(outputFile))) {
      if (gl.isExtensionAvailable("GL_EXT_texture_compression_s3tc") ||
          gl.isExtensionAvailable("GL_NV_texture_compression_vtc")) {
        attemptCompression = true;
      }
    }

    TextureData inputData = TextureIO.newTextureData(new File(inputFile), false, null);
    if (attemptCompression && !inputData.isDataCompressed()) {
      inputData.setInternalFormat(GL.GL_COMPRESSED_RGBA_S3TC_DXT3_EXT);
    }
    Texture tex = TextureIO.newTexture(inputData);

    // Now read it back and save to the output file
    TextureIO.write(tex, new File(outputFile));
  }
}
