/***
 * Creator: Kylan Thomson
 * Last Modified: 08/21/2020
 *
 * Description: This program allows the user to create a Stock Market
 * watch list. The program scrapes data for every stock on the watch
 * list and provides descriptive/inferential statistics.
 *
 * Version 1.02: added the ability to scrape
 * the last 100 days of stock market data
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

        for(int i = 0; i < mappedWatchList.size(); i ++){

            String company = securityName.get(mappedWatchList.get(i));
            String ticker = stockTicker.get(mappedWatchList.get(i));
            double stockPrice = getStockPrice(stockTicker.get(mappedWatchList.get(i)));
            ArrayList<Double> priceHistory = getPriceHistory(stockTicker.get(mappedWatchList.get(i)));
            ArrayList<Double> linRegOpen = linReg(getOpenHistory(priceHistory));
            ArrayList<Double> linRegHigh = linReg(getHighHistory(priceHistory));
            ArrayList<Double> linRegLow = linReg(getLowHistory(priceHistory));
            ArrayList<Double> linRegClose = linReg(getCloseHistory(priceHistory));

            ArrayList<Double> recLinRegOpen = recLinReg(getOpenHistory(priceHistory));
            ArrayList<Double> recLinRegHigh = recLinReg(getHighHistory(priceHistory));
            ArrayList<Double> recLinRegLow = recLinReg(getLowHistory(priceHistory));
            ArrayList<Double> recLinRegClose = recLinReg(getCloseHistory(priceHistory));

            String openEst = String.format("%.02f", 101.0 * linRegOpen.get(0) + linRegOpen.get(1));
            String highEst = String.format("%.02f", 101.0 * linRegHigh.get(0) + linRegHigh.get(1));
            String lowEst = String.format("%.02f", 101.0 * linRegLow.get(0) + linRegLow.get(1));
            String closeEst = String.format("%.02f", 101.0 * linRegClose.get(0) + linRegClose.get(1));

            String recOpenEst = String.format("%.02f", 101.0 * recLinRegOpen.get(0) + recLinRegOpen.get(1));
            String recHighEst = String.format("%.02f", 101.0 * recLinRegHigh.get(0) + recLinRegHigh.get(1));
            String recLowEst = String.format("%.02f", 101.0 * recLinRegLow.get(0) + recLinRegLow.get(1));
            String recCloseEst = String.format("%.02f", 101.0 * recLinRegClose.get(0) + recLinRegClose.get(1));

            System.out.println("***********************************************************************************************");
            System.out.println("");
            System.out.println(company + " ( $" + stockPrice + ")");
            System.out.println("");
            System.out.println("Predicted open price for " + ticker + " is: $" + openEst);
            System.out.println("Predicted high for " + ticker + " is: $" + highEst);
            System.out.println("Predicted low for " + ticker + " is: $" + lowEst);
            System.out.println("Predicted close for " + ticker + " is: $" + closeEst);
            System.out.println("");
            System.out.println("Predicted open price for " + ticker + " with recursive linear regression is: $" + recOpenEst);
            System.out.println("Predicted high price for " + ticker + " with recursive linear regression is: $" + recHighEst);
            System.out.println("Predicted low price for " + ticker + " with recursive linear regression is: $" + recLowEst);
            System.out.println("Predicted close price for " + ticker + " with recursive linear regression is: $" + recCloseEst);
            System.out.println("");
            System.out.println("***********************************************************************************************");
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
    public static ArrayList<Double> getOpenHistory(ArrayList<Double> priceHistory){
        ArrayList<Double> openHistory = new ArrayList<Double>();
        for(int i = 0; i < priceHistory.size(); i+=5){
            openHistory.add(priceHistory.get(i));
        }
        return openHistory;
    }

    public static ArrayList<Double> getHighHistory(ArrayList<Double> priceHistory){
        ArrayList<Double> highHistory = new ArrayList<Double>();
        for(int i = 1; i < priceHistory.size(); i+=5){
            highHistory.add(priceHistory.get(i));
        }
        return highHistory;
    }
    public static ArrayList<Double> getLowHistory(ArrayList<Double> priceHistory){
        ArrayList<Double> lowHistory = new ArrayList<Double>();
        for(int i = 2; i < priceHistory.size(); i+=5){
            lowHistory.add(priceHistory.get(i));
        }
        return lowHistory;
    }

    public static ArrayList<Double> getCloseHistory(ArrayList<Double> priceHistory){
        ArrayList<Double> closeHistory = new ArrayList<Double>();
        for(int i = 3; i < priceHistory.size(); i += 5){
            closeHistory.add(priceHistory.get(i));
        }
        return closeHistory;
    }

    public static ArrayList<Double> getAdjCloseHistory(ArrayList<Double> priceHistory){
        ArrayList<Double> adjCloseHistory = new ArrayList<Double>();
        for(int i = 4; i < priceHistory.size(); i+=5){
            adjCloseHistory.add(priceHistory.get(i));
        }
        return adjCloseHistory;
    }

    public static ArrayList<Double> linReg(ArrayList<Double> data){

        ArrayList<Double> linRegStats = new ArrayList<Double>();
        double xSum = 0;
        double ySum = 0;
        int n = data.size();
        double xAvg = 0;
        double yAvg = 0;
        double x_std_dev = 0;
        double y_std_dev = 0;
        double rNumerator = 0;
        double r = 0;
        double rSquare = 0;
        double slope = 0;
        double a = 0;

        for(int i = 0; i < n; i++) {
            xSum += i;
            ySum += data.get(i);
        }
        xAvg = (xSum + n) / n;
        yAvg = ySum / n;
        for(int i = 1; i <= n; i++){
            x_std_dev += Math.pow((((n+1) - i) - xAvg), 2);
            y_std_dev += Math.pow((data.get(i - 1) - yAvg), 2);
            rNumerator += ((((n + 1) - i) - xAvg) * (data.get(i - 1) - yAvg));
        }
        r = rNumerator / (Math.sqrt((x_std_dev * y_std_dev)));
        rSquare = Math.pow(r, 2);
        x_std_dev = Math.sqrt(x_std_dev / (n - 1));
        y_std_dev = Math.sqrt(y_std_dev / (n -1));
        slope = r * ((y_std_dev/x_std_dev));
        a = yAvg - slope * xAvg;

        linRegStats.add(slope);
        linRegStats.add(a);

        return linRegStats;
        }

        public static ArrayList<Double> recLinReg(ArrayList<Double> data){
        ArrayList<Double> slope = new ArrayList<Double>();
        double b = linReg(data).get(1);
        //ArrayList<Double> a = new ArrayList<Double>();
        ArrayList<Double> recursiveLinReg = new ArrayList<Double>();
        double slopeSum = 0;
        double aSum = 0;
        double slopeAvg = 0;
        //double aAvg = 0;
        double threshold = data.size() * 0.04;

        while(data.size() > threshold){
                int lastElement = data.size() - 1;
                data.remove(lastElement);
                slope.add(linReg(data).get(0));
                //a.add(linReg(data).get(1));
                }
        int n = slope.size();
        for(int i = 0; i < n; i++){
            slopeSum += slope.get(i);
           // aSum += a.get(i);
        }
            slopeAvg = slopeSum / n;
           // aAvg = aSum / n;
            recursiveLinReg.add(slopeAvg);
            recursiveLinReg.add(b);
        return recursiveLinReg;
        }
    }



/**********
 * sentiment analysis:
 * https://som.yale.edu/faculty-research-centers/centers-initiatives/international-center-for-finance/data/stock-market-confidence-indices/united-states-stock-market-confidence-indices
 */
