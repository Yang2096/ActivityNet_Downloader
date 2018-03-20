package decodeJson;

public class Annotation {
	//视频的分割信息
	private double beginTime;
	private double endTime;
	private String lable;
	Annotation(double begin,double end,String l) {

		this.beginTime = begin; 
		this.endTime = end;
		this.lable = l;
		
	}
	Annotation() {
		this.beginTime = 0;
		this.endTime = 0;
		lable = "";
	}
	public double getBeginTime() {
		return beginTime;
	}
	public double getEndTime() {
		return endTime;
	}
	public String getLable() {
		return lable;
	}
	
}
