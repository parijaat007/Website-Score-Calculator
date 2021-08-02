package com.company;

import java.io.*;
import java.util.*;
import org.openqa.selenium.net.UrlChecker;
import org.openqa.selenium.support.ui.Select;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.awt.Desktop;
import java.util.Vector;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

public class WebsiteScoreCalculator {
    public static void main(final String[] args) {
        /// opening file io
        //String filePath = "C:\\Users\\pc\\Documents\\AssignmentHCI.xlsx";
        final String filePath = "PartA_LinksOutput.xls";
        final String sheetName = "Sheet1";
        final String Operator="BSNL Fiber";
        final File fileData = new File(filePath);
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(fileData);
        } catch (final FileNotFoundException fileNotFoundException) {
            fileNotFoundException.printStackTrace();
        }
        Workbook workbookXLSX = null;
        try {
            workbookXLSX = new XSSFWorkbook(fileInputStream);
        } catch (final IOException ioException) {
            ioException.printStackTrace();
        }
        final Sheet workbookXLSXSheet = workbookXLSX.getSheet(sheetName);
        int rowCount = 0;

        // setting options for our webdriver
        System.setProperty("webdriver.chrome.driver", "C:\\chromedriver.exe");
        final ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--no-sandbox");
        chromeOptions.addArguments("--ignore-certificate-errors");
        chromeOptions.addArguments("--disable-dev-shm-usage");
        chromeOptions.addArguments("headless");
        chromeOptions.addArguments("window-size=1200x600");

        // creating webdriver with above options to scrape homepage for all links
        final WebDriver chromeDriver = new ChromeDriver(chromeOptions);

        HttpURLConnection httpURLConnection;

        // list of all homepages to scrape for links to visit
        final String[] strArray = {"https://nrega.nic.in/netnrega/home.aspx", 
                                    "https://www.usa.gov/", 
                                    "https://www.bits-pilani.ac.in/", 
                                    "https://www.isro.gov.in/", 
                                    "https://medium.com/"};
        final List<String> baseURL = Arrays.asList(strArray);

        // iterating over home pages
        for (int urlArrayIterator = 0; urlArrayIterator < 5; urlArrayIterator++)
        {
            chromeDriver.get(baseURL.get(urlArrayIterator));
            // creating list of all links in home page
            final List<WebElement> webElementList = chromeDriver.findElements(By.tagName("a"));

            // accessing all links and finding average time/no of dead links
            for (int i = 0; i < webElementList.size(); i++) {

                //Generating values to enter into excel sheet
                final Row row = workbookXLSXSheet.createRow(rowCount++);
                final Cell websiteLinkColumn = row.createCell(0);
                websiteLinkColumn.setCellValue(baseURL.get(urlArrayIterator));
                final Cell urlFullLinkColumn = row.createCell(1);
                urlFullLinkColumn.setCellValue(webElementList.get(i).getAttribute("href"));
                final Cell operatorNameColumn = row.createCell(2);
                operatorNameColumn.setCellValue(Operator);

                // getting each URL on the website and loading it
                final String linkUrlAttribute = webElementList.get(i).getAttribute("href");
                System.out.println(linkUrlAttribute);

                try {
                    // checking for HTTP response code
                    httpURLConnection = (HttpURLConnection) (new URL(linkUrlAttribute).openConnection());

                    httpURLConnection.setRequestMethod("HEAD");

                    httpURLConnection.connect();

                    final int respCode = httpURLConnection.getResponseCode();

                    // >=400 response code implies errors in loading the link
                    if (respCode >= 400) {
                        final Cell linkLoadTimeColumn = row.createCell(3);
                        linkLoadTimeColumn.setCellValue("0");
                        final Cell linkResponceDeadColumn = row.createCell(4);
                        linkResponceDeadColumn.setCellValue("Y");
                        System.out.println(respCode);
                    }
                    else {
                        // loading the link 5 times and calculating the average load time
                        float totalTime = 0;
                        for (int numberOfTrailAttempts = 0; numberOfTrailAttempts < 5; numberOfTrailAttempts++) {
                            final WebDriver driver1 = new ChromeDriver(chromeOptions);
                            final long start = System.currentTimeMillis();
                            driver1.get(linkUrlAttribute);
                            final long finish = System.currentTimeMillis();
                            driver1.close();
                            totalTime = totalTime + finish - start;
                        }
                        // sending totalTime taken for loading the link 5 times and averaging it in the excel sheet
                        final Cell linkLoadTimeColumn = row.createCell(3);
                        linkLoadTimeColumn.setCellValue(totalTime/5);
                        final Cell linkResponceDeadColumn = row.createCell(4);
                        linkResponceDeadColumn.setCellValue("N");
                    }
                }
                catch (final Exception exception) {
                    exception.printStackTrace();
                }
            }
        }

        try {
            fileInputStream.close();
        } catch (final IOException ioException) {
            ioException.printStackTrace();
        }

        // opening an output stream to print all the results
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(fileData);
            workbookXLSX.write(fileOutputStream);
            fileOutputStream.close();
            workbookXLSX.close();
        } catch (final FileNotFoundException fileNotFoundException) {
            fileNotFoundException.printStackTrace();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        // opening the excel sheet on the desktop after writing all the entries
        final Desktop desktop = Desktop.getDesktop();
        try {
            desktop.open(fileData);
        } catch (final IOException ioException) {
            ioException.printStackTrace();
        }
    }
}
