package species

class GeographicEntity extends SpeciesField {

	Country country;
	static mappping = {
        discriminator value:"species.GeographicEntity"
	}
}
