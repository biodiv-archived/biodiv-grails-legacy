import species.namelist.Utils;

def runReportGeneration() {
	println "=========START========= " + new Date()
	//Utils.generateColStats("/apps/git/biodiv/col_8May")
	Utils.downloadColXml("/apps/git/biodiv/col_8May/June4");
	//Utils.testObv()
	println "=========END========= " + new Date()
}
runReportGeneration()
