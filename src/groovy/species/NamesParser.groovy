package species

import org.apache.commons.logging.LogFactory;

import species.TaxonomyDefinition.TaxonomyRank;
import species.utils.Utils;

import groovyx.net.http.HTTPBuilder;
import groovyx.net.http.ContentType;
import groovyx.net.http.Method;

/**
 * Taxonomy name parser.
 * @author sravanthi
 *
 */
class NamesParser {

	private static final log = LogFactory.getLog(this);

	/**
	 * max 5000 
	 * @param names
	 * @return List of IName objects
	 */
	List<TaxonomyDefinition> parse(List<String> names) {
		log.debug "Parsing names : "+names;
		def cleanNames = [];
		List<TaxonomyDefinition> parsedNames = new ArrayList<TaxonomyDefinition>();
		for(name in names) {
			cleanNames.add(Utils.cleanName(name));
		}
		
		if(cleanNames) {
			def parsedNamesJSON = gniNamesParser(cleanNames.join("\n"));
			parsedNames.addAll(getParsedNames(parsedNamesJSON));
			cleanNames.clear();
			log.debug "Finished parsing rest of the names";
		}
		
		return parsedNames;
	}

	/**
	 * Parses name string as per the GNI name parser
	 * 5000 names max utf-8 names
	 * @param names
	 * @return On success returns parsed names information in JSON format. Otherwise returns null.
	 */
	private def gniNamesParser(names) {
		def parsedJSON;
		def http = new HTTPBuilder()
		http.request(  'http://gni.globalnames.org', Method.POST, ContentType.JSON) {
			uri.path = 'parsers.json'
			body = [ names : names ]

			response.success = { resp, json ->
				if(resp.isSuccess()) {
					log.debug "GNI parser result : "+json
					parsedJSON = json
				}
			}
			response.failure = { resp ->  log.error 'GNIParser request failed' }
		}
		return parsedJSON;
	}

	/**
	 * 
	 * @param parsedNamesJson
	 * @return
	 */
	private List getParsedNames(parsedNamesJSON) {
		List parsedNames = new ArrayList();
		if(parsedNamesJSON) {
			def sciNames = parsedNamesJSON.scientificName;
			sciNames.each { sciName ->
				if(sciName.parsed) {
					def hybridName;
					if(sciName.hybrid) {
//						log.debug "Creating a hybrid name"
//						hybridName = new HybridName();
//						hybridName.canonicalForm = sciName.canonical;
//						hybridName.normalizedForm = sciName.normalized;
//						hybridName.italicisedForm = getItalicisedForm(sciName);
//						hybridName.name = sciName.verbatim;
					}

					def parsedName;
					sciName.details.each { part ->
						parsedName = new TaxonomyDefinition();
						if(sciName.canonical) parsedName.canonicalForm = sciName.canonical;
						if(sciName.normalized) parsedName.normalizedForm = sciName.normalized;
						if(sciName.verbatim) parsedName.name = sciName.verbatim;
						parsedName.italicisedForm = getItalicisedForm(sciName);
						//if(part.uninomial?.string) parsedName.uninomial = part.uninomial.string;
						if(part.genus?.string && part.species?.string) parsedName.binomialForm = part.genus.string + " " + part.species.string;
						//if(part.infraGenus?.string) parsedName.infraGenus = part.infraGenus.string;
						//if(part.infraSpecies?.string) parsedName.infraSpecies = part.infraSpecies.string;

						//TODO make this descendant selector & there shd be a better way to write this
						if(part.species?.combinationAuthorTeam?.author) {
							for( author in (part.species.combinationAuthorTeam.author[0][0]) ) {
								parsedName.addToAuthor(author);
							}
						}

						if(part.species?.basionymAuthorTeam?.author) {
							for( author in part.species.basionymAuthorTeam.author[0][0] ) {
								parsedName.addToAuthor(author);
							}
						}

//						if(part.infraSpecies?.combinationAuthorTeam?.author) {
//							for( author in part.infraSpecies.combinationAuthorTeam.author[0][0]) {
//								parsedName.addToAuthors(author);
//							}
//						}
//
//						if(part.infraSpecies?.basionymAuthorTeam?.author) {
//							for(author in part.infraSpecies.basionymAuthorTeam.author[0][0]) {
//								parsedName.addToBasionymAuthors(author);
//							}
//						}

						if(part.species?.combinationAuthorTeam?.year)
							parsedName.addToYear(part.species.combinationAuthorTeam.year[0][0].toString());
						if(part.species?.basionymAuthorTeam?.year)
							parsedName.addToYear(part.species.basionymAuthorTeam.year[0][0].toString());
//						if(part.infraSpecies?.combinationAuthorTeam?.year)
//							parsedName.addToYear(part.infraSpecies.combinationAuthorTeam.year[0][0]);
//						if(part.infraSpecies?.basionymAuthorTeam?.year)
//							parsedName.addToBasionymYear(part.infraSpecies.basionymAuthorTeam.year[0][0]);

						//ignoring rank;
						//ignoring cultivar name type

						if(sciName.hybrid) {
							//hybridName.addToNames(parsedName);
						} 
					}

					if(sciName.hybrid) {
						//parsedNames.add(hybridName);
					} //else {
						parsedNames.add(parsedName);
					//}
				} else {
					log.warn "Name is not parsed : "+sciName.verbatim
					parsedNames.add(new TaxonomyDefinition(name:sciName.verbatim));
				}
			}
		}
		return parsedNames;
	}

	/**
	 * Italicising complete name except abbreviations and author names
	 * @return
	 */
	public String getItalicisedForm(sciName) {
		def name = sciName.verbatim;
		log.debug "Italicising scientific name  : "+name;
		BitSet flags = new BitSet(name.length());

		def italicisedForm = name;
		sciName.positions.each { e ->
			//collecting all author word positions to remove from italics
			if(e.getValue()[0].equals("author_word")) {
				flags.set(Integer.parseInt(e.key));
				flags.set(e.getValue()[1]);
			} else if(e.getValue()[0].equals("year")) {
				flags.set(Integer.parseInt(e.key));
				flags.set(e.getValue()[1]);
			} 
		};
		//collecting all abbreviations positions to remove from italics
		def matcher = name =~ /\s[a-z]+\./
		while(matcher.find()) {
			flags.set(matcher.start()+1);
			flags.set(matcher.end());
			log.debug matcher.start()
			log.debug matcher.end();
		}

		log.debug "Italicizing positions : "+flags

		int start = 0;
		int prevStart = 0;
		int end = 0;
		italicisedForm = "<i>";
		for(int i=flags.nextSetBit(0); i>=0; i=flags.nextSetBit(i+1)) {
			start = i;
			i = end = flags.nextSetBit(i+1);
			if(start >= name.length()) break;
			if(end >= 0 && end < name.length()-1) {
				italicisedForm += name.substring(prevStart, start) + "</i>" + name.substring(start, end) + "<i>";
			} else {
				italicisedForm += name.substring(prevStart, start) + "</i>" + name.substring(start) + "<i>";
				prevStart = name.length();
				break;
			}
			prevStart = end;
			start = end = 0;
		}

		if(prevStart < name.length())
			italicisedForm += name.substring(prevStart)

		italicisedForm += "</i>";
		italicisedForm = italicisedForm.replaceAll(/\<i\>\s*\<\/i\>/, " ");
		log.debug "Italicized form : "+italicisedForm
		return italicisedForm;
	}
}
