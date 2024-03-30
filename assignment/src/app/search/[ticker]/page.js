"use client";
import Home from "@/components/Home/Home";
import Details from "@/components/StockDetails/Details";
import { CircularProgress } from "@mui/material";
import axios from "axios";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";

const SearchPage = ({ params }) => {
  const router = useRouter();
  console.log(params.ticker);
  if (params.ticker === "home") router.push("/");
  const [stockDetails, setStockDetails] = useState({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  const isMarketOpen = () => {
    const now = new Date();
    now.setHours(now.getHours() + now.getTimezoneOffset() / 60 - 8);

    const marketOpen = new Date(now);
    marketOpen.setHours(6, 30, 0);

    const marketClose = new Date(now);
    marketClose.setHours(13, 0, 0);

    return (
      now >= marketOpen &&
      now < marketClose &&
      now.getDay() >= 1 &&
      now.getDay() <= 5
    );
  };

  useEffect(() => {
    const fetchStockDetails = () =>
      axios.get(`http://localhost:8080/api/stockDetails/${params.ticker}`);

    const fetchStockQuotes = () =>
      axios.get(`http://localhost:8080/api/stockQuote/${params.ticker}`);

    const fetchCompanyPeers = () =>
      axios.get(`http://localhost:8080/api/companyPeers/${params.ticker}`);

    const fetchHourlyData = () =>
      axios.get(`http://localhost:8080/api/hourlyData/${params.ticker}`);

    const fetchNewsData = () =>
      axios.get(`http://localhost:8080/api/stockNews/${params.ticker}`);

    const fetchChartsData = () =>
      axios.get(`http://localhost:8080/api/charts/${params.ticker}`);

    const fetchInsiderSentiments = () =>
      axios.get(`http://localhost:8080/api/insiderSentiment/${params.ticker}`);

    const fetchTrends = () =>
      axios.get(`http://localhost:8080/api/recommendation/${params.ticker}`);

    const fetchEarnings = () =>
      axios.get(`http://localhost:8080/api/earnings/${params.ticker}`);

    const fetchWatchList = () =>
      axios.get(`http://localhost:8080/api/watchlist/${params.ticker}`);

    const fetchWallet = () => axios.get(`http://localhost:8080/api/wallet`);

    const fetchPortfolio = () =>
      axios.get(`http://localhost:8080/api/portfolio/${params.ticker}`);

    Promise.all([
      fetchStockDetails(),
      fetchStockQuotes(),
      fetchCompanyPeers(),
      fetchHourlyData(),
      fetchNewsData(),
      fetchChartsData(),
      fetchInsiderSentiments(),
      fetchTrends(),
      fetchEarnings(),
      fetchWatchList(),
      fetchWallet(),
      fetchPortfolio(),
    ])
      .then((responses) => {
        const detailsResponse = responses[0];
        const quotesResponse = responses[1];
        const peersResponse = responses[2];
        const hourlyDataResponse = responses[3];
        const newsDataResponse = responses[4];
        const chartsDataResponse = responses[5];
        const sentimentsResponse = responses[6];
        const trendsResponse = responses[7];
        const earningsResponse = responses[8];
        const watchListResponse = responses[9];
        const walletResponse = responses[10];
        const portfolioResponse = responses[11];

        if (
          detailsResponse.status === 200 &&
          quotesResponse.status === 200 &&
          peersResponse.status === 200 &&
          hourlyDataResponse.status === 200 &&
          newsDataResponse.status === 200 &&
          newsDataResponse.data.length > 0 &&
          chartsDataResponse.status === 200 &&
          sentimentsResponse.status === 200 &&
          trendsResponse.status === 200 &&
          earningsResponse.status === 200 &&
          watchListResponse.status === 200 &&
          walletResponse.status === 200 &&
          portfolioResponse.status === 200
        ) {
          setStockDetails((prev) => ({
            ...prev,
            ...detailsResponse.data,
            ...quotesResponse.data,
            peers: peersResponse.data,
            hourlyData: hourlyDataResponse.data,
            news: newsDataResponse.data,
            charts: chartsDataResponse.data,
            sentiments: sentimentsResponse.data,
            trends: trendsResponse.data,
            earnings: earningsResponse.data,
            watchList: watchListResponse.data,
            wallet: walletResponse.data,
            portfolio: portfolioResponse.data,
          }));
        } else setError(true);

        setLoading(false);
      })
      .catch((error) => {
        console.error("Error fetching data: ", error);
        setLoading(false);
      });

    if (isMarketOpen()) {
      const interval = setInterval(() => {
        fetchStockQuotes()
          .then((response) => {
            if (response.status === 200) {
              if (isMarketOpen()) {
                setStockDetails((prev) => ({ ...prev, ...response.data }));
              } else {
                clearInterval(interval);
              }
            }
          })
          .catch((error) => {
            console.error("Error fetching stock quotes: ", error);
          });
      }, 15000);

      return () => clearInterval(interval);
    }
  }, [params.ticker]);

  return (
    <>
      <Home stock={params.ticker} />
      {loading ? (
        <div className="d-flex justify-content-center align-items-center mt-5">
          <CircularProgress />
        </div>
      ) : error ? (
        <div className="alert alert-danger m-auto mt-5 w-50 text-center">
          No data found. Please enter a valid ticker.
        </div>
      ) : (
        <Details data={stockDetails} />
      )}
    </>
  );
};

export default SearchPage;
