package Assets;

import Markets.CommodityMarket;
import Utilities.Data;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static Utilities.UtilityFunctions.*;

/**
 * The class representing a commodity.
 */
public class Commodity extends Asset{

    private String tradingUnit;
    private String tradingCurrency;
    static int COMMODITIES_MAX = 35;

    /**
     * Creates a commodity.
     * @param c The commodity market on which a commodity will be traded.
     */
    public Commodity(CommodityMarket c) {
        int n;
        ArrayList<String> data;
        try {
            Data temp = new Data("commodities.csv");
            do {
                n = generateRandomInt(0, COMMODITIES_MAX);
                data = new ArrayList<>(Arrays.asList(temp.getData().get(n).split(",")));
            } while (w.getAllAssets().containsKey(data.get(0)));

            this.setMarketName(c.getName());
            this.setName(data.get(0));
            this.tradingUnit = data.get(1);
            this.tradingCurrency = c.getTradingCurrency();
            this.setInitialPrice(generateRandomFloat(1.0f, 50.0f));
            this.setCurrentPrice(this.getInitialPrice());
            this.setMinimalPrice(this.getInitialPrice());
            this.setMaximalPrice(this.getInitialPrice());
            this.getPriceData().setName(this.getName());
            this.getPercentageData().setName(this.getName());
            monitorCurrentPrice();
        } catch(Exception e){
            System.out.println("Exception raised!");
        }
    }

    @Override
    public String toString(){
        return super.toString() +"commodity name: "+this.getName()+"\ntrading currency: "+this.tradingCurrency
                +"\ntrading unit: " +this.tradingUnit +"\ncurrent price: " +String.format("%.02f", this.getCurrentPrice())
                +"\nminimal price: " + String.format("%.02f", this.getMinimalPrice())
                +"\nmaximal price: " + String.format("%.02f", this.getMaximalPrice());
    }
}
