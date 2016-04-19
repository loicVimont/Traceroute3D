package Traceroute;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLJPanel;
import javax.media.opengl.glu.GLU;
import javax.swing.BorderFactory;

import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.gl2.GLUT;

public class Trace3D implements GLEventListener {

	private static final double MOVING_STEP = 75.0;
	float light1_ambient[] = { 0.6f, 0.6f, 0.6f, 1.0f };
	float light1_diffuse[] = { 0.3f, 0.3f, 0.3f, 1.0f };
	float light1_specular[] = { 0.8f, 0.8f, 0.8f, 1.0f };
	// float spot_direction[] = { 0.0f, 0.0f, -1.0f };
	float MatSpec[] = { 1.0f, 1.0f, 1.0f, 1.0f };
	float MatDiff[] = { 0.7f, 0.7f, 0.7f, 0.7f };
	float MatAmb[] = { 0.8f, 0.8f, 0.8f, 0.8f };
	float light1_position[] = { 0.0f, 0.7f, -1.0f, 1.0f };
	private double sin[] = new double[360], cos[] = new double[360];
	private int r1, r2;
	private double dx, dy, dz;
	private GLU cam;
	private GLUT glut;
	double px, py, pz;
	String ip, router;
	String ipDest, nomDest;
	double xT, yT;
	int fSize, cursor;
	String[][] data;
	TraceMemory memory;
	Point[] dest;
	ArrayList<Point[]> memoire;
	int nbSauts;
	boolean atteint, stop;
	TraceWindow container;

	/*
	 * Set up the background color, and clear the modelview matrix.
	 */
	public void init(GLAutoDrawable d) {

		GL2 gl = d.getGL().getGL2();
		glut = new GLUT();
		gl.glClearColor(0.1f, 0.f, 0.1f, 1.0f);
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
		gl.glEnable(GL2.GL_COLOR_MATERIAL);
		gl.glShadeModel(GL2.GL_SMOOTH);
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
		gl.glLineWidth(1.0f);
		px = 0.0;
		py = 0.0;
		pz = 75.0;
		r1 = 0;
		r2 = 0;
		dx = 0.0;
		dy = 0.0;
		dz = 0.0;
		calcCosSinTable();
		cam = new GLU();
		ip = "";
		router = "";
		nbSauts = 0;
		cursor = 0;
		atteint = false;
		stop = false;

		gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, light1_ambient, 0);
		gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_AMBIENT, light1_ambient, 0);
		gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_DIFFUSE, light1_diffuse, 0);
		gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_SPECULAR, light1_specular, 0);
		gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_POSITION, light1_position, 0);
		gl.glEnable(GL2.GL_LIGHT1);
		gl.glEnable(GL2.GL_LIGHTING);

		gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, MatSpec, 0);
		gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_DIFFUSE, MatDiff, 0);
		gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT, MatAmb, 0);

		gl.glEnable(GL2.GL_COLOR_MATERIAL);
		gl.glEnable(GL2.GL_NORMALIZE);// normal % chaque face de l objet
										// %lumiere

	}

	public void reshape(GLAutoDrawable d, int x, int y, int width, int height) {

		GL2 gl = d.getGL().getGL2();
		gl.glMatrixMode(GL2.GL_PROJECTION);
		cam.gluPerspective(60.0, (double) width / height, 0.0, 10000.0);
		gl.glLoadIdentity();
		gl.glMatrixMode(GL2.GL_MODELVIEW);
	}

	public void display(GLAutoDrawable d) {

		nbSauts = data.length;
		xT = dest[cursor].x;
		yT = dest[cursor].y;
		glut = new GLUT();
		GL2 gl = d.getGL().getGL2();
		gl.glEnable(GL2.GL_BLEND);
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
		if (gl.isExtensionAvailable("GL_ARB_multisample")) {
			gl.glEnable(GL2.GL_MULTISAMPLE);
		}
		envInstall(gl);
		drawGrid(gl);
		drawRouters(gl);
		drawMemoryRouters(gl);
		gl.glLineWidth(2.0f);
		drawTravelLine(gl);
		gl.glLineWidth(1.0f);
		drawText(glut, gl);
		move();

	}

	private void drawGrid(GL2 gl) {

		gl.glPushMatrix();
		gl.glColor3f(1.0f, 0.0f, 1.0f);
		gl.glLineWidth(1.0f);
		gl.glBegin(GL2.GL_LINES);
		// bottom
		for (int i = -3000; i <= 3000; i += 200) {
			gl.glVertex3f(i, -500, 0);
			gl.glVertex3f(i, -500, -20000);
		}
		for (int i = 0; i <= 20000; i += 1000) {
			gl.glVertex3f(-3000, -500, -i);
			gl.glVertex3f(3000, -500, -i);
		}
		// top
		for (int i = -3000; i <= 3000; i += 200) {
			gl.glVertex3f(i, 500, 0);
			gl.glVertex3f(i, 500, -20000);
		}
		for (int i = 0; i <= 20000; i += 1000) {
			gl.glVertex3f(-3000, 500, -i);
			gl.glVertex3f(3000, 500, -i);
		}
		gl.glEnd();
		gl.glPopMatrix();

	}

	void envInstall(GL2 gl) {

		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		cam.gluPerspective(60.0, 1.6, 0.0, 1000.0);
		cam.gluLookAt(px, py, pz, dest[cursor].x, dest[cursor].y,
				dest[cursor].z - 40.0, 0.0, 1.0, 0.0);

		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT);
		gl.glLoadIdentity();
		gl.glEnable(GL2.GL_LIGHT1);
		gl.glEnable(GL2.GL_LIGHTING);
		gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_POSITION, light1_position, 0);
		// rotations et translation de toute la scène
		// gl.glRotated(r1,1.0,0.0,0.0);
		// gl.glRotated(r2,0.0,1.0,0.0);
		// gl.glTranslated(-px,-py,-pz);

	}

	void calcCosSinTable() {

		for (int i = 0; i < 360; i++) {

			sin[i] = Math.sin(i / 360.0 * 6.283185);
			cos[i] = Math.cos(i / 360.0 * 6.283185);
		}
	}

	void drawRouters(GL2 gl) {

		for (int i = 0; i < dest.length; i++) {
			gl.glPushMatrix();
			gl.glTranslated(dest[i].x, dest[i].y, dest[i].z);
			gl.glColor3f(1.0f, 0.0f, 0.0f);
			glut.glutWireTorus(5.0, 20.0, 30, 30);
			gl.glTranslated(-dest[i].x, -dest[i].y, -dest[i].z);
			gl.glPopMatrix();

		}
	}

	void drawMemoryRouters(GL2 gl) {

		if (memory != null) {
			for (Point[] tp : memoire) {
				for (int i = 0; i < tp.length; i++) {
					gl.glPushMatrix();
					gl.glTranslated(tp[i].x, tp[i].y, tp[i].z);
					gl.glColor3f(0.0f, 0.5f, 1.0f);
					glut.glutWireTorus(5.0, 20.0, 30, 30);
					gl.glTranslated(-tp[i].x, -tp[i].y, -tp[i].z);
					gl.glPopMatrix();
				}
				gl.glBegin(GL2.GL_LINES);
				for (int i = 0; i < tp.length - 1; i++) {
					gl.glNormal3d(tp[i].x, tp[i].y, tp[i].z);
					gl.glVertex3d(tp[i].x, tp[i].y, tp[i].z);
					gl.glNormal3d(tp[i + 1].x, tp[i + 1].y, tp[i + 1].z);
					gl.glVertex3d(tp[i + 1].x, tp[i + 1].y, tp[i + 1].z);
				}
				gl.glEnd();
			}
		}
	}

	// dessin du chemin
	void drawTravelLine(GL2 gl) {

		gl.glPushMatrix();
		gl.glShadeModel(GL2.GL_SMOOTH);
		gl.glColor3f(0.0f, 0.9f, 0.9f);
		gl.glBegin(GL2.GL_TRIANGLES);
		gl.glNormal3d(px - 0.1, py - 1.0, pz);
		gl.glVertex3d(px - 0.1, py - 1.0, pz);
		gl.glNormal3d(px + 0.1, py - 1.0, pz);
		gl.glVertex3d(px + 0.1, py - 1.0, pz);
		gl.glNormal3d(dest[cursor].x, dest[cursor].y, dest[cursor].z);
		gl.glVertex3d(dest[cursor].x, dest[cursor].y, dest[cursor].z);
		gl.glEnd();
		gl.glBegin(GL2.GL_LINES);
		for (int i = cursor; i < nbSauts - 1; i++) {
			gl.glNormal3d(dest[i].x, dest[i].y, dest[i].z);
			gl.glVertex3d(dest[i].x, dest[i].y, dest[i].z);
			gl.glNormal3d(dest[i + 1].x, dest[i + 1].y, dest[i + 1].z);
			gl.glVertex3d(dest[i + 1].x, dest[i + 1].y, dest[i + 1].z);
		}
		gl.glEnd();
		gl.glPopMatrix();
	}

	// dessin du texte
	void drawText(GLUT glut, GL2 gl) {

		router = "";
		ip = "";
		if (data[cursor][4] != null)
			router = data[cursor][4];
		if (data[cursor][5] != null)
			ip = data[cursor][5];
		gl.glPushMatrix();
		gl.glColor3f(0.3f, 0.8f, 0.2f);
		gl.glRasterPos3d(px, py, pz - 100.0);
		glut.glutBitmapString(5, router);
		gl.glRasterPos3d(px, py - 100.0, pz - 100.0);
		glut.glutBitmapString(5, ip);
		if (ip.equals(ipDest) || router.equals(nomDest)) {
			gl.glRasterPos3d(px, py - 100, pz - 100.0);
			glut.glutBitmapString(5, "OBJECTIF ATTEINT");
			atteint = true;
		}
		gl.glPopMatrix();
	}

	void move() {
		int s1 = 1;
		int s2 = -1;
		// difference de position
		dx = dest[cursor].x - px;
		dy = dest[cursor].y - py;
		dz = dest[cursor].z - pz;
		// calcul angle de pivotement
		r1 = (int) Math.toDegrees(Math.atan(dy / dz));
		r2 = (int) Math.toDegrees(Math.atan(dx / dz));
		// deplacement selon les axes
		pz -= MOVING_STEP * cos[Math.abs(r2)];
		px += MOVING_STEP * sin[Math.abs(r2)] * ((r2 <= 0) ? s1 : s2);
		py += MOVING_STEP * sin[Math.abs(r1)] * ((r1 <= 0) ? s1 : s2);

		if (atteint && pz - dest[cursor].z < 2)
			stop = true;
		if (pz - dest[cursor].z < 20 && cursor < nbSauts - 1)
			cursor++;
		if (stop || (cursor == nbSauts - 1 && pz < dest[cursor].z))
			container.finish();
	}

	// non-used !!!
	public void displayChanged(GLAutoDrawable d, boolean modeChanged,
			boolean deviceChanged) {
	}

	public void dispose(GLAutoDrawable arg0) {
		// TODO Auto-generated method stub

	}

}

class Point {

	double x, y, z;

	Point(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
}

class TraceWindow extends GLJPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1857128526770036472L;
	Trace3D forme;
	Frame f;
	FPSAnimator animator;
	double posX, posY, posZ;

	public TraceWindow() {

		this.setPreferredSize(new Dimension(1024, 600));
		this.setBorder(BorderFactory.createLineBorder(Color.RED, 3));
		forme = new Trace3D();
		this.setFocusable(true);
		this.addGLEventListener(forme);
		this.setVisible(true);
		this.requestFocus();
		forme.container = this;
		animator = new FPSAnimator(this, 700);
		posX = 0.0;
		posY = 0.0;
		posZ = -500.0;

	}

	public void animation(Frame f, String[][] data, String ipDest,
			String nomDest, TraceMemory memory) {
		this.f = f;
		forme.data = data;
		forme.memory = memory;
		forme.ipDest = ipDest;
		forme.nomDest = nomDest;
		if (memory != null)
			for (String[] ts : memory.m) {
				System.out.println("memoire animation:");
				for (String s : ts) {
					System.out.println(s);
				}
			}
		// positionnement routeurs traceroute
		forme.dest = new Point[data.length];
		
//		String st = null;
//		for (int i = 0; i < data.length; i++) {
//			if (data[i][5] != null)
//				st = data[i][5];
//			else if (data[i][4] != null)
//				st = data[i][4];
//			else
//				st = "192.168.32.254";
//			String[] ip1 = new String[4];// tableau pour decouper l'ip en quatre
//			int[] ip2 = new int[4];
//			ip1 = st.split(".");
//			for (int j = 0; j < ip1.length; j++) {
//				ip2[j] = Integer.parseInt(ip1[j]);
//				System.out.println("ok !!!");
//			}
//			double posX = ip2[1] * ((ip2[1] < 168) ? -1 : 1);
//			double posY = ip2[2] * ((ip2[2] < 168) ? -1 : 1);
//			double posZ = -(ip2[0] + ip2[3]) * 10;
//			forme.dest[i] = new Point(posX, posY, posZ);
//		}

		 for (int i = 0; i < data.length; i++) {
		 forme.dest[i] = new Point(posX, posY, posZ);
		 int s1 = (Math.random() < 0.5) ? 1 : -1;
		 int s2 = (Math.random() < 0.5) ? 1 : -1;
		 posX = 150 + Math.random() * 400 * s1;
		 posY = 150 + Math.random() * 400 * s2;
		 posZ -= 1500.0;
		 }
		
		// positionnement routeurs en memoire
		if (memory != null) {
			forme.memoire = new ArrayList<Point[]>();
			for (String[] ts : memory.m) {
//				Point[] tp = new Point[ts.length];
//				for (int i = 0; i < ts.length; i++) {
//					st = ts[i];
//					String[] ip1;// tableau pour decouper l'ip en quatre 
//					int[] ip2 = new int[4];
//					ip1 = st.split(".");
//					for (int j = 0; j < ip1.length; j++) {
//						ip2[j] = Integer.parseInt(ip1[j]);
//					}
//					
//					double posX = ip2[1] * ((ip2[1] < 168) ? -1 : 1);
//					double posY = ip2[2] * ((ip2[2] < 168) ? -1 : 1);
//					double posZ = -(ip2[0] + ip2[3]) * 10;
//					tp[i] = new Point(posX, posY, posZ);
//				}
//				forme.memoire.add(tp);
				
				posZ = -500.0;
				Point[] tp = new Point[ts.length];
				for (int i = 0; i < ts.length; i++) {
					int s1 = (Math.random() < 0.5) ? 1 : -1;
					int s2 = (Math.random() < 0.5) ? 1 : -1;
					posX = (500 + Math.random() * 400) * s1;
					posY = (500 + Math.random() * 400) * s2;
					posZ = posZ - (500.0 + Math.random() * 400);
					tp[i] = new Point(posX, posY, posZ);
				}
				forme.memoire.add(tp);
			}
		}
		animator.start();
	}

	public void finish() {

		forme.stop = false;
		forme.cursor = 0;
		forme = null;
		animator.stop();
		f.changePanel("wb");
		f.tf1.requestFocus();
		this.destroy();
	}

}