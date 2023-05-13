import javax.swing.*;
import java.awt.*;

public class Win extends JFrame {
    private final JPanel leftPanel = new JPanel();
    private final JPanel rightPanel = new JPanel();

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
        leftPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        JLabel labelTitle = new JLabel("Country finder information");
        labelTitle.setFont(new Font("Arial", Font.BOLD, 30));
        c.gridwidth = 3;
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(10, 20, 10, 20);
        leftPanel.add(labelTitle, c);
        String[] options = {"Asia - AS", "OC - Australia/Oceania", "EU - Europe", "AN - Antarctica", "SA - South America", "NA - North America", "AF - Africa"};
        JComboBox<String> comboBox = new JComboBox<>(options);
        comboBox.setSelectedIndex(0);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 3;
        c.gridx = 0;
        c.insets = new Insets(10, 20, 10, 20);
        c.gridy = 1;
        leftPanel.add(comboBox, c);

        JLabel label = new JLabel("Choose a number of coutries to display ");
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setFont(new Font("Arial", Font.ITALIC, 18));
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 2;
        c.insets = new Insets(10, 20, 10, 20);
        leftPanel.add(label, c);

        JSpinner spinner = new JSpinner(new SpinnerNumberModel(2, 2, 10, 1));
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 2;
        c.gridwidth = 1;
        c.gridy = 2;
        c.insets = new Insets(10, 20, 10, 20);
        leftPanel.add(spinner, c);

        Button search = new Button("Search");
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weighty = 0.5;
        c.gridx = 0;
        c.gridwidth = 3;
        c.gridy = 3;
        c.insets = new Insets(10, 20, 10, 20);
        leftPanel.add(search, c);

        leftPanel.setBackground(Color.RED);
        add(leftPanel, BorderLayout.WEST);
    }

    public void settingRightJPanel() {
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.PAGE_AXIS));

        for (int i = 0; i < 10; i++) {
            rightPanel.add(new Button("Country name: " + i));
        }

        add(rightPanel, BorderLayout.CENTER);
    }
}
