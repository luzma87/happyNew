package happy.tramites

import happy.seguridad.Persona

class Tramite3Controller {
    def save() {
        /*
            [
                tramite:[
                    padre.id:,
                    padre:[id:],
                    id:,
                    hiddenCC:-10_41_150_1004_,
                    para:-10,
                    tipoDocumento.id:5,
                    tipoDocumento:[id:5],
                    prioridad.id:3,
                    prioridad:[id:3],
                    asunto:fgs dfgsdf
                ],
                origen:[
                   tipoPersona.id:2,
                   tipoPersona:[id:2],
                   cedula:1234523,
                   nombre:sdfg sdfgsd,
                   nombreContacto:sdfgsdfgsdf,
                   apellidoContacto:sdgsdfgsd,
                   titulo:sdfg,
                   cargo:sdfgsdgsdf,
                   mail:235sgdfgs,
                   telefono:sdfgsdfgsdf
                ],

                tramite.asunto:fgs dfgsdf,
                tramite.id:,
                tramite.padre.id:,
                tramite.prioridad.id:3,
                tramite.hiddenCC:,
                tramite.para:-10,
                tramite.tipoDocumento.id:5,

                origen.cargo:sdfgsdgsdf,
                origen.telefono:sdfgsdfgsdf,
                origen.titulo:sdfg,
                origen.nombre:sdfg sdfgsd,
                origen.cedula:1234523,
                origen.mail:235sgdfgs,
                origen.nombreContacto:sdfgsdfgsdf,
                origen.apellidoContacto:sdgsdfgsd,
                origen.tipoPersona.id:2,

                cc:on,
                action:save,
                format:null,
                controller:tramite
            ]
         */
        def persona = Persona.get(session.usuario.id)
        def estadoTramiteBorrador = EstadoTramite.findByCodigo("E001");

        def paramsOrigen = params.remove("origen")
        def paramsTramite = params.remove("tramite")

        paramsTramite.de = persona
        paramsTramite.estadoTramite = estadoTramiteBorrador
        paramsTramite.fecha = new Date()
        paramsTramite.anio = Anio.findByNumero(paramsTramite.fecha.format("yyyy"))
//        paramsTramite.numero = "MEMPRUEBA-0001"
        /* CODIGO DEL TRAMITE:
         *      tipoDoc.codigo-secuencial-dtpoEnvia.codigo-anio(yy)
         *      INF-1-DGCP-14       MEM-10-CEV-13
         */
        //el numero del ultimo tramite del anio, por tipo doc y dpto
        def num = Tramite.withCriteria {
            eq("anio", paramsTramite.anio)
            eq("tipoDocumento", TipoDocumento.get(paramsTramite.tipoDocumento.id))
            de {
                eq("departamento", persona.departamento)
            }
            projections {
                max "numero"
            }
        }
        println
        println num


        def tramite
        def error = false
        if (paramsTramite.id) {
            tramite = Tramite.get(paramsTramite.id)
        } else {
            tramite = new Tramite()
        }
        tramite.properties = paramsTramite

//        if (!tramite.save(flush: true)) {
//            println "error save tramite " + tramite.errors
//            flash.tipo = "error"
//            flash.message = "Ha ocurrido un error al grabar el tramite, por favor, verifique la información ingresada"
//            redirect(action: "crearTramite")
//            return
//        } else {
//
//        }
        render "OK"
    }
}