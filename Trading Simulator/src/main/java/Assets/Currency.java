package Assets;

import Markets.CurrencyMarket;
import Markets.Market;
import Utilities.Data;
import javafx.collections.MapChangeListener;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import static Utilities.UtilityFunctions.*;

/**
 * The class representing a currency.
 */
public class Currency extends Asset{

    private String currencyID;
    private final Set<String> countries = new HashSet<>();
    private float buyRate;
    private float sellRate;
    static int CURRENCIES_MAX = 50;

    /**
     * Creates a currency.
     * @param c The currency market on which a currency will be traded.
     */
    public Currency(CurrencyMarket c){
        int n;
        ArrayList<String> data;
        try {
            Data temp = new Data("currencies.csv");
            do {
                n = generateRandomInt(0, CURRENCIES_MAX);
                data = new ArrayList<>(Arrays.asList(temp.getData().get(n).split(",")));
            } while (w.getAllAssets().containsKey(data.get(1)));

            this.setMarketName(c.getName());
            this.currencyID = data.get(0);
            this.setName(data.get(1));
            this.setInitialPrice(generateRandomFloat(1.0f, 10.0f));
            this.setCurrentPrice(this.getInitialPrice());
            this.setMinimalPrice(this.getInitialPrice());
            this.setMaximalPrice(this.getInitialPrice());
            this.getPriceData().setName(this.getName());
            this.getPercentageData().setName(this.getName());
            this.countries.add(c.getCountry());

            initializeCountries();
            monitorCurrentPrice();
            monitorCurrency();

            w.getMarkets().addListener((MapChangeListener<String, Market>) change -> {
                Market marketAdded = w.getMarkets().get(change.getKey());
                if(marketAdded.getTradingCurrency().equals(this.currencyID)){
                    this.countries.add(marketAdded.getCountry());
                }
            });

        } catch(Exception e){
            System.out.println("Exception raised!p"+e);
        }
    }

    /**
     * Looks through all the existing markets and searches for the same trading currency.
     */
    public void initializeCountries(){
        Set<Market> temp = new HashSet<>(w.getMarkets().values());
        for(Market m: temp){
            if(m.getTradingCurrency().equals(this.currencyID)){
                this.countries.add(m.getCountry());
            }
        }
    }

    /**
     * Updates buy/sell rates of the currency at regular time intervals.
     */
    public void monitorCurrency(){
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                buyRate = getCurrentPrice()*(1+w.getMarkets().get(getMarketName()).getTradeMargin())
                        * w.getMarkets().get(getMarketName()).getExchangeRate();
                sellRate = getCurrentPrice()*(1-w.getMarkets().get(getMarketName()).getTradeMargin())
                        * w.getMarkets().get(getMarketName()).getExchangeRate();
            }
        }, 0, 5000);
    }

    /**
     * Prepares the set of countries of the currency to be printed out.
     * @return A String representing the names countries.
     */
    public String getCountriesPrint(){
        StringBuilder res = new StringBuilder();
        ArrayList<String> temp = new ArrayList<>(this.countries);
        if(temp.size()==0) return res.toString();
        for(String a: temp){
            res.append(", ").append(a);
        }
        return res.substring(2);
    }

    @Override
    public String toString(){
        return super.toString()+"currency name: "+this.getName()+"\ncurrency ID: "+this.currencyID
                +"\ncurrent price: " + String.format("%.02f", this.getCurrentPrice())
                +"\nminimal price: " + String.format("%.02f", this.getMinimalPrice())
                +"\nmaximal price: " + String.format("%.02f", this.getMaximalPrice())
                +"\nbuy rate: " + String.format("%.02f", this.buyRate)
                +"\nsell rate: "+ String.format("%.02f", this.sellRate)
                +"\ncountries: "+ getCountriesPrint();
    }
}
