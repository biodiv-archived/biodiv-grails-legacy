import species.namelist.Utils;

def runReportGeneration() {
	println "=========START========= " + new Date()
	Utils.generateColStats("/apps/git/biodiv/col_8May")
	//Utils.downloadColXml("/home/rahulk/col_8May");
	//Utils.testObv()
	println "=========END========= " + new Date()
}
runReportGeneration()
