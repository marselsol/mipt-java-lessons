package com.algo.bullet.hw2;

import java.util.*;

/**
 * Главный класс — точка входа в программу.
 */
public class TicTacToe {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("=== Крестики-нолики 3x3 ===");
        System.out.print("Имя игрока 1 (X): ");
        String name1 = readNonEmpty(sc);

        System.out.print("Играть против компьютера? (y/n): ");
        boolean vsAi = readYesNo(sc);

        // Используем фабрику для создания игроков
        PlayerFactory factory = new PlayerFactory();
        Player p1 = factory.createPlayer("human", name1, 'X');

        Player p2;
        if (vsAi) {
            p2 = factory.createPlayer("ai", "Компьютер", 'O');
        } else {
            System.out.print("Имя игрока 2 (O): ");
            String name2 = readNonEmpty(sc);
            p2 = factory.createPlayer("human", name2, 'O');
        }

        // Используем Builder для создания доски
        Board board = new Board.Builder()
                .withSize(3)
                .withEmptySymbol(' ')
                .build();

        // Singleton + Dependency Injection
        Game game = Game.getInstance(board, p1, p2);
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
 * Шаблон Builder для гибкого создания поля.
 */
class Board {
    private final char[][] grid;
    private final int size;
    private final char emptySymbol;

    private Board(Builder builder) {
        this.size = builder.size;
        this.emptySymbol = builder.emptySymbol;
        this.grid = new char[size][size];
        for (int r = 0; r < size; r++) Arrays.fill(grid[r], emptySymbol);
    }

    public void displayBoard() {
        System.out.println();
        System.out.print("   ");
        for (int i = 1; i <= size; i++) System.out.print(i + "   ");
        System.out.println();
        for (int r = 0; r < size; r++) {
            System.out.printf("%d ", r + 1);
            for (int c = 0; c < size; c++) {
                System.out.print(" " + grid[r][c]);
                if (c < size - 1) System.out.print(" |");
            }
            System.out.println();
            if (r < size - 1) {
                System.out.print("  ");
                for (int c = 0; c < size; c++) {
                    System.out.print("---");
                    if (c < size - 1) System.out.print("+");
                }
                System.out.println();
            }
        }
        System.out.println();
    }

    public boolean placeMark(int row, int col, char mark) {
        if (row < 0 || row >= size || col < 0 || col >= size) return false;
        if (grid[row][col] != emptySymbol) return false;
        grid[row][col] = mark;
        return true;
    }

    public boolean isFull() {
        for (char[] row : grid)
            for (char c : row)
                if (c == emptySymbol) return false;
        return true;
    }

    public boolean checkWin(char m) {
        // Проверка строк и столбцов
        for (int i = 0; i < size; i++) {
            if (checkLine(m, i, 0, 0, 1) || checkLine(m, 0, i, 1, 0)) return true;
        }
        // Диагонали
        return checkLine(m, 0, 0, 1, 1) || checkLine(m, 0, size - 1, 1, -1);
    }

    private boolean checkLine(char mark, int startRow, int startCol, int dRow, int dCol) {
        for (int i = 0; i < size; i++) {
            if (grid[startRow + i * dRow][startCol + i * dCol] != mark)
                return false;
        }
        return true;
    }

    public List<int[]> emptyCells() {
        List<int[]> list = new ArrayList<>();
        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++)
                if (grid[r][c] == emptySymbol) list.add(new int[]{r, c});
        return list;
    }

    /** Builder для Board */
    public static class Builder {
        private int size = 3;
        private char emptySymbol = ' ';

        public Builder withSize(int size) {
            this.size = size;
            return this;
        }

        public Builder withEmptySymbol(char emptySymbol) {
            this.emptySymbol = emptySymbol;
            return this;
        }

        public Board build() {
            return new Board(this);
        }
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

    public abstract boolean makeMove(Board board, Scanner sc);
}

/**
 * Игрок-человек.
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
 * Игрок-ИИ.
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
 * Фабрика для создания игроков (Factory Pattern).
 */
class PlayerFactory {
    public Player createPlayer(String type, String name, char mark) {
        return switch (type.toLowerCase(Locale.ROOT)) {
            case "ai", "computer" -> new AIPlayer(name, mark);
            case "human", "player" -> new HumanPlayer(name, mark);
            default -> throw new IllegalArgumentException("Неизвестный тип игрока: " + type);
        };
    }
}

/**
 * Класс Game реализует Singleton и использует Dependency Injection.
 */
class Game {
    private static Game instance;

    private final Board board;
    private final Player player1;
    private final Player player2;
    private Player currentPlayer;

    // Приватный конструктор (Singleton)
    private Game(Board board, Player player1, Player player2) {
        this.board = board;
        this.player1 = player1;
        this.player2 = player2;
        this.currentPlayer = player1;
    }

    // Статический метод получения единственного экземпляра
    public static Game getInstance(Board board, Player p1, Player p2) {
        if (instance == null) {
            instance = new Game(board, p1, p2);
        }
        return instance;
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
