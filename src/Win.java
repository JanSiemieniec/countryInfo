import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
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

import org.json.*;

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
            Button tmp = new Button(ele);
            tmp.addActionListener(e -> info(tmp.getLabel()));
            listButtons.add(tmp);
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

    public void info(String countryName) {

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
            StringBuilder infoCountry = doLongJob(countryName);
            dialog.dispose();
            infoDialog(infoCountry);
        }).start();

    }

    public StringBuilder doLongJob(String contryName) {
        contryName = contryName.replace(" ", "%20");
        String countryUrl = "https://restcountries.com/v3.1/name/" + contryName;
        String inf = "";
        try (
                BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(
                                new URL(countryUrl).openConnection().getInputStream()
                        )
                )
        ) {
            inf = bufferedReader.lines().collect(Collectors.joining());
        } catch (IOException e) {
            e.printStackTrace();
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

        fillCapitals("capital", jsonObject, stringBuilder);

        try {
            stringBuilder.append("Population: ").append(jsonObject.getLong("population")).append("\n");
        } catch (Exception e) {
            stringBuilder.append("No information about population").append("\n");
        }

        fillCurrencies("currencies", jsonObject, stringBuilder);

        try {
            stringBuilder.append("Subregion: ").append(jsonObject.get("subregion")).append("\n");
        } catch (Exception e) {
            stringBuilder.append("No information about subregion").append("\n");
        }
        fillLanguages("languages", jsonObject, stringBuilder);

        return stringBuilder;
    }

    public void infoDialog(StringBuilder infoAboutCountry) {
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

    public void fillCurrencies(String symbol, JSONObject jsonObject, StringBuilder stringBuilder) {
        try {

            JSONObject curObject = jsonObject.getJSONObject(symbol);
            int i = 1;
            for (String key : curObject.keySet()) {
                JSONObject currencieObject = curObject.getJSONObject(key);
                stringBuilder.append("Currencie name(").append(i).append("): ").append(currencieObject.getString("name")).append("\n");
            }
        } catch (Exception e) {
            stringBuilder.append("No information about currency!").append("\n");
        }
    }

    public void fillCapitals(String symbol, JSONObject jsonObject, StringBuilder stringBuilder) {
        try {
            JSONArray capitalObject = jsonObject.getJSONArray(symbol);
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

    public void fillLanguages(String symbol, JSONObject jsonObject, StringBuilder stringBuilder) {
        try {
            JSONObject lanObject = jsonObject.getJSONObject(symbol);
            int i = 1;
            for (String ele : lanObject.keySet()) {
                stringBuilder.append("Language(").append(i++).append("): ").append(lanObject.getString(ele)).append("\n");
            }
        } catch (Exception e) {
            stringBuilder.append("No information about languages!").append("\n");
        }
    }
}
