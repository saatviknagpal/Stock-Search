import React from "react";

export default function Footer(props) {
  return (
    <>
      <footer className="fixed-bottom w-100 bg-dark-subtle text-center pt-3">
        <p>
          <strong>Powered by</strong>{" "}
          <a href="https://www.finnhub.io">Finnhub.io</a>
        </p>
      </footer>
    </>
  );
}
