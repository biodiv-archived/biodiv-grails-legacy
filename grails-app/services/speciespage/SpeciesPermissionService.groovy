package speciespage

import java.util.Date;
import java.util.List;
import java.util.Map;

import species.TaxonomyDefinition;
import species.auth.SUser;
import species.SpeciesPermission;

class SpeciesPermissionService {

    static transactional = true

    def serviceMethod() {

    }

    List<SUser> getCurators(species) {
        def taxonConcept = species.taxonConcept;
        List parentTaxons = taxonConcept.parentTaxon()
        def res = []
        parentTaxons.each { pt->
            res.add(SpeciesPermission.findAllWhere(taxonConcept: pt, permissionType: PermissionType.ROLE_CURATOR))
        }
        def result = []
        res.each { r->
            result.add(r.author)            
        }
        return result
    }

    List<SUser> getContributors(species) {
        def taxonConcept = species.taxonConcept;
        List parentTaxons = taxonConcept.parentTaxon()
        def res = []
        parentTaxons.each { pt->
            res.add(SpeciesPermission.findAllWhere(taxonConcept: pt, permissionType: PermissionType.ROLE_CONTRIBUTOR))
        }
        def result = []
        res.each { r->
            result.add(r.author)            
        }
        return result
    }

    boolean addCurator(SUser author, List<TaxonomyDefinition> taxons) {
        def sp
        taxons.each { taxon ->
            try{
                sp = new SpeciesPermission(author: author, taxonConcept: taxon, permissionType: PermissionType.ROLE_CURATOR);
                if(!sp.save(flush:true)){
                    sp.errors.allErrors.each { log.error it }
                    return false
                }
            } catch (Exception e) {
                status = false;
                msg = "Error: ${e.getMessage()}";
                e.printStackTrace()
                log.error e.getMessage();
            } 
        }
        return true
    }

    boolean addContributor(SUser author, List<TaxonomyDefinition> taxons) {
        def sp
        taxons.each{ taxon ->
            try{
                sp = new SpeciesPermission(author: author, taxonConcept: taxon, permissionType: PermissionType.ROLE_CONTRIBUTOR);
                if(!sp.save(flush:true)){
                    sp.errors.allErrors.each { log.error it }
                    return false
                }
            } catch (Exception e) {
                status = false;
                msg = "Error: ${e.getMessage()}";
                e.printStackTrace()
                log.error e.getMessage();
            }
        }
        return true
    }
}
