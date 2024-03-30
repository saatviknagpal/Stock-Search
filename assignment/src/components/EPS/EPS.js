import HighchartsReact from "highcharts-react-official";
import React from "react";
import Highcharts from "highcharts";
import "@/components/StockDetails/Details.css";
export default function EPS({ eps }) {
  console.log(eps);
  const categories = eps.map((data) => {
    return `${data.period} Surprise: ${data.surprise}`;
  });

  const actualData = eps.map((data) => data.actual);
  const estimateData = eps.map((data) => data.estimate);

  const options = {
    chart: {
      type: "spline",
      backgroundColor: "#F5F5F5",
    },
    title: {
      text: "Historical EPS Surprises",
      align: "center",
    },

    xAxis: {
      categories: categories,
    },
    yAxis: {
      title: {
        text: "Quarterly EPS",
      },
    },
    legend: {
      enabled: true,
    },
    tooltip: {
      headerFormat: "<b>{series.name}</b><br/>",
      pointFormat: "{series.name}: <b>{point.y}</b><br/>{point.category}",
    },
    plotOptions: {
      spline: {
        marker: {
          enable: false,
        },
      },
    },
    series: [
      {
        name: "Actual",
        data: actualData,
      },
      {
        name: "Estimate",
        data: estimateData,
      },
    ],
  };

  return (
    <>
      <div className="mb-5 chart_3">
        <HighchartsReact
          highcharts={Highcharts}
          options={options}
          className="chart_3"
        />
      </div>
    </>
  );
}
