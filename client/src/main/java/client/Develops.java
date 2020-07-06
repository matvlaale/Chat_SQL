package client;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.*;

public class Develops {
    private static final Logger logger = Logger.getLogger(Develops.class.getName());

    public static void main(String[] args) throws IOException {
        /*logger.log(Level.SEVERE, "KEK!");
        logger.severe("LOL!");

        Handler fileHandler = new FileHandler("log_%g.log", 4*1024, 20);
        fileHandler.setFormatter(new XMLFormatter());
        fileHandler.setLevel(Level.ALL);
        logger.addHandler(fileHandler);
        logger.setLevel(Level.ALL);
        logger.setUseParentHandlers(false);
        logger.log(Level.FINE, "hh...");

        Handler handler = new ConsoleHandler();
        handler.setFormatter(new XMLFormatter());
        handler.setLevel(Level.ALL);
        handler.setFormatter(new Formatter() {
            @Override
            public String format(LogRecord record) {
                return String.format(">>> : %s, %s", record.getMessage(), record.getLevel());
            }
        });
        logger.addHandler(handler);
        logger.setLevel(Level.ALL);
        logger.setUseParentHandlers(false);
        logger.log(Level.FINE, "hh...");*/
        ForTests ft = new ForTests();
        Tests tests = new Tests();
        int[] mas = {1, 2, 3, 4, 5, 4, 7, 8, 9};
        System.out.println(Arrays.toString(ft.after4(mas)));
        mas = new int[]{1, 1, 4, 1};
        System.out.println(ft.check14(mas));
        tests.checkFalseAnother();
        tests.checkFalseNo1();
        tests.checkFalseNo4();
        tests.checkTrue();
    }


}

class ForTests {
    public int[] after4(int[] mas) {
        int zap = -1;
        for (int i = mas.length - 1; i >= 0; i--) {
            if (mas[i] == 4) {
                zap = i + 1;
                break;
            }
        }
        if (zap == -1) throw new RuntimeException("No 4 in arr");
        int[] newMas = new int[mas.length - zap];
        System.arraycopy(mas, zap, newMas, 0, mas.length - zap);
        return newMas;
    }

    public boolean check14(int[] mas) {
        boolean[] check = {false, false, true};
        for (int el : mas) {
            if (el == 1) check[0] = true;
            else if (el == 4) check[1] = true;
            else {
                check[2] = false;
                break;
            }
        }
        return check[0] && check[1] && check[2];
    }
}

class Tests {
    private static ForTests ft;

    @BeforeEach
    public void init() {
        ft = new ForTests();
    }

    @Test
    public void afterSimple() {
        init();
        Assertions.assertArrayEquals(new int[]{1, 2, 3}, ft.after4(new int[]{4, 1, 2, 3}));
    }

    @Test
    public void afterALotOf() {
        init();
        Assertions.assertArrayEquals(new int[]{1, 2, 3}, ft.after4(new int[]{4, 4, 4, 1, 2, 3}));
    }

    @Test
    public void afterNull() {
        init();
        Assertions.assertArrayEquals(new int[]{}, ft.after4(new int[]{4, 1, 3, 5, 4, 4}));
    }

    @Test
    public void checkTrue() {
        init();
        Assertions.assertTrue(ft.check14(new int[]{1, 1, 1, 4, 4, 4}));
    }

    @Test
    public void checkFalseAnother() {
        init();
        Assertions.assertFalse(ft.check14(new int[]{1, 1, 2, 4, 4, 4}));
    }

    @Test
    public void checkFalseNo1() {
        init();
        Assertions.assertFalse(ft.check14(new int[]{4, 4, 4}));
    }

    @Test
    public void checkFalseNo4() {
        init();
        Assertions.assertFalse(ft.check14(new int[]{1, 1, 1}));
    }
}