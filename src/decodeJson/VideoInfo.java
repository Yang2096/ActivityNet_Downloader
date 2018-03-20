package decodeJson;

import java.util.ArrayList;

public class VideoInfo {
	public String url;
	public String subset;
	public ArrayList<Annotation> annotations;
	
	public VideoInfo(String url, String subset) {
		this.url = url;
		this.subset = subset;
		if(!subset.equals("testing")){
			annotations = new ArrayList<Annotation>();
		}
	
	}
	public VideoInfo() {
		
	}
	public void addSeg(double begin, double end,String lable ) {
		annotations.add(new Annotation(begin,end,lable));
	}
	
}
