package edu.cnm.deepdive.life;

/**
 * {@code Life} implements the model for a CA. By default, the classic rules 
 * are used, but an alternate rule may be supplied. A toroidal world is assumed.
 * 
 * @author Nicholas Bennett
 */
public class Life {

  /** Width of the world, in cells. */
  public final int width;
  /** Height of the world, in cells. */
  public final int height;

  private byte[][] field = null;  
  private int generation = 0;

  private Classic classic = new Classic();
  
  /**
   * Initializes the CA without creating an empty field. {@link 
   * #populate(double)} must be invoked before performing any operation on
   * the CA.
   * 
   * @param width             Width of the world, in cells.
   * @param height            Height of the world, in cells.
   */
  public Life(int width, int height) {
    this(width, height, false);
  }

  /**
   * Initializes the CA, optionally with an empty field. (Mostly provided for 
   * future flexibility, in the event that a specialized implementation uses 
   * mouse clicks &ndash; for example &ndash; to seed the CA.) 
   * 
   * @param width             Width of the world, in cells.
   * @param height            Height of the world, in cells.
   * @param initializeEmpty   Flag specifying whether an empty field of cells
   *                          should be created.
   */
  public Life(int width, int height, boolean initializeEmpty) {
    this.width = width;
    this.height = height;
    if (initializeEmpty) {
      field = new byte[height][width];
    }
  }

  /**
   * Populates the field randomly, using {@code density} as a threshold
   * probability for creating a living cell in any given location.
   * 
   * @param density           Threshold probability (the approximate density
   *                          of resulting cells).
   */
  public void populate(double density) {
    byte[][] work = new byte[height][width];
    for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
        if (Math.random() < density) {
          work[i][j] = 1;
        }
      }
    }
    setField(work);
    setGeneration(0);
  }
  
  /**
   * 
   */
  public void step() {
    step(classic);
  }

  /**
   * 
   * @param rule
   */
  public void step(Rule rule) {
    byte[][] work = new byte[height][width];
    for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
        int neighbors = countNeighbors(i, j);
        byte cellGeneration = field[i][j];
        if (rule.live((cellGeneration != 0), neighbors)) {
          work[i][j] = (cellGeneration == Byte.MAX_VALUE) ? Byte.MAX_VALUE : ++cellGeneration;
        }
      }
    }
    setField(work);
    incGeneration();
  }

  /**
   * 
   * @param row
   * @param column
   * @return
   */
  protected int countNeighbors(int row, int column) {
    int count = 0;
    for (int i = row - 1; i<= row + 1; i++) {
      for (int j = column -1; j <= column + 1; j++) {
        int wrappedRow = (i + height) % height;
        int wrappedColumn = (j + width) % width;
        if (field[wrappedRow][wrappedColumn] != 0) {
          count++;
        }
      }
    } 
    return count - ((field[row][column] != 0) ? 1 : 0);
  }
  
  /**
   * @return the field
   */
  public synchronized byte[][] getField() {
    byte[][] safeCopy = new byte[height][];
    for (int i = 0; i < height; i++) {
      safeCopy[i] = field[i].clone();
    }
    return safeCopy;
  }

  /**
   * @param field the field to set
   */
  protected synchronized void setField(byte[][] field) {
    this.field = field;
  }

  /**
   * @return the generation
   */
  public synchronized int getGeneration() {
    return generation;
  }

  /**
   * @param generation the generation to set
   */
  protected synchronized void setGeneration(int generation) {
    this.generation = generation;
  }
  
  /**
   * 
   */
  protected synchronized void incGeneration() {
    generation++;
  }

  /**
   * 
   */
  public interface Rule {
    
    boolean live(boolean previouslyAlive, int liveNeighbors);
    
  }
  
  /**
   * 
   */
  public static class Classic implements Rule {

    /**
     * 
     */
    @Override
    public boolean live(boolean previouslyAlive, int liveNeighbors) {
      return (liveNeighbors == 3 || (liveNeighbors == 2 && previouslyAlive));
    }
    
  }
  
}
