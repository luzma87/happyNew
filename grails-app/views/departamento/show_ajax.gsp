
<%@ page import="happy.tramites.Departamento" %>

<g:if test="${!departamentoInstance}">
    <elm:notFound elem="Departamento" genero="o" />
</g:if>
<g:else>

    <g:if test="${departamentoInstance?.codigo}">
        <div class="row">
            <div class="col-md-3 text-info">
                Código
            </div>

            <div class="col-md-9">
                <g:fieldValue bean="${departamentoInstance}" field="codigo"/>
            </div>

        </div>
    </g:if>

    <g:if test="${departamentoInstance?.descripcion}">
        <div class="row">
            <div class="col-md-3 text-info">
                Descripción
            </div>

            <div class="col-md-9">
                <g:fieldValue bean="${departamentoInstance}" field="descripcion"/>
            </div>

        </div>
    </g:if>

%{--
    <g:if test="${departamentoInstance?.tipoDepartamento}">
        <div class="row">
            <div class="col-md-3 text-info">
                Tipo Departamento
            </div>

            <div class="col-md-9">
                ${departamentoInstance?.tipoDepartamento?.descripcion}
            </div>

        </div>
    </g:if>
--}%

    <g:if test="${departamentoInstance?.padre}">
        <div class="row">
            <div class="col-md-3 text-info">
                Dirección/Secretaría:
            </div>

            <div class="col-md-9">
                ${departamentoInstance?.padre?.encodeAsHTML()}
            </div>

        </div>
    </g:if>


    <g:if test="${departamentoInstance?.telefono}">
        <div class="row">
            <div class="col-md-3 text-info">
                Teléfono
            </div>

            <div class="col-md-9">
                <g:fieldValue bean="${departamentoInstance}" field="telefono"/>
            </div>

        </div>
    </g:if>

    <g:if test="${departamentoInstance?.extension}">
        <div class="row">
            <div class="col-md-3 text-info">
                Extensión
            </div>

            <div class="col-md-9">
                <g:fieldValue bean="${departamentoInstance}" field="extension"/>
            </div>

        </div>
    </g:if>

    <g:if test="${departamentoInstance?.direccion}">
        <div class="row">
            <div class="col-md-3 text-info">
                Dirección
            </div>
            
            <div class="col-md-9">
                <g:fieldValue bean="${departamentoInstance}" field="direccion"/>
            </div>
            
        </div>
    </g:if>

    <g:if test="${personal}">

        <div class="row">
            <div class="col-md-3 text-info">
                Recepción Dep.
            </div>

            <g:each in="${personal}" var="persona">
                <div class="col-md-9">
                    <g:fieldValue bean="${persona}" field="nombre"/> <g:fieldValue bean="${persona}" field="apellido"/>
                </div>
            </g:each>


        </div>
    </g:if>
    
</g:else>