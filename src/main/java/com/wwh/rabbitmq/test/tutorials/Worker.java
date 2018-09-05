package com.wwh.rabbitmq.test.tutorials;

import com.rabbitmq.client.*;

import java.io.IOException;

/**
 * <pre>
 * 消费消息
 * 一个发布者，多个消费者
 * 
 * 消息发布类：NewTask
 * 队列将自动创建
 * 
 * 运行两三个worker实例进行测试
 * </pre>
 * 
 * <pre>
 * 忘记手动确认消息(basicAck)
 * 会导致消息被重传，RabbitMQ占用的内存也会越来越多，因为它无法释放任何未经处理的消息。
 * 为了调试这种错误，可以使用rabbitmqctl 来打印messages_unacknowledged字段：
 * 
 * sudo rabbitmqctl list_queues name messages_ready messages_unacknowledged
 * </pre>
 * 
 * <pre>
 * 消息持久性
 * 如果RabbitMQ服务器停止、退出或崩溃时，队列和消息将丢失
 * 要持久化消息需要将队列和消息都标记为持久。
 * 
 * 持久化队列
 * boolean durable = true ;
 * channel.queueDeclare（“hello”，durable，false，false，null）;
 * 如果已经有一个队列‘hello’而它是非持久，此时会出错
 * RabbitMQ不允许使用不同的参数重新定义现有队列
 * 
 * 持久化消息
 * 通过将MessageProperties（实现BasicProperties）设置为值PERSISTENT_TEXT_PLAIN。
 * channel.basicPublish("","task_queue",MessageProperties.PERSISTENT_TEXT_PLAIN,message.getBytes());
 * 
 * </pre>
 *
 * 
 */
public class Worker {

	private static final String TASK_QUEUE_NAME = "task_queue";

	public static void main(String[] argv) throws Exception {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		final Connection connection = factory.newConnection();
		final Channel channel = connection.createChannel();

		channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null);
		System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

		// 服务器将交付的消息的最大数量，如果为0无限制
		channel.basicQos(1);

		/**
		 * 默认情况下，RabbitMQ将按顺序将每条消息发送给下一个消费者。
		 * 
		 */
		final Consumer consumer = new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
				String message = new String(body, "UTF-8");

				System.out.println(" [x] Received '" + message + "'");
				try {
					doWork(message);
				} finally {
					System.out.println(" [x] Done");
					// 确认一条或多条收到的消息
					// 第二个参数
					// true : 确认所以消息，直到并包括提供的交付标签
					// false : 仅确认提供的标签
					channel.basicAck(envelope.getDeliveryTag(), false);
					/**
					 * 为了确保消息永不丢失，RabbitMQ支持 消息确认。
					 * 消费者发回ack（nowledgement）告诉RabbitMQ已收到，处理了特定消息，RabbitMQ可以自由删除它。
					 * 
					 * 如果消费者死亡（其通道关闭，连接关闭或TCP连接丢失）而不发送确认，RabbitMQ将理解为消息未完全处理并将重新排队。
					 * 如果其他消费者同时在线，则会迅速将其重新发送给其他消费者。
					 * 这样你就可以确保没有消息丢失，即使消费者偶尔会死亡。
					 * 
					 */
				}
			}
		};
		// 默认情况下自动发送ACK，这里我们将其关闭
		// 等待消息处理完毕再手动发送ACK
		channel.basicConsume(TASK_QUEUE_NAME, false, consumer);
	}

	private static void doWork(String task) {
		for (char ch : task.toCharArray()) {
			if (ch == '.') {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException _ignored) {
					Thread.currentThread().interrupt();
				}
			}
		}
	}
}
