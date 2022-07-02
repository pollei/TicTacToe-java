/**
 * 
 */
package io.github.pollei.ticTac;

//import ticTac.baseGame.ttBoard;
// https://www.baeldung.com/java-find-all-classes-in-package



import io.github.pollei.ticTac.BaseTicTacGame.PlyrSym;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import java.awt.GridBagLayout;
import java.awt.Insets;
//import java.awt.ComponentOrientation;
import java.awt.GridBagConstraints;
import java.awt.BasicStroke;
//import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
//import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
//import java.awt.event.ComponentAdapter;
//import java.awt.event.ComponentEvent;
//import java.awt.font.TextLayout;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;




/**
 * @author Steve_Pollei
 *
 */
public class SwingGame implements Runnable {
	private JFrame topFrame;
	private SetupTT setupPanel;
	private TicBoardPanel ticTacBoard;
	private BaseTicTacGame game;
	
	private static class TicSquareButton extends JButton {
		/**
		 * 
		 */
		private static final long serialVersionUID = -7693177093986152812L;
		private BaseTicTacGame.PlyrSym sym;

		/**
		 * 
		 */
		public TicSquareButton() {
			super();
			sym = PlyrSym.Empty;
		}

		private void paintX(Graphics g) {
			var grph = g.create();
			
			if (grph instanceof Graphics2D grph2d) {
				double x = getWidth();
				double y = getHeight();
				//System.out.println("tt pnt comp " + x + " " +y);
				var strk = new BasicStroke((float)(x/50 + y/50 +2));
				var ln1 = new Line2D.Double(0.05*x, 0.05*y, 0.95*x, 0.95*y);
				var ln2 = new Line2D.Double(0.95*x, 0.05*y, 0.05*x, 0.95*y);
				grph2d.setStroke(strk);
				grph2d.draw(ln1);
				grph2d.draw(ln2);
				//System.out.println("tt pnt comp out");

			}
		}
		private void paintO(Graphics g) {
			var grph = g.create();
			
			if (grph instanceof Graphics2D grph2d) {
				double x = getWidth();
				double y = getHeight();
				//System.out.println("tt pnt comp " + x + " " +y);
				var strk = new BasicStroke((float)(x/50 + y/50 +2));
				var circ = new Ellipse2D.Double(0.1*x, 0.1*y,0.8*x, 0.8*y);
				grph2d.setStroke(strk);
				grph2d.draw(circ);
				//grph2d.draw(ln2);
				//System.out.println("tt pnt comp out");

			}
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			switch (sym) {
			  case X -> paintX(g);
			  case O -> paintO(g);
			  case Empty -> {}
			}
		}	
	}

	
	private class SetupTT extends JPanel implements ActionListener {
		/**
		 * 
		 */
		private static final long serialVersionUID = -1037090224263235377L;
		private JRadioButton xCbox = new JRadioButton("X");
		private JRadioButton oCbox = new JRadioButton("O");
		private JRadioButton frstOrdr = new JRadioButton("Go First");
		private JRadioButton lstOrdr = new JRadioButton("Go Last");
		
		@Override
		public Dimension getPreferredSize() { 
			var ret = super.getPreferredSize();
			if (ret.width < 300) ret.width=300;
			if (ret.height < 300) ret.height=300;
			return ret;
		}

		SetupTT() {
			var lo = new GridBagLayout();
			this.setLayout(lo);
			var labelCstraint = new GridBagConstraints();
			var cboxCstraint = new GridBagConstraints();
			labelCstraint.anchor = GridBagConstraints.EAST;
			// labelCstraint.fill = GridBagConstraints.HORIZONTAL;
			labelCstraint.ipady = 3;
			labelCstraint.gridx = 1;
			labelCstraint.gridy = 0;
			cboxCstraint.anchor = GridBagConstraints.CENTER;
			cboxCstraint.fill = GridBagConstraints.HORIZONTAL;
			cboxCstraint.ipadx = 3;
			cboxCstraint.ipady = 3;
			cboxCstraint.gridx = 2;
			cboxCstraint.gridy = 0;
			var xoLab = new JLabel("x or o: ");
			//lo.addLayoutComponent(xoLab, labelCstraint);
			this.add(xoLab, labelCstraint);
			this.setVisible(true);
			var ordrLab = new JLabel("Go First or Last: ");
			labelCstraint.gridy = 1;
			this.add(ordrLab, labelCstraint);
			var xoGrp = new ButtonGroup();
			var ordrGrp = new ButtonGroup();
			xoGrp.add(xCbox);
			xoGrp.add(oCbox);
			xCbox.setSelected(true);
			//xCbox.setActionCommand("X");
			//oCbox.setActionCommand("O");
			this.add(xCbox, cboxCstraint);
			cboxCstraint.gridx = 3;
			this.add(oCbox, cboxCstraint);
			
			ordrGrp.add(frstOrdr);
			ordrGrp.add(lstOrdr);
			frstOrdr.setSelected(true);
			//frstOrdr.setActionCommand("F");
			//lstOrdr.setActionCommand("L");
			cboxCstraint.gridx = 2;
			cboxCstraint.gridy = 1;
			this.add(frstOrdr, cboxCstraint);
			cboxCstraint.gridx = 3;
			this.add(lstOrdr, cboxCstraint);
			
			//xCbox.addActionListener(this);
			//oCbox.addActionListener(this);
			//frstOrdr.addActionListener(this);
			//lstOrdr.addActionListener(this);
			
			var sgBut = new JButton("start game");
			cboxCstraint.gridy =2;
			cboxCstraint.gridx =0;
			cboxCstraint.gridwidth =4;
			cboxCstraint.weightx=0.4;
			cboxCstraint.fill = GridBagConstraints.BOTH;
			sgBut.setActionCommand("G");
			this.add(sgBut, cboxCstraint);
			sgBut.addActionListener(this);
		}

		//@SuppressWarnings("unused")
		@Override
		public void actionPerformed(ActionEvent e) {
				game = new BaseTicTacGame();
				// game.setDefaultPlayers();
				BaseTicTacGame.PlyrSym sym = xCbox.isSelected() ? PlyrSym.X : PlyrSym.O;
				int place = frstOrdr.isSelected() ? 0 : 1;
				game.setHumanPlayerHVC(place,sym);
				ticTacBoard = new TicBoardPanel();
				topFrame.setContentPane(ticTacBoard);
				topFrame.pack();
		    topFrame.setVisible(true);
		}
	}
	
	private class BlankComp extends JComponent {

		@Override
		public Dimension getPreferredSize() { 
			final int sz =8;
			var ret = super.getPreferredSize();
			if (ret.width < sz) ret.width =sz;
			if (ret.height < sz) ret.height =sz;
			return ret;
		}

		@Override
		public Dimension getMaximumSize() { 
			final int sz =13;
			var ret = super.getMaximumSize();
			if (ret.width < sz) ret.width =sz;
			if (ret.height < sz) ret.height =sz;
			return ret;
		}

		/**
		 * 
		 */
		private static final long serialVersionUID = 200228379686017653L;

		@Override
		public Dimension getMinimumSize() { 
			final int sz =5;
			var ret = super.getMinimumSize();
			if (ret.width < sz) ret.width =sz;
			if (ret.height < sz) ret.height =sz;
			return ret;
		}
		
		
	}
	
	private class TicBoardPanel extends JPanel implements ActionListener {
		/**
		 * 
		 */
		private static final long serialVersionUID = -9197494490491743061L;
		TicSquareButton compBoxes[][] = {
				{null,null,null},
				{null,null,null},
				{null,null,null}, };
		private GridBagLayout layOut = new GridBagLayout();
		

		@Override
		public Dimension getPreferredSize() { 
			var ret = super.getPreferredSize();
			var topLvl = getTopLevelAncestor();
			var tly = topLvl.getHeight();
			var tlx =topLvl.getWidth();
			var sz = Math.max(Math.min(tly,tlx), 300);
			if (ret.width < sz) ret.width=sz;
			if (ret.height < sz) ret.height=sz;
			return ret;
		}

		/**
		 * 
		 */
		
		public TicBoardPanel() {
			this.setLayout(layOut);
			var sqrCstraint = new GridBagConstraints();
			var lineCstraint = new GridBagConstraints();
			sqrCstraint.anchor = GridBagConstraints.CENTER;
			sqrCstraint.fill = GridBagConstraints.BOTH;
			sqrCstraint.ipadx =5;
			sqrCstraint.ipadx =5;
			sqrCstraint.weightx = 0.8;
			sqrCstraint.weighty = 0.8;
			lineCstraint.anchor = GridBagConstraints.CENTER;
			lineCstraint.fill = GridBagConstraints.BOTH;
			lineCstraint.ipadx =4;
			lineCstraint.ipady =4;
			int cnrPnts[][] = {{0,0},  {0,2}, {0,4}, {0, 6},
					{2,0}, {4,0},
					{6,0}, {6,2}, {6,4}, {6,6} };
			for (var pnt : cnrPnts) {
				lineCstraint.gridx=pnt[0];
				lineCstraint.gridy=pnt[1];
				var blankSpot = new BlankComp();
				this.add(blankSpot, lineCstraint);
			}
			int linePnts[][] = {
					{2,0,1,7,SwingConstants.VERTICAL},
					{4,0,1,7,SwingConstants.VERTICAL},
					{0,2,7,1,SwingConstants.HORIZONTAL},
					{0,4,7,1,SwingConstants.HORIZONTAL}, };
			lineCstraint.insets = new Insets(7,7,2,2);
			for (var pnt : linePnts) {
				lineCstraint.gridx=pnt[0];
				lineCstraint.gridy=pnt[1];
				lineCstraint.gridwidth =pnt[2];
				lineCstraint.gridheight =pnt[3];
				
				var sep = new JSeparator(pnt[4]);
				this.add(sep, lineCstraint);
			}
			for (var x=0; x<3; x++) {
				sqrCstraint.gridx = 2*x+1;
				for (var y=0; y<3; y++) {
					sqrCstraint.gridy = 2*y+1;
					//var sqrBut = new JButton(" ");
					var sqrBut = new TicSquareButton();
					compBoxes[x][y] = sqrBut;
					this.add(sqrBut, sqrCstraint);
					sqrBut.addActionListener(this);
					
				}
			}
			
		}

		@Override
		public void actionPerformed(ActionEvent e) { 
			//e.getSource();
			if (e.getSource() instanceof JComponent compo) {
				var constra = layOut.getConstraints(compo);
				int x = (constra.gridx -1)/2;
				int y = (constra.gridy -1)/2;
				System.out.println( "X:" + x + " y:" + y);
				compBoxes[x][y].sym = PlyrSym.X;
				this.revalidate();
				this.repaint();
			}
			
		}
		/**
		 * 
		 */
	}
	
	@Override
	public void run() { 
		topFrame = new JFrame("TicTacToe using Swing");
		topFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//var label = new JLabel("Hello World");
		setupPanel = new SetupTT();
		//topFrame.getContentPane().add(setupPanel);
		topFrame.setContentPane(setupPanel);
		topFrame.pack();
        topFrame.setVisible(true);
		
	}
	 
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) { 
		SwingUtilities.invokeLater(new SwingGame() );
	}

	

}
