import species.Resource;
import groovy.sql.Sql;

import java.util.Date;
import species.utils.ImageUtils;
import java.util.*;

def _doCrop(resourceList, relativePath){
	println "===============" + resourceList.size()
	HashMap hm = new HashMap();
	resourceList.each { res->
		println "------------------------------------------------------------------"
		//String fileName = res.fileName;
		String fileName = relativePath + "/" + res.fileName;
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
		try{
			ImageUtils.doResize(file, outImg, 200, 200);
		}catch (Exception e) {
			hm.put(fileName.getAbsolutePath(), res + "  " + e.getMessage());
		}
		System.out.println("====================Error ==================" + hm.size() );
		System.out.println(hm);
		System.out.println("==================== End Error ==================");
	}
}

def getResoruceId(query, sql){
	def result = new HashSet()
	sql.rows(query).each{
		def res = Resource.read(it.getProperty("id"));
		if(res.type == Resource.ResourceType.IMAGE){
			result << res
		}
	}
	return result
}


def doCrop(){
	// Instantiate a Date object
	Date startDate = new Date();
	// display time and date using toString()
	System.out.println(startDate.toString());

	def dataSoruce = ctx.getBean("dataSource");
	def grailsApplication = ctx.getBean("grailsApplication");

	def sql =  Sql.newInstance(dataSoruce);

	//gettting all resource for species
	//	def query = "select distinct(resource_id) as id from species_resource";
	//	def result = getResoruceId(query, sql)
	//	query = "select distinct(resource_id) as id from species_field_resources";
	//	result.addAll(getResoruceId(query, sql))
	//	_doCrop(result, grailsApplication.config.speciesPortal.resources.rootDir)


	query = "select distinct(resource_id) as id from observation_resource";
	result = getResoruceId(query, sql)
	_doCrop(result, grailsApplication.config.speciesPortal.observations.rootDir)

	println "============= Start  Time " + startDate  + "          end time " + new Date()

}

doCrop();
println "=========== DONE!!";
