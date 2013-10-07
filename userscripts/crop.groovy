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

	println "==============Resource List Size==================" + resourceList.size()
	//HashMap hm = new HashMap();
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
			println "==============RahulImageException===== " + e.getMessage()
			//hm.put(file , e.getMessage());
		}
	}
	//println "===================ERROR COUNT============= " + hm.size()
	//println hm
	//println "====================ERROR END========================"
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
	Date startDate = new Date();

	def dataSoruce = ctx.getBean("dataSource");
	def grailsApplication = ctx.getBean("grailsApplication");

	def sql =  Sql.newInstance(dataSoruce);
	def query, result
/*
	//gettting all resource for species
	query = "select distinct(resource_id) as id from species_resource order by resource_id";
	result = getResoruceId(query, sql)
	query = "select distinct(resource_id) as id from species_field_resources order by resource_id";
	result.addAll(getResoruceId(query, sql))
	_doCrop(result, grailsApplication.config.speciesPortal.resources.rootDir)

	println "----------------DONE SPECIES-------------------------------------------------- "

	query = "select distinct(resource_id) as id from observation_resource  where resource_id > 291108 order by resource_id ";
	result = getResoruceId(query, sql)
	_doCrop(result, grailsApplication.config.speciesPortal.observations.rootDir)

	println "----------------DONE OBSERVATION-------------------------------------------------- "
	result = geUserResoruceId()
	_doCrop(result, grailsApplication.config.speciesPortal.users.rootDir)
	println "----------------DONE USERS-------------------------------------------------- "

	
	result = Resource.findAllByType(Resource.ResourceType.ICON)
	_doCrop(result, grailsApplication.config.speciesPortal.resources.rootDir)
	println "----------------DONE ICONS-------------------------------------------------- " + result.size()
*/
	
	//PNG format
	//species
	query = "select resource_id from species_resource where resource_id in (select id from resource where file_name like '%png')";
	result = getResoruceId(query, sql)
	query = "select resource_id from species_field_resources where resource_id in (select id from resource where file_name like '%png')";
	result.addAll(getResoruceId(query, sql))
	query = "select repr_image_id as id from species where repr_image_id is not null and repr_image_id not in (select resource_id from species_resource) and repr_image_id not in (select resource_id from species_field_resources)"
	result.addAll(getResoruceId(query, sql))
	_doCrop(result, grailsApplication.config.speciesPortal.resources.rootDir)
	
	//observation
	query = "select r.id from resource as r , observation_resource as obr  where file_name like '%png'  and r.id = obr.resource_id";
	result = getResoruceId(query, sql)
	_doCrop(result, grailsApplication.config.speciesPortal.observations.rootDir)
	
	println "============= Start  Time " + startDate  + "          end time " + new Date()
}

//doCrop();

ImageUtils.createScaledImages(new File('/tmp/142.jpg'), new File('/tmp') )
println "=========== DONE!!";

/*
 
for unsupported exception
select s.id, s.repr_image_id, r.file_name  from species as s, resource as r where s.repr_image_id = r.id and s.repr_image_id is not null and s.repr_image_id not in (select resource_id from species_resource) and repr_image_id not in (select resource_id from species_field_resources);

for png file
select id, file_name  from resource where file_name like '%png';


*/
