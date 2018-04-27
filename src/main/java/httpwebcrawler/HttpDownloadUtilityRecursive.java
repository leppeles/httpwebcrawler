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

/**
 * @author toszi
 *
 */
public class HttpDownloadUtilityRecursive {
	private static int MAX_DEPTH;
	private static String rootURL;
//	private static final int PAGE_LIMIT = 100;
	private static String saveParentDir;
	int noOfPage = 0;
	private HashSet<String> visitedURLs;
	private static final int BUFFER_SIZE = 4096;
	private static final SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	/** SLF4J Logger */
	private final static Logger log = LoggerFactory.getLogger(HttpDownloadUtilityRecursive.class);

	/** Initializing set for URLs */
	public HttpDownloadUtilityRecursive() {
		visitedURLs = new HashSet<>();
	}

	public void run(String saveTo, String rootPageLink, int maxDepth) {
		MAX_DEPTH = maxDepth;
		log.info("~~~~~~~~~~~~~~~~~~~~HTTP crawler started~~~~~~~~~~~~~~~~~~~~");
		saveParentDir = saveTo;
		rootURL = rootPageLink;
		getPagesFromWeb(rootURL, 0);
	}

	private void getPagesFromWeb(String URL, int depth) {
		if (!visitedURLs.contains(URL) && depth < MAX_DEPTH /* && noOfPage < PAGE_LIMIT */ ) {
			log.info(">> Depth: " + depth + " [" + URL + "]");
			try {
				visitedURLs.add(URL);
				Document document = Jsoup.connect(URL).get();

				// get all URLs on given page, not distinct
				Elements allURLsOnPage = document.select("a[href]");
				depth++;

				// get distinct URLs and only the URLs which are pointing to original site
				HashSet<String> uniqueURLsOnPage = new HashSet<>();
				for (Element element : allURLsOnPage) {
					String currentLink = element.attr("abs:href");
					URL currentURL = new URL(currentLink);
					if (!uniqueURLsOnPage.contains(currentLink) && rootURL.contains(currentURL.getHost().toString())) {
						uniqueURLsOnPage.add(currentLink);
					}
				}
				for (String page : uniqueURLsOnPage) {

					downloadFile(page, saveParentDir);
//					noOfPage++;
//					if (noOfPage < PAGE_LIMIT) {
						getPagesFromWeb(page, depth);
//					} else {
//						log.info("Page limit reached.");
//						log.info("~~~~~~~~~~~~~~~~~~~~HTTP crawler ended~~~~~~~~~~~~~~~~~~~~");
//						System.exit(0);
//					}
				}
			} catch (IOException e) {
				System.err.println("For '" + URL + "': " + e.getMessage());
			}
			log.info("~~~~~~~~~~~~~~~~~~~~HTTP crawler ended~~~~~~~~~~~~~~~~~~~~");
		}
	}

	/**
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

		// checks HTTP response code first
		if (responseCode == HttpURLConnection.HTTP_OK) {

			// Log content for each downloaded file built with StringBuffer
			StringBuffer logContent = new StringBuffer();

			String fileNameWithPath = createFileNameWithPath(stringURL, url, saveParentDir);

			// Download only if modified
			// >= operator needed for pages that return 0 for getLastModified 
			// and they are newly discovered: file lastModified = 0
			if (httpConn.getLastModified() >= (new File(fileNameWithPath).lastModified())) {

				logContent.append(dateformat.format(new Date()) + " - Run started\n");

				String contentType = httpConn.getContentType();

				log.info("Content-Type = " + contentType);

				// opens input stream from the HTTP connection
				InputStream inputStream = httpConn.getInputStream();

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
				appendOrCreateLogForFile(stringURL, fileNameWithPath, logContent);
				log.info("File name = " + fileNameWithPath);
				log.info("File downloaded");
			} else {
				logContent.append(dateformat.format(new Date()) + " - File was already up-to-date\n");
				appendOrCreateLogForFile(stringURL, fileNameWithPath, logContent);
				log.info("File was already up-to-date: " + fileNameWithPath + "\n" + stringURL);
			}

		} else {
			log.error("No file to download. Server replied HTTP code: " + responseCode + "\n From site: " + stringURL);
		}
		httpConn.disconnect();
	}

	/**
	 * Builds string file name with path and creates location if not exists
	 * 
	 * @param stringURL
	 * @param url
	 * @param saveParentDir
	 * @return the html filename with absolute path, including sub-dirs from URL
	 *         structure
	 */
	private static String createFileNameWithPath(String stringURL, URL url, String saveParentDir) {
		String fileName = "";
		String dirSubPath = "";
		saveParentDir = (saveParentDir + File.separator + url.getHost());
		
		fileName = (stringURL.substring(stringURL.lastIndexOf("/") + 1, stringURL.length())
				+ (stringURL.endsWith(".html") ? "" : ".html")).replaceAll("[\\/:*?<|>]", "_");

		dirSubPath = url.getPath().toString();
		dirSubPath = (dirSubPath.substring(0, dirSubPath.lastIndexOf("/") + 1));
		dirSubPath.replace("/", File.separator);

		(new File(saveParentDir + dirSubPath)).mkdirs();
		String fileNameWithPath = saveParentDir + dirSubPath + File.separator + fileName;

		return fileNameWithPath;
	}

	/**
	 * Append to the top of the log file or create if not exists
	 * 
	 * @param fileNameWithPath
	 *            - The original file, log file will have the original file name
	 *            with a .log ending
	 * @param logContent
	 *            - content of log to append to the beginning of the file
	 */
	private static void appendOrCreateLogForFile(String url, String fileNameWithPath, StringBuffer logContent) {

		try {
			RandomAccessFile file = new RandomAccessFile(new File(fileNameWithPath + ".log"), "rws");
			byte[] text = new byte[(int) file.length()];

			if (file.length() == 0) {
				logContent.append("\nOriginal link: " + url);
			}
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