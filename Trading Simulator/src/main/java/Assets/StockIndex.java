package Assets;

import Markets.StockMarket;
import Universe.Company;
import Universe.World;
import Utilities.Data;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static Utilities.UtilityFunctions.*;

/**
 * The class representing a stock index.
 */
public class StockIndex extends Asset{

    World world = World.INSTANCE;
    private final List<Company> companies = new ArrayList<>();

    /**
     * Creates an index of the specified stock market.
     * @param stockMarket The stock market a stock index will belong to.
     */
    public StockIndex(StockMarket stockMarket){
        int n;
        try {
            Data temp = new Data("indexes.csv");
            this.setName(temp.getData().get(w.getIndexCounter()));
            w.addIndexCounter();
            n = generateRandomInt(0, stockMarket.getAssets().size()) + 1;
            List<Share> shares = shuffleSelectN(stockMarket.getAssets(), n);
            for (Share s : shares) {
                this.companies.add(s.getCompany());
            }
            this.setMarketName(stockMarket.getName());
        } catch(Exception e){
            System.out.println("Exception raised!");
        }
    }

    /**
     * Computes the value of the index.
     * @param type A String representing the type of transaction involving the index (Buy/Sell).
     * @return The floating-point number representing the value of the index.
     */
    public float computeValue(String type){
        float total = 0.f;
        for (Company com: this.companies) {
            total += com.getSharesAmount() * com.getShare().getCurrentPrice();
        }
        if(type==null) return total;
        float tradeMargin = world.getMarkets().get(this.getMarketName()).getTradeMargin();
        if(type.equals("Buy")) return total*(1+tradeMargin);
        else return total*(1-tradeMargin);
    }

    @Override
    public String toString(){
        return "name: "+this.getName()+"\ncompanies: "+this.companies+"\ncurrent value: "+this.computeValue(null);
    }

    public List<Company> getCompanies() {
        return companies;
    }
}
