package httpwebcrawler;

public class Starter {

	public static void main(String[] args) {
		new HttpDownloadUtilityRecursive().run("C:\\Users\\toszi\\Desktop\\Crawler", "https://www.paypal.com/", 2);
	}

}
