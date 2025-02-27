package org.example;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Enumeration;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.*;

import org.example.editor.Editor;
import org.example.simulation.*;

public class App extends JFrame{
	private static final Scale SCALE = new Scale(60f,60f);
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

	private static final String CONFIG_FILE_NAME = "config";
	private static final String TEMP_EDITOR_SAVE = "temp-build";

	private JSpinner gravitySpinner;
	private JSpinner bounceKeepSpinner;
	private JSpinner viscousDragSpinner;

	private GravitationalForce gravity;
	private ViscousDragForce viscousDrag;

	private JTabbedPane tabbedPane;
	private JPanel simulationPanel;
	private JPanel simulationContent;
	private Editor editor;
	private Optional<File> currFile;
	private ParticleSystem system;
	private EOdeSolver currentSolver;

	private Thread simulationThread;
	private BlockingQueue<Runnable> tasks = new LinkedBlockingQueue<>();

	public App() throws Exception{
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setMinimumSize(WINDOW_SIZE);

		this.setJMenuBar(createMenuBar());

		AppConfig config = readConfig();
		this.currFile = Optional.ofNullable(config.currFile);
		this.system = new ParticleSystem(new Vector2(0, 0), 0);
		this.viscousDrag = new ViscousDragForce(DEFAULT_VISCOUS_DRAG);
		this.gravity = new GravitationalForce(DEFAULT_GRAVITY);
		this.simulationPanel = createSimulationPanel();
		this.editor = new Editor(SCALE);
		loadTempEditorState(editor);

		this.tabbedPane = new JTabbedPane();
		this.tabbedPane.setTabPlacement(JTabbedPane.BOTTOM);
		this.tabbedPane.addTab("Editor", this.editor);
		this.tabbedPane.addTab("Simulation", this.simulationPanel);

		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e){
				super.windowClosed(e);
				saveConfig(new AppConfig(App.this.currentSolver, getGravity(), getBounceKeep(), getViscousDrag(), App.this.currFile.orElse(null)));
				saveTempEditorState(App.this.editor);
			}
		});

		this.setContentPane(this.tabbedPane);
		this.setVisible(true);
	}

	private JPanel createSimulationPanel(){
		JPanel simulation = new JPanel();
		simulation.setLayout(new BorderLayout());

		simulation.add(createToolbar(), BorderLayout.NORTH);

		JPanel content = new JPanel(){
			@Override
			public void paint(Graphics g){
				super.paint(g);

				if(system == null){
					return;
				}
				for (SimulationParticle particle : system.getParticles()){
					Point point = getPosition(particle.getPosition(), radius);
					Point size = SCALE.scaleToPixels(new Vector2(radius * 2, radius * 2)).toPoint();
					g.fillOval(point.x, point.y, size.x, size.y);
				}

				for (Force force : system.getForces()){
					switch(force){
						case SpringForce sf:
							Point p1 = SCALE.scaleToPixels(sf.getParticleA().getPosition()).toPoint();
							Point p2 = SCALE.scaleToPixels(sf.getParticleB().getPosition()).toPoint();
							g.drawLine(p1.x, p1.y, p2.x, p2.y);
							break;
						default:
							break;
					}
				}
			}
		};
		content.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				super.componentResized(e);
				updateSimulatorSize();
			}
			@Override
			public void componentShown(ComponentEvent e){
				super.componentShown(e);
				updateSimulatorSize();
			}

			private void updateSimulatorSize(){
				if(system != null){
					Vector2 worldSize = SCALE.scaleToMeters(content.getSize());
					system.setSize(worldSize);
				}
			}
		});
		content.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1, true));
		simulation.add(content, BorderLayout.CENTER);
		this.simulationContent = content; //FIXME: Hack

		return simulation;
	}

	private JToolBar createToolbar(){
		AppConfig config = readConfig();

		JToolBar toolBar = new JToolBar();
		toolBar.setLayout(new FlowLayout(FlowLayout.LEADING, 5, 5));
		toolBar.setFloatable(false);


		JButton restartButton = new JButton("Restart");
		restartButton.addActionListener(e -> tasks.add(() -> {
			system.reset();
		}));
		toolBar.add(restartButton);

		final String solverKey = "solver";
		ButtonGroup solverGroup = new ButtonGroup();
		EOdeSolver[] solvers = EOdeSolver.values();

		this.currentSolver = config.odeSolver;
		this.system.setSolver(this.currentSolver.getSolver());
		ActionListener solverListener = e ->{
			JRadioButton source = (JRadioButton) e.getSource();
			EOdeSolver solver = (EOdeSolver) source.getClientProperty(solverKey);
			this.currentSolver = solver;
			tasks.add(() -> {
				system.setSolver(solver.getSolver());
			});
		};
		for(EOdeSolver solver : solvers){
			JRadioButton solverRadio = new JRadioButton(solver.displayName);
			if(solver == this.currentSolver){
				solverRadio.setSelected(true);
			}
			solverRadio.putClientProperty(solverKey, solver);
			solverRadio.addActionListener(solverListener);
			solverGroup.add(solverRadio);
			toolBar.add(solverRadio);
		}

		toolBar.addSeparator();

		JLabel gravityLabel = new JLabel("Gravity");
		this.gravitySpinner = new JSpinner(new SpinnerNumberModel(config.gravity, MIN_GRAVITY, MAX_GRAVITY, GRAVITY_STEP));
		this.gravitySpinner.addChangeListener(e -> this.gravity.setAcceleration(getGravity()));
		toolBar.add(gravityLabel);
		toolBar.add(this.gravitySpinner);
		toolBar.addSeparator();

		JLabel viscousDragLabel = new JLabel("Viscous drag");
		this.viscousDragSpinner = new JSpinner(new SpinnerNumberModel(config.viscousDrag, MIN_VISCOUS_DRAG, MAX_VISCOUS_DRAG, VISCOUS_DRAG_STEP));
		this.viscousDragSpinner.addChangeListener(e -> this.viscousDrag.setDrag(getViscousDrag()));
		toolBar.add(viscousDragLabel);
		toolBar.add(this.viscousDragSpinner);

		JLabel bounceLabel = new JLabel("Bounce");
		this.bounceKeepSpinner = new JSpinner(new SpinnerNumberModel(config.bounce, MIN_BOUNCE_KEEP_PERCENTAGE, MAX_BOUNCE_KEEP_PERCENTAGE, BOUNCE_KEEP_PERCENTAGE_STEP));
		this.bounceKeepSpinner.addChangeListener(e -> this.system.setBounceKeep(getBounceKeep() / 100));
		toolBar.add(bounceLabel);
		toolBar.add(this.bounceKeepSpinner);
		toolBar.addSeparator();

		return toolBar;
	}

	private JMenuBar createMenuBar(){
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(createFileMenu());
		menuBar.add(createRunMenu());

		return menuBar;
	}

	private JMenu createRunMenu(){
		JMenu menu = new JMenu("Run");

		JMenuItem run = new JMenuItem("Load and Run");
		run.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
		run.addActionListener(e -> loadAndRun());
		menu.add(run);

		return menu;
	}

	private void loadAndRun(){
		if(simulationThread != null){
			simulationThread.interrupt();
		}

		Vector2 worldSize = SCALE.scaleToMeters(this.simulationContent.getSize());
		system = new ParticleSystem(worldSize, radius);
		system.setSolver(this.currentSolver.getSolver());
		this.editor.initializeSystem(system);
		this.gravity = new GravitationalForce(getGravity());
		this.viscousDrag = new ViscousDragForce(getViscousDrag());
		system.addForce(this.gravity);
		system.addForce(this.viscousDrag);
		MouseForce mouseForce = new MouseForce(SCALE);
		this.simulationContent.addMouseListener(mouseForce);
		this.simulationContent.addMouseMotionListener(mouseForce);
		system.addForce(mouseForce);
		system.setBounceKeep(getBounceKeep() / 100);

		simulationThread = new Thread(){
			@Override
			public void run() {
				simulate();
			}
		};
		simulationThread.start();
		this.tabbedPane.setSelectedComponent(simulationPanel);
	}

	private JMenu createFileMenu(){
		JMenu menu = new JMenu("File");

		JMenuItem newSave = new JMenuItem("New...");
		newSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
		newSave.addActionListener(e -> newSave());
		menu.add(newSave);

		menu.addSeparator();

		JMenuItem load = new JMenuItem("Open");
		load.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
		load.addActionListener(e -> load(this.editor));
		menu.add(load);

		menu.addSeparator();

		JMenuItem save = new JMenuItem("Save...");
		save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
		save.addActionListener(e -> save(this.editor));
		menu.add(save);

		JMenuItem saveAs = new JMenuItem("Save As...");
		saveAs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
		saveAs.addActionListener(e -> selectFileAndSave(this.editor));
		menu.add(saveAs);

		return menu;
	}

	private void newSave(){
		this.editor.reset();
		this.currFile = Optional.empty();
		this.tabbedPane.setSelectedComponent(editor);
	}

	private void saveTempEditorState(Editor editor){
		File tempEditorState = AppStateManager.getStateFile(TEMP_EDITOR_SAVE);
		save(tempEditorState);
	}

	private void loadTempEditorState(Editor editor){
		File tempEditorState = AppStateManager.getStateFile(TEMP_EDITOR_SAVE);
		if(!tempEditorState.exists()){
			return;
		}
		load(tempEditorState, editor);
	}

	private void save(Editor editor){
		if(currFile.isPresent()){
			save(currFile.get());
			return;
		}
		selectFileAndSave(editor);
	}

	private void selectFileAndSave(Editor editor){
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

	private void load(Editor editor){
		FileDialog dialog = new FileDialog(this, "Load", FileDialog.LOAD);
		dialog.setFilenameFilter(getFilenameFilter());
		dialog.setVisible(true);
		if(dialog.getFile() == null){
			return;
		}
		File file = new File(dialog.getDirectory(), dialog.getFile());
		load(file, editor);

		this.currFile = Optional.of(file);
		this.tabbedPane.setSelectedComponent(editor);
	}

	private void load(File file, Editor editor){
		try (FileInputStream is = new FileInputStream(file)){
			ObjectInputStream ois = new ObjectInputStream(is);
			editor.load(ois);
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

	private void saveConfig(AppConfig config){
		File configFile = AppStateManager.getStateFile(CONFIG_FILE_NAME);
		if(!configFile.exists()){
            try {
                if(!configFile.createNewFile()){
					throw new RuntimeException("Unable to create config file");
				}
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
		try(FileOutputStream fileOutputStream = new FileOutputStream(configFile)){
			ObjectOutputStream ous = new ObjectOutputStream(fileOutputStream);
			ous.writeObject(config);
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}

	private AppConfig readConfig(){
		File configFile = AppStateManager.getStateFile(CONFIG_FILE_NAME);

		if(!configFile.exists()){
			saveConfig(getDefaultConfig());
			return getDefaultConfig();
		}

		try(FileInputStream fileInputStream = new FileInputStream(configFile)){
			ObjectInputStream ous = new ObjectInputStream(fileInputStream);
			return (AppConfig)ous.readObject();
		}catch(InvalidClassException e){
			e.printStackTrace();
			saveConfig(getDefaultConfig());
			return getDefaultConfig();
		} catch (ClassNotFoundException | IOException e) {
			throw new RuntimeException(e);
		}
    }

	private AppConfig getDefaultConfig(){
		return new AppConfig(EOdeSolver.RungeKutta, DEFAULT_GRAVITY, DEFAULT_BOUNCE_KEEP_PERCENTAGE, DEFAULT_VISCOUS_DRAG, null);
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
		return SCALE.scaleToPixels(v.sub(new Vector2(radius, radius))).toPoint();
	}

    public static void main(String[] args) throws Exception {
    	new App();
    }
}
