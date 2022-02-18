package InvestmentEntities;

import Assets.Asset;
import Assets.FundParticipationUnit;
import Markets.StockMarket;
import org.apache.commons.math3.distribution.NormalDistribution;
import java.util.*;

/**
 * The class representing an investor.
 */
import static Utilities.UtilityFunctions.*;

public class Investor extends InvestingEntity{

    private Map<FundParticipationUnit, Float> fundsWallet = new HashMap<>();

    /**
     * Creates an investor.
     */
    public Investor() {
        super();
        this.setID("Inv-"+this.world.useInvestorCounter());
        this.setInvestmentBudget(generateRandomInt(500, 5000));
        this.setNorm(new NormalDistribution(generateRandomInt(5, 10), generateRandomInt(1, 5)));

        Thread t = new Thread(this);
        t.start();
    }

    /**
     * Repeatedly and with some probability adds a random amount of money to the investment budget,
     * makes an investor invest in/withdraw from an investment fund.
     */
    @Override
    public void doActions() {
        float p = generateRandomFloat(0.0f, 1.0f);
        if(p * 4.0f < this.world.getBullBearRatio()){
            addInvestmentBudget(generateRandomFloat(0.0f, (float) (this.getNorm().sample()*1000.0f)));
        }
        p = generateRandomFloat(0.0f, 1.0f);
        if(p < 0.05f && !world.getInvestmentFunds().isEmpty()){
            this.investInFund();
        }
        p = generateRandomFloat(0.0f, 1.0f);
        if(p < 0.05f && !this.fundsWallet.isEmpty()){
            this.withdrawFromFund();
        }
    }

    /**
     * Selects the investment fund and the amount of money the investor will invest in it.
     */
    public void investInFund(){
        float money = generateRandomFloat(0.0f, this.getInvestmentBudget()/5);
        List<String> invFunds = new ArrayList<>(world.getInvestmentFunds().keySet());
        InvestmentFund fund = world.getInvestmentFunds().get(invFunds.get(generateRandomInt(0, invFunds.size())));
        StockMarket sm = (StockMarket) world.getMarkets().get(fund.getParticipationUnit().getMarketName());
        sm.buyFundPartUnit(this, fund.getParticipationUnit(), money);
    }

    /**
     * Selects the investment fund and the amount of its participation units to be sold by the investor.
     */
    public void withdrawFromFund(){
        List<FundParticipationUnit> temp = new ArrayList<>(this.fundsWallet.keySet());
        FundParticipationUnit fundUnit = temp.get(generateRandomInt(0, temp.size()));
        float amount = generateRandomFloat(0.0f, this.fundsWallet.get(fundUnit));
        StockMarket sm = (StockMarket) world.getMarkets().get(fundUnit.getMarketName());
        sm.sellFundPartUnit(this, fundUnit, amount);
    }

    /**
     * Prepares the investment fund names of fund participation units possessed by the investor to be printed out.
     * @return A String representing the names of investment funds.
     */
    public String getFundWalletAssets(){
        StringBuilder res = new StringBuilder();
        ArrayList<Asset> temp = new ArrayList<>(this.fundsWallet.keySet());
        if(temp.size()==0) return res.toString();
        for(Asset a: temp){
            res.append(", ").append(a.getName());
        }
        return res.substring(2);
    }

    public Map<FundParticipationUnit, Float> getFundsWallet() {
        return fundsWallet;
    }

    @Override
    public String toString(){
        if(this.fundsWallet.isEmpty()) return super.toString();
        return super.toString()+"\ninvestment funds:\n"+ getFundWalletAssets();
    }
}