import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

class MessageQueue {
    private final BlockingQueue<String> queue = new LinkedBlockingQueue<>();

    public void produce(String message) throws InterruptedException {
        queue.put(message);
        System.out.println("Produced: " + message);
    }

    public String consume() throws InterruptedException {
        String message = queue.take();
        System.out.println("Consumed: " + message);
        return message;
    }
}

class Producer extends Thread {
    private final MessageQueue messageQueue;

    public Producer(MessageQueue messageQueue) {
        this.messageQueue = messageQueue;
    }

    @Override
    public void run() {
        try {
            for (int i = 1; i <= 10; i++) {
                messageQueue.produce("Message " + i);
                Thread.sleep(100); // Simulating delay
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

class Consumer extends Thread {
    private final MessageQueue messageQueue;

    public Consumer(MessageQueue messageQueue) {
        this.messageQueue = messageQueue;
    }

    @Override
    public void run() {
        try {
            while (true) {
                messageQueue.consume();
                Thread.sleep(150); // Simulating delay
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

public class DistributedMessageQueue {
    public static void main(String[] args) {
        MessageQueue messageQueue = new MessageQueue();
        Producer producer = new Producer(messageQueue);
        Consumer consumer1 = new Consumer(messageQueue);
        Consumer consumer2 = new Consumer(messageQueue);

        producer.start();
        consumer1.start();
        consumer2.start();
    }
}
