package com.csis231.api.Transaction;

import com.csis231.api.Ticket.Ticket;
import com.csis231.api.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Find all transactions made by a specific user
    List<Transaction> findByUser(User user);

    // Find all transactions related to a specific ticket
    List<Transaction> findByTicket(Ticket ticket);

    // Find all transactions by status (e.g., SUCCESS, PENDING, REFUNDED)
    List<Transaction> findByStatus(Transaction.TransactionStatus status);

    // Find transactions by payment method (e.g., CREDIT_CARD, CASH)
    List<Transaction> findByPaymentMethod(Transaction.PaymentMethod paymentMethod);
}
