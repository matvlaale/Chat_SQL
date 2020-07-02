package client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.*;

public class RaceProgram {

}

class MainClass {
    public static final int CARS_COUNT = 4;
    public static String winner = "0";
    public static CyclicBarrier cb;
    public static CountDownLatch cdl;
    public static CountDownLatch lock;

    public static void main(String[] args) {
        cdl = new CountDownLatch(CARS_COUNT);
        cb = new CyclicBarrier(CARS_COUNT + 1);
        lock = new CountDownLatch(1);
        System.out.println("ВАЖНОЕ ОБЪЯВЛЕНИЕ >>> Подготовка!!!");
        Race race = new Race(new Road(60), new Tunnel(), new Road(40));
        Car[] cars = new Car[CARS_COUNT];
        for (int i = 0; i < cars.length; i++) {
            cars[i] = new Car(race, 20 + (int) (Math.random() * 10));
        }
        for (Car car : cars) {
            new Thread(car).start();
        }
        try {
            cb.await();
            System.out.println("ВАЖНОЕ ОБЪЯВЛЕНИЕ >>> Гонка началась!!!");
            new Thread(() -> {
                try {
                    lock.await();
                    System.out.println(winner + " - ПОБЕДИТЕЛЬ!!!");
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }).start();
            cdl.await();
            System.out.println("ВАЖНОЕ ОБЪЯВЛЕНИЕ >>> Гонка закончилась!!!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void win(String name) {
        winner = name;
        lock.countDown();
    }
}

class Car implements Runnable {
    private static int CARS_COUNT;

    static {
        CARS_COUNT = 0;
    }

    private Race race;
    private int speed;
    private String name;

    public String getName() {
        return name;
    }

    public int getSpeed() {
        return speed;
    }

    public Car(Race race, int speed) {
        this.race = race;
        this.speed = speed;
        CARS_COUNT++;
        this.name = "Участник #" + CARS_COUNT;
    }

    @Override
    public void run() {
        try {
            System.out.println(this.name + " готовится");
            Thread.sleep(500 + (int) (Math.random() * 800));
            System.out.println(this.name + " готов");
            MainClass.cb.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (int i = 0; i < race.getStages().size(); i++) {
            race.getStages().get(i).go(this);
        }
        MainClass.win(name);
        MainClass.cdl.countDown();
    }
}

abstract class Stage {
    protected int length;
    protected String description;

    public String getDescription() {
        return description;
    }

    public abstract void go(Car c);
}

class Road extends Stage {
    public Road(int length) {
        this.length = length;
        this.description = "Дорога " + length + " метров";
    }

    @Override
    public void go(Car c) {
        try {
            System.out.println(c.getName() + " начал этап: " + description);
            Thread.sleep(length / c.getSpeed() * 1000);
            System.out.println(c.getName() + " закончил этап: " + description);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class Tunnel extends Stage {
    int maxCars;
    Semaphore smp;

    public Tunnel() {
        this.length = 80;
        this.description = "Тоннель " + length + " метров";
        maxCars = MainClass.CARS_COUNT / 2;
        smp = new Semaphore(maxCars);
    }

    @Override
    public void go(Car c) {
        try {
            try {
                System.out.println(c.getName() + " готовится к этапу(ждет): " + description);
                smp.acquire();
                System.out.println(c.getName() + " начал этап: " + description);
                Thread.sleep(length / c.getSpeed() * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                System.out.println(c.getName() + " закончил этап: " + description);
                smp.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class Race {
    private ArrayList<Stage> stages;

    public ArrayList<Stage> getStages() {
        return stages;
    }

    public Race(Stage... stages) {
        this.stages = new ArrayList<>(Arrays.asList(stages));
    }
}