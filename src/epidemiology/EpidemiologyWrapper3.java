package epidemiology;

import java.awt.EventQueue;

import javax.swing.JFrame;

@SuppressWarnings("serial")
public class EpidemiologyWrapper3 extends JFrame {

	public final int FRAMESIZE = 600;
	public final int BTNSPACE = 63;
	public final int HRZSPACE = 8;
	
	public EpidemiologyWrapper3() {
        setSize(3*FRAMESIZE/2+HRZSPACE, FRAMESIZE+BTNSPACE);
		add(new Epidemiology3(FRAMESIZE, FRAMESIZE));
        setResizable(false);
        setTitle("SIR model, array based");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                EpidemiologyWrapper3 go = new EpidemiologyWrapper3();
                go.setVisible(true);
            }
        });
	}
}
