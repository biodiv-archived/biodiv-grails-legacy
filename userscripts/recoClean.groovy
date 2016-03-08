import species.namelist.Utils;
import species.sourcehandler.MappedSpreadsheetConverter;
import species.namelist.NameInfo
import species.participation.*;
import species.auth.*;
import species.*;
import species.participation.SpeciesBulkUpload.Status
import groovy.sql.Sql


private List searchReco(Recommendation reco, isSyn ){
	def c = Recommendation.createCriteria();
	def recoList = c.list {
		ne('id', reco.id)
		
		eq('lowercaseName', reco.lowercaseName);
			
		eq('isScientificName', reco.isScientificName);
		
		(reco.languageId) ? eq('languageId', reco.languageId) : isNull('languageId');
		
		
//		if(reco.taxonConcept)
//			eq('taxonConcept', reco.taxonConcept)
//		
//		if(isSyn){
//			isNotNull('taxonConcept')
//			neProperty("taxonConcept", "acceptedName")
//		}
	}
	
	return recoList	
}

def accQuery = '''
		select distinct(recommendation_id) as id from recommendation_vote where recommendation_id  in (select r.id from recommendation as r, taxonomy_definition as t  where r.taxon_concept_id = r.accepted_name_id and r.taxon_concept_id is not null and r.taxon_concept_id = t.id and t.lowercase_match_name != r.lowercase_name and r.is_scientific_name = true order by r.lowercase_name) ;
	'''

	
def synQuery = '''

	select distinct(recommendation_id) as id from recommendation_vote where recommendation_id  in (select r.id from recommendation as r, taxonomy_definition as t  where r.taxon_concept_id != r.accepted_name_id and r.taxon_concept_id is not null and r.accepted_name_id is not null  and r.taxon_concept_id = t.id and t.lowercase_match_name != r.lowercase_name and r.is_scientific_name = true order by r.lowercase_name);

'''
def recoSearch(query, isSyn, f){
	def ds = ctx.getBean("dataSource");
	ds.setUnreturnedConnectionTimeout(500);
	Sql conn = new Sql(ds);
	def idList = conn.rows(query);
	
	int zeroMatch = 0 
	int count = 0
	int totalCount = 0
	List rowList = [['source id', 'source name', 'target id', 'taraget name', 'status']]
	idList.each {
		def id = it.id
		def rr = Recommendation.read(id)
		def rList = searchReco(rr, isSyn)
		
		if(rList.isEmpty()){
			zeroMatch++ 
		}
		if(rList.size() == 1){
			count++
			//println "old reco " + id + " new reco " + rList[0].id
		}
		
		if(rList.size() > 1){
			def s = []
			rList.each { r->
				def t = [rr.id, rr.name]
				
				def taxon = r.taxonConcept
				t << r.id
				t << ((!taxon) ? r.name :taxon.normalizedForm)
				t << ((!taxon) ? 'No status': taxon.status)
				rowList << t
			}
		}
		
		//conn.executeUpdate("update recommendation set accepted_name_id = " + accId + " where taxon_concept_id = " + id )
		totalCount++	
	}
	
	println " total count " + totalCount + "  single match count " + count + "  zero Match " + zeroMatch
	
	
	f.withWriter { out ->
		rowList.each {
		  out.println it.join("|")
		}
	}
}

def getIdMap(f){
	Map retMap = [:]
	int count = 0
	f.splitEachLine("\\t") {
		if(!it)
			return
		
		count++
		if(count == 1)
			return
		
		if(count%100 == 0)
			println "----------------- count " + count
					
		def fields = it;
		def id = Long.parseLong(fields[0].trim())
		def targetId = fields[2]?.trim()
		targetId = targetId ? Long.parseLong(targetId) : id
		retMap.put(id, targetId)
	}
	
	println retMap
	return retMap
}


def correctReco(query, isSyn, f){
	def ds = ctx.getBean("dataSource");
	ds.setUnreturnedConnectionTimeout(500);
	Sql conn = new Sql(ds);
	def idList = conn.rows(query);
	
	def idMap = getIdMap(f)
	int zeroMatch = 0
	int count = 0
	int totalCount = 0
	int failCount = 0
	idList.each {
		try{
			def id = it.id
			def rr = Recommendation.read(id)
			def rList = searchReco(rr, isSyn)
			
			println "------------------ " + id 
			
			
			if(rList.isEmpty()){
				println "inside zero match"
				zeroMatch++
				conn.executeUpdate("update recommendation set accepted_name_id = NULL, taxon_concept_id = NULL where id = " + id)
			}
			
			if(rList.size() == 1){
				println "inside single match " +  id + " map val " + rList[0].id 
				
				count++
				conn.executeUpdate("update recommendation_vote set recommendation_id = " + rList[0].id + " where recommendation_id = " + id)
				conn.executeUpdate("update observation set max_voted_reco_id = " + rList[0].id + " where max_voted_reco_id = " + id)
			}
			
			if(rList.size() > 1){
				println "inside multiple match " +  id + " map val " + idMap.get(id)
				
				if(id !=  idMap.get(id)){
					conn.executeUpdate("update recommendation_vote set recommendation_id = " + idMap.get(id) + " where recommendation_id = " + id)
					conn.executeUpdate("update observation set max_voted_reco_id = " + idMap.get(id) + " where max_voted_reco_id = " + id)
				}else{
					conn.executeUpdate("update recommendation set accepted_name_id = NULL, taxon_concept_id = NULL where id = " + id)
				}
			}
			
			totalCount++
		}catch(e){
			println e.message
			failCount++
			
		}
	}
	
	println " total count " + totalCount + "  single match count " + count + "  zero Match " + zeroMatch + " fali count" + failCount
	
}


def removeAcceptedDuplicateFromReco(){
	def ds = ctx.getBean("dataSource");
	ds.setUnreturnedConnectionTimeout(500);
	Sql conn = new Sql(ds);
	
	String query = '''
			select name as name, taxon_concept_id as tid, count(*) as c from recommendation where is_scientific_name = true and taxon_concept_id = accepted_name_id group by name, taxon_concept_id having count(*) > 1 order by c desc
		'''
	
	def idList = conn.rows(query);
	
	Map corrMap = [:]
	idList.each { tt ->
			def name = tt.name 
			def tid = tt.tid
			
			println "  processing " + name +  "   " + tid
			
			String q = 'select id as id from recommendation where is_scientific_name = true and taxon_concept_id = accepted_name_id and taxon_concept_id = ' + tid + ' order by last_modified asc';
			def ll = conn.rows(q)
			def tlist = []
			ll.each { yy ->
				tlist << yy.id
			}
			println tlist
			
			corrMap.put(tlist.remove(0), tlist)
	}
	
	println corrMap
	
	corrMap.each { k, v ->
		v.each { oldId ->
			conn.executeUpdate("update recommendation_vote set recommendation_id = " + k + " where recommendation_id = " + oldId)
			conn.executeUpdate("update observation set max_voted_reco_id = " + k + " where max_voted_reco_id = " + oldId)
			conn.executeUpdate("delete from recommendation where id = " + oldId)
		}
 		
	}
	
	
}

removeAcceptedDuplicateFromReco()

//correctReco(accQuery, false, new File("/tmp/acc_reco.csv"))
//correctReco(synQuery, true, new File("/tmp/syn_reco.csv"))

//recoSearch(accQuery, false, new File("/tmp/acc.csv"))
//recoSearch(synQuery, true, new File("/tmp/syn.csv"))

//export GRAILS_OPTS="-Xmx8G -Xms1G -XX:MaxPermSize=1G"
//nohup grails -Dgrails.env=pamba  --stacktrace run-script userscripts/colReport.groovy >> gbif.txt 2>&1

/*
 SQL for reco consistency   

#duplicate recos for accepted name
select r.id, r.name, t.canonical_form, r.taxon_concept_id from recommendation as r, taxonomy_definition as t  where r.taxon_concept_id = r.accepted_name_id and r.taxon_concept_id is not null and r.taxon_concept_id = t.id and t.lowercase_match_name != r.lowercase_name and r.is_scientific_name = true order by r.lowercase_name;

#distinct reco count to be corrected
biodiv=# select count(distinct(recommendation_id)) from recommendation_vote where recommendation_id  in (select r.id from recommendation as r, taxonomy_definition as t  where r.taxon_concept_id = r.accepted_name_id and r.taxon_concept_id is not null and r.taxon_concept_id = t.id and t.lowercase_match_name != r.lowercase_name and r.is_scientific_name = true order by r.lowercase_name) ;

# reco to be pointed to other reco... 
select r1.id, r1.name, t1.canonical_form, r1.taxon_concept_id  from recommendation r1, taxonomy_definition t1 where r1.taxon_concept_id = t1.id and r1.id in (select distinct(recommendation_id) from recommendation_vote where recommendation_id  in (select r.id from recommendation as r, taxonomy_definition as t  where r.taxon_concept_id = r.accepted_name_id and r.taxon_concept_id is not null and r.taxon_concept_id = t.id and t.lowercase_match_name != r.lowercase_name and r.is_scientific_name = true order by r.lowercase_name));


#delete throu sql
ALTER TABLE recommendation disable TRIGGER ALL ;

create table tmp_reco_acc as ((select r.id from recommendation as r, taxonomy_definition as t  where r.taxon_concept_id = r.accepted_name_id and r.taxon_concept_id is not null and r.taxon_concept_id = t.id and t.lowercase_match_name != r.lowercase_name and r.is_scientific_name = true order by r.lowercase_name) except (select distinct(recommendation_id) from recommendation_vote where recommendation_id  in (select r.id from recommendation as r, taxonomy_definition as t  where r.taxon_concept_id = r.accepted_name_id and r.taxon_concept_id is not null and r.taxon_concept_id = t.id and t.lowercase_match_name != r.lowercase_name and r.is_scientific_name = true order by r.lowercase_name))); 

delete from recommendation where id in ( select id from tmp_reco_acc );


ALTER TABLE recommendation ENABLE TRIGGER ALL ;

drop table tmp_reco_acc;

------------------------------------------------------------------
Synonym
#duplicate synonym
select r.id, r.name, t.canonical_form, r.taxon_concept_id from recommendation as r, taxonomy_definition as t  where r.taxon_concept_id != r.accepted_name_id and r.taxon_concept_id is not null and r.accepted_name_id is not null  and r.taxon_concept_id = t.id and t.lowercase_match_name != r.lowercase_name and r.is_scientific_name = true order by r.lowercase_name;

#distinct reco count to be corrected
select count(distinct(recommendation_id)) from recommendation_vote where recommendation_id  in (select r.id from recommendation as r, taxonomy_definition as t  where r.taxon_concept_id != r.accepted_name_id and r.taxon_concept_id is not null and r.accepted_name_id is not null  and r.taxon_concept_id = t.id and t.lowercase_match_name != r.lowercase_name and r.is_scientific_name = true order by r.lowercase_name);


# delete by sql
ALTER TABLE recommendation disable TRIGGER ALL ;
 
create table tmp_reco_syn as ((select r.id from recommendation as r, taxonomy_definition as t  where r.taxon_concept_id != r.accepted_name_id and r.taxon_concept_id is not null and r.accepted_name_id is not null  and r.taxon_concept_id = t.id and t.lowercase_match_name != r.lowercase_name and r.is_scientific_name = true order by r.lowercase_name) except (select distinct(recommendation_id) from recommendation_vote where recommendation_id  in (select r.id from recommendation as r, taxonomy_definition as t  where r.taxon_concept_id != r.accepted_name_id and r.taxon_concept_id is not null and r.accepted_name_id is not null  and r.taxon_concept_id = t.id and t.lowercase_match_name != r.lowercase_name and r.is_scientific_name = true order by r.lowercase_name)));

delete from recommendation where id in ( select id from tmp_reco_syn );

ALTER TABLE recommendation enable TRIGGER ALL ;
drop table tmp_reco_syn;

















*/