import species.CommonNames;
import species.Language;
import species.participation.ChecklistService
import species.participation.curation.*
import species.participation.*
import species.formatReader.SpreadsheetReader;
import species.utils.*;

//def checklistService = ctx.getBean("checklistService");

//checklistService.updateUncuratedVotesTable()

//checklistService.migrateChecklist()

//checklistService.updateLocation()

//checklistService.changeCnName()

//checklistService.mCn()


def dupCommonNames(fname){
	List ids = []
	def res = []
	Set checkedIds = new HashSet()
	CommonNames.listOrderById().each {it ->
		ids.add(it.id)
	}
	File file = new File(fname);
	file.createNewFile()
	ids.each{ rId ->
		if(!checkedIds.contains(rId)){
			println "checking == " + rId

			CommonNames reco = CommonNames.read(rId)
			def c = CommonNames.createCriteria();
			def result = c.list {
				ilike('name', reco.name);
				(reco.language) ? eq('language', reco.language) : isNull('language');
				eq('taxonConcept', reco.taxonConcept)
			}


			if(result.size() > 1){
				List tmp = []
				result.each { it ->
					checkedIds.add(it.id)
					tmp.add(it.id)
				}
				tmp.sort()
				file << "\n"
				file << tmp.join(", ")
				res.add(tmp)
			}
			checkedIds.add(rId)
		}
	}

	println "=========== size " + res.size()
	println "========= done "
}


def dupReco(boolean isSn, fileName){
	List recoIds = []
	Recommendation.findAllByIsScientificName(isSn, [sort: "id", order: "desc"]).each{ it ->
		recoIds.add(it.id)
	}

	println " tatal size " + recoIds.size()

	List res = []

	Set checkedIds = new HashSet()

	File file = new File(fileName);
	file.createNewFile()
	recoIds.each{ rId ->
		if(!checkedIds.contains(rId)){
			println "checking == " + rId

			Recommendation reco = Recommendation.read(rId)
			def c = Recommendation.createCriteria();
			def result = c.list {
				ilike('name', reco.name);
				eq('isScientificName', reco.isScientificName);
				(reco.languageId) ? eq('languageId', reco.languageId) : isNull('languageId');
				(reco.taxonConcept) ? eq('taxonConcept', reco.taxonConcept) : isNull('taxonConcept');
			}


			if(result.size() > 1){
				List tmp = []
				result.each { it ->
					checkedIds.add(it.id)
					tmp.add(it.id)
				}
				tmp.sort()
				file << "\n"
				file << tmp.join(", ")
				res.add(tmp)
			}

			checkedIds.add(rId)
		}
	}

	println "=========== size " + res.size()
	println "========= done "

}


def postProcess(str){
	def file = new File(str);
	def res = []
	file.eachLine{ l ->
		if(l && l.trim() != ""){
			def tmp = []
			def ar = l.split(",")
			for(int i =0 ; i < ar.length; i++){
				tmp.add(ar[i].trim().toLong())
			}
			res.add(tmp)
		}
	}

	return res
}


def findInRecoVote(List res, boolean isSn, file){
	Set rvSnRecoId = new HashSet()

	RecommendationVote.listOrderById(order: "asc").each{ RecommendationVote rv ->
		if(isSn){
			if(rv.recommendation.isScientificName){
				rvSnRecoId.add(rv.recommendation.id)
			}
		}else{
			if(rv.commonNameReco && !rv.commonNameReco.isScientificName){
				rvSnRecoId.add(rv.commonNameReco.id)
			}
		}
	}

	return generateIds(res, rvSnRecoId, file)
}


def findInCl(List res, boolean isSN,  file){
	Set recoIds = new HashSet()

	if(isSN){
		ChecklistRowData.findAllByRecoIsNotNull().each{ChecklistRowData clr ->
			//if(clr.reco){
			recoIds.add(clr.reco.id)
			//}
		}
	}else{
		UnCuratedVotes.findAllByRefTypeAndCommonNameIsNotNull(Checklist.class.getCanonicalName()).each{ UnCuratedVotes uv ->
			//if(uv.commonName){
			recoIds.add(uv.commonName.reco.id)
			//}
		}
	}
	return generateIds(res, recoIds, file)
}


def generateIds(List res, Set recoIds, file){
	println "size  " + recoIds.size()
	Set preservIds = new HashSet()
	Set deleteIds = new HashSet()

	res.each { List l ->
		Set s = new HashSet(l)
		def iis = recoIds.intersect(s)
		if(iis.size() ==  1){
			println "=== intersect size " + iis.size()
			file << "\n preserve \n"
			file << iis.join(", ")
			preservIds.addAll(iis)
			//deleteIds.addAll(s)
		}else if(iis.size() >  1){
			println "=== intersect size " + iis.size()
			file << "\n error delete with presere one \n"
			file << iis.join(", ")
		}else if(iis.size() == 0){
			List a = s.sort()
			preservIds.add(a.get(0))
			//deleteIds.addAll(a)
		}
		deleteIds.addAll(l)
	}

	deleteIds.removeAll(preservIds)

	return [preservIds, deleteIds]

}

def createFile(){
	def cnamesFile =  "/home/sandeept/cl-mig/a1/dup_cn_names.txt"
	def snFile =  "/home/sandeept/cl-mig/a1/dup_sn_reco.txt"
	def cnFile =  "/home/sandeept/cl-mig/a1/dup_cn_reco.txt"

	dupCommonNames(cnamesFile)

	println "======================= sn name "
	dupReco(true, snFile)
	println "======================= common name "
	dupReco(false, cnFile)
}



def checkValidity(List res, o1, o2){
	def pres = o1[0].plus(o2[0])
	
	boolean flag = true
	res.each{ List l ->
		Set s = new HashSet(l)
		def iis = pres.intersect(s)
		if(iis.size() == 0){
			flag = false
			println "=== error " + iis
		}else if(iis.size() > 1){
			flag = false
			println "error  dups " + iis
			println " orignal set " + l
		}
	}
	return flag
}

def testFinal(){
	def snFile =  "/home/sandeept/cl-mig/a1/dup_sn_reco.txt"
	def cnFile =  "/home/sandeept/cl-mig/a1/dup_cn_reco.txt"

	def resFile = "/home/sandeept/cl-mig/a1/recoVote.txt"
	File file = new File(resFile);
	file.createNewFile()


	def res

	res = postProcess(snFile)
	file << "\n\n"

	file << "=======================================SN start reco ===="
	file << "recovote  sn "
	def o1 = findInRecoVote(res, true, file)
	file << "=======================================SN start reco ====DONNNNNNNNNNN"
	file << "\n\n"
	file << "=======================================SN start CHECKLIST ===="
	file << "for checklist  sn "
	def o2 = findInCl(res, true, file)
	file << "=======================================SN start CHECKLIST ====DONNNNNNNNNNN"

	file << " ===========sn==== validity " + checkValidity(res, o1, o2)

	res = postProcess(cnFile)

	file << "\n\n"
	file << "=======================================CN start reco ===="
	file << "recovote  cn "
	def o3 = findInRecoVote(res, false, file)
	file << "=======================================CN start reco ====DONNNNNNNNNNN"
	file << "\n\n"
	file << "=======================================SN start CHECKLIST ===="
	file << "for checklist  cn "
	def o4 = findInCl(res, false, file)
	file << "=======================================SN start CHECKLIST ====DONNNNNNNNNNN"
	file << " ===========cn==== validity " + checkValidity(res, o3, o4)

	Set pres = new HashSet()
	pres.addAll(o1[0])
	pres.addAll(o2[0])
	pres.addAll(o3[0])
	pres.addAll(o4[0])

	Set dels = new HashSet()
	dels.addAll(o1[1])
	dels.addAll(o2[1])
	dels.addAll(o3[1])
	dels.addAll(o4[1])


	Set npres = new HashSet()
	pres.each{ l ->
		npres.add(l.toString().trim().toLong().longValue())
	}

	Set ndels = new HashSet()
	dels.each{ l ->
		ndels.add(l.toString().trim().toLong().longValue())
	}

	println " ff intersectn should be null " + pres.intersect(dels)

	def ttList = new HashSet([
		312283L,
		301219L,
		300834L,
		308320L,
		312381L,
		300838L,
		299698L,
		299688L,
		299685L,
		299684L,
		299672L,
		299664L
	])
	
	println "==== s " + npres.size()

	println "  remove all " + npres.removeAll(ttList)
	println "==== s " + npres.size()
	println " pres and ttlist " + npres.intersect(ttList)

	println "==== aaa " + ndels.addAll(ttList)
	println "==== aaa " + ndels.removeAll(npres)

	println "after remove ff intersectn nn1" + npres.intersect(ndels)

	println "============== dels size " + ndels.size()
	file << "\n\n========================================="


	resFile = "/home/sandeept/cl-mig/a1/recoVote_1.txt"
	file = new File(resFile);
	file.createNewFile()

	file << ndels.join(", ")
}



def delReco(){

	def resFile = "/tmp/recoVote_1.txt"
	File file = new File(resFile);
	Set res = new HashSet()
	file.eachLine{ l ->
		if(l && l.trim() != ""){
			def ar = l.split(",")
			for(int i =0 ; i < ar.length; i++){
				res.add(ar[i].trim().toLong())
			}
		}
	}

	Recommendation.withTransaction(){
		res.each { Long id ->
			def r = Recommendation.get(id)
			if(r){
				r.delete()
			}else{
				println "  error " + id
			}
		}
	}
}

def cleanCommonName(){
	CommonNames.withTransaction(){

		SpreadsheetReader.readSpreadSheet("/tmp/common_name_lang.xls").get(0).each{ m ->
			//println m
			def cnId = m.id.toFloat().toLong()
			def newCnLang = Language.findByNameIlike(m.language.trim())
			def newName = Utils.getTitleCase(m.name.trim())

			CommonNames cn = CommonNames.get(cnId)
			def oldName = cn.name

			def rc = Recommendation.createCriteria();
			def result = rc.list {
				ilike('name', oldName);
				eq('isScientificName', false);
				(cn.taxonConcept) ? eq('taxonConcept', cn.taxonConcept) : isNull('taxonConcept');
				(cn.language) ? eq('languageId', cn.language.id) : isNull('languageId');
			}
			
			if(result.size() > 1){
				println " recommme error "
			}

			println "===== start "
			result.each{ Recommendation r ->
				println " reco === $r.id , $r.name, $r.languageId, $r.taxonConcept "
				def unCns = UnCuratedCommonNames.findAllByNameIlikeAndLanguage(r.name, r.languageId?Language.read(r.languageId):null);

				if(unCns.size() > 1) {
					println "error in uncurated common name" + unCns.size()
				}

				unCns.each { UnCuratedCommonNames uncn ->
					uncn.name = newName
					uncn.language = newCnLang
					uncn.save()
				}
				r.setName(newName)
				r.languageId = newCnLang.id
				r.save()

			}
			

			cn.name = newName
			cn.language = newCnLang
			cn.save()
			println " === end "
		}
	}
}

/*
 def test(){
 Recommendation.withTransaction(){
 def name = "should be unique always"
 def reco = new Recommendation(name:name, taxonConcept:null, isScientificName:false, languageId:205);
 def reco1 = new Recommendation(name:name, taxonConcept:null, isScientificName:false, languageId:205);
 reco.save()
 reco1.save()
 }
 }
 def getWrongCommonName(){
 def shared = 0
 Set checklistIds = new HashSet()
 Set badRecoIds = new HashSet()
 Set recoVoteCommonNamesIds = new HashSet(RecommendationVote.list().collect(){
 it.commonNameReco?.id
 });
 def ucvList = UnCuratedVotes.findAllWhere(refType:Checklist.class.getCanonicalName()).each{ ucv ->
 UnCuratedCommonNames ucn = ucv.commonName
 if(ucn &&  !ucn.reco.isScientificName ){
 if(recoVoteCommonNamesIds.contains(ucn.reco.id)){
 shared++
 }else{
 checklistIds.add(ucv.refId)
 badRecoIds.add(ucn.reco.id)
 }
 //println "=== bad reco " +  ucn.reco
 }
 }
 def d = new Date(112, 11, 31)
 println "=== date " + d
 def newRecosSize = Recommendation.findAllByIsScientificNameAndLastModifiedGreaterThanEquals(false, d).size()
 println "=============================================================="
 println "total bad common names " + badRecoIds.size()
 println "shared common name " + shared
 println "new recos size " + newRecosSize
 println "total checklist has common names " + checklistIds.size()
 println "useful common name reco " + recoVoteCommonNamesIds.size()
 println "=============================================================="
 }
 //getWrongCommonName()
 // select * from un_curated_votes where ref_type = 'species.participation.Checklist' and voted_on >= '2012-12-30 00:00:00' order by voted_on asc;
 */

