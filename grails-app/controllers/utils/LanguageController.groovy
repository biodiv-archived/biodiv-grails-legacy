package utils;

import species.Language;
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

class LanguageController {
    
    static defaultAction = "list"

    def index(){ 
        redirect (params:params)
    }

	def list() {
        render Language.list() as JSON;
	}

	def filteredList() {
        render Language.filteredList() as JSON;
	}

    def show() {
        render Language.read(params.long('id')) as JSON;
    }

    def name() {
        render Language.findByName(params.name) as JSON;
    }
}	
