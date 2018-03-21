package util;

import java.io.File;

import decodeJson.Taxonomy;
import jdk.internal.org.objectweb.asm.tree.IntInsnNode;

public class CreatePath {
	public Taxonomy[] taxonomies;
	private static final String TEST_TEMP = "testing temp";
	private static final String VALID_TRAIN = "validation training";
	public static final char[] ILLEGAL_FILENAME_CHARACTERS = { '/', '`', '?', '*', '\\', '<', '>', '|', '\"', ':'};
	public CreatePath(Taxonomy[] taxonomies) {
		super();
		this.taxonomies = taxonomies;
	}
	//根据Taxonomy创建下载目录  
	public void pathCreating(String root) {
		String destDir = "";
		
		//若目录存在则不再重复创建,若有其他目录创建失败则需删除temp重启程序.
		destDir = root + File.separator + "temp";
		File preDir = new File(destDir);
		if(preDir.exists()){
			return ;
		}
		
		//创建训练集目录和验证集目录
		for(int i=0;i<taxonomies.length;i++){
			if(taxonomies[i]==null){
				continue;
			}
			for(String twoPath:VALID_TRAIN.split(" ") ){
				destDir = root + File.separator + twoPath + File.separator + downloadPath(taxonomies[i].name);
				File dir = new File(destDir);  
				if (!dir.exists()) {  
					if(!dir.mkdirs()){
						System.err.println("Making dir "+destDir+" error.");
					}
				}
			}
		}
		
		//创建测试集(没有label)目录和临时文件目录
		for(String twoPath:TEST_TEMP.split(" ")){
			destDir = root + File.separator + twoPath;
			preDir = new File(destDir);
			if (!preDir.exists()) {  
				if(!preDir.mkdirs()){
					System.err.println("Making dir "+destDir+" error.");
				}
			}
		}
	}
	
	
	//输入label 得到其所属的路径(不包含root和subset路径,以File.separator结尾)
	public String downloadPath(String label) {
		String path = "";
		label = replaceIllegalChar(label);
		//只有root的parent.name等于自身name
		while(!taxonomies[Taxonomy.getParentID(label)].name.equals(label)){
			//从下至上还原所属路径
			path = label + File.separator + path;
			label = taxonomies[Taxonomy.getParentID(label)].name;
		}
		return path;
	}
	
	public static String replaceIllegalChar(String name) {
		for (char c : ILLEGAL_FILENAME_CHARACTERS) {
			   name = name.replace(c, '_');
		  }
		while(name.endsWith(" ")){
			name = name.substring(0, name.length()-2);
		}
		return name;
	}
}
