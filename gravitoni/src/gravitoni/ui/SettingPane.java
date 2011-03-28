package gravitoni.ui;

import gravitoni.simu.Body;

import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class SettingPane extends JPanel {
	private UI ui;
	private ArrayList<Widget> widgets; 
	
	
	public SettingPane(UI ui) {
		super(new GridLayout(0, 1));
		this.ui = ui;
		widgets = new ArrayList<Widget>();
		insertContents();
	}
	private void insertContents() {
	    setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 5));
	    Widget pane;
	    
	    pane = new BodyWidget(ui);
	    widgets.add(pane);
	    add(pane);
	    
	    pane = new TimeWidget(ui);
	    widgets.add(pane);
	    add(pane);
	    
	    pane = new DistanceWidget(ui);
	    widgets.add(pane);
	    add(pane);
	}
	public void updateDistance() {
		for (Widget pane : widgets) {
			if (pane instanceof DistanceWidget)
				((DistanceWidget)pane).update();
		}
	}
	public void setDistanceBodies(Body a, Body b) {
		for (Widget pane : widgets) {
			if (pane instanceof DistanceWidget)
				((DistanceWidget)pane).setBodies(a, b);
		}
	}
	public void refresh() {
		for (Widget w: widgets) {
			w.update();
		}
	}
}