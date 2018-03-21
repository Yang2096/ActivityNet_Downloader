package download;
import java.util.ArrayList;

public class DownloadUrls {
	ArrayList<DownloadInfo> urls ;
	public DownloadUrls() {
		// TODO 自动生成的构造函数存根
		urls = new ArrayList<DownloadInfo>();
	}
	public void addURL(DownloadInfo info) {
		urls.add(info);
	}
	public String getAvailableURL() {
		//从多个不同的url中选一个
		String url = "";
		for(DownloadInfo i:urls){
			if(i.hasAudio&&i.hasVideo&&i.extension.equals("mp4")){
				url =  i.url;
				break;
			}
		}
		return url;
	}
}
