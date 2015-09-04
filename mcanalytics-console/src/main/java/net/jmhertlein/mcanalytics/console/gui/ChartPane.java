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
package net.jmhertlein.mcanalytics.console.gui;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.input.KeyCode;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import net.jmhertlein.mcanalytics.api.APISocket;
import net.jmhertlein.mcanalytics.api.FutureRequest;
import net.jmhertlein.mcanalytics.api.request.NewPlayerLoginsRequest;
import net.jmhertlein.mcanalytics.api.request.PastOnlinePlayerCountRequest;
import net.jmhertlein.mcanalytics.api.request.UniqueLoginsPerDayRequest;
import net.jmhertlein.mcanalytics.console.ChartType;
import net.jmhertlein.mcanalytics.console.DateAxis;
import net.jmhertlein.mcanalytics.console.TimeUtils;

/**
 * FXML Controller class
 *
 * @author joshua
 */
public class ChartPane extends FXMLPane {
    private final APISocket sock;
    private final String username;

    @FXML
    private ChoiceBox<ChartType> chartChooser;
    @FXML
    private BorderPane chartPane;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;

    public ChartPane(String username, APISocket s) {
        super("/fxml/ChartScene.fxml");

        sock = s;
        this.username = username;
        chartChooser.setItems(FXCollections.observableArrayList(ChartType.values()));
        chartChooser.setValue(ChartType.ONLINE_PLAYERS);

        startDatePicker.setValue(LocalDate.now());
        endDatePicker.setValue(LocalDate.now());
    }

    @FXML
    private void onSearch(ActionEvent event) {
        ChartType type = chartChooser.getValue();
        if(type == null)
            return;

        try {
            switch(type) {
                case ONLINE_PLAYERS:
                    handleOnlinePlayersChart();
                    break;
                case UNIQUE_LOGINS:
                    handleUniqueLoginsChart();
                    break;
                default:
                    System.out.println("I FORGOT TO PUT A CASE FOR " + type.name());
            }
        } catch(IOException | InterruptedException | ExecutionException ex) {
            ex.printStackTrace(System.err);
        }

    }

    private void handleOnlinePlayersChart() throws IOException, InterruptedException, ExecutionException {
        FutureRequest<LinkedHashMap<LocalDateTime, Integer>> requestResult;

        requestResult = sock.submit(new PastOnlinePlayerCountRequest(
                startDatePicker.getValue().atStartOfDay(),
                endDatePicker.getValue().plusDays(1).atStartOfDay()
        ));

        LinkedHashMap<LocalDateTime, Integer> counts = requestResult.get();

        //mungeDataToSquareLines(counts);

        XYChart.Series<Date, Number> series = new XYChart.Series<>();
        for(Map.Entry<LocalDateTime, Integer> e : counts.entrySet()) {
            series.getData().add(new XYChart.Data(Date.from(e.getKey().atZone(ZoneId.systemDefault()).toInstant()), e.getValue()));
        }
        final DateAxis xAxis = new DateAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Time");
        yAxis.setLabel("Online Players");
        yAxis.setTickLength(5);
        yAxis.setMinorTickLength(1);
        xAxis.setAutoRanging(true);
        final LineChart<Date, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Online Players");
        lineChart.getData().add(series);
        chartPane.setCenter(lineChart);
        chartPane.layout();

        lineChart.setOnScroll(e -> handleScroll(xAxis, e));
    }

    private static <V> void mungeDataToSquareLines(LinkedHashMap<LocalDateTime, V> counts) {
        if(counts.size() > 1) {
            Map<LocalDateTime, V> padding = new HashMap<>();
            Iterator<Entry<LocalDateTime, V>> iterator = counts.entrySet().stream().sorted((l, r) -> l.getKey().compareTo(r.getKey())).collect(Collectors.toList()).iterator();
            Entry<LocalDateTime, V> prev = iterator.next();
            while(iterator.hasNext()) {
                Entry<LocalDateTime, V> cur = iterator.next();
                padding.put(cur.getKey().minusNanos(1), prev.getValue());
                System.out.println("Adding a transition from " + prev.getValue() + " to " + cur.getValue());
                prev = cur;
            }

            counts.putAll(padding);
        }
    }

    private void handleFirstLoginsChart() throws IOException, InterruptedException, ExecutionException {
        FutureRequest<LinkedHashMap<LocalDateTime, Integer>> requestResult = sock.submit(new NewPlayerLoginsRequest(
                startDatePicker.getValue(),
                endDatePicker.getValue().plusDays(1)
        ));

        LinkedHashMap<LocalDateTime, Integer> counts = requestResult.get();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for(Map.Entry<LocalDateTime, Integer> e : counts.entrySet()) {
            series.getData().add(new XYChart.Data(e.getKey().toString(), e.getValue()));
        }
        series.setName("New Players");
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Time Joined");
        yAxis.setLabel("Players Joined");
        yAxis.setTickLength(5);
        yAxis.setMinorTickLength(1);
        xAxis.setAutoRanging(true);
        final BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("New Players");
        barChart.getData().add(series);

        chartPane.setCenter(barChart);
        chartPane.layout();
    }

    private void scroll(DateAxis axis, int direction) {
        axis.setAutoRanging(false);
        LocalDateTime lower = TimeUtils.oldToNew(axis.getLowerBound());
        LocalDateTime upper = TimeUtils.oldToNew(axis.getUpperBound());

        Duration d = Duration.between(lower, upper);
        d = d.dividedBy(10).multipliedBy(direction);

        axis.setLowerBound(TimeUtils.newToOld(lower.minus(d)));
        axis.setUpperBound(TimeUtils.newToOld(upper.minus(d)));
    }

    private void zoom(DateAxis axis, int direction) {
        axis.setAutoRanging(false);
        LocalDateTime lower = TimeUtils.oldToNew(axis.getLowerBound());
        LocalDateTime upper = TimeUtils.oldToNew(axis.getUpperBound());

        Duration d = Duration.between(lower, upper);
        d = d.dividedBy(10).multipliedBy(direction);

        axis.setLowerBound(TimeUtils.newToOld(lower.minus(d)));
        axis.setUpperBound(TimeUtils.newToOld(upper.plus(d)));
    }

    private void handleKey(DateAxis xAxis, KeyCode code) {
        switch(code) {
            case LEFT:
                scroll(xAxis, -1);
                break;
            case RIGHT:
                scroll(xAxis, 1);
                break;
            case DOWN:
                zoom(xAxis, -1);
                break;
            case UP:
                scroll(xAxis, 1);
                break;
        }
        System.out.println("Done keying: " + code.name());
    }

    private void handleScroll(DateAxis xAxis, ScrollEvent e) {
        if(e.isShiftDown())
            zoom(xAxis, e.getDeltaY() > 0 ? -1 : 1);
        else
            scroll(xAxis, e.getDeltaY() > 0 ? 1 : -1);
    }

    @FXML
    private void onChangePassword() {
        PasswordResetDialog dlg = new PasswordResetDialog(username, sock);
        dlg.showAndWait();
    }

    private void handleUniqueLoginsChart() throws IOException, InterruptedException, ExecutionException {
        final LocalDate start = startDatePicker.getValue();
        final LocalDate end = endDatePicker.getValue().plusDays(1);
        FutureRequest<Map<LocalDate, Integer>> requestResult = sock.submit(new UniqueLoginsPerDayRequest(
                start, end));

        Map<LocalDate, Integer> uniqueLogins = requestResult.get();

        LocalDate c = start;
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        while(c.isBefore(end)) {
            if(uniqueLogins.containsKey(c)) {
                series.getData().add(new XYChart.Data(c.toString(), uniqueLogins.get(c)));
            } else {
                series.getData().add(new XYChart.Data(c.toString(), 0));
            }
            c = c.plusDays(1);
        }
        
        series.setName("Unique Logins per Day");
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Day");
        yAxis.setLabel("Unique Logins");
        yAxis.setTickLength(5);
        yAxis.setMinorTickLength(1);
        xAxis.setAutoRanging(true);
        final BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("New Players");
        barChart.getData().add(series);

        chartPane.setCenter(barChart);
        chartPane.layout();
    }
}
