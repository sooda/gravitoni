package gravitoni.ui;

import javax.swing.JPanel;

/** Base class for the widgets */
@SuppressWarnings("serial")
public abstract class Widget extends JPanel {
	public abstract void update();
}
