package decodeJson;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.json.*;



public class DecodeJson {

	public Taxonomy[] readTaxonomy(String filepath) {
		//"src/util/activity_net_taxonomy.json"
		
/*
	{
		parentName: "Playing sports",
      	nodeName: "Discus throw",
      	nodeId: 92,
      	parentId: 64
   	}
*/		
		Taxonomy[] taxonomies = new Taxonomy[400];
		JSONObject subObject = null;
		int nodeid = 0;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(filepath));
	        JSONObject jObject = new JSONObject(reader.readLine());
	        JSONArray jArray  = jObject.getJSONArray("taxonomy");
	        
            for(int i=0;i<jArray.length();i++){
                subObject=jArray.getJSONObject(i);
                nodeid = subObject.getInt("nodeId");
                if(nodeid!=0){
                    taxonomies[nodeid] = new Taxonomy(
								subObject.getString("nodeName"),
								subObject.getInt("parentId"));
                }else{
                    taxonomies[nodeid] = new Taxonomy("Root",0);
                }

                
            }
            reader.close();
		} catch (JSONException | IOException e) {
			
			e.printStackTrace();
			//System.out.println(subObject.toString());
		}
		return taxonomies;
	}
	
	public ArrayList<VideoInfo> readDatabase(String filepath) {
		//"src/util/activity_net_database.json"
		
	/*
	5n7NCViB5TU: {
      annotations: [
         {
            label: "Discus throw",
            segment: [24.25018, 38.08036]
         },
         {
            label: "Discus throw",
            segment: [97.00073, 106.284]
         }
      ],
      duration: 121.44,
      resolution: "320x240",
      subset: "training",
      url: "https://www.youtube.com/watch?v=5n7NCViB5TU"
   	}*/
		ArrayList<VideoInfo> videoInfos = new ArrayList<VideoInfo>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(filepath));
	        JSONObject jObject = new JSONObject(reader.readLine());
	        Iterator<String> iterator = jObject.keys();
	        while(iterator.hasNext()){
	        	
	        	String url =iterator.next();
	        	JSONObject subObject = jObject.getJSONObject(url);
	        	//double duration = subObject.getDouble("druation");
	        	String subset = subObject.getString("subset");
	        	/*
	        	String resolution = subObject.getString("resolution");
	        	int x = Integer.parseInt(resolution.split("x")[0]);
	        	int y = Integer.parseInt(resolution.split("x")[1]);
	        	*/
	        	VideoInfo vi = new VideoInfo(url,subset);
	        	if(!subset.equals("testing")){
	        		JSONArray annotations = subObject.getJSONArray("annotations");
	        		for (int i=0;i<annotations.length();i++){
	        			JSONObject seg = annotations.getJSONObject(i);
	        			JSONArray segment = seg.getJSONArray("segment");
	        			vi.addSeg(segment.getDouble(0), segment.getDouble(1), seg.getString("label"));
	        		}
	        	}
	        	
	        	videoInfos.add(vi);
	        }
	        reader.close();
		} catch (JSONException | IOException e) {
			
			e.printStackTrace();
		}
		return videoInfos;
	}
}
