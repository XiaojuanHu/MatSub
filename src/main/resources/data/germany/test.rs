attributeMappers=DFT_ATTR_TO_STMNT_MAPS-{IRREGULAR_4_CUTOFFS_CLUSTERING}|{IRREGULAR_6_CUTOFFS_CLUSTERING}

add csvimport of "data.txt" with attributes="attributes.txt" groups="groups.txt" id="germany" name="The socio-economics of Germany"
add statements of germany with mappers=attributeMappers id="propositions"

run ASSOCIATION_BEAMSEARCH "results"
export results "output.txt"
