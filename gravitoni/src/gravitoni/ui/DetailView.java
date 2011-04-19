package gravitoni.ui;

import gravitoni.simu.Body;
import gravitoni.simu.World;

import java.awt.Dimension;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

@SuppressWarnings("serial")
public class DetailView extends JTable {
	private World world;
	private WorldTableModel model;
	
	public DetailView(World w) {
		world = w;
		model = new WorldTableModel(world);
		setModel(model);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setPreferredScrollableViewportSize(new Dimension(1000, 600));
	}
	
	public void refresh() {
		model.update();
	}
}
@SuppressWarnings("serial")
class WorldTableModel extends AbstractTableModel {
	private World world;
	private String[] columns = {"#", "name", "pos.x", "pos.y", "pos.z", "vel.x", "vel.y", "vel.z"};
	
	public WorldTableModel(World world) {
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

