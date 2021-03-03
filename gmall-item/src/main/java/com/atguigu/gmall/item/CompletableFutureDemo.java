package com.atguigu.gmall.item;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class CompletableFutureDemo {

    public static void main(String[] args) {
//        FutureTask<String> futureTask = new FutureTask<String>(new MyCallable());
//        new Thread(futureTask).start();
//        try {
//            System.out.println("这是主线程");
//            System.out.println(futureTask.get());
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        }

        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("这是runAsync方法开启异步任务");
//            int i = 12 / 0;
            return "hello";
        });
        CompletableFuture<String> future1 = future.thenApplyAsync(t -> {
            System.out.println("获取上个任务的结果集" + t);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("执行自己的任务thenApplyAsync111--------------");
            return "thenApplyAsync1";
        });
        CompletableFuture<String> future2 = future.thenApplyAsync(t -> {
            System.out.println("获取上个任务的结果集" + t);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("执行自己的任务thenApplyAsync2222--------------");
            return "thenApplyAsync2";
        });
        CompletableFuture<String> future3 = future.thenApplyAsync(t -> {
            System.out.println("获取上个任务的结果集" + t);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("执行自己的任务thenApplyAsync33333--------------");
            return "thenApplyAsync3";
        });
//                .whenCompleteAsync((t, u) -> {
//            System.out.println("获取上一个任务的返回结果集t:" + t);
//            System.out.println("获取上一个任务的异常信息u:" + u);
//        }).exceptionally(t -> {
//            System.out.println("获取上一个任务的异常信息t:" + t);
//            System.out.println("出现异常信息后执行的业务");
//            return "异常信息";
//        });
        try {
//            System.out.println(future.get());
            CompletableFuture.allOf(future1, future2, future3).join();
            System.out.println("这是主线程");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

class MyCallable implements Callable<String> {

    public String call() throws Exception {
        System.out.println("这是callable开启线程执行任务");
        return "hello callable";
    }
}