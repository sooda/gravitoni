package gravitoni.ui;

import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/** Widget for going forwards and backwards in time. */
@SuppressWarnings("serial")
public class TimeWidget extends Widget implements ChangeListener, ItemListener {
	private UI ui;
	private JSlider speedSlider, timeSlider;
	private JCheckBox pauseBox;
	private JTextField timeBox;
	
	public TimeWidget(UI ui) {
		this.ui = ui;
		buildContents();
	}
	
	private void buildContents() {
		setLayout(new GridLayout(4, 1));
		
		pauseBox = new JCheckBox("Paused");
		pauseBox.addItemListener(this);
		add(pauseBox);
		
		timeBox = new JTextField("0");
		add(timeBox);
		
	    speedSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);
	    speedSlider.setMajorTickSpacing(20);
	    speedSlider.setMinorTickSpacing(1);
	    speedSlider.setPaintTicks(true);
	    speedSlider.setPaintLabels(true);
	    speedSlider.addChangeListener(this);
	    add(speedSlider);
	    
	    timeSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 100);
	    timeSlider.setEnabled(false);
	    timeSlider.setMajorTickSpacing(20);
	    timeSlider.setMinorTickSpacing(1);
	    timeSlider.setPaintTicks(true);
	    timeSlider.setPaintLabels(true);
	    timeSlider.addChangeListener(this);
	    add(timeSlider);
	}
	
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == speedSlider) {
			int val = speedSlider.getValue();
			ui.getRenderer().setSpeed(val / 50.0 - 1);
		} else if (e.getSource() == timeSlider) {
			ui.getRenderer().setTime(timeSlider.getValue());
		}
	}

	public void itemStateChanged(ItemEvent e) {
		boolean paused = e.getStateChange() != ItemEvent.DESELECTED;
		ui.getRenderer().setPause(paused);
		doPause(paused);
	}
	
	public void setPause(boolean pause) {
		pauseBox.setSelected(pause);
		doPause(pause);
	}
	private void doPause(boolean pause) {
		speedSlider.setEnabled(!pause);
		timeSlider.setEnabled(pause);
		if (!pause) timeSlider.setValue(100);
	}
	
	public void update() {
		double seconds = ui.getWorld().getTime();
		String s;
		if (seconds < 60) {
			s = seconds + "s";
		} else if (seconds < 60*60) {
			s = seconds / 60 + "m";
		} else if (seconds < 60*60*24) {
			s = seconds / (60*60) + "h";
		} else if (seconds < 60*60*24*365) {
			s = seconds / (60*60*24) + "d";
		} else {
			s = seconds / (60*60*24*365) + "y";
		}
		timeBox.setText(s);
	}
}
