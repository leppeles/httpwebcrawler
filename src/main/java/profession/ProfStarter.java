package profession;

import javax.swing.filechooser.FileSystemView;

public class ProfStarter {
	public static void main(String[] args) {
		new ProfessionCrawler().run("https://www.profession.hu/allasok/", "1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1",
				FileSystemView.getFileSystemView().getHomeDirectory().toString());
				//"C:\\Users\\Őszi\\Desktop\\prof");
	}
}