/*
 * PPAStar.java
 * Assignment for the Path planning part of the ZSB lab course.
 *
 * 21 juli 2015
 * Y. J. I. de Boer - 10786015
 * J. Main - 10541578
 *
 * ************************************************************
 *
 * This Java introduction was written for the 2001/2002 course.
 * Matthijs Spaan <mtjspaan@science.uva.nl>
 * $Id: Week1.java,v 1.9 2008/06/10 10:21:36 obooij Exp $
 *
 */

import java.lang.*;
import java.util.ArrayList;
import java.util.Vector;

public class PP {
    private static double SAFE_HEIGHT = 200;
    private static double LOW_HEIGHT = 40;
    private static double LOWPATH_HEIGHT = 20;
    private static double OPEN_GRIP = 30;
    private static double CLOSED_GRIP = 0;

    public static void main(String[] args) {
        Vector<GripperPosition> p = new Vector<GripperPosition>();
        ChessBoard b;
        String computerFrom, computerTo;

        System.out.println("**** THIS IS THE STUDENT PP MODULE IN JAVA");
        System.out.println("**** The computer move was " + args[0]);

    /* Read possibly changed board position */
        if (args.length > 1) {
            double x = Double.parseDouble(args[1]),
                    y = Double.parseDouble(args[2]),
                    theta = Double.parseDouble(args[3]);
            Point boardPosition = new Point(x, y, 0);

            System.out.println("**** Chessboard is at (x,y,z,theta): ("
                    + x + ", " + y + ", 0, " + theta + ")");

            b = new ChessBoard(boardPosition, theta);
        } else
            b = new ChessBoard();

    /* Read the board state*/
        b.read();
    /* print the board state*/
        System.out.println("**** The board before the move was:");
        b.print();

        computerFrom = args[0].substring(0, 2);
        computerTo = args[0].substring(2, 4);
    
    /* plan a path for the move */
        // Try to create a new low path
        ArrayList<BoardLocation> lowPath = lowPath(computerFrom, computerTo, b);

        // If a low path could not be created do a highpath instead
        if (lowPath == null) {
            highPath(computerFrom, computerTo, b, p);
        } else {
            doLowPath(computerFrom, computerTo, b, p, lowPath);
        }

    /* move the computer piece */
        try {
            // Check for piece at new location and remove piece before moving
            if (b.hasPiece(computerTo)) {
                // Remove the piece first
                moveToGarbage(computerTo, b, p);
            }
            b.movePiece(computerFrom, computerTo);
        } catch (ChessBoard.NoPieceAtPositionException e) {
            System.out.println(e);
            System.exit(1);
        }

        System.out.println("**** The board after the move was:");
    /* print the board state*/
        b.print();
    
    /* after done write the gripper positions */
        GripperPosition.write(p);
    }

    // Creates a starting boardlocation and a end boardlocation. Returns an arraylist of and gives this to
    // AStar boardlocations representing a low path. If a low path could not be found return null.
    private static ArrayList<BoardLocation> lowPath(String from, String to, ChessBoard b) {

        try {
            System.out.print("lowPath: trying to create a lowpath");

            BoardLocation fromLocation = new BoardLocation(from);
            BoardLocation toLocation = new BoardLocation(to);

            AStar mAstarAlgorithm = new AStar(fromLocation, toLocation, b);
            return mAstarAlgorithm.getPath();

        } catch (Exception e) {e.printStackTrace();}

        return null;
    }

    // Robot movements to do a low path. Loops through the board locations given by AStar.
    private static void doLowPath(String from, String to,
                                  ChessBoard b, Vector<GripperPosition> p,
                                  ArrayList<BoardLocation> locationList) {

        try {

            System.out.println("**** In low path");

            double pieceHeight = b.getPiece(from).height;

            StudentBoardTrans fromTrans = new StudentBoardTrans(from);
            StudentBoardTrans toTrans = new StudentBoardTrans(to);

            Point startPoint = fromTrans.toCartesian(fromTrans.boardLocation.column, fromTrans.boardLocation.row);
            Point endPoint = toTrans.toCartesian(toTrans.boardLocation.column, toTrans.boardLocation.row);

            double startHeight = startPoint.z;

            p.add(new GripperPosition(startPoint, 0, OPEN_GRIP));

            startPoint.z = startHeight + LOW_HEIGHT;
            p.add(new GripperPosition(startPoint, 0, OPEN_GRIP));

            startPoint.z = startHeight + (pieceHeight / 2);
            p.add(new GripperPosition(startPoint, 0, OPEN_GRIP));

            p.add(new GripperPosition(startPoint, 0, CLOSED_GRIP));

            startPoint.z = startHeight + LOWPATH_HEIGHT;
            p.add(new GripperPosition(startPoint, 0, CLOSED_GRIP));

            for (BoardLocation location : locationList) {

                StudentBoardTrans trans = new StudentBoardTrans("xx");
                Point transPoint = trans.toCartesian(location.column, location.row);
                System.out.println("LowPath path: " + location.column + ", " + location.row);

                p.add(new GripperPosition(transPoint, 0, CLOSED_GRIP));
            }

            endPoint.z = startHeight + LOW_HEIGHT + (pieceHeight / 2);
            p.add(new GripperPosition(endPoint, 0, CLOSED_GRIP));

            endPoint.z = startHeight + (LOW_HEIGHT / 2) + (pieceHeight / 2);
            p.add(new GripperPosition(endPoint, 0, CLOSED_GRIP));

            endPoint.z = startHeight + (pieceHeight / 2);
            p.add(new GripperPosition(endPoint, 0, OPEN_GRIP));

            endPoint.z = startHeight + SAFE_HEIGHT;
            p.add(new GripperPosition(endPoint, 0, OPEN_GRIP));

        } catch (Exception e) {e.printStackTrace();}

    }

    // Creates a starting point and a end point.
    private static void highPath(String from, String to,
                                 ChessBoard b, Vector<GripperPosition> p) {
        try {
        StudentBoardTrans fromTrans = new StudentBoardTrans(from);
        StudentBoardTrans toTrans = new StudentBoardTrans(to);

        Point startPoint = fromTrans.toCartesian(fromTrans.boardLocation.column, fromTrans.boardLocation.row);
        Point endPoint = toTrans.toCartesian(toTrans.boardLocation.column, toTrans.boardLocation.row);

        double pieceHeight = b.getPiece(from).height;

        doHighPath(startPoint, endPoint, pieceHeight, p);
        } catch (Exception e) {e.printStackTrace();}
    }

    private static void doHighPath(Point startPoint, Point endPoint, double pieceHeight,
                                   Vector<GripperPosition> p) {

            System.out.println("**** In high path");

            double startHeight = startPoint.z;

            p.add(new GripperPosition(startPoint, 0, OPEN_GRIP));

            startPoint.z = startHeight + LOW_HEIGHT;
            p.add(new GripperPosition(startPoint, 0, OPEN_GRIP));

            startPoint.z = startHeight + pieceHeight / 2;
            p.add(new GripperPosition(startPoint, 0, OPEN_GRIP));

            p.add(new GripperPosition(startPoint, 0, CLOSED_GRIP));

            startPoint.z = startHeight + SAFE_HEIGHT;
            p.add(new GripperPosition(startPoint, 0, CLOSED_GRIP));

            endPoint.z = startHeight + SAFE_HEIGHT;
            p.add(new GripperPosition(endPoint, 0, CLOSED_GRIP));

            endPoint.z = startHeight + LOW_HEIGHT + (pieceHeight / 2);
            p.add(new GripperPosition(endPoint, 0, CLOSED_GRIP));

            endPoint.z = startHeight + (LOW_HEIGHT / 2) + (pieceHeight / 2);
            p.add(new GripperPosition(endPoint, 0, CLOSED_GRIP));

            endPoint.z = startHeight + pieceHeight / 2;
            p.add(new GripperPosition(endPoint, 0, OPEN_GRIP));

            endPoint.z = startHeight + SAFE_HEIGHT;
            p.add(new GripperPosition(endPoint, 0, OPEN_GRIP));


    }

    // Takes care of moving a piece next to the board when its dead.
    private static void moveToGarbage(String to, ChessBoard b, Vector<GripperPosition> g)
            throws ChessBoard.NoPieceAtPositionException {

        System.out.println("**** In movoToGarbage");

        ChessPiece mPiece = b.getPiece(to);
        StudentBoardTrans fromTrans = new StudentBoardTrans(to);
        Point startPoint = fromTrans.toCartesian(fromTrans.boardLocation.column, fromTrans.boardLocation.row);

        Point endPoint = findGarbagePosition(b);

        double pieceHeight = mPiece.height;

        doHighPath(startPoint, endPoint, pieceHeight, g);
    }

    // Finds an end position next to the board and returns an end Point
    private static Point findGarbagePosition(ChessBoard b) {

        int deadblackpieces = -1;

        for (ChessPiece mChessPiece : b.deadChessPieces) {
            if (mChessPiece.side.equals("black")) {
                deadblackpieces++;
            }
        }

        int column = 9;
        int row = deadblackpieces;

        // When more than 7 pieces are dead start an extra column next to the other dead pieces
        if (deadblackpieces > 7) {
            column = 10;
        }

        StudentBoardTrans garbageTrans = new StudentBoardTrans("xx");
        garbageTrans.board = b;
        BoardLocation garbageLocation = new BoardLocation(column, row);

        Point garbagePoint = garbageTrans.toCartesian(garbageLocation.column, garbageLocation.row);

        return garbagePoint;
    }

}


// Class that implements the AStar algorithm for finding the shortest path for lowpath over the board.
// It works by utilizing a openlist and a closed list. In the closed list already explored nodes are stored. And
// in the open list the to be explored nodes.
// This class does NOT work currently.
class AStar {

    private BoardLocation startLocation;
    private BoardLocation endLocation;
    private ChessBoard b;

    private ArrayList<Node> openList = new ArrayList<Node>();
    private ArrayList<Node> closedList = new ArrayList<Node>();

    private BoardLocation currentBestLocation;
    private Node previousNode;
    private Node newBestNode;

    public AStar(BoardLocation startLocation, BoardLocation endLocation, ChessBoard b) {

        this.startLocation = startLocation;
        this.endLocation = endLocation;

        this.b = b;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Main function of this class. As long as the openlist not empty. Keep exploring other nodes. Or when
    // the endBoardLocation is reached start building the path from the endnode back to the startnode. After the
    // build return the whole path as an Arraylist of boardLocations.
    public ArrayList<BoardLocation> getPath() {

        initializeFirstNode();

        while (!openList.isEmpty()) {

            System.out.println("AStar: Main: loop untill");

            ArrayList<BoardLocation> newLocations = findNewLocations(currentBestLocation);
            AddNodesToOpenList(newLocations, previousNode);

            newBestNode = findNextBestNode();
            AddNodeToClossedList(newBestNode);

            currentBestLocation = newBestNode.getBoardLocation();

            if (newBestNode.getBoardLocation().equals(endLocation)) {
                // Path found
                return buildPath(newBestNode);
            }

        }

        return null;
    }

    //////////////////////////////////////////
    // Initializes the first node
    private void initializeFirstNode() {
        System.out.println("AStar: initialize first node");
        currentBestLocation = startLocation;

        Node startNode = new Node(startLocation, null, -1);
        previousNode = startNode;

        ArrayList<BoardLocation> newLocations = findNewLocations(currentBestLocation);
        AddNodesToOpenList(newLocations, previousNode);
        System.out.println("Nodes in openlist on initialize firstnode" + openList);

        closedList.add(startNode);

    }

    ///////////////////////////////////////////////////////////////
    // Builds the path by backtracking through previous locations
    // Starting with the endNode and work back to the startNode
    private ArrayList<BoardLocation> buildPath(Node endNode) {
        System.out.println("AStar: build path");

        ArrayList<BoardLocation> path = new ArrayList<BoardLocation>();

        Node node = endNode;

        while (node.getBoardLocation() != startLocation) {
            BoardLocation newBoardLocation = node.getBoardLocation();
            path.add(newBoardLocation);
            node = node.getPreviousNode();
        }

        return path;
    }


    /////////////////////////////////////////////////////////////////////
    // Find the next node (lowest score) and add it to the closed list.
    private Node findNextBestNode() {

        System.out.println("AStar: findNextBestNode");
        int highestFValue = -1;
        Node nextBestNode = null;

        for(Node node : openList) {
            int fValue = node.getFValue();
            if (fValue > highestFValue) {
                highestFValue = fValue;
                nextBestNode = node;
            }
        }

        previousNode = nextBestNode.getPreviousNode();
        return nextBestNode;
    }

    ////////////////////////////////////
    // Adds a node the closed list
    private void AddNodeToClossedList(Node nextBestNode) {
        closedList.add(nextBestNode);
    }

    /////////////////////////////////////
    // Check if node is in closed list
    private boolean inClosedList(BoardLocation location) {
        for (Node node : closedList) {
            if (node.boardLocation.column == location.column && node.boardLocation.row == location.row) {
                return true;
            }
        }
        return false;
    }

    ///////////////////////////////////////////////////////////
    // Create new Node objects and add them to the open list.
    private void AddNodesToOpenList(ArrayList<BoardLocation> newLocations,
                                               Node previousNode) {
        System.out.println("AStar: add nodes to open list");

        for(BoardLocation location : newLocations) {

            int FValue = calculateFValue(location);
            openList.add(new Node(location, previousNode, FValue));
        }
    }

    //////////////////////////////////////////////////////////////////////////////
    // Find new nodes around current node and export valid nodes as list of nodes
    // A valid node is on the board and is not occupied by any other piece.
    private ArrayList<BoardLocation> findNewLocations(BoardLocation currentLocation) {

        System.out.println("AStar: find new locations");

        ArrayList<BoardLocation> possibleNewLocations = new ArrayList<BoardLocation>();
        ArrayList<BoardLocation> newLocations = new ArrayList<BoardLocation>();

        int currentRow = currentBestLocation.row;
        int currentColumn = currentBestLocation.column;

        BoardLocation newLocationNorth = new BoardLocation(currentColumn + 1, currentRow);
        possibleNewLocations.add(newLocationNorth);

        BoardLocation newLocationSouth = new BoardLocation(currentColumn - 1, currentRow);
        possibleNewLocations.add(newLocationSouth);

        BoardLocation newLocationEast = new BoardLocation(currentColumn, currentRow + 1);
        possibleNewLocations.add(newLocationEast);

        BoardLocation newLocationWest = new BoardLocation(currentColumn, currentRow - 1);
        possibleNewLocations.add(newLocationWest);

        for (int i=0; i<possibleNewLocations.size(); i++) {
            System.out.println("AStar: added to list " + possibleNewLocations.get(i).column);
            if(onChessBoard(possibleNewLocations.get(i)) && !isOccupied(possibleNewLocations.get(i))
                    && !inClosedList(possibleNewLocations.get(i))) {
                newLocations.add(possibleNewLocations.get(i));
            }
        }

        return newLocations;
    }

    ////////////////////////////////////////////////////
    // Returns true if a boardLocation is not occupied
    private boolean isOccupied(BoardLocation nextLocationInts) {

        String nextLocationString = intPostoStringPos(nextLocationInts.column,
                nextLocationInts.row);
        System.out.println("AStar: isOccupied" + nextLocationString);

        if (b.hasPiece(nextLocationString)) {
            return true;
        }

        return false;
    }

    ////////////////////////////////////////////////////
    // Returns true if a location is on the board
    private boolean onChessBoard(BoardLocation nextLocationInts) {

        boolean onBoard = nextLocationInts.column >= 0 && nextLocationInts.column < 8
                && nextLocationInts.row >= 0 && nextLocationInts.row < 8;

        if (onBoard) {
            return true;
        }

        return false;
    }

    //////////////////////////////////////////////////////////////////////
    // Calculate F value by summing the heuristic cost with the move cost
    private int calculateFValue(BoardLocation nextLocation) {

        int heuristicCost = calculateHeuristicCost(nextLocation);
        int moveCost = 10;

        int fValue = Math.abs(heuristicCost) + Math.abs(moveCost);

        return fValue;
    }

    //////////////////////////////////////////////////////
    // Calculate heuristic with the manhatten distance
    private int calculateHeuristicCost(BoardLocation nextLocation) {

        int heuristicCost = Math.abs(startLocation.column - nextLocation.column)
                + Math.abs(startLocation.row - nextLocation.row);

        return heuristicCost;
    }

    ////////////////////////////////////////////////////
    // Board requires a string as argument
    private String intPostoStringPos(int column, int row) {
        System.out.println("AStar: toString" + column);

        String[] chars =  {"a", "b", "c", "d", "e", "f", "g", "h"};

        String columnChar = chars[column];
        int rowChar = row + 1;

        return columnChar + Integer.toString(rowChar);
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////
    // Node class which keeps track of the boardLocation, the node where this node was explored from
    // and the FValue of the node
    private class Node {

        private BoardLocation boardLocation;
        private Node previousNode;
        private int FValue;

        public Node(BoardLocation boardLocation, Node previousNode, int FValue) {

            this.boardLocation = boardLocation;
            this.previousNode = previousNode;
            this.FValue = FValue;
        }

        public BoardLocation getBoardLocation() {
            return boardLocation;
        }


        public Node getPreviousNode() {
            return previousNode;
        }

        public int getFValue() {
            return FValue;
        }

    }
}