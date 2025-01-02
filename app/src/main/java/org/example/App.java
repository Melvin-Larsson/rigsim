package org.example;

import java.awt.*;
import java.awt.event.ActionListener;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.*;

import org.example.editor.Editor;
import org.example.simulation.*;

public class App extends JFrame{
	private static final Vector2 PIXELS_PER_METER = new Vector2(60f,60f);
	private float radius = 0.1f;
	private static final Dimension WINDOW_SIZE = new Dimension(800, 600);

	private static final float DEFAULT_GRAVITY = 9.82f;
	private static final float MIN_GRAVITY = -100f;
	private static final float MAX_GRAVITY = 100f;

	private static final float DEFAULT_VISCOUS_DRAG = 0.01f;
	private static final float MIN_VISCOUS_DRAG = -100f;
	private static final float MAX_VISCOUS_DRAG = 100f;

	private static final float DEFAULT_BOUNCE_KEEP_PERCENTAGE = ParticleSystem.DEFAULT_BOUNCE_KEEP * 100;
	private static final float MIN_BOUNCE_KEEP_PERCENTAGE = 0;
	private static final float MAX_BOUNCE_KEEP_PERCENTAGE = 100;

	private JSpinner gravitySpinner;
	private JSpinner bounceKeepSpinner;
	private JSpinner viscousDragSpinner;

	private GravitationalForce gravity;
	private ViscousDragForce viscousDrag;

	private ParticleSystem system;

	private Thread simulationThread;
	private BlockingQueue<Runnable> tasks = new LinkedBlockingQueue<>();

	private boolean isEditing;

	public App() throws InterruptedException {
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setMinimumSize(WINDOW_SIZE);

		JPanel simulationPanel = createSimulationPanel();

		Editor editor = new Editor(PIXELS_PER_METER);
		this.add(editor, BorderLayout.CENTER);
		isEditing = true;

		JButton toggleModeButton = new JButton("Simulate");
		toggleModeButton.addActionListener(e -> {
			if(isEditing){
				system = new ParticleSystem((int)(WINDOW_SIZE.height * 0.8f / PIXELS_PER_METER.getX()), (int)(WINDOW_SIZE.height * 0.8f / PIXELS_PER_METER.getY()));
				editor.initializeSystem(system);
				this.gravity = new GravitationalForce(getGravity());
				this.viscousDrag = new ViscousDragForce(getViscousDrag());
				system.addForce(this.gravity);
				system.addForce(this.viscousDrag);
				system.setBounceKeep(getBounceKeep() / 100);

				App.this.remove(editor);
				App.this.add(simulationPanel, BorderLayout.CENTER);
				toggleModeButton.setText("Edit");

				simulationThread = new Thread(){
					@Override
					public void run() {
						simulate();
					}
				};
				simulationThread.start();
			}else{
				simulationThread.interrupt();
				App.this.remove(simulationPanel);
				App.this.add(editor, BorderLayout.CENTER);
				toggleModeButton.setText("Simulate");
			}

			isEditing = !isEditing;
			App.this.revalidate();
		});
		this.add(toggleModeButton, BorderLayout.SOUTH);


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
		this.gravitySpinner = new JSpinner(new SpinnerNumberModel(DEFAULT_GRAVITY, MIN_GRAVITY, MAX_GRAVITY, 1));
		this.gravitySpinner.addChangeListener(e -> this.gravity.setAcceleration(getGravity()));
		toolBar.add(gravityLabel);
		toolBar.add(this.gravitySpinner);
		toolBar.addSeparator();

		JLabel viscousDragLabel = new JLabel("Viscous drag");
		this.viscousDragSpinner = new JSpinner(new SpinnerNumberModel(DEFAULT_VISCOUS_DRAG, MIN_VISCOUS_DRAG, MAX_VISCOUS_DRAG, 1));
		this.viscousDragSpinner.addChangeListener(e -> this.viscousDrag.setDrag(getViscousDrag()));
		toolBar.add(viscousDragLabel);
		toolBar.add(this.viscousDragSpinner);

		JLabel bounceLabel = new JLabel("Bounce");
		this.bounceKeepSpinner = new JSpinner(new SpinnerNumberModel(DEFAULT_BOUNCE_KEEP_PERCENTAGE, MIN_BOUNCE_KEEP_PERCENTAGE, MAX_BOUNCE_KEEP_PERCENTAGE, 1));
		this.bounceKeepSpinner.addChangeListener(e -> this.system.setBounceKeep(getBounceKeep() / 100));
		toolBar.add(bounceLabel);
		toolBar.add(this.bounceKeepSpinner);
		toolBar.addSeparator();


		return toolBar;
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
