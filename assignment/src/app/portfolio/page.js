"use client";
import { faXmark } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { Box, CircularProgress, Modal, Typography } from "@mui/material";
import axios from "axios";
import React from "react";
import { useState } from "react";
import { useEffect } from "react";
import "./portfolio.css";

export default function Page() {
  const [portfolio, setPortfolio] = useState([]);
  const [wallet, setWallet] = useState(0);
  const [loading, setLoading] = useState(true);
  const [quantity, setQuantity] = React.useState(0);
  const [sellQuantity, setSellQuantity] = React.useState(0);
  const [selectedStock, setSelectedStock] = useState(null);
  const [bought, setBought] = useState(false);
  const [sell, setSell] = useState(false);

  useEffect(() => {
    const fetchPortfolio = async () => {
      setLoading(true);
      try {
        const portfolioResponse = await axios.get(
          "http://localhost:8080/api/portfolio/"
        );
        const portfolioData = portfolioResponse.data;

        const stockDetailsPromises = portfolioData.map((stock) =>
          axios.get(
            `http://localhost:8080/api/stockQuote/${stock.tickerSymbol}`
          )
        );
        const stockDetailsResponses = await Promise.all(stockDetailsPromises);

        const updatedPortfolio = portfolioData.map((stock, index) => ({
          ...stock,
          currentPrice: stockDetailsResponses[index].data.c,
        }));

        setPortfolio(updatedPortfolio);
      } catch (error) {
        console.error("Error fetching portfolio and stock quotes:", error);
      }

      try {
        const walletResponse = await axios.get(
          "http://localhost:8080/api/wallet"
        );
        setWallet(walletResponse.data);
      } catch (error) {
        console.error("Error fetching wallet:", error);
      }

      setLoading(false);
    };

    const fetchWallet = async () => {
      try {
        const response = await axios.get("http://localhost:8080/api/wallet");
        const walletData = response.data;
        setWallet(walletData);
        setLoading(false);
      } catch (error) {
        console.error("Error fetching wallet:", error);
        setLoading(false);
      }
    };

    fetchPortfolio();
    fetchWallet();

    let timer;
    if (bought || sell) {
      timer = setTimeout(() => {
        setBought(false);
        setSell(false);
      }, 5000);
    }
    return () => clearTimeout(timer);
  }, [bought, sell]);

  const [buyOpen, setBuyOpen] = React.useState(false);
  const [sellOpen, setSellOpen] = React.useState(false);

  const handleCloseBuy = () => {
    setBuyOpen(false);
  };
  const handleBuyOpen = (stock) => {
    setSell(false);
    setBought(false);
    setSelectedStock(stock);
    setBuyOpen(true);
  };

  const handleSellOpen = (stock) => {
    setBought(false);
    setSell(false);
    setSelectedStock(stock);
    setSellOpen(true);
  };

  const handleCloseSell = () => {
    setSellOpen(false);
  };

  const handleBuySubmit = async () => {
    try {
      await axios.post(
        `http://localhost:8080/api/portfolio/buy/${selectedStock?.tickerSymbol}`,
        {
          quantity: quantity,
          price: selectedStock?.currentPrice,
          name: selectedStock?.name,
        }
      );

      setBought(true);
      setQuantity(0);
      handleCloseBuy();
    } catch (error) {
      console.error("Error buying stock:", error);
    }
  };

  const handleSellSubmit = async () => {
    try {
      await axios.post(
        `http://localhost:8080/api/portfolio/sell/${selectedStock?.tickerSymbol}`,
        {
          quantity: sellQuantity,
          price: selectedStock?.currentPrice,
          name: selectedStock?.name,
        }
      );

      setSell(true);
      setSellQuantity(0);
      handleCloseSell();
    } catch (error) {
      console.error("Error selling stock:", error);
    }
  };

  const style = {
    position: "absolute",
    top: "40%",
    left: "50%",
    transform: "translate(-50%, -50%)",
    width: 350,
    bgcolor: "background.paper",
    boxShadow: 24,
    p: 4,
  };

  return (
    <div className="w-50 portfolio_start py-5 m-auto my-5">
      {loading ? (
        <CircularProgress />
      ) : (
        <>
          {bought ? (
            <div
              class="alert alert-success text-center alert-dismissible fade show"
              role="alert"
            >
              {selectedStock?.tickerSymbol} bought successfully.
              <button
                type="button"
                class="btn-close"
                onClick={() => {
                  setBought(false);
                }}
              ></button>
            </div>
          ) : sell ? (
            <div
              class="alert alert-danger text-center alert-dismissible fade show"
              role="alert"
            >
              {selectedStock?.tickerSymbol} sold successfully.
              <button
                type="button"
                class="btn-close"
                onClick={() => {
                  setSell(false);
                }}
              ></button>
            </div>
          ) : null}

          <h3>My Portfolio</h3>
          <h6>Money in Wallet: ${wallet?.balance.toFixed(2)}</h6>
          <div className="d-flex flex-column gap-4 portfolio_card">
            {portfolio?.map((stock) => {
              return (
                <div class="card">
                  <div class="card-header py-1 align-items-center  d-flex gap-2">
                    <h4>{stock?.tickerSymbol}</h4> <span>{stock?.name}</span>
                  </div>
                  <div class="card-body container gap-5 row ">
                    <div className="d-flex justify-content-between align-items-center gap-5 col">
                      <div className="d-flex flex-column ">
                        <strong>Quantity:</strong>
                        <strong>Avg. Cost/Share:</strong>
                        <strong>Total Cost:</strong>
                      </div>
                      <div className="d-flex flex-column ">
                        <span>{stock?.quantity.toFixed(2)}</span>
                        <span>{stock?.averageCostPerShare.toFixed(2)}</span>
                        <span>{stock?.totalCost.toFixed(2)}</span>
                      </div>
                    </div>
                    <div className="d-flex justify-content-between align-items-center gap-5 col">
                      <div className="d-flex flex-column ">
                        <strong>Change:</strong>
                        <strong>Current Price:</strong>
                        <strong>Market Value:</strong>
                      </div>
                      <div className="d-flex flex-column ">
                        <span>
                          {(
                            stock?.averageCostPerShare.toFixed(2) -
                            stock?.currentPrice
                          ).toFixed(2)}
                        </span>
                        <span>{stock?.currentPrice}</span>
                        <span>
                          {(stock?.currentPrice * stock?.quantity).toFixed(2)}
                        </span>
                      </div>
                    </div>
                  </div>
                  <div class="card-footer text-muted gap-2 d-flex">
                    <button
                      type="button"
                      className="btn btn-primary"
                      onClick={() => handleBuyOpen(stock)}
                    >
                      Buy
                    </button>
                    <button
                      type="button"
                      className="btn btn-danger"
                      onClick={() => handleSellOpen(stock)}
                    >
                      Sell
                    </button>

                    <Modal
                      open={buyOpen}
                      onClose={handleCloseBuy}
                      aria-labelledby="modal-modal-title"
                      aria-describedby="modal-modal-description"
                    >
                      <Box sx={style} className="modal_box">
                        <div className="d-flex justify-content-between ">
                          <Typography
                            id="modal-modal-title"
                            variant="h6"
                            component="h2"
                          >
                            {selectedStock?.tickerSymbol}
                          </Typography>
                          <FontAwesomeIcon
                            icon={faXmark}
                            onClick={() => {
                              setQuantity(0);
                              handleCloseBuy();
                            }}
                            style={{ color: "#74C0FC", cursor: "pointer" }}
                          />
                        </div>
                        <hr />

                        <p>Current Price: {selectedStock?.currentPrice}</p>
                        <p>
                          Money in Wallet:{" "}
                          <span>${wallet?.balance?.toFixed(2)}</span>
                        </p>
                        <div className="input-group mb-3 d-flex justify-content-center align-items-center gap-3">
                          <span>Quantity: </span>
                          <input
                            type="number"
                            class="form-control"
                            placeholder="quantity"
                            aria-label="quantity"
                            onChange={(e) =>
                              setQuantity(Number(e.target.value))
                            }
                            defaultValue={0}
                            aria-describedby="basic-addon1"
                          />
                        </div>
                        {quantity * selectedStock?.currentPrice >
                        wallet?.balance ? (
                          <p className="text-danger fw-bold">
                            Not enough money in wallet!!
                          </p>
                        ) : null}
                        <hr />
                        <div className="d-flex justify-content-between align-items-center">
                          <p>
                            Total:{" "}
                            {(quantity * selectedStock?.currentPrice).toFixed(
                              2
                            )}
                          </p>
                          {quantity * selectedStock?.currentPrice >
                          wallet?.balance ? (
                            <button
                              type="button"
                              className="btn btn-success"
                              disabled
                            >
                              Buy
                            </button>
                          ) : (
                            <button
                              type="button"
                              className="btn btn-success"
                              onClick={handleBuySubmit}
                            >
                              Buy
                            </button>
                          )}
                        </div>
                      </Box>
                    </Modal>
                    <div className="d-flex gap-3">
                      <Modal
                        open={sellOpen}
                        onClose={handleCloseSell}
                        aria-labelledby="modal-modal-title"
                        aria-describedby="modal-modal-description"
                      >
                        <Box sx={style} className="modal_box">
                          <div className="d-flex justify-content-between ">
                            <Typography
                              id="modal-modal-title"
                              variant="h6"
                              component="h2"
                            >
                              {selectedStock?.tickerSymbol}
                            </Typography>
                            <FontAwesomeIcon
                              icon={faXmark}
                              onClick={() => {
                                setSellQuantity(0);
                                handleCloseSell();
                              }}
                              style={{ color: "#74C0FC", cursor: "pointer" }}
                            />
                          </div>
                          <hr />

                          <p>Current Price: {selectedStock?.currentPrice}</p>
                          <p>
                            Money in Wallet:{" "}
                            <span>${wallet?.balance?.toFixed(2)}</span>
                          </p>
                          <div className="input-group mb-3 d-flex justify-content-center align-items-center gap-3">
                            <span>Quantity: </span>
                            <input
                              type="number"
                              class="form-control"
                              placeholder="quantity"
                              aria-label="quantity"
                              onChange={(e) =>
                                setSellQuantity(Number(e.target.value))
                              }
                              defaultValue={0}
                              aria-describedby="basic-addon1"
                            />
                          </div>
                          {sellQuantity > selectedStock?.quantity ? (
                            <p className="text-danger fw-bold">
                              You cannot sell the stocks that you don't have!
                            </p>
                          ) : null}
                          <hr />
                          <div className="d-flex justify-content-between align-items-center">
                            <p>
                              Total:{" "}
                              {(
                                sellQuantity * selectedStock?.currentPrice
                              ).toFixed(2)}
                            </p>
                            {sellQuantity > selectedStock?.quantity ? (
                              <button
                                type="button"
                                className="btn btn-success"
                                disabled
                              >
                                Sell
                              </button>
                            ) : (
                              <button
                                type="button"
                                className="btn btn-success"
                                onClick={handleSellSubmit}
                              >
                                Sell
                              </button>
                            )}
                          </div>
                        </Box>
                      </Modal>
                    </div>
                  </div>
                </div>
              );
            })}
            {portfolio.length === 0 && (
              <div class="alert alert-warning text-center mt-4" role="alert">
                Currently you don't have any stock.
              </div>
            )}
          </div>
        </>
      )}
    </div>
  );
}
