% -*- Mode: Prolog -*-
% J. Main 10541578
% Y. de Boer 10786015
% Date: 09/06/2015

% $Id: KRAPqueen.pl,v 1.1 2004/05/31 19:47:25 mtjspaan Exp $

% King and Queen vs king in Advice Language 0

% all rules

edge_rule :: if their_king_edge and kings_close
	then [ mate_in_2, approach, keeproom, divide_in_2, divide_in_3 ].

else_rule :: if true
	then [ squeeze, approach, keeproom, divide_in_2, divide_in_3 ].

% pieces of advice
% structure:
% advice( NAME, BETTERGOAL: HOLDINGGOAL: USMOVECONSTRAINT:
%		THEMMOVECONSTRAINT ).

% All advices are mostly the same as the advices for rook. The exceptions are that all
% advices now have that the opponents move constraint has to be legal. Also every advice now
% checks for no stalemate in the bettergoal.

advice( mate_in_2,
	mate :
	not queenlost and their_king_edge and not stalemate :
	( depth = 0 ) and legal then ( depth = 2 ) and checkmove:
	( depth = 1 ) and legal ).

% Makes the between the queen and king smaller
advice( squeeze,
	newroomsmaller and not queenexposed and queendivides and not stalemate :
	not queenlost :
	( depth = 0 ) and queenmove:
	( depth = 1 ) and legal ).

% Move the white king closer to the black king
advice( approach,
	okapproachedsquare and not queenexposed and not stalemate and (queendivides or lpatt) and (roomgt2 or not our_king_edge):
	not queenlost:
	( depth = 0 ) and kingdiagfirst:
	( depth = 1 ) and legal ).

% Keeps room for the king
advice( keeproom,
themtomove and not queenexposed and queendivides and okorndle and (roomgt2 or not our_king_edge) and not stalemate:
	not queenlost and not our_king_edge:
	( depth = 0 ) and kingdiagfirst:
	( depth = 1 ) and legal ).

% divide the room
advice( divide_in_2,
themtomove and queendivides and not queenexposed and not stalemate:
	not queenlost:
	( depth < 3 ) and legal:
	( depth < 2 ) and legal ).

advice( divide_in_3,
themtomove and queendivides and not queenexposed and not stalemate:
	not queenlost:
	( depth < 5 ) and legal:
	( depth < 4 ) and legal ).





