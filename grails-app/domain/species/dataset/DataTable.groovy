package species.dataset

import content.eml.UFile;
import content.eml.Document;
import species.Species;
import species.SpeciesField;
import species.dataset.Dataset.DatasetType;
import species.participation.Observation;
import species.trait.Trait;
import content.eml.UFile;
import species.dataset.DataPackage.DataTableType;

class DataTable extends CollMetadata {
	
	
//	DataTableType type;
	boolean agreeTerms = false;
	static hasOne = [dataset : Dataset1]
    UFile uFile;
    DataTableType dataTableType;

	static constraints = {
		agreeTerms nullable:true
	}
	
	static mapping = {
		tablePerHierarchy false;
		id  generator:'org.hibernate.id.enhanced.SequenceStyleGenerator', params:[sequence_name: "datatable_id_seq"]
	}
}

class ObservationDataTable extends DataTable {
	static hasMany = [observations:Observation]
}

class SpeciesDataTable extends DataTable {
	static hasMany = [species:Species]
}

class SpeciesFieldDataTable extends DataTable {
	static hasMany = [speciesFields:SpeciesField]
}

class DocumentsDataTable extends DataTable {
	static hasMany = [documents:Document]
}

class TraitsDataTable extends DataTable {
	static hasMany = [traits:Trait]
}
