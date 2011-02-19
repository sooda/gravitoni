package gravitoni.ui;

import java.awt.Button;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JSlider;

@SuppressWarnings("serial")
public class SettingPane extends JPanel {
	private UI ui;
	private ArrayList<JPanel> widgets; 
	
	
	public SettingPane(UI ui) {
		super(new GridLayout(3, 1));
		this.ui = ui;
		widgets = new ArrayList<JPanel>();
		insertContents();
	}
	private void insertContents() {
	    setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 5));
	    JPanel pane;
	    
	    pane = new BodyWidget(ui);
	    widgets.add(pane);
	    add(pane);
	    
	    pane = new TimeWidget(ui);
	    widgets.add(pane);
	    add(pane);
	}
}
