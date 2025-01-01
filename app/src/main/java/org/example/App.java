package org.example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.management.relation.RelationNotFoundException;
import javax.swing.*;

import org.ejml.data.*;
import org.ejml.simple.*;

public class App extends JFrame{
	private static final Vector2 PIXELS_PER_METER = new Vector2(60f,60f);
	private float radius = 0.1f;
	private static final Dimension WINDOW_SIZE = new Dimension(500, 600);

	public App() {
		SoftBodySystem sbs = new SurfaceSphereSystem((int)(WINDOW_SIZE.width / PIXELS_PER_METER.getX()), (int)(WINDOW_SIZE.width / PIXELS_PER_METER.getY()));
		ParticleSystem system = sbs.getSystem();

		system.addForce(new ViscousDragForce(0.01f));
		system.addForce(new GravitationalForce());

	/*	ParticleSystem system = new ParticleSystem((int)(500 / PIXELS_PER_METER.getX()), (int)(500 / PIXELS_PER_METER.getY()));
		float mass = 10;

		ParticleSystem.Particle[] ps = new ParticleSystem.Particle[10];
		Vector2 center = new Vector2(2,2);
		float radius = 1f;
		for(int i = 0; i < ps.length; i++){
			float angle = (float)(i * 2 * Math.PI / ps.length);
			Vector2 pos = center.add(new Vector2((float)Math.cos(angle) * radius, (float)Math.sin(angle) * radius));
			ps[i] = system.addParticle(pos, mass / ps.length);
		}
		for(int i = 0; i < ps.length; i++){
			ParticleSystem.Particle curr = ps[i];
			for (int j = i + 1; j < ps.length; j++){
				ParticleSystem.Particle next = ps[j];
				Vector2 diff = next.getPosition().sub(curr.getPosition());
				system.addForce(new SpringForce(curr, next, diff.length(), 500, 1000));
			}
		}
		system.addForce(new ViscousDragForce(1f));
		system.addForce(new GravitationalForce());
*/
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setSize(WINDOW_SIZE);

		BlockingQueue<Runnable> tasks = new LinkedBlockingQueue<>();

		BorderLayout borderLayout = new BorderLayout();
		this.setLayout(borderLayout);

		JPanel topPanel = new JPanel();
		this.add(topPanel, BorderLayout.NORTH);

		JCheckBox showMeshBox = new JCheckBox("Show mesh");
		topPanel.add(showMeshBox);

		JButton restartButton = new JButton("Restart");
		restartButton.addActionListener(e -> tasks.add(() -> {
			system.reset();
		}));
		topPanel.add(restartButton);


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

		system.setSolver(new RungeKuttaSolver());
		rungeKuttaSolverRadio.setSelected(true);

		ButtonGroup g = new ButtonGroup();
		g.add(eulerSolverRadio);
		g.add(midPointSolverRadio);
		g.add(rungeKuttaSolverRadio);
		topPanel.add(eulerSolverRadio);
		topPanel.add(midPointSolverRadio);
		topPanel.add(rungeKuttaSolverRadio);

		JPanel content = new JPanel(){
			@Override
			public void paint(Graphics g){
//				for (Vector2 pos : sbs.getNodes()){
//					Point point = getPosition(pos, radius);
//					g.fillOval(point.x, point.y, (int)(radius * 2 * PIXELS_PER_METER.getX()), (int)(radius * 2 * PIXELS_PER_METER.getY()));
//				}

				if(showMeshBox.isSelected()){
					for(Line line : sbs.getMesh()){
						Point start = getPosition(line.start);
						Point end = getPosition(line.end);
						g.drawLine(start.x, start.y, end.x, end.y);
					}
				}
				else{
					Vector2[] border = sbs.getBorder();
					int x[] = new int[border.length];
					int y[] = new int[border.length];
					for(int i = 0; i < border.length; i++){
						Point p = getPosition(border[i]);
						x[i] = p.x;
						y[i] = p.y;
					}
					g.fillPolygon(x, y, border.length);
				}
			}
		};
		this.add(content, BorderLayout.CENTER);

		this.setVisible(true);

		long lastTime = System.nanoTime();
		while(true){
			long time = System.nanoTime();
			long dt = time - lastTime;
			lastTime = time;

			system.step(dt / 1000_000_000f);
//			system.step(0.000001f);
			this.repaint();

			Runnable task = tasks.poll();
			if(task != null){
				task.run();
			}

			try {
                Thread.sleep(0);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
	}

	private Point getPosition(Vector2 v, float radius){
		return new Point(Math.round((v.getX() - radius) * PIXELS_PER_METER.getX()), Math.round((v.getY() - radius) * PIXELS_PER_METER.getY()));
	}

	private Point getPosition(Vector2 v){
		return new Point(Math.round((v.getX() * PIXELS_PER_METER.getX())), Math.round(v.getY() * PIXELS_PER_METER.getY()));
	}

    public static void main(String[] args) {
    	new App();
    }
}
