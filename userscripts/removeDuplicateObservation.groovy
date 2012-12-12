import species.participation.Observation

def removeDuplicate(){
	def obvIds = [269575, 269565, 269775, 270155, 270193, 270194, 270195]
	for(id in obvIds){
		def obv = Observation.get(id)
		obv.isDeleted = true
		if(!obv.save(flush:true)){
			obv.errors.allErrors.each { log.error it }
		}else{
			log.debug "deleted successfully  >>>>>>> " + obv
		}
	}
}

removeDuplicate()
/*
sql query
select a.id as id1, b.id as id2, a.created_on as c1, b.created_on as c2, abs(((DATE_PART('day', a.created_on::timestamp -  b.created_on::timestamp) * 24 + DATE_PART('hour', a.created_on::timestamp - b.created_on::timestamp)) * 60 + DATE_PART('minute', a.created_on::timestamp - b.created_on::timestamp)) * 60 + DATE_PART('second',  a.created_on::timestamp - b.created_on::timestamp)) as diff from observation as a, observation as b where a.author_id = b.author_id and a.id != b.id and abs(((DATE_PART('day', a.created_on::timestamp -  b.created_on::timestamp) * 24 + DATE_PART('hour', a.created_on::timestamp - b.created_on::timestamp)) * 60 + DATE_PART('minute', a.created_on::timestamp - b.created_on::timestamp)) * 60 + DATE_PART('second',  a.created_on::timestamp - b.created_on::timestamp)) < 10 and a.is_deleted = false and b.is_deleted = false and a.habitat_id = b.habitat_id and a.place_name = b.place_name order by a.created_on;
*/