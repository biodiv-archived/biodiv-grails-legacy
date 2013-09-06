import species.Resource;
import species.auth.SUser;
import groovy.sql.Sql;

import java.util.Date;
import species.utils.ImageUtils;
import java.util.*;

def geUserResoruceId(){
              def result = []
              SUser.findAllByIconIsNotNull().each { user ->
                      result << [id:user.id, fileName:user.icon]
              }
              return result
      }

def _doCrop(resourceList, relativePath){
<<<<<<< HEAD
	println "=======================================" + resourceList.size()
	HashMap hm = new HashMap();
=======
	println "=============== " + resourceList.size()
>>>>>>> 6ce35dc693da3223b1934c53e94e5cd14f10f620
	resourceList.each { res->
		println "------------------------------------------------------------------ " + res.id
		String fileName = relativePath + "/" + res.fileName;
		File file = new File(fileName);

		String name = file.getName();
		String parent = file.getParent();
		String inName = name;
		int lastIndex = name.lastIndexOf('.');
		if(lastIndex != -1) {
			inName = name.substring(0, lastIndex);
		}

		String outName = inName + "_th1.jpg"
		println file;
		File dir = new File(parent);
		File outImg = new File(dir,outName);
		println outImg;
		try{
			ImageUtils.doResize(file, outImg, 200, 200);
		}catch (Exception e) {
			println "==========================ee=== " + e.getMessage()
		}
	}
}

def getResoruceId(query, sql){
	def result = []
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
	//System.out.println(startDate.toString());

	def dataSoruce = ctx.getBean("dataSource");
	def grailsApplication = ctx.getBean("grailsApplication");

	def sql =  Sql.newInstance(dataSoruce);

	gettting all resource for species
	def query = "select distinct(resource_id) as id from species_resource order by resource_id";
	def result = getResoruceId(query, sql)
	query = "select distinct(resource_id) as id from species_field_resources order by resource_id";
	result.addAll(getResoruceId(query, sql))
	_doCrop(result, grailsApplication.config.speciesPortal.resources.rootDir)

<<<<<<< HEAD

	//query = "select distinct(resource_id) as id from observation_resource";
	//result = getResoruceId(query, sql)
	//_doCrop(result, grailsApplication.config.speciesPortal.observations.rootDir)

	result = geUserResoruceId()
	_doCrop(result, grailsApplication.config.speciesPortal.users.rootDir)
=======
	query = "select distinct(resource_id) as id from observation_resource order by resource_id";
	result = getResoruceId(query, sql)
	_doCrop(result, grailsApplication.config.speciesPortal.observations.rootDir)
>>>>>>> 6ce35dc693da3223b1934c53e94e5cd14f10f620

	println "============= Start  Time " + startDate  + "          end time " + new Date()
}

doCrop();
println "=========== DONE!!";
