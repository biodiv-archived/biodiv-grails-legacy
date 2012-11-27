package species.participation

import java.text.SimpleDateFormat
import java.util.Iterator;
import java.util.List;

import species.auth.SUser
import species.Species;
import species.groups.SpeciesGroup;
import species.groups.UserGroup;
import species.License;
import species.Reference;
import groovy.sql.Sql;
import species.utils.Utils;

import au.com.bytecode.opencsv.CSVReader

class ChecklistService {

	def grailsApplication

	String connectionUrl1 =  "jdbc:postgresql://192.168.4.172:5432/ibp";
	String connectionUrl =  "jdbc:postgresql://localhost/ibp";
	String userName = "postgres";
	String password = "postgres123";

	def migrateChecklist(){
		def sql = Sql.newInstance(connectionUrl, userName, password, "org.postgresql.Driver");
		int i=0;
		sql.eachRow("select * from ibp_checklist limit 20") { row ->
			log.debug " >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>     title ===  $i " + row.title
			Checklist checklist = createCheckList(row, sql)
			i++
		}
	}

	def Checklist createCheckList(row, sql){
		Checklist cl = new Checklist()
		cl.title = row.title
		cl.description = row.info
		cl.attribution = row.attribution

		cl.author = SUser.findByUsername("admin")
		cl.speciesGroup = getSpeciesGroup(row.id, sql)
		cl.license = getLicense(row.license)
		cl.refText = row.checklist_references

		cl.placeName = row.geography_given_name

		cl.linkText = row.link
		//addReferences(cl, row.link)

		//write one file in web server location
		saveRawFile(cl, row.raw_checklist)

		//get actual data
		fillData(cl, row.raw_checklist)

		/*
		 //locatoin related  
		 //date related  
		 cl.fromDate = row.from_date
		 cl.toDate = row.to_date
		 cl.publicationDate = row. publication_date
		 cl.lastUpdated = row.last_updated
		 */

		if(!cl.save(flush:true)){
			cl.errors.allErrors.each { log.error it }
			return null
		}else{
			log.debug "saved successfully  >>>>>>> " + cl
			return cl
		}
	}

	private saveRawFile(cl, String rawText){
		def rootDir = grailsApplication.config.speciesPortal.checklist.rootDir
		def checklistRootDir = new File(rootDir);
		if(!checklistRootDir.exists()) {
			checklistRootDir.mkdir();
		}

		def currentChecklistDir = new File(checklistRootDir, UUID.randomUUID().toString());
		currentChecklistDir.mkdir();

		File file = new File(currentChecklistDir, "rawFile.csv");
		file.createNewFile()
		file << rawText
		log.debug "saved in file " + file.getAbsolutePath()
		cl.rawChecklist = file.getAbsolutePath()
	}

	private fillData(cl, String rawText){
		CSVReader csvReader = new CSVReader(new StringReader(rawText))
		List arrayList = csvReader.readAll()

		log.debug " total size of rows " + arrayList.size()

		int i = 0
		def keyNames = null
		for(ar in arrayList){
			if(!keyNames){
				keyNames = ar
			}else{
				populateData(cl, keyNames, ar, i++)
			}
		}
		csvReader.close()
	}

	private populateData(cl, String[] keys, String[] values, rowId){
		//log.debug "keys ===  " +  keys.length   + "  "  + keys
		//log.debug "values === " + values.length  + "  " + values

		for (int i = 0; i < keys.length; i++) {
			def value = null
			if(i < values.length){
				value = values[i]
			}
			def clr = new ChecklistRowData(key:keys[i], value:value, rowId:rowId)
			//handle scintific name link and interacti with scientific name infrastructre
			cl.addToRow(clr);
		}
	}

	private addReferences(cl, linkText){
		//XXX needs to do more processing
		cl.addToReference(new Reference(url:linkText))
	}

	private License getLicense(licId){
		switch (licId) {
			case 1:
				return License.read(7)
			case 2:
				return License.read(6)
			case 3:
				return License.read(5)
			case 4:
				return License.read(4)
			case 5:
				return License.read(3)
			case 6:
				return License.read(2)
			default:
				log.debug "========== licId " + licId
				return null;
		}
	}


	private SpeciesGroup getSpeciesGroup(id, Sql sql){
		String query = "select common_name as cn from ibpcl_taxa as ita, checklist_taxa as clt where clt.taxa_id = ita.id and clt.checklist_id = " + id
		String taxaName = sql.firstRow(query).get("cn")
		//XXX handle missing group things
		def sg = SpeciesGroup.findByName(taxaName)
		if(!sg){
			log.debug " no group for     $taxaName"
		}
		return sg
	}

}
