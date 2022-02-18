package Universe;

import Assets.Share;
import Markets.StockMarket;
import Utilities.Data;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import static Utilities.UtilityFunctions.*;
import static java.lang.Math.abs;

/**
 * Represents a company.
 */
public class Company implements Runnable{

    World w = World.INSTANCE;
    private String name;
    private String marketName;
    private String IPOdate;
    private Share share;
    private float IPOshareValue;
    private float openingPrice;
    private float profit;
    private float revenue;
    private volatile float capital;
    private volatile float tradingVolume;
    private volatile float totalSales;
    private volatile float sharesAmount;
    private volatile float sharesAvailable;
    private final Object SharesAmountTyson = new Object();
    static int COMPANIES_MAX = 250;

    /**
     * Creates a company which shares will be trading on a specified stock market.
     * @param c The stock market assigned to a company.
     */
    public Company(StockMarket c){
        int n;
        try{
            Data data = new Data("companies.csv");
            do {
                n =  generateRandomInt(0, COMPANIES_MAX);
            } while (w.getCompanies().containsKey(data.getData().get(n)));

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String date = LocalDateTime.now().format(dtf);
            this.marketName = c.getName();
            this.name = data.getData().get(n);
            this.IPOdate = date;
            this.profit = generateRandomFloat(1000.0f, 50000.0f);
            this.revenue = 0.0f;
            this.tradingVolume = 0.0f;
            this.totalSales = 0.0f;
            this.sharesAmount = generateRandomInt(1000, 100000);
            this.sharesAvailable = this.sharesAmount;
            this.IPOshareValue = generateRandomFloat(5.0f, 20.0f);
            this.capital = this.sharesAmount * this.IPOshareValue;
            this.openingPrice = IPOshareValue;
            this.share = new Share(this);

            updateOpeningPrice();
            Thread t = new Thread(this);
            t.start();

        } catch(Exception e){
            System.out.println("Exception raised!");
        }
    }

    /**
     * Updates the opening price of a company at regular time intervals.
     */
    private void updateOpeningPrice() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                openingPrice = share.getCurrentPrice();
            }
        }, 0, 24000);
    }

    /**
     * Runs a company's thread: repeatedly updates its capital, generates revenue
     * and profit and increases the amount of shares with some probability.
     */
    @Override
    public void run(){
        while(true){
            try {
                Thread.sleep((5000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.capital = this.sharesAmount*this.share.getCurrentPrice();
            generateRevenueAndProfit();

            if(generateRandomFloat(0.0f, 1.0f) < 0.1f || this.sharesAvailable == 0.0){
                increaseShares();
            }
        }
    }

    /**
     * Updates the values of trading volume, total sales and shares available
     * after each transaction.
     * @param amount The value representing the amount of shares sold or bought (if negative).
     */
    public void updateCompany(float amount){
        synchronized(SharesAmountTyson){
            this.tradingVolume += abs(amount);
            this.totalSales += abs(amount)*this.share.getCurrentPrice();
            this.sharesAvailable += amount;
        }
    }

    /**
     * Generates revenue and profit.
     */
    public void generateRevenueAndProfit(){
        float revenue = this.capital*generateRandomFloat(0.01f, 0.25f);
        this.revenue += revenue;
        this.profit += revenue*(1-generateRandomFloat(0.05f, 0.25f));
    }

    /**
     * Increases the amount of shares.
     */
    public void increaseShares(){
        synchronized (SharesAmountTyson){
            int r = generateRandomInt(1000, 10000);
            this.sharesAmount += r;
            this.sharesAvailable += r;
        }
    }

    /**
     * Orders the buyout execution by the company.
     * @param number The amount of shares that will be attempted to buy back.
     */
    public void buyOut(float number){
        boolean toRemove = false;
        if(number > this.sharesAmount) {toRemove = true;}
        w.performBuyOut(this, number);
        if(toRemove){
            w.removeAsset(this.share.getName());
        } else synchronized(SharesAmountTyson){
            this.sharesAvailable -= number;
            this.sharesAmount -= number;
        }
    }

    @Override
    public String toString(){
        return "company: " + this.name + "\nIPO date: " + this.IPOdate
                + "\nIPO share value: " + String.format("%.02f", this.IPOshareValue)
                + "\nopening price: " + String.format("%.02f", this.openingPrice)
                + "\nprofit: " + String.format("%.02f", this.profit)
                + "\nrevenue: " + String.format("%.02f", this.revenue)
                + "\ncapital: " + String.format("%.02f", this.capital)
                + "\ntrading volume: " + String.format("%.02f", this.tradingVolume)
                + "\ntotal sales: " + String.format("%.02f", this.totalSales)
                + "\nshares available: " + String.format("%.02f", this.sharesAvailable);
    }

    public String getName() {
        return name;
    }

    public Share getShare() {
        return share;
    }

    public float getSharesAmount() {
        return sharesAmount;
    }

    public float getCapital() {
        return capital;
    }

    public float getProfit() {
        return profit;
    }

    public String getMarketName() {
        return marketName;
    }

    public float getIPOshareValue() {
        return IPOshareValue;
    }

    public float getSharesAvailable() {
        return sharesAvailable;
    }
}
