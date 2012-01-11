package species

import java.sql.ResultSet;

import species.TaxonomyDefinition.TaxonomyRank;
import species.formatReader.SpreadsheetReader;
import species.sourcehandler.MappedSpreadsheetConverter;
import species.sourcehandler.XMLConverter;
import grails.converters.JSON;
import grails.converters.XML;
import grails.web.JSONBuilder;
import groovy.sql.GroovyRowResult;
import groovy.sql.Sql
import groovy.xml.MarkupBuilder;


class DataController {

	def dataSource

	static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

	/**
	 * 
	 */
	def index = {
	}

	/**
	 * 
	 */
	def listHierarchy = {
		log.debug params;
		cache "taxonomy_results"
		includeOriginHeader();
		
		def level = params.n_level ? Integer.parseInt(params.n_level)+1 : null
		def parentId = params.nodeid  ?: null
		def expandAll = params.expand_all  ? (new Boolean(params.expand_all)).booleanValue(): false
		def expandSpecies = params.expand_species  ? (new Boolean(params.expand_species)).booleanValue(): false
		long classSystem = params.classSystem ? Long.parseLong(params.classSystem): Classification.getByName(grailsApplication.config.speciesPortal.fields.AUTHOR_CONTRIBUTED_TAXONOMIC_HIERARCHY).id;
		def speciesid = params.speciesid ? Integer.parseInt(params.speciesid) : null

		def rs = new ArrayList<GroovyRowResult>();
		if(expandSpecies) {
			def taxonIds = getSpeciesHierarchyTaxonIds(speciesid, classSystem)
			getHierarchyNodes(rs, 0, 8, null, classSystem, false, taxonIds);
		} else {
			getHierarchyNodes(rs, level, level+3, parentId, classSystem, expandAll, null);
		}
		render(contentType: "text/xml", text:buildHierarchyResult(rs, classSystem))
	}

	/**
	 * 
	 * @param resultSet
	 * @param level
	 * @param parentId
	 * @param classSystem
	 * @param expandAll
	 * @param taxonIds
	 */
	private void getHierarchyNodes(List<GroovyRowResult> resultSet, int level, int tillLevel, String parentId, long classSystem, boolean expandAll, List taxonIds) {
		def sql = new Sql(dataSource)
		def rs;
		if(!parentId) {
			rs = sql.rows("select t.id as taxonid, count(*) as count, 0 as rank, t.name as name, s.path as path \
			from taxonomy_registry s, \
				classification f, \
				taxonomy_definition t \
			where \
				s.taxon_definition_id = t.id and \
				s.classification_id = :classSystem and \
				t.rank = 0 and \
				s.parent_taxon_id is null \
			group by s.path, t.id, t.rank, t.name  ", [classSystem:classSystem])
		}
		else if(level == TaxonomyRank.SPECIES.ordinal()) {
			rs = sql.rows("select t.id as taxonid,  1 as count, t.rank as rank, t.name as name,  s.path as path \
			from taxonomy_registry s, classification f, taxonomy_definition t \
			where \
				s.taxon_definition_id = t.id and \
				s.classification_id = :classSystem and \
				t.rank = "+level+" and \
				s.path ~ '^"+parentId+"_[0-9]+\$' " , [classSystem:classSystem]);
		} else {
			rs = sql.rows("select t.id as taxonid, count(*) as count, t.rank as rank, t.name as name,  s.path as path \
			from taxonomy_registry s, \
				classification f, \
				taxonomy_definition t \
			where \
				s.taxon_definition_id = t.id and \
				s.classification_id = :classSystem and \
				s.path ~ '^"+parentId+"_[0-9]+\$' " +
					" group by s.path, t.rank, t.name, t.id \
			order by t.id, t.rank", [classSystem:classSystem])
		}
		rs.each { r ->
			r.put('expanded', false);
			r.put("speciesid", -1)
			if(r.rank == TaxonomyRank.SPECIES.ordinal()) {
				def species = getSpecies(r.taxonid);
				if(species) {
					r.put("speciesid", species.id)
					r.put('name', species.title)
					r.put('count', 1);
				}
			}
			resultSet.add(r);
			if(expandAll || (taxonIds && taxonIds.contains(r.taxonid))) {
				if(r.rank < TaxonomyRank.SPECIES.ordinal()) {
					//r.put('count', getCount(r.path, classSystem));
					if(r.rank+1 <= tillLevel) {
						r.put('expanded', true);
						getHierarchyNodes(resultSet, r.rank+1, tillLevel, r.path, classSystem, expandAll, taxonIds)
					}
				}
			}
		}
	}

	/**
	 * 
	 * @param name
	 * @param taxonId
	 * @param path
	 * @param classSystem
	 * @return
	 */
	private getSpecies(long taxonId) {
		def sql = new Sql(dataSource)
		int level = TaxonomyRank.SPECIES.ordinal();
		return Species.find("from Species as s where s.taxonConcept.id = :taxonId", [taxonId:taxonId]);
		/*
		def rs = sql.rows("select s.species_id as speciesid \
			from taxonomy_registry s, classification f, taxonomy_definition t \
			where \
				s.taxon_definition_id = t.id and \
				s.classification_id = f.id and \
				f.name = :classSystem and \
				t.rank = :level and \
				t.name = :name and \
				t.id = :taxonId and \
				s.path = :path", [level:level, taxonId:taxonId, name:name, path:path, classSystem:classSystem]);
		return rs[0]?.speciesid;
		*/
	}

	/**
	 * TODO:optimize 
	 * @param id
	 * @return
	 */
	private String getSpeciesName(long id) {
		def species = Species.get(id);
		return species.taxonConcept.italicisedForm;
	}

	/**
	 * 
	 * @param speciesId
	 * @param classSystem
	 * @return
	 */
	private List getSpeciesHierarchyTaxonIds(Long taxonId, long classSystem) {
		def sql = new Sql(dataSource)
		String s = """select s.path as path 
			from taxonomy_registry s, 
				taxonomy_definition t 
			where 
				s.taxon_definition_id = t.id and 
				s.classification_id = :classSystem and 
				s.path like '%!_"""+taxonId+"' escape '!'";
			
		def rs = sql.rows(s, [taxonId:taxonId, classSystem:classSystem])
		def paths = rs.collect {it.path};
		
		
		def result = [];
		 paths.each {
			 it.tokenize("_").each {
				 result.add(Long.parseLong(it));
			 }
		 }
		return result;
//		return [Species.get(speciesId).id]
	}

	/**
	*
	* @param speciesId
	* @param classSystem
	* @return
	*/
   private int getCount(String parentId, String classSystem) {
	   def sql = new Sql(dataSource)
	   def rs = sql.rows("select count(*) as count \
		   from taxonomy_registry s, \
			   classification f, \
			   taxonomy_definition t \
		   where \
			   s.taxon_definition_id = t.id and \
			   s.classification_id = f.id and \
			   f.name = :classSystem and \
		   		s.path ~ '^"+parentId+"_[0-9_]+\$' " +
				" group by t.rank \
			having t.rank = :rank", [classSystem:classSystem, rank:TaxonomyRank.SPECIES.ordinal()])
	   return rs[0]?.count;
   }

	/**
	 * render t as XML;
	 * @param rs
	 * @param classSystem
	 * @return
	 */
	private String buildHierarchyResult(rs, classSystem) {
		def writer = new StringWriter ();
		def result = new MarkupBuilder(writer);
		int i=0;
		result.rows() {
			page (1)
			total (1)
			int size = 0;
			//t.each { taxonReg ->
			rs.each { r->
				size ++;
				String parentPath = "";
				if(r.path && r.path.lastIndexOf("_")!=-1) {
					parentPath = r.path.substring(0, r.path.lastIndexOf("_"))
				}
				row(id:r.path) {
					cell(r.path)
					cell (r.name.trim())
					cell (r.count)
					cell (r["speciesid"])
					cell (r.rank)
					cell (parentPath)
					cell (r.rank == TaxonomyRank.SPECIES.ordinal() ? true : false)
					cell (r.expanded?:false) //for expanded
					cell (r.expanded?true:false) //for loaded
				}
			}
			records (size)
		}
		return writer.toString();
	}
	
	/**
	 * 
	 * @param origin
	 * @return
	 */
	private boolean isValid(String origin) {
		String originHost = (new URL(origin)).getHost();
		return grailsApplication.config.speciesPortal.validCrossDomainOrigins.contains(originHost)
	}
	
	/**
	 * 
	 */
	private void includeOriginHeader() {
		String origin = request.getHeader("Origin");
		if(origin) {
			String validOrigin = isValid(origin)?origin:"";
			response.setHeader("Access-Control-Allow-Origin", validOrigin);
			response.setHeader("Access-Control-Allow-Methods", request.getHeader("Access-Control-Request-Methods"));
			response.setHeader("Access-Control-Allow-Headers", request.getHeader("Access-Control-Request-Headers"));
			response.setHeader("Access-Control-Max-Age", "86400");
		}
	}

}