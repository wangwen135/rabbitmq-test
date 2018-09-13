package com.wwh.rabbitmq.test.tutorials;

import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * <pre>
 * 路由
 * 消息生产者：EmitLogDirect
 * 使用指定的队列，并绑定到交换：direct_logs
 * 使用指定的路由键
 * 
 * 启动多个实例，多个实例分别消费同一个队列中的数据，一次一条
 * </pre>
 *
 */
public class ReceiveLogsDirect2 {

	private static final String EXCHANGE_NAME = "direct_logs";

	private static final String QUEUE_NAME = "info_logs";

	public static void main(String[] argv) throws Exception {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();

		// 定义一个直接交换
		channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT);

		if (argv.length < 1) {
			System.out.println("Usage: ReceiveLogsDirect [info] [warn] [error]");

			System.out.println("输入队列需要绑定的路由key 如： [debug info warn error]");
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String line = br.readLine();

			argv = line.split("\\s+");
		}

		// 持久的 非独占 非自删除 队列
		channel.queueDeclare(QUEUE_NAME, true, false, false, null);

		for (String severity : argv) {
			// 将队列绑定到指定的交换
			// 使用指定的路由键
			channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, severity);
			System.out.println("队列：" + QUEUE_NAME + " 绑定：" + severity);

		}
		System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

		// 服务器将交付的消息的最大数量，如果为0无限制
		channel.basicQos(1);

		Consumer consumer = new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
				String message = new String(body, "UTF-8");
				System.out.println(" [x] Received '" + envelope.getRoutingKey() + "':'" + message + "'");
			}
		};
		channel.basicConsume(QUEUE_NAME, true, consumer);
	}
}
