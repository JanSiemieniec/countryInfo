import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

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
import java.util.Objects;
import java.util.stream.Collectors;

public class JPanelLeft extends JPanel {
    public JPanelRight rightPanel = new JPanelRight(this);
    public JFrame parentJFrame;
    private static final String[] CONTINENTS = {"AS - Asia", "OC - Australia/Oceania", "EU - Europe", "AN - Antarctica",
            "SA - South America", "NA - North America", "AF - Africa"};

    public JPanelLeft(JFrame frame) {
        this.parentJFrame = frame;

        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        JLabel labelTitle = new JLabel("Country finder information");
        labelTitle.setFont(new Font("Arial", Font.BOLD, 30));
        c.gridwidth = 3;
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(10, 20, 10, 20);
        add(labelTitle, c);

        String[] options = CONTINENTS;
        Arrays.sort(options);
        JComboBox<String> comboBox = new JComboBox<>(options);
        comboBox.setSelectedIndex(0);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 3;
        c.gridx = 0;
        c.insets = new Insets(10, 20, 10, 20);
        c.gridy = 1;
        add(comboBox, c);

        JLabel label = new JLabel("Choose a number of countries to display (2-10)");
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setFont(new Font("Serif", Font.ITALIC, 18));
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 2;
        c.insets = new Insets(10, 20, 10, 20);
        add(label, c);

        JTextField spinner = new JTextField(2);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 2;
        c.gridwidth = 1;
        c.gridy = 2;
        c.insets = new Insets(10, 20, 10, 20);
        add(spinner, c);

        Button search = new Button("Search");
        search.addActionListener(e ->
                rightPanel.search(Objects.requireNonNull(comboBox.getSelectedItem()).toString(), spinner.getText()));
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weighty = 0.5;
        c.gridx = 0;
        c.gridwidth = 3;
        c.gridy = 3;
        c.insets = new Insets(10, 20, 10, 20);
        add(search, c);

        setBackground(Color.GRAY);
    }


    public String sendingRequest(String countryCode) {

        HttpClient client = HttpClient.newHttpClient();
        countryCode = countryCode.substring(0, countryCode.indexOf('-') - 1);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://countries.trevorblades.com/graphql"))
                .POST(HttpRequest.BodyPublishers.ofString("{\"query\":\"query ExampleQuery {\\n  continent(code: \\\""
                        + countryCode + "\\\") {\\n    countries {\\n      name\\n    }\\n  }\\n}\"}"))
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
        ArrayList<String> countriesList = new ArrayList<>();
        for (int i = 0; i < countries.size(); i++) {
            countriesList.add(countries.get(i).getAsJsonObject().get("name").toString());
        }
        countriesList = (ArrayList<String>) countriesList.stream()
                .map(x -> x.substring(1, x.length() - 1)).collect(Collectors.toList());

        Collections.shuffle(countriesList);

        countriesList = (ArrayList<String>) countriesList.stream()
                .limit(Long.parseLong(amount)).sorted().collect(Collectors.toList());

        return countriesList;
    }
}
