package happy.reportes

import happy.seguridad.Persona
import happy.tramites.Departamento
import happy.tramites.EstadoTramite
import happy.tramites.PersonaDocumentoTramite
import happy.tramites.RolPersonaTramite
import happy.tramites.Tramite

class RetrasadosWebController extends happy.seguridad.Shield {

    static scope = "session"
    def reportesPdfService
    def maxLvl = null

    def reporteRetrasadosConsolidadoDir() {
        def datosGrafico = [:]
        def estadoR = EstadoTramite.findByCodigo("E004")
        def estadoE = EstadoTramite.findByCodigo("E003")
        def rolPara = RolPersonaTramite.findByCodigo("R001")
        def rolCopia = RolPersonaTramite.findByCodigo("R002")
        def now = new Date()

        def datos = [:]
        def usuario = null
        def deps = []
        def puedeVer = []
        def extraPersona = "and "

        def depStr = ""
        if (params.dpto) {
            def departamento = Departamento.get(params.dpto)
            def padre
            padre = departamento.padre
            while (padre) {
                deps.add(padre)
                padre = padre.padre
            }
            deps.add(departamento)
            puedeVer.add(departamento)
            def hi = Departamento.findAllByPadre(padre)
            while (hi.size() > 0) {
                puedeVer += hi
                hi = Departamento.findAllByPadreInList(hi)
            }
        }
//        println "deps "+deps+"  puede ver  "+puedeVer
        def pdt = PersonaDocumentoTramite.findAll("from PersonaDocumentoTramite where" +
                " fechaEnvio is not null " +
                "and rolPersonaTramite in (${rolPara.id},${rolCopia.id}) " +
                "and estado in (${estadoR.id},${estadoE.id}) ${usuario ? extraPersona : ''} ")

        if (pdt) {
            pdt.each { pd ->
                pd.refresh()
                if (pd.tramite.externo != "1" || pd.tramite == null) {
                    def resp = Tramite.findAllByAQuienContesta(pd)
                    if (resp.size() == 0) {
                        if (pd.fechaLimite < now || (!pd.fechaRecepcion))
                            datos = reportesPdfService.jerarquia(datos, pd)
                    }
                }
            }
        }

        def total = 0
        def totalSr = 0
        def hijos = datos["hijos"]

        def tabla = "<table class='table table-bordered table-condensed table-hover'>"
        tabla += "<thead>"
        tabla += "<tr>"
        tabla += "<th width='10%'></th>"
        tabla += "<th width='66%'></th>"
        tabla += "<th width='12%' class='text-warning'>Retrasados</th>"
        tabla += "<th width='12%' class='text-danger'>Sin recepción</th>"
        tabla += "</tr>"
        tabla += "</thead>"

        tabla += "<tbody>"

        hijos.each { lvl ->
//            println "hijo "+lvl
            if (puedeVer.size() == 0 || (puedeVer.id.contains(lvl["objeto"].id))) {
                //  println "primer if"
//                if (lvl["rezagados"]>0 || lvl["retrasados"]>0) {
                //  println "segundo if"
                datosGrafico.put(lvl["objeto"].toString(), [:])
                def dg = datosGrafico[lvl["objeto"].toString()]
                dg.put("rezagados", [:])
                dg.put("retrasados", [:])
                dg.put("totalRz", 0)
                dg.put("totalRs", 0)
                dg.put("objeto", lvl["objeto"])

                def totalNode = 0
                def totalNodeSr = 0

                def usuarios = ""
                def totales = ""
                def totalesSr = ""
                def datosLuz = []

                if (lvl["tramites"].size() > 0) {
                    lvl["triangulos"].each { t ->
                        usuarios += "${t} (Oficina)<br/>"
                        totales += "${lvl["rezagados"]} <br/>"
                        totalesSr += "" + lvl["retrasados"] + " <br/>"
                        datosLuz.add(["${t} (Oficina)", "${lvl.rezagados}", "${lvl.retrasados}", "Oficina"])
                        if (totalNode == 0) {
                            totalNode += lvl["rezagados"].toInteger()
                            dg["rezagados"].put("Oficina", lvl["rezagados"].toInteger())
                        }
                        if (totalNodeSr == 0) {
                            totalNodeSr += lvl["retrasados"].toInteger()
                            dg["retrasados"].put("Oficina", lvl["retrasados"].toInteger())
                        }
                    }
                }
                lvl["personas"].each { p ->
                    usuarios += "${p['objeto']} <br/>"
                    totales += "${p["rezagados"]} <br/>"
                    totalesSr += "" + p["retrasados"] + " <br/>"
                    dg["rezagados"].put(p['objeto'], p["rezagados"].toInteger())
                    datosLuz.add(["${p.objeto}", "${p.rezagados}", "${p.retrasados}", "${p.objeto.login}"])
                    dg["retrasados"].put(p['objeto'], p["retrasados"].toInteger())
                    totalNode += p["rezagados"].toInteger()
                    totalNodeSr += p["retrasados"].toInteger()
                }

                tabla += "<tr class='data dir ${lvl.rezagados > 0 ? 'rz' : ''} ${lvl.retrasados > 0 ? 'rs' : ''}' " +
                        "data-tipo='dir' data-value='${lvl.objeto.codigo}' data-rz='${lvl.rezagados}' data-rs='${lvl.retrasados}'>"
                tabla += "<td class='titulo'>Dirección</td>"
                tabla += "<td class='titulo'>${lvl.objeto} (${lvl.objeto.codigo})</td>"
                tabla += "<td class='titulo numero'>${lvl["rezagados"]}</td>"
                tabla += "<td class='titulo numero'>${lvl["retrasados"]}</td>"
                tabla += "</tr>"

//                    tabla += "<tr>"
//                    tabla += "<td class='titulo'>Usuario</td>"
//                    tabla += "<td>${usuarios}</td>"
//                    tabla += "<td class='numero'>${totales}</td>"
//                    tabla += "<td class='numero'>${totalesSr}</td>"
//                    tabla += "</tr>"
//                    tabla += "<tr>"
//                    tabla += "<td class='titulo' rowspan='${datosLuz.size() + 1}'>Usuario</td>"
//                    tabla += "</tr>"
//                    datosLuz.each { d ->
//                        tabla += "<tr class='data per ${d[1] > 0 ? 'rz' : ''} ${d[2] > 0 ? 'rs' : ''}' data-tipo='per' data-value='${d[3]}' data-rz='${d[1]}' data-rs='${d[2]}'>"
//                        tabla += "<td>${d[0]}</td>"
//                        tabla += "<td class='numero'>${d[1]}</td>"
//                        tabla += "<td class='numero'>${d[2]}</td>"
//                        tabla += "</tr>"
//                    }

                dg["totalRz"] = totalNode
                dg["totalRs"] = totalNodeSr

                total += totalNode
                totalSr += totalNodeSr
//                }
            }
            def res = imprimeHijosPdfConsolidadoDir(lvl, params, usuario, deps, puedeVer, total, totalSr, datosGrafico)
            total += res[0]
            totalSr += res[1]
            tabla += res[2]
        }
        tabla += "</tbody>"

        tabla += "<tfoot>"
        tabla += "<tr>"
        tabla += "<th colspan='2' class='titulo'>Gran Total</th>"
        tabla += "<th class='titulo numero'>${datos['rezagados']}</th>"
        tabla += "<th class='titulo numero'>${datos['retrasados']}</th>"
        tabla += "</tr>"
        tabla += "</tfoot>"

        tabla += "</table>"

        params.detalle = 1
        def inicio = false
        if (params.inicio)
            inicio = true

        print("datos " + datos["objeto"] + "  " + datos["rezagados"] + "  " + datos["retrasados"])
        return [tabla: tabla, params: params, inicio: inicio]
    }

    def imprimeHijosPdfConsolidadoDir(arr, params, usuario, deps, puedeVer, total, totalSr, datosGrafico) {
        def tabla = ""
        total = 0
        totalSr = 0
        def datos = arr["hijos"]
        datos.each { lvl ->
//            println "hijo imprime "+lvl
            if (puedeVer.size() == 0 || (puedeVer.id.contains(lvl["objeto"].id))) {
                datosGrafico.put(lvl["objeto"].toString(), [:])
                def dg = datosGrafico[lvl["objeto"].toString()]
                dg.put("rezagados", [:])
                dg.put("retrasados", [:])
                dg.put("totalRz", 0)
                dg.put("totalRs", 0)
                dg.put("objeto", lvl["objeto"])

                def totalNode = 0
                def totalNodeSr = 0

                def usuarios = ""
                def totales = ""
                def totalesSr = ""
                def datosLuz = []

                if (lvl["tramites"].size() > 0) {
                    lvl["triangulos"].each { t ->
                        usuarios += "${t} (Oficina)<br/>"
                        totales += "${lvl["rezagados"]} <br/>"
                        totalesSr += "" + lvl["retrasados"] + " <br/>"
                        datosLuz.add(["${t} (Oficina)", "${lvl.rezagados}", "${lvl.retrasados}", "Oficina"])
                        if (totalNode == 0) {
                            totalNode += lvl["rezagados"].toInteger()
                            dg["rezagados"].put("Oficina", lvl["rezagados"].toInteger())
                        }
                        if (totalNodeSr == 0) {
                            totalNodeSr += lvl["retrasados"].toInteger()
                            dg["retrasados"].put("Oficina", lvl["retrasados"].toInteger())
                        }
                    }
                }
                lvl["personas"].each { p ->
                    usuarios += "${p['objeto']} <br/>"
                    totales += "${p["rezagados"]} <br/>"
                    totalesSr += "" + p["retrasados"] + " <br/>"
                    datosLuz.add(["${p.objeto}", "${p.rezagados}", "${p.retrasados}", "${p.objeto.login}"])
                    dg["rezagados"].put(p['objeto'], p["rezagados"].toInteger())
                    dg["retrasados"].put(p['objeto'], p["retrasados"].toInteger())
                    totalNode += p["rezagados"].toInteger()
                    totalNodeSr += p["retrasados"].toInteger()
                }

                if (lvl.rezagados > 0 || lvl.retrasados > 0) {
                    tabla += "<tr class='data dep ${lvl.rezagados > 0 ? 'rz' : ''} ${lvl.retrasados > 0 ? 'rs' : ''}'" +
                            " data-tipo='dep' data-value='${lvl.objeto.codigo}' data-rz='${lvl.rezagados}' data-rs='${lvl.retrasados}'>"
                    tabla += "<td class='titulo'>**Departamento</td>"
                    tabla += "<td class='titulo'>${lvl.objeto} (${lvl.objeto.codigo})</td>"
                    tabla += "<td class='titulo numero'>${lvl['rezagados']}</td>"
                    tabla += "<td class='titulo numero'>${lvl['retrasados']}</td>"
                    tabla += "</tr>"
                }

//                tabla += "<tr>"
//                tabla += "<td class='titulo'>Usuario</td>"
//                tabla += "<td>${usuarios}</td>"
//                tabla += "<td class='numero'>${totales}</td>"
//                tabla += "<td class='numero'>${totalesSr}</td>"
//                tabla += "</tr>"
//                tabla += "<tr>"
//                tabla += "<td class='titulo' rowspan='${datosLuz.size() + 1}'>Usuario</td>"
//                tabla += "</tr>"
//                datosLuz.each { d ->
//                    tabla += "<tr class='data per ${d[1] > 0 ? 'rz' : ''} ${d[2] > 0 ? 'rs' : ''}' data-tipo='per' data-value='${d[3]}' data-rz='${d[1]}' data-rs='${d[2]}'>"
//                    tabla += "<td>${d[0]}</td>"
//                    tabla += "<td class='numero'>${d[1]}</td>"
//                    tabla += "<td class='numero'>${d[2]}</td>"
//                    tabla += "</tr>"
//                }

                total += totalNode
                totalSr += totalNodeSr
//                println "total "+total+"   "+totalNode
            }

            if (lvl["hijos"].size() > 0) {
                def res = imprimeHijosPdfConsolidadoDir(lvl, params, usuario, deps, puedeVer, total, totalSr, datosGrafico)
                total += res[0]
                totalSr += res[1]
                tabla += res[2]
            }
//            println "total des dentro "+total+"   "
        }
        return [total, totalSr, tabla]
    }

    def reporteRetrasadosConsolidado() {
        //println("params retra " + params)
        def datosGrafico = [:]
        def estadoR = EstadoTramite.findByCodigo("E004")
        def estadoE = EstadoTramite.findByCodigo("E003")
        def rolPara = RolPersonaTramite.findByCodigo("R001")
        def rolCopia = RolPersonaTramite.findByCodigo("R002")
        def now = new Date()

        def datos = [:]
        def usuario = null
        def deps = []
        def puedeVer = []
        def extraPersona = "and "
        if (params.prsn) {
            usuario = Persona.get(params.prsn)
            extraPersona += "persona=" + usuario.id + " "
            if (usuario.esTriangulo)
                extraPersona = "and (persona=${usuario.id} or departamento = ${usuario.departamento.id})"
            def padre = usuario.departamento.padre
            while (padre) {
                deps.add(padre)
                padre = padre.padre
            }
            deps.add(usuario.departamento)
            puedeVer.add(usuario.departamento)
            def hi = Departamento.findAllByPadre(usuario.departamento)
            while (hi.size() > 0) {
                puedeVer += hi
                hi = Departamento.findAllByPadreInList(hi)
            }

        }
        def depStr = ""
        if (params.dpto) {
            def departamento = Departamento.get(params.dpto)
            println "dep "+departamento
            def padre
            def pers= Persona.findAllByDepartamento(departamento)

            padre = departamento.padre
            while (padre) {
                deps.add(padre)
                padre = padre.padre
            }


            deps.add(departamento)
            puedeVer.add(departamento)
            def hi = Departamento.findAllByPadre(departamento)
            while (hi.size() > 0) {
                puedeVer += hi
                hi = Departamento.findAllByPadreInList(hi)
            }
        }
//        println "deps "+deps+"  puede ver  "+puedeVer
        def pdt = PersonaDocumentoTramite.findAll("from PersonaDocumentoTramite where" +
                " fechaEnvio is not null " +
                "and rolPersonaTramite in (${rolPara.id},${rolCopia.id}) " +
                "and estado in (${estadoR.id},${estadoE.id}) ${usuario ? extraPersona : ''}   ")

        if (pdt) {
            pdt.each { pd ->
                pd.refresh()
                if (pd.tramite.externo != "1" || pd.tramite == null) {
                    def resp = Tramite.findAllByAQuienContesta(pd)
                    if (resp.size() == 0) {
                        if (pd.fechaLimite < now || (!pd.fechaRecepcion)) {
                            //println "pdt -> "+pd.tramite.codigo+"  "+pd.estado.descripcion+" "+pd.fechaRecepcion+"  ||||  "+pd.fechaLimite+" "+" ||  "+pd.persona?.departamento?.codigo+" "+pd.departamento?.codigo
                            datos = reportesPdfService.jerarquia(datos, pd)
                        }
                    }
                }
            }
        }

        def total = 0
        def totalSr = 0
        def hijos = datos["hijos"]
        if ((puedeVer.id.contains(datos["objeto"].id))) {
            maxLvl = datos
        }

        def tabla = "<table class='table table-bordered table-condensed table-hover'>"
        tabla += "<thead>"
        tabla += "<tr>"
        tabla += "<th width='10%'></th>"
        tabla += "<th width='66%'></th>"
        tabla += "<th width='12%' class='text-warning'>Retrasados</th>"
        tabla += "<th width='12%' class='text-danger'>Sin recepción</th>"
        tabla += "</tr>"
        tabla += "</thead>"

        tabla += "<tbody>"



        hijos.each { lvl ->
            // println "lvl "+lvl["objeto"]+"  "+lvl["rezagados"]+"   "+lvl["retrasados"]
            if (puedeVer.size() == 0 || (puedeVer.id.contains(lvl["objeto"].id))) {
                if (maxLvl == null)
                    maxLvl = lvl
//                println "paso ambos if"
                datosGrafico.put(lvl["objeto"].toString(), [:])
                def dg = datosGrafico[lvl["objeto"].toString()]
                dg.put("rezagados", [:])
                dg.put("retrasados", [:])
                dg.put("totalRz", 0)
                dg.put("totalRs", 0)
                dg.put("objeto", lvl["objeto"])

                def totalNode = 0
                def totalNodeSr = 0

                def usuarios = ""
                def totales = ""
                def totalesSr = ""
                def datosLuz = []

                if (lvl["tramites"].size() > 0) {
                    lvl["triangulos"].each { t ->
                        usuarios += "${t} (Oficina)<br/>"
                        totales += "${lvl["ofiRz"]} <br/>"
                        totalesSr += "" + lvl["ofiRs"] + " <br/>"
                        datosLuz.add(["${t} (Oficina)", "${lvl.ofiRz}", "${lvl.ofiRs}", "Oficina"])
                        if (totalNode == 0) {
                            totalNode += lvl["ofiRz"].toInteger()
                            dg["rezagados"].put("Oficina", lvl["ofiRz"].toInteger())
                        }
                        if (totalNodeSr == 0) {
                            totalNodeSr += lvl["ofiRs"].toInteger()
                            dg["retrasados"].put("Oficina", lvl["ofiRs"].toInteger())
                        }
                    }
                }
                lvl["personas"].each { p ->
                    usuarios += "${p['objeto']} <br/>"
                    totales += "${p["rezagados"]} <br/>"
                    totalesSr += "" + p["retrasados"] + " <br/>"
                    dg["rezagados"].put(p['objeto'], p["rezagados"].toInteger())
                    datosLuz.add(["${p.objeto}", "${p.rezagados}", "${p.retrasados}", "${p.objeto.login}"])
                    dg["retrasados"].put(p['objeto'], p["retrasados"].toInteger())
                    totalNode += p["rezagados"].toInteger()
                    totalNodeSr += p["retrasados"].toInteger()
                }

                tabla += "<tr class='data dep ${lvl['rezagados'] > 0 ? 'rz' : ''} ${lvl['retrasados'] > 0 ? 'rs' : ''}' data-tipo='dep' data-value='${lvl.objeto.codigo}' data-rz='${lvl['rezagados']}' data-rs='${lvl['retrasados']}'>"
                tabla += "<td class='titulo'>Dirección</td>"
                tabla += "<td class='titulo'>${lvl.objeto} (${lvl.objeto.codigo})</td>"
                tabla += "<td class='titulo numero'>${lvl['rezagados']}</td>"
                tabla += "<td class='titulo numero'>${lvl['retrasados']}</td>"
                tabla += "</tr>"

//                    tabla += "<tr>"
//                    tabla += "<td class='titulo'>Usuario</td>"
//                    tabla += "<td>${usuarios}</td>"
//                    tabla += "<td class='numero'>${totales}</td>"
//                    tabla += "<td class='numero'>${totalesSr}</td>"
//                    tabla += "</tr>"
                if (datosLuz.size() > 0) {
                    tabla += "<tr>"
                    tabla += "<td  rowspan='${datosLuz.size() + 1}'>Usuario</td>"
                    tabla += "</tr>"
                }
                datosLuz.each { d ->
                    tabla += "<tr class='data per ${d[1] > 0 ? 'rz' : ''} ${d[2] > 0 ? 'rs' : ''}' data-tipo='per' data-value='${d[3]}' data-rz='${d[1]}' data-rs='${d[2]}'>"
                    tabla += "<td>${d[0]}</td>"
                    tabla += "<td class='numero'>${d[1]}</td>"
                    tabla += "<td class='numero'>${d[2]}</td>"
                    tabla += "</tr>"
                }

                dg["totalRz"] = lvl["rezagados"]
                dg["totalRs"] = lvl["retrasados"]

                total += totalNode
                totalSr += totalNodeSr

            }
            def res = imprimeHijosPdfConsolidado(lvl, params, usuario, deps, puedeVer, total, totalSr, datosGrafico)
            total += res[0]
            totalSr += res[1]
            tabla += res[2]
        }
        tabla += "</tbody>"
        if (maxLvl) {
            tabla += "<tfoot>"
            tabla += "<tr>"
            tabla += "<th colspan='2' class='titulo'>TOTAL</th>"
            tabla += "<th class='titulo numero'>${maxLvl['rezagados']}</th>"
            tabla += "<th class='titulo numero'>${maxLvl['retrasados']}</th>"
            tabla += "</tr>"
            tabla += "</tfoot>"
        }
        println "maxLvl "+maxLvl
        tabla += "</table>"

        params.detalle = 1
        def inicio = false
        if (params.inicio)
            inicio = true

        return [tabla: tabla, params: params, inicio: inicio]
    }

    def imprimeHijosPdfConsolidado(arr, params, usuario, deps, puedeVer, total, totalSr, datosGrafico) {
        def tabla = ""
        total = 0
        totalSr = 0
        def datos = arr["hijos"]
        datos.each { lvl ->
            if (puedeVer.size() == 0 || (puedeVer.id.contains(lvl["objeto"].id))) {
                if (maxLvl == null)
                    maxLvl = lvl

                datosGrafico.put(lvl["objeto"].toString(), [:])
                def dg = datosGrafico[lvl["objeto"].toString()]
                dg.put("rezagados", [:])
                dg.put("retrasados", [:])
                dg.put("totalRz", 0)
                dg.put("totalRs", 0)
                dg.put("objeto", lvl["objeto"])

                def totalNode = 0
                def totalNodeSr = 0

                def usuarios = ""
                def totales = ""
                def totalesSr = ""
                def datosLuz = []
                //println "triangulos "
                if (lvl["tramites"].size() > 0) {
                    lvl["triangulos"].each { t ->
//                        println " "+t
                        usuarios += "${t} (Oficina)<br/>"
                        totales += "${lvl["rezagados"]} <br/>"
                        totalesSr += "" + lvl["ofiRz"] + " <br/>"
                        datosLuz.add(["${t} (Oficina)", "${lvl.ofiRz}", "${lvl.ofiRs}", "Oficina"])
                        if (totalNode == 0) {
                            totalNode += lvl["rezagados"].toInteger()
                            dg["rezagados"].put("Oficina", lvl["ofiRz"].toInteger())
                        }
                        if (totalNodeSr == 0) {
                            totalNodeSr += lvl["ofiRs"].toInteger()
                            dg["retrasados"].put("Oficina", lvl["ofiRs"].toInteger())
                        }
                    }
                }
                //println "personas "
                lvl["personas"].each { p ->
//                    println " "+p["objeto"]+"  "+p["rezagados"]+"  "+p["retrasados"]
                    usuarios += "${p['objeto']} <br/>"
                    totales += "${p["rezagados"]} <br/>"
                    totalesSr += "" + p["retrasados"] + " <br/>"
                    datosLuz.add(["${p.objeto}", "${p.rezagados}", "${p.retrasados}", "${p.objeto.login}"])
                    dg["rezagados"].put(p['objeto'], p["rezagados"].toInteger())
                    dg["retrasados"].put(p['objeto'], p["retrasados"].toInteger())
                    totalNode += p["rezagados"].toInteger()
                    totalNodeSr += p["retrasados"].toInteger()
                }

                tabla += "<tr class='data dep ${lvl['rezagados'] > 0 ? 'rz' : ''} ${lvl['rezagados'] > 0 ? 'rs' : ''}' data-tipo='dep' data-value='${lvl.objeto.codigo}' data-rz='${lvl['rezagados']}' data-rs='${lvl['retrasados']}'>"
                tabla += "<td >Departamento</td>"
                tabla += "<td class='titulo'>- ${lvl.objeto} (${lvl.objeto.codigo})</td>"
                tabla += "<td class='titulo numero'>${lvl['rezagados']}</td>"
                tabla += "<td class='titulo numero'>${lvl['retrasados']}</td>"
                tabla += "</tr>"

//                tabla += "<tr>"
//                tabla += "<td class='titulo'>Usuario</td>"
//                tabla += "<td>${usuarios}</td>"
//                tabla += "<td class='numero'>${totales}</td>"
//                tabla += "<td class='numero'>${totalesSr}</td>"
//                tabla += "</tr>"
                if (datosLuz.size() > 0) {
                    tabla += "<tr>"
                    tabla += "<td  rowspan='${datosLuz.size() + 1}'>Usuario</td>"
                    tabla += "</tr>"
                }
                //println "imprime data"
                datosLuz.each { d ->
                    tabla += "<tr class='data per ${d[1] > 0 ? 'rz' : ''} ${d[2] > 0 ? 'rs' : ''}' data-tipo='per' data-value='${d[3]}' data-rz='${d[1]}' data-rs='${d[2]}'>"
                    tabla += "<td>-- ${d[0]}</td>"
                    tabla += "<td class='numero'>${d[1]}</td>"
                    tabla += "<td class='numero'>${d[2]}</td>"
                    tabla += "</tr>"
                }

                total += totalNode
                totalSr += totalNodeSr
//                println "total "+total+"   "+totalNode
            }

            if (lvl["hijos"].size() > 0) {
                // println "tiene hijos ? "
                def res = imprimeHijosPdfConsolidado(lvl, params, usuario, deps, puedeVer, total, totalSr, datosGrafico)
                total += res[0]
                totalSr += res[1]
                tabla += res[2]
            }
//            println "total des dentro "+total+"   "
        }
        return [total, totalSr, tabla]
    }


}
