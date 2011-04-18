package gravitoni.ui;


import javax.swing.JSlider;

@SuppressWarnings("serial")
public class TimeWidget extends Widget {
	private UI ui;
	
	public TimeWidget(UI ui) {
		this.ui = ui;
		buildContents();
	}
	
	private void buildContents() {
	    JSlider slider = new JSlider(JSlider.HORIZONTAL, 0, 100, 80);
	    slider.setMajorTickSpacing(10);
	    slider.setMinorTickSpacing(1);
	    slider.setPaintTicks(true);
	    slider.addChangeListener(ui);
	    add(slider);
	}

	@Override
	public void update() {
		// TODO Auto-generated method stub
		
	}

}
