package Traceroute;

import java.awt.Graphics;
import java.awt.Image;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class InitBoard extends JPanel{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Image im;
	
	public InitBoard(){
	
		this.setSize(1024,600);
		try {
			im = ImageIO.read(getClass().getResource("/TracerouteVmt.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		im=new ImageIcon(this.getClass().getResource("images/TracerouteVmt.png")).getImage();
	}
	
	public void paintComponent(Graphics g) {

        g.drawImage(im,0,0,null);
		
	}
}