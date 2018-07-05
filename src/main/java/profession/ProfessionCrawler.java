package profession;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.HashSet;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import httpwebcrawler.HttpDownloadUtilityRecursive;

public class ProfessionCrawler {

	private static String saveParentDir;
	int noOfPage = 0;
	private HashSet<String> visitedURLs;
	private static final int BUFFER_SIZE = 4096;
	private static Workbook workbook;
	private static final String sheetName = "Joblist";
	private static final SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	String rootURL;

	/** SLF4J Logger */
	private final static Logger log = LoggerFactory.getLogger(HttpDownloadUtilityRecursive.class);

	/** Initializing set for URLs */
	public ProfessionCrawler() {
		visitedURLs = new HashSet<>();
	}

	public void run(String globalProfURL, String localProfURL, String saveTo) {
		log.info("~~~~~~~~~~~~~~~~~~~~HTTP crawler started~~~~~~~~~~~~~~~~~~~~");
		saveParentDir = saveTo;
		workbook = createExcel();
		loopThrough(globalProfURL, localProfURL.substring(1));
	}

	private Workbook createExcel() {
		Workbook workbook = new XSSFWorkbook();

		Sheet sheet = workbook.createSheet(sheetName);
		sheet.setColumnWidth(0, 6000);
		sheet.setColumnWidth(1, 4000);

		Row header = sheet.createRow(0);

		CellStyle headerStyle = workbook.createCellStyle();
		headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
		headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

		XSSFFont font = ((XSSFWorkbook) workbook).createFont();
		font.setFontName("Arial");
		font.setFontHeightInPoints((short) 16);
		font.setBold(true);
		headerStyle.setFont(font);

		Cell headerCell = header.createCell(0);
		headerCell.setCellValue("Title");
		headerCell.setCellStyle(headerStyle);

		headerCell = header.createCell(1);
		headerCell.setCellValue("Salary");
		headerCell.setCellStyle(headerStyle);

		headerCell = header.createCell(2);
		headerCell.setCellValue("Location");
		headerCell.setCellStyle(headerStyle);

		headerCell = header.createCell(3);
		headerCell.setCellValue("Link");
		headerCell.setCellStyle(headerStyle);

		headerCell = header.createCell(4);
		headerCell.setCellValue("Description");
		headerCell.setCellStyle(headerStyle);

		CellStyle style = workbook.createCellStyle();
		style.setWrapText(true);

		return workbook;

	}

	private void loopThrough(String linkFirstPart, String linkSecondPart) {

		// int noOfPages = 0;
		// while (isExistingSite(linkFirstPart, linkSecondPart, noOfPages)) {
		// noOfPages++;
		// }

		for (int i = 0; i < 1; i++) {

			StringBuilder tempURL = new StringBuilder();
			tempURL.append(linkFirstPart);
			tempURL.append(i);
			tempURL.append(linkSecondPart);
			try {
				extractEntities(tempURL);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private boolean isExistingSite(String linkFirstPart, String linkSecondPart, int noOfPages) {

		StringBuilder tempURL = new StringBuilder();
		tempURL.append(linkFirstPart);
		tempURL.append(noOfPages);
		tempURL.append(linkSecondPart);

		URL url;
		int responseCode = 0;
		try {
			url = new URL(tempURL.toString());
			HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
			responseCode = httpConn.getResponseCode();
			System.out.println(tempURL);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return responseCode == HttpURLConnection.HTTP_OK;
	}

	private void extractEntities(StringBuilder tempURL) throws IOException {
		URL url = new URL(tempURL.toString());
		HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
		int responseCode = httpConn.getResponseCode();
		System.out.println(tempURL);

		// checks HTTP response code first
		if (responseCode == HttpURLConnection.HTTP_OK) {

			Document doc = Jsoup.connect(tempURL.toString()).get();
			Elements jobNodes = doc.getElementsByTag("li");

			for (Element jobNode : jobNodes) {

				Elements jobTitle = jobNode.getElementsByTag("strong");
				Elements jobSalary = jobNode.getElementsByTag("dd");
				Elements jobLocation = jobNode.getElementsByClass("position_and_company").select("span");
				Elements jobLink = jobNode.select("a[href]");
				Elements jobDescription = jobNode.getElementsByClass("list_tasks");

				if (jobTitle.size() > 0 && jobSalary.size() > 0) {

					System.out.print(jobTitle.get(0).text());
					System.out.print(" ----- ");
					System.out.print(jobSalary.get(0).text());
					System.out.print(" ----- ");
					System.out.print(jobLocation.hasText() ? jobLocation.get(0).text() : "");
					System.out.print(" ----- ");
					System.out.print(jobLink.isEmpty() ? "" : jobLink.get(0).attr("href"));
					System.out.print(" ----- ");
					System.out.println(jobDescription.hasText() ? jobDescription.get(0).text() : "");

					Workbook workbook = new XSSFWorkbook();

					Sheet sheet = workbook.createSheet("temp");
					sheet.setColumnWidth(0, 6000);
					sheet.setColumnWidth(1, 4000);

					Row header = sheet.createRow(0);

					CellStyle headerStyle = workbook.createCellStyle();
					headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
					headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

					XSSFFont font = ((XSSFWorkbook) workbook).createFont();
					font.setFontName("Arial");
					font.setFontHeightInPoints((short) 16);
					font.setBold(true);
					headerStyle.setFont(font);

					Cell headerCell = header.createCell(0);
					headerCell.setCellValue("Name");
					headerCell.setCellStyle(headerStyle);

					headerCell = header.createCell(1);
					headerCell.setCellValue("Age");
					headerCell.setCellStyle(headerStyle);

					CellStyle style = workbook.createCellStyle();
					style.setWrapText(true);

					Row row = sheet.createRow(2);
					Cell cell = row.createCell(0);
					cell.setCellValue("John Smith");
					cell.setCellStyle(style);

					cell = row.createCell(1);
					cell.setCellValue(20);
					cell.setCellStyle(style);

					File currDir = new File(saveParentDir);
					String path = currDir.getAbsolutePath();
					String fileLocation = path.substring(0, path.length() - 1) + "profSalaries.xlsx";

					FileOutputStream outputStream = new FileOutputStream(fileLocation);
					workbook.write(outputStream);
					workbook.close();

				}
			}
		} else {
			log.error("No file to download. Server replied HTTP code: " + responseCode + "\n From site: " + tempURL);
		}
		httpConn.disconnect();
	}

}
