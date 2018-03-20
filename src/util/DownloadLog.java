package util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class DownloadLog {
	//记录下载成功了的和失败的视频ID
	private static ArrayList<String> isDownloaded = new ArrayList<String>();
	private static ArrayList<String> failed2download = new ArrayList<String>();
	
	public static boolean isDownload(String videoId) {
		return isDownloaded.contains(videoId);
	}
	public static void save2File(String Root,String filename,boolean cover) {
		String outputFile = Root + File.separator + filename;
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(outputFile));

			if(cover){
				for(String i : failed2download){
					out.write(i+"\n");
				}
			}else{
				for(String i : isDownloaded){
					out.write(i+"\n");
				}
			}
			out.close();
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		
	}
	public static void saveLog(String Root) {
		save2File(Root, "downloaded.log", false);
		save2File(Root, "failed.log", true);
	}
	public static void load(String root) {
		File inputFile = new File(root,"downloaded.log");
		if(inputFile.exists()&&inputFile.isFile()){
			try {
				InputStream inputStream =new FileInputStream(inputFile );
		        BufferedReader stdout = new BufferedReader(new InputStreamReader(inputStream));  
			        String line;  
			        while ((line = stdout.readLine()) != null) {  
			        	isDownloaded.add(line);
			        }  
			    stdout.close();
			} catch (IOException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
		}else{
			try {
				inputFile.createNewFile();
			} catch (IOException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
		}

	}
	public static void check(String videoId) {
		if(!isDownload(videoId)){
			isDownloaded.add(videoId);
			System.out.print(videoId);
		}
	}
	public static void log(String videoId) {
		failed2download.add(videoId);
	}
	
	public static int count() {
		return isDownloaded.size();
	}

}
