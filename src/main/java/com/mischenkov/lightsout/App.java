package com.mischenkov.lightsout;


import com.mischenkov.lightsout.entity.Board;
import com.mischenkov.lightsout.entity.Piece;
import com.mischenkov.lightsout.entity.Position;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class App {
    public static void main( String[] args ) {
        Board initialBoard;
        List<Piece> pieceList;

        try(BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/02.txt"))) {
            var boardDepth = Integer.parseInt(reader.readLine().trim());
            var boardVector = reader.readLine().trim();
            var pieceVector = reader.readLine().trim();

            initialBoard = new Board(boardDepth, boardVector);
            pieceList = Arrays.stream(pieceVector.split(" "))
                    .map(Piece::new).toList();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        System.out.println(initialBoard);
        System.out.println(pieceList.get(0));
        System.out.println(initialBoard.placePiece(pieceList.get(0), new Position(0,0)));
        //pieceList.forEach(System.out::println);
    }
}
