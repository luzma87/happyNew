<%--
  Created by IntelliJ IDEA.
  User: luz
  Date: 14/07/14
  Time: 11:18 AM
--%>

<%@ page import="happy.tramites.Tramite; happy.seguridad.Persona; happy.tramites.DocumentoTramite" contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta name="layout" content="main">
        <title>Redireccionar trámites de la bandeja de entrada personal de ${persona.login}</title>
        <style type="text/css">
        td {
            vertical-align : middle !important;
        }

        th {
            text-align     : center;
            vertical-align : middle !important;
        }

        select.loading {
            background : #aaa !important;
        }

        tr.loading td {
            background : #bbb;;
        }

        .table-hover > tbody > tr.loading:hover > td,
        .table-hover > tbody > tr.loading:hover > th {
            background-color : #ccc;
        }

        .select {
            width : 275px;
        }
        </style>
    </head>

    <body>
        <div class="btn-toolbar toolbar" style="margin-top: 10px !important">
            <div class="btn-group">
                <a href="javascript: history.go(-1)" class="btn btn-primary regresar">
                    <i class="fa fa-arrow-left"></i> Regresar
                </a>
            </div>
        </div>

        <h3>Redireccionar trámites de la bandeja de entrada personal de ${persona.login}</h3>

        <div class="alert alert-info">
            <p>
                Para cada trámite que desea redireccionar, seleccione el nuevo destino y presione
                el botón Enviar (<a href="#" class="btn btn-xs btn-success" title="Enviar">
                <i class="fa fa-plane"></i>&nbsp;
            </a>)
            </p>
        </div>

        <table class="table table-bordered table-condensed table-hover">
            <thead>
                <tr>
                    <th>&nbsp;</th>
                    <th>Trámite</th>
                    <th>Fecha envío</th>
                    <th>Fecha recepción</th>
                    <th>De</th>
                    <th>Creado por</th>
                    <th>Fecha límite</th>
                    <th>Rol</th>
                    <th>Estado</th>
                    <th>Nuevo destino</th>
                    <th>Enviar</th>
                </tr>
            </thead>
            <tbody>
                <g:each in="${rows}" var="row" status="i">
                    <g:set var="now" value="${new Date()}"/>

                    <g:set var="estado" value="Por recibir"/>

                    <g:if test="${row.trmtfcrc}">%{-- fecha de recepcion --}%
                        <g:if test="${row.trmtfclr < now}">%{-- fecha limite respuesta --}%
                            <g:set var="estado" value="Retrasado"/>
                        </g:if>
                        <g:else>
                            <g:set var="estado" value="Recibido"/>
                        </g:else>
                    </g:if>
                    <g:else>
                        <g:if test="${row.trmtfcbq && row.trmtfcbq < now}">%{-- fecha bloqueo --}%
                            <g:set var="estado" value="Sin recepción"/>
                        </g:if>
                        <g:else>
                            <g:set var="estado" value="Por recibir"/>
                        </g:else>
                    </g:else>

                    <tr>
                        <td class="text-center">${i + 1}</td>
                        <td class="text-center">
                            <g:if test="${row.tptrcdgo == 'C'}">%{-- tipo tramite cdgo --}%
                                <i class="fa fa-eye-slash" style="margin-left: 10px"></i>
                            </g:if>
                            <g:if test="${row.trmtdctr > 0}">%{-- DocumentoTramite.count --}%
                                <i class="fa fa-paperclip"></i>
                            </g:if>
                            ${row.trmtcdgo}%{-- tramite cdgo --}%
                        </td>
                        <td class="text-center">${row.trmtfcen?.format("dd-MM-yyyy HH:mm")}</td>
                        <td class="text-center">${row.trmtfcrc?.format("dd-MM-yyyy HH:mm")}</td>
                        <td class="text-center">${row.deprdpto}</td>
                        <td class="text-center">
                            <g:if test="${row.dpto__de}">
                                <i class="fa fa-download"></i>
                                ${row.deprdpto} (${row.deprlogn})
                            </g:if>
                            <g:else>
                                <i class="fa fa-user"></i>
                                ${row.deprlogn}
                            </g:else>
                        %{--${row.deprlogn ?: row.deprdscr}--}%
                        </td>
                        <td class="text-center">${row.trmtfclr?.format("dd-MM-yyyy HH:mm")}</td>
                        <td class="text-center">${row.rltrdscr}</td>
                        <td class="text-center">${estado}</td>
                        <td class="text-center">
                            <g:if test="${row.dpto__de}">
                            %{--<g:select class="form-control input-sm select" name="cmbRedirect_${tr.id}" from="${personas}" optionKey="id"/>--}%
                                <g:select class="form-control input-sm select" name="cmbRedirect_${row.trmt__id}" from="${filtradas}" optionKey="id"/>
                            </g:if>
                            <g:else>

                            %{--<g:set var="pers2" value="${personas - tr.tramite.de}"/>--}%
                            %{--<g:set var="pers2" value="${filtradas - tr.tramite.de}"/>--}%
                                <g:set var="pers2" value="${filtradas - filtradas.find { it.login == row.deprlogn }}"/>
                                <g:select class="form-control input-sm select" name="cmbRedirect_${row.trmt__id}" from="${pers2}" optionKey="id"
                                          noSelection="[('-' + dep.id): dep.descripcion]"/>
                            </g:else>
                        </td>
                        <td class="text-center">
                            <a href="#" class="btn btn-xs btn-success btn-move"
                               data-loading-text="<i class='fa fa-spinner fa-spin'></i>"
                               data-id="${row.trmt__id}" title="Enviar">
                                <i class="fa fa-plane"></i>&nbsp;
                            </a>
                        </td>
                    </tr>
                </g:each>
            </tbody>
        </table>

        <script type="text/javascript">
            $(function () {
                $(".btn-move").click(function () {
                    $(".qtip").hide();
                    var $this = $(this);
                    var $tr = $this.parents("tr");
                    var pr = $this.data("id");
                    var $cmb = $("#cmbRedirect_" + pr);
                    var quien = $cmb.val();

                    $this.button('loading');
                    $tr.addClass("loading");
                    $cmb.addClass("loading").prop('disabled', 'disabled');

//                    console.log(pr, quien);
                    $.ajax({
                        type    : "POST",
                        url     : "${createLink(controller: 'tramiteAdmin', action: 'redireccionarTramite_ajax')}",
                        data    : {
                            pr    : pr,
                            quien : quien,
                            id    : "${persona.id}"
                        },
                        success : function (msg) {
                            if (msg == "OK") {
                                $cmb.removeClass("loading").prop('disabled', false);
                                $tr.hide("slow", function () {
                                    $tr.remove();
                                    log("El trámite ha sido redireccionado", "success");
                                });
                            } else {
                                log(msg, "error", "Ha ocurrido un error");
                                $this.button('reset');
                                $tr.removeClass("loading").addClass("danger").effect("pulsate", 3000, function () {
                                    $tr.removeClass("danger");
                                });
                                $cmb.removeClass("loading").prop('disabled', false);
                            }
                        }
                    });
                    return false;
                });
            });
        </script>

    </body>
</html>