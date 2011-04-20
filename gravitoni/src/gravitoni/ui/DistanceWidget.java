package gravitoni.ui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JComboBox;
import javax.swing.JTextField;

import gravitoni.simu.Body;

/** Widget for displaying the distance between two bodies */
@SuppressWarnings("serial")
public class DistanceWidget extends Widget implements ActionListener {
	private Body a = null, b = null;
	private JComboBox boxa;
	private JComboBox boxb;
	private JTextField tf3 = new JTextField();
	private UI ui;
	
	public DistanceWidget(UI ui) {
		this.ui = ui;
		String[] items = new String[ui.world.getBodies().size()];
		int i = 0;
		for (Body b: ui.world.getBodies())
			items[i++] = b.getName(); 
		boxa = new JComboBox(items);
		boxb = new JComboBox(items);
		boxa.addActionListener(this);
		boxb.addActionListener(this);

		setLayout(new GridLayout(3, 1));
		add(boxa);
		add(boxb);
		add(tf3);
	}
	
	public void setBodies(Body a, Body b) {
		if (a != null) this.a = a;
		if (b != null) this.b = b;
		ArrayList<Body> bodies = ui.world.getBodies();
		for (int i = 0; i < bodies.size(); i++) {
			if (bodies.get(i) == a)
				boxa.setSelectedIndex(i);
			if (bodies.get(i) == b)
				boxb.setSelectedIndex(i);				
		}
		update();
	}

	public void actionPerformed(ActionEvent e) {
		a = ui.world.getBodies().get(boxa.getSelectedIndex());
		b = ui.world.getBodies().get(boxb.getSelectedIndex());
		update();
	}

	public void update() {
		if (a != null && b != null) {
			double distance = Math.abs(a.getPos().clone().sub(b.getPos()).len());
			tf3.setText("" + distance);
		}
	}
}
