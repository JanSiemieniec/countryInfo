import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class JPanelRight extends JPanel {

    private final JPanelLeft leftPanel;

    private final List<Button> listButtons = new ArrayList<>();

    public JPanelRight(JPanelLeft leftPanel) {
        this.leftPanel = leftPanel;
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
    }

    private StringBuilder generatingInfo(String countryName) {
        countryName = countryName.replace(" ", "%20");
        String countryUrl = "https://restcountries.com/v3.1/name/" + countryName;
        String inf;
        try (
                BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(
                                new URL(countryUrl).openConnection().getInputStream()
                        )
                )
        ) {
            inf = bufferedReader.lines().collect(Collectors.joining());
        } catch (IOException e) {
            return new StringBuilder("No information found!");
        }
        StringBuilder stringBuilder = new StringBuilder();

        JSONArray jsonArray = new JSONArray(inf);
        JSONObject jsonObject = jsonArray.getJSONObject(0);

        try {
            JSONObject nameObject = jsonObject.getJSONObject("name");
            stringBuilder.append("Official name: ").append(nameObject.getString("official")).append("\n");
        } catch (Exception e) {
            stringBuilder.append("No information about official name").append("\n");
        }

        fillCapitals(jsonObject, stringBuilder);

        try {
            stringBuilder.append("Population: ").append(jsonObject.getLong("population")).append("\n");
        } catch (Exception e) {
            stringBuilder.append("No information about population").append("\n");
        }

        fillCurrencies(jsonObject, stringBuilder);

        try {
            stringBuilder.append("Subregion: ").append(jsonObject.get("subregion")).append("\n");
        } catch (Exception e) {
            stringBuilder.append("No information about subregion").append("\n");
        }
        fillLanguages(jsonObject, stringBuilder);

        return stringBuilder;
    }

    public void search(String countryCode, String amount) {

        new Thread(() -> {
            JDialog dialog = new JDialog();
            JLabel label = new JLabel("Loading countries...");
            label.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
            label.setFont(new Font("Serif", Font.BOLD, 20));
            dialog.add(label);
            dialog.pack();
            dialog.setSize(250, 120);
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);
            searching(countryCode, amount);
            dialog.dispose();
        }).start();

    }

    private void searching(String countryCode, String amount) {

        Pattern pattern = Pattern.compile("[2-9]|10");
        Matcher matcher = pattern.matcher(amount);
        if (!matcher.matches()) {
            JOptionPane.showMessageDialog(null,
                    "Number of display countries should be between 2 and 10!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String jsonResponse = leftPanel.sendingRequest(countryCode);
        ArrayList<String> countries = leftPanel.parseJson(jsonResponse, amount);
        if (!listButtons.isEmpty()) {
            for (Button button : listButtons) {
                remove(button);
                button.setVisible(false);
            }
        }

        for (String ele : countries) {
            Button tmp = new Button(ele);
            tmp.addActionListener(e -> info(tmp.getLabel()));
            listButtons.add(tmp);
        }

        for (Button button : listButtons) {
            add(button);
        }

        leftPanel.parentJFrame.pack();
    }

    private void infoDialog(StringBuilder infoAboutCountry) {

        JDialog dialog = new JDialog();
        JTextArea jTextField = new JTextArea(infoAboutCountry.toString());
        jTextField.setFont(new Font("Arial", Font.ITALIC, 24));
        jTextField.setMargin(new Insets(10, 10, 10, 10));
        jTextField.setEditable(false);
        dialog.add(jTextField);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
        dialog.pack();
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

    private void fillCurrencies(JSONObject jsonObject, StringBuilder stringBuilder) {
        try {
            JSONObject curObject = jsonObject.getJSONObject("currencies");
            int i = 1;
            for (String key : curObject.keySet()) {
                JSONObject currencyObject = curObject.getJSONObject(key);
                stringBuilder.append("Currency name(").append(i).append("): ").append(currencyObject.getString("name")).append("\n");
            }
        } catch (Exception e) {
            stringBuilder.append("No information about currency!").append("\n");
        }
    }

    private void fillCapitals(JSONObject jsonObject, StringBuilder stringBuilder) {
        try {
            JSONArray capitalObject = jsonObject.getJSONArray("capital");
            if (capitalObject.length() == 1) {
                stringBuilder.append("Capital: ");
                stringBuilder.append(capitalObject.get(0)).append("\n");
            } else {
                stringBuilder.append("Capitals: ");
                for (int i = 0; i < capitalObject.length() - 1; i++) {
                    stringBuilder.append(capitalObject.get(i).toString()).append(", ");
                }
                stringBuilder.append(capitalObject.get(capitalObject.length() - 1).toString()).append("\n");
            }
        } catch (Exception e) {
            stringBuilder.append("No information about capital!").append("\n");
        }
    }

    private void fillLanguages(JSONObject jsonObject, StringBuilder stringBuilder) {
        try {
            JSONObject lanObject = jsonObject.getJSONObject("languages");
            int i = 1;
            for (String ele : lanObject.keySet()) {
                stringBuilder.append("Language(").append(i++).append("): ").append(lanObject.getString(ele)).append("\n");
            }
        } catch (Exception e) {
            stringBuilder.append("No information about languages!").append("\n");
        }
    }

    private void info(String countryName) {

        new Thread(() -> {
            JDialog dialog = new JDialog();
            JLabel label = new JLabel("Loading ...");
            label.setHorizontalAlignment(JLabel.CENTER);
            label.setFont(new Font("Arial", Font.ITALIC, 24));
            dialog.add(label);
            dialog.pack();
            dialog.setSize(200, 100);
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);
            StringBuilder infoCountry = generatingInfo(countryName);
            dialog.dispose();
            infoDialog(infoCountry);
        }).start();
    }
}
