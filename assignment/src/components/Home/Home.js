// pages/index.js

"use client";
import { useState, useEffect } from "react";
import Autocomplete from "@mui/material/Autocomplete";
import TextField from "@mui/material/TextField";
import CircularProgress from "@mui/material/CircularProgress";
import { useRouter } from "next/navigation";
import axios from "axios";
import "@/components/Home/Home.css";
import { Box } from "@mui/material";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faSearch, faXmark } from "@fortawesome/free-solid-svg-icons";

export default function Home({ stock }) {
  const [ticker, setTicker] = useState(stock || "");
  const [open, setOpen] = useState(false);
  const [options, setOptions] = useState([]);
  const [loading, setLoading] = useState(false);
  const [display, setDisplay] = useState(false);

  const router = useRouter();

  useEffect(() => {
    if (!loading) {
      return undefined;
    }
    (async () => {
      try {
        setOptions([]);
        setDisplay(false);

        if (ticker === "") return;
        else {
          const response = await axios.get(
            `http://localhost:8080/api/home/${ticker}`
          );
          const stocks = response.data;
          if (stocks.length === 0) {
            setDisplay(true);
          }

          setOptions(
            stocks.map((stock) => ({
              label: `${stock.symbol} | ${stock.description}`,
              ...stock,
            }))
          );
        }
      } catch (error) {
        console.error("Error fetching data: ", error);
      } finally {
        setLoading(false);
      }
    })();
  }, [ticker]);

  useEffect(() => {
    if (!open) {
      setOptions([]);
    }
  }, [open]);

  const handleSubmit = (e) => {
    e.preventDefault();
    const tick = ticker.substring(0, ticker.indexOf(" |"));
    console.log(ticker);
    router.push(`/search/${ticker}`);
  };

  const handleReset = () => {
    setTicker("");
    router.push("/");
  };

  return (
    <>
      <div className="container mt-5 pt-5 text-center">
        <h2 className="fw-normal">STOCK SEARCH</h2>
        <form
          onSubmit={handleSubmit}
          onReset={handleReset}
          className="stock_search_bar mt-4"
        >
          {/* <input
            type="text"
            className="search_bar"
            placeholder="Enter stock ticker symbol"
            value={ticker}
            onChange={(e) => {
              setTicker(e.target.value), setLoading(true);
            }}
          /> */}

          <Autocomplete
            freeSolo
            id="asynchronous-search"
            // style={{ width:  }}
            open={open}
            fullWidth={true}
            size="large"
            loadingText={<CircularProgress size={24} />}
            onClose={() => {
              setOpen(false);
            }}
            isOptionEqualToValue={(option, value) =>
              option.symbol === value.symbol
            }
            options={options}
            // value={stock}
            loading={loading}
            onInputChange={(event, newInputValue) => {
              setOpen(true);
              setTicker(newInputValue);
              setLoading(true);
            }}
            renderInput={(params) => (
              <Box ref={params.InputProps.ref} className="box_flex w-100 ">
                <input
                  type="text"
                  {...params.inputProps}
                  className="search_bar"
                  value={ticker}
                  placeholder="Enter stock ticker symbol"
                  required
                  onSubmit={handleSubmit}
                />
              </Box>
            )}
            renderOption={(props, option) => {
              return (
                <Box
                  component="li"
                  sx={{ "& > img": { mr: 2, flexShrink: 0 } }}
                  {...props}
                  onClick={() => {
                    router.push(`/search/${option.symbol}`);
                  }}
                >
                  {option.label}
                </Box>
              );
            }}
          />

          <div className="md:gap-2 d-flex justify-content-center align-items-center">
            <button
              className="search_button"
              type="submit"
              onSubmit={handleSubmit}
            >
              <FontAwesomeIcon icon={faSearch} className="search_icon" />
            </button>
            <button
              className="delete_button  "
              type="reset"
              onReset={handleReset}
            >
              <FontAwesomeIcon icon={faXmark} className="x_icon" />
            </button>
          </div>
        </form>
        {display && (
          <div
            className="alert alert-danger mt-5 p-2 w-75 mx-auto  rounded-1 text-small "
            id="error_result"
          >
            No data found. Please enter a valid Ticker.
          </div>
        )}
      </div>
    </>
  );
}
