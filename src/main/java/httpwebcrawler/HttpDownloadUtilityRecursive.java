package httpwebcrawler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HttpDownloadUtilityRecursive {
	private static final int MAX_DEPTH = 3;
	private static final int PAGE_LIMIT = 100;
	private String saveParentDir;
	int noOfPage = 0;
	private HashSet<String> visitedLinks;
	private static final int BUFFER_SIZE = 4096;
	private static final SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	/** SLF4J Logger */
	private final static Logger log = LoggerFactory.getLogger(HttpDownloadUtilityRecursive.class);

	public void run(String saveTo, String link, int depth) {

		log.info("~~~~~~~~~~~~~~~~~~~~HTTP crawler started~~~~~~~~~~~~~~~~~~~~");
		this.saveParentDir = saveTo;
		getPagesFromWeb(link, depth);
	}

	public HttpDownloadUtilityRecursive() {
		visitedLinks = new HashSet<>();
	}

	private void getPagesFromWeb(String URL, int depth) {
		if (!visitedLinks.contains(URL) && depth < MAX_DEPTH && noOfPage < PAGE_LIMIT) {
			log.info(">> Depth: " + depth + " [" + URL + "]");
			try {
				visitedLinks.add(URL);
				Document document = Jsoup.connect(URL).get();
				// get all links on given page, not distinct
				Elements allLinksOnPage = document.select("a[href]");
				depth++;
				// get distinct links
				HashSet<String> uniqueLinksOnPage =  new HashSet<>();
				for (Element element : allLinksOnPage) {
					
					String currentLink = element.attr("abs:href");
					if (!uniqueLinksOnPage.contains(currentLink)) {
						uniqueLinksOnPage.add(currentLink);
					}
				}
				for (String page : uniqueLinksOnPage) {

					downloadFile(page, saveParentDir);
					noOfPage++;
					if (noOfPage < PAGE_LIMIT) {
						getPagesFromWeb(page, depth);
					} else {
						log.info("Page limit reached.");
						log.info("~~~~~~~~~~~~~~~~~~~~HTTP crawler ended~~~~~~~~~~~~~~~~~~~~");
						System.exit(0);
					}
				}
			} catch (IOException e) {
				System.err.println("For '" + URL + "': " + e.getMessage());
			}
			log.info("~~~~~~~~~~~~~~~~~~~~HTTP crawler ended~~~~~~~~~~~~~~~~~~~~");
		}
	}

	/**
	 * source:
	 * https://stackoverflow.com/questions/26454916/download-the-entire-webpage
	 * Downloads a file from a URL
	 * 
	 * @param stringURL
	 *            HTTP URL of the site to be downloaded
	 * @param saveParentDir
	 *            path of the directory to save the file
	 * @throws IOException
	 */
	private static void downloadFile(String stringURL, String saveParentDir) throws IOException {
		if (stringURL.endsWith("/")) {
			stringURL = stringURL.substring(0, stringURL.length() - 1);
		}
		URL url = new URL(stringURL);
		HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
		int responseCode = httpConn.getResponseCode();

		// always check HTTP response code first
		if (responseCode == HttpURLConnection.HTTP_OK) {
			StringBuffer logContent = new StringBuffer();
			logContent.append(dateformat.format(new Date()) + " - Run started\n");

			String fileName = "";
			String dirSubPath = "";
			String contentType = httpConn.getContentType();
			int contentLength = httpConn.getContentLength();

			// extracts file name from URL
			fileName = (stringURL.substring(stringURL.lastIndexOf("/") + 1, stringURL.length())
					+ (stringURL.endsWith(".html") ? "" : ".html")).replaceAll("[\\/:*?<|>]", "_");

			log.info("Content-Type = " + contentType);
			log.info("Content-Length = " + contentLength);

			dirSubPath = url.getPath().toString();
			dirSubPath = (dirSubPath.substring(0, dirSubPath.lastIndexOf("/") + 1));
			dirSubPath.replace("/", File.separator);

			(new File(saveParentDir + dirSubPath)).mkdirs();

			// opens input stream from the HTTP connection
			InputStream inputStream = httpConn.getInputStream();
			String fileNameWithPath = saveParentDir + dirSubPath + File.separator + fileName;

			// opens an output stream to save into file
			FileOutputStream outputStream = new FileOutputStream(fileNameWithPath);

			int bytesRead = -1;
			byte[] buffer = new byte[BUFFER_SIZE];
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
			}

			outputStream.close();
			inputStream.close();

			logContent.append("" + dateformat.format(new Date()) + " - Run terminated\n");
			createLogForFile(fileNameWithPath, logContent);
			log.info("File name = " + fileNameWithPath);
			log.info("File downloaded");
		} else {
			log.error("No file to download. Server replied HTTP code: " + responseCode);
		}
		httpConn.disconnect();
	}

	private static void createLogForFile(String fileNameWithPath, StringBuffer logContent) {

		try {
			// then append to the top of the file
			RandomAccessFile file = new RandomAccessFile(new File(fileNameWithPath + ".log"), "rws");
			byte[] text = new byte[(int) file.length()];
			file.readFully(text);
			file.seek(0);
			file.writeBytes(logContent.toString() + "\n");
			file.write(text);
			file.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}