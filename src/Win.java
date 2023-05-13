import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.gson.*;

public class Win extends JFrame {
    private final JPanel leftPanel = new JPanel();
    private final JPanel rightPanel = new JPanel();

    public List<Button> listButtons = new ArrayList<>();

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
        String[] options = {"AS - Asia", "OC - Australia/Oceania", "EU - Europe", "AN - Antarctica", "SA - South America", "NA - North America", "AF - Africa"};
        Arrays.sort(options);
        JComboBox<String> comboBox = new JComboBox<>(options);
        comboBox.setSelectedIndex(0);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 3;
        c.gridx = 0;
        c.insets = new Insets(10, 20, 10, 20);
        c.gridy = 1;
        leftPanel.add(comboBox, c);

        JLabel label = new JLabel("Choose a number of coutries to display (2-10)");
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setFont(new Font("Arial", Font.ITALIC, 18));
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 2;
        c.insets = new Insets(10, 20, 10, 20);
        leftPanel.add(label, c);

        //    JSpinner spinner = new JSpinner(new SpinnerNumberModel(2, 2, 10, 1));
        JTextField spinner = new JTextField(2);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 2;
        c.gridwidth = 1;
        c.gridy = 2;
        c.insets = new Insets(10, 20, 10, 20);
        leftPanel.add(spinner, c);

        Button search = new Button("Search");
        search.addActionListener(e ->
                searching(comboBox.getSelectedItem().toString(), spinner.getText()));
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weighty = 0.5;
        c.gridx = 0;
        c.gridwidth = 3;
        c.gridy = 3;
        c.insets = new Insets(10, 20, 10, 20);
        leftPanel.add(search, c);

        leftPanel.setBackground(Color.GRAY);
        add(leftPanel, BorderLayout.WEST);
    }

    public void settingRightJPanel() {
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.PAGE_AXIS));
        add(rightPanel, BorderLayout.CENTER);
    }

    public void searching(String countryCode, String amount) {

        Pattern pattern = Pattern.compile("[2-9]|10");
        Matcher matcher = pattern.matcher(amount);
        if (!matcher.matches()) {
            JOptionPane.showMessageDialog(null, "Number of display countries should be between 2 and 10!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String jsonResponse = sendingRequest(countryCode);
        ArrayList<String> countries = parseJson(jsonResponse, amount);
        if (!listButtons.isEmpty()) {
            for (Button button : listButtons) {
                rightPanel.remove(button);
                button.setVisible(false);
            }
        }
        for (String ele : countries) {
            listButtons.add(new Button(ele));
        }
        for (Button button : listButtons) {
            rightPanel.add(button);
        }

        pack();
    }

    public String sendingRequest(String countryCode) {
        HttpClient client = HttpClient.newHttpClient();
        countryCode = countryCode.substring(0, countryCode.indexOf('-') - 1);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://countries.trevorblades.com/graphql"))
                .POST(HttpRequest.BodyPublishers.ofString("{\"query\":\"query ExampleQuery {\\n  continent(code: \\\"" + countryCode + "\\\") {\\n    countries {\\n      name\\n    }\\n  }\\n}\"}"))
                .setHeader("content-type", "application/json")
                .build();

        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return response.body();
    }

    public ArrayList<String> parseJson(String jsonResponse, String amount) {
        JsonObject jsonObject = new Gson().fromJson(jsonResponse, JsonObject.class);
        jsonObject = jsonObject.getAsJsonObject("data").getAsJsonObject("continent");

        JsonArray countries = jsonObject.getAsJsonArray("countries");
        ArrayList<String> counteriesList = new ArrayList<>();
        for (int i = 0; i < countries.size(); i++) {
            counteriesList.add(countries.get(i).getAsJsonObject().get("name").toString());
        }
        counteriesList = (ArrayList<String>) counteriesList.stream()
                .map(x -> x.substring(1, x.length() - 1)).collect(Collectors.toList());
        Collections.shuffle(counteriesList);
        counteriesList = (ArrayList<String>) counteriesList.stream()
                .limit(Long.parseLong(amount)).sorted().collect(Collectors.toList());

        return counteriesList;
    }
}
