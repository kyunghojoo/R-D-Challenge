package emsec.korea.ui;
import ChartDirector.*;
import javax.swing.*;
import java.awt.event.*;

public class BarChart {
	        
	private ChartViewer chartViewer1 = null;
	
	private javax.swing.Timer chartUpdateTimer = null;
	
	private JFrame innerframe = null;
	private int width=0, height=0; 
	private int interval = 0;
	
	private double value = 0;
	private int max = 0, tick = 0, index = 0;
	private String text = null;

	public BarChart(JFrame fr, int x, int y, int width, int height, int draw_interval, int max, int tick, int chartIndex, String text)
	{
		this.innerframe = fr;
		this.width = width;
		this.height = height;
		
		this.interval = draw_interval;
		
		this.max = max;
		this.tick = tick;
		this.index = chartIndex;
		this.text = text;
		
		chartViewer1 = new ChartViewer();
        
        chartViewer1.addViewPortListener(new ViewPortAdapter() {
            public void viewPortChanged(ViewPortChangedEvent e) {
            	drawChart(chartViewer1);
            }
        });
        
        chartUpdateTimer = new javax.swing.Timer(this.interval, new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                	chartViewer1.updateViewPort(true, false);
                }
            });
		
        chartViewer1.setBounds(x, y, width, height);
        drawChart(chartViewer1);
        this.innerframe.getContentPane().add(chartViewer1);
	}
	
	public void startDraw()
	{
		chartUpdateTimer.start();
	}
	
	public void stopDraw()
	{
		chartUpdateTimer.stop();
	}
	
	public void updateData(Double value)
	{
		this.value = value;
	}
	
	private void drawChart(ChartViewer viewer)
    {
		LinearMeter m = new LinearMeter(width, height, 0xeeeeee, 0xaaaaaa);
		m.setRoundedFrame(Chart.Transparent);
        m.setThickFrame(3);
		
        m.setMeter(18, 24, width-40, height-60, Chart.Top);
        m.setScale(0, max, tick);
		
        double[] smoothColorScale = {0, 0x0000ff, max*0.25, 0x0088ff, max*0.50, 0x00ff00, max*0.75, 0xdddd00, max, 0xff0000};
		
        if (index == 0) {
            m.addBar(0, value, 0x0088ff, Chart.glassEffect(Chart.NormalGlare, Chart.Top), 4);
            m.addColorScale(smoothColorScale, 48, 5);
        }
        else if (index == 1)
        {
            m.addBar(0, value, 0xee3333, Chart.glassEffect(Chart.NormalGlare, Chart.Top), 4);
            m.addColorScale(smoothColorScale, 48, 5);
        }
        
        m.addText(width-15, height-15, text, "Arial Bold", 8, Chart.TextColor, Chart.Right);
		
        TextBox t = m.addText(18, 65, m.formatValue(value, "2"), "Arial", 8, 0xffffff, Chart.Left);
        t.setBackground(0x000000, 0x000000, -1);
        t.setRoundedCorners(3);

        chartViewer1.setChart(m);
    }
}
