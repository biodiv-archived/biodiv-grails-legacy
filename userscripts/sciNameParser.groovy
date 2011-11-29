def namesParser = new species.NamesParser();
//def names = namesParser.parse(["Acacia nilotica (L.) Del. ssp. indica (Benth.) Brenan", "Acacia leucophloea ( Roxb.) Willd.", "Canthium dicoccum (Gaertner) Teijsm. & Binnend.", "Dichrostachys cinerea (L.) Wight & Arn. ssp. cinerea var. cinerea", "Salix tetrasperma Roxb. var. terasperma", "Sisyphus (s.str) crispatus hirtus"]);
//def names = namesParser.parse(["Triumfetta annua L. 201", "Terminalia bellerica  (Gaertner) Roxb.  2001"])
def names = namesParser.parse(["Orophea heyneana Hook. f. & Thomson", "Viburnum punctatum Buch.â€“Ham.ex D.Don"]);
names.each {
	println it.italicisedForm;
}
