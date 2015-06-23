% -*- Mode: Prolog -*-
% King and Pawn vs king in Advice Language 0

% all rules

else_rule :: if true
	then [ move_random ].

% pieces of advice
% structure:
% advice( NAME, BETTERGOAL: HOLDINGGOAL: USMOVECONSTRAINT: 
%		THEMMOVECONSTRAINT ).


advice( move_random, 
	not did_not_move_pawn :
	not pawnlost :
	pawnmove :
        legal).
