package download;


import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.*;
import org.apache.commons.cli.*;
import decodeJson.DecodeJson;
import decodeJson.VideoInfo;
import util.CreatePath;
import util.DownloadLog;

public class Main extends Thread{
	
	private static String root = "/home/mcislab3d/ucf_idf/activity_net.v1-3";	//linux
	private static String databaseFile = "src/util/activity_net_database.json";
	private static int threadNum = 3;
    static String strClassName = Main.class.getName();  
    static Logger logger = Logger.getLogger(strClassName);  
    static LogManager logManager = LogManager.getLogManager(); 
    
	public Main() {
		
	}
	public void setHook() {
		Runtime.getRuntime().addShutdownHook(this);
	}
	public static void usage(String[] args)  {
  	  	Options options = new Options( );  
  	  	options.addOption("h", "help", false, "帮助信息");  
  	  	options.addOption("c","enable_video_cut", false, "开启视频剪切(使用OpenCV,剪切后文件会大很多)" );  
  	  	Option c = Option.builder("r") 
  	  			.longOpt("root")
  	  			.required(false)  
  	  			.hasArg() 
  	  			.argName("Path") 
  	  			.desc("设置下载根目录,无须以/或\\结尾.默认目录: "+root) 
  	  			.build(); 
  	  	options.addOption(c);
  	  	
  	  	c = Option.builder("t") 
  	  			.longOpt("test")
  	  			.required(false)  
  	  			.desc("只下载少量视频,验证程序可行性") 
  	  			.build(); 
  	  	options.addOption(c);
  	  	
  	  	c = Option.builder("tn") 
  	  			.longOpt("thread_number")
  	  			.required(false)  
  	  			.hasArg() 
  	  			.argName("number") 
  	  			.desc("设置下载线程数,默认: 3") 
  	  			.build(); 
  	  	options.addOption(c);
  	  	
  	  	CommandLineParser parser = new DefaultParser();
  	  	CommandLine cmd;
		try {
			cmd = parser.parse(options,args);
			
	  	  	if(cmd.hasOption("h")) { //调用默认的help函数打印
	  	  		HelpFormatter formatter = new HelpFormatter(); 
	  	  		formatter.printHelp( "java downloader [options]", options ); 
	  	  		System.exit(0);
	  	  	}
	  	  	
	  	  	if(cmd.hasOption("r")){
	  	  		String path = cmd.getOptionValue("root");
	  	  		System.out.print("确认设置下载根目录为: "+path+" ? y/n");
	  	  		Scanner scanner = new Scanner(System.in);
	  	  		String ans = scanner.next();
	  	  		if(ans.toLowerCase().startsWith("y")){
	  	  			root = path;
	  	  			System.out.println("设置成功.");
	  	  			logger.config("用户设置下载根目录至: "+root);
	  	  		}else{
	  	  			System.exit(0);
	  	  		}
	  	  		scanner.close();
	  	  	}
	  	  	
	  	  	if(cmd.hasOption("c")){
	  	  		Downloader.VIDEO_CUT = true;
	  	  		System.out.println("已开启视频剪切,注意磁盘容量");
	  	  	}
	  	  	if(cmd.hasOption("t")){
	  	  		databaseFile = "src/util/activity_net_database_test.json";
	  	  	}
	  	  	if(cmd.hasOption("tn")){
	  	  		threadNum = Integer.parseInt(cmd.getOptionValue("tn"));
	  	  		if(threadNum>15||threadNum<1){
	  	  			System.err.println("线程数目设置有误.");
	  	  			System.exit(0);
	  	  		}
	  	  	}
	  	  	
		} catch (ParseException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
			System.err.println("参数错误,使用-h查看帮助信息");
			System.exit(0);
		}
  	  	

	}
	
	public static void main(String[] args){
		String os = System.getProperty("os.name");  
		if(os.toLowerCase().startsWith("win")){
			root = "D:\\Downloads\\activity_net.v1-3";//windows
		}
		usage(args);
		Main main1 = new Main();
		main1.setHook();
		Properties prop = System.getProperties();
		// 设置http访问要使用的代理服务器的地址
		prop.setProperty("http.proxyHost", "127.0.0.1");
		// 设置http访问要使用的代理服务器的端口
		prop.setProperty("http.proxyPort", "1080");
		DecodeJson decodeJson = new DecodeJson();
		CreatePath path = new CreatePath(decodeJson.readTaxonomy("src/util/activity_net_taxonomy.json"));
		path.pathCreating(root);
		ArrayList<VideoInfo> videoInfos = decodeJson.readDatabase(databaseFile);
		System.out.println("Total videos: "+Integer.toString(videoInfos.size()));
		DownloadLog.load(root);
		
		Thread[] downloader = new Thread[threadNum];
	

		try {
			for(int j=0;j<threadNum;j++){	
				downloader[j] = new Thread (new Downloader(videoInfos,root,j,path));
				Thread.sleep(500);
				downloader[j].start();
			}
			for(int j=0;j<threadNum;j++){
				downloader[j].join();
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		DownloadLog.saveLog(root);
		
	}

	@Override
	public void run() {
		// TODO 自动生成的方法存根
		DownloadLog.saveLog(root);
		System.out.println("已下载视频ID保存完毕.");
	}

}
