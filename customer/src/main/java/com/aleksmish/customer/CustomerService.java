package com.aleksmish.customer;

import com.aleksmish.amqp.RabbitMQMessageProducer;
import com.aleksmish.clients.fraud.FraudCheckResponse;
import com.aleksmish.clients.fraud.FraudClient;
import com.aleksmish.clients.notification.NotificationClient;
import com.aleksmish.clients.notification.NotificationRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final FraudClient fraudClient;
    private final RabbitMQMessageProducer rabbitMQMessageProducer;

    public void registerCustomer(CustomerRegistrationRequest request) {
        Customer customer = Customer.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .build();

        customerRepository.saveAndFlush(customer);

        FraudCheckResponse fraudCheckResponse = fraudClient.isFraudster(customer.getId());

        if (fraudCheckResponse.isFraudster()) {
            throw new IllegalStateException("fraudster");
        }

        NotificationRequest notificationRequest = new NotificationRequest(
            customer.getId(),
            customer.getEmail(),
            String.format("Hi, %s, welcome to aleksmish...",
                    customer.getFirstName()
        ));

        rabbitMQMessageProducer.publish(
                notificationRequest,
                "internal.exchange",
                "internal.notification.routing-key"
        );
    }
}
