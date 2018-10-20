//Anjaneya Bhardwaj
package epidemiology;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;

import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

@SuppressWarnings("serial")
public class Epidemiology3 extends JPanel implements ActionListener {
	
	//standard SIR model for herd immunity/epidemiology simulator (susceptible, infected, recovered)
	//2d array based variant
	private JButton startBtn;
	private JButton stopBtn;
	private JButton resetBtn;
	private JButton speedBtn;
	private JButton imageBtn;
	private JTextField sizeTxt;
	private JTextField immRateTxt;
	private JTextField unvacInfRateTxt;
	private JTextField genSkipTxt;
	private JTextField vacInfRateTxt;
	private JTextField durationTxt;
	private JTextField densityTxt; 
	private JLabel vacLbl;		//states number of vaccinated
	private JLabel susLbl;		//states number of susceptible
	private JLabel infLbl;		//states number of infected
	private JLabel recLbl; 		//states number of recovered
	private JLabel picLbl;		//where the drawing happens
	private int vOffset;
	private int hOffset;
	private int vMax;
	private int hMax;
	private int genSkip;
	private int size = 10;
	private int[][] cells;
	private int[] speeds = new int[4];
	private int speedIndex;
	private Image pic;
	private Color[] colors;
	private double density;					//how densely filled to make the population
	private double immRate;					//percent of population that are immunized
	private double unvacInfRate; 			//percent chance unvaccinated people get infected
	private double vacInfRate;  			//percent chance vaccinated people get infected
	private int duration;					//how many ticks an infected person stays infectious
	private int vaccinated;
	private int susceptible;
	private int infectedCount;
	private int recoveredCount;
	private Timer timer;
	private boolean isRunning;
	
	public static final int EMPTY = 0;
	public static final int VAC = 1;
	public static final int UNVAC = 2;
	public static final int INFECTED = 3;
	public static final int RECOVERED = Integer.MAX_VALUE;
	
    public Epidemiology3(int xDim, int yDim) {
        super(new GridBagLayout());
        setBackground(Color.LIGHT_GRAY);
		addMouseListener(new MAdapter());
		addMouseMotionListener(new MAdapter());
		setFocusable(true);
		setDoubleBuffered(true);
		pic = new BufferedImage(xDim, yDim, BufferedImage.TYPE_INT_RGB);
		picLbl = new JLabel(new ImageIcon(pic));
		//initializing the colors...note how their indices match up to the final variables
		colors = new Color[] {
				 Color.WHITE,		//empty
				 Color.ORANGE,		//vaccinated 
				 Color.RED,			//unvaccinated
				 Color.GREEN,		//infected, contagious
				 Color.BLACK		//infected but recovered/not contagious
		};
		initBtns();
		initTxt();
		initLabels();
		addThingsToPanel();
		genSkip = 1;
		vMax = yDim;
		hMax = xDim;
		immRate = Double.parseDouble(immRateTxt.getText()) / 100;			//percent of agents immunized at start of run
		unvacInfRate = Double.parseDouble(unvacInfRateTxt.getText()) / 100;	//percent chance for unvaccinated agent to get infected
		vacInfRate = Double.parseDouble(vacInfRateTxt.getText()) / 100;		//percent chance for vaccinated agent to get infected
		duration = Integer.parseInt(durationTxt.getText());					//radius of square in which someone can get infected
		density = Double.parseDouble(densityTxt.getText()) / 100;			//density of the population in the grid
		size = Integer.parseInt(sizeTxt.getText());							//pixel size of agent cells
		isRunning = false;
		for (int i = 0; i < 4; i++) {					//set the speed variation
			speeds[3-i] = 100 * i * i;
		}
		//initialize the cells		
		cells = new int[xDim / size][yDim / size];		//make the cells values represent the state of that cell!
		resetSim();
		drawCells(pic.getGraphics());
		timer = new Timer(speeds[speedIndex], this);	//initialize the timer
		timer.start();									//start up the sim
    }

    //a bunch of really irritating code so that things look nice
    public void addThingsToPanel() {
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(1, 1, 0, 1);
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 5;
		c.gridheight = 14;
		add(picLbl, c);
		c.gridwidth = 1;
		c.gridheight = 1;
		c.insets = new Insets(0, 2, 0, 2);
		c.gridx = 0;
		c.gridy = 0;
		add(startBtn, c);
		c.gridx = 1;
		c.gridy = 0;
		add(stopBtn, c);
		c.gridx = 2;
		add(resetBtn, c);
		c.gridx = 3;
		add(speedBtn, c);
		c.insets = new Insets(0, 10, 0, 10);
		c.gridx = 6;
		c.gridy = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		add(imageBtn, c);
		c.gridy = 2;
		c.fill = GridBagConstraints.BOTH;
		add(new JLabel("Skip Generations"), c);
		c.gridy = 3;
		add(new JLabel("% immunized"), c);
		c.gridy = 4;
		add(new JLabel("% infect chance unvac"), c);
		c.gridy = 5;
		add(new JLabel("% infect chance vac"), c);
		c.gridy = 6;
		add(new JLabel("Infectious duration"), c);
		c.gridy = 7;
		add(new JLabel("Cell size"), c);
		c.gridy = 8;
		add(new JLabel("% density"), c);
		c.gridy = 9;
		add(new JLabel(" "), c);
		c.gridy = 10;
		add(new JLabel("Vaccinated"), c);
		c.gridy = 11;
		add(new JLabel("Susceptible"), c);
		c.gridy = 12;
		add(new JLabel("Infected"), c);
		c.gridy = 13;
		add(new JLabel("Recovered"), c);
		c.gridx = 7;
		c.gridy = 2;
		add(genSkipTxt, c);    	
		c.gridy = 3;
		add(immRateTxt, c);
		c.gridy = 4;
		add(unvacInfRateTxt, c);
		c.gridy = 5;
		add(vacInfRateTxt, c);
		c.gridy = 6;
		add(durationTxt, c);
		c.gridy = 7;
		add(sizeTxt, c);
		c.gridy = 8;
		add(densityTxt, c);
		c.gridy = 10;
		add(vacLbl, c);
		c.gridy = 11;
		add(susLbl, c);
		c.gridy = 12;
		add(infLbl, c);
		c.gridy = 13;
		add(recLbl, c);
    }
    
    public void initTxt() {
    	genSkipTxt = new JTextField("1", 4);
    	genSkipTxt.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent e) {
    			genSkip = Integer.parseInt(genSkipTxt.getText());
    		}
    	});
    	
    	immRateTxt = new JTextField("50", 4);
    	immRateTxt.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent e) {
    			immRate = Double.parseDouble(immRateTxt.getText()) / 100;
				resetSim();
				drawCells(pic.getGraphics());
    		}
    	});
    	
    	unvacInfRateTxt = new JTextField("90", 4);
    	unvacInfRateTxt.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent e) {
    			unvacInfRate = Double.parseDouble(unvacInfRateTxt.getText()) / 100;
    		}
    	});
    	vacInfRateTxt = new JTextField("10", 4);
    	vacInfRateTxt.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent e) {
    			vacInfRate = Double.parseDouble(vacInfRateTxt.getText()) / 100;
    		}
    	});
    	durationTxt = new JTextField("10", 4);
    	durationTxt.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent e) {
    			duration = Integer.parseInt(durationTxt.getText());
    		}
    	});
    	sizeTxt = new JTextField("10", 4);
    	sizeTxt.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent e) {
    			size = Integer.parseInt(sizeTxt.getText());
				resetSim();
				drawCells(pic.getGraphics());
    		}
    	});
    	densityTxt = new JTextField("40", 4);
    	densityTxt.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent e) {
    			density = Double.parseDouble(densityTxt.getText()) / 100;
    			resetSim();
    			drawCells(pic.getGraphics());
    		}
    	});
    }

    public void initLabels() {
    	vacLbl = new JLabel(" " + vaccinated);
    	susLbl = new JLabel(" " + susceptible);
    	infLbl = new JLabel(" " + infectedCount);
    	recLbl = new JLabel(" " + recoveredCount);
    	vacLbl.setForeground(colors[VAC]);
    	susLbl.setForeground(colors[UNVAC]);
    	infLbl.setForeground(colors[INFECTED]);
    	recLbl.setForeground(colors[colors.length - 1]);
    }
    
    public void initBtns() {
		startBtn = new JButton("Start");
		startBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				isRunning = true;
			}
		});
		
		stopBtn = new JButton("Stop");
		stopBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				isRunning = false;
			}
		});
		
		resetBtn = new JButton("Reset");
		resetBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				resetSim();
				drawCells(pic.getGraphics());
			}
		});    	
		
		imageBtn = new JButton("Save Picture");
		imageBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					Calendar c = Calendar.getInstance();
					String fileName = ".\\d=" + immRate + " a=" + unvacInfRate + " r=" + duration + " @" + c.get(Calendar.HOUR) + "." + c.get(Calendar.MINUTE) + "." + c.get(Calendar.SECOND)+ ".png";
					System.out.println(fileName);
					File outputFile = new File(fileName);
					outputFile.createNewFile();
					ImageIO.write((RenderedImage) pic, "png", outputFile);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		
		speedBtn = new JButton("Speed = Fast");
		speedIndex = 2;
		speedBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				speedIndex = (speedIndex + 1) % 4;
				timer.setDelay(speeds[speedIndex]);
				switch (speedIndex) {
				case 0 : {
					speedBtn.setText("Speed = Slow");
					break;
				}
				case 1 : {
					speedBtn.setText("Speed = Med");
					break;
				}
				case 2 : {
					speedBtn.setText("Speed = Fast");
					break;
				}
				case 3 : {
					speedBtn.setText("Speed = Whoa");
					break;
				}
				}
			}
		});
    }
    
    public Epidemiology3() {
        super();
        setBackground(Color.WHITE);
		addMouseListener(new MAdapter());
		setFocusable(true);
		setDoubleBuffered(true);
	}
 
    public void paintComponent(Graphics g) { 	                 // draw graphics in the panel
        super.paintComponent(g);                              	 // call superclass' method to make panel display correctly
    }
        
    public void drawCells(Graphics g) {
    	for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[i].length; j++) {
				if (cells[i][j] == EMPTY) {
					g.setColor(Color.WHITE);
					g.fillRect(j * size, i * size, size, size);
				}
				else if(cells[i][j] == VAC) {
					g.setColor(Color.YELLOW);
					g.fillRect(j * size, i * size, size, size);
				}
				else if(cells[i][j] == UNVAC) {
					g.setColor(Color.RED);
					g.fillRect(j * size, i * size, size, size);
				}
				else if(cells[i][j] >= INFECTED && cells[i][j] <= INFECTED+duration) {
					g.setColor(Color.GREEN);
					g.fillRect(j * size, i * size, size, size);
				}
				else if(cells[i][j] == RECOVERED) {
					g.setColor(Color.BLACK);
					g.fillRect(j * size, i * size, size, size);
				}
			}
		}
    }

    public void resetSim() {
    	cells = new int[hMax / size][vMax / size];
    	vaccinated = 0;
    	susceptible = 0;
    	infectedCount = 0;
    	recoveredCount = 0;

    	for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[i].length; j++) {
				if(Math.random()<density){
					cells[i][j] = 2;
					susceptible++;
				if(Math.random()<immRate && cells[i][j] == 2){
					cells[i][j] = 1;
					susceptible--;
					vaccinated++;
				}
				}
			}
    	}
    	
		isRunning = false;
    }
    
    //attempts to allow the entity at location row,col to walk to a nearby location
    public Point findEmpty(int row, int col) {
		int yCells = pic.getHeight(null) / size; //ycells
		int xCells = pic.getWidth(null) / size;  //xcells
    	Point result = new Point(row, col);
		for (int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++) {
				if (cells[(yCells + i + row) % yCells][(xCells + j + col) % xCells] == EMPTY) {
					if ((Math.random()*100)<25.0) {
						result = new Point((yCells + i + row) % yCells, (xCells + j + col) % xCells);
					}
				}
			}
		}
    	return result;
    }
    
    //returns true if there's an infected (or worse, but not recovered) cell adjacent to row,col, false otherwise
    public boolean canCatch(int row, int col) {
    	int yCells = pic.getHeight(null) / size; //ycells
		int xCells = pic.getWidth(null) / size;  //xcells
		if (cells[row][col] == UNVAC || cells[row][col] == VAC){
			
		for (int i = -1; i <= 1; i++) 
			for (int j = -1; j <= 1; j++){
		if(cells[(yCells + i + row) % yCells][(xCells + j + col) % xCells] >= INFECTED &&
				cells[(yCells + i + row) % yCells][(xCells + j + col) % xCells] <= INFECTED+duration){
			return true;
		}
			}
			}
        return false;
    }



    public void moveAllCells() {
    	Point reloc = null;
    	int choice = (int)(Math.random() * 4);
    	if (choice == 0) {
        	for (int i = 0; i < cells.length; i++) {
    			for (int j = 0; j < cells[0].length; j++) {
    				if (cells[i][j] != EMPTY) { //if the cell is vac unvac or infected
        				reloc = findEmpty(i, j); // use findempty to move it to the empty space
        				if (!reloc.equals(new Point(i, j))) { //if the cells moves make the other one empty
        		    		cells[reloc.x][reloc.y] = cells[i][j];
        		    		cells[i][j] = 0;
        				}
    				}
    			}
    		}
    	} else if (choice == 1) {
        	for (int i = cells.length - 1; i >= 0; i--) {
    			for (int j = cells[0].length - 1; j >= 0; j--) {
    				if (cells[i][j] != EMPTY) {
        				reloc = findEmpty(i, j);
        				if (!reloc.equals(new Point(i, j))) {
        		    		cells[reloc.x][reloc.y] = cells[i][j];
        		    		cells[i][j] = 0;
        				}
    				}
    			}
    		}
    	} else if (choice == 2) {
        	for (int i = 0; i < cells.length; i++) {
    			for (int j = cells[0].length - 1; j >= 0; j--) {
    				if (cells[i][j] != EMPTY) {
        				reloc = findEmpty(i, j);
        				if (!reloc.equals(new Point(i, j))) {
        		    		cells[reloc.x][reloc.y] = cells[i][j];
        		    		cells[i][j] = 0;
        				}
    				}
    			}
    		}
    		
    	} else {
        	for (int i = cells.length - 1; i >= 0; i--) {
    			for (int j = 0; j < cells[0].length; j++) {
    				if (cells[i][j] != EMPTY) {
        				reloc = findEmpty(i, j); 
        				if (!reloc.equals(new Point(i, j))) {
        		    		cells[reloc.x][reloc.y] = cells[i][j];
        		    		cells[i][j] = 0;
        				}
    				}
    			}
    		}
    		
    	}

    }

    public void updateCells() {
    	ArrayList<Point> changed = new ArrayList<Point>();
    	for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[i].length; j++) {
			    if(cells[i][j] == VAC && (Math.random()*100)<vacInfRate && canCatch(i,j)){
			    	Point vac = new Point(i, j);
			    	changed.add(vac);
			    }
			    else if(cells[i][j] == UNVAC && (Math.random()*100)<unvacInfRate && canCatch(i,j)){
			    	Point unvac = new Point(i, j);
			    	changed.add(unvac);
			    }
				else if (cells[i][j] >= INFECTED && cells[i][j] < INFECTED+duration){
					cells[i][j]++;
				}
				else if (cells[i][j] >= INFECTED+duration && cells[i][j] < RECOVERED){
					Point recover = new Point(i, j);
					changed.add(recover);
				}
			    }
			
    }

    	for(Point p : changed) {
			if(cells[(int) p.getY()][(int) p.getX()] == INFECTED+duration){
				infectedCount--;
				recoveredCount++;
				cells[(int) p.getY()][(int) p.getX()] = RECOVERED;
			}
    		else if(cells[(int) p.getY()][(int) p.getX()] == UNVAC){
    			susceptible--;
    			infectedCount++;
    			cells[(int) p.getY()][(int) p.getX()] = INFECTED;
    		}
    		else if(cells[(int) p.getY()][(int) p.getX()] == VAC){
    			vaccinated--;
    			infectedCount++;
    			cells[(int) p.getY()][(int) p.getX()] = INFECTED;
    		}

    	
  	}
    	//lastly, let cells wander and stop the sim if there's nothing that can infect anything
        	moveAllCells();
	    	isRunning = (infectedCount != 0); //don't keep running needlessly
    }
    
    
    public void updateLabels() {
    	vacLbl.setText(" " + vaccinated);
    	susLbl.setText(" " + susceptible);
    	infLbl.setText(" " + infectedCount);
    	recLbl.setText(" " + recoveredCount);
    }
    
	@Override
	public void actionPerformed(ActionEvent e) {//update the graphics and the offsets
		if (isRunning) {
			updateCells();			
	        drawCells(pic.getGraphics());
		}
		hOffset = picLbl.getLocationOnScreen().x - getLocationOnScreen().x;
		vOffset = picLbl.getLocationOnScreen().y - getLocationOnScreen().y;
		updateLabels();
		repaint();
	}

	private class MAdapter extends MouseAdapter {
		
		@Override
		public void mousePressed(MouseEvent e) {
			Point p = new Point((e.getY()- vOffset) / size, (e.getX()- hOffset) / size);
			try {
				if (cells[p.x][p.y] == VAC) {
					vaccinated--;
				}
				if (cells[p.x][p.y] == UNVAC) {
					susceptible--;
				}
				if (cells[p.x][p.y] == RECOVERED) {
					recoveredCount--;
				}
				if (cells[p.x][p.y] != INFECTED) {
					cells[p.x][p.y] = INFECTED;
					infectedCount++;
					drawCells(pic.getGraphics());
				}
			} catch (ArrayIndexOutOfBoundsException e2) {
			}
		}

	}
}

