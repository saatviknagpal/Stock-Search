// models/Wallet.js
const mongoose = require("mongoose");
const Schema = mongoose.Schema;

const walletSchema = new Schema({
  balance: {
    type: Number,
    required: true,
  },
});

const Wallet = mongoose.model("Wallet", walletSchema);

module.exports = Wallet;
