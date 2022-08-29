package com.company;

public class Main {

    static String chessBoard[][] = {
            {"r", "k", "b", "q", "a", "b", "k", "r"},
            {"p", "p", "p", "p", "p", "p", "p", "p"},
            {" ", " ", " ", " ", " ", " ", " ", " "},
            {" ", " ", " ", " ", " ", " ", "b", " "},
            {" ", " ", " ", " ", " ", " ", " ", " "},
            {" ", " ", " ", " ", "A", " ", " ", " "},
            {"P", "P", "P", "P", "P", "P", "P", "P"},
            {"R", "K", "B", "Q", "A", "B", "K", "R"}
    };

    static int kingPositionC = 0, kingPositionL = 0;

    public static void main(String[] args) {


	    /** JFrame frame = new JFrame("Chess");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        UserInterface ui = new UserInterface();
        frame.add(ui);
        frame.setSize(UserInterface.WIDTH, UserInterface.HEIGHT);
        frame.setVisible(true); */
        while (!"A".equals(chessBoard[kingPositionC/8][kingPositionC%8])) {
            kingPositionC++;
        }
        while (!"a".equals(chessBoard[kingPositionL/8][kingPositionL%8])) {
            kingPositionL++;
        }
        System.out.println(possibleMoves());

    }

    public static String possibleMoves() {
        String listcap = "";
        for (int i  = 0 ; i < 64; i++) {
            switch (chessBoard[i/8][i%8]) {
                case "P":
                    listcap+= possibleP(i);
                    break;
                case "K":
                    listcap+= possibleK(i);
                    break;
                case "B":
                    listcap+= possibleB(i);
                    break;
                case "R":
                    listcap+= possibleR(i);
                    break;
                case "Q":
                    listcap+= possibleQ(i);
                    break;
                case "A":
                    listcap+= possibleA(i);
                    break;
            }
        }
        return listcap;
    }

    public static String possibleP(int i) {
        String list = "";
        int r = i/8, c = i%8;

        int newr = r - 1;

        while (" ".equals(chessBoard[newr][c]) && Math.abs(newr - r) <= 2) {
            list = update("P", list, r, c, newr, c);
            newr -= 1;
        }

        return list;
    }

    public static String possibleK(int i) {
        String list = "";
        int r = i/8, c = i%8;

        for (int a  = -2; a <= 2; a++) {
            for (int j = -2; j <= 2; j++) {
                if (Math.abs(a * j) == 2) {
                    try {
                        int newr = r + a;
                        int newc = c + j;

                        if (!Character.isUpperCase(chessBoard[newr][newc].charAt(0))) {
                            list = update("K", list, r, c, newr, newc);
                        }

                    } catch (Exception e) {}
                }
            }
        }
        return list;
    }
    public static String possibleR(int i) {
        String list = "";
        int r = i /8, c = i%8;
        int mul = 1;

        for (int j = -1; j <= 1; j++) {
            for (int k = -1; k <= 1; k++) {
                if (Math.abs(j * k) == 0 && j != k) {
                    try {
                        while (" ".equals(chessBoard[r+mul*j][c+mul*k])) {
                            list = update("R", list, r, c, r+mul*j, c+mul*k);
                            mul++;
                        }

                        if (Character.isLowerCase(chessBoard[r+mul*j][c+mul*k].charAt(0))) {
                            list = update("R", list, r, c, r+mul*j, c+mul*k);
                        }
                    }

                    catch (Exception e) {}
                    mul = 1;
                }
            }
        }
        return list;
    }

    public static String possibleB(int i) {
        String list = "";
        int r = i/8, c = i%8;
        int mul = 1;

        for (int j = -1; j <= 1; j++) {
            for (int k = -1; k <= 1; k++) {
                if (Math.abs(j * k) == 1) {
                    try {
                        while (" ".equals(chessBoard[r+mul*j][c+mul*k])) {
                            list = update("B", list, r, c, r+mul*j, c+mul*k);
                            mul++;
                        }

                        if (Character.isLowerCase(chessBoard[r+mul*j][c+mul*k].charAt(0))) {
                            list = update("B", list, r, c, r+mul*j, c+mul*k);
                        }
                    } catch (Exception e) {}
                    mul = 1;
                }
            }
        }

        return list;
    }

    public static String possibleQ(int i) {
        String list = "";
        int r = i/8, c = i%8;
        int mul = 1;

        for (int j = -1; j <=1; j++) {
            for (int k = -1; k <= 1; k++) {
                try {
                    while(" ".equals(chessBoard[r+mul*j][c+mul*k])) {
                        list = update("Q", list, r, c, r+mul*j, c+mul*k);
                        mul++;
                    }

                    if (Character.isLowerCase(chessBoard[r+mul*j][c+mul*k].charAt(0))) {
                        list = update("Q", list, r, c, r+mul*j, c+mul*k);
                    }

                } catch (Exception e) {}
                mul = 1;
            }
        }
        return list;
    }

    public static String possibleA(int i) {
        String list = "";
        int r = i/8, c = i%8;

        for (int j = 0; j < 9; j++) {
            if (j != 4) {
                try {
                    if (Character.isLowerCase(chessBoard[r - 1 + j / 3][c - 1 + j % 3].charAt(0))
                            || " ".equals(chessBoard[r - 1 + j / 3][c - 1 + j % 3])) {
                        int kingTemp = kingPositionC;
                        kingPositionC = i + (j / 3) * 8 + j % 3 - 9;
                        list = update("A", list, r, c, r-1+j/3, c-1+j%3);
                        kingPositionC = kingTemp;
                    }
                } catch (Exception e) {

                }
            }
        }
        return list;
    }

    public static String update(String piece, String list, int r, int c, int newr, int newc) {
        String oldpiece = chessBoard[newr][newc];
        chessBoard[r][c] = " ";
        chessBoard[newr][newc] = piece;
        if (kingSafe()) {
            list = list + r + c + newr + newc + oldpiece;
        }
        chessBoard[r][c] = piece;
        chessBoard[newr][newc] = oldpiece;

        return list;
    }

    public static boolean kingSafe() {

        int mul = 1;

        for (int i = -2; i <= 2; i++) {
            for (int j = -2; j <= 2; j++) {
                if (Math.abs(j * i) == 1) {
                    try {
                        while (" ".equals(chessBoard[kingPositionC/8+mul*i][kingPositionC%8+mul*j])) {mul++;}

                        if ("b".equals(chessBoard[kingPositionC/8+mul*i][kingPositionC%8+mul*j]) ||
                                "q".equals(chessBoard[kingPositionC/8+mul*i][kingPositionC%8+mul*j])) {
                            return false;
                        }

                    } catch (Exception e) {}
                }

                mul = 1;

                if (Math.abs(j * i) == 0 && j != i) {
                    try {
                        while (" ".equals(chessBoard[kingPositionC/8+mul*i][kingPositionC%8+mul*j])) {mul++;}

                        if ("r".equals(chessBoard[kingPositionC/8+mul*i][kingPositionC%8+mul*j])) {
                            return false;
                        }
                    } catch (Exception e) {}
                }

                mul = 1;

                if (Math.abs(j * i) == 2) {
                    try {
                        if ("k".equals(chessBoard[kingPositionC / 8 + i][kingPositionC % 8 + j])) {
                            return false;
                        }
                    } catch(Exception e) {}
                }
            }
        }

        return true;
    }


}
