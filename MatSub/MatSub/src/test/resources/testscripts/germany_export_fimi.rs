--assumes to be run from project root folder
dataFile="src//main//resources//data//germany//data.txt"
attributesFile="src//main//resources//data//germany//attributes.txt"
groupsFile="src//main//resources//data//germany//groups.txt"
attributeMappers=DFT_ATTR_TO_STMNT_MAPS-{IRREGULAR_4_CUTOFFS_CLUSTERING}|{IRREGULAR_6_CUTOFFS_CLUSTERING}

add csvimport of dataFile with attributes=attributesFile groups=groupsFile id="germany"
add statements of germany with mappers=attributeMappers id="propositions"

export fimidata of propositions "data.fimi"
export fiminames of propositions "data.names"
