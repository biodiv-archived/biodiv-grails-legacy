import species.Resource;
import java.util.Date;
import species.utils.ImageUtils;
import java.util.*;

def doCrop(){
		// Instantiate a Date object
       	Date startDate = new Date();
        
       // display time and date using toString()
       System.out.println(startDate.toString());
	Resource.list(type:Resource.ResourceType.IMAGE).each { res->
		println "------------------------------------------------------------------"
		//String fileName = res.fileName;
		String fileName = res.absPath();
		File file = new File(fileName);
		
		String name = file.getName();
		String parent = file.getParent();
		//println name;
		//println parent;
		String inName = name;
		int lastIndex = name.lastIndexOf('.');
		if(lastIndex != -1) {
            inName = name.substring(0, lastIndex);
        }
        
		String outName = inName + "_th1.jpg"
		//println inName;
		//println outName;
		println file;
		File dir = new File(parent);
		File outImg = new File(dir,outName);
		//println "out file full path";
		println outImg;
		
		ImageUtils.doResize(file, outImg, 200, 200);
		
	}
		// Instantiate a Date object
       	Date endDate = new Date();
        
       // display time and date using toString()
       System.out.println(endDate.toString());
	
}
doCrop();
println "=========== DONE!!";
