package com.mischenkov.lightsout;


import com.mischenkov.lightsout.entity.Board;
import com.mischenkov.lightsout.entity.Piece;
import com.mischenkov.lightsout.entity.Position;
import com.mischenkov.lightsout.entity.PriorityPosition;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class App {
    private static volatile boolean solutionFound = false;

    public static void main(String[] args) {
        Board initialBoard;
        List<Piece> pieceList;
        try (BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/02.txt"))) {
            var boardDepth = Integer.parseInt(reader.readLine().trim());
            var boardVector = reader.readLine().trim();
            var pieceVector = reader.readLine().trim();
            initialBoard = new Board(boardDepth, boardVector);
            pieceList = Arrays.stream(pieceVector.split(" "))
                    .map(Piece::new)
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        List<Position> solutionList = new ArrayList<>(pieceList.size());

        var t1 = System.currentTimeMillis();
        if (solveParallel(initialBoard, pieceList, solutionList)) {
            solutionList.forEach(pos -> System.out.print(pos.x() + "," + pos.y() + " "));
            System.out.println();
        } else {
            System.out.println("No solution");
        }
        var t2 = System.currentTimeMillis();
        var dtSec = (t2 -t1) / 1000;
        System.out.println("Time: " + dtSec);

        // trace

        var board = initialBoard;
        for (int i = 0; i < solutionList.size(); i++) {
            var pos = solutionList.get(i);
            var piece = pieceList.get(i);
            System.out.println("Step " + (i+1) );
            System.out.println(board);
            System.out.println("+");
            System.out.println(piece + "\n### " + pos);
            Board finalBoard = board;
            var posPos = posPosition(board, piece);
            PriorityQueue<PriorityPosition> positionDeque = new PriorityQueue<>(posPos.size());
            posPos.forEach(el -> {
                var posWidth = calculate(finalBoard.getBoardMatrix(), piece.getPieceMatrix(), el.x(), el.y());
                positionDeque.add(new PriorityPosition(el, posWidth));
            });
            IntStream.range(0, posPos.size()).forEach(index -> {
                var el = posPos.get(index);
                var posWidth = calculate(finalBoard.getBoardMatrix(), piece.getPieceMatrix(), el.x(), el.y());
                var line = String.format("[%d](%d, %d)=%d | ", index + 1, el.x(), el.y(), posWidth);
                System.out.print(line);
            });
            System.out.println();
            IntStream.range(0, positionDeque.size()).forEach(index -> {
                var quePosition = positionDeque.remove();
                var priorityLine = String.format("[%d](%d, %d)=%d | ",
                        index + 1, quePosition.x(), quePosition.y(), quePosition.getPriority());
                System.out.print(priorityLine);
            });
            System.out.println();
            board = board.placePiece(piece, pos);
            System.out.println("=");
            System.out.println(board);
            System.out.println("----------------------");
        }
    }

    public static int calculate(int[][] m1, int[][] m2, int x, int y) {
        int sum = 0;

        // Проходим по всем элементам меньшей матрицы (m2)
        for (int m2y = 0; m2y < m2.length; m2y++) {
            for (int m2x = 0; m2x < m2[m2y].length; m2x++) {
                // Вычисляем соответствующие координаты в большой матрице (m1)
                int m1Row = y + m2y;
                int m1Col = x + m2x;
                sum += m1[m1Row][m1Col] + m2[m2y][m2x];
            }
        }

        return sum;
    }

    public static List<Position> posPosition(Board board, Piece piece) {
        List<Position> result = new ArrayList<>();
        int[][] matrix = board.getBoardMatrix();
        int boardHeight = matrix.length;
        int boardWidth = matrix[0].length;

        for (int y = 0; y < boardHeight; y++) {
            for (int x = 0; x < boardWidth; x++) {
                Position pos = new Position(x, y);
                if (board.isEmbedPiece(piece, pos)) {
                    result.add(pos);
                }
            }
        }
        return result;
    }

    private static boolean solveParallel(Board board, List<Piece> pieces, List<Position> solutionList) {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Future<List<Position>>> futures = new ArrayList<>();
        Piece firstPiece = pieces.get(0);
        int[][] matrix = board.getBoardMatrix();
        int boardHeight = matrix.length;
        int boardWidth = matrix[0].length;


        for (int y = 0; y < boardHeight; y++) {
            for (int x = 0; x < boardWidth; x++) {
                Position pos = new Position(x, y);
                if (board.isEmbedPiece(firstPiece, pos)) {
                    Board newBoard = board.placePiece(firstPiece, pos);

                    List<Position> partialSolution = new ArrayList<>();
                    partialSolution.add(pos);
                    // # start step 1
                    Future<List<Position>> future = executor.submit(() -> {
                        if (solve(1, newBoard, pieces, partialSolution)) {
                            return partialSolution;
                        } else {
                            return null;
                        }
                    });
                    futures.add(future);
                }
            }
        }

        try {
            for (Future<List<Position>> future : futures) {
                List<Position> result = future.get();
                if (result != null) {
                    solutionFound = true;
                    solutionList.addAll(result);
                    executor.shutdownNow();
                    return true;
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        executor.shutdown();
        return false;
    }

    private static boolean solve(int step, Board board, List<Piece> pieces, List<Position> solutionList) {
        if (solutionFound) {
            return true;
        }

        if (step == pieces.size()) {
            return isSolved(board);
        }

        Piece currentPiece = pieces.get(step);
        int[][] matrix = board.getBoardMatrix();

        List<PriorityPosition> positionList = posPosition(board, currentPiece).stream()
                .map(position -> {
                    var currentPositionWeight = calculate(matrix, currentPiece.getPieceMatrix(), position.x(), position.y());
                    return new PriorityPosition(position, currentPositionWeight);})
                .collect(Collectors.toUnmodifiableList());
        Queue<PriorityPosition> positionDeque = new PriorityQueue<>(positionList);

        while (!positionDeque.isEmpty()) {
            if (solutionFound) {
                return true;
            }
            Position pos = positionDeque.remove();
            Board newBoard = board.placePiece(currentPiece, pos);
            solutionList.add(pos);
            if (solve(step + 1, newBoard, pieces, solutionList)) {
                return true;
            }
            solutionList.remove(solutionList.size() - 1);
        }
        return false;
    }

    private static boolean isSolved(Board board) {
        return Arrays.stream(board.getBoardMatrix())
                .flatMapToInt(Arrays::stream)
                .sum() == 0;
    }
}
