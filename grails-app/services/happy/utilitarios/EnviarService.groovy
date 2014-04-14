package happy.utilitarios

import grails.transaction.Transactional
import happy.seguridad.Persona
import happy.tramites.Departamento
import happy.tramites.Tramite
import org.w3c.dom.Document
import org.xhtmlrenderer.extend.FontResolver
import org.xhtmlrenderer.pdf.ITextRenderer

import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

@Transactional


import com.lowagie.text.Element
import com.lowagie.text.Font
import com.lowagie.text.HeaderFooter
import com.lowagie.text.PageSize
import com.lowagie.text.Paragraph
import com.lowagie.text.Phrase
import com.lowagie.text.Rectangle
import com.lowagie.text.pdf.PdfPCell
import com.lowagie.text.pdf.PdfPTable
import com.lowagie.text.pdf.PdfWriter
import happy.seguridad.Persona
import happy.tramites.Departamento
import happy.tramites.Tramite
import org.xhtmlrenderer.extend.FontResolver

import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import java.awt.Color
import java.io.*;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.w3c.dom.Document;

import happy.ElementosTagLib

class EnviarService {

    def crearPdf(Tramite tramite, Persona usuario, String enviar, String type, String editorTramite, String asunto, String realPath, String mensaje) {
        println "crear pdf"

        if (editorTramite) {
            tramite.texto = editorTramite
            tramite.asunto = asunto
            tramite.fechaModificacion = new Date()
            if (tramite.save(flush: true)) {
                def para = tramite.para
                if (tramite.getPara().persona.id) {
                    if (tramite.getPara().persona.id.toLong() > 0) {
                        para.persona = Persona.get(tramite.getPara().persona.id.toLong())
                    } else {
                        para.departamento = Departamento.get(tramite.getPara().persona.id.toLong() * -1)
                    }
                    if (para.save(flush: true)) {
//                println "OK_Trámite guardado exitosamente"
                    } else {
//                        println "NO_Ha ocurrido un error al guardar el destinatario: " + renderErrors(bean: para)
                        println "NO_Ha ocurrido un error al guardar el destinatario: "
                    }
                }
            } else {
//                println "NO_Ha ocurrido un error al guardar el trámite: " + renderErrors(bean: tramite)
                println "NO_Ha ocurrido un error al guardar el trámite: "
            }
        }

        tramite.refresh()

//        def realPath = servletContext.getRealPath("/")
//        def realPath = servletContext.getRealPath("/")
        def pathImages = realPath + "images/"
        def path = pathImages + "redactar/" + usuario.id + "/"

        new File(path).mkdirs()

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();
        FontResolver resolver = renderer.getFontResolver();

        renderer.getFontResolver().addFont(realPath + "fontsPdf/comic.ttf", true);
        resolver.addFont(realPath + "fontsPdf/OpenSans-Bold.ttf", true);
        resolver.addFont(realPath + "fontsPdf/OpenSans-BoldItalic.ttf", true);
        resolver.addFont(realPath + "fontsPdf/OpenSans-ExtraBold.ttf", true);
        resolver.addFont(realPath + "fontsPdf/OpenSans-ExtraBoldItalic.ttf", true);
        resolver.addFont(realPath + "fontsPdf/OpenSans-Italic.ttf", true);
        resolver.addFont(realPath + "fontsPdf/OpenSans-Light.ttf", true);
        resolver.addFont(realPath + "fontsPdf/OpenSans-LightItalic.ttf", true);
        resolver.addFont(realPath + "fontsPdf/OpenSans-Regular.ttf", true);
        resolver.addFont(realPath + "fontsPdf/OpenSans-Semibold.ttf", true);
        resolver.addFont(realPath + "fontsPdf/OpenSans-SemiboldItalic.ttf", true);

        def text = tramite.texto
//        text = util.clean(str: text)
        text = text.decodeHTML()

//        println "html:" + tramite.texto.decodeHTML()
//        println "\n\n" + text

        text = text.replaceAll(~"\\?\\_debugResources=y\\&n=[0-9]*", "")
//        text = text.replaceAll(message(code: 'pathImages'), pathImages)
        text = text.replaceAll(mensaje, pathImages)

        def content = "<!DOCTYPE HTML>\n<html>\n"
        content += "<head>\n"
        content += "<link href=\"${realPath + 'font/open/stylesheet.css'}\" rel=\"stylesheet\"/>"
        content += "<style language='text/css'>\n"
        content += "  @page {\n" +
                "            size   : 21cm 29.7cm;  /*width height */\n" +
                "            margin : 4.5cm 2.5cm 2.5cm 3cm;\n" +
                "        }\n" +
                ".hoja {\n" +
//                "            background  : #123456;\n" +
//                "            width       : 15.5cm; /*21-2.5-3*/\n" +
                "            font-family : arial;\n" +
                "            font-size   : 12pt;\n" +
                "        }\n" +
                ".titulo-horizontal {\n" +
                "    padding-bottom : 15px;\n" +
                "    border-bottom  : 1px solid #000000;\n" +
                "    text-align     : center;\n" +
                "    width          : 105%;\n" +
                "}\n" +
                ".titulo-azul {\n" +
//                "    color       : #0088CC;\n" +
//                "    border      : 0px solid red;\n" +
                "    white-space : nowrap;\n" +
                "    display     : block;\n" +
                "    width       : 98%;\n" +
                "    height      : 30px;\n" +
                "    font-family : 'open sans condensed';\n" +
                "    font-weight : bold;\n" +
                "    font-size   : 25px;\n" +
                "    margin-top  : 10px;\n" +
                "    line-height : 20px;\n" +
                "}\n" +
                ".tramiteHeader {\n" +
                "   width        : 100%;\n" +
                "   border-bottom: solid 1px black;\n" +
                "}\n" +
                "p{\n" +
                "   text-align: justify;\n" +
                "}\n"
        content += "</style>\n"
        content += "</head>\n"
        content += "<body>\n"
        content += "<div class='hoja'>\n"
        content += new ElementosTagLib().headerTramite(tramite: tramite, pdf: true)

        content += text
        content += "</div>\n"
        content += "</body>\n"
        content += "</html>"

        def file = new File(path + tramite.tipoDocumento.descripcion + "_" + tramite.codigo + "_source_" + (new Date().format("yyyyMMdd_HH:mm:ss")) + ".html")
        file.write(content)

        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = builder.parse(file);
        renderer.setDocument(doc, null);
        renderer.layout();
        renderer.createPDF(baos);
        byte[] b = baos.toByteArray();

        file.delete()

        def dpto = ""
        if (enviar == "1") {
//            println("entro enviar")
            def pathPdf = realPath + "tramites/"
            if (tramite.de.departamento && tramite.de.departamento.codigo && tramite.de.departamento.codigo != "") {
                dpto = tramite.de.departamento.codigo
                pathPdf += dpto + "/"
            }
            new File(pathPdf).mkdirs()
            def fileSave = new File(pathPdf + tramite.codigo + ".pdf")
//            println("filesave" + fileSave)
            OutputStream os = new FileOutputStream(fileSave);
            renderer.layout();
            renderer.createPDF(os);
            os.close();
        }

        if (type == "download") {
            println("entro!!!!!")
//            render "OK*" + tramite.codigo + ".pdf"
            return "OK*" + dpto + "/" + tramite.codigo + ".pdf"
        } else {
//            response.setContentType("application/pdf")
//            response.setHeader("Content-disposition", "attachment; filename=" + (tramite.tipoDocumento.descripcion + "_" + tramite.codigo + ".pdf"))
//            response.setContentLength(b.length)
//            response.getOutputStream().write(b)
            return "NO"
        }
    }


}