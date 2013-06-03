import grails.converters.JSON;
import groovyx.net.http.HTTPBuilder;
import groovyx.net.http.ContentType;
import groovyx.net.http.Method;
import species.TaxonomyDefinition;
import species.auth.FacebookUser;
import species.auth.SUser;
import species.participation.Observation;
import species.utils.HttpUtils
import species.utils.ImageUtils
import species.auth.FacebookUser;

def http = new HTTPBuilder()

def rootDir = grailsApplication.config.speciesPortal.users.rootDir.toString()
File userDir = new File(rootDir);
SUser.withTransaction {
    SUser.findAllByProfilePicLike('http://graph.facebook%', [sort:"id"]).each { user ->
        def fbUser = FacebookUser.findByUser(user);
        String relPath = fbUser.uid.toString()+File.separator+"resources"
        File usersDir = new File(userDir, relPath); 
        usersDir.mkdirs();              
        
        File file = HttpUtils.download(user.profilePic, usersDir, true, fbUser.uid+".jpg");
        ImageUtils.createScaledImages(file, usersDir);

        String filePath = file.getAbsolutePath().replace(rootDir,'');
        println filePath;
        user.icon = filePath
        user.save();
    }
}
