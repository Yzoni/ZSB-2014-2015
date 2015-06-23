% -*- Mode: Prolog -*-
% J. Main 10541578
% Y. de Boer 10786015
% Date: 09/06/2015

% Position is represented by Side..Wx : Wy..Qx : Qy .. Bx : By .. Depth
% Side is side to move next ( us or them )
% Wx, Wy are X and Y coordinates of the white king
% Qx, Qy are X and Y coordinates of the white queen
% Bx, By are the X and Y coordinates of the black king
% depth is depth of position in the search tree

mode(queen).

% call the general original move predicates for queen moves etc.
move(A,B,C,D):-
        moveGeneral(A,B,C,D).

% diagonal, vertical and horizontal moves
move(queenmove, us..W..Qx : Qy..B..D, Qx:Qy - Q, them..W..Q..B..D1):-
	D1 is D + 1,
	coord( I ),		% integer between 1 and 8, size of playboard
	(
		Q = Qx : I
	;
		Q = I : Qy
	;
		diag(Qx : Qy, Q)
	),
	Q \== Qx : Qy,	                % Must have moved
	WQ = Qx:Qy,
	not inway( WQ, W, Q ),	        % white king not in way
	not inway( Qx : Qy, W, Q ),	% white king not in way
	not inway( (Qx : Qy), B, Q ).	% black king not in way

% If the white queen and the opponents king are in the same
% diagonal, column or row, and the white king is not in the way, then a
% checkmove can be made.
move( checkmove, Pos, Qx : Qy - Qx1 : Qy1, Pos1 ):-
	wk( Pos, W ),	                % white king position
	wq( Pos, Qx : Qy ),		% white Queen position
	bk( Pos, Bx : By ),	        % black king position
	% place black king and white queen on line
	(
		Qx1 = Bx,
		Qy1 = Qy
	;
		Qx1 = Qx,
		Qy1 = By
	;
		diag(Bx : By, Qx1 : Qy1)
	),
	% not the white king between the queen and black king
	not inway( Qx1 : Qy, W, Bx : By ),
	move( queenmove, Pos, Qx : Qy - Qx1 : Qy1, Pos1 ).

% generate all legal moves
move( legal, us..P, M, P1 ) :-
	(
		MC = kingdiagfirst
	;
		MC = queenmove
	),
	move( MC, us..P, M, P1 ).

queenexposed( Side..W..Q..B.._D, _ ) :-
	dist( W, Q, D1 ),
	dist( B, Q, D2 ),
	(
		Side = us, !,
		D1 > D2 + 1
	;
		Side = them, !,
		D1 > D2
	).

queendivides( _Side..Wx : Wy..Qx : Qy..Bx : By.._D, _ ) :-
	ordered( Wx, Qx, Bx ), !;
	ordered( Wy, Qy, By ).

queenlost( _.._W..B..B.._ ,_).	% queen has been captured

queenlost( them..W..Q..B.._ ,_) :-
	ngb( B, Q ),	        % black king attacks queen
	not ngb( W, Q ).	% white king does not defend











