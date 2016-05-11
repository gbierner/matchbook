/*
 * Copyright (c) 2014, Gann Bierner
 */
grammar Matchbook;

@parser::header {
    package org.bierner.matchbook.parser;
}

@lexer::header {
    package org.bierner.matchbook.parser;
}

expression:  or EOF;

or: is (OR is)*;

is: isnt (IS isnt)*;

isnt: sequence (ISNT sequence)?;

sequence: repeat+;

repeat:
    capture                           #RepeatNone
  | capture '?'                       #Repeat0or1
  | capture '[' NUMBER ']'            #RepeatN
  | capture '[' NUMBER ':' NUMBER ']' #RepeatNtoM
;

capture: (LOWERCASE '=')? atom;

atom: 
    LOWERCASE     #Stems
  | STEMS         #Stems
  | STRICT_STEMS  #StrictStems
  | TOKENS        #Tokens
  | EXACT_CONCEPT #ExactConcept
  | CONCEPT       #Concept
  | CHUNK         #Chunk
  | POS           #Pos
  | REGEX         #Regex
  | START         #Start
  | END           #End
  | ANNOTATION    #Annotation
  | ANNOTATION WITH or #With
  | ANNOTATION WITH '(' or (',' or)* ')'  #WithList
  | '(' or ')'   #Expr
;


START:'START';
END  :'END';
OR   :'OR'; 
IS   :'IS'; 
ISNT :'ISNT';
WITH :'WITH';

LOWERCASE :   [a-z]+;
ANNOTATION:   [A-Z]+(':'[a-zA-Z]+)?;
TOKENS:       '"'~['"']+'"';
STEMS:        '\''~['\'']+'\'';
STRICT_STEMS: '\'''\''~['\'']+'\'''\'';
EXACT_CONCEPT:  '<''<'~['>']+'>''>';
CONCEPT:      '<'~['>']+'>';
CHUNK:        '['[A-Z]+']';
POS:          '{'[A-Z]+'}';
REGEX:        '/'(~['/']|'\\/')+'/';
NUMBER:       [0-9]+;

WS   : [ \t\r\n]+ -> skip ; // skip spaces, tabs, newlines
