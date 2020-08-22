/***
 * Creator: Kylan Thomson
 * Date: 08/21/2020
 *
 * Description: This program allows the user to create a Stock Market
 * watch list. The program scrapes data for every stock on the watch
 * list and provides descriptive/inferential statistics.
 *
 * Version 1.01: More accurately adds stocks to the watch list
 *
 */

package StockAnalyzer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws IOException {
        ArrayList<String> watchList = getWatchList();
        ArrayList<String> stockTicker = getStockTicker();
        ArrayList<String> securityName = getSecurityName();
        ArrayList<Integer> mappedWatchList = mapWatchList(watchList, stockTicker, securityName);

        ArrayList<Double> amazon = getPriceHistory("AMZN");
        System.out.println((amazon.size()) / 5 + " days of data");

        for(int i = 0; i < mappedWatchList.size(); i ++){
            System.out.println("The current stock price for " + securityName.get(mappedWatchList.get(i)) + " (" + stockTicker.get(mappedWatchList.get(i)) + ") is " + getStockPrice(stockTicker.get(mappedWatchList.get(i))));
        }
    }



    public static ArrayList<String> getWatchList(){
        Scanner scanner = new Scanner(System.in);
        ArrayList<String> watchList = new ArrayList<String>();
        boolean flag = true;

        System.out.println("Create your Watch List");
        while(flag == true){
            String entry = scanner.nextLine().toUpperCase();
            if(entry.contains("STOP")) break;
            else{
                watchList.add(entry);
            }
        }
        return watchList;
    }

    public static ArrayList<Integer> mapWatchList(ArrayList<String> watchList, ArrayList<String> stockTicker, ArrayList<String> securityName) throws IOException {
        ArrayList<Integer> mappedWatchList = new ArrayList<Integer>();
        for(int i = 0; i < watchList.size(); i++){
            int count = 1;
            for(int j = 0; j < stockTicker.size(); j++){
                if(stockTicker.get(j).toUpperCase().contains(watchList.get(i)) || securityName.get(j).toUpperCase().contains(watchList.get(i))){
                    if(count > 1 && securityName.get(mappedWatchList.get(mappedWatchList.size() - 1)).length() > securityName.get(j).length()) {
                        mappedWatchList.set(mappedWatchList.size() - 1, j);
                    }
                    if(count < 2) {
                        mappedWatchList.add(j);
                        count++;
                    }
                }
            }
        }
        return mappedWatchList;
    }

    public static ArrayList<String> getStockTicker() throws IOException {
        ArrayList<String> stockTicker = new ArrayList<String>();
        URL stockDataURL = new URL("http://ftp.nasdaqtrader.com/dynamic/SymDir/nasdaqlisted.txt"); //stock ticker info
        URLConnection urlConn = stockDataURL.openConnection();
        urlConn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
        urlConn.connect();
        InputStreamReader inStream = new InputStreamReader(urlConn.getInputStream());
        BufferedReader buff = new BufferedReader(inStream);
        String line = "";
        while (line != null) {
            line = buff.readLine();
            if(line.contains("Symbol")) continue;
            if(line.contains("File Creation")) break;
            int target = line.indexOf("|");
            String ticker = line.substring(0,target);
            stockTicker.add(ticker);
        }
        return stockTicker;
    }

    public static ArrayList<String> getSecurityName() throws IOException {
        ArrayList<String> securityName = new ArrayList<String>();
        URL stockDataURL = new URL("http://ftp.nasdaqtrader.com/dynamic/SymDir/nasdaqlisted.txt"); //stock ticker info
        URLConnection urlConn = stockDataURL.openConnection();
        urlConn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
        urlConn.connect();
        InputStreamReader inStream = new InputStreamReader(urlConn.getInputStream());
        BufferedReader buff = new BufferedReader(inStream);
        String line = "";
        while (line != null) {
            line = buff.readLine();
            if(line.contains("Symbol")) continue;
            if(line.contains("File Creation")) break;
            int start = line.indexOf("|") + 1;
            int end = line.indexOf("|", line.indexOf("|") + 1);
            String name = line.substring(start,end);
            securityName.add(name);
        }
        return securityName;
    }


    /**
     * @param stockSYM
     * @throws IOException receives String array of stock tickers and uses the array to create a
     *                     unique google finance URL for each stock. Each URL is scraped to retrieve
     *                     the price for each stock.
     */
    public static double getStockPrice(String stockSYM) throws IOException {

        double stockPrice = -1.0;


        URL stockDataURL = new URL("https://www.google.com/search?tbm=fin&q=" + stockSYM);
        URLConnection urlConn = stockDataURL.openConnection();
        urlConn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
        urlConn.connect();
        InputStreamReader inStream = new InputStreamReader(urlConn.getInputStream());
        BufferedReader buff = new BufferedReader(inStream);

        String line = buff.readLine();
        String price = "not found";

        while (line != null) {
            if (line.contains("\"> USD</span></span></span> <span class=\"")) {
                int target = line.indexOf("\"> USD</span></span></span> <span class=\"");
                int deci = line.indexOf(".", (target - 55));
                int start = deci;
                while (line.charAt(start) != '>') {
                    start--;
                }
                price = (line.substring((start + 1), deci + 3));
                if (price.contains(",")) {
                    price = price.replaceAll(",", "");
                }
                stockPrice = Double.parseDouble(price);
            }
            line = buff.readLine();
        }
        return stockPrice;
    }

    /***
     * @param stockSYM
     * @return
     * @throws IOException
     *
     * Returns an array list of historical stock market data
     * from the last 100 days.
     *
     * Every fifth element is the adj. close price
     * Every (5 - 1) element is the close* price
     * Every (5 - 2) element is the low price
     * Every (5 - 3) element is the high price
     * Every (5 - 4) element is the open price
     * 
     */

    public static ArrayList<Double> getPriceHistory(String stockSYM) throws IOException {

        double stockPrice = -1.0;
        ArrayList<Double> priceHistory = new ArrayList<Double>();


        URL stockDataURL = new URL("https://finance.yahoo.com/quote/" + stockSYM + "/history/");
        URLConnection urlConn = stockDataURL.openConnection();
        urlConn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
        urlConn.connect();
        InputStreamReader inStream = new InputStreamReader(urlConn.getInputStream());
        BufferedReader buff = new BufferedReader(inStream);

        String line = buff.readLine();
        String price = "not found";

        while (line != null) {

            if (line.contains("</span></td><td class=\"Py(10px) Pstart(10px)\" data-reactid=\"")) {
                int target = line.indexOf("</span></td><td class=\"Py(10px) Pstart(10px)\" data-reactid=\"");
                int deci = line.indexOf(".", target);
                int start = deci;
                while (line.charAt(start) != '>') {
                    start--;
                }
                price = (line.substring((start + 1), deci + 3));
                if (price.contains(",")) {
                    price = price.replaceAll(",", "");
                }
                stockPrice = Double.parseDouble(price);
                priceHistory.add(stockPrice);

                boolean flag = true;
                do {
                    try {
                        target = line.indexOf("</span></td><td class=\"Py(10px) Pstart(10px)\" data-reactid=\"", start);
                        start = line.indexOf(".", target);
                        while (line.charAt(start) != '>') {
                            start--;
                        }
                        deci = line.indexOf(".", start);
                        price = (line.substring((start + 1), deci + 3));
                        if (price.contains(",")) {
                            price = price.replaceAll(",", "");
                        }
                        //System.out.println("The price is: " + price);
                        stockPrice = Double.parseDouble(price);
                        priceHistory.add(stockPrice);
                    } catch (Exception e) {
                        flag = false;
                    }
                }
                while(flag);
            }
            line = buff.readLine();
        }
        return priceHistory;
    }
}
/**********
 * sentiment analysis:
 * https://som.yale.edu/faculty-research-centers/centers-initiatives/international-center-for-finance/data/stock-market-confidence-indices/united-states-stock-market-confidence-indices
 */
