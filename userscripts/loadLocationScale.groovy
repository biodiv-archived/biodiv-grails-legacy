import java.util.Date;
import java.util.List;
import java.lang.Float;

import species.participation.Checklists;
import species.auth.SUser;
import species.Metadata.LocationScale
import groovy.sql.Sql;



def test(){
	def ds = ctx.getBean("dataSource")
	def sql =  Sql.newInstance(ds);
	new File("/home/sandeept/checklists.csv").splitEachLine(",") {
		//SUser.withNewTransaction {
			
		def fields = it;
		def id = fields[0].trim().toLong()
		def cl = Checklists.get(id)
		if(cl){
			def lc = LocationScale.getEnum(fields[1].trim()) 
			println "=== saving  checklist location for " + cl + "  locatoin " + lc
			
			sql.executeUpdate('update observation set location_scale = ? where id = ? ', [lc.value().toUpperCase(), id]);
			sql.executeUpdate('update observation set location_scale = ? where is_checklist = false and id != source_id and source_id = ?', [lc.value().toUpperCase(), id]);
		
		}
		}
	//}
	//SUser.withNewTransaction {
		
	sql.executeUpdate('update observation set location_scale = ? where location_accuracy = ? and is_checklist = false and id = source_id', ['ACCURATE', 'Accurate']);
	sql.executeUpdate('update observation set location_scale = ? where location_accuracy = ? and is_checklist = false and id = source_id', ['APPROXIMATE', 'Approximate']);
	sql.executeUpdate('update observation set location_scale = ? where location_accuracy is null and is_checklist = false and id = source_id', ['APPROXIMATE']);
	//}
}	

test()
//XXX : what happen to delete obv and checklists  location_scale
// some of them have null value in location_accuracy column what will be defalut value in this case
// drop location accuracy column after change from create/edit and download ...



