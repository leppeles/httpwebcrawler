package httpwebcrawler;

public class Starter {

	public static void main(String[] args) {
		new HttpDownloadUtilityRecursive().run("C:\\Users\\toszi\\Desktop\\Crawler", "http://www.gitlab.com/", 2);
	}

}
