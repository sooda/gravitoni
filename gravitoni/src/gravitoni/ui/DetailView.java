package gravitoni.ui;

import gravitoni.simu.Body;
import gravitoni.simu.Vec3;
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

	public int getRowCount() {
		return world.getBodies().size();
	}

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
	
	public boolean isCellEditable(int row, int col) {
		return col > 0;
	}
	
	public void setValueAt(Object value, int row, int col) {
		switch (col) {
		case 1:
			world.getBodies().get(row).setName((String)value);
			break;
		case 2:
		case 3:
		case 4:
			double xyz[] = { (Double)getValueAt(row, 2), (Double)getValueAt(row, 3), (Double)getValueAt(row, 4) };
			try {
				xyz[col - 2] = Double.parseDouble((String)value);
			} catch (NumberFormatException e) {
				return;
			}
			world.getBodies().get(row).setPos(new Vec3(xyz[0], xyz[1], xyz[2]));
			break;
		case 5:
		case 6:
		case 7:
			double xyzv[] = { (Double)getValueAt(row, 5), (Double)getValueAt(row, 6), (Double)getValueAt(row, 7) };
			try {
				xyzv[col - 5] = Double.parseDouble((String)value);
			} catch (NumberFormatException e) {
				return;
			}
			world.getBodies().get(row).setVel(new Vec3(xyzv[0], xyzv[1], xyzv[2]));
			break;
		}
		fireTableCellUpdated(row, col);
	}
}

