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
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
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
import net.jmhertlein.mcanalytics.api.request.FirstJoinRequest;
import net.jmhertlein.mcanalytics.api.request.PastOnlinePlayerCountRequest;

/**
 * FXML Controller class
 *
 * @author joshua
 */
public class ChartSceneController implements Initializable {
    private Socket raw;
    private APISocket sock;

    @FXML
    private ChoiceBox<ChartType> chartChooser;
    @FXML
    private BorderPane chartPane;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private Button searchButton;

    private PrintWriter out;
    private BufferedReader in;

    public void setIO(APISocket s) {
        sock = s;
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        chartChooser.setItems(FXCollections.observableArrayList(ChartType.values()));
    }

    @FXML
    private void onSearch(ActionEvent event) {
        LocalDateTime start = startDatePicker.getValue().atStartOfDay(),
                end = endDatePicker.getValue().plusDays(1).atStartOfDay();
        ChartType type = chartChooser.getValue();
        if(type == null)
            return;

        switch(type) {
            case BOUNCED_LOGINS:
            case FIRST_LOGINS:
                handleFirstLoginsChart(start, end);
            case ONLINE_PLAYERS:
                handleOnlinePlayersChart(start, end);
            default:
                System.out.println("I FORGOT TO PUT A CASE FOR " + type.name());
        }

    }

    private LineChart<Date, Number> buildChart(LinkedHashMap<LocalDateTime, Integer> data) {
        final DateAxis xAxis = new DateAxis();
        final NumberAxis yAxis = new NumberAxis();

        xAxis.setLabel("Time");
        yAxis.setLabel("Players");
        yAxis.setTickLength(5);
        yAxis.setMinorTickLength(1);

        xAxis.setAutoRanging(true);

        final LineChart<Date, Number> lineChart = new LineChart<>(xAxis, yAxis);

        lineChart.setTitle("Player Activity");

        XYChart.Series series = new XYChart.Series();
        series.setName("Online Players");

        for(Map.Entry<LocalDateTime, Integer> e : data.entrySet()) {
            series.getData().add(new XYChart.Data(Date.from(e.getKey().atZone(ZoneId.systemDefault()).toInstant()), e.getValue()));
        }

        lineChart.getData().add(series);

        return lineChart;
    }

    private void handleOnlinePlayersChart(LocalDateTime start, LocalDateTime end) {
        FutureRequest<LinkedHashMap<LocalDateTime, Integer>> submit;
        try {
            submit = sock.submit(new PastOnlinePlayerCountRequest(
                    startDatePicker.getValue().atStartOfDay(),
                    endDatePicker.getValue().plusDays(1).atStartOfDay()
            ));
        } catch(IOException ex) {
            Logger.getLogger(ChartSceneController.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        try {
            LinkedHashMap<LocalDateTime, Integer> counts = submit.get();
            chartPane.setCenter(buildChart(counts));
            chartPane.layout();

        } catch(InterruptedException | ExecutionException ex) {
            Logger.getLogger(ChartSceneController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void handleFirstLoginsChart(LocalDateTime start, LocalDateTime end) {

    }

}
