package gravitoni.ui;

import gravitoni.config.Config;
import gravitoni.gfx.Renderer;
import gravitoni.simu.Body;
import gravitoni.simu.World;

import java.awt.*;
import java.awt.event.*;
import java.io.FileReader;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import javax.media.opengl.GLCanvas;
import com.sun.opengl.util.Animator;

/** Main window frame. Contains the GL canvas, renderer, world, settingpane and numeric inspector. */
@SuppressWarnings("serial")
public class UI extends JFrame implements ActionListener {
	protected GLCanvas canvas;
	protected Animator animator;
	protected Renderer renderer;
	protected World world;
	protected SettingPane settings;
	private JTabbedPane tabPane;
	private DetailView details;
	
	public UI(World world) {
		super("Eippää, behold maailmankaikkeus!");
		this.world = world;
		doit();
	}
	
	private void doit() {
		canvas = new GLCanvas();
		canvas.setPreferredSize(new Dimension(800, 600));
		
		renderer = new Renderer(world, this, canvas);
		
		settings = new SettingPane(this);
		if (world.getBodies().size() >= 2)
			settings.setDistanceBodies(world.getBodies().get(0), world.getBodies().get(1));
		
		insertContents();
		pack();
		
		animator = new Animator(canvas);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				new Thread(new Runnable() {
					public void run() {
						System.out.println("Closing");
						animator.stop();
						world.stop();
						System.exit(0);
					}
				}).start();
			}
		});
		
		setVisible(true);
		animator.start();
	}
	
	private void reload(World w) { // TODO: reload crashes :(
		removeAll();
		world = w;
		doit();
		/*renderer = new Renderer(world, this, canvas);
		removeAll();
		insertContents();
		pack();*/
	}
	
	public SettingPane getSettings() {
		return settings;
	}
	
	/** Build the swing elements */
	private void insertContents() {
		JPanel detailView = new JPanel();
		details = new DetailView(world);
		detailView.add(new JScrollPane(details));
		
		JPanel quickView = new JPanel();
	    quickView.add(settings, BorderLayout.WEST);
		quickView.add(canvas, BorderLayout.EAST);
		
		tabPane = new JTabbedPane();
		tabPane.addTab("Full details", detailView);
		tabPane.addTab("Quick view", quickView);
		tabPane.setSelectedComponent(quickView);
		tabPane.addChangeListener(new TabPaneListener());
		add(tabPane);
		
		buildMenu();
	}
	
	
	class TabPaneListener implements ChangeListener {
		public void stateChanged(ChangeEvent e) {
			if (tabPane.getSelectedIndex() == 0) {
				renderer.pause();
				details.refresh();
			} else {
				renderer.cont();
			}
		}
	}

	private void buildMenu() {
		JMenuBar menuBar;
		JMenu menu;
		menuBar = new JMenuBar();
		menu = new JMenu("File");
		menu.setMnemonic(KeyEvent.VK_F);
		menuBar.add(menu);
		addMenuItem(menu, "Open");
		addMenuItem(menu, "Pause");
		addMenuItem(menu, "Unpause");
		addMenuItem(menu, "New body");
		addMenuItem(menu, "Quit");
		
		/*menu = new JMenu("Bodies");
		menuBar.add(menu);
		addMenuItem(menu, "New body");
		addMenuItem(menu, "Remove body");*/
		setJMenuBar(menuBar);
	}
	
	private void addMenuItem(JMenu menu, String name) {
		JMenuItem item = new JMenuItem(name);
		menu.add(item);
		item.addActionListener(this);
	}
	
	/** For menu bar */
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if ("Quit".equals(cmd)) {
			WindowEvent wev = new WindowEvent(this, WindowEvent.WINDOW_CLOSING);
			Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(wev);
		} else if ("Pause".equals(cmd)) {
			renderer.pause();
		} else if ("Unpause".equals(cmd)) {
			renderer.cont();
		} else if ("Open".equals(cmd)) {
			JFileChooser fc = new JFileChooser();
			if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				openConfig(fc.getSelectedFile().getAbsolutePath());
			}
		} else if ("New body".equals(cmd)) {
			tabPane.setSelectedIndex(0);
			int i = 1;
			while (world.getBody("newbody" + i) != null) i++;
			world.getBodies().add(new Body(Config.fromString("name newbody" + i)));
			details.refresh();
		}
	}
	
	private void openConfig(String file) {
		FileReader r;
		try {
			r = new FileReader(file);
		} catch (Exception e) {
			System.out.println("Woop, file opening error");
			e.printStackTrace();
			return;
		}
		World newWorld = new World();
		Config cfg = new Config("(main)", r);
		newWorld.loadConfig(cfg);
		reload(newWorld);
	}
	
	public void refreshWidgets() {
		settings.refresh();
	}
	
	public Renderer getRenderer() {
		return renderer;
	}
	
	public World getWorld() {
		return world;
	}
}
