package com.anmi;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TcpIpServerTest {

    private static TcpIpServer server;

    @BeforeAll
    static void setUp() {
        server = new TcpIpServer(5555);
        new Thread(() -> server.start()).start();
    }

    @AfterAll
    static void tearDown() {
        server.stop();
    }

    @Test
    void serverShouldHandleMultipleClientsInParallel() throws InterruptedException {
        int threadCount = 20;
        CyclicBarrier barrier = new CyclicBarrier(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                System.out.println(Thread.currentThread().getName() + " was run and waiting others");
                try {
                    barrier.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
                createClientTestActivity();
                latch.countDown();
            }).start();
        }
        boolean result = latch.await(2000, TimeUnit.MILLISECONDS);
        assertTrue(result, "Check exception log in console");
    }

    private void createClientTestActivity() {
        TcpIpClient client = new TcpIpClient();
        client.startConnection("127.0.0.1", 5555);
        String msg1Resp = client.sendMessage("Banzai");
        String msg2Resp = client.sendMessage("Server");
        String terminateResp = client.sendMessage(".");

        assertEquals("Hello", msg1Resp);
        assertEquals("Client", msg2Resp);
        assertEquals("bye", terminateResp);
        client.stopConnection();
    }
}