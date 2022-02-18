package GUI;

import Assets.Asset;
import Assets.Commodity;
import Assets.Currency;
import Assets.Share;
import InvestmentEntities.InvestmentFund;
import InvestmentEntities.Investor;
import Markets.CommodityMarket;
import Markets.CurrencyMarket;
import Markets.Market;
import Markets.StockMarket;
import Universe.Company;
import Universe.World;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.MapChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class PrimaryController implements Initializable {

    World w = World.INSTANCE;


    private final Image BBratioIcon = new Image(String.valueOf(getClass().getResource("bbratio_icon.png")));
    private final Image transMinIcon = new Image(String.valueOf(getClass().getResource("clock_fire.png")));
    private final Image bezos = new Image(String.valueOf(getClass().getResource("bezos.jpeg")));
    private final Object Tyson = new Object();

    private final ChangeListener<String> marketClicked = new ChangeListener<>() {
        @Override
        public void changed(ObservableValue<? extends String> v, String oldValue, String newValue) {
            if (w.getMarkets().get(newValue) == null) return;

            addAssetButton.setDisable(false);
            setMarketButtons(newValue);
            selectedMarket = newValue;
            selectedMarketLabel.setText(selectedMarket);
            selectedMarketLabel.setVisible(true);
            infoLabel.setText(w.getMarkets().get(newValue).toString());
            addAssetButton.setVisible(true);
        }
    };

    private String selectedMarket;
    private String selectedAsset;
    private String selectedCompany;

    @FXML
    private ListView<String> stockMarketsListView;

    @FXML
    private ListView<String> commodityMarketsListView;

    @FXML
    private ListView<String> currencyMarketsListView;

    @FXML
    private ListView<String> assetListView;

    @FXML
    private ListView<String> investorsListView;

    @FXML
    private ListView<String> investmentFundsListView;

    @FXML
    private ListView<String> companiesListView;

    @FXML
    private ImageView BBimageView;

    @FXML
    private ImageView transMinimageView;

    @FXML
    private ImageView bezosImageView;

    @FXML
    private Slider BBratioSlider;

    @FXML
    private Slider transPerMinSlider;

    @FXML
    private Label BBratioLabel;

    @FXML
    private Label transMinLabel;

    @FXML
    private Label infoLabel;

    @FXML
    private Label invInfoLabel;

    @FXML
    private Label selectedMarketLabel;

    @FXML
    private Label assetLabel;

    @FXML
    private Button addAssetButton;

    @FXML
    private Button addIndexButton;

    @FXML
    private Button multiChartButton;

    @FXML
    private Button buyOutButton;

    @FXML
    private Button removeButton;

    @FXML
    private LineChart<Number, Number> primaryLineChart;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        BBimageView.setImage(BBratioIcon);
        transMinimageView.setImage(transMinIcon);
        bezosImageView.setImage(bezos);
        addIndexButton.setVisible(false);

        /* SLIDERS */
        BBratioLabel.setText(String.format("%.2f", BBratioSlider.getValue()));
        BBratioSlider.valueProperty().addListener((observableValue, number, t1) -> {
            w.setBullBearRatio(BBratioSlider.getValue());
            BBratioLabel.setText(String.format("%.2f", BBratioSlider.getValue()));
        });

        transMinLabel.setText(String.format("%d", (int) transPerMinSlider.getValue()));
        transPerMinSlider.valueProperty().addListener((observableValue, number, t1) -> {
            w.setTransactionsPerMin((int) transPerMinSlider.getValue());
            transMinLabel.setText(String.format("%d", (int) transPerMinSlider.getValue()));
        });

        /* MARKET LISTVIEWS */
        stockMarketsListView.setItems(w.getMarketNames().get("Stock"));
        commodityMarketsListView.setItems(w.getMarketNames().get("Commodity"));
        currencyMarketsListView.setItems(w.getMarketNames().get("Currency"));

        stockMarketsListView.getSelectionModel().selectedItemProperty().addListener(marketClicked);
        commodityMarketsListView.getSelectionModel().selectedItemProperty().addListener(marketClicked);
        currencyMarketsListView.getSelectionModel().selectedItemProperty().addListener(marketClicked);

        /* ASSET LISTVIEW */
        assetListView.getItems().setAll(w.getAllAssets().keySet());
        w.getAllAssets().addListener((MapChangeListener<String, Asset>) change -> {
            assetListView.getItems().removeAll(change.getKey());
            if (change.wasAdded()) {
                assetListView.getItems().add(change.getKey());
            }
        });

        /* ASSET LINE CHART ON CLICK */
        assetListView.getSelectionModel().selectedItemProperty()
                .addListener( (v, oldValue, newValue) -> {
                    bezosImageView.setVisible(false);
                    multiChartButton.setVisible(true);
                    primaryLineChart.setVisible(true);

                    synchronized(Tyson){
                        if(w.getAllAssets().get(newValue)==null || newValue.equals(selectedAsset)) return;

                        removeButton.setDisable(false);
                        selectedAsset = newValue;
                        infoLabel.setText(w.getAllAssets().get(selectedAsset).toString());
                        assetLabel.setText(selectedAsset);
                        assetLabel.setVisible(true);
                        plotAsset(w.getAllAssets().get(selectedAsset));
                    }
                });

        /* INVESTORS LISTVIEW */
        investorsListView.getItems().setAll(w.getInvestors().keySet());
        w.getInvestors().addListener((MapChangeListener<String, Investor>) change -> investorsListView.getItems().add(change.getKey()));
        investorsListView.getSelectionModel().selectedItemProperty()
                .addListener( (v, oldValue, newValue) -> {
                    if(w.getInvestors().get(newValue)==null) return ;
                    investmentFundsListView.getSelectionModel().clearSelection();
                    invInfoLabel.setText(w.getInvestors().get(newValue).toString());
                });


        investmentFundsListView.getItems().setAll(w.getInvestmentFunds().keySet());
        w.getInvestmentFunds().addListener((MapChangeListener<String, InvestmentFund>) change -> investmentFundsListView.getItems().add(change.getKey()));
        investmentFundsListView.getSelectionModel().selectedItemProperty()
                .addListener( (v, oldValue, newValue) -> {
                    if(w.getInvestmentFunds().get(newValue)==null) return ;
                    investorsListView.getSelectionModel().clearSelection();
                    invInfoLabel.setText(w.getInvestmentFunds().get(newValue).toString());
                });

        /* COMPANIES LISTVIEW */
        companiesListView.getItems().setAll(w.getCompanies().keySet());
        w.getCompanies().addListener((MapChangeListener<String, Company>) change -> {
            companiesListView.getItems().removeAll(change.getKey());
            if (change.wasAdded()) {
                companiesListView.getItems().add(change.getKey());
            }
        });

        companiesListView.getSelectionModel().selectedItemProperty()
                .addListener( (v, oldValue, newValue) -> {
                    if(w.getCompanies().get(newValue)==null) return;
                    selectedCompany = newValue;
                    buyOutButton.setDisable(false);
                    infoLabel.setText(w.getCompanies().get(selectedCompany).toString());
                });

    }

    @FXML private void addStockMarket(){
        Market<Share> m = new StockMarket();
        w.getMarkets().put(m.getName(), m);
        w.getMarketNames().get("Stock").add(m.getName());
        m.addAssetToWorld();
    }
    @FXML private void addCommodityMarket(){
        Market<Commodity> m = new CommodityMarket();
        w.getMarkets().put(m.getName(), m);
        w.getMarketNames().get("Commodity").add(m.getName());
        m.addAssetToWorld();
    }

    @FXML private void addCurrencyMarket(){
        Market<Currency> m = new CurrencyMarket();
        w.getMarkets().put(m.getName(), m);
        w.getMarketNames().get("Currency").add(m.getName());
        m.addAssetToWorld();
    }

    @FXML private void addAsset(){
        w.getMarkets().get(selectedMarket).addAssetToWorld();
    }

    @FXML void addIndex(){
        w.getMarkets().get(selectedMarket).addIndex();
    }

    @FXML private void addInvestor(){
        w.addInvestor();
    }

    @FXML private void addInvestmentFund(){
        w.addInvestmentFund();
    }

    @FXML
    private void setMarketButtons(String temp){
        String className = w.getMarkets().get(temp).getClass().getSimpleName();
        if(className.equals("StockMarket")){
            commodityMarketsListView.getSelectionModel().clearSelection();
            currencyMarketsListView.getSelectionModel().clearSelection();
            addAssetButton.setText("NEW COMPANY");
            addIndexButton.setVisible(true);

        } else if(className.equals("CommodityMarket")){
            stockMarketsListView.getSelectionModel().clearSelection();
            currencyMarketsListView.getSelectionModel().clearSelection();
            addAssetButton.setText("NEW COMMODITY");
            addIndexButton.setVisible(false);
        } else {
            commodityMarketsListView.getSelectionModel().clearSelection();
            stockMarketsListView.getSelectionModel().clearSelection();
            addAssetButton.setText("NEW CURRENCY");
            addIndexButton.setVisible(false);
        }
    }

    @FXML
    public Stage setStage(Parent root, String title){
        Stage stage = new Stage();
        Image icon = new Image(String.valueOf(getClass().getResource("icon.png")));
        stage.getIcons().add(icon);

        stage.setTitle(title);
        stage.setScene(new Scene(root));
        stage.show();
        return stage;
    }

    @FXML
    public void showMultiChart() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("multichart.fxml"));
        Parent root = fxmlLoader.load();
        MultiChartController mcc = fxmlLoader.getController();
        mcc.setListView(assetListView);
        setStage(root, "Multi Line Charts");
    }

    @FXML
    public void forceBuyOut() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("buyout.fxml"));
        Parent root = fxmlLoader.load();
        BuyOutController boc = fxmlLoader.getController();
        Stage stage = setStage(root, selectedCompany+" Buyout");
        boc.setCompany(selectedCompany);
        boc.setStage(stage);
    }

    @FXML
    public synchronized void plotAsset(Asset asset){
        primaryLineChart.setAnimated(false);
        primaryLineChart.getData().clear();
        primaryLineChart.setAnimated(true);
        primaryLineChart.getData().add(asset.getPriceData());
    }

    @FXML
    public void removeAsset(){
        w.removeAsset(selectedAsset);
    }
}
