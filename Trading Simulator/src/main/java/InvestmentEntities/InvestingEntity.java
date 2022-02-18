package InvestmentEntities;

import Assets.Asset;
import Assets.StockIndex;
import Markets.StockMarket;
import Universe.World;
import Utilities.Data;
import org.apache.commons.math3.distribution.NormalDistribution;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;
import static Utilities.UtilityFunctions.*;

/**
 * The abstract class representing an investing entity.
 */
public abstract class InvestingEntity implements Runnable{

    World world = World.INSTANCE;
    private String ID;
    private String firstName;
    private String lastName;
    private Map<Asset, Float> wallet = new HashMap<>();
    private float investmentBudget;
    private NormalDistribution norm;
    static int INVESTING_ENTITIES = 1000;

    /**
     * Creates an investing entity.
     */
    public InvestingEntity(){
        try{
            Data temp = new Data("names_surnames.csv");
            ArrayList<String> data = new ArrayList<>
                    (Arrays.asList(temp.getData().get(generateRandomInt(0, INVESTING_ENTITIES)).split(",")));
            this.firstName = data.get(0);
            this.lastName = data.get(1);
        } catch (Exception e){
            System.out.println("Exception raised! " + e);
        }
    }

    /**
     * Runs an investing entity's thread: repeatedly performs trading operations.
     */
    public void run() {
        while(true){
            float p = generateRandomFloat(0.0f, 1.0f);
            if(p < this.world.getBullBearRatio() && this.world.getAllAssets().size()!=0 && this.investmentBudget > 0){
                this.buy();
            }

            try {
                Thread.sleep((long) (60000/(this.world.getTransactionsPerMin()+0.01)));
            } catch (InterruptedException ex) {
                System.out.println("Thread interrupted!");
            }

            p = generateRandomFloat(0.0f, 1.0f);
            if(p < 1-this.world.getBullBearRatio() && this.wallet.size()!=0){
                this.sell();
            }
            this.doActions();
        }
    }

    /**
     * Selects the asset to be bought and the amount of money the investing entity will invest in it.
     */
    public void buy() {
        String selectedMarket;
        Asset chosen;
        if(norm.sample() > this.wallet.size() || this.wallet.size() == 0){
//          buy some new asset - it is not 100% sure bc there's still a chance to buy some 'old' asset
            List<String> allMarkets = world.getMarketNamesList();
            selectedMarket = allMarkets.get(generateRandomInt(0, allMarkets.size()));
            if(world.getMarkets().get(selectedMarket).getClass().getSimpleName().equals("StockMarket")){
                if(generateRandomFloat(0.0f, 1.0f) < 0.35){
                    this.invBuyIndex(null, (StockMarket) world.getMarkets().get(selectedMarket));
                    return;
                }
            }
            List<String> tempAsset = new ArrayList<String>(world.getMarkets().get(selectedMarket).getAssets().keySet());
            Collections.shuffle(tempAsset);
            chosen = world.getMarkets().get(selectedMarket).chooseAsset(
                    tempAsset.subList(0, generateRandomInt(1,tempAsset.size()+1)));
        } else {
//          buy more of an asset you already have
            List<Asset> assetList = new ArrayList<>(this.wallet.keySet());
            chosen = assetList.get(generateRandomInt(0, assetList.size()));
            if(chosen.getClass().getSimpleName().equals("StockIndex")){
                this.invBuyIndex((StockIndex) chosen, (StockMarket) world.getMarkets().get(chosen.getMarketName()));
                return;
            }
            selectedMarket = chosen.getMarketName();
        }
        float money = generateRandomFloat(1, this.investmentBudget+10);
        if(money > this.investmentBudget) money = this.investmentBudget;

        if(!world.getAllAssets().containsKey(chosen.getName())) return;
        world.getMarkets().get(selectedMarket).buyAsset(this, chosen, money);
    }

    /**
     * Selects the asset and its amount to be sold by the investing entity.
     */
    public void sell() {
        List<Asset> assetList = new ArrayList<>(this.wallet.keySet());
        Asset chosen = assetList.get(generateRandomInt(0, assetList.size()));
        if(chosen.getClass().getSimpleName().equals("StockIndex")){
            this.invSellIndex((StockIndex) chosen, (StockMarket) world.getMarkets().get(chosen.getMarketName()));
            return;
        }
        float amount = this.wallet.get(chosen)*generateRandomFloat(0.0f, (float) (1.25f-this.world.getBullBearRatio()));
        if(amount > this.wallet.get(chosen)) amount = this.wallet.get(chosen);
        if(!world.getAllAssets().containsKey(chosen.getName())) return;
        this.world.getMarkets().get(chosen.getMarketName()).sellAsset(this, chosen, amount);
    }

    /**
     * Selects the index to be bought by the investing entity and the amount of money the investing entity will invest in it.
     * @param chosen The stock index that will be bought (might be null!)
     * @param market The stock market to which the index belongs to.
     */
    public void invBuyIndex(StockIndex chosen, StockMarket market){
        if(market.getStockMarketIndexes().isEmpty()) return;
        if(chosen==null){
            List<String> indexList = new ArrayList<>(market.getStockMarketIndexes().keySet());
            String temp = indexList.get(generateRandomInt(0, indexList.size()));
            chosen = market.getStockMarketIndexes().get(temp);
        }

        float money = generateRandomFloat(1, this.investmentBudget+10);
        if(money > this.investmentBudget) money = this.investmentBudget;
        market.buyIndex(this, chosen, money);
    }

    /**
     * Selects the amount of index to be sold by the investing entity.
     * @param index The stock index that will be sold.
     * @param market The stock market to which the index belongs to.
     */
    public void invSellIndex(StockIndex index, StockMarket market){
        float amount = this.wallet.get(index)*generateRandomFloat(0.0f, (float) (1.25f-this.world.getBullBearRatio()));
        if(amount > this.wallet.get(index)) amount = this.wallet.get(index);
        market.sellIndex(this, index, amount);
    }

    /**
     * Performs actions characteristic for a certain type of investing entity.
     */
    public abstract void doActions();

    /**
     * Prepares the assets possessed by the investing entity to be printed out.
     * @return A String representing the names of assets.
     */
    public String getWalletAssets(){
        StringBuilder res = new StringBuilder();
        ArrayList<Asset> temp = new ArrayList<>(this.wallet.keySet());
        if(temp.size()==0) return res.toString();
        for(Asset a: temp){
            res.append(", ").append(a.getName());
        }
        return res.substring(2);
    }

    /**
     * Adds a specified amount of money to the investment budget.
     * @param amount The amount of money to be added.
     */
    public synchronized void addInvestmentBudget(float amount){
        this.investmentBudget += amount;
    }

    public Map<Asset, Float> getWallet() {
        return wallet;
    }

    public float getInvestmentBudget() {
        return investmentBudget;
    }

    public void setInvestmentBudget(float investmentBudget) {
        this.investmentBudget = investmentBudget;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public NormalDistribution getNorm() {
        return norm;
    }

    public void setNorm(NormalDistribution norm) {
        this.norm = norm;
    }

    @Override
    public String toString(){
        return "ID: "+ this.ID + "\nname: "+ this.firstName+ " " + this.lastName
                +"\ninvestment budget: "+String.format("%.02f", this.investmentBudget)+"\nassets:\n"+getWalletAssets();
    }
}
