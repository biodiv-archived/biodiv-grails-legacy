package species.dataset

import content.eml.UFile;

class Dataset1 extends CollMetadata {
	
    DataPackage dataPackage;
    UFile uFile;	
	static hasMany = [dataTables: DataTable];

    static constraints = {
    }

	static mapping  = {
		id  generator:'org.hibernate.id.enhanced.SequenceStyleGenerator', params:[sequence_name: "dataset_id_seq"]
	}

    int countByDataTable() {
        def result = Dataset1.executeQuery ('''
            select count(*) from Dataset1 d, DataTable dt where d.id=:datasetId and dt.dataset.id=d.id and dt.isDeleted = 'false'
            ''', [datasetId:this.id]);
        return result[0];
    }
}
