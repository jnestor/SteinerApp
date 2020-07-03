
/**
 * @Author: John Nestor <nestorj>
 * @Date: 2020-06-24T20:49:46-04:00
 * @Email: nestorj@lafayette.edu
 * @Last modified by: nestorj
 * @Last modified time: 2020-06-24T20:56:56-04:00
 */

import java.lang.Math;
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.ButtonGroup;
import javax.swing.JFrame; // for unit test
import javax.swing.BorderFactory;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Graphics;
import java.io.*;
import javax.swing.JLabel;
import javax.swing.ImageIcon;

public class UIPrimMST extends JPanel implements PrimMSTInterface, UIAnimated, ActionListener, UIGraphChangeListener {

    private STGraph gr;
    private UIGraph ugr;
    private STPrimMST prim;

    private JPanel statusPanel;
    private JPanel controlPanel;

    private UIValDisplay halfPerimDisplay;
    private UIValDisplay lengthDisplay;

    private boolean animate = true;  // don't need this here?

    private UIAnimationController ucontrol;

    public static final long serialVersionUID = 1L;  // to shut up serialization warning

    private JToggleButton autoButton;

    private JLabel msgBoard;
    private String msg;

    private boolean autoMode = false;

    public UIPrimMST() {
        super();
        setLayout(new BorderLayout());
        gr = new STGraph();
        ugr = new UIGraph(gr, this);
        prim = new STPrimMST(this, gr);
        ucontrol = new UIAnimationController(this);
        autoButton = new JToggleButton("AUTO");
        autoButton.setActionCommand("AUTO");
        autoButton.setToolTipText("Auto-Update on Edit");
        autoButton.addActionListener(this);
        msgBoard = new JLabel("Click to create nodes");
        msg = "Click to create nodes";

        statusPanel = new JPanel();
        statusPanel.setLayout(new GridLayout(1, 2));
        halfPerimDisplay = new UIValDisplay("Half Perimeter", 0);
        statusPanel.add(halfPerimDisplay);
        lengthDisplay = new UIValDisplay("Edge Length", 0);
        statusPanel.add(lengthDisplay);
        add(statusPanel, BorderLayout.NORTH);
        add(ugr, BorderLayout.CENTER);

        controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());
        controlPanel.add(msgBoard);
        controlPanel.add(ucontrol);
        controlPanel.add(autoButton);
        add(controlPanel, BorderLayout.SOUTH);

        setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

    }

    public void setCostDisplay() {
        halfPerimDisplay.setValue(gr.halfPerim());
        lengthDisplay.setValue(gr.edgeLength());
    }

    public void initRandom() {
        int width = ugr.getWidth();
        int height = ugr.getHeight();
        gr.clearGraph();
        gr.addRandomNodes(10, width, height); // change to use range of graphics window
        try {
            prim.primMST(false);
        } catch (InterruptedException e) {
        }
    }

    public void readGraph(BufferedReader in) throws IOException {
        gr.readGraph(in);
    }

    public void clear() {
        gr.clearGraph();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        setCostDisplay();
        msgBoard.setText(msg);
    }

    /*----------------------------------------------------------------------*/
 /*        methods from UIAnimated interface                             */
 /*----------------------------------------------------------------------*/

 /* from the new thread - use to call the algorithm code */
    @Override
    public void runAnimation() throws InterruptedException {
        prim.primMST(true);
    }

    /* use to clean up when animation is terminated */
    @Override
    public void stopAnimation() {
        ucontrol.interruptAnimation();
    }

    /*----------------------------------------------------------------------*/
 /*        methods from PrimMSTInterface ace                             */
 /*----------------------------------------------------------------------*/
    /**
     * Interesting event: partial tree display/redisplay
     *
     * @throws java.lang.InterruptedException
     */
    @Override
    public void displayPartialTree() throws InterruptedException {
        repaint();
        ucontrol.animateDelay();
    }

    /**
     * Interesting event: display distance calculations
     *
     * @throws java.lang.InterruptedException
     */
    @Override
    /** Interesting event: display distance calculations */
    public void displayDistances() throws InterruptedException { 
        System.out.println("displayDistances not implemented!");

    } // do nothing for now
    // when we have a distance display, use this to update

    /**
     * Interesting event: display minimum distance node
     *
     * @param cn
     * @throws java.lang.InterruptedException
     */
    @Override
    public void displayClosestNode(STNode cn) throws InterruptedException {
        ugr.selectNode(cn); // select the closest node
        repaint();
        ucontrol.animateDelay();
        ugr.selectNode(null);
    }


    /*----------------------------------------------------------------------*/
 /*        ActionListener method                                         */
 /*----------------------------------------------------------------------*/
    @Override
    public synchronized void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmd == "AUTO") {
            if(gr.isEmpty()){
                autoButton.setSelected(false);
                setText("Please Create a root first");
            }
            else if (autoButton.isSelected()) {
                setText("Auto Mode");
                autoMode = true;
                ucontrol.disableAnimation();
                ugr.selectNode(null);
                try {
                    prim.primMST(false);
                } catch (InterruptedException except) {
                }
                repaint();
            } else {
                setText("Click to create nodes");
                autoMode = false;
                ucontrol.enableAnimation();
            }
        }
    }

    /*----------------------------------------------------------------------*/
 /*        UIGraphChangeListener method                                  */
 /*----------------------------------------------------------------------*/
    @Override
    public void graphChanged() {
        if (autoMode) {
            try {
                prim.primMST(false);
                repaint();
            } catch (InterruptedException e) {
            }
        } else {  // can't continue animaton with a changed graph!
            ucontrol.interruptAnimation();
            gr.clearEdges();
            gr.clearVisited();
            repaint();
        }
    }


    /*----------------------------------------------------------------------*/
 /*        main() / unit test                                            */
 /*----------------------------------------------------------------------*/
    public static void main(String[] args) {
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        UIPrimMST p = new UIPrimMST();
        f.setSize(600, 600);
        f.getContentPane().add(p);
        f.setVisible(true);
    }

    public void setText(String s) {
        msg = s;
        repaint();
    }

}
