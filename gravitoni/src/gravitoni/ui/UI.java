package gravitoni.ui;

import gravitoni.gfx.Renderer;
import gravitoni.simu.Body;
import gravitoni.simu.World;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;

import javax.media.opengl.GLCanvas;
import com.sun.opengl.util.Animator;

@SuppressWarnings("serial")
public class UI extends JFrame implements ChangeListener {
	protected GLCanvas canvas;
	protected Animator animator;
	protected Renderer renderer;
	protected World world;
	protected SettingPane settings;
	private JTabbedPane tabukki;
	private LolModel lollero;
	
	public UI(World world) {
		super("Eippää, behold maailmankaikkeus!");
		this.world = world;
		canvas = new GLCanvas();
		canvas.setPreferredSize(new Dimension(640, 480));
		
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
	                animator.stop();
	                System.exit(0);
	              }
	            }).start();
	        }
	      });
		
		setVisible(true);
		animator.start();
	}
	
	private void insertContents() {
		JPanel detailView = new JPanel();
		//detailView.add(new JLabel("Sinep"), BorderLayout.NORTH);
		lollero=new LolModel(world);
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
		
	//public void start() {
	//}
	
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

