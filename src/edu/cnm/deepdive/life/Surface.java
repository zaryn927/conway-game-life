/**
 * 
 */
package edu.cnm.deepdive.life;

import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

/**
 * @author Sky Link
 *
 */
public class Surface extends JPanel {

  /**
   * 
   */
  private static final long serialVersionUID = 2495214640084762801L;
  
  
  private static final int INSET = 1;
  private static final Color CELL_COLOR = Color.YELLOW;
  private static final Color OLD_CELL_COLOR = Color.CYAN;
  
  public final int width;
  public final int height;
  public final float scale;
  
  private byte[][] field;
  
  
  public Surface(int width, int height, float scale) {
    super(true);
    this.width = width;
    this.height = height;
    this.scale = scale;
    setBorder(LineBorder.createGrayLineBorder());
  }


  /**
   *
   */
  @Override
  public Dimension getPreferredSize() {
    // TODO Auto-generated method stub
    return new Dimension(1 + 2 * INSET + Math.round(width * scale),
        1 + 2 * INSET + Math.round(height * scale));
  }


  /**
   * 
   */
  @Override
  protected void paintComponent(Graphics g) {
    setBackground(Color.BLACK);
    super.paintComponent(g);
    //g.setColor(CELL_COLOR);
    synchronized (this) {
      for (int i = 0; i < height; i++) {
        int top = INSET + Math.round(i * scale);
        int height = INSET + Math.round((i + 1) * scale) - top;
        for (int j = 0; j < width; j++) {
          int left = INSET + Math.round(j * scale);
          int width = INSET + Math.round((j + 1) * scale) - left;
          byte cellGeneration = field[i][j];
          if (cellGeneration > 1) {
            g.setColor(OLD_CELL_COLOR);
            g.fillOval(left, top, width, height);
          } else if (cellGeneration == 1) {
            g.setColor(CELL_COLOR);
            g.fillOval(left, top, width, height);
          }
        }
      }
    }
  }
  
  public synchronized void setField(byte[][] field) {
    this.field = field;
    repaint();
  }
  
  
  
}
