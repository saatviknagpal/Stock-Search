const mongoose = require("mongoose");
const Schema = mongoose.Schema;

const watchlistSchema = new Schema(
  {
    tickerSymbol: {
      type: String,
      required: true,
      unique: true,
      trim: true,
      uppercase: true,
    },
    name: {
      type: String,
      required: true,
      unique: true,
      trim: true,
    },
  },
  {
    timestamps: true,
  }
);

const Watchlist = mongoose.model("Watchlist", watchlistSchema);

module.exports = Watchlist;
