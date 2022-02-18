package Assets;

import Markets.Market;
import Universe.World;
import javafx.scene.chart.XYChart;
import java.util.*;

/**
 * The abstract class representing an asset.
 */
public abstract class Asset {

    World w = World.INSTANCE;
    private String name;
    private volatile float currentPrice;
    private volatile float minimalPrice;
    private volatile float maximalPrice;
    private float initialPrice;
    private String marketName;
    private XYChart.Series<Number, Number> priceData = new XYChart.Series<>();
    private XYChart.Series<Number, Number> percentageData = new XYChart.Series<>();

//    needed to establish the trend ^/v
    private LinkedList<Float> trendQueue = new LinkedList<>(Arrays.asList(.0f, .0f, .0f));
    private volatile int recentBuy = 1;
    private volatile int recentSell = 1;

//    security
    private final Object PriceSalesTyson = new Object();
    private final Object TrendTyson = new Object();
    private final Object MinMaxTyson = new Object();

    /**
     * Creates an asset.
     */
    public Asset(){}

    /**
     * Creates an asset with specified name, initial price and market name.
     * @param name The String representing the name of an asset.
     * @param initialPrice The initial price of an asset.
     * @param marketName The String representing the name of the market an asset will belong to.
     */
    public Asset(String name, float initialPrice, String marketName) {
        this.name = name;
        this.initialPrice = initialPrice;
        this.currentPrice = initialPrice;
        this.minimalPrice = initialPrice;
        this.maximalPrice = initialPrice;
        this.marketName = marketName;
    }

    /**
     * Monitors the price of an asset, updates the trend, adds the data needed to plot the asset's price over time.
     */
    public void monitorCurrentPrice(){
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                priceData.getData().add(new XYChart.Data<>(w.getSecondsElapsed(),currentPrice));
                percentageData.getData().add(new XYChart.Data<>(w.getSecondsElapsed(),currentPrice/initialPrice*100));
                if(w.getSecondsElapsed()%3 == 0){
                    synchronized(TrendTyson){
                        trendQueue.add((float)recentBuy/recentSell);
                        trendQueue.remove();
                        assert(trendQueue.size() == 3);
                    }
                    synchronized(PriceSalesTyson){
                        recentBuy = 1;
                        recentSell = 1;
                    }
                }
            }
        }, 0, 1000);
    }

    /**
     * Establishes the trend of the asset.
     * @return The string informing about the trend ("UP"/"DOWN"/"" - none).
     */
    public String trend(){
        float a, b, c;
        synchronized(TrendTyson){
            a = this.trendQueue.get(0);
            b = this.trendQueue.get(1);
            c = this.trendQueue.get(2);
        }
        if(a == b && b == c) return "";
        if((a <= b && b <= c) || (a<=c && c<=b)) return "UP";
        else return "DOWN";
    }

    /**
     * Updates the minimal/maximal price of the asset found so far.
     */
    public void updateMinMax(){
        synchronized(MinMaxTyson){
            if(this.currentPrice < this.minimalPrice) this.minimalPrice = this.currentPrice;
            if(this.currentPrice > this.maximalPrice) this.maximalPrice = this.currentPrice;
        }
    }

    /**
     * Computes the amount of the asset an investing entity can buy having a specified amount of money.
     * @param money Amount of money.
     * @param market Market on which the asset is traded.
     * @return A floating-point number representing the amount of the asset.
     */
    public float computeAmount(float money, Market market){
        if(!w.getAllAssets().containsKey(this.getName())) return 0;
        return money/(this.currentPrice*market.getExchangeRate()*(1.0f+market.getTradeMargin()));
    }

    /**
     * Updates current price of the asset after an investing entity buys some.
     * @param amount The amount of asset bought.
     */
    public void updateBuy(float amount){
        synchronized(PriceSalesTyson) {
            this.recentBuy += 1;
            if(this.trend().equals("UP")){
                this.currentPrice += (float)this.recentBuy/(this.recentSell+this.recentBuy);
            }
        }
        this.updateMinMax();
    }

    /**
     * Updates current price of the asset after an investing entity sells some.
     * @param amount The amount of asset sold.
     */
    public void updateSell(float amount){
        synchronized(PriceSalesTyson) {
            this.recentSell+=1;
            if(this.trend().equals("DOWN")){
                this.currentPrice -= (float)this.recentBuy/(this.recentSell+this.recentBuy);
            }
            this.currentPrice = (this.currentPrice > 0) ? this.currentPrice : 0.1f;
        }
        this.updateMinMax();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getInitialPrice() {
        return initialPrice;
    }

    public void setInitialPrice(float initialPrice) {
        this.initialPrice = initialPrice;
    }

    public float getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(float currentPrice) {
        this.currentPrice = currentPrice;
    }

    public float getMinimalPrice() {
        return minimalPrice;
    }

    public void setMinimalPrice(float minimalPrice) {
        this.minimalPrice = minimalPrice;
    }

    public float getMaximalPrice() {
        return maximalPrice;
    }

    public void setMaximalPrice(float maximalPrice) {
        this.maximalPrice = maximalPrice;
    }

    public String getMarketName() {
        return marketName;
    }

    public void setMarketName(String marketName) {
        this.marketName = marketName;
    }

    public XYChart.Series<Number, Number> getPriceData() {
        return priceData;
    }

    public XYChart.Series<Number, Number> getPercentageData() {
        return percentageData;
    }

    /**
     * Gets current price of the specified amount of the asset expressed in the base currency.
     * @param amount Amount of the asset.
     * @return Floating-point number representing the price.
     */
    public float getBaseCurrentPrice(float amount) {
        return amount * currentPrice * w.getMarkets().get(marketName).getExchangeRate();
    }

    public Object getPriceSalesTyson() {
        return PriceSalesTyson;
    }

    @Override
    public String toString(){
        return "market: "+this.marketName+"\n";
    }
}
