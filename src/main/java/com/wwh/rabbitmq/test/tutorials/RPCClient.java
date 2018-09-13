package com.wwh.rabbitmq.test.tutorials;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Envelope;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;

/**
 * <pre>
 * 客户端将临时队列名和请求的ID发送到服务端
 * 服务端处理后发送响应信息到客户端指定的队列并关联请求的ID
 * </pre>
 *
 */
public class RPCClient implements AutoCloseable {

	private Connection connection;
	private Channel channel;
	private String requestQueueName = "rpc_queue";
	private String replyQueueName;

	public RPCClient() throws IOException, TimeoutException {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");

		connection = factory.newConnection();
		channel = connection.createChannel();

		// 主动声明一个由服务器命名的独占、自动删除、非持久队列
		// 这个队列与当前客户端绑定，用于接收服务端的响应
		replyQueueName = channel.queueDeclare().getQueue();
	}

	public String call(String message) throws IOException, InterruptedException {
		final String corrId = UUID.randomUUID().toString();

		/**
		 * AMQP 0-9-1协议预定义了一组带有消息的14个属性。大多数属性很少使用，但以下情况除外：
		 *  deliveryMode：将消息标记为持久性（值为2）或瞬态（任何其他值）。
		 *  contentType：用于描述编码的mime类型。例如，对于经常使用的JSON编码，将此属性设置为：application/json是一种很好的做法。
		 *  replyTo：通常用于命名回调队列。
		 *  correlationId：用于将RPC响应与请求相关联。
		 */
		AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(corrId).replyTo(replyQueueName).build();

		channel.basicPublish("", requestQueueName, props, message.getBytes("UTF-8"));

		// BlockingQueue 实现是线程安全的
		final BlockingQueue<String> response = new ArrayBlockingQueue<String>(1);

		channel.basicConsume(replyQueueName, true, new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
				if (properties.getCorrelationId().equals(corrId)) {
					// 将指定元素插入此队列中（如果立即可行且不会违反容量限制），成功时返回 true，如果当前没有可用的空间，则返回 false。
					response.offer(new String(body, "UTF-8"));
				}
			}
		});

		// 获取并移除此队列的头部，在元素变得可用之前一直等待
		return response.take();
	}

	public void close() throws IOException {
		connection.close();
	}

	public static void main(String[] argv) {
		try (RPCClient fibonacciRpc = new RPCClient()) {
			System.out.println(" [x] Requesting fib(30)");

			final String response = fibonacciRpc.call("30");
			System.out.println(" [.] Got '" + response + "'");

		} catch (IOException | TimeoutException | InterruptedException e) {
			e.printStackTrace();
		}
	}
}
