<html>
<head>
<title>${title}</title>
<style>
body {
	font-family: 'DejaVu Sans', Arial, Helvetica, sans-serif;
	font-size: 14px;
}

table {
	border-collapse: collapse;
	font-size: 14px;
	width: 90%;
}

td, th {
    border: 1px solid #dddddd;
    text-align: left;
    vertical-align: top;
    padding: 8px;
}

tr:nth-child(even) {
    background-color: #dddddd;
}

caption { 
	padding-top: 5px;
    padding-left: 12px;
    padding-right: 12px;
    padding-bottom: 7px;
    display: inline-block;
 
    float: left;
    background-color: #F8981D;
	color: #253441;
    font-weight: bold;
    height: 14px;
    float: left;
}
</style>
</head>

<body>
  <h1>${title}</h1>
  <h2>${type.name}<#if (type.parameters?size>0)>(${type.parameters?join(", ")})</#if></h2>
  <p>${type.description}</p>
  <#if (type.parameters?size>0)>
  <h3>parameters</h3>
  <#list type.parameters as p>
  	${p.name} - ${p.description} 
  </#list>
  </#if>
  <h3>supertypes</h3> 
    <#list type.supertypes as supertype>
    	<a href="${supertype}.html">${supertype}</a><#sep>, </#sep> 
    <#else>
    	<p>none</p>
    </#list>
  <h3>subtypes</h3> 
    <#list type.subtypes as subtype>
    	<a href="${subtype}.html">${subtype}</a><#sep>, </#sep>
    <#else>
    	<p>none</p>
    </#list>  
  <h3>fields</h3> 
  <table>
  	<tbody>
  		<tr>
  			<th>name</th>
  			<th>type</th>
  			<th>description</th>
  		</tr>
    	<#list type.fields as field>
      		<tr>
      			<td><#if field.optional><i></#if>${field.name}<#if field.optional></i></#if></td>
      			<td>
      				<a <#if !field.typeExternal>href="${field.type}.html"</#if>>${field.type}</a><#if (field.parameters?size>0)>(<#list field.parameters as p><a <#if !p.external>href="${p.value}.html"</#if>>${p.value}</a><#if p?hasNext>,</#if></#list>)</#if>
      			</td>
      			<td>${field.description}</td>
      		</tr> 
    	</#list>
  	</tbody>
  </table>

</body>
</html>