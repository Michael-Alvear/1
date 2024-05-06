package com.shepherdmoney.interviewproject.controller;

import com.shepherdmoney.interviewproject.model.BalanceHistory;
import com.shepherdmoney.interviewproject.model.CreditCard;
import com.shepherdmoney.interviewproject.model.User;
import com.shepherdmoney.interviewproject.repository.CreditCardRepository;
import com.shepherdmoney.interviewproject.repository.UserRepository;
import com.shepherdmoney.interviewproject.vo.request.AddCreditCardToUserPayload;
import com.shepherdmoney.interviewproject.vo.request.UpdateBalancePayload;
import com.shepherdmoney.interviewproject.vo.response.CreditCardView;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;


import java.time.LocalDate;
import java.util.Optional;

@RestController
public class CreditCardController {

    // TODO: wire in CreditCard repository here (~1 line)
    CreditCardRepository creditCardRepository;
    UserRepository userRepository;

    @PostMapping("/credit-card")
    public ResponseEntity<Integer> addCreditCardToUser(@RequestBody AddCreditCardToUserPayload payload) {
        // TODO: Create a credit card entity, and then associate that credit card with user with given userId
        //       Return 200 OK with the credit card id if the user exists and credit card is successfully associated with the user
        //       Return other appropriate response code for other exception cases
        //       Do not worry about validating the card number, assume card number could be any arbitrary format and length
        
        Optional<User> user = userRepository.findById(payload.getUserId());

        // Return 404 Not Found if user does not exist.
        if (user.isEmpty()) {
            return new ResponseEntity<>(payload.getUserId(), HttpStatus.NOT_FOUND);
        }

        // Create new credit card and assign its ownership to user.
        CreditCard creditCard = new CreditCard();
        creditCard.setOwner(user.get());
        creditCard.setNumber(payload.getCardNumber());

        creditCardRepository.save(creditCard);

        // Return 200 OK if user exists and credit card is successfully associated with the user
        return ResponseEntity.ok(creditCard.getId());

    }

    @GetMapping("/credit-card:all")
    public ResponseEntity<List<CreditCardView>> getAllCardOfUser(@RequestParam int userId) {
        // TODO: return a list of all credit card associated with the given userId, using CreditCardView class
        //       if the user has no credit card, return empty list, never return null
        Optional<User> user = userRepository.findById(userId);

        // Return 404 Not Found with empty list if user does not exist.
        if (user.isEmpty()) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.NOT_FOUND);
        }
        // Create list of all credit cards associated with a user.
        List<CreditCardView> creditCardViews = user.get().getCreditCards().stream().map(creditCard -> new CreditCardView(creditCard.getIssuanceBank(), creditCard.getNumber())).toList();

        // Return 200 OK with potentially empty list of user's credit cards.
        return ResponseEntity.ok(creditCardViews);
    }


    @GetMapping("/credit-card:user-id")
    public ResponseEntity<Integer> getUserIdForCreditCard(@RequestParam String creditCardNumber) {
        // TODO: Given a credit card number, efficiently find whether there is a user associated with the credit card
        //       If so, return the user id in a 200 OK response. If no such user exists, return 400 Bad Request
        Optional<CreditCard> creditCard = creditCardRepository.findByNumber(creditCardNumber);
        if (creditCard.isPresent()) {
            return ResponseEntity.ok(creditCard.get().getOwner().getId());
        }
        return ResponseEntity.badRequest().build();
    }


    @PostMapping("/credit-card:update-balance")
    public ResponseEntity<Void> updateBalances(@RequestBody UpdateBalancePayload[] payload) {
        //TODO: Given a list of transactions, update credit cards' balance history.
        //      1. For the balance history in the credit card
        //      2. If there are gaps between two balance dates, fill the empty date with the balance of the previous date
        //      3. Given the payload `payload`, calculate the balance different between the payload and the actual balance stored in the database
        //      4. If the different is not 0, update all the following budget with the difference
        //      For example: if today is 4/12, a credit card's balanceHistory is [{date: 4/12, balance: 110}, {date: 4/10, balance: 100}],
        //      Given a balance amount of {date: 4/11, amount: 110}, the new balanceHistory is
        //      [{date: 4/12, balance: 120}, {date: 4/11, balance: 110}, {date: 4/10, balance: 100}]
        //      This is because
        //      1. You would first populate 4/11 with previous day's balance (4/10), so {date: 4/11, amount: 100}
        //      2. And then you observe there is a +10 difference
        //      3. You propagate that +10 difference until today
        //      Return 200 OK if update is done and successful, 400 Bad Request if the given card number
        //        is not associated with a card.
        
        Arrays.sort(payload, Comparator.comparing(UpdateBalancePayload::getTransactionTime));
        // Truncate UpdateBalancePayload transactionTimes to start of day.
        for (UpdateBalancePayload update : payload) {
            // update.setTransactionTime(update.getTransactionTime().truncatedTo(ChronoUnit.DAYS));
            // Truncate the transaction time to days
            LocalDate truncatedDate = LocalDate.of(update.getTransactionTime().getYear(), 
            update.getTransactionTime().getMonth(), 
            update.getTransactionTime().getDayOfMonth());

            // Set the truncated date back to the update object
            update.setTransactionTime(truncatedDate);
        }

        for (UpdateBalancePayload update : payload) {
            Optional<CreditCard> optionalCreditCard = creditCardRepository.findByNumber(update.getCreditCardNumber());

            // Return 400 Bad Request if card number does not correspond to a card.
            if (optionalCreditCard.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            CreditCard creditCard = optionalCreditCard.get();
            List<BalanceHistory> balanceHistories = creditCard.getBalanceHistories();
            Optional<BalanceHistory> currBalanceHistory = creditCard.getBalanceHistories().stream().filter(history -> history.getDate().equals(update.getTransactionTime())).findFirst();

            // Add a new balanceHistory if it does not exist, else update an existing one.
            if (currBalanceHistory.isEmpty()) {
                balanceHistories.add(new BalanceHistory(update.getTransactionTime(), update.getTransactionAmount(), creditCard));
            } else {
                currBalanceHistory.get().setBalance(currBalanceHistory.get().getBalance() + update.getTransactionAmount());
            }
            creditCardRepository.save(creditCard);
        }
        return ResponseEntity.ok().build();
    }
    
}

