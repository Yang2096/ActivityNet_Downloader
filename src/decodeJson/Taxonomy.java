package decodeJson;

import java.util.HashMap;

import util.CreatePath;
//树形目录
public class Taxonomy {
	public String name;
	public int parentID;
	//用名字找到父节点编号,之所以用名字是因为database里的视频分类只有lable即分类名字,而没有节点编号.
	//结合Taxonomy[] taxonomies得到父节点名字
	public static HashMap<String, Integer> parent = new HashMap<String, Integer>();
	
	public Taxonomy(String name, int parentID) {
		//对于json文件中的所有不合法字符在存储时就将其替换掉.
		this.name = CreatePath.replaceIllegalChar(name);
		this.parentID = parentID;
		parent.put(this.name, parentID);
	}
	public Taxonomy() {
		
	}
	public static int getParentID(String nodeName) {
		return parent.get(nodeName);
	}

}
