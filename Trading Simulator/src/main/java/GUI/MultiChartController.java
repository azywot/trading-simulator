package GUI;

import Assets.Asset;
import Universe.World;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.util.*;

public class MultiChartController {

    World w = World.INSTANCE;
    private final List<XYChart.Series<Number, Number>> temp = new ArrayList<>();
    private final Object Tyson = new Object();

    @FXML
    private LineChart<Number, Number> multiLineChart;

    @FXML
    private ListView<String> assetListView;

    @FXML
    private ImageView bezosImageView;

    public void setListView(ListView<String> a) {
        bezosImageView.setImage(new Image(String.valueOf(getClass().getResource("bezos.jpeg"))));
        assetListView.setItems(a.getItems());
        assetListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        assetListView.getSelectionModel().selectedItemProperty().addListener((changed, oldVal, newVal) -> {
            bezosImageView.setVisible(false);
            synchronized(Tyson){
                List<String> selected = assetListView.getSelectionModel().getSelectedItems();
                for(String s: selected){
                    temp.add(w.getAllAssets().get(s).getPercentageData());
                    if(!inMultichart(s)){
                        plotData(w.getAllAssets().get(s));
                    }
                }
                multiLineChart.setAnimated(false);
                multiLineChart.getData().retainAll(temp);
                multiLineChart.setAnimated(true);
                temp.clear();
            }
        });
    }

    public boolean inMultichart(String s){
        for(XYChart.Series<Number, Number> series : multiLineChart.getData()) {
            if(series.getName().equals(s)) return true;
        }
        return false;
    }

    public synchronized void plotData(Asset asset){
        multiLineChart.getData().add(asset.getPercentageData());
    }
}
