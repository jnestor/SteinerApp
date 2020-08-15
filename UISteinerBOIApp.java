
import javax.swing.JFrame;
import java.awt.BorderLayout;
public class UISteinerBOIApp
{
    public static void main(String[] args) {
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setLayout(new BorderLayout());
        UISteinerBOI p = new UISteinerBOI();
        f.setSize(1000, 600);
        f.getContentPane().add(p, BorderLayout.CENTER);
        f.getContentPane().add(p.getBOI().table, BorderLayout.EAST);
        f.setVisible(true);
        p.initRandom();
    }
}
