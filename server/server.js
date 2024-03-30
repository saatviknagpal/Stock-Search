const express = require("express");
const app = express();
const cors = require("cors");
const PORT = 8080;
const http = require("http");
const mongoose = require("mongoose");

app.use(cors());
app.use(express.json());

const mongoURI =
  "mongodb+srv://saatvik:iQcWXpWyOTLsLScy@cluster0.onbpcop.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0";

const axios = require("axios");
const Watchlist = require("./models/WatchList");
const Wallet = require("./models/Wallet");
const Portfolio = require("./models/Portfolio");

mongoose
  .connect(mongoURI, { useUnifiedTopology: true })
  .then(() => {
    console.log("MongoDB connected successfully.");
    initializeWallet();
  })
  .catch((err) => console.error("MongoDB connection error:", err));

const initializeWallet = async () => {
  const Wallet = require("./models/Wallet");
  const wallet = await Wallet.findOne();
  if (!wallet) {
    const newWallet = new Wallet({ balance: 25000 });
    await newWallet.save();
    console.log("Wallet initialized with $25,000");
  }
};

app.get("/api/home/:ticker", async (req, res) => {
  const tickerSymbol = req.params.ticker;

  try {
    const apiResponse = await axios.get(
      `https://finnhub.io/api/v1/search?q=${encodeURIComponent(
        tickerSymbol
      )}&token=cmvvsmhr01qkcvkff840cmvvsmhr01qkcvkff84g`
    );

    const filteredResults = apiResponse.data.result.filter(
      (stock) => stock.type === "Common Stock" && !stock.symbol.includes(".")
    );

    console.log(filteredResults);
    res.json(filteredResults);
    // console.log("HI", apiResponse.data);
  } catch (error) {
    // console.error(error);
    if (error.response) {
      res.status(error.response.status).json(error.response.data);
    } else {
      res
        .status(500)
        .json({ message: "Error setting up request to Finnhub API" });
    }
  }
});

app.get("/api/stockDetails/:ticker", async (req, res) => {
  const tickerSymbol = req.params.ticker;

  try {
    const apiResponse = await axios.get(
      `https://finnhub.io/api/v1/stock/profile2?symbol=${tickerSymbol}&token=cmvvsmhr01qkcvkff840cmvvsmhr01qkcvkff84g`
    );

    res.json(apiResponse.data);
  } catch (error) {
    if (error.response) {
      res.status(error.response.status).json(error.response.data);
    } else {
      res
        .status(500)
        .json({ message: "Error setting up request to Finnhub API" });
    }
  }
});

app.get("/api/stockQuote/:ticker", async (req, res) => {
  const tickerSymbol = req.params.ticker;

  try {
    const apiResponse = await axios.get(
      `https://finnhub.io/api/v1/quote?symbol=${tickerSymbol}&token=cmvvsmhr01qkcvkff840cmvvsmhr01qkcvkff84g`
    );

    res.json(apiResponse.data);
  } catch (error) {
    if (error.response) {
      res.status(error.response.status).json(error.response.data);
    } else {
      res
        .status(500)
        .json({ message: "Error setting up request to Finnhub API" });
    }
  }
});

app.get("/api/companyPeers/:ticker", async (req, res) => {
  const tickerSymbol = req.params.ticker;

  try {
    const apiResponse = await axios.get(
      `https://finnhub.io/api/v1/stock/peers?symbol=${tickerSymbol}&token=cmvvsmhr01qkcvkff840cmvvsmhr01qkcvkff84g`
    );

    res.json(apiResponse.data);
  } catch (error) {
    if (error.response) {
      res.status(error.response.status).json(error.response.data);
    } else {
      res
        .status(500)
        .json({ message: "Error setting up request to Finnhub API" });
    }
  }
});

app.get("/api/hourlyData/:ticker", async (req, res) => {
  const tickerSymbol = req.params.ticker;
  const apiKey = "XFSW7waE93IvXjk02Y8Vi2SNAeOnuJXr";
  const currentDate = new Date();
  currentDate.setMinutes(
    currentDate.getMinutes() + currentDate.getTimezoneOffset() - 480
  );

  const isOpen =
    currentDate.getHours() >= 6 &&
    currentDate.getHours() < 13 &&
    currentDate.getDay() >= 1 &&
    currentDate.getDay() <= 5;

  let startDate;
  if (isOpen) {
    startDate = new Date(currentDate);
    startDate.setDate(currentDate.getDate() - 1);
  } else {
    startDate = new Date(currentDate);
    if (currentDate.getDay() === 1) {
      startDate.setDate(currentDate.getDate() - 3);
    } else {
      startDate.setDate(currentDate.getDate() - 2);
    }
  }

  const formattedStartDate = `${startDate.getFullYear()}-${String(
    startDate.getMonth() + 1
  ).padStart(2, "0")}-${String(startDate.getDate()).padStart(2, "0")}`;
  const formattedEndDate = `${currentDate.getFullYear()}-${String(
    currentDate.getMonth() + 1
  ).padStart(2, "0")}-${String(currentDate.getDate()).padStart(2, "0")}`;

  const url = `https://api.polygon.io/v2/aggs/ticker/${tickerSymbol}/range/1/hour/${formattedStartDate}/${formattedEndDate}?adjusted=true&sort=asc&apiKey=${apiKey}`;

  try {
    const response = await axios.get(url);
    res.json(response.data);
  } catch (error) {
    console.error("Error fetching data:", error);
    res.status(500).json({ message: "Error fetching data" });
  }
});

app.get("/api/stockNews/:ticker", async (req, res) => {
  const tickerSymbol = req.params.ticker;

  const currentDate = new Date().toISOString().split("T")[0];
  const sevenDaysAgo = new Date();
  sevenDaysAgo.setDate(sevenDaysAgo.getDate() - 7);
  const formattedSevenDaysAgo = sevenDaysAgo.toISOString().split("T")[0];

  try {
    const apiResponse = await axios.get(
      `https://finnhub.io/api/v1/company-news?symbol=${tickerSymbol}&from=${formattedSevenDaysAgo}&to=${currentDate}&token=cmvvsmhr01qkcvkff840cmvvsmhr01qkcvkff84g`
    );

    res.json(apiResponse.data);
  } catch (error) {
    if (error.response) {
      res.status(error.response.status).json(error.response.data);
    } else {
      res
        .status(500)
        .json({ message: "Error setting up request to Finnhub API" });
    }
  }
});

app.get("/api/charts/:ticker", async (req, res) => {
  const tickerSymbol = req.params.ticker;

  let currentDate = new Date();
  const formattedFromDate = `${currentDate.getFullYear() - 2}-${String(
    currentDate.getMonth() + 1
  ).padStart(2, "0")}-${String(currentDate.getDate()).padStart(2, "0")}`;

  console.log(formattedFromDate);

  currentDate = currentDate.toISOString().split("T")[0];
  const url = `https://api.polygon.io/v2/aggs/ticker/${tickerSymbol}/range/1/day/${formattedFromDate}/${currentDate}?adjusted=true&sort=asc&apiKey=XFSW7waE93IvXjk02Y8Vi2SNAeOnuJXr`;

  try {
    const response = await axios.get(url);
    res.json(response.data);
  } catch (error) {
    console.error("Error fetching data:", error);
    res.status(500).json({ message: "Error fetching data" });
  }
});

app.get("/api/insiderSentiment/:ticker", async (req, res) => {
  const tickerSymbol = req.params.ticker;

  try {
    const apiResponse = await axios.get(
      `https://finnhub.io/api/v1/stock/insider-sentiment?symbol=${tickerSymbol}&token=cmvvsmhr01qkcvkff840cmvvsmhr01qkcvkff84g`
    );

    res.json(apiResponse.data);
  } catch (error) {
    if (error.response) {
      res.status(error.response.status).json(error.response.data);
    } else {
      res
        .status(500)
        .json({ message: "Error setting up request to Finnhub API" });
    }
  }
});

app.get("/api/recommendation/:ticker", async (req, res) => {
  const tickerSymbol = req.params.ticker;

  try {
    const apiResponse = await axios.get(
      `https://finnhub.io/api/v1/stock/recommendation?symbol=${tickerSymbol}&token=cmvvsmhr01qkcvkff840cmvvsmhr01qkcvkff84g`
    );

    res.json(apiResponse.data);
  } catch (error) {
    if (error.response) {
      res.status(error.response.status).json(error.response.data);
    } else {
      res
        .status(500)
        .json({ message: "Error setting up request to Finnhub API" });
    }
  }
});

app.get("/api/earnings/:ticker", async (req, res) => {
  const tickerSymbol = req.params.ticker;

  try {
    const apiResponse = await axios.get(
      `https://finnhub.io/api/v1/stock/earnings?symbol=${tickerSymbol}&token=cmvvsmhr01qkcvkff840cmvvsmhr01qkcvkff84g`
    );

    res.json(apiResponse.data);
  } catch (error) {
    if (error.response) {
      res.status(error.response.status).json(error.response.data);
    } else {
      res
        .status(500)
        .json({ message: "Error setting up request to Finnhub API" });
    }
  }
});

app.post("/api/watchlist", async (req, res) => {
  const { tickerSymbol, name } = req.body;

  try {
    const newWatchlistItem = new Watchlist({
      tickerSymbol,
      name,
    });

    const savedItem = await newWatchlistItem.save();
    res.status(201).json(savedItem);
  } catch (error) {
    res.status(400).json({ message: "Error saving to watchlist", error });
  }
});

app.get("/api/watchlist/:ticker", async (req, res) => {
  const tickerSymbol = req.params.ticker;
  try {
    const watchlistItem = await Watchlist.findOne({ tickerSymbol });
    res.json(watchlistItem);
  } catch (error) {
    res.status(500).json({ message: "Error fetching watchlist items", error });
  }
});

app.delete("/api/watchlist/:id", async (req, res) => {
  const tickerSymbol = req.params.id;

  try {
    const deletedItem = await Watchlist.findByIdAndDelete(tickerSymbol);
    res.json(deletedItem);
  } catch (error) {
    res.status(500).json({ message: "Error deleting watchlist item", error });
  }
});

app.get("/api/watchlist", async (req, res) => {
  try {
    const watchlistItems = await Watchlist.find({});
    res.json(watchlistItems);
  } catch (error) {
    res.status(500).json({ message: "Error fetching watchlist items", error });
  }
});

app.get("/api/portfolio", async (req, res) => {
  try {
    const portfolioItems = await Portfolio.find({});
    res.json(portfolioItems);
  } catch (error) {
    res.status(500).json({ message: "Error fetching portfolio items", error });
  }
});

app.post("/api/portfolio/buy/:ticker", async (req, res) => {
  const { quantity, price, name } = req.body;
  const tickerSymbol = req.params.ticker;
  const totalCost = quantity * price;
  console.log(quantity);

  const wallet = await Wallet.findOne();
  if (wallet.balance < totalCost) {
    return res.status(400).json({ message: "Insufficient funds" });
  }

  wallet.balance -= totalCost;
  await wallet.save();

  let portfolioItem = await Portfolio.findOne({ tickerSymbol });
  if (portfolioItem) {
    portfolioItem.quantity += quantity;
    portfolioItem.totalCost += totalCost;
    portfolioItem.averageCostPerShare =
      portfolioItem.totalCost / portfolioItem.quantity;
  } else {
    portfolioItem = new Portfolio({
      tickerSymbol,
      name,
      quantity,
      currentPrice: price,
      totalCost,
      averageCostPerShare: totalCost / quantity,
    });
  }
  await portfolioItem.save();

  res.json(portfolioItem);
});

app.get("/api/wallet", async (req, res) => {
  try {
    const wallet = await Wallet.findOne();
    res.json(wallet);
  } catch (error) {
    res.status(500).json({ message: "Error fetching wallet balance", error });
  }
});

app.get("/api/portfolio/:ticker", async (req, res) => {
  const tickerSymbol = req.params.ticker;

  try {
    const portfolioItem = await Portfolio.findOne({ tickerSymbol });
    res.json(portfolioItem);
  } catch (error) {
    res.status(500).json({ message: "Error fetching portfolio item", error });
  }
});

app.post("/api/portfolio/sell/:ticker", async (req, res) => {
  const { quantity, price } = req.body;
  const tickerSymbol = req.params.ticker;
  const totalCost = quantity * price;

  const wallet = await Wallet.findOne();
  wallet.balance += totalCost;
  await wallet.save();

  let portfolioItem = await Portfolio.findOne({ tickerSymbol });
  if (portfolioItem.quantity === quantity) {
    await Portfolio.findByIdAndDelete(portfolioItem._id);
  } else {
    portfolioItem.quantity -= quantity;
    portfolioItem.totalCost -= totalCost;
    portfolioItem.averageCostPerShare =
      portfolioItem.totalCost / portfolioItem.quantity;
    await portfolioItem.save();
  }

  res.json({ message: "Stocks sold successfully" });
});

app.listen(PORT, () => {
  console.log(`Server is running on port ${PORT}`);
});
