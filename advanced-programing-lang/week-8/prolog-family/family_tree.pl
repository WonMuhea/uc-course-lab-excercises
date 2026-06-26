% --- Facts: Basic Relationships ---
male(john).
male(peter).
male(mark).
male(paul).
female(mary).
female(susan).
female(linda).
female(jane).

% Parent relationships
parent(john, mary).
parent(john, peter).
parent(susan, mary).
parent(susan, peter).
parent(mary, mark).
parent(mary, paul).
parent(linda, jane).
parent(peter, jane).

% --- Rules: Derived Relationships ---

% Grandparent: X is grandparent of Y
grandparent(X, Y) :- 
    parent(X, Z), 
    parent(Z, Y).

% Sibling: X and Y share a parent and are not the same person
sibling(X, Y) :- 
    parent(P, X), 
    parent(P, Y), 
    X \= Y.

% Cousin: X and Y have parents who are siblings
cousin(X, Y) :- 
    parent(PX, X), 
    parent(PY, Y), 
    sibling(PX, PY).

% Descendant: Recursive definition
% X is a descendant of Y if X is a child of Y, or a child of a descendant of Y
descendant(X, Y) :- parent(Y, X).
descendant(X, Y) :- parent(Z, X), descendant(Z, Y).