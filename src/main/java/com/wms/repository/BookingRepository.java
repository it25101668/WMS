package com.wms.repository;

import com.wms.entity.Booking;
import com.wms.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByCustomer(Customer customer);

    List<Booking> findByStatus(String status);

    List<Booking> findByEventDateBetween(LocalDate startDate, LocalDate endDate);

    List<Booking> findByCustomerOrderByBookingDateDesc(Customer customer);

    List<Booking> findAllByOrderByBookingDateDesc();

    @Query("SELECT b FROM Booking b JOIN b.customer c WHERE " +
           "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.venue) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.status) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Booking> searchBookings(@Param("keyword") String keyword);
}
