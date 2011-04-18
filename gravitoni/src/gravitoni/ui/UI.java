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
import javax.swing.table.AbstractTableModel;

import javax.media.opengl.GLCanvas;
import com.sun.opengl.util.Animator;

/** Main window frame. Contains the GL canvas, renderer, world, settingpane and numeric inspector. */
@SuppressWarnings("serial")
public class UI extends JFrame implements ChangeListener,ActionListener {
	protected GLCanvas canvas;
	protected Animator animator;
	protected Renderer renderer;
	protected World world;
	protected SettingPane settings;
	private JTabbedPane tabukki; // TODO: numeric inspector :P
	private LolModel lollero;
	
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
						System.exit(0);
					}
				}).start();
			}
		});
		
		setVisible(true);
		animator.start();
	}
	
	private void reload(World w) { // TODO: :(
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
		lollero = new LolModel(world);
		JTable tbl = new JTable(lollero);
		tbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tbl.setPreferredScrollableViewportSize(new Dimension(1000, 70));
		JScrollPane panomies = new JScrollPane(tbl);
		detailView.add(panomies);
		
		JPanel quickView = new JPanel();
	    quickView.add(settings, BorderLayout.WEST);
		quickView.add(canvas, BorderLayout.EAST);
		
		JTabbedPane p = new JTabbedPane();
		p.addTab("Full details", detailView);
		p.addTab("Quick view", quickView);
		p.addChangeListener(new Lisnur());
		
		tabukki=p;
		p.setSelectedComponent(quickView);
		add(p);
		
		buildMenu();
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
		addMenuItem(menu, "Quit");
		setJMenuBar(menuBar);
		//JRadioButtonMenuItem rbMenuItem;
		//JCheckBoxMenuItem cbMenuItem;
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
			//setVisible(false);
			//dispose();
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
	
	class Lisnur implements ChangeListener {
		public void stateChanged(ChangeEvent e) {
			if (tabukki.getSelectedIndex() == 0) {
				renderer.pause();
			} else {
				renderer.cont();
				lollero.update();
			}
		}
	}
	
	public void stateChanged(ChangeEvent e) {
		int val = ((JSlider)e.getSource()).getValue();
		double zomg = val / 100.0 - 0.5;
		renderer.setSpeed(zomg);
	}
	public void refreshWidgets() {
		settings.refresh();
	}
}

@SuppressWarnings("serial")
class LolModel extends AbstractTableModel {
	private World world;
	private String[] columns = {"#", "name", "pos.x", "pos.y", "pos.z", "vel.x", "vel.y", "vel.z"};
	
	public LolModel(World world) {
		this.world = world;
	}
	
	public int getColumnCount() {
		return 1 + 1 + 3 + 3;
	}
	
	public String getColumnName(int col) {
		return columns[col];
	}

	@Override
	public int getRowCount() {
		return world.getBodies().size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Body body = world.getBodies().get(rowIndex);
		switch (columnIndex) {
		case 0:
			return rowIndex;
		case 1:
			return body.getName();
		case 2:
		case 3:
		case 4:
			return body.getPos().component(columnIndex - 2);
		case 5:
		case 6:
		case 7:
			return body.getVel().component(columnIndex - 5);
		}
		return "?!";
	}
	public void update() {
		fireTableDataChanged();
	}
}

