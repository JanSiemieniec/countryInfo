import javax.swing.*;
import java.awt.*;


public class Win extends JFrame {
    private final JPanelLeft leftPanel = new JPanelLeft(this);

    public Win() {
        setLayout(new BorderLayout());

        settingLeftJPanel();
        settingRightJPanel();
        pack();

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void settingLeftJPanel() {
        add(leftPanel, BorderLayout.WEST);
    }

    public void settingRightJPanel() {
        add(leftPanel.rightPanel, BorderLayout.CENTER);
    }
}
