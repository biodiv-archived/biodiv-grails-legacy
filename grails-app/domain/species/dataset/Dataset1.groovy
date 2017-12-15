package species.dataset

import content.eml.UFile;

class Dataset1 extends CollMetadata {
	
    DataPackage dataPackage;
    UFile uFile;	
	static hasMany = [dataTables: DataTable];

	static mapping = {
		id  generator:'org.hibernate.id.enhanced.SequenceStyleGenerator', params:[sequence_name: "dataset_id_seq"]
	}
}
