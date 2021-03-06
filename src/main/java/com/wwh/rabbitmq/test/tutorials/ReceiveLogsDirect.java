package com.wwh.rabbitmq.test.tutorials;

import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * <pre>
 * 路由
 * 消息生产者：EmitLogDirect
 * 创建一个由服务器命名的独占、自动删除、非持久队列
 * 并将队列绑定到交换：direct_logs
 * 使用指定的路由键
 * </pre>
 *
 */
public class ReceiveLogsDirect {

	private static final String EXCHANGE_NAME = "direct_logs";

	public static void main(String[] argv) throws Exception {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();

		// 定义一个直接交换
		channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT);

		// 主动声明一个由服务器命名的独占、自动删除、非持久队列。
		// 获取随机的队列名称
		String queueName = channel.queueDeclare().getQueue();

		if (argv.length < 1) {
			System.out.println("Usage: ReceiveLogsDirect [info] [warn] [error]");

			System.out.println("输入队列需要绑定的路由key 如： [debug info warn error]");
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String line = br.readLine();

			argv = line.split("\\s+");
		}

		for (String severity : argv) {
			// 将队列绑定到指定的交换
			// 使用指定的路由键
			channel.queueBind(queueName, EXCHANGE_NAME, severity);
			System.out.println("队列：" + queueName + " 绑定：" + severity);

		}
		System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

		Consumer consumer = new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
				String message = new String(body, "UTF-8");
				System.out.println(" [x] Received '" + envelope.getRoutingKey() + "':'" + message + "'");
			}
		};
		channel.basicConsume(queueName, true, consumer);
	}
}
