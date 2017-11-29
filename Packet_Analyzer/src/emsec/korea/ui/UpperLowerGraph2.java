package emsec.korea.ui;
import ChartDirector.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class UpperLowerGraph2 {
	    
	private Date[] timeStamps = null;
	private Date[] realTimes = null;
    private double[] UpperY = null;
    private double[] dataSeries = null;
    private double[] LowerY = null;
    private Date[] zoneX = null;
    
	private ChartViewer chartViewer1 = null;
	
	private javax.swing.Timer chartUpdateTimer = null;
	
	private JFrame innerframe = null;
	private int width=0, height=0; 
	private int interval = 0;
	
	private Date nextDataTime;

	public UpperLowerGraph2(JFrame fr, int x, int y, int width, int height, int draw_interval)
	{
		this.innerframe = fr;
		this.width = width;
		this.height = height;
		
		this.interval = draw_interval;
		
		
		timeStamps = new Date[20000/draw_interval];
		realTimes = new Date[20000/draw_interval];
	    dataSeries = new double[20000/draw_interval];
	    UpperY = new double[2];
	    LowerY = new double[2];
	    zoneX = new Date[2];
		
	    nextDataTime = new Date(0);
	    
		chartViewer1 = new ChartViewer();
        chartViewer1.setBackground(new Color(255, 255, 255));
        chartViewer1.setOpaque(true);
        chartViewer1.setHorizontalAlignment(SwingConstants.CENTER);
        
        chartViewer1.addViewPortListener(new ViewPortAdapter() {
            public void viewPortChanged(ViewPortChangedEvent e) {
            	drawChart(chartViewer1);
            }
        });
        
        chartViewer1.addTrackCursorListener(new TrackCursorAdapter() {
			public void mouseMovedPlotArea(MouseEvent e) {
				chartViewer1_MouseMovedPlotArea(e);
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
	
	public void updateData(Double upper, Double current, Double lower, long timestamp)
	{
		this.UpperY[0] = this.UpperY[1] = upper;
		this.LowerY[0] = this.LowerY[1] = lower;
        shiftData(dataSeries, current);
        shiftData(timeStamps, nextDataTime);
        nextDataTime = new Date(nextDataTime.getTime() + interval);
        
        shiftData(realTimes, new Date(timestamp));
	}
	
	private void shiftData(double[] data, double newValue)
    {
        for (int i = 1; i < data.length; ++i)
            data[i - 1] = data[i];
        data[data.length - 1] = newValue;
    }
	
	private void shiftData(Date[] data, Date newValue)
    {
        for (int i = 1; i < data.length; ++i)
            data[i - 1] = data[i];
        data[data.length - 1] = newValue;
    }

	
	private void drawChart(ChartViewer viewer)
    {
        XYChart c = new XYChart(this.width, this.height, 0xf4f4f4, 0x000000, 1);
        c.setRoundedFrame();
        c.recycle(viewer.getChart());
        c.setPlotArea(55, 62, this.width-70, this.height-100, 0xffffff, -1, -1, 0xcccccc, 0xcccccc);
        c.setClipping();
        c.addTitle("CAN ID Sequence Monitor", "Times New Roman Bold Italic", 13).setBackground(0xdddddd, 0x000000, Chart.glassEffect());

        LegendBox b = c.addLegend2(55, 33, 3, "Arial Bold", 9);
        b.setBackground(Chart.Transparent, Chart.Transparent);
        b.setWidth(this.width-100);

        c.yAxis().setTitle("Sequence Distance", "Arial Bold", 11);
        c.xAxis().setTickDensity(75, 15);
        c.xAxis().setWidth(2);
        c.yAxis().setWidth(2);

        Date lastTime = timeStamps[timeStamps.length - 1];
        if (lastTime != null)
        {
            c.xAxis().setDateScale(new Date(lastTime.getTime() - this.interval * timeStamps.length), lastTime);
            c.xAxis().setLabelFormat("");
            
            LineLayer datalayer = c.addLineLayer(dataSeries, 0x800080, "Sequence Distance");//  c.addLineLayer2();
            datalayer.setXData(timeStamps);
            datalayer.setLineWidth(2);           

            LineLayer lineLayer = c.addLineLayer2();
            lineLayer.addDataSet(UpperY, 0x338033, "Similar Zone");
            lineLayer.addDataSet(LowerY, 0x338033);
            
            zoneX[0] = timeStamps[0];
            
            for(int i=0;i<timeStamps.length;i++)
            {
            	if(timeStamps[i] != null)
            	{
            		zoneX[0] = timeStamps[i];
            		break;
            	}
            }
            
            zoneX[1] = lastTime;
            lineLayer.setXData(zoneX);
            lineLayer.setLineWidth(2);
            
            c.addInterLineLayer(lineLayer.getLine(0), lineLayer.getLine(1), 0x8099ff99, 0x8099ff99);
            c.addInterLineLayer(datalayer.getLine(0), lineLayer.getLine(0), 0xff0000, Chart.Transparent);
            c.addInterLineLayer(datalayer.getLine(0), lineLayer.getLine(1), Chart.Transparent, 0x0000ff);
        }

        chartViewer1.setChart(c);
    }
	
	private void chartViewer1_MouseMovedPlotArea(MouseEvent e)
    {
		try {
			ChartViewer viewer = (ChartViewer)e.getSource();
			trackLineAxis((XYChart)viewer.getChart(), viewer.getPlotAreaMouseX());
			viewer.updateDisplay();
			viewer.removeDynamicLayer("MouseExitedPlotArea");
		}
		catch (Exception exc)
		{
			
		}
    }
	
	private void trackLineAxis(XYChart c, int mouseX)
    {
        DrawArea d = c.initDynamicLayer();
        PlotArea plotArea = c.getPlotArea();
        double xValue = c.getNearestXValue(mouseX);
        int xCoor = c.getXCoor(xValue);
        int minY = plotArea.getBottomY();
        int xIndex = 0;
        
        for (int i = 0; i < c.getLayerCount(); ++i)
        {
            Layer layer = c.getLayerByZ(i);
            xIndex = layer.getXIndexOf(xValue);

            for (int j = 0; j < layer.getDataSetCount(); ++j) {
                ChartDirector.DataSet dataSet = layer.getDataSetByZ(j);

                double dataPoint = dataSet.getPosition(xIndex);
                if ((dataPoint != Chart.NoValue) && (dataSet.getDataColor() != Chart.Transparent))
                {
                    minY = Math.min(minY, c.getYCoor(dataPoint, dataSet.getUseYAxis()));
                }
            }
        }

        d.vline(Math.max(minY, plotArea.getTopY()), plotArea.getBottomY() + 6, xCoor, d.dashLineColor(0x000000, 0x0101));
        d.text("<*font,bgColor=000000*> " + realTimes[xIndex].getTime() + " <*/font*>", "Arial Bold", 8).draw(xCoor, plotArea.getBottomY() + 6, 0xffffff, Chart.Top);

        for (int i = 0; i < c.getLayerCount(); ++i) {
            Layer layer = c.getLayerByZ(i);
            xIndex = layer.getXIndexOf(xValue);

            for (int j = 0; j < layer.getDataSetCount(); ++j) {
                ChartDirector.DataSet dataSet = layer.getDataSetByZ(j);
                double dataPoint = dataSet.getPosition(xIndex);
                Axis yAxis = dataSet.getUseYAxis();
                int yCoor = c.getYCoor(dataPoint, yAxis);
                int color = dataSet.getDataColor();

                if ((dataPoint != Chart.NoValue) && (color != Chart.Transparent) && (yCoor >= plotArea.getTopY()) && (yCoor <= plotArea.getBottomY()))
                {
                    int xPos = yAxis.getX() + ((yAxis.getAlignment() == Chart.Left) ? -4 : 4);
                    d.hline(xCoor, xPos, yCoor, d.dashLineColor(color, 0x0101));
                    d.circle(xCoor, yCoor, 4, 4, color, color);
                    d.text("<*font,bgColor=" + Integer.toHexString(color) + "*> " + c.formatValue(dataPoint, "{value|P4}") + " <*/font*>", "Arial Bold", 8).draw(xPos, yCoor, 0xffffff, ((yAxis.getAlignment() == Chart.Left) ? Chart.Right : Chart.Left));
                    
                }
            }
        }
    }
}
