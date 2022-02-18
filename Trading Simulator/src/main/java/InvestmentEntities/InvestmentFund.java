package InvestmentEntities;

import Assets.FundParticipationUnit;
import org.apache.commons.math3.distribution.NormalDistribution;
import static Utilities.UtilityFunctions.generateRandomFloat;
import static Utilities.UtilityFunctions.generateRandomInt;

/**
 * The class representing an investment fund.
 */
public class InvestmentFund extends InvestingEntity{

    private final FundParticipationUnit participationUnit;
    private float totalMoneyGathered = 0.0f;
    private final float commissionFee;
    private volatile float partUnitAmount;
    private final Object partUnitTyson = new Object();

    /**
     * Creates an investment fund.
     */
    public InvestmentFund() {
        super();
        this.setID("InvFund-"+this.world.useInvestmentFundCounter());
        this.setInvestmentBudget(0.0f);
        this.setNorm(new NormalDistribution(generateRandomInt(15, 20), generateRandomInt(2, 10)));
        float initialPrice = generateRandomFloat(10, 1000);
        String marketName = world.getMarketNames().get("Stock").get(generateRandomInt(0, world.getMarketNames().get("Stock").size()));
        this.participationUnit = new FundParticipationUnit(this, initialPrice, marketName);
        this.partUnitAmount = generateRandomInt(100, 1000);
        this.commissionFee = generateRandomFloat(0.05f, 0.15f);

        Thread t = new Thread(this);
        t.start();
    }

    /**
     * Produces more fund participation units if needed.
     */
    @Override
    public void doActions() {
        if(this.partUnitAmount == 0){
            synchronized(partUnitTyson){
                this.partUnitAmount += generateRandomInt(10, 500);
            }
        }
    }

    /**
     * Adds a specified amount of money to the investment budget and total money gathered.
     * @param amount The amount of money to be added.
     */
    @Override
    public synchronized void addInvestmentBudget(float amount) {
        super.addInvestmentBudget(amount);
        this.totalMoneyGathered += amount;
    }

    /**
     * Adds a specified amount of fund participation units.
     * @param amount The amount of participation units to be added.
     */
    public void addPartUnitAmount(float amount){
        synchronized(partUnitTyson){
            this.partUnitAmount +=amount;
        }
    }

    public FundParticipationUnit getParticipationUnit() {
        return participationUnit;
    }

    public float getTotalMoneyGathered() {
        return totalMoneyGathered;
    }

    public float getPartUnitAmount() {
        return partUnitAmount;
    }

    public float getCommissionFee() {
        return commissionFee;
    }

    @Override
    public String toString(){
        return "ID: "+ this.getID() + "\nmanager: "+ this.getFirstName()+ " " + this.getLastName()
                +"\ninvestment budget: "+String.format("%.02f", this.getInvestmentBudget())+"\nassets:\n"+getWalletAssets();
    }
}
