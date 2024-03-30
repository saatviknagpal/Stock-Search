import HighchartsReact from "highcharts-react-official";
import React from "react";
import Highcharts from "highcharts";
import "@/components/StockDetails/Details.css";

export default function RecommendationTrend({ trendData }) {
  console.log(trendData);

  let buyArray = [];
  let holdArray = [];
  let sellArray = [];
  let strongBuyArray = [];
  let strongSellArray = [];
  let periodArray = [];

  trendData.forEach((item) => {
    buyArray.push(item.buy);
    holdArray.push(item.hold);
    sellArray.push(item.sell);
    strongBuyArray.push(item.strongBuy);
    strongSellArray.push(item.strongSell);
    periodArray.push(item.period);
  });

  const options = {
    chart: {
      type: "column",
      backgroundColor: "#F5F5F5",
      width: "600",
      reflow: true,
    },
    responsive: {
      rules: [
        {
          condition: {
            maxWidth: 500,
          },
        },
      ],
    },

    title: {
      text: "Recommendation Trends",
      align: "center",
    },
    xAxis: {
      categories: periodArray,
    },
    yAxis: {
      min: 0,
      title: {
        text: "#Analysis",
      },
      stackLabels: {
        enabled: true,
      },
    },

    tooltip: {
      headerFormat: "<b>{point.x}</b><br/>",
      pointFormat: "{series.name}: {point.y}<br/>Total: {point.stackTotal}",
    },
    plotOptions: {
      column: {
        stacking: "normal",
        dataLabels: {
          enabled: true,
        },
      },
    },
    series: [
      {
        name: "Strong Buy",
        data: strongBuyArray,
        color: "#195F32",
      },
      {
        name: "Buy",
        data: buyArray,
        color: "#23AF50",
      },
      {
        name: "Hold",
        data: holdArray,
        color: "#AF7D28",
      },
      {
        name: "Sell",
        data: sellArray,
        color: "#F05050",
      },
      {
        name: "Strong Sell",
        data: strongSellArray,
        color: "#732828",
      },
    ],
  };

  return (
    <>
      <div className="mb-5 chart_2">
        <HighchartsReact
          highcharts={Highcharts}
          options={options}
          className="chart_2"
        />
      </div>
    </>
  );
}
