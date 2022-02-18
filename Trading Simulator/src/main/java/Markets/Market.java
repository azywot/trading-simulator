package Markets;

import Assets.Asset;
import InvestmentEntities.InvestingEntity;
import Universe.World;
import Utilities.Data;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import static Utilities.UtilityFunctions.*;

/**
 * The abstract class representing a market.
 * @param <T> Asset that is traded on a market
 */
public abstract class Market<T extends Asset>{

    World w = World.INSTANCE;
    private String name;
    private String country;
    private String tradingCurrency;
    private String city;
    private String address;
    protected float tradeMargin;  // percentage margin (cost) for each trading operation
    protected float exchangeRate; // to convert from the base currency to tradingCurrency
    static int MARKETS_MAX = 220;
    protected ObservableMap<String, T> assets = FXCollections.observableMap(new HashMap<>());

    private final Object BuySellTyson = new Object();

    /**
     * Creates a new market of a specific type.
     * @param suffix A String appended to the market name associated with market type.
     */
    public Market(String suffix){
        int n;
        try{
            Data temp = new Data("markets.csv");
            ArrayList<String> data;
            do {
                n =  generateRandomInt(0, MARKETS_MAX);
                data = new ArrayList<>(Arrays.asList(temp.getData().get(n).split(",")));
            } while (marketNameChecker(data.get(0)));
            this.name = data.get(0) + " "+ suffix + " EXCH";
            this.country = data.get(1);
            this.city = data.get(0);
            this.address = data.get(2);
            this.tradingCurrency = data.get(3);
            this.tradeMargin = (float)Math.round(generateRandomFloat(0.01f, 0.1f)*1000)/1000;
            this.exchangeRate = (float)Math.round(generateRandomFloat(0.5f, 5.0f)*1000)/1000;

        } catch(Exception e){
            System.out.println("Exception raised: "+ e);
        }
    }

    /**
     * Adds an asset to the world.
     */
    public synchronized void addAssetToWorld() {
        w.addToAllAssets(addAssetToMarket());
        w.update();
    }

    /**
     * Adds a new asset to a market.
     * @return Newly added asset.
     */
    public abstract Asset addAssetToMarket();

    /**
     * Chooses the asset to be traded.
     * @param subList List of assets from which one asset will be selected.
     * @return Asset selected.
     */
    public Asset chooseAsset(List<String> subList) {
//      the list is already shuffled therefore we select some random asset
        return this.assets.get(subList.get(0));
    }

    /**
     * Performs a purchase of an asset by an investing entity.
     * @param inv The investing entity trading.
     * @param asset The asset to be bought.
     * @param money The amount of money an investing entity is willing to invest.
     */
    public void buyAsset(InvestingEntity inv, Asset asset, float money) {
        synchronized (BuySellTyson){
            float amount = asset.computeAmount(money, this);
            if(amount == 0) return;
            if(inv != null) {
                if (inv.getWallet().containsKey(asset)) {
                    float currAmount = inv.getWallet().get(asset);
                    inv.getWallet().put(asset, currAmount + amount);
                } else {
                    inv.getWallet().put(asset, amount);
                }
                inv.addInvestmentBudget(-amount*asset.getCurrentPrice()*this.exchangeRate*(1.0f+this.tradeMargin));
            }
            asset.updateBuy(amount);
        }
    }

    /**
     * Performs a sale of an asset by an investing entity.
     * @param inv The investing entity trading.
     * @param asset The asset to be sold.
     * @param amount The amount of asset the investing entity is willing to sell.
     */
    public void sellAsset(InvestingEntity inv, Asset asset, float amount) {
        synchronized (BuySellTyson){
            if(!w.getAllAssets().containsKey(asset.getName())) return;
            float moneyAdded = amount*asset.getCurrentPrice()*this.exchangeRate*(1.0f-this.tradeMargin);
            if(inv != null){
                float currAmount = inv.getWallet().get(asset);
                if(currAmount <= amount){
                    inv.getWallet().remove(asset);
                } else {
                    inv.getWallet().put(asset, currAmount-amount);
                }
                inv.addInvestmentBudget(moneyAdded);
            }
            asset.updateSell(amount);
        }
    }

    /**
     * Adds an index to a market.
     */
    public void addIndex(){}

    public String getName() {
        return name;
    }

    public ObservableMap<String, T> getAssets() {
        return this.assets;
    }

    public String getTradingCurrency() {
        return tradingCurrency;
    }

    public String getCountry() {
        return country;
    }

    public float getTradeMargin() {
        return tradeMargin;
    }

    public float getExchangeRate() {
        return exchangeRate;
    }

    @Override
    public String toString(){
        return "name: " + this.name +"\ntrading currency: "+this.tradingCurrency+"\ncountry: " + this.country + "\ncity: "
                + this.city + "\naddress: " + this.address + "\ntrade margin: " + this.tradeMargin + "\nexchange rate: " + this.exchangeRate;
    }
}
