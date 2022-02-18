package Assets;

import InvestmentEntities.InvestmentFund;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 * The class representing a fund participation unit.
 */
public class FundParticipationUnit extends Asset{

    private final InvestmentFund invFund;

    /**
     * Creates a fund participation unit.
     * @param invFund The fund which will be assigned to a fund participation unit.
     * @param initialPrice Initial price of a unit.
     * @param marketName String representing the market name to which a unit will belong.
     */
    public FundParticipationUnit(InvestmentFund invFund, float initialPrice, String marketName){
        super(invFund.getID(), initialPrice, marketName);
        this.invFund = invFund;
    }

    /**
     * Calls for an update at regular time intervals.
     */
    @Override
    public void monitorCurrentPrice(){
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateCurrentPrice();
            }
        }, 0, 5000);
    }

    /**
     * Updates current price of the fund participation unit based on investing outcome of its fund.
     */
    public void updateCurrentPrice(){
        synchronized(this.getPriceSalesTyson()){
            float total = invFund.getInvestmentBudget();
            HashMap<Asset, Float> tempWallet = new HashMap<>(this.invFund.getWallet());
            if(tempWallet.isEmpty()) return;
            for(Asset a: tempWallet.keySet()){
                total += a.getBaseCurrentPrice(tempWallet.get(a));
            }
            this.setCurrentPrice(this.getCurrentPrice()*(total/this.invFund.getTotalMoneyGathered()));
        }
    }

    public InvestmentFund getInvFund() {
        return invFund;
    }
}
