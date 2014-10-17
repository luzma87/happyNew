package happy.reportes

import happy.tramites.Departamento
import happy.tramites.PersonaDocumentoTramite
import happy.tramites.RolPersonaTramite
import happy.tramites.Tramite
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CreationHelper
import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook

class ReporteGestionExcelController {

    def diasLaborablesService

    def reporteGestion() {
        def desde = new Date().parse("dd-MM-yyyy", params.desde)
        def hasta = new Date().parse("dd-MM-yyyy", params.hasta)
        def departamento = Departamento.get(params.id)
        def departamentos = [departamento]

        def fileName = "gestion_" + departamento.codigo
        def titulo = "Reporte de gestión de trámites del dpto. ${departamento.descripcion} del ${params.desde} al ${params.hasta}"

        def downloadName = fileName + "_" + new Date().format("ddMMyyyy_hhmm") + ".xlsx";

        def path = servletContext.getRealPath("/") + "xls/"
        new File(path).mkdirs()
        //esto crea un archivo temporal que puede ser siempre el mismo para no ocupar espacio
        String filename = path + "text.xlsx";
        String sheetName = "Resumen";
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet(sheetName);
        CreationHelper createHelper = wb.getCreationHelper();
        sheet.setAutobreaks(true);

        XSSFRow rowTitle = sheet.createRow((short) 0);
        Cell cellTitle = rowTitle.createCell((short) 0);
        cellTitle.setCellValue("GAD DE LA PROVINCIA DE PICHINCHA");
        rowTitle = sheet.createRow((short) 1);
        cellTitle = rowTitle.createCell((short) 0);
        cellTitle.setCellValue("SISTEMA DE ADMINISTRACION DOCUMENTAL");
        rowTitle = sheet.createRow((short) 2);
        cellTitle = rowTitle.createCell((short) 0);
        cellTitle.setCellValue(titulo);
        rowTitle = sheet.createRow((short) 3);
        cellTitle = rowTitle.createCell((short) 0);
        cellTitle.setCellValue("Reporte generado el " + new Date().format("dd-MM-yyyy HH:mm"));

        if (departamento) {

            def wFechas = 5000
            def wTiempos = 6000

            sheet.setColumnWidth(0, 6000)
            sheet.setColumnWidth(1, wFechas)
            sheet.setColumnWidth(2, wFechas)
            sheet.setColumnWidth(3, wFechas)
            sheet.setColumnWidth(4, wTiempos)
            sheet.setColumnWidth(5, 8000)
            sheet.setColumnWidth(6, 15000)
            sheet.setColumnWidth(7, 8000)
            sheet.setColumnWidth(8, wTiempos)

            def i = 5

            def tramitesPrincipales = []

            PersonaDocumentoTramite.withCriteria {
//                isNotNull('fechaEnvio')
//                eq("departamento", departamento)
                inList("departamento", departamentos)
                or {
                    eq("rolPersonaTramite", RolPersonaTramite.findByCodigo('R001'))
                    eq("rolPersonaTramite", RolPersonaTramite.findByCodigo('R002'))
                }
                tramite {
                    ge('fechaCreacion', desde)
                    le('fechaCreacion', hasta)
                }
            }.each { prtr ->
                def tramite = prtr.tramite
                def principal = tramite
                if (tramite.padre) {
                    principal = tramite.padre
                    while (true) {
                        if (!principal.padre)
                            break
                        else {
                            principal = principal.padre
                        }
                    }
                }
                if (!tramitesPrincipales.contains(principal)) {
                    tramitesPrincipales += principal
                }
            }

            tramitesPrincipales.each { principal ->
                def pdts = PersonaDocumentoTramite.withCriteria {
                    eq("tramite", principal)
                    or {
                        eq("rolPersonaTramite", RolPersonaTramite.findByCodigo('R001'))
                        eq("rolPersonaTramite", RolPersonaTramite.findByCodigo('R002'))
                    }
                }

                def j = 0
                XSSFRow row = sheet.createRow((short) i);
                Cell cell = row.createCell((short) j);
                cell.setCellValue("DOC PRINCIPAL: ");
                j += 2
                cell = row.createCell((short) j);
                cell.setCellValue(principal.codigo);
                j += 2
                cell = row.createCell((short) j);
                cell.setCellValue("ASUNTO: ");
                j++
                cell = row.createCell((short) j);
                cell.setCellValue(principal.asunto);

                i++
                j = 0
                row = sheet.createRow((short) i);
                cell = row.createCell((short) j);
                cell.setCellValue("Trámite n°");
                j++
                cell = row.createCell((short) j);
                cell.setCellValue("F. creación");
                j++
                cell = row.createCell((short) j);
                cell.setCellValue("F. envío");
                j++
                cell = row.createCell((short) j);
                cell.setCellValue("F. recepción");
                j++
                cell = row.createCell((short) j);
                cell.setCellValue("T. envío - recepción");
                j++
                cell = row.createCell((short) j);
                cell.setCellValue("De");
                j++
                cell = row.createCell((short) j);
                cell.setCellValue("Asunto");
                j++
                cell = row.createCell((short) j);
                cell.setCellValue("Para");
                j++
                cell = row.createCell((short) j);
                cell.setCellValue("T. recepción - respuesta");
                i++

                pdts.each { prtr ->
                    if ((departamentos.id).contains(prtr.departamentoId)) {
                        i = rowTramite(prtr, sheet, i)
                    }
                    i = llenaTablaTramite(prtr, departamentos, sheet, i)
                }
                i += 2
            }
        }

        FileOutputStream fileOut = new FileOutputStream(filename);
        wb.write(fileOut);
        fileOut.close();
        String disHeader = "Attachment;Filename=\"${downloadName}\"";
        response.setHeader("Content-Disposition", disHeader);
        File desktopFile = new File(filename);
        PrintWriter pw = response.getWriter();
        FileInputStream fileInputStream = new FileInputStream(desktopFile);
        int j;

        while ((j = fileInputStream.read()) != -1) {
            pw.write(j);
        }
        fileInputStream.close();
        response.flushBuffer();
        pw.flush();
        pw.close();
    }

    def rowTramite(PersonaDocumentoTramite pdt, XSSFSheet sheet, int i) {
        def tramite = pdt.tramite

        println "rowTRamite    " + tramite.codigo + "       i=" + i

        def de, dias, para = "", codigo = tramite.codigo
        if (tramite.deDepartamento) {
            de = tramite.deDepartamento.codigo
        } else {
            de = tramite.de.login + " (${tramite.de.departamento.codigo})"
        }

        def dif
        if (pdt.fechaEnvio) {
            if (pdt.fechaRecepcion) {
                dif = diasLaborablesService.tiempoLaborableEntre(pdt.fechaRecepcion, pdt.fechaEnvio)
            } else {
                dif = diasLaborablesService.tiempoLaborableEntre(pdt.fechaEnvio, new Date())
            }
            if (dif[0]) {
                def d = dif[1]
                if (d.dias > 0) {
                    dias = "${d.dias} día${d.dias == 1 ? '' : 's'}, "
                } else {
                    dias = ""
                }
                dias += "${d.horas} hora${d.horas == 1 ? '' : 's'}, ${d.minutos} minuto${d.minutos == 1 ? '' : 's'}"
            } else {
                println "error: " + dif
            }
        } else {
            dias = "No enviado"
        }


        if (pdt.departamento) {
            para = pdt.departamento.codigo
        } else if (pdt.persona) {
            para = pdt.persona.login + " (${pdt.persona.departamento.codigo})"
        }

        if (pdt.rolPersonaTramite.codigo == "R002") {
            codigo += " [CC]"
        }

        def contestacionRetraso = "Sin respuesta"
        // f. recep - fecha actual (no hay contestacion)
        // f.recep - fecha creacion contestacion mas antigua (hijo mas viejo)
        def respuestas = Tramite.withCriteria {
            eq("aQuienContesta", pdt)
            order("fechaCreacion", "asc")
        }
        def dif2
        if (respuestas.size() > 0) {
            def respuesta = respuestas.last()
            if (pdt.fechaRecepcion && respuesta.fechaCreacion) {
                dif2 = diasLaborablesService.tiempoLaborableEntre(pdt.fechaRecepcion, respuesta.fechaCreacion)
            }
        } else {
            if (pdt.fechaRecepcion) {
                dif2 = diasLaborablesService.tiempoLaborableEntre(pdt.fechaRecepcion, new Date())
            }
        }
        if (dif2) {
            if (dif2[0]) {
                def d = dif2[1]
                if (d.dias > 0) {
                    contestacionRetraso = "${d.dias} día${d.dias == 1 ? '' : 's'}, "
                } else {
                    contestacionRetraso = ""
                }
                contestacionRetraso += "${d.horas} hora${d.horas == 1 ? '' : 's'}, ${d.minutos} minuto${d.minutos == 1 ? '' : 's'}"
            } else {
                println "error: " + dif2
            }
        } else {
            contestacionRetraso = "No recibido"
        }

        /* ********************************************************************************************* */
        def j = 0
        def row = sheet.createRow((short) i);
        def cell = row.createCell((short) j);
        cell.setCellValue(codigo);
        j++
        cell = row.createCell((short) j);
        cell.setCellValue(tramite.fechaCreacion ? tramite.fechaCreacion.format('dd-MM-yyyy HH:mm') : "");
        j++
        cell = row.createCell((short) j);
        cell.setCellValue(pdt.fechaEnvio ? pdt.fechaEnvio.format('dd-MM-yyyy HH:mm') : "");
        j++
        cell = row.createCell((short) j);
        cell.setCellValue(pdt.fechaRecepcion ? pdt.fechaRecepcion.format('dd-MM-yyyy HH:mm') : "");
        j++
        cell = row.createCell((short) j);
        cell.setCellValue(dias);
        j++
        cell = row.createCell((short) j);
        cell.setCellValue(de);
        j++
        cell = row.createCell((short) j);
        cell.setCellValue(tramite.asunto);
        j++
        cell = row.createCell((short) j);
        cell.setCellValue(para);
        j++
        cell = row.createCell((short) j);
        cell.setCellValue(contestacionRetraso);
        /* ********************************************************************************************* */
        i++
        return i
    }

    def llenaTablaTramite(PersonaDocumentoTramite prtr, departamentos, XSSFSheet sheet, int i) {
        def respuestas = Tramite.withCriteria {
            eq("aQuienContesta", prtr)
            order("fechaCreacion", "asc")
        }
        if (respuestas.size() > 0) {
            respuestas.each { h ->
                def rolPara = RolPersonaTramite.findByCodigo("R001")
                def rolCc = RolPersonaTramite.findByCodigo("R002")

                def paras = PersonaDocumentoTramite.findAllByTramiteAndRolPersonaTramite(h, rolPara)
                def ccs = PersonaDocumentoTramite.findAllByTramiteAndRolPersonaTramite(h, rolCc)

                (paras + ccs).each { pdt ->
                    if ((departamentos.id).contains(pdt.departamentoId) || (departamentos.id).contains(pdt.persona?.departamentoId) ||
                            (departamentos.id).contains(pdt.tramite.departamentoId)) {
                        i = rowTramite(pdt, sheet, i)
                    }
                    i = llenaTablaTramite(pdt, departamentos, sheet, i)
                }
            }
        }
        return i
    }

}