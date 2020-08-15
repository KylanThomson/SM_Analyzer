package StockAnalyzer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Scanner;

public class StockMarketAnalyzer {

    public static void main(String[] args) throws IOException {
        ArrayList<String> watchList = getWatchList();
        ArrayList<String> stockTicker = getStockTicker();
        ArrayList<String> securityName = getSecurityName();
        ArrayList<Integer> mappedWatchList = mapWatchList(watchList, stockTicker, securityName);
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
            for(int j = 0; j < stockTicker.size(); j++){
                if(stockTicker.get(j).toUpperCase().contains(watchList.get(i)) || securityName.get(j).toUpperCase().contains(watchList.get(i))){
                    mappedWatchList.add(j);
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
}

