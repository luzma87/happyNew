<script type="text/javascript" src="${resource(dir: 'js', file: 'ui.js')}"></script>

<table class="table table-bordered  table-condensed table-hover">
    <thead>
        <tr>
            <th class="cabecera">Documento</th>
            <th>De</th>
            <th class="cabecera">Fec. Creación</th>
            <th class="cabecera">Para</th>
            <th class="cabecera">Destinatario</th>
            <th class="cabecera">Prioridad</th>
            <th class="cabecera">Fecha Envio</th>
            <th class="cabecera">Fecha Límite</th>
            <th class="cabecera">Estado</th>
            <th class="cabecera">Enviar</th>
        </tr>
    </thead>
    <tbody id="tabla_salida">

        <g:each in="${tramites}" var="tramite">
            <g:set var="limite" value="${tramite.getFechaLimite()}"/>


            <g:set var="esImprimir" value="${false}"/>
            <g:if test="${(happy.tramites.PersonaDocumentoTramite.findAllByPersonaAndTramite(session.usuario, tramite).findAll {
                it.rolPersonaTramite.codigo == 'I005'
            }).size() > 0}">
                <g:set var="esImprimir" value="${true}"/>
            </g:if>


            <tr id="${tramite?.id}" data-id="${tramite?.id}" class="${esImprimir ? 'imprimir' : ''} ${(limite) ? ((limite < new Date()) ? 'alerta' : tramite.estadoTramite.codigo) : tramite.estadoTramite.codigo}" estado="${tramite.estadoTramite.codigo}" de="${tramite.de.id}" codigo="${tramite.codigo}">
                <td title="${tramite.asunto}">${tramite?.codigo}</td>
                <td title="${tramite.de.departamento}">${(tramite.deDepartamento) ? tramite.deDepartamento.codigo : tramite.de}</td>
                <td>${tramite.fechaCreacion?.format("dd-MM-yyyy")}</td>
                <g:set var="para" value="${tramite.getPara()}"/>
                <td>${para?.departamento?.codigo}</td>
                <td>${para?.persona}</td>
                <td>${tramite?.prioridad.descripcion}</td>
                <td>${tramite.fechaEnvio?.format("dd-MM-yyyy HH:mm")}</td>
                <td>${limite ? limite.format("dd-MM-yyyy HH:mm") : ''}</td>
                <td>
                    ${tramite?.estadoTramite.descripcion}
                    <g:if test="${tramite.nota && tramite.nota != ''}">
                        <span class="badge pull-right">
                            <g:link controller="tramite" action="redactar" id="${tramite.id}" title="Con notas de revisión">
                                <i class="fa fa-pencil text-white"></i>
                            </g:link>
                        </span>
                    </g:if>
                </td>
                <g:if test="${tramite?.estadoTramite?.id == 2}">
                    <td id="${tramite?.id}" class="ck"><g:checkBox name="porEnviar" tramite="${tramite?.id}" style="margin-left: 30px" class="form-control combo" checked="false"/></td>
                </g:if>
                <g:else>
                    <td id="${tramite.id}" class="ck"></td>
                </g:else>
            </tr>
        </g:each>
    </tbody>
</table>

%{--<script type="text/javascript">--}%

%{--$(function () {--}%

%{--var tbody = $("#tabla_salida");--}%
%{--var trId = []--}%

%{--tbody.children("tr").each(function () {--}%
%{--console.log("entro" + $(this).attr("id"))--}%
%{--console.log("entro2" + $(this).children("td").children().get(1))--}%
%{--//                if(($(this).children("td").children().get(9).checked) == true){--}%
%{--//                     trId += $(this).attr("id")--}%
%{--//                }else{--}%
%{--//                    console.log("afuera")--}%
%{--//                }--}%



%{--});--}%


%{--})--}%

%{--</script>--}%