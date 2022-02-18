# trading-simulator
simulation of transactions made by investing entities on various markets (GUI included)

## project description
The main entities in the simulation are markets, indexes, companies, investors, funds, currencies, and commodities. Investors can trade assets on various markets directly or
through units of investment funds. GUI enables the user to create markets, indexes, companies, currencies, commodities, investors and funds (in addition, the number of investing entities is propotional to the number of assets available on the markets). Each investing entity/company is a seperate thread.

## how to use it
To start the simulation one has to create a market of any type. Selecting a market enables to add a new asset or index to it (the first asset is created automatically). Selecting an asset from the list shows a plot of the asset's prices over time. It is possible to draw multiple assets on one plot on a percentage scale. One can read about basic information of any entity after clicking on it. There is a possibility to force a buy out operation by the company of a user-specified share amount; share reclaimed by the company disappear form the market. The sliders provided enable the user to set world parameters, namely transactions per minute and the bear/bull ratio.
