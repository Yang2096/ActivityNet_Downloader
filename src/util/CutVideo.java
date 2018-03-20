package util;

import java.io.File;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.videoio.*;

import decodeJson.Annotation;
import decodeJson.VideoInfo;

public class CutVideo {
	//根据Annotations中的各个分段切割视频
	private CreatePath path;
	private String root;
	public CutVideo(CreatePath path,String Root) {
		super();
		this.path = path;
		this.root = Root;
	}
	
/*	public void exec(List<String> cmd){  
		try {  
			ProcessBuilder builder = new ProcessBuilder();    
	        builder.command(cmd);  
	        builder.redirectErrorStream(true);  
	        Process proc = builder.start();  
	        BufferedReader stdout = new BufferedReader(  
	        	new InputStreamReader(proc.getInputStream()));  
	        String line;  
	        while ((line = stdout.readLine()) != null) {  
	        	//System.out.println(line);
	        }  
	        proc.waitFor();     
	        stdout.close();  
	    } catch (Exception e) {  
	        e.printStackTrace();  
	    }  
	}  
	//使用ffmpeg进行剪切,但是剪切精度会很低(前后差好几秒).
	public void videoCutwithFFMPEG(String file, VideoInfo videoInfo) {
		List<String> cmd = new ArrayList<String>();
		if(videoInfo.subset.equals("testing"))
			return ;
		Annotation annotation = null;
		for(int i =0;i<videoInfo.annotations.size();i++){
		
			annotation = videoInfo.annotations.get(i);
			cmd.clear();
			cmd.add("ffmpeg");
			cmd.add("-ss");
			cmd.add(annotation.beginTime);
			cmd.add("-i");
			cmd.add(file);
			cmd.add("-t");
			cmd.add(annotation.duration);
			cmd.add("-vcodec");
			cmd.add("copy");
			cmd.add("-acodec");
			cmd.add("copy");
			cmd.add("-write_xing");
			cmd.add("0");
			String outputFile = "";
			//根据lable得到输出文件路径
			outputFile = root + File.separator + videoInfo.subset;
			outputFile += File.separator + path.downloadPath(annotation.lable);
			outputFile += videoInfo.url + "_" + Integer.toString(i) + ".mp4";
			cmd.add(outputFile);//输出文件
			cmd.add("-y");
			exec(cmd);
		}
		File tempFile = new File(file);
		tempFile.delete();
	}*/
	//
	/**
	 * 使用OpenCV剪切视频文件,转换为帧内编码,文件将变大许多(约为3.6倍)
	 * OpenCV每剪切一个视频还会报两行warning,但是对输出文件没什么影响.
	 * @param file 待剪切文件的目录
	 * @param videoInfo 该文件的信息
	 */
	public void videoCut(String file, VideoInfo videoInfo) {
		if(videoInfo.subset.equals("testing"))
			return ;
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		VideoCapture videoCapture = new VideoCapture(file);
		if(!videoCapture.isOpened()){
			//TODO
			System.err.println("open video failed");
			return ;
		}
		
		double fps = videoCapture.get(Videoio.CV_CAP_PROP_FPS);
		Mat frame = new Mat();
		String output = null;
		Annotation annotation = null;
		
		for(int index = 0; index < videoInfo.annotations.size() ; index++){
			
			annotation = videoInfo.annotations.get(index);
			//输出多个文件,在原文件名后加入标号
			output = root + File.separator + videoInfo.subset;
			output += File.separator + path.downloadPath(annotation.getLable());
			output += videoInfo.url + "_" + Integer.toString(index) + ".mp4";
			//通过时间得到帧数
			int beginFrame = (int) (annotation.getBeginTime()*fps);
			int endFrame = (int) (annotation.getEndTime()*fps);
			
			videoCapture.set(Videoio.CV_CAP_PROP_POS_FRAMES, beginFrame);
			videoCapture.read(frame);
			VideoWriter output_video = new VideoWriter(output,
		    		VideoWriter.fourcc('D','I','V','X'),
		    		fps,
		    		frame.size(),
		    		true);
			
		    while(beginFrame<=endFrame){
		    	if(!videoCapture.read(frame)){
		    		System.err.println("read frame failed");
		    		break;
		    	}
		    	output_video.write(frame);
		    	beginFrame++;
		    }
		    output_video.release();
		}
		videoCapture.release();
		//删除临时文件
		File tempFile = new File(file);
		tempFile.delete();
	}
} 

