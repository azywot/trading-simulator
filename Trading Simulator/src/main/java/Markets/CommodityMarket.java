package Markets;

import Assets.Asset;
import Assets.Commodity;

/**
 * The class representing a commodity market.
 */
public class CommodityMarket extends Market<Commodity> {

    /**
     * Creates a commodity market.
     */
    public CommodityMarket(){
        super("Commodity");
    }

    @Override
    public synchronized Asset addAssetToMarket() {
        Commodity newCommodity = new Commodity(this);
        this.assets.put(newCommodity.getName(), newCommodity);
        return newCommodity;
    }
}
