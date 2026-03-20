package com.wms.controller;

import com.wms.entity.Booking;
import com.wms.service.BookingService;
import com.wms.service.PackageService;
import com.wms.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private ProfileService profileService;

    @Autowired
    private PackageService packageService;

    @GetMapping
    public String listBookings(@RequestParam(value = "search", required = false) String search, Model model) {
        if (search != null && !search.isEmpty()) {
            model.addAttribute("bookings", bookingService.searchBookings(search));
            model.addAttribute("search", search);
        } else {
            model.addAttribute("bookings", bookingService.getAllBookings());
        }
        model.addAttribute("totalBookings", bookingService.getTotalBookings());
        model.addAttribute("confirmedBookings", bookingService.getConfirmedBookings());
        return "admin/bookings";
    }

    @GetMapping("/new")
    public String showAddForm(Model model) {
        model.addAttribute("booking", new Booking());
        model.addAttribute("customers", profileService.getAllCustomers());
        model.addAttribute("packages", packageService.getAllPackages());
        model.addAttribute("formTitle", "New Booking");
        model.addAttribute("formAction", "/admin/bookings/save");
        return "admin/booking-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return bookingService.getBookingById(id).map(booking -> {
            model.addAttribute("booking", booking);
            model.addAttribute("customers", profileService.getAllCustomers());
            model.addAttribute("packages", packageService.getAllPackages());
            model.addAttribute("formTitle", "Edit Booking");
            model.addAttribute("formAction", "/admin/bookings/update/" + id);
            return "admin/booking-form";
        }).orElseGet(() -> {
            ra.addFlashAttribute("errorMsg", "Booking not found!");
            return "redirect:/admin/bookings";
        });
    }

    @PostMapping("/save")
    public String saveBooking(@ModelAttribute Booking booking,
                              @RequestParam(value = "customerId") Long customerId,
                              @RequestParam(value = "packageId", required = false) Long packageId,
                              RedirectAttributes ra) {
        try {
            bookingService.createBooking(booking, customerId, packageId);
            ra.addFlashAttribute("successMsg", "Booking created successfully!");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/bookings";
    }

    @PostMapping("/update/{id}")
    public String updateBooking(@PathVariable Long id,
                                @ModelAttribute Booking booking,
                                @RequestParam(value = "packageId", required = false) Long packageId,
                                RedirectAttributes ra) {
        try {
            bookingService.updateBooking(id, booking, packageId);
            ra.addFlashAttribute("successMsg", "Booking updated successfully!");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/bookings";
    }

    @GetMapping("/delete/{id}")
    public String deleteBooking(@PathVariable Long id, RedirectAttributes ra) {
        try {
            bookingService.deleteBooking(id);
            ra.addFlashAttribute("successMsg", "Booking deleted successfully!");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/bookings";
    }

    @GetMapping("/history/{customerId}")
    public String bookingHistory(@PathVariable Long customerId, Model model) {
        model.addAttribute("bookings", bookingService.getBookingHistory(customerId));
        model.addAttribute("historyMode", true);
        return "admin/bookings";
    }
}
