package Markets;

import Assets.*;
import InvestmentEntities.InvestingEntity;
import InvestmentEntities.Investor;
import Universe.Company;
import static Utilities.UtilityFunctions.*;
import java.util.*;

/**
 * The class representing a stock market.
 */
public class StockMarket extends Market<Share> {

    private Map<String, StockIndex> stockMarketIndexes = new HashMap<>();
    private final Object IndexTyson = new Object();
    private final Object FundTyson = new Object();

    /**
     * Creates a stock market.
     */
    public StockMarket(){
        super("Stock");
    }

    /**
     * Performs a purchase of a fund participation unit by an investor.
     * @param inv The investor trading.
     * @param partUnit The fund participation units to be bought.
     * @param money The amount of money an investor is willing to invest.
     */
    public void buyFundPartUnit(Investor inv, FundParticipationUnit partUnit, float money){
        synchronized(FundTyson){
            if(partUnit.getInvFund().getPartUnitAmount()==0) return;
            float amount = money/partUnit.getCurrentPrice()/(1+this.tradeMargin);
            if(amount > partUnit.getInvFund().getPartUnitAmount()){
                amount = partUnit.getInvFund().getPartUnitAmount();
                money = amount*partUnit.getCurrentPrice()*(1+this.tradeMargin);
            }
            if (inv.getFundsWallet().containsKey(partUnit)) {
                float currAmount = inv.getFundsWallet().get(partUnit);
                inv.getFundsWallet().put(partUnit, currAmount + amount);
            } else {
                inv.getFundsWallet().put(partUnit, amount);
            }
            inv.addInvestmentBudget(-money);
            partUnit.getInvFund().addPartUnitAmount(-amount);
            partUnit.getInvFund().addInvestmentBudget(money);
        }
    }

    /**
     * Performs a sale of a fund participation unit by an investor.
     * @param inv The investor trading.
     * @param partUnit The fund participation unit to be sold.
     * @param amount The amount of fund participation unit the investor is willing to sell.
     */
    public void sellFundPartUnit(Investor inv, FundParticipationUnit partUnit, float amount){
        synchronized(FundTyson){
            float moneyAdded = amount*partUnit.getCurrentPrice()*(1-partUnit.getInvFund().getCommissionFee())*(1-this.tradeMargin);
            float currAmount = inv.getFundsWallet().get(partUnit);
            if(currAmount <= amount){
                inv.getFundsWallet().remove(partUnit);
            } else {
                inv.getFundsWallet().put(partUnit, currAmount-amount);
            }
            inv.addInvestmentBudget(moneyAdded);
            partUnit.getInvFund().addPartUnitAmount(amount);
            moneyAdded = Math.min(moneyAdded, partUnit.getInvFund().getInvestmentBudget());
            partUnit.getInvFund().addInvestmentBudget(-moneyAdded);
        }
    }

    /**
     * Performs a purchase of an index by an investing entity.
     * @param inv The investing entity trading.
     * @param index The index to be bought.
     * @param money The amount of money an investing entity is willing to invest.
     */
    public void buyIndex(InvestingEntity inv, StockIndex index, float money){
        synchronized(IndexTyson){
            float amount = money/index.computeValue("Buy")/this.exchangeRate;
            float moneyFraction = money/index.getCompanies().size();
            for(Company c: index.getCompanies()){
                this.buyAsset(null, c.getShare(), moneyFraction);
            }
            if (inv.getWallet().containsKey(index)) {
                float currAmount = inv.getWallet().get(index);
                inv.getWallet().put(index, currAmount + amount);
            } else {
                inv.getWallet().put(index, amount);
            }
            inv.addInvestmentBudget(-money);
        }
    }

    /**
     * Performs a sale of an index by an investing entity.
     * @param inv The investing entity trading.
     * @param index The index to be sold.
     * @param amount The amount of index the investing entity is willing to sell.
     */
    public synchronized void sellIndex(InvestingEntity inv, StockIndex index, float amount){
        synchronized(IndexTyson){
            float currAmount = inv.getWallet().get(index);
            float moneyAdded = index.computeValue("Sell")*this.exchangeRate;
            float amountFraction = amount/index.getCompanies().size();
            for(Company c: index.getCompanies()){
                this.sellAsset(null, c.getShare(), amountFraction);
            }
            if(currAmount <= amount){
                inv.getWallet().remove(index);
            } else {
                inv.getWallet().put(index, currAmount-amount);
            }
            inv.addInvestmentBudget(moneyAdded);
        }
    }

    /**
     * Prepares the map of stock indexes to be printed out.
     * @return A String representing the names of indexes.
     */
    public String getStockMarketIndexesPrint(){
        StringBuilder res = new StringBuilder();
        ArrayList<String> temp = new ArrayList<>(this.stockMarketIndexes.keySet());
        if(temp.size()==0) return res.toString();
        for(String a: temp){
            res.append(", ").append(a);
        }
        return "\nindexes: " + res.substring(2);
    }

    /**
     * Gets all stock market indexes.
     * @return A map containing all stock market indexes where names of indexes serve as a key.
     */
    public  Map<String, StockIndex> getStockMarketIndexes() {
        return stockMarketIndexes;
    }

    @Override
    public synchronized Asset addAssetToMarket() {
        Company newCompany = new Company(this);
        w.getCompanies().put(newCompany.getName(), newCompany);
        this.assets.put(newCompany.getShare().getName(), newCompany.getShare());
        return newCompany.getShare();
    }

    @Override
    public synchronized void addIndex(){
        StockIndex newIndex = new StockIndex(this);
        this.stockMarketIndexes.put(newIndex.getName(), newIndex);
    }

    @Override
    public Asset chooseAsset(List<String> subList) {
        String current = subList.get(0);
        float currentPEratio = this.assets.get(current).computePEratio();
        float PEratio;
        float prob;

        for(String name: subList){
            PEratio = this.assets.get(name).computePEratio();
            prob = generateRandomFloat(0.1f, 1.0f);
            if(prob > PEratio/(PEratio+currentPEratio)){
                current = name;
                currentPEratio = PEratio;
            }
        }
        return this.assets.get(current);
    }

    @Override
    public String toString(){
        String res = super.toString();
        return res +getStockMarketIndexesPrint();
    }
}
