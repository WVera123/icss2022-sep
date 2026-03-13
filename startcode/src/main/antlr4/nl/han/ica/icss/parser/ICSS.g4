grammar ICSS;

//--- LEXER: ---

// IF support:
IF: 'if';
ELSE: 'else';
BOX_BRACKET_OPEN: '[';
BOX_BRACKET_CLOSE: ']';


//Literals
TRUE: 'TRUE';
FALSE: 'FALSE';
PIXELSIZE: [0-9]+ 'px';
PERCENTAGE: [0-9]+ '%';
SCALAR: [0-9]+;

//Color value takes precedence over id idents
COLOR: '#' [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f];

//Specific identifiers for id's and css classes
ID_IDENT: '#' [a-z0-9\-]+;
CLASS_IDENT: '.' [a-z0-9\-]+;

//General identifiers
LOWER_IDENT: [a-z] [a-z0-9\-]*;
CAPITAL_IDENT: [A-Z] [A-Za-z0-9_]*;

//All whitespace is skipped
WS: [ \t\r\n]+ -> skip;

//
OPEN_BRACE: '{';
CLOSE_BRACE: '}';
SEMICOLON: ';';
COLON: ':';
PLUS: '+';
MIN: '-';
MUL: '*';
ASSIGNMENT_OPERATOR: ':=';

//--- PARSER: ---
stylesheet: (variableAssignment | stylerule)* EOF;

// Var := 10px;
variableAssignment: variableReference ASSIGNMENT_OPERATOR expression SEMICOLON;

expression: literal | variableReference;

// a { color: #ff0000; }
stylerule: selector body;
// a, .menu, #menu
selector: LOWER_IDENT | CLASS_IDENT | ID_IDENT;

body: OPEN_BRACE (declaration)* CLOSE_BRACE;

//width: 100px;
declaration: propertyName COLON expression SEMICOLON;

literal: TRUE | FALSE | COLOR | PERCENTAGE | PIXELSIZE | SCALAR;

propertyName: LOWER_IDENT;
variableReference: CAPITAL_IDENT;