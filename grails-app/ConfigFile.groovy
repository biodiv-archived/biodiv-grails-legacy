dataSource.username = "postgres"
dataSource.password = "postgres123"

speciesPortal {
    observations {
        filePicker.key = 'AXCVl73JWSwe7mTPb2kXdz'
    }
}
environments {
    development {
        speciesPortal {
            wgp {
                facebook {
                    appId= "424071494335902"
                    secret= "bb87b98979ae30936342364178c7b170"
                }
                supportEmail = "team(at)thewesternghats(dot)in"
            }
            ibp {
                facebook {
                    appId= "347177228674021"
                    secret= "82d91308b5437649bfe891a027205501"
                }
                supportEmail = "support(at)indiabiodiversity(dot)org"
            }
            
        }

    }	
	production {
		speciesPortal {
			wgp {
				facebook {
					appId= "327308053982589"
					secret= "f36074901fc24b904794692755796fd1"
				}
				supportEmail = "team(at)thewesternghats(dot)in"
			}
			ibp {
				facebook {
					appId= "347177228674021"
					secret= "82d91308b5437649bfe891a027205501"
				}
				supportEmail = "support(at)indiabiodiversity(dot)org"
			}
		}
	}

	saturn {
		speciesPortal {
			observations {
                filePicker.key = 'AXCVl73JWSwe7mTPb2kXdz'
			}
			wgp {
				facebook {
					appId= "310694198984953"
					secret= "eedf76e46272190fbd26e578ae764a60"
				}
				supportEmail = "team(at)thewesternghats(dot)in"
			}
			ibp {
				facebook {
					appId= "310694198984953"
					secret= "eedf76e46272190fbd26e578ae764a60"
				}
				supportEmail = "support(at)indiabiodiversity(dot)org"
			}
		}

	}
	pambaTest {
		speciesPortal {
			wgp {
				facebook {
					appId= "310694198984953"
					secret= "eedf76e46272190fbd26e578ae764a60"
				}
				supportEmail = "team(at)thewesternghats(dot)in"
			}
			ibp {
				facebook {
					appId= "310694198984953"
					secret= "eedf76e46272190fbd26e578ae764a60"
				}
				supportEmail = "support(at)indiabiodiversity(dot)org"
			}
		}
	}


	pamba {
        speciesPortal {
            observations {
				filePicker.key = 'Az2MIh1LOQC2OMDowCnioz'
            }
            wgp {
                facebook {
                    appId= "320284831369968"
                    secret= "900d0811194fe28503006b31792690ae"
                }
                supportEmail = "team(at)thewesternghats(dot)in"
            }
            ibp {
                facebook {
                    appId= "320284831369968"
                    secret= "900d0811194fe28503006b31792690ae"
                }
                supportEmail = "support(at)indiabiodiversity(dot)org"
            }
        }

	}
}

