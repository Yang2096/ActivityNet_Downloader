package download;

public class DownloadInfo {
	public String extension = null;
	public String url = null;
	public boolean hasAudio = false;
	public boolean hasVideo = false;

	public boolean isValid(){
		return  extension!=null && url != null;
	}
}
