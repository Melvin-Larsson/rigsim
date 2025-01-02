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
	private static final Dimension WINDOW_SIZE = new Dimension(500, 600);

	private ParticleSystem system;

	private Thread simulationThread;
	private BlockingQueue<Runnable> tasks = new LinkedBlockingQueue<>();

	private boolean isEditing;

	public App() throws InterruptedException {
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setSize(WINDOW_SIZE);

		JPanel simulationPanel = createSimulationPanel();

		Editor editor = new Editor(PIXELS_PER_METER);
		this.add(editor, BorderLayout.CENTER);
		isEditing = true;

		JButton toggleModeButton = new JButton("Simulate");
		toggleModeButton.addActionListener(e -> {
			if(isEditing){
				system = new ParticleSystem((int)(WINDOW_SIZE.width / PIXELS_PER_METER.getX()), (int)(WINDOW_SIZE.width / PIXELS_PER_METER.getY()));
				editor.initializeSystem(system);
				system.addForce(new GravitationalForce());

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

		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		simulation.add(toolBar, BorderLayout.NORTH);

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
