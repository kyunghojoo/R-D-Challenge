package emsec.korea.ui;

import javax.swing.JFrame;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;

import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.awt.event.ActionEvent;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;

import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.metal.MetalCheckBoxIcon;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.DefaultCaret;

import emsec.korea.Monitor;
import emsec.korea.data_structure.AnomalObject;
import emsec.korea.data_structure.DataObject;
import emsec.korea.data_structure.FeatureObject;
import emsec.korea.data_structure.ShowObject;
import emsec.korea.data_structure.TotalFeaturesObject;
import emsec.korea.feature_operator.AnomalDetector;
import emsec.korea.feature_operator.Offline_Analyzer;
import emsec.korea.feature_operator.modules.F2_Bus_Load;
import emsec.korea.ui.MainFrame_1080p.DetectWindow.offAnalyzer_Monitor;

public class MainFrame_1080p {

	public JFrame frame;

	private static JButton btnStart = null;
	private static JButton btnPause = null;

	private static JTextArea CanRx = null;
	private static JScrollPane scrollPane = null;

	private JScrollPane scrollPane2 = null;
	private DefaultTableModel CanModel = null;
	private JTable CanTable = null;
	private JCheckBox ascending = null;

	private Monitor m = null;

	int count = 0;

	private JLabel label1 = null;
	private JLabel label2 = null;
	private JLabel label3 = null;
	private JLabel label4 = null;

	private JLabel label_normalstate = null;
	private JLabel label_current = null;

	private static JLabel UpBusLoad = null;
	private static JLabel UpNumOfMessage = null;
	private static JLabel UpNumOfIDs = null;
	private static JLabel UpSelfsimilarity = null;

	private static JLabel UpBusLoad_normal = null;
	private static JLabel UpNumOfMessage_normal = null;
	private static JLabel UpNumOfIDs_normal = null;
	private static JLabel UpSelfsimilarity_normal = null;

	private static final int refresh_freq = 1000;
	private static final int fast_refresh_freq = 50;

	private static UpperLowerGraph busload_graph = null;
	private static BarChart numOfMsg_graph = null;
	private static BarChart numOfIDs_graph = null;
	private static UpperLowerGraph2 id_similarity_graph = null;

	private JLabel CurMonitor_LBL = null;
	public static volatile boolean suspicious = false;
	private static volatile byte current_condition = 0;
	private ReadOnlyCheckBox CurMonitor_Box = null;
	private BufferedImage timage = null;
	private BufferedImage fimage = null;
	private String normal_str = "          Normal";
	private String abnormal_str = "          Abnormal";
	private JLabel TotalAttacks = null;
	private JLabel TotalErrors = null;

	private static String s1080p_colnames[] = { "CAN ID", "Frequency (ms)", "Elapsed Time", "Count", "DLC", "Data",
			"TimeStamp" };

	private static volatile boolean going = false;

	private static boolean onlineMode = false;
	private static String DatasetFileLoc = null;

	public static void setOnlineMode(boolean on) {
		onlineMode = on;
	}

	public static void setDatasetFile(String loc) {
		DatasetFileLoc = loc;
	}

	private volatile Offline_Analyzer offanalyzer = null;
	private offAnalyzer_Monitor offanalyzer_monitor = null;

	public MainFrame_1080p() {
		initialize();
	}
	
	public static void setSuspicious(byte obj_id)
	{
		current_condition |= obj_id;
		
		MainFrame_1080p.suspicious = true;
	}
	
	public static void releaseSuspicious(byte obj_id)
	{
		current_condition &= ~obj_id;
		
		if(current_condition == 0x00)
			MainFrame_1080p.suspicious = false;
	}
	
	public static invalidID_Noticer invID_noti = null;
	
	public class invalidID_Noticer {
		private JFrame noticer_frame = new JFrame("Invalid ID Appeared");
		
		private JTextArea CanRx = null;
		private JScrollPane scrollPane = null;
		
		public invalidID_Noticer()
		{
			Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
			
			noticer_frame = new JFrame("Invalid ID Appeared");
			noticer_frame.setVisible(false);
			noticer_frame.setSize(600, 850);
			noticer_frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
			noticer_frame.setResizable(false);

			Dimension f1_size = noticer_frame.getSize();
			int left = (screen.width / 2) - (f1_size.width / 2);
			int top = (screen.height / 2) - (f1_size.height / 2);
			noticer_frame.setLocation(left, top);
			noticer_frame.getContentPane().setLayout(null);
			
			CanRx = new JTextArea();
			CanRx.setEditable(false);

			scrollPane = new JScrollPane(CanRx);
			scrollPane.setBounds(5, 10, 585, 805);
			noticer_frame.getContentPane().add(scrollPane);
			
			DefaultCaret caret = (DefaultCaret) CanRx.getCaret();
			caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		}
		
		public void updateCanRxField(String str) {
			CanRx.append(str + "\n");
		}

		public void clearCanRxField() {
			CanRx.setText("");
		}
		
		public void hideframe()
		{
			noticer_frame.setVisible(false);
		}
		
		public void showframe()
		{
			if( !noticer_frame.isVisible() )
				noticer_frame.setVisible(true);
		}
	}

	private void initialize() {
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		EtchedBorder eborder = new EtchedBorder(EtchedBorder.RAISED);
		
		invID_noti = new invalidID_Noticer();
		invID_noti.hideframe();

		frame = new JFrame("Packet Analyzer");
		frame.setSize(1700, 950);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		frame.setResizable(false);

		Dimension f1_size = frame.getSize();
		int left = (screen.width / 2) - (f1_size.width / 2);
		int top = (screen.height / 2) - (f1_size.height / 2);
		frame.setLocation(left, top);

		JLabel lblCurrentNetworkCondition = new JLabel("Network Condition");
		lblCurrentNetworkCondition.setHorizontalAlignment(SwingConstants.CENTER);
		lblCurrentNetworkCondition.setFont(new Font("Serif", Font.BOLD, 23));
		lblCurrentNetworkCondition.setBounds(1050, 15, 625, 30);
		frame.getContentPane().add(lblCurrentNetworkCondition);

		busload_graph = new UpperLowerGraph(frame, 1100, 50, 550, 250, fast_refresh_freq);

		label_normalstate = new JLabel("Normal");
		label_normalstate.setBounds(1325, 300, 150, 20);
		label_normalstate.setFont(new Font("Serif", Font.BOLD, 11));
		label_normalstate.setHorizontalAlignment(SwingConstants.CENTER);
		frame.getContentPane().add(label_normalstate);

		label_current = new JLabel("Current");
		label_current.setBounds(1425, 300, 150, 20);
		label_current.setFont(new Font("Serif", Font.BOLD, 11));
		label_current.setHorizontalAlignment(SwingConstants.CENTER);
		frame.getContentPane().add(label_current);

		label1 = new JLabel("Bus Load");
		label1.setBounds(1200, 320, 150, 20);
		label1.setFont(new Font("Serif", Font.BOLD, 15));
		label1.setBorder(eborder);
		label1.setHorizontalAlignment(SwingConstants.CENTER);
		frame.getContentPane().add(label1);

		UpBusLoad_normal = new JLabel("");
		UpBusLoad_normal.setBounds(1350, 320, 100, 20);
		UpBusLoad_normal.setFont(new Font("Serif", Font.BOLD, 15));
		UpBusLoad_normal.setBorder(eborder);
		UpBusLoad_normal.setHorizontalAlignment(SwingConstants.CENTER);
		UpBusLoad_normal.setBackground(Color.white);
		frame.getContentPane().add(UpBusLoad_normal);

		UpBusLoad = new JLabel("");
		UpBusLoad.setBounds(1450, 320, 100, 20);
		UpBusLoad.setFont(new Font("Serif", Font.BOLD, 15));
		UpBusLoad.setBorder(eborder);
		UpBusLoad.setHorizontalAlignment(SwingConstants.CENTER);
		UpBusLoad.setBackground(Color.white);
		frame.getContentPane().add(UpBusLoad);

		numOfMsg_graph = new BarChart(frame, 1100, 350, 260, 80, fast_refresh_freq, 4000, 500, 0, "Number of Message");

		label2 = new JLabel("Num of Msg");
		label2.setBounds(1100, 440, 100, 20);
		label2.setFont(new Font("Serif", Font.BOLD, 15));
		label2.setBorder(eborder);
		label2.setHorizontalAlignment(SwingConstants.CENTER);
		frame.getContentPane().add(label2);

		UpNumOfMessage_normal = new JLabel("");
		UpNumOfMessage_normal.setBounds(1200, 440, 80, 20);
		UpNumOfMessage_normal.setFont(new Font("Serif", Font.BOLD, 15));
		UpNumOfMessage_normal.setBorder(eborder);
		UpNumOfMessage_normal.setHorizontalAlignment(SwingConstants.CENTER);
		UpNumOfMessage_normal.setBackground(Color.white);
		frame.getContentPane().add(UpNumOfMessage_normal);

		UpNumOfMessage = new JLabel("");
		UpNumOfMessage.setBounds(1280, 440, 80, 20);
		UpNumOfMessage.setFont(new Font("Serif", Font.BOLD, 15));
		UpNumOfMessage.setBorder(eborder);
		UpNumOfMessage.setHorizontalAlignment(SwingConstants.CENTER);
		UpNumOfMessage.setBackground(Color.white);
		frame.getContentPane().add(UpNumOfMessage);

		numOfIDs_graph = new BarChart(frame, 1390, 350, 260, 80, fast_refresh_freq, 90, 10, 1, "Number of CAN IDs");

		label3 = new JLabel("Num of CAN IDs");
		label3.setBounds(1390, 440, 100, 20);
		label3.setFont(new Font("Serif", Font.BOLD, 13));
		label3.setBorder(eborder);
		label3.setHorizontalAlignment(SwingConstants.CENTER);
		frame.getContentPane().add(label3);

		UpNumOfIDs_normal = new JLabel("");
		UpNumOfIDs_normal.setBounds(1490, 440, 80, 20);
		UpNumOfIDs_normal.setFont(new Font("Serif", Font.BOLD, 15));
		UpNumOfIDs_normal.setBorder(eborder);
		UpNumOfIDs_normal.setHorizontalAlignment(SwingConstants.CENTER);
		UpNumOfIDs_normal.setBackground(Color.white);
		frame.getContentPane().add(UpNumOfIDs_normal);

		UpNumOfIDs = new JLabel("");
		UpNumOfIDs.setBounds(1570, 440, 80, 20);
		UpNumOfIDs.setFont(new Font("Serif", Font.BOLD, 15));
		UpNumOfIDs.setBorder(eborder);
		UpNumOfIDs.setHorizontalAlignment(SwingConstants.CENTER);
		UpNumOfIDs.setBackground(Color.white);
		frame.getContentPane().add(UpNumOfIDs);

		id_similarity_graph = new UpperLowerGraph2(frame, 1100, 470, 550, 200, fast_refresh_freq);

		label4 = new JLabel("Sequence Distance");
		label4.setBounds(1200, 670, 150, 20);
		label4.setFont(new Font("Serif", Font.BOLD, 15));
		label4.setBorder(eborder);
		label4.setHorizontalAlignment(SwingConstants.CENTER);
		frame.getContentPane().add(label4);

		UpSelfsimilarity_normal = new JLabel("");
		UpSelfsimilarity_normal.setBounds(1350, 670, 100, 20);
		UpSelfsimilarity_normal.setFont(new Font("Serif", Font.BOLD, 15));
		UpSelfsimilarity_normal.setBorder(eborder);
		UpSelfsimilarity_normal.setHorizontalAlignment(SwingConstants.CENTER);
		UpSelfsimilarity_normal.setBackground(Color.white);
		frame.getContentPane().add(UpSelfsimilarity_normal);

		UpSelfsimilarity = new JLabel("");
		UpSelfsimilarity.setBounds(1450, 670, 100, 20);
		UpSelfsimilarity.setFont(new Font("Serif", Font.BOLD, 15));
		UpSelfsimilarity.setBorder(eborder);
		UpSelfsimilarity.setHorizontalAlignment(SwingConstants.CENTER);
		UpSelfsimilarity.setBackground(Color.white);
		frame.getContentPane().add(UpSelfsimilarity);

		
		JLabel cur_state_lbl = new JLabel("<html><center>Current Status</center></html>");
		cur_state_lbl.setBorder(eborder);
		cur_state_lbl.setBounds(1105, 715, 180, 50);
		cur_state_lbl.setFont(new Font("Serif", Font.BOLD, 20));
		cur_state_lbl.setHorizontalAlignment(SwingConstants.CENTER);
		frame.getContentPane().add(cur_state_lbl);
		
		JLabel numofattacks_lbl = new JLabel("<html><center><p style='margin-top:-5'># Detected<br><p style='margin-top:-10'>Attacks</center></html>");
		numofattacks_lbl.setBorder(eborder);
		numofattacks_lbl.setBounds(1285, 715, 180, 50);
		numofattacks_lbl.setFont(new Font("Serif", Font.BOLD, 20));
		numofattacks_lbl.setHorizontalAlignment(SwingConstants.CENTER);
		frame.getContentPane().add(numofattacks_lbl);
		
		JLabel numoferrors_lbl = new JLabel("<html><center># Errors</center></html>");
		numoferrors_lbl.setBorder(eborder);
		numoferrors_lbl.setBounds(1465, 715, 180, 50);
		numoferrors_lbl.setFont(new Font("Serif", Font.BOLD, 20));
		numoferrors_lbl.setHorizontalAlignment(SwingConstants.CENTER);
		frame.getContentPane().add(numoferrors_lbl);
		
		
		CurMonitor_LBL = new JLabel();
		CurMonitor_LBL.setBorder(eborder);
		CurMonitor_LBL.setBounds(1105, 765, 180, 50);
		CurMonitor_LBL.setFont(new Font("Serif", Font.BOLD, 23));
		CurMonitor_LBL.setHorizontalAlignment(SwingConstants.LEFT);
		frame.getContentPane().add(CurMonitor_LBL);
		
		int boxsize = 30;
		
		timage = new BufferedImage(boxsize, boxsize, BufferedImage.TYPE_INT_RGB);
		fimage = new BufferedImage(boxsize, boxsize, BufferedImage.TYPE_INT_RGB);
		
		Graphics2D tg = timage.createGraphics();
		Graphics2D fg = fimage.createGraphics();
		
		GradientPaint tt = new GradientPaint(0f, 0f, new Color(255,0,0,180), 0f, boxsize, new Color(255,0,0,230));
		//GradientPaint ff = new GradientPaint(0f, 0f, new Color(244,244,244,180), 0f, 13f, new Color(244,244,244,230));
		GradientPaint ff = new GradientPaint(0f, 0f, new Color(0,255,0,180), 0f, boxsize, new Color(0,255,0,230));
		tg.setPaint(tt);
		fg.setPaint(ff);
		tg.fillRect(0, 0, boxsize, boxsize);
		fg.fillRect(0, 0, boxsize, boxsize);
		
		CurMonitor_Box = new ReadOnlyCheckBox();
		CurMonitor_Box.setBounds(1125, 775, boxsize, boxsize);
		CurMonitor_Box.setVisible(false);
		CurMonitor_Box.setBorder(new EmptyBorder(3, 5, 2, 20));
		CurMonitor_Box.setIcon(new ImageIcon(fimage));
		CurMonitor_Box.setSelectedIcon(new ImageIcon(timage));
		frame.getContentPane().add(CurMonitor_Box);
		
		TotalAttacks = new JLabel();
		TotalAttacks.setBorder(eborder);
		TotalAttacks.setBounds(1285, 765, 180, 50);
		TotalAttacks.setFont(new Font("Serif", Font.BOLD, 25));
		TotalAttacks.setHorizontalAlignment(SwingConstants.CENTER);
		frame.getContentPane().add(TotalAttacks);

		TotalErrors = new JLabel();
		TotalErrors.setBorder(eborder);
		TotalErrors.setBounds(1465, 765, 180, 50);
		TotalErrors.setFont(new Font("Serif", Font.BOLD, 25));
		TotalErrors.setHorizontalAlignment(SwingConstants.CENTER);
		frame.getContentPane().add(TotalErrors);
		

		JButton btnDetecRes = new JButton("View Analysis Result");
		btnDetecRes.setFont(new Font("Serif", Font.BOLD, 25));
		btnDetecRes.setBounds(1180, 840, 400, 50);
		btnDetecRes.setBackground(Color.PINK);
		btnDetecRes.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new DetectWindow();
			}
		});
		btnDetecRes.setFocusPainted(false);
		frame.getContentPane().add(btnDetecRes);

		JLabel label5 = new JLabel("Received CAN Packet");
		label5.setBounds(25, 15, 200, 20);
		label5.setFont(new Font("Serif", Font.BOLD, 15));
		label5.setHorizontalAlignment(SwingConstants.LEFT);
		frame.getContentPane().add(label5);

		CanRx = new JTextArea();
		CanRx.setEditable(false);

		scrollPane = new JScrollPane(CanRx);
		scrollPane.setBounds(25, 45, 1025, 100);
		frame.getContentPane().add(scrollPane);
		scrollPane.setVisible(true);

		DefaultCaret caret = (DefaultCaret) CanRx.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		btnStart = new JButton("Start");
		btnStart.setFont(new Font("Serif", Font.BOLD, 17));
		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Bus Load count up!!!
				if (m != null) {
					try {
						going = true;
						m.Packet_receive_start();
						busload_graph.startDraw();
						numOfMsg_graph.startDraw();
						numOfIDs_graph.startDraw();
						id_similarity_graph.startDraw();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		btnStart.setBounds(25, 870, 120, 25);
		frame.getContentPane().add(btnStart);

		btnPause = new JButton("Pause");
		btnPause.setFont(new Font("Serif", Font.BOLD, 17));
		btnPause.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (m != null) {
					try {
						going = false;
						m.Packet_receive_Pause();
						busload_graph.stopDraw();
						numOfMsg_graph.stopDraw();
						numOfIDs_graph.stopDraw();
						id_similarity_graph.stopDraw();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		btnPause.setBounds(180, 870, 120, 25);
		frame.getContentPane().add(btnPause);

		//CanModel = new DefaultTableModel(s1080p_colnames, 0);
		CanModel = new DefaultTableModel(s1080p_colnames, 0)
		{
			private static final long serialVersionUID = 3780143003478460644L;

			@Override
		    public boolean isCellEditable(int row, int column) {
		       return false;
		    }
		};
		CanTable = new JTable(CanModel) {
			private static final long serialVersionUID = 3869291354911175259L;

			DefaultTableCellRenderer renderCenter = new DefaultTableCellRenderer();
			{
				renderCenter.setHorizontalAlignment(SwingConstants.CENTER);
			}

			@Override
			public TableCellRenderer getCellRenderer(int arg0, int arg1) {
				return renderCenter;
			}
		};

		scrollPane2 = new JScrollPane(CanTable);
		scrollPane2.setBounds(25, 155, 1025, 700);
		frame.getContentPane().add(scrollPane2);
		scrollPane2.setVisible(true);

		ascending = new JCheckBox("Ascending Order");
		ascending.setFont(new Font("Serif", Font.BOLD, 14));
		ascending.setBounds(920, 855, 200, 30);
		ascending.setSelected(false);
		frame.getContentPane().add(ascending);

		if (onlineMode) {
			Monitor.setOnlineMode(true);
		}

		if (DatasetFileLoc != null)
			Monitor.setDataSetFile(DatasetFileLoc);

		m = new Monitor();

		updateNormalState updateNState = new updateNormalState();
		updateNState.start();

		updateCurrentFeatures updateCurState = new updateCurrentFeatures();
		updateCurState.start();

		updatePacketInfos updatePacketinfo = new updatePacketInfos();
		updatePacketinfo.start();
	}

	public static void updateCanRxField(String str) {
		if (CanRx.getLineCount() > 10000)
			clearCanRxField();

		CanRx.append(str + "\n");
	}

	public static void clearCanRxField() {
		CanRx.setText("");
	}

	public static void uiControl(boolean switch_on) {
		going = switch_on;
	}

	class updatePacketInfos extends Thread {
		public void run() {
			while (true) {
				try {
					TimeUnit.MILLISECONDS.sleep(refresh_freq);
				} catch (Exception e) {
					e.printStackTrace();
				}

				if (m.isReceiverAlive() && (Monitor.final_features.f10_IDfeatures != null) && going) {
					HashMap<String, ShowObject> datas = new HashMap<String, ShowObject>(
							Monitor.final_features.f10_IDfeatures);

					//CanModel = new DefaultTableModel(s1080p_colnames, 0);
					CanModel = new DefaultTableModel(s1080p_colnames, 0)
					{
						private static final long serialVersionUID = 3780143003478460644L;

						@Override
					    public boolean isCellEditable(int row, int column) {
					       return false;
					    }
					};
					CanTable.setModel(CanModel);

					if (ascending.isSelected()) {
						TreeMap<String, ShowObject> tm = new TreeMap<String, ShowObject>(datas);

						Iterator<String> iteratorKey = tm.keySet().iterator();

						while (iteratorKey.hasNext()) {
							String key = iteratorKey.next();
							ShowObject value = ((ShowObject) tm.get(key));

							Object[] row = { key,
									// String.format("%.1f", value.freq),
									String.format("%.1f",
											Monitor.normal_condition.f5_frequence_per_ID.get(key.toUpperCase())),
									String.format("%.1f", ((double) value.elapsed_time) / 1000.0), value.count,
									value.data_length, value.data, value.timestamp };
							CanModel.addRow(row);
						}
					} else {
						for (Map.Entry<String, ShowObject> entry : datas.entrySet()) {
							Object[] row = { entry.getKey(),
									// String.format("%.1f",
									// ((ShowObject)entry.getValue()).freq),
									String.format("%.1f",
											Monitor.normal_condition.f5_frequence_per_ID
													.get(entry.getKey())),
									String.format("%.1f",
											((double) ((ShowObject) entry.getValue()).elapsed_time) / 1000.0),
									((ShowObject) entry.getValue()).count, ((ShowObject) entry.getValue()).data_length,
									((ShowObject) entry.getValue()).data, ((ShowObject) entry.getValue()).timestamp };
							CanModel.addRow(row);

						}
					}
				}
			}
		}
	}

	class updateNormalState extends Thread {

		public void run() {
			while (true) {
				if (Monitor.normal_condition.isready) {
					UpBusLoad_normal.setText(String.format("%.2f", Monitor.normal_condition.f2_bus_load));
					UpNumOfMessage_normal.setText(String.format("%.2f", Monitor.normal_condition.f1_number_of_packets));
					UpNumOfIDs_normal.setText(String.format("%.2f", Monitor.normal_condition.f3_number_of_IDs));
					UpSelfsimilarity_normal.setText(Integer.toString(Monitor.similarity_minimum_threshold));

					break;
				}
			}
		}
	}

	class updateCurrentFeatures extends Thread {
		public void run() {
			//String totalattacks = "Detected Number of Attacks : ";
			//String totalerrors = "Detected Number of Errors : ";

			while (true) {
				try {
					// TimeUnit.MILLISECONDS.sleep(refresh_freq);
					TimeUnit.MILLISECONDS.sleep(fast_refresh_freq);

					// m.show_all_features();

					if (m.isReceiverAlive() && going) {
						FeatureObject bus = Monitor.final_features.get_f2_FeatureObject();
						if (Monitor.normal_condition.isready)
							busload_graph.updateData(
									(double) Monitor.normal_condition.f2_bus_load * F2_Bus_Load.upper_bound_coeff,
									Double.parseDouble(bus.getValue()),
									(double) Monitor.normal_condition.f2_bus_load * F2_Bus_Load.lower_bound_coeff,
									bus.getTimestamp());
						else
							busload_graph.updateData((double) 100, Double.parseDouble(bus.getValue()), (double) 0,
									bus.getTimestamp());
						UpBusLoad.setText(String.format("%.2f", Float.parseFloat(bus.getValue())));

						FeatureObject nom = Monitor.final_features.get_f1_FeatureObject();
						numOfMsg_graph.updateData(Double.parseDouble(nom.getValue()));
						UpNumOfMessage.setText(nom.getValue());

						FeatureObject noid = Monitor.final_features.get_f3_FeatureObject();
						numOfIDs_graph.updateData(Double.parseDouble(noid.getValue()));
						UpNumOfIDs.setText(noid.getValue());

						FeatureObject idseq = Monitor.final_features.get_f6_Similarity();
						id_similarity_graph.updateData((double) Monitor.similarity_minimum_threshold,
								Double.parseDouble(idseq.getValue()), (double) 0, idseq.getTimestamp());
						UpSelfsimilarity.setText(idseq.getValue());

						if(MainFrame_1080p.suspicious)
						{
							CurMonitor_Box.setVisible(true);
							CurMonitor_Box.setSelected(true);
							CurMonitor_LBL.setText(abnormal_str);
						}
						else
						{
							CurMonitor_Box.setVisible(true);
							CurMonitor_Box.setSelected(false);
							CurMonitor_LBL.setText(normal_str);
						}
						//TotalAttacks.setText(totalattacks + Monitor.anomaly_interval.getNumofInterval());
						//TotalErrors.setText(totalerrors + Monitor.anomaly_interval.getNumofInterval2());
						TotalAttacks.setText(Integer.toString(Monitor.anomaly_interval.getNumofInterval()));
						TotalErrors.setText(Integer.toString(Monitor.anomaly_interval.getNumofInterval2()));
					}
				} catch (Exception e) {
					// e.printStackTrace();
				}
			}
		}
	}

	public static void updateNormalState(String[] args) {
		UpBusLoad_normal.setText(args[0]);
		UpNumOfMessage_normal.setText(args[1]);
		UpNumOfIDs_normal.setText(args[2]);
		UpSelfsimilarity_normal.setText(args[3]);
	}

	class DetectWindow {

		private JFrame frame;

		private String detcolName[] = { "Start time", "End time", "Elapsed time (s)" };
		private DefaultTableModel attack_tbl_model = null;
		private JTable attack_Tbl = null;

		private JScrollPane attack_list_sc = null;
		private JScrollPane attack_list_radio_sc = null;
		private JRadioButton[] attack_radio_buttons = null;
		private ButtonGroup attack_radiogroup = null;
		private JPanel attack_radiobtns_panel = null;

		private JScrollPane attack_list_lowbus_sc = null;
		private JCheckBox[] attack_lowbus_boxes = null;
		private JPanel attack_lowbus_btns_panel = null;

		private JScrollPane attack_list_highbus_sc = null;
		private JCheckBox[] attack_highbus_boxes = null;
		private JPanel attack_highbus_btns_panel = null;

		private JScrollPane attack_list_lessNoP_sc = null;
		private JCheckBox[] attack_lessNoP_boxes = null;
		private JPanel attack_lessNoP_btns_panel = null;

		private JScrollPane attack_list_moreNoP_sc = null;
		private JCheckBox[] attack_moreNoP_boxes = null;
		private JPanel attack_moreNoP_btns_panel = null;

		private JScrollPane attack_list_lessNoID_sc = null;
		private JCheckBox[] attack_lessNoID_boxes = null;
		private JPanel attack_lessNoID_btns_panel = null;

		private JScrollPane attack_list_moreNoID_sc = null;
		private JCheckBox[] attack_moreNoID_boxes = null;
		private JPanel attack_moreNoID_btns_panel = null;

		private JScrollPane attack_list_lowFreq_sc = null;
		private JCheckBox[] attack_lowFreq_boxes = null;
		private JPanel attack_lowFreq_btns_panel = null;

		private JScrollPane attack_list_highFreq_sc = null;
		private JCheckBox[] attack_highFreq_boxes = null;
		private JPanel attack_highFreq_btns_panel = null;

		private JScrollPane attack_list_invalidID_sc = null;
		private JCheckBox[] attack_invalidID_boxes = null;
		private JPanel attack_invalidID_btns_panel = null;

		private JScrollPane attack_list_invalidIDSeq_sc = null;
		private JCheckBox[] attack_invalidIDSeq_boxes = null;
		private JPanel attack_invalidIDSeq_btns_panel = null;

		private JScrollPane attack_list_finaltype_sc = null;
		private JLabel[] attack_finaltype_labels = null;
		private JPanel attack_finaltype_btns_panel = null;

		private DefaultTableModel error_tbl_model = null;
		private JTable error_Tbl = null;

		private JScrollPane error_list_sc = null;
		private JScrollPane error_list_radio_sc = null;
		private JRadioButton[] error_radio_buttons = null;
		private ButtonGroup error_radiogroup = null;
		private JPanel error_radiobtns_panel = null;

		private JScrollPane error_list_lowbus_sc = null;
		private JCheckBox[] error_lowbus_boxes = null;
		private JPanel error_lowbus_btns_panel = null;

		private JScrollPane error_list_highbus_sc = null;
		private JCheckBox[] error_highbus_boxes = null;
		private JPanel error_highbus_btns_panel = null;

		private JScrollPane error_list_lessNoP_sc = null;
		private JCheckBox[] error_lessNoP_boxes = null;
		private JPanel error_lessNoP_btns_panel = null;

		private JScrollPane error_list_moreNoP_sc = null;
		private JCheckBox[] error_moreNoP_boxes = null;
		private JPanel error_moreNoP_btns_panel = null;

		private JScrollPane error_list_lessNoID_sc = null;
		private JCheckBox[] error_lessNoID_boxes = null;
		private JPanel error_lessNoID_btns_panel = null;

		private JScrollPane error_list_moreNoID_sc = null;
		private JCheckBox[] error_moreNoID_boxes = null;
		private JPanel error_moreNoID_btns_panel = null;

		private JScrollPane error_list_lowFreq_sc = null;
		private JCheckBox[] error_lowFreq_boxes = null;
		private JPanel error_lowFreq_btns_panel = null;

		private JScrollPane error_list_highFreq_sc = null;
		private JCheckBox[] error_highFreq_boxes = null;
		private JPanel error_highFreq_btns_panel = null;

		private JScrollPane error_list_invalidID_sc = null;
		private JCheckBox[] error_invalidID_boxes = null;
		private JPanel error_invalidID_btns_panel = null;

		private JScrollPane error_list_invalidIDSeq_sc = null;
		private JCheckBox[] error_invalidIDSeq_boxes = null;
		private JPanel error_invalidIDSeq_btns_panel = null;

		private JScrollPane error_list_finaltype_sc = null;
		private JLabel[] error_finaltype_labels = null;
		private JPanel error_finaltype_btns_panel = null;

		private JProgressBar progressBar = null;
		private JProgressBar progressBar2 = null;

		private JLabel label_load = null;

		private JLabel label_per_abnormal = null;
		private JButton previous = null;
		private JButton next = null;

		private JLabel det_label1 = null;
		private JLabel det_label2 = null;
		private JLabel det_label3 = null;
		// private JLabel det_label4 = null;

		private JLabel det_UpBusLoad = null;
		private JLabel det_UpNumOfMessage = null;
		private JLabel det_UpNumOfIDs = null;
		// private JLabel det_UpSelfsimilarity = null;

		private JScrollPane det_scrollPane = null;
		private DefaultTableModel det_CanModel = null;
		private JTable det_CanTable = null;
		private JCheckBox det_ascending = null;

		public DetectWindow() {
			Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

			frame = new JFrame("Anomaly Detection Result");
			//frame.setSize(1880, 800);
			frame.setSize(790, 800);
			frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
			frame.setResizable(false);

			Dimension f1_size = frame.getSize();
			int left = (screen.width / 2) - (f1_size.width / 2);
			int top = (screen.height / 2) - (f1_size.height / 2);
			frame.setLocation(left, top);
			frame.getContentPane().setLayout(null);
			frame.setVisible(true);

			JLabel label = new JLabel("The List of Detected Attacks");
			label.setBounds(25, 15, 200, 20);
			label.setFont(new Font("Serif", Font.BOLD, 15));
			label.setHorizontalAlignment(SwingConstants.LEFT);
			frame.getContentPane().add(label);

			// ======================================
			// Table Scroll pane Begin
			// ======================================
			attack_tbl_model = new DefaultTableModel(detcolName, 0);
			attack_Tbl = new JTable(attack_tbl_model) {
				private static final long serialVersionUID = 6521313161490729702L;

				DefaultTableCellRenderer renderCenter = new DefaultTableCellRenderer();
				{
					renderCenter.setHorizontalAlignment(SwingConstants.CENTER);
				}

				@Override
				public TableCellRenderer getCellRenderer(int arg0, int arg1) {
					return renderCenter;
				}
			};
			attack_Tbl.setRowHeight(18);
			attack_Tbl.setEnabled(false);

			attack_list_sc = new JScrollPane(attack_Tbl);
			attack_list_sc.setBounds(25, 40, 350, 360);
			attack_list_sc.setVisible(true);
			attack_list_sc.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {

				@Override
				public void adjustmentValueChanged(AdjustmentEvent evt) {
					int value = evt.getValue();
					set_attack_scroll(value);
				}
			});
			frame.getContentPane().add(attack_list_sc);
			// ======================================
			// Table Scroll pane End
			// ======================================

			Border border = LineBorder.createGrayLineBorder();
			// ======================================
			// LowBus Box Scroll pane Begin
			// ======================================
			JLabel attack1_label = new JLabel("L-BL");
			attack1_label.setBounds(380, 40, 25, 20);
			attack1_label.setFont(new Font("Serif", Font.PLAIN, 9));
			attack1_label.setHorizontalAlignment(SwingConstants.CENTER);
			frame.getContentPane().add(attack1_label);

			attack_lowbus_btns_panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
			attack_lowbus_btns_panel.setBorder(new EmptyBorder(0, 8, 4, 0));
			attack_lowbus_btns_panel.setVisible(true);

			attack_list_lowbus_sc = new JScrollPane(attack_lowbus_btns_panel, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			// attack_list_lowbus_sc.setBorder(new EmptyBorder(0, 0, 0, 0));
			attack_list_lowbus_sc.setBorder(border);
			attack_list_lowbus_sc.getVerticalScrollBar().setVisible(false);
			attack_list_lowbus_sc.getHorizontalScrollBar().setVisible(false);

			attack_list_lowbus_sc.setBounds(380, 60, 25, 340);
			frame.getContentPane().add(attack_list_lowbus_sc);
			// ======================================
			// LowBus Box Scroll pane End
			// ======================================

			// ======================================
			// HighBus Box Scroll pane Begin
			// ======================================
			JLabel attack2_label = new JLabel("H-BL");
			attack2_label.setBounds(405, 40, 25, 20);
			attack2_label.setFont(new Font("Serif", Font.PLAIN, 9));
			attack2_label.setHorizontalAlignment(SwingConstants.CENTER);
			frame.getContentPane().add(attack2_label);

			attack_highbus_btns_panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
			attack_highbus_btns_panel.setBorder(new EmptyBorder(0, 8, 4, 0));
			attack_highbus_btns_panel.setVisible(true);

			attack_list_highbus_sc = new JScrollPane(attack_highbus_btns_panel, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			// attack_list_highbus_sc.setBorder(new EmptyBorder(0, 0, 0, 0));
			attack_list_highbus_sc.setBorder(border);
			attack_list_highbus_sc.getVerticalScrollBar().setVisible(false);
			attack_list_highbus_sc.getHorizontalScrollBar().setVisible(false);

			attack_list_highbus_sc.setBounds(405, 60, 25, 340);
			frame.getContentPane().add(attack_list_highbus_sc);
			// ======================================
			// HighBus Box Scroll pane End
			// ======================================

			// ======================================
			// less Number of Packet Box Scroll pane Begin
			// ======================================
			JLabel attack3_label = new JLabel("<html>Less<br>NoP</html>");
			attack3_label.setBounds(430, 40, 25, 20);
			attack3_label.setFont(new Font("Serif", Font.PLAIN, 8));
			attack3_label.setHorizontalAlignment(SwingConstants.CENTER);
			frame.getContentPane().add(attack3_label);

			attack_lessNoP_btns_panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
			attack_lessNoP_btns_panel.setBorder(new EmptyBorder(0, 8, 4, 0));
			attack_lessNoP_btns_panel.setVisible(true);

			attack_list_lessNoP_sc = new JScrollPane(attack_lessNoP_btns_panel, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			// attack_list_lessNoP_sc.setBorder(new EmptyBorder(0, 0, 0, 0));
			attack_list_lessNoP_sc.setBorder(border);
			attack_list_lessNoP_sc.getVerticalScrollBar().setVisible(false);
			attack_list_lessNoP_sc.getHorizontalScrollBar().setVisible(false);

			attack_list_lessNoP_sc.setBounds(430, 60, 25, 340);
			frame.getContentPane().add(attack_list_lessNoP_sc);
			// ======================================
			// less Number of Packet Box Scroll pane End
			// ======================================

			// ======================================
			// more Number of Packet Box Scroll pane Begin
			// ======================================
			JLabel attack4_label = new JLabel("<html>More<br>NoP</html>");
			attack4_label.setBounds(455, 40, 25, 20);
			attack4_label.setFont(new Font("Serif", Font.PLAIN, 8));
			attack4_label.setHorizontalAlignment(SwingConstants.CENTER);
			frame.getContentPane().add(attack4_label);

			attack_moreNoP_btns_panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
			attack_moreNoP_btns_panel.setBorder(new EmptyBorder(0, 8, 4, 0));
			attack_moreNoP_btns_panel.setVisible(true);

			attack_list_moreNoP_sc = new JScrollPane(attack_moreNoP_btns_panel, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			// attack_list_moreNoP_sc.setBorder(new EmptyBorder(0, 0, 0, 0));
			attack_list_moreNoP_sc.setBorder(border);
			attack_list_moreNoP_sc.getVerticalScrollBar().setVisible(false);
			attack_list_moreNoP_sc.getHorizontalScrollBar().setVisible(false);

			attack_list_moreNoP_sc.setBounds(455, 60, 25, 340);
			frame.getContentPane().add(attack_list_moreNoP_sc);
			// ======================================
			// more Number of Packet Box Scroll pane End
			// ======================================

			// ======================================
			// less Number of ID Box Scroll pane Begin
			// ======================================
			JLabel attack5_label = new JLabel("<html>Less<br>NoID</html>");
			attack5_label.setBounds(480, 40, 25, 20);
			attack5_label.setFont(new Font("Serif", Font.PLAIN, 8));
			attack5_label.setHorizontalAlignment(SwingConstants.CENTER);
			frame.getContentPane().add(attack5_label);

			attack_lessNoID_btns_panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
			attack_lessNoID_btns_panel.setBorder(new EmptyBorder(0, 8, 4, 0));
			attack_lessNoID_btns_panel.setVisible(true);

			attack_list_lessNoID_sc = new JScrollPane(attack_lessNoID_btns_panel, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			// attack_list_lessNoID_sc.setBorder(new EmptyBorder(0, 0, 0, 0));
			attack_list_lessNoID_sc.setBorder(border);
			attack_list_lessNoID_sc.getVerticalScrollBar().setVisible(false);
			attack_list_lessNoID_sc.getHorizontalScrollBar().setVisible(false);

			attack_list_lessNoID_sc.setBounds(480, 60, 25, 340);
			frame.getContentPane().add(attack_list_lessNoID_sc);
			// ======================================
			// less Number of ID Box Scroll pane End
			// ======================================

			// ======================================
			// more Number of ID Box Scroll pane Begin
			// ======================================
			JLabel attack6_label = new JLabel("<html>More<br>NoID</html>");
			attack6_label.setBounds(505, 40, 25, 20);
			attack6_label.setFont(new Font("Serif", Font.PLAIN, 8));
			attack6_label.setHorizontalAlignment(SwingConstants.CENTER);
			frame.getContentPane().add(attack6_label);

			attack_moreNoID_btns_panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
			attack_moreNoID_btns_panel.setBorder(new EmptyBorder(0, 8, 4, 0));
			attack_moreNoID_btns_panel.setVisible(true);

			attack_list_moreNoID_sc = new JScrollPane(attack_moreNoID_btns_panel, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			// attack_list_moreNoID_sc.setBorder(new EmptyBorder(0, 0, 0, 0));
			attack_list_moreNoID_sc.setBorder(border);
			attack_list_moreNoID_sc.getVerticalScrollBar().setVisible(false);
			attack_list_moreNoID_sc.getHorizontalScrollBar().setVisible(false);

			attack_list_moreNoID_sc.setBounds(505, 60, 25, 340);
			frame.getContentPane().add(attack_list_moreNoID_sc);
			// ======================================
			// more Number of ID Box Scroll pane End
			// ======================================

			// ======================================
			// LowFreq Box Scroll pane Begin
			// ======================================
			JLabel attack7_label = new JLabel("<html>Low<br>Freq</html>");
			attack7_label.setBounds(530, 40, 25, 20);
			attack7_label.setFont(new Font("Serif", Font.PLAIN, 8));
			attack7_label.setHorizontalAlignment(SwingConstants.CENTER);
			frame.getContentPane().add(attack7_label);

			attack_lowFreq_btns_panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
			attack_lowFreq_btns_panel.setBorder(new EmptyBorder(0, 8, 4, 0));
			attack_lowFreq_btns_panel.setVisible(true);

			attack_list_lowFreq_sc = new JScrollPane(attack_lowFreq_btns_panel, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			// attack_list_lowFreq_sc.setBorder(new EmptyBorder(0, 0, 0, 0));
			attack_list_lowFreq_sc.setBorder(border);
			attack_list_lowFreq_sc.getVerticalScrollBar().setVisible(false);
			attack_list_lowFreq_sc.getHorizontalScrollBar().setVisible(false);

			attack_list_lowFreq_sc.setBounds(530, 60, 25, 340);
			frame.getContentPane().add(attack_list_lowFreq_sc);
			// ======================================
			// LowFreq Box Scroll pane End
			// ======================================

			// ======================================
			// HighFreq Box Scroll pane Begin
			// ======================================
			JLabel attack8_label = new JLabel("<html>High<br>Freq</html>");
			attack8_label.setBounds(555, 40, 25, 20);
			attack8_label.setFont(new Font("Serif", Font.PLAIN, 8));
			attack8_label.setHorizontalAlignment(SwingConstants.CENTER);
			frame.getContentPane().add(attack8_label);

			attack_highFreq_btns_panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
			attack_highFreq_btns_panel.setBorder(new EmptyBorder(0, 8, 4, 0));
			attack_highFreq_btns_panel.setVisible(true);

			attack_list_highFreq_sc = new JScrollPane(attack_highFreq_btns_panel, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			// attack_list_highFreq_sc.setBorder(new EmptyBorder(0, 0, 0, 0));
			attack_list_highFreq_sc.setBorder(border);
			attack_list_highFreq_sc.getVerticalScrollBar().setVisible(false);
			attack_list_highFreq_sc.getHorizontalScrollBar().setVisible(false);

			attack_list_highFreq_sc.setBounds(555, 60, 25, 340);
			frame.getContentPane().add(attack_list_highFreq_sc);
			// ======================================
			// HighFreq Box Scroll pane End
			// ======================================

			// ======================================
			// Invalid ID Scroll pane Begin
			// ======================================
			JLabel attack9_label = new JLabel("Inv-ID");
			attack9_label.setBounds(580, 40, 25, 20);
			attack9_label.setFont(new Font("Serif", Font.PLAIN, 8));
			attack9_label.setHorizontalAlignment(SwingConstants.CENTER);
			frame.getContentPane().add(attack9_label);

			attack_invalidID_btns_panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
			attack_invalidID_btns_panel.setBorder(new EmptyBorder(0, 8, 4, 0));
			attack_invalidID_btns_panel.setVisible(true);

			attack_list_invalidID_sc = new JScrollPane(attack_invalidID_btns_panel,
					JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			// attack_list_invalidID_sc.setBorder(new EmptyBorder(0, 0, 0, 0));
			attack_list_invalidID_sc.setBorder(border);
			attack_list_invalidID_sc.getVerticalScrollBar().setVisible(false);
			attack_list_invalidID_sc.getHorizontalScrollBar().setVisible(false);

			attack_list_invalidID_sc.setBounds(580, 60, 25, 340);
			frame.getContentPane().add(attack_list_invalidID_sc);
			// ======================================
			// Invalid ID Scroll pane End
			// ======================================

			// ======================================
			// Invalid ID Seq Scroll pane Begin
			// ======================================
			JLabel attack10_label = new JLabel("<html><center>Inv-ID<br>Seq</center></html>");
			attack10_label.setBounds(605, 40, 25, 20);
			attack10_label.setFont(new Font("Serif", Font.PLAIN, 8));
			attack10_label.setHorizontalAlignment(SwingConstants.CENTER);
			frame.getContentPane().add(attack10_label);

			attack_invalidIDSeq_btns_panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
			attack_invalidIDSeq_btns_panel.setBorder(new EmptyBorder(0, 8, 4, 0));
			attack_invalidIDSeq_btns_panel.setVisible(true);

			attack_list_invalidIDSeq_sc = new JScrollPane(attack_invalidIDSeq_btns_panel,
					JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			// attack_list_invalidIDSeq_sc.setBorder(new EmptyBorder(0, 0, 0,
			// 0));
			attack_list_invalidIDSeq_sc.setBorder(border);
			attack_list_invalidIDSeq_sc.getVerticalScrollBar().setVisible(false);
			attack_list_invalidIDSeq_sc.getHorizontalScrollBar().setVisible(false);

			attack_list_invalidIDSeq_sc.setBounds(605, 60, 25, 340);
			frame.getContentPane().add(attack_list_invalidIDSeq_sc);
			// ======================================
			// Invalid ID Seq Scroll pane End
			// ======================================

			// ======================================
			// Attack Type Scroll pane Begin
			// ======================================
			JLabel attack11_label = new JLabel("Attack Type");
			attack11_label.setBounds(630, 40, 100, 20);
			attack11_label.setFont(new Font("Serif", Font.PLAIN, 12));
			attack11_label.setHorizontalAlignment(SwingConstants.CENTER);
			frame.getContentPane().add(attack11_label);

			attack_finaltype_btns_panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
			attack_finaltype_btns_panel.setBorder(new EmptyBorder(0, 8, 4, 0));
			attack_finaltype_btns_panel.setVisible(true);

			attack_list_finaltype_sc = new JScrollPane(attack_finaltype_btns_panel,
					JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			// attack_list_finaltype_sc.setBorder(new EmptyBorder(0, 0, 0, 0));
			attack_list_finaltype_sc.setBorder(border);
			attack_list_finaltype_sc.getVerticalScrollBar().setVisible(false);
			attack_list_finaltype_sc.getHorizontalScrollBar().setVisible(false);

			attack_list_finaltype_sc.setBounds(630, 60, 100, 340);
			frame.getContentPane().add(attack_list_finaltype_sc);
			// ======================================
			// Attack Type Scroll pane End
			// ======================================

			// ======================================
			// Radio Button Scroll pane Begin
			// ======================================
			JLabel attack12_label = new JLabel("Sel");
			attack12_label.setBounds(730, 40, 25, 20);
			attack12_label.setFont(new Font("Serif", Font.PLAIN, 9));
			attack12_label.setHorizontalAlignment(SwingConstants.CENTER);
			frame.getContentPane().add(attack12_label);

			attack_radiobtns_panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
			attack_radiobtns_panel.setBorder(new EmptyBorder(0, 8, 4, 0));
			attack_radiobtns_panel.setVisible(true);

			attack_list_radio_sc = new JScrollPane(attack_radiobtns_panel, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			// attack_list_radio_sc.setBorder(new EmptyBorder(0, 0, 0, 0));
			attack_list_radio_sc.setBorder(border);
			attack_list_radio_sc.getVerticalScrollBar().setVisible(false);
			attack_list_radio_sc.getHorizontalScrollBar().setVisible(false);

			attack_list_radio_sc.setBounds(730, 60, 25, 340);
			frame.getContentPane().add(attack_list_radio_sc);
			// ======================================
			// Radio Button Scroll pane End
			// ======================================

			JLabel label2 = new JLabel("The List of Detected Errors");
			label2.setBounds(25, 410, 200, 20);
			label2.setFont(new Font("Serif", Font.BOLD, 15));
			label2.setHorizontalAlignment(SwingConstants.LEFT);
			frame.getContentPane().add(label2);

			// ======================================
			// Error Table Scroll pane Begin
			// ======================================
			error_tbl_model = new DefaultTableModel(detcolName, 0);
			error_Tbl = new JTable(error_tbl_model) {
				private static final long serialVersionUID = 324370063483861133L;

				DefaultTableCellRenderer renderCenter = new DefaultTableCellRenderer();
				{
					renderCenter.setHorizontalAlignment(SwingConstants.CENTER);
				}

				@Override
				public TableCellRenderer getCellRenderer(int arg0, int arg1) {
					return renderCenter;
				}
			};
			error_Tbl.setRowHeight(18);
			error_Tbl.setEnabled(false);

			error_list_sc = new JScrollPane(error_Tbl);
			error_list_sc.setBounds(25, 435, 350, 300);
			error_list_sc.setVisible(true);
			error_list_sc.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {

				@Override
				public void adjustmentValueChanged(AdjustmentEvent evt) {
					int value = evt.getValue();
					set_error_scroll(value);
				}
			});
			frame.getContentPane().add(error_list_sc);
			// ======================================
			// Table Scroll pane End
			// ======================================

			// ======================================
			// LowBus Box Scroll pane Begin
			// ======================================
			JLabel error1_label = new JLabel("L-BL");
			error1_label.setBounds(380, 435, 25, 20);
			error1_label.setFont(new Font("Serif", Font.PLAIN, 9));
			error1_label.setHorizontalAlignment(SwingConstants.CENTER);
			frame.getContentPane().add(error1_label);

			error_lowbus_btns_panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
			error_lowbus_btns_panel.setBorder(new EmptyBorder(0, 8, 4, 0));
			error_lowbus_btns_panel.setVisible(true);

			error_list_lowbus_sc = new JScrollPane(error_lowbus_btns_panel, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			// error_list_lowbus_sc.setBorder(new EmptyBorder(0, 0, 0, 0));
			error_list_lowbus_sc.setBorder(border);
			error_list_lowbus_sc.getVerticalScrollBar().setVisible(false);
			error_list_lowbus_sc.getHorizontalScrollBar().setVisible(false);

			error_list_lowbus_sc.setBounds(380, 455, 25, 280);
			frame.getContentPane().add(error_list_lowbus_sc);
			// ======================================
			// LowBus Box Scroll pane End
			// ======================================

			// ======================================
			// HighBus Box Scroll pane Begin
			// ======================================
			JLabel error2_label = new JLabel("H-BL");
			error2_label.setBounds(405, 435, 25, 20);
			error2_label.setFont(new Font("Serif", Font.PLAIN, 9));
			error2_label.setHorizontalAlignment(SwingConstants.CENTER);
			frame.getContentPane().add(error2_label);

			error_highbus_btns_panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
			error_highbus_btns_panel.setBorder(new EmptyBorder(0, 8, 4, 0));
			error_highbus_btns_panel.setVisible(true);

			error_list_highbus_sc = new JScrollPane(error_highbus_btns_panel, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			// error_list_highbus_sc.setBorder(new EmptyBorder(0, 0, 0, 0));
			error_list_highbus_sc.setBorder(border);
			error_list_highbus_sc.getVerticalScrollBar().setVisible(false);
			error_list_highbus_sc.getHorizontalScrollBar().setVisible(false);

			error_list_highbus_sc.setBounds(405, 455, 25, 280);
			frame.getContentPane().add(error_list_highbus_sc);
			// ======================================
			// HighBus Box Scroll pane End
			// ======================================

			// ======================================
			// less Number of Packet Box Scroll pane Begin
			// ======================================
			JLabel error3_label = new JLabel("<html>Less<br>NoP</html>");
			error3_label.setBounds(430, 435, 25, 20);
			error3_label.setFont(new Font("Serif", Font.PLAIN, 8));
			error3_label.setHorizontalAlignment(SwingConstants.CENTER);
			frame.getContentPane().add(error3_label);

			error_lessNoP_btns_panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
			error_lessNoP_btns_panel.setBorder(new EmptyBorder(0, 8, 4, 0));
			error_lessNoP_btns_panel.setVisible(true);

			error_list_lessNoP_sc = new JScrollPane(error_lessNoP_btns_panel, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			// error_list_lessNoP_sc.setBorder(new EmptyBorder(0, 0, 0, 0));
			error_list_lessNoP_sc.setBorder(border);
			error_list_lessNoP_sc.getVerticalScrollBar().setVisible(false);
			error_list_lessNoP_sc.getHorizontalScrollBar().setVisible(false);

			error_list_lessNoP_sc.setBounds(430, 455, 25, 280);
			frame.getContentPane().add(error_list_lessNoP_sc);
			// ======================================
			// less Number of Packet Box Scroll pane End
			// ======================================

			// ======================================
			// more Number of Packet Box Scroll pane Begin
			// ======================================
			JLabel error4_label = new JLabel("<html>More<br>NoP</html>");
			error4_label.setBounds(455, 435, 25, 20);
			error4_label.setFont(new Font("Serif", Font.PLAIN, 8));
			error4_label.setHorizontalAlignment(SwingConstants.CENTER);
			frame.getContentPane().add(error4_label);

			error_moreNoP_btns_panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
			error_moreNoP_btns_panel.setBorder(new EmptyBorder(0, 8, 4, 0));
			error_moreNoP_btns_panel.setVisible(true);

			error_list_moreNoP_sc = new JScrollPane(error_moreNoP_btns_panel, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			// error_list_moreNoP_sc.setBorder(new EmptyBorder(0, 0, 0, 0));
			error_list_moreNoP_sc.setBorder(border);
			error_list_moreNoP_sc.getVerticalScrollBar().setVisible(false);
			error_list_moreNoP_sc.getHorizontalScrollBar().setVisible(false);

			error_list_moreNoP_sc.setBounds(455, 455, 25, 280);
			frame.getContentPane().add(error_list_moreNoP_sc);
			// ======================================
			// more Number of Packet Box Scroll pane End
			// ======================================

			// ======================================
			// less Number of ID Box Scroll pane Begin
			// ======================================
			JLabel error5_label = new JLabel("<html>Less<br>NoID</html>");
			error5_label.setBounds(480, 435, 25, 20);
			error5_label.setFont(new Font("Serif", Font.PLAIN, 8));
			error5_label.setHorizontalAlignment(SwingConstants.CENTER);
			frame.getContentPane().add(error5_label);

			error_lessNoID_btns_panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
			error_lessNoID_btns_panel.setBorder(new EmptyBorder(0, 8, 4, 0));
			error_lessNoID_btns_panel.setVisible(true);

			error_list_lessNoID_sc = new JScrollPane(error_lessNoID_btns_panel, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			// error_list_lessNoID_sc.setBorder(new EmptyBorder(0, 0, 0, 0));
			error_list_lessNoID_sc.setBorder(border);
			error_list_lessNoID_sc.getVerticalScrollBar().setVisible(false);
			error_list_lessNoID_sc.getHorizontalScrollBar().setVisible(false);

			error_list_lessNoID_sc.setBounds(480, 455, 25, 280);
			frame.getContentPane().add(error_list_lessNoID_sc);
			// ======================================
			// less Number of ID Box Scroll pane End
			// ======================================

			// ======================================
			// more Number of ID Box Scroll pane Begin
			// ======================================
			JLabel error6_label = new JLabel("<html>More<br>NoID</html>");
			error6_label.setBounds(505, 435, 25, 20);
			error6_label.setFont(new Font("Serif", Font.PLAIN, 8));
			error6_label.setHorizontalAlignment(SwingConstants.CENTER);
			frame.getContentPane().add(error6_label);

			error_moreNoID_btns_panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
			error_moreNoID_btns_panel.setBorder(new EmptyBorder(0, 8, 4, 0));
			error_moreNoID_btns_panel.setVisible(true);

			error_list_moreNoID_sc = new JScrollPane(error_moreNoID_btns_panel, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			// error_list_moreNoID_sc.setBorder(new EmptyBorder(0, 0, 0, 0));
			error_list_moreNoID_sc.setBorder(border);
			error_list_moreNoID_sc.getVerticalScrollBar().setVisible(false);
			error_list_moreNoID_sc.getHorizontalScrollBar().setVisible(false);

			error_list_moreNoID_sc.setBounds(505, 455, 25, 280);
			frame.getContentPane().add(error_list_moreNoID_sc);
			// ======================================
			// more Number of ID Box Scroll pane End
			// ======================================

			// ======================================
			// LowFreq Box Scroll pane Begin
			// ======================================
			JLabel error7_label = new JLabel("<html>Low<br>Freq</html>");
			error7_label.setBounds(530, 435, 25, 20);
			error7_label.setFont(new Font("Serif", Font.PLAIN, 8));
			error7_label.setHorizontalAlignment(SwingConstants.CENTER);
			frame.getContentPane().add(error7_label);

			error_lowFreq_btns_panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
			error_lowFreq_btns_panel.setBorder(new EmptyBorder(0, 8, 4, 0));
			error_lowFreq_btns_panel.setVisible(true);

			error_list_lowFreq_sc = new JScrollPane(error_lowFreq_btns_panel, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			// error_list_lowFreq_sc.setBorder(new EmptyBorder(0, 0, 0, 0));
			error_list_lowFreq_sc.setBorder(border);
			error_list_lowFreq_sc.getVerticalScrollBar().setVisible(false);
			error_list_lowFreq_sc.getHorizontalScrollBar().setVisible(false);

			error_list_lowFreq_sc.setBounds(530, 455, 25, 280);
			frame.getContentPane().add(error_list_lowFreq_sc);
			// ======================================
			// LowFreq Box Scroll pane End
			// ======================================

			// ======================================
			// HighFreq Box Scroll pane Begin
			// ======================================
			JLabel error8_label = new JLabel("<html>High<br>Freq</html>");
			error8_label.setBounds(555, 435, 25, 20);
			error8_label.setFont(new Font("Serif", Font.PLAIN, 8));
			error8_label.setHorizontalAlignment(SwingConstants.CENTER);
			frame.getContentPane().add(error8_label);

			error_highFreq_btns_panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
			error_highFreq_btns_panel.setBorder(new EmptyBorder(0, 8, 4, 0));
			error_highFreq_btns_panel.setVisible(true);

			error_list_highFreq_sc = new JScrollPane(error_highFreq_btns_panel, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			// error_list_highFreq_sc.setBorder(new EmptyBorder(0, 0, 0, 0));
			error_list_highFreq_sc.setBorder(border);
			error_list_highFreq_sc.getVerticalScrollBar().setVisible(false);
			error_list_highFreq_sc.getHorizontalScrollBar().setVisible(false);

			error_list_highFreq_sc.setBounds(555, 455, 25, 280);
			frame.getContentPane().add(error_list_highFreq_sc);
			// ======================================
			// HighFreq Box Scroll pane End
			// ======================================

			// ======================================
			// Invalid ID Scroll pane Begin
			// ======================================
			JLabel error9_label = new JLabel("Inv-ID");
			error9_label.setBounds(580, 435, 25, 20);
			error9_label.setFont(new Font("Serif", Font.PLAIN, 8));
			error9_label.setHorizontalAlignment(SwingConstants.CENTER);
			frame.getContentPane().add(error9_label);

			error_invalidID_btns_panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
			error_invalidID_btns_panel.setBorder(new EmptyBorder(0, 8, 4, 0));
			error_invalidID_btns_panel.setVisible(true);

			error_list_invalidID_sc = new JScrollPane(error_invalidID_btns_panel, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			// error_list_invalidID_sc.setBorder(new EmptyBorder(0, 0, 0, 0));
			error_list_invalidID_sc.setBorder(border);
			error_list_invalidID_sc.getVerticalScrollBar().setVisible(false);
			error_list_invalidID_sc.getHorizontalScrollBar().setVisible(false);

			error_list_invalidID_sc.setBounds(580, 455, 25, 280);
			frame.getContentPane().add(error_list_invalidID_sc);
			// ======================================
			// Invalid ID Scroll pane End
			// ======================================

			// ======================================
			// Invalid ID Seq Scroll pane Begin
			// ======================================
			JLabel error10_label = new JLabel("<html><center>Inv-ID<br>Seq</center></html>");
			error10_label.setBounds(605, 435, 25, 20);
			error10_label.setFont(new Font("Serif", Font.PLAIN, 8));
			error10_label.setHorizontalAlignment(SwingConstants.CENTER);
			frame.getContentPane().add(error10_label);

			error_invalidIDSeq_btns_panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
			error_invalidIDSeq_btns_panel.setBorder(new EmptyBorder(0, 8, 4, 0));
			error_invalidIDSeq_btns_panel.setVisible(true);

			error_list_invalidIDSeq_sc = new JScrollPane(error_invalidIDSeq_btns_panel,
					JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			// error_list_invalidIDSeq_sc.setBorder(new EmptyBorder(0, 0, 0,
			// 0));
			error_list_invalidIDSeq_sc.setBorder(border);
			error_list_invalidIDSeq_sc.getVerticalScrollBar().setVisible(false);
			error_list_invalidIDSeq_sc.getHorizontalScrollBar().setVisible(false);

			error_list_invalidIDSeq_sc.setBounds(605, 455, 25, 280);
			frame.getContentPane().add(error_list_invalidIDSeq_sc);
			// ======================================
			// Invalid ID Seq Scroll pane End
			// ======================================

			// ======================================
			// Attack Type Scroll pane Begin
			// ======================================
			JLabel error11_label = new JLabel("Error Type");
			error11_label.setBounds(630, 435, 100, 20);
			error11_label.setFont(new Font("Serif", Font.PLAIN, 12));
			error11_label.setHorizontalAlignment(SwingConstants.CENTER);
			frame.getContentPane().add(error11_label);

			error_finaltype_btns_panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
			error_finaltype_btns_panel.setBorder(new EmptyBorder(0, 8, 4, 0));
			error_finaltype_btns_panel.setVisible(true);

			error_list_finaltype_sc = new JScrollPane(error_finaltype_btns_panel, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			// error_list_finaltype_sc.setBorder(new EmptyBorder(0, 0, 0, 0));
			error_list_finaltype_sc.setBorder(border);
			error_list_finaltype_sc.getVerticalScrollBar().setVisible(false);
			error_list_finaltype_sc.getHorizontalScrollBar().setVisible(false);

			error_list_finaltype_sc.setBounds(630, 455, 100, 280);
			frame.getContentPane().add(error_list_finaltype_sc);
			// ======================================
			// Attack Type Scroll pane End
			// ======================================

			// ======================================
			// Radio Button Scroll pane Begin
			// ======================================
			JLabel error12_label = new JLabel("Sel");
			error12_label.setBounds(730, 435, 25, 20);
			error12_label.setFont(new Font("Serif", Font.PLAIN, 9));
			error12_label.setHorizontalAlignment(SwingConstants.CENTER);
			frame.getContentPane().add(error12_label);

			error_radiobtns_panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
			error_radiobtns_panel.setBorder(new EmptyBorder(0, 8, 4, 0));
			error_radiobtns_panel.setVisible(true);

			error_list_radio_sc = new JScrollPane(error_radiobtns_panel, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			// error_list_radio_sc.setBorder(new EmptyBorder(0, 0, 0, 0));
			error_list_radio_sc.setBorder(border);
			error_list_radio_sc.getVerticalScrollBar().setVisible(false);
			error_list_radio_sc.getHorizontalScrollBar().setVisible(false);

			error_list_radio_sc.setBounds(730, 455, 25, 280);
			frame.getContentPane().add(error_list_radio_sc);
			// ======================================
			// Radio Button Scroll pane End
			// ======================================

			updateDetectionLists uptlists = new updateDetectionLists();
			uptlists.start();

			label_load = new JLabel("Loading... ");
			label_load.setBounds(820, 15, 1025, 20);
			label_load.setFont(new Font("Serif", Font.BOLD, 15));
			label_load.setHorizontalAlignment(SwingConstants.LEFT);
			label_load.setVisible(false);
			frame.getContentPane().add(label_load);

			label_per_abnormal = new JLabel("Detail Information");
			label_per_abnormal.setBounds(820, 70, 1025, 20);
			label_per_abnormal.setFont(new Font("Serif", Font.BOLD, 15));
			label_per_abnormal.setHorizontalAlignment(SwingConstants.LEFT);
			label_per_abnormal.setVisible(false);
			frame.getContentPane().add(label_per_abnormal);

			det_label1 = new JLabel("Bus-Load : ");
			det_label1.setBounds(900, 180, 200, 20);
			det_label1.setFont(new Font("Serif", Font.BOLD, 15));
			det_label1.setHorizontalAlignment(SwingConstants.LEFT);
			frame.getContentPane().add(det_label1);
			det_label1.setVisible(false);

			det_UpBusLoad = new JLabel("");
			det_UpBusLoad.setBounds(1100, 180, 250, 20);
			det_UpBusLoad.setFont(new Font("Serif", Font.BOLD, 15));
			det_UpBusLoad.setHorizontalAlignment(SwingConstants.LEFT);
			det_UpBusLoad.setBackground(Color.white);
			frame.getContentPane().add(det_UpBusLoad);
			det_UpBusLoad.setVisible(false);

			det_label2 = new JLabel("Number of Messages : ");
			det_label2.setBounds(1350, 180, 200, 20);
			det_label2.setFont(new Font("Serif", Font.BOLD, 15));
			det_label2.setHorizontalAlignment(SwingConstants.LEFT);
			frame.getContentPane().add(det_label2);
			det_label2.setVisible(false);

			det_UpNumOfMessage = new JLabel("");
			det_UpNumOfMessage.setBounds(1550, 180, 250, 20);
			det_UpNumOfMessage.setFont(new Font("Serif", Font.BOLD, 15));
			det_UpNumOfMessage.setHorizontalAlignment(SwingConstants.LEFT);
			det_UpNumOfMessage.setBackground(Color.white);
			frame.getContentPane().add(det_UpNumOfMessage);
			det_UpNumOfMessage.setVisible(false);

			det_label3 = new JLabel("Number of CAN IDs : ");
			det_label3.setBounds(900, 205, 200, 20);
			det_label3.setFont(new Font("Serif", Font.BOLD, 15));
			det_label3.setHorizontalAlignment(SwingConstants.LEFT);
			frame.getContentPane().add(det_label3);
			det_label3.setVisible(false);

			det_UpNumOfIDs = new JLabel("");
			det_UpNumOfIDs.setBounds(1100, 205, 250, 20);
			det_UpNumOfIDs.setFont(new Font("Serif", Font.BOLD, 15));
			det_UpNumOfIDs.setHorizontalAlignment(SwingConstants.LEFT);
			det_UpNumOfIDs.setBackground(Color.white);
			frame.getContentPane().add(det_UpNumOfIDs);
			det_UpNumOfIDs.setVisible(false);

			// det_label4 = new JLabel("Self Similarity");
			// det_label4.setBounds(1350, 205, 200, 20);
			// det_label4.setFont(new Font("Serif", Font.BOLD, 15));
			// det_label4.setHorizontalAlignment(SwingConstants.LEFT);
			// frame.getContentPane().add(det_label4);
			// det_label4.setVisible(false);
			//
			// det_UpSelfsimilarity = new JLabel("");
			// det_UpSelfsimilarity.setBounds(1550, 205, 250, 20);
			// det_UpSelfsimilarity.setFont(new Font("Serif", Font.BOLD, 15));
			// det_UpSelfsimilarity.setHorizontalAlignment(SwingConstants.LEFT);
			// det_UpSelfsimilarity.setBackground(Color.white);
			// frame.getContentPane().add(det_UpSelfsimilarity);
			// det_UpSelfsimilarity.setVisible(false);

			//det_CanModel = new DefaultTableModel(s1080p_colnames, 0);
			det_CanModel = new DefaultTableModel(s1080p_colnames, 0)
			{
				private static final long serialVersionUID = 2202263851649136106L;

				@Override
			    public boolean isCellEditable(int row, int column) {
			       return false;
			    }
			};
			det_CanTable = new JTable(det_CanModel) {
				private static final long serialVersionUID = 8047923611201469817L;

				DefaultTableCellRenderer renderCenter = new DefaultTableCellRenderer();
				{
					renderCenter.setHorizontalAlignment(SwingConstants.CENTER);
				}

				@Override
				public TableCellRenderer getCellRenderer(int arg0, int arg1) {
					return renderCenter;
				}
			};
			det_CanTable.addMouseListener(new MouseAdapter() {
			    public void mousePressed(MouseEvent mouseEvent)
			    {
			    	try{
				        JTable table =(JTable) mouseEvent.getSource();
				        Point point = mouseEvent.getPoint();
				        
				        int row = table.rowAtPoint(point);
				        int col = table.columnAtPoint(point);
	
				        String IDvalue = (String) table.getValueAt(row, col);
				        int loc = progressBar2.getValue();
				        
				        if ( (mouseEvent.getClickCount() == 2) && (row >= 0) && (col == 0) )
				        {
				        	//System.out.println("R : " + row + " / C : " + col + " / Val : " + IDvalue);
				        	new IDWindow(IDvalue, loc);	
				        }
			    	}
				    catch(Exception e)
			    	{
				    	
			    	}
			    }
			});
			

			det_scrollPane = new JScrollPane(det_CanTable);
			det_scrollPane.setBounds(820, 230, 1025, 480);
			frame.getContentPane().add(det_scrollPane);
			det_scrollPane.setVisible(false);

			det_ascending = new JCheckBox("Ascending Order");
			det_ascending.setFont(new Font("Serif", Font.BOLD, 13));
			det_ascending.setBounds(1730, 715, 200, 30);
			det_ascending.setSelected(true);
			frame.getContentPane().add(det_ascending);
			det_ascending.setVisible(false);

			previous = new JButton("Move to Prev TimeSlot");
			previous.setFont(new Font("Serif", Font.BOLD, 16));
			previous.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (!progressBar2.isMinimumSizeSet())
						progressBar2.setValue(progressBar2.getValue() - 1);

					int loc = progressBar2.getValue();

					ArrayList<TotalFeaturesObject> total_list = offanalyzer.get_total_list();

					progressBar2.setString("Focused Time : " + total_list.get(loc).f1_num_of_packets.getTimestamp());

					det_UpBusLoad.setText(total_list.get(loc).f2_bus_load.getValue());
					det_UpNumOfIDs.setText(total_list.get(loc).f3_number_of_IDs.getValue());
					det_UpNumOfMessage.setText(total_list.get(loc).f1_num_of_packets.getValue());
					// det_UpSelfsimilarity.setText("Not Yet");

					HashMap<String, ShowObject> datas = total_list.get(loc).f10_IDfeatures;

					det_CanModel = new DefaultTableModel(s1080p_colnames, 0)
					{
						private static final long serialVersionUID = 2202263851649136106L;

						@Override
					    public boolean isCellEditable(int row, int column) {
					       return false;
					    }
					};
					det_CanTable.setModel(det_CanModel);

					if (det_ascending.isSelected()) {
						TreeMap<String, ShowObject> tm = new TreeMap<String, ShowObject>(datas);

						Iterator<String> iteratorKey = tm.keySet().iterator();

						while (iteratorKey.hasNext()) {
							String key = iteratorKey.next();
							ShowObject value = ((ShowObject) tm.get(key));

							Object[] row = { key, String.format("%.1f", value.freq), value.elapsed_time, value.count,
									value.data_length, value.data, value.timestamp };
							det_CanModel.addRow(row);
						}
					} else {
						for (Map.Entry<String, ShowObject> entry : datas.entrySet()) {
							Object[] row = { entry.getKey(),
									String.format("%.1f", ((ShowObject) entry.getValue()).freq),
									((ShowObject) entry.getValue()).elapsed_time, ((ShowObject) entry.getValue()).count,
									((ShowObject) entry.getValue()).data_length, ((ShowObject) entry.getValue()).data,
									((ShowObject) entry.getValue()).timestamp };
							det_CanModel.addRow(row);
						}
					}

					det_CanModel.fireTableDataChanged();
				}
			});
			previous.setBounds(1055, 130, 250, 25);
			previous.setVisible(false);
			frame.getContentPane().add(previous);

			next = new JButton("Move to Next TimeSlot");
			next.setFont(new Font("Serif", Font.BOLD, 16));
			next.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (!progressBar2.isMaximumSizeSet())
						progressBar2.setValue(progressBar2.getValue() + 1);

					int loc = progressBar2.getValue();

					ArrayList<TotalFeaturesObject> total_list = offanalyzer.get_total_list();

					progressBar2.setString("Focused Time : " + total_list.get(loc).f1_num_of_packets.getTimestamp());

					det_UpBusLoad.setText(total_list.get(loc).f2_bus_load.getValue());
					det_UpNumOfIDs.setText(total_list.get(loc).f3_number_of_IDs.getValue());
					det_UpNumOfMessage.setText(total_list.get(loc).f1_num_of_packets.getValue());
					// det_UpSelfsimilarity.setText("Not Yet");

					HashMap<String, ShowObject> datas = total_list.get(loc).f10_IDfeatures;

					det_CanModel = new DefaultTableModel(s1080p_colnames, 0)
					{
						private static final long serialVersionUID = 2202263851649136106L;

						@Override
					    public boolean isCellEditable(int row, int column) {
					       return false;
					    }
					};
					det_CanTable.setModel(det_CanModel);

					if (det_ascending.isSelected()) {
						TreeMap<String, ShowObject> tm = new TreeMap<String, ShowObject>(datas);

						Iterator<String> iteratorKey = tm.keySet().iterator();

						while (iteratorKey.hasNext()) {
							String key = iteratorKey.next();
							ShowObject value = ((ShowObject) tm.get(key));

							Object[] row = { key, String.format("%.1f", value.freq), value.elapsed_time, value.count,
									value.data_length, value.data, value.timestamp };
							det_CanModel.addRow(row);
						}
					} else {
						for (Map.Entry<String, ShowObject> entry : datas.entrySet()) {
							Object[] row = { entry.getKey(),
									String.format("%.1f", ((ShowObject) entry.getValue()).freq),
									((ShowObject) entry.getValue()).elapsed_time, ((ShowObject) entry.getValue()).count,
									((ShowObject) entry.getValue()).data_length, ((ShowObject) entry.getValue()).data,
									((ShowObject) entry.getValue()).timestamp };
							det_CanModel.addRow(row);
						}
					}

					det_CanModel.fireTableDataChanged();
				}
			});
			next.setBounds(1355, 130, 250, 25);
			next.setVisible(false);
			frame.getContentPane().add(next);
		}

		public void set_attack_scroll(int value) {
			attack_list_lowbus_sc.getVerticalScrollBar().setValue(value);
			attack_list_highbus_sc.getVerticalScrollBar().setValue(value);
			attack_list_lessNoP_sc.getVerticalScrollBar().setValue(value);
			attack_list_moreNoP_sc.getVerticalScrollBar().setValue(value);
			attack_list_lessNoID_sc.getVerticalScrollBar().setValue(value);
			attack_list_moreNoID_sc.getVerticalScrollBar().setValue(value);
			attack_list_lowFreq_sc.getVerticalScrollBar().setValue(value);
			attack_list_highFreq_sc.getVerticalScrollBar().setValue(value);
			attack_list_invalidID_sc.getVerticalScrollBar().setValue(value);
			attack_list_invalidIDSeq_sc.getVerticalScrollBar().setValue(value);
			attack_list_finaltype_sc.getVerticalScrollBar().setValue(value);
			attack_list_radio_sc.getVerticalScrollBar().setValue(value);
		}

		public void set_error_scroll(int value) {
			error_list_lowbus_sc.getVerticalScrollBar().setValue(value);
			error_list_highbus_sc.getVerticalScrollBar().setValue(value);
			error_list_lessNoP_sc.getVerticalScrollBar().setValue(value);
			error_list_moreNoP_sc.getVerticalScrollBar().setValue(value);
			error_list_lessNoID_sc.getVerticalScrollBar().setValue(value);
			error_list_moreNoID_sc.getVerticalScrollBar().setValue(value);
			error_list_lowFreq_sc.getVerticalScrollBar().setValue(value);
			error_list_highFreq_sc.getVerticalScrollBar().setValue(value);
			error_list_invalidID_sc.getVerticalScrollBar().setValue(value);
			error_list_invalidIDSeq_sc.getVerticalScrollBar().setValue(value);
			error_list_finaltype_sc.getVerticalScrollBar().setValue(value);
			error_list_radio_sc.getVerticalScrollBar().setValue(value);
		}

		class offAnalyzer_Monitor extends Thread {
			public void run() {
				while (true) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					if ((offanalyzer != null) && offanalyzer.isEnd) {
						label_load.setText("Loading... Complete");
						label_per_abnormal.setVisible(true);

						ArrayList<TotalFeaturesObject> total_list = offanalyzer.get_total_list();

						progressBar2 = new JProgressBar(0, total_list.size() - 1);
						progressBar2.setValue(0);
						progressBar2.setStringPainted(true);
						progressBar2.setBounds(820, 100, 1025, 20);
						progressBar2.setString("Focused Time : " + total_list.get(0).f1_num_of_packets.getTimestamp());
						frame.getContentPane().add(progressBar2);
						progressBar2.setVisible(true);

						previous.setVisible(true);
						next.setVisible(true);

						det_label1.setVisible(true);
						det_label2.setVisible(true);
						det_label3.setVisible(true);
						// det_label4.setVisible(true);

						det_UpBusLoad.setText(total_list.get(0).f2_bus_load.getValue());
						det_UpNumOfIDs.setText(total_list.get(0).f3_number_of_IDs.getValue());
						det_UpNumOfMessage.setText(total_list.get(0).f1_num_of_packets.getValue());
						// det_UpSelfsimilarity.setText("Not Yet");

						det_UpBusLoad.setVisible(true);
						det_UpNumOfIDs.setVisible(true);
						det_UpNumOfMessage.setVisible(true);
						// det_UpSelfsimilarity.setVisible(true);

						HashMap<String, ShowObject> datas = total_list.get(0).f10_IDfeatures;

						det_CanModel = new DefaultTableModel(s1080p_colnames, 0)
						{
							private static final long serialVersionUID = 2202263851649136106L;

							@Override
						    public boolean isCellEditable(int row, int column) {
						       return false;
						    }
						};
						det_CanTable.setModel(det_CanModel);

						if (det_ascending.isSelected()) {
							TreeMap<String, ShowObject> tm = new TreeMap<String, ShowObject>(datas);

							Iterator<String> iteratorKey = tm.keySet().iterator();

							while (iteratorKey.hasNext()) {
								String key = iteratorKey.next();
								ShowObject value = ((ShowObject) tm.get(key));

								Object[] row = { key,
										// String.format("%.1f", value.freq),
										String.format("%.1f",
												Monitor.normal_condition.f5_frequence_per_ID.get(key.toUpperCase())),
										value.elapsed_time, value.count, value.data_length, value.data,
										value.timestamp };
								det_CanModel.addRow(row);
							}
						} else {
							for (Map.Entry<String, ShowObject> entry : datas.entrySet()) {
								Object[] row = { entry.getKey(),
										// String.format("%.1f",
										// ((ShowObject)entry.getValue()).freq),
										String.format("%.1f",
												Monitor.normal_condition.f5_frequence_per_ID.get(entry.getKey())),
										((ShowObject) entry.getValue()).elapsed_time,
										((ShowObject) entry.getValue()).count,
										((ShowObject) entry.getValue()).data_length,
										((ShowObject) entry.getValue()).data,
										((ShowObject) entry.getValue()).timestamp };
								det_CanModel.addRow(row);

							}
						}

						det_scrollPane.setVisible(true);
						det_ascending.setVisible(true);

						break;
					}
				}
			}
		}

		class paintProgressBar extends Thread {
			public void run() {
				while (true) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					if (offanalyzer != null) {
						progressBar.setValue(offanalyzer.progress_count);

						if (progressBar.isMaximumSizeSet())
							break;
					}
				}
			}
		}

		class updateDetectionLists extends Thread {
			public void run() {
				ActionListener attack_radioActionListener = new ActionListener() {
					public void actionPerformed(ActionEvent actionEvent) {
						AbstractButton aButton = (AbstractButton) actionEvent.getSource();

						int row = Integer.parseInt(aButton.getActionCommand());
						
						//Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
						frame.setSize(1880, 800);						
//						Dimension f1_size = frame.getSize();
//						int left = (screen.width / 2) - (f1_size.width / 2);
//						int top = (screen.height / 2) - (f1_size.height / 2);
//						frame.setLocation(left, top);

						long start = (long) attack_Tbl.getModel().getValueAt(row, 0);
						long end = (long) attack_Tbl.getModel().getValueAt(row, 1);

						label_per_abnormal.setVisible(false);
						if (progressBar2 != null) {
							progressBar2.setVisible(false);
							frame.remove(progressBar2);
						}
						previous.setVisible(false);
						next.setVisible(false);
						det_scrollPane.setVisible(false);
						det_ascending.setVisible(false);

						det_label1.setVisible(false);
						det_label2.setVisible(false);
						det_label3.setVisible(false);
						// det_label4.setVisible(false);

						det_UpBusLoad.setVisible(false);
						det_UpNumOfIDs.setVisible(false);
						det_UpNumOfMessage.setVisible(false);
						// det_UpSelfsimilarity.setVisible(false);

						label_load.setVisible(true);

						progressBar = new JProgressBar(0, ((int) ((end - Monitor.veryFirstTime) / 1000000)) + 2);
						progressBar.setValue(0);
						progressBar.setStringPainted(true);
						progressBar.setBounds(820, 35, 1025, 20);
						progressBar.setVisible(true);
						frame.getContentPane().add(progressBar);

						offanalyzer = new Offline_Analyzer(start, end);
						offanalyzer_monitor = new offAnalyzer_Monitor();
						paintProgressBar paint = new paintProgressBar();

						offanalyzer_monitor.start();
						paint.start();
						offanalyzer.start();
					}
				};

				ActionListener error_radioActionListener = new ActionListener() {
					public void actionPerformed(ActionEvent actionEvent) {
						AbstractButton aButton = (AbstractButton) actionEvent.getSource();

						int row = Integer.parseInt(aButton.getActionCommand());
						
						//Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
						frame.setSize(1880, 800);						
//						Dimension f1_size = frame.getSize();
//						int left = (screen.width / 2) - (f1_size.width / 2);
//						int top = (screen.height / 2) - (f1_size.height / 2);
//						frame.setLocation(left, top);

						long start = (long) error_Tbl.getModel().getValueAt(row, 0);
						long end = (long) error_Tbl.getModel().getValueAt(row, 1);

						label_per_abnormal.setVisible(false);
						if (progressBar2 != null) {
							progressBar2.setVisible(false);
							frame.remove(progressBar2);
						}
						previous.setVisible(false);
						next.setVisible(false);
						det_scrollPane.setVisible(false);
						det_ascending.setVisible(false);

						det_label1.setVisible(false);
						det_label2.setVisible(false);
						det_label3.setVisible(false);
						// det_label4.setVisible(false);

						det_UpBusLoad.setVisible(false);
						det_UpNumOfIDs.setVisible(false);
						det_UpNumOfMessage.setVisible(false);
						// det_UpSelfsimilarity.setVisible(false);

						label_load.setVisible(true);

						progressBar = new JProgressBar(0, ((int) ((end - Monitor.veryFirstTime) / 1000000)) + 2);
						progressBar.setValue(0);
						progressBar.setStringPainted(true);
						progressBar.setBounds(820, 35, 1025, 20);
						progressBar.setVisible(true);
						frame.getContentPane().add(progressBar);

						offanalyzer = new Offline_Analyzer(start, end);
						offanalyzer_monitor = new offAnalyzer_Monitor();
						paintProgressBar paint = new paintProgressBar();

						offanalyzer_monitor.start();
						paint.start();
						offanalyzer.start();
					}
				};

				HighlightOnSelectIcon icon = new HighlightOnSelectIcon();				

				while (true) {
					try {
						TimeUnit.MILLISECONDS.sleep(refresh_freq);
					} catch (Exception e) {
						e.printStackTrace();
					}

					if ((Monitor.anomaly_interval != null) && going )
					{
						ArrayList<AnomalObject> list = Monitor.anomaly_interval.getList();

						attack_tbl_model = new DefaultTableModel(detcolName, 0);
						attack_Tbl.setModel(attack_tbl_model);

						attack_radiobtns_panel.removeAll();
						attack_radiobtns_panel.setPreferredSize(new Dimension(30, list.size() * 18));

						attack_lowbus_btns_panel.removeAll();
						attack_lowbus_btns_panel.setPreferredSize(new Dimension(30, list.size() * 18));

						attack_highbus_btns_panel.removeAll();
						attack_highbus_btns_panel.setPreferredSize(new Dimension(30, list.size() * 18));

						attack_lessNoP_btns_panel.removeAll();
						attack_lessNoP_btns_panel.setPreferredSize(new Dimension(30, list.size() * 18));

						attack_moreNoP_btns_panel.removeAll();
						attack_moreNoP_btns_panel.setPreferredSize(new Dimension(30, list.size() * 18));

						attack_lessNoID_btns_panel.removeAll();
						attack_lessNoID_btns_panel.setPreferredSize(new Dimension(30, list.size() * 18));

						attack_moreNoID_btns_panel.removeAll();
						attack_moreNoID_btns_panel.setPreferredSize(new Dimension(30, list.size() * 18));

						attack_lowFreq_btns_panel.removeAll();
						attack_lowFreq_btns_panel.setPreferredSize(new Dimension(30, list.size() * 18));

						attack_highFreq_btns_panel.removeAll();
						attack_highFreq_btns_panel.setPreferredSize(new Dimension(30, list.size() * 18));

						attack_invalidID_btns_panel.removeAll();
						attack_invalidID_btns_panel.setPreferredSize(new Dimension(30, list.size() * 18));

						attack_invalidIDSeq_btns_panel.removeAll();
						attack_invalidIDSeq_btns_panel.setPreferredSize(new Dimension(30, list.size() * 18));

						attack_finaltype_btns_panel.removeAll();
						attack_finaltype_btns_panel.setPreferredSize(new Dimension(100, list.size() * 18));

						String groupselection = null;
						try {
							groupselection = attack_radiogroup.getSelection().getActionCommand();
						} catch (Exception e) {

						}

						attack_radio_buttons = new JRadioButton[list.size()];
						attack_radiogroup = new ButtonGroup();

						attack_lowbus_boxes = new JCheckBox[list.size()];
						attack_highbus_boxes = new JCheckBox[list.size()];
						attack_lessNoP_boxes = new JCheckBox[list.size()];
						attack_moreNoP_boxes = new JCheckBox[list.size()];
						attack_lessNoID_boxes = new JCheckBox[list.size()];
						attack_moreNoID_boxes = new JCheckBox[list.size()];
						attack_lowFreq_boxes = new JCheckBox[list.size()];
						attack_highFreq_boxes = new JCheckBox[list.size()];
						attack_invalidID_boxes = new JCheckBox[list.size()];
						attack_invalidIDSeq_boxes = new JCheckBox[list.size()];
						attack_finaltype_labels = new JLabel[list.size()];

						for (int i = 0; i < list.size(); i++) {
							Object[] row = { list.get(i).getStartTime(), list.get(i).getEndTime(),
									((double) (list.get(i).getEndTime() - list.get(i).getStartTime()) / 1000000) };
							attack_tbl_model.addRow(row);

							attack_radio_buttons[i] = new JRadioButton();
							attack_radio_buttons[i].setActionCommand(Integer.toString(i));
							attack_radio_buttons[i].setSelected(false);
							attack_radio_buttons[i].setSize(10, 5);
							attack_radio_buttons[i].setVisible(true);
							attack_radio_buttons[i].addActionListener(attack_radioActionListener);
							attack_radio_buttons[i].setBorder(new EmptyBorder(3, 5, 2, 20));
							attack_radiogroup.add(attack_radio_buttons[i]);

							attack_radiobtns_panel.add(attack_radio_buttons[i]);

							attack_lowbus_boxes[i] = new JCheckBox();
							attack_lowbus_boxes[i].setSize(10, 5);
							attack_lowbus_boxes[i].setVisible(true);
							attack_lowbus_boxes[i].setBorder(new EmptyBorder(3, 5, 2, 20));
							attack_lowbus_boxes[i].setEnabled(false);
							attack_lowbus_boxes[i].setIcon(icon);
							//attack_lowbus_boxes[i].setIcon(new ImageIcon(fimage));
							//attack_lowbus_boxes[i].setSelectedIcon(new ImageIcon(timage));
							if ((list.get(i).getType() & AnomalDetector.Low_Busload_SIG) == AnomalDetector.Low_Busload_SIG)
								attack_lowbus_boxes[i].setSelected(true);
							else
								attack_lowbus_boxes[i].setSelected(false);
							attack_lowbus_btns_panel.add(attack_lowbus_boxes[i]);

							attack_highbus_boxes[i] = new JCheckBox();
							attack_highbus_boxes[i].setSize(10, 5);
							attack_highbus_boxes[i].setVisible(true);
							attack_highbus_boxes[i].setBorder(new EmptyBorder(3, 5, 2, 20));
							attack_highbus_boxes[i].setEnabled(false);
							attack_highbus_boxes[i].setIcon(icon);
							//attack_highbus_boxes[i].setIcon(new ImageIcon(fimage));
							//attack_highbus_boxes[i].setSelectedIcon(new ImageIcon(timage));
							if ((list.get(i).getType()
									& AnomalDetector.High_Busload_SIG) == AnomalDetector.High_Busload_SIG)
								attack_highbus_boxes[i].setSelected(true);
							else
								attack_highbus_boxes[i].setSelected(false);
							attack_highbus_btns_panel.add(attack_highbus_boxes[i]);

							attack_lessNoP_boxes[i] = new JCheckBox();
							attack_lessNoP_boxes[i].setSize(10, 5);
							attack_lessNoP_boxes[i].setVisible(true);
							attack_lessNoP_boxes[i].setBorder(new EmptyBorder(3, 5, 2, 20));
							attack_lessNoP_boxes[i].setEnabled(false);
							attack_lessNoP_boxes[i].setIcon(icon);
							//attack_lessNoP_boxes[i].setIcon(new ImageIcon(fimage));
							//attack_lessNoP_boxes[i].setSelectedIcon(new ImageIcon(timage));
							if ((list.get(i).getType()
									& AnomalDetector.Less_Num_of_Packets_SIG) == AnomalDetector.Less_Num_of_Packets_SIG)
								attack_lessNoP_boxes[i].setSelected(true);
							else
								attack_lessNoP_boxes[i].setSelected(false);
							attack_lessNoP_btns_panel.add(attack_lessNoP_boxes[i]);

							attack_moreNoP_boxes[i] = new JCheckBox();
							attack_moreNoP_boxes[i].setSize(10, 5);
							attack_moreNoP_boxes[i].setVisible(true);
							attack_moreNoP_boxes[i].setBorder(new EmptyBorder(3, 5, 2, 20));
							attack_moreNoP_boxes[i].setEnabled(false);
							attack_moreNoP_boxes[i].setIcon(icon);
							//attack_moreNoP_boxes[i].setIcon(new ImageIcon(fimage));
							//attack_moreNoP_boxes[i].setSelectedIcon(new ImageIcon(timage));
							if ((list.get(i).getType()
									& AnomalDetector.More_Num_of_Packets_SIG) == AnomalDetector.More_Num_of_Packets_SIG)
								attack_moreNoP_boxes[i].setSelected(true);
							else
								attack_moreNoP_boxes[i].setSelected(false);
							attack_moreNoP_btns_panel.add(attack_moreNoP_boxes[i]);

							attack_lessNoID_boxes[i] = new JCheckBox();
							attack_lessNoID_boxes[i].setSize(10, 5);
							attack_lessNoID_boxes[i].setVisible(true);
							attack_lessNoID_boxes[i].setBorder(new EmptyBorder(3, 5, 2, 20));
							attack_lessNoID_boxes[i].setEnabled(false);
							attack_lessNoID_boxes[i].setIcon(icon);
							//attack_lessNoID_boxes[i].setIcon(new ImageIcon(fimage));
							//attack_lessNoID_boxes[i].setSelectedIcon(new ImageIcon(timage));
							if ((list.get(i).getType()
									& AnomalDetector.Less_Num_of_IDs_SIG) == AnomalDetector.Less_Num_of_IDs_SIG)
								attack_lessNoID_boxes[i].setSelected(true);
							else
								attack_lessNoID_boxes[i].setSelected(false);
							attack_lessNoID_btns_panel.add(attack_lessNoID_boxes[i]);

							attack_moreNoID_boxes[i] = new JCheckBox();
							attack_moreNoID_boxes[i].setSize(10, 5);
							attack_moreNoID_boxes[i].setVisible(true);
							attack_moreNoID_boxes[i].setBorder(new EmptyBorder(3, 5, 2, 20));
							attack_moreNoID_boxes[i].setEnabled(false);
							attack_moreNoID_boxes[i].setIcon(icon);
							//attack_moreNoID_boxes[i].setIcon(new ImageIcon(fimage));
							//attack_moreNoID_boxes[i].setSelectedIcon(new ImageIcon(timage));
							if ((list.get(i).getType()
									& AnomalDetector.More_Num_of_IDs_SIG) == AnomalDetector.More_Num_of_IDs_SIG)
								attack_moreNoID_boxes[i].setSelected(true);
							else
								attack_moreNoID_boxes[i].setSelected(false);
							attack_moreNoID_btns_panel.add(attack_moreNoID_boxes[i]);

							attack_lowFreq_boxes[i] = new JCheckBox();
							attack_lowFreq_boxes[i].setSize(10, 5);
							attack_lowFreq_boxes[i].setVisible(true);
							attack_lowFreq_boxes[i].setBorder(new EmptyBorder(3, 5, 2, 20));
							attack_lowFreq_boxes[i].setEnabled(false);
							attack_lowFreq_boxes[i].setIcon(icon);
							//attack_lowFreq_boxes[i].setIcon(new ImageIcon(fimage));
							//attack_lowFreq_boxes[i].setSelectedIcon(new ImageIcon(timage));
							if ((list.get(i).getType()
									& AnomalDetector.Low_Frequence_SIG) == AnomalDetector.Low_Frequence_SIG)
								attack_lowFreq_boxes[i].setSelected(true);
							else
								attack_lowFreq_boxes[i].setSelected(false);
							attack_lowFreq_btns_panel.add(attack_lowFreq_boxes[i]);

							attack_highFreq_boxes[i] = new JCheckBox();
							attack_highFreq_boxes[i].setSize(10, 5);
							attack_highFreq_boxes[i].setVisible(true);
							attack_highFreq_boxes[i].setBorder(new EmptyBorder(3, 5, 2, 20));
							attack_highFreq_boxes[i].setEnabled(false);
							attack_highFreq_boxes[i].setIcon(icon);
							//attack_highFreq_boxes[i].setIcon(new ImageIcon(fimage));
							//attack_highFreq_boxes[i].setSelectedIcon(new ImageIcon(timage));
							if ((list.get(i).getType()
									& AnomalDetector.High_Frequence_SIG) == AnomalDetector.High_Frequence_SIG)
								attack_highFreq_boxes[i].setSelected(true);
							else
								attack_highFreq_boxes[i].setSelected(false);
							attack_highFreq_btns_panel.add(attack_highFreq_boxes[i]);

							attack_invalidID_boxes[i] = new JCheckBox();
							attack_invalidID_boxes[i].setSize(10, 5);
							attack_invalidID_boxes[i].setVisible(true);
							attack_invalidID_boxes[i].setBorder(new EmptyBorder(3, 5, 2, 20));
							attack_invalidID_boxes[i].setEnabled(false);
							attack_invalidID_boxes[i].setIcon(icon);
							//attack_invalidID_boxes[i].setIcon(new ImageIcon(fimage));
							//attack_invalidID_boxes[i].setSelectedIcon(new ImageIcon(timage));
							if ((list.get(i).getType()
									& AnomalDetector.Invalid_ID_SIG) == AnomalDetector.Invalid_ID_SIG)
								attack_invalidID_boxes[i].setSelected(true);
							else
								attack_invalidID_boxes[i].setSelected(false);
							attack_invalidID_btns_panel.add(attack_invalidID_boxes[i]);

							attack_invalidIDSeq_boxes[i] = new JCheckBox();
							attack_invalidIDSeq_boxes[i].setSize(10, 5);
							attack_invalidIDSeq_boxes[i].setVisible(true);
							attack_invalidIDSeq_boxes[i].setBorder(new EmptyBorder(3, 5, 2, 20));
							attack_invalidIDSeq_boxes[i].setEnabled(false);
							attack_invalidIDSeq_boxes[i].setIcon(icon);
							//attack_invalidIDSeq_boxes[i].setIcon(new ImageIcon(fimage));
							//attack_invalidIDSeq_boxes[i].setSelectedIcon(new ImageIcon(timage));
							if ((list.get(i).getType()
									& AnomalDetector.Invalid_ID_Sequence_SIG) == AnomalDetector.Invalid_ID_Sequence_SIG)
								attack_invalidIDSeq_boxes[i].setSelected(true);
							else
								attack_invalidIDSeq_boxes[i].setSelected(false);
							attack_invalidIDSeq_btns_panel.add(attack_invalidIDSeq_boxes[i]);

							attack_finaltype_labels[i] = new JLabel();
							attack_finaltype_labels[i].setSize(100, 5);
							attack_finaltype_labels[i].setVisible(true);
							attack_finaltype_labels[i].setBorder(new EmptyBorder(2, 3, 2, 10));
							attack_finaltype_labels[i].setText(list.get(i).getAttackCategory());
							attack_finaltype_labels[i].setFont(new Font("Serif", Font.BOLD, 10));
							attack_finaltype_btns_panel.add(attack_finaltype_labels[i]);

						}

						if (groupselection != null)
							attack_radio_buttons[Integer.parseInt(groupselection)].setSelected(true);
						

						ArrayList<AnomalObject> list_error = Monitor.anomaly_interval.getList2();

						error_tbl_model = new DefaultTableModel(detcolName, 0);
						error_Tbl.setModel(error_tbl_model);

						error_radiobtns_panel.removeAll();
						error_radiobtns_panel.setPreferredSize(new Dimension(30, list_error.size() * 18));

						error_lowbus_btns_panel.removeAll();
						error_lowbus_btns_panel.setPreferredSize(new Dimension(30, list_error.size() * 18));

						error_highbus_btns_panel.removeAll();
						error_highbus_btns_panel.setPreferredSize(new Dimension(30, list_error.size() * 18));

						error_lessNoP_btns_panel.removeAll();
						error_lessNoP_btns_panel.setPreferredSize(new Dimension(30, list_error.size() * 18));

						error_moreNoP_btns_panel.removeAll();
						error_moreNoP_btns_panel.setPreferredSize(new Dimension(30, list_error.size() * 18));

						error_lessNoID_btns_panel.removeAll();
						error_lessNoID_btns_panel.setPreferredSize(new Dimension(30, list_error.size() * 18));

						error_moreNoID_btns_panel.removeAll();
						error_moreNoID_btns_panel.setPreferredSize(new Dimension(30, list_error.size() * 18));

						error_lowFreq_btns_panel.removeAll();
						error_lowFreq_btns_panel.setPreferredSize(new Dimension(30, list_error.size() * 18));

						error_highFreq_btns_panel.removeAll();
						error_highFreq_btns_panel.setPreferredSize(new Dimension(30, list_error.size() * 18));

						error_invalidID_btns_panel.removeAll();
						error_invalidID_btns_panel.setPreferredSize(new Dimension(30, list_error.size() * 18));

						error_invalidIDSeq_btns_panel.removeAll();
						error_invalidIDSeq_btns_panel.setPreferredSize(new Dimension(30, list_error.size() * 18));

						error_finaltype_btns_panel.removeAll();
						error_finaltype_btns_panel.setPreferredSize(new Dimension(100, list_error.size() * 18));

						String error_groupselection = null;
						try {
							error_groupselection = error_radiogroup.getSelection().getActionCommand();
						} catch (Exception e) {

						}

						error_radio_buttons = new JRadioButton[list_error.size()];
						error_radiogroup = new ButtonGroup();

						error_lowbus_boxes = new JCheckBox[list_error.size()];
						error_highbus_boxes = new JCheckBox[list_error.size()];
						error_lessNoP_boxes = new JCheckBox[list_error.size()];
						error_moreNoP_boxes = new JCheckBox[list_error.size()];
						error_lessNoID_boxes = new JCheckBox[list_error.size()];
						error_moreNoID_boxes = new JCheckBox[list_error.size()];
						error_lowFreq_boxes = new JCheckBox[list_error.size()];
						error_highFreq_boxes = new JCheckBox[list_error.size()];
						error_invalidID_boxes = new JCheckBox[list_error.size()];
						error_invalidIDSeq_boxes = new JCheckBox[list_error.size()];
						error_finaltype_labels = new JLabel[list_error.size()];

						for (int i = 0; i < list_error.size(); i++) {
							Object[] row = { list_error.get(i).getStartTime(), list_error.get(i).getEndTime(),
									((double) (list_error.get(i).getEndTime() - list_error.get(i).getStartTime())
											/ 1000000) };
							error_tbl_model.addRow(row);

							error_radio_buttons[i] = new JRadioButton();
							error_radio_buttons[i].setActionCommand(Integer.toString(i));
							error_radio_buttons[i].setSelected(false);
							error_radio_buttons[i].setSize(10, 5);
							error_radio_buttons[i].setVisible(true);
							error_radio_buttons[i].addActionListener(error_radioActionListener);
							error_radio_buttons[i].setBorder(new EmptyBorder(3, 5, 2, 20));
							error_radiogroup.add(error_radio_buttons[i]);

							error_radiobtns_panel.add(error_radio_buttons[i]);

							error_lowbus_boxes[i] = new JCheckBox();
							error_lowbus_boxes[i].setSize(10, 5);
							error_lowbus_boxes[i].setVisible(true);
							error_lowbus_boxes[i].setBorder(new EmptyBorder(3, 5, 2, 20));
							error_lowbus_boxes[i].setEnabled(false);
							error_lowbus_boxes[i].setIcon(icon);
							if ((list_error.get(i).getType()
									& AnomalDetector.Low_Busload_SIG) == AnomalDetector.Low_Busload_SIG)
								error_lowbus_boxes[i].setSelected(true);
							else
								error_lowbus_boxes[i].setSelected(false);
							error_lowbus_btns_panel.add(error_lowbus_boxes[i]);

							error_highbus_boxes[i] = new JCheckBox();
							error_highbus_boxes[i].setSize(10, 5);
							error_highbus_boxes[i].setVisible(true);
							error_highbus_boxes[i].setBorder(new EmptyBorder(3, 5, 2, 20));
							error_highbus_boxes[i].setEnabled(false);
							error_highbus_boxes[i].setIcon(icon);
							if ((list_error.get(i).getType()
									& AnomalDetector.High_Busload_SIG) == AnomalDetector.High_Busload_SIG)
								error_highbus_boxes[i].setSelected(true);
							else
								error_highbus_boxes[i].setSelected(false);
							error_highbus_btns_panel.add(error_highbus_boxes[i]);

							error_lessNoP_boxes[i] = new JCheckBox();
							error_lessNoP_boxes[i].setSize(10, 5);
							error_lessNoP_boxes[i].setVisible(true);
							error_lessNoP_boxes[i].setBorder(new EmptyBorder(3, 5, 2, 20));
							error_lessNoP_boxes[i].setEnabled(false);
							error_lessNoP_boxes[i].setIcon(icon);
							if ((list_error.get(i).getType()
									& AnomalDetector.Less_Num_of_Packets_SIG) == AnomalDetector.Less_Num_of_Packets_SIG)
								error_lessNoP_boxes[i].setSelected(true);
							else
								error_lessNoP_boxes[i].setSelected(false);
							error_lessNoP_btns_panel.add(error_lessNoP_boxes[i]);

							error_moreNoP_boxes[i] = new JCheckBox();
							error_moreNoP_boxes[i].setSize(10, 5);
							error_moreNoP_boxes[i].setVisible(true);
							error_moreNoP_boxes[i].setBorder(new EmptyBorder(3, 5, 2, 20));
							error_moreNoP_boxes[i].setEnabled(false);
							error_moreNoP_boxes[i].setIcon(icon);
							if ((list_error.get(i).getType()
									& AnomalDetector.More_Num_of_Packets_SIG) == AnomalDetector.More_Num_of_Packets_SIG)
								error_moreNoP_boxes[i].setSelected(true);
							else
								error_moreNoP_boxes[i].setSelected(false);
							error_moreNoP_btns_panel.add(error_moreNoP_boxes[i]);

							error_lessNoID_boxes[i] = new JCheckBox();
							error_lessNoID_boxes[i].setSize(10, 5);
							error_lessNoID_boxes[i].setVisible(true);
							error_lessNoID_boxes[i].setBorder(new EmptyBorder(3, 5, 2, 20));
							error_lessNoID_boxes[i].setEnabled(false);
							error_lessNoID_boxes[i].setIcon(icon);
							if ((list_error.get(i).getType()
									& AnomalDetector.Less_Num_of_IDs_SIG) == AnomalDetector.Less_Num_of_IDs_SIG)
								error_lessNoID_boxes[i].setSelected(true);
							else
								error_lessNoID_boxes[i].setSelected(false);
							error_lessNoID_btns_panel.add(error_lessNoID_boxes[i]);

							error_moreNoID_boxes[i] = new JCheckBox();
							error_moreNoID_boxes[i].setSize(10, 5);
							error_moreNoID_boxes[i].setVisible(true);
							error_moreNoID_boxes[i].setBorder(new EmptyBorder(3, 5, 2, 20));
							error_moreNoID_boxes[i].setEnabled(false);
							error_moreNoID_boxes[i].setIcon(icon);
							if ((list_error.get(i).getType()
									& AnomalDetector.More_Num_of_IDs_SIG) == AnomalDetector.More_Num_of_IDs_SIG)
								error_moreNoID_boxes[i].setSelected(true);
							else
								error_moreNoID_boxes[i].setSelected(false);
							error_moreNoID_btns_panel.add(error_moreNoID_boxes[i]);

							error_lowFreq_boxes[i] = new JCheckBox();
							error_lowFreq_boxes[i].setSize(10, 5);
							error_lowFreq_boxes[i].setVisible(true);
							error_lowFreq_boxes[i].setBorder(new EmptyBorder(3, 5, 2, 20));
							error_lowFreq_boxes[i].setEnabled(false);
							error_lowFreq_boxes[i].setIcon(icon);
							if ((list_error.get(i).getType()
									& AnomalDetector.Low_Frequence_SIG) == AnomalDetector.Low_Frequence_SIG)
								error_lowFreq_boxes[i].setSelected(true);
							else
								error_lowFreq_boxes[i].setSelected(false);
							error_lowFreq_btns_panel.add(error_lowFreq_boxes[i]);

							error_highFreq_boxes[i] = new JCheckBox();
							error_highFreq_boxes[i].setSize(10, 5);
							error_highFreq_boxes[i].setVisible(true);
							error_highFreq_boxes[i].setBorder(new EmptyBorder(3, 5, 2, 20));
							error_highFreq_boxes[i].setEnabled(false);
							error_highFreq_boxes[i].setIcon(icon);
							if ((list_error.get(i).getType()
									& AnomalDetector.High_Frequence_SIG) == AnomalDetector.High_Frequence_SIG)
								error_highFreq_boxes[i].setSelected(true);
							else
								error_highFreq_boxes[i].setSelected(false);
							error_highFreq_btns_panel.add(error_highFreq_boxes[i]);

							error_invalidID_boxes[i] = new JCheckBox();
							error_invalidID_boxes[i].setSize(10, 5);
							error_invalidID_boxes[i].setVisible(true);
							error_invalidID_boxes[i].setBorder(new EmptyBorder(3, 5, 2, 20));
							error_invalidID_boxes[i].setEnabled(false);
							error_invalidID_boxes[i].setIcon(icon);
							if ((list_error.get(i).getType()
									& AnomalDetector.Invalid_ID_SIG) == AnomalDetector.Invalid_ID_SIG)
								error_invalidID_boxes[i].setSelected(true);
							else
								error_invalidID_boxes[i].setSelected(false);
							error_invalidID_btns_panel.add(error_invalidID_boxes[i]);

							error_invalidIDSeq_boxes[i] = new JCheckBox();
							error_invalidIDSeq_boxes[i].setSize(10, 5);
							error_invalidIDSeq_boxes[i].setVisible(true);
							error_invalidIDSeq_boxes[i].setBorder(new EmptyBorder(3, 5, 2, 20));
							error_invalidIDSeq_boxes[i].setEnabled(false);
							error_invalidIDSeq_boxes[i].setIcon(icon);
							if ((list_error.get(i).getType()
									& AnomalDetector.Invalid_ID_Sequence_SIG) == AnomalDetector.Invalid_ID_Sequence_SIG)
								error_invalidIDSeq_boxes[i].setSelected(true);
							else
								error_invalidIDSeq_boxes[i].setSelected(false);
							error_invalidIDSeq_btns_panel.add(error_invalidIDSeq_boxes[i]);

							error_finaltype_labels[i] = new JLabel();
							error_finaltype_labels[i].setSize(100, 5);
							error_finaltype_labels[i].setVisible(true);
							error_finaltype_labels[i].setBorder(new EmptyBorder(2, 3, 2, 10));
							error_finaltype_labels[i].setText("   " + list_error.get(i).getAttackCategory() + "   ");
							error_finaltype_labels[i].setFont(new Font("Serif", Font.BOLD, 10));
							error_finaltype_btns_panel.add(error_finaltype_labels[i]);
						}

						if (error_groupselection != null)
							error_radio_buttons[Integer.parseInt(error_groupselection)].setSelected(true);

						frame.validate();
					}
				}
			}
		}
	}
	
	class IDWindow {

		private JFrame frame;

		private String detcolName[] = { "TimeStamp", "Elapsed(Gap) Time (ms)", "Data" };
		
		private DefaultTableModel id_tbl_model = null;
		private JTable id_Tbl = null;
		private JScrollPane idtbl_list_sc = null;

		private JLabel label_load = null;
		private JProgressBar progressBar = null;
		
		private String targetid = null;
		private int target_loc = 0;

		public IDWindow(String id, int loc)
		{
			this.targetid = id;
			this.target_loc = loc;
			
			Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
			
			frame = new JFrame("State of ID : " + id);
						
			frame.setSize(500, 850);
			frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
			frame.setResizable(false);

			Dimension f1_size = frame.getSize();
			int left = (screen.width / 2) - (f1_size.width / 2);
			int top = (screen.height / 2) - (f1_size.height / 2);
			frame.setLocation(left, top);
			frame.getContentPane().setLayout(null);
			frame.setVisible(true);

			label_load = new JLabel("Loading ID(" + id + ") state... ");
			label_load.setBounds(20, 15, 200, 20);
			label_load.setFont(new Font("Serif", Font.BOLD, 15));
			label_load.setHorizontalAlignment(SwingConstants.LEFT);
			frame.getContentPane().add(label_load);

			// ======================================
			// Table Scroll pane Begin
			// ======================================
			id_tbl_model = new DefaultTableModel(detcolName, 0);
			id_Tbl = new JTable(id_tbl_model)
			{
				private static final long serialVersionUID = 4760915797543132074L;
				
				DefaultTableCellRenderer renderCenter = new DefaultTableCellRenderer();
				{
					renderCenter.setHorizontalAlignment(SwingConstants.CENTER);
				}

				@Override
				public TableCellRenderer getCellRenderer(int arg0, int arg1) {
					return renderCenter;
				}
			};
			id_Tbl.setRowHeight(18);
			id_Tbl.setEnabled(false);

			idtbl_list_sc = new JScrollPane(id_Tbl);
			idtbl_list_sc.setBounds(20, 40, 450, 750);
			idtbl_list_sc.setVisible(false);
			frame.getContentPane().add(idtbl_list_sc);
			// ======================================
			// Table Scroll pane End
			// ======================================
			
			updateIDStateLists update = new updateIDStateLists();
			update.start();
		}
		
		class updateIDStateLists extends Thread
		{
			public void run()
			{
				ArrayList<DataObject> total_list = null;
				
				if ((offanalyzer != null) && offanalyzer.isEnd)
				{
					total_list = offanalyzer.get_total_list().get(target_loc).data_perID.get(targetid);
					
					if(total_list == null)
					{
						JLabel label_state = new JLabel("ID(" + targetid + ") was not appeared in this time section... ");
						label_state.setBounds(20, 50, 450, 20);
						label_state.setFont(new Font("Serif", Font.BOLD, 19));
						label_state.setHorizontalAlignment(SwingConstants.LEFT);
						label_state.setVisible(true);
						frame.getContentPane().add(label_state);
						
						return;
					}

					progressBar = new JProgressBar(0, total_list.size());
					progressBar.setValue(0);
					progressBar.setStringPainted(true);
					progressBar.setBounds(20, 35, 450, 20);
					progressBar.setVisible(true);
					frame.getContentPane().add(progressBar);
					
					for(int i=0;i<total_list.size();i++)
					{
						if(i == 0)
						{
							Object[] row = { total_list.get(i).ts, 0, total_list.get(i).getdata() };
							id_tbl_model.addRow(row);
						}
						else
						{
							Object[] row = { total_list.get(i).ts, String.format("%.1f", (total_list.get(i).ts - total_list.get(i-1).ts) / 1000.0) , total_list.get(i).getdata() };
							id_tbl_model.addRow(row);
						}
						
						progressBar.setValue(i+1);
					}
					
					progressBar.setVisible(false);
					idtbl_list_sc.setVisible(true);
				}
			}
		}
	}
}

class HighlightOnSelectIcon extends MetalCheckBoxIcon
{
	private static final long serialVersionUID = -3291264224314455898L;

	public void paintIcon(Component c, Graphics g, int x, int y) {
		JCheckBox cb = (JCheckBox) c;

		if (cb.isSelected()) {
			g.setColor(new Color(255, 0, 0));
			g.fillRect(x, y, getIconWidth(), getIconHeight());
		} else {
			g.setColor(new Color(115, 115, 115));
			g.drawRect(x, y, getIconWidth(), getIconHeight());

		}
	}
}

class ReadOnlyCheckBox extends JCheckBox
{
	private static final long serialVersionUID = 1894232898042206143L;

	protected void processKeyEvent(KeyEvent e) {

	}

	protected void processMouseEvent(MouseEvent e) {

	}
}