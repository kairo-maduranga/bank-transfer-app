package com.example.bankapp.controller;

import com.example.bankapp.dto.TransferRequest;
import com.example.bankapp.model.Customer;
import com.example.bankapp.model.TransactionStatus;
import com.example.bankapp.service.BankService;
import com.example.bankapp.service.TransactionAuditService;
import com.example.bankapp.repository.CustomerRepository;
import com.example.bankapp.repository.BankTransactionRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequiredArgsConstructor
public class BankController {

    private final BankService bankService;
    private final TransactionAuditService auditService;
    private final CustomerRepository customerRepository;
    private final BankTransactionRepository transactionRepository;

    @GetMapping("/")
    public String dashboard(@RequestParam(required = false) String keyword, Model model) {
        model.addAttribute("totalCustomers", customerRepository.count());
        model.addAttribute("totalBalance", customerRepository.getTotalBalance());
        model.addAttribute("successCount", transactionRepository.countByStatus(TransactionStatus.SUCCESS));
        model.addAttribute("failedCount", transactionRepository.countByStatus(TransactionStatus.FAILED));
        model.addAttribute("allCustomers", customerRepository.findAllOrdered());

        if (keyword != null && !keyword.isEmpty()) {
            model.addAttribute("customers", customerRepository.searchByName(keyword));
        } else {
            model.addAttribute("customers", customerRepository.findAllOrdered());
        }
        model.addAttribute("keyword", keyword);
        model.addAttribute("transactions", transactionRepository.findAllLatest());
        model.addAttribute("transferRequest", new TransferRequest());
        model.addAttribute("customer", new Customer());
        return "bank";
    }

    @PostMapping("/transfer")
    public String transfer(@Valid @ModelAttribute TransferRequest request, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Validation failed! Check inputs.");
            return "redirect:/";
        }
        
        try {
            bankService.transfer(request.getFromAccount(), request.getToAccount(), request.getAmount(), request.isSimulateFailure());
            redirectAttributes.addFlashAttribute("success", "Transfer completed successfully!");
        } catch (Exception e) {
            auditService.recordFailure(request.getFromAccount(), request.getToAccount(), request.getAmount(), e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage() + " (All changes were rolled back)");
        }
        return "redirect:/";
    }

    @PostMapping("/customers")
    public String addCustomer(@ModelAttribute Customer customer, RedirectAttributes redirectAttributes) {
        try {
            bankService.createCustomer(customer);
            redirectAttributes.addFlashAttribute("success", "Customer added successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/";
    }

    // --- Task 12 Aluth Endpoints ---

    // History pituwa (Update karapu eka)[cite: 10]
    @GetMapping("/accounts/{accountNumber}/history")
    public String history(@PathVariable String accountNumber, Model model) {
        Customer c = customerRepository.getByAccountNumber(accountNumber).orElseThrow();
        model.addAttribute("customer", c);
        model.addAttribute("transactions", transactionRepository.findHistory(accountNumber));
        
        BigDecimal totalSent = transactionRepository.getTotalSentByAccount(accountNumber);
        model.addAttribute("totalSent", totalSent != null ? totalSent : BigDecimal.ZERO);
        
        return "history";
    }

    // Large Transfers pituwa[cite: 10]
    @GetMapping("/large-transfers")
    public String largeTransfers(@RequestParam(defaultValue = "10000") BigDecimal min, Model model) {
        model.addAttribute("min", min);
        model.addAttribute("transfers", transactionRepository.findLargeTransfers(min));
        return "large-transfers";
    }

    // Deposit pituwata yanna (GET)[cite: 10]
    @GetMapping("/deposit")
    public String depositPage(Model model) {
        model.addAttribute("customers", customerRepository.findAllOrdered());
        return "deposit";
    }

    // Deposit eka karanna (POST)[cite: 10]
    @PostMapping("/deposit")
    public String deposit(@RequestParam String accountNumber, @RequestParam BigDecimal amount, RedirectAttributes redirectAttributes) {
        try {
            bankService.deposit(accountNumber, amount);
            redirectAttributes.addFlashAttribute("success", "LKR " + amount + " deposited successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/deposit";
    }

    // Withdraw pituwata yanna (GET)[cite: 10]
    @GetMapping("/withdraw")
    public String withdrawPage(Model model) {
        model.addAttribute("customers", customerRepository.findAllOrdered());
        return "withdraw";
    }

    // Withdraw eka karanna (POST)[cite: 10]
    @PostMapping("/withdraw")
    public String withdraw(@RequestParam String accountNumber, @RequestParam BigDecimal amount, RedirectAttributes redirectAttributes) {
        try {
            bankService.withdraw(accountNumber, amount);
            redirectAttributes.addFlashAttribute("success", "LKR " + amount + " withdrawn successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/withdraw";
    }
}