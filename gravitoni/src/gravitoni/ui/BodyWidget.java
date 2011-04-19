package gravitoni.ui;

import gravitoni.simu.Body;
import gravitoni.simu.Vec3;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComboBox;
import javax.swing.JTextField;


/** Widget for displaying some basic information about bodies */
@SuppressWarnings("serial")
public class BodyWidget extends Widget implements ActionListener {
	private UI ui;
	private JTextField tfx, tfy, tfz;
	private JComboBox boks;
	
	public BodyWidget(UI ui) {
		this.ui = ui;
		buildContents();
	}
	
	private void buildContents() {
		String[] items = new String[ui.world.getBodies().size()];
		int i = 0;
		for (Body b: ui.world.getBodies())
			items[i++] = b.getName(); 
		setLayout(new GridLayout(4, 1));
		boks = new JComboBox(items);
		boks.addActionListener(this);
		add(boks);
		tfx = new JTextField();
		tfx.setPreferredSize(new Dimension(100, 20));
		add(tfx);
		tfy = new JTextField();
		tfy.setPreferredSize(new Dimension(100, 20));
		add(tfy);
		tfz = new JTextField();
		tfz.setPreferredSize(new Dimension(100, 20));
		add(tfz);
	}

	public void actionPerformed(ActionEvent e) {
		System.out.println("No terve");
		update();
	}
	public void update() {
		int i = boks.getSelectedIndex();
		Body b = ui.world.getBodies().get(i);
		Vec3 pos = b.getPos();
		tfx.setText("" + pos.x);
		tfy.setText("" + pos.y);
		tfz.setText("" + pos.z);
	}
	
	public void setSelected(Body body) {
		int i = 0;
		for (Body b: ui.world.getBodies()) {
			if (b == body) {
				boks.setSelectedIndex(i);
				break;
			}
			i++;
		}
	}
}
