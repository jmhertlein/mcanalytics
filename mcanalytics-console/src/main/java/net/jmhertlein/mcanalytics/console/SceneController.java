/*
 * Copyright (C) 2015 Joshua Michael Hertlein
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.jmhertlein.mcanalytics.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.BorderPane;
import net.jmhertlein.mcanalytics.api.APISocket;
import net.jmhertlein.mcanalytics.api.FutureRequest;
import net.jmhertlein.mcanalytics.api.request.PastOnlinePlayerCountRequest;

/**
 * FXML Controller class
 *
 * @author joshua
 */
public class SceneController implements Initializable {
    private Socket raw;
    private APISocket sock;

    @FXML
    private ChoiceBox<?> chartChooser;
    @FXML
    private BorderPane chartPane;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private Button searchButton;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            raw = new Socket("localhost", 35555);
            PrintWriter out = new PrintWriter(raw.getOutputStream());
            BufferedReader in = new BufferedReader(new InputStreamReader(raw.getInputStream()));
            sock = new APISocket(out, in);
            sock.startListener();
        } catch(IOException ex) {
            Logger.getLogger(SceneController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void onSearch(ActionEvent event) {
        FutureRequest<LinkedHashMap<LocalDateTime, Integer>> submit;
        try {
            submit = sock.submit(new PastOnlinePlayerCountRequest(
                    startDatePicker.getValue().atStartOfDay(),
                    endDatePicker.getValue().plusDays(1).atStartOfDay()
            ));
        } catch(IOException ex) {
            Logger.getLogger(SceneController.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        try {
            LinkedHashMap<LocalDateTime, Integer> counts = submit.get();
            chartPane.setCenter(buildChart(counts));
            chartPane.layout();

        } catch(InterruptedException | ExecutionException ex) {
            Logger.getLogger(SceneController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private LineChart<String, Number> buildChart(LinkedHashMap<LocalDateTime, Integer> data) {
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();

        xAxis.setLabel("Time");
        yAxis.setLabel("Players");

        final LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);

        lineChart.setTitle("Player Activity");

        XYChart.Series series = new XYChart.Series();
        series.setName("Player Login Counts");

        for(Map.Entry<LocalDateTime, Integer> e : data.entrySet()) {
            series.getData().add(new XYChart.Data(e.getKey().toString(), e.getValue()));
        }

        lineChart.getData().add(series);

        return lineChart;
    }

}
