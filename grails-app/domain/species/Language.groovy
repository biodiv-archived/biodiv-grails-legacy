package species

import com.fasterxml.jackson.annotation.JsonIgnore

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

//@Cache(region="language", usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, include = "non-lazy")
class Language  implements Serializable {
	public static final String DEFAULT_LANGUAGE = "English";
	private static final Random NUMBER_GENERATOR = new Random();
	
	String threeLetterCode;
	String twoLetterCode;
	String name;
	String region;
	//to identify language for curation
	boolean isDirty = false;

    @JsonIgnore
    static Language defaultLanguage;
    static transients = ['defaultLanguage']

    static constraints = {
		threeLetterCode(blank:false, nullable:false, unique:true);
		twoLetterCode(nullable:true);
		name(blank:false, nullable:false);
		region(blank:true, nullable:true);
		isDirty(blank:false, nullable:false);
    }
	
	static mapping = { 
		version false;
		sort 'name';
        //cache usage: 'nonstrict-read-write', include: 'non-lazy'
	}
	
	public static Language getLanguage(String languageName){
		Language lang = null;
		
		if(!languageName || languageName.trim() == ""){
            if(!Language.defaultLanguage) Language.defaultLanguage = Language.findByName(DEFAULT_LANGUAGE);
			return Language.defaultLanguage;
        } else{ 
			lang = Language.findByNameIlike(languageName.trim());
			if(!lang){
				//inserting new language
				lang = new Language(name:languageName.trim(), threeLetterCode:_getThreeLetterCode(languageName), isDirty:true);
				if(!lang.save(flush:true)){
					//println "Error during new language save $languageName"
					lang = null;
				}
			}
		}
		return lang;
    }
	
	private static String _getThreeLetterCode(String languageName){
		//TODO fix this
		int i = 0;
		while(++i < 100){
			//getting a 3 digit number
			String number = "" + (Math.abs((NUMBER_GENERATOR.nextInt() + 100) % 1000)) ;
			if(!Language.findByThreeLetterCode(number)){
				return number;
			}
		}
		//println "Invalid ThreeLetterCode. please give unique code"
		return null;
	}
	
	public static filteredList(){
		return Language.findAllByIsDirtyOrRegionIsNotNull(true, [cache:true]).collect{it.name;} ;
	}

    static List<Language> list() { 
        return Language.createCriteria().list {
            order('name', 'asc')
            //cache true
        }
    }

    static Language findByName(String whatever) { 
        return Language.createCriteria().get {
            eq 'name', whatever
            cache true
        }
    }

    static Language findByTwoLetterCode(String whatever) { 
        return Language.createCriteria().get {
            eq 'twoLetterCode', whatever
            cache true
        }
    }
}
