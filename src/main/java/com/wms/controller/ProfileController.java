package com.wms.controller;

import com.wms.entity.Customer;
import com.wms.entity.User;
import com.wms.service.ProfileService;
import com.wms.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private UserService userService;

    @Autowired
    private com.wms.service.PackageService packageService;

    @Autowired
    private com.wms.service.BookingService bookingService;

    // =================== ADMIN ===================

    @GetMapping("/login")
    public String adminLoginPage() {
        return "admin/login";
    }

    @PostMapping("/login")
    public String adminLogin(@RequestParam String username,
                             @RequestParam String password,
                             HttpSession session,
                             RedirectAttributes ra) {
        return userService.findByUsername(username).map(user -> {
            if (user.getPassword().equals(password) && "ADMIN".equals(user.getRole())) {
                session.setAttribute("adminUser", user);
                return "redirect:/admin/dashboard";
            } else {
                ra.addFlashAttribute("errorMsg", "Invalid credentials or not an admin.");
                return "redirect:/login";
            }
        }).orElseGet(() -> {
            ra.addFlashAttribute("errorMsg", "User not found.");
            return "redirect:/login";
        });
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(HttpSession session, Model model) {
        if (session.getAttribute("adminUser") == null) {
            return "redirect:/login";
        }
        model.addAttribute("adminUser", session.getAttribute("adminUser"));
        return "admin/dashboard";
    }

    @GetMapping("/admin/profile")
    public String adminProfile(HttpSession session, Model model) {
        if (session.getAttribute("adminUser") == null) {
            return "redirect:/login";
        }
        model.addAttribute("adminUser", session.getAttribute("adminUser"));
        return "admin/profile";
    }

    @PostMapping("/admin/profile/update")
    public String updateAdminProfile(@ModelAttribute User user, HttpSession session, RedirectAttributes ra) {
        User adminUser = (User) session.getAttribute("adminUser");
        if (adminUser == null) return "redirect:/login";
        try {
            User updated = userService.updateUser(adminUser.getId(), user);
            session.setAttribute("adminUser", updated);
            ra.addFlashAttribute("successMsg", "Profile updated successfully!");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/profile";
    }

    // =================== CUSTOMER ===================

    @GetMapping("/customer/login")
    public String customerLoginPage() {
        return "customer/login";
    }

    @PostMapping("/customer/login")
    public String customerLogin(@RequestParam String email,
                                @RequestParam String password,
                                HttpSession session,
                                RedirectAttributes ra) {
        return profileService.customerLogin(email, password).map(customer -> {
            session.setAttribute("customer", customer);
            return "redirect:/customer/dashboard";
        }).orElseGet(() -> {
            ra.addFlashAttribute("errorMsg", "Invalid email or password.");
            return "redirect:/customer/login";
        });
    }

    @GetMapping("/customer/register")
    public String customerRegisterPage(Model model) {
        model.addAttribute("customer", new Customer());
        return "customer/register";
    }

    @PostMapping("/customer/register")
    public String customerRegister(@ModelAttribute Customer customer, RedirectAttributes ra) {
        try {
            profileService.addCustomer(customer);
            ra.addFlashAttribute("successMsg", "Registration successful! Please login.");
            return "redirect:/customer/login";
        } catch (RuntimeException e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/customer/register";
        }
    }

    @GetMapping("/customer/dashboard")
    public String customerDashboard(HttpSession session, Model model) {
        Customer customer = (Customer) session.getAttribute("customer");
        if (customer == null) return "redirect:/customer/login";
        model.addAttribute("customer", customer);
        model.addAttribute("bookings", profileService.getCustomerBookings(customer.getId()));
        model.addAttribute("packages", packageService.getAvailablePackages());
        return "customer/dashboard";
    }

    @GetMapping("/customer/profile")
    public String customerProfile(HttpSession session, Model model) {
        Customer customer = (Customer) session.getAttribute("customer");
        if (customer == null) return "redirect:/customer/login";
        model.addAttribute("customer", customer);
        return "customer/profile";
    }

    @PostMapping("/customer/profile/update")
    public String updateCustomerProfile(@ModelAttribute Customer customer, HttpSession session, RedirectAttributes ra) {
        Customer sessionCustomer = (Customer) session.getAttribute("customer");
        if (sessionCustomer == null) return "redirect:/customer/login";
        try {
            Customer updated = profileService.updateCustomerProfile(sessionCustomer.getId(), customer);
            session.setAttribute("customer", updated);
            ra.addFlashAttribute("successMsg", "Profile updated successfully!");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/customer/profile";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/customer/bookings")
    public String customerBookings(HttpSession session, Model model) {
        Customer customer = (Customer) session.getAttribute("customer");
        if (customer == null) return "redirect:/customer/login";
        model.addAttribute("bookings", profileService.getCustomerBookings(customer.getId()));
        model.addAttribute("customer", customer);
        return "customer/bookings";
    }

    // =================== CUSTOMER BOOKING FLOW ===================

    @GetMapping("/customer/packages")
    public String customerPackages(HttpSession session, Model model) {
        if (session.getAttribute("customer") == null) return "redirect:/customer/login";
        model.addAttribute("packages", packageService.getAvailablePackages());
        return "customer/packages";
    }

    @GetMapping("/customer/packages/{id}")
    public String viewPackageDetails(@PathVariable Long id, HttpSession session, Model model, RedirectAttributes ra) {
        if (session.getAttribute("customer") == null) return "redirect:/customer/login";
        return packageService.getPackageById(id).map(pkg -> {
            model.addAttribute("pkg", pkg);
            return "customer/customer-package";
        }).orElseGet(() -> {
            ra.addFlashAttribute("errorMsg", "Package not found.");
            return "redirect:/customer/packages";
        });
    }

    @GetMapping("/customer/book/{packageId}")
    public String viewBookingForm(@PathVariable Long packageId, HttpSession session, Model model, RedirectAttributes ra) {
        if (session.getAttribute("customer") == null) return "redirect:/customer/login";
        return packageService.getPackageById(packageId).map(pkg -> {
            model.addAttribute("weddingPackage", pkg);
            return "customer/book";
        }).orElseGet(() -> {
            ra.addFlashAttribute("errorMsg", "Package not found.");
            return "redirect:/customer/packages";
        });
    }

    @PostMapping("/customer/book/process")
    public String processBooking(@RequestParam Long packageId,
                                 @ModelAttribute com.wms.entity.Booking bookingParams,
                                 HttpSession session, RedirectAttributes ra) {
        Customer customer = (Customer) session.getAttribute("customer");
        if (customer == null) return "redirect:/customer/login";
        
        try {
            bookingParams.setStatus("Pending");
            bookingService.createBooking(bookingParams, customer.getId(), packageId);
            ra.addFlashAttribute("successMsg", "Your booking request has been submitted successfully!");
            return "redirect:/customer/dashboard";
        } catch (RuntimeException e) {
            ra.addFlashAttribute("errorMsg", "Failed to submit booking: " + e.getMessage());
            return "redirect:/customer/book/" + packageId;
        }
    }
}
