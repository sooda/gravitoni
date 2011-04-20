package gravitoni.ui;

import java.awt.GridLayout;

import javax.swing.JTextField;

import gravitoni.simu.Body;

/** Widget for displaying the distance between two bodies */
@SuppressWarnings("serial")
public class DistanceWidget extends Widget {
	private Body a = null, b = null;
	private JTextField tf1 = new JTextField("Rai rai");
	private JTextField tf2 = new JTextField("Rai rai");
	private JTextField tf3 = new JTextField("Rai rai");
	//private UI ui;
	
	public DistanceWidget(UI ui) {
		//this.ui = ui;
		setLayout(new GridLayout(3, 1));
		add(tf1);
		add(tf2);
		add(tf3);
		tf1.setEditable(false);
		tf2.setEditable(false);
	}
	public void setBodies(Body a, Body b) {
		this.a = a;
		this.b = b;
		tf1.setText(a.getName());
		tf2.setText(b.getName());
	}
	public void update() {
		if (a != null && b != null) {
			double distance = Math.abs(a.getPos().clone().sub(b.getPos()).len());
			tf3.setText("" + distance);
		}
	}
}
