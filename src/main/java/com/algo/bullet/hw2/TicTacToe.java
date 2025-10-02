package com.algo.bullet.hw2;

import java.util.*;

public class TicTacToe {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("=== Крестики-нолики 3x3 ===");
        System.out.print("Имя игрока 1 (X): ");
        String name1 = readNonEmpty(sc);

        System.out.print("Играть против компьютера? (y/n): ");
        boolean vsAi = readYesNo(sc);

        Player p1 = new HumanPlayer(name1, 'X');
        Player p2;
        if (vsAi) {
            p2 = new AIPlayer("Компьютер", 'O');
        } else {
            System.out.print("Имя игрока 2 (O): ");
            String name2 = readNonEmpty(sc);
            p2 = new HumanPlayer(name2, 'O');
        }

        Game game = new Game(new Board(), p1, p2);
        game.startGame(sc);
        sc.close();
    }

    private static String readNonEmpty(Scanner sc) {
        String s;
        do {
            s = sc.nextLine().trim();
        } while (s.isEmpty());
        return s;
    }

    private static boolean readYesNo(Scanner sc) {
        while (true) {
            String s = sc.nextLine().trim().toLowerCase(Locale.ROOT);
            if (s.equals("y") || s.equals("yes") || s.equals("д") || s.equals("да")) return true;
            if (s.equals("n") || s.equals("no") || s.equals("н") || s.equals("нет")) return false;
            System.out.print("Ответьте y/n: ");
        }
    }
}

/**
 * Модель игрового поля 3x3 (композиция в Game).
 */
class Board {
    private final char[][] grid = new char[3][3];

    public Board() {
        for (int r = 0; r < 3; r++) Arrays.fill(grid[r], ' ');
    }

    public void displayBoard() {
        System.out.println();
        System.out.println("   1   2   3 ");
        for (int r = 0; r < 3; r++) {
            System.out.printf("%d  %c | %c | %c %n", r + 1, grid[r][0], grid[r][1], grid[r][2]);
            if (r < 2) System.out.println("  ---+---+---");
        }
        System.out.println();
    }

    /**
     * Пытается поставить метку; возвращает true, если клетка была свободна и ход сделан.
     */
    public boolean placeMark(int row, int col, char mark) {
        if (row < 0 || row >= 3 || col < 0 || col >= 3) return false;
        if (grid[row][col] != ' ') return false;
        grid[row][col] = mark;
        return true;
    }

    public boolean isFull() {
        for (char[] row : grid) for (char c : row) if (c == ' ') return false;
        return true;
    }

    public boolean checkWin(char m) {
        for (int i = 0; i < 3; i++) {
            if (grid[i][0] == m && grid[i][1] == m && grid[i][2] == m) return true;
            if (grid[0][i] == m && grid[1][i] == m && grid[2][i] == m) return true;
        }
        // диагонали
        return (grid[0][0] == m && grid[1][1] == m && grid[2][2] == m) ||
                (grid[0][2] == m && grid[1][1] == m && grid[2][0] == m);
    }

    /**
     * Список свободных клеток для ИИ.
     */
    public List<int[]> emptyCells() {
        List<int[]> list = new ArrayList<>();
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 3; c++)
                if (grid[r][c] == ' ') list.add(new int[]{r, c});
        return list;
    }
}

/**
 * Базовый класс игрока (наследование).
 */
abstract class Player {
    protected final String name;
    protected final char mark;

    protected Player(String name, char mark) {
        this.name = name;
        this.mark = mark;
    }

    public String getName() {
        return name;
    }

    public char getMark() {
        return mark;
    }

    /**
     * Сделать ход. Возвращает true, если ход выполнен.
     */
    public abstract boolean makeMove(Board board, Scanner sc);
}

/**
 * Игрок-человек: вводит координаты.
 */
class HumanPlayer extends Player {
    public HumanPlayer(String name, char mark) {
        super(name, mark);
    }

    @Override
    public boolean makeMove(Board board, Scanner sc) {
        while (true) {
            System.out.printf("%s (%c), введите ход как 'строка столбец' (1..3 1..3): ", name, mark);
            String line = sc.nextLine().trim();
            String[] parts = line.split("\\s+");
            if (parts.length != 2) {
                System.out.println("Нужно два числа, например: 2 3");
                continue;
            }
            try {
                int r = Integer.parseInt(parts[0]) - 1;
                int c = Integer.parseInt(parts[1]) - 1;
                if (board.placeMark(r, c, mark)) return true;
                System.out.println("Клетка занята или вне диапазона. Попробуйте ещё раз.");
            } catch (NumberFormatException e) {
                System.out.println("Некорректный ввод. Используйте числа 1..3.");
            }
        }
    }
}

/**
 * Простой ИИ: случайный допустимый ход.
 */
class AIPlayer extends Player {
    private final Random rnd = new Random();

    public AIPlayer(String name, char mark) {
        super(name, mark);
    }

    @Override
    public boolean makeMove(Board board, Scanner sc) {
        List<int[]> empty = board.emptyCells();
        if (empty.isEmpty()) return false;
        int[] pick = empty.get(rnd.nextInt(empty.size()));
        board.placeMark(pick[0], pick[1], mark);
        System.out.printf("%s (%c) сходил: %d %d%n", name, mark, pick[0] + 1, pick[1] + 1);
        return true;
    }
}

/**
 * Оркестратор игры: содержит Board и двух Players (композиция).
 */
class Game {
    private final Board board;
    private final Player player1;
    private final Player player2;
    private Player currentPlayer;

    public Game(Board board, Player player1, Player player2) {
        this.board = board;
        this.player1 = player1;
        this.player2 = player2;
        this.currentPlayer = player1;
    }

    public void startGame(Scanner sc) {
        while (true) {
            board.displayBoard();
            if (!currentPlayer.makeMove(board, sc)) {
                System.out.println("Ход сделать невозможно.");
                return;
            }

            if (checkForWinOrDraw()) return;
            switchPlayer();
        }
    }

    private void switchPlayer() {
        currentPlayer = (currentPlayer == player1) ? player2 : player1;
    }

    /**
     * Возвращает true, если игра закончена (победа или ничья).
     */
    private boolean checkForWinOrDraw() {
        if (board.checkWin(currentPlayer.getMark())) {
            board.displayBoard();
            System.out.printf("Победил %s (%c)!%n", currentPlayer.getName(), currentPlayer.getMark());
            return true;
        }
        if (board.isFull()) {
            board.displayBoard();
            System.out.println("Ничья!");
            return true;
        }
        return false;
    }
}
