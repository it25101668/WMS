package com.wms.service;

import com.wms.entity.Booking;
import com.wms.entity.Customer;
import com.wms.entity.WeddingPackage;
import com.wms.repository.BookingRepository;
import com.wms.repository.CustomerRepository;
import com.wms.repository.PackageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PackageRepository packageRepository;

    public List<Booking> getAllBookings() {
        return bookingRepository.findAllByOrderByBookingDateDesc();
    }

    public Optional<Booking> getBookingById(Long id) {
        return bookingRepository.findById(id);
    }

    public Booking createBooking(Booking booking, Long customerId, Long packageId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + customerId));
        booking.setCustomer(customer);

        if (packageId != null) {
            WeddingPackage pkg = packageRepository.findById(packageId)
                    .orElseThrow(() -> new RuntimeException("Package not found: " + packageId));
            booking.setWeddingPackage(pkg);
            if (booking.getTotalAmount() == null) {
                booking.setTotalAmount(pkg.getPrice());
            }
        }
        return bookingRepository.save(booking);
    }

    public Booking updateBooking(Long id, Booking updatedBooking, Long packageId) {
        Booking existing = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found: " + id));
        existing.setEventDate(updatedBooking.getEventDate());
        existing.setVenue(updatedBooking.getVenue());
        existing.setGuestCount(updatedBooking.getGuestCount());
        existing.setTotalAmount(updatedBooking.getTotalAmount());
        existing.setStatus(updatedBooking.getStatus());
        existing.setNotes(updatedBooking.getNotes());

        if (packageId != null) {
            WeddingPackage pkg = packageRepository.findById(packageId)
                    .orElseThrow(() -> new RuntimeException("Package not found: " + packageId));
            existing.setWeddingPackage(pkg);
        }
        return bookingRepository.save(existing);
    }

    public void deleteBooking(Long id) {
        if (!bookingRepository.existsById(id)) {
            throw new RuntimeException("Booking not found: " + id);
        }
        bookingRepository.deleteById(id);
    }

    public List<Booking> searchBookings(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return bookingRepository.findAllByOrderByBookingDateDesc();
        }
        return bookingRepository.searchBookings(keyword.trim());
    }

    public List<Booking> getBookingHistory(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + customerId));
        return bookingRepository.findByCustomerOrderByBookingDateDesc(customer);
    }

    public long getTotalBookings() {
        return bookingRepository.count();
    }

    public long getConfirmedBookings() {
        return bookingRepository.findByStatus("Confirmed").size();
    }
}
