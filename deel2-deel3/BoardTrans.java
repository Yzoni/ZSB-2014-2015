/*
 * BoardTrans.java
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


import java.io.*;
import java.lang.*;

public class BoardTrans {
    /*
     * BoardTrans takes one optional argument, specifying the position on the field
     * it should use. It defaults to b7.
     */
    public static void main(String[] args) {
        String position;

        try {
            position = args[0];
        } catch (ArrayIndexOutOfBoundsException e) {
            position = "a1";
        }

        StudentBoardTrans boardTrans = new StudentBoardTrans(position);

        // set up the board in starting position
        boardTrans.board.setup();

        // draw the board state
        boardTrans.board.print();
    
    /*
     * You are now asked to access some data in the board structure.
     * Please print them and check your answers with the chess_board eitor
     * and the chess_piece editor from SCIL.
     */
        try {
            System.out.println("The dimensions of the squares on the board are " +
                    boardTrans.board.delta_x +
                    " by " +
                    boardTrans.board.delta_y +
                    "mm");

            System.out.println("The x,y coordinates of the board are " +
                            boardTrans.board.coords.x + " , " + boardTrans.board.coords.y
            );

            System.out.println("The height of the piece at " + boardTrans.pos + " is " +
                    boardTrans.board.getHeight(boardTrans.pos) +
                    " mm");

            System.out.println("The color of the piece at " + boardTrans.pos + " is "
                            + boardTrans.board.getSide(boardTrans.pos)
            );
        } catch (Exception e) {
            System.out.println(e);
            System.exit(1);
        }
    
        StudentBoardTrans.BoardLocation location = boardTrans.boardLocation;
        BoardLocation realLocation = new BoardLocation(boardTrans.pos);

        System.out.println("You think position " + boardTrans.pos + " is at (" +
                location.column + "," + location.row +
                "), the correct answer is (" + realLocation.column +
                "," + realLocation.row + ")");

        Point cartesian = new Point();
        cartesian = boardTrans.toCartesian(location.column, location.row);

        System.out.println("You think " + boardTrans.pos + " is at " + cartesian +
                ", the correct answer is " +
                boardTrans.board.toCartesian(boardTrans.pos));

        // Let's turn the board 45 degrees
        boardTrans.board.theta = 45;

        // recalculate cartesian
        cartesian = boardTrans.toCartesian(location.column, location.row);

        System.out.println("You think " + boardTrans.pos + " is at " + cartesian +
                ", the correct answer is " +
                boardTrans.board.toCartesian(boardTrans.pos));

        // Let's move the position of the board and turn it again
        boardTrans.board.coords.x = 100;
        boardTrans.board.coords.y = 200;
        boardTrans.board.theta = -60;

        // recalculate cartesian
        cartesian = boardTrans.toCartesian(location.column, location.row);

        System.out.println("You think " + boardTrans.pos + " is at " + cartesian +
                ", the correct answer is " +
                boardTrans.board.toCartesian(boardTrans.pos));
    }
}

class StudentBoardTrans {
    public ChessBoard board; // our board
    public String pos; // the position we're going to examine
    public BoardLocation boardLocation;

    // Empty constructor added for PP.java
    public StudentBoardTrans() {}

    public StudentBoardTrans(String position) {
        board = new ChessBoard();
        pos = position;
        boardLocation = new BoardLocation();
    }

    public Point toCartesian( int column, int row )
    {

        Point result = new Point();

        // Size of a square
        double squareSizeX = board.delta_x;
        double squareSizeY = board.delta_y;

        // Coordinates of the outer corner of h8
        double h8PosX = board.coords.x - board.sur_x;
        double h8PosY = board.coords.y + board.sur_y;

        // Position of the middle of the to be determined square
        double xPre = h8PosX - (8 - column) * squareSizeX + squareSizeX / 2;
        double yPre = h8PosY + (8 - row) * squareSizeY - squareSizeY / 2;

        // Distance between h8Pos and the specified square
        double translatedX = xPre - board.coords.x;
        double translatedY = yPre - board.coords.y;

        // Rotate using the rotation matrix:
        //  cos  |  sin
        // -------------
        //  -sin |  cos
        // This is the transpose of the fixed-axes matrix. It rotates the
        // coordinate system around the coordinates of h8Pos.
        double cos = Math.cos(Math.toRadians(board.theta));
        double sin = Math.sin(Math.toRadians(board.theta));

        // Matrix multiplication with the rotation matrix
        double rotateX = (translatedX * cos) + (translatedY * sin);
        double rotateY = (translatedX * -sin) + (translatedY * cos);

        result.x = rotateX + board.coords.x;
        result.y = rotateY + board.coords.y;
        result.z = board.board_thickness;

        return (result);
    }

    class BoardLocation {
        public int row;
        public int column;

        public BoardLocation() {

            // Use ASCII value to convert letter to int
            column = (int) pos.charAt(0);
            column = 8 - (105 % column);

            row = Character.getNumericValue(pos.charAt(1)) - 1;

        }
    }
}