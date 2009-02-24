/*
 * Copyright (c) 2006 Sun Microsystems, Inc. All Rights Reserved.
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


import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import java.util.*;

import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import com.sun.opengl.util.texture.*;

/** Demonstrates and tests updates of sub-rectangles of a Texture
    generated from a BufferedImage. Draws a cursor in texel space
    around the cursor using Java 2D's XOR mode and pushes those
    updates to an underlying OpenGL texture using
    Texture.updateSubImage(). */

public class TestSubImage {
  private boolean  haveForcedImageType;
  private int      forcedImageType;
  private List     imageTypeMenuItems = new ArrayList();
  private GLU      glu = new GLU();
  private GLCanvas mainCanvas;
  private Rectangle curRect;
  private Rectangle lastRect;
  private TextureData textureData;
  private Texture  texture;
  private BufferedImage baseImage;
  private BufferedImage convertedImage;
  private int CURSOR_SIZE = 10;

  class Listener implements GLEventListener {
    public void init(GLAutoDrawable drawable) {}

    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
      GL gl = drawable.getGL();
      gl.glMatrixMode(GL.GL_PROJECTION);
      gl.glLoadIdentity();
      glu.gluOrtho2D(0, 1, 0, 1);
      gl.glMatrixMode(GL.GL_MODELVIEW);
      gl.glLoadIdentity();
    }

    public void display(GLAutoDrawable drawable) {
      GL gl = drawable.getGL();
      gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

      if (convertedImage == null) {
        // Get rid of any previously allocated texture
        if (texture != null) {
          texture.dispose();
          texture = null;
        }

        // Convert the base image
        // Note that for simplicity in handling of the XOR cursor, we
        // always make a copy of the base image
        int imageType = BufferedImage.TYPE_INT_RGB;
        if (haveForcedImageType) {
          imageType = forcedImageType;
        }
        convertedImage = new BufferedImage(baseImage.getWidth(),
                                           baseImage.getHeight(),
                                           imageType);
        Graphics2D g = convertedImage.createGraphics();
        g.setComposite(AlphaComposite.Src);
        g.drawImage(baseImage, 0, 0, null);
        g.dispose();

        // Create a TextureData and Texture from it
        textureData = TextureIO.newTextureData(convertedImage, false);
        texture = TextureIO.newTexture(textureData);
      }

      Graphics2D g = convertedImage.createGraphics();
      g.setColor(Color.RED);
      g.setXORMode(Color.WHITE);
      if (lastRect != null) {
        // Paint with XOR in this area
        g.fillRect(lastRect.x, lastRect.y, lastRect.width, lastRect.height);
      }
      if (curRect != null) {
        // Paint with XOR in this area
        g.fillRect(curRect.x, curRect.y, curRect.width, curRect.height);
      }
      // Compute the union of these rectangles, if any
      Rectangle union = null;
      if (lastRect != null) {
        union = new Rectangle(lastRect);
        if (curRect != null) {
          union.add(curRect);
        }
      } else if (curRect != null) {
        union = curRect;
      }

      // Move these down
      lastRect = curRect;
      curRect = null;

      // Update the affected area of the texture (if there is one)
      // Note: this one API call is basically what this demo is illustrating
      if (union != null) {
        int yOrigin = union.y;

        // Note: if the Y origin of the texture data didn't match Java
        // 2D's Y origin, we would need to correct the Y coordinate
        // passed down here. This code path is not taken in the
        // current Texture implementation.
        if (!texture.getMustFlipVertically()) {
          yOrigin = texture.getHeight() - yOrigin;
        }

        texture.updateSubImage(textureData, 0,
                               union.x, yOrigin,
                               union.x, yOrigin,
                               union.width, union.height);
      }

      // Now draw one quad with the texture
      texture.enable();
      texture.bind();
      gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_REPLACE);
      TextureCoords coords = texture.getImageTexCoords();
      gl.glBegin(GL.GL_QUADS);
      gl.glTexCoord2f(coords.left(), coords.bottom());
      gl.glVertex3f(0, 0, 0);
      gl.glTexCoord2f(coords.right(), coords.bottom());
      gl.glVertex3f(1, 0, 0);
      gl.glTexCoord2f(coords.right(), coords.top());
      gl.glVertex3f(1, 1, 0);
      gl.glTexCoord2f(coords.left(), coords.top());
      gl.glVertex3f(0, 1, 0);
      gl.glEnd();
      texture.disable();
    }

    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {}
  }

  private JMenuItem newItem(final int imageType, String imageTypeName) {
    final JCheckBoxMenuItem item = new JCheckBoxMenuItem(imageTypeName);
    item.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (item.isSelected()) {
            // Disable all other checkbox menu items
            for (Iterator iter = imageTypeMenuItems.iterator(); iter.hasNext(); ) {
              JCheckBoxMenuItem cb = (JCheckBoxMenuItem) iter.next();
              if (cb != item)
                cb.setSelected(false);
            }

            haveForcedImageType = true;
            forcedImageType = imageType;
          } else {
            // No longer have a forced image type
            haveForcedImageType = false;
          }

          // Get rid of the previously created converted image, if any
          if (convertedImage != null) {
            convertedImage.flush();
            convertedImage = null;
          }

          lastRect = null;
          mainCanvas.repaint();
        }
      });
    imageTypeMenuItems.add(item);
    return item;
  }

  private void run() {
    JPopupMenu.setDefaultLightWeightPopupEnabled(false);

    // Create top-level window and menus
    JFrame frame = new JFrame("Texture Sub Image Test");
    JMenuBar menuBar = new JMenuBar();
    JMenu menu = new JMenu("File");
    menu.setMnemonic(KeyEvent.VK_F);
    JMenuItem item = new JMenuItem("Quit");
    item.setMnemonic(KeyEvent.VK_Q);
    item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK));
    item.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          System.exit(0);
        }
      });
    menu.add(item);

    menuBar.add(menu);

    menu = new JMenu("Tools");
    menu.setMnemonic(KeyEvent.VK_T);
    JMenu submenu = new JMenu("Force BufferedImage Type");
    submenu.setMnemonic(KeyEvent.VK_F);
    submenu.add(newItem(BufferedImage.TYPE_3BYTE_BGR, "TYPE_3BYTE_BGR"));
    submenu.add(newItem(BufferedImage.TYPE_4BYTE_ABGR, "TYPE_4BYTE_ABGR"));
    submenu.add(newItem(BufferedImage.TYPE_4BYTE_ABGR_PRE, "TYPE_4BYTE_ABGR_PRE"));
    submenu.add(newItem(BufferedImage.TYPE_BYTE_BINARY, "TYPE_BYTE_BINARY"));
    submenu.add(newItem(BufferedImage.TYPE_BYTE_GRAY, "TYPE_BYTE_GRAY"));
    submenu.add(newItem(BufferedImage.TYPE_BYTE_INDEXED, "TYPE_BYTE_INDEXED"));
    submenu.add(newItem(BufferedImage.TYPE_INT_ARGB, "TYPE_INT_ARGB"));
    submenu.add(newItem(BufferedImage.TYPE_INT_ARGB_PRE, "TYPE_INT_ARGB_PRE"));
    submenu.add(newItem(BufferedImage.TYPE_INT_BGR, "TYPE_INT_BGR"));
    submenu.add(newItem(BufferedImage.TYPE_INT_RGB, "TYPE_INT_RGB"));
    submenu.add(newItem(BufferedImage.TYPE_USHORT_555_RGB, "TYPE_USHORT_555_RGB"));
    submenu.add(newItem(BufferedImage.TYPE_USHORT_565_RGB, "TYPE_USHORT_565_RGB"));
    submenu.add(newItem(BufferedImage.TYPE_USHORT_GRAY, "TYPE_USHORT_GRAY"));
    menu.add(submenu);
    
    menuBar.add(menu);

    frame.setJMenuBar(menuBar);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    // Set up the base image we'll use to draw with
    baseImage = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
    Graphics2D g = baseImage.createGraphics();
    g.setPaint(new GradientPaint(0, 0, Color.CYAN,
                                 baseImage.getWidth(), baseImage.getHeight(), Color.BLUE));
    g.fillRect(0, 0, baseImage.getWidth(), baseImage.getHeight());
    g.dispose();

    // Now set up the main GLCanvas
    mainCanvas = new GLCanvas();
    mainCanvas.addGLEventListener(new Listener());
    mainCanvas.addMouseListener(new MouseAdapter() {
        public void mouseExited(MouseEvent e) {
          curRect = null;
          mainCanvas.repaint();
        }
      });
    mainCanvas.addMouseMotionListener(new MouseMotionAdapter() {
        public void mouseMoved(MouseEvent e) {
          // Scale x and y into coordinate system of texture
          int x = (int) (e.getX() * (float) baseImage.getWidth()  / (float) e.getComponent().getWidth());
          int y = (int) (e.getY() * (float) baseImage.getHeight() / (float) e.getComponent().getHeight());
          // Paint cursor on texture around this point
          int minx = Math.max(0, x - CURSOR_SIZE);
          int maxx = Math.min(baseImage.getWidth(), x + CURSOR_SIZE);
          int miny = Math.max(0, y - CURSOR_SIZE);
          int maxy = Math.min(baseImage.getHeight(), y + CURSOR_SIZE);
          curRect = new Rectangle(minx, miny, maxx - minx, maxy - miny);
          mainCanvas.repaint();
        }
      });

    frame.getContentPane().add(mainCanvas);
    frame.setSize(512, 512);
    frame.setVisible(true);
  }

  public static void main(String[] args) {
    new TestSubImage().run();
  }
}
