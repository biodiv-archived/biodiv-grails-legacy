package species.participation.curation

import java.util.Date;

import species.auth.SUser;
import species.participation.Observation

class UnCuratedVotes {
	SUser author;
	Date votedOn = new Date();
	Observation obv;
	UnCuratedScientificNames sciName;
	UnCuratedCommonNames commonName;

	static constraints = {
		author(nullable:false, unique:[
			'author',
			'sciName',
			'commonName',
			'obv'
		]);
		votedOn validator : {val -> val < new Date()};
		obv(nullable:false);
		sciName(nullable:true);
		commonName(nullable:true);
	}

	static mapping = { version false; }

	//duplicate check will be done on 'author', 'sciName', 'commonName'

	//on each row curation decrement sn and cn counter by 1  and delete them if the reaches to 0.

	//function
	//1. for given SN can know its standard or not
	//2. 		can get all uncurated common names
	//3. can store sn and cn in all combination with ref to reco
	//4. get all uncurated recos grouped by user/observation.

}
