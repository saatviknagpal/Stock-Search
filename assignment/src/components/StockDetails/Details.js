"use client";
import React from "react";
import "./Details.css";
import {
  AppBar,
  Box,
  Button,
  Modal,
  Tab,
  Tabs,
  Typography,
} from "@mui/material";
import { PropTypes } from "prop-types";
import HighchartsReact from "highcharts-react-official";
import Highcharts from "highcharts";
import moment from "moment";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faFacebook, faXTwitter } from "@fortawesome/free-brands-svg-icons";
import Link from "next/link";
import { faStar, faXmark } from "@fortawesome/free-solid-svg-icons";
import Charts from "../Charts/Charts";
import RecommendationTrend from "../RecommendationTrend/RecommendationTrend";
import EPS from "../EPS/EPS";
import axios from "axios";
import { useState } from "react";

export default function Details({ data }) {
  const [alert, setAlert] = React.useState(false);
  const [quantity, setQuantity] = React.useState(0);
  const [sellQuantity, setSellQuantity] = React.useState(0);
  const [bought, setBought] = React.useState(false);
  const [sell, setSell] = React.useState(false);
  const date = new Date(data?.t * 1000);
  const currentTimestamp = new Date().getTime();

  const [portfolio, setPortfolio] = useState(data?.portfolio);
  const [walletBalance, setWalletBalance] = useState(data?.wallet?.balance);

  const calculateTotalMSPR = () => {
    return data?.sentiments?.data
      .reduce((total, item) => total + item.mspr, 0)
      .toFixed(2);
  };

  const calculatePositiveMSPR = () => {
    return data?.sentiments?.data
      ?.filter((item) => item.mspr > 0)
      .reduce((total, item) => total + item.mspr, 0)
      .toFixed(2);
  };

  const calculateNegativeMSPR = () => {
    return data?.sentiments?.data
      ?.filter((item) => item.mspr < 0)
      .reduce((total, item) => total + item.mspr, 0)
      .toFixed(2);
  };

  const calculateTotalChange = () => {
    return data?.sentiments?.data.reduce(
      (total, item) => total + item.change,
      0
    );
  };

  const calculatePositiveChange = () => {
    return data?.sentiments?.data
      ?.filter((item) => item.change > 0)
      .reduce((total, item) => total + item.change, 0);
  };

  const calculateNegativeChange = () => {
    return data?.sentiments?.data
      ?.filter((item) => item.change < 0)
      .reduce((total, item) => total + item.change, 0);
  };

  const [isInWatchList, setIsInWatchList] = React.useState(
    data?.watchList != null
  );

  const difference = currentTimestamp - data?.t * 1000;

  const [selectedNews, setSelectedNews] = React.useState(null);
  const year = date.getFullYear();
  const month = (date.getMonth() + 1).toString().padStart(2, "0");
  const day = date.getDate().toString().padStart(2, "0");
  const hours = date.getHours().toString().padStart(2, "0");
  const minutes = date.getMinutes().toString().padStart(2, "0");
  const seconds = date.getSeconds().toString().padStart(2, "0");

  const [value, setValue] = React.useState(0);
  console.log(data);
  const stockPriceData = data?.hourlyData?.results?.map((e) => {
    return [e.t, e.c];
  });

  const options = {
    chart: {
      type: "line",
      backgroundColor: "#F5F5F5",
      zoomType: "x",
      // width: 350,
    },
    title: {
      text: data?.ticker + " Hourly Price Variation",
    },
    xAxis: {
      type: "datetime",
      dateTimeLabelFormats: {
        hour: "%H:%M",
        day: "%e. %b",
      },
      labels: {
        format: "{value:%H:%M}",
      },
    },

    yAxis: [
      {
        title: {
          text: "",
        },
        opposite: true,
        tickAmount: 5,
      },
    ],
    responsive: {
      rules: [
        {
          condition: {
            maxWidth: 600,
          },
        },
      ],
    },

    legend: {
      enabled: false,
    },
    series: [
      {
        name: "",
        data: stockPriceData,
        lineWidth: 2,
        color: `${data?.d >= 0 ? `#198754` : `#FF0000`}`,
        marker: {
          enabled: false,
        },
      },
    ],
    navigation: {
      menuItemStyle: {
        fontSize: "10px",
      },
    },
  };

  const handleChange = (event, newValue) => {
    setValue(newValue);
  };

  React.useEffect(() => {
    let timeoutId;
    if (alert || bought || sell) {
      timeoutId = setTimeout(() => {
        setAlert(false);
        setBought(false);
        setSell(false);
      }, 5000);
    }

    return () => clearTimeout(timeoutId);
  }, [alert, bought, sell]);

  function CustomTabPanel(props) {
    const { children, value, index, ...other } = props;

    return (
      <div
        role="tabpanel"
        hidden={value !== index}
        id={`simple-tabpanel-${index}`}
        aria-labelledby={`simple-tab-${index}`}
        {...other}
      >
        {value === index && (
          <Box sx={{ p: 3 }}>
            <Typography>{children}</Typography>
          </Box>
        )}
      </div>
    );
  }

  CustomTabPanel.propTypes = {
    children: PropTypes.node,
    index: PropTypes.number.isRequired,
    value: PropTypes.number.isRequired,
  };

  function a11yProps(index) {
    return {
      id: `full-width-tab-${index}`,
      "aria-controls": `full-width-tabpanel-${index}`,
    };
  }

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

  const [open, setOpen] = React.useState(false);
  const [buyOpen, setBuyOpen] = React.useState(false);
  const [sellOpen, setSellOpen] = React.useState(false);

  const handleOpen = () => setOpen(true);
  const handleClose = () => setOpen(false);

  const handleCloseBuy = () => setBuyOpen(false);
  const handleBuyOpen = () => setBuyOpen(true);

  const handleCloseSell = () => setSellOpen(false);
  const handleSellOpen = () => setSellOpen(true);

  const handleDelete = async (id) => {
    setIsInWatchList(false);
    try {
      await axios.delete(`http://localhost:8080/api/watchList/${id}`);
    } catch (error) {
      console.error("Error deleting from watchlist:", error);
      setIsInWatchList(true);
    }
  };

  const handleSubmit = async () => {
    setIsInWatchList(true);
    setAlert(true);
    try {
      await axios.post(`http://localhost:8080/api/watchList`, {
        tickerSymbol: data?.ticker,
        name: data?.name,
      });
    } catch (error) {
      console.error("Error adding to watchlist:", error);
      setIsInWatchList(false);
    }
  };

  const handleBuySubmit = async () => {
    try {
      const response = await axios.post(
        `http://localhost:8080/api/portfolio/buy/${data?.ticker}`,
        {
          quantity: quantity,
          price: data?.c,
          name: data?.name,
        }
      );

      const fetchWallet = await axios.get(`http://localhost:8080/api/wallet`);
      setPortfolio(response.data);
      setWalletBalance(fetchWallet.data.balance);
      setBought(true);
      setQuantity(0);
      handleCloseBuy();
    } catch (error) {
      console.error("Error buying stock:", error);
    }
  };

  const handleSellSubmit = async () => {
    try {
      const response = await axios.post(
        `http://localhost:8080/api/portfolio/sell/${data?.ticker}`,
        {
          quantity: sellQuantity,
          price: data?.c,
          name: data?.name,
        }
      );
      const fetchWallet = await axios.get(`http://localhost:8080/api/wallet`);
      setPortfolio(response.data);
      setWalletBalance(fetchWallet.data.balance);
      setSell(true);
      setSellQuantity(0);
      handleCloseSell();
    } catch (error) {
      console.error("Error selling stock:", error);
    }
  };

  return (
    <div id="details_tab">
      {alert && (
        <div
          class="alert alert-success alert-dismissible fade show w-75 m-auto mt-5 "
          role="alert"
        >
          <p className="text-center">{data?.ticker} added to WatchList.</p>
          <button
            type="button"
            class="btn-close"
            data-bs-dismiss="alert"
            aria-label="Close"
          ></button>
        </div>
      )}

      {bought ? (
        <div
          class="alert alert-success text-center alert-dismissible fade show w-75 m-auto mt-5"
          role="alert"
        >
          {data?.ticker} bought successfully.
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
          class="alert alert-danger text-center alert-dismissible fade show w-75 m-auto mt-5"
          role="alert"
        >
          {data?.ticker} sold successfully.
          <button
            type="button"
            class="btn-close"
            onClick={() => {
              setSell(false);
            }}
          ></button>
        </div>
      ) : null}

      <div className="d-flex sm:justify-content-between justify-content-evenly p-2 mt-4 md:mt-0 md:p-5 md:w-75 m-auto ">
        <div className="d-flex flex-column first_div justify-content-center align-items-center">
          <div className="ticker">
            {data?.ticker}{" "}
            {isInWatchList ? (
              <FontAwesomeIcon
                className="star_fav"
                icon={faStar}
                onClick={() => handleDelete(data?.watchList?._id)}
                style={{ color: "#FFD43B" }}
              />
            ) : (
              <svg
                xmlns="http://www.w3.org/2000/svg"
                width="24"
                height="24"
                fill="currentColor"
                className="bi bi-star mb-2 star_fav"
                viewBox="0 0 16 16"
                onClick={handleSubmit}
              >
                <path d="M2.866 14.85c-.078.444.36.791.746.593l4.39-2.256 4.389 2.256c.386.198.824-.149.746-.592l-.83-4.73 3.522-3.356c.33-.314.16-.888-.282-.95l-4.898-.696L8.465.792a.513.513 0 0 0-.927 0L5.354 5.12l-4.898.696c-.441.062-.612.636-.283.95l3.523 3.356-.83 4.73zm4.905-2.767-3.686 1.894.694-3.957a.56.56 0 0 0-.163-.505L1.71 6.745l4.052-.576a.53.53 0 0 0 .393-.288L8 2.223l1.847 3.658a.53.53 0 0 0 .393.288l4.052.575-2.906 2.77a.56.56 0 0 0-.163.506l.694 3.957-3.686-1.894a.5.5 0 0 0-.461 0z" />
              </svg>
            )}
          </div>
          <div className="name_company">{data?.name}</div>
          <div className="md:m-2 exchange">{data?.exchange}</div>
          <Modal
            open={buyOpen}
            onClose={handleCloseBuy}
            aria-labelledby="modal-modal-title"
            aria-describedby="modal-modal-description"
          >
            <Box sx={style} className="modal_box">
              <div className="d-flex justify-content-between ">
                <Typography id="modal-modal-title" variant="h6" component="h2">
                  {data?.ticker}
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

              <p className="fw-bold title_modal">{selectedNews?.headline}</p>
              <p>Current Price: {data?.c}</p>
              <p>
                Money in Wallet: <span>${walletBalance?.toFixed(2)}</span>
              </p>
              <div className="input-group mb-3 d-flex justify-content-center align-items-center gap-3">
                <span>Quantity: </span>
                <input
                  type="number"
                  class="form-control"
                  placeholder="quantity"
                  aria-label="quantity"
                  onChange={(e) => setQuantity(Number(e.target.value))}
                  defaultValue={0}
                  aria-describedby="basic-addon1"
                />
              </div>
              {quantity * data?.c > walletBalance ? (
                <p className="text-danger fw-bold">
                  Not enough money in wallet!!
                </p>
              ) : null}
              <hr />
              <div className="d-flex justify-content-between align-items-center">
                <p>Total: {(quantity * data?.c).toFixed(2)}</p>
                {quantity * data?.c > walletBalance ? (
                  <button type="button" className="btn btn-success" disabled>
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
          <div className="d-flex trade_btn mt-2 gap-2 md:gap-3">
            <button
              type="button"
              className="btn btn-success"
              onClick={handleBuyOpen}
            >
              Buy
            </button>
            {portfolio && portfolio.quantity > 0 && (
              <button
                type="button"
                className="btn btn-danger"
                onClick={handleSellOpen}
              >
                Sell
              </button>
            )}

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
                    {data?.ticker}
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

                <p>Current Price: {data?.c}</p>
                <p>
                  Money in Wallet: <span>${walletBalance?.toFixed(2)}</span>
                </p>
                <div className="input-group mb-3 d-flex justify-content-center align-items-center gap-3">
                  <span>Quantity: </span>
                  <input
                    type="number"
                    class="form-control"
                    placeholder="quantity"
                    aria-label="quantity"
                    onChange={(e) => setSellQuantity(Number(e.target.value))}
                    defaultValue={0}
                    aria-describedby="basic-addon1"
                  />
                </div>
                {sellQuantity > portfolio?.quantity ? (
                  <p className="text-danger fw-bold">
                    You cannot sell the stocks that you don't have!
                  </p>
                ) : null}
                <hr />
                <div className="d-flex justify-content-between align-items-center">
                  <p>Total: {(sellQuantity * data?.c).toFixed(2)}</p>
                  {sellQuantity > portfolio?.quantity ? (
                    <button type="button" className="btn btn-success" disabled>
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
        <div className="text-center">
          <img src={data?.logo} alt="Company Logo" className="logo_img" />
        </div>
        <div
          className={`d-flex flex-column text-center ${
            data?.d >= 0 ? `text-success` : `closed_market`
          }`}
        >
          <h2>{data?.c?.toFixed(2)}</h2>
          <h3>
            {data?.d <= 0 ? (
              <>
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  width="24"
                  height="24"
                  fill="currentColor"
                  class="bi bi-caret-down-fill"
                  viewBox="0 0 16 16"
                >
                  <path d="M7.247 11.14 2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z" />
                </svg>
                &nbsp;
              </>
            ) : (
              <>
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  width="24"
                  height="24"
                  fill="currentColor"
                  class="bi bi-caret-up-fill"
                  viewBox="0 0 16 16"
                >
                  <path d="m7.247 4.86-4.796 5.481c-.566.647-.106 1.659.753 1.659h9.592a1 1 0 0 0 .753-1.659l-4.796-5.48a1 1 0 0 0-1.506 0z" />
                </svg>
                &nbsp;
              </>
            )}
            {data?.d} &nbsp;({data?.dp?.toFixed(2)}%)
          </h3>

          <p className="text-black current_date">
            {moment(new Date()).format("YYYY-MM-DD HH:mm:ss")}
          </p>
        </div>
      </div>
      {difference <= 300000 ? (
        <p className="md:mt-5 text-success fw-bold market_status">
          Market is Open
        </p>
      ) : (
        <p className="md:mt-5 text-danger fw-bold market_status text-center">
          Market Closed on{" "}
          {year +
            "-" +
            month +
            "-" +
            day +
            " " +
            hours +
            ":" +
            minutes +
            ":" +
            seconds}
        </p>
      )}
      <div>
        <Box className="box_tab" sx={{ width: "100%" }}>
          <Box>
            <Tabs
              value={value}
              onChange={handleChange}
              variant="scrollable"
              scrollButtons
              allowScrollButtonsMobile
              aria-label="scrollable force tabs example"
            >
              <Tab
                className="tab_bar text-capitalize"
                label="Summary"
                {...a11yProps(0)}
              />
              <Tab
                wrapped={false}
                className="tab_bar text-nowrap text-capitalize "
                label="Top News"
                {...a11yProps(1)}
              />
              <Tab
                className="tab_bar text-capitalize"
                label="Charts"
                {...a11yProps(2)}
              />
              <Tab
                className="tab_bar text-capitalize "
                label="Insights"
                {...a11yProps(2)}
              />
            </Tabs>
          </Box>
          <CustomTabPanel value={value} index={0}>
            <div className="summary_tab">
              <div>
                <div className="summary_tab_price">
                  <div className="d-flex ">
                    <p className="fw-bold">High Price: &nbsp;</p>
                    <p>{data?.h}</p>
                  </div>
                  <div className="d-flex">
                    <p className="fw-bold">Low Price: &nbsp;</p>
                    <p>{data?.l}</p>
                  </div>
                  <div className="d-flex">
                    <p className="fw-bold">Open Price &nbsp;</p>
                    <p>{data?.o}</p>
                  </div>
                  <div className="d-flex">
                    <p className="fw-bold">Prev. Close &nbsp;</p>
                    <p>{data?.pc}</p>
                  </div>
                </div>
                <div className="about_company">
                  <h4 className="company_header">About the company</h4>
                  <div className="d-flex">
                    <p className="fw-bold">IPO Start Date: &nbsp;</p>
                    <p>{data?.ipo}</p>
                  </div>
                  <div className="d-flex">
                    <p className="fw-bold">Industry: &nbsp;</p>
                    <p>{data?.finnhubIndustry}</p>
                  </div>
                  <div className="d-flex">
                    <p className="fw-bold">Webpage: &nbsp;</p>
                    <a target="_blank" href={data?.weburl}>
                      {data?.weburl}
                    </a>
                  </div>
                  <div className="d-flex flex-column ">
                    <p className="fw-bold">Company Peers: &nbsp;</p>
                    <p>
                      {data?.peers
                        ?.filter((e) => !e.includes("."))
                        ?.map((e) => {
                          return (
                            <a href={e}>
                              {e}, {""}
                            </a>
                          );
                        })}
                    </p>
                  </div>
                </div>
              </div>
              <div>
                <div className="chart_1">
                  <HighchartsReact
                    highcharts={Highcharts}
                    options={options}
                    className="chart_1"
                  />
                </div>
              </div>
            </div>
          </CustomTabPanel>
          <CustomTabPanel value={value} index={1}>
            <div className="news_tab mb-5">
              {data?.news
                ?.filter((item) => item.image != "")
                ?.slice(0, 20)
                ?.map((e) => {
                  return (
                    <>
                      <div
                        className="news_card"
                        onClick={() => {
                          setSelectedNews(e);
                          handleOpen();
                        }}
                      >
                        <div className="d-lg-flex">
                          <img
                            src={e.image}
                            alt="News Image"
                            className="news_img rounded-2"
                          />
                          <div className="news_content">
                            <p className="news_title text-center px-4 pt-4">
                              {e.headline}
                            </p>
                          </div>
                        </div>
                      </div>
                    </>
                  );
                })}
            </div>
            <Modal
              open={open}
              onClose={handleClose}
              aria-labelledby="modal-modal-title"
              aria-describedby="modal-modal-description"
            >
              <Box sx={style} className="modal_box">
                <Typography id="modal-modal-title" variant="h6" component="h2">
                  {selectedNews?.source}
                </Typography>
                <div className="d-flex justify-content-between ">
                  <p>
                    {moment(selectedNews?.timestamp).format("MMMM D, YYYY")}
                  </p>
                  <FontAwesomeIcon
                    icon={faXmark}
                    onClick={handleClose}
                    style={{ color: "#74C0FC", cursor: "pointer" }}
                  />
                </div>
                <hr />

                <p className="fw-bold title_modal">{selectedNews?.headline}</p>
                <p>{selectedNews?.summary.split(".")[0]}.</p>
                <p>
                  For more details{" "}
                  <a target="_blank" href={selectedNews?.url}>
                    {" "}
                    here
                  </a>
                </p>

                <div className="mt-5 social_box">
                  <p className="share_button ">Share</p>
                  <div className="d-flex mt-2">
                    <Link
                      href={`https://twitter.com/intent/tweet?text=${selectedNews?.headline}&url=${selectedNews?.url}`}
                      target="_blank"
                    >
                      <FontAwesomeIcon
                        icon={faXTwitter}
                        className="social_logo text-black"
                      />
                    </Link>
                    <Link
                      href={`https://www.facebook.com/sharer/sharer.php?u=${selectedNews?.url}`}
                      data-href={selectedNews?.url}
                      target="_blank"
                    >
                      <FontAwesomeIcon
                        icon={faFacebook}
                        style={{ color: "#0000FF" }}
                        className="social_logo"
                      />
                    </Link>
                  </div>
                </div>
              </Box>
            </Modal>
          </CustomTabPanel>
          <CustomTabPanel value={value} index={2}>
            <Charts data={data} />
          </CustomTabPanel>
          <CustomTabPanel value={value} index={3}>
            <h3 className="text-center">Insider Sentiments</h3>
            <table class="sentiments-table mb-5 d-flex justify-content-center align-items-center ">
              <thead>
                <tr>
                  <th scope=" text-center">{data?.name}</th>
                  <th scope="col">MSPR</th>
                  <th scope="col">Change</th>
                </tr>
                <tr>
                  <td className="fw-bold text-center">Total</td>
                  <td>{calculateTotalMSPR()}</td>
                  <td>{calculateTotalChange()}</td>
                </tr>
                <tr>
                  <td className="fw-bold text-center">Positive</td>
                  <td>{calculatePositiveMSPR()}</td>
                  <td>{calculatePositiveChange()}</td>
                </tr>
                <tr>
                  <td className="fw-bold text-center">Negative</td>
                  <td>{calculateNegativeMSPR()}</td>
                  <td>{calculateNegativeChange()}</td>
                </tr>
              </thead>
            </table>
            <div className="d-flex justify-content-center gap-3 flex-column flex-lg-row">
              <RecommendationTrend trendData={data.trends} />
              <EPS eps={data.earnings} />
            </div>
          </CustomTabPanel>
        </Box>
      </div>
    </div>
  );
}
