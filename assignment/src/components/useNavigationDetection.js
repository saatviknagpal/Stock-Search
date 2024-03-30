// useNavigationDetection.js
import { useState, useEffect } from "react";
import { useRouter } from "next/router";

let navigationHistory = [];

export default function useNavigationDetection() {
  const router = useRouter();
  const [isBackNavigation, setIsBackNavigation] = useState(false);

  useEffect(() => {
    const handleRouteChange = (url) => {
      const previousRoute = navigationHistory[navigationHistory.length - 2];
      const backNavDetected = url === previousRoute;

      setIsBackNavigation(backNavDetected);

      if (!backNavDetected) {
        navigationHistory.push(url);
        if (navigationHistory.length > 10) {
          navigationHistory.shift();
        }
      }
    };

    router.events.on("routeChangeComplete", handleRouteChange);

    if (navigationHistory.length === 0) {
      navigationHistory.push(router.asPath);
    }

    return () => {
      router.events.off("routeChangeComplete", handleRouteChange);
    };
  }, [router.asPath, router.events]);

  return isBackNavigation;
}
