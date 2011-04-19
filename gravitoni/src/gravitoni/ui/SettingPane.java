package gravitoni.ui;

import gravitoni.simu.Body;

import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

@SuppressWarnings("serial")
public class SettingPane extends JPanel {
	private UI ui;
	private ArrayList<Widget> widgets; 
	private BodyWidget bw;
	private TimeWidget tw;
	private DistanceWidget dw;
	
	
	public SettingPane(UI ui) {
		super(new GridLayout(0, 1));
		this.ui = ui;
		widgets = new ArrayList<Widget>();
		insertContents();
	}
	private void insertContents() {
	    setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 5));
	    
	    bw = new BodyWidget(ui);
	    widgets.add(bw);
	    add(bw);
	    add(new JSeparator(SwingConstants.HORIZONTAL));
	    
	    tw = new TimeWidget(ui);
	    widgets.add(tw);
	    add(tw);
	    add(new JSeparator(SwingConstants.HORIZONTAL));
	    
	    dw = new DistanceWidget(ui);
	    widgets.add(dw);
	    add(dw);
	    add(new JSeparator(SwingConstants.HORIZONTAL));
	}
	public void updateDistance() {
		dw.update();
	}
	public void setDistanceBodies(Body a, Body b) {
		dw.setBodies(a, b);
	}
	public void setSelected(Body b) {
		bw.setSelected(b);
	}
	public void refresh() {
		for (Widget w: widgets) {
			w.update();
		}
	}
	public void setPause(boolean pause) {
		tw.setPause(pause);
	}
	public void setPause() {
		setPause(true);
	}
}
