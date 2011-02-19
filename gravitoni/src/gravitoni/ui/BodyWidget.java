package gravitoni.ui;

import java.awt.Button;

import javax.swing.JPanel;

public class BodyWidget extends JPanel {
	private UI ui;
	
	public BodyWidget(UI ui) {
		this.ui = ui;
		buildContents();
	}
	
	private void buildContents() {
	    add(new Button("Yks"));
	    add(new Button("kakx"));
	    add(new Button("coo'lme"));
	}
}
