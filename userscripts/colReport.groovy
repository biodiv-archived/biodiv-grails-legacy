import species.namelist.Utils;

def runReportGeneration() {
	println "=========START========= " + new Date()
	//Utils.generateColStats("/home/rahulk/col_Dec4")
	Utils.downloadColXml("col_feb24");
	//Utils.testObv()
	println "=========END========= " + new Date()
}
runReportGeneration()
