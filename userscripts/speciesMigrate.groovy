import species.Species;
import species.SpeciesField;

import javax.sql.DataSource
import groovy.sql.Sql

def updateContributorOrder() {
    def dataSource =  ctx.getBean("dataSource");
    def sql =  Sql.newInstance(dataSource);

    sql.eachRow('select * from species_field') { sf ->
        int i = 0;
        sql.eachRow("select contributor_id from species_field_contributor where species_field_contributors_id=?", [sf.id]) { cid ->
            sql.execute("update species_field_contributor set contributors_idx= ?,  attributors_idx=? where contributor_id=? and  species_field_contributors_id=?", [i++, null, cid.contributor_id, sf.id]);
        }

        i=0;
        sql.eachRow("select contributor_id from species_field_contributor where species_field_attributors_id=?", [sf.id]) { cid ->
            sql.execute("update species_field_contributor set attributors_idx= ?, contributors_idx=? where contributor_id=? and  species_field_attributors_id=?", [i++,null,cid.contributor_id, sf.id]);
        }
    }
}

updateContributorOrder();
