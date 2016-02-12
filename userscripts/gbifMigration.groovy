import groovy.sql.Sql
def sessionFactory = ctx.getBean("sessionFactory");
def hibSession = sessionFactory?.getCurrentSession();
def ds = ctx.getBean('dataSource');
def sql = new Sql(ds);
def map = [:];
sql.rows("select r.id as id,r.name as name,r.taxon_concept_id as taxon_concept_id from recommendation r join (select name,taxon_concept_id, count(*) from  recommendation where is_scientific_name = 't' group by name,taxon_concept_id having count(*) > 1 order by name,taxon_concept_id) g on (r.name=g.name and r.taxon_concept_id=g.taxon_concept_id and g.taxon_concept_id is not null) or (r.name=g.name and g.taxon_concept_id is null) order by r.id").each {
    String key = it.name+"_"+ it.taxon_concept_id;
    if(!map[key]) map[key] = [];
    map[key] << it.id;
}
map.each { key, rList   ->
    if(rList.size() > 1) {
        for(int i=1; i<rList.size(); i++) {
            sql.executeUpdate("update observation set max_voted_reco_id = ${rList[0]} where max_voted_reco_id=${rList[i]}");
            sql.executeUpdate("update recommendation_vote set recommendation_id = ${rList[0]} where recommendation_id=${rList[i]}");
            sql.executeUpdate("delete from recommendation where id=${rList[i]}");
        }
    }
}


