package Traceroute;

import java.io.*; 
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Frame extends JFrame implements ActionListener, Runnable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7891737225292874786L;
	JPanel main;
	CardLayout cardLayout;
	InitBoard ib;
	JPanel wb;
	TraceWindow trb;
	JLabel label1, label2, label3, label4;
	JTextField tf1;
	JTextField tf2;
	Font police, police2;
	String dest;
	int nbSauts;
	String ip, router, numRouter;
	String rep;
	int delay=0;
	Scanner sc = new Scanner(System.in);
	private String [][] data;
	private String [] destination;
	int ligne;
	Thread anim;
	TraceMemory memory;
	
	public Frame(){
	
		destination=new String[2];
		this.setTitle("Traceroute by VmtSoftwares");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(1024,600);
		this.setResizable(false);
		this.setLocationRelativeTo(null);
		this.setUndecorated(true);
		main=new JPanel(cardLayout=new CardLayout());
		ib=new InitBoard();
		wb=new JPanel();
		wb.setSize(1024,600);
		wb.setBackground(Color.black);
		wb.setLayout(new FlowLayout(FlowLayout.LEFT,60,100));
		police = new Font("Arial", Font.BOLD, 50);
		police2 = new Font("Arial", Font.BOLD, 30);
		label1=new JLabel("  URL Destination:");
		label2=new JLabel("  Nombre de sauts maximum:");
		label3=new JLabel("T R A C E R O U T E  by VmtSoftwares");
		label4=new JLabel("Connexion en cours...");
		label1.setFont(police2);
		label2.setFont(police2);
		label3.setFont(police);
		label4.setFont(police2);
		label1.setForeground(Color.red);
		label2.setForeground(Color.red);
		label3.setForeground(Color.red);
		label4.setForeground(Color.black);
		tf1=new JTextField(20);
		tf1.setText("www.laamdatun.com");
		tf2=new JTextField(2);
		tf2.setText("7");
		tf1.setFocusable(true);
		tf1.setFont(police2);
		tf2.setFont(police2);
		tf1.addActionListener(this);
		tf2.addActionListener(this); 
		wb.add("North", label3);
		wb.add("Center", label1);
		wb.add("Center",tf1);
		wb.add("South",label2);
		wb.add("South",tf2);
		wb.add("South",label4);
		trb=new TraceWindow();
		main.add(ib,"ib");
		main.add(wb,"wb");
		main.add(trb,"trb");
		this.add(main);
		this.setVisible(true);
		changePanel("ib");
		try{
			Thread.sleep(2000);
		}
		catch(InterruptedException e){}
		changePanel("wb");
		tf1.requestFocus();
	}
	
	public void changePanel(String name){
        cardLayout.show(main, name);
    }
	
	public void process(){
			
		data=new String[nbSauts][10];
		try { 
			Process p=Runtime.getRuntime().exec("cmd /c tracert -h "+nbSauts+" -w 1 "+dest);  
			BufferedReader reader=new BufferedReader(new InputStreamReader(p.getInputStream())); 
			String line=reader.readLine();
			ligne=0;
			while(line!=null&&ligne!=-1){ 
				ligne=analyse(line, data, ligne);
				System.out.println(line); 
				line=reader.readLine();
			} 

		} 
		catch(IOException e1) {}  
		
		for(int i=0;i<destination.length;i++){
			System.out.println(destination[i]);
		}
		
		// affichage console du tableau data
		for(int i=0; i<data.length && data[i][0]!=null; i++){
				for(int j=0; j<data[i].length; j++){
					if(data[i][j]!=null)
						System.out.print(data[i][j]+" | ");
				}
				System.out.println("");
		}
		// lecture memoire
		try{
			chargerData();
		}
		catch(FileNotFoundException e){
			e.printStackTrace();
		}
		
		// affichage de l'ecran 3D
		changePanel("trb");
		
		// lancement animation 3D !!!
		if(trb == null){
			trb = new TraceWindow();
		}
		trb.animation(this, data, destination[1], destination[0], memory);
		// sérialisation memoire
		sauvegarderData();
	}			

	public int analyse(String st, String[][]data, int ligne){
		
		int res=ligne;
		int k=0;//debut token
		boolean isToken=false;
		boolean rec=false;
		int ca=0;//case du tableau
		
		for(int i=0; i<st.length(); i++){
			 if(st.charAt(0)=='D'){
				String sd=st.substring(35,st.length());
				destination=sd.split(" ");
				destination[1]=destination[1].substring(1,destination[1].length()-1);
			}
			if(st.charAt(2)==' '||!Character.isDigit(st.charAt(2))){
				return res;
			}
			if(st.charAt(i)!=' '&&!isToken){
				isToken=true;
				k=i;
			}
			else if(st.charAt(i)==' '&&isToken){
					
				if(st.charAt(k)=='D' && st.charAt(k+2)=='l'){
					data[ligne][ca]="routeur non reconnu";
					break;
				}
				else if(st.charAt(k)=='*'){
					data[ligne][ca]="100";
					ca++;
					rec=true;
				}
				else if(st.charAt(k)=='m' && st.charAt(k+1)=='s'){}	
				else if(st.charAt(k)=='['){
					data[ligne][ca]=st.substring(k+1,i-1);
					ca++;
					rec=true;
				}
				else{
					data[ligne][ca]=st.substring(k,i);
					ca++;
					rec=true;
				}
				
				isToken=false;
			} 
		
		}
		if(rec)
			res=ligne+1;
		return res;
	}
	public void actionPerformed(ActionEvent e) {
    
		if("close".equals(e.getActionCommand()))
			this.dispose();
		else{
			anim=new Thread(this);
			anim.start();
		}	
	}
	public void run(){
		
		dest=tf1.getText();
		nbSauts=Integer.parseInt(tf2.getText());
		// message connexion
		new Thread(this){
			public void run(){
				for(int i=0; i<nbSauts*4; i++){	
					try{
						label4.setForeground(Color.red);
						Thread.sleep(400);
					}catch(InterruptedException e){
						e.printStackTrace();
					}
					try{
						label4.setForeground(Color.black);
						Thread.sleep(400);
					}catch(InterruptedException e){
						e.printStackTrace();
					}
				}
			}
		}.start();
		
		process();
		
	}
	
	void sauvegarderData(){
		
		String[] rec=new String [nbSauts];
		ArrayList <String[]> liste=new ArrayList <String[]>();
		boolean same=true;
		for(int i=0; i<data.length && data[i][0]!=null; i++){
				if(data[i][5]!=null && !data[i][5].equals("routeur non reconnu"))
					rec[i]=data[i][5];
				else if(!data[i][4].equals("routeur non reconnu"))
					rec[i]=data[i][4];
		}		
		if(memory!=null){
			for(String [] ts : memory.m){
				int length = ts.length < rec.length ? ts.length : rec.length; // on prend le plus court
				for(int i=0; i<length; i++){
					if(!rec[i].equals(ts[i]))
						same=false;
				}
			}
			if(!same)
				memory.m.add(rec);
			
		}
		else{
			liste.add(rec);
			memory=new TraceMemory(liste);
		}
		same=true;
		
		try {
      FileOutputStream fichier = new FileOutputStream("Memory.ser");
      ObjectOutputStream oos = new ObjectOutputStream(fichier);
      oos.writeObject(memory);
      oos.flush();
      oos.close();
    }
    catch (java.io.IOException e) {
      e.printStackTrace();
    }
	}
	
	void chargerData() throws FileNotFoundException{
	
		try {
      FileInputStream fichier = new FileInputStream("Memory.ser");
      ObjectInputStream ois = new ObjectInputStream(fichier);
      memory = (TraceMemory) ois.readObject();
      ois.close();	
    } 
    catch (java.io.IOException e) {
      e.printStackTrace();
    }
    catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
		
		
		// for(String s : memory.m){
			// System.out.println("lecture memoire: ");
			// System.out.println(s);
		// }
	}
}
		