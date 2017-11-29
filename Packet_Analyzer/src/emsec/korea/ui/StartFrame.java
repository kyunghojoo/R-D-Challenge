package emsec.korea.ui;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Toolkit;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import java.awt.event.ActionListener;
import java.io.File;
import java.awt.event.ActionEvent;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

public class StartFrame {

	private JFrame frame = null;
	
	private static JLabel comments = null;
	private static JButton btnOnlinet = null;
	private static JButton btnOfflinet = null;
	
	private boolean is1080p = false;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					StartFrame window = new StartFrame();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public StartFrame() {
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize(); // 모니터화면의 해상도 얻기
		
		if( (screen.width >= 1920) && (screen.height >= 1080) )
			this.is1080p = true;
		
		frame = new JFrame("Select Operation Mode");
		frame.setSize(390, 150);
		//frame.setBounds(left, top, 390, 150);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		Dimension f1_size = frame.getSize(); // 프레임크기
		int left = (screen.width / 2) - (f1_size.width / 2);
		int top = (screen.height / 2) - (f1_size.height /2 );
		frame.setLocation(left, top);
		
		comments = new JLabel("Which One?");
		comments.setHorizontalAlignment(SwingConstants.LEFT);
		comments.setFont(new Font("Serif", Font.BOLD, 15));
		comments.setBounds(30, 10, 200, 50);
		frame.getContentPane().add(comments);
		
		btnOnlinet = new JButton("Online Mode");
		btnOnlinet.setBounds(30, 60, 150, 30);
		btnOnlinet.setFont(new Font("Serif", Font.BOLD, 15));
		frame.getContentPane().add(btnOnlinet);
		
		btnOfflinet = new JButton("Offline Mode");
		btnOfflinet.setBounds(190, 60, 150, 30);
		btnOfflinet.setFont(new Font("Serif", Font.BOLD, 15));
		frame.getContentPane().add(btnOfflinet);
		
		btnOnlinet.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				frame.setVisible(false);
				
				if(is1080p)
				{
					MainFrame_1080p.setOnlineMode(true);
					MainFrame_1080p window = new MainFrame_1080p();
					window.frame.setVisible(true);
				}
				else
				{
					MainFrame_1080p.setOnlineMode(true);
					MainFrame_1080p window = new MainFrame_1080p();
					window.frame.setVisible(true);
//					MainFrame_720p.setOnlineMode(true);
//					MainFrame_720p window = new MainFrame_720p();
//					window.frame.setVisible(true);
				}
			}
		});
		
		btnOfflinet.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				frame.setVisible(false);
				
				String folderPath = "";
		        
		        JFileChooser chooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
		        chooser.setCurrentDirectory(new File("/"));
		        chooser.setAcceptAllFileFilterUsed(true); 
		        chooser.setDialogTitle("Select a Dataset file");
		        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		        chooser.setPreferredSize(new Dimension(900, 600));
		        
		        FileNameExtensionFilter filter = new FileNameExtensionFilter("Text File", "txt");
		        chooser.setFileFilter(filter);
		        
		        int returnVal = chooser.showOpenDialog(null);
		        
		        if(returnVal == JFileChooser.APPROVE_OPTION) {
		            folderPath = chooser.getSelectedFile().toString();
		        }else if(returnVal == JFileChooser.CANCEL_OPTION){
		            folderPath = null;
		        }				
				
				if(is1080p)
				{
					MainFrame_1080p.setOnlineMode(false);
					MainFrame_1080p.setDatasetFile(folderPath);
					MainFrame_1080p window = new MainFrame_1080p();
					window.frame.setVisible(true);
				}
				else
				{
					MainFrame_1080p.setOnlineMode(false);
					MainFrame_1080p.setDatasetFile(folderPath);
					MainFrame_1080p window = new MainFrame_1080p();
					window.frame.setVisible(true);
//					MainFrame_720p.setOnlineMode(false);
//					MainFrame_1080p.setDatasetFile(folderPath);
//					MainFrame_720p window = new MainFrame_720p();
//					window.frame.setVisible(true);
				}
			}
		});
	}
}
