"use client";
import { faXmark } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { CircularProgress } from "@mui/material";
import axios from "axios";
import React, { useState } from "react";
import { useEffect } from "react";
import "./watchlist.css";
import Link from "next/link";

export default function WatchList() {
  const [watchlist, setWatchlist] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchWatchlist = async () => {
      try {
        const response = await axios.get(
          "http://localhost:8080/api/watchlist/"
        );
        const watchlistData = response.data;
        setWatchlist(watchlistData);

        const fetchStockDetails = watchlistData.map((stock) =>
          axios.get(
            `http://localhost:8080/api/stockQuote/${stock.tickerSymbol}`
          )
        );
        const stockDetailsResponses = await Promise.all(fetchStockDetails);

        const updatedWatchlist = watchlistData.map((stock, index) => ({
          ...stock,
          details: stockDetailsResponses[index].data,
        }));

        setWatchlist(updatedWatchlist);
        setLoading(false);
      } catch (error) {
        console.error("Error fetching watchlist:", error);
        setLoading(false);
      }
    };

    fetchWatchlist();
  }, []);

  console.log(watchlist);

  const handleDelete = async (id) => {
    console.log(id);
    try {
      await axios.delete(`http://localhost:8080/api/watchlist/${id}`);
      const updatedWatchlist = watchlist.filter((stock) => stock._id !== id);
      setWatchlist(updatedWatchlist);
    } catch (error) {
      console.error("Error deleting stock from watchlist:", error);
    }
  };

  return (
    <div className=" my-5 m-auto d-flex justify-content-center align-items-center ">
      {loading ? (
        <CircularProgress />
      ) : (
        <div className="w-50 watchlist_cards py-5 m-auto ">
          <h2>My WatchList</h2>
          <div>
            {watchlist.map((stock) => (
              <div key={stock._id} className="card my-2">
                <FontAwesomeIcon
                  icon={faXmark}
                  className="x_mark "
                  onClick={() => handleDelete(stock._id)}
                />
                <Link
                  href={`/search/${stock.tickerSymbol}`}
                  key={stock._id}
                  className="text-decoration-none "
                >
                  <div className="card-body d-flex justify-content-between w-75 align-items-center watchlist_div">
                    <div className="text-black">
                      <h3 className="card-title">{stock.tickerSymbol}</h3>
                      <hh4 className="card-text">{stock.name}</hh4>
                    </div>
                    <div
                      className={`d-flex flex-column  ${
                        stock?.details?.d >= 0 ? `text-success` : `text-danger`
                      }`}
                    >
                      <h3>{stock?.details?.c}</h3>
                      <h4>
                        {stock?.details?.d <= 0 ? (
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
                        {stock?.details?.d} &nbsp;(
                        {stock?.details?.dp.toFixed(2)}
                        %)
                      </h4>
                    </div>
                  </div>
                </Link>
              </div>
            ))}
          </div>

          {watchlist.length === 0 && (
            <div class="alert alert-warning text-center mt-4" role="alert">
              Currently you don't have any stock in your watchlist.
            </div>
          )}
        </div>
      )}
    </div>
  );
}
