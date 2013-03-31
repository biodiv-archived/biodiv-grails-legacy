package content.eml

import content.fileManager.UFile

/**
 * eml-literature module
 * http://knb.ecoinformatics.org/software/eml/eml-2.1.1/eml-literature.html
 * http://knb.ecoinformatics.org/software/eml/eml-2.1.1/index.html
 *
 */
class Document {

	public enum DocumentType {
		REPORT("Report"),
		POSTER("Poster"),


		private String value;

		DocumentType(String value) {
			this.value = value;
		}

		public String value() {
			return this.value;
		}
	}

	DocumentType type
	String title

	UFile uFile   //covers physical file formats, party info, access info

	Coverage coverage 	//Coverage Information


	static constraints = {
		uFile(nullable: true)
		coverage(nullable:true)
		
	
	}
	
	static mapping = {
		coverage cascade: "all-delete-orphan"
		uFile cascade: "all-delete-orphan"
		
	}
}
