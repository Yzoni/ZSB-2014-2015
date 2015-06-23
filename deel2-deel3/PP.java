/*
 * PP.java
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
                b.removePiece(computerTo);
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

    // Creates an Arraylist of boardlocations by utilizing the DistanceMatrix class. It creates a distanceTransform
    // from the start starting position.
    private static ArrayList<BoardLocation> lowPath(String from, String to, ChessBoard b) {

        try {
            System.out.println("lowPath: trying to create a lowpath");

            ArrayList<BoardLocation> boardLocations = new ArrayList<BoardLocation>();

            DistanceMatrix mDistanceMatrix = new DistanceMatrix();
            mDistanceMatrix.distanceTransform(b, from);

            if (mDistanceMatrix.notPossible(to)) {
                System.out.println("lowPath: a lowpath could not be found");
                return null;
            }

            // Initialize the goal boardlocation
            BoardLocation toLocation = new BoardLocation(to);
            int newSmallest = mDistanceMatrix.smallestPositiveNeighbourValue(toLocation.column, toLocation.row);

            // Loop untill the current boardlocation is the goald boardLocation
            while (newSmallest > 0) {

                newSmallest = mDistanceMatrix.smallestPositiveNeighbourValue(toLocation.column, toLocation.row);
                BoardLocation neighbourBoardLocation = new BoardLocation(mDistanceMatrix.neighbourCol,
                        mDistanceMatrix.neighbourRow);
                boardLocations.add(neighbourBoardLocation);
                toLocation.column = neighbourBoardLocation.column;
                toLocation.row = neighbourBoardLocation.row;

            }

            return boardLocations;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // Robot movements to do a low path. Loops through the board locations created by lowPath
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
            double endHeight = endPoint.z;

            startPoint.z = startHeight + SAFE_HEIGHT;
            p.add(new GripperPosition(startPoint, 0, OPEN_GRIP));

            startPoint.z = startHeight + LOW_HEIGHT;
            p.add(new GripperPosition(startPoint, 0, OPEN_GRIP));

            startPoint.z = startHeight + (pieceHeight / 2);
            p.add(new GripperPosition(startPoint, 0, OPEN_GRIP));

            p.add(new GripperPosition(startPoint, 0, CLOSED_GRIP));

            startPoint.z = startHeight + LOWPATH_HEIGHT;
            p.add(new GripperPosition(startPoint, 0, CLOSED_GRIP));

            // Go through all boardLocations from lowPath
            for (BoardLocation location : locationList) {

                StudentBoardTrans trans = new StudentBoardTrans("xx"); // Hack because no empty constructor
                trans.board = b;
                Point transPoint = trans.toCartesian(location.column, location.row);
                System.out.println("LowPath path: " + location.column + ", " + location.row);

                p.add(new GripperPosition(transPoint, 0, CLOSED_GRIP));
            }

            endPoint.z = endHeight + LOW_HEIGHT + (pieceHeight / 2);
            p.add(new GripperPosition(endPoint, 0, CLOSED_GRIP));

            endPoint.z = endHeight + (LOW_HEIGHT / 2) + (pieceHeight / 2);
            p.add(new GripperPosition(endPoint, 0, CLOSED_GRIP));

            endPoint.z = endHeight + (pieceHeight / 2);
            p.add(new GripperPosition(endPoint, 0, OPEN_GRIP));

            endPoint.z = endHeight + SAFE_HEIGHT;
            p.add(new GripperPosition(endPoint, 0, OPEN_GRIP));

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // Creates a starting point and a end point and starts the highPath
    private static void highPath(String from, String to,
                                 ChessBoard b, Vector<GripperPosition> p) {
        try {
            StudentBoardTrans fromTrans = new StudentBoardTrans(from);
            StudentBoardTrans toTrans = new StudentBoardTrans(to);

            Point startPoint = fromTrans.toCartesian(fromTrans.boardLocation.column, fromTrans.boardLocation.row);
            Point endPoint = toTrans.toCartesian(toTrans.boardLocation.column, toTrans.boardLocation.row);

            double pieceHeight = b.getPiece(from).height;

            doHighPath(startPoint, endPoint, pieceHeight, p);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Robot movements to do a high path.
    private static void doHighPath(Point startPoint, Point endPoint, double pieceHeight,
                                   Vector<GripperPosition> p) {

        System.out.println("**** In high path");

        double startHeight = startPoint.z;
        double endHeight = endPoint.z;

        startPoint.z = startHeight + SAFE_HEIGHT;
        p.add(new GripperPosition(startPoint, 0, OPEN_GRIP));

        startPoint.z = startHeight + LOW_HEIGHT;
        p.add(new GripperPosition(startPoint, 0, OPEN_GRIP));

        startPoint.z = startHeight + pieceHeight / 2;
        p.add(new GripperPosition(startPoint, 0, OPEN_GRIP));

        p.add(new GripperPosition(startPoint, 0, CLOSED_GRIP));

        startPoint.z = startHeight + SAFE_HEIGHT;
        p.add(new GripperPosition(startPoint, 0, CLOSED_GRIP));

        endPoint.z = endHeight + SAFE_HEIGHT;
        p.add(new GripperPosition(endPoint, 0, CLOSED_GRIP));

        endPoint.z = endHeight + LOW_HEIGHT + (pieceHeight / 2);
        p.add(new GripperPosition(endPoint, 0, CLOSED_GRIP));

        endPoint.z = endHeight + (LOW_HEIGHT / 2) + (pieceHeight / 2);
        p.add(new GripperPosition(endPoint, 0, CLOSED_GRIP));

        endPoint.z = endHeight + pieceHeight / 2;
        p.add(new GripperPosition(endPoint, 0, OPEN_GRIP));

        endPoint.z = endHeight + SAFE_HEIGHT;
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
        int column = 9;
        int row = deadblackpieces;

        for (ChessPiece mChessPiece : b.deadChessPieces) {
            if (mChessPiece.side.equals("black")) {
                deadblackpieces++;
            }
        }

        // When more than 7 pieces are dead start an extra column next to the other dead pieces
        if (deadblackpieces > 7) {
            column = 10;
        }

        StudentBoardTrans garbageTrans = new StudentBoardTrans("xx"); // Hack because no empty constructor
        garbageTrans.board = b;
        BoardLocation garbageLocation = new BoardLocation(column, row);

        Point garbagePoint = garbageTrans.toCartesian(garbageLocation.column, garbageLocation.row);

        return garbagePoint;
    }

}


/**
 * This class it implements a distance matrix. It can be used
 * to make a distance transform of the chess board locations.
 *
 * Edited to fit the convention of using the column as the first argument and
 * row as the second argument.
 *
 * @author Nikos Massios
 * @author Matthijs Spaan
 * @version $Id: DistanceMatrix.java,v 1.5 2002/04/26 14:17:35 mtjspaan Exp $
 */
class DistanceMatrix {
    private static int OCCUPIED = -1;
    private static int EMPTY = -2;
    private static int UNREACHABLE = -3;
    private static int HAVENT_FOUND_IT = 1000;
    private int distanceMatrix[][];

    /**
     * The row of the closest neighbour. The correct value
     * is contained only after a call to the smallestPositiveNeighbourValue
     * method.
     */
    public int neighbourRow;
    /**
     * The row of the closest neighbour. The correct value
     * is contained only after a call to the smallestPositiveNeighbourValue
     * method.
     */
    public int neighbourCol;


    /**
     * Sole constructor. It just allocates memory. A call
     * to the distnanceTransform method is necessary before the distance
     * matrix can be used.
     */
    public DistanceMatrix() {
        distanceMatrix = new int[8][8];
    }

    /**
     * This method uses the locations of the pieces on the board
     * to initialise the distance matrix. It specifies the
     * pieces that are empty and the ones that are occupied.
     *
     * @param board The chess board to use in order to initialise the
     *              distance matrix.
     */
    private void init(ChessBoard board) {
        ChessPiece p;

        for (int col = 0; col < 8; col++)
            for (int row = 0; row < 8; row++)
                distanceMatrix[row][col] = EMPTY;

        for (int i = 0; i < board.aliveChessPieces.size(); i++) {
            p = (ChessPiece) board.aliveChessPieces.get(i);
            distanceMatrix[p.getRow()][p.getCol()] = OCCUPIED;
        }
    }

    /**
     * This method prints the distance matrix. The distance
     * matrix values are printed to System.out.<p>
     * "o" stands for occupied.<p>
     * "e" stands for empty. <p>
     * "u" stands for unrechable. High path is necessary there<p>
     * any integer value is the distance from the target.
     */
    public void print() {
        String s;

        s = "  abcdefgh";
        System.out.println(s);
        for (int row = 7; row >= 0; row--) {
            s = new Integer(row + 1).toString() + " ";
            for (int col = 0; col < 8; col++) {
                if (distanceMatrix[row][col] == OCCUPIED)
                    s = s + "o";
                else if (distanceMatrix[row][col] == EMPTY)
                    s = s + "e";
                else if (distanceMatrix[row][col] == UNREACHABLE)
                    s = s + "u";
                else {
                    Integer dist = new Integer(distanceMatrix[row][col]);
                    s = s + dist.toString();
                }
            }
            System.out.println(s);
        }
    }

    /**
     * This method finds the smallest neightbour and its distance value.
     * It arguments are a location on the board. It examines the
     * neighbours of that location (assuming 4-connectivity) and
     * returns the distance value of the closest neighbour. The location
     * of the closest neighbour is set at the neighbourRow and
     * neighbourCol fields. These fields can be inspected if its
     * necessary to know the location of the neighbour.
     *
     * @param row The row of the board location.
     * @param col The column of the board location.
     * @return The distance value of the closest neighbour. If no
     * available neighbour exists 1000 is returned.
     * @see #neighbourRow
     * @see #neighbourCol
     */
    public int smallestPositiveNeighbourValue(int col, int row) {
        int[] values = new int[4];
        int smallestIndex, smallestValue;


	/*find all neighbours that exist...
           0
             3 c 2
	       1
	*/
        if (row + 1 < 8)
            values[0] = distanceMatrix[row + 1][col];
        else
            values[0] = OCCUPIED;
        if (row - 1 >= 0)
            values[1] = distanceMatrix[row - 1][col];
        else
            values[1] = OCCUPIED;

        if (col + 1 < 8)
            values[2] = distanceMatrix[row][col + 1];
        else
            values[2] = OCCUPIED;
        if (col - 1 >= 0)
            values[3] = distanceMatrix[row][col - 1];
        else
            values[3] = OCCUPIED;

	/* find the smallest positive now*/
        smallestValue = HAVENT_FOUND_IT;
        smallestIndex = 0;
        for (int i = 0; i < 4; i++)
            if ((smallestValue > values[i]) && (values[i] >= 0)) {
                smallestValue = values[i];
                smallestIndex = i;
            }


	/*Neighbour was all neighbours that exist...
	       0
             3 c 2
	       1
	*/

        if (smallestIndex == 0) {
            neighbourRow = row + 1;
            neighbourCol = col;
        } else if (smallestIndex == 1) {
            neighbourRow = row - 1;
            neighbourCol = col;
        } else if (smallestIndex == 2) {
            neighbourRow = row;
            neighbourCol = col + 1;
        } else if (smallestIndex == 3) {
            neighbourRow = row;
            neighbourCol = col - 1;
        }

        return (smallestValue);
    }

    /**
     * This method checks if it is notPossible to plan a low path
     *
     * @param target The board location to plan a path to.
     * @return True If a low path to that location is not possible.
     * False if a low path to that location is possible.
     */
    public boolean notPossible(String target) {
        BoardLocation t = new BoardLocation(target);

        if (distanceMatrix[t.row][t.column] == UNREACHABLE)
            return (true);
        else
            return (false);
    }


    /**
     * This method generates the distance transform. It sets the
     * correct values in the distance matrix. A call to this method
     * is necessary before a call to the smallestPositiveNeighbourValue
     * method makes sence. It uses the information stored in the board
     * to determine the empty and occupied locations. Then starting
     * from the target location (distance 0), it assigns distances
     * to the empty neighbours of the target location (distance 1)
     * and to their empty neighbours (distance 2) and so on. Until
     * the whole board in examined. The empty locations that remain
     * at the end of the iteration are unreachable due to obstacles.
     * In terms of path planning a high path should be planned then.
     *
     * @param board  The chess board to use.
     * @param target The target location to use when generating the transform.
     */
    public void distanceTransform(ChessBoard board,
                                  String target) {
        int targetRow, targetCol;
        int dummyRow, dummyCol;
        int redo;
        int count = 0;
        BoardLocation t = new BoardLocation(target);


        init(board);

        distanceMatrix[t.row][t.column] = 0;

        dummyRow = dummyCol = 0;

        redo = 1;
        while ((redo == 1) && (count < 16)) {
            redo = 0;
            count++;
            for (int cellCol = 0; cellCol < 8; cellCol++)
                for (int cellRow = 0; cellRow < 8; cellRow++)
                    if (distanceMatrix[cellRow][cellCol] == EMPTY) {
                        redo = 1;
                        if (smallestPositiveNeighbourValue(cellCol, cellRow) != HAVENT_FOUND_IT) {
                            distanceMatrix[cellRow][cellCol] = smallestPositiveNeighbourValue(cellCol, cellRow) + 1;
                        }
                    }
        }

        for (int cellCol = 0; cellCol < 8; cellCol++)
            for (int cellRow = 0; cellRow < 8; cellRow++)
                if (distanceMatrix[cellRow][cellCol] == EMPTY)
                    distanceMatrix[cellRow][cellCol] = UNREACHABLE;


    }

}