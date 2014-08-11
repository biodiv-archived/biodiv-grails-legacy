package species

import species.auth.SUser

abstract class Cataloglifedata extends NamesSorucedata {

	String lifeZone //enum
	String souceDatabse
	String lsid
	String nameGlobalUniqueIndetifier
	String taxonGlobalUniqueIndetifier
	
	//to store reference backbone i.e col, butterfly by kunte default is col
	String referenceTaxonomyBackbone = "COL"
	//more to be added here 
	
    static constraints = {
		lifeZone nullable:true;
		souceDatabse nullable:true;
		lsid nullable:true;
		nameGlobalUniqueIndetifier nullable:true;
		taxonGlobalUniqueIndetifier nullable:true;
	}

    static mapping = {
        tablePerHierarchy false
    }

	def beforeInsert(){
	}
	
	def beforeUpdate(){
		//overwriting base class method
	}
	
	def updateRefTaxonomyBackbone(String newRef){
		referenceTaxonomyBackbone = newRef
		//propagate this to all its child carefully and save them
	}

}
