import species.namelist.Utils;

def runReportGeneration() {
	println "=========START========= " + new Date()
	Utils.generateColStats("/home/rahulk/col_8May")
	//Utils.downloadColXml("/home/rahulk/col_8May");
	//Utils.testObv()
	println "=========END========= " + new Date()
}
runReportGeneration()
