<div style="margin-top: 10px; height: 450px"  class="container-celdas">
    <span class="grupo">
        <table class="table table-bordered table-striped table-condensed table-hover">
            <thead>
            <tr>
                <th class="cabecera">Documento</th>
                <th class="cabecera">Fecha Recepción</th>
                <th class="cabecera">De</th>
                <th class="cabecera">Creado Por</th>
                <th class="cabecera">Para</th>
                <th class="cabecera">Destinatario</th>
                <th class="cabecera">Prioridad</th>
                <th class="cabecera">Fecha Límite</th>
                <th class="cabecera">Doc. Principal</th>
            </tr>

            </thead>
            <tbody>
            <g:each in="${tramites}" var="tramite">

                <g:set var="type" value=""/>

                <g:if test="${tramite?.estadoTramite?.codigo == 'E002'}">
                    <g:set var="type" value="revisado"/>
                </g:if>

                <g:if test="${tramite?.estadoTramite?.codigo == 'E003'}">
                    <g:set var="type" value="enviado"/>
                </g:if>

                %{--<g:if test="${idTramitesNoRecibidos.contains(tramite.id)}">--}%
                    %{--<g:set var="type" value="noRecibido"/>--}%
                %{--</g:if>--}%

                <tr data-id="${tramite?.id}" class="${type}">
                    <td>${tramite?.codigo}</td>
                    <td></td>
                    <td>${tramite?.de}</td>
                    <td>${tramite?.de?.departamento?.descripcion}</td>
                    <td></td>
                    <td></td>
                    <td>${tramite?.estado}</td>
                    <td>?</td>
                    <td>${tramite?.padre}</td>

                </tr>

            </g:each>

            </tbody>
        </table>

    </span>

</div>


<script type="text/javascript">
    function clean() {
        $(".revisadoColor").removeClass("revisadoColor");
        $(".enviadoColor").removeClass("enviadoColor");
        $(".noRecibidoColor").removeClass("noRecibidoColor");
//        $(".pendienteRojoColor").removeClass("pendienteRojoColor");
    }
    function getRows(clase) {
        clean();
        $("."+clase).addClass(clase+"Color");
    }
</script>