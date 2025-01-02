package org.example;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Observable;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.*;
import javax.swing.text.html.Option;

import org.example.editor.Editor;
import org.example.simulation.*;

public class App extends JFrame{
	private static final Vector2 PIXELS_PER_METER = new Vector2(60f,60f);
	private float radius = 0.1f;
	private static final Dimension WINDOW_SIZE = new Dimension(800, 600);

	private static final float DEFAULT_GRAVITY = 9.82f;
	private static final float MIN_GRAVITY = -100f;
	private static final float MAX_GRAVITY = 100f;
	private static final float GRAVITY_STEP = 1f;

	private static final float DEFAULT_VISCOUS_DRAG = 0.01f;
	private static final float MIN_VISCOUS_DRAG = -100f;
	private static final float MAX_VISCOUS_DRAG = 100f;
	private static final float VISCOUS_DRAG_STEP = 0.01f;


	private static final float DEFAULT_BOUNCE_KEEP_PERCENTAGE = ParticleSystem.DEFAULT_BOUNCE_KEEP * 100;
	private static final float MIN_BOUNCE_KEEP_PERCENTAGE = 0;
	private static final float MAX_BOUNCE_KEEP_PERCENTAGE = 100;
	private static final float BOUNCE_KEEP_PERCENTAGE_STEP = 1;

	private JSpinner gravitySpinner;
	private JSpinner bounceKeepSpinner;
	private JSpinner viscousDragSpinner;

	private GravitationalForce gravity;
	private ViscousDragForce viscousDrag;

	private JPanel simulationPanel;
	private Editor editor;
	private JButton toggleModeButton;
	private Optional<File> currFile;
	private ParticleSystem system;

	private Thread simulationThread;
	private BlockingQueue<Runnable> tasks = new LinkedBlockingQueue<>();

	private boolean isEditing;

	public App() throws InterruptedException {
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setMinimumSize(WINDOW_SIZE);

		this.setJMenuBar(createMenuBar());

		this.simulationPanel = createSimulationPanel();

		this.editor = new Editor(PIXELS_PER_METER);
		this.currFile = Optional.empty();
		this.add(this.editor, BorderLayout.CENTER);

		this.toggleModeButton = new JButton("Simulate");
		toggleModeButton.addActionListener(e -> {
			toggleDisplay();
		});
		this.add(toggleModeButton, BorderLayout.SOUTH);
		this.displayEditor();


		this.setVisible(true);
	}

	private JPanel createSimulationPanel(){
		JPanel simulation = new JPanel();
		simulation.setLayout(new BorderLayout());

		simulation.add(createToolbar(), BorderLayout.NORTH);

		JPanel content = new JPanel(){
			@Override
			public void paint(Graphics g){
				for (SimulationParticle particle : system.getParticles()){
					Point point = getPosition(particle.getPosition(), radius);
					g.fillOval(point.x, point.y, (int)(radius * 2 * PIXELS_PER_METER.getX()), (int)(radius * 2 * PIXELS_PER_METER.getY()));
				}

				for (Force force : system.getForces()){
					switch(force){
						case SpringForce sf:
							Point p1 = getPosition(sf.getParticleA().getPosition());
							Point p2 = getPosition(sf.getParticleB().getPosition());
							g.drawLine(p1.x, p1.y, p2.x, p2.y);
							break;
						default:
							break;
					}
				}
			}
		};
		simulation.add(content, BorderLayout.CENTER);

		return simulation;
	}

	private JToolBar createToolbar(){
		JToolBar toolBar = new JToolBar();
		toolBar.setLayout(new FlowLayout(FlowLayout.LEADING, 5, 5));
		toolBar.setFloatable(false);

		JButton restartButton = new JButton("Restart");
		restartButton.addActionListener(e -> tasks.add(() -> {
			system.reset();
		}));
		toolBar.add(restartButton);

		JRadioButton eulerSolverRadio = new JRadioButton("Euler");
		JRadioButton midPointSolverRadio = new JRadioButton("Midpoint");
		JRadioButton rungeKuttaSolverRadio = new JRadioButton("RungeKutta");

		ActionListener solverAction = e -> {
			if(e.getSource().equals(eulerSolverRadio)){
				tasks.add(() -> {
					system.setSolver(new EulerSolver());
				});
			}
			else if(e.getSource().equals(midPointSolverRadio)){
				tasks.add(() -> {
					system.setSolver(new MidPointSolver());
				});
			}
			else{
				tasks.add(() -> {
					system.setSolver(new RungeKuttaSolver());
				});
			}
		};
		eulerSolverRadio.addActionListener(solverAction);
		midPointSolverRadio.addActionListener(solverAction);
		rungeKuttaSolverRadio.addActionListener(solverAction);

		rungeKuttaSolverRadio.setSelected(true);

		ButtonGroup g = new ButtonGroup();
		g.add(eulerSolverRadio);
		g.add(midPointSolverRadio);
		g.add(rungeKuttaSolverRadio);
		toolBar.add(eulerSolverRadio);
		toolBar.add(midPointSolverRadio);
		toolBar.add(rungeKuttaSolverRadio);

		toolBar.addSeparator();

		JLabel gravityLabel = new JLabel("Gravity");
		this.gravitySpinner = new JSpinner(new SpinnerNumberModel(DEFAULT_GRAVITY, MIN_GRAVITY, MAX_GRAVITY, GRAVITY_STEP));
		this.gravitySpinner.addChangeListener(e -> this.gravity.setAcceleration(getGravity()));
		toolBar.add(gravityLabel);
		toolBar.add(this.gravitySpinner);
		toolBar.addSeparator();

		JLabel viscousDragLabel = new JLabel("Viscous drag");
		this.viscousDragSpinner = new JSpinner(new SpinnerNumberModel(DEFAULT_VISCOUS_DRAG, MIN_VISCOUS_DRAG, MAX_VISCOUS_DRAG, VISCOUS_DRAG_STEP));
		this.viscousDragSpinner.addChangeListener(e -> this.viscousDrag.setDrag(getViscousDrag()));
		toolBar.add(viscousDragLabel);
		toolBar.add(this.viscousDragSpinner);

		JLabel bounceLabel = new JLabel("Bounce");
		this.bounceKeepSpinner = new JSpinner(new SpinnerNumberModel(DEFAULT_BOUNCE_KEEP_PERCENTAGE, MIN_BOUNCE_KEEP_PERCENTAGE, MAX_BOUNCE_KEEP_PERCENTAGE, BOUNCE_KEEP_PERCENTAGE_STEP));
		this.bounceKeepSpinner.addChangeListener(e -> this.system.setBounceKeep(getBounceKeep() / 100));
		toolBar.add(bounceLabel);
		toolBar.add(this.bounceKeepSpinner);
		toolBar.addSeparator();

		return toolBar;
	}

	private JMenuBar createMenuBar(){
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(createFileMenu());

		return menuBar;
	}

	private JMenu createFileMenu(){
		JMenu menu = new JMenu("File");

		JMenuItem load = new JMenuItem("Open");
		load.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
		load.addActionListener(e -> load());
		menu.add(load);

		menu.addSeparator();

		JMenuItem save = new JMenuItem("Save...");
		save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
		save.addActionListener(e -> save());
		menu.add(save);

		JMenuItem saveAs = new JMenuItem("Save As...");
		saveAs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
		saveAs.addActionListener(e -> selectFileAndSave());
		menu.add(saveAs);

		return menu;
	}

	private void toggleDisplay(){
		if(isEditing){
			displaySimulation();
		}else{
			displayEditor();
		}
	}

	private void displayEditor(){
		if(simulationThread != null){
			simulationThread.interrupt();
		}

		App.this.remove(simulationPanel);
		App.this.add(this.editor, BorderLayout.CENTER);

		isEditing = !isEditing;
		App.this.revalidate();

		this.isEditing = true;
		this.toggleModeButton.setText("Simulate");
	}

	private void displaySimulation(){
		system = new ParticleSystem((int)(WINDOW_SIZE.height * 0.8f / PIXELS_PER_METER.getX()), (int)(WINDOW_SIZE.height * 0.8f / PIXELS_PER_METER.getY()));
		this.editor.initializeSystem(system);
		this.gravity = new GravitationalForce(getGravity());
		this.viscousDrag = new ViscousDragForce(getViscousDrag());
		system.addForce(this.gravity);
		system.addForce(this.viscousDrag);
		system.setBounceKeep(getBounceKeep() / 100);

		App.this.remove(this.editor);
		App.this.add(simulationPanel, BorderLayout.CENTER);

		simulationThread = new Thread(){
			@Override
			public void run() {
				simulate();
			}
		};
		simulationThread.start();

		this.isEditing = false;
		this.toggleModeButton.setText("Edit");
	}

	private void save(){
		if(currFile.isPresent()){
			save(currFile.get());
			return;
		}
		selectFileAndSave();
	}

	private void selectFileAndSave(){
		findFreeFileName();
		FileDialog dialog = new FileDialog(this, "Save As", FileDialog.SAVE);
		if(currFile.isPresent()){
			dialog.setFile(currFile.get().getAbsolutePath());
		}else{
			System.out.println(findFreeFileName().getAbsolutePath());
			dialog.setFile(findFreeFileName().getAbsolutePath());
		}
		dialog.setFilenameFilter(getFilenameFilter());
		dialog.setVisible(true);
		if(dialog.getFile() == null){
			return;
		}
		File file = new File(dialog.getDirectory(), dialog.getFile());
		save(file);
	}

	private File findFreeFileName(){
		File test = new File("Untitled.psim");
		if(!test.exists()){
			return test;
		}

		System.out.println("Finding free filename");
		for(int i = 1; true; i++){
			test = new File("Untitled(" + i + ").psim") ;
			if(!test.exists()){
				return test;
			}
		}
	}

	private void save(File file){
		this.currFile = Optional.of(file);

        try (FileOutputStream os = new FileOutputStream(file)){
			ObjectOutputStream ous = new ObjectOutputStream(os);
			this.editor.save(ous);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

	private void load(){
		FileDialog dialog = new FileDialog(this, "Load", FileDialog.LOAD);
		dialog.setFilenameFilter(getFilenameFilter());
		dialog.setVisible(true);
		if(dialog.getFile() == null){
			return;
		}
		File file = new File(dialog.getDirectory(), dialog.getFile());
		load(file);

		this.currFile = Optional.of(file);
		displayEditor();
	}

	private void load(File file){
		try (FileInputStream is = new FileInputStream(file)){
			ObjectInputStream ois = new ObjectInputStream(is);
			this.editor.load(ois);
		} catch (IOException | ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
    }

	private FilenameFilter getFilenameFilter(){
		return new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".psim");
			}
		};
	}

	private void simulate(){
		long lastTime = System.nanoTime();
		while(true){
			long time = System.nanoTime();
			long dt = time - lastTime;
			lastTime = time;

			system.step(dt / 1000_000_000f);
//			system.step(0.00002f);
			this.repaint();

			Runnable task = tasks.poll();
			if(task != null){
				task.run();
			}

			try {
				Thread.sleep(0);
			} catch (InterruptedException e) {
				return;
			}
		}
	}

	private float getGravity(){
		return (float)(double)this.gravitySpinner.getValue();
	}

	private float getViscousDrag(){
		return (float)(double)this.viscousDragSpinner.getValue();
	}

	private float getBounceKeep(){
		return (float)(double)this.bounceKeepSpinner.getValue();
	}

	private Point getPosition(Vector2 v, float radius){
		return new Point(Math.round((v.getX() - radius) * PIXELS_PER_METER.getX()), Math.round((v.getY() - radius) * PIXELS_PER_METER.getY()));
	}

	private Point getPosition(Vector2 v){
		return new Point(Math.round((v.getX() * PIXELS_PER_METER.getX())), Math.round(v.getY() * PIXELS_PER_METER.getY()));
	}

    public static void main(String[] args) throws InterruptedException {
    	new App();
    }
}
