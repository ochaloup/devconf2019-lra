package io.narayana.demo.lra.devconf2019;

import java.util.List;

import javax.enterprise.context.Dependent;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import io.narayana.demo.lra.devconf2019.jpa.Booking;

@Dependent
@Transactional
public class BookingManager {
    @PersistenceContext
    private EntityManager em;

    public void save(Booking booking) {
        em.persist(booking);
    }

    @SuppressWarnings("unchecked")
    public List<Booking> getAllBookings() {
        return em.createNamedQuery("Booking.findAll").getResultList();
    }

    public void createBooking(Booking booking) {
        em.persist(booking);
    }
}
