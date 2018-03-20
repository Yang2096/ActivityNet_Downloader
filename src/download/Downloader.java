package download;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import decodeJson.VideoInfo;
import util.*;

public class Downloader implements Runnable{

	private static String userAgent = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.75 Safari/537.36 Firefox/3.6.13";
	private CloseableHttpClient client;
	private RequestConfig requestConfig;
	private ArrayList<VideoInfo>  videoInfos;
	private static int iterator = 0;
	public static Boolean VIDEO_CUT = false;
	private CreatePath path;
	private String Root ;
	private int num;
	
	public Downloader(ArrayList<VideoInfo> videoInfos,String root,Integer num,CreatePath path) {
		// TODO 自动生成的构造函数存根
		this.videoInfos = videoInfos;
		this.Root = root;
		this.num = num;
		this.path = path;
		int timeoutInMs = 10 * 1000; // Timeout in millis.
		requestConfig = RequestConfig.custom()
			    .setConnectionRequestTimeout(timeoutInMs)
			    .setConnectTimeout(timeoutInMs)
			    .setSocketTimeout(timeoutInMs)
			    .build();
		client = HttpClientBuilder.create().build();
	}
	/**
	 * 根据videoID获得视频实际存储所在的url
	 * @param videoID 视频ID
	 * @return 视频实际的url
	 * @throws Throwable
	 */
	public String getDownloadURL(String videoID) throws Throwable {
		
		URI uri = new URI("http://www.youtube.com/get_video_info?video_id=" + videoID);
		//http://youtube.com/get_video_info?video_id=10uSu442cOE
		
		HttpGet httpget = new HttpGet(uri);
		httpget.setHeader("User-Agent", userAgent);
		httpget.setConfig(requestConfig);

		HttpResponse response = client.execute(httpget);
		HttpEntity entity = response.getEntity();

		if (entity != null && response.getStatusLine().getStatusCode() == 200) {
			DownloadUrls urls = new DownloadUrls();
			StringBuilder builder = new StringBuilder();
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(entity.getContent()));
			char buffer[] = new char[1024];
			int count = 0;
				
			while ((count = bufferedReader.read(buffer)) != -1){
				builder.append(new String(buffer, 0, count));
			}
			String decodedResponse = builder.toString();
			
			ArrayList<Pair> decodedPairs = responseToPairs(decodedResponse, "&", "=");
			String downloadUrl = null;
			for (Pair i : decodedPairs){
				if (i.key.equals("url_encoded_fmt_stream_map") || i.key.equals("adaptive_fmts")){
					String fmts[] = i.value.split(",");
					for (String fmt : fmts){
						//一个video对应多个url
						urls.addURL(parseDownloadType(responseToPairs(fmt, "&", "=")));
					}
				}else if(i.key.equals("status")){
					if(i.value.equals("fail")){
						System.err.println(videoID+" :get video infomation failed.");
						//TODO
						return null;
					}
				}
			}
			downloadUrl = urls.getAvailableURL();
			if (downloadUrl.length()>9) {
				return downloadUrl;
			}else{
				System.err.println(videoID+" :No available url.");
			}
		}else{
			System.err.println(videoID+" :Could not complete download. Code: " + response.getStatusLine().getStatusCode());
		}
		return null;
	}
	/**
	 * 使用HttpClient下载视频
	 * @param downloadUrl  视频链接
	 * @param outputfile 输出文件目录
	 * @return 是否下载成功
	 * @throws Throwable
	 */
	public boolean downloadWithHttpClient(String downloadUrl, File outputfile) throws Throwable {
		HttpGet request = new HttpGet(downloadUrl);
		request.setHeader("User-Agent", userAgent);

		HttpResponse response = client.execute(request);
		HttpEntity entity = response.getEntity();
		if (entity != null && response.getStatusLine().getStatusCode() == 200) {
			long length = entity.getContentLength();
			InputStream instream = entity.getContent();
			//System.out.println("Writing " + length + " bytes to " + outputfile);
			
			if (outputfile.exists()) {
				outputfile.delete();
			}
			
			FileOutputStream outstream = new FileOutputStream(outputfile);

			byte[] buffer = new byte[2048];
			int count = 0;
			while ((count = instream.read(buffer)) != -1) {
				outstream.write(buffer, 0, count);
			}
			outstream.flush();
			FileDescriptor fd = outstream.getFD();  
			fd.sync();  
			outstream.close();
			return true;
		}else{
			return false;
		}
	}

	/**
	 * 根据http response得到视频的信息对
	 * @param input http response
	 * @param pairDelim 各对信息对之间的分隔符
	 * @param keyValueDelim 信息对内部的分隔符
	 * @return
	 */
	private ArrayList<Pair> responseToPairs(String input, String pairDelim, String keyValueDelim) {
		ArrayList<Pair> params = new ArrayList<Pair>();

		String kv[] = null;
		try {
			String pairs[] = input.split(pairDelim);
			Pair pair = null;	
			
			for (String i : pairs) {
				kv = i.split(keyValueDelim);
				if (kv.length < 2){
					continue;
				}
				
				pair = new Pair(kv[0], URLDecoder.decode(kv[1], "UTF-8"));
				if (kv[1].indexOf('=') != -1){
					String innerPair[] = pair.value.substring(pair.value.indexOf(",")).split("=");
					if (innerPair.length == 2){
						params.add(new Pair(innerPair[0], URLDecoder.decode(innerPair[1], "UTF-8")));
					}
				}
				params.add(pair);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return params;
	}
	/**
	 * 根据信息对获取视频信息
	 * @param params 信息对
	 * @return DownloadInfo
	 */
	private DownloadInfo parseDownloadType(ArrayList<Pair> params){
		try {
			DownloadInfo type = new DownloadInfo();
			for (Pair i : params){
				if (i.key.equals("url")){
					type.url = i.value;
				}
				else if (i.key.equals("type")){
						String typeCodec[] = i.value.split(";");
						if (typeCodec[0].startsWith("video")){
							type.hasVideo = true;
							String codecs[] = typeCodec[1].substring(typeCodec[1].indexOf("=") + 2, typeCodec[1].length() - 1).split(",");
							if (codecs.length > 1){
								type.hasAudio = true; //most likely has audio
							}
						}
						else if (typeCodec[0].startsWith("audio")){
							type.hasAudio = true;
						}
						type.extension = i.value.substring(i.value.indexOf("/") + 1, i.value.indexOf(";") );
				}
			}
			return type;
		}
		catch (Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public void run() {
		String downloadUrl = "";
		String videoID = "";
		String outputdir = "";
		VideoInfo videoInfo = null;
		CutVideo cVideo=new CutVideo(path,Root);
		while(true){
			//阻塞获取每个视频ID
			synchronized (this) {
				if(iterator<videoInfos.size()){ 
					videoInfo = videoInfos.get(iterator);
					iterator++;
				}else 
				return ;
			}
			videoID = videoInfo.url;
			if(!DownloadLog.isDownload(videoID)){
				System.out.println(videoID+" Handled by Thread "+num);
			}
			if(DownloadLog.isDownload(videoID)){
				//System.out.println(videoID+" :video has been downloaded before.");
				continue;
			}
			if(videoInfo.subset.equals("testing")){
				outputdir = Root + File.separator + "testing";
			}else{
				if(VIDEO_CUT == true){
					//traning 和 validation 放到temp文件夹  等待后续分割
					outputdir = Root + File.separator + "temp";
				}else{
					//若不进行剪切则直接放入所属文件夹,但是只能放入第一个分割所属的lable下.若有多个segment且不属于同一lable则会有分类缺失
					outputdir = Root + File.separator + videoInfo.subset  
									 + File.separator + path.downloadPath(videoInfo.annotations.get(0).getLable());
				}

			}
			File outputfile = new File(outputdir, videoID+".mp4");
			
			
			if(!outputfile.exists() || !DownloadLog.isDownload(videoID)){
				try {
					downloadUrl = this.getDownloadURL(videoID);
				} catch (Throwable e) {
					DownloadLog.log(videoID);
					System.err.println(videoID+" : get video download url failed.");
					continue;
				}
			
				try {
					if(this.downloadWithHttpClient(downloadUrl, outputfile)){
						//System.out.println(videoID+" downloaded");
						if(VIDEO_CUT == true){
							cVideo.videoCut(outputfile.toString(), videoInfo); //下载完成后进行剪切
						}
						
						DownloadLog.check(videoInfo.url);
						System.out.println(" done. "+DownloadLog.count()+" / "+videoInfos.size());
					}else{
						System.err.println(videoID+" download failed");
					}
				} catch (Throwable  e) {
					DownloadLog.log(videoID);//下载失败
					e.printStackTrace();
					System.err.println(videoID+" : error in downloading video .");
					continue;
				}

			}else{
				//System.out.println("File "+outputfile+" exists.");
				if(VIDEO_CUT == true){
					cVideo.videoCut(outputfile.toString(), videoInfo); //下载完成后进行剪切
				}
				DownloadLog.check(videoID);
				System.out.println(" done."+DownloadLog.count()+" / "+videoInfos.size());
			}

		}

	}

}
