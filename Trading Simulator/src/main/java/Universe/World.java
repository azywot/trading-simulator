package Universe;

import Assets.*;
import InvestmentEntities.InvestmentFund;
import InvestmentEntities.Investor;
import Markets.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import java.util.*;

/**
 * The singleton class representing the world, it contains all entities and parameters involved in trading;
 * the parameters are already set but can be modified by the user through the GUI.
 */
public enum World {

    INSTANCE;
    private double bullBearRatio = 0.5;
    private Integer transactionsPerMin = 50;
    private ObservableMap<String, Market> markets = FXCollections.observableMap(new HashMap<>());
    private Map<String, ObservableList<String>> marketNames = new HashMap<>(); // eg. "Stock Market" : {"ABC", "XYZ",...}
    private ObservableMap<String, Asset> allAssets = FXCollections.observableMap(new HashMap<>());
    private volatile int indexCounter = 0;

    private ObservableMap<String, Company> companies = FXCollections.observableMap(new HashMap<>());
    private ObservableMap<String, Investor> investors = FXCollections.observableMap(new HashMap<>());
    private ObservableMap<String, InvestmentFund> investmentFunds = FXCollections.observableMap(new HashMap<>());
    private int secondsElapsed = 0;

    private volatile int investorCounter = 0;
    private volatile int investmentFundCounter = 0;

    /**
     * Creates the world, initializes observable lists for market names of each category
     * and a timer responsible for measuring the time elapsed in seconds.
     */
    World(){
        this.marketNames.put("Stock", FXCollections.observableList(new ArrayList<>()));
        this.marketNames.put("Commodity", FXCollections.observableList(new ArrayList<>()));
        this.marketNames.put("Currency", FXCollections.observableList(new ArrayList<>()));

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                secondsElapsed++;
            }
        }, 0, 1000);
    }

    /**
     * Increments investorCounter value and returns its old value.
     * @return The integer representing ID number.
     */
    public synchronized int useInvestorCounter(){
        int res = this.investorCounter;
        this.investorCounter += 1;
        return res;
    }

    /**
     * Increments investmentFundCounter value and returns its old value.
     * @return The integer representing ID number.
     */
    public synchronized int useInvestmentFundCounter(){
        int res = this.investmentFundCounter;
        this.investmentFundCounter  += 1;
        return res;
    }

    /**
     * Adds an investor to the world's observable map 'investors'.
     */
    public synchronized void addInvestor(){
        Investor inv = new Investor();
        this.investors.put(inv.getID(), inv);
    }

    /**
     * Adds an investment fund to the world's observable map 'investmentFunds'.
     * Note: An investment fund can be created only if there is at least stock market in the world.
     */
    public synchronized void addInvestmentFund(){
        if(this.marketNames.get("Stock").isEmpty()) return;
        InvestmentFund inv = new InvestmentFund();
        this.investmentFunds.put(inv.getID(), inv);
    }

    /**
     * Maintains the ratio between available assets and the number
     * of investors/investment funds; creates investing entities if needed.
     */
    public void update() {
        int allAssets = this.allAssets.size();
        if(allAssets > 0){
            if(this.investors.size()/allAssets < 3){
                createMoreInvestors((int) Math.ceil(3*allAssets-this.investors.size()));
            }
            if((float)this.investmentFunds.size()/allAssets < 0.75){
                createMoreInvestmentFunds((int) Math.ceil(0.75*allAssets-this.investmentFunds.size()));
            }
        }
    }

    /**
     * Creates a specified number of investors.
     * @param n The number of investors to be created.
     */
    private void createMoreInvestors(int n) {
        for(int i = 0; i<n; i++){
            addInvestor();
        }
    }

    /**
     * Creates a specified number of investment funds.
     * @param n The number of investment funds to be created.
     */
    private void createMoreInvestmentFunds(int n) {
        for(int i = 0; i<n; i++){
            addInvestmentFund();
        }
    }

    /**
     * Removes a specified asset from the world.
     * @param assetName A String containing the name of an asset to be removed.
     */
    public void removeAsset(String assetName){
        if(this.markets.get(this.getAllAssets().get(assetName).getMarketName()).getAssets().size()==1) return;

        String className = this.markets.get(this.getAllAssets().get(assetName).getMarketName()).getClass().getSimpleName();
        if(className.equals("StockMarket")) {
            this.companies.remove(assetName);
        }
        this.markets.get(this.getAllAssets().get(assetName).getMarketName()).getAssets().remove(assetName);
        this.getAllAssets().remove(assetName);
    }

    /**
     * Gets the world's markets.
     * @return An observable map representing all world's markets where market names serve as keys.
     */
    public ObservableMap<String, Market> getMarkets() {
        return markets;
    }

    /**
     * Gets the world's market names divided into three categories with respect to types of markets.
     * @return An observable map representing all world's markets names categorized.
     */
    public Map<String, ObservableList<String>> getMarketNames() {
        return marketNames;
    }

    /**
     * Gets the world's market names.
     * @return A list of world's market names.
     */
    public List<String> getMarketNamesList(){
        List<String> res = new ArrayList<>();
        for(String elem: this.marketNames.keySet()){
            res.addAll(this.marketNames.get(elem));
        }
        return res;
    }

    /**
     * Gets the world's companies.
     * @return An observable map of world's companies where companies names serve as keys.
     */
    public ObservableMap<String, Company> getCompanies() {
        return companies;
    }

    /**
     * Performs a buyout operation, i.e. a company's attempt to buy back its own shares.
     * @param c A company on which a buyout operation will be performed.
     * @param targetAmount The amount of shares that will be attempted to buy back.
     */
    public void performBuyOut(Company c, float targetAmount){
        float amountBoughtOut = 0.0f;
        List<String> invEntity = new ArrayList<>(this.investors.keySet());
        Collections.shuffle(invEntity);
        for(String inv: invEntity){
            if(this.investors.get(inv).getWallet().containsKey(c.getShare())){

                float amount = this.investors.get(inv).getWallet().get(c.getShare()) < targetAmount-amountBoughtOut ?
                        this.investors.get(inv).getWallet().get(c.getShare()) : targetAmount-amountBoughtOut;
                this.markets.get(c.getShare().getMarketName()).sellAsset(this.investors.get(inv), c.getShare(), amount);
                amountBoughtOut+=amount;
                if(amountBoughtOut >= targetAmount) return;
            }
        }
        invEntity.clear();
        invEntity = new ArrayList<>(this.investmentFunds.keySet());
        Collections.shuffle(invEntity);
        for(String inv: invEntity){
            if(this.investmentFunds.get(inv).getWallet().containsKey(c.getShare())){
                float amount = this.investmentFunds.get(inv).getWallet().get(c.getShare()) < targetAmount-amountBoughtOut ?
                        this.investmentFunds.get(inv).getWallet().get(c.getShare()) : targetAmount-amountBoughtOut;
                this.markets.get(c.getShare().getMarketName()).sellAsset(this.investmentFunds.get(inv), c.getShare(), amount);
                amountBoughtOut+=amount;
                if(amountBoughtOut >= targetAmount) return;
            }
        }
    }

    /**
     * Increments the index counter.
     */
    public synchronized void addIndexCounter(){
        this.indexCounter+=1;
    }

    /**
     * Gets the number of world's indexes.
     * @return The number of world's indexes.
     */
    public int getIndexCounter() {
        return indexCounter;
    }

    /**
     * Gets the number of seconds elapsed.
     * @return The number of seconds passed since the start of instantiating the world.
     */
    public int getSecondsElapsed() {
        return secondsElapsed;
    }

    /**
     * Gets the bull/bear ratio.
     * @return The value of the bull/bear ratio.
     */
    public double getBullBearRatio() {
        return bullBearRatio;
    }

    /**
     * Sets the bull/bear ratio.
     * @param bullBearRatio The value for the bull/bear ratio to be set.
     */
    public void setBullBearRatio(double bullBearRatio) {
        this.bullBearRatio = bullBearRatio;
    }

    /**
     * Gets the number of transactions per minute.
     * @return The number of transactions per minute.
     */
    public Integer getTransactionsPerMin() {
        return transactionsPerMin;
    }

    /**
     * Sets the number of transactions per minute.
     * @param transactionsPerMin The number of transactions per minute to be set.
     */
    public void setTransactionsPerMin(Integer transactionsPerMin) {
        this.transactionsPerMin = transactionsPerMin;
    }

    /**
     * Gets the world's investors.
     * @return An observable map representing all world's investors where investors' IDs serve as keys.
     */
    public ObservableMap<String, Investor> getInvestors() {
        return investors;
    }

    /**
     * Gets the world's investment funds.
     * @return An observable map representing all world's investment funds where investment funds' IDs serve as keys.
     */
    public ObservableMap<String, InvestmentFund> getInvestmentFunds() {
        return investmentFunds;
    }

    /**
     * Adds an asset to the world's asset list.
     * @param asset An Asset to be added to the world's asset list.
     */
    public void addToAllAssets(Asset asset) {
        this.allAssets.put(asset.getName(), asset);
    }

    /**
     * Gets the world's assets.
     * @return The list of the world's assets.
     */
    public ObservableMap<String, Asset> getAllAssets() {
        return allAssets;
    }
}
