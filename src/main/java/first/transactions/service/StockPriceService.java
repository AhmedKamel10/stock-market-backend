
package first.transactions.service;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Service
public class StockPriceService {

    // 👉 Replace this with your real Finnhub API key
    private static final String API_KEY = "d2iq8bhr01qhm15blvi0d2iq8bhr01qhm15blvig";

    public static double getStockPrice(String symbol) {
        try {
            // Trim spaces and encode safely for URL
            symbol = URLEncoder.encode(symbol.trim(), "UTF-8");

            String urlStr = "https://finnhub.io/api/v1/quote?symbol=" + symbol + "&token=" + API_KEY;

            // Open connection
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            // Read response
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

            in.close();
            conn.disconnect();

            // Parse JSON
            JSONObject json = new JSONObject(content.toString());

            // "c" = current price
            return json.getDouble("c");

        } catch (Exception e) {
            System.err.println("⚠️ Failed to fetch price for " + symbol + " : " + e.getMessage());
            return -1;
        }
    }

    // Quick test
    public static void main(String[] args) {
        double price = getStockPrice("AAPL");
        if (price != -1) {
            System.out.println("AAPL Price: " + price);
        }
    }
}