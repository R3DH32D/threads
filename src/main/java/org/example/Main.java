package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Main {
    public static void main(String[] args) {
        //Создание объектов буферов для дальнейшей работы с ними
        Buffer1 buffer1= new Buffer1();
        Buffer2 buffer2= new Buffer2();

        //Инициализация потоков и передача буферов
        Thread thread1 = new Thread(new Thread1(buffer1));
        thread1.setName("Thread 1");
        thread1.start();

        Thread thread2 = new Thread(new Thread2(buffer1, buffer2));
        thread2.setName("Thread 2");
        thread2.start();

        Thread thread3 = new Thread(new Thread3( buffer2));
        thread3.setName("Thread 3");
        thread3.start();
        try {
            thread1.join();
            thread2.join();
            thread3.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

abstract class Buffer{
    int N = 50;
    int counter = 0;
    int countRun1Thread = 0;
    int countRun2Thread = 0;
    int countRun3Thread = 0;
}
class Buffer1 extends Buffer{
    ArrayList<Integer> buf = new ArrayList<Integer>();
}
class Buffer2 extends Buffer{
    ArrayList<Float> buf =  new ArrayList<Float>();
}

class Thread1 implements Runnable{
    Buffer1 buffer1;
    Thread1(Buffer1 buffer1){
        this.buffer1=buffer1;
    }
    @Override
    public void run() {

        while (buffer1.counter < 100) {
            //при достаточном количестве сгенирированных данных освобождает монитор 1-го буфера
            synchronized (buffer1) {
                while (buffer1.buf.size() >= buffer1.N){
                    try {
                        buffer1.wait();
                    } catch (InterruptedException | IllegalMonitorStateException e) {
                    }
                }

                    buffer1.countRun1Thread++;
                    //генерирование случайных чисел
                    Random rand = new Random();
                    buffer1.buf.add(rand.nextInt(100));
                    buffer1.counter++;

            }
        }

        System.out.println("Поток 1 выполнился - " + buffer1.countRun1Thread+ " раз");
    }
}

class Thread2 implements Runnable{
    Buffer1 buffer1;
    Buffer2 buffer2;
    Thread2(Buffer1 buffer1, Buffer2 buffer2){
        this.buffer1=buffer1;
        this.buffer2=buffer2;
    }
    @Override
    public void run() {

        while (buffer1.counter <= buffer1.N || buffer1.buf.size() > 0) {

            synchronized (buffer2) {

                    synchronized (buffer1) {
                        while (buffer1.buf.size() == 0){
                            try {
                                buffer2.wait();

                            } catch (InterruptedException e) {

                            }
                        }
                        while (buffer2.buf.size() >= buffer2.N) {
                            try {
                                buffer2.wait();
                            } catch (InterruptedException e) {

                            }

                        }
                        buffer2.countRun2Thread++;
                        if (buffer1.buf.get(0) % 2 == 0) {
                            buffer2.buf.add((float) Math.sqrt(buffer1.buf.get(0)));
                        } else {
                            buffer2.buf.add((float) (buffer1.buf.get(0) * buffer1.buf.get(0)));
                        }
                        buffer2.counter++;
                        buffer1.buf.remove(0);
                        buffer1.notifyAll();

                    }

            }
        }
        System.out.println("Поток 2 выполнился - " + buffer2.countRun2Thread+ " раз");
    }
}

class Thread3 implements Runnable{
    Buffer2 buffer2;
    Thread3(Buffer2 buffer2){
        this.buffer2=buffer2;
    }
    public void run(){
        while (buffer2.counter <= buffer2.N || buffer2.buf.size() > 0) {
            synchronized (buffer2) {
                while (buffer2.counter == 0){
                    try {

                        buffer2.wait();

                    } catch (InterruptedException e) {

                    }
                }

                    //System.out.println("Thread 3 ");
                    buffer2.countRun3Thread ++;
                    //вывод данных из буфера и удаление выведенных
                    System.out.println(Collections.max(buffer2.buf));
                    buffer2.buf.remove(Collections.max(buffer2.buf));


                //вызов 2-го потока

                        buffer2.notifyAll();


            }
        }
        System.out.println("Поток 3 выполнился - " + buffer2.countRun3Thread+ " раз");
    }
}

