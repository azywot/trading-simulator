package Markets;

import Assets.Asset;
import Assets.Currency;

/**
 * The class representing a currency market.
 */
public class CurrencyMarket extends Market<Currency> {

    /**
     * Creates a currency market.
     */
    public CurrencyMarket(){
        super("Currency");
    }

    @Override
    public synchronized Asset addAssetToMarket() {
        Currency newCurrency = new Currency(this);
        this.assets.put(newCurrency.getName(), newCurrency);
        return newCurrency;
    }
}
