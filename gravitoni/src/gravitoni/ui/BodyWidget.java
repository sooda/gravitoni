package gravitoni.ui;

import gravitoni.simu.Body;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComboBox;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class BodyWidget extends Widget implements ActionListener {
	private UI ui;
	private JTextField tf;
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
		boks = new JComboBox(items);
		boks.addActionListener(this);
		add(boks);
		tf=new JTextField("Moi");
		tf.setPreferredSize(new Dimension(100, 20));
		add(tf);
	}

	public void actionPerformed(ActionEvent e) {
		System.out.println("Moroooo");
		update();
	}
	public void update() {
		int i = boks.getSelectedIndex();
		Body b = ui.world.getBodies().get(i);
		tf.setText(b.getName() + ": " + b.getPos());
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
