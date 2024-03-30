import HighchartsReact from "highcharts-react-official";
import React, { useState, useEffect } from "react";
import Highcharts from "highcharts/highstock";
import IndicatorsCore from "highcharts/indicators/indicators-all";
import VBP from "highcharts/indicators/volume-by-price";

IndicatorsCore(Highcharts);
VBP(Highcharts);

export default function Charts({ data }) {
  const stockPriceData = data?.charts?.results?.map((item) => [
    item.t,
    item.o,
    item.h,
    item.l,
    item.c,
  ]);
  const volumeData = data?.charts?.results?.map((item) => [item.t, item.v]);

  // console.log(stockPriceData, volumeData);

  const newOptions = {
    chart: {
      height: 600,
    },
    rangeSelector: {
      selected: 2,
    },
    title: {
      text: "AAPL Historical",
    },
    subtitle: {
      text: "With SMA and Volume by Price technical indicators",
    },
    yAxis: [
      {
        startOnTick: false,
        endOnTick: false,
        labels: {
          align: "right",
          x: -3,
        },
        title: {
          text: "OHLC",
        },
        height: "60%",
        lineWidth: 2,
        resize: {
          enabled: true,
        },
      },
      {
        labels: {
          align: "right",
          x: -3,
        },
        title: {
          text: "Volume",
        },
        top: "65%",
        height: "35%",
        offset: 0,
        lineWidth: 2,
      },
    ],
    tooltip: {
      split: true,
    },
    series: [
      {
        type: "candlestick",
        name: "AAPL",
        id: "aapl",
        zIndex: 2,
        data: stockPriceData,
      },
      {
        type: "column",
        name: "Volume",
        id: "volume",
        data: volumeData,
        yAxis: 1,
      },
      {
        type: "vbp",
        linkedTo: "aapl",
        params: {
          volumeSeriesID: "volume",
        },
        dataLabels: {
          enabled: false,
        },
        zoneLines: {
          enabled: false,
        },
      },
      {
        type: "sma",
        linkedTo: "aapl",
        zIndex: 1,
        marker: {
          enabled: false,
        },
      },
    ],
  };

  return (
    <div className="mb-5">
      <HighchartsReact
        highcharts={Highcharts}
        constructorType={"stockChart"}
        options={newOptions}
      />
    </div>
  );
}
