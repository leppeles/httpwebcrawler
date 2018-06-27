package profession;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.HashSet;

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
		getJobs(globalProfURL, localProfURL.substring(1));
	}

	private void getJobs(String linkFirstPart, String linkSecondPart) {
		for (int i = 0; i < 2; i++) {

			StringBuilder tempURL = new StringBuilder();
			tempURL.append(linkFirstPart);
			tempURL.append(i);
			tempURL.append(linkSecondPart);
			try {
				getListOfJobs(tempURL);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private void getListOfJobs(StringBuilder tempURL) throws IOException {
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
				if (jobTitle.size() > 0 && jobSalary.size() > 0) {
					System.out.print(jobTitle.get(0).text());
					System.out.print(" ----- ");
					System.out.println(jobSalary.get(0).text());
				}
			}
		} else {
			log.error("No file to download. Server replied HTTP code: " + responseCode + "\n From site: " + tempURL);
		}
		httpConn.disconnect();
	}

	/**
	 * Downloads a file from a URL
	 * 
	 * @param stringURL HTTP URL of the site to be downloaded
	 * @param saveParentDir path of the directory to save the file
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
	 * @return the html filename with absolute path, including sub-dirs from URL structure
	 */
	private static String createFileNameWithPath(String stringURL, URL url, String saveParentDir) {
		String fileName = "";
		String dirSubPath = "";
		saveParentDir = (saveParentDir + File.separator + url.getHost());

		fileName = (stringURL.substring(stringURL.lastIndexOf("/") + 1, stringURL.length()) + (stringURL.endsWith(".html") ? "" : ".html"))
				.replaceAll("[\\/:*?<|>]", "_");

		dirSubPath = url.getPath().toString();
		dirSubPath = (dirSubPath.substring(0, dirSubPath.lastIndexOf("/") + 1));
		dirSubPath.replace("/", File.separator);

		(new File(saveParentDir + dirSubPath)).mkdirs();
		String fileNameWithPath = saveParentDir + dirSubPath + File.separator + fileName;

		return fileNameWithPath;
	}

}
