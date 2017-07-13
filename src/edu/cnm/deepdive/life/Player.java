package edu.cnm.deepdive.life;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Entry point and controller (in the MVC sense) for a basic implementation of
 * the well-known <em>cellular automaton</em> (CA), Conway's "Game of Life".
 *  
 * @author Nicholas Bennett
 */
public class Player implements ActionListener, ChangeListener {

  private static final String WINDOW_TITLE = "Deep Dive: Conway's Game of Life";
  private static final String THRESHOLD_LABEL = "Density";
  private static final String SETUP_BUTTON = "Reset";
  private static final String STEP_BUTTON = "Generation: %d";
  private static final String RUN_BUTTON_UNSELECTED = "Run!";
  private static final String RUN_BUTTON_SELECTED = "Stop!";
  private static final int WIDTH = 150;
  private static final int HEIGHT = 150;
  private static final float SCALE = 6f;
  private static final int PADDING = 10;
  private static final double DEFAULT_DENSITY = 0.15;
  private static final int MAX_SPEED = 20;
  private static final int MIN_SPEED = 2;
  private static final int GENERATION_DELAY = 75;
  
  private Surface surface;
  private Life life;

  private JSlider thresholdSlider;
  private JSlider speedSlider;
  private JButton setup;
  private JButton step;
  private JToggleButton run;

  private boolean uiSetup = false;
  private boolean populating = false;
  private boolean stepping = false;
  private boolean running = false;
  private double density = DEFAULT_DENSITY;
  private long sleepInterval = GENERATION_DELAY;
  
  /**
   * Creates and starts an instance of the {@code Player} class, to control
   * execution of <em>Life</em>.
   * 
   * @param args  Command line arguments (ignored)
   */
  public static void main(String[] args) {
    Player player = new Player();
    player.init();
    player.run();
  }

  /**
   * Handles button-clicks to single-step, start, stop, and reset the Life CA.
   */
  @Override
  public void actionPerformed(ActionEvent evt) {
    Object source = evt.getSource();
    if (source == setup) {
      setup.setEnabled(false);
      step.setEnabled(false);
      run.setEnabled(false);
      synchronized (this) {
        populating = true;
        notify();
      }
      run.setEnabled(true);
      step.setEnabled(true);
      setup.setEnabled(true);
    } else if (source == step) {
      setup.setEnabled(false);
      step.setEnabled(false);
      run.setEnabled(false);
      synchronized (this) {
        stepping = true;
        notify();
      }
      run.setEnabled(true);
      step.setEnabled(true);
      setup.setEnabled(true);
    } else if (source == run) {
      if (run.isSelected()) {
        setup.setEnabled(false);
        step.setEnabled(false);
        run.setText(RUN_BUTTON_SELECTED);
        synchronized (this) {
          running = true;
          notify();
        }
      } else {
        synchronized (this) {
          running = false;
          notify();
        }
        run.setText(RUN_BUTTON_UNSELECTED);
        step.setEnabled(true);
        setup.setEnabled(true);
      }      
    }
  }
  
  private void init() {
    life = new Life(WIDTH, HEIGHT);
    life.populate(density);
    SwingUtilities.invokeLater(() -> buildGui());
    synchronized (this) {
      while (!uiSetup) {
        try {
          wait();
        } catch (InterruptedException ex) {
          // Do nothing.
        }
      }
    }
  }
  
  private void buildGui() {
    JFrame frame = new JFrame(WINDOW_TITLE);
    frame.setLayout(new BorderLayout());
    frame.setResizable(false);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
    surface = new Surface(WIDTH, HEIGHT, SCALE);
    surface.setField(life.getField());
    frame.add(surface, BorderLayout.NORTH);

    JPanel controls = new JPanel(new GridLayout(1, 3));
    controls.setBorder(LineBorder.createGrayLineBorder());

     
    thresholdSlider = new JSlider(0, 50, (int) Math.round(DEFAULT_DENSITY * 100));  
    setup = new JButton(SETUP_BUTTON);
    
    step = new JButton(String.format(STEP_BUTTON, 0));
    
    speedSlider = new JSlider(MIN_SPEED, MAX_SPEED, 1000 / GENERATION_DELAY);
    run = new JToggleButton(RUN_BUTTON_UNSELECTED);
    
    speedSlider.addChangeListener(this);
    setup.addActionListener(this);
    step.addActionListener(this);
    run.addActionListener(this);
    
    JPanel setupPanel = new JPanel(new GridLayout(2, 1));
    setupPanel.setBorder(new EmptyBorder(PADDING, PADDING, PADDING, PADDING));
    setupPanel.add(thresholdSlider);
    setupPanel.add(setup);
    
    JPanel stepPanel = new JPanel();
    stepPanel.setBorder(new EmptyBorder(PADDING, PADDING, PADDING, PADDING));
    stepPanel.add(step);
    
    JPanel runPanel = new JPanel(new GridLayout(2, 1));
    runPanel.setBorder(new EmptyBorder(PADDING, PADDING, PADDING, PADDING));
    runPanel.add(speedSlider);
    runPanel.add(run);
    
    controls.add(setupPanel);
    controls.add(stepPanel);
    controls.add(runPanel);
    frame.add(controls, BorderLayout.SOUTH);
    
    frame.pack();
    frame.setVisible(true);
    synchronized (this) {
      uiSetup = true;
      notify();
    }
  }
  
  private synchronized void run() {
    while (true) {
      if (populating) {
        populating = false;
        density = thresholdSlider.getValue() / 100d;
        life.populate(density);
        surface.setField(life.getField());
        SwingUtilities.invokeLater(() -> 
            step.setText(String.format(STEP_BUTTON, 0)));
        try {
          wait();
        } catch (InterruptedException ex) {
          // Do nothing.
        }
      } else if (running) {
        step();
        try {
          wait(sleepInterval );
        } catch (InterruptedException ex) {
          // Do nothing.
        }
      } else if (stepping) {
        stepping = false;
        step();
        try {
          wait();
        } catch (InterruptedException ex) {
          // Do nothing.
        }
      } else {
        try {
          wait();
        } catch (InterruptedException ex) {
          // Do nothing.
        }
      }
    }
  }
  
  private void step() {
    life.step();
    surface.setField(life.getField());
    SwingUtilities.invokeLater(() -> step.setText(String.format(STEP_BUTTON, life.getGeneration())));
  }

  @Override
  public void stateChanged(ChangeEvent ev) {
    synchronized (this) {
      sleepInterval = Math.round(1000.0F / speedSlider.getValue());
    }
  }

}
