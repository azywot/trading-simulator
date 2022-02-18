package Assets;

import Markets.Market;
import Universe.Company;

/**
 * The class representing a share.
 */
public class Share extends Asset{

    private final Company company;

    /**
     * Create a share of the specified company.
     * @param c Company which share will be initialized.
     */
    public Share(Company c){
        super(c.getName(), c.getIPOshareValue(), c.getMarketName());
        this.company = c;
        this.getPriceData().setName(this.getName());
        this.getPercentageData().setName(this.getName());
        monitorCurrentPrice();
    }

    @Override
    public void updateBuy(float amount){
        this.company.updateCompany(-amount);
        super.updateBuy(amount);
    }

    @Override
    public void updateSell(float amount){
        this.company.updateCompany(amount);
        super.updateSell(amount);
    }

    @Override
    public float computeAmount(float money, Market market){
        float amount = super.computeAmount(money, market);
        if(amount > w.getCompanies().get(this.getName()).getSharesAvailable()){
            amount = w.getCompanies().get(this.getName()).getSharesAvailable();
        }
        return amount;
    }

    /**
     * Computes current price–earnings ratio.
     * @return The floating-point number representing p-e ratio.
     */
    public float computePEratio(){
        return this.company.getCapital()/this.company.getProfit();
    }

    public Company getCompany() {
        return company;
    }

    @Override
    public String toString(){
        return super.toString()+"company name: "+this.getName()
                +"\ncurrent price: " +String.format("%.02f", this.getCurrentPrice())
                +"\nminimal price: " + String.format("%.02f", this.getMinimalPrice())
                +"\nmaximal price: " + String.format("%.02f", this.getMaximalPrice());
    }
}
