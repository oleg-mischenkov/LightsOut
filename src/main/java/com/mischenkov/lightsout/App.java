package com.mischenkov.lightsout;


import com.mischenkov.lightsout.entity.Board;
import com.mischenkov.lightsout.entity.Piece;
import com.mischenkov.lightsout.entity.Position;
import com.mischenkov.lightsout.entity.PriorityPosition;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class App {
    private static volatile boolean solutionFound = false;

    public static void main(String[] args) {
        if(args.length == 0) {
            System.out.println("""
                    No file name
                    try:
                        java -jar lightsout <file name>
                    """);
            return;
        }

        Board initialBoard;
        List<Piece> pieceList;
        try (BufferedReader reader = new BufferedReader(new FileReader(args[0]))) {
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

        if (solveParallel(initialBoard, pieceList, solutionList)) {
            var solution = solutionList.stream().map(Position::toString).collect(Collectors.joining(" "));
            System.out.println(solution);
        } else {
            System.out.println("No solution");
        }
    }

    public static int calculateMatrixIntersectionWeight(int[][] m1, int[][] m2, int x, int y) {
        int sum = 0;
        for (int m2y = 0; m2y < m2.length; m2y++) {
            for (int m2x = 0; m2x < m2[m2y].length; m2x++) {
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

    private static boolean isPromising(Board board, long[] coverageMasks, long remainingMask) {
        int[][] matrix = board.getBoardMatrix();
        int boardHeight = matrix.length;
        int boardWidth = matrix[0].length;
        int depth = board.getDepth();

        for (int y = 0; y < boardHeight; y++) {
            for (int x = 0; x < boardWidth; x++) {
                int current = matrix[y][x];
                int stepsToZero = (depth - current) % depth;
                if (stepsToZero < 0) stepsToZero += depth;
                int cellIndex = y * boardWidth + x;
                long coverage = coverageMasks[cellIndex] & remainingMask;
                int pieceNumberForPosition = Long.bitCount(coverage);
                if ((pieceNumberForPosition == 0 && current != 0) || (stepsToZero > pieceNumberForPosition)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean solveParallel(Board board, List<Piece> pieces, List<Position> solutionList) {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Future<List<Position>>> futures = new ArrayList<>();
        Piece firstPiece = pieces.get(0);
        int[][] matrix = board.getBoardMatrix();
        int boardHeight = matrix.length;
        int boardWidth = matrix[0].length;
        long[] coverageMasks = board.computeCoverageMasks(pieces);
        long initialRemainingMask = (1L << pieces.size()) - 1;

        for (int y = 0; y < boardHeight; y++) {
            for (int x = 0; x < boardWidth; x++) {
                Position pos = new Position(x, y);
                if (board.isEmbedPiece(firstPiece, pos)) {
                    Board newBoard = board.placePiece(firstPiece, pos);
                    List<Position> partialSolution = new ArrayList<>();
                    partialSolution.add(pos);
                    long newRemainingMask = initialRemainingMask & ~(1L << 0);
                    if (isPromising(newBoard, coverageMasks, newRemainingMask)) {
                        Future<List<Position>> future = executor.submit(() -> {
                            if (solve(1, newBoard, pieces, partialSolution, coverageMasks, newRemainingMask)) {
                                return partialSolution;
                            } else {
                                return null;
                            }
                        });
                        futures.add(future);
                    }
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

    private static boolean solve(int step, Board board, List<Piece> pieces, List<Position> solutionList, long[] coverageMasks, long remainingMask) {
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
                    var currentPositionWeight = calculateMatrixIntersectionWeight(matrix, currentPiece.getPieceMatrix(), position.x(), position.y());
                    return new PriorityPosition(position, currentPositionWeight);
                })
                .collect(Collectors.toUnmodifiableList());
        Queue<PriorityPosition> positionDeque = new PriorityQueue<>(positionList);

        while (!positionDeque.isEmpty()) {
            if (solutionFound) {
                return true;
            }
            Position pos = positionDeque.remove();
            Board newBoard = board.placePiece(currentPiece, pos);
            long newRemainingMask = remainingMask & ~(1L << step);
            if (isPromising(newBoard, coverageMasks, newRemainingMask)) {
                solutionList.add(pos);
                if (solve(step + 1, newBoard, pieces, solutionList, coverageMasks, newRemainingMask)) {
                    return true;
                }
                solutionList.remove(solutionList.size() - 1);
            }
        }
        return false;
    }

    private static boolean isSolved(Board board) {
        return Arrays.stream(board.getBoardMatrix())
                .flatMapToInt(Arrays::stream)
                .sum() == 0;
    }
}
