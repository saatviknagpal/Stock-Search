"use client";

import React from "react";
import "@/components/Navbar/Navbar.css";
import { usePathname, useRouter } from "next/navigation";
export default function Navbar() {
  const router = usePathname();

  console.log(router);

  const isActive = (route) => router.startsWith(route);
  return (
    <>
      <nav className="navbar fixed-top navbar-expand-lg navbar-dark navbar_cl">
        <div className="container-fluid">
          <a className="navbar-brand" href="/">
            Stock Search
          </a>
          <button
            className="navbar-toggler"
            type="button"
            data-bs-toggle="collapse"
            data-bs-target="#navbarNavAltMarkup"
            aria-controls="navbarNavAltMarkup"
            aria-expanded="false"
            aria-label="Toggle navigation"
          >
            <span className="navbar-toggler-icon"></span>
          </button>
          <div className="collapse navbar-collapse" id="navbarNavAltMarkup">
            <div className="navbar-nav ms-auto">
              <a
                className={`nav-link ${
                  isActive("/search") ? "border border-white rounded-pill" : ""
                }`}
                href="/"
              >
                Search
              </a>
              <a
                className={`nav-link ${
                  isActive("/watchlist")
                    ? "border border-white rounded-pill"
                    : ""
                }`}
                href="/watchlist"
              >
                Watchlist
              </a>
              <a
                className={`nav-link ${
                  isActive("/portfolio")
                    ? "border border-white rounded-pill"
                    : ""
                }`}
                href="/portfolio"
              >
                Portfolio
              </a>
            </div>
          </div>
        </div>
      </nav>
    </>
  );
}
